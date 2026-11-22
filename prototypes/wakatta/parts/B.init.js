/* ===== Part B init: runs after App exists ===== */
(() => {
const FM = ROLE === 'foreman', t = __tr;
const esc = s => String(s).replace(/[&<>"']/g, c => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c]));
const body = document.getElementById('bd-sheet-body');
const workers = Object.keys(DATA.users).filter(u => DATA.users[u].role === 'worker');
const DUES = [['Today', '今日', 'Today'], ['Tomorrow', '明日', 'Tomorrow'], ['Fri', '金曜', 'Friday']];

function build() {
  body.innerHTML = `<div class="bd-sheet"><h2 class="bd-sh">${t('タスクを追加', 'New task')}</h2>
    <input class="field" id="bd-title" autocomplete="off" placeholder="${t('タイトル', 'Title')}" aria-label="${t('タイトル', 'Title')}">
    <div class="bd-lbl">${t('担当', 'Assignees')}</div>
    <div class="row bd-wrap" id="bd-asg">${Object.keys(DATA.users).map(u => `<button class="chip" data-u="${u}">${esc(nameOf(u).split(' ')[0])}</button>`).join('')}</div>
    <div class="bd-lbl">${t('期限', 'Due')}</div>
    <div class="row bd-wrap" id="bd-due">${DUES.map(([v, ja, en], i) => `<button class="chip${i ? '' : ' on'}" data-chip data-due="${v}">${t(ja, en)}</button>`).join('')}</div>
    <button class="btn block" id="bd-save">${t('追加', 'Add task')}</button></div>`;
  const first = workers[0]; if (first) body.querySelector(`[data-u="${first}"]`).classList.add('on');
}
build();

body.addEventListener('click', e => {
  const chip = e.target.closest('#bd-asg .chip');
  if (chip) { chip.classList.toggle('on'); return; }
  if (!e.target.closest('#bd-save')) return;
  const input = document.getElementById('bd-title'), title = input.value.trim();
  if (!title) { input.focus(); App.toast(t('タイトルを入力してください', 'Enter a title first')); return; }
  const as = [...body.querySelectorAll('#bd-asg .chip.on')].map(c => c.dataset.u);
  const due = (body.querySelector('#bd-due .chip.on') || {dataset: {}}).dataset.due || '';
  Bus.emit('task:update', {task: {id: 'k' + Date.now().toString(36), title, ja: title, col: 'todo', assignees: as.length ? as : [ME], due, machine: null, from: null}});
  App.close(); input.value = ''; build();
  App.toast(t('タスクを追加しました', 'Task added'));
});
if (window.__wkB) window.__wkB.badge();
})();
