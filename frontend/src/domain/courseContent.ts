import type { Drill, InspectItem, Panel } from './course'
import type { Tri } from './wakatta'

const t = (ja: string, en: string, bn: string): Tri => ({ ja, en, bn })

export const COURSE = {
  title: t('玉掛け', 'Crane slinging', 'ক্রেনে স্লিং বাঁধা'),
  sub: t(
    '吊り荷を安全に掛けて、合図して、吊る。',
    'Rig a load, signal, and lift it safely.',
    'মাল নিরাপদে বাঁধা, ইশারা দেওয়া ও তোলা।',
  ),
}

/** Door 1, Comic: `漢字{よみ}` carries the furigana. */
export const PANELS: Panel[] = [
  {
    who: 'foreman',
    text: t(
      '吊{つ}り荷{に}の下{した}に入{はい}るな！',
      'Never stand under a hanging load!',
      'ঝুলন্ত মালের নিচে কখনো দাঁড়াবে না!',
    ),
  },
  {
    who: 'worker',
    text: t(
      '…わかりました。でも、ワイヤーの角度{かくど}は？',
      '…Understood. But what about the wire angle?',
      '…বুঝেছি। কিন্তু তারের কোণ কত হবে?',
    ),
  },
  {
    who: 'foreman',
    text: t(
      '60度以内{どいない}。広{ひろ}げるほど、ワイヤーに力{ちから}がかかる。',
      'Within 60°. The wider it opens, the more force on the wire.',
      '৬০° এর মধ্যে। যত বেশি ফাঁক, তারে তত বেশি টান।',
    ),
  },
]

// Plain-language summary, not statute text (spec §3.1); check the wording before it goes in front of a regulator
export const HANDBOOK: Tri[] = [
  t(
    '玉掛{たまが}けは、決{き}められた資格{しかく}のある人{ひと}だけが行{おこな}う。',
    'Only a qualified person may sling loads.',
    'শুধু যোগ্যতাপ্রাপ্ত ব্যক্তি স্লিং বাঁধতে পারবেন।',
  ),
  t(
    '吊{つ}り荷{に}の下{した}への立入{たちいり}は禁止{きんし}。',
    'Entering the area under a suspended load is prohibited.',
    'ঝুলন্ত মালের নিচে ঢোকা নিষেধ।',
  ),
  t(
    '使用前{しようまえ}にワイヤーロープを点検{てんけん}する。',
    'Inspect the wire rope before every use.',
    'প্রতিবার ব্যবহারের আগে ওয়্যার রোপ পরীক্ষা করবে।',
  ),
]

/** Door 2: Shisa Kanko (pointing and calling) lines, one per beat. */
export const REEL: Tri[] = [
  t(
    '主電源{しゅでんげん}、OFF、ヨシ！',
    'Main switch OFF, yoshi!',
    'মেইন সুইচ বন্ধ, ঠিক আছে!',
  ),
  t(
    'ワイヤー、傷{きず}なし、ヨシ！',
    'Wire, no damage, yoshi!',
    'তার অক্ষত, ঠিক আছে!',
  ),
  t(
    '吊{つ}り角度{かくど}、60度以内{どいない}、ヨシ！',
    'Sling angle within 60°, yoshi!',
    'কোণ ৬০° এর মধ্যে, ঠিক আছে!',
  ),
  t(
    '吊{つ}り荷{に}の下{した}、人{ひと}なし、ヨシ！',
    'Nobody under the load, yoshi!',
    'মালের নিচে কেউ নেই, ঠিক আছে!',
  ),
  t(
    '合図{あいず}、巻上{まきあ}げ、ヨシ！',
    'Signal, hoist, yoshi!',
    'ইশারা, তোলো, ঠিক আছে!',
  ),
]

const seq = (id: string, name: Tri, goal: Tri, steps: Tri[]): Drill => ({
  id,
  name,
  goal,
  kind: 'seq',
  steps,
})

/** Door 3: the nine drills. Five engines, the rest is data. */
export const DRILLS: Drill[] = [
  {
    id: 'kyt',
    kind: 'spot',
    secs: 30,
    need: 4,
    name: t('危険予知（KYT）', 'Hazard spotting (KYT)', 'ঝুঁকি চেনা (KYT)'),
    goal: t(
      '30秒で危険を4つ見つける。',
      'Find 4 hazards in 30 seconds.',
      '৩০ সেকেন্ডে ৪টি বিপদ খুঁজে বের করো।',
    ),
    items: [
      { hazard: true, label: t('床の油', 'Oil on the floor', 'মেঝেতে তেল') },
      { hazard: false, label: t('消火器', 'Fire extinguisher', 'অগ্নিনির্বাপক') },
      {
        hazard: true,
        label: t('通路のケーブル', 'Cable across the walkway', 'পথে ছড়ানো তার'),
      },
      { hazard: false, label: t('工具棚', 'Tool rack', 'টুল র‍্যাক') },
      {
        hazard: true,
        label: t(
          'カバーのない回転部',
          'Moving part with no guard',
          'গার্ডহীন ঘূর্ণায়মান অংশ',
        ),
      },
      {
        hazard: false,
        label: t('きれいな通路', 'Clear walkway', 'পরিষ্কার পথ'),
      },
      {
        hazard: true,
        label: t(
          '吊り荷の下の人',
          'Person under a hanging load',
          'ঝুলন্ত মালের নিচে মানুষ',
        ),
      },
      { hazard: true, label: t('ヘルメットなし', 'No helmet', 'হেলমেট নেই') },
    ],
  },
  {
    id: 'sling',
    kind: 'sling',
    loadKg: 1000,
    wllKg: 600,
    name: t('吊り角度と張力', 'Sling angle and tension', 'স্লিং কোণ ও টান'),
    goal: t(
      'ワイヤー1本の限度（600kgf）を超えない角度にする。',
      'Keep each wire under its 600 kgf limit.',
      'প্রতিটি তারের সীমা (৬০০ কেজি-বল) পার না করে কোণ ঠিক করো।',
    ),
  },
  {
    id: 'estop',
    kind: 'reflex',
    name: t('非常停止の反射', 'E-stop reflex', 'ইমার্জেন্সি স্টপ রিফ্লেক্স'),
    goal: t(
      '巻き込まれ！0.4秒以内に非常停止を押す。',
      'Entanglement! Hit E-stop within 0.4 s.',
      'জড়িয়ে যাচ্ছে! ০.৪ সেকেন্ডের মধ্যে ই-স্টপ চাপো।',
    ),
  },
  {
    id: 'ring',
    kind: 'ring',
    wheels: [true, false, true],
    name: t(
      '砥石の打音点検',
      'Grinder wheel ring test',
      'গ্রাইন্ডার হুইলের শব্দ পরীক্ষা',
    ),
    goal: t(
      '叩いて音を聞く。澄んだ音は良品、鈍い音はひび割れ。',
      'Tap and listen. A clear ring is sound; a dull thud is cracked.',
      'টোকা দিয়ে শোনো। পরিষ্কার শব্দ ভালো, ভোঁতা শব্দ মানে ফাটা।',
    ),
  },
  seq(
    'ppe',
    t('保護具の装着', 'PPE checklist', 'পিপিই চেকলিস্ট'),
    t('正しい順番で装着する。', 'Put on in the right order.', 'সঠিক ক্রমে পরো।'),
    [
      t('ヘルメット、あごひも', 'Helmet, chin strap', 'হেলমেট, চিনস্ট্র্যাপ'),
      t(
        '安全帯のバックル確認',
        'Harness, check buckles',
        'হারনেসের বাকল পরীক্ষা',
      ),
      t('安全靴', 'Safety shoes', 'সেফটি জুতা'),
      t('手袋', 'Gloves', 'গ্লাভস'),
    ],
  ),
  seq(
    'confined',
    t(
      '酸素濃度とロックアウト',
      'Confined space: O2 and LOTO',
      'বদ্ধ স্থান: অক্সিজেন ও লক-আউট',
    ),
    t(
      '入る前の手順を正しい順番で。',
      'Do the pre-entry steps in order.',
      'ঢোকার আগের ধাপগুলো ক্রমে করো।',
    ),
    [
      t(
        'バルブをロックアウト・タグアウト',
        'Lock out and tag out the valve',
        'ভাল্ব লক-আউট ও ট্যাগ-আউট',
      ),
      t(
        'ガス検知器を校正',
        'Calibrate the gas detector',
        'গ্যাস ডিটেক্টর ক্যালিব্রেট',
      ),
      t('換気する', 'Ventilate', 'বাতাস চলাচল করাও'),
      t(
        '酸素18%以上を確認',
        'Confirm oxygen is 18% or more',
        'অক্সিজেন ১৮% বা বেশি নিশ্চিত করো',
      ),
    ],
  ),
  seq(
    'scaffold',
    t('移動式足場の設置', 'Rolling scaffold setup', 'চলমান স্ক্যাফোল্ড স্থাপন'),
    t(
      '登る前の手順を正しい順番で。',
      'Do the steps before climbing, in order.',
      'ওঠার আগের ধাপগুলো ক্রমে করো।',
    ),
    [
      t('地面が平らか確認', 'Check the ground is level', 'মাটি সমতল কিনা দেখো'),
      t(
        'キャスターのブレーキをかける',
        'Lock the caster brakes',
        'চাকার ব্রেক লাগাও',
      ),
      t('アウトリガーを張り出す', 'Extend the outriggers', 'আউটট্রিগার বের করো'),
      t(
        '手すりを確認して登る',
        'Check handrails, then climb',
        'হ্যান্ডরেল দেখে তারপর ওঠো',
      ),
    ],
  ),
  seq(
    'welder',
    t('アーク溶接のアース', 'Arc welder grounding', 'আর্ক ওয়েল্ডার আর্থিং'),
    t(
      '溶接前の手順を正しい順番で。',
      'Do the steps before welding, in order.',
      'ওয়েল্ডিংয়ের আগের ধাপগুলো ক্রমে করো।',
    ),
    [
      t('電源OFFを確認', 'Confirm power is OFF', 'পাওয়ার বন্ধ নিশ্চিত করো'),
      t(
        'アースを母材にしっかり接続',
        'Clamp the earth firmly to the workpiece',
        'আর্থ ক্ল্যাম্প ওয়ার্কপিসে শক্ত করে লাগাও',
      ),
      t(
        'ケーブルの傷を点検',
        'Inspect the cable for damage',
        'ক্যাবলে ক্ষত আছে কিনা দেখো',
      ),
      t('電源ON', 'Switch power ON', 'পাওয়ার চালু করো'),
    ],
  ),
  seq(
    'evac',
    t(
      '津波・火災の避難',
      'Tsunami / fire evacuation',
      'সুনামি / আগুনে সরে যাওয়া',
    ),
    t(
      '警報から集合までを正しい順番で。',
      'From alarm to rally point, in order.',
      'অ্যালার্ম থেকে জমায়েত পর্যন্ত ক্রমে।',
    ),
    [
      t('機械を止める', 'Stop the machine', 'মেশিন বন্ধ করো'),
      t(
        '荷物を持たず出口へ',
        'Leave belongings, go to the exit',
        'জিনিস ফেলে বের হও',
      ),
      t('高台へ向かう', 'Move to high ground', 'উঁচু জায়গায় যাও'),
      t(
        '集合場所で点呼',
        'Roll call at the rally point',
        'জমায়েত স্থানে হাজিরা',
      ),
    ],
  ),
]

/** Door 4: tap the check points, then Pass (良品) or Reject (不良品). */
export const INSPECT: InspectItem[] = [
  {
    id: 'harness',
    defective: false,
    name: t('安全帯', 'Full-body harness', 'ফুল-বডি হারনেস'),
    spots: [
      {
        x: 22,
        y: 30,
        check: t(
          'ベルトのほつれ・切れ',
          'Frayed or cut webbing',
          'ফিতা ছেঁড়া বা কাটা',
        ),
      },
      {
        x: 74,
        y: 30,
        check: t(
          'バックルの動き・変形',
          'Buckle moves freely, not bent',
          'বাকল স্বাভাবিক, বাঁকা নয়',
        ),
      },
    ],
  },
  {
    id: 'wheel',
    defective: true,
    name: t('研削砥石', 'Grinder wheel', 'গ্রাইন্ডার হুইল'),
    spots: [
      {
        x: 38,
        y: 24,
        check: t('ひび・欠け', 'Cracks or chips', 'ফাটল বা ভাঙা অংশ'),
      },
      {
        x: 66,
        y: 36,
        check: t(
          'フランジの締まり',
          'Flange tight and flat',
          'ফ্ল্যাঞ্জ শক্ত ও সমতল',
        ),
      },
    ],
  },
  {
    id: 'clamp',
    defective: true,
    name: t('足場用クランプ', 'Scaffold clamp', 'স্ক্যাফোল্ড ক্ল্যাম্প'),
    spots: [
      {
        x: 30,
        y: 30,
        check: t(
          'ボルトの曲がり',
          'Bolt straight, not bent',
          'বোল্ট সোজা, বাঁকা নয়',
        ),
      },
      {
        x: 70,
        y: 30,
        check: t(
          '本体の変形・さび',
          'Body not deformed or rusted',
          'বডি বিকৃত বা মরচে ধরা নয়',
        ),
      },
    ],
  },
]
