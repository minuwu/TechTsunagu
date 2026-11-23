import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string, bn: string): Tri => ({ ja, en, bn })

export const genbaWords: CourseData = {
  id: 'genba-words',
  category: 'language',
  level: 1,
  minutes: 15,
  title: t(
    '現場用語{げんばようご}',
    'Workplace Japanese',
    'কাজের জায়গার প্রয়োজনীয় জাপানি ভাষা',
  ),
  sub: t(
    '現場{げんば}で飛{と}び交{か}う言葉{ことば}と合図{あいず}を身{み}につけ、安全{あんぜん}に作業{さぎょう}する。',
    'Master workplace terms and signals heard on site every day to work safely.',
    'প্রতিদিন কাজের জায়গায় ব্যবহৃত ভাষা ও ইশারা শিখে নিরাপদে কাজ করো।',
  ),
  refs: [
    '労働安全衛生法 第59条（雇入れ時等の安全衛生教育）',
    '厚生労働省 外国人労働者の安全衛生対策についてのガイドライン',
    '中央労働災害防止協会 ゼロ災害全員参加運動推進要綱（KYT・指差呼称）',
  ],
  panels: [
    {
      who: 'foreman',
      text: t(
        '今日{きょう}から現場{げんば}に入{はい}ります。「段取{だんど}り」や「合図{あいず}」などの現場用語{げんばようご}を学{まな}びましょう。',
        'You start on site today. Let us learn workplace terms like "dandori" and signals.',
        'আজ থেকে তুমি সাইটে কাজ শুরু করছ। চলো "দানদোরি" ও ইশারার মতো কাজের জায়গার ভাষা শিখি।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '先輩{せんぱい}たちが「手元{てもと}」や「養生{ようじょう}」と言{い}っていましたが、どんな意味{いみ}ですか？',
        'I heard senior workers say "temoto" and "youjou". What do they mean?',
        'সিনিয়ররা বলছিলেন "তেমোতো" এবং "ইয়োজো"। এগুলোর মানে কী?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '「手元{てもと}」は職人{しょくにん}の作業{さぎょう}を補助{ほじょ}すること、「養生{ようじょう}」は床{ゆか}や製品{せいひん}をシートで保護{ほご}することです。',
        '"Temoto" means assisting a craftsman, and "youjou" means protecting floors or products with sheets.',
        '"তেমোতো" মানে কারিগরের কাজে সাহায্য করা, আর "ইয়োজো" মানে শিট দিয়ে মেঝে বা পণ্য ঢেকে সুরক্ষিত রাখা।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'わかりました！さっき職人{しょくにん}から工具{こうぐ}を頼{たの}まれたので、無言{むごん}で背後{はいご}からスパナを手渡{てわた}しました。',
        'Understood! Earlier a craftsman asked for a tool, so I handed him a spanner silently from behind.',
        'বুঝেছি! একটু আগে এক কারিগর টুল চাইলে আমি পেছন থেকে কোনো কথা না বলেই স্প্যানারটা বাড়িয়ে দিয়েছিলাম।',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'それは危{あぶ}ないです。急{きゅう}に手{て}を出{だ}さず、「スパナ行{い}きます」と必{かなら}ず声{こえ}をかけて渡{わた}します。',
        'That is dangerous. Never reach out suddenly; always call out "Spanner coming!" before handing it over.',
        'ওটা বিপজ্জনক। হঠাৎ হাত বাড়াবে না; দেওয়ার আগে অবশ্যই "স্প্যানার দিচ্ছি" বলে ডেকে দেবে।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '声{こえ}を出{だ}さないと、相手{あいて}が振{ふ}り返{かえ}ったときにぶつかって怪我{けが}をするのですね。',
        'If I do not call out, the other person might turn around and bump into it, causing injury.',
        'মুখে না বললে অপরজন হঠাৎ ঘুরে দাঁড়ালে ধাক্কা লেগে আঘাত পেতে পারে, তাই না?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'その通{とお}りです。危険{きけん}なときは遠慮{えんりょ}せず、大声{おおごえ}で「危{あぶ}ない！」「止{と}めて！」と叫{さけ}んでください。',
        'Exactly. In dangerous moments, do not hesitate; shout "Danger!" or "Stop!" at the top of your voice.',
        'ঠিক তাই। কোনো বিপদ দেখলে দ্বিধা না করে জোরে চিৎকার করে "বিপদ!" বা "থামুন!" বলবে।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'クレーンの合図{あいず}でも、「上{あ}げて」「下{さ}げて」「右{みぎ}」「左{ひだり}」と短{みじか}くハッキリ伝{つた}えます！',
        'Even for crane signals, I will say "Hoist", "Lower", "Right", and "Left" clearly and briefly!',
        'ক্রেনের ইশারা দেওয়ার সময়ও আমি "তোলো", "নামাও", "ডানে" ও "বামে" স্পষ্টভাবে ও সংক্ষেপে বলব!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '素晴{すば}らしいです。「安全第一{あんぜんだいいち}」で作業{さぎょう}し、終{お}わったら「お疲{つか}れさまでした」と終礼{しゅうれい}をします。',
        'Wonderful. Work with "Safety First", and when finished, close the shift with "Thank you for your hard work".',
        'চমৎকার। "নিরাপত্তা সর্বাগ্রে" মেনে কাজ করব, আর কাজ শেষে "কাজের জন্য ধন্যবাদ" বলে সমাপনী মিটিং করব।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'はい！現場{げんば}の言葉{ことば}と正{ただ}しい合図{あいず}を身{み}につけて、今日{きょう}もゼロ災{さい}で頑張{がんば}ります！',
        'Yes! I will master site terms and correct signals, and do my best for zero accidents today!',
        'হ্যাঁ! সাইটের ভাষা ও সঠিক ইশারা শিখে আজকেও দুর্ঘটনা শূন্য রাখার জন্য সেরাটা দেব!',
      ),
    },
  ],
  handbook: [
    t(
      '指示{しじ}を受{う}けたら、自分{じぶん}の勝手{かって}な判断{はんだん}で動{うご}かず「はい、わかりました」と明確{めいかく}に返事{へんじ}をしなければならない。',
      'When receiving instructions, never act on your own guess; clearly reply "Yes, understood".',
      'নির্দেশ পেলে নিজের আন্দাজে কাজ না করে স্পষ্টভাবে "হ্যাঁ, বুঝেছি" বলে উত্তর দেবে।',
    ),
    t(
      '作業{さぎょう}を始{はじ}める前{まえ}に、必要{ひつよう}な工具{こうぐ}と手順{てじゅん}を揃{そろ}える「段取{だんど}り」を怠{おこた}ってはならない。',
      'Never neglect "dandori" (preparation) to organize needed tools and procedures before starting work.',
      'কাজ শুরু করার আগে প্রয়োজনীয় সরঞ্জাম ও ধাপগুলো গুছিয়ে নেওয়ার প্রস্তুতি (দানদোরি) অবহেলা করবে না।',
    ),
    t(
      '人{ひと}に工具{こうぐ}を手渡{てわた}すときは、無言{むごん}で差{さ}し出{だ}さず「行{い}きます」「手元{てもと}、どうぞ」と必{かなら}ず声{こえ}をかけなければならない。',
      'When passing tools to someone, never hand them silently; always call out "Coming through!" or "Here is the tool".',
      'কাউকে টুলস দেওয়ার সময় নিঃশব্দে বাড়িয়ে দেবে না; অবশ্যই "দিচ্ছি" বা "টুলটি নিন" বলে ডেকে দেবে।',
    ),
    t(
      '危険{きけん}を感{かん}じたり異常{いじょう}を見{み}つけたりしたときは、遠慮{えんりょ}せず大声{おおごえ}で「危{あぶ}ない！」「止{と}めて！」と叫{さけ}ばなければならない。',
      'If you sense danger or spot an abnormality, shout "Danger!" or "Stop!" loudly without hesitation.',
      'বিপদ আঁচ করলে বা কোনো অস্বাভাবিকতা দেখলে দ্বিধা না করে জোরে চিৎকার করে "বিপদ!" বা "থামুন!" বলবে।',
    ),
    t(
      '指示{しじ}された言葉{ことば}の意味{いみ}がわからないときは、そのまま作業{さぎょう}を続{つづ}けず「もう一度{いちど}お願{ねが}いします」と聞{き}き直{なお}さなければならない。',
      'If you do not understand a spoken instruction, never continue working; ask "Please say that once more".',
      'বলা কোনো নির্দেশের অর্থ না বুঝলে কাজ চালিয়ে যাবে না; "দয়া করে আরেকবার বলুন" বলে জেনে নেবে।',
    ),
    t(
      '床{ゆか}や壁{かべ}、製品{せいひん}を傷{きず}や火花{ひばな}から守{まも}るため、シートやテープによる「養生{ようじょう}」を省{はぶ}いてはならない。',
      'Never skip "youjou" (protective covering) with sheets or tape to protect floors, walls, and products from scratches and sparks.',
      'মেঝে, দেয়াল ও পণ্যকে আঁচড় বা আগুনের ফুলকি থেকে বাঁচাতে শিট বা টেপ দিয়ে সুরক্ষা (ইয়োজো) বাদ দেবে না।',
    ),
    t(
      '荷物{にもつ}の移動{いどう}やクレーン作業{さぎょう}では、「上{あ}げて」「下{さ}げて」「右{みぎ}」「左{ひだり}」「止{と}まれ」の合図{あいず}をハッキリ伝{つた}えなければならない。',
      'In crane operations and load handling, you must communicate signals like "Hoist", "Lower", "Right", "Left", and "Stop" clearly.',
      'মালামাল সরানো বা ক্রেনের কাজে "তোলো", "নামাও", "ডানে", "বামে" ও "থামো" ইশারাগুলো স্পষ্টভাবে দেবে।',
    ),
    t(
      '作業{さぎょう}が終{お}わったら、使{つか}った工具{こうぐ}をその場{ば}に放置{ほうち}せず、定位置{ていいち}へ「片付{かたづ}け」なければならない。',
      'When work is finished, never leave used tools lying around; you must tidy and return them to designated spots.',
      'কাজ শেষ হলে ব্যবহৃত টুলস সেখানে ফেলে রাখবে না; অবশ্যই নির্দিষ্ট স্থানে গুছিয়ে (কাতাজুকে) রাখবে।',
    ),
    t(
      '毎朝{まいあさ}の朝礼{ちょうれい}やKY（危険予知{きけんよち}）ミーティングには遅刻{ちこく}せず参加{さんか}し、危険{きけん}のポイントを確認{かくにん}しなければならない。',
      'You must attend the morning assembly and KY (hazard prediction) meeting on time to confirm safety points.',
      'প্রতিদিন সকালের সমাবেশ (চৌরেই) ও কেওয়াই মিটিংয়ে দেরি না করে অংশ নিয়ে বিপদের মূল বিষয়গুলো জেনে নেবে।',
    ),
    t(
      '「一服{いっぷく}」（小休止{しょうきゅうし}）の後{のち}は、保護具{ほごぐ}を再確認{さいかくにん}して「服装{ふくそう}ヨシ！」と指差呼称{しさこしょう}してから作業{さぎょう}へ戻{もど}らなければならない。',
      'After an "ippuku" (short break), recheck PPE, call "PPE check, yoshi!", and then return to work.',
      '"ইপ্পুকু" (ছোট বিরতি)-র পর পিপিই পুনরায় পরীক্ষা করে "পোশাক ঠিক আছে!" বলে আঙুল নির্দেশ করে কাজে ফিরবে।',
    ),
  ],
  reel: [
    t(
      '朝礼{ちょうれい}、時間{じかん}厳守{げんしゅ}、ヨシ！',
      'Morning briefing, on time, yoshi!',
      'সকালের সমাবেশ, সময়মতো, ঠিক আছে!',
    ),
    t(
      '本日{ほんじつ}の段取{だんど}り、確認{かくにん}、ヨシ！',
      'Today\'s workflow setup, confirmed, yoshi!',
      'আজকের কাজের প্রস্তুতি, নিশ্চিত, ঠিক আছে!',
    ),
    t(
      '足元{あしもと}の養生{ようじょう}、ズレなし、ヨシ！',
      'Floor protective covering, no slippage, yoshi!',
      'মেঝের সুরক্ষা শিট, নড়চড় নেই, ঠিক আছে!',
    ),
    t(
      '手元{てもと}の工具{こうぐ}、声{こえ}かけ、ヨシ！',
      'Handing tools, call out first, yoshi!',
      'সহকারীকে টুলস দেওয়া, ডেকে দেওয়া হয়েছে, ঠিক আছে!',
    ),
    t(
      '周囲{しゅうい}確認{かくにん}、行{い}きます、ヨシ！',
      'Surroundings clear, moving through, yoshi!',
      'চারপাশ নিরাপদ, যাচ্ছি, ঠিক আছে!',
    ),
    t(
      '危{あぶ}ない、一旦{いったん}停止{ていし}、ヨシ！',
      'Danger, pause and stop, yoshi!',
      'বিপদ, সাময়িক বিরতি, ঠিক আছে!',
    ),
    t(
      '作業後{さぎょうご}の片付{かたづ}け、完了{かんりょう}、ヨシ！',
      'Post-work cleanup, complete, yoshi!',
      'কাজ শেষে গোছানো, সম্পন্ন, ঠিক আছে!',
    ),
    t(
      '今日{きょう}も一日{いちにち}、安全第一{あんぜんだいいち}、ヨシ！',
      'All day today, safety first, yoshi!',
      'আজকের সারা দিন, নিরাপত্তা সর্বাগ্রে, ঠিক আছে!',
    ),
  ],
  drills: [
    {
      id: 'genba-words-hazard-spot',
      kind: 'spot',
      secs: 30,
      need: 4,
      name: t(
        '現場{げんば}の言葉{ことば}と行動{こうどう}の危険{きけん}チェック',
        'Workplace words and action hazard spotting',
        'কাজের জায়গার ভাষা ও আচরণের ঝুঁকি চিহ্নিতকরণ',
      ),
      goal: t(
        '30秒{びょう}で現場{げんば}の危険{きけん}な言葉{ことば}や不安全{ふあんぜん}な行動{こうどう}を4つ見{み}つける。',
        'Find 4 dangerous words or unsafe actions on the job site within 30 seconds.',
        '৩০ সেকেন্ডে কাজের জায়গার ৪টি বিপজ্জনক কথা বা অনিরাপদ আচরণ খুঁজে বের করো।',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '無言{むごん}で背後{はいご}から尖{とが}った工具{こうぐ}を手渡{てわた}す',
            'Handing sharp tools from behind silently',
            'পেছন থেকে কোনো কথা না বলে ধারালো টুল বাড়িয়ে দেওয়া',
          ),
        },
        {
          hazard: false,
          label: t(
            '「スパナ行{い}きます」と声{こえ}をかけてから手元{てもと}へ渡{わた}す',
            'Calling out "Spanner coming" before passing the tool',
            '"স্প্যানার দিচ্ছি" বলে ডেকে সহকারীর হাতে টুল তুলে দেওয়া',
          ),
        },
        {
          hazard: true,
          label: t(
            '指示{しじ}の意味{いみ}がわからないのに「はい」と答{こた}えて作業{さぎょう}する',
            'Saying "yes" and working despite not understanding instructions',
            'নির্দেশ না বুঝেও "হ্যাঁ" বলে কাজ শুরু করে দেওয়া',
          ),
        },
        {
          hazard: false,
          label: t(
            '聞き{き}取れ{とれ}ない指示{しじ}を「もう一度{いちど}お願{ねが}いします」と確認{かくにん}する',
            'Asking "Please say once more" when instructions are unclear',
            'অস্পষ্ট নির্দেশ "দয়া করে আরেকবার বলুন" বলে নিশ্চিত করা',
          ),
        },
        {
          hazard: true,
          label: t(
            '荷物{にもつ}が崩{くず}れそうなのに声{こえ}を出{だ}さず黙{だま}って見{み}ている',
            'Watching a tilting load silently without shouting a warning',
            'মালপত্র পড়ে যাওয়ার উপক্রম হলেও না ডেকে চুপচাপ দেখা',
          ),
        },
        {
          hazard: false,
          label: t(
            '危険{きけん}を見{み}つけて大声{おおごえ}で「止{と}めて！危{あぶ}ない！」と叫{さけ}ぶ',
            'Shouting "Stop! Danger!" immediately upon spotting a hazard',
            'বিপদ দেখামাত্র জোরে "থামুন! বিপদ!" বলে চিৎকার করা',
          ),
        },
        {
          hazard: true,
          label: t(
            'クレーンの合図{あいず}で声{こえ}を出{だ}さず曖昧{あいまい}に手{て}を振{ふ}る',
            'Waving hands vaguely without calling out crane signals',
            'ক্রেনের ইশারায় মুখে কোনো শব্দ না করে অস্পষ্টভাবে হাত নাড়ানো',
          ),
        },
        {
          hazard: false,
          label: t(
            '「巻上{まきあ}げ、ヨシ！」と短{みじか}く明確{めいかく}に呼称{こしょう}する',
            'Calling out "Hoist, yoshi!" clearly and concisely',
            '"তোলো, ঠিক আছে!" বলে স্পষ্ট ও সংক্ষেপে মুখে বলা',
          ),
        },
      ],
    },
    {
      id: 'dandori-and-work-seq',
      kind: 'seq',
      name: t(
        '朝礼{ちょうれい}から作業{さぎょう}開始{かいし}までの段取{だんど}り手順{てじゅん}',
        'Workflow sequence from morning briefing to work start',
        'সকালের সমাবেশ থেকে কাজ শুরু করার পূর্বপ্রস্তুতির ক্রম',
      ),
      goal: t(
        '朝礼{ちょうれい}から作業{さぎょう}に就{つ}くまでの正{ただ}しい流{なが}れを順番{じゅんばん}に並{なら}べる。',
        'Arrange the correct workflow from morning assembly to starting work.',
        'সকালের মিটিং থেকে কাজ শুরুর সঠিক ধারাবাহিক ধাপগুলো সাজাও।',
      ),
      steps: [
        t(
          '1. 朝礼{ちょうれい}に参加{さんか}して体調{たいちょう}報告{ほうこく}と全体{ぜんたい}指示{しじ}を聞{き}く',
          '1. Attend morning assembly, report health, and listen to site instructions',
          '১. সকালের সমাবেশে যোগ দিয়ে শারীরিক অবস্থা জানানো ও সাধারণ নির্দেশ শোনা',
        ),
        t(
          '2. TBM（ツールボックスミーティング）で危険{きけん}予知{よち}（KY）を行{おこな}う',
          '2. Conduct hazard prediction (KY) in the tool box meeting',
          '২. টুল বক্স মিটিংয়ে (টিবিএম) বিপদ পূর্বানুমান (কেওয়াই) চর্চা করা',
        ),
        t(
          '3. 本日{ほんじつ}の手順{てじゅん}を確認{かくにん}し、工具{こうぐ}と保護具{ほごぐ}を「段取{だんど}り」する',
          '3. Check today\'s steps and arrange tools and PPE (dandori)',
          '৩. আজকের কাজের ধাপ বুঝে সরঞ্জাম ও পিপিই গুছিয়ে প্রস্তুতি (দানদোরি) নেওয়া',
        ),
        t(
          '4. 作業{さぎょう}場所{ばしょ}の床{ゆか}や設備{せつび}に傷{きず}を防{ふせ}ぐ「養生{ようじょう}」を行{おこな}う',
          '4. Apply protective covering (youjou) to floors and equipment',
          '৪. কাজের জায়গার মেঝে ও সরঞ্জামে সুরক্ষার আবরণ (ইয়োজো) বিছানো',
        ),
        t(
          '5. 「周囲{しゅうい}ヨシ！」と指差呼称{しさこしょう}し、合図{あいず}と共{とも}に作業{さぎょう}を開始{かいし}する',
          '5. Call "Surroundings clear, yoshi!" and start work on signal',
          '৫. "চারপাশ ঠিক আছে!" আঙুল নির্দেশ করে ইশারার সাথে কাজ শুরু করা',
        ),
      ],
    },
    {
      id: 'abunai-emergency-callout-seq',
      kind: 'seq',
      name: t(
        '危険{きけん}発見時{はっけんじ}の「危{あぶ}ない・止{と}めて」対応{たいおう}手順{てじゅん}',
        'Emergency response sequence when spotting hazards',
        'বিপদ দেখার পর "বিপদ / থামুন" বলে পদক্ষেপ নেওয়ার ক্রম',
      ),
      goal: t(
        '作業場{さぎょうば}で急{きゅう}な危険{きけん}を見{み}つけたときの呼{よ}びかけ手順{てじゅん}を順番{じゅんばん}に並{なら}べる。',
        'Arrange the verbal warning and action steps when discovering immediate danger.',
        'কাজের জায়গায় হঠাৎ বিপদ দেখলে সতর্কবার্তা ও পদক্ষেপের ধাপগুলো সাজাও।',
      ),
      steps: [
        t(
          '1. 大声{おおごえ}で「危{あぶ}ない！」「止{と}めて！」と叫{さけ}んで周囲{しゅうい}に知{し}らせる',
          '1. Shout "Danger!" or "Stop!" loudly to alert everyone nearby',
          '১. জোরে চিৎকার করে "বিপদ!" বা "থামুন!" বলে আশেপাশের সবাইকে সতর্ক করা',
        ),
        t(
          '2. 機械{きかい}の非常{ひじょう}停止{ていし}ボタンを押{お}すか、合図{あいず}で動作{どうさ}を止{と}めさせる',
          '2. Hit emergency stop or use hand signals to halt the operation',
          '২. মেশিনের ইমার্জেন্সি স্টপ বোতাম চাপা বা ইশারা দিয়ে কাজ বন্ধ করানো',
        ),
        t(
          '3. 全員{ぜんいん}が安全{あんぜん}な退避{たいひ}場所{ばしょ}へ移動{いどう}したことを確認{かくにん}する',
          '3. Confirm that all workers have moved to a safe evacuation area',
          '৩. সবাই নিরাপদ স্থানে সরে গেছে কিনা তা নিশ্চিত করা',
        ),
        t(
          '4. 職長{しょくちょう}（リーダー）に状況{じょうきょう}を正{ただ}しく報告{ほうこく}する（報連相{ほうれんそう}）',
          '4. Report the situation accurately to the foreman (Horenso)',
          '৪. ফোরম্যান বা দলনেতার কাছে পরিস্থিতির সঠিক রিপোর্ট দেওয়া (হোরেনসো)',
        ),
      ],
    },
  ],
  inspect: [
    {
      id: 'ky-board',
      defective: false,
      name: t(
        'KY（危険予知{きけんよち}）活動{かつどう}黒板{こくばん}',
        'KY hazard prediction activity board',
        'কেওয়াই (ঝুঁকি অনুমান) কার্যক্রমের বোর্ড',
      ),
      spots: [
        {
          x: 32,
          y: 30,
          check: t(
            '作業{さぎょう}内容{ないよう}と危険{きけん}ポイント：具体的{ぐたいてき}な行動{こうどう}目標{もくひょう}が書{か}かれているか（良品{りょうひん}：明記{めいき} / 不良{ふりょう}：空欄{くうらん}・未記入{みきにゅう}）',
            'Work contents & hazard points: Concrete action target written (Good: Clearly written / Defect: Blank or unwritten)',
            'কাজের বিবরণ ও বিপদের বিষয়: সুনির্দিষ্ট পদক্ষেপের লক্ষ্য লেখা আছে কিনা (ভালো: স্পষ্ট লেখা / ত্রুটি: খালি বা লেখা নেই)',
          ),
        },
        {
          x: 68,
          y: 70,
          check: t(
            '指差呼称{しさこしょう}項目{こうもく}と参加者{さんかしゃ}サイン：全員{ぜんいん}が確認{かくにん}して名前{なまえ}を書{か}いたか（良品{りょうひん}：全員{ぜんいん}署名{しょめい} / 不良{ふりょう}：署名{しょめい}なし）',
            'Pointing & calling item & signatures: Everyone confirmed and signed (Good: All signed / Defect: Missing signatures)',
            'পয়েন্টিং ও কলিং বিষয় এবং স্বাক্ষর: সবাই নিশ্চিত হয়ে স্বাক্ষর করেছে কিনা (ভালো: সবার স্বাক্ষর আছে / ত্রুটি: স্বাক্ষর নেই)',
          ),
        },
      ],
    },
    {
      id: 'youjou-floor',
      defective: true,
      name: t(
        '通路{つうろ}と作業場{さぎょうば}の養生{ようじょう}シート',
        'Protective floor curing sheets in work area',
        'কাজের জায়গা ও পথের সুরক্ষা শিট (ইয়োজো)',
      ),
      spots: [
        {
          x: 28,
          y: 36,
          check: t(
            '養生{ようじょう}テープの固定{こてい}：シートの端{はし}がめくれず床{ゆか}に密着{みっちゃく}しているか（良品{りょうひん}：全面{ぜんめん}テープ固定{こてい} / 不良{ふりょう}：端{はし}がめくれて浮{う}いている）',
            'Curing tape fixing: Edges flat without peeling up (Good: Taped firmly / Defect: Edges curling up)',
            'সুরক্ষা টেপের বন্ধন: শিটের কিনারা উল্টে না গিয়ে মেঝের সাথে লেগে আছে কিনা (ভালো: শক্তভাবে টেপ লাগানো / ত্রুটি: কিনারা উল্টে থাকা)',
          ),
        },
        {
          x: 72,
          y: 64,
          check: t(
            '滑{すべ}り止{ど}めと破れ{やぶれ}：シートにつまずく穴{あな}や裂け目{さけめ}がないか（良品{りょうひん}：破れ{やぶれ}なし / 不良{ふりょう}：大{おお}きな破れ{やぶれ}と穴{あな}がある）',
            'Tear and anti-slip: No tears or trip holes in the sheet (Good: Intact / Defect: Torn with trip hazard)',
            'ছেঁড়া ও পিছলে পড়া রোধ: শিটে হোঁচট খাওয়ার মতো ছেঁড়া বা ফুটো নেই তো (ভালো: অক্ষত / ত্রুটি: ছেঁড়া ও গর্তযুক্ত)',
          ),
        },
      ],
    },
    {
      id: 'temoto-tool-caddy',
      defective: false,
      name: t(
        '手元{てもと}工具{こうぐ}箱{ばこ}と整理{せいり}トレー',
        'Assistant tool caddy and sorting tray',
        'সহকারীর হ্যান্ড টুল বক্স ও সুবিন্যস্ত ট্রে',
      ),
      spots: [
        {
          x: 36,
          y: 32,
          check: t(
            '刃物{はもの}・尖{とが}った工具{こうぐ}の保護{ほご}：サヤやキャップが被{かぶ}せてあるか（良品{りょうひん}：カバー装着{そうちゃく} / 不良{ふりょう}：刃先{はさき}が露出{ろしゅつ}）',
            'Sharp tool protection: Sheaths or caps fitted on blades (Good: Covered / Defect: Exposed sharp edge)',
            'ধারালো টুলের সুরক্ষা: ব্লেডে খাপ বা ক্যাপ লাগানো আছে কিনা (ভালো: কভার লাগানো / ত্রুটি: খোলা ধারালো অংশ)',
          ),
        },
        {
          x: 64,
          y: 68,
          check: t(
            '受渡{うけわた}し用{よう}グリップ：油{あぶら}汚れ{よごれ}がなく滑{すべ}らず持{も}てるか（良品{りょうひん}：清掃{せいそう}済{ず}み / 不良{ふりょう}：油{あぶら}まみれで滑{すべ}る）',
            'Handover grip: Clean and non-slippery for safe passing (Good: Cleaned / Defect: Covered in grease)',
            'হাতবদল করার গ্রিপ: তেল বা ময়লামুক্ত এবং পিছলে না গিয়ে ধরা যায় কিনা (ভালো: পরিষ্কার / ত্রুটি: তেলে পিচ্ছিল)',
          ),
        },
      ],
    },
  ],
  quiz: [
    {
      q: t(
        '作業{さぎょう}を始{はじ}める前{まえ}に職長{しょくちょう}から「しっかり段取{だんど}りをしておけ」と言{い}われました。どう行動{こうどう}すべきですか？',
        'Before starting work, the foreman told you to "do proper dandori". How should you act?',
        'কাজ শুরুর আগে ফোরম্যান বললেন "ভালো করে দানদোরি করে নাও"। তোমার কী করা উচিত?',
      ),
      options: [
        t(
          '作業{さぎょう}手順{てじゅん}を確認{かくにん}し、必要{ひつよう}な部材{ぶざい}・工具{こうぐ}・保護具{ほごぐ}を前{まえ}もって揃{そろ}える',
          'Review work procedures and gather needed materials, tools, and PPE in advance',
          'কাজের ধাপগুলো বুঝে নিয়ে প্রয়োজনীয় সরঞ্জাম, মালপত্র ও পিপিই আগে থেকেই গুছিয়ে রাখা',
        ),
        t(
          '始業{しぎょう}チャイムが鳴{な}り終{お}わるまで休憩所{きゅうけいじょ}で座{すわ}って待{ま}つ',
          'Sit and wait in the break area until the start chime finishes',
          'কাজের ঘণ্টা শেষ না হওয়া পর্যন্ত বিশ্রামাগারে বসে অপেক্ষা করা',
        ),
        t(
          '指示{しじ}を受{う}ける前{まえ}に自分{じぶん}の判断{はんだん}だけで勝手{かって}に機械{きかい}を動{うご}かす',
          'Start operating machines on your own judgment before receiving instructions',
          'কোনো নির্দেশ পাওয়ার আগেই নিজের আন্দাজে মেশিন চালানো শুরু করা',
        ),
        t(
          '今日{きょう}使{つか}う予定{よてい}のない壊{こわ}れた古い工具{こうぐ}を磨{みが}き始{はじ}める',
          'Start polishing old broken tools that are not scheduled for use today',
          'আজকে কাজে লাগবে না এমন পুরনো ভাঙা সরঞ্জাম পরিষ্কার করা শুরু করা',
        ),
      ],
      answer: 0,
      why: t(
        '「段取{だんど}り」とは、作業{さぎょう}を安全{あんぜん}かつスムーズに進{すす}めるために、事前{じぜん}に手順{てじゅん}や道具{どうぐ}を準備{じゅんび}することです。',
        'Dandori means preparing procedures, tools, and materials in advance to ensure smooth and safe work.',
        '"দানদোরি" হলো কাজ নিরাপদ ও নির্বিঘ্ন করতে কাজের আগেই ধাপ, যন্ত্রপাতি ও সামগ্রী প্রস্তুত রাখা।',
      ),
    },
    {
      q: t(
        '先輩{せんぱい}職人{しょくにん}の「手元{てもと}」につくことになりました。手元{てもと}の正{ただ}しい役目{やくめ}はどれですか？',
        'You are assigned as "temoto" (assistant) to a senior craftsman. What is your correct role?',
        'তুমি একজন সিনিয়র কারিগরের "তেমোতো" (সহকারী) হিসেবে কাজ করবে। তেমোতো-র সঠিক ভূমিকা কোনটি?',
      ),
      options: [
        t(
          '職人{しょくにん}から離{はな}れた場所{ばしょ}で座{すわ}ってスマートフォンを見{み}る',
          'Sit far away from the craftsman and look at your smartphone',
          'কারিগর থেকে দূরে বসে স্মার্টফোন চালানো',
        ),
        t(
          '作業{さぎょう}のそばで安全{あんぜん}を見守{みまも}り、頼{たの}まれた工具{こうぐ}や材料{ざいりょう}を声{こえ}をかけて手渡{てわた}す',
          'Watch safety nearby and hand over requested tools and materials after calling out',
          'কাছে থেকে নিরাপত্তা পর্যবেক্ষণ করা এবং ডাকার পর মুখে ডেকে সরঞ্জাম ও কাঁচামাল তুলে দেওয়া',
        ),
        t(
          '職人{しょくにん}に無断{むだん}で自分{じぶん}一人{ひとり}だけで別{べつ}の難{むずか}しい作業{さぎょう}を始{はじ}める',
          'Start another difficult task alone without informing the craftsman',
          'কারিগরকে না জানিয়ে একা একা অন্য একটি কঠিন কাজ শুরু করে দেওয়া',
        ),
        t(
          '職人{しょくにん}の作業{さぎょう}を見{み}ずに自分{じぶん}の荷物{にもつ}の整理{せいり}だけを行{おこな}う',
          'Ignore the craftsman\'s work and only organize your own personal belongings',
          'কারিগরের কাজের দিকে খেয়াল না রেখে শুধু নিজের জিনিসপত্র গোছানো',
        ),
      ],
      answer: 1,
      why: t(
        '「手元{てもと}」は職人{しょくにん}のそばで道具{どうぐ}の受渡{うけわた}しや安全{あんぜん}確認{かくにん}を助{たす}ける大切{たいせつ}な補助{ほじょ}役{やく}です。',
        'Temoto is an important assistant role helping with tool handovers and safety watching beside the craftsman.',
        '"তেমোতো" হলো কারিগরের পাশে থেকে সরঞ্জাম আদান-প্রদান ও নিরাপত্তা নজরদারিতে সাহায্যকারী গুরুত্বপূর্ণ ভূমিকা।',
      ),
    },
    {
      q: t(
        '溶接{ようせつ}作業{さぎょう}の周辺{しゅうへん}で「養生{ようじょう}をしてくれ」と頼{たの}まれました。何{なに}をすべきですか？',
        'You were asked to "do youjou" around a welding area. What should you do?',
        'ওয়েল্ডিং কাজের আশেপাশে তোমাকে "ইয়োজো" করতে বলা হলো। তোমার কী করা উচিত?',
      ),
      options: [
        t(
          '体調{たいちょう}が悪{わる}いので医務室{いむしつ}へ休{やす}みに行{い}く',
          'Go to the clinic to rest because you feel unwell',
          'শরীর খারাপ লাগছে ভেবে বিশ্রাম নিতে চিকিৎসাকেন্দ্রে যাওয়া',
        ),
        t(
          '火花{ひばな}が見{み}えやすいよう周囲{しゅうい}の照明{しょうめい}を全{すべ}て消{け}す',
          'Turn off all surrounding lights to make sparks easier to see',
          'আগুনের ফুলকি সহজে দেখার জন্য আশেপাশের সমস্ত আলো নিভিয়ে দেওয়া',
        ),
        t(
          '防炎{ぼうえん}シートなどで周辺{しゅうへん}の床{ゆか}や機械{きかい}を覆{おお}い、火花{ひばな}の飛散{ひさん}や傷{きず}を防{ふせ}ぐ',
          'Cover surrounding floors and machinery with fire-resistant sheets to block sparks and damage',
          'অগ্নি-প্রতিরোধক শিট দিয়ে আশেপাশের মেঝে ও মেশিন ঢেকে দেওয়া যাতে আগুনের ফুলকি ও ক্ষতি রোধ হয়',
        ),
        t(
          '床{ゆか}に水{みず}を大量{たいりょう}に撒{ま}いて作業場{さぎょうば}を水浸{みずびた}しにする',
          'Splash large amounts of water on the floor, flooding the entire workspace',
          'মেঝেতে প্রচুর পানি ঢেলে পুরো কাজের জায়গা জলমগ্ন করে ফেলা',
        ),
      ],
      answer: 2,
      why: t(
        '現場{げんば}の「養生{ようじょう}」は、シートやテープで周囲{しゅうい}を保護{ほご}し、火災{かさい}や製品{せいひん}の汚損{おそん}を防{ふせ}ぐ作業{さぎょう}です。',
        'On site, "youjou" means protecting surroundings with sheets and tape to prevent fire, damage, and stains.',
        'সাইটে "ইয়োজো" মানে শিট ও টেপ দিয়ে চারপাশ ঢেকে আগুন, ক্ষতি বা দাগ লাগা থেকে সুরক্ষা দেওয়া।',
      ),
    },
    {
      q: t(
        '吊{つ}り荷{に}のワイヤーが外{はず}れそうになり、仲間{なかま}の頭上{ずじょう}へ傾{かたむ}いてきました。どう叫{さけ}びますか？',
        'A suspended crane load wire is slipping and tilting toward a coworker\'s head. What do you shout?',
        'ঝুলন্ত মালের তার ফসকে গিয়ে সহকর্মীর মাথার ওপর হেলে পড়ছে। তুমি কী বলে চিৎকার করবে?',
      ),
      options: [
        t(
          '「お疲{つか}れさまでした」と丁寧{ていねい}に挨拶{あいさつ}する',
          'Politely greet with "Thank you for your hard work"',
          'ভদ্রভাবে "কাজের জন্য ধন্যবাদ" বলে শুভেচ্ছা জানানো',
        ),
        t(
          '「次{つぎ}の休憩{きゅうけい}は何時{なんじ}ですか」と質問{しつもん}する',
          'Ask "What time is our next break?"',
          '"পরবর্তী বিরতি কয়টায়?" বলে প্রশ্ন করা',
        ),
        t(
          '声{こえ}を出{だ}すと驚{おどろ}かせてしまうので何{なに}も言{い}わず走{はし}って逃{に}げる',
          'Say nothing and run away to avoid startling anyone',
          'কথা বললে ভয় পেয়ে যাবে ভেবে কিছু না বলে একা দৌড়ে পালিয়ে যাওয়া',
        ),
        t(
          '大声{おおごえ}で「危{あぶ}ない！」「止{と}めて！」と叫{さけ}び、作業{さぎょう}を緊急{きんきゅう}停止{ていし}させる',
          'Shout "Danger!" and "Stop!" at the top of your voice to halt the operation immediately',
          'উচ্চস্বরে "বিপদ!" ও "থামুন!" বলে চিৎকার করে কাজ তাৎক্ষণিকভাবে বন্ধ করানো',
        ),
      ],
      answer: 3,
      why: t(
        '命{いのち}に関{かか}わる危険{きけん}を見{み}つけたときは、誰{だれ}でも即座{そくざ}に「危{あぶ}ない！」「止{と}めて！」と大声{おおごえ}で危険{きけん}を知{し}らせます。',
        'When spotting life-threatening danger, anyone must immediately shout "Danger!" and "Stop!" to alert everyone.',
        'জীবনঘাতী বিপদ দেখলে যে কেউই সঙ্গে সঙ্গে জোরে "বিপদ!" ও "থামুন!" বলে সবাইকে সতর্ক করতে বাধ্য।',
      ),
    },
    {
      q: t(
        '夕方{ゆうがた}の「終礼{しゅうれい}」の前{まえ}に職長{しょくちょう}から「片付{かたづ}け！」の合図{あいず}がありました。正{ただ}しい対応{たいおう}はどれですか？',
        'Before evening shuurei (closing debrief), the foreman signals "Kataduke!". What is the correct response?',
        'বিকালে সমাপনী সভার আগে ফোরম্যান বললেন "কাতাজুকে (গোছাও)!। সঠিক পদক্ষেপ কোনটি?',
      ),
      options: [
        t(
          '使{つか}った工具{こうぐ}を拭{ふ}いて定位置{ていいち}へ戻{もど}し、ゴミを分別{ぶんべつ}して片付{かたづ}ける',
          'Wipe used tools clean, return them to designated spots, sort trash, and tidy up',
          'ব্যবহৃত সরঞ্জাম মুছে নির্দিষ্ট স্থানে রাখা, আবর্জনা আলাদা করা এবং জায়গা গোছানো',
        ),
        t(
          '工具{こうぐ}を通路{つうろ}に置{お}いたままタイムカードを押{お}しに走{はし}る',
          'Leave tools in the walkway and run to punch the time clock',
          'টুলসগুলো চলাচলের পথে ফেলে রেখে হাজিরা কার্ড পাঞ্চ করতে দৌড় দেওয়া',
        ),
        t(
          '機械{きかい}のスイッチを入{い}れたまま翌朝{よくあさ}まで放置{ほうち}する',
          'Leave machine switches ON unattended until the next morning',
          'মেশিনের সুইচ চালু রেখেই পরদিন সকাল পর্যন্ত ফেলে রাখা',
        ),
        t(
          '自分{じぶん}のゴミを他{ほか}の作業員{さぎょういん}の足元{あしもと}に押{お}し込{こ}んで隠{かく}す',
          'Shove your trash under other workers\' feet to hide it',
          'নিজের বর্জ্য অন্য শ্রমিকের পায়ের কাছে ঠেলে দিয়ে লুকিয়ে রাখা',
        ),
      ],
      answer: 0,
      why: t(
        '「片付{かたづ}け」は道具{どうぐ}を元{もと}の場所{ばしょ}へ戻{もど}し、ゴミを処理{しょり}して現場{げんば}の安全{あんぜん}を確保{かくほ}する基本{きほん}です。',
        'Kataduke means returning tools to their designated spots and cleaning scrap to secure site safety.',
        '"কাতাজুকে" হলো সরঞ্জাম নির্দিষ্ট জায়গায় ফিরিয়ে রাখা এবং আবর্জনা পরিষ্কার করে সাইট সুরক্ষিত রাখার ভিত্তি।',
      ),
    },
    {
      q: t(
        'クレーンで荷物{にもつ}を移動{いどう}中{ちゅう}、合図者{あいずしゃ}から「もう少{すこ}し上{あ}げて！」と声{こえ}がかかりました。どういう意味{いみ}ですか？',
        'While moving a crane load, the signaler calls out "Mou sukoshi agete!". What does this mean?',
        'ক্রেনে মাল সরানোর সময় সিগন্যালকারী বললেন "মো সুকোশি আগেতে!"। এর অর্থ কী?',
      ),
      options: [
        t(
          '荷物{にもつ}を急{きゅう}に一番{いちばん}下{した}まで落{お}とすこと',
          'Suddenly drop the load all the way to the bottom',
          'মালপত্র হঠাৎ একদম নিচে ফেলে দেওয়া',
        ),
        t(
          '荷物{にもつ}をあとわずかだけ上{うえ}に巻{ま}き上{あ}げること',
          'Hoist the load slightly higher by a small amount',
          'মালপত্র সামান্য একটু উপরের দিকে তোলা',
        ),
        t(
          '荷物{にもつ}を右{みぎ}へ大{おお}きく振{ふ}り回{まわ}すこと',
          'Swing the load widely to the right',
          'মালপত্র ডান দিকে জোরে দুলিয়ে দেওয়া',
        ),
        t(
          'クレーンの電源{でんげん}を切{き}ってそのまま離{はな}れること',
          'Turn off crane power and walk away immediately',
          'ক্রেনের পাওয়ার বন্ধ করে দিয়ে সেখান থেকে চলে যাওয়া',
        ),
      ],
      answer: 1,
      why: t(
        '「もう少{すこ}し上{あ}げて」は「わずかに上昇{じょうしょう}させる」合図{あいず}です。微調整{びちょうせい}のときは慎重{しんちょう}に動{うご}かします。',
        '"Mou sukoshi agete" signals hoisting up just a little bit. Make fine adjustments very carefully.',
        '"মো সুকোশি আগেতে" মানে সামান্য একটু উপরে তোলা। সূক্ষ্ম পরিবর্তনের সময় সতর্কতার সাথে পরিচালনা করতে হয়।',
      ),
    },
    {
      q: t(
        '毎朝{まいあさ}行{おこな}われる「朝礼{ちょうれい}」の大切{たいせつ}な目的{もくてき}は何{なに}ですか？',
        'What is the vital purpose of the "chourei" (morning assembly) held every morning?',
        'প্রতিদিন সকালে অনুষ্ঠিত "চৌরেই" (সকালের সমাবেশ)-এর মূল উদ্দেশ্য কী?',
      ),
      options: [
        t(
          '朝{あさ}ご飯{はん}のメニューを全員{ぜんいん}で決{き}めること',
          'Decide the breakfast menu together as a team',
          'দলের সবাই মিলে সকালের নাস্তার মেনু ঠিক করা',
        ),
        t(
          '歌{うた}やゲームをして楽{たの}しく遊{あそ}ぶこと',
          'Sing songs and play games for casual entertainment',
          'গান গেয়ে বা খেলাধুলা করে আনন্দ করা',
        ),
        t(
          '健康{けんこう}状態{じょうたい}の確認{かくにん}、作業{さぎょう}予定{よてい}と安全{あんぜん}指示{しじ}を全員{ぜんいん}で共有{きょうゆう}すること',
          'Confirm health condition, and share work schedules and safety instructions with all workers',
          'শারীরিক সুস্থতা যাচাই এবং কাজের পরিকল্পনা ও সুরক্ষার নির্দেশাবলী সবার সাথে ভাগ করে নেওয়া',
        ),
        t(
          '昨日{きのう}ミスをした人{ひと}を見{み}つけて全員{ぜんいん}の前{まえ}で叱{しか}ること',
          'Find someone who made a mistake yesterday and scold them publicly',
          'গতকাল যে ভুল করেছে তাকে সবার সামনে দাঁড় করিয়ে তিরস্কার করা',
        ),
      ],
      answer: 2,
      why: t(
        '「朝礼{ちょうれい}」は体調{たいちょう}の確認{かくにん}と、その日{ひ}の危険{きけん}や作業{さぎょう}指示{しじ}を全員{ぜんいん}で統一{とういつ}するための重要{じゅうよう}な場{ば}です。',
        'Chourei is crucial for checking worker health and aligning daily work instructions and safety targets.',
        '"চৌরেই" হলো কর্মীদের স্বাস্থ্য নিশ্চিত করা এবং সেদিনের কাজের নির্দেশ ও নিরাপত্তা লক্ষ্য এক করার মূল স্থান।',
      ),
    },
    {
      q: t(
        '午前{ごぜん}10時{じ}に職長{しょくちょう}が「一服{いっぷく}にしよう」と言{い}いました。どう行動{こうどう}するのが正{ただ}しいですか？',
        'At 10:00 AM, the foreman says "Let\'s have an ippuku". What is the correct action?',
        'সকাল ১০টায় ফোরম্যান বললেন "চলো একটু ইপ্পুকু করি"। সঠিক আচরণ কোনটি?',
      ),
      options: [
        t(
          '今日{きょう}の作業{さぎょう}が終{お}わったと勘違{かんちが}いして勝手{かって}に帰宅{きたく}する',
          'Mistake it for the end of the day and head home on your own',
          'আজকের কাজ শেষ ভেবে নিজের ইচ্ছায় বাসায় চলে যাওয়া',
        ),
        t(
          '機械{きかい}の主電源{しゅでんげん}を入{い}れたまま休憩所{きゅうけいじょ}へ走{はし}っていく',
          'Run to the break area leaving the machine main power switched ON',
          'মেশিনের মেইন পাওয়ার অন রেখেই বিশ্রামাগারে দৌড়ে যাওয়া',
        ),
        t(
          '無断{むだん}で工場{こうじょう}の敷地{しきち}外{がい}へ出{で}て長{なが}い買{か}い物{もの}をする',
          'Leave the factory grounds without permission for a long shopping trip',
          'অনুমতি ছাড়া কারখানার বাইরে চলে গিয়ে দীর্ঘক্ষণ কেনাকাটা করা',
        ),
        t(
          '作業{さぎょう}を一旦{いったん}安全{あんぜん}に止{と}め、指定{してい}場所{ばしょ}で水分{すいぶん}補給{ほきゅう}や小休止{しょうきゅうし}を取{と}る',
          'Safely pause work, and take a short rest and hydrate at the designated break spot',
          'কাজ সাময়িকভাবে নিরাপদে থামিয়ে নির্ধারিত স্থানে পানি পান ও অল্প বিশ্রাম নেওয়া',
        ),
      ],
      answer: 3,
      why: t(
        '「一服{いっぷく}」は集中力{しゅうちゅうりょく}を保{たも}つための短{みじか}い休憩{きゅうけい}です。機械{きかい}の安全{あんぜん}を確認{かくにん}してから休{やす}みます。',
        'Ippuku is a short break to maintain focus and prevent fatigue. Pause machines safely before resting.',
        '"ইপ্পুকু" হলো কাজের ক্লান্তি দূর করে মনোযোগ ধরে রাখার সংক্ষিপ্ত বিরতি। মেশিন নিরাপদ করে তবেই বিশ্রাম নাও।',
      ),
    },
    {
      q: t(
        '現場{げんば}で「職長{しょくちょう}」や「親方{おやかた}」呼{よ}ばれる人{ひと}は、どのような役割{やくわり}の人{ひと}ですか？',
        'What role do the people called "shokuchou" or "oyakata" play on the job site?',
        'কাজের জায়গায় "শোকুচো" বা "ওয়ায়াকাতা" বলতে কাদের বোঝায় এবং তাদের ভূমিকা কী?',
      ),
      options: [
        t(
          '現場{げんば}で作業員{さぎょういん}を直接{ちょくせつ}指導{しどう}し、安全{あんぜん}と作業{さぎょう}を指揮{しき}するリーダー',
          'Leaders who directly guide workers on site and direct safety and operations',
          'সাইটে সরাসরি কর্মীদের দিকনির্দেশনা দেন এবং নিরাপত্তা ও কাজ পরিচালনা করেন এমন দলনেতা বা ওস্তাদ',
        ),
        t(
          '現場{げんば}へ一度{いちど}も来{こ}ない遠{とお}くの取引先{とりひきさき}の担当者{たんとうしゃ}',
          'A contact person from a distant client company who never visits the site',
          'দূরবর্তী কোনো ক্লায়েন্ট কোম্পানির ব্যক্তি যিনি কখনোই সাইটে আসেন না',
        ),
        t(
          'その日{ひ}初{はじ}めてアルバイトに来{き}た見習{みなら}いの作業員{さぎょういん}',
          'A novice part-time trainee who came to the site for the very first time today',
          'আজকে প্রথম দিন কাজে আসা একদম নতুন শিক্ষানবিস কর্মী',
        ),
        t(
          '現場{げんば}に清涼{せいりょう}飲料{いんりょう}の自動{じどう}販売機{はんばいき}を補充{ほじゅう}に来{く}る業者{ぎょうしゃ}',
          'A vendor who comes to restock soft drink vending machines on site',
          'সাইটে কোমল পানীয়র ভেন্ডিং মেশিনে মালপত্র ভরতে আসা সরবরাহকারী',
        ),
      ],
      answer: 0,
      why: t(
        '「職長{しょくちょう}」や「親方{おやかた}」は作業{さぎょう}の指示{しじ}と現場{げんば}の安全{あんぜん}を守{まも}る責任者{せきにんしゃ}です。困{こま}ったときは相談{そうだん}します。',
        'Shokuchou and oyakata are leaders responsible for directing operations and maintaining safety on site.',
        '"শোকুচো" ও "ওয়ায়াকাতা" হলেন কাজের নির্দেশনা ও সাইটের নিরাপত্তা রক্ষার দায়িত্বপ্রাপ্ত প্রধান ব্যক্তি।',
      ),
    },
    {
      q: t(
        '狭{せま}い通路{つうろ}で長{なが}い鉄パイプを運{はこ}んでいます。角{かど}を曲{ま}がるときはどう声{こえ}をかけますか？',
        'You are carrying a long steel pipe in a narrow walkway. How should you call out when turning a corner?',
        'সংকীর্ণ পথে একটি লম্বা লোহার পাইপ বহন করছ। কোণা ঘোরার সময় কীভাবে ডেকে সতর্ক করবে?',
      ),
      options: [
        t(
          '誰{だれ}にも気づ{きづ}かれないよう足音{あしおと}を忍{しの}ばせて通{とお}る',
          'Walk silently on tiptoe so no one notices you',
          'কেউ যেন টের না পায় সেজন্য নিঃশব্দে পা টিপে হেঁটে যাওয়া',
        ),
        t(
          '「長物{ながもの}通{とお}ります！」「行{い}きます！」と前{まえ}もって大声{おおごえ}で呼{よ}びかける',
          'Call out loudly in advance: "Long object passing!" or "Coming through!"',
          'আগে থেকেই জোরে ডেকে বলবে: "লম্বা জিনিস নিয়ে যাচ্ছি!" বা "যাচ্ছি!"',
        ),
        t(
          '角{かど}の向{む}こうを見{み}ずにパイプを前{まえ}に突{つ}き出{だ}して走{はし}る',
          'Thrust the pipe forward and run without checking around the corner',
          'কোণার ওপাশে না দেখেই পাইপটা সামনের দিকে বাড়িয়ে ধরে দৌড়ে যাওয়া',
        ),
        t(
          'スマートフォンで音楽{おんがく}を大音量{だいおんりょう}で鳴{な}らしながら歩{ある}く',
          'Walk while playing loud music on your smartphone',
          'স্মার্টফোনে জোরে গান বাজিয়ে হাঁটতে থাকা',
        ),
      ],
      answer: 1,
      why: t(
        '見通{みとお}しの悪{わる}い角{かど}や長物{ながもの}の運搬{うんぱん}では、「行{い}きます！」と声{こえ}をかけて衝突{しょうとつ}事故{じこ}を防{ふせ}ぎます。',
        'At blind corners or when carrying long items, calling out "Coming through!" prevents collisions.',
        'দৃষ্টিসীমার বাইরের কোণায় বা লম্বা জিনিস বহনের সময় "যাচ্ছি!" বলে ডেকে সংঘর্ষের দুর্ঘটনা রোধ করতে হয়।',
      ),
    },
    {
      q: t(
        '作業{さぎょう}直前{ちょくぜん}に行{おこな}う「TBM（ツールボックスミーティング）」の正{ただ}しい説明{せつめい}はどれですか？',
        'Which of the following is the correct explanation of a pre-work "TBM" (Tool Box Meeting)?',
        'কাজ শুরুর ঠিক আগে অনুষ্ঠিত "টিবিএম (টুল বক্স মিটিং)"-এর সঠিক ব্যাখ্যা কোনটি?',
      ),
      options: [
        t(
          '新{あたら}しい道具{どうぐ}箱{ばこ}を買{か}うためにお金{かね}を集{あつ}める会合{かいごう}',
          'A meeting to collect money to purchase new toolboxes',
          'নতুন টুল বক্স কেনার জন্য চাঁদা তোলার সভা',
        ),
        t(
          '作業{さぎょう}中{ちゅう}に道具{どうぐ}箱{ばこ}を椅子{いす}代{が}わりにして座{すわ}る練習{れんしゅう}',
          'Practice using a toolbox as a stool to sit on during work',
          'কাজের সময় টুল বক্সকে টুলের মতো বানিয়ে বসে থাকার অনুশীলন',
        ),
        t(
          '作業{さぎょう}現場{げんば}で道具{どうぐ}箱{ばこ}を囲{かこ}み、短時間{たんじかん}で作業{さぎょう}内容{ないよう}と危険{きけん}を確認{かくにん}する打合{うちあわ}せ',
          'A short meeting gathered around the toolbox on site to confirm work details and hazards',
          'কাজের জায়গায় টুল বক্সের চারপাশে দাঁড়িয়ে অল্প সময়ে কাজের বিবরণ ও বিপদের বিষয় নিশ্চিত করার সংক্ষিপ্ত সভা',
        ),
        t(
          '壊{こわ}れた機械{きかい}を秘密{ひみつ}で隠{かく}すための相談{そうだん}',
          'A secret consultation to hide broken machinery from management',
          'ভাঙা যন্ত্রপাতি কর্তৃপক্ষের চোখ থেকে লুকিয়ে ফেলার গোপন পরামর্শ',
        ),
      ],
      answer: 2,
      why: t(
        '「TBM」は作業{さぎょう}直前{ちょくぜん}に現場{げんば}で短時間{たんじかん}で行{おこな}う打合せ{うちあわせ}で、作業{さぎょう}の役割{やくわり}と危険{きけん}を再確認{さいかくにん}します。',
        'TBM is a brief on-site meeting right before work to reaffirm task assignments and hazards.',
        '"টিবিএম" হলো কাজের ঠিক আগে সাইটে অল্প সময়ের সভা, যেখানে দায়িত্ব বণ্টন ও সম্ভাব্য বিপদ পুনর্বার নিশ্চিত করা হয়।',
      ),
    },
    {
      q: t(
        '一日{いちにち}の作業{さぎょう}が終了{しゅうりょう}したとき、現場{げんば}の仲間{なかま}や職長{しょくちょう}に対{たい}して交{か}わす正{ただ}しい挨拶{あいさつ}はどれですか？',
        'When the day\'s work is finished, what is the correct greeting to exchange with coworkers and foreman?',
        'দিনের কাজ শেষ হওয়ার পর সহকর্মী এবং ফোরম্যানের সাথে কোন অভিবাদনটি বিনিময় করা সঠিক?',
      ),
      options: [
        t(
          '「おはようございます」',
          '"Good morning"',
          '"শুভ সকাল (ওহাইও গোজাইমাস)"',
        ),
        t(
          '「ごめんなさい」',
          '"I am sorry"',
          '"আমি দুঃখিত (গোমেন নাসাই)"',
        ),
        t(
          '「いただきます」',
          '"Thank you for the meal"',
          '"খাবার জন্য ধন্যবাদ (ইতাদাকিমাস)"',
        ),
        t(
          '「お疲{つか}れさまでした」（または「ご安全{あんぜん}に」）',
          '"Otsukaresama deshita" (or "Go-anzen ni")',
          '"কাজের জন্য ধন্যবাদ / শুভ বিশ্রাম (ওতসুকারে সামা দেশিতা / গো-আনজেন নি)"',
        ),
      ],
      answer: 3,
      why: t(
        '作業{さぎょう}終了時{しゅうりょうじ}には「お疲{つか}れさまでした」や「ご安全{あんぜん}に」と挨拶{あいさつ}し、互{たが}いの無事故{むじこ}と労苦{ろうく}をねぎらいます。',
        'At the end of work, exchange "Otsukaresama deshita" or "Go-anzen ni" to appreciate each other\'s safe work.',
        'কাজ শেষে "ওতসুকারে সামা দেশিতা" বা "গো-আনজেন নি" বলে অভিবাদন জানিয়ে পরস্পরের নিরাপদ কাজ ও পরিশ্রমের স্বীকৃতি দেওয়া হয়।',
      ),
    },
  ],
  glossary: [
    {
      ja: 'ヨシ',
      reading: 'よし',
      en: 'Yoshi / All clear / Confirmed',
      bn: 'ঠিক আছে (ইয়োশি / নিশ্চিত)',
      note: t(
        '指差呼称などで対象の安全を確認したときに発声する決め言葉。',
        'Standard call-out spoken when confirming safety through pointing and calling.',
        'পয়েন্টিং ও কলিংয়ের মাধ্যমে কোনো বিষয় নিরাপদ নিশ্চিত করার পর মুখে বলার প্রমাণ বাক্য।',
      ),
    },
    {
      ja: '段取り',
      reading: 'だんどり',
      en: 'Dandori / Setup, preparation, workflow planning',
      bn: 'দানদোরি (কাজের পূর্ব প্রস্তুতি ও পরিকল্পনা)',
      note: t(
        '作業を安全かつ手際よく進めるため、前もって手順や道具を整えること。',
        'Arranging procedures, tools, and materials in advance for safe and efficient work.',
        'কাজ নিরাপদ ও দক্ষতার সাথে সম্পন্ন করতে আগে থেকেই ধাপ ও সরঞ্জাম প্রস্তুত রাখা।',
      ),
    },
    {
      ja: '片付け',
      reading: 'かたづけ',
      en: 'Kataduke / Tidying up, sorting and packing tools',
      bn: 'কাতাজুকে (টুলস ও মালামাল নির্দিষ্ট স্থানে গুছিয়ে রাখা)',
      note: t(
        '使った工具を元の場所に戻し、ゴミを片付けて作業場を整えること。',
        'Returning used tools to proper storage and clearing waste to tidy the workspace.',
        'ব্যবহৃত সরঞ্জাম নির্দিষ্ট স্থানে ফেরত রাখা এবং বর্জ্য পরিষ্কার করে কাজের জায়গা পরিপাটি করা।',
      ),
    },
    {
      ja: '養生',
      reading: 'ようじょう',
      en: 'Youjou / Protective covering, masking, curing',
      bn: 'ইয়োজো (সুরক্ষা শিট দিয়ে মেঝে, দেয়াল বা যন্ত্রপাতি ঢাকা)',
      note: t(
        '作業中の傷・汚れ・火花などから製品や床・設備をシート等で保護すること。',
        'Protecting products, floors, and machinery from damage, stains, or sparks using sheets.',
        'কাজের সময় পণ্য, মেঝে বা যন্ত্রপাতিতে আঁচড়, দাগ বা আগুনের ফুলকি লাগা আটকাতে শিট দিয়ে ঢেকে রাখা।',
      ),
    },
    {
      ja: '手元',
      reading: 'てもと',
      en: 'Temoto / Helper, assistant, handing tools',
      bn: 'তেমোতো (কারিগরের সহকারী / সরঞ্জাম এগিয়ে দেওয়া)',
      note: t(
        '熟練職人のすぐそばで道具を手渡したり周囲の安全を確認したりする補助役。',
        'Assistant who passes tools and monitors surroundings beside a skilled craftsman.',
        'দক্ষ কারিগরের পাশে থেকে সরঞ্জাম এগিয়ে দেওয়া এবং চারপাশের নিরাপত্তা পর্যবেক্ষণকারী সহকারী।',
      ),
    },
    {
      ja: '職長',
      reading: 'しょくちょう',
      en: 'Shokuchou / On-site foreman, safety supervisor',
      bn: 'শোকুচো (সাইট ফোরম্যান / কাজের ও সুরক্ষার সুপারভাইজার)',
      note: t(
        '現場で作業員を直接指導し、作業指示と安全管理を行うリーダー。',
        'On-site leader who directly instructs workers and manages safety and operations.',
        'সাইটে সরাসরি শ্রমিকদের পরিচালনা করেন এবং কাজের নির্দেশ ও নিরাপত্তা তদারকি করেন এমন দলনেতা।',
      ),
    },
    {
      ja: '親方',
      reading: 'おやかた',
      en: 'Oyakata / Master craftsperson, crew boss',
      bn: 'ওয়ায়াকাতা (প্রধান ওস্তাদ কারিগর / দলপতি)',
      note: t(
        '長年の経験を持ち、作業班や下請け職人を束ねる熟練のリーダー。',
        'Experienced master craftsman who leads a work crew or trade subcontractors.',
        'দীর্ঘদিনের অভিজ্ঞতাসম্পন্ন দক্ষ কারিগর যিনি কাজের দল বা উপ-ঠিকাদার কারিগরদের নেতৃত্ব দেন।',
      ),
    },
    {
      ja: '元請・下請',
      reading: 'もとうけ・したうけ',
      en: 'Motouke / Shitauke (Prime contractor / Subcontractor)',
      bn: 'মোতৌকে ও শিতাউকে (মূল ঠিকাদার ও উপ-ঠিকাদার)',
      note: t(
        '発注者から直接工事を引き受ける元請と、その一部を請け負う下請企業。',
        'Prime contractor taking direct orders from clients, and subcontractors handling parts of the job.',
        'গ্রাহকের কাছ থেকে সরাসরি কাজ নেওয়া মূল ঠিকাদার এবং তাদের অধীনে অংশবিশেষ সম্পন্নকারী উপ-ঠিকাদার।',
      ),
    },
    {
      ja: '朝礼・終礼',
      reading: 'ちょうれい・しゅうれい',
      en: 'Chourei / Shuurei (Morning assembly / Evening debrief)',
      bn: 'চৌরেই ও শ্যূরেই (সকালের সমাবেশ ও বিকালের সমাপনী মিটিং)',
      note: t(
        '朝の安全指示や連絡を行う朝礼と、一日の作業結果を確認する終礼。',
        'Morning gathering for safety notices and evening debrief to review shift results.',
        'সকালের নিরাপত্তা নির্দেশনার জন্য প্রাতঃসমাবেশ এবং দিনশেষে কাজের ফলাফল মূল্যায়নের সমাপনী সভা।',
      ),
    },
    {
      ja: '一服',
      reading: 'いっぷく',
      en: 'Ippuku / Short rest or tea break',
      bn: 'ইপ্পুকু (কাজের ফাঁকে ১০-১৫ মিনিটের সংক্ষিপ্ত চা-পানের বিরতি)',
      note: t(
        '午前や午後に取る10〜15分程度の短い小休止。疲労を防ぎ集中力を回復する。',
        'A short 10-15 minute rest during morning or afternoon to prevent fatigue and restore focus.',
        'ক্লান্তি দূর করতে ও মনোযোগ ফিরিয়ে আনতে সকালে বা বিকালে নেওয়া ১০-১৫ মিনিটের ছোট বিরতি।',
      ),
    },
    {
      ja: '危険予知（KY）',
      reading: 'きけんよち',
      en: 'KY (Kiken Yochi) / Hazard Prediction Activity',
      bn: 'কেওয়াই (বিপদ পূর্বানুমান কার্যক্রম)',
      note: t(
        '作業に潜む危険を事前に話し合い、対策を決めて事故を防ぐ活動。',
        'Activity where team discusses hidden hazards in advance and sets countermeasures.',
        'কাজের সম্ভাব্য গোপন বিপদগুলো নিয়ে আগে থেকেই আলোচনা করে দুর্ঘটনা ঠেকানোর দলগত কার্যক্রম।',
      ),
    },
    {
      ja: 'TBM',
      reading: 'てぃーびーえむ',
      en: 'TBM (Tool Box Meeting) / Pre-work safety huddle',
      bn: 'টিবিএম (কাজের ঠিক আগের সংক্ষিপ্ত টুল বক্স মিটিং)',
      note: t(
        '作業開始直前に現場で工具箱を囲んで行う、5分程度の安全打ち合わせ。',
        'Brief 5-minute safety huddle around the toolbox on site right before starting work.',
        'কাজ শুরুর ঠিক আগে সাইটে টুল বক্স ঘিরে ৫ মিনিটের মতো সংক্ষিপ্ত নিরাপত্তা বৈঠক।',
      ),
    },
    {
      ja: '安全第一',
      reading: 'あんぜんだいいち',
      en: 'Anzen Daiichi / Safety First',
      bn: 'আনজেন দাইইচি (নিরাপত্তা সর্বাগ্রে)',
      note: t(
        '生産や納期よりも、働く人の命と安全を最優先にする基本理念。',
        'Core philosophy putting workers\' lives and safety above production and deadlines.',
        'উৎপাদন বা কাজের সময়সীমার চেয়েও শ্রমিকের জীবন ও নিরাপত্তাকে সর্বোচ্চ অগ্রাধিকার দেওয়ার মূলনীতি।',
      ),
    },
  ],
}
