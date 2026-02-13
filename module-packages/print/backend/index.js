const express = require('express');
const Knex = require('knex');
const Handlebars = require('handlebars');
const sanitizeHtml = require('sanitize-html');
const puppeteer = require('puppeteer');
const axios = require('axios');
const FormData = require('form-data');

const app = express();
app.use(express.json({ limit: '10mb' }));

const PORT = Number(process.env.PORT || 3901);
const DB_TYPE = String(process.env.TDESIGN_DB_TYPE || 'mysql').toLowerCase();
const DB_URL = process.env.TDESIGN_DB_URL || '';
const DB_USER = process.env.TDESIGN_DB_USER || '';
const DB_PASSWORD = process.env.TDESIGN_DB_PASSWORD || '';
const CORE_API = process.env.TDESIGN_CORE_API_BASE || 'http://127.0.0.1:8080/api';

if (!['mysql', 'postgresql', 'postgres'].includes(DB_TYPE)) {
  throw new Error('当前 DB 类型未支持 print 模块后端');
}

const knex = Knex({
  client: DB_TYPE === 'postgres' ? 'pg' : (DB_TYPE === 'postgresql' ? 'pg' : 'mysql2'),
  connection: DB_URL || {
    host: process.env.TDESIGN_DB_HOST || '127.0.0.1',
    user: DB_USER,
    password: DB_PASSWORD,
    database: process.env.TDESIGN_DB_NAME || '',
  },
  pool: { min: 0, max: 6 },
});

let browserPromise;
const pdfQueue = [];
let running = 0;
const MAX_CONCURRENT = 2;

function requireAuth(req, res, next) {
  const auth = req.headers.authorization || '';
  if (!auth.startsWith('Bearer ')) return res.status(401).json({ message: 'Unauthorized' });
  req.authToken = auth;
  next();
}

app.use(requireAuth);

const cleanTemplate = (html = '') => sanitizeHtml(html, {
  allowedTags: sanitizeHtml.defaults.allowedTags.concat(['img', 'style', 'table', 'thead', 'tbody', 'tr', 'td', 'th']),
  allowedAttributes: { '*': ['style', 'class'], img: ['src', 'alt', 'width', 'height'] },
  allowedSchemes: ['data'],
  disallowedTagsMode: 'discard',
});

async function getTemplateWithVersion(templateId, version) {
  const t = await knex('print_template').where({ id: templateId }).first();
  if (!t) throw new Error('模板不存在');
  if (!version) return t;
  const v = await knex('print_template_version').where({ template_id: templateId, version }).first();
  if (!v) throw new Error('版本不存在');
  return { ...t, ...v };
}

function buildDoc(html, css, data) {
  const safeHtml = cleanTemplate(html);
  const tpl = Handlebars.compile(safeHtml, { noEscape: false, strict: false });
  const body = tpl({ data: data || {} });
  return `<!doctype html><html><head><meta charset="utf-8"/><style>${css || ''}</style></head><body>${body}</body></html>`;
}

async function getBrowser() {
  if (!browserPromise) {
    browserPromise = puppeteer.launch({ headless: 'new', args: ['--no-sandbox', '--disable-setuid-sandbox'] });
  }
  return browserPromise;
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
  try { task.resolve(await task.fn()); } catch (e) { task.reject(e); } finally { running -= 1; drain(); }
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
    html: body.html || '',
    css: body.css || '',
    enabled: body.enabled === false ? 0 : 1,
    current_version: 1,
    created_at: new Date(),
    updated_at: new Date(),
  };
  const [id] = await knex('print_template').insert(insert);
  await knex('print_template_version').insert({
    template_id: Number(id), version: 1, schema_json: insert.schema_json, html: insert.html, css: insert.css, published: 0, created_at: new Date(),
  });
  res.json({ id: Number(id) });
});

app.put('/templates/:id', async (req, res) => {
  const old = await knex('print_template').where({ id: req.params.id }).first();
  if (!old) return res.status(404).json({ message: '模板不存在' });
  const nextVersion = Number(old.current_version || 1) + 1;
  const patch = {
    name: req.body.name || old.name,
    schema_json: JSON.stringify(req.body.schema || {}),
    html: req.body.html || old.html,
    css: req.body.css || old.css,
    current_version: nextVersion,
    updated_at: new Date(),
  };
  await knex('print_template').where({ id: req.params.id }).update(patch);
  await knex('print_template_version').insert({ template_id: Number(req.params.id), version: nextVersion, schema_json: patch.schema_json, html: patch.html, css: patch.css, published: 0, created_at: new Date() });
  res.json({ id: Number(req.params.id), version: nextVersion });
});

app.post('/templates/:id/publish', async (req, res) => {
  const version = Number(req.body.version || 0);
  if (!version) return res.status(400).json({ message: 'version required' });
  await knex('print_template_version').where({ template_id: req.params.id }).update({ published: 0 });
  await knex('print_template_version').where({ template_id: req.params.id, version }).update({ published: 1 });
  res.json({ ok: true });
});

app.post('/templates/:id/validate', async (req, res) => {
  const html = String(req.body.html || '');
  const css = String(req.body.css || '');
  if (html.length < 3) return res.status(400).json({ message: 'html 为空' });
  if (css.includes('<script')) return res.status(400).json({ message: 'css 非法' });
  res.json({ ok: true, sanitizedHtml: cleanTemplate(html) });
});

app.post('/render/html', async (req, res) => {
  try {
    const { templateId, version, data } = req.body;
    const t = await getTemplateWithVersion(templateId, version);
    const computedHtml = buildDoc(t.html, t.css, data || {});
    res.json({ html: t.html, css: t.css, computedHtml });
  } catch (e) { res.status(400).json({ message: e.message }); }
});

app.post('/render/pdf', async (req, res) => {
  try {
    const { bizType, bizId, templateId, version, data } = req.body;
    const t = await getTemplateWithVersion(templateId, version);
    const computedHtml = buildDoc(t.html, t.css, data || {});
    const pdf = await enqueuePdf(async () => {
      const browser = await getBrowser();
      const page = await browser.newPage();
      await page.setRequestInterception(true);
      page.on('request', (r) => r.abort());
      await page.setContent(computedHtml, { waitUntil: 'domcontentloaded' });
      const output = await page.pdf({ format: t.paper_size || 'A4', landscape: String(t.orientation) === 'landscape', printBackground: true });
      await page.close();
      return output;
    });

    const form = new FormData();
    form.append('file', pdf, { filename: `print-${Date.now()}.pdf`, contentType: 'application/pdf' });
    const uploadResp = await axios.post(`${CORE_API}/system/file/upload?folder=business&page=print`, form, {
      headers: { ...form.getHeaders(), Authorization: req.authToken },
      maxBodyLength: Infinity,
    });
    const fileUrl = uploadResp.data?.data?.url || uploadResp.data?.url || '';

    const [jobId] = await knex('print_job').insert({
      biz_type: bizType || '', biz_id: bizId || '', template_id: templateId, template_version: version || t.current_version,
      mode: 'pdf', status: 'success', file_url: fileUrl, html_snapshot: computedHtml, meta_json: JSON.stringify({}), created_at: new Date(),
    });

    res.json({ fileUrl, jobId: Number(jobId) });
  } catch (e) {
    res.status(500).json({ message: e.message });
  }
});

app.post('/jobs', async (req, res) => {
  if (req.body.mode === 'pdf') return app._router.handle({ ...req, url: '/render/pdf', method: 'POST' }, res);
  const rendered = await (await axios.post(`http://127.0.0.1:${PORT}/render/html`, req.body, { headers: { Authorization: req.authToken } })).data;
  const [jobId] = await knex('print_job').insert({ biz_type: req.body.bizType || '', biz_id: req.body.bizId || '', template_id: req.body.templateId, template_version: req.body.version || 1, mode: 'html', status: 'success', html_snapshot: rendered.computedHtml, meta_json: JSON.stringify({}), created_at: new Date() });
  res.json({ jobId: Number(jobId) });
});

app.get('/jobs', async (req, res) => {
  const q = knex('print_job').select('*').orderBy('created_at', 'desc');
  if (req.query.bizType) q.where({ biz_type: req.query.bizType });
  if (req.query.bizId) q.where({ biz_id: req.query.bizId });
  res.json(await q);
});

app.get('/jobs/:id', async (req, res) => {
  res.json(await knex('print_job').where({ id: req.params.id }).first());
});

app.listen(PORT, () => console.log(`print backend running on ${PORT}`));
