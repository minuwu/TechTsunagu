import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string): Tri => ({ ja, en })

export const hourenSou: CourseData = {
  id: 'houren-sou',
  category: 'language',
  level: 1,
  minutes: 15,
  title: t(
    '報連相{ほうれんそう}と緊急{きんきゅう}フレーズ',
    'Reporting & Emergency Phrases (Hou-Ren-So)',
  ),
  sub: t(
    '現場{げんば}で伝{つた}える、命{いのち}を守{まも}る、迷{まよ}ったら聞{き}き返{かえ}す。',
    'Report on site, protect lives, and never hesitate to ask again.',
  ),
  refs: [
    '労働安全衛生法 第59条（雇入れ時等の安全衛生教育）',
    '労働安全衛生規則 第526条（救急用具及び救護体制）',
    '消防法 第24条（火災の通報義務・119番）',
  ],
  panels: [
    {
      who: 'foreman',
      text: t(
        '現場{げんば}では「報連相{ほうれんそう}」（報告{ほうこく}・連絡{れんらく}・相談{そうだん}）が一番{いちばん}大切{たいせつ}です。困{こま}ったときはすぐ伝{つた}えてください。',
        'On site, "Hou-Ren-So" (Report, Contact, Consult) is essential. If anything troubles you, tell me right away.',
      ),
    },
    {
      who: 'worker',
      text: t(
        'はい、分{わ}かりました！今日{きょう}の作業{さぎょう}手順{てじゅん}はだいたい分{わ}かりました。',
        "Yes, understood! I mostly understand today's work procedure.",
      ),
    },
    {
      who: 'foreman',
      text: t(
        '「だいたい」は危{あぶ}ないよ。早口{はやくち}で聞{き}き取{と}れなかったときは、何{なん}と言{い}いますか？',
        '"Mostly" is dangerous. If I speak too fast and you cannot catch it, what do you say?',
      ),
    },
    {
      who: 'worker',
      text: t(
        '怒{おこ}られるのが怖{こわ}くて、分{わ}からなくても「はい」とうなずいてしまいました…',
        'I was afraid of being scolded, so I just nodded "Yes" even though I did not understand...',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '分{わ}からないまま作業{さぎょう}するのが一番{いちばん}危険{きけん}です。「もう一度{いちど}お願{ねが}いします」「ゆっくり話{はな}してください」と聞{き}き返{かえ}していいんだよ。',
        'Working without understanding is the most dangerous thing. It is always right to ask: "Please say that again" or "Please speak slowly."',
      ),
    },
    {
      who: 'worker',
      text: t(
        '安心{あんしん}しました！では、機械{きかい}が変{へん}な音{おと}を立{た}てたり故障{こしょう}したときは、どう伝{つた}えますか？',
        'That is a relief! So if a machine makes strange noises or breaks down, how should I report it?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'まず機械{きかい}を止{と}めて「異音{いおん}がします」と結果{けっか}から報告{ほうこく}してください。言{い}い訳{わけ}をせず、事実{じじつ}をすぐ伝{つた}えるのが基本{きほん}です。',
        'First stop the machine and report the result: "There is an abnormal noise." Stating facts first without excuses is the rule.',
      ),
    },
    {
      who: 'worker',
      text: t(
        'もし現場{げんば}で同僚{どうりょう}が大怪我{おおけが}をしたり倒{たお}れたら、どう叫{さけ}べばいいですか？',
        'If a coworker gets badly injured or collapses on site, what should I shout?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '大声{おおごえ}で「助{たす}けてください！」「救急車{きゅうきゅうしゃ}をお願{ねが}いします！」と叫{さけ}び、119番{ばん}通報{つうほう}します。',
        'Shout loudly: "Tasukete kudasai!" (Help!) and "Kyukyusha o onegaishimasu!" (Call an ambulance!), then call 119.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '「助{たす}けてください」「もう一度{いちど}お願{ねが}いします」を覚{おぼ}えて、安全{あんぜん}に報連相{ほうれんそう}を徹底{てってい}します！',
        'I will remember "Please help" and "Please repeat," and practice thorough Hou-Ren-So safely!',
      ),
    },
  ],
  handbook: [
    t(
      '指示{しじ}が分{わ}からないときは、うなずかずに「もう一度{いちど}お願{ねが}いします」と聞{き}き返{かえ}す。',
      'When you do not understand instructions, never just nod; ask "Please say that again."',
    ),
    t(
      '日本語{にほんご}が早{はや}くて聞{き}き取{と}れないときは、「ゆっくり話{はな}してください」と頼{たの}む。',
      'If Japanese is spoken too fast to catch, request "Please speak slowly."',
    ),
    t(
      '報告{ほうこく}するときは、言{い}い訳{わけ}を言{い}わずに「結論{けつろん}と事実{じじつ}」を最初{さいしょ}に伝{つた}える。',
      'When giving a report, state the conclusion and facts first without making excuses.',
    ),
    t(
      '小{ちい}さなミスやヒヤリ・ハットでも、隠{かく}さずに直{ただ}ちに職長{しょくちょう}へ報告{ほうこく}する。',
      'Report even small mistakes or near-misses immediately to your foreman without hiding them.',
    ),
    t(
      '危険{きけん}な状態{じょうたい}を見{み}つけたら、作業{さぎょう}を止{と}めて大声{おおごえ}で「危{あぶ}ない！」と周囲{しゅうい}に知{し}らせる。',
      'If you spot a dangerous condition, halt work and shout "Abunai!" (Watch out!) to alert those around you.',
    ),
    t(
      '事故{じこ}や怪我{けが}人{にん}を発見{はっけん}したときは、大声{おおごえ}で「助{たす}けてください！」と叫{さけ}んで人{ひと}を集{あつ}める。',
      'If you find an accident or injured person, shout "Tasukete kudasai!" (Help!) loudly to gather help.',
    ),
    t(
      '体調{たいちょう}が良{よ}くないときは我慢{がまん}せず、作業前{さぎょうまえ}に「気分{きぶん}が悪{わる}いです」と相談{そうだん}する。',
      'If feeling unwell, never endure it silently; consult your foreman before work: "Kibun ga warui desu."',
    ),
    t(
      '火災{かさい}や救急{きゅうきゅう}の119番{ばん}通報{つうほう}では、最初{さいしょ}に「火事{かじ}」か「救急{きゅうきゅう}」かをはっきり言{い}う。',
      'When calling 119 for fire or medical emergency, state clearly first whether it is "Fire" or "Ambulance."',
    ),
    t(
      '事故{じこ}や犯罪{はんざい}・盗難{とうなん}の警察通報{けいさつつうほう}は「110番{ばん}」に電話{でんわ}する。',
      'Call 110 for police reporting of accidents, crimes, or theft.',
    ),
    t(
      '安全{あんぜん}に関{かか}わる大切{たいせつ}な説明{せつめい}で理解{りかい}できないときは、「通訳{つうやく}をお願{ねが}いします」と伝{つた}える。',
      'If you cannot understand vital safety instructions, say "Tsuyaku o onegaishimasu" (Please get an interpreter).',
    ),
  ],
  reel: [
    t('もう一度{いちど}お願{ねが}いします！', 'Please say that again!'),
    t('ゆっくり話{はな}してください！', 'Please speak slowly!'),
    t('助{たす}けてください！', 'Please help me!'),
    t('危{あぶ}ない、ストップ！', 'Watch out, stop!'),
    t('火事{かじ}です、119番{ばん}！', 'Fire! Call 119!'),
    t(
      '救急車{きゅうきゅうしゃ}をお願{ねが}いします！',
      'Please call an ambulance!',
    ),
    t(
      '報告{ほうこく}・連絡{れんらく}・相談{そうだん}、ヨシ！',
      'Report, communicate, consult, yoshi!',
    ),
  ],
  drills: [
    {
      id: 'spot-houren-sou-hazards',
      kind: 'spot',
      secs: 30,
      need: 4,
      name: t(
        'コミュニケーションの危険予知{きけんよち}（KYT）',
        'Communication Hazard Spotting (KYT)',
      ),
      goal: t(
        '危険{きけん}な受{う}け答{こた}えや行動{こうどう}を4つ見{み}つける。',
        'Find 4 dangerous communication hazards or actions.',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '分{わ}からないのに「はい」とうなずく',
            'Nodding "Yes" when not understanding',
          ),
        },
        {
          hazard: false,
          label: t(
            '聞{き}き取{と}れないとき「もう一度{いちど}お願{ねが}いします」と聞{き}き返{かえ}す',
            'Asking "Please repeat" when not catching words',
          ),
        },
        {
          hazard: true,
          label: t(
            '機械{きかい}の異音{いおん}に気{き}づいても「気{き}のせい」と放置{ほうち}する',
            'Ignoring machine noise thinking it is nothing',
          ),
        },
        {
          hazard: true,
          label: t(
            '小{ちい}さな怪我{けが}を職長{しょくちょう}に隠{かく}して作業{さぎょう}を続{つづ}ける',
            'Hiding a minor injury from foreman and working',
          ),
        },
        {
          hazard: false,
          label: t(
            '小{ちい}さなミスでも直{ただ}ちに作業{さぎょう}を止{と}めて職長{しょくちょう}に報告{ほうこく}する',
            'Stopping work immediately and reporting small error to foreman',
          ),
        },
        {
          hazard: true,
          label: t(
            '体調{たいちょう}が悪{わる}いのに我慢{がまん}して高所{こうしょ}作業{さぎょう}に上{あ}がる',
            'Enduring illness and climbing to work at heights',
          ),
        },
        {
          hazard: false,
          label: t(
            '作業前{さぎょうまえ}に「気分{きぶん}が悪{わる}いです」と職長{しょくちょう}に相談{そうだん}する',
            'Consulting foreman before work saying feeling unwell',
          ),
        },
        {
          hazard: false,
          label: t(
            '指示{しじ}された内容{ないよう}をメモして声{こえ}に出{だ}して復唱{ふくしょう}する',
            'Writing down instructions and repeating them aloud',
          ),
        },
        {
          hazard: true,
          label: t(
            '火事{かじ}を発見{はっけん}したのに声{こえ}を出{だ}さず一人{ひとり}で逃{に}げる',
            'Fleeing alone without shouting when spotting a fire',
          ),
        },
      ],
    },
    {
      id: 'call-119-seq',
      kind: 'seq',
      name: t(
        '119番{ばん}通報{つうほう}の手順{てじゅん}',
        '119 Emergency Call Sequence',
      ),
      goal: t(
        '火災{かさい}・救急{きゅうきゅう}通報{つうほう}の正{ただ}しい流{なが}れを順番{じゅんばん}に並{なら}べる。',
        'Arrange the correct sequence for fire or ambulance 119 reporting.',
      ),
      steps: [
        t('電話{でんわ}で「119」をダイヤルする', 'Dial 119 on the telephone'),
        t(
          '「救急{きゅうきゅう}です」または「火事{かじ}です」と告{つ}げる',
          'State clearly "Ambulance" or "Fire"',
        ),
        t(
          '現場{げんば}の住所{じゅうしょ}と会社名{かいしゃめい}・工場{こうじょう}の場所{ばしょ}を伝{つた}える',
          'Provide site address, company name, and building location',
        ),
        t(
          '怪我{けが}人{にん}の人数{にんずう}や意識{いしき}・火災{かさい}の状況{じょうきょう}を説明{せつめい}する',
          'Describe number of casualties, consciousness, and fire situation',
        ),
        t(
          '通報者{つうほうしゃ}の氏名{しめい}と連絡先{れんらくさき}電話番号{でんわばんごう}を伝{つた}える',
          'Give your name and callback telephone number',
        ),
      ],
    },
    {
      id: 'abnormality-report-seq',
      kind: 'seq',
      name: t(
        '異常{いじょう}・ミス発生時{はっせいじ}の報告手順{ほうこくてじゅん}',
        'Abnormality & Incident Reporting Sequence',
      ),
      goal: t(
        'トラブルが起{お}きたときの報連相{ほうれんそう}の手順{てじゅん}を身{み}につける。',
        'Master the sequence of reporting when trouble occurs.',
      ),
      steps: [
        t(
          '作業{さぎょう}を直{ただ}ちに止{と}めて機械{きかい}の停止{ていし}ボタンを押{お}す',
          'Immediately stop work and press the machine stop button',
        ),
        t(
          '自分{じぶん}と同僚{どうりょう}の安全{あんぜん}を確認{かくにん}して二次災害{にじさいがい}を防{ふせ}ぐ',
          'Confirm safety of yourself and coworkers to prevent secondary disasters',
        ),
        t(
          '直{ただ}ちに職長{しょくちょう}のところへ行{い}き「報告{ほうこく}があります」と伝{つた}える',
          'Go immediately to the foreman and say "I have a report"',
        ),
        t(
          '言{い}い訳{わけ}をせず何{なに}が起{お}きたか事実{じじつ}を簡潔{かんけつ}に説明{せつめい}する',
          'Briefly explain the facts of what happened without making excuses',
        ),
        t(
          '職長{しょくちょう}の指示{しじ}を聞{き}いて声{こえ}に出{だ}して復唱{ふくしょう}する',
          'Listen to foreman instructions and repeat them aloud to confirm',
        ),
      ],
    },
    {
      id: 'emergency-call-reflex',
      kind: 'reflex',
      name: t(
        '緊急時{きんきゅうじ}の「危{あぶ}ない！」反射{はんしゃ}',
        'Emergency Danger Reaction Reflex',
      ),
      goal: t(
        '危険{きけん}な兆候{ちょうこう}を発見{はっけん}！0.4秒以内{びょういない}に「危{あぶ}ない！」と声{こえ}を上{あ}げて非常停止{ひじょうていし}を押{お}す。',
        'Danger sign spotted! Call out "Abunai!" and hit emergency stop within 0.4 s.',
      ),
    },
  ],
  inspect: [
    {
      id: 'emergency-contact-card',
      name: t(
        '携帯用{けいたいよう}緊急連絡{きんきゅうれんらく}カード',
        'Personal Emergency Contact Card',
      ),
      defective: false,
      spots: [
        {
          x: 30,
          y: 35,
          check: t(
            '工場{こうじょう}の正式住所{せいしきじゅうしょ}と電話番号{でんわばんごう}が鮮明{せんめい}に読{よ}み取{と}れること',
            'Factory official address and phone number are clearly legible',
          ),
        },
        {
          x: 70,
          y: 65,
          check: t(
            '現場{げんば}の職長{しょくちょう}の氏名{しめい}と緊急連絡先{きんきゅうれんらくさき}が正{ただ}しく記入{きにゅう}されていること',
            'Site foreman name and emergency contact number are correctly entered',
          ),
        },
      ],
    },
    {
      id: 'emergency-phone-board',
      name: t(
        '工場内{こうじょうない}非常電話{ひじょうでんわ}・通報{つうほう}板{ばん}',
        'Site Emergency Call Station Board',
      ),
      defective: true,
      spots: [
        {
          x: 25,
          y: 30,
          check: t(
            '119番{ばん}（消防{しょうぼう}・救急{きゅうきゅう}）と110番{ばん}（警察{けいさつ}）の表示{ひょうじ}がかすれず読{よ}めること（読めないものは不良）',
            '119 (fire/ambulance) and 110 (police) signs are legible; illegible is defective',
          ),
        },
        {
          x: 75,
          y: 70,
          check: t(
            '受話器{じゅわき}のコードに断線{だんせん}がなく通話{つうわ}テストで発信音{はっしんおん}が鳴{な}ること',
            'Handset cord has no breaks and dial tone sounds during test',
          ),
        },
      ],
    },
    {
      id: 'safety-handbook-pocket',
      name: t(
        '安全{あんぜん}ポケット手帳{てちょう}・指差呼称{しさこしょう}票{ひょう}',
        'Pocket Safety Handbook & Pointing Sheet',
      ),
      defective: false,
      spots: [
        {
          x: 35,
          y: 40,
          check: t(
            '報連相{ほうれんそう}の手順{てじゅん}と緊急連絡網{きんきゅうれんらくもう}のページが破{やぶ}れず揃{そろ}っていること',
            'Hou-Ren-So procedure and emergency tree pages are complete with no tears',
          ),
        },
        {
          x: 65,
          y: 60,
          check: t(
            '救急時{きゅうきゅうじ}の伝達{でんたつ}フレーズが母国語{ぼこくご}（ベンガル語{ご}・英語{えいご}）で併記{へいき}されていること',
            'Emergency phrases are written alongside in native languages (Bengali / English)',
          ),
        },
      ],
    },
    {
      id: 'first-aid-station',
      name: t(
        '現場{げんば}救急箱{きゅうきゅうばこ}・応急手当{おうきゅうてあて}セット',
        'Site First Aid Kit Box',
      ),
      defective: true,
      spots: [
        {
          x: 30,
          y: 30,
          check: t(
            '消毒液{しょうどくえき}・清潔{せいけつ}なガーゼ・絆創膏{ばんそうこう}が十分{じゅうぶん}に補充{ほじゅう}されていること（中身が空は不良）',
            'Disinfectant, clean gauze, and bandages are well stocked; empty is defective',
          ),
        },
        {
          x: 70,
          y: 75,
          check: t(
            '救急箱{きゅうきゅうばこ}の外箱{そとばこ}に破損{はそん}がなく、ふたが密閉{みっぺい}できること',
            'Outer case has no cracks and lid seals tightly against moisture and dust',
          ),
        },
      ],
    },
  ],
  quiz: [
    {
      q: t(
        '職長{しょくちょう}の早口{はやくち}の日本語{にほんご}が聞{き}き取{と}れず、指示{しじ}の内容{ないよう}がよく分{わ}かりませんでした。どう行動{こうどう}すべきですか？',
        'The foreman spoke fast in Japanese and you could not catch the instructions. What should you do?',
      ),
      options: [
        t(
          '「すみません、もう一度{いちど}ゆっくりお願{ねが}いします」と聞{き}き返{かえ}す',
          'Ask "Excuse me, please say that once more slowly"',
        ),
        t(
          '怒{おこ}られないように「はい、分{わ}かりました」と答{こた}えて作業{さぎょう}を始{はじ}める',
          'Answer "Yes, understood" to avoid being scolded and start working',
        ),
        t(
          '自分{じぶん}の判断{はんだん}で適当{てきとう}に手順{てじゅん}を予想{よそう}して作業{さぎょう}する',
          'Guess the procedure on your own and proceed with work',
        ),
        t(
          '誰{だれ}にも聞{き}き返{かえ}さず、他{ほか}の作業員{さぎょういん}の様子{ようす}を黙{だま}って観察{かんさつ}し続{つづ}ける',
          'Ask nobody and just keep quietly watching what other workers do',
        ),
      ],
      answer: 0,
      why: t(
        '分{わ}からないままうなずいて作業{さぎょう}に入{はい}るのは重大事故{じゅうだいじこ}の最大{さいだい}の原因{げんいん}です。何度{なんど}でも「もう一度{いちど}お願{ねが}いします」と聞{き}き直{なお}すのが正{ただ}しい安全行動{あんぜんこうどう}です。',
        'Working without understanding is the greatest cause of serious accidents. Asking "Please repeat" as many times as needed is the right safety practice.',
      ),
    },
    {
      q: t(
        '作業中{さぎょうちゅう}に足場{あしば}から同僚{どうりょう}が転落{てんらく}し、床{ゆか}で血{ち}を流{なが}して倒{たお}れています。最初{さいしょ}に叫{さけ}ぶべき言葉{ことば}はどれですか？',
        'During work, a coworker fell from a scaffold and is bleeding on the floor. What phrase should you shout first?',
      ),
      options: [
        t('「私{わたし}は悪{わる}くありません！」', '"It is not my fault!"'),
        t(
          '「助{たす}けてください！救急車{きゅうきゅうしゃ}をお願{ねが}いします！」',
          '"Please help! Please call an ambulance!"',
        ),
        t(
          '「今日{きょう}の作業{さぎょう}は終{お}わりですか？」',
          '"Is today\'s work finished?"',
        ),
        t(
          '「写真{しゃしん}を撮{と}ってください！」',
          '"Please take a photograph!"',
        ),
      ],
      answer: 1,
      why: t(
        '人命{じんめい}に関{かか}わる緊急時{きんきゅうじ}は、直{ただ}ちに「助{たす}けてください！」と叫{さけ}んで周囲{しゅうい}に非常事態{ひじょうじたい}を伝{つた}え、救急車{きゅうきゅうしゃ}を呼{よ}びます。',
        'In life-threatening emergencies, immediately shout "Help!" to alert everyone of the crisis and summon an ambulance.',
      ),
    },
    {
      q: t(
        '火災{かさい}や急病{きゅうびょう}で「119番{ばん}」に電話{でんわ}をかけました。指令員{しれいいん}が出{で}た最初{さいしょ}に聞{き}かれる質問{しつもん}は何{なん}ですか？',
        'You dialed 119 for a fire or medical emergency. What is the first question the operator asks you?',
      ),
      options: [
        t(
          '「今日{きょう}の天気{てんき}はどうですか」',
          '"How is the weather today?"',
        ),
        t(
          '「あなたの国籍{こくせき}はどこですか」',
          '"What is your nationality?"',
        ),
        t(
          '「火事{かじ}ですか、救急{きゅうきゅう}ですか」',
          '"Is it a fire, or is it an ambulance (medical)?"',
        ),
        t(
          '「会社{かいしゃ}の社長{しゃちょう}の名前{なまえ}は何{なん}ですか」',
          '"What is the name of your company president?"',
        ),
      ],
      answer: 2,
      why: t(
        '119番{ばん}は消防署{しょうぼうしょ}につながり、最初{さいしょ}に「火事{かじ}ですか、救急{きゅうきゅう}ですか」と確認{かくにん}されます。落{お}ち着{つ}いて「救急{きゅうきゅう}です」または「火事{かじ}です」と答{こた}えます。',
        '119 connects to the fire dispatch, which first asks "Fire or ambulance?" Stay calm and answer either "Ambulance" or "Fire."',
      ),
    },
    {
      q: t(
        '使用中{しようちゅう}の工作機械{こうさくきかい}から激{はげ}しい異音{いおん}と焦{こ}げ臭{くさ}いにおいが発生{はっせい}しました。正{ただ}しい対応{たいおう}はどれですか？',
        'A machine you are operating suddenly makes a violent abnormal screech and emits a burning smell. What is the correct action?',
      ),
      options: [
        t(
          '臭{にお}いが消{き}えるまで我慢{がまん}してそのまま加工作業{かこうさぎょう}を続{つづ}ける',
          'Endure until the smell disappears and keep machining',
        ),
        t(
          '同僚{どうりょう}に気{き}づかれないよう、機械{きかい}の速度{そくど}を最高{さいこう}にして急{いそ}ぐ',
          'Speed up the machine to maximum so coworkers do not notice',
        ),
        t(
          '機械{きかい}のスイッチを入{い}れたまま、手{て}を伸{の}ばして中{なか}を覗{のぞ}き込{こ}む',
          'Reach in and peer inside while leaving the machine power switch on',
        ),
        t(
          '直{ただ}ちに停止{ていし}ボタンを押{お}して機械{きかい}を止{と}め、職長{しょくちょう}に「異音{いおん}がします」と報告{ほうこく}する',
          'Immediately press stop button to halt machine and report "Abnormal noise" to foreman',
        ),
      ],
      answer: 3,
      why: t(
        '機械{きかい}の異常{いじょう}や異音{いおん}・異臭{いしゅう}は重大事故{じゅうだいじこ}や火災{かさい}の前兆{ぜんちょう}です。直{ただ}ちに停止{ていし}させて職長{しょくちょう}へ報告{ほうこく}します。',
        'Abnormal sounds and burning smells are warning signs of serious damage or fire. Stop the machine immediately and report to your foreman.',
      ),
    },
    {
      q: t(
        '日本{にほん}の現場{げんば}の基本{きほん}「報連相{ほうれんそう}」の意味{いみ}として正{ただ}しい組合{くみあ}わせはどれですか？',
        'What is the correct combination of meanings for "Hou-Ren-So" in Japanese workplaces?',
      ),
      options: [
        t(
          '報告{ほうこく}（結果{けっか}を伝{つた}える）・連絡{れんらく}（情報{じょうほう}を共有{きょうゆう}する）・相談{そうだん}（迷{まよ}ったら意見{いけん}を聞{き}く）',
          'Houkoku (report results), Renraku (share information), Soudan (consult when unsure)',
        ),
        t(
          '放置{ほうち}（そのままにする）・練習{れんしゅう}（訓練{くんれん}する）・早退{そうたい}（早{はや}く帰{かえ}る）',
          'Houchi (leave alone), Renshuu (practice), Soutai (leave early)',
        ),
        t(
          '放任{ほうにん}（何{なに}もしない）・連続{れんぞく}（休{やす}まず働{はたら}く）・掃除{そうじ}（床{ゆか}を拭{ふ}く）',
          'Hounin (do nothing), Renzoku (work without break), Souji (sweep floor)',
        ),
        t(
          '褒美{ほうび}（ご褒美{ほうび}をもらう）・連休{れんきゅう}（長{なが}く休{やす}む）・早起{はやお}き（朝早{あさはや}く起{お}きる）',
          'Houbi (get rewards), Renkyuu (long holiday), Hayaoki (wake up early)',
        ),
      ],
      answer: 0,
      why: t(
        '報連相{ほうれんそう}は「報告{ほうこく}」「連絡{れんらく}」「相談{そうだん}」の略{りゃく}です。業務{ぎょうむ}の結果{けっか}を伝{つた}え、情報{じょうほう}を共有{きょうゆう}し、疑問{ぎもん}や不安{ふあん}を相談{そうだん}します。',
        'Hou-Ren-So stands for Houkoku (Report), Renraku (Communicate), and Soudan (Consult). It ensures clear results, shared facts, and mutual advice.',
      ),
    },
    {
      q: t(
        '作業中{さぎょうちゅう}に手元{てもと}が狂{くる}い、工具{こうぐ}を通路{つうろ}に落{お}としてしまいました。幸{さいわ}い誰{だれ}にも当{あ}たらず怪我{けが}人{にん}はいません。どうすべきですか？',
        'During work, you slipped and dropped a tool into a walkway. Fortunately nobody was struck and no one was hurt. What should you do?',
      ),
      options: [
        t(
          '誰{だれ}にも見{み}られていないので、何{なに}も言{い}わずに工具{こうぐ}を拾{ひろ}って隠{かく}す',
          'Nobody saw it, so silently pick it up and hide the incident',
        ),
        t(
          '「工具{こうぐ}を落{お}としてしまいました」と直{ただ}ちに職長{しょくちょう}に報告{ほうこく}する（ヒヤリ・ハット）',
          'Immediately report "I dropped a tool" to the foreman (near-miss report)',
        ),
        t(
          '落{お}とした工具{こうぐ}を通路{つうろ}に置{お}いたまま作業{さぎょう}を続{つづ}ける',
          'Leave the dropped tool on the walkway and keep working',
        ),
        t(
          '同僚{どうりょう}のせいにして自分{じぶん}の責任{せきにん}ではないと嘘{うそ}をつく',
          'Blame a coworker and falsely claim it was not your fault',
        ),
      ],
      answer: 1,
      why: t(
        'ケガがなくても、落下{らっか}は人{ひと}に直撃{ちょくげき}すれば死亡{しぼう}事故{じこ}になり得{う}る重大{じゅうだい}なヒヤリ・ハットです。原因{げんいん}を共有{きょうゆう}して再発{さいはつ}を防{ふせ}ぐため、必{かなら}ず報告{ほうこく}します。',
        'Even if no one was hurt, falling objects can cause fatal accidents. You must report near-misses immediately so the site can prevent recurrence.',
      ),
    },
    {
      q: t(
        '夏{なつ}の午後{ごご}の作業前{さぎょうまえ}、めまいと頭痛{ずつう}がして吐{は}き気{け}があります。どう行動{こうどう}すべきですか？',
        'Before starting work on a hot summer afternoon, you feel dizzy, have a headache and feel nauseous. How should you act?',
      ),
      options: [
        t(
          '「休{やす}むと迷惑{めいわく}がかかる」と思{おも}って無理{むり}をして作業{さぎょう}を続{つづ}ける',
          'Think "taking a break causes trouble" and force yourself to keep working',
        ),
        t(
          '誰{だれ}にも言{い}わずに現場{げんば}の物陰{ものかげ}に隠{かく}れて眠{ねむ}る',
          'Hide in the shadows on site and go to sleep without telling anyone',
        ),
        t(
          '我慢{がまん}せず、作業前{さぎょうまえ}に職長{しょくちょう}に「気分{きぶん}が悪{わる}いです」と相談{そうだん}する',
          'Do not endure it; consult the foreman before work: "Kibun ga warui desu"',
        ),
        t(
          '冷{つめ}たい水{みず}を大量{たいりょう}に一気{いっき}飲{の}みして、走{はし}って高所{こうしょ}足場{あしば}に登{のぼ}る',
          'Gulp a huge amount of cold water at once and run up the scaffold',
        ),
      ],
      answer: 2,
      why: t(
        '熱中症{ねっちゅうしょう}や体調不良{たいちょうふりょう}を我慢{がまん}して作業{さぎょう}に入{はい}ると、意識{いしき}を失{うしな}って墜落{ついらく}するなどの命{いのち}に関{かか}わる事故{じこ}につながります。事前{じぜん}の相談{そうだん}が原則{げんそく}です。',
        'Enduring heatstroke symptoms or illness leads to loss of consciousness and fatal falls. Consulting your foreman beforehand is a mandatory rule.',
      ),
    },
    {
      q: t(
        '工場{こうじょう}の敷地内{しきちない}で盗難{とうなん}や不審者{ふしんしゃ}の侵入{しんにゅう}、または道路{どうろ}での交通事故{こうつうじこ}が起{お}きました。警察{けいさつ}を呼{よ}ぶ緊急番号{きんきゅうばんごう}はどれですか？',
        'A theft, an intruder, or a traffic accident occurred near the factory gates. Which emergency telephone number calls the police?',
      ),
      options: [
        t('119番{ばん}', '119'),
        t('117番{ばん}', '117'),
        t('104番{ばん}', '104'),
        t('110番{ばん}', '110'),
      ],
      answer: 3,
      why: t(
        '日本{にほん}で警察{けいさつ}（事件{じけん}・事故{じこ}・盗難{とうなん}）に通報{つうほう}する番号{ばんごう}は「110番{ばん}」です。消防{しょうぼう}・救急{きゅうきゅう}は「119番{ばん}」です。混同{こんどう}しないよう覚{おぼ}えましょう。',
        'In Japan, the emergency telephone number for police (crimes, accidents, theft) is 110. Fire and ambulance is 119. Never confuse the two.',
      ),
    },
    {
      q: t(
        '朝礼{ちょうれい}で新{あたら}しい安全装置{あんぜんそうち}の操作方法{そうさほうほう}が説明{せつめい}されましたが、専門用語{せんもんようご}が多{おお}くて理解{りかい}できませんでした。どうすべきですか？',
        'A new safety device procedure was explained at the morning meeting, but you could not understand it due to technical jargon. What should you do?',
      ),
      options: [
        t(
          '「分{わ}かりません。通訳{つうやく}をお願{ねが}いします」と職長{しょくちょう}に申{もう}し出{で}る',
          'Tell the foreman: "I do not understand. Please get an interpreter"',
        ),
        t(
          '自分{じぶん}だけ分{わ}からないのが恥{は}ずかしいので、他{ほか}の人{ひと}に合{あ}わせてうなずく',
          'Feel embarrassed to be the only one who does not know, so nod along with others',
        ),
        t(
          '説明書{せつめいしょ}を読{よ}まずに、スイッチを適当{てきとう}に押{お}して機械{きかい}を動{うご}かしてみる',
          'Without reading the manual, press buttons randomly to test the machine',
        ),
        t(
          '安全装置{あんぜんそうち}の配線{はいせん}を勝手{かって}に切断{せつだん}して無効化{むこうか}する',
          'Cut the safety device wiring without permission to disable it',
        ),
      ],
      answer: 0,
      why: t(
        '安全{あんぜん}に関{かか}わる手順{てじゅん}は完璧{かんぺき}に理解{りかい}していなければ命{いのち}に関{かか}わります。「分{わ}かりません」「通訳{つうやく}をお願{ねが}いします」と伝{つた}えることは正当{せいとう}な権利{けんり}です。',
        'Safety procedures must be fully understood to avoid fatal risks. Saying "I do not understand" or asking for an interpreter is your proper right.',
      ),
    },
    {
      q: t(
        '職長{しょくちょう}に仕事{しごと}の「報告{ほうこく}」をするとき、話{はな}し方{かた}の順序{じゅんじょ}として最{もっと}も適切{てきせつ}なものはどれですか？',
        'When delivering a work report (Houkoku) to your foreman, what is the best order of speaking?',
      ),
      options: [
        t(
          '自分{じぶん}の苦労{くろう}や言{い}い訳{わけ}を長{なが}く話{はな}したあとで、結論{けつろん}を濁{にご}す',
          'Talk at length about personal struggles and excuses, then leave the conclusion vague',
        ),
        t(
          'まず「結論{けつろん}と結果{けっか}（終{お}わったかどうか・起{お}きた事実{じじつ}）」を言{い}い、次{つぎ}に「理由{りゆう}や詳細{しょうさい}」を伝{つた}える',
          'State "Conclusion and results (finished or not, facts)" first, then give reasons and details',
        ),
        t(
          '職長{しょくちょう}が質問{しつもん}してくるまで、何{なに}も話{はな}さず作業{さぎょう}を終{お}わらせる',
          'Say nothing and finish work until the foreman comes over to interrogate you',
        ),
        t(
          '事実{じじつ}と異{こと}なる嘘{うそ}の結果{けっか}を伝{つた}えて、怒{おこ}られないようにする',
          'Report false results contrary to facts so that you do not get scolded',
        ),
      ],
      answer: 1,
      why: t(
        '報告{ほうこく}は「結論{けつろん}ファースト（結論{けつろん}から先{さき}に話{はな}す）」が基本{きほん}です。まず結果{けっか}や起{お}きた事実{じじつ}を伝{つた}えることで、迅速{じんそく}な判断{はんだん}ができます。',
        'Reports should always follow "Conclusion First." Stating results and facts first enables supervisors to make swift, accurate decisions.',
      ),
    },
    {
      q: t(
        '工場{こうじょう}の電気{でんき}設備{せつび}の近{ちか}くで白煙{はくえん}が上{あ}がり、小{ちい}さな炎{ほのお}が見{み}えました。正{ただ}しい初動{しょどう}対応{たいおう}はどれですか？',
        'White smoke rose near an electrical cabinet on site and small flames were visible. What is the correct initial action?',
      ),
      options: [
        t(
          '誰{だれ}にも知{し}らせず、自分{じぶん}の飲{の}み物{もの}（お茶{ちゃ}）を電気{でんき}盤{ばん}に浴{あ}びせかける',
          'Tell nobody and splash your drinking tea onto the electrical panel',
        ),
        t(
          '煙{けむり}を写真{しゃしん}に撮{と}ってSNSに投稿{とうこう}する',
          'Take photos of the smoke and post them to social media',
        ),
        t(
          '大声{おおごえ}で「火事{かじ}だ！」と叫{さけ}んで周囲{しゅうい}に知{し}らせ、初期消火{しょきしょうか}と通報{つうほう}を行{おこな}う',
          'Shout "Kaji da!" (Fire!) loudly to alert everyone, begin initial firefighting, and report',
        ),
        t(
          '小{ちい}さな火{ひ}だから大丈夫{だいじょうぶ}と思{おも}ってそのまま放置{ほうち}する',
          'Assume it will be fine since the flame is small and leave it alone',
        ),
      ],
      answer: 2,
      why: t(
        '火災{かさい}を発見{はっけん}したら、まず大声{おおごえ}で「火事{かじ}だ！」と周囲{しゅうい}に知{し}らせることが最優先{さいゆうせん}です。電気{でんき}火災{かさい}への水{みず}かけは感電{かんでん}の危険{きけん}があるため絶対{ぜったい}にしてはなりません。',
        'When discovering a fire, immediately shout "Fire!" to notify the entire floor. Never throw water on electrical fires due to electrocution danger.',
      ),
    },
    {
      q: t(
        '同僚{どうりょう}から「『分{わ}かりません』と何回{なんかい}も言{い}うと職長{しょくちょう}に嫌{きら}われるから、適当{てきとう}にうなずいておけ」と言{い}われました。どう考{かんが}えるべきですか？',
        "A coworker tells you: \"If you say 'I don't understand' repeatedly the foreman will dislike you, so just nod.\" How should you view this?",
      ),
      options: [
        t(
          '同僚{どうりょう}の言{い}う通{とお}りに、全{すべ}て「はい」とうなずいて分{わ}かったふりをする',
          'Follow the coworkers advice and pretend to understand by nodding "Yes" to everything',
        ),
        t(
          '怒{おこ}られるのを避{さ}けるため、返事{へんじ}をせず黙{だま}り込{こ}んで無視{むし}する',
          'Stay completely silent and ignore everyone to avoid getting scolded',
        ),
        t(
          '安全{あんぜん}教育{きょういく}は意味{いみ}がないので、次{つぎ}の休憩{きゅうけい}まで隠{かく}れてサボる',
          'Think safety training is pointless and hide until the next break to slack off',
        ),
        t(
          '同僚{どうりょう}の助言{じょげん}は間違{まちが}いであり、分{わ}からないまま作業{さぎょう}して命{いのち}を落{お}とす危険{きけん}を避{さ}けるため必{かなら}ず質問{しつもん}する',
          'The coworkers advice is wrong; always ask questions to avoid fatal risks of working without comprehension',
        ),
      ],
      answer: 3,
      why: t(
        '日本{にほん}の現場{げんば}では、分{わ}からないのに「はい」と答{こた}えることが最{もっと}も危険{きけん}で嫌{きら}われます。安全{あんぜん}のため、理解{りかい}できるまで聞{き}き直{なお}す作業員{さぎょういん}こそが信頼{しんらい}されます。',
        'In Japanese workplaces, pretending to understand by saying "Yes" is the most dangerous and disliked habit. Workers who clarify until they understand are trusted.',
      ),
    },
  ],
  glossary: [
    {
      ja: '報告',
      reading: 'ほうこく',
      en: 'Report / Reporting results',
    },
    {
      ja: '連絡',
      reading: 'れんらく',
      en: 'Notice / Information sharing',
    },
    {
      ja: '相談',
      reading: 'そうだん',
      en: 'Consultation / Asking advice',
    },
    {
      ja: '報連相',
      reading: 'ほうれんそう',
      en: 'Hou-Ren-So (Report-Contact-Consult)',
    },
    {
      ja: '救急車',
      reading: 'きゅうきゅうしゃ',
      en: 'Ambulance',
    },
    {
      ja: '消防署',
      reading: 'しょうぼうしょ',
      en: 'Fire station (dial 119)',
    },
    {
      ja: '警察',
      reading: 'けいさつ',
      en: 'Police (dial 110)',
    },
    {
      ja: '非常停止',
      reading: 'ひじょうていし',
      en: 'Emergency stop',
    },
    {
      ja: '異音',
      reading: 'いおん',
      en: 'Abnormal noise',
    },
    {
      ja: '通訳',
      reading: 'つうやく',
      en: 'Interpreter / Interpretation',
    },
    {
      ja: '怪我',
      reading: 'けが',
      en: 'Injury / Wound',
    },
    {
      ja: '気分',
      reading: 'きぶん',
      en: 'Health state / Physical feeling',
    },
  ],
}
