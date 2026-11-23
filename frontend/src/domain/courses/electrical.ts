import type { CourseData } from './types'
import type { Tri } from '../wakatta'

const t = (ja: string, en: string): Tri => ({ ja, en })

export const electrical: CourseData = {
  id: 'electrical',
  category: 'trade',
  level: 2,
  minutes: 20,
  title: t(
    '感電{かんでん}防止{ぼうし}と電気{でんき}取扱{とりあつかい}の安全{あんぜん}',
    'Electric shock prevention and electrical safety',
  ),
  sub: t(
    '濡{ぬ}れた手{て}の危険{きけん}・漏電{ろうでん}遮断器{しゃだんき}・停電{ていでん}作業{さぎょう}の手順{てじゅん}を学{まな}び、感電{かんでん}災害{さいがい}と火災{かさい}を確実{かくじつ}に防{ふせ}ぐ。',
    'Learn hazards of wet hands, earth-leakage breakers, and power-off procedures to reliably prevent electric shocks and fires.',
  ),
  refs: [
    '労働安全衛生法 第59条第3項（特別教育）',
    '労働安全衛生規則 第36条第4号（低圧電気取扱業務特別教育）', // verify: 低圧電気取扱業務には安衛則第36条第4号に基づく特別教育が必要
    '労働安全衛生規則 第333条（漏電遮断器の設置義務）', // verify: 安衛則第333条により感電防止用漏電遮断器の接続が義務付け
    '労働安全衛生規則 第339条（停電作業時の開閉器施錠等の措置）', // verify: 安衛則第339条により停電作業時の開閉器施錠・通電禁止表示等が義務付け
  ],
  panels: [
    {
      who: 'foreman',
      text: t(
        '今日{きょう}は仮設{かせつ}配線{はいせん}と電動{でんどう}工具{こうぐ}を使{つか}って作業{さぎょう}を行{おこな}います。水気{みずけ}のある場所{ばしょ}なので感電{かんでん}に十分{じゅうぶん}注意{ちゅうい}してください。',
        'Today we are working with temporary wiring and power tools. Take great care against electric shocks in wet areas.',
      ),
    },
    {
      who: 'worker',
      text: t(
        'はい！作業{さぎょう}を急{いそ}ぐので、手{て}を洗{あら}って濡{ぬ}れたままですが、コードリールをコンセントに差{さ}し込{こ}みます！',
        'Yes! In a hurry to start, my hands are still wet from washing, but I will plug the cord reel into the outlet right now!',
      ),
    },
    {
      who: 'foreman',
      text: t(
        'ストップ！濡{ぬ}れた手{て}でプラグを触{さわ}ってはいけません！水{みず}で皮膚{ひふ}の抵抗{ていこう}が激減{げきげん}し、命{いのち}に関{かか}わる大電流{だいでんりゅう}が流{なが}れます。手{て}を拭{ふ}きなさい。',
        'Stop! Never touch plugs with wet hands! Water drastically lowers skin resistance, allowing fatal currents to flow. Dry your hands.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '危{あぶ}ないところでした！乾{かわ}いたタオルで手{て}を拭{ふ}きました。電工{でんこう}ドラム（コードリール）は巻{ま}いたままで使{つか}ってもいいですか？',
        'That was close! I dried my hands with a towel. Can I use the cord reel while it is still coiled up?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '巻{ま}いたまま使{つか}うと熱{ねつ}がこもって被覆{ひふく}が溶{と}け、火災{かさい}やショートの原因{げんいん}になります。全{すべ}て引{ひ}き出{だ}して使{つか}いなさい。', // verify: 屋内型電工ドラムの許容電流は巻いた状態で約5A、全て引き出した状態で15A
        'Using it coiled traps heat and melts insulation, causing fires and short circuits. Always pull the entire cord out.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '全部{ぜんぶ}引{ひ}き出{だ}しました！このコンセントについている漏電{ろうでん}遮断器{しゃだんき}のテストボタンは何{なん}ですか？',
        'I pulled it all out! What is this test button on the earth-leakage breaker on the outlet?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '作業前{さぎょうまえ}にテストボタンを押{お}して、瞬時{しゅんじ}に切{き}れるか確認{かくにん}します。漏電{ろうでん}時{じ}に電気{でんき}を止{と}める命{いのち}の装置{そうち}です。',
        'Push the test button before work to confirm it trips instantly. It is a lifesaver that cuts power during leakage.',
      ),
    },
    {
      who: 'worker',
      text: t(
        'カチッと切{き}れてレバーが落{お}ちました！リセットして電源{でんげん}を入{い}れます。高所{こうしょ}の架空{かくう}電線{でんせん}の近{ちか}くでも作業{さぎょう}がありますね。',
        'It clicked and the lever tripped! I will reset it and turn power on. We also have work near overhead power lines above, right?',
      ),
    },
    {
      who: 'foreman',
      text: t(
        '架空{かくう}電線{でんせん}には十分{じゅうぶん}な離隔{りかく}距離{きょり}を取{と}り、防護{ぼうご}管{かん}の確認{かくにん}と監視員{かんしいん}の配置{はいち}を行{おこな}います。', // verify: クレーンや足場の電線離隔距離基準（低圧1m以上、高圧2m以上、特別高圧離隔基準）
        'Keep ample clearance from overhead lines, verify protective insulating covers are fitted, and assign a dedicated lookout.',
      ),
    },
    {
      who: 'worker',
      text: t(
        '手袋{てぶくろ}乾燥{かんそう}、コード点検{てんけん}、漏電{ろうでん}遮断器{しゃだんき}よし！指差呼称{しさこしょう}で安全{あんぜん}第一{だいいち}で進{すす}めます！',
        'Dry gloves, cord inspection, earth-leakage breaker yoshi! I will proceed safety first with pointing and calling!',
      ),
    },
  ],
  handbook: [
    t(
      '水{みず}や汗{あせ}で濡{ぬ}れた手{て}で、プラグやスイッチ・電気{でんき}機器{きき}に絶対{ぜったい}に触{ふ}れてはならない。',
      'Never touch plugs, switches, or electrical equipment with hands wet with water or sweat.',
    ),
    t(
      '使用前{しようまえ}に必{かなら}ずコードの被覆{ひふく}破{やぶ}れ、芯線{しんせん}の露出{ろしゅつ}、プラグの曲{ま}がりや緩{ゆる}みを点検{てんけん}する。',
      'Always inspect cords before use for torn insulation, exposed inner wires, and bent or loose plug pins.',
    ),
    t(
      '水気{みずけ}や湿気{しっき}のある場所{ばしょ}で電動{でんどう}工具{こうぐ}を使用{しよう}する際{さい}は、感度{かんど}15mA以下{いか}の漏電{ろうでん}遮断器{しゃだんき}を経由{けいゆ}して接続{せつぞく}すること。', // verify: 水気・湿気のある場所の感電防止用漏電遮断器は定格感度電流15mA以下・動作時間0.1秒以内
      'When using power tools in wet or damp areas, connect through an earth-leakage breaker rated at 15mA or less.',
    ),
    t(
      '漏電{ろうでん}遮断器{しゃだんき}のテストボタンを作業{さぎょう}開始前{かいしまえ}に押{お}し、正常{せいじょう}にカチッとトリップ（作動{さどう}）することを毎日{まいにち}確認{かくにん}する。', // verify: 漏電遮断器の作動確認は安衛則等により作業開始前のテストボタン押下が推奨・義務付け
      'Push the test button on the earth-leakage breaker before daily work to confirm it clicks and trips normally.',
    ),
    t(
      '電工{でんこう}ドラム（コードリール）は、発熱{はつねつ}による被覆{ひふく}溶損{ようそん}や火災{かさい}を防{ふせ}ぐため、巻{ま}いたまま使{つか}わず必{かなら}ず全{すべ}て引{ひ}き出{だ}して使用{しよう}する。', // verify: 屋内型電工ドラムの許容電流は巻いた状態で約5A、全て引き出した状態で15A
      'Never use extension cord reels coiled up; always pull out the entire cord to prevent heat buildup and melted fires.',
    ),
    t(
      '電気{でんき}配線{はいせん}や機器{きき}の点検{てんけん}・補修{ほしゅう}は必{かなら}ず主電源{しゅでんげん}を切{き}り、ロックアウト・タグアウト（施錠{せじょう}と通電{つうでん}禁止{きんし}表示{ひょうじ}）を徹底{てってい}する。', // verify: 停電作業時の開閉器施錠・通電禁止表示・無電圧確認は安衛則第339条に規定
      'Always cut main power for inspections and repairs, enforcing Lockout/Tagout (locking switches and posting Do-Not-Energize signs).',
    ),
    t(
      '停電{ていでん}作業{さぎょう}を行{おこな}う際{さい}は、作業{さぎょう}に着手{ちゃくしゅ}する直前{ちょくぜん}に検電器{けんでんき}を使{つか}って無電圧{むでんあつ}状態{じょうたい}（通電{つうでん}していないこと）を指差呼称{しさこしょう}で確認{かくにん}する。',
      'Before starting de-energized work, always use a voltage tester to verify zero voltage by pointing and calling.',
    ),
    t(
      '二重{にじゅう}絶縁{ぜつえん}構造{こうぞう}（回マーク）でない電動{でんどう}工具{こうぐ}は、接地線{せっちせん}（アース線{せん}）を確実{かくじつ}に接地極{せっちきょく}へ接続{せつぞく}しなければならない。',
      'Power tools without double-insulation mark must have their ground wire securely connected to an earth ground.',
    ),
    t(
      '高所{こうしょ}作業車{さぎょうしゃ}や足場{あしば}作業{さぎょう}では、近接{きんせつ}する架空{かくう}電線{でんせん}から安全{あんぜん}な離隔{りかく}距離{きょり}を保{たも}ち、絶縁{ぜつえん}防護{ぼうご}管{かん}の装着{そうちゃく}を事業所{じぎょうしょ}に確認{かくにん}する。', // verify: クレーンや足場の電線離隔距離基準（低圧1m以上、高圧2m以上、特別高圧離隔基準）
      'When working near overhead power lines, maintain safe clearance distances and ensure insulating covers are installed.',
    ),
    t(
      '感電{かんでん}した被災者{ひさいしゃ}を発見{はっけん}した際{さい}は、素手{すで}で触{さわ}らず直{ただ}ちに電源{でんげん}を切{き}り、大声{おおごえ}で119番{ばん}通報{つうほう}とAEDを手配{てはい}する。',
      'If you find an electric shock victim, never touch them with bare hands; cut power immediately, call 119, and get an AED.',
    ),
  ],
  reel: [
    t(
      '手{て}の乾燥{かんそう}・濡{ぬ}れ手{て}なし、ヨシ！',
      'Hands dry, no moisture, yoshi!',
    ),
    t(
      '電源{でんげん}コード・被覆{ひふく}損傷{そんしょう}なし、ヨシ！',
      'Power cord, insulation undamaged, yoshi!',
    ),
    t(
      '漏電{ろうでん}遮断器{しゃだんき}、作動{さどう}テスト、ヨシ！',
      'Earth-leakage breaker, trip test passed, yoshi!',
    ),
    t(
      '電工{でんこう}ドラム、全線{ぜんせん}引{ひ}き出{だ}し、ヨシ！',
      'Extension cord reel, fully uncoiled, yoshi!',
    ),
    t(
      'アース接地{せっち}、確実{かくじつ}に接続{せつぞく}、ヨシ！',
      'Earth ground, securely connected, yoshi!',
    ),
    t(
      '主電源{しゅでんげん}OFF・ロックアウト施錠{せじょう}、ヨシ！',
      'Main power switch OFF, lockout locked, yoshi!',
    ),
    t(
      '検電器{けんでんき}、無電圧{むでんあつ}確認{かくにん}、ヨシ！',
      'Voltage tester, zero voltage confirmed, yoshi!',
    ),
    t(
      '架空{かくう}電線{でんせん}、離隔{りかく}距離{きょり}確保{かくほ}、ヨシ！',
      'Overhead power lines, safe clearance confirmed, yoshi!',
    ),
  ],
  drills: [
    {
      id: 'electrical-spot-kyt',
      kind: 'spot',
      secs: 30,
      need: 5,
      name: t(
        '電気{でんき}作業{さぎょう}現場{げんば}の危険{きけん}予知{よち}（KYT）',
        'Electrical safety hazard spotting (KYT)',
      ),
      goal: t(
        '30秒{びょう}で感電{かんでん}や火災{かさい}の危険{きけん}を5つ見{み}つける。',
        'Find 5 electrical shock and fire hazards in 30 seconds.',
      ),
      items: [
        {
          hazard: true,
          label: t(
            '濡{ぬ}れた手{て}で電動{でんどう}グラインダーの電源{でんげん}プラグを差{さ}し込{こ}もうとしている',
            'Plugging in an electric grinder with wet hands',
          ),
        },
        {
          hazard: false,
          label: t(
            'コードリールから電線{でんせん}が全{すべ}て引{ひ}き出{だ}されて配置{はいち}されている',
            'Extension cord is fully pulled out from the reel and neatly laid',
          ),
        },
        {
          hazard: true,
          label: t(
            '電源{でんげん}コードの被覆{ひふく}が破{やぶ}れて内部{ないぶ}の銅線{どうせん}がむき出{だ}しになっている',
            'Power cord sheath is torn with bare copper wire exposed',
          ),
        },
        {
          hazard: false,
          label: t(
            '漏電{ろうでん}遮断器{しゃだんき}のテストボタンを押{お}して正常{せいじょう}な作動{さどう}を確認{かくにん}している',
            'Pressing the test button on the earth-leakage breaker to verify proper tripping',
          ),
        },
        {
          hazard: true,
          label: t(
            '大型{おおがた}ヒーターを接続{せつぞく}した電工{でんこう}ドラムが全{すべ}て巻{ま}かれたまま使用{しよう}されている',
            'Heavy heater operating on an extension reel that is left completely coiled up',
          ),
        },
        {
          hazard: false,
          label: t(
            '金属{きんぞく}外郭{がいかく}の電動{でんどう}工具{こうぐ}のアース線{せん}が接地極{せっちきょく}にしっかり接続{せつぞく}されている',
            'Earth wire of metal-cased power tool is firmly connected to grounding terminal',
          ),
        },
        {
          hazard: true,
          label: t(
            '配電盤{はいでんばん}の停電{ていでん}作業{さぎょう}中{ちゅう}なのにスイッチに施錠{せじょう}も通電{つうでん}禁止{きんし}の札{ふだ}もない',
            'Switchboard de-energized work underway without lockout padlock or warning tag',
          ),
        },
        {
          hazard: false,
          label: t(
            '架空{かくう}電線{でんせん}に黄色{きいろ}の絶縁{ぜつえん}防護{ぼうご}管{かん}が装着{そうちゃく}され監視員{かんしいん}が配置{はいち}されている',
            'Yellow insulating sleeves fitted on overhead lines with a lookout stationed',
          ),
        },
        {
          hazard: true,
          label: t(
            '通路{つうろ}の水{みず}たまりの中{なか}に電源{でんげん}プラグとコンセントの接続部{せつぞくぶ}が沈{しず}んでいる',
            'Plug and receptacle connection submerged in a water puddle on the walkway',
          ),
        },
        {
          hazard: false,
          label: t(
            '停電{ていでん}作業{さぎょう}の前{まえ}に検電器{けんでんき}を使{つか}って電気が流{なが}れていないことを確認{かくにん}している',
            'Testing with a voltage detector before power-off work to confirm zero voltage',
          ),
        },
      ],
    },
    {
      id: 'electrical-loto-seq',
      kind: 'seq',
      name: t(
        '停電{ていでん}作業{さぎょう}とロックアウトの手順{てじゅん}',
        'De-energized work and Lockout/Tagout sequence',
      ),
      goal: t(
        '感電{かんでん}を防{ふせ}ぐため、停電{ていでん}作業{さぎょう}の安全{あんぜん}手順{てじゅん}を正{ただ}しい順番{じゅんばん}に並{なら}べる。',
        'Arrange the safety steps for de-energized work in the correct order to prevent shock.',
      ),
      steps: [
        t(
          '関係者{かんけいしゃ}へ作業{さぎょう}計画{けいかく}と停電{ていでん}時間{じかん}を周知{しゅうち}し、周囲{しゅうい}の安全{あんぜん}を確認{かくにん}する',
          'Inform all affected workers of work plan and power outage time, checking perimeter safety',
        ),
        t(
          '対象{たいしょう}回路{かいろ}の主開閉器{しゅかいへいき}（ブレーカー）をOFFにして電気{でんき}を遮断{しゃだん}する',
          'Turn OFF the main breaker of the target circuit to cut power',
        ),
        t(
          'ブレーカーに南京錠{なんきんじょう}で施錠{せじょう}し「送電{そうでん}禁止{きんし}・作業中{さぎょうちゅう}」のタグ（標識{ひょうしき}）を取{と}り付{つ}ける',
          'Lock the breaker with a padlock and attach a "DO NOT ENERGIZE - WORK IN PROGRESS" tag',
        ),
        t(
          '校正{こうせい}された検電器{けんでんき}を使{つか}い、全{すべ}ての相{そう}で無電圧{むでんあつ}（電気が流{なが}れていないこと）を確認{かくにん}する',
          'Use a calibrated voltage detector to verify zero voltage across all phases',
        ),
        t(
          '必要{ひつよう}に応{おう}じて短絡{たんらく}接地{せっち}器具{きぐ}を取{と}り付{つ}け、指差呼称{しさこしょう}を行{おこな}ってから作業{さぎょう}に着手{ちゃくしゅ}する',
          'Attach short-circuit grounding conductors if required, then begin work after pointing and calling',
        ),
      ],
    },
    {
      id: 'electrical-firstaid-seq',
      kind: 'seq',
      name: t(
        '感電{かんでん}事故{じこ}発生時{はっせいじ}の緊急{きんきゅう}救護{きゅうご}手順{てじゅん}',
        'Electric shock emergency response and first aid sequence',
      ),
      goal: t(
        '二次{にじ}災害{さいがい}を防{ふせ}ぎ、感電{かんでん}被災者{ひさいしゃ}の命{いのち}を救{すく}う救護{きゅうご}手順{てじゅん}を順番{じゅんばん}に並{なら}べる。',
        'Arrange rescue and first aid steps in order to prevent secondary shocks and save lives.',
      ),
      steps: [
        t(
          '被災者{ひさいしゃ}に素手{すで}で触{さわ}らず、直{ただ}ちに開閉器{かいへいき}を切{き}るか乾{かわ}いた木{き}の棒{ぼう}等{とう}の絶縁物{ぜつえんぶつ}で引{ひ}き離{はな}す',
          'Never touch victim with bare hands; cut power immediately or separate with a dry wooden stick or insulator',
        ),
        t(
          '感電{かんでん}の危険{きけん}がない安全{あんぜん}な場所{ばしょ}であることを確認{かくにん}し、大声{おおごえ}で周囲{しゅうい}に応援{おうえん}を呼{よ}ぶ',
          'Confirm area is safe from electrical hazards, and call loudly for help from colleagues',
        ),
        t(
          '119番{ばん}通報{つうほう}（救急車{きゅうきゅうしゃ}要請{ようせい}）とAED（自動体外式除細動器{じどうたいがいしきじょさいどうき}）の搬送{はんそう}を具体例{ぐたいれい}を指名{しめい}して指示{しじ}する',
          'Designate specific persons to call 119 for an ambulance and bring an AED immediately',
        ),
        t(
          '被災者{ひさいしゃ}の意識{いしき}と呼吸{こきゅう}を確認{かくにん}し、普段{ふだん}通{どお}りの呼吸{こきゅう}がない場合{ばあい}は直{ただ}ちに胸骨{きょうこつ}圧迫{あっぱく}（心肺蘇生{しんぱいそせい}）を開始{かいし}する',
          'Check consciousness and breathing; if not breathing normally, begin chest compressions immediately',
        ),
        t(
          'AEDが届{とど}いたら電極{でんきょく}パッドを素早{すばや}く貼{は}り、音声{おんせい}ガイダンスに従{したが}って電気{でんき}ショックを実施{じっし}する',
          'As soon as AED arrives, apply electrode pads quickly and follow voice prompts to deliver a shock',
        ),
      ],
    },
    {
      id: 'electrical-estop-reflex',
      kind: 'reflex',
      name: t(
        '感電{かんでん}・発煙{はつえん}の緊急{きんきゅう}停止{ていし}反射{はんしゃ}',
        'Emergency power cutoff reflex for shock and smoke',
      ),
      goal: t(
        '配線{はいせん}からのスパークや感電{かんでん}の瞬間{しゅんかん}、0.4秒{びょう}以内{いない}に非常{ひじょう}停止{ていし}ボタンを押{お}す。',
        'At the moment of sparks or electric shock, hit the emergency stop button within 0.4 seconds.',
      ),
    },
  ],
  inspect: [
    {
      id: 'electrical-cordreel-inspect',
      defective: true,
      name: t(
        'コードリール（電工{でんこう}ドラム）・延長{えんちょう}コードの点検{てんけん}',
        'Extension cord reel and cable inspection',
      ),
      spots: [
        {
          x: 25,
          y: 35,
          check: t(
            '電線{でんせん}被覆{ひふく}の亀裂{きれつ}・芯線{しんせん}露出{ろしゅつ}：被覆{ひふく}の破{やぶ}れやビニールテープ補修{ほしゅう}がないか（良品{りょうひん}：被覆{ひふく}に傷{きず}がなく健全{けんぜん} / 不良{ふりょう}：被覆{ひふく}が破{やぶ}れて銅線{どうせん}が露出{ろしゅつ}）',
            'Cable insulation damage: No tears or vinyl tape patches (Good: Smooth undamaged jacket / Defective: Torn jacket exposing bare copper wires)',
          ),
        },
        {
          x: 50,
          y: 55,
          check: t(
            '電源{でんげん}プラグの刃{は}と根元{ねもと}：刃{は}の曲{ま}がりや焦{こ}げ・変形{へんけい}がないか（良品{りょうひん}：刃{は}がまっすぐで絶縁{ぜつえん}部{ぶ}正常{せいじょう} / 不良{ふりょう}：刃{は}が折{お}れ曲{ま}がり根元{ねもと}が炭化{たんか}）',
            'Plug blades and base: No bending, burning, or cracking (Good: Straight blades with sound insulation / Defective: Bent blades or burnt blackened base)',
          ),
        },
        {
          x: 75,
          y: 70,
          check: t(
            'ドラム枠{わく}とリール回転{かいてん}機構{きこう}：ドラムの割{わ}れや電線{でんせん}の噛{か}み込{こ}みがないか（良品{りょうひん}：円滑{えんかつ}に回転{かいてん}し全線{ぜんせん}引{ひ}き出{だ}し可能{かのう} / 不良{ふりょう}：枠{わく}が割{わ}れて電線{でんせん}が引{ひ}っ掛{か}かる）',
            'Reel frame and rotation mechanism: No cracks or jammed cord (Good: Rotates smoothly for full cord extension / Defective: Cracked frame snagging cable)',
          ),
        },
      ],
    },
    {
      id: 'electrical-breaker-inspect',
      defective: false,
      name: t(
        'ポータブル漏電{ろうでん}遮断器{しゃだんき}（ブレーカー）の点検{てんけん}',
        'Portable earth-leakage circuit breaker inspection',
      ),
      spots: [
        {
          x: 30,
          y: 30,
          check: t(
            'テストボタンによるトリップ作動{さどう}：ボタンを押{お}した瞬時{しゅんじ}にレバーがOFFに落{お}ちるか（良品{りょうひん}：カチッと即座{そくざ}にトリップする / 不良{ふりょう}：ボタンを押{お}しても反応{はんのう}しない）',
            'Trip function via test button: Lever trips to OFF instantly when pressed (Good: Clicks and trips instantly / Defective: No reaction when pressed)',
          ),
        },
        {
          x: 55,
          y: 55,
          check: t(
            '接地{せっち}端子{たんし}（アース極{きょく}）の接続{せつぞく}：アースクリップやプラグ接地{せっち}極{きょく}が健全{けんぜん}か（良品{りょうひん}：確実{かくじつ}に接地{せっち}極{きょく}へ繋{つな}がる / 不良{ふりょう}：アース線{せん}が断線{だんせん}・端子{たんし}脱落{だつらく}）',
            'Earth terminal connection: Ground pin and clamp intact (Good: Securely connects to ground / Defective: Broken earth wire or missing pin)',
          ),
        },
        {
          x: 70,
          y: 75,
          check: t(
            '外郭{がいかく}ケースの防雨{ぼうう}・防塵{ぼうじん}性{せい}：パッキンの劣化{れっか}や本体{ほんたい}ケースの割{わ}れがないか（良品{りょうひん}：隙間{すきま}なく密閉{みっぺい}され水気{みずけ}なし / 不良{ふりょう}：カバー破損{はそん}で内部{ないぶ}に浸水{しんすい}）',
            'Enclosure weatherproofing: No casing cracks or deteriorated gaskets (Good: Sealed tightly with no moisture inside / Defective: Broken cover with water ingress)',
          ),
        },
      ],
    },
    {
      id: 'electrical-powertool-inspect',
      defective: true,
      name: t(
        '電動{でんどう}工具{こうぐ}（ディスクグラインダー）の電気{でんき}系統{けいとう}点検{てんけん}',
        'Power tool (disc grinder) electrical inspection',
      ),
      spots: [
        {
          x: 20,
          y: 40,
          check: t(
            'コード導入部{どうにゅうぶ}（コードガード）の状態{じょうたい}：屈曲{くっきょく}による被覆{ひふく}の引{ひ}っ張{ぱ}りや裂{さ}けがないか（良品{りょうひん}：ゴムガードが強固{きょうこ}で保護{ほご}良好{りょうこう} / 不良{ふりょう}：ガードが抜{ぬ}けて内部{ないぶ}線{せん}が屈曲{くっきょく}疲労{ひろう}で破断{はだん}寸前{すんぜん}）',
            'Cord entry sleeve (cord guard): No stretching or tearing from bending (Good: Firm rubber guard protecting cord / Defective: Guard slipped out, internal wires near breaking)',
          ),
        },
        {
          x: 50,
          y: 50,
          check: t(
            '絶縁{ぜつえん}ハウジング（本体{ほんたい}ケース）の亀裂{きれつ}：二重{にじゅう}絶縁{ぜつえん}ボディの割{わ}れやネジの緩{ゆる}みがないか（良品{りょうひん}：樹脂{じゅし}ケースが無傷{むきず} / 不良{ふりょう}：ケースに深{ふか}い亀裂{きれつ}があり内部{ないぶ}モーター充電部{じゅうでんぶ}に触{ふ}れる危険{きけん}）',
            'Insulating housing condition: No cracks in double-insulated body or loose screws (Good: Flawless plastic shell / Defective: Deep crack exposing live internal motor parts)',
          ),
        },
        {
          x: 80,
          y: 65,
          check: t(
            '電源{でんげん}スイッチの操作{そうさ}と火花{ひばな}：ON/OFFが軽快{けいかい}で作動{さどう}時{じ}に内部{ないぶ}から過大{かだい}なスパーク・異臭{いしゅう}がないか（良品{りょうひん}：カチッと確実{かくじつ}に切{き}れる / 不良{ふりょう}：スイッチが戻{もど}らず連続{れんぞく}火花{ひばな}と焦{こ}げ臭{くさ}さ）',
            'Power switch operation and sparking: Smooth ON/OFF without excessive internal sparks or smell (Good: Crisp positive shutoff / Defective: Sticky switch with continuous sparks and burning odor)',
          ),
        },
      ],
    },
  ],
  quiz: [
    {
      q: t(
        '夏場{なつば}の暑{あつ}い日{ひ}、作業者{さぎょうしゃ}が手{て}を水{みず}で洗{あら}った直後{ちょくご}、汗{あせ}と水分{すいぶん}でびしょ濡{ぬ}れになった手{て}のまま電動{でんどう}工具{こうぐ}の電源{でんげん}プラグを差{さ}し込{こ}もうとしました。この行動{こうどう}の危険性{きけんせい}と正{ただ}しい対応{たいおう}として、適切{てきせつ}なものはどれですか？',
        'On a hot summer day, right after washing hands with water, a worker tried to plug in a power tool while hands were soaking wet with sweat and water. Which describes the danger and correct action?',
      ),
      options: [
        t(
          '純粋{じゅんすい}な水{みず}は電気{でんき}を全{まった}く通{とお}さないため、手{て}が濡{ぬ}れていても感電{かんでん}の危険{きけん}はなく、そのまま差{さ}し込{こ}んで良{よ}い',
          'Pure water does not conduct electricity at all, so wet hands pose no shock hazard and plugging in as is is safe',
        ),
        t(
          '濡{ぬ}れた手{て}でも片手{かたて}だけでプラグをつまめば電気{でんき}が心臓{しんぞう}を通{とお}らないので安全{あんぜん}である',
          'Even with wet hands, pinching the plug with one hand keeps current from passing through heart and is safe',
        ),
        t(
          '水{みず}や汗{あせ}で皮膚{ひふ}の電気{でんき}抵抗{ていこう}が大幅{おおはば}に低下{ていか}し、致死{ちし}的{てき}な大電流{だいでんりゅう}が流{なが}れて感電{かんでん}死{し}する危険{きけん}があるため、必{かなら}ず乾{かわ}いたタオルで拭{ふ}いてから作業{さぎょう}する',
          'Water and sweat drastically drop skin resistance, risking fatal electric shock, so hands must be dried completely with a towel first',
        ),
        t(
          '手{て}が濡{ぬ}れているときは、息{いき}を強{つよ}く吹{ふ}きかけて風{かぜ}を送{おく}りながら差{さ}し込{こ}めば何{なに}も問題{もんだい}ない',
          'When hands are wet, blowing strongly on them while plugging in eliminates all danger',
        ),
      ],
      answer: 2,
      why: t(
        '人{ひと}の皮膚{ひふ}は水{みず}や汗{あせ}で濡{ぬ}れると電気{でんき}抵抗{ていこう}が乾{かわ}いた状態{じょうたい}の数十分{すうじゅうぶん}の1に激減{げきげん}し、100Vの工事用{こうじよう}電源{でんげん}でも命{いのち}に関{かか}わる大電流{だいでんりゅう}が流{なが}れます。必{かなら}ず手{て}を完全{かんぜん}に乾{かわ}かして作業{さぎょう}します。',
        'Wet skin drops electrical resistance to a fraction of dry skin, allowing fatal current even from standard 100V supplies. Hands must be completely dried before touching electrical gear.',
      ),
    },
    {
      q: t(
        '工事{こうじ}現場{げんば}で30メートルの電工{でんこう}ドラム（コードリール）を使{つか}い、1500Wの大型{おおがた}投光器{とうこうき}と電動{でんどう}ハンマーを同時{どうじ}に使用{しよう}します。コードを巻{ま}いたまま使{つか}った場合{ばあい}に生{しょう}じる危険{きけん}として、正{ただ}しいものはどれですか？',
        'At a job site, using a 30m extension reel to power a 1500W floodlight and electric hammer simultaneously. Which describes the danger if used while coiled?',
      ),
      options: [
        t(
          '巻{ま}いた状態{じょうたい}では許容{きょよう}電流{でんりゅう}が約{やく}5Aに低下{ていか}し、コードに熱{ねつ}がこもって被覆{ひふく}が溶融{ようゆう}・ショートして火災{かさい}や感電{かんでん}を引{ひ}き起{お}こす', // verify: 屋内型電工ドラムの許容電流は巻いた状態で約5A、全て引き出した状態で15A
          'Coiled, allowable current drops to ~5A; heat buildup melts insulation, causing shorts, fires, and electric shock',
        ),
        t(
          '巻{ま}いたまま使{つか}うと電気{でんき}の流{なが}れるスピードが速{はや}くなりすぎて、接続{せつぞく}した電動{でんどう}工具{こうぐ}が壊{こわ}れる',
          'Using it coiled makes electricity flow too fast, destroying connected power tools',
        ),
        t(
          '電線{でんせん}が冷{ひ}えすぎて凍結{とうけつ}し、内部{ないぶ}の銅線{どうせん}が折{お}れて電気が流{なが}れなくなる',
          'The wire becomes overly cold and freezes, breaking inner copper wires and cutting current',
        ),
        t(
          'コードを巻{ま}いておくと電気{でんき}がドラムの中{なか}に蓄電{ちくでん}されて省{しょう}エネになり、安全{あんぜん}上{じょう}推奨{すいしょう}される',
          'Keeping the cord coiled stores power inside the reel for energy saving and is safety-recommended',
        ),
      ],
      answer: 0,
      why: t(
        '電工{でんこう}ドラムは巻{ま}いたままだと熱{ねつ}が放熱{ほうねつ}されず、許容{きょよう}電流{でんりゅう}が全引出し時{ぜんひきだしじ}（15A）の3分{ぶん}の1（約{やく}5A）に激減{げきげん}します。過熱{かねつ}による火災{かさい}と被覆{ひふく}溶損{ようそん}を防{ふせ}ぐため必{かなら}ず全線{ぜんせん}引{ひ}き出{だ}して使{つか}います。',
        'Coiled cord reels cannot dissipate heat, cutting allowable capacity from 15A to ~5A. To prevent melted insulation and fires, always uncoil completely.',
      ),
    },
    {
      q: t(
        '造船所{ぞうせんじょ}や建設{けんせつ}現場{げんば}で可搬式{かはんしき}の漏電{ろうでん}遮断器{しゃだんき}（感電{かんでん}防止{ぼうし}用{よう}）を使用{しよう}する際{さい}の日常{にちじょう}点検{てんけん}として、義務{ぎむ}付{づ}けられている正{ただ}しい方法{ほうほう}はどれですか？',
        'What is the mandatory daily inspection method when using a portable earth-leakage circuit breaker at a shipyard or construction site?',
      ),
      options: [
        t(
          '1年間{ねんかん}に1度{ど}だけ専門{せんもん}業者{ぎょうしゃ}に点検{てんけん}を頼{たの}めば、毎朝{まいあさ}の点検{てんけん}は一切{いっさい}行{おこな}わなくてよい',
          'Having an external specialist inspect once a year is enough; daily morning inspections are never needed',
        ),
        t(
          '水{みず}たまりの中{なか}に漏電{ろうでん}遮断器{しゃだんき}を浸{つ}けてみて、煙{けむり}が出{で}ないことを肉眼{にくがん}で確{たし}かめる',
          'Submerge breaker in water puddle to see with bare eyes that no smoke comes out',
        ),
        t(
          'ドライバーで本体{ほんたい}ケースを分解{ぶんかい}し、内部{ないぶ}の回路{かいろ}基板{きばん}を手{て}で触{さわ}って確認{かくにん}する',
          'Disassemble the casing with a screwdriver and touch inner circuit board by hand',
        ),
        t(
          'その日{ひ}の作業{さぎょう}開始前{かいしまえ}に「テストボタン」を押{お}し、瞬時{しゅんじ}にレバーが落ちて電気{でんき}が遮断{しゃだん}されることを確認{かくにん}する', // verify: 漏電遮断器の作動確認は安衛則等により作業開始前のテストボタン押下が推奨・義務付け
          'Push the "Test Button" before starting daily work to confirm lever trips instantly and cuts off power',
        ),
      ],
      answer: 3,
      why: t(
        '漏電{ろうでん}遮断器{しゃだんき}は、作業{さぎょう}開始前{かいしまえ}に必{かなら}ずテストボタンを押{お}して正常{せいじょう}にトリップ（遮断{しゃだん}）するかを確認{かくにん}しなければなりません。故障{こしょう}した遮断器{しゃだんき}を使{つか}っていると万一{まんいち}の漏電{ろうでん}時{じ}に電気{でんき}が止{と}まらず致命傷{ちめいしょう}になります。',
        'Always press the test button before work to confirm the breaker trips instantly. Using a faulty breaker will fail to protect against fatal shock during leakage.',
      ),
    },
    {
      q: t(
        '分電盤{ぶんでんばん}の回路{かいろ}補修{ほしゅう}や配線{はいせん}工事{こうじ}を行{おこな}うため、「停電{ていでん}作業{さぎょう}」を実施{じっし}します。感電{かんでん}を防{ふせ}ぐための安全{あんぜん}措置{そち}の順序{じゅんじょ}として、正{ただ}しいものはどれですか？',
        'When conducting de-energized work for switchboard repair or wiring, which represents the correct sequence of safety precautions to prevent electric shock?',
      ),
      options: [
        t(
          'ブレーカーを落{お}としたらすぐに作業{さぎょう}を始{はじ}め、作業{さぎょう}後{ご}に検電器{けんでんき}で電気が来{き}ていないか調{しら}べる',
          'Start work immediately after tripping breaker, and check for zero voltage with a tester only after finishing',
        ),
        t(
          '主開閉器{しゅかいへいき}をOFFにし、スイッチに南京錠{なんきんじょう}で施錠{せじょう}（ロックアウト）して「作業中{さぎょうちゅう}・通電{つうでん}禁止{きんし}」のタグを掲示{けいじ}し、検電器{けんでんき}で無電圧{むでんあつ}を確認{かくにん}してから着手{ちゃくしゅ}する', // verify: 停電作業時の開閉器施錠・通電禁止表示・無電圧確認は安衛則第339条に規定
          'Turn OFF main breaker, lock switch with padlock (Lockout), post "DO NOT ENERGIZE" tag, and verify zero voltage with detector before starting',
        ),
        t(
          '誰{だれ}かが間違{まちが}えてブレーカーを入{い}れないよう、ブレーカーの前に見張{みは}り番{ばん}を置{お}くだけで施錠{せじょう}や検電{けんでん}は省略{しょうりゃく}してよい',
          'Just placing a lookout in front of breaker without padlock or voltage check is sufficient to prevent errors',
        ),
        t(
          '通電{つうでん}したまま（活線{かっせん}作業{さぎょう}）作業{さぎょう}した方{ほう}が異常{いじょう}がすぐ分{わ}かるので、手袋{てぶくろ}なしでそのまま触{さわ}る',
          'Working while energized makes anomalies instantly noticeable, so touch conductors directly without gloves',
        ),
      ],
      answer: 1,
      why: t(
        '停電{ていでん}作業{さぎょう}では、開閉器{かいへいき}の遮断{しゃだん}、誤操作{ごそうさ}を防{ふせ}ぐロックアウト（施錠{せじょう}）とタグアウト（標識{ひょうしき}掲示{けいじ}）、そして検電器{けんでんき}による無電圧{むでんあつ}確認{かくにん}が法令{ほうれい}上の厳格{げんかく}な必須{ひっす}手順{てじゅん}です。',
        'De-energized work strictly mandates opening breakers, lockout padlocking, tagout labeling, and voltage detector verification before touching wires.',
      ),
    },
    {
      q: t(
        '移動式{いどうしき}クレーンや高所{こうしょ}作業車{さぎょうしゃ}を使{つか}って作業{さぎょう}を行{おこな}う際{さい}、現場{げんば}の近{ちか}くに架空{かくう}電線{でんせん}（配電線{はいでんせん}）が通{とお}っています。感電{かんでん}を防止{ぼうし}する正{ただ}しい措置{そち}はどれですか？',
        'When working with mobile cranes or aerial platforms near overhead power lines, what is the correct measure to prevent electric shock?',
      ),
      options: [
        t(
          '電線{でんせん}の被覆{ひふく}は絶対{ぜったい}に破{やぶ}れないため、クレーンのブームが電線{でんせん}に直接{ちょくせつ}接触{せっしょく}しても問題{もんだい}ない',
          'Line insulation never breaks, so direct contact between crane boom and power lines poses no problem',
        ),
        t(
          '雨{あめ}の日{ひ}は電線{でんせん}の周{まわ}りの空気{くうき}が電気{でんき}を吸収{きゅうしゅう}してくれるため、晴{は}れの日{ひ}よりも電線{でんせん}に近{ちか}づいて作業{さぎょう}できる',
          'Rainy air absorbs electricity, allowing equipment to operate closer to power lines than on sunny days',
        ),
        t(
          '電線{でんせん}に十分{じゅうぶん}な安全{あんぜん}離隔{りかく}距離{きょり}を保{たも}ち、電力{でんりょく}会社{がいしゃ}に依頼{いらい}して絶縁{ぜつえん}防護{ぼうご}管{かん}を取{と}り付{つ}け、専任{せんにん}の監視員{かんしいん}を配置{はいち}する', // verify: クレーンや足場の電線離隔距離基準（低圧1m以上、高圧2m以上、特別高圧離隔基準）
          'Maintain safe clearance distances, have utility company install insulating pipe sleeves, and post a dedicated lookout',
        ),
        t(
          '電線{でんせん}に触{ふ}れそうになったら、手{て}で押{お}しのけてクレーンを通{とお}せば安全{あんぜん}である',
          'If crane gets close to touching wires, pushing them away by hand ensures safety',
        ),
      ],
      answer: 2,
      why: t(
        '高圧{こうあつ}架空{かくう}電線{でんせん}は直接{ちょくせつ}触{ふ}れなくても近{ちか}づくだけで空中{くうちゅう}放電{ほうでん}（アーク）が起{お}きて感電{かんでん}します。十分{じゅうぶん}な離隔{りかく}距離{きょり}の確保{かくほ}、防護{ぼうご}管{かん}の装着{そうちゃく}、および合図{あいず}を行{おこな}う監視員{かんしいん}の配置{はいち}が必須{ひっす}です。',
        'High-voltage lines can arc across air without direct physical contact. Maintaining legal clearance, installing insulating covers, and stationing a watchman are essential.',
      ),
    },
    {
      q: t(
        '作業場{さぎょうば}で同僚{どうりょう}が電動{でんどう}工具{こうぐ}を握{にぎ}ったまま「うわっ！」と叫{さけ}んで倒{たお}れ、感電{かんでん}して動{うご}けなくなっています。あなたが最初{さいしょ}に行{おこな}うべき行動{こうどう}として、正{ただ}しいものはどれですか？',
        'A co-worker gripping a power tool shouts and falls, immobilized by electric shock. What is the very first action you must take?',
      ),
      options: [
        t(
          '素手{すで}で同僚{どうりょう}の体{からだ}に触{さわ}らず、直{ただ}ちに元{もと}のスイッチやブレーカーを切{き}る（遮断{しゃだん}できない場合{ばあい}は乾{かわ}いた木{き}の棒{ぼう}など絶縁物{ぜつえんぶつ}で引{ひ}き離{はな}す）',
          'Never touch the victim with bare hands; instantly shut off breaker/switch, or separate using a dry wooden stick/insulator',
        ),
        t(
          'すぐに素手{すで}で同僚{どうりょう}の腕{うで}を強{つよ}く引{ひ}っ張{ぱ}って工具{こうぐ}から引き離{はな}す',
          'Immediately grab co-worker arms with bare hands and pull strongly away from tool',
        ),
        t(
          '感電{かんでん}している同僚{どうりょう}に水{みず}をかけて冷{ひ}やし、意識{いしき}を取{と}り戻{もど}させようとする',
          'Pour cold water over the shocked co-worker to cool down and revive consciousness',
        ),
        t(
          'その場{ば}を動{うご}かず、同僚{どうりょう}が自分{じぶん}の力{ちから}で立ち上{あ}がるまで大声{おおごえ}で名前{なまえ}を呼{よ}び続{つづ}ける',
          'Stay still and keep shouting co-worker name loudly until they stand up on their own',
        ),
      ],
      answer: 0,
      why: t(
        '通電{つうでん}中{ちゅう}の被災者{ひさいしゃ}に素手{すで}で触{ふ}れると、救助者{きゅうじょしゃ}も感電{かんでん}して共倒{ともだお}れになります。まず電源{でんげん}スイッチやブレーカーを遮断{しゃだん}し、直{ただ}ちに電気が流{なが}れない状態{じょうたい}を作{つく}ることが最優先{さいゆうせん}です。',
        'Touching an energized victim directly will shock and kill the rescuer too. Immediately cut power or use dry non-conductive objects to detach them safely.',
      ),
    },
    {
      q: t(
        '金属{きんぞく}製{せい}の外枠{そとわく}を持{も}つ電動{でんどう}工具{こうぐ}や機械{きかい}に「アース線{せん}（接地線{せっちせん}）」を取{と}り付{つ}ける理由{りゆう}として、正{ただ}しい説明{せつめい}はどれですか？',
        'Which correctly explains why an earth ground wire must be connected to power tools and machines with metal enclosures?',
      ),
      options: [
        t(
          '機械{きかい}の消費{しょうひ}電力{でんりょく}を半分{はんぶん}に減{へ}らして電気代{でんきだい}を節約{せつやく}するため',
          'To cut machinery power consumption in half and save electricity costs',
        ),
        t(
          '万一{まんいち}の絶縁{ぜつえん}劣化{れっか}で外枠{そとわく}に電気が漏{も}れた際{さい}、電気{でんき}を大地{だいち}へ逃{に}がして人間{にんげん}に流{なが}れる電流{でんりゅう}を減{へ}らし感電{かんでん}を防{ふせ}ぐため',
          'To direct leakage current safely into the earth if internal insulation fails, protecting humans from lethal electric shock',
        ),
        t(
          'アース線{せん}を繋{つな}ぐと機械{きかい}の回転{かいてん}スピードが2倍{ばい}になって能率{のうりつ}が上{あ}がるため',
          'Connecting earth wire doubles rotation speed of machine to improve work efficiency',
        ),
        t(
          '停電{ていでん}したときでもアース線{せん}から地面{じめん}の電気{でんき}を吸{す}い上{あ}げて機械{きかい}を動{うご}かし続{つづ}けるため',
          'To suck power out of the ground through earth wire during blackouts to keep tools running',
        ),
      ],
      answer: 1,
      why: t(
        'アース線{せん}は、機器{きき}内部{ないぶ}で漏電{ろうでん}した電流{でんりゅう}を抵抗{ていこう}の低{ひく}い大地{だいち}へ逃{に}がし、人体{じんたい}を通{とお}る危険{きけん}な電流{でんりゅう}を極小{きょくしょう}に抑{おさ}えるための命{いのち}を守{まも}る安全{あんぜん}回路{かいろ}です。',
        'The earth wire safely discharges leakage currents into the low-resistance ground, diverting dangerous currents away from human bodies.',
      ),
    },
    {
      q: t(
        '作業前{さぎょうまえ}点検{てんけん}で、電動{でんどう}工具{こうぐ}の電源{でんげん}コードの被覆{ひふく}が裂{さ}け、内部{ないぶ}の銅線{どうせん}が少{すこ}し見{み}えているのを発見{はっけん}しました。同僚{どうりょう}が「ビニールテープを巻{ま}けば使{つか}える」と言{い}いました。正{ただ}しい対応{たいおう}はどれですか？',
        'During pre-work inspection, you found a power tool cord with cracked insulation exposing copper strands. A co-worker says "wrap tape around it and use it." What is the correct action?',
      ),
      options: [
        t(
          '同僚{どうりょう}の言{い}う通{とお}り、上{うえ}から黒{くろ}いビニールテープを巻{ま}けば新品{しんぴん}と同{おな}じように安全{あんぜん}に使{つか}える',
          'As co-worker says, wrapping black vinyl tape over it makes it just as safe as brand new',
        ),
        t(
          '銅線{どうせん}を手{て}で触{さわ}ってみて、ピリピリしなければそのまま今日{きょう}一日{いちにち}使{つか}い続{つづ}ける',
          'Touch the copper wire by hand; if it does not tingle, keep using it for the rest of day',
        ),
        t(
          '破{やぶ}れた部分{ぶぶん}に水{みず}を少{すこ}しかけて冷{ひ}やしながら作業{さぎょう}する',
          'Sprinkle water over the torn section to keep it cool while continuing to work',
        ),
        t(
          'ビニールテープでの応急{おうきゅう}処置{しょち}は禁止{きんし}されているため、直{ただ}ちに使用{しよう}を中止{ちゅうし}して職長{しょくちょう}に報告{ほうこく}し、コード全体{ぜんたい}を交換{こうかん}・修理{しゅうり}する',
          'Emergency patching with tape is prohibited; immediately stop use, report to foreman, and replace the entire damaged cord',
        ),
      ],
      answer: 3,
      why: t(
        '被覆{ひふく}が破{やぶ}れたコードにテープを巻{ま}くだけの補修{ほしゅう}は、引{ひ}っ張{ぱ}りや水気{みずけ}ですぐに剥{は}がれて感電{かんでん}やショート火災{かさい}の原因{げんいん}となり大変{たいへん}危険{きけん}です。直{ただ}ちに使用{しよう}を中止{ちゅうし}して交換{こうかん}します。',
        'Taped cords easily peel under tension or moisture, posing severe shock and fire risks. Temporary taping is prohibited; replace or repair the cord immediately.',
      ),
    },
    {
      q: t(
        '労働安全衛生法{ろうどうあんぜんえいせいほう}に基づき、事業所{じぎょうしょ}で低圧{ていあつ}（交流{こうりゅう}600V以下{いか}）の電気{でんき}設備{せつび}の敷設{ふせつ}や充電部{じゅうでんぶ}の点検{てんけん}などの業務{ぎょうむ}に就{つ}く者{もの}に義務{ぎむ}付{づ}けられている教育{きょういく}はどれですか？', // verify: 低圧電気取扱業務には安衛則第36条第4号に基づく特別教育が必要
        'Under Industrial Safety and Health Law, what education is mandatory for workers engaging in low-voltage (AC 600V or less) electrical installation and live-part inspection?',
      ),
      options: [
        t(
          '法令{ほうれい}に基{づ}く「低圧{ていあつ}電気{でんき}取扱{とりあつかい}業務{ぎょうむ}特別{とくべつ}教育{きょういく}」を修了{しゅうりょう}すること',
          'Completing statutory "Special Safety Education for Low-Voltage Electrical Handling Operations"',
        ),
        t(
          '普通{ふつう}自動車{じどうしゃ}免許{めんきょ}を持{も}っていれば特別{とくべつ}な教育{きょういく}は一切{いっさい}必要{ひつよう}ない',
          'Holding a standard driver license eliminates the need for any special electrical education',
        ),
        t(
          'インターネットの動画{どうが}を5分間{ふんかん}見{み}るだけで自分{じぶん}の判断{はんだん}で作業{さぎょう}してよい',
          'Watching a 5-minute internet video allows one to perform electrical work at own discretion',
        ),
        t(
          '日本{にほん}語{ご}能力{のうりょく}試験{しけん}N1を持{も}っていれば電気{でんき}教育{きょういく}は免除{めんじょ}される',
          'Holding JLPT N1 proficiency exempts workers from statutory electrical safety training',
        ),
      ],
      answer: 0,
      why: t(
        '低圧{ていあつ}の電気{でんき}取扱{とりあつかい}業務{ぎょうむ}（充電部{じゅうでんぶ}の敷設{ふせつ}・修理{しゅうり}等{など}）には、労働安全衛生規則{ろうどうあんぜんえいせいきそく}第36条{じょう}第4号{ごう}に基{づ}く「特別{とくべつ}教育{きょういく}」の修了{しゅうりょう}が法令{ほうれい}で義務{ぎむ}付{づ}けられています。',
        'Article 36, Item 4 of Ordinance on Industrial Safety and Health mandates statutory Special Education before engaging in low-voltage electrical work.',
      ),
    },
    {
      q: t(
        '電源{でんげん}を切{き}って感電{かんでん}被災者{ひさいしゃ}を安全{あんぜん}な場所{ばしょ}へ救出{きゅうしゅつ}しました。呼{よ}びかけても返事{へんじ}がなく、呼吸{こきゅう}をしていない（または死戦期{しせんき}呼吸{こきゅう}）場合{ばあい}、救急車{きゅうきゅうしゃ}が到着{とうちゃく}するまでに行{おこな}うべき命{いのち}を救{すく}う処置{しょち}はどれですか？',
        'After cutting power and moving an electric shock victim to safety, the victim is unresponsive and not breathing normally. What life-saving action must you perform while waiting for ambulance?',
      ),
      options: [
        t(
          '体{からだ}を激{はげ}しく揺{ゆ}さぶり、冷{つめ}たい水{みず}を顔{かお}に浴{あ}びせ続{つづ}ける',
          'Shake the body violently and keep splashing cold water onto face',
        ),
        t(
          '直{ただ}ちに胸{むね}の真ん中{まんなか}を強{つよ}く速{はや}く押{お}す胸骨{きょうこつ}圧迫{あっぱく}（心肺蘇生{しんぱいそせい}）を開始{かいし}し、AEDが届{とど}いたら直{ただ}ちに装着{そうちゃく}して指示{しじ}に従{したが}う',
          'Immediately start hard and fast chest compressions (CPR) and apply AED as soon as it arrives, following prompts',
        ),
        t(
          '毛布{もうふ}をかけて静{しず}かに寝{ね}かせ、救急隊{きゅうきゅうたい}が来{く}るまで何{なに}もせずに待{ま}つ',
          'Cover with blanket, lay down quietly, and wait without doing anything until paramedics arrive',
        ),
        t(
          '背中{せなか}を強{つよ}く叩{たた}いて胃{い}の中{なか}のものを吐{は}き出{だ}させる',
          'Slap back hard to force vomiting of stomach contents',
        ),
      ],
      answer: 1,
      why: t(
        '感電{かんでん}による心停止{しんていし}（心室細動{しんしつさいどう}）では、数分{すうふん}以内{いない}の胸骨{きょうこつ}圧迫{あっぱく}とAEDによる除細動{じょさいどう}が生死{せいし}を分{わ}けます。救急隊{きゅうきゅうたい}の到着{とうちゃく}を待{ま}たず、その場{ば}ですぐに心肺蘇生{しんぱいそせい}を開始{かいし}します。',
        'Cardiac arrest from shock requires immediate chest compressions and AED defibrillation within minutes. Begin CPR on site without waiting for paramedics.',
      ),
    },
    {
      q: t(
        '感電{かんでん}災害{さいがい}において、なぜ数十{すうじゅう}ミリアンペア（mA）というごくわずかな電流{でんりゅう}でも人間{にんげん}が死亡{しぼう}することがあるのですか？医学的{いがくてき}・安全{あんぜん}上{じょう}の理由{りゆう}として正{ただ}しいものはどれですか？', // verify: 商用周波交流において数十mAから100mAの人体通過電流で心室細動が発生する危険性
        'In electric shock accidents, why can a tiny current of merely tens of milliamperes (mA) prove fatal to humans? Which is correct medically and scientifically?',
      ),
      options: [
        t(
          '電気{でんき}が体内{たいない}に入{はい}ると骨{ほね}が瞬時{しゅんじ}に溶{と}けて歩{ある}けなくなるから',
          'Current instantly dissolves bones inside body, preventing walking',
        ),
        t(
          '電気{でんき}が胃{い}の中{なか}の食{た}べ物{もの}を爆発{ばくはつ}させるから',
          'Current causes food inside stomach to explode violently',
        ),
        t(
          '商用{しょうよう}交流{こうりゅう}電流{でんりゅう}が心臓{しんぞう}を通過{つうか}すると、心室細動{しんしつさいどう}（心臓{しんぞう}が小刻{こざざ}みに震{ふる}えて血液{けつえき}を送{おく}れなくなる状態{じょうたい}）を引{ひ}き起{お}こすから',
          'When commercial AC current passes across the heart, it triggers ventricular fibrillation, preventing the heart from pumping blood',
        ),
        t(
          '人間{にんげん}の目{め}が光{ひかり}に耐{た}えられなくなって失神{しっしん}するから',
          'Human eyes cannot bear the electrical light, causing fatal fainting',
        ),
      ],
      answer: 2,
      why: t(
        '心臓{しんぞう}を挟{はさ}む経路{けいろ}（右手{みぎて}から左足{ひだりあし}など）に数十{すうじゅう}mAの交流{こうりゅう}が流{なが}れると、心筋{しんきん}のリズムが狂{くる}い「心室細動{しんしつさいどう}」となって血液{けつえき}循環{じゅんかん}が停止{ていし}するため、ごくわずかな電流{でんりゅう}でも致命傷{ちめいしょう}になります。',
        'When tens of milliamperes of AC cross the heart, it disrupts normal contractions into lethal ventricular fibrillation, halting blood circulation within seconds.',
      ),
    },
    {
      q: t(
        '造船所{ぞうせんじょ}の船底{ふなぞこ}や鉄骨{てっこつ}上{じょう}など、周囲{しゅうい}が金属{きんぞく}で囲{かこ}まれ水気{みずけ}の多{おお}い導電性{どうでんせい}の高{たか}い場所{ばしょ}で電動{でんどう}工具{こうぐ}を使{つか}う場合{ばあい}、必須{ひっす}となる感電{かんでん}防止{ぼうし}対策{たいさく}はどれですか？',
        'When using power tools in conductive environments surrounded by metal and water, such as shipyard bilges or steel structures, what mandatory measure is required?',
      ),
      options: [
        t(
          '機械{きかい}が濡{ぬ}れないよう新聞紙{しんぶんし}で包{つつ}んで作業{さぎょう}する',
          'Wrap tool in newspapers to keep it dry while working',
        ),
        t(
          '素足{すあし}になって足{あし}の裏{うら}の感覚{かんかく}を研{と}ぎ澄{す}ませて作業{さぎょう}する',
          'Work barefoot to sharpen sensations under feet',
        ),
        t(
          '感電{かんでん}しても痛{いた}くないように大声{おおごえ}で歌{うた}いながら作業{さぎょう}する',
          'Sing loudly while working so that electric shocks do not hurt',
        ),
        t(
          '高感度{こうかんど}高速形{こうそくがた}の漏電{ろうでん}遮断器{しゃだんき}（感度{かんど}15mA以下{いか}）を接続{せつぞく}し、二重{にじゅう}絶縁{ぜつえん}工具{こうぐ}の使用{しよう}と乾{かわ}いた絶縁{ぜつえん}保護具{ほごぐ}を着用{ちゃくよう}する', // verify: 導電性の高い場所での電動工具使用時は感電防止用漏電遮断器接続が義務付け（安衛則第333条）
          'Connect through high-sensitivity fast-acting earth-leakage breaker (15mA or less), use double-insulated tools, and wear dry insulating PPE',
        ),
      ],
      answer: 3,
      why: t(
        '導電性{どうでんせい}が高{たか}く水気{みずけ}のある場所{ばしょ}は感電{かんでん}の危険{きけん}が極{きわ}めて高{たか}いため、感度{かんど}15mA以下{いか}・動作時間{どうさじかん}0.1秒以内{びょういない}の漏電{ろうでん}遮断器{しゃだんき}の設置{せっち}が法令{ほうれい}（安衛則第333条）で義務{ぎむ}付{づ}けられています。',
        'In highly conductive locations, severe shock hazards mandate using fast-acting earth-leakage breakers rated 15mA or less under safety regulations.',
      ),
    },
  ],
  glossary: [
    {
      ja: '感電',
      reading: 'かんでん',
      en: 'electric shock',
      note: t(
        '人体{じんたい}に電流{でんりゅう}が流{なが}れて組織{そしき}が損傷{そんしょう}したり心停止{しんていし}を引起{ひきお}こす災害{さいがい}。',
        'An injury or death caused when electrical current passes through the human body, potentially stopping the heart.',
      ),
    },
    {
      ja: '漏電遮断器',
      reading: 'ろうでんしゃだんき',
      en: 'earth-leakage circuit breaker (ELCB / GFCI)',
      note: t(
        '配線{はいせん}や機器{きき}から大地{だいち}に電気が漏{も}れた際{さい}、瞬時{しゅんじ}に電気{でんき}を止{と}めて感電{かんでん}を防{ふせ}ぐ安全{あんぜん}装置{そうち}。',
        'A safety device that cuts off electricity instantly when leakage current to ground is detected.',
      ),
    },
    {
      ja: 'アース（接地）',
      reading: 'あーす（せっち）',
      en: 'electrical grounding / earthing',
      note: t(
        '漏電{ろうでん}した危険{きけん}な電流{でんりゅう}を地面{じめん}（大地{だいち}）に逃{に}がすため、電気{でんき}機器{きき}と大地{だいち}をつなぐ電線{でんせん}。',
        'A protective wire connecting equipment to the earth to safely discharge leakage current.',
      ),
    },
    {
      ja: '停電作業',
      reading: 'ていでんさぎょう',
      en: 'de-energized work / power-off work',
      note: t(
        '感電{かんでん}を防{ふせ}ぐため、回路{かいろ}の主開閉器{しゅかいへいき}を切{き}って電気が流{なが}れていない状態{じょうたい}で行{おこな}う作業{さぎょう}。',
        'Work performed on electrical circuits after completely turning off the power supply.',
      ),
    },
    {
      ja: 'ロックアウト・タグアウト（LOTO）',
      reading: 'ろっくあうと・たぐあうと',
      en: 'Lockout/Tagout (LOTO)',
      note: t(
        '作業中{さぎょうちゅう}に第三者{だいさんしゃ}が誤{あやま}って電源{でんげん}を入{い}れないよう、開閉器{かいへいき}に鍵{かぎ}をかけ警告{けいこく}札{ふだ}を取{と}り付{つ}ける安全{あんぜん}手順{てじゅん}。',
        'Safety procedure of locking switches with a padlock and tagging them to prevent accidental re-energization.',
      ),
    },
    {
      ja: '検電器',
      reading: 'けんでんき',
      en: 'voltage detector / tester',
      note: t(
        '配線{はいせん}や端子{たんし}に電気が来{き}ているか（電圧{でんあつ}の有無{うむ}）を光{ひかり}や音{おと}で安全{あんぜん}に確認{かくにん}する器具{きぐ}。',
        'A handheld tool used to safely test whether a circuit is energized using light or sound.',
      ),
    },
    {
      ja: '電工ドラム',
      reading: 'でんこうどらむ',
      en: 'extension cord reel',
      note: t(
        '長{なが}い延長{えんちょう}コードを巻{ま}き取{と}る器具{きぐ}。巻{ま}いたまま大電流{だいでんりゅう}を通{とお}すと熱{ねつ}がこもり火災{かさい}の原因{げんいん}になる。',
        'A reel holding a long extension cord. Heavy current while coiled causes overheating and fires.',
      ),
    },
    {
      ja: '被覆損傷',
      reading: 'ひふくそんしょう',
      en: 'cable insulation damage',
      note: t(
        '電線{でんせん}の外側{そとがわ}のゴムやビニールが破{やぶ}れ、内部{ないぶ}の銅線{どうせん}がむき出{だ}しになった危険{きけん}な状態{じょうたい}。',
        'A hazardous condition where the outer protective sheath of a wire is torn, exposing live copper conductors.',
      ),
    },
    {
      ja: '架空電線',
      reading: 'かくうでんせん',
      en: 'overhead power lines',
      note: t(
        '電柱{でんちゅう}や鉄塔{てっとう}の間{あいだ}に張{は}られた高所{こうしょ}の電線{でんせん}。近{ちか}づくだけで空中{くうちゅう}放電{ほうでん}による感電{かんでん}の危険{きけん}がある。',
        'Elevated electrical cables strung between poles; approaching too closely risks arc flash shock.',
      ),
    },
    {
      ja: '低圧電気取扱特別教育',
      reading: 'ていあつでんきとりあつかいとくべつきょういく',
      en: 'special education for low-voltage electricity',
      note: t(
        '労働安全衛生法{ろうどうあんぜんえいせいほう}に基づき、低圧{ていあつ}（交流{こうりゅう}600V以下{いか}）の電気{でんき}業務{ぎょうむ}に従事{じゅうじ}する者{もの}が受講{じゅこう}する必須{ひっす}の安全{あんぜん}教育{きょういく}。',
        'Mandatory safety training under Japanese law for workers handling low-voltage electrical equipment.',
      ),
    },
    {
      ja: '心肺蘇生法（CPR）',
      reading: 'しんぱいそせいほう（しーぴーあーる）',
      en: 'Cardiopulmonary Resuscitation (CPR)',
      note: t(
        '心停止{しんていし}した人{ひと}の胸{むね}の真ん中{まんなか}を強{つよ}く圧迫{あっぱく}し、脳{のう}と心臓{しんぞう}に血液{けつえき}を送{おく}り続{つづ}ける救命{きゅうめい}処置{しょち}。',
        'Emergency life-saving procedure of compressing the chest to circulate blood to the brain and heart.',
      ),
    },
    {
      ja: 'AED（自動体外式除細動器）',
      reading: 'えーいーでぃー（じどうたいがいしきじょさいどうき）',
      en: 'Automated External Defibrillator (AED)',
      note: t(
        '感電{かんでん}等{など}で痙攣{けいれん}（心室細動{しんしつさいどう}）した心臓{しんぞう}に電気{でんき}ショックを与{あた}え、正常{せいじょう}なリズムに戻{もど}す救命{きゅうめい}装置{そうち}。',
        'A portable medical device that delivers an electric shock to reset a fibrillating heart back to normal rhythm.',
      ),
    },
  ],
}
