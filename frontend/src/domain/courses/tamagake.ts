import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string, bn: string): Tri => ({ ja, en, bn })

export const tamagake: CourseData = {
  id: 'tamagake',
  category: 'trade',
  level: 2,
  minutes: 20,
  title: t(
    '玉掛{たまが}け作業{さぎょう}',
    'Crane slinging operations',
    'ক্রেন স্লিং অপারেশন (মাল বাঁধা ও সংকেত)',
  ),
  sub: t(
    '質量{しつりょう}に応{おう}じた資格{しかく}を持{も}ち、正{ただ}しい吊{つ}り角度{かくど}と地切{じぎ}りで安全{あんぜん}に荷{に}を運{はこ}ぶ。',
    'Hold proper certification by load weight, use correct sling angles and ground lift to move loads safely.',
    'মালের ওজন অনুযায়ী উপযুক্ত সনদ নিয়ে, সঠিক স্লিং কোণ ও গ্রাউন্ড-লিফ্ট মেনে নিরাপদে ভার বহন করো।',
  ),
  refs: [
    '労働安全衛生法 第61条（就業制限：玉掛け業務）',
    'クレーン等安全規則 第219条（玉掛け用ワイヤロープ等の使用制限）',
    'クレーン等安全規則 第221条・第222条（安全係数及び玉掛け作業の合図）',
    '厚生労働省 クレーン・玉掛け作業安全マニュアル',
  ],
  panels: [
    {
      who: 'foreman',
      text: t(
        '今日{きょう}は工場{こうじょう}で質量{しつりょう}1.5トンある鋼板{こうはん}の玉掛{たまが}け作業{さぎょう}を行{おこな}います。',
        'Today we will perform slinging work on 1.5-ton steel plates in the plant.',
        'আজ আমরা কারখানায় ১.৫ টন ওজনের স্টিল প্লেটে স্লিং বাঁধার কাজ করব।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '私{わたし}がワイヤーロープを掛{か}けてクレーンに合図{あいず}を出{だ}してみます！',
        'I will attach the wire ropes and give signals to the crane!',
        'আমি ওয়্যার রোপ আটকে ক্রেন চালককে সংকেত দিয়ে দেখি!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'ラヒムさん、玉掛{たまが}け技能講習{ぎのうこうしゅう}の修了証{しゅうりょうしょう}は持{も}っていますね？1トン以上{いじょう}は技能講習{ぎのうこうしゅう}が必須{ひっす}です。',
        'Rahim, you have your slinging skills training certificate, right? It is required for loads of 1 ton or more.',
        'রহিম, তোমার কাছে স্লিং ট্রেনিংয়ের সার্টিফিকেট আছে তো? ১ টন বা তার বেশিতে এই সনদ থাকা বাধ্যতামূলক।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'はい、胸{むね}ポケットに携帯{けいたい}しています！荷{に}が落{お}ちないよう、ワイヤー2本{ほん}の間隔{かんかく}を大{おお}きく開{ひら}いて掛{か}けました！',
        'Yes, I carry it in my chest pocket! To prevent falling, I opened the two wire ropes very wide!',
        'জি, বুকের পকেটে আছে! মাল যাতে না পড়ে, তাই দুটি তারের দূরত্ব অনেক চওড়া করে আটকেছি!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '待{ま}ちなさい！吊{つ}り角度{かくど}が90度{ど}を超{こ}えています。角度{かくど}が大{おお}きいとワイヤーに過大{かだい}な張力{ちょうりょく}がかかり切断{せつだん}の恐{おそ}れがあります。',
        'Hold on! The sling angle exceeds 90°. A wider angle creates excessive tension on wires, risking a break.',
        'থামো! স্লিংয়ের কোণ ৯০ ডিগ্রির বেশি হয়ে গেছে। কোণ বেশি হলে তারের ওপর অতিরিক্ত টান পড়ে ছিঁড়ে যেতে পারে।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '本当{ほんとう}ですね！ワイヤーの角度{かくど}は60度以内{どいない}になるよう掛{か}け直{なお}しました。すぐ巻{ま}き上{あ}げてよいですか？',
        'You are right! I readjusted the wires within 60°. Can I hoist the load right away?',
        'ঠিক তাই তো! তারের কোণ ৬০ ডিগ্রির মধ্যে রেখে আবার বাঁধলাম। এখন কি সাথে সাথে উপরে তুলব?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'まず「地切{じぎ}り」です。床{ゆか}から約{やく}30cm巻{ま}き上{あ}げて一旦{いったん}停止{ていし}し、重心{じゅうしん}のズレやワイヤーの掛{か}かりを確認{かくにん}します。',
        'First do "ground lift". Hoist about 30 cm off the floor, stop, and check center of gravity and wire seating.',
        'প্রথমে "গ্রাউন্ড-লিফ্ট"। মেঝে থেকে প্রায় ৩০ সেমি তুলে থামাও, এবং ভরকেন্দ্রের ভারসাম্য ও তার ঠিকমতো বসেছে কিনা দেখো।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '30cm巻{ま}き上{あ}げて停止{ていし}、傾{かたむ}きなし、フック外{はず}れ止{ど}めも閉{と}まっています！手{て}で押{お}さえて誘導{ゆうどう}しますね。',
        'Lifted 30 cm and stopped, no tilt, safety latch closed! I will push it with my hands to guide it.',
        '৩০ সেমি তুলে থামালাম, কোনো ঢাল নেই, সেফটি ল্যাচও বন্ধ আছে! হাত দিয়ে ধরে টেনে পথ দেখাই।',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '手{て}で直接{ちょくせつ}触{ふ}れてはいけません。介錯{かいしゃく}ロープを使{つか}い、絶対{ぜったい}に吊{つ}り荷{に}の下{した}に入{はい}らない位置{いち}から誘導{ゆうどう}します。',
        'Do not touch it directly with your hands. Use a tag line, and guide it from a position never underneath the load.',
        'সরাসরি হাত দিয়ে ধরা যাবে না। গাইড রোপ ব্যবহার করো, এবং ঝুলন্ত মালের নিচে কখনো না গিয়ে নিরাপদ দূর থেকে সামলাও।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '介錯{かいしゃく}ロープで距離{きょり}を取{と}り、周囲{しゅうい}の作業員{さぎょういん}に声{こえ}をかけてから巻{ま}き上{あ}げの合図{あいず}を出{だ}します！',
        'I will keep distance using the tag line, alert nearby workers, and give the hoist signal!',
        'গাইড রোপ দিয়ে নিরাপদ দূরত্ব রাখব, আশেপাশের সবাইকে সতর্ক করব এবং তারপর ওপরে তোলার ইশারা দেব!',
      ),
    },
  ],
  handbook: [
    t(
      '吊{つ}り上{あ}げ荷重{かじゅう}1トン以上{いじょう}は「玉掛{たまが}け技能講習{ぎのうこうしゅう}」、1トン未満{みまん}は「玉掛{たまが}け特別教育{とくべつきょういく}」の修了者{しゅうりょうしゃ}のみが作業{さぎょう}し、修了証{しゅうりょうしょう}を常{つね}に携帯{けいたい}する。',
      'Only those who completed the Skill Training (1t or more) or Special Education (under 1t) may sling loads, carrying their card at all times.',
      '১ টন বা তার বেশিতে "দক্ষতা প্রশিক্ষণ" এবং ১ টনের কমের জন্য "বিশেষ শিক্ষা" সনদপ্রাপ্ত ব্যক্তিরাই শুধু কাজ করবেন, এবং সর্বদা সনদ সাথে রাখবেন।',
    ),
    t(
      '吊{つ}り荷{に}の真下{ました}や、荷{に}の倒{たお}れ・振{ふ}れによって挟{はさ}まれるおそれのある危険{きけん}区域{くいき}には絶対{ぜったい}に立{た}ち入{い}らない。',
      'Never enter the area directly beneath a suspended load or the danger zone where you could be crushed by tipping or swinging.',
      'ঝুলন্ত মালের ঠিক নিচে বা মাল হেলে পড়ে চাপা পড়ার ঝুঁকি আছে এমন বিপজ্জনক জায়গায় কখনো প্রবেশ করবে না।',
    ),
    t(
      '2本{ほん}吊{づ}り・4本{ほん}吊{づ}りのワイヤーロープの吊{つ}り角度{かくど}は原則{げんそく}として60度以内{どいない}とし、角度{かくど}が大{おお}きくなるほど急激{きゅうげき}に張力{ちょうりょく}が増大{ぞうだい}することを厳守{げんしゅ}する。',
      'Keep the sling angle of multi-leg wire ropes within 60 degrees; strictly observe that tension increases sharply as the angle widens.',
      'মাল বাঁধার ক্ষেত্রে স্লিং তারের কোণ সর্বদা ৬০ ডিগ্রির মধ্যে রাখবে; কোণ বাড়লে তারে প্রচণ্ড টান পড়ে তা মনে রাখবে।',
    ),
    t(
      '巻{ま}き上{あ}げ開始時{かいしじ}は、床{ゆか}から約{やく}30cm浮{う}かせた位置{いち}で一旦{いったん}停止{ていし}する「地切{じぎ}り」を行{おこな}い、荷{に}の傾{かたむ}き・重心{じゅうしん}・ワイヤーの緩{ゆる}み・ブレーキ制動{せいどう}を確認{かくにん}する。',
      'When lifting begins, perform a "ground lift" by stopping about 30 cm off the floor to check tilt, center of gravity, wire slack, and brake holding.',
      'উত্তোলন শুরুর সময় মেঝে থেকে প্রায় ৩০ সেমি উঁচুতে থামিয়ে "গ্রাউন্ড-লিফ্ট" করো, এবং ঢাল, ভরকেন্দ্র, তারের টান ও ক্রেনের ব্রেক পরীক্ষা করো।',
    ),
    t(
      '荷{に}の重心{じゅうしん}の真上{まうえ}にクレーンのフックを位置{いち}させ、巻{ま}き上{あ}げ時{じ}に荷{に}が横{よこ}に引{ひ}っ張{ぱ}られて振{ふ}れる「斜{しゃ}め吊{づ}り」を絶対{ぜったい}に行{おこな}わない。',
      'Position the crane hook directly above the center of gravity; never perform "side pulling" where the load swings horizontally.',
      'ক্রেনের হুক সর্বদা মালের ভরকেন্দ্রের ঠিক ওপরে রাখবে; মাল পাশে টেনে এনে তোলার মতো বিপজ্জনক কাজ কখনো করবে না।',
    ),
    t(
      '1撚{よ}りの間{あいだ}で素線{そせん}の10%以上{いじょう}が切断{せつだん}しているワイヤーロープは使用{しよう}せず、直{ただ}ちに廃棄{はいき}する。', // verify: クレーン則第219条：1撚りの間で素線の10%以上が切断したワイヤロープは使用禁止
      'Never use and immediately discard wire rope with 10% or more broken wires within one lay length.',
      'একটি লে দৈর্ঘ্যে ১০% বা তার বেশি তার ছিঁড়ে গেলে সেই ওয়্যার রোপ ব্যবহার না করে অবিলম্বে বাতিল করো।',
    ),
    t(
      '直径{ちょっけい}の減少{げんしょう}が公称径{こうしょうけい}の7%を超{こ}えるワイヤーロープは使用{しよう}せず、廃棄{はいき}する。', // verify: クレーン則第219条：直径の減少が公称径の7%を超えるワイヤロープは使用禁止
      'Never use and discard wire rope whose diameter reduction exceeds 7% of nominal diameter.',
      'ওয়্যার রোপের ব্যাস নামমাত্র মাপের চেয়ে ৭% এর বেশি কমে গেলে তা ব্যবহার না করে বাতিল করো।',
    ),
    t(
      'キンク（折{お}れ曲{ま}がりによる著{いちじる}しい歪{ゆが}み）や著{いちじる}しい変形{へんけい}、腐食{ふしょく}のあるワイヤーロープは直{ただ}ちに廃棄{はいき}する。', // verify: クレーン則第219条：キンクしたもの、著しい変形・腐食のあるワイヤロープは使用禁止
      'Immediately discard wire rope with kinks, severe deformation, or corrosion.',
      'কিংক (কুঁচকে বেঁকে যাওয়া), মারাত্মক বিকৃতি বা মরিচা ধরা ওয়্যার রোপ অবিলম্বে বাতিল করো।',
    ),
    t(
      'クレーンフックの外{はず}れ止{ど}め装置{そうち}（安全{あんぜん}ラッチ）が正常{せいじょう}に作動{さどう}していることを確認{かくにん}し、ワイヤーのはずれを防{ふせ}ぐ。',
      'Confirm the hook safety latch operates normally to prevent wire ropes from jumping off.',
      'হুক থেকে তার ছিটকে পড়া রোধে সেফটি ল্যাচ সঠিকভাবে কাজ করছে কিনা তা নিশ্চিত করো।',
    ),
    t(
      'クレーンへの運転{うんてん}合図{あいず}は指名{しめい}された1名{めい}の合図者{あいずしゃ}のみが行{おこな}う。ただし、危険{きけん}を発見{はっけん}したときの「非常停止{ひじょうていし}」合図{あいず}は誰{だれ}でも直{ただ}ちに出{だ}すことができる。',
      'Crane operating signals are given by only one designated signal person, but anyone can immediately signal an emergency stop upon sensing danger.',
      'ক্রেনের সাধারণ সংকেত কেবল একজন নির্ধারিত সংকেতকারী দেবেন। তবে বিপদের মুহূর্তে যে কেউই অবিলম্বে জরুরি থামার সংকেত দিতে পারেন।',
    ),
  ],
  reel: [
    t(
      '資格{しかく}証{しょう}、携帯{けいたい}、ヨシ！',
      'Qualification card, carried, yoshi!',
      'যোগ্যতার সনদ, সাথে আছে, ঠিক আছে!',
    ),
    t(
      'ワイヤーロープ、素線{そせん}切{き}れ・キンクなし、ヨシ！',
      'Wire rope, no broken wires or kinks, yoshi!',
      'ওয়্যার রোপ, ছেঁড়া তার বা কিংক নেই, ঠিক আছে!',
    ),
    t(
      'フック外{はず}れ止{ど}め、作動{さどう}、ヨシ！',
      'Hook safety latch, operating, yoshi!',
      'হুকের সেফটি ল্যাচ, কাজ করছে, ঠিক আছে!',
    ),
    t(
      '吊{つ}り角度{かくど}、60度以内{どいない}、ヨシ！',
      'Sling angle within 60 degrees, yoshi!',
      'স্লিং কোণ, ৬০ ডিগ্রির মধ্যে, ঠিক আছে!',
    ),
    t(
      '地切{じぎ}り30cm、一旦{いったん}停止{ていし}・バランス、ヨシ！',
      'Ground lift 30cm, temporary stop and balance, yoshi!',
      'গ্রাউন্ড-লিফ্ট ৩০ সেমি, বিরতি ও ভারসাম্য, ঠিক আছে!',
    ),
    t(
      '吊{つ}り荷{に}の下{した}、立入{たちいり}なし・周囲{しゅうい}クリア、ヨシ！',
      'Under the load, no entry and perimeter clear, yoshi!',
      'ঝুলন্ত মালের নিচে কেউ নেই ও চারপাশ পরিষ্কার, ঠিক আছে!',
    ),
    t(
      '巻上{まきあ}げ合図{あいず}、ヨシ！',
      'Hoist signal, yoshi!',
      'উত্তোলন সংকেত, ঠিক আছে!',
    ),
  ],
  drills: [
    {
      id: 'tamagake-spot',
      kind: 'spot',
      secs: 30,
      need: 4,
      name: t(
        '玉掛{たまが}け現場{げんば}の危険{きけん}予知{よち}',
        'Hazard spotting in slinging operations',
        'স্লিং অপারেশনের কাজের জায়গায় বিপদ শনাক্তকরণ',
      ),
      goal: t(
        '30秒{びょう}で玉掛{たまが}け作業{さぎょう}に潜{ひそ}む危険{きけん}な状態{じょうたい}を4つ見{み}つける。',
        'Find 4 hazardous slinging conditions within 30 seconds.',
        '৩০ সেকেন্ডের মধ্যে স্লিং কাজের ৪টি বিপজ্জনক ত্রুটি খুঁজে বের করো।',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '吊{つ}り荷{に}の真下{ました}に作業員{さぎょういん}が入{はい}り込{こ}んでいる',
            'Worker standing directly beneath a suspended load',
            'ঝুলন্ত মালের ঠিক নিচে কর্মী দাঁড়িয়ে আছে',
          ),
        },
        {
          hazard: false,
          label: t(
            '吊{つ}り角度{かくど}が50度{ど}で荷{に}が水平{すいへい}に保{たも}たれている',
            'Sling angle is 50 degrees and load is kept horizontal',
            'স্লিং কোণ ৫০ ডিগ্রি এবং মাল সমানভাবে ভারসাম্য রাখা আছে',
          ),
        },
        {
          hazard: true,
          label: t(
            'ワイヤーロープの吊{つ}り角度{かくど}が100度{ど}に大{おお}きく開{ひら}いている',
            'Wire rope sling angle is opened excessively to 100 degrees',
            'ওয়্যার রোপের স্লিং কোণ ১০০ ডিগ্রির বেশি চওড়া হয়ে আছে',
          ),
        },
        {
          hazard: false,
          label: t(
            '介錯{かいしゃく}ロープを持{も}って吊{つ}り荷{に}から2m以上{いじょう}離{はな}れて誘導{ゆうどう}している',
            'Guiding using a tag line while standing 2m or more away from the load',
            'গাইড রোপ ধরে মাল থেকে ২ মিটার দূরে থেকে নির্দেশনা দিচ্ছে',
          ),
        },
        {
          hazard: true,
          label: t(
            'クレーンフックの外{はず}れ止{ど}め装置{そうち}（安全{あんぜん}ラッチ）が破損{はそん}している',
            'Hook safety latch is broken or missing',
            'ক্রেন হুকের সেফটি ল্যাচ ভাঙা বা নেই',
          ),
        },
        {
          hazard: false,
          label: t(
            '玉掛{たまが}け技能講習{ぎのうこうしゅう}の修了証{しゅうりょうしょう}を胸{むね}ポケットに携帯{けいたい}している',
            'Carrying slinging skills training certificate card in chest pocket',
            'বুকের পকেটে স্লিং স্কিল ট্রেনিং সনদ বহন করছে',
          ),
        },
        {
          hazard: true,
          label: t(
            '地切{じぎ}りを行{おこな}わず、一気{いっき}に高速{こうそく}で荷{に}を高{たか}く巻{ま}き上{あ}げている',
            'Hoisting load high at high speed without performing a ground lift',
            'গ্রাউন্ড-লিফ্ট না করে একবারে দ্রুত গতিতে মাল অনেক উপরে তুলে ফেলা হচ্ছে',
          ),
        },
        {
          hazard: false,
          label: t(
            '荷{に}の重心{じゅうしん}の真上{まうえ}にフックが位置{いち}している',
            'Hook positioned directly above load center of gravity',
            'মালের ভরকেন্দ্রের ঠিক সোজা উপরে হুক রাখা আছে',
          ),
        },
      ],
    },
    {
      id: 'jigiri-seq',
      kind: 'seq',
      name: t(
        '地切{じぎ}りと安全{あんぜん}巻上{まきあ}げ手順{てじゅん}',
        'Ground-lift and safe hoisting sequence',
        'গ্রাউন্ড-লিফ্ট ও নিরাপদ উত্তোলনের ধাপ',
      ),
      goal: t(
        '玉掛{たまが}けから巻{ま}き上{あ}げ完了{かんりょう}までの5つの手順{てじゅん}を正{ただ}しい順番{じゅんばん}に並{なら}べる。',
        'Arrange the 5 steps from slinging to hoisting in the correct order.',
        'স্লিং লাগানো থেকে উত্তোলন শেষ পর্যন্ত ৫টি ধাপ সঠিক ক্রমে সাজাও।',
      ),
      steps: [
        t(
          '1. 荷{に}の重量{じゅうりょう}と重心{じゅうしん}を確認{かくにん}し、適合{てきごう}するワイヤーロープを荷{に}に掛{か}ける',
          '1. Check load weight and center of gravity, then attach suitable wire ropes',
          '১. মালের ওজন ও ভরকেন্দ্র দেখে নিয়ে উপযুক্ত ওয়্যার রোপ মালে আটকাও',
        ),
        t(
          '2. クレーンフックを重心{じゅうしん}真上{まうえ}に合{あ}わせ、ワイヤーを掛{か}けて外{はず}れ止{ど}めを閉{と}じる',
          '2. Align hook directly over center of gravity, engage wires, and close latch',
          '২. হুক ভরকেন্দ্রের ঠিক ওপরে এনে তার হুকে ঢোকাও এবং সেফটি ল্যাচ বন্ধ করো',
        ),
        t(
          '3. ワイヤーが張{は}る直前{ちょくぜん}まで巻{ま}き、手{て}を触{ふ}れずにワイヤーの噛{か}み合{あ}わせを目視{もくし}確認{かくにん}する',
          '3. Hoist until wires near tension, visually inspect seating without touching',
          '৩. তার টানটান হওয়ার আগ পর্যন্ত তুলে হাত না দিয়ে তাকিয়ে তারের অবস্থান যাচাই করো',
        ),
        t(
          '4. 床{ゆか}から約{やく}30cm浮{う}かせて一旦{いったん}停止{ていし}（地切{じぎ}り）し、傾{かたむ}き・バランス・ブレーキを確認{かくにん}する',
          '4. Lift approx 30 cm off floor and stop (ground lift) to check tilt, balance, and brake',
          '৪. মেঝে থেকে ৩০ সেমি উঁচুতে থামিয়ে (গ্রাউন্ড-লিফ্ট) ঢাল, ভারসাম্য ও ব্রেক পরীক্ষা করো',
        ),
        t(
          '5. 周囲{しゅうい}の安全{あんぜん}と退避{たいひ}を確認{かくにん}し、合図{あいず}を行{おこな}って目的{もくてき}の高{たか}さまで巻{ま}き上{あ}げる',
          '5. Confirm surrounding clearance, then signal to hoist to target height',
          '৫. চারপাশের মানুষ নিরাপদ দূরত্বে আছে দেখে সংকেত দিয়ে নির্দিষ্ট উচ্চতায় তোলো',
        ),
      ],
    },
    {
      id: 'sling-angle',
      kind: 'sling',
      loadKg: 2000,
      wllKg: 1200,
      name: t(
        '吊{つ}り角度{かくど}とワイヤー張力{ちょうりょく}',
        'Sling angle and wire tension',
        'স্লিং কোণ ও তারের টান',
      ),
      goal: t(
        'ワイヤー1本{いっぽん}の許容荷重{きょようかじゅう}（1200kg）を超{こ}えないよう、角度{かくど}を60度以内{どいない}に調整{ちょうせつ}する。',
        'Adjust the angle within 60 degrees so single wire tension does not exceed limit (1200 kg).',
        'তারের সহনশীল সীমা (১২০০ কেজি) যাতে না ছাড়ায় সেজন্য কোণ ৬০ ডিগ্রির মধ্যে রাখো।',
      ),
    },
    {
      id: 'crane-estop',
      kind: 'reflex',
      name: t(
        '非常停止{ひじょうていし}合図{あいず}の反射{はんしゃ}',
        'Emergency stop signal reflex',
        'জরুরি থামার সংকেত প্রতিফলন',
      ),
      goal: t(
        '荷崩{にくず}れや人{ひと}の侵入{しんにゅう}などの危険{きけん}を発見{はっけん}したら、直{ただ}ちに非常停止{ひじょうていし}を行{おこな}う。',
        'Immediately act to emergency-stop upon spotting danger like shifting load or entering workers.',
        'মাল হেলে পড়া বা মানুষ ঢুকে পড়ার মতো বিপদ দেখলে তৎক্ষণাৎ জরুরি থামার কাজ করো।',
      ),
    },
  ],
  inspect: [
    {
      id: 'wire-rope-inspect',
      defective: true,
      name: t(
        'ワイヤーロープの始業前{しぎょうまえ}点検{てんけん}',
        'Wire rope pre-use inspection',
        'ওয়্যার রোপ কাজ শুরুর পূর্ববর্তী পরিদর্শন',
      ),
      spots: [
        {
          x: 30,
          y: 35,
          check: t(
            '素線{そせん}の切断{せつだん}：1撚{よ}りの間{あいだ}で素線{そせん}の10%以上{いじょう}が切断{せつだん}していないか（良品{りょうひん}：切断{せつだん}なし / 不良{ふりょう}：多数{たすう}の素線{そせん}切{き}れあり）', // verify: クレーン則第219条：素線の10%以上が切断したワイヤロープは使用禁止
            'Broken wires: Has 10% or more of wires broken within one lay length (Good: No breaks / Defect: Many broken wires)',
            'তার ছেঁড়া: ১টি লে-র মধ্যে ১০% বা বেশি তার ছিঁড়েছে কিনা (ভালো: অক্ষত / ত্রুটি: অনেকগুলো তার ছেঁড়া)',
          ),
        },
        {
          x: 50,
          y: 55,
          check: t(
            'キンク・型{かた}崩{くず}れ：折{お}れ曲{ま}がりによる著{いちじる}しい癖{くせ}や歪{ゆが}みがないか（良品{りょうひん}：変形{へんけい}なし / 不良{ふりょう}：鋭角{えいかく}に折{お}れたキンクあり）', // verify: クレーン則第219条：キンクしたワイヤロープは使用禁止
            'Kinks and deformation: Any sharp bends, severe kinks, or distortion (Good: Undistorted / Defect: Sharp kink present)',
            'কিংক বা বিকৃতি: তীব্র বাঁক বা কিংক আছে কিনা (ভালো: স্বাভাবিক ও অবিকৃত / ত্রুটি: ধারালো কিংক বাঁক বিদ্যমান)',
          ),
        },
        {
          x: 70,
          y: 75,
          check: t(
            '直径{ちょっけい}の減少{げんしょう}：摩耗{まもう}による細{ほそ}りが公称径{こうしょうけい}の7%を超{こ}えていないか（良品{りょうひん}：規定{きてい}寸法{すんぽう}内{ない} / 不良{ふりょう}：摩耗{まもう}大{だい}で8%減少{げんしょう}）', // verify: クレーン則第219条：直径の減少が公称径の7%を超えるワイヤロープは使用禁止
            'Diameter reduction: Has wear reduced diameter by over 7% of nominal size (Good: Within tolerance / Defect: Reduced by 8%)',
            'ব্যাসের হ্রাস: ক্ষয়ের কারণে নামমাত্র মাপের ৭% এর বেশি কমেছে কিনা (ভালো: সঠিক মাপে / ত্রুটি: ক্ষয়ে ৮% কমে গেছে)',
          ),
        },
      ],
    },
    {
      id: 'crane-hook-inspect',
      defective: false,
      name: t(
        'クレーンフック・外{はず}れ止{ど}め装置{そうち}の点検{てんけん}',
        'Crane hook and safety latch inspection',
        'ক্রেন হুক ও সেফটি ল্যাচ পরিদর্শন',
      ),
      spots: [
        {
          x: 50,
          y: 25,
          check: t(
            '外{はず}れ止{ど}め装置{そうち}（安全{あんぜん}ラッチ）：バネが健全{けんぜん}でフック先端{せんたん}に密着{みっちゃく}して閉{と}じるか（良品{りょうひん}：隙間{すきま}なく閉{と}じる / 不良{ふりょう}：バネ折{お}れ・開{ひら}きっぱなし）',
            'Safety latch: Spring intact and latch closes fully against tip (Good: Closes securely / Defect: Broken spring or stuck open)',
            'সেফটি ল্যাচ: স্প্রিং ঠিক আছে এবং হুকের মাথায় পুরোপুরি লেগে বন্ধ হয় কিনা (ভালো: শক্তভাবে বন্ধ হয় / ত্রুটি: খোলা বা স্প্রিং ভাঙা)',
          ),
        },
        {
          x: 45,
          y: 55,
          check: t(
            'フックの口{くち}開{ひら}き：過荷重{かかじゅう}による懐{ふところ}の伸{の}びや変形{へんけい}がないか（良品{りょうひん}：基準{きじゅん}寸法{すんぽう}通{とお}り / 不良{ふりょう}：口{くち}開{ひら}きが5%以上{いじょう}拡大{かくだい}）', // verify: フックの口開きは原寸の5%以上の拡大で廃棄
            'Hook throat opening: Any throat stretch or distortion from overload (Good: Normal dimension / Defect: Widened by 5% or more)',
            'হুকের মুখ ফাঁকা হওয়া: অতিরিক্ত ভারে হুকের মুখ চওড়া বা বিকৃত হয়েছে কিনা (ভালো: স্বাভাবিক মাপ / ত্রুটি: ৫% এর বেশি চওড়া)',
          ),
        },
        {
          x: 65,
          y: 75,
          check: t(
            'フック本体{ほんたい}の摩耗{まもう}・亀裂{きれつ}：ロープ掛{か}け部{ぶ}に深{ふか}い溝{みぞ}やクラックがないか（良品{りょうひん}：滑{なめ}らかで傷{きず}なし / 不良{ふりょう}：深{ふか}い傷{きず}や亀裂{きれつ}あり）',
            'Hook wear and cracks: Any deep grooves or cracks at rope saddle (Good: Smooth and sound / Defect: Deep notches or cracks)',
            'হুকের ক্ষয় ও ফাটল: তার বসার অংশে গভীর খাঁজ বা ফাটল আছে কিনা (ভালো: মসৃণ ও অক্ষত / ত্রুটি: গভীর খাঁজ বা ফাটল)',
          ),
        },
      ],
    },
    {
      id: 'shackle-inspect',
      defective: true,
      name: t(
        'シャックルの摩耗{まもう}・ねじ部{ぶ}点検{てんけん}',
        'Shackle wear and pin thread inspection',
        'শ্যাকলের ক্ষয় ও পিনের থ্রেড পরিদর্শন',
      ),
      spots: [
        {
          x: 40,
          y: 40,
          check: t(
            '本体{ほんたい}の変形{へんけい}：過大{かだい}な力{ちから}による曲{ま}がりや口{くち}開{ひら}きがないか（良品{りょうひん}：左右対称{さゆうたいしょう}で変形{へんけい}なし / 不良{ふりょう}：本体{ほんたい}が目視{もくし}で分{わ}かるほどねじれている）',
            'Body deformation: Any bending or opening from excess load (Good: Symmetrical without twist / Defect: Visibly twisted body)',
            'বডির বিকৃতি: অতিরিক্ত টানে বাঁকা বা মুখ চওড়া হয়েছে কিনা (ভালো: নিখুঁত প্রতিসম / ত্রুটি: খালি চোখেই স্পষ্ট বাঁকানো)',
          ),
        },
        {
          x: 60,
          y: 70,
          check: t(
            'ピンのねじ山{やま}・摩耗{まもう}：ピンが奥{おく}まで完全{かんぜん}に締{し}まりガタがないか（良品{りょうひん}：手{て}で最後{さいご}まで円滑{えんかつ}に締{し}まる / 不良{ふりょう}：ねじ山{やま}がつぶれて途中{とちゅう}で噛{か}み込{こ}む）',
            'Pin threads and seating: Does pin thread fully to collar without play (Good: Threads in smoothly / Defect: Damaged threads bind)',
            'পিনের থ্রেড ও সেটিং: পিন পুরো শেষ পর্যন্ত স্বাভাবিকভাবে আটকায় কিনা (ভালো: মসৃণভাবে পুরো আটকায় / ত্রুটি: প্যাঁচ নষ্ট হয়ে আটকে যায়)',
          ),
        },
      ],
    },
  ],
  quiz: [
    {
      q: t(
        '現場{げんば}で質量{しつりょう}1.5トンの鉄骨{てっこつ}をクレーンで吊{つ}り上{あ}げる玉掛{たまが}け作業{さぎょう}を行{おこな}うことになりました。この作業{さぎょう}を行{おこな}うために法律上{ほうりつじょう}必要{ひつよう}な資格{しかく}はどれですか？',
        'You are slinging a 1.5-ton steel beam with a crane on site. Which qualification is required by law to perform this task?',
        'কাজের জায়গায় ক্রেন দিয়ে ১.৫ টন ওজনের স্টিল বিম তোলার জন্য স্লিং বাঁধতে হবে। আইন অনুযায়ী কোন সনদটি আবশ্যক?',
      ),
      options: [
        t(
          '玉掛{たまが}け技能講習{ぎのうこうしゅう}の修了証{しゅうりょうしょう}（吊{つ}り上{あ}げ荷重{かじゅう}1トン以上{いじょう}）',
          'Slinging skill training certificate (for loads of 1 ton or more)',
          'স্লিং স্কিল ট্রেনিং সার্টিফিকেট (১ টন বা তার বেশি লোডের জন্য)',
        ),
        t(
          '1トン未満{みまん}の玉掛{たまが}け特別教育{とくべつきょういく}の修了証{しゅうりょうしょう}',
          'Special education certificate for slinging loads under 1 ton',
          '১ টনের কমের জন্য স্লিং বিশেষ শিক্ষা সনদ',
        ),
        t(
          'クレーン運転士{うんてんし}の免許{めんきょ}（玉掛{たまが}け講習{こうしゅう}は未受講{みじゅこう}）',
          'Crane operator license (without slinging training)',
          'ক্রেন অপারেটর লাইসেন্স (স্লিং প্রশিক্ষণ ছাড়া)',
        ),
        t(
          '普通自動車{ふつうじどうしゃ}第{だい}一種{いっしゅ}運転免許{うんてんめんきょ}',
          'Standard driver license',
          'সাধারণ মোটরগাড়ি ড্রাইভিং লাইসেন্স',
        ),
      ],
      answer: 0,
      why: t(
        '労働安全衛生法により、吊り上げ荷重1トン以上の玉掛け作業には「玉掛け技能講習」の修了が義務付けられています。特別教育では1トン以上の荷を扱うことはできません。',
        'Under the Industrial Safety and Health Act, slinging loads of 1 ton or more strictly requires the Skill Training Course. Special education covers only loads under 1 ton.',
        'শ্রম নিরাপত্তা আইন অনুসারে ১ টন বা তার বেশি লোডের স্লিং কাজের জন্য "স্কিল ট্রেনিং" সনদ বাধ্যতামূলক। বিশেষ শিক্ষা দিয়ে ১ টনের বেশি মাল পরিচালনা করা যায় না।',
      ),
    },
    {
      q: t(
        'ワイヤーロープ2本{ほん}を使{つか}って荷物{にもつ}を吊{つ}る際{さい}、2本{ほん}のワイヤーが成{な}す「吊{つ}り角度{かくど}」として現場{げんば}で守{まも}るべき基本{きほん}ルールはどれですか？',
        'When slinging a load with 2 wire ropes, which of the following is the basic rule to follow on site regarding the sling angle?',
        '২টি ওয়্যার রোপ দিয়ে মাল তোলার সময় স্লিং কোণের ক্ষেত্রে কাজের জায়গায় কোন মূল নিয়মটি মেনে চলা আবশ্যক?',
      ),
      options: [
        t(
          'ワイヤーが短{みじか}く済{す}むように、できるだけ90度以上{どいじょう}に大{おお}きく開{ひら}く',
          'Open wider than 90 degrees as much as possible to shorten wire length',
          'তারের দৈর্ঘ্য যাতে কম লাগে সেজন্য কোণ ৯০ ডিগ্রির চেয়ে বেশি চওড়া রাখা',
        ),
        t(
          'ワイヤーにかかる過大{かだい}な張力{ちょうりょく}を防{ふせ}ぐため、原則{げんそく}として60度以内{どいない}にする',
          'Keep within 60 degrees in principle to prevent excessive tension on the wires',
          'তারের ওপর অতিরিক্ত টান প্রতিরোধ করতে নীতিগতভাবে কোণ ৬০ ডিগ্রির মধ্যে রাখা',
        ),
        t(
          '荷物{にもつ}が揺{ゆ}れないように、角度{かくど}は120度{ど}ちょうどに固定{こてい}する',
          'Fix angle exactly at 120 degrees to prevent the load from swaying',
          'মাল যাতে না দোলে সেজন্য কোণ ঠিক ১২০ ডিগ্রিতে স্থির রাখা',
        ),
        t(
          '荷物{にもつ}の重{おも}さにかかわらず、角度{かくど}は自由{じゆう}に決{き}めてよい',
          'Angle can be chosen freely regardless of the weight of the load',
          'মালের ওজন যাই হোক না কেন কোণ ইচ্ছামতো নির্ধারণ করা যায়',
        ),
      ],
      answer: 1,
      why: t(
        '吊り角度が大きくなるほどワイヤー1本にかかる張力が増大します。安全のため吊り角度は原則60度以内に抑えます。',
        'As the sling angle widens, tension on each wire increases sharply. For safety, the sling angle must be kept within 60 degrees.',
        'স্লিং কোণ যত চওড়া হয়, প্রতিটি তারের টান তত মারাত্মকভাবে বৃদ্ধি পায়। নিরাপত্তার জন্য কোণ ৬০ ডিগ্রির মধ্যে রাখা বাধ্যতামূলক।',
      ),
    },
    {
      q: t(
        'クレーンで荷物{にもつ}を吊{つ}り上{あ}げる際{さい}、地面{じめん}から少{すこ}し浮{う}かせたところで一旦{いったん}停止{ていし}する「地切{じぎ}り」を行{おこな}います。地切{じぎ}りの高{たか}さと目的{もくてき}として正{ただ}しいものはどれですか？',
        'When hoisting with a crane, a "ground lift" is performed by stopping briefly after lifting off the floor. Which statement correctly describes its height and purpose?',
        'ক্রেন দিয়ে মাল তোলার সময় মাটি থেকে সামান্য তুলে কিছুক্ষণের জন্য থামানোর "গ্রাউন্ড-লিফ্ট" করা হয়। এর উচ্চতা ও উদ্দেশ্য হিসেবে কোনটি সঠিক?',
      ),
      options: [
        t(
          '床{ゆか}から1.5mまで上{あ}げて、荷物{にもつ}の下{した}を作業員{さぎょういん}が通{とお}れるようにする',
          'Hoist 1.5 m from floor so workers can walk underneath the load',
          'মেঝে থেকে ১.৫ মিটার উপরে তোলা যাতে কর্মীরা মালের নিচ দিয়ে চলাচল করতে পারেন',
        ),
        t(
          '床{ゆか}から5cmだけ浮{う}かせて、クレーンの巻{ま}き上{あ}げスピードを最大{さいだい}にする',
          'Lift just 5 cm off floor, then set crane hoisting speed to maximum',
          'মেঝে থেকে মাত্র ৫ সেমি তুলে ক্রেনের তোলার গতি সর্বোচ্চ করে দেওয়া',
        ),
        t(
          '床{ゆか}から約{やく}30cm浮{う}かせて一旦{いったん}停止{ていし}し、荷{に}の傾{かたむ}きや重心{じゅうしん}、ブレーキの効{き}きを確認{かくにん}する',
          'Lift approx 30 cm off floor, stop, and verify load tilt, center of gravity, and brake hold',
          'মেঝে থেকে প্রায় ৩০ সেমি উঁচুতে থামিয়ে মালের ঢাল, ভরকেন্দ্র ও ব্রেকের কার্যকারিতা যাচাই করা',
        ),
        t(
          '荷物{にもつ}を床{ゆか}に置{お}いたままワイヤーを限界{げんかい}まで強{つよ}く引{ひ}っ張{ぱ}る',
          'Keep load on floor while pulling wire ropes as tight as possible',
          'মাল মেঝেতে রেখেই ওয়্যার রোপকে সর্বোচ্চ শক্তিতে টেনে ধরা',
        ),
      ],
      answer: 2,
      why: t(
        '地切りは床から約30cm巻き上げて停止し、荷のバランスや重心のズレ、ワイヤーの掛かり具合とブレーキの制動を確認する重要な安全手順です。',
        'Ground lift is a crucial safety step: hoist about 30 cm off the ground, stop, and inspect balance, center of gravity, wire seating, and crane brakes.',
        'গ্রাউন্ড-লিফ্ট অত্যন্ত জরুরি নিরাপত্তা ধাপ: মেঝে থেকে প্রায় ৩০ সেমি তুলে থামিয়ে ভারসাম্য, ভরকেন্দ্রের অবস্থান, তার ঠিকমতো বসা ও ক্রেনের ব্রেক পরীক্ষা করা হয়।',
      ),
    },
    {
      q: t(
        'ワイヤーロープの始業前{しぎょうまえ}点検{てんけん}を行{おこな}っています。法令上{ほうれいじょう}、直{ただ}ちに使用{しよう}を中止{ちゅうし}して廃棄{はいき}しなければならないワイヤーロープの状態{じょうたい}はどれですか？',
        'You are conducting a pre-use inspection of wire ropes. Under regulations, which condition requires immediately discontinuing use and discarding the rope?',
        'ওয়্যার রোপের কাজ শুরুর পূর্ববর্তী পরিদর্শন করছেন। সরকারি বিধিমালার অধীনে অবিলম্বে ব্যবহার বন্ধ ও বাতিল করতে হবে কোন অবস্থায়?',
      ),
      options: [
        t(
          '表面{ひょうめん}に薄{うす}い油{あぶら}が付着{ふちゃく}して光{ひか}っている',
          'Thin layer of lubricant oil is present on the surface',
          'পৃষ্ঠতলে লুব্রিকেন্ট তেলের হালকা আস্তরণ দেখা যাচ্ছে',
        ),
        t(
          '製造番号{せいぞうばんごう}の刻印{こくいん}が少{すこ}し擦{す}れている',
          'Serial number stamp is slightly rubbed or faded',
          'সিরিয়াল নম্বরের খোদাই করা লেখা কিছুটা ঘষে গেছে',
        ),
        t(
          '全長{ぜんちょう}が新品{しんぴん}時{じ}より1cm短{みじか}い',
          'Total length is 1 cm shorter than when brand new',
          'নতুন অবস্থার চেয়ে মোট দৈর্ঘ্য ১ সেমি কম',
        ),
        t(
          '1撚{よ}りの間{あいだ}で素線{そせん}の10%以上{いじょう}が切断{せつだん}している、またはキンク（折{お}れ曲{ま}がり）がある',
          '10% or more wires broken within one lay, or a kink is present',
          'একটি লে দৈর্ঘ্যে ১০% বা বেশি তার ছেঁড়া, অথবা কিংক (কুঁচকে বাঁকা) রয়েছে',
        ),
      ],
      answer: 3,
      why: t(
        'クレーン等安全規則第219条により、1撚りでの素線切れ10%以上、直径減少7%超、キンクや著しい変形があるワイヤロープは使用が禁止されています。',
        'Under Crane Safety Ordinance Art. 219, wire rope with 10%+ broken wires per lay, diameter reduction over 7%, kinks, or severe deformation is strictly banned.',
        'ক্রেন নিরাপত্তা বিধির ২১৯ ধারা অনুযায়ী, ১ লে-তে ১০%+ তার ছেঁড়া, ৭% এর বেশি ব্যাস হ্রাস, কিংক বা বিকৃত ওয়্যার রোপ ব্যবহার সম্পূর্ণ নিষিদ্ধ।',
      ),
    },
    {
      q: t(
        'クレーンで吊{つ}り上{あ}げられた荷物{にもつ}が頭上{ずじょう}を移動{いどう}しています。玉掛{たまが}け作業者{さぎょうしゃ}として絶対{ぜったい}に取{と}ってはならない危険{きけん}な行動{こうどう}はどれですか？',
        'A suspended load lifted by a crane is moving overhead. As a slinger, which dangerous action must you NEVER take?',
        'ক্রেনে তোলা ঝুলন্ত মাল মাথার ওপর দিয়ে চলাচল করছে। স্লিং কর্মী হিসেবে কোন বিপজ্জনক কাজটি কখনো করা যাবে না?',
      ),
      options: [
        t(
          '荷物{にもつ}の傾{かたむ}きを手{て}で直{なお}すために、吊{つ}り荷{に}の真下{ました}に入{はい}り込{こ}む',
          'Stepping directly underneath the suspended load to adjust tilt by hand',
          'হাত দিয়ে মালের ঢাল সোজা করার জন্য ঝুলন্ত মালের ঠিক নিচে চলে যাওয়া',
        ),
        t(
          '荷物{にもつ}から十分{じゅうぶん}な距離{きょり}を取{と}って安全{あんぜん}な退避{たいひ}場所{ばしょ}に立{た}つ',
          'Standing in a safe clearance zone keeping sufficient distance from the load',
          'মাল থেকে পর্যাপ্ত দূরত্ব বজায় রেখে নিরাপদ স্থানে দাঁড়িয়ে থাকা',
        ),
        t(
          '荷{に}の振{ふ}れを止{と}めるために長{なが}い介錯{かいしゃく}ロープ（ガイドロープ）を遠{とお}くから持{も}つ',
          'Holding a long tag line from a distance to steady load swing',
          'মালের দোলনা থামাতে দূর থেকে লম্বা গাইড রোপ ধরে নিয়ন্ত্রণ করা',
        ),
        t(
          'クレーン運転士{うんてんし}と目{め}を合{あ}わせながら明確{めいかく}な手合図{てあいず}を行{おこな}う',
          'Giving clear hand signals while maintaining eye contact with crane operator',
          'ক্রেন চালকের দিকে দৃষ্টি রেখে সুস্পষ্ট হাতের ইশারায় সংকেত দেওয়া',
        ),
      ],
      answer: 0,
      why: t(
        '吊り荷の落下や荷崩れによる死亡災害を防ぐため、吊り荷の下への立ち入りは法律で固く禁止されています。',
        'Entering under a suspended load is strictly prohibited by law to prevent fatal accidents from falling loads or collapses.',
        'মাল পড়ে গিয়ে বা ভেঙে পড়ে প্রাণঘাতী দুর্ঘটনা ঠেকাতে ঝুলন্ত মালের নিচে প্রবেশ করা আইনত সম্পূর্ণ নিষিদ্ধ।',
      ),
    },
    {
      q: t(
        '形{かたち}が左右非対称{さゆうひたいしょう}で重{おも}さが偏{かたよ}っている大型{おおがた}機械{きかい}を玉掛{たまが}けします。吊{つ}り荷{に}が傾{かたむ}いて滑{すべ}り落{お}ちるのを防{ふせ}ぐための正{ただ}しい対処法{たいしょほう}はどれですか？',
        'You are slinging an asymmetric machine with off-center weight. What is the correct procedure to keep the load from tilting and sliding out?',
        'একপাশে বেশি ভারী এমন অসম আকারের বড় মেশিনে স্লিং বাঁধছেন। মাল হেলে গিয়ে পিছলে পড়া রোধে সঠিক পদ্ধতি কোনটি?',
      ),
      options: [
        t(
          '重心{じゅうしん}の位置{いち}は無視{むし}して、見た目の中心{ちゅうしん}にフックを合{あ}わせる',
          'Ignore center of gravity and place hook at the visual center',
          'ভরকেন্দ্র অগ্রাহ্য করে শুধুমাত্র চোখের আন্দাজে মাঝখানে হুক রাখা',
        ),
        t(
          '荷{に}の重心{じゅうしん}の真上{まうえ}にクレーンのフックを位置{いち}させ、ワイヤーの長{なが}さを調整{ちょうせつ}して水平{すいへい}に吊{つ}る',
          'Position hook directly above center of gravity and adjust wire lengths to lift level',
          'ক্রেনের হুক ঠিক ভরকেন্দ্রের সোজা উপরে রাখা এবং তারের দৈর্ঘ্য সমন্বয় করে মাল সমান রাখা',
        ),
        t(
          '軽{かる}い側{がわ}だけにワイヤーを1本{いっぽん}だけ掛{か}けて一気{いっき}に持{も}ち上{あ}げる',
          'Attach a single wire only on the light side and lift rapidly',
          'শুধু হালকা দিকে একটি মাত্র তার আটকে এক টানে তুলে ফেলা',
        ),
        t(
          'バランスを取{と}るために作業員{さぎょういん}が軽{かる}い側{がわ}の荷{に}の上{うえ}に乗{の}って重石{おもし}になる',
          'Have a worker ride on the lighter side of the load to act as counterweight',
          'ভারসাম্য আনতে হালকা পাশে কর্মীকে মালের ওপর চড়িয়ে ওজন বাড়ানো',
        ),
      ],
      answer: 1,
      why: t(
        'クレーンのフックは必ず荷の重心の真上に持ってきます。ワイヤーの長さを変えるかシャックル等を用いて荷が水平に保たれるよう調整します。',
        'The crane hook must always be positioned directly above the center of gravity. Adjust sling lengths or use shackles to ensure the load remains level.',
        'ক্রেনের হুক সর্বদা মালের ভরকেন্দ্রের ঠিক সোজা উপরে রাখতে হবে। তারের দৈর্ঘ্য ছোট-বড় করে বা শ্যাকল ব্যবহার করে মাল অনুভূমিক রাখতে হয়।',
      ),
    },
    {
      q: t(
        'クレーン作業中{さぎょうちゅう}、別{べつ}の作業員{さぎょういん}が吊{つ}り荷{に}の接近{せっきん}に気{き}づかず危険{きけん}エリアに歩{ある}いて入{はい}ってくるのを発見{はっけん}しました。どうすべきですか？',
        'During crane operations, you spot another worker unknowingly walking into the danger zone near the suspended load. What should you do?',
        'ক্রেন চলার সময় দেখলেন অন্য একজন কর্মী না বুঝে ঝুলন্ত মালের বিপজ্জনক এলাকায় হেঁটে ঢুকে পড়ছেন। কী করবেন?',
      ),
      options: [
        t(
          '合図者{あいずしゃ}に指名{しめい}されていないので、合図者{あいずしゃ}が気{き}づくまで何{なに}もせず見{み}ている',
          'Do nothing and watch until designated signalman notices, since you are not signalman',
          'সংকেতকারী নন বলে সংকেতকারীর চোখে না পড়া পর্যন্ত চুপচাপ চেয়ে থাকা',
        ),
        t(
          'クレーン運転士{うんてんし}に「もっと早{はや}く巻{ま}き上{あ}げろ」と合図{あいず}する',
          'Signal crane operator to hoist faster to get out of the way',
          'মাল সরিয়ে নিতে ক্রেন চালককে আরও দ্রুত তুলতে তাড়া দেওয়া',
        ),
        t(
          '直{ただ}ちに大声{おおごえ}と手合図{てあいず}でクレーンに「非常停止{ひじょうていし}！」を合図{あいず}し、作業員{さぎょういん}にも警告{けいこく}する',
          'Immediately shout and give hand signal for emergency stop to crane, and warn the worker',
          'অবিলম্বে চিৎকার ও হাতের ইশারায় ক্রেনকে "ইমার্জেন্সি স্টপ" সংকেত দেওয়া এবং কর্মীকে সতর্ক করা',
        ),
        t(
          '作業{さぎょう}が遅{おく}れると困{こま}るので、その人{ひと}が通{とお}り過{す}ぎるまで黙{だま}っている',
          'Stay silent until the person walks past to avoid work delays',
          'কাজে দেরি হবে ভেবে লোকটি পার হয়ে যাওয়া পর্যন্ত নীরব থাকা',
        ),
      ],
      answer: 2,
      why: t(
        '通常の運転合図は指定された合図者が行いますが、非常停止の合図は人命を守るため、危険を発見した誰もが直ちに出さなければなりません。',
        'While routine signals are given by designated signal persons, an emergency stop signal must be issued immediately by anyone who detects danger.',
        'সাধারণ সংকেত নির্ধারিত ব্যক্তি দিলেও, জীবন বাঁচাতে জরুরি থামার সংকেত বিপদ দেখতে পাওয়া যে কেউই অবিলম্বে দিতে পারেন।',
      ),
    },
    {
      q: t(
        '吊{つ}り荷{に}を降{お}ろす際{さい}、安全{あんぜん}に荷{に}を下敷{したじ}き（リンギ・盤木{ばんぎ}）の上{うえ}に設置{せっち}する正{ただ}しい手順{てじゅん}はどれですか？',
        'When lowering a load onto wooden dunnage blocks (ringi), what is the correct safety procedure?',
        'ঝুলন্ত মাল কাঠের ব্লকের (রিঙ্গি) ওপর নামিয়ে রাখার ক্ষেত্রে সঠিক ও নিরাপদ পদ্ধতি কোনটি?',
      ),
      options: [
        t(
          '荷物{にもつ}が地面{じめん}に着{つ}く直前{ちょくぜん}に、手{て}を荷{に}の底{そこ}に差{さ}し込{こ}んでリンギの位置{いち}を直{なお}す',
          'Reach hand underneath the descending load right before touchdown to adjust blocks',
          'মাল মাটিতে পড়ার ঠিক আগে নিচে হাত ঢুকিয়ে কাঠের ব্লকের অবস্থান ঠিক করা',
        ),
        t(
          'ワイヤーの張力{ちょうりょく}が残{のこ}っている状態{じょうたい}で、無理{むり}やり手{て}でワイヤーを引{ひ}き抜{ぬ}く',
          'Forcefully pull out wire ropes by hand while tension remains on the sling',
          'তারের ওপর টান থাকা অবস্থাতেই হাত দিয়ে জোর করে তার টেনে বের করা',
        ),
        t(
          '着地{ちゃくち}場所{ばしょ}を確認{かくにん}せず、クレーン運転士{うんてんし}の判断{はんだん}だけで急降下{きゅうこうか}させる',
          'Drop load rapidly based solely on crane operator judgment without checking ground',
          'জায়গা পরীক্ষা না করে ক্রেন চালকের একক সিদ্ধান্তে দ্রুত গতিতে নামিয়ে দেওয়া',
        ),
        t(
          '荷{に}を床面{ゆかめん}の直前{ちょくぜん}で一旦{いったん}停止{ていし}させ、リンギの位置{いち}と挟{はさ}まれの危険{きけん}がないことを確認{かくにん}して静{しず}かに着地{ちゃくち}させる',
          'Stop load momentarily right before contact, verify block position and pinch points, then lower gently',
          'মেঝে স্পর্শের ঠিক আগে একবার থামিয়ে ব্লকের অবস্থান ও চাপা পড়ার ঝুঁকি দেখে নিয়ে ধীরে নামানো',
        ),
      ],
      answer: 3,
      why: t(
        '荷降ろし時も着地直前で一旦停止し、手や足の挟まれを防ぎます。荷の下に手を差し込んではいけません。',
        'Always pause just above touchdown when lowering to prevent hand and foot pinch injuries; never reach hands under a suspended load.',
        'মাল নামানোর সময়ও স্পর্শের ঠিক আগে থামিয়ে হাত-পা চাপা পড়ার ঝুঁকি ঠেকাতে হয়; মালের নিচে কখনো হাত ঢোকানো যাবে না।',
      ),
    },
    {
      q: t(
        'クレーンのフックに付{つ}いている「外{はず}れ止{ど}め装置{そうち}（安全{あんぜん}ラッチ）」の役割{やくわり}として正{ただ}しいものはどれですか？',
        'Which statement correctly describes the function of the hook safety latch on a crane hook?',
        'ক্রেন হুকের সাথে থাকা "সেফটি ল্যাচ" এর মূল কাজ সম্পর্কে কোনটি সঠিক?',
      ),
      options: [
        t(
          '荷{に}の揺{ゆ}れや巻{ま}き上{あ}げ時{じ}に、ワイヤーロープがフックから外{はず}れて荷{に}が墜落{ついらく}するのを防{ふせ}ぐ',
          'Prevent wire ropes from slipping off hook during load swing or hoist, preventing drops',
          'মাল দোলা বা তোলার সময় তার হুক থেকে ছিটকে মাল নিচে পড়ে যাওয়া রোধ করা',
        ),
        t(
          'フックの重{おも}さを軽{かる}くしてクレーンの吊{つ}り上{あ}げ能力{のうりょく}を高{たか}める',
          'Reduce hook weight to increase crane hoisting capacity',
          'হুকের ওজন কমিয়ে ক্রেনের উত্তোলনের ক্ষমতা বৃদ্ধি করা',
        ),
        t(
          'ワイヤーロープの摩耗{まもう}を完全{かんぜん}になくす',
          'Completely eliminate all friction and wear on wire ropes',
          'ওয়্যার রোপের সব ধরনের ঘর্ষণ ও ক্ষয় সম্পূর্ণ দূর করা',
        ),
        t(
          'フックを自動的{じどうてき}に回転{かいてん}させて荷{に}の向{む}きを変{か}える',
          'Automatically rotate hook to reorient load direction',
          'হুককে স্বয়ংক্রিয়ভাবে ঘুরিয়ে মালের দিক পরিবর্তন করা',
        ),
      ],
      answer: 0,
      why: t(
        '外れ止め装置は、吊り作業中にワイヤーがフックの先端から飛び出して荷が落下する重大事故を防ぐ命綱です。',
        'The safety latch is a critical failsafe that prevents slinging wires from detaching from hook tip and causing catastrophic dropped loads.',
        'সেফটি ল্যাচ একটি জীবনরক্ষাকারী ব্যবস্থা যা কাজের সময় হুকের মুখ থেকে তার ছিটকে মাল পড়ে যাওয়া রোধ করে।',
      ),
    },
    {
      q: t(
        '風{かぜ}が強{つよ}い日{ひ}の造船所{ぞうせんじょ}で、面積{めんせき}の広{ひろ}い鋼板{こうはん}をクレーンで吊{つ}り上{あ}げる作業{さぎょう}を行{おこな}うことになりました。最も適切{てきせつ}な安全{あんぜん}対策{たいさく}はどれですか？',
        'In a shipyard on a windy day, you must lift a large-area steel plate by crane. What is the most appropriate safety measure?',
        'বাতাসপূর্ণ দিনে শিপইয়ার্ডে ক্রেন দিয়ে একটি বিশাল আকৃতির স্টিল প্লেট তুলতে হবে। সবচেয়ে উপযুক্ত নিরাপত্তা ব্যবস্থা কোনটি?',
      ),
      options: [
        t(
          '風{かぜ}に負{ま}けないよう、クレーンを最高速度{さいこうそくど}で高速{こうそく}旋回{せんかい}させる',
          'Slew crane at maximum speed to overpower the wind force',
          'বাতাসের সাথে পাল্লা দিতে ক্রেনকে সর্বোচ্চ গতিতে ঘোরানো',
        ),
        t(
          '荷{に}の振{ふ}れや回転{かいてん}を抑{おさ}えるために両端{りょうたん}に「介錯{かいしゃく}ロープ」を取{と}り付{つ}け、強風時{きょうふうじ}は作業{さぎょう}を中止{ちゅうし}する',
          'Attach tag lines to both ends to control swing and spin, and halt work during strong winds',
          'মাল দোলা ও ঘোরা আটকাতে দুই প্রান্তে গাইড রোপ বাঁধা, এবং তীব্র বাতাসে কাজ বন্ধ রাখা',
        ),
        t(
          '作業員{さぎょういん}数名{すうめい}が鋼板{こうはん}に直接{ちょくせつ}抱{だ}きついて風{かぜ}による揺{ゆ}れを手{て}で押{お}さえる',
          'Have several workers embrace the steel plate directly to hold it steady',
          'কয়েকজন কর্মী মিলে সরাসরি স্টিল প্লেট জড়িয়ে ধরে হাতের শক্তিতে থামিয়ে রাখা',
        ),
        t(
          '天気予報{てんきよほう}は見{み}ずに、突風{とっぷう}が吹{ふ}いても作業{さぎょう}を続行{ぞっこう}する',
          'Ignore weather forecasts and keep working even during severe gusts',
          'আবহাওয়ার পূর্বাভাস না দেখে ঝড়ো বাতাস থাকলেও কাজ চালিয়ে যাওয়া',
        ),
      ],
      answer: 1,
      why: t(
        '広い板状の荷は風の影響を大きく受けます。直接手で触れず介錯ロープで制御し、強風時は法令に基づき作業を中止します。',
        'Broad plate loads catch wind easily. Control them using tag lines from safe distance, and suspend operations during strong winds pursuant to regulations.',
        'চওড়া প্লেট বাতাসে সহজে ওড়ে। নিরাপদ দূর থেকে গাইড রোপ দিয়ে সামলাতে হবে এবং ঝড়ো বাতাসে আইন অনুযায়ী কাজ বন্ধ রাখতে হবে।',
      ),
    },
  ],
  glossary: [
    {
      ja: '玉掛け',
      reading: 'たまがけ',
      en: 'crane slinging / rigging',
      bn: 'ক্রেন স্লিং বাঁধা ও খোলা',
      note: t(
        'クレーンなどのフックに荷{に}を掛{か}けたり外{はず}したりする一連{いちれん}の作業{さぎょう}。',
        'Series of operations for attaching and detaching loads to crane hooks.',
        'ক্রেনের হুকে মাল আটকানো ও খোলার সামগ্রিক কাজ।',
      ),
    },
    {
      ja: '地切り',
      reading: 'じぎり',
      en: 'ground lift (30 cm initial lift)',
      bn: 'গ্রাউন্ড-লিফ্ট (৩০ সেমি প্রাথমিক উত্তোলন)',
      note: t(
        '荷{に}を床面{ゆかめん}から約{やく}30cm浮{う}かせて一旦{いったん}停止{ていし}し、重心{じゅうしん}・ブレーキ・ワイヤーの掛{か}かりを確認{かくにん}する作業{さぎょう}。',
        'Lifting load approx 30 cm off floor and pausing to check balance, brake, and wires.',
        'মেঝে থেকে প্রায় ৩০ সেমি উপরে তুলে থামিয়ে ভারসাম্য, ব্রেক ও তারের অবস্থান যাচাই।',
      ),
    },
    {
      ja: '吊り角度',
      reading: 'つりかくど',
      en: 'sling angle',
      bn: 'স্লিং কোণ',
      note: t(
        '2本{ほん}以上{いじょう}のワイヤーロープが荷{に}を吊{つ}るときに成{な}す内角{ないかく}。原則{げんそく}60度以内{どいない}。',
        'Included angle between 2 or more sling legs. Should be within 60 degrees.',
        'একাধিক ওয়্যার রোপ দিয়ে তোলার সময় তাদের মধ্যকার কোণ। নীতিগতভাবে ৬০ ডিগ্রির মধ্যে রাখা হয়।',
      ),
    },
    {
      ja: 'ワイヤーロープ',
      reading: 'わいやーろーぷ',
      en: 'wire rope',
      bn: 'ওয়্যার রোপ (ইস্পাতের তারের দড়ি)',
      note: t(
        '鋼線{こうせん}（素線{そせん}）を撚{よ}り合{あ}わせた強靱{きょうじん}なロープ。玉掛{たまが}けの主用具{しゅようぐ}。',
        'Tough rope twisted from steel wires. Primary tool for slinging.',
        'ইস্পাতের চিকন তার পেঁচিয়ে তৈরি শক্ত দড়ি। স্লিং বাঁধার প্রধান সরঞ্জাম।',
      ),
    },
    {
      ja: '吊り荷',
      reading: 'つりに',
      en: 'suspended load / hanging load',
      bn: 'ঝুলন্ত মাল',
      note: t(
        'クレーンによって空柱{くうちゅう}に吊{つ}り上{あ}げられている荷物{にもつ}。下{した}への立入{たちいり}は絶対{ぜったい}禁止{きんし}。',
        'Load suspended in midair by crane. Standing underneath is strictly prohibited.',
        'ক্রেন দ্বারা আকাশে ঝুলিয়ে রাখা মাল। এর নিচে দাঁড়ানো সম্পূর্ণ নিষিদ্ধ।',
      ),
    },
    {
      ja: '重心',
      reading: 'じゅうしん',
      en: 'center of gravity',
      bn: 'ভরকেন্দ্র (ভারসাম্যের কেন্দ্রবিন্দু)',
      note: t(
        '荷物{にもつ}の重{おも}さのバランスが取{と}れる中心点{ちゅうしんてん}。フックは必{かなら}ず重心{じゅうしん}の真上{まうえ}に合{あ}わせる。',
        'Balance point of load weight. Crane hook must always be aligned directly above it.',
        'মালের ওজনের ভারসাম্যের কেন্দ্র। ক্রেনের হুক অবশ্যই ভরকেন্দ্রের ঠিক সোজা উপরে রাখতে হয়।',
      ),
    },
    {
      ja: '合図',
      reading: 'あいず',
      en: 'crane hand signals',
      bn: 'ক্রেনের সংকেত বা ইশারা',
      note: t(
        '手{て}や笛{ふえ}を使{つか}ってクレーン運転士{うんてんし}に動作{どうさ}を伝{つた}える明確{めいかく}なサイン。',
        'Clear signs using hands or whistle to direct crane operators.',
        'হাত বা বাঁশি ব্যবহার করে ক্রেন চালককে দিকনির্দেশনা দেওয়ার সুস্পষ্ট সংকেত।',
      ),
    },
    {
      ja: '外れ止め装置',
      reading: 'はずれどめそうち',
      en: 'hook safety latch',
      bn: 'হুকের সেফটি ল্যাচ',
      note: t(
        'フックからワイヤーロープが外{はず}れ落{お}ちるのを防{ふせ}ぐバネ式{しき}の安全{あんぜん}金具{かなぐ}。',
        'Spring-loaded safety latch preventing wire ropes from slipping off hook.',
        'হুক থেকে তার ছিটকে বাইরে বেরিয়ে যাওয়া রোধকারী স্প্রিংযুক্ত সুরক্ষা কব্জা।',
      ),
    },
    {
      ja: '介錯ロープ',
      reading: 'かいしゃくろーぷ',
      en: 'tag line / guide rope',
      bn: 'গাইড রোপ বা নিয়ন্ত্রণ দড়ি',
      note: t(
        '吊{つ}り荷{に}の振{ふ}れや回転{かいてん}を離{はな}れた位置{いち}から安全{あんぜん}に制御{せいぎょ}するための補助{ほじょ}ロープ。',
        'Auxiliary rope used from a safe distance to prevent suspended load swing or spin.',
        'ঝুলন্ত মালের দোলনা বা ঘোরা নিরাপদ দূর থেকে নিয়ন্ত্রণ করার সহায়ক দড়ি।',
      ),
    },
    {
      ja: 'キンク',
      reading: 'きんく',
      en: 'kink (wire rope damage)',
      bn: 'কিংক (তারের বিকৃতি ও স্থায়ী ভাঁজ)',
      note: t(
        'ワイヤーロープが輪{わ}になった状態{じょうたい}で引{ひ}っ張{ぱ}られて生{しょう}じる鋭角{えいかく}な折{お}れ曲{ま}がり損傷{そんしょう}。即{そく}廃棄{はいき}。',
        'Sharp bend damage caused when looping wire rope is pulled under tension. Immediate discard.',
        'লুপ হয়ে থাকা তারে টান পড়লে সৃষ্ট স্থায়ী বিকৃত ভাঁজ। দেখা মাত্রই বাতিল করতে হয়।',
      ),
    },
    {
      ja: '過荷重',
      reading: 'かかじゅう',
      en: 'overload',
      bn: 'অতিরিক্ত ভার বা ওভারলোড',
      note: t(
        'クレーンやワイヤーの制限{せいげん}荷重{かじゅう}（許容荷重{きょようかじゅう}）を超{こ}える重量{じゅうりょう}。絶対{ぜったい}禁止{きんし}。',
        'Weight exceeding the rated capacity of crane or wire rope. Strictly forbidden.',
        'ক্রেন বা তারের বহন ক্ষমতার চেয়ে বেশি ওজন তোলা। সম্পূর্ণ নিষিদ্ধ।',
      ),
    },
    {
      ja: '巻上げ',
      reading: 'まきあげ',
      en: 'hoisting / lifting',
      bn: 'উত্তোলন বা ওপরে তোলা',
      note: t(
        'ワイヤーロープを巻{ま}き取{と}って荷物{にもつ}を上{うえ}に引{ひ}き上{あ}げるクレーン動作{どうさ}。',
        'Crane motion of winding up wire rope to raise the load.',
        'ওয়্যার রোপ টেনে মাল ওপরের দিকে তোলার ক্রেনের চলন।',
      ),
    },
  ],
}
