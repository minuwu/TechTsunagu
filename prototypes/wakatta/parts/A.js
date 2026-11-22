/* ===== Part A: chat screen, composer (voice dictation + auto-send), Wakatta card, worker Check card,
   re-explain, ask foreman, glossary lock, read/nudge/deadline, confusions, read aloud ===== */
(() => {
  const $ = (s, r) => (r || document).querySelector(s);
  const esc = s => String(s).replace(/[&<>"]/g, c => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;'}[c]));
  const T = (ja, en) => ROLE === 'foreman' ? ja : en;
  const P = {
    mic: 'M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3zm5.3-3c0 3-2.54 5.1-5.3 5.1S6.7 14 6.7 11H5c0 3.41 2.72 6.23 6 6.72V21h2v-3.28c3.28-.48 6-3.3 6-6.72h-1.7z',
    check: 'M9 16.17 4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z',
    close: 'M19 6.41 17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z',
    more: 'M16.59 8.59 12 13.17 7.41 8.59 6 10l6 6 6-6z',
    eq: 'M7 18h2V6H7v12zm4 4h2V2h-2v20zm-8-8h2v-4H3v4zm12 4h2V6h-2v12zm4-8v4h2v-4h-2z',
    okc: 'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z',
    warn: 'M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z',
    bad: 'M12 2C6.47 2 2 6.47 2 12s4.47 10 10 10 10-4.47 10-10S17.53 2 12 2zm5 13.59L15.59 17 12 13.41 8.41 17 7 15.59 10.59 12 7 8.41 8.59 7 12 10.59 15.41 7 17 8.41 13.41 12 17 15.59z',
    task: 'M19 3h-4.18C14.4 1.84 13.3 1 12 1s-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm-2 14-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z',
    vol: 'M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z',
    clock: 'M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z',
    arrow: 'M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z',
    lock: 'M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z',
    power: 'M13 3h-2v10h2V3zm4.83 2.17-1.42 1.42C17.99 7.86 19 9.81 19 12c0 3.87-3.13 7-7 7s-7-3.13-7-7c0-2.19 1.01-4.14 2.58-5.42L6.17 5.17C4.23 6.82 3 9.26 3 12c0 4.97 4.03 9 9 9s9-4.03 9-9c0-2.74-1.23-5.18-3.17-6.83z',
    build: 'M22.7 19l-9.1-9.1c.9-2.3.4-5-1.5-6.9-2-2-5-2.4-7.4-1.3L9 6 6 9 1.6 4.7C.4 7.1.9 10.1 2.9 12.1c1.9 1.9 4.6 2.4 6.9 1.5l9.1 9.1c.4.4 1 .4 1.4 0l2.3-2.3c.5-.4.5-1.1.1-1.4z',
    pin: 'M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z',
    drop: 'M12 2c-5.33 4.55-8 8.48-8 11.8 0 4.98 3.8 8.2 8 8.2s8-3.22 8-8.2c0-3.32-2.67-7.25-8-11.8z',
    book: 'M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 4h5v8l-2.5-1.5L6 12V4z',
    add: 'M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z',
    eye: 'M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z',
    bell: 'M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z',
    redo: 'M12 5V1L7 6l5 5V7c3.31 0 6 2.69 6 6s-2.69 6-6 6-6-2.69-6-6H4c0 4.42 3.58 8 8 8s8-3.58 8-8-3.58-8-8-8z',
    spark: 'M19 9l1.25-2.75L23 5l-2.75-1.25L19 1l-1.25 2.75L15 5l2.75 1.25L19 9zm-7.5.5L9 4 6.5 9.5 1 12l5.5 2.5L9 20l2.5-5.5L17 12l-5.5-2.5zM19 15l-1.25 2.75L15 19l2.75 1.25L19 23l1.25-2.75L23 19l-2.75-1.25L19 15z',
    reply: 'M10 9V5l-7 7 7 7v-4.1c5 0 8.5 1.6 11 5.1-1-5-4-10-11-11z',
    help: 'M11 18h2v-2h-2v2zm1-16C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z',
  };
  const svg = k => `<svg class="ic" viewBox="0 0 24 24" aria-hidden="true"><path d="${P[k]}"/></svg>`;
  const JA_ING = ['文字起こし中', '英語に翻訳中', '確認質問を作成中', 'タスクを抽出中', '作業員へ送信中', '回答を待っています'];
  const JA_STEP = ['文字起こし', '英語に翻訳', '確認質問を作成', 'タスクを抽出', '作業員へ送信', '回答が届く'];
  const ST_JA = {green: 'わかった', orange: '要確認', red: '未理解', wait: '回答待ち'};
  const stLabel = s => ROLE === 'foreman' ? ST_JA[s] : STATUS[s];
  const CIRC = 188.5;

  /* ---------------- seed data (owned by part A) ---------------- */
  if (!DATA.glossary) DATA.glossary = [
    {ja: '金型交換', en: 'die change', reading: 'かながたこうかん', alt: ['change the die']},
    {ja: '段取り', en: 'setup', reading: 'だんどり'},
    {ja: '玉掛け', en: 'slinging', reading: 'たまがけ'},
    {ja: '治具', en: 'jig', reading: 'じぐ'},
    {ja: '切粉', en: 'chips', reading: 'きりこ'},
  ];
  // confusions: {user, term, msgId, at: ISO-8601 string}. ~12 historic rows, then one row per wrong answer.
  if (!DATA.confusions) DATA.confusions = [
    ['u_siti', '玉掛け', 1], ['u_siti', '金型交換', 2], ['u_siti', '治具', 3], ['u_siti', '切粉', 6], ['u_siti', '段取り', 9], ['u_siti', '玉掛け', 12],
    ['u_rahim', '金型交換', 2], ['u_rahim', '段取り', 4], ['u_rahim', '切粉', 8], ['u_rahim', '玉掛け', 11],
    ['u_nguyen', '治具', 5], ['u_nguyen', '玉掛け', 7],
  ].map(([user, term, d], i) => ({user, term, msgId: 'h' + (i + 1), at: new Date(Date.now() - d * 864e5).toISOString()}));
  // followups: {id, msgId, thread, user, term, question, wrong, at, status:'open'|'clarified'|'understood', note?, by?, resolvedAt?}
  if (!DATA.followups) DATA.followups = [
    {id: 'f_h3', msgId: 'h3', thread: 't_line2', user: 'u_siti', term: '玉掛け', question: 'What is 玉掛け (slinging)?', questionJa: '「玉掛け」の意味は？', wrong: 'Not sure', at: new Date(Date.now() - 1 * 864e5).toISOString(), status: 'clarified', note: '玉掛けは、クレーンにワイヤーで荷物をかける作業です。 / Slinging means hooking a load to the crane with wire.', by: 'u_tanaka', resolvedAt: new Date(Date.now() - 1 * 864e5 + 36e5).toISOString()},
    {id: 'f_h5', msgId: 'h5', thread: 't_line2', user: 'u_rahim', term: '金型交換', question: 'What is 金型交換 (die change)?', questionJa: '「金型交換」の意味は？', wrong: 'Oil change', at: new Date(Date.now() - 2 * 864e5).toISOString(), status: 'clarified', note: '金型は、プレスにつける型です。 / The die is the shaped tool on the press.', by: 'u_tanaka', resolvedAt: new Date(Date.now() - 2 * 864e5 + 18e5).toISOString()},
    {id: 'f_m1_u_siti_0', msgId: 'm1', thread: 't_line2', user: 'u_siti', term: '3号機オイル', question: 'What do you check first today?', questionJa: '今日最初に何を点検しますか？', wrong: 'Air on Machine 2', at: new Date(Date.now() - 36e5).toISOString(), status: 'open'},
  ];
  // a short topic label on each question: the confusion "term" when the question has no `term`
  (DATA.script.questions || []).forEach((qq, i) => { qq.topic = qq.topic || ['集合時間', 'ロックアウト', '金型交換'][i]; });
  try {
    const m1 = DATA.messages.t_line2.find(x => x.id === 'm1');
    if (m1 && m1.wk) {
      m1.wk.questions[0].topic = m1.wk.questions[0].topic || '3号機オイル';
      m1.wk.due = m1.wk.due || '08:30';
      m1.wk.read = m1.wk.read || {u_rahim: '08:03', u_nguyen: '08:05', u_siti: '08:07'};
    }
  } catch (e) { /* seed is optional */ }

  /* ---------------- fake AI content ---------------- */
  // shortcut: fake AI (canned per topic), no model call; swap for a real LLM prompt that sees the wrong answers
  const RX = {
    '集合時間': {
      ja: '集合は 7時45分 です。8時 ではありません。8時に 仕事が はじまる ので、その 前に ここに 来て ください。',
      en: 'Gather at 7:45, not 8:00. The work starts at 8:00, so be at Press #2 before that.',
      pics: [['clock', '7:45'], ['arrow'], ['pin', 'Press #2']],
      q: {q: 'The work starts at 8:00. When do you gather?', qJa: '作業は8時に始まります。何時に集合しますか？', options: ['8:00', '7:30', '7:45'], answer: 2},
    },
    'ロックアウト': {
      ja: 'ロックアウトは、でんげんを きって、自分の かぎを かける ことです。だれも スイッチを いれられません。金型交換の 前に かならず します。',
      en: 'Lockout: switch the power off and put your own lock on it, so nobody can turn it on. Always do it before the die change.',
      pics: [['power'], ['arrow'], ['lock', 'Lock'], ['arrow'], ['build', 'Die']],
      q: {q: 'What do you do before the die change?', qJa: '金型交換の前に、何をしますか？', options: ['Lock out the power', 'Call the office', 'Run the press once'], optionsJa: ['電源をロックアウトする', '事務所に電話する', 'プレスを一度動かす'], answer: 0},
    },
    '金型交換': {
      ja: '金型（かながた）は、プレスに つける 「かた」です。金型交換は、その かたを べつの かたに かえる しごとです。',
      en: 'The die is the shaped tool on the press. A die change means we swap it for another die.',
      pics: [['build', 'Die'], ['arrow'], ['build', 'New die']],
      q: {q: 'What is 金型交換 (die change)?', qJa: '「金型交換」の意味は？', term: '金型交換', options: ['Die change', 'Oil change', 'Shift change'], optionsJa: ['金型の交換', 'オイル交換', 'シフト交代'], answer: 0},
    },
    '3号機オイル': {
      ja: 'きょうは まず 3ごうきの オイルを みます。オイルが すくない ときは、わたしに いって ください。',
      en: 'Check the oil on Machine 3 first today. If the oil is low, tell me.',
      pics: [['drop', 'Oil'], ['arrow'], ['warn', 'Low?'], ['arrow'], ['help', 'Tell Tanaka']],
      q: {q: 'What do you check first today?', qJa: '今日最初に何を点検しますか？', options: ['Safety shoes', 'Oil on Machine 3', 'Air on Machine 2'], optionsJa: ['安全靴', '3号機のオイル', '2号機のエア'], answer: 1},
    },
  };
  const TOPIC_EN = {'集合時間': 'Gather time', 'ロックアウト': 'Lockout', '金型交換': 'Die change', '3号機オイル': 'Machine 3 oil'};
  const ASKS = {  // shortcut: simulated English dictation, canned text + canned translation
    '集合時間': {en: 'Sorry, what time do we gather tomorrow? I did not catch it.', ja: 'すみません、明日は何時に集合ですか？聞き取れませんでした。'},
    'ロックアウト': {en: 'What does "lock out" mean? What should I do?', ja: '「ロックアウト」とはどういう意味ですか？何をすればいいですか？'},
    '金型交換': {en: 'What is "die change"? I do not know this word.', ja: '「金型交換」とは何ですか？この言葉がわかりません。'},
    '3号機オイル': {en: 'Which oil do I check, and where? Please tell me again.', ja: 'どのオイルをどこで点検しますか？もう一度教えてください。'},
    _: {en: 'Sorry, I did not understand this. Can you explain it again, please?', ja: 'すみません、よくわかりません。もう一度説明してください。'},
  };
  const RP = {  // shortcut: simulated Japanese dictation of a canned reply + canned translation
    '集合時間': {ja: '7時45分に集合です。8時ではありません。気をつけてね。', en: 'We gather at 7:45, not 8:00. Please be careful.'},
    'ロックアウト': {ja: '電源を切って、自分の鍵をかけることです。交換の前に必ずやってください。', en: 'Switch the power off and put your own lock on it. Always do this before the change.'},
    '金型交換': {ja: '金型はプレスにつける型です。それを別の型に替える作業のことです。', en: 'The die is the shaped tool on the press. It means swapping it for another die.'},
    '3号機オイル': {ja: '3号機の油圧オイルです。タンクの横のゲージを見てください。', en: 'The hydraulic oil on Machine 3. Look at the gauge beside the tank.'},
    _: {ja: 'もう一度ゆっくり説明します。あとで現場で一緒に見ましょう。', en: 'I will explain it again slowly. Let us look at it together on the floor later.'},
  };
  const SUG = {  // shortcut: fake AI suggestion for the glossary sheet
    'ロックアウト': {en: 'lockout', reading: 'ろっくあうと'}, '点検表': {en: 'inspection sheet', reading: 'てんけんひょう'},
    '集合': {en: 'gather', reading: 'しゅうごう'}, '保護具': {en: 'protective gear', reading: 'ほごぐ'}, '安全靴': {en: 'safety shoes', reading: 'あんぜんぐつ'},
  };

  const S = {el: null, thread: null, orig: {}, ws: {}, rs: {}, io: null, readSent: new Set(), pop: null, popT: 0, sel: ''};
  const C = {st: 'idle', tok: 0, settled: '', interim: '', voice: false, ring: null, tick: null, edit: null};
  const q = s => $(s, S.el);
  const findMsg = id => { for (const t in DATA.messages) { const m = DATA.messages[t].find(x => x.id === id); if (m) return m; } };
  const visible = m => ROLE === 'foreman' || !m.wk || m.wk.stage >= 4 || m.from === ME;
  const onChat = () => { try { return !!S.el && App.stack[App.stack.length - 1] === 'chat'; } catch (e) { return false; } };
  const hhmm = (ms) => { const d = new Date(Date.now() + (ms || 0)); return String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0'); };
  const first = id => nameOf(id).split(' ')[0];
  const topicOf = qq => qq.term || qq.topic || (qq.qJa || qq.q).slice(0, 10);
  const topicLabel = t => ROLE === 'worker' ? (TOPIC_EN[t] ? TOPIC_EN[t] + (/[^\x00-\x7f]/.test(t) && t !== '3号機オイル' ? ' (' + t + ')' : '') : t) : t;
  const wrongQs = (m, u) => { const r = m.wk.results[u] || {}; return m.wk.questions.filter((qq, i) => (r.answers || [])[i] !== qq.answer); };
  const rxp = s => s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

  /* ---------------- glossary lock: underline fixed shop terms ---------------- */
  function hl(text, en) {
    const keys = [];
    (DATA.glossary || []).forEach(g => (en ? [g.en].concat(g.alt || []) : [g.ja]).forEach(k => k && keys.push([k, g])));
    if (!keys.length || !text) return esc(text || '');
    keys.sort((a, b) => b[0].length - a[0].length);
    const re = new RegExp((en ? '\\b(?:' : '(?:') + keys.map(k => rxp(k[0])).join('|') + (en ? ')\\b' : ')'), en ? 'gi' : 'g');
    let out = '', last = 0, mm;
    while ((mm = re.exec(text))) {
      const g = keys.find(k => k[0].toLowerCase() === mm[0].toLowerCase())[1];
      out += esc(text.slice(last, mm.index)) + `<span class="ch-g" data-act="gl" data-g="${esc(g.ja)}">${esc(mm[0])}</span>`;
      last = mm.index + mm[0].length;
    }
    return out + esc(text.slice(last));
  }
  // correct explanation shown to the worker after a wrong / "Not sure" answer (+ the locked glossary term when there is one)
  function explain(qq) {
    const t = topicOf(qq), x = RX[t], g = (DATA.glossary || []).find(y => y.ja === t || y.ja === qq.term);
    const txt = x ? x.en : `The right answer is "${qq.options[qq.answer]}".`;
    return `<div class="ch-exp"><div class="ch-expt">${svg('help')}What to remember</div><div class="ch-tx">${hl(txt, true)}</div>` +
      (g ? `<div class="ch-expg">${svg('lock')}<b>${esc(g.ja)}</b><i>${esc(g.reading || '')}</i> = ${esc(g.en)}<span>Fixed shop term</span></div>` : '') + '</div>';
  }
  const picHTML = pics => (pics || []).map(([k, l]) => k === 'arrow' ? `<span class="ch-pa">${svg('arrow')}</span>` : `<span class="ch-pi">${svg(k)}${l ? `<em>${esc(l)}</em>` : ''}</span>`).join('');

  /* ---------------- message list ---------------- */
  const bars = n => Array.from({length: 14}, (_, i) => `<i style="--h:${4 + ((i * 7 + n) % 5) * 3}px"></i>`).join('');
  const spkBtn = (say) => `<button class="ch-spk" data-act="spk"${say ? ` data-say="${esc(say)}"` : ''} aria-label="${T('読み上げ', 'Read aloud')}">${svg('vol')}</button>`;
  function askQuote(m) {
    const ref = findMsg(m.ask.ref);
    return `<div class="ch-quote"><b>${T('引用 · ', 'Re: ')}${esc(topicLabel(m.ask.topic))}</b>${ref ? `<i>${esc(textOf(ref).slice(0, 38))}…</i>` : ''}</div>`;
  }
  function buildMsg(m) {
    const mine = m.from === ME, u = DATA.users[m.from];
    const n = document.createElement('div');
    n.className = 'ch-m ' + (mine ? 'own' : 'oth');
    n.dataset.id = m.id;
    const vt = m.kind === 'voice' ? `<div class="ch-vt">${svg('mic')}<span class="ch-bars">${bars(m.id.length)}</span><span>0:${String(Math.max(4, Math.round(m.ja.length / 5))).padStart(2, '0')}</span></div>` : '';
    const bub = `<div class="ch-bub">${vt}${m.ask ? askQuote(m) : ''}<div class="ch-tx">${hl(textOf(m), ROLE === 'worker')}</div></div>`;
    const row = `<div class="ch-brow">${mine ? spkBtn() + bub : bub + spkBtn()}</div>`;
    const due = m.wk && m.wk.due ? `<span class="ch-due">${svg('clock')}${T('回答期限 ', 'Reply by ')}${esc(m.wk.due)}</span>` : '';
    const meta = `<div class="ch-meta">${esc(m.at)}${due}</div>`;
    const ar = m.ask ? '<div class="ch-ar"></div>' : '';
    if (mine) {
      const wk = m.wk && ROLE === 'foreman' ? '<div class="ch-wk"><button class="ch-pill" data-act="wk" aria-expanded="false"></button><div class="ch-fold"><div><div class="ch-bd"></div></div></div></div>' : '';
      n.innerHTML = row + meta + wk + ar;
    } else {
      const o = ROLE === 'worker' ? m.ja : m.en, open = S.orig[m.id];
      const lab = open ? T('原文を隠す', 'Hide original') : T('原文を見る (English)', 'Show original (日本語)');
      const orig = o ? `<button class="ch-orig-b" data-act="orig">${lab}</button><div class="ch-fold${open ? ' open' : ''}"><div><div class="ch-orig">${hl(o, ROLE === 'foreman')}</div></div></div>` : '';
      const chk = m.wk && ROLE === 'worker' ? '<div class="ch-chk"></div>' : '';
      n.innerHTML = `<div class="ch-av avatar" style="background:${u.color}">${esc(u.initials)}</div><div class="ch-col"><div class="ch-nm">${esc(nameOf(m.from))}</div>${row}${orig}${meta}${chk}${ar}</div>`;
    }
    return n;
  }
  function scrollEnd(smooth) { const l = q('.ch-list'); if (l) l.scrollTo({top: l.scrollHeight, behavior: smooth ? 'smooth' : 'auto'}); }
  function renderList(animate) {
    const list = q('.ch-list');
    let added = false;
    (DATA.messages[S.thread] || []).filter(visible).forEach(m => {
      let n = list.querySelector(`[data-id="${m.id}"]`);
      if (!n) {
        n = buildMsg(m);
        if (animate) n.classList.add('ch-new');
        list.appendChild(n); added = true;
        if (ROLE === 'worker' && m.wk && S.io) S.io.observe(n);
      }
      fillAll(n, m);
      if (ROLE === 'worker' && m.wk && m.wk.re && m.wk.re[ME]) {
        let rn = list.querySelector(`[data-id="${m.id}~re"]`);
        if (!rn) {
          rn = buildRe(m);
          if (animate) rn.classList.add('ch-new');
          list.appendChild(rn); added = true;
        }
        fillRe(rn, m);
      }
    });
    if (added) { scrollEnd(animate); requestAnimationFrame(() => scrollEnd(animate)); }
  }
  function fillAll(n, m) { fillWk(n, m); fillChk(n, m); fillAsk(n, m); }
  function refresh(id) {
    const m = findMsg(id), n = S.el && $(`[data-id="${id}"]`, S.el);
    if (!m) return;
    if (n) fillAll(n, m);
    const rn = S.el && $(`[data-id="${id}~re"]`, S.el);
    if (rn) fillRe(rn, m);
  }
  function retext() {   // glossary changed: redraw every bubble text
    if (!S.el) return;
    S.el.querySelectorAll('.ch-m[data-id]').forEach(n => {
      const m = findMsg(n.dataset.id); if (!m) return;
      const t = $('.ch-tx', n); if (t) t.innerHTML = hl(textOf(m), ROLE === 'worker');
      const o = $('.ch-orig', n); if (o) o.innerHTML = hl(ROLE === 'worker' ? m.ja : m.en, ROLE === 'foreman');
      if (n.dataset.mid) { n._r = ''; fillRe(n, m = findMsg(n.dataset.mid)); }
    });
  }

  /* ---------------- ask thread bits (quoted question + reply) ---------------- */
  function fillAsk(n, m) {
    const ar = $('.ch-ar', n); if (!ar || !m.ask) return;
    const r = m.ask.reply;
    let h;
    if (ROLE === 'foreman') {
      h = r ? `<div class="ch-rep own"><b>あなたの返信</b><div>${esc(r.ja)}</div></div>` : `<button class="btn tonal ch-areply" data-act="areply">${svg('mic')}音声で返信する</button>`;
    } else {
      h = r ? `<div class="ch-rep"><b>Tanaka replied</b><div class="ch-tx">${esc(r.en)}</div>${spkBtn(r.en)}</div>` : `<div class="ch-aw">${svg('clock')}Waiting for Tanaka to answer</div>`;
    }
    if (ar._h !== h) { ar._h = h; ar.innerHTML = h; }
  }

  /* ---------------- Wakatta card (foreman) ---------------- */
  const summary = (wk, ids) => ['green', 'orange', 'red', 'wait'].map(s => [s, ids.filter(u => wk.results[u].status === s).length]).filter(x => x[1]).map(([s, c]) => `<b>${c}</b> ${ST_JA[s]}`).join(' · ');
  const marks = (wk, u) => wk.questions.map((qq, i) => {
    const r = wk.results[u];
    if (!r || r.status === 'wait' || !r.answers) return `<span class="ch-mk">Q${i + 1} –</span>`;
    const ok = r.answers[i] === qq.answer;
    return `<span class="ch-mk ${ok ? 'ok' : 'no'}">Q${i + 1}${svg(ok ? 'check' : 'close')}</span>`;
  }).join('');
  function fillWk(n, m) {
    const w = $('.ch-wk', n); if (!w) return;
    const wk = m.wk, st = wk.stage, ids = Object.keys(wk.results), done = st >= 5;
    const allIn = ids.every(u => wk.results[u].status !== 'wait');
    const prev = JSON.parse(w.dataset.prev || '{}');
    const cur = {}; ids.forEach(u => cur[u] = wk.results[u].status);
    w.dataset.prev = JSON.stringify(cur);
    const pct = (st + 1) / 6, prevPct = w.dataset.pct || '0';
    const head = done
      ? `<span class="ch-dots">${ids.map(u => `<i class="ch-dot ${cur[u]}${prev[u] && prev[u] !== cur[u] ? ' pop' : ''}"></i>`).join('')}</span><span class="ch-pt">${summary(wk, ids)}</span>`
      : `<span class="spinner ch-spin"></span><span class="ch-pt">${JA_ING[st + 1]}… ${st + 2}/6</span>`;
    const pill = head + `<span class="ch-chev">${svg('more')}</span>` + (done ? '' : `<i class="ch-prog" style="transform:scaleX(${prevPct})"></i>`);
    const pb = $('.ch-pill', w);
    if (w._p !== pill) {
      w._p = pill; pb.innerHTML = pill; w.dataset.pct = pct;
      const bar = $('.ch-prog', pb);
      if (bar) { void bar.offsetWidth; bar.style.transform = `scaleX(${pct})`; }
    }
    // board
    const steps = JA_STEP.map((l, i) => {
      const dn = i === 5 ? (done && allIn) : i <= st, act = !dn && (i === st + 1 || (i === 5 && done));
      return `<li class="${dn ? 'done' : ''}"><span class="ch-tick">${dn ? svg('check') : act ? '<span class="spinner"></span>' : ''}</span>${l}</li>`;
    }).join('');
    const qs = wk.questions.map((qq, i) => `<div class="ch-q"><span class="ch-qn">Q${i + 1}</span><div><div class="ch-qt">${esc(qq.qJa)}</div><div class="ch-opts">${(qq.optionsJa || qq.options).map((o, j) => `<span class="ch-op${j === qq.answer ? ' ok' : ''}">${j === qq.answer ? svg('check') : ''}${esc(o)}</span>`).join('')}</div>${qq.term ? `<span class="ch-term">用語集 · ${esc(qq.term)} = ${esc(qq.options[qq.answer])}</span>` : ''}</div></div>`).join('');
    const rows = ids.map(u => {
      const s = cur[u], usr = DATA.users[u], r = wk.results[u], rd = wk.read && wk.read[u], R = wk.re && wk.re[u], nd = wk.nudged && wk.nudged[u];
      const tag = r.re && s === 'green' ? `<span class="ch-ret">${svg('redo')}再説明で理解</span>` : '';
      const fus = (DATA.followups || []).filter(f => f.msgId === m.id && f.user === u), fs = fus.some(f => f.status === 'open') ? 'open' : fus.some(f => f.status === 'clarified') ? 'clarified' : fus.length ? 'understood' : '';
      const fuh = fs ? `<span class="ch-fu ${fs}">${{open: '要フォロー', clarified: '説明済', understood: '理解'}[fs]}</span>` : '';
      const rdh = fuh + `<span class="ch-rd${rd ? ' on' : ''}">${rd ? svg('eye') + '既読 ' + esc(rd) : '未読'}</span>`;
      let act = '';
      if (s === 'orange' || s === 'red') {
        act = R && R.sel === undefined
          ? `<div class="ch-ract">${svg('redo')}再説明を送信済み · ${esc(R.at)} · 回答待ち</div>`
          : `<button class="btn tonal ch-again" data-act="again" data-u="${u}">${svg('redo')}もう一度説明する</button>` + (R ? '<span class="ch-ract no">再説明でも未理解</span>' : '');
      } else if (s === 'wait' && done) {
        act = nd ? `<div class="ch-ract">${svg('bell')}催促済み · ${esc(nd)}</div>` : `<button class="btn tonal ch-again" data-act="nudge" data-u="${u}">${svg('bell')}催促</button>`;
      }
      return `<div class="ch-wr"><span class="avatar" style="background:${usr.color}">${esc(usr.initials)}</span><div class="nm">${esc(nameOf(u))}<div class="ch-mks">${marks(wk, u)}${rdh}${tag}</div></div><span class="ch-st ${s}${prev[u] && prev[u] !== s ? ' pop' : ''}">${ST_JA[s]}</span></div>` + (act ? `<div class="ch-wa">${act}</div>` : '');
    }).join('');
    const nt = DATA.tasks.filter(t => t.from === m.id).length;
    const due = wk.due ? `<div class="ch-dl">${svg('clock')}回答期限 ${esc(wk.due)}</div>` : '';
    const board = `<div class="ch-h">進行状況</div><ul class="ch-steps">${steps}</ul><div class="ch-h">確認質問 (${wk.questions.length})</div>${qs}<div class="ch-h">作業員の回答</div>${due}${rows}` +
      `<button class="ch-gchip" data-act="gadd">${svg('book')}用語集に追加</button>` +
      (st >= 3 && nt ? `<button class="btn tonal block" data-act="board">${svg('task')}タスク${nt}件をボードに追加しました</button>` : '');
    if (w._b !== board) { w._b = board; $('.ch-bd', w).innerHTML = board; }
  }

  /* ---------------- Check card (worker) ---------------- */
  function chkHTML(m) {
    const qs = m.wk.questions, st = S.ws[m.id] || (S.ws[m.id] = {i: 0, ans: [], fb: null, anim: false});
    const res = m.wk.results[ME], head = (dots) => `<div class="ch-chk-h">${svg('eq')}<b>Wakatta?</b><span>${qs.length} quick question${qs.length > 1 ? 's' : ''}</span>${dots || ''}</div>`;
    if (res && res.status !== 'wait') {
      const ok = (res.answers || []).filter((a, i) => a === qs[i].answer).length;
      const mine = DATA.tasks.filter(t => t.from === m.id && t.assignees.includes(ME));
      const icon = {green: 'okc', orange: 'warn', red: 'bad'}[res.status];
      const sub = res.re && res.status === 'green' ? 'understood after Tanaka explained again' : res.status === 'green' ? '' : ' · Tanaka will explain again';
      return head() + `<div class="ch-res ${res.status}">${svg(icon)}<div><b>${STATUS[res.status]}</b><span>${ok} of ${qs.length} correct${res.re && res.status === 'green' ? ' · ' : ''}${res.status === 'green' && !res.re ? '' : sub}</span></div></div>` +
        (mine.length ? `<div class="ch-tk">${svg('okc')}Added to your Board</div><div class="ch-chips">${mine.map(t => `<button data-act="board">${esc(t.title)}</button>`).join('')}</div>` : '') +
        (res.status !== 'green' ? qs.map((qq, i) => (res.answers || [])[i] !== qq.answer ? explain(qq) : '').join('') : '') +
        (res.status !== 'green' && !(m.wk.re && m.wk.re[ME]) ? `<button class="btn tonal ch-ask" data-act="askagain">${svg('mic')}Ask Tanaka to explain again</button>` : '');
    }
    const qq = qs[st.i], fb = st.fb;
    const dots = `<span class="ch-pd">${qs.map((x, i) => `<i class="${i === st.i && !fb ? 'cur' : i < st.i || (i === st.i && fb) ? (st.ans[i] === x.answer ? 'ok' : 'no') : ''}"></i>`).join('')}</span>`;
    const opts = qq.options.concat(['Not sure']).map((o, j) => {
      const idx = j === qq.options.length ? -1 : j;
      let cls = idx === -1 ? ' ns' : '', ic = '';
      if (fb) {
        if (idx === qq.answer) { cls += ' ok'; ic = svg('check'); } else if (idx === fb.sel) { cls += ' bad'; ic = svg('close'); } else cls += ' dim';
      }
      return `<button class="ch-or${cls}" data-act="pick" data-i="${idx}">${esc(o)}${ic}</button>`;
    }).join('');
    const right = fb && fb.sel === qq.answer, ns = fb && fb.sel === -1;
    const anim = st.anim ? ' ch-slide' : ''; st.anim = false;
    const nd = m.wk.nudged && m.wk.nudged[ME] ? `<div class="ch-nd">${svg('bell')}Tanaka is waiting for your answer</div>` : '';
    const due = m.wk.due ? `<span class="ch-due">${svg('clock')}Reply by ${esc(m.wk.due)}</span>` : '';
    return head(dots) + nd + `<div class="ch-cq${anim}"><div class="ch-cqn">Question ${st.i + 1} of ${qs.length}${due}</div><div class="ch-cqt">${esc(qq.q)}</div>${opts}` +
      (fb ? `<div class="ch-fbt ${right ? 'ok' : 'bad'}">${svg(right ? 'okc' : 'bad')}${right ? 'Correct' : (ns ? 'No problem · answer: ' : 'Not quite · answer: ') + esc(qq.options[qq.answer])}</div>` : '') +
      (fb && !right ? explain(qq) : '') +
      (fb && !right && !ns ? `<button class="btn ch-next" data-act="nscont">${st.i + 1 < qs.length ? 'Next question' : 'Finish'}</button>` : '') +
      (ns ?`<div class="ch-nsp"><span>Not sure? Tanaka can answer you.</span><div><button class="btn" data-act="asktanaka">${svg('mic')}Ask Tanaka</button><button class="btn text" data-act="nscont">Continue</button></div></div>` :
        !fb ? `<button class="ch-asktxt" data-act="asktanaka">${svg('mic')}Ask Tanaka</button>` : '') + '</div>';
  }
  function fillChk(n, m) {
    const box = $('.ch-chk', n); if (!box || !m.wk) return;
    const h = chkHTML(m);
    if (box._c !== h) { box._c = h; box.innerHTML = h; box.classList.toggle('fb', !!(S.ws[m.id] && S.ws[m.id].fb)); if (S.ws[m.id] && S.ws[m.id].fb) requestAnimationFrame(() => box.scrollIntoView({block: 'nearest', behavior: 'smooth'})); }
  }
  function pick(id, idx) {
    const m = findMsg(id), st = S.ws[id]; if (!m || !st || st.fb) return;
    st.fb = {sel: idx}; st.ans[st.i] = idx; refresh(id);
    if (idx !== m.wk.questions[st.i].answer) return;   // wrong / "Not sure": read the explanation, then Next, Continue or Ask Tanaka
    const at = st.i;
    setTimeout(() => advance(id, at), 1100);
  }
  function advance(id, at) {
    const m = findMsg(id), st = S.ws[id]; if (!m || !st || !st.fb || st.i !== at) return;
    const qs = m.wk.questions;
    if (st.i + 1 < qs.length) { st.fb = null; st.i++; st.anim = true; refresh(id); } else {
      st.fb = null;
      submit(id, ME, st.ans.slice());
    }
  }
  // answer in + one Follow-up per wrong answer (or "Not sure"); ids are deterministic so replays are harmless
  function submit(id, user, answers) {
    const m = findMsg(id); if (!m) return;
    Bus.emit('check:answer', {msgId: id, user, answers, status: statusOf(m.wk.questions, answers)});
    m.wk.questions.forEach((qq, i) => {
      if (answers[i] === qq.answer) return;
      Bus.emit('followup:new', {id: `f_${id}_${user}_${i}`, msgId: id, thread: m.thread, user, term: topicOf(qq), question: qq.q, questionJa: qq.qJa, wrong: answers[i] === -1 || answers[i] === undefined ? 'Not sure' : qq.options[answers[i]], at: new Date().toISOString(), status: 'open'});
    });
  }
  const fuOf = (id, user) => (DATA.followups || []).filter(f => f.msgId === id && f.user === user);
  function resolveFu(id, user, from, status, note) {
    fuOf(id, user).filter(f => f.status === from).forEach(f => Bus.emit('followup:resolve', {id: f.id, status, note, by: 'u_tanaka', at: new Date().toISOString()}));
  }

  /* ---------------- re-explain bubble (worker, addressed to one worker) ---------------- */
  function buildRe(m) {
    const n = document.createElement('div');
    n.className = 'ch-m oth ch-rxn';
    n.dataset.id = m.id + '~re'; n.dataset.mid = m.id;
    const u = DATA.users.u_tanaka;
    n.innerHTML = `<div class="ch-av avatar" style="background:${u.color}">${esc(u.initials)}</div><div class="ch-col"><div class="ch-nm">${esc(nameOf('u_tanaka'))}</div><div class="ch-rxb"></div></div>`;
    return n;
  }
  function fillRe(n, m) {
    const R = m.wk && m.wk.re && m.wk.re[ME], box = $('.ch-rxb', n); if (!R || !box) return;
    const qq = R.question, done = R.sel !== undefined, ok = R.sel === qq.answer;
    const opts = qq.options.map((o, j) => {
      let cls = '', ic = '';
      if (done) { if (j === qq.answer) { cls = ' ok'; ic = svg('check'); } else if (j === R.sel) { cls = ' bad'; ic = svg('close'); } else cls = ' dim'; }
      return `<button class="ch-or${cls}" data-act="rpick" data-i="${j}">${esc(o)}${ic}</button>`;
    }).join('');
    const h = `<div class="ch-rxt">${svg('redo')}<b>Explained again, just for you</b></div>` +
      `<div class="ch-bub ch-rxbub"><div class="ch-tx">${hl(R.en, true)}</div><div class="ch-rjp" lang="ja">${esc(R.ja)}</div><div class="ch-pics">${picHTML(R.pics)}</div>${spkBtn(R.en)}</div>` +
      `<div class="ch-rck${done ? ' fb' : ''}"><div class="ch-cqn">New question</div><div class="ch-cqt">${esc(qq.q)}</div>${opts}` +
      (done ? `<div class="ch-fbt ${ok ? 'ok' : 'bad'}">${svg(ok ? 'okc' : 'bad')}${ok ? 'Wakatta! Tanaka can see you understood.' : 'Not quite · answer: ' + esc(qq.options[qq.answer]) + '. Tanaka will help you in person.'}</div>` : '') + '</div>' +
      `<div class="ch-meta">${esc(R.at)}</div>`;
    if (n._r !== h) { n._r = h; box.innerHTML = h; }
  }

  /* ---------------- composer ---------------- */
  const setSt = s => { C.st = s; q('.ch-comp').dataset.st = s; syncAct(); };
  const syncAct = () => q('.ch-comp').toggleAttribute('data-send', C.st === 'count' || C.st === 'sending' || C.st === 'edit' || (C.st === 'idle' && !!q('.ch-ta').value.trim()));
  const hint = h => { q('.ch-hint .t').innerHTML = h; };
  const showX = on => { q('.ch-x').hidden = !on; };
  const resizeTa = () => { const t = q('.ch-ta'); t.style.height = '56px'; t.style.height = Math.max(56, Math.min(t.scrollHeight, 132)) + 'px'; t.style.overflowY = t.scrollHeight > 132 ? 'auto' : 'hidden'; };
  function drawDict(caret, top) {
    const d = q('.ch-dict');
    d.innerHTML = esc(C.settled) + (C.interim ? `<span class="int">${esc(C.interim)}</span>` : '') + (caret ? '<i class="ch-caret"></i>' : '');
    d.scrollTop = top ? 0 : d.scrollHeight;
  }
  const mkWait = tok => ms => new Promise(r => setTimeout(() => r(tok === C.tok), ms));

  function resetComp() {
    C.tok++; clearInterval(C.tick); clearTimeout(C.edit);
    if (C.ring) { C.ring.onfinish = null; C.ring.cancel(); C.ring = null; }
    C.settled = ''; C.interim = ''; C.voice = false;
    const ta = q('.ch-ta'); ta.value = ''; resizeTa();
    q('.ch-send').classList.remove('done'); q('.ch-dict').innerHTML = '';
    hint(''); showX(false); setSt('idle');
  }

  async function startListen() {
    if (C.st !== 'idle') return;
    const tok = ++C.tok, w = mkWait(tok);
    C.voice = true; C.settled = ''; C.interim = '';
    q('.ch-ta').value = '';
    setSt('listen'); showX(false); hint('<i class="rec"></i>聞いています…'); drawDict(true);
    // shortcut: simulated dictation (timed reveal of DATA.script.ja), not the Web Speech API
    const chunks = DATA.script.ja.match(/[^、。]+[、。]?/g) || [];
    if (!await w(650)) return;
    for (const ch of chunks) {
      for (let i = 0; i < ch.length;) {
        i = Math.min(ch.length, i + 2 + (Math.random() * 2 | 0));
        C.interim = ch.slice(0, i); drawDict(true);
        if (!await w(70 + Math.random() * 60)) return;
      }
      if (!await w(180)) return;
      C.settled += ch; C.interim = ''; drawDict(true);          // interim words settle
      if (!await w(/。$/.test(ch) ? 560 : 280)) return;
    }
    // silence detection: ~1.2s without speech = instruction ended
    setSt('silence'); drawDict(true);
    hint('<span class="ch-sil">…</span><span class="ch-silbar"><i></i></span><span>無音を検出中</span>');
    if (!await w(1200)) return;
    hint(svg('check') + '音声入力が終わりました'); drawDict(false);
    if (!await w(750)) return;
    startCount();
  }
  async function stopListen() {
    const tok = ++C.tok, w = mkWait(tok);
    C.settled += C.interim; C.interim = '';
    if (!C.settled.trim()) return resetComp();
    setSt('silence'); drawDict(false); hint(svg('check') + '音声入力が終わりました');
    if (await w(600)) startCount();
  }
  function startCount() {
    const tok = ++C.tok;
    clearTimeout(C.edit); q('.ch-ta').blur();
    setSt('count'); showX(true); drawDict(false, true);
    if (C.ring) { C.ring.onfinish = null; C.ring.cancel(); }
    const ring = C.ring = q('.ch-ring .pg').animate([{strokeDashoffset: CIRC}, {strokeDashoffset: 0}], {duration: 4000, easing: 'linear', fill: 'forwards'});
    let last = 0;
    const upd = () => {
      const s = Math.max(1, Math.ceil((4000 - ring.currentTime) / 1000));
      if (s === last) return; last = s;
      q('.ch-secs').textContent = s; hint(`<b>${s}</b>秒後に送信 · タップで編集`);
    };
    upd(); clearInterval(C.tick); C.tick = setInterval(upd, 120);
    ring.onfinish = () => { if (tok === C.tok) finishCount(); };
  }
  function finishCount() {
    clearInterval(C.tick); setSt('sending'); showX(false); hint('送信中…');
    q('.ch-send').classList.add('done');            // ring closed -> confirm check pops
    const tok = C.tok;
    setTimeout(() => { if (tok === C.tok) doSend(C.settled); }, 600);
  }
  function toEdit() {
    if (!['listen', 'silence', 'count'].includes(C.st)) return;
    const text = C.settled + C.interim;
    C.tok++; clearInterval(C.tick);
    if (C.ring) { C.ring.onfinish = null; C.ring.cancel(); C.ring = null; }
    const ta = q('.ch-ta'); ta.value = text;
    setSt('edit'); showX(false); hint('編集中 · 入力が止まると自動送信');
    resizeTa(); ta.focus(); ta.setSelectionRange(text.length, text.length);
  }
  function doSend(text) {
    text = (text || '').trim();
    if (!text) return resetComp();
    const id = 'm_' + Date.now().toString(36), thread = S.thread;
    let msg;
    if (C.voice && ROLE === 'foreman') {
      msg = {id, thread, from: ME, at: hhmm(), kind: 'voice', ja: text, en: DATA.script.en,
        wk: {stage: 0, due: hhmm(30 * 60000), read: {}, questions: DATA.script.questions, results: {u_rahim: {status: 'wait'}, u_nguyen: {status: 'wait'}, u_siti: {status: 'wait'}}}, tasks: []};
    } else {
      // shortcut: no real machine translation; typed text gets a placeholder counterpart
      msg = {id, thread, from: ME, at: hhmm(), kind: 'text', ja: ROLE === 'foreman' ? text : '（自動翻訳）' + text, en: ROLE === 'foreman' ? '(auto-translated) ' + text : text};
    }
    resetComp();
    Bus.emit('msg:new', {thread, msg});
  }
  function onSend() {
    if (C.st === 'count') { if (C.ring) C.ring.finish(); return; }   // tap = send now (ring snaps closed)
    if (C.st === 'idle' || C.st === 'edit') doSend(q('.ch-ta').value);
  }
  function onMic() {
    if (ROLE === 'worker') return App.toast('Not in prototype');
    if (C.st === 'idle') startListen(); else if (C.st === 'listen') stopListen();
  }

  /* ---------------- read aloud ---------------- */
  function speak(text, btn) {
    const ss = window.speechSynthesis, fail = () => App.toast(T('この端末では読み上げできません', 'Read aloud is not available on this device'));
    if (!ss || !window.SpeechSynthesisUtterance) return fail();
    // shortcut: browser speechSynthesis (voice quality depends on the device); use cloud TTS for production
    if (btn.classList.contains('on')) { ss.cancel(); btn.classList.remove('on'); return; }
    ss.cancel();
    document.querySelectorAll('.ch-spk.on').forEach(b => b.classList.remove('on'));
    const u = new SpeechSynthesisUtterance(text);
    u.lang = ROLE === 'foreman' ? 'ja-JP' : 'en-US'; u.rate = .95;
    let started = false;
    u.onstart = () => { started = true; btn.classList.add('on'); };
    u.onend = () => btn.classList.remove('on');
    u.onerror = () => { btn.classList.remove('on'); if (!started) fail(); };
    ss.speak(u);
    setTimeout(() => { if (!started) { ss.cancel(); btn.classList.remove('on'); fail(); } }, 2500);
  }

  /* ---------------- glossary popover + selection bar ---------------- */
  function closePop() { clearTimeout(S.popT); if (S.pop) { S.pop.remove(); S.pop = null; } }
  function showPop(span) {
    const g = (DATA.glossary || []).find(x => x.ja === span.dataset.g), n = span.closest('.ch-m'); if (!g || !n) return;
    closePop();
    const nr = n.getBoundingClientRect(), k = nr.width / n.offsetWidth || 1, sr = span.getBoundingClientRect();
    const pop = document.createElement('div');
    pop.className = 'ch-pop'; pop.setAttribute('role', 'tooltip');
    pop.innerHTML = `<div class="ch-pj">${esc(g.ja)}<span>${esc(g.reading || '')}</span></div><div class="ch-pe">= ${esc(g.en)}</div><div class="ch-pl">${svg('lock')}${T('固定用語 · AIは訳を変えません', 'Fixed shop term')}</div>`;
    n.appendChild(pop);
    const w = pop.offsetWidth, h = pop.offsetHeight, nw = n.offsetWidth;
    let left = (sr.left - nr.left) / k; left = Math.max(0, Math.min(left, nw - w));
    const below = (sr.bottom - nr.top) / k + 6, above = (sr.top - nr.top) / k - h - 6;
    const lr = q('.ch-list').getBoundingClientRect(), roomBelow = (lr.bottom - sr.bottom) / k;
    pop.style.left = left + 'px'; pop.style.top = (roomBelow > h + 10 || above < 0 ? below : above) + 'px';
    S.pop = pop; S.popT = setTimeout(closePop, 5000);
  }
  function selBar() {
    if (ROLE !== 'foreman' || !S.el) return;
    const bar = $('.ch-selbar', S.el); if (!bar) return;
    const s = window.getSelection(), txt = s && !s.isCollapsed ? s.toString().trim() : '';
    let ok = false;
    if (txt && txt.length <= 20 && s.anchorNode) {
      const el = s.anchorNode.nodeType === 1 ? s.anchorNode : s.anchorNode.parentElement;
      ok = !!(el && el.closest && el.closest('.own .ch-tx') && S.el.contains(el));
    }
    if (ok) { S.sel = txt; $('b', bar).textContent = '「' + txt + '」'; }
    bar.classList.toggle('on', ok);
  }
  document.addEventListener('selectionchange', selBar);
  function longPress(e) {    // long-press a word in own message: select it, selection bar offers 用語集に追加
    const tx = e.target.closest && e.target.closest('.own .ch-tx'); if (!tx || ROLE !== 'foreman') return;
    const x = e.clientX, y = e.clientY;
    clearTimeout(longPress.t);
    longPress.t = setTimeout(() => {
      const r = document.caretRangeFromPoint ? document.caretRangeFromPoint(x, y) : null;
      if (!r || !r.startContainer || r.startContainer.nodeType !== 3 || !tx.contains(r.startContainer)) return;
      const tn = r.startContainer, d = tn.data, off = r.startOffset;
      let a = 0, b = d.length;
      if (window.Intl && Intl.Segmenter) {
        for (const sg of new Intl.Segmenter('ja', {granularity: 'word'}).segment(d)) { if (off >= sg.index && off <= sg.index + sg.segment.length) { a = sg.index; b = sg.index + sg.segment.length; break; } }
      } else {
        const m = /[一-龯ァ-ヶー々]+/g; let mm;
        while ((mm = m.exec(d))) if (off >= mm.index && off <= mm.index + mm[0].length) { a = mm.index; b = a + mm[0].length; break; }
      }
      const rg = document.createRange(); rg.setStart(tn, a); rg.setEnd(tn, b);
      const s = window.getSelection(); s.removeAllRanges(); s.addRange(rg);
    }, 480);
  }
  const cancelLong = () => clearTimeout(longPress.t);

  /* ---------------- events (chat screen) ---------------- */
  function onClick(e) {
    const b = e.target.closest('[data-act]');
    if (S.pop && !(b && b.dataset.act === 'gl')) closePop();
    if (!b || !S.el.contains(b)) return;
    const n = b.closest('[data-id]'), id = n && (n.dataset.mid || n.dataset.id);
    switch (b.dataset.act) {
      case 'wk': {
        const w = b.closest('.ch-wk'), open = !w.classList.contains('open');
        w.classList.toggle('open', open); $('.ch-fold', w).classList.toggle('open', open); b.setAttribute('aria-expanded', open);
        if (open) setTimeout(() => w.scrollIntoView({block: 'nearest', behavior: 'smooth'}), 340);
        break;
      }
      case 'orig': {
        const open = S.orig[id] = !S.orig[id];
        $('.ch-fold', n).classList.toggle('open', open);
        b.textContent = open ? T('原文を隠す', 'Hide original') : T('原文を見る (English)', 'Show original (日本語)');
        break;
      }
      case 'pick': pick(id, +b.dataset.i); break;
      case 'board': if ($('.screen[data-screen="board"]')) App.tab('board'); else App.toast(T('ボードは別画面です', 'Board not in this build')); break;
      case 'again': openRx(id, b.dataset.u); break;
      case 'nudge': {
        const at = hhmm();
        Bus.emit('wk:nudge', {msgId: id, user: b.dataset.u, at});
        App.toast(`${first(b.dataset.u)}さんに催促を送りました`);
        break;
      }
      case 'askagain': {
        const m = findMsg(id), w = m ? wrongQs(m, ME)[0] : null;
        openVs('ask', {msgId: id, topic: w ? topicOf(w) : '_', fromNS: false});
        break;
      }
      case 'asktanaka': {
        const m = findMsg(id), st = S.ws[id], qq = m && st ? m.wk.questions[st.i] : null;
        openVs('ask', {msgId: id, topic: qq ? topicOf(qq) : '_', fromNS: !!(st && st.fb && st.fb.sel === -1), i: st ? st.i : 0});
        break;
      }
      case 'nscont': { const st = S.ws[id]; if (st) advance(id, st.i); break; }
      case 'rpick': {
        const m = findMsg(id), R = m && m.wk.re && m.wk.re[ME]; if (!R || R.sel !== undefined) break;
        const idx = +b.dataset.i, ok = idx === R.question.answer, cur = m.wk.results[ME] || {};
        Bus.emit('check:answer', {msgId: id, user: ME, answers: (cur.answers || []).slice(), status: ok ? 'green' : (cur.status || 'orange'), re: true, reSel: idx});
        if (ok) resolveFu(id, ME, 'clarified', 'understood');
        break;
      }
      case 'areply': {
        const m = findMsg(id);
        openVs('reply', {askId: id, msgId: m.ask.ref, topic: m.ask.topic, user: m.from, thread: m.thread, q: m.ja});
        break;
      }
      case 'gl': showPop(b); break;
      case 'gadd': openGl({}); break;
      case 'gsel': { const t = S.sel; openGl({ja: t}); break; }
      case 'spk': {
        const say = b.dataset.say || (b.closest('.ch-brow') && $('.ch-tx', b.closest('.ch-brow')).textContent);
        speak(say || '', b); break;
      }
      case 'mic': onMic(); break;
      case 'send': onSend(); break;
      case 'cancel': case 'fld': toEdit(); break;
    }
  }
  function bindOnce(el) {
    if (el._ch) return; el._ch = 1;
    el.addEventListener('click', onClick);
    el.addEventListener('pointerdown', longPress); el.addEventListener('pointerup', cancelLong); el.addEventListener('pointermove', cancelLong); el.addEventListener('pointercancel', cancelLong);
    el.addEventListener('contextmenu', e => { if (e.target.closest && e.target.closest('.ch-tx')) e.preventDefault(); });
    $('.ch-selbar', el).addEventListener('pointerdown', e => e.preventDefault());
    const ta = $('.ch-ta', el);
    ta.addEventListener('input', () => {
      resizeTa(); syncAct();
      if (C.st === 'edit') {       // typing stopped for 1.5s -> auto-send countdown starts again
        clearTimeout(C.edit);
        C.edit = setTimeout(() => { if (C.st === 'edit' && ta.value.trim()) { C.settled = ta.value.trim(); C.interim = ''; drawDict(false, true); startCount(); } }, 1500);
      }
    });
    ta.addEventListener('keydown', e => { if (e.key === 'Enter' && !e.shiftKey && !e.isComposing) { e.preventDefault(); onSend(); } });
    $('.ch-wave', el).innerHTML = Array.from({length: 40}, () => `<i style="--d:${(.45 + Math.random() * .6).toFixed(2)}s;--m:${(.35 + Math.random() * .65).toFixed(2)}"></i>`).join('');
  }

  Object.assign(SCREENS, {
    chat(el, p) {
      S.el = el; bindOnce(el);
      const id = (p && p.thread) || 't_line2', th = DATA.threads.find(t => t.id === id) || DATA.threads[0];
      S.thread = th.id; th.unread = 0;
      $('.ch-title', el).textContent = T(th.title, th.titleEn);
      $('.ch-sub', el).textContent = T(`${th.members.length}人`, `${th.members.length} members`);
      $('.ch-mem', el).innerHTML = th.members.slice(0, 4).map(u => `<span class="avatar" style="background:${DATA.users[u].color}">${esc(DATA.users[u].initials)}</span>`).join('');
      $('.ch-ta', el).placeholder = T('メッセージ・マイクで話す', 'Message');
      $('.ch-mic', el).setAttribute('aria-label', T('音声入力', 'Voice input'));
      $('.ch-list', el).innerHTML = '';
      closePop();
      setupRead(el);
      resetComp();
      renderList(false);
      scrollEnd(false); requestAnimationFrame(() => scrollEnd(false));
    },
  });

  /* ---------------- read receipts (worker phone) ---------------- */
  function setupRead(el) {
    if (S.io) { S.io.disconnect(); S.io = null; }
    if (ROLE !== 'worker' || !window.IntersectionObserver) return;
    S.io = new IntersectionObserver(es => es.forEach(en => {
      if (!en.isIntersecting || !(en.intersectionRatio >= .5 || en.intersectionRect.height >= 120) || !onChat()) return;
      const id = en.target.dataset.id, m = findMsg(id);
      if (!m || !m.wk || S.readSent.has(id) || (m.wk.read && m.wk.read[ME])) return;
      S.readSent.add(id);
      Bus.emit('wk:read', {msgId: id, user: ME, at: hhmm()});
    }), {root: $('.ch-list', el), threshold: [0, .5, 1]});
  }

  /* ---------------- sheets: re-explain, voice (ask / reply), glossary ---------------- */
  const SH = id => document.getElementById(id);
  const closeSh = () => { try { App.close(); } catch (e) { /* not booted */ } };
  const X = {tok: 0, m: null, user: null, q: null, topics: [], pics: []};

  function fakeAI(m, u) {
    // shortcut: fake AI; templates per topic. Real version: LLM prompt with the wrong answers + glossary lock.
    const wq = wrongQs(m, u), topics = wq.map(topicOf).slice(0, 2), parts = topics.map(t => RX[t]).filter(Boolean);
    if (!parts.length) {
      const w = wq[0] || m.wk.questions[0], n = w.options.length, rot = (a) => a.slice(1).concat(a[0]);
      return {topics: topics.length ? topics : [topicOf(w)], ja: 'もう一度、ゆっくり 言います。むずかしい ところを、かんたんな ことばで せつめい します。', en: 'In simple words: ' + m.en,
        pics: [['help'], ['arrow'], ['check']], q: Object.assign({}, w, {options: rot(w.options), optionsJa: w.optionsJa && rot(w.optionsJa), answer: (w.answer - 1 + n) % n})};
    }
    const pics = []; parts.forEach((p, i) => { if (i) pics.push(['arrow']); pics.push(...p.pics); });
    return {topics, ja: parts.map(p => p.ja).join('\n'), en: parts.map(p => p.en).join(' '), pics, q: Object.assign({term: topics[0] === '金型交換' ? '金型交換' : undefined}, parts[0].q)};
  }
  function openRx(msgId, u) {
    const m = findMsg(msgId); if (!m) return;
    const b = SH('ch-rx-b'), tok = ++X.tok;
    X.m = m; X.user = u;
    b.innerHTML = `<div class="ch-sh-h"><b>${esc(first(u))}さんへ 再説明</b></div><div class="ch-gen"><span class="spinner"></span><span>AIが やさしい 再説明を 作成中…</span></div><div class="ch-skel"><i class="skeleton"></i><i class="skeleton"></i><i class="skeleton"></i></div>`;
    App.open('ch-rx');
    setTimeout(() => {
      if (tok !== X.tok) return;
      const a = fakeAI(m, u); X.q = a.q; X.pics = a.pics; X.topics = a.topics;
      b.innerHTML = `<div class="ch-sh-h"><b>${esc(first(u))}さんへ 再説明</b><span class="ch-ai">${svg('spark')}AI作成 · 編集できます</span></div>` +
        `<div class="ch-sh-s">間違えた用語 ${a.topics.map(t => `<span class="ch-tchip">${esc(t)}</span>`).join('')}</div>` +
        `<label class="ch-lb" for="ch-rx-ja">やさしい日本語</label><textarea class="ch-in" id="ch-rx-ja" rows="3">${esc(a.ja)}</textarea>` +
        `<label class="ch-lb" for="ch-rx-en">English (${esc(first(u))}さんに表示)</label><textarea class="ch-in" id="ch-rx-en" rows="3">${esc(a.en)}</textarea>` +
        `<div class="ch-lb">ピクトグラム</div><div class="ch-pics">${picHTML(a.pics)}</div>` +
        `<div class="ch-lb">新しい確認質問 (1問)</div><input class="ch-in" id="ch-rx-qja" value="${esc(a.q.qJa || '')}" aria-label="質問 日本語"><input class="ch-in" id="ch-rx-qen" value="${esc(a.q.q)}" aria-label="Question English">` +
        `<div class="ch-opts">${a.q.options.map((o, j) => `<span class="ch-op${j === a.q.answer ? ' ok' : ''}">${j === a.q.answer ? svg('check') : ''}${esc((a.q.optionsJa || a.q.options)[j])}</span>`).join('')}</div>` +
        `<div class="ch-foot"><button class="btn block ch-go" data-act="rxsend">${svg('check')}承認して${esc(first(u))}さんに送る</button></div>`;
    }, 800);
  }
  function sendRx() {
    const m = X.m, u = X.user, ja = SH('ch-rx-ja'), en = SH('ch-rx-en'); if (!m || !ja || !X.q) return;
    const question = Object.assign({}, X.q, {q: SH('ch-rx-qen').value.trim() || X.q.q, qJa: SH('ch-rx-qja').value.trim() || X.q.qJa});
    // Emit once; handler runs on both phones. The foreman phone also pings the worker (once).
    Bus.emit('wk:reexplain', {msgId: m.id, user: u, ja: ja.value.trim(), en: en.value.trim(), question, pics: X.pics, topic: X.topics[0], at: hhmm()});
    resolveFu(m.id, u, 'open', 'clarified', ja.value.trim() + ' / ' + en.value.trim());
    notify({to: 'worker', title: 'Tanaka explained again', body: en.value.trim().slice(0, 80), push: ['chat', {thread: m.thread}]});
    closeSh();
    setTimeout(() => App.toast(`${first(u)}さんに再説明を送りました`), 250);
  }

  /* voice sheet: simulated dictation (ask: worker English; reply: foreman Japanese) */
  const V = {tok: 0, mode: null, ctx: null, canned: null, st: 'idle'};
  const vwait = (tok, ms) => new Promise(r => setTimeout(() => r(tok === V.tok), ms));
  function openVs(mode, ctx) {
    V.mode = mode; V.ctx = ctx; V.tok++; V.st = 'idle';
    V.canned = mode === 'ask' ? (ASKS[ctx.topic] || ASKS._) : (RP[ctx.topic] || RP._);
    const ask = mode === 'ask', ref = findMsg(ctx.msgId);
    const quote = ask
      ? `<div class="ch-quote"><b>Re: ${esc(topicLabel(ctx.topic) === '_' ? 'Tanaka’s message' : topicLabel(ctx.topic))}</b>${ref ? `<i>${esc(ref.en.slice(0, 60))}…</i>` : ''}</div>`
      : `<div class="ch-quote"><b>${esc(first(ctx.user))}さんの質問 · ${esc(ctx.topic === '_' ? '' : ctx.topic)}</b><i>${esc(ctx.q)}</i></div>`;
    SH('ch-vs-b').innerHTML = `<div class="ch-sh-h"><b>${ask ? 'Ask Tanaka' : `${esc(first(ctx.user))}さんへ返信`}</b><span class="ch-ai">${ask ? 'Speak in English · Tanaka reads Japanese' : 'マイクで話す · 相手には英語で届きます'}</span></div>${quote}` +
      `<div class="ch-vbox" data-st="idle"><div class="ch-vwave">${Array.from({length: 28}, () => `<i style="--d:${(.45 + Math.random() * .6).toFixed(2)}s;--m:${(.35 + Math.random() * .65).toFixed(2)}"></i>`).join('')}</div><div class="ch-vtxt" id="ch-vtxt"></div></div>` +
      `<div class="ch-vhint" id="ch-vhint">${ask ? 'Tap the mic and speak' : 'マイクを押して話してください'}</div>` +
      `<div class="ch-foot"><div class="ch-vrow"><button class="btn text" data-act="vredo" hidden>${ask ? 'Record again' : 'もう一度録音'}</button><button class="ch-vmic" data-act="vmic" aria-label="${ask ? 'Record' : '録音'}">${svg('mic')}</button><button class="btn" data-act="vsend" disabled>${svg('reply')}${ask ? 'Send to Tanaka' : '送信'}</button></div></div>`;
    App.open('ch-vs');
  }
  async function vListen() {
    const tok = ++V.tok, ask = V.mode === 'ask', box = $('.ch-vbox', SH('ch-vs-b')), tx = SH('ch-vtxt'), hintEl = SH('ch-vhint');
    V.st = 'listen'; box.dataset.st = 'listen'; tx.innerHTML = '<i class="ch-caret"></i>';
    hintEl.innerHTML = `<i class="rec"></i>${ask ? 'Listening…' : '聞いています…'}`;
    const text = ask ? V.canned.en : V.canned.ja;
    const chunks = text.match(ask ? /[^,.?!]+[,.?!]?\s*/g : /[^、。？]+[、。？]?/g) || [text];
    let settled = '';
    if (!await vwait(tok, 500)) return;
    for (const ch of chunks) {
      for (let i = 0; i < ch.length;) {
        i = Math.min(ch.length, i + 2 + (Math.random() * 2 | 0));
        tx.innerHTML = esc(settled) + `<span class="int">${esc(ch.slice(0, i))}</span><i class="ch-caret"></i>`;
        if (!await vwait(tok, 55 + Math.random() * 45)) return;
      }
      settled += ch; tx.innerHTML = esc(settled) + '<i class="ch-caret"></i>';
      if (!await vwait(tok, 200)) return;
    }
    vDone(tok);
  }
  function vDone(tok) {
    const ask = V.mode === 'ask', box = $('.ch-vbox', SH('ch-vs-b'));
    V.tok = tok ? tok : ++V.tok; V.st = 'done'; box.dataset.st = 'done';
    SH('ch-vtxt').textContent = ask ? V.canned.en : V.canned.ja;
    SH('ch-vhint').innerHTML = svg('check') + (ask ? 'Done. Check the text, then send.' : '入力できました。内容を確認して送信してください。');
    $('[data-act=vsend]', SH('ch-vs-b')).disabled = false; $('[data-act=vredo]', SH('ch-vs-b')).hidden = false;
  }
  function sendVs() {
    if (V.st !== 'done') return;
    const c = V.ctx, cn = V.canned;
    if (V.mode === 'ask') {
      const ref = findMsg(c.msgId), id = 'a_' + Date.now().toString(36);
      Bus.emit('ask:foreman', {id, msgId: c.msgId, user: ME, thread: ref ? ref.thread : S.thread, topic: c.topic, en: cn.en, ja: cn.ja, at: hhmm()});
      notify({to: 'foreman', title: first(ME) + 'さんから質問', body: cn.ja, push: ['chat', {thread: ref ? ref.thread : S.thread}]});
      closeSh();
      setTimeout(() => App.toast('Sent to Tanaka'), 250);
      if (c.fromNS) setTimeout(() => advance(c.msgId, c.i), 500);
    } else {
      Bus.emit('ask:foremanReply', {id: c.askId, msgId: c.msgId, user: c.user, ja: cn.ja, en: cn.en, at: hhmm()});
      notify({to: 'worker', title: 'Tanaka replied to your question', body: cn.en, push: ['chat', {thread: c.thread}]});
      closeSh();
      setTimeout(() => App.toast(first(c.user) + 'さんに返信しました'), 250);
    }
  }

  /* glossary sheet */
  function openGl(pre) {
    const g = (DATA.glossary || []).find(x => x.ja === pre.ja), sg = SUG[pre.ja] || {};   // shortcut: fake AI suggestion
    SH('ch-gl-b').innerHTML = `<div class="ch-sh-h"><b>用語集に追加</b><span class="ch-ai">${svg('lock')}固定用語</span></div>` +
      '<p class="ch-sh-p">AIは固定用語の訳を変えません。両方の画面で下線が付き、タップで意味が見られます。</p>' +
      `<label class="ch-lb" for="ch-gl-ja">日本語</label><input class="ch-in" id="ch-gl-ja" value="${esc(pre.ja || '')}" autocomplete="off">` +
      `<label class="ch-lb" for="ch-gl-en">English</label><input class="ch-in" id="ch-gl-en" value="${esc(g ? g.en : sg.en || '')}" autocomplete="off">` +
      `<label class="ch-lb" for="ch-gl-rd">よみがな</label><input class="ch-in" id="ch-gl-rd" value="${esc(g ? g.reading : sg.reading || '')}" autocomplete="off">` +
      `<div class="ch-gprev" id="ch-gl-pv"></div><div class="ch-foot"><button class="btn block ch-go" data-act="glsave">${svg('add')}用語集に追加</button></div>`;
    glPreview(); App.open('ch-gl');
  }
  function glPreview() {
    const v = id => (SH(id) ? SH(id).value.trim() : '');
    const pv = SH('ch-gl-pv'); if (!pv) return;
    pv.innerHTML = v('ch-gl-ja') ? `<span class="ch-g">${esc(v('ch-gl-ja'))}</span><span> = ${esc(v('ch-gl-en') || '…')}</span><span class="ch-pl">${svg('lock')}固定用語</span>` : '';
  }
  function saveGl() {
    const v = id => SH(id).value.trim(), ja = v('ch-gl-ja'), en = v('ch-gl-en');
    if (!ja || !en) { App.toast('日本語とEnglishを入力してください'); (ja ? SH('ch-gl-en') : SH('ch-gl-ja')).focus(); return; }
    Bus.emit('glossary:add', {ja, en, reading: v('ch-gl-rd')});
    closeSh();
    window.getSelection().removeAllRanges();
    setTimeout(() => App.toast(`「${ja}」を用語集に追加しました`), 250);
  }

  function onSheet(e) {
    const b = e.target.closest('[data-act]'); if (!b) return;
    switch (b.dataset.act) {
      case 'rxsend': sendRx(); break;
      case 'vmic': if (V.st === 'idle') vListen(); else if (V.st === 'listen') vDone(++V.tok); break;
      case 'vredo': { V.tok++; V.st = 'idle'; const bx = $('.ch-vbox', SH('ch-vs-b')); bx.dataset.st = 'idle'; SH('ch-vtxt').textContent = ''; SH('ch-vhint').textContent = V.mode === 'ask' ? 'Tap the mic and speak' : 'マイクを押して話してください'; b.hidden = true; $('[data-act=vsend]', SH('ch-vs-b')).disabled = true; break; }
      case 'vsend': sendVs(); break;
      case 'glsave': saveGl(); break;
    }
  }
  ['ch-rx', 'ch-vs', 'ch-gl'].forEach(id => { const el = SH(id); if (el) { el.addEventListener('click', onSheet); el.addEventListener('input', e => { if (e.target.id && e.target.id.startsWith('ch-gl-')) glPreview(); }); } });

  /* ---------------- Bus: state + pipeline ---------------- */
  function chIncoming(msg, label) {          // a message from the other phone: render live, or toast + unread badge
    const th = DATA.threads.find(t => t.id === msg.thread);
    if (onChat() && S.thread === msg.thread) return renderList(true);
    if (th) th.unread = (th.unread || 0) + 1;
    const open = () => { if (onChat()) SCREENS.chat(S.el, {thread: msg.thread}); else App.push('chat', {thread: msg.thread}); };
    App.toast(label || (ROLE === 'worker' ? 'New message from ' + first(msg.from) : `${first(msg.from)}さん: ${textOf(msg).slice(0, 18)}`), {label: T('開く', 'Open'), run: open});
  }
  Bus.on('msg:new', ({thread, msg}) => {
    const arr = DATA.messages[thread] || (DATA.messages[thread] = []);
    if (arr.some(x => x.id === msg.id)) return;
    arr.push(msg);                                   // DATA mutated only here, so both phones stay in step
    if (ROLE === 'foreman' && msg.from === ME && msg.wk) runPipeline(msg.id);
    if (!visible(msg)) return;
    if (msg.from === ME) { if (S.el && S.thread === thread) renderList(true); } else chIncoming(msg);
  });
  Bus.on('wk:stage', ({msgId, stage}) => {
    const m = findMsg(msgId); if (!m || !m.wk) return;
    const was = visible(m);
    m.wk.stage = Math.max(m.wk.stage, stage);
    if (ROLE === 'worker' && !was && visible(m)) chIncoming(m); else refresh(msgId);
  });
  // one confusion row per wrong answer, deduped on user + msgId + term
  function confuse(user, term, msgId) {
    if (!term || DATA.confusions.some(c => c.user === user && c.msgId === msgId && c.term === term)) return;
    DATA.confusions.push({user, term, msgId, at: new Date().toISOString()});
  }
  Bus.on('check:answer', ({msgId, user, answers, status, re, reSel}) => {
    const m = findMsg(msgId); if (!m || !m.wk) return;
    const old = m.wk.results[user] || {};
    if (re) {
      const R = m.wk.re && m.wk.re[user]; if (!R || R.sel !== undefined) return;
      R.sel = reSel;
      const ok = reSel === R.question.answer;
      m.wk.results[user] = {status: ok ? 'green' : old.status || status, answers: old.answers || answers, re: ok ? true : old.re};
      if (!ok) confuse(user, R.question.term || R.topic, msgId + '~re');
    } else {
      m.wk.results[user] = {status, answers};
      answers.forEach((a, i) => { if (a !== m.wk.questions[i].answer) confuse(user, topicOf(m.wk.questions[i]), msgId); });
    }
    refresh(msgId);
  });
  Bus.on('followup:new', f => {
    const arr = DATA.followups || (DATA.followups = []);
    if (arr.some(x => x.id === f.id)) return;
    arr.push(Object.assign({status: 'open'}, f));
    if (ROLE === 'foreman') refresh(f.msgId);
  });
  Bus.on('followup:resolve', ({id, status, note, by, at}) => {
    const f = (DATA.followups || []).find(x => x.id === id); if (!f) return;
    if (f.status === 'understood' || (f.status === 'clarified' && status === 'clarified')) return;   // idempotent, never goes backwards
    f.status = status; if (note) f.note = note; if (by) f.by = by; f.resolvedAt = at;
    if (ROLE === 'foreman') refresh(f.msgId);
  });
  Bus.on('wk:read',({msgId, user, at}) => {
    const m = findMsg(msgId); if (!m || !m.wk) return;
    const rd = m.wk.read || (m.wk.read = {});
    if (rd[user]) return;
    rd[user] = at || hhmm();
    if (ROLE === 'foreman') refresh(msgId);
  });
  Bus.on('wk:nudge', ({msgId, user, at}) => {
    const m = findMsg(msgId); if (!m || !m.wk) return;
    const nd = m.wk.nudged || (m.wk.nudged = {});
    if (nd[user]) return;
    nd[user] = at || hhmm();
    refresh(msgId);
    if (ROLE === 'worker' && user === ME) {
      notify({to: 'worker', title: 'Tanaka is waiting for your answer', body: 'Please answer the quick check' + (m.wk.due ? ' by ' + m.wk.due : ''), push: ['chat', {thread: m.thread}]});
      setTimeout(() => {
        if (!(onChat() && S.thread === m.thread)) return;
        const nb = $('.ch-nb', S.el); nb.innerHTML = `${svg('bell')}<span>Tanaka is waiting for your answer</span>`; nb.hidden = false;
        nb.classList.remove('in'); void nb.offsetWidth; nb.classList.add('in');
        setTimeout(() => { nb.hidden = true; }, 5200);
      }, 0);
    }
  });
  Bus.on('wk:reexplain', p => {
    const m = findMsg(p.msgId); if (!m || !m.wk) return;
    (m.wk.re || (m.wk.re = {}))[p.user] = {ja: p.ja, en: p.en, question: p.question, pics: p.pics || [], topic: p.topic, at: p.at || hhmm()};
    if (ROLE === 'worker') {
      if (p.user !== ME) return;
      if (onChat() && S.thread === m.thread) renderList(true);
      else {
        const th = DATA.threads.find(t => t.id === m.thread); if (th) th.unread = (th.unread || 0) + 1;
        setTimeout(() => App.toast('Tanaka explained again, just for you', {label: 'Open', run: () => { if (onChat()) SCREENS.chat(S.el, {thread: m.thread}); else App.push('chat', {thread: m.thread}); }}), 0);
      }
      return;
    }
    refresh(p.msgId);
    // shortcut: simulated workers answer the re-explain Check themselves (right answer); Rahim only when no real phone is paired
    if (p.user !== 'u_rahim' || !Bus.paired) {
      setTimeout(() => {
        Bus.emit('check:answer', {msgId: p.msgId, user: p.user, answers: (m.wk.results[p.user] || {}).answers || [], status: 'green', re: true, reSel: p.question.answer});
        resolveFu(p.msgId, p.user, 'clarified', 'understood');
      }, 3200);
    }
  });
  Bus.on('ask:foreman', p => {
    const arr = DATA.messages[p.thread] || (DATA.messages[p.thread] = []);
    if (arr.some(x => x.id === p.id)) return;
    const msg = {id: p.id, thread: p.thread, from: p.user, at: p.at || hhmm(), kind: 'ask', ja: p.ja, en: p.en, ask: {ref: p.msgId, topic: p.topic, reply: null}};
    arr.push(msg);
    if (msg.from === ME) { if (S.el && S.thread === p.thread) renderList(true); } else chIncoming(msg, `${first(msg.from)}さんから質問: ${p.ja.slice(0, 16)}`);
  });
  Bus.on('ask:foremanReply', p => {
    const m = findMsg(p.id); if (!m || !m.ask || m.ask.reply) return;
    m.ask.reply = {ja: p.ja, en: p.en, at: p.at || hhmm()};
    refresh(p.id);
    if (ROLE === 'worker' && m.from === ME && !(onChat() && S.thread === m.thread)) {
      const th = DATA.threads.find(t => t.id === m.thread); if (th) th.unread = (th.unread || 0) + 1;
      setTimeout(() => App.toast('Tanaka replied to your question', {label: 'Open', run: () => { if (onChat()) SCREENS.chat(S.el, {thread: m.thread}); else App.push('chat', {thread: m.thread}); }}), 0);
    }
  });
  Bus.on('glossary:add', ({ja, en, reading}) => {
    const g = (DATA.glossary || (DATA.glossary = [])).find(x => x.ja === ja);
    if (g) { g.en = en; g.reading = reading || g.reading; } else DATA.glossary.push({ja, en, reading: reading || ''});
    retext();
  });

  // The foreman phone drives the Wakatta pipeline (one stage per ~900ms) and the simulated workers.
  function runPipeline(id) {
    [1, 2, 3, 4, 5].forEach(s => setTimeout(() => {
      Bus.emit('wk:stage', {msgId: id, stage: s});
      if (s === 3) DATA.script.tasks.forEach((t, k) => Bus.emit('task:update', {task: {id: `t_${id}_${k + 1}`, title: t.title, ja: t.ja, col: 'todo', assignees: t.assignees.slice(), due: t.due, machine: t.machine, from: id}}));
      if (s === 5) simulate(id);
    }, 900 * s));
  }
  function simulate(id) {
    const ans = (u, a) => submit(id, u, a);
    const rd = (u, ms) => setTimeout(() => Bus.emit('wk:read', {msgId: id, user: u, at: hhmm()}), ms);
    rd('u_nguyen', 1200); rd('u_siti', 2400);
    if (!Bus.paired) rd('u_rahim', 2000);
    Object.entries(DATA.script.simulated).forEach(([u, s]) => setTimeout(() => ans(u, s.answers), s.after));
    if (!Bus.paired) setTimeout(() => ans('u_rahim', [1, 1, 2]), 8000);   // standalone: no real Rahim phone
  }
})();
