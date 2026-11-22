import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string, bn: string): Tri => ({ ja, en, bn })

export const kyKatsudo: CourseData = {
  id: 'ky-katsudo',
  category: 'core',
  level: 1,
  minutes: 15,
  title: t(
    '危険予知活動{きけんよちかつどう}（KY）',
    'Hazard Prediction Activity (KY / KYT)',
    'বিপদ পূর্বানুমান কার্যক্রম (কেওয়াই / কেওয়াইটি)',
  ),
  sub: t(
    '作業前{さぎょうまえ}に危険{きけん}を見{み}つけ、チームで「ゼロ災{さい}」をめざす。',
    'Spot hazards before work and aim for zero accidents as a team.',
    'কাজের আগে বিপদ শনাক্ত করে দলগতভাবে দুর্ঘটনা শূন্যের লক্ষ্য রাখা।',
  ),
  refs: [
    '労働安全衛生法 第60条（職長等に対する安全衛生教育）',
    '労働安全衛生法 第28条の2（危険性・有害性の調査とリスク低減）',
    '中央労働災害防止協会 ゼロ災害全員参加運動推進要綱（KYT・指差呼称）',
  ],
  panels: [
    {
      who: 'foreman',
      text: t(
        'ラヒムさん、作業{さぎょう}を始{はじ}める前{まえ}にKYミーティングをやろう。',
        'Rahim, let\'s do our KY meeting before starting work.',
        'রহিম, কাজ শুরু করার আগে কেওয়াই মিটিং করে নিই।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '職長{しょくちょう}、いつもと同{おな}じ配管{はいかん}の作業{さぎょう}でも、毎朝{まいあさ}KYが必要{ひつよう}ですか？',
        'Foreman, do we need KY every morning even for our usual pipe-fitting work?',
        'ফোরম্যান, প্রতিদিনের চেনা পাইপের কাজের জন্যও কি রোজ সকালে কেওয়াই করতে হবে?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '慣{な}れた作業{さぎょう}ほど油断{ゆだん}がある。まず「第{だい}1ラウンド：現状把握{げんじょうはあく}」、どんな危険{きけん}が潜{ひそ}んでいるかな？',
        'Familiar work breeds carelessness. First is Round 1: Grasp the situation. What hazards are hidden here?',
        'চেনা কাজই অসতর্কতা ডেকে আনে। প্রথমে ১ম রাউন্ড: পরিস্থিতি বোঝা। এখানে কী কী লুকানো বিপদ আছে?',
      ),
    },
    {
      who: 'worker',
      text: t(
        '足元{あしもと}に溶接{ようせつ}ケーブルがあり、すぐ横{よこ}をフォークリフトが通{とお}ります。',
        'There are welding cables underfoot, and forklifts pass right next to us.',
        'পায়ের কাছে ওয়েল্ডিংয়ের তার ছড়ানো, আর পাশ দিয়েই ফর্কলিফট চলাচল করছে।',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'いい気{き}づきだ。では「第{だい}2ラウンド：本質追究{ほんしつついきゅう}」、一番{いちばん}危{あぶ}ないポイントはどこだろう？',
        'Good eye. Now Round 2: Find the critical hazard. What is the most dangerous point?',
        'ভালো ধরেছো। এবার ২য় রাউন্ড: মূল বিপদ চিহ্নিত করা। সবচেয়ে মারাত্মক বিপদ কোনটি?',
      ),
    },
    {
      who: 'worker',
      text: t(
        'ケーブルをまたげば大丈夫{だいじょうぶ}だから、急{いそ}いでパイプを運{はこ}べばいいですか？',
        'If I just step over the cable, is it fine to carry the pipe quickly?',
        'তারটা ডিঙিয়ে গেলেই চলবে, তাড়াতাড়ি পাইপটা বয়ে নিয়ে গেলে হয় না?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'だめだよ。重{おも}い物{もの}を持{も}ってつまずいたら、リフトの通路{つうろ}に倒{たお}れてしまう。それが危険{きけん}のポイント（◎）だ。',
        'No. If you trip while carrying a heavy pipe, you will fall into the forklift lane. That is our critical hazard (◎).',
        'না, তা নয়। ভারী জিনিস নিয়ে হোঁচট খেলে সোজা ফর্কলিফটের পথে গিয়ে পড়বে। এটাই মূল বিপদ (◎)।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'わかりました！「第{だい}3ラウンド：対策樹立{たいさくじゅりつ}」として、ケーブルをフックで上{うえ}に通{とお}し、足元{あしもと}を片付{かたづ}けます。',
        'Understood! For Round 3: Set countermeasures, we hang cables on hooks overhead and clear the floor.',
        'বুঝেছি! ৩য় রাউন্ড: প্রতিকার নির্ধারণ হিসেবে, তারগুলো হুকে ঝুলিয়ে দিয়ে পায়ের নিচ পরিষ্কার করব।',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'よし！最後{さいご}は「第{だい}4ラウンド：目標設定{もくひょうせってい}」だ。「足元{あしもと}クリア、声{こえ}かけヨシ！」でゼロ災{さい}で行{い}こう！',
        'Good! Finally Round 4: Team goal. Let\'s aim for zero accidents with "Footing clear, call out, yoshi!"',
        'বেশ! শেষে ৪র্থ রাউন্ড: দলের লক্ষ্য নির্ধারণ। "পায়ের নিচে সাফ, ডাক দিয়ে ঠিক আছে!" বলে দুর্ঘটনা শূন্যে রাখব!',
      ),
    },
    {
      who: 'worker',
      text: t(
        '「足元{あしもと}クリア、声{こえ}かけヨシ！」ゼロ災{さい}で行{い}こう、ヨシ！',
        '"Footing clear, call out, yoshi!" Zero accidents, let\'s go, yoshi!',
        '"পায়ের নিচে সাফ, ডাক দিয়ে ঠিক আছে!" জিরো অ্যাক্সিডেন্ট, চলো, ঠিক আছে!',
      ),
    },
  ],
  handbook: [
    t(
      'KY活動{かつどう}は、毎朝{まいあさ}の作業前{さぎょうまえ}にチーム全員{ぜんいん}で必{かなら}ず行{おこな}う。',
      'Carry out KY activity every morning before work with the entire team without fail.',
      'প্রতিদিন সকালে কাজ শুরুর আগে দলের সবাই মিলে অবশ্যই কেওয়াই করবে।',
    ),
    t(
      '「自分{じぶん}だけは大丈夫{だいじょうぶ}」という思{おも}い込{こ}み（油断{ゆだん}）を捨{す}てる。',
      'Discard the careless assumption of "it will never happen to me."',
      '"আমার কিছু হবে না" এমন অসতর্ক মনোভাব মন থেকে দূর করো।',
    ),
    t(
      'KY四{よん}ラウンド法{ほう}の手順{てじゅん}（現状把握{げんじょうはあく}・本質追究{ほんしつついきゅう}・対策樹立{たいさくじゅりつ}・目標設定{もくひょうせってい}）を守{まも}る。',
      'Follow the 4-round KY method: grasp situation, find key hazard, set measures, set team goal.',
      'কেওয়াই চার-রাউন্ড নিয়ম (পরিস্থিতি, মূল বিপদ, সমাধান, দলের লক্ষ্য) মেনে চলো।',
    ),
    t(
      '一番{いちばん}危険{きけん}なポイントには「◎」をつけ、全員{ぜんいん}で共有{きょうゆう}する。',
      'Mark the most critical hazard with ◎ and share it with everyone.',
      'সবচেয়ে বিপজ্জনক বিষয়টিতে ◎ চিহ্নিত করে সবাই জেনে নাও।',
    ),
    t(
      '対策{たいさく}は「注意{ちゅうい}する」だけでなく、具体的{ぐたいてき}な行動{こうどう}で決{き}める。',
      'Set countermeasures as concrete actions, never just vague advice to "be careful."',
      'প্রতিকার শুধু "সাবধান থাকব" নয়, নির্দিষ্ট কার্যকর কাজের মাধ্যমে ঠিক করতে হবে।',
    ),
    t(
      '作業前{さぎょうまえ}には指差呼称{しさこしょう}で対象{たいしょう}を指{ゆび}さし、「○○、ヨシ！」と声{こえ}を出{だ}す。',
      'Before work, point at the target and call out "... yoshi!" with pointing and calling.',
      'কাজ শুরুর আগে হাত দিয়ে ইশারা করে জোরে বলো "...ঠিক আছে!" (ইয়োশি)।',
    ),
    t(
      '危険{きけん}を感{かん}じたり手順{てじゅん}と違{ちが}うときは、作業{さぎょう}を止{と}めて職長{しょくちょう}に相談{そうだん}する。',
      'If you sense danger or something differs from procedure, stop work and consult the foreman.',
      'বিপদ দেখলে বা নিয়মের হেরফের হলে কাজ থামিয়ে ফোরম্যানকে জানাও।',
    ),
    t(
      'ヒヤリ・ハットの経験{けいけん}は隠{かく}さず、ミーティングで報告{ほうこく}して対策{たいさく}につなげる。',
      'Never hide near-miss incidents; report them in meetings to build countermeasures.',
      'দুর্ঘটনা ঘটতে ঘটতে বেঁচে যাওয়ার ঘটনা না লুকিয়ে মিটিংয়ে জানিয়ে প্রতিকার নাও।',
    ),
    t(
      '急{いそ}いでいるときや疲{つか}れているときこそ、立{た}ち止{ど}まってKYを行{おこな}う。',
      'Stop and perform KY especially when rushed or tired.',
      'ব্যস্ত বা ক্লান্ত থাকলে বিশেষভাবে থেমে কেওয়াই করো।',
    ),
    t(
      '最後{さいご}は「ゼロ災{さい}で行{い}こう、ヨシ！」の唱和{しょうわ}でチームの気合{きあ}いを合{あ}わせる。',
      'Unite the team spirit at the end by chanting "Zero accidents, let\'s go, yoshi!"',
      'সবশেষে "জিরো অ্যাক্সিডেন্ট, চলো, ঠিক আছে!" সমস্বরে ডেকে দলের মনোবল এক করো।',
    ),
  ],
  reel: [
    t(
      '足元{あしもと}、段差{だんさ}・油{あぶら}なし、ヨシ！',
      'Footing, no bumps or oil, yoshi!',
      'পায়ের নিচে খাঁদ ও তেল নেই, ঠিক আছে!',
    ),
    t(
      '保護具{ほごぐ}、あごひも締{し}め、ヨシ！',
      'PPE, chin strap fastened, yoshi!',
      'পিপিই, চিনস্ট্র্যাপ বাঁধা, ঠিক আছে!',
    ),
    t(
      '溶接{ようせつ}ケーブル、フック掛{か}け、ヨシ！',
      'Welding cables hung on hooks, yoshi!',
      'ওয়েল্ডিং কেবল হুকে ঝুলানো, ঠিক আছে!',
    ),
    t(
      'リフト通路{つうろ}、障害物{しょうがいぶつ}なし、ヨシ！',
      'Forklift lane, no obstacles, yoshi!',
      'ফর্কলিফটের পথ বাধামুক্ত, ঠিক আছে!',
    ),
    t(
      'バルブ、閉{へい}・ロックアウト、ヨシ！',
      'Valve closed and locked out, yoshi!',
      'ভাল্ব বন্ধ ও লকআউট করা, ঠিক আছে!',
    ),
    t(
      '合図{あいず}確認{かくにん}、周囲{しゅうい}よし、ヨシ！',
      'Signals confirmed, surroundings clear, yoshi!',
      'ইশারা নিশ্চিত, আশপাশ নিরাপদ, ঠিক আছে!',
    ),
    t(
      '今日{きょう}も一日{いちにち}、ゼロ災{さい}で行{い}こう、ヨシ！',
      'Zero accidents again today, let\'s go, yoshi!',
      'আজ সারাদিন দুর্ঘটনা শূন্যে রাখব, চলো, ঠিক আছে!',
    ),
  ],
  drills: [
    {
      id: 'kyt-spot-shipyard',
      kind: 'spot',
      secs: 30,
      need: 4,
      name: t(
        '造船{ぞうせん}ブロック溶接{ようせつ}のKYT',
        'Shipyard block welding KYT',
        'জাহাজ নির্মাণ ব্লকে ওয়েল্ডিংয়ের কেওয়াইটি',
      ),
      goal: t(
        '30秒{びょう}で潜{ひそ}む危険{きけん}を4つ見{み}つける。',
        'Find 4 hidden hazards in 30 seconds.',
        '৩০ সেকেন্ডে ৪টি লুকানো বিপদ চিহ্নিত করো।',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '足元{あしもと}に放置{ほうち}された溶接{ようせつ}ホルダー',
            'Welding holder left on the floor',
            'মেঝেতে ফেলে রাখা ওয়েল্ডিং হোল্ডার',
          ),
        },
        {
          hazard: false,
          label: t(
            '整理整頓{せいりせいとん}された配管{はいかん}ラック',
            'Pipes organized in a storage rack',
            'র‍্যাকে গুছিয়ে রাখা পাইপ',
          ),
        },
        {
          hazard: true,
          label: t(
            '被覆{ひふく}が破{やぶ}れた電源{でんげん}コード',
            'Power cord with damaged insulation',
            'ছেঁড়া কভারযুক্ত বিদ্যুতের তার',
          ),
        },
        {
          hazard: false,
          label: t(
            '点検済{てんけんずみ}の消火器{しょうかき}',
            'Inspected fire extinguisher',
            'পরীক্ষা করা অগ্নিনির্বাপক',
          ),
        },
        {
          hazard: true,
          label: t(
            '手すりの外{そと}に身{み}を乗{の}り出{だ}す作業者{さぎょうしゃ}',
            'Worker leaning out past the handrail',
            'রেলিংয়ের বাইরে ঝুঁকে কাজ করা কর্মী',
          ),
        },
        {
          hazard: true,
          label: t(
            '転倒防止{てんとうぼうし}チェーンのないガスボンベ',
            'Gas cylinder without anti-tip chain',
            'চেইন ছাড়া রাখা গ্যাস সিলিন্ডার',
          ),
        },
        {
          hazard: false,
          label: t(
            '防じんマスクの正{ただ}しい着用{ちゃくよう}',
            'Correctly wearing a dust mask',
            'সঠিকভাবে পরা ডাস্ট মাস্ক',
          ),
        },
        {
          hazard: true,
          label: t(
            'フォークリフト通路{つうろ}に置{お}かれた資材{しざい}',
            'Materials placed in forklift lane',
            'ফর্কলিফটের পথে রাখা মালামাল',
          ),
        },
        {
          hazard: false,
          label: t(
            '親綱{おやづな}に掛{か}けた安全帯{あんぜんたい}のフック',
            'Safety harness hook fastened to lifeline',
            'সেফটি লাইনে আটকানো হারনেস হুক',
          ),
        },
      ],
    },
    {
      id: 'kyt-4round-steps',
      kind: 'seq',
      name: t(
        'KY四{よん}ラウンド法{ほう}の手順{てじゅん}',
        'KY 4-round method steps',
        'কেওয়াই ৪-রাউন্ড পদ্ধতির ধাপ',
      ),
      goal: t(
        'KY四{よん}ラウンドを正{ただ}しい順番{じゅんばん}に並{なら}べる。',
        'Arrange the 4 KY rounds in the correct order.',
        'কেওয়াই ৪টি রাউন্ড সঠিক ক্রমে সাজাও।',
      ),
      steps: [
        t(
          '第{だい}1ラウンド：現状把握{げんじょうはあく}（潜{ひそ}む危険{きけん}の洗{あら}い出{だ}し）',
          'Round 1: Grasp situation (identify latent hazards)',
          '১ম রাউন্ড: পরিস্থিতি বোঝা (লুকানো বিপদ চিহ্নিত করা)',
        ),
        t(
          '第{だい}2ラウンド：本質追究{ほんしつついきゅう}（危険{きけん}のポイントに◎をつける）',
          'Round 2: Find key hazard (mark critical point with ◎)',
          '২য় রাউন্ড: মূল বিপদ চিহ্নিত করা (মূল বিপদে ◎ দাগ দেওয়া)',
        ),
        t(
          '第{だい}3ラウンド：対策樹立{たいさくじゅりつ}（具体的{ぐたいてき}な行動計画{こうどうけいかく}の策定{さくてい}）',
          'Round 3: Set countermeasures (plan concrete actions)',
          '৩য় রাউন্ড: প্রতিকার নির্ধারণ (নির্দিষ্ট কাজের পরিকল্পনা তৈরি)',
        ),
        t(
          '第{だい}4ラウンド：目標設定{もくひょうせってい}（チーム行動目標{こうどうもくひょう}を決{き}めて指差呼称{しさこしょう}）',
          'Round 4: Set team goal (set team action goal and point-call)',
          '৪র্থ রাউন্ড: লক্ষ্য নির্ধারণ (দলের কাজের লক্ষ্য ঠিক করে হাত দিয়ে ইশারা)',
        ),
      ],
    },
    {
      id: 'tbm-flow-steps',
      kind: 'seq',
      name: t(
        '朝{あさ}のTBMミーティングの流れ',
        'Morning TBM meeting flow',
        'সকালের টিবিএম মিটিং পরিচালনার ক্রম',
      ),
      goal: t(
        '作業前{さぎょうまえ}ミーティングの手順{てじゅん}を正{ただ}しい順番{じゅんばん}に並{なら}べる。',
        'Arrange the pre-work meeting steps in the correct order.',
        'কাজ শুরুর আগের মিটিংয়ের ধাপগুলো সঠিক ক্রমে সাজাও।',
      ),
      steps: [
        t(
          '健康状態{けんこうじょうたい}と保護具{ほごぐ}の相互確認{そうごかくにん}',
          'Mutual check of health and PPE condition',
          'শারীরিক সুস্থতা ও একে অপরের পিপিই পরীক্ষা',
        ),
        t(
          '今日{きょう}の作業内容{さぎょうないよう}と役割分担{やくわりぶんたん}の伝達{でんたつ}',
          'Explain today\'s work scope and role assignments',
          'আজকের কাজের বিষয় ও দায়িত্ব বণ্টন জানানো',
        ),
        t(
          '現場{げんば}の危険予知{きけんよち}（KY四{よん}ラウンド）を実施{じっし}',
          'Conduct on-site KY hazard prediction (4 rounds)',
          'কাজের জায়গায় কেওয়াই ৪টি রাউন্ড অনুশীলন',
        ),
        t(
          'チーム行動目標{こうどうもくひょう}と指差呼称{しさこしょう}項目{こうもく}の決定{けってい}',
          'Decide team action goal and pointing-call phrase',
          'দলের কাজের লক্ষ্য ও ইশারার বাক্য নির্ধারণ',
        ),
        t(
          '「ゼロ災{さい}で行{い}こう、ヨシ！」の唱和{しょうわ}で作業開始{さぎょうかいし}',
          'Chant "Zero accidents, let\'s go, yoshi!" and start work',
          '"জিরো অ্যাক্সিডেন্ট, চলো, ঠিক আছে!" সমস্বরে ডেকে কাজ শুরু',
        ),
      ],
    },
  ],
  inspect: [
    {
      id: 'kyt-blackboard',
      defective: false,
      name: t(
        'KY活動黒板{かつどうこくばん}',
        'KY activity blackboard',
        'কেওয়াই কার্যক্রমের ব্ল্যাকবোর্ড',
      ),
      spots: [
        {
          x: 28,
          y: 35,
          check: t(
            '危険{きけん}のポイント（◎）が1つに絞{しぼ}られていること',
            'Critical hazard (◎) is narrowed down to one key point',
            'মূল বিপদের স্থানে একটিমাত্র ◎ নির্দিষ্ট করা আছে',
          ),
        },
        {
          x: 72,
          y: 65,
          check: t(
            'チーム行動目標{こうどうもくひょう}と指差呼称{しさこしょう}が具体的{ぐたいてき}に書{か}かれていること',
            'Team action goal and call phrase are written concretely',
            'দলের কাজের লক্ষ্য ও ইশারার বাক্য নির্দিষ্টভাবে লেখা আছে',
          ),
        },
      ],
    },
    {
      id: 'kyt-sheet-check',
      defective: true,
      name: t(
        'KY活動指示書{かつどうしじしょ}',
        'KY activity record sheet',
        'কেওয়াই কার্যক্রম নির্দেশিকা শীট',
      ),
      spots: [
        {
          x: 32,
          y: 42,
          check: t(
            '対策{たいさく}が「注意{ちゅうい}する」だけの抽象表現{ちゅうしょうひょうげん}ではなく具体的{ぐたいてき}な行動{こうどう}か',
            'Measures specify concrete actions rather than vague "be careful"',
            'প্রতিকারে শুধুই "সাবধান থাকা" না লিখে নির্দিষ্ট কাজ লেখা আছে কিনা',
          ),
        },
        {
          x: 68,
          y: 72,
          check: t(
            '参加者全員{さんかしゃぜんいん}のサインと職長{しょくちょう}の確認印{かくにんいん}があるか',
            'All participant signatures and foreman confirmation stamp are present',
            'অংশগ্রহণকারী সবার স্বাক্ষর ও ফোরম্যানের অনুমোদন সিল আছে কিনা',
          ),
        },
      ],
    },
    {
      id: 'grinder-safety-inspect',
      defective: true,
      name: t(
        '高速{こうそく}グラインダーの作業前点検{さぎょうまえてんけん}',
        'High-speed grinder pre-use inspection',
        'হাই-স্পিড গ্রাইন্ডারের কাজ শুরুর আগের পরীক্ষা',
      ),
      spots: [
        {
          x: 25,
          y: 28,
          check: t(
            'といし安全覆{あんぜんおお}い（カバー）が正{ただ}しく固定{こてい}されているか',
            'Abrasive wheel safety guard is securely mounted and positioned',
            'হুইলের সেফটি গার্ড সঠিকভাবে লাগানো ও স্থির আছে কিনা',
          ),
        },
        {
          x: 75,
          y: 35,
          check: t(
            'といしにひび割{わ}れ・欠{か}けがなく外観{がいかん}が正常{せいじょう}か',
            'Abrasive wheel has no visible cracks or chipping',
            'হুইলে কোনো ফাটল বা ভাঙা অংশ নেই এবং দেখতে ত্রুটিমুক্ত কিনা',
          ),
        },
        {
          x: 45,
          y: 78,
          check: t(
            '電源{でんげん}コードに芯線{しんせん}の露出{ろしゅつ}や深{ふか}い傷{きず}がないか',
            'Power cord has no exposed inner wires or deep cuts',
            'পাওয়ার কর্ডে ভেতরের তার বের হয়ে থাকা বা গভীর ক্ষত নেই কিনা',
          ),
        },
      ],
    },
  ],
  quiz: [
    {
      q: t(
        '毎朝{まいあさ}の作業前{さぎょうまえ}にKY活動{かつどう}を行{おこな}う最{もっと}も重要{じゅうよう}な目的{もくてき}は何{なん}ですか？',
        'What is the most important purpose of conducting KY activity every morning before work?',
        'প্রতিদিন সকালে কাজ শুরুর আগে কেওয়াই কার্যক্রম করার সবচেয়ে প্রধান উদ্দেশ্য কী?',
      ),
      options: [
        t(
          '作業{さぎょう}のスピードを上{あ}げてノルマを早{はや}く達成{たっせい}するため',
          'To increase work speed and meet daily quotas earlier',
          'কাজের গতি বাড়িয়ে দৈনিক কোটা দ্রুত পূরণ করার জন্য',
        ),
        t(
          '潜在的{せんざいてき}な危険{きけん}をチームで予知{よち}し、災害{さいがい}を未然{みぜん}に防{ふせ}ぐため',
          'To predict latent hazards as a team and prevent accidents before they occur',
          'দলে সম্ভাব্য বিপদ আগে থেকে অনুমান করে দুর্ঘটনা ঘটার আগেই তা রোধ করার জন্য',
        ),
        t(
          '職長{しょくちょう}が作業員{さぎょういん}のミスを記録{きろく}して減給{げんきゅう}するため',
          'For the foreman to record worker mistakes and deduct wages',
          'ফোরম্যান যাতে কর্মীদের ভুল নথিভুক্ত করে বেতন কাটতে পারেন তার জন্য',
        ),
        t(
          '朝礼{ちょうれい}の形式{けいしき}としてサインだけを集{あつ}めるため',
          'Merely to collect signatures as a morning meeting formality',
          'সকালের মিটিংয়ের আনুষ্ঠানিকতা হিসেবে কেবল স্বাক্ষর সংগ্রহ করার জন্য',
        ),
      ],
      answer: 1,
      why: t(
        'KY活動{かつどう}は、作業前{さぎょうまえ}に潜{ひそ}む危険{きけん}をチームで発見{はっけん}・共有{きょうゆう}し、労働災害{ろうどうさいがい}を未然{みぜん}に防{ふせ}ぐために行{おこな}います。',
        'KY activity is conducted to spot and share hidden hazards before starting work, preventing workplace accidents.',
        'কেওয়াই কার্যক্রমের মূল লক্ষ্য হলো কাজ শুরুর আগে লুকানো বিপদগুলো দলে শনাক্ত ও ভাগাভাগি করে দুর্ঘটনা আগেই ঠেকানো।',
      ),
    },
    {
      q: t(
        'KY四{よん}ラウンド法{ほう}の「第{だい}2ラウンド（本質追究{ほんしつついきゅう}）」では何{なに}をしますか？',
        'What is done in Round 2 (Finding the critical hazard) of the 4-round KY method?',
        'কেওয়াই চার-রাউন্ড পদ্ধতির "২য় রাউন্ড (মূল বিপদ অনুসন্ধান)"-এ কী করা হয়?',
      ),
      options: [
        t(
          'すぐに作業{さぎょう}を開始{かいし}して手順{てじゅん}を短縮{たんしゅく}する',
          'Immediately begin work and shorten work procedures',
          'দেরি না করে তৎক্ষণাৎ কাজ শুরু করে কাজের ধাপ কমিয়ে ফেলা',
        ),
        t(
          '新{あたら}しい工具{こうぐ}の購入{こうにゅう}予算{よさん}を計算{けいさん}する',
          'Calculate the purchase budget for new replacement tools',
          'নতুন যন্ত্রপাতি কেনার বাজেট হিসাব করা',
        ),
        t(
          '洗{あら}い出{だ}した危険{きけん}の中{なか}から最{もっと}も重大{じゅうだい}な危険{きけん}ポイント（◎）を絞{しぼ}り込{こ}む',
          'Narrow down to the single most critical danger point and mark it with ◎',
          'খুঁজে পাওয়া বিপদগুলোর মধ্য থেকে সবচেয়ে মারাত্মক বিপদের পয়েন্টটি বেছে নিয়ে ◎ দাগ দেওয়া',
        ),
        t(
          '過去{かこ}に失敗{しっぱい}した人{ひと}の責任{せきにん}を追及{ついきゅう}する',
          'Investigate and hold individuals responsible for past mistakes',
          'অতীতে ভুল করা ব্যক্তির ওপর দোষ চাপানো',
        ),
      ],
      answer: 2,
      why: t(
        '第{だい}2ラウンドでは、洗{あら}い出{だ}した危険要因{きけんよういん}から「これが危険{きけん}のポイントだ」という最重要{さいじゅうよう}の項目{こうもく}に◎をつけてチームで合意{ごうい}します。',
        'In Round 2, the team narrows down the identified hazards to the critical point and marks it with ◎.',
        '২য় রাউন্ডে প্রাপ্ত বিপদগুলো থেকে সবচেয়ে মারাত্মক বিষয়টিকে বেছে নিয়ে ◎ চিহ্নিত করে সবাই একমত হয়।',
      ),
    },
    {
      q: t(
        '第{だい}3ラウンド（対策樹立{たいさくじゅりつ}）で決{き}める対策{たいさく}として、最{もっと}も適切{てきせつ}なものはどれですか？',
        'Which of the following is the most appropriate countermeasure decided in Round 3?',
        '৩য় রাউন্ডে (প্রতিকার নির্ধারণ) ঠিক করার জন্য সবচেয়ে উপযুক্ত প্রতিকার কোনটি?',
      ),
      options: [
        t(
          '「足元{あしもと}のケーブルをフックで架空配線{かくうはいせん}し、通路{つうろ}に置{お}かない」',
          '"Hang floor cables overhead using hooks so they are kept off the walkway"',
          '"পায়ের নিচের তারগুলো হুকে ঝুলিয়ে উপর দিয়ে নাও, যাতে চলাচলের পথে না থাকে"',
        ),
        t(
          '「気{き}をつけて早足{はやあし}でまたぎながら運{はこ}ぶ」',
          '"Be careful and stride over them quickly while carrying loads"',
          '"ভারী জিনিস নিয়ে চলার সময় সাবধান হয়ে পা ফাঁক করে দ্রুত পার হও"',
        ),
        t(
          '「危{あぶ}ないから今日{きょう}は作業{さぎょう}を一切{いっさい}やらない」',
          '"Do not carry out any work today because it is dangerous"',
          '"বিপজ্জনক বিধায় আজকের দিনে কোনো কাজই করো না"',
        ),
        t(
          '「怪我{けが}をしないように心{こころ}の中{なか}で強{つよ}く念{ねん}じる」',
          '"Wish strongly in your heart that nobody gets hurt"',
          '"মনে মনে প্রার্থনা করো যাতে কেউ আঘাত না পায়"',
        ),
      ],
      answer: 0,
      why: t(
        '安全対策{あんぜんたいさく}は「注意{ちゅうい}する」という心構{こころがま}えではなく、誰{だれ}でも実行{じっこう}できる具体的{ぐたいてき}な行動{こうどう}で決{き}める必要{ひつよう}があります。',
        'Safety measures must be concrete actions anyone can execute, not merely vague intentions to "be careful."',
        'নিরাপত্তা প্রতিকার শুধু মনে মনে "সাবধান থাকা" নয়, এমন নির্দিষ্ট কাজ হতে হবে যা যে কেউই বাস্তবায়ন করতে পারে।',
      ),
    },
    {
      q: t(
        '作業前{さぎょうまえ}に行{おこな}う「指差呼称{しさこしょう}」に高{たか}い安全効果{あんぜんこうか}がある理由{りゆう}は何{なん}ですか？',
        'Why does "pointing and calling" before work provide a strong safety effect?',
        'কাজ শুরুর আগে "আঙুল দিয়ে নির্দেশ ও মুখে বলা (শিসা-কোশো)" এত কার্যকর নিরাপত্তা দেওয়ার কারণ কী?',
      ),
      options: [
        t(
          '大声{おおごえ}を出{だ}して周囲{しゅうい}の作業者{さぎょうしゃ}を驚{おどろ}かせるため',
          'To surprise and startle surrounding coworkers with loud shouting',
          'জোরে চিৎকার করে চারপাশের কর্মীদের চমকে দেওয়ার জন্য',
        ),
        t(
          '防犯{ぼうはん}カメラに作業{さぎょう}をしている証拠{しょうこ}を残{のこ}すため',
          'To leave video evidence on security cameras that work is in progress',
          'সিকিউরিটি ক্যামেরায় কাজ করার প্রমাণ রেকর্ড করে রাখার জন্য',
        ),
        t(
          '時計{とけい}を見{み}ずに正確{せいかく}な時間{じかん}を測{はか}るため',
          'To measure accurate time intervals without looking at a clock',
          'ঘড়ি না দেখেই নির্দিষ্ট সময় পরিমাপ করার জন্য',
        ),
        t(
          '目{め}・指{ゆび}・口{くち}・耳{みみ}を総動員{そうどういん}して意識{いしき}を高{たか}め、確認{かくにん}のミスを大幅{おおはば}に減{へ}らすため',
          'To mobilize eyes, fingers, mouth, and ears to raise consciousness and sharply reduce mistakes',
          'চোখ, আঙুল, মুখ ও কান একসাথে ব্যবহার করে মনোযোগ তুঙ্গে নেওয়া ও ভুলের মাত্রা কমিয়ে আনা',
        ),
      ],
      answer: 3,
      why: t(
        '対象{たいしょう}を指差{ゆびさ}して声{こえ}に出{だ}し自分{じぶん}の耳{みみ}で確認{かくにん}することで、脳{のう}の覚醒水準{かくせいすいじゅん}が高{たか}まり不注意{ふちゅうい}事故{じこ}を防{ふせ}ぎます。',
        'Pointing, calling out, and hearing one\'s own voice stimulates the brain and drastically reduces errors.',
        'বস্তু নির্দেশ করে মুখে বলে নিজের কানে তা শুনলে মস্তিষ্কের সতর্কতা বৃদ্ধি পায় এবং অসতর্কতাজনিত দুর্ঘটনা কমে।',
      ),
    },
    {
      q: t(
        '重量物{じゅうりょうぶつ}（20kg以上{いじょう}の配管{はいかん}など）を手{て}で運{はこ}ぶ際{さい}、最{もっと}も予測{よそく}すべき危険{きけん}は何{なん}ですか？',
        'When manually carrying heavy objects (pipes over 20 kg), what hazard should be anticipated most?',
        'ভারী বস্তু (২০ কেজির বেশি পাইপ ইত্যাদি) হাতে বহন করার সময় কোন বিপদটি সবচেয়ে বেশি অনুমান করা উচিত?',
      ),
      options: [
        t(
          '作業着{さぎょうぎ}に鉄{てつ}の粉{こな}がついて洗濯{せんたく}が大変{たいへん}になること',
          'Iron dust getting on work clothes, making washing difficult',
          'কাজের পোশাকে লোহার গুঁড়ো লেগে কাপড় ধোয়া কঠিন হয়ে যাওয়া',
        ),
        t(
          '腰{こし}を曲{ま}げた無理{むり}な姿勢{しせい}での持{も}ち上{あ}げによる腰痛{ようつう}や、足先{あしさき}への落下{らっか}',
          'Lower back injury from lifting with a bent posture, or dropping the load onto toes',
          'কোমর বাঁকিয়ে ভুল ভঙ্গিতে ওঠানোর ফলে কোমরে চোট লাগা বা পায়ের আঙুলের ওপর পড়ে যাওয়া',
        ),
        t(
          '鉄{てつ}の表面{ひょうめん}が冷{つめ}たくて手{て}がかじかむこと',
          'The cold iron surface making your hands uncomfortably chilled',
          'লোহার উপরিভাগ ঠান্ডা থাকায় হাত অসাড় লাগা',
        ),
        t(
          '運{はこ}び終{お}わったあとに喉{のど}が渇{かわ}くこと',
          'Feeling thirsty after finishing the carrying task',
          'মাল বহন শেষ করার পর খুব তৃষ্ণা পাওয়া',
        ),
      ],
      answer: 1,
      why: t(
        '重量物{じゅうりょうぶつ}の運搬{うんぱん}では腰痛{ようつう}災害{さいがい}と落下{らっか}による足{あし}の骨折{こっせつ}が頻発{ひんぱつ}します。膝{ひざ}を曲{ま}げて抱{かか}え、安全靴{あんぜんぐつ}を必{かなら}ず履{は}きます。',
        'Heavy lifting frequently causes lumbar spine injuries and toe fractures. Bend your knees to lift and always wear safety shoes.',
        'ভারী মালামাল বহনে প্রায়ই কোমরে আঘাত ও পড়ে গিয়ে পায়ের হাড় ভাঙার ঘটনা ঘটে। হাঁটু ভেঙে তুলুন ও অবশ্যই সেফটি জুতো পরুন।',
      ),
    },
    {
      q: t(
        '作業中{さぎょうちゅう}にヒヤリとしたりハッとしたこと（ヒヤリ・ハット）があったとき、どうすべきですか？',
        'When you experience a close call or near-miss (hiyari-hatto) during work, what should you do?',
        'কাজের সময় দুর্ঘটনা ঘটতে ঘটতে বেঁচে যাওয়ার (হিয়ারি-হাট্টো) মতো কিছু হলে আপনার কী করা উচিত?',
      ),
      options: [
        t(
          '怒{いか}られるのが怖{こわ}いので、誰{だれ}にも話{はな}さず秘密{ひみつ}にする',
          'Keep it a secret and tell no one out of fear of being scolded',
          'বকা খাওয়ার ভয়ে কাউকে না জানিয়ে গোপন রাখা',
        ),
        t(
          '実際{じっさい}に怪我{けが}はしなかったので、何{なに}も気{き}にせず忘{わす}れる',
          'Forget about it completely since no actual injury occurred',
          'যেহেতু কোনো সত্যিকারের আঘাত লাগেনি, তাই তা ভুলে যাওয়া',
        ),
        t(
          '職長{しょくちょう}に報告{ほうこく}し、KYやミーティングで共有{きょうゆう}して再発防止策{さいはつぼうしさく}を立{た}てる',
          'Report it to the foreman and share it in KY meetings to formulate prevention measures',
          'ফোরম্যানকে জানিয়ে কেওয়াই বা মিটিংয়ে সবার সাথে শেয়ার করে প্রতিরোধের ব্যবস্থা নেওয়া',
        ),
        t(
          '自分{じぶん}のSNSに投稿{とうこう}して注意{ちゅうい}を呼{よ}びかける',
          'Post about it on personal social media to raise awareness',
          'সতর্ক করার নামে নিজের সোশ্যাল মিডিয়ায় পোস্ট করা',
        ),
      ],
      answer: 2,
      why: t(
        '重大事故{じゅうだいじこ}の背景{はいけい}には多数{たすう}のヒヤリ・ハットが潜{ひそ}んでいます。隠{かく}さず共有{きょうゆう}して対策{たいさく}を打{う}つことが命{いのち}を守{まも}ります。',
        'Many near-misses underlie every major accident. Reporting and addressing them openly saves lives.',
        'প্রতিটি মারাত্মক দুর্ঘটনার পেছনে অসংখ্য ছোটখাটো ঝুঁকি বা বেঁচে যাওয়ার ঘটনা থাকে। না লুকিয়ে প্রকাশ করলেই জীবন বাঁচে।',
      ),
    },
    {
      q: t(
        'KY第{だい}4ラウンド（目標設定{もくひょうせってい}）で決{き}める「チーム行動目標{こうどうもくひょう}」として最善{さいぜん}のものはどれですか？',
        'Which of the following is the best "team action goal" established in KY Round 4?',
        'কেওয়াই ৪র্থ রাউন্ডে (লক্ষ্য নির্ধারণ) ঠিক করার মতো সর্বোত্তম "দলের কাজের লক্ষ্য" কোনটি?',
      ),
      options: [
        t(
          '「足元{あしもと}クリア、声{こえ}かけヨシ！」のように短{みじか}く具体的{ぐたいてき}なもの',
          'A short, concrete phrase like "Footing clear, call out, yoshi!"',
          '"পায়ের নিচে সাফ, ডাক দিয়ে ঠিক আছে!" এর মতো সংক্ষিপ্ত ও নির্দিষ্ট লক্ষ্য',
        ),
        t(
          '「今日{きょう}も一日{いちにち}なんとなく気{き}をつけて頑張{がんば}ろう」という抽象的{ちゅうしょうてき}なもの',
          'A vague, abstract slogan like "Let\'s just do our best carefully today somehow"',
          '"আজ সারাদিন যেভাবেই হোক একটু সাবধানে ভালো করে কাজ করি" এমন অনির্দিষ্ট বাক্য',
        ),
        t(
          '長{なが}すぎて誰{だれ}も暗唱{あんしょう}できない200文字{もじ}のスローガン',
          'A 200-character long manifesto that no worker can recite',
          '২০০ অক্ষরের এক বিশাল রচনা যা কোনো কর্মীই মুখস্থ রাখতে পারে না',
        ),
        t(
          '作業員{さぎょういん}同士{どうし}で作業{さぎょう}スピードの速{はや}さを競{きそ}い合{あ}う目標{もくひょう}',
          'A competitive goal urging workers to race each other for speed',
          'কে কার চেয়ে দ্রুত কাজ শেষ করতে পারে তার প্রতিযোগিতা করার লক্ষ্য',
        ),
      ],
      answer: 0,
      why: t(
        'チーム行動目標{こうどうもくひょう}は、全員{ぜんいん}が一息{ひといき}で唱和{しょうわ}でき、作業中{さぎょうちゅう}にいつでも実践{じっせん}できる簡潔{かんけつ}な表現{ひょうげん}にします。',
        'Team goals must be concise enough for everyone to recite in one breath and practice instantly while working.',
        'দলের কাজের লক্ষ্য এক নিঃশ্বাসে সমস্বরে বলা যায় এবং কাজ চলাকালে অবিলম্বে কাজে লাগানো যায় এমন সংক্ষিপ্ত হতে হবে।',
      ),
    },
    {
      q: t(
        'クレーン作業{さぎょう}の現場{げんば}で、吊{つ}り荷{に}のワイヤー角度{かくど}が想定{そうてい}より大{おお}きく開{ひら}いているのを見{み}つけました。どうすべきですか？',
        'At a crane work site, you notice the sling wire angle is spread much wider than expected. What should you do?',
        'ক্রেন কাজের জায়গায় দেখতে পেলেন স্লিং তারের কোণ ধারণার চেয়ে অনেক বেশি ফাঁক হয়ে গেছে। কী করবেন?',
      ),
      options: [
        t(
          '自分{じぶん}には関係{かんけい}ない作業{さぎょう}なので見{み}て見{み}ぬふりをする',
          'Pretend not to see it since it is not your direct assignment',
          'নিজের দায়িত্ব নয় ভেবে দেখেও না দেখার ভান করে চলে যাওয়া',
        ),
        t(
          '吊{つ}り荷{に}の下{した}に駆{か}け込{こ}んで手{て}で押{お}さえる',
          'Rush under the suspended load to steady it with your bare hands',
          'ঝুলন্ত মালের নিচে দৌড়ে গিয়ে হাত দিয়ে ধরে স্থির রাখার চেষ্টা করা',
        ),
        t(
          '予定{よてい}の時間{じかん}に遅{おく}れそうなので合図{あいず}を急{いそ}がせる',
          'Hurry the signalman to lift quickly to avoid falling behind schedule',
          'দেরি হয়ে যাচ্ছে দেখে সিগন্যালম্যানকে দ্রুত তুলে ফেলতে তাড়া দেওয়া',
        ),
        t(
          '直{ただ}ちに「ストップ！」と大声{おおごえ}で合図{あいず}して作業{さぎょう}を止{と}め、職長{しょくちょう}に伝{つた}える',
          'Immediately shout "Stop!" to halt the operation and inform the foreman',
          'অবিলম্বে "স্টপ!" বলে চিৎকার দিয়ে কাজ থামিয়ে ফোরম্যানকে জানানো',
        ),
      ],
      answer: 3,
      why: t(
        '危険{きけん}な状態{じょうたい}を見{み}つけたときは、誰{だれ}でも作業{さぎょう}を一時停止{いちじていし}させる権利{けんり}と義務{ぎむ}があります（作業停止{さぎょうていし}の徹底{てってい}）。',
        'Anyone discovering an unsafe condition has the right and duty to halt work immediately.',
        'বিপজ্জনক পরিস্থিতি দেখলে যে কারোরই তৎক্ষণাৎ কাজ থামিয়ে দেওয়ার অধিকার ও নিরাপত্তামূলক বাধ্যবাধকতা রয়েছে।',
      ),
    },
    {
      q: t(
        '作業前{さぎょうまえ}に現場{げんば}で行{おこな}うTBM（ツールボックスミーティング）の説明{せつめい}として正{ただ}しいものはどれですか？',
        'Which statement correctly describes the TBM (Toolbox Meeting) held on site before work?',
        'কাজ শুরুর আগে কাজের জায়গায় অনুষ্ঠিত টিবিএম (টুলবক্স মিটিং) সম্পর্কে কোন তথ্যটি সঠিক?',
      ),
      options: [
        t(
          '作業場所{さぎょうばしょ}で作業直前{さぎょうちょくぜん}に5〜10分{ぷん}で行{おこな}う具体的{ぐたいてき}な安全打{あんぜんう}ち合{あ}わせ',
          'A 5-10 minute concrete safety briefing held at the work location right before starting',
          'কাজ শুরুর ঠিক আগে কাজের জায়গায় ৫-১০ মিনিটের সংক্ষিপ্ত কার্যকর নিরাপত্তা আলোচনা',
        ),
        t(
          '週末{しゅうまつ}に事務所{じむしょ}で丸一日{まるいちにち}かけて行{おこな}う筆記試験{ひっきしけん}',
          'A full-day written examination held in the main office over the weekend',
          'সপ্তাহের শেষে অফিসে সারাদিন ধরে নেওয়া লিখিত পরীক্ষা',
        ),
        t(
          '新{あたら}しい道具箱{どうぐばこ}をセールスマンから買{か}うための商談会{しょうだんかい}',
          'A business meeting to purchase new toolboxes from a traveling salesperson',
          'বিক্রেতার কাছ থেকে নতুন টুলবক্স কেনার বাণিজ্যিক মিটিং',
        ),
        t(
          '昼休{ひるやす}みにみんなで集{あつ}まって食事{しょくじ}をする懇親会{こんしんかい}',
          'A social gathering where coworkers gather during lunch break to eat',
          'দুপুরের খাবারের বিরতিতে সবাই মিলে একসাথে বসে খাওয়ার আড্ডা',
        ),
      ],
      answer: 0,
      why: t(
        'TBMは、作業直前{さぎょうちょくぜん}に現場{げんば}で作業内容{さぎょうないよう}や手順{てじゅん}、危険要因{きけんよういん}と対策{たいさく}を短時間{たんじかん}で確認{かくにん}する重要{じゅうよう}な活動{かつどう}です。',
        'A TBM is a crucial brief meeting held right at the job site before starting, reviewing tasks, risks, and countermeasures.',
        'টিবিএম হলো কাজ শুরুর ঠিক আগে কাজের স্থানে দাঁড়িয়ে কাজ, ঝুঁকি ও প্রতিকার সংক্ষেপে নিশ্চিত করার অত্যন্ত গুরুত্বপূর্ণ প্রক্রিয়া।',
      ),
    },
    {
      q: t(
        '造船所{ぞうせんじょ}の狭隘区画{きょうあいくかく}（タンク内{ない}など）で作業{さぎょう}する際{さい}、最{もっと}も警戒{けいかい}すべき危険{きけん}は何{なん}ですか？',
        'When working inside confined spaces (e.g., inside shipyard tanks), what hazard requires the highest vigilance?',
        'জাহাজ কারখানার সংকীর্ণ বদ্ধ স্থানে (যেমন ট্যাঙ্কের ভেতর) কাজ করার সময় কোন বিপদ সম্পর্কে সবচেয়ে বেশি সতর্ক থাকা আবশ্যক?',
      ),
      options: [
        t(
          'スマートフォンに電話{でんわ}がかかってきて作業{さぎょう}が中断{ちゅうだん}すること',
          'Work being interrupted by incoming calls on your smartphone',
          'স্মার্টফোনে ফোন এসে কাজে ব্যাঘাত ঘটা',
        ),
        t(
          '作業中{さぎょうちゅう}に喉{のど}が痛{いた}くなって大声{おおごえ}が出{で}なくなること',
          'Having a sore throat that prevents shouting loudly during work',
          'কাজের সময় গলা শুকিয়ে গিয়ে জোরে কথা বলতে না পারা',
        ),
        t(
          '酸素欠乏症{さんそけつぼうしょう}や有害{ゆうがい}ガスの滞留{たいりゅう}、火花{ひばな}による爆発{ばくはつ}・火災{かさい}',
          'Oxygen deficiency, buildup of toxic gas, or explosion/fire caused by sparks',
          'অক্সিজেনের ঘাটতি, বিষাক্ত গ্যাস জমে থাকা, অথবা স্ফুলিঙ্গ থেকে বিস্ফোরণ ও অগ্নিকাণ্ড',
        ),
        t(
          '船内{せんない}の照明{しょうめい}が明{あか}るすぎて目{め}が疲{つか}れること',
          'Interior lighting being too bright and straining your eyes',
          'ভেতরের বাতি অতিরিক্ত উজ্জ্বল হওয়ায় চোখের ক্লান্তি আসা',
        ),
      ],
      answer: 2,
      why: t(
        'タンクや船体内{せんたいない}などの密閉空間{みっぺいくうかん}では、換気不足{かんきぶそく}による酸欠{さんけつ}やガス爆発{ばくはつ}が命{いのち}に関{かか}わります。換気{かんき}と測定{そくてい}を徹底{てってい}します。',
        'In enclosed spaces like tanks, oxygen deficiency and gas explosions are life-threatening. Ventilation and measurement are essential.',
        'ট্যাঙ্ক বা আবদ্ধ স্থানে বাতাস চলাচলের অভাবে অক্সিজেনের ঘাটতি ও গ্যাস বিস্ফোরণ প্রাণঘাতী হতে পারে। বায়ু চলাচল ও গ্যাস পরিমাপ নিশ্চিত করুন।',
      ),
    },
    {
      q: t(
        'KYミーティングの最後{さいご}に行{おこな}う「タッチ・アンド・コール」の主{おも}な目的{もくてき}は何{なん}ですか？',
        'What is the primary purpose of "touch-and-call" performed at the end of a KY meeting?',
        'কেওয়াই মিটিংয়ের শেষে "টাচ অ্যান্ড কল (হাত মিলিয়ে সমস্বরে বলা)" করার প্রধান উদ্দেশ্য কী?',
      ),
      options: [
        t(
          'じゃんけんをして最初{さいしょ}に片付{かたづ}けをする人{ひと}を決{き}めるため',
          'To play rock-paper-scissors to decide who cleans up first',
          'কে প্রথমে পরিষ্কারের কাজ করবে তা লটারি করে ঠিক করার জন্য',
        ),
        t(
          '全員{ぜんいん}で手{て}や肩{かた}を重{かさ}ねて目標{もくひょう}を唱和{しょうわ}し、チームの一体感{いったいかん}と安全意識{あんぜんいしき}を高{たか}めるため',
          'To join hands/shoulders and chant the goal together, strengthening team unity and safety commitment',
          'সবাই একসাথে হাত বা কাঁধ মিলিয়ে লক্ষ্য মুখে উচ্চারণ করে দলের একতা ও নিরাপত্তা সতর্কতা জোরদার করার জন্য',
        ),
        t(
          '作業員{さぎょういん}の手袋{てぶくろ}が汚{よご}れていないか触{さわ}って調{しら}べるため',
          'To touch coworkers\' gloves to inspect whether they are soiled',
          'অন্যদের হাতের গ্লাভসে ময়লা লেগেছে কিনা তা স্পর্শ করে পরীক্ষা করার জন্য',
        ),
        t(
          'お互{たが}いの体温{たいおん}を測{はか}って風邪{かぜ}をひいていないか確{たし}かめるため',
          'To feel body heat and check whether anyone has caught a cold',
          'একে অপরের শরীরের তাপমাত্রা পরীক্ষা করে সর্দিজ্বর হয়েছে কিনা তা দেখার জন্য',
        ),
      ],
      answer: 1,
      why: t(
        'タッチ・アンド・コールは、スキンシップを通じて「みんなでゼロ災{さい}を達成{たっせい}する」という連帯感{れんたいかん}と責任感{せきにんかん}を高{たか}める手法{しゅほう}です。',
        'Touch-and-call uses physical touch to reinforce solidarity and shared responsibility for achieving zero accidents.',
        'টাচ অ্যান্ড কল পারস্পরিক স্পর্শের মাধ্যমে "সবাই মিলে দুর্ঘটনা শূন্য রাখব" এই দায়বদ্ধতা ও ঐক্যের অনুভূতি তৈরি করে।',
      ),
    },
    {
      q: t(
        '朝{あさ}のKYで日本語{にほんご}の指示{しじ}や危険箇所{きけんかしょ}がよく分{わ}からなかったとき、どう行動{こうどう}すべきですか？',
        'What should you do if you do not understand a Japanese instruction or hazard point during morning KY?',
        'সকালের কেওয়াইতে জাপানি ভাষার কোনো নির্দেশ বা ঝুঁকিপূর্ণ স্থান ভালোভাবে বুঝতে না পারলে আপনি কী করবেন?',
      ),
      options: [
        t(
          '分{わ}かったふりをして愛想笑{あいそわら}いでうなずいておく',
          'Pretend to understand and nod along with a polite smile',
          'বুঝেছি এমন ভান করে মুখে হাসি রেখে মাথা নাড়ানো',
        ),
        t(
          '怒{いか}られるのが嫌{いや}なので、勘{かん}に頼{たよ}って勝手{かって}に作業{さぎょう}を始{はじ}める',
          'Rely on your own guesswork and start working alone to avoid scolding',
          'বকা খাওয়ার ভয়ে নিজের অনুমানের ওপর ভরসা করে খেয়ালখুশিমতো কাজ শুরু করা',
        ),
        t(
          '昼休{ひるやす}みになってからスマートフォンで調{しら}べる',
          'Wait until lunch break to search about it on your smartphone',
          'দুপুরের বিরতি পর্যন্ত অপেক্ষা করে তারপর স্মার্টফোনে সার্চ করা',
        ),
        t(
          '「もう一度{いちど}お願{ねが}いします」と職長{しょくちょう}や先輩{せんぱい}に伝{つた}え、納得{なっとく}できるまで確認{かくにん}する',
          'Say "Once more please" to the foreman or senior worker and clarify until completely understood',
          'ফোরম্যান বা অভিজ্ঞ কর্মীকে "দয়া করে আরেকবার বলুন" বলে পুরোপুরি স্পষ্ট না হওয়া পর্যন্ত বুঝে নেওয়া',
        ),
      ],
      answer: 3,
      why: t(
        '曖昧{あいまい}な理解{りかい}のまま作業{さぎょう}することは極{きわ}めて危険{きけん}です。安全{あんぜん}に関{かか}わる指示{しじ}は何度{なんど}でも確認{かくにん}するのが正{ただ}しい行動{こうどう}です。',
        'Working with ambiguous understanding is extremely dangerous. Re-checking safety instructions repeatedly is the correct practice.',
        'অস্পষ্ট ধারণা নিয়ে কাজ করা অত্যন্ত ঝুঁকিপূর্ণ। নিরাপত্তার বিষয়ে বারবার জিজ্ঞাসা করে নিশ্চিত হওয়াই সঠিক নিয়ম।',
      ),
    },
  ],
  glossary: [
    {
      ja: '危険予知',
      reading: 'きけんよち',
      en: 'hazard prediction',
      bn: 'বিপদ পূর্বানুমান',
      note: t(
        '作業前{さぎょうまえ}に危険{きけん}を発見{はっけん}し予測{よそく}すること。',
        'Predicting and identifying hazards before starting work.',
        'কাজ শুরুর আগে লুকানো বিপদ খুঁজে বের করে তা অনুমান করা।',
      ),
    },
    {
      ja: '指差呼称',
      reading: 'しさこしょう',
      en: 'pointing and calling',
      bn: 'আঙুল দিয়ে নির্দেশ ও মুখে বলা',
      note: t(
        '対象{たいしょう}を指{ゆび}さし「○○ヨシ！」と声{こえ}に出{だ}して確認{かくにん}する動作{どうさ}。',
        'Safety procedure of pointing at the object and calling out "... yoshi!".',
        'লক্ষ্যবস্তুর দিকে আঙুল দেখিয়ে "...ঠিক আছে!" বলে মুখে নিশ্চিত করার পদ্ধতি।',
      ),
    },
    {
      ja: 'ゼロ災',
      reading: 'ぜろさい',
      en: 'zero accidents',
      bn: 'দুর্ঘটনা শূন্য',
      note: t(
        '職場{しょくば}からすべての人身災害{じんしんさいがい}をなくす全員参加{ぜんいんさんか}の運動{うんどう}。',
        'Campaign involving all workers to eliminate all workplace injuries.',
        'কাজের জায়গা থেকে সব ধরনের দুর্ঘটনা দূর করার দলগত আন্দোলন।',
      ),
    },
    {
      ja: '現状把握',
      reading: 'げんじょうはあく',
      en: 'grasping the situation (Round 1)',
      bn: 'পরিস্থিতি বোঝা (১ম রাউন্ড)',
      note: t(
        'KY第{だい}1ラウンド。どんな危険{きけん}が潜{ひそ}んでいるか見{み}つける。',
        'KY Round 1: finding what hazards are lurking in the workplace.',
        'কেওয়াই ১ম রাউন্ড: কী কী লুকানো বিপদ রয়েছে তা খুঁজে বের করা।',
      ),
    },
    {
      ja: '本質追究',
      reading: 'ほんしつついきゅう',
      en: 'finding the critical hazard (Round 2)',
      bn: 'মূল বিপদ অনুসন্ধান (২য় রাউন্ড)',
      note: t(
        'KY第{だい}2ラウンド。最{もっと}も危険{きけん}なポイントに◎をつける。',
        'KY Round 2: pinpointing the most dangerous hazard with ◎.',
        'কেওয়াই ২য় রাউন্ড: সবচেয়ে মারাত্মক বিপদটি চিহ্নিত করে ◎ দাগ দেওয়া।',
      ),
    },
    {
      ja: '対策樹立',
      reading: 'たいさくじゅりつ',
      en: 'setting countermeasures (Round 3)',
      bn: 'প্রতিকার নির্ধারণ (৩য় রাউন্ড)',
      note: t(
        'KY第{だい}3ラウンド。具体的{ぐたいてき}で実行{じっこう}できる解決策{かいけつさく}を決{き}める。',
        'KY Round 3: deciding concrete and actionable solutions.',
        'কেওয়াই ৩য় রাউন্ড: সুনির্দিষ্ট ও কার্যকর সমাধানের উপায় ঠিক করা।',
      ),
    },
    {
      ja: '目標設定',
      reading: 'もくひょうせってい',
      en: 'setting team goal (Round 4)',
      bn: 'লক্ষ্য নির্ধারণ (৪র্থ রাউন্ড)',
      note: t(
        'KY第{だい}4ラウンド。チーム全員{ぜんいん}で実践{じっせん}する行動目標{こうどうもくひょう}を決{き}める。',
        'KY Round 4: setting action goals practiced by the whole team.',
        'কেওয়াই ৪র্থ রাউন্ড: দলের সবাই মেনে চলার মতো কাজের লক্ষ্য ঠিক করা।',
      ),
    },
    {
      ja: '職長',
      reading: 'しょくちょう',
      en: 'foreman / site supervisor',
      bn: 'ফোরম্যান / কাজের তদারককারী',
      note: t(
        '現場{げんば}で作業員{さぎょういん}を直接{ちょくせつ}指導{しどう}・監督{かんとく}するリーダー。',
        'On-site leader who directly instructs and supervises workers.',
        'কাজের জায়গায় কর্মীদের সরাসরি পরিচালনা ও তদারকি করা দলনেতা।',
      ),
    },
    {
      ja: 'ヒヤリ・ハット',
      reading: 'ひやりはっと',
      en: 'near-miss incident',
      bn: 'হিয়ারি-হাট্টো (অল্পের জন্য রক্ষা)',
      note: t(
        '事故{じこ}にはならなかったが、ヒヤリとした危険{きけん}な出来事{できごと}。',
        'Hazardous near-miss incident that did not result in actual injury.',
        'দুর্ঘটনা না ঘটলেও চমকে ওঠার মতো ঝুঁকিপূর্ণ ঘটনা।',
      ),
    },
    {
      ja: 'ツールボックスミーティング',
      reading: 'つーるぼっくすみーてぃんぐ',
      en: 'toolbox meeting (TBM)',
      bn: 'টুলবক্স মিটিং (টিবিএম)',
      note: t(
        '作業直前{さぎょうちょくぜん}に現場{げんば}で行{おこな}う5〜10分{ぷん}の安全打{あんぜんう}ち合{あ}わせ。',
        'Short 5-10 minute safety talk held on site right before work.',
        'কাজ শুরুর ঠিক আগে কাজের জায়গায় ৫-১০ মিনিটের সংক্ষিপ্ত নিরাপত্তা বৈঠক।',
      ),
    },
    {
      ja: 'タッチ・アンド・コール',
      reading: 'たっちあんどこーる',
      en: 'touch and call',
      bn: 'টাচ অ্যান্ড কল (হাত মিলিয়ে সমস্বরে বলা)',
      note: t(
        '全員{ぜんいん}で手{て}を重{かさ}ねてスローガンを唱和{しょうわ}し、気合{きあ}いを合{あ}わせる手法{しゅほう}。',
        'Method of joining hands and chanting safety slogans to unite the team.',
        'সবাই একসাথে হাত মিলিয়ে স্লোগান মুখে বলে ঐক্যবদ্ধ হওয়ার নিয়ম।',
      ),
    },
    {
      ja: '作業停止',
      reading: 'さぎょうていし',
      en: 'stop-work authority',
      bn: 'কাজ বন্ধ করার অধিকার',
      note: t(
        '危険{きけん}を発見{はっけん}したときに作業{さぎょう}を直{ただ}ちに中断{ちゅうだん}する権利{けんり}と義務{ぎむ}。',
        'Right and safety duty to halt work immediately upon detecting danger.',
        'বিপদ দেখলে অবিলম্বে কাজ থামিয়ে দেওয়ার অধিকার ও নিরাপত্তামূলক দায়িত্ব।',
      ),
    },
  ],
}
