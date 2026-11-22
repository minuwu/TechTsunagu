import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string, bn: string): Tri => ({ ja, en, bn })

export const lowBack: CourseData = {
  id: 'low-back',
  category: 'core',
  level: 1,
  minutes: 15,
  title: t(
    '腰痛予防{ようつうよぼう}',
    'Back pain prevention',
    'কোমর ব্যথার প্রতিরোধ (পিঠ ও কোমরের যত্ন)',
  ),
  sub: t(
    '正{ただ}しい姿勢{しせい}で荷物{にもつ}を持{も}ち上{あ}げ、腰{こし}の負担{ふたん}を減{へ}らして怪我{けが}を防{ふせ}ぐ。',
    'Lift loads with correct posture, reduce strain on your lower back, and prevent injuries.',
    'সঠিক ভঙ্গিতে জিনিসপত্র তোলো, কোমরের ওপর চাপ কমাও এবং আঘাত থেকে বাঁচো।',
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
        'আজ আমরা ভারী স্টিলের পাত ও যন্ত্রাংশ বহন করব। কোমর আঘাতপ্রাপ্ত না করে কীভাবে বইতে হবে তা নিশ্চিত করি।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '自分{じぶん}の腕{うで}で持{も}ち上{あ}がる重{おも}さなら、そのままぐっと持{も}ち上{あ}げて大丈夫{だいじょうぶ}ですか？',
        'If I can lift the weight with my arms, is it okay to just hoist it up directly?',
        'নিজের হাতের শক্তিতে তোলা সম্ভব হলে কি সরাসরি এক টানে তুলে ফেলা যাবে?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'いいえ、膝{ひざ}を伸{の}ばしたまま腰{こし}だけで持{も}ち上{あ}げると、椎間板{ついかんばん}に強{つよ}い圧力{あつりょく}がかかり危険{きけん}です。',
        'No. If you lift with your back while keeping your knees straight, dangerous pressure acts on your spinal discs.',
        'না। হাঁটু সোজা রেখে শুধু কোমর বাঁকিয়ে তুললে মেরুদণ্ডের ডিস্কের ওপর মারাত্মক চাপ পড়ে এবং তা বিপজ্জনক।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '急{いそ}いで運{はこ}ぼう！（※膝{ひざ}を伸{の}ばしたまま腰{こし}を大{おお}きく曲{ま}げて荷物{にもつ}を持{も}とうとする）',
        'I will hurry and move it! (*attempts to grab the load with straight legs, bending deeply from the waist*)',
        'তাড়াতাড়ি নিয়ে যাই! (*হাঁটু না ভেঙে কোমর ঝুঁকিয়ে ভারি জিনিস তোলার চেষ্টা করে*)',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '危{あぶ}ない、ストップ！まず足{あし}を肩幅{かたはば}に広{ひろ}げ、荷物{にもつ}に近寄{ちかよ}ってしっかり腰{こし}を落{お}とします。',
        'Dangerous, stop! First spread your feet shoulder-width, get close to the load, and lower your hips deeply.',
        'বিপজ্জনক, থামো! প্রথমে দুই পা কাঁধের সমান ফাঁক করো, মালামালের একদম কাছে গিয়ে হাঁটু ভেঙে কোমর নামাও।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '足{あし}を開{ひら}いて膝{ひざ}を曲{ま}げ、荷物{にもつ}をお腹{なか}にぴったり引{ひ}き寄{よ}せます。こうですか？',
        'Spread my feet, bend my knees, and pull the load tight against my belly. Like this?',
        'পা ফাঁক করে হাঁটু বাঁকালাম এবং মালটি পেটের সাথে শক্ত করে চেপে ধরলাম। এভাবে?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'とても良{よ}いです。背筋{せすじ}をまっすぐ伸{の}ばし、脚{あし}の力{ちから}で立{た}ち上{あ}がります。身体{からだ}をねじってはいけません。',
        'Very good. Keep your back straight and stand up using the power of your legs. Never twist your torso.',
        'খুব ভালো। মেরুদণ্ড সোজা রাখো এবং পায়ের পেশির শক্তিতে উঠে দাঁড়াও। শরীর কখনো মোচড় দেবে না।',
      ),
    },
    {
      who: 'worker',
      text: t(
        'なるほど！腕{うで}や腰{こし}ではなく、太{ふと}ももの筋肉{きんにく}を使{つか}うと腰{こし}が全然{ぜんぜん}痛{いた}くありません！',
        'I see! Using my thigh muscles instead of my arms or lower back makes it completely painless for my back!',
        'তাই তো! হাত বা কোমরের বদলে উরুর পেশি ব্যবহার করায় কোমরে একেবারেই ব্যথা লাগছে না!',
      ),
    },
    {
      who: 'foreman',
      // verify: 厚生労働省「職場における腰痛予防対策指針」では、男性の重量物取扱い上限の目安を体重の約40%、女性はその約60%としている
      text: t(
        '重{おも}い物{もの}は無理{むり}せず「二人{ふたり}で持{も}つ」か「台車{だいしゃ}」を使{つか}います。男性{だんせい}は体重{たいじゅう}の40％以下{いか}が目安{めやす}です。',
        'For heavy items do not force it; use two people or a cart. As a guideline, men should stay under 40% of body weight.',
        'ভারী জিনিসের ক্ষেত্রে জোর না করে "দুজন মিলে তোলা" বা "ট্রলি" ব্যবহার করবে। পুরুষদের ক্ষেত্রে শরীরের ওজনের সর্বোচ্চ ৪০% একটি নির্দেশক মাপ।',
      ),
    },
    {
      who: 'worker',
      text: t(
        '了解{りょうかい}しました！腰{こし}に違和感{いわかん}やすじの痛{いた}みが出{で}たら我慢{がまん}せず、すぐに職長{しょくちょう}へ報告{ほうこく}します！',
        'Understood! If I feel any discomfort or strain in my back, I will not endure it alone and will report to you immediately!',
        'বুঝতে পেরেছি! কোমরে কোনো অস্বস্তি বা পেশিতে টান অনুভব করলে চেপে রাখব না, সাথে সাথে ফোরম্যানকে জানাব!',
      ),
    },
  ],
  handbook: [
    t(
      '荷物{にもつ}を持{も}ち上{あ}げるときは、膝{ひざ}を曲{ま}げて腰{こし}を深{ふか}く落{お}とし、脚{あし}の力{ちから}で立{た}ち上{あ}がる。',
      'When lifting loads, bend your knees to lower your hips deeply and stand up using leg strength.',
      'জিনিসপত্র তোলার সময় হাঁটু ভেঙে কোমর নিচে নামাও এবং পায়ের পেশির শক্তি ব্যবহার করে ওঠো।',
    ),
    t(
      '膝{ひざ}を伸{の}ばしたまま腰{こし}だけを曲{ま}げて荷物{にもつ}を引{ひ}き上{あ}げる作業{さぎょう}は厳禁{げんきん}。',
      'Lifting a load by bending only at your waist while keeping knees straight is strictly forbidden.',
      'হাঁটু সোজা রেখে শুধু কোমর ঝুঁকিয়ে মাল টেনে তোলার কাজ সম্পূর্ণ নিষিদ্ধ।',
    ),
    t(
      '荷物{にもつ}はできる限{かぎ}り身体{からだ}やお腹{なか}に近{ちか}づけて抱{かか}え、腕{うで}だけで支{ささ}えない。',
      'Hold the load as close to your body and stomach as possible; do not support it with your arms alone.',
      'মাল যতদূর সম্ভব শরীর ও পেটের কাছাকাছি চেপে রাখো; শুধু বাহুর জোরে ধরে রাখবে না।',
    ),
    t(
      '荷物{にもつ}を持{も}ったまま上半身{じょうはんしん}をねじらない。方向{ほうこう}を変{か}えるときは足{あし}の向{む}きを変{か}える。',
      'Do not twist your upper body while carrying a load. Turn by stepping and changing your foot direction.',
      'ভার বহন করার সময় শরীরের উপরের অংশ মোচড় দেবে না। দিক পরিবর্তনের সময় পা ঘুরিয়ে ঘুরবে।',
    ),
    // verify: 厚生労働省「職場における腰痛予防対策指針」では、男性の重量物取扱い上限の目安を体重の約40%、女性はその約60%としている
    t(
      '重{おも}い荷物{にもつ}（体重{たいじゅう}の40％超{ちょう}など）は一人{ひとり}で持{も}たず、2人{ふたり}で持{も}つか台車{だいしゃ}を使{つか}う。',
      'Do not carry heavy loads alone (e.g. over 40% body weight); lift with two people or use a cart.',
      'ভারী মাল (যেমন শরীরের ওজনের ৪০% এর বেশি) একা তুলবে না; দুজনে মিলে তুলবে বা ট্রলি ব্যবহার করবে।',
    ),
    t(
      '作業{さぎょう}前{まえ}や休憩{きゅうけい}時間{じかん}には、腰{こし}や脚{あし}の筋肉{きんにく}を伸{の}ばすストレッチを必{かなら}ず行{おこな}う。',
      'Always perform stretching exercises before work and during breaks to loosen back and leg muscles.',
      'কাজ শুরুর আগে ও বিরতির সময় কোমরের ও পায়ের পেশি প্রসারিত করার স্ট্রেচিং অবশ্যই করবে।',
    ),
    t(
      '長時間{ちょうじかん}の中腰{ちゅうごし}姿勢{しせい}を続{つづ}けず、合間{あいま}に上体{じょうたい}を反{そ}らすなど姿勢{しせい}を定期的{ていきてき}に変{か}える。',
      'Do not maintain a prolonged bent or stooped posture; change postures periodically by leaning back.',
      'দীর্ঘক্ষণ ঝুঁকে বা কুঁজো হয়ে কাজ করবে না; মাঝে মাঝে শরীর পেছনের দিকে টান টান করে ভঙ্গি বদলাও।',
    ),
    t(
      '荷物{にもつ}を置{お}くときも放{ほう}り投{な}げず、膝{ひざ}を曲{ま}げて腰{こし}を落{お}とし静{しず}かに下{お}ろす。',
      'When placing a load down, never drop it; bend your knees to lower your hips and set it down quietly.',
      'মাল নামিয়ে রাখার সময়ও ছুড়ে ফেলবে না; হাঁটু ভেঙে কোমর নামিয়ে আলতো করে মেঝে বা টেবিলে রাখবে।',
    ),
    t(
      '通路{つうろ}の段差{だんさ}や油{あぶら}・水濡{みずぬ}れを事前{じぜん}に確認{かくにん}し、滑{すべ}りや踏{ふ}み外{はず}しによる腰痛{ようつう}を防{ふせ}ぐ。',
      'Check walkways beforehand for steps, oil, and wet spots to prevent back injury from slipping or stumbling.',
      'চলাচলের পথে উঁচুনিচু জায়গা বা তেল-জল আগে থেকেই দেখে নাও, যাতে পিছলে বা হোঁচট খেয়ে কোমর আঘাত না পায়।',
    ),
    t(
      '腰{こし}に違和感{いわかん}やすじの痛{いた}みを感{かん}じたら作業{さぎょう}を止{と}め、我慢{がまん}せず職長{しょくちょう}へ報告{ほうこく}する。',
      'If you feel discomfort or acute strain in your back, stop working and report to the foreman without enduring it alone.',
      'কোমরে কোনো অস্বস্তি বা তীব্র টান অনুভব করলে কাজ বন্ধ করো এবং চেপে না রেখে ফোরম্যানকে জানাও।',
    ),
  ],
  reel: [
    t(
      '荷物{にもつ}の重{おも}さ、確認{かくにん}、ヨシ！',
      'Load weight, verified, yoshi!',
      'মালের ওজন, যাচাই করা হয়েছে, ঠিক আছে!',
    ),
    t(
      '足幅{あしはば}、肩幅{かたはば}、安定{あんてい}、ヨシ！',
      'Stance, shoulder-width, stable, yoshi!',
      'পায়ের অবস্থান, কাঁধের সমান, স্থির, ঠিক আছে!',
    ),
    t(
      '膝曲{ひざま}げ、腰{こし}落{お}とし、ヨシ！',
      'Knees bent, hips lowered, yoshi!',
      'হাঁটু ভাঁজ, কোমর নামানো, ঠিক আছে!',
    ),
    t(
      '荷物{にもつ}密着{みっちゃく}、引{ひ}き寄{よ}せ、ヨシ！',
      'Load held close to body, yoshi!',
      'মাল পেটের সাথে চেপে ধরা, ঠিক আছে!',
    ),
    t(
      '背筋{せすじ}伸{の}ばし、ねじれなし、ヨシ！',
      'Back straight, no twisting, yoshi!',
      'মেরুদণ্ড সোজা, কোনো মোচড় নেই, ঠিক আছে!',
    ),
    t(
      '二人{ふたり}持{も}ち、合図{あいず}「せーの」、ヨシ！',
      'Two-person lift, call signal "Ready, heve!", yoshi!',
      'দুজনে তোলা, সংকেতের ডাক "এক সাথে", ঠিক আছে!',
    ),
    t(
      '運搬{うんぱん}通路{つうろ}、足元{あしもと}障害物{しょうがいぶつ}なし、ヨシ！',
      'Transport route, footway clear of obstacles, yoshi!',
      'চলাচলের পথ, পায়ের নিচে কোনো বাধা নেই, ঠিক আছে!',
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
        'ভারী জিনিসপত্র বহনে বিপজ্জনক অঙ্গভঙ্গি চিহ্নিতকরণ',
      ),
      goal: t(
        '30秒{びょう}で腰{こし}を痛{いた}める不安全{ふあんぜん}な動作{どうさ}を4つ見{み}つける。',
        'Find 4 unsafe movements that injure the back in 30 seconds.',
        '৩০ সেকেন্ডে কোমর ক্ষতিগ্রস্ত করার মতো ৪টি বিপজ্জনক অঙ্গভঙ্গি খুঁজে বের করো।',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '膝{ひざ}を伸{の}ばしたまま腰{こし}だけ曲{ま}げて持{も}ち上{あ}げる',
            'Lifting by bending only the waist with straight knees',
            'হাঁটু সোজা রেখে শুধু কোমর বাঁকিয়ে কোনো কিছু তোলা',
          ),
        },
        {
          hazard: false,
          label: t(
            '足幅{あしはば}を肩幅{かたはば}に広{ひろ}げて腰{こし}を深{ふか}く落{お}とす',
            'Spreading feet shoulder-width and lowering hips deeply',
            'পা কাঁধের সমান ফাঁক করে কোমর ভালোমতো নিচে নামানো',
          ),
        },
        {
          hazard: true,
          label: t(
            '荷物{にもつ}を身体{からだ}から遠{とお}く離{はな}して腕{うで}だけで支{ささ}える',
            'Holding the load far away from body with arms alone',
            'মাল শরীর থেকে দূরে রেখে শুধু হাতের জোরে ধরে রাখা',
          ),
        },
        {
          hazard: false,
          label: t(
            '荷物{にもつ}をお腹{なか}にしっかり引{ひ}き寄{よ}せて密着{みっちゃく}させる',
            'Pulling load tightly against belly and holding close',
            'মাল পেটের সাথে শক্তভাবে টেনে শরীরের সাথে চেপে রাখা',
          ),
        },
        {
          hazard: true,
          label: t(
            '荷物{にもつ}を抱{かか}えたまま上体{じょうたい}を急{きゅう}にねじる',
            'Twisting upper body abruptly while holding the load',
            'মাল কোলে থাকা অবস্থায় হঠাৎ শরীর একপাশে মোচড় দেওয়া',
          ),
        },
        {
          hazard: false,
          label: t(
            '向{む}きを変{か}えるときは足{あし}のステップを踏{ふ}み換{か}える',
            'Changing direction by shifting and stepping with feet',
            'দিক বদলানোর সময় পুরো শরীর না মুচড়ে পায়ের পাতা ঘুরিয়ে ঘোরা',
          ),
        },
        {
          hazard: true,
          label: t(
            '30kgの重{おも}い鉄板{てっぱん}を一人{ひとり}で無理{むり}に持{も}ち上{あ}げる',
            'Forcing oneself to lift a heavy 30kg steel plate alone',
            '৩০ কেজি ওজনের ভারী লোহার পাত একা জোর করে তুলতে যাওয়া',
          ),
        },
        {
          hazard: false,
          label: t(
            '重{おも}い荷物{にもつ}は手押{てお}し台車{だいしゃ}に載{の}せて運{はこ}ぶ',
            'Transporting heavy cargo on a manual push cart',
            'ভারী জিনিসপত্র ম্যানুয়াল হ্যান্ড ট্রলিতে তুলে পরিবহন করা',
          ),
        },
        {
          hazard: true,
          label: t(
            '前{まえ}が見{み}えない高{たか}さまで箱{はこ}を積{つ}み重{かさ}ねて運{はこ}ぶ',
            'Carrying boxes stacked so high that forward view is blocked',
            'সামনের রাস্তা দেখা যায় না এমন উঁচুতে বাক্স সাজিয়ে হাঁটা',
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
        'মেঝে থেকে জিনিসপত্র তোলার সঠিক ক্রম',
      ),
      goal: t(
        '腰{こし}を痛{いた}めずに荷物{にもつ}を持{も}ち上{あ}げる4つの基本{きほん}手順{てじゅん}を正{ただ}しい順番{じゅんばん}に並{なら}べる。',
        'Arrange the 4 basic steps of lifting without back strain in the correct order.',
        'কোমরে চোট না পেয়ে মাল তোলার মৌলিক ৪টি ধাপ সঠিক ক্রমে সাজাও।',
      ),
      steps: [
        t(
          '1. 荷物{にもつ}に近寄{ちかよ}り、足幅{あしはば}を肩幅{かたはば}程度{ていど}に広{ひろ}げて構{かま}える',
          '1. Approach the load and stand with feet about shoulder-width apart',
          '১. মালামালের কাছে গিয়ে দুই পা কাঁধের সমান দূরত্বে রেখে দাঁড়াও',
        ),
        t(
          '2. 膝{ひざ}を曲{ま}げて腰{こし}を落{お}とし、荷物{にもつ}をお腹{なか}に引{ひ}き寄{よ}せて抱{かか}える',
          '2. Bend knees to lower hips and pull the load tight against your belly',
          '২. হাঁটু ভেঙে কোমর নামাও এবং মালটি পেটের কাছে শক্তভাবে চেপে ধরো',
        ),
        t(
          '3. 背筋{せすじ}を伸{の}ばしたまま、脚{あし}（太{ふと}もも）の力{ちから}で垂直{すいちょく}に立{た}ち上{あ}がる',
          '3. Keeping back straight, stand up vertically using leg and thigh power',
          '৩. পিঠ সোজা রেখে পায়ের (উরুর) পেশির জোরে সোজা উপরে উঠে দাঁড়াও',
        ),
        t(
          '4. 身体{からだ}をねじらず、足{あし}の向{む}きを変{か}えて歩行{ほこう}を始{はじ}める',
          '4. Without twisting your body, turn by moving your feet and begin walking',
          '৪. শরীর না মোচড়া দিয়ে পায়ের পাতা ঘুরিয়ে চলা শুরু করো',
        ),
      ],
    },
    {
      id: 'two-person-lift-seq',
      kind: 'seq',
      name: t(
        '2人{ふたり}での共同{きょうどう}持{も}ち上{あ}げ手順{てじゅん}',
        'Procedure for two-person coordinated lift',
        'দুজনে মিলে একসাথে মাল তোলার নিয়ম',
      ),
      goal: t(
        '重量物{じゅうりょうぶつ}を2人{ふたり}で安全{あんぜん}に運{はこ}ぶ手順{てじゅん}を順番{じゅんばん}に並{なら}べる。',
        'Arrange the steps for two people safely carrying a heavy load in order.',
        'ভারী জিনিস দুজনে মিলে নিরাপদে বহন করার ধাপগুলো ক্রমে সাজাও।',
      ),
      steps: [
        t(
          '1. 荷物{にもつ}の重{おも}さと運搬{うんぱん}ルートを確認{かくにん}し、合図者{あいずしゃ}を決{き}める',
          '1. Check load weight and route, and designate who will give signals',
          '১. মালের ওজন ও নেওয়ার পথ নিশ্চিত করো এবং কে নির্দেশ দেবে তা ঠিক করো',
        ),
        t(
          '2. 2人{ふたり}で向{む}かい合{あ}い、膝{ひざ}を曲{ま}げて同{おな}じタイミングで腰{こし}を落{お}とす',
          '2. Face each other, bend knees, and lower hips at the same timing',
          '২. মুখোমুখি অবস্থান নিয়ে হাঁটু ভেঙে একই সাথে কোমর নিচে নামাও',
        ),
        t(
          '3. 「せーの！」の掛{か}け声{ごえ}に合わせて同時{どうじ}に脚{あし}の力{ちから}で立{た}ち上{あ}がる',
          '3. Stand up simultaneously using leg power to the call "Ready, heve!"',
          '৩. "এক সাথে!" হাঁকডাকের সাথে সাথে একযোগে পায়ের শক্তিতে উঠে দাঁড়াও',
        ),
        t(
          '4. 歩調{ほちょう}を合{あ}わせて進{すす}み、降{お}ろすときも声{こえ}をかけて同時{どうじ}に腰{こし}を落{お}とす',
          '4. Walk in step together, and call out to lower hips together when placing it down',
          '৪. তাল মিলিয়ে পা ফেলে এগিয়ে যাও এবং নামানোর সময়ও ডাক দিয়ে একযোগে কোমর নামাও',
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
        'মালামাল তোলার অঙ্গভঙ্গির সঠিকতা পরীক্ষা',
      ),
      spots: [
        {
          x: 45,
          y: 25,
          check: t(
            '背骨{せぼね}・背筋{せすじ}：背筋{せすじ}がまっすぐ伸{の}びている（良品{りょうひん}：自然{しぜん}なS字{じ}カーブ / 不良{ふりょう}：猫背{ねこぜ}・腰{こし}が曲{ま}がる）',
            'Spine and back: Back is straight (Good: Natural S-curve / Defect: Slouched or arched lower back)',
            'মেরুদণ্ড ও পিঠ: পিঠ সোজা আছে (ভালো: স্বাভাবিক সোজা ভঙ্গি / ত্রুটি: কুঁজো হয়ে কোমর বাঁকানো)',
          ),
        },
        {
          x: 60,
          y: 50,
          check: t(
            '荷物{にもつ}との距離{きょり}：お腹{なか}にぴったり密着{みっちゃく}している（良品{りょうひん}：抱{かか}え込{こ}み / 不良{ふりょう}：身体{からだ}から離{はな}れる）',
            'Distance to load: Held tightly against belly (Good: Embraced close / Defect: Far from body)',
            'মালামালের দূরত্ব: পেটের সাথে শক্ত করে চেপে ধরা (ভালো: একদম কাছে / ত্রুটি: শরীর থেকে দূরে)',
          ),
        },
        {
          x: 40,
          y: 78,
          check: t(
            '膝{ひざ}と足幅{あしはば}：膝{ひざ}を深{ふか}く曲{ま}げて肩幅{かたはば}で安定{あんてい}（良品{りょうひん}：膝曲{ひざま}げ姿勢{しせい} / 不良{ふりょう}：膝直立{ひざちょくりつ}）',
            'Knees and feet: Knees deeply bent with stable shoulder-width base (Good: Knees bent / Defect: Knees locked straight)',
            'হাঁটু ও পা: হাঁটু পর্যাপ্ত বাঁকিয়ে কাঁধের সমান দূরত্বে স্থির (ভালো: হাঁটু ভাঙা / ত্রুটি: হাঁটু টানটান সোজা)',
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
        'মালামাল বহনের হ্যান্ড ট্রলির কাজের আগের পরীক্ষা',
      ),
      spots: [
        {
          x: 30,
          y: 25,
          check: t(
            'ハンドル部{ぶ}：固定{こてい}ボルトの緩{ゆる}みや変形{へんけい}がないか（良品{りょうひん}：堅固{けんご}に固定{こてい} / 不良{ふりょう}：ぐらつき・亀裂{きれつ}）',
            'Handle section: Check for loose bolts or deformation (Good: Firmly fixed / Defect: Loose or cracked)',
            'হ্যান্ডেল অংশ: নাটবল্টু ঢিলে বা বাঁকা আছে কিনা (ভালো: শক্তভাবে আটকানো / ত্রুটি: নড়বড়ে বা ফাটল)',
          ),
        },
        {
          x: 75,
          y: 55,
          check: t(
            '車輪{しゃりん}ブレーキ：ペダルを踏{ふ}んで確実{かくじつ}に車輪{しゃりん}がロックされるか（良品{りょうひん}：完全制動{かんぜんせいどう} / 不良{ふりょう}：制動不能{せいどうふのう}・滑{すべ}る）',
            'Wheel brake: Depress pedal to confirm wheels lock securely (Good: Fully locked / Defect: Brake fails and slips)',
            'চাকার ব্রেক: প্যাডেল চেপে চাকা পুরোপুরি লক হয় কিনা (ভালো: সম্পূর্ণ লক / ত্রুটি: ব্রেক না ধরা ও পিছলে যাওয়া)',
          ),
        },
        {
          x: 35,
          y: 80,
          check: t(
            'キャスター車輪{しゃりん}：糸{いと}くず巻{ま}き込{こ}みやゴムの剥離{はくり}がないか（良品{りょうひん}：滑{なめ}らかに回転{かいてん} / 不良{ふりょう}：異音{いおん}・破損{はそん}）',
            'Caster wheels: Check for tangled fibers or peeled rubber (Good: Spins smoothly / Defect: Abnormal noise or damaged)',
            'কাস্টার চাকা: সুতো বা ময়লা জড়ানো কিংবা রাবার ক্ষয় আছে কিনা (ভালো: মসৃণ ঘূর্ণন / ত্রুটি: শব্দ ও ভাঙা)',
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
        'ভারী পণ্যের তাকের সংরক্ষণ অবস্থা পরীক্ষা',
      ),
      spots: [
        {
          x: 50,
          y: 35,
          check: t(
            '重量物{じゅうりょうぶつ}の配置{はいち}：重{おも}い荷物{にもつ}が腰{こし}から胸{むね}の高{たか}さに置{お}かれているか（良品{りょうひん}：中段{ちゅうだん}配置{はいち} / 不良{ふりょう}：床直置{ゆかじかお}きや最上段{さいじょうだん}）',
            'Heavy items placement: Heavy items stored between waist and chest height (Good: Middle shelf / Defect: On floor or top shelf)',
            'ভারী পণ্যের অবস্থান: ভারী মাল কোমর থেকে বুকের উচ্চতায় রাখা আছে কিনা (ভালো: মাঝের তাকে রাখা / ত্রুটি: মেঝেতে বা সবার উপরের তাকে)',
          ),
        },
        {
          x: 45,
          y: 70,
          check: t(
            '作業{さぎょう}床面{ゆかめん}：水濡{みずぬ}れや油{あぶら}、足元{あしもと}の段差{だんさ}がないか（良品{りょうひん}：乾燥{かんそう}・清掃済{せいそうずみ} / 不良{ふりょう}：油膜{ゆまく}で滑{すべ}る）',
            'Working floor: Free of water, oil puddles, or tripping steps (Good: Dry and clean / Defect: Slippery with oil slick)',
            'কাজের মেঝে: মেঝের ওপর তেল, জল বা হোঁচট খাওয়ার মতো বাধা নেই (ভালো: শুকনো ও পরিষ্কার / ত্রুটি: তৈলাক্ত ও পিছল)',
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
        'মেঝে থেকে প্রায় ১৫ কেজির যন্ত্রাংশের বাক্স তোলার সময় সবচেয়ে নিরাপদ ভঙ্গি কোনটি?',
      ),
      options: [
        t(
          '足幅{あしはば}を肩幅{かたはば}に広{ひろ}げ、膝{ひざ}を曲{ま}げて腰{こし}を深{ふか}く落{お}とし、身体{からだ}に引{ひ}き寄{よ}せて脚{あし}の力{ちから}で立{た}つ',
          'Spread feet shoulder-width, bend knees to lower hips, pull load close, and stand with leg power',
          'পা কাঁধের সমান ফাঁক করে হাঁটু ভেঙে কোমর নামিয়ে, মাল শরীরের কাছে চেপে ধরে পায়ের শক্তিতে ওঠা',
        ),
        t(
          '膝{ひざ}をまっすぐ伸{の}ばしたまま腰{こし}だけを深{ふか}く曲{ま}げ、腕{うで}の力{ちから}で勢{いきお}いよく引{ひ}き上{あ}げる',
          'Keep knees straight, bend only at the waist, and jerk the load up forcefully with arms',
          'হাঁটু সোজা রেখে শুধু কোমর গভীরভাবে ঝুঁকিয়ে হাতের জোরে এক টানে তোলা',
        ),
        t(
          '荷物{にもつ}から一歩{いっぽ}離{はな}れた位置{いち}に立{た}ち、腕{うで}をいっぱいに伸{の}ばして指先{ゆびさき}だけで持{も}ち上{あ}げる',
          'Stand one step away from the load, extend arms fully, and lift using only fingertips',
          'মাল থেকে এক কদম দূরে দাঁড়িয়ে দুই হাত পুরোপুরি বাড়িয়ে শুধু আঙুলের ডগা দিয়ে তোলা',
        ),
        t(
          '荷物{にもつ}を抱{かか}えながら急激{きゅうげき}に腰{こし}をひねって素早{すばや}く立{た}ち上{あ}がる',
          'Twist waist abruptly while holding the load and stand up rapidly',
          'মাল জড়িয়ে ধরে থাকা অবস্থায় কোমর হঠাৎ মোচড় দিয়ে দ্রুত উঠে দাঁড়ানো',
        ),
      ],
      answer: 0,
      why: t(
        '膝{ひざ}を曲{ま}げて腰{こし}を落{お}とし、荷物{にもつ}を身体{からだ}に近{ちか}づけて脚{あし}の筋肉{きんにく}で持{も}ち上{あ}げると、腰{こし}にかかる負担{ふたん}を最小限{さいしょうげん}に抑{おさ}えられます。',
        'Bending knees to lower hips, bringing the load close, and lifting with leg muscles minimizes strain on the lower back.',
        'হাঁটু ভেঙে কোমর নামিয়ে, মাল শরীরের কাছে এনে পায়ের পেশির শক্তিতে তুললে কোমরের ওপর চাপ সবচেয়ে কম পড়ে।',
      ),
    },
    {
      q: t(
        '荷物{にもつ}を抱{かか}えて運{はこ}ぶとき、身体{からだ}への負担{ふたん}を減{へ}らすための荷物{にもつ}の位置{いち}として正{ただ}しいものはどれですか？',
        'When carrying a load, what is the correct position of the load to reduce strain on your body?',
        'মাল কোলে নিয়ে হাঁটার সময় শরীরের ওপর চাপ কমাতে মালের সঠিক অবস্থান কোনটি?',
      ),
      options: [
        t(
          '胸{むね}より高{たか}く持{も}ち上{あ}げて前方{ぜんぽう}の視界{しかい}を遮{さえぎ}る位置{いち}',
          'Held higher than the chest, blocking forward visibility',
          'বুকের চেয়ে উঁচুতে তুলে ধরে সামনের রাস্তা আড়াল করা অবস্থান',
        ),
        t(
          'できるだけお腹{なか}や胸{むね}に引{ひ}き寄{よ}せて身体{からだ}に密着{みっちゃく}させた位置{いち}',
          'Pulled as close as possible to the belly and chest, pressed against the body',
          'যতটা সম্ভব পেট ও বুকের কাছে টেনে শরীরের সাথে শক্তভাবে চেপে রাখা অবস্থান',
        ),
        t(
          '腕{うで}をまっすぐ下{した}に伸{の}ばして身体{からだ}から30cm以上{いじょう}離{はな}した位置{いち}',
          'Arms stretched straight down, keeping the load over 30cm away from the body',
          'হাত সোজা নিচের দিকে বাড়িয়ে শরীর থেকে ৩০ সেন্টিমিটারের বেশি দূরে রাখা অবস্থান',
        ),
        t(
          '片方{かたほう}の肩{かた}の上{うえ}に無理{むり}に乗{の}せて片手{かたて}だけで支{ささ}える位置{いち}',
          'Forced onto one shoulder and supported with only one hand',
          'এক কাঁধের ওপর জোর করে তুলে শুধু এক হাত দিয়ে ধরে রাখার অবস্থান',
        ),
      ],
      answer: 1,
      why: t(
        '荷物{にもつ}を身体{からだ}の中心{ちゅうしん}（重心{じゅうしん}）に近{ちか}づけて密着{みっちゃく}させることで、てこの原理{げんり}による腰{こし}への過剰{かじょう}な負荷{ふか}を防{ふせ}ぐことができます。',
        'Holding the load close to your center of gravity prevents excessive leverage strain on the lower back.',
        'মাল শরীরের ভরকেন্দ্রের কাছে এনে শক্তভাবে চেপে রাখলে লিভার নীতির কারণে কোমরে অতিরিক্ত চাপ পড়া বন্ধ হয়।',
      ),
    },
    {
      q: t(
        '荷物{にもつ}を運搬中{うんぱんちゅう}、右側{みぎがわ}の棚{だな}へ荷物{にもつ}を移動{いどう}させたいとき、腰{こし}を痛{いた}めないための動作{どうさ}はどれですか？',
        'While carrying a load, what is the movement to avoid back injury when moving the load to a shelf on the right?',
        'মাল বহনকালে ডান পাশের তাকে মালটি রাখতে চাইলে কোমর বাঁচাতে সঠিক নড়াচড়া কোনটি?',
      ),
      options: [
        t(
          '足{あし}を床{ゆか}に固定{こてい}したまま、上半身{じょうはんしん}だけを強{つよ}く右{みぎ}へねじる',
          'Keep feet planted on the floor and twist only upper body forcefully to the right',
          'মেঝেতে পা শক্ত করে রেখে শুধু শরীরের ওপরের অংশ ডানে সজোরে মোচড় দেওয়া',
        ),
        t(
          '勢{いきお}いをつけて反動{はんどう}を利用{りよう}し、荷物{にもつ}を右{みぎ}へ振{ふ}り投{な}げる',
          'Use momentum and rebound to swing and fling the load to the right',
          'শরীরের ঝাঁকুনি ও গতি কাজে লাগিয়ে মাল ডান দিকে ছুড়ে দেওয়া',
        ),
        t(
          '足{あし}のステップを踏{ふ}み直{なお}して身体全体{からだぜんたい}の向{む}きを右{みぎ}に変{か}えてから置{お}く',
          'Step with your feet to turn your entire body toward the right before setting it down',
          'পায়ের পাতা ঘুরিয়ে পুরো শরীরের মুখ ডান দিকে ফিরিয়ে নিয়ে তবেই রাখা',
        ),
        t(
          '腰{こし}を反{そ}らせながら首{くび}だけを右{みぎ}に向{む}けて素早{すばや}く置{お}く',
          'Arch your lower back while turning only your neck to the right to place it quickly',
          'কোমর পেছনের দিকে বাঁকিয়ে শুধু ঘাড় ডানে ফিরিয়ে দ্রুত মাল নামানো',
        ),
      ],
      answer: 2,
      why: t(
        '荷物{にもつ}を持{も}ったまま上体{じょうたい}をねじると椎間板{ついかんばん}に強{つよ}いせん断力{だんりょく}がかかります。必{かなら}ず足{あし}を踏{ふ}み換{か}えて身体{からだ}ごと向{む}きを変{か}えます。',
        'Twisting while holding a load exerts dangerous shear forces on spinal discs; always step to turn your whole body.',
        'মাল ধরা অবস্থায় শরীর মোচড়ালে মেরুদণ্ডের ডিস্কে মারাত্মক চাপ পড়ে; অবশ্যই পা ঘুরিয়ে পুরো শরীর ঘোরাতে হবে।',
      ),
    },
    {
      // verify: 厚生労働省「職場における腰痛予防対策指針」では、男性の重量物取扱い上限の目安を体重の約40%、女性はその約60%としている
      q: t(
        '職場{しょくば}で約{やく}35kgの重量物{じゅうりょうぶつ}を移動{いどう}させる作業{さぎょう}を指示{しじ}されました。このときの正{ただ}しい対応{たいおう}はどれですか？',
        'You were instructed to move a heavy object of about 35kg at work. What is the correct response?',
        'কর্মস্থলে আপনাকে প্রায় ৩৫ কেজির ভারী জিনিস সরানোর নির্দেশ দেওয়া হলো। এক্ষেত্রে সঠিক পদক্ষেপ কোনটি?',
      ),
      options: [
        t(
          '自分{じぶん}の体力{たいりょく}を試{ため}すため、一人{ひとり}で一気{いっき}に持{も}ち上{あ}げて走{はし}って運{はこ}ぶ',
          'Test personal strength by hoisting it alone all at once and running to carry it',
          'নিজের শক্তি যাচাই করতে একাই এক টানে তুলে দৌড়ে বহন করা',
        ),
        t(
          '腰{こし}に痛{いた}みが出{で}ても誰{だれ}にも言{い}わず我慢{がまん}して一人{ひとり}で作業{さぎょう}を続{つづ}ける',
          'Even if back pain strikes, endure silently without telling anyone and keep working alone',
          'কোমরে ব্যথা হলেও কাউকে না বলে সহ্য করে একা একাই কাজ চালিয়ে যাওয়া',
        ),
        t(
          '床{ゆか}の上{うえ}を引{ひ}きずって運{はこ}び、周囲{しゅうい}の安全確認{あんぜんかくにん}を省略{しょうりゃく}する',
          'Drag it across the floor while skipping safety confirmation of surrounding areas',
          'মেঝের ওপর দিয়ে ঘষে ঘষে টেনে নিয়ে যাওয়া এবং আশেপাশের নিরাপত্তা পরীক্ষা বাদ দেওয়া',
        ),
        t(
          '一人{ひとり}で無理{むり}に持{も}たず、同僚{どうりょう}に声{こえ}をかけて2人{ふたり}で持{も}つか台車{だいしゃ}を使用{しよう}する',
          'Do not lift alone; call a coworker to lift together with two people or use a cart',
          'একা জোর করে না তুলে সহকর্মীকে ডেকে দুজনে মিলে তোলা অথবা ট্রলি ব্যবহার করা',
        ),
      ],
      answer: 3,
      why: t(
        '30kg以上{いじょう}の荷物{にもつ}は一人{ひとり}作業{さぎょう}の制限{せいげん}を超{こ}えています。無理{むり}をせず台車{だいしゃ}を利用{りよう}するか、2人{ふたり}で息{いき}を合{あ}わせて運搬{うんぱん}します。',
        'Objects of 30kg or more exceed safe single-person limits; use a cart or perform a coordinated two-person lift.',
        '৩০ কেজির বেশি ওজনের জিনিস একা তোলার নিরাপদ সীমা অতিক্রম করে; ট্রলি ব্যবহার করো বা দুজনে মিলে তোলো।',
      ),
    },
    {
      q: t(
        '2人{ふたり}で重量物{じゅうりょうぶつ}を持{も}ち上{あ}げる「共同{きょうどう}持{も}ち上{あ}げ作業{さぎょう}」で最{もっと}も重要{じゅうよう}なルールは何{なん}ですか？',
        'What is the most important rule in a two-person coordinated lift of heavy objects?',
        'দুজনে মিলে ভারী জিনিস তোলার সময় সবচেয়ে গুরুত্বপূর্ণ নিয়ম কোনটি?',
      ),
      options: [
        t(
          '「せーの！」など合図{あいず}の声{こえ}を出{だ}して、2人{ふたり}で息{いき}を合{あ}わせて同時{どうじ}に持{も}ち上{あ}げる',
          'Call out signals like "Ready, heave!" to synchronize timing and lift together simultaneously',
          '"এক সাথে!" বলে সংকেত দিয়ে দুজন একসাথে তাল মিলিয়ে একই মুহূর্তে মাল তোলা',
        ),
        t(
          '力{ちから}の強{つよ}い人{ひと}が合図{あいず}なしで一気{いっき}に引{ひ}き上{あ}げて主導権{しゅどうけん}をとる',
          'The stronger person jerks the load up quickly without signaling to take the lead',
          'শক্তিশালী ব্যক্তি কোনো সংকেত না দিয়ে একাই এক টানে তুলে নিয়ন্ত্রণ নেওয়া',
        ),
        t(
          '2人{ふたり}がそれぞれ別{べつ}のタイミングで適当{てきとう}に力{ちから}を入{い}れる',
          'Each of the two persons applies force at completely random separate timings',
          'দুজন মানুষ সম্পূর্ণ আলাদা সময়ে নিজেদের মতো করে জোর খাটানো',
        ),
        t(
          '運搬中{うんぱんちゅう}に声{こえ}を出{だ}さず無言{むごん}のまま作業{さぎょう}を進{すす}める',
          'Carry out the task in complete silence without making any vocal calls',
          'বহনের পুরো সময় মুখে কোনো কথা না বলে সম্পূর্ণ নিঃশব্দে কাজ করা',
        ),
      ],
      answer: 0,
      why: t(
        '2人{ふたり}の呼吸{こきゅう}やタイミングがずれると片方{かたほう}に全重量{ぜんじゅうりょう}が一気{いっき}にかかり腰{こし}を痛{いた}めます。合図{あいず}の徹底{てってい}が必須{ひっす}です。',
        'If timing is out of sync, the full weight shifts onto one person causing severe injury; strict signaling is essential.',
        'দুজনের সময়ের সমন্বয় না থাকলে একজনের ওপর পুরো ওজন হঠাৎ পড়ে কোমর মারাত্মক জখম হতে পারে; সংকেত দেওয়া আবশ্যক।',
      ),
    },
    {
      q: t(
        '手押{てお}し台車{だいしゃ}を使{つか}って重量物{じゅうりょうぶつ}を運搬{うんぱん}する際{さい}、安全{あんぜん}な押{お}し方{かた}として正{ただ}しいものはどれですか？',
        'When transporting heavy loads using a push cart, which is the correct safe way to push it?',
        'হ্যান্ড ট্রলিতে ভারী জিনিস পরিবহনের সময় নিরাপদে ঠেলার সঠিক নিয়ম কোনটি?',
      ),
      options: [
        t(
          '台車{だいしゃ}を前方{ぜんぽう}に走{はし}らせて自分{じぶん}は後{うし}ろから小走{こばし}りで追{お}いかける',
          'Shove the cart forward to let it roll and jog after it from behind',
          'ট্রলিকে সামনের দিকে জোরে গড়িয়ে দিয়ে নিজে পেছন থেকে দৌড়ে যাওয়া',
        ),
        t(
          '背筋{せすじ}を伸{の}ばして両手{りょうて}でハンドルを握{にぎ}り、自分{じぶん}の体重{たいじゅう}を乗{の}せて前方{ぜんぽう}にゆっくり押{お}す',
          'Keep back straight, grip handle with both hands, and slowly push forward using body weight',
          'পিঠ সোজা রেখে দুই হাতে হ্যান্ডেল ধরে নিজের শরীরের ভর কাজে লাগিয়ে ধীরে ধীরে সামনে ঠেলা',
        ),
        t(
          '台車{だいしゃ}を後{うし}ろ向{む}きに引{ひ}っ張{ぱ}りながら後{うし}ろ歩{ある}きで進{すす}む',
          'Walk backward while pulling the cart behind you',
          'পেছনের দিকে হেঁটে হেঁটে ট্রলিকে নিজের দিকে টেনে নিয়ে যাওয়া',
        ),
        t(
          '荷物{にもつ}を高{たか}く積{つ}み上{あ}げて前{まえ}が見{み}えない状態{じょうたい}で無理{むり}に押{お}す',
          'Stack cargo so high that forward vision is blocked and push blindly',
          'মাল এত উঁচুতে স্তূপ করা যাতে সামনের রাস্তা দেখা না যায় এবং অন্ধভাবে ঠেলা',
        ),
      ],
      answer: 1,
      why: t(
        '台車{だいしゃ}は背筋{せすじ}を伸{の}ばし、両手{りょうて}でしっかりハンドルを押{お}して前進{ぜんしん}するのが基本{きほん}です。引{ひ}っ張{ぱ}ると足{あし}を巻{ま}き込{こ}む危険{きけん}があります。',
        'Pushing forward with both hands and a straight back is fundamental; pulling risks running over your own feet.',
        'পিঠ সোজা রেখে দুই হাতে হ্যান্ডেল ধরে সামনে ঠেলে এগোনোই নিয়ম; ট্রলি টানলে পায়ের পাতার ওপর চাকা ওঠার ঝুঁকি থাকে।',
      ),
    },
    {
      q: t(
        '長時間{ちょうじかん}中腰{ちゅうごし}や前屈{ぜんくつ}姿勢{しせい}での溶接{ようせつ}・組立{くみた}て作業{さぎょう}が続{つづ}くとき、腰痛{ようつう}を防{ふせ}ぐための適切{てきせつ}な行動{こうどう}はどれですか？',
        'When stooped or forward-bent welding/assembly continues for hours, what is appropriate to prevent back pain?',
        'ঝুঁকে বা কুঁজো হয়ে দীর্ঘক্ষণ ওয়েল্ডিং বা ফিটিংয়ের কাজ চলার সময় কোমর ব্যথা প্রতিরোধে সঠিক কাজ কোনটি?',
      ),
      options: [
        t(
          '痛{いた}くても休憩{きゅうけい}を取{と}らず、作業{さぎょう}が終{お}わるまで同{おな}じ姿勢{しせい}を固定{こてい}し続{つづ}ける',
          'Take no breaks even if painful, keeping the same posture locked until work is finished',
          'ব্যথা হলেও বিরতি না নিয়ে কাজ শেষ না হওয়া পর্যন্ত একই ভঙ্গিতে অনড় থাকা',
        ),
        t(
          '姿勢{しせい}がきつくなったら腰{こし}を急激{きゅうげき}にバキバキと強{つよ}くひねる',
          'Abruptly crack and twist your waist with strong force when feeling stiff',
          'ভঙ্গি কষ্টকর মনে হলে হঠাৎ সজোরে কোমর মটকে ঝাঁকুনি দেওয়া',
        ),
        t(
          '定期的{ていきてき}に小休止{しょうきゅうし}を入{い}れ、背筋{せすじ}をゆっくり反{そ}らすストレッチを行{おこな}う',
          'Take short breaks regularly and gently arch your back backward to stretch',
          'নিয়মিত ছোট বিরতি নিয়ে শরীর পেছনের দিকে আলতোভাবে বাঁকিয়ে স্ট্রেচিং করা',
        ),
        t(
          '痛{いた}み止{ど}めを飲{の}んで無理{むり}やり長時間{ちょうじかん}の連続{れんぞく}作業{さぎょう}をこなす',
          'Take painkiller medicine and force yourself through continuous hours of work',
          'ব্যথানাশক ওষুধ খেয়ে জোর করে টানা দীর্ঘ সময় কাজ চালিয়ে যাওয়া',
        ),
      ],
      answer: 2,
      why: t(
        '同{おな}じ姿勢{しせい}を長{なが}く続{つづ}けると筋肉{きんにく}の血流{けつりゅう}が悪化{あっか}します。定期的{ていきてき}に姿勢{しせい}を変{か}え、上体{じょうたい}を反{そ}らすストレッチでほぐします。',
        'Holding one posture worsens muscle blood circulation; change postures periodically and stretch by arching backward.',
        'একই ভঙ্গিতে বেশিক্ষণ থাকলে পেশিতে রক্ত সঞ্চালন কমে যায়; নিয়মিত ভঙ্গি বদলাও এবং পেছনে টানটান করে স্ট্রেচিং করো।',
      ),
    },
    {
      q: t(
        '作業{さぎょう}開始前{かいしまえ}に行{おこな}う腰痛予防{ようつうよぼう}のストレッチで、正{ただ}しい方法{ほうほう}はどれですか？',
        'What is the correct method for warm-up stretching to prevent lower back pain before starting work?',
        'কাজ শুরুর আগে কোমর ব্যথার প্রতিরোধে প্রস্তুতিমূলক স্ট্রেচিংয়ের সঠিক নিয়ম কোনটি?',
      ),
      options: [
        t(
          '勢{いきお}いをつけて反動{はんどう}（バウンド）をつけながら激{はげ}しく身体{からだ}を曲{ま}げる',
          'Bounce vigorously with momentum and jerk your body into deep bends',
          'ঝাঁকুনি দিয়ে গতি সৃষ্টি করে শরীরের ওপর জোর খাটিয়ে মোচড়ানো',
        ),
        t(
          '息{いき}を止{と}めて限界{げんかい}まで強{つよ}く筋肉{きんにく}を引{ひ}っ張{ぱ}り続{つづ}ける',
          'Hold breath and pull muscles as hard as possible past their limit',
          'দম বন্ধ করে রেখে পেশিকে সীমার বাইরে সজোরে টেনে রাখা',
        ),
        t(
          '身体{からだ}が温{あたた}まっていない状態{じょうたい}でいきなり重{おも}い鉄材{てつざい}を持{も}ち上{あ}げる',
          'Directly hoist heavy steel materials suddenly before the body has warmed up',
          'শরীর গরম না করেই হঠাৎ করে ভারী লোহার জিনিস তুলতে যাওয়া',
        ),
        t(
          '息{いき}を吐{は}きながら反動{はんどう}をつけず、ゆっくり20秒{びょう}ほど筋肉{きんにく}を伸{の}ばす',
          'Exhale naturally without bouncing, gently stretching the muscles for about 20 seconds',
          'দম ছাড়তে ছাড়তে কোনো ঝাঁকুনি ছাড়া ধীরে ধীরে প্রায় ২০ সেকেন্ড পেশি টানটান রাখা',
        ),
      ],
      answer: 3,
      why: t(
        'ストレッチは反動{はんどう}をつけず、自然{しぜん}な呼吸{こきゅう}を続{つづ}けながらゆっくり伸{の}ばすことで筋肉{きんにく}を安全{あんぜん}にほぐします。',
        'Stretching safely relaxes muscles when done gently without bouncing while maintaining steady breathing.',
        'ঝাঁকুনি ছাড়া স্বাভাবিক শ্বাসপ্রশ্বাস নিয়ে ধীরে ধীরে টানটান করলে পেশি নিরাপদে শিথিল ও নমনীয় হয়।',
      ),
    },
    {
      q: t(
        '作業中{さぎょうちゅう}に腰{こし}に「ピキッ」と鋭{するど}い痛{いた}みが走{はし}り、違和感{いわかん}を感{かん}じました。直{ただ}ちにとるべき行動{こうどう}はどれですか？',
        'During work, you felt an acute sharp pain and strain in your lower back. What action should you take immediately?',
        'কাজের সময় হঠাৎ কোমরে তীক্ষ্ণ টান লেগে ব্যথা অনুভূত হলো। সাথে সাথে আপনার কী করা উচিত?',
      ),
      options: [
        t(
          '直{ただ}ちに作業{さぎょう}を中断{ちゅうだん}して腰{こし}を休{やす}め、速{すみ}やかに職長{しょくちょう}へ報告{ほうこく}・相談{そうだん}する',
          'Stop work immediately to rest your back and report/consult with the foreman promptly',
          'সাথে সাথে কাজ বন্ধ করে কোমরকে বিশ্রাম দাও এবং দ্রুত ফোরম্যানকে জানিয়ে পরামর্শ নাও',
        ),
        t(
          '周囲{しゅうい}に心配{しんぱい}をかけないよう、痛{いた}みを隠{かく}して定時{ていじ}まで重{おも}い荷物{にもつ}を運{はこ}び続{つづ}ける',
          'Conceal pain to avoid worrying others, continuing to carry heavy loads until shift ends',
          'কাউকে দুশ্চিন্তায় না ফেলতে ব্যথা লুকিয়ে রেখে ছুটির সময় পর্যন্ত ভারী মাল বয়ে যাওয়া',
        ),
        t(
          '自己判断{じこはんだん}で強{つよ}く腰{こし}を叩{たた}いて作業{さぎょう}を再開{さいかい}する',
          'Beat your own back forcefully on self-judgment and resume working',
          'নিজের বিবেচনায় কোমর সজোরে চাপড়ে আবার কাজ শুরু করা',
        ),
        t(
          '誰{だれ}にも言{い}わずに現場{げんば}を抜{ぬ}け出{だ}して勝手{かって}に帰宅{きたく}する',
          'Slip away from the work site without telling anyone and go home alone',
          'কাউকে কিছু না বলে কাজের সাইট থেকে গোপনে একা একা বাড়ি ফিরে যাওয়া',
        ),
      ],
      answer: 0,
      why: t(
        'ぎっくり腰{ごし}などの初期{しょき}症状{しょうじょう}で無理{むり}を続{つづ}けると重症化{じゅうしょうか}します。作業{さぎょう}を止{と}めて職長{しょくちょう}に報告{ほうこく}し適切{てきせつ}な処置{しょち}を受{う}けます。',
        'Continuing work with acute lumbago causes severe aggravation; stop working and report to the foreman for proper care.',
        'হঠাৎ কোমর ব্যথার পর জোর করে কাজ চালালে মারাত্মক ক্ষতি হয়; কাজ থামিয়ে ফোরম্যানকে জানিয়ে সঠিক চিকিৎসা নাও।',
      ),
    },
    {
      q: t(
        '棚{だな}に部品{ぶひん}を保管{ほかん}するとき、腰{こし}の負担{ふたん}を減{へ}らすための配置{はいち}として最{もっと}も適切{てきせつ}なものはどれですか？',
        'When storing parts on shelves, which arrangement is most appropriate to reduce strain on the lower back?',
        'তাকের ওপর মালামাল সাজিয়ে রাখার সময় কোমরের চাপ কমাতে সবচেয়ে উপযুক্ত বিন্যাস কোনটি?',
      ),
      options: [
        t(
          '一番{いちばん}重{おも}い金属{きんぞく}部品{ぶひん}を一歩{いっぽ}手前{てまえ}の最上段{さいじょうだん}（頭{あたま}より高{たか}い位置{いち}）に置{お}く',
          'Place the heaviest metal parts on the top shelf higher than head level',
          'সবচেয়ে ভারী ধাতব যন্ত্রাংশ সবার উপরের তাকে (মাথার চেয়ে উঁচুতে) রাখা',
        ),
        t(
          '頻繁{ひんぱん}に使{つか}う重{おも}い部品{ぶひん}を、腰{こし}から胸{むね}の間の高{たか}さの中段{ちゅうだん}に置{お}く',
          'Place frequently used heavy parts on middle shelves between waist and chest height',
          'নিয়মিত ব্যবহৃত ভারী মালামাল কোমর থেকে বুকের উচ্চতার মাঝের তাকে রাখা',
        ),
        t(
          'すべての重量物{じゅうりょうぶつ}を床{ゆか}に直置{じかお}きして、毎回{まいかい}深{ふか}くしゃがませる',
          'Place all heavy items directly on the floor, requiring deep squatting each time',
          'সব ভারী জিনিস মেঝের ওপর ফেলে রাখা যাতে প্রতিবার গভীরভাবে বসতে হয়',
        ),
        t(
          '手前{てまえ}に軽{かる}い物{もの}を置{お}き、奥{おく}の取{と}りにくい場所{ばしょ}に重{おも}い物{もの}を置{お}く',
          'Place light items in front and heavy items deep inside where they are hard to reach',
          'সামনে হালকা জিনিস রাখা আর পেছনের নাগালহীন জায়গায় ভারী জিনিস রাখা',
        ),
      ],
      answer: 1,
      why: t(
        '腰{こし}から胸{むね}の高{たか}さは中腰{ちゅうごし}にならずに持{も}ち上{あ}げられる最{もっと}も安全{あんぜん}な範囲{はんい}です。重量物{じゅうりょうぶつ}は中段{ちゅうだん}に配置{はいち}します。',
        'The height between waist and chest is the safest power zone avoiding stooping; store heavy loads on middle shelves.',
        'কোমর থেকে বুকের উচ্চতা হলো ঝুঁকে না পড়ে মাল তোলার সবচেয়ে নিরাপদ জোন; ভারী জিনিস মাঝের তাকেই রাখতে হয়।',
      ),
    },
    {
      q: t(
        '荷物{にもつ}を運搬{うんぱん}する通路{つうろ}の管理{かんり}について、腰痛{ようつう}や転倒{てんとう}を防{ふせ}ぐために正{ただ}しいものはどれですか？',
        'Regarding walkway management, which is correct for preventing falls and back injuries?',
        'চলাচলের রাস্তা নিয়ন্ত্রণের ক্ষেত্রে পিছলে পড়ে কোমর জখম হওয়া ঠেকাতে সঠিক কোনটি?',
      ),
      options: [
        t(
          '通路{つうろ}に油{あぶら}や水{みず}がこぼれていても、急{いそ}いでいるときはそのまま放置{ほうち}する',
          'Even if oil or water spills in the aisle, leave it alone when in a hurry',
          'হাঁটার পথে তেল বা জল পড়ে থাকলেও ব্যস্ত থাকলে সেভাবেই ফেলে রাখা',
        ),
        t(
          '通路{つうろ}の真{ま}ん中{なか}に余{あま}ったパレットや電線{でんせん}コードを置{お}いておく',
          'Leave surplus pallets and electric cables right in the middle of walkways',
          'চলাচলের পথের মাঝখানে অতিরিক্ত প্যালেট বা বিদ্যুতের তার ফেলে রাখা',
        ),
        t(
          '通路{つうろ}の段差{だんさ}や油{あぶら}・水濡{みずぬ}れを清掃{せいそう}・整頓{せいとん}し、滑{すべ}りにくい安全{あんぜん}な状態{じょうたい}を保{たも}つ',
          'Clean and organize steps, oil, and wet puddles in aisles to maintain a non-slip safe surface',
          'পথের উঁচুনিচু, তেল ও জল পরিষ্কার করে পিছলে না পড়ার মতো নিরাপদ অবস্থা বজায় রাখা',
        ),
        t(
          'つまずきそうな突起{とっき}があっても「気{き}をつけて歩{ある}けばよい」と放置{ほうち}する',
          'Leave tripping hazards alone assuming people can just walk carefully',
          'হোঁচট খাওয়ার মতো উঁচু স্থান থাকলেও "সাবধানে হাঁটলেই হবে" ভেবে ফেলে রাখা',
        ),
      ],
      answer: 2,
      why: t(
        '足元{あしもと}が滑{すべ}ったりつまずいたりすると、体勢{たいせい}を立{た}て直{なお}そうとして腰{こし}に瞬間的{しゅんかんてき}な大荷重{だいかじゅう}がかかり腰痛{ようつう}の原因{げんいん}になります。',
        'Slipping or stumbling causes sudden severe reflex loads on the lower back; keeping walkways clear is vital.',
        'পা পিছলে গেলে ভারসাম্য ঠিক করতে গিয়ে কোমরে হঠাৎ প্রচণ্ড ঝাঁকুনি লাগে যা মারাত্মক কোমর ব্যথার সৃষ্টি করে।',
      ),
    },
    {
      // verify: 厚生労働省「職場における腰痛予防対策指針」では、男性の重量物取扱い上限の目安を体重の約40%、女性はその約60%としている
      q: t(
        '厚生労働省{こうせいろうどうしょう}の指針{ししん}における、成人{せいじん}男性{だんせい}作業者{さぎょうしゃ}が一人{ひとり}で取扱{とりあつか}う重量物{じゅうりょうぶつ}の目安{めやす}として適切{てきせつ}なものはどれですか？',
        'According to MHLW guidelines, what is an appropriate guideline for weight an adult male worker handles alone?',
        'শ্রম মন্ত্রণালয়ের নির্দেশিকা অনুযায়ী একজন পূর্ণবয়স্ক পুরুষ শ্রমিকের একা ভার উত্তোলনের নির্দেশক ওজন কোনটি?',
      ),
      options: [
        t(
          '自分{じぶん}の体重{たいじゅう}と同{おな}じ重{おも}さ（100%）まで',
          'Up to 100% of one own body weight',
          'নিজের ওজনের সমান (১০০%) পর্যন্ত',
        ),
        t(
          '自分{じぶん}の体重{たいじゅう}の80%程度{ていど}まで',
          'Up to about 80% of one own body weight',
          'নিজের ওজনের প্রায় ৮০% পর্যন্ত',
        ),
        t(
          '年齢{ねんれい}や体力{たいりょく}に関係{かんけい}なく一律{いちりつ}50kgまで',
          'A flat 50kg regardless of age or physical strength',
          'বয়স বা শারীরিক শক্তি নির্বিশেষে সবার জন্য ৫০ কেজি',
        ),
        t(
          '自分{じぶん}の体重{たいじゅう}の概{おおむ}ね40%以下{いか}を目安{めやす}とし、無理{むり}のない範囲{はんい}にする',
          'As a rule of thumb, approximately 40% or less of body weight within reasonable limits',
          'নির্দেশক হিসেবে নিজের শরীরের ওজনের প্রায় ৪০% বা তার নিচে যুক্তিসঙ্গত সীমার মধ্যে',
        ),
      ],
      answer: 3,
      why: t(
        '指針{ししん}では男性{だんせい}の単独{たんどく}重量物{じゅうりょうぶつ}取扱{とりあつか}いの上限{じょうげん}目安{めやす}を体重{たいじゅう}の約{やく}40%（女性{じょせい}はその約{やく}60%）としています。',
        'The guideline sets the single-person lift limit for men at roughly 40% of body weight (approx. 60% of that for women).',
        'নির্দেশিকায় পুরুষদের জন্য একা ভার বহনের সীমা শরীরের ওজনের প্রায় ৪০% (মহিলাদের জন্য তার প্রায় ৬০%) নির্ধারণ করা হয়েছে।',
      ),
    },
  ],
  glossary: [
    {
      ja: '腰痛予防',
      reading: 'ようつうよぼう',
      en: 'Back pain prevention',
      bn: 'কোমর ব্যথার প্রতিরোধ',
      note: t(
        '正しい荷物の持ち方や環境改善により、腰の障害を防ぐこと。',
        'Preventing back disorders through correct lifting postures and environmental improvements.',
        'সঠিক পদ্ধতিতে ভার উত্তোলন ও কাজের পরিবেশ উন্নত করে কোমরের আঘাত প্রতিরোধ করা।',
      ),
    },
    {
      ja: '椎間板',
      reading: 'ついかんばん',
      en: 'Intervertebral disc',
      bn: 'মেরুদণ্ডের ডিস্ক',
      note: t(
        '背骨の骨と骨の間にあるクッション。無理な前屈姿勢で強い圧力がかかる。',
        'Cushion between spinal vertebrae. Heavy pressure is applied during forced stooping.',
        'মেরুদণ্ডের হাড়গুলোর মধ্যকার কুশন। কোমর ঝুঁকিয়ে ভার তুললে এর ওপর প্রচণ্ড চাপ পড়ে।',
      ),
    },
    {
      ja: '中腰姿勢',
      reading: 'ちゅうごししせい',
      en: 'Half-crouching posture / stooped posture',
      bn: 'আধ-কুঁজো ভঙ্গি',
      note: t(
        '膝を十分に曲げず腰だけを前に曲げた姿勢。腰への負担が極めて大きい。',
        'Posture where only waist is bent forward without bending knees enough. Heavy strain on back.',
        'হাঁটু পর্যাপ্ত না ভেঙে শুধু কোমর সামনে ঝুঁকিয়ে রাখা ভঙ্গি। কোমরের জন্য অত্যন্ত ক্ষতিকর।',
      ),
    },
    {
      ja: '前屈姿勢',
      reading: 'ぜんくつしせい',
      en: 'Forward bending posture',
      bn: 'সামনে বাঁকা ভঙ্গি',
      note: t(
        '上体を前方に深く倒した姿勢。背筋や腰の筋肉に緊張が続く。',
        'Posture with upper body bent deeply forward. Prolonged tension on back muscles.',
        'শরীরের উপরের অংশ অতিরিক্ত সামনে ঝুঁকিয়ে রাখা ভঙ্গি। পিঠের পেশিতে টান তৈরি করে।',
      ),
    },
    {
      ja: '重量物',
      reading: 'じゅうりょうぶつ',
      en: 'Heavy load / heavy object',
      bn: 'ভারী জিনিস / মালামাল',
      note: t(
        '機械部品や鉄板など、持ち上げや移動に注意が必要な重い物。',
        'Heavy objects such as machine parts or steel plates requiring caution when moving.',
        'মেশিনের যন্ত্রাংশ বা লোহার পাতের মতো ভারী জিনিস, যা সতর্কতার সাথে সরাতে হয়।',
      ),
    },
    {
      ja: '台車',
      reading: 'だいしゃ',
      en: 'Hand truck / push cart / trolley',
      bn: 'হ্যান্ড ট্রলি / ঠেলাগাড়ি',
      note: t(
        '荷物を載せて車輪で押して運ぶ運搬器具。腰の負担を大幅に減らす。',
        'Transport equipment carrying loads on wheels pushed by hand, greatly reducing back strain.',
        'চাকাযুক্ত মালামাল বহনের ট্রলি, যা ঠেলে নেওয়া হয় এবং কোমরের চাপ অনেকাংশে কমায়।',
      ),
    },
    {
      ja: '共同持ち上げ',
      reading: 'きょうどうもちあげ',
      en: 'Coordinated two-person lift',
      bn: 'যৌথভাবে ভার উত্তোলন (দুজনে মিলে তোলা)',
      note: t(
        '重い物を2人以上で呼吸を合わせ、合図とともに持ち上げること。',
        'Lifting heavy objects together by two or more people in coordination with signals.',
        'দুই বা ততোধিক মানুষ সংকেতের সাথে তাল মিলিয়ে একসাথে ভারী জিনিস তোলা।',
      ),
    },
    {
      ja: 'ストレッチ',
      reading: 'すとれっち',
      en: 'Stretching exercise',
      bn: 'স্ট্রেচিং (পেশি টান টান করার ব্যায়াম)',
      note: t(
        '筋肉や腱をゆっくり伸ばして柔軟性を高め、腰痛や怪我を防ぐ運動。',
        'Exercise of gently extending muscles and tendons to increase flexibility and prevent injury.',
        'পেশি ও টেন্ডন ধীরে ধীরে প্রসারিত করে নমনীয়তা বাড়ানো ও আঘাত প্রতিরোধের ব্যায়াম।',
      ),
    },
    {
      ja: '背筋',
      reading: 'せすじ',
      en: 'Back spine / back posture',
      bn: 'মেরুদণ্ডের সোজা ভঙ্গি',
      note: t(
        '首から腰にかけての背骨のライン。荷物を持つときはまっすぐ伸ばす。',
        'Line of the spine from neck to waist. Must be kept straight when lifting loads.',
        'ঘাড় থেকে কোমর পর্যন্ত মেরুদণ্ডের রেখা। মাল তোলার সময় এটি সোজা রাখা আবশ্যক।',
      ),
    },
    {
      ja: 'ぎっくり腰',
      reading: 'ぎっくりごし',
      en: 'Acute lower back strain / lumbago',
      bn: 'হঠাৎ কোমরে তীব্র চোট (গিক্কুরি-গোশি)',
      note: t(
        '重い荷物を持った瞬間などに起こる、急性の激しい腰の痛みや筋膜損傷。',
        'Acute severe back pain or myofascial strain occurring upon lifting heavy loads.',
        'ভারী জিনিস তোলার মুহূর্তে হঠাৎ ঘটে যাওয়া তীব্র কোমর ব্যথা বা পেশির ক্ষয়ক্ষতি।',
      ),
    },
    {
      ja: '大腿部',
      reading: 'だいたいぶ',
      en: 'Thigh muscles / quadriceps',
      bn: 'উরুর পেশি',
      note: t(
        '太ももの太い筋肉。荷物を持ち上げるときは腰ではなくこの力を使う。',
        'Thick muscles of the thighs. Use this power instead of lower back when lifting.',
        'উরুর শক্ত পেশি। জিনিসপত্র তোলার সময় কোমরের বদলে এই পেশির শক্তি ব্যবহার করতে হয়।',
      ),
    },
    {
      ja: '報連相',
      reading: 'ほうれんそう',
      en: 'Report, communicate, consult (Ho-Ren-So)',
      bn: 'হো-রেন-সো (রিপোর্ট, যোগাযোগ ও পরামর্শ)',
      note: t(
        '報告・連絡・相談。腰に痛みや違和感が出たときも速やかに職長へ行う。',
        'Report, Contact, Consult. Promptly notify the foreman when experiencing back pain.',
        'রিপোর্ট, যোগাযোগ ও পরামর্শ। কোমরে ব্যথা বা অস্বস্তি দেখা দিলেও দ্রুত ফোরম্যানকে জানানো।',
      ),
    },
  ],
}
