import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string): Tri => ({ ja, en })

export const welding: CourseData = {
  id: 'welding',
  category: 'trade',
  level: 2,
  minutes: 20,
  title: t('アーク溶接{ようせつ}の安全{あんぜん}', 'Arc welding safety'),
  sub: t(
    '感電{かんでん}・火災{かさい}・有害光線{ゆうがいこうせん}・溶接{ようせつ}ヒュームの危険{きけん}を理解{りかい}し、正{ただ}しい保護具{ほごぐ}と手順{てじゅん}で命{いのち}を守{まも}る。',
    'Understand hazards of electric shock, fire, arc radiation, and fumes, protecting life with proper PPE and procedures.',
  ),
  refs: [
    '労働安全衛生法 第59条第3項（特別教育）',
    '労働安全衛生規則 第36条第3号（アーク溶接等の業務にかかる特別教育）',
    '労働安全衛生規則 第332条（交流アーク溶接機用自動電撃防止装置）',
    '特定化学物質障害予防規則（溶接ヒュームに関する健康障害防止対策）',
  ],
  panels: [
    {
      who: 'foreman',
      text: t(
        '今日{きょう}は造船所{ぞうせんじょ}の船内{せんない}ブロックで鋼板{こうはん}のアーク溶接{ようせつ}を行{おこな}います。',
        'Today we are doing arc welding on steel plates in the shipyard compartment.',
      ),
    },
    {
      who: 'worker',
      text: t(
        'はい！溶接棒{ようせつぼう}とホルダーを準備{じゅんび}して、すぐ作業{さぎょう}に入{はい}ります！',
        'Yes! I will prepare the welding rod and holder, and start working right away!',
      ),
    },
    {
      who: 'worker',
      text: t(
        '手{て}に汗{あせ}をかきやすいので、涼{すず}しくなるよう皮手袋{かわてぶくろ}を水{みず}で濡{ぬ}らしてきました！',
        'My hands sweat easily, so I soaked my leather gloves in water to stay cool!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'ストップ！濡{ぬ}れた皮手袋{かわてぶくろ}は電気抵抗{でんきていこう}が下{さ}がり、命{いのち}に関{かか}わる感電{かんでん}を引起{ひきお}こします！すぐ乾{かわ}いた手袋{てぶくろ}に替{か}えなさい。',
        'Stop! Wet leather gloves drastically drop electrical resistance and cause fatal electric shocks! Switch to dry gloves immediately.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '危{あぶ}ないところでした！周{まわ}りにダンボールや木製{もくせい}パレットがありますが、このままでよいですか？',
        'That was dangerous! There are cardboard boxes and wooden pallets nearby—is it okay to leave them?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '火花{ひばな}は遠{とお}くまで飛{と}びます。可燃物{かねんぶつ}を取{と}り除{のぞ}くか防炎{ぼうえん}シートで覆{おお}い、消火器{しょうかき}を手元{てもと}に置{お}きなさい。',
        'Sparks fly far. Remove combustibles or cover them with a fireproof sheet, and keep a fire extinguisher close at hand.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '了解{りょうかい}です！この狭{せま}い区画{くかく}は煙{けむり}がこもりやすいですが、どうすればよいですか？',
        'Understood! Smoke builds up easily in this confined space—what should I do?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '溶接{ようせつ}ヒュームは肺{はい}の病気{びょうき}の原因{げんいん}になります。型式{かたしき}検定{けんてい}合格{ごうかく}の防{ぼう}じんマスクを着{つ}け、排気{はいき}ファンを回{まわ}してください。',
        'Welding fumes cause lung disease. Wear an approved dust mask and turn on the exhaust fan.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '乾{かわ}いた手袋{てぶくろ}、防火{ぼうか}養生{ようじょう}、換気{かんき}ですね！アークを出{だ}す前{まえ}に指差呼称{しさこしょう}を行{おこな}います！',
        'Dry gloves, fire protection, and ventilation! I will do pointing and calling before striking an arc!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'よろしい！自動電撃防止装置{じどうでんげきぼうしそうち}（電防{でんぼう}）の作動{さどう}も確認{かくにん}して、ゼロ災害{さいがい}で作業{さぎょう}を始{はじ}めましょう！',
        'Good! Verify that the shock preventer is functioning too, and let us start work with zero accidents!',
      ),
    },
  ],
  handbook: [
    t(
      'アーク溶接{ようせつ}の業務{ぎょうむ}には、法令{ほうれい}に基{づ}く「特別教育{とくべつきょういく}」を修了{しゅうりょう}した資格者{しかくしゃ}のみが就{つ}くこと。',
      'Only persons who have completed the statutory Special Education may perform arc welding.',
    ),
    t(
      '感電{かんでん}を防止{ぼうし}するため、水{みず}や汗{あせ}で濡{ぬ}れた皮手袋{かわてぶくろ}や破損{はそん}した作業服{さぎょうふく}で溶接{ようせつ}を行{おこな}ってはならない。',
      'Never weld with leather gloves wet with water or sweat, or torn work clothes, to prevent electric shock.',
    ),
    t(
      '溶接機{ようせつき}の自動電撃防止装置{じどうでんげきぼうしそうち}（電防{でんぼう}）が正常{せいじょう}に作動{さどう}していることを始業前{しぎょうまえ}に必{かなら}ず確認{かくにん}する。', // verify: 電防によりアーク停止時の無負荷電圧を25V以下に低下
      'Always confirm that the automatic electric shock prevention device operates normally before starting work.',
    ),
    t(
      'スパッタや火花{ひばな}による火災{かさい}を防{ふせ}ぐため、周囲{しゅうい}の可燃物{かねんぶつ}を取{と}り除{のぞ}き、防炎{ぼうえん}シートで養生{ようじょう}する。', // verify: 溶接作業時の可燃物離隔距離は原則5m以上または防炎シート養生
      'Remove combustibles around the work area and cover with fireproof sheets to prevent fires from sparks.',
    ),
    t(
      '作業場所{さぎょうばしょ}のすぐ手元{てもと}に、用途{ようと}に適{てき}した消火器{しょうかき}（ABC粉末{ふんまつ}消火器{しょうかき}等{など}）を常備{じょうび}する。',
      'Always keep an appropriate fire extinguisher (such as ABC powder type) immediately accessible at the work area.',
    ),
    t(
      '強烈{きょうれつ}なアーク光{こう}（紫外線{しがいせん}）から目{め}や皮膚{ひふ}を守{まも}るため、適切{てきせつ}な遮光度{しゃこうど}の遮光面{しゃこうめん}と保護眼鏡{ほごめがね}を着用{ちゃくよう}する。', // verify: アーク溶接の遮光度番号は溶接電流に応じて#10から#12程度を使用
      'Wear a welding helmet with proper shade level and protective glasses to shield eyes and skin from intense UV arc light.',
    ),
    t(
      '有害{ゆうがい}な溶接{ようせつ}ヒュームを吸{す}い込{こ}まないよう、型式{かたしき}検定{けんてい}合格{ごうかく}の防{ぼう}じんマスクを密着{みっちゃく}着用{ちゃくよう}し、局所排気装置{きょくしょはいきそうち}を稼働{かどう}させる。',
      'Tightly fit an approved dust respirator and operate local exhaust ventilation to avoid inhaling harmful welding fumes.',
    ),
    t(
      '母材{ぼざい}アース線{せん}（接地線{せっちせん}）は、作業箇所{さぎょうかしょ}の母材{ぼざい}に直接{ちょくせつ}・確実{かくじつ}にクランプして接続{せつぞく}する。',
      'Firmly clamp the ground cable directly to the workpiece base metal near the welding point.',
    ),
    t(
      '船内{せんない}やタンクなど狭隘{きょうあい}・密閉{みっぺい}場所{ばしょ}では、作業前{さぎょうまえ}に酸素濃度{さんそのうど}（18%以上{いじょう}）を確認{かくにん}し、常時{じょうじ}換気{かんき}を行{おこな}う。', // verify: 酸素欠乏危険場所での酸素濃度基準は18%以上
      'In confined spaces like ship compartments, verify oxygen levels are 18% or higher before entry and ventilate continuously.',
    ),
    t(
      '作業終了時{さぎょうしゅうりょうじ}は溶接機{ようせつき}の主電源{しゅでんげん}を切{き}り、最低{さいてい}30分間{ぷんかん}は周囲{しゅうい}の残火{ざんか}・火気{かき}の有無{うむ}を見張{みは}る。', // verify: 溶接作業終了後の火気見張り時間は原則30分以上
      'Turn off main power when finishing work, and watch the area for residual fire or embers for at least 30 minutes.',
    ),
  ],
  reel: [
    t(
      '乾{かわ}いた皮手袋{かわてぶくろ}・保護具{ほごぐ}着用{ちゃくよう}、ヨシ！',
      'Dry leather gloves and PPE on, yoshi!',
    ),
    t(
      '溶接機{ようせつき}・ケーブル被覆{ひふく}、傷{きず}なし、ヨシ！',
      'Welder and cable insulation undamaged, yoshi!',
    ),
    t(
      '自動電撃防止装置{じどうでんげきぼうしそうち}、作動{さどう}、ヨシ！',
      'Automatic shock preventer operating, yoshi!',
    ),
    t(
      '母材{ぼざい}アース、確実{かくじつ}にクランプ、ヨシ！',
      'Base metal earth securely clamped, yoshi!',
    ),
    t(
      '周囲{しゅうい}の可燃物{かねんぶつ}除去{じょきょ}・防炎{ぼうえん}シート、ヨシ！',
      'Combustibles removed, fireproof sheet ready, yoshi!',
    ),
    t(
      '手元{てもと}の消火器{しょうかき}、配置{はいち}、ヨシ！',
      'Fire extinguisher in place at hand, yoshi!',
    ),
    t(
      '排気{はいき}ファン・換気{かんき}、運転{うんてん}、ヨシ！',
      'Exhaust fan and ventilation running, yoshi!',
    ),
    t(
      '作業後{さぎょうご}の電源{でんげん}OFF・残火{ざんか}なし、ヨシ！',
      'Post-work power OFF, no residual embers, yoshi!',
    ),
  ],
  drills: [
    {
      id: 'welding-spot-kyt',
      kind: 'spot',
      secs: 30,
      need: 5,
      name: t(
        'アーク溶接{ようせつ}現場{げんば}の危険{きけん}予知{よち}',
        'Arc welding hazard spotting (KYT)',
      ),
      goal: t(
        '30秒{びょう}で溶接{ようせつ}作業{さぎょう}に潜{ひそ}む危険{きけん}な状態{じょうたい}を5つ見{み}つける。',
        'Find 5 hazardous conditions in the welding scene within 30 seconds.',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '水{みず}や汗{あせ}でびしょ濡{ぬ}れになった皮手袋{かわてぶくろ}でホルダーを握{にぎ}っている',
            'Holding the welding holder with leather gloves soaked in water or sweat',
          ),
        },
        {
          hazard: false,
          label: t(
            '母材{ぼざい}の表面{ひょうめん}にアースクランプが直接{ちょくせつ}しっかりと固定{こてい}されている',
            'Earth clamp is firmly fastened directly to the base metal surface',
          ),
        },
        {
          hazard: true,
          label: t(
            '火花{ひばな}が飛{と}び散{ち}る足元{あしもと}に段{だん}ボールや油{あぶら}のウエスが放置{ほうち}されている',
            'Cardboard boxes and oily rags left beneath where sparks scatter',
          ),
        },
        {
          hazard: false,
          label: t(
            '作業場所{さぎょうばしょ}からすぐ手{て}の届{とど}く位置{いち}に消火器{しょうかき}が配置{はいち}されている',
            'Fire extinguisher is placed within immediate reach of work area',
          ),
        },
        {
          hazard: true,
          label: t(
            '溶接{ようせつ}ケーブルの被覆{ひふく}が擦{す}り切{き}れて銅線{どうせん}がむき出{だ}しになっている',
            'Welding cable outer insulation is worn through exposing bare copper wire',
          ),
        },
        {
          hazard: false,
          label: t(
            '移動{いどう}できない可燃物{かねんぶつ}の上{うえ}に防炎{ぼうえん}シートが隙間{すきま}なく掛{か}けられている',
            'Fireproof sheet is tightly draped over immovable combustible materials',
          ),
        },
        {
          hazard: true,
          label: t(
            '換気{かんき}のない密閉{みっぺい}区画{くかく}で防{ぼう}じんマスクを着用{ちゃくよう}せずに溶接{ようせつ}している',
            'Welding in a confined space without ventilation and without a dust mask',
          ),
        },
        {
          hazard: false,
          label: t(
            '溶接{ようせつ}電流{でんりゅう}に合{あ}った適正{てきせい}な遮光度{しゃこうど}の遮光面{しゃこうめん}を使用{しよう}している',
            'Using a welding helmet with shade degree suited to the welding current',
          ),
        },
        {
          hazard: true,
          label: t(
            'アルゴンガスボンベがチェーン固定{こてい}されず単体{たんたい}で自立{じりつ}している',
            'Argon gas cylinder is standing freestanding without chain securing',
          ),
        },
        {
          hazard: false,
          label: t(
            '自動電撃防止装置{じどうでんげきぼうしそうち}の表示灯{ひょうじとう}が点灯{てんとう}し正常{せいじょう}に機能{きのう}している',
            'Automatic shock preventer indicator lamp is lit and working normally',
          ),
        },
      ],
    },
    {
      id: 'welding-startup-seq',
      kind: 'seq',
      name: t(
        'アーク溶接{ようせつ}の安全{あんぜん}開始{かいし}手順{てじゅん}',
        'Safe arc welding startup sequence',
      ),
      goal: t(
        '作業前{さぎょうまえ}の点検{てんけん}からアーク発生{はっせい}までの正{ただ}しい順序{じゅんじょ}を覚{おぼ}える。',
        'Master the correct order from pre-work check to striking an arc.',
      ),
      steps: [
        t(
          '乾{かわ}いた皮手袋{かわてぶくろ}、遮光面{しゃこうめん}、防{ぼう}じんマスクなど指定{してい}の保護具{ほごぐ}を完全{かんぜん}に着用{ちゃくよう}する',
          'Put on designated PPE completely: dry leather gloves, welding helmet, and dust mask',
        ),
        t(
          '作業{さぎょう}エリア周辺{しゅうへん}の可燃物{かねんぶつ}を取{と}り除{のぞ}き、防炎{ぼうえん}シートを敷{し}いて消火器{しょうかき}を配置{はいち}する',
          'Remove combustibles around work area, lay down fireproof sheets, and set fire extinguisher',
        ),
        t(
          'ケーブルの被覆{ひふく}損傷{そんしょう}がないか点検{てんけん}し、母材{ぼざい}にアースクランプを直接{ちょくせつ}・確実{かくじつ}に接続{せつぞく}する',
          'Inspect cable insulation for damage and clamp earth ground directly and securely to base metal',
        ),
        t(
          '溶接機{ようせつき}の主電源{しゅでんげん}を入{い}れ、自動電撃防止装置{じどうでんげきぼうしそうち}が正常{せいじょう}に作動{さどう}するか確認{かくにん}する',
          'Turn on welder main power and verify the automatic shock preventer functions properly',
        ),
        t(
          '局所排気装置{きょくしょはいきそうち}を起動{きどう}し、周囲{しゅうい}に合図{あいず}を行{おこな}ってからアークを発生{はっせい}させる',
          'Start local exhaust ventilation, give warning signal to surroundings, and strike arc',
        ),
      ],
    },
    {
      id: 'welding-shutdown-seq',
      kind: 'seq',
      name: t(
        '作業{さぎょう}終了時{しゅうりょうじ}の火気{かき}・安全{あんぜん}後片付{あとかたづ}け手順{てじゅん}',
        'Work shutdown and fire safety cleanup sequence',
      ),
      goal: t(
        '溶接{ようせつ}終了後{しゅうりょうご}の消火{しょうか}・電源{でんげん}遮断{しゃだん}と残火{ざんか}見張{みは}りの手順{てじゅん}を身{み}につける。',
        'Learn the steps for post-welding shutdown, power cutoff, and ember fire watch.',
      ),
      steps: [
        t(
          '溶接棒{ようせつぼう}をホルダーから取{と}り外{はず}し、溶接機{ようせつき}の主電源{しゅでんげん}スイッチをOFFにする',
          'Remove welding rod from holder and turn OFF the welder main power switch',
        ),
        t(
          'シールドガスを使用{しよう}している場合{ばあい}はボンベの元栓{もとせん}を完全{かんぜん}に閉{し}めて圧力{あつりょく}を抜{ぬ}く',
          'If using shield gas, fully close cylinder valve and release residual pressure',
        ),
        t(
          '電源{でんげん}コード、アース線{せん}、ホース類{るい}を整然{せいぜん}と巻{ま}き取{と}り所定{しょてい}の場所{ばしょ}に片付{かたづ}ける',
          'Neatly coil power cords, earth lines, and hoses, storing them in designated place',
        ),
        t(
          '火花{ひばな}が飛散{ひさん}した範囲{はんい}を最低{さいてい}30分間{ぷんかん}見張{みは}り、燻{くすぶ}りや残火{ざんか}がないことを指差呼称{しさこしょう}で確認{かくにん}する', // verify: 溶接作業終了後の火気見張り時間は原則30分以上
          'Watch the spark scatter area for at least 30 minutes, confirming no smoldering embers by pointing and calling',
        ),
      ],
    },
  ],
  inspect: [
    {
      id: 'welding-holder-inspect',
      defective: true,
      name: t(
        '溶接棒{ようせつぼう}ホルダー・ケーブルの点検{てんけん}',
        'Welding rod holder and cable inspection',
      ),
      spots: [
        {
          x: 25,
          y: 30,
          check: t(
            'ホルダー絶縁{ぜつえん}カバーの破損{はそん}：割{わ}れや欠{か}けがなく充電部{じゅうでんぶ}が露出{ろしゅつ}していないか（良品{りょうひん}：完全{かんぜん}に被覆{ひふく} / 不良{ふりょう}：樹脂{じゅし}が割{わ}れて内部{ないぶ}金属{きんぞく}が露出{ろしゅつ}）',
            'Holder insulation cover damage: No cracks or chips exposing live metal parts (Good: Fully insulated / Defective: Cracked exposing bare metal)',
          ),
        },
        {
          x: 50,
          y: 55,
          check: t(
            '溶接{ようせつ}ケーブル被覆{ひふく}の亀裂{きれつ}・摩耗{まもう}：踏{ふ}まれや引{ひ}っ張{ぱ}りによる素線{そせん}露出{ろしゅつ}がないか（良品{りょうひん}：傷{きず}なし / 不良{ふりょう}：ゴム被覆{ひふく}が破{やぶ}れて銅線{どうせん}がむき出{だ}し）',
            'Welding cable insulation cracks/wear: No exposed strands from pinching or dragging (Good: Sound sheath / Defective: Rubber torn exposing copper wire)',
          ),
        },
        {
          x: 75,
          y: 70,
          check: t(
            'アースクランプのバネと先端{せんたん}：錆{さび}や変形{へんけい}がなく母材{ぼざい}を強固{きょうこ}に挟{はさ}み込{こ}めるか（良品{りょうひん}：強力{きょうりょく}に固定{こてい} / 不良{ふりょう}：バネが緩{ゆる}くガタつく）',
            'Earth clamp spring and jaw: Clamps base metal firmly without rust or distortion (Good: Strong bite / Defective: Weak loose spring)',
          ),
        },
      ],
    },
    {
      id: 'welding-ppe-inspect',
      defective: false,
      name: t(
        '溶接用{ようせつよう}保護具{ほごぐ}（遮光面{しゃこうめん}・皮手袋{かわてぶくろ}）の点検{てんけん}',
        'Welding PPE inspection (helmet & leather gloves)',
      ),
      spots: [
        {
          x: 30,
          y: 30,
          check: t(
            '遮光面{しゃこうめん}プレートの状態{じょうたい}：ひび割{わ}れ・破損{はそん}がなく作業{さぎょう}に適{てき}した遮光度{しゃこうど}番号{ばんごう}か（良品{りょうひん}：透明度{とうめいど}良好{りょうこう}で割{わ}れなし / 不良{ふりょう}：フィルター割{わ}れや極度{きょくど}の汚れ{よごれ}）',
            'Welding helmet filter plate condition: Free of cracks with proper shade number (Good: Clean and intact / Defective: Cracked or heavily soiled)',
          ),
        },
        {
          x: 55,
          y: 50,
          check: t(
            '革手袋{かわてぶくろ}の乾燥{かんそう}と破{やぶ}れ：水分{すいぶん}付着{ふちゃく}がなく十分{じゅうぶん}に乾燥{かんそう}し穴{あな}がないか（良品{りょうひん}：完全{かんぜん}乾燥{かんそう}で無傷{むきず} / 不良{ふりょう}：濡{ぬ}れや指先{ゆびさき}の穴{あな}あき）',
            'Leather gloves dryness and tears: Completely dry without moisture or holes (Good: Fully dry and sound / Defective: Wet or torn at fingertips)',
          ),
        },
        {
          x: 70,
          y: 75,
          check: t(
            '防{ぼう}じんマスクの密着{みっちゃく}とフィルター：接顔部{せつがんぶ}の変形{へんけい}がなくフィルターが目詰{めづ}まりしていないか（良品{りょうひん}：フィット良好{りょうこう}・通気{つうき}正常{せいじょう} / 不良{ふりょう}：ゴム劣化{れっか}・目詰{めづ}まり大{だい}）',
            'Dust mask face seal and filter: No deformation of facepiece and filter not clogged (Good: Good seal and normal airflow / Defective: Degraded rubber or clogged)',
          ),
        },
      ],
    },
    {
      id: 'gas-shield-inspect',
      defective: true,
      name: t(
        'シールドガスボンベおよび圧力{あつりょく}調整器{ちょうせいき}の点検{てんけん}',
        'Shield gas cylinder and regulator inspection',
      ),
      spots: [
        {
          x: 35,
          y: 25,
          check: t(
            '圧力{あつりょく}調整器{ちょうせいき}（レギュレーター）のゲージ：指針{ししん}折{お}れやカバーガラス割{わ}れがないか（良品{りょうひん}：指針{ししん}正常{せいじょう} / 不良{ふりょう}：ガラスが割{わ}れて針{はり}が曲{ま}がっている）',
            'Pressure regulator gauges: No bent needles or broken glass covers (Good: Clear undamaged gauges / Defective: Smashed glass with bent needle)',
          ),
        },
        {
          x: 50,
          y: 55,
          check: t(
            'ボンベ転倒{てんとう}防止{ぼうし}チェーン：ボンベ胴部{どうぶ}に2重{じゅう}または確実{かくじつ}にチェーンが掛{か}けられているか（良品{りょうひん}：強固{きょうこ}に固定{こてい} / 不良{ふりょう}：チェーンが外{はず}れて転倒{てんとう}の危険{きけん}あり）',
            'Cylinder anti-toppling chain: Secured firmly around cylinder body (Good: Chained tightly / Defective: Chain loose or detached)',
          ),
        },
        {
          x: 65,
          y: 80,
          check: t(
            'ガスホースの接続部{せつぞくぶ}と外傷{がいしょう}：ひび割{わ}れやホツレがなくバンドで締{し}め付{つ}けられているか（良品{りょうひん}：亀裂{きれつ}なし / 不良{ふりょう}：根元{ねもと}に深{ふか}いひび割{わ}れあり）',
            'Gas hose connection and condition: Band-clamped securely without cracks or fraying (Good: Intact hose / Defective: Deep dry rot cracks near joint)',
          ),
        },
      ],
    },
  ],
  quiz: [
    {
      q: t(
        '造船所{ぞうせんじょ}でアーク溶接{ようせつ}を作業中{さぎょうちゅう}、手袋{てぶくろ}が汗{あせ}や水{みず}で湿{しめ}ってきました。感電{かんでん}防止{ぼうし}のため、作業員{さぎょういん}が直{ただ}ちに取{と}るべき正{ただ}しい行動{こうどう}はどれですか？',
        'While arc welding in a shipyard, your gloves become damp with sweat and water. To prevent electric shock, what is the correct action to take immediately?',
      ),
      options: [
        t(
          '溶接機{ようせつき}のスイッチを切{き}り、完全{かんぜん}に乾{かわ}いた溶接用{ようせつよう}革手袋{かわてぶくろ}に直{ただ}ちに交換{こうかん}する',
          'Turn off the welding machine switch and immediately change to completely dry welding leather gloves',
        ),
        t(
          '水{みず}で濡{ぬ}れた手袋{てぶくろ}の上{うえ}からビニールテープを巻{ま}いてそのまま溶接{ようせつ}を続{つづ}ける',
          'Wrap vinyl tape over the wet gloves and continue welding as is',
        ),
        t(
          '濡{ぬ}れていても直接{ちょくせつ}溶接棒{ようせつぼう}に触{ふ}れなければ感電{かんでん}の危険{きけん}はないのでそのまま作業{さぎょう}する',
          'Continue working since there is no danger of electric shock as long as you do not touch the rod directly',
        ),
        t(
          '手袋{てぶくろ}を外{はず}して素手{すで}の方{ほう}が乾{かわ}きやすいので、素手{すで}でホルダーを握{にぎ}る',
          'Remove gloves and hold the holder barehanded because bare hands dry faster',
        ),
      ],
      answer: 0,
      why: t(
        '濡{ぬ}れた皮手袋{かわてぶくろ}は電気抵抗{でんきていこう}が著{いちじる}しく低下{ていか}し、感電{かんでん}死亡{しぼう}災害{さいがい}を招{まね}く極{きわ}めて危険{きけん}な状態{じょうたい}です。直{ただ}ちに電源{でんげん}を切{き}り、乾燥{かんそう}した手袋{てぶくろ}に交換{こうかん}しなければなりません。',
        'Wet leather gloves drastically reduce electrical resistance, creating an extreme risk of fatal electric shock. Always cut power and change to dry gloves.',
      ),
    },
    {
      q: t(
        '新{あたら}しく工場{こうじょう}に配属{はいぞく}された作業者{さぎょうしゃ}がアーク溶接{ようせつ}作業{さぎょう}を行{おこな}う際{さい}、労働安全衛生法{ろうどうあんぜんえいせいほう}で定{さだ}められた必須{ひっす}の条件{じょうけん}はどれですか？',
        'When a newly assigned worker performs arc welding operations in a factory, what is the statutory requirement under the Industrial Safety and Health Act?',
      ),
      options: [
        t(
          '先輩{せんぱい}作業員{さぎょういん}から口頭{こうとう}で10分間{ぷんかん}の説明{せつめい}を受{う}ければ資格{しかく}なしで作業{さぎょう}できる',
          'Can work without qualifications after receiving a 10-minute verbal briefing from a senior coworker',
        ),
        t(
          '法令{ほうれい}に基{づ}く「アーク溶接{ようせつ}等{とう}の業務{ぎょうむ}にかかる特別教育{とくべつきょういく}」を修了{しゅうりょう}していること',
          'Having completed the statutory Special Education for arc welding operations',
        ),
        t(
          '自動車{じどうしゃ}の普通{ふつう}運転免許証{うんてんめんきょしょう}を持{も}っていれば誰{だれ}でも作業{さぎょう}できる',
          "Anyone who holds a regular driver's license can perform welding operations",
        ),
        t(
          '会社{かいしゃ}の役員{やくいん}や社長{しゃちょう}の許可{きょか}があれば教育{きょういく}を受{う}けなくても自由{じゆう}に溶接{ようせつ}できる',
          'Can weld freely without training as long as permitted by the company president',
        ),
      ],
      answer: 1,
      why: t(
        '労働安全衛生規則{ろうどうあんぜんえいせいきそく}第{だい}36条{じょう}第{だい}3号{ごう}により、アーク溶接機{ようせつき}を用{もち}いる業務{ぎょうむ}には学科{がっか}・実技{じつぎ}を含{ふく}む「特別教育{とくべつきょういく}」の修了{しゅうりょう}が法令上{ほうれいじょう}義務付{ぎむづ}けられています。',
        'Under Ordinance on Industrial Safety and Health Art. 36 item 3, completing Special Education (lectures and practical training) is mandatory for arc welding.',
      ),
    },
    {
      q: t(
        'アーク溶接{ようせつ}を行{おこな}う作業場所{さぎょうばしょ}の周囲{しゅうい}における火災{かさい}防止{ぼうし}対策{たいさく}として、最も{もっとも}適切{てきせつ}なものはどれですか？',
        'Which of the following is the most appropriate fire prevention measure for the area surrounding arc welding work?',
      ),
      options: [
        t(
          '火花{ひばな}が出{で}たら足{あし}で踏{ふ}み消{け}せばよいので、足元{あしもと}に段{だん}ボールがあってもそのまま溶接{ようせつ}する',
          'Weld as is with cardboard beneath feet, assuming sparks can simply be stomped out',
        ),
        t(
          '風{かぜ}を当{あ}てて火花{ひばな}を遠{とお}くへ飛{と}ばすために、大型{おおがた}扇風機{せんぷうき}を溶接部{ようせつぶ}の正面{しょうめん}から強風{きょうふう}で回{まわ}す',
          'Blow high-powered fans directly at the weld to scatter sparks far away into the air',
        ),
        t(
          '周囲{しゅうい}の可燃物{かねんぶつ}を取{と}り除{のぞ}き、移動{いどう}できないものは防炎{ぼうえん}シートで覆{おお}い、消火器{しょうかき}を手元{てもと}に常備{じょうび}する',
          'Remove nearby combustibles, cover immovable items with fireproof sheets, and keep a fire extinguisher at hand',
        ),
        t(
          '消火器{しょうかき}は邪魔{じゃま}になるため、作業現場{さぎょうげんば}から100m以上{いじょう}離{はな}れた倉庫{そうこ}に保管{ほかん}しておく',
          'Store extinguishers in a warehouse over 100 meters away so they do not clutter the workspace',
        ),
      ],
      answer: 2,
      why: t(
        '溶接{ようせつ}の火花{ひばな}は広範囲{こうはんい}に飛{と}び散{ち}り火災{かさい}の原因{げんいん}になります。周囲{しゅうい}の可燃物{かねんぶつ}除去{じょきょ}、防炎{ぼうえん}シートによる養生{ようじょう}、手元{てもと}への消火器{しょうかき}配置{はいち}が三原則{さんげんそく}です。',
        'Welding sparks scatter widely and ignite fires. The three golden rules are clearing combustibles, covering with fireproof sheets, and placing an extinguisher nearby.',
      ),
    },
    {
      q: t(
        '交流{こうりゅう}アーク溶接機{ようせつき}に取{と}り付{つ}けられている「自動電撃防止装置{じどうでんげきぼうしそうち}（電防{でんぼう}）」の主{おも}な役割{やくわり}として、正{ただ}しい説明{せつめい}はどれですか？',
        'Which of the following correctly explains the primary role of the automatic electric shock prevention device installed on an AC arc welder?',
      ),
      options: [
        t(
          '溶接{ようせつ}のスピードを自動的{じどうてき}に2倍{ばい}に高{たか}めて作業時間{さぎょうじかん}を短縮{たんしゅく}する',
          'Automatically doubles welding speed to shorten work time',
        ),
        t(
          '溶接{ようせつ}中{ちゅう}に出{で}る有害{ゆうがい}な煙{けむり}を自動{じどう}で消{き}え去{さ}らせる',
          'Automatically eliminates harmful fumes generated during welding',
        ),
        t(
          '溶接棒{ようせつぼう}が短{みじか}くなったときに新{あたら}しい棒{ぼう}を自動{じどう}でホルダーに補充{ほじゅう}する',
          'Automatically feeds a new welding rod into the holder when the rod shortens',
        ),
        t(
          'アーク停止時{ていしじ}にホルダーの無負荷{むふか}電圧{でんあつ}を安全{あんぜん}な低電圧{ていでんあつ}（25V以下{いか}）に自動{じどう}で下{さ}げて感電{かんでん}を防{ふせ}ぐ', // verify: 電防によりアーク停止時の無負荷電圧を25V以下に低下
          'Automatically lowers no-load voltage at the holder to a safe level (25V or below) when not arcing to prevent shock',
        ),
      ],
      answer: 3,
      why: t(
        '自動電撃防止装置{じどうでんげきぼうしそうち}は、溶接棒{ようせつぼう}が母材{ぼざい}から離{はな}れている間{あいだ}の無負荷{むふか}電圧{でんあつ}を瞬時{しゅんじ}に25V以下{いか}の安全{あんぜん}電圧{でんあつ}に低下{ていか}させ、棒{ぼう}交換時{こうかんじ}の感電{かんでん}を防{ふせ}ぐ安全{あんぜん}装置{そうち}です。',
        'The shock preventer instantly drops no-load voltage to a safe 25V or less while not welding, protecting workers from shocks when replacing rods.',
      ),
    },
    {
      q: t(
        'アーク溶接{ようせつ}時{じ}に発生{はっせい}する「溶接{ようせつ}ヒューム」と作業者{さぎょうしゃ}の健康{けんこう}障害{しょうがい}に関{かん}する記述{きじゅつ}として、正{ただ}しいものはどれですか？',
        'Which of the following statements is correct regarding "welding fumes" generated during arc welding and workers\' health?',
      ),
      options: [
        t(
          '金属{きんぞく}の微粒子{びりゅうし}であり、長年{ながねん}吸入{きゅうにゅう}すると「じん肺{はい}」や神経{しんけい}障害{しょうがい}を引{ひ}き起{お}こすため、防{ぼう}じんマスクと局所排気{きょくしょはいき}が必須{ひっす}である',
          'They are fine metal particles; inhaling them over years causes pneumoconiosis and neurological disorders, requiring dust masks and exhaust ventilation',
        ),
        t(
          '水蒸気{すいじょうき}と同{おな}じ無害{むがい}な気体{きたい}なので、換気{かんき}のない密閉{みっぺい}部屋{べや}で深呼吸{しんこきゅう}しても健康{けんこう}に影響{えいきょう}はない',
          'They are harmless vapor like steam, so breathing deeply in a sealed room has no health impact',
        ),
        t(
          '市販{しはん}の布{ぬの}マスクやガーゼマスクを1枚{まい}着{つ}けていれば有害{ゆうがい}ヒュームを100%完全{かんぜん}に防{ふせ}げる',
          'Wearing a regular cloth or gauze mask will 100% completely block harmful fumes',
        ),
        t(
          '溶接{ようせつ}ヒュームは体{からだ}に良{よ}い栄養素{えいようそ}を含{ふく}んでいるため、マスクを外{はず}して作業{さぎょう}することが推奨{すいしょう}される',
          'Welding fumes contain beneficial nutrients, so welding without a mask is recommended',
        ),
      ],
      answer: 0,
      why: t(
        '溶接{ようせつ}ヒュームは金属{きんぞく}が蒸発{じょうはつ}・酸化{さんか}してできた微細{びさい}粒子{りゅうし}で、吸入{きゅうにゅう}し続{つづ}けるとじん肺{はい}や重篤{じゅうとく}な健康{けんこう}被害{ひがい}をもたらします。検定{けんてい}合格{ごうかく}の防{ぼう}じんマスクと排気装置{はいきそうち}が必要{ひつよう}です。',
        'Welding fumes are fine oxidized metal particles causing pneumoconiosis and serious illness over time. Approved dust respirators and ventilation are essential.',
      ),
    },
    {
      q: t(
        '溶接{ようせつ}のアーク光{こう}（紫外線{しがいせん}）を直接{ちょくせつ}裸眼{らがん}で見{み}続{つづ}けた場合{ばあい}に起{お}きる健康{けんこう}被害{ひがい}と、その予防策{よぼうさく}として正{ただ}しいものはどれですか？',
        'Which describes the health harm caused by looking directly at arc light (UV) with bare eyes, along with its correct preventive measure?',
      ),
      options: [
        t(
          '視力{しりょく}が一時的{いちじてき}に良{よ}くなるため、遮光面{しゃこうめん}を使{つか}わずに肉眼{にくがん}で見{み}るのが正{ただ}しい',
          'Visual acuity temporarily improves, so looking with bare eyes without a welding helmet is correct',
        ),
        t(
          '夜間{やかん}に目{め}が激痛{げきつう}で開{あ}けられなくなる「アーク眼{がん}（電気性眼炎{でんきせいがんえん}）」を起{お}こすため、適切{てきせつ}な遮光度{しゃこうど}の遮光面{しゃこうめん}を着{つ}ける',
          'Causes "arc eye" (photokeratitis) causing severe nighttime eye pain, prevented by wearing a welding helmet with proper shade',
        ),
        t(
          '普通{ふつう}の透明{とうめい}な近視用{きんしよう}メガネをかけていればアーク光{こう}の紫外線{しがいせん}を完全{かんぜん}に防{ふせ}げる',
          'Regular clear prescription glasses completely block all UV radiation from arc light',
        ),
        t(
          '少{すこ}し眩{まぶ}しいだけなので、目{め}を細{ほそ}めて横{よこ}を向{む}きながら溶接{ようせつ}すれば何{なに}も着{つ}けなくてよい',
          'It is only slightly bright, so squinting and looking sideways eliminates the need for protection',
        ),
      ],
      answer: 1,
      why: t(
        'アーク光{こう}に含まれる強力{きょうりょく}な紫外線{しがいせん}は角膜{かくまく}を傷{きず}つけ、数時間後{すうじかんご}に激痛{げきつう}を伴{ともな}う「アーク眼{がん}」を引{ひ}き起{お}こします。作業{さぎょう}に適{てき}した遮光番号{しゃこうばんごう}の遮光面{しゃこうめん}着用{ちゃくよう}が必須{ひっす}です。',
        'Strong UV in arc light damages the cornea, triggering agonizing "arc eye" hours later. Wearing a welding face shield with proper shade is mandatory.',
      ),
    },
    {
      q: t(
        'アーク溶接{ようせつ}時{じ}のアース線{せん}（接地線{せっちせん}）の取{と}り付{つ}け方法{ほうほう}について、感電{かんでん}や火災{かさい}・迷走電流{めいそうでんりゅう}を防{ふせ}ぐ正{ただ}しいやり方はどれですか？',
        'Regarding the attachment of the earth ground cable during arc welding, what is the correct method to prevent electric shock, fires, and stray currents?',
      ),
      options: [
        t(
          'アース線{せん}を床{ゆか}の水{みず}たまりに浸{つ}けておくだけで十分{じゅうぶん}にアースが取{と}れる',
          'Simply dipping the earth wire into a puddle on the floor provides sufficient grounding',
        ),
        t(
          '工場{こうじょう}の足場{あしば}パイプやガス配管{はいかん}のどこでもよいので適当{てきとう}に挟{はさ}んでおく',
          'Randomly clamp it anywhere on scaffold pipes or gas piping in the factory',
        ),
        t(
          '溶接{ようせつ}する母材{ぼざい}（鋼板{こうはん}等{など}）の溶接{ようせつ}箇所{かしょ}に近{ちか}い場所{ばしょ}へ直接{ちょくせつ}・確実{かくじつ}にクランプする',
          'Firmly clamp directly to the workpiece base metal (steel plate, etc.) near the welding location',
        ),
        t(
          'アース線{せん}は作業{さぎょう}の邪魔{じゃま}になるため、ケーブルを切断{せつだん}してどこにも繋{つな}がずに作業{さぎょう}する',
          'Cut the earth cable and weld without connecting it anywhere because the cable gets in the way',
        ),
      ],
      answer: 2,
      why: t(
        'アーク接地{せっち}が不十分{ふじゅうぶん}だと迷走電流{めいそうでんりゅう}が足場{あしば}やクレーンワイヤーを流{なが}れてスパーク火災{かさい}や機械{きかい}損傷{そんしょう}を起{お}こします。必{かなら}ず溶接箇所{ようせつかしょ}の母材{ぼざい}に直接{ちょくせつ}クランプします。',
        'Poor grounding causes stray currents through scaffolds or crane wires, sparking fires or damaging gear. Always clamp directly to the workpiece.',
      ),
    },
    {
      q: t(
        '造船所{ぞうせんじょ}の二重底{にじゅうぞこ}や密閉{みっぺい}タンク内{ない}でアーク溶接{ようせつ}を行{おこな}う際{さい}、命{いのち}を守{まも}るために最優先{さいゆうせん}で実施{じっし}すべき安全{あんぜん}対策{たいさく}はどれですか？',
        'When arc welding inside a shipyard double bottom or confined tank, what safety measure must be prioritized above all to protect life?',
      ),
      options: [
        t(
          '暗{くら}くて見{み}えにくいので、酸素{さんそ}ボンベのバルブを開{あ}けて部屋{へや}に酸素{さんそ}を充満{じゅうまん}させて作業{さぎょう}する',
          'Open an oxygen cylinder valve to fill the space with pure oxygen so it is easier to see',
        ),
        t(
          '狭{せま}い場所{ばしょ}では声{こえ}が響{ひび}くので、外{そと}の監視員{かんしいん}と連絡{れんらく}をとらずに静{しず}かに作業{さぎょう}する',
          'Work silently without communicating with the outside watcher because voices echo in tight spaces',
        ),
        t(
          '暑{あつ}いので保護具{ほごぐ}をすべて脱{ぬ}いでTシャツ1枚{まい}で溶接{ようせつ}する',
          'Take off all protective equipment and weld in a T-shirt because it is hot',
        ),
        t(
          '作業前{さぎょうまえ}に酸素濃度{さんそのうど}（18%以上{いじょう}）を測定{そくてい}し、常時{じょうじ}強制{きょうせい}換気{かんき}を行{おこな}い、外部{がいぶ}に見張{みは}り員{いん}を配置{はいち}する', // verify: 酸素欠乏危険場所での酸素濃度基準は18%以上
          'Measure oxygen levels (18%+) before entry, maintain continuous forced ventilation, and post an outside lookout',
        ),
      ],
      answer: 3,
      why: t(
        '密閉空間{みっぺいくうかん}では溶接{ようせつ}による酸素{さんそ}消費{しょうひ}やシールドガス滞留{たいりゅう}で酸欠{さんけつ}・中毒{ちゅうどく}が起{お}きやすくなります。18%以上{いじょう}の酸素{さんそ}確認{かくにん}、送風{そうふう}換気{かんき}、監視員{かんしいん}の配置{はいち}が不可欠{ふかけつ}です。なお純酸素{じゅんさんそ}の放出{ほうしゅつ}は爆発{ばくはつ}火災{かさい}を招{まね}くため絶対禁止{ぜったいきんし}です。',
        'Confined spaces risk asphyxiation from oxygen consumption or inert gas accumulation. Confirming 18%+ O2, continuous ventilation, and an outside watcher are mandatory. Releasing pure oxygen is strictly forbidden.',
      ),
    },
    {
      q: t(
        '炭酸{たんさん}ガスやアルゴンガスなど高圧{こうあつ}シールドガスボンベの取{と}り扱{あつか}い方法{ほうほう}として、正{ただ}しいものはどれですか？',
        'Which of the following is the correct method for handling high-pressure shield gas cylinders such as CO2 or argon?',
      ),
      options: [
        t(
          '直射日光{ちょくしゃにっこう}を避{さ}け、転倒{てんとう}防止{ぼうし}のためにチェーンなどで専用{せんよう}スタンドや壁{かべ}に確実{かくじつ}に固定{こてい}する',
          'Avoid direct sunlight and secure firmly to a dedicated rack or wall with chains to prevent toppling',
        ),
        t(
          'いつでも転{ころ}がして移動{いどう}できるように、通路{つうろ}の真{ま}ん中{なか}に横{よこ}にして置{お}いておく',
          'Lay it sideways in the middle of walkways so it can be rolled around easily anytime',
        ),
        t(
          'ガスが冷{つめ}たくなったら、溶接{ようせつ}の火花{ひばな}やバーナーの炎{ほのお}で直接{ちょくせつ}ボンベを炙{あぶ}って温{あたた}める',
          'Warm the cylinder directly with welding sparks or a torch flame if the gas gets cold',
        ),
        t(
          'ホースが届{とど}かないときは、クレーンのワイヤー1本{ほん}でボンベのバルブを縛{しば}り宙吊{ちゅうづ}りにして溶接{ようせつ}する',
          'Suspend the cylinder by tying crane wire to its valve if the hose is too short',
        ),
      ],
      answer: 0,
      why: t(
        '高圧{こうあつ}ガスボンベが転倒{てんとう}してバルブが損傷{そんしょう}すると、ロケットのように噴射{ふんしゃ}して大惨事{だいさんじ}になります。必{かなら}ず直立{ちょくりつ}させてチェーンで2重{じゅう}固定{こてい}します。加熱{かねつ}も厳禁{げんきん}です。',
        'If a high-pressure cylinder falls and breaks its valve, it can launch like a missile. Always keep upright, double-chained, and never apply heat.',
      ),
    },
    {
      q: t(
        'アーク溶接{ようせつ}の作業前{さぎょうまえ}点検{てんけん}中{ちゅう}、溶接{ようせつ}ケーブルの途{と}中でゴム被覆{ひふく}が破{やぶ}れ、内部{ないぶ}の銅線{どうせん}が露出{ろしゅつ}しているのを見{み}つけました。どうすべきですか？',
        'During pre-welding inspection, you discover the rubber sheath of a welding cable is torn with internal copper wire exposed. What should you do?',
      ),
      options: [
        t(
          '銅線{どうせん}に直接{ちょくせつ}水{みず}をかけて冷{ひ}やしながらそのまま溶接{ようせつ}作業{さぎょう}を始{はじ}める',
          'Pour water directly on the copper wire to cool it down and proceed with welding',
        ),
        t(
          '直{ただ}ちに職長{しょくちょう}に報告{ほうこく}し、指定{してい}の絶縁{ぜつえん}処理{しょり}（熱収縮{ねつしゅうしゅく}チューブ等{など}）で補修{ほしゅう}するか健全{けんぜん}なケーブルに交換{こうかん}する',
          'Report immediately to the foreman and either repair with approved insulation (heat-shrink tubing, etc.) or replace with an undamaged cable',
        ),
        t(
          '紙{かみ}のガムテープを薄{うす}く1枚{まい}巻{ま}いて目立{めだ}たなくして作業{さぎょう}する',
          'Wrap a single strip of paper packaging tape over it to hide it and continue working',
        ),
        t(
          '露出{ろしゅつ}した部分{ぶぶん}を手{て}で押{お}さえつけながら溶接機{ようせつき}の電流{でんりゅう}を最大{さいだい}にする',
          'Hold down the exposed part by hand while turning welder current to maximum',
        ),
      ],
      answer: 1,
      why: t(
        'ケーブルの銅線{どうせん}露出{ろしゅつ}は重大{じゅうだい}な感電{かんでん}や短絡{たんらく}（ショート）火災{かさい}の原因{げんいん}になります。簡易{かんい}テープ等{など}でごまかさず、直{ただ}ちに報告{ほうこく}して完全{かんぜん}な絶縁{ぜつえん}補修{ほしゅう}または交換{こうかん}を行{おこな}います。',
        'Exposed cable wire causes fatal electric shocks and short-circuit fires. Do not patch with makeshift tape; report immediately and repair or replace properly.',
      ),
    },
    {
      q: t(
        '本日{ほんじつ}の溶接{ようせつ}作業{さぎょう}がすべて終了{しゅうりょう}しました。作業場所{さぎょうばしょ}を離{はな}れる際{さい}の安全{あんぜん}手順{てじゅん}として、正{ただ}しいものはどれですか？',
        'All welding work for today is complete. What is the correct safety procedure before leaving the workspace?',
      ),
      options: [
        t(
          '明日{あす}の朝{あさ}すぐ溶接{ようせつ}できるよう、主電源{しゅでんげん}を入{い}れたままホルダーに新{あたら}しい溶接棒{ようせつぼう}を挟{はさ}んで帰{かえ}る',
          'Leave the main power ON with a fresh rod in the holder so you can weld immediately tomorrow morning',
        ),
        t(
          '火花{ひばな}の飛{と}んだ場所{ばしょ}を見{み}ずに、作業{さぎょう}が終{お}わったら1秒{びょう}ですぐに現場{げんば}から走{はし}り去{さ}る',
          'Sprint away from the site the very second work ends without checking where sparks flew',
        ),
        t(
          '溶接機{ようせつき}の主電源{しゅでんげん}を切{き}って片付{かたづ}け、最低{さいてい}30分間{ぷんかん}は火花{ひばな}が飛散{ひさん}した範囲{はんい}の残火{ざんか}・燻{くすぶ}りを見張{みは}る', // verify: 溶接作業終了後の火気見張り時間は原則30分以上
          'Switch OFF welder main power, tidy up, and watch the spark zone for residual embers or smoldering for at least 30 minutes',
        ),
        t(
          '残火{ざんか}を確認{かくにん}するため、周囲{しゅうい}のダンボールにライターで火{ひ}をつけて試{ため}す',
          'Test for residual fire by lighting nearby cardboard boxes with a cigarette lighter',
        ),
      ],
      answer: 2,
      why: t(
        '溶接{ようせつ}終了後{しゅうりょうご}、数十分{すうじっぷん}経{た}ってから火花{ひばな}が着火{ちゃっか}して大火災{だいかさい}になるケースが多発{たはつ}しています。電源{でんげん}遮断{しゃだん}に加{くわ}え、最低{さいてい}30分間{ぷんかん}の残火{ざんか}見張{みは}り（火気見張{かきみは}り）が必須{ひっす}です。',
        'Delayed fires frequently break out tens of minutes after welding when unnoticed sparks ignite combustibles. Cutting power and a 30+ minute fire watch are mandatory.',
      ),
    },
    {
      q: t(
        '溶接{ようせつ}中{ちゅう}、同僚{どうりょう}の背後{はいご}にある木製{もくせい}パレットから煙{けむり}が上{あ}がり、小{ちい}さな炎{ほのお}が出{で}ているのを発見{はっけん}しました。最初{さいしょ}に取{と}るべき行動{こうどう}はどれですか？',
        'While welding, you notice smoke and small flames rising from a wooden pallet behind your coworker. What is the very first action you should take?',
      ),
      options: [
        t(
          '自分{じぶん}の仕事{しごと}ではないので、気{き}づかないふりをして自分{じぶん}の溶接{ようせつ}に集中{しゅうちゅう}する',
          'Pretend not to notice and focus on your own weld since it is not your assigned job',
        ),
        t(
          '記念{きねん}にスマートフォンで動画{どうが}を撮影{さつえい}してSNSに投稿{とうこう}する',
          'Take a video on your smartphone for memories and post it to social media',
        ),
        t(
          '誰{だれ}にも知{し}られないように足{あし}で隠{かく}してそのまま放置{ほうち}する',
          'Cover it with your foot to hide it from others and leave it unattended',
        ),
        t(
          '大声{おおごえ}で「火事{かじ}だ！」と周囲{しゅうい}に知{し}らせて作業{さぎょう}を中断{ちゅうだん}させ、手元{てもと}の消火器{しょうかき}で直{ただ}ちに初期{しょき}消火{しょうか}を行{おこな}う',
          'Loudly yell "Fire!" to warn coworkers, halt work, and immediately perform initial extinguishing with the fire extinguisher at hand',
        ),
      ],
      answer: 3,
      why: t(
        '火災{かさい}を発見{はっけん}した時{とき}は直{ただ}ちに大声{おおごえ}で周囲{しゅうい}に知{し}らせ、初期消火{しょきしょうか}を行{おこな}います。危険{きけん}を発見{はっけん}した際{さい}は誰{だれ}でも作業{さぎょう}を中断{ちゅうだん}させる権利{けんり}と義務{ぎむ}があります。',
        'Upon spotting a fire, immediately shout to alert everyone and execute initial firefighting. Anyone has both the right and duty to stop work upon acute danger.',
      ),
    },
  ],
  glossary: [
    {
      ja: 'アーク溶接',
      reading: 'あーくようせつ',
      en: 'arc welding',
      note: t(
        '電気{でんき}の火花{ひばな}（アーク）の熱{ねつ}を利用{りよう}して金属{きんぞく}同士{どうし}を溶{と}かして接合{せつごう}する作業{さぎょう}。',
        'Welding process that joins metals by melting them using the heat of an electric arc.',
      ),
    },
    {
      ja: '感電',
      reading: 'かんでん',
      en: 'electric shock',
      note: t(
        '人体{じんたい}に電流{でんりゅう}が流{なが}れてショックを受{う}けること。特{とく}に濡{ぬ}れた手{て}や体{からだ}は死亡{しぼう}事故{じこ}に直結{ちょっけつ}する。',
        'Shock caused by electric current passing through the body. Wet conditions can easily cause death.',
      ),
    },
    {
      ja: '自動電撃防止装置',
      reading: 'じどうでんげきぼうしそうち',
      en: 'automatic electric shock prevention device',
      note: t(
        'アークが停止{ていし}している間{あいだ}、溶接機{ようせつき}の電圧{でんあつ}を自動{じどう}で安全{あんぜん}な低電圧{ていでんあつ}（25V以下{いか}）に下{さ}げる安全{あんぜん}装置{そうち}。通称{つうしょう}「電防{でんぼう}」。', // verify: 電防によりアーク停止時の無負荷電圧を25V以下に低下
        'Safety device lowering welder voltage to safe level (25V or less) when not welding. Commonly called "denbo".',
      ),
    },
    {
      ja: 'スパッタ',
      reading: 'すぱった',
      en: 'welding spatter',
      note: t(
        '溶接中{ようせつちゅう}に飛{と}び散{ち}る溶{と}けた金属{きんぞく}の小{ちい}さな粒{つぶ}や火花{ひばな}。周囲{しゅうい}の可燃物{かねんぶつ}に引火{いんか}して火災{かさい}の原因{げんいん}になる。',
        'Droplets of molten metal and sparks scattered during welding, capable of igniting surrounding materials.',
      ),
    },
    {
      ja: '防炎シート',
      reading: 'ぼうえんしーと',
      en: 'fireproof sheet / welding blanket',
      note: t(
        '火花{ひばな}（スパッタ）を受{う}けても燃{も}え広{ひろ}がらない特殊{とくしゅ}な耐火{たいか}織物{おりもの}シート。可燃物{かねんぶつ}の養生{ようじょう}に使{つか}う。',
        'Fire-resistant fabric sheet that will not ignite from sparks, used to shield combustible materials.',
      ),
    },
    {
      ja: '遮光面',
      reading: 'しゃこうめん',
      en: 'welding helmet / face shield',
      note: t(
        '強烈{きょうれつ}なアーク光{こう}（紫外線{しがいせん}・赤外線{せきがいせん}）から顔{かお}と目{め}を守{まも}る遮光{しゃこう}フィルター付{つ}きの保護面{ほごめん}。',
        'Protective shield with optical filter shielding eyes and face from intense UV and infrared arc light.',
      ),
    },
    {
      ja: 'アーク眼',
      reading: 'あーくがん',
      en: 'arc eye (photokeratitis)',
      note: t(
        'アーク光{こう}の紫外線{しがいせん}によって角膜{かくまく}が炎症{えんしょう}を起{お}こす障害{しょうがい}。数時間後{すうじかんご}に目{め}に激{はげ}しい痛{いた}みが生{しょう}じる。',
        'Corneal inflammation caused by arc ultraviolet light, causing severe eye pain several hours later.',
      ),
    },
    {
      ja: '溶接ヒューム',
      reading: 'ようせつひゅーむ',
      en: 'welding fumes',
      note: t(
        '溶接{ようせつ}の熱{ねつ}で蒸発{じょうはつ}した金属{きんぞく}が空気中{くうきちゅう}で冷{ひ}えて酸化{さんか}した微細{びさい}な粉じん。吸入{きゅうにゅう}を続{つづ}けるとじん肺{はい}を起{お}こす。',
        'Fine dust formed when vaporized metal oxidizes and cools in air. Prolonged inhalation causes pneumoconiosis.',
      ),
    },
    {
      ja: '防じんマスク',
      reading: 'ぼうじんますく',
      en: 'particulate respirator / dust mask',
      note: t(
        '空気中{くうきちゅう}の微粒子{びりゅうし}（ヒュームや粉じん）をろ過{か}して体内{たいない}への吸入{きゅうにゅう}を防{ふせ}ぐ型式{かたしき}検定{けんてい}合格{ごうかく}のマスク。',
        'Certified respirator filtering airborne fine particles (fumes, dust) to prevent lung inhalation.',
      ),
    },
    {
      ja: '母材接地',
      reading: 'ぼざいせっち',
      en: 'base metal grounding / earth clamp',
      note: t(
        '溶接{ようせつ}電流{でんりゅう}の帰路{きろ}を確保{かくほ}するため、アース線{せん}を溶接{ようせつ}対象{たいしょう}の鋼板{こうはん}に直接{ちょくせつ}クランプして繋{つな}ぐこと。',
        'Directly clamping the ground cable to the workpiece to ensure a safe return path for weld current.',
      ),
    },
    {
      ja: '特別教育',
      reading: 'とくべつきょういく',
      en: 'special safety training',
      note: t(
        '労働安全衛生法{ろうどうあんぜんえいせいほう}に基づき、危険{きけん}・有害{ゆうがい}な業務{ぎょうむ}（アーク溶接{ようせつ}等{など}）に就{つ}く者{もの}が受講{じゅこう}を義務{ぎむ}付{づ}けられている教育{きょういく}。',
        'Mandatory statutory safety training for workers engaging in hazardous tasks such as arc welding.',
      ),
    },
    {
      ja: '火気見張り',
      reading: 'かきみはり',
      en: 'fire watch',
      note: t(
        '溶接{ようせつ}作業{さぎょう}の終了後{しゅうりょうご}、スパッタによる燻{くすぶ}りや残火{ざんか}がないかを現場{げんば}で一定{いってい}時間{じかん}（30分以上{ぷんいじょう}）監視{かんし}すること。', // verify: 溶接作業終了後の火気見張り時間は原則30分以上
        'Monitoring the work area for a set period (30+ minutes) after welding to spot smoldering embers.',
      ),
    },
  ],
}
