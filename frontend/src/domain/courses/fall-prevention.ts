import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string): Tri => ({ ja, en })

export const fallPrevention: CourseData = {
  id: 'fall-prevention',
  category: 'trade',
  level: 2,
  minutes: 20,
  title: t(
    '高所{こうしょ}作業{さぎょう}・墜落{ついらく}防止{ぼうし}',
    'Working at height & fall prevention',
  ),
  sub: t(
    '2m以上{いじょう}の危険{きけん}を知{し}り、手{て}すり・足場{あしば}・フルハーネスで命{いのち}を守{まも}る。',
    'Understand the dangers of 2m+ heights and protect your life with guardrails, scaffolding, and full harness.',
  ),
  refs: [
    '労働安全衛生規則 第518条・第519条（作業床の設置等及び開口部等の囲い等）',
    '労働安全衛生規則 第564条・第567条（足場の組立て等及び点検）',
    '労働安全衛生法 第59条第3項・安衛則第36条第41号（フルハーネス型特別教育）', // verify: 労働安全衛生規則第36条第41号：高さ2m以上で作業床の端等においてフルハーネス型を使用する業務に従事する労働者には特別教育が必要
    '厚生労働省 墜落制止用器具の安全な使用に関するガイドライン',
  ],
  panels: [
    {
      who: 'foreman',
      text: t(
        '今日{きょう}は造船所{ぞうせんじょ}のドックで、高{たか}さ4メートルの足場{あしば}に上{のぼ}って作業{さぎょう}を行{おこな}います。',
        'Today in the shipyard dock, we will climb 4-meter-high scaffolding to work.',
      ),
    },
    {
      who: 'worker',
      text: t(
        'はい！急{いそ}いで資材{しざい}を両手{りょうて}に持{も}って、そのまま足場{あしば}を登{のぼ}ってきます！',
        'Yes! I will quickly grab materials in both hands and climb right up!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '待{ま}ちなさい！両手{りょうて}がふさがっていると危険{きけん}です。三点支持{さんてんしじ}で登{のぼ}り、荷物{にもつ}はロープで揚{あ}げます。',
        'Wait! It is dangerous if both hands are occupied. Climb using 3-point contact and hoist materials with a rope.',
      ),
    },
    {
      who: 'worker',
      text: t(
        'わかりました。フルハーネスも装着{そうちゃく}しました！手{て}すりがある場所{ばしょ}なら、フックは掛{か}けなくても大丈夫{だいじょうぶ}ですか？',
        'Understood. I also put on my full harness! If there are guardrails, is it okay not to hook the lanyard?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'いいえ、手{て}すりがあってもフックは必{かなら}ず腰{こし}より高{たか}い位置{いち}に掛{か}けます。油断{ゆだん}が一番{いちばん}の事故{じこ}の原因{げんいん}です。',
        'No, even with guardrails, always attach the hook above waist level. Carelessness is the top cause of accidents.',
      ),
    },
    {
      who: 'worker',
      text: t(
        'フックを掛{か}けました！作業{さぎょう}がしにくいので、邪魔{じゃま}な手{て}すりと中{なか}さんを一時的{いちじてき}に外{はず}してもいいですか？',
        'Hook attached! It is hard to reach, so may I temporarily remove the guardrail and midrail here?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '手{て}すりを勝手{かって}に外{はず}すのは絶対{ぜったい}に禁止{きんし}です！外{はず}した場所{ばしょ}が開口部{かいこうぶ}になり、過去{かこ}に多{おお}くの墜落{ついらく}が起{お}きています。',
        'Removing guardrails on your own is strictly prohibited! That removed section becomes an opening where many fatal falls have happened.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '危{あぶ}ないところでした…！手{て}すりは外{はず}さず、持{も}ち込{こ}むレンチにも落下{らっか}防止{ぼうし}コードを着{つ}けます。',
        'That was close...! I will never remove guardrails, and I will attach drop-prevention cords to my wrenches too.',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '良{よ}い判断{はんだん}です。工具{こうぐ}が落{お}ちると、下{した}で作業{さぎょう}している仲間{なかま}の命{いのち}に関{かか}わります。',
        'Good judgment. If a tool falls, it endangers the lives of teammates working below.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '足場{あしば}よし、フックよし、工具{こうぐ}よし！指差呼称{しさこしょう}で確認{かくにん}して作業{さぎょう}に入{はい}ります！',
        'Scaffold yoshi, hook yoshi, tools yoshi! I will confirm everything with pointing and calling before starting work!',
      ),
    },
  ],
  handbook: [
    t(
      '高{たか}さ2m以上{いじょう}の作業床{さぎょうゆか}の端{はし}や開口部{かいこうぶ}で手{て}すりがない場所{ばしょ}では、必{かなら}ずフルハーネス型{がた}墜落制止用器具{ついらくせいしようきぐ}を使用{しよう}する。', // verify: 労働安全衛生規則第518条：高さ2m以上の箇所で作業床を設けることが困難なときは墜落制止用器具（フルハーネス型）を使用
      'Always use a full-harness fall arrest system at heights of 2m or more where there are no guardrails.',
    ),
    t(
      'フルハーネスのランヤード（フック）は、墜落時{ついらくじ}の自由落下{じゆうらっか}距離{きょり}を短{みじか}くするため、必{かなら}ず腰{こし}より高{たか}い位置{いち}の頑丈{がんじょう}な構造物{こうぞうぶつ}や親綱{おやづな}に掛{か}ける。',
      'Always hook the full harness lanyard to a sturdy structure or lifeline higher than waist level to minimize free-fall distance.',
    ),
    t(
      '足場{あしば}の手{て}すり・中{なか}さん・幅木{はばき}・壁当{かべあ}てなどを、自分{じぶん}の判断{はんだん}で勝手{かって}に取{と}り外{はず}してはならない。',
      'Never remove scaffolding handrails, midrails, toe boards, or wall ties on your own judgment.',
    ),
    t(
      '床{ゆか}の開口部{かいこうぶ}（穴{あな}）には必{かなら}ず固定{こてい}されたフタと「開口部{かいこうぶ}注意{ちゅうい}」の標識{ひょうしき}を設置{せっち}し、フタをずらしたまま放置{ほうち}してはならない。',
      'Securely cover floor openings with signs saying "Warning: Floor Opening", and never leave covers shifted or open.',
    ),
    t(
      'はしごの立{た}て掛{か}け角度{かくど}は約{やく}75度{ど}とし、上端{じょうたん}は踏{ふ}み出{だ}し面{めん}から60cm以上{いじょう}突{つ}き出{だ}して上部{じょうぶ}を縛{しば}って固定{こてい}する。', // verify: 厚生労働省「はしご・脚立の安全使用」：立て掛け角度は約75度、上端は踏み出し面から60cm以上突出させる
      'Set ladder angle to about 75°, extend top 60cm or more above landing, and tie the top securely.',
    ),
    t(
      'はしごや階段{かいだん}の昇降{しょうこう}では、両手{りょうて}と片足{かたあし}または両足{りょうあし}と片手{かたて}の三点支持{さんてんしじ}を徹底{てってい}し、手荷物{てにもつ}を持{も}って登{のぼ}ってはならない。',
      'Maintain 3-point contact when climbing ladders or stairs, and never climb while carrying items in your hands.',
    ),
    t(
      '脚立{きゃたつ}の天板{てんばん}（最上段{さいじょうだん}）に乗{の}って作業{さぎょう}したり、天板{てんばん}を跨{また}いで座{すわ}って作業{さぎょう}してはならない。', // verify: 脚立の天板に乗っての作業や跨がっての作業の禁止（厚生労働省通達・安全基準）
      'Never stand on the top plate of a stepladder or straddle it to work.',
    ),
    t(
      '高所{こうしょ}作業{さぎょう}で使用{しよう}するすべての手工具{てこうぐ}には、落下{らっか}防止{ぼうし}コードを取{と}り付{つ}けて手首{てくび}や腰{こし}ベルトに連結{れんけつ}する。', // verify: 足場からの物体の飛来・落下防止措置（幅木15cm以上、メッシュシート、工具セーフティロープ）
      'Attach drop-prevention lanyards to all hand tools used at heights and connect them to your wrist or tool belt.',
    ),
    t(
      '強風{きょうふう}（10分間{じゅっぷんかん}の平均風速{へいきんふうそく}10m/s以上{いじょう}）や大雨{おおあめ}・大雪{おおゆき}など悪天候{あくてんこう}のときは、直{ただ}ちに高所{こうしょ}作業{さぎょう}を中止{ちゅうし}する。', // verify: 労働安全衛生規則第522条：10分間の平均風速が10m/s以上、または大雨・大雪等の悪天候時は高所・足場作業を中止
      'Immediately stop working at height during severe weather like strong winds (10m/s+ average over 10 min), heavy rain, or snow.',
    ),
    t(
      '高{たか}さ2m以上{いじょう}で作業床{さぎょうゆか}の設置{せっち}が困難{こんなん}な箇所{かしょ}でフルハーネスを使用{しよう}する作業者{さぎょうしゃ}は、特別教育{とくべつきょういく}を修了{しゅうりょう}していなければならない。', // verify: 労働安全衛生規則第36条第41号：高さ2m以上で作業床の端等においてフルハーネス型を使用する業務に従事する労働者には特別教育が必要
      'Workers using a full harness at heights of 2m or more without work platforms must complete designated special training.',
    ),
  ],
  reel: [
    t(
      'フルハーネス、腿{もも}ベルト・胸{むね}ベルト締{し}まり、ヨシ！',
      'Full harness, thigh and chest straps secured, yoshi!',
    ),
    t(
      'ランヤード、フック腰{こし}上{うえ}・二重{にじゅう}ロック、ヨシ！',
      'Lanyard, hook attached above waist and double locked, yoshi!',
    ),
    t(
      '足場{あしば}手{て}すり・中{なか}さん、固定{こてい}・ガタなし、ヨシ！',
      'Scaffold guardrail and midrail, securely fixed without wobble, yoshi!',
    ),
    t(
      '床{ゆか}の開口部{かいこうぶ}、フタ固定{こてい}・隙間{すきま}なし、ヨシ！',
      'Floor opening, cover secured with no gaps, yoshi!',
    ),
    t(
      'はしご角度{かくど}75度{ど}、上端{じょうたん}突出{とっしゅつ}・上部{じょうぶ}固定{こてい}、ヨシ！',
      'Ladder angle 75°, top extends and secured, yoshi!',
    ),
    t(
      '持{も}ち歩{ある}き工具{こうぐ}、落下{らっか}防止{ぼうし}コード接続{せつぞく}、ヨシ！',
      'Carried tools, drop-prevention cords connected, yoshi!',
    ),
    t(
      '作業床{さぎょうゆか}、油{あぶ}ら・つまずき物{もの}なし、ヨシ！',
      'Work platform, no oil or trip hazards, yoshi!',
    ),
  ],
  drills: [
    {
      id: 'fall-hazard-spot',
      kind: 'spot',
      secs: 30,
      need: 4,
      name: t(
        '高所{こうしょ}作業{さぎょう}の危険{きけん}予知{よち}',
        'Spotting fall hazards at height',
      ),
      goal: t(
        '30秒{びょう}で高所{こうしょ}の危険{きけん}な状態{じょうたい}を4つ見{み}つける。',
        'Find 4 dangerous fall hazards in 30 seconds.',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '足場{あしば}の手{て}すりと中{なか}さんが外{はず}されて開口部{かいこうぶ}になっている',
            'Scaffold guardrail and midrail removed leaving an opening',
          ),
        },
        {
          hazard: false,
          label: t(
            '作業床{さぎょうゆか}の端{はし}に高{たか}さ15cm以上{いじょう}の幅木{はばき}がすき間{ま}なく付{つ}いている',
            'Toe boards 15cm+ high installed along platform edge without gaps',
          ),
        },
        {
          hazard: true,
          label: t(
            'フルハーネスのフックを作業床{さぎょうゆか}の足元{あしもと}（くるぶしの高{たか}さ）に掛{か}けている',
            'Full harness hook attached at foot level near ankles',
          ),
        },
        {
          hazard: false,
          label: t(
            '高所{こうしょ}作業者{さぎょうしゃ}がフックを腰{こし}より高{たか}い頑丈{がんじょう}な親綱{おやづな}に掛{か}けている',
            'Worker hooked harness lanyard to sturdy lifeline above waist',
          ),
        },
        {
          hazard: true,
          label: t(
            '床{ゆか}の開口部{かいこうぶ}にフタも囲{かこ}いもなく穴{あな}が開{あ}いたまま放置{ほうち}されている',
            'Floor opening left uncovered with no barrier or warning',
          ),
        },
        {
          hazard: false,
          label: t(
            '開口部{かいこうぶ}に固定{こてい}フタと「開口部{かいこうぶ}注意{ちゅうい}」の表示{ひょうじ}板{ばん}がある',
            'Floor opening has fixed cover and "Caution: Opening" sign',
          ),
        },
        {
          hazard: true,
          label: t(
            '脚立{きゃたつ}の天板{てんばん}（最上段{さいじょうだん}）に乗{の}って背伸{せの}びしながら天井{てんじょう}作業{さぎょう}をしている',
            'Standing on stepladder top plate and stretching for ceiling work',
          ),
        },
        {
          hazard: false,
          label: t(
            '持{も}ち歩{ある}くスパナとラチェットに落下{らっか}防止{ぼうし}コードを装着{そうちゃく}している',
            'Carried wrenches and ratchets have drop-prevention lanyards attached',
          ),
        },
        {
          hazard: true,
          label: t(
            '足場{あしば}の作業床{さぎょうゆか}の端{はし}に電動{でんどう}工具{こうぐ}がコードなしで直置{じかお}きされている',
            'Power tool placed directly at platform edge without drop lanyard',
          ),
        },
      ],
    },
    {
      id: 'scaffold-climb-seq',
      kind: 'seq',
      name: t(
        '足場{あしば}作業{さぎょう}の開始{かいし}手順{てじゅん}',
        'Scaffold work starting sequence',
      ),
      goal: t(
        '足場{あしば}へ登{のぼ}って安全{あんぜん}に作業{さぎょう}を始{はじ}める5つの手順{てじゅん}を並{なら}べる。',
        'Arrange the 5 steps to climb scaffolding and start work safely.',
      ),
      steps: [
        t(
          '1. 始業前{しぎょうまえ}に足場{あしば}の手{て}すり・作業床{さぎょうゆか}・筋交{すじか}いのガタつきを点検{てんけん}する',
          '1. Inspect scaffold guardrails, platform, and braces for looseness before work',
        ),
        t(
          '2. ヘルメットのあご紐{ひも}とフルハーネスの各{かく}ベルトを確実{かくじつ}に締{し}める',
          '2. Fasten helmet chin strap and tighten all harness straps securely',
        ),
        t(
          '3. 使用{しよう}する工具{こうぐ}に落下{らっか}防止{ぼうし}コードを着{つ}けて腰袋{こしぶくろ}に収{おさ}め、両手{りょうて}を空{あ}ける',
          '3. Attach drop lanyards to tools, place in pouch, and keep both hands free',
        ),
        t(
          '4. 昇降{しょうこう}階段{かいだん}または昇降{しょうこう}はしごを手{て}すりを握{にぎ}って三点支持{さんてんしじ}で登{のぼ}る',
          '4. Climb access stairs or ladder holding handrail with 3-point contact',
        ),
        t(
          '5. 作業床{さぎょうゆか}に上{あ}がったら、直{ただ}ちにフックを腰{こし}より高{たか}い親綱{おやづな}や構造物{こうぞうぶつ}に掛{か}ける',
          '5. Immediately attach hook to lifeline or structure above waist once on platform',
        ),
      ],
    },
    {
      id: 'ladder-setup-seq',
      kind: 'seq',
      name: t(
        'はしごの安全{あんぜん}な設置{せっち}と昇降{しょうこう}',
        'Safe ladder setup and climbing sequence',
      ),
      goal: t(
        'はしごを安全{あんぜん}に設置{せっち}して登{のぼ}る4つのステップを並{なら}べる。',
        'Arrange the 4 steps to safely set up and climb a ladder.',
      ),
      steps: [
        t(
          '1. はしごの支柱{しちゅう}や踏{ふ}み桟{ざん}に亀裂{きれつ}・変形{へんけい}がないか外観{がいかん}点検{てんけん}する',
          '1. Inspect ladder stiles and rungs for cracks or deformation',
        ),
        t(
          '2. 平坦{へいたん}で滑{すべ}らない地面{じめん}を選{えら}び、立{た}て掛{か}け角度{かくど}を約{やく}75度{ど}にする',
          '2. Choose flat, non-slip ground and set leaning angle to about 75 degrees',
        ),
        t(
          '3. はしごの上端{じょうたん}を踏{ふ}み出{だ}し床{ゆか}から60cm以上{いじょう}突出{とっしゅつ}させ、上部{じょうぶ}をロープで固定{こてい}する',
          '3. Extend top 60cm+ above landing and tie the top with rope',
        ),
        t(
          '4. 両手{りょうて}と片足{かたあし}の三点支持{さんてんしじ}を守{まも}り、手荷物{てにもつ}を持{も}たずに昇降{しょうこう}する',
          '4. Maintain 3-point contact and climb without carrying items in hand',
        ),
      ],
    },
  ],
  inspect: [
    {
      id: 'scaffold-inspect',
      defective: true,
      name: t(
        '枠組{わくぐみ}足場{あしば}の作業床{さぎょうゆか}と手{て}すり点検{てんけん}',
        'Scaffold platform and guardrail inspection',
      ),
      spots: [
        {
          x: 25,
          y: 30,
          check: t(
            '手{て}すり・中{なか}さん：固定{こてい}金具{かなぐ}が締{し}まり脱落{だつらく}がないか（良品{りょうひん}：グラつきなく強固{きょうこ}に固定{こてい} / 不良{ふりょう}：中{なか}さんが外{はず}れて欠落{けつらく}・開口{かいこう}状態{じょうたい}）',
            'Guardrail and midrail: Clamps tight and none missing (Good: Solid with no wobble / Defect: Midrail detached and missing, forming opening)',
          ),
        },
        {
          x: 50,
          y: 65,
          check: t(
            '作業床{さぎょうゆか}・幅木{はばき}：床板{ゆかいた}の隙間{すきま}が3cm以下{いか}で幅木{はばき}があるか（良品{りょうひん}：隙間{すきま}なく幅木{はばき}高{たか}さ15cm以上{いじょう} / 不良{ふりょう}：床板{ゆかいた}に大{おお}きな隙間{すきま}あり）',
            'Work platform and toe boards: Gaps under 3cm and toe boards present (Good: No gaps and 15cm+ toe boards / Defect: Large gaps between planks)',
          ),
        },
        {
          x: 75,
          y: 80,
          check: t(
            '筋交{すじか}い（ブレース）：ロックピンが確実{かくじつ}に掛{か}かっているか（良品{りょうひん}：全{すべ}てのブレースが正常{せいじょう}ロック / 不良{ふりょう}：ピンが抜{ぬ}けてブレースが外{はず}れている）',
            'Cross-braces: Lock pins fully engaged (Good: All braces locked normally / Defect: Lock pin missing and brace detached)',
          ),
        },
      ],
    },
    {
      id: 'harness-lanyard-inspect',
      defective: false,
      name: t(
        'フルハーネス・ランヤードの始業前{しぎょうまえ}点検{てんけん}',
        'Full harness & lanyard pre-use inspection',
      ),
      spots: [
        {
          x: 50,
          y: 25,
          check: t(
            'フックの外れ止{ど}め装置{そうち}：バネが正常{せいじょう}に働{はたら}き二重{にじゅう}ロックされるか（良品{りょうひん}：確実{かくじつ}に閉{と}じて手{て}で開{あ}かない / 不良{ふりょう}：バネ破損{はそん}・閉{と}じ不良{ふりょう}）',
            'Hook safety latch: Spring works and locks securely (Good: Snaps shut firmly and will not push open / Defect: Broken spring or sticky gate)',
          ),
        },
        {
          x: 50,
          y: 55,
          check: t(
            'ショックアブソーバ：過去{かこ}の落下{らっか}による伸{の}びや破断{はだん}がないか（良品{りょうひん}：未作動{みさどう}で外装{がいそう}カバー無傷{むきず} / 不良{ふりょう}：破断{はだん}・縫製{ほうせい}が解{ほど}けている）',
            'Shock absorber: Any deployment or tear from previous falls (Good: Undeployed with intact protective pack / Defect: Torn open or stitches unraveling)',
          ),
        },
        {
          x: 30,
          y: 75,
          check: t(
            'ランヤードのベルト（織{お}りロープ）：切断{せつだん}や熱{ねつ}による溶融{ようゆう}、摩耗{まもう}がないか（良品{りょうひん}：傷{きず}やほつれなし / 不良{ふりょう}：溶融{ようゆう}硬化{こうか}や深{ふか}い傷{きず}あり）',
            'Lanyard webbing: Any cuts, friction burns, or severe wear (Good: Free of cuts, burns, or fraying / Defect: Heat-melted spots or deep fraying)',
          ),
        },
      ],
    },
    {
      id: 'stepladder-inspect',
      defective: true,
      name: t(
        '作業用{さぎょうよう}脚立{きゃたつ}の安全{あんぜん}点検{てんけん}',
        'Work stepladder safety inspection',
      ),
      spots: [
        {
          x: 50,
          y: 30,
          check: t(
            '天板{てんばん}・踏{ふ}み桟{ざん}：リベットの緩{ゆる}みや踏{ふ}み板{いた}の曲{ま}がり・亀裂{きれつ}がないか（良品{りょうひん}：歪{ゆが}みなく頑丈{がんじょう} / 不良{ふりょう}：踏{ふ}み桟{ざん}に変形{へんけい}・リベット脱落{だつらく}）',
            'Top plate & rungs: Loose rivets or bent/cracked steps (Good: Solid and straight / Defect: Bent rung and loose rivets)',
          ),
        },
        {
          x: 70,
          y: 55,
          check: t(
            '開{ひら}き止{ど}め金具{かなぐ}：左右{さゆう}の金具{かなぐ}が完全{かんぜん}に突{つ}っ張{ぱ}ってロックされるか（良品{りょうひん}：水平{すいへい}に完全{かんぜん}ロック / 不良{ふりょう}：金具{かなぐ}が曲{ま}がりロック不能{ふのう}）',
            'Spreader brace: Both side braces fully locked in position (Good: Locked straight and level / Defect: Bent spreader failing to lock)',
          ),
        },
        {
          x: 35,
          y: 80,
          check: t(
            '脚端具{きゃくたんぐ}（滑{すべ}り止{ど}めゴム）：すり減{へ}りや脱落{だつらく}がないか（良品{りょうひん}：四脚{よんきゃく}すべてゴム健全{けんぜん} / 不良{ふりょう}：ゴムが脱落{だつらく}して金属{きんぞく}が露出{ろしゅつ}）',
            'Foot rubber pads: Worn out or missing anti-slip feet (Good: All four rubber feet intact / Defect: Foot pad missing and bare metal exposed)',
          ),
        },
      ],
    },
  ],
  quiz: [
    {
      q: t(
        '高{たか}さ3mの足場{あしば}で作業{さぎょう}をするとき、フルハーネスのランヤードのフックを掛{か}ける場所{ばしょ}として最も適切{てきせつ}なものはどれですか？',
        'When working on a 3m scaffold, which is the most appropriate location to hook the full harness lanyard?',
      ),
      options: [
        t(
          '腰{こし}の高さより上{うえ}にある親綱{おやづな}または頑丈{がんじょう}な足場{あしば}の構造部材{こうぞうぶざい}',
          'A lifeline or sturdy scaffolding structural member located above waist level',
        ),
        t(
          '足元{あしもと}の作業床{さぎょうゆか}に転{ころ}がっている細{ほそ}い単管{たんかん}パイプ',
          'A thin pipe rolling around on the floor platform at your feet',
        ),
        t(
          '隣{となり}で作業{さぎょう}している仲間{なかま}のハーネスのD環{かん}',
          'The D-ring of a fellow worker’s harness next to you',
        ),
        t(
          '足場{あしば}の外側{そとがわ}に張{は}ってあるメッシュシートの端{はし}',
          'The edge of a mesh dust sheet hanging on the outside of the scaffold',
        ),
      ],
      answer: 0,
      why: t(
        'フックは墜落時{ついらくじ}の落下{らっか}距離{きょり}を最小限{さいしょうげん}にするため、腰{こし}より高{たか}い頑丈{がんじょう}な親綱{おやづな}や構造物{こうぞうぶつ}に掛{か}けます。',
        'To minimize fall distance and shock load, attach the hook to an adequately strong lifeline or structure positioned higher than your waist.',
      ),
    },
    {
      q: t(
        '足場{あしば}で作業中{さぎょうちゅう}、資材{しざい}を通{とお}すために手{て}すりと中{なか}さんが邪魔{じゃま}になりました。どのように対応{たいおう}すべきですか？',
        'While working on scaffolding, the guardrail and midrail are in the way of passing materials. How should you respond?',
      ),
      options: [
        t(
          '作業{さぎょう}の効率{こうりつ}を優先{ゆうせん}し、自分{じぶん}の判断{はんだん}ですぐに手{て}すりを外{はず}す',
          'Prioritize efficiency and remove the handrail immediately on your own judgment',
        ),
        t(
          '勝手{かって}に外{はず}さず職長{しょくちょう}に相談{そうだん}し、安全帯{あんぜんたい}使用{しよう}など安全{あんぜん}対策{たいさく}の指示{しじ}を受{う}ける',
          'Do not remove it on your own; consult the foreman and follow instructions on safety measures like harness use',
        ),
        t(
          '手{て}すりのボルトを緩{ゆる}めて下{した}にぶら下{さ}げたまま作業{さぎょう}を続{つづ}ける',
          'Loosen the bolts and let the rail hang down while continuing to work',
        ),
        t(
          '後{あと}で戻{もど}せば良{よ}いので、仲間{なかま}に声{こえ}をかけて2人{ふたり}で外{はず}す',
          'Since you can put it back later, tell a teammate and remove it together',
        ),
      ],
      answer: 1,
      why: t(
        '足場{あしば}の手{て}すりを勝手{かって}に取{と}り外{はず}すことは禁止{きんし}されており、外{はず}す必要{ひつよう}がある場合{ばあい}は職長{しょくちょう}の許可{きょか}と代替{だいたい}の安全{あんぜん}措置{そち}が不可欠{ふかけつ}です。',
        'Removing scaffold handrails on your own is strictly prohibited; if removal is necessary, foreman approval and alternative safety measures are mandatory.',
      ),
    },
    {
      q: t(
        'はしごを使{つか}って高所{こうしょ}の足場{あしば}へ登{のぼ}るときの正{ただ}しい方法{ほうほう}はどれですか？',
        'What is the correct way to climb a ladder to reach scaffolding at height?',
      ),
      options: [
        t(
          '片手{かたて}に重{おも}い工具箱{こうぐばこ}を持{も}ち、もう一方{いっぽう}の手{て}だけではしごを握{にぎ}って急{いそ}いで登{のぼ}る',
          'Hold a heavy toolbox in one hand and hurry up holding the ladder with only the other hand',
        ),
        t(
          'はしごの立{た}て掛{か}け角度{かくど}を45度{ど}にして、緩{ゆる}やかな坂{さか}のように登{のぼ}る',
          'Set the ladder angle to 45 degrees and climb like a gentle slope',
        ),
        t(
          '手荷物{てにもつ}を持{も}たず、両手{りょうて}と片足{かたあし}など三点支持{さんてんしじ}を保{たも}ちながら一歩{いっぽ}ずつ昇降{しょうこう}する',
          'Carry no luggage in hands, and climb step by step maintaining 3-point contact with both hands and one foot',
        ),
        t(
          '背中{せなか}をはしごに向{む}けて、階段{かいだん}のように前{まえ}を向{む}いて降{お}りる',
          'Face away from the ladder and climb down facing forward like stairs',
        ),
      ],
      answer: 2,
      why: t(
        'はしご昇降{しょうこう}では両手{りょうて}を空{あ}け、常{つね}に手足{てあし}の3箇所以上{かしょいじょう}が接{せっ}する三点支持{さんてんしじ}を守{まも}ることでスリップや転落{てんらく}を防{ふせ}ぎます。',
        'Keep both hands free and maintain 3-point contact at all times when climbing ladders to prevent slips and falls.',
      ),
    },
    {
      q: t(
        '作業用{さぎょうよう}の脚立{きゃたつ}（開き止め付き）を使用{しよう}する際{さい}、禁止{きんし}されている行為{こうい}はどれですか？',
        'When using a work stepladder with spreaders, which action is prohibited?',
      ),
      options: [
        t(
          '平坦{へいたん}で凹凸{おうとつ}のない固{かた}い床面{ゆかめん}に設置{せっち}する',
          'Setting it on a flat, even, and firm floor surface',
        ),
        t(
          '左右{さゆう}の開{ひら}き止{ど}め金具{かなぐ}を完全{かんぜん}に突{つ}っ張{ぱ}らせてロックする',
          'Fully extending and locking the left and right spreader braces',
        ),
        t(
          '作業{さぎょう}前{まえ}に脚端具{きゃくたんぐ}の滑{すべ}り止{ど}めゴムに摩耗{まもう}がないか点検{てんけん}する',
          'Inspecting anti-slip rubber feet for wear before starting work',
        ),
        t(
          '天板{てんばん}（最上段{さいじょうだん}）の上{うえ}に両足{りょうあし}で立{た}って背伸{せの}びして天井{てんじょう}作業{さぎょう}をする',
          'Standing on the top plate with both feet and stretching to work on the ceiling',
        ),
      ],
      answer: 3,
      why: t(
        '脚立{きゃたつ}の天板{てんばん}に乗{の}る行為{こうい}はバランスを崩{くず}しやすく極{きわ}めて危険{きけん}なため、法令{ほうれい}および安全{あんぜん}基準{きじゅん}で禁止{きんし}されています。',
        'Standing on the top plate of a stepladder easily leads to loss of balance and is strictly prohibited by safety regulations.',
      ),
    },
    {
      q: t(
        '床{ゆか}の開口部{かいこうぶ}（配管用{はいかんよう}の穴{あな}など）を発見{はっけん}しました。最初{さいしょ}に行{おこな}うべき正{ただ}しい行動{こうどう}はどれですか？',
        'You discovered a floor opening such as a piping penetration hole. What is the correct first action?',
      ),
      options: [
        t(
          '周囲{しゅうい}に危険{きけん}を知{し}らせ、ずれたり外{はず}れたりしない強固{きょうこ}なフタと「開口部{かいこうぶ}注意{ちゅうい}」の表示{ひょうじ}を設{もう}ける',
          'Alert those around you, and set up a sturdy fixed cover that will not slide off along with a "Caution: Floor Opening" sign',
        ),
        t(
          '開口部{かいこうぶ}の上{うえ}に薄{うす}い段{だん}ボールを1枚{いちまい}だけ被{かぶ}せて見{み}えなくする',
          'Cover the opening with a single sheet of thin cardboard so it cannot be seen',
        ),
        t(
          '小{ちい}さな穴{あな}なので自分{じぶん}だけ跨{また}いで通{とお}り過{す}ぎ、誰{だれ}にも言{い}わずに放置{ほうち}する',
          'Since it is small, just step over it yourself and leave it without telling anyone',
        ),
        t(
          '開口部{かいこうぶ}の縁{ふち}に腰{こし}掛{か}けて休憩{きゅうけい}をとる',
          'Sit down on the rim of the opening to take a rest',
        ),
      ],
      answer: 0,
      why: t(
        '開口部{かいこうぶ}は見落{みお}としによる踏{ふ}み外{はず}しや墜落{ついらく}が多発{たはつ}するため、確実{かくじつ}に固定{こてい}できるフタと注意{ちゅうい}喚起{かんき}の標識{ひょうしき}が必須{ひっす}です。',
        'Floor openings cause numerous fall accidents when stepped into accidentally; securely fastened covers and clear warning signs are essential.',
      ),
    },
    {
      q: t(
        '高所{こうしょ}作業中{さぎょうちゅう}の工具{こうぐ}の落下{らっか}（飛来{ひらい}落下{らっか}）を防{ふせ}ぐための対策{たいさく}として正{ただ}しいものはどれですか？',
        'What is the correct measure to prevent tools from falling during work at heights?',
      ),
      options: [
        t(
          '工具{こうぐ}は使{つか}いやすいように作業床{さぎょうゆか}の端{はし}に並{なら}べて置{お}く',
          'Line up tools along the edge of the working platform for easy reach',
        ),
        t(
          'すべての工具{こうぐ}に落下{らっか}防止{ぼうし}コードを付{つ}け、不使用時{ふしようじ}は腰袋{こしぶくろ}に確実{かくじつ}に収{おさ}める',
          'Attach drop lanyards to all tools and keep them securely stored in tool pouches when not in use',
        ),
        t(
          'ポケットにたくさんのドライバーやボルトを突{つ}っ込{こ}んで登{のぼ}る',
          'Stuff many screwdrivers and bolts into your clothing pockets before climbing',
        ),
        t(
          '手{て}が滑{すべ}らないように工具{こうぐ}に油{あぶ}らを塗{ぬ}布{ふ}しておく',
          'Coat tools with oil so that hands do not slip',
        ),
      ],
      answer: 1,
      why: t(
        '高所{こうしょ}からの工具{こうぐ}落下{らっか}は直下{ちょっか}の作業員{さぎょういん}の重大{じゅうだい}な人身{じんしん}事故{じこ}につながるため、コード取{と}り付{つ}けと腰袋{こしぶくろ}への収納{しゅうのう}を徹底{てってい}します。',
        'Tools falling from height can cause fatal injuries to workers below; always attach safety lanyards and store tools in pouches.',
      ),
    },
    {
      q: t(
        '天候{てんこう}が急変{きゅうへん}し、強風{きょうふう}が吹{ふ}いてきました。足場{あしば}作業{さぎょう}を中止{ちゅうし}すべき法令{ほうれい}上{じょう}の基準{きじゅん}（悪天候{あくてんこう}）はどれですか？',
        'Weather changed suddenly and strong winds blew in. What is the legal threshold for stopping scaffolding work?',
      ),
      options: [
        t(
          '帽子{ぼうし}が飛{と}ばされそうな微風{びふう}のとき',
          'When there is a slight breeze that might blow off a cap',
        ),
        t(
          '雨{あめ}がポツポツと降{ふ}り始{はじ}めたが作業{さぎょう}に支障{ししょう}がないとき',
          'When a light drizzle starts falling but does not interfere with work',
        ),
        t(
          '10分間{じゅっぷんかん}の平均風速{へいきんふうそく}が10m/s以上{いじょう}、または大雨{おおあめ}・大雪{おおゆき}など危険{きけん}が予想{よそう}されるとき', // verify: 労働安全衛生規則第522条：10分間の平均風速が10m/s以上、または大雨・大雪等の悪天候時は高所・足場作業を中止
          'When 10-minute average wind speed is 10 m/s or more, or danger is anticipated from heavy rain or snow',
        ),
        t(
          '日暮{ひぐ}れになって気温{きおん}が下{さ}がり寒{さむ}くなってきたとき',
          'When evening arrives and the temperature drops and feels cold',
        ),
      ],
      answer: 2,
      why: t(
        '労働安全衛生規則{ろうどうあんぜんえいせいきそく}では、10分間{じゅっぷんかん}の平均風速{へいきんふうそく}が10m/s以上{いじょう}などの悪天候{あくてんこう}時{じ}には、高所{こうしょ}足場{あしば}作業{さぎょう}の中止{ちゅうし}が義務{ぎむ}付{づ}けられています。',
        'Under occupational safety rules, work on scaffolding at height must be stopped during severe weather such as 10-minute average winds of 10 m/s or more.',
      ),
    },
    {
      q: t(
        '日本{にほん}の労働安全衛生法{ろうどうあんぜんえいせいほう}において、作業床{さぎょうゆか}の設置{せっち}や墜落制止用器具{ついらくせいしようきぐ}の使用{しよう}が義務{ぎむ}づけられる基準{きじゅん}の高{たか}さはどれですか？',
        'Under Japan’s Industrial Safety and Health Act, what is the standard height at which work platforms or fall arrest systems are mandatory?',
      ),
      options: [
        t('高{たか}さ0.5m以上{いじょう}', '0.5 meters or more in height'),
        t('高{たか}さ1.0m以上{いじょう}', '1.0 meters or more in height'),
        t('高{たか}さ1.5m以上{いじょう}', '1.5 meters or more in height'),
        t(
          '高{たか}さ2m以上{いじょう}', // verify: 労働安全衛生規則第518条：高さ2m以上の箇所で作業床を設けることが困難なときは墜落制止用器具（フルハーネス型）を使用
          '2 meters or more in height',
        ),
      ],
      answer: 3,
      why: t(
        '労働安全衛生規則{ろうどうあんぜんえいせいきそく}第518条{だいごひゃくじゅうはちじょう}により、高{たか}さ2m以上{いじょう}の箇所{かしょ}で墜落{ついらく}の危険{きけん}があるときは作業床{さぎょうゆか}を設{もう}けることが義務{ぎむ}づけられています。',
        'Under Article 518 of the Ordinance on Industrial Safety and Health, employers must provide a work platform when working at heights of 2m or higher.',
      ),
    },
    {
      q: t(
        'フルハーネス型{がた}墜落制止用器具{ついらくせいしようきぐ}のショックアブソーバを点検{てんけん}したところ、カバーが破{やぶ}れ内部{ないぶ}の帯{おび}が引{ひ}き出{だ}されていました。どうすべきですか？',
        'When inspecting a full harness shock absorber, the cover was torn and internal webbing was pulled out. What should you do?',
      ),
      options: [
        t(
          '過去{かこ}に落下{らっか}衝撃{しょうげき}を受{う}けて作動{さどう}済{ず}みのため、絶対{ぜったい}に使用{しよう}せず直{ただ}ちに廃棄{はいき}・交換{こうかん}する',
          'Since it deployed from an impact load in a past fall, never use it and discard/replace it immediately',
        ),
        t(
          '伸{の}びた帯{おび}を元{もと}のように折{お}りたたんでビニールテープで巻{ま}き直{なお}して使{つか}う',
          'Fold back the stretched webbing and wrap with plastic tape to keep using it',
        ),
        t(
          '軽{かる}い作業{さぎょう}のときだけ使{つか}う予備{よび}として保管{ほかん}する',
          'Store it as a spare to use only during light-duty work',
        ),
        t(
          'ハーネス本体{ほんたい}に傷{きず}がなければ気にせずそのまま着用{ちゃくよう}する',
          'Put it on without worrying as long as the harness webbing itself is not damaged',
        ),
      ],
      answer: 0,
      why: t(
        '一度{いちど}でも衝撃{しょうげき}を吸収{きゅうしゅう}して作動{さどう}したショックアブソーバは再使用{さいしよう}できず、次{つぎ}の墜落{ついらく}で衝撃{しょうげき}を抑{おさ}えられないため廃棄{はいき}します。',
        'A shock absorber that deployed once to cushion a fall cannot be reused and must be discarded because it can no longer absorb impact.',
      ),
    },
    {
      q: t(
        '直立{ちょくりつ}したはしごを壁{かべ}に立{た}て掛{か}けて昇降{しょうこう}通路{つうろ}として設置{せっち}します。立{た}て掛{か}け角度{かくど}と上端{じょうたん}の突出{とっしゅつ}の基準{きじゅん}として正{ただ}しいものはどれですか？',
        'You set up a straight ladder leaning against a wall as an access route. Which is the correct standard for leaning angle and top extension?',
      ),
      options: [
        t(
          '角度{かくど}は約{やく}90度{ど}（垂直{すいちょく}）、上端{じょうたん}は壁{かべ}と同{おな}じ高{たか}さ',
          'Angle is about 90° (vertical), top flush with the top of wall',
        ),
        t(
          '角度{かくど}は約{やく}75度{ど}とし、上端{じょうたん}は踏{ふ}み出{だ}し床面{ゆかめん}より60cm以上{いじょう}突出{とっしゅつ}させる', // verify: 厚生労働省「はしご・脚立の安全使用」：立て掛け角度は約75度、上端は踏み出し面から60cm以上突出させる
          'Angle is about 75°, and top extends 60cm or more above the landing',
        ),
        t(
          '角度{かくど}は約{やく}30度{ど}とし、上端{じょうたん}は固定{こてい}せず置{お}くだけにする',
          'Angle is about 30°, and top is left resting without tying',
        ),
        t(
          '角度{かくど}は約{やく}60度{ど}とし、上端{じょうたん}は床面{ゆかめん}より10cm低{ひく}くする',
          'Angle is about 60°, and top is 10cm lower than the landing surface',
        ),
      ],
      answer: 1,
      why: t(
        'はしごは滑{すべ}りや転倒{てんとう}を防{ふせ}ぐため約{やく}75度{ど}に立{た}て掛{か}け、乗{の}り移{うつ}り時{じ}に手{て}で支{ささ}えられるよう上部{じょうぶ}を60cm以上{いじょう}突{つ}き出{だ}します。',
        'Ladders must be leaned at about 75° to prevent slipping and overturning, extending 60cm+ above the landing for safe handholds.',
      ),
    },
    {
      q: t(
        '高{たか}さ2m以上{いじょう}の足場{あしば}作業{さぎょう}で、フルハーネス型{がた}墜落制止用器具{ついらくせいしようきぐ}を着用{ちゃくよう}して作業{さぎょう}を行{おこな}う労働者{ろうどうしゃ}に義務{ぎむ}づけられていることはどれですか？',
        'What is legally required for workers who wear full-harness fall arrest systems to work on scaffolding at heights of 2m or more?',
      ),
      options: [
        t(
          '運転免許証{うんてんめんきょしょう}の携帯{けいたい}',
          'Carrying a driver’s license',
        ),
        t(
          '毎月{まいつき}の健康診断{けんこうしんだん}の受診{じゅしん}',
          'Undergoing a monthly health checkup',
        ),
        t(
          '法令{ほうれい}に基{づ}くフルハーネス型{がた}墜落制止用器具{ついらくせいしようきぐ}特別教育{とくべつきょういく}の修了{しゅうりょう}', // verify: 労働安全衛生規則第36条第41号：高さ2m以上で作業床の端等においてフルハーネス型を使用する業務に従事する労働者には特別教育が必要
          'Completion of special education on full-harness fall arrest systems pursuant to laws and regulations',
        ),
        t(
          '日本語能力試験{にほんごのうりょくしけん}N1の合格{ごうかく}',
          'Passing the Japanese Language Proficiency Test N1',
        ),
      ],
      answer: 2,
      why: t(
        '労働安全衛生法{ろうどうあんぜんえいせいほう}および規則{きそく}により、作業床{さぎょうゆか}の端{はし}などでフルハーネス型{がた}を使用{しよう}する業務{ぎょうむ}には特別教育{とくべつきょういく}の修了{しゅうりょう}が必須{ひっす}です。',
        'Labor safety regulations require workers using full harness equipment at platform edges to complete designated special training.',
      ),
    },
    {
      q: t(
        '足場{あしば}の作業床{さぎょうゆか}で作業中{さぎょうちゅう}に、物{もの}の転がり落{お}ちを防{ふせ}ぐ「幅木{はばき}」について正{ただ}しい説明{せつめい}はどれですか？',
        'What is the correct explanation of "toe boards" (habaki) used on scaffold platforms to prevent objects from rolling off?',
      ),
      options: [
        t(
          '作業者{さぎょうしゃ}が座{すわ}って休憩{きゅうけい}するためのベンチ板{いた}である',
          'A bench plank intended for workers to sit on and take a break',
        ),
        t(
          '足場{あしば}の強度{きょうど}を上{あ}げるための補強材{ほきょうざい}なので床{ゆか}の上{うえ}ならどこに置{お}いてもよい',
          'A reinforcing member to increase scaffold strength, so it can be placed anywhere on the deck',
        ),
        t(
          '邪魔{じゃま}になったらいつでも外{はず}して投{な}げ捨{す}ててよい',
          'It can be removed and tossed aside anytime it gets in the way',
        ),
        t(
          '床板{ゆかいた}の端{はし}に設置{せっち}し、足元{あしもと}の工具{こうぐ}やボルトが下{した}へ落下{らっか}するのを物理的{ぶつりてき}に防{ふせ}ぐ板{いた}である', // verify: 足場からの物体の飛来・落下防止措置（幅木15cm以上、メッシュシート、工具セーフティロープ）
          'A board installed along platform edges to physically prevent tools and bolts at feet from falling down',
        ),
      ],
      answer: 3,
      why: t(
        '幅木{はばき}は高{たか}さ15cm以上{いじょう}の板{いた}などを床{ゆか}の端{はし}に設{もう}け、工具{こうぐ}や資材{しざい}の飛来{ひらい}落下{らっか}から下{した}の作業者{さぎょうしゃ}を守{まも}る重要{じゅうよう}な設備{せつび}です。',
        'Toe boards of 15cm+ height installed at deck edges are critical safety components to guard workers below from falling objects.',
      ),
    },
  ],
  glossary: [
    {
      ja: '高所作業',
      reading: 'こうしょさぎょう',
      en: 'Work at height',
      note: t(
        '高さ2m以上で墜落の危険がある場所で行う作業。',
        'Work performed at heights of 2 meters or more with risk of falling.',
      ),
    },
    {
      ja: '墜落制止用器具',
      reading: 'ついらくせいしようきぐ',
      en: 'Fall arrest equipment',
      note: t(
        '高所からの墜落を防ぎ身体への衝撃を和らげる器具。旧名称は安全帯。',
        'Equipment preventing falls and cushioning impact; formerly called safety belt.',
      ),
    },
    {
      ja: 'フルハーネス',
      reading: 'ふるはーねす',
      en: 'Full-body harness',
      note: t(
        '肩、胸、腿のベルトで身体全体を支え、墜落時の荷重を分散する保護具。',
        'PPE supporting the whole body with shoulder, chest, and thigh straps.',
      ),
    },
    {
      ja: 'ランヤード',
      reading: 'らんやーど',
      en: 'Lanyard',
      note: t(
        'ハーネス本体と構造物をつなぐ命綱。フックとショックアブソーバを含む。',
        'Lifeline connecting harness to structure, including hook and shock absorber.',
      ),
    },
    {
      ja: '親綱',
      reading: 'おやづな',
      en: 'Lifeline (horizontal safety line)',
      note: t(
        'フックを掛ける設備がない場所で張るワイヤーロープや合成繊維ロープ。',
        'Horizontal rope or wire strung to attach lanyard hooks where no structure exists.',
      ),
    },
    {
      ja: '足場',
      reading: 'あしば',
      en: 'Scaffolding',
      note: t(
        '高所で作業するために組み立てられる仮設の作業床や骨組み。',
        'Temporary work platforms and framing erected for working at height.',
      ),
    },
    {
      ja: '作業床',
      reading: 'さぎょうゆか',
      en: 'Working platform / deck',
      note: t(
        '足場の上に板を隙間なく敷き詰めて人が乗って作業できるようにした床面。',
        'Planked floor surface on scaffolding installed without gaps for workers to stand on.',
      ),
    },
    {
      ja: '手すり',
      reading: 'てすり',
      en: 'Handrail / guardrail',
      note: t(
        '作業床の端からの転落や墜落を防ぐために設置する横木やパイプ。',
        'Horizontal bars or pipes installed along platform edges to prevent falls.',
      ),
    },
    {
      ja: '中さん',
      reading: 'なかさん',
      en: 'Midrail',
      note: t(
        '手すりと作業床の中間に設けられ、身体のすり抜け落下を防ぐ部材。',
        'Bar placed midway between handrail and deck to prevent slipping through.',
      ),
    },
    {
      ja: '幅木',
      reading: 'はばき',
      en: 'Toe board',
      note: t(
        '作業床の端に立てる高さ15cm以上の板。工具やボルトの落下を防ぐ。',
        'Vertical board 15cm+ high at platform edge preventing tools from rolling off.',
      ),
    },
    {
      ja: '開口部',
      reading: 'かいこうぶ',
      en: 'Floor opening / hole',
      note: t(
        '作業床や床面に開いた穴。転落防止のためフタや手すりによる囲いが必要。',
        'Hole in floor or deck; requires covers or guardrails to prevent falls.',
      ),
    },
    {
      ja: '脚立',
      reading: 'きゃたつ',
      en: 'Stepladder',
      note: t(
        '開いて自立する昇降・作業器具。天板に乗って作業することは禁止。',
        'Self-supporting portable ladder; standing on top plate is strictly prohibited.',
      ),
    },
    {
      ja: '三点支持',
      reading: 'さんてんしじ',
      en: 'Three-point contact',
      note: t(
        'はしごや階段で両手片足、または両足片手で常に3点を支えて昇降する基本。',
        'Rule of keeping 3 points of contact (two hands and one foot, or two feet and one hand).',
      ),
    },
  ],
}
