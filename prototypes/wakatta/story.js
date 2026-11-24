/* Story journey beside the two phones. It listens to the Bus events index.html relays,
   so the story follows what the visitor actually taps, in order or not. app.html is untouched. */
(() => {
'use strict';
const $ = id => document.getElementById(id);
const NAME = {u_rahim: 'Rahim', u_nguyen: 'Nguyen', u_siti: 'Siti'};
const STAGES = ['Transcribed', 'Translated to English', 'Check Questions made', 'Tasks extracted', 'Sent to workers', 'Answers in'];
const CHAPTERS = ['The nod', 'Tanaka speaks', 'Rahim answers', 'Close the gap', 'What changed'];
const PLAN = {ok: ['7:45', 'Lock out the power', 'Die change'], miss: ['7:45', 'Lock out the power', 'Oil change']};
const FRAME = {foreman, worker};

let S;   // story state, rebuilt by reset()
const fresh = () => ({step: 'p0', plan: null, msgId: null, stage: 0, status: {}, read: {}, gap: null, reOk: null, seen: {}, tasks: 0});

/* ---------- steps: copy, choices and what to point at. Events move between them (route below). ---------- */
const jp = (ja, kana, en) => `<span class="jp">${ja}</span> <small>(${kana}${en ? ', "' + en + '"' : ''})</small>`;
const todo = (side, html) => `<div class="todo ${side === 'foreman' ? 'left' : 'right'}"><i aria-hidden="true"></i><span>${html}</span></div>`;
const chat = '.screen[data-screen=chat] ';
const idle = doc => { const c = doc.querySelector(chat + '.ch-comp'); return !c || c.dataset.st === 'idle'; };
const sheetOpen = doc => !!doc.querySelector('#scrim.on');   // "Explain again" sits behind an open sheet
const last = sel => doc => { const a = doc.querySelectorAll(sel); return a[a.length - 1]; };

const STEPS = {
  p0: {ch: 0, pro: true, html: () => `<h2>One word. One afternoon.</h2>
      <p>Mr. Tanaka runs a small metal factory beside the new Nagasaki Stadium City. Rahim-san came from Bangladesh and holds a JLPT certificate.</p>
      <p>One morning, Tanaka gives him an instruction.</p>`,
    bubbles: {F: ['Tanaka · Foreman', '「明日、2号プレスの金型交換をお願いします。」', 'Tomorrow, please change the die on Press #2.']},
    cta: ['What does Rahim say?', 'p1']},
  p1: {ch: 0, pro: true, html: () => `<h2>Rahim nods.</h2>
      <p>${jp('わかりました', 'wakarimashita', 'understood')}. He doesn't want to slow the team down.</p><p><b>You are Tanaka. What do you do?</b></p>`,
    bubbles: {F: ['Tanaka · Foreman', '「明日、2号プレスの金型交換をお願いします。」'], W: ['Rahim · Worker', '「わかりました。」', 'Wakarimashita. (Understood.)']},
    choices: [['Trust the nod', 'He said he understood. Move on.', 'p2'], ['Ask again, out loud', '"Are you sure? Really understood?"', 'p1b']]},
  p1b: {ch: 0, pro: true, html: () => `<h2>He says yes again.</h2>
      <p>Asked in front of colleagues, "I don't understand" feels like the hardest thing to say. Asking louder gets the same answer.</p>`,
    bubbles: {F: ['Tanaka · Foreman', '「本当に大丈夫？」', 'Are you really OK?'], W: ['Rahim · Worker', '「はい、大丈夫です。」', 'Yes, I\'m fine.']},
    cta: ['See what happened', 'p2']},
  p2: {ch: 0, pro: true, html: () => `<h2>By the afternoon, Rahim is injured.</h2>
      <p>He had misread one word: ${jp('金型交換', 'kanagata kōkan', 'die change')}. Nobody was careless, and neither of them knew until it was too late.</p>
      <p>The failure was in the connection between two people. <b>So the fix belongs there too.</b></p>`,
    bubbles: {W: ['Afternoon', 'Rahim is hurt at Press #2.', 'He heard 金型交換 and guessed wrong.']}, hurt: true,
    cta: ['Rewind the morning, with Wakatta', 'speak']},

  speak: {ch: 1, phone: 'foreman', html: () => `<h2>Same morning. This time Tanaka uses Wakatta.</h2>
      <p>He just talks, in Japanese, the way he always does.</p>
      ${todo('foreman', '<b>Tap the mic</b> on Tanaka\'s phone, once.')}`,
    hint: [['foreman', doc => idle(doc) && last(chat + '[data-act="mic"]')(doc)]]},
  listening: {ch: 1, phone: 'foreman', html: () => `<h2>Tanaka is talking.</h2>
      <p>Watch the Japanese appear as he speaks. Then a 4-second ring gives him time to stop or edit before anything is sent.</p>
      <p class="fine">Nothing to tap: just watch.</p>`},
  pipeline: {ch: 1, html: () => `<h2>Wakatta is working.</h2>
      <p>One voice message becomes clear English for Rahim, <b>3 short Check Questions</b>, and <b>${S.tasks || 3} Tasks</b> on the Board.</p>
      <p class="fine">Watch the pipeline on the right. No one had to copy and paste into a translation app.</p>`},

  answer: {ch: 2, phone: 'worker', html: () => S.plan ? `<h2>${S.plan === 'ok' ? 'Answer as Rahim who understood.' : 'Replay the real mistake.'}</h2>
      ${todo('worker', S.plan === 'ok' ? 'Tap <b>7:45</b>, then <b>Lock out the power</b>, then <b>Die change</b>.' : 'Tap <b>7:45</b>, then <b>Lock out the power</b>, then <b>Oil change</b>. That is the word Rahim misread. Then tap <b>Finish</b>.')}
      <p class="fine">Or answer any way you like. The story follows what you do.</p>`
      : `<h2>Now you are Rahim.</h2>
      <p>The message arrived in English, with the shop word kept: ${jp('金型交換', 'kanagata kōkan')}. Under it, Wakatta asks <b>3 quick questions, privately</b>.</p>
      <p><b>How does Rahim answer?</b></p>`,
    choices: () => S.plan ? null : [['Rahim understood', 'All three right.', () => setPlan('ok')], ['Rahim misread 金型交換', 'Like this morning: he thinks it means "oil change".', () => setPlan('miss')]],
    // Next/Finish first: after a wrong pick the picked option still matches the plan
    hint: [['worker', last(chat + '[data-act="nscont"]')],
           ['worker', doc => S.plan && [...doc.querySelectorAll(chat + '[data-act="pick"]')].find(b => PLAN[S.plan].includes(b.textContent.trim()))]]},

  green: {ch: 3, phone: 'foreman', html: () => `<h2>Green. Tanaka knows, without asking.</h2>
      <p>Rahim's dot turned green on Tanaka's phone. "Understood" now means understood.</p>
      ${S.status.u_siti && S.status.u_siti !== 'green' ? `<p>Look at <b>Siti</b>: she missed a step. Wakatta caught it and opened a Follow-up for her, privately.</p>
      ${todo('foreman', 'Optional: tap <b>もう一度説明する</b> (Explain again) for Siti.')}` : ''}`,
    hint: [['foreman', last('[data-act="rxsend"]')], ['foreman', doc => !sheetOpen(doc) && last(chat + '[data-act="again"][data-u="u_siti"]')(doc)], ['foreman', last(chat + '[data-act="wk"]')]],
    cta: ['See what changed', 'end']},
  gap: {ch: 3, phone: 'foreman', html: () => `<h2>Caught before the press moved.</h2>
      <p>Rahim never had to say "I don't understand" out loud. Wakatta noticed the missed word${S.gap ? `, <b>${S.gap.term || '金型交換'}</b> (he chose "${S.gap.wrong}")` : ''}, and told only Tanaka.</p>
      ${todo('foreman', 'Tap <b>もう一度説明する</b> (Explain again) for Rahim, then <b>Send</b>. The AI drafts a simpler explanation of that one word. Nothing goes out until Tanaka taps Send.')}`,
    hint: [['foreman', last('[data-act="rxsend"]')], ['foreman', doc => !sheetOpen(doc) && last(chat + '[data-act="again"][data-u="u_rahim"]')(doc)]]},
  stillgap: {ch: 3, phone: 'foreman', html: () => `<h2>Still not quite, and that's fine.</h2>
      <p>Nothing happened at the machine. Tanaka can explain once more, in other words.</p>
      ${todo('foreman', 'Tap <b>もう一度説明する</b> for Rahim again, then <b>Send</b>.')}`,
    hint: [['foreman', last('[data-act="rxsend"]')], ['foreman', doc => !sheetOpen(doc) && last(chat + '[data-act="again"][data-u="u_rahim"]')(doc)]]},
  reask: {ch: 3, phone: 'worker', html: () => `<h2>A Re-explanation, just for Rahim.</h2>
      <p>Simple words and a picture: a die is the shaped tool on the press. Then one new question.</p>
      ${todo('worker', `Read it, then tap <b>${S.reOk || 'the right answer'}</b>.`)}`,
    hint: [['worker', doc => [...doc.querySelectorAll('[data-act="rpick"]')].filter(b => b.textContent.trim() === S.reOk).pop()]]},
  fixed: {ch: 3, phone: 'foreman', html: () => `<h2>Now Rahim really understands.</h2>
      <p>The orange dot turned green. The Follow-up closed itself. No one was embarrassed, and nothing was guessed.</p>`,
    cta: ['See what changed', 'end']},

  end: {ch: 4, html: () => `<h2>From "wakarimashita" to <i>wakatta</i>.</h2>
      <p><b>Understanding is confirmed</b>, not assumed: 3 quick questions, private answers, green at a glance.</p>
      <p><b>Dignity is kept</b>: nobody had to say "I don't understand" in front of the team.</p>
      <p><b>Gaps get repaired</b>: a Follow-up and a simpler Re-explanation of the exact word missed.</p>
      <p><b>People stay in control</b>: on our tests the AI passed 30 of 30 cases for machine notes, notices and worker questions, but only 7 of 10 chat drafts were fully correct. So Tanaka sees every draft first and can stop or edit it.</p>
      <p class="fine">Keep tapping: the phones stay live, and the right side keeps narrating.</p>`,
    choices: () => [[S.seen.gap ? 'Replay: Rahim understood' : 'Replay: Rahim misread 金型交換', 'Same morning, the other answer.', () => { const p = S.seen.gap ? 'ok' : 'miss'; reset('speak'); S.plan = p; }],
      ['Start over from the nod', 'The full story again.', () => reset('p0')]]},
};
const ORDER = Object.keys(STEPS);
// Nekko narrates: its face mirrors the moment (missing = neutral)
const FACE = {p1: 'worried', p1b: 'worried', p2: 'worried', gap: 'surprised', stillgap: 'worried', green: 'happy', fixed: 'happy', end: 'happy'};

/* ---------- routing: real Bus events decide where the story goes ---------- */
function route(t, p) {
  const rahim = p && p.user === 'u_rahim';
  if (t === 'msg:new' && p.msg && p.msg.wk && !S.msgId) { S.msgId = p.msg.id; return 'pipeline'; }
  if (t === 'wk:stage' && p.stage >= 5 && S.step === 'pipeline') return 'answer';
  if (t === 'check:answer' && rahim && !p.re) { S.seen[p.status === 'green' ? 'ok' : 'gap'] = 1; return p.status === 'green' ? 'green' : 'gap'; }
  if (t === 'followup:new' && rahim && !S.gap) { S.gap = p; render(); }
  if (t === 'wk:reexplain' && rahim) { S.reOk = p.question && p.question.options ? p.question.options[p.question.answer] : null; return 'reask'; }
  if (t === 'check:answer' && rahim && p.re) return p.status === 'green' ? 'fixed' : 'stillgap';
}

/* ---------- right rail: pipeline, Wakatta Status and a plain-English feed ---------- */
function feed(text) {
  const li = document.createElement('li');
  li.innerHTML = `<span>${new Date().toTimeString().slice(0, 5)}</span>${text}`;
  $('feed').prepend(li);
}
const WORD = {green: 'understood', orange: 'partly understood', red: 'did not understand', wait: 'waiting'};
function observe(t, p) {
  const n = p && NAME[p.user];
  if (t === 'msg:new' && p.msg && p.msg.wk) feed('Tanaka sent a voice message in Japanese');
  else if (t === 'wk:stage' && p.msgId === S.msgId) { S.stage = Math.max(S.stage, p.stage); feed(STAGES[p.stage - 1]); }
  else if (t === 'task:update' && p.task && p.task.from && p.task.from === S.msgId && !S.seen[p.task.id]) { S.seen[p.task.id] = 1; S.tasks++; feed(`Task on the Board: ${p.task.title}`); }
  else if (t === 'wk:read' && n && !S.read[p.user]) { S.read[p.user] = 1; feed(`${n} opened it`); }
  else if (t === 'check:answer' && n) { S.status[p.user] = p.status; feed(`<b>${n}</b>: ${WORD[p.status] || p.status}${p.re ? ' after the Re-explanation' : ''}`); }
  else if (t === 'followup:new' && n) feed(`Follow-up opened for ${n}: ${p.term || 'a missed step'}`);
  else if (t === 'wk:reexplain' && n) feed(`Re-explanation sent to ${n}, in simpler words`);
  else if (t === 'followup:resolve' && p.status === 'understood') feed('Follow-up closed: understood');
  else return;
  drawRail();
}
function drawRail() {
  $('pipe').innerHTML = STAGES.map((s, i) => `<li class="${i < S.stage || (i === 5 && Object.keys(S.status).length) ? 'on' : ''}">${s}</li>`).join('');
  $('who').innerHTML = Object.keys(NAME).map(u => { const st = S.status[u] || (S.read[u] ? 'wait' : ''); return `<li><span class="dot ${st}"></span>${NAME[u]}<em>${st ? WORD[st] : S.msgId ? 'not opened yet' : '—'}</em></li>`; }).join('');
}

/* ---------- left rail ---------- */
function render() {
  const st = STEPS[S.step], ch = st.ch;
  $('prog').innerHTML = CHAPTERS.map((c, i) => `<button class="${i < ch ? 'done' : i === ch ? 'now' : ''}" aria-label="Chapter ${i + 1}: ${c}" ${i < 2 ? `data-go="${i ? 'speak' : 'p0'}"` : 'disabled'}></button>`).join('');
  $('chap').textContent = `Chapter ${ch + 1} of ${CHAPTERS.length} · ${CHAPTERS[ch]}`;
  const face = `nekko/nekko-${FACE[S.step] || 'neutral'}.svg`, img = $('nekko');
  if (!img.src.endsWith(face)) { img.src = face; img.classList.remove('pop'); void img.offsetWidth; img.classList.add('pop'); }
  const choices = typeof st.choices === 'function' ? st.choices() : st.choices;
  $('story').innerHTML = st.html() +
    (choices ? `<div class="choices">${choices.map((c, i) => `<button class="choice" data-c="${i}"><b>${c[0]}</b><small>${c[1]}</small></button>`).join('')}</div>` : '') +
    (st.cta ? `<button class="cta" data-cta>${st.cta[0]}</button>` : '');
  $('story').onclick = e => {
    const c = e.target.closest('[data-c]'), cta = e.target.closest('[data-cta]');
    if (c) { const go = choices[+c.dataset.c][2]; typeof go === 'function' ? go() : goto(go); }
    if (cta) goto(st.cta[1]);
  };
  // prologue scene: speech bubbles over dimmed phones
  $('stage').classList.toggle('pro', !!st.pro);
  [['F', 'bF'], ['W', 'bW']].forEach(([k, id]) => {
    const b = st.bubbles && st.bubbles[k], el = $(id);
    el.classList.toggle('show', !!b); el.classList.toggle('hurt', !!st.hurt && k === 'W');
    el.innerHTML = b ? `<div class="who-s">${b[0]}</div>${b[1]}${b[2] ? `<small>${b[2]}</small>` : ''}` : '';
  });
  $('pF').classList.toggle('act', st.phone === 'foreman');
  $('pW').classList.toggle('act', st.phone === 'worker');
  ['factsCard'].forEach(id => $(id).hidden = !(st.pro || S.step === 'end'));
  ['pipeCard', 'whoCard', 'feedCard'].forEach(id => $(id).hidden = !!st.pro);
  drawRail();
}
function goto(id) {
  if (!STEPS[id] || id === S.step) return;
  S.step = id; render();
  const h = $('story').querySelector('h2'); if (h) { h.tabIndex = -1; h.focus({preventScroll: true}); }
}
function setPlan(p) { S.plan = p; render(); }

/* ---------- hints inside the phones (same-origin only; otherwise the text and phone glow guide) ---------- */
const CSS = `.story-hint{outline:3px solid #f59e0b!important;outline-offset:-3px;animation:story-hint 1.2s ease-out infinite}
@keyframes story-hint{0%{box-shadow:0 0 0 0 rgba(245,158,11,.55)}100%{box-shadow:0 0 0 14px rgba(245,158,11,0)}}
@media (prefers-reduced-motion:reduce){.story-hint{animation:none}}`;
const docOf = f => { try { return FRAME[f].contentDocument; } catch (e) { return null; } };   // cross-origin (file://) => no inner hints
let cur = null;
function findHint() {
  for (const [f, pick] of (STEPS[S.step].hint || [])) {
    const d = docOf(f), el = d && pick(d);
    if (el && el.getClientRects().length) return el;
  }
  return null;
}
setInterval(() => {
  const d = docOf('foreman');
  if (S.step === 'speak' && d && !idle(d)) goto('listening');   // dictation started: stop asking for the tap
  const el = findHint();
  $('doIt').hidden = !el;
  if (el === cur) return;
  if (cur) cur.classList.remove('story-hint');
  cur = el;
  if (el) { el.classList.add('story-hint'); reveal(el); }
}, 400);
// scrollIntoView would also scroll the app shell; scroll only the nearest scrolling list
function reveal(el) {
  const cs = n => el.ownerDocument.defaultView.getComputedStyle(n).overflowY;
  let p = el.parentElement;
  while (p && !(p.scrollHeight > p.clientHeight && /auto|scroll/.test(cs(p)))) p = p.parentElement;
  if (!p) return;
  const r = el.getBoundingClientRect(), pr = p.getBoundingClientRect();
  if (r.top < pr.top || r.bottom > pr.bottom) p.scrollBy({top: r.top - pr.top - (pr.height - r.height) / 2, behavior: 'smooth'});
}
$('doIt').onclick = () => { const el = findHint(); if (el) el.click(); };

/* ---------- reset: reload both phones straight into the Line 2 chat ---------- */
function load(f) {
  FRAME[f].onload = () => { const d = docOf(f); if (d) { const s = d.createElement('style'); s.textContent = CSS; d.head.append(s); } };
  FRAME[f].src = `${APP}?role=${f}&embed=1&v=${Date.now()}#chats/chat`;
}
function reset(step) {
  S = fresh(); cur = null;
  $('feed').innerHTML = '';
  load('foreman'); load('worker');
  S.step = step; render();
}
$('restart').onclick = () => reset('p0');
$('prog').onclick = e => { const b = e.target.closest('[data-go]'); if (b) reset(b.dataset.go); };

addEventListener('message', e => {
  if (!e.data || !e.data.wk) return;
  const {t, p} = e.data;
  observe(t, p);
  const next = route(t, p);
  // a jump only moves forward from the prologue: the phones are inactive there
  if (next && !STEPS[S.step].pro) goto(next);
});

console.assert(ORDER.every(k => STEPS[k].html), 'every step has copy');
reset('p0');
})();
