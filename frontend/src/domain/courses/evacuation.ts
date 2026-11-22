import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string, bn: string): Tri => ({ ja, en, bn })

export const evacuation: CourseData = {
  id: 'evacuation',
  category: 'core',
  level: 1,
  minutes: 15,
  title: t(
    '地震{じしん}・津波{つなみ}・火災{かさい}の避難{ひなん}',
    'Evacuation: Earthquake, Tsunami, and Fire',
    'ভূমিকম্প, সুনামি ও অগ্নিকাণ্ডে জরুরি স্থানান্তর',
  ),
  sub: t(
    '災害時{さいがいじ}に命{いのち}を守{まも}る初期行動{しょきこうどう}と安全{あんぜん}な避難{ひなん}手順{てじゅん}。',
    'Initial actions and safe evacuation procedures to protect life during disasters.',
    'দুর্যোগের সময় জীবন বাঁচাতে তাৎক্ষণিক পদক্ষেপ ও নিরাপদ স্থানান্তরের ধারাবাহিক নিয়ম।',
  ),
  refs: [
    '消防法 第8条（消防計画及び消火・通報・避難訓練の実施義務）',
    '労働安全衛生規則 第614条・第615条（安全通路・非常口の確保及び避難設備）',
    '内閣府（防災担当） 津波避難ビル等に係るガイドライン',
  ],
  panels: [
    {
      who: 'foreman',
      text: t(
        '今日{きょう}は地震{じしん}・津波{つなみ}・火災{かさい}の避難{ひなん}訓練{くんれん}を行{おこな}います。非常時{ひじょうじ}に命{いのち}を守{まも}る行動{こうどう}を身{み}につけましょう。',
        'Today we conduct evacuation training for earthquakes, tsunamis, and fires. Learn how to protect your life in emergencies.',
        'আজ আমরা ভূমিকম্প, সুনামি ও অগ্নিকাণ্ডের জরুরি স্থানান্তর মহড়া করব। বিপদের সময় জীবন বাঁচানোর নিয়মগুলো ভালোভাবে শিখে নাও।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'よろしくお願いします！もし大{おお}きな地震{じしん}の揺{ゆ}れを感{かん}じたら、すぐに外{そと}へ走{はし}り出{で}せばいいですか？',
        'Ready! If I feel a major earthquake shaking, should I run outside immediately?',
        'ধন্যবাদ! যদি বড় ধরনের ভূমিকম্পের ঝাঁকুনি অনুভব করি, তবে কি সাথে সাথে বাইরে দৌড়ে যাব?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '慌{あわ}てて外{そと}へ飛{と}び出{だ}してはいけません。まず頭{あたま}を守{まも}り、丈夫{じょうぶ}な机{つくえ}の下{した}に入{はい}るなど低{ひく}い姿勢{しせい}をとってください。',
        'Never rush outside in a panic. First protect your head and stay low, such as taking cover under a sturdy desk.',
        'আতঙ্কিত হয়ে কখনো বাইরে ছুটে বের হবে না। প্রথমে নিজের মাথা বাঁচাও এবং শক্ত টেবিলের নিচে ঢুকে শরীর নিচু করে থাকো।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '分{わ}かりました！揺{ゆ}れがおさまったら、急{いそ}いでエレベーターに乗{の}って地上{ちじょう}へ降{お}りますね。',
        'Understood! Once the shaking stops, I will quickly take the elevator down to the ground floor.',
        'বুঝেছি! ঝাঁকুনি থেমে গেলে দ্রুত লিফটে চড়ে নিচতলায় নেমে যাব।',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'エレベーターは絶対{ぜったい}に使{つか}ってはいけません。停電{ていでん}で閉{と}じ込{こ}められる危険{きけん}があるので、非常階段{ひじょうかいだん}を使{つか}います。',
        'Never use the elevator! There is a high risk of being trapped due to power failure. Always use the emergency stairs.',
        'লিফট কখনো ব্যবহার করবে না! বিদ্যুৎ চলে গিয়ে ভেতরে আটকা পড়ার চরম ঝুঁকি থাকে। সবসময় জরুরি সিঁড়ি ব্যবহার করবে।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '佐世保{させぼ}は海{うみ}が近{ちか}いですが、大{おお}きな揺{ゆ}れのあと津波{つなみ}の危険{きけん}があるときはどうしますか？',
        'Sasebo is near the sea. If there is tsunami danger after a big tremor, what should we do?',
        'সাসেবো সমুদ্রের কাছে। বড় ঝাঁকুনির পর যদি সুনামির আশঙ্কা থাকে, তবে আমরা কী করব?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '「津波{つなみ}てんでんこ」です。指示{しじ}を待{ま}たず、各自{かくじつ}ですぐに高台{たかだい}や津波避難{つなみひなん}ビルへ走{はし}って逃{に}げてください。',
        '"Tsunami tendenko"—do not wait for orders. Everyone must immediately run independently to high ground or a tsunami evacuation building.',
        '"সুনামি তেন্দেনকো"—কারো আদেশের অপেক্ষায় থেকো না। প্রত্যেকে নিজ দায়িত্বে সাথে সাথে উঁচু স্থান বা সুনামি আশ্রয় ভবনে দৌড়ে পালাবে।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '工場{こうじょう}で火災{かさい}を見{み}つけたときは、最初{さいしょ}に何{なに}をすればいいですか？',
        'If I spot a fire in the factory, what is the very first thing I should do?',
        'কারখানায় আগুন লাগতে দেখলে সবার প্রথমে আমার কী করা উচিত?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '大声{おおごえ}で「火事{かじ}だ！」と叫{さけ}び、119番{ばん}通報{つうほう}と初期消火{しょきしょうか}を行{おこな}います。天井{てんじょう}に火{ひ}が届{とど}いたら直{ただ}ちに逃{に}げてください。',
        'Shout "Fire!" loudly, call 119, and attempt initial firefighting. If flames reach the ceiling, evacuate immediately.',
        'উঁচু গলায় "আগুন!" বলে চিৎকার করো, ১১৯ নম্বরে কল দাও এবং প্রাথমিক নেভানোর চেষ্টা করো। আগুন সিলিং স্পর্শ করলে সাথে সাথে পালিয়ে আসো।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '避難場所{ひなんばしょ}へ逃{に}げたら、全員{ぜんいん}が無事{ぶじ}か確認{かくにん}するためすぐに職長{しょくちょう}の点呼{てんこ}を受{う}けます！',
        'Once at the evacuation site, I will immediately report to the foreman for roll call to verify everyone is safe!',
        'নিরাপদ স্থানে পৌঁছানোর পর সবাই অক্ষত আছে কিনা নিশ্চিত করতে সাথে সাথে ফোরম্যানের কাছে রোল কলে হাজিরা দেব!',
      ),
    },
  ],
  handbook: [
    t(
      '緊急地震速報{きんきゅうじしんそくほう}を聞{き}いたら直{ただ}ちに頭{あたま}を保護{ほご}し、落下物{らっかぶつ}のない安全{あんぜん}な場所{ばしょ}で低{ひく}い姿勢{しせい}をとる。',
      'When hearing the Earthquake Early Warning, protect your head immediately and stay low in a safe spot away from falling objects.',
      'ভূমিকম্পের জরুরি সতর্কবার্তা শোনামাত্র মাথা বাঁচাও এবং ওপর থেকে জিনিস পড়ার ঝুঁকিহীন স্থানে শরীর নিচু করে থাকো।',
    ),
    t(
      '激{はげ}しい揺{ゆ}れの最中{さいちゅう}は慌{あわ}てて外{そと}へ飛{と}び出{だ}さず、揺{ゆ}れがおさまってから安全{あんぜん}に通路{つうろ}へ出{で}る。',
      'Never rush outside during severe shaking; wait until shaking stops before moving calmly into walkways.',
      'তীব্র ঝাঁকুনির সময় আতঙ্কিত হয়ে বাইরে ছুটে যাবে না; ঝাঁকুনি থামার পর সাবধানে নিরাপদ চলাচলের পথে বের হও।',
    ),
    t(
      '地震{じしん}や火災{かさい}の際{さい}にエレベーターを使{つか}ってはならない（停電{ていでん}で閉{と}じ込{こ}められるため非常階段{ひじょうかいだん}を使{つか}う）。',
      'Never use elevators during earthquakes or fires; use emergency stairs to avoid getting trapped by power failures.',
      'ভূমিকম্প বা আগুনে কখনো লিফট ব্যবহার করবে না; বিদ্যুৎ বিভ্রাটে আটকা পড়া এড়াতে জরুরি সিঁড়ি ব্যবহার করো।',
    ),
    t(
      '沿岸部{えんがんぶ}で強{つよ}い揺{ゆ}れを感{かん}じたら、津波警報{つなみけいほう}の前{まえ}でも直{ただ}ちに高台{たかだい}へ避難{ひなん}する（津波{つなみ}てんでんこ）。',
      'If you feel strong shaking in coastal areas, evacuate immediately to high ground even before tsunami warnings sound (Tsunami Tendenko).',
      'উপকূলীয় এলাকায় তীব্র ঝাঁকুনি অনুভূত হলে সুনামির সাইরেন বাজার আগেই অবিলম্বে উঁচু স্থানে সরে যাও (সুনামি তেন্দেনকো)।',
    ),
    t(
      '命{いのち}を守{まも}る「緊急避難場所{きんきゅうひなんばしょ}」と被災後{ひさいご}に生活{せいかつ}する「避難所{ひなんじょ}」の違{ちが}いと位置{いち}を事前{じぜん}に確認{かくにん}しておく。',
      'Check in advance the locations and difference between emergency evacuation sites (for immediate survival) and shelters (for staying post-disaster).',
      'জীবনরক্ষাকারী জরুরি এলাকা (তাৎক্ষণিক রক্ষা) ও আশ্রয়কেন্দ্রের (দুর্যোগপরবর্তী থাকার জায়গা) পার্থক্য ও অবস্থান আগেই জেনে রাখো।',
    ),
    t(
      '火災{かさい}を発見{はっけん}したら、大声{おおごえ}で「火事{かじ}だ！」と叫{さけ}び、直{ただ}ちに119番{ばん}へ通報{つうほう}する。',
      'Upon discovering a fire, shout "Fire!" loudly to alert everyone and report to 119 immediately.',
      'আগুন দেখতে পেলে চিৎকার করে সবাইকে সতর্ক করো এবং তৎক্ষণাৎ দমকলের ১১৯ নম্বরে ফোন দাও।',
    ),
    t(
      '消火器{しょうかき}による初期消火{しょきしょうか}は炎{ほのお}が天井{てんじょう}に届{とど}く前{まえ}までとし、天井{てんじょう}に燃{も}え移{うつ}ったらすぐ避難{ひなん}する。',
      'Initial firefighting with extinguishers is only effective before flames reach the ceiling; evacuate immediately if flames touch the ceiling.',
      'অগ্নিনির্বাপক দিয়ে নেভানোর চেষ্টা কেবল সিলিংয়ে আগুন লাগার আগ পর্যন্তই সীমিত; সিলিংয়ে আগুন ছড়িয়ে পড়লে সাথে সাথে পালাও।',
    ),
    t(
      '煙{けむり}の中{なか}を避難{ひなん}するときは、濡{ぬ}れタオルや衣服{いふく}で口{くち}と鼻{はな}を覆{おお}い、姿勢{しせい}を低{ひく}くして壁{かべ}沿{づた}いに進{すす}む。',
      'When evacuating through smoke, cover mouth and nose with a damp cloth or clothing, keep a low posture, and follow walls.',
      'ধোঁয়ার ভেতর দিয়ে বের হওয়ার সময় ভেজা কাপড় দিয়ে নাক-মুখ ঢাকো, নিচু হয়ে থাকো এবং দেয়াল ঘেঁষে সামনে এগোও।',
    ),
    t(
      '避難場所{ひなんばしょ}に到着{とうちゃく}したら、直{ただ}ちに職長{しょくちょう}の点呼{てんこ}を受{う}け、逃{に}げ遅{おく}れや怪我人{けがにん}の有無{うむ}を報告{ほうこく}する。',
      'Upon arriving at the evacuation site, immediately report to the foreman for roll call to confirm all coworkers are accounted for.',
      'নিরাপদ স্থানে পৌঁছানো মাত্র ফোরম্যানের কাছে রোল কলে উপস্থিত হও এবং কেউ নিখোঁজ বা আহত আছে কিনা তা নিশ্চিত করো।',
    ),
    t(
      '工場内{こうじょうない}の避難経路図{ひなんけいろず}、非常口{ひじょうぐち}、消火器{しょうかき}の設置{せっち}場所{ばしょ}を日頃{ひごろ}から把握{はあく}しておく。',
      'Familiarize yourself daily with the factory evacuation route map, emergency exits, and fire extinguisher stations.',
      'কারখানার জরুরি নির্গমন মানচিত্র, জরুরি দরজা এবং অগ্নিনির্বাপকের অবস্থান সম্পর্কে প্রতিদিন ওয়াকিবহাল থাকো।',
    ),
  ],
  reel: [
    t(
      '緊急地震速報{きんきゅうじしんそくほう}、頭{あたま}を保護{ほご}、ヨシ！',
      'Earthquake warning, protect head, yoshi!',
      'ভূমিকম্প সতর্কবার্তা, মাথা সুরক্ষিত, ঠিক আছে!',
    ),
    t(
      '非常口{ひじょうぐち}、障害物{しょうがいぶつ}なし、ヨシ！',
      'Emergency exit, clear of obstacles, yoshi!',
      'জরুরি বহির্গমন পথ, বাধাহীন, ঠিক আছে!',
    ),
    t(
      'エレベーター不使用{ふしよう}、非常階段{ひじょうかいだん}、ヨシ！',
      'No elevator, use emergency stairs, yoshi!',
      'লিফট ব্যবহার বন্ধ, জরুরি সিঁড়ি, ঠিক আছে!',
    ),
    t(
      '津波{つなみ}警報{けいほう}、高台{たかだい}へ避難{ひなん}、ヨシ！',
      'Tsunami warning, evacuate to high ground, yoshi!',
      'সুনামি সতর্কতা, উঁচু স্থানে স্থানান্তর, ঠিক আছে!',
    ),
    t(
      '火災{かさい}発見{はっけん}、大声{おおごえ}・119番{ばん}通報{つうほう}、ヨシ！',
      'Fire spotted, shout and call 119, yoshi!',
      'আগুন দেখা গেছে, চিৎকার ও ১১৯ নম্বরে কল, ঠিক আছে!',
    ),
    t(
      '消火器{しょうかき}操作{そうさ}、ピン・ホース・レバー、ヨシ！',
      'Extinguisher operation: pin, hose, lever, yoshi!',
      'অগ্নিনির্বাপক পরিচালনা: পিন, পাইপ, লিভার, ঠিক আছে!',
    ),
    t(
      '避難場所{ひなんばしょ}到着{とうちゃく}、点呼{てんこ}報告{ほうこく}、ヨシ！',
      'Arrived at evacuation site, roll call reported, yoshi!',
      'নিরাপদ স্থানে আগমন, রোল কল রিপোর্ট সম্পন্ন, ঠিক আছে!',
    ),
  ],
  drills: [
    {
      id: 'evacuation-route-hazard-spot',
      kind: 'spot',
      secs: 30,
      need: 4,
      name: t(
        '避難通路{ひなんつうろ}と非常口{ひじょうぐち}の危険{きけん}箇所{かしょ}チェック',
        'Evacuation route and emergency exit hazard spotting',
        'জরুরি বহির্গমন পথ ও দরজার বিপদ চিহ্নিতকরণ',
      ),
      goal: t(
        '30秒{びょう}で避難{ひなん}の妨{さまた}げとなる危険{きけん}な状態{じょうたい}を4つ見{み}つける。',
        'Find 4 hazards blocking evacuation within 30 seconds.',
        '৩০ সেকেন্ডের মধ্যে জরুরি স্থানান্তরে বাধা সৃষ্টিকারী ৪টি ঝুঁকি খুঁজে বের করো।',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '非常口{ひじょうぐち}の扉{とびら}の前{まえ}に積{つ}み上{あ}げられたパレット',
            'Wooden pallets stacked in front of emergency exit door',
            'জরুরি দরজার সামনে স্তূপ করে রাখা কাঠের প্যালেট',
          ),
        },
        {
          hazard: false,
          label: t(
            '緑色{みどりいろ}に点灯{てんとう}している非常口{ひじょうぐち}の誘導灯{ゆうどうとう}',
            'Emergency exit sign clearly illuminated in green',
            'সবুজ বাতিতে উজ্জ্বলভাবে জ্বলতে থাকা জরুরি দিকনির্দেশক বাতি',
          ),
        },
        {
          hazard: true,
          label: t(
            '地震{じしん}で倒{たお}れそうな固定{こてい}されていない高{たか}い棚{たな}',
            'Tall unanchored shelf likely to topple in an earthquake',
            'ভূমিকম্পে আছড়ে পড়ার মতো ঝুঁকিপূর্ণ আলগা উঁচু তাক',
          ),
        },
        {
          hazard: false,
          label: t(
            '見{み}やすい壁{かべ}に掲示{けいじ}された工場{こうじょう}の避難経路図{ひなんけいろず}',
            'Factory evacuation route map posted visibly on the wall',
            'সহজে চোখে পড়ার মতো দেয়ালে টানানো কারখানার জরুরি নির্গমন মানচিত্র',
          ),
        },
        {
          hazard: true,
          label: t(
            '避難通路{ひなんつうろ}の真{ま}ん中{なか}に放置{ほうち}された運搬{うんぱん}台車{だいしゃ}',
            'Transport cart left abandoned in the middle of evacuation walkway',
            'জরুরি চলাচলের পথের মাঝখানে ফেলে রাখা হ্যান্ড ট্রলি',
          ),
        },
        {
          hazard: false,
          label: t(
            '障害物{しょうがいぶつ}がなく手{て}すりがしっかりした非常階段{ひじょうかいだん}',
            'Emergency stairs free of clutter with sturdy handrails',
            'বাধামুক্ত এবং মজবুত হাতলযুক্ত জরুরি বহির্গমন সিঁড়ি',
          ),
        },
        {
          hazard: true,
          label: t(
            '火災発生時{かさいはっせいじ}にエレベーターに乗{の}り込{こ}もうとする人{ひと}',
            'Person attempting to enter an elevator during a fire',
            'আগুন লাগার সময় লিফটে ওঠার চেষ্টা করা ব্যক্তি',
          ),
        },
        {
          hazard: false,
          label: t(
            '定位置{ていいち}に置{お}かれ点検{てんけん}カードが付{つ}いた消火器{しょうかき}',
            'Fire extinguisher in designated spot with inspection tag attached',
            'নির্দিষ্ট স্থানে রাখা এবং পরিদর্শন কার্ড লাগানো অগ্নিনির্বাপক যন্ত্র',
          ),
        },
      ],
    },
    {
      id: 'fire-extinguisher-operation-seq',
      kind: 'seq',
      name: t(
        '消火器{しょうかき}の正{ただ}しい操作{そうさ}手順{てじゅん}',
        'Correct fire extinguisher operating sequence',
        'অগ্নিনির্বাপক ব্যবহারের সঠিক ধারাবাহিক ধাপ',
      ),
      goal: t(
        '消火器{しょうかき}を使{つか}って火{ひ}を消{け}す手順{てじゅん}を順番{じゅんばん}に並{なら}べる。',
        'Arrange the steps to operate a fire extinguisher in the correct order.',
        'অগ্নিনির্বাপক দিয়ে আগুন নেভানোর ধাপগুলো সঠিক ক্রমে সাজাও।',
      ),
      steps: [
        t(
          '1. 消火器{しょうかき}を火元{ひもと}の手前{てまえ}（3〜5メートル）まで運{はこ}ぶ', // verify: 消火器放射時の推奨初期距離（3〜5m）
          '1. Carry the fire extinguisher to 3-5 meters in front of the fire',
          '১. অগ্নিনির্বাপকটি আগুনের ৩-৫ মিটার সামনে নিয়ে এসো',
        ),
        t(
          '2. 黄色{きいろ}の安全{あんぜん}ピンを上{うえ}に向{む}かって引{ひ}き抜{ぬ}く',
          '2. Pull out the yellow safety pin straight upward',
          '২. হলুদ সুরক্ষা পিনটি ওপরের দিকে টেনে খুলে ফেলো',
        ),
        t(
          '3. ノズルやホースを外{はず}して先端{せんたん}を火{ひ}の根元{ねもと}に向{む}ける',
          '3. Detach the hose and aim the nozzle directly at the base of the fire',
          '৩. পাইপটি খুলে তার মুখ সরাসরি আগুনের গোড়ার দিকে তাক করো',
        ),
        t(
          '4. レバーを強{つよ}く握{にぎ}り、ほうきで掃{は}くように左右{さゆう}に放射{ほうしゃ}する',
          '4. Squeeze the lever firmly and sweep the nozzle from side to side',
          '৪. লিভার শক্তভাবে চেপে ধরে ঝাড়ু দেওয়ার মতো ডানে-বায়ে ছিটাও',
        ),
      ],
    },
    {
      id: 'tsunami-evacuation-seq',
      kind: 'seq',
      name: t(
        '沿岸部{えんがんぶ}での大津波{おおつなみ}避難{ひなん}手順{てじゅん}',
        'Tsunami evacuation sequence for coastal areas',
        'উপকূলীয় এলাকায় বড় সুনামির সময় স্থানান্তরের ক্রম',
      ),
      goal: t(
        '大地震{おおじしん}の揺{ゆ}れがおさまった後{のち}の津波{つなみ}避難{ひなん}手順{てじゅん}を正{ただ}しい順番{じゅんばん}に並{なら}べる。',
        'Arrange tsunami evacuation steps in the correct order after strong shaking.',
        'বড় ভূমিকম্পের ঝাঁকুনি থামার পর সুনামির সময় নিরাপদ আশ্রয়ে যাওয়ার ধাপগুলো সাজাও।',
      ),
      steps: [
        t(
          '1. 激{はげ}しい揺{ゆ}れがおさまったら直{ただ}ちに作業{さぎょう}を止{と}めて機械{きかい}を停止{ていし}する',
          '1. Stop work immediately and shut down machines once intense shaking ceases',
          '১. তীব্র ঝাঁকুনি থামার সাথে সাথে কাজ বন্ধ করো এবং মেশিন বন্ধ করো',
        ),
        t(
          '2. 荷物{にもつ}を取{と}りに戻{もど}らず、ヘルメットを着用{ちゃくよう}して周囲{しゅうい}に避難{ひなん}を呼{よ}びかける',
          '2. Do not return for belongings; wear your helmet and call out to coworkers to evacuate',
          '২. মালামাল নিতে ফিরে যাবে না; হেলমেট পরে সবাইকে সাথে নিয়ে সরার ডাক দাও',
        ),
        t(
          '3. 海岸{かいがん}や河川{かせん}から離{はな}れ、高台{たかだい}や指定津波避難{していつなみひなん}ビルへ走{はし}って逃{に}げる（津波{つなみ}てんでんこ）',
          '3. Run away from the coast and rivers towards high ground or designated tsunami evacuation buildings (Tsunami Tendenko)',
          '৩. নদী ও সমুদ্র থেকে দূরে উঁচু পাহাড়ি এলাকা বা নির্ধারিত সুনামি আশ্রয় ভবনে দৌড়ে পালাও (সুনামি তেন্দেনকো)',
        ),
        t(
          '4. 避難場所{ひなんばしょ}に到着{とうちゃく}後{ご}、直{ただ}ちに点呼{てんこ}を受{う}けて全員{ぜんいん}の安否{あんぴ}を職長{しょくちょう}へ報告{ほうこく}する',
          '4. Upon reaching safety, immediately participate in roll call and report headcount to the foreman',
          '৪. নিরাপদ স্থানে পৌঁছে অবিলম্বে রোল কলে অংশ নাও এবং ফোরম্যানের কাছে সবার উপস্থিতি নিশ্চিত করো',
        ),
      ],
    },
    {
      id: 'emergency-119-call-seq',
      kind: 'seq',
      name: t(
        '119番{ばん}消防{しょうぼう}通報{つうほう}の手順{てじゅん}',
        '119 emergency call reporting sequence',
        '১১৯ জরুরি দমকল কল করার ধারাবাহিক নিয়ম',
      ),
      goal: t(
        '火災{かさい}を発見{はっけん}したときに119番{ばん}へ正確{せいかく}に通報{つうほう}する手順{てじゅん}を並{なら}べる。',
        'Arrange the steps to report a fire accurately when calling 119.',
        'আগুন লাগার খবর সঠিকভাবে দমকলের ১১৯ নম্বরে জানানোর ধাপগুলো সাজাও।',
      ),
      steps: [
        t(
          '1. 119番{ばん}にダイヤルし、消防署{しょうぼうしょ}に「火事{かじ}です」とはっきり伝{つた}える',
          '1. Dial 119 and clearly state "It is a fire" to the dispatcher',
          '১. ১১৯ নম্বরে ডায়াল করে অপারেটরকে স্পষ্ট করে বলো "আগুন লেগেছে"',
        ),
        t(
          '2. 工場{こうじょう}の正確{せいかく}な住所{じゅうしょ}と会社名{かいしゃめい}・建物名{たてものめい}を伝{つた}える',
          '2. Provide the exact factory address, company name, and building identification',
          '২. কারখানার সঠিক ঠিকানা, কোম্পানির নাম এবং ভবনের নাম বলো',
        ),
        t(
          '3. 何{なに}が燃{も}えているかと、逃{に}げ遅{おく}れや怪我人{けがにん}がいるかを伝{つた}える',
          '3. Explain what is burning and whether anyone is trapped or injured',
          '৩. কীসে আগুন লেগেছে এবং কেউ ভেতরে আটকা পড়েছে বা আহত হয়েছে কিনা তা জানাও',
        ),
        t(
          '4. 自分{じぶん}の名前{なまえ}と現在{げんざい}の電話番号{でんわばんごう}を伝{つた}え、消防{しょうぼう}の指示{しじ}に従{したが}う',
          '4. State your name and phone number, then follow the dispatcher instructions',
          '৪. নিজের নাম ও ফোন নম্বর বলো এবং দমকল অপারেটরের নির্দেশ মেনে চলো',
        ),
      ],
    },
  ],
  inspect: [
    {
      id: 'fire-extinguisher-station',
      defective: false,
      name: t(
        '消火器{しょうかき}の日常{にちじょう}外観{がいかん}点検{てんけん}',
        'Daily visual inspection of fire extinguisher',
        'অগ্নিনির্বাপক যন্ত্রের দৈনিক চাক্ষুষ পরিদর্শন',
      ),
      spots: [
        {
          x: 45,
          y: 20,
          check: t(
            '安全{あんぜん}ピン：黄色{きいろ}のピンが確実{かくじつ}に差{さ}し込{こ}まれ、封印{ふういん}が付{つ}いているか（良品{りょうひん}：脱落{だつらく}なし / 不良{ふりょう}：ピン外{はず}れ）',
            'Safety pin: Yellow pin securely inserted with unbroken seal (Good: Intact / Defect: Missing pin)',
            'সুরক্ষা পিন: হলুদ পিনটি সিলসহ সঠিকভাবে লাগানো আছে কিনা (ভালো: অক্ষত / ত্রুটি: পিন খোলা)',
          ),
        },
        {
          x: 35,
          y: 45,
          check: t(
            '圧力計{あつりょくけい}：指針{ししん}が緑色{みどりいろ}の正常{せいじょう}ゾーン（0.7〜0.98MPa）を指{さ}しているか（良品{りょうひん}：緑色{みどりいろ}枠内{わくない} / 不良{ふりょう}：圧力{あつりょく}低下{ていか}）', // verify: 蓄圧式消火器の指示圧力計の緑色ゾーン（0.7〜0.98MPa）
            'Pressure gauge: Indicator points to green normal zone (0.7-0.98 MPa) (Good: In green zone / Defect: Low pressure)',
            'প্রেসার গেজ: কাঁটা সবুজ স্বাভাবিক জোনে (০.৭-০.৯৮ এমপিএ) আছে কিনা (ভালো: সবুজ জোনে / ত্রুটি: চাপ কমে যাওয়া)',
          ),
        },
        {
          x: 55,
          y: 75,
          check: t(
            'ホース・容器{ようき}：ひび割{わ}れやサビ、ノズルの詰{つ}まりがないか（良品{りょうひん}：異状{いじょう}なし / 不良{ふりょう}：腐食{ふしょく}・亀裂{きれつ}）',
            'Hose and body: No cracking, rust, or blocked nozzle (Good: No damage / Defect: Corroded or cracked)',
            'পাইপ ও বডি: ফাটল, মরিচা বা নজল আটকে নেই তো (ভালো: কোনো ত্রুটি নেই / ত্রুটি: মরিচা বা ফাটল)',
          ),
        },
      ],
    },
    {
      id: 'emergency-exit-door-and-path',
      defective: true,
      name: t(
        '非常口{ひじょうぐち}扉{とびら}および避難通路{ひなんつうろ}の点検{てんけん}',
        'Emergency exit door and evacuation path inspection',
        'জরুরি বহির্গমন দরজা ও নির্গমন পথের পরিদর্শন',
      ),
      spots: [
        {
          x: 30,
          y: 35,
          check: t(
            '誘導灯{ゆうどうとう}：緑色{みどりいろ}の避難口{ひなんぐち}マークが正常{せいじょう}に点灯{てんとう}しているか（良品{りょうひん}：常時点灯{じょうじてんとう} / 不良{ふりょう}：消灯{しょうとう}・点滅{てんめつ}）',
            'Exit light: Green exit sign properly illuminated (Good: Steadily lit / Defect: Off or blinking)',
            'দিকনির্দেশক বাতি: সবুজ জরুরি বাতিটি সঠিকভাবে জ্বলছে কিনা (ভালো: সার্বক্ষণিক জ্বলছে / ত্রুটি: বন্ধ বা টিমটিমে)',
          ),
        },
        {
          x: 70,
          y: 65,
          check: t(
            '扉{とびら}の前{まえ}の空間{くうかん}：段{だん}ボールやパレットで開口部{かいこうぶ}がふさがれていないか（良品{りょうひん}：開放{かいほう}クリア / 不良{ふりょう}：荷物{にもつ}で閉鎖{へいさ}）',
            'Space in front of door: Exit opening not obstructed by boxes or pallets (Good: Fully clear / Defect: Blocked by cargo)',
            'দরজার সামনের স্থান: কার্টন বা প্যালেট দিয়ে দরজা আটকে রাখা হয়নি তো (ভালো: সম্পূর্ণ মুক্ত / ত্রুটি: মালামাল দিয়ে বন্ধ)',
          ),
        },
      ],
    },
    {
      id: 'evacuation-map-and-kit',
      defective: false,
      name: t(
        '避難経路図{ひなんけいろず}と非常持出具{ひじょうもちだしぐ}の確認{かくにん}',
        'Evacuation route map and emergency kit inspection',
        'জরুরি নির্গমন মানচিত্র ও জরুরি সরঞ্জামের পরিদর্শন',
      ),
      spots: [
        {
          x: 40,
          y: 35,
          check: t(
            '避難経路図{ひなんけいろず}：現在地{げんざいち}、非常口{ひじょうぐち}、集合場所{しゅうごうばしょ}が明記{めいき}されているか（良品{りょうひん}：鮮明{せんめい}に表示{ひょうじ} / 不良{ふりょう}：破損{はそん}・脱落{だつらく}）',
            'Evacuation map: Current location, exits, and rally points clearly indicated (Good: Clearly marked / Defect: Missing or damaged)',
            'নির্গমন মানচিত্র: বর্তমান অবস্থান, বহির্গমন পথ ও জমা হওয়ার স্থান স্পষ্ট তো (ভালো: স্পষ্ট চিহ্নিত / ত্রুটি: ছেঁড়া বা নিখোঁজ)',
          ),
        },
        {
          x: 65,
          y: 75,
          check: t(
            '非常用{ひじょうよう}ライト・メガホン：点検済{てんけんず}みで電池{でんち}が切{き}れていないか（良品{りょうひん}：点灯{てんとう}・動作{どうさ}良好{りょうこう} / 不良{ふりょう}：作動{さどう}せず）',
            'Emergency flashlight and megaphone: Inspected with working batteries (Good: Working properly / Defect: Dead battery)',
            'জরুরি লাইট ও মেগাফোন: ব্যাটারি চার্জযুক্ত ও কার্যকর আছে কিনা (ভালো: চমৎকার কাজ করছে / ত্রুটি: ব্যাটারি মৃত)',
          ),
        },
      ],
    },
  ],
  quiz: [
    {
      q: t(
        '作業中{さぎょうちゅう}に大{おお}きな地震{じしん}の揺{ゆ}れを感{かん}じました。最初{さいしょ}にとるべき最優先{さいゆうせん}の行動{こうどう}はどれですか？',
        'You feel a major earthquake shaking during work. What is the top-priority action to take first?',
        'কাজের সময় বড় ধরনের ভূমিকম্পের ঝাঁকুনি অনুভব করলেন। সবার প্রথমে সবচেয়ে জরুরি কোন কাজটি করবেন?',
      ),
      options: [
        t(
          'その場{ば}で身{み}を低{ひく}くし、ヘルメットや頑丈{がんじょう}な机{つくえ}で頭{あたま}を守{まも}る',
          'Drop low on the spot and protect your head with a helmet or under a sturdy desk',
          'সেখানেই শরীর নিচু করুন এবং হেলমেট বা শক্ত টেবিলের নিচে মাথা বাঁচিয়ে থাকুন',
        ),
        t(
          '財布{さいふ}やスマートフォンを探{さが}して急{いそ}いでカバンに詰{つ}め込{こ}む',
          'Look for your wallet and smartphone and quickly pack them into your bag',
          'মানিব্যাগ বা স্মার্টফোন খুঁজে বের করে দ্রুত ব্যাগে ভরতে থাকুন',
        ),
        t(
          '慌{あわ}てて工場{こうじょう}の出入口{でいりぐち}に向{む}かって全力{ぜんりょく}で走{はし}り出{で}す',
          'Panic and run at full speed towards the factory exit immediately',
          'আতঙ্কিত হয়ে সাথে সাথে কারখানার দরজার দিকে দৌড়ে ছুটে যান',
        ),
        t(
          '揺{ゆ}れている最中{さいちゅう}に機械{きかい}の調整{ちょうせい}や加工作業{かこうさぎょう}を続{つづ}ける',
          'Continue machining or adjustments while the building is actively shaking',
          'ঝাঁকুনির মধ্যেই মেশিনের কাজ বা পার্টস ঠিক করার কাজ চালিয়ে যান',
        ),
      ],
      answer: 0,
      why: t(
        '地震{じしん}の初期{しょき}は落下物{らっかぶつ}による怪我{けが}が多{おお}いため、まず低{ひく}い姿勢{しせい}で頭{あたま}を守{まも}ることが最優先{さいゆうせん}です。',
        'In the initial moments of an earthquake, head injuries from falling objects are the biggest risk; stay low and protect your head first.',
        'ভূমিকম্পের প্রথম মুহূর্তে উপর থেকে জিনিস পড়ে মাথায় আঘাত লাগার ঝুঁকি সবচেয়ে বেশি; তাই শরীর নিচু করে সবার আগে মাথা বাঁচানো প্রধান দায়িত্ব।',
      ),
    },
    {
      q: t(
        '大地震{おおじしん}の揺{ゆ}れがおさまった後{のち}、工場{こうじょう}の3階{かい}から地上{ちじょう}へ避難{ひなん}します。正{ただ}しい移動方法{いどうほうほう}はどれですか？',
        'After strong earthquake shaking ceases, you evacuate from the 3rd floor to the ground. What is the correct way to move?',
        'ভূমিকম্পের ঝাঁকুনি থেমে যাওয়ার পর ৩য় তলা থেকে নিচে নেমে নিরাপদ আশ্রয়ে যাবেন। সঠিক উপায় কোনটি?',
      ),
      options: [
        t(
          '最{もっと}も速{はや}く降{お}りられる工場{こうじょう}のエレベーターを使{つか}う',
          'Use the factory elevator to descend as quickly as possible',
          'সবচেয়ে দ্রুত নামার জন্য কারখানার লিফট ব্যবহার করা',
        ),
        t(
          '非常階段{ひじょうかいだん}を使{つか}い、手{て}すりにつかまりながら足元{あしもと}に注意{ちゅうい}して降{お}りる',
          'Use the emergency stairs, holding the handrail and watching your step carefully',
          'জরুরি সিঁড়ি ব্যবহার করে হাতল ধরে সাবধানে পা ফেলে নিচে নামা',
        ),
        t(
          '窓{まど}から下{した}の地面{じめん}へ直接{ちょくせつ}飛{と}び降{お}りる',
          'Jump directly from the windows down to the ground below',
          'জানালা দিয়ে সরাসরি নিচের মাটিতে লাফিয়ে পড়া',
        ),
        t(
          '工場{こうじょう}の荷物用{にもつよう}リフトに乗{の}り込{こ}んで降{お}りる',
          'Board the freight cargo lift to descend to the ground',
          'মালামাল ওঠানামার কার্গো লিফটে চড়ে নিচে নামা',
        ),
      ],
      answer: 1,
      why: t(
        '地震{じしん}直後{ちょくご}は停電{ていでん}やレール破損{はそん}で閉{と}じ込{こ}められるため、エレベーターは絶対{ぜったい}に使{つか}わず非常階段{ひじょうかいだん}を使{つか}います。',
        'Elevators must never be used after earthquakes due to power outages and entrapment risks; always take the emergency stairs.',
        'ভূমিকম্পের পরপরই বিদ্যুৎ বিভ্রাট বা ট্র্যাক নষ্ট হয়ে লিফটে আটকা পড়ার চরম ঝুঁকি থাকে; তাই লিফট বর্জন করে সর্বদা জরুরি সিঁড়ি দিয়ে নামতে হবে।',
      ),
    },
    {
      q: t(
        '海沿{うみぞ}いの工場{こうじょう}で大{おお}きな揺{ゆ}れを感{かん}じました。「津波{つなみ}てんでんこ」の教訓{きょうくん}に沿{そ}った行動{こうどう}はどれですか？',
        'You feel strong shaking at a coastal shipyard. Which action follows the principle of "Tsunami Tendenko"?',
        'উপকূলের কারখানায় তীব্র ঝাঁকুনি অনুভব করলেন। "সুনামি তেন্দেনকো" নীতির সাথে সংগতিপূর্ণ আচরণ কোনটি?',
      ),
      options: [
        t(
          '事務所{じむしょ}で津波警報{つなみけいほう}の放送{ほうそう}があるまで作業{さぎょう}を続{つづ}けて待機{たいき}する',
          'Continue working in the office and wait until official tsunami warning broadcasts begin',
          'অফিসে কাজ চালিয়ে যাওয়া এবং সুনামির সতর্কবার্তা ঘোষণা না হওয়া পর্যন্ত অপেক্ষা করা',
        ),
        t(
          '自家用車{じかようしゃ}に荷物{にもつ}を積{つ}み込{こ}んで一般道路{いっぱんどうろ}を運転{うんてん}して避難{ひなん}する',
          'Load personal luggage into a private car and drive along public roads to flee',
          'ব্যক্তিগত গাড়িতে মালামাল তুলে পাবলিক রাস্তা দিয়ে গাড়ি চালিয়ে পালানো',
        ),
        t(
          '人{ひと}を待{ま}たず指示{しじ}を待{ま}たず、各自{かくじつ}ですぐに近{ちか}くの高台{たかだい}や津波避難{つなみひなん}ビルへ逃{に}げる',
          'Without waiting for others or orders, everyone flees immediately to nearby high ground or tsunami evacuation buildings',
          'কারো জন্য অপেক্ষা না করে বা নির্দেশের অপেক্ষায় না থেকে প্রত্যেকে সাথে সাথে নিকটস্থ উঁচু পাহাড় বা সুনামি আশ্রয় ভবনে পালিয়ে যাওয়া',
        ),
        t(
          '津波{つなみ}が本当{ほんとう}に来{く}るか確認{かくにん}するため海岸{かいがん}の岸壁{がんぺき}を見{み}に行{い}く',
          'Walk down to the coastal quay to observe if the sea water is actually receding',
          'সুনামি সত্যি আসছে কিনা তা দেখার জন্য সমুদ্রের পাড়ে বা জেটিতে যাওয়া',
        ),
      ],
      answer: 2,
      why: t(
        '「津波{つなみ}てんでんこ」とは、津波{つなみ}の危険{きけん}があるときは指示{しじ}や人{ひと}を待{ま}たず、各自{かくじつ}が直{ただ}ちに高台{たかだい}へ命{いのち}がけで避難{ひなん}する心得{こころえ}です。',
        '"Tsunami Tendenko" teaches that when tsunami threatens, each person must flee immediately to high ground without waiting for anyone.',
        '"সুনামি তেন্দেনকো" নীতি শেখায় যে সুনামির ঝুঁকি দেখা দিলে কারো নির্দেশের অপেক্ষা না করে প্রত্যেকে নিজ দায়িত্বে তাৎক্ষণিক উঁচু স্থানে প্রাণপণে ছুটে যাবে।',
      ),
    },
    {
      q: t(
        '自治体{じちたい}が指定{してい}する「指定緊急避難場所{していきんきゅうひなんばしょ}」と「指定避難所{していひなんじょ}」の違{ちが}いについて正{ただ}しい説明{せつめい}はどれですか？',
        'Which explanation correctly describes the difference between a Designated Emergency Evacuation Site and a Designated Evacuation Shelter?',
        'স্থানীয় সরকার কর্তৃক নির্ধারিত "জরুরি জীবনরক্ষা এলাকা" এবং "জরুরি আশ্রয়কেন্দ্রের" মধ্যকার পার্থক্যের সঠিক বর্ণনা কোনটি?',
      ),
      options: [
        t(
          '名称{めいしょう}が違{ちが}うだけで施設{しせつ}の役割{やくわり}は完全{かんぜん}に同{おな}じである',
          'Only the names differ; the actual function and facilities are completely identical',
          'শুধু নামে পার্থক্য রয়েছে; তাদের কাজ এবং সুবিধা সম্পূর্ণ একই রকম',
        ),
        t(
          '「指定緊急避難場所{していきんきゅうひなんばしょ}」は被災後{ひさいご}に長期間{ちょうきかん}生活{せいかつ}するための宿泊{しゅくはく}施設{しせつ}である',
          'The Emergency Evacuation Site is a residential lodging facility for long-term stay after disasters',
          'জরুরি জীবনরক্ষা এলাকা হলো দুর্যোগের পর দীর্ঘদিন থাকার জন্য একটি আবাসিক ব্যবস্থা',
        ),
        t(
          '「指定避難所{していひなんじょ}」は津波{つなみ}や地震{じしん}の瞬間{しゅんかん}に命{いのち}を守{まも}るために逃{に}げ込{こ}む高台{たかだい}の広場{ひろば}である',
          'The Designated Evacuation Shelter is an open hilltop plaza to run to at the very moment of a tsunami',
          'জরুরি আশ্রয়কেন্দ্র হলো সুনামির মুহূর্তে তৎক্ষণাৎ ছুটে যাওয়ার মতো পাহাড়ের খোলা চত্বর',
        ),
        t(
          '「避難場所{ひなんばしょ}」は命{いのち}を守{まも}るため緊急{きんきゅう}に逃{に}げる場所{ばしょ}で、「避難所{ひなんじょ}」は自宅{じたく}が被災{ひさい}した人{ひと}が一定期間{いっていきかん}生活{せいかつ}する施設{しせつ}である',
          'The Evacuation Site is where you escape immediately to save life, while the Shelter is where displaced people temporarily live',
          'জীবনরক্ষা এলাকা হলো প্রাণ বাঁচাতে তৎক্ষণাৎ ছুটে যাওয়ার জায়গা, আর আশ্রয়কেন্দ্র হলো যাদের ঘরবাড়ি ক্ষতিগ্রস্ত হয়েছে তাদের সাময়িক বসবাসের স্থান',
        ),
      ],
      answer: 3,
      why: t(
        '災害発生{さいがいはっせい}時{じ}はまず命{いのち}を守{まも}る緊急避難場所{きんきゅうひなんばしょ}へ逃{に}げ、安全{あんぜん}確保後{かくほご}に生活{せいかつ}の場{ば}である避難所{ひなんじょ}（体育館{たいいくかん}等）へ移動{いどう}します。',
        'During an emergency, first flee to an evacuation site to save lives; after danger passes, people move to shelters (like gymnasiums) for lodging.',
        'দুর্যোগ শুরু হলে প্রথমে জীবন বাঁচাতে জরুরি স্থানান্তর এলাকায় যেতে হয়; বিপদ কেটে গেলে অস্থায়ী বসবাসের জন্য আশ্রয়কেন্দ্রে (যেমন স্কুলের জিমনেশিয়াম) যেতে হয়।',
      ),
    },
    {
      q: t(
        '工場内{こうじょうない}で配線{はいせん}ショートによる火災{かさい}を発見{はっけん}しました。最初{さいしょ}に行{おこな}うべき行動{こうどう}はどれですか？',
        'You discover a fire caused by an electrical short circuit in the factory. What should you do first?',
        'কারখানার ভেতরে বৈদ্যুতিক শর্ট সার্কিট থেকে আগুন লাগতে দেখলেন। সবার প্রথমে আপনার কী করা উচিত?',
      ),
      options: [
        t(
          '大声{おおごえ}で「火事{かじ}だ！」と周囲{しゅうい}に叫{さけ}んで危険{きけん}を全員{ぜんいん}に知{し}らせる',
          'Shout "Fire!" loudly to alert everyone nearby of the danger immediately',
          'উঁচু গলায় "আগুন! আগুন!" বলে চিৎকার করে আশেপাশের সবাইকে সতর্ক করা',
        ),
        t(
          '誰{だれ}にも知{し}らせず、自分{じぶん}1人{ひとり}だけで火{ひ}を消{け}そうとする',
          'Attempt to put out the fire alone without notifying anyone else',
          'কাউকে কিছু না জানিয়ে একাকী আগুন নেভানোর চেষ্টা করা',
        ),
        t(
          '現場{げんば}の火災{かさい}の様子{ようす}をスマートフォンで撮影{さつえい}してSNSに投稿{とうこう}する',
          'Record a smartphone video of the flames and post it to social media',
          'স্মার্টফোন দিয়ে আগুনের ভিডিও রেকর্ড করে সোশ্যাল মিডিয়ায় আপলোড করা',
        ),
        t(
          '煙{けむり}が外{そと}に出{で}ないように工場{こうじょう}の窓{まど}とシャッターを全{すべ}て閉{し}め切{き}る',
          'Shut all factory windows and shutters tightly so smoke cannot escape outside',
          'ধোঁয়া বাইরে যাওয়া আটকাতে কারখানার সব জানালা ও শাটার পুরোপুরি বন্ধ করে দেওয়া',
        ),
      ],
      answer: 0,
      why: t(
        '火災{かさい}を発見{はっけん}したら、まず大声{おおごえ}で周囲{しゅうい}に知{し}らせて初期消火{しょきしょうか}、通報{つうほう}、避難{ひなん}の協力{きょうりょく}を求{もと}めることが鉄則{てっそく}です。',
        'Upon spotting a fire, shouting loudly to alert coworkers is the fundamental first step to coordinate firefighting and evacuation.',
        'আগুন দেখতে পেলে চিৎকার করে সহকর্মীদের জানানোই প্রথম নিয়ম, যাতে সবাই মিলে নেভানো, দমকলে খবর দেওয়া ও নিরাপদে সরার প্রস্তুতি নেওয়া যায়।',
      ),
    },
    {
      q: t(
        '消火器{しょうかき}を使{つか}って初期消火{しょきしょうか}を行{おこな}う際{さい}、正{ただ}しい操作{そうさ}の順番{じゅんばん}はどれですか？',
        'When performing initial firefighting with an extinguisher, what is the correct operational sequence?',
        'অগ্নিনির্বাপক যন্ত্র দিয়ে প্রাথমিক আগুন নেভানোর সঠিক কার্যপ্রণালী কোনটি?',
      ),
      options: [
        t(
          'ホースを外{はず}す → レバーを握{にぎ}る → 黄色{きいろ}いピンを抜{ぬ}く',
          'Detach hose -> Squeeze lever -> Pull yellow pin',
          'পাইপ খোলা -> লিভার টেপা -> হলুদ পিন টানা',
        ),
        t(
          '黄色{きいろ}いピンを抜{ぬ}く → ホースを火元{ひもと}に向{む}ける → レバーを強{つよ}く握{にぎ}る',
          'Pull yellow pin -> Aim hose at base of fire -> Squeeze lever firmly',
          'হলুদ পিন টানা -> পাইপের মুখ আগুনের গোড়ায় তাক করা -> লিভার শক্ত করে চেপে ধরা',
        ),
        t(
          'レバーを強{つよ}く握{にぎ}る → 黄色{きいろ}いピンを抜{ぬ}く → ホースを火元{ひもと}に向{む}ける',
          'Squeeze lever firmly -> Pull yellow pin -> Aim hose at base of fire',
          'লিভার শক্ত করে টেপা -> হলুদ পিন টানা -> পাইপের মুখ আগুনের দিকে ধরা',
        ),
        t(
          '消火器{しょうかき}を逆{ぎゃく}さまに振{ふ}る → 安全{あんぜん}ピンを抜{ぬ}く → 火元{ひもと}に投{な}げつける',
          'Shake extinguisher upside down -> Pull safety pin -> Throw it into the fire',
          'অগ্নিনির্বাপক উল্টো করে ঝাঁকানো -> সেফটি পিন খোলা -> আগুনের মধ্যে ছুঁড়ে মারা',
        ),
      ],
      answer: 1,
      why: t(
        '消火器{しょうかき}の基本{きほん}操作{そうさ}は「1. 安全{あんぜん}ピンを抜{ぬ}く、2. ホースを火元{ひもと}に向{む}ける、3. レバーを握{にぎ}る」の3ステップです。',
        'The standard extinguisher procedure follows 3 steps: 1. Pull the pin, 2. Aim the hose, 3. Squeeze the lever.',
        'অগ্নিনির্বাপক ব্যবহারের মূল ৩টি ধাপ: ১. সেফটি পিন খোলা, ২. পাইপের মুখ আগুনের গোড়ায় তাক করা, ৩. লিভার শক্ত করে চেপে ধরা।',
      ),
    },
    {
      q: t(
        '消火器{しょうかき}での初期消火{しょきしょうか}をあきらめて、直{ただ}ちに避難{ひなん}しなければならない状況{じょうきょう}はどれですか？',
        'Under which condition must you give up initial firefighting with an extinguisher and evacuate immediately?',
        'কোন পরিস্থিতিতে অগ্নিনির্বাপক দিয়ে আগুন নেভানোর চেষ্টা অবিলম্বে ত্যাগ করে নির্দ্বিধায় পালিয়ে যেতে হবে?',
      ),
      options: [
        t(
          '消火器{しょうかき}の薬剤{やくざい}の粉{こな}が周囲{しゅうい}に白{しろ}く広{ひろ}がったとき',
          'When white extinguishing powder spreads around the floor',
          'যখন অগ্নিনির্বাপকের সাদা পাউডার মেঝেতে ছড়িয়ে পড়ে',
        ),
        t(
          '火災{かさい}の煙{けむり}のにおいがわずかに感{かん}じられたとき',
          'When a faint smell of smoke is first detected in the room',
          'যখন বাতাসে পোড়া ধোঁয়ার হালকা গন্ধ পাওয়া যায়',
        ),
        t(
          '炎{ほのお}が天井{てんじょう}に届{とど}いたか、作業者{さぎょうしゃ}の背丈{せたけ}を超{こ}えて燃{も}え広{ひろ}がったとき',
          'When flames reach the ceiling or spread taller than the worker height',
          'যখন আগুনের শিখা সিলিং ছুঁয়ে ফেলে বা মানুষের উচ্চতা ছাড়িয়ে উপরে ওঠে',
        ),
        t(
          '消防車{しょうぼうしゃ}のサイレンの音{おと}が遠{とお}くから聞{き}こえてきたとき',
          'When the siren of an approaching fire engine is heard in the distance',
          'যখন দূর থেকে দমকলের গাড়ির সাইরেনের শব্দ শোনা যায়',
        ),
      ],
      answer: 2,
      why: t(
        '炎{ほのお}が天井{てんじょう}に達{たっ}するとフラッシュオーバーが起{お}き部屋{へや}全体{ぜんたい}に火{ひ}が回{まわ}るため、初期消火{しょきしょうか}を即座{そくざ}に中止{ちゅうし}して逃{に}げます。',
        'Once flames reach the ceiling, flashover can occur engulfing the whole room in seconds; stop firefighting and flee immediately.',
        'আগুনের শিখা সিলিং স্পর্শ করলে কয়েক সেকেন্ডে পুরো রুমে আগুন ছড়িয়ে যেতে পারে (ফ্ল্যাশওভার); তাই সাথে সাথে নেভানো বন্ধ করে পালাতে হবে।',
      ),
    },
    {
      q: t(
        '火災{かさい}の煙{けむり}が充満{じゅうまん}する建物{たてもの}から避難{ひなん}する際{さい}、最{もっと}も適切{てきせつ}な姿勢{しせい}と行動{こうどう}はどれですか？',
        'When evacuating a building filled with fire smoke, what is the most appropriate posture and action?',
        'ধোঁয়ায় আচ্ছন্ন ভবন থেকে নিরাপদে বের হওয়ার সময় সবচেয়ে উপযুক্ত শারীরিক ভঙ্গি ও পদক্ষেপ কোনটি?',
      ),
      options: [
        t(
          '深呼吸{しんこきゅう}をしながら背筋{せすじ}を伸{の}ばして大股{おおまた}で走{はし}る',
          'Stand upright taking deep breaths and run with long strides',
          'সোজা হয়ে দাঁড়িয়ে বুকভরে শ্বাস নিতে নিতে দ্রুত দৌড়ে যাওয়া',
        ),
        t(
          '煙{けむり}を吸{す}わないよう目を閉{と}じて手探{てさぐ}りで後退{こうたい}する',
          'Close your eyes to avoid smoke and walk backwards by touch',
          'ধোঁয়া থেকে চোখ বাঁচাতে চোখ বন্ধ করে হাতড়ে হাতড়ে পেছনের দিকে হাঁটা',
        ),
        t(
          '部屋{へや}の中{なか}に戻{もど}り、ドアを施錠{せじょう}して助{たす}けが来{く}るのを静{しず}かに待{ま}つ',
          'Return into a room, lock the door, and wait quietly for rescue',
          'রুমের ভেতর ফিরে গিয়ে দরজা লক করে চুপচাপ সাহায্যের অপেক্ষায় থাকা',
        ),
        t(
          '濡{ぬ}れタオルなどで口{くち}と鼻{はな}を覆{おお}い、姿勢{しせい}を低{ひく}くして床{ゆか}近{ちか}くの空気{くうき}を吸{す}いながら進{すす}む',
          'Cover mouth and nose with a damp towel, stay low to breathe clearer air near the floor, and move forward',
          'ভেজা তোয়ালে দিয়ে নাক-মুখ ঢেকে শরীর নিচু করে মেঝের কাছের তুলনামূলক পরিষ্কার বাতাসে শ্বাস নিয়ে সামনে এগোনো',
        ),
      ],
      answer: 3,
      why: t(
        '有毒{ゆうどく}ガスや高温{こうおん}の煙{けむり}は上{うえ}にたまります。口{くち}と鼻{はな}を保護{ほご}し、姿勢{しせい}を低{ひく}くして壁{かべ}沿{づた}いに避難{ひなん}します。',
        'Toxic hot smoke rises to the ceiling; cover your respiratory tract, crouch low to use cleaner floor-level air, and follow the walls.',
        'বিষাক্ত গ্যাস ও গরম ধোঁয়া উপরের দিকে জমা হয়; তাই নাক-মুখ ঢেকে নিচু হয়ে মেঝের কাছের বাতাস নিয়ে দেয়াল ধরে বের হতে হবে।',
      ),
    },
    {
      q: t(
        '日本{にほん}で工場{こうじょう}の火災{かさい}や急病{きゅうびょう}・ケガを発見{はっけん}したとき、通報{つうほう}すべき緊急{きんきゅう}電話番号{でんわばんごう}はどれですか？',
        'What is the emergency telephone number to dial in Japan to report a factory fire or medical emergency?',
        'জাপানে কারখানায় আগুন লাগলে বা গুরুতর অসুস্থ/আহত ব্যক্তির জন্য জরুরি কোন নম্বরে ফোন করতে হয়?',
      ),
      options: [
        t(
          '119番{ばん}（消防{しょうぼう}・救急{きゅうきゅう}）',
          '119 (Fire and Ambulance)',
          '১১৯ (দমকল ও অ্যাম্বুলেন্স)',
        ),
        t(
          '110番{ばん}（警察{けいさつ}事件{じけん}・事故{じこ}）',
          '110 (Police incidents)',
          '১১০ (পুলিশ ও অপরাধ বিষয়ক)',
        ),
        t(
          '118番{ばん}（海上{かいじょう}の事件{じけん}・事故{じこ}）',
          '118 (Coast Guard sea emergencies)',
          '১১৮ (কোস্ট গার্ড ও সমুদ্র দুর্ঘটনা)',
        ),
        t(
          '104番{ばん}（電話番号{でんわばんごう}案内{あんない}）',
          '104 (Directory assistance)',
          '১০৪ (ডিরেক্টরি সহায়তা)',
        ),
      ],
      answer: 0,
      why: t(
        '日本{にほん}では火災{かさい}と救急{きゅうきゅう}は「119番{ばん}」です。事件{じけん}・事故{じこ}の警察{けいさつ}は「110番{ばん}」に連絡{れんらく}します。',
        'In Japan, fire and medical emergencies are reported by dialing 119, while police incidents are handled via 110.',
        'জাপানে আগুন ও অ্যাম্বুলেন্সের জরুরি নম্বর ১১৯। অপরদিকে পুলিশি সহায়তা বা দুর্ঘটনার জন্য ১১০ নম্বরে ফোন দিতে হয়।',
      ),
    },
    {
      q: t(
        '工場{こうじょう}の外{そと}の避難場所{ひなんばしょ}に全員{ぜんいん}で脱出{だっしゅつ}しました。到着後{とうちゃくご}に最{もっと}も優先{ゆうせん}して行{おこな}うべき行動{こうどう}はどれですか？',
        'Everyone successfully evacuated to the outside assembly area. What is the most important action upon arrival?',
        'সবাই কারখানার বাইরের নিরাপদ সমাবেশস্থলে পৌঁছালেন। পৌঁছানোর পর সবার আগে সবচেয়ে গুরুত্বপূর্ণ করণীয় কী?',
      ),
      options: [
        t(
          '避難{ひなん}が完了{かんりょう}したので直{ただ}ちに解散{かいさん}して自分{じぶん}の家{いえ}へ帰{かえ}る',
          'Immediately dismiss and go home since evacuation is completed',
          'স্থানান্তর সম্পন্ন হয়েছে ভেবে সাথে সাথে যার যার বাসায় চলে যাওয়া',
        ),
        t(
          '整列{せいれつ}して職長{しょくちょう}の点呼{てんこ}を受{う}け、逃{に}げ遅{おく}れやケガ人{にん}がいないか確認{かくにん}する',
          'Line up for the foreman roll call to confirm no one is missing or injured',
          'লাইনে দাঁড়িয়ে ফোরম্যানের কাছে রোল কলে নাম ডাকানো এবং কেউ আটকা বা আহত আছে কিনা নিশ্চিত করা',
        ),
        t(
          '作業場{さぎょうば}に置{お}き忘{わす}れた財布{さいふ}や携帯電話{けいたいでんわ}を1人{ひとり}で取{と}りに戻{もど}る',
          'Go back into the workspace alone to retrieve forgotten wallets or mobile phones',
          'কাজের টেবিলে ফেলে আসা মানিব্যাগ বা ফোন আনতে একাকী ভেতরে ফিরে যাওয়া',
        ),
        t(
          '同僚{どうりょう}と話{はな}しながら避難場所{ひなんばしょ}の敷地外{しきちがい}へ自由{じゆう}に出歩{である}く',
          'Wander away from the assembly area while chatting casually with coworkers',
          'সহকর্মীদের সাথে আড্ডা দিতে দিতে সমাবেশস্থলের বাইরে যেখানে-সেখানে ঘুরে বেড়ানো',
        ),
      ],
      answer: 1,
      why: t(
        '避難場所{ひなんばしょ}では全員{ぜんいん}が無事{ぶじ}か確認{かくにん}する「点呼{てんこ}」が最優先{さいゆうせん}です。勝手{かって}な帰宅{きたく}や現場{げんば}への再進入{さいしんにゅう}は絶対{ぜったい}に禁止{きんし}です。',
        'At the assembly site, roll call to account for all workers is crucial; leaving or re-entering the building is strictly prohibited.',
        'সমাবেশস্থলে সবার উপস্থিতি ও নিরাপত্তা নিশ্চিত করতে রোল কল সবচেয়ে গুরুত্বপূর্ণ; কাউকে না জানিয়ে চলে যাওয়া বা ভেতরে ফেরা সম্পূর্ণ নিষিদ্ধ।',
      ),
    },
    {
      q: t(
        '工場{こうじょう}の壁{かべ}に掲示{けいじ}されている「避難経路図{ひなんけいろず}」について、普段{ふだん}から確認{かくにん}しておくべき事項{じこう}はどれですか？',
        'Regarding the factory evacuation route map posted on the wall, what should you verify on a daily basis?',
        'দেয়ালে টানানো কারখানার জরুরি নির্গমন মানচিত্র দেখে প্রতিদিন আগে থেকেই কী জেনে রাখা দরকার?',
      ),
      options: [
        t(
          '工場{こうじょう}の敷地{しきち}の面積{めんせき}と周辺{しゅうへん}の住宅{じゅうたく}の価格{かかく}',
          'The square meters of the factory premises and nearby housing prices',
          'কারখানার মোট জমির আয়তন এবং আশেপাশের বাড়ির বর্তমান বাজারমূল্য',
        ),
        t(
          '休憩室{きゅうけいしつ}の自動販売機{じどうはんばいき}のジュースの種類{しゅるい}と値段{ねだん}',
          'The drink selection and prices of the vending machine in the break room',
          'বিশ্রামাগারের ভেন্ডিং মেশিনের জুসের ধরন এবং সেগুলোর দাম',
        ),
        t(
          '自分{じぶん}の作業場{さぎょうば}から最寄{もよ}りの非常口{ひじょうぐち}への進路{しんろ}と、消火器{しょうかき}や非常ベルの位置{いち}',
          'The path from your workstation to the nearest emergency exit, and locations of extinguishers and fire alarms',
          'নিজের কাজের জায়গা থেকে নিকটবর্তী জরুরি দরজা পর্যন্ত পথ এবং অগ্নিনির্বাপক ও ফায়ার অ্যালার্মের অবস্থান',
        ),
        t(
          '工場{こうじょう}の屋根{やね}のペンキの塗{ぬ}り替{か}え予定{よてい}年月{ねんげつ}',
          'The scheduled month and year for repainting the factory roof',
          'কারখানার ছাদে আবার নতুন করে রং করার নির্ধারিত বছর ও মাস',
        ),
      ],
      answer: 2,
      why: t(
        '停電{ていでん}や煙{けむり}で視界{しかい}が悪{わる}い緊急時{きんきゅうじ}でも迷{まよ}わず避難{ひなん}できるよう、日頃{ひごろ}から非常口{ひじょうぐち}や消火設備{しょうかせつび}の位置{いち}を覚{おぼ}えておきます。',
        'Knowing the exits and firefighting equipment in advance allows you to evacuate safely even under zero visibility or blackout.',
        'বিদ্যুৎ চলে গেলে বা ধোঁয়ায় পথ দেখতে না পেলেও যেন নিরাপদে বের হওয়া যায়, সেজন্য আগেই বহির্গমন পথ ও সরঞ্জামের অবস্থান মুখস্থ রাখতে হবে।',
      ),
    },
    {
      q: t(
        '長崎県{ながさきけん}佐世保市{させぼし}のように山{やま}と海{うみ}が近接{きんせつ}した地域{ちいき}で、台風{たいふう}や大雨{おおあめ}の時{とき}に特{とく}に注意{ちゅうい}すべき自然災害{しぜんさいがい}はどれですか？',
        'In regions like Sasebo, Nagasaki where mountains and the sea are close, which natural hazards require special caution during typhoons and torrential rain?',
        'সাসেবো, নাগাসাকির মতো যেখানে পাহাড় ও সমুদ্র খুব কাছাকাছি, সেখানে টাইফুন ও ভারী বৃষ্টির সময় কোন প্রাকৃতিক দুর্যোগে সবচেয়ে বেশি সতর্ক থাকা প্রয়োজন?',
      ),
      options: [
        t(
          '豪雪{ごうせつ}による大規模{だいきぼ}な雪崩{なだれ}と道路{どうろ}の凍結{とうけつ}',
          'Massive snow avalanches and road freezing caused by blizzards',
          'তুষারঝড়ের কারণে বিশাল বরফধস এবং রাস্তাঘাট বরফে জমে যাওয়া',
        ),
        t(
          '乾燥{かんそう}地帯{ちたい}における激{はげ}しい砂嵐{すなあらし}と干ばつ',
          'Intense sandstorms and prolonged drought typical of arid zones',
          'মরু অঞ্চলের মতো তীব্র বালুঝড় এবং একটানা খরা সৃষ্টি হওয়া',
        ),
        t(
          '活火山{かっかざん}の大噴火{だいふんか}に伴{ともな}う溶岩{ようがん}流{りゅう}の接近{せっきん}',
          'Lava flows advancing from massive eruptions of active volcanoes',
          'জীবন্ত আগ্নেয়গিরির ভয়াবহ অগ্ন্যুৎপাত থেকে লাভা ধেয়ে আসা',
        ),
        t(
          '急傾斜地{きゅうけいしゃち}のがけ崩{くず}れ（土砂災害{どしゃさいがい}）および河川{かせん}・低地{ていち}の浸水{しんすい}',
          'Landslides on steep slopes (sediment disasters) and flooding of rivers and low-lying coastal land',
          'খাড়া পাহাড়ের মাটি বা পাথর ধসে পড়া (ভূমিধস) এবং নদী ও নিচু উপকূলীয় এলাকায় আকস্মিক বন্যা/প্লাবন',
        ),
      ],
      answer: 3,
      why: t(
        '佐世保市{させぼし}は急傾斜地{きゅうけいしゃち}が多{おお}く、大雨{おおあめ}や台風{たいふう}では土砂崩{どしゃくず}れや浸水{しんすい}の危険{きけん}が高{たか}まるため、早{はや}めの避難{ひなん}情報{じょうほう}確認{かくにん}が不可欠{ふかけつ}です。',
        'Sasebo has many steep slopes prone to landslides and coastal flooding during torrential rains; early checking of evacuation advisories is vital.',
        'সাসেবোতে প্রচুর খাড়া পাহাড় থাকায় ভারী বৃষ্টি বা টাইফুনে ভূমিধস ও আকস্মিক বন্যার ঝুঁকি অনেক বেশি; তাই আগে থেকেই স্থানান্তর সতর্কবার্তা জেনে রাখা জরুরি।',
      ),
    },
  ],
  glossary: [
    {
      ja: '緊急地震速報',
      reading: 'きんきゅうじしんそくほう',
      en: 'Earthquake Early Warning',
      bn: 'ভূমিকম্প জরুরি সতর্কবার্তা',
      note: t(
        '地震の発生直後に強い揺れが来ることを事前に知らせる警報システム。',
        'Warning system alerting that strong tremors are arriving seconds after earthquake detection.',
        'ভূমিকম্প শনাক্ত হওয়ার পরপরই তীব্র ঝাঁকুনি আসার পূর্বমুহূর্তে সতর্ক করার স্বয়ংক্রিয় ব্যবস্থা।',
      ),
    },
    {
      ja: '避難経路',
      reading: 'ひなんけいろ',
      en: 'Evacuation route',
      bn: 'জরুরি নির্গমন পথ',
      note: t(
        '災害時に作業場から屋外の安全な場所へ脱出するために指定された通路。',
        'Designated path used to escape safely from the workstation to outside.',
        'দুর্যোগের সময় কাজের জায়গা থেকে নিরাপদে বাইরে বের হওয়ার নির্ধারিত চলাচলের পথ।',
      ),
    },
    {
      ja: '非常口',
      reading: 'ひじょうぐち',
      en: 'Emergency exit',
      bn: 'জরুরি বহির্গমন দরজা',
      note: t(
        '火災や地震などの緊急時に外へ避難するために設けられた特別な出入口。',
        'Special doorway installed for evacuating outside during fires or earthquakes.',
        'আগুন বা ভূমিকম্পের মতো জরুরি মুহূর্তে দ্রুত বাইরে বের হওয়ার বিশেষ বহির্গমন দরজা।',
      ),
    },
    {
      ja: '誘導灯',
      reading: 'ゆうどうとう',
      en: 'Emergency exit light / guide sign',
      bn: 'জরুরি দিকনির্দেশক বাতি',
      note: t(
        '停電時でも非常口や避難通路の方向を示す緑色に光る標識灯。',
        'Green illuminated sign indicating emergency exits and evacuation paths during blackouts.',
        'বিদ্যুৎ চলে গেলেও জরুরি দরজা ও নির্গমন পথের দিক দেখানো সবুজ আলোর নির্দেশক বাতি।',
      ),
    },
    {
      ja: '消火器',
      reading: 'しょうかき',
      en: 'Fire extinguisher',
      bn: 'অগ্নিনির্বাপক যন্ত্র',
      note: t(
        '火災の初期段階で火を消し止めるための持ち運び式消火器具。',
        'Portable firefighting cylinder used to extinguish fires in their initial stage.',
        'প্রাথমিক অবস্থায় আগুন দ্রুত নিভিয়ে ফেলার কাজে ব্যবহৃত বহনযোগ্য যন্ত্র।',
      ),
    },
    {
      ja: '初期消火',
      reading: 'しょきしょうか',
      en: 'Initial firefighting',
      bn: 'প্রাথমিক অগ্নি নির্বাপণ',
      note: t(
        '火災が小さいうちに消火器や水を使って素早く火を消す行動。',
        'Action to extinguish a fire quickly using extinguishers before it spreads.',
        'আগুন বড় আকারে ছড়িয়ে পড়ার আগেই অগ্নিনির্বাপক বা পানি দিয়ে দ্রুত নিভিয়ে ফেলার পদক্ষেপ।',
      ),
    },
    {
      ja: '津波',
      reading: 'つなみ',
      en: 'Tsunami',
      bn: 'সুনামি',
      note: t(
        '海底地震などによって発生し、沿岸部に押し寄せる巨大な波。',
        'Giant sea waves caused by undersea earthquakes that strike coastal areas.',
        'সমুদ্রের তলদেশে ভূমিকম্পের ফলে সৃষ্ট বিশাল সামুদ্রিক ঢেউ যা উপকূলে আঘাত হানে।',
      ),
    },
    {
      ja: '津波てんでんこ',
      reading: 'つなみてんでんこ',
      en: 'Tsunami Tendenko (Evacuate independently to high ground)',
      bn: 'সুনামি তেন্দেনকো (নিজ দায়িত্বে অবিলম্বে উঁচু স্থানে ছুটে যাওয়া)',
      note: t(
        '津波が来るときは人を待たず各自が急いで高台へ逃げよという三陸地方の教訓。',
        'Traditional disaster wisdom urging everyone to flee immediately to high ground without waiting for others.',
        'সুনামির সময় কারো জন্য কালক্ষেপণ না করে প্রত্যেকে নিজ দায়িত্বে সাথে সাথে উঁচু স্থানে ছুটে যাওয়ার সনাতন নীতি।',
      ),
    },
    {
      ja: '指定緊急避難場所',
      reading: 'していきんきゅうひなんばしょ',
      en: 'Designated Emergency Evacuation Site',
      bn: 'নির্ধারিত জরুরি জীবনরক্ষা স্থান',
      note: t(
        '津波や洪水、火災の危険から命を守るために緊急に逃げ込む場所（高台や公園）。',
        'Location (high ground or park) to escape to immediately to protect life from imminent disaster.',
        'সুনামি, বন্যা বা আগুনের হাত থেকে জীবন বাঁচাতে তাৎক্ষণিক ছুটে যাওয়ার জায়গা (যেমন উঁচু পাহাড় বা পার্ক)।',
      ),
    },
    {
      ja: '指定避難所',
      reading: 'していひなんじょ',
      en: 'Designated Evacuation Shelter',
      bn: 'নির্ধারিত জরুরি আশ্রয়কেন্দ্র',
      note: t(
        '家を失った被災者が一定期間滞在して生活支援を受けるための施設（学校の体育館など）。',
        'Facility (such as a school gym) where displaced survivors stay and receive relief supplies.',
        'ঘরবাড়ি ক্ষতিগ্রস্ত হওয়া দুর্গতরা কিছুদিন থাকা ও ত্রাণ সহায়তা পাওয়ার জন্য নির্ধারিত স্থান (যেমন স্কুলের জিমনেশিয়াম)।',
      ),
    },
    {
      ja: '点呼',
      reading: 'てんこ',
      en: 'Roll call',
      bn: 'রোল কল / হাজিরা নিশ্চিতকরণ',
      note: t(
        '避難場所で全員の名前を呼び、逃げ遅れや怪我人がいないか確認すること。',
        'Calling names at the assembly site to confirm all workers are safe and accounted for.',
        'সমাবেশস্থলে প্রত্যেকের নাম ডেকে কেউ আটকা পড়েছে বা নিখোঁজ আছে কিনা তা নিশ্চিত করার প্রক্রিয়া।',
      ),
    },
    {
      ja: '土砂崩れ',
      reading: 'どしゃくずれ',
      en: 'Landslide',
      bn: 'ভূমিধস',
      note: t(
        '大雨や台風によって山の斜面やがけが崩れ落ちてくる現象。',
        'Phenomenon where steep mountain slopes or cliffs collapse due to heavy rain.',
        'ভারী বৃষ্টি বা টাইফুনের কারণে খাড়া পাহাড়ের মাটি ও পাথর ধসে পড়ার বিপজ্জনক প্রাকৃতিক ঘটনা।',
      ),
    },
  ],
}
