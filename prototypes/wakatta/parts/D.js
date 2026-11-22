/* ===== Part D: life outside work (prefix lf-) =====
   Screens: life, notice {id}, notice-new, ask, senpai, supportlog {worker?}
   Events:  notice:new {notice}, notice:answer {id,user,answer,status}, ask:new {ask}, ask:reply {id,replyJa,replyEn,by}, meetup:new {meetup}
   App is NOT available at top level (TDZ): only use it inside event handlers. */
(() => {
'use strict';
const FM = ROLE === 'foreman';
const t = (ja, en) => FM ? ja : en;
const $ = (s, r = document) => r.querySelector(s);
const $$ = (s, r = document) => [...r.querySelectorAll(s)];
const esc = s => String(s == null ? '' : s).replace(/[&<>"']/g, c => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c]));
const prm = o => `data-param='${JSON.stringify(o)}'`;
const scr = id => $(`.screen[data-screen="${id}"]`);
// shortcut: fixed demo clock so seeded dates stay "this week", swap for new Date() in production
const LF_TODAY = '2026-11-24';

/* ---------- icons (Material, 24px) ---------- */
const ICON = {
  back: 'M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z',
  next: 'M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z',
  bell: 'M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z',
  bellOff: 'M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2zm-2 1H8v-6c0-2.48 1.51-4.5 4-4.5s4 2.02 4 4.5v6z',
  event: 'M17 12h-5v5h5v-5zM16 1v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2h-1V1h-2zm3 18H5V8h14v11z',
  clock: 'M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z',
  place: 'M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z',
  bag: 'M18 6h-2c0-2.21-1.79-4-4-4S8 3.79 8 6H6c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-8 4c0 .55-.45 1-1 1s-1-.45-1-1V8h2v2zm2-6c1.1 0 2 .9 2 2h-4c0-1.1.9-2 2-2zm4 6c0 .55-.45 1-1 1s-1-.45-1-1V8h2v2z',
  water: 'M12 2c-5.33 4.55-8 8.48-8 11.8 0 4.98 3.8 8.2 8 8.2s8-3.22 8-8.2c0-3.32-2.67-7.25-8-11.8z',
  paper: 'M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z',
  warning: 'M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z',
  info: 'M11 7h2v2h-2zm0 4h2v6h-2zm1-9C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z',
  groups: 'M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z',
  mic: 'M12 14c1.66 0 2.99-1.34 2.99-3L15 5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3zm5.3-3c0 3-2.54 5.1-5.3 5.1S6.7 14 6.7 11H5c0 3.41 2.72 6.23 6 6.72V21h2v-3.28c3.28-.48 6-3.3 6-6.72h-1.7z',
  send: 'M2.01 21L23 12 2.01 3 2 10l15 2-15 2z',
  help: 'M11 18h2v-2h-2v2zm1-16C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z',
  tick: 'M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z',
  close: 'M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z',
  add: 'M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z',
  volume: 'M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z',
  play: 'M8 5v14l11-7z',
  pause: 'M6 19h4V5H6v14zm8-14v14h4V5h-4z',
  photo: 'M9 2L7.17 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2h-3.17L15 2H9zm3 15c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z',
  text: 'M3 18h12v-2H3v2zM3 6v2h18V6H3zm0 7h18v-2H3v2z',
  person: 'M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z',
  coffee: 'M2 21h18v-2H2v2zM20 8h-2V5h2v3zm0-5H4v10c0 2.21 1.79 4 4 4h6c2.21 0 4-1.79 4-4v-3h2c1.11 0 2-.9 2-2V5c0-1.11-.89-2-2-2z',
  print: 'M19 8H5c-1.66 0-3 1.34-3 3v6h4v4h12v-4h4v-6c0-1.66-1.34-3-3-3zm-3 11H8v-5h8v5zm3-7c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1zm-1-9H6v4h12V3z',
  note: 'M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zM6 20V4h7v5h5v11H6z',
};
const ic = (n, cls = '') => `<svg class="lf-i ${cls}" viewBox="0 0 24 24" aria-hidden="true"><path d="${ICON[n]}"/></svg>`;
const icBig = n => `<svg class="ic" viewBox="0 0 24 24" aria-hidden="true"><path d="${ICON[n]}"/></svg>`;

/* ---------- date helpers (fixed demo clock) ---------- */
const DOW_JA = ['日', '月', '火', '水', '木', '金', '土'], DOW_EN = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
const MON_EN = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
const dt = iso => { const [y, m, d] = iso.split('-').map(Number); return new Date(Date.UTC(y, m - 1, d)); };
const dow = iso => dt(iso).getUTCDay();
const dayDiff = iso => Math.round((dt(iso) - dt(LF_TODAY)) / 864e5);
const dm = iso => iso.split('-').map(Number);
const fmtDate = iso => { const [, m, d] = dm(iso); return FM ? `${m}/${d}(${DOW_JA[dow(iso)]})` : `${DOW_EN[dow(iso)]}, ${MON_EN[m - 1]} ${d}`; };
const rel = iso => { const n = dayDiff(iso); return FM ? (n === 0 ? '今日' : n === 1 ? '明日' : n < 0 ? `${-n}日前` : `${n}日後`) : (n === 0 ? 'Today' : n === 1 ? 'Tomorrow' : n < 0 ? `${-n} days ago` : `In ${n} days`); };
const pad = n => String(n).padStart(2, '0');
const nowHM = () => new Date().toTimeString().slice(0, 5);

/* ---------- seed data ---------- */
Object.assign(DATA.users, {u_sato: {name: '佐藤 美咲', en: 'Sato Misaki', role: 'senpai', initials: '佐', color: '#ad1457'}});
const WORKERS = ['u_rahim', 'u_nguyen', 'u_siti'];
Object.assign(DATA, {
  senpai: {u_rahim: 'u_sato', u_nguyen: 'u_sato', u_siti: 'u_sato'},
  /* notice: {id, titleJa, titleEn, src:'flyer'|'text'|'voice', seed (picsum), srcText, srcEn, stepsJa[], stepsEn[], icons[], events:[{date,time,labelJa,labelEn}],
              q:{q,qJa,options,optionsJa,answer}, kw[], prio, meet, answerEn, from, at:'YYYY-MM-DD', results:{user:status}, answers:{user:idx}} */
  notices: [
    {id: 'n_burn', titleJa: '燃えるごみ 月・木 8:00まで', titleEn: 'Burnable garbage: Mon and Thu, by 8:00', src: 'flyer', seed: 'lf-burn', at: '2026-11-10', from: '自治会',
     stepsJa: ['月曜日と木曜日に出します。', '朝8:00までに、ごみ置き場に出します。', '長崎市の指定ごみ袋に入れて、口をしばります。', '8:00をすぎたら、出さないでください。'],
     stepsEn: ['Put it out on Monday and Thursday.', 'Take it to the garbage station by 8:00 a.m.', 'Use the official city garbage bag and tie it closed.', 'Do not put it out after 8:00.'],
     icons: ['event', 'clock', 'bag', 'warning'],
     events: [{date: '2026-11-26', time: '8:00', labelJa: '燃えるごみの日', labelEn: 'Burnable garbage day'}, {date: '2026-11-30', time: '8:00', labelJa: '燃えるごみの日', labelEn: 'Burnable garbage day'}, {date: '2026-12-03', time: '8:00', labelJa: '燃えるごみの日', labelEn: 'Burnable garbage day'}],
     q: {q: 'By what time must the garbage be out?', qJa: '何時までにごみを出しますか？', options: ['9:00', '8:00', 'Any time'], optionsJa: ['9:00', '8:00', 'いつでも'], answer: 1},
     kw: ['garbage', 'trash', 'rubbish', 'burnable', 'ごみ', 'ゴミ', '燃える'],
     answerEn: 'Burnable garbage goes out on Monday and Thursday, by 8:00 a.m. The next one is Thu, Nov 26.',
     results: {u_rahim: 'wait', u_nguyen: 'green', u_siti: 'green'}, answers: {u_nguyen: 1, u_siti: 1}},
    {id: 'n_res', titleJa: '資源ごみの分け方', titleEn: 'How to sort recyclables', src: 'text', at: '2026-11-12', from: '自治会', prio: 2,
     srcText: '【自治会より】資源ごみは水曜日です。びん・缶・ペットボトルはかるく洗って、べつべつの箱に入れてください。ペットボトルはふたとラベルをはずしてください。',
     srcEn: 'From the association: Recyclables go out on Wednesday. Rinse bottles, cans and PET bottles lightly and put them in separate boxes. Take the cap and label off PET bottles.',
     stepsJa: ['びん・缶・ペットボトルは、水でかるく洗います。', 'ペットボトルは、ふたとラベルをはずします。', 'びん・缶・ペットボトルを、べつべつの箱に入れます。', '紙は、ひもでしばって出します。'],
     stepsEn: ['Rinse bottles, cans and PET bottles lightly with water.', 'For PET bottles, remove the cap and the label.', 'Put bottles, cans and PET bottles in separate boxes.', 'Tie paper up with string.'],
     icons: ['water', 'info', 'bag', 'paper'],
     events: [{date: '2026-11-25', time: '8:00', labelJa: '資源ごみの日', labelEn: 'Recyclables day'}],
     q: {q: 'What do you remove from a PET bottle?', qJa: 'ペットボトルは何をはずしますか？', options: ['The bottom', 'The cap and the label', 'Nothing'], optionsJa: ['底', 'ふたとラベル', 'なにもしない'], answer: 1},
     kw: ['sort', 'separate', 'recycle', 'recycling', 'recyclable', 'recyclables', 'bottle', 'bottles', 'pet', 'cans', 'cardboard', 'newspaper', '資源', '分け', 'ペットボトル', 'びん', '缶'],
     answerEn: 'Rinse bottles, cans and PET bottles, take the cap and label off PET bottles, and put each kind in its own box.',
     results: {u_rahim: 'green', u_nguyen: 'orange', u_siti: 'green'}, answers: {u_rahim: 1, u_nguyen: -1, u_siti: 1}},
    {id: 'n_drill', titleJa: '地区の防災訓練 12/7 9:00 公民館', titleEn: 'Neighborhood disaster drill: Dec 7, 9:00, community hall', src: 'flyer', seed: 'lf-drill', at: '2026-11-18', from: '自治会', meet: 'drill',
     stepsJa: ['12月7日（月）9:00に、公民館に行きます。', 'まず、地震のときの逃げかたを練習します。', '消火器の使いかたも練習します。', '動きやすい服と、くつで行きます。'],
     stepsEn: ['Go to the community hall at 9:00 on Monday, Dec 7.', 'First, practice how to get safe in an earthquake.', 'Also practice how to use a fire extinguisher.', 'Wear comfortable clothes and shoes.'],
     icons: ['place', 'warning', 'warning', 'info'],
     events: [{date: '2026-12-07', time: '9:00', labelJa: '防災訓練（公民館）', labelEn: 'Disaster drill (community hall)'}],
     q: {q: 'Where do you go on Dec 7?', qJa: '12月7日はどこに行きますか？', options: ['City office', 'Community hall', 'Factory'], optionsJa: ['市役所', '公民館', '工場'], answer: 1},
     kw: ['drill', 'disaster', 'earthquake', 'evacuate', 'evacuation', 'fire', '防災', '訓練', '避難', '地震'],
     answerEn: 'The disaster drill is on Mon, Dec 7 at 9:00 at the community hall.',
     results: {u_rahim: 'wait', u_nguyen: 'green', u_siti: 'red'}, answers: {u_nguyen: 1, u_siti: 0}},
    {id: 'n_fest', titleJa: '秋祭り 11/30', titleEn: 'Autumn festival: Nov 30', src: 'voice', at: '2026-11-20', from: '自治会', meet: 'festival', dur: '0:24',
     srcText: '自治会長より。11月30日、月曜日の夕方5時から、神社で秋祭りがあります。だれでも来てください。',
     srcEn: 'From the association head: There is an autumn festival at the shrine on Monday, Nov 30, from 5 p.m. Everyone is welcome.',
     stepsJa: ['11月30日（月）17:00から、神社で秋祭りがあります。', 'だれでも行けます。家族や友だちといっしょに来てください。', '食べものや飲みものは、お店で買えます。'],
     stepsEn: ['The autumn festival is at the shrine on Monday, Nov 30, from 5:00 p.m.', 'Everyone can come. Bring your family or friends.', 'You can buy food and drinks at the stalls.'],
     icons: ['event', 'groups', 'bag'],
     events: [{date: '2026-11-30', time: '17:00', labelJa: '秋祭り（神社）', labelEn: 'Autumn festival (shrine)'}],
     q: {q: 'Where is the autumn festival?', qJa: '秋祭りはどこでありますか？', options: ['At the factory', 'At the shrine', 'At the station'], optionsJa: ['工場', '神社', '駅'], answer: 1},
     kw: ['festival', 'matsuri', 'shrine', '祭', '祭り', '秋祭り'],
     answerEn: 'The autumn festival is at the shrine on Mon, Nov 30, from 5:00 p.m. Everyone can come.',
     results: {u_rahim: 'green', u_nguyen: 'green', u_siti: 'green'}, answers: {u_rahim: 1, u_nguyen: 1, u_siti: 1}},
  ],
  asks: [
    {id: 'ak1', user: 'u_rahim', q: 'When is garbage day?', qJa: 'ごみの日はいつですか？', at: '11/20 18:10', kind: 'text', match: 'n_burn', status: 'answered'},
    {id: 'ak2', user: 'u_rahim', q: 'Where can I send money home cheaply?', qJa: '安く仕送りできるところはどこですか？', at: '11/21 20:30', kind: 'voice', match: null, status: 'replied',
     replyJa: 'ゆうちょ銀行は手数料が安いですよ。今度、いっしょに行きましょう。', replyEn: 'Japan Post Bank has low fees. Let us go together next time.', by: 'u_sato'},
  ],
  /* meetup: {id, worker, senpai, kind:'coffee'|'drill'|'festival'|'other', date:'YYYY-MM-DD', noteJa, noteEn, by} */
  meetups: [
    {id: 'mu1', worker: 'u_rahim', senpai: 'u_sato', kind: 'coffee', date: '2026-10-27', noteJa: 'カフェで日本語の練習', noteEn: 'Japanese practice at a cafe', by: 'u_sato'},
    {id: 'mu2', worker: 'u_rahim', senpai: 'u_sato', kind: 'coffee', date: '2026-11-05', noteJa: '給料明細の見かたを説明', noteEn: 'Explained how to read the pay slip', by: 'u_sato'},
    {id: 'mu3', worker: 'u_nguyen', senpai: 'u_sato', kind: 'coffee', date: '2026-11-12', noteJa: '病院の行きかたを相談', noteEn: 'Talked about how to visit a clinic', by: 'u_sato'},
    {id: 'mu4', worker: 'u_siti', senpai: 'u_sato', kind: 'drill', date: '2026-11-16', noteJa: '駅前で避難経路を確認', noteEn: 'Checked the evacuation route near the station', by: 'u_sato'},
  ],
});
const rebuild = () => { DATA.events = DATA.notices.flatMap(n => (n.events || []).map((e, i) => Object.assign({id: n.id + '_' + i, noticeId: n.id}, e))); };
rebuild();

const KIND = {
  coffee: {ja: 'コーヒー', en: 'Coffee', icon: 'coffee'},
  drill: {ja: '防災訓練をいっしょに', en: 'Drill together', icon: 'warning'},
  festival: {ja: 'お祭り', en: 'Festival', icon: 'event'},
  other: {ja: 'その他', en: 'Other', icon: 'groups'},
};
const CANNED = [
  {ja: 'いい質問ですね。明日の昼休みに、いっしょに調べましょう。', en: 'Good question. Let us look it up together at lunch tomorrow.'},
  {ja: '市役所の窓口で聞くといいですよ。私もついて行きます。', en: 'It is best to ask at the city office. I will go with you.'},
  {ja: '今度の土曜日に、いっしょに行きましょう。', en: 'Let us go together this Saturday.'},
];
const QJA = {
  'When is garbage day?': 'ごみの日はいつですか？', 'Where is the drill?': '訓練はどこですか？', 'How do I get a bus pass?': 'バスの定期券はどうやって買いますか？',
  'Where can I buy a bus pass?': 'バスの定期券はどこで買えますか？', 'Is there a festival soon?': 'もうすぐお祭りはありますか？', 'How do I sort PET bottles?': 'ペットボトルはどう分けますか？',
};
const MIC_Q = ['Where can I buy a bus pass?', 'Is there a festival soon?', 'How do I sort PET bottles?'];
const SUGGEST = ['When is garbage day?', 'Where is the drill?', 'How do I get a bus pass?'];

/* Samples for the 自治会 poster. // shortcut: fake OCR/AI, three canned flyers, swap for a real OCR + LLM call */
const SAMPLES = [
  {id: 's_water', seed: 'lf-water', titleJa: '断水のお知らせ 12/2 9:00〜12:00', titleEn: 'Water outage: Dec 2, 9:00 to 12:00', capJa: '断水のお知らせ',
   textJa: '12月2日の9時から12時まで、水道工事のため断水します。前の日にバケツに水をためておいてください。',
   textEn: 'Water will be off on Dec 2 from 9:00 to 12:00 for pipe work. Please store water in buckets the day before.',
   stepsJa: ['12月2日（水）9:00〜12:00は、水が出ません。', '前の日（12月1日）に、バケツやペットボトルに水をためます。', 'トイレに使う水も、ためておきます。'],
   stepsEn: ['There is no water on Wednesday, Dec 2, from 9:00 to 12:00.', 'The day before (Dec 1), store water in buckets and bottles.', 'Also keep some water for the toilet.'],
   icons: ['water', 'water', 'info'],
   events: [{date: '2026-12-01', time: '18:00', labelJa: '水をためる', labelEn: 'Store water'}, {date: '2026-12-02', time: '9:00', labelJa: '断水（12:00まで）', labelEn: 'Water outage (until 12:00)'}],
   q: {q: 'What should you do the day before?', qJa: '前の日に何をしますか？', options: ['Buy a new faucet', 'Store water in buckets', 'Nothing'], optionsJa: ['新しい蛇口を買う', 'バケツに水をためる', 'なにもしない'], answer: 1},
   kw: ['断水', '水道', 'water', 'outage'], answerEn: 'The water will be off on Wed, Dec 2 from 9:00 to 12:00. Store water the day before.'},
  {id: 's_clean', seed: 'lf-clean', titleJa: '年末の地区大掃除 12/13 8:00', titleEn: 'Year-end neighborhood cleanup: Dec 13, 8:00', capJa: '年末の大掃除',
   textJa: '12月13日（日）朝8時から、東公園で年末の大掃除をします。手袋と帽子を持ってきてください。雨のときは中止です。',
   textEn: 'Year-end cleanup at East Park on Sunday, Dec 13, from 8 a.m. Bring gloves and a hat. Cancelled if it rains.',
   stepsJa: ['12月13日（日）8:00に、東公園へ行きます。', '手袋と帽子を持っていきます。', 'ごみ袋は自治会が用意します。', '雨のときは、中止です。'],
   stepsEn: ['Go to East Park at 8:00 on Sunday, Dec 13.', 'Bring gloves and a hat.', 'The neighborhood association provides garbage bags.', 'If it rains, it is cancelled.'],
   icons: ['place', 'bag', 'bag', 'warning'],
   events: [{date: '2026-12-13', time: '8:00', labelJa: '地区大掃除（東公園）', labelEn: 'Neighborhood cleanup (East Park)'}],
   q: {q: 'What should you bring?', qJa: '何を持っていきますか？', options: ['Money', 'A bicycle', 'Gloves and a hat'], optionsJa: ['お金', '自転車', '手袋と帽子'], answer: 2},
   kw: ['大掃除', '掃除', 'cleanup', 'cleaning'], answerEn: 'The cleanup is at East Park on Sun, Dec 13 at 8:00. Bring gloves and a hat.'},
  {id: 's_mochi', seed: 'lf-mochi', titleJa: 'もちつき大会 12/20 10:00', titleEn: 'Rice-cake pounding event: Dec 20, 10:00', capJa: 'もちつき大会',
   textJa: '12月20日（日）10時から、公民館でもちつき大会があります。参加は無料です。エプロンを持ってきてください。',
   textEn: 'Rice-cake pounding at the community hall on Sunday, Dec 20, from 10 a.m. Free to join. Bring an apron.',
   stepsJa: ['12月20日（日）10:00に、公民館へ行きます。', '参加は無料です。', 'エプロンを持っていきます。'],
   stepsEn: ['Go to the community hall at 10:00 on Sunday, Dec 20.', 'It is free to join.', 'Bring an apron.'],
   icons: ['place', 'info', 'bag'],
   events: [{date: '2026-12-20', time: '10:00', labelJa: 'もちつき大会（公民館）', labelEn: 'Rice-cake pounding (community hall)'}],
   q: {q: 'How much does it cost?', qJa: '参加費はいくらですか？', options: ['500 yen', 'Free', '2000 yen'], optionsJa: ['500円', '無料', '2000円'], answer: 1},
   kw: ['もちつき', '餅', 'mochi'], answerEn: 'The rice-cake pounding is at the community hall on Sun, Dec 20 at 10:00. It is free.'},
];

/* ---------- state + helpers ---------- */
const S = {nn: null, log: {w: 'all', m: null}, notice: null, think: null, showJa: new Set(), speaking: false, listening: false, micN: 0};
const rem = new Set(); // reminders live on this phone only
const nOf = id => DATA.notices.find(n => n.id === id);
const nT = n => FM ? n.titleJa : n.titleEn;
const nSteps = n => FM ? n.stepsJa : n.stepsEn;
const stOf = (n, u) => (n.results && n.results[u]) || 'wait';
const cnt = n => { const c = {green: 0, orange: 0, red: 0, wait: 0}; WORKERS.forEach(u => c[stOf(n, u)]++); return c; };
const ST_JA = {green: 'わかった', orange: '要確認', red: '未理解', wait: '未回答'};
const stLabel = s => FM ? ST_JA[s] : (s === 'wait' ? 'Not answered' : STATUS[s]);
const chip = s => `<span class="lf-st" data-s="${s}">${stLabel(s)}</span>`;
const av = (u, cls = '') => { const x = DATA.users[u]; return `<span class="lf-av ${cls}" style="background:${x.color}">${esc(x.initials)}</span>`; };
const short = u => String(nameOf(u)).split(' ')[0];
const srcIcon = n => ({flyer: 'photo', text: 'text', voice: 'mic'}[n.src] || 'note');
const srcName = n => ({flyer: t('チラシの写真', 'Flyer photo'), text: t('テキスト', 'Text'), voice: t('音声メモ', 'Voice memo')}[n.src] || '');
const fromName = t('自治会', 'Neighborhood association');
const pendingCount = u => DATA.notices.filter(n => stOf(n, u) === 'wait').length;
const evLabel = e => FM ? e.labelJa : e.labelEn;
const upcoming = () => DATA.events.filter(e => dayDiff(e.date) >= 0 && dayDiff(e.date) <= 30).sort((a, b) => (a.date + a.time.padStart(5, '0')).localeCompare(b.date + b.time.padStart(5, '0')));
const mkey = iso => iso.slice(0, 7);
const mlabel = k => `${Number(k.slice(5))}月`;
const myAsks = () => DATA.asks.filter(a => a.user === ME);
const nextMeet = () => { const e = upcoming().find(x => { const n = nOf(x.noticeId); return n && n.meet; }); return e && {e, n: nOf(e.noticeId)}; };
const isPoster = () => FM;

function matchNotice(q) {
  const low = q.toLowerCase(), words = low.match(/[a-z]+/g) || [];
  const hit = k => /^[a-z]+$/.test(k) ? words.includes(k) : q.includes(k);
  return [...DATA.notices].sort((a, b) => (b.prio || 1) - (a.prio || 1)).find(n => (n.kw || []).some(hit)) || null;
}

/* ---------- shell ---------- */
function shell(el, title, right = '', extra = '') {
  el.dataset.lf = 1;
  el.classList.toggle('lf-hasbar', /lf-bar/.test(extra));
  el.innerHTML = `<header class="appbar"><button class="icon-btn" data-back aria-label="${t('戻る', 'Back')}">${icBig('back')}</button><h1>${title}</h1>${right}</header><div class="content"></div>${extra}`;
  return $('.content', el);
}
const guardForeman = el => { $('.content', el).innerHTML = `<div class="lf-empty">${t('この画面は職長用です', 'This screen is for the foreman')}</div>`; };

/* ---------- Life hub ---------- */
function evCard(e, i) {
  const [, m, d] = dm(e.date), on = rem.has(e.id);
  return `<div class="lf-ev${i === 0 ? ' first' : ''}">
    <button class="lf-ev-main" data-push="notice" ${prm({id: e.noticeId})}>
      <span class="lf-ev-d"><b>${d}</b><i>${FM ? m + '月 ' + DOW_JA[dow(e.date)] : MON_EN[m - 1] + ' · ' + DOW_EN[dow(e.date)]}</i></span>
      <span class="lf-ev-t">${esc(evLabel(e))}</span><span class="lf-ev-s">${esc(e.time)} · ${rel(e.date)}</span></button>
    <button class="lf-bell${on ? ' on' : ''}" data-lf="rem:${e.id}" aria-pressed="${on}" aria-label="${t('リマインダー', 'Reminder')}">${ic(on ? 'bell' : 'bellOff')}</button></div>`;
}
const senCard = () => {
  const s = DATA.users[DATA.senpai[ME] || 'u_sato'], nx = nextMeet();
  return `<button class="card lf-sen" data-push="senpai"><span class="lf-av" style="background:${s.color}">${esc(s.initials)}</span>
    <span style="flex:1;min-width:0"><b>${t('私の先輩', 'My Senpai')}</b><small>${esc(nameOf(DATA.senpai[ME] || 'u_sato'))}${nx ? ' · ' + t('次: ', 'Next: ') + esc(nx.n.meet ? KIND[nx.n.meet][FM ? 'ja' : 'en'] : '') + ' ' + fmtDate(nx.e.date) : ''}</small></span>${ic('next', 'lf-chev')}</button>`;
};
function renderLife(el, anim) {
  const c = $('.content', el), st = anim ? ' stagger' : '';
  if (!FM) {
    const evs = upcoming();
    c.innerHTML = `<div class="lf-pad${st}">
      <div><h3 class="lf-h">This week &amp; coming up</h3>${evs.length ? `<div class="lf-strip">${evs.map(evCard).join('')}</div>` : '<div class="card lf-empty">No events yet</div>'}</div>
      <div><h3 class="lf-h"><span>Notices</span>${pendingCount(ME) ? `<span class="lf-badge" aria-label="${pendingCount(ME)} to answer">${pendingCount(ME)}</span>` : ''}</h3>
        <div class="card">${DATA.notices.map(n => `<button class="list-item lf-row" data-tap data-push="notice" ${prm({id: n.id})}><span class="lf-tile">${ic(srcIcon(n))}</span>
          <span class="lf-rmid"><b>${esc(nT(n))}</b><small>${esc(fromName)} · ${fmtDate(n.at)}</small></span>${chip(stOf(n, ME))}</button>`).join('')}</div></div>
      <button class="lf-askbtn" data-push="ask"><span class="lf-ab">${ic('help', 'lg')}</span><span style="flex:1"><b>Ask about life here</b><small>Garbage, drills, festivals. Not sure? Your Senpai answers.</small></span>${ic('next')}</button>
      ${senCard()}
    </div>`;
  } else {
    const fw = DATA.asks.filter(a => a.status === 'forwarded').length;
    c.innerHTML = `<div class="lf-pad${st}">
      <div><button class="btn lf-newbtn block" data-push="notice-new">${ic('add')}お知らせを作成</button></div>
      <div><h3 class="lf-h">配信済みのお知らせ</h3><div class="card">${DATA.notices.map(n => { const k = cnt(n);
        return `<button class="list-item lf-row" data-tap data-push="notice" ${prm({id: n.id})}><span class="lf-tile">${ic(srcIcon(n))}</span>
          <span class="lf-rmid"><b>${esc(n.titleJa)}</b><small>${fromName} · ${fmtDate(n.at)} · ${srcName(n)}</small>
            <span class="lf-cns">${['green', 'orange', 'red', 'wait'].map(s => `<span class="lf-cn${k[s] ? '' : ' z'}" data-s="${s}">${ST_JA[s]} ${k[s]}</span>`).join('')}</span></span>${ic('next', 'lf-chev')}</button>`; }).join('')}</div></div>
      <div><h3 class="lf-h">先輩・支援記録</h3><div class="card">
        <button class="list-item lf-row" data-tap data-push="senpai"><span class="lf-tile">${ic('groups')}</span><span class="lf-rmid"><b>先輩ペア</b><small>${Object.keys(DATA.senpai).length}組 · 面談 ${DATA.meetups.length}件</small></span>${ic('next', 'lf-chev')}</button>
        <button class="list-item lf-row" data-tap data-push="ask"><span class="lf-tile">${ic('help')}</span><span class="lf-rmid"><b>転送された質問</b><small>${fw ? `返信待ち ${fw}件（佐藤さん宛）` : '返信待ちはありません'}</small></span>${fw ? `<span class="lf-badge">${fw}</span>` : ''}${ic('next', 'lf-chev')}</button>
        <button class="list-item lf-row" data-tap data-push="supportlog"><span class="lf-tile">${ic('note')}</span><span class="lf-rmid"><b>支援記録</b><small>面談とお知らせ確認の記録 · PDF出力</small></span>${ic('next', 'lf-chev')}</button></div></div>
    </div>`;
  }
}

/* ---------- Notice ---------- */
function srcBlock(n) {
  const cap = `${srcName(n)} · ${fromName}`;
  if (n.src === 'flyer') return `<figure class="lf-fly">${ic('photo', 'lg')}<img src="https://picsum.photos/seed/${esc(n.seed || n.id)}/640/400" alt="${t('チラシの写真', 'Photo of the flyer')}" loading="lazy" onerror="this.remove()"><figcaption>${esc(cap)}</figcaption></figure>`;
  const tr = `<div class="lf-alt">${esc(FM ? '' : n.srcEn || '')}</div>`;
  if (n.src === 'voice') return `<button class="lf-voice" data-lf="play"><span class="lf-pl">${ic('play', 'lg')}</span><span class="lf-bars">${Array.from({length: 22}, (_, i) => `<i style="--h:${(.3 + Math.abs(Math.sin(i * 1.7)) * .7).toFixed(2)};--dl:${i * 40}ms"></i>`).join('')}</span><small>${esc(n.dur || '0:20')}</small></button>
    <blockquote class="lf-quote"><small>${esc(cap)}</small>${esc(n.srcText || '')}${tr}</blockquote>`;
  return `<blockquote class="lf-quote"><small>${esc(cap)}</small>${esc(n.srcText || '')}${tr}</blockquote>`;
}
function checkCard(n) {
  const q = n.q, a = (n.answers || {})[ME], st = stOf(n, ME), done = st !== 'wait';
  const opts = q.options.map((o, i) => { const r = done ? (i === q.answer ? 'right' : i === a ? 'wrong' : '') : '';
    return `<button class="lf-opt" data-r="${r}" data-lf="ans:${n.id}" data-i="${i}"${done ? ' disabled' : ''}><span class="lf-ol">${'ABC'[i]}</span>${esc(o)}${r === 'right' ? ic('tick') : r === 'wrong' ? ic('close') : ''}</button>`; }).join('');
  const msg = {green: 'Wakatta! That is right.', orange: 'Thanks for telling us. Your foreman will explain it again.', red: `Not quite. The answer is "${q.options[q.answer]}".`}[st];
  return `<div class="card lf-chk"><div class="lf-q">${esc(q.q)}</div>${opts}
    ${done ? `<div class="lf-res" data-s="${st}" role="status">${ic(st === 'green' ? 'tick' : st === 'red' ? 'close' : 'info')}<div>${msg}${st === 'red' ? `<br><button class="btn tonal" data-push="ask">${ic('help')}Ask a question</button>` : ''}</div></div>`
      : `<button class="btn text block lf-notsure" data-lf="ans:${n.id}" data-i="-1">I'm not sure</button>`}</div>`;
}
function resultsCard(n) {
  const q = n.q;
  return `<div class="card"><div class="lf-wrow" style="min-height:56px;flex-wrap:wrap"><div style="font-size:15px;line-height:1.4">${esc(q.qJa)}<small>正解: ${esc(q.optionsJa[q.answer])}</small></div></div>${WORKERS.map(u => { const a = (n.answers || {})[u], s = stOf(n, u);
    return `<div class="lf-wrow">${av(u)}<div style="flex:1;min-width:0"><div>${esc(nameOf(u))}</div><small>${s === 'wait' ? '回答待ち' : a >= 0 && a != null ? '回答: ' + esc(q.optionsJa[a]) : 'わからない、と回答'}</small></div>${chip(s)}</div>`; }).join('')}</div>`;
}
function renderNotice(el, anim) {
  const c = $('.content', el), n = nOf(S.notice);
  if (!n) { c.innerHTML = `<div class="lf-empty">${t('お知らせが見つかりません', 'Notice not found')}</div>`; return; }
  const evs = (n.events || []).map((e, i) => Object.assign({id: n.id + '_' + i}, e));
  c.innerHTML = `<div class="lf-pad${anim ? ' stagger' : ''}">
    <div><h2 class="lf-title">${esc(nT(n))}</h2><div class="lf-meta">${esc(fromName)} · ${fmtDate(n.at)} · ${srcName(n)}</div></div>
    <div><h3 class="lf-h">${t('やること', 'What to do')}</h3><ol class="card lf-steps">${n.stepsJa.map((ja, i) => `<li class="lf-step"><span class="lf-sn"><span class="lf-tile">${ic((n.icons || [])[i] || 'info')}</span><b>${i + 1}</b></span>
      <div class="lf-stx">${esc(nSteps(n)[i])}${FM ? `<div class="lf-alt">${esc(n.stepsEn[i] || '')}</div>` : ''}</div></li>`).join('')}</ol></div>
    ${evs.length ? `<div><h3 class="lf-h">${t('日程', 'Dates')}</h3><div class="card">${evs.map(e => { const [, m, d] = dm(e.date), on = rem.has(e.id);
      return `<div class="lf-evrow"><span class="lf-dtile"><b>${d}</b><i>${FM ? m + '月(' + DOW_JA[dow(e.date)] + ')' : MON_EN[m - 1] + ' ' + DOW_EN[dow(e.date)]}</i></span>
        <span class="lf-rmid"><b>${esc(evLabel(e))}</b><small>${esc(e.time)} · ${rel(e.date)}</small></span>
        ${FM ? '' : `<button class="btn tonal lf-rembtn${on ? ' on' : ''}" data-lf="rem:${e.id}" aria-pressed="${on}">${ic(on ? 'bell' : 'bellOff')}${on ? 'Reminder on' : 'Add reminder'}</button>`}</div>`; }).join('')}</div></div>` : ''}
    <div><h3 class="lf-h">${t('元のお知らせ', 'Original')}</h3>${srcBlock(n)}</div>
    <div><h3 class="lf-h">${FM ? 'チェック結果' : 'Check'}</h3>${FM ? resultsCard(n) : checkCard(n)}</div>
  </div>`;
}

function speak(btn) {
  const n = nOf(S.notice), ss = window.speechSynthesis;
  if (!n) return;
  if (!ss || typeof SpeechSynthesisUtterance === 'undefined') { App.toast(t('このブラウザは読み上げに未対応です', 'Read-aloud is not supported in this browser')); return; }
  if (S.speaking) { ss.cancel(); S.speaking = false; btn.classList.remove('on'); return; }
  const u = new SpeechSynthesisUtterance([nT(n), ...nSteps(n)].join('. '));
  u.lang = FM ? 'ja-JP' : 'en-US'; u.rate = .9;
  u.onend = u.onerror = () => { S.speaking = false; btn.classList.remove('on'); };
  S.speaking = true; btn.classList.add('on'); ss.speak(u);
}

/* ---------- notice-new (foreman posts as the 自治会) ---------- */
const nnInit = () => ({src: 'flyer', stage: 'pick', sample: null, text: '', res: null, evOn: [], phase: 0});
function aiMake(st) {
  // shortcut: fake OCR/AI, canned results keyed by sample or keyword; generic splitter for anything else. Swap for OCR + LLM.
  let s = st.sample && SAMPLES.find(x => x.id === st.sample);
  if (!s && st.text) s = SAMPLES.find(x => x.kw.some(k => st.text.includes(k)));
  if (s) return s;
  const sents = st.text.split(/[。\n]+/).map(x => x.trim()).filter(Boolean).slice(0, 5);
  const md = st.text.match(/(\d{1,2})月(\d{1,2})日/), tm = st.text.match(/(\d{1,2})[時:](\d{2})?/);
  const day = md ? Number(md[2]) : 0;
  return {seed: 'lf-' + st.text.length, titleJa: (sents[0] || 'お知らせ').slice(0, 24), titleEn: 'Notice from the association', textEn: '(translation not available in the prototype)',
    stepsJa: sents, stepsEn: sents.map(x => '(machine translation pending) ' + x), icons: sents.map(() => 'info'),
    events: md ? [{date: `2026-${pad(md[1])}-${pad(md[2])}`, time: tm ? `${tm[1]}:${tm[2] || '00'}` : '', labelJa: (sents[0] || '').slice(0, 14), labelEn: 'Event'}] : [],
    q: md ? {q: 'What is the date?', qJa: '日付はいつですか？', options: [`${md[1]}/${day + 1}`, `${md[1]}/${day}`, `${md[1]}/${Math.max(1, day - 1)}`], optionsJa: [`${md[1]}/${day + 1}`, `${md[1]}/${day}`, `${md[1]}/${Math.max(1, day - 1)}`], answer: 1}
      : {q: 'Did you understand this notice?', qJa: 'このお知らせがわかりましたか？', options: ['Yes', 'No'], optionsJa: ['はい', 'いいえ'], answer: 0},
    kw: [], answerEn: 'Please open the notice for details.'};
}
function nnRun(st) {
  const dly = st.src === 'flyer' ? 1300 : 250;
  st.stage = 'busy'; st.phase = st.src === 'flyer' ? 0 : 1; renderNN();
  setTimeout(() => { if (S.nn !== st) return; st.phase = 1; renderNN();
    setTimeout(() => { if (S.nn !== st) return; st.res = aiMake(st); st.evOn = st.res.events.map(() => true); st.stage = 'result'; renderNN(); }, 1100); }, dly);
}
function renderNN() {
  const el = scr('notice-new'), st = S.nn;
  if (!el || !st || !el.dataset.lf) return;
  const c = $('.content', el), bar = $('.lf-bar', el);
  if (bar) bar.hidden = st.stage !== 'result';
  const imgOf = seed => `<img src="https://picsum.photos/seed/${seed}/480/640" alt="" loading="lazy" onerror="this.remove()">`;
  if (st.stage === 'pick') {
    const seg = `<div class="lf-seg" role="group">${[['flyer', 'photo', 'チラシ写真'], ['text', 'text', 'テキスト'], ['voice', 'mic', '音声']].map(([k, i, l]) => `<button data-lf="nnsrc:${k}" class="${st.src === k ? 'on' : ''}">${ic(i)}${l}</button>`).join('')}</div>`;
    let body = '';
    if (st.src === 'flyer') body = `<h3 class="lf-h">見本のチラシをタップ</h3><div class="lf-samples">${SAMPLES.map(s => `<button class="lf-smp" data-lf="nnsmp:${s.id}"><span class="lf-img">${imgOf(s.seed)}</span><span class="lf-cap">${esc(s.capJa)}</span></button>`).join('')}</div>
      <div class="lf-to">写真を撮る代わりに見本を使います（プロトタイプ）</div>`;
    else if (st.src === 'text') body = `<h3 class="lf-h">お知らせの文章</h3><textarea class="lf-ta" data-lf-text placeholder="例: 12月13日（日）朝8時から、東公園で大掃除をします。">${esc(st.text)}</textarea>
      <div class="lf-lbl" style="margin:12px 4px 8px;font-size:13px;color:var(--on-2)">例文を入れる</div><div class="lf-wrap">${SAMPLES.map(s => `<button class="chip" data-lf="nntxt:${s.id}">${esc(s.capJa)}</button>`).join('')}</div>
      <button class="btn block" style="height:56px;border-radius:28px;margin-top:20px;font-size:16px" data-lf="nngo">AIで読み取る</button>`;
    else body = `<h3 class="lf-h">音声でお知らせを入れる</h3><button class="lf-micbig" data-lf="nnmic" aria-label="マイク">${ic('mic')}</button><div class="lf-cap2">タップして話す</div>`;
    c.innerHTML = `<div class="lf-pad">${seg}${body}</div>`;
  } else if (st.stage === 'listen') {
    c.innerHTML = `<div class="lf-pad"><h3 class="lf-h">音声でお知らせを入れる</h3><div class="lf-micbig on">${ic('mic')}</div><div class="lf-cap2">聞いています…</div>
      <div class="skeleton lf-sk" style="width:90%"></div><div class="skeleton lf-sk" style="width:70%"></div></div>`;
  } else if (st.stage === 'busy') {
    const s = st.sample && SAMPLES.find(x => x.id === st.sample);
    const label = st.src === 'flyer' && st.phase === 0 ? 'OCR: チラシの文字を読み取り中…' : 'AI: やさしい日本語と英語にしています…';
    c.innerHTML = `<div class="lf-pad">${st.src === 'flyer' && s ? `<div class="lf-scanbox" style="margin-top:16px">${imgOf(s.seed)}</div>` : `<blockquote class="lf-quote" style="margin-top:16px">${esc(st.text)}</blockquote>`}
      <div class="lf-busy"><div class="spinner"></div><span>${label}</span></div><div class="skeleton lf-sk" style="width:95%"></div><div class="skeleton lf-sk" style="width:80%"></div><div class="skeleton lf-sk" style="width:60%"></div></div>`;
  } else {
    const r = st.res;
    c.innerHTML = `<div class="lf-pad stagger">
      <div><h3 class="lf-h">タイトル<span class="lf-st" data-s="green">${ic(srcIcon({src: st.src}))}${srcName({src: st.src})}から作成</span></h3><input class="lf-field" id="lf-nn-title" value="${esc(r.titleJa)}" aria-label="タイトル"></div>
      <div><h3 class="lf-h">やさしい手順（日本語 / English）</h3><ol class="card lf-steps">${r.stepsJa.map((ja, i) => `<li class="lf-step"><span class="lf-sn"><span class="lf-tile">${ic(r.icons[i] || 'info')}</span><b>${i + 1}</b></span><div class="lf-stx">${esc(ja)}<div class="lf-alt">${esc(r.stepsEn[i] || '')}</div></div></li>`).join('')}</ol></div>
      <div><h3 class="lf-h">見つかった予定（チェックして確認）</h3><div class="card">${r.events.length ? r.events.map((e, i) => `<label class="lf-ck"><input type="checkbox" class="lf-cbx" data-lf="nnev:${i}"${st.evOn[i] ? ' checked' : ''}><span class="lf-rmid"><b>${esc(e.labelJa)}</b><small>${fmtDate(e.date)} ${esc(e.time)}</small></span></label>`).join('') : '<div class="lf-empty">予定は見つかりませんでした</div>'}</div></div>
      <div><h3 class="lf-h">チェック質問（1問）</h3><div class="card lf-chk"><div class="lf-q">${esc(r.q.qJa)}</div><div class="lf-alt" style="margin-top:2px">${esc(r.q.q)}</div>
        ${r.q.options.map((o, i) => `<div class="lf-qopt${i === r.q.answer ? ' ok' : ''}">${i === r.q.answer ? ic('tick') : '<span class="sp"></span>'}<span>${esc(r.q.optionsJa[i])} <span style="color:var(--on-2);font-size:13px">${esc(o)}</span></span></div>`).join('')}</div></div>
      <div class="lf-to">配信先: 作業者 ${WORKERS.length}名（${WORKERS.map(short).join('、')}）</div></div>`;
  }
}
function nnPost() {
  const st = S.nn, r = st && st.res; if (!r) return;
  const title = ($('#lf-nn-title') || {}).value || r.titleJa;
  const id = 'n' + Date.now().toString(36);
  const n = {id, titleJa: title.trim() || r.titleJa, titleEn: r.titleEn, src: st.src, seed: r.seed, srcText: st.src === 'flyer' ? '' : (st.text || r.textJa || ''), srcEn: r.textEn || '', dur: '0:18',
    stepsJa: r.stepsJa.slice(), stepsEn: r.stepsEn.slice(), icons: r.icons.slice(), events: r.events.filter((e, i) => st.evOn[i]).map(e => Object.assign({}, e)),
    q: JSON.parse(JSON.stringify(r.q)), kw: (r.kw || []).slice(), answerEn: r.answerEn, from: '自治会', at: LF_TODAY, results: {u_rahim: 'wait', u_nguyen: 'wait', u_siti: 'wait'}, answers: {}};
  Bus.emit('notice:new', {notice: n});
  notify({to: 'worker', title: 'New notice from your neighborhood', body: n.titleEn, push: ['notice', {id}]});
  // shortcut: simulated Nguyen/Siti (and Rahim when unpaired) answer, replace with real phones
  setTimeout(() => Bus.emit('notice:answer', {id, user: 'u_nguyen', answer: n.q.answer, status: 'green'}), 4000);
  setTimeout(() => Bus.emit('notice:answer', {id, user: 'u_siti', answer: -1, status: 'orange'}), 7000);
  if (!Bus.paired) setTimeout(() => Bus.emit('notice:answer', {id, user: 'u_rahim', answer: n.q.answer, status: 'green'}), 10000);
  S.nn = null;
  App.back();
  setTimeout(() => App.toast(`配信しました: 作業者${WORKERS.length}名に通知`), 300);
}

/* ---------- Ask ---------- */
function senBubble(a) {
  const s = DATA.users[a.by || 'u_sato'], ja = S.showJa.has(a.id);
  return `<div class="lf-b sen"><div class="lf-who">${av(a.by || 'u_sato', 'sm')}${esc(nameOf(a.by || 'u_sato'))}</div>${esc(a.replyEn)}
    <div class="lf-tr">${ic('info')}Translated from Japanese</div>${ja ? `<div class="lf-orig">${esc(a.replyJa)}</div>` : ''}
    <div><button class="lf-link" data-lf="orig:${a.id}">${ja ? 'Hide original' : 'Show original (日本語)'}</button></div></div>`;
}
function renderAsk(el, anim, scroll) {
  const c = $('.content', el);
  if (FM) {
    const fw = DATA.asks.filter(a => a.status === 'forwarded'), rp = DATA.asks.filter(a => a.status === 'replied'), au = DATA.asks.filter(a => a.status === 'answered');
    const card = a => `<div class="card lf-ibox"><div class="lf-top">${av(a.user, 'sm')}<b>${esc(nameOf(a.user))}</b><span>${esc(a.at)}</span>${a.kind === 'voice' ? ic('mic') : ''}</div>
      <div class="lf-qj">${esc(a.qJa)}</div><div class="lf-alt">${esc(a.q)}</div>`;
    c.innerHTML = `<div class="lf-inbox${anim ? ' stagger' : ''}">
      <div class="lf-hint">${ic('info')}<span>この画面は佐藤さんのスマホの代わりです。日本語で返信すると、作業者に英語で届きます。</span></div>
      ${fw.length ? fw.slice().reverse().map(a => card(a) + `<div class="lf-lbl" style="margin:12px 0 0;font-size:13px;color:var(--on-2)">返信（定型文）</div>${CANNED.map((x, i) => `<button class="lf-canned" data-lf="reply:${a.id}:${i}">${esc(x.ja)}</button>`).join('')}</div>`).join('')
        : `<div class="card lf-empty">${ic('tick', 'lg')}<div>転送された質問はありません</div></div>`}
      ${rp.length ? `<h3 class="lf-h">返信済み</h3>${rp.slice().reverse().map(a => card(a) + `<div class="lf-qopt ok" style="margin-top:8px">${ic('tick')}<span>${esc(a.replyJa)}</span></div></div>`).join('')}` : ''}
      ${au.length ? `<h3 class="lf-h">お知らせで自動回答</h3><div class="card">${au.slice().reverse().map(a => `<div class="lf-wrow">${av(a.user, 'sm')}<div style="flex:1;min-width:0"><div class="lf-ell">${esc(a.qJa)}</div><small>${esc(nOf(a.match) ? nOf(a.match).titleJa : '')}</small></div></div>`).join('')}</div>` : ''}</div>`;
    return;
  }
  const mine = myAsks();
  const bot = a => {
    if (S.think === a.id) return '<div class="lf-b bot lf-think" aria-label="Typing"><i></i><i></i><i></i></div>';
    if (a.match && nOf(a.match)) { const n = nOf(a.match);
      return `<div class="lf-b bot">${esc(n.answerEn || n.stepsEn.slice(0, 2).join(' '))}<div class="lf-src"><small>From: ${esc(n.titleJa)} notice</small><p>${esc(n.stepsEn[0] || '')}</p>
        <button class="btn text" data-push="notice" ${prm({id: n.id})}>Open notice${ic('next')}</button></div></div>`; }
    const sd = DATA.users[DATA.senpai[ME] || 'u_sato'];
    return `<div class="lf-b bot">I'll ask your Senpai, 佐藤さん (${esc(sd.en)}).${a.status === 'forwarded' ? '<div class="lf-pend">Sent to your Senpai. Waiting for her reply.</div>' : ''}</div>${a.status === 'replied' ? senBubble(a) : ''}`;
  };
  c.innerHTML = `<div class="lf-chat">
    <div class="lf-b bot">Hi ${esc(short(ME))}. Ask me about garbage days, drills, festivals and local rules. If the notices do not cover it, I will ask your Senpai.</div>
    ${mine.map(a => `<div class="lf-b me">${a.kind === 'voice' ? ic('mic') : ''}${esc(a.q)}</div>${bot(a)}`).join('')}</div>`;
  if (scroll || anim) c.scrollTop = c.scrollHeight;
}
function sendAsk(q, kind) {
  q = (q || '').trim(); if (!q) return;
  const n = matchNotice(q);
  const ask = {id: 'ak' + Date.now().toString(36), user: ME, q, qJa: QJA[q] || q + '（英語の原文）', at: nowHM(), kind, match: n ? n.id : null, status: n ? 'answered' : 'forwarded'};
  S.think = ask.id;
  setTimeout(() => { if (S.think === ask.id) { S.think = null; refresh(true); } }, 800);
  Bus.emit('ask:new', {ask});
  if (!n) {
    notify({to: 'foreman', title: '佐藤さん宛の質問が転送されました', body: `${short(ME)}: ${ask.qJa}`, push: ['ask']});
    // shortcut: when unpaired, a fake Senpai reply arrives after a delay
    if (!Bus.paired) setTimeout(() => Bus.emit('ask:reply', {id: ask.id, replyJa: CANNED[0].ja, replyEn: CANNED[0].en, by: 'u_sato'}), 3500);
  }
}
function micAsk() {
  if (S.listening) return;
  const comp = $('.lf-comp', scr('ask')), inp = $('.lf-in', comp), mic = $('.lf-mic', comp);
  S.listening = true; mic.classList.add('on'); inp.placeholder = 'Listening…';
  // shortcut: fake speech recognition, cycles canned questions. Swap for webkitSpeechRecognition.
  setTimeout(() => {
    const q = MIC_Q[S.micN++ % MIC_Q.length];
    inp.value = q; mic.classList.remove('on'); S.listening = false; inp.placeholder = 'Ask a question…';
    setTimeout(() => { inp.value = ''; sendAsk(q, 'voice'); }, 700);
  }, 1500);
}

/* ---------- Senpai ---------- */
const mu = (x, who) => { const k = KIND[x.kind] || KIND.other;
  return `<div class="list-item lf-row"><span class="lf-tile">${ic(k.icon)}</span><span class="lf-rmid"><b>${FM ? k.ja : k.en}</b><small>${fmtDate(x.date)} · ${who ? esc(nameOf(x.worker)) : (FM ? '' : 'with ') + esc(nameOf(x.senpai))}</small>${(FM ? x.noteJa : x.noteEn) || x.noteEn || x.noteJa ? `<span class="lf-note">${esc((FM ? x.noteJa : x.noteEn) || x.noteEn || x.noteJa)}</span>` : ''}</span></div>`; };
function nextCard() {
  const nx = nextMeet();
  const txt = nx ? (FM ? `「${nx.n.titleJa}」にいっしょに行く` : `Go to "${nx.n.titleEn}" together`) : t('コーヒーに誘う', 'Invite them for coffee');
  const sub = nx ? `${fmtDate(nx.e.date)} · ${rel(nx.e.date)}` : '';
  return `<div class="card lf-next"><div class="lf-nt"><span class="lf-tile">${ic('event')}</span><div><small>${t('次のおすすめ面談', 'Suggested next meet-up')}</small><b>${esc(txt)}</b><small>${esc(sub)}</small></div></div>
    <button class="btn block" data-lf="meetnew:${nx && nx.n.meet || 'coffee'}">${ic('add')}${t('面談を記録', 'Log a meet-up')}</button></div>`;
}
function renderSenpai(el, anim) {
  const c = $('.content', el), st = anim ? ' stagger' : '';
  if (!FM) {
    const sid = DATA.senpai[ME] || 'u_sato', s = DATA.users[sid], list = DATA.meetups.filter(x => x.worker === ME).sort((a, b) => b.date.localeCompare(a.date));
    c.innerHTML = `<div class="lf-pad${st}">
      <div><div class="card lf-pair">${av(sid, 'lg')}<div style="flex:1;min-width:0"><small style="margin:0">Your Senpai</small><h2>${esc(s.en)}</h2><small>${esc(s.name)}</small>
        <div class="lf-act"><button class="btn tonal" data-push="ask">${ic('help')}Ask a question</button></div></div></div></div>
      <div><h3 class="lf-h"> </h3>${nextCard()}</div>
      <div><h3 class="lf-h">Meet-ups</h3><div class="card">${list.map(x => mu(x, false)).join('') || '<div class="lf-empty">No meet-ups yet</div>'}</div></div></div>`;
  } else {
    const list = DATA.meetups.slice().sort((a, b) => b.date.localeCompare(a.date));
    c.innerHTML = `<div class="lf-pad${st}">
      <div><h3 class="lf-h">先輩ペア（タップで支援記録）</h3><div class="card">${Object.keys(DATA.senpai).map(w => { const mine = DATA.meetups.filter(x => x.worker === w).sort((a, b) => b.date.localeCompare(a.date));
        return `<button class="list-item lf-row" data-tap data-push="supportlog" ${prm({worker: w})}>${av(w)}<span class="lf-rmid"><b>${esc(nameOf(w))} ← ${esc(nameOf(DATA.senpai[w]))}</b><small>面談 ${mine.length}件${mine[0] ? ' · 最後 ' + fmtDate(mine[0].date) : ' · まだありません'}</small></span>${ic('next', 'lf-chev')}</button>`; }).join('')}</div></div>
      <div><h3 class="lf-h"> </h3>${nextCard()}</div>
      <div><h3 class="lf-h">面談の記録</h3><div class="card">${list.map(x => mu(x, true)).join('')}</div></div></div>`;
  }
}
function openMeet(kind) {
  const b = $('#lf-meet-body');
  b.innerHTML = `<div class="lf-sheet"><h2 class="lf-sh">${t('面談を記録', 'Log a meet-up')}</h2>
    ${FM ? `<div class="lf-lbl">作業者</div><div class="lf-wrap" id="lf-mw">${WORKERS.map((u, i) => `<button class="chip${i ? '' : ' on'}" data-lf="mw:${u}">${esc(short(u))}</button>`).join('')}</div>` : ''}
    <div class="lf-lbl">${t('種類', 'Kind')}</div><div class="lf-wrap" id="lf-mk">${Object.keys(KIND).map(k => `<button class="chip${k === (KIND[kind] ? kind : 'coffee') ? ' on' : ''}" data-lf="mk:${k}">${ic(KIND[k].icon)}${FM ? KIND[k].ja : KIND[k].en}</button>`).join('')}</div>
    <div class="lf-lbl">${t('日付', 'Date')}</div><input class="field" type="date" id="lf-md" value="${LF_TODAY}" aria-label="${t('日付', 'Date')}">
    <div class="lf-lbl">${t('メモ', 'Note')}</div><textarea class="lf-ta" id="lf-mn" style="min-height:80px" placeholder="${t('話したこと', 'What did you talk about?')}"></textarea>
    <button class="btn block" data-lf="meetsave">${t('保存', 'Save')}</button></div>`;
  App.open('lf-meetup');
}
function saveMeet() {
  const b = $('#lf-meet-body'), kind = ($('#lf-mk .chip.on', b) || {dataset: {lf: 'mk:coffee'}}).dataset.lf.slice(3);
  const worker = FM ? (($('#lf-mw .chip.on', b) || {dataset: {lf: 'mw:u_rahim'}}).dataset.lf.slice(3)) : ME;
  const date = $('#lf-md', b).value || LF_TODAY, note = $('#lf-mn', b).value.trim();
  // shortcut: note is not translated, stored as typed in both languages
  const m = {id: 'mu' + Date.now().toString(36), worker, senpai: DATA.senpai[worker] || 'u_sato', kind, date, noteJa: note, noteEn: note, by: ME};
  Bus.emit('meetup:new', {meetup: m});
  notify({to: FM ? 'worker' : 'foreman', title: FM ? 'Meet-up logged' : '面談が記録されました', body: `${FM ? (KIND[kind].en) : nameOf(worker) + ': ' + KIND[kind].ja} · ${date}`, push: ['senpai']});
  App.close();
  setTimeout(() => App.toast(t('面談を記録しました', 'Meet-up logged')), 260);
}

/* ---------- Support log ---------- */
const monthsAll = () => [...new Set([...DATA.meetups.map(x => mkey(x.date)), ...DATA.notices.map(n => mkey(n.at))])].sort();
function logData() {
  const {w, m} = S.log;
  const mus = DATA.meetups.filter(x => (w === 'all' || x.worker === w) && (m === 'all' || mkey(x.date) === m)).sort((a, b) => b.date.localeCompare(a.date));
  const nns = DATA.notices.filter(n => m === 'all' || mkey(n.at) === m).sort((a, b) => b.at.localeCompare(a.at));
  const wl = w === 'all' ? WORKERS : [w];
  let ok = 0, tot = 0; nns.forEach(n => wl.forEach(u => { tot++; if (stOf(n, u) === 'green') ok++; }));
  return {mus, nns, wl, ok, tot, label: m === 'all' ? '全期間' : mlabel(m)};
}
function renderLog(el) {
  const c = $('.content', el), ms = monthsAll();
  if (!S.log.m) S.log.m = ms[ms.length - 1] || 'all';
  const w = S.log.w, d = logData();
  c.innerHTML = `<div>
    <div class="lf-fl">作業者</div><div class="lf-flt" role="group">${[['all', '全員']].concat(WORKERS.map(u => [u, short(u)])).map(([k, l]) => `<button class="chip${w === k ? ' on' : ''}" data-lf="flw:${k}">${k !== 'all' ? av(k, 'sm') : ''}${esc(l)}</button>`).join('')}</div>
    <div class="lf-fl">月</div><div class="lf-flt" role="group">${['all'].concat(ms).map(k => `<button class="chip${S.log.m === k ? ' on' : ''}" data-lf="flm:${k}">${k === 'all' ? '全期間' : mlabel(k)}</button>`).join('')}</div>
    <div class="card lf-sum"><b>${d.label}: 面談 ${d.mus.length}件 · お知らせ確認 ${d.ok}/${d.tot}</b><small>${w === 'all' ? '全員' : esc(nameOf(w))} · 登録支援機関への報告用</small></div>
    <button class="btn tonal lf-exp block" data-lf="export" style="width:calc(100% - 32px)">${ic('print')}PDFを書き出す</button>
    <div class="lf-pad"><h3 class="lf-h">面談（${d.mus.length}件）</h3><div class="card">${d.mus.map(x => mu(x, true)).join('') || '<div class="lf-empty">この期間の面談はありません</div>'}</div>
    <h3 class="lf-h">お知らせ確認（${d.ok}/${d.tot}）</h3><div class="card">${d.nns.map(n => `<div class="lf-ng"><div class="lf-ngt"><span>${esc(n.titleJa)}</span><small>${fmtDate(n.at)}</small></div>
      <div class="lf-wchips">${d.wl.map(u => `<span class="lf-wc" data-s="${stOf(n, u)}">${av(u, 'sm')}${esc(short(u))} ${chip(stOf(n, u))}</span>`).join('')}</div></div>`).join('') || '<div class="lf-empty">この期間のお知らせはありません</div>'}</div></div></div>`;
}
function openPdf() {
  const d = logData(), w = S.log.w;
  $('#lf-pdf-body').innerHTML = `<h2 class="lf-sh">PDFプレビュー</h2>
    <div class="lf-pv"><div class="lf-paper"><h2>支援記録</h2><div class="lf-ps">特定技能1号 · 生活支援の実施記録 · ${d.label}</div>
      <h3>対象</h3><div>第2工場（長崎）· ${w === 'all' ? '作業者全員' : esc(nameOf(w))}</div>
      <h3>概要</h3><div>面談 ${d.mus.length}件 · お知らせ確認 ${d.ok}/${d.tot}</div>
      <h3>面談</h3><table class="lf-tb"><tr><th>日付</th><th>作業者</th><th>内容</th></tr>${d.mus.map(x => `<tr><td>${fmtDate(x.date)}</td><td>${esc(nameOf(x.worker))}</td><td>${esc(KIND[x.kind].ja)}${x.noteJa ? '：' + esc(x.noteJa) : ''}</td></tr>`).join('') || '<tr><td colspan="3">なし</td></tr>'}</table>
      <h3>お知らせ確認</h3><table class="lf-tb"><tr><th>お知らせ</th><th>作業者</th><th>結果</th></tr>${d.nns.flatMap(n => d.wl.map(u => `<tr><td>${esc(n.titleJa)}</td><td>${esc(short(u))}</td><td>${ST_JA[stOf(n, u)]}</td></tr>`)).join('')}</table>
      <div class="lf-sign"><span>発行日 ${fmtDate(LF_TODAY)}</span><span>職長 ${esc(nameOf(ME))}</span></div></div></div>
    <div class="lf-pbtns"><button class="btn outlined" data-close>キャンセル</button><button class="btn" data-lf="savepdf">${ic('print')}PDFを保存</button></div>`;
  App.open('lf-pdf');
}

/* ---------- screens ---------- */
const R = {life: renderLife, notice: renderNotice, ask: renderAsk, senpai: renderSenpai, supportlog: renderLog};
Object.assign(SCREENS, {
  life(el) { shell(el, t('くらし', 'Life')); renderLife(el, true); },
  notice(el, p) {
    S.notice = p.id;
    shell(el, t('お知らせ', 'Notice'), `<button class="icon-btn lf-spk" data-lf="speak" aria-label="${t('読み上げ', 'Read aloud')}">${icBig('volume')}</button>`);
    renderNotice(el, true);
  },
  'notice-new'(el) {
    if (!isPoster()) { shell(el, 'Notice'); guardForeman(el); return; }
    S.nn = nnInit();
    shell(el, 'お知らせを作成', '', `<div class="lf-bar" hidden><button class="btn outlined" data-lf="nnreset">やり直す</button><button class="btn" data-lf="nnpost">${ic('send')}配信</button></div>`);
    renderNN();
  },
  ask(el) {
    shell(el, FM ? '質問（佐藤さん宛）' : 'Ask', '', FM ? '' : `<div class="lf-comp"><div class="lf-sug">${SUGGEST.map(q => `<button class="chip" data-lf="sug" data-q="${esc(q)}">${esc(q)}</button>`).join('')}</div>
      <div class="lf-crow"><input class="lf-in" placeholder="Ask a question…" aria-label="Ask a question" autocomplete="off"><button class="lf-mic icon-btn" data-lf="mic" aria-label="Speak">${icBig('mic')}</button><button class="lf-send" data-lf="send" aria-label="Send">${icBig('send')}</button></div></div>`);
    renderAsk(el, true, true);
  },
  senpai(el) { shell(el, t('先輩', 'My Senpai')); renderSenpai(el, true); },
  supportlog(el, p) {
    if (!FM) { shell(el, 'Support log'); guardForeman(el); return; }
    S.log.w = (p && p.worker) || 'all';
    shell(el, '支援記録', `<button class="icon-btn" data-lf="export" aria-label="PDF">${icBig('print')}</button>`);
    renderLog(el);
  },
});

/* ---------- Bus: shared state ---------- */
function refresh(scroll) {
  Object.keys(R).forEach(id => { const el = scr(id); if (el && el.dataset.lf) R[id](el, false, scroll); });
}
Bus.on('notice:new', ({notice}) => {
  if (!notice || !notice.id) return;
  notice.results = notice.results || {}; notice.answers = notice.answers || {};
  const i = DATA.notices.findIndex(n => n.id === notice.id);
  if (i < 0) DATA.notices.unshift(notice); else DATA.notices[i] = notice;
  rebuild(); refresh();
});
Bus.on('notice:answer', ({id, user, answer, status}) => {
  const n = nOf(id); if (!n) return;
  (n.results = n.results || {})[user] = status; (n.answers = n.answers || {})[user] = answer;
  refresh();
});
Bus.on('ask:new', ({ask}) => {
  if (!ask || !ask.id) return;
  const i = DATA.asks.findIndex(a => a.id === ask.id);
  if (i < 0) DATA.asks.push(ask); else if (DATA.asks[i].status !== 'replied') Object.assign(DATA.asks[i], ask);
  refresh(true);
});
Bus.on('ask:reply', ({id, replyJa, replyEn, by}) => {
  const a = DATA.asks.find(x => x.id === id); if (!a) return;
  Object.assign(a, {status: 'replied', replyJa, replyEn, by: by || 'u_sato'});
  refresh(true);
});
Bus.on('meetup:new', ({meetup}) => {
  if (!meetup || !meetup.id) return;
  const i = DATA.meetups.findIndex(x => x.id === meetup.id);
  if (i < 0) DATA.meetups.push(meetup); else DATA.meetups[i] = meetup;
  const k = mkey(meetup.date); if (S.log.m && S.log.m !== 'all' && !monthsAll().includes(S.log.m)) S.log.m = k;
  refresh();
});

/* ---------- clicks ---------- */
document.addEventListener('click', e => {
  const el = e.target.closest('[data-lf]'); if (!el) return;
  const [act, ...rest] = el.dataset.lf.split(':'), arg = rest.join(':');
  switch (act) {
    case 'rem': {
      const ev = DATA.events.find(x => x.id === arg); if (!ev) return;
      const on = !rem.has(arg); on ? rem.add(arg) : rem.delete(arg);
      refresh();
      App.toast(on ? `${t('リマインダーを設定しました', 'Reminder set')}: ${evLabel(ev)}, ${fmtDate(ev.date)} ${ev.time}` : t('リマインダーを解除しました', 'Reminder removed'));
      break;
    }
    case 'speak': speak(el); break;
    case 'play': { el.classList.toggle('play'); setTimeout(() => el.classList.remove('play'), 4000); // shortcut: no audio, animation only
      break; }
    case 'ans': {
      const n = nOf(arg), i = Number(el.dataset.i); if (!n || stOf(n, ME) !== 'wait') return;
      const status = i < 0 ? 'orange' : i === n.q.answer ? 'green' : 'red';
      Bus.emit('notice:answer', {id: n.id, user: ME, answer: i, status});
      if (status !== 'green') notify({to: 'foreman', title: `${short(ME)}: ${ST_JA[status]}`, body: n.titleJa, push: ['notice', {id: n.id}]});
      break;
    }
    case 'orig': S.showJa.has(arg) ? S.showJa.delete(arg) : S.showJa.add(arg); refresh(); break;
    case 'sug': sendAsk(el.dataset.q, 'text'); break;
    case 'send': { const inp = $('.lf-in', scr('ask')); const q = inp.value; inp.value = ''; sendAsk(q, 'text'); break; }
    case 'mic': micAsk(); break;
    case 'reply': {
      const [id, i] = arg.split(':'), a = DATA.asks.find(x => x.id === id), c = CANNED[Number(i)]; if (!a || !c) return;
      Bus.emit('ask:reply', {id, replyJa: c.ja, replyEn: c.en, by: 'u_sato'});
      notify({to: 'worker', title: 'Your Senpai replied', body: c.en, push: ['ask']});
      App.toast('返信しました（作業者には英語で届きます）');
      break;
    }
    case 'nnsrc': S.nn.src = arg; renderNN(); break;
    case 'nnsmp': S.nn.sample = arg; nnRun(S.nn); break;
    case 'nntxt': { const s = SAMPLES.find(x => x.id === arg); S.nn.text = s.textJa; renderNN(); break; }
    case 'nngo': if (!S.nn.text.trim()) { App.toast('文章を入れてください'); return; } S.nn.sample = null; nnRun(S.nn); break;
    case 'nnmic': {
      // shortcut: fake speech, types a canned announcement
      const st = S.nn; st.stage = 'listen'; renderNN();
      setTimeout(() => { if (S.nn !== st) return; st.text = SAMPLES[0].textJa; st.sample = SAMPLES[0].id; nnRun(st); }, 1600);
      break;
    }
    case 'nnev': if (el.tagName === 'INPUT') S.nn.evOn[Number(arg)] = el.checked; break;
    case 'nnreset': S.nn = nnInit(); renderNN(); break;
    case 'nnpost': nnPost(); break;
    case 'meetnew': openMeet(arg); break;
    case 'mk': case 'mw': $$(`[data-lf^="${act}:"]`, el.parentElement).forEach(x => x.classList.toggle('on', x === el)); break;
    case 'meetsave': saveMeet(); break;
    case 'flw': S.log.w = arg; refresh(); break;
    case 'flm': S.log.m = arg; refresh(); break;
    case 'export': openPdf(); break;
    case 'savepdf':
      // shortcut: no real PDF, the preview sheet only. Swap for window.print() with a print stylesheet or a server-side render.
      App.close(); setTimeout(() => App.toast(`support-log-${S.log.m || 'all'}.pdf を保存しました（デモ）`), 260); break;
  }
});
document.addEventListener('input', e => { if (e.target.matches && e.target.matches('[data-lf-text]') && S.nn) S.nn.text = e.target.value; });
document.addEventListener('keydown', e => {
  if (e.key === 'Enter' && e.target.matches && e.target.matches('.lf-in')) { e.preventDefault(); const q = e.target.value; e.target.value = ''; sendAsk(q, 'text'); }
});

window.lfPending = pendingCount; // for Home: number of notices a worker has not answered yet
})();
