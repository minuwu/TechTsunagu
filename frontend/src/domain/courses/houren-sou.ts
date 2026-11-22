import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string, bn: string): Tri => ({ ja, en, bn })

export const hourenSou: CourseData = {
  id: 'houren-sou',
  category: 'language',
  level: 1,
  minutes: 15,
  title: t(
    '報連相{ほうれんそう}と緊急{きんきゅう}フレーズ',
    'Reporting & Emergency Phrases (Hou-Ren-So)',
    'রিপোর্টিং ও জরুরি বাক্য (হো-রেন-সো)',
  ),
  sub: t(
    '現場{げんば}で伝{つた}える、命{いのち}を守{まも}る、迷{まよ}ったら聞{き}き返{かえ}す。',
    'Report on site, protect lives, and never hesitate to ask again.',
    'সাইটে সঠিক তথ্য জানানো, জীবন বাঁচানো ও না বুঝলে আবার জিজ্ঞেস করা।',
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
        'সাইটে "হো-রেন-সো" (রিপোর্ট, যোগাযোগ, পরামর্শ) সবচেয়ে জরুরি। কোনো সমস্যা হলে সাথে সাথে আমাকে জানাবে।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'はい、分{わ}かりました！今日{きょう}の作業{さぎょう}手順{てじゅん}はだいたい分{わ}かりました。',
        'Yes, understood! I mostly understand today\'s work procedure.',
        'হ্যাঁ, বুঝেছি! আজকের কাজের নিয়ম মোটামুটি বুঝতে পেরেছি।',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '「だいたい」は危{あぶ}ないよ。早口{はやくち}で聞{き}き取{と}れなかったときは、何{なん}と言{い}いますか？',
        '"Mostly" is dangerous. If I speak too fast and you cannot catch it, what do you say?',
        '"মোটামুটি" বলা ঝুঁকিপূর্ণ। আমি দ্রুত কথা বললে যদি বুঝতে না পারো, তবে কী বলবে?',
      ),
    },
    {
      who: 'worker',
      text: t(
        '怒{おこ}られるのが怖{こわ}くて、分{わ}からなくても「はい」とうなずいてしまいました…',
        'I was afraid of being scolded, so I just nodded "Yes" even though I did not understand...',
        'বকা খাওয়ার ভয়ে না বুঝেও "হ্যাঁ" বলে মাথা নেড়ে ফেলেছিলাম...',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '分{わ}からないまま作業{さぎょう}するのが一番{いちばん}危険{きけん}です。「もう一度{いちど}お願{ねが}いします」「ゆっくり話{はな}してください」と聞{き}き返{かえ}していいんだよ。',
        'Working without understanding is the most dangerous thing. It is always right to ask: "Please say that again" or "Please speak slowly."',
        'না বুঝে কাজ চালিয়ে যাওয়াই সবচেয়ে বিপজ্জনক। "দয়া করে আরেকবার বলুন" বা "আস্তে কথা বলুন" বলে নির্দ্বিধায় আবার জিজ্ঞেস করবে।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '安心{あんしん}しました！では、機械{きかい}が変{へん}な音{おと}を立{た}てたり故障{こしょう}したときは、どう伝{つた}えますか？',
        'That is a relief! So if a machine makes strange noises or breaks down, how should I report it?',
        'আশ্বস্ত হলাম! যদি মেশিন থেকে অদ্ভুত আওয়াজ বের হয় বা নষ্ট হয়, তবে কীভাবে জানাব?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'まず機械{きかい}を止{と}めて「異音{いおん}がします」と結果{けっか}から報告{ほうこく}してください。言{い}い訳{わけ}をせず、事実{じじつ}をすぐ伝{つた}えるのが基本{きほん}です。',
        'First stop the machine and report the result: "There is an abnormal noise." Stating facts first without excuses is the rule.',
        'প্রথমে মেশিন থামাবে এবং মূল কথা আগে বলবে: "অস্বাভাবিক শব্দ হচ্ছে।" কোনো অজুহাত না দিয়ে সরাসরি সত্য ঘটনা জানানোই নিয়ম।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'もし現場{げんば}で同僚{どうりょう}が大怪我{おおけが}をしたり倒{たお}れたら、どう叫{さけ}べばいいですか？',
        'If a coworker gets badly injured or collapses on site, what should I shout?',
        'যদি সাইটে কোনো সহকর্মী মারাত্মক আহত হয় বা পড়ে যায়, তবে কীভাবে চিৎকার করে ডাকব?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '大声{おおごえ}で「助{たす}けてください！」「救急車{きゅうきゅうしゃ}をお願{ねが}いします！」と叫{さけ}び、119番{ばん}通報{つうほう}します。',
        'Shout loudly: "Tasukete kudasai!" (Help!) and "Kyukyusha o onegaishimasu!" (Call an ambulance!), then call 119.',
        'জোরে চিৎকার করে বলবে "তাসুকেতে কুদাসাই!" (বাঁচান!) ও "কিউকিউশা ও ওনেগাইশিমাসু!" (অ্যাম্বুলেন্স ডাকুন!), তারপর ১১৯ এ ফোন করবে।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '「助{たす}けてください」「もう一度{いちど}お願{ねが}いします」を覚{おぼ}えて、安全{あんぜん}に報連相{ほうれんそう}を徹底{てってい}します！',
        'I will remember "Please help" and "Please repeat," and practice thorough Hou-Ren-So safely!',
        '"বাঁচান" এবং "আরেকবার বলুন" মনে রাখব, এবং নিরাপদে সর্বদা হো-রেন-সো মেনে চলব!',
      ),
    },
  ],
  handbook: [
    t(
      '指示{しじ}が分{わ}からないときは、うなずかずに「もう一度{いちど}お願{ねが}いします」と聞{き}き返{かえ}す。',
      'When you do not understand instructions, never just nod; ask "Please say that again."',
      'নির্দেশ বুঝতে না পারলে শুধু মাথা না নেড়ে "দয়া করে আরেকবার বলুন" বলবে।',
    ),
    t(
      '日本語{にほんご}が早{はや}くて聞{き}き取{と}れないときは、「ゆっくり話{はな}してください」と頼{たの}む。',
      'If Japanese is spoken too fast to catch, request "Please speak slowly."',
      'জাপানি ভাষা বেশি দ্রুত মনে হলে "দয়া করে আস্তে কথা বলুন" অনুরোধ করবে।',
    ),
    t(
      '報告{ほうこく}するときは、言{い}い訳{わけ}を言{い}わずに「結論{けつろん}と事実{じじつ}」を最初{さいしょ}に伝{つた}える。',
      'When giving a report, state the conclusion and facts first without making excuses.',
      'রিপোর্ট করার সময় অজুহাত না দিয়ে প্রথমে "সিদ্ধান্ত ও আসল ঘটনা" বলবে।',
    ),
    t(
      '小{ちい}さなミスやヒヤリ・ハットでも、隠{かく}さずに直{ただ}ちに職長{しょくちょう}へ報告{ほうこく}する。',
      'Report even small mistakes or near-misses immediately to your foreman without hiding them.',
      'ছোটখাটো ভুল বা অল্পের জন্য দুর্ঘটনার ঘটনাও গোপন না করে সাথে সাথে ফোরম্যানকে জানাবে।',
    ),
    t(
      '危険{きけん}な状態{じょうたい}を見{み}つけたら、作業{さぎょう}を止{と}めて大声{おおごえ}で「危{あぶ}ない！」と周囲{しゅうい}に知{し}らせる。',
      'If you spot a dangerous condition, halt work and shout "Abunai!" (Watch out!) to alert those around you.',
      'বিপজ্জনক অবস্থা দেখলে কাজ বন্ধ করে চিৎকার করে "আবুনাই!" (বিপদ!) বলে সবাইকে সতর্ক করবে।',
    ),
    t(
      '事故{じこ}や怪我{けが}人{にん}を発見{はっけん}したときは、大声{おおごえ}で「助{たす}けてください！」と叫{さけ}んで人{ひと}を集{あつ}める。',
      'If you find an accident or injured person, shout "Tasukete kudasai!" (Help!) loudly to gather help.',
      'দুর্ঘটনা বা কাউকে আহত দেখলে জোরে "তাসুকেতে কুদাসাই!" (বাঁচান!) বলে লোক জড়ো করবে।',
    ),
    t(
      '体調{たいちょう}が良{よ}くないときは我慢{がまん}せず、作業前{さぎょうまえ}に「気分{きぶん}が悪{わる}いです」と相談{そうだん}する。',
      'If feeling unwell, never endure it silently; consult your foreman before work: "Kibun ga warui desu."',
      'শরীর খারাপ লাগলে চেপে রাখবে না, কাজ শুরুর আগেই "কিবুন গা ওয়ারুই দেসু" (শরীর খারাপ লাগছে) বলে জানাবে।',
    ),
    t(
      '火災{かさい}や救急{きゅうきゅう}の119番{ばん}通報{つうほう}では、最初{さいしょ}に「火事{かじ}」か「救急{きゅうきゅう}」かをはっきり言{い}う。',
      'When calling 119 for fire or medical emergency, state clearly first whether it is "Fire" or "Ambulance."',
      'আগুন বা চিকিৎসার জন্য ১১৯ এ ফোন করলে শুরুতেই পরিষ্কার করে বলবে "আগুন" নাকি "অ্যাম্বুলেন্স"।',
    ),
    t(
      '事故{じこ}や犯罪{はんざい}・盗難{とうなん}の警察通報{けいさつつうほう}は「110番{ばん}」に電話{でんわ}する。',
      'Call 110 for police reporting of accidents, crimes, or theft.',
      'দুর্ঘটনা, অপরাধ বা চুরির ক্ষেত্রে পুলিশকে জানাতে "১১০" নম্বরে ফোন করবে।',
    ),
    t(
      '安全{あんぜん}に関{かか}わる大切{たいせつ}な説明{せつめい}で理解{りかい}できないときは、「通訳{つうやく}をお願{ねが}いします」と伝{つた}える。',
      'If you cannot understand vital safety instructions, say "Tsuyaku o onegaishimasu" (Please get an interpreter).',
      'নিরাপত্তা সংক্রান্ত জরুরি নির্দেশ বুঝতে অসুবিধা হলে "সুয়াকু ও ওনেগাইশিমাসু" (দোভাষী ডেকে দিন) বলবে।',
    ),
  ],
  reel: [
    t(
      'もう一度{いちど}お願{ねが}いします！',
      'Please say that again!',
      'দয়া করে আরেকবার বলুন!',
    ),
    t(
      'ゆっくり話{はな}してください！',
      'Please speak slowly!',
      'দয়া করে আস্তে কথা বলুন!',
    ),
    t(
      '助{たす}けてください！',
      'Please help me!',
      'আমাকে বাঁচান / সাহায্য করুন!',
    ),
    t(
      '危{あぶ}ない、ストップ！',
      'Watch out, stop!',
      'বিপদ, থামো!',
    ),
    t(
      '火事{かじ}です、119番{ばん}！',
      'Fire! Call 119!',
      'আগুন লেগেছে, ১১৯ এ ফোন করো!',
    ),
    t(
      '救急車{きゅうきゅうしゃ}をお願{ねが}いします！',
      'Please call an ambulance!',
      'অ্যাম্বুলেন্স ডাকুন!',
    ),
    t(
      '報告{ほうこく}・連絡{れんらく}・相談{そうだん}、ヨシ！',
      'Report, communicate, consult, yoshi!',
      'রিপোর্ট, যোগাযোগ, পরামর্শ, ঠিক আছে!',
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
        'যোগাযোগের ঝুঁকি চেনা (KYT)',
      ),
      goal: t(
        '危険{きけん}な受{う}け答{こた}えや行動{こうどう}を4つ見{み}つける。',
        'Find 4 dangerous communication hazards or actions.',
        '৪টি বিপজ্জনক কথাবার্তা বা আচরণ খুঁজে বের করো।',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '分{わ}からないのに「はい」とうなずく',
            'Nodding "Yes" when not understanding',
            'না বুঝে "হ্যাঁ" বলে মাথা নাড়ানো',
          ),
        },
        {
          hazard: false,
          label: t(
            '聞{き}き取{と}れないとき「もう一度{いちど}お願{ねが}いします」と聞{き}き返{かえ}す',
            'Asking "Please repeat" when not catching words',
            'বুঝতে না পারলে "আরেকবার বলুন" জিজ্ঞেস করা',
          ),
        },
        {
          hazard: true,
          label: t(
            '機械{きかい}の異音{いおん}に気{き}づいても「気{き}のせい」と放置{ほうち}する',
            'Ignoring machine noise thinking it is nothing',
            'মেশিনের অস্বাভাবিক শব্দকে পাত্তা না দিয়ে এড়িয়ে যাওয়া',
          ),
        },
        {
          hazard: true,
          label: t(
            '小{ちい}さな怪我{けが}を職長{しょくちょう}に隠{かく}して作業{さぎょう}を続{つづ}ける',
            'Hiding a minor injury from foreman and working',
            'ছোট চোট ফোরম্যানের কাছে গোপন রেখে কাজ চালিয়ে যাওয়া',
          ),
        },
        {
          hazard: false,
          label: t(
            '小{ちい}さなミスでも直{ただ}ちに作業{さぎょう}を止{と}めて職長{しょくちょう}に報告{ほうこく}する',
            'Stopping work immediately and reporting small error to foreman',
            'সামান্য ভুলেও সাথে সাথে কাজ থামিয়ে ফোরম্যানকে জানানো',
          ),
        },
        {
          hazard: true,
          label: t(
            '体調{たいちょう}が悪{わる}いのに我慢{がまん}して高所{こうしょ}作業{さぎょう}に上{あ}がる',
            'Enduring illness and climbing to work at heights',
            'শরীর খারাপ চেপে রেখে উঁচুতে কাজ করতে ওঠা',
          ),
        },
        {
          hazard: false,
          label: t(
            '作業前{さぎょうまえ}に「気分{きぶん}が悪{わる}いです」と職長{しょくちょう}に相談{そうだん}する',
            'Consulting foreman before work saying feeling unwell',
            'কাজ শুরুর আগে শরীর খারাপ লাগছে বলে ফোরম্যানের সাথে কথা বলা',
          ),
        },
        {
          hazard: false,
          label: t(
            '指示{しじ}された内容{ないよう}をメモして声{こえ}に出{だ}して復唱{ふくしょう}する',
            'Writing down instructions and repeating them aloud',
            'নির্দেশ খাতায় লিখে মুখে বলে নিশ্চিত করা',
          ),
        },
        {
          hazard: true,
          label: t(
            '火事{かじ}を発見{はっけん}したのに声{こえ}を出{だ}さず一人{ひとり}で逃{に}げる',
            'Fleeing alone without shouting when spotting a fire',
            'আগুন দেখে চিৎকার না করে একা একা পালিয়ে যাওয়া',
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
        '১১৯ জরুরি কল করার ধাপ',
      ),
      goal: t(
        '火災{かさい}・救急{きゅうきゅう}通報{つうほう}の正{ただ}しい流{なが}れを順番{じゅんばん}に並{なら}べる。',
        'Arrange the correct sequence for fire or ambulance 119 reporting.',
        'অগ্নিকাণ্ড বা অ্যাম্বুলেন্সের জন্য ১১৯ এ ডাকার সঠিক ক্রম সাজাও।',
      ),
      steps: [
        t(
          '電話{でんわ}で「119」をダイヤルする',
          'Dial 119 on the telephone',
          'টেলিফোনে "১১৯" ডায়াল করা',
        ),
        t(
          '「救急{きゅうきゅう}です」または「火事{かじ}です」と告{つ}げる',
          'State clearly "Ambulance" or "Fire"',
          '"অ্যাম্বুলেন্স" বা "আগুন লেগেছে" পরিষ্কারভাবে বলা',
        ),
        t(
          '現場{げんば}の住所{じゅうしょ}と会社名{かいしゃめい}・工場{こうじょう}の場所{ばしょ}を伝{つた}える',
          'Provide site address, company name, and building location',
          'সাইটের ঠিকানা, কোম্পানির নাম ও ভবনের অবস্থান জানানো',
        ),
        t(
          '怪我{けが}人{にん}の人数{にんずう}や意識{いしき}・火災{かさい}の状況{じょうきょう}を説明{せつめい}する',
          'Describe number of casualties, consciousness, and fire situation',
          'আহত ব্যক্তির সংখ্যা, জ্ঞান আছে কিনা এবং আগুনের পরিস্থিতি বর্ণনা করা',
        ),
        t(
          '通報者{つうほうしゃ}の氏名{しめい}と連絡先{れんらくさき}電話番号{でんわばんごう}を伝{つた}える',
          'Give your name and callback telephone number',
          'নিজের নাম এবং যোগাযোগের ফোন নম্বর জানানো',
        ),
      ],
    },
    {
      id: 'abnormality-report-seq',
      kind: 'seq',
      name: t(
        '異常{いじょう}・ミス発生時{はっせいじ}の報告手順{ほうこくてじゅん}',
        'Abnormality & Incident Reporting Sequence',
        'অস্বাভাবিকতা বা ভুলের ক্ষেত্রে রিপোর্ট করার ধাপ',
      ),
      goal: t(
        'トラブルが起{お}きたときの報連相{ほうれんそう}の手順{てじゅん}を身{み}につける。',
        'Master the sequence of reporting when trouble occurs.',
        'সমস্যা হলে রিপোর্ট করার সঠিক ধাপগুলো আয়ত্ত করো।',
      ),
      steps: [
        t(
          '作業{さぎょう}を直{ただ}ちに止{と}めて機械{きかい}の停止{ていし}ボタンを押{お}す',
          'Immediately stop work and press the machine stop button',
          'সাথে সাথে কাজ বন্ধ করো এবং মেশিনের স্টপ বাটন চাপো',
        ),
        t(
          '自分{じぶん}と同僚{どうりょう}の安全{あんぜん}を確認{かくにん}して二次災害{にじさいがい}を防{ふせ}ぐ',
          'Confirm safety of yourself and coworkers to prevent secondary disasters',
          'নিজের ও সহকর্মীদের নিরাপত্তা নিশ্চিত করে পরবর্তী দুর্ঘটনা রোধ করো',
        ),
        t(
          '直{ただ}ちに職長{しょくちょう}のところへ行{い}き「報告{ほうこく}があります」と伝{つた}える',
          'Go immediately to the foreman and say "I have a report"',
          'দেরি না করে ফোরম্যানের কাছে গিয়ে "রিপোর্ট আছে" বলো',
        ),
        t(
          '言{い}い訳{わけ}をせず何{なに}が起{お}きたか事実{じじつ}を簡潔{かんけつ}に説明{せつめい}する',
          'Briefly explain the facts of what happened without making excuses',
          'কোনো অজুহাত না দিয়ে কী ঘটেছে সংক্ষেপে সত্য বলো',
        ),
        t(
          '職長{しょくちょう}の指示{しじ}を聞{き}いて声{こえ}に出{だ}して復唱{ふくしょう}する',
          'Listen to foreman instructions and repeat them aloud to confirm',
          'ফোরম্যানের পরবর্তী নির্দেশ মনোযোগ দিয়ে শোনো ও মুখে বলে নিশ্চিত করো',
        ),
      ],
    },
    {
      id: 'emergency-call-reflex',
      kind: 'reflex',
      name: t(
        '緊急時{きんきゅうじ}の「危{あぶ}ない！」反射{はんしゃ}',
        'Emergency Danger Reaction Reflex',
        'জরুরি পরিস্থিতিতে "আবুনাই!" রিফ্লেক্স',
      ),
      goal: t(
        '危険{きけん}な兆候{ちょうこう}を発見{はっけん}！0.4秒以内{びょういない}に「危{あぶ}ない！」と声{こえ}を上{あ}げて非常停止{ひじょうていし}を押{お}す。',
        'Danger sign spotted! Call out "Abunai!" and hit emergency stop within 0.4 s.',
        'বিপদের লক্ষণ! ০.৪ সেকেন্ডের মধ্যে "আবুনাই!" চিৎকার করে জরুরি স্টপ বাটন চাপো।',
      ),
    },
  ],
  inspect: [
    {
      id: 'emergency-contact-card',
      name: t(
        '携帯用{けいたいよう}緊急連絡{きんきゅうれんらく}カード',
        'Personal Emergency Contact Card',
        'ব্যক্তিগত জরুরি যোগাযোগ কার্ড',
      ),
      defective: false,
      spots: [
        {
          x: 30,
          y: 35,
          check: t(
            '工場{こうじょう}の正式住所{せいしきじゅうしょ}と電話番号{でんわばんごう}が鮮明{せんめい}に読{よ}み取{と}れること',
            'Factory official address and phone number are clearly legible',
            'কারখানার আনুষ্ঠানিক ঠিকানা ও ফোন নম্বর পরিষ্কারভাবে পড়া যাচ্ছে',
          ),
        },
        {
          x: 70,
          y: 65,
          check: t(
            '現場{げんば}の職長{しょくちょう}の氏名{しめい}と緊急連絡先{きんきゅうれんらくさき}が正{ただ}しく記入{きにゅう}されていること',
            'Site foreman name and emergency contact number are correctly entered',
            'সাইট ফোরম্যানের নাম ও জরুরি যোগাযোগ নম্বর সঠিকভাবে লেখা আছে',
          ),
        },
      ],
    },
    {
      id: 'emergency-phone-board',
      name: t(
        '工場内{こうじょうない}非常電話{ひじょうでんわ}・通報{つうほう}板{ばん}',
        'Site Emergency Call Station Board',
        'কারখানার জরুরি টেলিফোন ও নোটিশ বোর্ড',
      ),
      defective: true,
      spots: [
        {
          x: 25,
          y: 30,
          check: t(
            '119番{ばん}（消防{しょうぼう}・救急{きゅうきゅう}）と110番{ばん}（警察{けいさつ}）の表示{ひょうじ}がかすれず読{よ}めること（読めないものは不良）',
            '119 (fire/ambulance) and 110 (police) signs are legible; illegible is defective',
            '১১৯ (দমকল/অ্যাম্বুলেন্স) এবং ১১০ (পুলিশ) লেখা স্পষ্ট পড়া যাচ্ছে (অস্পষ্ট হলে ত্রুটিযুক্ত)',
          ),
        },
        {
          x: 75,
          y: 70,
          check: t(
            '受話器{じゅわき}のコードに断線{だんせん}がなく通話{つうわ}テストで発信音{はっしんおん}が鳴{な}ること',
            'Handset cord has no breaks and dial tone sounds during test',
            'হ্যান্ডসেটের তার অক্ষত এবং পরীক্ষায় ডায়াল টোন শোনা যাচ্ছে',
          ),
        },
      ],
    },
    {
      id: 'safety-handbook-pocket',
      name: t(
        '安全{あんぜん}ポケット手帳{てちょう}・指差呼称{しさこしょう}票{ひょう}',
        'Pocket Safety Handbook & Pointing Sheet',
        'পকেট সেফটি হ্যান্ডবুক ও পয়েন্টিং শিট',
      ),
      defective: false,
      spots: [
        {
          x: 35,
          y: 40,
          check: t(
            '報連相{ほうれんそう}の手順{てじゅん}と緊急連絡網{きんきゅうれんらくもう}のページが破{やぶ}れず揃{そろ}っていること',
            'Hou-Ren-So procedure and emergency tree pages are complete with no tears',
            'হো-রেন-সো নিয়ম ও জরুরি যোগাযোগের পাতার পৃষ্ঠা অক্ষত আছে',
          ),
        },
        {
          x: 65,
          y: 60,
          check: t(
            '救急時{きゅうきゅうじ}の伝達{でんたつ}フレーズが母国語{ぼこくご}（ベンガル語{ご}・英語{えいご}）で併記{へいき}されていること',
            'Emergency phrases are written alongside in native languages (Bengali / English)',
            'জরুরি অবস্থার কথা বলার বাক্য মাতৃভাষায় (বাংলা / ইংরেজি) পাশাপাশি লেখা আছে',
          ),
        },
      ],
    },
    {
      id: 'first-aid-station',
      name: t(
        '現場{げんば}救急箱{きゅうきゅうばこ}・応急手当{おうきゅうてあて}セット',
        'Site First Aid Kit Box',
        'কাজের জায়গার প্রাথমিক চিকিৎসা বক্স',
      ),
      defective: true,
      spots: [
        {
          x: 30,
          y: 30,
          check: t(
            '消毒液{しょうどくえき}・清潔{せいけつ}なガーゼ・絆創膏{ばんそうこう}が十分{じゅうぶん}に補充{ほじゅう}されていること（中身が空は不良）',
            'Disinfectant, clean gauze, and bandages are well stocked; empty is defective',
            'জীবাণুনাশক, পরিষ্কার গজ ও ব্যান্ডেজ পর্যাপ্ত আছে (খালি থাকলে ত্রুটিযুক্ত)',
          ),
        },
        {
          x: 70,
          y: 75,
          check: t(
            '救急箱{きゅうきゅうばこ}の外箱{そとばこ}に破損{はそん}がなく、ふたが密閉{みっぺい}できること',
            'Outer case has no cracks and lid seals tightly against moisture and dust',
            'বক্সের বাইরের অংশ ভাঙা নয় এবং ঢাকনা ধুলোবালি রোধে শক্তভাবে বন্ধ হয়',
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
        'ফোরম্যান দ্রুত জাপানি বলায় আপনি কাজের নির্দেশ ভালো বুঝতে পারেননি। আপনার কী করা উচিত?',
      ),
      options: [
        t(
          '「すみません、もう一度{いちど}ゆっくりお願{ねが}いします」と聞{き}き返{かえ}す',
          'Ask "Excuse me, please say that once more slowly"',
          '"মাফ করবেন, দয়া করে আরেকবার আস্তে বলুন" বলে আবার জিজ্ঞেস করা',
        ),
        t(
          '怒{おこ}られないように「はい、分{わ}かりました」と答{こた}えて作業{さぎょう}を始{はじ}める',
          'Answer "Yes, understood" to avoid being scolded and start working',
          'বকা না খাওয়ার জন্য "হ্যাঁ, বুঝেছি" বলে কাজ শুরু করে দেওয়া',
        ),
        t(
          '自分{じぶん}の判断{はんだん}で適当{てきとう}に手順{てじゅん}を予想{よそう}して作業{さぎょう}する',
          'Guess the procedure on your own and proceed with work',
          'নিজের অনুমানে কাজের নিয়ম আন্দাজ করে কাজ চালিয়ে যাওয়া',
        ),
        t(
          '誰{だれ}にも聞{き}き返{かえ}さず、他{ほか}の作業員{さぎょういん}の様子{ようす}を黙{だま}って観察{かんさつ}し続{つづ}ける',
          'Ask nobody and just keep quietly watching what other workers do',
          'কাউকে কিছু না জিজ্ঞেস করে অন্য কর্মীরা কী করছে চুপচাপ দেখতে থাকা',
        ),
      ],
      answer: 0,
      why: t(
        '分{わ}からないままうなずいて作業{さぎょう}に入{はい}るのは重大事故{じゅうだいじこ}の最大{さいだい}の原因{げんいん}です。何度{なんど}でも「もう一度{いちど}お願{ねが}いします」と聞{き}き直{なお}すのが正{ただ}しい安全行動{あんぜんこうどう}です。',
        'Working without understanding is the greatest cause of serious accidents. Asking "Please repeat" as many times as needed is the right safety practice.',
        'না বুঝে মাথা নেড়ে কাজ শুরু করাই মারাত্মক দুর্ঘটনার সবচেয়ে বড় কারণ। যতবার দরকার "দয়া করে আরেকবার বলুন" বলে জেনে নেওয়াই সঠিক নিয়ম।',
      ),
    },
    {
      q: t(
        '作業中{さぎょうちゅう}に足場{あしば}から同僚{どうりょう}が転落{てんらく}し、床{ゆか}で血{ち}を流{なが}して倒{たお}れています。最初{さいしょ}に叫{さけ}ぶべき言葉{ことば}はどれですか？',
        'During work, a coworker fell from a scaffold and is bleeding on the floor. What phrase should you shout first?',
        'কাজের সময় সহকর্মী মাচা থেকে পড়ে মেঝেতে রক্তাক্ত অবস্থায় পড়ে আছে। প্রথমে কী বলে চিৎকার করা উচিত?',
      ),
      options: [
        t(
          '「私{わたし}は悪{わる}くありません！」',
          '"It is not my fault!"',
          '"আমার কোনো দোষ নেই!"',
        ),
        t(
          '「助{たす}けてください！救急車{きゅうきゅうしゃ}をお願{ねが}いします！」',
          '"Please help! Please call an ambulance!"',
          '"বাঁচান! দয়া করে অ্যাম্বুলেন্স ডাকুন!"',
        ),
        t(
          '「今日{きょう}の作業{さぎょう}は終{お}わりですか？」',
          '"Is today\'s work finished?"',
          '"আজকের কাজ কি শেষ?"',
        ),
        t(
          '「写真{しゃしん}を撮{と}ってください！」',
          '"Please take a photograph!"',
          '"একটা ছবি তুলুন!"',
        ),
      ],
      answer: 1,
      why: t(
        '人命{じんめい}に関{かか}わる緊急時{きんきゅうじ}は、直{ただ}ちに「助{たす}けてください！」と叫{さけ}んで周囲{しゅうい}に非常事態{ひじょうじたい}を伝{つた}え、救急車{きゅうきゅうしゃ}を呼{よ}びます。',
        'In life-threatening emergencies, immediately shout "Help!" to alert everyone of the crisis and summon an ambulance.',
        'জীবন বাঁচানোর জরুরি পরিস্থিতিতে সাথে সাথে "বাঁচান!" বলে সবাইকে বিপদ সংকেত দিতে হবে এবং অ্যাম্বুলেন্স ডাকতে হবে।',
      ),
    },
    {
      q: t(
        '火災{かさい}や急病{きゅうびょう}で「119番{ばん}」に電話{でんわ}をかけました。指令員{しれいいん}が出{で}た最初{さいしょ}に聞{き}かれる質問{しつもん}は何{なん}ですか？',
        'You dialed 119 for a fire or medical emergency. What is the first question the operator asks you?',
        'আগুন বা জরুরি চিকিৎসার জন্য ১১৯ এ ফোন করলেন। অপারেটর ফোন ধরেই প্রথম কোন প্রশ্নটি করবে?',
      ),
      options: [
        t(
          '「今日{きょう}の天気{てんき}はどうですか」',
          '"How is the weather today?"',
          '"আজকের আবহাওয়া কেমন?"',
        ),
        t(
          '「あなたの国籍{こくせき}はどこですか」',
          '"What is your nationality?"',
          '"আপনার জাতীয়তা কী?"',
        ),
        t(
          '「火事{かじ}ですか、救急{きゅうきゅう}ですか」',
          '"Is it a fire, or is it an ambulance (medical)?"',
          '"আগুন লেগেছে, নাকি অ্যাম্বুলেন্স (চিকিৎসা) প্রয়োজন?"',
        ),
        t(
          '「会社{かいしゃ}の社長{しゃちょう}の名前{なまえ}は何{なん}ですか」',
          '"What is the name of your company president?"',
          '"আপনার কোম্পানির প্রেসিডেন্টের নাম কী?"',
        ),
      ],
      answer: 2,
      why: t(
        '119番{ばん}は消防署{しょうぼうしょ}につながり、最初{さいしょ}に「火事{かじ}ですか、救急{きゅうきゅう}ですか」と確認{かくにん}されます。落{お}ち着{つ}いて「救急{きゅうきゅう}です」または「火事{かじ}です」と答{こた}えます。',
        '119 connects to the fire dispatch, which first asks "Fire or ambulance?" Stay calm and answer either "Ambulance" or "Fire."',
        '১১৯ দমকল কেন্দ্রে যুক্ত হয় এবং শুরুতেই জানতে চায় "আগুন নাকি অ্যাম্বুলেন্স?" শান্ত থেকে "অ্যাম্বুলেন্স" বা "আগুন" উত্তর দিতে হবে।',
      ),
    },
    {
      q: t(
        '使用中{しようちゅう}の工作機械{こうさくきかい}から激{はげ}しい異音{いおん}と焦{こ}げ臭{くさ}いにおいが発生{はっせい}しました。正{ただ}しい対応{たいおう}はどれですか？',
        'A machine you are operating suddenly makes a violent abnormal screech and emits a burning smell. What is the correct action?',
        'চলমান মেশিন থেকে হঠাৎ বিকট অস্বাভাবিক শব্দ ও পোড়া গন্ধ বের হতে লাগল। সঠিক পদক্ষেপ কোনটি?',
      ),
      options: [
        t(
          '臭{にお}いが消{き}えるまで我慢{がまん}してそのまま加工作業{かこうさぎょう}を続{つづ}ける',
          'Endure until the smell disappears and keep machining',
          'গন্ধ চলে যাওয়া পর্যন্ত সহ্য করে কাজ চালিয়ে যাওয়া',
        ),
        t(
          '同僚{どうりょう}に気{き}づかれないよう、機械{きかい}の速度{そくど}を最高{さいこう}にして急{いそ}ぐ',
          'Speed up the machine to maximum so coworkers do not notice',
          'সহকর্মীরা যাতে না দেখে সেজন্য মেশিনের গতি বাড়িয়ে দ্রুত কাজ সারা',
        ),
        t(
          '機械{きかい}のスイッチを入{い}れたまま、手{て}を伸{の}ばして中{なか}を覗{のぞ}き込{こ}む',
          'Reach in and peer inside while leaving the machine power switch on',
          'মেশিনের সুইচ চালু রেখেই ভেতরে হাত দিয়ে উঁকি মেরে দেখা',
        ),
        t(
          '直{ただ}ちに停止{ていし}ボタンを押{お}して機械{きかい}を止{と}め、職長{しょくちょう}に「異音{いおん}がします」と報告{ほうこく}する',
          'Immediately press stop button to halt machine and report "Abnormal noise" to foreman',
          'সাথে সাথে স্টপ বাটন চেপে মেশিন বন্ধ করা এবং ফোরম্যানকে "অস্বাভাবিক শব্দ হচ্ছে" জানানো',
        ),
      ],
      answer: 3,
      why: t(
        '機械{きかい}の異常{いじょう}や異音{いおん}・異臭{いしゅう}は重大事故{じゅうだいじこ}や火災{かさい}の前兆{ぜんちょう}です。直{ただ}ちに停止{ていし}させて職長{しょくちょう}へ報告{ほうこく}します。',
        'Abnormal sounds and burning smells are warning signs of serious damage or fire. Stop the machine immediately and report to your foreman.',
        'মেশিনের অস্বাভাবিক শব্দ বা পোড়া গন্ধ মারাত্মক দুর্ঘটনা ও অগ্নিকাণ্ডের লক্ষণ। সাথে সাথে থামিয়ে ফোরম্যানকে রিপোর্ট করতে হবে।',
      ),
    },
    {
      q: t(
        '日本{にほん}の現場{げんば}の基本{きほん}「報連相{ほうれんそう}」の意味{いみ}として正{ただ}しい組合{くみあ}わせはどれですか？',
        'What is the correct combination of meanings for "Hou-Ren-So" in Japanese workplaces?',
        'জাপানি কর্মক্ষেত্রের মূল ভিত্তি "হো-রেন-সো" এর সঠিক অর্থ কোনটি?',
      ),
      options: [
        t(
          '報告{ほうこく}（結果{けっか}を伝{つた}える）・連絡{れんらく}（情報{じょうほう}を共有{きょうゆう}する）・相談{そうだん}（迷{まよ}ったら意見{いけん}を聞{き}く）',
          'Houkoku (report results), Renraku (share information), Soudan (consult when unsure)',
          'হোকোকু (ফলাফল জানানো), রেনরাকু (তথ্য ভাগ করা), সৌদান (সন্দেহ হলে পরামর্শ নেওয়া)',
        ),
        t(
          '放置{ほうち}（そのままにする）・練習{れんしゅう}（訓練{くんれん}する）・早退{そうたい}（早{はや}く帰{かえ}る）',
          'Houchi (leave alone), Renshuu (practice), Soutai (leave early)',
          'হোউচি (ফেলে রাখা), রেনশু (অনুশীলন), সোতাই (আগে চলে যাওয়া)',
        ),
        t(
          '放任{ほうにん}（何{なに}もしない）・連続{れんぞく}（休{やす}まず働{はたら}く）・掃除{そうじ}（床{ゆか}を拭{ふ}く）',
          'Hounin (do nothing), Renzoku (work without break), Souji (sweep floor)',
          'হোনিন (কিছু না করা), রেনজোকু (না থেমে কাজ করা), সোজি (ঝাড়ু দেওয়া)',
        ),
        t(
          '褒美{ほうび}（ご褒美{ほうび}をもらう）・連休{れんきゅう}（長{なが}く休{やす}む）・早起{はやお}き（朝早{あさはや}く起{お}きる）',
          'Houbi (get rewards), Renkyuu (long holiday), Hayaoki (wake up early)',
          'হোবি (পুরস্কার পাওয়া), রেনকিউ (লম্বা ছুটি), হায়াওকি (সকালে ওঠা)',
        ),
      ],
      answer: 0,
      why: t(
        '報連相{ほうれんそう}は「報告{ほうこく}」「連絡{れんらく}」「相談{そうだん}」の略{りゃく}です。業務{ぎょうむ}の結果{けっか}を伝{つた}え、情報{じょうほう}を共有{きょうゆう}し、疑問{ぎもん}や不安{ふあん}を相談{そうだん}します。',
        'Hou-Ren-So stands for Houkoku (Report), Renraku (Communicate), and Soudan (Consult). It ensures clear results, shared facts, and mutual advice.',
        'হো-রেন-সো হলো হোকোকু (রিপোর্ট), রেনরাকু (যোগাযোগ) ও সৌদান (পরামর্শ) এর সংক্ষিপ্ত রূপ। এটি কাজের ফলাফল, তথ্য ও দ্বিধা দূর করতে সাহায্য করে।',
      ),
    },
    {
      q: t(
        '作業中{さぎょうちゅう}に手元{てもと}が狂{くる}い、工具{こうぐ}を通路{つうろ}に落{お}としてしまいました。幸{さいわ}い誰{だれ}にも当{あ}たらず怪我{けが}人{にん}はいません。どうすべきですか？',
        'During work, you slipped and dropped a tool into a walkway. Fortunately nobody was struck and no one was hurt. What should you do?',
        'কাজের সময় হাত ফসকে একটি টুল হাঁটার পথে পড়ে গেল। ভাগ্যক্রমে কারও গায়ে লাগেনি এবং কেউ আহত হয়নি। আপনার কী করা উচিত?',
      ),
      options: [
        t(
          '誰{だれ}にも見{み}られていないので、何{なに}も言{い}わずに工具{こうぐ}を拾{ひろ}って隠{かく}す',
          'Nobody saw it, so silently pick it up and hide the incident',
          'কেউ দেখেনি, তাই চুপচাপ টুলটি তুলে নিয়ে ঘটনা চেপে যাওয়া',
        ),
        t(
          '「工具{こうぐ}を落{お}としてしまいました」と直{ただ}ちに職長{しょくちょう}に報告{ほうこく}する（ヒヤリ・ハット）',
          'Immediately report "I dropped a tool" to the foreman (near-miss report)',
          'সাথে সাথে ফোরম্যানকে জানানো "টুলটি পড়ে গিয়েছিল" (হিয়ারি-হাট্টো / অল্পের জন্য দুর্ঘটনা)',
        ),
        t(
          '落{お}とした工具{こうぐ}を通路{つうろ}に置{お}いたまま作業{さぎょう}を続{つづ}ける',
          'Leave the dropped tool on the walkway and keep working',
          'পড়ে যাওয়া টুলটি পথের ওপর ফেলেই কাজ চালিয়ে যাওয়া',
        ),
        t(
          '同僚{どうりょう}のせいにして自分{じぶん}の責任{せきにん}ではないと嘘{うそ}をつく',
          'Blame a coworker and falsely claim it was not your fault',
          'সহকর্মীর ওপর দোষ চাপিয়ে মিথ্যা বলা যে এটা আপনার ভুল ছিল না',
        ),
      ],
      answer: 1,
      why: t(
        'ケガがなくても、落下{らっか}は人{ひと}に直撃{ちょくげき}すれば死亡{しぼう}事故{じこ}になり得{う}る重大{じゅうだい}なヒヤリ・ハットです。原因{げんいん}を共有{きょうゆう}して再発{さいはつ}を防{ふせ}ぐため、必{かなら}ず報告{ほうこく}します。',
        'Even if no one was hurt, falling objects can cause fatal accidents. You must report near-misses immediately so the site can prevent recurrence.',
        'কেউ আহত না হলেও ওপর থেকে কিছু পড়লে প্রাণহানি ঘটতে পারে। পুনরাবৃত্তি রোধে ছোটখাটো বিপদও সাথে সাথে রিপোর্ট করতে হবে।',
      ),
    },
    {
      q: t(
        '夏{なつ}の午後{ごご}の作業前{さぎょうまえ}、めまいと頭痛{ずつう}がして吐{は}き気{け}があります。どう行動{こうどう}すべきですか？',
        'Before starting work on a hot summer afternoon, you feel dizzy, have a headache and feel nauseous. How should you act?',
        'তপ্ত গ্রীষ্মের দুপুরে কাজ শুরুর আগে আপনার মাথা ঘুরছে, মাথাব্যথা ও বমি বমি ভাব হচ্ছে। আপনার কীভাবে চলা উচিত?',
      ),
      options: [
        t(
          '「休{やす}むと迷惑{めいわく}がかかる」と思{おも}って無理{むり}をして作業{さぎょう}を続{つづ}ける',
          'Think "taking a break causes trouble" and force yourself to keep working',
          '"বিশ্রাম নিলে ঝামেলা হবে" ভেবে শরীর খারাপ নিয়েও জোর করে কাজ চালিয়ে যাওয়া',
        ),
        t(
          '誰{だれ}にも言{い}わずに現場{げんば}の物陰{ものかげ}に隠{かく}れて眠{ねむ}る',
          'Hide in the shadows on site and go to sleep without telling anyone',
          'কাউকে না জানিয়ে সাইটের আড়ালে লুকিয়ে ঘুমিয়ে থাকা',
        ),
        t(
          '我慢{がまん}せず、作業前{さぎょうまえ}に職長{しょくちょう}に「気分{きぶん}が悪{わる}いです」と相談{そうだん}する',
          'Do not endure it; consult the foreman before work: "Kibun ga warui desu"',
          'চেপে না রেখে কাজ শুরুর আগেই ফোরম্যানকে "শরীর খারাপ লাগছে" বলে জানানো',
        ),
        t(
          '冷{つめ}たい水{みず}を大量{たいりょう}に一気{いっき}飲{の}みして、走{はし}って高所{こうしょ}足場{あしば}に登{のぼ}る',
          'Gulp a huge amount of cold water at once and run up the scaffold',
          'একসাথে অনেক ঠান্ডা পানি গিলে দৌড়ে মাচার ওপরে উঠে যাওয়া',
        ),
      ],
      answer: 2,
      why: t(
        '熱中症{ねっちゅうしょう}や体調不良{たいちょうふりょう}を我慢{がまん}して作業{さぎょう}に入{はい}ると、意識{いしき}を失{うしな}って墜落{ついらく}するなどの命{いのち}に関{かか}わる事故{じこ}につながります。事前{じぜん}の相談{そうだん}が原則{げんそく}です。',
        'Enduring heatstroke symptoms or illness leads to loss of consciousness and fatal falls. Consulting your foreman beforehand is a mandatory rule.',
        'হিটস্ট্রোক বা শারীরিক অসুস্থতা চেপে রেখে কাজ করলে জ্ঞান হারিয়ে পড়ে গিয়ে মারাত্মক দুর্ঘটনা ঘটতে পারে। আগেই পরামর্শ নেওয়া আবশ্যক।',
      ),
    },
    {
      q: t(
        '工場{こうじょう}の敷地内{しきちない}で盗難{とうなん}や不審者{ふしんしゃ}の侵入{しんにゅう}、または道路{どうろ}での交通事故{こうつうじこ}が起{お}きました。警察{けいさつ}を呼{よ}ぶ緊急番号{きんきゅうばんごう}はどれですか？',
        'A theft, an intruder, or a traffic accident occurred near the factory gates. Which emergency telephone number calls the police?',
        'কারখানার ভেতরে চুরি, সন্দেহভাজন ব্যক্তি প্রবেশ বা রাস্তায় ট্রাফিক দুর্ঘটনা ঘটল। পুলিশ ডাকতে কোন নম্বরে ফোন করবেন?',
      ),
      options: [
        t('119番{ばん}', '119', '১১৯'),
        t('117番{ばん}', '117', '১১৭'),
        t('104番{ばん}', '104', '১০৪'),
        t('110番{ばん}', '110', '১১০'),
      ],
      answer: 3,
      why: t(
        '日本{にほん}で警察{けいさつ}（事件{じけん}・事故{じこ}・盗難{とうなん}）に通報{つうほう}する番号{ばんごう}は「110番{ばん}」です。消防{しょうぼう}・救急{きゅうきゅう}は「119番{ばん}」です。混同{こんどう}しないよう覚{おぼ}えましょう。',
        'In Japan, the emergency telephone number for police (crimes, accidents, theft) is 110. Fire and ambulance is 119. Never confuse the two.',
        'জাপানে পুলিশ (অপরাধ, দুর্ঘটনা, চুরি) ডাকার জরুরি নম্বর হলো ১১০। অগ্নিনির্বাপণ ও অ্যাম্বুলেন্স হলো ১১৯। এই দুটো গুলিয়ে ফেলবেন না।',
      ),
    },
    {
      q: t(
        '朝礼{ちょうれい}で新{あたら}しい安全装置{あんぜんそうち}の操作方法{そうさほうほう}が説明{せつめい}されましたが、専門用語{せんもんようご}が多{おお}くて理解{りかい}できませんでした。どうすべきですか？',
        'A new safety device procedure was explained at the morning meeting, but you could not understand it due to technical jargon. What should you do?',
        'সকালের মিটিংয়ে নতুন সেফটি ডিভাইসের ব্যবহার বোঝানো হলো, কিন্তু কঠিন টেকনিক্যাল শব্দের কারণে আপনি কিছুই বোঝেননি। কী করবেন?',
      ),
      options: [
        t(
          '「分{わ}かりません。通訳{つうやく}をお願{ねが}いします」と職長{しょくちょう}に申{もう}し出{で}る',
          'Tell the foreman: "I do not understand. Please get an interpreter"',
          'ফোরম্যানকে বলা: "আমি বুঝিনি। দয়া করে একজন দোভাষী দিন"',
        ),
        t(
          '自分{じぶん}だけ分{わ}からないのが恥{は}ずかしいので、他{ほか}の人{ひと}に合{あ}わせてうなずく',
          'Feel embarrassed to be the only one who does not know, so nod along with others',
          'নিজে একা না বোঝায় লজ্জা পেয়ে সবার সাথে তাল মিলিয়ে মাথা নাড়ানো',
        ),
        t(
          '説明書{せつめいしょ}を読{よ}まずに、スイッチを適当{てきとう}に押{お}して機械{きかい}を動{うご}かしてみる',
          'Without reading the manual, press buttons randomly to test the machine',
          'ম্যানুয়াল না পড়ে আন্দাজে যেকোনো বাটন চেপে মেশিন চালিয়ে পরীক্ষা করা',
        ),
        t(
          '安全装置{あんぜんそうち}の配線{はいせん}を勝手{かって}に切断{せつだん}して無効化{むこうか}する',
          'Cut the safety device wiring without permission to disable it',
          'অনুমতি ছাড়া সেফটি ডিভাইসের তার কেটে তা অকেজো করে রাখা',
        ),
      ],
      answer: 0,
      why: t(
        '安全{あんぜん}に関{かか}わる手順{てじゅん}は完璧{かんぺき}に理解{りかい}していなければ命{いのち}に関{かか}わります。「分{わ}かりません」「通訳{つうやく}をお願{ねが}いします」と伝{つた}えることは正当{せいとう}な権利{けんり}です。',
        'Safety procedures must be fully understood to avoid fatal risks. Saying "I do not understand" or asking for an interpreter is your proper right.',
        'নিরাপত্তা সংক্রান্ত নিয়ম সম্পূর্ণ না বুঝলে জীবনের ঝুঁকি তৈরি হয়। "বুঝিনি" বলা বা দোভাষী চাওয়া আপনার ন্যায্য অধিকার।',
      ),
    },
    {
      q: t(
        '職長{しょくちょう}に仕事{しごと}の「報告{ほうこく}」をするとき、話{はな}し方{かた}の順序{じゅんじょ}として最{もっと}も適切{てきせつ}なものはどれですか？',
        'When delivering a work report (Houkoku) to your foreman, what is the best order of speaking?',
        'ফোরম্যানের কাছে কাজের "হোকোকু" (রিপোর্ট) করার সময় কথার সঠিক ধারাবাহিকতা কোনটি?',
      ),
      options: [
        t(
          '自分{じぶん}の苦労{くろう}や言{い}い訳{わけ}を長{なが}く話{はな}したあとで、結論{けつろん}を濁{にご}す',
          'Talk at length about personal struggles and excuses, then leave the conclusion vague',
          'নিজের কষ্ট ও অজুহাত অনেকক্ষণ ধরে বলে শেষে মূল সিদ্ধান্ত অস্পষ্ট রাখা',
        ),
        t(
          'まず「結論{けつろん}と結果{けっか}（終{お}わったかどうか・起{お}きた事実{じじつ}）」を言{い}い、次{つぎ}に「理由{りゆう}や詳細{しょうさい}」を伝{つた}える',
          'State "Conclusion and results (finished or not, facts)" first, then give reasons and details',
          'প্রথমে "ফলাফল ও মূল কথা (কাজ শেষ হয়েছে কি না, আসল ঘটনা)" বলা, তারপর কারণ ও বিস্তারিত জানানো',
        ),
        t(
          '職長{しょくちょう}が質問{しつもん}してくるまで、何{なに}も話{はな}さず作業{さぎょう}を終{お}わらせる',
          'Say nothing and finish work until the foreman comes over to interrogate you',
          'ফোরম্যান এসে নিজে জিজ্ঞেস না করা পর্যন্ত কিছু না বলে কাজ চালিয়ে যাওয়া',
        ),
        t(
          '事実{じじつ}と異{こと}なる嘘{うそ}の結果{けっか}を伝{つた}えて、怒{おこ}られないようにする',
          'Report false results contrary to facts so that you do not get scolded',
          'বকা না খাওয়ার জন্য আসল ঘটনার বিপরীত মিথ্যা তথ্য বানিয়ে বলা',
        ),
      ],
      answer: 1,
      why: t(
        '報告{ほうこく}は「結論{けつろん}ファースト（結論{けつろん}から先{さき}に話{はな}す）」が基本{きほん}です。まず結果{けっか}や起{お}きた事実{じじつ}を伝{つた}えることで、迅速{じんそく}な判断{はんだん}ができます。',
        'Reports should always follow "Conclusion First." Stating results and facts first enables supervisors to make swift, accurate decisions.',
        'রিপোর্টের মূল নিয়ম হলো "আগে মূল কথা বলা"। ফলাফল ও সত্য আগে জানালে দায়িত্বপ্রাপ্ত ব্যক্তি দ্রুত ও সঠিক সিদ্ধান্ত নিতে পারেন।',
      ),
    },
    {
      q: t(
        '工場{こうじょう}の電気{でんき}設備{せつび}の近{ちか}くで白煙{はくえん}が上{あ}がり、小{ちい}さな炎{ほのお}が見{み}えました。正{ただ}しい初動{しょどう}対応{たいおう}はどれですか？',
        'White smoke rose near an electrical cabinet on site and small flames were visible. What is the correct initial action?',
        'কারখানার বৈদ্যুতিক প্যানেলের কাছে সাদা ধোঁয়া উঠল এবং ছোট আগুনের শিখা দেখা গেল। তাৎক্ষণিক সঠিক পদক্ষেপ কোনটি?',
      ),
      options: [
        t(
          '誰{だれ}にも知{し}らせず、自分{じぶん}の飲{の}み物{もの}（お茶{ちゃ}）を電気{でんき}盤{ばん}に浴{あ}びせかける',
          'Tell nobody and splash your drinking tea onto the electrical panel',
          'কাউকে না জানিয়ে নিজের খাওয়ার চা বা পানি বৈদ্যুতিক বোর্ডের ওপর ঢেলে দেওয়া',
        ),
        t(
          '煙{けむり}を写真{しゃしん}に撮{と}ってSNSに投稿{とうこう}する',
          'Take photos of the smoke and post them to social media',
          'ধোঁয়ার ছবি তুলে সোশ্যাল মিডিয়ায় পোস্ট করা',
        ),
        t(
          '大声{おおごえ}で「火事{かじ}だ！」と叫{さけ}んで周囲{しゅうい}に知{し}らせ、初期消火{しょきしょうか}と通報{つうほう}を行{おこな}う',
          'Shout "Kaji da!" (Fire!) loudly to alert everyone, begin initial firefighting, and report',
          'জোরে "কাঝি দা!" (আগুন!) বলে চিৎকার করে সবাইকে জানানো, আগুন নেভানোর চেষ্টা ও খবর দেওয়া',
        ),
        t(
          '小{ちい}さな火{ひ}だから大丈夫{だいじょうぶ}と思{おも}ってそのまま放置{ほうち}する',
          'Assume it will be fine since the flame is small and leave it alone',
          'ছোট আগুন ভেবে কিছু হবে না মনে করে ওভাবেই ফেলে রাখা',
        ),
      ],
      answer: 2,
      why: t(
        '火災{かさい}を発見{はっけん}したら、まず大声{おおごえ}で「火事{かじ}だ！」と周囲{しゅうい}に知{し}らせることが最優先{さいゆうせん}です。電気{でんき}火災{かさい}への水{みず}かけは感電{かんでん}の危険{きけん}があるため絶対{ぜったい}にしてはなりません。',
        'When discovering a fire, immediately shout "Fire!" to notify the entire floor. Never throw water on electrical fires due to electrocution danger.',
        'আগুন দেখলে সাথে সাথে চিৎকার করে সবাইকে জানানো সবচেয়ে জরুরি। বিদ্যুতের আগুনে পানি ঢাললে বিদ্যুৎস্পৃষ্ট হওয়ার চরম ঝুঁকি থাকে, তাই তা কখনোই করবেন না।',
      ),
    },
    {
      q: t(
        '同僚{どうりょう}から「『分{わ}かりません』と何回{なんかい}も言{い}うと職長{しょくちょう}に嫌{きら}われるから、適当{てきとう}にうなずいておけ」と言{い}われました。どう考{かんが}えるべきですか？',
        'A coworker tells you: "If you say \'I don\'t understand\' repeatedly the foreman will dislike you, so just nod." How should you view this?',
        'একজন সহকর্মী বলল: "বারবার \'বুঝিনি\' বললে ফোরম্যান বিরক্ত হবে, তাই মাথা নেড়ে হ্যাঁ বলে দাও।" এ ব্যাপারে আপনার কী ভাবা উচিত?',
      ),
      options: [
        t(
          '同僚{どうりょう}の言{い}う通{とお}りに、全{すべ}て「はい」とうなずいて分{わ}かったふりをする',
          'Follow the coworkers advice and pretend to understand by nodding "Yes" to everything',
          'সহকর্মীর কথা মেনে সব কিছুতেই "হ্যাঁ" বলে মাথা নেড়ে বোঝার ভান করা',
        ),
        t(
          '怒{おこ}られるのを避{さ}けるため、返事{へんじ}をせず黙{だま}り込{こ}んで無視{むし}する',
          'Stay completely silent and ignore everyone to avoid getting scolded',
          'বকা এড়াতে কোনো উত্তর না দিয়ে চুপচাপ থেকে সবাইকে উপেক্ষা করা',
        ),
        t(
          '安全{あんぜん}教育{きょういく}は意味{いみ}がないので、次{つぎ}の休憩{きゅうけい}まで隠{かく}れてサボる',
          'Think safety training is pointless and hide until the next break to slack off',
          'নিরাপত্তা শিক্ষার কোনো অর্থ নেই ভেবে পরবর্তী বিরতি পর্যন্ত কাজে ফাঁকি দেওয়া',
        ),
        t(
          '同僚{どうりょう}の助言{じょげん}は間違{まちが}いであり、分{わ}からないまま作業{さぎょう}して命{いのち}を落{お}とす危険{きけん}を避{さ}けるため必{かなら}ず質問{しつもん}する',
          'The coworkers advice is wrong; always ask questions to avoid fatal risks of working without comprehension',
          'সহকর্মীর পরামর্শ ভুল; না বুঝে কাজ করে জীবন হারানোর ঝুঁকি এড়াতে অবশ্যই প্রশ্ন করতে হবে',
        ),
      ],
      answer: 3,
      why: t(
        '日本{にほん}の現場{げんば}では、分{わ}からないのに「はい」と答{こた}えることが最{もっと}も危険{きけん}で嫌{きら}われます。安全{あんぜん}のため、理解{りかい}できるまで聞{き}き直{なお}す作業員{さぎょういん}こそが信頼{しんらい}されます。',
        'In Japanese workplaces, pretending to understand by saying "Yes" is the most dangerous and disliked habit. Workers who clarify until they understand are trusted.',
        'জাপানি কর্মক্ষেত্রে না বুঝে "হ্যাঁ" বলা সবচেয়ে ঝুঁকিপূর্ণ ও অপছন্দনীয়। নিরাপত্তার স্বার্থে পুরোপুরি না বোঝা পর্যন্ত প্রশ্নকারী কর্মীকেই সবচেয়ে বেশি বিশ্বাস করা হয়।',
      ),
    },
  ],
  glossary: [
    {
      ja: '報告',
      reading: 'ほうこく',
      en: 'Report / Reporting results',
      bn: 'রিপোর্ট / কাজের ফলাফল জানানো',
    },
    {
      ja: '連絡',
      reading: 'れんらく',
      en: 'Notice / Information sharing',
      bn: 'যোগাযোগ / তথ্য জানানো',
    },
    {
      ja: '相談',
      reading: 'そうだん',
      en: 'Consultation / Asking advice',
      bn: 'পরামর্শ / উপদেশ চাওয়া',
    },
    {
      ja: '報連相',
      reading: 'ほうれんそう',
      en: 'Hou-Ren-So (Report-Contact-Consult)',
      bn: 'হো-রেন-সো (রিপোর্ট-যোগাযোগ-পরামর্শ)',
    },
    {
      ja: '救急車',
      reading: 'きゅうきゅうしゃ',
      en: 'Ambulance',
      bn: 'অ্যাম্বুলেন্স',
    },
    {
      ja: '消防署',
      reading: 'しょうぼうしょ',
      en: 'Fire station (dial 119)',
      bn: 'দমকল কেন্দ্র (১১৯)',
    },
    {
      ja: '警察',
      reading: 'けいさつ',
      en: 'Police (dial 110)',
      bn: 'পুলিশ (১১০)',
    },
    {
      ja: '非常停止',
      reading: 'ひじょうていし',
      en: 'Emergency stop',
      bn: 'জরুরি নির্গমন বা থামা',
    },
    {
      ja: '異音',
      reading: 'いおん',
      en: 'Abnormal noise',
      bn: 'অস্বাভাবিক শব্দ',
    },
    {
      ja: '通訳',
      reading: 'つうやく',
      en: 'Interpreter / Interpretation',
      bn: 'দোভাষী / অনুবাদ',
    },
    {
      ja: '怪我',
      reading: 'けが',
      en: 'Injury / Wound',
      bn: 'আঘাত / চোট',
    },
    {
      ja: '気分',
      reading: 'きぶん',
      en: 'Health state / Physical feeling',
      bn: 'শারীরিক অবস্থা / অনুভূতি',
    },
  ],
}
