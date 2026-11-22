import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string, bn: string): Tri => ({ ja, en, bn })

export const onboardingSafety: CourseData = {
  id: 'onboarding-safety',
  category: 'core',
  level: 1,
  minutes: 15,
  title: t(
    '雇{やと}い入{い}れ時{じ}安全衛生教育{あんぜんえいせいきょういく}',
    'Hiring-Time Safety & Health Training',
    'নিয়োগকালীন নিরাপত্তা ও স্বাস্থ্য প্রশিক্ষণ',
  ),
  sub: t(
    '初日{しょにち}に学{まな}ぶ、現場{げんば}で命{いのち}と健康{けんこう}を守{まも}る基本{きほん}。',
    'Learn site essentials on day one to protect life and health.',
    'কাজের প্রথম দিনে জেনে নাও সাইটে নিজের জীবন ও স্বাস্থ্য রক্ষার মূল নিয়ম।',
  ),
  refs: [
    '労働安全衛生法 第59条（雇入れ時等の安全衛生教育）',
    '労働安全衛生規則 第35条（雇入れ時等の教育事項）',
    '厚生労働省 腰痛予防対策指針',
  ],
  panels: [
    {
      who: 'foreman',
      text: t(
        '今日{きょう}からよろしく。現場{げんば}に入{はい}る前{まえ}に、命{いのち}を守{まも}る雇入{やとい}れ時{じ}安全衛生教育{あんぜんえいせいきょういく}を行{おこな}います。',
        'Welcome to our site. Before entering the floor, we conduct hiring-time safety training to protect your life.',
        'আমাদের সাইটে স্বাগতম। কাজের জায়গায় ঢোকার আগে নিজের জীবন বাঁচাতে নিয়োগকালীন নিরাপত্তা প্রশিক্ষণ নেব।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'よろしくお願いします。保護具{ほごぐ}を付{つ}けましたが、あごひもはここまで強{つよ}く締{し}めるのですか？',
        'Thank you. I put on my PPE, but does the chin strap really need to be tightened this firmly?',
        'ধন্যবাদ। আমি পিপিই পরেছি, কিন্তু চিনস্ট্র্যাপ কি সত্যিই এত শক্ত করে বাঁধতে হবে?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '下{した}を向{む}いた時{とき}や転倒時{てんとうじ}に脱{ぬ}げないよう、指{ゆび}1本{ぽん}入{はい}る隙間{すきま}でしっかり締{し}めます。',
        'Yes. Fasten it so only one finger fits underneath, so it never falls off when bending over or falling.',
        'হ্যাঁ। আঙুল একটার বেশি না ঢোকার মতো শক্ত করে বাঁধো, যাতে নিচু হলে বা পড়ে গেলে খুলে না যায়।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '分{わ}かりました！あちらに近道{ちかみち}があるので、黄色{きいろ}い線{せん}を越{こ}えて移動{いどう}しますね。',
        'Understood! There is a shortcut over there, so I will cross past the yellow line to get there.',
        'বুঝেছি! ওইদিকে একটা শর্টকাট আছে, আমি হলুদ দাগ পার হয়ে ওদিক দিয়ে যাই।',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'ストップ！クレーンやフォークリフトの通路{つうろ}は立入禁止{たちいりきんし}です。決{き}められた安全通路{あんぜんつうろ}を歩{ある}いてください。',
        'Stop! Forklift and crane paths are strictly off-limits. Always walk along designated safety walkways.',
        'থামো! ক্রেন ও ফর্কফ্লিটের পথ দিয়ে হাঁটা সম্পূর্ণ নিষেধ। সবসময় নির্ধারিত নিরাপদ পথ দিয়ে চলো।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '危{あぶ}ないところでした。もし作業中{さぎょうちゅう}に危険{きけん}を感{かん}じたり、手順{てじゅん}が分{わ}からないときはどうすればいいですか？',
        'That was close. If I feel danger or am unsure of the procedure during work, what should I do?',
        'একটু হলে বিপদে পড়তাম। কাজের সময় যদি বিপদ মনে হয় বা নিয়ম বুঝতে না পারি, তবে কী করব?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '迷{まよ}ったら作業{さぎょう}を止{と}めて、すぐ私{わたし}（職長{しょくちょう}）に聞{き}いてください。危険{きけん}を避{さ}けるために作業{さぎょう}を止{と}める権利{けんり}があなたにあります。',
        'If in doubt, stop work and ask me (your Foreman) immediately. You have the right to stop work to avoid danger.',
        'সন্দেহ হলেই কাজ থামাও এবং সাথে সাথে আমাকে (ফোরম্যানকে) জানাও। বিপদ এড়াতে কাজ বন্ধ রাখার অধিকার তোমার আছে।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '安心{あんしん}しました。もし指{ゆび}を少{すこ}し擦{す}りむくような小{ちい}さなケガをした場合{ばあい}も、報告{ほうこく}が必要{ひつよう}ですか？',
        'That is reassuring. If I get a minor scratch on my finger, do I still need to report it?',
        'আশ্বস্ত হলাম। যদি আঙুলে সামান্য একটু আঁচড় লাগে, তবুও কি রিপোর্ট করতে হবে?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'どんな小{ちい}さなケガでも必{かなら}ず報告{ほうこく}してください。労災保険{ろうさいほけん}は国籍{こくせき}やビザに関係{かんけい}なく全{すべ}ての労働者{ろうどうしゃ}に適用{てきよう}されます。',
        'Always report every injury, no matter how small. Workers compensation covers all workers regardless of nationality or visa.',
        'যত সামান্য আঘাতই হোক, অবশ্যই জানাবে। জাতীয়তা বা ভিসা যাই হোক না কেন, সব কর্মীই কর্মক্ষেত্র বীমার আওতাভুক্ত।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '整理整頓{せいりせいとん}と始業前点検{しぎょうまえてんけん}を徹底{てってい}して、安全第一{あんぜんだいいち}で作業{さぎょう}します！',
        'I will keep my area tidy, check my equipment before starting, and put safety first every day!',
        'আমি কাজের জায়গা গোছগাছ রাখব, কাজ শুরুর আগে পরীক্ষা করব এবং নিরাপত্তাকে সবার আগে রাখব!',
      ),
    },
  ],
  handbook: [
    t(
      '保護帽{ほごぼう}（ヘルメット）はあごひもを指{ゆび}1本{ぽん}の隙間{すきま}で正{ただ}しく締{し}める。',
      'Wear your safety helmet and fasten the chin strap firmly with one finger of clearance.',
      'সেফটি হেলমেটের চিনস্ট্র্যাপ এক আঙুল ফাঁক রেখে শক্তভাবে বাঁধবে।',
    ),
    t(
      '作業前{さぎょうまえ}に保護具{ほごぐ}や安全靴{あんぜんぐつ}を点検{てんけん}し、破損{はそん}があればすぐ交換{こうかん}する。',
      'Inspect your PPE and safety shoes before work; replace them immediately if damaged.',
      'কাজের আগে পিপিই ও সেফটি জুতা পরীক্ষা করবে, ক্ষতিগ্রস্ত হলে সাথে সাথে বদলাবে।',
    ),
    t(
      'クレーン吊{つ}り荷{に}の下{した}や重機{じゅうき}の作業半径内{さぎょうはんけいない}には絶対{ぜったい}に入{はい}らない（立入禁止{たちいりきんし}）。',
      'Never enter under a crane load or inside heavy equipment work zones (strictly off-limits).',
      'ক্রেনের ঝুলন্ত মালের নিচে বা ভারী মেশিনের কাজের সীমানায় কখনো ঢুকবে না (ঢোকা সম্পূর্ণ নিষেধ)।',
    ),
    t(
      '危険{きけん}を感{かん}じたときは作業{さぎょう}を中断{ちゅうだん}し、安全{あんぜん}を確認{かくにん}する権利{けんり}がある。',
      'You have the right to suspend work and confirm safety if you perceive imminent danger.',
      'বিপদ অনুভব করলে কাজ বন্ধ রেখে নিরাপত্তা যাচাই করার অধিকার তোমার আছে।',
    ),
    t(
      '作業手順{さぎょうてじゅん}が分{わ}からないときや異常{いじょう}を発見{はっけん}したときは、自分{じぶん}で判断{はんだん}せず職長{しょくちょう}に相談{そうだん}する。',
      'When unsure of work steps or when finding an anomaly, never guess—consult your foreman.',
      'কাজের ধাপ বুঝতে না পারলে বা কোনো ত্রুটি দেখলে নিজে সিদ্ধান্ত না নিয়ে ফোরম্যানের সাথে পরামর্শ করো।',
    ),
    t(
      'どんな小{ちい}さな怪我{けが}やヒヤリ・ハットでも、隠{かく}さず当日中{とうじつちゅう}に職長{しょくちょう}へ報告{ほうこく}する。',
      'Report even minor scrapes or near-misses to your foreman on the same day without concealing them.',
      'যতই সামান্য চোট বা বিপদ থেকে বেঁচে যাওয়ার ঘটনা হোক, না লুকিয়ে সেদিনই ফোরম্যানকে জানাও।',
    ),
    t(
      '労災保険{ろうさいほけん}は国籍{こくせき}や在留資格{ざいりゅうしかく}を問{と}わず、全{すべ}ての労働者{ろうどうしゃ}に平等{びょうどう}に適用{てきよう}される。',
      'Workers compensation applies equally to all workers regardless of nationality or visa status.',
      'জাতীয়তা বা ভিসার ধরন যাই হোক না কেন, সব কর্মীর জন্য কর্মক্ষেত্র ক্ষতিপূরণ বীমা সমানভাবে প্রযোজ্য।',
    ),
    t(
      '機械{きかい}の清掃{せいそう}や点検{てんけん}を行{おこな}う時{とき}は、必{かなら}ず主電源{しゅでんげん}を切{き}って停止{ていし}を確認{かくにん}する。',
      'Always turn off the main power and confirm complete stoppage before cleaning or inspecting machinery.',
      'মেশিন পরিষ্কার বা পরীক্ষা করার সময় অবশ্যই মেইন পাওয়ার বন্ধ করে সম্পূর্ণ থামা নিশ্চিত করো।',
    ),
    t(
      '通路{つうろ}や非常口{ひじょうぐち}に物{もの}を置{お}かず、4S（整理{せいり}・整頓{せいとん}・清掃{せいそう}・清潔{せいけつ}）を徹底{てってい}する。',
      'Keep walkways and emergency exits clear of clutter; maintain strict 4S organization.',
      'হাঁটার পথ ও জরুরি বহির্গমনে মালামাল রাখবে না; কঠোরভাবে ৪এস (পরিচ্ছন্নতা ও শৃঙ্খলা) বজায় রাখো।',
    ),
    t(
      '重{おも}い荷物{にもつ}を持{も}ち上{あ}げるときは、膝{ひざ}を深{ふか}く曲{ま}げて腰{こし}を落{お}とし、荷物{にもつ}を体{からだ}に密着{みっちゃく}させる。',
      'When lifting heavy objects, bend your knees deep, lower your hips, and hold the load close to your body.',
      'ভারী বস্তু তোলার সময় হাঁটু ভাঁজ করে কোমর নামাও এবং বস্তুটি শরীরের সাথে চেপে ধরো।',
    ),
  ],
  reel: [
    t(
      'あごひも、締{し}まり、ヨシ！',
      'Chin strap fastened, yoshi!',
      'চিনস্ট্র্যাপ বাঁধা, ঠিক আছে!',
    ),
    t(
      '足元{あしもと}、整理整頓{せいりせいとん}、ヨシ！',
      'Footing clear and tidy, yoshi!',
      'পায়ের কাছে পরিষ্কার, ঠিক আছে!',
    ),
    t(
      '安全通路{あんぜんつうろ}、障害物{しょうがいぶつ}なし、ヨシ！',
      'Safety walkway, no obstacles, yoshi!',
      'নিরাপদ হাঁটার পথ, কোনো বাধা নেই, ঠিক আছে!',
    ),
    t(
      'クレーン下{した}、立入禁止{たちいりきんし}、ヨシ！',
      'Under the crane, off-limits, yoshi!',
      'ক্রেনের নিচে ঢোকা নিষেধ, ঠিক আছে!',
    ),
    t(
      '非常停止{ひじょうていし}ボタン、位置確認{いちかくにん}、ヨシ！',
      'Emergency stop button location confirmed, yoshi!',
      'ইমার্জেন্সি স্টপ বোতামের অবস্থান নিশ্চিত, ঠিক আছে!',
    ),
    t(
      '保護具{ほごぐ}、完全装着{かんぜんそうちゃく}、ヨシ！',
      'PPE fully equipped, yoshi!',
      'পিপিই পুরোপুরি পরা, ঠিক আছে!',
    ),
    t(
      '本日{ほんじつ}の体調{たいちょう}、良好{りょうこう}、ヨシ！',
      'Physical condition today, good, yoshi!',
      'আজ শরীর ভালো, ঠিক আছে!',
    ),
  ],
  drills: [
    {
      id: 'onboarding-ky-spot',
      name: t(
        '危険予知{きけんよち}（KY）スポット',
        'Hazard prediction (KY) spot',
        'বিপদ চিহ্নিতকরণ (কে-ওয়াই) স্পট',
      ),
      goal: t(
        '作業場{さぎょうば}の危険{きけん}な場所{ばしょ}を4つ見{み}つける。',
        'Find 4 hazards in the workplace.',
        'কাজের জায়গায় ৪টি বিপজ্জনক বিষয় খুঁজে বের করো।',
      ),
      kind: 'spot',
      secs: 30,
      need: 4,
      items: [
        {
          label: t(
            '床{ゆか}に油{あぶら}がこぼれて滑{すべ}りやすい',
            'Oil spilled on floor, slip hazard',
            'মেঝেতে তেল পড়ে পিচ্ছিল হয়ে আছে',
          ),
          hazard: true,
        },
        {
          label: t(
            '非常停止{ひじょうていし}ボタンの前{まえ}に荷物{にもつ}が置{お}かれている',
            'Boxes blocking emergency stop button',
            'ইমার্জেন্সি স্টপ বোতামের সামনে বাক্স রাখা',
          ),
          hazard: true,
        },
        {
          label: t(
            '作業員{さぎょういん}があごひもを外{はず}している',
            'Worker has unfastened chin strap',
            'কর্মীর হেলমেটের চিনস্ট্র্যাপ খোলা',
          ),
          hazard: true,
        },
        {
          label: t(
            'クレーン吊{つ}り荷{に}の真下{ました}を歩{ある}いている',
            'Walking directly under crane suspended load',
            'ক্রেনে ঝুলন্ত মালের ঠিক নিচ দিয়ে হাঁটছে',
          ),
          hazard: true,
        },
        {
          label: t(
            '電気{でんき}コードの被覆{ひふく}が破{やぶ}れて芯線{しんせん}が見{み}えている',
            'Power cord damaged with exposed wires',
            'বৈদ্যুতিক তারের কভার ফেটে তার বেরিয়ে আছে',
          ),
          hazard: true,
        },
        {
          label: t(
            '正{ただ}しく黄色{きいろ}の区画線{くかくせん}の内側{うちがわ}に置{お}かれた台車{だいしゃ}',
            'Cart neatly parked inside yellow line',
            'হলুদ দাগের ভেতর রাখা ঠেলাগাড়ি',
          ),
          hazard: false,
        },
        {
          label: t(
            '安全靴{あんぜんぐつ}を履{は}いて作業{さぎょう}している',
            'Worker wearing proper safety shoes',
            'সেফটি জুতা পরে কাজ করছে',
          ),
          hazard: false,
        },
        {
          label: t(
            '定位置{ていいち}に置{お}かれた点検済{てんけんず}みの消火器{しょうかき}',
            'Inspected fire extinguisher in fixed spot',
            'নির্দিষ্ট স্থানে রাখা পরীক্ষিত অগ্নিনির্বাপক',
          ),
          hazard: false,
        },
        {
          label: t(
            '整理整頓{せいりせいとん}された工具棚{こうぐだな}',
            'Tidy tool rack with tools returned',
            'পরিচ্ছন্ন ও গুছিয়ে রাখা টুলের তাক',
          ),
          hazard: false,
        },
      ],
    },
    {
      id: 'morning-startup-seq',
      name: t(
        '朝{あさ}の作業開始手順{さぎょうかいしてじゅん}',
        'Morning start-of-work sequence',
        'সকালের কাজ শুরুর ধাপ',
      ),
      goal: t(
        '作業{さぎょう}を始{はじ}める前{まえ}の正{ただ}しい順番{じゅんばん}を覚{おぼ}える。',
        'Learn the correct sequence before starting work.',
        'কাজ শুরু করার আগের সঠিক ক্রম মনে রাখো।',
      ),
      kind: 'seq',
      steps: [
        t(
          '保護具{ほごぐ}（ヘルメット・安全靴{あんぜんぐつ}）の点検{てんけん}と装着{そうちゃく}',
          'Inspect and put on PPE (helmet, safety shoes)',
          'পিপিই (হেলমেট ও সেফটি জুতা) পরীক্ষা ও পরিধান',
        ),
        t(
          '朝礼{ちょうれい}と危険予知{きけんよち}（KY）活動{かつどう}への参加{さんか}',
          'Participate in morning meeting and KY hazard prediction',
          'সকালের মিটিং ও কে-ওয়াই বিপদ পূর্বানুমান কার্যক্রমে অংশগ্রহণ',
        ),
        t(
          '作業場所{さぎょうばしょ}の整理整頓{せいりせいとん}と通路{つうろ}の確認{かくにん}',
          'Tidy up work area and check walkways',
          'কাজের জায়গা পরিষ্কার-পরিচ্ছন্ন করা ও হাঁটার পথ দেখা',
        ),
        t(
          '使用{しよう}する機械{きかい}・工具{こうぐ}の始業前点検{しぎょうまえてんけん}',
          'Pre-operation inspection of machines and tools',
          'ব্যবহার্য যন্ত্রপাতি ও টুলের কাজ শুরুর আগের পরীক্ষা',
        ),
        t(
          '職長{しょくちょう}からの指示内容{しじないよう}を指差呼称{しさこしょう}で確認{かくにん}',
          'Confirm foreman instructions with point-and-call',
          'ফোরম্যানের নির্দেশ আঙুল দিয়ে দেখিয়ে ও মুখে বলে নিশ্চিত করা',
        ),
      ],
    },
    {
      id: 'injury-incident-seq',
      name: t(
        'ケガ・異常発生時{いじょうはっせいじ}の対応手順{たいおうてじゅん}',
        'Emergency & injury response sequence',
        'আঘাত বা অস্বাভাবিক পরিস্থিতিতে করণীয় ধাপ',
      ),
      goal: t(
        'ケガや異常{いじょう}が起{お}きたときの行動手順{こうどうてじゅん}を身{み}につける。',
        'Master the action steps when an injury or issue occurs.',
        'আঘাত বা সমস্যা হলে করণীয় পদক্ষেপগুলো আয়ত্ত করো।',
      ),
      kind: 'seq',
      steps: [
        t(
          '作業{さぎょう}を直{ただ}ちに中断{ちゅうだん}し機械{きかい}を止{と}める',
          'Immediately suspend work and stop the machine',
          'কাজ অবিলম্বে বন্ধ করো এবং মেশিন থামাও',
        ),
        t(
          '怪我{けが}の状態{じょうたい}を確認{かくにん}し必要{ひつよう}なら応急手当{おうきゅうてあて}をする',
          'Check injury condition and apply first aid if needed',
          'আঘাতের অবস্থা দেখে প্রয়োজন হলে প্রাথমিক চিকিৎসা দাও',
        ),
        t(
          '職長{しょくちょう}へ直{ただ}ちに報告{ほうこく}する（報連相{ほうれんそう}）',
          'Immediately report to the foreman (Hou-Ren-So)',
          'ফোরম্যানকে সাথে সাথে রিপোর্ট করো (হো-রেন-সো)',
        ),
        t(
          '病院受診{びょういんじゅしん}と労災記録票{ろうさいきろくひょう}の作成{さくせい}',
          'Hospital visit and filling out workers compensation report',
          'হাসপাতালে চিকিৎসা ও দুর্ঘটনা রিপোর্ট তৈরি',
        ),
      ],
    },
    {
      id: 'emergency-stop-reflex',
      name: t(
        '非常停止{ひじょうていし}の反射{はんしゃ}',
        'Emergency stop reflex',
        'ইমার্জেন্সি স্টপ রিফ্লেক্স',
      ),
      goal: t(
        '異常{いじょう}や挟{はさ}まれ発生{はっせい}！0.4秒以内{びょういない}に非常停止{ひじょうていし}を押{お}す。',
        'Abnormality or pinch caught! Hit emergency stop within 0.4 s.',
        'বিপদ বা কিছু আটকে যাওয়া! ০.৪ সেকেন্ডের মধ্যে ইমার্জেন্সি স্টপ চাপো।',
      ),
      kind: 'reflex',
    },
  ],
  inspect: [
    {
      id: 'helmet',
      name: t(
        '保護帽{ほごぼう}（ヘルメット）',
        'Safety helmet',
        'সেফটি হেলমেট',
      ),
      defective: false,
      spots: [
        {
          x: 50,
          y: 25,
          check: t(
            '帽体{ぼうたい}にひび割{わ}れ・深{ふか}い傷{きず}がなく変形{へんけい}していないこと',
            'Shell has no cracks, deep gouges, or warping',
            'হেলমেটের বডিতে কোনো ফাটল, গভীর দাগ বা বিকৃতি নেই',
          ),
        },
        {
          x: 35,
          y: 75,
          check: t(
            'あごひも・耳{みみ}ひもに緩{ゆる}みやほつれがなく正{ただ}しく締{し}まること',
            'Chin and ear straps have no slack or fraying and fasten firmly',
            'চিনস্ট্র্যাপ ও কানের ফিতা ঢিলে বা ছেঁড়া নয় এবং শক্তভাবে আটকায়',
          ),
        },
        {
          x: 65,
          y: 50,
          check: t(
            '内側{うちがわ}の衝撃吸収{しょうげききゅうしゅう}ライナーが外{はず}れていないこと',
            'Shock-absorbing liner inside is properly fitted in place',
            'ভেতরের শক-শোষণকারী লাইনার ঠিকমতো লাগানো আছে',
          ),
        },
      ],
    },
    {
      id: 'safety-shoes',
      name: t(
        '安全靴{あんぜんぐつ}',
        'Safety shoes',
        'সেফটি জুতা',
      ),
      defective: true,
      spots: [
        {
          x: 30,
          y: 40,
          check: t(
            'つま先{さき}の先芯{さきしん}が破{やぶ}れて露出{ろしゅつ}していないこと（破れは不良）',
            'Protective toecap not exposed through torn leather (tears are defective)',
            'পায়ের আঙুলের শক্ত ক্যাপ চামড়া ফেটে বের হয়ে নেই (বের হলে ত্রুটিযুক্ত)',
          ),
        },
        {
          x: 70,
          y: 70,
          check: t(
            '靴底{くつぞこ}の溝{みぞ}がすり減{へ}っておらず滑{すべ}り止{ど}めが効{き}くこと',
            'Tread grooves on outsole not worn smooth, slip resistance intact',
            'সোলের খাঁজ ক্ষয়ে যায়নি এবং পিছলে যাওয়া রোধ করে',
          ),
        },
      ],
    },
    {
      id: 'power-cord',
      name: t(
        '作業用延長{さぎょうようえんちょう}コード',
        'Work extension cord',
        'কাজের এক্সটেনশন কর্ড',
      ),
      defective: true,
      spots: [
        {
          x: 25,
          y: 35,
          check: t(
            '差{さ}し込{こ}みプラグの刃{ば}に曲{ま}がりや焦{こ}げ痕{あと}がないこと',
            'Plug prongs not bent, blackened, or burned',
            'প্লাগের পিন বাঁকা বা পোড়া নয়',
          ),
        },
        {
          x: 60,
          y: 65,
          check: t(
            '被覆{ひふく}に亀裂{きれつ}がなく内部{ないぶ}の芯線{しんせん}が露出{ろしゅつ}していないこと（露出は感電の危険で不良）',
            'Insulation jacket has no cuts and wire is not exposed (exposed wire is shock hazard and defective)',
            'তারের কভারে ফাটল নেই এবং ভেতরের তার বেরিয়ে নেই (বেরিয়ে থাকলে শক খাবার ঝুঁকি ও ত্রুটিযুক্ত)',
          ),
        },
      ],
    },
  ],
  quiz: [
    {
      q: t(
        '新{あたら}しく支給{しきゅう}されたヘルメットをかぶる際{さい}、あごひもの締{し}め方{かた}として正{ただ}しいものはどれですか？',
        'When putting on your newly issued safety helmet, which is the correct way to adjust the chin strap?',
        'নতুন দেওয়া সেফটি হেলমেট পরার সময় চিনস্ট্র্যাপ বাঁধার সঠিক নিয়ম কোনটি?',
      ),
      options: [
        t(
          '指{ゆび}が1本{ぽん}入{はい}る程度{ていど}の隙間{すきま}で、たるみなくしっかり締{し}める',
          'Fasten firmly with no slack, leaving about one finger of space underneath',
          'এক আঙুল ঢোকার মতো ফাঁক রেখে কোনো ঢিল না দিয়ে শক্ত করে বাঁধা',
        ),
        t(
          '苦{くる}しくないよう、あごの下{した}に指{ゆび}が4本{ほん}入{はい}るくらい緩{ゆる}く締{し}める',
          'Fasten loosely so about 4 fingers fit underneath to avoid discomfort',
          'অস্বস্তি এড়াতে চিবুকের নিচে ৪ আঙুল ঢোকার মতো ঢিলে করে বাঁধা',
        ),
        t(
          '暑{あつ}い日{ひ}は熱中症{ねっちゅうしょう}を防{ふせ}ぐため、あごひもを外{はず}して帽体{ぼうたい}の上{うえ}にかける',
          'On hot days, unhook the chin strap and loop it over the shell to prevent heatstroke',
          'গরমের দিনে হিটস্ট্রোক এড়াতে চিনস্ট্র্যাপ খুলে হেলমেটের ওপর তুলে রাখা',
        ),
        t(
          '耳{みみ}ひもだけを固定{こてい}し、あごひもは作業{さぎょう}が始{はじ}まるまで締{し}めない',
          'Only fasten ear straps and leave the chin strap undone until work starts',
          'শুধু কানের ফিতা আটকে রেখে কাজ শুরু না হওয়া পর্যন্ত চিনস্ট্র্যাপ না বাঁধা',
        ),
      ],
      answer: 0,
      why: t(
        'あごひもが緩{ゆる}いと、転倒{てんとう}や落下時{らっかじ}にヘルメットが脱{ぬ}げて重大{じゅうだい}な頭部災害{とうぶさいがい}につながります。指{ゆび}1本{ぽん}入{はい}る程度{ていど}に締{し}めるのが基本{きほん}です。',
        'If the chin strap is loose, the helmet can fall off during a trip or fall, causing severe head injury. Fastening it with one finger of clearance is the basic standard.',
        'চিনস্ট্র্যাপ ঢিলে থাকলে হোঁচট খেলে বা পড়ে গেলে হেলমেট খুলে মারাত্মক মাথায় আঘাত লাগতে পারে। এক আঙুল ফাঁক রেখে শক্ত করে বাঁধাই নিয়ম।',
      ),
    },
    {
      q: t(
        '工場内{こうじょうない}を歩{ある}いているとき、天井{てんじょう}クレーンが警報{けいほう}を鳴{な}らしながら鉄板{てっぱん}を運{はこ}んでいます。どう行動{こうどう}すべきですか？',
        'While walking in the factory, an overhead crane is transporting steel plates sounding its alarm. What should you do?',
        'কারখানার ভেতর হাঁটার সময় একটি ওভারহেড ক্রেন অ্যালার্ম বাজিয়ে স্টিলের প্লেট নিয়ে যাচ্ছে। আপনার কী করা উচিত?',
      ),
      options: [
        t(
          '吊{つ}り荷{に}が近{ちか}づく前{まえ}に、急{いそ}ぎ足{あし}で下{した}を走{はし}り抜{ぬ}ける',
          'Quickly run underneath before the load gets closer',
          'মালটি কাছে আসার আগেই নিচ দিয়ে দ্রুত দৌড়ে পার হওয়া',
        ),
        t(
          '吊{つ}り荷{に}の動{うご}きが見{み}える安全{あんぜん}な場所{ばしょ}で立{た}ち止{ど}まり、通過{つうか}するのを待{ま}つ',
          'Stop in a safe location where you can see the load and wait for it to pass',
          'মালটি দেখা যায় এমন নিরাপদ স্থানে দাঁড়িয়ে সেটি পার হয়ে যাওয়ার জন্য অপেক্ষা করা',
        ),
        t(
          '合図者{あいずしゃ}に大声{おおごえ}で注意{ちゅうい}しながら、そのまま歩{ある}き続{つづ}ける',
          'Keep walking while shouting warnings to the signalman',
          'ইশারাকারীকে জোরে সতর্ক করতে করতে একইভাবে হেঁটে যেতে থাকা',
        ),
        t(
          'フォークリフト専用通路{せんようつうろ}に移動{いどう}して避{よ}ける',
          'Move into the dedicated forklift lane to avoid it',
          'ফর্কফ্লিটের জন্য নির্ধারিত লেনে ঢুকে পথ পরিবর্তন করা',
        ),
      ],
      answer: 1,
      why: t(
        'クレーンの吊{つ}り荷{に}の下{した}への立入{たちいり}は法令{ほうれい}で固{かた}く禁止{きんし}されています。安全{あんぜん}な位置{いち}で停止{ていし}し、通過{つうか}を待{ま}ちます。',
        'Entering the area under a suspended crane load is strictly prohibited by law. Stop in a safe place and wait for it to pass.',
        'ক্রেনের ঝুলন্ত মালের নিচে যাওয়া আইনত কঠোরভাবে নিষিদ্ধ। নিরাপদ স্থানে দাঁড়িয়ে সেটি পার হওয়া পর্যন্ত অপেক্ষা করতে হবে।',
      ),
    },
    {
      q: t(
        '通路{つうろ}の床{ゆか}に油{あぶら}が漏{も}れて水{みず}たまりのようになっているのを発見{はっけん}しました。最初{さいしょ}に取{と}るべき行動{こうどう}はどれですか？',
        'You found oil leaking on the walkway forming a large puddle. What is the first action you should take?',
        'হাঁটার পথে তেল চুইয়ে পড়ে বড় জলাশয়ের মতো হয়ে আছে দেখতে পেলেন। প্রথমে আপনার কী পদক্ষেপ নেওয়া উচিত?',
      ),
      options: [
        t(
          '自分{じぶん}の担当部署{たんとうぶしょ}ではないので、そのまま通{とお}り過{す}ぎる',
          'Ignore and walk past since it is not your assigned department',
          'নিজের কাজের বিভাগ নয় বলে কোনো গুরুত্ব না দিয়ে পাশ কাটিয়ে চলে যাওয়া',
        ),
        t(
          '乾{かわ}くまで時間{じかん}がかかるため、上{うえ}から段{だん}ボールを敷{し}いて隠{かく}す',
          'Cover it with cardboard to hide it since it will take time to dry',
          'শুকানোর সময় লাগবে বলে ওপর দিয়ে পিচবোর্ড বিছিয়ে লুকিয়ে রাখা',
        ),
        t(
          '周囲{しゅうい}の作業員{さぎょういん}に声{こえ}をかけて注意{ちゅうい}を促{うなが}し、職長{しょくちょう}に報告{ほうこく}して油{あぶら}を拭{ふ}き取{と}る',
          'Alert nearby workers, report to the foreman, and clean up the oil',
          'আশেপাশের কর্মীদের ডেকে সতর্ক করা, ফোরম্যানকে জানানো এবং তেল মুছে ফেলা',
        ),
        t(
          '終業時{しゅうぎょうじ}の清掃時間{せいそうじかん}まで待{ま}ってから片付{かたづ}ける',
          'Wait until the end-of-shift cleaning time to clean it up',
          'কাজ শেষের ঝাড়পোঁছের সময় পর্যন্ত অপেক্ষা করে পরিষ্কার করা',
        ),
      ],
      answer: 2,
      why: t(
        '油{あぶら}による転倒{てんとう}は骨折{こっせつ}などの重大事故{じゅうだいじこ}につながります。発見{はっけん}したら直{ただ}ちに周囲{しゅうい}に知{し}らせ、報告{ほうこく}して処理{しょり}します。',
        'Oil slips can lead to severe accidents such as fractures. Once spotted, alert others immediately, report it, and clean it up.',
        'তেলে পিছলে গেলে হাড়ভাঙার মতো মারাত্মক দুর্ঘটনা ঘটতে পারে। দেখতে পেলে সাথে সাথে আশেপাশের সবাইকে সতর্ক করুন, রিপোর্ট করুন এবং মুছে ফেলুন।',
      ),
    },
    {
      q: t(
        'コンベアや回転機械{かいてんきかい}に異物{いぶつ}が挟{はさ}まって止{と}まりました。どう対応{たいおう}すべきですか？',
        'A conveyor belt or rotating machine jammed and stopped due to a foreign object. How should you handle it?',
        'কনভেয়ার বেল্ট বা ঘূর্ণায়মান মেশিনে কিছু আটকে মেশিন থেমে গেল। আপনার কীভাবে ব্যবস্থা নেওয়া উচিত?',
      ),
      options: [
        t(
          '動{うご}いていないので、そのまま手{て}を突{つ}っ込{こ}んで異物{いぶつ}を引{ひ}っ張{ぱ}り出{だ}す',
          'Since it is not moving, reach your hand directly inside to pull the object out',
          'যেহেতু চলছে না, তাই হাত ঢুকিয়ে সরাসরি বস্তুটি টেনে বের করা',
        ),
        t(
          '運転{うんてん}スイッチを何度{なんど}も押{お}して無理{むり}やり回{まわ}してみる',
          'Repeatedly press the run switch to force it to turn',
          'স্টার্ট সুইচ বারবার চেপে জোর করে ঘোরানোর চেষ্টা করা',
        ),
        t(
          '軍手{ぐんて}を二重{にじゅう}にはめて、機械{きかい}の隙間{すきま}から棒{ぼう}で突{つ}つく',
          'Wear double cloth gloves and poke it with a stick through gaps',
          'হাতে জোড়া কাপড়ের গ্লাভস পরে মেশিনের ফাঁক দিয়ে লাঠি দিয়ে খোঁচানো',
        ),
        t(
          '必{かなら}ず主電源{しゅでんげん}を切{き}り、「修理中{しゅうりちゅう}」の表示{ひょうじ}をして職長{しょくちょう}に報告{ほうこく}する',
          'Always turn off main power, display Under Repair tag, and report to foreman',
          'অবশ্যই মেইন পাওয়ার বন্ধ করে, মেরামতের বোর্ড ঝুলিয়ে ফোরম্যানকে জানানো',
        ),
      ],
      answer: 3,
      why: t(
        '電源{でんげん}を切{き}らずに手{て}を出{だ}すと、詰{つ}まりが取{と}れた瞬間{しゅんかん}に突然{とつぜん}動{うご}き出{だ}して巻{ま}き込{こ}まれる危険{きけん}があります。電源遮断{でんげんしゃだん}（ロックアウト）が鉄則{てっそく}です。',
        'Touching it without killing power risks sudden startup once the jam clears, causing severe entanglement. Cutting power (lockout) is an absolute rule.',
        'পাওয়ার বন্ধ না করে হাত দিলে আটকে থাকা জিনিস ছুটে গিয়ে হঠাৎ মেশিন চালু হয়ে হাত জড়িয়ে যেতে পারে। পাওয়ার বন্ধ করাই প্রধান নিয়ম।',
      ),
    },
    {
      q: t(
        '作業中{さぎょうちゅう}に鉄板{てっぱん}の端{はし}で指先{ゆびさき}を少{すこ}し切{き}り、出血{しゅっけつ}しました。絆創膏{ばんそうこう}を貼{は}れば作業{さぎょう}を続{つづ}けられます。どうすべきですか？',
        'During work, you slightly cut your fingertip on the edge of a steel plate and it bled. A bandage would allow you to continue. What should you do?',
        'কাজের সময় স্টিলের পাতের কোণায় আঙুল কেটে সামান্য রক্ত বের হলো। একটি ব্যান্ডেজ লাগালেই কাজ চালিয়ে যাওয়া যায়। আপনার কী করা উচিত?',
      ),
      options: [
        t(
          '作業{さぎょう}を止{と}めて職長{しょくちょう}に報告{ほうこく}し、救急箱{きゅうきゅうばこ}で適切{てきせつ}に手当{てあて}を受{う}ける',
          'Stop work, report to your foreman, and get proper treatment from first aid kit',
          'কাজ থামিয়ে ফোরম্যানকে জানানো এবং ফার্স্ট এইড বক্স থেকে সঠিক চিকিৎসা নেওয়া',
        ),
        t(
          '誰{だれ}にも言{い}わずに自分{じぶん}のポケットの絆創膏{ばんそうこう}を貼{は}って作業{さぎょう}を続{つづ}ける',
          'Put on a personal bandage from your pocket without telling anyone and keep working',
          'কাউকে কিছু না বলে নিজের পকেট থেকে ব্যান্ডেজ লাগিয়ে কাজ চালিয়ে যাওয়া',
        ),
        t(
          '作業{さぎょう}が遅{おく}れると迷惑{めいわく}がかかるので、仕事{しごと}が全部{ぜんぶ}終{お}わってから報告{ほうこく}する',
          'Wait until all work finishes to report, so you do not cause delays',
          'কাজে দেরি হলে ক্ষতি হবে ভেবে কাজ শেষ হওয়ার পর জানানো',
        ),
        t(
          '職長{しょくちょう}に怒{おこ}られないよう、傷口{きずぐち}を手袋{てぶくろ}で隠{かく}して我慢{がまん}する',
          'Hide the cut inside your glove and endure it to avoid getting scolded by foreman',
          'ফোরম্যানের বকুনি এড়াতে ক্ষতটি গ্লাভসের ভেতর লুকিয়ে রেখে সহ্য করা',
        ),
      ],
      answer: 0,
      why: t(
        'どんな小{ちい}さな傷{きず}でも破傷風{はしょうふう}や化膿{かのう}の恐{おそ}れがあり、同{おな}じ場所{ばしょ}で他{ほか}の人{ひと}が大怪我{おおがけ}をする危険{きけん}も隠{かく}れています。即座{そくざ}の報告{ほうこく}が義務{ぎむ}です。',
        'Even minor cuts carry infection risks, and may indicate hazards that could injure others. Immediate reporting is mandatory.',
        'যতই ছোট ক্ষত হোক ইনফেকশনের ঝুঁকি থাকে এবং অন্য কেউ সেখানে বড় দুর্ঘটনার শিকার হতে পারে। সাথে সাথে জানানো বাধ্যতামূলক।',
      ),
    },
    {
      q: t(
        '高所{こうしょ}での作業{さぎょう}を指示{しじ}されましたが、手{て}すりがなく、安全帯{あんぜんたい}（ハーネス）のフックを掛{か}ける場所{ばしょ}もありません。どうすべきですか？',
        'You were instructed to work at heights, but there is no handrail and no place to anchor your harness hook. What should you do?',
        'আপনাকে উঁচুতে কাজ করতে বলা হলো, কিন্তু কোনো হ্যান্ডরেল নেই এবং হারনেসের হুক আটকানোর জায়গাও নেই। আপনার কী করা উচিত?',
      ),
      options: [
        t(
          '気{き}をつけて足元{あしもと}を見{み}ながら、慎重{しんちょう}に作業{さぎょう}を進{すす}める',
          'Carefully proceed with the work while watching your step closely',
          'পায়ের নিচের দিকে ভালো করে তাকিয়ে সাবধানে কাজ চালিয়ে যাওয়া',
        ),
        t(
          '墜落{ついらく}の危険{きけん}があるため作業{さぎょう}を中断{ちゅうだん}し、職長{しょくちょう}に安全対策{あんぜんたいさく}を求{もと}める',
          'Suspend work due to fall risk and ask your foreman for safety measures',
          'পড়ে যাওয়ার মারাত্মক ঝুঁকি থাকায় কাজ বন্ধ রাখা এবং ফোরম্যানের কাছে নিরাপত্তার ব্যবস্থা চাওয়া',
        ),
        t(
          '同僚{どうりょう}に服{ふく}の背中{せなか}を掴{つか}んでもらいながら作業{さぎょう}する',
          'Have a coworker hold the back of your clothes while you work',
          'সহকর্মীকে জামার পেছনের অংশ ধরে রাখতে বলে কাজ করা',
        ),
        t(
          'ヘルメットをきつく締{し}め直{なお}してそのまま登{のぼ}る',
          'Tighten your helmet extra firmly and climb up anyway',
          'হেলমেট আরও শক্ত করে বেঁধে ওপরে উঠে যাওয়া',
        ),
      ],
      answer: 1,
      why: t(
        '労働者{ろうどうしゃ}には、生命{せいめい}や身体{しんたい}に危険{きけん}があるとき作業{さぎょう}を中断{ちゅうだん}して避難{ひなん}する権利{けんり}があります。無理{むり}に危険{きけん}な作業{さぎょう}を行{おこな}ってはいけません。',
        'Workers have the right to suspend work and evacuate when there is imminent danger to life or body. Never proceed under unsafe conditions.',
        'জীবন বা শরীরের সরাসরি বিপদের আশঙ্কা থাকলে কর্মীদের কাজ থামিয়ে নিরাপদ দূরত্বে যাওয়ার অধিকার আছে। কখনোই জোর করে কাজ করবেন না।',
      ),
    },
    {
      q: t(
        '日本{にほん}の労働者災害補償保険{ろうどうしゃさいがいほしょうほけん}（労災保険{ろうさいほけん}）についての説明{せつめい}で、正{ただ}しいものはどれですか？',
        'Which statement correctly describes Japans Industrial Accident Compensation Insurance (Rosai)?',
        'জাপানের কর্মক্ষেত্র দুর্ঘটনা ক্ষতিপূরণ বীমা (রোজাই) সম্পর্কে নিচের কোন বক্তব্যটি সঠিক?',
      ),
      options: [
        t(
          '日本人{にほんじん}正社員{せいしゃいん}だけが対象{たいしょう}で、技能実習生{ぎのうじっしゅうせい}や特定技能{とくていぎのう}は使{つか}えない',
          'Only Japanese regular employees are eligible; technical interns and SSW cannot use it',
          'শুধু জাপানি স্থায়ী কর্মীরা এটি পায়, টেকনিক্যাল ইন্টার্ন বা নির্দিষ্ট দক্ষতার কর্মীরা পারে না',
        ),
        t(
          '治療費{ちりょうひ}は全額{ぜんがく}自分{じぶん}の給料{きゅうりょう}から後{あと}で天引き{てんびき}される',
          'Medical costs are deducted in full from your salary later',
          'চিকিৎসার খরচ পরবর্তী সময়ে সম্পূর্ণ নিজের বেতন থেকে কেটে নেওয়া হয়',
        ),
        t(
          '国籍{こくせき}や在留資格{ざいりゅうしかく}に関係{かんけい}なく、仕事中{しごとちゅう}や通勤中{つうきんちゅう}のケガに全{すべ}ての労働者{ろうどうしゃ}へ適用{てきよう}される',
          'Applies to all workers for work or commute injuries regardless of nationality or visa',
          'জাতীয়তা বা ভিসার ধরন নির্বিশেষে সব কর্মীর কাজ বা যাতায়াতের আঘাতের ক্ষেত্রে প্রযোজ্য',
        ),
        t(
          '会社{かいしゃ}に雇{やと}われてから1年{ねん}以上{いじょう}経過{けいか}しないと申請{しんせい}できない',
          'You cannot apply until you have been employed by the company for over one year',
          'কোম্পানিতে ১ বছরের বেশি কাজ না করলে এর জন্য আবেদন করা যায় না',
        ),
      ],
      answer: 2,
      why: t(
        '労災保険{ろうさいほけん}は国{くに}の制度{せいど}であり、外国人労働者{がいこくじんろうどうしゃ}を含{ふく}む全{すべ}ての労働者{ろうどうしゃ}に初日{しょにち}から適用{てきよう}され、治療費{ちりょうひ}などが補償{ほしょう}されます。',
        'Rosai is a government program covering all workers including foreign staff from day one, covering medical care costs.',
        'রোজাই একটি সরকারি ব্যবস্থা যা প্রথম দিন থেকেই বিদেশি কর্মীসহ সবার জন্য প্রযোজ্য এবং চিকিৎসার যাবতীয় খরচ বহন করে।',
      ),
    },
    {
      q: t(
        '床{ゆか}にある約{やく}20kgの重{おも}い鉄製部品{てつせいぶひん}を持{も}ち上{あ}げるとき、腰痛{ようつう}を防{ふせ}ぐ正{ただ}しい姿勢{しせい}はどれですか？',
        'When lifting a heavy ~20 kg iron part from the floor, what is the correct posture to prevent lower back pain?',
        'মেঝে থেকে প্রায় ২০ কেজির একটি ভারী লোহার যন্ত্রাংশ তোলার সময় কোমর ব্যথা এড়াতে সঠিক ভঙ্গি কোনটি?',
      ),
      options: [
        t(
          '膝{ひざ}を伸{の}ばしたまま腰{こし}だけを前{まえ}に曲{ま}げて持{も}ち上{あ}げる',
          'Keep knees straight and only bend your waist forward to lift',
          'হাঁটু সোজা রেখে শুধু কোমর সামনে ঝুঁকিয়ে তোলা',
        ),
        t(
          '勢{いきお}いをつけて体{からだ}をひねりながら素早{すばや}く持{も}ち上{あ}げる',
          'Twist your torso quickly to use momentum to lift',
          'শরীর দ্রুত মুচড়ে ঝাঁকুনি দিয়ে তোলা',
        ),
        t(
          '荷物{にもつ}から離{はな}れて腕{うで}を前{まえ}に伸{の}ばしたまま持{も}ち上{あ}げる',
          'Stand far away from the load and lift with fully outstretched arms',
          'মাল থেকে দূরে দাঁড়িয়ে দুই হাত সামনে পুরো প্রসারিত করে তোলা',
        ),
        t(
          '荷物{にもつ}に体{からだ}を近{ちか}づけ、膝{ひざ}を曲{ま}げて腰{こし}を落{お}とし、足{あし}の力{ちから}で持{も}ち上{あ}げる',
          'Bring body close, bend knees and lower hips, and lift using leg strength',
          'মালের কাছে শরীর এনে, হাঁটু ভাঁজ করে কোমর নামিয়ে পায়ের শক্তিতে তোলা',
        ),
      ],
      answer: 3,
      why: t(
        '膝{ひざ}を伸{の}ばしたまま持{も}ち上{あ}げると腰{こし}に大{おお}きな負担{ふたん}がかかり、ぎっくり腰{ごし}の原因{げんいん}になります。腰{こし}を落{お}とし、荷物{にもつ}を体{からだ}に密着{みっちゃく}させます。',
        'Lifting with straight knees puts extreme stress on your lower back, causing disc injuries. Lower your hips and keep the load close to your body.',
        'হাঁটু সোজা রেখে তুললে কোমরে অতিরিক্ত চাপ পড়ে এবং মারাত্মক চোট লাগে। কোমর নিচু করে মাল শরীরের সাথে চেপে ধরে তোলা উচিত।',
      ),
    },
    {
      q: t(
        '職長{しょくちょう}から日本語{にほんご}で指示{しじ}を受{う}けましたが、専門用語{せんもんようご}があり意味{いみ}がよく分{わ}かりませんでした。どうすべきですか？',
        'You received instructions in Japanese from your foreman, but could not understand well due to technical jargon. What should you do?',
        'ফোরম্যান আপনাকে জাপানি ভাষায় নির্দেশ দিলেন, কিন্তু বিশেষ কারিগরি শব্দের কারণে আপনি ভালো বুঝতে পারলেন না। কী করবেন?',
      ),
      options: [
        t(
          '「もう一度{いちど}ゆっくり教{おし}えてください」とお願{ねが}いし、分{わ}かるまで確認{かくにん}する',
          'Ask them to explain slowly once more and confirm until you clearly understand',
          '"আরেকবার দয়া করে আস্তে বুঝিয়ে বলুন" বলে স্পষ্ট না বোঝা পর্যন্ত নিশ্চিত হওয়া',
        ),
        t(
          '分{わ}からないと言{い}うと恥{は}ずかしいので、「はい」と返事{へんじ}して見{み}よう見{み}まねで作業{さぎょう}する',
          'Feel embarrassed to admit, say "yes", and work by imitating others',
          'বুঝিনি বলতে লজ্জা পেয়ে "হ্যাঁ" বলে অন্যের দেখাদেখি আন্দাজে কাজ শুরু করা',
        ),
        t(
          '自分{じぶん}の勝手{かって}な解釈{かいしゃく}でやりやすい方法{ほうほう}を選{えら}んで作業{さぎょう}する',
          'Choose whatever way seems easiest based on your own guess',
          'নিজের মতো সহজ মনে হওয়া উপায়ে কাজ করা',
        ),
        t(
          '作業{さぎょう}を何{なに}もせずにその場{ば}で黙{だま}って立{た}ち尽{つ}くす',
          'Stand there silently without doing anything at all',
          'কাউকে কিছু না বলে চুপচাপ দাঁড়িয়ে থাকা',
        ),
      ],
      answer: 0,
      why: t(
        '曖昧{あいまい}なまま作業{さぎょう}を始{はじ}めることは現場{げんば}で最{もっと}も危険{きけん}です。職長{しょくちょう}は質問{しつもん}されることを歓迎{かんげい}します。必{かなら}ず確認{かくにん}しましょう。',
        'Starting work with vague understanding is the most dangerous act on site. Foremen welcome questions. Always verify.',
        'স্পষ্ট না বুঝে কাজ শুরু করা সাইটের সবচেয়ে বিপজ্জনক ভুল। ফোরম্যানকে প্রশ্ন করলে তিনি খুশিই হবেন। অবশ্যই নিশ্চিত হয়ে নিন।',
      ),
    },
    {
      q: t(
        'ボール盤{ばん}や旋盤{せんばん}など、刃物{はもの}が高速{こうそく}で回転{かいてん}する工作機械{こうさくきかい}を使用{しよう}するとき、手元{てもと}の安全{あんぜん}として正{ただ}しいものはどれですか？',
        'When operating machine tools where blades rotate at high speed like drilling or lathe machines, what is the correct hand safety practice?',
        'ড্রিল বা লেদ মেশিনের মতো দ্রুত ঘূর্ণায়মান যন্ত্রপাতিতে কাজ করার সময় হাতের নিরাপত্তার জন্য কোনটি সঠিক?',
      ),
      options: [
        t(
          '手{て}を保護{ほご}するため、厚手{あつで}の布製軍手{ぬのせいぐんて}を着用{ちゃくよう}する',
          'Wear thick cotton woven work gloves to protect your hands',
          'হাত রক্ষার জন্য মোটা কাপড়ের কাজের গ্লাভস পরা',
        ),
        t(
          '手袋{てぶくろ}は着用{ちゃくよう}せず素手{すで}で行{おこな}い、袖口{そでぐち}をしっかり締{し}める',
          'Do not wear gloves; work with bare hands, and keep cuffs tightly fastened',
          'গ্লাভস না পরে খালি হাতে কাজ করা, এবং জামার হাতা শক্তভাবে আটকানো',
        ),
        t(
          '首{くび}にタオルを巻{ま}き、長{なが}い髪{かみ}は結{むす}ばずに作業{さぎょう}する',
          'Wrap a towel around your neck and work with long hair loose',
          'গলায় তোয়ালে পেঁচিয়ে রাখা এবং লম্বা চুল না বেঁধে কাজ করা',
        ),
        t(
          '袖口{そでぐち}のボタンを外{はず}して腕{うで}を動{うご}かしやすくする',
          'Unbutton cuffs to make arms easier to move freely',
          'হাত সহজে নাড়াচাড়া করার জন্য জামার হাতার বোতাম খুলে রাখা',
        ),
      ],
      answer: 1,
      why: t(
        '回転機械{かいてんきかい}に手袋{てぶくろ}を使用{しよう}すると、繊維{せんい}が巻{ま}き込{こ}まれて指{ゆび}や手{て}を切断{せつだん}する重大事故{じゅうだいじこ}につながります。素手{すで}での作業{さぎょう}が原則{げんそく}です。',
        'Using gloves around rotating machinery risks the fabric getting caught, causing amputations. Working barehanded is the standard rule.',
        'ঘূর্ণায়মান মেশিনে গ্লাভস ব্যবহার করলে কাপড়ের সুতা আটকে হাত কেটে ফেলার মতো মারাত্মক দুর্ঘটনা ঘটে। খালি হাতে কাজ করাই নিয়ম।',
      ),
    },
    {
      q: t(
        '工場内{こうじょうない}で非常火災警報{ひじょうかさいけいほう}のサイレンが鳴{な}り響{ひび}きました。避難時{ひなんじ}の行動{こうどう}として正{ただ}しいものはどれですか？',
        'An emergency fire alarm siren went off inside the factory. Which action is correct when evacuating?',
        'কারখানার ভেতর হঠাৎ জরুরি অগ্নিকাণ্ডের সাইরেন বেজে উঠল। বের হওয়ার সময় কোন কাজটি সঠিক?',
      ),
      options: [
        t(
          'ロッカールームに戻{もど}って自分{じぶん}の財布{さいふ}やかばんを取{と}りに行{い}く',
          'Return to the locker room to retrieve your wallet and bag',
          'লকার রুমে ফিরে নিজের মানিব্যাগ ও ব্যাগ সংগ্রহ করতে যাওয়া',
        ),
        t(
          '火{ひ}がどこにあるか見物{けんぶつ}するため、煙{けむり}の方向{ほうこう}へ見{み}に行{い}く',
          'Go toward the smoke to watch where the fire is',
          'আগুন কোথায় লেগেছে দেখতে ধোঁয়ার দিকে যাওয়া',
        ),
        t(
          '機械{きかい}を停止{ていし}させ、荷物{にもつ}を持{も}たずに非常口{ひじょうぐち}から指定{してい}の避難場所{ひなんばしょ}へ急{いそ}ぐ',
          'Stop machine, leave personal belongings, and hasten to designated muster point via emergency exit',
          'মেশিন বন্ধ করে, মালামাল পেছনে ফেলে জরুরি বহির্গমন দিয়ে নির্দিষ্ট জমায়েত স্থানে যাওয়া',
        ),
        t(
          'エレベーターを使{つか}って素早{すばや}く1階{かい}に降{お}りる',
          'Use the elevator to descend quickly to the ground floor',
          'দ্রুত নিচতলায় নামার জন্য লিফট ব্যবহার করা',
        ),
      ],
      answer: 2,
      why: t(
        '避難時{ひなんじ}に荷物{にもつ}を取{と}りに戻{もど}ると逃{に}げ遅{おく}れて命{いのち}を落{お}とします。機械{きかい}を止{と}めたら直{ただ}ちに身一重{みひとつ}で避難場所{ひなんばしょ}へ向{む}かいます。',
        'Returning for belongings during evacuation delays escape and causes fatalities. Stop machinery and evacuate immediately with nothing in hand.',
        'পালানোর সময় জিনিসপত্র আনতে গেলে আটকে পড়ে প্রাণহানি ঘটতে পারে। মেশিন বন্ধ করে সাথে সাথে খালি হাতে জমায়েত স্থানে যান।',
      ),
    },
    {
      q: t(
        '作業開始前{さぎょうかいしまえ}の点検{てんけん}で、電動{でんどう}工具{こうぐ}の電源{でんげん}コードの被覆{ひふく}が裂{さ}けて銅線{どうせん}が見{み}えているのを見{み}つけました。どうすべきですか？',
        'During pre-operation inspection, you noticed an electric tool power cord rubber was cracked with copper wires visible. What should you do?',
        'কাজ শুরুর আগের পরীক্ষায় দেখলেন বৈদ্যুতিক টুলের তারের রবার কেটে তামার তার দেখা যাচ্ছে। আপনার কী করা উচিত?',
      ),
      options: [
        t(
          'ビニールテープを巻{ま}き付{つ}けて、そのまま作業{さぎょう}に使{つか}う',
          'Wrap it tightly with vinyl tape and use it for work anyway',
          'ট্যাপ দিয়ে ভালো করে পেঁচিয়ে নিয়ে তা দিয়েই কাজ চালিয়ে যাওয়া',
        ),
        t(
          '銅線{どうせん}に手{て}が触{ふ}れないよう注意{ちゅうい}しながら作業{さぎょう}を続{つづ}ける',
          'Continue working carefully so your hands do not touch the copper wire',
          'তামার তারে যাতে হাত না লাগে সেদিকে খেয়াল রেখে সাবধানে কাজ করা',
        ),
        t(
          '今日{きょう}の作業{さぎょう}が終{お}わってから、同僚{どうりょう}に直{なお}してもらう',
          'Ask a colleague to fix it after todays shift ends',
          'আজকের কাজ শেষ হওয়ার পর সহকর্মীকে ঠিক করে দিতে বলা',
        ),
        t(
          '絶対{ぜったい}に使用{しよう}せず、「使用禁止{しようきんし}」の札{ふだ}を掛{か}けて職長{しょくちょう}に報告{ほうこく}して交換{こうかん}する',
          'Never use it; attach an Out of Service tag, report to foreman, and replace it',
          'কখনোই ব্যবহার না করা, "ব্যবহার নিষেধ" ট্যাগ লাগানো, ফোরম্যানকে জানানো ও বদলে নেওয়া',
        ),
      ],
      answer: 3,
      why: t(
        '被覆{ひふく}が破{やぶ}れた電気機器{でんききき}は漏電{ろうでん}や感電{かんでん}の危険{きけん}が極{きわ}めて高{たか}いです。自己判断{じこはんだん}で修理{しゅうり}せず、直{ただ}ちに使用禁止{しようきんし}にします。',
        'Damaged electrical equipment presents severe electrocution hazards. Never attempt personal repairs; tag it out of service immediately.',
        'তার কাটা বৈদ্যুতিক যন্ত্রপাতিতে মারাত্মক বৈদ্যুতিক শক লাগার চরম ঝুঁকি থাকে। নিজে মেরামত না করে সাথে সাথে ব্যবহার নিষিদ্ধ করুন।',
      ),
    },
  ],
  glossary: [
    {
      ja: '雇入れ時教育',
      reading: 'やといれじきょういく',
      en: 'Hiring-time safety training',
      bn: 'নিয়োগকালীন নিরাপত্তা প্রশিক্ষণ',
    },
    {
      ja: '職長',
      reading: 'しょくちょう',
      en: 'Foreman / site supervisor',
      bn: 'ফোরম্যান / সাইট সুপারভাইজার',
    },
    {
      ja: '保護具',
      reading: 'ほごぐ',
      en: 'PPE (personal protective equipment)',
      bn: 'পিপিই (ব্যক্তিগত সুরক্ষাসামগ্রী)',
    },
    {
      ja: '保護帽',
      reading: 'ほごぼう',
      en: 'Safety helmet / hard hat',
      bn: 'সেফটি হেলমেট',
    },
    {
      ja: '立入禁止',
      reading: 'たちいりきんし',
      en: 'Off-limits / no entry',
      bn: 'প্রবেশ নিষেধ',
    },
    {
      ja: '指差呼称',
      reading: 'しさこしょう',
      en: 'Pointing and calling (safety check)',
      bn: 'আঙুল নির্দেশ ও মুখে বলে নিশ্চিতকরণ',
    },
    {
      ja: '危険予知',
      reading: 'きけんよち',
      en: 'Hazard prediction (KY)',
      bn: 'বিপদ পূর্বানুমান (কে-ওয়াই)',
    },
    {
      ja: 'ヒヤリ・ハット',
      reading: 'ひやりはっと',
      en: 'Near-miss incident',
      bn: 'অল্পের জন্য রক্ষা পাওয়ার ঘটনা',
    },
    {
      ja: '労災保険',
      reading: 'ろうさいほけん',
      en: 'Workers compensation insurance',
      bn: 'কর্মক্ষেত্র ক্ষতিপূরণ বীমা',
    },
    {
      ja: '始業前点検',
      reading: 'しぎょうまえてんけん',
      en: 'Pre-operation inspection',
      bn: 'কাজ শুরুর আগের পরীক্ষা',
    },
    {
      ja: '整理整頓',
      reading: 'せいりせいとん',
      en: 'Sort and organize (5S tidiness)',
      bn: 'পরিচ্ছন্নতা ও শৃঙ্খলা (৫এস)',
    },
    {
      ja: '非常停止',
      reading: 'ひじょうていし',
      en: 'Emergency stop',
      bn: 'জরুরি নির্গমন বা থামা',
    },
  ],
}
