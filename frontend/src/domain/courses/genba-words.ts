import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string): Tri => ({ ja, en })

export const genbaWords: CourseData = {
  id: 'genba-words',
  category: 'language',
  level: 1,
  minutes: 15,
  title: t('現場用語{げんばようご}', 'Workplace Japanese'),
  sub: t(
    '現場{げんば}で飛{と}び交{か}う言葉{ことば}と合図{あいず}を身{み}につけ、安全{あんぜん}に作業{さぎょう}する。',
    'Master workplace terms and signals heard on site every day to work safely.',
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
      ),
    },
    {
      who: 'worker',
      text: t(
        '先輩{せんぱい}たちが「手元{てもと}」や「養生{ようじょう}」と言{い}っていましたが、どんな意味{いみ}ですか？',
        'I heard senior workers say "temoto" and "youjou". What do they mean?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '「手元{てもと}」は職人{しょくにん}の作業{さぎょう}を補助{ほじょ}すること、「養生{ようじょう}」は床{ゆか}や製品{せいひん}をシートで保護{ほご}することです。',
        '"Temoto" means assisting a craftsman, and "youjou" means protecting floors or products with sheets.',
      ),
    },
    {
      who: 'worker',
      text: t(
        'わかりました！さっき職人{しょくにん}から工具{こうぐ}を頼{たの}まれたので、無言{むごん}で背後{はいご}からスパナを手渡{てわた}しました。',
        'Understood! Earlier a craftsman asked for a tool, so I handed him a spanner silently from behind.',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'それは危{あぶ}ないです。急{きゅう}に手{て}を出{だ}さず、「スパナ行{い}きます」と必{かなら}ず声{こえ}をかけて渡{わた}します。',
        'That is dangerous. Never reach out suddenly; always call out "Spanner coming!" before handing it over.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '声{こえ}を出{だ}さないと、相手{あいて}が振{ふ}り返{かえ}ったときにぶつかって怪我{けが}をするのですね。',
        'If I do not call out, the other person might turn around and bump into it, causing injury.',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'その通{とお}りです。危険{きけん}なときは遠慮{えんりょ}せず、大声{おおごえ}で「危{あぶ}ない！」「止{と}めて！」と叫{さけ}んでください。',
        'Exactly. In dangerous moments, do not hesitate; shout "Danger!" or "Stop!" at the top of your voice.',
      ),
    },
    {
      who: 'worker',
      text: t(
        'クレーンの合図{あいず}でも、「上{あ}げて」「下{さ}げて」「右{みぎ}」「左{ひだり}」と短{みじか}くハッキリ伝{つた}えます！',
        'Even for crane signals, I will say "Hoist", "Lower", "Right", and "Left" clearly and briefly!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '素晴{すば}らしいです。「安全第一{あんぜんだいいち}」で作業{さぎょう}し、終{お}わったら「お疲{つか}れさまでした」と終礼{しゅうれい}をします。',
        'Wonderful. Work with "Safety First", and when finished, close the shift with "Thank you for your hard work".',
      ),
    },
    {
      who: 'worker',
      text: t(
        'はい！現場{げんば}の言葉{ことば}と正{ただ}しい合図{あいず}を身{み}につけて、今日{きょう}もゼロ災{さい}で頑張{がんば}ります！',
        'Yes! I will master site terms and correct signals, and do my best for zero accidents today!',
      ),
    },
  ],
  handbook: [
    t(
      '指示{しじ}を受{う}けたら、自分{じぶん}の勝手{かって}な判断{はんだん}で動{うご}かず「はい、わかりました」と明確{めいかく}に返事{へんじ}をしなければならない。',
      'When receiving instructions, never act on your own guess; clearly reply "Yes, understood".',
    ),
    t(
      '作業{さぎょう}を始{はじ}める前{まえ}に、必要{ひつよう}な工具{こうぐ}と手順{てじゅん}を揃{そろ}える「段取{だんど}り」を怠{おこた}ってはならない。',
      'Never neglect "dandori" (preparation) to organize needed tools and procedures before starting work.',
    ),
    t(
      '人{ひと}に工具{こうぐ}を手渡{てわた}すときは、無言{むごん}で差{さ}し出{だ}さず「行{い}きます」「手元{てもと}、どうぞ」と必{かなら}ず声{こえ}をかけなければならない。',
      'When passing tools to someone, never hand them silently; always call out "Coming through!" or "Here is the tool".',
    ),
    t(
      '危険{きけん}を感{かん}じたり異常{いじょう}を見{み}つけたりしたときは、遠慮{えんりょ}せず大声{おおごえ}で「危{あぶ}ない！」「止{と}めて！」と叫{さけ}ばなければならない。',
      'If you sense danger or spot an abnormality, shout "Danger!" or "Stop!" loudly without hesitation.',
    ),
    t(
      '指示{しじ}された言葉{ことば}の意味{いみ}がわからないときは、そのまま作業{さぎょう}を続{つづ}けず「もう一度{いちど}お願{ねが}いします」と聞{き}き直{なお}さなければならない。',
      'If you do not understand a spoken instruction, never continue working; ask "Please say that once more".',
    ),
    t(
      '床{ゆか}や壁{かべ}、製品{せいひん}を傷{きず}や火花{ひばな}から守{まも}るため、シートやテープによる「養生{ようじょう}」を省{はぶ}いてはならない。',
      'Never skip "youjou" (protective covering) with sheets or tape to protect floors, walls, and products from scratches and sparks.',
    ),
    t(
      '荷物{にもつ}の移動{いどう}やクレーン作業{さぎょう}では、「上{あ}げて」「下{さ}げて」「右{みぎ}」「左{ひだり}」「止{と}まれ」の合図{あいず}をハッキリ伝{つた}えなければならない。',
      'In crane operations and load handling, you must communicate signals like "Hoist", "Lower", "Right", "Left", and "Stop" clearly.',
    ),
    t(
      '作業{さぎょう}が終{お}わったら、使{つか}った工具{こうぐ}をその場{ば}に放置{ほうち}せず、定位置{ていいち}へ「片付{かたづ}け」なければならない。',
      'When work is finished, never leave used tools lying around; you must tidy and return them to designated spots.',
    ),
    t(
      '毎朝{まいあさ}の朝礼{ちょうれい}やKY（危険予知{きけんよち}）ミーティングには遅刻{ちこく}せず参加{さんか}し、危険{きけん}のポイントを確認{かくにん}しなければならない。',
      'You must attend the morning assembly and KY (hazard prediction) meeting on time to confirm safety points.',
    ),
    t(
      '「一服{いっぷく}」（小休止{しょうきゅうし}）の後{のち}は、保護具{ほごぐ}を再確認{さいかくにん}して「服装{ふくそう}ヨシ！」と指差呼称{しさこしょう}してから作業{さぎょう}へ戻{もど}らなければならない。',
      'After an "ippuku" (short break), recheck PPE, call "PPE check, yoshi!", and then return to work.',
    ),
  ],
  reel: [
    t(
      '朝礼{ちょうれい}、時間{じかん}厳守{げんしゅ}、ヨシ！',
      'Morning briefing, on time, yoshi!',
    ),
    t(
      '本日{ほんじつ}の段取{だんど}り、確認{かくにん}、ヨシ！',
      "Today's workflow setup, confirmed, yoshi!",
    ),
    t(
      '足元{あしもと}の養生{ようじょう}、ズレなし、ヨシ！',
      'Floor protective covering, no slippage, yoshi!',
    ),
    t(
      '手元{てもと}の工具{こうぐ}、声{こえ}かけ、ヨシ！',
      'Handing tools, call out first, yoshi!',
    ),
    t(
      '周囲{しゅうい}確認{かくにん}、行{い}きます、ヨシ！',
      'Surroundings clear, moving through, yoshi!',
    ),
    t(
      '危{あぶ}ない、一旦{いったん}停止{ていし}、ヨシ！',
      'Danger, pause and stop, yoshi!',
    ),
    t(
      '作業後{さぎょうご}の片付{かたづ}け、完了{かんりょう}、ヨシ！',
      'Post-work cleanup, complete, yoshi!',
    ),
    t(
      '今日{きょう}も一日{いちにち}、安全第一{あんぜんだいいち}、ヨシ！',
      'All day today, safety first, yoshi!',
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
      ),
      goal: t(
        '30秒{びょう}で現場{げんば}の危険{きけん}な言葉{ことば}や不安全{ふあんぜん}な行動{こうどう}を4つ見{み}つける。',
        'Find 4 dangerous words or unsafe actions on the job site within 30 seconds.',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '無言{むごん}で背後{はいご}から尖{とが}った工具{こうぐ}を手渡{てわた}す',
            'Handing sharp tools from behind silently',
          ),
        },
        {
          hazard: false,
          label: t(
            '「スパナ行{い}きます」と声{こえ}をかけてから手元{てもと}へ渡{わた}す',
            'Calling out "Spanner coming" before passing the tool',
          ),
        },
        {
          hazard: true,
          label: t(
            '指示{しじ}の意味{いみ}がわからないのに「はい」と答{こた}えて作業{さぎょう}する',
            'Saying "yes" and working despite not understanding instructions',
          ),
        },
        {
          hazard: false,
          label: t(
            '聞き{き}取れ{とれ}ない指示{しじ}を「もう一度{いちど}お願{ねが}いします」と確認{かくにん}する',
            'Asking "Please say once more" when instructions are unclear',
          ),
        },
        {
          hazard: true,
          label: t(
            '荷物{にもつ}が崩{くず}れそうなのに声{こえ}を出{だ}さず黙{だま}って見{み}ている',
            'Watching a tilting load silently without shouting a warning',
          ),
        },
        {
          hazard: false,
          label: t(
            '危険{きけん}を見{み}つけて大声{おおごえ}で「止{と}めて！危{あぶ}ない！」と叫{さけ}ぶ',
            'Shouting "Stop! Danger!" immediately upon spotting a hazard',
          ),
        },
        {
          hazard: true,
          label: t(
            'クレーンの合図{あいず}で声{こえ}を出{だ}さず曖昧{あいまい}に手{て}を振{ふ}る',
            'Waving hands vaguely without calling out crane signals',
          ),
        },
        {
          hazard: false,
          label: t(
            '「巻上{まきあ}げ、ヨシ！」と短{みじか}く明確{めいかく}に呼称{こしょう}する',
            'Calling out "Hoist, yoshi!" clearly and concisely',
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
      ),
      goal: t(
        '朝礼{ちょうれい}から作業{さぎょう}に就{つ}くまでの正{ただ}しい流{なが}れを順番{じゅんばん}に並{なら}べる。',
        'Arrange the correct workflow from morning assembly to starting work.',
      ),
      steps: [
        t(
          '1. 朝礼{ちょうれい}に参加{さんか}して体調{たいちょう}報告{ほうこく}と全体{ぜんたい}指示{しじ}を聞{き}く',
          '1. Attend morning assembly, report health, and listen to site instructions',
        ),
        t(
          '2. TBM（ツールボックスミーティング）で危険{きけん}予知{よち}（KY）を行{おこな}う',
          '2. Conduct hazard prediction (KY) in the tool box meeting',
        ),
        t(
          '3. 本日{ほんじつ}の手順{てじゅん}を確認{かくにん}し、工具{こうぐ}と保護具{ほごぐ}を「段取{だんど}り」する',
          "3. Check today's steps and arrange tools and PPE (dandori)",
        ),
        t(
          '4. 作業{さぎょう}場所{ばしょ}の床{ゆか}や設備{せつび}に傷{きず}を防{ふせ}ぐ「養生{ようじょう}」を行{おこな}う',
          '4. Apply protective covering (youjou) to floors and equipment',
        ),
        t(
          '5. 「周囲{しゅうい}ヨシ！」と指差呼称{しさこしょう}し、合図{あいず}と共{とも}に作業{さぎょう}を開始{かいし}する',
          '5. Call "Surroundings clear, yoshi!" and start work on signal',
        ),
      ],
    },
    {
      id: 'abunai-emergency-callout-seq',
      kind: 'seq',
      name: t(
        '危険{きけん}発見時{はっけんじ}の「危{あぶ}ない・止{と}めて」対応{たいおう}手順{てじゅん}',
        'Emergency response sequence when spotting hazards',
      ),
      goal: t(
        '作業場{さぎょうば}で急{きゅう}な危険{きけん}を見{み}つけたときの呼{よ}びかけ手順{てじゅん}を順番{じゅんばん}に並{なら}べる。',
        'Arrange the verbal warning and action steps when discovering immediate danger.',
      ),
      steps: [
        t(
          '1. 大声{おおごえ}で「危{あぶ}ない！」「止{と}めて！」と叫{さけ}んで周囲{しゅうい}に知{し}らせる',
          '1. Shout "Danger!" or "Stop!" loudly to alert everyone nearby',
        ),
        t(
          '2. 機械{きかい}の非常{ひじょう}停止{ていし}ボタンを押{お}すか、合図{あいず}で動作{どうさ}を止{と}めさせる',
          '2. Hit emergency stop or use hand signals to halt the operation',
        ),
        t(
          '3. 全員{ぜんいん}が安全{あんぜん}な退避{たいひ}場所{ばしょ}へ移動{いどう}したことを確認{かくにん}する',
          '3. Confirm that all workers have moved to a safe evacuation area',
        ),
        t(
          '4. 職長{しょくちょう}（リーダー）に状況{じょうきょう}を正{ただ}しく報告{ほうこく}する（報連相{ほうれんそう}）',
          '4. Report the situation accurately to the foreman (Horenso)',
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
      ),
      spots: [
        {
          x: 32,
          y: 30,
          check: t(
            '作業{さぎょう}内容{ないよう}と危険{きけん}ポイント：具体的{ぐたいてき}な行動{こうどう}目標{もくひょう}が書{か}かれているか（良品{りょうひん}：明記{めいき} / 不良{ふりょう}：空欄{くうらん}・未記入{みきにゅう}）',
            'Work contents & hazard points: Concrete action target written (Good: Clearly written / Defect: Blank or unwritten)',
          ),
        },
        {
          x: 68,
          y: 70,
          check: t(
            '指差呼称{しさこしょう}項目{こうもく}と参加者{さんかしゃ}サイン：全員{ぜんいん}が確認{かくにん}して名前{なまえ}を書{か}いたか（良品{りょうひん}：全員{ぜんいん}署名{しょめい} / 不良{ふりょう}：署名{しょめい}なし）',
            'Pointing & calling item & signatures: Everyone confirmed and signed (Good: All signed / Defect: Missing signatures)',
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
      ),
      spots: [
        {
          x: 28,
          y: 36,
          check: t(
            '養生{ようじょう}テープの固定{こてい}：シートの端{はし}がめくれず床{ゆか}に密着{みっちゃく}しているか（良品{りょうひん}：全面{ぜんめん}テープ固定{こてい} / 不良{ふりょう}：端{はし}がめくれて浮{う}いている）',
            'Curing tape fixing: Edges flat without peeling up (Good: Taped firmly / Defect: Edges curling up)',
          ),
        },
        {
          x: 72,
          y: 64,
          check: t(
            '滑{すべ}り止{ど}めと破れ{やぶれ}：シートにつまずく穴{あな}や裂け目{さけめ}がないか（良品{りょうひん}：破れ{やぶれ}なし / 不良{ふりょう}：大{おお}きな破れ{やぶれ}と穴{あな}がある）',
            'Tear and anti-slip: No tears or trip holes in the sheet (Good: Intact / Defect: Torn with trip hazard)',
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
      ),
      spots: [
        {
          x: 36,
          y: 32,
          check: t(
            '刃物{はもの}・尖{とが}った工具{こうぐ}の保護{ほご}：サヤやキャップが被{かぶ}せてあるか（良品{りょうひん}：カバー装着{そうちゃく} / 不良{ふりょう}：刃先{はさき}が露出{ろしゅつ}）',
            'Sharp tool protection: Sheaths or caps fitted on blades (Good: Covered / Defect: Exposed sharp edge)',
          ),
        },
        {
          x: 64,
          y: 68,
          check: t(
            '受渡{うけわた}し用{よう}グリップ：油{あぶら}汚れ{よごれ}がなく滑{すべ}らず持{も}てるか（良品{りょうひん}：清掃{せいそう}済{ず}み / 不良{ふりょう}：油{あぶら}まみれで滑{すべ}る）',
            'Handover grip: Clean and non-slippery for safe passing (Good: Cleaned / Defect: Covered in grease)',
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
      ),
      options: [
        t(
          '作業{さぎょう}手順{てじゅん}を確認{かくにん}し、必要{ひつよう}な部材{ぶざい}・工具{こうぐ}・保護具{ほごぐ}を前{まえ}もって揃{そろ}える',
          'Review work procedures and gather needed materials, tools, and PPE in advance',
        ),
        t(
          '始業{しぎょう}チャイムが鳴{な}り終{お}わるまで休憩所{きゅうけいじょ}で座{すわ}って待{ま}つ',
          'Sit and wait in the break area until the start chime finishes',
        ),
        t(
          '指示{しじ}を受{う}ける前{まえ}に自分{じぶん}の判断{はんだん}だけで勝手{かって}に機械{きかい}を動{うご}かす',
          'Start operating machines on your own judgment before receiving instructions',
        ),
        t(
          '今日{きょう}使{つか}う予定{よてい}のない壊{こわ}れた古い工具{こうぐ}を磨{みが}き始{はじ}める',
          'Start polishing old broken tools that are not scheduled for use today',
        ),
      ],
      answer: 0,
      why: t(
        '「段取{だんど}り」とは、作業{さぎょう}を安全{あんぜん}かつスムーズに進{すす}めるために、事前{じぜん}に手順{てじゅん}や道具{どうぐ}を準備{じゅんび}することです。',
        'Dandori means preparing procedures, tools, and materials in advance to ensure smooth and safe work.',
      ),
    },
    {
      q: t(
        '先輩{せんぱい}職人{しょくにん}の「手元{てもと}」につくことになりました。手元{てもと}の正{ただ}しい役目{やくめ}はどれですか？',
        'You are assigned as "temoto" (assistant) to a senior craftsman. What is your correct role?',
      ),
      options: [
        t(
          '職人{しょくにん}から離{はな}れた場所{ばしょ}で座{すわ}ってスマートフォンを見{み}る',
          'Sit far away from the craftsman and look at your smartphone',
        ),
        t(
          '作業{さぎょう}のそばで安全{あんぜん}を見守{みまも}り、頼{たの}まれた工具{こうぐ}や材料{ざいりょう}を声{こえ}をかけて手渡{てわた}す',
          'Watch safety nearby and hand over requested tools and materials after calling out',
        ),
        t(
          '職人{しょくにん}に無断{むだん}で自分{じぶん}一人{ひとり}だけで別{べつ}の難{むずか}しい作業{さぎょう}を始{はじ}める',
          'Start another difficult task alone without informing the craftsman',
        ),
        t(
          '職人{しょくにん}の作業{さぎょう}を見{み}ずに自分{じぶん}の荷物{にもつ}の整理{せいり}だけを行{おこな}う',
          "Ignore the craftsman's work and only organize your own personal belongings",
        ),
      ],
      answer: 1,
      why: t(
        '「手元{てもと}」は職人{しょくにん}のそばで道具{どうぐ}の受渡{うけわた}しや安全{あんぜん}確認{かくにん}を助{たす}ける大切{たいせつ}な補助{ほじょ}役{やく}です。',
        'Temoto is an important assistant role helping with tool handovers and safety watching beside the craftsman.',
      ),
    },
    {
      q: t(
        '溶接{ようせつ}作業{さぎょう}の周辺{しゅうへん}で「養生{ようじょう}をしてくれ」と頼{たの}まれました。何{なに}をすべきですか？',
        'You were asked to "do youjou" around a welding area. What should you do?',
      ),
      options: [
        t(
          '体調{たいちょう}が悪{わる}いので医務室{いむしつ}へ休{やす}みに行{い}く',
          'Go to the clinic to rest because you feel unwell',
        ),
        t(
          '火花{ひばな}が見{み}えやすいよう周囲{しゅうい}の照明{しょうめい}を全{すべ}て消{け}す',
          'Turn off all surrounding lights to make sparks easier to see',
        ),
        t(
          '防炎{ぼうえん}シートなどで周辺{しゅうへん}の床{ゆか}や機械{きかい}を覆{おお}い、火花{ひばな}の飛散{ひさん}や傷{きず}を防{ふせ}ぐ',
          'Cover surrounding floors and machinery with fire-resistant sheets to block sparks and damage',
        ),
        t(
          '床{ゆか}に水{みず}を大量{たいりょう}に撒{ま}いて作業場{さぎょうば}を水浸{みずびた}しにする',
          'Splash large amounts of water on the floor, flooding the entire workspace',
        ),
      ],
      answer: 2,
      why: t(
        '現場{げんば}の「養生{ようじょう}」は、シートやテープで周囲{しゅうい}を保護{ほご}し、火災{かさい}や製品{せいひん}の汚損{おそん}を防{ふせ}ぐ作業{さぎょう}です。',
        'On site, "youjou" means protecting surroundings with sheets and tape to prevent fire, damage, and stains.',
      ),
    },
    {
      q: t(
        '吊{つ}り荷{に}のワイヤーが外{はず}れそうになり、仲間{なかま}の頭上{ずじょう}へ傾{かたむ}いてきました。どう叫{さけ}びますか？',
        "A suspended crane load wire is slipping and tilting toward a coworker's head. What do you shout?",
      ),
      options: [
        t(
          '「お疲{つか}れさまでした」と丁寧{ていねい}に挨拶{あいさつ}する',
          'Politely greet with "Thank you for your hard work"',
        ),
        t(
          '「次{つぎ}の休憩{きゅうけい}は何時{なんじ}ですか」と質問{しつもん}する',
          'Ask "What time is our next break?"',
        ),
        t(
          '声{こえ}を出{だ}すと驚{おどろ}かせてしまうので何{なに}も言{い}わず走{はし}って逃{に}げる',
          'Say nothing and run away to avoid startling anyone',
        ),
        t(
          '大声{おおごえ}で「危{あぶ}ない！」「止{と}めて！」と叫{さけ}び、作業{さぎょう}を緊急{きんきゅう}停止{ていし}させる',
          'Shout "Danger!" and "Stop!" at the top of your voice to halt the operation immediately',
        ),
      ],
      answer: 3,
      why: t(
        '命{いのち}に関{かか}わる危険{きけん}を見{み}つけたときは、誰{だれ}でも即座{そくざ}に「危{あぶ}ない！」「止{と}めて！」と大声{おおごえ}で危険{きけん}を知{し}らせます。',
        'When spotting life-threatening danger, anyone must immediately shout "Danger!" and "Stop!" to alert everyone.',
      ),
    },
    {
      q: t(
        '夕方{ゆうがた}の「終礼{しゅうれい}」の前{まえ}に職長{しょくちょう}から「片付{かたづ}け！」の合図{あいず}がありました。正{ただ}しい対応{たいおう}はどれですか？',
        'Before evening shuurei (closing debrief), the foreman signals "Kataduke!". What is the correct response?',
      ),
      options: [
        t(
          '使{つか}った工具{こうぐ}を拭{ふ}いて定位置{ていいち}へ戻{もど}し、ゴミを分別{ぶんべつ}して片付{かたづ}ける',
          'Wipe used tools clean, return them to designated spots, sort trash, and tidy up',
        ),
        t(
          '工具{こうぐ}を通路{つうろ}に置{お}いたままタイムカードを押{お}しに走{はし}る',
          'Leave tools in the walkway and run to punch the time clock',
        ),
        t(
          '機械{きかい}のスイッチを入{い}れたまま翌朝{よくあさ}まで放置{ほうち}する',
          'Leave machine switches ON unattended until the next morning',
        ),
        t(
          '自分{じぶん}のゴミを他{ほか}の作業員{さぎょういん}の足元{あしもと}に押{お}し込{こ}んで隠{かく}す',
          "Shove your trash under other workers' feet to hide it",
        ),
      ],
      answer: 0,
      why: t(
        '「片付{かたづ}け」は道具{どうぐ}を元{もと}の場所{ばしょ}へ戻{もど}し、ゴミを処理{しょり}して現場{げんば}の安全{あんぜん}を確保{かくほ}する基本{きほん}です。',
        'Kataduke means returning tools to their designated spots and cleaning scrap to secure site safety.',
      ),
    },
    {
      q: t(
        'クレーンで荷物{にもつ}を移動{いどう}中{ちゅう}、合図者{あいずしゃ}から「もう少{すこ}し上{あ}げて！」と声{こえ}がかかりました。どういう意味{いみ}ですか？',
        'While moving a crane load, the signaler calls out "Mou sukoshi agete!". What does this mean?',
      ),
      options: [
        t(
          '荷物{にもつ}を急{きゅう}に一番{いちばん}下{した}まで落{お}とすこと',
          'Suddenly drop the load all the way to the bottom',
        ),
        t(
          '荷物{にもつ}をあとわずかだけ上{うえ}に巻{ま}き上{あ}げること',
          'Hoist the load slightly higher by a small amount',
        ),
        t(
          '荷物{にもつ}を右{みぎ}へ大{おお}きく振{ふ}り回{まわ}すこと',
          'Swing the load widely to the right',
        ),
        t(
          'クレーンの電源{でんげん}を切{き}ってそのまま離{はな}れること',
          'Turn off crane power and walk away immediately',
        ),
      ],
      answer: 1,
      why: t(
        '「もう少{すこ}し上{あ}げて」は「わずかに上昇{じょうしょう}させる」合図{あいず}です。微調整{びちょうせい}のときは慎重{しんちょう}に動{うご}かします。',
        '"Mou sukoshi agete" signals hoisting up just a little bit. Make fine adjustments very carefully.',
      ),
    },
    {
      q: t(
        '毎朝{まいあさ}行{おこな}われる「朝礼{ちょうれい}」の大切{たいせつ}な目的{もくてき}は何{なに}ですか？',
        'What is the vital purpose of the "chourei" (morning assembly) held every morning?',
      ),
      options: [
        t(
          '朝{あさ}ご飯{はん}のメニューを全員{ぜんいん}で決{き}めること',
          'Decide the breakfast menu together as a team',
        ),
        t(
          '歌{うた}やゲームをして楽{たの}しく遊{あそ}ぶこと',
          'Sing songs and play games for casual entertainment',
        ),
        t(
          '健康{けんこう}状態{じょうたい}の確認{かくにん}、作業{さぎょう}予定{よてい}と安全{あんぜん}指示{しじ}を全員{ぜんいん}で共有{きょうゆう}すること',
          'Confirm health condition, and share work schedules and safety instructions with all workers',
        ),
        t(
          '昨日{きのう}ミスをした人{ひと}を見{み}つけて全員{ぜんいん}の前{まえ}で叱{しか}ること',
          'Find someone who made a mistake yesterday and scold them publicly',
        ),
      ],
      answer: 2,
      why: t(
        '「朝礼{ちょうれい}」は体調{たいちょう}の確認{かくにん}と、その日{ひ}の危険{きけん}や作業{さぎょう}指示{しじ}を全員{ぜんいん}で統一{とういつ}するための重要{じゅうよう}な場{ば}です。',
        'Chourei is crucial for checking worker health and aligning daily work instructions and safety targets.',
      ),
    },
    {
      q: t(
        '午前{ごぜん}10時{じ}に職長{しょくちょう}が「一服{いっぷく}にしよう」と言{い}いました。どう行動{こうどう}するのが正{ただ}しいですか？',
        'At 10:00 AM, the foreman says "Let\'s have an ippuku". What is the correct action?',
      ),
      options: [
        t(
          '今日{きょう}の作業{さぎょう}が終{お}わったと勘違{かんちが}いして勝手{かって}に帰宅{きたく}する',
          'Mistake it for the end of the day and head home on your own',
        ),
        t(
          '機械{きかい}の主電源{しゅでんげん}を入{い}れたまま休憩所{きゅうけいじょ}へ走{はし}っていく',
          'Run to the break area leaving the machine main power switched ON',
        ),
        t(
          '無断{むだん}で工場{こうじょう}の敷地{しきち}外{がい}へ出{で}て長{なが}い買{か}い物{もの}をする',
          'Leave the factory grounds without permission for a long shopping trip',
        ),
        t(
          '作業{さぎょう}を一旦{いったん}安全{あんぜん}に止{と}め、指定{してい}場所{ばしょ}で水分{すいぶん}補給{ほきゅう}や小休止{しょうきゅうし}を取{と}る',
          'Safely pause work, and take a short rest and hydrate at the designated break spot',
        ),
      ],
      answer: 3,
      why: t(
        '「一服{いっぷく}」は集中力{しゅうちゅうりょく}を保{たも}つための短{みじか}い休憩{きゅうけい}です。機械{きかい}の安全{あんぜん}を確認{かくにん}してから休{やす}みます。',
        'Ippuku is a short break to maintain focus and prevent fatigue. Pause machines safely before resting.',
      ),
    },
    {
      q: t(
        '現場{げんば}で「職長{しょくちょう}」や「親方{おやかた}」呼{よ}ばれる人{ひと}は、どのような役割{やくわり}の人{ひと}ですか？',
        'What role do the people called "shokuchou" or "oyakata" play on the job site?',
      ),
      options: [
        t(
          '現場{げんば}で作業員{さぎょういん}を直接{ちょくせつ}指導{しどう}し、安全{あんぜん}と作業{さぎょう}を指揮{しき}するリーダー',
          'Leaders who directly guide workers on site and direct safety and operations',
        ),
        t(
          '現場{げんば}へ一度{いちど}も来{こ}ない遠{とお}くの取引先{とりひきさき}の担当者{たんとうしゃ}',
          'A contact person from a distant client company who never visits the site',
        ),
        t(
          'その日{ひ}初{はじ}めてアルバイトに来{き}た見習{みなら}いの作業員{さぎょういん}',
          'A novice part-time trainee who came to the site for the very first time today',
        ),
        t(
          '現場{げんば}に清涼{せいりょう}飲料{いんりょう}の自動{じどう}販売機{はんばいき}を補充{ほじゅう}に来{く}る業者{ぎょうしゃ}',
          'A vendor who comes to restock soft drink vending machines on site',
        ),
      ],
      answer: 0,
      why: t(
        '「職長{しょくちょう}」や「親方{おやかた}」は作業{さぎょう}の指示{しじ}と現場{げんば}の安全{あんぜん}を守{まも}る責任者{せきにんしゃ}です。困{こま}ったときは相談{そうだん}します。',
        'Shokuchou and oyakata are leaders responsible for directing operations and maintaining safety on site.',
      ),
    },
    {
      q: t(
        '狭{せま}い通路{つうろ}で長{なが}い鉄パイプを運{はこ}んでいます。角{かど}を曲{ま}がるときはどう声{こえ}をかけますか？',
        'You are carrying a long steel pipe in a narrow walkway. How should you call out when turning a corner?',
      ),
      options: [
        t(
          '誰{だれ}にも気づ{きづ}かれないよう足音{あしおと}を忍{しの}ばせて通{とお}る',
          'Walk silently on tiptoe so no one notices you',
        ),
        t(
          '「長物{ながもの}通{とお}ります！」「行{い}きます！」と前{まえ}もって大声{おおごえ}で呼{よ}びかける',
          'Call out loudly in advance: "Long object passing!" or "Coming through!"',
        ),
        t(
          '角{かど}の向{む}こうを見{み}ずにパイプを前{まえ}に突{つ}き出{だ}して走{はし}る',
          'Thrust the pipe forward and run without checking around the corner',
        ),
        t(
          'スマートフォンで音楽{おんがく}を大音量{だいおんりょう}で鳴{な}らしながら歩{ある}く',
          'Walk while playing loud music on your smartphone',
        ),
      ],
      answer: 1,
      why: t(
        '見通{みとお}しの悪{わる}い角{かど}や長物{ながもの}の運搬{うんぱん}では、「行{い}きます！」と声{こえ}をかけて衝突{しょうとつ}事故{じこ}を防{ふせ}ぎます。',
        'At blind corners or when carrying long items, calling out "Coming through!" prevents collisions.',
      ),
    },
    {
      q: t(
        '作業{さぎょう}直前{ちょくぜん}に行{おこな}う「TBM（ツールボックスミーティング）」の正{ただ}しい説明{せつめい}はどれですか？',
        'Which of the following is the correct explanation of a pre-work "TBM" (Tool Box Meeting)?',
      ),
      options: [
        t(
          '新{あたら}しい道具{どうぐ}箱{ばこ}を買{か}うためにお金{かね}を集{あつ}める会合{かいごう}',
          'A meeting to collect money to purchase new toolboxes',
        ),
        t(
          '作業{さぎょう}中{ちゅう}に道具{どうぐ}箱{ばこ}を椅子{いす}代{が}わりにして座{すわ}る練習{れんしゅう}',
          'Practice using a toolbox as a stool to sit on during work',
        ),
        t(
          '作業{さぎょう}現場{げんば}で道具{どうぐ}箱{ばこ}を囲{かこ}み、短時間{たんじかん}で作業{さぎょう}内容{ないよう}と危険{きけん}を確認{かくにん}する打合{うちあわ}せ',
          'A short meeting gathered around the toolbox on site to confirm work details and hazards',
        ),
        t(
          '壊{こわ}れた機械{きかい}を秘密{ひみつ}で隠{かく}すための相談{そうだん}',
          'A secret consultation to hide broken machinery from management',
        ),
      ],
      answer: 2,
      why: t(
        '「TBM」は作業{さぎょう}直前{ちょくぜん}に現場{げんば}で短時間{たんじかん}で行{おこな}う打合せ{うちあわせ}で、作業{さぎょう}の役割{やくわり}と危険{きけん}を再確認{さいかくにん}します。',
        'TBM is a brief on-site meeting right before work to reaffirm task assignments and hazards.',
      ),
    },
    {
      q: t(
        '一日{いちにち}の作業{さぎょう}が終了{しゅうりょう}したとき、現場{げんば}の仲間{なかま}や職長{しょくちょう}に対{たい}して交{か}わす正{ただ}しい挨拶{あいさつ}はどれですか？',
        "When the day's work is finished, what is the correct greeting to exchange with coworkers and foreman?",
      ),
      options: [
        t('「おはようございます」', '"Good morning"'),
        t('「ごめんなさい」', '"I am sorry"'),
        t('「いただきます」', '"Thank you for the meal"'),
        t(
          '「お疲{つか}れさまでした」（または「ご安全{あんぜん}に」）',
          '"Otsukaresama deshita" (or "Go-anzen ni")',
        ),
      ],
      answer: 3,
      why: t(
        '作業{さぎょう}終了時{しゅうりょうじ}には「お疲{つか}れさまでした」や「ご安全{あんぜん}に」と挨拶{あいさつ}し、互{たが}いの無事故{むじこ}と労苦{ろうく}をねぎらいます。',
        'At the end of work, exchange "Otsukaresama deshita" or "Go-anzen ni" to appreciate each other\'s safe work.',
      ),
    },
  ],
  glossary: [
    {
      ja: 'ヨシ',
      reading: 'よし',
      en: 'Yoshi / All clear / Confirmed',
      note: t(
        '指差呼称などで対象の安全を確認したときに発声する決め言葉。',
        'Standard call-out spoken when confirming safety through pointing and calling.',
      ),
    },
    {
      ja: '段取り',
      reading: 'だんどり',
      en: 'Dandori / Setup, preparation, workflow planning',
      note: t(
        '作業を安全かつ手際よく進めるため、前もって手順や道具を整えること。',
        'Arranging procedures, tools, and materials in advance for safe and efficient work.',
      ),
    },
    {
      ja: '片付け',
      reading: 'かたづけ',
      en: 'Kataduke / Tidying up, sorting and packing tools',
      note: t(
        '使った工具を元の場所に戻し、ゴミを片付けて作業場を整えること。',
        'Returning used tools to proper storage and clearing waste to tidy the workspace.',
      ),
    },
    {
      ja: '養生',
      reading: 'ようじょう',
      en: 'Youjou / Protective covering, masking, curing',
      note: t(
        '作業中の傷・汚れ・火花などから製品や床・設備をシート等で保護すること。',
        'Protecting products, floors, and machinery from damage, stains, or sparks using sheets.',
      ),
    },
    {
      ja: '手元',
      reading: 'てもと',
      en: 'Temoto / Helper, assistant, handing tools',
      note: t(
        '熟練職人のすぐそばで道具を手渡したり周囲の安全を確認したりする補助役。',
        'Assistant who passes tools and monitors surroundings beside a skilled craftsman.',
      ),
    },
    {
      ja: '職長',
      reading: 'しょくちょう',
      en: 'Shokuchou / On-site foreman, safety supervisor',
      note: t(
        '現場で作業員を直接指導し、作業指示と安全管理を行うリーダー。',
        'On-site leader who directly instructs workers and manages safety and operations.',
      ),
    },
    {
      ja: '親方',
      reading: 'おやかた',
      en: 'Oyakata / Master craftsperson, crew boss',
      note: t(
        '長年の経験を持ち、作業班や下請け職人を束ねる熟練のリーダー。',
        'Experienced master craftsman who leads a work crew or trade subcontractors.',
      ),
    },
    {
      ja: '元請・下請',
      reading: 'もとうけ・したうけ',
      en: 'Motouke / Shitauke (Prime contractor / Subcontractor)',
      note: t(
        '発注者から直接工事を引き受ける元請と、その一部を請け負う下請企業。',
        'Prime contractor taking direct orders from clients, and subcontractors handling parts of the job.',
      ),
    },
    {
      ja: '朝礼・終礼',
      reading: 'ちょうれい・しゅうれい',
      en: 'Chourei / Shuurei (Morning assembly / Evening debrief)',
      note: t(
        '朝の安全指示や連絡を行う朝礼と、一日の作業結果を確認する終礼。',
        'Morning gathering for safety notices and evening debrief to review shift results.',
      ),
    },
    {
      ja: '一服',
      reading: 'いっぷく',
      en: 'Ippuku / Short rest or tea break',
      note: t(
        '午前や午後に取る10〜15分程度の短い小休止。疲労を防ぎ集中力を回復する。',
        'A short 10-15 minute rest during morning or afternoon to prevent fatigue and restore focus.',
      ),
    },
    {
      ja: '危険予知（KY）',
      reading: 'きけんよち',
      en: 'KY (Kiken Yochi) / Hazard Prediction Activity',
      note: t(
        '作業に潜む危険を事前に話し合い、対策を決めて事故を防ぐ活動。',
        'Activity where team discusses hidden hazards in advance and sets countermeasures.',
      ),
    },
    {
      ja: 'TBM',
      reading: 'てぃーびーえむ',
      en: 'TBM (Tool Box Meeting) / Pre-work safety huddle',
      note: t(
        '作業開始直前に現場で工具箱を囲んで行う、5分程度の安全打ち合わせ。',
        'Brief 5-minute safety huddle around the toolbox on site right before starting work.',
      ),
    },
    {
      ja: '安全第一',
      reading: 'あんぜんだいいち',
      en: 'Anzen Daiichi / Safety First',
      note: t(
        '生産や納期よりも、働く人の命と安全を最優先にする基本理念。',
        "Core philosophy putting workers' lives and safety above production and deadlines.",
      ),
    },
  ],
}
