import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string): Tri => ({ ja, en })

export const lowBack: CourseData = {
  id: 'low-back',
  category: 'core',
  level: 1,
  minutes: 15,
  title: t('腰痛予防{ようつうよぼう}', 'Back pain prevention'),
  sub: t(
    '正{ただ}しい姿勢{しせい}で荷物{にもつ}を持{も}ち上{あ}げ、腰{こし}の負担{ふたん}を減{へ}らして怪我{けが}を防{ふせ}ぐ。',
    'Lift loads with correct posture, reduce strain on your lower back, and prevent injuries.',
  ),
  refs: [
    '厚生労働省 職場における腰痛予防対策指針',
    '労働安全衛生法 第59条（安全衛生教育）',
    '中央労働災害防止協会（中災防） 腰痛予防対策労働衛生教育',
  ],
  panels: [
    {
      who: 'foreman',
      text: t(
        '今日{きょう}は重{おも}い鋼板{こうはん}や部品{ぶひん}を運{はこ}びます。腰{こし}を痛{いた}めない運{はこ}び方{かた}を確認{かくにん}しましょう。',
        'Today we move heavy steel plates and parts. Let us check how to carry them without hurting your back.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '自分{じぶん}の腕{うで}で持{も}ち上{あ}がる重{おも}さなら、そのままぐっと持{も}ち上{あ}げて大丈夫{だいじょうぶ}ですか？',
        'If I can lift the weight with my arms, is it okay to just hoist it up directly?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'いいえ、膝{ひざ}を伸{の}ばしたまま腰{こし}だけで持{も}ち上{あ}げると、椎間板{ついかんばん}に強{つよ}い圧力{あつりょく}がかかり危険{きけん}です。',
        'No. If you lift with your back while keeping your knees straight, dangerous pressure acts on your spinal discs.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '急{いそ}いで運{はこ}ぼう！（※膝{ひざ}を伸{の}ばしたまま腰{こし}を大{おお}きく曲{ま}げて荷物{にもつ}を持{も}とうとする）',
        'I will hurry and move it! (*attempts to grab the load with straight legs, bending deeply from the waist*)',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '危{あぶ}ない、ストップ！まず足{あし}を肩幅{かたはば}に広{ひろ}げ、荷物{にもつ}に近寄{ちかよ}ってしっかり腰{こし}を落{お}とします。',
        'Dangerous, stop! First spread your feet shoulder-width, get close to the load, and lower your hips deeply.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '足{あし}を開{ひら}いて膝{ひざ}を曲{ま}げ、荷物{にもつ}をお腹{なか}にぴったり引{ひ}き寄{よ}せます。こうですか？',
        'Spread my feet, bend my knees, and pull the load tight against my belly. Like this?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'とても良{よ}いです。背筋{せすじ}をまっすぐ伸{の}ばし、脚{あし}の力{ちから}で立{た}ち上{あ}がります。身体{からだ}をねじってはいけません。',
        'Very good. Keep your back straight and stand up using the power of your legs. Never twist your torso.',
      ),
    },
    {
      who: 'worker',
      text: t(
        'なるほど！腕{うで}や腰{こし}ではなく、太{ふと}ももの筋肉{きんにく}を使{つか}うと腰{こし}が全然{ぜんぜん}痛{いた}くありません！',
        'I see! Using my thigh muscles instead of my arms or lower back makes it completely painless for my back!',
      ),
    },
    {
      who: 'foreman',
      // verify: 厚生労働省「職場における腰痛予防対策指針」では、男性の重量物取扱い上限の目安を体重の約40%、女性はその約60%としている
      text: t(
        '重{おも}い物{もの}は無理{むり}せず「二人{ふたり}で持{も}つ」か「台車{だいしゃ}」を使{つか}います。男性{だんせい}は体重{たいじゅう}の40％以下{いか}が目安{めやす}です。',
        'For heavy items do not force it; use two people or a cart. As a guideline, men should stay under 40% of body weight.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '了解{りょうかい}しました！腰{こし}に違和感{いわかん}やすじの痛{いた}みが出{で}たら我慢{がまん}せず、すぐに職長{しょくちょう}へ報告{ほうこく}します！',
        'Understood! If I feel any discomfort or strain in my back, I will not endure it alone and will report to you immediately!',
      ),
    },
  ],
  handbook: [
    t(
      '荷物{にもつ}を持{も}ち上{あ}げるときは、膝{ひざ}を曲{ま}げて腰{こし}を深{ふか}く落{お}とし、脚{あし}の力{ちから}で立{た}ち上{あ}がる。',
      'When lifting loads, bend your knees to lower your hips deeply and stand up using leg strength.',
    ),
    t(
      '膝{ひざ}を伸{の}ばしたまま腰{こし}だけを曲{ま}げて荷物{にもつ}を引{ひ}き上{あ}げる作業{さぎょう}は厳禁{げんきん}。',
      'Lifting a load by bending only at your waist while keeping knees straight is strictly forbidden.',
    ),
    t(
      '荷物{にもつ}はできる限{かぎ}り身体{からだ}やお腹{なか}に近{ちか}づけて抱{かか}え、腕{うで}だけで支{ささ}えない。',
      'Hold the load as close to your body and stomach as possible; do not support it with your arms alone.',
    ),
    t(
      '荷物{にもつ}を持{も}ったまま上半身{じょうはんしん}をねじらない。方向{ほうこう}を変{か}えるときは足{あし}の向{む}きを変{か}える。',
      'Do not twist your upper body while carrying a load. Turn by stepping and changing your foot direction.',
    ),
    // verify: 厚生労働省「職場における腰痛予防対策指針」では、男性の重量物取扱い上限の目安を体重の約40%、女性はその約60%としている
    t(
      '重{おも}い荷物{にもつ}（体重{たいじゅう}の40％超{ちょう}など）は一人{ひとり}で持{も}たず、2人{ふたり}で持{も}つか台車{だいしゃ}を使{つか}う。',
      'Do not carry heavy loads alone (e.g. over 40% body weight); lift with two people or use a cart.',
    ),
    t(
      '作業{さぎょう}前{まえ}や休憩{きゅうけい}時間{じかん}には、腰{こし}や脚{あし}の筋肉{きんにく}を伸{の}ばすストレッチを必{かなら}ず行{おこな}う。',
      'Always perform stretching exercises before work and during breaks to loosen back and leg muscles.',
    ),
    t(
      '長時間{ちょうじかん}の中腰{ちゅうごし}姿勢{しせい}を続{つづ}けず、合間{あいま}に上体{じょうたい}を反{そ}らすなど姿勢{しせい}を定期的{ていきてき}に変{か}える。',
      'Do not maintain a prolonged bent or stooped posture; change postures periodically by leaning back.',
    ),
    t(
      '荷物{にもつ}を置{お}くときも放{ほう}り投{な}げず、膝{ひざ}を曲{ま}げて腰{こし}を落{お}とし静{しず}かに下{お}ろす。',
      'When placing a load down, never drop it; bend your knees to lower your hips and set it down quietly.',
    ),
    t(
      '通路{つうろ}の段差{だんさ}や油{あぶら}・水濡{みずぬ}れを事前{じぜん}に確認{かくにん}し、滑{すべ}りや踏{ふ}み外{はず}しによる腰痛{ようつう}を防{ふせ}ぐ。',
      'Check walkways beforehand for steps, oil, and wet spots to prevent back injury from slipping or stumbling.',
    ),
    t(
      '腰{こし}に違和感{いわかん}やすじの痛{いた}みを感{かん}じたら作業{さぎょう}を止{と}め、我慢{がまん}せず職長{しょくちょう}へ報告{ほうこく}する。',
      'If you feel discomfort or acute strain in your back, stop working and report to the foreman without enduring it alone.',
    ),
  ],
  reel: [
    t(
      '荷物{にもつ}の重{おも}さ、確認{かくにん}、ヨシ！',
      'Load weight, verified, yoshi!',
    ),
    t(
      '足幅{あしはば}、肩幅{かたはば}、安定{あんてい}、ヨシ！',
      'Stance, shoulder-width, stable, yoshi!',
    ),
    t(
      '膝曲{ひざま}げ、腰{こし}落{お}とし、ヨシ！',
      'Knees bent, hips lowered, yoshi!',
    ),
    t(
      '荷物{にもつ}密着{みっちゃく}、引{ひ}き寄{よ}せ、ヨシ！',
      'Load held close to body, yoshi!',
    ),
    t(
      '背筋{せすじ}伸{の}ばし、ねじれなし、ヨシ！',
      'Back straight, no twisting, yoshi!',
    ),
    t(
      '二人{ふたり}持{も}ち、合図{あいず}「せーの」、ヨシ！',
      'Two-person lift, call signal "Ready, heve!", yoshi!',
    ),
    t(
      '運搬{うんぱん}通路{つうろ}、足元{あしもと}障害物{しょうがいぶつ}なし、ヨシ！',
      'Transport route, footway clear of obstacles, yoshi!',
    ),
  ],
  drills: [
    {
      id: 'low-back-lifting-hazards',
      kind: 'spot',
      secs: 30,
      need: 4,
      name: t(
        '重量物{じゅうりょうぶつ}取扱{とりあつか}いの危険{きけん}動作{どうさ}',
        'Hazardous actions in handling heavy loads',
      ),
      goal: t(
        '30秒{びょう}で腰{こし}を痛{いた}める不安全{ふあんぜん}な動作{どうさ}を4つ見{み}つける。',
        'Find 4 unsafe movements that injure the back in 30 seconds.',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '膝{ひざ}を伸{の}ばしたまま腰{こし}だけ曲{ま}げて持{も}ち上{あ}げる',
            'Lifting by bending only the waist with straight knees',
          ),
        },
        {
          hazard: false,
          label: t(
            '足幅{あしはば}を肩幅{かたはば}に広{ひろ}げて腰{こし}を深{ふか}く落{お}とす',
            'Spreading feet shoulder-width and lowering hips deeply',
          ),
        },
        {
          hazard: true,
          label: t(
            '荷物{にもつ}を身体{からだ}から遠{とお}く離{はな}して腕{うで}だけで支{ささ}える',
            'Holding the load far away from body with arms alone',
          ),
        },
        {
          hazard: false,
          label: t(
            '荷物{にもつ}をお腹{なか}にしっかり引{ひ}き寄{よ}せて密着{みっちゃく}させる',
            'Pulling load tightly against belly and holding close',
          ),
        },
        {
          hazard: true,
          label: t(
            '荷物{にもつ}を抱{かか}えたまま上体{じょうたい}を急{きゅう}にねじる',
            'Twisting upper body abruptly while holding the load',
          ),
        },
        {
          hazard: false,
          label: t(
            '向{む}きを変{か}えるときは足{あし}のステップを踏{ふ}み換{か}える',
            'Changing direction by shifting and stepping with feet',
          ),
        },
        {
          hazard: true,
          label: t(
            '30kgの重{おも}い鉄板{てっぱん}を一人{ひとり}で無理{むり}に持{も}ち上{あ}げる',
            'Forcing oneself to lift a heavy 30kg steel plate alone',
          ),
        },
        {
          hazard: false,
          label: t(
            '重{おも}い荷物{にもつ}は手押{てお}し台車{だいしゃ}に載{の}せて運{はこ}ぶ',
            'Transporting heavy cargo on a manual push cart',
          ),
        },
        {
          hazard: true,
          label: t(
            '前{まえ}が見{み}えない高{たか}さまで箱{はこ}を積{つ}み重{かさ}ねて運{はこ}ぶ',
            'Carrying boxes stacked so high that forward view is blocked',
          ),
        },
      ],
    },
    {
      id: 'safe-lift-seq',
      kind: 'seq',
      name: t(
        '床{ゆか}からの正{ただ}しい荷持{にも}ち上{あ}げ手順{てじゅん}',
        'Correct sequence for lifting a load from the floor',
      ),
      goal: t(
        '腰{こし}を痛{いた}めずに荷物{にもつ}を持{も}ち上{あ}げる4つの基本{きほん}手順{てじゅん}を正{ただ}しい順番{じゅんばん}に並{なら}べる。',
        'Arrange the 4 basic steps of lifting without back strain in the correct order.',
      ),
      steps: [
        t(
          '1. 荷物{にもつ}に近寄{ちかよ}り、足幅{あしはば}を肩幅{かたはば}程度{ていど}に広{ひろ}げて構{かま}える',
          '1. Approach the load and stand with feet about shoulder-width apart',
        ),
        t(
          '2. 膝{ひざ}を曲{ま}げて腰{こし}を落{お}とし、荷物{にもつ}をお腹{なか}に引{ひ}き寄{よ}せて抱{かか}える',
          '2. Bend knees to lower hips and pull the load tight against your belly',
        ),
        t(
          '3. 背筋{せすじ}を伸{の}ばしたまま、脚{あし}（太{ふと}もも）の力{ちから}で垂直{すいちょく}に立{た}ち上{あ}がる',
          '3. Keeping back straight, stand up vertically using leg and thigh power',
        ),
        t(
          '4. 身体{からだ}をねじらず、足{あし}の向{む}きを変{か}えて歩行{ほこう}を始{はじ}める',
          '4. Without twisting your body, turn by moving your feet and begin walking',
        ),
      ],
    },
    {
      id: 'two-person-lift-seq',
      kind: 'seq',
      name: t(
        '2人{ふたり}での共同{きょうどう}持{も}ち上{あ}げ手順{てじゅん}',
        'Procedure for two-person coordinated lift',
      ),
      goal: t(
        '重量物{じゅうりょうぶつ}を2人{ふたり}で安全{あんぜん}に運{はこ}ぶ手順{てじゅん}を順番{じゅんばん}に並{なら}べる。',
        'Arrange the steps for two people safely carrying a heavy load in order.',
      ),
      steps: [
        t(
          '1. 荷物{にもつ}の重{おも}さと運搬{うんぱん}ルートを確認{かくにん}し、合図者{あいずしゃ}を決{き}める',
          '1. Check load weight and route, and designate who will give signals',
        ),
        t(
          '2. 2人{ふたり}で向{む}かい合{あ}い、膝{ひざ}を曲{ま}げて同{おな}じタイミングで腰{こし}を落{お}とす',
          '2. Face each other, bend knees, and lower hips at the same timing',
        ),
        t(
          '3. 「せーの！」の掛{か}け声{ごえ}に合わせて同時{どうじ}に脚{あし}の力{ちから}で立{た}ち上{あ}がる',
          '3. Stand up simultaneously using leg power to the call "Ready, heve!"',
        ),
        t(
          '4. 歩調{ほちょう}を合{あ}わせて進{すす}み、降{お}ろすときも声{こえ}をかけて同時{どうじ}に腰{こし}を落{お}とす',
          '4. Walk in step together, and call out to lower hips together when placing it down',
        ),
      ],
    },
  ],
  inspect: [
    {
      id: 'lifting-form-inspection',
      defective: false,
      name: t(
        '荷持{にも}ち上{あ}げ姿勢{しせい}の良否{りょうひ}判定{はんてい}',
        'Inspection of load lifting posture and form',
      ),
      spots: [
        {
          x: 45,
          y: 25,
          check: t(
            '背骨{せぼね}・背筋{せすじ}：背筋{せすじ}がまっすぐ伸{の}びている（良品{りょうひん}：自然{しぜん}なS字{じ}カーブ / 不良{ふりょう}：猫背{ねこぜ}・腰{こし}が曲{ま}がる）',
            'Spine and back: Back is straight (Good: Natural S-curve / Defect: Slouched or arched lower back)',
          ),
        },
        {
          x: 60,
          y: 50,
          check: t(
            '荷物{にもつ}との距離{きょり}：お腹{なか}にぴったり密着{みっちゃく}している（良品{りょうひん}：抱{かか}え込{こ}み / 不良{ふりょう}：身体{からだ}から離{はな}れる）',
            'Distance to load: Held tightly against belly (Good: Embraced close / Defect: Far from body)',
          ),
        },
        {
          x: 40,
          y: 78,
          check: t(
            '膝{ひざ}と足幅{あしはば}：膝{ひざ}を深{ふか}く曲{ま}げて肩幅{かたはば}で安定{あんてい}（良品{りょうひん}：膝曲{ひざま}げ姿勢{しせい} / 不良{ふりょう}：膝直立{ひざちょくりつ}）',
            'Knees and feet: Knees deeply bent with stable shoulder-width base (Good: Knees bent / Defect: Knees locked straight)',
          ),
        },
      ],
    },
    {
      id: 'push-cart-inspection',
      defective: true,
      name: t(
        '運搬用{うんぱんよう}手押{てお}し台車{だいしゃ}の始業前{しぎょうまえ}点検{てんけん}',
        'Pre-operation inspection of transport hand truck',
      ),
      spots: [
        {
          x: 30,
          y: 25,
          check: t(
            'ハンドル部{ぶ}：固定{こてい}ボルトの緩{ゆる}みや変形{へんけい}がないか（良品{りょうひん}：堅固{けんご}に固定{こてい} / 不良{ふりょう}：ぐらつき・亀裂{きれつ}）',
            'Handle section: Check for loose bolts or deformation (Good: Firmly fixed / Defect: Loose or cracked)',
          ),
        },
        {
          x: 75,
          y: 55,
          check: t(
            '車輪{しゃりん}ブレーキ：ペダルを踏{ふ}んで確実{かくじつ}に車輪{しゃりん}がロックされるか（良品{りょうひん}：完全制動{かんぜんせいどう} / 不良{ふりょう}：制動不能{せいどうふのう}・滑{すべ}る）',
            'Wheel brake: Depress pedal to confirm wheels lock securely (Good: Fully locked / Defect: Brake fails and slips)',
          ),
        },
        {
          x: 35,
          y: 80,
          check: t(
            'キャスター車輪{しゃりん}：糸{いと}くず巻{ま}き込{こ}みやゴムの剥離{はくり}がないか（良品{りょうひん}：滑{なめ}らかに回転{かいてん} / 不良{ふりょう}：異音{いおん}・破損{はそん}）',
            'Caster wheels: Check for tangled fibers or peeled rubber (Good: Spins smoothly / Defect: Abnormal noise or damaged)',
          ),
        },
      ],
    },
    {
      id: 'storage-shelf-environment',
      defective: false,
      name: t(
        '重量物{じゅうりょうぶつ}棚{だな}の保管{ほかん}状態{じょうたい}点検{てんけん}',
        'Inspection of heavy goods shelving and storage condition',
      ),
      spots: [
        {
          x: 50,
          y: 35,
          check: t(
            '重量物{じゅうりょうぶつ}の配置{はいち}：重{おも}い荷物{にもつ}が腰{こし}から胸{むね}の高{たか}さに置{お}かれているか（良品{りょうひん}：中段{ちゅうだん}配置{はいち} / 不良{ふりょう}：床直置{ゆかじかお}きや最上段{さいじょうだん}）',
            'Heavy items placement: Heavy items stored between waist and chest height (Good: Middle shelf / Defect: On floor or top shelf)',
          ),
        },
        {
          x: 45,
          y: 70,
          check: t(
            '作業{さぎょう}床面{ゆかめん}：水濡{みずぬ}れや油{あぶら}、足元{あしもと}の段差{だんさ}がないか（良品{りょうひん}：乾燥{かんそう}・清掃済{せいそうずみ} / 不良{ふりょう}：油膜{ゆまく}で滑{すべ}る）',
            'Working floor: Free of water, oil puddles, or tripping steps (Good: Dry and clean / Defect: Slippery with oil slick)',
          ),
        },
      ],
    },
  ],
  quiz: [
    {
      q: t(
        '床{ゆか}に置{お}いてある約{やく}15kgの部品箱{ぶひんばこ}を持{も}ち上{あ}げるとき、最{もっと}も安全{あんぜん}な姿勢{しせい}はどれですか？',
        'When lifting a 15kg parts box from the floor, what is the safest posture?',
      ),
      options: [
        t(
          '足幅{あしはば}を肩幅{かたはば}に広{ひろ}げ、膝{ひざ}を曲{ま}げて腰{こし}を深{ふか}く落{お}とし、身体{からだ}に引{ひ}き寄{よ}せて脚{あし}の力{ちから}で立{た}つ',
          'Spread feet shoulder-width, bend knees to lower hips, pull load close, and stand with leg power',
        ),
        t(
          '膝{ひざ}をまっすぐ伸{の}ばしたまま腰{こし}だけを深{ふか}く曲{ま}げ、腕{うで}の力{ちから}で勢{いきお}いよく引{ひ}き上{あ}げる',
          'Keep knees straight, bend only at the waist, and jerk the load up forcefully with arms',
        ),
        t(
          '荷物{にもつ}から一歩{いっぽ}離{はな}れた位置{いち}に立{た}ち、腕{うで}をいっぱいに伸{の}ばして指先{ゆびさき}だけで持{も}ち上{あ}げる',
          'Stand one step away from the load, extend arms fully, and lift using only fingertips',
        ),
        t(
          '荷物{にもつ}を抱{かか}えながら急激{きゅうげき}に腰{こし}をひねって素早{すばや}く立{た}ち上{あ}がる',
          'Twist waist abruptly while holding the load and stand up rapidly',
        ),
      ],
      answer: 0,
      why: t(
        '膝{ひざ}を曲{ま}げて腰{こし}を落{お}とし、荷物{にもつ}を身体{からだ}に近{ちか}づけて脚{あし}の筋肉{きんにく}で持{も}ち上{あ}げると、腰{こし}にかかる負担{ふたん}を最小限{さいしょうげん}に抑{おさ}えられます。',
        'Bending knees to lower hips, bringing the load close, and lifting with leg muscles minimizes strain on the lower back.',
      ),
    },
    {
      q: t(
        '荷物{にもつ}を抱{かか}えて運{はこ}ぶとき、身体{からだ}への負担{ふたん}を減{へ}らすための荷物{にもつ}の位置{いち}として正{ただ}しいものはどれですか？',
        'When carrying a load, what is the correct position of the load to reduce strain on your body?',
      ),
      options: [
        t(
          '胸{むね}より高{たか}く持{も}ち上{あ}げて前方{ぜんぽう}の視界{しかい}を遮{さえぎ}る位置{いち}',
          'Held higher than the chest, blocking forward visibility',
        ),
        t(
          'できるだけお腹{なか}や胸{むね}に引{ひ}き寄{よ}せて身体{からだ}に密着{みっちゃく}させた位置{いち}',
          'Pulled as close as possible to the belly and chest, pressed against the body',
        ),
        t(
          '腕{うで}をまっすぐ下{した}に伸{の}ばして身体{からだ}から30cm以上{いじょう}離{はな}した位置{いち}',
          'Arms stretched straight down, keeping the load over 30cm away from the body',
        ),
        t(
          '片方{かたほう}の肩{かた}の上{うえ}に無理{むり}に乗{の}せて片手{かたて}だけで支{ささ}える位置{いち}',
          'Forced onto one shoulder and supported with only one hand',
        ),
      ],
      answer: 1,
      why: t(
        '荷物{にもつ}を身体{からだ}の中心{ちゅうしん}（重心{じゅうしん}）に近{ちか}づけて密着{みっちゃく}させることで、てこの原理{げんり}による腰{こし}への過剰{かじょう}な負荷{ふか}を防{ふせ}ぐことができます。',
        'Holding the load close to your center of gravity prevents excessive leverage strain on the lower back.',
      ),
    },
    {
      q: t(
        '荷物{にもつ}を運搬中{うんぱんちゅう}、右側{みぎがわ}の棚{だな}へ荷物{にもつ}を移動{いどう}させたいとき、腰{こし}を痛{いた}めないための動作{どうさ}はどれですか？',
        'While carrying a load, what is the movement to avoid back injury when moving the load to a shelf on the right?',
      ),
      options: [
        t(
          '足{あし}を床{ゆか}に固定{こてい}したまま、上半身{じょうはんしん}だけを強{つよ}く右{みぎ}へねじる',
          'Keep feet planted on the floor and twist only upper body forcefully to the right',
        ),
        t(
          '勢{いきお}いをつけて反動{はんどう}を利用{りよう}し、荷物{にもつ}を右{みぎ}へ振{ふ}り投{な}げる',
          'Use momentum and rebound to swing and fling the load to the right',
        ),
        t(
          '足{あし}のステップを踏{ふ}み直{なお}して身体全体{からだぜんたい}の向{む}きを右{みぎ}に変{か}えてから置{お}く',
          'Step with your feet to turn your entire body toward the right before setting it down',
        ),
        t(
          '腰{こし}を反{そ}らせながら首{くび}だけを右{みぎ}に向{む}けて素早{すばや}く置{お}く',
          'Arch your lower back while turning only your neck to the right to place it quickly',
        ),
      ],
      answer: 2,
      why: t(
        '荷物{にもつ}を持{も}ったまま上体{じょうたい}をねじると椎間板{ついかんばん}に強{つよ}いせん断力{だんりょく}がかかります。必{かなら}ず足{あし}を踏{ふ}み換{か}えて身体{からだ}ごと向{む}きを変{か}えます。',
        'Twisting while holding a load exerts dangerous shear forces on spinal discs; always step to turn your whole body.',
      ),
    },
    {
      // verify: 厚生労働省「職場における腰痛予防対策指針」では、男性の重量物取扱い上限の目安を体重の約40%、女性はその約60%としている
      q: t(
        '職場{しょくば}で約{やく}35kgの重量物{じゅうりょうぶつ}を移動{いどう}させる作業{さぎょう}を指示{しじ}されました。このときの正{ただ}しい対応{たいおう}はどれですか？',
        'You were instructed to move a heavy object of about 35kg at work. What is the correct response?',
      ),
      options: [
        t(
          '自分{じぶん}の体力{たいりょく}を試{ため}すため、一人{ひとり}で一気{いっき}に持{も}ち上{あ}げて走{はし}って運{はこ}ぶ',
          'Test personal strength by hoisting it alone all at once and running to carry it',
        ),
        t(
          '腰{こし}に痛{いた}みが出{で}ても誰{だれ}にも言{い}わず我慢{がまん}して一人{ひとり}で作業{さぎょう}を続{つづ}ける',
          'Even if back pain strikes, endure silently without telling anyone and keep working alone',
        ),
        t(
          '床{ゆか}の上{うえ}を引{ひ}きずって運{はこ}び、周囲{しゅうい}の安全確認{あんぜんかくにん}を省略{しょうりゃく}する',
          'Drag it across the floor while skipping safety confirmation of surrounding areas',
        ),
        t(
          '一人{ひとり}で無理{むり}に持{も}たず、同僚{どうりょう}に声{こえ}をかけて2人{ふたり}で持{も}つか台車{だいしゃ}を使用{しよう}する',
          'Do not lift alone; call a coworker to lift together with two people or use a cart',
        ),
      ],
      answer: 3,
      why: t(
        '30kg以上{いじょう}の荷物{にもつ}は一人{ひとり}作業{さぎょう}の制限{せいげん}を超{こ}えています。無理{むり}をせず台車{だいしゃ}を利用{りよう}するか、2人{ふたり}で息{いき}を合{あ}わせて運搬{うんぱん}します。',
        'Objects of 30kg or more exceed safe single-person limits; use a cart or perform a coordinated two-person lift.',
      ),
    },
    {
      q: t(
        '2人{ふたり}で重量物{じゅうりょうぶつ}を持{も}ち上{あ}げる「共同{きょうどう}持{も}ち上{あ}げ作業{さぎょう}」で最{もっと}も重要{じゅうよう}なルールは何{なん}ですか？',
        'What is the most important rule in a two-person coordinated lift of heavy objects?',
      ),
      options: [
        t(
          '「せーの！」など合図{あいず}の声{こえ}を出{だ}して、2人{ふたり}で息{いき}を合{あ}わせて同時{どうじ}に持{も}ち上{あ}げる',
          'Call out signals like "Ready, heave!" to synchronize timing and lift together simultaneously',
        ),
        t(
          '力{ちから}の強{つよ}い人{ひと}が合図{あいず}なしで一気{いっき}に引{ひ}き上{あ}げて主導権{しゅどうけん}をとる',
          'The stronger person jerks the load up quickly without signaling to take the lead',
        ),
        t(
          '2人{ふたり}がそれぞれ別{べつ}のタイミングで適当{てきとう}に力{ちから}を入{い}れる',
          'Each of the two persons applies force at completely random separate timings',
        ),
        t(
          '運搬中{うんぱんちゅう}に声{こえ}を出{だ}さず無言{むごん}のまま作業{さぎょう}を進{すす}める',
          'Carry out the task in complete silence without making any vocal calls',
        ),
      ],
      answer: 0,
      why: t(
        '2人{ふたり}の呼吸{こきゅう}やタイミングがずれると片方{かたほう}に全重量{ぜんじゅうりょう}が一気{いっき}にかかり腰{こし}を痛{いた}めます。合図{あいず}の徹底{てってい}が必須{ひっす}です。',
        'If timing is out of sync, the full weight shifts onto one person causing severe injury; strict signaling is essential.',
      ),
    },
    {
      q: t(
        '手押{てお}し台車{だいしゃ}を使{つか}って重量物{じゅうりょうぶつ}を運搬{うんぱん}する際{さい}、安全{あんぜん}な押{お}し方{かた}として正{ただ}しいものはどれですか？',
        'When transporting heavy loads using a push cart, which is the correct safe way to push it?',
      ),
      options: [
        t(
          '台車{だいしゃ}を前方{ぜんぽう}に走{はし}らせて自分{じぶん}は後{うし}ろから小走{こばし}りで追{お}いかける',
          'Shove the cart forward to let it roll and jog after it from behind',
        ),
        t(
          '背筋{せすじ}を伸{の}ばして両手{りょうて}でハンドルを握{にぎ}り、自分{じぶん}の体重{たいじゅう}を乗{の}せて前方{ぜんぽう}にゆっくり押{お}す',
          'Keep back straight, grip handle with both hands, and slowly push forward using body weight',
        ),
        t(
          '台車{だいしゃ}を後{うし}ろ向{む}きに引{ひ}っ張{ぱ}りながら後{うし}ろ歩{ある}きで進{すす}む',
          'Walk backward while pulling the cart behind you',
        ),
        t(
          '荷物{にもつ}を高{たか}く積{つ}み上{あ}げて前{まえ}が見{み}えない状態{じょうたい}で無理{むり}に押{お}す',
          'Stack cargo so high that forward vision is blocked and push blindly',
        ),
      ],
      answer: 1,
      why: t(
        '台車{だいしゃ}は背筋{せすじ}を伸{の}ばし、両手{りょうて}でしっかりハンドルを押{お}して前進{ぜんしん}するのが基本{きほん}です。引{ひ}っ張{ぱ}ると足{あし}を巻{ま}き込{こ}む危険{きけん}があります。',
        'Pushing forward with both hands and a straight back is fundamental; pulling risks running over your own feet.',
      ),
    },
    {
      q: t(
        '長時間{ちょうじかん}中腰{ちゅうごし}や前屈{ぜんくつ}姿勢{しせい}での溶接{ようせつ}・組立{くみた}て作業{さぎょう}が続{つづ}くとき、腰痛{ようつう}を防{ふせ}ぐための適切{てきせつ}な行動{こうどう}はどれですか？',
        'When stooped or forward-bent welding/assembly continues for hours, what is appropriate to prevent back pain?',
      ),
      options: [
        t(
          '痛{いた}くても休憩{きゅうけい}を取{と}らず、作業{さぎょう}が終{お}わるまで同{おな}じ姿勢{しせい}を固定{こてい}し続{つづ}ける',
          'Take no breaks even if painful, keeping the same posture locked until work is finished',
        ),
        t(
          '姿勢{しせい}がきつくなったら腰{こし}を急激{きゅうげき}にバキバキと強{つよ}くひねる',
          'Abruptly crack and twist your waist with strong force when feeling stiff',
        ),
        t(
          '定期的{ていきてき}に小休止{しょうきゅうし}を入{い}れ、背筋{せすじ}をゆっくり反{そ}らすストレッチを行{おこな}う',
          'Take short breaks regularly and gently arch your back backward to stretch',
        ),
        t(
          '痛{いた}み止{ど}めを飲{の}んで無理{むり}やり長時間{ちょうじかん}の連続{れんぞく}作業{さぎょう}をこなす',
          'Take painkiller medicine and force yourself through continuous hours of work',
        ),
      ],
      answer: 2,
      why: t(
        '同{おな}じ姿勢{しせい}を長{なが}く続{つづ}けると筋肉{きんにく}の血流{けつりゅう}が悪化{あっか}します。定期的{ていきてき}に姿勢{しせい}を変{か}え、上体{じょうたい}を反{そ}らすストレッチでほぐします。',
        'Holding one posture worsens muscle blood circulation; change postures periodically and stretch by arching backward.',
      ),
    },
    {
      q: t(
        '作業{さぎょう}開始前{かいしまえ}に行{おこな}う腰痛予防{ようつうよぼう}のストレッチで、正{ただ}しい方法{ほうほう}はどれですか？',
        'What is the correct method for warm-up stretching to prevent lower back pain before starting work?',
      ),
      options: [
        t(
          '勢{いきお}いをつけて反動{はんどう}（バウンド）をつけながら激{はげ}しく身体{からだ}を曲{ま}げる',
          'Bounce vigorously with momentum and jerk your body into deep bends',
        ),
        t(
          '息{いき}を止{と}めて限界{げんかい}まで強{つよ}く筋肉{きんにく}を引{ひ}っ張{ぱ}り続{つづ}ける',
          'Hold breath and pull muscles as hard as possible past their limit',
        ),
        t(
          '身体{からだ}が温{あたた}まっていない状態{じょうたい}でいきなり重{おも}い鉄材{てつざい}を持{も}ち上{あ}げる',
          'Directly hoist heavy steel materials suddenly before the body has warmed up',
        ),
        t(
          '息{いき}を吐{は}きながら反動{はんどう}をつけず、ゆっくり20秒{びょう}ほど筋肉{きんにく}を伸{の}ばす',
          'Exhale naturally without bouncing, gently stretching the muscles for about 20 seconds',
        ),
      ],
      answer: 3,
      why: t(
        'ストレッチは反動{はんどう}をつけず、自然{しぜん}な呼吸{こきゅう}を続{つづ}けながらゆっくり伸{の}ばすことで筋肉{きんにく}を安全{あんぜん}にほぐします。',
        'Stretching safely relaxes muscles when done gently without bouncing while maintaining steady breathing.',
      ),
    },
    {
      q: t(
        '作業中{さぎょうちゅう}に腰{こし}に「ピキッ」と鋭{するど}い痛{いた}みが走{はし}り、違和感{いわかん}を感{かん}じました。直{ただ}ちにとるべき行動{こうどう}はどれですか？',
        'During work, you felt an acute sharp pain and strain in your lower back. What action should you take immediately?',
      ),
      options: [
        t(
          '直{ただ}ちに作業{さぎょう}を中断{ちゅうだん}して腰{こし}を休{やす}め、速{すみ}やかに職長{しょくちょう}へ報告{ほうこく}・相談{そうだん}する',
          'Stop work immediately to rest your back and report/consult with the foreman promptly',
        ),
        t(
          '周囲{しゅうい}に心配{しんぱい}をかけないよう、痛{いた}みを隠{かく}して定時{ていじ}まで重{おも}い荷物{にもつ}を運{はこ}び続{つづ}ける',
          'Conceal pain to avoid worrying others, continuing to carry heavy loads until shift ends',
        ),
        t(
          '自己判断{じこはんだん}で強{つよ}く腰{こし}を叩{たた}いて作業{さぎょう}を再開{さいかい}する',
          'Beat your own back forcefully on self-judgment and resume working',
        ),
        t(
          '誰{だれ}にも言{い}わずに現場{げんば}を抜{ぬ}け出{だ}して勝手{かって}に帰宅{きたく}する',
          'Slip away from the work site without telling anyone and go home alone',
        ),
      ],
      answer: 0,
      why: t(
        'ぎっくり腰{ごし}などの初期{しょき}症状{しょうじょう}で無理{むり}を続{つづ}けると重症化{じゅうしょうか}します。作業{さぎょう}を止{と}めて職長{しょくちょう}に報告{ほうこく}し適切{てきせつ}な処置{しょち}を受{う}けます。',
        'Continuing work with acute lumbago causes severe aggravation; stop working and report to the foreman for proper care.',
      ),
    },
    {
      q: t(
        '棚{だな}に部品{ぶひん}を保管{ほかん}するとき、腰{こし}の負担{ふたん}を減{へ}らすための配置{はいち}として最{もっと}も適切{てきせつ}なものはどれですか？',
        'When storing parts on shelves, which arrangement is most appropriate to reduce strain on the lower back?',
      ),
      options: [
        t(
          '一番{いちばん}重{おも}い金属{きんぞく}部品{ぶひん}を一歩{いっぽ}手前{てまえ}の最上段{さいじょうだん}（頭{あたま}より高{たか}い位置{いち}）に置{お}く',
          'Place the heaviest metal parts on the top shelf higher than head level',
        ),
        t(
          '頻繁{ひんぱん}に使{つか}う重{おも}い部品{ぶひん}を、腰{こし}から胸{むね}の間の高{たか}さの中段{ちゅうだん}に置{お}く',
          'Place frequently used heavy parts on middle shelves between waist and chest height',
        ),
        t(
          'すべての重量物{じゅうりょうぶつ}を床{ゆか}に直置{じかお}きして、毎回{まいかい}深{ふか}くしゃがませる',
          'Place all heavy items directly on the floor, requiring deep squatting each time',
        ),
        t(
          '手前{てまえ}に軽{かる}い物{もの}を置{お}き、奥{おく}の取{と}りにくい場所{ばしょ}に重{おも}い物{もの}を置{お}く',
          'Place light items in front and heavy items deep inside where they are hard to reach',
        ),
      ],
      answer: 1,
      why: t(
        '腰{こし}から胸{むね}の高{たか}さは中腰{ちゅうごし}にならずに持{も}ち上{あ}げられる最{もっと}も安全{あんぜん}な範囲{はんい}です。重量物{じゅうりょうぶつ}は中段{ちゅうだん}に配置{はいち}します。',
        'The height between waist and chest is the safest power zone avoiding stooping; store heavy loads on middle shelves.',
      ),
    },
    {
      q: t(
        '荷物{にもつ}を運搬{うんぱん}する通路{つうろ}の管理{かんり}について、腰痛{ようつう}や転倒{てんとう}を防{ふせ}ぐために正{ただ}しいものはどれですか？',
        'Regarding walkway management, which is correct for preventing falls and back injuries?',
      ),
      options: [
        t(
          '通路{つうろ}に油{あぶら}や水{みず}がこぼれていても、急{いそ}いでいるときはそのまま放置{ほうち}する',
          'Even if oil or water spills in the aisle, leave it alone when in a hurry',
        ),
        t(
          '通路{つうろ}の真{ま}ん中{なか}に余{あま}ったパレットや電線{でんせん}コードを置{お}いておく',
          'Leave surplus pallets and electric cables right in the middle of walkways',
        ),
        t(
          '通路{つうろ}の段差{だんさ}や油{あぶら}・水濡{みずぬ}れを清掃{せいそう}・整頓{せいとん}し、滑{すべ}りにくい安全{あんぜん}な状態{じょうたい}を保{たも}つ',
          'Clean and organize steps, oil, and wet puddles in aisles to maintain a non-slip safe surface',
        ),
        t(
          'つまずきそうな突起{とっき}があっても「気{き}をつけて歩{ある}けばよい」と放置{ほうち}する',
          'Leave tripping hazards alone assuming people can just walk carefully',
        ),
      ],
      answer: 2,
      why: t(
        '足元{あしもと}が滑{すべ}ったりつまずいたりすると、体勢{たいせい}を立{た}て直{なお}そうとして腰{こし}に瞬間的{しゅんかんてき}な大荷重{だいかじゅう}がかかり腰痛{ようつう}の原因{げんいん}になります。',
        'Slipping or stumbling causes sudden severe reflex loads on the lower back; keeping walkways clear is vital.',
      ),
    },
    {
      // verify: 厚生労働省「職場における腰痛予防対策指針」では、男性の重量物取扱い上限の目安を体重の約40%、女性はその約60%としている
      q: t(
        '厚生労働省{こうせいろうどうしょう}の指針{ししん}における、成人{せいじん}男性{だんせい}作業者{さぎょうしゃ}が一人{ひとり}で取扱{とりあつか}う重量物{じゅうりょうぶつ}の目安{めやす}として適切{てきせつ}なものはどれですか？',
        'According to MHLW guidelines, what is an appropriate guideline for weight an adult male worker handles alone?',
      ),
      options: [
        t(
          '自分{じぶん}の体重{たいじゅう}と同{おな}じ重{おも}さ（100%）まで',
          'Up to 100% of one own body weight',
        ),
        t(
          '自分{じぶん}の体重{たいじゅう}の80%程度{ていど}まで',
          'Up to about 80% of one own body weight',
        ),
        t(
          '年齢{ねんれい}や体力{たいりょく}に関係{かんけい}なく一律{いちりつ}50kgまで',
          'A flat 50kg regardless of age or physical strength',
        ),
        t(
          '自分{じぶん}の体重{たいじゅう}の概{おおむ}ね40%以下{いか}を目安{めやす}とし、無理{むり}のない範囲{はんい}にする',
          'As a rule of thumb, approximately 40% or less of body weight within reasonable limits',
        ),
      ],
      answer: 3,
      why: t(
        '指針{ししん}では男性{だんせい}の単独{たんどく}重量物{じゅうりょうぶつ}取扱{とりあつか}いの上限{じょうげん}目安{めやす}を体重{たいじゅう}の約{やく}40%（女性{じょせい}はその約{やく}60%）としています。',
        'The guideline sets the single-person lift limit for men at roughly 40% of body weight (approx. 60% of that for women).',
      ),
    },
  ],
  glossary: [
    {
      ja: '腰痛予防',
      reading: 'ようつうよぼう',
      en: 'Back pain prevention',
      note: t(
        '正しい荷物の持ち方や環境改善により、腰の障害を防ぐこと。',
        'Preventing back disorders through correct lifting postures and environmental improvements.',
      ),
    },
    {
      ja: '椎間板',
      reading: 'ついかんばん',
      en: 'Intervertebral disc',
      note: t(
        '背骨の骨と骨の間にあるクッション。無理な前屈姿勢で強い圧力がかかる。',
        'Cushion between spinal vertebrae. Heavy pressure is applied during forced stooping.',
      ),
    },
    {
      ja: '中腰姿勢',
      reading: 'ちゅうごししせい',
      en: 'Half-crouching posture / stooped posture',
      note: t(
        '膝を十分に曲げず腰だけを前に曲げた姿勢。腰への負担が極めて大きい。',
        'Posture where only waist is bent forward without bending knees enough. Heavy strain on back.',
      ),
    },
    {
      ja: '前屈姿勢',
      reading: 'ぜんくつしせい',
      en: 'Forward bending posture',
      note: t(
        '上体を前方に深く倒した姿勢。背筋や腰の筋肉に緊張が続く。',
        'Posture with upper body bent deeply forward. Prolonged tension on back muscles.',
      ),
    },
    {
      ja: '重量物',
      reading: 'じゅうりょうぶつ',
      en: 'Heavy load / heavy object',
      note: t(
        '機械部品や鉄板など、持ち上げや移動に注意が必要な重い物。',
        'Heavy objects such as machine parts or steel plates requiring caution when moving.',
      ),
    },
    {
      ja: '台車',
      reading: 'だいしゃ',
      en: 'Hand truck / push cart / trolley',
      note: t(
        '荷物を載せて車輪で押して運ぶ運搬器具。腰の負担を大幅に減らす。',
        'Transport equipment carrying loads on wheels pushed by hand, greatly reducing back strain.',
      ),
    },
    {
      ja: '共同持ち上げ',
      reading: 'きょうどうもちあげ',
      en: 'Coordinated two-person lift',
      note: t(
        '重い物を2人以上で呼吸を合わせ、合図とともに持ち上げること。',
        'Lifting heavy objects together by two or more people in coordination with signals.',
      ),
    },
    {
      ja: 'ストレッチ',
      reading: 'すとれっち',
      en: 'Stretching exercise',
      note: t(
        '筋肉や腱をゆっくり伸ばして柔軟性を高め、腰痛や怪我を防ぐ運動。',
        'Exercise of gently extending muscles and tendons to increase flexibility and prevent injury.',
      ),
    },
    {
      ja: '背筋',
      reading: 'せすじ',
      en: 'Back spine / back posture',
      note: t(
        '首から腰にかけての背骨のライン。荷物を持つときはまっすぐ伸ばす。',
        'Line of the spine from neck to waist. Must be kept straight when lifting loads.',
      ),
    },
    {
      ja: 'ぎっくり腰',
      reading: 'ぎっくりごし',
      en: 'Acute lower back strain / lumbago',
      note: t(
        '重い荷物を持った瞬間などに起こる、急性の激しい腰の痛みや筋膜損傷。',
        'Acute severe back pain or myofascial strain occurring upon lifting heavy loads.',
      ),
    },
    {
      ja: '大腿部',
      reading: 'だいたいぶ',
      en: 'Thigh muscles / quadriceps',
      note: t(
        '太ももの太い筋肉。荷物を持ち上げるときは腰ではなくこの力を使う。',
        'Thick muscles of the thighs. Use this power instead of lower back when lifting.',
      ),
    },
    {
      ja: '報連相',
      reading: 'ほうれんそう',
      en: 'Report, communicate, consult (Ho-Ren-So)',
      note: t(
        '報告・連絡・相談。腰に痛みや違和感が出たときも速やかに職長へ行う。',
        'Report, Contact, Consult. Promptly notify the foreman when experiencing back pain.',
      ),
    },
  ],
}
