import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string, bn: string): Tri => ({ ja, en, bn })

export const shisaKanko: CourseData = {
  id: 'shisa-kanko',
  category: 'core',
  level: 1,
  minutes: 15,
  title: t(
    '指差呼称{しさこしょう}',
    'Pointing and calling',
    'পয়েন্টিং এবং কলিং (ইঙ্গিত ও মুখে বলা)',
  ),
  sub: t(
    '目{め}で見{み}て、指{ゆび}を差{さ}し、声{こえ}を出{だ}して「ヨシ！」と確認{かくにん}する。',
    'Look with your eyes, point your finger, speak aloud, and confirm with "Yoshi!".',
    'চোখে দেখে, আঙুল তাক করে, মুখে জোরে "ঠিক আছে (ইয়োশি)!" বলে নিশ্চিত করো।',
  ),
  refs: [
    '労働安全衛生法 第59条（安全衛生教育）',
    '中央労働災害防止協会（中災防） ゼロ災運動・指差呼称推進',
    '鉄道総合技術研究所（JR総研） 指差呼称のエラー防止効果検証',
  ],
  panels: [
    {
      who: 'foreman',
      text: t(
        '今日{きょう}から現場{げんば}に入{はい}ります。まず「指差呼称{しさこしょう}」を覚{おぼ}えましょう。',
        'You start on site today. First, let us learn pointing and calling.',
        'আজ থেকে তুমি সাইটে কাজ শুরু করছ। প্রথমে আমরা পয়েন্টিং ও কলিং শিখব।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '一人{ひとり}で作業{さぎょう}するときも、声{こえ}を出{だ}して指{ゆび}を差{さ}すのですか？',
        'Do I still need to point and call out when working alone?',
        'একা কাজ করার সময়ও কি আঙুল তাক করে মুখে বলতে হবে?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'そうです。目{め}と手{て}と声{こえ}を使{つか}うことで、脳{のう}が刺激{しげき}されミスが減{へ}ります。',
        'Yes. Using your eyes, hand, and voice stimulates your brain and prevents mistakes.',
        'হ্যাঁ। চোখ, হাত এবং কণ্ঠ ব্যবহার করলে মস্তিষ্ক সতর্ক হয় এবং ভুল কমে।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'わかりました！……スイッチ、ヨシ！（※よそ見{み}をしながら、手{て}だけを振{ふ}る）',
        'Understood! ...Switch, yoshi! (*looking away while casually waving hand*)',
        'বুঝেছি! ...সুইচ, ঠিক আছে! (*অন্য দিকে তাকিয়ে হাত নাড়িয়ে*)',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'ちょっと待{ま}ってください。足{あし}を止{と}めて、スイッチをしっかり見{み}て腕{うで}をまっすぐ伸{の}ばします。',
        'Wait a moment. Stop your feet, look straight at the switch, and extend your arm fully.',
        'একটু দাঁড়াও। পা থামাও, সুইচের দিকে সরাসরি তাকাও এবং হাত সোজা করো।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '目{め}で見{み}て、腕{うで}を伸{の}ばして……「主電源{しゅでんげん}、OFF、ヨシ！」。こうですか？',
        'Look with eyes, extend arm... "Main switch OFF, yoshi!". Like this?',
        'চোখে দেখে, হাত বাড়িয়ে... "মেইন সুইচ বন্ধ, ঠিক আছে!"। এভাবে?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'とても良{よ}い姿勢{しせい}です。最後{さいご}に「ヨシ！」と耳{みみ}で聞{き}くことが大切{たいせつ}です。',
        'Very good posture. Hearing yourself say "Yoshi!" at the end is essential.',
        'খুব সুন্দর ভঙ্গি। শেষে নিজের মুখে "ইয়োশি!" বলাটা কানে শোনা খুব জরুরি।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '通路{つうろ}を横断{おうだん}するときも、同{おな}じように確認{かくにん}すればいいですか？',
        'Should I confirm in the same way when crossing a walkway?',
        'হাঁটার পথ পার হওয়ার সময়ও কি একইভাবে নিশ্চিত করব?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'その通{とお}りです。白線{はくせん}の手前{てまえ}で止{と}まり、「右{みぎ}ヨシ、左{ひだり}ヨシ！」と指{ゆび}を差{さ}します。',
        'Exactly. Stop before the white line, point, and call "Right clear, left clear!".',
        'ঠিক তাই। সাদা দাগের সামনে থেমে আঙুল দেখিয়ে বলবে, "ডান ঠিক আছে, বাম ঠিক আছে!"।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '焦{あせ}らず一{ひと}つずつ指差呼称{しさこしょう}をして、安全{あんぜん}に作業{さぎょう}します！',
        'I will point and call calmly step by step, and work safely!',
        'তাড়াহুড়ো না করে ধাপে ধাপে পয়েন্টিং ও কলিং করব এবং নিরাপদে কাজ করব!',
      ),
    },
  ],
  handbook: [
    t(
      '対象{たいしょう}を目{め}で見{み}て、腕{うで}を伸{の}ばして指{ゆび}を差{さ}す。',
      'Look at the target with your eyes and point with an outstretched arm.',
      'চোখ দিয়ে লক্ষ্যবস্তুর দিকে তাকাও এবং হাত বাড়িয়ে আঙুল তাক করো।',
    ),
    t(
      '状態{じょうたい}を声{こえ}に出{だ}し、最後{さいご}に「ヨシ！」と呼称{こしょう}する。',
      'State the condition aloud and finish by calling out "Yoshi!".',
      'অবস্থাটি মুখে জোরে বলো এবং শেষে "ঠিক আছে (ইয়োশি)!" উচ্চারণ করো।',
    ),
    t(
      '自分{じぶん}の耳{みみ}で呼称{こしょう}を聞{き}いて、確実{かくじつ}に意識{いしき}を向{む}ける。',
      'Listen to your own voice to focus your conscious attention firmly.',
      'নিজের মুখের ডাক নিজের কানে শুনে মনোযোগ সম্পূর্ণভাবে কেন্দ্রীভূত করো।',
    ),
    t(
      '歩{ある}きながらではなく、必{かなら}ず足{あし}を止{と}めて確認{かくにん}する。',
      'Never do it while walking; always stop your feet to confirm.',
      'হাঁটতে হাঁটতে কখনো নয়; অবশ্যই পা থামিয়ে নিশ্চিত করো।',
    ),
    t(
      '形{かたち}だけのいいかげんな指差{ゆびさ}しや、早口{はやくち}の呼称{こしょう}は禁止{きんし}。',
      'Sloppy half-hearted pointing and rushed mumbling are strictly forbidden.',
      'দায়সারাভাবে আঙুল দেখানো এবং তাড়াহুড়ো করে অস্পষ্ট উচ্চারণ সম্পূর্ণ নিষেধ।',
    ),
    t(
      'スイッチ・バルブ・レバーを動{うご}かす前{まえ}に必{かなら}ず実行{じっこう}する。',
      'Always execute before operating switches, valves, or control levers.',
      'সুইচ, ভাল্ব বা নিয়ন্ত্রণ লিভার চালানোর আগে অবশ্যই এটি করো।',
    ),
    t(
      '通路{つうろ}や交差点{こうさてん}を渡{わた}る時{とき}は「右{みぎ}ヨシ、左{ひだり}ヨシ！」を確認{かくにん}する。',
      'When crossing walkways or intersections, confirm "Right clear, left clear!".',
      'রাস্তা বা হাঁটার পথ পারাপারে "ডান ঠিক আছে, বাম ঠিক আছে!" নিশ্চিত করো।',
    ),
    t(
      '一人{ひとり}作業{さぎょう}でも省略{しょうりゃく}せず、必{かなら}ず声{こえ}を出{だ}す。',
      'Never skip it even when working alone; always speak aloud.',
      'একা কাজ করলেও বাদ দেবে না, অবশ্যই মুখে আওয়াজ করবে।',
    ),
    t(
      '「たぶん大丈夫{だいじょうぶ}」という思{おも}い込{こ}みで手{て}を動{うご}かさない。',
      'Never operate based on blind assumptions like "it is probably fine".',
      '"সম্ভবত ঠিক আছে" এমন অনুমানের ওপর ভিত্তি করে কখনো হাত চালাবে না।',
    ),
    t(
      '指差{ゆびさ}しで異常{いじょう}を見{み}つけたら、操作{そうさ}せず直{ただ}ちに職長{しょくちょう}へ報告{ほうこく}する。',
      'If you spot an abnormality while pointing, do not operate; report to the foreman immediately.',
      'ইঙ্গিত করার সময় কোনো অস্বাভাবিকতা দেখলে কাজ থামিয়ে সাথে সাথে ফোরম্যানকে জানাও।',
    ),
  ],
  reel: [
    t(
      '主電源{しゅでんげん}、OFF、ヨシ！',
      'Main switch, OFF, yoshi!',
      'মেইন সুইচ, বন্ধ, ঠিক আছে!',
    ),
    t(
      '回転部{かいてんぶ}カバー、固定{こてい}、ヨシ！',
      'Rotating part cover, locked, yoshi!',
      'ঘূর্ণায়মান অংশের কভার, আটকানো, ঠিক আছে!',
    ),
    t(
      '吊{つ}り荷{に}の下{した}、立入{たちいり}なし、ヨシ！',
      'Under the load, clear, yoshi!',
      'ঝুলন্ত মালের নিচে, কেউ নেই, ঠিক আছে!',
    ),
    t(
      '左右{さゆう}確認{かくにん}、通路{つうろ}、ヨシ！',
      'Look right and left, walkway clear, yoshi!',
      'ডানে-বামে দেখা, হাঁটার পথ, ঠিক আছে!',
    ),
    t(
      'アース線{せん}、接続{せつぞく}、ヨシ！',
      'Earth clamp, connected, yoshi!',
      'আর্থিং ক্ল্যাম্প, লাগানো, ঠিক আছে!',
    ),
    t(
      '爪{つめ}の降下{こうか}、接地{せっち}、ヨシ！',
      'Forks lowered, on the ground, yoshi!',
      'ফর্কের কাঁটা নামানো, মাটিতে স্পর্শ, ঠিক আছে!',
    ),
    t(
      'ヘルメット、あごひも締{し}まり、ヨシ！',
      'Helmet, chin strap tight, yoshi!',
      'হেলমেট, চিনস্ট্র্যাপ শক্ত, ঠিক আছে!',
    ),
  ],
  drills: [
    {
      id: 'shisa-spot',
      kind: 'spot',
      secs: 30,
      need: 4,
      name: t(
        '指差呼称{しさこしょう}の実践{じっせん}チェック',
        'Pointing and calling practice check',
        'পয়েন্টিং ও কলিং অনুশীলনের ভুলত্রুটি খোঁজা',
      ),
      goal: t(
        '30秒{びょう}で不安全{ふあんぜん}な行動{こうどう}を4つ見{み}つける。',
        'Find 4 unsafe behaviors in 30 seconds.',
        '৩০ সেকেন্ডে ৪টি অনিরাপদ আচরণ খুঁজে বের করো।',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '見{み}ないでボタンを押{お}す',
            'Pressing button without looking',
            'না দেখে বোতাম টেপা',
          ),
        },
        {
          hazard: false,
          label: t(
            '足{あし}を止{と}めて対象{たいしょう}を指{ゆび}差{さ}す',
            'Stopping feet to point at the target',
            'পা থামিয়ে লক্ষ্যবস্তুর দিকে আঙুল তাক করা',
          ),
        },
        {
          hazard: true,
          label: t(
            '歩{ある}きながらスイッチ操作{そうさ}',
            'Operating switch while walking',
            'হাঁটতে হাঁটতে সুইচ চালানো',
          ),
        },
        {
          hazard: false,
          label: t(
            '「〇〇、ヨシ！」とはっきり声{こえ}に出{だ}す',
            'Calling out clearly "... Yoshi!"',
            'স্পষ্ট কণ্ঠে "... ঠিক আছে (ইয়োশি)!" বলা',
          ),
        },
        {
          hazard: true,
          label: t(
            '声{こえ}を出{だ}さず手{て}だけ振{ふ}る',
            'Waving hand without speaking aloud',
            'মুখে কিছু না বলে শুধু হাত নাড়ানো',
          ),
        },
        {
          hazard: false,
          label: t(
            '白線{はくせん}の手前{てまえ}で一時停止{いちじていし}',
            'Stopping before the white line',
            'সাদা দাগের সামনে সাময়িক থামা',
          ),
        },
        {
          hazard: true,
          label: t(
            '同僚{どうりょう}と話{はな}しながら機械{きかい}を動{うご}かす',
            'Starting machine while chatting with coworker',
            'সহকর্মীর সাথে গল্প করতে করতে মেশিন চালানো',
          ),
        },
        {
          hazard: true,
          label: t(
            '「たぶん大丈夫{だいじょうぶ}」と確認{かくにん}を省略{しょうりゃく}',
            'Skipping check assuming "probably fine"',
            '"সম্ভবত ঠিক আছে" ভেবে চেক বাদ দেওয়া',
          ),
        },
        {
          hazard: false,
          label: t(
            '指差{ゆびさ}しで異常{いじょう}に気{き}づき作業{さぎょう}中断{ちゅうだん}',
            'Stopping work upon noticing defect via pointing',
            'ইঙ্গিত করার সময় সমস্যা দেখে কাজ থামানো',
          ),
        },
      ],
    },
    {
      id: 'shisa-four-steps',
      kind: 'seq',
      name: t(
        '指差呼称{しさこしょう}の4段階{だんかい}',
        'Four steps of pointing and calling',
        'পয়েন্টিং ও কলিংয়ের ৪টি ধাপ',
      ),
      goal: t(
        '基本{きほん}の4ステップを正{ただ}しい順番{じゅんばん}に並{なら}べる。',
        'Arrange the 4 basic steps in the correct order.',
        'মৌলিক ৪টি ধাপ সঠিক ক্রমে সাজাও।',
      ),
      steps: [
        t(
          '1. 見{み}る（対象{たいしょう}を目{め}でしっかり見つめる）',
          '1. Look (Fix your eyes firmly on the target)',
          '১. দেখা (লক্ষ্যবস্তুর দিকে মনোযোগ দিয়ে তাকানো)',
        ),
        t(
          '2. 指差{ゆびさ}す（腕{うで}をまっすぐ伸{の}ばして指{ゆび}差{さ}す）',
          '2. Point (Extend your arm straight and point)',
          '২. আঙুল তাক করা (হাত সোজা বাড়িয়ে নির্দেশ করা)',
        ),
        t(
          '3. 言{い}う（状態{じょうたい}を声{こえ}に出{だ}し「ヨシ！」と呼称{こしょう}）',
          '3. Call (State condition aloud and call "Yoshi!")',
          '৩. মুখে বলা (অবস্থা বলে "ঠিক আছে (ইয়োশি)!" বলা)',
        ),
        t(
          '4. 動作{どうさ}する（耳{みみ}で確認{かくにん}してから操作{そうさ}に移{うつ}る）',
          '4. Act (Listen with ears, then perform the action)',
          '৪. কাজ করা (কানে শুনে নিশ্চিত হয়ে তবেই পরিচালনা করা)',
        ),
      ],
    },
    {
      id: 'crossing-check',
      kind: 'seq',
      name: t(
        '構内{こうない}通路{つうろ}の安全{あんぜん}横断{おうだん}',
        'Safe factory walkway crossing',
        'কারখানার হাঁটার পথ নিরাপদে পার হওয়া',
      ),
      goal: t(
        '通路{つうろ}を横断{おうだん}する手順{てじゅん}を正{ただ}しい順番{じゅんばん}に並{なら}べる。',
        'Arrange the steps for crossing a walkway in the correct order.',
        'হাঁটার পথ পার হওয়ার ধাপগুলো সঠিক ক্রমে সাজাও।',
      ),
      steps: [
        t(
          '停止線{ていしせん}（白線{はくせん}）の手前{てまえ}で足{あし}を止{と}める',
          'Stop your feet before the stop line (white line)',
          'স্টপ লাইনের (সাদা দাগ) সামনে পা থামাও',
        ),
        t(
          '右{みぎ}を指{ゆび}差{さ}して「右{みぎ}ヨシ！」と確認{かくにん}',
          'Point right and confirm "Right clear, yoshi!"',
          'ডানে আঙুল তাক করে "ডান ঠিক আছে!" নিশ্চিত করো',
        ),
        t(
          '左{ひだり}を指{ゆび}差{さ}して「左{ひだり}ヨシ！」と確認{かくにん}',
          'Point left and confirm "Left clear, yoshi!"',
          'বামে আঙুল তাক করে "বাম ঠিক আছে!" নিশ্চিত করো',
        ),
        t(
          '前{まえ}を指{ゆび}差{さ}して「前{まえ}ヨシ！」と声{こえ}を出{だ}して渡{わた}る',
          'Point ahead, call "Ahead clear, yoshi!", and cross',
          'সামনে নির্দেশ করে "সামনে ঠিক আছে!" বলে পার হও',
        ),
      ],
    },
  ],
  inspect: [
    {
      id: 'posture-check',
      defective: false,
      name: t(
        '指差呼称{しさこしょう}の基本{きほん}フォーム',
        'Basic form of pointing and calling',
        'পয়েন্টিং ও কলিংয়ের মৌলিক ভঙ্গি',
      ),
      spots: [
        {
          x: 48,
          y: 22,
          check: t(
            '目線{めせん}：対象{たいしょう}をしっかり見{み}つめている（良品{りょうひん}：直視{ちょくし} / 不良{ふりょう}：よそ見{み}）',
            'Eye contact: Looking firmly at the target (Good: Direct focus / Defect: Looking away)',
            'চোখের দৃষ্টি: লক্ষ্যবস্তুর দিকে স্থির (ভালো: সরাসরি দৃষ্টি / ত্রুটি: অন্যমনস্ক)',
          ),
        },
        {
          x: 72,
          y: 38,
          check: t(
            '腕{うで}と指{ゆび}：肩{かた}からまっすぐ伸{の}びている（良品{りょうひん}：一直線{いっちょくせん} / 不良{ふりょう}：曲{ま}がっている）',
            'Arm and finger: Extended straight from the shoulder (Good: Straight line / Defect: Bent)',
            'হাত ও আঙুল: কাঁধ থেকে সোজা প্রসারিত (ভালো: সোজা লাইন / ত্রুটি: বাঁকানো)',
          ),
        },
        {
          x: 48,
          y: 78,
          check: t(
            '立{た}ち位置{いち}：足{あし}を止{と}めて安定{あんてい}している（良品{りょうひん}：静止{せいし} / 不良{ふりょう}：歩{ある}きながら）',
            'Stance: Stopped and stable on both feet (Good: Stationary / Defect: Moving)',
            'দাঁড়ানোর অবস্থান: পা থেমে স্থির ও ভারসাম্যপূর্ণ (ভালো: স্থির / ত্রুটি: চলমান)',
          ),
        },
      ],
    },
    {
      id: 'breaker-panel',
      defective: true,
      name: t(
        '動力盤{どうりょくばん}の通電{つうでん}前{まえ}点検{てんけん}',
        'Power distribution panel pre-energizing inspection',
        'পাওয়ার প্যানেল বিদ্যুৎ সংযোগের আগের পরীক্ষা',
      ),
      spots: [
        {
          x: 32,
          y: 30,
          check: t(
            '主開閉器{しゅかいへいき}：レバーがOFF位置{いち}にあるか（良品{りょうひん}：OFF表示{ひょうじ} / 不良{ふりょう}：ONのまま）',
            'Main breaker: Lever in OFF position (Good: Shows OFF / Defect: Left in ON position)',
            'মেইন ব্রেকার: লিভার বন্ধ অবস্থানে আছে কিনা (ভালো: OFF নির্দেশ / ত্রুটি: ON অবস্থায় থাকা)',
          ),
        },
        {
          x: 68,
          y: 62,
          check: t(
            '盤{ばん}の扉{とびら}：ハンドルが施錠{せじょう}されているか（良品{りょうひん}：施錠{せじょう}済{ず}み / 不良{ふりょう}：半開{はんびら}き・開{あ}き）',
            'Panel door: Handle locked properly (Good: Latched / Defect: Ajar or loose)',
            'প্যানেলের দরজা: হ্যান্ডেল ঠিকমতো লক করা কিনা (ভালো: লক করা / ত্রুটি: আধখোলা)',
          ),
        },
      ],
    },
    {
      id: 'crossing-spot',
      defective: false,
      name: t(
        '工場内{こうじょうない}交差点{こうさてん}の確認{かくにん}設備{せつび}',
        'Factory intersection confirmation facilities',
        'কারখানার রাস্তার মোড়ের নিরাপত্তা নিশ্চিতকরণ ব্যবস্থা',
      ),
      spots: [
        {
          x: 28,
          y: 28,
          check: t(
            'カーブミラー：割{わ}れや曇{くも}りがなく見{み}通{とお}せる（良品{りょうひん}：鮮明{せんめい} / 不良{ふりょう}：破損{はそん}・汚損{おそん}）',
            'Curved mirror: Clean with clear visibility and no cracks (Good: Clear / Defect: Broken)',
            'কনভেক্স মিরর: ভাঙা বা ঘোলা ছাড়া পরিষ্কার দৃশ্য (ভালো: স্পষ্ট / ত্রুটি: ভাঙা বা নোংরা)',
          ),
        },
        {
          x: 70,
          y: 75,
          check: t(
            '停止線{ていしせん}：路面{ろめん}の白線{はくせん}がはっきり見{み}える（良品{りょうひん}：明瞭{めいりょう} / 不良{ふりょう}：かすれ・消失{しょうしつ}）',
            'Stop line: White line on floor clearly visible (Good: Clear / Defect: Faded)',
            'স্টপ লাইন: মেঝের সাদা দাগ স্পষ্টভাবে দৃশ্যমান (ভালো: স্পষ্ট / ত্রুটি: মুছে যাওয়া)',
          ),
        },
      ],
    },
  ],
  quiz: [
    {
      q: t(
        '機械{きかい}を始動{しどう}する前{まえ}に指差呼称{しさこしょう}をするとき、正{ただ}しい姿勢{しせい}はどれですか？',
        'When performing pointing and calling before starting a machine, what is the correct posture?',
        'মেশিন চালু করার আগে পয়েন্টিング ও কলিং করার সময় সঠিক ভঙ্গি কোনটি?',
      ),
      options: [
        t(
          '足{あし}を止{と}め、腕{うで}をまっすぐ伸{の}ばして対象{たいしょう}を指{ゆび}差{さ}す',
          'Stop your feet, extend your arm straight, and point at the target',
          'পা থামিয়ে হাত সোজা বাড়িয়ে লক্ষ্যবস্তুর দিকে নির্দেশ করা',
        ),
        t(
          '歩{ある}きながら手{て}首{くび}だけを動{うご}かして指{ゆび}差{さ}す',
          'Point by flicking only your wrist while walking',
          'হাঁটতে হাঁটতে শুধু কব্জি নাড়িয়ে আঙুল দেখানো',
        ),
        t(
          '次{つぎ}の作業{さぎょう}を見{み}ながら素早{すばや}く指{ゆび}差{さ}す',
          'Point quickly while looking ahead at the next task',
          'পরের কাজের দিকে তাকিয়ে রেখে দ্রুত আঙুল দেখানো',
        ),
        t(
          '声{こえ}を出{だ}さずにうなずくだけにする',
          'Just nod silently without speaking',
          'মুখে না বলে শুধু মাথা নাড়ানো',
        ),
      ],
      answer: 0,
      why: t(
        '指差呼称{しさこしょう}は足{あし}を止{と}めて対象{たいしょう}を直視{ちょくし}し、腕{うで}をまっすぐ伸{の}ばして声{こえ}を出{だ}すのが正{ただ}しい基本{きほん}姿勢{しせい}です。',
        'The correct posture is to stop your feet, look straight at the target, extend your arm, and speak aloud.',
        'পা থামিয়ে লক্ষ্যবস্তুর দিকে সরাসরি তাকানো, হাত সোজা বাড়ানো এবং স্পষ্ট কণ্ঠে উচ্চারণ করাই সঠিক ভঙ্গি।',
      ),
    },
    {
      q: t(
        '一人{ひとり}だけで作業{さぎょう}をしている時{とき}、指差呼称{しさこしょう}はどうすべきですか？',
        'When working completely alone, how should you perform pointing and calling?',
        'সম্পূর্ণ একা কাজ করার সময় পয়েন্টিング ও কলিং কীভাবে করা উচিত?',
      ),
      options: [
        t(
          '周{まわ}りに人{ひと}がいないので、省略{しょうりゃく}してよい',
          'You may skip it since nobody is around',
          'আশেপাশে কেউ না থাকায় এটি বাদ দেওয়া যায়',
        ),
        t(
          '一人{ひとり}でも省略{しょうりゃく}せず、必{かなら}ず声{こえ}を出{だ}して指{ゆび}差{さ}す',
          'Never skip it even alone; always point and speak aloud',
          'একা হলেও বাদ না দিয়ে অবশ্যই আঙুল দেখিয়ে মুখে বলবে',
        ),
        t(
          '恥{は}ずかしいので心{こころ}の中{なか}でつぶやくだけにする',
          'Only whisper it in your mind to avoid feeling embarrassed',
          'লজ্জা লাগায় মনে মনে আওড়ালেই চলবে',
        ),
        t(
          '職長{しょくちょう}が見{み}ている時{とき}だけ声{こえ}を出{だ}す',
          'Speak aloud only when the foreman is watching',
          'ফোরম্যান যখন দেখছেন কেবল তখনই মুখে বলবে',
        ),
      ],
      answer: 1,
      why: t(
        '指差呼称{しさこしょう}は自分{じぶん}の意識{いしき}を高{たか}めてミスを防{ふせ}ぐ行動{こうどう}なので、一人{ひとり}作業{さぎょう}でも必{かなら}ず声{こえ}を出{だ}して行{おこな}います。',
        'Pointing and calling heightens your own awareness and prevents mistakes, so you must always speak aloud even when working alone.',
        'পয়েন্টিング ও কলিং নিজের মনোযোগ বাড়ায় ও ভুল রোধ করে, তাই একা কাজ করলেও অবশ্যই মুখে বলে এটি করতে হবে।',
      ),
    },
    {
      q: t(
        '工場内{こうじょうない}の通路{つうろ}を横断{おうだん}するとき、最初{さいしょ}に行{おこな}うべき動作{どうさ}は何{なん}ですか？',
        'When crossing a walkway inside the factory, what is the first action you should take?',
        'কারখানার ভেতরে হাঁটার পথ পার হওয়ার সময় প্রথমে কী করা উচিত?',
      ),
      options: [
        t(
          '早足{はやあし}で素早{すばや}く通{とお}り抜{ぬ}ける',
          'Quickly run across with brisk steps',
          'দ্রুত পা চালিয়ে তাড়াতাড়ি পার হয়ে যাওয়া',
        ),
        t(
          '右{みぎ}だけを見{み}てそのまま渡{わた}る',
          'Look only to the right and cross immediately',
          'কেবল ডান দিকে তাকিয়ে সাথে সাথে পার হওয়া',
        ),
        t(
          '白線{はくせん}（停止線{ていしせん}）の手前{てまえ}で足{あし}を止{と}める',
          'Stop your feet in front of the white stop line',
          'সাদা দাগের (স্টপ লাইন) সামনে পুরোপুরি পা থামানো',
        ),
        t(
          'スマートフォンを見{み}ながら手{て}をあげる',
          'Raise your hand while looking at your smartphone',
          'স্মার্টফোনের দিকে তাকিয়ে হাত তোলা',
        ),
      ],
      answer: 2,
      why: t(
        '歩{ある}きながらの確認{かくにん}は危険{きけん}です。まず白線{はくせん}の手前{てまえ}で足{あし}を止{と}め、左右{さゆう}の安全{あんぜん}を指差{ゆびさ}しで確認{かくにん}します。',
        'Checking while walking is dangerous; first stop before the white line, then point and check left and right safely.',
        'হাঁটতে হাঁটতে দেখা বিপজ্জনক; প্রথমে সাদা দাগের সামনে পা থামাতে হবে, তারপর ডানে-বামে আঙুল দেখিয়ে নিশ্চিত করতে হবে।',
      ),
    },
    {
      q: t(
        '呼称{こしょう}の最後{さいご}に「ヨシ！」と言{い}う理由{りゆう}として、最も適切なものはどれですか？',
        'What is the most appropriate reason for saying "Yoshi!" at the end of the call?',
        'ডাকের শেষে "ঠিক আছে (ইয়োশি)!" বলার সবচেয়ে যথার্থ কারণ কোনটি?',
      ),
      options: [
        t(
          '大{おお}きな声{こえ}を出{だ}して周{まわ}りの人{ひと}を驚{おどろ}かせるため',
          'To startle people around you with a loud voice',
          'উচ্চকণ্ঠে চিৎকার করে আশেপাশের মানুষকে চমকে দেওয়ার জন্য',
        ),
        t(
          '作業{さぎょう}が速{はや}く終{お}わったことを合図{あいず}するため',
          'To signal that the task was finished quickly',
          'কাজটি দ্রুত শেষ হয়েছে তার ইঙ্গিত দেওয়ার জন্য',
        ),
        t(
          '形{かたち}だけルールを守{まも}っているように見{み}せるため',
          'To pretend that you are following the rules',
          'লোকদেখানো নিয়ম মানার অভিনয় করার জন্য',
        ),
        t(
          '状態{じょうたい}が正常{せいじょう}であることを自分{じぶん}の耳{みみ}と脳{のう}で最終{さいしゅう}確認{かくにん}するため',
          'To make final confirmation with your own ears and brain that conditions are normal',
          'অবস্থা স্বাভাবিক আছে তা নিজের কান ও মস্তিষ্ক দিয়ে চূড়ান্তভাবে নিশ্চিত করার জন্য',
        ),
      ],
      answer: 3,
      why: t(
        '自分{じぶん}の声{こえ}で「ヨシ！」と発声{はっせい}し、それを耳{みみ}で聞{き}くことで、確認{かくにん}結果{けっか}が脳{のう}に深{ふか}く定着{ていちゃく}します。',
        'By calling "Yoshi!" and hearing it with your own ears, the confirmation is firmly registered in your brain.',
        'নিজের কণ্ঠে "ইয়োশি!" বলে তা কানে শুনলে নিশ্চিতকরণের বার্তাটি মস্তিষ্কে গভীরভাবে গেঁথে যায়।',
      ),
    },
    {
      q: t(
        '鉄道総合技術研究所{てつどうそうごうぎじゅつけんきゅうしょ}（JR総研{そうけん}）の実験{じっけん}で明{あき}らかになった指差呼称{しさこしょう}の効果{こうか}はどれですか？',
        'What effect of pointing and calling was proven in the experiment by the Railway Technical Research Institute (RTRI)?',
        'রেলওয়ে টেকনিক্যাল রিসার্চ ইনস্টিটিউট (JR সো-কেন)-এর পরীক্ষায় পয়েন্টিング ও কলিংয়ের কী প্রভাব প্রমাণিত হয়েছে?',
      ),
      options: [
        t(
          '何{なに}もしない場合{ばあい}に比{くら}べて、作業{さぎょう}のエラーが大幅{おおはば}に減少{げんしょう}する',
          'Operational errors are reduced drastically compared to doing nothing',
          'কোনো কিছু না করার তুলনায় কাজের ভুল উল্লেখযোগ্য পরিমাণে কমে যায়',
        ),
        t(
          '作業{さぎょう}のスピードが2倍{ばい}に速{はや}くなる',
          'Work speed becomes twice as fast',
          'কাজের গতি দ্বিগুণ বেড়ে যায়',
        ),
        t(
          '機械{きかい}の電気代{でんきだい}が安{やす}くなる',
          'Electricity cost of the machinery becomes cheaper',
          'মেশিনের বিদ্যুৎ খরচ কমে যায়',
        ),
        t(
          '保護具{ほごぐ}を着用{ちゃくよう}しなくても安全{あんぜん}になる',
          'It becomes safe even without wearing protective equipment',
          'পিপিই বা নিরাপত্তা সরঞ্জাম না পরলেও নিরাপদ থাকা যায়',
        ),
      ],
      answer: 0,
      why: t(
        '指差呼称{しさこしょう}を行{おこな}うことで、ミスや見落{みお}としのエラー発生率{はっせいりつ}が大{おお}きく減少{げんしょう}することが実験{じっけん}で実証{じっしょう}されています。',
        'Experiments have verified that pointing and calling substantially reduces the rate of human error and oversights.',
        'পরীক্ষায় প্রমাণিত হয়েছে যে পয়েন্টিング ও কলিং করলে মানুষের ভুল ও অসতর্কতার হার ব্যাপকভাবে কমে যায়।',
      ),
    },
    {
      q: t(
        'クレーン作業{さぎょう}で玉掛{たまが}けを行{おこな}う際{さい}、巻{ま}き上{あ}げる前{まえ}の指差呼称{しさこしょう}として正{ただ}しいものはどれですか？',
        'When slinging for crane work, which pointing and calling line is correct before hoisting?',
        'ক্রেন দিয়ে মাল তোলার সময় উত্তোলনের আগে কোন পয়েন্টিング ও কলিংটি সঠিক?',
      ),
      options: [
        t(
          '「誰{だれ}か下{した}にいても、巻{ま}き上{あ}げ、ヨシ！」',
          'Hoist even if someone is underneath, yoshi!',
          'নিচে কেউ থাকলেও তোলো, ঠিক আছে!',
        ),
        t(
          '「吊{つ}り荷{に}の下{した}、立入{たちいり}なし、ヨシ！」',
          'Under the load, clear of people, yoshi!',
          'ঝুলন্ত মালের নিচে কেউ নেই, ঠিক আছে!',
        ),
        t(
          '「ワイヤーが古{ふる}くても、大丈夫{だいじょうぶ}、ヨシ！」',
          'The wire is old but it is fine, yoshi!',
          'তার পুরনো হলেও চলবে, ঠিক আছে!',
        ),
        t(
          '「合図{あいず}なしで、どんどん巻{ま}き上{あ}げ、ヨシ！」',
          'Hoist quickly without signals, yoshi!',
          'ইশারা ছাড়াই দ্রুত তোলো, ঠিক আছে!',
        ),
      ],
      answer: 1,
      why: t(
        '吊{つ}り荷{に}の下{した}への立入{たちいり}は極{きわ}めて危険{きけん}です。必{かなら}ず人{ひと}がいないことを指差{ゆびさ}し確認{かくにん}してから巻{ま}き上{あ}げます。',
        'Standing under a suspended load is extremely hazardous; you must always point and confirm nobody is beneath before hoisting.',
        'ঝুলন্ত মালের নিচে যাওয়া চরম ঝুঁকিপূর্ণ; মাল তোলার আগে অবশ্যই কেউ নেই তা নির্দেশ করে নিশ্চিত হতে হবে।',
      ),
    },
    {
      q: t(
        'スイッチを指{ゆび}差{さ}したとき、表示{ひょうじ}が「ON」になっていました。本来{ほんらい}は「OFF」のはずです。どうすべきですか？',
        'When pointing at a switch, the indicator showed "ON", but it was supposed to be "OFF". What should you do?',
        'সুইচে আঙুল তাক করে দেখলে নির্দেশক "ON" দেখাচ্ছে, অথচ তা "OFF" থাকার কথা। তোমার কী করা উচিত?',
      ),
      options: [
        t(
          '見{み}なかったことにして「OFF、ヨシ！」と声{こえ}を出{だ}す',
          'Pretend not to see it and call "OFF, yoshi!" anyway',
          'না দেখার ভান করে মুখে "বন্ধ, ঠিক আছে!" বলে দেওয়া',
        ),
        t(
          '自分{じぶん}の判断{はんだん}ですぐにスイッチをOFFに切{き}り替{か}えて作業{さぎょう}を続{つづ}ける',
          'Switch it to OFF on your own judgment and keep working without asking',
          'নিজের বুদ্ধিতে সাথে সাথে বন্ধ করে কাজ চালিয়ে যাওয়া',
        ),
        t(
          '操作{そうさ}を止{と}めて足{あし}を止{と}め、直{ただ}ちに職長{しょくちょう}に状況{じょうきょう}を報告{ほうこく}する',
          'Stop the operation, pause your work, and report the situation to the foreman immediately',
          'কাজ থামিয়ে সাথে সাথে ফোরম্যানকে অবস্থাটি জানানো',
        ),
        t(
          '隣{となり}の作業者{さぎょうしゃ}に「見{み}ておいて」と頼{たの}んで放置{ほうち}する',
          'Ask a nearby worker to keep an eye on it and leave it alone',
          'পাশের কর্মীকে "একটু দেখে রেখো" বলে ফেলে রাখা',
        ),
      ],
      answer: 2,
      why: t(
        '想定{そうてい}と異{こと}なる異常{いじょう}を発見{はっけん}した時{とき}は、勝手{かって}に操作{そうさ}せず「止{と}める・呼{よ}ぶ・待{ま}つ」を守{まも}り職長{しょくちょう}に報告{ほうこく}します。',
        'When discovering an abnormality, never act arbitrarily; follow "Stop, Call, Wait" and report to the foreman.',
        'প্রত্যাশিত অবস্থার অমিল বা ত্রুটি দেখলে নিজে নিজে সিদ্ধান্ত না নিয়ে কাজ থামিয়ে ফোরম্যানকে রিপোর্ট করতে হবে।',
      ),
    },
    {
      q: t(
        '指差呼称{しさこしょう}をするときに、避{さ}けるべき「悪{わる}いクセ」はどれですか？',
        'Which of the following is a "bad habit" that must be avoided when pointing and calling?',
        'পয়েন্টিング ও কলিং করার সময় বর্জনীয় "খারাপ অভ্যাস" কোনটি?',
      ),
      options: [
        t(
          '対象{たいしょう}の名前{なまえ}と状態{じょうたい}を具体的{ぐたいてき}に呼称{こしょう}する',
          'Calling out the target name and its state concretely',
          'লক্ষ্যবস্তুর নাম ও অবস্থা সুনির্দিষ্টভাবে উচ্চারণ করা',
        ),
        t(
          '確認{かくにん}する前{まえ}に足{あし}を止{と}めて呼吸{こきゅう}を整{ととの}える',
          'Stopping your feet and steadying your breath before checking',
          'দেখার আগে পা থামিয়ে শান্ত হওয়া',
        ),
        t(
          '腕{うで}を肩{かた}の高{たか}さからまっすぐ前{まえ}へ突{つ}き出{だ}す',
          'Extending your arm straight forward from shoulder height',
          'কাঁধের উচ্চতা থেকে হাত সোজা সামনের দিকে প্রসারিত করা',
        ),
        t(
          '目線{めせん}を他{ほか}へ向{む}けたまま手{て}先{さき}だけでパパッと指{ゆび}差{さ}す',
          'Flicking your finger while looking away elsewhere',
          'অন্যদিকে তাকিয়ে রেখে শুধু আঙুল নাড়িয়ে দায়সারাভাবে দেখানো',
        ),
      ],
      answer: 3,
      why: t(
        '対象{たいしょう}を見{み}ずに形{かたち}だけで指{ゆび}差{さ}しても、脳{のう}の確認{かくにん}が働{はたら}かずヒューマンエラーを防{ふせ}げません。',
        'Pointing only in form without looking directly does not engage brain verification and fails to prevent human error.',
        'লক্ষ্যবস্তুর দিকে না তাকিয়ে কেবল অঙ্গভঙ্গি করলে মস্তিষ্ক সতর্ক হয় না এবং ভুল ঠেকানো যায় না।',
      ),
    },
    {
      q: t(
        '研削盤{けんさくばん}（グラインダー）を使{つか}う前{まえ}の指差呼称{しさこしょう}として、適切{てきせつ}なものはどれですか？',
        'Which pointing and calling is appropriate before using a grinding machine?',
        'গ্রাইন্ডিং মেশিন ব্যবহারের আগে কোন পয়েন্টিング ও কলিংটি উপযুক্ত?',
      ),
      options: [
        t(
          '「といしカバー、固定{こてい}、ヨシ！」',
          'Grinding wheel cover, secured, yoshi!',
          'হুইল কভার, শক্তভাবে আটকানো, ঠিক আছে!',
        ),
        t(
          '「火花{ひばな}が飛{と}んでも、我慢{がまん}、ヨシ！」',
          'Tolerate flying sparks, yoshi!',
          'স্ফুলিঙ্গ ছিটকে এলেও সহ্য করব, ঠিক আছে!',
        ),
        t(
          '「保護{ほご}メガネなし、視界{しかい}良好{りょうこう}、ヨシ！」',
          'No safety goggles, good vision, yoshi!',
          'গগলস ছাড়া দৃষ্টি পরিষ্কার, ঠিক আছে!',
        ),
        t(
          '「といしのヒビ、小{ちい}さいから、ヨシ！」',
          'The wheel crack is small, so yoshi!',
          'হুইলের ফাটল ছোট, তাই ঠিক আছে!',
        ),
      ],
      answer: 0,
      why: t(
        '回転{かいてん}砥石{といし}の破裂{はれつ}や飛散{ひさん}を防{ふせ}ぐため、安全{あんぜん}カバーが確実{かくじつ}に固定{こてい}されていることを確認{かくにん}します。',
        'To prevent burst or flying fragments of the wheel, ensure that the safety cover is firmly secured.',
        'ঘূর্ণায়মান চাকা ফেটে যাওয়া বা টুকরো ছিটকে আসা ঠেকাতে সেফটি কভার শক্তভাবে লাগানো নিশ্চিত করতে হবে।',
      ),
    },
    {
      q: t(
        'フォークリフトを停止{ていし}して離{はな}れる際{さい}の指差呼称{しさこしょう}で、正{ただ}しい項目{こうもく}はどれですか？',
        'When parking and leaving a forklift, which pointing and calling item is correct?',
        'ফর্কলিফ্ট থামিয়ে চলে যাওয়ার সময় কোন পয়েন্টিング ও কলিংটি সঠিক?',
      ),
      options: [
        t(
          '「キーを挿{さ}したまま、放置{ほうち}、ヨシ！」',
          'Leave the key inserted, yoshi!',
          'চাবি লাগানো অবস্থায় ফেলে রাখা, ঠিক আছে!',
        ),
        t(
          '「サイドブレーキ、掛{か}かり、爪{つめ}接地{せっち}、ヨシ！」',
          'Parking brake engaged, forks on the ground, yoshi!',
          'পার্কিং ব্রেক টানা, ফর্ক মাটিতে নামানো, ঠিক আছে!',
        ),
        t(
          '「爪{つめ}を高{たか}く上{あ}げたまま、ヨシ！」',
          'Forks raised high in the air, yoshi!',
          'ফর্কের কাঁটা উঁচুতে তুলে রাখা, ঠিক আছে!',
        ),
        t(
          '「通路{つうろ}の真{ま}ん中{なか}に駐車{ちゅうしゃ}、ヨシ！」',
          'Parked right in the middle of the aisle, yoshi!',
          'চলাচলের পথের ঠিক মাঝখানে পার্কিং, ঠিক আছে!',
        ),
      ],
      answer: 1,
      why: t(
        '無人{むじん}暴走{ぼうそう}や爪{つめ}への衝突{しょうとつ}を防{ふせ}ぐため、ブレーキと爪{つめ}の接地{せっち}、キー抜{ぬ}き取{と}りを指差{ゆびさ}し確認{かくにん}します。',
        'To prevent unintended runaway or tripping, confirm parking brake, forks lowered to ground, and key removed.',
        'অনিয়ন্ত্রিত চলাচল বা ফর্কে হোঁচট খাওয়া রোধে ব্রেক টানা, ফর্ক মাটিতে নামানো ও চাবি খোলা নিশ্চিত করতে হয়।',
      ),
    },
    {
      q: t(
        '指差呼称{しさこしょう}の4つの動作{どうさ}「目{め}・手{て}・口{くち}・耳{みみ}」で、「耳{みみ}」は何{なん}のために使{つか}いますか？',
        'Among the four actions of pointing and calling (eyes, hand, mouth, ears), what are the "ears" used for?',
        'পয়েন্টিング ও কলিংয়ের ৪টি অঙ্গের (চোখ, হাত, মুখ, কান) মধ্যে "কান" কী কাজে লাগে?',
      ),
      options: [
        t(
          '周{まわ}りの人{ひと}の雑談{ざつだん}を聞{き}くため',
          'To listen to coworkers chatting nearby',
          'আশেপাশের মানুষের আড্ডা বা কথা শোনার জন্য',
        ),
        t(
          '遠{とお}くのチャイムの音{おと}を聞{き}くため',
          'To listen for a distant bell or chime',
          ' দূরের সাইরেন বা ঘণ্টার শব্দ শোনার জন্য',
        ),
        t(
          '自分{じぶん}が発声{はっせい}した「ヨシ！」という声{こえ}を聞{き}いて確認{かくにん}するため',
          'To listen to your own voice calling "Yoshi!" for confirmation',
          'নিজের মুখ থেকে বলা "ইয়োশি!" শব্দ শুনে নিশ্চিত হওয়ার জন্য',
        ),
        t(
          '職長{しょくちょう}に叱{しか}られていないか聞{き}くため',
          'To listen for whether the foreman is scolding you',
          'ফোরম্যান বকা দিচ্ছেন কিনা তা শোনার জন্য',
        ),
      ],
      answer: 2,
      why: t(
        '自分{じぶん}の声{こえ}を耳{みみ}で聞{き}く（聴覚{ちょうかく}フィードバック）ことで、確認{かくにん}した事実{じじつ}を脳{のう}により深{ふか}く認識{にんしき}させます。',
        'Hearing your own voice (auditory feedback) helps your brain register the confirmed fact much more deeply.',
        'নিজের কণ্ঠ নিজের কানে শুনলে মস্তিষ্কে নিশ্চিত করার তথ্যটি আরো গভীরভাবে প্রোথিত হয়।',
      ),
    },
  ],
  glossary: [
    {
      ja: '指差呼称',
      reading: 'しさこしょう',
      en: 'Pointing and calling',
      bn: 'পয়েন্টিং এবং কলিং (ইঙ্গিত ও মুখে বলা)',
      note: t(
        '目と手と声で安全を確認する手法。',
        'Method of confirming safety using eyes, hand, and voice.',
        'চোখ, হাত ও কণ্ঠ দিয়ে নিরাপত্তা নিশ্চিত করার পদ্ধতি।',
      ),
    },
    {
      ja: 'ヨシ',
      reading: 'よし',
      en: 'All clear / Good',
      bn: 'ঠিক আছে (ইয়োশি)',
      note: t(
        '指差呼称の最後に状態が正常であることを宣言する言葉。',
        'Word declared at the end of pointing and calling to state that conditions are normal.',
        'পয়েন্টিング ও কলিংয়ের শেষে অবস্থা স্বাভাবিক তা ঘোষণার শব্দ।',
      ),
    },
    {
      ja: 'ゼロ災運動',
      reading: 'ぜろさいうんどう',
      en: 'Zero-accident campaign',
      bn: 'জিরো-অ্যাক্সিডেন্ট আন্দোলন',
      note: t(
        '働く人全員で職場の危険をゼロにする安全運動。',
        'Safety movement where all workers collaborate to eliminate site hazards.',
        'সব কর্মীর অংশগ্রহণে কর্মক্ষেত্রের বিপদ শূন্যে নামানোর আন্দোলন।',
      ),
    },
    {
      ja: '注視',
      reading: 'ちゅうし',
      en: 'Close observation / gazing',
      bn: 'মনোযোগ দিয়ে পর্যবেক্ষণ / স্থির দৃষ্টি',
      note: t(
        '確認する対象から目をそらさずしっかり見つめること。',
        'Looking steadily at the target without looking away.',
        'লক্ষ্যবস্তু থেকে দৃষ্টি না সরিয়ে গভীরভাবে তাকানো।',
      ),
    },
    {
      ja: '職長',
      reading: 'しょくちょう',
      en: 'Foreman / supervisor',
      bn: 'ফোরম্যান / সাইট সুপারভাইজার',
      note: t(
        '作業現場で直接労働者を指揮・監督する責任者。',
        'Person responsible for directly directing and supervising workers on site.',
        'কাজের সাইটে কর্মীদের সরাসরি পরিচালনা ও তদারকির দায়িত্বপ্রাপ্ত ব্যক্তি।',
      ),
    },
    {
      ja: 'ヒヤリ・ハット',
      reading: 'ひやりはっと',
      en: 'Near-miss incident',
      bn: 'অল্পের জন্য রক্ষা (নিয়ার-মিস)',
      note: t(
        '一歩間違えれば重大な事故になった危険な出来事。',
        'Dangerous incident that could have caused a serious injury if slightly different.',
        'একটু ভুলেই বড় দুর্ঘটনা হতে পারত এমন ঝুঁকিপূর্ণ ঘটনা।',
      ),
    },
    {
      ja: '一時停止',
      reading: 'いちじていし',
      en: 'Temporary stop / pause',
      bn: 'সাময়িক থামা / স্টপ',
      note: t(
        '交差点や通路の手前で完全に足を止めること。',
        'Completely stopping your feet before intersections or walkways.',
        'মোড়ে বা হাঁটার পথের সামনে পা পুরোপুরি থামানো।',
      ),
    },
    {
      ja: '危険予知',
      reading: 'きけんよち',
      en: 'Hazard prediction (KYT)',
      bn: 'বিপদ পূর্বাভাস (কেওয়াইটি)',
      note: t(
        '作業前に潜む危険を話し合って予測・対策する活動。',
        'Activity of predicting hazards and taking countermeasures before work.',
        'কাজ শুরুর আগেই সম্ভাব্য বিপদ নিয়ে আলোচনা ও সতর্কতামূলক ব্যবস্থা।',
      ),
    },
    {
      ja: '主電源',
      reading: 'しゅでんげん',
      en: 'Main power supply',
      bn: 'মেইন পাওয়ার / প্রধান বিদ্যুৎ সংযোগ',
      note: t(
        '機械や設備の電力を一括して入切する大元のスイッチ。',
        'Main master switch that turns power ON or OFF for equipment.',
        'মেশিন বা যন্ত্রপাতির বিদ্যুৎ একযোগে চালু বা বন্ধের প্রধান সুইচ।',
      ),
    },
    {
      ja: '誤操作',
      reading: 'ごそうさ',
      en: 'Erroneous operation',
      bn: 'ভুল পরিচালনা / অসাবধানতাবশত বোতাম টেপা',
      note: t(
        '押し間違いやレバーの誤認などによる間違った操作。',
        'Mistaken operation caused by pressing wrong buttons or misidentifying levers.',
        'ভুল বোতাম টেপা বা লিভারের ভুলে অনিচ্ছাকৃতভাবে মেশিন চালানো।',
      ),
    },
    {
      ja: '報連相',
      reading: 'ほうれんそう',
      en: 'Report, communicate, consult (Ho-Ren-So)',
      bn: 'রিপোর্ট, যোগাযোগ ও পরামর্শ (হো-রেন-সো)',
      note: t(
        '報告・連絡・相談の頭文字をとった日本職場の基本習慣。',
        'Japanese workplace habit taking the initials of report, contact, consult.',
        'জাপানি কর্মক্ষেত্রের মৌলিক রীতি: তথ্য জানানো, যোগাযোগ ও পরামর্শ করা।',
      ),
    },
    {
      ja: '不安全行動',
      reading: 'ふあんぜんこうどう',
      en: 'Unsafe behavior',
      bn: 'অনিরাপদ আচরণ',
      note: t(
        '手順の省略や不注意など、事故の原因となる行動。',
        'Actions causing accidents, such as skipping procedures or carelessness.',
        'নিয়ম না মানা বা অসাবধানতা, যা দুর্ঘটনার কারণ ঘটায়।',
      ),
    },
  ],
}
