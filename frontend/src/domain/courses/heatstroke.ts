import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string): Tri => ({ ja, en })

export const heatstroke: CourseData = {
  id: 'heatstroke',
  category: 'core',
  level: 1,
  minutes: 15,
  title: t('熱中症{ねっちゅうしょう}予防{よぼう}', 'Heat illness prevention'),
  sub: t(
    '水分{すいぶん}・塩分{えんぶん}補給{ほきゅう}とWBGTの確認{かくにん}で、熱中症{ねっちゅうしょう}から命{いのち}を守{まも}る。',
    'Protect lives from heat illness with hydration, salt intake, and WBGT checks.',
  ),
  refs: [
    // verify: 2025年6月1日施行の改正安衛則（報告体制整備および救護対応手順の策定義務）
    '労働安全衛生規則（安衛則）熱中症予防対策',
    '厚生労働省 職場における熱中症予防基本対策要綱',
    '環境省 熱中症予防情報サイト（暑さ指数：WBGT指針）',
  ],
  panels: [
    {
      who: 'foreman',
      text: t(
        '佐世保{させぼ}の夏{なつ}は湿気{しっけ}が高{たか}く危険{きけん}です。今日{きょう}は熱中症{ねっちゅうしょう}に注意{ちゅうい}しましょう。',
        'Summer in Sasebo is very humid and dangerous. Let us watch out for heat illness today.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '喉{のど}が渇{かわ}いたら、お昼{ひる}の休憩{きゅうけい}でたくさん水{みず}を飲{の}めば大丈夫{だいじょうぶ}ですか？',
        'If I feel thirsty, is it okay to drink plenty of water during lunch break?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'それは危険{きけん}です！喉{のど}が渇{かわ}く前{まえ}に、20分{ぷん}おきに水分{すいぶん}と塩分{えんぶん}をとってください。',
        'That is dangerous! Take water and salt every 20 minutes before you even feel thirsty.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '今週{こんしゅう}入社{にゅうしゃ}したばかりなので、周{まわ}りに迷惑{めいわく}をかけたくなくて我慢{がまん}していました……。',
        'I only joined this week, so I was holding back to avoid causing trouble for others...',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '最初{さいしょ}の1週{しゅう}間{かん}は体{からだ}が暑{あつ}さに慣{な}れていません。我慢{がまん}は絶対{ぜったい}にだめです。',
        'In the first week your body is not acclimated to heat. You must never endure it silently.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '少{すこ}し頭{あたま}が痛{いた}くて、立ちくらみがします。どうすればいいですか？',
        'I have a slight headache and feel lightheaded. What should I do?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'すぐ日陰{ひかげ}の休憩所{きゅうけいじょ}へ行{い}きましょう！服{ふく}をゆるめて首{くび}と脇{わき}を冷{ひ}やし、経口補水液{けいこうほすいえき}を飲{の}みます。',
        'Let us go to the shaded rest area right now! Loosen your clothes, cool neck and armpits, and drink ORS.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '冷風{れいふう}で休{やす}んだら楽{らく}になりました。一人{ひとり}で我慢{がまん}せず、すぐに相談{そうだん}します！',
        'Resting in the cool air helped me recover. I will never hold back alone and will speak up right away!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'お互{たが}いに声{こえ}をかけ合{あ}う「バディ確認{かくにん}」で、暑{あつ}い現場{げんば}を乗{の}り切{き}りましょう！',
        'Let us overcome this hot workplace by checking on each other through buddy checks!',
      ),
    },
  ],
  handbook: [
    t(
      '喉{のど}が渇{かわ}く前{まえ}に、こまめに水分{すいぶん}と塩分{えんぶん}を補給{ほきゅう}する。',
      'Drink water and replenish salt frequently before you feel thirsty.',
    ),
    t(
      '作業{さぎょう}前{まえ}にWBGT（暑{あつ}さ指数{しすう}）を確認{かくにん}し、基準{きじゅん}に応{おう}じて休憩{きゅうけい}をとる。',
      'Check the WBGT (heat index) before work and take breaks according to guidelines.',
    ),
    t(
      '入社{にゅうしゃ}直後{ちょくご}や休{やす}み明{あ}けは暑熱順化{しょねつじゅんか}が不十分{ふじゅうぶん}なため、作業量{さぎょうりょう}を控{ひか}えめにする。',
      'Reduce workload when starting a new job or returning from leave, as heat acclimatization is incomplete.',
    ),
    t(
      '通気性{つうきせい}の良{よ}い作業着{さぎょうぎ}やファン付{つ}きウェアを着用{ちゃくよう}して体温{たいおん}の上昇{じょうしょう}を防{ふせ}ぐ。',
      'Wear breathable work clothes or fan-equipped cooling jackets to prevent body temperature rise.',
    ),
    t(
      'めまい・頭痛{ずつう}・吐{は}き気{け}を感{かん}じたら、我慢{がまん}せず直{ただ}ちに作業{さぎょう}を中断{ちゅうだん}する。',
      'If feeling dizziness, headache, or nausea, stop work immediately without holding back.',
    ),
    t(
      '体調{たいちょう}の異変{いへん}は自分{じぶん}だけで抱{かか}え込{こ}まず、すぐに職長{しょくちょう}やバディに報告{ほうこく}する。',
      'Never hide physical discomfort; report immediately to your foreman or buddy.',
    ),
    t(
      'バディ同士{どうし}で顔色{かおいろ}や汗{あせ}のかき方{かた}を観察{かんさつ}し、お互{たが}いの体調{たいちょう}を見守{みまも}る。',
      "Observe your buddy's facial color and sweating patterns to watch over each other's condition.",
    ),
    t(
      '熱中症{ねっちゅうしょう}の疑{うたが}いがある人{ひと}を涼{すず}しい場所{ばしょ}へ移{うつ}し、絶対{ぜったい}に一人{ひとり}で放置{ほうち}しない。',
      'Move anyone suspected of heat illness to a cool spot and never leave them alone.',
    ),
    t(
      '意識{いしき}がない場合{ばあい}や自力{じりき}で水分{すいぶん}が飲{の}めないときは、無理{むり}に飲{の}ませず直{ただ}ちに119番通報{ばんつうほう}する。',
      'If unconscious or unable to drink independently, never force fluids and call 119 immediately.',
    ),
    t(
      '前夜{ぜんや}の睡眠不足{すいみんぶそく}や朝食{ちょうしょく}抜{ぬ}きは熱中症{ねっちゅうしょう}のリスクを高{たか}めるため、体調管理{たいちょうかんり}を徹底{てってい}する。',
      'Lack of sleep or skipping breakfast increases heat illness risk; maintain strict physical self-care.',
    ),
  ],
  reel: [
    t(
      '水分{すいぶん}・塩分{えんぶん}補給{ほきゅう}、準備{じゅんび}、ヨシ！',
      'Hydration and salt supply, ready, yoshi!',
    ),
    t(
      'WBGT測定値{そくていち}、確認{かくにん}、ヨシ！',
      'WBGT reading, checked, yoshi!',
    ),
    t(
      '休憩所{きゅうけいじょ}のエアコン、稼働{かどう}、ヨシ！',
      'Rest area air conditioner, running, yoshi!',
    ),
    t(
      'ファン付{つ}きウェア、バッテリー作動{さどう}、ヨシ！',
      'Fan jacket, battery running, yoshi!',
    ),
    t(
      'バディの顔色{かおいろ}、異常{いじょう}なし、ヨシ！',
      "Buddy's face color, normal, yoshi!",
    ),
    t(
      '首筋{くびすじ}と脇{わき}の冷却{れいきゃく}材{ざい}、常備{じょうび}、ヨシ！',
      'Cooling packs for neck and armpits, stocked, yoshi!',
    ),
    t(
      '体調{たいちょう}の異変{いへん}、我慢{がまん}せず報告{ほうこく}、ヨシ！',
      'Report condition changes without enduring silently, yoshi!',
    ),
  ],
  drills: [
    {
      id: 'heatstroke-spot',
      kind: 'spot',
      secs: 30,
      need: 4,
      name: t(
        '熱中症{ねっちゅうしょう}の危険{きけん}要因{よういん}',
        'Heat illness hazard spotting',
      ),
      goal: t(
        '30秒{びょう}で不安全{ふあんぜん}な状態{じょうたい}や行動{こうどう}を4つ見{み}つける。',
        'Find 4 unsafe conditions or actions in 30 seconds.',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '直射日光{ちょくしゃにっこう}の下{した}で帽子{ぼうし}を被{かぶ}らず休憩{きゅうけい}なしで作業{さぎょう}',
            'Working under direct sun without a hat and without breaks',
          ),
        },
        {
          hazard: false,
          label: t(
            '水筒{すいとう}と経口補水液{けいこうほすいえき}を作業場{さぎょうば}に携帯{けいたい}',
            'Carrying a water bottle and ORS to the work area',
          ),
        },
        {
          hazard: true,
          label: t(
            '喉{のど}が渇{かわ}いているのに「あとで飲{の}む」と水分{すいぶん}補給{ほきゅう}を我慢{がまん}',
            'Feeling thirsty but holding back on drinking, saying "later"',
          ),
        },
        {
          hazard: false,
          label: t(
            '通風{つうふう}の良{よ}い日陰{ひかげ}で定期{ていき}的{てき}に休息{きゅうそく}',
            'Resting regularly in well-ventilated shade',
          ),
        },
        {
          hazard: true,
          label: t(
            '体調不良{たいちょうふりょう}を訴{うった}えた同僚{どうりょう}を日陰{ひかげ}に一人{ひとり}きりで放置{ほうち}',
            'Leaving a sick coworker completely alone in the shade',
          ),
        },
        {
          hazard: false,
          label: t(
            '朝礼{ちょうれい}でバディ同士{どうし}の顔色{かおいろ}と汗{あせ}を確認{かくにん}',
            "Checking buddy's facial color and sweat at morning meeting",
          ),
        },
        {
          hazard: true,
          label: t(
            '意識{いしき}が混濁{こんだく}している人{ひと}の口{くち}に無理{むり}やり水{みず}を流{なが}し込{こ}む',
            'Forcing water into the mouth of a semi-conscious person',
          ),
        },
        {
          hazard: false,
          label: t(
            '塩分{えんぶん}タブレットを定時{ていじ}に摂取{せっしゅ}して予防{よぼう}',
            'Taking salt tablets on schedule for prevention',
          ),
        },
        {
          hazard: true,
          label: t(
            '前夜{ぜんや}の睡眠不足{すいみんぶそく}と朝食{ちょうしょく}抜{ぬ}きのまま高温{こうおん}作業{さぎょう}',
            'Working in high heat after lack of sleep and skipping breakfast',
          ),
        },
      ],
    },
    {
      id: 'heatstroke-first-aid-seq',
      kind: 'seq',
      name: t(
        '熱中症{ねっちゅうしょう}の応急手当{おうきゅうてあて}手順{てじゅん}',
        'First aid sequence for heat illness',
      ),
      goal: t(
        '体調不良{たいちょうふりょう}者{しゃ}が出{で}た時{とき}の手順{てじゅん}を正{ただ}しい順番{じゅんばん}に並{なら}べる。',
        'Arrange the emergency steps in the correct order.',
      ),
      steps: [
        t(
          '涼{すず}しい日陰{ひかげ}や冷房{れいぼう}の効{き}いた室内{しつない}へ速{すみ}やかに運{はこ}ぶ',
          'Promptly move person to cool shade or air-conditioned room',
        ),
        t(
          '衣服{いふく}をゆるめ、ベルトや作業靴{さぎょうぐつ}を外{はず}して熱{ねつ}を逃{に}がす',
          'Loosen clothing, unbuckle belt and shoes to release heat',
        ),
        t(
          '首{くび}の付{つ}け根{ね}・脇{わき}の下{した}・太{ふと}ももの付{つ}け根{ね}を保冷剤{ほれいざい}で冷{ひ}やす',
          'Cool base of neck, armpits, and groin with ice packs',
        ),
        t(
          '自力{じりき}で飲{の}めることを確認{かくにん}して冷{つめ}たい水分{すいぶん}・塩分{えんぶん}を補給{ほきゅう}する',
          'Provide cool fluids and salt only after confirming they can drink alone',
        ),
        t(
          '意識{いしき}がもうろうとしている場合{ばあい}は直{ただ}ちに119番通報{ばんつうほう}する',
          'Call 119 immediately if person shows clouded consciousness',
        ),
      ],
    },
    {
      id: 'heat-acclimation-seq',
      kind: 'seq',
      name: t(
        '作業前{さぎょうまえ}の熱中症{ねっちゅうしょう}予防{よぼう}手順{てじゅん}',
        'Pre-work heat illness prevention routine',
      ),
      goal: t(
        '朝{あさ}の作業前{さぎょうまえ}に行{おこな}う予防{よぼう}行動{こうどう}を正{ただ}しい順番{じゅんばん}に並{なら}べる。',
        'Arrange morning pre-work prevention actions in correct order.',
      ),
      steps: [
        t(
          '朝食{ちょうしょく}の摂取{せっしゅ}と睡眠{すいみん}時間{じかん}の自己{じこ}チェックを行{おこな}う',
          'Self-check breakfast intake and sleep duration',
        ),
        t(
          '現場{げんば}のWBGT（暑{あつ}さ指数{しすう}）測定値{そくていち}を確認{かくにん}する',
          'Check on-site WBGT (heat index) reading',
        ),
        t(
          '十分{じゅうぶん}な冷水{れいすい}・経口補水液{けいこうほすいえき}・塩分{えんぶん}を作業場{さぎょうば}に用意{ようい}する',
          'Prepare enough cool water, ORS, and salt at the workstation',
        ),
        t(
          '朝礼{ちょうれい}でバディとお互{たが}いの体調{たいちょう}を指差呼称{しさこしょう}で確認{かくにん}する',
          "Confirm each other's condition with buddy at morning meeting",
        ),
      ],
    },
    {
      id: 'heat-emergency-reflex',
      kind: 'reflex',
      name: t(
        '緊急{きんきゅう}救護{きゅうご}の反射{はんしゃ}',
        'Emergency rescue reflex',
      ),
      goal: t(
        '同僚{どうりょう}が倒{たお}れて意識{いしき}がない！0.4秒{びょう}以内{いない}に非常通報{ひじょうつうほう}ボタンを押{お}す。',
        'Coworker collapsed unconscious! Hit emergency alarm within 0.4s.',
      ),
    },
  ],
  inspect: [
    {
      id: 'cool-rest-shelter',
      defective: false,
      name: t(
        '涼{すず}しい休憩所{きゅうけいじょ}の設備{せつび}',
        'Cool rest shelter facilities',
      ),
      spots: [
        {
          x: 28,
          y: 32,
          check: t(
            'エアコン・冷房{れいぼう}：冷風{れいふう}が十分{じゅうぶん}に出{で}て室温{しつおん}が下{さ}がっている（良品{りょうひん}：冷気{れいき}あり / 不良{ふりょう}：故障{こしょう}・高温{こうおん}）',
            'Air conditioner: Cool air flowing and lowering room temperature (Good: Cold air / Defect: Broken, hot)',
          ),
        },
        {
          x: 72,
          y: 45,
          check: t(
            '水分{すいぶん}・塩分{えんぶん}補給{ほきゅう}所{しょ}：冷水{れいすい}・経口補水液{けいこうほすいえき}・塩分{えんぶん}飴{あめ}が常備{じょうび}されている（良品{りょうひん}：満杯{まんぱい} / 不良{ふりょう}：品切{しなぎ}れ）',
            'Hydration station: Cool water, ORS, and salt tablets stocked (Good: Full / Defect: Empty)',
          ),
        },
        {
          x: 50,
          y: 75,
          check: t(
            '冷却{れいきゃく}用具{ようぐ}：首筋{くびすじ}や脇{わき}を冷{ひ}やすアイスパックが氷結{ひょうけつ}している（良品{りょうひん}：凍結{とうけつ} / 不良{ふりょう}：ぬるい・なし）',
            'Cooling gear: Ice packs for neck and armpits properly frozen (Good: Frozen / Defect: Warm, missing)',
          ),
        },
      ],
    },
    {
      id: 'cooling-fan-vest',
      defective: true,
      name: t('ファン付{つ}き作業着{さぎょうぎ}', 'Fan-equipped cooling vest'),
      spots: [
        {
          x: 35,
          y: 40,
          check: t(
            'ファンモーター：異音{いおん}がなく十分{じゅうぶん}な風量{ふうりょう}で回転{かいてん}している（良品{りょうひん}：円滑{えんかつ}回転{かいてん} / 不良{ふりょう}：異音{いおん}・停止{ていし}）',
            'Fan motor: Spinning smoothly with ample airflow without strange noise (Good: Smooth / Defect: Noise, stopped)',
          ),
        },
        {
          x: 68,
          y: 55,
          check: t(
            '接続{せつぞく}コード：断線{だんせん}や被覆{ひふく}の破れがなくバッテリーが充電{じゅうでん}されている（良品{りょうひん}：健全{けんぜん} / 不良{ふりょう}：コード露出{ろしゅつ}・破損{はそん}）',
            'Connecting cord: No fraying or exposed wire, battery charged (Good: Sound / Defect: Wire exposed, damaged)',
          ),
        },
      ],
    },
    {
      id: 'wbgt-checker',
      defective: false,
      name: t(
        'WBGT（暑{あつ}さ指数{しすう}）測定器{そくていき}',
        'WBGT heat index monitor',
      ),
      spots: [
        {
          x: 42,
          y: 30,
          check: t(
            '黒球{こっきゅう}・センサー：破損{はそん}や埃{ほこり}の付着{ふちゃく}がなく直射日光{ちょくしゃにっこう}を受けられる（良品{りょうひん}：清潔{せいけつ}・正常{せいじょう} / 不良{ふりょう}：汚損{おそん}・割れ）',
            'Black globe / sensor: Clean without dust or cracks to measure direct radiation (Good: Clean / Defect: Dirty, cracked)',
          ),
        },
        {
          x: 58,
          y: 68,
          check: t(
            '画面{がめん}表示{ひょうじ}：WBGT値{ち}と温湿度{おんしつど}が鮮明{せんめい}に表示{ひょうじ}され電池{でんち}残量{ざんりょう}がある（良品{りょうひん}：明瞭{めいりょう} / 不良{ふりょう}：文字{もじ}欠け・電池切{でんちぎ}れ）',
            'Display screen: WBGT and humidity clearly shown with battery power (Good: Clear / Defect: Missing digits, low battery)',
          ),
        },
      ],
    },
  ],
  quiz: [
    {
      q: t(
        '夏{なつ}の現場{げんば}での水分{すいぶん}補給{ほきゅう}のタイミングとして、最も正{ただ}しいものはどれですか？',
        'What is the most correct timing for hydration at a summer worksite?',
      ),
      options: [
        t(
          '喉{のど}が渇{かわ}く前{まえ}に、定期的{ていきてき}に水分{すいぶん}と塩分{えんぶん}を補給{ほきゅう}する',
          'Replenish water and salt regularly before feeling thirsty',
        ),
        t(
          '喉{のど}が渇{かわ}ききってから、一度{いちど}に大量{たいりょう}の真水{まみず}を飲{の}む',
          'Drink a large amount of plain water all at once after getting very thirsty',
        ),
        t(
          'お昼{ひる}の休憩{きゅうけい}まで我慢{がまん}して、昼食{ちゅうしょく}時{じ}だけ飲{の}む',
          'Hold out until lunch break and drink only during lunch',
        ),
        t(
          '汗{あせ}をかかないように、水分{すいぶん}の摂取{せっしゅ}を極力{きょくりょく}控{ひか}える',
          'Refrain from drinking water as much as possible to avoid sweating',
        ),
      ],
      answer: 0,
      why: t(
        '喉{のど}の渇{かわ}きを感{かん}じた時点{じてん}ですでに脱水{だっすい}が始{はじ}まっているため、渇{かわ}く前{まえ}にこまめな水分{すいぶん}と塩分{えんぶん}補給{ほきゅう}が必要{ひつよう}です。',
        'Dehydration is already underway by the time thirst is felt, making regular hydration and salt intake necessary before thirst strikes.',
      ),
    },
    {
      q: t(
        '入社{にゅうしゃ}して1週目{いっしゅうめ}の外国人{がいこくじん}技能{ぎのう}実習生{じっしゅうせい}が作業{さぎょう}する際{さい}、熱中症{ねっちゅうしょう}対策{たいさく}で特{とく}に注意{ちゅうい}すべき点{てん}は何{なに}ですか？',
        'What must be especially watched out for regarding heat illness for a newly arrived technical intern in their first week?',
      ),
      options: [
        t(
          'ベテラン作業員{さぎょういん}と同{おな}じペースで初日{しょにち}から全力{ぜんりょく}で作業{さぎょう}する',
          'Work at full capacity at the same pace as experienced workers from day one',
        ),
        t(
          '暑熱順化{しょねつじゅんか}（暑{あつ}さへの慣{な}れ）ができていないため、作業量{さぎょうりょう}を控{ひか}え休憩{きゅうけい}を多{おお}くする',
          'Limit workload and take more breaks because the body is not yet acclimatized to heat',
        ),
        t(
          '迷惑{めいわく}をかけないように体調不良{たいちょうふりょう}を言{い}い出{だ}さず我慢{がまん}する',
          'Endure illness silently without speaking up to avoid troubling others',
        ),
        t(
          '水分{すいぶん}補給{ほきゅう}の回数{かいすう}を減{へ}らして作業{さぎょう}時間{じかん}を優先{ゆうせん}する',
          'Reduce hydration breaks to prioritize work hours',
        ),
      ],
      answer: 1,
      why: t(
        '体{からだ}が暑{あつ}さに慣{な}れる暑熱順化{しょねつじゅんか}には数日{すうじつ}から1週間{いっしゅうかん}程度{ていど}かかるため、初週{しょしゅう}は作業{さぎょう}強度{きょうど}の配慮{はいりょ}が不可欠{ふかけつ}です。',
        'Heat acclimatization takes several days to a week, so adjusting workload during the first week is essential.',
      ),
    },
    {
      q: t(
        '同僚{どうりょう}が「少{すこ}しめまいがして頭{あたま}が痛{いた}い」と訴{うった}えてきました。最初{さいしょ}にとるべき正{ただ}しい行動{こうどう}はどれですか？',
        'A coworker complains of slight dizziness and a headache. What is the correct initial action to take?',
      ),
      options: [
        t(
          '「気合{きあい}が足{た}りない」と励{はげ}まして作業{さぎょう}を続{つづ}けさせる',
          'Encourage them that they just lack willpower and tell them to continue working',
        ),
        t(
          'その場{ば}で立{た}ったまま深呼吸{しんこきゅう}をさせて様子{ようす}を見{み}る',
          'Have them stand where they are and take deep breaths while watching them',
        ),
        t(
          '直{ただ}ちに作業{さぎょう}を中断{ちゅうだん}させ、涼{すず}しい日陰{ひかげ}や冷房{れいぼう}室{しつ}へ移動{いどう}させる',
          'Immediately halt work and move them to cool shade or an air-conditioned room',
        ),
        t(
          '一人{ひとり}で歩{ある}かせて遠{とお}くの医務室{いむしつ}へ向{む}かわせる',
          'Have them walk alone to a distant infirmary',
        ),
      ],
      answer: 2,
      why: t(
        '熱中症{ねっちゅうしょう}の初期{しょき}症状{しょうじょう}が疑{うたが}われる場合{ばあい}は、直{ただ}ちに作業{さぎょう}を中止{ちゅうし}して涼{すず}しい環境{かんきょう}へ避難{ひなん}させることが最優先{さいゆうせん}です。',
        'If early symptoms of heat illness are suspected, stopping work and evacuating to a cool environment is top priority.',
      ),
    },
    {
      q: t(
        '現場{げんば}で作業者{さぎょうしゃ}が倒{たお}れ、呼{よ}びかけても返事{へんじ}がなく意識{いしき}がありません。最初{さいしょ}に行{おこな}うべき対応{たいおう}はどれですか？',
        'A worker collapses on site, gives no response when called, and is unconscious. What is the first response to take?',
      ),
      options: [
        t(
          '目{め}を覚{さ}まさせるために冷水{れいすい}を顔{かお}に掛{か}けて叩{たた}く',
          'Splash cold water on their face and slap them to wake them up',
        ),
        t(
          '水筒{すいとう}のお茶{ちゃ}を無理{むり}やり口{くち}の中{なか}へ流{なが}し込{こ}む',
          'Force tea from a water bottle into their mouth',
        ),
        t(
          '静{しず}かに日陰{ひかげ}で寝{ね}かせたまま一人{ひとり}にして30分{ぷん}待{ま}つ',
          'Let them sleep quietly in the shade alone and wait 30 minutes',
        ),
        t(
          '直{ただ}ちに119番通報{ばんつうほう}（救急車{きゅうきゅうしゃ}要請{ようせい}）を行{おこな}い、AEDと救護{きゅうご}を手配{てはい}する',
          'Immediately call 119 for an ambulance and arrange for an AED and rescue assistance',
        ),
      ],
      answer: 3,
      why: t(
        '意識障害{いしきしょうがい}がある熱中症{ねっちゅうしょう}は命{いのち}に関{かか}わる重症{じゅうしょう}（III度）であり、ためらわずに直{ただ}ちに119番通報{ばんつうほう}しなければなりません。',
        'Heat illness with impaired consciousness is life-threatening (Stage III) and requires immediate 119 emergency call without hesitation.',
      ),
    },
    {
      q: t(
        '倒{たお}れた人{ひと}の体温{たいおん}を下{さ}げるため保冷剤{ほれいざい}や氷嚢{ひょうのう}を当{あ}てる場所{ばしょ}として、最{もっと}も効果{こうか}的{てき}な部位{ぶい}はどこですか？',
        'Where is the most effective location to apply ice packs or cooling packs to lower the body temperature of a collapsed person?',
      ),
      options: [
        t(
          '首{くび}の付{つ}け根{ね}の両側{りょうがわ}・脇{わき}の下{した}・太{ふと}ももの付{つ}け根{ね}（股関節{こかんせつ}）',
          'Both sides of neck base, armpits, and groin area',
        ),
        t(
          '手{て}のひらと足{あし}の甲{こう}の表面{ひょうめん}',
          'Palms of hands and tops of feet',
        ),
        t(
          'ヘルメットの上{うえ}と安全靴{さぎょうぐつ}の先端{せんたん}',
          'On top of helmet and tips of safety shoes',
        ),
        t(
          '背中{せなか}の中央{ちゅうおう}とお尻{しり}の筋肉{きんにく}',
          'Middle of back and buttocks muscles',
        ),
      ],
      answer: 0,
      why: t(
        '太{ふと}い血管{けっかん}が皮膚{ひふ}の近{ちか}くを通{とお}っている首{くび}・脇{わき}・太{ふと}ももの付{つ}け根{ね}を冷{ひ}やすことで、効率{こうりつ}よく全身{ぜんしん}の体温{たいおん}を下{さ}げられます。',
        'Cooling the neck, armpits, and groin where major blood vessels run close to the skin cools circulating blood and lowers body temperature efficiently.',
      ),
    },
    {
      q: t(
        'WBGT（暑{あつ}さ指数{しすう}）が基準値{きじゅんち}を超{こ}えるような極度{きょくど}の高温{こうおん}環境{かんきょう}下{か}での作業{さぎょう}管理{かんり}として、正{ただ}しいものはどれですか？',
        'What is the correct work management approach in extremely hot environments where WBGT exceeds reference thresholds?',
      ),
      options: [
        t(
          '暑{あつ}い現場{げんば}ほど休憩{きゅうけい}回数{かいすう}を減{へ}らして短時間{たんじかん}で一気{いっき}に作業{さぎょう}を終{お}わらせる',
          'Reduce breaks in hotter sites to finish work all at once in short duration',
        ),
        t(
          '休憩{きゅうけい}時間{じかん}と頻度{ひんど}を増{ふ}やし、WBGT値{ち}に応{おう}じて重作業{じゅうさぎょう}の中止{ちゅうし}や負荷{ふか}軽減{けいげん}を行{おこな}う',
          'Increase break time and frequency, and halt heavy labor or reduce load based on WBGT',
        ),
        t(
          'WBGT値{ち}は見{み}ずに、作業員{さぎょういん}個人{こじん}の感覚{かんかく}だけに任{まか}せて作業{さぎょう}を続行{ぞっこう}する',
          "Ignore WBGT values and continue work relying solely on individual workers' feelings",
        ),
        t(
          '直射日光{ちょくしゃにっこう}を避{さ}けるための遮光{しゃこう}ネットや扇風機{せんぷうき}の設置{せっち}を禁止{きんし}する',
          'Prohibit sunshades and industrial fans intended to block direct sunlight',
        ),
      ],
      answer: 1,
      why: t(
        'WBGT値{ち}が高{たか}い日{ひ}は作業{さぎょう}基準{きじゅん}に基{づ}き、休憩{きゅうけい}時間{じかん}の延長{えんちょう}や作業{さぎょう}負荷{ふか}の軽減{けいげん}、場合{ばあい}によっては作業{さぎょう}中止{ちゅうし}を判断{はんだん}します。',
        'On high-WBGT days, work protocols mandate extending rest periods, reducing labor intensity, or halting operations.',
      ),
    },
    {
      q: t(
        '現場{げんば}で導入{どうにゅう}されている「バディ確認{かくにん}」の目的{もくてき}として、最{もっと}も適切{てきせつ}なものはどれですか？',
        'What is the most appropriate purpose of the "buddy check" system implemented at worksites?',
      ),
      options: [
        t(
          '同僚{どうりょう}の作業{さぎょう}スピードを監視{かんし}して遅{おく}れを叱責{しっせき}するため',
          "To monitor coworkers' working speed and scold them for delays",
        ),
        t(
          '道具{どうぐ}の貸{か}し借{か}りだけを二人{ふたり}の間{あいだ}で行{おこな}うため',
          'To restrict borrowing and lending of tools exclusively between two people',
        ),
        t(
          '二人{ふたり}一組{ひとくみ}でお互{たが}いの顔色{かおいろ}や汗{あせ}・異変{いへん}に早{はや}く気{き}づいて声{こえ}をかけ合{あ}うため',
          'To pair up and notice changes in facial color, sweat, and abnormalities early to speak up',
        ),
        t(
          '休憩{きゅうけい}中{ちゅう}に仕事{しごと}以外{いがい}の会話{かいわ}をしないように監視{かんし}するため',
          'To monitor coworkers so they do not engage in casual conversation during breaks',
        ),
      ],
      answer: 2,
      why: t(
        '熱中症{ねっちゅうしょう}は本人{ほんにん}が気づかないうちに判断力{はんだんりょく}が低下{ていか}するため、バディ同士{どうし}で相互{そうご}に体調{たいちょう}を確認{かくにん}し合{あ}うことが極{きわ}めて有効{ゆうこう}です。',
        'Because heat illness impairs judgment without the person realizing it, mutual monitoring between buddies is vital for early detection.',
      ),
    },
    {
      q: t(
        '熱中症{ねっちゅうしょう}で気分{きぶん}が悪{わる}くなり横{よこ}になっている同僚{どうりょう}への対応{たいおう}として、絶対{ぜったい}にしてはならないことはどれですか？',
        'What must you NEVER do when assisting a coworker lying down due to heat illness?',
      ),
      options: [
        t(
          'ネクタイや作業着{さぎょうぎ}のボタンを外{はず}して襟元{えりもと}を緩{ゆる}めること',
          'Unbuttoning work shirt collar to loosen tight clothing',
        ),
        t(
          'エアコンの効{き}いた涼{すず}しい部屋{へや}へ運{はこ}び風{かぜ}を当{あ}てること',
          'Moving them to an air-conditioned cool room and fanning them',
        ),
        t(
          '氷嚢{ひょうのう}や冷{つめ}たいペットボトルで首筋{くびすじ}を冷{ひ}やすこと',
          'Cooling their neck with an ice pack or cold drink bottle',
        ),
        t(
          '「良{よ}くなるまで寝{ね}ていて」と日陰{ひかげ}に一人{ひとり}きりで放置{ほうち}して立{た}ち去{さ}ること',
          'Leaving them completely alone in the shade telling them to "rest until better"',
        ),
      ],
      answer: 3,
      why: t(
        '熱中症{ねっちゅうしょう}は急激{きゅうげき}に容態{ようだい}が悪化{あっか}する危険{きけん}があるため、回復{かいふく}するまで誰{だれ}かが必{かなら}ず付{つ}き添{そ}わなければなりません。',
        'Heat illness can deteriorate rapidly, so someone must always remain by their side until recovery.',
      ),
    },
    {
      q: t(
        '水分{すいぶん}補給{ほきゅう}の際{さい}、真水{まみず}だけを大量{たいりょう}に飲{の}むことの危険性{きけんせい}について正{ただ}しい説明{せつめい}はどれですか？',
        'What is the correct explanation of the risk of drinking large amounts of only plain water during heavy sweating?',
      ),
      options: [
        t(
          '汗{あせ}で失{うしな}われた塩分{えんぶん}が補給{ほきゅう}されず血中{けっちゅう}の塩分{えんぶん}濃度{のうど}が下{さ}がり、筋肉{きんにく}のこむら返りや重症{じゅうしょう}化{か}を招{まね}く',
          'Salt lost in sweat is not replaced, diluting blood sodium and causing muscle cramps and worsening condition',
        ),
        t(
          '真水{まみず}だけを飲{の}むと胃{い}が冷{ひ}えすぎて瞬時{しゅんじ}に熱中症{ねっちゅうしょう}が完治{かんち}してしまう',
          'Drinking plain water overcools the stomach and cures heat illness instantly',
        ),
        t(
          '塩分{えんぶん}は汗{あせ}と共{とも}に排泄{はいせつ}されないため、水{みず}だけを飲{の}むのが最善{さいぜん}である',
          'Salt is not lost in sweat, so drinking only water is always best',
        ),
        t(
          '真水{まみず}を飲{の}むとすぐに尿{にょう}が出{で}なくなるため現場{げんば}では禁止{きんし}されている',
          'Drinking plain water immediately halts urination and is therefore banned on site',
        ),
      ],
      answer: 0,
      why: t(
        '大量{たいりょう}の発汗{はっかん}時{じ}に水{みず}だけを飲{の}むと体液{たいえき}の塩分{えんぶん}濃度{のうど}が薄{うす}まり、自発的{じはつてき}脱水{だっすい}や有痛性{ゆうつうせい}筋痙攣{きんけいれん}（こむら返り）を引{ひ}き起{お}こします。',
        'Drinking only water during heavy sweat dilutes bodily sodium, triggering spontaneous dehydration and painful heat cramps.',
      ),
    },
    {
      q: t(
        '熱中症{ねっちゅうしょう}の軽度{けいど}な症状{しょうじょう}（I度）に当{あ}てはまるものの組{く}み合{あ}わせはどれですか？',
        'Which combination corresponds to mild (Stage I) symptoms of heat illness?',
      ),
      options: [
        t(
          '意識{いしき}の消失{しょうしつ}、全身{ぜんしん}の痙攣{けいれん}、呼{よ}びかけに無反応{むはんのう}',
          'Loss of consciousness, full-body convulsions, no response to calls',
        ),
        t(
          'めまい、立ちくらみ、筋肉痛{きんにくつう}・こむら返り、大豆粒{だいずりゅう}のような大量{たいりょう}の汗{あせ}',
          'Dizziness, lightheadedness, muscle pain/cramps, heavy sweating with large sweat beads',
        ),
        t(
          '全身{ぜんしん}の激{はげ}しい骨折{こっせつ}と出血{しゅっけつ}',
          'Severe fractures and bleeding across the body',
        ),
        t(
          '呼吸{こきゅう}停止{ていし}および脈拍{みゃくはく}の消失{しょうしつ}',
          'Respiratory arrest and absent pulse',
        ),
      ],
      answer: 1,
      why: t(
        '立ちくらみ・めまい・筋肉{きんにく}のこむら返りは熱中症{ねっちゅうしょう}の初期{しょき}サイン（I度）であり、この段階{だんかい}で休息{きゅうそく}と水分{すいぶん}・塩分{えんぶん}補給{ほきゅう}を行{おこな}えば回復{かいふく}します。',
        'Dizziness and muscle cramps are early warning signs (Stage I); rest and hydration with salt at this stage will enable recovery.',
      ),
    },
    {
      q: t(
        '現場{げんば}の服装{ふくそう}や装備{そうび}による熱中症{ねっちゅうしょう}予防{よぼう}として、最{もっと}も適切{てきせつ}なものはどれですか？',
        'Which clothing or equipment practice is most appropriate for preventing heat illness on site?',
      ),
      options: [
        t(
          '汗{あせ}を蒸発{じょうはつ}させないために通気性{つうきせい}ゼロの厚手{あつで}ゴム手袋{てぶくろ}とカッパを着{き}る',
          'Wear non-breathable thick rubber gloves and rainwear to trap sweat',
        ),
        t(
          '暑{あつ}いのでヘルメットを脱{ぬ}いで直射日光{ちょくしゃにっこう}を作業中{さぎょうちゅう}に頭{あたま}へ直接{ちょくせつ}受{う}ける',
          'Take off helmet because it is hot and expose head directly to blazing sun during work',
        ),
        t(
          '通気性{つうきせい}・吸汗速乾性{きゅうかんそっかんせい}の高{たか}いインナーやファン付{つ}き作業着{さぎょうぎ}を活用{かつよう}する',
          'Utilize highly breathable, moisture-wicking innerwear and fan-equipped cooling workwear',
        ),
        t(
          '防暑{ぼうしょ}タレ（日除{ひよ}け布{ぬの}）をヘルメットから取{と}り外{はず}して捨{す}てる',
          'Remove and discard sunshade flaps from hard hats',
        ),
      ],
      answer: 2,
      why: t(
        '吸汗速乾性{きゅうかんそっかんせい}素材{そざい}やファン付{つ}きウェアを使用{しよう}することで、汗{あせ}の気化熱{きかねつ}を利用{りよう}して体感{たいかん}温度{おんど}と体温{たいおん}の上昇{じょうしょう}を抑{おさ}えられます。',
        'Using moisture-wicking materials and fan vests utilizes evaporative cooling to lower perceived temperature and suppress body heat buildup.',
      ),
    },
    {
      q: t(
        '2025年{ねん}6月{がつ}の安衛則{あんえいそく}改正{かいせい}に伴{ともな}い、事業者{じぎょうしゃ}および現場{げんば}で義務化{ぎむか}される熱中症{ねっちゅうしょう}対策{たいさく}の重点{じゅうてん}は何{なに}ですか？',
        'Under the Industrial Safety and Health Regulations revision taking effect in June 2025, what is a key mandatory heat illness measure for employers and sites?',
      ),
      options: [
        t(
          '夏場{なつば}の作業員{さぎょういん}の給与{きゅうよ}を半額{はんがく}に減額{げんがく}する制度{せいど}',
          "A system halving workers' wages during summer months",
        ),
        t(
          '40度{ど}を超{こ}える日{ひ}も一切{いっさい}の休憩{きゅうけい}を禁{きん}じる罰則{ばっそく}規定{きてい}',
          'A penalty clause forbidding any breaks even on days exceeding 40 degrees',
        ),
        t(
          '熱中症{ねっちゅうしょう}になった作業員{さぎょういん}の氏名{しめい}を構内{こうない}に掲示{けいじ}する制度{せいど}',
          'A rule posting names of workers who suffered heat illness publicly',
        ),
        t(
          '体調不良{たいちょうふりょう}時{じ}の報告{ほうこく}体制{たいせい}の整備{せいび}や応急措置{おうきゅうそち}・医療{いりょう}機関{きかん}搬送{はんそう}手順{てじゅん}の確立{かくりつ}',
          'Establishing reporting systems for physical discomfort, along with emergency first-aid and medical transport procedures',
        ),
      ],
      // verify: 2025年6月1日施行の改正安衛則（報告体制整備および救護対応手順の策定義務）
      answer: 3,
      why: t(
        '熱中症{ねっちゅうしょう}の重症{じゅうしょう}化{か}を防{ふせ}ぐため、早期{そうき}の報告{ほうこく}体制{たいせい}整備{せいび}や緊急{きんきゅう}時{じ}の救護{きゅうご}・搬送{はんそう}手順{てじゅん}の確立{かくりつ}が義務化{ぎむか}されます。',
        'To prevent severe heat illness, employers are mandated to establish early reporting protocols and emergency medical response procedures.',
      ),
    },
  ],
  glossary: [
    {
      ja: '熱中症',
      reading: 'ねっちゅうしょう',
      en: 'Heat illness / Heatstroke',
      note: t(
        '高温多湿な環境で体温調節機能が破綻し、体内に熱がこもる病態。',
        'Condition where heat accumulates in the body due to temperature regulation failure in hot, humid environments.',
      ),
    },
    {
      ja: '暑さ指数',
      reading: 'あつさしすう',
      en: 'WBGT (Wet Bulb Globe Temperature)',
      note: t(
        '気温・湿度・日射などの熱環境を総合して熱中症リスクを評価する指標。',
        'Index evaluating heat illness risk by combining temperature, humidity, and radiant heat.',
      ),
    },
    {
      ja: '暑熱順化',
      reading: 'しょねつじゅんか',
      en: 'Heat acclimatization',
      note: t(
        '体が次第に暑さに慣れ、汗をかきやすく体温が上がりにくくなる状態。',
        'Process where the body gradually adapts to heat, sweating more easily to regulate temperature.',
      ),
    },
    {
      ja: '経口補水液',
      reading: 'けいこうほすいえき',
      en: 'Oral rehydration solution (ORS)',
      note: t(
        '水に塩分と糖分が一定の割合で溶け、体に速やかに吸収される飲料。',
        'Drink containing water, salt, and glucose in balanced proportions for rapid body absorption.',
      ),
    },
    {
      ja: '水分補給',
      reading: 'すいぶんほきゅう',
      en: 'Hydration / fluid intake',
      note: t(
        '汗などで体から失われた水分をこまめに体内に補うこと。',
        'Replenishing bodily fluids lost through perspiration regularly.',
      ),
    },
    {
      ja: '塩分補給',
      reading: 'えんぶんほきゅう',
      en: 'Salt / electrolyte replenishment',
      note: t(
        '発汗時に水分と同時に失われるナトリウムなどの塩分を補うこと。',
        'Replenishing sodium and minerals lost alongside water during heavy sweating.',
      ),
    },
    {
      ja: 'バディ確認',
      reading: 'ばでぃかくにん',
      en: 'Buddy check',
      note: t(
        '二人一組となり、互いの顔色や汗のかき方など体調の異変を確認し合う安全活動。',
        'Safety practice where pairs monitor each other for facial color, sweat, and physical changes.',
      ),
    },
    {
      ja: '意識障害',
      reading: 'いしきしょうがい',
      en: 'Impaired consciousness',
      note: t(
        '呼びかけに答えない、会話が噛み合わないなど意識が正常でない重症状態。',
        'Severe medical state where a person does not respond or speaks incoherently.',
      ),
    },
    {
      ja: '応急手当',
      reading: 'おうきゅうてあて',
      en: 'First aid',
      note: t(
        '病気やけがの発生直後に、医師の治療を受けるまでに行う救護処置。',
        'Immediate medical assistance provided before professional medical care is available.',
      ),
    },
    {
      ja: 'こむら返り',
      reading: 'こむらがえり',
      en: 'Muscle cramp / heat cramp',
      note: t(
        '大量の発汗と塩分不足によってふくらはぎなどの筋肉が急激に痙攣し痛むこと。',
        'Sudden painful spasm of calf or other muscles caused by heavy sweating and salt depletion.',
      ),
    },
    {
      ja: '冷却',
      reading: 'れいきゃく',
      en: 'Cooling',
      note: t(
        '首筋や脇の下など太い血管がある部位を冷やして体温を下げること。',
        'Lowering body temperature by applying cooling packs to large blood vessel areas.',
      ),
    },
    {
      ja: 'ファン付き作業着',
      reading: 'ふぁんつきさぎょうぎ',
      en: 'Fan-equipped cooling jacket',
      note: t(
        '小型ファンで外気を作業着内に取り込み、汗を気化させて涼しく保つ作業服。',
        'Work vest with miniature fans circulating air to evaporate sweat and keep workers cool.',
      ),
    },
  ],
}
