import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string, bn: string): Tri => ({ ja, en, bn })

export const safetySigns: CourseData = {
  id: 'safety-signs',
  category: 'language',
  level: 1,
  minutes: 15,
  title: t(
    '安全標識{あんぜんひょうしき}と安全色{あんぜんしょく}',
    'Safety signs and safety colors',
    'নিরাপত্তা সাইন ও নিরাপত্তা রঙ',
  ),
  sub: t(
    '色{いろ}と形{かたち}と漢字{かんじ}で危険{きけん}を見分{みわ}け、正{ただ}しい行動{こうどう}をとる。',
    'Recognize hazards by color, shape, and kanji, and take the correct safety actions.',
    'রঙ, আকার ও কাঞ্জি দেখে বিপদ চেনা এবং সঠিক সুরক্ষামূলক পদক্ষেপ নেওয়া।',
  ),
  refs: [
    'JIS Z 9101（安全色及び安全標識―安全標識及び安全マーキングのデザイン通則）',
    '労働安全衛生法 第59条（雇入れ時等の安全衛生教育）',
  ],
  panels: [
    {
      who: 'foreman',
      text: t(
        '今日{きょう}は現場{げんば}の「安全標識{あんぜんひょうしき}」と「安全色{あんぜんしょく}」を学{まな}びましょう。',
        'Today let us learn the safety signs and safety colors used on our site.',
        'আজ আমরা কাজের জায়গার "নিরাপত্তা সাইন" ও "নিরাপত্তা রঙ" সম্পর্কে শিখব।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '赤{あか}や黄色{きいろ}、青{あお}や緑{みどり}の看板{かんばん}がたくさんありますね。何{なに}が違{ちが}うのですか？',
        'There are many red, yellow, blue, and green signs. What is the difference?',
        'এখানে লাল, হলুদ, নীল ও সবুজ রঙের অনেক সাইনবোর্ড দেখছি। এগুলোর পার্থক্য কী?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '赤{あか}は禁止{きんし}、黄色{きいろ}は注意{ちゅうい}、青{あお}は指示{しじ}、緑{みどり}は安全{あんぜん}や非常口{ひじょうぐち}を表{あらわ}します。',
        'Red is prohibition, yellow is caution, blue is mandatory action, and green indicates safety or emergency exits.',
        'লাল নিষেধ, হলুদ সতর্কতা, নীল বাধ্যতামূলক নির্দেশ, আর সবুজ নিরাপত্তা বা জরুরি পথ নির্দেশ করে।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'あそこに赤{あか}い「関係者以外{かんけいしゃいがい}立入禁止{たちいりきんし}」の看板{かんばん}があります。今{いま}は誰{だれ}もいないので、近道{ちかみち}で通{とお}ってもいいですか？',
        'There is a red "Authorized Personnel Only" sign over there. Nobody is working there right now, so can I take a shortcut through it?',
        'ওখানে লাল রঙের "অননুমোদিত ব্যক্তির প্রবেশ নিষেধ" সাইন দেখছি। এখন তো সেখানে কেউ নেই, আমি কি শর্টকাট হিসেবে ওখান দিয়ে যেতে পারি?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '入{はい}ってはいけません。人{ひと}がいなくても、頭上{ずじょう}から物{もの}が落{お}ちたり、危険{きけん}な機械{きかい}が動{うご}く場所{ばしょ}です。',
        'You must not enter. Even if no one is visible, objects can fall from overhead or dangerous machines may operate there.',
        'কখনোই ঢুকবে না। মানুষ দেখা না গেলেও ওপর থেকে জিনিস পড়তে পারে বা বিপজ্জনক মেশিন চলতে পারে।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '危{あぶ}ないですね。わかりました。あっちの赤{あか}い「火気厳禁{かきげんきん}」はどういう意味{いみ}ですか？',
        'That is dangerous. Understood. What does that red "火気厳禁" sign over there mean?',
        'খুব বিপজ্জনক। বুঝেছি। ওপাশের লাল "আগুন কঠোরভাবে নিষেধ" লেখা সাইনটির অর্থ কী?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'タバコやライターなどの火{ひ}を絶対{ぜったい}に使{つか}うな、という意味{いみ}です。塗料{とりょう}やガスに引火{いんか}すると大火災{だいかさい}になります。',
        'It means never use matches, lighters, or open flames. If sparks ignite paint vapors or gas, a massive fire can occur.',
        'এর অর্থ ম্যাচ, লাইটার বা কোনো আগুন কখনই ব্যবহার করা যাবে না। পেইন্ট বা গ্যাসে আগুন লাগলে মারাত্মক অগ্নিকাণ্ড ঘটতে পারে।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '青{あお}い円{えん}の「保護帽着用{ほごぼうちゃくよう}」は必{かなら}ずヘルメットをかぶる約束{やくそく}ですね！',
        'The blue circle "Wear Safety Helmet" means wearing a hard hat is mandatory, right!',
        'নীল বৃত্তের "সুরক্ষা হেলমেট পরিধান" মানে বাধ্যতামূলকভাবে হেলমেট পরার নিয়ম, তাই না!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'その通{とお}りです。標識{ひょうしき}を見{み}たら指差呼称{しさこしょう}で声{こえ}に出{だ}し、安全{あんぜん}を確認{かくにん}して作業{さぎょう}しましょう。',
        'Exactly right. When you see a sign, confirm it out loud with pointing and calling before starting work.',
        'একদম ঠিক। সাইনবোর্ড দেখলেই আঙুল তুলে ডেকে বলে নিশ্চিত হও, তারপর নিরাপদে কাজ শুরু করো।',
      ),
    },
  ],
  handbook: [
    t(
      '赤{あか}の円{えん}に斜{なな}め線{せん}の標識{ひょうしき}は「禁止{きんし}」を表{あらわ}す。書{か}かれた行動{こうどう}を絶対{ぜったい}にしてはならない。',
      'A red circle with a diagonal slash means "Prohibition". Never perform the depicted action.',
      'লাল বৃত্তের ওপর তির্যক দাগের সাইন মানে "নিষেধ"। সেখানে নির্দেশিত কাজ কখনই করা যাবে না।',
    ),
    t(
      '黄色{きいろ}の三角{さんかく}の標識{ひょうしき}は「注意{ちゅうい}・警告{けいこく}」を表{あらわ}す。周囲{しゅうい}の危険{きけん}に警戒{けいかい}する。',
      'A yellow triangular sign means "Caution / Warning". Stay alert to surrounding hazards.',
      'হলুদ ত্রিভুজ সাইন মানে "সতর্কতা / সাবধান"। চারপাশের বিপদের বিষয়ে সর্বদা সতর্ক থাকো।',
    ),
    t(
      '青色{あおいろ}の円形{えんけい}の標識{ひょうしき}は「指示{しじ}（義務{ぎむ}）」を表{あらわ}す。保護具{ほごぐ}着用{ちゃくよう}などの指示{しじ}に必{かなら}ず従{したが}う。',
      'A blue circular sign means "Mandatory Action". Always obey instructions such as wearing required PPE.',
      'নীল বৃত্তাকার সাইন মানে "বাধ্যতামূলক নির্দেশ"। পিপিই পরাসহ নির্দেশিত কাজ অবশ্যই পালন করতে হবে।',
    ),
    t(
      '緑色{みどりいろ}の四角{しかく}の標識{ひょうしき}は「安全状態{あんぜんじょうたい}・避難{ひなん}」を表{あらわ}す。非常口{ひじょうぐち}や救護所{きゅうごしょ}の場所{ばしょ}を確認{かくにん}する。',
      'A green rectangular sign indicates "Safe Condition / Evacuation". Confirm locations of emergency exits and first aid.',
      'সবুজ চারকোনা সাইন মানে "নিরাপদ অবস্থা / জরুরি উদ্ধার"। জরুরি বহির্গমন ও প্রাথমিক চিকিৎসা কেন্দ্রের অবস্থান দেখে রাখো।',
    ),
    t(
      '「立入禁止{たちいりきんし}」の場所{ばしょ}には、人{ひと}がいなくても絶対{ぜったい}に入{はい}ってはならない。',
      'Never enter an area marked "No Entry", even if no one appears to be working there.',
      '"প্রবেশ নিষেধ" চিহ্নিত স্থানে মানুষ না থাকলেও কখনই প্রবেশ করবে না।',
    ),
    t(
      '「火気厳禁{かきげんきん}」の区域{くいき}では、タバコやライターなどの火気{かき}を絶対{ぜったい}に使{つか}ってはならない。',
      'In areas marked "Open Flames Prohibited", never use matches, lighters, or open sparks.',
      '"আগুন কঠোরভাবে নিষেধ" এলাকায় ধূমপান, লাইটার বা কোনো ধরনের আগুন জ্বালানো সম্পূর্ণ নিষেধ।',
    ),
    t(
      '「頭上注意{ずじょうちゅうい}」の場所{ばしょ}では、クレーンの吊{つ}り荷{に}や高所{こうしょ}からの落下物{らっかぶつ}を必{かなら}ず確認{かくにん}する。',
      'In areas marked "Watch Overhead", always check for crane loads and objects falling from above.',
      '"মাথার ওপর সাবধান" এলাকায় ক্রেনের ঝুলন্ত মাল ও ওপর থেকে কিছু পড়ার বিষয়ে অবশ্যই সতর্ক থাকো।',
    ),
    t(
      '「足元注意{あしもとちゅうい}」の場所{ばしょ}では、床{ゆか}の油{あぶら}や段差{だんさ}、開口部{かいこうぶ}につまずかないよう慎重{しんちょう}に歩{ある}く。',
      'In areas marked "Watch Your Step", walk carefully to avoid slips, trips on steps, or floor openings.',
      '"পায়ের নিচে সাবধান" এলাকায় মেঝের তেল, উঁচু-নিচু বা গর্ত এড়িয়ে সাবধানে হাঁটো।',
    ),
    t(
      '「保護帽着用{ほごぼうちゃくよう}」の区域{くいき}に入{はい}るときは、あごひもをしっかり締{し}めてヘルメットをかぶる。',
      'When entering an area requiring safety helmets, securely fasten your chin strap and wear your hard hat.',
      '"সুরক্ষা হেলমেট পরিধান" এলাকায় ঢোকার সময় ফিতা শক্ত করে বেঁধে অবশ্যই হেলমেট পরবে।',
    ),
    t(
      '「非常口{ひじょうぐち}」の前{まえ}や避難通路{ひなんつうろ}には、荷物{にもつ}や台車{だいしゃ}を絶対{ぜったい}に置{お}いてはならない。',
      'Never place cargo or carts in front of emergency exits or along evacuation pathways.',
      '"জরুরি নির্গমন পথ" বা উদ্ধার পথের সামনে কখনোই মালামাল বা ঠেলাগাড়ি রাখবে না।',
    ),
  ],
  reel: [
    t(
      '立入禁止{たちいりきんし}、進入{しんにゅう}なし、ヨシ！',
      'No entry, no unauthorized person inside, yoshi!',
      'প্রবেশ নিষেধ, কেউ ভেতরে ঢোকেনি, ঠিক আছে!',
    ),
    t(
      '火気厳禁{かきげんきん}、火気{かき}なし、ヨシ！',
      'Flames prohibited, no open fire, yoshi!',
      'আগুন কঠোরভাবে নিষেধ, কোনো আগুন নেই, ঠিক আছে!',
    ),
    t(
      '頭上注意{ずじょうちゅうい}、吊{つ}り荷{に}なし、ヨシ！',
      'Overhead hazard, no suspended load, yoshi!',
      'মাথার ওপর সাবধান, কোনো ঝুলন্ত মাল নেই, ঠিক আছে!',
    ),
    t(
      '足元注意{あしもとちゅうい}、段差{だんさ}・油{あぶら}なし、ヨシ！',
      'Watch your step, no obstacles or oil, yoshi!',
      'পায়ের নিচে সাবধান, কোনো বাধা বা তেল নেই, ঠিক আছে!',
    ),
    t(
      '保護帽着用{ほごぼうちゃくよう}、あごひも締{し}め、ヨシ！',
      'Safety helmet required, chin strap fastened, yoshi!',
      'হেলমেট পরিধান, ফিতা শক্ত করে বাঁধা, ঠিক আছে!',
    ),
    t(
      '非常口{ひじょうぐち}、障害物{しょうがいぶつ}なし、ヨシ！',
      'Emergency exit, clear of obstacles, yoshi!',
      'জরুরি বহির্গমন পথ, কোনো বাধা নেই, ঠিক আছে!',
    ),
    t(
      '安全標識{あんぜんひょうしき}、指差呼称{しさこしょう}、ヨシ！',
      'Safety sign, pointing and calling confirmed, yoshi!',
      'নিরাপত্তা সাইন, পয়েন্টিং ও কলিং সম্পন্ন, ঠিক আছে!',
    ),
  ],
  drills: [
    {
      id: 'sign-spot-warning',
      kind: 'spot',
      secs: 30,
      need: 4,
      name: t(
        '黄色{きいろ}の注意標識{ちゅういひょうしき}の発見{はっけん}',
        'Spotting yellow warning signs',
        'হলুদ সতর্কতা সাইন চিহ্নিতকরণ',
      ),
      goal: t(
        '30秒{びょう}で黄色{きいろ}い三角{さんかく}の「注意{ちゅうい}・警告{けいこく}」標識{ひょうしき}を4つ見{み}つける。',
        'Find 4 yellow triangular warning signs in 30 seconds.',
        '৩০ সেকেন্ডে ৪টি হলুদ ত্রিভুজ সতর্কতা সাইন খুঁজে বের করো।',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '黄色{きいろ}三角{さんかく}「頭上注意{ずじょうちゅうい}」',
            'Yellow triangle: "Watch Overhead"',
            'হলুদ ত্রিভুজ: "মাথার ওপর সাবধান"',
          ),
        },
        {
          hazard: false,
          label: t(
            '緑色{みどりいろ}四角{しかく}「非常口{ひじょうぐち}」',
            'Green rectangle: "Emergency Exit"',
            'সবুজ চারকোনা: "জরুরি পথ"',
          ),
        },
        {
          hazard: true,
          label: t(
            '黄色{きいろ}三角{さんかく}「高圧注意{こうあつちゅうい}」',
            'Yellow triangle: "High Voltage Caution"',
            'হলুদ ত্রিভুজ: "উচ্চ ভোল্টেজ সাবধান"',
          ),
        },
        {
          hazard: false,
          label: t(
            '青色{あおいろ}円形{えんけい}「保護帽着用{ほごぼうちゃくよう}」',
            'Blue circle: "Wear Safety Helmet"',
            'নীল বৃত্ত: "হেলমেট পরুন"',
          ),
        },
        {
          hazard: true,
          label: t(
            '黄色{きいろ}三角{さんかく}「足元注意{あしもとちゅうい}」',
            'Yellow triangle: "Watch Your Step"',
            'হলুদ ত্রিভুজ: "পায়ের নিচে সাবধান"',
          ),
        },
        {
          hazard: false,
          label: t(
            '緑色{みどりいろ}四角{しかく}「AED（救護{きゅうご}）」',
            'Green rectangle: "AED / First Aid"',
            'সবুজ চারকোনা: "এইডি / প্রাথমিক চিকিৎসা"',
          ),
        },
        {
          hazard: true,
          label: t(
            '黄色{きいろ}三角{さんかく}「感電注意{かんでんちゅうい}」',
            'Yellow triangle: "Electric Shock Warning"',
            'হলুদ ত্রিভুজ: "বৈদ্যুতিক শক সাবধান"',
          ),
        },
        {
          hazard: false,
          label: t(
            '赤色{あかいろ}円形{えんけい}「火気厳禁{かきげんきん}」',
            'Red circle with slash: "No Open Flames"',
            'লাল বৃত্ত: "আগুন নিষেধ"',
          ),
        },
        {
          hazard: true,
          label: t(
            '黄色{きいろ}三角{さんかく}「開口部注意{かいこうぶちゅうい}」',
            'Yellow triangle: "Floor Opening Caution"',
            'হলুদ ত্রিভুজ: "মেঝের গর্ত সাবধান"',
          ),
        },
      ],
    },
    {
      id: 'sign-spot-prohibition',
      kind: 'spot',
      secs: 30,
      need: 4,
      name: t(
        '赤色{あかいろ}の禁止標識{きんしひょうしき}の発見{はっけん}',
        'Spotting red prohibition signs',
        'লাল নিষেধাজ্ঞা সাইন চিহ্নিতকরণ',
      ),
      goal: t(
        '30秒{びょう}で赤色{あかいろ}の「禁止{きんし}」標識{ひょうしき}を4つ見{み}つける。',
        'Find 4 red prohibition signs in 30 seconds.',
        '৩০ সেকেন্ডে ৪টি লাল নিষেধাজ্ঞা সাইন খুঁজে বের করো।',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '赤色{あかいろ}円形{えんけい}「立入禁止{たちいりきんし}」',
            'Red circle: "No Entry"',
            'লাল বৃত্ত: "প্রবেশ নিষেধ"',
          ),
        },
        {
          hazard: false,
          label: t(
            '青色{あおいろ}円形{えんけい}「安全靴着用{あんぜんぐつちゃくよう}」',
            'Blue circle: "Wear Safety Shoes"',
            'নীল বৃত্ত: "নিরাপত্তা জুতা পরুন"',
          ),
        },
        {
          hazard: true,
          label: t(
            '赤色{あかいろ}円形{えんけい}「火気厳禁{かきげんきん}」',
            'Red circle: "Open Flames Strictly Prohibited"',
            'লাল বৃত্ত: "আগুন কঠোরভাবে নিষেধ"',
          ),
        },
        {
          hazard: false,
          label: t(
            '緑色{みどりいろ}四角{しかく}「休憩所{きゅうけいじょ}」',
            'Green rectangle: "Rest Area"',
            'সবুজ চারকোনা: "বিশ্রামাগার"',
          ),
        },
        {
          hazard: true,
          label: t(
            '赤色{あかいろ}円形{えんけい}「関係者以外立入禁止{かんけいしゃいがいたちいりきんし}」',
            'Red circle: "Authorized Personnel Only"',
            'লাল বৃত্ত: "অননুমোদিত ব্যক্তির প্রবেশ নিষেধ"',
          ),
        },
        {
          hazard: false,
          label: t(
            '黄色{きいろ}三角{さんかく}「高温注意{こうおんちゅうい}」',
            'Yellow triangle: "Hot Surface Warning"',
            'হলুদ ত্রিভুজ: "উচ্চ তাপমাত্রা সাবধান"',
          ),
        },
        {
          hazard: true,
          label: t(
            '赤色{あかいろ}円形{えんけい}「使用禁止{しようきんし}」',
            'Red circle: "Do Not Operate / Use"',
            'লাল বৃত্ত: "ব্যবহার নিষেধ"',
          ),
        },
        {
          hazard: false,
          label: t(
            '緑色{みどりいろ}四角{しかく}「喫煙所{きつえんじょ}」',
            'Green rectangle: "Designated Smoking Area"',
            'সবুজ চারকোনা: "ধূমপানের স্থান"',
          ),
        },
        {
          hazard: true,
          label: t(
            '赤色{あかいろ}円形{えんけい}「通行禁止{つうこうきんし}」',
            'Red circle: "No Thoroughfare"',
            'লাল বৃত্ত: "চলাচল নিষেধ"',
          ),
        },
      ],
    },
    {
      id: 'sign-response-seq',
      kind: 'seq',
      name: t(
        '危険標識{きけんひょうしき}を見{み}たときの行動手順{こうどうてじゅん}',
        'Action sequence when encountering a hazard sign',
        'ঝুঁকি সাইন দেখার পর পদক্ষেপের ধারাবাহিক ক্রম',
      ),
      goal: t(
        '「立入禁止{たちいりきんし}」などの標識{ひょうしき}を見{み}たときの行動{こうどう}を正{ただ}しい順番{じゅんばん}に並{なら}べる。',
        'Arrange the safety actions in order when seeing a "No Entry" or hazard sign.',
        '"প্রবেশ নিষেধ" বা ঝুঁকি সাইন দেখার পর করণীয় কাজগুলো সঠিক ক্রমে সাজাও।',
      ),
      steps: [
        t(
          '1. 標識{ひょうしき}の前{まえ}で一旦{いったん}立{た}ち止{ど}まる',
          '1. Stop immediately in front of the sign',
          '১. সাইনবোর্ডের সামনে তৎক্ষণাৎ থামো',
        ),
        t(
          '2. 色{いろ}・形{かたち}・文字{もじ}を確認{かくにん}して指差呼称{しさこしょう}する',
          '2. Check the color, shape, and text, then perform pointing and calling',
          '২. রঙ, আকার ও লেখা দেখে আঙুল নির্দেশ করে পয়েন্টিং ও কলিং করো',
        ),
        t(
          '3. 危険区域{きけんくいき}に入{はい}らず、指定{してい}された安全通路{あんぜんつうろ}へ迂回{うかい}する',
          '3. Do not enter the hazard area; detour to the designated safe walkway',
          '৩. ঝুঁকিপূর্ণ এলাকায় না ঢুকে নির্ধারিত নিরাপদ চলাচলের পথে ঘুরে যাও',
        ),
        t(
          '4. 周囲{しゅうい}に危険{きけん}な状態{じょうたい}があれば近{ちか}くの作業者{さぎょうしゃ}に声{こえ}をかける',
          '4. Warn nearby workers if any hazardous conditions are observed',
          '৪. কোনো বিপজ্জনক অবস্থা দেখলে আশেপাশের সহকর্মীদের ডেকে সতর্ক করো',
        ),
        t(
          '5. 判断{はんだん}に迷{まよ}ったら自分{じぶん}で決{き}めず、職長{しょくちょう}に報告{ほうこく}して確認{かくにん}する',
          '5. If unsure, do not assume; report to the foreman for confirmation',
          '৫. কোনো সন্দেহ থাকলে নিজে সিদ্ধান্ত না নিয়ে ফোরম্যানকে report করে নিশ্চিত হও',
        ),
      ],
    },
  ],
  inspect: [
    {
      id: 'exit-sign-station',
      defective: false,
      name: t(
        '非常口{ひじょうぐち}誘導標識{ゆうどうひょうしき}と通路{つうろ}',
        'Emergency exit sign and walkway',
        'জরুরি বহির্গমন সাইন ও নির্গমন পথ',
      ),
      spots: [
        {
          x: 45,
          y: 25,
          check: t(
            '標識{ひょうしき}の点灯{てんとう}・破損{はそん}：緑色{みどりいろ}の非常口{ひじょうぐち}ランプが明{あか}るく点灯{てんとう}しているか（良品{りょうひん}：正常{せいじょう}点灯{てんとう}・破損{はそん}なし / 不良{ふりょう}：消灯{しょうとう}・割{わ}れ）',
            'Sign illumination and damage: Green exit light is clearly illuminated (Good: Lit, undamaged / Defect: Off, cracked)',
            'সাইনের বাতি ও অবস্থা: সবুজ বাতি উজ্জ্বলভাবে জ্বলছে কিনা (ভালো: জ্বলছে ও অক্ষত / ত্রুটি: বন্ধ বা ভাঙা)',
          ),
        },
        {
          x: 50,
          y: 70,
          check: t(
            '扉{とびら}の前{まえ}の空間{くうかん}：非常口{ひじょうぐち}の扉{とびら}の前{まえ}に物{もの}が置{お}かれていないか（良品{りょうひん}：通路{つうろ}確保{かくほ} / 不良{ふりょう}：荷物{にもつ}で閉鎖{へいさ}）',
            'Space in front of door: Area in front of exit door is clear of items (Good: Clear / Defect: Blocked by cargo)',
            'দরজার সামনের ফাঁকা জায়গা: জরুরি দরজার সামনে কোনো জিনিস রাখা নেই তো (ভালো: পথ পরিষ্কার / ত্রুটি: মালামাল দিয়ে বন্ধ)',
          ),
        },
      ],
    },
    {
      id: 'no-entry-barrier',
      defective: true,
      name: t(
        '立入禁止{たちいりきんし}の区画{くかく}と看板{かんばん}',
        'No entry zone and warning board',
        'প্রবেশ নিষেধ এলাকা ও সতর্কবার্তা বোর্ড',
      ),
      spots: [
        {
          x: 35,
          y: 30,
          check: t(
            '標識{ひょうしき}の視認性{しにんせい}：「立入禁止{たちいりきんし}」の赤色{あかいろ}看板{かんばん}が正面{しょうめん}から見{み}えるか（良品{りょうひん}：直立{ちょくりつ}・鮮明{せんめい} / 不良{ふりょう}：倒{たお}れて文字{もじ}が隠{かく}れている）',
            'Sign visibility: Red "No Entry" sign clearly visible from the front (Good: Upright, legible / Defect: Fallen, obscured)',
            'সাইনের দৃশ্যমানতা: লাল "প্রবেশ নিষেধ" সাইনটি সামনে থেকে স্পষ্টভাবে দেখা যাচ্ছে কিনা (ভালো: সোজা ও স্পষ্ট / ত্রুটি: পড়ে গিয়ে ঢাকা পড়ে গেছে)',
          ),
        },
        {
          x: 65,
          y: 65,
          check: t(
            'トラバー・コーンの設置{せっち}：黄黒{きくろ}のバーで完全{かんぜん}に区画{くかく}を塞{ふさ}いでいるか（良品{りょうひん}：隙間{すきま}なく封鎖{ふうさ} / 不良{ふりょう}：バーが外{はず}れて人{ひと}が入{はい}れる）',
            'Tiger bar and cone placement: Yellow-black bar completely seals the zone (Good: Fully closed / Defect: Bar detached, accessible)',
            'টাইগার বার ও কোন বসানো: হলুদ-কালো বার দিয়ে এলাকা সম্পূর্ণ ঘেরা আছে কিনা (ভালো: সম্পূর্ণ অবরুদ্ধ / ত্রুটি: বার খুলে মানুষ ঢোকার ফাঁক তৈরি হয়েছে)',
          ),
        },
      ],
    },
    {
      id: 'ppe-mandatory-station',
      defective: true,
      name: t(
        '保護具{ほごぐ}着用{ちゃくよう}の指示標識{しじひょうしき}',
        'Mandatory PPE instruction sign',
        'বাধ্যতামূলক পিপিই পরিধানের নির্দেশক সাইন',
      ),
      spots: [
        {
          x: 30,
          y: 35,
          check: t(
            '標識{ひょうしき}の汚損{おそん}：青色{あおいろ}の「保護帽{ほごぼう}・保護{ほご}メガネ着用{ちゃくよう}」の絵{え}と文字{もじ}が読{よ}めるか（良品{りょうひん}：鮮明{せんめい} / 不良{ふりょう}：塗料{とりょう}やホコリで読{よ}めない）',
            'Sign surface: Blue "Wear Helmet and Goggles" symbol and text legible (Good: Clear / Defect: Covered in paint or dust)',
            'সাইনের পরিচ্ছন্নতা: নীল "হেলমেট ও চশমা পরিধান" চিত্র ও লেখা পড়া যাচ্ছে কিনা (ভালো: স্পষ্ট / ত্রুটি: রঙ বা ধুলায় অস্পষ্ট)',
          ),
        },
        {
          x: 70,
          y: 60,
          check: t(
            '固定{こてい}状態{じょうたい}：看板{かんばん}が針金{はりがね}などでぐらつかず固定{こてい}されているか（良品{りょうひん}：強固{きょうこ}に固定{こてい} / 不良{ふりょう}：脱落{だつらく}しかけて危険{きけん}）',
            'Mounting condition: Sign securely mounted without wobbling (Good: Firmly fixed / Defect: Loose, risk of falling)',
            'আটকে রাখার অবস্থা: সাইনবোর্ডটি নড়বড়ে না হয়ে শক্তভাবে আটকানো কিনা (ভালো: শক্তভাবে বাঁধা / ত্রুটি: খসে পড়ার মতো আলগা)',
          ),
        },
      ],
    },
    {
      id: 'flammable-storage-sign',
      defective: false,
      name: t(
        '危険物{きけんぶつ}置場{おきば}の火気厳禁{かきげんきん}標識{ひょうしき}',
        'Flammables storage "No Open Flames" sign',
        'বিপজ্জনক দাহ্য পদার্থাগারের "আগুন নিষেধ" সাইন',
      ),
      spots: [
        {
          x: 40,
          y: 30,
          check: t(
            '文字{もじ}と色{いろ}の判別{はんべつ}：赤色{あかいろ}の「火気厳禁{かきげんきん}」が遠{とお}くからでも視認{しにん}できるか（良品{りょうひん}：赤白{あかしろ}が鮮明{せんめい} / 不良{ふりょう}：色{いろ}あせて見{み}えない）',
            'Color and text legibility: Red "Open Flames Strictly Prohibited" clearly visible from afar (Good: Vivid / Defect: Faded)',
            'রঙ ও লেখার স্পষ্টতা: দূর থেকেও লাল "আগুন কঠোরভাবে নিষেধ" পরিষ্কার দেখা যায় কিনা (ভালো: উজ্জ্বল লাল ও সাদা / ত্রুটি: বিবর্ণ)',
          ),
        },
        {
          x: 60,
          y: 75,
          check: t(
            '消火設備{しょうかせつび}の併設{へいせつ}：標識{ひょうしき}の近{ちか}くに指定{してい}の消火器{しょうかき}が配置{はいち}されているか（良品{りょうひん}：消火器{しょうかき}あり / 不良{ふりょう}：消火器{しょうかき}がない）',
            'Fire equipment paired: Designated fire extinguisher located near the sign (Good: Extinguisher present / Defect: Missing)',
            'অগ্নিনির্বাপক সরঞ্জামের উপস্থিতি: সাইনের কাছাকাছি নির্দিষ্ট অগ্নিনির্বাপক রাখা আছে কিনা (ভালো: অগ্নিনির্বাপক বর্তমান / ত্রুটি: অনুপস্থিত)',
          ),
        },
      ],
    },
  ],
  quiz: [
    {
      q: t(
        '工場{こうじょう}の通路{つうろ}で、赤色{あかいろ}の円{えん}に斜{なな}め線{せん}の「関係者以外{かんけいしゃいがい}立入禁止{たちいりきんし}」の看板{かんばん}を見{み}つけました。今{いま}は誰{だれ}も作業{さぎょう}していません。どうすべきですか？',
        'In the factory corridor, you see a red circle with a diagonal slash reading "Authorized Personnel Only". No one is working there right now. What should you do?',
        'কারখানার চলাচলের পথে লাল বৃত্তের ওপর তির্যক দাগের "অননুমোদিত ব্যক্তির প্রবেশ নিষেধ" সাইন দেখতে পেলেন। এখন কেউ কাজ করছে না। আপনার কী করা উচিত?',
      ),
      options: [
        t(
          '中{なか}に人{ひと}がいなくても絶対{ぜったい}に入{はい}らず、必要{ひつよう}なら職長{しょくちょう}に連絡{れんらく}する',
          'Never enter even if no one is inside, and contact the foreman if you need something',
          'ভেতরে কেউ না থাকলেও কখনই ঢুকবে না, প্রয়োজনে ফোরম্যানের সাথে যোগাযোগ করবে',
        ),
        t(
          '誰{だれ}もいないなら、走{はし}って急{いそ}いで通り抜{ぬ}けてよい',
          'If no one is there, you may run quickly through the area',
          'কেউ না থাকলে দ্রুত দৌড়ে এলাকাটি পার হওয়া যাবে',
        ),
        t(
          'ヘルメットを脱{ぬ}いで静{しず}かに歩{ある}けば入{はい}ってもよい',
          'You may enter as long as you take off your helmet and walk quietly',
          'হেলমেট খুলে নিঃশব্দে হাঁটলে ভেতরে ঢোকা যাবে',
        ),
        t(
          '看板{かんばん}を手{て}で倒{たお}して見{み}えなくしてから入{はい}る',
          'Knock down the sign so it is not visible, then enter',
          'সাইনবোর্ড হাত দিয়ে ফেলে দিয়ে আড়াল করে তারপর ঢুকবে',
        ),
      ],
      answer: 0,
      why: t(
        '「関係者以外立入禁止{かんけいしゃいがいたちいりきんし}」は許可{きょか}のない人{ひと}の立入{たちいり}を固{かた}く禁{きん}じています。人{ひと}がいなくても落下物{らっかぶつ}や有害物質{ゆうがいぶっしつ}の危険{きけん}があります。',
        '"Authorized Personnel Only" strictly prohibits entry by unauthorized workers. Even if no one is visible, hazards like falling objects or hazardous substances may exist.',
        '"অননুমোদিত ব্যক্তির প্রবেশ নিষেধ" অননুমোদিত কর্মীদের প্রবেশ কঠোরভাবে নিষিদ্ধ করে। কাউকে দেখা না গেলেও ওপর থেকে জিনিস পড়া বা ক্ষতিকারক পদার্থের ঝুঁকি থাকতে পারে।',
      ),
    },
    {
      q: t(
        '塗料{とりょう}やシンナーが置{お}いてある倉庫{そうこ}の入口{いりぐち}に、赤色{あかいろ}の「火気厳禁{かきげんきん}」の標識{ひょうしき}があります。この標識{ひょうしき}の正{ただ}しい意味{いみ}はどれですか？',
        'At the entrance to a paint and thinner storage area, there is a red "Open Flames Strictly Prohibited" sign. What is the correct meaning?',
        'পেইন্ট ও থিনার রাখার গুদামের প্রবেশদ্বারে লাল রঙের "আগুন কঠোরভাবে নিষেধ" সাইন আছে। এর সঠিক অর্থ কোনটি?',
      ),
      options: [
        t(
          '部屋{へや}が寒{さむ}いときは暖房{だんぼう}のストーブをつけてよい',
          'You may turn on a heating stove if the room is cold',
          'ঘর ঠান্ডা হলে হিটার স্টোভ জ্বালানো যাবে',
        ),
        t(
          'ライター、タバコ、火花{ひばな}などの火気{かき}を絶対{ぜったい}に使{つか}ってはならない',
          'Never use matches, lighters, cigarettes, sparks, or any open fire',
          'লাইটার, সিগারেট, স্ফুলিঙ্গ বা কোনো ধরনের আগুন কখনই ব্যবহার করা যাবে না',
        ),
        t(
          '赤色{あかいろ}の作業服{さぎょうふく}を着{き}て入{はい}ってはならない',
          'You must not enter wearing red work clothes',
          'লাল রঙের কাজের পোশাক পরে ভেতরে ঢোকা যাবে না',
        ),
        t(
          '火災{かさい}のとき以外{いがい}は走{はし}ってはいけない',
          'You must not run except during a fire emergency',
          'আগুনের জরুরি পরিস্থিতি ছাড়া দৌড়ানো যাবে না',
        ),
      ],
      answer: 1,
      why: t(
        '「火気厳禁{かきげんきん}」は可燃性{かねんせい}ガスや溶剤{ようざい}に引火{いんか}する危険{きけん}を防{ふせ}ぐため、火{ひ}や火花{ひばな}の発生{はっせい}を全面{ぜんめん}禁止{きんし}するものです。',
        '"Open Flames Strictly Prohibited" bans all open fire and sparks to prevent igniting flammable vapors or solvents.',
        '"আগুন কঠোরভাবে নিষেধ" দাহ্য গ্যাস বা দ্রাবকে আগুন লাগার ঝুঁকি প্রতিরোধ করতে আগুন ও স্ফুলিঙ্গ তৈরি সম্পূর্ণ নিষিদ্ধ করে।',
      ),
    },
    {
      q: t(
        '造船所{ぞうせんじょ}のクレーン下{した}の通路{つうろ}に、黄色{きいろ}い三角{さんかく}の「頭上注意{ずじょうちゅうい}」の標識{ひょうしき}があります。何{なに}を警戒{けいかい}すべきですか？',
        'On a walkway beneath a shipyard crane, there is a yellow triangular "Watch Overhead" sign. What hazard should you watch for?',
        'জাহাজ নির্মাণ ডকের ক্রেনের নিচের পথে হলুদ ত্রিভুজাকার "মাথার ওপর সাবধান" সাইন আছে। কোন বিপদের বিষয়ে সতর্ক থাকা উচিত?',
      ),
      options: [
        t(
          '足元{あしもと}のケーブルにつまずく危険{きけん}',
          'Danger of tripping over floor cables',
          'মেঝের তারে হোঁচট খাওয়ার বিপদ',
        ),
        t(
          '床{ゆか}の油{あぶら}で足{あし}を滑{すべ}らせる危険{きけん}',
          'Danger of slipping on oily floor surfaces',
          'মেঝেতে জমে থাকা তেলে পা পিছলে যাওয়ার বিপদ',
        ),
        t(
          'クレーンの吊{つ}り荷{に}や高所{こうしょ}足場{あしば}からの工具{こうぐ}などの落下物{らっかぶつ}',
          'Falling objects such as crane loads or tools dropping from high scaffolds',
          'ক্রেনের ঝুলন্ত মাল বা উঁচু স্ক্যাফোল্ড থেকে টুলস নিচে পড়ে যাওয়ার ঝুঁকি',
        ),
        t(
          '壁{かべ}から突然{とつぜん}大{おお}きな音{おと}が鳴{な}り響{ひび}く危険{きけん}',
          'Danger of sudden loud noises echoing from walls',
          'দেয়াল থেকে হঠাৎ বিকট শব্দ ভেসে আসার বিপদ',
        ),
      ],
      answer: 2,
      why: t(
        '黄色{きいろ}の三角{さんかく}は警告{けいこく}を表{あらわ}し、「頭上注意{ずじょうちゅうい}」は上{うえ}からの落下物{らっかぶつ}やクレーンの荷{に}に警戒{けいかい}することを求{もと}めています。',
        'Yellow triangles indicate warnings, and "Watch Overhead" requires vigilance against falling objects or overhead crane cargo.',
        'হলুদ ত্রিভুজ সতর্কতা নির্দেশ করে এবং "মাথার ওপর সাবধান" ওপর থেকে জিনিস পড়া বা ক্রেনের মালের বিষয়ে সতর্ক থাকার নির্দেশ দেয়।',
      ),
    },
    {
      q: t(
        '鉄骨{てっこつ}加工{かこう}エリアの扉{とびら}に、青色{あおいろ}の円形{えんけい}「保護帽着用{ほごぼうちゃくよう}」の標識{ひょうしき}があります。中{なか}に入{はい}る前{まえ}の正{ただ}しい行動{こうどう}はどれですか？',
        'On the door of a steel fabrication area, there is a blue circle reading "Wear Safety Helmet". What is the correct action before entering?',
        'স্টিল প্রসেসিং এলাকার দরজায় নীল বৃত্তাকার "সুরক্ষা হেলমেট পরিধান" সাইন আছে। ভেতরে ঢোকার আগে সঠিক পদক্ষেপ কোনটি?',
      ),
      options: [
        t(
          '作業用{さぎょうよう}の手袋{てぶくろ}を外{はず}してポケットに入{い}れる',
          'Remove your work gloves and put them in your pocket',
          'কাজের গ্লাভস খুলে পকেটে ঢুকিয়ে রাখা',
        ),
        t(
          '靴{くつ}を脱{ぬ}いでスリッパに履{は}き替{か}える',
          'Take off your shoes and change into slippers',
          'জুতা খুলে চটি স্যান্ডেল পরে নেওয়া',
        ),
        t(
          '音{おと}を立{た}てないように素早{すばや}く走{はし}って通{とお}り抜{ぬ}ける',
          'Run through as quickly as possible without making noise',
          'কোনো শব্দ না করে যত দ্রুত সম্ভব দৌড়ে এলাকা পার হওয়া',
        ),
        t(
          'ヘルメットのあごひもをしっかり締{し}めて、頭部{とうぶ}を保護{ほご}してから入{はい}る',
          'Fasten the chin strap of your helmet securely to protect your head before entering',
          'মাথা সুরক্ষিত করতে হেলমেটের চিনস্ট্র্যাপ শক্ত করে বেঁধে তারপর ভেতরে ঢোকা',
        ),
      ],
      answer: 3,
      why: t(
        '青色{あおいろ}の円形{えんけい}は「指示{しじ}（義務{ぎむ}）」を表{あらわ}します。保護帽{ほごぼう}を正{ただ}しく着用{ちゃくよう}しないと、頭部{とうぶ}の激突{げきとつ}や落下物{らっかぶつ}で命{いのち}に関{かか}わります。',
        'A blue circle represents a mandatory instruction. Failing to wear a helmet properly risks fatal head injuries from falling objects or collisions.',
        'নীল বৃত্ত বাধ্যতামূলক নির্দেশ বোঝায়। হেলমেট সঠিকভাবে না পরলে মাথায় আঘাত বা ওপর থেকে জিনিস পড়ে জীবনহানি ঘটতে পারে।',
      ),
    },
    {
      q: t(
        '緑色{みどりいろ}の四角{しかく}に、走{はし}る人{ひと}の絵{え}と「非常口{ひじょうぐち}」と書{か}かれた標識{ひょうしき}があります。この場所{ばしょ}の管理{かんり}で正{ただ}しいものはどれですか？',
        'There is a green rectangular sign showing a running person and reading "Emergency Exit". What is the correct management of this location?',
        'সবুজ চারকোনা সাইনে একজন মানুষের দৌড়ানোর ছবি ও "জরুরি বহির্গমন" লেখা রয়েছে। এই স্থানটির সঠিক ব্যবস্থাপনা কোনটি?',
      ),
      options: [
        t(
          '緊急時{きんきゅうじ}に避難{ひなん}できるよう、扉{とびら}の前{まえ}に通路{つうろ}を常{つね}に確保{かくほ}し物{もの}を置{お}かない',
          'Always keep the pathway clear and never place items in front of the door so people can evacuate in emergencies',
          'জরুরি মুহূর্তে নিরাপদে বের হতে দরজার সামনে সর্বদা পথ ফাঁকা রাখতে হবে এবং কোনো জিনিস রাখা যাবে না',
        ),
        t(
          '使{つか}わなくなったダンボールやパレットの一時{いちじ}置{お}き場{ば}にする',
          'Use it as a temporary storage area for unused cardboard boxes and pallets',
          'অব্যবহৃত কার্টন বা প্যালেট সাময়িক রাখার জায়গা হিসেবে ব্যবহার করা',
        ),
        t(
          '鍵{かぎ}をしっかりかけて誰{だれ}も通{とお}れないように閉鎖{へいさ}する',
          'Lock it securely to prevent anyone from passing through',
          'কড়াভাবে তালা ঝুলিয়ে দিয়ে কারো চলাচল সম্পূর্ণ বন্ধ করে রাখা',
        ),
        t(
          '休憩時間{きゅうけいじかん}のタバコを吸{す}う場所{ばしょ}として使{つか}う',
          'Use it as a designated spot to smoke cigarettes during breaks',
          'বিরতির সময় সিগারেট খাওয়ার নির্দিষ্ট স্থান হিসেবে ব্যবহার করা',
        ),
      ],
      answer: 0,
      why: t(
        '「非常口{ひじょうぐち}」は火災{かさい}や地震{じしん}のときの脱出口{だっしゅつぐち}です。荷物{にもつ}で塞{ふさ}ぐことは法律{ほうりつ}で固{かた}く禁{きん}じられています。',
        'An "Emergency Exit" is an escape route during fires and earthquakes. Blocking it with cargo is strictly prohibited by law.',
        '"জরুরি বহির্গমন পথ" আগুন বা ভূমিকম্পের সময় বের হওয়ার পথ। মালামাল দিয়ে এটি আটকে রাখা আইনত কঠোরভাবে নিষিদ্ধ।',
      ),
    },
    {
      q: t(
        '機械{きかい}の点検{てんけん}ピットの周辺{しゅうへん}に「足元注意{あしもとちゅうい}」の黄色{きいろ}い標識{ひょうしき}が立{た}っています。歩行時{ほこうじ}の正{ただ}しい行動{こうどう}はどれですか？',
        'Around a machine inspection pit stands a yellow "Watch Your Step" sign. What is the correct action while walking?',
        'মেশিন পরীক্ষা করার পিটের চারপাশে হলুদ "পায়ের নিচে সাবধান" সাইন বসানো আছে। হাঁটার সময় সঠিক আচরণ কোনটি?',
      ),
      options: [
        t(
          '急{いそ}いでいるので足元{あしもと}を見{み}ずに走{はし}り抜{ぬ}ける',
          'Run through quickly without looking down because you are in a rush',
          'তাড়াহুড়ো থাকায় পায়ের নিচে না তাকিয়ে দৌড়ে পার হওয়া',
        ),
        t(
          '段差{だんさ}や床{ゆか}の開口部{かいこうぶ}、油{あぶら}による滑{すべ}りを確認{かくにん}しながら慎重{しんちょう}に歩{ある}く',
          'Walk cautiously while checking for floor steps, pit openings, and slippery oil patches',
          'মেঝের উঁচু-নিচু, খোলা গর্ত বা তেলের পিচ্ছিল ভাব লক্ষ্য করে সাবধানে হাঁটা',
        ),
        t(
          'スマートフォンを操作{そうさ}しながら進{すす}む',
          'Walk forward while operating your smartphone',
          'স্মার্টফোন চালাতে চালাতে এগিয়ে যাওয়া',
        ),
        t(
          '安全靴{あんぜんぐつ}を履{は}いていれば下{した}を見{み}る必要{ひつよう}はないと判断{はんだん}する',
          'Assume there is no need to look down as long as you wear safety shoes',
          'পায়ে সেফটি জুতা থাকলে নিচে দেখার প্রয়োজন নেই বলে মনে করা',
        ),
      ],
      answer: 1,
      why: t(
        '「足元注意{あしもとちゅうい}」は転倒{てんとう}や開口部{かいこうぶ}への転落{てんらく}を防{ふせ}ぐための注意{ちゅうい}表示{ひょうじ}です。足元{あしもと}をしっかり目視{もくし}して歩行{ほこう}します。',
        '"Watch Your Step" alerts workers to prevent trips, slips, and falls into openings. Always visually inspect where you step.',
        '"পায়ের নিচে সাবধান" হোঁচট খাওয়া, পিছলে পড়া ও গর্তে পড়ে যাওয়া আটকাতে সতর্ক করে। হাঁটার সময় পায়ের নিচে ভালো করে চোখ রাখতে হবে।',
      ),
    },
    {
      q: t(
        '電気室{でんきしつ}の分電盤{ぶんでんばん}の扉{とびら}に、黄色{きいろ}い「高圧注意{こうあつちゅうい}」の標識{ひょうしき}があります。この標識{ひょうしき}は何{なに}を伝{つた}えていますか？',
        'On the distribution panel door in an electrical room, there is a yellow "High Voltage Caution" sign. What does this sign convey?',
        'বৈদ্যুতিক কক্ষের ডিস্ট্রিবিউশন প্যানেলের দরজায় হলুদ "উচ্চ ভোল্টেজ সাবধান" সাইন আছে। এটি কী বার্তা দেয়?',
      ),
      options: [
        t(
          '高圧{こうあつ}の空気{くうき}でゴミを吹{ふ}き飛{と}ばす清掃{せいそう}エリアであること',
          'That it is a cleaning area using high-pressure air blowers',
          'উচ্চ চাপের বাতাস দিয়ে ধুলোবালি ওড়ানোর পরিষ্কার এলাকা',
        ),
        t(
          '部屋{へや}の中{なか}の気圧{きあつ}が高{たか}く耳{みみ}が痛{いた}くなる危険{きけん}があること',
          'That atmospheric pressure inside the room is high and may hurt ears',
          'ঘরের বাতাসের চাপ বেশি এবং কানে ব্যথা হওয়ার ঝুঁকি আছে',
        ),
        t(
          '高圧{こうあつ}の電化設備{でんかせつび}があり、無断{むだん}で触{ふ}れると感電{かんでん}・死亡{しぼう}事故{じこ}になる危険{きけん}があること',
          'That high-voltage equipment is present, and unauthorized contact risks severe electric shock or death',
          'উচ্চ ভোল্টেজের বিদ্যুৎ সরঞ্জাম রয়েছে এবং অনুমতি ছাড়া স্পর্শ করলে মারাত্মক বিদ্যুৎস্পৃষ্ট বা মৃত্যুর ঝুঁকি আছে',
        ),
        t(
          '水圧{すいあつ}が高{たか}い水道管{すいどうかん}が通{とお}っていること',
          'That high-pressure water supply pipes run through the area',
          'উচ্চ চাপের পানির পাইপ এখান দিয়ে গেছে',
        ),
      ],
      answer: 2,
      why: t(
        '「高圧注意{こうあつちゅうい}」は高電圧{こうでんあつ}による激{はげ}しい感電{かんでん}災害{さいがい}を防{ふせ}ぐための警告{けいこく}です。有資格者{ゆうしかくしゃ}以外{いがい}は開{あ}けてはなりません。',
        '"High Voltage Caution" warns of fatal electric shock hazards from high voltage. Only qualified personnel may open or access it.',
        '"উচ্চ ভোল্টেজ সাবধান" উচ্চ ভোল্টেজের মারাত্মক বিদ্যুৎস্পৃষ্ট দুর্ঘটনা প্রতিরোধে সতর্ক করে। যোগ্যতাসম্পন্ন কর্মী ছাড়া এটি খোলা নিষিদ্ধ।',
      ),
    },
    {
      q: t(
        '休憩時間{きゅうけいじかん}にタバコを吸{す}いたくなりました。工場内{こうじょうない}のどこで吸{す}うべきですか？',
        'You want to smoke a cigarette during break time. Where in the factory should you smoke?',
        'কাজের বিরতির সময় আপনি একটি সিগারেট খেতে চান। কারখানার কোথায় ধূমপান করা উচিত?',
      ),
      options: [
        t(
          '誰{だれ}も作業{さぎょう}していない機械{きかい}の陰{かげ}',
          'Behind an empty machine where no one is working',
          'ফাঁকা মেশিনের আড়ালে যেখানে কেউ কাজ করছে না',
        ),
        t(
          '「火気厳禁{かきげんきん}」の看板{かんばん}から少{すこ}し離{はな}れた屋外{おくがい}の資材置場{しざいおきば}',
          'In an outdoor material yard a short distance from a "No Open Flames" sign',
          '"আগুন নিষেধ" সাইন থেকে কিছুটা দূরে বাইরের মালামালের ডিপোতে',
        ),
        t(
          '非常口{ひじょうぐち}の扉{とびら}のすぐ外側{そとがわ}',
          'Just outside the emergency exit door',
          'জরুরি বহির্গমন দরজার ঠিক বাইরে',
        ),
        t(
          '緑色{みどりいろ}や白色{しろいろ}の看板{かんばん}で指定{してい}された「喫煙所{きつえんじょ}」の区画{くかく}の内側{うちがわ}',
          'Inside the designated "Smoking Area" clearly marked by green or white signs',
          'সবুজ বা সাদা সাইনবোর্ড দিয়ে নির্ধারিত "ধূমপানের স্থান"-এর ভেতরে',
        ),
      ],
      answer: 3,
      why: t(
        '工場{こうじょう}の火災{かさい}を防{ふせ}ぐため、喫煙{きつえん}は灰皿{はいざら}と消火器{しょうかき}が備{そな}えられた「喫煙所{きつえんじょ}」の中{なか}だけで行{おこな}います。',
        'To prevent fires in the factory, smoking is permitted only inside designated "Smoking Areas" equipped with ashtrays and fire extinguishers.',
        'কারখানায় আগুন প্রতিরোধে শুধুমাত্র ছাইদানি ও অগ্নিনির্বাপক সম্পন্ন নির্ধারিত "ধূমপানের স্থান"-এই ধূমপান করা যাবে।',
      ),
    },
    {
      q: t(
        '柱{はしら}に赤色{あかいろ}の「消火器{しょうかき}」の標識{ひょうしき}がありますが、その下{した}に空{あ}き箱{ばこ}が積{つ}まれていました。何{なに}が危険{きけん}ですか？',
        'There is a red "Fire Extinguisher" sign on a pillar, but empty boxes are piled beneath it. Why is this dangerous?',
        'খুঁটিতে লাল "অগ্নিনির্বাপক" সাইন রয়েছে, কিন্তু তার নিচে খালি বাক্স স্তূপ করে রাখা। এটি কেন বিপজ্জনক?',
      ),
      options: [
        t(
          '火災{かさい}の初期{しょき}に消火器{しょうかき}を即座{そくざ}に取{と}り出{だ}せず、火{ひ}が燃{も}え広{ひろ}がってしまう',
          'You cannot retrieve the extinguisher immediately at the start of a fire, causing the fire to spread',
          'আগুন লাগার শুরুতে দ্রুত অগ্নিনির্বাপক বের করা যাবে না, ফলে আগুন ছড়িয়ে পড়বে',
        ),
        t(
          '箱{ばこ}の重{おも}みで消火器{しょうかき}から自然{しぜん}に泡{あわ}が噴射{ふんしゃ}してしまう',
          'The weight of the boxes will cause the extinguisher to discharge foam automatically',
          'বাক্সের চাপে অগ্নিনির্বাপক থেকে নিজে নিজেই ফোম বের হতে শুরু করবে',
        ),
        t(
          '消火器{しょうかき}の赤色{あかいろ}ペンキが箱{ばこ}に移{うつ}って汚{よご}れてしまう',
          'The red paint of the extinguisher will rub off and dirty the boxes',
          'অগ্নিনির্বাপকের লাল রঙ বাক্সে লেগে ময়লা হয়ে যাবে',
        ),
        t(
          '箱{ばこ}のせいで柱{はしら}が傾{かたむ}いて倒{たお}れる恐{おそ}れがある',
          'There is a risk that the pillar will tilt and collapse because of the boxes',
          'বাক্সের ওজনে কারখানার খুঁটি হেলে পড়ে ভেঙে যেতে পারে',
        ),
      ],
      answer: 0,
      why: t(
        '消火器{しょうかき}は初期{しょき}消火{しょうか}のための最重要{さいじゅうよう}設備{せつび}です。標識{ひょうしき}の下{した}や周囲{しゅうい}には絶対{ぜったい}に障害物{しょうがいぶつ}を置{お}いてはなりません。',
        'A fire extinguisher is critical equipment for initial fire response. Never place obstacles under or around the sign.',
        'প্রাথমিক আগুন নেভানোর সবচেয়ে গুরুত্বপূর্ণ সরঞ্জাম হলো অগ্নিনির্বাপক। সাইনের নিচে বা আশেপাশে কোনো বাধা রাখা যাবে না।',
      ),
    },
    {
      q: t(
        'JIS規格{きかく}（JIS Z 9101）で定{さだ}められた「禁止標識{きんしひょうしき}」の正{ただ}しい色{いろ}と形{かたち}の組合{くみあ}わせはどれですか？',
        'According to JIS Z 9101, which combination of color and shape correctly represents a "Prohibition" sign?',
        'জেআইএস মানদণ্ড (JIS Z 9101) অনুযায়ী "নিষেধাজ্ঞা সাইন"-এর রঙ ও আকারের সঠিক জোড় কোনটি?',
      ),
      options: [
        t(
          '緑色{みどりいろ}の正方形{せいほうけい}・長方形{ちょうほうけい}',
          'Green square or rectangle',
          'সবুজ বর্গক্ষেত্র বা আয়তক্ষেত্র',
        ),
        t(
          '赤色{あかいろ}の円形{えんけい}に斜{なな}め線{せん}',
          'Red circle with a diagonal slash',
          'লাল বৃত্তের ওপর তির্যক দাগ',
        ),
        t(
          '黄色{きいろ}の正三角形{せいさんかくけい}',
          'Yellow equilateral triangle',
          'হলুদ সমবাহু ত্রিভুজ',
        ),
        t('青色{あおいろ}の円形{えんけい}', 'Blue circle', 'নীল বৃত্তাকার'),
      ],
      answer: 1,
      why: t(
        'JIS Z 9101では、禁止{きんし}は「赤{あか}の円{えん}に斜{なな}め線{せん}」、警告{けいこく}は「黄色{きいろ}の三角{さんかく}」、指示{しじ}は「青{あお}の円{えん}」、安全{あんぜん}は「緑{みどり}の四角{しかく}」と規定{きてい}されています。',
        'Under JIS Z 9101, prohibition is a red slashed circle, warning is a yellow triangle, mandatory is a blue circle, and safety is a green rectangle.',
        'JIS Z 9101 অনুসারে নিষেধ হলো লাল কাটা বৃত্ত, সতর্কতা হলুদ ত্রিভুজ, নির্দেশ নীল বৃত্ত এবং নিরাপত্তা সবুজ চারকোনা।',
      ),
    },
    {
      q: t(
        '緑色{みどりいろ}の看板{かんばん}に「休憩所{きゅうけいじょ}」と書{か}いてあります。ここはどのような場所{ばしょ}ですか？',
        'A green sign reads "Rest Area (休憩所)". What kind of facility is this?',
        'একটি সবুজ সাইনবোর্ডে "বিশ্রামাগার (休憩所)" লেখা রয়েছে। এটি কেমন জায়গা?',
      ),
      options: [
        t(
          '塗料{とりょう}や高圧{こうあつ}ガスボンベを保管{ほかん}する危険{きけん}な場所{ばしょ}',
          'A hazardous location where paints and high-pressure gas cylinders are stored',
          'পেইন্ট ও উচ্চ চাপের গ্যাস সিলিন্ডার রাখার ঝুঁকিপূর্ণ জায়গা',
        ),
        t(
          'フォークリフトやトラックが荷受{にう}けをする作業{さぎょう}エリア',
          'An operating area where forklifts and trucks load cargo',
          'ফর্কলিফট ও ট্রাক মালামাল ওঠানো-নামানোর কাজের এলাকা',
        ),
        t(
          '作業者{さぎょうしゃ}が疲労{ひろう}を回復{かいふく}し、水分{すいぶん}補給{ほきゅう}や休息{きゅうそく}をとる安全{あんぜん}な場所{ばしょ}',
          'A safe place for workers to recover from fatigue, rehydrate, and take a rest',
          'কর্মীদের ক্লান্তি দূর করা, পানি পান করা এবং বিশ্রাম নেওয়ার নিরাপদ স্থান',
        ),
        t(
          '鉄板{てっぱん}を切断{せつだん}して大{おお}きな音{おと}と火花{ひばな}が出{で}る作業場{さぎょうば}',
          'A workshop where steel plates are cut, generating loud noise and sparks',
          'স্টিল শিট কাটার ওয়ার্কশপ যেখানে তীব্র শব্দ ও স্ফুলিঙ্গ তৈরি হয়',
        ),
      ],
      answer: 2,
      why: t(
        '「休憩所{きゅうけいじょ}」は作業員{さぎょういん}が体調{たいちょう}を整{ととの}え、熱中症{ねっちゅうしょう}を防{ふせ}ぐために水分{すいぶん}をとるための安全{あんぜん}な施設{しせつ}です。',
        'A "Rest Area" is a safe facility for workers to recover physical condition and drink water to prevent heatstroke.',
        '"বিশ্রামাগার" হলো কর্মীদের শারীরিক সুস্থতা বজায় রাখা এবং হিটস্ট্রোক প্রতিরোধে পানি পান করার নিরাপদ ব্যবস্থা।',
      ),
    },
    {
      q: t(
        'クレーンヤードの入口{いりぐち}で、標識{ひょうしき}が汚{よご}れて破{やぶ}れており、文字{もじ}が読{よ}めませんでした。どうすべきですか？',
        'At the entrance to a crane yard, a sign is dirty and torn, making the text unreadable. What should you do?',
        'ক্রেন ইয়ার্ডের মুখে একটি সাইন নোংরা ও ছেঁড়া থাকায় লেখা পড়া যাচ্ছে না। আপনার কী করা উচিত?',
      ),
      options: [
        t(
          '読{よ}めないなら危険{きけん}はないと考{かんが}え、そのまま入{はい}る',
          'Assume there is no danger since it cannot be read, and enter directly',
          'পড়া না গেলে বিপদ নেই মনে করে সরাসরি ভেতরে চলে যাওয়া',
        ),
        t(
          '自分{じぶん}のペンで看板{かんばん}にてきとうな文字{もじ}を書{か}いておく',
          'Write arbitrary text on the board with your own pen',
          'নিজের কলম দিয়ে সাইনবোর্ডে ইচ্ছামতো কিছু লিখে দেওয়া',
        ),
        t(
          '看板{かんばん}を取{と}り外{はず}して自分{じぶん}のロッカーにしまっておく',
          'Remove the signboard and store it in your personal locker',
          'সাইনবোর্ডটি খুলে নিজের লকারে লুকিয়ে রাখা',
        ),
        t(
          '立入{たちいり}をやめて安全通路{あんぜんつうろ}へ迂回{うかい}し、「標識{ひょうしき}が読{よ}めない」と職長{しょくちょう}に報告{ほうこく}する',
          'Do not enter, detour to a safe walkway, and report to the foreman that the sign is unreadable',
          'ভেতরে না ঢুকে নিরাপদ পথে ঘুরে যাও এবং ফোরম্যানকে রিপোর্ট করো যে "সাইন পড়া যাচ্ছে না"',
        ),
      ],
      answer: 3,
      why: t(
        '標識{ひょうしき}が見{み}えない場所{ばしょ}は重大{じゅうだい}な危険{きけん}が潜{ひそ}んでいる可能性{かのうせい}があります。勝手{かって}に入{はい}らず職長{しょくちょう}へ報告{ほうこく}します。',
        'An unreadable sign could hide critical hazards. Never enter without confirmation; report it immediately to the foreman.',
        'অস্পষ্ট সাইনবোর্ডের পেছনে মারাত্মক বিপদ লুকিয়ে থাকতে পারে। নিজের মতো না ঢুকে অবিলম্বে ফোরম্যানকে রিপোর্ট করতে হবে।',
      ),
    },
  ],
  glossary: [
    {
      ja: '立入禁止',
      reading: 'たちいりきんし',
      en: 'No entry / Keep out',
      bn: 'প্রবেশ নিষেধ',
      note: t(
        '危険な区域や作業中の場所への立ち入りを禁じる標識。',
        'Sign prohibiting entry into hazardous areas or active work zones.',
        'বিপজ্জনক বা কাজের স্থানে প্রবেশ নিষিদ্ধ করার সাইন।',
      ),
    },
    {
      ja: '火気厳禁',
      reading: 'かきげんきん',
      en: 'Open flames strictly prohibited',
      bn: 'আগুন কঠোরভাবে নিষেধ',
      note: t(
        '引火や爆発を防ぐため、マッチ・ライター・火花を禁止する表示。',
        'Sign prohibiting matches, lighters, and sparks to prevent fire or explosion.',
        'আগুন বা বিস্ফোরণ ঠেকাতে ম্যাচ, লাইটার ও স্ফুলিঙ্গ নিষিদ্ধের সাইন।',
      ),
    },
    {
      ja: '関係者以外立入禁止',
      reading: 'かんけいしゃいがいたちいりきんし',
      en: 'Authorized personnel only',
      bn: 'অননুমোদিত ব্যক্তির প্রবেশ নিষেধ',
      note: t(
        'その作業の担当者や許可を受けた人以外は入ってはならない区域。',
        'Area where only authorized workers or permitted staff may enter.',
        'অনুমতিপ্রাপ্ত কর্মী ছাড়া অন্য কারো প্রবেশ সম্পূর্ণ নিষেধ।',
      ),
    },
    {
      ja: '高圧注意',
      reading: 'こうあつちゅうい',
      en: 'High voltage caution',
      bn: 'উচ্চ ভোল্টেজ সাবধান',
      note: t(
        '高電圧の設備があり、感電死傷事故を防ぐための警告表示。',
        'Warning sign for high-voltage equipment to prevent fatal electric shocks.',
        'মারাত্মক বিদ্যুৎস্পৃষ্ট দুর্ঘটনা প্রতিরোধে উচ্চ ভোল্টেজের সতর্কবার্তা।',
      ),
    },
    {
      ja: '足元注意',
      reading: 'あしもとちゅうい',
      en: 'Watch your step',
      bn: 'পায়ের নিচে সাবধান',
      note: t(
        '床の段差や油、開口部による転倒・転落を防ぐための注意標識。',
        'Caution sign to prevent tripping, slipping, or falling into openings.',
        'হোঁচট, পিছলে পড়া বা গর্তে পড়া রোধ করতে সতর্কীকরণ সাইন।',
      ),
    },
    {
      ja: '頭上注意',
      reading: 'ずじょうちゅうい',
      en: 'Watch for overhead hazards',
      bn: 'মাথার ওপর সাবধান',
      note: t(
        'クレーンの吊り荷や足場からの落下物への警戒を促す標識。',
        'Sign urging vigilance against crane loads or falling objects from above.',
        'ক্রেনের ঝুলন্ত মাল বা ওপর থেকে কিছু পড়ার বিষয়ে সতর্ক করার সাইন।',
      ),
    },
    {
      ja: '保護帽着用',
      reading: 'ほごぼうちゃくよう',
      en: 'Wear safety helmet (mandatory)',
      bn: 'সুরক্ষা হেলমেট পরিধান (বাধ্যতামূলক)',
      note: t(
        '頭部を保護するため、ヘルメット着用が義務付けられた区域の指示。',
        'Mandatory instruction requiring hard hats to protect workers heads.',
        'মাথা বাঁচাতে হেলমেট পরা বাধ্যতামূলক এমন এলাকার নির্দেশক।',
      ),
    },
    {
      ja: '非常口',
      reading: 'ひじょうぐち',
      en: 'Emergency exit',
      bn: 'জরুরি বহির্গমন পথ',
      note: t(
        '火災や地震などの緊急時に建物の外へ安全に脱出するための出口。',
        'Exit for evacuating safely outside during fires or earthquakes.',
        'আগুন বা ভূমিকম্পে নিরাপদে বাইরে বের হওয়ার জন্য জরুরি দরজা।',
      ),
    },
    {
      ja: '休憩所',
      reading: 'きゅうけいじょ',
      en: 'Rest area / break room',
      bn: 'বিশ্রামাগার',
      note: t(
        '作業者が疲労回復や水分補給を行うための安全な休息施設。',
        'Safe facility where workers rest, rehydrate, and recover from fatigue.',
        'কর্মীদের ক্লান্তি দূর ও পানি পান করে বিশ্রাম নেওয়ার স্থান।',
      ),
    },
    {
      ja: '喫煙所',
      reading: 'きつえんじょ',
      en: 'Designated smoking area',
      bn: 'নির্দিষ্ট ধূমপানের স্থান',
      note: t(
        '火災予防のため、灰皿が設置され喫煙が許可された指定区域。',
        'Designated area equipped with ashtrays where smoking is allowed safely.',
        'অগ্নি প্রতিরোধে ছাইদানি স্থাপন করে ধূমপানের অনুমতিপ্রাপ্ত স্থান।',
      ),
    },
    {
      ja: '危険',
      reading: 'きけん',
      en: 'Danger',
      bn: 'বিপদ',
      note: t(
        '死亡や重大な傷害に至る高い危険性があることを示す用語。',
        'Term indicating high risk of death or severe injury.',
        'মৃত্যু বা মারাত্মক শারীরিক ক্ষতির উচ্চ ঝুঁকি নির্দেশক শব্দ।',
      ),
    },
    {
      ja: '注意',
      reading: 'ちゅうい',
      en: 'Caution / Attention',
      bn: 'সতর্কতা',
      note: t(
        '軽傷や事故を防ぐため、周囲の状況に警戒を促す用語。',
        'Term urging alertness to surroundings to prevent accidents or injuries.',
        'দুর্ঘটনা বা চোট এড়াতে চারপাশের পরিস্থিতিতে সতর্ক থাকার নির্দেশক।',
      ),
    },
    {
      ja: '禁止',
      reading: 'きんし',
      en: 'Prohibition / Forbidden',
      bn: 'নিষেধ',
      note: t(
        '安全確保のため、特定の行為を行ってはならないとする強い定め。',
        'Strict rule stating that a specific action must not be performed.',
        'সুরক্ষা নিশ্চিতে নির্দিষ্ট কোনো কাজ না করার কঠোর নিয়ম।',
      ),
    },
  ],
}
