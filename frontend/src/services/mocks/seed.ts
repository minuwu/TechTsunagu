import type {
  Ask,
  Briefing,
  CheckQuestion,
  Confusion,
  GlossaryTerm,
  Machine,
  Meetup,
  NearMissReport,
  Notice,
  NoteVersion,
  Poster,
  Receipt,
  SenpaiPair,
  ShiftAssignment,
  SignOff,
  Tri,
  Worker,
  CheckRecord,
} from '@/domain/wakatta'
import { tokyoDate } from '@/domain/wakatta'

// Fictional shop and neighbourhood (plan: "Seed story"). Names are invented.

export interface Db {
  workers: Worker[]
  machines: Machine[]
  glossary: GlossaryTerm[]
  notes: NoteVersion[]
  checks: CheckRecord[]
  confusions: Confusion[]
  signOffs: SignOff[]
  assignments: ShiftAssignment[]
  briefings: Briefing[]
  receipts: Receipt[]
  nearMisses: NearMissReport[]
  posters: Poster[]
  notices: Notice[]
  pairs: SenpaiPair[]
  asks: Ask[]
  meetups: Meetup[]
}

export const SHOP_NAME: Tri = {
  ja: '佐世保鋼材加工（架空）',
  en: 'Sasebo Steel Processing (fictional)',
}
export const FOREMAN_NAME = '山口 隆'

const t = (ja: string, en: string): Tri => ({ ja, en })

const q = (
  id: string,
  termId: string | null,
  prompt: Tri,
  options: Tri[],
  correctIndex: number,
): CheckQuestion => ({ id, termId, prompt, options, correctIndex })

const glossary: GlossaryTerm[] = [
  {
    id: 'g-sunpo',
    ja: '寸法',
    reading: 'すんぽう',
    en: 'dimensions (sunpō)',
  },
  {
    id: 'g-dandori',
    ja: '段取り',
    reading: 'だんどり',
    en: 'set-up (dandori)',
  },
  {
    id: 'g-tamakake',
    ja: '玉掛け',
    reading: 'たまかけ',
    en: 'slinging (tamakake)',
  },
  {
    id: 'g-bari',
    ja: 'バリ',
    reading: 'ばり',
    en: 'burr (bari)',
  },
  {
    id: 'g-goggles',
    ja: '保護メガネ',
    reading: 'ほごめがね',
    en: 'safety glasses',
  },
  {
    id: 'g-estop',
    ja: '非常停止',
    reading: 'ひじょうていし',
    en: 'emergency stop',
  },
  {
    id: 'g-kanetsu',
    ja: '高温注意',
    reading: 'こうおんちゅうい',
    en: 'hot — caution',
  },
  {
    id: 'g-kanki',
    ja: '換気',
    reading: 'かんき',
    en: 'ventilation',
  },
  {
    id: 'g-kakunin',
    ja: '指差し確認',
    reading: 'ゆびさしかくにん',
    en: 'point-and-call check',
  },
  {
    id: 'g-tsuriage',
    ja: '吊り荷',
    reading: 'つりに',
    en: 'suspended load',
  },
  {
    id: 'g-kanen',
    ja: '可燃ごみ',
    reading: 'かねんごみ',
    en: 'burnable garbage',
  },
  {
    id: 'g-shiteibukuro',
    ja: '指定袋',
    reading: 'していぶくろ',
    en: "city's designated bag",
  },
  {
    id: 'g-sodai',
    ja: '粗大ごみ',
    reading: 'そだいごみ',
    en: 'oversized garbage',
  },
  {
    id: 'g-hinanjo',
    ja: '避難所',
    reading: 'ひなんじょ',
    en: 'evacuation shelter',
  },
  {
    id: 'g-jichikai',
    ja: '自治会',
    reading: 'じちかい',
    en: 'neighbourhood association',
  },
]

const DAY = 86_400_000
const ago = (now: Date, days: number) =>
  new Date(now.getTime() - days * DAY).toISOString()

function notes(now: Date): NoteVersion[] {
  const base = {
    status: 'approved' as const,
    recordedBy: FOREMAN_NAME,
    backTranslation: { en: '' },
  }
  return [
    {
      ...base,
      id: 'n-plasma-1',
      machineId: 'm-plasma',
      versionNo: 1,
      transcriptJa:
        'プラズマは段取りしてから。保護メガネ忘れるなよ。切ったらバリ取って、寸法は必ず測ってから次へ。',
      steps: [
        {
          text: t('保護メガネをかけます。', 'Put on safety glasses.'),
          icon: 'ppe-goggles',
        },
        {
          text: t(
            '段取り（材料の位置と設定）を確認します。',
            'Check the set-up (dandori): material position and settings.',
          ),
          icon: 'check-list',
        },
        {
          text: t(
            '切ったあと、バリを取ります。',
            'After cutting, remove the burr (bari).',
          ),
          icon: 'ppe-gloves',
        },
        {
          text: t(
            '次の作業の前に、寸法を必ず測ります。',
            'Always measure the dimensions (sunpō) before the next job.',
          ),
          icon: 'measure',
        },
      ],
      backTranslation: {
        en: '保護メガネをかける。段取り（材料の位置と設定）を確認。切断後にバリを取る。次の作業の前に必ず寸法を測る。',
      },
      questions: [
        q(
          'q-plasma-1',
          'g-sunpo',
          t(
            '次の作業の前に、何をしますか？',
            'What do you do before the next job?',
          ),
          [
            t('寸法を測る', 'Measure the dimensions'),
            t('すぐ次を切る', 'Cut the next piece right away'),
            t('機械を止めて帰る', 'Stop the machine and leave'),
          ],
          0,
        ),
        q(
          'q-plasma-2',
          'g-bari',
          t('「バリ」は何ですか？', 'What is "bari"?'),
          [
            t('材料の名前', 'The name of the material'),
            t(
              '切ったあとの、とがった部分',
              'The sharp edge left after cutting',
            ),
            t('機械のボタン', 'A button on the machine'),
          ],
          1,
        ),
      ],
      createdAt: ago(now, 120),
      approvedAt: ago(now, 120),
    },
    {
      ...base,
      id: 'n-press-1',
      machineId: 'm-press',
      versionNo: 1,
      transcriptJa:
        'プレスは両手をしっかり離してから踏む。おかしいと思ったらすぐ非常停止。',
      steps: [
        {
          text: t(
            '型の中に手を入れません。',
            'Never put your hands inside the die.',
          ),
          icon: 'hands-clear',
        },
        {
          text: t(
            '両手をはなしてから、ペダルを踏みます。',
            'Take both hands away, then press the pedal.',
          ),
          icon: 'hands-clear',
        },
        {
          text: t(
            'おかしいと思ったら、すぐ非常停止を押します。',
            'If anything seems wrong, press the emergency stop at once.',
          ),
          icon: 'emergency-stop',
        },
      ],
      backTranslation: {
        en: '型の中に手を入れない。両手を離してからペダルを踏む。何かおかしければすぐ非常停止を押す。',
      },
      questions: [
        q(
          'q-press-1',
          'g-estop',
          t(
            '音がおかしいとき、何をしますか？',
            'What do you do when the sound is strange?',
          ),
          [
            t('そのまま続ける', 'Keep going'),
            t('非常停止を押す', 'Press the emergency stop'),
            t('音楽を止める', 'Turn off the music'),
          ],
          1,
        ),
        q(
          'q-press-2',
          null,
          t('ペダルを踏むのはいつですか？', 'When do you press the pedal?'),
          [
            t('両手をはなしたあと', 'After both hands are away'),
            t(
              '片手で材料をおさえながら',
              'While holding the material with one hand',
            ),
            t('いつでもよい', 'Any time'),
          ],
          0,
        ),
      ],
      createdAt: ago(now, 150),
      approvedAt: ago(now, 150),
    },
    {
      ...base,
      id: 'n-crane-1',
      machineId: 'm-crane',
      versionNo: 1,
      transcriptJa:
        '玉掛けは資格者だけ。吊り荷の下には絶対入るな。合図は指差し確認で。',
      steps: [
        {
          text: t(
            '玉掛けは、資格のある人だけがします。',
            'Only licensed people do the slinging (tamakake).',
          ),
          icon: 'crane-sling',
        },
        {
          text: t(
            '吊り荷の下に、ぜったいに入りません。',
            'Never stand under a suspended load.',
          ),
          icon: 'crane-sling',
        },
        {
          text: t(
            '合図の前に、指差し確認をします。',
            'Do a point-and-call check before giving the signal.',
          ),
          icon: 'check-list',
        },
      ],
      backTranslation: {
        en: '玉掛けは資格者のみ。吊り荷の下に立たない。合図の前に指差し確認。',
      },
      questions: [
        q(
          'q-crane-1',
          'g-tsuriage',
          t(
            '吊り荷が動いているとき、どこに立ちますか？',
            'Where do you stand while a load is moving?',
          ),
          [
            t('吊り荷の真下', 'Right under the load'),
            t('吊り荷から離れた場所', 'Away from the load'),
            t('クレーンの上', 'On top of the crane'),
          ],
          1,
        ),
        q(
          'q-crane-2',
          'g-tamakake',
          t('玉掛けをしてよいのはだれですか？', 'Who may do the slinging?'),
          [
            t('だれでも', 'Anyone'),
            t('資格のある人', 'A licensed person'),
            t('新人', 'New staff'),
          ],
          1,
        ),
      ],
      createdAt: ago(now, 60),
      approvedAt: ago(now, 60),
    },
    {
      ...base,
      id: 'n-weld-1',
      machineId: 'm-weld',
      versionNo: 1,
      transcriptJa:
        '溶接の前に換気扇つけろ。終わってもしばらく材料は熱いから素手で触るな。',
      steps: [
        {
          text: t(
            '溶接の前に、換気をつけます。',
            'Turn on the ventilation before welding.',
          ),
          icon: 'ventilation',
        },
        {
          text: t(
            '終わったあとも材料は熱いです。素手でさわりません。',
            'The material stays hot after welding. Do not touch it bare-handed.',
          ),
          icon: 'hot-surface',
        },
      ],
      backTranslation: {
        en: '溶接前に換気をつける。溶接後も材料は熱いので素手で触らない。',
      },
      questions: [
        q(
          'q-weld-1',
          'g-kanetsu',
          t(
            '溶接が終わった材料は？',
            'What about material just after welding?',
          ),
          [
            t('すぐ素手で運ぶ', 'Carry it bare-handed right away'),
            t('まだ熱いので手袋を使う', 'Still hot, so use gloves'),
            t('水をかけて食べる', 'Pour water on it and eat it'),
          ],
          1,
        ),
      ],
      createdAt: ago(now, 30),
      approvedAt: ago(now, 30),
    },
  ]
}

export function seed(now: Date): Db {
  const today = tokyoDate(now)
  const workers: Worker[] = [
    { id: 'w-rahim', name: 'Rahim Uddin', language: 'en' },
    { id: 'w-sadia', name: 'Sadia Akter', language: 'en' },
    { id: 'w-tanvir', name: 'Tanvir Hasan', language: 'en' },
    { id: 'w-tanaka', name: '田中 健', language: 'ja' },
  ]
  const machines: Machine[] = [
    {
      id: 'm-plasma',
      name: t('プラズマ切断機 #1', 'Plasma cutter #1'),
      currentVersionId: 'n-plasma-1',
      activeWorkerId: null,
    },
    {
      id: 'm-press',
      name: t('プレスブレーキ #2', 'Press brake #2'),
      currentVersionId: 'n-press-1',
      activeWorkerId: null,
    },
    {
      id: 'm-crane',
      name: t('天井クレーン（玉掛け）#3', 'Overhead crane (slinging) #3'),
      currentVersionId: 'n-crane-1',
      activeWorkerId: null,
    },
    {
      id: 'm-weld',
      name: t('溶接ブース #4', 'Welding booth #4'),
      currentVersionId: 'n-weld-1',
      activeWorkerId: null,
    },
  ]
  const so = (
    id: string,
    workerId: string,
    machineId: string,
    days: number,
  ): SignOff => ({
    id,
    workerId,
    machineId,
    noteVersionId: machines.find((m) => m.id === machineId)!.currentVersionId!,
    passedAt: ago(now, days),
  })
  const conf = (
    id: string,
    termId: string,
    machineId: string,
    workerId: string,
    days: number,
  ): Confusion => ({
    id,
    checkId: 'c-seed',
    questionId: 'q-seed',
    termId,
    machineId,
    workerId,
    createdAt: ago(now, days),
    acknowledgedAt: ago(now, days),
  })
  const drillQuestion = q(
    'q-notice-drill',
    'g-hinanjo',
    t('訓練の日、どこに集まりますか？', 'Where do you gather on drill day?'),
    [
      t('みなと小学校の体育館', 'Minato Elementary School gym'),
      t('会社', 'The company'),
      t('駅', 'The station'),
    ],
    0,
  )

  const notices: Notice[] = [
    {
      id: 'nt-drill',
      posterId: 'p-jichikai',
      category: 'disaster',
      sourceKind: 'text',
      sourceJa:
        '12月6日（日）9時から、みなと町の防災訓練を行います。みなと小学校の体育館に集合してください。',
      photo: null,
      title: t('防災訓練（12月6日）', 'Disaster drill (6 Dec)'),
      steps: [
        t('12月6日（日）9:00に集まります。', 'Gather on Sunday 6 Dec at 9:00.'),
        t(
          '場所：みなと小学校の体育館（避難所）。',
          'Place: Minato Elementary School gym (evacuation shelter).',
        ),
        t('動きやすい服で来てください。', 'Wear clothes you can move in.'),
      ],
      events: [
        {
          id: 'ev-drill',
          title: t('防災訓練', 'Disaster drill'),
          startsAt: '2026-12-06T09:00:00+09:00',
          rrule: null,
          confirmed: true,
        },
      ],
      question: drillQuestion,
      status: 'published',
      createdAt: ago(now, 3),
    },
    {
      id: 'nt-festival',
      posterId: 'p-city',
      category: 'event',
      sourceKind: 'text',
      sourceJa:
        '12月13日（日）11時から、みなと公園で冬まつりがあります。外国人の方もぜひ参加してください。',
      photo: null,
      title: t('みなと公園 冬まつり', 'Minato Park winter festival'),
      steps: [
        t(
          '12月13日（日）11:00から、みなと公園で。',
          'Sunday 13 Dec from 11:00, Minato Park.',
        ),
        t('だれでも参加できます。無料です。', 'Anyone can join. Free.'),
      ],
      events: [
        {
          id: 'ev-festival',
          title: t('冬まつり', 'Winter festival'),
          startsAt: '2026-12-13T11:00:00+09:00',
          rrule: null,
          confirmed: true,
        },
      ],
      question: q(
        'q-notice-fest',
        null,
        t(
          'まつりに参加するのに、お金はいりますか？',
          'Does the festival cost money?',
        ),
        [
          t('いらない（無料）', 'No (free)'),
          t('1万円', '¥10,000'),
          t('会社が払う', 'The company pays'),
        ],
        0,
      ),
      status: 'published',
      createdAt: ago(now, 5),
    },
    {
      id: 'nt-cleanup',
      posterId: 'p-jichikai',
      category: 'duty',
      sourceKind: 'text',
      sourceJa:
        '毎月第1日曜は自治会の清掃当番です。8時に公民館前に集合。軍手を持ってきてください。',
      photo: null,
      title: t('自治会の清掃当番', 'Neighbourhood clean-up duty'),
      steps: [
        t(
          '毎月の第1日曜、8:00に公民館の前に集まります。',
          'First Sunday of each month, 8:00, in front of the community hall.',
        ),
        t('軍手を持ってきます。', 'Bring work gloves.'),
      ],
      events: [
        {
          id: 'ev-cleanup',
          title: t('清掃当番', 'Clean-up duty'),
          startsAt: '2026-12-06T08:00:00+09:00',
          rrule: null,
          confirmed: true,
        },
      ],
      question: q(
        'q-notice-clean',
        null,
        t(
          '清掃当番に何を持っていきますか？',
          'What do you bring to clean-up duty?',
        ),
        [
          t('軍手', 'Work gloves'),
          t('お弁当', 'A lunch box'),
          t('パスポート', 'Passport'),
        ],
        0,
      ),
      status: 'published',
      createdAt: ago(now, 10),
    },
  ]

  const briefing: Briefing = {
    id: 'b-1',
    text: t(
      '今日は午後から雨です。外の材料置き場は、すべりやすいです。台車はゆっくり押してください。',
      'Rain from this afternoon. The outdoor material yard will be slippery. Push trolleys slowly.',
    ),
    question: q(
      'q-brief-1',
      null,
      t(
        '午後、外の材料置き場はどうなりますか？',
        'What happens to the outdoor yard this afternoon?',
      ),
      [t('すべりやすい', 'Slippery'), t('閉まる', 'Closed'), t('暑い', 'Hot')],
      0,
    ),
    createdAt: ago(now, 0.1),
  }

  return {
    workers,
    machines,
    glossary,
    notes: notes(now),
    checks: [],
    confusions: [
      conf('cf-1', 'g-sunpo', 'm-plasma', 'w-tanvir', 20),
      conf('cf-2', 'g-sunpo', 'm-plasma', 'w-sadia', 15),
      conf('cf-3', 'g-sunpo', 'm-press', 'w-tanvir', 12),
      conf('cf-4', 'g-tamakake', 'm-crane', 'w-sadia', 9),
      conf('cf-5', 'g-tsuriage', 'm-crane', 'w-tanvir', 8),
      conf('cf-6', 'g-bari', 'm-plasma', 'w-rahim', 6),
      conf('cf-7', 'g-estop', 'm-press', 'w-sadia', 4),
      conf('cf-8', 'g-kanetsu', 'm-weld', 'w-tanvir', 2),
    ],
    signOffs: [
      so('s-1', 'w-rahim', 'm-plasma', 30),
      so('s-2', 'w-rahim', 'm-crane', 20),
      so('s-3', 'w-sadia', 'm-plasma', 80), // expiring
      so('s-4', 'w-sadia', 'm-weld', 10),
      so('s-5', 'w-tanvir', 'm-weld', 25),
      {
        id: 's-6',
        workerId: 'w-tanvir',
        machineId: 'm-press',
        noteVersionId: 'n-press-1',
        passedAt: ago(now, 100),
      }, // expired
      so('s-7', 'w-tanaka', 'm-plasma', 40),
      so('s-8', 'w-tanaka', 'm-press', 40),
      so('s-9', 'w-tanaka', 'm-crane', 40),
      so('s-10', 'w-tanaka', 'm-weld', 25),
    ],
    assignments: [
      {
        id: 'a-1',
        workerId: 'w-rahim',
        machineId: 'm-plasma',
        date: today,
        shift: 'day',
      },
      {
        id: 'a-2',
        workerId: 'w-tanvir',
        machineId: 'm-press',
        date: today,
        shift: 'day',
      }, // flagged: expired
      {
        id: 'a-3',
        workerId: 'w-sadia',
        machineId: 'm-crane',
        date: today,
        shift: 'day',
      }, // flagged: none
      {
        id: 'a-4',
        workerId: 'w-tanaka',
        machineId: 'm-weld',
        date: today,
        shift: 'day',
      },
    ],
    briefings: [briefing],
    receipts: [
      {
        targetId: 'b-1',
        workerId: 'w-tanaka',
        openedAt: ago(now, 0.05),
        passed: true,
      },
      {
        targetId: 'b-1',
        workerId: 'w-sadia',
        openedAt: ago(now, 0.04),
        passed: null,
      },
      {
        targetId: 'nt-drill',
        workerId: 'w-sadia',
        openedAt: ago(now, 2),
        passed: true,
      },
      {
        targetId: 'nt-drill',
        workerId: 'w-tanvir',
        openedAt: ago(now, 2),
        passed: null,
      },
      {
        targetId: 'nt-festival',
        workerId: 'w-sadia',
        openedAt: ago(now, 4),
        passed: true,
      },
    ],
    nearMisses: [
      {
        id: 'nm-1',
        workerId: 'w-sadia',
        machineId: 'm-crane',
        text: t(
          'クレーンのフックの安全レバーが戻りにくいです。荷がはずれそうになりました。',
          'The safety latch on the crane hook does not spring back well. A load nearly slipped off.',
        ),
        photo: null,
        createdAt: ago(now, 1),
        acknowledgedAt: null,
      },
    ],
    posters: [
      { id: 'p-jichikai', name: 'みなと町自治会（架空）', kind: 'jichikai' },
      { id: 'p-city', name: '佐世保市 国際交流（架空）', kind: 'city' },
    ],
    notices,
    pairs: [
      {
        id: 'sp-rahim',
        workerId: 'w-rahim',
        senpaiName: '田中 健',
        kind: 'coworker',
        token: 'demo-tanaka-7f3a9c',
      },
      {
        id: 'sp-sadia',
        workerId: 'w-sadia',
        senpaiName: '森 早苗',
        kind: 'jichikai',
        token: 'demo-mori-2b8e41',
      },
    ],
    asks: [],
    meetups: [
      {
        id: 'mu-1',
        pairId: 'sp-rahim',
        kind: 'coffee',
        date: tokyoDate(new Date(now.getTime() - 6 * DAY)),
        note: '駅前のカフェで。ごみの出し方を説明。',
        loggedBy: 'senpai',
      },
      {
        id: 'mu-2',
        pairId: 'sp-sadia',
        kind: 'event',
        date: tokyoDate(new Date(now.getTime() - 12 * DAY)),
        note: '自治会の秋祭りの準備を一緒に。',
        loggedBy: 'worker',
      },
    ],
  }
}
