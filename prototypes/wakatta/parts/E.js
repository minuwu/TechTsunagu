/* ===== Part E: notification centre, onboarding, settings, analytics, skill matrix, follow-ups, learning =====
   Prefix ux-. Runs BEFORE App exists: App only inside event handlers / timeouts. Exports: window.uxUnread, window.uxSettings, window.uxSettingsHtml */
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
const top = () => { const s = App.stack; return s[s.length - 1]; };
const B = () => window.__wkB || {};
const go = (id, p) => B().go ? B().go(id, p) : (scr(id) ? App.push(id, p || {}) : App.toast(t('この画面はまだ準備中です', 'Coming soon')));
const later = (f, ms = 60) => setTimeout(f, ms);

/* ---------- settings (persisted per role in localStorage) ---------- */
const KEY = 'wk.ux.' + ROLE;
const S = {dark: false, largeText: false, offline: false, nMsg: true, nSafe: true, nLife: true};
try { Object.assign(S, JSON.parse(localStorage.getItem(KEY) || '{}')); } catch (e) {}
const save = () => { try { localStorage.setItem(KEY, JSON.stringify(S)); } catch (e) {} };
function applySettings() {
  const h = document.documentElement;
  h.dataset.theme = S.dark ? 'dark' : 'light';
  if (S.largeText) h.dataset.large = '1'; else delete h.dataset.large;
  const m = $('meta[name="theme-color"]'); if (m) m.content = S.dark ? '#0c1626' : '#e4effb';
}
applySettings();   // synchronous and DOM-only: no flash of the wrong theme
window.uxSettings = S;

/* ---------- icons ---------- */
const P = {
  bell: 'M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z',
  back: 'M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z',
  chat: 'M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z',
  task: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z',
  tick: 'M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z',
  build: 'M22.7 19l-9.1-9.1c.9-2.3.4-5-1.5-6.9-2-2-5-2.4-7.4-1.3L9 6 6 9 1.6 4.7C.4 7.1.9 10.1 2.9 12.1c1.9 1.9 4.6 2.4 6.9 1.5l9.1 9.1c.4.4 1 .4 1.4 0l2.3-2.3c.5-.4.5-1.1.1-1.4z',
  lock: 'M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z',
  warn: 'M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z',
  camp: 'M18 11v2h4v-2h-4zm-2 6.61c.96.71 2.21 1.65 3.2 2.39.4-.53.8-1.07 1.2-1.6-.99-.74-2.24-1.68-3.2-2.4-.4.54-.8 1.08-1.2 1.61zM20.4 5.6c-.4-.53-.8-1.07-1.2-1.6-.99.74-2.24 1.68-3.2 2.4.4.53.8 1.07 1.2 1.6.96-.72 2.21-1.65 3.2-2.4zM4 9c-1.1 0-2 .9-2 2v2c0 1.1.9 2 2 2h1v4h2v-4h1l5 3V6L8 9H4zm11.5 3c0-1.33-.58-2.53-1.5-3.35v6.69c.92-.81 1.5-2.01 1.5-3.34z',
  groups: 'M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z',
  help: 'M11 18h2v-2h-2v2zm1-16C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z',
  qr: 'M9.5 6.5v3h-3v-3h3M11 5H5v6h6V5zm-1.5 9.5v3h-3v-3h3M11 13H5v6h6v-6zm6.5-6.5v3h-3v-3h3M19 5h-6v6h6V5zm-6 8h1.5v1.5H13V13zm1.5 1.5H16V16h-1.5v-1.5zM16 13h1.5v1.5H16V13zm-3 3h1.5v1.5H13V16zm1.5 1.5H16V19h-1.5v-1.5zM16 16h1.5v1.5H16V16zm1.5-1.5H19V16h-1.5v-1.5zm0 3H19V19h-1.5v-1.5zM22 7h-2V4h-3V2h5v5zm0 15v-5h-2v3h-3v2h5zM2 22h5v-2H4v-3H2v5zM2 2v5h2V4h3V2H2z',
  next: 'M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z',
  dark: 'M12 3c-4.97 0-9 4.03-9 9s4.03 9 9 9 9-4.03 9-9c0-.46-.04-.92-.1-1.36-.98 1.37-2.58 2.26-4.4 2.26-2.98 0-5.4-2.42-5.4-5.4 0-1.81.89-3.42 2.26-4.4-.44-.06-.9-.1-1.36-.1z',
  text: 'M2.5 4v3h5v12h3V7h5V4h-13zm19 5h-9v3h3v7h3v-7h3V9z',
  wifioff: 'M22.99 9C19.15 5.16 13.8 3.76 8.84 4.78l2.52 2.52c3.47-.17 6.99 1.05 9.63 3.7l2-2zm-4 4c-1.29-1.29-2.84-2.13-4.49-2.56l3.53 3.53.96-.97zM2 3.05L5.07 6.1C3.6 6.82 2.22 7.78 1 9l1.99 2c1.24-1.24 2.67-2.16 4.2-2.77l2.24 2.24C7.81 10.89 6.27 11.73 5 13v.01L6.99 15c1.36-1.36 3.14-2.04 4.92-2.06L18.98 20l1.27-1.26L3.29 1.79 2 3.05zM9 17l3 3 3-3c-1.65-1.66-4.34-1.66-6 0z',
  globe: 'M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zm6.93 6h-2.95c-.32-1.25-.78-2.45-1.38-3.56 1.84.63 3.37 1.91 4.33 3.56zM12 4.04c.83 1.2 1.48 2.53 1.91 3.96h-3.82c.43-1.43 1.08-2.76 1.91-3.96zM4.26 14C4.1 13.36 4 12.69 4 12s.1-1.36.26-2h3.38c-.08.66-.14 1.32-.14 2 0 .68.06 1.34.14 2H4.26zm.82 2h2.95c.32 1.25.78 2.45 1.38 3.56-1.84-.63-3.37-1.9-4.33-3.56zm2.95-8H5.08c.96-1.66 2.49-2.93 4.33-3.56C8.81 5.55 8.35 6.75 8.03 8zM12 19.96c-.83-1.2-1.48-2.53-1.91-3.96h3.82c-.43 1.43-1.08 2.76-1.91 3.96zM14.34 14H9.66c-.09-.66-.16-1.32-.16-2 0-.68.07-1.35.16-2h4.68c.09.65.16 1.32.16 2 0 .68-.07 1.34-.16 2zm.25 5.56c.6-1.11 1.06-2.31 1.38-3.56h2.95c-.96 1.65-2.49 2.93-4.33 3.56zM16.36 14c.08-.66.14-1.32.14-2 0-.68-.06-1.34-.14-2h3.38c.16.64.26 1.31.26 2s-.1 1.36-.26 2h-3.38z',
  replay: 'M12 5V1L7 6l5 5V7c3.31 0 6 2.69 6 6s-2.69 6-6 6-6-2.69-6-6H4c0 4.42 3.58 8 8 8s8-3.58 8-8-3.58-8-8-8z',
  all: 'M18 7l-1.41-1.41-6.34 6.34 1.41 1.41L18 7zm4.24-1.41L11.66 16.17 7.48 12l-1.41 1.41L11.66 19l12-12-1.42-1.41zM.41 13.41L6 19l1.41-1.41L1.83 12 .41 13.41z',
  mic: 'M12 14c1.66 0 2.99-1.34 2.99-3L15 5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3zm5.3-3c0 3-2.54 5.1-5.3 5.1S6.7 14 6.7 11H5c0 3.41 2.72 6.23 6 6.72V21h2v-3.28c3.28-.48 6-3.3 6-6.72h-1.7z',
  copy: 'M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z',
  close: 'M19 6.41 17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z',
  star: 'M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z',
  clock: 'M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z',
  verified: 'M23 12l-2.44-2.79.34-3.69-3.61-.82-1.89-3.2L12 2.96 8.6 1.5 6.71 4.69 3.1 5.5l.34 3.7L1 12l2.44 2.79-.34 3.7 3.61.82L8.6 22.5l3.4-1.47 3.4 1.46 1.89-3.19 3.61-.82-.34-3.69L23 12zm-12.91 4.72l-3.8-3.81 1.48-1.48 2.32 2.33 5.85-5.87 1.48 1.48-7.33 7.35z',
  up: 'M7 14l5-5 5 5z', down: 'M7 10l5 5 5-5z',
  book: 'M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 4h5v8l-2.5-1.5L6 12V4z',
  fire: 'M13.5.67s.74 2.65.74 4.8c0 2.06-1.35 3.73-3.41 3.73-2.07 0-3.63-1.67-3.63-3.73l.03-.36C5.21 7.51 4 10.62 4 14c0 4.42 3.58 8 8 8s8-3.58 8-8C20 8.61 17.41 3.8 13.5.67zM11.71 19c-1.78 0-3.22-1.4-3.22-3.14 0-1.62 1.05-2.76 2.81-3.12 1.77-.36 3.6-1.21 4.62-2.58.39 1.29.59 2.65.59 4.04 0 2.65-2.15 4.8-4.8 4.8z',
  check: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z',
  hour: 'M6 2v6h.01L6 8.01 10 12l-4 4 .01.01H6V22h12v-5.99h-.01L18 16l-4-4 4-3.99-.01-.01H18V2H6zm10 14.5V20H8v-3.5l4-4 4 4zm-4-5l-4-4V4h8v3.5l-4 4z',
};
const ic = (n, c = 'ic') => `<svg class="${c}" viewBox="0 0 24 24" aria-hidden="true"><path d="${P[n]}"/></svg>`;
const workers = () => Object.keys(DATA.users).filter(u => DATA.users[u].role === 'worker');
const av = (id, cls = '') => { const u = DATA.users[id]; return u ? `<span class="hm-av ${cls}" style="background:${u.color}">${esc(u.initials)}</span>` : ''; };
const first = id => String(nameOf(id)).split(' ')[0];
const findMsg = id => Object.values(DATA.messages).flat().find(m => m.id === id);
const mcOf = id => DATA.machines.find(m => m.id === id);
const mcN = m => FM ? m.ja : m.name;

/* ---------- Wakatta status helpers (same rules as part B, kept local) ---------- */
const uxOv = {};
Bus.on('check:answer', ({msgId, user, status}) => { (uxOv[msgId] = uxOv[msgId] || {})[user] = status; });
const wkMsgs = () => Object.values(DATA.messages).flat().filter(m => m.wk);
const rcpt = m => ((DATA.threads.find(x => x.id === m.thread) || {}).members || []).filter(u => u !== m.from && DATA.users[u] && DATA.users[u].role === 'worker');
const stOf = (m, u) => (uxOv[m.id] && uxOv[m.id][u]) || (m.wk.results && m.wk.results[u] && m.wk.results[u].status) || 'wait';
function liveTally() {
  const all = {green: 0, orange: 0, red: 0, wait: 0}, per = {};
  wkMsgs().forEach(m => rcpt(m).forEach(u => { const s = stOf(m, u); all[s]++; (per[u] = per[u] || {green: 0, orange: 0, red: 0, wait: 0})[s]++; }));
  return {all, per};
}

/* ---------- follow-ups / confusions (owned by part A, read-only here) ---------- */
const FU = () => DATA.followups || [];
const FU_ST = {open: ['要フォロー', 'Open'], clarified: ['説明済', 'Explained'], understood: ['理解', 'Understood']};
// shortcut: local fallback seed used only while part A has not seeded DATA.confusions; drop once A's seed is the only source
const CF_SEED = [
  {user: 'u_siti', term: '金型交換', msgId: 'm1', at: 1}, {user: 'u_siti', term: 'ロックアウト', msgId: 'm1', at: 2}, {user: 'u_siti', term: '点検表', msgId: 'm1', at: 3},
  {user: 'u_nguyen', term: 'ロックアウト', msgId: 'm1', at: 4}, {user: 'u_nguyen', term: '金型交換', msgId: 'm1', at: 5}, {user: 'u_rahim', term: '金型交換', msgId: 'm1', at: 6},
  {user: 'u_siti', term: '金型交換', msgId: 'm1', at: 7}, {user: 'u_siti', term: '集合', msgId: 'm1', at: 8}, {user: 'u_nguyen', term: '油圧', msgId: 'm1', at: 9},
  {user: 'u_rahim', term: '点検表', msgId: 'm1', at: 10}, {user: 'u_siti', term: '油圧', msgId: 'm1', at: 11}, {user: 'u_siti', term: 'ロックアウト', msgId: 'm1', at: 12},
];
const CF = () => (DATA.confusions && DATA.confusions.length ? DATA.confusions : CF_SEED);
const TERM_EN = {'金型交換': 'die change', 'ロックアウト': 'lockout', '点検表': 'inspection sheet', '集合': 'gather', '油圧': 'hydraulic pressure', '電源': 'power', '保護具': 'PPE'};
const termEn = term => { const g = (DATA.glossary || []).find(x => x.ja === term); return (g && g.en) || TERM_EN[term] || ''; };
const inGloss = term => (DATA.glossary || []).some(g => g.ja === term);
// age in days for a number (ms epoch) / ISO string; null when the value is not a date (e.g. 'HH:MM')
const days = at => { const d = typeof at === 'number' ? (at > 1e11 ? at : NaN) : Date.parse(at); return isNaN(d) ? null : (Date.now() - d) / 864e5; };
function ago(at) {
  const d = days(at);
  if (d == null) return String(at == null ? '' : at);
  const m = Math.round(d * 1440);
  return m < 1 ? t('たった今', 'just now') : m < 60 ? t(m + '分前', m + ' min ago') : m < 1440 ? t(Math.round(m / 60) + '時間前', Math.round(m / 60) + ' h ago') : t(Math.round(m / 1440) + '日前', Math.round(m / 1440) + ' d ago');
}
// per-term stats: count, workers affected, trend (later half vs earlier half of the rows, ordered as stored)
function termStats() {
  const rows = CF(), by = {};
  rows.forEach((r, i) => { const o = by[r.term] = by[r.term] || {term: r.term, n: 0, users: new Set(), early: 0, late: 0, rows: []}; o.n++; o.users.add(r.user); o.rows.push(r); (i < rows.length / 2 ? o.early++ : o.late++); });
  return Object.values(by).map(o => Object.assign(o, {users: [...o.users], trend: o.late - o.early})).sort((a, b) => b.n - a.n);
}
const msgText = id => { const m = findMsg(id); return m ? (FM ? m.ja : m.en) : ''; };

/* ===================================================================
   Notification centre + heads-up banner
   =================================================================== */
if (!DATA.notifications.length) {   // seed once per load so the bell badge and centre have content
  DATA.notifications.push(...(FM ? [
    {id: 'ns1', at: '08:20', read: false, to: 'foreman', title: 'Siti さんが「金型交換」でつまずきました', body: '要フォローです。もう一度説明しましょう。', push: ['followups']},
    {id: 'ns2', at: '08:15', read: false, to: 'foreman', title: 'タスク完了: 3号機 オイル点検', body: 'Siti さんが完了しました。', push: ['task', {id: 'k1'}]},
    {id: 'ns3', at: '昨日', read: true, to: 'foreman', title: '今週の理解度レポート', body: 'チーム全体の理解度は 74% でした。', push: ['analytics']},
  ] : [
    {id: 'ns1', at: '08:02', read: false, to: 'worker', title: 'New Check from Tanaka', body: 'Answer the question about Machine 3 oil.', push: ['chat', {thread: 't_line2', msg: 'm1'}]},
    {id: 'ns2', at: '07:50', read: false, to: 'worker', title: 'Press #2 is locked for you', body: 'Read the Note and take the Check to unlock it.', push: ['note', {machine: 'mc2'}]},
    {id: 'ns3', at: 'Yesterday', read: true, to: 'worker', title: 'Welcome to Wakatta?', body: 'Your Skill Passport is ready.', push: ['me']},
  ]));
}
const unread = () => DATA.notifications.filter(n => !n.read).length;
window.uxUnread = unread;
function badges() {
  const n = unread();
  $$('.ux-bdg').forEach(b => { b.hidden = !n; b.textContent = n > 9 ? '9+' : n; });
}
const TYPES = {chat: ['chat', 'p'], task: ['task', 'ok'], followups: ['help', 'w'], analytics: ['help', 'p'], skills: ['build', 'p'],
  note: ['lock', 'w'], panel: ['build', 'w'], scan: ['build', 'w'], machine: ['build', 'w'], nearmiss: ['warn', 'b'], 'nearmiss-list': ['warn', 'b'], 'nearmiss-detail': ['warn', 'b'],
  briefing: ['camp', 'w'], 'briefing-board': ['camp', 'w'], life: ['groups', 'p'], notice: ['groups', 'p'], 'notice-new': ['groups', 'p'], ask: ['help', 'p'], senpai: ['groups', 'p'], supportlog: ['groups', 'p'], learning: ['book', 'ok']};
const typeOf = n => TYPES[n.type || (n.push && n.push[0])] || ['bell', 'p'];
const catOf = n => { const k = (n.push && n.push[0]) || ''; return /^(chat|task|followups)$/.test(k) ? 'nMsg' : /^(nearmiss|briefing|note|panel|scan|machine)/.test(k) ? 'nSafe' : /^(life|notice|ask|senpai|supportlog)/.test(k) ? 'nLife' : 'nMsg'; };

function openNotif(n) {
  if (!n.read) Bus.emit('notify:read', {ids: [n.id], role: ROLE});
  const p = n.push;
  if (!p || !p[0]) return;
  if (top() === p[0]) return;
  if (['home', 'chats', 'board', 'machines', 'me'].includes(p[0])) { App.tab(p[0]); return; }
  go(p[0], p[1] || {});
}
// notify:read is applied only by the phone it was sent for (notifications are per role), inside a Bus handler
Bus.on('notify:read', ({ids, role}) => {
  if (role !== ROLE) return;
  DATA.notifications.forEach(n => { if (ids.includes(n.id)) n.read = true; });
  badges(); renderNotifs();
});
let elN = null;
function renderNotifs() {
  if (!elN || !elN.firstElementChild) return;
  const c = $('.content', elN), list = DATA.notifications;
  $('.ux-markall', elN).disabled = !unread();
  c.innerHTML = list.length ? `<ul class="list ux-nl">${list.map(n => { const [i, k] = typeOf(n); return `<li><button class="ux-nr${n.read ? '' : ' unread'}" data-ux-n="${esc(n.id)}" data-tap>
      <span class="ux-ni" data-k="${k}">${ic(i)}</span><span class="ux-nm"><span class="ux-nt">${esc(n.title)}</span><span class="ux-nb">${esc(n.body || '')}</span></span>
      <span class="ux-nside"><span class="ux-ntime">${esc(n.at)}</span>${n.read ? '' : '<i class="ux-ndot"></i>'}</span></button></li>`; }).join('')}</ul>`
    : `<div class="ux-empty">${ic('bell')}<b>${t('通知はありません', 'No notifications')}</b><span>${t('新しいお知らせがここに表示されます', 'New updates will show up here')}</span></div>`;
}
let hdT = 0;
function heads(n) {
  const h = $('#ux-heads'); if (!h) return;
  const [i, k] = typeOf(n);
  h.innerHTML = `<button class="ux-hd" data-ux-h="${esc(n.id)}"><span class="ux-ni" data-k="${k}">${ic(i)}</span><span class="ux-nm"><span class="ux-nt">${esc(n.title)}</span><span class="ux-nb">${esc(n.body || '')}</span></span></button>`;
  void h.offsetWidth; h.classList.add('on');
  clearTimeout(hdT); hdT = setTimeout(() => h.classList.remove('on'), 3000);
}
Bus.on('notify', n => {
  if (!(n.to === 'all' || n.to === ROLE)) return;
  later(() => { badges(); renderNotifs(); if (!isOn('notifications') && S[catOf(n)] !== false) heads(n); }, 0);
});

/* ===================================================================
   Settings (rendered inside Me by part B via uxSettingsHtml)
   =================================================================== */
const sw = (key, icon, label, sub) => `<li><label class="list-item">${ic(icon, 'lead')}<div class="grow">${label}${sub ? `<div class="sub">${sub}</div>` : ''}</div><input type="checkbox" class="switch" data-ux-set="${key}" ${S[key] ? 'checked' : ''} aria-label="${esc(label)}"></label></li>`;
window.uxSettingsHtml = extra => `
  <div class="hm-h pad">${t('設定', 'Settings')}</div>
  <div class="card" style="margin:0 16px"><ul class="list">
    <li class="list-item">${ic('globe', 'lead')}<div class="grow">${t('言語', 'Language')}</div><span class="me-val">${t('日本語', 'English')}</span></li>
    ${sw('dark', 'dark', t('ダークモード', 'Dark mode'))}
    ${sw('largeText', 'text', t('大きい文字', 'Large text'))}
    ${sw('offline', 'wifioff', t('オフラインを試す', 'Simulate offline'), t('表示のみ', 'Visual only'))}
    ${extra || ''}
  </ul></div>
  <div class="hm-h pad">${t('通知設定', 'Notification preferences')}</div>
  <div class="card" style="margin:0 16px"><ul class="list">
    ${sw('nMsg', 'chat', t('メッセージ・タスク', 'Messages & tasks'))}
    ${sw('nSafe', 'warn', t('安全・朝礼・機械', 'Safety, briefings & machines'))}
    ${sw('nLife', 'groups', t('生活・お知らせ', 'Life & notices'))}
  </ul></div>
  <div class="card" style="margin:16px 16px 0"><ul class="list">
    ${FM ? '' : `<li><button class="list-item ux-row" data-tap data-go="learning" ${param({user: ME})}>${ic('book', 'lead')}<div class="grow">My learning</div>${ic('next', 'lead')}</button></li>`}
    <li><button class="list-item ux-row" data-tap data-ux-act="replay">${ic('replay', 'lead')}<div class="grow">${t('オンボーディングをもう一度', 'Replay onboarding')}</div>${ic('next', 'lead')}</button></li>
  </ul></div>`;

let offT = 0;
function showOffline(on, quiet) {
  const d = $('#device'), b = $('#ux-off'), tx = $('#ux-off-t'); if (!d || !b) return;
  clearTimeout(offT);
  // shortcut: visual only, nothing is actually queued; wire to navigator.onLine + a send queue when the real app exists
  if (on) { b.dataset.k = 'off'; tx.textContent = t('オフライン · オンラインに戻るとメッセージを送信します', 'Offline · messages will send when back online'); d.classList.add('ux-offline'); }
  else if (d.classList.contains('ux-offline') && !quiet) {
    b.dataset.k = 'ok'; tx.textContent = t('オンラインに戻りました · 2件送信しました', 'Back online · 2 sent');
    offT = setTimeout(() => d.classList.remove('ux-offline'), 2400);
  } else d.classList.remove('ux-offline');
}
document.addEventListener('change', e => {
  const i = e.target.closest('[data-ux-set]'); if (!i) return;
  const k = i.dataset.uxSet; S[k] = i.checked; save();
  if (k === 'dark' || k === 'largeText') applySettings();
  if (k === 'offline') showOffline(S.offline);
});

/* ---------- global click delegation for E ---------- */
document.addEventListener('click', e => {
  let el;
  if ((el = e.target.closest('[data-ux-n]'))) { const n = DATA.notifications.find(x => x.id === el.dataset.uxN); if (n) openNotif(n); return; }
  if ((el = e.target.closest('[data-ux-h]'))) { const n = DATA.notifications.find(x => x.id === el.dataset.uxH); $('#ux-heads').classList.remove('on'); if (n) openNotif(n); return; }
  if (e.target.closest('.ux-markall')) { const ids = DATA.notifications.filter(n => !n.read).map(n => n.id); if (ids.length) Bus.emit('notify:read', {ids, role: ROLE}); return; }
  if (e.target.closest('[data-ux-act="replay"]')) { obReset(); go('onboarding'); return; }
});

/* ===================================================================
   Onboarding (full screen, modal): worker 3 steps, foreman 2 steps
   =================================================================== */
const OB = {step: 0, joined: false, scanning: false, ok: false, pick: -1, mic: false};
const OB_N = FM ? 2 : 3;
function obReset() { Object.assign(OB, {step: 0, joined: false, scanning: false, ok: false, pick: -1, mic: false}); }
const LANGS = [['en', 'English', 'English'], ['vi', 'Tiếng Việt', 'Vietnamese'], ['id', 'Bahasa Indonesia', 'Indonesian'], ['bn', 'বাংলা', 'Bengali']];
const confetti = () => Array.from({length: 22}, (_, i) => `<i style="--x:${((i * 37) % 100) - 50}px;--y:${-70 - (i * 53) % 90}px;--r:${(i * 83) % 360}deg;--d:${(i % 6) * 40}ms;--c:${i % 4}"></i>`).join('');
function obBody() {
  if (FM) {
    if (OB.step === 0) return `<div class="ux-ob-ico">${ic('groups')}</div><h2>チームコード</h2><p>作業者はこのQRかコードでチームに参加します。</p>
      <div class="ux-qrbox">${B().qrSvg ? B().qrSvg('team-line2') : ''}</div><div class="ux-code">PRESS-2F-4821</div>
      <button class="btn tonal ux-ob-sec" data-ux-ob="copy">${ic('copy')}コードをコピー</button>`;
    return `<div class="ux-ob-ico">${ic('mic')}</div><h2>マイクを使います</h2><p>話すだけで指示を送れます。話した内容は文字になり、作業者の言語に翻訳されます。</p>
      <ul class="ux-ob-pts"><li>${ic('tick')}マイクはメッセージを録音するときだけ使います</li><li>${ic('tick')}送信前に内容を確認・修正できます</li><li>${ic('tick')}設定からいつでも変更できます</li></ul>
      ${OB.mic ? `<div class="ux-granted">${ic('check')}マイクを許可しました</div>` : `<button class="btn tonal ux-ob-sec" data-ux-ob="mic">${ic('mic')}マイクを許可</button>`}`;
  }
  if (OB.step === 0) return `<div class="ux-ob-ico">${ic('qr')}</div><h2>Join your team</h2><p>Scan the team QR code your Foreman shows you.</p>
    ${OB.joined ? `<div class="ux-joined"><span class="ux-jc">${ic('check')}</span><div><b>Line 2 · Press team</b><span>Joined as Rahim Uddin</span></div></div>
      <div class="ux-jav">${['u_tanaka', 'u_nguyen', 'u_siti'].map(u => av(u)).join('')}<span>+ you</span></div>`
    : `<button class="ux-vf${OB.scanning ? ' scanning' : ''}" data-ux-ob="scan" aria-label="Scan team QR"><i></i><i></i><i></i><i></i><span class="ux-scanline"></span><span class="ux-vf-t">${OB.scanning ? 'Scanning…' : 'Tap to scan'}</span></button>`}`;
  if (OB.step === 1) return `<div class="ux-ob-ico">${ic('globe')}</div><h2>Choose your language</h2><p>Messages are translated for you.</p>
    <div class="ux-langs">${LANGS.map(([c, n, en]) => `<button class="ux-lang${c === 'en' ? ' on' : ''}" data-ux-ob="lang" data-c="${c}"><span><b>${n}</b><small>${en}</small></span><span class="ux-radio"></span></button>`).join('')}</div>`;
  if (OB.ok) return `<div class="ux-cele"><div class="ux-confetti">${confetti()}</div><div class="ux-cbig">${ic('check')}</div><h2>You're ready!</h2><p>You can read Notes, take Checks and get signed off on machines.</p></div>`;
  return `<div class="ux-ob-ico">${ic('help')}</div><h2>Try a Check</h2><p>A Check is a short question that shows you understood. This one is easy.</p>
    <div class="ux-q"><b>Which one protects your head at work?</b>${['Helmet', 'Gloves', 'Boots'].map((o, i) => `<button class="ux-opt${OB.pick === i ? ' bad' : ''}" data-ux-ob="ans" data-i="${i}">${o}</button>`).join('')}
    ${OB.pick >= 0 ? '<div class="ux-hint">Not quite. Try again.</div>' : ''}</div>`;
}
function renderOb(el, anim) {
  const last = OB.step === OB_N - 1;
  const canNext = FM ? true : OB.step === 0 ? OB.joined : OB.step === 1 ? true : OB.ok;
  el.innerHTML = `<div class="ux-ob"><header class="ux-obh">${OB.step ? `<button class="icon-btn" data-ux-ob="prev" aria-label="${t('戻る', 'Back')}">${ic('back')}</button>` : '<span style="width:48px"></span>'}
      <div class="ux-dots">${Array.from({length: OB_N}, (_, i) => `<i class="${i <= OB.step ? 'on' : ''}"></i>`).join('')}</div>
      <button class="btn text" data-ux-ob="skip">${t('スキップ', 'Skip')}</button></header>
    <div class="ux-obb${anim ? ' slide' : ''}">${obBody()}</div>
    <footer class="ux-obf"><button class="btn block ux-next" data-ux-ob="next" ${canNext ? '' : 'disabled'}>${last ? t('はじめる', 'Start') : t('次へ', 'Next')}</button></footer></div>`;
}
function obDone() { try { localStorage.setItem('wk.ux.onboarded', '1'); } catch (e) {} App.back(); }
function obClick(el, e) {
  const b = e.target.closest('[data-ux-ob]'); if (!b || b.disabled) return;
  const a = b.dataset.uxOb;
  if (a === 'skip') return obDone();
  if (a === 'prev') { OB.step--; return renderOb(el, true); }
  if (a === 'next') { if (OB.step === OB_N - 1) return obDone(); OB.step++; return renderOb(el, true); }
  if (a === 'scan') { // shortcut: fake QR camera, any tap joins the team; use getUserMedia + a QR decoder in the real app
    OB.scanning = true; renderOb(el, false);
    setTimeout(() => { OB.scanning = false; OB.joined = true; if (isOn('onboarding')) renderOb(el, false); }, 1300); return;
  }
  if (a === 'lang') { if (b.dataset.c !== 'en') App.toast('Coming soon'); return; }
  if (a === 'ans') { const i = +b.dataset.i; if (i === 0) { OB.ok = true; OB.pick = -1; } else OB.pick = i; return renderOb(el, false); }
  if (a === 'copy') { try { navigator.clipboard.writeText('PRESS-2F-4821'); } catch (e2) {} return App.toast('コピーしました'); }
  if (a === 'mic') { OB.mic = true; return renderOb(el, false); }   // shortcut: fake permission, call getUserMedia({audio:true}) in the real app
}

/* ===================================================================
   Value story card: one instruction, three winners
   =================================================================== */
function loopStats() {
  const fu = FU(), flagged = fu.length, explained = fu.filter(f => f.status !== 'open').length, understood = fu.filter(f => f.status === 'understood').length;
  return {fu, flagged, explained, understood, open: flagged - explained};
}
function valueCard() {
  const s = loopStats(), rec = (DATA.signoffs || []).length + s.understood;
  const col = (i, who, line, m) => `<div class="ux-vc"><span class="ux-vi">${ic(i)}</span><small>${who}</small><b>${m}</b><span>${line}</span></div>`;
  return `<div class="ux-value"><div class="ux-vh">${t('1つの指示がもたらす価値', 'One instruction, three wins')}</div><div class="ux-v3">
    ${col('book', t('作業者', 'Worker'), t('言葉を理解できた', 'Learned the term'), s.understood)}
    ${col('help', t('職長', 'Supervisor'), t('穴を発見し解消', 'Gap found and fixed'), `${s.explained}/${s.flagged}`)}
    ${col('verified', t('会社', 'Employer'), t('ミス削減・研修記録', 'Fewer errors, training on record'), rec)}</div></div>`;
}

/* ===================================================================
   Analytics (foreman)
   =================================================================== */
// shortcut: historic baseline numbers are seeded; the live part comes from DATA.messages + DATA.followups. Replace with a real analytics endpoint.
const BASE = {sent: 36, ok: 28, trend: [62, 68, 74], bars: {u_rahim: [14, 3, 1], u_nguyen: [11, 5, 2], u_siti: [9, 6, 3]}};
const AN = {pt: 3, cell: null, kv: {}};
const ADDED = new Set();
let elA = null;
function anNums() {
  const {all} = liveTally(), s = loopStats(), tot = all.green + all.orange + all.red + all.wait;
  const sent = BASE.sent + tot, ok = Math.min(sent, BASE.ok + all.green), pct = Math.min(100, Math.round(100 * (ok + s.understood) / sent));
  return {sent, ok, pct, s};
}
const CH = {W: 320, H: 150, l: 34, r: 18, tp: 14, b: 28};
const cx = i => CH.l + i * (CH.W - CH.l - CH.r) / 3, cy = v => CH.tp + (100 - v) / 60 * (CH.H - CH.tp - CH.b);
function trendSvg(vals) {
  const {W, H, l, r, b} = CH, n = vals.length;
  const pts = vals.map((v, i) => [cx(i), cy(v)]), line = pts.map((p, i) => (i ? 'L' : 'M') + p[0].toFixed(1) + ' ' + p[1].toFixed(1)).join('');
  const lab = [t('3週前', '3w ago'), t('2週前', '2w ago'), t('先週', 'Last wk'), t('今週', 'This wk')];
  return `<svg viewBox="0 0 ${W} ${H}" class="ux-chart" role="img" aria-label="${t('理解度の推移', 'Understanding trend')}">
    ${[50, 70, 90].map(v => `<line class="ux-g" x1="${l}" x2="${W - r}" y1="${cy(v)}" y2="${cy(v)}"/><text class="ux-gl" x="${l - 6}" y="${cy(v) + 4}" text-anchor="end">${v}</text>`).join('')}
    <path class="ux-area" d="${line}L${cx(n - 1)} ${H - b}L${cx(0)} ${H - b}Z"/><path class="ux-ln" d="${line}"/>
    ${pts.map((p, i) => `<circle class="ux-pt" data-i="${i}" cx="${p[0]}" cy="${p[1]}" r="5"/><circle class="ux-hit" data-ux-pt="${i}" cx="${p[0]}" cy="${p[1]}" r="22"/><text class="ux-gl" x="${p[0]}" y="${H - 8}" text-anchor="middle">${lab[i]}</text>`).join('')}
  </svg><div class="ux-tip" id="ux-tip"></div>`;
}
function placeTip(el) {
  const vals = BASE.trend.concat(anNums().pct), i = Math.min(AN.pt, vals.length - 1), tip = $('#ux-tip', el); if (!tip) return;
  tip.style.left = (cx(i) / CH.W * 100) + '%'; tip.style.top = (cy(vals[i]) / CH.H * 100) + '%';
  tip.textContent = vals[i] + '%'; $$('.ux-pt', el).forEach(c => c.classList.toggle('sel', +c.dataset.i === i));
}
function countUp(node, to, dec) {
  const from = AN.kv[node.dataset.k] || 0; AN.kv[node.dataset.k] = to;
  if (from === to) { node.textContent = dec ? to.toFixed(1) : to; return; }
  const t0 = performance.now(), D = 700;
  const tick = now => { const p = Math.min(1, (now - t0) / D), v = from + (to - from) * (1 - Math.pow(1 - p, 3)); node.textContent = dec ? v.toFixed(1) : Math.round(v); if (p < 1) requestAnimationFrame(tick); };
  requestAnimationFrame(tick);
}
function vocabHtml() {
  const rows = termStats().slice(0, 5);
  return rows.map((o, i) => {
    const added = inGloss(o.term) || ADDED.has(o.term), tr = o.trend;
    return `<div class="ux-vr"><span class="ux-rank">${i + 1}</span><div class="ux-vm"><div class="ux-vt"><b>${esc(o.term)}</b><small>${esc(termEn(o.term))}</small></div>
      <div class="ux-vs"><span class="ux-avs">${o.users.map(u => av(u, 'sm')).join('')}</span><span>${t(o.users.length + '人 · ' + o.n + '回', o.users.length + ' people · ' + o.n + '×')}</span>
      <span class="ux-tr" data-d="${tr > 0 ? 'up' : tr < 0 ? 'dn' : 'eq'}">${ic(tr > 0 ? 'up' : tr < 0 ? 'down' : 'tick')}${tr > 0 ? '+' + tr : tr < 0 ? tr : '±0'}</span></div></div>
      ${added ? `<span class="hm-st" data-s="green">${t('用語集済', 'In glossary')}</span>` : `<button class="btn tonal ux-gadd" data-ux-gl="${esc(o.term)}">${t('用語集に追加・固定', 'Add &amp; pin')}</button>`}</div>`;
  }).join('') || `<div class="hm-empty"><span>${t('まだ混乱は記録されていません', 'No confusion recorded yet')}</span></div>`;
}
function heatDetail() {
  if (!AN.cell) return `<div class="ux-hint2">${t('セルをタップすると原因のメッセージを表示します', 'Tap a cell to see the messages that caused it')}</div>`;
  const [term, u] = AN.cell.split('|'), rows = CF().filter(r => r.term === term && r.user === u);
  return `<div class="ux-det"><div class="ux-dh">${av(u, 'sm')}<b>${esc(nameOf(u))}</b> · ${esc(term)} <span class="hm-st" data-s="${rows.length > 1 ? 'red' : rows.length ? 'orange' : 'green'}">${rows.length}${t('回', '×')}</span></div>
    ${rows.length ? rows.map(r => { const m = findMsg(r.msgId), th = (m && m.thread) || 't_line2'; return `<div class="ux-dr"><div class="grow"><div class="ux-dq">${esc(msgText(r.msgId) || t('(メッセージ)', '(message)'))}</div><small>${esc(ago(r.at))}</small></div>
      <button class="btn tonal ux-open" data-go="chat" ${param({thread: th, msg: r.msgId})}>${t('チャットを開く', 'Open chat')}</button></div>`; }).join('')
    : `<div class="hm-empty"><span>${t('つまずきはありません', 'No confusion here')}</span></div>`}
    ${valueCard()}</div>`;
}
function heatHtml() {
  const terms = termStats().slice(0, 6), ws = workers(), cnt = (term, u) => CF().filter(r => r.term === term && r.user === u).length;
  return `<div class="ux-hm" style="--c:${ws.length}"><span></span>${ws.map(u => `<span class="ux-hw">${av(u, 'sm')}<small>${esc(first(u))}</small></span>`).join('')}
    ${terms.map(o => `<span class="ux-ht">${esc(o.term)}</span>${ws.map(u => { const n = cnt(o.term, u); return `<button class="ux-hc${AN.cell === o.term + '|' + u ? ' sel' : ''}" data-ux-hc="${esc(o.term)}|${u}" data-n="${Math.min(n, 3)}" aria-label="${esc(o.term)} ${esc(first(u))} ${n}">${n || ''}</button>`; }).join('')}`).join('')}</div>
    <div class="ux-legend"><span><i data-n="0"></i>0</span><span><i data-n="1"></i>1</span><span><i data-n="2"></i>2</span><span><i data-n="3"></i>3+</span></div>
    <div id="ux-hdet">${heatDetail()}</div>`;
}
function renderAnalytics() {
  const el = elA; if (!el || !el.firstElementChild) return;
  const d = anNums(), lt = liveTally(), s = d.s, vals = BASE.trend.concat(d.pct), ws = workers();
  const funnel = [[t('指示', 'Sent'), d.sent, 'p'], [t('理解', 'Understood'), d.ok, 'ok'], [t('要フォロー', 'Follow-up'), s.flagged, 'bad'], [t('説明済', 'Explained'), s.explained, 'warn'], [t('理解 (再確認)', 'Re-understood'), s.understood, 'ok']];
  const fmax = Math.max(1, ...funnel.map(f => f[1]));
  const bars = ws.map(u => { const b = BASE.bars[u] || [0, 0, 0], p = lt.per[u] || {green: 0, orange: 0, red: 0}, g = b[0] + p.green, o = b[1] + p.orange, r = b[2] + p.red;
    return `<button class="ux-bar" data-go="learning" ${param({user: u})}>${av(u)}<div class="grow"><div class="ux-bn">${esc(nameOf(u))}</div><div class="hm-bar"><i data-s="green" style="flex:${g}"></i><i data-s="orange" style="flex:${o}"></i><i data-s="red" style="flex:${r}"></i></div></div><span class="ux-bc">${Math.round(100 * g / Math.max(1, g + o + r))}%</span></button>`; }).join('');
  $('.content', el).innerHTML = `<div class="pad">
    <div class="ux-kpis"><div class="card ux-kpi"><small>${t('節約した時間', 'Time saved')}</small><div><b data-k="h" class="ux-kn">0</b><span>${t('時間/週', 'h/week')}</span></div></div>
      <div class="card ux-kpi"><small>${t('手戻り防止', 'Rework avoided')}</small><div><b data-k="r" class="ux-kn">0</b><span>${t('件', 'cases')}</span></div></div></div>
    <div class="hm-h">${t('理解度の推移 (4週間)', 'Understanding rate, 4 weeks')}</div>
    <div class="card ux-chartw">${trendSvg(vals)}</div>
    <div class="hm-h">${t('ループの流れ', 'Loop funnel')}</div>
    <div class="card ux-funnel">${funnel.map(([n, v, k]) => `<div class="ux-fr"><span class="ux-fn">${n}</span><div class="ux-ft"><i data-k="${k}" style="width:${Math.max(6, v / fmax * 100)}%"></i></div><b>${v}</b></div>`).join('')}</div>
    <div class="hm-h">${t('作業者ごとの理解', 'Understanding per worker')}</div>
    <div class="card ux-bars">${bars}</div>
    <div class="hm-h">${t('つまずきやすい用語', 'Vocabulary insights')}</div>
    <div class="card ux-vocab">${vocabHtml()}</div>
    <div class="hm-h">${t('混乱ヒートマップ', 'Confusion heatmap')}</div>
    <div class="card ux-heat">${heatHtml()}</div>
    <div style="height:8px"></div></div>`;
  placeTip(el);
  countUp($('[data-k="h"]', el), +(2.4 + .4 * s.explained).toFixed(1), true);
  countUp($('[data-k="r"]', el), 3 + 2 * s.understood + (s.explained - s.understood));
}
function anClick(el, e) {
  let b;
  if ((b = e.target.closest('[data-ux-pt]'))) { AN.pt = +b.dataset.uxPt; placeTip(el); return; }
  if ((b = e.target.closest('[data-ux-hc]'))) { AN.cell = b.dataset.uxHc; $$('.ux-hc', el).forEach(c => c.classList.toggle('sel', c === b)); $('#ux-hdet', el).innerHTML = heatDetail(); return; }
  if ((b = e.target.closest('[data-ux-gl]'))) {
    const term = b.dataset.uxGl; ADDED.add(term);
    Bus.emit('glossary:add', {ja: term, term, en: termEn(term), reading: ''});   // part A owns DATA.glossary
    App.toast(t(`「${term}」を用語集に追加しました`, `Added ${term} to the glossary`)); renderAnalytics();
  }
}

/* ===================================================================
   Skill matrix (foreman): workers x machines
   =================================================================== */
// shortcut: seeded sign-off ages in days (valid 365 days); real dates come from DATA.signoffs when part C emits them
const AGE = {'u_rahim|mc1': 120, 'u_rahim|mc3': 40, 'u_nguyen|mc1': 200, 'u_nguyen|mc2': 356, 'u_nguyen|mc3': 15, 'u_siti|mc3': 60};
const VALID = 365, SOON = 14;
function cellInfo(u, m) {
  if (!m.ready[u]) return {k: 'missing'};
  const so = (DATA.signoffs || []).filter(x => x.user === u && x.machine === m.id).pop();
  let age = AGE[u + '|' + m.id] != null ? AGE[u + '|' + m.id] : 30, ver = 'v1';
  if (so) { const d = days(so.at); age = d == null ? 0 : Math.max(0, d); if (so.version) ver = String(so.version).replace(/^(\d)/, 'v$1'); }
  const left = Math.round(VALID - age), at = new Date(Date.now() - age * 864e5), exp = new Date(Date.now() + left * 864e5);
  return {k: left <= SOON ? 'expiring' : 'valid', left, at, exp, ver};
}
const dF = d => d.toLocaleDateString(FM ? 'ja-JP' : 'en-US', {year: 'numeric', month: 'short', day: 'numeric'});
const dS = d => d.toLocaleDateString(FM ? 'ja-JP' : 'en-US', {month: 'numeric', day: 'numeric'});
let elS = null;
function renderSkills() {
  const el = elS; if (!el || !el.firstElementChild) return;
  const ws = workers(), ms = DATA.machines;
  $('.content', el).innerHTML = `<div class="pad"><div class="ux-sk" style="--c:${ms.length}"><span></span>${ms.map(m => `<span class="ux-sm">${esc(mcN(m))}</span>`).join('')}
    ${ws.map(u => `<span class="ux-sw">${av(u, 'sm')}<small>${esc(first(u))}</small></span>${ms.map(m => { const c = cellInfo(u, m);
      return `<button class="ux-sc" data-k="${c.k}" data-ux-sc="${u}|${m.id}" aria-label="${esc(first(u))} ${esc(mcN(m))}">${ic(c.k === 'missing' ? 'lock' : c.k === 'expiring' ? 'hour' : 'tick')}<small>${c.k === 'missing' ? t('未', 'None') : c.k === 'expiring' ? t(`あと${c.left}日`, `${c.left}d left`) : dS(c.at)}</small></button>`; }).join('')}`).join('')}</div>
    <div class="ux-legend ux-sl"><span><i data-k="valid"></i>${t('有効', 'Valid')}</span><span><i data-k="expiring"></i>${t('14日以内に失効', 'Expiring in 14 days')}</span><span><i data-k="missing"></i>${t('未取得', 'Missing')}</span></div>
    <div class="ux-hint2">${t('セルをタップして詳細とチェックの割り当て', 'Tap a cell for details and to assign a Check')}</div></div>`;
}
function skClick(el, e) {
  const b = e.target.closest('[data-ux-sc]'); if (!b) return;
  const [u, mid] = b.dataset.uxSc.split('|'), m = mcOf(mid), c = cellInfo(u, m);
  const st = {valid: ['green', t('有効', 'Valid')], expiring: ['orange', t('まもなく失効', 'Expiring soon')], missing: ['red', t('未取得', 'Missing')]}[c.k];
  $('#ux-skill-body').innerHTML = `<div class="ux-sh"><div class="ux-shh">${av(u)}<div class="grow"><b>${esc(nameOf(u))}</b><div class="sub">${esc(mcN(m))}</div></div><span class="hm-st" data-s="${st[0]}">${st[1]}</span></div>
    <div class="card"><div class="list-item"><div class="grow"><div class="sub">${t('サインオフ日', 'Sign-off date')}</div><div>${c.at ? dF(c.at) : t('なし', 'None')}</div></div></div>
      <div class="list-item"><div class="grow"><div class="sub">${t('バージョン', 'Version')}</div><div>${c.ver || '-'}</div></div></div>
      <div class="list-item"><div class="grow"><div class="sub">${t('有効期限', 'Expires')}</div><div>${c.exp ? dF(c.exp) : '-'}</div></div></div></div>
    <button class="btn block ux-assign" data-ux-assign="${u}|${mid}">${t('チェックを割り当て', 'Assign Check')}</button>
    <button class="btn outlined block ux-assign" data-go="learning" ${param({user: u})} data-ux-close>${t('学習履歴を見る', 'View learning history')}</button></div>`;
  App.open('ux-skill');
}
document.addEventListener('click', e => {
  if (e.target.closest('[data-ux-close]')) { App.close(); return; }
  const b = e.target.closest('[data-ux-assign]'); if (!b) return;
  const [u, mid] = b.dataset.uxAssign.split('|'), m = mcOf(mid);
  // shortcut: only Rahim has a phone; Nguyen and Siti are simulated, so their Check is toast-only
  if (u === 'u_rahim') notify({to: 'worker', title: 'New Check: ' + m.name, body: 'Tanaka assigned you a Check. Read the Note, then answer.', push: ['note', {machine: mid}]});
  App.close(); setTimeout(() => App.toast(t(`${first(u)}さんにチェックを割り当てました`, 'Check assigned')), 250);
});

/* ===================================================================
   Follow-up inbox (foreman): data from part A (DATA.followups), read-only
   =================================================================== */
const FUS = {f: 'open'};
let elF = null;
function renderFollowups() {
  const el = elF; if (!el || !el.firstElementChild) return;
  const fu = FU(), cnt = k => fu.filter(f => f.status === k).length, list = fu.filter(f => f.status === FUS.f);
  $('.content', el).innerHTML = `<div class="pad"><div class="ux-chips" role="tablist">${['open', 'clarified', 'understood'].map(k => `<button class="ux-fc${FUS.f === k ? ' on' : ''}" data-ux-f="${k}" role="tab" aria-selected="${FUS.f === k}">${FU_ST[k][0]}<b>${cnt(k)}</b></button>`).join('')}</div>
    <div class="ux-fl">${list.length ? list.map(f => `<button class="ux-fi" data-go="chat" ${param({thread: f.thread, msg: f.msgId})}>${av(f.user)}
      <div class="ux-fm"><div class="ux-ft2"><b>${esc(f.term)}</b><small>${esc(termEn(f.term))}</small></div><div class="ux-fq">${esc(f.question || '')}</div>
      ${f.wrong ? `<span class="ux-wr">${t('誤答', 'Wrong')}: ${esc(f.wrong)}</span>` : ''}</div>
      <div class="ux-fr2"><span class="hm-st" data-s="${f.status === 'open' ? 'red' : f.status === 'clarified' ? 'orange' : 'green'}">${FU_ST[f.status] ? FU_ST[f.status][0] : esc(f.status)}</span><small>${esc(ago(f.at))}</small></div></button>`).join('')
      : `<div class="ux-empty">${ic('task')}<b>${FUS.f === 'open' ? '要フォローはありません' : 'まだありません'}</b><span>${FUS.f === 'open' ? 'みんな理解できています' : 'ここに履歴が表示されます'}</span></div>`}</div>
    ${valueCard()}</div>`;
}

/* ===================================================================
   Learning history (worker: own, foreman: param {user})
   =================================================================== */
// shortcut: streak is seeded per worker; derive it from daily Check activity when the real log exists
const STREAK = {u_rahim: 5, u_nguyen: 7, u_siti: 2};
const LR = {user: ME};
let elL = null;
function learnData(u) {
  const terms = {};
  CF().filter(r => r.user === u).forEach((r, i) => { const o = terms[r.term] = terms[r.term] || {term: r.term, missed: 0, i, status: 'understood', msgId: r.msgId, at: r.at}; o.missed++; o.i = i; o.msgId = r.msgId; o.at = r.at; });
  FU().filter(f => f.user === u).forEach((f, i) => { const o = terms[f.term] = terms[f.term] || {term: f.term, missed: 0, i: 100 + i, status: 'understood', msgId: f.msgId, at: f.at};
    o.status = f.status; o.thread = f.thread; o.msgId = f.msgId; o.question = f.question; o.at = f.at; o.i = Math.max(o.i, 100 + i); o.missed = Math.max(o.missed, 1); });
  const all = Object.values(terms).sort((a, b) => b.i - a.i);
  return {all, gaps: all.filter(o => o.status !== 'understood'), mastered: all.filter(o => o.status === 'understood').length};
}
function renderLearning() {
  const el = elL; if (!el || !el.firstElementChild) return;
  const u = LR.user, d = learnData(u), thr = o => o.thread || ((findMsg(o.msgId) || {}).thread) || 't_line2';
  const st = o => o.status === 'understood' ? ['green', t('理解', 'Understood')] : o.status === 'clarified' ? ['orange', t('説明済', 'Explained')] : ['red', t('要フォロー', 'Gap')];
  const row = o => { const [k, lbl] = st(o); return `<li class="ux-tl" data-s="${k}"><i class="ux-td"></i><div class="ux-tb"><div class="ux-tt"><b>${esc(o.term)}</b><small>${esc(termEn(o.term))}</small><span class="hm-st" data-s="${k}">${lbl}</span></div>
    <div class="ux-ts">${o.missed ? t(`${o.missed}回つまずきました`, `Missed ${o.missed}×`) : ''}${o.at != null && ago(o.at) ? ' · ' + esc(ago(o.at)) : ''}</div>
    ${o.status !== 'understood' ? `<button class="btn tonal ux-open" data-go="chat" ${param({thread: thr(o), msg: o.msgId})}>${t('復習', 'Review')}</button>` : ''}</div></li>`; };
  $('.content', el).innerHTML = `<div class="pad">
    ${FM ? `<div class="ux-lwho">${av(u, 'md')}<b>${esc(nameOf(u))}</b></div>` : ''}
    <div class="ux-lstats"><div class="card"><span class="ux-vi">${ic('verified')}</span><b>${d.mastered}</b><small>${t('習得した言葉', 'Words I mastered')}</small></div>
      <div class="card"><span class="ux-vi ux-gp">${ic('help')}</span><b>${d.gaps.length}</b><small>${t('いまの課題', 'Current gaps')}</small></div>
      <div class="card"><span class="ux-vi ux-fire">${ic('fire')}</span><b>${STREAK[u] || 0}</b><small>${t('連続日数', 'Day streak')}</small></div></div>
    <div class="hm-h">${t('いまの課題', 'Current gaps')}</div>
    ${d.gaps.length ? `<ul class="ux-tll">${d.gaps.map(row).join('')}</ul>` : `<div class="card"><div class="hm-empty">${ic('check', 'hm-ci')}<span>${t('課題はありません', 'No gaps right now')}</span></div></div>`}
    <div class="hm-h">${t('学習の記録', 'Timeline')}</div>
    ${d.all.length ? `<ul class="ux-tll">${d.all.map(row).join('')}</ul>` : `<div class="card"><div class="hm-empty"><span>${t('まだ記録がありません', 'Nothing here yet')}</span></div></div>`}
    </div>`;
}

/* ===================================================================
   Screens
   =================================================================== */
const hdr = (title, right) => `<header class="appbar"><button class="icon-btn" data-back aria-label="${t('戻る', 'Back')}">${ic('back')}</button><h1>${title}</h1>${right || ''}</header><div class="content"></div>`;
const once = (el, fn) => { if (!el.dataset.uxReady) { el.dataset.uxReady = 1; fn(); } };
Object.assign(SCREENS, {
  notifications(el) {
    elN = el;
    once(el, () => { el.innerHTML = hdr(t('通知', 'Notifications'), `<button class="btn text ux-markall">${ic('all')}${t('すべて既読', 'Mark all read')}</button>`); });
    renderNotifs();
  },
  onboarding(el) {
    obReset(); renderOb(el, false);
    once(el, () => el.addEventListener('click', e => obClick(el, e)));
  },
  analytics(el) {
    elA = el;
    once(el, () => { el.innerHTML = hdr(t('理解度分析', 'Analytics')); el.addEventListener('click', e => anClick(el, e)); });
    AN.kv = {}; renderAnalytics();
  },
  skills(el) {
    elS = el;
    once(el, () => { el.innerHTML = hdr(t('力量表', 'Skill Matrix')); el.addEventListener('click', e => skClick(el, e)); });
    renderSkills();
  },
  followups(el) {
    elF = el;
    once(el, () => { el.innerHTML = hdr(t('フォローアップ', 'Follow-ups')); el.addEventListener('click', e => { const b = e.target.closest('[data-ux-f]'); if (b) { FUS.f = b.dataset.uxF; renderFollowups(); } }); });
    renderFollowups();
  },
  learning(el, p) {
    elL = el; LR.user = (p && p.user) || (FM ? workers()[0] : ME);
    once(el, () => { el.innerHTML = hdr(''); });
    $('h1', el).textContent = FM ? t(first(LR.user) + 'さんの学習履歴', '') : 'My learning';
    renderLearning();
  },
});

/* ---------- loop events: keep every E screen and the bell live ---------- */
const FSEEN = new Set();
function refreshLoop() { renderAnalytics(); renderFollowups(); renderLearning(); if (window.__wkB && window.__wkB.refresh) window.__wkB.refresh(); }
Bus.on('followup:new', f => {
  later(refreshLoop);
  // notify once, on the foreman phone only (the event also reaches the worker phone)
  if (FM && f && !FSEEN.has(f.id)) { FSEEN.add(f.id); later(() => notify({to: 'foreman', title: `${first(f.user)}さんが「${f.term}」を理解していません`, body: f.question || '要フォローです', push: ['followups']}), 0); }
});
['followup:resolve', 'check:answer', 'wk:stage', 'signoff', 'glossary:add'].forEach(ev => Bus.on(ev, () => later(refreshLoop)));
window.uxBoot = () => { badges(); if (S.offline) showOffline(true, true); };
window.uxOpenOnboarding = () => { obReset(); go('onboarding'); };
})();
