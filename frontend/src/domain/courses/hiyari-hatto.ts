import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string, bn: string): Tri => ({ ja, en, bn })

export const hiyariHatto: CourseData = {
  id: 'hiyari-hatto',
  category: 'core',
  level: 1,
  minutes: 15,
  title: t(
    'ヒヤリ・ハット報告{ほうこく}',
    'Near-miss (Hiyari-Hatto) reporting',
    'হিয়ারি-হাট্টো (আসন্ন বিপদ) রিপোর্ট',
  ),
  sub: t(
    '「ヒヤリ」とした瞬間{しゅんかん}を隠{かく}さず報告{ほうこく}し、大事故{だいじこ}を未然{みぜん}に防{ふせ}ぐ。',
    'Report near-miss moments without hiding to prevent major accidents.',
    'সামান্য বেঁচে যাওয়ার ঘটনা না লুকিয়ে রিপোর্ট করো, বড় দুর্ঘটনা প্রতিরোধ করো।',
  ),
  refs: [
    '労働安全衛生法 第59条（安全衛生教育）',
    '厚生労働省 職場のあんぜんサイト（ヒヤリ・ハット事例）',
    '中央労働災害防止協会（中災防） ヒヤリ・ハット活動推進指針',
  ],
  panels: [
    {
      who: 'foreman',
      text: t(
        'ラヒムさん、今日{きょう}の作業{さぎょう}はどうでしたか？危険{きけん}なことはありませんでしたか？',
        'Rahim, how did work go today? Did anything dangerous happen?',
        'রহিম, আজকের কাজ কেমন হলো? কোনো বিপদের মুখোমুখি হয়েছিলে?',
      ),
    },
    {
      who: 'worker',
      text: t(
        '実{じつ}は階段{かいだん}で少{すこ}し足{あし}が滑{すべ}りました。でもケガはなく、壊{こわ}れた物{もの}もありません。',
        'Actually, my foot slipped a bit on the stairs. But there was no injury and nothing broke.',
        'আসলে সিঁড়িতে আমার পা সামান্য পিছলে গিয়েছিল। তবে চোট লাগেনি এবং কিছু ভাঙেনি।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '誰{だれ}もケガをしていないのに、怒{おこ}られたり国{くに}へ帰{かえ}されたりするのが怖{こわ}くて言{い}えませんでした。',
        'Since no one was hurt, I was afraid of getting scolded or sent back home, so I stayed silent.',
        'কারো আঘাত লাগেনি দেখে বকা খাওয়ার বা দেশে ফেরত পাঠানোর ভয়ে কাউকে কিছু বলিনি।',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '話{はな}してくれてありがとう。報告{ほうこく}して怒{おこ}られたり帰国{きこく}させられたりすることは絶対{ぜったい}にありません。',
        'Thank you for telling me. You will never be scolded or sent home for reporting.',
        'বলার জন্য ধন্যবাদ। রিপোর্ট করার কারণে বকা দেওয়া বা দেশে ফেরত পাঠানোর কোনো প্রশ্নই ওঠে না।',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'むしろ危険{きけん}を隠{かく}すことが一番{いちばん}危{あぶ}ないのです。ヒヤリとした報告{ほうこく}は仲間{なかま}を守{まも}る勇気{ゆうき}ある行動{こうどう}ですよ。',
        'In fact, hiding hazards is the most dangerous thing. Reporting a near-miss is a brave act that protects teammates.',
        'বরং বিপদ লুকিয়ে রাখাই সবচেয়ে বিপজ্জনক। সামান্য বেঁচে যাওয়ার ঘটনা জানানো সহকর্মীদের বাঁচানোর এক সাহসী কাজ।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'ケガがない小{ちい}さなことでも、なぜ報告{ほうこく}が必要{ひつよう}なのですか？',
        'Why is it necessary to report even small things where no one got hurt?',
        'কেউ চোট না পেলেও এত ছোট ঘটনা রিপোর্ট করা কেন এত জরুরি?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '「ハインリッヒの法則{ほうそく}（1:29:300）」という経験則{けいけんそく}があります。1件{けん}の重大{じゅうだい}事故{じこ}の裏{うら}には、300件{けん}のヒヤリ・ハットが潜{ひそ}んでいます。',
        'There is Heinrich\'s rule of thumb (1:29:300). Behind 1 major accident lie 300 near-miss incidents.',
        '"হাইনরিখের সূত্র (১:২৯:৩০০)" নামে একটি বাস্তব নিয়ম আছে। ১টি বড় দুর্ঘটনার পেছনে ৩০০টি আসন্ন বিপদের ঘটনা লুকিয়ে থাকে।',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '300のヒヤリの段階{だんかい}で原因{げんいん}をなくせば、命{いのち}にかかわる大事故{だいじこ}を未然{みぜん}に防{ふせ}ぐことができます。',
        'If we eliminate the causes at the stage of the 300 near-misses, we can prevent fatal accidents beforehand.',
        'এই ৩০০টি বিপদের সময়ই কারণগুলো দূর করলে আমরা প্রাণঘাতী বড় দুর্ঘটনা আগেভাগেই ঠেকাতে পারি।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'よく分{わ}かりました！階段{かいだん}に油{あぶら}が垂{た}れていたので、すぐ拭{ふ}き取{と}って職長{しょくちょう}に1分{いっぷん}で報連相{ほうれんそう}します！',
        'I understand clearly! Oil was dripping on the stairs, so I will wipe it up immediately and report to you in 1 minute!',
        'আমি খুব ভালো বুঝেছি! সিঁড়িতে তেল পড়েছিল, তাই এখনই মুছে ১ মিনিটে আপনাকে হো-রেন-সো (রিপোর্ট) করছি!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '素晴{すば}らしい判断{はんだん}です。小{ちい}さなヒヤリを全員{ぜんいん}で共有{きょうゆう}して、安全{あんぜん}な現場{げんば}をつくりましょう！',
        'Excellent judgment. Let us share small near-misses as a team and build a safe workplace!',
        'চমৎকার সিদ্ধান্ত। ছোটখাটো বিপদ সবাই মিলে শেয়ার করে একটি নিরাপদ কর্মক্ষেত্র গড়ে তুলি!',
      ),
    },
  ],
  handbook: [
    t(
      '「ヒヤリ」としたり「ハッ」としたら、ケガがなくても直{ただ}ちに職長{しょくちょう}へ報告{ほうこく}する。',
      'If you experience a near-miss or close call, report to your foreman immediately even if no one is hurt.',
      'অল্পের জন্য বেঁচে গেলে বা চমকে উঠলে, চোট না লাগলেও সাথে সাথে ফোরম্যানকে জানাও।',
    ),
    t(
      'ヒヤリ・ハットの報告{ほうこく}で怒{おこ}られたり、帰国{きこく}させられたりすることは絶対{ぜったい}にない。',
      'You will never be scolded or deported for submitting a near-miss report.',
      'হিয়ারি-হাট্টো রিপোর্ট করার কারণে কখনো বকা দেওয়া বা দেশে ফেরত পাঠানো হবে না।',
    ),
    t(
      '危険{きけん}を隠{かく}すことや黙{だま}っていることが、現場{げんば}で最も危険{きけん}な行為{こうい}である。',
      'Hiding a hazard or staying silent is the most dangerous act on site.',
      'বিপদ লুকিয়ে রাখা বা চুপ থাকা কর্মক্ষেত্রের সবচেয়ে মারাত্মক বিপজ্জনক কাজ।',
    ),
    t(
      'ハインリッヒの法則{ほうそく}（1:29:300）：300のヒヤリをなくすことが1件{けん}の大事故{だいじこ}を防{ふせ}ぐ。',
      'Heinrich\'s Law (1:29:300): Eliminating 300 near-misses prevents 1 major catastrophe.',
      'হাইনরিখের সূত্র (১:২৯:৩০০): ৩০০টি ছোট বিপদ দূর করলেই ১টি বড় দুর্ঘটনা রোধ করা যায়।',
    ),
    t(
      '報告{ほうこく}は「いつ・どこで・何{なに}をして・どうヒヤリとしたか」を簡潔{かんけつ}に1分{いっぷん}で伝{つた}える。',
      'Report in 1 minute concisely stating when, where, what you were doing, and what happened.',
      'কখন, কোথায়, কী করতে গিয়ে কীভাবে বিপদ হয়েছিল তা সংক্ষেপে ১ মিনিটে জানাও।',
    ),
    t(
      '自分{じぶん}だけのミスだと思{おも}い込{こ}まず、職場{しょくば}の仲間{なかま}全員{ぜんいん}で情報{じょうほう}を共有{きょうゆう}する。',
      'Do not assume it was only your mistake; share the information with all coworkers.',
      'শুধু নিজের ভুল ভেবে একা চেপে রেখো না; কর্মক্ষেত্রের সকল সহকর্মীর সাথে তথ্য শেয়ার করো।',
    ),
    t(
      '油{あぶら}こぼれや足場{あしば}の段差{だんさ}を見{み}つけたら、報告{ほうこく}と同時{どうじ}に応急{おうきゅう}処置{しょち}を行{おこな}う。',
      'If you find spilled oil or uneven scaffold steps, take immediate action while reporting.',
      'তেল ছড়ানো বা মাচায় উঁচুনীচু দেখলে রিপোর্ট করার সাথে সাথেই সাময়িক ব্যবস্থা নাও।',
    ),
    t(
      '日本語{にほんご}に自信{じしん}がなくても、身振{みぶ}りや写真{しゃしん}、単語{たんご}を使{つか}って職長{しょくちょう}に報連相{ほうれんそう}する。',
      'Even if not confident in Japanese, use gestures, photos, or key words to report to your foreman.',
      'জাপানি ভাষায় দুর্বল হলেও অঙ্গভঙ্গি, ছবি বা শব্দ ব্যবহার করে ফোরম্যানকে তথ্য জানাও।',
    ),
    t(
      '同僚{どうりょう}のヒヤリ・ハットを見{み}かけた時{とき}も、声{こえ}をかけ合{あ}って一緒{いっしょ}に報告{ほうこく}する。',
      'When you witness a coworker\'s near-miss, call out to each other and report together.',
      'সহকর্মীর আসন্ন বিপদ দেখলে একে অপরকে ডেকে একসাথে রিপোর্ট করতে উৎসাহিত করো।',
    ),
    t(
      '「何{なに}も起きなくてよかった」で終わらせず、設備{せつび}の改善{かいぜん}や手順{てじゅん}の見直{みなお}しにつなげる。',
      'Do not end with "glad nothing happened"; turn it into equipment fixes or procedure reviews.',
      '"ভাগ্যিস কিছু হয়নি" বলে ফেলে রেখো না; যন্ত্রপাতির সংস্কার বা নিয়ম সংশোধনে কাজে লাগাও।',
    ),
  ],
  reel: [
    t(
      '階段{かいだん}の油{あぶら}、拭{ふ}き取{と}り、ヨシ！',
      'Stair oil wiped clean, yoshi!',
      'সিঁড়ির তেল মোছা শেষ, ঠিক আছে!',
    ),
    t(
      '足元{あしもと}の段差{だんさ}、注意{ちゅうい}表示{ひょうじ}、ヨシ！',
      'Uneven step warning displayed, yoshi!',
      'উঁচুনীচু জায়গার সতর্কবার্তা টাঙানো, ঠিক আছে!',
    ),
    t(
      '開口部{かいこうぶ}の安全{あんぜん}ネット、展張{てんちょう}、ヨシ！',
      'Opening safety netting spread, yoshi!',
      'মেঝের গর্তে সেফটি নেট পাতা, ঠিক আছে!',
    ),
    t(
      '工具{こうぐ}の落下防止{らっかぼうし}ワイヤー、装着{そうちゃく}、ヨシ！',
      'Tool lanyard attached, yoshi!',
      'টুলস পড়ে যাওয়া রোধক তার লাগানো, ঠিক আছে!',
    ),
    t(
      '職長{しょくちょう}へのヒヤリ報告{ほうこく}、1分{いっぷん}、ヨシ！',
      'Near-miss 1-minute report to foreman, yoshi!',
      'ফোরম্যানকে ১ মিনিটে বিপদের রিপোর্ট, ঠিক আছে!',
    ),
    t(
      '危険{きけん}箇所の情報{じょうほう}共有{きょうゆう}、報連相{ほうれんそう}、ヨシ！',
      'Hazard info shared via Ho-Ren-So, yoshi!',
      'বিপদের তথ্য হো-রেন-সো মাধ্যমে শেয়ার, ঠিক আছে!',
    ),
    t(
      'ハインリッヒの法則{ほうそく}、ゼロ災{さい}、ヨシ！',
      'Heinrich\'s law, zero accidents, yoshi!',
      'হাইনরিখের সূত্র মেনে দুর্ঘটনা শূন্য, ঠিক আছে!',
    ),
  ],
  drills: [
    {
      id: 'hiyari-spot',
      kind: 'spot',
      secs: 30,
      need: 4,
      name: t(
        '作業場{さぎょうば}のヒヤリ要因{よういん}発見{はっけん}',
        'Spotting near-miss hazard factors',
        'কর্মক্ষেত্রের আসন্ন বিপদের কারণ শনাক্তকরণ',
      ),
      goal: t(
        '30秒{びょう}で作業場{さぎょうば}に潜{ひそ}む危険{きけん}な要因{よういん}を4つ見{み}つける。',
        'Find 4 hidden hazard factors in the workshop in 30 seconds.',
        '৩০ সেকেন্ডে কর্মক্ষেত্রে লুকিয়ে থাকা ৪টি বিপদের কারণ চিহ্নিত করো।',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '通路{つうろ}に放置{ほうち}された油{あぶら}の垂{た}れ',
            'Oil spilled and left on walkway',
            'হাঁটার পথে ফেলে রাখা ছড়ানো তেল',
          ),
        },
        {
          hazard: false,
          label: t(
            '整理{せいり}整頓{せいとん}された工具棚{こうぐだな}',
            'Neatly organized tool rack',
            'পরিপাটি করে গোছানো টুল র‍্যাক',
          ),
        },
        {
          hazard: true,
          label: t(
            '足場{あしば}の端{はし}に置{お}かれた丸{まる}いパイプ',
            'Round pipe left loose near scaffold edge',
            'মাচার কিনারায় আলগা ফেলে রাখা গোল পাইপ',
          ),
        },
        {
          hazard: false,
          label: t(
            '階段{かいだん}に貼{は}られた滑{すべ}り止{ど}めテープ',
            'Anti-slip tape applied on stairs',
            'সিঁড়ির ধাপে লাগানো অ্যান্টি-স্লিপ টেপ',
          ),
        },
        {
          hazard: true,
          label: t(
            '通路{つうろ}を横切{よこぎ}る仮設{かせつ}配管{はいかん}の段差{だんさ}',
            'Temporary pipe crossing walkway causing trip ledge',
            'হাঁটার পথের ওপর ছড়ানো পাইপের উঁচুনীচু বাধা',
          ),
        },
        {
          hazard: false,
          label: t(
            '白線{はくせん}で区画{くかく}された歩行者{ほこうしゃ}通路{つうろ}',
            'Pedestrian lane demarcated with white lines',
            'সাদা দাগ দিয়ে চিহ্নিত হাঁটার পথ',
          ),
        },
        {
          hazard: true,
          label: t(
            '高所{こうしょ}作業{さぎょう}での工具{こうぐ}落下防止{らっかぼうし}ヒモ未装着{みそうちゃく}',
            'Working at height without tool lanyard attached',
            'উঁচু স্থানে কাজ করার সময় টুলসের সেফটি ল্যানিয়ার্ড না লাগানো',
          ),
        },
        {
          hazard: false,
          label: t(
            '扉{とびら}が閉{し}められ施錠{せじょう}された分電盤{ぶんでんばん}',
            'Electrical distribution panel closed and latched',
            'দরজা বন্ধ ও লক করা বিদ্যুৎ বিতরণ প্যানেল',
          ),
        },
        {
          hazard: true,
          label: t(
            '安全{あんぜん}カバーが外{はず}されたグラインダー',
            'Grinder operated with safety cover removed',
            'নিরাপত্তা কভার ছাড়া চালানো হ্যান্ড গ্রাইন্ডার',
          ),
        },
        {
          hazard: false,
          label: t(
            '表示{ひょうじ}が見{み}えやすい定位置{ていいち}の消火器{しょうかき}',
            'Fire extinguisher in designated spot with clear sign',
            'নির্ধারিত স্থানে স্পষ্ট সাইনবোর্ডসহ অগ্নিনির্বাপক',
          ),
        },
      ],
    },
    {
      id: 'one-minute-report-seq',
      kind: 'seq',
      name: t(
        '1分間{いっぷんかん}ヒヤリ報告{ほうこく}の手順{てじゅん}',
        'One-minute near-miss reporting procedure',
        '১ মিনিটে হিয়ারি-হাট্টো রিপোর্ট করার ধাপসমূহ',
      ),
      goal: t(
        'ヒヤリ・ハットを職長に報告する4つのステップを正しい順序に並べる。',
        'Arrange the 4 steps of reporting a near-miss to the foreman in the correct order.',
        'ফোরম্যানকে আসন্ন বিপদ রিপোর্ট করার ৪টি ধাপ সঠিক ক্রমে সাজাও।',
      ),
      steps: [
        t(
          '1. 作業{さぎょう}を一旦{いったん}止{と}めて深呼吸{しんこきゅう}し、自分{じぶん}と周囲{しゅうい}の安全{あんぜん}を確認{かくにん}する',
          '1. Pause work, take a breath, and confirm safety for yourself and surroundings',
          '১. কাজ সাময়িক থামিয়ে দীর্ঘশ্বাস নাও এবং নিজের ও চারপাশের নিরাপত্তা নিশ্চিত করো',
        ),
        t(
          '2. すぐに職長{しょくちょう}のもとへ行{い}き、「ヒヤリ・ハットの報告{ほうこく}です」と声{こえ}をかける',
          '2. Go to the foreman immediately and announce "I have a near-miss report"',
          '২. সাথে সাথে ফোরম্যানের কাছে গিয়ে বলো "একটি হিয়ারি-হাট্টো রিপোর্ট আছে"',
        ),
        t(
          '3. 「いつ・どこで・何{なに}をして・どうヒヤリとしたか」を1分{いっぷん}で簡潔{かんけつ}に伝{つた}える',
          '3. Concisely explain when, where, what you were doing, and what happened in 1 minute',
          '৩. কখন, কোথায়, কী কাজ করতে গিয়ে কী ঘটেছিল তা ১ মিনিটে সংক্ষেপে জানাও',
        ),
        t(
          '4. 職長{しょくちょう}と現場{げんば}を確認{かくにん}し、油{あぶら}の清掃{せいそう}や危険{きけん}表示{ひょうじ}などの応急{おうきゅう}処置{しょち}を行{おこな}う',
          '4. Check the site with the foreman and take immediate action like wiping oil or posting warnings',
          '৪. ফোরম্যানকে নিয়ে ঘটনাস্থল দেখে তেল পরিষ্কার বা সতর্কবার্তা টাঙানোর মতো জরুরি ব্যবস্থা নাও',
        ),
      ],
    },
    {
      id: 'heinrich-pyramid-seq',
      kind: 'seq',
      name: t(
        'ハインリッヒの法則{ほうそく}（ピラミッド構造{こうぞう}）',
        'Heinrich\'s law pyramid structure',
        'হাইনরিখের সূত্র (পিরামিড কাঠামো)',
      ),
      goal: t(
        '事故ピラミッドの土台（発生件数の多い順）から頂点へ正しい順序に並べる。',
        'Arrange from the pyramid base (highest count) to the top in the correct order.',
        'দুর্ঘটনা পিরামিডের ভিত্তি (সর্বোচ্চ সংখ্যা) থেকে চূড়া পর্যন্ত সঠিক ক্রমে সাজাও।',
      ),
      steps: [
        t(
          '1. 【土台{どだい}】無数{むすう}の不安全{ふあんぜん}行動{こうどう}と不安全{ふあんぜん}状態{じょうたい}（日常{にちじょう}に潜{ひそ}む危険{きけん}の芽{め}）',
          '1. [Base] Countless unsafe behaviors and unsafe conditions (latent day-to-day hazard seeds)',
          '১. [ভিত্তি] অগণিত অনিরাপদ আচরণ ও অনিরাপদ অবস্থা (প্রতিদিনের কাজে লুকিয়ে থাকা বিপদের বীজ)',
        ),
        t(
          '2. 【300件{けん}】ケガに至{いた}らなかった無傷害{むしょうがい}事故{じこ}（ヒヤリ・ハットの体験{たいけん}）',
          '2. [300 cases] Non-injury incidents without physical harm (near-miss experiences)',
          '২. [৩০০টি ঘটনা] চোট না লাগা অক্ষত ঘটনা (হিয়ারি-হাট্টো বা অল্পের জন্য রক্ষা পাওয়ার অভিজ্ঞতা)',
        ),
        t(
          '3. 【29件{けん}】応急{おうきゅう}手当{てあて}やすり傷{きず}で済{す}んだ軽傷{けいしょう}事故{じこ}（小{ちい}さな労働{ろうどう}災害{さいがい}）',
          '3. [29 cases] Minor injury accidents requiring first aid or minor treatment',
          '৩. [২৯টি ঘটনা] প্রাথমিক চিকিৎসা বা সামান্য ছড়ে যাওয়া ছোটখাটো দুর্ঘটনা',
        ),
        t(
          '4. 【1件{けん}】命{いのち}を落{お}としたり重度{じゅうど}の後遺症{こういしょう}が残{のこ}る重大{じゅうだい}災害{さいがい}',
          '4. [1 case] Major catastrophic disaster resulting in fatality or permanent disability',
          '৪. [১টি ঘটনা] প্রাণহানি বা স্থায়ী পঙ্গুত্ব ডেকে আনা মারাত্মক গুরুতর দুর্ঘটনা',
        ),
      ],
    },
  ],
  inspect: [
    {
      id: 'hiyari-report-form',
      defective: false,
      name: t(
        'ヒヤリ・ハット報告書{ほうこくしょ}の記入{きにゅう}状態{じょうたい}',
        'Near-miss report form entry state',
        'হিয়ারি-হাট্টো রিপোর্ট ফর্ম পূরণের অবস্থা',
      ),
      spots: [
        {
          x: 32,
          y: 24,
          check: t(
            '発生{はっせい}日時{にちじ}と場所{ばしょ}：具体性{ぐたいせい}があるか（良品{りょうひん}：「第2船台階段」と特定{とくてい} / 不良{ふりょう}：「工場内」と曖昧{あいまい}）',
            'Date and location: Specific detail provided (Good: Specified as "Berth 2 stairs" / Defect: Vague entry like "In factory")',
            'তারিখ ও স্থান: নির্দিষ্ট বিবরণ দেওয়া (ভালো: "২নং বার্থের সিঁড়ি" নির্দিষ্ট / ত্রুটি: "কারখানায়" অস্পষ্ট)',
          ),
        },
        {
          x: 68,
          y: 46,
          check: t(
            '状況{じょうきょう}と原因{げんいん}：「何{なに}をしてどうなったか」が客観的{きゃっかんてき}か（良品{りょうひん}：油で滑った事実{じじつ}を記載{きさい} / 不良{ふりょう}：個人{こじん}の責任{せきにん}を非難{ひなん}）',
            'Situation and cause: Objective description (Good: States fact of slipping on oil / Defect: Blaming individuals)',
            'পরিস্থিতি ও কারণ: বস্তুনিষ্ঠ বিবরণ (ভালো: তেলে পিছলে যাওয়ার সত্য উল্লেখ / ত্রুটি: ব্যক্তিকে দোষারোপ করা)',
          ),
        },
        {
          x: 50,
          y: 78,
          check: t(
            '応急{おうきゅう}処置{しょち}：直{ただ}ちに取{と}った対策{たいさく}があるか（良品{りょうひん}：油の拭き取りとコーン設置 / 不良{ふりょう}：放置）',
            'Immediate action: Countermeasures taken on the spot (Good: Wiped oil and placed warning cone / Defect: Ignored)',
            'জরুরি ব্যবস্থা: ঘটনাস্থলে তাৎক্ষণিক ব্যবস্থা নেওয়া (ভালো: তেল মোছা ও কোন বসানো / ত্রুটি: অবহেলা করা)',
          ),
        },
      ],
    },
    {
      id: 'scaffold-stair-hazard',
      defective: true,
      name: t(
        '足場{あしば}階段{かいだん}の現場{げんば}状況{じょうきょう}',
        'Scaffold stairway site condition',
        'মাচা বেয়ে ওঠার সিঁড়ির কর্মক্ষেত্রের অবস্থা',
      ),
      spots: [
        {
          x: 35,
          y: 32,
          check: t(
            '踏{ふ}み板{いた}の表面{ひょうめん}：油{あぶら}や泥{どろ}の付着{ふちゃく}がないか（良品{りょうひん}：乾燥{かんそう}して清掃{せいそう}済{ず}み / 不良{ふりょう}：油が付着{ふちゃく}して滑{すべ}りやすい）',
            'Step surface: Free of oil and mud (Good: Clean and dry / Defect: Oily and slippery)',
            'সিঁড়ির ধাপে তল্লাশি: তেল বা কাদা মুক্ত (ভালো: শুকনো ও পরিষ্কার / ত্রুটি: তেলতেলে ও পিচ্ছিল)',
          ),
        },
        {
          x: 72,
          y: 68,
          check: t(
            '踊{おど}り場{ば}の足元{あしもと}：工具{こうぐ}や配線{はいせん}が放置{ほうち}されていないか（良品{りょうひん}：整理{せいり}整頓{せいとん} / 不良{ふりょう}：溶接{ようせつ}ケーブルが散乱{さんらん}）',
            'Landing floor: Free of tools and cables (Good: Neatly organized / Defect: Welding cables strewn about)',
            'ল্যান্ডিংয়ের মেঝে: টুলস ও তার মুক্ত (ভালো: পরিপাটি গোছানো / ত্রুটি: ওয়েল্ডিংয়ের তার ছড়ানো)',
          ),
        },
      ],
    },
    {
      id: 'safety-board-map',
      defective: false,
      name: t(
        '安全{あんぜん}掲示板{けいじばん}の危険{きけん}マップ',
        'Safety notice board hazard map',
        'নিরাপত্তা নোটিশ বোর্ডের বিপদ ম্যাপ',
      ),
      spots: [
        {
          x: 28,
          y: 35,
          check: t(
            '危険{きけん}マップの更新{こうしん}：最新{さいしん}のヒヤリ箇所{かしょ}が周知{しゅうち}されているか（良品{りょうひん}：シールで明示{めいじ} / 不良{ふりょう}：数カ月放置）',
            'Hazard map update: Latest near-miss spots displayed (Good: Clearly marked with stickers / Defect: Left unattended for months)',
            'বিপদ ম্যাপের আপডেট: নতুন বিপদের স্থান প্রদর্শিত (ভালো: স্টিকার দিয়ে চিহ্নিত / ত্রুটি: কয়েক মাস ধরে অবহেলিত)',
          ),
        },
        {
          x: 74,
          y: 65,
          check: t(
            '是正{ぜせい}対策{たいさく}の掲示{けいじ}：改善{かいぜん}後の写真{しゃしん}があるか（良品{りょうひん}：改善{かいぜん}写真を添付{てんぷ} / 不良{ふりょう}：対策の記載なし）',
            'Corrective action display: Improvement photos posted (Good: Photos attached / Defect: No countermeasure written)',
            'প্রতিকারের বিবরণ প্রদর্শন: সংস্কারের ছবি আছে কিনা (ভালো: উন্নতির ছবি সাঁটানো / ত্রুটি: কোনো ব্যবস্থা নেই)',
          ),
        },
      ],
    },
  ],
  quiz: [
    {
      q: t(
        '工場{こうじょう}の通路{つうろ}を歩{ある}いている時{とき}、垂{た}れた油{あぶら}で足{あし}が滑{すべ}りましたが、転倒{てんとう}せずケガもありませんでした。この時{とき}の正{ただ}しい行動{こうどう}はどれですか？',
        'While walking in a factory aisle, you slipped on spilled oil but did not fall and had no injury. What is the correct action to take?',
        'কারখানার পথ দিয়ে হাঁটার সময় মেঝের তেলে পা পিছলে গেল, তবে তুমি পড়ে যাওনি বা আঘাত পাওনি। এই পরিস্থিতিতে সঠিক কাজ কোনটি?',
      ),
      options: [
        t(
          '足{あし}を止{と}めて油{あぶら}を拭{ふ}き取{と}るか危険{きけん}表示{ひょうじ}をし、すぐ職長{しょくちょう}にヒヤリ報告{ほうこく}をする',
          'Stop, wipe up the oil or place a warning sign, and report the near-miss to the foreman immediately',
          'পা থামিয়ে তেল মুছে নাও বা সতর্কচিহ্ন বসাও, এবং সাথে সাথে ফোরম্যানকে আসন্ন বিপদের রিপোর্ট করো',
        ),
        t(
          '誰{だれ}にも見{み}られずケガもなかったので、何{なに}も言{い}わずに自分{じぶん}の作業{さぎょう}へ向{む}かう',
          'Since no one saw you and you were not hurt, say nothing and proceed to your work',
          'কেউ দেখেনি এবং চোটও লাগেনি, তাই কিছু না বলে নিজের কাজে চলে যাও',
        ),
        t(
          '仕事{しごと}が終わる夕方{ゆうがた}の終礼{しゅうれい}まで待{ま}ってから、思{おも}い出{だ}したら話{はな}す',
          'Wait until the end-of-day meeting, and only mention it if you happen to remember',
          'দিনের কাজ শেষের সমাপনী মিটিং পর্যন্ত অপেক্ষা করো এবং মনে পড়লে তবেই বলো',
        ),
        t(
          '次{つぎ}の誰{だれ}かが転{ころ}んでケガをしてから、一緒{いっしょ}に報告{ほうこく}する',
          'Wait until the next person slips and gets injured, then report together',
          'পরে অন্য কেউ পিছলে পড়ে আঘাত পেলে তখন তার সাথে গিয়ে রিপোর্ট করো',
        ),
      ],
      answer: 0,
      why: t(
        'ケガがなくても油{あぶら}を放置{ほうち}すると、次{つぎ}に通{とお}る人{ひと}が大ケガをします。直{ただ}ちに油{あぶら}を拭{ふ}き取{と}り、職長{しょくちょう}に報告{ほうこく}することが重要{じゅうよう}です。',
        'Leaving oil untreated can cause serious injury to the next person passing by. Wiping it immediately and reporting to the foreman prevents accidents.',
        'আঘাত না পেলেও তেল ফেলে রাখলে পরবর্তী পথচারী বড় দুর্ঘটনার শিকার হতে পারে। সাথে সাথে তেল মোছা এবং ফোরম্যানকে জানানো অত্যন্ত জরুরি।',
      ),
    },
    {
      q: t(
        '外国人{がいこくじん}労働者{ろうどうしゃ}がヒヤリ・ハットを経験{けいけん}した時{とき}、「怒{おこ}られたり国{くに}へ帰{かえ}されたりしないか」と不安{ふあん}になりました。正{ただ}しい理解{りかい}はどれですか？',
        'When a foreign worker experienced a near-miss, they worried: "Will I get scolded or sent back home?" What is the correct understanding?',
        'একজন বিদেশি কর্মী আসন্ন বিপদের শিকার হয়ে চিন্তায় পড়ল: "আমাকে কি বকা দেওয়া হবে বা দেশে ফেরত পাঠানো হবে?" সঠিক ধারণা কোনটি?',
      ),
      options: [
        t(
          'ミスを報告{ほうこく}すると評価{ひょうか}が下{さ}がり、ビザが取{と}り消{け}される危険{きけん}がある',
          'Reporting mistakes lowers your evaluation score and risks visa cancellation',
          'ভুল রিপোর্ট করলে মূল্যায়নে নম্বর কাটা যায় এবং ভিসা বাতিলের ঝুঁকি থাকে',
        ),
        t(
          'ヒヤリ報告{ほうこく}は仲間{なかま}を守{まも}る勇気{ゆうき}ある行動{こうどう}として褒{ほ}められ、怒{おこ}られたり帰国{きこく}させられることはない',
          'Near-miss reporting is praised as a brave act to protect coworkers, and you will never be scolded or sent home',
          'হিয়ারি রিপোর্ট সহকর্মীদের রক্ষার সাহসী পদক্ষেপ হিসেবে প্রশংসিত হয়, কখনোই বকা দেওয়া বা দেশে পাঠানো হয় না',
        ),
        t(
          '職長{しょくちょう}から「誰{だれ}が失敗{しっぱい}したか」と問{と}い詰{つ}められた時{とき}だけ白状{はくじょう}する',
          'Only confess when pressed by the foreman about who made the mistake',
          'ফোরম্যান কে ভুল করেছে তা নিয়ে চেপে ধরলে কেবল তখনই স্বীকার করবে',
        ),
        t(
          '自分{じぶん}ではなく同僚{どうりょう}の名前{なまえ}で報告{ほうこく}書{しょ}を提出{ていしゅつ}する',
          'Submit the report under a coworker\'s name instead of your own',
          'নিজের নামের বদলে কোনো সহকর্মীর নামে রিপোর্ট জমা দাও',
        ),
      ],
      answer: 1,
      why: t(
        '日本{にほん}の安全{あんぜん}管理{かんり}では、ヒヤリ・ハットの報告{ほうこく}は職場{しょくば}の安全{あんぜん}を高{たか}める貢献{こうけん}として歓迎{かんげい}され、処分{しょぶん}や帰国{きこく}の対象{たいしょう}には絶対{ぜったい}になりません。',
        'In Japanese safety management, near-miss reporting is welcomed as a contribution to site safety and is never cause for punishment or deportation.',
        'জাপানি নিরাপত্তা ব্যবস্থাপনায় হিয়ারি রিপোর্ট কর্মক্ষেত্রের নিরাপত্তার অবদান হিসেবে সমাদৃত হয়; শাস্তি বা দেশে ফেরত পাঠানোর কারণ কখনোই হয় না।',
      ),
    },
    {
      q: t(
        'ハインリッヒの法則{ほうそく}（1:29:300）において、1件{けん}の重大{じゅうだい}事故{じこ}（死亡{しぼう}・重傷{じゅうしょう}）の背景{はいけい}にあるものは何{なに}ですか？',
        'According to Heinrich\'s Law (1:29:300), what lies behind 1 major accident (fatality or serious injury)?',
        'হাইনরিখের সূত্র (১:২৯:৩০০) অনুসারে, ১টি গুরুতর দুর্ঘটনার (মৃত্যু বা মারাত্মক চোট) পেছনে কী থাকে?',
      ),
      options: [
        t(
          '10件{けん}の軽傷{けいしょう}事故{じこ}と、0件{けん}のヒヤリ・ハット',
          '10 minor injury accidents and 0 near-misses',
          '১০টি সামান্য আঘাতের দুর্ঘটনা এবং ০টি হিয়ারি-হাট্টো',
        ),
        t(
          '100件{けん}の意図的{いとてき}な規則{きそく}違反{いはん}',
          '100 intentional rule violations',
          '১০০টি ইচ্ছাকৃত নিয়ম ভঙ্গ',
        ),
        t(
          '29件{けん}の軽傷{けいしょう}事故{じこ}と、300件{けん}のケガのないヒヤリ・ハット',
          '29 minor injury accidents and 300 non-injury near-misses',
          '২৯টি সামান্য আঘাতের দুর্ঘটনা এবং ৩০০টি চোটহীন হিয়ারি-হাট্টো',
        ),
        t(
          '300件{けん}の機械{きかい}の完全{かんぜん}故障{こしょう}',
          '300 total machine breakdown cases',
          '৩০০টি যন্ত্রপাতির সম্পূর্ণ নষ্ট হওয়া',
        ),
      ],
      answer: 2,
      why: t(
        'ハインリッヒの法則{ほうそく}は、1件{けん}の重大{じゅうだい}事故{じこ}の裏{うら}に29件{けん}の軽傷{けいしょう}、300件{けん}のヒヤリ・ハットがあるという経験則{けいけんそく}です。300の段階{だんかい}で芽{め}を摘{つ}むことが事故{じこ}防止{ぼうし}の要{かなめ}です。',
        'Heinrich\'s empirical law states that behind 1 major disaster lie 29 minor injuries and 300 near-misses. Eliminating the 300 near-misses is key to prevention.',
        'হাইনরিখের বাস্তব নিয়ম হলো ১টি বড় দুর্ঘটনার পেছনে ২৯টি ছোট দুর্ঘটনা এবং ৩০০টি হিয়ারি-হাট্টো থাকে। ৩০০টির পর্যায়ে বিপদ দূর করাই দুর্ঘটনা প্রতিরোধের মূল চাবিকাঠি।',
      ),
    },
    {
      q: t(
        '職長{しょくちょう}にヒヤリ・ハットを「1分間{いっぷんかん}」で報告{ほうこく}する際{さい}、最{もっと}も適切{てきせつ}な伝{つた}え方{かた}はどれですか？',
        'When giving a 1-minute near-miss report to the foreman, which is the most appropriate way to communicate?',
        'ফোরম্যানকে "১ মিনিটে" হিয়ারি-হাট্টো রিপোর্ট করার সময় সবচেয়ে উপযুক্ত পদ্ধতি কোনটি?',
      ),
      options: [
        t(
          '難{むずか}しい日本語{にほんご}の文章{ぶんしょう}を作{つく}り、翌日{よくじつ}になってから書面{しょめん}で渡{わた}す',
          'Compose a difficult Japanese essay and hand it in writing the next day',
          'কঠিন জাপানি ভাষার প্রবন্ধ বানিয়ে পরের দিন লিখিতভাবে জমা দাও',
        ),
        t(
          '自分{じぶん}の行動{こうどう}を隠{かく}して、設備{せつび}だけが悪{わる}いと強調{きょうちょう}して話{はな}す',
          'Hide your own actions and insist that only the equipment was at fault',
          'নিজের কাজ লুকিয়ে রেখে শুধু যন্ত্রপাতির ত্রুটি ছিল বলে দাবি করো',
        ),
        t(
          '言{い}い訳{わけ}と謝罪{しゃざい}を30分間{さんじゅっぷんかん}にわたって繰{く}り返{かえ}す',
          'Repeat excuses and apologies continuously for 30 minutes',
          '৩০ মিনিট ধরে ক্ষমা চাওয়া আর অজুহাত দেওয়া চালিয়ে যাও',
        ),
        t(
          '「いつ・どこで・何{なに}をして・どうヒヤリとしたか」の要点{ようてん}を1分{いっぷん}で簡潔{かんけつ}に話{はな}す',
          'Concisely explain when, where, what you were doing, and what happened in 1 minute',
          '"কখন, কোথায়, কী করতে গিয়ে কীভাবে বিপদ হয়েছিল" এই মূল কথাগুলো সংক্ষেপে ১ মিনিটে বলো',
        ),
      ],
      answer: 3,
      why: t(
        '1分間{いっぷんかん}報告{ほうこく}では、いつ・どこで・誰{だれ}が・何{なに}をして・どうなったかの事実{じじつ}を簡潔{かんけつ}に伝{つた}えることで、職長{しょくちょう}が即座{そくざ}に改善{かいぜん}に動{うご}けます。',
        'In a 1-minute report, stating the concise facts of when, where, who, what, and how allows the foreman to act immediately on improvements.',
        '১ মিনিটের রিপোর্টে কখন, কোথায়, কে, কী করতে গিয়ে কী ঘটেছিল তা সংক্ষেপে বললে ফোরম্যান তাৎক্ষণিকভাবে ব্যবস্থা নিতে পারেন।',
      ),
    },
    {
      q: t(
        '見通{みとお}しの悪{わる}い交差点{こうさてん}で、走{はし}ってきたフォークリフトと衝突{しょうとつ}しそうになりました。直{ただ}ちに取{と}るべき行動{こうどう}はどれですか？',
        'At a blind intersection, you almost collided with a moving forklift. What is the immediate action you should take?',
        'দৃষ্টিহীন একটি বাঁকে দ্রুত আসা ফর্কলিফটের সাথে মুখোমুখি সংঘর্ষ হতে হতে বাঁচলে। অবিলম্বে কী পদক্ষেপ নেওয়া উচিত?',
      ),
      options: [
        t(
          '足{あし}を止{と}めて深呼吸{しんこきゅう}し、カーブミラーの死角{しかく}や一時停止{いちじていし}のヒヤリを職長{しょくちょう}へ報告{ほうこく}する',
          'Stop, take a breath, and report the near-miss regarding mirror blind spots and stopping lines to the foreman',
          'পা থামিয়ে দীর্ঘশ্বাস নাও এবং মিররের অন্ধবিন্দু বা স্টপ লাইনের হিয়ারি বিষয়ে ফোরম্যানকে রিপোর্ট করো',
        ),
        t(
          '危{あぶ}ないのでリフトの運転手{うんてんしゅ}と大声{おおごえ}で口論{こうろん}する',
          'Start a loud argument with the forklift driver because it was dangerous',
          'বিপজ্জনক পরিস্থিতির জন্য ফর্কলিফটের চালকের সাথে চিৎকার করে ঝগড়া শুরু করো',
        ),
        t(
          'ぶつからなかったのだから運{うん}が良{よ}かったと考{かんが}え、そのまま立{た}ち去{さ}る',
          'Think you were just lucky not to get hit, and simply walk away',
          'ধাক্কা লাগেনি বলে ভাগ্য ভালো ভেবে চুপচাপ সেখান থেকে চলে যাও',
        ),
        t(
          '会社{かいしゃ}の誰{だれ}にも言{い}わず、SNSに現場{げんば}の愚痴{ぐち}を書{か}き込{こ}む',
          'Post complaints about the site on social media without telling anyone at work',
          'কাজের কাউকে না জানিয়ে সোশ্যাল মিডিয়ায় কর্মক্ষেত্রের ক্ষোভ প্রকাশ করো',
        ),
      ],
      answer: 0,
      why: t(
        '交差点{こうさてん}のヒヤリは、ミラーの角度{かくど}調整{ちょうせい}や路面{ろめん}の停止線{ていしせん}引き直{なお}しなど、設備{せつび}やルールの改善{かいぜん}で再発{さいはつ}を防{ふせ}ぐ絶好{ぜっこう}の機会{きかい}です。',
        'Intersection near-misses provide an essential opportunity to adjust mirror angles or repaint stop lines to prevent recurrence.',
        'মোড়ের আসন্ন বিপদ মিররের কোণ ঠিক করা বা স্টপ লাইন নতুন করে আঁকার মতো ব্যবস্থা নিয়ে পুনরাবৃত্তি রোধের দারুণ সুযোগ তৈরি করে।',
      ),
    },
    {
      q: t(
        '日本{にほん}の製造業{せいぞうぎょう}や造船{ぞうせん}所{じょ}における「ヒヤリ・ハット活動{かつどう}」の基本的{きほんてき}な考{かんが}え方はどれですか？',
        'What is the fundamental philosophy of "Hiyari-Hatto activities" in Japanese manufacturing and shipyards?',
        'জাপানের কারখানা ও জাহাজ নির্মাণ শিল্পে "হিয়ারি-হাট্টো কার্যক্রমের" মূল দর্শন কোনটি?',
      ),
      options: [
        t(
          '優秀{ゆうしゅう}な人{ひと}は決{けっ}してヒヤリ・ハットを起{お}こさず、報告{ほうこく}するのは能力{のうりょく}が低{ひく}い人{ひと}だけである',
          'Competent workers never experience near-misses, and only low-skilled workers submit reports',
          'দক্ষ কর্মীরা কখনোই আসন্ন বিপদে পড়ে না, কেবল অদক্ষ কর্মীরাই রিপোর্ট জমা দেয়',
        ),
        t(
          '人間{にんげん}は誰{だれ}でもミスをするものであり、環境{かんきょう}やルールを改善{かいぜん}して事故{じこ}を防{ふせ}ぐ',
          'Humans inevitably make mistakes; improving environments and procedures prevents accidents',
          'মানুষ মাত্রই ভুল হতে পারে; কর্মক্ষেত্রের পরিবেশ ও নিয়ম উন্নত করেই দুর্ঘটনা ঠেকানো হয়',
        ),
        t(
          'ヒヤリ報告{ほうこく}を出{だ}した人{ひと}は、罰{ばつ}として1週間{いっしゅうかん}トイレ掃除{そうじ}をする',
          'The person who submits a near-miss report must clean the toilets for a week as punishment',
          'যে ব্যক্তি হিয়ারি রিপোর্ট দেবে তাকে শাস্তি হিসেবে এক সপ্তাহ বাথরুম পরিষ্কার করতে হবে',
        ),
        t(
          '年間{ねんかん}の報告{ほうこく}件数{けんすう}をゼロにすることが安全{あんぜん}管理{かんり}の最終{さいしゅう}目標{もくひょう}である',
          'The ultimate safety goal is reducing annual reported cases to zero'
          ,
          'বছরে জমা পড়া রিপোর্টের সংখ্যা শূন্যে নামিয়ে আনাই নিরাপত্তা ব্যবস্থাপনার মূল লক্ষ্য',
        ),
      ],
      answer: 1,
      why: t(
        '「人は誰{だれ}でも間違{まちが}える」という前提{ぜんてい}に立{た}ち、個人の責任{せきにん}ではなく設備{せつび}や作業{さぎょう}手順{てじゅん}の不備{ふび}を見直{みなお}すのがヒヤリ・ハットの本質{ほんしつ}です。',
        'Based on the premise that anyone can make mistakes, the essence of Hiyari-Hatto is reviewing site conditions and procedures rather than blaming individuals.',
        '"মানুষ মাত্রই ভুল করতে পারে" এই ভিত্তির ওপর দাঁড়িয়ে ব্যক্তির দোষ না খুঁজে পরিবেশ ও কাজের নিয়মের ত্রুটি দূর করাই হিয়ারি-হাট্টোর মূল কথা।',
      ),
    },
    {
      q: t(
        '同僚{どうりょう}の後輩{こうはい}が「日本語{にほんご}が下手{へた}で職長{しょくちょう}にヒヤリを上手{うまく}説明{せつめい}できない」と悩{なや}んでいます。どう助言{じょげん}すべきですか？',
        'A junior coworker is worried: "My Japanese is poor, so I cannot explain my near-miss well to the foreman." How should you advise them?',
        'এক কনিষ্ঠ সহকর্মী চিন্তিত: "আমার জাপানি ভাষা দুর্বল, তাই ফোরম্যানকে হিয়ারি বুঝিয়ে বলতে পারছি না।" তাকে কী পরামর্শ দেওয়া উচিত?',
      ),
      options: [
        t(
          '日本語{にほんご}検定{けんてい}に合格{ごうかく}するまで報告{ほうこく}は諦{あきら}めるように言{い}う',
          'Tell them to give up reporting until they pass the Japanese proficiency test',
          'জাপানি ভাষার পরীক্ষায় পাস না করা পর্যন্ত রিপোর্ট করা বাদ দিতে বলো',
        ),
        t(
          '言葉{ことば}が通{つう}じないのだから、報告{ほうこく}しなくても仕方{しかた}がないと答{こた}える',
          'Reply that nothing can be done and reporting is not necessary due to language barriers',
          'ভাষা বোঝানো যায় না বলে রিপোর্ট না করলেও চলবে বলে আশ্বস্ত করো',
        ),
        t(
          'スマートフォンの写真{しゃしん}や指差{ゆびさ}し、単語{たんご}で十分{じゅうぶん}伝{つた}わるので、一緒{いっしょ}に職長{しょくちょう}の所{ところ}へ行{い}こうと励{はげ}ます',
          'Encourage them that smartphone photos, pointing, and simple words are plenty, and offer to go together',
          'স্মার্টফোনের ছবি, আঙুল দিয়ে দেখানো আর সাধারণ শব্দই যথেষ্ট বলে সাহস দাও এবং সাথে ফোরম্যানের কাছে চলো',
        ),
        t(
          '誰{だれ}にも分{わ}からないように、匿名{とくめい}の手紙{てがみ}を更衣室{こういしつ}に置{お}いておくよう勧{すす}める',
          'Recommend leaving an anonymous note in the locker room so no one knows who wrote it',
          'কেউ যাতে না জানে সেজন্য লকার রুমে একটি বেনামী চিঠি রেখে দিতে বলো',
        ),
      ],
      answer: 2,
      why: t(
        '安全{あんぜん}報告{ほうこく}に完璧{かんぺき}な日本語{にほんご}は不要{ふよう}です。写真{しゃしん}や現場{げんば}での指差{ゆびさ}し、簡単{かんたん}な単語{たんご}で危険{きけん}の事実{じじつ}を共有{きょうゆう}することが最優先{さいゆうせん}です。',
        'Flawless Japanese is not required for safety reporting. Conveying the hazard fact via photos, pointing at the scene, or simple keywords takes top priority.',
        'নিরাপত্তা রিপোর্টে নিখুঁত জাপানি ভাষা জরুরি নয়। ছবি, অকুস্থলে আঙুল দিয়ে নির্দেশ বা সহজ শব্দ ব্যবহার করে বিপদের তথ্য জানানোই প্রধান কাজ।',
      ),
    },
    {
      q: t(
        '次{つぎ}のうち、ヒヤリ・ハットの引{ひ}き金{がね}となる典型{てんけい}的な「不安全{ふあんぜん}状態{じょうたい}」はどれですか？',
        'Which of the following is a typical "unsafe condition" that triggers near-misses?',
        'নিচের কোনটি আসন্ন বিপদের সূত্রপাত ঘটায় এমন একটি পরিচিত "অনিরাপদ অবস্থা"?',
      ),
      options: [
        t(
          'ヘルメットのあごひもを指{ゆび}1本{ぽん}の隙間{すきま}で確実{かくじつ}に締{し}めている状態{じょうたい}',
          'Safety helmet chin strap firmly fastened with one finger gap',
          'হেলমেটের চিনস্ট্র্যাপ এক আঙুল ফাঁক রেখে শক্তভাবে বাঁধা অবস্থা',
        ),
        t(
          '定期的{ていきてき}に点検{てんけん}され、合格{ごうかく}タグが付{つ}いたワイヤーロープ',
          'Wire rope regularly inspected and labeled with an approval tag',
          'নিয়মিত পরীক্ষা করা এবং পাসের ট্যাগ লাগানো ওয়্যার রোপ',
        ),
        t(
          '歩行者{ほこうしゃ}用{よう}通路{つうろ}の白線{はくせん}が鮮明{せんめい}に引{ひ}かれている状態{じょうたい}',
          'Pedestrian walkway clearly outlined with bright white lines',
          'হাঁটার পথের সাদা দাগ স্পষ্টভাবে চিহ্নিত থাকা অবস্থা',
        ),
        t(
          '暗{くら}い通路{つうろ}の床{ゆか}に、保護{ほご}カバーなしで延長{えんちょう}コードが横切{よこぎ}っている状態{じょうたい}',
          'Extension cord crossing a dark hallway floor without a protective ramp',
          'অন্ধকার হাঁটার পথের মেঝের ওপর সুরক্ষা কভার ছাড়াই এক্সটেনশন তার ছড়ানো থাকা অবস্থা',
        ),
      ],
      answer: 3,
      why: t(
        '保護{ほご}カバーのない電気{でんき}コードは足{あし}を引{ひ}っ掛{か}けて転倒{てんとう}する「不安全{ふあんぜん}状態{じょうたい}」であり、ヒヤリやつまずきの主{おも}な原因{げんいん}になります。',
        'Uncovered electrical cords create a tripping hazard (unsafe condition) that leads to near-miss stumbles or severe falls.',
        'কভারহীন বৈদ্যুতিক তার হোঁচট খাওয়ার মতো "অনিরাপদ অবস্থা" সৃষ্টি করে, যা হিয়ারি বা পড়ে যাওয়ার প্রধান কারণ।',
      ),
    },
    {
      q: t(
        '日本{にほん}の現場{げんば}で大切{たいせつ}にされる「報連相{ほうれんそう}（報告{ほうこく}・連絡{れんらく}・相談{そうだん}）」とヒヤリ・ハットの関係{かんけい}として、正{ただ}しいものはどれですか？',
        'What is the correct understanding of the relationship between Ho-Ren-So (Report, Communicate, Consult) and near-misses in Japan?',
        'জাপানি কর্মক্ষেত্রে মান্য করা "হো-রেন-সো (রিপোর্ট, যোগাযোগ, পরামর্শ)" এবং হিয়ারি-হাট্টোর মধ্যকার সম্পর্ক হিসেবে কোনটি সঠিক?',
      ),
      options: [
        t(
          'ヒヤリとした事実{じじつ}を「報告{ほうこく}」し、改善{かいぜん}策{さく}を職長{しょくちょう}に「相談{そうだん}」することは報連相{ほうれんそう}の重要{じゅうよう}な実践{じっせん}である',
          'Reporting near-miss facts and consulting the foreman on countermeasures is a vital practice of Ho-Ren-So',
          'বিপদের তথ্য "রিপোর্ট" করা এবং প্রতিকার নিয়ে ফোরম্যানের সাথে "পরামর্শ" করা হো-রেন-সোর গুরুত্বপূর্ণ বাস্তবায়ন',
        ),
        t(
          '報連相{ほうれんそう}は事務{じむ}仕事{しごと}だけのルールであり、現場{げんば}の危険{きけん}とは関係{かんけい}がない',
          'Ho-Ren-So is only for office paperwork and has nothing to do with site hazards',
          'হো-রেন-সো কেবল অফিসের কাজের নিয়ম, সাইটের বিপদের সাথে এর সম্পর্ক নেই',
        ),
        t(
          '自分{じぶん}の仕事{しごと}が予定{よてい}通{とお}り進{すす}んでいる時{とき}だけ報連相{ほうれんそう}を行{おこな}えばよい',
          'You only need to practice Ho-Ren-So when your work is proceeding exactly on schedule',
          'নিজের কাজ শিডিউল মতো এগোলেই কেবল হো-রেন-সো করলেই চলে',
        ),
        t(
          'ヒヤリ・ハットは個人的{こじんてき}な失敗{しっぱい}なので、相談{そうだん}せず一人{ひとり}で抱{かか}え込{こ}むべきである',
          'A near-miss is a private blunder, so you should keep it to yourself without consulting anyone',
          'হিয়ারি-হাট্টো ব্যক্তিগত ব্যর্থতা, তাই পরামর্শ না করে নিজের ভেতরে চেপে রাখা উচিত',
        ),
      ],
      answer: 0,
      why: t(
        'ヒヤリ・ハットは事実{じじつ}を迅速{じんそく}に「報告{ほうこく}」し、対策{たいさく}を「相談{そうだん}」して全員{ぜんいん}に「連絡{れんらく}」する報連相{ほうれんそう}の代表例{だいひょうれい}です。',
        'Near-miss reporting is a prime example of Ho-Ren-So: swiftly reporting facts, consulting on countermeasures, and communicating with the whole team.',
        'হিয়ারি-হাট্টো হলো হো-রেন-সোর অন্যতম প্রধান উদাহরণ: দ্রুত তথ্য রিপোর্ট করা, প্রতিকার নিয়ে পরামর্শ করা এবং সবার সাথে যোগাযোগ করা।',
      ),
    },
    {
      q: t(
        '頭上{ずじょう}でクレーンが動{うご}いている時{とき}、落{お}としたペンを拾{ひろ}うため立入禁止{たちいりきんし}エリアに入{はい}り、吊{つ}り荷{に}がかすめました。どうすべきですか？',
        'While an overhead crane moved, you entered a restricted zone to pick up a dropped pen, and the load brushed close. What should you do?',
        'মাথার ওপর ক্রেন চলার সময় পড়ে যাওয়া কলম তুলতে নিষিদ্ধ এলাকায় ঢুকলে এবং ঝুলন্ত মাল ঘেঁষে গেল। তোমার কী করা উচিত?',
      ),
      options: [
        t(
          '怒{おこ}られるのが嫌{いや}なので、立入禁止{たちいりきんし}エリアに入{はい}った事実{じじつ}を隠{かく}す',
          'Hide the fact that you entered the restricted zone because you dislike being scolded',
          'বকা খাওয়ার ভয়ে নিষিদ্ধ এলাকায় ঢোকার সত্য গোপন রাখো',
        ),
        t(
          'すぐ職長{しょくちょう}に報告{ほうこく}し、進入{しんにゅう}防止{ぼうし}ロープの設置{せっち}や注意{ちゅうい}喚起{かんき}などの対策{たいさく}を一緒{いっしょ}に行{おこな}う',
          'Immediately report to the foreman and work together on fixes like installing barrier ropes or warning signs',
          'সাথে সাথে ফোরম্যানকে জানাও এবং ব্যারিকেড রশি টাঙানো ও সতর্কবার্তার মতো প্রতিকারে সহায়তা করো',
        ),
        t(
          'ペンが無事{ぶじ}に拾{ひろ}えたので、何{なに}も問題{もんだい}ないと納得{なっとく}する',
          'Be satisfied that nothing went wrong since the pen was safely retrieved',
          'কলমটা অক্ষত কুড়িয়ে পাওয়া গেছে তাই কোনো সমস্যা নেই ভেবে সন্তুষ্ট থাকো',
        ),
        t(
          'クレーンの合図者{あいずしゃ}が自分{じぶん}を見{み}ていなかったのが悪{わる}いと相手{あいて}を責{せ}める',
          'Blame the crane signalman for not looking out for you',
          'ক্রেনের সিগন্যালম্যান খেয়াল করেনি বলে তাকে দোষী সাব্যস্ত করো',
        ),
      ],
      answer: 1,
      why: t(
        '禁止{きんし}区域{くいき}に入{はい}ってしまった理由{りゆう}を正直{しょうじき}に報告{ほうこく}することで、ロープの強化{きょうか}や作業{さぎょう}手順{てじゅん}の改善{かいぜん}につながり、仲間{なかま}の命{いのち}を守{まも}れます。',
        'Honest reporting about entering a restricted zone leads to stronger barrier ropes and improved procedures that protect everyone\'s lives.',
        'নিষিদ্ধ জায়গায় প্রবেশের কারণ সততার সাথে রিপোর্ট করলে ব্যারিকেড রশি জোরদার এবং কাজের নিয়ম সংশোধন সম্ভব হয়, যা সহকর্মীদের জীবন রক্ষা করে।',
      ),
    },
    {
      q: t(
        '提出{ていしゅつ}されたヒヤリ・ハット報告書{ほうこくしょ}は、現場{げんば}でどのように活用{かつよう}されるのが正{ただ}しいですか？',
        'How should submitted near-miss reports properly be utilized on the worksite?',
        'জমা দেওয়া হিয়ারি-হাট্টো রিপোর্ট কর্মক্ষেত্রে কীভাবে সঠিকভাবে কাজে লাগানো উচিত?',
      ),
      options: [
        t(
          '事務室{じむしつ}の金庫{きんこ}に鍵{かぎ}をかけて保管{ほかん}し、誰{だれ}にも見{み}せない',
          'Lock it away in an office safe and never let anyone see it',
          'অফিসের লকারে তালা দিয়ে রেখে দাও যাতে কেউ দেখতে না পায়',
        ),
        t(
          '朝礼{ちょうれい}で報告{ほうこく}者{しゃ}の名前{なまえ}を呼{よ}び出{だ}し、みんなの前{まえ}で反省{はんせい}させる',
          'Call out the reporter\'s name at the morning meeting and make them apologize in front of everyone',
          'সকালের সমাবেশে রিপোর্টকারীর নাম ডেকে সবার সামনে দাঁড়িয়ে ক্ষমা চাইতে বাধ্য করো',
        ),
        t(
          '朝{あさ}のKYミーティングで全員{ぜんいん}で情報{じょうほう}共有{きょうゆう}し、設備{せつび}の修繕{しゅうぜん}や手順{てじゅん}の改善{かいぜん}に役立{やくだ}てる',
          'Share information in the morning KY meeting and use it to repair equipment and improve procedures',
          'সকালের কেওয়াই মিটিংয়ে সবার সাথে শেয়ার করো এবং যন্ত্রপাতি মেরামত ও কাজের নিয়ম সংশোধনে কাজে লাগাও',
        ),
        t(
          '1年分{いちねんぶん}溜{た}めてから、読{よ}まずにそのまま廃棄{はいき}処分{しょぶん}する',
          'Accumulate them for a year and then discard them without reading',
          'এক বছর জমা করে রেখে না পড়েই ময়লার ঝুড়িতে ফেলে দাও',
        ),
      ],
      answer: 2,
      why: t(
        'ヒヤリ報告{ほうこく}は職場{しょくば}全員{ぜんいん}の財産{ざいさん}です。朝礼{ちょうれい}やKY活動{かつどう}で危険{きけん}を共有{きょうゆう}し、具体的{ぐたいてき}な改善{かいぜん}に生{い}かしてこそ意味{いみ}があります。',
        'Near-miss reports are valuable assets for the whole team. Sharing hazards during morning KY meetings and turning them into concrete fixes gives them true value.',
        'হিয়ারি রিপোর্ট পুরো কর্মক্ষেত্রের সম্পদ। সকালের কেওয়াই মিটিংয়ে বিপদ শেয়ার করে বাস্তব সংস্কারে রূপ দিলেই এর সার্থকতা।',
      ),
    },
    {
      q: t(
        '造船{ぞうせん}所{じょ}や建設{けんせつ}現場{げんば}でヒヤリ・ハットを「隠{かく}す（黙{だま}っている）」ことの最大{さいだい}の危険{きけん}は何{なに}ですか？',
        'What is the greatest danger of "hiding" or keeping silent about a near-miss in a shipyard or construction site?',
        'জাহাজ নির্মাণ বা নির্মাণ সাইটে হিয়ারি-হাট্টো "গোপন রাখা (চুপ থাকা)"-র সবচেয়ে বড় বিপদ কোনটি?',
      ),
      options: [
        t(
          '報告{ほうこく}用紙{ようし}の紙{かみ}が節約{せつやく}できてしまうこと',
          'Saving paper by not using up report forms',
          'রিপোর্ট ফরম খরচ না হয়ে কাগজ বেঁচে যাওয়া',
        ),
        t(
          '職長{しょくちょう}の仕事{しごと}が減{へ}り、退屈{たいくつ}になること',
          'The foreman has less work and becomes bored',
          'ফোরম্যানের কাজের চাপ কমে গিয়ে একঘেয়ে লাগা',
        ),
        t(
          '安全{あんぜん}会議{かいぎ}が早{はや}く終{お}わってしまうこと',
          'Safety meetings ending earlier than scheduled',
          'নিরাপত্তা সংক্রান্ত সভা সময়ের আগেই শেষ হয়ে যাওয়া',
        ),
        t(
          '危険{きけん}な状態{じょうたい}がそのまま放置{ほうち}され、次{つぎ}に自分{じぶん}や仲間{なかま}が命{いのち}にかかわる重大{じゅうだい}事故{じこ}に遭{あ}うこと',
          'The hazard remains uncorrected, leading to a fatal catastrophe for yourself or teammates next time',
          'বিপজ্জনক অবস্থা অপরিবর্তিত থাকে এবং পরবর্তী সময়ে নিজের বা সহকর্মীর প্রাণঘাতী বড় দুর্ঘটনা ঘটে',
        ),
      ],
      answer: 3,
      why: t(
        'ヒヤリを隠{かく}すと現場{げんば}の危険{きけん}が放置{ほうち}され、ハインリッヒの法則{ほうそく}の通{とお}り、いずれ誰{だれ}かが大ケガや死亡{しぼう}事故{じこ}に遭{あ}う最悪{さいあく}の結果{けっか}を招{まね}きます。',
        'Hiding a near-miss leaves the hazard active, inevitably resulting in severe injury or fatality for someone in line with Heinrich\'s Law.',
        'বিপদ গোপন রাখলে কর্মক্ষেত্রে ঝুঁকি অমীমাংসিত থেকে যায়, যার ফলে হাইনরিখের সূত্রানুযায়ী একসময় কারো প্রাণঘাতী বা গুরুতর দুর্ঘটনা ঘটে।',
      ),
    },
  ],
  glossary: [
    {
      ja: 'ヒヤリ・ハット',
      reading: 'ひやりはっと',
      en: 'Near-miss incident (close call)',
      bn: 'হিয়ারি-হাট্টো (অল্পের জন্য রক্ষা / আসন্ন বিপদ)',
      note: t(
        '突発的な事象で、ケガや物損には至らなかったが、一歩間違えれば重大な事故になった危険な出来事。',
        'An unexpected event that caused no injury or damage, but could have resulted in a serious accident.',
        'এমন কোনো অনাকাঙ্ক্ষিত ঘটনা যাতে আঘাত বা ক্ষতি হয়নি, কিন্তু সামান্য এদিক-সেদিক হলেই মারাত্মক দুর্ঘটনা ঘটত।',
      ),
    },
    {
      ja: 'ハインリッヒの法則',
      reading: 'はいんりっひのほうそく',
      en: 'Heinrich\'s Law (1:29:300 ratio)',
      bn: 'হাইনরিখের সূত্র (১:২৯:৩০০ অনুপাত)',
      note: t(
        '1件の重大事故の背後に29件の軽傷事故、300件の無傷害ヒヤリ・ハットがあるという労働災害の経験則。',
        'An empirical safety rule stating that behind 1 major accident lie 29 minor injuries and 300 non-injury near-misses.',
        'কর্মক্ষেত্রের বাস্তব নিয়ম: ১টি বড় দুর্ঘটনার পেছনে ২৯টি ছোট দুর্ঘটনা এবং ৩০০টি চোটহীন আসন্ন বিপদ লুকিয়ে থাকে।',
      ),
    },
    {
      ja: '不安全状態',
      reading: 'ふあんぜんじょうたい',
      en: 'Unsafe condition',
      bn: 'অনিরাপদ অবস্থা',
      note: t(
        '床の油、手すりの破損、防護カバーの欠如など、設備や環境に潜む物理的な危険。',
        'Physical hazards in equipment or environment, such as oily floors, broken handrails, or missing machine guards.',
        'মেঝেতে তেল, ভাঙা হ্যান্ডরেল বা সুরক্ষা কভার না থাকার মতো যন্ত্রপাতি বা পরিবেশের বাহ্যিক বিপদ।',
      ),
    },
    {
      ja: '不安全行動',
      reading: 'ふあんぜんこうどう',
      en: 'Unsafe behavior / action',
      bn: 'অনিরাপদ আচরণ',
      note: t(
        '安全手順の省略、保護具の不着用、近道通行など、作業者が行う危険な行動。',
        'Hazardous actions by workers, such as skipping safety steps, not wearing PPE, or taking unauthorized shortcuts.',
        'নিরাপত্তার নিয়ম না মানা, পিপিই না পরা বা নিষিদ্ধ শর্টকাট ব্যবহারের মতো কর্মীর ঝুঁকিপূর্ণ আচরণ।',
      ),
    },
    {
      ja: '報連相',
      reading: 'ほうれんそう',
      en: 'Ho-Ren-So (Report, Communicate, Consult)',
      bn: 'হো-রেন-সো (রিপোর্ট, যোগাযোগ, পরামর্শ)',
      note: t(
        '報告（事実を伝える）・連絡（情報を共有する）・相談（判断を仰ぐ）の頭文字をとった日本職場の基本。',
        'Core Japanese workplace communication habit: Hokoku (Report facts), Renraku (Share info), Sodan (Consult).',
        'জাপানি কর্মক্ষেত্রের যোগাযোগের মূল ভিত্তি: হোকোকু (রিপোর্ট), রেনরাকু (যোগাযোগ) ও সোদান (পরামর্শ)।',
      ),
    },
    {
      ja: '職長',
      reading: 'しょくちょう',
      en: 'Foreman / front-line supervisor',
      bn: 'ফোরম্যান / ফিল্ড সুপারভাইজার',
      note: t(
        '作業現場で労働者を直接指導・指揮し、安全衛生を管理するリーダー。',
        'Site leader who directly guides workers and oversees occupational safety and health.',
        'কাজের জায়গায় কর্মীদের সরাসরি পরিচালনা এবং নিরাপত্তা তদারককারী নেতা।',
      ),
    },
    {
      ja: '再発防止策',
      reading: 'さいはつぼうしさく',
      en: 'Recurrence prevention measure',
      bn: 'পুনরাবৃত্তি রোধমূলক ব্যবস্থা',
      note: t(
        '同じ危険や事故が二度と起きないよう、設備・ルール・教育を恒久的に改善すること。',
        'Permanently improving equipment, rules, or training so the same hazard never happens again.',
        'একই বিপদ যাতে দ্বিতীয়বার না ঘটে সেজন্য যন্ত্রপাতি, নিয়ম বা প্রশিক্ষণের স্থায়ী সংস্কার।',
      ),
    },
    {
      ja: '応急処置',
      reading: 'おうきゅうしょち',
      en: 'Immediate / emergency countermeasure',
      bn: 'জরুরি / তাৎক্ষণিক ব্যবস্থা',
      note: t(
        '危険を発見した直後に、油を拭き取ったりコーンを置くなどその場で行う緊急の安全確保。',
        'Emergency safety measures taken on the spot immediately upon finding a hazard, like wiping oil or placing a cone.',
        'বিপদ দেখামাত্র ঘটনাস্থলে তাৎক্ষণিকভাবে তেল মোছা বা কোন বসানোর মতো জরুরি নিরাপত্তা নিশ্চিতকরণ।',
      ),
    },
    {
      ja: '是正処置',
      reading: 'ぜせいしょち',
      en: 'Corrective action',
      bn: 'সংশোধনমূলক ব্যবস্থা',
      note: t(
        '不適合や欠陥の原因を取り除き、再発を防ぐために実施する根本的な対策。',
        'Fundamental measures implemented to eliminate root causes of non-conformities and prevent recurrence.',
        'সমস্যা বা ত্রুটির মূল কারণ দূর করে পুনরাবৃত্তি রোধের জন্য নেওয়া স্থায়ী প্রতিকারমূলক ব্যবস্থা।',
      ),
    },
    {
      ja: '死角',
      reading: 'しかく',
      en: 'Blind spot',
      bn: 'অন্ধবিন্দু / দৃষ্টির আড়ালের জায়গা',
      note: t(
        '構造物や機械の影になり、目視やミラーで確認できない危険な見通しの悪い範囲。',
        'Dangerous obstructed areas hidden behind structures or machines that cannot be seen directly or via mirrors.',
        'যন্ত্রপাতি বা কাঠামোর আড়ালে থাকা বিপজ্জনক জায়গা যা সরাসরি বা আয়না দিয়ে দেখা যায় না।',
      ),
    },
    {
      ja: '段差',
      reading: 'だんさ',
      en: 'Step / difference in floor level',
      bn: 'উঁচুনীচু ধাপ / মেঝের অসমতা',
      note: t(
        '床や通路のつまずきや転倒の原因となる高低差。',
        'Difference in height on floors or walkways that can cause tripping or stumbling.',
        'মেঝে বা হাঁটার পথের অসম উচ্চতা যা হোঁচট খাওয়া বা পড়ে যাওয়ার কারণ ঘটায়।',
      ),
    },
    {
      ja: '落下防止',
      reading: 'らっかぼうし',
      en: 'Fall and drop prevention',
      bn: 'পড়ে যাওয়া রোধ',
      note: t(
        '高所からの工具や資材の落下、および作業者の墜落を防ぐためのロープや安全ネットなどの措置。',
        'Measures like lanyards and safety nets to prevent tools, materials, or workers from falling from heights.',
        'উঁচু থেকে জিনিসপত্র বা কর্মী নিচে পড়ে যাওয়া রোধ করতে ব্যবহৃত রশি, সেফটি নেট ইত্যাদি ব্যবস্থা।',
      ),
    },
  ],
}
