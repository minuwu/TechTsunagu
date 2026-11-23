import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string): Tri => ({ ja, en })

export const ppe: CourseData = {
  id: 'ppe',
  category: 'core',
  level: 1,
  minutes: 15,
  title: t('保護具{ほごぐ}', 'Personal protective equipment (PPE)'),
  sub: t(
    '身{み}の安全{あんぜん}を守{まも}るため、正{ただ}しい保護具{ほごぐ}を選{えら}び、正{ただ}しく身{み}につける。',
    'Choose and wear the right PPE correctly to protect your life.',
  ),
  refs: [
    '労働安全衛生法 第20条・第21条（事業者の講ずべき措置等）',
    '労働安全衛生規則 第593条〜第599条（保護具）',
    '厚生労働省 墜落制止用器具の安全な使用に関するガイドライン',
    '厚生労働省 保護帽の着用規定及び定期交換基準',
  ],
  panels: [
    {
      who: 'foreman',
      text: t(
        '今日{きょう}は工場{こうじょう}での保護具{ほごぐ}の正{ただ}しい使{つか}い方{かた}を確認{かくにん}します。',
        'Today we will confirm the correct use of personal protective equipment in the factory.',
      ),
    },
    {
      who: 'worker',
      text: t(
        'はい！ヘルメットを被{かぶ}り、安全靴{あんぜんぐつ}も履{は}いてきました。',
        'Yes! I put on my helmet and also wore safety shoes.',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'ヘルメットのあご紐{ひも}が緩{ゆる}んでいますよ。指{ゆび}が1本{いっぽん}入{はい}る程度{ていど}に締{し}めましょう。',
        'Your helmet chin strap is loose. Tighten it so that about one finger fits.',
      ),
    },
    {
      who: 'worker',
      text: t(
        'カチッと締{し}めました！この保護{ほご}メガネは、少{すこ}し曇{くも}るので外{はず}してもいいですか？',
        'I clicked it tight! These safety glasses fog up a little; may I take them off?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '「少{すこ}しだけ」でも外{はず}してはいけません。研磨{けんま}の火花{ひばな}や破片{はへん}が目{め}に入{はい}ると失明{しつめい}の危険{きけん}があります。',
        'Never remove them even for a moment. Grinding sparks or flying chips can blind you.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '危{あぶ}ないですね。それでは、ボール盤{ばん}で穴{あな}をあける作業{さぎょう}に軍手{ぐんて}をつけて入{はい}ります！',
        'That is dangerous. Then, I will wear cotton work gloves to drill holes on the drilling machine!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '待{ま}ちなさい！回転{かいてん}する機械{きかい}では手袋{てぶくろ}が巻{ま}き込{こ}まれるので、手袋{てぶくろ}の着用{ちゃくよう}は禁止{きんし}です。',
        'Stop! Wearing gloves is strictly prohibited near rotating machines because they can get caught.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '素手{すで}で作業{さぎょう}するのですね。危{あぶ}なく巻{ま}き込{こ}まれるところでした。',
        'So I must work with bare hands. I was close to a dangerous entanglement.',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '高所{こうしょ}作業{さぎょう}ではフルハーネス、粉{ふん}じん作業{さぎょう}では防{ぼう}じんマスクと、作業{さぎょう}ごとに選{えら}びます。',
        'Choose for each job: full harness for high places, and dust mask for dusty work.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '自分{じぶん}の身{み}を守{まも}るため、ルールを守{まも}って正{ただ}しく保護具{ほごぐ}を着用{ちゃくよう}します！',
        'To protect myself, I will obey the rules and wear my PPE correctly!',
      ),
    },
  ],
  handbook: [
    t(
      'ヘルメット（保護帽{ほごぼう}）のあご紐{ひも}は緩{ゆる}みなく必{かなら}ず締{し}める。',
      'Always fasten the helmet chin strap securely without any slack.',
    ),
    t(
      '一度{いちど}でも強{つよ}い衝撃{しょうげき}を受{う}けたヘルメットは、外見{がいけん}に傷{きず}がなくても交換{こうかん}・廃棄{はいき}する。',
      'Replace and discard any helmet that took a strong impact, even if it looks undamaged.',
    ),
    t(
      'FRP製{せい}ヘルメットは約{やく}5年{ねん}、熱可塑性樹脂製{ねつかそせいじゅしせい}は約{やく}3年{ねん}で定期的{ていきてき}に交換{こうかん}する。', // verify: FRP製保護帽は使用開始から約5年、熱可塑性樹脂製は約3年を目安に定期交換
      'Replace FRP helmets after roughly 5 years and thermoplastic ones after about 3 years.',
    ),
    t(
      'ボール盤{ばん}・旋盤{せんばん}など回転{かいてん}する機械{きかい}の作業{さぎょう}では、巻{ま}き込{こ}まれ防止{ぼうし}のため手袋{てぶくろ}を着用{ちゃくよう}してはならない。',
      'Never wear gloves near rotating machines like drills and lathes to avoid entanglement.',
    ),
    t(
      '高{たか}さ2m以上{いじょう}の手すりのない場所{ばしょ}では、墜落制止用器具{ついらくせいしようきぐ}（フルハーネス型{がた}）を正{ただ}しく着用{ちゃくよう}する。',
      'Wear a full-harness fall arrest system properly when working at heights of 2 meters or more without handrails.',
    ),
    t(
      'フルハーネスのフックは、腰{こし}より高{たか}い位置{いち}の強固{きょうこ}な構造物{こうぞうぶつ}に確実{かくじつ}に掛{か}ける。',
      'Hook the full harness lanyard securely to a sturdy structure higher than waist level.',
    ),
    t(
      '研磨{けんま}や切断{せつだん}など火花{ひばな}や粉{ふん}じんが飛散{ひさん}する作業{さぎょう}では、必{かなら}ず保護{ほご}メガネを着用{ちゃくよう}し、「少{すこ}しだけ」でも外{はず}さない。',
      'Always wear safety glasses during grinding or cutting; never remove them even for a second.',
    ),
    t(
      '有害{ゆうがい}な粉{ふん}じんには防{ぼう}じんマスク、有機溶剤{ゆうきようざい}やガスには防毒{ぼうどく}マスクを正{ただ}しく選{えら}んで使{つか}い分{わ}ける。',
      'Use a dust mask for hazardous dust, and a gas mask for organic solvents or toxic vapors.',
    ),
    t(
      'マスクの装着後{そうちゃくご}は、息{いき}を吸{す}い込{こ}んで顔{かお}に密着{みっちゃく}しているか（フィットテスト）を必{かなら}ず確認{かくにん}する。',
      'After putting on a mask, inhale to perform a seal check and confirm it fits tight to your face.',
    ),
    t(
      '足元{あしもと}への物{もの}の落下{らっか}や踏{ふ}み抜{ぬ}きから守{まも}るため、先芯{さきしん}入{い}りの安全靴{あんぜんぐつ}を正{ただ}しく履{は}き、かかとを踏{ふ}まない。',
      'Wear steel-toe safety shoes properly to protect feet from falling objects, and never crush the heels.',
    ),
  ],
  reel: [
    t(
      'ヘルメット、あご紐{ひも}締{し}まり、ヨシ！',
      'Helmet, chin strap tightened, yoshi!',
    ),
    t(
      '保護{ほご}メガネ、密着{みっちゃく}・傷{きず}なし、ヨシ！',
      'Safety glasses, snug fit and no scratches, yoshi!',
    ),
    t(
      '回転{かいてん}機械{きかい}、手袋{てぶくろ}なし・素手{すで}、ヨシ！',
      'Rotating machine, no gloves and bare hands, yoshi!',
    ),
    t(
      '防{ぼう}じんマスク、密着{みっちゃく}確認{かくにん}、ヨシ！',
      'Dust mask, seal check confirmed, yoshi!',
    ),
    t(
      '安全靴{あんぜんぐつ}、先芯{さきしん}・紐{ひも}締{し}め、ヨシ！',
      'Safety shoes, toe cap intact and laces tied, yoshi!',
    ),
    t(
      'フルハーネス、腿{もも}ベルト緩{ゆる}みなし、ヨシ！',
      'Full harness, thigh straps secure without slack, yoshi!',
    ),
    t(
      'ランヤード、フック掛{か}け位置{いち}・腰{こし}上{うえ}、ヨシ！',
      'Lanyard, hook attached above waist, yoshi!',
    ),
  ],
  drills: [
    {
      id: 'ppe-hazard-spot',
      kind: 'spot',
      secs: 30,
      need: 4,
      name: t(
        '保護具{ほごぐ}の不安全{ふあんぜん}チェック',
        'PPE unsafe practices check',
      ),
      goal: t(
        '30秒{びょう}で保護具{ほごぐ}に関{かん}する危険{きけん}な状態{じょうたい}を4つ見{み}つける。',
        'Find 4 dangerous PPE hazards in 30 seconds.',
      ),
      items: [
        {
          hazard: true,
          label: t(
            'ヘルメットのあご紐{ひも}を締{し}めずに作業{さぎょう}している',
            'Working without fastening helmet chin strap',
          ),
        },
        {
          hazard: false,
          label: t(
            '安全靴{あんぜんぐつ}の紐{ひも}をしっかり結{むす}んでいる',
            'Laces of safety shoes tied firmly',
          ),
        },
        {
          hazard: true,
          label: t(
            'ボール盤{ばん}作業{さぎょう}で軍手{ぐんて}を着用{ちゃくよう}している',
            'Wearing cotton work gloves while operating a drilling machine',
          ),
        },
        {
          hazard: false,
          label: t(
            '研磨{けんま}作業{さぎょう}で保護{ほご}メガネを着用{ちゃくよう}している',
            'Wearing safety glasses during grinding work',
          ),
        },
        {
          hazard: true,
          label: t(
            '「少{すこ}しだけ」と保護{ほご}メガネを外{はず}してグラインダーを使{つか}う',
            'Removing safety glasses "just for a minute" while using grinder',
          ),
        },
        {
          hazard: false,
          label: t(
            '高所{こうしょ}作業{さぎょう}でフルハーネスのフックを腰{こし}より上{うえ}に掛{か}けている',
            'Hooking full harness lanyard above waist during high work',
          ),
        },
        {
          hazard: true,
          label: t(
            '強{つよ}い衝撃{しょうげき}を受{う}けて亀裂{きれつ}が入{はい}ったヘルメットをそのまま使{つか}う',
            'Continuing to use a cracked helmet that suffered strong impact',
          ),
        },
        {
          hazard: false,
          label: t(
            '防{ぼう}じんマスクを装着{そうちゃく}したあと顔{かお}への密着{みっちゃく}テストをする',
            'Performing face seal check after putting on dust mask',
          ),
        },
        {
          hazard: true,
          label: t(
            '安全靴{あんぜんぐつ}のかかとを踏{ふ}みつぶして歩{ある}いている',
            'Walking with heels of safety shoes crushed down',
          ),
        },
      ],
    },
    {
      id: 'full-harness-seq',
      kind: 'seq',
      name: t(
        'フルハーネスの正{ただ}しい装着{そうちゃく}手順{てじゅん}',
        'Correct sequence for putting on a full harness',
      ),
      goal: t(
        '墜落制止用器具{ついらくせいしようきぐ}を安全{あんぜん}に装着{そうちゃく}する5つの手順{てじゅん}を並{なら}べる。',
        'Arrange the 5 steps for safely wearing fall prevention equipment.',
      ),
      steps: [
        t(
          '1. ハーネス本体{ほんたい}とランヤードの傷{きず}や縫製{ほうせい}のほつれを点検{てんけん}する',
          '1. Inspect harness webbing, lanyard, and stitching for damage',
        ),
        t(
          '2. ベルトのねじれがないことを確認{かくにん}して肩{かた}ベルトに腕{うで}を通{とお}す',
          '2. Confirm straps are untwisted and pass arms through shoulder straps',
        ),
        t(
          '3. 腿{もも}ベルトのバックルを結合{けつごう}し、手のひらが入る程度{ていど}に調節{ちょうせつ}する',
          '3. Fasten thigh buckles and adjust so one palm can slide beneath',
        ),
        t(
          '4. 胸{むね}ベルトのバックルを留{と}めて、背中{せなか}のD環{かん}が肩甲骨{けんこうこつ}の間{あいだ}に来{く}るように締{し}める',
          '4. Fasten chest buckle and position rear D-ring between shoulder blades',
        ),
        t(
          '5. 余{あま}ったベルト端{たん}を留{と}め具{ぐ}に収{おさ}め、全体{ぜんたい}のフィット感{かん}を確認{かくにん}する',
          '5. Tuck loose strap ends into keepers and confirm overall snug fit',
        ),
      ],
    },
    {
      id: 'mask-fit-seq',
      kind: 'seq',
      name: t(
        '取替式{とりかえしき}防{ぼう}じんマスクの装着{そうちゃく}とフィットチェック',
        'Replaceable dust mask fitting and seal check',
      ),
      goal: t(
        '防{ぼう}じんマスクを正{ただ}しく装着{そうちゃく}して密着{みっちゃく}を確認{かくにん}する4つのステップを並{なら}べる。',
        'Arrange the 4 steps to put on a dust mask and verify its airtight seal.',
      ),
      steps: [
        t(
          '1. フィルターに破{やぶ}れや目詰{めづ}まりがないか点検{てんけん}し、面体{めんたい}を鼻{はな}と口{くち}に当{あ}てる',
          '1. Inspect filter for tears or clogging, then place facepiece over nose and mouth',
        ),
        t(
          '2. 上{うえ}の固定{こてい}紐{ひも}を頭頂部{とうちょうぶ}に掛{か}け、下{した}の紐{ひも}を首{くび}の後{うし}ろで留{と}める',
          '2. Hook upper strap over crown of head and fasten lower strap behind neck',
        ),
        t(
          '3. 左右{さゆう}の紐{ひも}を均等{きんとう}に引{ひ}き、面体{めんたい}が顔{かお}にすき間{ま}なく密着{みっちゃく}するよう締{し}める',
          '3. Pull both straps evenly so facepiece contacts skin with zero gaps',
        ),
        t(
          '4. 吸気口{きゅうきこう}を手{て}で塞{ふさ}いで息{いき}を吸{す}い、面体{めんたい}が顔{かお}に吸{す}い付{つ}くか（陰圧{いんあつ}確認{かくにん}）を確{たし}かめる',
          '4. Cover air inlet with hand, inhale gently, and check if mask collapses against face',
        ),
      ],
    },
  ],
  inspect: [
    {
      id: 'helmet-preuse-inspect',
      defective: true,
      name: t(
        '保護帽{ほごぼう}（ヘルメット）の外観{がいかん}・内装{ないそう}点検{てんけん}',
        'Protective helmet exterior and harness inspection',
      ),
      spots: [
        {
          x: 50,
          y: 25,
          check: t(
            '帽体{ぼうたい}のひび割{わ}れ：衝撃{しょうげき}による亀裂{きれつ}や変形{へんけい}がないか（良品{りょうひん}：亀裂{きれつ}なし / 不良{ふりょう}：頭頂部{とうちょうぶ}に深{ふか}いクラック・割{わ}れあり）',
            'Shell cracks: Any cracks or deformation from impact (Good: No cracks / Defect: Deep crack on shell crown)',
          ),
        },
        {
          x: 50,
          y: 55,
          check: t(
            '衝撃吸収{しょうげききゅうしゅう}ライナー：発泡{はっぽう}スチロールの割{わ}れや脱落{だつらく}がないか（良品{りょうひん}：密着{みっちゃく}固定{こてい} / 不良{ふりょう}：欠損{けっそん}・へこみ）',
            'Shock-absorbing liner: Any crack or missing foam (Good: Firmly seated / Defect: Missing pieces or dents)',
          ),
        },
        {
          x: 50,
          y: 80,
          check: t(
            'あご紐{ひも}・耳紐{みみひも}：ほつれやバックルの破損{はそん}がないか（良品{りょうひん}：しっかり結合{けつごう} / 不良{ふりょう}：バックル爪{つめ}割{わ}れ）',
            'Chin/ear straps: Any fraying or buckle damage (Good: Clips securely / Defect: Broken buckle prongs)',
          ),
        },
      ],
    },
    {
      id: 'full-harness-inspect',
      defective: false,
      name: t(
        'フルハーネス型{がた}墜落制止用器具{ついらくせいしようきぐ}の始業前{しぎょうまえ}点検{てんけん}',
        'Full harness pre-use daily inspection',
      ),
      spots: [
        {
          x: 50,
          y: 30,
          check: t(
            '背中{せなか}D環{かん}：変形{へんけい}や亀裂{きれつ}、サビがないか（良品{りょうひん}：無傷{むきず}・円滑{えんかつ} / 不良{ふりょう}：変形{へんけい}・歪{ゆが}み）',
            'Rear D-ring: Any deformation, crack, or rust (Good: Undamaged and smooth / Defect: Bent or deformed)',
          ),
        },
        {
          x: 75,
          y: 50,
          check: t(
            'ショックアブソーバ：落下{らっか}衝撃{しょうげき}で破断{はだん}・伸{の}びていないか（良品{りょうひん}：未作動{みさどう}・カバー完全{かんぜん} / 不良{ふりょう}：展開{てんかい}済{ず}み・破{やぶ}れ）',
            'Shock absorber: Has it deployed from a past fall (Good: Undeployed and sealed / Defect: Unfolded or torn)',
          ),
        },
        {
          x: 30,
          y: 70,
          check: t(
            'ベルト縫製部{ほうせいぶ}：糸{いと}のほつれや切断{せつだん}、溶融{ようゆう}がないか（良品{りょうひん}：縫{ぬ}い目{め}正常{せいじょう} / 不良{ふりょう}：糸{いと}切{き}れ・焦{こ}げ）',
            'Strap stitching: Any frayed, cut, or heat-melted threads (Good: Intact stitching / Defect: Broken seams or burns)',
          ),
        },
      ],
    },
    {
      id: 'safety-shoes-inspect',
      defective: true,
      name: t(
        '安全靴{あんぜんぐつ}の摩耗{まもう}・破損{はそん}点検{てんけん}',
        'Safety shoes wear and damage inspection',
      ),
      spots: [
        {
          x: 75,
          y: 40,
          check: t(
            'つま先{さき}先芯{さきしん}部{ぶ}：皮{かわ}の破{やぶ}れで金属{きんぞく}芯{しん}が露出{ろしゅつ}していないか（良品{りょうひん}：覆{おお}い正常{せいじょう} / 不良{ふりょう}：芯{しん}露出{ろしゅつ}・陥没{かんぼつ}）',
            'Toe cap: Steel toe exposed by torn leather (Good: Fully covered / Defect: Metal exposed or crushed)',
          ),
        },
        {
          x: 45,
          y: 75,
          check: t(
            '靴底{くつぞこ}（ソール）：溝{みぞ}のすり減{へ}りや釘{くぎ}の刺{さ}さりがないか（良品{りょうひん}：溝{みぞ}あり滑{すべ}り止{ど}め有効{ゆうこう} / 不良{ふりょう}：つるつる・異物{いぶつ}貫通{かんつう}）',
            'Tread sole: Worn flat or pierced by nails (Good: Deep anti-slip tread / Defect: Tread worn smooth or pierced)',
          ),
        },
      ],
    },
  ],
  quiz: [
    {
      q: t(
        'ボール盤{ばん}（穴{あな}あけ機{き}）で鉄板{てっぱん}を加工{かこう}するとき、手{て}の保護{ほご}について正{ただ}しい行動{こうどう}はどれですか？',
        'When processing steel plates with a drilling machine, what is the correct action regarding hand protection?',
      ),
      options: [
        t(
          '手袋{てぶくろ}を着用{ちゃくよう}せず、素手{すで}で作業{さぎょう}する',
          'Do not wear gloves; work with bare hands',
        ),
        t(
          '滑{すべ}り止{ど}めゴムが付{つ}いた手袋{てぶくろ}を着用{ちゃくよう}する',
          'Wear gloves with rubber grip coating',
        ),
        t(
          '厚手{あつで}の軍手{ぐんて}を2枚{まい}重{かさ}ねて着用{ちゃくよう}する',
          'Wear two layers of thick cotton work gloves',
        ),
        t(
          '指先{ゆびさき}をカットした革{かわ}手袋{てぶくろ}を着用{ちゃくよう}する',
          'Wear fingerless leather gloves',
        ),
      ],
      answer: 0,
      why: t(
        'ボール盤{ばん}や旋盤{せんばん}などの回転{かいてん}機械{きかい}では、手袋{てぶくろ}が巻{ま}き込{こ}まれて指{ゆび}や手{て}を巻き込む大事故{だいじこ}になるため、手袋{てぶくろ}の着用{ちゃくよう}は厳禁{げんきん}です。',
        'Rotating machinery such as drill presses can catch gloves and pull hands into catastrophic entanglements; wearing gloves is strictly prohibited.',
      ),
    },
    {
      q: t(
        'ヘルメット（保護帽{ほごぼう}）のあご紐{ひも}の締{し}め方{かた}として、最{もっと}も適切{てきせつ}なものはどれですか？',
        'What is the most appropriate way to fasten a helmet chin strap?',
      ),
      options: [
        t(
          '窮屈{きゅうくつ}なので指{ゆび}が3本{さんぼん}入{はい}るくらい緩{ゆる}く締{し}める',
          'Keep it loose enough for 3 fingers to avoid feeling constricted',
        ),
        t(
          '頭{あたま}を振{ふ}ってもズレず、あごと紐{ひも}の間{あいだ}に指{ゆび}が1本{いっぽん}入{はい}る程度{ていど}に締{し}める',
          'Fasten securely so it does not shift when shaking head, with space for one finger',
        ),
        t(
          '後頭部{こうとうぶ}に回{まわ}してあごには掛{か}けない',
          'Loop it behind the back of the head instead of under the chin',
        ),
        t(
          '作業中{さぎょうちゅう}に汗{あせ}を拭{ふ}くためバックルを外{はず}しておく',
          'Leave the buckle unfastened to easily wipe sweat during work',
        ),
      ],
      answer: 1,
      why: t(
        'あご紐{ひも}は転倒{てんとう}や墜落{ついらく}時{じ}にヘルメットが脱落{だつらく}するのを防{ふせ}ぐため、指{ゆび}1本{いっぽん}分{ぶん}の隙間{すきま}を目安{めやす}に確実{かくじつ}に締{し}めます。',
        'The chin strap prevents the helmet from falling off during a stumble or fall; fasten it securely leaving about one finger width.',
      ),
    },
    {
      q: t(
        'サンダー（ディスクグラインダー）で鋼板{こうはん}のバリ取{と}りを作業{さぎょう}しているとき、保護{ほご}メガネが少{すこ}し曇{くも}ってきました。どうすべきですか？',
        'When deburring steel plate with a disc grinder, your safety glasses fog up. What should you do?',
      ),
      options: [
        t(
          '片手{かたて}でグラインダーを持{も}ちながら、もう一方{いっぽう}の手{て}でメガネを外{はず}す',
          'Hold grinder with one hand and remove glasses with the other',
        ),
        t(
          '視界{しかい}が悪{わる}いので、メガネをおでこに上{あ}げてそのまま作業{さぎょう}を続{つづ}ける',
          'Push glasses up to forehead and keep grinding because visibility is poor',
        ),
        t(
          '一度{いちど}グラインダーを完全{かんぜん}に停止{ていし}させ、火花{ひばな}が出{で}ない安全{あんぜん}な場所{ばしょ}で拭{ふ}く',
          'Stop grinder completely and wipe glasses in a safe area away from sparks',
        ),
        t(
          'メガネを足元{あしもと}に置{お}き、目{め}を細{ほそ}めて作業{さぎょう}を急{いそ}いで終{お}わらせる',
          'Place glasses on floor, squint eyes, and quickly finish the job',
        ),
      ],
      answer: 2,
      why: t(
        '作業中{さぎょうちゅう}に保護{ほご}メガネを外{はず}すと飛散{ひさん}した切粉{きりこ}や火花{ひばな}が目{め}に入{はい}るため、必{かなら}ず機械{きかい}を止{と}めてから安全{あんぜん}な場所{ばしょ}で手入{てい}れします。',
        'Removing safety glasses while working exposes eyes to flying grit and sparks; always stop the machine first before cleaning in safety.',
      ),
    },
    {
      q: t(
        '過去{かこ}に足場{あしば}から落{お}としたことのあるヘルメットについて、正{ただ}しい取扱{とりあつか}いはどれですか？',
        'What is the correct handling of a helmet that previously fell from scaffolding?',
      ),
      options: [
        t(
          '表面{ひょうめん}の汚れを拭{ふ}き取{と}ればそのまま再使用{さいしよう}してよい',
          'It can be reused normally once surface dirt is wiped off',
        ),
        t(
          '軽{かる}く叩{たた}いて音{おと}に異常{いじょう}がなければ使用{しよう}を続{つづ}ける',
          'Tap it lightly and continue using if the sound is normal',
        ),
        t(
          '油性{ゆせい}マジックで名前{なまえ}を書{か}き直{なお}して使い続ける',
          'Rewrite name on it with oil-based marker and continue using',
        ),
        t(
          '目{め}に見{み}える亀裂{きれつ}がなくても衝撃{しょうげき}吸収{きゅうしゅう}性能{せいのう}が低下{ていか}しているため廃棄{はいき}・交換{こうかん}する',
          'Discard and replace it because impact performance degrades even without visible cracks',
        ),
      ],
      answer: 3,
      why: t(
        '一度{いちど}でも強{つよ}い衝撃{しょうげき}を受{う}けた保護帽{ほごぼう}は、外見{がいけん}に異常{いじょう}がなくても内部{ないぶ}や構造{こうぞう}が劣化{れっか}しているため廃棄{はいき}します。',
        'A helmet that has received a heavy impact must be discarded, as internal structural integrity is compromised even without visible defects.',
      ),
    },
    {
      q: t(
        '高所{こうしょ}作業{さぎょう}でフルハーネス型{がた}墜落制止用器具{ついらくせいしようきぐ}のランヤード（フック）を掛{か}ける位置{いち}として正{ただ}しいものはどれですか？',
        'What is the correct attachment position for a full harness lanyard hook during work at heights?',
      ),
      options: [
        t(
          '腰{こし}の高さより上{うえ}にある強固{きょうこ}な鉄骨{てっこつ}などの構造物{こうぞうぶつ}',
          'A sturdy structural member such as a steel beam above waist level',
        ),
        t(
          '足元{あしもと}に転{ころ}がっている移動式{いどうしき}の台車{だいしゃ}',
          'A mobile cart lying around at your feet',
        ),
        t(
          '細{ほそ}い塩ビパイプや配線{はいせん}ケーブル',
          'A thin PVC pipe or electrical conduit',
        ),
        t(
          '作業員{さぎょういん}同士{どうし}のハーネスのD環{かん}',
          'The D-ring of a fellow worker’s harness',
        ),
      ],
      answer: 0,
      why: t(
        'フックは落下{らっか}距離{きょり}と衝撃{しょうげき}を最小限{さいしょうげん}にするため、腰{こし}より高{たか}い位置{いち}の十分{じゅうぶん}な強度{きょうど}がある構造物{こうぞうぶつ}に掛{か}けます。',
        'To minimize fall distance and shock load, attach the hook to an adequately strong structure positioned higher than your waist.',
      ),
    },
    {
      q: t(
        '塗装{とそう}作業{さぎょう}でシンナー（有機溶剤{ゆうきようざい}）を使用{しよう}します。着用{ちゃくよう}すべき適切{てきせつ}な呼吸用{こきゅうよう}保護具{ほごぐ}はどれですか？',
        'When using thinner (organic solvent) during painting work, what is the appropriate respiratory PPE to wear?',
      ),
      options: [
        t(
          '一般{いっぱん}の不織布{ふしょくふ}マスク（風邪用{かぜよう}）',
          'General non-woven hygiene mask (for colds)',
        ),
        t(
          '有機溶剤用{ゆうきようざいよう}の吸収缶{きゅうしゅうかん}を装着{そうちゃく}した防毒{ぼうどく}マスク',
          'Gas mask equipped with an organic vapor chemical canister',
        ),
        t(
          'ろ過材{かざい}だけが付{つ}いた防{ぼう}じんマスク',
          'Dust mask equipped only with a particulate filter',
        ),
        t(
          'タオルを口{くち}と鼻{はな}に巻{ま}きつけたもの',
          'A towel wrapped around the mouth and nose',
        ),
      ],
      answer: 1,
      why: t(
        '有機溶剤{ゆうきようざい}のガス・蒸気{じょうき}は防{ぼう}じんマスクでは除去{じょきょ}できません。専用{せんよう}の吸収缶{きゅうしゅうかん}を取{と}り付{つ}けた防毒{ぼうどく}マスクが必須{ひっす}です。',
        'Organic solvent vapors cannot be filtered by dust masks; a dedicated gas mask with appropriate chemical canisters is mandatory.',
      ),
    },
    {
      q: t(
        'ヘルメットに表示{ひょうじ}されている「労・検」ラベルに「飛来{ひらい}・落下物用{らっかぶつよう}」とだけ書{か}かれていました。このヘルメットを高所{こうしょ}作業{さぎょう}に使{つか}えますか？',
        'The certification label on a helmet shows only "For flying/falling objects". Can this helmet be used for work at heights?',
      ),
      options: [
        t(
          '軽{かる}くて動{うご}きやすいので高所{こうしょ}作業{さぎょう}に最適{さいてき}である',
          'It is lightweight and easy to move in, so it is ideal for high work',
        ),
        t(
          'あご紐{ひも}を2重{じゅう}に締{し}めれば高所{こうしょ}でも使用{しよう}できる',
          'It can be used at heights if you double-tighten the chin strap',
        ),
        t(
          '「墜落時保護用{ついらくじほごよう}」（衝撃吸収{しょうげききゅうしゅう}ライナー入{い}り）の表示{ひょうじ}がないため高所{こうしょ}作業{さぎょう}には使{つか}えない',
          'It cannot be used for high work because it lacks "Protection during falls" (no shock-absorbing liner)',
        ),
        t(
          'Foremanの許可{きょか}があればどの作業{さぎょう}でも自由{じゆう}に使{つか}える',
          'It can be used freely for any work as long as the foreman approves',
        ),
      ],
      answer: 2,
      why: t(
        '高所{こうしょ}作業{さぎょう}では万一{まんいち}の転落{てんらく}時{じ}に頭{あたま}を守{まも}るため、「墜落時保護用{ついらくじほごよう}」の合格{ごうかく}標章{ひょうしょう}（発泡{はっぽう}ライナー付き）のヘルメットが義務{ぎむ}付{づ}けられています。',
        'For work at heights, helmets certified for "Protection during falls" with shock-absorbing foam liners are legally required to guard the head.',
      ),
    },
    {
      q: t(
        '安全靴{あんぜんぐつ}を履{は}くときの正{ただ}しい注意点{ちゅういてん}として適切{てきせつ}なものはどれですか？',
        'What is the correct precaution when wearing safety shoes?',
      ),
      options: [
        t(
          '脱{ぬ}ぎ履{は}きしやすいように、かかとを踏{ふ}んでスリッパのように履{は}く',
          'Crush down the heel like a slipper for easy slipping on and off',
        ),
        t(
          '作業中{さぎょうちゅう}に足{あし}が痛{いた}くならないよう、2サイズ大{おお}きめの靴{くつ}を履{は}く',
          'Wear shoes 2 sizes larger so feet do not ache during long work',
        ),
        t(
          'つま先{さき}の先芯{さきしん}（鉄芯{てつしん}）が重{おも}いので取{と}り外{はず}して履{は}く',
          'Remove the steel toe cap because it feels too heavy',
        ),
        t(
          '足{あし}のサイズに合{あ}った靴{くつ}を選{えら}び、紐{ひも}や面ファスナーを締{し}めてかかとを踏{ふ}まない',
          'Choose shoes fitting your size, secure laces/velcro, and never crush down the heels',
        ),
      ],
      answer: 3,
      why: t(
        'かかとを踏{ふ}んで履{は}くと脱{ぬ}げやすく転倒{てんとう}の原因{げんいん}になり、先芯{さきしん}による保護{ほご}や滑{すべ}り止{ど}め機能{きのう}も正常{せいじょう}に働{はたら}きません。',
        'Crushing the heels causes tripping hazards, and prevents safety toes and anti-slip soles from functioning properly.',
      ),
    },
    {
      q: t(
        '造船所{ぞうせんじょ}の船体内{せんたいない}など、強{つよ}い騒音{そうおん}（85dB以上{いじょう}など）が発生{はっせい}する作業場{さぎょうば}での行動{こうどう}として正{ただ}しいものはどれですか？',
        'What is the correct action in high-noise workplaces (such as inside a shipyard ship hull exceeding 85dB)?',
      ),
      options: [
        t(
          '耳栓{みみせん}やイヤーマフなどの遮音{しゃおん}保護具{ほごぐ}を正{ただ}しく装着{そうちゃく}する',
          'Wear hearing protection such as earplugs or earmuffs correctly',
        ),
        t(
          '声{こえ}が聞{き}こえにくくなるので耳栓{みみせん}は一切{いっさい}使{つか}わない',
          'Do not use earplugs at all because it makes voices hard to hear',
        ),
        t(
          '耳{みみ}にティッシュペーパーを詰{つ}めて代用{だいよう}する',
          'Stuff tissue paper into the ears as a substitute',
        ),
        t(
          '耳{みみ}が痛{いた}くなってきた時{とき}だけ一時的{いちじてき}に耳{みみ}をふさぐ',
          'Cover ears with hands only temporarily when ears start hurting',
        ),
      ],
      answer: 0,
      why: t(
        '騒音{そうおん}による難聴{なんちょう}は自覚{じかく}のないまま徐々{じょじょ}に進行{しんこう}し回復{かいふく}しないため、騒音{そうおん}職場{しょくば}では耳栓{みみせん}・イヤーマフの着用{ちゃくよう}が必須{ひっす}です。',
        'Noise-induced hearing loss progresses unnoticed and is irreversible; wearing earplugs or earmuffs in noisy areas is essential.',
      ),
    },
    {
      q: t(
        '保護具{ほごぐ}の着用{ちゃくよう}ルールについて、「自分{じぶん}は作業{さぎょう}に慣{な}れているから保護具{ほごぐ}を外{はず}してもよいか」という質問{しつもん}に対{たい}する正{ただ}しい答{こた}えはどれですか？',
        'Regarding PPE rules, what is the correct answer to the question "Can I remove PPE because I am experienced at this job"?',
      ),
      options: [
        t(
          'ベテランであれば自分{じぶん}の判断{はんだん}で外{はず}してよい',
          'Veteran workers may remove it based on their own personal judgment',
        ),
        t(
          '経験{けいけん}年数{ねんすう}に関係{かんけい}なく、現場{げんば}の全員{ぜんいん}が保護具{ほごぐ}の着用{ちゃくよう}ルールを守{まも}らなければならない',
          'Regardless of experience, everyone on site must obey PPE regulations without exception',
        ),
        t(
          '作業{さぎょう}スピードが速{はや}くなるなら外{はず}した方{ほう}がよい',
          'It is better to remove it if doing so speeds up the work',
        ),
        t(
          '午前中{ごぜんちゅう}だけ着用{ちゃくよう}し、午後{ごご}は外{はず}して作業{さぎょう}する',
          'Wear it only in the morning and work without it in the afternoon',
        ),
      ],
      answer: 1,
      why: t(
        '保護具{ほごぐ}は熟練者{じゅくれんしゃ}であっても予期{よき}せぬ不測{ふそく}の事故{じこ}から命{いのち}を守{まも}る最後の砦{とりで}であり、例外{れいがい}なく着用{ちゃくよう}が義務{ぎむ}付{づ}けられています。',
        'PPE is the last line of defense protecting life even for skilled workers; compliance is strictly mandatory for everyone without exception.',
      ),
    },
    {
      q: t(
        'FRP（繊維強化プラスチック）製{せい}のヘルメットの使用{しよう}期間{きかん}の目安{めやす}として一般的に推奨{すいしょう}されている年数{ねんすう}はどれですか？',
        'What is the generally recommended service life for an FRP (fiberglass reinforced plastic) helmet?',
      ),
      options: [
        t(
          '一度{いちど}購入{こうにゅう}したら定年{ていねん}まで何十年{なんじゅうねん}でも使{つか}える',
          'Once purchased, it can be used for decades until retirement',
        ),
        t(
          '1か月{げつ}ごとに必{かなら}ず新品{しんぴん}に交換{こうかん}する',
          'Must be replaced with a brand new one every single month',
        ),
        t(
          '使用開始{しようかいし}から約{やく}5年{ねん}（熱可塑性樹脂製{ねつかそせいじゅしせい}は約{やく}3年{ねん}）を目安{めやす}に定期交換{ていきこうかん}する', // verify: FRP製保護帽は使用開始から約5年（熱可塑性樹脂製は約3年）を目安に交換
          'Replace periodically after roughly 5 years (roughly 3 years for thermoplastics)',
        ),
        t(
          '外側{そとがわ}に好{す}きなシールを貼{は}れば使用{しよう}期限{きげん}はなくなる',
          'Putting favorite stickers on the outside removes any expiration date',
        ),
      ],
      answer: 2,
      why: t(
        'ヘルメットは紫外線{しがいせん}や経年{けいねん}劣化{れっか}で樹脂{じゅし}強度が低下{ていか}するため、外見{がいけん}に傷{きず}がなくてもFRP製{せい}は約{やく}5年{ねん}を目安{めやす}に交換{こうかん}します。',
        'Because resin strength degrades over time from UV light and aging, FRP helmets should be replaced after about 5 years even without visible flaws.',
      ),
    },
    {
      q: t(
        '防{ぼう}じんマスクを装着{そうちゃく}した際{さい}に行{おこな}う「フィットテスト（密着性{みっちゃくせい}の確認{かくにん}）」について、正{ただ}しい方法{ほうほう}はどれですか？',
        'What is the correct procedure for the seal check (fit check) performed after putting on a dust mask?',
      ),
      options: [
        t(
          'マスクを横{よこ}に引{ひ}っ張{ぱ}ってゴムが伸{の}びるか確認{かくにん}する',
          'Pull the mask sideways to check if the elastic strap stretches',
        ),
        t(
          'マスクの内側{うちがわ}に水{みず}を入{い}れて漏{も}れがないか確{たし}かめる',
          'Pour water inside the mask to check for leaks',
        ),
        t(
          '職長{しょくちょう}に遠{とお}くから見{み}てもらって合図{あいず}をもらう',
          'Have the foreman look from a distance and give a signal',
        ),
        t(
          '吸気口{きゅうきこう}を手{て}で塞{ふさ}いで息{いき}を吸{す}い込{こ}み、面体{めんたい}が顔{かお}に密着{みっちゃく}して空気{くうき}が漏{も}れないか確認{かくにん}する',
          'Cover air inlet with hand, inhale gently, and check that mask draws tight against face with no leaks',
        ),
      ],
      answer: 3,
      why: t(
        '防{ぼう}じんマスクと顔{かお}の間{あいだ}にすき間{ま}があると粉{ふん}じんを直接{ちょくせつ}吸{す}い込{こ}んでしまうため、吸気口{きゅうきこう}を押{お}さえて息{いき}を吸{す}い陰圧{いんあつ}で密着{みっちゃく}を確認{かくにん}します。',
        'If gaps exist between the mask and face, harmful dust bypasses the filter; checking negative pressure confirms an airtight seal.',
      ),
    },
  ],
  glossary: [
    {
      ja: '保護具',
      reading: 'ほごぐ',
      en: 'Personal protective equipment (PPE)',
      note: t(
        '作業中の危険や有害物から作業者の体を守る器具の総称。',
        'General term for equipment protecting workers from hazards and toxic substances.',
      ),
    },
    {
      ja: '保護帽',
      reading: 'ほごぼう',
      en: 'Safety helmet / hard hat',
      note: t(
        '落下物や墜落の衝撃から頭部を保護するヘルメット。',
        'Helmet protecting the head from falling objects and fall impacts.',
      ),
    },
    {
      ja: 'あご紐',
      reading: 'あごひも',
      en: 'Chin strap',
      note: t(
        '衝撃や強風でヘルメットが脱落しないよう固定するストラップ。',
        'Strap securing the helmet so it does not dislodge from impact or wind.',
      ),
    },
    {
      ja: '安全靴',
      reading: 'あんぜんぐつ',
      en: 'Safety shoes',
      note: t(
        '重物の落下から足の指を守る先芯が入った作業靴。',
        'Work footwear with protective toe caps guarding against falling heavy objects.',
      ),
    },
    {
      ja: '先芯',
      reading: 'さきしん',
      en: 'Protective toe cap (steel/resin)',
      note: t(
        '安全靴のつま先に内蔵されている金属製または樹脂製の防護部材。',
        'Metal or resin protective cup built into the toe box of safety shoes.',
      ),
    },
    {
      ja: '保護メガネ',
      reading: 'ほごめがね',
      en: 'Safety glasses / protective goggles',
      note: t(
        '飛散する火花や切粉、薬品から目を守る専用メガネ。',
        'Dedicated eyewear protecting eyes from flying sparks, metal chips, or chemicals.',
      ),
    },
    {
      ja: '防じんマスク',
      reading: 'ぼうじんますく',
      en: 'Dust mask / particulate respirator',
      note: t(
        '研磨や溶接で発生する粉じんやヒュームをろ過する呼吸用保護具。',
        'Respiratory PPE that filters dust and fumes generated during grinding and welding.',
      ),
    },
    {
      ja: '防毒マスク',
      reading: 'ぼうどくますく',
      en: 'Gas mask / chemical respirator',
      note: t(
        '有機溶剤や有害ガスの蒸気を吸収缶で無毒化する呼吸用保護具。',
        'Respiratory PPE using chemical canisters to neutralize toxic vapors and solvents.',
      ),
    },
    {
      ja: '墜落制止用器具',
      reading: 'ついらくせいしようきぐ',
      en: 'Fall arrest equipment (full harness)',
      note: t(
        '高所からの墜落を防ぎ衝撃を分散する器具。旧名称は安全帯。',
        'Equipment preventing falls and dispersing fall impact; formerly known as safety belt.',
      ),
    },
    {
      ja: 'フルハーネス',
      reading: 'ふるはーねす',
      en: 'Full harness',
      note: t(
        '肩・胸・腿のベルトで体全体を支え、墜落時の荷重を分散する保護具。',
        'Harness supporting the whole body across shoulders, chest, and thighs.',
      ),
    },
    {
      ja: '耳栓',
      reading: 'みみせん',
      en: 'Earplugs',
      note: t(
        '造船所などの強烈な騒音から聴覚を守る遮音保護具。',
        'Hearing protection inserted into ears to guard against intense noise in shipyards.',
      ),
    },
    {
      ja: '巻き込まれ',
      reading: 'まきこまれ',
      en: 'Entanglement hazard',
      note: t(
        'ボール盤や旋盤などの回転部分に手袋や衣服が巻き込まれる事故。',
        'Accident where gloves or clothing get caught and dragged into rotating machine parts.',
      ),
    },
  ],
}
