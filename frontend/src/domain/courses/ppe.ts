import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string, bn: string): Tri => ({ ja, en, bn })

export const ppe: CourseData = {
  id: 'ppe',
  category: 'core',
  level: 1,
  minutes: 15,
  title: t(
    '保護具{ほごぐ}',
    'Personal protective equipment (PPE)',
    'ব্যক্তিগত সুরক্ষা সরঞ্জাম (পিপিই)',
  ),
  sub: t(
    '身{み}の安全{あんぜん}を守{まも}るため、正{ただ}しい保護具{ほごぐ}を選{えら}び、正{ただ}しく身{み}につける。',
    'Choose and wear the right PPE correctly to protect your life.',
    'নিজের জীবন বাঁচাতে সঠিক সুরক্ষা সরঞ্জাম বাছাই করো এবং সঠিকভাবে পরিধান করো।',
  ),
  refs: [
    '労働安全衛生法 第20条・第21条（事業者の講ずべき措置等）',
    '労働安全衛生規則 第593条〜第599条（保護具）',
    '厚生労働省 墜落制止用器具の安全な使用に関するガイドライン',
    '厚生労働省 保護帽の着用規定及び定期交換基準',
  ],
  panels: [
    {
      who: 'foreman',
      text: t(
        '今日{きょう}は工場{こうじょう}での保護具{ほごぐ}の正{ただ}しい使{つか}い方{かた}を確認{かくにん}します。',
        'Today we will confirm the correct use of personal protective equipment in the factory.',
        'আজ আমরা কারখানায় ব্যক্তিগত সুরক্ষা সরঞ্জামের সঠিক ব্যবহার যাচাই করব।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'はい！ヘルメットを被{かぶ}り、安全靴{あんぜんぐつ}も履{は}いてきました。',
        'Yes! I put on my helmet and also wore safety shoes.',
        'জি! আমি হেলমেট পরেছি এবং সেফটি জুতোও পরে এসেছি।',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'ヘルメットのあご紐{ひも}が緩{ゆる}んでいますよ。指{ゆび}が1本{いっぽん}入{はい}る程度{ていど}に締{し}めましょう。',
        'Your helmet chin strap is loose. Tighten it so that about one finger fits.',
        'তোমার হেলমেটের ফিতা আলগা হয়ে আছে। একটা আঙুল ঢোকার মতো মাপে শক্ত করে আঁটো।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'カチッと締{し}めました！この保護{ほご}メガネは、少{すこ}し曇{くも}るので外{はず}してもいいですか？',
        'I clicked it tight! These safety glasses fog up a little; may I take them off?',
        'ক্লিক করে আঁটলাম! এই চশমা কিছুটা ঘোলা হয়, আমি কি খুলে রাখতে পারি?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '「少{すこ}しだけ」でも外{はず}してはいけません。研磨{けんま}の火花{ひばな}や破片{はへん}が目{め}に入{はい}ると失明{しつめい}の危険{きけん}があります。',
        'Never remove them even for a moment. Grinding sparks or flying chips can blind you.',
        'এক মুহূর্তের জন্যও খোলা যাবে না। গ্রাইন্ডিংয়ের স্ফুলিঙ্গ বা কণা চোখে লাগলে অন্ধ হওয়ার ঝুঁকি থাকে।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '危{あぶ}ないですね。それでは、ボール盤{ばん}で穴{あな}をあける作業{さぎょう}に軍手{ぐんて}をつけて入{はい}ります！',
        'That is dangerous. Then, I will wear cotton work gloves to drill holes on the drilling machine!',
        'খুবই বিপজ্জনক। তাহলে আমি ড্রিলিং মেশিনে ফুটো করতে সুতি গ্লাভস পরে কাজে যাচ্ছি!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '待{ま}ちなさい！回転{かいてん}する機械{きかい}では手袋{てぶくろ}が巻{ま}き込{こ}まれるので、手袋{てぶくろ}の着用{ちゃくよう}は禁止{きんし}です。',
        'Stop! Wearing gloves is strictly prohibited near rotating machines because they can get caught.',
        'থামো! ঘূর্ণায়মান মেশিনে গ্লাভস জড়িয়ে হাত টেনে নিতে পারে, তাই এখানে গ্লাভস পরা সম্পূর্ণ নিষিদ্ধ।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '素手{すで}で作業{さぎょう}するのですね。危{あぶ}なく巻{ま}き込{こ}まれるところでした。',
        'So I must work with bare hands. I was close to a dangerous entanglement.',
        'তাহলে খালি হাতে কাজ করতে হবে। প্রায় জড়িয়ে যাওয়ার মতো বিপদে পড়ে যাচ্ছিলাম।',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '高所{こうしょ}作業{さぎょう}ではフルハーネス、粉{ふん}じん作業{さぎょう}では防{ぼう}じんマスクと、作業{さぎょう}ごとに選{えら}びます。',
        'Choose for each job: full harness for high places, and dust mask for dusty work.',
        'কাজের ধরন বুঝে বাছাই করতে হবে: উঁচুতে ফুল হারনেস, আর ধূলিবালির কাজে ডাস্ট মাস্ক।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '自分{じぶん}の身{み}を守{まも}るため、ルールを守{まも}って正{ただ}しく保護具{ほごぐ}を着用{ちゃくよう}します！',
        'To protect myself, I will obey the rules and wear my PPE correctly!',
        'নিজেকে নিরাপদে রাখতে আমি সব নিয়ম মেনে সঠিকভাবে সুরক্ষা সরঞ্জাম পরব!',
      ),
    },
  ],
  handbook: [
    t(
      'ヘルメット（保護帽{ほごぼう}）のあご紐{ひも}は緩{ゆる}みなく必{かなら}ず締{し}める。',
      'Always fasten the helmet chin strap securely without any slack.',
      'হেলমেটের ফিতা কখনো আলগা রাখবে না, অবশ্যই শক্ত করে আঁটবে।',
    ),
    t(
      '一度{いちど}でも強{つよ}い衝撃{しょうげき}を受{う}けたヘルメットは、外見{がいけん}に傷{きず}がなくても交換{こうかん}・廃棄{はいき}する。',
      'Replace and discard any helmet that took a strong impact, even if it looks undamaged.',
      'একবারও প্রচণ্ড আঘাত পাওয়া হেলমেট বাইরে থেকে ভালো দেখালেও বদলে ফেলে দাও।',
    ),
    t(
      'FRP製{せい}ヘルメットは約{やく}5年{ねん}、熱可塑性樹脂製{ねつかそせいじゅしせい}は約{やく}3年{ねん}で定期的{ていきてき}に交換{こうかん}する。', // verify: FRP製保護帽は使用開始から約5年、熱可塑性樹脂製は約3年を目安に定期交換
      'Replace FRP helmets after roughly 5 years and thermoplastic ones after about 3 years.',
      'এফআরপি হেলমেট প্রায় ৫ বছর এবং থার্মোপ্লাস্টিক হেলমেট প্রায় ৩ বছর ব্যবহারের পর বদলে ফেলতে হয়।',
    ),
    t(
      'ボール盤{ばん}・旋盤{せんばん}など回転{かいてん}する機械{きかい}の作業{さぎょう}では、巻{ま}き込{こ}まれ防止{ぼうし}のため手袋{てぶくろ}を着用{ちゃくよう}してはならない。',
      'Never wear gloves near rotating machines like drills and lathes to avoid entanglement.',
      'ড্রিল বা লেদ মেশিনের মতো ঘূর্ণায়মান যন্ত্রপাতিতে জড়িয়ে যাওয়া রোধে কখনো গ্লাভস পরবে না।',
    ),
    t(
      '高{たか}さ2m以上{いじょう}の手すりのない場所{ばしょ}では、墜落制止用器具{ついらくせいしようきぐ}（フルハーネス型{がた}）を正{ただ}しく着用{ちゃくよう}する。',
      'Wear a full-harness fall arrest system properly when working at heights of 2 meters or more without handrails.',
      'রেলিংবিহীন ২ মিটার বা তার বেশি উচ্চতায় অবশ্যই ফুল হারনেস সঠিকভাবে পরিধান করতে হবে।',
    ),
    t(
      'フルハーネスのフックは、腰{こし}より高{たか}い位置{いち}の強固{きょうこ}な構造物{こうぞうぶつ}に確実{かくじつ}に掛{か}ける。',
      'Hook the full harness lanyard securely to a sturdy structure higher than waist level.',
      'ফুল হারনেসের হুকটি কোমরের চেয়ে উঁচুতে থাকা কোনো শক্ত কাঠামোর সাথে নিশ্চিতভাবে আটকাও।',
    ),
    t(
      '研磨{けんま}や切断{せつだん}など火花{ひばな}や粉{ふん}じんが飛散{ひさん}する作業{さぎょう}では、必{かなら}ず保護{ほご}メガネを着用{ちゃくよう}し、「少{すこ}しだけ」でも外{はず}さない。',
      'Always wear safety glasses during grinding or cutting; never remove them even for a second.',
      'গ্রাইন্ডিং বা কাটার কাজে অবশ্যই সুরক্ষা চশমা পরো; "এক মুহূর্তের জন্যও" তা খুলবে না।',
    ),
    t(
      '有害{ゆうがい}な粉{ふん}じんには防{ぼう}じんマスク、有機溶剤{ゆうきようざい}やガスには防毒{ぼうどく}マスクを正{ただ}しく選{えら}んで使{つか}い分{わ}ける。',
      'Use a dust mask for hazardous dust, and a gas mask for organic solvents or toxic vapors.',
      'ধূলিকণার জন্য ডাস্ট মাস্ক এবং রাসায়নিক গ্যাস বা বাষ্পের জন্য গ্যাস মাস্ক সঠিকভাবে বেছে নাও।',
    ),
    t(
      'マスクの装着後{そうちゃくご}は、息{いき}を吸{す}い込{こ}んで顔{かお}に密着{みっちゃく}しているか（フィットテスト）を必{かなら}ず確認{かくにん}する。',
      'After putting on a mask, inhale to perform a seal check and confirm it fits tight to your face.',
      'মাস্ক পরার পর শ্বাস টেনে পরীক্ষা করো যে এটি মুখের সাথে পুরোপুরি শক্তভাবে বসেছে কিনা।',
    ),
    t(
      '足元{あしもと}への物{もの}の落下{らっか}や踏{ふ}み抜{ぬ}きから守{まも}るため、先芯{さきしん}入{い}りの安全靴{あんぜんぐつ}を正{ただ}しく履{は}き、かかとを踏{ふ}まない。',
      'Wear steel-toe safety shoes properly to protect feet from falling objects, and never crush the heels.',
      'পা কেটে যাওয়া ও ভারী জিনিস পড়া থেকে বাঁচতে সেফটি জুতো ঠিকমতো পরো, গোড়ালি দাবিয়ে পরবে না।',
    ),
  ],
  reel: [
    t(
      'ヘルメット、あご紐{ひも}締{し}まり、ヨシ！',
      'Helmet, chin strap tightened, yoshi!',
      'হেলমেট, চিনস্ট্র্যাপ শক্ত, ঠিক আছে!',
    ),
    t(
      '保護{ほご}メガネ、密着{みっちゃく}・傷{きず}なし、ヨシ！',
      'Safety glasses, snug fit and no scratches, yoshi!',
      'সুরক্ষা চশমা, আঁটসাঁট ও দাগহীন, ঠিক আছে!',
    ),
    t(
      '回転{かいてん}機械{きかい}、手袋{てぶくろ}なし・素手{すで}、ヨシ！',
      'Rotating machine, no gloves and bare hands, yoshi!',
      'ঘূর্ণায়মান মেশিন, গ্লাভস নেই ও খালি হাত, ঠিক আছে!',
    ),
    t(
      '防{ぼう}じんマスク、密着{みっちゃく}確認{かくにん}、ヨシ！',
      'Dust mask, seal check confirmed, yoshi!',
      'ডাস্ট মাস্ক, ফিট সিল ঠিক আছে!',
    ),
    t(
      '安全靴{あんぜんぐつ}、先芯{さきしん}・紐{ひも}締{し}め、ヨシ！',
      'Safety shoes, toe cap intact and laces tied, yoshi!',
      'সেফটি জুতো, টো-ক্যাপ ও ফিতা বাঁধা, ঠিক আছে!',
    ),
    t(
      'フルハーネス、腿{もも}ベルト緩{ゆる}みなし、ヨシ！',
      'Full harness, thigh straps secure without slack, yoshi!',
      'ফুল হারনেস, থাই বেল্ট আলগা নেই, ঠিক আছে!',
    ),
    t(
      'ランヤード、フック掛{か}け位置{いち}・腰{こし}上{うえ}、ヨシ！',
      'Lanyard, hook attached above waist, yoshi!',
      'ল্যানিয়ার্ড, হুক কোমরের ওপরে আটকানো, ঠিক আছে!',
    ),
  ],
  drills: [
    {
      id: 'ppe-hazard-spot',
      kind: 'spot',
      secs: 30,
      need: 4,
      name: t(
        '保護具{ほごぐ}の不安全{ふあんぜん}チェック',
        'PPE unsafe practices check',
        'পিপিই ব্যবহারের বিপজ্জনক ভুলত্রুটি শনাক্তকরণ',
      ),
      goal: t(
        '30秒{びょう}で保護具{ほごぐ}に関{かん}する危険{きけん}な状態{じょうたい}を4つ見{み}つける。',
        'Find 4 dangerous PPE hazards in 30 seconds.',
        '৩০ সেকেন্ডে পিপিই সংক্রান্ত ৪টি অনিরাপদ ঝুঁকি খুঁজে বের করো।',
      ),
      items: [
        {
          hazard: true,
          label: t(
            'ヘルメットのあご紐{ひも}を締{し}めずに作業{さぎょう}している',
            'Working without fastening helmet chin strap',
            'হেলমেটের ফিতা না বেঁধে কাজ করা',
          ),
        },
        {
          hazard: false,
          label: t(
            '安全靴{あんぜんぐつ}の紐{ひも}をしっかり結{むす}んでいる',
            'Laces of safety shoes tied firmly',
            'সেফটি জুতোর ফিতা শক্ত করে বাঁধা',
          ),
        },
        {
          hazard: true,
          label: t(
            'ボール盤{ばん}作業{さぎょう}で軍手{ぐんて}を着用{ちゃくよう}している',
            'Wearing cotton work gloves while operating a drilling machine',
            'ড্রিলিং মেশিন চালানোর সময় সুতি গ্লাভস পরা',
          ),
        },
        {
          hazard: false,
          label: t(
            '研磨{けんま}作業{さぎょう}で保護{ほご}メガネを着用{ちゃくよう}している',
            'Wearing safety glasses during grinding work',
            'গ্রাইন্ডিং কাজের সময় সুরক্ষা চশমা পরা',
          ),
        },
        {
          hazard: true,
          label: t(
            '「少{すこ}しだけ」と保護{ほご}メガネを外{はず}してグラインダーを使{つか}う',
            'Removing safety glasses "just for a minute" while using grinder',
            '"সামান্য সময়ের জন্য" চশমা খুলে গ্রাইন্ডার চালানো',
          ),
        },
        {
          hazard: false,
          label: t(
            '高所{こうしょ}作業{さぎょう}でフルハーネスのフックを腰{こし}より上{うえ}に掛{か}けている',
            'Hooking full harness lanyard above waist during high work',
            'উঁচুতে কাজের সময় ফুল হারনেসের হুক কোমরের ওপরে আটকানো',
          ),
        },
        {
          hazard: true,
          label: t(
            '強{つよ}い衝撃{しょうげき}を受{う}けて亀裂{きれつ}が入{はい}ったヘルメットをそのまま使{つか}う',
            'Continuing to use a cracked helmet that suffered strong impact',
            'প্রচণ্ড আঘাত পেয়ে ফাটল ধরা হেলমেট ব্যবহার করে যাওয়া',
          ),
        },
        {
          hazard: false,
          label: t(
            '防{ぼう}じんマスクを装着{そうちゃく}したあと顔{かお}への密着{みっちゃく}テストをする',
            'Performing face seal check after putting on dust mask',
            'ডাস্ট মাস্ক পরার পর মুখে ফিট পরীক্ষা করা',
          ),
        },
        {
          hazard: true,
          label: t(
            '安全靴{あんぜんぐつ}のかかとを踏{ふ}みつぶして歩{ある}いている',
            'Walking with heels of safety shoes crushed down',
            'সেফটি জুতোর গোড়ালি দুমড়ে-মুচড়ে জুতো পরে হাঁটা',
          ),
        },
      ],
    },
    {
      id: 'full-harness-seq',
      kind: 'seq',
      name: t(
        'フルハーネスの正{ただ}しい装着{そうちゃく}手順{てじゅん}',
        'Correct sequence for putting on a full harness',
        'ফুল হারনেস পরার সঠিক ধারাবাহিক ধাপ',
      ),
      goal: t(
        '墜落制止用器具{ついらくせいしようきぐ}を安全{あんぜん}に装着{そうちゃく}する5つの手順{てじゅん}を並{なら}べる。',
        'Arrange the 5 steps for safely wearing fall prevention equipment.',
        'উচ্চতা থেকে পতনরোধক সরঞ্জাম নিরাপদে পরিধানের ৫টি ধাপ সাজাও।',
      ),
      steps: [
        t(
          '1. ハーネス本体{ほんたい}とランヤードの傷{きず}や縫製{ほうせい}のほつれを点検{てんけん}する',
          '1. Inspect harness webbing, lanyard, and stitching for damage',
          '১. হারনেস বডি, ল্যানিয়ার্ডের ফিতা ও সেলাইয়ের ক্ষয়ক্ষতি পরীক্ষা করা',
        ),
        t(
          '2. ベルトのねじれがないことを確認{かくにん}して肩{かた}ベルトに腕{うで}を通{とお}す',
          '2. Confirm straps are untwisted and pass arms through shoulder straps',
          '২. বেল্ট যাতে পেঁচিয়ে না থাকে তা দেখে নিয়ে কাঁধের বেল্টে হাত ঢোকানো',
        ),
        t(
          '3. 腿{もも}ベルトのバックルを結合{けつごう}し、手のひらが入る程度{ていど}に調節{ちょうせつ}する',
          '3. Fasten thigh buckles and adjust so one palm can slide beneath',
          '৩. উরুর বেল্টের বাকল আটকে হাতের তালু ঢোকার মতো ফাঁক রেখে মাপ ঠিক করা',
        ),
        t(
          '4. 胸{むね}ベルトのバックルを留{と}めて、背中{せなか}のD環{かん}が肩甲骨{けんこうこつ}の間{あいだ}に来{く}るように締{し}める',
          '4. Fasten chest buckle and position rear D-ring between shoulder blades',
          '৪. বুকের বাকল আটকানো এবং পেছনের ডি-রিং যাতে দুই কাঁধের হাড়ের মাঝে থাকে তা নিশ্চিত করা',
        ),
        t(
          '5. 余{あま}ったベルト端{たん}を留{と}め具{ぐ}に収{おさ}め、全体{ぜんたい}のフィット感{かん}を確認{かくにん}する',
          '5. Tuck loose strap ends into keepers and confirm overall snug fit',
          '৫. বেল্টের বাড়তি অংশ ক্লিপে গুজে দেওয়া এবং পুরো ফিটিং ঠিক আছে কিনা নিশ্চিত করা',
        ),
      ],
    },
    {
      id: 'mask-fit-seq',
      kind: 'seq',
      name: t(
        '取替式{とりかえしき}防{ぼう}じんマスクの装着{そうちゃく}とフィットチェック',
        'Replaceable dust mask fitting and seal check',
        'ফিল্টার পরিবর্তনযোগ্য ডাস্ট মাস্ক পরা ও সিল নিশ্চিতকরণ',
      ),
      goal: t(
        '防{ぼう}じんマスクを正{ただ}しく装着{そうちゃく}して密着{みっちゃく}を確認{かくにん}する4つのステップを並{なら}べる。',
        'Arrange the 4 steps to put on a dust mask and verify its airtight seal.',
        'ডাস্ট মাস্ক পরা ও মুখের সাথে বায়ুরোধী সিল পরীক্ষার ৪টি ধাপ সাজাও।',
      ),
      steps: [
        t(
          '1. フィルターに破{やぶ}れや目詰{めづ}まりがないか点検{てんけん}し、面体{めんたい}を鼻{はな}と口{くち}に当{あ}てる',
          '1. Inspect filter for tears or clogging, then place facepiece over nose and mouth',
          '১. ফিল্টারে ফুটো বা ধুলো জমা আছে কিনা পরীক্ষা করে মাস্কটি নাক ও মুখে বসানো',
        ),
        t(
          '2. 上{うえ}の固定{こてい}紐{ひも}を頭頂部{とうちょうぶ}に掛{か}け、下{した}の紐{ひも}を首{くび}の後{うし}ろで留{と}める',
          '2. Hook upper strap over crown of head and fasten lower strap behind neck',
          '২. উপরের ফিতা মাথার তালুতে রাখা এবং নিচের ফিতা ঘাড়ের পেছনে আটকানো',
        ),
        t(
          '3. 左右{さゆう}の紐{ひも}を均等{きんとう}に引{ひ}き、面体{めんたい}が顔{かお}にすき間{ま}なく密着{みっちゃく}するよう締{し}める',
          '3. Pull both straps evenly so facepiece contacts skin with zero gaps',
          '৩. দুই দিকের ফিতা সমানভাবে টেনে মুখের সাথে কোনো ফাঁক না রেখে আঁটসাঁট করা',
        ),
        t(
          '4. 吸気口{きゅうきこう}を手{て}で塞{ふさ}いで息{いき}を吸{す}い、面体{めんたい}が顔{かお}に吸{す}い付{つ}くか（陰圧{いんあつ}確認{かくにん}）を確{たし}かめる',
          '4. Cover air inlet with hand, inhale gently, and check if mask collapses against face',
          '৪. বাতাসের প্রবেশমুখ হাত দিয়ে চেপে ধরে শ্বাস টেনে দেখা যে মাস্কটি মুখের সাথে সেঁটে যায় কিনা',
        ),
      ],
    },
  ],
  inspect: [
    {
      id: 'helmet-preuse-inspect',
      defective: true,
      name: t(
        '保護帽{ほごぼう}（ヘルメット）の外観{がいかん}・内装{ないそう}点検{てんけん}',
        'Protective helmet exterior and harness inspection',
        'সুরক্ষা হেলমেটের বাইরের ও ভেতরের অংশ পরীক্ষা',
      ),
      spots: [
        {
          x: 50,
          y: 25,
          check: t(
            '帽体{ぼうたい}のひび割{わ}れ：衝撃{しょうげき}による亀裂{きれつ}や変形{へんけい}がないか（良品{りょうひん}：亀裂{きれつ}なし / 不良{ふりょう}：頭頂部{とうちょうぶ}に深{ふか}いクラック・割{わ}れあり）',
            'Shell cracks: Any cracks or deformation from impact (Good: No cracks / Defect: Deep crack on shell crown)',
            'হেলমেটের শেল: আঘাতে কোনো ফাটল বা বিকৃতি আছে কিনা (ভালো: ফাটল নেই / ত্রুটি: উপরিভাগে গভীর ফাটল)',
          ),
        },
        {
          x: 50,
          y: 55,
          check: t(
            '衝撃吸収{しょうげききゅうしゅう}ライナー：発泡{はっぽう}スチロールの割{わ}れや脱落{だつらく}がないか（良品{りょうひん}：密着{みっちゃく}固定{こてい} / 不良{ふりょう}：欠損{けっそん}・へこみ）',
            'Shock-absorbing liner: Any crack or missing foam (Good: Firmly seated / Defect: Missing pieces or dents)',
            'শক অ্যাবজরবিং লাইনার: ফোমের ফাটল বা ক্ষয় আছে কিনা (ভালো: মজবুতভাবে বসানো / ত্রুটি: ভাঙা বা দেবে যাওয়া)',
          ),
        },
        {
          x: 50,
          y: 80,
          check: t(
            'あご紐{ひも}・耳紐{みみひも}：ほつれやバックルの破損{はそん}がないか（良品{りょうひん}：しっかり結合{けつごう} / 不良{ふりょう}：バックル爪{つめ}割{わ}れ）',
            'Chin/ear straps: Any fraying or buckle damage (Good: Clips securely / Defect: Broken buckle prongs)',
            'চিনস্ট্র্যাপ: ফিতার ক্ষয় বা বাকল ভাঙা আছে কিনা (ভালো: শক্তভাবে আটকে / ত্রুটি: বাকলের ক্লিপ ভাঙা)',
          ),
        },
      ],
    },
    {
      id: 'full-harness-inspect',
      defective: false,
      name: t(
        'フルハーネス型{がた}墜落制止用器具{ついらくせいしようきぐ}の始業前{しぎょうまえ}点検{てんけん}',
        'Full harness pre-use daily inspection',
        'ফুল হারনেস কাজ শুরুর পূর্ববর্তী পরিদর্শন',
      ),
      spots: [
        {
          x: 50,
          y: 30,
          check: t(
            '背中{せなか}D環{かん}：変形{へんけい}や亀裂{きれつ}、サビがないか（良品{りょうひん}：無傷{むきず}・円滑{えんかつ} / 不良{ふりょう}：変形{へんけい}・歪{ゆが}み）',
            'Rear D-ring: Any deformation, crack, or rust (Good: Undamaged and smooth / Defect: Bent or deformed)',
            'পেছনের ডি-রিং: বাঁকা, ফাটল বা মরিচা আছে কিনা (ভালো: অক্ষত ও স্বাভাবিক / ত্রুটি: বাঁকানো বা বিকৃত)',
          ),
        },
        {
          x: 75,
          y: 50,
          check: t(
            'ショックアブソーバ：落下{らっか}衝撃{しょうげき}で破断{はだん}・伸{の}びていないか（良品{りょうひん}：未作動{みさどう}・カバー完全{かんぜん} / 不良{ふりょう}：展開{てんかい}済{ず}み・破{やぶ}れ）',
            'Shock absorber: Has it deployed from a past fall (Good: Undeployed and sealed / Defect: Unfolded or torn)',
            'শক অ্যাবজরবার: আগের পতনের আঘাতে ছিঁড়ে বা প্রসারিত হয়ে গেছে কিনা (ভালো: অক্ষত / ত্রুটি: ফেটে খুলে যাওয়া)',
          ),
        },
        {
          x: 30,
          y: 70,
          check: t(
            'ベルト縫製部{ほうせいぶ}：糸{いと}のほつれや切断{せつだん}、溶融{ようゆう}がないか（良品{りょうひん}：縫{ぬ}い目{め}正常{せいじょう} / 不良{ふりょう}：糸{いと}切{き}れ・焦{こ}げ）',
            'Strap stitching: Any frayed, cut, or heat-melted threads (Good: Intact stitching / Defect: Broken seams or burns)',
            'বেল্টের সেলাই: সেলাই খুলে যাওয়া বা পুড়ে যাওয়া আছে কিনা (ভালো: অটুট সেলাই / ত্রুটি: সুতো ছেঁড়া বা পোড়া দাগ)',
          ),
        },
      ],
    },
    {
      id: 'safety-shoes-inspect',
      defective: true,
      name: t(
        '安全靴{あんぜんぐつ}の摩耗{まもう}・破損{はそん}点検{てんけん}',
        'Safety shoes wear and damage inspection',
        'সেফটি জুতোর ক্ষয় ও ক্ষতির পরিদর্শন',
      ),
      spots: [
        {
          x: 75,
          y: 40,
          check: t(
            'つま先{さき}先芯{さきしん}部{ぶ}：皮{かわ}の破{やぶ}れで金属{きんぞく}芯{しん}が露出{ろしゅつ}していないか（良品{りょうひん}：覆{おお}い正常{せいじょう} / 不良{ふりょう}：芯{しん}露出{ろしゅつ}・陥没{かんぼつ}）',
            'Toe cap: Steel toe exposed by torn leather (Good: Fully covered / Defect: Metal exposed or crushed)',
            'পায়ের টো-ক্যাপ: চামড়া ফেটে ভেতরের মেটাল ক্যাপ বেরিয়ে গেছে কিনা (ভালো: সুরক্ষিত / ত্রুটি: লোহা বের হওয়া বা দেবে যাওয়া)',
          ),
        },
        {
          x: 45,
          y: 75,
          check: t(
            '靴底{くつぞこ}（ソール）：溝{みぞ}のすり減{へ}りや釘{くぎ}の刺{さ}さりがないか（良品{りょうひん}：溝{みぞ}あり滑{すべ}り止{ど}め有効{ゆうこう} / 不良{ふりょう}：つるつる・異物{いぶつ}貫通{かんつう}）',
            'Tread sole: Worn flat or pierced by nails (Good: Deep anti-slip tread / Defect: Tread worn smooth or pierced)',
            'জুতোর তলা: খাঁজ ক্ষয়ে পিচ্ছিল বা পেরেক ঢুকে গেছে কিনা (ভালো: খাঁজযুক্ত গ্রিপ / ত্রুটি: সম্পূর্ণ পিচ্ছিল বা ফুটো)',
          ),
        },
      ],
    },
  ],
  quiz: [
    {
      q: t(
        'ボール盤{ばん}（穴{あな}あけ機{き}）で鉄板{てっぱん}を加工{かこう}するとき、手{て}の保護{ほご}について正{ただ}しい行動{こうどう}はどれですか？',
        'When processing steel plates with a drilling machine, what is the correct action regarding hand protection?',
        'ড্রিলিং মেশিনে স্টিল প্লেট ছিদ্র করার সময় হাত সুরক্ষার জন্য সঠিক আচরণ কোনটি?',
      ),
      options: [
        t(
          '手袋{てぶくろ}を着用{ちゃくよう}せず、素手{すで}で作業{さぎょう}する',
          'Do not wear gloves; work with bare hands',
          'গ্লাভস না পরে খালি হাতে কাজ করা',
        ),
        t(
          '滑{すべ}り止{ど}めゴムが付{つ}いた手袋{てぶくろ}を着用{ちゃくよう}する',
          'Wear gloves with rubber grip coating',
          'রাবারের গ্রিপযুক্ত গ্লাভস পরে কাজ করা',
        ),
        t(
          '厚手{あつで}の軍手{ぐんて}を2枚{まい}重{かさ}ねて着用{ちゃくよう}する',
          'Wear two layers of thick cotton work gloves',
          'দুই পরতের মোটা সুতি কাপড়ের গ্লাভস পরা',
        ),
        t(
          '指先{ゆびさき}をカットした革{かわ}手袋{てぶくろ}を着用{ちゃくよう}する',
          'Wear fingerless leather gloves',
          'আঙুলের ডগা কাটা চামড়ার গ্লাভস পরা',
        ),
      ],
      answer: 0,
      why: t(
        'ボール盤{ばん}や旋盤{せんばん}などの回転{かいてん}機械{きかい}では、手袋{てぶくろ}が巻{ま}き込{こ}まれて指{ゆび}や手{て}を巻き込む大事故{だいじこ}になるため、手袋{てぶくろ}の着用{ちゃくよう}は厳禁{げんきん}です。',
        'Rotating machinery such as drill presses can catch gloves and pull hands into catastrophic entanglements; wearing gloves is strictly prohibited.',
        'ড্রিল বা লেদ মেশিনের ঘূর্ণায়মান অংশ গ্লাভস পেঁচিয়ে হাত টেনে মারাত্মক দুর্ঘটনা ঘটায়, তাই গ্লাভস পরা সম্পূর্ণ নিষিদ্ধ।',
      ),
    },
    {
      q: t(
        'ヘルメット（保護帽{ほごぼう}）のあご紐{ひも}の締{し}め方{かた}として、最{もっと}も適切{てきせつ}なものはどれですか？',
        'What is the most appropriate way to fasten a helmet chin strap?',
        'হেলমেটের ফিতা বাঁধার সবচেয়ে উপযুক্ত নিয়ম কোনটি?',
      ),
      options: [
        t(
          '窮屈{きゅうくつ}なので指{ゆび}が3本{さんぼん}入{はい}るくらい緩{ゆる}く締{し}める',
          'Keep it loose enough for 3 fingers to avoid feeling constricted',
          'অস্বস্তি এড়াতে ৩টি আঙুল ঢোকার মতো আলগা রাখা',
        ),
        t(
          '頭{あたま}を振{ふ}ってもズレず、あごと紐{ひも}の間{あいだ}に指{ゆび}が1本{いっぽん}入{はい}る程度{ていど}に締{し}める',
          'Fasten securely so it does not shift when shaking head, with space for one finger',
          'মাথা নাড়ালেও নড়বে না এবং থুতনির মাঝে একটি আঙুল ঢোকার মতো আঁটসাঁট করা',
        ),
        t(
          '後頭部{こうとうぶ}に回{まわ}してあごには掛{か}けない',
          'Loop it behind the back of the head instead of under the chin',
          'থুতনির নিচে না দিয়ে মাথার পেছনের দিকে আটকে রাখা',
        ),
        t(
          '作業中{さぎょうちゅう}に汗{あせ}を拭{ふ}くためバックルを外{はず}しておく',
          'Leave the buckle unfastened to easily wipe sweat during work',
          'কাজের সময় ঘাম মোছার সুবিধার্থে বাকল খুলে রাখা',
        ),
      ],
      answer: 1,
      why: t(
        'あご紐{ひも}は転倒{てんとう}や墜落{ついらく}時{じ}にヘルメットが脱落{だつらく}するのを防{ふせ}ぐため、指{ゆび}1本{いっぽん}分{ぶん}の隙間{すきま}を目安{めやす}に確実{かくじつ}に締{し}めます。',
        'The chin strap prevents the helmet from falling off during a stumble or fall; fasten it securely leaving about one finger width.',
        'পড়ে যাওয়ার সময় হেলমেট মাথা থেকে খুলে যাওয়া ঠেকাতে একটি আঙুলের সমান ফাঁক রেখে ফিতা শক্তভাবে বাঁধতে হয়।',
      ),
    },
    {
      q: t(
        'サンダー（ディスクグラインダー）で鋼板{こうはん}のバリ取{と}りを作業{さぎょう}しているとき、保護{ほご}メガネが少{すこ}し曇{くも}ってきました。どうすべきですか？',
        'When deburring steel plate with a disc grinder, your safety glasses fog up. What should you do?',
        'ডিস্ক গ্রাইন্ডার দিয়ে স্টিল প্লেট ঘষার সময় সুরক্ষা চশমা কিছুটা ঘোলা হয়ে গেছে। আপনার কী করা উচিত?',
      ),
      options: [
        t(
          '片手{かたて}でグラインダーを持{も}ちながら、もう一方{いっぽう}の手{て}でメガネを外{はず}す',
          'Hold grinder with one hand and remove glasses with the other',
          'এক হাতে গ্রাইন্ডার ধরে অন্য হাত দিয়ে চশমা খুলে ফেলা',
        ),
        t(
          '視界{しかい}が悪{わる}いので、メガネをおでこに上{あ}げてそのまま作業{さぎょう}を続{つづ}ける',
          'Push glasses up to forehead and keep grinding because visibility is poor',
          'দেখতে অসুবিধা হওয়ায় চশমা কপালে তুলে রেখে গ্রাইন্ডিং চালিয়ে যাওয়া',
        ),
        t(
          '一度{いちど}グラインダーを完全{かんぜん}に停止{ていし}させ、火花{ひばな}が出{で}ない安全{あんぜん}な場所{ばしょ}で拭{ふ}く',
          'Stop grinder completely and wipe glasses in a safe area away from sparks',
          'গ্রাইন্ডার পুরোপুরি বন্ধ করে স্ফুলিঙ্গ নেই এমন নিরাপদ জায়গায় গিয়ে চশমা মোছা',
        ),
        t(
          'メガネを足元{あしもと}に置{お}き、目{め}を細{ほそ}めて作業{さぎょう}を急{いそ}いで終{お}わらせる',
          'Place glasses on floor, squint eyes, and quickly finish the job',
          'চশমা মেঝেতে রেখে চোখ কুঁচকে কাজ দ্রুত শেষ করা',
        ),
      ],
      answer: 2,
      why: t(
        '作業中{さぎょうちゅう}に保護{ほご}メガネを外{はず}すと飛散{ひさん}した切粉{きりこ}や火花{ひばな}が目{め}に入{はい}るため、必{かなら}ず機械{きかい}を止{と}めてから安全{あんぜん}な場所{ばしょ}で手入{てい}れします。',
        'Removing safety glasses while working exposes eyes to flying grit and sparks; always stop the machine first before cleaning in safety.',
        'কাজের সময় চশমা খুললে ধাতুর কণা ও স্ফুলিঙ্গ চোখে ঢুকে অন্ধ হতে পারে; তাই মেশিন পুরোপুরি থামিয়ে নিরাপদ জায়গায় চশমা পরিষ্কার করতে হবে।',
      ),
    },
    {
      q: t(
        '過去{かこ}に足場{あしば}から落{お}としたことのあるヘルメットについて、正{ただ}しい取扱{とりあつか}いはどれですか？',
        'What is the correct handling of a helmet that previously fell from scaffolding?',
        'আগে কখনো মাচা থেকে নিচে পড়ে যাওয়া হেলমেটের ক্ষেত্রে সঠিক করণীয় কোনটি?',
      ),
      options: [
        t(
          '表面{ひょうめん}の汚れを拭{ふ}き取{と}ればそのまま再使用{さいしよう}してよい',
          'It can be reused normally once surface dirt is wiped off',
          'উপরের ময়লা মুছে ফেলে পুনরায় ব্যবহার করা যায়',
        ),
        t(
          '軽{かる}く叩{たた}いて音{おと}に異常{いじょう}がなければ使用{しよう}を続{つづ}ける',
          'Tap it lightly and continue using if the sound is normal',
          'হালকা টোকা দিয়ে শব্দ স্বাভাবিক লাগলে ব্যবহার চালিয়ে যাওয়া',
        ),
        t(
          '油性{ゆせい}マジックで名前{なまえ}を書{か}き直{なお}して使い続ける',
          'Rewrite name on it with oil-based marker and continue using',
          'মার্কার দিয়ে নাম নতুন করে লিখে ব্যবহার করা',
        ),
        t(
          '目{め}に見{み}える亀裂{きれつ}がなくても衝撃{しょうげき}吸収{きゅうしゅう}性能{せいのう}が低下{ていか}しているため廃棄{はいき}・交換{こうかん}する',
          'Discard and replace it because impact performance degrades even without visible cracks',
          'বাইরে দৃশ্যমান ফাটল না থাকলেও আঘাত শোষণের ক্ষমতা নষ্ট হয়ে যাওয়ায় তা ফেলে দিয়ে নতুন নেওয়া',
        ),
      ],
      answer: 3,
      why: t(
        '一度{いちど}でも強{つよ}い衝撃{しょうげき}を受{う}けた保護帽{ほごぼう}は、外見{がいけん}に異常{いじょう}がなくても内部{ないぶ}や構造{こうぞう}が劣化{れっか}しているため廃棄{はいき}します。',
        'A helmet that has received a heavy impact must be discarded, as internal structural integrity is compromised even without visible defects.',
        'একবারও প্রচণ্ড আঘাত পাওয়া হেলমেটের ভেতরের শক্তি নষ্ট হয়ে যায়, তাই বাইরে থেকে ভালো দেখালেও তা বর্জন করতে হয়।',
      ),
    },
    {
      q: t(
        '高所{こうしょ}作業{さぎょう}でフルハーネス型{がた}墜落制止用器具{ついらくせいしようきぐ}のランヤード（フック）を掛{か}ける位置{いち}として正{ただ}しいものはどれですか？',
        'What is the correct attachment position for a full harness lanyard hook during work at heights?',
        'উঁচুতে কাজের সময় ফুল হারনেসের ল্যানিয়ার্ডের হুক লাগানোর সঠিক অবস্থান কোনটি?',
      ),
      options: [
        t(
          '腰{こし}の高さより上{うえ}にある強固{きょうこ}な鉄骨{てっこつ}などの構造物{こうぞうぶつ}',
          'A sturdy structural member such as a steel beam above waist level',
          'কোমরের উচ্চতার চেয়ে উপরে থাকা শক্ত স্টিল বিম বা মজবুত কাঠামো',
        ),
        t(
          '足元{あしもと}に転{ころ}がっている移動式{いどうしき}の台車{だいしゃ}',
          'A mobile cart lying around at your feet',
          'পায়ের কাছে রাখা নড়াচড়া করা চাকার ট্রলি',
        ),
        t(
          '細{ほそ}い塩ビパイプや配線{はいせん}ケーブル',
          'A thin PVC pipe or electrical conduit',
          'চিকন পিভিসি পাইপ বা বৈদ্যুতিক তার',
        ),
        t(
          '作業員{さぎょういん}同士{どうし}のハーネスのD環{かん}',
          'The D-ring of a fellow worker’s harness',
          'অন্য সহকর্মীর হারনেসের ডি-রিং',
        ),
      ],
      answer: 0,
      why: t(
        'フックは落下{らっか}距離{きょり}と衝撃{しょうげき}を最小限{さいしょうげん}にするため、腰{こし}より高{たか}い位置{いち}の十分{じゅうぶん}な強度{きょうど}がある構造物{こうぞうぶつ}に掛{か}けます。',
        'To minimize fall distance and shock load, attach the hook to an adequately strong structure positioned higher than your waist.',
        'পতনের দূরত্ব ও ঝাঁকুনি কমাতে হুকটি অবশ্যই কোমরের চেয়ে উঁচুতে থাকা যথেষ্ট শক্ত কাঠামোর সাথে লাগাতে হয়।',
      ),
    },
    {
      q: t(
        '塗装{とそう}作業{さぎょう}でシンナー（有機溶剤{ゆうきようざい}）を使用{しよう}します。着用{ちゃくよう}すべき適切{てきせつ}な呼吸用{こきゅうよう}保護具{ほごぐ}はどれですか？',
        'When using thinner (organic solvent) during painting work, what is the appropriate respiratory PPE to wear?',
        'পেইন্টিংয়ের কাজে থিনার (জৈব দ্রাবক) ব্যবহারের সময় কোন শ্বাসযন্ত্র সুরক্ষা সরঞ্জামটি পরা উচিত?',
      ),
      options: [
        t(
          '一般{いっぱん}の不織布{ふしょくふ}マスク（風邪用{かぜよう}）',
          'General non-woven hygiene mask (for colds)',
          'সাধারণ সার্জিক্যাল বা কাপড়ের মাস্ক',
        ),
        t(
          '有機溶剤用{ゆうきようざいよう}の吸収缶{きゅうしゅうかん}を装着{そうちゃく}した防毒{ぼうどく}マスク',
          'Gas mask equipped with an organic vapor chemical canister',
          'জৈব দ্রাবকের জন্য নির্ধারিত ক্যানিস্টারযুক্ত গ্যাস মাস্ক',
        ),
        t(
          'ろ過材{かざい}だけが付{つ}いた防{ぼう}じんマスク',
          'Dust mask equipped only with a particulate filter',
          'শুধুমাত্র ডাস্ট ফিল্টারযুক্ত ডাস্ট মাস্ক',
        ),
        t(
          'タオルを口{くち}と鼻{はな}に巻{ま}きつけたもの',
          'A towel wrapped around the mouth and nose',
          'নাক ও মুখে পেঁচানো তোয়ালে',
        ),
      ],
      answer: 1,
      why: t(
        '有機溶剤{ゆうきようざい}のガス・蒸気{じょうき}は防{ぼう}じんマスクでは除去{じょきょ}できません。専用{せんよう}の吸収缶{きゅうしゅうかん}を取{と}り付{つ}けた防毒{ぼうどく}マスクが必須{ひっす}です。',
        'Organic solvent vapors cannot be filtered by dust masks; a dedicated gas mask with appropriate chemical canisters is mandatory.',
        'গ্যাসের বাষ্প ডাস্ট মাস্ক দিয়ে আটকানো যায় না; জৈব দ্রাবকের ক্ষতিকর বাষ্প থেকে বাঁচতে ক্যানিস্টারযুক্ত গ্যাস মাস্ক অপরিহার্য।',
      ),
    },
    {
      q: t(
        'ヘルメットに表示{ひょうじ}されている「労・検」ラベルに「飛来{ひらい}・落下物用{らっかぶつよう}」とだけ書{か}かれていました。このヘルメットを高所{こうしょ}作業{さぎょう}に使{つか}えますか？',
        'The certification label on a helmet shows only "For flying/falling objects". Can this helmet be used for work at heights?',
        'একটি হেলমেটের সার্টিফিকেশন লেবেলে শুধু "উড়ে আসা ও পড়ন্ত বস্তুর জন্য" লেখা আছে। এটি কি উঁচুতে কাজের জন্য ব্যবহার করা যাবে?',
      ),
      options: [
        t(
          '軽{かる}くて動{うご}きやすいので高所{こうしょ}作業{さぎょう}に最適{さいてき}である',
          'It is lightweight and easy to move in, so it is ideal for high work',
          'হালকা ও নড়াচড়ায় সুবিধা হওয়ায় উঁচুতে কাজের জন্য আদর্শ',
        ),
        t(
          'あご紐{ひも}を2重{じゅう}に締{し}めれば高所{こうしょ}でも使用{しよう}できる',
          'It can be used at heights if you double-tighten the chin strap',
          'চিনস্ট্র্যাপ দুইবার টাইট করে বাঁধলে উঁচুতেও ব্যবহার করা যাবে',
        ),
        t(
          '「墜落時保護用{ついらくじほごよう}」（衝撃吸収{しょうげききゅうしゅう}ライナー入{い}り）の表示{ひょうじ}がないため高所{こうしょ}作業{さぎょう}には使{つか}えない',
          'It cannot be used for high work because it lacks "Protection during falls" (no shock-absorbing liner)',
          'এতে "পতনের সময় সুরক্ষাকারী" লাইনার না থাকায় এটি উঁচুতে কাজের ক্ষেত্রে ব্যবহার করা যাবে না',
        ),
        t(
          'Foremanの許可{きょか}があればどの作業{さぎょう}でも自由{じゆう}に使{つか}える',
          'It can be used freely for any work as long as the foreman approves',
          'ফোরম্যান অনুমতি দিলে যেকোনো কাজে ব্যবহার করা যায়',
        ),
      ],
      answer: 2,
      why: t(
        '高所{こうしょ}作業{さぎょう}では万一{まんいち}の転落{てんらく}時{じ}に頭{あたま}を守{まも}るため、「墜落時保護用{ついらくじほごよう}」の合格{ごうかく}標章{ひょうしょう}（発泡{はっぽう}ライナー付き）のヘルメットが義務{ぎむ}付{づ}けられています。',
        'For work at heights, helmets certified for "Protection during falls" with shock-absorbing foam liners are legally required to guard the head.',
        'উঁচুতে কাজের ক্ষেত্রে পড়ে গেলে মাথা বাঁচাতে অবশ্যই "পতনজনিত সুরক্ষামূলক" ফোম লাইনারযুক্ত অনুমোদিত হেলমেট পরা বাধ্যতামূলক।',
      ),
    },
    {
      q: t(
        '安全靴{あんぜんぐつ}を履{は}くときの正{ただ}しい注意点{ちゅういてん}として適切{てきせつ}なものはどれですか？',
        'What is the correct precaution when wearing safety shoes?',
        'সেফটি জুতো পরার ক্ষেত্রে নিচের কোন সতর্কতাটি সঠিক?',
      ),
      options: [
        t(
          '脱{ぬ}ぎ履{は}きしやすいように、かかとを踏{ふ}んでスリッパのように履{は}く',
          'Crush down the heel like a slipper for easy slipping on and off',
          'সহজে খোলা ও পরার সুবিধার জন্য গোড়ালি দাবিয়ে চটির মতো পরা',
        ),
        t(
          '作業中{さぎょうちゅう}に足{あし}が痛{いた}くならないよう、2サイズ大{おお}きめの靴{くつ}を履{は}く',
          'Wear shoes 2 sizes larger so feet do not ache during long work',
          'কাজে পা যাতে ব্যথা না করে সেজন্য ২ সাইজ বড় জুতো পরা',
        ),
        t(
          'つま先{さき}の先芯{さきしん}（鉄芯{てつしん}）が重{おも}いので取{と}り外{はず}して履{は}く',
          'Remove the steel toe cap because it feels too heavy',
          'সামনের স্টিল টো-ক্যাপটি ভারী মনে হওয়ায় তা খুলে ফেলা',
        ),
        t(
          '足{あし}のサイズに合{あ}った靴{くつ}を選{えら}び、紐{ひも}や面ファスナーを締{し}めてかかとを踏{ふ}まない',
          'Choose shoes fitting your size, secure laces/velcro, and never crush down the heels',
          'নিজের পায়ের মাপের জুতো বেছে নেওয়া, ফিতা শক্ত করে বাঁধা এবং গোড়ালি কখনোই না দাবানো',
        ),
      ],
      answer: 3,
      why: t(
        'かかとを踏{ふ}んで履{は}くと脱{ぬ}げやすく転倒{てんとう}の原因{げんいん}になり、先芯{さきしん}による保護{ほご}や滑{すべ}り止{ど}め機能{きのう}も正常{せいじょう}に働{はたら}きません。',
        'Crushing the heels causes tripping hazards, and prevents safety toes and anti-slip soles from functioning properly.',
        'গোড়ালি দাবিয়ে জুতো পরলে জুতো খুলে পড়ে দুর্ঘটনা ঘটে এবং শক্ত টো-ক্যাপের সুরক্ষা ও গ্রিপ ঠিকমতো কাজ করে না।',
      ),
    },
    {
      q: t(
        '造船所{ぞうせんじょ}の船体内{せんたいない}など、強{つよ}い騒音{そうおん}（85dB以上{いじょう}など）が発生{はっせい}する作業場{さぎょうば}での行動{こうどう}として正{ただ}しいものはどれですか？',
        'What is the correct action in high-noise workplaces (such as inside a shipyard ship hull exceeding 85dB)?',
        'জাহাজ নির্মাণের হলের ভেতরের মতো তীব্র শব্দযুক্ত (৮৫ ডেসিবেলের বেশি) কর্মস্থলে সঠিক আচরণ কোনটি?',
      ),
      options: [
        t(
          '耳栓{みみせん}やイヤーマフなどの遮音{しゃおん}保護具{ほごぐ}を正{ただ}しく装着{そうちゃく}する',
          'Wear hearing protection such as earplugs or earmuffs correctly',
          'কানের ইয়ারপ্লাগ বা ইয়ারমাফের মতো শ্রবণ সুরক্ষা সরঞ্জাম সঠিকভাবে পরা',
        ),
        t(
          '声{こえ}が聞{き}こえにくくなるので耳栓{みみせん}は一切{いっさい}使{つか}わない',
          'Do not use earplugs at all because it makes voices hard to hear',
          'কথা শুনতে সমস্যা হবে ভেবে মোটেও ইয়ারপ্লাগ ব্যবহার না করা',
        ),
        t(
          '耳{みみ}にティッシュペーパーを詰{つ}めて代用{だいよう}する',
          'Stuff tissue paper into the ears as a substitute',
          'বিকল্প হিসেবে কানের ভেতরে টিস্যু পেপার গুঁজে রাখা',
        ),
        t(
          '耳{みみ}が痛{いた}くなってきた時{とき}だけ一時的{いちじてき}に耳{みみ}をふさぐ',
          'Cover ears with hands only temporarily when ears start hurting',
          'কানে ব্যথা শুরু হলে শুধু কিছুক্ষণের জন্য হাত দিয়ে কান চেপে ধরা',
        ),
      ],
      answer: 0,
      why: t(
        '騒音{そうおん}による難聴{なんちょう}は自覚{じかく}のないまま徐々{じょじょ}に進行{しんこう}し回復{かいふく}しないため、騒音{そうおん}職場{しょくば}では耳栓{みみせん}・イヤーマフの着用{ちゃくよう}が必須{ひっす}です。',
        'Noise-induced hearing loss progresses unnoticed and is irreversible; wearing earplugs or earmuffs in noisy areas is essential.',
        'শব্দজনিত বধিরতা নীরবে বাড়ে এবং তা আর ভালো হয় না; তাই তীব্র শব্দের কর্মস্থলে ইয়ারপ্লাগ বা ইয়ারমাফ পরা আবশ্যক।',
      ),
    },
    {
      q: t(
        '保護具{ほごぐ}の着用{ちゃくよう}ルールについて、「自分{じぶん}は作業{さぎょう}に慣{な}れているから保護具{ほごぐ}を外{はず}してもよいか」という質問{しつもん}に対{たい}する正{ただ}しい答{こた}えはどれですか？',
        'Regarding PPE rules, what is the correct answer to the question "Can I remove PPE because I am experienced at this job"?',
        'পিপিই পরার ক্ষেত্রে "আমি অভিজ্ঞ তাই কি সুরক্ষা সরঞ্জাম খুলে কাজ করতে পারি?" প্রশ্নের সঠিক উত্তর কোনটি?',
      ),
      options: [
        t(
          'ベテランであれば自分{じぶん}の判断{はんだん}で外{はず}してよい',
          'Veteran workers may remove it based on their own personal judgment',
          'অভিজ্ঞ কর্মী হলে নিজের বিবেচনায় খুলে কাজ করতে পারে',
        ),
        t(
          '経験{けいけん}年数{ねんすう}に関係{かんけい}なく、現場{げんば}の全員{ぜんいん}が保護具{ほごぐ}の着用{ちゃくよう}ルールを守{まも}らなければならない',
          'Regardless of experience, everyone on site must obey PPE regulations without exception',
          'অভিজ্ঞতা যাই হোক না কেন, সাইটের প্রত্যেকের সুরক্ষা সরঞ্জামের নিয়ম মানা বাধ্যতামূলক',
        ),
        t(
          '作業{さぎょう}スピードが速{はや}くなるなら外{はず}した方{ほう}がよい',
          'It is better to remove it if doing so speeds up the work',
          'কাজের গতি বাড়লে সুরক্ষা সরঞ্জাম খুলে ফেলা উচিত',
        ),
        t(
          '午前中{ごぜんちゅう}だけ着用{ちゃくよう}し、午後{ごご}は外{はず}して作業{さぎょう}する',
          'Wear it only in the morning and work without it in the afternoon',
          'শুধুমাত্র সকালে পরে কাজ করা এবং বিকেলে খুলে কাজ করা',
        ),
      ],
      answer: 1,
      why: t(
        '保護具{ほごぐ}は熟練者{じゅくれんしゃ}であっても予期{よき}せぬ不測{ふそく}の事故{じこ}から命{いのち}を守{まも}る最後の砦{とりで}であり、例外{れいがい}なく着用{ちゃくよう}が義務{ぎむ}付{づ}けられています。',
        'PPE is the last line of defense protecting life even for skilled workers; compliance is strictly mandatory for everyone without exception.',
        'সুরক্ষা সরঞ্জাম অভিজ্ঞদেরও অনাকাঙ্ক্ষিত বিপদ থেকে বাঁচানোর শেষ ঢাল; তাই কোনো ব্যতিক্রম ছাড়া সবার জন্যই এটি পরা বাধ্যতামূলক।',
      ),
    },
    {
      q: t(
        'FRP（繊維強化プラスチック）製{せい}のヘルメットの使用{しよう}期間{きかん}の目安{めやす}として一般的に推奨{すいしょう}されている年数{ねんすう}はどれですか？',
        'What is the generally recommended service life for an FRP (fiberglass reinforced plastic) helmet?',
        'একটি এফআরপি (ফাইবারগ্লাস) হেলমেটের ব্যবহারের জন্য সাধারণত কত বছর সুপারিশ করা হয়?',
      ),
      options: [
        t(
          '一度{いちど}購入{こうにゅう}したら定年{ていねん}まで何十年{なんじゅうねん}でも使{つか}える',
          'Once purchased, it can be used for decades until retirement',
          'একবার কিনলে অবসরের বয়স পর্যন্ত বহু বছর ব্যবহার করা যায়',
        ),
        t(
          '1か月{げつ}ごとに必{かなら}ず新品{しんぴん}に交換{こうかん}する',
          'Must be replaced with a brand new one every single month',
          'প্রতি এক মাস পর পর অবশ্যই নতুনের সাথে বদলাতে হবে',
        ),
        t(
          '使用開始{しようかいし}から約{やく}5年{ねん}（熱可塑性樹脂製{ねつかそせいじゅしせい}は約{やく}3年{ねん}）を目安{めやす}に定期交換{ていきこうかん}する', // verify: FRP製保護帽は使用開始から約5年（熱可塑性樹脂製は約3年）を目安に交換
          'Replace periodically after roughly 5 years (roughly 3 years for thermoplastics)',
          'ব্যবহার শুরুর পর থেকে প্রায় ৫ বছর (থার্মোপ্লাস্টিকের ক্ষেত্রে প্রায় ৩ বছর) পর পর বদলে ফেলা',
        ),
        t(
          '外側{そとがわ}に好{す}きなシールを貼{は}れば使用{しよう}期限{きげん}はなくなる',
          'Putting favorite stickers on the outside removes any expiration date',
          'বাইরে নিজের পছন্দের স্টিকার লাগিয়ে নিলে মেয়াদের কোনো হিসাব থাকে না',
        ),
      ],
      answer: 2,
      why: t(
        'ヘルメットは紫外線{しがいせん}や経年{けいねん}劣化{れっか}で樹脂{じゅし}強度が低下{ていか}するため、外見{がいけん}に傷{きず}がなくてもFRP製{せい}は約{やく}5年{ねん}を目安{めやす}に交換{こうかん}します。',
        'Because resin strength degrades over time from UV light and aging, FRP helmets should be replaced after about 5 years even without visible flaws.',
        'সূর্যের অতিবেগুনি রশ্মি ও সময়ের ব্যবধানে উপাদানের শক্তি কমে যায়, তাই বাইরে থেকে ভালো দেখালেও ৫ বছর ব্যবহারের পর এফআরপি হেলমেট বদলে ফেলতে হয়।',
      ),
    },
    {
      q: t(
        '防{ぼう}じんマスクを装着{そうちゃく}した際{さい}に行{おこな}う「フィットテスト（密着性{みっちゃくせい}の確認{かくにん}）」について、正{ただ}しい方法{ほうほう}はどれですか？',
        'What is the correct procedure for the seal check (fit check) performed after putting on a dust mask?',
        'ডাস্ট মাস্ক পরার পর মুখে যথাযথভাবে বসার সিল পরীক্ষার (ফিট টেস্ট) সঠিক নিয়ম কোনটি?',
      ),
      options: [
        t(
          'マスクを横{よこ}に引{ひ}っ張{ぱ}ってゴムが伸{の}びるか確認{かくにん}する',
          'Pull the mask sideways to check if the elastic strap stretches',
          'মাস্ক দুই পাশে টেনে ফিতা লম্বা হয় কিনা দেখা',
        ),
        t(
          'マスクの内側{うちがわ}に水{みず}を入{い}れて漏{も}れがないか確{たし}かめる',
          'Pour water inside the mask to check for leaks',
          'মাস্কের ভেতরে পানি ঢেলে কোনো ফুটো আছে কিনা দেখা',
        ),
        t(
          '職長{しょくちょう}に遠{とお}くから見{み}てもらって合図{あいず}をもらう',
          'Have the foreman look from a distance and give a signal',
          'দূর থেকে ফোরম্যানকে দেখিয়ে ইশারা নেওয়া',
        ),
        t(
          '吸気口{きゅうきこう}を手{て}で塞{ふさ}いで息{いき}を吸{す}い込{こ}み、面体{めんたい}が顔{かお}に密着{みっちゃく}して空気{くうき}が漏{も}れないか確認{かくにん}する',
          'Cover air inlet with hand, inhale gently, and check that mask draws tight against face with no leaks',
          'বাতাস প্রবেশের মুখ হাত দিয়ে চেপে শ্বাস টেনে দেখা যে চারপাশ দিয়ে বাতাস না ঢুকে মাস্কটি মুখে চেপে বসে কিনা',
        ),
      ],
      answer: 3,
      why: t(
        '防{ぼう}じんマスクと顔{かお}の間{あいだ}にすき間{ま}があると粉{ふん}じんを直接{ちょくせつ}吸{す}い込{こ}んでしまうため、吸気口{きゅうきこう}を押{お}さえて息{いき}を吸{す}い陰圧{いんあつ}で密着{みっちゃく}を確認{かくにん}します。',
        'If gaps exist between the mask and face, harmful dust bypasses the filter; checking negative pressure confirms an airtight seal.',
        'মাস্ক ও মুখের মাঝে ফাঁক থাকলে বিপজ্জনক ধুলো সরাসরি ফুসফুসে ঢুকে পড়ে, তাই বাতাসের মুখ চেপে শ্বাস টেনে নেগেটিভ প্রেশার দিয়ে সিল নিশ্চিত করতে হয়।',
      ),
    },
  ],
  glossary: [
    {
      ja: '保護具',
      reading: 'ほごぐ',
      en: 'Personal protective equipment (PPE)',
      bn: 'ব্যক্তিগত সুরক্ষা সরঞ্জাম (পিপিই)',
      note: t(
        '作業中の危険や有害物から作業者の体を守る器具の総称。',
        'General term for equipment protecting workers from hazards and toxic substances.',
        'কাজের সময় যেকোনো বিপদ বা ক্ষতিকর বস্তু থেকে শরীর বাঁচানোর সামগ্রিক সরঞ্জাম।',
      ),
    },
    {
      ja: '保護帽',
      reading: 'ほごぼう',
      en: 'Safety helmet / hard hat',
      bn: 'সুরক্ষা হেলমেট',
      note: t(
        '落下物や墜落の衝撃から頭部を保護するヘルメット。',
        'Helmet protecting the head from falling objects and fall impacts.',
        'পড়ন্ত বস্তু বা নিচে পড়ে যাওয়ার আঘাত থেকে মাথা রক্ষাকারী হেলমেট।',
      ),
    },
    {
      ja: 'あご紐',
      reading: 'あごひも',
      en: 'Chin strap',
      bn: 'চিনস্ট্র্যাপ / হেলমেটের ফিতা',
      note: t(
        '衝撃や強風でヘルメットが脱落しないよう固定するストラップ。',
        'Strap securing the helmet so it does not dislodge from impact or wind.',
        'ঝাঁকুনি বা বাতাসে হেলমেট মাথা থেকে পড়ে যাওয়া রোধকারী ফিতা।',
      ),
    },
    {
      ja: '安全靴',
      reading: 'あんぜんぐつ',
      en: 'Safety shoes',
      bn: 'সেফটি জুতো / নিরাপত্তা বুট',
      note: t(
        '重物の落下から足の指を守る先芯が入った作業靴。',
        'Work footwear with protective toe caps guarding against falling heavy objects.',
        'ভারী জিনিস পড়া থেকে পায়ের পাতা সুরক্ষায় সামনে শক্ত টো-ক্যাপযুক্ত জুতো।',
      ),
    },
    {
      ja: '先芯',
      reading: 'さきしん',
      en: 'Protective toe cap (steel/resin)',
      bn: 'টো-ক্যাপ / পায়ের পাতার সুরক্ষা টুপি',
      note: t(
        '安全靴のつま先に内蔵されている金属製または樹脂製の防護部材。',
        'Metal or resin protective cup built into the toe box of safety shoes.',
        'সেফটি জুতোর সামনের অংশে বসানো শক্ত ধাতু বা রেজিনের অংশ।',
      ),
    },
    {
      ja: '保護メガネ',
      reading: 'ほごめがね',
      en: 'Safety glasses / protective goggles',
      bn: 'সুরক্ষা চশমা / গগলস',
      note: t(
        '飛散する火花や切粉、薬品から目を守る専用メガネ。',
        'Dedicated eyewear protecting eyes from flying sparks, metal chips, or chemicals.',
        'উড়ে আসা আগুনের স্ফুলিঙ্গ, লোহার গুঁড়ো বা রাসায়নিক থেকে চোখ রক্ষাকারী চশমা।',
      ),
    },
    {
      ja: '防じんマスク',
      reading: 'ぼうじんますく',
      en: 'Dust mask / particulate respirator',
      bn: 'ডাস্ট মাস্ক / ধূলিনিরোধক মাস্ক',
      note: t(
        '研磨や溶接で発生する粉じんやヒュームをろ過する呼吸用保護具。',
        'Respiratory PPE that filters dust and fumes generated during grinding and welding.',
        'গ্রাইন্ডিং বা ওয়েল্ডিংয়ের সময় তৈরি ধূলিকণা বা ক্ষতিকর ধোঁয়া ফিল্টারকারী মাস্ক।',
      ),
    },
    {
      ja: '防毒マスク',
      reading: 'ぼうどくますく',
      en: 'Gas mask / chemical respirator',
      bn: 'গ্যাস মাস্ক / বিষাক্ত বাষ্পরোধক মাস্ক',
      note: t(
        '有機溶剤や有害ガスの蒸気を吸収缶で無毒化する呼吸用保護具。',
        'Respiratory PPE using chemical canisters to neutralize toxic vapors and solvents.',
        'ক্যানিস্টারের সাহায্যে বিষাক্ত গ্যাস ও দ্রাবকের বাষ্প নিষ্ক্রিয়কারী শ্বাসযন্ত্র।',
      ),
    },
    {
      ja: '墜落制止用器具',
      reading: 'ついらくせいしようきぐ',
      en: 'Fall arrest equipment (full harness)',
      bn: 'পতনরোধক সরঞ্জাম (ফুল হারনেস)',
      note: t(
        '高所からの墜落を防ぎ衝撃を分散する器具。旧名称は安全帯。',
        'Equipment preventing falls and dispersing fall impact; formerly known as safety belt.',
        'উঁচু থেকে নিচে পড়া ঠেকাতে এবং আঘাতের চাপ ছড়িয়ে দিতে ব্যবহৃত সরঞ্জাম।',
      ),
    },
    {
      ja: 'フルハーネス',
      reading: 'ふるはーねす',
      en: 'Full harness',
      bn: 'ফুল হারনেস',
      note: t(
        '肩・胸・腿のベルトで体全体を支え、墜落時の荷重を分散する保護具。',
        'Harness supporting the whole body across shoulders, chest, and thighs.',
        'কাঁধ, বুক ও উরুর বেল্ট দিয়ে পুরো শরীর ধরে রেখে পতনের ধাক্কা ভাগ করে নেওয়া সরঞ্জাম।',
      ),
    },
    {
      ja: '耳栓',
      reading: 'みみせん',
      en: 'Earplugs',
      bn: 'ইয়ারপ্লাগ / কানের প্লাগ',
      note: t(
        '造船所などの強烈な騒音から聴覚を守る遮音保護具。',
        'Hearing protection inserted into ears to guard against intense noise in shipyards.',
        'জাহাজ নির্মাণ বা কারখানার তীব্র শব্দ থেকে কান সুরক্ষাকারী সামগ্রী।',
      ),
    },
    {
      ja: '巻き込まれ',
      reading: 'まきこまれ',
      en: 'Entanglement hazard',
      bn: 'ঘূর্ণায়মান মেশিনে জড়িয়ে যাওয়ার বিপদ',
      note: t(
        'ボール盤や旋盤などの回転部分に手袋や衣服が巻き込まれる事故。',
        'Accident where gloves or clothing get caught and dragged into rotating machine parts.',
        'ড্রিল বা লেদ মেশিনের মতো ঘূর্ণায়মান যন্ত্রে গ্লাভস বা পোশাক জড়িয়ে ঘটা দুর্ঘটনা।',
      ),
    },
  ],
}
