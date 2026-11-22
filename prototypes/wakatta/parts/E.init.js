/* ===== Part E init: runs after App exists ===== */
(() => {
if (window.uxBoot) window.uxBoot();
// onboarding is NOT shown by default (it would get in the way of other tests): only with ?onboard=1, or Me -> Replay onboarding
if (QS.has('onboard')) setTimeout(() => window.uxOpenOnboarding && window.uxOpenOnboarding(), 700);
})();
