import type {
  Briefing,
  CheckQuestion,
  KnowledgeAnswer,
  KnowledgeSource,
  Notice,
  NoteVersion,
  Tri,
} from '@/domain/wakatta'
import { FOREMAN_NAME } from './seed'

// Canned AI output: what the Gemini pipeline returns for the filmed demo takes (plan: AI pipeline, step 4).
// shortcut: fixed answers regardless of the audio/photo; replace with POST /api/... once the backend is live.

const t = (ja: string, en: string): Tri => ({ ja, en })
const id = (prefix: string) =>
  `${prefix}-${Math.random().toString(36).slice(2, 9)}`

export function noteDraft(
  machineId: string,
  current: NoteVersion | undefined,
  versionNo: number,
  nowIso: string,
): NoteVersion {
  if (machineId === 'm-plasma')
    return {
      id: id('n'),
      machineId,
      versionNo,
      status: 'draft',
      recordedBy: FOREMAN_NAME,
      transcriptJa:
        'いいか、プラズマは段取りが命だ。保護メガネ。切ったらバリ取り。寸法はプラスマイナス1ミリ、ノギスで2か所測れ。外れてたら次を切るな、俺を呼べ。',
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
            'ノギスで寸法を2か所測ります。±1mm以内です。',
            'Measure the dimensions (sunpō) at 2 points with calipers. Within ±1 mm.',
          ),
          icon: 'measure',
        },
        {
          text: t(
            '寸法が外れたら、次を切らずに班長を呼びます。',
            'If the dimensions are out, do not cut the next piece. Call the foreman.',
          ),
          icon: 'emergency-stop',
        },
      ],
      backTranslation: {
        en: '保護メガネ着用。段取り確認。切断後バリ取り。ノギスで2点、±1mm以内。寸法外なら次を切らず班長を呼ぶ。',
      },
      questions: [
        {
          id: id('q'),
          termId: 'g-sunpo',
          prompt: t(
            '寸法が±1mmから外れていました。どうしますか？',
            'The dimensions are outside ±1 mm. What do you do?',
          ),
          options: [
            t('次を切らずに班長を呼ぶ', 'Stop and call the foreman'),
            t('そのまま次を切る', 'Cut the next piece anyway'),
            t('バリを取ればよい', 'Just remove the burr'),
          ],
          correctIndex: 0,
        },
        {
          id: id('q'),
          termId: 'g-sunpo',
          prompt: t(
            '寸法は何か所測りますか？',
            'At how many points do you measure?',
          ),
          options: [
            t('1か所', '1 point'),
            t('2か所', '2 points'),
            t('測らない', 'None'),
          ],
          correctIndex: 1,
        },
        {
          id: id('q'),
          termId: 'g-bari',
          prompt: t('「バリ」は何ですか？', 'What is "bari"?'),
          options: [
            t('材料の名前', 'The name of the material'),
            t(
              '切ったあとの、とがった部分',
              'The sharp edge left after cutting',
            ),
            t('機械のボタン', 'A button on the machine'),
          ],
          correctIndex: 1,
        },
      ],
      createdAt: nowIso,
      approvedAt: null,
    }
  // Other Machines: the current note plus one point-and-call step
  const base = current!
  return {
    ...base,
    id: id('n'),
    versionNo,
    status: 'draft',
    transcriptJa: base.transcriptJa + ' 終わったら指差し確認して報告。',
    steps: [
      ...base.steps,
      {
        text: t(
          '終わったら、指差し確認をして班長に報告します。',
          'When finished, do a point-and-call check and report to the foreman.',
        ),
        icon: 'check-list',
      },
    ],
    questions: base.questions.map((q) => ({ ...q, id: id('q') })),
    createdAt: nowIso,
    approvedAt: null,
  }
}

export function briefingDraft(nowIso: string): Briefing {
  return {
    id: id('b'),
    text: t(
      '今日15時から天井クレーンの点検です。15時から16時まで、クレーンは使えません。吊り荷の作業は午前中に終わらせてください。',
      'Overhead crane inspection from 15:00 today. The crane cannot be used 15:00–16:00. Finish all lifting work in the morning.',
    ),
    question: {
      id: id('q'),
      termId: 'g-tsuriage',
      prompt: t(
        'クレーンが使えないのは何時から何時？',
        'When can the crane not be used?',
      ),
      options: [
        t('15時〜16時', '15:00–16:00'),
        t('9時〜10時', '9:00–10:00'),
        t('一日中', 'All day'),
      ],
      correctIndex: 0,
    },
    createdAt: nowIso,
  }
}

// shortcut: placeholder rules; copy the real 佐世保市 rules for the demo district before filming (plan: Facts to verify).
export function noticeDraft(
  posterId: string,
  sourceKind: Notice['sourceKind'],
  photo: string | null,
  nowIso: string,
): Notice {
  return {
    id: id('nt'),
    posterId,
    category: 'garbage',
    sourceKind,
    sourceJa:
      '【家庭ごみの出し方】燃やせるごみ：毎週 月・木 朝8時までに、市の指定袋で。資源物（缶・びん・ペットボトル）：毎週 水 朝8時まで。粗大ごみ：事前に電話で申し込み、処理券を貼って出してください。',
    photo,
    title: t('ごみの出し方', 'How to put out garbage'),
    steps: [
      t(
        '可燃ごみ：月・木の朝8:00まで。市の指定袋を使います。',
        "Burnable: Mon/Thu before 8:00, use the city's designated bag.",
      ),
      t(
        '缶・びん・ペットボトル：水曜の朝8:00まで。',
        'Cans, bottles, PET bottles: Wednesday before 8:00.',
      ),
      t(
        '粗大ごみ（扇風機・いすなど）：先に電話で申し込み、処理券をはって出します。',
        'Oversized items (fans, chairs…): book by phone first, then attach the fee sticker.',
      ),
    ],
    events: [
      {
        id: id('ev'),
        title: t('可燃ごみの日', 'Burnable garbage day'),
        startsAt: '2026-11-02T08:00:00+09:00',
        rrule: 'FREQ=WEEKLY;BYDAY=MO,TH',
        confirmed: false,
      },
      {
        id: id('ev'),
        title: t('資源物の日', 'Recyclables day'),
        startsAt: '2026-11-04T08:00:00+09:00',
        rrule: 'FREQ=WEEKLY;BYDAY=WE',
        confirmed: false,
      },
    ],
    question: {
      id: id('q'),
      termId: 'g-kanen',
      prompt: t(
        '可燃ごみは何曜日に出しますか？',
        'Which days is burnable garbage collected?',
      ),
      options: [
        t('月・木', 'Mon & Thu'),
        t('水', 'Wed'),
        t('毎日', 'Every day'),
      ],
      correctIndex: 0,
    },
    status: 'draft',
    createdAt: nowIso,
  }
}

const FAN_WORDS = ['扇風機', '粗大', 'fan', 'chair', 'いす', 'oversized']

/** Grounded answer: only from published Notices; otherwise not covered. */
export function answerAsk(text: string, notices: Notice[]) {
  const lower = text.toLowerCase()
  const garbage = notices.find(
    (n) => n.status === 'published' && n.category === 'garbage',
  )
  if (garbage && FAN_WORDS.some((w) => lower.includes(w.toLowerCase())))
    return {
      covered: true,
      answer: t(
        '扇風機は「粗大ごみ」です。先に電話で申し込み、処理券をはって出します。（出典：「ごみの出し方」）',
        'A fan is "oversized garbage". Book by phone first, then attach the fee sticker. (Source: "How to put out garbage")',
      ),
      sourceNoticeIds: [garbage.id],
    }
  const drill = notices.find(
    (n) => n.status === 'published' && n.category === 'disaster',
  )
  if (
    drill &&
    ['訓練', 'drill', '避難', 'shelter'].some((w) =>
      lower.includes(w.toLowerCase()),
    )
  )
    return {
      covered: true,
      answer: t(
        '12月6日（日）9:00、みなと小学校の体育館に集まります。（出典：「防災訓練」）',
        'Sunday 6 Dec at 9:00, Minato Elementary School gym. (Source: "Disaster drill")',
      ),
      sourceNoticeIds: [drill.id],
    }
  return { covered: false, answer: null, sourceNoticeIds: [] }
}

/** Voice Ask: what the demo take says. */
export const ASK_VOICE_TRANSCRIPT = 'My fan is broken. How do I throw it away?'

/** Near-miss voice report: what the demo take says, translated for the Foreman. */
export const NEAR_MISS_VOICE = t(
  'プレスのペダルの近くに油がこぼれていて、すべりそうになりました。',
  'Oil was spilled near the press pedal and I almost slipped.',
)

/** Typed near-miss: keep the Worker's words and mark the Japanese as pending. */
export const typedNearMiss = (text: string): Tri => ({
  ja: `（翻訳待ち）${text}`,
  en: text,
})

/** Senpai quick replies, pre-translated; free text is shown in Japanese only. */
export const SENPAI_REPLIES: Tri[] = [
  t(
    '土曜日に一緒に申し込みましょう。処理券はコンビニで買えます。',
    "Let's book it together on Saturday. The fee sticker is sold at convenience stores.",
  ),
  t('明日、会社で説明しますね。', "I'll explain tomorrow at work."),
  t(
    '自治会の森さんに聞いてみます。',
    "I'll ask Mori-san from the neighbourhood association.",
  ),
]

// Chatbot (POST /api/knowledge/asks, XREQ-016): canned answers for the filmed questions, same shape as the backend.
const MHLW: Omit<KnowledgeSource, 'id' | 'page'> = {
  kind: 'safety',
  title:
    'For Work Safety and Health: To those working in the manufacturing industry',
  url: 'https://www.mhlw.go.jp/file/06-Seisakujouhou-11300000-Roudoukijunkyokuanzeneiseibu/0000148471.pdf',
  attribution:
    '出典：厚生労働省「未熟練労働者に対する安全衛生教育マニュアル（製造業・英語）」(https://www.mhlw.go.jp/stf/seisakunitsuite/bunya/0000118557.html) をもとに Wakatta? が抜粋・要約',
}
const ISA: Omit<KnowledgeSource, 'id' | 'page'> = {
  kind: 'life',
  title: 'Guidebook on Living and Working (7th edition)',
  url: 'https://www.moj.go.jp/isa/content/001450885.pdf',
  attribution:
    '出典：出入国在留管理庁「生活・就労ガイドブック（英語版）」(https://www.moj.go.jp/isa/support/portal/guidebook_all.html) をもとに Wakatta? が抜粋・要約',
}
/** The fixed card the backend shows for injury, fire or gas (written by people, never AI). */
export const EMERGENCY_CARD = t(
  'すぐに作業を止めて、まわりの人とフォアマンに大きな声で知らせてください。けが・火事・ガスのときは 119 に電話してください。',
  'Stop work now and shout to the people around you and your Foreman. For an injury, fire or gas, call 119.',
)
// Mirrors backend app/domain/retrieval.py is_emergency (a short list is enough for the demo).
const EMERGENCY =
  /怪我|けが|ケガ|出血|血が|火事|火災|ガス漏れ|救急|倒れ|挟まれ|\binjur|\bbleed|\bblood\b|\bfire\b|\bsmoke\b|gas leak|\btrapped\b|\bambulance\b|\b119\b|\bemergency\b/i

const q = (
  prompt: Tri,
  options: Tri[],
  correctIndex: number,
): CheckQuestion => ({
  id: 'q1',
  termId: null,
  prompt,
  options,
  correctIndex,
})

export function answerKnowledge(
  text: string,
  machineId: string | null,
  notes: NoteVersion[],
  notices: Notice[],
): Omit<KnowledgeAnswer, 'question'> {
  const lower = text.toLowerCase()
  const has = (...words: string[]) =>
    words.some((w) => lower.includes(w.toLowerCase()))
  const out = (
    answer: Tri,
    sources: KnowledgeSource[],
    check: CheckQuestion | null,
  ) => ({ covered: true, emergency: false, answer, sources, check })

  if (EMERGENCY.test(text))
    return {
      covered: true,
      emergency: true,
      answer: EMERGENCY_CARD,
      sources: [],
      check: null,
    }
  if (has('clean', '掃除', 'stop the machine', '止め'))
    return out(
      t(
        '機械を掃除するときは、必ず先に機械を完全に止めてください。動いている機械に手を入れると、巻き込まれて大けがをします。',
        'Always stop the machine completely before you clean it. If you put your hand into a moving machine, it can be caught and badly injured.',
      ),
      [{ ...MHLW, id: 'mhlw-safety:p15:1', page: 15 }],
      q(
        t(
          '機械を掃除する前に、何をしますか？',
          'What do you do before cleaning the machine?',
        ),
        [
          t('機械を完全に止める', 'Stop the machine completely'),
          t('ゆっくり動かしておく', 'Keep it running slowly'),
          t('手袋だけつける', 'Only put on gloves'),
        ],
        0,
      ),
    )
  const note = notes
    .filter((n) => n.machineId === machineId && n.status === 'approved')
    .sort((a, b) => b.versionNo - a.versionNo)[0]
  if (note && has('wear', 'safe', 'start', '着', '安全', '始め'))
    return out(
      {
        ja: note.steps.map((s) => s.text.ja).join(' '),
        en: note.steps.map((s) => s.text.en).join(' '),
      },
      [
        {
          id: `note:${note.id}`,
          kind: 'note',
          title: `Work instruction v${note.versionNo}`,
          page: null,
          url: null,
          attribution: null,
        },
      ],
      note.questions[0] ?? null,
    )
  if (has('garbage', 'trash', 'ごみ', 'ゴミ'))
    return out(
      t(
        'ごみは種類ごとに分けて、決められた日の朝に、決められた場所に出します。分け方と曜日は市町村によって違うので、市役所の案内を確認してください。',
        'Sort garbage by type and put it out on the set day, in the morning, at the set place. The rules and days differ by city, so check your city office’s guide.',
      ),
      [{ ...ISA, id: 'isa-guide:p142:1', page: 142 }],
      q(
        t('ごみはいつ出しますか？', 'When do you put out garbage?'),
        [
          t('決められた日の朝', 'On the set day, in the morning'),
          t('いつでもよい', 'Any time'),
          t('夜中', 'In the middle of the night'),
        ],
        0,
      ),
    )
  if (has('earthquake', '地震'))
    return out(
      t(
        '揺れている間は、机の下などで頭を守ります。揺れが止まったら火を消し、ドアを開けて出口を確保し、避難場所へ向かいます。',
        'While it shakes, protect your head, for example under a desk. When the shaking stops, turn off any fire, open the door to keep an exit, and go to the evacuation site.',
      ),
      [
        { ...ISA, id: 'isa-guide:p128:1', page: 128 },
        { ...ISA, id: 'isa-guide:p129:2', page: 129 },
      ],
      q(
        t(
          '揺れている間、まず何をしますか？',
          'What do you do first while it shakes?',
        ),
        [
          t('頭を守る', 'Protect your head'),
          t('外へ走る', 'Run outside'),
          t('エレベーターに乗る', 'Take the elevator'),
        ],
        0,
      ),
    )
  const drill = notices.find(
    (n) => n.status === 'published' && n.category === 'disaster',
  )
  if (drill && has('drill', '訓練', 'evacuat', '避難'))
    return out(
      t(
        '12月6日（日）9:00、みなと小学校の体育館に集まります。',
        'Sunday 6 Dec at 9:00, at the Minato Elementary School gym.',
      ),
      [
        {
          id: `notice:${drill.id}`,
          kind: 'notice',
          title: drill.title.en || drill.title.ja,
          page: null,
          url: null,
          attribution: null,
        },
      ],
      q(
        t('防災訓練はどこですか？', 'Where is the disaster drill?'),
        [
          t('みなと小学校の体育館', 'Minato Elementary School gym'),
          t('駅の前', 'In front of the station'),
          t('会社の駐車場', 'The company car park'),
        ],
        0,
      ),
    )
  return {
    covered: false,
    emergency: false,
    answer: null,
    sources: [],
    check: null,
  }
}
