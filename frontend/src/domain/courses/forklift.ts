import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string): Tri => ({ ja, en })

export const forklift: CourseData = {
  id: 'forklift',
  category: 'trade',
  level: 2,
  minutes: 20,
  title: t(
    'フォークリフト運転{うんてん}安全{あんぜん}',
    'Forklift operation safety',
  ),
  sub: t(
    '正{ただ}しい資格{しかく}と始業前{しぎょうまえ}点検{てんけん}、走行時{そうこうじ}のルールを守{まも}り、人{ひと}との接触{せっしょく}と転倒{てんとう}を防{ふせ}ぐ。',
    'Prevent pedestrian collisions and tip-overs with proper certification, pre-shift inspections, and safe travel rules.',
  ),
  refs: [
    '労働安全衛生法 第61条（就業制限：フォークリフト運転業務）',
    '労働安全衛生規則 第36条第5号（特別教育：最大積載荷重1トン未満のフォークリフト）',
    '労働安全衛生規則 第151条（車両系荷役運搬機械等：フォークリフト）',
    '厚生労働省 フォークリフト作業安全ガイドライン',
  ],
  panels: [
    {
      who: 'foreman',
      text: t(
        '今日{きょう}は倉庫{そうこ}でパレット積{づ}みの荷物{にもつ}をフォークリフトで運搬{うんぱん}します。',
        'Today we will transport palletized cargo in the warehouse using a forklift.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '荷物{にもつ}を積{つ}みました！前{まえ}が見{み}えにくいので、フォークを高{たか}く上{あ}げて前進{ぜんしん}してみます！',
        'I loaded the cargo! Since forward visibility is poor, I will raise the forks high and drive forward!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '待{ま}ちなさい！荷{に}を高{たか}く上{あ}げたまま走行{そうこう}してはいけません。重心{じゅうしん}が高{たか}くなり、わずかな段差{だんさ}や旋回{せんかい}で簡単{かんたん}に転倒{てんとう}します！',
        'Wait! Never drive with a raised load. The high center of gravity makes it tip over easily on small bumps or turns!',
      ),
    },
    {
      who: 'worker',
      text: t(
        'すみません！荷物{にもつ}で前{まえ}が遮{さえぎ}られて見{み}えなかったのです。前{まえ}が見{み}えない時{とき}はどうしますか？',
        'I am sorry! The load was blocking my forward view. What should I do when I cannot see ahead?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '前{まえ}が見{み}えない時{とき}は「後進{こうしん}（バック）走行{そうこう}」が鉄則{てっそく}です！後方{こうほう}を目視{もくし}で確認{かくにん}して進{すす}むか、誘導者{ゆうどうしゃ}を配置{はいち}します。',
        'When your forward view is blocked, driving in reverse is the absolute rule! Check behind by direct sight, or assign a spotter.',
      ),
    },
    {
      who: 'worker',
      text: t(
        'なるほど！フォークを地上{ちじょう}20cmまで下{さ}げ、マストを後方{こうほう}に傾{かたむ}けました。これなら安定{あんてい}しますね！', // verify: 走行時のフォーク高さは地上20〜30cm（マスト後傾）
        'I see! I lowered the forks to 20 cm off the ground and tilted the mast back. This keeps it stable!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '交差点{こうさてん}や歩行者{ほこうしゃ}通路{つうろ}の手前{てまえ}では必{かなら}ず一時{いちじ}停止{ていし}し、警音器{けいおんき}を鳴{な}らして左右{さゆう}の安全{あんぜん}を確認{かくにん}してください。',
        'Always stop at intersections and before pedestrian walkways, sound the horn, and check left and right for safety.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '交差点{こうさてん}で一旦{いったん}停止{ていし}、クラクション鳴動{めいどう}、歩行者{ほこうしゃ}なし、左右{さゆう}よし！後退{こうたい}で安全{あんぜん}に通過{つうか}します！',
        'Temporary stop at intersection, sound horn, no pedestrians, left and right clear! Safely passing in reverse!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '完璧{かんぺき}です！駐車{ちゅうしゃ}する時{とき}はフォークを地面{じめん}に下{お}ろし、マストを前傾{ぜんけい}させ、キーを抜{ぬ}いてブレーキを引{ひ}くことを忘{わす}れずに。',
        'Perfect! When parking, remember to lower the forks to the ground, tilt mast forward, remove the key, and set the brake.',
      ),
    },
    {
      who: 'worker',
      text: t(
        'はい！始業前{しぎょうまえ}点検{てんけん}と安全{あんぜん}運転{うんてん}を徹底{てってい}し、無事故{むじこ}で作業{さぎょう}を続{つづ}けます！',
        'Yes! I will strictly follow pre-shift inspections and safe driving to continue working with zero accidents!',
      ),
    },
  ],
  handbook: [
    t(
      '最大{さいだい}積載{せきさい}荷重{かじゅう}1トン以上{いじょう}は「フォークリフト運転{うんてん}技能講習{ぎのうこうしゅう}」、1トン未満{みまん}は「特別教育{とくべつきょういく}」の修了証{しゅうりょうしょう}を携帯{けいたい}する。', // verify: 最大積載荷重1トン以上はフォークリフト運転技能講習、1トン未満はフォークリフト運転特別教育
      'Only those who completed the Skill Training (1t or more) or Special Education (under 1t) may drive, carrying their card at all times.',
    ),
    t(
      'フォークの爪{つめ}の上{うえ}に人{ひと}を乗{の}せたり、パレットの上{うえ}に立{た}たせて昇降{しょうこう}作業{さぎょう}をさせてはならない。',
      'Never allow people to ride on the forks or stand on pallets for elevated work.',
    ),
    t(
      '荷役{にやく}走行{そうこう}時{じ}はフォークを地上{ちじょう}20〜30cmに保{たも}ち、マストを後方{こうほう}に傾{かたむ}けて安定{あんてい}させる。', // verify: 走行時のフォーク高さは地上20〜30cm（マスト後傾）
      'When driving with a load, keep forks 20 to 30 cm off the ground and tilt the mast back for stability.',
    ),
    t(
      '荷物{にもつ}で前{まえ}の視界{しかい}が遮{さえぎ}られる時{とき}は、必{かなら}ず後進{こうしん}（バック）走行{そうこう}するか誘導者{ゆうどうしゃ}を配置{はいち}する。',
      'When forward visibility is blocked by cargo, always drive in reverse or assign a spotter.',
    ),
    t(
      'フォークを高{たか}く上{あ}げたまま走行{そうこう}したり、急旋回{きゅうせんかい}や急{きゅう}ブレーキを行{おこな}ってはならない。',
      'Never drive with the forks raised high, and avoid sudden sharp turns or abrupt stops.',
    ),
    t(
      '構内{こうない}では事業場{じぎょうじょう}で定{さだ}められた制限{せいげん}速度{そくど}（一般的{いっぱんてき}に時速{じそく}10km以下{いか}）を守{まも}り徐行{じょこう}する。', // verify: 構内走行速度は各事業場規程（一般に時速10km以下基準）
      'Strictly obey site speed limits (generally 10 km/h or less) and slow down in plant yards.',
    ),
    t(
      '歩行者{ほこうしゃ}通路{つうろ}との交差点{こうさてん}や見通{みとお}しの悪{わる}い角{かど}では、必{かなら}ず一時{いちじ}停止{ていし}して警音器{けいおんき}を鳴{な}らす。',
      'Always come to a full stop and sound the horn at pedestrian crossings and blind corners.',
    ),
    t(
      '荷物{にもつ}の偏荷重{へんかじゅう}や過積載{かせきさい}を避{さ}け、パレットの奥{おく}深{ふか}くまでフォークを差{さ}し込{こ}む。',
      'Avoid off-center loads and overloading, inserting the forks fully to the root of the pallet.',
    ),
    t(
      '作業{さぎょう}開始{かいし}前{まえ}にブレーキ、タイヤ、油圧{ゆあつ}装置{そうち}、警報{けいほう}装置{そうち}などの始業前{しぎょうまえ}点検{てんけん}を実施{じっし}する。',
      'Perform daily pre-shift inspections on brakes, tires, hydraulics, horn, and lights before starting work.',
    ),
    t(
      '運転席{うんてんせき}を離{はな}れる時{とき}は、フォークを接地{せっち}させ、ブレーキを引{ひ}き、キーを抜{ぬ}き取{と}る。',
      'When leaving the seat, lower forks to the ground, apply parking brake, stop engine, and remove key.',
    ),
  ],
  reel: [
    t(
      '周囲{しゅうい}確認{かくにん}、人{ひと}なし、ヨシ！',
      'Area clear, no pedestrians, yoshi!',
    ),
    t(
      'フォーク高{たか}さ、地上{ちじょう}20センチ、ヨシ！', // verify: 走行時のフォーク高さは地上20〜30cm（マスト後傾）
      'Fork height, 20 cm from ground, yoshi!',
    ),
    t(
      'マスト後傾{こうけい}、傾{かたむ}き、ヨシ！',
      'Mast tilt, backward tilt, yoshi!',
    ),
    t(
      '交差点{こうさてん}、一時{いちじ}停止{ていし}、左右{さゆう}、ヨシ！',
      'Intersection, temporary stop, left and right clear, yoshi!',
    ),
    t(
      '後進{こうしん}走行{そうこう}、後方{こうほう}確認{かくにん}、ヨシ！',
      'Reverse driving, rear view clear, yoshi!',
    ),
    t(
      '警音器{けいおんき}鳴動{めいどう}、クラクション、ヨシ！',
      'Horn sounding, signal given, yoshi!',
    ),
    t(
      '駐車{ちゅうしゃ}、爪{つめ}接地{せっち}、キー抜{ぬ}き、ヨシ！',
      'Parking, forks on ground, key removed, yoshi!',
    ),
  ],
  drills: [
    {
      id: 'forklift-spot',
      kind: 'spot',
      secs: 30,
      need: 4,
      name: t(
        'フォークリフト作業{さぎょう}の危険{きけん}予知{よち}',
        'Forklift operation hazard spotting',
      ),
      goal: t(
        '30秒{びょう}でフォークリフト作業{さぎょう}に潜{ひそ}む危険{きけん}な状態{じょうたい}を4つ見{み}つける。',
        'Find 4 hazardous forklift operation conditions within 30 seconds.',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '荷物{にもつ}を地上{ちじょう}1.5mまで高{たか}く上{あ}げたままで前進{ぜんしん}走行{そうこう}している',
            'Driving forward with forks raised high at 1.5m above ground',
          ),
        },
        {
          hazard: false,
          label: t(
            'フォークを地上{ちじょう}20cmに保{たも}ちマストを後方{こうほう}に倒{たお}して走行{そうこう}している', // verify: 走行時のフォーク高さは地上20〜30cm（マスト後傾）
            'Driving with forks at 20cm above ground and mast tilted backward',
          ),
        },
        {
          hazard: true,
          label: t(
            'フォークの爪{つめ}の上{うえ}に作業員{さぎょういん}を直接{ちょくせつ}乗{の}せて高所{こうしょ}作業{さぎょう}をしている',
            'Worker standing directly on the fork tines to do elevated work',
          ),
        },
        {
          hazard: false,
          label: t(
            '見通{みとお}しの悪{わる}い交差点{こうさてん}の手前{てまえ}で一時{いちじ}停止{ていし}し警音器{けいおんき}を鳴{な}らしている',
            'Stopping temporarily and sounding horn before a blind intersection',
          ),
        },
        {
          hazard: true,
          label: t(
            '大{おお}きな荷物{にもつ}で前{まえ}が見{み}えないのにそのまま前進{ぜんしん}運転{うんてん}している',
            'Driving forward despite the high cargo completely blocking the front view',
          ),
        },
        {
          hazard: false,
          label: t(
            '歩行者{ほこうしゃ}専用{せんよう}通路{つうろ}の白線{はくせん}の内側{うちがわ}を歩行者{ほこうしゃ}が歩{ある}いている',
            'Pedestrians walking inside the designated pedestrian walkway lines',
          ),
        },
        {
          hazard: true,
          label: t(
            '運転席{うんてんせき}を離{はな}れる際{さい}に爪{つめ}を浮{う}かせたままキーを差{さ}しっぱなしにしている',
            'Leaving the driver seat with forks raised and key left in the ignition',
          ),
        },
        {
          hazard: false,
          label: t(
            'パレットの差込口{さしこみぐち}の奥{おく}までフォークをしっかり差{さ}し込{こ}んでいる',
            'Inserting the forks fully into the pallet opening up to the root',
          ),
        },
        {
          hazard: true,
          label: t(
            'パレットの片側{かたがわ}だけに重{おも}い荷物{にもつ}を載{の}せて偏荷重{へんかじゅう}のまま走{はし}っている',
            'Driving with an unbalanced load shifted entirely to one side of pallet',
          ),
        },
        {
          hazard: false,
          label: t(
            'フォークリフト運転{うんてん}技能講習{ぎのうこうしゅう}の修了証{しゅうりょうしょう}を携帯{けいたい}している', // verify: 最大積載荷重1トン以上はフォークリフト運転技能講習
            'Carrying forklift operator skill training certificate card at all times',
          ),
        },
      ],
    },
    {
      id: 'forklift-parking-seq',
      kind: 'seq',
      name: t(
        '安全{あんぜん}駐車{ちゅうしゃ}と降車{こうしゃ}手順{てじゅん}',
        'Safe parking and shutdown sequence',
      ),
      goal: t(
        '作業{さぎょう}終了{しゅうりょう}時{じ}の安全{あんぜん}な駐車{ちゅうしゃ}手順{てじゅん}5ステップを正{ただ}しい順番{じゅんばん}に並{なら}べる。',
        'Arrange the 5 steps for safely parking and shutting down the forklift in order.',
      ),
      steps: [
        t(
          '1. 平坦{へいたん}で安全{あんぜん}な指定{してい}駐車{ちゅうしゃ}場所{ばしょ}にフォークリフトを完全{かんぜん}に停止{ていし}させる',
          '1. Stop the forklift completely at the designated flat, safe parking area',
        ),
        t(
          '2. 変速{へんそく}レバーを中立{ちゅうりつ}（ニュートラル）にし、駐車{ちゅうしゃ}ブレーキを確実{かくじつ}に引{ひ}く',
          '2. Shift the directional lever to neutral and securely apply the parking brake',
        ),
        t(
          '3. マストを前傾{ぜんけい}させ、フォークの爪{つめ}先端{せんたん}を地面{じめん}に完全{かんぜん}に接地{せっち}させる',
          '3. Tilt the mast forward and lower fork tips until completely resting on the floor',
        ),
        t(
          '4. エンジンスイッチをOFFにし、キースイッチからキーを抜{ぬ}き取{と}る',
          '4. Turn off the engine switch and remove the key from the ignition',
        ),
        t(
          '5. 車両{しゃりょう}から降{お}りて車輪{しゃりん}に輪留{わど}め（チョック）を正{ただ}しく設置{せっち}する',
          '5. Dismount safely and place wheel chocks firmly against the tires',
        ),
      ],
    },
    {
      id: 'forklift-estop',
      kind: 'reflex',
      name: t(
        '歩行者{ほこうしゃ}飛{と}び出{だ}しの急停止{きゅうていし}反射{はんしゃ}',
        'Emergency stop reflex for pedestrian hazard',
      ),
      goal: t(
        '見通{みとお}しの悪{わる}い角{かど}から人{ひと}が飛{と}び出{だ}した！0.4秒以内{びょういない}にブレーキを踏{ふ}んで急停止{きゅうていし}する。',
        'A worker suddenly stepped into your path! Hit the brake within 0.4 seconds to emergency stop.',
      ),
    },
  ],
  inspect: [
    {
      id: 'fork-inspect',
      defective: true,
      name: t(
        'フォーク（爪{つめ}）の始業前{しぎょうまえ}点検{てんけん}',
        'Fork tines pre-shift inspection',
      ),
      spots: [
        {
          x: 25,
          y: 35,
          check: t(
            '爪{つめ}の根元{ねもと}（直角{ちょっかく}曲{ま}がり部{ぶ}）：過荷重{かかじゅう}による亀裂{きれつ}・クラックがないか（良品{りょうひん}：亀裂{きれつ}なし / 不良{ふりょう}：付{つ}け根{ね}に明{あき}らかなヒビ割{わ}れあり）',
            'Fork heel: Any cracks from shock or overload (Good: Sound and crack-free / Defect: Visible crack at heel)',
          ),
        },
        {
          x: 65,
          y: 55,
          check: t(
            '位置{いち}固定{こてい}ロックピン：スプリングが健全{けんぜん}で爪{つめ}の横滑{よこすべ}りを防{ふせ}いでいるか（良品{りょうひん}：溝{みぞ}に完全{かんぜん}に噛{か}み合{あ}う / 不良{ふりょう}：ピンが折{お}れて固定{こてい}できない）',
            'Position lock pin: Spring holds tines securely (Good: Firmly engaged in notch / Defect: Broken pin, tines slide)',
          ),
        },
        {
          x: 80,
          y: 75,
          check: t(
            '爪{つめ}の底面{ていめん}厚{あつ}み摩耗{まもう}：摩耗{まもう}による厚{あつ}みの減少{げんしょう}が基準値{きじゅんち}の10%以内{いじょう}か（良品{りょうひん}：十分{じゅうぶん}な厚{あつ}み / 不良{ふりょう}：摩耗{まもう}大{だい}で10%以上{いじょう}薄{うす}い）', // verify: フォークの厚み摩耗限度は基準厚の10%減
            'Blade bottom thickness: Is wear within 10% limit (Good: Sufficient thickness / Defect: Thickness reduced over 10%)',
          ),
        },
      ],
    },
    {
      id: 'tire-inspect',
      defective: false,
      name: t(
        'タイヤ・車輪{しゃりん}およびハブナット点検{てんけん}',
        'Tires, wheels, and hub nuts inspection',
      ),
      spots: [
        {
          x: 35,
          y: 40,
          check: t(
            'タイヤの摩耗{まもう}と亀裂{きれつ}：トレッド面{めん}に深{ふか}い亀裂{きれつ}や欠{か}けがないか（良品{りょうひん}：摩耗{まもう}限度{げんど}内{ない}で亀裂{きれつ}なし / 不良{ふりょう}：大{おお}きな亀裂{きれつ}や偏摩耗{へんまもう}あり）',
            'Tire tread wear and cracks: Any deep cracks or chunking (Good: Within wear limit, intact / Defect: Deep cuts or chunking)',
          ),
        },
        {
          x: 55,
          y: 60,
          check: t(
            'ハブナットの緩{ゆる}み：ホイール固定{こてい}ナットの緩{ゆる}みや脱落{だつらく}がないか（良品{りょうひん}：全{すべ}て均一{きんいつ}に締結{ていけつ}・合{あ}いマーク一致{いっち} / 不良{ふりょう}：ナット緩{ゆる}み・欠落{けつらく}）',
            'Hub nut tightness: Any loose or missing wheel rim nuts (Good: All torqued with alignment marks / Defect: Loose or missing nuts)',
          ),
        },
        {
          x: 75,
          y: 45,
          check: t(
            '異物{いぶつ}の噛{か}み込{こ}み：タイヤ溝{みぞ}に金属片{きんぞくへん}やボルト・ガラスが刺{さ}さっていないか（良品{りょうひん}：異物{いぶつ}なし / 不良{ふりょう}：太{ふと}い釘{くぎ}が深{ふか}く刺{さ}さっている）',
            'Foreign object penetration: Any nails, bolts, or metal debris (Good: Clean tread / Defect: Large nail embedded deeply)',
          ),
        },
      ],
    },
    {
      id: 'hydraulic-inspect',
      defective: true,
      name: t(
        '油圧{ゆあつ}シリンダー・ホースの油漏{あぶらも}れ点検{てんけん}',
        'Hydraulic cylinder and hose leak inspection',
      ),
      spots: [
        {
          x: 30,
          y: 30,
          check: t(
            '油圧{ゆあつ}配管{はいかん}・継手{つぎて}の油漏{あぶらも}れ：ホース接続部{せつぞくぶ}から油{あぶら}がにじんでいないか（良品{りょうひん}：油漏{あぶらも}れなし / 不良{ふりょう}：継手{つぎて}から作動油{さどうゆ}が滴{したた}り落{お}ちている）',
            'Hydraulic hose fittings: Any fluid leakage at joints (Good: Completely dry and clean / Defect: Hydraulic oil actively dripping)',
          ),
        },
        {
          x: 50,
          y: 55,
          check: t(
            'リフトチェーンの張{は}り：左右{さゆう}のチェーンの張{は}りが均等{きんとう}で給油{きゅうゆ}されているか（良品{りょうひん}：左右{さゆう}均等{きんとう}で適正{てきせい}な張{は}り / 不良{ふりょう}：片側{かたがわ}が著{いちじる}しく弛緩{しかん}）',
            'Lift chain tension: Are both chains evenly tensioned and oiled (Good: Symmetrical tension / Defect: One side visibly sagging)',
          ),
        },
        {
          x: 70,
          y: 70,
          check: t(
            'シリンダーロッドの傷{きず}：ピストンロッド表面{ひょうめん}に縦{たて}傷{きず}やサビがないか（良品{りょうひん}：鏡面{きょうめん}で平滑{へいかつ} / 不良{ふりょう}：深{ふか}い筋{すじ}傷{きず}があり油{あぶら}が漏{も}れ出{で}ている）',
            'Cylinder rod surface: Any deep scratches or scoring (Good: Mirror-smooth rod / Defect: Deep score marks causing oil leaks)',
          ),
        },
      ],
    },
  ],
  quiz: [
    {
      q: t(
        '工場{こうじょう}で最大{さいだい}積載{せきさい}荷重{かじゅう}2.5トンのフォークリフトを運転{うんてん}して荷役{にやく}を行{おこな}うことになりました。労働安全衛生法上{ろうどうあんぜんえいせいほうじょう}、必要{ひつよう}な資格{しかく}はどれですか？',
        'You need to operate a forklift with 2.5-ton rated capacity in a factory. Which qualification is legally required?',
      ),
      options: [
        t(
          '最大{さいだい}積載{せきさい}荷重{かじゅう}1トン以上{いじょう}の「フォークリフト運転{うんてん}技能講習{ぎのうこうしゅう}」修了証{しゅうりょうしょう}', // verify: 最大積載荷重1トン以上はフォークリフト運転技能講習
          'Forklift operator skill training certificate (for capacities of 1 ton or more)',
        ),
        t(
          '最大{さいだい}積載{せきさい}荷重{かじゅう}1トン未満{みまん}の「フォークリフト特別教育{とくべつきょういく}」修了証{しゅうりょうしょう}',
          'Forklift special education certificate (for capacities under 1 ton)',
        ),
        t(
          '普通自動車{ふつうじどうしゃ}第{だい}一種{いっしゅ}運転免許{うんてんめんきょ}（フォークリフト講習{こうしゅう}は未受講{みじゅこう}）',
          'Standard automobile driver license (without forklift training)',
        ),
        t(
          '特別{とくべつ}な資格{しかく}は不要{ふよう}で、社内{しゃない}での30分間{ぷんかん}の練習{れんしゅう}のみ',
          'No license needed, only 30 minutes of in-house practice',
        ),
      ],
      answer: 0,
      why: t(
        '労働安全衛生法により、最大積載荷重1トン以上のフォークリフトの運転には「技能講習」の修了が義務付けられています。自動車免許や特別教育では運転できません。',
        'Under the Industrial Safety and Health Act, operating forklifts of 1 ton or more strictly requires the Skill Training course. Driver licenses or special education are not sufficient.',
      ),
    },
    {
      q: t(
        '荷物{にもつ}をフォークリフトに積{つ}んで構内{こうない}を走行{そうこう}する際{さい}、転倒{てんとう}を防{ふせ}ぐ安全{あんぜん}なフォークの高{たか}さはどれですか？',
        'When carrying a load on a forklift in the yard, what is the safe fork height to prevent tip-over accidents?',
      ),
      options: [
        t(
          '床{ゆか}から1.5メートル以上{いじょう}の高{たか}さに上{あ}げて走{はし}る',
          'Raise to 1.5 meters or more above the floor while driving',
        ),
        t(
          'フォークを地上{ちじょう}20〜30cmまで下{さ}げ、マストを後方{こうほう}に傾{かたむ}ける', // verify: 走行時のフォーク高さは地上20〜30cm（マスト後傾）
          'Lower forks to 20-30 cm above ground and tilt mast backward',
        ),
        t(
          'フォークの先端{せんたん}が地面{じめん}に擦{す}れる高{たか}さ（0cm）でマストを前傾{ぜんけい}させる',
          'Keep forks touching the ground (0 cm) with mast tilted forward',
        ),
        t(
          '積{つ}み荷{に}の重{おも}さに関係{かんけい}なく、マストを垂直{すいちょく}にしたまま最高{さいこう}位置{いち}まで上{あ}げる',
          'Raise to maximum height with mast vertical regardless of load',
        ),
      ],
      answer: 1,
      why: t(
        '走行時は車体の重心を低く保つため、フォークを地上20〜30cmまで下げ、マストを後方に倒して（後傾）走行するのが鉄則です。',
        'To keep the center of gravity low while traveling, lower forks to 20-30 cm above the ground and tilt the mast fully backward.',
      ),
    },
    {
      q: t(
        '背{せ}の高{たか}い荷物{にもつ}を積{つ}んだため、前方向{まえほうこう}の視界{しかい}が完全{かんぜん}に遮{さえぎ}られて見{み}えません。この時{とき}の正{ただ}しい走行{そうこう}方法{ほうほう}はどれですか？',
        'Tall cargo on the pallet completely blocks your forward visibility. What is the correct and safe way to drive?',
      ),
      options: [
        t(
          '前{まえ}が見{み}えるようになるまでフォークを高{たか}く上{あ}げて前進{ぜんしん}する',
          'Raise forks high until you can see underneath, then drive forward',
        ),
        t(
          '運転席{うんてんせき}から身{み}を大{おお}きく乗{の}り出{だ}して見{み}ながら高速{こうそく}で前進{ぜんしん}する',
          'Lean far out of the cabin and drive forward at high speed',
        ),
        t(
          '顔{かお}を後{うし}ろに向{む}けて直接{ちょくせつ}目視{もくし}で後方{こうほう}を確認{かくにん}しながら後進{こうしん}（バック）走行{そうこう}する',
          'Turn head to look directly behind and travel in reverse (backward)',
        ),
        t(
          '前{まえ}が見{み}えないままクラクションを連続{れんぞく}で鳴{な}らしながら前進{ぜんしん}する',
          'Drive forward blindly while continuously sounding the horn',
        ),
      ],
      answer: 2,
      why: t(
        '荷物で前方視界が塞がれた時は、フォークを上げてはいけません。後方を直接目視しながら後進走行するか、誘導者を配置して誘導を受けます。',
        'When forward sight is blocked, never raise the forks. Drive in reverse while directly looking backward, or use a designated spotter.',
      ),
    },
    {
      q: t(
        '高{たか}い棚{たな}のダンボールを取{と}るため、同僚{どうりょう}から「フォークのパレットに乗{の}せて上{うえ}まで持{も}ち上{あ}げてほしい」と頼{たの}まれました。どう対応{たいおう}すべきですか？',
        'A coworker asks you to lift them up on a pallet resting on the forks to reach boxes on a high shelf. How should you respond?',
      ),
      options: [
        t(
          '同僚{どうりょう}がパレットにしっかり掴{つか}まっていれば持{も}ち上{あ}げてよい',
          'Lift them as long as they hold firmly onto the pallet',
        ),
        t(
          'ヘルメットをかぶってゆっくり昇降{しょうこう}させれば法令上{ほうれいじょう}問題{もんだい}ない',
          'It is allowed by law if wearing a helmet and moving slowly',
        ),
        t(
          '短時間{たんじかん}の作業{さぎょう}で1人{ひとり}だけなら特別{とくべつ}に認{みと}められている',
          'Allowed as an exception for short tasks with only one person',
        ),
        t(
          '用途外{ようとがい}使用{しよう}および搭乗{とうじょう}制限{せいげん}違反{いはん}となるため、絶対{ぜったい}に断{ことわ}る',
          'Refuse immediately, as riding forks is illegal and strictly prohibited',
        ),
      ],
      answer: 3,
      why: t(
        '労働安全衛生規則により、フォークリフトを人を乗せて昇降させる用途に使うことは厳禁されています。高所作業台や脚立を使用しなければなりません。',
        'Under occupational safety regulations, using forklifts to lift personnel is strictly prohibited. Use dedicated aerial work platforms or rolling ladders.',
      ),
    },
    {
      q: t(
        'フォークリフトで走行中{そうこうちゅう}、見通{みとお}しの悪{わる}い交差点{こうさてん}や歩行者{ほこうしゃ}通路{つうろ}の手前{てまえ}に近{ちか}づきました。正{ただ}しい運転{うんてん}行動{こうどう}はどれですか？',
        'You approach a blind intersection or pedestrian walkway while driving a forklift. What is the correct driving procedure?',
      ),
      options: [
        t(
          '交差点{こうさてん}手前{てまえ}で必{かなら}ず一時{いちじ}停止{ていし}し、クラクションを鳴{な}らして左右{さゆう}の安全{あんぜん}を確認{かくにん}する',
          'Always stop before intersection, sound horn, and confirm left/right clearance',
        ),
        t(
          '人{ひと}がいないように見{み}えたので、減速{げんそく}せずにそのまま一気{いっき}に走{はし}り抜{ぬ}ける',
          'Speed straight through without slowing down since no one seems to be there',
        ),
        t(
          'クラクションを鳴{な}らし続{つづ}けながら、停止{ていし}せずにスピードを上{あ}げて通過{つうか}する',
          'Speed up through the crossing while continuously sounding the horn',
        ),
        t(
          'ブレーキを踏{ふ}まず、片手{かたて}運転{うんてん}で手{て}を振{ふ}って周囲{しゅうい}に合図{あいず}しながら進{すす}む',
          'Drive through with one hand on steering and waving with the other hand',
        ),
      ],
      answer: 0,
      why: t(
        '見通しの悪い交差点や歩行者通路の手前では、必ず一旦停止して警音器を鳴らし、左右の安全を指差呼称で確認してから徐行で進みます。',
        'Before blind corners or pedestrian zones, you must stop completely, sound the horn, and confirm clearance on both sides before crawling forward.',
      ),
    },
    {
      q: t(
        'フォークリフトの旋回{せんかい}（曲{ま}がり角{かど}）特性{とくせい}について、正{ただ}しい説明{せつめい}はどれですか？',
        'Which statement correctly describes the steering and turning characteristics of a forklift?',
      ),
      options: [
        t(
          '一般{いっぱん}乗用車{じょうようしゃ}と同{おな}じ前輪{ぜんりん}操舵{そうだ}なので、曲{ま}がる時{とき}に車体{しゃたい}後部{こうぶ}は一切{いっさい}外側{そとがわ}に振{ふ}れない',
          'Steers with front wheels like a car, so rear end never swings outward',
        ),
        t(
          '後輪{こうりん}で舵{かじ}を切{き}る後輪{こうりん}操舵{そうだ}のため、旋回{せんかい}時{じ}に車体{しゃたい}後部{こうぶ}が大{おお}きく外側{そとがわ}へ振{ふ}れ出{だ}す',
          'Steers with rear wheels, causing the rear counterweight to swing out widely',
        ),
        t(
          '四輪{よんりん}すべてが同{おな}じ角度{かくど}で曲{ま}がるため、急旋回{きゅうせんかい}しても横転{おうてん}する危険{きけん}はない',
          'All four wheels steer at the same angle, so it cannot tip over on sharp turns',
        ),
        t(
          '荷物{にもつ}を高{たか}く持{も}ち上{あ}げた状態{じょうたい}の方{ほう}が重心{じゅうしん}が安定{あんてい}して急旋回{きゅうせんかい}しやすい',
          'Sharp turns are more stable with the load raised high in the air',
        ),
      ],
      answer: 1,
      why: t(
        'フォークリフトは後輪操舵のため、旋回時にリヤ部が外側に大きく振れます。周囲の作業員や柱を巻き込まないよう十分な間隔が必要です。',
        'Forklifts steer using the rear wheels, causing the rear end to swing out widely. Always watch for nearby personnel and walls.',
      ),
    },
    {
      q: t(
        '工場{こうじょう}や倉庫{そうこ}の構内{こうない}をフォークリフトで走行{そうこう}する際{さい}の制限{せいげん}速度{そくど}のルールとして正{ただ}しいものはどれですか？',
        'What is the standard rule regarding speed limits when operating a forklift inside factory yards and warehouses?',
      ),
      options: [
        t(
          '広{ひろ}い構内{こうない}であれば、公道{こうどう}と同{おな}じ時速{じそく}40kmで走行{そうこう}してよい',
          'In wide yards, you may drive at 40 km/h just like public roads',
        ),
        t(
          '荷物{にもつ}を積{つ}んでいない空車{くうしゃ}時{じ}であれば、速度{そくど}無制限{むせいげん}で走{はし}ってよい',
          'When carrying no load, you may drive at unlimited speed',
        ),
        t(
          '事業場{じぎょうじょう}で定{さだ}められた制限{せいげん}速度{そくど}（一般{いっぱん}に時速{じそく}10km以下{いか}）を守{まも}り徐行{じょこう}する', // verify: 構内走行速度は各事業場規程（一般に時速10km以下基準）
          'Strictly follow the site speed limit (typically 10 km/h or less) and drive slowly',
        ),
        t(
          '作業{さぎょう}終了{しゅうりょう}前{まえ}で時間{じかん}がない時{とき}は、周囲{しゅうい}に声{こえ}をかけて全速力{ぜんそくりょく}で走{はし}ってよい',
          'When in a rush near shift end, you may drive at top speed while shouting',
        ),
      ],
      answer: 2,
      why: t(
        '構内では歩行者や他の車両と交差するため、各事業場で定められた制限速度（原則時速10km以下）を厳守し、常に安全に停止できる速度で走行します。',
        'Because yards have crossing pedestrians and equipment, obey the site limit (normally 10 km/h or less) and travel at speeds that allow instant stopping.',
      ),
    },
    {
      q: t(
        '休憩{きゅうけい}などでフォークリフトを駐車{ちゅうしゃ}して運転席{うんてんせき}を離{はな}れる際{さい}、正{ただ}しい安全{あんぜん}措置{そち}はどれですか？',
        'When parking a forklift and leaving the operator seat for a break, which safety procedure must you perform?',
      ),
      options: [
        t(
          'フォークを床上{ゆかうえ}50cmに浮{う}かせたまま、ギアを入{い}れてエンジンを止{と}める',
          'Leave forks 50 cm off ground in gear and turn off engine',
        ),
        t(
          '傾斜{けいしゃ}のある坂道{さかみち}に止{と}め、キーを差{さ}しっぱなしで立{た}ち去{さ}る',
          'Park on a slope and walk away leaving key in ignition',
        ),
        t(
          '通路{つうろ}の真{ま}ん中{なか}にフォークを上{あ}げたまま止{と}め、サイドブレーキを引{ひ}く',
          'Park in the center of walkway with forks raised high',
        ),
        t(
          'フォークを地面{じめん}に下{お}ろして接地{せっち}させ、駐車{ちゅうしゃ}ブレーキを引{ひ}き、キーを抜{ぬ}く',
          'Lower forks fully to the ground, set parking brake, stop engine, and remove key',
        ),
      ],
      answer: 3,
      why: t(
        '法令により、運転位置を離れる時はフォーク先端を地上に接地させ、駐車ブレーキを確実にかけ、キーを抜いて保管することが義務付けられています。',
        'Safety regulations require lowering forks completely to the floor, applying parking brake firmly, and removing the key whenever leaving the vehicle.',
      ),
    },
    {
      q: t(
        '労働安全衛生規則{ろうどうあんぜんえいせいきそく}で定{さだ}められている、フォークリフトの「作業{さぎょう}開始{かいし}前{まえ}点検{てんけん}」の必須{ひっす}項目{こうもく}はどれですか？',
        'Which of the following sets of inspection items is legally required before starting daily forklift operations?',
      ),
      options: [
        t(
          '制動{せいどう}装置{そうち}（ブレーキ）、操縦{そうじゅう}装置{そうち}、荷役{にやく}・油圧{ゆあつ}装置{そうち}、警報{けいほう}装置{そうち}（クラクション・前照灯{ぜんしょうとう}）の機能{きのう}',
          'Braking systems, steering controls, loading/hydraulic systems, and horn/headlights',
        ),
        t(
          '車体{しゃたい}の外装{がいそう}ワックスのツヤ具合{ぐあい}とシートの色{いろ}',
          'Exterior body wax gloss level and driver seat fabric color',
        ),
        t(
          '運転席{うんてんせき}のラジオ音量{おんりょう}とエアコンの冷{ひ}え具合{ぐあい}',
          'Cabin radio volume and air conditioner cooling strength',
        ),
        t(
          '荷台{にだい}のステッカーの貼{は}り方{かた}のみを目視{もくし}で確認{かくにん}する',
          'Only visually checking if exterior safety stickers are neat',
        ),
      ],
      answer: 0,
      why: t(
        '労働安全衛生規則第151条の27により、ブレーキ、操縦装置、荷役油圧装置、前照灯・クラクションの機能点検が始業前の必須義務です。',
        'Under Article 151-27 of the regulations, inspecting brakes, steering, hydraulics, mast/forks, and horn/lights is mandatory before daily operation.',
      ),
    },
    {
      q: t(
        'パレットの片側{かたがわ}だけに重{おも}い荷物{にもつ}が載{の}っている「偏荷重{へんかじゅう}」の状態で走行{そうこう}すると、どのような危険{きけん}がありますか？',
        'What dangerous hazard occurs if you travel with an off-center load placed on only one side of the pallet?',
      ),
      options: [
        t(
          '車体{しゃたい}の片側{かたがわ}のタイヤが浮{う}き上{あ}がって摩擦{まさつ}が減{へ}り、燃費{ねんぴ}が向上{こうじょう}する',
          'Tires on one side lift reducing road friction, improving fuel economy',
        ),
        t(
          '旋回{せんかい}やわずかな段差{だんさ}でバランスを崩{くず}し、車体{しゃたい}が横転{おうてん}したり荷崩{にくず}れを起{お}こす',
          'The forklift easily loses balance on turns or bumps, causing rollover or cargo collapse',
        ),
        t(
          'ブレーキの効{き}きが普段{ふだん}よりも格段{かくだん}に良{よ}くなり、急停止{きゅうていし}しやすくなる',
          'Braking performance becomes much better than usual for quick stops',
        ),
        t(
          'フォークの片方{かたほう}が長持{ながも}ちするため、点検{てんけん}の手間{てま}が省{はぶ}ける',
          'One tine lasts longer, saving time on maintenance inspections',
        ),
      ],
      answer: 1,
      why: t(
        '荷物の重心が偏っていると、旋回時の遠心力や路面の段差で簡単に横転します。荷物はパレット中央に均等に積み、フォークを根元まで差し込みます。',
        'An eccentric load severely shifts center of gravity, causing catastrophic rollovers on turns. Always center cargo and fully engage forks.',
      ),
    },
    {
      q: t(
        '荷物{にもつ}を載{の}せたフォークリフトで坂道{さかみち}（勾配{こうばい}）を走行{そうこう}する際{さい}の正{ただ}しいルールはどれですか？',
        'What is the correct rule when driving a loaded forklift on a slope or ramp?',
      ),
      options: [
        t(
          '上{のぼ}り坂{ざか}も下{くだ}り坂{ざか}も、荷物{にもつ}の向{む}きに関係{かんけい}なく全{すべ}て前進{ぜんしん}で一気{いっき}に走{はし}り抜{ぬ}ける',
          'Drive forward at high speed on both uphill and downhill slopes',
        ),
        t(
          '坂道{さかみち}の途中{とちゅう}で車体{しゃたい}を真横{まよこ}に向{む}けて旋回{せんかい}し、小回{こまわ}りで方向転換{ほうこうてんかん}する',
          'Turn the vehicle sideways across the middle of the slope to turn around',
        ),
        t(
          '荷物{にもつ}を常{つね}に坂上{さかうえ}側{がわ}に向{む}け、上{のぼ}りは前進{ぜんしん}、下{くだ}りは後進{こうしん}（バック）で徐行{じょこう}する',
          'Keep load facing uphill at all times: drive forward uphill, and drive in reverse downhill',
        ),
        t(
          '下{くだ}り坂{ざか}ではギアをニュートラルにして惰性{だせい}でスピードをつけて下{お}りる',
          'Shift into neutral on downhill slopes and coast down using momentum',
        ),
      ],
      answer: 2,
      why: t(
        '荷物を積んで坂道を走る時は、荷物の転落を防ぐため常に荷物を坂上側に向けます（上り前進、下り後進）。坂道での方向転換や横切りは転倒するため厳禁です。',
        'When loaded on a ramp, always keep the load facing uphill (forward uphill, reverse downhill) to prevent spills. Never turn across a slope.',
      ),
    },
    {
      q: t(
        'パレットに載{の}った荷物{にもつ}をすくい上{あ}げる際{さい}、フォークの差{さ}し込{こ}み方{かた}の基本{きほん}動作{どうさ}として正{ただ}しいものはどれですか？',
        'When picking up palletized cargo, what is the correct procedure for inserting the fork tines?',
      ),
      options: [
        t(
          'スピードを落{お}とさず勢{いきお}いよくパレットに突{つ}っ込{こ}んで一度{いちど}に差{さ}し込{こ}む',
          'Ram into the pallet without slowing down to slide forks in at once',
        ),
        t(
          'パレットの差込口{さしこみぐち}の先端{せんたん}20cmだけに爪{つめ}を浅{あさ}く掛{か}けて持{も}ち上{あ}げる',
          'Insert only 20 cm of the fork tips into the pallet to lift',
        ),
        t(
          '車体{しゃたい}をパレットに対{たい}して大{おお}きく斜{なな}めにしたまま片方{かたほう}の爪{つめ}から差{さ}し込{こ}む',
          'Approach at a steep angle and insert one tine first into the opening',
        ),
        t(
          'パレットに車体{しゃたい}を正対{せいたい}させ、爪{つめ}をパレットの奥{おく}（根元{ねもと}）まで水平{すいへい}に完全{かんぜん}に差{さ}し込{こ}む',
          'Square up directly to the pallet and insert tines horizontally all the way to the root',
        ),
      ],
      answer: 3,
      why: t(
        '荷役時は車体をパレットに正対させ、フォークを奥深く根元まで差し込みます。浅掛けは荷の落下やフォークの破損を招くため極めて危険です。',
        'Align squarely with the pallet and insert forks completely to the heel. Partial insertion risks load drops and fork damage.',
      ),
    },
  ],
  glossary: [
    {
      ja: 'フォークリフト',
      reading: 'ふぉーくりふと',
      en: 'forklift / lift truck',
      note: t(
        '荷物{にもつ}を載{の}せるフォーク（爪{つめ}）と昇降{しょうこう}用{よう}マストを備{そな}えた荷役{にやく}運搬{うんぱん}車両{しゃりょう}。',
        'Cargo handling vehicle equipped with forks and a mast for lifting and moving.',
      ),
    },
    {
      ja: '技能講習',
      reading: 'ぎのうこうしゅう',
      en: 'skill training course (1 ton or more)',
      note: t(
        '最大{さいだい}積載{せきさい}荷重{かじゅう}1トン以上{いじょう}のフォークリフト運転{うんてん}に必須{ひっす}の国家{こっか}資格{しかく}講習{こうしゅう}。', // verify: 最大積載荷重1トン以上はフォークリフト運転技能講習
        'Mandatory national certification course to drive forklifts of 1 ton capacity or more.',
      ),
    },
    {
      ja: '特別教育',
      reading: 'とくべつきょういく',
      en: 'special education (under 1 ton)',
      note: t(
        '最大{さいだい}積載{せきさい}荷重{かじゅう}1トン未満{みまん}のフォークリフト運転{うんてん}のために事業主{じぎょうぬし}が行{おこな}う安全{あんぜん}教育{きょういく}。', // verify: 最大積載荷重1トン未満はフォークリフト運転特別教育
        'Safety training conducted by employers for driving forklifts under 1 ton capacity.',
      ),
    },
    {
      ja: 'マスト',
      reading: 'ますと',
      en: 'mast',
      note: t(
        'フォークを上下{じょうげ}に昇降{しょうこう}させたり前後{ぜんご}に傾{かたむ}けたりするための支柱{しちゅう}枠{わく}。',
        'Upright frame that raises, lowers, and tilts the forklift tines.',
      ),
    },
    {
      ja: 'チルト',
      reading: 'ちると',
      en: 'tilt (mast tilt forward / back)',
      note: t(
        '荷物{にもつ}の落下{らっか}を防{ふせ}ぐため、マストを前傾{ぜんけい}または後傾{こうけい}させる動作{どうさ}。',
        'Motion of tilting the mast forward or backward to secure loads.',
      ),
    },
    {
      ja: '最大積載荷重',
      reading: 'さいだいせきさいかじゅう',
      en: 'maximum rated load capacity',
      note: t(
        'フォークリフトに安全{あんぜん}に積載{せきさい}できる限界{げんかい}の重量{じゅうりょう}（トンまたはキログラム）。',
        'Maximum limit weight that can be safely loaded on the forklift.',
      ),
    },
    {
      ja: '偏荷重',
      reading: 'へんかじゅう',
      en: 'unbalanced / eccentric load',
      note: t(
        '荷物{にもつ}の重心{じゅうしん}がパレットやフォークの中央{ちゅうおう}から片側{かたがわ}に偏{かたよ}っている危険{きけん}な状態{じょうたい}。',
        'Hazardous condition where the load center of gravity is off-center on the forks.',
      ),
    },
    {
      ja: '後輪操舵',
      reading: 'こうりんそうだ',
      en: 'rear-wheel steering',
      note: t(
        '後輪{こうりん}で曲{ま}がるフォークリフトの構造{こうぞう}。旋回{せんかい}時{じ}に車体{しゃたい}後部{こうぶ}が大{おお}きく振{ふ}れる。',
        'Steering mechanism using the rear wheels, causing the rear end to swing wide.',
      ),
    },
    {
      ja: '始業前点検',
      reading: 'しぎょうまえてんけん',
      en: 'pre-shift inspection',
      note: t(
        '作業{さぎょう}開始{かいし}前{まえ}にブレーキ、油圧{ゆあつ}、タイヤ、警音器{けいおんき}などを毎日{まいにち}点検{てんけん}すること。',
        'Daily inspection of brakes, hydraulics, tires, horn, etc., before starting operations.',
      ),
    },
    {
      ja: '後進走行',
      reading: 'こうしんそうこう',
      en: 'reverse driving (backward travel)',
      note: t(
        '積{つ}み荷{に}で前{まえ}が見{み}えない時{とき}に、後方{こうほう}を目視{もくし}で確認{かくにん}しながらバックで走{はし}ること。',
        'Driving in reverse while directly looking behind when forward visibility is blocked.',
      ),
    },
    {
      ja: '誘導者',
      reading: 'ゆうどうしゃ',
      en: 'spotter / signal person',
      note: t(
        '見通{みとお}しの悪{わる}い場所{ばしょ}や狭{せま}い通路{つうろ}で、安全{あんぜん}な進行{しんこう}を合図{あいず}で指示{しじ}する人{ひと}。',
        'Person who directs safe vehicle movement with hand signals in narrow or blind areas.',
      ),
    },
    {
      ja: '輪留め',
      reading: 'わどめ',
      en: 'wheel chocks',
      note: t(
        '駐車{ちゅうしゃ}中{ちゅう}の車両{しゃりょう}が不意{ふい}に動{うご}き出{だ}さないよう車輪{しゃりん}に噛{か}ませるストッパー。',
        'Stoppers placed against tires to prevent unintentional vehicle rolling while parked.',
      ),
    },
  ],
}
