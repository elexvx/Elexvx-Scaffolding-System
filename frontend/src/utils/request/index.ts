// axios闂傚倷鐒﹀妯肩矓閸洘鍋? 闂備礁鎲￠悷顖炲垂閻㈢鍚规繝濠傛噺閸嬫﹢鎮橀悙鎻掆挃濠㈢懓鐭傞弻鐔烘嫚閳ヨ櫕鐝濋梺闈涙缁夊綊骞嗛崟顖ｆ晩閻犳亽鍔庨鎺楁煟閻愬鈽夐柡鍫墮鐓ら柛褎顨呯紒鈺呮煟瑜嶉幗婊呯矆婢舵劖鐓曟繛鍡樏奸柇顖涚箾閹惧磭鍩ｇ€殿喖顕埀顒佺⊕钃遍柡鍡╁弮閹綊宕堕妷銉ュ闂佸搫顑呴崯顖滅矉閹烘梹宕夐柛婵嗗閵堫噣姊洪崨濠勬噽闁搞劎鏅Σ鎰攽鐎ｎ亞顦ч梺鍝勭墢閺佹悂鏌ч崒鐐寸厸闁稿本纰嶉惌妤佺箾閸℃劕鐏茬€规洩缍侀、娑樷枎鎼达綁鐎哄┑鐐村灦閹稿摜绮旂€涙ɑ鍙?
import type { AxiosInstance } from 'axios';
import axios from 'axios';
import isString from 'lodash/isString';
import merge from 'lodash/merge';
import { MessagePlugin } from 'tdesign-vue-next';

import { ContentTypeEnum } from '@/constants';
import router from '@/router';
import { useUserStore } from '@/store';
import { resolveApiHost } from '@/utils/apiHost';
import { clearTokenStorage } from '@/utils/secureToken';

import { VAxios } from './Axios';
import type { AxiosTransform, CreateAxiosOptions } from './AxiosTransform';
import { formatRequestDate, joinTimestamp, setObjToUrlParams } from './utils';

let hasNotifiedUnauthorized = false;
let isUnauthorizedRedirecting = false;

// 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞閹邦剙寮烽梺姹囧劤閹槈k婵犵妲呴崹顏堝焵椤掆偓绾绢厾娑甸埀?闂?婵犵數鍋涙径鍥焵椤掑啯鐝柛搴㈢懇閺岋綁锝為鈧俊鎸庣箾閸欏顕滄繛鐓庮煼閹瑩寮堕幐搴㈡闂?闂佽绻愮换鎰亹婢跺瞼绠斿鑸靛姈閻撯偓閻庡箍鍎卞ú銊╁几娣囩惤st 濠电偞娼欓崥瀣┍濞差亝鎲橀悗锝庡枛鐎氬銇勯幒鍡椾壕濠电姭鍋撶痪鐗堢睄ck闂備胶鎳撻崵鏍⒔閸曨垰鏄?闂?Vite 濠电偛顕刊瀵稿緤閸ф绠?
const host = resolveApiHost();
const UNAUTHORIZED_SENTINEL = '\u767b\u5f55\u72b6\u6001\u5df2\u5931\u6548 [401]';
const UNAUTHORIZED_NOTICE_KEY = 'tdesign.auth.invalid.notice';
const UNAUTHORIZED_NOTICE_TEXT = '\u5f53\u524d\u767b\u5f55\u72b6\u6001\u5931\u6548\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55';

const buildHardLoginHref = (redirect?: string) => {
  const query: Record<string, string> = { _t: String(Date.now()) };
  if (redirect) query.redirect = redirect;
  return router.resolve({ path: '/login', query }).href;
};

const saveUnauthorizedNotice = (message: string) => {
  try {
    sessionStorage.setItem(UNAUTHORIZED_NOTICE_KEY, message);
  } catch {}
};

const isSilent403Path = (url?: string) => {
  if (!url) return false;
  const pure = String(url).split('?')[0];
  return pure.endsWith('/system/ui');
};

// 闂備浇妗ㄩ懗鑸垫櫠濡も偓閻ｅ灚鎷呴悷鎵獮闂佸憡娲﹂崢浠嬪磹閻愮儤鐓ユ繛鎴炆戝﹢浼存煛瀹€鈧崰鎰箔閻旂厧鍨傛い鏂裤偢濞堫剟姊洪崨濠傜瑨婵☆偅顨婅棟閻犳亽鍔庨惌鎾垛偓骞垮劚閹峰危闁秵鐓熼柣鎰级椤ョ偤鏌″畝鈧崰搴敋?
const transform: AxiosTransform = {
  // 濠电姰鍨煎▔娑氣偓姘煎櫍楠炲啯绻濋崟顒€鐝伴梻浣哥仢椤戝洤锕㈤幘顔界厸濞达絽鎼。鑲┾偓瑙勬尫缁舵碍淇婇幘顔肩＜婵ê鍚嬮幖鎰版⒑閸濆嫷鍎愮紒顔肩Ч瀵娊鎮㈤悡搴ｎ唹濡炪倖妫佸畷鐢告偡濠靛鐓涢柛鏇㈡涧閻忥附銇勯敐鍌涙珚鐎殿喚鏁婚幃銏ゅ传閵夈儺妫旈柣搴㈩問閸犳牗顨ヨ箛鏇燁潟婵犻潧顑呴惌妤併亜閺嶃劎鐭嬬紒缁㈠灦閺岀喖骞侀幒鎴濆婵犳鍠栫换妯虹暦濞嗗緷娲敂閸涱喗鐝﹂梺?
  transformRequestHook: (res, options) => {
    const { isTransformResponse, isReturnNativeResponse } = options;

    // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃?04闂備礁鎼崯鐗堟叏閹绢喖鑸归悗鐢电《閸嬫捇鎮烽悧鍫ｇ濠电偞褰冨锟犵嵁閹烘唯闁靛ě鍜佸敼闂?
    const method = res.config.method?.toLowerCase();
    if (res.status === 204 && ['put', 'patch', 'delete'].includes(method)) {
      return res;
    }

    // 闂備礁鎼€氱兘宕规导鏉戠畾濞达綀顫夋禍銈夋煛閸屾稑顕滅紒鈧埀顒勬⒑閸涘鐒介柛鐘虫崌瀵偊骞樼拠韫炊閻庡箍鍎遍幊鎰偓鐟邦槸椤?婵犳鍣徊楣冨蓟閵娾斂鈧懓顦归柡浣哥Т椤繈鎳滈崗鍝ュ笡闂佽崵鍠愬ú鎴澝归崶顒傚祦闊洦绋戦惌妤呮煛瀹擃喖瀚崕銉╂煙閻撳海鎽犻柡灞诲姂婵＄敻寮崼婵堢潉闂佸搫鐗撳褔鍩㈣箛娑欑厽妞ゎ偒鍓欐慨鈧銈嗘礃濮樸劍绂掗敃鍌涘€风紒顔炬嚀娴?
    if (isReturnNativeResponse) {
      return res;
    }
    // 濠电偞鍨堕幐鍝ョ矓妞嬪孩宕叉俊顖氬悑閸嬫﹢鎮橀悙鎻掓倯鐎规洘褰冮湁闁挎繂妫欑粈鈧銈忚吂閺呯娀骞冮崼鏇炲耿妞ゆ挾濮烽ˇ顕€姊洪悜鈺傛珔闁宦板妿閺侇喚鈧稒蓱娴溿倝鏌￠崒娑橆嚋缂佲偓閳?
    // 闂備焦妞垮鍧楀礉鐎ｎ剝濮虫い鎺嶉檷娴滄粓鏌ｉ姀鐘冲暈婵℃ぜ鍔岄湁闁绘瑥鎳愰幃濂告煟閺嶃劎鐭掔€规洩缍侀、娑樷攽閸℃绠ｉ梻鍌氬€稿ú鐘诲磻閹剧粯鍋￠柡鍥ㄤ亢閸忓本绻涢幓鎺撳仴妤犵偞甯℃俊鐑藉Ω閵壯屽數闂備礁鎲￠悷锕€鈹冮崸顧猠闂備焦瀵х粙鎴︽偩閻槡a闂備焦瀵х粙鎴︻敊閸楁仩sage闂佸搫顦弲婊堟偡閵堝洩濮抽柣鎴烆焽閳瑰秹鏌嶉埡浣告殨缂佽鲸鐗犻弻锟犲礃椤撶喓銆愮紓浣介哺閸ㄥ灝鐣?
    if (!isTransformResponse) {
      return res.data;
    }

    // 闂傚倷鐒︾€笛囨偡閵娾晩鏁嬮柕鍫濐槹閸庡秹鏌涢弴銊ュ妞は佸洦鐓曞┑鐘插暟閹冲棛绱掔紒妯虹伌鐎?
    const { data } = res;
    if (!data) {
      throw new Error('Request interface error');
    }

    //  闂佸搫顦弲婊堟偡閳哄懎闂?code濠?闂備礁鎲￠懝鐐殽濮濆被浜归悗闈涙憸绾惧ジ鏌ｉ弬鍨暢妞ゅ繘浜堕弻锝夊Ω閵夈儺浠奸梺鐑╁閸愶絾鐏佺紒鐐緲椤﹁京绮堟径鎰拻闁搞儻绲芥禍楣冩煟閻斿憡纾荤紒鈧担闈╄€?types.ts闂備礁鎲￠崝鏇㈠箠瀹ュ绠為柕濞炬櫅缂佲晠姊洪锝囶灱缂佲偓鐎ｎ亶娓婚柕鍫濇噺鐠愨剝绻涢煫顓炲祮闁硅櫕鐩、鏃堝礋椤撶姷鏆㈤梻浣圭湽閸斿瞼鈧凹鍘鹃弫顕€骞橀鑲╃厬闂佹寧绻傞悧蹇曠不閺屻儲鐓曢柕澹啩娌梺缁橆殘椤牓顢?
    const { code, message } = data;

    // 闂佸搫顦弲婊堟偡閳哄懎闂柣鎴ｅГ閻掑鏌￠崟顐ょ閻㈩垰妫濋弻娑樷枎濡櫣浠ф繛瀛樼矋鐢€愁嚕閻ｅ本鍠嗛柛鏇ㄥ亞閵堬附淇婇悙顏勨偓鏇烇耿閸楃伝娲偋閸喍姘﹀┑鐐村灦缁娀寮ㄩ挊澹″酣宕剁捄鐑樼亶闂?
    const hasSuccess = data && code === 0;
    if (hasSuccess) {
      return data.data;
    }

    // 濠电偠鎻紞鈧繛澶嬫礋瀵偊濡舵径濠呅曢柟鑹版彧缂嶅棙瀵奸崒鐐茬骇闁冲搫鍊归悡銉︾箾閸欏澧甸柟顖氬暣瀹曠喖顢涘☉娆愮彟闂佽崵濮村ù鍌炲垂椤栨稓鈹嶅┑鐘叉搐缁犳帗銇勯弽銉モ偓妤冪矆婢跺ň鏀介柍銉ュ暱閳ь剙缍婇幃妯诲緞婵炵偓鐓㈤梺鏂ユ櫅閸燁垳绮婚幒妤佺厱闁圭儤鎸鹃幊浣糕攽閻愬弶鍠橀柟顔荤矙婵℃悂鍩￠崘銊ь偧闂佽崵濮抽梽宥夊磹閹惧鈹嶅┑鐘叉搐缁?
    const errorMsg = message || `闂佽崵濮村ú顓㈠绩闁秵鍎戦柣妤€鐗嗙欢鐐哄级閸偄浜悮婵嬫⒑閹稿海鈽夐柣顓炲€垮顐ｇ節閸曨剙鐝板銈嗙墬缁酣宕? ${code}`;
    throw new Error(`${errorMsg} [${code}]`);
  },

  // 闂備胶顢婇～澶娒哄┑瀣祦?transformRequestHook 闂備胶顢婇崺鏍矙閹达箑鍚规い鎾卞灪閸庡秹鏌涢弴銊ョ仭闁哄應鏅犻幃褰掑炊鐠鸿櫣浼堢紓浣介哺缁诲牓骞婇弴鐑嗘僵妞ゆ帊绀侀埀顑惧€濋弻娑㈠箳閹炬剚妫嗙紓渚囧枤閸庛倗绮欐径鎰劦妞ゆ帒鍊荤壕濂告煙閹屽殶濞存粠鍨伴—?
  requestCatchHook: async (e) => {
    const msg = String(e?.message || '');
    const axiosError = axios.isAxiosError(e) ? e : null;
    const responseStatus = axiosError?.response?.status as number | undefined;
    const responseMessage = String(axiosError?.response?.data?.message || '').trim();
    const m = msg.match(/\[(\d{3})\]/) || msg.match(/闂傚倷鐒︾€笛囨偡閵娾晩鏁嬮柕鍫濐槹閸?\s*(\d{3})/);
    const code = m ? Number(m[1]) : responseStatus;

    // 闂備礁鎲￠悷顖涚濠靛棴鑰垮〒姘ｅ亾鐎规洜鍏樻俊姝岊槻妞ゃ儲顨婇幃妤冩喆閸曨剛娈ゅ┑鐐茬墛閸ㄥ墎绮欐径鎰ㄩ柍杞扮劍椤忕喖姊绘担鐟邦嚋闁荤喆鍔戦、姘跺Χ婢跺﹦鐫勯梺鍏兼倐濞佳嚶烽崨瀛樺仯闁告繂瀚弸鍐╃箾閺夋垶顥為柕鍥у閹粙宕ㄦ繝鍐︹偓鍐⒑閹稿海鈽夐柣顒傚帶閳诲秹濡烽妸锝勬睏闂佸搫娲ㄩ崑鎴炵婵傚憡鍋ｉ柟鎵虫櫅閸斻倝鏌ｉ埄鍐ㄧ仸缂佸顦甸崺鈧い鎺戝閸ゅ銇勯幒鎴濐仾闁哄應鏅犻幃褰掑炊鐠鸿櫣浠х紓浣诡殔椤﹂亶骞戦崟顓涘亾閸︻厼校缂?
    if (code === 401) {
      const user = useUserStore();
      const humanMsg = UNAUTHORIZED_NOTICE_TEXT;
      const currentRoute = router.currentRoute.value;
      const onLoginPage = currentRoute.path === '/login';

      user.token = '';
      user.refreshToken = '';
      user.tokenExpiresAt = null;
      user.userInfo = { name: '', avatar: '', roles: [] };
      user.userInfoLoaded = false;
      clearTokenStorage();

      if (!onLoginPage && !isUnauthorizedRedirecting) {
        isUnauthorizedRedirecting = true;
        if (!hasNotifiedUnauthorized) {
          hasNotifiedUnauthorized = true;
        }
        saveUnauthorizedNotice(humanMsg || UNAUTHORIZED_NOTICE_TEXT);
        const hardHref = buildHardLoginHref(currentRoute.fullPath);
        window.location.replace(hardHref);
        return new Promise(() => {});
      }

      return Promise.reject(new Error(UNAUTHORIZED_SENTINEL));
    } else if (code === 403) {
      const humanMsg = responseMessage || msg.replace(/\s*\[\d{3}\]\s*$/, '').trim();
      const url = String(axiosError?.config?.url || '');
      if (!isSilent403Path(url)) {
        MessagePlugin.error(humanMsg || '\u6743\u9650\u4e0d\u8db3\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458\u5f00\u901a');
      }
    } else if (code === 422) {
      const humanMsg = responseMessage || msg.replace(/\s*\[\d{3}\]\s*$/, '').trim();
      if (humanMsg) {
        MessagePlugin.warning(humanMsg);
      }
    } else if (code != null && code >= 500) {
      const humanMsg = responseMessage || msg.replace(/\s*\[\d{3}\]\s*$/, '').trim();
      MessagePlugin.error(humanMsg || '\u670d\u52a1\u5668\u9519\u8bef\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5');
    }
    // 濠电偞鍨堕幐鍝ョ矓閹绢喖鐤柍褜鍓熼弻銈嗙附婢跺鐩庢繝娈垮枓閺呮繈骞戦崟顓涘亾閸︻厼校缂佷緡鍣ｉ弻娑㈠箳瀹ュ懎绁梺杞扮閿曪箓骞忛悩娲绘晬闁绘劦浜栭崑鎾村緞閹邦収妫冨銈庡亽閸犳氨绮堟径鎰仯濞达絽寮跺▍鍐磼濡ゅ啫鏋涚€规洘绻堥崹楣冨箵閹烘挻娈ㄩ梻浣规た閸犳洖霉閸ヮ剙鍚规繝濠傚枤閸熷懘鏌涘Δ鍐ㄥ壉婵℃煡浜堕弻锝夋倷閸欏妫﹂梺杞扮閿曪箓骞忛悩娲绘晬婵炴垶顭囬崐鎺旂磽娴ｆ彃浜?
    return Promise.reject(e);
  },

  // 闂佽崵濮村ú顓㈠绩闁秵鍎戝ù鍏兼綑缁€鍫⑩偓骞垮劚閹峰危闁秵鐓熼柣鎰级椤ャ垽鏌涘Ο绋库偓妤冩?
  beforeRequestHook: (config, options) => {
    const { apiUrl, isJoinPrefix, urlPrefix, joinParamsToUrl, formatDate, joinTime = true } = options;

    // 婵犵數鍎戠紞鈧い鏇嗗嫭鍙忛柣鎰惈缁犳娊鏌曟径鍫濆姎缂傚秵鎹囬弻娑㈠箻瀹曞泦銈囩磼?
    if (isJoinPrefix && urlPrefix && isString(urlPrefix)) {
      config.url = `${urlPrefix}${config.url}`;
    }

    // 闂佽绻愮换鎰板箺绾扮磩eUrl闂備胶鎳撻崲鏌ュ触鐎ｎ剚鏆?
    if (apiUrl && isString(apiUrl)) {
      config.url = `${apiUrl}${config.url}`;
    }
    const params = config.params || {};
    const data = config.data || false;

    if (formatDate && data && !isString(data)) {
      formatRequestDate(data);
    }
    if (config.method?.toUpperCase() === 'GET') {
      if (!isString(params)) {
        // 缂?get 闂佽崵濮村ú顓㈠绩闁秵鍎戝ù鍏兼綑缁€澶愭煟濡じ鍚柣鐔稿姍閺岋繝宕橀妸褍鐓熷┑鈽嗗亜濞硷繝鐛€ｎ喖绀冮柟缁樺笚椤ユ繈姊烘潪鎵槮閹煎瓨绮庡Σ鎰攽鐎ｎ偆鍔堕梺鍓茬厛閸犳牜鏁崘瑁佸綊鎮╅崣澶嬪闯缂備焦鍞绘俊鍥焵椤掍胶鈯曢惇澶愭煠閸偄鐏存鐐茬箻楠炴劖鎯旈姀鈽嗗晙闂備胶顢婇鏍窗閹捐鐒?        config.params = Object.assign(params || {}, joinTimestamp(joinTime, false));
      } else {
        // 闂備胶顭堢换鎺楀储瑜旈、娆撳箒閻掓唶tful濠碉紕鍋涢鍛偓娑掓櫊閹?
        config.url = `${config.url + params}${joinTimestamp(joinTime, true)}`;
        config.params = undefined;
      }
    } else if (!isString(params)) {
      if (formatDate) {
        formatRequestDate(params);
      }
      if (
        Reflect.has(config, 'data') &&
        config.data &&
        (Object.keys(config.data).length > 0 || data instanceof FormData)
      ) {
        config.data = data;
        config.params = params;
      } else {
        // 闂傚倸鍊搁悧濠囧礄閻ゅ兜闂佽崵濮村ú顓㈠绩闁秵鍎戦柣妤€鐗呯换鍡涙煕鐏炲墽銆掑ù婊庡灡缁绘稓浠︾粙鍨拤濠电姭鍋撳〒姘ｅ亾妤犵偞鎹囬獮鍥敂閸℃氨绠瞕ata闂備焦瀵х粙鎴︽嚐椤栫偛鍨傛い鏍ㄧ矆閻掑﹪鏌涢悙鏉戭瀱rams闂佽崵鍠愰悷銉ノ涢弮鍌涘闁绘帒璇漷a
        config.data = params;
        config.params = undefined;
      }
      if (joinParamsToUrl) {
        config.url = setObjToUrlParams(config.url as string, { ...config.params, ...config.data });
      }
    } else {
      // 闂備胶顭堢换鎺楀储瑜旈、娆撳箒閻掓唶tful濠碉紕鍋涢鍛偓娑掓櫊閹?
      config.url += params;
      config.params = undefined;
    }
    return config;
  },

  // 闂佽崵濮村ú顓㈠绩闁秵鍎戝ù鍏兼綑缁狀垶鏌ㄩ弮鍥棄闁绘帒顭烽弻娑㈡晲閸愩劌顬堝銈忚吂閺呯娀骞?
  requestInterceptors: (config, options) => {
    // 闂佽崵濮村ú顓㈠绩闁秵鍎戦柣妤€鐗忛埢鏇㈡倵閿濆簼绨绘い銈呮噹椤法鎹勯崫鍕典紑闂佽鍠栭妶绁噉fig
    const userStore = useUserStore();
    const { token } = userStore;

    if (token) hasNotifiedUnauthorized = false;
    if (token && (config as Recordable)?.requestOptions?.withToken !== false) {
      (config as Recordable).headers.Authorization = options.authenticationScheme
        ? `${options.authenticationScheme} ${token}`
        : token;
    }
    const data = (config as Recordable)?.data;
    if (typeof FormData !== 'undefined' && data instanceof FormData) {
      (config as Recordable).headers = (config as Recordable).headers || {};
      delete (config as Recordable).headers['Content-Type'];
      delete (config as Recordable).headers['content-type'];
    }
    const currentPath = router.currentRoute.value?.path;
    if (currentPath) {
      (config as Recordable).headers = (config as Recordable).headers || {};
      (config as Recordable).headers['X-Page-Path'] = currentPath;
    }
    return config;
  },

  // 闂備礁鎲＄换鍌滅矓鐎垫瓕濮抽柡灞诲劚缁狀垶鏌ㄩ弮鍥棄闁绘帒顭烽弻娑㈡晲閸愩劌顬堝銈忚吂閺呯娀骞?
  responseInterceptors: (res) => {
    return res;
  },

  // 闂備礁鎲＄换鍌滅矓鐎垫瓕濮抽柡灞诲劜閻撱儲绻涢崱妯轰刊闁搞倖鐗曢…璺ㄦ崉閸濆嫷浼€闂?
  responseInterceptorsCatch: (error: any, instance: AxiosInstance) => {
    const { config } = error;
    const status = error?.response?.status as number | undefined;

    if (status === 401) {
      // 401 闂傚倷鐒︾€笛囨偡閵娾晩鏁嬮柕鍫濐槹閸?requestCatchHook 缂傚倸鍊烽懗鍫曞窗瀹ュ洨鍗氶悗娑櫭欢鐐烘煕閺囥劌骞楅柛?
      return Promise.reject(error);
    }

    if (status === 403) {
      const serverMsg = error?.response?.data?.message;
      const url = String(error?.config?.url || '');
      if (!isSilent403Path(url)) {
        MessagePlugin.error(serverMsg || '\u6743\u9650\u4e0d\u8db3\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458\u5f00\u901a');
      }
    }

    // 闂佽娴烽弫濠氬焵椤掍胶銆掗柤褰掔畺閹鐛崹顔句痪闂佺硶鏅滅粙鎾跺垝閳哄拋鏁嶉柣鎰摠濞呮捇鏌?4xx)闂備焦瀵х粙鎴炵附閺冨倻绠斿璺侯儐娴溿倖绻涢幋鐐电煠闁衡偓娴犲鈷戦柟缁樺笧鍟搁梺浼欏瘜閸ㄥ爼寮鍛殕闁逞屽墮鐓ら柛褎顨呯粻鎶芥煏婢跺牆濡跨紒鐘崇叀閺屾盯濡疯娴犻亶鏌℃担闈╁姛闁?
    if (status && status >= 400 && status < 500) {
      return Promise.reject(error);
    }

    // 闂備礁鎲￠悷顖涚濠婂牜鏁嬫い鏂垮⒔绾鹃箖鏌熺€电啸闁硅娲熷娲箰鎼达絾鍣銈嗘磸閸婃繂鐣烽鍡曟勃闁绘劦鍓涢悿鈧梻浣告啞閺岋繝鍩€椤掑啯鐝柣婵勫€濆娲箰鎼达絾鍣?5xx)闂佸搫顦弲婊呯矙閺嶎厹鈧線骞嬮敂鐣屽帓閻庡箍鍎遍悧蹇撐?
    if (!config || !config.requestOptions.retry) return Promise.reject(error);

    config.retryCount = config.retryCount || 0;

    if (config.retryCount >= config.requestOptions.retry.count) {
      // 闂傚倷鐒﹁ぐ鍐矓閻戣姤鍎婃い鏍ㄥ焹閺嬪酣鏌嶉埡浣告殲婵炲牆鐖奸弻锝夛綖椤掆偓婵″ジ鏌℃担鍦⒌鐎规洏鍎查幆鏃堫敍濠婂拋妲峰┑鐐村灦閹稿摜绮旈悜钘夊惞婵犲﹤鐗嗙粈澶愭煏婵炲灝濡块柛搴㈢叀瀵爼鎮滈崱妤€鈷嬮梺闈涙椤﹂潧螞閸愵噯缍栨い鏂垮⒔椤︻噣鏌ｉ悩杈唹闁哄懏鐟х划顓㈡偄閻撳海顦梺鐐藉劜瑜板啴寮埡鍛厽妞ゆ棁宕甸ˇ锔姐亜閿旇娅婇柟?
      return Promise.reject(error);
    }

    config.retryCount += 1;

    const backoff = new Promise((resolve) => {
      setTimeout(() => {
        resolve(config);
      }, config.requestOptions.retry.delay || 1);
    });
    config.headers = { ...config.headers, 'Content-Type': ContentTypeEnum.Json };
    return backoff.then((config) => instance.request(config));
  },
};

function createAxios(opt?: Partial<CreateAxiosOptions>) {
  return new VAxios(
    merge(
      <CreateAxiosOptions>{
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication#authentication_schemes
        // 濠电偞鎸婚懝楣冾敄閸涙番鈧? authenticationScheme: 'Bearer'
        authenticationScheme: '',
        // 闂佺儵鍓濈敮鎺楀箠韫囨搩娓?
        timeout: 10 * 1000,
        // 闂備礁婀遍崢褔骞栭锔藉仺闁绘稓顨宱kie
        withCredentials: true,
        // 濠电姰鍨规晶搴ｆ崲濠靛洨鈹嶅┑鐘叉搐缁?        headers: { 'Content-Type': ContentTypeEnum.Json },
        // 闂備浇妗ㄩ懗鑸垫櫠濡も偓閻ｅ灚鎷呴悷鎵獮闂佸憡娲﹂崢浠嬪磹閻愮儤鐓涢柛灞剧矋閸も偓缂?
        transform,
        // 闂傚倷鐒﹀妯肩矓閸洘鍋柛鈩兦滄禍婊堟煟瑜嶉幗婊呯矆婢跺ň妲堥柟鐐墯閸庢梹绻涢梻鏉戝祮闁诡垰鍟村畷鐔碱敍濡も偓娴滄儳顭块懜闈涘闁逞屽墮缁夊綊寮婚崼銉﹀癄濠㈣泛锕ㄩ澶嬬箾鐎电袨闁稿骸宕敃銏ゎ敂閸喎浜煎銈嗗笒閸婃槒銇愬畝鍕厽闁靛鍎遍顏堟偨椤栨稒灏︾€规洩缍侀弫鎰板醇椤愩垺顔夋繝娈垮枤閹虫捇宕愰悜鑺ュ殘闁瑰濮甸崯娲煕閳╁啰鎳勭痪?
        requestOptions: {
          // 闂備浇顫夋禍浠嬪磿鏉堫偁浜规繛鎴欏灩閹瑰爼鏌℃径瀣嚋缂?
          apiUrl: host,
          // 闂備礁鎼€氱兘宕规导鏉戠畾濞撴埃鍋撻柟铏洴椤㈡棃宕熼宥嗩殕缁绘盯鏁愰崼顐㈡婵犳鍠氶崑銈夌嵁閹烘唯闁靛鍎崇粙澶愭⒑閸涘﹥灏紒澶婂濡?
          isJoinPrefix: true,
          // 闂備浇顫夋禍浠嬪磿鏉堫偁浜规繛鎴欏灩缁€鍫⑩偓骞垮劚濞层劎澹?
          // 濠电偞鎸婚懝楣冾敄閸涙番鈧? https://www.baidu.com/api
          // urlPrefix: '/api'
          urlPrefix: import.meta.env.VITE_API_URL_PREFIX,
          // 闂備礁鎼€氱兘宕规导鏉戠畾濞达綀顫夋禍銈夋煛閸屾稑顕滅紒鈧埀顒勬⒑閸涘鐒介柛鐘虫崌瀵偊骞樼拠韫炊閻庡箍鍎遍幊鎰偓鐟邦槸椤?婵犳鍣徊楣冨蓟閵娾斂鈧懓顦归柡浣哥Т椤繈鎳滈崗鍝ュ笡闂佽崵鍠愬ú鎴澝归崶顒傚祦闊洦绋戦惌妤呮煛瀹擃喖瀚崕銉╂煙閻撳海鎽犻柡灞诲姂婵＄敻寮崼婵堢潉闂佸搫鐗撳褔鍩㈣箛娑欑厽妞ゎ偒鍓欐慨鈧銈嗘礃濮樸劍绂掗敃鍌涘€风紒顔炬嚀娴?          isReturnNativeResponse: false,
          // 闂傚倸鍊稿ú鐘诲磻閹剧粯鍋￠柡鍥ㄦ皑椤︼附銇勯弴銊ユ灍婵炵厧顭峰顒勫箰鎼达綆妲梻浣芥〃閼宠埖鏅跺Δ鈧悾鍨媴鐟欏嫪姘﹀┑鐐村灦缁娀寮ㄩ挊澶樼唵閻犲搫鎼顐︽煙?
          isTransformResponse: true,
          // post闂佽崵濮村ú顓㈠绩闁秵鍎戝ù鐓庣摠閸庡秹鏌涢弴銊ュ妞は佸洦鐓曞┑鐘插暟閹冲嫰鏌涜閿曨亜鐣峰┑瀣亹闁圭粯甯楅ˉ婵嬫⒑鏉炴壆鍔嶆繝銏★耿瀹曡櫣鈧鍋搑l
          joinParamsToUrl: false,
          // 闂備礁鎼粔鍫曞储瑜忓Σ鎰版晸閻樿尙顦遍梺鍝勭Р閸斿瞼寰婇悷鎳婄懓鈹冮崹顔瑰亾閺囩儐鍤曟い鎺戝閺嬩線鏌ｅΔ鈧悧濠囁囪濮?          formatDate: true,
          // 闂備礁鎼€氱兘宕规导鏉戠畾濞撴埃鍋撶€规洘绻堥幃鈺呭箵閹烘垶杈堥梻浣告惈閸燁偊鎮ラ悡骞盯宕稿Δ鈧粻?          joinTime: true,
          // 闂備礁鎼€氱兘宕规导鏉戠畾濞达絿鏅悿鈧梺浼欑到閺堫剙鈻撻敐澶嬪仯闁搞儺鍓氶弫閬嶆煟椤忓啫宓嗙€规洩缍佸浠嬪Ω椤喓鍎遍湁闁绘﹩鍠栭埀顒侇殕閺?
          // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞閹邦剝袝濡炪倖鐗楃划宥夊汲韫囨稒鐓ユ繛鎴烆焾鐎氫即鏌涢…鎴濈仸闁哄苯鑻濂稿椽娴ｅ墣鈺呮煟閻樺弶鍠橀柡鈧柆宥嗗剳濞村吋娼欑猾宥夋煛閸垺鏆╅柣鐔哥箞瀵爼鍩￠崒婊庣伇濡炪們鍨洪崹璺侯嚗閸曨垰绀嬫い鎾跺枎閳?
          // 濠电姷顣介埀顒€鍟块埀顒€缍婇幃妯诲緞瀹€鈧惌澶娒归敐鍥у妺闁哄棗绻橀弻銊モ槈濡灝顏梺鍓茬厛閸ㄥ爼寮荤仦绛嬪悑闁告粈鐒︿簺闂佽崵濮村ú顓㈠绩闁秵鍎戝ù鍏兼綑缁秹鏌￠崼銏℃毄缂佹劖顨婇弻娑樷枎閹邦喖顫х紓浣割槺閺屽濡甸幇鏉跨闁告劕妯婇弸鈧梺鑽ゅТ濞差參寮ㄩ柆宥嗗剳?
          ignoreCancelToken: true,
          // 闂備礁鎼€氱兘宕规导鏉戠畾濞撴埃鍋撴鐐寸墵瀹曨偊宕熼鐔告token
          withToken: true,
          // 闂傚倷鐒﹁ぐ鍐矓閻戣姤鍎?
          retry: {
            count: 3,
            delay: 1000,
          },
        },
      },
      opt || {},
    ),
  );
}
export const request = createAxios();
