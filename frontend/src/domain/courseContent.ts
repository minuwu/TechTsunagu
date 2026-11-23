import type { Drill, InspectItem, Panel } from './course'
import type { Tri } from './wakatta'

const t = (ja: string, en: string): Tri => ({ ja, en })

export const COURSE = {
  title: t('玉掛け', 'Crane slinging'),
  sub: t(
    '吊り荷を安全に掛けて、合図して、吊る。',
    'Rig a load, signal, and lift it safely.',
  ),
}

/** Door 1, Comic: `漢字{よみ}` carries the furigana. */
export const PANELS: Panel[] = [
  {
    who: 'foreman',
    text: t(
      '吊{つ}り荷{に}の下{した}に入{はい}るな！',
      'Never stand under a hanging load!',
    ),
  },
  {
    who: 'worker',
    text: t(
      '…わかりました。でも、ワイヤーの角度{かくど}は？',
      '…Understood. But what about the wire angle?',
    ),
  },
  {
    who: 'foreman',
    text: t(
      '60度以内{どいない}。広{ひろ}げるほど、ワイヤーに力{ちから}がかかる。',
      'Within 60°. The wider it opens, the more force on the wire.',
    ),
  },
]

// Plain-language summary, not statute text (spec §3.1); check the wording before it goes in front of a regulator
export const HANDBOOK: Tri[] = [
  t(
    '玉掛{たまが}けは、決{き}められた資格{しかく}のある人{ひと}だけが行{おこな}う。',
    'Only a qualified person may sling loads.',
  ),
  t(
    '吊{つ}り荷{に}の下{した}への立入{たちいり}は禁止{きんし}。',
    'Entering the area under a suspended load is prohibited.',
  ),
  t(
    '使用前{しようまえ}にワイヤーロープを点検{てんけん}する。',
    'Inspect the wire rope before every use.',
  ),
]

/** Door 2: Shisa Kanko (pointing and calling) lines, one per beat. */
export const REEL: Tri[] = [
  t('主電源{しゅでんげん}、OFF、ヨシ！', 'Main switch OFF, yoshi!'),
  t('ワイヤー、傷{きず}なし、ヨシ！', 'Wire, no damage, yoshi!'),
  t(
    '吊{つ}り角度{かくど}、60度以内{どいない}、ヨシ！',
    'Sling angle within 60°, yoshi!',
  ),
  t(
    '吊{つ}り荷{に}の下{した}、人{ひと}なし、ヨシ！',
    'Nobody under the load, yoshi!',
  ),
  t('合図{あいず}、巻上{まきあ}げ、ヨシ！', 'Signal, hoist, yoshi!'),
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
    name: t('危険予知（KYT）', 'Hazard spotting (KYT)'),
    goal: t('30秒で危険を4つ見つける。', 'Find 4 hazards in 30 seconds.'),
    items: [
      { hazard: true, label: t('床の油', 'Oil on the floor') },
      { hazard: false, label: t('消火器', 'Fire extinguisher') },
      {
        hazard: true,
        label: t('通路のケーブル', 'Cable across the walkway'),
      },
      { hazard: false, label: t('工具棚', 'Tool rack') },
      {
        hazard: true,
        label: t('カバーのない回転部', 'Moving part with no guard'),
      },
      {
        hazard: false,
        label: t('きれいな通路', 'Clear walkway'),
      },
      {
        hazard: true,
        label: t('吊り荷の下の人', 'Person under a hanging load'),
      },
      { hazard: true, label: t('ヘルメットなし', 'No helmet') },
    ],
  },
  {
    id: 'sling',
    kind: 'sling',
    loadKg: 1000,
    wllKg: 600,
    name: t('吊り角度と張力', 'Sling angle and tension'),
    goal: t(
      'ワイヤー1本の限度（600kgf）を超えない角度にする。',
      'Keep each wire under its 600 kgf limit.',
    ),
  },
  {
    id: 'estop',
    kind: 'reflex',
    name: t('非常停止の反射', 'E-stop reflex'),
    goal: t(
      '巻き込まれ！0.4秒以内に非常停止を押す。',
      'Entanglement! Hit E-stop within 0.4 s.',
    ),
  },
  {
    id: 'ring',
    kind: 'ring',
    wheels: [true, false, true],
    name: t('砥石の打音点検', 'Grinder wheel ring test'),
    goal: t(
      '叩いて音を聞く。澄んだ音は良品、鈍い音はひび割れ。',
      'Tap and listen. A clear ring is sound; a dull thud is cracked.',
    ),
  },
  seq(
    'ppe',
    t('保護具の装着', 'PPE checklist'),
    t('正しい順番で装着する。', 'Put on in the right order.'),
    [
      t('ヘルメット、あごひも', 'Helmet, chin strap'),
      t('安全帯のバックル確認', 'Harness, check buckles'),
      t('安全靴', 'Safety shoes'),
      t('手袋', 'Gloves'),
    ],
  ),
  seq(
    'confined',
    t('酸素濃度とロックアウト', 'Confined space: O2 and LOTO'),
    t('入る前の手順を正しい順番で。', 'Do the pre-entry steps in order.'),
    [
      t('バルブをロックアウト・タグアウト', 'Lock out and tag out the valve'),
      t('ガス検知器を校正', 'Calibrate the gas detector'),
      t('換気する', 'Ventilate'),
      t('酸素18%以上を確認', 'Confirm oxygen is 18% or more'),
    ],
  ),
  seq(
    'scaffold',
    t('移動式足場の設置', 'Rolling scaffold setup'),
    t(
      '登る前の手順を正しい順番で。',
      'Do the steps before climbing, in order.',
    ),
    [
      t('地面が平らか確認', 'Check the ground is level'),
      t('キャスターのブレーキをかける', 'Lock the caster brakes'),
      t('アウトリガーを張り出す', 'Extend the outriggers'),
      t('手すりを確認して登る', 'Check handrails, then climb'),
    ],
  ),
  seq(
    'welder',
    t('アーク溶接のアース', 'Arc welder grounding'),
    t('溶接前の手順を正しい順番で。', 'Do the steps before welding, in order.'),
    [
      t('電源OFFを確認', 'Confirm power is OFF'),
      t(
        'アースを母材にしっかり接続',
        'Clamp the earth firmly to the workpiece',
      ),
      t('ケーブルの傷を点検', 'Inspect the cable for damage'),
      t('電源ON', 'Switch power ON'),
    ],
  ),
  seq(
    'evac',
    t('津波・火災の避難', 'Tsunami / fire evacuation'),
    t(
      '警報から集合までを正しい順番で。',
      'From alarm to rally point, in order.',
    ),
    [
      t('機械を止める', 'Stop the machine'),
      t('荷物を持たず出口へ', 'Leave belongings, go to the exit'),
      t('高台へ向かう', 'Move to high ground'),
      t('集合場所で点呼', 'Roll call at the rally point'),
    ],
  ),
]

/** Door 4: tap the check points, then Pass (良品) or Reject (不良品). */
export const INSPECT: InspectItem[] = [
  {
    id: 'harness',
    defective: false,
    name: t('安全帯', 'Full-body harness'),
    spots: [
      {
        x: 22,
        y: 30,
        check: t('ベルトのほつれ・切れ', 'Frayed or cut webbing'),
      },
      {
        x: 74,
        y: 30,
        check: t('バックルの動き・変形', 'Buckle moves freely, not bent'),
      },
    ],
  },
  {
    id: 'wheel',
    defective: true,
    name: t('研削砥石', 'Grinder wheel'),
    spots: [
      {
        x: 38,
        y: 24,
        check: t('ひび・欠け', 'Cracks or chips'),
      },
      {
        x: 66,
        y: 36,
        check: t('フランジの締まり', 'Flange tight and flat'),
      },
    ],
  },
  {
    id: 'clamp',
    defective: true,
    name: t('足場用クランプ', 'Scaffold clamp'),
    spots: [
      {
        x: 30,
        y: 30,
        check: t('ボルトの曲がり', 'Bolt straight, not bent'),
      },
      {
        x: 70,
        y: 30,
        check: t('本体の変形・さび', 'Body not deformed or rusted'),
      },
    ],
  },
]
