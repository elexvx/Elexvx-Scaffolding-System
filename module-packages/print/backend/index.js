const express = require('express');
const Knex = require('knex');
const Handlebars = require('handlebars');
const sanitizeHtml = require('sanitize-html');
const puppeteer = require('puppeteer');
const axios = require('axios');
const FormData = require('form-data');

const app = express();

const PORT = Number(process.env.PORT || 3901);
const DB_TYPE = String(process.env.TDESIGN_DB_TYPE || 'mysql').toLowerCase();
const DB_URL = process.env.TDESIGN_DB_URL || '';
const DB_USER = process.env.TDESIGN_DB_USER || '';
const DB_PASSWORD = process.env.TDESIGN_DB_PASSWORD || '';
const CORE_API = process.env.TDESIGN_CORE_API_BASE || 'http://127.0.0.1:8080/api';
const MAX_CONCURRENT = Math.max(1, Math.min(Number(process.env.PRINT_MAX_CONCURRENT || 2), Math.max(2, require('os').cpus().length)));
const RENDER_TIMEOUT_MS = Math.max(10_000, Number(process.env.PRINT_RENDER_TIMEOUT_MS || 12_000));
const ENABLE_JS = String(process.env.PRINT_ENABLE_JS || 'false').toLowerCase() === 'true';
const MAX_HTML_BYTES = 300 * 1024;
const MAX_CSS_BYTES = 300 * 1024;
const MAX_DATA_BYTES = 2 * 1024 * 1024;
const CACHE_TTL_MS = Math.max(5_000, Number(process.env.PRINT_TEMPLATE_CACHE_TTL_MS || 60_000));
const CACHE_MAX = Math.max(20, Number(process.env.PRINT_TEMPLATE_CACHE_MAX || 500));

app.use(express.json({ limit: '5mb' }));

if (!['mysql', 'postgresql', 'postgres'].includes(DB_TYPE)) {
  throw new Error('当前 DB 类型未支持 print 模块后端');
}

const knex = Knex({
  client: DB_TYPE === 'postgres' || DB_TYPE === 'postgresql' ? 'pg' : 'mysql2',
  connection: DB_URL || {
    host: process.env.TDESIGN_DB_HOST || '127.0.0.1',
    user: DB_USER,
    password: DB_PASSWORD,
    database: process.env.TDESIGN_DB_NAME || '',
  },
  pool: { min: 0, max: 6 },
});

const templateCache = new Map();
let browserPromise;
const pagePool = [];
let shuttingDown = false;
const pdfQueue = [];
let running = 0;
const pendingPageResolvers = [];

function requireAuth(req, res, next) {
  const auth = req.headers.authorization || '';
  if (!auth.startsWith('Bearer ')) return res.status(401).json({ message: 'Unauthorized' });
  req.authToken = auth;
  next();
}

function checkBodyLimits(req, res, next) {
  const body = req.body || {};
  const html = body.html == null ? '' : String(body.html);
  const css = body.css == null ? '' : String(body.css);
  const dataRaw = body.data == null ? '' : JSON.stringify(body.data);
  const bodySize = Buffer.byteLength(JSON.stringify(body));
  if (Buffer.byteLength(html) > MAX_HTML_BYTES) return res.status(413).json({ message: 'html 超过大小限制(300KB)' });
  if (Buffer.byteLength(css) > MAX_CSS_BYTES) return res.status(413).json({ message: 'css 超过大小限制(300KB)' });
  if (Buffer.byteLength(dataRaw) > MAX_DATA_BYTES) return res.status(413).json({ message: 'data 超过大小限制(2MB)' });
  if (bodySize > 5 * 1024 * 1024) return res.status(413).json({ message: '请求体超过大小限制(5MB)' });
  next();
}

app.use(requireAuth);
app.use(checkBodyLimits);

const cleanTemplate = (html = '') => sanitizeHtml(html, {
  allowedTags: sanitizeHtml.defaults.allowedTags.concat(['img', 'style', 'table', 'thead', 'tbody', 'tr', 'td', 'th']),
  allowedAttributes: { '*': ['style', 'class'], img: ['src', 'alt', 'width', 'height'] },
  allowedSchemes: ['data'],
  disallowedTagsMode: 'discard',
});

function sanitizeCss(css = '') {
  const raw = String(css || '');
  const dangerous = [/@import/gi, /url\s*\(/gi, /expression\s*\(/gi, /behavior\s*:/gi, /javascript\s*:/gi];
  if (dangerous.some((re) => re.test(raw))) {
    throw new Error('css 包含危险内容');
  }
  return raw;
}

function cacheKey({ id, version, templateKey }) {
  return `${templateKey || id}:${version || 'current'}`;
}

function setTemplateCache(key, payload) {
  if (templateCache.size >= CACHE_MAX) {
    const oldestKey = templateCache.keys().next().value;
    templateCache.delete(oldestKey);
  }
  templateCache.set(key, { at: Date.now(), payload });
}

function getTemplateCache(key) {
  const val = templateCache.get(key);
  if (!val) return null;
  if ((Date.now() - val.at) > CACHE_TTL_MS) {
    templateCache.delete(key);
    return null;
  }
  templateCache.delete(key);
  templateCache.set(key, val);
  return val.payload;
}

function invalidateTemplateCache(templateId, templateKey) {
  for (const key of templateCache.keys()) {
    if (String(key).startsWith(`${templateId}:`) || (templateKey && String(key).startsWith(`${templateKey}:`))) {
      templateCache.delete(key);
    }
  }
}

async function getTemplateWithVersion({ templateId, templateKey, version }) {
  const key = cacheKey({ id: templateId, templateKey, version });
  const cached = getTemplateCache(key);
  if (cached) return cached;

  let t;
  if (templateId) {
    t = await knex('print_template').where({ id: templateId }).first();
  } else if (templateKey) {
    t = await knex('print_template').where({ template_key: templateKey, enabled: 1 }).orderBy('updated_at', 'desc').first();
  }
  if (!t) throw new Error('模板不存在');

  let merged = t;
  if (version) {
    const v = await knex('print_template_version').where({ template_id: t.id, version: Number(version) }).first();
    if (!v) throw new Error('版本不存在');
    merged = { ...t, ...v };
  } else {
    const published = await knex('print_template_version').where({ template_id: t.id, published: 1 }).orderBy('version', 'desc').first();
    if (published) merged = { ...t, ...published, version: published.version };
  }

  const safeHtml = cleanTemplate(merged.html || '');
  const safeCss = sanitizeCss(merged.css || '');
  const compiled = Handlebars.compile(safeHtml, { noEscape: false, strict: false });
  const payload = { ...merged, html: safeHtml, css: safeCss, compiled };
  setTemplateCache(key, payload);
  return payload;
}

function buildDoc(template, data) {
  const body = template.compiled({ data: data || {} });
  return `<!doctype html><html><head><meta charset="utf-8"/><style>${template.css || ''}</style></head><body>${body}</body></html>`;
}

async function getBrowser() {
  if (!browserPromise) {
    browserPromise = puppeteer.launch({ headless: 'new', args: ['--no-sandbox', '--disable-setuid-sandbox'] });
    await browserPromise;
  }
  return browserPromise;
}

function withTimeout(promise, timeoutMs, message = '操作超时') {
  let timer;
  const timeoutPromise = new Promise((_, reject) => {
    timer = setTimeout(() => reject(new Error(message)), timeoutMs);
  });
  return Promise.race([promise, timeoutPromise]).finally(() => clearTimeout(timer));
}

async function acquirePage() {
  if (shuttingDown) throw new Error('服务正在停止');
  if (pagePool.length > 0) return pagePool.pop();
  const browser = await getBrowser();
  const activePages = (await browser.pages()).length;
  if (activePages < MAX_CONCURRENT) return browser.newPage();
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => reject(new Error('获取渲染资源超时')), RENDER_TIMEOUT_MS);
    pendingPageResolvers.push((page) => {
      clearTimeout(timer);
      resolve(page);
    });
  });
}

async function resetPage(page) {
  if (!page || page.isClosed()) return;
  page.removeAllListeners();
  try { await page.setRequestInterception(false); } catch (_) {}
  try { await page.goto('about:blank', { waitUntil: 'domcontentloaded', timeout: 2_000 }); } catch (_) {}
}

async function releasePage(page) {
  if (!page || page.isClosed()) return;
  await resetPage(page);
  if (pendingPageResolvers.length > 0) {
    const waiter = pendingPageResolvers.shift();
    waiter(page);
    return;
  }
  if (pagePool.length >= MAX_CONCURRENT) {
    await page.close().catch(() => {});
    return;
  }
  pagePool.push(page);
}

async function forceRecycleBrowser() {
  if (!browserPromise) return;
  try {
    const browser = await browserPromise;
    await browser.close();
  } catch (_) {}
  browserPromise = undefined;
  pagePool.length = 0;
}

async function renderPdfBuffer(computedHtml, template) {
  const page = await acquirePage();
  try {
    await withTimeout((async () => {
      await page.setJavaScriptEnabled(ENABLE_JS);
      await page.setRequestInterception(true);
      page.on('request', (r) => {
        const url = (r.url() || '').toLowerCase();
        if (url.startsWith('data:') || url.startsWith('about:') || url.startsWith('blob:')) return r.continue();
        r.abort();
      });
      await page.setContent(computedHtml, { waitUntil: 'domcontentloaded', timeout: RENDER_TIMEOUT_MS });
    })(), RENDER_TIMEOUT_MS, '渲染超时');

    return await withTimeout(
      page.pdf({ format: template.paper_size || 'A4', landscape: String(template.orientation) === 'landscape', printBackground: true }),
      RENDER_TIMEOUT_MS,
      '导出 PDF 超时',
    );
  } catch (error) {
    if (/超时/.test(error.message || '')) await forceRecycleBrowser();
    throw error;
  } finally {
    await releasePage(page).catch(() => {});
  }
}

function enqueuePdf(fn) {
  return new Promise((resolve, reject) => {
    pdfQueue.push({ fn, resolve, reject });
    drain();
  });
}

async function drain() {
  if (running >= MAX_CONCURRENT || pdfQueue.length === 0) return;
  running += 1;
  const task = pdfQueue.shift();
  try {
    task.resolve(await withTimeout(task.fn(), RENDER_TIMEOUT_MS + 3_000, '打印任务超时'));
  } catch (e) {
    task.reject(e);
  } finally {
    running -= 1;
    drain();
  }
}

async function insertAndGetId(table, payload) {
  if (DB_TYPE === 'postgres' || DB_TYPE === 'postgresql') {
    const rows = await knex(table).insert(payload).returning('id');
    const first = rows[0];
    return Number(first && first.id !== undefined ? first.id : first);
  }
  const [id] = await knex(table).insert(payload);
  return Number(id);
}

app.get('/templates', async (req, res) => {
  const { bizType, enabled } = req.query;
  const q = knex('print_template').select('*').orderBy('updated_at', 'desc');
  if (bizType) q.where({ biz_type: bizType });
  if (enabled !== undefined) q.where({ enabled: Number(enabled) });
  res.json(await q);
});

app.get('/templates/:id', async (req, res) => {
  res.json(await knex('print_template').where({ id: req.params.id }).first());
});

app.post('/templates', async (req, res) => {
  const body = req.body || {};
  const insert = {
    biz_type: body.bizType,
    name: body.name,
    template_key: body.templateKey || null,
    paper_size: body.paperSize || 'A4',
    orientation: body.orientation || 'portrait',
    schema_json: JSON.stringify(body.schema || {}),
    html: cleanTemplate(body.html || ''),
    css: sanitizeCss(body.css || ''),
    enabled: body.enabled === false ? 0 : 1,
    current_version: 1,
    created_at: new Date(),
    updated_at: new Date(),
  };
  const id = await insertAndGetId('print_template', insert);
  await knex('print_template_version').insert({
    template_id: id, version: 1, schema_json: insert.schema_json, html: insert.html, css: insert.css, published: 0, created_at: new Date(),
  });
  invalidateTemplateCache(id, insert.template_key);
  res.json({ id });
});

app.put('/templates/:id', async (req, res) => {
  const old = await knex('print_template').where({ id: req.params.id }).first();
  if (!old) return res.status(404).json({ message: '模板不存在' });
  const nextVersion = Number(old.current_version || 1) + 1;
  const patch = {
    name: req.body.name || old.name,
    schema_json: JSON.stringify(req.body.schema || JSON.parse(old.schema_json || '{}')),
    html: req.body.html == null ? old.html : cleanTemplate(req.body.html),
    css: req.body.css == null ? old.css : sanitizeCss(req.body.css),
    current_version: nextVersion,
    updated_at: new Date(),
  };
  await knex('print_template').where({ id: req.params.id }).update(patch);
  await knex('print_template_version').insert({ template_id: Number(req.params.id), version: nextVersion, schema_json: patch.schema_json, html: patch.html, css: patch.css, published: 0, created_at: new Date() });
  invalidateTemplateCache(req.params.id, old.template_key);
  res.json({ id: Number(req.params.id), version: nextVersion });
});

app.post('/templates/:id/publish', async (req, res) => {
  const version = Number(req.body.version || 0);
  if (!version) return res.status(400).json({ message: 'version required' });
  await knex('print_template_version').where({ template_id: req.params.id }).update({ published: 0 });
  await knex('print_template_version').where({ template_id: req.params.id, version }).update({ published: 1 });
  const template = await knex('print_template').where({ id: req.params.id }).first();
  invalidateTemplateCache(req.params.id, template?.template_key);
  res.json({ ok: true });
});

app.get('/templates/:id/versions', async (req, res) => {
  const versions = await knex('print_template_version')
    .select('version', 'published', 'created_at')
    .where({ template_id: req.params.id })
    .orderBy('version', 'desc');
  res.json(versions);
});

app.get('/templates/:id/versions/:version', async (req, res) => {
  const row = await knex('print_template_version')
    .select('version', 'published', 'schema_json', 'html', 'css', 'created_at')
    .where({ template_id: req.params.id, version: Number(req.params.version) })
    .first();
  if (!row) return res.status(404).json({ message: '版本不存在' });
  res.json({ ...row, schema: JSON.parse(row.schema_json || '{}') });
});

app.post('/templates/:id/clone', async (req, res) => {
  const source = await knex('print_template').where({ id: req.params.id }).first();
  if (!source) return res.status(404).json({ message: '模板不存在' });
  const insert = {
    biz_type: req.body.bizType || source.biz_type,
    name: req.body.name || `${source.name}-副本`,
    template_key: req.body.templateKey || null,
    paper_size: source.paper_size,
    orientation: source.orientation,
    schema_json: source.schema_json,
    html: source.html,
    css: source.css,
    enabled: source.enabled,
    current_version: 1,
    created_at: new Date(),
    updated_at: new Date(),
  };
  const id = await insertAndGetId('print_template', insert);
  await knex('print_template_version').insert({
    template_id: id,
    version: 1,
    schema_json: source.schema_json,
    html: source.html,
    css: source.css,
    published: 0,
    created_at: new Date(),
  });
  res.json({ id });
});

app.post('/templates/:id/validate', async (req, res) => {
  const html = String(req.body.html || '');
  const css = String(req.body.css || '');
  if (html.length < 3) return res.status(400).json({ message: 'html 为空' });
  try {
    sanitizeCss(css);
  } catch (e) {
    return res.status(400).json({ message: e.message });
  }
  res.json({ ok: true, sanitizedHtml: cleanTemplate(html) });
});

app.post('/render/html', async (req, res) => {
  try {
    const { templateId, templateKey, version, data } = req.body;
    const t = await getTemplateWithVersion({ templateId, templateKey, version });
    const computedHtml = buildDoc(t, data || {});
    res.json({ html: t.html, css: t.css, computedHtml, templateId: t.id, version: t.version || t.current_version });
  } catch (e) {
    res.status(400).json({ message: e.message });
  }
});

app.post('/render/pdf', async (req, res) => {
  try {
    const { bizType, bizId, templateId, templateKey, version, data } = req.body;
    const t = await getTemplateWithVersion({ templateId, templateKey, version });
    const computedHtml = buildDoc(t, data || {});

    const pdf = await enqueuePdf(() => renderPdfBuffer(computedHtml, t));

    const form = new FormData();
    form.append('file', pdf, { filename: `print-${Date.now()}.pdf`, contentType: 'application/pdf' });
    const uploadResp = await axios.post(`${CORE_API}/system/file/upload?folder=business&page=print`, form, {
      headers: { ...form.getHeaders(), Authorization: req.authToken },
      maxBodyLength: Infinity,
      timeout: RENDER_TIMEOUT_MS,
    });
    const fileUrl = uploadResp.data?.data?.url || uploadResp.data?.url || '';

    const jobId = await insertAndGetId('print_job', {
      biz_type: bizType || '',
      biz_id: bizId || '',
      template_id: t.id,
      template_version: version || t.version || t.current_version,
      mode: 'pdf',
      status: 'success',
      file_url: fileUrl,
      html_snapshot: computedHtml,
      meta_json: JSON.stringify({ templateKey: templateKey || t.template_key || null }),
      created_at: new Date(),
    });

    res.json({ fileUrl, jobId });
  } catch (e) {
    res.status(500).json({ message: e.message });
  }
});

app.post('/jobs', async (req, res) => {
  if (req.body.mode === 'pdf') return app._router.handle({ ...req, url: '/render/pdf', method: 'POST' }, res);
  const rendered = await (await axios.post(`http://127.0.0.1:${PORT}/render/html`, req.body, { headers: { Authorization: req.authToken }, timeout: RENDER_TIMEOUT_MS })).data;
  const jobId = await insertAndGetId('print_job', { biz_type: req.body.bizType || '', biz_id: req.body.bizId || '', template_id: rendered.templateId || req.body.templateId, template_version: rendered.version || req.body.version || 1, mode: 'html', status: 'success', html_snapshot: rendered.computedHtml, meta_json: JSON.stringify({}), created_at: new Date() });
  res.json({ jobId });
});

app.get('/jobs', async (req, res) => {
  const page = Math.max(1, Number(req.query.page || 1));
  const pageSize = Math.min(100, Math.max(1, Number(req.query.pageSize || 20)));
  const q = knex('print_job').select('*').orderBy('created_at', 'desc');
  const countQ = knex('print_job').count({ total: '*' }).first();

  if (req.query.bizType) {
    q.where({ biz_type: req.query.bizType });
    countQ.where({ biz_type: req.query.bizType });
  }
  if (req.query.bizId) {
    q.where({ biz_id: req.query.bizId });
    countQ.where({ biz_id: req.query.bizId });
  }
  if (req.query.templateId) {
    q.where({ template_id: req.query.templateId });
    countQ.where({ template_id: req.query.templateId });
  }
  if (req.query.createdAtFrom) {
    q.where('created_at', '>=', req.query.createdAtFrom);
    countQ.where('created_at', '>=', req.query.createdAtFrom);
  }
  if (req.query.createdAtTo) {
    q.where('created_at', '<=', req.query.createdAtTo);
    countQ.where('created_at', '<=', req.query.createdAtTo);
  }

  const [items, totalRaw] = await Promise.all([q.offset((page - 1) * pageSize).limit(pageSize), countQ]);
  const total = Number(totalRaw?.total || Object.values(totalRaw || {})[0] || 0);
  res.json({ items, page, pageSize, total });
});

app.get('/jobs/:id', async (req, res) => {
  res.json(await knex('print_job').where({ id: req.params.id }).first());
});

const server = app.listen(PORT, () => console.log(`print backend running on ${PORT}`));

async function shutdown() {
  if (shuttingDown) return;
  shuttingDown = true;
  server.close(() => {});
  while (pdfQueue.length > 0) {
    const task = pdfQueue.shift();
    task.reject(new Error('服务正在停止'));
  }
  await forceRecycleBrowser();
  await knex.destroy();
  process.exit(0);
}

process.on('SIGTERM', shutdown);
process.on('SIGINT', shutdown);
