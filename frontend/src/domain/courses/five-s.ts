import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string): Tri => ({ ja, en })

export const fiveS: CourseData = {
  id: 'five-s',
  category: 'core',
  level: 1,
  minutes: 15,
  title: t(
    '5S（整理{せいり}・整頓{せいとん}・清掃{せいそう}・清潔{せいけつ}・躾{しつけ}）',
    '5S: Sort, Set in order, Shine, Standardize, Sustain',
  ),
  sub: t(
    '安全{あんぜん}は5Sから。職場{しょくば}を整{ととの}えて、転倒{てんとう}や事故{じこ}を防{ふせ}ぐ。',
    'Safety begins with 5S. Organize the workplace to prevent slips, trips, and accidents.',
  ),
  refs: [
    '労働安全衛生法 第59条（雇入れ時等の安全衛生教育）',
    '労働安全衛生規則 第540条・第542条（作業場内の安全通路の確保）',
    '労働安全衛生規則 第619条・第620条（清掃の実施及び清潔の保持）',
    '中央労働災害防止協会（中災防） 5S推進・職場安全衛生管理指針',
  ],
  panels: [
    {
      who: 'foreman',
      text: t(
        '今日{きょう}は工場{こうじょう}の基本{きほん}、「5S」について学{まな}びましょう。',
        'Today we will learn about the foundation of the factory: 5S.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '5Sですか？掃除{そうじ}をして職場{しょくば}をきれいにすることですか？',
        'Is 5S just about cleaning and making the workplace look tidy?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'ただの掃除{そうじ}ではありません。不要{ふよう}な物{もの}を捨{す}てる「整理{せいり}」と、定位置{ていいち}に置{お}く「整頓{せいとん}」が事故{じこ}を防{ふせ}ぎます。',
        'It is not just cleaning. "Sort" removes unneeded items, and "Set in order" places tools properly to prevent accidents.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '使{つか}った工具{こうぐ}は、後{あと}でまた使{つか}うので足元{あしもと}に置{お}いておきますね。',
        'I will put the used tools by my feet since I will use them again soon.',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'ちょっと待{ま}ってください。床{ゆか}に置{お}いた工具{こうぐ}やコードにつまずくと、大{おお}きなケガになります。',
        'Wait a moment. If someone trips over tools or cords on the floor, it can cause serious injury.',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '工具{こうぐ}は姿置{すがたお}きの定位置{ていいち}へ戻{もど}し、通路{つうろ}には物{もの}を一切{いっさい}置{お}かないのが鉄則{てっそく}です。',
        'Always return tools to their marked shadow board spots, and never place anything in the walkway.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '機械{きかい}の周{まわ}りに油{あぶら}が垂{た}れています。これも拭{ふ}いたほうがいいですか？',
        'There is oil dripping around the machine. Should I wipe this up too?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'すぐに拭{ふ}き取{と}りましょう。「清掃{せいそう}は点検{てんけん}」です。滑{すべ}りを防{ふせ}ぎ、油漏{あぶらも}れの故障{こしょう}も早{はや}く見{み}つかります。',
        'Wipe it up right away. "Cleaning is inspecting." It prevents slips and detects oil leak faults early.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '整理{せいり}・整頓{せいとん}・清掃{せいそう}を毎日{まいにち}の習慣{しゅうかん}にして、安全{あんぜん}に作業{さぎょう}します！',
        'I will make sorting, ordering, and cleaning a daily habit, and work safely!',
      ),
    },
  ],
  handbook: [
    t(
      '通路{つうろ}の白線{はくせん}・黄線{きいろせん}の内側{うちがわ}に物{もの}を置{お}いてはならない。',
      'Never place any objects inside the marked white or yellow walkway lines.',
    ),
    t(
      '非常口{ひじょうぐち}や消火器{しょうかき}の前{まえ}に荷物{にもつ}を置{お}いてふさいではならない。',
      'Never block emergency exits or fire extinguishers with equipment or cargo.',
    ),
    t(
      '使{つか}わない物{もの}や不要{ふよう}なゴミは作業場{さぎょうば}にためず、すぐに捨{す}てる（整理{せいり}）。',
      'Never accumulate unneeded items or scrap in the workspace; discard them immediately (Sort).',
    ),
    t(
      '工具{こうぐ}は使{つか}い終{お}わったら必{かなら}ず姿置{すがたお}きや棚{たな}の定位置{ていいち}へ戻{もど}す（整頓{せいとん}）。',
      'Always return tools to their designated spot on the shadow board or shelf right after use (Set in order).',
    ),
    t(
      '通路{つうろ}を横切{よこぎ}るようにコードやホースを床{ゆか}へ放置{ほうち}してはならない。',
      'Never leave cables or hoses lying across walkways where people walk.',
    ),
    t(
      '床{ゆか}に垂{た}れた油{あぶら}や水{みず}は、転倒{てんとう}を防{ふせ}ぐため直{ただ}ちに拭{ふ}き取{と}る（清掃{せいそう}）。',
      'Always wipe up spilled oil or water on the floor immediately to prevent slips and falls (Shine).',
    ),
    t(
      '清掃{せいそう}しながら機械{きかい}のボルトの緩{ゆる}みや油漏{あぶらも}れを点検{てんけん}する。',
      'Inspect the machine for loose bolts and fluid leaks while cleaning it.',
    ),
    t(
      '作業{さぎょう}の終了時{しゅうりょうじ}には必{かなら}ず「3分間{さんぷんかん}の5S」を行{おこな}い、清潔{せいけつ}な状態{じょうたい}を維持{いじ}する（清潔{せいけつ}）。',
      'Always perform 3 minutes of 5S cleanup at the end of each shift to maintain cleanliness (Standardize).',
    ),
    t(
      '決{き}められた置{お}き場{ば}と整頓{せいとん}ルールを毎日{まいにち}守{まも}る習慣{しゅうかん}をつける（躾{しつけ}）。',
      'Make it a strict daily habit to follow designated storage places and ordering rules (Sustain).',
    ),
    t(
      '高所{こうしょ}の足場{あしば}の上{うえ}に工具{こうぐ}や材料{ざいりょう}を置{お}いたまま離{はな}れてはならない。',
      'Never leave tools or loose materials unattended on elevated scaffoldings.',
    ),
  ],
  reel: [
    t(
      '通路{つうろ}、障害物{しょうがいぶつ}なし、ヨシ！',
      'Walkway, no obstacles, yoshi!',
    ),
    t(
      '床{ゆか}の油{あぶら}、拭{ふ}き取{と}り、ヨシ！',
      'Floor oil, wiped clean, yoshi!',
    ),
    t(
      '消火器{しょうかき}の前{まえ}、クリア、ヨシ！',
      'In front of fire extinguisher, clear, yoshi!',
    ),
    t(
      '非常口{ひじょうぐち}、開放{かいほう}、ヨシ！',
      'Emergency exit, unblocked, yoshi!',
    ),
    t(
      '配線{はいせん}、保護{ほご}カバー、ヨシ！',
      'Cables, protective cover in place, yoshi!',
    ),
    t(
      '工具{こうぐ}の姿置{すがたお}き、定位置{ていいち}、ヨシ！',
      'Tools on shadow board, in place, yoshi!',
    ),
    t(
      '作業後{さぎょうご}の整理{せいり}・整頓{せいとん}、完了{かんりょう}、ヨシ！',
      'Post-work sort and set in order, complete, yoshi!',
    ),
  ],
  drills: [
    {
      id: 'five-s-hazard-spot',
      kind: 'spot',
      secs: 30,
      need: 4,
      name: t(
        '作業場{さぎょうば}の5S危険{きけん}箇所{かしょ}チェック',
        '5S workplace hazard spotting',
      ),
      goal: t(
        '30秒{びょう}で作業場{さぎょうば}の不安全{ふあんぜん}な状態{じょうたい}を4つ見{み}つける。',
        'Find 4 unsafe conditions in the workspace within 30 seconds.',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '通路{つうろ}を横切{よこぎ}る電気{でんき}コード',
            'Electric cord lying across the walkway',
          ),
        },
        {
          hazard: false,
          label: t(
            '定位置{ていいち}に掛{か}けられたスパナ',
            'Wrench hung on its designated shadow spot',
          ),
        },
        {
          hazard: true,
          label: t(
            '非常口{ひじょうぐち}の扉{とびら}の前{まえ}に置{お}かれた段{だん}ボール箱{ばこ}',
            'Cardboard boxes stacked in front of emergency exit',
          ),
        },
        {
          hazard: false,
          label: t(
            '白線{はくせん}の内側{うちがわ}に何{なに}も置{お}かれていない通路{つうろ}',
            'Walkway clear inside the white boundary lines',
          ),
        },
        {
          hazard: true,
          label: t(
            'ボール盤{ばん}の周{まわ}りに垂{た}れた切削油{せっさくゆ}',
            'Spilled cutting oil around the drill press',
          ),
        },
        {
          hazard: false,
          label: t(
            'フタが閉{し}まった油用{あぶらよう}のゴミ箱{ばこ}',
            'Covered trash bin for oily waste rags',
          ),
        },
        {
          hazard: true,
          label: t(
            '作業台{さぎょうだい}の端{はし}に置{お}かれた転落{てんらく}しそうなハンマー',
            'Hammer placed dangerously on the edge of the workbench',
          ),
        },
        {
          hazard: false,
          label: t(
            '配線{はいせん}カバーで覆{おお}われた足元{あしもと}のケーブル',
            'Floor cable protected by a floor ramp cover',
          ),
        },
        {
          hazard: true,
          label: t(
            '消火器{しょうかき}を隠{かく}すように置{お}かれた端材{はざい}',
            'Scrap wood blocking and hiding the fire extinguisher',
          ),
        },
      ],
    },
    {
      id: 'five-s-end-of-day-seq',
      kind: 'seq',
      name: t(
        '作業{さぎょう}終了時{しゅうりょうじ}の5S手順{てじゅん}',
        'End-of-day 5S cleanup sequence',
      ),
      goal: t(
        '作業{さぎょう}が終{お}わったときの片付{かたづ}け手順{てじゅん}を正{ただ}しい順番{じゅんばん}に並{なら}べる。',
        'Arrange the cleanup steps after work in the correct sequence.',
      ),
      steps: [
        t(
          '1. 機械{きかい}のスイッチを止{と}めて主電源{しゅでんげん}を切{き}る',
          '1. Stop the machine and turn OFF the main power switch',
        ),
        t(
          '2. 床{ゆか}の切{き}りくずや油{あぶら}を掃除{そうじ}して拭{ふ}き取{と}る',
          '2. Sweep up metal chips and wipe spilled oil off the floor',
        ),
        t(
          '3. 使{つか}った工具{こうぐ}を拭{ふ}いて姿置{すがたお}きの定位置{ていいち}へ戻{もど}す',
          '3. Wipe used tools clean and return them to the shadow board',
        ),
        t(
          '4. ゴミを分別{ぶんべつ}して指定{してい}の廃棄場{はいきじょう}へ捨{す}てる',
          '4. Sort trash and dispose of it in the designated waste bins',
        ),
        t(
          '5. 通路{つうろ}と消火器{しょうかき}の前{まえ}に物{もの}がないか指差呼称{しさこしょう}で確認{かくにん}する',
          '5. Confirm with pointing and calling that walkways and extinguishers are clear',
        ),
      ],
    },
    {
      id: 'oil-spill-action-seq',
      kind: 'seq',
      name: t(
        '床{ゆか}の油漏{あぶらも}れ発見時{はっけんじ}の対応{たいおう}',
        'Oil spill response sequence on the floor',
      ),
      goal: t(
        '床{ゆか}に油{あぶら}がこぼれているのを見{み}つけたときの対応{たいおう}を順番{じゅんばん}に並{なら}べる。',
        'Arrange the response steps in order when finding spilled oil on the floor.',
      ),
      steps: [
        t(
          '1. 「足元{あしもと}注意{ちゅうい}」の声{こえ}かけをして周囲{しゅうい}に知{し}らせる',
          '1. Call out "Watch your step!" to warn workers nearby',
        ),
        t(
          '2. 吸着{きゅうちゃく}マットやウエスを油{あぶら}の上{うえ}に敷{し}いて広{ひろ}がりを防{ふせ}ぐ',
          '2. Place oil absorbent mats or rags over the oil to stop it spreading',
        ),
        t(
          '3. 床{ゆか}をしっかり拭{ふ}き取{と}り、滑{すべ}らない状態{じょうたい}にする',
          '3. Wipe the floor thoroughly until it is completely non-slippery',
        ),
        t(
          '4. 漏{も}れの原因{げんいん}と場所{ばしょ}を職長{しょくちょう}へ報告{ほうこく}する',
          '4. Report the oil leak location and suspected cause to the foreman',
        ),
      ],
    },
  ],
  inspect: [
    {
      id: 'tool-shadow-board',
      defective: false,
      name: t(
        '工具{こうぐ}の姿置{すがたお}き・形跡管理{けいせきかんり}パネル',
        'Tool shadow board and placement panel',
      ),
      spots: [
        {
          x: 35,
          y: 30,
          check: t(
            '輪郭線{りんかくせん}：全{すべ}ての工具{こうぐ}が形{かたち}の通{とお}りに戻{もど}っているか（良品{りょうひん}：欠落{けつらく}なし / 不良{ふりょう}：空{あ}きがある）',
            'Outlines: All tools returned to their shaped silhouettes (Good: None missing / Defect: Missing tools)',
          ),
        },
        {
          x: 65,
          y: 65,
          check: t(
            '名札{なふだ}表示{ひょうじ}：工具{こうぐ}の名称{めいしょう}とサイズが見{み}えるか（良品{りょうひん}：鮮明{せんめい} / 不良{ふりょう}：はがれ・汚損{おそん}）',
            'Name label: Tool name and size clearly readable (Good: Legible / Defect: Peeling or stained)',
          ),
        },
      ],
    },
    {
      id: 'walkway-and-exit',
      defective: true,
      name: t(
        '工場内{こうじょうない}通路{つうろ}と非常扉{ひじょうとびら}の周辺{しゅうへん}',
        'Factory walkway and emergency exit area',
      ),
      spots: [
        {
          x: 25,
          y: 40,
          check: t(
            '通路{つうろ}の白線{はくせん}：荷物{にもつ}が線{せん}の内側{うちがわ}にはみ出{で}ていないか（良品{りょうひん}：障害物{しょうがいぶつ}ゼロ / 不良{ふりょう}：荷物{にもつ}がはみ出{で}し）',
            'Walkway line: No cargo protruding inside the walkway lines (Good: Zero obstacles / Defect: Cargo sticking out)',
          ),
        },
        {
          x: 75,
          y: 70,
          check: t(
            '非常扉{ひじょうとびら}の前{まえ}：即座{そくざ}に開{あ}けられる空間{くうかん}があるか（良品{りょうひん}：空間{くうかん}確保{かくほ} / 不良{ふりょう}：パレットで閉鎖{へいさ}）',
            'Front of exit: Clear space to open immediately (Good: Clear space / Defect: Blocked by pallet)',
          ),
        },
      ],
    },
    {
      id: 'fire-extinguisher-station',
      defective: false,
      name: t(
        '消火器{しょうかき}の設置{せっち}場所{ばしょ}と表示{ひょうじ}',
        'Fire extinguisher station and indicator sign',
      ),
      spots: [
        {
          x: 40,
          y: 25,
          check: t(
            '位置{いち}表示板{ひょうじばん}：遠{とお}くからでも「消火器{しょうかき}」の文字{もじ}が見{み}えるか（良品{りょうひん}：視認{しにん}可能{かのう} / 不良{ふりょう}：隠{かく}れている）',
            'Signboard: "Fire Extinguisher" text clearly visible from distance (Good: Visible / Defect: Hidden)',
          ),
        },
        {
          x: 55,
          y: 75,
          check: t(
            '前{まえ}の床面{ゆかめん}：黄色{きいろ}の枠線{わくせん}の中{なか}に物{もの}が置{お}かれていないか（良品{りょうひん}：立入{たちいり}可能{かのう} / 不良{ふりょう}：物置{ものお}き化{か}）',
            'Floor in front: Yellow boundary box completely clear of items (Good: Accessible / Defect: Blocked)',
          ),
        },
      ],
    },
  ],
  quiz: [
    {
      q: t(
        '作業中{さぎょうちゅう}に「後{あと}でまた使{つか}うから」と、スパナを通路{つうろ}の床{ゆか}に置{お}きました。何{なに}が問題{もんだい}ですか？',
        'During work, a worker left a wrench on the walkway floor because "I will use it again later." What is the problem?',
      ),
      options: [
        t(
          '人{ひと}がつまずいて転倒{てんとう}する危険{きけん}がある',
          'There is a danger that someone will trip and fall',
        ),
        t(
          '工具{こうぐ}が冷{つめ}たくなって使{つか}えなくなる',
          'The tool will become cold and unusable',
        ),
        t(
          '通路{つうろ}のペンキがはがれてしまう',
          'The walkway paint will peel off',
        ),
        t(
          'スパナの重{おも}さで床{ゆか}がへこんでしまう',
          'The floor will dent from the weight of the wrench',
        ),
      ],
      answer: 0,
      why: t(
        '床{ゆか}や通路{つうろ}に置{お}いた工具{こうぐ}は、歩行者{ほこうしゃ}がつまずいて転倒{てんとう}する重大{じゅうだい}な危険{きけん}になります。必{かなら}ず定位置{ていいち}へ戻{もど}します。',
        'Tools left on floors or walkways pose a severe trip-and-fall hazard to anyone walking by. Always return them to their designated place.',
      ),
    },
    {
      q: t(
        '機械{きかい}の周{まわ}りの床{ゆか}に油{あぶら}がこぼれているのを見{み}つけました。最初{さいしょ}に行{おこな}うべき行動{こうどう}はどれですか？',
        'You find spilled oil on the floor around a machine. What is the first action you should take?',
      ),
      options: [
        t(
          '「自分{じぶん}のこぼした油{あぶら}ではない」と無視{むし}して作業{さぎょう}を続{つづ}ける',
          'Ignore it and continue working because "I did not spill it"',
        ),
        t(
          '周囲{しゅうい}に声{こえ}をかけて滑{すべ}る危険{きけん}を伝{つた}え、直{ただ}ちに拭{ふ}き取{と}る',
          'Warn others nearby of slip hazards and wipe it up immediately',
        ),
        t(
          '乾{かわ}くまでそのままにしておく',
          'Leave it as it is until it dries naturally',
        ),
        t(
          '上{うえ}から砂{すな}やゴミをかぶせて隠{かく}す',
          'Cover it with dirt or scrap to hide it',
        ),
      ],
      answer: 1,
      why: t(
        '床{ゆか}の油{あぶら}は転倒{てんとう}事故{じこ}の原因{げんいん}になります。周囲{しゅうい}に知{し}らせてすぐ拭{ふ}き取{と}り、滑{すべ}らないようにします。',
        'Oil on the floor causes slipping and falling accidents. Alert others nearby and wipe it clean immediately to ensure safety.',
      ),
    },
    {
      q: t(
        '工場{こうじょう}の「非常口{ひじょうぐち}」の前{まえ}に、空{あ}いたパレットが積{つ}まれていました。どうすべきですか？',
        'Empty pallets are stacked in front of the factory emergency exit. What should be done?',
      ),
      options: [
        t(
          '扉{とびら}が半分{はんぶん}開{あ}くなら問題{もんだい}ないと判断{はんだん}する',
          'Decide it is fine as long as the door can half open',
        ),
        t(
          '終業{しゅうぎょう}時間{じかん}までそのままにしておく',
          'Leave them there until the end of the shift',
        ),
        t(
          '火災{かさい}や地震{じしん}の避難{ひなん}を妨{さまた}げるため、直{ただ}ちに移動{いどう}させる',
          'Move them immediately because they obstruct evacuation during fire or earthquake',
        ),
        t(
          'パレットの上{うえ}を登{のぼ}って逃{に}げられるので放置{ほうち}する',
          'Leave them assuming people can climb over the pallets to escape',
        ),
      ],
      answer: 2,
      why: t(
        '非常口{ひじょうぐち}や避難通路{ひなんつうろ}の前{まえ}に物{もの}を置{お}くことは、法律{ほうりつ}（安衛則{あんえいそく}・消防法{しょうぼうほう}）で禁止{きんし}されています。',
        'Placing items in front of emergency exits or evacuation routes is prohibited by law. Clear them immediately for emergency egress.',
      ),
    },
    {
      q: t(
        '「5S」の「整理{せいり}」の正{ただ}しい意味{いみ}はどれですか？',
        'What is the correct meaning of "Sort" (Seiri) in 5S?',
      ),
      options: [
        t(
          '散{ち}らかった物{もの}を見{み}えない箱{はこ}の中{なか}へ隠{かく}すこと',
          'Hiding scattered items inside boxes out of sight',
        ),
        t(
          'すべての物{もの}をきれいに水洗{みずあら}いすること',
          'Washing all items thoroughly with water',
        ),
        t(
          '使{つか}う予定{よてい}のない物{もの}も将来{しょうらい}のために取{と}っておくこと',
          'Keeping items you have no plans to use just for the future',
        ),
        t(
          '必要{ひつよう}な物{もの}と不要{ふよう}な物{もの}を分{わ}け、不要{ふよう}な物{もの}を捨{す}てること',
          'Separating needed items from unneeded ones and discarding the unneeded',
        ),
      ],
      answer: 3,
      why: t(
        '「整理{せいり}」とは要{い}る物{もの}と要{い}らない物{もの}を明確{めいかく}に区分{くぶん}し、不要{ふよう}な物{もの}を作業場{さぎょうば}から処分{しょぶん}することです。',
        'Sort means distinguishing between needed and unneeded items, and removing unneeded items from the workspace completely.',
      ),
    },
    {
      q: t(
        '「整頓{せいとん}」を進{すす}めるとき、工具{こうぐ}の管理{かんり}で最{もっと}も効果的{こうかてき}な方法{ほうほう}はどれですか？',
        'When implementing "Set in order" (Seiton), what is the most effective method for tool management?',
      ),
      options: [
        t(
          '置{お}き場所{ばしょ}に工具{こうぐ}の形{かたち}を描{えが}く「姿置{すがたお}き」をして定位置{ていいち}を決{き}める',
          'Mark silhouettes on the board ("shadow board") to define fixed positions',
        ),
        t(
          '大{おお}きな箱{はこ}に全{すべ}ての工具{こうぐ}をまとめて入{い}れておく',
          'Throwing all tools together into one big box',
        ),
        t(
          '作業者{さぎょうしゃ}がそれぞれ好{す}きな場所{ばしょ}に保管{ほかん}する',
          'Letting each worker store tools wherever they like',
        ),
        t(
          '紛失{ふんしつ}を防{ふせ}ぐため工具{こうぐ}に鍵{かぎ}をかけて誰{だれ}も使{つか}えなくする',
          'Locking up tools so nobody can use them to prevent loss',
        ),
      ],
      answer: 0,
      why: t(
        '「姿置{すがたお}き」をすると、工具{こうぐ}が戻{もど}っているか一目{ひとめ}で分{わ}かり、探{さが}す時間{じかん}や紛失{ふんしつ}、置{お}き忘{わす}れを防{ふせ}げます。',
        'Shadow boards allow anyone to see at a glance if a tool is returned, preventing search time, loss, and forgotten tools.',
      ),
    },
    {
      q: t(
        '清掃{せいそう}は「点検{てんけん}」でもあると言{い}われます。それはなぜですか？',
        'Why is cleaning said to be equivalent to "inspection"?',
      ),
      options: [
        t(
          '掃除{そうじ}をすると時間{じかん}がつぶれて休憩{きゅうけい}できるから',
          'Because cleaning kills time and gives you a break',
        ),
        t(
          '手{て}で触{ふ}れて拭{ふ}くことでボルトの緩{ゆる}みや油漏{あぶらも}れに気{き}づけるから',
          'Because touching and wiping lets you notice loose bolts and fluid leaks',
        ),
        t(
          'きれいにしておけば機械{きかい}が絶対{ぜったい}に故障{こしょう}しなくなるから',
          'Because keeping it clean guarantees the machine will never break',
        ),
        t(
          '上司{じょうし}に掃除{そうじ}している姿{すがた}を見{み}せられるから',
          'Because it shows the supervisor that you are busy cleaning',
        ),
      ],
      answer: 1,
      why: t(
        '清掃{せいそう}しながら機械{きかい}に触{ふ}れることで、異常{いじょう}な発熱{はつねつ}・油漏{あぶらも}れ・ネジの緩{ゆる}みなどの初期{しょき}トラブルを発見{はっけん}できます。',
        'Touching the machine while cleaning allows you to detect early problems such as overheating, fluid leaks, and loose screws.',
      ),
    },
    {
      q: t(
        '作業場{さぎょうば}の通路{つうろ}を横切{よこぎ}って電動{でんどう}工具{こうぐ}のコードをつなぐ必要{ひつよう}があります。どうすべきですか？',
        'You need to run a power tool cable across a workshop walkway. What should you do?',
      ),
      options: [
        t(
          '短時間{たんじかん}なら人{ひと}が通{とお}ってもそのまま床{ゆか}に置{お}いてよい',
          'Leave it on the floor as is if it is only for a short time',
        ),
        t(
          'コードの上{うえ}に段{だん}ボールを乗{の}せて隠{かく}す',
          'Cover and hide the cord with cardboard sheets',
        ),
        t(
          '配線{はいせん}プロテクター（保護{ほご}カバー）で覆{おお}うか、頭上{ずじょう}を通{とお}す',
          'Cover with a cable protector ramp or route it overhead',
        ),
        t(
          '走{はし}って通{とお}るよう周囲{しゅうい}に大声{おおごえ}で注意{ちゅうい}する',
          'Shout at people to run across the cord quickly',
        ),
      ],
      answer: 2,
      why: t(
        '露出{ろしゅつ}したコードは足{あし}を引{ひ}っ掛{か}けて転倒{てんとう}する原因{げんいん}になります。必{かなら}ずカバーで保護{ほご}するか頭上{ずじょう}配線{はいせん}にします。',
        'Exposed cords cause severe trip-and-fall accidents. Always protect them with floor ramps or route them safely overhead.',
      ),
    },
    {
      q: t(
        '5Sの「清潔{せいけつ}」とはどのような状態{じょうたい}を指{さ}しますか？',
        'What state does "Standardize / Cleanliness" (Seiketsu) refer to in 5S?',
      ),
      options: [
        t(
          '年{ねん}に1回{かい}だけ全員{ぜんいん}で大掃除{おおそうじ}をする状態{じょうたい}',
          'Conducting a big company cleanup only once a year',
        ),
        t(
          '新品{しんぴん}の道具{どうぐ}だけを使{つか}って作業{さぎょう}する状態{じょうたい}',
          'Working exclusively with brand-new equipment and tools',
        ),
        t(
          '白{しろ}い作業服{さぎょうふく}を毎日{まいにち}着替{きが}えること',
          'Changing into a clean white work uniform every day',
        ),
        t(
          '整理{せいり}・整頓{せいとん}・清掃{せいそう}の3Sを徹底{てってい}し、キレイな状態{じょうたい}を保{たも}ち続{つづ}けること',
          'Maintaining the first 3S (Sort, Set in order, Shine) continuously',
        ),
      ],
      answer: 3,
      why: t(
        '「清潔{せいけつ}」とは、整理{せいり}・整頓{せいとん}・清掃{せいそう}を維持{いじ}して、ゴミや汚れのない安全{あんぜん}で衛生的{えいせいてき}な職場{しょくば}を保{たも}つことです。',
        'Standardize / Cleanliness means sustaining the first 3S continuously to keep the workplace safe, clean, and hygienic.',
      ),
    },
    {
      q: t(
        '「躾{しつけ}（習慣化{しゅうかんか}）」を職場{しょくば}で定着{ていちゃく}させるために最{もっと}も大切{たいせつ}なことは何{なに}ですか？',
        'What is the most important thing to establish "Sustain / Habit" (Shitsuke) in the workplace?',
      ),
      options: [
        t(
          '決{き}められたルールを全員{ぜんいん}が言{い}われなくても正{ただ}しく実行{じっこう}すること',
          'Everyone correctly following established safety rules without being told',
        ),
        t(
          '注意{ちゅうい}されたときだけ慌{あわ}てて片付{かたづ}けること',
          'Rushing to clean up only when scolded by the boss',
        ),
        t(
          '自分{じぶん}のやりやすい勝手{かって}なルールを作{つく}って作業{さぎょう}すること',
          'Making up your own convenient rules for working',
        ),
        t(
          '後輩{こうはい}だけに掃除{そうじ}を言{い}いつけて自分{じぶん}はやらないこと',
          'Ordering juniors to do all cleaning while doing nothing yourself',
        ),
      ],
      answer: 0,
      why: t(
        '「躾{しつけ}」とは、安全{あんぜん}ルールや手順{てじゅん}を守{まも}ることが習慣{しゅうかん}となり、無意識{むいしき}でも正{ただ}しく行動{こうどう}できる状態{じょうたい}です。',
        'Sustain means following safety rules and procedures becomes an automatic habit so that everyone acts correctly on instinct.',
      ),
    },
    {
      q: t(
        '高所{こうしょ}作業{さぎょう}の足場{あしば}の上{うえ}で作業{さぎょう}が終{お}わりました。工具{こうぐ}はどう処置{しょち}すべきですか？',
        'Work on an elevated scaffolding platform is finished. How should tools be handled?',
      ),
      options: [
        t(
          '明日{あした}も使{つか}うので足場{あしば}の板{いた}の上{うえ}にそのまま並{なら}べておく',
          'Leave them neatly on the scaffold planks because you will use them tomorrow',
        ),
        t(
          '落下{らっか}防止{ぼうし}のため工具{こうぐ}袋{ぶくろ}に入{い}れて下{した}の保管場所{ほかんばしょ}へ持{も}ち帰{かえ}る',
          'Place them in a tool bag and bring them down to storage to prevent drops',
        ),
        t(
          '足場{あしば}から下{した}の地面{じめん}へ向{む}けて投{な}げ落{お}ろす',
          'Throw them down to the ground below from the scaffold',
        ),
        t(
          '足場{あしば}の手すりに引{ひ}っ掛{か}けてぶら下{さ}げておく',
          'Hang them loosely from the scaffolding handrails',
        ),
      ],
      answer: 1,
      why: t(
        '高所{こうしょ}に工具{こうぐ}を放置{ほうち}すると、振動{しんどう}や接触{せっしょく}で落下{らっか}し下{した}の人{ひと}に直撃{ちょくげき}する危険{きけん}があります。',
        'Leaving tools at heights risks them falling due to vibration or bumps, hitting people below. Always bag and bring them down.',
      ),
    },
    {
      q: t(
        '消火器{しょうかき}の置{お}き場{ば}について、5Sの観点{かんてん}から正{ただ}しい管理{かんり}はどれですか？',
        'Regarding fire extinguisher storage, what is the correct 5S management practice?',
      ),
      options: [
        t(
          '邪魔{じゃま}にならないよう倉庫{そうこ}の奥{おく}深{ふか}くにしまっておく',
          'Store them deep inside a storage room so they do not get in the way',
        ),
        t(
          '雨{あめ}に濡{ぬ}れないよう段{だん}ボール箱{ばこ}を上{うえ}にかぶせておく',
          'Cover them with cardboard boxes so they do not get wet in rain',
        ),
        t(
          '黄色{きいろ}の区画線{くかくせん}の内側{うちがわ}を常{つね}に空{あ}け、すぐ使{つか}える状態{じょうたい}を保{たも}つ',
          'Keep the marked yellow boundary box completely clear so it is ready for immediate use',
        ),
        t(
          '消火器{しょうかき}の横{よこ}にモップやすき間{ま}のゴミを立{た}てかけておく',
          'Lean mops and trash against the side of the fire extinguisher',
        ),
      ],
      answer: 2,
      why: t(
        '初期{しょき}消火{しょうか}のため、消火器{しょうかき}の周囲{しゅうい}には絶対{ぜったい}に物{もの}を置{お}かず、誰{だれ}でも即座{そくざ}に取{と}り出{だ}せるようにします。',
        'For initial fire response, never place obstacles around fire extinguishers so anyone can access them instantly in an emergency.',
      ),
    },
  ],
  glossary: [
    {
      ja: '整理',
      reading: 'せいり',
      en: 'Sort / organization (Seiri)',
      note: t(
        '必要な物と不要な物を分け、不要な物を処分すること。',
        'Separating needed items from unneeded, and discarding the unneeded.',
      ),
    },
    {
      ja: '整頓',
      reading: 'せいとん',
      en: 'Set in order / arrangement (Seiton)',
      note: t(
        '必要な物をいつでも誰でも使えるように定位置に配置すること。',
        'Placing needed items in designated spots so anyone can use them anytime.',
      ),
    },
    {
      ja: '清掃',
      reading: 'せいそう',
      en: 'Shine / cleaning and inspection (Seisou)',
      note: t(
        'ゴミや汚れを清掃しながら、設備の異常を点検すること。',
        'Inspecting equipment for abnormalities while cleaning dirt and debris.',
      ),
    },
    {
      ja: '清潔',
      reading: 'せいけつ',
      en: 'Standardize / cleanliness (Seiketsu)',
      note: t(
        '整理・整頓・清掃の3Sを維持し、職場をきれいに保つこと。',
        'Maintaining the first 3S to keep the workplace safe and hygienic.',
      ),
    },
    {
      ja: '躾',
      reading: 'しつけ',
      en: 'Sustain / discipline and habit (Shitsuke)',
      note: t(
        '決められた安全ルールを守る習慣を身につけること。',
        'Forming a habit of correctly following established safety rules.',
      ),
    },
    {
      ja: '姿置き',
      reading: 'すがたおき',
      en: 'Shadow board / outline placement',
      note: t(
        '工具の形を描き、戻す場所を一目で分かるようにした管理。',
        'Management technique drawing tool outlines so placement is clear at a glance.',
      ),
    },
    {
      ja: '転倒',
      reading: 'てんとう',
      en: 'Trip / slip and fall',
      note: t(
        '段差や床の油、放置された物につまずいて倒れる労働災害。',
        'Workplace accident where one trips or slips over obstacles or oil and falls.',
      ),
    },
    {
      ja: '非常口',
      reading: 'ひじょうぐち',
      en: 'Emergency exit',
      note: t(
        '火災や地震などの緊急時に外へ避難するための出入口。',
        'Doorway for evacuating outside during emergencies such as fires or earthquakes.',
      ),
    },
    {
      ja: '消火器',
      reading: 'しょうかき',
      en: 'Fire extinguisher',
      note: t(
        '火災の初期段階で火を消すための持ち運び式消火器具。',
        'Portable firefighting device for extinguishing fires at an initial stage.',
      ),
    },
    {
      ja: '区画線',
      reading: 'くかくせん',
      en: 'Demarcation line / walkway marking',
      note: t(
        '通路や作業区域、物置場所を明確にする床面の白線や黄色線。',
        'White or yellow floor markings separating walkways, work areas, and storage.',
      ),
    },
    {
      ja: '定位置',
      reading: 'ていいち',
      en: 'Designated fixed position',
      note: t(
        '物や工具を保管するためにあらかじめ決められた場所。',
        'Predefined fixed location for storing specific items or tools.',
      ),
    },
    {
      ja: '油漏れ',
      reading: 'あぶらもれ',
      en: 'Oil leakage',
      note: t(
        '配管や機械の隙間から油が垂れて床を汚す不具合。',
        'Malfunction where oil drips from fittings or machines, contaminating the floor.',
      ),
    },
  ],
}
