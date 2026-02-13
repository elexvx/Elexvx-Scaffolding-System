import { createApp, ref, onMounted } from 'vue';
import axios from 'axios';

const TOKEN_STORAGE_KEY = 'tdesign.auth.token';
const TOKEN_KEY_STORAGE_KEY = 'tdesign.auth.token.key';
const fromBase64 = (value) => { const binary = atob(value); const bytes = new Uint8Array(binary.length); for (let i=0;i<binary.length;i++) bytes[i]=binary.charCodeAt(i); return bytes; };
async function loadToken(){
  const payload = localStorage.getItem(TOKEN_STORAGE_KEY); if(!payload) return '';
  const keyRaw = localStorage.getItem(TOKEN_KEY_STORAGE_KEY)||sessionStorage.getItem(TOKEN_KEY_STORAGE_KEY); if(!keyRaw) return '';
  try { const subtle = window.crypto?.subtle; if(!subtle) return ''; const key=await subtle.importKey('raw',fromBase64(keyRaw),{name:'AES-GCM'},false,['decrypt']); const bytes=fromBase64(payload); const iv=bytes.slice(0,12); const data=bytes.slice(12); const plain=await subtle.decrypt({name:'AES-GCM',iv},key,data); return new TextDecoder().decode(plain);} catch { return ''; }
}

createApp({
  setup() {
    const templates = ref([]); const versions = ref([]); const bizType = ref('invoice'); const name = ref(''); const html = ref('<h1>{{data.title}}</h1>'); const css = ref('body{font-family:Arial;}'); const templateId=ref(); const previewHtml=ref(''); const selectedVersion=ref();
    let client;
    const init = async () => {
      const token = await loadToken();
      client = axios.create({ baseURL: '/api/module-api/print', headers: { Authorization: `Bearer ${token}` } });
      await loadTemplates();
    };
    const loadTemplates = async ()=>{ const {data} = await client.get('/templates',{params:{bizType:bizType.value}}); templates.value=data; };
    const loadVersions = async()=>{ if(!templateId.value) return; const {data}=await client.get(`/templates/${templateId.value}/versions`); versions.value=data; selectedVersion.value = data.find(v=>v.published===1)?.version || data[0]?.version; };
    const save = async()=>{ if(templateId.value){ await client.put(`/templates/${templateId.value}`,{name:name.value,html:html.value,css:css.value,schema:{}});}else{ const {data}=await client.post('/templates',{bizType:bizType.value,name:name.value||'新模板',html:html.value,css:css.value,schema:{}}); templateId.value=data.id;} await loadTemplates(); await loadVersions(); alert('已保存'); };
    const edit = async (t)=>{ templateId.value=t.id; name.value=t.name; html.value=t.html; css.value=t.css; await loadVersions(); };
    const preview = async()=>{ const {data}=await client.post('/render/html',{templateId:templateId.value,version:selectedVersion.value,data:{title:'预览标题'}}); previewHtml.value=data.computedHtml; };
    const publish = async()=>{ if(!templateId.value||!selectedVersion.value) return; await client.post(`/templates/${templateId.value}/publish`,{version:selectedVersion.value}); await loadVersions(); alert('已发布/回滚'); };
    onMounted(init);
    return { templates,versions,bizType,name,html,css,templateId,previewHtml,selectedVersion,save,edit,preview,publish,loadTemplates,loadVersions };
  },
  template: `<div style='padding:16px;font-family:Arial'><h2>打印中心</h2><div>业务类型 <input v-model='bizType'/><button @click='loadTemplates'>筛选</button></div><div style='display:flex;gap:16px;margin-top:12px'><div style='width:35%'><h3>模板列表</h3><ul><li v-for='t in templates' :key='t.id'><a href='#' @click.prevent='edit(t)'>{{t.name}}(v{{t.current_version}})</a></li></ul><h3>版本列表</h3><ul><li v-for='v in versions' :key='v.version'><label><input type='radio' :value='v.version' v-model='selectedVersion'/> v{{v.version}} {{v.published?"(已发布)":""}}</label></li></ul></div><div style='width:65%'><h3>设计器</h3><input v-model='name' placeholder='模板名称' style='width:100%;margin-bottom:8px'/><textarea v-model='html' rows='8' style='width:100%'></textarea><textarea v-model='css' rows='6' style='width:100%;margin-top:8px'></textarea><div><button @click='save'>保存</button><button @click='publish' :disabled='!templateId||!selectedVersion'>发布/回滚到所选版本</button><button @click='preview' :disabled='!templateId'>预览</button></div><iframe v-if='previewHtml' :srcdoc='previewHtml' style='width:100%;height:300px;margin-top:8px'></iframe></div></div></div>`
}).mount('#app');
