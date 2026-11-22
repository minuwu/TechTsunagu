import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string, bn: string): Tri => ({ ja, en, bn })

export const forklift: CourseData = {
  id: 'forklift',
  category: 'trade',
  level: 2,
  minutes: 20,
  title: t(
    'フォークリフト運転{うんてん}安全{あんぜん}',
    'Forklift operation safety',
    'ফর্কলিফ্ট চালনার নিরাপত্তা',
  ),
  sub: t(
    '正{ただ}しい資格{しかく}と始業前{しぎょうまえ}点検{てんけん}、走行時{そうこうじ}のルールを守{まも}り、人{ひと}との接触{せっしょく}と転倒{てんとう}を防{ふせ}ぐ。',
    'Prevent pedestrian collisions and tip-overs with proper certification, pre-shift inspections, and safe travel rules.',
    'সঠিক সনদ, কাজ শুরুর আগের পরিদর্শন এবং চলার নিয়ম মেনে মানুষ ধাক্কা ও ফর্কলিফ্ট উল্টে যাওয়া প্রতিরোধ করো।',
  ),
  refs: [
    '労働安全衛生法 第61条（就業制限：フォークリフト運転業務）',
    '労働安全衛生規則 第36条第5号（特別教育：最大積載荷重1トン未満のフォークリフト）',
    '労働安全衛生規則 第151条（車両系荷役運搬機械等：フォークリフト）',
    '厚生労働省 フォークリフト作業安全ガイドライン',
  ],
  panels: [
    {
      who: 'foreman',
      text: t(
        '今日{きょう}は倉庫{そうこ}でパレット積{づ}みの荷物{にもつ}をフォークリフトで運搬{うんぱん}します。',
        'Today we will transport palletized cargo in the warehouse using a forklift.',
        'আজ আমরা গুদামে ফর্কলিফ্ট দিয়ে প্যালেটে রাখা মালামাল পরিবহন করব।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '荷物{にもつ}を積{つ}みました！前{まえ}が見{み}えにくいので、フォークを高{たか}く上{あ}げて前進{ぜんしん}してみます！',
        'I loaded the cargo! Since forward visibility is poor, I will raise the forks high and drive forward!',
        'মাল লোড করেছি! সামনে দেখতে সমস্যা হচ্ছে, তাই কাঁটা উঁচুতে তুলে সামনে এগিয়ে দেখি!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '待{ま}ちなさい！荷{に}を高{たか}く上{あ}げたまま走行{そうこう}してはいけません。重心{じゅうしん}が高{たか}くなり、わずかな段差{だんさ}や旋回{せんかい}で簡単{かんたん}に転倒{てんとう}します！',
        'Wait! Never drive with a raised load. The high center of gravity makes it tip over easily on small bumps or turns!',
        'থামো! মাল উঁচুতে তুলে রাখা অবস্থায় কখনো চালাবে না। ভরকেন্দ্র উঁচুতে ওঠায় সামান্য উঁচু-নিচু বা বাঁক নিলেই সহজে উল্টে যাবে!',
      ),
    },
    {
      who: 'worker',
      text: t(
        'すみません！荷物{にもつ}で前{まえ}が遮{さえぎ}られて見{み}えなかったのです。前{まえ}が見{み}えない時{とき}はどうしますか？',
        'I am sorry! The load was blocking my forward view. What should I do when I cannot see ahead?',
        'মাফ করবেন! মাল সামনে আড়াল করায় দেখতে পাচ্ছিলাম না। সামনে দেখতে না পেলে কী করতে হবে?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '前{まえ}が見{み}えない時{とき}は「後進{こうしん}（バック）走行{そうこう}」が鉄則{てっそく}です！後方{こうほう}を目視{もくし}で確認{かくにん}して進{すす}むか、誘導者{ゆうどうしゃ}を配置{はいち}します。',
        'When your forward view is blocked, driving in reverse is the absolute rule! Check behind by direct sight, or assign a spotter.',
        'সামনে দেখতে না পেলে "রিভার্স বা পেছনে চালিয়ে যাওয়া" লোহার মতো নিয়ম! সরাসরি পেছনে তাকিয়ে দেখে চালাও, অথবা একজন সংকেতকারী রাখো।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'なるほど！フォークを地上{ちじょう}20cmまで下{さ}げ、マストを後方{こうほう}に傾{かたむ}けました。これなら安定{あんてい}しますね！', // verify: 走行時のフォーク高さは地上20〜30cm（マスト後傾）
        'I see! I lowered the forks to 20 cm off the ground and tilted the mast back. This keeps it stable!',
        'বুঝতে পেরেছি! ফর্ক মেঝে থেকে ২০ সেমি ওপরে নামিয়ে এনে মাস্ট পেছনে হেলিয়ে দিলাম। এতে ফর্কলিফ্ট স্থির থাকবে!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '交差点{こうさてん}や歩行者{ほこうしゃ}通路{つうろ}の手前{てまえ}では必{かなら}ず一時{いちじ}停止{ていし}し、警音器{けいおんき}を鳴{な}らして左右{さゆう}の安全{あんぜん}を確認{かくにん}してください。',
        'Always stop at intersections and before pedestrian walkways, sound the horn, and check left and right for safety.',
        'মোড়ে এবং পথচারীদের চলার পথের আগে অবশ্যই এক মুহূর্ত থামো, হর্ন বাজাও এবং ডানে-বামে নিরাপত্তা যাচাই করো।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '交差点{こうさてん}で一旦{いったん}停止{ていし}、クラクション鳴動{めいどう}、歩行者{ほこうしゃ}なし、左右{さゆう}よし！後退{こうたい}で安全{あんぜん}に通過{つうか}します！',
        'Temporary stop at intersection, sound horn, no pedestrians, left and right clear! Safely passing in reverse!',
        'মোড়ে সাময়িক থামা, হর্ন বাজালাম, কোনো পথচারী নেই, ডানে-বামে ঠিক আছে! নিরাপদে রিভার্সে পার হচ্ছি!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '完璧{かんぺき}です！駐車{ちゅうしゃ}する時{とき}はフォークを地面{じめん}に下{お}ろし、マストを前傾{ぜんけい}させ、キーを抜{ぬ}いてブレーキを引{ひ}くことを忘{わす}れずに。',
        'Perfect! When parking, remember to lower the forks to the ground, tilt mast forward, remove the key, and set the brake.',
        'চমৎকার! পার্কিং করার সময় কাঁটা মাটিতে নামানো, মাস্ট সামনে হেলানো, চাবি বের করা এবং ব্রেক টানার কথা ভুলবে না।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'はい！始業前{しぎょうまえ}点検{てんけん}と安全{あんぜん}運転{うんてん}を徹底{てってい}し、無事故{むじこ}で作業{さぎょう}を続{つづ}けます！',
        'Yes! I will strictly follow pre-shift inspections and safe driving to continue working with zero accidents!',
        'জি! কাজ শুরুর আগের পরিদর্শন ও নিরাপদ ড্রাইভিং পুরোপুরি মেনে চলে দুর্ঘটনা ছাড়া কাজ চালিয়ে যাব!',
      ),
    },
  ],
  handbook: [
    t(
      '最大{さいだい}積載{せきさい}荷重{かじゅう}1トン以上{いじょう}は「フォークリフト運転{うんてん}技能講習{ぎのうこうしゅう}」、1トン未満{みまん}は「特別教育{とくべつきょういく}」の修了証{しゅうりょうしょう}を携帯{けいたい}する。', // verify: 最大積載荷重1トン以上はフォークリフト運転技能講習、1トン未満はフォークリフト運転特別教育
      'Only those who completed the Skill Training (1t or more) or Special Education (under 1t) may drive, carrying their card at all times.',
      '১ টন বা তার বেশিতে "দক্ষতা প্রশিক্ষণ" এবং ১ টনের কমের জন্য "বিশেষ শিক্ষা" সনদপ্রাপ্ত ব্যক্তিরাই শুধু ফর্কলিফ্ট চালাবেন, এবং সর্বদা সনদ সাথে রাখবেন।',
    ),
    t(
      'フォークの爪{つめ}の上{うえ}に人{ひと}を乗{の}せたり、パレットの上{うえ}に立{た}たせて昇降{しょうこう}作業{さぎょう}をさせてはならない。',
      'Never allow people to ride on the forks or stand on pallets for elevated work.',
      'ফর্কের কাঁটার ওপর মানুষ চড়ানো বা প্যালেটের ওপর কাউকে দাঁড় করিয়ে ওপরে ওঠানো-নামানোর কাজ সম্পূর্ণ নিষেধ।',
    ),
    t(
      '荷役{にやく}走行{そうこう}時{じ}はフォークを地上{ちじょう}20〜30cmに保{たも}ち、マストを後方{こうほう}に傾{かたむ}けて安定{あんてい}させる。', // verify: 走行時のフォーク高さは地上20〜30cm（マスト後傾）
      'When driving with a load, keep forks 20 to 30 cm off the ground and tilt the mast back for stability.',
      'মাল বহনের সময় ফর্ক মাটি থেকে ২০-৩০ সেমি ওপরে রাখবে এবং স্থিতিশীলতার জন্য মাস্ট পেছনের দিকে হেলিয়ে চালাবে।',
    ),
    t(
      '荷物{にもつ}で前{まえ}の視界{しかい}が遮{さえぎ}られる時{とき}は、必{かなら}ず後進{こうしん}（バック）走行{そうこう}するか誘導者{ゆうどうしゃ}を配置{はいち}する。',
      'When forward visibility is blocked by cargo, always drive in reverse or assign a spotter.',
      'সামনের মাল দৃষ্টি আটকে দিলে অবশ্যই রিভার্স গিয়ারে পেছনে চালিয়ে যাবে অথবা একজন সংকেতকারী নিয়োজিত করবে।',
    ),
    t(
      'フォークを高{たか}く上{あ}げたまま走行{そうこう}したり、急旋回{きゅうせんかい}や急{きゅう}ブレーキを行{おこな}ってはならない。',
      'Never drive with the forks raised high, and avoid sudden sharp turns or abrupt stops.',
      'ফর্ক উঁচুতে তুলে গাড়ি চালানো, এবং হঠাত দ্রুত বাঁক নেওয়া বা কড়া ব্রেক কষা সম্পূর্ণ নিষিদ্ধ।',
    ),
    t(
      '構内{こうない}では事業場{じぎょうじょう}で定{さだ}められた制限{せいげん}速度{そくど}（一般的{いっぱんてき}に時速{じそく}10km以下{いか}）を守{まも}り徐行{じょこう}する。', // verify: 構内走行速度は各事業場規程（一般に時速10km以下基準）
      'Strictly obey site speed limits (generally 10 km/h or less) and slow down in plant yards.',
      'কারখানার ভেতরে নির্ধারিত গতিসীমা (সাধারণত ঘণ্টায় ১০ কিমির কম) মেনে চলবে এবং সাবধানে ধীরে চালাবে।',
    ),
    t(
      '歩行者{ほこうしゃ}通路{つうろ}との交差点{こうさてん}や見通{みとお}しの悪{わる}い角{かど}では、必{かなら}ず一時{いちじ}停止{ていし}して警音器{けいおんき}を鳴{な}らす。',
      'Always come to a full stop and sound the horn at pedestrian crossings and blind corners.',
      'পথচারীদের পথ ও মোড়ের মুখে অবশ্যই এক মুহূর্ত থেমে হর্ন বাজাবে।',
    ),
    t(
      '荷物{にもつ}の偏荷重{へんかじゅう}や過積載{かせきさい}を避{さ}け、パレットの奥{おく}深{ふか}くまでフォークを差{さ}し込{こ}む。',
      'Avoid off-center loads and overloading, inserting the forks fully to the root of the pallet.',
      'একপাশে হেলে থাকা ভার ও অতিরিক্ত লোড এড়িয়ে চলবে এবং ফর্ক পুরো প্যালেটের গোড়া পর্যন্ত ঢোকাবে।',
    ),
    t(
      '作業{さぎょう}開始{かいし}前{まえ}にブレーキ、タイヤ、油圧{ゆあつ}装置{そうち}、警報{けいほう}装置{そうち}などの始業前{しぎょうまえ}点検{てんけん}を実施{じっし}する。',
      'Perform daily pre-shift inspections on brakes, tires, hydraulics, horn, and lights before starting work.',
      'কাজ শুরুর আগে ব্রেক, টায়ার, হাইড্রলিক্স ও হর্ন-লাইটের প্রাত্যহিক পরিদর্শন সম্পন্ন করবে।',
    ),
    t(
      '運転席{うんてんせき}を離{はな}れる時{とき}は、フォークを接地{せっち}させ、ブレーキを引{ひ}き、キーを抜{ぬ}き取{と}る。',
      'When leaving the seat, lower forks to the ground, apply parking brake, stop engine, and remove key.',
      'ড্রাইভিং সিট ছাড়ার সময় ফর্ক মাটিতে ঠেকাবে, পার্কিং ব্রেক টানবে, ইঞ্জিন বন্ধ করে চাবি খুলে নেবে।',
    ),
  ],
  reel: [
    t(
      '周囲{しゅうい}確認{かくにん}、人{ひと}なし、ヨシ！',
      'Area clear, no pedestrians, yoshi!',
      'চারপাশে নজরদারি, মানুষ নেই, ঠিক আছে!',
    ),
    t(
      'フォーク高{たか}さ、地上{ちじょう}20センチ、ヨシ！', // verify: 走行時のフォーク高さは地上20〜30cm（マスト後傾）
      'Fork height, 20 cm from ground, yoshi!',
      'ফর্কের উচ্চতা, মাটি থেকে ২০ সেমি, ঠিক আছে!',
    ),
    t(
      'マスト後傾{こうけい}、傾{かたむ}き、ヨシ！',
      'Mast tilt, backward tilt, yoshi!',
      'মাস্ট পেছনে হেলানো, ঢাল ঠিক আছে!',
    ),
    t(
      '交差点{こうさてん}、一時{いちじ}停止{ていし}、左右{さゆう}、ヨシ！',
      'Intersection, temporary stop, left and right clear, yoshi!',
      'মোড়ের মুখ, সাময়িক থামা, ডানে-বামে ঠিক আছে!',
    ),
    t(
      '後進{こうしん}走行{そうこう}、後方{こうほう}確認{かくにん}、ヨシ！',
      'Reverse driving, rear view clear, yoshi!',
      'পেছনে চালিয়ে যাওয়া, পেছনের পথ পরিষ্কার, ঠিক আছে!',
    ),
    t(
      '警音器{けいおんき}鳴動{めいどう}、クラクション、ヨシ！',
      'Horn sounding, signal given, yoshi!',
      'হর্ন বাজানো, সংকেত দেওয়া, ঠিক আছে!',
    ),
    t(
      '駐車{ちゅうしゃ}、爪{つめ}接地{せっち}、キー抜{ぬ}き、ヨシ！',
      'Parking, forks on ground, key removed, yoshi!',
      'পার্কিং, কাঁটা মাটিতে ঠেকানো, চাবি খোলা, ঠিক আছে!',
    ),
  ],
  drills: [
    {
      id: 'forklift-spot',
      kind: 'spot',
      secs: 30,
      need: 4,
      name: t(
        'フォークリフト作業{さぎょう}の危険{きけん}予知{よち}',
        'Forklift operation hazard spotting',
        'ফর্কলিফ্ট পরিচালনায় বিপদ শনাক্তকরণ',
      ),
      goal: t(
        '30秒{びょう}でフォークリフト作業{さぎょう}に潜{ひそ}む危険{きけん}な状態{じょうたい}を4つ見{み}つける。',
        'Find 4 hazardous forklift operation conditions within 30 seconds.',
        '৩০ সেকেন্ডের মধ্যে ফর্কলিফ্ট কাজের ৪টি বিপজ্জনক ত্রুটি খুঁজে বের করো।',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '荷物{にもつ}を地上{ちじょう}1.5mまで高{たか}く上{あ}げたままで前進{ぜんしん}走行{そうこう}している',
            'Driving forward with forks raised high at 1.5m above ground',
            'মাটি থেকে ১.৫ মিটার উঁচুতে মাল তুলে রেখে সামনে চালিয়ে যাওয়া হচ্ছে',
          ),
        },
        {
          hazard: false,
          label: t(
            'フォークを地上{ちじょう}20cmに保{たも}ちマストを後方{こうほう}に倒{たお}して走行{そうこう}している', // verify: 走行時のフォーク高さは地上20〜30cm（マスト後傾）
            'Driving with forks at 20cm above ground and mast tilted backward',
            'ফর্ক মাটি থেকে ২০ সেমি উঁচুতে রেখে ও মাস্ট পেছনে হেলিয়ে গাড়ি চালানো হচ্ছে',
          ),
        },
        {
          hazard: true,
          label: t(
            'フォークの爪{つめ}の上{うえ}に作業員{さぎょういん}を直接{ちょくせつ}乗{の}せて高所{こうしょ}作業{さぎょう}をしている',
            'Worker standing directly on the fork tines to do elevated work',
            'উঁচু স্থানে কাজের জন্য কর্মীকে সরাসরি ফর্কের ওপর চড়িয়ে তোলা হচ্ছে',
          ),
        },
        {
          hazard: false,
          label: t(
            '見通{みとお}しの悪{わる}い交差点{こうさてん}の手前{てまえ}で一時{いちじ}停止{ていし}し警音器{けいおんき}を鳴{な}らしている',
            'Stopping temporarily and sounding horn before a blind intersection',
            'অন্ধকার মোড়ের মুখে সাময়িক থেমে হর্ন বাজিয়ে সংকেত দেওয়া হচ্ছে',
          ),
        },
        {
          hazard: true,
          label: t(
            '大{おお}きな荷物{にもつ}で前{まえ}が見{み}えないのにそのまま前進{ぜんしん}運転{うんてん}している',
            'Driving forward despite the high cargo completely blocking the front view',
            'উঁচু মালে সামনের দৃষ্টি পুরোপুরি আটকে গেলেও সামনে চালিয়ে যাওয়া হচ্ছে',
          ),
        },
        {
          hazard: false,
          label: t(
            '歩行者{ほこうしゃ}専用{せんよう}通路{つうろ}の白線{はくせん}の内側{うちがわ}を歩行者{ほこうしゃ}が歩{ある}いている',
            'Pedestrians walking inside the designated pedestrian walkway lines',
            'পথচারীরা তাদের জন্য নির্দিষ্ট সাদা দাগের ভেতরের পথ ধরে হাঁটছেন',
          ),
        },
        {
          hazard: true,
          label: t(
            '運転席{うんてんせき}を離{はな}れる際{さい}に爪{つめ}を浮{う}かせたままキーを差{さ}しっぱなしにしている',
            'Leaving the driver seat with forks raised and key left in the ignition',
            'ফর্ক উঁচুতে রেখে এবং চাবি সুইচে রেখে চালক সিট ছেড়ে চলে গেছে',
          ),
        },
        {
          hazard: false,
          label: t(
            'パレットの差込口{さしこみぐち}の奥{おく}までフォークをしっかり差{さ}し込{こ}んでいる',
            'Inserting the forks fully into the pallet opening up to the root',
            'প্যালেটের খাঁজের ভেতরে গোড়া পর্যন্ত ফর্ক শক্তভাবে ঢোকানো হয়েছে',
          ),
        },
        {
          hazard: true,
          label: t(
            'パレットの片側{かたがわ}だけに重{おも}い荷物{にもつ}を載{の}せて偏荷重{へんかじゅう}のまま走{はし}っている',
            'Driving with an unbalanced load shifted entirely to one side of pallet',
            'প্যালেটের একপাশে অতিরিক্ত ভার চাপিয়ে ভারসাম্যহীনভাবে চালানো হচ্ছে',
          ),
        },
        {
          hazard: false,
          label: t(
            'フォークリフト運転{うんてん}技能講習{ぎのうこうしゅう}の修了証{しゅうりょうしょう}を携帯{けいたい}している', // verify: 最大積載荷重1トン以上はフォークリフト運転技能講習
            'Carrying forklift operator skill training certificate card at all times',
            'ফর্কলিফ্ট ড্রাইভিং স্কিল ট্রেনিংয়ের সনদ সাথে রাখা আছে',
          ),
        },
      ],
    },
    {
      id: 'forklift-parking-seq',
      kind: 'seq',
      name: t(
        '安全{あんぜん}駐車{ちゅうしゃ}と降車{こうしゃ}手順{てじゅん}',
        'Safe parking and shutdown sequence',
        'নিরাপদ পার্কিং ও গাড়ি ছাড়ার সঠিক ধাপ',
      ),
      goal: t(
        '作業{さぎょう}終了{しゅうりょう}時{じ}の安全{あんぜん}な駐車{ちゅうしゃ}手順{てじゅん}5ステップを正{ただ}しい順番{じゅんばん}に並{なら}べる。',
        'Arrange the 5 steps for safely parking and shutting down the forklift in order.',
        'কাজ শেষে ফর্কলিফ্ট নিরাপদে পার্ক ও বন্ধ করার ৫টি ধাপ সঠিক ক্রমে সাজাও।',
      ),
      steps: [
        t(
          '1. 平坦{へいたん}で安全{あんぜん}な指定{してい}駐車{ちゅうしゃ}場所{ばしょ}にフォークリフトを完全{かんぜん}に停止{ていし}させる',
          '1. Stop the forklift completely at the designated flat, safe parking area',
          '১. সমতল ও নিরাপদ নির্ধারিত পার্কিং স্থানে ফর্কলিফ্ট পুরোপুরি থামাও',
        ),
        t(
          '2. 変速{へんそく}レバーを中立{ちゅうりつ}（ニュートラル）にし、駐車{ちゅうしゃ}ブレーキを確実{かくじつ}に引{ひ}く',
          '2. Shift the directional lever to neutral and securely apply the parking brake',
          '২. গিয়ার লিভার নিউট্রালে নাও এবং শক্ত করে পার্কিং ব্রেক টেনো',
        ),
        t(
          '3. マストを前傾{ぜんけい}させ、フォークの爪{つめ}先端{せんたん}を地面{じめん}に完全{かんぜん}に接地{せっち}させる',
          '3. Tilt the mast forward and lower fork tips until completely resting on the floor',
          '৩. মাস্ট সামনের দিকে কাত করো এবং ফর্কের ডগা পুরোপুরি মাটিতে ঠেকিয়ে নামাও',
        ),
        t(
          '4. エンジンスイッチをOFFにし、キースイッチからキーを抜{ぬ}き取{と}る',
          '4. Turn off the engine switch and remove the key from the ignition',
          '৪. ইঞ্জিন সুইচ বন্ধ করো এবং ইগনিশন থেকে চাবি খুলে নাও',
        ),
        t(
          '5. 車両{しゃりょう}から降{お}りて車輪{しゃりん}に輪留{わど}め（チョック）を正{ただ}しく設置{せっち}する',
          '5. Dismount safely and place wheel chocks firmly against the tires',
          '৫. সাবধানে নেমে টায়ারের নিচে হুইল চক বা চাকার গুটি শক্তভাবে বসাও',
        ),
      ],
    },
    {
      id: 'forklift-estop',
      kind: 'reflex',
      name: t(
        '歩行者{ほこうしゃ}飛{と}び出{だ}しの急停止{きゅうていし}反射{はんしゃ}',
        'Emergency stop reflex for pedestrian hazard',
        'পথচারী সামনে আসার সাথে সাথে জরুরি থামার প্রতিফলন',
      ),
      goal: t(
        '見通{みとお}しの悪{わる}い角{かど}から人{ひと}が飛{と}び出{だ}した！0.4秒以内{びょういない}にブレーキを踏{ふ}んで急停止{きゅうていし}する。',
        'A worker suddenly stepped into your path! Hit the brake within 0.4 seconds to emergency stop.',
        'অন্ধকার কোণ থেকে হঠাত মানুষ সামনে চলে এসেছে! ০.৪ সেকেন্ডের মধ্যে ব্রেক চেপে জরুরিভাবে গাড়ি থামাও।',
      ),
    },
  ],
  inspect: [
    {
      id: 'fork-inspect',
      defective: true,
      name: t(
        'フォーク（爪{つめ}）の始業前{しぎょうまえ}点検{てんけん}',
        'Fork tines pre-shift inspection',
        'ফর্ক (কাঁটা) কাজ শুরুর আগের পরিদর্শন',
      ),
      spots: [
        {
          x: 25,
          y: 35,
          check: t(
            '爪{つめ}の根元{ねもと}（直角{ちょっかく}曲{ま}がり部{ぶ}）：過荷重{かかじゅう}による亀裂{きれつ}・クラックがないか（良品{りょうひん}：亀裂{きれつ}なし / 不良{ふりょう}：付{つ}け根{ね}に明{あき}らかなヒビ割{わ}れあり）',
            'Fork heel: Any cracks from shock or overload (Good: Sound and crack-free / Defect: Visible crack at heel)',
            'ফর্কের গোড়া: অতিরিক্ত চাপে ফাটল বা ক্র্যাক আছে কিনা (ভালো: সম্পূর্ণ অক্ষত / ত্রুটি: গোড়ায় স্পষ্ট ফাটল)',
          ),
        },
        {
          x: 65,
          y: 55,
          check: t(
            '位置{いち}固定{こてい}ロックピン：スプリングが健全{けんぜん}で爪{つめ}の横滑{よこすべ}りを防{ふせ}いでいるか（良品{りょうひん}：溝{みぞ}に完全{かんぜん}に噛{か}み合{あ}う / 不良{ふりょう}：ピンが折{お}れて固定{こてい}できない）',
            'Position lock pin: Spring holds tines securely (Good: Firmly engaged in notch / Defect: Broken pin, tines slide)',
            'লক পিন: স্প্রিং ঠিক আছে এবং ফর্ক পাশে নড়াচড়া আটকায় কিনা (ভালো: খাঁজে শক্তভাবে আটকে থাকে / ত্রুটি: পিন ভাঙা)',
          ),
        },
        {
          x: 80,
          y: 75,
          check: t(
            '爪{つめ}の底面{ていめん}厚{あつ}み摩耗{まもう}：摩耗{まもう}による厚{あつ}みの減少{げんしょう}が基準値{きじゅんち}の10%以内{いじょう}か（良品{りょうひん}：十分{じゅうぶん}な厚{あつ}み / 不良{ふりょう}：摩耗{まもう}大{だい}で10%以上{いじょう}薄{うす}い）', // verify: フォークの厚み摩耗限度は基準厚の10%減
            'Blade bottom thickness: Is wear within 10% limit (Good: Sufficient thickness / Defect: Thickness reduced over 10%)',
            'ফর্কের তলার ক্ষয়: ক্ষয় ১০% সীমার ভেতরে আছে কিনা (ভালো: পর্যাপ্ত পুরুত্ব / ত্রুটি: ১০% এর বেশি পাতলা হয়ে গেছে)',
          ),
        },
      ],
    },
    {
      id: 'tire-inspect',
      defective: false,
      name: t(
        'タイヤ・車輪{しゃりん}およびハブナット点検{てんけん}',
        'Tires, wheels, and hub nuts inspection',
        'টায়ার, চাকা ও হাব নাট পরিদর্শন',
      ),
      spots: [
        {
          x: 35,
          y: 40,
          check: t(
            'タイヤの摩耗{まもう}と亀裂{きれつ}：トレッド面{めん}に深{ふか}い亀裂{きれつ}や欠{か}けがないか（良品{りょうひん}：摩耗{まもう}限度{げんど}内{ない}で亀裂{きれつ}なし / 不良{ふりょう}：大{おお}きな亀裂{きれつ}や偏摩耗{へんまもう}あり）',
            'Tire tread wear and cracks: Any deep cracks or chunking (Good: Within wear limit, intact / Defect: Deep cuts or chunking)',
            'টায়ার ক্ষয় ও ফাটল: ট্রেডে গভীর ফাটল বা অংশ খসে গেছে কিনা (ভালো: ক্ষয়সীমার মধ্যে অক্ষত / ত্রুটি: গভীর ফাটল বা ক্ষয়)',
          ),
        },
        {
          x: 55,
          y: 60,
          check: t(
            'ハブナットの緩{ゆる}み：ホイール固定{こてい}ナットの緩{ゆる}みや脱落{だつらく}がないか（良品{りょうひん}：全{すべ}て均一{きんいつ}に締結{ていけつ}・合{あ}いマーク一致{いっち} / 不良{ふりょう}：ナット緩{ゆる}み・欠落{けつらく}）',
            'Hub nut tightness: Any loose or missing wheel rim nuts (Good: All torqued with alignment marks / Defect: Loose or missing nuts)',
            'হাব নাট আঁটসাঁট: চাকার নাট ঢিলে বা পড়ে গেছে কিনা (ভালো: সব নাট সমান আঁটসাঁট ও দাগ মেলানো / ত্রুটি: নাট আলগা বা নেই)',
          ),
        },
        {
          x: 75,
          y: 45,
          check: t(
            '異物{いぶつ}の噛{か}み込{こ}み：タイヤ溝{みぞ}に金属片{きんぞくへん}やボルト・ガラスが刺{さ}さっていないか（良品{りょうひん}：異物{いぶつ}なし / 不良{ふりょう}：太{ふと}い釘{くぎ}が深{ふか}く刺{さ}さっている）',
            'Foreign object penetration: Any nails, bolts, or metal debris (Good: Clean tread / Defect: Large nail embedded deeply)',
            'বাইরের বস্তু বিঁধে থাকা: টায়ারে পেরেক, লোহা বা কাচ বিঁধে আছে কিনা (ভালো: পরিষ্কার / ত্রুটি: বড় পেরেক গভীরভাবে বিঁধে আছে)',
          ),
        },
      ],
    },
    {
      id: 'hydraulic-inspect',
      defective: true,
      name: t(
        '油圧{ゆあつ}シリンダー・ホースの油漏{あぶらも}れ点検{てんけん}',
        'Hydraulic cylinder and hose leak inspection',
        'হাইড্রলিক সিলিন্ডার ও হোস পাইপ লিক পরিদর্শন',
      ),
      spots: [
        {
          x: 30,
          y: 30,
          check: t(
            '油圧{ゆあつ}配管{はいかん}・継手{つぎて}の油漏{あぶらも}れ：ホース接続部{せつぞくぶ}から油{あぶら}がにじんでいないか（良品{りょうひん}：油漏{あぶらも}れなし / 不良{ふりょう}：継手{つぎて}から作動油{さどうゆ}が滴{したた}り落{お}ちている）',
            'Hydraulic hose fittings: Any fluid leakage at joints (Good: Completely dry and clean / Defect: Hydraulic oil actively dripping)',
            'হাইড্রলিক হোস সংযোগ: জয়েন্ট দিয়ে তেল চুইয়ে পড়ছে কিনা (ভালো: সম্পূর্ণ শুকনো ও লিকমুক্ত / ত্রুটি: পাইপ দিয়ে তেল ফোঁটায় ফোঁটায় পড়ছে)',
          ),
        },
        {
          x: 50,
          y: 55,
          check: t(
            'リフトチェーンの張{は}り：左右{さゆう}のチェーンの張{は}りが均等{きんとう}で給油{きゅうゆ}されているか（良品{りょうひん}：左右{さゆう}均等{きんとう}で適正{てきせい}な張{は}り / 不良{ふりょう}：片側{かたがわ}が著{いちじる}しく弛緩{しかん}）',
            'Lift chain tension: Are both chains evenly tensioned and oiled (Good: Symmetrical tension / Defect: One side visibly sagging)',
            'লিফ্ট চেইনের টান: দুই পাশের চেইন সমান টানা ও তৈলাক্ত কিনা (ভালো: দুই পাশে সমান টান / ত্রুটি: এক পাশ অস্বাভাবিক ঝুলে গেছে)',
          ),
        },
        {
          x: 70,
          y: 70,
          check: t(
            'シリンダーロッドの傷{きず}：ピストンロッド表面{ひょうめん}に縦{たて}傷{きず}やサビがないか（良品{りょうひん}：鏡面{きょうめん}で平滑{へいかつ} / 不良{ふりょう}：深{ふか}い筋{すじ}傷{きず}があり油{あぶら}が漏{も}れ出{で}ている）',
            'Cylinder rod surface: Any deep scratches or scoring (Good: Mirror-smooth rod / Defect: Deep score marks causing oil leaks)',
            'সিলিন্ডার রডের দাগ: রডের গায়ে লম্বা আঁচড় বা মরচে আছে কিনা (ভালো: মসৃণ আয়নার মতো / ত্রুটি: গভীর ক্ষত ও তেল বেরিয়ে আসছে)',
          ),
        },
      ],
    },
  ],
  quiz: [
    {
      q: t(
        '工場{こうじょう}で最大{さいだい}積載{せきさい}荷重{かじゅう}2.5トンのフォークリフトを運転{うんてん}して荷役{にやく}を行{おこな}うことになりました。労働安全衛生法上{ろうどうあんぜんえいせいほうじょう}、必要{ひつよう}な資格{しかく}はどれですか？',
        'You need to operate a forklift with 2.5-ton rated capacity in a factory. Which qualification is legally required?',
        'কারখানায় ২.৫ টন বহনক্ষমতার ফর্কলিফ্ট চালিয়ে মাল তোলার দায়িত্ব দেওয়া হয়েছে। আইন অনুযায়ী কোন সনদটি আবশ্যক?',
      ),
      options: [
        t(
          '最大{さいだい}積載{せきさい}荷重{かじゅう}1トン以上{いじょう}の「フォークリフト運転{うんてん}技能講習{ぎのうこうしゅう}」修了証{しゅうりょうしょう}', // verify: 最大積載荷重1トン以上はフォークリフト運転技能講習
          'Forklift operator skill training certificate (for capacities of 1 ton or more)',
          'ফর্কলিফ্ট ড্রাইভিং দক্ষতা প্রশিক্ষণ সনদ (১ টন বা তার বেশি লোডের জন্য)',
        ),
        t(
          '最大{さいだい}積載{せきさい}荷重{かじゅう}1トン未満{みまん}の「フォークリフト特別教育{とくべつきょういく}」修了証{しゅうりょうしょう}',
          'Forklift special education certificate (for capacities under 1 ton)',
          'ফর্কলিফ্ট বিশেষ শিক্ষা সনদ (১ টনের কম লোডের জন্য)',
        ),
        t(
          '普通自動車{ふつうじどうしゃ}第{だい}一種{いっしゅ}運転免許{うんてんめんきょ}（フォークリフト講習{こうしゅう}は未受講{みじゅこう}）',
          'Standard automobile driver license (without forklift training)',
          'সাধারণ মোটরযান ড্রাইভিং লাইসেন্স (ফর্কলিফ্ট প্রশিক্ষণ ছাড়া)',
        ),
        t(
          '特別{とくべつ}な資格{しかく}は不要{ふよう}で、社内{しゃない}での30分間{ぷんかん}の練習{れんしゅう}のみ',
          'No license needed, only 30 minutes of in-house practice',
          'কোনো সনদের প্রয়োজন নেই, শুধু কারখানার ভেতরে ৩০ মিনিট চালালেই চলবে',
        ),
      ],
      answer: 0,
      why: t(
        '労働安全衛生法により、最大積載荷重1トン以上のフォークリフトの運転には「技能講習」の修了が義務付けられています。自動車免許や特別教育では運転できません。',
        'Under the Industrial Safety and Health Act, operating forklifts of 1 ton or more strictly requires the Skill Training course. Driver licenses or special education are not sufficient.',
        'শ্রম নিরাপত্তা আইন অনুসারে ১ টন বা তার বেশি বহনক্ষমতার ফর্কলিফ্ট চালনার জন্য "দক্ষতা প্রশিক্ষণ" সনদ বাধ্যতামূলক। সাধারণ লাইসেন্স বা বিশেষ শিক্ষা দিয়ে চালানো নিষিদ্ধ।',
      ),
    },
    {
      q: t(
        '荷物{にもつ}をフォークリフトに積{つ}んで構内{こうない}を走行{そうこう}する際{さい}、転倒{てんとう}を防{ふせ}ぐ安全{あんぜん}なフォークの高{たか}さはどれですか？',
        'When carrying a load on a forklift in the yard, what is the safe fork height to prevent tip-over accidents?',
        'ফর্কলিফ্টে মাল নিয়ে চলার সময় গাড়ি উল্টে যাওয়া রোধে কাঁটা কত উচ্চতায় রাখা নিরাপদ?',
      ),
      options: [
        t(
          '床{ゆか}から1.5メートル以上{いじょう}の高{たか}さに上{あ}げて走{はし}る',
          'Raise to 1.5 meters or more above the floor while driving',
          'মেঝে থেকে ১.৫ মিটার বা তার বেশি উঁচুতে তুলে চালানো',
        ),
        t(
          'フォークを地上{ちじょう}20〜30cmまで下{さ}げ、マストを後方{こうほう}に傾{かたむ}ける', // verify: 走行時のフォーク高さは地上20〜30cm（マスト後傾）
          'Lower forks to 20-30 cm above ground and tilt mast backward',
          'কাঁটা মাটি থেকে ২০-৩০ সেমি ওপরে নামিয়ে মাস্ট পেছনের দিকে হেলিয়ে রাখা',
        ),
        t(
          'フォークの先端{せんたん}が地面{じめん}に擦{す}れる高{たか}さ（0cm）でマストを前傾{ぜんけい}させる',
          'Keep forks touching the ground (0 cm) with mast tilted forward',
          'কাঁটা মাটিতে ঘষে চলার মতো নিচে (০ সেমি) রেখে মাস্ট সামনে হেলানো',
        ),
        t(
          '積{つ}み荷{に}の重{おも}さに関係{かんけい}なく、マストを垂直{すいちょく}にしたまま最高{さいこう}位置{いち}まで上{あ}げる',
          'Raise to maximum height with mast vertical regardless of load',
          'মালের ওজন যাই হোক মাস্ট সোজা রেখে সর্বোচ্চ উচ্চতায় তুলে রাখা',
        ),
      ],
      answer: 1,
      why: t(
        '走行時は車体の重心を低く保つため、フォークを地上20〜30cmまで下げ、マストを後方に倒して（後傾）走行するのが鉄則です。',
        'To keep the center of gravity low while traveling, lower forks to 20-30 cm above the ground and tilt the mast fully backward.',
        'চলার সময় ভরকেন্দ্র নিচে রাখতে ফর্ক মাটি থেকে ২০-৩০ সেমি ওপরে রাখা এবং মাস্ট পেছনে হেলিয়ে গাড়ি চালানো প্রধান নিয়ম।',
      ),
    },
    {
      q: t(
        '背{せ}の高{たか}い荷物{にもつ}を積{つ}んだため、前方向{まえほうこう}の視界{しかい}が完全{かんぜん}に遮{さえぎ}られて見{み}えません。この時{とき}の正{ただ}しい走行{そうこう}方法{ほうほう}はどれですか？',
        'Tall cargo on the pallet completely blocks your forward visibility. What is the correct and safe way to drive?',
        'প্যালেটে উঁচু মাল লোড করায় সামনের দৃষ্টি পুরোপুরি আড়াল হয়ে গেছে। এ অবস্থায় নিরাপদে গাড়ি চালানোর সঠিক পদ্ধতি কোনটি?',
      ),
      options: [
        t(
          '前{まえ}が見{み}えるようになるまでフォークを高{たか}く上{あ}げて前進{ぜんしん}する',
          'Raise forks high until you can see underneath, then drive forward',
          'নিচ দিয়ে দেখা না যাওয়া পর্যন্ত ফর্ক অনেক উঁচুতে তুলে সামনে এগিয়ে যাওয়া',
        ),
        t(
          '運転席{うんてんせき}から身{み}を大{おお}きく乗{の}り出{だ}して見{み}ながら高速{こうそく}で前進{ぜんしん}する',
          'Lean far out of the cabin and drive forward at high speed',
          'কেবিন থেকে শরীর ঝুঁকিয়ে বাইরে বের করে দ্রুত গতিতে সামনে যাওয়া',
        ),
        t(
          '顔{かお}を後{うし}ろに向{む}けて直接{ちょくせつ}目視{もくし}で後方{こうほう}を確認{かくにん}しながら後進{こうしん}（バック）走行{そうこう}する',
          'Turn head to look directly behind and travel in reverse (backward)',
          'পেছনে সরাসরি তাকিয়ে চোখ দিয়ে দেখে রিভার্স গিয়ারে পেছনে চালিয়ে যাওয়া',
        ),
        t(
          '前{まえ}が見{み}えないままクラクションを連続{れんぞく}で鳴{な}らしながら前進{ぜんしん}する',
          'Drive forward blindly while continuously sounding the horn',
          'সামনে কিছু না দেখেই ক্রমাগত হর্ন বাজিয়ে সামনে চালিয়ে যাওয়া',
        ),
      ],
      answer: 2,
      why: t(
        '荷物で前方視界が塞がれた時は、フォークを上げてはいけません。後方を直接目視しながら後進走行するか、誘導者を配置して誘導を受けます。',
        'When forward sight is blocked, never raise the forks. Drive in reverse while directly looking backward, or use a designated spotter.',
        'সামনের দৃষ্টি আটকে গেলে কখনো ফর্ক ওপরে তুলবে না। সরাসরি পেছনে তাকিয়ে রিভার্সে চালাবে অথবা সংকেতকারীর দিকনির্দেশনা নেবে।',
      ),
    },
    {
      q: t(
        '高{たか}い棚{たな}のダンボールを取{と}るため、同僚{どうりょう}から「フォークのパレットに乗{の}せて上{うえ}まで持{も}ち上{あ}げてほしい」と頼{たの}まれました。どう対応{たいおう}すべきですか？',
        'A coworker asks you to lift them up on a pallet resting on the forks to reach boxes on a high shelf. How should you respond?',
        'উঁচু শেলফের কার্টন নামানোর জন্য একজন সহকর্মী অনুরোধ করল ফর্কের ওপর রাখা প্যালেটে তাকে তুলে ওপরে তুলতে। তোমার কী করা উচিত?',
      ),
      options: [
        t(
          '同僚{どうりょう}がパレットにしっかり掴{つか}まっていれば持{も}ち上{あ}げてよい',
          'Lift them as long as they hold firmly onto the pallet',
          'সহকর্মী শক্তভাবে ধরে দাঁড়িয়ে থাকলে তাকে ওপরে তোলা যায়',
        ),
        t(
          'ヘルメットをかぶってゆっくり昇降{しょうこう}させれば法令上{ほうれいじょう}問題{もんだい}ない',
          'It is allowed by law if wearing a helmet and moving slowly',
          'হেলমেট পরা থাকলে এবং আস্তে আস্তে ওঠানামা করালে আইনে বাধা নেই',
        ),
        t(
          '短時間{たんじかん}の作業{さぎょう}で1人{ひとり}だけなら特別{とくべつ}に認{みと}められている',
          'Allowed as an exception for short tasks with only one person',
          'অল্প সময়ের কাজের জন্য একজন মানুষকে এভাবে তোলা বিশেষ অনুমতিপ্রাপ্ত',
        ),
        t(
          '用途外{ようとがい}使用{しよう}および搭乗{とうじょう}制限{せいげん}違反{いはん}となるため、絶対{ぜったい}に断{ことわ}る',
          'Refuse immediately, as riding forks is illegal and strictly prohibited',
          'অবিলম্বে সরাসরি না করবে, কারণ ফর্কে মানুষ ওঠানো বেআইনি ও সম্পূর্ণ নিষিদ্ধ',
        ),
      ],
      answer: 3,
      why: t(
        '労働安全衛生規則により、フォークリフトを人を乗せて昇降させる用途に使うことは厳禁されています。高所作業台や脚立を使用しなければなりません。',
        'Under occupational safety regulations, using forklifts to lift personnel is strictly prohibited. Use dedicated aerial work platforms or rolling ladders.',
        'শ্রম নিরাপত্তা আইন অনুসারে ফর্কলিফ্টে মানুষ চড়িয়ে ওঠানামা করানো কঠোরভাবে নিষিদ্ধ। এজন্য নির্ধারিত মই বা ওয়ার্ক প্ল্যাটফর্ম ব্যবহার করতে হবে।',
      ),
    },
    {
      q: t(
        'フォークリフトで走行中{そうこうちゅう}、見通{みとお}しの悪{わる}い交差点{こうさてん}や歩行者{ほこうしゃ}通路{つうろ}の手前{てまえ}に近{ちか}づきました。正{ただ}しい運転{うんてん}行動{こうどう}はどれですか？',
        'You approach a blind intersection or pedestrian walkway while driving a forklift. What is the correct driving procedure?',
        'ফর্কলিফ্ট চালিয়ে যাওয়ার সময় অন্ধকার মোড়ের মুখ বা পথচারী পারাপারের কাছে পৌঁছালে সঠিক করণীয় কী?',
      ),
      options: [
        t(
          '交差点{こうさてん}手前{てまえ}で必{かなら}ず一時{いちじ}停止{ていし}し、クラクションを鳴{な}らして左右{さゆう}の安全{あんぜん}を確認{かくにん}する',
          'Always stop before intersection, sound horn, and confirm left/right clearance',
          'মোড়ের আগে অবশ্যই এক মুহূর্ত থামো, হর্ন বাজাও এবং ডানে-বামে নিরাপত্তা নিশ্চিত করো',
        ),
        t(
          '人{ひと}がいないように見{み}えたので、減速{げんそく}せずにそのまま一気{いっき}に走{はし}り抜{ぬ}ける',
          'Speed straight through without slowing down since no one seems to be there',
          'কাউকে দেখা যাচ্ছে না ভেবে গতি না কমিয়ে দ্রুত পার হয়ে যাওয়া',
        ),
        t(
          'クラクションを鳴{な}らし続{つづ}けながら、停止{ていし}せずにスピードを上{あ}げて通過{つうか}する',
          'Speed up through the crossing while continuously sounding the horn',
          'গাড়ি না থামিয়ে বরং গতি বাড়িয়ে ক্রমাগত হর্ন বাজিয়ে মোড় পার হওয়া',
        ),
        t(
          'ブレーキを踏{ふ}まず、片手{かたて}運転{うんてん}で手{て}を振{ふ}って周囲{しゅうい}に合図{あいず}しながら進{すす}む',
          'Drive through with one hand on steering and waving with the other hand',
          'ব্রেক না চেপে এক হাতে স্টিয়ারিং ধরে অন্য হাত নেড়ে ইশারা দিয়ে পার হওয়া',
        ),
      ],
      answer: 0,
      why: t(
        '見通しの悪い交差点や歩行者通路の手前では、必ず一旦停止して警音器を鳴らし、左右の安全を指差呼称で確認してから徐行で進みます。',
        'Before blind corners or pedestrian zones, you must stop completely, sound the horn, and confirm clearance on both sides before crawling forward.',
        'অন্ধকার মোড় ও পথচারী পথের আগে অবশ্যই পুরোপুরি গাড়ি থামিয়ে হর্ন বাজাতে হবে এবং ডানে-বামে দেখে তারপর ধীরে এগোতে হবে।',
      ),
    },
    {
      q: t(
        'フォークリフトの旋回{せんかい}（曲{ま}がり角{かど}）特性{とくせい}について、正{ただ}しい説明{せつめい}はどれですか？',
        'Which statement correctly describes the steering and turning characteristics of a forklift?',
        'ফর্কলিফ্টের বাঁক নেওয়া বা ঘোরার বৈশিষ্ট্য সম্পর্কে কোন ব্যাখ্যাটি সঠিক?',
      ),
      options: [
        t(
          '一般{いっぱん}乗用車{じょうようしゃ}と同{おな}じ前輪{ぜんりん}操舵{そうだ}なので、曲{ま}がる時{とき}に車体{しゃたい}後部{こうぶ}は一切{いっさい}外側{そとがわ}に振{ふ}れない',
          'Steers with front wheels like a car, so rear end never swings outward',
          'সাধারণ গাড়ির মতো সামনের চাকা ঘোরে, তাই গাড়ির পেছনের অংশ বাইরে ছিটকে যায় না',
        ),
        t(
          '後輪{こうりん}で舵{かじ}を切{き}る後輪{こうりん}操舵{そうだ}のため、旋回{せんかい}時{じ}に車体{しゃたい}後部{こうぶ}が大{おお}きく外側{そとがわ}へ振{ふ}れ出{だ}す',
          'Steers with rear wheels, causing the rear counterweight to swing out widely',
          'পেছনের চাকা ঘুরে দিক বদলায়, তাই মোড় ঘোরার সময় গাড়ির পেছনের অংশ অনেকখানি বাইরে ছিটকে যায়',
        ),
        t(
          '四輪{よんりん}すべてが同{おな}じ角度{かくど}で曲{ま}がるため、急旋回{きゅうせんかい}しても横転{おうてん}する危険{きけん}はない',
          'All four wheels steer at the same angle, so it cannot tip over on sharp turns',
          'চারটি চাকা একই কোণে ঘোরে, তাই কড়া বাঁক নিলেও গাড়ি উল্টে যাওয়ার কোনো ভয় নেই',
        ),
        t(
          '荷物{にもつ}を高{たか}く持{も}ち上{あ}げた状態{じょうたい}の方{ほう}が重心{じゅうしん}が安定{あんてい}して急旋回{きゅうせんかい}しやすい',
          'Sharp turns are more stable with the load raised high in the air',
          'মাল উঁচুতে তোলা অবস্থায় ভরকেন্দ্র বেশি স্থির থাকে তাই দ্রুত বাঁক নেওয়া সহজ',
        ),
      ],
      answer: 1,
      why: t(
        'フォークリフトは後輪操舵のため、旋回時にリヤ部が外側に大きく振れます。周囲の作業員や柱を巻き込まないよう十分な間隔が必要です。',
        'Forklifts steer using the rear wheels, causing the rear end to swing out widely. Always watch for nearby personnel and walls.',
        'ফর্কলিফ্ট পেছনের চাকা দিয়ে মোড় ঘোরে, তাই ঘোরার সময় পেছনের অংশ বাইরে ছড়িয়ে যায়। আশেপাশের মানুষ বা পিলারে ধাক্কা না লাগার খেয়াল রাখা জরুরি।',
      ),
    },
    {
      q: t(
        '工場{こうじょう}や倉庫{そうこ}の構内{こうない}をフォークリフトで走行{そうこう}する際{さい}の制限{せいげん}速度{そくど}のルールとして正{ただ}しいものはどれですか？',
        'What is the standard rule regarding speed limits when operating a forklift inside factory yards and warehouses?',
        'কারখানা বা গুদামের ভেতরে ফর্কলিফ্ট চালানোর সময় গতিসীমার নিয়ম কী?',
      ),
      options: [
        t(
          '広{ひろ}い構内{こうない}であれば、公道{こうどう}と同{おな}じ時速{じそく}40kmで走行{そうこう}してよい',
          'In wide yards, you may drive at 40 km/h just like public roads',
          'খোলামেলা চত্বর হলে সাধারণ সড়কের মতো ঘণ্টায় ৪০ কিমি গতিতে চালানো যায়',
        ),
        t(
          '荷物{にもつ}を積{つ}んでいない空車{くうしゃ}時{じ}であれば、速度{そくど}無制限{むせいげん}で走{はし}ってよい',
          'When carrying no load, you may drive at unlimited speed',
          'গাড়িতে মাল লোড করা না থাকলে যত খুশি দ্রুত চালানো যায়',
        ),
        t(
          '事業場{じぎょうじょう}で定{さだ}められた制限{せいげん}速度{そくど}（一般{いっぱん}に時速{じそく}10km以下{いか}）を守{まも}り徐行{じょこう}する', // verify: 構内走行速度は各事業場規程（一般に時速10km以下基準）
          'Strictly follow the site speed limit (typically 10 km/h or less) and drive slowly',
          'কারখানার নির্ধারিত গতিসীমা (সাধারণত ঘণ্টায় ১০ কিমির কম) মেনে সাবধানে ধীরে চালাবে',
        ),
        t(
          '作業{さぎょう}終了{しゅうりょう}前{まえ}で時間{じかん}がない時{とき}は、周囲{しゅうい}に声{こえ}をかけて全速力{ぜんそくりょく}で走{はし}ってよい',
          'When in a rush near shift end, you may drive at top speed while shouting',
          'কাজের শেষ সময়ে তাড়াহুড়ো থাকলে মুখে সবাইকে সরে যেতে বলে সর্বোচ্চ গতিতে চালানো যায়',
        ),
      ],
      answer: 2,
      why: t(
        '構内では歩行者や他の車両と交差するため、各事業場で定められた制限速度（原則時速10km以下）を厳守し、常に安全に停止できる速度で走行します。',
        'Because yards have crossing pedestrians and equipment, obey the site limit (normally 10 km/h or less) and travel at speeds that allow instant stopping.',
        'কারখানার ভেতরে পথচারী ও অন্যান্য গাড়ির আনাগোনা থাকে, তাই নির্ধারিত গতিসীমা (সাধারণত ১০ কিমি/ঘণ্টার কম) মেনে তাৎক্ষণিক থামার মতো গতিতে চালাতে হয়।',
      ),
    },
    {
      q: t(
        '休憩{きゅうけい}などでフォークリフトを駐車{ちゅうしゃ}して運転席{うんてんせき}を離{はな}れる際{さい}、正{ただ}しい安全{あんぜん}措置{そち}はどれですか？',
        'When parking a forklift and leaving the operator seat for a break, which safety procedure must you perform?',
        'বিরতিতে যাওয়ার সময় ফর্কলিফ্ট পার্ক করে সিট ছাড়ার ক্ষেত্রে কোন নিরাপত্তা পদক্ষেপটি নেওয়া বাধ্যতামূলক?',
      ),
      options: [
        t(
          'フォークを床上{ゆかうえ}50cmに浮{う}かせたまま、ギアを入{い}れてエンジンを止{と}める',
          'Leave forks 50 cm off ground in gear and turn off engine',
          'কাঁটা মেঝে থেকে ৫০ সেমি ওপরে ঝুলিয়ে গিয়ারে রেখে ইঞ্জিন বন্ধ করা',
        ),
        t(
          '傾斜{けいしゃ}のある坂道{さかみち}に止{と}め、キーを差{さ}しっぱなしで立{た}ち去{さ}る',
          'Park on a slope and walk away leaving key in ignition',
          'ঢালু রাস্তায় গাড়ি রেখে সুইচে চাবি রেখেই চলে যাওয়া',
        ),
        t(
          '通路{つうろ}の真{ま}ん中{なか}にフォークを上{あ}げたまま止{と}め、サイドブレーキを引{ひ}く',
          'Park in the center of walkway with forks raised high',
          'চলার পথের মাঝখানে কাঁটা উঁচুতে তুলে রেখে হ্যান্ডব্রেক টানা',
        ),
        t(
          'フォークを地面{じめん}に下{お}ろして接地{せっち}させ、駐車{ちゅうしゃ}ブレーキを引{ひ}き、キーを抜{ぬ}く',
          'Lower forks fully to the ground, set parking brake, stop engine, and remove key',
          'কাঁটা মাটিতে ঠেকিয়ে নামানো, পার্কিং ব্রেক শক্ত করে টানা, ইঞ্জিন বন্ধ করা ও চাবি খুলে নেওয়া',
        ),
      ],
      answer: 3,
      why: t(
        '法令により、運転位置を離れる時はフォーク先端を地上に接地させ、駐車ブレーキを確実にかけ、キーを抜いて保管することが義務付けられています。',
        'Safety regulations require lowering forks completely to the floor, applying parking brake firmly, and removing the key whenever leaving the vehicle.',
        'আইন অনুযায়ী গাড়ি ছাড়ার সময় কাঁটা মাটিতে ঠেকিয়ে রাখা, পার্কিং ব্রেক নিশ্চিত করা এবং চাবি খুলে সাথে নিয়ে নেওয়া বাধ্যতামূলক।',
      ),
    },
    {
      q: t(
        '労働安全衛生規則{ろうどうあんぜんえいせいきそく}で定{さだ}められている、フォークリフトの「作業{さぎょう}開始{かいし}前{まえ}点検{てんけん}」の必須{ひっす}項目{こうもく}はどれですか？',
        'Which of the following sets of inspection items is legally required before starting daily forklift operations?',
        'শ্রম নিরাপত্তা বিধিমালার আওতায় ফর্কলিফ্টের দৈনিক কাজ শুরুর আগে কোন বিষয়গুলো পরীক্ষা করা বাধ্যতামূলক?',
      ),
      options: [
        t(
          '制動{せいどう}装置{そうち}（ブレーキ）、操縦{そうじゅう}装置{そうち}、荷役{にやく}・油圧{ゆあつ}装置{そうち}、警報{けいほう}装置{そうち}（クラクション・前照灯{ぜんしょうとう}）の機能{きのう}',
          'Braking systems, steering controls, loading/hydraulic systems, and horn/headlights',
          'ব্রেক সিস্টেম, স্টিয়ারিং নিয়ন্ত্রণ, লোডিং ও হাইড্রলিক ব্যবস্থা, এবং হর্ন ও হেডলাইটের কার্যকারিতা',
        ),
        t(
          '車体{しゃたい}の外装{がいそう}ワックスのツヤ具合{ぐあい}とシートの色{いろ}',
          'Exterior body wax gloss level and driver seat fabric color',
          'গাড়ির বাইরের রঙের চকচকে ভাব এবং ড্রাইভিং সিটের কাপড়ের রং',
        ),
        t(
          '運転席{うんてんせき}のラジオ音量{おんりょう}とエアコンの冷{ひ}え具合{ぐあい}',
          'Cabin radio volume and air conditioner cooling strength',
          'কেবিনের রেডিওর ভলিউম ও এসির ঠান্ডার মাত্রা',
        ),
        t(
          '荷台{にだい}のステッカーの貼{は}り方{かた}のみを目視{もくし}で確認{かくにん}する',
          'Only visually checking if exterior safety stickers are neat',
          'বাইরের স্টিকারগুলো সোজাভাবে লাগানো আছে কিনা কেবল তা চোখে দেখে নেওয়া',
        ),
      ],
      answer: 0,
      why: t(
        '労働安全衛生規則第151条の27により、ブレーキ、操縦装置、荷役油圧装置、前照灯・クラクションの機能点検が始業前の必須義務です。',
        'Under Article 151-27 of the regulations, inspecting brakes, steering, hydraulics, mast/forks, and horn/lights is mandatory before daily operation.',
        'বিধিমালার ১৫১-২৭ অনুচ্ছেদ অনুযায়ী কাজ শুরুর আগে ব্রেক, স্টিয়ারিং, হাইড্রলিক্স ও হর্ন-লাইটের কার্যকারিতা যাচাই বাধ্যতামূলক।',
      ),
    },
    {
      q: t(
        'パレットの片側{かたがわ}だけに重{おも}い荷物{にもつ}が載{の}っている「偏荷重{へんかじゅう}」の状態で走行{そうこう}すると、どのような危険{きけん}がありますか？',
        'What dangerous hazard occurs if you travel with an off-center load placed on only one side of the pallet?',
        'প্যালেটের একপাশে অতিরিক্ত ভার চাপিয়ে ভারসাম্যহীন বা কাত করা অবস্থায় গাড়ি চালালে কী ধরনের বিপদ হতে পারে?',
      ),
      options: [
        t(
          '車体{しゃたい}の片側{かたがわ}のタイヤが浮{う}き上{あ}がって摩擦{まさつ}が減{へ}り、燃費{ねんぴ}が向上{こうじょう}する',
          'Tires on one side lift reducing road friction, improving fuel economy',
          'একপাশের চাকা ওপরে ওঠায় রাস্তার ঘর্ষণ কমে এবং জ্বালানি খরচ বাঁচে',
        ),
        t(
          '旋回{せんかい}やわずかな段差{だんさ}でバランスを崩{くず}し、車体{しゃたい}が横転{おうてん}したり荷崩{にくず}れを起{お}こす',
          'The forklift easily loses balance on turns or bumps, causing rollover or cargo collapse',
          'বাঁক নেওয়ার সময় বা সামান্য উঁচু-নিচুতেই ভারসাম্য হারিয়ে গাড়ি উল্টে যায় বা মাল পড়ে যায়',
        ),
        t(
          'ブレーキの効{き}きが普段{ふだん}よりも格段{かくだん}に良{よ}くなり、急停止{きゅうていし}しやすくなる',
          'Braking performance becomes much better than usual for quick stops',
          'ব্রেক স্বাভাবিকের চেয়ে অনেক বেশি শক্তভাবে ধরে এবং হঠাত থামা সহজ হয়',
        ),
        t(
          'フォークの片方{かたほう}が長持{ながも}ちするため、点検{てんけん}の手間{てま}が省{はぶ}ける',
          'One tine lasts longer, saving time on maintenance inspections',
          'একটি ফর্ক অনেক দিন ভালো থাকে, ফলে রক্ষণাবেক্ষণের ঝক্কি কমে',
        ),
      ],
      answer: 1,
      why: t(
        '荷物の重心が偏っていると、旋回時の遠心力や路面の段差で簡単に横転します。荷物はパレット中央に均等に積み、フォークを根元まで差し込みます。',
        'An eccentric load severely shifts center of gravity, causing catastrophic rollovers on turns. Always center cargo and fully engage forks.',
        'ওজন একপাশে হেলে থাকলে মোড় ঘোরার সময় ছিটকে গাড়ি সহজেই উল্টে যায়। মাল সর্বদা প্যালেটের মাঝে সমানভাবে রাখবে এবং ফর্ক গোড়া পর্যন্ত ঢুকাবে।',
      ),
    },
    {
      q: t(
        '荷物{にもつ}を載{の}せたフォークリフトで坂道{さかみち}（勾配{こうばい}）を走行{そうこう}する際{さい}の正{ただ}しいルールはどれですか？',
        'What is the correct rule when driving a loaded forklift on a slope or ramp?',
        'মালামাল বহনকারী ফর্কলিফ্ট নিয়ে ঢালু রাস্তায় ওঠার বা নামার ক্ষেত্রে কোন নিয়মটি সঠিক?',
      ),
      options: [
        t(
          '上{のぼ}り坂{ざか}も下{くだ}り坂{ざか}も、荷物{にもつ}の向{む}きに関係{かんけい}なく全{すべ}て前進{ぜんしん}で一気{いっき}に走{はし}り抜{ぬ}ける',
          'Drive forward at high speed on both uphill and downhill slopes',
          'ওঠার সময় ও নামার সময় উভয় ক্ষেত্রেই মাল যেদিকেই থাকুক দ্রুত গতিতে সামনে চালানো',
        ),
        t(
          '坂道{さかみち}の途中{とちゅう}で車体{しゃたい}を真横{まよこ}に向{む}けて旋回{せんかい}し、小回{こまわ}りで方向転換{ほうこうてんかん}する',
          'Turn the vehicle sideways across the middle of the slope to turn around',
          'ঢালু রাস্তার মাঝখানে এসে আড়াআড়িভাবে গাড়ি ঘুরিয়ে মোড় নেওয়া',
        ),
        t(
          '荷物{にもつ}を常{つね}に坂上{さかうえ}側{がわ}に向{む}け、上{のぼ}りは前進{ぜんしん}、下{くだ}りは後進{こうしん}（バック）で徐行{じょこう}する',
          'Keep load facing uphill at all times: drive forward uphill, and drive in reverse downhill',
          'মাল সর্বদা ঢালের ওপরের দিকে রাখবে: ওঠার সময় সামনে চালাবে এবং নামার সময় রিভার্স গিয়ারে ধীরে নামবে',
        ),
        t(
          '下{くだ}り坂{ざか}ではギアをニュートラルにして惰性{だせい}でスピードをつけて下{お}りる',
          'Shift into neutral on downhill slopes and coast down using momentum',
          'ঢাল বেয়ে নামার সময় গিয়ার নিউট্রালে দিয়ে স্বাভাবিক গতিতে গাড়ি গড়িয়ে নামানো',
        ),
      ],
      answer: 2,
      why: t(
        '荷物を積んで坂道を走る時は、荷物の転落を防ぐため常に荷物を坂上側に向けます（上り前進、下り後進）。坂道での方向転換や横切りは転倒するため厳禁です。',
        'When loaded on a ramp, always keep the load facing uphill (forward uphill, reverse downhill) to prevent spills. Never turn across a slope.',
        'ঢালু পথে মাল যাতে ছিটকে না পড়ে সেজন্য মাল সর্বদা উঁচু অংশের দিকে রাখতে হয় (ওঠার সময় সামনে, নামার সময় রিভার্সে)। ঢালে আড়াআড়ি ঘোরানো নিষেধ।',
      ),
    },
    {
      q: t(
        'パレットに載{の}った荷物{にもつ}をすくい上{あ}げる際{さい}、フォークの差{さ}し込{こ}み方{かた}の基本{きほん}動作{どうさ}として正{ただ}しいものはどれですか？',
        'When picking up palletized cargo, what is the correct procedure for inserting the fork tines?',
        'প্যালেটে থাকা মালামাল তোলার সময় ফর্কের কাঁটা ভেতরে ঢোকানোর সঠিক নিয়ম কোনটি?',
      ),
      options: [
        t(
          'スピードを落{お}とさず勢{いきお}いよくパレットに突{つ}っ込{こ}んで一度{いちど}に差{さ}し込{こ}む',
          'Ram into the pallet without slowing down to slide forks in at once',
          'গতি না কমিয়ে জোরে ধাক্কা দিয়ে একবারে প্যালেটের ভেতরে ফর্ক ঢুকিয়ে দেওয়া',
        ),
        t(
          'パレットの差込口{さしこみぐち}の先端{せんたん}20cmだけに爪{つめ}を浅{あさ}く掛{か}けて持{も}ち上{あ}げる',
          'Insert only 20 cm of the fork tips into the pallet to lift',
          'প্যালেটের মুখে কাঁটার সামনের ২০ সেমি সামান্য ঢুকিয়ে মাল তোলা',
        ),
        t(
          '車体{しゃたい}をパレットに対{たい}して大{おお}きく斜{なな}めにしたまま片方{かたほう}の爪{つめ}から差{さ}し込{こ}む',
          'Approach at a steep angle and insert one tine first into the opening',
          'গাড়িটি প্যালেটের সাথে তেরছা বা বাঁকা রেখে এক কাঁটা আগে ঢুকিয়ে তোলা',
        ),
        t(
          'パレットに車体{しゃたい}を正対{せいたい}させ、爪{つめ}をパレットの奥{おく}（根元{ねもと}）まで水平{すいへい}に完全{かんぜん}に差{さ}し込{こ}む',
          'Square up directly to the pallet and insert tines horizontally all the way to the root',
          'প্যালেটের একদম সামনাসামনি সোজাভাবে এসে কাঁটাটি অনুভূমিক রেখে গোড়া পর্যন্ত পুরোপুরি ঢোকানো',
        ),
      ],
      answer: 3,
      why: t(
        '荷役時は車体をパレットに正対させ、フォークを奥深く根元まで差し込みます。浅掛けは荷の落下やフォークの破損を招くため極めて危険です。',
        'Align squarely with the pallet and insert forks completely to the heel. Partial insertion risks load drops and fork damage.',
        'মাল তোলার সময় গাড়িকে প্যালেটের মুখোমুখি রেখে ফর্ক পুরো গোড়া পর্যন্ত ঢোকাতে হবে। অল্প ঢুকিয়ে তোলা অত্যন্ত ঝুঁকিপূর্ণ।',
      ),
    },
  ],
  glossary: [
    {
      ja: 'フォークリフト',
      reading: 'ふぉーくりふと',
      en: 'forklift / lift truck',
      bn: 'ফর্কলিফ্ট',
      note: t(
        '荷物{にもつ}を載{の}せるフォーク（爪{つめ}）と昇降{しょうこう}用{よう}マストを備{そな}えた荷役{にやく}運搬{うんぱん}車両{しゃりょう}。',
        'Cargo handling vehicle equipped with forks and a mast for lifting and moving.',
        'মাল তোলা ও বহনের জন্য ফর্ক ও মাস্টযুক্ত বিশেষ গাড়ি।',
      ),
    },
    {
      ja: '技能講習',
      reading: 'ぎのうこうしゅう',
      en: 'skill training course (1 ton or more)',
      bn: 'দক্ষতা প্রশিক্ষণ কোর্স (১ টন বা বেশি)',
      note: t(
        '最大{さいだい}積載{せきさい}荷重{かじゅう}1トン以上{いじょう}のフォークリフト運転{うんてん}に必須{ひっす}の国家{こっか}資格{しかく}講習{こうしゅう}。', // verify: 最大積載荷重1トン以上はフォークリフト運転技能講習
        'Mandatory national certification course to drive forklifts of 1 ton capacity or more.',
        '১ টন বা তার বেশি বহনক্ষমতার ফর্কলিফ্ট চালানোর বাধ্যতামূলক সনদ কোর্স।',
      ),
    },
    {
      ja: '特別教育',
      reading: 'とくべつきょういく',
      en: 'special education (under 1 ton)',
      bn: 'বিশেষ শিক্ষা প্রশিক্ষণ (১ টনের কম)',
      note: t(
        '最大{さいだい}積載{せきさい}荷重{かじゅう}1トン未満{みまん}のフォークリフト運転{うんてん}のために事業主{じぎょうぬし}が行{おこな}う安全{あんぜん}教育{きょういく}。', // verify: 最大積載荷重1トン未満はフォークリフト運転特別教育
        'Safety training conducted by employers for driving forklifts under 1 ton capacity.',
        '১ টনের কম বহনক্ষমতার ফর্কলিফ্ট চালনার জন্য নির্ধারিত নিরাপত্তা প্রশিক্ষণ।',
      ),
    },
    {
      ja: 'マスト',
      reading: 'ますと',
      en: 'mast',
      bn: 'মাস্ট (ফর্ক ওঠানামার স্তম্ভ কাঠামো)',
      note: t(
        'フォークを上下{じょうげ}に昇降{しょうこう}させたり前後{ぜんご}に傾{かたむ}けたりするための支柱{しちゅう}枠{わく}。',
        'Upright frame that raises, lowers, and tilts the forklift tines.',
        'ফর্ক ওপরে-নিচে ওঠানামা এবং সামনে-পেছনে হেলানোর খাড়া ফ্রেম বা কাঠামো।',
      ),
    },
    {
      ja: 'チルト',
      reading: 'ちると',
      en: 'tilt (mast tilt forward / back)',
      bn: 'টিল্ট (মাস্ট সামনে বা পেছনে হেলানো)',
      note: t(
        '荷物{にもつ}の落下{らっか}を防{ふせ}ぐため、マストを前傾{ぜんけい}または後傾{こうけい}させる動作{どうさ}。',
        'Motion of tilting the mast forward or backward to secure loads.',
        'মাল পড়ে যাওয়া রোধে মাস্টকে সামনে বা পেছনের দিকে হেলানোর কাজ।',
      ),
    },
    {
      ja: '最大積載荷重',
      reading: 'さいだいせきさいかじゅう',
      en: 'maximum rated load capacity',
      bn: 'সর্বোচ্চ লোড ক্ষমতা',
      note: t(
        'フォークリフトに安全{あんぜん}に積載{せきさい}できる限界{げんかい}の重量{じゅうりょう}（トンまたはキログラム）。',
        'Maximum limit weight that can be safely loaded on the forklift.',
        'ফর্কলিফ্টে নিরাপদে বহন করার সর্বোচ্চ অনুমোদিত ওজন।',
      ),
    },
    {
      ja: '偏荷重',
      reading: 'へんかじゅう',
      en: 'unbalanced / eccentric load',
      bn: 'ভারসাম্যহীন বা একপাশে হেলে থাকা ভার',
      note: t(
        '荷物{にもつ}の重心{じゅうしん}がパレットやフォークの中央{ちゅうおう}から片側{かたがわ}に偏{かたよ}っている危険{きけん}な状態{じょうたい}。',
        'Hazardous condition where the load center of gravity is off-center on the forks.',
        'ফর্কের ঠিক মাঝখানে না রেখে একপাশে বেশি ওজন হয়ে থাকা বিপজ্জনক অবস্থা।',
      ),
    },
    {
      ja: '後輪操舵',
      reading: 'こうりんそうだ',
      en: 'rear-wheel steering',
      bn: 'পেছনের চাকায় স্টিয়ারিং ব্যবস্থা',
      note: t(
        '後輪{こうりん}で曲{ま}がるフォークリフトの構造{こうぞう}。旋回{せんかい}時{じ}に車体{しゃたい}後部{こうぶ}が大{おお}きく振{ふ}れる。',
        'Steering mechanism using the rear wheels, causing the rear end to swing wide.',
        'পেছনের চাকা ঘুরে দিক বদলানোর ব্যবস্থা, যাতে মোড় নেওয়ার সময় গাড়ির পেছন অংশ বাইরে ছিটকে যায়।',
      ),
    },
    {
      ja: '始業前点検',
      reading: 'しぎょうまえてんけん',
      en: 'pre-shift inspection',
      bn: 'কাজ শুরুর আগের পরিদর্শন',
      note: t(
        '作業{さぎょう}開始{かいし}前{まえ}にブレーキ、油圧{ゆあつ}、タイヤ、警音器{けいおんき}などを毎日{まいにち}点検{てんけん}すること。',
        'Daily inspection of brakes, hydraulics, tires, horn, etc., before starting operations.',
        'প্রতিদিন কাজ শুরুর আগে ব্রেক, হাইড্রলিক্স, টায়ার, হর্ন ইত্যাদি খতিয়ে দেখার নিয়ম।',
      ),
    },
    {
      ja: '後進走行',
      reading: 'こうしんそうこう',
      en: 'reverse driving (backward travel)',
      bn: 'পেছনে চালিয়ে যাওয়া (রিভার্স ড্রাইভ)',
      note: t(
        '積{つ}み荷{に}で前{まえ}が見{み}えない時{とき}に、後方{こうほう}を目視{もくし}で確認{かくにん}しながらバックで走{はし}ること。',
        'Driving in reverse while directly looking behind when forward visibility is blocked.',
        'সামনে মালামালে দৃষ্টি আটকে গেলে সরাসরি পেছনে তাকিয়ে ব্যাক গিয়ারে চালানো।',
      ),
    },
    {
      ja: '誘導者',
      reading: 'ゆうどうしゃ',
      en: 'spotter / signal person',
      bn: 'সংকেতকারী বা গাইডম্যান',
      note: t(
        '見通{みとお}しの悪{わる}い場所{ばしょ}や狭{せま}い通路{つうろ}で、安全{あんぜん}な進行{しんこう}を合図{あいず}で指示{しじ}する人{ひと}。',
        'Person who directs safe vehicle movement with hand signals in narrow or blind areas.',
        'অন্ধকার বা সংকীর্ণ স্থানে নিরাপদ গতিবিধি ইশারা দিয়ে দেখিয়ে দেওয়া দায়িত্বপ্রাপ্ত ব্যক্তি।',
      ),
    },
    {
      ja: '輪留め',
      reading: 'わどめ',
      en: 'wheel chocks',
      bn: 'হুইল চক (চাকার নিচে আটকা দেওয়ার গুটি)',
      note: t(
        '駐車{ちゅうしゃ}中{ちゅう}の車両{しゃりょう}が不意{ふい}に動{うご}き出{だ}さないよう車輪{しゃりん}に噛{か}ませるストッパー。',
        'Stoppers placed against tires to prevent unintentional vehicle rolling while parked.',
        'পার্ক করা গাড়ি যাতে হঠাৎ গড়িয়ে না যায় সেজন্য চাকার নিচে লাগিয়ে রাখা সেফটি ব্লক।',
      ),
    },
  ],
}
