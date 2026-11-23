/* ===== Part B: shell + Home / Chats / Board / Machines / Me + Task / Machine detail =====
   Wrapped in an IIFE so names never collide with part A. App is NOT available at top level (TDZ): only use it in event handlers. */
(() => {
'use strict';
const FM = ROLE === 'foreman';
const t = __tr;
const $ = (s, r = document) => r.querySelector(s);
const $$ = (s, r = document) => [...r.querySelectorAll(s)];
const esc = s => String(s == null ? '' : s).replace(/[&<>"']/g, c => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c]));
const param = o => `data-param='${JSON.stringify(o)}'`;
const scr = id => $(`.screen[data-screen="${id}"]`);
const isOn = id => { const e = scr(id); return e && e.classList.contains('active') ? e : null; };

/* ---------- icons (Material, 24px) ---------- */
const ICON = {
  bell: 'M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z',
  search: 'M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z',
  add: 'M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z',
  back: 'M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z',
  lock: 'M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z',
  check: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z',
  tick: 'M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z',
  next: 'M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z',
  clock: 'M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z',
  chat: 'M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z',
  mic: 'M12 14c1.66 0 2.99-1.34 2.99-3L15 5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3zm5.3-3c0 3-2.54 5.1-5.3 5.1S6.7 14 6.7 11H5c0 3.41 2.72 6.23 6 6.72V21h2v-3.28c3.28-.48 6-3.3 6-6.72h-1.7z',
  build: 'M22.7 19l-9.1-9.1c.9-2.3.4-5-1.5-6.9-2-2-5-2.4-7.4-1.3L9 6 6 9 1.6 4.7C.4 7.1.9 10.1 2.9 12.1c1.9 1.9 4.6 2.4 6.9 1.5l9.1 9.1c.4.4 1 .4 1.4 0l2.3-2.3c.5-.4.5-1.1.1-1.4z',
  groups: 'M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z',
  swap: 'M6.99 11L3 15l3.99 4v-3H14v-2H6.99v-3zM21 9l-3.99-4v3H10v2h7.01v3L21 9z',
  verified: 'M23 12l-2.44-2.79.34-3.69-3.61-.82-1.89-3.2L12 2.96 8.6 1.5 6.71 4.69 3.1 5.5l.34 3.7L1 12l2.44 2.79-.34 3.7 3.61.82L8.6 22.5l3.4-1.47 3.4 1.46 1.89-3.19 3.61-.82-.34-3.69L23 12zm-12.91 4.72l-3.8-3.81 1.48-1.48 2.32 2.33 5.85-5.87 1.48 1.48-7.33 7.35z',
  globe: 'M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zm6.93 6h-2.95c-.32-1.25-.78-2.45-1.38-3.56 1.84.63 3.37 1.91 4.33 3.56zM12 4.04c.83 1.2 1.48 2.53 1.91 3.96h-3.82c.43-1.43 1.08-2.76 1.91-3.96zM4.26 14C4.1 13.36 4 12.69 4 12s.1-1.36.26-2h3.38c-.08.66-.14 1.32-.14 2 0 .68.06 1.34.14 2H4.26zm.82 2h2.95c.32 1.25.78 2.45 1.38 3.56-1.84-.63-3.37-1.9-4.33-3.56zm2.95-8H5.08c.96-1.66 2.49-2.93 4.33-3.56C8.81 5.55 8.35 6.75 8.03 8zM12 19.96c-.83-1.2-1.48-2.53-1.91-3.96h3.82c-.43 1.43-1.08 2.76-1.91 3.96zM14.34 14H9.66c-.09-.66-.16-1.32-.16-2 0-.68.07-1.35.16-2h4.68c.09.65.16 1.32.16 2 0 .68-.07 1.34-.16 2zm.25 5.56c.6-1.11 1.06-2.31 1.38-3.56h2.95c-.96 1.65-2.49 2.93-4.33 3.56zM16.36 14c.08-.66.14-1.32.14-2 0-.68-.06-1.34-.14-2h3.38c.16.64.26 1.31.26 2s-.1 1.36-.26 2h-3.38z',
  notif: 'M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z',
  translate: 'M12.87 15.07l-2.54-2.51.03-.03c1.74-1.94 2.98-4.17 3.71-6.53H17V4h-7V2H8v2H1v1.99h11.17C11.5 7.92 10.44 9.75 9 11.35 8.07 10.32 7.3 9.19 6.69 8h-2c.73 1.63 1.73 3.17 2.98 4.56l-5.09 5.02L4 19l5-5 3.11 3.11.76-2.04zM18.5 10h-2L12 22h2l1.12-3h4.75L21 22h2l-4.5-12zm-2.62 7l1.62-4.33L19.12 17h-3.24z',
  info: 'M11 7h2v2h-2zm0 4h2v6h-2zm1-9C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z',
  link: 'M3.9 12c0-1.71 1.39-3.1 3.1-3.1h4V7H7c-2.76 0-5 2.24-5 5s2.24 5 5 5h4v-1.9H7c-1.71 0-3.1-1.39-3.1-3.1zM8 13h8v-2H8v2zm9-6h-4v1.9h4c1.71 0 3.1 1.39 3.1 3.1s-1.39 3.1-3.1 3.1h-4V17h4c2.76 0 5-2.24 5-5s-2.24-5-5-5z',
  send: 'M2.01 21L23 12 2.01 3 2 10l15 2-15 2z',
  qr: 'M9.5 6.5v3h-3v-3h3M11 5H5v6h6V5zm-1.5 9.5v3h-3v-3h3M11 13H5v6h6v-6zm6.5-6.5v3h-3v-3h3M19 5h-6v6h6V5zm-6 8h1.5v1.5H13V13zm1.5 1.5H16V16h-1.5v-1.5zM16 13h1.5v1.5H16V13zm-3 3h1.5v1.5H13V16zm1.5 1.5H16V19h-1.5v-1.5zM16 16h1.5v1.5H16V16zm1.5-1.5H19V16h-1.5v-1.5zm0 3H19V19h-1.5v-1.5zM22 7h-2V4h-3V2h5v5zm0 15v-5h-2v3h-3v2h5zM2 22h5v-2H4v-3H2v5zM2 2v5h2V4h3V2H2z',
  warn: 'M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z',
  camp: 'M18 11v2h4v-2h-4zm-2 6.61c.96.71 2.21 1.65 3.2 2.39.4-.53.8-1.07 1.2-1.6-.99-.74-2.24-1.68-3.2-2.4-.4.54-.8 1.08-1.2 1.61zM20.4 5.6c-.4-.53-.8-1.07-1.2-1.6-.99.74-2.24 1.68-3.2 2.4.4.53.8 1.07 1.2 1.6.96-.72 2.21-1.65 3.2-2.4zM4 9c-1.1 0-2 .9-2 2v2c0 1.1.9 2 2 2h1v4h2v-4h1l5 3V6L8 9H4zm11.5 3c0-1.33-.58-2.53-1.5-3.35v6.69c.92-.81 1.5-2.01 1.5-3.34z',
  chart: 'M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z',
  grid: 'M3 3v8h8V3H3zm6 6H5V5h4v4zm-6 4v8h8v-8H3zm6 6H5v-4h4v4zm4-16v8h8V3h-8zm6 6h-4V5h4v4zm-6 4v8h8v-8h-8zm6 6h-4v-4h4v4z',
  heart: 'M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z',
  doc: 'M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z',
  help: 'M11 18h2v-2h-2v2zm1-16C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z',
  book: 'M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 4h5v8l-2.5-1.5L6 12V4z',
  replay: 'M12 5V1L7 6l5 5V7c3.31 0 6 2.69 6 6s-2.69 6-6 6-6-2.69-6-6H4c0 4.42 3.58 8 8 8s8-3.58 8-8-3.58-8-8-8z',
};
const ic = (n, cls = 'hm-ci') => `<svg class="${cls}" viewBox="0 0 24 24" aria-hidden="true"><path d="${ICON[n]}"/></svg>`;

/* ---------- data helpers ---------- */
const USERS = Object.keys(DATA.users);
const workers = () => USERS.filter(u => DATA.users[u].role === 'worker');
const thr = id => DATA.threads.find(x => x.id === id);
const allMsgs = () => Object.values(DATA.messages).flat();
const findMsg = id => allMsgs().find(m => m.id === id);
const mc = id => DATA.machines.find(m => m.id === id);
const mcName = m => FM ? m.ja : m.name;
const ttl = k => FM ? k.ja : k.title;
const short = id => String(nameOf(id)).split(' ')[0];
const DUE_JA = {Today: '今日', Tomorrow: '明日', Mon: '月曜', Tue: '火曜', Wed: '水曜', Thu: '木曜', Fri: '金曜', Sat: '土曜', Sun: '日曜'};
const dueT = s => !s ? '' : FM ? String(s).replace(/Today|Tomorrow|Mon|Tue|Wed|Thu|Fri|Sat|Sun/g, w => DUE_JA[w]) : s;
const ST_JA = {green: 'わかった', orange: '要確認', red: '未理解', wait: '未回答'};
const stT = s => FM ? ST_JA[s] : (s === 'wait' ? 'Waiting' : STATUS[s]);
const stChip = s => `<span class="hm-st" data-s="${s}">${stT(s)}</span>`;
const msChip = r => r ? `<span class="hm-st" data-s="green">${t('使用可', 'Ready')}</span>`
  : `<span class="hm-st hm-lk" data-s="wait">${ic('lock')}${t('ロック中', 'Locked')}</span>`;
const av = (id, cls = '') => { const u = DATA.users[id]; return `<span class="hm-av ${cls}" style="background:${u.color}">${esc(u.initials)}</span>`; };

// check status overlay: filled by 'check:answer' so it works whichever side mutates DATA
const ov = {};
const stOf = (m, u) => (ov[m.id] && ov[m.id][u]) || (m.wk && m.wk.results && m.wk.results[u] && m.wk.results[u].status) || 'wait';
const rcpt = m => ((thr(m.thread) || {}).members || []).filter(u => u !== m.from && DATA.users[u] && DATA.users[u].role === 'worker');
const wkMsgs = () => allMsgs().filter(m => m.wk);
const worst = (m) => { const s = rcpt(m).map(u => stOf(m, u)); return s.includes('red') ? 'red' : s.includes('orange') ? 'orange' : s.length && s.every(x => x === 'green') ? 'green' : 'wait'; };
function tally() {
  const c = {green: 0, orange: 0, red: 0, wait: 0};
  wkMsgs().forEach(m => rcpt(m).forEach(u => c[stOf(m, u)]++));
  return c;
}
function attention() {
  const out = [];
  wkMsgs().forEach(m => rcpt(m).forEach(u => { const s = stOf(m, u); if (s === 'red' || s === 'orange') out.push({m, u, s}); }));
  return out.sort((a, b) => (a.s === 'red' ? 0 : 1) - (b.s === 'red' ? 0 : 1));
}
const pending = () => wkMsgs().filter(m => m.from !== ME && rcpt(m).includes(ME) && stOf(m, ME) === 'wait' && (m.wk.stage == null || m.wk.stage >= 4));
const openTasks = () => DATA.tasks.filter(k => k.col !== 'done');
const lockedWorkers = m => workers().filter(u => !m.ready[u]);

/* ---------- shells ---------- */
function shell(el, title, left, right) {
  el.innerHTML = `<header class="appbar">${left || ''}<h1>${title}</h1>${right || ''}</header><div class="content"></div>`;
  return $('.content', el);
}
const backBtn = `<button class="icon-btn" data-back aria-label="${t('戻る', 'Back')}">${ic('back', 'ic')}</button>`;
// bell opens the notification centre (part E); badge count comes from window.uxUnread, kept fresh by E on every notify
const bell = () => { const n = window.uxUnread ? window.uxUnread() : 0; return `<button class="icon-btn ux-bell" data-go="notifications" aria-label="${t('通知', 'Notifications')}">${ic('bell', 'ic')}<span class="ux-bdg"${n ? '' : ' hidden'}>${n > 9 ? '9+' : n}</span></button>`; };
// guarded navigation: push if the screen exists in this build, else toast (parts C and D may not be merged yet)
const go = (id, p) => { if (scr(id)) App.push(id, p || {}); else App.toast(t('この画面はまだ準備中です', 'Coming soon')); };
const stag = anim => anim ? ' stagger' : '';

/* ---------- Home ---------- */
let homeLoaded = false;
const homeSkel = () => `<div class="pad"><div class="skeleton" style="height:32px;width:60%;margin:16px 0 8px"></div><div class="skeleton" style="height:16px;width:35%"></div><div class="skeleton" style="height:150px;margin-top:24px;border-radius:12px"></div><div class="skeleton" style="height:72px;margin-top:16px;border-radius:12px"></div><div class="skeleton" style="height:72px;margin-top:8px;border-radius:12px"></div></div>`;
const dateStr = () => new Date().toLocaleDateString(FM ? 'ja-JP' : 'en-US', {month: 'long', day: 'numeric', weekday: 'long'});
const hello = () => `<div class="hm-hello"><h2>${FM ? 'おはようございます、' + esc(short(ME)) + 'さん' : 'Good morning, ' + esc(short(ME))}</h2><div class="hm-date">${esc(dateStr())}</div></div>`;


/* quick actions (entry points to parts C and D screens). Schemas of DATA.nearmisses / DATA.briefings belong to part C: read defensively. */
const nmUnacked = () => window.sfOpenNearmisses ? window.sfOpenNearmisses() : (DATA.nearmisses || []).filter(n => n.status !== 'ack').length;
const answeredBy = (b, u) => { const r = b.results && b.results[u]; return !!r && r.status !== 'wait'; };
const briefingToday = () => { const l = window.sfLatestBriefing ? window.sfLatestBriefing() : (DATA.briefings || []).slice().sort((x, y) => x.at < y.at ? 1 : -1)[0]; return l && !answeredBy(l, ME) ? l : null; };
const qa = (id, icon, label, badge, p) => `<button class="hm-qa" data-go="${id}"${p ? ' ' + param(p) : ''}><span class="hm-qi">${ic(icon, 'ic')}${badge ? `<i class="hm-qb">${badge}</i>` : ''}</span><span class="hm-ql">${label}</span></button>`;

const fuOpen = () => (DATA.followups || []).filter(f => f.status === 'open');
function fuCard() {
  const o = fuOpen(), f = o[o.length - 1];
  return `<button class="card hm-fu" data-go="followups" data-z="${o.length ? 0 : 1}"><span class="hm-qi">${ic(o.length ? 'help' : 'check', 'ic')}</span>
    <div class="grow"><b>要フォロー ${o.length}件</b><span>${f ? esc(short(f.user)) + 'さん · 「' + esc(f.term) + '」が理解できていません' : 'みんな理解できています'}</span></div>${ic('next', 'ic')}</button>`;
}
function homeForeman(anim) {
  const c = tally(), total = c.green + c.orange + c.red + c.wait, att = attention(), locked = DATA.machines.filter(m => lockedWorkers(m).length);
  const bar = ['green', 'orange', 'red', 'wait'].filter(s => c[s]).map(s => `<i data-s="${s}" style="flex:${c[s]}"></i>`).join('');
  const attRows = att.slice(0, 4).map(({m, u, s}) => `<div class="list-item" data-tap data-push="chat" ${param({thread: m.thread, msg: m.id})}>
      ${av(u)}<div class="grow"><div class="hm-ell">${esc(nameOf(u))}</div><div class="sub hm-ell">${esc(textOf(m))}</div></div>${stChip(s)}</div>`).join('');
  return `<div class="pad${stag(anim)}">
    ${hello()}
    ${fuCard()}
    <div><div class="hm-h">今日の理解度</div>
      <div class="card hm-sum"><div class="hm-counts">
        <div class="hm-count" data-s="green"><b>${c.green}</b><span>${ST_JA.green}</span></div>
        <div class="hm-count" data-s="orange"><b>${c.orange}</b><span>${ST_JA.orange}</span></div>
        <div class="hm-count" data-s="red"><b>${c.red}</b><span>${ST_JA.red}</span></div></div>
        <div class="hm-bar">${bar}</div>
        <div class="hm-bar-cap">${total ? `回答待ち ${c.wait}件 · 全${total}件` : 'まだ指示はありません'}</div></div></div>
    <div><div class="hm-h">クイック操作</div><div class="hm-qgrid">
      ${qa('briefing', 'camp', '朝礼')}${qa('nearmiss-list', 'warn', 'ヒヤリハット', nmUnacked() || '')}${qa('analytics', 'chart', '理解度分析')}
      ${qa('skills', 'grid', '力量表')}${qa('life', 'groups', '地域・生活')}${qa('supportlog', 'doc', '支援記録')}</div></div>
    <div><div class="hm-h">確認が必要な回答</div>
      <div class="card">${attRows || `<div class="hm-empty">${ic('check')}<span>確認が必要な人はいません</span></div>`}</div></div>
    <div class="hm-tiles">
      <button class="card hm-tile" data-tab="board"><b>${openTasks().length}</b><span>未完了タスク</span></button>
      <button class="card hm-tile" data-tab="machines"><b>${locked.length}</b><span>ロック中の機械</span></button></div>
    ${locked.length ? `<div><div class="hm-h">サインオフ待ち</div><div class="card">${locked.map(m => `<div class="list-item" data-tap data-push="machine" ${param({id: m.id})}>
      <div class="grow"><div>${esc(m.ja)}</div><div class="sub">${lockedWorkers(m).map(u => esc(short(u))).join('、')}</div></div>${ic('lock')}</div>`).join('')}</div></div>` : ''}
  </div>`;
}
function homeWorker(anim) {
  const pend = pending(), mine = openTasks().filter(k => k.assignees.includes(ME)), br = briefingToday();
  const brT = br && (br.focusEn || br.en || 'Today’s briefing');
  return `<div class="pad${stag(anim)}" style="padding-bottom:88px">
    ${hello()}
    ${br ? `<button class="card hm-brief" data-go="briefing-card" ${param({id: br.id})}><span class="hm-qi">${ic('camp', 'ic')}</span><div class="grow"><div class="from">Today’s briefing</div><div class="q hm-clamp">${esc(brT)}</div></div><span class="btn tonal">Open</span></button>` : ''}
    <div><div class="hm-h">Checks waiting for you</div>${pend.map(m => `<button class="card hm-chk" data-push="chat" ${param({thread: m.thread, msg: m.id})}>
        <div class="from">From ${esc(nameOf(m.from))} · ${esc(m.at)}</div><div class="q hm-clamp">${esc(textOf(m))}</div>
        <div class="foot">${stChip('wait')}<span class="btn tonal">Answer now</span></div></button>`).join('')
      || `<div class="card"><div class="hm-empty">${ic('check')}<span>You're all caught up</span></div></div>`}</div>
    <div><div class="hm-h">Quick actions</div><div class="hm-qgrid">
      ${qa('nearmiss', 'warn', 'Report near-miss')}${qa('life', 'groups', 'Life &amp; notices', (window.lfPending && window.lfPending(ME)) || '')}${qa('ask', 'help', 'Ask')}${qa('course', 'book', 'Safety course')}</div></div>
    <button class="card hm-learn" data-go="learning" ${param({user: ME})} style="margin-top:12px"><span class="hm-qi">${ic('book', 'ic')}</span><div class="grow"><div>My learning</div><small>Words you learned and gaps to review</small></div>${ic('next', 'ic')}</button>
    <div><div class="hm-h">My tasks</div><div class="card">${mine.map(k => `<div class="list-item" data-tap data-push="task" ${param({id: k.id})}>
      <div class="grow"><div class="hm-ell">${esc(ttl(k))}</div><div class="sub">${esc(dueT(k.due))}</div></div>${ic('next')}</div>`).join('')
      || `<div class="hm-empty">${ic('check')}<span>No open tasks</span></div>`}</div></div>
    <div><div class="hm-h">My machines</div><div class="card">${DATA.machines.map(m => `<div class="list-item" data-tap ${m.ready[ME] ? `data-push="machine" ${param({id: m.id})}` : `data-go="note" ${param({machine: m.id})}`}>
      <div class="grow">${esc(mcName(m))}</div>${msChip(m.ready[ME])}</div>`).join('')}</div></div>
  </div>`;
}
function renderHome(el, anim) {
  const c = $('.content', el); if (!c) return;
  c.innerHTML = FM ? homeForeman(anim) : homeWorker(anim);
}

/* ---------- Chats ---------- */
const bump = {};
const lastMsg = th => { const a = DATA.messages[th.id] || []; return a[a.length - 1]; };
function threadAvatar(th) {
  if (th.kind === 'talk') return `<span class="cl-av grp">${ic('mic', 'ic')}</span>`;
  if (th.members.length > 2) return `<span class="cl-av grp">${ic('groups', 'ic')}</span>`;
  const o = th.members.find(u => u !== ME) || th.members[0], u = DATA.users[o];
  return `<span class="cl-av" style="background:${u.color}">${esc(u.initials)}</span>`;
}
function renderChats(el, anim) {
  const c = $('.content', el); if (!c) return;
  const list = DATA.threads.map((th, i) => ({th, i})).sort((a, b) => (bump[b.th.id] || 0) - (bump[a.th.id] || 0) || a.i - b.i).map(x => x.th).filter(th => !th.kind || th.members.includes(ME)); // personal Threads (ADR 0014) show only to their members
  c.innerHTML = `<ul class="list${stag(anim)}" style="padding-top:4px">${list.map(th => {
    const m = lastMsg(th), fw = (DATA.messages[th.id] || []).filter(x => x.wk && DATA.users[x.from].role === 'foreman').pop();
    const pre = m ? th.kind ? (th.kind === 'talk' ? m.ja : textOf(m)) : ((th.members.length > 2 ? short(m.from) + ': ' : m.from === ME ? t('あなた: ', 'You: ') : '') + textOf(m)) : t('メッセージはまだありません', 'No messages yet');
    const fwS = fw ? worst(fw) : null;
    return `<li><button class="cl-item${th.unread ? ' unread' : ''}" data-tap data-push="${th.kind ? 'pchat' : 'chat'}" ${param({thread: th.id})}>
      ${threadAvatar(th)}
      <div class="cl-mid"><div class="cl-title hm-ell">${esc(FM ? th.title : th.titleEn)}</div>
        <div class="cl-prev">${m && m.kind === 'voice' ? ic('mic', 'cl-mi') : ''}<span class="hm-ell">${esc(pre)}</span></div></div>
      <div class="cl-right"><span class="cl-time">${m ? esc(m.at) : ''}</span>
        <span class="cl-r2">${fwS ? `<span class="hm-dot" data-s="${fwS}" title="${stT(fwS)}"></span>` : ''}${th.unread ? `<span class="cl-badge">${th.unread}</span>` : ''}</span></div>
    </button></li>`;
  }).join('')}</ul>`;
}

/* ---------- Board ---------- */
const COLS = [['todo', '未着手', 'To do'], ['doing', '作業中', 'Doing'], ['done', '完了', 'Done']];
const seen = new Set(DATA.tasks.map(k => k.id));
const colOf = {}; DATA.tasks.forEach(k => colOf[k.id] = k.col);
const fresh = new Map(); let freshN = 0, freshTimer = 0;
let dragging = false, pendBoard = false;
const srcThread = k => { const m = k.from && findMsg(k.from); return m ? m.thread : null; };
function card(k) {
  const m = k.machine && mc(k.machine), st = srcThread(k), d = fresh.get(k.id);
  return `<div class="bd-card${d != null ? ' bd-new' : ''}" data-id="${esc(k.id)}" data-col="${k.col}"${d != null ? ` style="--d:${d}ms"` : ''}>
    <div class="bd-title">${esc(ttl(k))}</div>
    <div class="bd-meta">${k.due ? `<span>${ic('clock')}${esc(dueT(k.due))}</span>` : ''}${m ? `<span>${ic('build')}${esc(mcName(m))}</span>` : ''}</div>
    <div class="bd-foot"><div class="bd-avs">${k.assignees.map(u => av(u, 'sm')).join('')}</div>
      ${st ? `<span class="bd-src" data-push="chat" ${param({thread: st, msg: k.from})}>${ic('chat')}${t('チャットから', 'From chat')}</span>` : ''}</div></div>`;
}
function renderBoard(el, anim) {
  if (dragging) { pendBoard = true; return; }
  let strip = $('.bd-strip', el);
  if (!strip) {
    el.innerHTML = `<header class="appbar"><h1>${t('ボード', 'Board')}</h1><button class="icon-btn" data-toast="${t('フィルターはプロトタイプでは未対応', 'Filters are not in the prototype')}" aria-label="${t('絞り込み', 'Filter')}">${ic('search', 'ic')}</button>${bell()}</header>
      <div class="bd-strip">${COLS.map(([c, ja, en]) => `<div class="bd-col" data-col="${c}"><div class="bd-head"><i></i><span>${t(ja, en)}</span><span class="bd-count"></span></div><div class="bd-list"></div></div>`).join('')}</div>
      <button class="fab" data-open="bd-add" aria-label="${t('タスクを追加', 'Add task')}">${ic('add', 'ic')}</button>`;
    strip = $('.bd-strip', el);
  }
  COLS.forEach(([c]) => {
    const col = $(`.bd-col[data-col="${c}"]`, strip), ks = DATA.tasks.filter(k => k.col === c);
    $('.bd-count', col).textContent = ks.length;
    $('.bd-list', col).innerHTML = ks.map(card).join('') || `<div class="bd-empty">${t('タスクなし · ここにドロップ', 'No tasks · drop here')}</div>`;
  });
  if (fresh.size && !freshTimer && el.classList.contains('active')) freshTimer = setTimeout(() => { fresh.clear(); freshN = 0; freshTimer = 0; }, 3500);
}
function scrollToCol(c) {
  const el = isOn('board'), col = el && $(`.bd-col[data-col="${c}"]`, el), strip = el && $('.bd-strip', el);
  if (col && strip) strip.scrollTo({left: col.offsetLeft - 16, behavior: 'smooth'});
}
function setupBoard(el) {
  if (el._bd) return; el._bd = 1;
  const strip = $('.bd-strip', el);
  let st = null, raf = 0, suppress = 0;
  const scale = () => el.getBoundingClientRect().width / el.offsetWidth || 1;
  const cols = () => $$('.bd-col', strip);
  const colAt = x => { let best = null, bd = 1e9; cols().forEach(c => { const r = c.getBoundingClientRect(), d = x < r.left ? r.left - x : x > r.right ? x - r.right : 0; if (d < bd) { bd = d; best = c; } }); return best; };
  const place = () => {
    const sr = el.getBoundingClientRect(), k = scale();
    st.g.style.transform = `translate(${(st.x - st.gx - sr.left) / k}px,${(st.y - st.gy - sr.top) / k}px)`;
    const o = colAt(st.x);
    if (o !== st.over) { cols().forEach(c => c.classList.toggle('bd-over', c === o)); st.over = o; }
  };
  const tick = () => {
    if (!st || !st.lifted) return;
    const r = strip.getBoundingClientRect();
    if (st.x < r.left + 56) strip.scrollLeft -= 12; else if (st.x > r.right - 56) strip.scrollLeft += 12;
    place(); raf = requestAnimationFrame(tick);
  };
  const lift = () => {
    if (!st) return;
    const card = st.card, r = card.getBoundingClientRect();
    st.lifted = true; dragging = true;
    st.gx = st.x - r.left; st.gy = st.y - r.top;
    const g = document.createElement('div'); g.className = 'bd-ghost';
    const cl = card.cloneNode(true); cl.classList.remove('bd-new', 'bd-hole'); cl.style.cssText = `width:${card.offsetWidth}px`;
    g.appendChild(cl); el.appendChild(g); st.g = g;
    card.classList.add('bd-hole'); strip.classList.add('bd-dragging');
    if (navigator.vibrate) try { navigator.vibrate(12); } catch (e) {}
    place(); raf = requestAnimationFrame(tick);
  };
  const end = commit => {
    if (!st) return;
    clearTimeout(st.timer);
    const s = st; st = null; cancelAnimationFrame(raf);
    if (!s.lifted) return;
    dragging = false; suppress = Date.now() + 150;
    strip.classList.remove('bd-dragging'); s.g.remove(); s.card.classList.remove('bd-hole');
    cols().forEach(c => c.classList.remove('bd-over'));
    const task = DATA.tasks.find(k => k.id === s.id), col = s.over && s.over.dataset.col;
    if (commit && task && col && task.col !== col) { Bus.emit('task:update', {task: {...task, col}}); scrollToCol(col); }
    else { if (pendBoard) { pendBoard = false; renderBoard(el); } if (col) scrollToCol(col); }
  };
  // Mouse: drag empty space or a card sideways to pan; touch pans natively. Shift+wheel (or a sideways wheel) scrolls the strip.
  let pan = null;
  strip.addEventListener('wheel', e => {
    const dx = e.deltaX || (e.shiftKey ? e.deltaY : 0);
    if (!dx) return;
    e.preventDefault(); strip.scrollBy({left: dx});
  }, {passive: false});
  strip.addEventListener('pointerdown', e => {
    if (e.pointerType === 'mouse' && e.button === 0 && !e.target.closest('[data-push]')) pan = {x0: e.clientX, y0: e.clientY, sl: strip.scrollLeft, list: e.target.closest('.bd-list'), st0: e.target.closest('.bd-list') ? e.target.closest('.bd-list').scrollTop : 0, on: false};
    const c = e.target.closest('.bd-card');
    if (!c || (e.pointerType === 'mouse' && e.button !== 0) || e.target.closest('[data-push]')) return;
    st = {card: c, id: c.dataset.id, x0: e.clientX, y0: e.clientY, x: e.clientX, y: e.clientY, lifted: false};
    st.timer = setTimeout(lift, 250);
  });
  addEventListener('pointermove', e => {
    if (pan && !(st && st.lifted)) {
      const dx = e.clientX - pan.x0, dy = e.clientY - pan.y0;
      if (!pan.on && Math.hypot(dx, dy) > 10) { pan.on = true; strip.classList.add('bd-dragging'); strip.style.userSelect = 'none'; if (st) { clearTimeout(st.timer); st = null; } }
      if (pan.on) { strip.scrollLeft = pan.sl - dx / scale(); if (pan.list) pan.list.scrollTop = pan.st0 - dy / scale(); }
    }
    if (!st) return;
    st.x = e.clientX; st.y = e.clientY;
    if (!st.lifted && Math.hypot(st.x - st.x0, st.y - st.y0) > 10) { clearTimeout(st.timer); st = null; }
  });
  const endPan = () => { if (!pan) return; if (pan.on) { suppress = Date.now() + 150; strip.classList.remove('bd-dragging'); strip.style.userSelect = ''; } pan = null; };
  addEventListener('pointerup', () => { end(true); endPan(); });
  addEventListener('pointercancel', () => { end(false); endPan(); });
  strip.addEventListener('touchmove', e => { if (st && st.lifted && e.cancelable) e.preventDefault(); }, {passive: false});
  strip.addEventListener('contextmenu', e => e.preventDefault());
  el.addEventListener('click', e => {
    if (Date.now() < suppress || e.target.closest('[data-push],[data-open],[data-toast]')) return;
    const c = e.target.closest('.bd-card');
    if (c) App.push('task', {id: c.dataset.id});
  });
}

/* ---------- Task detail ---------- */
let curTask = null;
function renderTask(el) {
  const k = DATA.tasks.find(x => x.id === curTask), c = $('.content', el);
  if (!k || !c) return;
  const m = k.machine && mc(k.machine), src = k.from && findMsg(k.from);
  c.innerHTML = `<div class="pad stagger">
    <h2 class="bd-big">${esc(ttl(k))}</h2>
    <div class="bd-seg" role="group">${COLS.map(([col, ja, en]) => `<button data-bcol="${col}" class="${k.col === col ? 'on' : ''}">${ic('tick')}${t(ja, en)}</button>`).join('')}</div>
    <div class="hm-h">${t('担当', 'Assigned to')}</div>
    <div class="bd-nm">${k.assignees.map(u => `<span>${av(u, 'sm')}${esc(nameOf(u))}</span>`).join('')}</div>
    <div class="card" style="margin-top:16px"><div class="list-item bd-row">${ic('clock', 'lead')}<div class="grow"><div class="sub">${t('期限', 'Due')}</div><div>${esc(dueT(k.due) || '-')}</div></div></div>
      ${m ? `<div class="list-item bd-row" data-tap data-push="machine" ${param({id: m.id})}>${ic('build', 'lead')}<div class="grow"><div class="sub">${t('機械', 'Machine')}</div><div>${esc(mcName(m))}</div></div>${ic('next')}</div>` : ''}</div>
    ${k.nm ? `<div class="card" style="margin-top:12px"><button class="list-item bd-row" data-tap data-go="nearmiss-detail" ${param({id: k.nm})} style="width:100%;text-align:left">${ic('warn', 'lead')}<div class="grow"><div class="sub">${t('元の報告', 'Source')}</div><div>${t('ヒヤリハット報告から', 'From near-miss report')}</div></div>${ic('next')}</button></div>` : ''}
    ${src ? `<div class="hm-h">${t('元のメッセージ', 'Source message')}</div>
      <blockquote class="bd-q"><div class="who">${av(src.from, 'sm')}<span>${esc(nameOf(src.from))} · ${esc(src.at)}</span></div><div class="txt">${esc(textOf(src))}</div></blockquote>
      <button class="btn tonal block" style="margin-top:12px;height:48px;border-radius:24px" data-push="chat" ${param({thread: src.thread, msg: src.id})}>${ic('chat')}${t('チャットで開く', 'Open in chat')}</button>` : ''}
    <div class="hm-h">${t('コメント', 'Comments')}</div>
    <button class="bd-cm" data-toast="${t('コメントはプロトタイプでは未対応', 'Comments are not in the prototype')}"><span style="flex:1">${t('コメントを書く…', 'Add a comment…')}</span>${ic('send')}</button>
  </div>`;
}

/* ---------- Machines ---------- */
function renderMachines(el, anim) {
  const c = $('.content', el); if (!c) return;
  if (FM) c.innerHTML = `<div class="pad${stag(anim)}" style="padding-top:8px">${DATA.machines.map(m => `<button class="card mc-card" data-push="machine" ${param({id: m.id})}>
      <div class="mc-name">${esc(m.ja)}</div><div class="sub hm-sub2">${esc(m.name)}</div>
      <div class="mc-ws">${workers().map(u => `<span class="mc-wchip" data-r="${m.ready[u] ? 1 : 0}">${av(u, 'sm')}${esc(short(u))}${ic(m.ready[u] ? 'tick' : 'lock')}</span>`).join('')}</div></button>`).join('')}</div>`;
  else c.innerHTML = `<div class="pad${stag(anim)}" style="padding-top:8px">${DATA.machines.map(m => machineBig(m, true)).join('')}</div>`;
}
function machineBig(m, link) {
  const r = !!m.ready[ME];
  return `<div class="mc-big" data-r="${r ? 1 : 0}"${link ? (r ? ` data-push="machine" ${param({id: m.id})}` : ` data-go="note" ${param({machine: m.id})}`) : ''}>
    <span class="st">${ic(r ? 'check' : 'lock', 'ic')}</span>
    <div style="flex:1;min-width:0"><div class="nm hm-ell">${esc(mcName(m))}</div><div class="stt">${r ? 'Ready to operate' : 'Locked'}</div>
      ${r ? '' : `<button class="btn tonal" data-go="note" ${param({machine: m.id})}>Take the Check to unlock</button>`}</div></div>`;
}
function qrSvg(seed) {
  const N = 25; let s = [...seed].reduce((a, ch) => (a * 31 + ch.charCodeAt(0)) >>> 0, 7);
  const rnd = () => (s = (s * 1664525 + 1013904223) >>> 0) / 4294967296;
  const corners = [[0, 0], [N - 7, 0], [0, N - 7]]; let d = '';
  for (let y = 0; y < N; y++) for (let x = 0; x < N; x++) {
    let on = null;
    for (const [cx, cy] of corners) {
      const lx = x - cx, ly = y - cy;
      if (lx >= -1 && lx <= 7 && ly >= -1 && ly <= 7) on = lx >= 0 && lx <= 6 && ly >= 0 && ly <= 6 && (lx === 0 || lx === 6 || ly === 0 || ly === 6 || (lx >= 2 && lx <= 4 && ly >= 2 && ly <= 4));
    }
    if (on === null) on = rnd() > .52;
    if (on) d += `M${x} ${y}h1v1h-1z`;
  }
  return `<svg viewBox="-1 -1 ${N + 2} ${N + 2}" role="img" aria-label="QR"><path d="${d}" fill="#1d1b20"/></svg>`;
}
let curMc = null;
function renderMachine(el) {
  const m = mc(curMc), c = $('.content', el); if (!m || !c) return;
  const tasks = DATA.tasks.filter(k => k.machine === m.id);
  c.innerHTML = `<div class="pad stagger">
    ${FM ? `<div class="hm-h" style="margin-top:8px">${t('作業者の状態', 'Workers')}</div>` : machineBig(m, false)}
    ${FM ? '' : '<div class="hm-h">Everyone on this machine</div>'}
    <div class="card">${workers().map(u => `<div class="list-item">${av(u)}<div class="grow">${esc(nameOf(u))}${u === ME ? ' <span class="sub">(' + t('あなた', 'you') + ')</span>' : ''}</div>${msChip(m.ready[u])}</div>`).join('')}</div>
    <button class="btn block hm-panel" data-go="panel" ${param({machine: m.id})}>${ic('build', 'ic')}${t('マシンパネルを開く', 'Open Machine Panel')}</button>
    <div class="hm-h">${t('機械QR', 'Machine QR')}</div>
    <div class="mc-qr">${qrSvg(m.id)}</div>
    <div class="mc-cap">${t('機械に貼って、スキャンでチェックと承認を開始', 'Stick it on the machine. Scan to start the Check and sign-off.')}</div>
    <button class="btn tonal block" style="margin-top:12px;height:48px;border-radius:24px" data-toast="${t('QRの印刷はプロトタイプでは未対応', 'Printing is not in the prototype')}">${t('QRを印刷', 'Print QR')}</button>
    <div class="hm-h">${t('この機械のタスク', 'Recent tasks on this machine')}</div>
    <div class="card">${tasks.map(k => `<div class="list-item" data-tap data-push="task" ${param({id: k.id})}><div class="grow"><div class="hm-ell">${esc(ttl(k))}</div><div class="sub">${esc(dueT(k.due))}</div></div>${ic('next')}</div>`).join('')
      || `<div class="hm-empty"><span>${t('タスクなし', 'No tasks yet')}</span></div>`}</div>
  </div>`;
}

/* ---------- Me ---------- */
const fmtD = at => { const d = new Date(at); return isNaN(d) ? String(at || '') : d.toLocaleDateString(FM ? 'ja-JP' : 'en-US', {month: 'short', day: 'numeric'}); };
const signoffOf = (u, m) => (DATA.signoffs || []).filter(x => x.user === u && x.machine === m).pop();
function renderMe(el, anim) {
  const c = $('.content', el); if (!c) return;
  const u = DATA.users[ME], other = ROLE === 'foreman' ? 'worker' : 'foreman';
  const passport = DATA.machines.map(m => { const r = m.ready[ME], so = signoffOf(ME, m.id);
    const sub = r ? (so ? `Signed off ${esc(fmtD(so.at))}${so.version ? ' · ' + esc(String(so.version).replace(/^(\d)/, 'v$1')) : ''}` : 'Valid sign-off') : 'Not signed off yet';
    return `<div class="list-item${r ? '' : ' off'}" data-tap ${r ? `data-push="machine"` : `data-go="note"`} ${param(r ? {id: m.id} : {machine: m.id})}>${ic(r ? 'verified' : 'lock', 'ic ' + (r ? 'ok' : 'no'))}<div class="grow"><div>${esc(mcName(m))}</div><div class="sub">${sub}</div></div>${r ? msChip(true) : `<span class="hm-st" data-s="wait">${ic('next')}Take Check</span>`}</div>`; }).join('');
  const team = workers().map(w => { const n = DATA.machines.filter(m => m.ready[w]).length; return `<div class="list-item">${av(w)}<div class="grow"><div>${esc(nameOf(w))}</div><div class="sub">${n}/${DATA.machines.length} 台 使用可</div></div></div>`; }).join('');
  const roleRow = Bus.paired ? '' : `<li><button class="list-item" data-tap data-role-switch style="width:100%;text-align:left">${ic('swap', 'lead')}<div class="grow">${t('ロールを切り替え', 'Switch role')}<div class="sub">${other === 'worker' ? '作業者 (Worker · English)' : 'Foreman (日本語)'}</div></div>${ic('next')}</button></li>`;
  c.innerHTML = `<div class="${stag(anim).trim()}">
    <div class="me-prof">${av(ME, 'lg')}<div><h2>${esc(nameOf(ME))}</h2><span class="me-role">${FM ? '職長 · 第2工場' : 'Worker · Plant 2'}</span></div></div>
    ${FM ? `<div class="hm-h pad">チーム</div><div class="card" style="margin:0 16px">${team}<button class="list-item" data-tap data-go="skills" style="width:100%;text-align:left">${ic('grid', 'lead')}<div class="grow">力量表を開く</div>${ic('next')}</button></div>`
      : `<div class="hm-h pad">Skill Passport</div><div class="card me-pass" style="margin:0 16px">${passport}</div>`}
    ${window.uxSettingsHtml ? window.uxSettingsHtml(roleRow) : ''}</div>`;
}

/* ---------- Screens ---------- */
Object.assign(SCREENS, {
  home(el) {
    if (!el.dataset.ready) { el.dataset.ready = 1; shell(el, 'Wakatta?', '', bell());
      // worker: big Scan machine CTA in the thumb zone, above the tab bar
      if (!FM) el.insertAdjacentHTML('beforeend', `<button class="hm-scan" data-go="scan">${ic('qr', 'ic')}<span>Scan machine</span></button>`); }
    const c = $('.content', el);
    if (!homeLoaded) {
      c.innerHTML = homeSkel();
      setTimeout(() => { homeLoaded = true; if (isOn('home')) renderHome(el, true); }, 500);
    } else renderHome(el, true);
  },
  chats(el) {
    if (!el.dataset.ready) { el.dataset.ready = 1; shell(el, t('チャット', 'Chats'), '', `<button class="icon-btn" data-push="connect" aria-label="${t('QRでつながる', 'Connect with QR')}">${ic('qr', 'ic')}</button><button class="icon-btn" data-toast="${t('検索はプロトタイプでは未対応', 'Search is not in the prototype')}" aria-label="${t('検索', 'Search')}">${ic('search', 'ic')}</button>${bell()}`); }
    if (!$('.fab', el)) el.insertAdjacentHTML('beforeend', `<button class="fab" data-lf="newtalk" aria-label="${t('新しい会話', 'New talk')}">${ic('add', 'ic')}</button>`);
    renderChats(el, true);
  },
  board(el) { renderBoard(el, true); setupBoard(el); },
  machines(el) {
    if (!el.dataset.ready) { el.dataset.ready = 1; shell(el, t('機械', 'Machines'), '', bell());
      el.insertAdjacentHTML('beforeend', `<button class="fab" data-go="scan" aria-label="${t('機械をスキャン', 'Scan machine')}">${ic('qr', 'ic')}</button>`); }
    renderMachines(el, true);
  },
  me(el) {
    if (!el.dataset.ready) { el.dataset.ready = 1; shell(el, t('自分', 'Me'), '', bell()); }
    renderMe(el, true);
  },
  task(el, p) { curTask = p.id; shell(el, t('タスク', 'Task'), backBtn, ''); renderTask(el); },
  machine(el, p) { curMc = p.id; const m = mc(p.id); shell(el, m ? mcName(m) : t('機械', 'Machine'), backBtn, ''); renderMachine(el); },
});

/* ---------- refresh + Bus ---------- */
function badge() {
  const n = DATA.threads.reduce((a, th) => a + (th.unread || 0), 0), pill = $('.tab[data-tab="chats"] .pill');
  if (!pill) return;
  let b = $('.cl-tbadge', pill);
  if (!n) { if (b) b.remove(); return; }
  if (!b) { b = document.createElement('span'); b.className = 'cl-tbadge'; pill.appendChild(b); }
  b.textContent = n;
}
// re-render every screen that was opened before (screens under the stack top stay in the DOM and must not go stale)
const built = id => { const e = scr(id); return e && e.firstElementChild ? e : null; };
function refresh() {
  let e;
  if (homeLoaded && (e = built('home'))) renderHome(e, false);
  if (e = built('chats')) renderChats(e, false);
  if (e = built('board')) renderBoard(e, false);
  if (e = built('machines')) renderMachines(e, false);
  if (e = built('me')) renderMe(e, false);
  if (curTask && (e = built('task'))) renderTask(e);
  if (curMc && (e = built('machine'))) renderMachine(e);
  badge();
  if (window.uxRefresh) window.uxRefresh();
}
window.hmRefresh = refresh; // part D re-renders the Chats list after it changes a personal Thread
Bus.on('msg:new', ({thread, msg}) => { bump[thread] = Date.now(); refresh(); setTimeout(refresh, 60); });
Bus.on('wk:stage', () => refresh());
// part C / D data changed: their own handlers run in the same tick, so re-render after them
['followup:new', 'followup:resolve', 'signoff', 'nearmiss:new', 'nearmiss:ack', 'briefing:new', 'briefing:answer', 'confusion:new', 'notice:new', 'notice:answer', 'ask:new', 'ask:reply'].forEach(ev => Bus.on(ev, () => setTimeout(refresh, 50)));
Bus.on('check:answer', ({msgId, user, status}) => { (ov[msgId] = ov[msgId] || {})[user] = status; refresh(); });
Bus.on('task:update', ({task}) => {
  if (!task || !task.id) return;
  const i = DATA.tasks.findIndex(k => k.id === task.id);
  if (i < 0) DATA.tasks.push(task); else DATA.tasks[i] = task;
  const moved = seen.has(task.id) && colOf[task.id] !== task.col;
  if (!seen.has(task.id)) { seen.add(task.id); fresh.set(task.id, freshN++ * 160); }
  else if (moved) fresh.set(task.id, 0);
  colOf[task.id] = task.col;
  if (moved && !dragging && isOn('board')) scrollToCol(task.col);
  refresh();
});

/* ---------- tab labels, role switch ---------- */
const LBL = {home: ['ホーム', 'Home'], chats: ['チャット', 'Chats'], board: ['ボード', 'Board'], machines: ['機械', 'Machines'], me: ['自分', 'Me']};
$$('.tab[data-tab]').forEach(b => { const l = LBL[b.dataset.tab]; if (!l) return; const s = $('[data-lbl]', b); if (s) s.textContent = t(l[0], l[1]); b.setAttribute('aria-label', t(l[0], l[1])); });
document.addEventListener('click', e => {
  const g = e.target.closest('[data-go]');
  if (g) { go(g.dataset.go, g.dataset.param ? JSON.parse(g.dataset.param) : {}); return; }
  const seg = e.target.closest('[data-bcol]');
  if (seg) {
    const k = DATA.tasks.find(x => x.id === curTask);
    if (k && k.col !== seg.dataset.bcol) Bus.emit('task:update', {task: {...k, col: seg.dataset.bcol}});
    return;
  }
  if (!e.target.closest('[data-role-switch]')) return;
  location.search = '?role=' + (FM ? 'worker' : 'foreman') + (QS.has('embed') ? '&embed=1' : '');
});
window.__wkB = {badge, refresh, go, qrSvg, tally, ICON, workers};
})();
