import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string, bn: string): Tri => ({ ja, en, bn })

export const machineHazards: CourseData = {
  id: 'machine-hazards',
  category: 'trade',
  level: 2,
  minutes: 20,
  title: t(
    '機械{きかい}の挟{はさ}まれ・巻{ま}き込{こ}まれ防止{ぼうし}',
    'Machine caught-in and entanglement prevention',
    'যন্ত্রপাতিতে আটকে যাওয়া ও জড়িয়ে পড়া প্রতিরোধ',
  ),
  sub: t(
    '回転体{かいてんたい}の素手{すで}作業{さぎょう}、安全{あんぜん}カバー、非常{ひじょう}停止{ていし}とLOTO（施錠{せじょう}・表示{ひょうじ}）を徹底{てってい}する。',
    'Master rotating machinery rules (no gloves), safety guards, emergency stop, and LOTO (lockout/tagout).',
    'ঘূর্ণায়মান অংশে খালি হাতে কাজ, সুরক্ষা গার্ড, জরুরি স্টপ বোতাম ও লোটো (তালা ও ট্যাগ) নিশ্চিত করো।',
  ),
  refs: [
    '労働安全衛生法 第20条（機械等による危険の防止）',
    '労働安全衛生規則 第101条（回転軸・歯車等の覆い等の設置）',
    '労働安全衛生規則 第107条（掃除、給油、検査、修理等の作業時の運転停止）',
    '労働安全衛生規則 第110条（手袋の使用禁止）',
  ],
  panels: [
    {
      who: 'foreman',
      text: t(
        '今日{きょう}は工場{こうじょう}の加工作業場{かこうさぎょうば}で、旋盤{せんばん}や搬送{はんそう}コンベアを使{つか}って作業{さぎょう}を行{おこな}います。',
        'Today in the machine shop, we will work with lathes and conveyor belts.',
        'আজ কারখানার ওয়ার্কশপে আমরা লেদ মেশিন ও কনভেয়ার বেল্ট ব্যবহার করে কাজ করব।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'はい！切粉{きりこ}で手{て}を切{き}らないよう、厚手{あつで}の軍手{ぐんて}を着{つ}けてしっかり作業{さぎょう}します！',
        'Yes! I will wear thick knit gloves so sharp metal chips do not cut my hands!',
        'জি! ধারালো ধাতুর কুচি থেকে হাত বাঁচাতে আমি মোটা সুতি গ্লাভস পরে কাজ শুরু করছি!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'ストップ！旋盤{せんばん}やボール盤{ばん}など回転体{かいてんたい}を扱{あつか}うときは、軍手{ぐんて}や手袋{てぶくろ}は絶対{ぜったい}に禁止{きんし}です！',
        'Stop! When working around rotating machinery like lathes or drills, gloves are strictly prohibited!',
        'থামো! লেদ বা ড্রিল মেশিনের মতো ঘূর্ণায়মান যন্ত্রপাতিতে কাজ করার সময় গ্লাভস পরা সম্পূর্ণ নিষিদ্ধ!',
      ),
    },
    {
      who: 'worker',
      text: t(
        'えっ、手袋{てぶくろ}をしてはいけないのですか？手{て}を保護{ほご}した方{ほう}が安全{あんぜん}だと思{おも}っていました…',
        'Eh, no gloves? I thought protecting my hands with gloves was safer...',
        'অ্যাঁ, গ্লাভস পরা যাবে না? আমি তো ভেবেছিলাম হাত ঢেকে রাখলে বেশি নিরাপদ হবে...',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '手袋{てぶくろ}の繊維{せんい}が回転部{かいてんぶ}に触{ふ}れると一瞬{いっしゅん}で巻{ま}き込{こ}まれ、手{て}や腕{うで}の骨折{こっせつ}や切断{せつだん}につながるからです。素手{すで}で作業{さぎょう}します。',
        'If glove fibers touch a spinning part, they drag your whole hand in instantly, fracturing or severing limbs. Work barehanded.',
        'গ্লাভসের সুতো ঘূর্ণায়মান অংশে লাগলে সাথে সাথে পুরো হাত ভেতরে টেনে নিয়ে হাড় ভাঙা বা হাত কাটার মতো দুর্ঘটনা ঘটায়। খালি হাতে কাজ করতে হবে।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'わかりました！袖口{そでぐち}のボタンや長{なが}い髪{かみ}も巻{ま}き込{こ}まれる危険{きけん}がありますね。しっかり留{と}めます。',
        'Understood! Loose cuffs and long hair could also get caught, right? I will button them tightly.',
        'বুঝেছি! ঢিলেঢালা জামার হাতা বা লম্বা চুলও তো পেঁচিয়ে যেতে পারে। আমি শক্ত করে বোতাম লাগিয়ে নিচ্ছি।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'もしコンベアに材料{ざいりょう}が詰{つ}まったら、機械{きかい}を動{うご}かしたまま手{て}で素早{すばや}く引{ひ}き抜{ぬ}いてもよいですか？',
        'If material jams in the conveyor, is it okay to quickly pull it out with my hand while it runs?',
        'যদি কনভেয়ারে মালপত্র আটকে যায়, তবে কি চালু অবস্থাতেই হাত দিয়ে দ্রুত টেনে বের করা যাবে?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '絶対{ぜったい}にダメです！どんなに急{いそ}いでいても、必{かなら}ず機械{きかい}を止{と}めて主電源{しゅでんげん}を施錠{せじょう}（LOTO）してから治具{じぐ}で取{と}り除{のぞ}きます。',
        'Never! No matter how rushed, always stop the machine, lock out the power (LOTO), and use tools to clear it.',
        'কখনোই না! যত তাড়াহুড়োই থাকুক, মেশিন বন্ধ করে মেইন পাওয়ার লক (LOTO) করতে হবে এবং টুলস দিয়ে পরিষ্কার করতে হবে।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'ラインが遅{おく}れていても、停止{ていし}と施錠{せじょう}を省{はぶ}いてはいけないのですね。非常{ひじょう}停止{ていし}ボタンの位置{いち}も覚{おぼ}えました！',
        'Even if the line is delayed, I must never skip stopping and locking out. I have also memorized the E-stop button location!',
        'লাইন পিছিয়ে থাকলেও মেশিন বন্ধ ও লক করা বাদ দেওয়া যাবে না। আমি জরুরি স্টপ বোতামের অবস্থানও চিনে রেখেছি!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'その通{とお}りです。自分{じぶん}の身{み}を守{まも}るため、指差呼称{しさこしょう}で安全{あんぜん}を確認{かくにん}してから作業{さぎょう}に入{はい}りましょう。',
        'Exactly right. To protect yourself, always confirm safety with pointing and calling before starting work.',
        'একদম ঠিক। নিজের সুরক্ষার জন্য সবসময় পয়েন্টিং অ্যান্ড কলিংয়ের মাধ্যমে নিরাপত্তা নিশ্চিত করে কাজে নামবে।',
      ),
    },
  ],
  handbook: [
    t(
      '旋盤{せんばん}、ボール盤{ばん}、ローラーなどの回転体{かいてんたい}の作業{さぎょう}では、手袋{てぶくろ}（軍手{ぐんて}・皮手袋{かわてぶくろ}）を絶対{ぜったい}に使用{しよう}してはならない。',
      'Never wear gloves (knit or leather) when operating machines with rotating parts like lathes, drills, or rollers.',
      'লেদ, ড্রিল বা রোলারের মতো ঘূর্ণায়মান যন্ত্রপাতিতে কাজ করার সময় কোনো ধরনের গ্লাভস (সুতি বা চামড়া) পরা সম্পূর্ণ নিষিদ্ধ।',
    ),
    t(
      '作業服{さぎょうふく}の袖口{そでぐち}や裾{すそ}のボタンを留{と}め、上着{うわぎ}の裾{すそ}をズボンに入{い}れ、長{なが}い髪{かみ}は保護帽{ほごぼう}に完全{かんぜん}に収{おさ}める。',
      'Button shirt cuffs and hems, tuck shirt tails into pants, and tie back long hair completely inside your safety cap.',
      'কাজের পোশাকের হাতা ও নিচের বোতাম শক্ত করে আটকাও, জামা প্যান্টে ইন করো এবং লম্বা চুল ক্যাপের ভেতরে সম্পূর্ণ ঢেকে রাখো।',
    ),
    t(
      '機械{きかい}の安全{あんぜん}カバーや防護柵{ぼうごさく}、インターロックを外{はず}したり、無効化{むこうか}した状態{じょうたい}で運転{うんてん}してはならない。',
      'Never operate machinery with safety covers, protective fences, or interlocks removed or bypassed.',
      'নিরাপত্তা কভার, বেড়া বা ইন্টারলক খুলে ফেলে বা নিষ্ক্রিয় করে কখনো কোনো মেশিন চালাবে না।',
    ),
    t(
      '詰{つ}まり処理{しょり}、清掃{せいそう}、給油{きゅうゆ}、点検{てんけん}を行{おこな}う際{さい}は、必{かなら}ず機械{きかい}を完全{かんぜん}に停止{ていし}させる。',
      'Always stop the machine completely before clearing jams, cleaning, lubricating, or inspecting.',
      'জ্যাম ছাড়ানো, পরিষ্কার করা, তেল দেওয়া বা পরীক্ষার সময় মেশিন পুরোপুরি বন্ধ করা বাধ্যতামূলক।',
    ),
    t(
      '点検{てんけん}や異物{いぶつ}除去{じょきょ}の際{さい}は、主電源{しゅでんげん}をOFFにして南京錠{なんきんじょう}で施錠{せじょう}し、操作{そうさ}禁止{きんし}の安全{あんぜん}タグを掲示{けいじ}する（LOTO）。',
      'When inspecting or clearing objects, switch off the main breaker, lock it with a padlock, and attach a DO NOT OPERATE tag (LOTO).',
      'পরীক্ষা বা জ্যাম সরানোর সময় প্রধান ব্রেকার বন্ধ করে প্যাডলক দিয়ে তালা লাগাও এবং "চালানো নিষেধ" ট্যাগ ঝোলাও (লোটো)।',
    ),
    t(
      '機械{きかい}のスイッチを切{き}った直後{ちょくご}の惰力{だりょく}回転{かいてん}している部分{ぶぶん}には、完全{かんぜん}に停止{ていし}するまで手{て}を触{ふ}れてはならない。',
      'Never touch parts coasting down by inertia after turning off the switch until they come to a complete standstill.',
      'সুইচ বন্ধ করার পর জড়তার কারণে ঘুরতে থাকা অংশে সম্পূর্ণ থামা পর্যন্ত কখনোই হাত দেবে না।',
    ),
    t(
      '詰{つ}まった材料{ざいりょう}や切粉{きりこ}を取{と}り除{のぞ}くときは、素手{すで}を使{つか}わず、トングや掻{か}き出{だ}し棒{ぼう}などの専用{せんよう}治具{じぐ}を使用{しよう}する。',
      'Never use bare hands to remove jammed materials or metal chips; always use dedicated tools like tongs or hooks.',
      'আটকে থাকা মালপত্র বা ধাতুর কুচি সরাতে কখনো খালি হাত ব্যবহার করবে না; চিমটা বা রডের মতো নির্দিষ্ট টুলস ব্যবহার করবে।',
    ),
    t(
      '研削{けんさく}といしは使用前{しようまえ}に木槌{きづち}等{とう}で軽{かる}く叩{たた}いて打音{だおん}点検{てんけん}を行{おこな}い、澄{す}んだ音{おと}がしてひび割{わ}れがないことを確認{かくにん}する。',
      'Before using a grinding wheel, lightly tap it with a wooden mallet for a ring test to confirm a clear sound free of cracks.',
      'গ্রাইন্ডিং হুইল ব্যবহারের আগে কাঠের হাতুড়ি দিয়ে হালকা টোকা দিয়ে রিং টেস্ট করো এবং পরিষ্কার শব্দের মাধ্যমে নিশ্চিত হও যে কোনো ফাটল নেই।',
    ),
    t(
      '研削{けんさく}といしの試運転{しうんてん}は、その日{ひ}の作業{さぎょう}開始前{かいしまえ}に1分間{ぷんかん}以上{いじょう}、といし取替{とりか}え後{ご}に3分間{ぷんかん}以上{いじょう}行{おこな}う。', // verify: 研削といしの試運転時間は労働安全衛生規則第119条で始業前1分以上・取替え後3分以上と規定
      'Conduct a trial run for grinding wheels: at least 1 minute before starting work each day, and at least 3 minutes after replacing the wheel.',
      'গ্রাইন্ডিং হুইলের ট্রায়াল রান চালাও: প্রতিদিন কাজ শুরুর আগে কমপক্ষে ১ মিনিট এবং হুইল পরিবর্তনের পর কমপক্ষে ৩ মিনিট।',
    ),
    t(
      '非常{ひじょう}停止{ていし}ボタンの周囲{しゅうい}に物{もの}を置{お}かず、異常{いじょう}や危険{きけん}を感{かん}じたら直{ただ}ちに躊躇{ちゅうちょ}なく押{お}す。',
      'Keep the area around the emergency stop button clear of obstructions, and hit it immediately without hesitation if hazard arises.',
      'জরুরি স্টপ বোতামের চারপাশে কোনো বাধা রাখবে না এবং কোনো বিপদ বা অসঙ্গতি দেখলে দ্বিধা না করে সাথে সাথে বোতাম চাপবে।',
    ),
  ],
  reel: [
    t(
      '回転部{かいてんぶ}、手袋{てぶくろ}なし、素手{すで}、ヨシ！',
      'Rotating parts, no gloves, bare hands, yoshi!',
      'ঘূর্ণায়মান অংশ, গ্লাভস নেই, খালি হাত, ঠিক আছে!',
    ),
    t(
      '作業服{さぎょうふく}の袖口{そでぐち}、ボタン留{と}め、ヨシ！',
      'Workwear cuffs, buttoned tight, yoshi!',
      'পোশাকের হাতা, বোতাম আটকানো, ঠিক আছে!',
    ),
    t(
      '長{なが}い髪{かみ}、保護帽{ほごぼう}の中{なか}、ヨシ！',
      'Long hair, tucked inside cap, yoshi!',
      'লম্বা চুল, ক্যাপের ভেতরে রাখা, ঠিক আছে!',
    ),
    t(
      '安全{あんぜん}カバー・インターロック、装着{そうちゃく}、ヨシ！',
      'Safety guards and interlocks, in place, yoshi!',
      'সুরক্ষা গার্ড ও ইন্টারলক, লাগানো আছে, ঠিক আছে!',
    ),
    t(
      '非常{ひじょう}停止{ていし}ボタンの位置{いち}、確認{かくにん}、ヨシ！',
      'Emergency stop button position, checked, yoshi!',
      'জরুরি স্টপ বোতামের অবস্থান, নিশ্চিত, ঠিক আছে!',
    ),
    t(
      '詰{つ}まり処理{しょり}、電源{でんげん}OFF・LOTO施錠{せじょう}、ヨシ！',
      'Clearing jams, power OFF and LOTO locked, yoshi!',
      'জ্যাম ছাড়ানো, পাওয়ার বন্ধ ও লোটো তালা লাগানো, ঠিক আছে!',
    ),
    t(
      '研削{けんさく}といし、打音{だおん}点検{てんけん}・澄{す}んだ音{おと}、ヨシ！',
      'Grinding wheel, ring test clear sound, yoshi!',
      'গ্রাইন্ডিং হুইল, শব্দ পরীক্ষায় পরিষ্কার আওয়াজ, ঠিক আছে!',
    ),
    t(
      '始業前{しぎょうまえ}試運転{しうんてん}、1分間{ぷんかん}、ヨシ！', // verify: 研削といしの始業前試運転時間は1分以上
      'Pre-work trial run, 1 minute, yoshi!',
      'কাজ শুরুর ট্রায়াল রান, ১ মিনিট সম্পন্ন, ঠিক আছে!',
    ),
  ],
  drills: [
    {
      id: 'machine-kyt-spot',
      kind: 'spot',
      secs: 30,
      need: 5,
      name: t(
        '工作{こうさく}機械{きかい}・コンベアの危険{きけん}予知{よち}（KYT）',
        'Machine shop hazard spotting (KYT)',
        'মেশিন শপ বিপদ শনাক্তকরণ (KYT)',
      ),
      goal: t(
        '30秒{びょう}で機械{きかい}作業{さぎょう}に潜{ひそ}む危険{きけん}な状態{じょうたい}を5つ見{み}つける。',
        'Find 5 hazardous conditions in the machinery area within 30 seconds.',
        '৩০ সেকেন্ডের মধ্যে মেশিন শপের ৫টি বিপজ্জনক অবস্থা চিহ্নিত করো।',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '作業者{さぎょうしゃ}が軍手{ぐんて}を着{つ}けたままボール盤{ばん}で金属{きんぞく}の穴{あな}あけ作業{さぎょう}をしている',
            'Worker operating a drilling machine while wearing knit work gloves',
            'কর্মী সুতি গ্লাভস পরে ড্রিল মেশিনে ধাতু ছিদ্র করার কাজ করছে',
          ),
        },
        {
          hazard: true,
          label: t(
            'ベルトコンベアの駆動{くどう}ローラー安全{あんぜん}カバーが外{はず}されたまま運転{うんてん}している',
            'Belt conveyor running with drive roller safety guard removed',
            'ড্রাইভ রোলারের সুরক্ষা কভার খোলা রেখেই কনভেয়ার বেল্ট চালানো হচ্ছে',
          ),
        },
        {
          hazard: false,
          label: t(
            '卓上{たくじょう}グラインダーのといし覆{おお}いと安全{あんぜん}シールドが正{ただ}しく固定{こてい}されている',
            'Bench grinder wheel guard and transparent safety shield are properly secured',
            'বেঞ্চ গ্রাইন্ডারের হুইল গার্ড ও স্বচ্ছ সেফটি শিল্ড সঠিকভাবে লাগানো আছে',
          ),
        },
        {
          hazard: true,
          label: t(
            '作業服{さぎょうふく}の袖口{そでぐち}のボタンが外{はず}れてだらりと垂{た}れ下{さ}がっている',
            'Work jacket sleeve button is undone and cuff hangs loose',
            'কাজের জ্যাকেটের হাতার বোতাম খোলা এবং হাতা আলগাভাবে ঝুলছে',
          ),
        },
        {
          hazard: false,
          label: t(
            '機械{きかい}の詰{つ}まり処理{しょり}のため主電源{しゅでんげん}スイッチに南京錠{なんきんじょう}で施錠{せじょう}されている',
            'Main power switch is padlocked for machine jam maintenance',
            'মেশিনের জ্যাম সরানোর কাজের জন্য প্রধান পাওয়ার সুইচে তালা লাগানো আছে',
          ),
        },
        {
          hazard: true,
          label: t(
            'ライン稼働中{かどうちゅう}に手{て}を伸{の}ばして詰{つ}まった材料{ざいりょう}を引っ張{ひっぱ}り出{だ}そうとしている',
            'Reaching into running production line to pull out jammed material by hand',
            'চালু প্রোডাকশন লাইনের ভেতরে হাত ঢুকিয়ে আটকে থাকা মালপত্র টেনে বের করার চেষ্টা করছে',
          ),
        },
        {
          hazard: false,
          label: t(
            '長{なが}い髪{かみ}が後{うし}ろで束{たば}ねられ保護帽{ほごぼう}（ヘルメット）の中{なか}に収{おさ}められている',
            'Long hair is tied back and fully tucked under the safety helmet',
            'লম্বা চুল পেছনে বেঁধে সেফটি হেলমেটের ভেতরে সম্পূর্ণ গুটিয়ে রাখা আছে',
          ),
        },
        {
          hazard: true,
          label: t(
            '非常{ひじょう}停止{ていし}ボタンの正面{しょうめん}に段{だん}ボール箱{ばこ}が高{たか}く積{つ}まれて隠{かく}れている',
            'Cardboard boxes stacked high directly blocking the emergency stop button',
            'জরুরি স্টপ বোতামের সামনে কার্ডবোর্ডের বাক্স উঁচিয়ে রেখে বোতাম ঢেকে ফেলা হয়েছে',
          ),
        },
        {
          hazard: false,
          label: t(
            '旋盤{せんばん}の切粉{きりこ}を素手{すで}ではなく専用{せんよう}の掻{か}き出{だ}しフック治具{じぐ}で清掃{せいそう}している',
            'Cleaning lathe metal chips using a dedicated clearing hook tool, not bare hands',
            'লেদ মেশিনের ধাতুর কুচি খালি হাতে না ধরে নির্দিষ্ট হুক টুল দিয়ে পরিষ্কার করা হচ্ছে',
          ),
        },
      ],
    },
    {
      id: 'machine-loto-jam-seq',
      kind: 'seq',
      name: t(
        '詰{つ}まり処理{しょり}とLOTO（施錠{せじょう}・表示{ひょうじ}）安全{あんぜん}手順{てじゅん}',
        'Jam clearing and LOTO (lockout/tagout) safe sequence',
        'জ্যাম ছাড়ানো ও লোটো (তালা ও ট্যাগ) নিরাপদ ক্রম',
      ),
      goal: t(
        'コンベアや回転機{かいてんき}の詰{つ}まりを安全{あんぜん}に除去{じょきょ}する手順{てじゅん}を覚{おぼ}える。',
        'Master the step-by-step procedure to safely clear jams in conveyors and machinery.',
        'কনভেয়ার ও মেশিনের জ্যাম নিরাপদে সরানোর সঠিক ধাপগুলো আয়ত্ত করো।',
      ),
      steps: [
        t(
          '機械{きかい}の停止{ていし}ボタンを押{お}し、完全{かんぜん}に動作{どうさ}を停止{ていし}させる',
          'Press the machine stop button and bring all motion to a complete stop',
          'মেশিনের স্টপ বোতাম চেপে সমস্ত ঘূর্ণন ও চলাচল পুরোপুরি বন্ধ করো',
        ),
        t(
          '主電源{しゅでんげん}ブレーカーをOFFにし、操作盤{そうさばん}に南京錠{なんきんじょう}で施錠{せじょう}する（ロックアウト）',
          'Switch OFF the main power breaker and lock the panel with a padlock (lockout)',
          'প্রধান পাওয়ার ব্রেকার বন্ধ করো এবং কন্ট্রোল প্যানেলে প্যাডলক দিয়ে তালা লাগাও (লকআউট)',
        ),
        t(
          'スイッチ部{ぶ}に「点検中{てんけんちゅう}・操作{そうさ}禁止{きんし}」の安全{あんぜん}タグを取{と}り付{つ}ける（タグアウト）',
          'Attach a DO NOT OPERATE / UNDER INSPECTION safety tag to the switch (tagout)',
          'সুইচে "চালানো নিষেধ / পরিদর্শন চলছে" নিরাপত্তা ট্যাগ ঝুলিয়ে দাও (ট্যাগআউট)',
        ),
        t(
          '機械{きかい}の残圧{ざんあつ}を抜{ぬ}き、惰力{だりょく}回転{かいてん}が完全{かんぜん}に止{と}まったことを指差呼称{しさこしょう}で確認{かくにん}する',
          'Release residual pressure and confirm complete stop of coasting parts by pointing and calling',
          'অবশিষ্ট বাতাস বা হাইড্রোলিক চাপ বের করো এবং জড়তায় ঘোরা বন্ধ হয়েছে কিনা তা হাত দিয়ে নির্দেশ করে মুখে বলো',
        ),
        t(
          '専用{せんよう}の治具{じぐ}（トングや掻{か}き出{だ}し棒{ぼう}）を使{つか}って詰{つ}まった異物{いぶつ}を除去{じょきょ}する',
          'Remove the jammed object using dedicated tools such as tongs or clearing hooks',
          'চিমটা বা হুকের মতো নির্দিষ্ট টুলস ব্যবহার করে আটকে থাকা বস্তু বের করে আনো',
        ),
      ],
    },
    {
      id: 'grinding-wheel-ring-test',
      kind: 'ring',
      wheels: [true, false, true, true, false],
      name: t(
        '研削{けんさく}といしの打音{だおん}点検{てんけん}（リングテスト）',
        'Grinding wheel ring test',
        'গ্রাইন্ডিং হুইল শব্দ পরীক্ষা (রিং টেস্ট)',
      ),
      goal: t(
        'といしを木製{もくせい}の柄{え}で軽{かる}く叩{たた}き、澄{す}んだ音{おと}（良品{りょうひん}）と鈍{にぶ}い音{おと}（ひび割{わ}れ）を聞{き}き分{わ}ける。',
        'Lightly tap the wheel with a wooden handle to distinguish clear ringing (sound) from dull thuds (cracked).',
        'হুইলে কাঠের হাতল দিয়ে হালকা টোকা দিয়ে পরিষ্কার সুরযুক্ত শব্দ (ভালো) ও ভোঁতা শব্দ (ফাটা) আলাদা করতে শেখো।',
      ),
    },
    {
      id: 'machine-emergency-stop',
      kind: 'reflex',
      name: t(
        '非常{ひじょう}停止{ていし}の反射{はんしゃ}動作{どうさ}',
        'Emergency stop reflex reaction',
        'জরুরি স্টপ রিফ্লেক্স অ্যাকশন',
      ),
      goal: t(
        '巻{ま}き込{こ}まれや挟{はさ}まれの瞬間{しゅんかん}！0.4秒{びょう}以内{いない}に非常{ひじょう}停止{ていし}ボタンを押{お}す。',
        'Caught-in or entanglement hazard! Hit the emergency stop button within 0.4 seconds.',
        'জড়িয়ে পড়ার চরম বিপদ! ০.৪ সেকেন্ডের মধ্যে জরুরি স্টপ বোতাম চাপো।',
      ),
    },
  ],
  inspect: [
    {
      id: 'conveyor-guard-inspect',
      defective: true,
      name: t(
        'ベルトコンベア安全{あんぜん}防護{ぼうご}カバーの点検{てんけん}',
        'Belt conveyor safety guard inspection',
        'কনভেয়ার বেল্ট সুরক্ষা কভার পরিদর্শন',
      ),
      spots: [
        {
          x: 30,
          y: 35,
          check: t(
            '回転部{かいてんぶ}安全{あんぜん}カバーの固定{こてい}状態{じょうたい}：隙間{すきま}なくボルトで確実{かくじつ}に締{し}め付{つ}けられているか（良品{りょうひん}：完全{かんぜん}に覆{おお}われ固定{こてい} / 不良{ふりょう}：ボルトが外{はず}れてカバーが脱落{だつらく}し回転{かいてん}ローラーが露出{ろしゅつ}）',
            'Rotating part safety guard securement: Fully fastened with bolts with no gap (Good: Firmly covered and fixed / Defective: Missing bolts, guard loose or fallen exposing roller)',
            'ঘূর্ণায়মান অংশের সুরক্ষা গার্ডের অবস্থা: কোনো ফাঁক না রেখে নাট-বোল্ট শক্তভাবে আটকানো (ভালো: সম্পূর্ণ ঢাকা ও মজবুত / ত্রুটি: বোল্ট নেই বা কভার খসে রোলার উন্মুক্ত)',
          ),
        },
        {
          x: 70,
          y: 65,
          check: t(
            '非常{ひじょう}停止{ていし}ロープ・スイッチの作動{さどう}：コンベア全周{ぜんしゅう}の引き紐{ひきひも}を引{ひ}いて即座{そくざ}に停止{ていし}するか（良品{りょうひん}：引{ひ}くと即時{そくじ}停止{ていし} / 不良{ふりょう}：ロープが緩{ゆる}んで作動{さどう}しない）',
            'Emergency stop pull-cord switch operation: Pulling the cord along the conveyor stops it immediately (Good: Stops instantly when pulled / Defective: Slack cord fails to trigger stop)',
            'জরুরি স্টপ দড়ি ও সুইচের কার্যকারিতা: কনভেয়ারের তার বা দড়ি টানলে সাথে সাথে মেশিন বন্ধ হয় (ভালো: টানেই সাথে সাথে বন্ধ / ত্রুটি: দড়ি ঢিলে হয়ে কাজ না করা)',
          ),
        },
      ],
    },
    {
      id: 'bench-grinder-inspect',
      defective: false,
      name: t(
        '卓上{たくじょう}グラインダーの始業前{しぎょうまえ}点検{てんけん}',
        'Bench grinder pre-operation inspection',
        'বেঞ্চ গ্রাইন্ডারের কাজ শুরুর আগের পরিদর্শন',
      ),
      spots: [
        {
          x: 25,
          y: 30,
          check: t(
            'といし覆{おお}いとワークレストの間隔{かんかく}：ワークレストとといしの隙間{すきま}が3mm以内{いない}に調整{ちょうせい}されているか（良品{りょうひん}：隙間{すきま}3mm以内{いない}でがたつきなし / 不良{ふりょう}：隙間{すきま}が3mmを超{こ}えて材料{ざいりょう}が挟{はさ}まる危険{きけん}）', // verify: 研削盤のワークレスト隙間は3mm以内、調整片隙間は3-10mm（労働安全衛生規則）
            'Grinding wheel guard and work rest clearance: Gap between work rest and wheel maintained within 3 mm (Good: Within 3 mm, rigid / Defective: Gap exceeds 3 mm risking workpiece pinch)',
            'হুইল গার্ড ও ওয়ার্ক রেস্টের ফাঁক: ওয়ার্ক রেস্ট ও হুইলের মধ্যবর্তী ফাঁক ৩ মিলিমিটারের মধ্যে আছে কিনা (ভালো: ৩ মিমি বা কম, শক্ত / ত্রুটি: ৩ মিমি বেশি ফাঁক যাতে মালপত্র আটকে যায়)',
          ),
        },
        {
          x: 75,
          y: 45,
          check: t(
            '飛散{ひさん}防止{ぼうし}透明{とうめい}シールド：火花{ひばな}除{よ}けシールドに割{わ}れや著{いちじる}しい曇{くも}りがなく正{ただ}しい位置{いち}にあるか（良品{りょうひん}：視界{しかい}良好{りょうこう}でしっかり固定{こてい} / 不良{ふりょう}：破損{はそん}や汚れで視界{しかい}不良{ふりょう}）',
            'Spark shield transparency and position: Spark protection shield crack-free, clear, and positioned correctly (Good: Clean visibility, firmly set / Defective: Broken or obscure)',
            'ফুলকি প্রতিরোধী স্বচ্ছ শিল্ড: ফুলকি আটকানোর স্বচ্ছ কাভার ভাঙা নেই এবং সঠিক অবস্থানে আছে (ভালো: পরিষ্কার দৃশ্যমানতা ও মজবুত / ত্রুটি: ফাটা বা ময়লায় ঘোলাটে)',
          ),
        },
        {
          x: 50,
          y: 80,
          check: t(
            '始業前{しぎょうまえ}の試運転{しうんてん}（から回転{かいてん}）：1分間{ぷんかん}のから回転{かいてん}で異音{いおん}や激{はげ}しい振動{しんどう}がないか（良品{りょうひん}：異音{いおん}なく滑{なめ}らかに回転{かいてん} / 不良{ふりょう}：異常{いじょう}な異音{いおん}やガタつきが発生{はっせい}）', // verify: 研削といしの始業前試運転時間は1分以上
            'Pre-work trial run: Run unloaded for at least 1 minute checking for abnormal noise or heavy vibration (Good: Smooth quiet rotation / Defective: Severe rattle or wobble)',
            'কাজ শুরুর ট্রায়াল রান: কোনো কাজ ছাড়া ১ মিনিট চালিয়ে অস্বাভাবিক শব্দ বা কাঁপন আছে কিনা দেখা (ভালো: মসৃণ ও শান্ত ঘূর্ণন / ত্রুটি: বিকট শব্দ বা অতিরিক্ত কাঁপন)',
          ),
        },
      ],
    },
    {
      id: 'emergency-stop-inspect',
      defective: true,
      name: t(
        '機械{きかい}操作盤{そうさばん}・非常{ひじょう}停止{ていし}ボタンの点検{てんけん}',
        'Machine control panel and emergency stop button inspection',
        'মেশিন কন্ট্রোল প্যানেল ও জরুরি স্টপ বোতাম পরিদর্শন',
      ),
      spots: [
        {
          x: 35,
          y: 40,
          check: t(
            '非常{ひじょう}停止{ていし}ボタンの前方{ぜんぽう}視認性{しにんせい}：ボタンの正面{しょうめん}や手前{てまえ}に障害物{しょうがいぶつ}がないか（良品{りょうひん}：障害物{しょうがいぶつ}がなく即座{そくざ}に押{お}せる / 不良{ふりょう}：手前{てまえ}に段{だん}ボールや材料{ざいりょう}が置{お}かれてボタンが隠{かく}れている）',
            'Emergency stop button visibility and access: No obstacles in front of button (Good: Completely clear for immediate hit / Defective: Blocked by boxes or materials)',
            'জরুরি স্টপ বোতামের দৃশ্যমানতা ও প্রবেশের সুবিধা: বোতামের সামনে কোনো বাধা নেই (ভালো: সম্পূর্ণ ফাঁকা ও দ্রুত চাপার উপযোগী / ত্রুটি: বাক্স বা মালামাল দিয়ে ঢাকা)',
          ),
        },
        {
          x: 65,
          y: 65,
          check: t(
            'キノコ型{がた}押しボタンスイッチの動作{どうさ}：ボタンを押{お}し込{こ}んだときロック状態{じょうたい}が維持{いじ}されるか（良品{りょうひん}：押{お}すとロック保持{ほじ}され停止{ていし} / 不良{ふりょう}：押{お}してもロックされず勝手{かって}に戻{もど}る）',
            'Mushroom pushbutton latching action: Pressing button locks in depressed state maintaining stop (Good: Positively latches stop / Defective: Springs back without locking)',
            'মাশরুম আকৃতির পুশ বোতামের ল্যাচিং ক্রিয়া: চাপ দিলে আটকে থেকে বন্ধ অবস্থা বজায় থাকে কিনা (ভালো: চেপে লক হয়ে বন্ধ থাকে / ত্রুটি: চাপলে আটকে না থেকে নিজে থেকেই উঠে আসে)',
          ),
        },
      ],
    },
  ],
  quiz: [
    {
      q: t(
        '旋盤{せんばん}やボール盤{ばん}など、高速{こうそく}で回転{かいてん}する刃物{はもの}や機械{きかい}を操作{そうさ}する際{さい}の手袋{てぶくろ}着用{ちゃくよう}について、正{ただ}しいものはどれですか？',
        'Which is correct regarding wearing gloves when operating high-speed rotating machinery such as lathes or drill presses?',
        'লেদ বা ড্রিল মেশিনের মতো দ্রুত ঘূর্ণায়মান যন্ত্রপাতিতে কাজ করার সময় গ্লাভস পরা সম্পর্কে কোনটি সঠিক?',
      ),
      options: [
        t(
          '回転体{かいてんたい}の作業{さぎょう}では、繊維{せんい}が巻{ま}き込{こ}まれる危険{きけん}があるため手袋{てぶくろ}は絶対{ぜったい}に着用{ちゃくよう}せず素手{すで}で行{おこな}う',
          'Never wear gloves around rotating parts; work barehanded because fabric fibers can get caught and drag your hand in',
          'ঘূর্ণায়মান অংশে কখনো গ্লাভস পরা যাবে না; খালি হাতে কাজ করতে হবে কারণ সুতো পেঁচিয়ে পুরো হাত ভেতরে টেনে নিতে পারে',
        ),
        t(
          '手{て}の油{あぶら}や汗{あせ}で滑{すべ}らないよう、厚手{あつで}のゴム手袋{てぶくろ}を着用{ちゃくよう}して作業{さぎょう}する',
          'Wear thick rubber gloves to prevent slipping from hand sweat or machine oil',
          'ঘাম বা মেশিনের তেলের পিচ্ছিল ভাব এড়াতে মোটা রাবার গ্লাভস পরে কাজ করা উচিত',
        ),
        t(
          '金属{きんぞく}の切粉{きりこ}から手{て}を守{まも}るため、新品{しんぴん}の軍手{ぐんて}を二重{にじゅう}に着{つ}けて作業{さぎょう}する',
          'Wear two pairs of brand-new knit gloves to protect hands from sharp metal chips',
          'ধাতুর তীক্ষ্ণ কুচি থেকে হাত বাঁচাতে নতুন সুতি গ্লাভস জোড়া করে পরা উচিত',
        ),
        t(
          '革手袋{かわてぶくろ}であれば巻{ま}き込{こ}まれても破{やぶ}れにくいので着用{ちゃくよう}が推奨{すいしょう}される',
          'Leather gloves are recommended because they resist tearing even if touched by moving parts',
          'চামড়ার গ্লাভস সহজে ছেঁড়ে না বলে ঘূর্ণায়মান অংশে এটি পরা উৎসাহিত করা হয়',
        ),
      ],
      answer: 0,
      why: t(
        '労働安全衛生規則{ろうどうあんぜんえいせいきそく}第{だい}110条{じょう}により、ボール盤{ばん}や旋盤{せんばん}など刃物{はもの}が回転{かいてん}する作業{さぎょう}での手袋{てぶくろ}着用{ちゃくよう}は禁止{きんし}されています。繊維{せんい}が引{ひ}っ掛{か}かると手{て}や腕{うで}全体{ぜんたい}が一瞬{いっしゅん}で巻{ま}き込{こ}まれるためです。',
        'Under Ordinance on Industrial Safety and Health Art. 110, gloves are strictly prohibited when operating rotating machinery. If fibers catch, limbs are instantly dragged in.',
        'পেশাগত নিরাপত্তা বিধিমালার ১১০ ধারা অনুযায়ী ঘূর্ণায়মান কাটার বা যন্ত্রপাতিতে গ্লাভস পরা সম্পূর্ণ নিষিদ্ধ। সুতো আটকে গেলে চোখের পলকে পুরো হাত ভেতরে টেনে নিয়ে যায়।',
      ),
    },
    {
      q: t(
        '稼働中{かどうちゅう}のベルトコンベアで荷物{にもつ}や段{だん}ボールが斜{なな}めに引っ掛{ひっか}かり、ラインが停止{ていし}しそうになりました。直{ただ}ちに取{と}るべき正{ただ}しい行動{こうどう}はどれですか？',
        'Packages got jammed on a running conveyor and the production line is about to stall. What is the correct immediate action?',
        'চলন্ত কনভেয়ার বেল্টে একটি প্যাকেট বাঁকা হয়ে আটকে গেছে এবং লাইন বন্ধ হওয়ার উপক্রম হয়েছে। সাথে সাথে কী করা উচিত?',
      ),
      options: [
        t(
          'ラインが遅{おく}れるのを防{ふせ}ぐため、動{うご}いたままの手{て}で素早{すばや}く箱{ばこ}を引{ひ}っ張{ぱ}り直{なお}す',
          'To prevent line delays, quickly pull and straighten the box with bare hands while running',
          'লাইনের গতি বজায় রাখতে চালু অবস্থাতেই হাত দিয়ে দ্রুত টেনে বাক্সটি সোজা করে দেওয়া',
        ),
        t(
          'コンベアの運転{うんてん}停止{ていし}ボタンを押{お}し、完全{かんぜん}に停止{ていし}したことを確認{かくにん}してから直{なお}す',
          'Hit the conveyor stop button, confirm complete stop, and then clear or adjust the item',
          'কনভেয়ারের স্টপ বোতাম চেপে পুরোপুরি থামা নিশ্চিত করে তারপর বাক্সটি ঠিক করা',
        ),
        t(
          '足{あし}で箱{ばこ}を強{つよ}く蹴{け}り飛{と}ばしてコンベアの進路{しんろ}を元{もと}に戻{もど}す',
          'Kick the package hard with safety boots to knock it back into the conveyor path',
          'পায়ের সেফটি জুতো দিয়ে জোরে লাথি মেরে বাক্সটি কনভেয়ারের লাইনে ঠেলে দেওয়া',
        ),
        t(
          '隣{となり}の作業員{さぎょういん}に服{ふく}を持{も}ってもらいながら、身{み}を乗{の}り出{だ}して手{て}で引{ひ}っ張{ぱ}る',
          'Have a coworker hold your clothes while you lean far in to pull it free with hands',
          'পাশের সহকর্মীকে জামা ধরে রাখতে বলে শরীর ঝুঁকিয়ে হাত দিয়ে টেনে ছাড়ানো',
        ),
      ],
      answer: 1,
      why: t(
        '稼働中{かどうちゅう}のコンベアに手{て}を伸{の}ばすと、プーリーやローラーに手{て}が挟{はさ}まれて重大{じゅうだい}災害{さいがい}になります。どんなに急{いそ}いでいても、必{かなら}ず運転{うんてん}を停止{ていし}させてから作業{さぎょう}します。',
        'Reaching into a moving conveyor leads to severe pinch injuries in pulleys and rollers. Always stop the conveyor completely before touching jammed items.',
        'চলন্ত কনভেয়ারের ভেতরে হাত দিলে পুলি বা রোলারে হাত আটকে মারাত্মক দুর্ঘটনা ঘটে। যত তাড়াহুড়োই থাকুক, মেশিন পুরোপুরি থামিয়ে কাজ করতে হবে।',
      ),
    },
    {
      q: t(
        '機械{きかい}の定期{ていき}点検{てんけん}や内部{ないぶ}の清掃{せいそう}を行{おこな}う際{さい}の「LOTO（ロックアウト・タグアウト）」の手順{てじゅん}として、最も{もっとも}適切{てきせつ}なものはどれですか？',
        'Which is the most appropriate procedure for Lockout/Tagout (LOTO) when inspecting or cleaning inside machinery?',
        'মেশিনের ভেতরের অংশ পরীক্ষা বা পরিষ্কার করার সময় "লোটো (লকআউট ট্যাগআউট)" এর সবচেয়ে সঠিক পদ্ধতি কোনটি?',
      ),
      options: [
        t(
          'スイッチを切{き}るだけで、施錠{せじょう}やタグ表示{ひょうじ}は行{おこな}わずに作業{さぎょう}に入{はい}る',
          'Simply flip the control switch off, without locking or tagging out, and begin work',
          'শুধু সুইচে অফ করে কোনো তালা বা ট্যাগ না লাগিয়েই কাজে হাত দেওয়া',
        ),
        t(
          '他{ほか}の人{ひと}に口頭{こうとう}で「掃除{そうじ}する」と一度{いちど}伝{つた}えれば、電源{でんげん}を切{き}らずに作業{さぎょう}してよい',
          'Telling coworkers verbally once is enough, so work can proceed without cutting power',
          'অন্যদের মুখে একবার "পরিষ্কার করছি" বলে জানালে পাওয়ার বন্ধ না করলেও চলে',
        ),
        t(
          '主電源{しゅでんげん}を遮断{しゃだん}し、操作部{そうさぶ}を南京錠{なんきんじょう}で施錠{せじょう}して「点検中{てんけんちゅう}・操作{そうさ}禁止{きんし}」のタグを掲示{けいじ}する',
          'Shut off main breaker, lock it with a padlock, and hang a DO NOT OPERATE warning tag',
          'প্রধান পাওয়ার ব্রেকার বন্ধ করে প্যাডলক দিয়ে তালা লাগানো এবং "চালানো নিষেধ" সতর্কবার্তা ট্যাগ ঝুলানো',
        ),
        t(
          'ブレーカーのスイッチに触{ふ}れられないよう、ガムテープを貼{は}るだけで十分{じゅうぶん}である',
          'Taping over the breaker switch with packaging tape is sufficient protection',
          'ব্রেকারের ওপর প্যাকিং টেপ সেঁটে দিলেই যথেষ্ট সুরক্ষা পাওয়া যায়',
        ),
      ],
      answer: 2,
      why: t(
        'LOTOは他{ほか}の作業員{さぎょういん}が誤{あやま}って機械{きかい}を起動{きどう}するのを物理的{ぶつりてき}に防{ふせ}ぐため、主電源{しゅでんげん}を施錠{せじょう}（ロックアウト）し、操作{そうさ}禁止{きんし}タグ（タグアウト）を取{と}り付{つ}けることが必須{ひっす}です。',
        'LOTO physically prevents other workers from mistakenly restarting machines during maintenance by padlocking breakers and displaying warning tags.',
        'লোটোর মূল উদ্দেশ্য হলো রক্ষণাবেক্ষণের সময় অন্য কেউ ভুল করে মেশিন চালু করা রোধ করতে ব্রেকারে তালা দেওয়া ও সতর্কীকরণ ট্যাগ লাগানো।',
      ),
    },
    {
      q: t(
        '回転機械{かいてんきかい}（旋盤{せんばん}やフライス盤{ばん}）のある作業場{さぎょうば}での服装{ふくそう}や身{み}だしなみについて、正{ただ}しいものはどれですか？',
        'Which is correct regarding clothing and personal appearance in a workshop with rotating machinery?',
        'ঘূর্ণায়মান যন্ত্রপাতি (লেদ বা মিলিং মেশিন) থাকা ওয়ার্কশপে পোশাক ও ব্যক্তিগত পরিচ্ছন্নতা বিষয়ে কোনটি সঠিক?',
      ),
      options: [
        t(
          '暑{あつ}い日{ひ}は作業服{さぎょうふく}の前{まえ}ボタンを全開{ぜんかい}にして風通{かぜとお}しをよくする',
          'On hot days, leave work jacket buttons fully open for ventilation',
          'গরমের দিনে বাতাসের জন্য কাজের জ্যাকেটের সব বোতাম খুলে রাখা',
        ),
        t(
          '汗{あせ}を拭{ふ}きやすいよう、首{くび}に長{なが}いタオルを巻{ま}いて作業{さぎょう}する',
          'Wrap a long towel around your neck to easily wipe away sweat during work',
          'ঘাম মোছার সুবিধার জন্য গলায় লম্বা তোয়ালে ঝুলিয়ে কাজ করা',
        ),
        t(
          '手首{てくび}の動きを楽{らく}にするため、袖口{そでぐち}のボタンは外{はず}して広{ひろ}げておく',
          'Leave cuff buttons undone and flared out for free wrist movement',
          'কবজির সহজে নড়াচড়ার সুবিধার্থে হাতার বোতাম খুলে ঢিলে রাখা',
        ),
        t(
          '袖口{そでぐち}のボタンを留{と}め、上着{うわぎ}の裾{すそ}はズボンに入{い}れ、長{なが}い髪{かみ}は保護帽{ほごぼう}の中{なか}に収{おさ}める',
          'Button shirt cuffs, tuck jacket hem into trousers, and tuck long hair fully inside your safety cap',
          'পোশাকের হাতার বোতাম লাগানো, জামা প্যান্টে ইন করা এবং লম্বা চুল সেফটি ক্যাপের ভেতর ঢেকে রাখা',
        ),
      ],
      answer: 3,
      why: t(
        'たるんだ袖口{そでぐち}、首{くび}に巻{ま}いたタオル、垂{た}らした髪{かみ}は回転軸{かいてんじく}に引{ひ}っ掛{か}かりやすく、一瞬{いっしゅん}で身体{しんたい}ごと巻{ま}き込{こ}まれる危険{きけん}があります。',
        'Loose cuffs, neck towels, and hanging hair get caught on rotating shafts instantly dragging the worker in, causing fatal accidents.',
        'আলগা হাতা, গলায় জড়ানো তোয়ালে বা খোলা চুল দ্রুত ঘূর্ণায়মান শ্যাফটে আটকে চোখের পলকে পুরো শরীর ভেতরে টেনে নিয়ে মারাত্মক বিপদ ঘটায়।',
      ),
    },
    {
      q: t(
        '研削{けんさく}といし（グラインダーのといし）を取り替{とりか}える際{さい}、取{と}り付{つ}け前{まえ}に行{おこな}う「打音{だおん}点検{てんけん}」の正{ただ}しい方法{ほうほう}はどれですか？',
        'What is the correct procedure for the ring test before mounting a new grinding wheel?',
        'নতুন গ্রাইন্ডিং হুইল লাগানোর আগে রিং টেস্ট বা শব্দ পরীক্ষার সঠিক পদ্ধতি কোনটি?',
      ),
      options: [
        t(
          'といしを指{ゆび}で吊{つ}るし、木製{もくせい}の柄{え}などで軽{かる}く叩{たた}いて澄{す}んだ音{おと}がするか確認{かくにん}する',
          'Suspend the wheel on a finger and lightly tap it with a wooden handle to check for a clear ring',
          'আঙুলে হুইল ঝুলিয়ে কাঠের হাতল দিয়ে হালকা টোকা দিয়ে পরিষ্কার সুরযুক্ত শব্দ হয় কিনা পরীক্ষা করা',
        ),
        t(
          '金槌{かなづち}で力強{ちからづよ}く叩{たた}いて、といしが割{わ}れないか耐久性{たいきゅうせい}を試{ため}す',
          'Hit the wheel forcefully with a steel hammer to test its fracture resistance',
          'লোহার হাতুড়ি দিয়ে জোরে পিটিয়ে দেখা যে হুইলটি শক্ত কিনা এবং ভাঙে কিনা',
        ),
        t(
          '水{みず}につけて気泡{きほう}が出{で}るかどうかでひび割{わ}れを点検{てんけん}する',
          'Submerge the grinding wheel in water to see if bubbles emerge from cracks',
          'হুইলটি পানিতে ডুবিয়ে কোনো ফাটল থেকে বুদ্বুদ বের হয় কিনা তা দেখা',
        ),
        t(
          '目視{もくし}で傷{きず}がなければ叩{たた}いて点検{てんけん}する必要{ひつよう}はない',
          'If no visible scratches appear to the naked eye, tapping is unnecessary',
          'চোখে কোনো দাগ না দেখা গেলে টোকা দিয়ে পরীক্ষা করার কোনো প্রয়োজন নেই',
        ),
      ],
      answer: 0,
      why: t(
        '研削{けんさく}といしにひび割{わ}れがあると、高速{こうそく}回転{かいてん}中{ちゅう}に破壊{はかい}して破片{はへん}が飛散{ひさん}し命{いのち}に関{かか}わります。木製{もくせい}の柄{え}で軽{かる}く叩{たた}き、澄{す}んだ音{おと}なら良品{りょうひん}、鈍{にぶ}い音{おと}なら不良品{ふりょうひん}と判断{はんだん}します。',
        'A cracked grinding wheel can burst under centrifugal force, scattering fatal high-speed fragments. A clear ring indicates sound structure; a dull thud means cracked.',
        'ফাটা হুইল দ্রুত ঘোরার সময় ভেঙে টুকরো ছিটকে এসে প্রাণঘাতী আঘাত করতে পারে। কাঠের টোকায় পরিষ্কার শব্দ হলে ভালো, ভোঁতা শব্দ হলে বাতিল করতে হবে।',
      ),
    },
    {
      q: t(
        '労働安全衛生規則{ろうどうあんぜんえいせいきそく}に基{づ}く、研削{けんさく}といしの「試運転{しうんてん}（から回転{かいてん}）」の時間{じかん}について正{ただ}しいものはどれですか？',
        'Which is correct regarding statutory trial run durations for grinding wheels under the Ordinance on Industrial Safety and Health?',
        'শিল্প নিরাপত্তা বিধিমালা অনুযায়ী গ্রাইন্ডিং হুইলের ট্রায়াল রান চালানোর সময়ের নিয়ম কোনটি?',
      ),
      options: [
        t(
          '始業前{しぎょうまえ}は試運転{しうんてん}不要{ふよう}で、といし取替{とりか}え後{ご}のみ10秒間{びょうかん}行{おこな}う',
          'No trial run needed before work; only run for 10 seconds after replacement',
          'কাজ শুরুর আগে কোনো ট্রায়াল দরকার নেই; শুধু পরিবর্তনের পর ১০ সেকেন্ড চালানো',
        ),
        t(
          'その日{ひ}の作業{さぎょう}開始前{かいしまえ}は1分間{ぷんかん}以上{いじょう}、といし取替{とりか}え後{ご}は3分間{ぷんかん}以上{いじょう}行{おこな}う', // verify: 研削といしの試運転時間は労働安全衛生規則第119条で始業前1分以上・取替え後3分以上と規定
          'At least 1 minute before starting work each day, and at least 3 minutes after replacing the wheel',
          'প্রতিদিন কাজ শুরুর আগে কমপক্ষে ১ মিনিট এবং হুইল পরিবর্তনের পর কমপক্ষে ৩ মিনিট চালানো',
        ),
        t(
          '作業{さぎょう}開始前{かいしまえ}も取替{とりか}え後{ご}も、一律{いちりつ}30分間{ぷんかん}試運転{しうんてん}しなければならない',
          'Uniformly run 30 minutes of trial operation both before work and after replacement',
          'কাজ শুরুর আগে ও পরিবর্তনের পর উভয় ক্ষেত্রেই ৩০ মিনিট করে ট্রায়াল দেওয়া বাধ্যতামূলক',
        ),
        t(
          '異常{いじょう}な音{おと}がしなければ試運転{しうんてん}は行{おこな}わずにすぐ加工{かこう}してよい',
          'Skip trial runs and grind immediately as long as no odd sound is heard at startup',
          'চালুর সময় কোনো শব্দ না থাকলে কোনো ট্রায়াল ছাড়াই সাথে সাথে গ্রাইন্ডিং শুরু করা যায়',
        ),
      ],
      answer: 1,
      why: t(
        '労働安全衛生規則{ろうどうあんぜんえいせいきそく}第{だい}119条{じょう}により、研削{けんさく}といしはその日{ひ}の作業{さぎょう}開始前{かいしまえ}に1分間{ぷんかん}以上{いじょう}、といしを取{と}り替{か}えたときは3分間{ぷんかん}以上{いじょう}の試運転{しうんてん}が義務付{ぎむづ}けられています。',
        'Under Ordinance Art. 119, trial runs are legally required: 1+ minute before starting work daily, and 3+ minutes after wheel replacement.',
        'বিধিমালার ১১৯ ধারা অনুযায়ী প্রতিদিন কাজ শুরুর আগে ১ মিনিট বা তার বেশি এবং নতুন হুইল লাগানোর পর ৩ মিনিট বা তার বেশি ট্রায়াল চালানো আইনগতভাবে বাধ্যতামূলক।',
      ),
    },
    {
      q: t(
        '機械{きかい}の危険部{きけんぶ}に設置{せっち}されている「インターロック（連動{れんどう}装置{そうち}）」や安全{あんぜん}カバーの取扱{とりあつか}いとして、正{ただ}しいものはどれですか？',
        'Which is correct regarding machine safety guards and interlock devices?',
        'মেশিনের বিপজ্জনক অংশের সুরক্ষা কভার ও সেফটি ইন্টারলক পরিচালনা বিষয়ে কোনটি সঠিক?',
      ),
      options: [
        t(
          'カバーを外{はず}した方{ほう}が視界{しかい}がよく作業{さぎょう}が速{はや}くなるので、自由{じゆう}に外{はず}してよい',
          'Remove guards freely to improve visibility and increase production speed',
          'কাজের সুবিধার্থে ও পরিষ্কার দেখার জন্য সুরক্ষা কভার ইচ্ছেমতো খুলে ফেলা যায়',
        ),
        t(
          'インターロックスイッチに針金{はりがね}を巻{ま}いて、ドアを開{あ}けたまま動{うご}くように改造{かいぞう}する',
          'Tie wire around the interlock switch so the machine runs with the door open',
          'ইন্টারলক সুইচে তার পেঁচিয়ে দরজা খোলা অবস্থাতেই মেশিন চলার ব্যবস্থা করা',
        ),
        t(
          '安全{あんぜん}カバーやインターロックを絶対{ぜったい}に無効化{むこうか}せず、正常{せいじょう}に作動{さどう}する状態{じょうたい}で運転{うんてん}する',
          'Never bypass safety covers or interlocks; only operate machines when safety devices work normally',
          'সুরক্ষা কভার বা ইন্টারলক কখনোই নিষ্ক্রিয় করবে না; সব সুরক্ষা ডিভাইস সক্রিয় থাকলেই কেবল মেশিন চালাবে',
        ),
        t(
          '故障{こしょう}してインターロックが効{き}かなくなっても、職長{しょくちょう}に報告{ほうこく}せずそのまま作業{さぎょう}を続{つづ}ける',
          'Continue working silently without reporting even if the interlock malfunctions',
          'ইন্টারলক নষ্ট হয়ে কাজ না করলেও ফোরম্যানকে না জানিয়ে সেভাবেই কাজ চালিয়ে যাওয়া',
        ),
      ],
      answer: 2,
      why: t(
        'インターロックはガードを開{あ}けると自動的{じどうてき}に動勢{どうせい}を止{と}める重要{じゅうよう}な安全{あんぜん}装置{そうち}です。これを無効化{むこうか}したりカバーを外{はず}して運転{うんてん}することは厳禁{げんきん}です。',
        'Interlocks immediately cut power when protective doors open. Tampering with or bypassing safety guards is strictly forbidden.',
        'ইন্টারলক হলো দরজা খুললেই স্বয়ংক্রিয়ভাবে মেশিন থামিয়ে দেওয়ার একটি অত্যাবশ্যকীয় নিরাপত্তা ব্যবস্থা। এটি নিষ্ক্রিয় করে কাজ করা কঠোরভাবে নিষিদ্ধ।',
      ),
    },
    {
      q: t(
        '高速{こうそく}回転{かいてん}する丸{まる}のこ盤{ばん}やプレス機{き}で、停止{ていし}スイッチをOFFにした直後{ちょくご}の対応{たいおう}として正{ただ}しいものはどれですか？',
        'What is the correct action immediately after switching OFF machines with high inertia, like circular saws or presses?',
        'সার্কুলার স বা প্রেস মেশিনের মতো যন্ত্রপাতির সুইচ বন্ধ করার ঠিক পরপরই কোন পদক্ষেপটি সঠিক?',
      ),
      options: [
        t(
          'スイッチがOFFになっていれば、まだ回転{かいてん}していても手{て}で触{ふ}れてよい',
          'Touch moving parts freely as long as the electrical switch is in the OFF position',
          'সুইচ অফ করা থাকলে মেশিন তখনও ঘুরতে থাকলেও হাত দিতে কোনো বাধা নেই',
        ),
        t(
          '回転{かいてん}が止{と}まるのを待{ま}つのは時間{じかん}の無駄{むだ}なので、木片{もくへん}を刃{は}に強{つよ}く押{お}し当{あ}てて無理{むり}やり止{と}める',
          'Force the blade to a stop by jamming a piece of wood against it to save time',
          'সময় বাঁচাতে ব্লেডের গায়ে কাঠের টুকরো জোরে চেপে ধরে জোর করে ঘোরানো বন্ধ করা',
        ),
        t(
          '厚手{あつで}の手袋{てぶくろ}を手{て}にはめて、直接{ちょくせつ}刃物{はもの}を握{にぎ}って制動{せいどう}をかける',
          'Grip spinning parts with thick gloves to brake the coasting motion',
          'ঘূর্ণন থামাতে মোটা গ্লাভস পরা হাত দিয়ে সরাসরি ব্লেড শক্ত করে চেপে ধরা',
        ),
        t(
          '惰力{だりょく}回転{かいてん}が完全{かんぜん}に停止{ていし}するまで、危険{きけん}区域{くいき}に手{て}や身体{しんたい}を絶対{ぜったい}に入{い}れない',
          'Keep hands and body completely out of hazardous zones until coasting rotation comes to a dead stop',
          'জড়তার ঘূর্ণন পুরোপুরি বন্ধ না হওয়া পর্যন্ত বিপজ্জনক অংশে কখনোই হাত বা শরীর দেবে না',
        ),
      ],
      answer: 3,
      why: t(
        'スイッチをOFFにしても、慣性{かんせい}によって刃物{はもの}はしばらく回転{かいてん}を続{つづ}けます（惰力{だりょく}回転{かいてん}）。完全{かんぜん}に回転{かいてん}がゼロになるまで手{て}を出{だ}してはいけません。',
        'Even after switching off, blades coast down by inertia. Never reach into machine hazard zones until parts come to an absolute standstill.',
        'সুইচ বন্ধ করলেও জড়তার কারণে যন্ত্র কিছুক্ষণ ঘুরতে থাকে। ঘূর্ণন পুরোপুরি শূন্যে না পৌঁছানো পর্যন্ত ভেতরে হাত দেওয়া নিষিদ্ধ।',
      ),
    },
    {
      q: t(
        'プレス機械{きかい}などの「両手{りょうて}操作式{そうさしき}安全{あんぜん}装置{そうち}」について、正{ただ}しい使用{しよう}方法{ほうほう}はどれですか？',
        'Which is the correct method for using two-hand safety controls on press machinery?',
        'প্রেস মেশিনের মতো যন্ত্রপাতিতে "দুই হাত চালিত সুরক্ষা বোতাম" ব্যবহারের সঠিক নিয়ম কোনটি?',
      ),
      options: [
        t(
          '危険{きけん}限界{げんかい}に両手{りょうて}が入{はい}らないよう、左右{さゆう}の押しボタンを必{かなら}ず両手{りょうて}で同時{どうじ}に押{お}して操作{そうさ}する',
          'Operate by pressing both left and right buttons simultaneously with both hands to ensure hands stay clear',
          'বিপজ্জনক অংশে হাত ঢুকতে না দিতে উভয় হাত দিয়ে একসাথে ডানে ও বামের বোতাম চেপে মেশিন চালানো',
        ),
        t(
          '片手{かたて}が自由{じゆう}になるよう、片方{かたほう}のボタンをテープで押{お}したまま固定{こてい}して作業{さぎょう}する',
          'Tape one button down continuously so you can operate the press with only one hand',
          'এক হাত ফাঁকা রাখার সুবিধার্থে একটি বোতাম টেপ দিয়ে চেপে আটকে রেখে অন্য হাতে কাজ করা',
        ),
        t(
          '足{あし}でボタンを踏{ふ}めるようにボタンを床{ゆか}に移設{いせつ}して作業{さぎょう}する',
          'Relocate buttons to the floor so you can press them with feet instead of hands',
          'হাতের বদলে পা দিয়ে চাপার জন্য বোতামগুলো মেঝের ওপর নামিয়ে নেওয়া',
        ),
        t(
          'ボタンを押{お}すのが面倒{めんどう}なときは、両手{りょうて}ボタンを連動{れんどう}解除{かいじょ}して自動{じどう}運転{うんてん}にする',
          'Disconnect the two-hand interlock and switch to automatic mode when tired of pressing',
          'বারবার চাপতে কষ্ট হলে দুই হাতের ইন্টারলক সংযোগ খুলে অটোমেটিক মোডে রাখা',
        ),
      ],
      answer: 0,
      why: t(
        '両手{りょうて}操作式{そうさしき}は、両手{りょうて}でボタンを押{お}していないとスライドが作動{さどう}しないことで、手{て}の挟{はさ}まれを防{ふせ}ぐ装置{そうち}です。片手{かたて}固定{こてい}などの不正{ふせい}操作{そうさ}は絶対{ぜったい}にしてはなりません。',
        'Two-hand controls prevent hands from being crushed in the die by requiring both hands on the pushbuttons during the stroke. Tampering is strictly prohibited.',
        'দুই হাত বোতামে না থাকলে প্রেস মেশিনের ডাই নিচে নামবে না—এই নিয়মে দুই হাত বোতামে রাখতে হয়। একটি বোতাম টেপ দিয়ে আটকে রাখা মারাত্মক অন্যায়।',
      ),
    },
    {
      q: t(
        '旋盤{せんばん}やフライス盤{ばん}での金属{きんぞく}加工中{かこうちゅう}、工作物{こうさくぶつ}に長{なが}く巻{ま}き付{つ}いた切粉{きりこ}（切りくず）の除去{じょきょ}方法{ほうほう}として正{ただ}しいものはどれですか？',
        'How should long tangled metal chips be safely removed during lathe or milling machining?',
        'লেদ বা মিলিং মেশিনে ধাতু কাটার সময় জট পাকিয়ে যাওয়া ধাতুর কুচি বা ফালি কীভাবে নিরাপদে সরানো উচিত?',
      ),
      options: [
        t(
          '加工{かこう}速度{そくど}を落{お}とさず、運転中{うんてんちゅう}に素手{すで}で素早{すばや}く引{ひ}っ張{ぱ}り取{と}る',
          'Quickly snatch chips away with bare hands while spinning to avoid slowing cycle times',
          'কাজের গতি বজায় রাখতে চালু অবস্থাতেই খালি হাতে দ্রুত টেনে ধাতুর কুচি বের করে ফেলা',
        ),
        t(
          '機械{きかい}の回転{かいてん}を完全{かんぜん}に停止{ていし}させ、掻{か}き出{だ}し用{よう}トングやフックなどの治具{じぐ}を使{つか}って除去{じょきょ}する',
          'Stop machine rotation completely and clear chips using dedicated tools like tongs or hooks',
          'মেশিনের ঘূর্ণন পুরোপুরি বন্ধ করো এবং চিমটা বা হুকের মতো নির্দিষ্ট টুলস ব্যবহার করে কুচি সরাও',
        ),
        t(
          '手袋{てぶくろ}をはめて、切粉{きりこ}を強{つよ}く掴{つか}んで一気{いっき}に引{ひ}き剥{は}がす',
          'Put on gloves, grasp chips firmly, and rip them off in one forceful motion',
          'গ্লাভস পরে ধাতুর কুচি শক্ত করে ধরে এক টানে ছিঁড়ে ফেলা',
        ),
        t(
          '口{くち}で息{いき}を強{つよ}く吹{ふ}きかけて、切粉{きりこ}を周囲{しゅうい}に飛{と}ばす',
          'Blow hard with your breath to scatter the hot chips away from the workpiece',
          'মুখ দিয়ে জোরে ফুঁ দিয়ে কাজের বস্তু থেকে ধাতুর কুচি দূরে উড়িয়ে দেওয়া',
        ),
      ],
      answer: 1,
      why: t(
        '回転中{かいてんちゅう}の切粉{きりこ}を手{て}で触{ふ}ると切{き}り傷{きず}だけでなく回転体{かいてんたい}へ引{ひ}き込{こ}まれる原因{げんいん}になります。必{かなら}ず停止{ていし}させ、治具{じぐ}を使用{しよう}します。',
        'Touching metal chips during rotation causes severe lacerations and risks dragging hands into rotating parts. Always stop the spindle and use clearing tools.',
        'ঘূর্ণায়মান অবস্থায় ধাতুর কুচি হাত দিয়ে ধরলে হাত কাটার পাশাপাশি ঘূর্ণায়মান অংশে হাত পেঁচিয়ে মারাত্মক দুর্ঘটনা ঘটে। অবশ্যই মেশিন থামিয়ে টুলস ব্যবহার করতে হবে।',
      ),
    },
    {
      q: t(
        '同僚{どうりょう}の作業服{さぎょうふく}の袖{そで}が機械{きかい}の回転軸{かいてんじく}に引{ひ}っ掛{か}かり、巻{ま}き込{こ}まれそうになっているのを目撃{もくげき}しました。最初{さいしょ}にすべきことは何{なん}ですか？',
        'You witness a coworker\'s sleeve snagging on a rotating shaft and starting to drag them in. What should you do first?',
        'আপনি দেখলেন একজন সহকর্মীর পোশাকের হাতা একটি ঘূর্ণায়মান শ্যাফটে আটকে তাকে ভেতরে টেনে নিচ্ছে। সবার আগে আপনার কী করা উচিত?',
      ),
      options: [
        t(
          '走{はし}って工場長{こうじょうちょう}の事務所{じむしょ}に行{い}き、指示{しじ}を仰{あお}ぐ',
          'Run to the factory manager\'s office to report and seek instructions',
          'ছুটে ফ্যাক্টরি ম্যানেজারের অফিসে গিয়ে কী করবেন তার নির্দেশনা চাওয়া',
        ),
        t(
          '引{ひ}っ張{ぱ}られている服{ふく}を手{て}で逆{ぎゃく}方向{ほうこう}に全力{ぜんりょく}で引{ひ}っ張{ぱ}り返{かえ}す',
          'Grab the trapped clothing and pull back with all your strength against the machine',
          'আটকে যাওয়া পোশাক ধরে নিজের সমস্ত শক্তি দিয়ে উল্টো দিকে টেনে রাখা',
        ),
        t(
          '最寄{もよ}りの非常{ひじょう}停止{ていし}ボタン（非常{ひじょう}停止{ていし}スイッチ）を直{ただ}ちに躊躇{ちゅうちょ}なく押{お}す',
          'Hit the nearest emergency stop button immediately without hesitation',
          'দ্বিধা না করে সাথে সাথে সবচেয়ে কাছের জরুরি স্টপ বোতামটি চেপে দেওয়া',
        ),
        t(
          'スマートフォンで状況{じょうきょう}を撮影{さつえい}して記録{きろく}を残{のこ}す',
          'Take photos on your smartphone to document evidence of the incident',
          'ঘটনার প্রমাণ সংরক্ষণের জন্য স্মার্টফোনে ভিডিও বা ছবি তোলা শুরু করা',
        ),
      ],
      answer: 2,
      why: t(
        '巻{ま}き込{こ}まれ事故{じこ}は一瞬{いっしゅん}で身体{しんたい}全体{ぜんたい}に及{およ}びます。何{なに}よりもまず直{ただ}ちに非常{ひじょう}停止{ていし}ボタンを押{お}して動力{どうりょく}を遮断{しゃだん}することが最優先{さいゆうせん}です。',
        'Entanglement accidents progress to fatal injuries in split seconds. Hitting the emergency stop button to cut machine power is the absolute first priority.',
        'মেশিনে জড়িয়ে পড়ার দুর্ঘটনা চোখের পলকে ঘটে যায়। তাই সবার আগে জরুরি স্টপ বোতাম চেপে মেশিনের পাওয়ার বন্ধ করাই প্রধান দায়িত্ব।',
      ),
    },
    {
      q: t(
        '卓上{たくじょう}グラインダー（両頭{りょうとう}研削盤{けんさくばん}）のワークレスト（加工物{かこうぶつ}を載{の}せる台{だい}）とといしの隙間{すきま}の基準{きじゅん}として正{ただ}しいものはどれですか？',
        'What is the statutory standard for clearance between the work rest and grinding wheel on a bench grinder?',
        'বেঞ্চ গ্রাইন্ডারে ওয়ার্ক রেস্ট (কাজের বস্তু রাখার টেবিল) ও হুইলের মধ্যবর্তী ফাঁকের সরকারি নিয়ম কোনটি?',
      ),
      options: [
        t(
          '隙間{すきま}は広{ひろ}い方{ほう}がといしが長持{ながも}ちするので、20mm以上{いじょう}空{あ}ける',
          'A wider gap preserves wheel life, so keep it at 20 mm or more',
          'হুইল বেশিদিন টিকার জন্য বেশি ফাঁক রাখা ভালো, তাই ২০ মিলিমিটার বা বেশি রাখা',
        ),
        t(
          '隙間{すきま}が全く{まったく}ないと危{あぶ}ないので、15mm程度{ていど}に調整{ちょうせい}する',
          'Zero clearance is hazardous, so adjust the gap to about 15 mm',
          'একেবারে ফাঁক না থাকা ক্ষতিকর, তাই আনুমানিক ১৫ মিলিমিটার ফাঁক রাখা',
        ),
        t(
          '指{ゆび}が1本{ぽん}入{はい}るくらい（約{やく}10mm）が最適{さいてき}である',
          'A gap allowing one finger (around 10 mm) is ideal',
          'একটি আঙুল ঢোকার মতো (প্রায় ১০ মিলিমিটার) ফাঁক রাখাই সবচেয়ে আদর্শ',
        ),
        t(
          '加工物{かこうぶつ}が挟{はさ}み込{こ}まれないよう、隙間{すきま}は3mm以内{いない}に調整{ちょうせい}する', // verify: 研削盤のワークレスト隙間は3mm以内、調整片隙間は3-10mm（労働安全衛生規則）
          'Adjust the gap within 3 mm to prevent workpieces from being pinched and dragged in',
          'কাজের বস্তু ভেতরে ঢুকে আটকে যাওয়া প্রতিরোধে ফাঁক অবশ্যই ৩ মিলিমিটারের মধ্যে রাখতে হবে',
        ),
      ],
      answer: 3,
      why: t(
        'ワークレストとといしの隙間{すきま}が広{ひろ}いと、加工物{かこうぶつ}が巻き込{まきこ}まれてといしが破壊{はかい}したり手{て}を大怪我{おおがが}します。隙間{すきま}は必{かなら}ず3mm以内{いない}に保{たも}ちます。',
        'If the work rest gap exceeds 3 mm, workpieces can get wedged, shattering the wheel and causing severe hand injuries. Maintain clearance within 3 mm.',
        'ওয়ার্ক রেস্টের ফাঁক ৩ মিলিমিটারের বেশি হলে কাজের বস্তু ভেতরে ঢুকে হুইল ফেটে যেতে পারে এবং হাতে মারাত্মক চোট লাগতে পারে। তাই ফাঁক ৩ মিমি বা কম রাখা বাধ্যতামূলক।',
      ),
    },
  ],
  glossary: [
    {
      ja: '挟まれ・巻き込まれ',
      reading: 'はさまれ・まきこまれ',
      en: 'caught in / caught between / entanglement',
      bn: 'আটকে যাওয়া ও জড়িয়ে পড়া',
      note: t(
        '機械{きかい}の回転{かいてん}部{ぶ}や可動{かどう}部{ぶ}と固定{こてい}部{ぶ}の隙間{すきま}に手{て}や服{ふく}が引{ひ}き込{こ}まれる労働{ろうどう}災害{さいがい}。',
        'Industrial injury where hands, limbs, or clothing are drawn into moving parts or pinch points.',
        'ঘূর্ণায়মান বা সচল যন্ত্রাংশের খাঁজে হাত, শরীর বা পোশাক আটকে ভেতরে টেনে নেওয়ার দুর্ঘটনা।',
      ),
    },
    {
      ja: '回転体',
      reading: 'かいてんたい',
      en: 'rotating part / rotating machinery',
      bn: 'ঘূর্ণায়মান অংশ',
      note: t(
        '旋盤{せんばん}の主軸{しゅじく}、ボール盤{ばん}の刃具{はぐ}、ローラーなど回転{かいてん}する機械{きかい}の部位{ぶい}。手袋{てぶくろ}着用{ちゃくよう}は厳禁{げんきん}。',
        'Rotating machine parts such as lathe spindles, drill bits, or rollers. Glove usage is strictly prohibited.',
        'লেদ স্পিন্ডল, ড্রিল বিট বা রোলারের মতো ঘূর্ণনশীল যন্ত্রাংশ। এতে গ্লাভস পরা কঠোরভাবে নিষিদ্ধ।',
      ),
    },
    {
      ja: '軍手',
      reading: 'ぐんて',
      en: 'knit cotton work gloves',
      bn: 'সুতি কাজের গ্লাভস',
      note: t(
        '作業用{さぎょうよう}の編{あ}み手袋{てぶくろ}。糸{いと}が回転部{かいてんぶ}に引{ひ}っ掛{か}かりやすいため、回転体{かいてんたい}の作業{さぎょう}では絶対{ぜったい}に使用{しよう}禁止{きんし}。',
        'Woven fabric work gloves. Because fibers readily snag on rotating shafts, never wear them around moving machinery.',
        'সুতির বোনা কাজের গ্লাভস। সুতো ঘূর্ণায়মান অংশে সহজেই পেঁচিয়ে যেতে পারে বলে মেশিনে এটি পরা সম্পূর্ণ নিষিদ্ধ।',
      ),
    },
    {
      ja: '安全カバー',
      reading: 'あんぜんかばー',
      en: 'safety guard / machine cover',
      bn: 'নিরাপত্তা গার্ড / কভার',
      note: t(
        'ベルト、ギア、といしなどの危険{きけん}な露出{ろしゅつ}部分{ぶぶん}を覆{おお}い、身体{しんたい}の接触{せっしょく}を防{ふせ}ぐ防護{ぼうご}器具{きぐ}。',
        'Protective enclosure covering dangerous exposed mechanisms like belts, gears, and wheels to prevent contact.',
        'বেল্ট, গিয়ার ও হুইলের মতো বিপজ্জনক অংশ ঢেকে রেখে স্পর্শ প্রতিরোধ করার সুরক্ষামূলক আবরণ।',
      ),
    },
    {
      ja: 'インターロック',
      reading: 'いんたーろっく',
      en: 'safety interlock',
      bn: 'সেফটি ইন্টারলক',
      note: t(
        '安全{あんぜん}カバーや扉{とびら}が開{あ}いている間{あいだ}は機械{きかい}が起動{きどう}しない連動{れんどう}安全{あんぜん}機構{きこう}。',
        'Safety mechanism that automatically cuts power and stops the machine when guards or doors are opened.',
        'সুরক্ষা দরজা বা কভার খোলা থাকলে মেশিন স্বয়ংক্রিয়ভাবে বন্ধ রাখার বা চালু হতে না দেওয়ার ব্যবস্থা।',
      ),
    },
    {
      ja: 'ロックアウト・タグアウト',
      reading: 'ろっくあうと・たぐあうと',
      en: 'lockout / tagout (LOTO)',
      bn: 'লকআউট ট্যাগআউট (লোটো)',
      note: t(
        '点検{てんけん}や詰{つ}まり処理{しょり}時{じ}に主電源{しゅでんげん}を遮断{しゃだん}し、南京錠{なんきんじょう}で施錠{せじょう}して「操作{そうさ}禁止{きんし}」タグを付{つ}ける安全{あんぜん}手順{てじゅん}。',
        'Safety procedure of switching off power, locking switches with padlocks, and attaching DO NOT OPERATE tags during service.',
        'রক্ষণাবেক্ষণকালে পাওয়ার বন্ধ করে প্যাডলক দিয়ে তালা লাগানো এবং "চালানো নিষেধ" ট্যাগ লাগানোর পদ্ধতি।',
      ),
    },
    {
      ja: '非常停止',
      reading: 'ひじょうていし',
      en: 'emergency stop (E-stop)',
      bn: 'জরুরি স্টপ (ই-স্টপ)',
      note: t(
        '異常{いじょう}や危険{きけん}時{じ}に直{ただ}ちに機械{きかい}の動力{どうりょく}を遮断{しゃだん}するための赤色{あかいろ}キノコ型{がた}押しボタンスイッチ。',
        'Red mushroom-head pushbutton switch that immediately cuts machinery power in case of sudden danger.',
        'হঠাৎ বিপদের সময় মেশিনের সব পাওয়ার অবিলম্বে বন্ধ করার লাল মাশরুম আকৃতির পুশ বোতাম।',
      ),
    },
    {
      ja: '研削といし',
      reading: 'けんさくといし',
      en: 'grinding wheel',
      bn: 'গ্রাইন্ডিং হুইল',
      note: t(
        '金属{きんぞく}の研削{けんさく}や切断{せつだん}に用{もち}いる円盤状{えんばんじょう}のといし。割{わ}れると高速{こうそく}で破片{はへん}が飛散{ひさん}するため厳格{げんかく}な点検{てんけん}が必要{ひつよう}。',
        'Abrasive disc used for cutting and grinding metal. Requires rigorous inspection because bursts cause fatal shrapnel.',
        'ধাতু কাটা বা ঘষার চাকা। ভাঙলে মারাত্মক গতিতে টুকরো ছিটকে যায় বলে কঠোর পরীক্ষা জরুরি।',
      ),
    },
    {
      ja: '打音点検',
      reading: 'だおんてんけん',
      en: 'ring test / tap sound test',
      bn: 'শব্দ পরীক্ষা (রিং টেস্ট)',
      note: t(
        'といしを木製{もくせい}の柄{え}等{とう}で軽{かる}く叩{たた}き、澄{す}んだ音{おと}（良品{りょうひん}）か鈍{にぶ}い音{おと}（ひび割{わ}れ）かで健全性{けんぜんせい}を確認{かくにん}する検査{けんさ}。',
        'Tapping a grinding wheel lightly with a wooden handle to verify integrity from clear resonant rings versus dull thuds.',
        'কাঠের হাতল দিয়ে হালকা টোকা দিয়ে পরিষ্কার সুরযুক্ত শব্দ (ভালো) বা ভোঁতা শব্দ (ফাটা) যাচাইয়ের পরীক্ষা।',
      ),
    },
    {
      ja: '試運転',
      reading: 'しうんてん',
      en: 'trial run / test operation',
      bn: 'পরীক্ষামূলক চালানো',
      note: t(
        '作業{さぎょう}開始前{かいしまえ}（1分以上{ぷんいじょう}）やといし交換後{こうかんご}（3分以上{ぷんいじょう}）に、無負荷{むふか}で回転{かいてん}させて異音{いおん}や振動{しんどう}を確認{かくにん}すること。', // verify: 研削といしの試運転時間は労働安全衛生規則第119条で始業前1分以上・取替え後3分以上と規定
        'Running a machine unloaded (1+ min daily, 3+ min after wheel replacement) to verify no abnormal sound or vibration.',
        'কাজ শুরুর আগে (১ মিনিট) বা হুইল পরিবর্তনের পর (৩ মিনিট) লোড ছাড়া চালিয়ে শব্দ ও কাঁপন পরীক্ষার প্রক্রিয়া।',
      ),
    },
    {
      ja: 'ワークレスト',
      reading: 'わーくれすと',
      en: 'work rest',
      bn: 'ওয়ার্ক রেস্ট',
      note: t(
        '卓上{たくじょう}グラインダーで加工物{かこうぶつ}を支{ささ}える台{だい}。といしとの隙間{すきま}を3mm以内{いない}に保{たも}ち巻{ま}き込{こ}みを防{ふせ}ぐ。', // verify: 研削盤のワークレスト隙間は3mm以内、調整片隙間は3-10mm（労働安全衛生規則）
        'Support table for workpieces on a bench grinder. Keep clearance within 3 mm to prevent work from being pulled in.',
        'বেঞ্চ গ্রাইন্ডারে কাজের বস্তু রাখার সাপোর্ট টেবিল। হুইলের সাথে ফাঁক ৩ মিলিমিটারের মধ্যে রাখা আবশ্যক।',
      ),
    },
    {
      ja: '惰力回転',
      reading: 'だりょくかいてん',
      en: 'coasting rotation / inertial rotation',
      bn: 'জড়তা ঘূর্ণন',
      note: t(
        '電源{でんげん}スイッチをOFFにした後{のち}も、慣性{かんせい}力{りょく}によってしばらく機械{きかい}が回転{かいてん}を続{つづ}けること。完全{かんぜん}停止{ていし}まで接触{せっしょく}禁止{きんし}。',
        'Continued rotation of machine parts due to inertia even after the power is shut off. Touching is prohibited until complete stop.',
        'বিদ্যুৎ সুইচ বন্ধ করার পরেও জড়তার কারণে যন্ত্রের কিছুক্ষণ ঘুরতে থাকা। পুরো থামা পর্যন্ত ছোঁয়া নিষেধ।',
      ),
    },
  ],
}
