/* ===== Part C: safety (prefix sf-) =====
   Machine Lock/Ready via QR (scan, panel, note + Check), Near-miss Report (nearmiss, nearmiss-list, nearmiss-detail),
   Morning Briefing (briefing, briefing-board, briefing-card).
   Runs BEFORE App exists: App is only touched inside event handlers and timers. */
(() => {
'use strict';
const FM = ROLE === 'foreman';
const t = (ja, en) => FM ? ja : en;
const $ = (s, r = document) => r.querySelector(s);
const $$ = (s, r = document) => [...r.querySelectorAll(s)];
const esc = s => String(s == null ? '' : s).replace(/[&<>"']/g, c => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c]));
const scr = id => $(`.screen[data-screen="${id}"]`);
const topId = () => { try { return App.stack[App.stack.length - 1]; } catch (e) { return null; } };
const param = o => `data-param='${JSON.stringify(o)}'`;
const WK = ['u_rahim', 'u_nguyen', 'u_siti'];
const mc = id => DATA.machines.find(m => m.id === id);
const mcName = m => m ? (FM ? m.ja : m.name) : '';
const fn = id => { const u = DATA.users[id]; return u ? String((FM ? u.name : (u.en || u.name))).split(' ')[0] : ''; };
const hhmm = () => new Date().toTimeString().slice(0, 5);
const ago = h => new Date(Date.now() - h * 36e5).toISOString();
const upsert = (arr, o, key = 'id') => { const i = arr.findIndex(x => x[key] === o[key]); if (i < 0) arr.push(o); else arr[i] = o; return i < 0; };
function fmtAt(at) {
  const d = new Date(at); if (isNaN(d)) return String(at || '');
  const hm = d.toTimeString().slice(0, 5), day = x => x.toDateString();
  if (day(d) === day(new Date())) return t('今日 ', 'Today ') + hm;
  if (day(d) === day(new Date(Date.now() - 864e5))) return t('昨日 ', 'Yesterday ') + hm;
  return d.toLocaleDateString(FM ? 'ja-JP' : 'en-US', {month: 'short', day: 'numeric'}) + ' ' + hm;
}

/* ---------- icons (Material 24px paths; ppe/pinch drawn by hand) ---------- */
const P = {
  back: 'M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z',
  close: 'M19 6.41 17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z',
  check: 'M9 16.17 4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z',
  okc: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z',
  warn: 'M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z',
  mic: 'M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3zm5.3-3c0 3-2.54 5.1-5.3 5.1S6.7 14 6.7 11H5c0 3.41 2.72 6.23 6 6.72V21h2v-3.28c3.28-.48 6-3.3 6-6.72h-1.7z',
  flash: 'M7 2v11h3v9l7-12h-4l4-8z',
  flashOff: 'M3.27 3 2 4.27l5 5V13h3v9l3.58-6.14L17.73 20 19 18.73 3.27 3zM17 10h-4l4-8H7v2.18l8.46 8.46L17 10z',
  send: 'M2.01 21 23 12 2.01 3 2 10l15 2-15 2z',
  cam: 'M9 2 7.17 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2h-3.17L15 2H9zm3 15c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8.2a3.2 3.2 0 1 0 0 6.4 3.2 3.2 0 0 0 0-6.4z',
  vol: 'M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z',
  retry: 'M17.65 6.35A7.958 7.958 0 0 0 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08A5.99 5.99 0 0 1 12 18c-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z',
  task: 'M19 3h-4.18C14.4 1.84 13.3 1 12 1s-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm-2 14-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z',
  next: 'M10 6 8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z',
  eye: 'M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z',
  bell: 'M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z',
  camp: 'M18 11v2h4v-2h-4zm-2 6.61c.96.71 2.21 1.65 3.2 2.39.4-.53.8-1.07 1.2-1.6-.99-.74-2.24-1.68-3.2-2.4-.4.54-.8 1.08-1.2 1.61zM20.4 5.6c-.4-.53-.8-1.07-1.2-1.6-.99.74-2.24 1.68-3.2 2.4.4.53.8 1.07 1.2 1.6.96-.72 2.21-1.65 3.2-2.4zM4 9c-1.1 0-2 .9-2 2v2c0 1.1.9 2 2 2h1v4h2v-4h1l5 3V6L8 9H4zm11.5 3c0-1.33-.58-2.53-1.5-3.35v6.69c.92-.81 1.5-2.01 1.5-3.34z',
  lock: 'M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z',
  ppe: 'M2 16h20v3H2v-3zm2-1a8 8 0 0 1 16 0H4zm5.5-8h5v8h-5V7z',
  hot: 'M13.5.67s.74 2.65.74 4.8c0 2.06-1.35 3.73-3.41 3.73-2.07 0-3.63-1.67-3.63-3.73l.03-.36C5.21 7.51 4 10.62 4 14c0 4.42 3.58 8 8 8s8-3.58 8-8C20 8.61 17.41 3.8 13.5.67zM11.71 19c-1.78 0-3.22-1.4-3.22-3.14 0-1.62 1.05-2.76 2.81-3.12 1.77-.36 3.6-1.21 4.62-2.58.39 1.29.59 2.65.59 4.04 0 2.65-2.15 4.8-4.8 4.8z',
  pinch: 'M2 4h2v16H2V4zm18 0h2v16h-2V4zM6 8l5 4-5 4V8zm12 0v8l-5-4 5-4z',
};
const svg = (k, cls = '') => `<svg class="sf-ic ${cls}" viewBox="0 0 24 24" aria-hidden="true"><path d="${P[k]}"/></svg>`;
const backBtn = `<button class="icon-btn" data-back aria-label="${t('戻る', 'Back')}">${svg('back')}</button>`;
const bar = (title, left = backBtn, right = '') => `<header class="appbar">${left}<h1>${title}</h1>${right}</header>`;
const chip = (s, txt) => `<span class="sf-st" data-s="${s}">${txt}</span>`;
const ST_JA = {green: 'わかった', orange: '要確認', red: '未理解', wait: '未回答'};
const stTxt = s => FM ? ST_JA[s] : (s === 'wait' ? 'Waiting' : STATUS[s]);
const avatar = (u, cls = '') => `<span class="sf-av ${cls}" style="background:${DATA.users[u].color}">${esc(DATA.users[u].initials)}</span>`;
const wave = n => `<span class="sf-wave" aria-hidden="true">${Array.from({length: n || 18}, (_, i) => `<i style="--d:${(i * 83) % 600}ms;--h:${8 + (i * 7) % 5 * 5}px"></i>`).join('')}</span>`;
const S = {pn: null, hold: false, scanBusy: false, say: 0};

/* ---------- seed data (consistent with DATA.machines[].ready) ---------- */
DATA.confusions = DATA.confusions || [];
DATA.notes = DATA.notes || {
  mc1: {version: 'v2', updated: '2026-11-09', by: 'u_tanaka', icons: ['ppe', 'lock', 'hot'],
    stepsEn: ['Wear safety glasses. Do not wear gloves near the spinning chuck.', 'Stop the spindle and lock out the power before you change a tool.', 'Metal chips are hot and sharp. Use the hook, never your hands.'],
    stepsJa: ['保護メガネを着用する。回転中のチャック付近では手袋をしない。', '工具交換の前に、主軸を止めて電源をロックアウトする。', '切粉は高温で鋭い。手ではなくフックを使う。'],
    backJa: ['保護眼鏡をつけてください。回っているチャックの近くでは手袋をつけないでください。', '工具を替える前に、スピンドルを止めて電源をロックしてください。', '金属の切りくずは熱くて鋭いです。手ではなくフックを使ってください。'],
    questions: [
      {q: 'What do you use to remove metal chips?', qJa: '切粉を取るときに何を使いますか？', options: ['The hook', 'Your gloves', 'Your hands'], optionsJa: ['フック', '手袋', '素手'], answer: 0, step: 2, term: '切粉'},
      {q: 'Can you wear gloves near the spinning chuck?', qJa: '回転中のチャックの近くで手袋をしてよいですか？', options: ['Yes, always', 'No, never', 'Only thin ones'], optionsJa: ['はい、いつも', 'いいえ、してはいけない', '薄いものだけ'], answer: 1, step: 0, term: '手袋'},
      {q: 'Before you change a tool, you…', qJa: '工具を交換する前に、何をしますか？', options: ['Stop the spindle and lock out', 'Slow the spindle down', 'Ask a friend to watch'], optionsJa: ['主軸を止めてロックアウト', '主軸の回転を落とす', '友達に見てもらう'], answer: 0, step: 1, term: 'ロックアウト'},
    ]},
  mc2: {version: 'v3', updated: '2026-11-14', by: 'u_tanaka', icons: ['ppe', 'lock', 'hot', 'pinch'],
    stepsEn: ['Wear safety shoes, gloves and safety glasses.', 'Lock out the power and put your own lock on the switch before you reach into the press.', 'The die is hot after running. Do not touch it for 10 minutes.', 'Keep your hands out of the pinch point between the upper and lower die.'],
    stepsJa: ['安全靴・手袋・保護メガネを着用する。', 'プレスに手を入れる前に、電源をロックアウトし、自分の錠をスイッチに掛ける。', '運転後の金型は高温。10分間は触らない。', '上型と下型の間の挟まれ点に手を入れない。'],
    backJa: ['安全靴、手袋、保護眼鏡を身につけてください。', 'プレスに手を伸ばす前に、電源を切って、スイッチに自分のロックを付けてください。', '金型は運転後に熱くなります。10分間は触れないでください。', '上と下の金型の間の挟み込み箇所から手を離しておいてください。'],
    questions: [
      {q: 'What do you do before you reach into the press?', qJa: 'プレスに手を入れる前に、何をしますか？', options: ['Lock out the power', 'Wipe the die', 'Turn the speed up'], optionsJa: ['電源をロックアウトする', '金型を拭く', '速度を上げる'], answer: 0, step: 1, term: 'ロックアウト'},
      {q: 'The die just finished running. What do you do?', qJa: '金型が運転を終えたところです。どうしますか？', options: ['Touch it to check', 'Wait 10 minutes', 'Pour water on it'], optionsJa: ['触って確かめる', '10分待つ', '水をかける'], answer: 1, step: 2, term: '金型'},
      {q: 'Where must your hands stay away from?', qJa: '手を近づけてはいけない場所はどこですか？', options: ['The control panel', 'The pinch point between the dies', 'The oil can'], optionsJa: ['操作盤', '金型の間の挟まれ点', 'オイル缶'], answer: 1, step: 3, term: '挟まれ'},
    ]},
  mc3: {version: 'v4', updated: '2026-11-18', by: 'u_tanaka', icons: ['hot', 'pinch', 'ppe'],
    stepsEn: ['The nozzle and barrel are very hot (about 200 °C). Never touch them.', 'Close the safety door before you start. Keep your hands out of the mold area.', 'When purging, wear heat-resistant gloves and a face shield.'],
    stepsJa: ['ノズルとバレルは非常に高温(約200 °C)。絶対に触らない。', '始める前に安全扉を閉める。金型エリアに手を入れない。', 'パージのときは、耐熱手袋とフェイスシールドを着用する。'],
    backJa: ['ノズルとバレルはとても熱い(約200 °C)です。決して触れないでください。', '始める前に安全ドアを閉めてください。金型の範囲に手を入れないでください。', 'パージするときは、耐熱グローブとフェイスシールドをつけてください。'],
    questions: [
      {q: 'Can you touch the nozzle?', qJa: 'ノズルに触ってもよいですか？', options: ['Yes, with bare hands', 'No, it is very hot', 'Yes, if it is quick'], optionsJa: ['はい、素手で', 'いいえ、とても高温', 'はい、すぐなら'], answer: 1, step: 0, term: 'ノズル'},
      {q: 'What must be closed before you start?', qJa: '始める前に、何を閉めますか？', options: ['The safety door', 'The window', 'The lunch box'], optionsJa: ['安全扉', '窓', '弁当箱'], answer: 0, step: 1, term: '安全扉'},
      {q: 'What do you wear when purging?', qJa: 'パージのとき、何を着用しますか？', options: ['Sandals', 'Heat-resistant gloves and a face shield', 'Nothing special'], optionsJa: ['サンダル', '耐熱手袋とフェイスシールド', '特になし'], answer: 1, step: 2, term: 'パージ'},
    ]},
};
DATA.signoffs = DATA.signoffs || [
  {user: 'u_rahim', machine: 'mc1', at: ago(70), version: 'v2'}, {user: 'u_nguyen', machine: 'mc1', at: ago(70), version: 'v2'},
  {user: 'u_nguyen', machine: 'mc2', at: ago(30), version: 'v3'},
  {user: 'u_rahim', machine: 'mc3', at: ago(20), version: 'v4'}, {user: 'u_nguyen', machine: 'mc3', at: ago(20), version: 'v4'}, {user: 'u_siti', machine: 'mc3', at: ago(19), version: 'v4'},
];
DATA.nearmisses = DATA.nearmisses || [
  {id: 'nm_s1', user: 'u_siti', machine: 'mc3', severity: 'high', status: 'open', at: ago(22),
   en: 'Hot resin splashed from the nozzle when I opened the door too early.', ja: 'ドアを早く開けすぎて、ノズルから熱い樹脂が飛び散りました。',
   photo: 'https://picsum.photos/seed/nozzle/600/400', mark: {x: 58, y: 44}},
  {id: 'nm_s2', user: 'u_nguyen', machine: 'mc1', severity: 'low', status: 'ack', ackAt: ago(40), at: ago(48),
   en: 'A metal chip fell from the tray and I almost slipped on it.', ja: '切粉がトレーから落ちていて、滑りそうになりました。',
   photo: 'https://picsum.photos/seed/chips/600/400', mark: {x: 40, y: 62}},
];
// Morning briefing demo script. shortcut: canned dictation + translation + question, swap for ASR/MT/LLM
const BR = {
  ja: 'おはようございます。今日の安全の重点は、プレスでの手の挟まれ防止です。作業の前に、必ず安全カバーが閉まっているか確認してください。変更が一つあります。2号プレスの金型交換は午後に行います。午前中は使えません。',
  en: 'Good morning. Today’s safety focus is keeping your hands from getting caught in the press. Before you start work, always check that the safety guard is closed. One change: the die on Press #2 will be changed this afternoon. You cannot use it this morning.',
  focusJa: 'プレスでの手の挟まれ防止。作業前に、安全カバーが閉まっているか必ず確認する。', focusEn: 'Keep your hands from getting caught in the press. Before work, always check the safety guard is closed.',
  changeJa: '2号プレスは金型交換のため、午前中は使用不可(午後に交換)。', changeEn: 'Press #2 cannot be used this morning. The die will be changed this afternoon.',
  q: {q: 'What must you check before you start work?', qJa: '作業を始める前に、必ず何を確認しますか？', options: ['The safety guard is closed', 'The oil is full', 'The floor is clean'], optionsJa: ['安全カバーが閉まっている', 'オイルが満タン', '床がきれい'], answer: 0},
};
DATA.briefings = DATA.briefings || [{
  id: 'b_seed', at: ago(24), ja: '昨日の朝礼: 3号機のノズルは高温です。ドアは完全に止まってから開けてください。', en: 'Yesterday’s briefing: the nozzle on Machine 3 is very hot. Open the door only after it has fully stopped.',
  focusJa: '3号機のノズルは高温。', focusEn: 'The nozzle on Machine 3 is very hot.', changeJa: 'ドアは完全に止まってから開ける。', changeEn: 'Open the door only after it has fully stopped.',
  q: {q: 'When do you open the door of Machine 3?', qJa: '3号機のドアはいつ開けますか？', options: ['After it has fully stopped', 'Any time', 'While it runs'], optionsJa: ['完全に止まってから', 'いつでも', '運転中に'], answer: 0},
  results: {u_rahim: {seen: true, status: 'green', a: 0, at: '08:03'}, u_nguyen: {seen: true, status: 'green', a: 0, at: '08:04'}, u_siti: {seen: true, status: 'orange', a: -1, at: '08:06'}},
}];

/* ---------- fake QR (deterministic per machine) ---------- */
function qrSvg(seed) {
  let s = 0; for (const c of seed) s = (s * 31 + c.charCodeAt(0)) >>> 0;
  const rnd = () => (s = (s * 1664525 + 1013904223) >>> 0) / 4294967296, n = 21;
  let r = '';
  for (let y = 0; y < n; y++) for (let x = 0; x < n; x++) {
    let dark, zone = false;
    for (const [cx, cy] of [[0, 0], [14, 0], [0, 14]]) {
      const lx = x - cx, ly = y - cy;
      if (lx >= -1 && lx <= 7 && ly >= -1 && ly <= 7) { zone = true; dark = lx >= 0 && lx <= 6 && ly >= 0 && ly <= 6 && (lx === 0 || lx === 6 || ly === 0 || ly === 6 || (lx >= 2 && lx <= 4 && ly >= 2 && ly <= 4)); }
    }
    if (!zone) dark = rnd() > .52;
    if (dark) r += `<rect x="${x + 2}" y="${y + 2}" width="1.03" height="1.03"/>`;
  }
  return `<svg viewBox="0 0 25 25" aria-hidden="true"><rect width="25" height="25" class="sf-qrbg"/><g class="sf-qrfg">${r}</g></svg>`;
}

/* =====================================================================
   1. scan
   ===================================================================== */
function renderScan(el) {
  // shortcut: fake camera (CSS viewfinder, chips simulate a QR read), swap for getUserMedia + a QR decoder
  el.innerHTML = `<div class="sf-cam">
    <div class="sf-cam-bg"></div><div class="sf-cam-torch"></div>
    <header class="sf-cam-top"><button class="icon-btn" data-back aria-label="${t('閉じる', 'Close')}">${svg('close')}</button><b>${t('機械のQRをスキャン', 'Scan the machine QR')}</b>
      <button class="icon-btn" data-act="torch" aria-pressed="false" aria-label="${t('ライト', 'Torch')}"><span class="sf-t-off">${svg('flashOff')}</span><span class="sf-t-on">${svg('flash')}</span></button></header>
    <div class="sf-vf"><i class="tl"></i><i class="tr"></i><i class="bl"></i><i class="br"></i><i class="sf-line"></i><div class="sf-qr"></div></div>
    <p class="sf-cam-msg" aria-live="polite">${t('機械のQRコードを枠に合わせてください', 'Point the camera at the QR on the machine')}</p>
    <div class="sf-cam-chips">${DATA.machines.map(m => `<button class="sf-cchip" data-act="scan" data-mid="${m.id}"><span class="sf-mini">${qrSvg(m.id)}</span><span>${esc(mcName(m))}</span></button>`).join('')}</div>
    <p class="sf-cam-tip">${t('機械をタップして、スキャンを再現します', 'Tap a machine to simulate a scan')}</p>
  </div>`;
  if (!el._sf) {
    el._sf = 1;
    el.addEventListener('click', e => {
      const b = e.target.closest('[data-act]'); if (!b) return;
      if (b.dataset.act === 'torch') { const cam = $('.sf-cam', el), on = cam.classList.toggle('sf-torch'); b.setAttribute('aria-pressed', on); }
      if (b.dataset.act === 'scan') scanTo(b.dataset.mid);
    });
  }
}
function scanTo(mid) {
  const el = scr('scan'), m = mc(mid); if (!el || !m || S.scanBusy) return;
  S.scanBusy = true;
  const vf = $('.sf-vf', el);
  $('.sf-qr', el).innerHTML = qrSvg(mid);
  vf.classList.add('sf-hit');
  $('.sf-cam-msg', el).textContent = t('読み取りました: ', 'Read: ') + mcName(m);
  setTimeout(() => {
    App.push('panel', {machine: mid});
    setTimeout(() => { // reset the camera while it is hidden behind the panel
      vf.classList.remove('sf-hit'); $('.sf-qr', el).innerHTML = '';
      $('.sf-cam-msg', el).textContent = t('機械のQRコードを枠に合わせてください', 'Point the camera at the QR on the machine');
      S.scanBusy = false;
    }, 900);
  }, 800);
}

/* =====================================================================
   2. panel: Machine Panel (Lock / Ready)
   ===================================================================== */
const lockHero = `<svg class="sf-lock" viewBox="0 0 24 24" aria-hidden="true"><g class="sf-shk"><path d="M8 11V7a4 4 0 0 1 8 0v4" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/></g><rect x="4.5" y="10.5" width="15" height="11" rx="2.5" fill="currentColor"/><circle cx="12" cy="15.2" r="1.6" class="sf-key"/><rect x="11.3" y="15.4" width="1.4" height="3.2" rx=".7" class="sf-key"/></svg>`;
const signOf = (u, mid) => DATA.signoffs.filter(s => s.user === u && s.machine === mid).sort((a, b) => String(a.at) < String(b.at) ? -1 : 1).pop();
const panelSt = m => (FM ? WK.every(u => m.ready[u]) : !!m.ready[ME]) ? 'ready' : 'lock';
function renderPanel(el, p) {
  const m = mc(p.machine || p.id) || DATA.machines[0], st = panelSt(m);
  S.pn = {mid: m.id, snap: null};
  el.innerHTML = `<div class="sf-pn" data-st="${st}" data-ic="${st}">
    <div class="sf-sweep"></div>
    <header class="sf-pn-top"><button class="icon-btn" data-back aria-label="${t('戻る', 'Back')}">${svg('back')}</button><span>${t('機械パネル', 'Machine Panel')}</span></header>
    <div class="sf-pn-hero">
      ${lockHero}
      <div class="sf-pn-lbl" aria-live="polite"><b class="sf-l-lock">${FM ? 'ロック中' : 'LOCKED'}</b><b class="sf-l-ok">${FM ? '使用可' : 'READY'}</b></div>
      <div class="sf-pn-mc">${esc(mcName(m))}</div>
      <div class="sf-pn-sub"></div>
    </div>
    <div class="sf-pn-sheet"></div>
  </div>`;
  fillPanel(el);
  if (!el._sf) {
    el._sf = 1;
    el.addEventListener('click', e => {
      const b = e.target.closest('[data-act]'); if (!b || !S.pn) return;
      const m = mc(S.pn.mid);
      if (b.dataset.act === 'remind') {
        const u = b.dataset.u;
        App.toast(`${fn(u)} ${t('にリマインドしました', 'reminded')}`);
        if (u === 'u_rahim') notify({to: 'worker', title: 'Tanaka asks you to take the Check', body: `${m.name}: take the Check to unlock`, push: ['panel', {machine: m.id}]});
      }
    });
  }
}
function fillPanel(el) {
  const m = mc(S.pn.mid), pn = $('.sf-pn', el), sub = $('.sf-pn-sub', pn), sheet = $('.sf-pn-sheet', pn);
  const n = DATA.notes[m.id], snap = S.pn.snap;
  if (FM) {
    const locked = WK.filter(u => !m.ready[u]);
    sub.textContent = locked.length ? `${WK.length - locked.length}/${WK.length} ${t('サインオフ済み', '')} · ${locked.map(fn).join('、')}` : `${WK.length}/${WK.length} サインオフ済み`;
    sheet.innerHTML = `<div class="sf-sh-h">${t('作業員ごとの状態', 'Per-worker status')}</div><ul class="sf-wl">${WK.map(u => {
      const r = m.ready[u], so = signOf(u, m.id);
      return `<li class="sf-wrow${snap && !snap[u] && r ? ' sf-pop' : ''}">${avatar(u)}<div class="grow"><b>${esc(nameOf(u))}</b><div class="sub"><span class="sf-live"></span>在席${so && r ? ` · ${esc(so.version)} · ${esc(fmtAt(so.at))}` : ' · サインオフ未取得'}</div></div>
        ${r ? chip('green', '使用可') : `<span class="sf-st sf-lk" data-s="wait">${svg('lock', 's')}ロック中</span>`}
        ${r ? '' : `<button class="btn tonal sf-rem" data-act="remind" data-u="${u}">${svg('bell', 's')}リマインド</button>`}</div></li>`;
    }).join('')}</ul>
    <button class="btn tonal block sf-big" data-push="note" ${param({machine: m.id})}>${svg('task')}ノートと確認質問を見る (${esc(n ? n.version : '')})</button>`;
  } else {
    const r = !!m.ready[ME], so = signOf(ME, m.id);
    sub.textContent = r ? `Signed off · Note ${n ? n.version : ''}` : `No sign-off for Note ${n ? n.version : ''} yet`;
    sheet.innerHTML = `<div class="sf-pres">${avatar(ME, 'lg')}<div class="grow"><b>${esc(nameOf(ME))}</b><div class="sub"><span class="sf-live"></span>Presence: at this machine</div></div>${r ? chip('green', 'Ready') : `<span class="sf-st sf-lk" data-s="wait">${svg('lock', 's')}Locked</span>`}</div>` +
      (r ? `<div class="sf-signed">${svg('okc')}<div><b>Signed off on Note ${esc(so ? so.version : (n ? n.version : ''))}</b><div class="sub">${so ? esc(fmtAt(so.at)) : ''}</div></div></div>
        <div class="sf-two"><button class="btn tonal" data-push="note" ${param({machine: m.id})}>${svg('task')}Read the Note</button><button class="btn outlined" data-push="nearmiss" ${param({machine: m.id})}>${svg('warn')}Report a near-miss</button></div>`
        : `<p class="sf-hint">You cannot use this machine until you read the Note and pass the Check.</p>
        <button class="btn block sf-big" data-push="note" ${param({machine: m.id})}>${svg('lock')}Take the Check to unlock</button>`);
  }
  S.pn.snap = Object.assign({}, m.ready);
}
function playSweep(el, to) {
  const pn = $('.sf-pn', el), sw = $('.sf-sweep', pn); if (!pn || !sw) return;
  clearTimeout(S.pn.tm);
  sw.dataset.to = to; sw.style.transition = 'none'; sw.style.transform = 'scale(0)'; sw.style.opacity = '1'; void sw.offsetWidth;
  pn.dataset.ic = to;
  sw.style.transition = 'transform .8s cubic-bezier(.2,0,0,1)'; sw.style.transform = 'scale(1)';
  S.pn.tm = setTimeout(() => { pn.dataset.st = to; sw.style.transition = 'none'; sw.style.transform = 'scale(0)'; }, 820);
}
function syncPanel(anim) {
  const el = scr('panel'); if (!el || !S.pn || !el.firstElementChild) return;
  const pn = $('.sf-pn', el), to = panelSt(mc(S.pn.mid));
  if (anim && pn.dataset.ic !== to) playSweep(el, to);
  else if (!anim) { pn.dataset.st = to; pn.dataset.ic = to; }
  fillPanel(el);
}

/* =====================================================================
   3. note: the Note + Check Questions
   ===================================================================== */
const N = {el: null, mid: 'mc2', ph: 'read', i: 0, fb: null, wrong: [], tm: 0};
const stepIc = k => `<span class="sf-sic" data-k="${k}">${svg(k)}</span>`;
function renderNote(slide) {
  const el = N.el, m = mc(N.mid), n = DATA.notes[N.mid];
  if (!n) { el.innerHTML = bar(t('ノート', 'Note')) + `<div class="content"><div class="sf-empty">${svg('task')}<span>${t('この機械のノートはまだありません', 'No Note for this machine yet')}</span></div></div>`; return; }
  const title = `${t('ノート', 'Note')} · ${esc(mcName(m))}`, signed = !!m.ready[ME], qs = n.questions, q = qs[N.i];
  let body = '', foot = '';
  if (N.ph === 'read') {
    const date = new Date(n.updated).toLocaleDateString(FM ? 'ja-JP' : 'en-US', {month: 'short', day: 'numeric'});
    body = `<div class="pad sf-stack stagger">
      <div class="sf-nh"><span class="sf-ver">${esc(n.version)}</span><div><b>${t('現場ノート', 'Safe-use Note')}</b><div class="sub">${t('更新', 'Updated')} ${esc(date)} · ${esc(fn(n.by))}</div></div></div>
      ${!FM && signed ? `<div class="sf-signed">${svg('okc')}<div><b>You are signed off on this Note</b><div class="sub">Machine is Ready for you</div></div></div>` : ''}
      <ol class="sf-steps">${n.icons.map((k, i) => FM
        ? `<li>${stepIc(k)}<div class="grow"><div class="sf-sj">${esc(n.stepsJa[i])}</div><div class="sf-en"><span>英語版</span>${esc(n.stepsEn[i])}</div><div class="sf-bt"><span>逆翻訳 (英→日)</span>${esc(n.backJa[i])}</div></div></li>`
        : `<li>${stepIc(k)}<div class="grow"><div class="sf-se">${esc(n.stepsEn[i])}</div></div></li>`).join('')}</ol>
      ${FM ? `<div class="sf-sh-h">確認質問 (${qs.length})</div><div class="card sf-qprev">${qs.map((x, i) => `<div><span class="sf-qn">Q${i + 1}</span><div><div>${esc(x.qJa)}</div><div class="sub">正解: ${esc(x.optionsJa[x.answer])}</div></div></div>`).join('')}</div>` : ''}
    </div>`;
    foot = !FM && !signed ? `<button class="btn block sf-big" data-act="start">${t('', `Start the Check · ${qs.length} questions`)}</button>` : '';
  } else if (N.ph === 'q') {
    const fb = N.fb;
    body = `<div class="pad sf-stack"><div class="sf-dots">${qs.map((x, i) => `<i class="${i < N.i ? (N.res[i] ? 'ok' : 'no') : i === N.i ? (fb ? (fb.ok ? 'ok' : 'no') : 'cur') : ''}"></i>`).join('')}</div>
      <div class="sf-q${slide ? ' sf-slide' : ''}"><div class="sf-qk">Question ${N.i + 1} of ${qs.length}</div><h2 class="sf-qt">${esc(q.q)}</h2>
      ${q.options.map((o, j) => `<button class="sf-opt${fb ? (j === q.answer ? ' ok' : j === fb.sel ? ' bad' : ' dim') : ''}" data-act="pick" data-i="${j}"${fb ? ' disabled' : ''}>${esc(o)}${fb && j === q.answer ? svg('check') : fb && j === fb.sel ? svg('close') : ''}</button>`).join('')}
      ${fb ? `<div class="sf-fb ${fb.ok ? 'ok' : 'bad'}">${svg(fb.ok ? 'okc' : 'warn')}<div><b>${fb.ok ? 'Correct' : 'Not quite'}</b>${fb.ok ? '' : `<div class="sub sf-rv">${stepIc(n.icons[q.step])}<span>${esc(n.stepsEn[q.step])}</span></div>`}</div></div>` : ''}</div></div>`;
    foot = fb ? `<button class="btn block sf-big" data-act="next">${N.i < qs.length - 1 ? 'Next question' : 'See result'}</button>` : '';
  } else if (N.ph === 'pass') {
    body = `<div class="sf-res"><div class="sf-resic ok">${svg('check')}</div><h2>Check passed</h2><p>Unlocking ${esc(mcName(m))}…</p></div>`;
  } else {
    const miss = [...new Set(N.wrong.map(i => qs[i].step))];
    body = `<div class="pad sf-stack"><div class="sf-res"><div class="sf-resic bad">${svg('warn')}</div><h2>Not yet</h2><p>${qs.length - N.wrong.length} of ${qs.length} correct. The machine stays Locked.</p></div>
      <div class="sf-sh-h">Read these steps again</div><ol class="sf-steps">${miss.map(i => `<li>${stepIc(n.icons[i])}<div class="grow"><div class="sf-se">${esc(n.stepsEn[i])}</div></div></li>`).join('')}</ol></div>`;
    foot = `<div class="sf-two"><button class="btn outlined" data-act="reread">Read the Note</button><button class="btn" data-act="retry">${svg('retry')}Retry the Check</button></div>`;
  }
  el.innerHTML = bar(title, N.ph === 'pass' ? '<span class="sf-sp"></span>' : backBtn, '') + `<div class="content">${body}</div>` + (foot ? `<div class="sf-foot">${foot}</div>` : '');
}
function wireNote(el) {
  if (el._sf) return; el._sf = 1;
  el.addEventListener('click', e => {
    const b = e.target.closest('[data-act]'); if (!b) return;
    const n = DATA.notes[N.mid], qs = n.questions, a = b.dataset.act;
    if (a === 'start' || a === 'retry') { N.ph = 'q'; N.i = 0; N.fb = null; N.wrong = []; N.res = []; renderNote(true); }
    else if (a === 'reread') { N.ph = 'read'; renderNote(); }
    else if (a === 'pick' && !N.fb) {
      const q = qs[N.i], j = +b.dataset.i, ok = j === q.answer;
      N.fb = {sel: j, ok}; N.res[N.i] = ok;
      if (!ok) {
        N.wrong.push(N.i);
        // Failing records a Confusion
        Bus.emit('confusion:new', {sfid: 'cf' + Date.now() + N.i, user: ME, term: q.term, msgId: 'note:' + N.mid, at: hhmm(), machine: N.mid});
      }
      renderNote();
    } else if (a === 'next') {
      if (N.i < qs.length - 1) { N.i++; N.fb = null; renderNote(true); }
      else { N.ph = N.wrong.length ? 'fail' : 'pass'; renderNote(); if (N.ph === 'pass') N.tm = setTimeout(finishPass, 1400); }
    }
  });
}
function finishPass() {
  const n = DATA.notes[N.mid], m = mc(N.mid), onNote = topId() === 'note';
  S.hold = onNote;   // keep the panel on Lock until we are back on it, then animate
  Bus.emit('signoff', {user: ME, machine: N.mid, at: new Date().toISOString(), version: n.version});
  notify({to: 'foreman', title: `${fn(ME)} が ${m.ja} のサインオフを取得`, body: `ノート ${n.version} · 確認質問に全問正解`, push: ['panel', {machine: N.mid}]});
  if (onNote) { App.back(); setTimeout(() => { S.hold = false; syncPanel(true); }, 560); }
}

/* =====================================================================
   4. nearmiss (worker), nearmiss-list, nearmiss-detail
   ===================================================================== */
const SEV = {low: ['軽微', 'Low'], mid: ['中程度', 'Medium'], high: ['重大', 'High']};
// shortcut: canned dictation + canned translation per machine, swap for ASR + MT
const DICT = {
  mc1: {en: 'A metal chip flew out when I opened the chuck cover. It missed my eye by a few centimeters.', ja: 'チャックのカバーを開けたとき、切粉が飛び出しました。目の数センチ横を通りました。'},
  mc2: {en: 'The safety guard was open when I reached in to clear a jam. My glove almost got caught between the dies.', ja: '詰まりを取ろうと手を入れたとき、安全カバーが開いていました。手袋が金型の間に挟まれそうになりました。'},
  mc3: {en: 'Hot resin splashed from the nozzle when I opened the door too early.', ja: 'ドアを早く開けすぎて、ノズルから熱い樹脂が飛び散りました。'},
};
const toJa = (mid, text) => { const d = DICT[mid]; return d && d.en === text ? d.ja : '(自動翻訳) ' + text; };
const photoUrl = mid => `https://picsum.photos/seed/nearmiss-${mid}/600/400`;
const NM = {el: null, mid: null, text: '', photo: false, mark: {x: 56, y: 46}, sev: 'mid', rec: false, wi: 0, tm: 0, sent: false};
const sevChip = s => `<span class="sf-sev" data-sv="${s}">${t(SEV[s][0], SEV[s][1])}</span>`;
const photoBox = (src, mark, cls = '') => `<div class="sf-photo ${cls}"><div class="sf-ph-fb">${svg('cam')}</div><img src="${esc(src)}" alt="${t('現場の写真', 'Photo of the spot')}" onload="this.classList.add('on')" onerror="this.parentNode.classList.add('err');this.remove()">${mark ? `<i class="sf-ring" style="left:${mark.x}%;top:${mark.y}%"></i>` : ''}</div>`;
const myReports = () => DATA.nearmisses.filter(x => x.user === ME).sort((a, b) => a.at < b.at ? 1 : -1);
function minesHTML() {
  const l = myReports().slice(0, 3);
  return l.length ? `<div class="sf-sh-h">My reports</div><div class="card">${l.map(x => `<button class="list-item" data-tap data-push="nearmiss-detail" ${param({id: x.id})}><div class="grow"><div class="sf-ell">${esc(x.en)}</div><div class="sub">${esc(mcName(mc(x.machine)))} · ${esc(fmtAt(x.at))}</div></div>${x.status === 'ack' ? chip('green', 'Seen by Tanaka') : chip('wait', 'Sent')}</button>`).join('')}</div>` : '';
}
function renderNM() {
  const el = NM.el;
  if (FM) {
    el.innerHTML = bar('ヒヤリハット') + `<div class="content"><div class="pad"><div class="sf-empty">${svg('warn')}<span>ヒヤリハットは作業員のスマホから届きます</span><button class="btn tonal" data-push="nearmiss-list">報告の一覧を見る</button></div></div></div>`;
    return;
  }
  if (NM.sent) {
    el.innerHTML = bar('Near-miss report', '<span class="sf-sp"></span>') + `<div class="content"><div class="sf-res"><div class="sf-resic ok">${svg('check')}</div><h2>Sent to Tanaka</h2><p>He gets a notice now. Thank you for speaking up.</p>
      <div class="sf-two"><button class="btn outlined" data-back>Done</button><button class="btn" data-act="again">Report another</button></div></div></div>`;
    return;
  }
  el.innerHTML = bar('Near-miss report', backBtn) + `<div class="content"><div class="pad sf-stack">
    <div class="sf-sh-h">1 · Which machine?</div>
    <div class="sf-chips">${DATA.machines.map(m => `<button class="chip${NM.mid === m.id ? ' on' : ''}" data-chip data-act="mc" data-mid="${m.id}">${esc(m.name)}</button>`).join('')}</div>
    <div class="sf-sh-h">2 · What happened? Hold to speak</div>
    <div class="sf-rec"><button class="sf-mic" data-act="mic" aria-label="Hold to record">${svg('mic')}</button>${wave(24)}<span class="sf-tm">0:00</span></div>
    <textarea class="sf-ta" rows="4" placeholder="Hold the mic and say what happened, or type here" aria-label="What happened">${esc(NM.text)}</textarea>
    <div class="sub">Auto-translated to Japanese for Tanaka</div>
    <div class="sf-sh-h">3 · Photo (optional)</div>
    <div class="sf-phwrap">${NM.photo ? photoBox(photoUrl(NM.mid || 'x'), NM.mark, 'tap') + `<div class="sub">Tap the photo to circle the spot</div><button class="btn text" data-act="rmphoto">Remove photo</button>` : `<button class="btn tonal sf-add" data-act="photo">${svg('cam')}Add photo</button>`}</div>
    <div class="sf-sh-h">4 · How serious?</div>
    <div class="sf-chips">${Object.keys(SEV).map(s => `<button class="chip${NM.sev === s ? ' on' : ''}" data-chip data-act="sev" data-sv="${s}"><i class="sf-sdot" data-sv="${s}"></i>${SEV[s][1]}</button>`).join('')}</div>
    <div class="sf-mine">${minesHTML()}</div>
  </div></div><div class="sf-foot"><button class="btn block sf-big sf-send" data-act="send" disabled>${svg('send')}Send to Tanaka</button></div>`;
  updSend();
}
function updSend() { const b = $('.sf-send', NM.el); if (b) b.disabled = !(NM.mid && NM.text.trim().length > 2); }
function setRec(on) {
  const el = NM.el, rec = $('.sf-rec', el); if (!rec) return;
  NM.rec = on; rec.classList.toggle('on', on);
  clearInterval(NM.tm);
  if (!on) return;
  const words = DICT[NM.mid].en.split(' '), t0 = Date.now(), base = NM.wi;
  NM.tm = setInterval(() => {
    const ta = $('.sf-ta', el); if (!ta) return clearInterval(NM.tm);
    NM.wi = Math.min(words.length, base + Math.floor((Date.now() - t0) / 230));
    ta.value = NM.text = words.slice(0, NM.wi).join(' ');
    const s = Math.floor((Date.now() - t0) / 1000); $('.sf-tm', el).textContent = '0:0' + Math.min(9, s);
    updSend();
    if (NM.wi >= words.length) setRec(false);
  }, 120);
}
function wireNM(el) {
  if (el._sf) return; el._sf = 1;
  el.addEventListener('click', e => {
    const b = e.target.closest('[data-act]'); if (!b) return;
    const a = b.dataset.act;
    if (a === 'mc') { if (NM.mid !== b.dataset.mid) { NM.mid = b.dataset.mid; NM.wi = 0; NM.text = ''; const ta = $('.sf-ta', el); if (ta) ta.value = ''; if (NM.photo) { NM.photo = false; renderNM(); } } updSend(); }
    else if (a === 'sev') NM.sev = b.dataset.sv;
    else if (a === 'photo') { NM.photo = true; renderNM(); }
    else if (a === 'rmphoto') { NM.photo = false; renderNM(); }
    else if (a === 'again') { resetNM(); renderNM(); }
    else if (a === 'send') sendNM();
  });
  el.addEventListener('input', e => { if (e.target.classList.contains('sf-ta')) { NM.text = e.target.value; updSend(); } });
  el.addEventListener('click', e => { // annotate-circle: tap the photo to move it
    const ph = e.target.closest('.sf-photo.tap'); if (!ph) return;
    const r = ph.getBoundingClientRect(), k = r.width / ph.offsetWidth || 1;
    NM.mark = {x: Math.round(((e.clientX - r.left) / k) / ph.offsetWidth * 100), y: Math.round(((e.clientY - r.top) / k) / ph.offsetHeight * 100)};
    const old = $('.sf-ring', ph), ring = old.cloneNode(); ring.style.left = NM.mark.x + '%'; ring.style.top = NM.mark.y + '%'; old.replaceWith(ring);
  });
  const mic = e => e.target.closest('.sf-mic');
  el.addEventListener('pointerdown', e => {
    const b = mic(e); if (!b) return;
    e.preventDefault();
    if (!NM.mid) { App.toast('Pick the machine first'); return; }
    if (NM.wi >= DICT[NM.mid].en.split(' ').length) { NM.wi = 0; }
    try { b.setPointerCapture(e.pointerId); } catch (x) {}
    setRec(true);
  });
  ['pointerup', 'pointercancel'].forEach(ev => el.addEventListener(ev, e => { if (NM.rec && mic(e)) setRec(false); }));
  el.addEventListener('contextmenu', e => { if (mic(e)) e.preventDefault(); });
}
function resetNM() { Object.assign(NM, {mid: null, text: '', photo: false, mark: {x: 56, y: 46}, sev: 'mid', rec: false, wi: 0, sent: false}); clearInterval(NM.tm); }
function sendNM() {
  const id = 'nm' + Date.now().toString(36), text = NM.text.trim();
  const nm = {id, user: ME, machine: NM.mid, en: text, ja: toJa(NM.mid, text), severity: NM.sev, photo: NM.photo ? photoUrl(NM.mid) : null, mark: NM.photo ? Object.assign({}, NM.mark) : null, at: new Date().toISOString(), status: 'open'};
  Bus.emit('nearmiss:new', nm);
  notify({to: 'foreman', title: `ヒヤリハット · ${mc(nm.machine).ja}`, body: `${fn(ME)}: ${nm.ja}`, push: ['nearmiss-detail', {id}]});
  NM.sent = true; renderNM();
}

/* ---- list ---- */
const L = {el: null, f: 'open'};
function renderList() {
  const el = L.el;
  el.innerHTML = bar(t('ヒヤリハット', 'Near-miss reports'), backBtn, '') + `<div class="sf-fchips"><button class="chip${L.f === 'open' ? ' on' : ''}" data-chip data-f="open">${t('未対応', 'Open')} <b class="sf-cn" data-n="open"></b></button><button class="chip${L.f === 'ack' ? ' on' : ''}" data-chip data-f="ack">${t('対応済', 'Seen')} <b class="sf-cn" data-n="ack"></b></button></div>
    <div class="content"><div class="sf-list"></div></div>${FM ? '' : `<button class="fab" data-push="nearmiss" aria-label="New report">${svg('cam')}</button>`}`;
  fillList();
}
function fillList() {
  const el = L.el; if (!el || !el.firstElementChild) return;
  const all = DATA.nearmisses.filter(x => FM || x.user === ME).sort((a, b) => a.at < b.at ? 1 : -1);
  $$('.sf-cn', el).forEach(c => c.textContent = all.filter(x => x.status === c.dataset.n).length);
  const rows = all.filter(x => x.status === L.f);
  $('.sf-list', el).innerHTML = rows.map(x => `<button class="sf-nrow" data-tap data-push="nearmiss-detail" ${param({id: x.id})}><i class="sf-bar" data-sv="${x.severity}"></i>
    <div class="grow"><div class="sf-nm-h"><b>${esc(mcName(mc(x.machine)))}</b><span>${esc(fmtAt(x.at))}</span></div><div class="sf-clamp">${esc(FM ? x.ja : x.en)}</div><div class="sub">${FM ? avatar(x.user, 'sm') + esc(nameOf(x.user)) + ' · ' : ''}${sevChip(x.severity)}</div></div>
    ${x.photo ? `<div class="sf-thumb"><img src="${esc(x.photo)}" alt="" onerror="this.remove()"></div>` : ''}</button>`).join('')
    || `<div class="sf-empty">${svg(L.f === 'open' ? 'check' : 'eye')}<span>${L.f === 'open' ? t('未対応の報告はありません', 'No open reports') : t('対応済の報告はまだありません', 'Nothing seen yet')}</span></div>`;
}
function wireList(el) {
  if (el._sf) return; el._sf = 1;
  el.addEventListener('click', e => { const c = e.target.closest('[data-f]'); if (c) { L.f = c.dataset.f; fillList(); } });
}

/* ---- detail ---- */
const D = {el: null, id: null};
const nmById = id => DATA.nearmisses.find(x => x.id === id);
function renderDetail() {
  const el = D.el, x = nmById(D.id);
  if (!x) { el.innerHTML = bar(t('ヒヤリハット', 'Near-miss')) + `<div class="content"><div class="sf-empty">${svg('warn')}<span>${t('報告が見つかりません', 'Report not found')}</span></div></div>`; return; }
  const m = mc(x.machine), ack = x.status === 'ack', hasTask = !!x.taskId;
  el.innerHTML = bar(t('ヒヤリハット', 'Near-miss'), backBtn) + `<div class="content"><div class="pad sf-stack">
    ${x.photo ? photoBox(x.photo, x.mark, 'big') : ''}
    <div class="row sf-wrapr">${sevChip(x.severity)}${ack ? chip('green', t('対応済', 'Seen by Tanaka')) : chip('orange', t('未対応', 'Sent'))}</div>
    <div class="card sf-meta"><div>${svg('lock', 's')}<span>${esc(mcName(m))}</span></div><div>${avatar(x.user, 'sm')}<span>${esc(nameOf(x.user))}</span></div><div>${svg('next', 's')}<span>${esc(fmtAt(x.at))}</span></div></div>
    ${FM ? `<div class="sf-sh-h">日本語訳</div><div class="card sf-big-t">${esc(x.ja)}</div><div class="sf-sh-h">原文 (English)</div><div class="sf-orig">${esc(x.en)}</div>`
          : `<div class="sf-sh-h">What you said</div><div class="card sf-big-t">${esc(x.en)}</div><div class="sf-sh-h">What Tanaka reads (日本語)</div><div class="sf-orig">${esc(x.ja)}</div>`}
    ${ack && x.ackAt ? `<div class="sub">${t('確認 ', 'Seen ')}${esc(fmtAt(x.ackAt))}</div>` : ''}
  </div></div>${FM ? `<div class="sf-foot sf-two"><button class="btn tonal" data-act="task"${hasTask ? ' disabled' : ''}>${svg('task')}${hasTask ? 'タスク作成済' : 'タスクにする'}</button><button class="btn" data-act="ack"${ack ? ' disabled' : ''}>${svg('eye')}${ack ? '確認済み' : '確認'}</button></div>` : ''}`;
}
function wireDetail(el) {
  if (el._sf) return; el._sf = 1;
  el.addEventListener('click', e => {
    const b = e.target.closest('[data-act]'); if (!b || b.disabled) return;
    const x = nmById(D.id); if (!x) return;
    if (b.dataset.act === 'ack') ackNM(x);
    if (b.dataset.act === 'task') {
      const m = mc(x.machine);
      Bus.emit('task:update', {task: {id: 'k_' + x.id, title: `Near-miss follow-up · ${m.name}`, ja: `ヒヤリハット対応 · ${m.ja}`, col: 'todo', assignees: ['u_tanaka'], due: 'Today', machine: x.machine, from: null, nm: x.id}});
      if (x.status === 'open') ackNM(x);
      App.toast('ボードにタスクを追加しました', {label: 'ボード', run: () => App.tab('board')});
    }
  });
}
function ackNM(x) {
  Bus.emit('nearmiss:ack', {id: x.id, by: ME, at: new Date().toISOString()});
  if (x.user === 'u_rahim') notify({to: 'worker', title: 'Tanaka saw your report', body: `${mc(x.machine).name}: ${x.en}`, push: ['nearmiss-detail', {id: x.id}]});
}
function refreshNM() {
  if (L.el && L.el.firstElementChild) fillList();
  if (D.el && D.el.firstElementChild && D.id) { const y = D.el.querySelector('.content').scrollTop; renderDetail(); D.el.querySelector('.content') && (D.el.querySelector('.content').scrollTop = y); }
  if (NM.el && !FM && !NM.sent && NM.el.firstElementChild) { const m = $('.sf-mine', NM.el); if (m) m.innerHTML = minesHTML(); }
}

/* =====================================================================
   5. briefing (foreman), briefing-board (foreman), briefing-card (worker modal)
   ===================================================================== */
const B = {el: null, ph: 'idle', n: 0, tm: 0};
function renderBriefing() {
  const el = B.el, past = DATA.briefings.slice().sort((a, b) => a.at < b.at ? 1 : -1);
  let body;
  if (B.ph === 'idle' || B.ph === 'rec') {
    body = `<div class="pad sf-stack"><div class="card sf-brec">
      <p>${B.ph === 'rec' ? '話してください…' : 'マイクをタップして、今日の朝礼を話してください'}</p>
      <button class="sf-bmic${B.ph === 'rec' ? ' on' : ''}" data-act="bmic" aria-label="${B.ph === 'rec' ? '停止' : '録音'}">${svg('mic')}</button>
      ${wave(26)}
      <div class="sf-tx" aria-live="polite"></div></div>
      ${B.ph === 'idle' ? `<div class="sf-sh-h">過去の朝礼</div><div class="card">${past.map(b => `<button class="list-item" data-tap data-push="briefing-board" ${param({id: b.id})}><div class="grow"><div class="sf-ell">${esc(b.focusJa)}</div><div class="sub">${esc(fmtAt(b.at))}</div></div>${svg('next')}</button>`).join('')}</div>` : ''}</div>`;
  } else if (B.ph === 'tr') {
    body = `<div class="pad sf-stack"><div class="card sf-tr"><span class="spinner"></span><b>英語に翻訳し、確認質問を作成中…</b></div><div class="skeleton" style="height:84px"></div><div class="skeleton" style="height:120px"></div></div>`;
  } else {
    const q = BR.q;
    body = `<div class="pad sf-stack stagger">
      <div class="sf-sh-h">話した内容 (日本語)</div><div class="card sf-ja-t">${esc(BR.ja)}</div>
      <div class="sf-sh-h">作業員に届く英語</div>
      <div class="card sf-focus"><span class="sf-tag">TODAY’S SAFETY FOCUS</span><div>${esc(BR.focusEn)}</div></div>
      <div class="card sf-change"><span class="sf-tag">ONE CHANGE</span><div>${esc(BR.changeEn)}</div></div>
      <div class="sf-sh-h">確認質問 (自動作成 1問)</div>
      <div class="card sf-bq"><div class="sf-qt2">${esc(q.q)}</div><div class="sub">${esc(q.qJa)}</div>${q.options.map((o, j) => `<div class="sf-op${j === q.answer ? ' ok' : ''}">${j === q.answer ? svg('check') : ''}<span>${esc(o)}<small>${esc(q.optionsJa[j])}</small></span></div>`).join('')}</div>
    </div>`;
  }
  el.innerHTML = bar('朝礼') + `<div class="content">${body}</div>` + (B.ph === 'ready' ? `<div class="sf-foot sf-two"><button class="btn outlined" data-act="redo">やり直す</button><button class="btn" data-act="send">${svg('send')}配信</button></div>` : '');
}
function wireBriefing(el) {
  if (el._sf) return; el._sf = 1;
  el.addEventListener('click', e => {
    const b = e.target.closest('[data-act]'); if (!b) return;
    const a = b.dataset.act;
    if (a === 'bmic') { if (B.ph === 'rec') { clearInterval(B.tm); typeTo(BR.ja.length); finishRec(); } else startRec(); }
    else if (a === 'redo') { B.ph = 'idle'; renderBriefing(); }
    else if (a === 'send') sendBriefing();
  });
}
function typeTo(n) { const tx = $('.sf-tx', B.el); if (tx) tx.textContent = BR.ja.slice(0, n); }
function startRec() {
  // shortcut: canned Japanese dictation, swap for speech recognition
  B.ph = 'rec'; renderBriefing(); B.n = 0;
  clearInterval(B.tm);
  B.tm = setInterval(() => {
    B.n += 3; typeTo(B.n);
    if (B.n >= BR.ja.length) { clearInterval(B.tm); setTimeout(finishRec, 350); }
  }, 70);
}
function finishRec() {
  if (B.ph !== 'rec') return;
  B.ph = 'tr'; renderBriefing();
  setTimeout(() => { if (B.ph === 'tr') { B.ph = 'ready'; renderBriefing(); } }, 1400);
}
function sendBriefing() {
  const id = 'b' + Date.now().toString(36);
  const b = {id, at: new Date().toISOString(), ja: BR.ja, en: BR.en, focusJa: BR.focusJa, focusEn: BR.focusEn, changeJa: BR.changeJa, changeEn: BR.changeEn, q: BR.q,
    results: {u_rahim: {seen: false, status: 'wait'}, u_nguyen: {seen: false, status: 'wait'}, u_siti: {seen: false, status: 'wait'}}};
  Bus.emit('briefing:new', b);
  notify({to: 'worker', title: 'Morning briefing from Tanaka', body: BR.focusEn, push: ['briefing-card', {id}]});
  App.push('briefing-board', {id});
  setTimeout(() => { B.ph = 'idle'; renderBriefing(); }, 700);
}

/* ---- board ---- */
const BD = {el: null, id: null, prev: {}};
const brById = id => DATA.briefings.find(x => x.id === id);
const resOf = (b, u) => (b.results = b.results || {})[u] || (b.results[u] = {seen: false, status: 'wait'});
function renderBoard() {
  const el = BD.el, b = brById(BD.id);
  if (!b) { el.innerHTML = bar('朝礼の確認') + `<div class="content"><div class="sf-empty">${svg('camp')}<span>朝礼がまだありません</span></div></div>`; return; }
  const R = 54, C = 2 * Math.PI * R, seg = C / WK.length, gap = 10, done = WK.filter(u => resOf(b, u).status !== 'wait').length;
  const cnt = {green: 0, orange: 0, red: 0, wait: 0}; WK.forEach(u => cnt[resOf(b, u).status]++);
  const ring = WK.map((u, i) => { const s = resOf(b, u).status, pop = BD.prev[u] && BD.prev[u] !== s;
    return `<g transform="rotate(${-90 + gap / 2 / C * 360 + i * 360 / WK.length} 70 70)"><circle class="sf-seg${pop ? ' sf-pop' : ''}" data-s="${s}" cx="70" cy="70" r="${R}" stroke-dasharray="${seg - gap} ${C - seg + gap}"/></g>`; }).join('');
  el.innerHTML = bar('朝礼の確認') + `<div class="content"><div class="pad sf-stack">
    <div class="sf-bsum"><div class="sf-ringw"><svg viewBox="0 0 140 140" aria-hidden="true">${ring}</svg><div class="sf-ringt"><b>${done}/${WK.length}</b><span>確認済</span></div></div>
      <div class="grow"><div class="sf-clamp"><b>${esc(b.focusJa)}</b></div><div class="sub">${esc(fmtAt(b.at))}</div>
      <div class="sf-legend">${['green', 'orange', 'red', 'wait'].filter(s => cnt[s]).map(s => `<span><i data-s="${s}"></i>${ST_JA[s]} ${cnt[s]}</span>`).join('')}</div></div></div>
    <div class="sf-sh-h">作業員ごとの出席と理解</div>
    <ul class="sf-wl">${WK.map(u => { const r = resOf(b, u), pres = r.seen || r.status !== 'wait', pop = BD.prev[u] && BD.prev[u] !== r.status;
      return `<li class="sf-wrow${pop ? ' sf-pop' : ''}">${avatar(u)}<div class="grow"><b>${esc(nameOf(u))}</b><div class="sub">${pres ? chip('green', '出席').replace('sf-st', 'sf-st sm') : chip('wait', '未出席').replace('sf-st', 'sf-st sm')}${r.status !== 'wait' ? ` ${r.a >= 0 ? esc(b.q.optionsJa[r.a]) : 'わからない'}` : ''}</div></div>${chip(r.status, ST_JA[r.status])}
        ${r.status === 'orange' || r.status === 'red' ? `<button class="btn tonal sf-rem" data-act="again" data-u="${u}">もう一度説明</button>` : ''}</li>`; }).join('')}</ul>
    <div class="sf-sh-h">確認質問</div><div class="card sf-bq"><div class="sf-qt2">${esc(b.q.qJa)}</div><div class="sub">正解: ${esc(b.q.optionsJa[b.q.answer])}</div></div>
  </div></div>`;
  WK.forEach(u => BD.prev[u] = resOf(b, u).status);
}
function wireBoard(el) {
  if (el._sf) return; el._sf = 1;
  el.addEventListener('click', e => {
    const b = e.target.closest('[data-act="again"]'); if (!b) return;
    const u = b.dataset.u; App.toast(`${fn(u)} に再説明を送りました`);
    if (u === 'u_rahim') notify({to: 'worker', title: 'Tanaka will explain the briefing again', body: brById(BD.id).focusEn, push: ['briefing-card', {id: BD.id}]});
  });
}

/* ---- worker modal card ---- */
const BC = {el: null, id: null};
function renderCard() {
  const el = BC.el, b = brById(BC.id);
  if (!b) { el.innerHTML = bar('Morning briefing', `<button class="icon-btn" data-back aria-label="Close">${svg('close')}</button>`) + `<div class="content"><div class="sf-empty">${svg('camp')}<span>No briefing yet</span></div></div>`; return; }
  const r = resOf(b, ME), answered = r.status !== 'wait', q = b.q, u = DATA.users.u_tanaka;
  el.innerHTML = `<header class="appbar"><button class="icon-btn" data-act="close" aria-label="Close">${svg('close')}</button><h1>Morning briefing · 朝礼</h1></header><div class="content"><div class="pad sf-stack">
    <div class="sf-from">${avatar('u_tanaka')}<div><b>${esc(u.en)}</b><div class="sub">${esc(fmtAt(b.at))}</div></div></div>
    <div class="card sf-focus"><span class="sf-tag">TODAY’S SAFETY FOCUS</span><div>${esc(b.focusEn)}</div></div>
    <div class="card sf-change"><span class="sf-tag">ONE CHANGE</span><div>${esc(b.changeEn)}</div></div>
    <button class="btn tonal sf-read" data-act="read" aria-pressed="false">${svg('vol')}<span class="sf-rl">Read aloud</span><span class="sf-eq" aria-hidden="true"><i></i><i></i><i></i></span></button>
    <div class="sf-sh-h">1 quick question</div>
    <div class="sf-bcq"><div class="sf-qt2">${esc(q.q)}</div>
      ${q.options.concat(['Not sure']).map((o, j) => { const idx = j === q.options.length ? -1 : j; let cls = idx === -1 ? ' ns' : '', ic = '';
        if (answered) { if (idx === q.answer) { cls += ' ok'; ic = svg('check'); } else if (idx === r.a) { cls += ' bad'; ic = svg('close'); } else cls += ' dim'; }
        return `<button class="sf-opt${cls}" data-act="ans" data-i="${idx}"${answered ? ' disabled' : ''}>${esc(o)}${ic}</button>`; }).join('')}
      ${answered ? `<div class="sf-fb ${r.status === 'green' ? 'ok' : r.status === 'orange' ? 'warn' : 'bad'}">${svg(r.status === 'green' ? 'okc' : 'warn')}<div><b>${r.status === 'green' ? 'Wakatta! You understood.' : r.status === 'orange' ? 'Tanaka will explain it again.' : 'Not quite. Tanaka will explain it again.'}</b>${r.status === 'green' ? '' : `<div class="sub">Answer: ${esc(q.options[q.answer])}</div>`}</div></div>` : ''}
    </div></div></div>${answered ? `<div class="sf-foot"><button class="btn block sf-big" data-act="close">Done</button></div>` : ''}`;
}
function wireCard(el) {
  if (el._sf) return; el._sf = 1;
  el.addEventListener('click', e => {
    const b = e.target.closest('[data-act]'); if (!b || b.disabled) return;
    const a = b.dataset.act, br = brById(BC.id); if (!br) return;
    if (a === 'close') { stopSay(); App.back(); }
    else if (a === 'read') {
      const on = b.getAttribute('aria-pressed') !== 'true'; stopSay(); b.setAttribute('aria-pressed', on);
      if (!on) return;
      // shortcut: browser speechSynthesis (silent without a voice), swap for a TTS service
      try { const u = new SpeechSynthesisUtterance(br.en); u.lang = 'en-US'; u.rate = .95; u.onend = () => b.setAttribute('aria-pressed', 'false'); speechSynthesis.speak(u); } catch (x) {}
      S.say = setTimeout(() => b.setAttribute('aria-pressed', 'false'), Math.min(30000, br.en.split(' ').length * 380));
    } else if (a === 'ans') {
      stopSay();
      const i = +b.dataset.i, status = i === br.q.answer ? 'green' : i === -1 ? 'orange' : 'red';
      Bus.emit('briefing:answer', {id: br.id, user: ME, answer: i, status, at: hhmm()});
      renderCard();
    }
  });
}
function stopSay() { clearTimeout(S.say); try { speechSynthesis.cancel(); } catch (x) {} const r = $('.sf-read', BC.el); if (r) r.setAttribute('aria-pressed', 'false'); }

/* ---- briefing events ---- */
function simulate(b) {
  // shortcut: scripted answers for the workers who are not on a phone in the demo
  const plan = [['u_nguyen', 1200, 3500, b.q.answer], ['u_siti', 2600, 6000, -1]];
  if (!Bus.paired) plan.push(['u_rahim', 4200, 9000, b.q.answer]);   // standalone: no real Rahim phone
  plan.forEach(([u, seen, ans, a]) => {
    setTimeout(() => Bus.emit('briefing:seen', {id: b.id, user: u, at: hhmm()}), seen);
    setTimeout(() => Bus.emit('briefing:answer', {id: b.id, user: u, answer: a, status: a === b.q.answer ? 'green' : a === -1 ? 'orange' : 'red', at: hhmm()}), ans);
  });
}
function openCard(id, tries = 0) {
  const done = () => topId() === 'briefing-card' && BC.id === id;
  if (done()) return;
  try { App.push('briefing-card', {id}); } catch (x) {}
  setTimeout(() => { if (tries < 8 && !done()) openCard(id, tries + 1); }, 700);   // App.push is ignored while a transition is running
}
function refreshBoard() {
  if (BD.el && BD.el.firstElementChild && BD.id) { const c = BD.el.querySelector('.content'), y = c ? c.scrollTop : 0; renderBoard(); const c2 = BD.el.querySelector('.content'); if (c2) c2.scrollTop = y; }
}

/* =====================================================================
   screens
   ===================================================================== */
Object.assign(SCREENS, {
  scan(el) { renderScan(el); },
  panel(el, p) { renderPanel(el, p || {}); },
  note(el, p) { N.el = el; N.mid = (p && (p.machine || p.id)) || 'mc2'; N.ph = 'read'; N.i = 0; N.fb = null; N.wrong = []; N.res = []; clearTimeout(N.tm); renderNote(); wireNote(el); },
  nearmiss(el, p) { NM.el = el; resetNM(); if (p && p.machine && !FM) NM.mid = p.machine; renderNM(); wireNM(el); },
  'nearmiss-list'(el) { L.el = el; L.f = 'open'; renderList(); wireList(el); },
  'nearmiss-detail'(el, p) { D.el = el; D.id = (p && p.id) || (DATA.nearmisses[0] || {}).id; renderDetail(); wireDetail(el); },
  briefing(el) { B.el = el; clearInterval(B.tm); B.ph = 'idle'; renderBriefing(); wireBriefing(el); },
  'briefing-board'(el, p) { BD.el = el; BD.prev = {}; BD.id = (p && p.id) || DATA.briefings.slice().sort((a, b) => a.at < b.at ? 1 : -1)[0].id; renderBoard(); wireBoard(el); },
  'briefing-card'(el, p) {
    BC.el = el; BC.id = (p && p.id) || DATA.briefings.slice().sort((a, b) => a.at < b.at ? 1 : -1)[0].id;
    renderCard(); wireCard(el);
    const b = brById(BC.id); if (b && !resOf(b, ME).seen) Bus.emit('briefing:seen', {id: b.id, user: ME, at: hhmm()});
  },
});

/* =====================================================================
   Bus handlers: all shared state changes happen here (idempotent)
   ===================================================================== */
Bus.on('signoff', s => {
  if (!s || !s.user || !s.machine) return;
  const rec = {user: s.user, machine: s.machine, at: s.at, version: s.version};
  const i = DATA.signoffs.findIndex(x => x.user === s.user && x.machine === s.machine);
  if (i < 0) DATA.signoffs.push(rec); else DATA.signoffs[i] = rec;
  const m = mc(s.machine); if (m) m.ready[s.user] = true;
  if (window.__wkB) { try { window.__wkB.refresh(); } catch (e) {} }   // part B re-renders Home / Machines
  if (!(S.hold && s.user === ME)) setTimeout(() => syncPanel(topId() === 'panel'), 0);
});
Bus.on('confusion:new', c => {
  if (!c || !c.user || !c.term) return;
  DATA.confusions = DATA.confusions || [];
  if (c.sfid && DATA.confusions.some(x => x.sfid === c.sfid)) return;
  DATA.confusions.push({user: c.user, term: c.term, msgId: c.msgId, at: c.at, sfid: c.sfid});
});
Bus.on('nearmiss:new', nm => {
  if (!nm || !nm.id) return;
  upsert(DATA.nearmisses, nm);
  refreshNM();
});
Bus.on('nearmiss:ack', a => {
  const x = a && nmById(a.id); if (!x) return;
  x.status = 'ack'; x.ackAt = a.at;
  refreshNM();
});
Bus.on('task:update', ({task}) => {   // a task made from a Near-miss remembers its link
  const x = task && task.nm && nmById(task.nm); if (x) { x.taskId = task.id; refreshNM(); }
});
Bus.on('briefing:new', b => {
  if (!b || !b.id) return;
  const fresh = upsert(DATA.briefings, b);
  if (!fresh) return;   // idempotent: a replay must not restart the simulation or reopen the card
  if (FM) simulate(b);
  else setTimeout(() => openCard(b.id), 900);
});
Bus.on('briefing:seen', ({id, user, at}) => {
  const b = brById(id); if (!b) return;
  const r = resOf(b, user); r.seen = true; r.seenAt = r.seenAt || at;
  refreshBoard();
});
Bus.on('briefing:answer', ({id, user, answer, status, at}) => {
  const b = brById(id); if (!b) return;
  const r = resOf(b, user); r.seen = true; r.a = answer; r.status = status; r.at = at;
  refreshBoard();
});

window.sfOpenNearmisses = () => DATA.nearmisses.filter(x => x.status === 'open').length;
window.sfLatestBriefing = () => DATA.briefings.slice().sort((a, b) => a.at < b.at ? 1 : -1)[0];
})();
