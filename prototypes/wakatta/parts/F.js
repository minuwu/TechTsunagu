/* ===== Part F: safety course, Rover Villa "four doors" (prefix cs-) =====
   Screen: course. Progress: localStorage rv:course_progress:v1 (same key as the React app).
   Content and strings below are generated from frontend/src/domain/courseContent.ts and src/i18n (course_*). */
(() => {
'use strict';
const D = {"course":{"title":{"ja":"玉掛け","en":"Crane slinging","bn":"ক্রেনে স্লিং বাঁধা"},"sub":{"ja":"吊り荷を安全に掛けて、合図して、吊る。","en":"Rig a load, signal, and lift it safely.","bn":"মাল নিরাপদে বাঁধা, ইশারা দেওয়া ও তোলা।"}},"panels":[{"who":"foreman","text":{"ja":"吊{つ}り荷{に}の下{した}に入{はい}るな！","en":"Never stand under a hanging load!","bn":"ঝুলন্ত মালের নিচে কখনো দাঁড়াবে না!"}},{"who":"worker","text":{"ja":"…わかりました。でも、ワイヤーの角度{かくど}は？","en":"…Understood. But what about the wire angle?","bn":"…বুঝেছি। কিন্তু তারের কোণ কত হবে?"}},{"who":"foreman","text":{"ja":"60度以内{どいない}。広{ひろ}げるほど、ワイヤーに力{ちから}がかかる。","en":"Within 60°. The wider it opens, the more force on the wire.","bn":"৬০° এর মধ্যে। যত বেশি ফাঁক, তারে তত বেশি টান।"}}],"handbook":[{"ja":"玉掛{たまが}けは、決{き}められた資格{しかく}のある人{ひと}だけが行{おこな}う。","en":"Only a qualified person may sling loads.","bn":"শুধু যোগ্যতাপ্রাপ্ত ব্যক্তি স্লিং বাঁধতে পারবেন।"},{"ja":"吊{つ}り荷{に}の下{した}への立入{たちいり}は禁止{きんし}。","en":"Entering the area under a suspended load is prohibited.","bn":"ঝুলন্ত মালের নিচে ঢোকা নিষেধ।"},{"ja":"使用前{しようまえ}にワイヤーロープを点検{てんけん}する。","en":"Inspect the wire rope before every use.","bn":"প্রতিবার ব্যবহারের আগে ওয়্যার রোপ পরীক্ষা করবে।"}],"reel":[{"ja":"主電源{しゅでんげん}、OFF、ヨシ！","en":"Main switch OFF, yoshi!","bn":"মেইন সুইচ বন্ধ, ঠিক আছে!"},{"ja":"ワイヤー、傷{きず}なし、ヨシ！","en":"Wire, no damage, yoshi!","bn":"তার অক্ষত, ঠিক আছে!"},{"ja":"吊{つ}り角度{かくど}、60度以内{どいない}、ヨシ！","en":"Sling angle within 60°, yoshi!","bn":"কোণ ৬০° এর মধ্যে, ঠিক আছে!"},{"ja":"吊{つ}り荷{に}の下{した}、人{ひと}なし、ヨシ！","en":"Nobody under the load, yoshi!","bn":"মালের নিচে কেউ নেই, ঠিক আছে!"},{"ja":"合図{あいず}、巻上{まきあ}げ、ヨシ！","en":"Signal, hoist, yoshi!","bn":"ইশারা, তোলো, ঠিক আছে!"}],"drills":[{"id":"kyt","kind":"spot","secs":30,"need":4,"name":{"ja":"危険予知（KYT）","en":"Hazard spotting (KYT)","bn":"ঝুঁকি চেনা (KYT)"},"goal":{"ja":"30秒で危険を4つ見つける。","en":"Find 4 hazards in 30 seconds.","bn":"৩০ সেকেন্ডে ৪টি বিপদ খুঁজে বের করো।"},"items":[{"hazard":true,"label":{"ja":"床の油","en":"Oil on the floor","bn":"মেঝেতে তেল"}},{"hazard":false,"label":{"ja":"消火器","en":"Fire extinguisher","bn":"অগ্নিনির্বাপক"}},{"hazard":true,"label":{"ja":"通路のケーブル","en":"Cable across the walkway","bn":"পথে ছড়ানো তার"}},{"hazard":false,"label":{"ja":"工具棚","en":"Tool rack","bn":"টুল র‍্যাক"}},{"hazard":true,"label":{"ja":"カバーのない回転部","en":"Moving part with no guard","bn":"গার্ডহীন ঘূর্ণায়মান অংশ"}},{"hazard":false,"label":{"ja":"きれいな通路","en":"Clear walkway","bn":"পরিষ্কার পথ"}},{"hazard":true,"label":{"ja":"吊り荷の下の人","en":"Person under a hanging load","bn":"ঝুলন্ত মালের নিচে মানুষ"}},{"hazard":true,"label":{"ja":"ヘルメットなし","en":"No helmet","bn":"হেলমেট নেই"}}]},{"id":"sling","kind":"sling","loadKg":1000,"wllKg":600,"name":{"ja":"吊り角度と張力","en":"Sling angle and tension","bn":"স্লিং কোণ ও টান"},"goal":{"ja":"ワイヤー1本の限度（600kgf）を超えない角度にする。","en":"Keep each wire under its 600 kgf limit.","bn":"প্রতিটি তারের সীমা (৬০০ কেজি-বল) পার না করে কোণ ঠিক করো।"}},{"id":"estop","kind":"reflex","name":{"ja":"非常停止の反射","en":"E-stop reflex","bn":"ইমার্জেন্সি স্টপ রিফ্লেক্স"},"goal":{"ja":"巻き込まれ！0.4秒以内に非常停止を押す。","en":"Entanglement! Hit E-stop within 0.4 s.","bn":"জড়িয়ে যাচ্ছে! ০.৪ সেকেন্ডের মধ্যে ই-স্টপ চাপো।"}},{"id":"ring","kind":"ring","wheels":[true,false,true],"name":{"ja":"砥石の打音点検","en":"Grinder wheel ring test","bn":"গ্রাইন্ডার হুইলের শব্দ পরীক্ষা"},"goal":{"ja":"叩いて音を聞く。澄んだ音は良品、鈍い音はひび割れ。","en":"Tap and listen. A clear ring is sound; a dull thud is cracked.","bn":"টোকা দিয়ে শোনো। পরিষ্কার শব্দ ভালো, ভোঁতা শব্দ মানে ফাটা।"}},{"id":"ppe","name":{"ja":"保護具の装着","en":"PPE checklist","bn":"পিপিই চেকলিস্ট"},"goal":{"ja":"正しい順番で装着する。","en":"Put on in the right order.","bn":"সঠিক ক্রমে পরো।"},"kind":"seq","steps":[{"ja":"ヘルメット、あごひも","en":"Helmet, chin strap","bn":"হেলমেট, চিনস্ট্র্যাপ"},{"ja":"安全帯のバックル確認","en":"Harness, check buckles","bn":"হারনেসের বাকল পরীক্ষা"},{"ja":"安全靴","en":"Safety shoes","bn":"সেফটি জুতা"},{"ja":"手袋","en":"Gloves","bn":"গ্লাভস"}]},{"id":"confined","name":{"ja":"酸素濃度とロックアウト","en":"Confined space: O2 and LOTO","bn":"বদ্ধ স্থান: অক্সিজেন ও লক-আউট"},"goal":{"ja":"入る前の手順を正しい順番で。","en":"Do the pre-entry steps in order.","bn":"ঢোকার আগের ধাপগুলো ক্রমে করো।"},"kind":"seq","steps":[{"ja":"バルブをロックアウト・タグアウト","en":"Lock out and tag out the valve","bn":"ভাল্ব লক-আউট ও ট্যাগ-আউট"},{"ja":"ガス検知器を校正","en":"Calibrate the gas detector","bn":"গ্যাস ডিটেক্টর ক্যালিব্রেট"},{"ja":"換気する","en":"Ventilate","bn":"বাতাস চলাচল করাও"},{"ja":"酸素18%以上を確認","en":"Confirm oxygen is 18% or more","bn":"অক্সিজেন ১৮% বা বেশি নিশ্চিত করো"}]},{"id":"scaffold","name":{"ja":"移動式足場の設置","en":"Rolling scaffold setup","bn":"চলমান স্ক্যাফোল্ড স্থাপন"},"goal":{"ja":"登る前の手順を正しい順番で。","en":"Do the steps before climbing, in order.","bn":"ওঠার আগের ধাপগুলো ক্রমে করো।"},"kind":"seq","steps":[{"ja":"地面が平らか確認","en":"Check the ground is level","bn":"মাটি সমতল কিনা দেখো"},{"ja":"キャスターのブレーキをかける","en":"Lock the caster brakes","bn":"চাকার ব্রেক লাগাও"},{"ja":"アウトリガーを張り出す","en":"Extend the outriggers","bn":"আউটট্রিগার বের করো"},{"ja":"手すりを確認して登る","en":"Check handrails, then climb","bn":"হ্যান্ডরেল দেখে তারপর ওঠো"}]},{"id":"welder","name":{"ja":"アーク溶接のアース","en":"Arc welder grounding","bn":"আর্ক ওয়েল্ডার আর্থিং"},"goal":{"ja":"溶接前の手順を正しい順番で。","en":"Do the steps before welding, in order.","bn":"ওয়েল্ডিংয়ের আগের ধাপগুলো ক্রমে করো।"},"kind":"seq","steps":[{"ja":"電源OFFを確認","en":"Confirm power is OFF","bn":"পাওয়ার বন্ধ নিশ্চিত করো"},{"ja":"アースを母材にしっかり接続","en":"Clamp the earth firmly to the workpiece","bn":"আর্থ ক্ল্যাম্প ওয়ার্কপিসে শক্ত করে লাগাও"},{"ja":"ケーブルの傷を点検","en":"Inspect the cable for damage","bn":"ক্যাবলে ক্ষত আছে কিনা দেখো"},{"ja":"電源ON","en":"Switch power ON","bn":"পাওয়ার চালু করো"}]},{"id":"evac","name":{"ja":"津波・火災の避難","en":"Tsunami / fire evacuation","bn":"সুনামি / আগুনে সরে যাওয়া"},"goal":{"ja":"警報から集合までを正しい順番で。","en":"From alarm to rally point, in order.","bn":"অ্যালার্ম থেকে জমায়েত পর্যন্ত ক্রমে।"},"kind":"seq","steps":[{"ja":"機械を止める","en":"Stop the machine","bn":"মেশিন বন্ধ করো"},{"ja":"荷物を持たず出口へ","en":"Leave belongings, go to the exit","bn":"জিনিস ফেলে বের হও"},{"ja":"高台へ向かう","en":"Move to high ground","bn":"উঁচু জায়গায় যাও"},{"ja":"集合場所で点呼","en":"Roll call at the rally point","bn":"জমায়েত স্থানে হাজিরা"}]}],"inspect":[{"id":"harness","defective":false,"name":{"ja":"安全帯","en":"Full-body harness","bn":"ফুল-বডি হারনেস"},"spots":[{"x":22,"y":30,"check":{"ja":"ベルトのほつれ・切れ","en":"Frayed or cut webbing","bn":"ফিতা ছেঁড়া বা কাটা"}},{"x":74,"y":30,"check":{"ja":"バックルの動き・変形","en":"Buckle moves freely, not bent","bn":"বাকল স্বাভাবিক, বাঁকা নয়"}}]},{"id":"wheel","defective":true,"name":{"ja":"研削砥石","en":"Grinder wheel","bn":"গ্রাইন্ডার হুইল"},"spots":[{"x":38,"y":24,"check":{"ja":"ひび・欠け","en":"Cracks or chips","bn":"ফাটল বা ভাঙা অংশ"}},{"x":66,"y":36,"check":{"ja":"フランジの締まり","en":"Flange tight and flat","bn":"ফ্ল্যাঞ্জ শক্ত ও সমতল"}}]},{"id":"clamp","defective":true,"name":{"ja":"足場用クランプ","en":"Scaffold clamp","bn":"স্ক্যাফোল্ড ক্ল্যাম্প"},"spots":[{"x":30,"y":30,"check":{"ja":"ボルトの曲がり","en":"Bolt straight, not bent","bn":"বোল্ট সোজা, বাঁকা নয়"}},{"x":70,"y":30,"check":{"ja":"本体の変形・さび","en":"Body not deformed or rusted","bn":"বডি বিকৃত বা মরচে ধরা নয়"}}]}],"tx":{"course_nav":{"en":"Safety course","ja":"安全コース","bn":"নিরাপত্তা কোর্স"},"course_sub":{"en":"One safety skill, four doors. Finish all four to earn your passport stamp.","ja":"安全スキルを4つの入口で学びます。4つ終えるとパスポートにスタンプ。","bn":"একটি নিরাপত্তা দক্ষতা, চারটি দরজা। চারটি শেষ করলে পাসপোর্টে স্ট্যাম্প।"},"course_door_read":{"en":"Read","ja":"読む","bn":"পড়ো"},"course_door_watch":{"en":"Watch","ja":"見る","bn":"দেখো"},"course_door_play":{"en":"Play","ja":"練習","bn":"খেলো"},"course_door_inspect":{"en":"Inspect","ja":"点検","bn":"পরীক্ষা"},"course_door_read_sub":{"en":"Comic story or handbook","ja":"マンガまたはハンドブック","bn":"কমিক গল্প বা হ্যান্ডবুক"},"course_door_watch_sub":{"en":"Pointing-and-calling reel","ja":"指差呼称のリール","bn":"আঙুল দেখিয়ে বলার রিল"},"course_door_play_sub":{"en":"Nine short drills","ja":"9つの短い練習","bn":"নয়টি ছোট অনুশীলন"},"course_door_inspect_sub":{"en":"Check equipment, then pass or reject","ja":"道具を見て、良品か不良品か判断","bn":"সরঞ্জাম দেখে পাস বা রিজেক্ট"},"course_open":{"en":"Open","ja":"開く","bn":"খোলো"},"course_back":{"en":"Back to the course","ja":"コースに戻る","bn":"কোর্সে ফিরে যাও"},"course_done":{"en":"Done","ja":"完了","bn":"শেষ"},"course_passport_title":{"en":"Skill Passport","ja":"スキルパスポート","bn":"স্কিল পাসপোর্ট"},"course_passport_earned":{"en":"Stamped: you finished all four doors.","ja":"スタンプ済み：4つすべて完了しました。","bn":"স্ট্যাম্প পেয়েছ: চারটি দরজাই শেষ।"},"course_passport_locked":{"en":"Finish all four doors to earn the stamp.","ja":"4つすべて終えるとスタンプがもらえます。","bn":"চারটি দরজা শেষ করলে স্ট্যাম্প পাবে।"},"course_reset":{"en":"Start over","ja":"最初からやり直す","bn":"আবার শুরু করো"},"course_mode_comic":{"en":"Comic","ja":"マンガ","bn":"কমিক"},"course_mode_handbook":{"en":"Handbook","ja":"ハンドブック","bn":"হ্যান্ডবুক"},"course_furigana":{"en":"Furigana","ja":"ふりがな","bn":"ফুরিগানা"},"course_translation":{"en":"Translation","ja":"翻訳","bn":"অনুবাদ"},"course_finish_read":{"en":"I read it","ja":"読みました","bn":"পড়া হয়েছে"},"course_speed":{"en":"Slow (0.75×)","ja":"ゆっくり（0.75×）","bn":"ধীরে (০.৭৫×)"},"course_play":{"en":"Play","ja":"再生","bn":"চালাও"},"course_pause":{"en":"Pause","ja":"一時停止","bn":"থামাও"},"course_replay":{"en":"Replay","ja":"もう一度","bn":"আবার চালাও"},"course_watch_hint":{"en":"Say each line aloud and point, like in the reel.","ja":"動画のように、声に出して指差しましょう。","bn":"রিলের মতো জোরে বলো ও আঙুল দেখাও।"},"course_drills_passed":{"en":"{n} of {total} drills passed (need {need})","ja":"{total}個中{n}個合格（必要：{need}個）","bn":"{total}টির মধ্যে {n}টি পাস ({need}টি দরকার)"},"course_drill_pass":{"en":"Passed","ja":"合格","bn":"পাস"},"course_drill_fail":{"en":"Not yet","ja":"もう少し","bn":"এখনও না"},"course_retry":{"en":"Try again","ja":"もう一度","bn":"আবার চেষ্টা করো"},"course_start":{"en":"Start","ja":"開始","bn":"শুরু"},"course_check":{"en":"Check","ja":"確認","bn":"যাচাই"},"course_time_left":{"en":"{n} s left","ja":"残り{n}秒","bn":"{n} সেকেন্ড বাকি"},"course_spot_found":{"en":"Hazards found: {n}","ja":"見つけた危険：{n}","bn":"পাওয়া বিপদ: {n}"},"course_reflex_idle":{"en":"Wait for the alarm, then hit E-stop.","ja":"警報が鳴ったら非常停止を押す。","bn":"অ্যালার্ম বাজলে ই-স্টপ চাপো।"},"course_reflex_wait":{"en":"Wait…","ja":"待って…","bn":"অপেক্ষা করো…"},"course_reflex_go":{"en":"NOW!","ja":"今！","bn":"এখনই!"},"course_reflex_early":{"en":"Too early. Wait for the alarm.","ja":"早すぎ。警報を待って。","bn":"বড্ড আগে। অ্যালার্মের জন্য অপেক্ষা করো।"},"course_reflex_result":{"en":"{ms} ms (limit {limit} ms)","ja":"{ms}ミリ秒（基準{limit}ミリ秒）","bn":"{ms} মি.সে. (সীমা {limit} মি.সে.)"},"course_estop":{"en":"E-STOP","ja":"非常停止","bn":"ই-স্টপ"},"course_sling_angle":{"en":"Angle between wires: {deg}°","ja":"ワイヤーの角度：{deg}°","bn":"তারের মধ্যে কোণ: {deg}°"},"course_sling_tension":{"en":"Tension per wire: {kg} kgf","ja":"ワイヤー1本の張力：{kg}kgf","bn":"প্রতি তারে টান: {kg} কেজি-বল"},"course_sling_limit":{"en":"Limit {kg} kgf","ja":"限度 {kg}kgf","bn":"সীমা {kg} কেজি-বল"},"course_sling_lift":{"en":"Lift","ja":"吊る","bn":"তোলো"},"course_sling_ok":{"en":"Safe lift.","ja":"安全に吊れます。","bn":"নিরাপদে তোলা গেছে।"},"course_sling_bad":{"en":"Over the limit: the wire could snap.","ja":"限度超え：ワイヤーが切れるおそれ。","bn":"সীমা ছাড়িয়েছে: তার ছিঁড়ে যেতে পারে।"},"course_ring_tap":{"en":"Tap wheel {n}","ja":"砥石{n}を叩く","bn":"হুইল {n} এ টোকা দাও"},"course_ring_ok":{"en":"Sound","ja":"良品","bn":"ভালো"},"course_ring_reject":{"en":"Cracked","ja":"ひび割れ","bn":"ফাটা"},"course_sound_note":{"en":"Turn the sound on.","ja":"音をオンにしてください。","bn":"শব্দ চালু করো।"},"course_seq_hint":{"en":"Tap the steps in the right order.","ja":"正しい順番でタップ。","bn":"সঠিক ক্রমে ট্যাপ করো।"},"course_mistakes":{"en":"Mistakes: {n}","ja":"まちがい：{n}","bn":"ভুল: {n}"},"course_inspect_explode":{"en":"Exploded view","ja":"分解表示","bn":"খুলে দেখা"},"course_inspect_tap":{"en":"Tap each dot to check that point.","ja":"各点をタップして確認。","bn":"প্রতিটি বিন্দুতে ট্যাপ করে দেখো।"},"course_inspect_checked":{"en":"Checked {n} of {total} points","ja":"{total}点中{n}点を確認","bn":"{total}টির মধ্যে {n}টি দেখা হয়েছে"},"course_inspect_pass":{"en":"Pass","ja":"良品","bn":"পাস"},"course_inspect_reject":{"en":"Reject","ja":"不良品","bn":"রিজেক্ট"},"course_inspect_right":{"en":"Correct.","ja":"正解です。","bn":"ঠিক আছে।"},"course_inspect_wrong":{"en":"Not correct. Look again.","ja":"違います。もう一度見て。","bn":"ঠিক নয়। আবার দেখো।"}}};
const L = o => o[LANG] || o.ja;
const tx = (k, v = {}) => (D.tx['course_' + k][LANG] || D.tx['course_' + k].en).replace(/\{(\w+)\}/g, (_, n) => v[n]);
const esc = s => String(s == null ? '' : s).replace(/[&<>"']/g, c => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c]));
const plain = s => s.replace(/\{[^}]*\}/g, '');
const ruby = (s, on) => s.split(/([一-鿿々]+\{[^}]+\})/).filter(Boolean).map(p => {
  const r = /^(.+)\{(.+)\}$/.exec(p);
  return r ? (on ? `<ruby>${esc(r[1])}<rt>${esc(r[2])}</rt></ruby>` : esc(r[1])) : esc(p);
}).join('');
const KEY = 'rv:course_progress:v1', NEED = 3, REFLEX_MS = 400, HZ = {intact: 1200, cracked: 320};
const DOORS = ['read', 'watch', 'play', 'inspect'];
const EMPTY = () => ({read: false, watch: false, inspect: false, drills: []});
const load = () => { try { const p = JSON.parse(localStorage.getItem(KEY)); return {read: p.read === true, watch: p.watch === true, inspect: p.inspect === true, drills: Array.isArray(p.drills) ? p.drills.filter(x => typeof x === 'string') : []}; } catch (e) { return EMPTY(); } };
const save = p => localStorage.setItem(KEY, JSON.stringify(p));
const done = (p, d) => d === 'play' ? p.drills.length >= NEED : p[d];
const passport = p => DOORS.every(d => done(p, d));
const tension = (kg, deg) => kg / 2 / Math.cos(deg * Math.PI / 360);
const shuf = n => [...Array(n).keys()].sort(() => Math.random() - .5);
// shortcut: Web Audio chime/thud, no sound files; needs a user gesture and sound on
let AC; const tone = (hz, secs, gain = .25) => { AC = AC || new (window.AudioContext || window.webkitAudioContext)(); AC.resume(); const o = AC.createOscillator(), g = AC.createGain(); o.frequency.value = hz; g.gain.setValueAtTime(gain, AC.currentTime); g.gain.exponentialRampToValueAtTime(.001, AC.currentTime + secs); o.connect(g).connect(AC.destination); o.start(); o.stop(AC.currentTime + secs); };
const SHAPES = {
  harness: {body: 'M10 8h20l20 32 20-32h20v6H74L50 52 26 14H10z', cap: 'M42 26h16v10H42z', bad: ''},
  wheel: {body: 'M50 10a22 22 0 1 0 .01 0zM50 26a6 6 0 1 1-.01 0z', cap: 'M38 6h24v5H38z', bad: 'M44 14l6 10-4 6 8 12'},
  clamp: {body: 'M18 24h64v16H18z', cap: 'M46 6h8v18h-8z', bad: 'M44 8l12 6-12 6 12 6'},
};

let EL = null, S = {};
const reset = () => { S = {door: null, drill: null, mode: 'comic', furi: true, trans: true, reel: {i: 0, playing: false, slow: false}, insp: {i: 0, gap: 0, seen: {}, right: {}, wrong: false}, d: null}; };
reset();
let timer = 0;
const stop = () => { clearTimeout(timer); timer = 0; S.reel.playing = false; try { speechSynthesis.cancel(); } catch (e) {} };
const visible = () => EL && EL.offsetParent !== null;
const btn = (on, a, v, label) => `<button class="btn${on ? '' : ' outlined'}" data-cs="${a}" data-a="${v}" aria-pressed="${!!on}">${label}</button>`;
const act = (a, label, cls = '', extra = '') => `<button class="btn ${cls}" data-cs="${a}" ${extra}>${label}</button>`;
const result = (ok, retry) => `<div class="cs-res" role="status"><b class="${ok ? 'cs-ok' : 'cs-bad'}">${tx(ok ? 'drill_pass' : 'drill_fail')}</b><button class="btn outlined" data-cs="${retry}">${tx('retry')}</button></div>`;

/* ---------- Door 1: Read ---------- */
const lineHtml = (t, big) => `<div class="cs-ja${big ? ' big' : ''}" lang="ja">${ruby(t.ja, S.furi)}</div>${S.trans && LANG !== 'ja' ? `<div class="cs-tr">${esc(L(t))}</div>` : ''}`;
function readDoor(p) {
  const lines = S.mode === 'comic' ? D.panels.map(x => x.text) : D.handbook;
  return `<div class="cs-row">${btn(S.mode === 'comic', 'mode', 'comic', tx('mode_comic'))}${btn(S.mode === 'handbook', 'mode', 'handbook', tx('mode_handbook'))}${btn(S.furi, 'furi', '', tx('furigana'))}${btn(S.trans, 'trans', '', tx('translation'))}</div>
  ${lines.map((t, i) => `<div class="card cs-line${S.mode === 'comic' ? ' ' + D.panels[i].who : ''}">${lineHtml(t)}</div>`).join('')}
  ${act('finish', p.read ? tx('done') : tx('finish_read'), '', p.read ? 'disabled' : '')}`;
}

/* ---------- Door 2: Watch ---------- */
const REEL_JA = D.reel.map(r => plain(r.ja));
function reelTick() {
  const r = S.reel;
  if (!r.playing) return;
  if (!visible()) { stop(); return; }
  try { speechSynthesis.cancel(); const u = new SpeechSynthesisUtterance(REEL_JA[r.i]); u.lang = 'ja-JP'; u.rate = r.slow ? .75 : 1; speechSynthesis.speak(u); } catch (e) {}
  timer = setTimeout(() => {
    if (r.i < D.reel.length - 1) { r.i++; draw(); reelTick(); }
    else { stop(); const p = load(); p.watch = true; save(p); draw(); }
  }, 2600 / (r.slow ? .75 : 1));
}
function watchDoor(p) {
  const r = S.reel;
  return `<p class="cs-sub">${tx('watch_hint')}</p>
  <div class="card cs-stage" aria-live="polite">${lineHtml(D.reel[r.i], true)}</div>
  <div class="cs-dots" aria-hidden="true">${D.reel.map((_, i) => `<i class="${i <= r.i && (r.playing || p.watch) ? 'on' : ''}"></i>`).join('')}</div>
  <div class="cs-row">${act('toggle', r.playing ? tx('pause') : tx('play'))}${act('replay', tx('replay'), 'outlined')}${btn(r.slow, 'slow', '', tx('speed'))}${p.watch ? `<b class="cs-ok">${tx('done')}</b>` : ''}</div>`;
}

/* ---------- Door 3: Play (nine drills, five engines) ---------- */
const DR = id => D.drills.find(x => x.id === id);
const pass = id => { const p = load(); if (!p.drills.includes(id)) { p.drills.push(id); save(p); } };
function playDoor(p) {
  const dr = S.drill && DR(S.drill);
  if (!dr) return `<p><b>${tx('drills_passed', {n: p.drills.length, total: D.drills.length, need: NEED})}</b></p>
    ${D.drills.map(d => `<button class="card cs-tile" data-cs="open" data-a="${d.id}"><span>${esc(L(d.name))}</span>${p.drills.includes(d.id) ? `<b class="cs-ok">${tx('drill_pass')}</b>` : ''}</button>`).join('')}`;
  return `<div class="card cs-pad"><h2 class="cs-h2">${esc(L(dr.name))}</h2><p class="cs-sub">${esc(L(dr.goal))}</p>${engine(dr)}</div>`;
}
function engine(dr) {
  const d = S.d;
  if (dr.kind === 'spot') {
    const found = Object.values(d.taps).filter(Boolean).length;
    if (d.phase === 'idle') return act('spotstart', tx('start'));
    return `<p><b>${tx('time_left', {n: d.left})} · ${tx('spot_found', {n: found})}</b></p>
      <div class="cs-grid">${dr.items.map((it, i) => `<button class="card cs-tile ${d.taps[i] === true ? 'good' : d.taps[i] === false ? 'miss' : ''}" data-cs="spottap" data-a="${i}" ${d.phase !== 'run' ? 'disabled' : ''}>${esc(L(it.label))}</button>`).join('')}</div>
      ${d.phase === 'end' ? result(found >= dr.need, 'spotstart') : ''}`;
  }
  if (dr.kind === 'sling') {
    const kg = Math.round(tension(dr.loadKg, d.deg)), ok = kg <= dr.wllKg;
    return `<label class="cs-lab"><b id="cs-deg">${tx('sling_angle', {deg: d.deg})}</b><input type="range" min="0" max="120" step="5" value="${d.deg}" data-cs-range="sling"></label>
      <p id="cs-kg">${tx('sling_tension', {kg})} · ${tx('sling_limit', {kg: dr.wllKg})}</p>
      <meter id="cs-meter" min="0" max="${dr.loadKg}" high="${dr.wllKg}" value="${kg}" style="width:100%;height:16px"></meter>
      ${act('lift', tx('sling_lift'))}
      ${d.lifted ? `<p role="status" class="${ok ? 'cs-ok' : 'cs-bad'}"><b>${tx(ok ? 'sling_ok' : 'sling_bad')}</b></p>` : ''}`;
  }
  if (dr.kind === 'reflex') {
    const ok = d.st === 'done' && d.ms < REFLEX_MS;
    return `<p>${tx('reflex_idle')}</p>
      ${['idle', 'early', 'done'].includes(d.st) ? act('rstart', d.st === 'idle' ? tx('start') : tx('retry')) : `<button class="cs-estop ${d.st}" data-cs="rhit">${tx(d.st === 'go' ? 'reflex_go' : 'reflex_wait')}<small>${tx('estop')}</small></button>`}
      ${d.st === 'early' ? `<p role="alert" class="cs-bad"><b>${tx('reflex_early')}</b></p>` : ''}
      ${d.st === 'done' ? `<p role="status" class="${ok ? 'cs-ok' : 'cs-bad'}"><b>${tx('reflex_result', {ms: d.ms, limit: REFLEX_MS})} · ${tx(ok ? 'drill_pass' : 'drill_fail')}</b></p>` : ''}`;
  }
  if (dr.kind === 'ring') {
    const ok = d.order.every(w => d.said[w] === dr.wheels[w]);
    return `<p class="cs-sub">${tx('sound_note')}</p>
      ${d.order.map((w, n) => `<div class="card cs-pad cs-row"><button class="btn outlined" data-cs="tap" data-a="${w}">${tx('ring_tap', {n: n + 1})}</button>
        ${btn(d.said[w] === true, 'say', w + ':1', tx('ring_ok'))}${btn(d.said[w] === false, 'say', w + ':0', tx('ring_reject'))}</div>`).join('')}
      ${act('check', tx('check'), '', Object.keys(d.said).length < d.order.length ? 'disabled' : '')}
      ${d.checked ? result(ok, 'restart') : ''}`;
  }
  const fin = d.picked.length === dr.steps.length, ok = fin && d.mistakes <= 1;
  return `<p class="cs-sub">${tx('seq_hint')}</p>
    ${d.order.map(i => { const at = d.picked.indexOf(i); return `<button class="card cs-tile ${at >= 0 ? 'good' : ''}" data-cs="step" data-a="${i}" ${at >= 0 || fin ? 'disabled' : ''}><span class="cs-n">${at >= 0 ? at + 1 : ''}</span><span class="grow">${esc(L(dr.steps[i]))}</span></button>`; }).join('')}
    <p aria-live="polite">${tx('mistakes', {n: d.mistakes})}</p>${fin ? result(ok, 'restart') : ''}`;
}
function initDrill(dr) {
  S.d = dr.kind === 'spot' ? {phase: 'idle', left: dr.secs, taps: {}} : dr.kind === 'sling' ? {deg: 100, lifted: false}
    : dr.kind === 'reflex' ? {st: 'idle', ms: 0} : dr.kind === 'ring' ? {order: shuf(dr.wheels.length), said: {}, checked: false}
    : {order: shuf(dr.steps.length), picked: [], mistakes: 0};
}
function spotTick() {
  clearTimeout(timer);
  timer = setTimeout(() => {
    const d = S.d; if (!d || d.phase !== 'run') return;
    d.left--;
    if (d.left <= 0) { d.phase = 'end'; draw(); return; }
    draw(); spotTick();
  }, 1000);
}

/* ---------- Door 4: Inspect (SVG, exploded view, pass/reject) ---------- */
function inspectDoor() {
  const q = S.insp, item = D.inspect[q.i], sh = SHAPES[item.id], seen = q.seen[item.id] || [], sp = item.spots;
  const lock = seen.length < sp.length || q.right[item.id] ? 'disabled' : '';
  return `<div class="cs-row">${D.inspect.map((x, n) => btn(n === q.i, 'item', n, (q.right[x.id] ? '✓ ' : '') + esc(L(x.name)))).join('')}</div>
  <div class="card cs-pad"><h2 class="cs-h2">${esc(L(item.name))}</h2>
    <div class="cs-svg"><svg viewBox="0 0 100 60" role="img" aria-label="${esc(L(item.name))}">
      <g id="cs-body" transform="translate(0 ${q.gap / 4})" fill="var(--surface-3)" stroke="var(--on)" stroke-width=".8" fill-rule="evenodd"><path d="${sh.body}"/></g>
      <g id="cs-cap" transform="translate(0 ${-q.gap / 2})" fill="var(--surface)" stroke="var(--on)" stroke-width=".8"><path d="${sh.cap}"/></g>
      ${item.defective ? `<path d="${sh.bad}" fill="none" stroke="var(--bad)" stroke-width="1.6"/>` : ''}</svg>
      ${sp.map((s, n) => `<button class="cs-spot${seen.includes(n) ? ' seen' : ''}" style="left:${s.x}%;top:${s.y / 60 * 100}%" data-cs="spot" data-a="${n}" aria-label="${esc(L(s.check))}"></button>`).join('')}</div>
    <label class="cs-lab">${tx('inspect_explode')}<input type="range" min="0" max="20" value="${q.gap}" data-cs-range="gap"></label>
    <p class="cs-sub">${tx('inspect_tap')}</p><p>${tx('inspect_checked', {n: seen.length, total: sp.length})}</p>
    <ul>${seen.map(n => `<li><b>${esc(L(sp[n].check))}</b></li>`).join('')}</ul>
    <div class="cs-row">${act('judge', tx('inspect_pass'), '', `data-a="0" ${lock}`)}${act('judge', tx('inspect_reject'), 'outlined', `data-a="1" ${lock}`)}
    ${q.right[item.id] ? `<b class="cs-ok">${tx('inspect_right')}</b>` : ''}${q.wrong ? `<b role="alert" class="cs-bad">${tx('inspect_wrong')}</b>` : ''}</div></div>`;
}

/* ---------- screen ---------- */
function draw() {
  if (!EL) return;
  const p = load();
  const name = d => tx('door_' + d);
  let body;
  if (!S.door) body = `${DOORS.map(d => `<button class="card cs-door" data-cs="door" data-a="${d}"><b>${name(d)}</b><span>${tx('door_' + d + '_sub')}</span>${done(p, d) ? `<em>${tx('done')}</em>` : ''}</button>`).join('')}
    <div class="card cs-pad cs-pp${passport(p) ? ' on' : ''}"><h2 class="cs-h2">${tx('passport_title')}</h2><p><b>${tx(passport(p) ? 'passport_earned' : 'passport_locked')}</b></p><button class="btn text" data-cs="wipe">${tx('reset')}</button></div>`;
  else body = `${act('home', tx('back'), 'text')}<h2 class="cs-h2">${name(S.door)}</h2>${{read: readDoor, watch: watchDoor, play: playDoor, inspect: inspectDoor}[S.door](p)}`;
  const old = EL.querySelector('.content'), y = old ? old.scrollTop : 0;
  EL.innerHTML = `<header class="appbar"><button class="icon-btn" data-back aria-label="${tx('back')}"><svg class="ic" viewBox="0 0 24 24" aria-hidden="true"><path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/></svg></button><h1>${esc(L(D.course.title))}</h1></header>
    <div class="content cs-c">${S.door ? '' : `<p class="cs-sub">${tx('sub')}</p>`}${body}</div>`;
  const c = EL.querySelector('.content'); if (c) c.scrollTop = y;
}

function onClick(e) {
  const b = e.target.closest('[data-cs]'); if (!b || !EL.contains(b)) return;
  const a = b.dataset.cs, v = b.dataset.a, dr = S.drill && DR(S.drill), d = S.d, p = load();
  switch (a) {
    case 'door': S.door = v; S.drill = null; break;
    case 'home': stop(); S.door = null; S.drill = null; break;
    case 'wipe': localStorage.removeItem(KEY); stop(); reset(); break;
    case 'mode': S.mode = v; break;
    case 'furi': S.furi = !S.furi; break;
    case 'trans': S.trans = !S.trans; break;
    case 'finish': p.read = true; save(p); break;
    case 'toggle': if (S.reel.playing) stop(); else { S.reel.playing = true; reelTick(); } break;
    case 'replay': stop(); S.reel.i = 0; S.reel.playing = true; reelTick(); break;
    case 'slow': S.reel.slow = !S.reel.slow; break;
    case 'open': S.drill = v; initDrill(DR(v)); break;
    case 'restart': initDrill(dr); break;
    case 'spotstart': S.d = {phase: 'run', left: dr.secs, taps: {}}; spotTick(); break;
    case 'spottap': {
      if (d.phase !== 'run' || d.taps[v] !== undefined) break;
      d.taps[v] = dr.items[v].hazard;
      const found = Object.values(d.taps).filter(Boolean).length;
      if (found >= dr.items.filter(x => x.hazard).length) { d.phase = 'end'; clearTimeout(timer); }
      if (d.phase === 'end' && found >= dr.need) pass(dr.id);
      break;
    }
    case 'lift': d.lifted = true; if (tension(dr.loadKg, d.deg) <= dr.wllKg) pass(dr.id); break;
    case 'rstart': d.st = 'wait'; timer = setTimeout(() => { d.t0 = performance.now(); tone(110, .5, .3); d.st = 'go'; draw(); }, 1000 + Math.random() * 2000); break;
    case 'rhit':
      if (d.st === 'wait') { clearTimeout(timer); d.st = 'early'; }
      else if (d.st === 'go') { d.ms = Math.round(performance.now() - d.t0); d.st = 'done'; if (d.ms < REFLEX_MS) pass(dr.id); }
      break;
    case 'tap': tone(dr.wheels[v] ? HZ.intact : HZ.cracked, dr.wheels[v] ? 1.2 : .15); return;
    case 'say': { const [w, ok] = v.split(':'); d.said[w] = ok === '1'; break; }
    case 'check': d.checked = true; if (d.order.every(w => d.said[w] === dr.wheels[w])) pass(dr.id); break;
    case 'step': {
      const i = +v;
      if (i === d.picked.length) { d.picked.push(i); if (d.picked.length === dr.steps.length && d.mistakes <= 1) pass(dr.id); } else d.mistakes++;
      break;
    }
    case 'item': S.insp.i = +v; S.insp.gap = 0; S.insp.wrong = false; break;
    case 'spot': { const id = D.inspect[S.insp.i].id, s = S.insp.seen[id] || []; S.insp.seen[id] = [...new Set([...s, +v])]; break; }
    case 'judge': {
      const item = D.inspect[S.insp.i], ok = (v === '1') === item.defective;
      S.insp.wrong = !ok;
      if (ok) { S.insp.right[item.id] = true; if (D.inspect.every(x => S.insp.right[x.id])) { p.inspect = true; save(p); } }
      break;
    }
  }
  draw();
}
// range inputs update the DOM in place so dragging is not interrupted
function onInput(e) {
  const r = e.target.dataset.csRange; if (!r) return;
  const v = +e.target.value;
  if (r === 'sling') {
    const dr = DR(S.drill), kg = Math.round(tension(dr.loadKg, v));
    S.d.deg = v; S.d.lifted = false;
    EL.querySelector('#cs-deg').textContent = tx('sling_angle', {deg: v});
    EL.querySelector('#cs-kg').textContent = tx('sling_tension', {kg}) + ' · ' + tx('sling_limit', {kg: dr.wllKg});
    EL.querySelector('#cs-meter').value = kg;
  } else {
    S.insp.gap = v;
    EL.querySelector('#cs-body').setAttribute('transform', `translate(0 ${v / 4})`);
    EL.querySelector('#cs-cap').setAttribute('transform', `translate(0 ${-v / 2})`);
  }
}

Object.assign(SCREENS, {
  course(el) {
    EL = el;
    if (!el._cs) { el._cs = 1; el.addEventListener('click', onClick); el.addEventListener('input', onInput); }
    stop(); reset(); draw();
  },
});
})();
