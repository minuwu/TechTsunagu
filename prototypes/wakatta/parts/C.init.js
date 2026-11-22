/* ===== Part C init: App exists now. Light status bar over the dark camera and the solid Lock/Ready panel ===== */
(() => {
  const bar = document.querySelector('.statusbar'), stage = document.getElementById('stage');
  if (!bar || !stage) return;
  let mine = false;
  const sync = () => {
    const top = App.stack[App.stack.length - 1], want = top === 'scan' || top === 'panel';
    if (want && !bar.classList.contains('light')) { bar.classList.add('light'); mine = true; }
    else if (!want && mine) { bar.classList.remove('light'); mine = false; }
  };
  new MutationObserver(sync).observe(stage, {subtree: true, attributes: true, attributeFilter: ['class']});
  sync();
})();
