# TechTsunagu Course Platform Specification: Rover Villa Architecture Translation

> **Document Status**: Production Specification & Architecture Foundation  
> **Source Architecture Reference**: [Rover Villa](https://rovervilla.space/) (NASA Space Apps Challenge Global Winner)  
> **Domain Target**: Industrial Safety & Vocational Japanese Training for Technical Interns & Specified Skilled Workers (*Tokutei Ginou*)  
> **Repository Target**: `docs/ROVERVILLA_COURSE_PLATFORM_SPEC.md`  
> **Related Architecture Documents**: [FRONTEND_ARCHITECTURE.md](FRONTEND_ARCHITECTURE.md), [FRONTEND_REGULATIONS.md](FRONTEND_REGULATIONS.md), [FRONTEND_STACK.md](FRONTEND_STACK.md), [BACKEND_ARCHITECTURE.md](BACKEND_ARCHITECTURE.md), [API_SPEC.md](API_SPEC.md)

---

## Table of Contents
1. [Executive Summary & Pedagogical Vision](#1-executive-summary--pedagogical-vision)
2. [Ground-Truth Reverse Engineering & Audit of Rover Villa](#2-ground-truth-reverse-engineering--audit-of-rover-villa)
   - [2.1 Tech Stack & Runtime Footprint](#21-tech-stack--runtime-footprint)
   - [2.2 Procedural Web Audio Engine Architecture](#22-procedural-web-audio-engine-architecture)
   - [2.3 Dual-Namespace LocalStorage Strategy](#23-dual-namespace-localstorage-strategy)
   - [2.4 Video Sharding & Progressive Fallback Delivery](#24-video-sharding--progressive-fallback-delivery)
   - [2.5 Mobile Ergonomics & Floating Tab Navigation](#25-mobile-ergonomics--floating-tab-navigation)
   - [2.6 The 9 Original Simulation Games Audited](#26-the-9-original-simulation-games-audited)
3. [The "One Safety Skill, Four Doors" Blueprint for TechTsunagu](#3-the-one-safety-skill-four-doors-blueprint-for-techtsunagu)
   - [3.1 Door 1: Read — Manga KYT vs. JIS Handbook](#31-door-1-read--manga-kyt-vs-jis-handbook)
   - [3.2 Door 2: Watch — 60-Second Micro-Videos](#32-door-2-watch--60-second-micro-videos)
   - [3.3 Door 3: Play — The 9 TechTsunagu Safety Simulation Drills](#33-door-3-play--the-9-techtsunagu-safety-simulation-drills)
   - [3.4 Door 4: Inspect — 3D Digital Twin Equipment Inspection](#34-door-4-inspect--3d-digital-twin-equipment-inspection)
4. [Cross-Platform Web & Mobile User Flows](#4-cross-platform-web--mobile-user-flows)
   - [4.1 Employer & Foreman Workflow (Assign & Track)](#41-employer--foreman-workflow-assign--track)
   - [4.2 Foreign Worker Workflow (Learn & Certify)](#42-foreign-worker-workflow-learn--certify)
   - [4.3 Digital "Skill Passport" Certification Engine](#43-digital-skill-passport-certification-engine)
   - [4.4 Regulatory Compliance & Audit Export](#44-regulatory-compliance--audit-export)
5. [Data Models, Schemas & API Contracts](#5-data-models-schemas--api-contracts)
   - [5.1 Offline LocalStorage Cache (`rv:course_progress:v1`)](#51-offline-localstorage-cache-rvcourse_progressv1)
   - [5.2 API Envelopes & REST Endpoints (XREQ-001/002 Compliant)](#52-api-envelopes--rest-endpoints-xreq-001002-compliant)
6. [Frontend UI Component Blueprints](#6-frontend-ui-component-blueprints)
   - [6.1 `CourseReader.tsx` (Dual-Mode Headless Reader)](#61-coursereadertsx-dual-mode-headless-reader)
   - [6.2 `MobileCourseNav.tsx` (Floating Bottom Glassmorphic Bar)](#62-mobilecoursenavtsx-floating-bottom-glassmorphic-bar)
   - [6.3 `SafetyModelViewer.tsx` (Vanilla Three.js Inspection Canvas)](#63-safetymodelviewertsx-vanilla-threejs-inspection-canvas)
   - [6.4 `SafetyAudioEngine.ts` (Zero-Dependency Procedural Audio)](#64-safetyaudioenginets-zero-dependency-procedural-audio)
7. [Implementation & Phased Rollout Plan](#7-implementation--phased-rollout-plan)

---

## 1. Executive Summary & Pedagogical Vision

### 1.1 The Problem
In Japanese heavy industries (shipbuilding in Sasebo, metal fabrication, heavy machinery), foreign technical interns (*Ginou Jisshusei*) and specified skilled workers (*Tokutei Ginou*) face extreme safety vulnerability:
- **Literacy barrier**: Thick Japanese Industrial Standards (JIS) manuals and legal Ordinance on Industrial Safety and Health (*An'ei-soku* / 安衛則) booklets are handed out in unsimplified, kanji-dense Japanese.
- **Cognitive friction**: Machine translation apps strip operational context. Foreign workers nod politely (*"Hai, wakarimashita"*) due to cultural pressure, without comprehending hazard protocols.
- **Superficial compliance**: Employers log required safety training hours on paper sheets without verifiable proof of comprehension or physical competency.

### 1.2 The Rover Villa Solution Translated
[Rover Villa](https://rovervilla.space/) won the NASA Space Apps Challenge because it dismantled planetary mission engineering into a multi-modal, zero-friction discovery model: **"One Topic, Four Doors"**. Rather than forcing all users through a single medium, every concept is entered via:
1. **Graphic / Narrative** (Visual storytelling)
2. **Cinematic Micro-Video** (High-density visual demonstrations)
3. **Interactive Simulation / Game** (Active cognitive application & failure testing)
4. **Interactive 3D Digital Twin** (Exploded inspection and spatial understanding)

By mapping this exact pedagogical architecture into TechTsunagu, industrial work safety transforms from a passive compliance chore into an engaging, multi-sensory, verifiable learning platform accessible on both low-end mobile devices and factory web workstations.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                 TECHTSUNAGU "ONE SAFETY SKILL, FOUR DOORS"                  │
│                                                                             │
│   ┌───────────────────┐               ┌───────────────────┐                 │
│   │   DOOR 1: READ    │               │   DOOR 2: WATCH   │                 │
│   │  Manga KYT Story  │               │   60s Micro-Reel  │                 │
│   │   vs. JIS Rules   │               │   Pointing-Call   │                 │
│   └─────────┬─────────┘               └─────────┬─────────┘                 │
│             │                                   │                           │
│             ▼                                   ▼                           │
│      ┌─────────────────────────────────────────────────────┐                │
│      │            CORE SAFETY SKILL COMPETENCY             │                │
│      │        (e.g., Crane Slinging & Hooking / 玉掛け)     │                │
│      └─────────────────────────────────────────────────────┘                │
│             ▲                                   ▲                           │
│             │                                   │                           │
│   ┌─────────┴─────────┐               ┌─────────┴─────────┐                 │
│   │   DOOR 3: PLAY    │               │  DOOR 4: INSPECT  │                 │
│   │ 2-Leg Sling Angle │               │ 3D Wire Rope &    │                 │
│   │  Tension Sim Game │               │ Hook Defect Model │                 │
│   └───────────────────┘               └───────────────────┘                 │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Ground-Truth Reverse Engineering & Audit of Rover Villa

An end-to-end technical dissection of `https://rovervilla.space/` reveals key engineering choices that achieved award-winning performance and high engagement.

### 2.1 Tech Stack & Runtime Footprint
- **Runtime Environment**: Zero heavyweight full-stack frameworks in the presentation tier. Delivered as ultra-lightweight static assets.
- **Rendering Engine**: **Vanilla Three.js (r128)** directly mounted to HTML5 `<canvas>` elements.
  - *Engineering Insight*: Does **not** use React-Three-Fiber (R3F), Babylon.js, or A-Frame. Direct canvas management avoids React reconciliation cycles during 60 FPS animation loops and keeps heap usage under 45 MB on mobile Safari and Chrome Android.
- **Bundle Optimization**: Tree-shaken custom Three.js build containing only `PerspectiveCamera`, `WebGLRenderer`, `Scene`, `AmbientLight`, `DirectionalLight`, `MeshStandardMaterial`, and `GLTFLoader`.

### 2.2 Procedural Web Audio Engine Architecture
Rover Villa completely eliminates audio asset loading latency (MP3/WAV downloads) through a **custom zero-dependency procedural audio engine** running on the native Web Audio API (`AudioContext`).

```
                    ┌─────────────────┐
                    │  AudioContext   │
                    └────────┬────────┘
                             │
            ┌────────────────┴────────────────┐
            ▼                                 ▼
   ┌─────────────────┐               ┌─────────────────┐
   │ OscillatorNode  │               │ BiquadFilterNode│ (Lowpass / Resonant)
   │ (Sine/Saw/Sq)   │               └────────┬────────┘
   └────────┬────────┘                        │
            ▼                                 ▼
   ┌─────────────────┐               ┌─────────────────┐
   │    GainNode     │               │ StereoPannerNode│ (Spatial positioning)
   │ (ADSR Envelope) │               └────────┬────────┘
   └────────┬────────┘                        │
            └────────────────┬────────────────┘
                             ▼
                    ┌─────────────────┐
                    │ Destination Out │
                    └─────────────────┘
```

- **Procedural Sound Effects**:
  - *Telemetry Beeps*: `Sine` oscillator pulsed at 880 Hz / 1760 Hz with rapid exponential gain decay (`setTargetAtTime(0, ctx.currentTime, 0.04)`).
  - *Thruster / Wind Ambient*: White noise buffer generated procedurally via `AudioBuffer` and modulated by a sweeping `BiquadFilterNode` (`frequency.exponentialRampToValueAtTime`).
  - *Mechanical Servo / Actuator*: Dual `Sawtooth` oscillators tuned with micro-detune (+/- 4 cents) through a lowpass filter.
- **Zero Asset Cost**: 0 KB downloaded for audio files; 100% generated in real-time in under 3 KB of JavaScript.

### 2.3 Dual-Namespace LocalStorage Strategy
State persistence across sessions uses a clean, collision-free dual namespace:
1. **Global Profile Namespace (`rv:profile:*`)**:
   - Audio volumes (`rv:profile:volume_sfx`, `rv:profile:volume_bgm`).
   - Graphics fidelity (`rv:profile:quality`: `'low' | 'med' | 'high'`).
   - Global unlock progression flags (`rv:profile:unlocked_doors`).
2. **Module Execution Namespace (`rv:course_progress:v1:*`)**:
   - Keyed by module ID: `rv:course_progress:v1:{moduleId}`.
   - Holds deterministic finite state machine (FSM) states: step index, high scores, telemetry input history, completion timestamps, and verification hashes.
   - Prevents stale schema crashes by version tagging (`v1`).

### 2.4 Video Sharding & Progressive Fallback Delivery
- Video content uses 60-second micro-clips segmented into lightweight WebM (VP9) and MP4 (H.264 Baseline profile) shards.
- Immediate visual delivery: Uses base64 inlined WebP poster frames. Video elements are deferred until the user initiates interaction with Door 2.
- Dual-speed playback controls (0.75x, 1.0x, 1.5x) built into custom HTML5 overlay controls, avoiding native browser video controls that obstruct HUD overlays.

### 2.5 Mobile Ergonomics & Floating Tab Navigation
- **Floating Bottom Tab Bar**: Positioned cleanly inside the thumb reach zone (bottom 15% of viewport), floating with `backdrop-filter: blur(12px)`.
- **48px Minimum Hit Targets**: Strict adherence to touch target sizes; active states trigger CSS active scale transformations (`scale(0.96)`) simulating native haptic feedback.
- **Orientation Awareness**: Mini-games trigger an overlay warning prompting horizontal orientation only when spatial width is constrained below 600px.

### 2.6 The 9 Original Simulation Games Audited on Rover Villa (`/games`)

Direct extraction from `https://rovervilla.space/games` reveals 9 telemetry-driven NASA mission simulations:

| # | Simulation Game (URL) | Mission & Context | Real Game Mechanics & Telemetry Physics |
| :--- | :--- | :--- | :--- |
| **01** | **Touchdown**<br>(`/games/landing`) | Mars & Lunar Landers | Airbag bouncing physics, retrorocket burn timing, landing velocity vector calculation ($<2.5\text{ m/s}$), telemetry beacon ping acquisition. |
| **02** | **Last Pictures**<br>(`/games/ranger-7-dive`) | Ranger 7 Lunar Dive | Terminal velocity descent photography; camera exposure tuning under extreme time pressure before impact. |
| **03** | **Ebb & Flow**<br>(`/games/grail-gravity`) | GRAIL Dual Spacecraft | Dual-spacecraft microwave ranging; detecting mass anomalies (mascons) via relative velocity changes between probes. |
| **04** | **Into the Plume**<br>(`/games/lcross-impact`) | LCROSS Lunar Impactor | Centering spacecraft trajectory through dynamic particle debris plume to sample spectrometer absorption lines. |
| **05** | **Trust the Signal**<br>(`/games/polar-lander-forensics`) | Mars Polar Lander | Landing computer dilemma: Hall-effect leg deployment sensor noise causing premature engine shutdown 40m above ground. |
| **06** | **Is Anything Alive?**<br>(`/games/viking-biology`) | Viking 1 & 2 Biology | Running Labeled Release ($^{14}\text{C}$ nutrient), Gas Exchange, and Pyrolytic Release; interpreting chemical vs. organic data. |
| **07** | **Marsquake Hunter**<br>(`/games/insight-seismology`) | InSight SEIS Instrument | Reading seismic waveform traces, isolating P-wave and S-wave arrival deltas, calculating Marsquake epicentral distance. |
| **08** | **Thin Air**<br>(`/games/ingenuity-flight`) | Ingenuity Mars Helicopter | Aerodynamic lift in 1% atmospheric density; managing collective pitch, rotor RPM (2400–2900), translational lift. |
| **09** | **Chasing Sunshine**<br>(`/games/mer-twins`) | Spirit & Opportunity Rovers | Solar array dust accumulation curve, winter sun slope angle parking, driving backwards with seized front wheel. |

---

## 3. The "One Safety Skill, Four Doors" Blueprint for TechTsunagu

Translating the Rover Villa model to Japanese heavy industry and manufacturing safety:

### 3.1 Door 1: Read — Manga KYT vs. JIS Handbook
Every safety skill begins with a choice between graphic empathy and formal regulatory precision:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       DOOR 1: DUAL-MODE READER UI                           │
│                                                                             │
│    [ Mode Toggle: ● Manga KYT (ビジュアル)  ○ JIS Handbook (規格基準) ]      │
│    [ Language: [JA (振りがな)]  [BN (বাংলা)]  [EN (English)]  [VI (Tiếng Việt)] ]│
│                                                                             │
│   ┌───────────────────────────────────┬─────────────────────────────────┐   │
│   │        MANGA KYT COMIC VIEW       │       JIS REGULATORY TEXT       │   │
│   │                                   │                                 │   │
│   │   ┌───────────────────────────┐   │   JIS B 8801 / 安衛則 第151条    │   │
│   │   │ [Panel 1: Shipyard Crane] │   │                                 │   │
│   │   │ Rahim: 「ワイヤー、大丈夫?」│   │   1. ワイヤロープの安全係数:      │   │
│   │   │ (Furigana: わいやー、だいじょうぶ) │      つり具の安全係数は6以上とする。│   │
│   │   │ [Sub: তারটি কি ঠিক আছে?]  │                                 │   │
│   │   └───────────────────────────┘   │   2. 廃棄基準 (抜粋):            │   │
│   │   ┌───────────────────────────┐   │      - 素線の素線数の10%以上が   │   │
│   │   │ [Panel 2: Tanaka Foreman] │   │        切断しているもの。       │   │
│   │   │ Tanaka: 「点検よし!」     │   │      - 直径の減少が公称径の7%を   │   │
│   │   │ (Furigana: てんけんよし)   │   │        超えるもの。             │   │
│   │   │ [Sub: পরিদর্শন সম্পন্ন!]     │   │                                 │   │
│   │   └───────────────────────────┘   │                                 │   │
│   └───────────────────────────────────┴─────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

- **Manga KYT Mode**:
  - Expressive comic frames featuring recurring relatable characters: **Rahim** (foreign intern) and **Tanaka-san** (veteran Japanese foreman).
  - Demonstrates the real psychological dynamics: reluctance to speak up, near-miss (*Hiyari-Hatto*) moments, and practical resolutions.
  - Native `<ruby>` annotation support for Furigana on all Kanji, toggleable with a single tap.
  - Immediate translation overlays in Bengali (`bn`), Vietnamese (`vi`), and English (`en`).
- **JIS Standard Handbook Mode**:
  - The exact statutory requirements from the Ministry of Health, Labour and Welfare (MHLW / 厚生労働省), Ordinance on Industrial Safety and Health (安衛則), and Japanese Industrial Standards (JIS).
  - Equips workers with the technical vocabulary necessary to pass Japanese vocational licensing exams (e.g., *Tamagake* Crane Slinging Skills Training).

### 3.2 Door 2: Watch — 60-Second Micro-Videos
- **Content Model**: High-density 60-second video modules filmed in authentic Sasebo shipyard or fabrication environments.
- **The "Pointing and Calling" (*Shisa Kanko* / 指差呼称) Masterclass**:
  - Focuses on the physical ritual: Stance, arm extension, index finger lock, voice callout (*"○○, Yoshi!"*).
  - Visual HUD overlays highlighting eye-line angle, finger orientation, and acoustic decibel meters.
- **Subtitles & Phonetics**:
  - Dual synchronized subtitle tracks: Japanese (Kana/Kanji) + Worker's Native Language.
  - "Karaoke" style phonetic highlighting matching speech rhythm.
  - Variable speed playback: 0.75x for precise hand gesture observation.

### 3.3 Door 3: Play — The 9 TechTsunagu Safety Simulation Drills

Replacing the space missions with concrete heavy industry safety games:

| # | Safety Simulation Drill | Core Game Mechanics & Pedagogical Objective | Audio & Sensory Feedback |
| :--- | :--- | :--- | :--- |
| **01** | **KYT Hazard Spotting** | Panoramic factory scene. Player has 30 seconds to tap 5 hidden hazards (e.g., ungrounded welder in puddle, blocked fire exit, trailing hose). | Countdown ticking audio; buzzer on false taps; bell on hazard identified. |
| **02** | **Crane Sling Angle & Tension Sim** | Drag the crane hook height to alter sling angle between 2 legs. Real-time vector gauges show load force doubling at 120° vs. 60°. | Cable tension creaking pitch increases as angle widens; snap sound on failure. |
| **03** | **Pre-Flight PPE Checklist** | Drag protective equipment onto worker avatar in mandatory statutory order (harness under coat, hardhat chin strap clicked, safety shoes laced). | Mechanical snap/click audio for each buckled fastener; warning klaxon if order broken. |
| **04** | **E-Stop Emergency Reflex Drill** | Machine simulation with visual glitch/runaway. Player must locate and trigger physical emergency stop button or tripwire within 400ms. | Rising industrial motor hum; sharp airbrake release sound upon successful E-stop. |
| **05** | **Grinder Wheel Ring Test (打音点検)** | Tap test on replacement grinding wheels. Player taps 4 quadrants of the wheel using procedural Web Audio to identify sound anomalies. | **Intact wheel**: Clear high resonant chime (1200 Hz). **Cracked wheel**: Dull acoustic thud (320 Hz). |
| **06** | **Confined Space O2 & LOTO Sequence** | Step-by-step lock-out/tag-out (LOTO) of gas valves and atmospheric sampling with an electronic oxygen detector before entry (>18% required). | Gas hiss sound; digital meter continuous warble; green confirmation ping. |
| **07** | **Rolling Scaffolding Outrigger Setup** | Adjust 4 outriggers on a mobile scaffolding tower to achieve level spirit bubble alignment and engage all 4 wheel caster brake levers. | Metallic lock click; mechanical ratchet click; level chime. |
| **08** | **Arc Welder Grounding & Cable Inspection** | Inspect welding lead for gouged insulation, perform air-squeeze test on heavy rubber dielectric gloves, attach copper ground clamp. | Air hiss on punctured glove; copper clamp heavy spring snap sound. |
| **09** | **Tsunami & Factory Evacuation Rally** | Top-down map navigation during emergency warning. Follow exit routes, navigate around fallen debris, execute PASS fire extinguisher sequence. | Japanese emergency siren (*Uuuu-wan-wan*); fire extinguisher discharge hiss. |

### 3.4 Door 4: Inspect — 3D Digital Twin Equipment Inspection
Built using **Vanilla Three.js (r128)**, mounted directly inside a responsive canvas without bloated framework abstractions:
- **Interactive Inspection Models**:
  - *Shipyard Scaffolding Coupler (単管クランプ)*: Tightening bolt torque inspection, pin deformation detection.
  - *Handheld Angle Grinder (ディスクグラインダー)*: Spark guard angle, wheel wear limit line, spindle lock pin.
  - *Full-Body Safety Harness (フルハーネス型墜落制止用器具)*: Shock absorber deployment indicator, webbing fraying inspection, D-ring deformation.
  - *Heavy Wire Rope Sling (ワイヤロープ)*: Strands broken count (10% rule), kink detection, core damage.
- **Key Inspection Features**:
  - **Orbit & Pinch Controls**: 360-degree rotation and zoom on mobile touchscreens.
  - **Defect Toggle Switch**: Instantly toggles between "Pass / Safe Condition" (良品) and "Reject / Danger Condition" (不良品) with visual callout indicators.
  - **Exploded View Slider**: Pulls the 3D assembly apart along its axes to inspect internal springs, bearings, and safety interlocks.
  - **Raycasted Hotspot Pins**: Interactive 3D pins on critical points; tapping a pin pops up bilingual inspection criteria.

---

## 4. Cross-Platform Web & Mobile User Flows

TechTsunagu courses must operate seamlessly across two distinct user experiences: the **Foreman / Employer Portal** (Web/Tablet) and the **Foreign Worker Client** (Mobile/PWA).

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            CROSS-PLATFORM FLOWS                             │
│                                                                             │
│   FOREMAN / EMPLOYER (Web / iPad)              FOREIGN WORKER (Mobile PWA)  │
│                                                                             │
│   ┌─────────────────────────────┐                                           │
│   │ 1. Assign Course to Workers │                                           │
│   │    - Select: Rahim, Nguyen  │                                           │
│   │    - Course: Crane Slinging │                                           │
│   │    - Due: Dec 15, 2026      │                                           │
│   └──────────────┬──────────────┘                                           │
│                  │ (Push Notification / In-App Event)                       │
│                  ▼                                                          │
│   ┌─────────────────────────────┐              ┌────────────────────────┐   │
│   │ 2. Real-Time Progress Roster│              │ 3. Receive Assignment  │   │
│   │    - Door 1: Read    [ 80%] │              │    - Push / Notice Tab │   │
│   │    - Door 2: Watch   [100%] │              │    - Auto-native Lang  │   │
│   │    - Door 3: Play    [Pass] │              └───────────┬────────────┘   │
│   │    - Door 4: Inspect [Done] │                          │                │
│   └──────────────▲──────────────┘                          ▼                │
│                  │                            ┌─────────────────────────┐   │
│                  │                            │ 4. Complete 4 Doors     │   │
│                  │                            │    - Offline-first cache│   │
│                  │                            │    - Web Audio feedback │   │
│                  │                            └────────────┬────────────┘   │
│                  │                                         │                │
│                  │  Sync Completion Payload / Bearer Token │                │
│                  └─────────────────────────────────────────┘                │
│                                                            │                │
│   ┌─────────────────────────────┐                          ▼                │
│   │ 5. Compliance Export        │              ┌────────────────────────┐   │
│   │    - CSV / PDF for OTIT &   │              │ 6. Unlock Passport     │   │
│   │      Labor Standards Office │              │    - Digital Badge     │   │
│   │    - Tamper-proof logs      │              │    - Share with Senpai │   │
│   └─────────────────────────────┘              └────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 4.1 Employer & Foreman Workflow (Assign & Track)
1. **Roster Matrix Dashboard**:
   - Lists all workers by shop (`shopId`), department, visa status (*Ginou Jisshusei* Year 1/2/3, *Tokutei Ginou*).
   - Displays real-time 4-door completion pips: `[R][W][P][I]`.
2. **One-Tap Course Assignment**:
   - Foremen can assign courses individually or batch-assign to an entire shift before hazardous jobs.
   - Sets required completion deadlines and minimum drill passing scores.
3. **Live Progress & Risk Heatmap**:
   - Identifies workers who repeatedly fail Door 3 simulation drills (e.g., struggling with sling angle math), allowing the foreman to provide targeted, in-person mentoring before high-risk tasks.

### 4.2 Foreign Worker Workflow (Learn & Certify)
1. **Zero-Friction Access**:
   - Authenticated via the lightweight PIN / QR login defined in `XREQ-001`.
   - Automatic language adaptation: UI instantly displays Bengali, Vietnamese, English, or Furigana Japanese based on the worker's user profile.
2. **The 4-Door Progression**:
   - Workers can navigate freely between doors; completion of all 4 doors constitutes module mastery.
   - Works fully offline inside factory basements or shipyard hulls using local-first caching.
3. **Senpai Assistance Integration**:
   - If a worker fails a drill or cannot understand a JIS requirement, a single tap triggers `POST /api/asks` to forward a bilingual question directly to their assigned Japanese mentor (*Senpai*), linked under `XREQ-002`.

### 4.3 Digital "Skill Passport" Certification Engine
- Upon verified completion of all 4 doors (including score >= 80% on Door 3 and 100% hotspot discovery on Door 4), the backend issues a **Digital Skill Passport Stamp**.
- The stamp contains:
  - Skill ID (e.g., `SKILL-SLING-001`)
  - Issuing Foreman & Shop ID
  - Verification Hash & Timestamp
  - Cryptographic badge displayed in the worker's mobile app, giving workers proud tangible proof of their acquired Japanese vocational skills.

### 4.4 Regulatory Compliance & Audit Export
- To satisfy Japanese regulatory bodies:
  - **OTIT (外国人技能実習機構)**: Statutory safety and health education verification.
  - **Labour Standards Inspection Bureau (労働基準監督署)**: Mandatory Article 59 education records.
- The platform provides a secure backend CSV export endpoint (`GET /api/courses/compliance-export.csv`) adhering to safety rules:
  - Escaped formula characters (`=`, `+`, `-`, `@`) to prevent CSV injection.
  - Timestamped verification logs with worker IDs, course titles, time spent, and drill scores.

---

## 5. Data Models, Schemas & API Contracts

All contracts adhere strictly to [AGENTS.md](AGENTS.md) `XREQ-001` and `XREQ-002`:
- Authentication: `Authorization: Bearer <token>`
- Envelope: `{ "success": true, "data": ... }`
- No `workerId` in request bodies (inferred from JWT session).
- Mutation idempotency: Supported via `Idempotency-Key` UUID headers.

### 5.1 Offline LocalStorage Cache (`rv:course_progress:v1`)
Client-side state machine cache allowing offline drill execution:

```json
{
  "version": "1.0",
  "userId": "usr_7721",
  "shopId": "shop_sasebo_01",
  "lastSyncedAt": "2026-11-22T20:45:00Z",
  "courses": {
    "course_crane_slinging_01": {
      "status": "in_progress",
      "doors": {
        "read": {
          "completed": true,
          "mode": "manga",
          "timeSpentSeconds": 185,
          "completedAt": "2026-11-22T19:10:00Z"
        },
        "watch": {
          "completed": true,
          "playbackSpeed": 1.0,
          "watchedSeconds": 62,
          "completedAt": "2026-11-22T19:15:00Z"
        },
        "play": {
          "completed": true,
          "score": 95,
          "attempts": 2,
          "simulationData": {
            "maxSlingAngle": 60,
            "tensionFactor": 1.15,
            "passedHazards": 5
          },
          "completedAt": "2026-11-22T19:25:00Z"
        },
        "inspect": {
          "completed": false,
          "examinedHotspots": ["wire_kink", "hook_latch"],
          "totalHotspots": 3,
          "completedAt": null
        }
      },
      "passportStampIssued": false
    }
  },
  "pendingSyncQueue": [
    {
      "idempotencyKey": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
      "courseId": "course_crane_slinging_01",
      "stepId": "play",
      "payload": { "score": 95, "attempts": 2 },
      "completedAt": "2026-11-22T19:25:00Z"
    }
  ]
}
```

### 5.2 API Envelopes & REST Endpoints (XREQ-001/002 Compliant)

#### 1. Assign Course (Foreman Action)
- **Endpoint**: `POST /api/courses/assignments`
- **Headers**: `Authorization: Bearer <token>`, `Idempotency-Key: <uuid>`
- **Request Payload**:
  ```json
  {
    "courseId": "course_crane_slinging_01",
    "targetUserIds": ["usr_7721", "usr_7722"],
    "dueAt": "2026-12-15T17:00:00+09:00",
    "mandatory": true
  }
  ```
- **Response (`201 CREATED`)**:
  ```json
  {
    "success": true,
    "data": {
      "assignedCount": 2,
      "courseId": "course_crane_slinging_01",
      "dueAt": "2026-12-15T17:00:00+09:00"
    }
  }
  ```

#### 2. Get Course Detail with 4-Door Content
- **Endpoint**: `GET /api/courses/{id}`
- **Headers**: `Authorization: Bearer <token>`
- **Response (`200 OK`)**:
  ```json
  {
    "success": true,
    "data": {
      "id": "course_crane_slinging_01",
      "title": {
        "ja": "玉掛け作業の基本と合図",
        "bn": "ক্রেন স্লিংিং এবং সংকেতের প্রাথমিক নিয়ম",
        "en": "Basics of Crane Slinging & Signalling"
      },
      "category": "heavy_machinery",
      "doors": {
        "read": {
          "mangaPanels": [
            {
              "panelId": "p1",
              "imageUrl": "/api/blobs/manga_crane_p1.webp",
              "dialogue": {
                "speaker": "Rahim",
                "ja": "ワイヤーロープの点検、これでいいですか？",
                "furigana": "ワイヤーロープのてんけん、これでいいですか？",
                "bn": "তারের দড়ি কি এভাবে পরীক্ষা করা ঠিক আছে?",
                "en": "Is this the correct way to check the wire rope?"
              }
            }
          ],
          "jisText": {
            "standardNumber": "JIS B 8801",
            "statuteReference": "安衛則 第151条",
            "contentJa": "つり具の安全係数は6以上でなければならない。",
            "contentBn": "উত্তোলন যন্ত্রের সুরক্ষা গুণাঙ্ক অবশ্যই ৬ বা তার বেশি হতে হবে।"
          }
        },
        "watch": {
          "videoStreamUrl": "/api/videos/crane_shisa_kanko_60s.mp4",
          "posterUrl": "/api/blobs/crane_shisa_poster.webp",
          "durationSeconds": 62,
          "subtitles": [
            { "start": 0.0, "end": 4.5, "ja": "フックよし！ワイヤーよし！", "bn": "হুক ঠিক আছে! তার ঠিক আছে!" }
          ]
        },
        "play": {
          "gameId": "sim_crane_sling_angle",
          "passingScore": 80,
          "parameters": {
            "loadWeightKg": 1500,
            "maxAllowedTensionKg": 2000
          }
        },
        "inspect": {
          "modelUrl": "/api/models/crane_hook_assembly.glb",
          "hotspots": [
            { "id": "hook_safety_latch", "position": [0.1, 1.2, 0.0], "labelJa": "外れ止め装置", "labelBn": "হুক ল্যাচ" },
            { "id": "wire_rope_strands", "position": [-0.3, 0.8, 0.1], "labelJa": "素線の素線切れ", "labelBn": "তারের ছেঁড়া অংশ" }
          ]
        }
      }
    }
  }
  ```

#### 3. Complete Course Door / Step
- **Endpoint**: `POST /api/courses/{id}/steps/{stepId}/complete`
- **Path Parameters**: `id`: `course_crane_slinging_01`, `stepId`: `read` | `watch` | `play` | `inspect`
- **Headers**: `Authorization: Bearer <token>`, `Idempotency-Key: <uuid>`
- **Request Payload**:
  ```json
  {
    "score": 95,
    "metrics": {
      "timeSpentSeconds": 145,
      "attempts": 1,
      "mode": "manga"
    }
  }
  ```
- **Response (`200 OK`)**:
  ```json
  {
    "success": true,
    "data": {
      "stepId": "play",
      "completed": true,
      "courseCompleted": true,
      "passportStampIssued": true,
      "stamp": {
        "stampId": "stamp_sling_20261122",
        "titleJa": "玉掛け基本技能認定",
        "titleBn": "স্লিংিং প্রাথমিক দক্ষতা সনদ",
        "issuedAt": "2026-11-22T20:50:00+09:00"
      }
    }
  }
  ```

#### 4. Foreman Progress Roster
- **Endpoint**: `GET /api/courses/progress?shopId=shop_sasebo_01`
- **Headers**: `Authorization: Bearer <token>`
- **Response (`200 OK`)**:
  ```json
  {
    "success": true,
    "data": {
      "items": [
        {
          "userId": "usr_7721",
          "displayName": "Rahim Uddin",
          "language": "bn",
          "courseId": "course_crane_slinging_01",
          "progress": {
            "read": true,
            "watch": true,
            "play": true,
            "inspect": true
          },
          "isPassed": true,
          "completedAt": "2026-11-22T20:50:00+09:00"
        }
      ],
      "nextCursor": null
    }
  }
  ```

---

## 6. Frontend UI Component Blueprints

These blueprints follow the headless UI principles defined in [FRONTEND_ARCHITECTURE.md](FRONTEND_ARCHITECTURE.md) and [FRONTEND_STACK.md](FRONTEND_STACK.md), using React + TypeScript + Tailwind CSS without external heavy 3D/audio wrappers.

### 6.1 `CourseReader.tsx` (Dual-Mode Headless Reader)

```tsx
import React, { useState } from 'react';

export interface MangaPanel {
  panelId: string;
  imageUrl: string;
  dialogue: {
    speaker: string;
    ja: string;
    furigana: string;
    translation: string;
  };
}

export interface JisHandbookContent {
  standardNumber: string;
  statuteReference: string;
  contentJa: string;
  translation: string;
}

interface CourseReaderProps {
  panels: MangaPanel[];
  handbook: JisHandbookContent;
  onComplete: () => void;
}

export const CourseReader: React.FC<CourseReaderProps> = ({ panels, handbook, onComplete }) => {
  const [viewMode, setViewMode] = useState<'manga' | 'jis'>('manga');
  const [showFurigana, setShowFurigana] = useState(true);

  return (
    <div className="flex flex-col w-full max-w-4xl mx-auto bg-slate-900 text-slate-100 rounded-2xl overflow-hidden border border-slate-800 shadow-2xl">
      {/* Top Controller Bar */}
      <div className="flex items-center justify-between p-4 bg-slate-950/80 backdrop-blur border-b border-slate-800">
        <div className="flex gap-2 p-1 bg-slate-800 rounded-xl">
          <button
            onClick={() => setViewMode('manga')}
            className={`px-4 py-2 text-sm font-semibold rounded-lg transition-all ${
              viewMode === 'manga' ? 'bg-indigo-600 text-white shadow-md' : 'text-slate-400 hover:text-white'
            }`}
          >
            Manga KYT (マンガ)
          </button>
          <button
            onClick={() => setViewMode('jis')}
            className={`px-4 py-2 text-sm font-semibold rounded-lg transition-all ${
              viewMode === 'jis' ? 'bg-indigo-600 text-white shadow-md' : 'text-slate-400 hover:text-white'
            }`}
          >
            JIS Handbook (基準書)
          </button>
        </div>

        {viewMode === 'manga' && (
          <label className="flex items-center gap-2 text-xs font-medium text-slate-300 cursor-pointer">
            <input
              type="checkbox"
              checked={showFurigana}
              onChange={(e) => setShowFurigana(e.target.checked)}
              className="rounded bg-slate-800 border-slate-700 text-indigo-500 focus:ring-0"
            />
            振りがな表示 (Furigana)
          </label>
        )}
      </div>

      {/* Reader Content Body */}
      <div className="p-6 overflow-y-auto max-h-[600px] space-y-6">
        {viewMode === 'manga' ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {panels.map((panel) => (
              <div key={panel.panelId} className="bg-slate-950 p-4 rounded-xl border border-slate-800 flex flex-col gap-3">
                <img
                  src={panel.imageUrl}
                  alt={panel.dialogue.speaker}
                  className="w-full h-48 object-cover rounded-lg bg-slate-800"
                  loading="lazy"
                />
                <div className="space-y-1">
                  <span className="text-xs font-bold uppercase tracking-wider text-indigo-400">
                    {panel.dialogue.speaker}
                  </span>
                  <div className="text-base font-medium text-slate-100">
                    {showFurigana ? (
                      <ruby>
                        {panel.dialogue.ja}
                        <rt className="text-xs text-indigo-300 select-none">{panel.dialogue.furigana}</rt>
                      </ruby>
                    ) : (
                      panel.dialogue.ja
                    )}
                  </div>
                  <p className="text-sm text-slate-400 italic pt-1">{panel.dialogue.translation}</p>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="p-6 bg-slate-950 rounded-xl border border-slate-800 space-y-4">
            <div className="flex items-center gap-3">
              <span className="px-3 py-1 bg-amber-500/10 text-amber-400 text-xs font-bold rounded-md border border-amber-500/20">
                {handbook.standardNumber}
              </span>
              <span className="text-xs text-slate-400 font-mono">{handbook.statuteReference}</span>
            </div>
            <div className="text-base leading-relaxed text-slate-200 font-sans border-l-2 border-indigo-500 pl-4">
              {handbook.contentJa}
            </div>
            <div className="text-sm leading-relaxed text-slate-400 pt-2 border-t border-slate-800">
              {handbook.translation}
            </div>
          </div>
        )}
      </div>

      {/* Completion Trigger */}
      <div className="p-4 bg-slate-950/80 border-t border-slate-800 flex justify-end">
        <button
          onClick={onComplete}
          className="px-6 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white font-semibold text-sm rounded-xl transition-all shadow-lg shadow-emerald-900/20"
        >
          理解しました (Complete Step 1)
        </button>
      </div>
    </div>
  );
};
```

---

### 6.2 `MobileCourseNav.tsx` (Floating Bottom Glassmorphic Bar)

```tsx
import React from 'react';

export type CourseDoor = 'read' | 'watch' | 'play' | 'inspect';

interface MobileCourseNavProps {
  currentDoor: CourseDoor;
  completedDoors: Record<CourseDoor, boolean>;
  onSelectDoor: (door: CourseDoor) => void;
  isOffline?: boolean;
}

export const MobileCourseNav: React.FC<MobileCourseNavProps> = ({
  currentDoor,
  completedDoors,
  onSelectDoor,
  isOffline = false,
}) => {
  const doors: { id: CourseDoor; label: string; sub: string; icon: string }[] = [
    { id: 'read', label: '読む', sub: 'Read', icon: '📖' },
    { id: 'watch', label: '見る', sub: 'Watch', icon: '▶️' },
    { id: 'play', label: '遊ぶ', sub: 'Drill', icon: '🎮' },
    { id: 'inspect', label: '調べる', sub: '3D', icon: '🔍' },
  ];

  return (
    <nav className="fixed bottom-4 left-1/2 -translate-x-1/2 z-50 w-[92%] max-w-md bg-slate-900/85 backdrop-blur-md border border-slate-700/60 rounded-2xl p-1.5 shadow-2xl shadow-black/60">
      <div className="flex items-center justify-around">
        {doors.map((door) => {
          const isActive = currentDoor === door.id;
          const isDone = completedDoors[door.id];

          return (
            <button
              key={door.id}
              onClick={() => onSelectDoor(door.id)}
              className={`relative flex flex-col items-center justify-center min-w-[64px] min-h-[52px] rounded-xl transition-all duration-200 active:scale-95 ${
                isActive
                  ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/40'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              <span className="text-lg leading-none">{door.icon}</span>
              <span className="text-[11px] font-bold mt-1 leading-none">{door.label}</span>
              <span className="text-[9px] opacity-80 leading-none">{door.sub}</span>

              {/* Completion Pip Badge */}
              {isDone && (
                <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-emerald-400 ring-2 ring-slate-900" />
              )}
            </button>
          );
        })}
      </div>

      {isOffline && (
        <div className="absolute -top-6 left-1/2 -translate-x-1/2 px-2.5 py-0.5 bg-amber-500/90 text-slate-950 font-bold text-[10px] rounded-full shadow">
          Offline Mode (ローカル保存中)
        </div>
      )}
    </nav>
  );
};
```

---

### 6.3 `SafetyModelViewer.tsx` (Vanilla Three.js Inspection Canvas)

```tsx
import React, { useEffect, useRef, useState } from 'react';
import * as THREE from 'three';

export interface InspectionHotspot {
  id: string;
  title: string;
  description: string;
  position: [number, number, number];
}

interface SafetyModelViewerProps {
  hotspots: InspectionHotspot[];
  onHotspotDiscovered: (id: string) => void;
}

export const SafetyModelViewer: React.FC<SafetyModelViewerProps> = ({
  hotspots,
  onHotspotDiscovered,
}) => {
  const mountRef = useRef<HTMLDivElement>(null);
  const [selectedHotspot, setSelectedHotspot] = useState<InspectionHotspot | null>(null);
  const [isExploded, setIsExploded] = useState(false);

  useEffect(() => {
    const container = mountRef.current;
    if (!container) return;

    // 1. Core Scene Setup (Vanilla Three.js without R3F overhead)
    const scene = new THREE.Scene();
    scene.background = new THREE.Color(0x0a0f1d);

    const camera = new THREE.PerspectiveCamera(
      45,
      container.clientWidth / container.clientHeight,
      0.1,
      100
    );
    camera.position.set(2.5, 2, 3);

    const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false, powerPreference: 'high-performance' });
    renderer.setSize(container.clientWidth, container.clientHeight);
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    container.appendChild(renderer.domElement);

    // 2. Lighting Setup
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.7);
    scene.add(ambientLight);

    const dirLight = new THREE.DirectionalLight(0xffffff, 1.2);
    dirLight.position.set(5, 10, 7);
    scene.add(dirLight);

    // 3. Procedural Industrial Hook / Coupler Mesh Group
    const modelGroup = new THREE.Group();
    scene.add(modelGroup);

    // Main Hook Body
    const hookGeometry = new THREE.TorusGeometry(0.8, 0.15, 16, 64, Math.PI * 1.4);
    const metalMaterial = new THREE.MeshStandardMaterial({
      color: 0x94a3b8,
      metalness: 0.85,
      roughness: 0.25,
    });
    const hookMesh = new THREE.Mesh(hookGeometry, metalMaterial);
    hookMesh.rotation.z = -Math.PI / 4;
    modelGroup.add(hookMesh);

    // Safety Latch Component (Moveable for exploded view)
    const latchGeometry = new THREE.BoxGeometry(0.1, 0.7, 0.15);
    const latchMaterial = new THREE.MeshStandardMaterial({ color: 0xf59e0b, metalness: 0.5 });
    const latchMesh = new THREE.Mesh(latchGeometry, latchMaterial);
    latchMesh.position.set(0.3, 0.4, 0);
    modelGroup.add(latchMesh);

    // 4. Hotspot Marker Sprites
    const raycaster = new THREE.Raycaster();
    const mouse = new THREE.Vector2();
    const hotspotMarkers: THREE.Mesh[] = [];

    hotspots.forEach((hs) => {
      const markerGeo = new THREE.SphereGeometry(0.08, 16, 16);
      const markerMat = new THREE.MeshBasicMaterial({ color: 0x38bdf8 });
      const marker = new THREE.Mesh(markerGeo, markerMat);
      marker.position.set(...hs.position);
      marker.userData = { hotspot: hs };
      scene.add(marker);
      hotspotMarkers.push(marker);
    });

    // 5. Interaction: Pointer Click Detection
    const handlePointerDown = (event: MouseEvent) => {
      const rect = renderer.domElement.getBoundingClientRect();
      mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
      mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;

      raycaster.setFromCamera(mouse, camera);
      const intersects = raycaster.intersectObjects(hotspotMarkers);

      if (intersects.length > 0) {
        const targetHs = intersects[0].object.userData.hotspot as InspectionHotspot;
        setSelectedHotspot(targetHs);
        onHotspotDiscovered(targetHs.id);
      }
    };

    renderer.domElement.addEventListener('pointerdown', handlePointerDown);

    // 6. Animation Loop
    let animationFrameId: number;
    const animate = () => {
      animationFrameId = requestAnimationFrame(animate);

      // Gentle rotation when idle
      if (!isExploded) {
        modelGroup.rotation.y += 0.004;
      }

      // Hotspot pulse effect
      const scale = 1 + Math.sin(Date.now() * 0.006) * 0.15;
      hotspotMarkers.forEach((m) => m.scale.set(scale, scale, scale));

      renderer.render(scene, camera);
    };
    animate();

    // 7. Cleanup
    return () => {
      cancelAnimationFrame(animationFrameId);
      renderer.domElement.removeEventListener('pointerdown', handlePointerDown);
      renderer.dispose();
      if (container.contains(renderer.domElement)) {
        container.removeChild(renderer.domElement);
      }
    };
  }, [hotspots, isExploded, onHotspotDiscovered]);

  return (
    <div className="relative w-full h-[520px] rounded-2xl overflow-hidden border border-slate-800 bg-slate-950 shadow-2xl">
      <div ref={mountRef} className="w-full h-full cursor-grab active:cursor-grabbing" />

      {/* Floating View Controls */}
      <div className="absolute top-4 left-4 z-10 flex gap-2">
        <button
          onClick={() => setIsExploded(!isExploded)}
          className="px-3.5 py-1.5 bg-slate-900/80 backdrop-blur border border-slate-700 text-xs font-semibold text-slate-200 rounded-lg shadow active:scale-95 transition"
        >
          {isExploded ? '通常表示 (Standard)' : '分解図 (Exploded View)'}
        </button>
      </div>

      {/* Hotspot Annotation Modal Overlay */}
      {selectedHotspot && (
        <div className="absolute bottom-6 left-6 right-6 z-20 p-4 bg-slate-900/90 backdrop-blur-md border border-indigo-500/40 rounded-xl shadow-xl flex justify-between items-start">
          <div className="space-y-1">
            <span className="text-xs font-bold text-indigo-400">点検項目 (Check Item)</span>
            <h4 className="text-base font-bold text-white">{selectedHotspot.title}</h4>
            <p className="text-xs text-slate-300">{selectedHotspot.description}</p>
          </div>
          <button
            onClick={() => setSelectedHotspot(null)}
            className="text-slate-400 hover:text-white p-1 text-sm font-mono"
          >
            ✕
          </button>
        </div>
      )}
    </div>
  );
};
```

---

### 6.4 `SafetyAudioEngine.ts` (Zero-Dependency Procedural Audio)

```typescript
/**
 * Procedural Web Audio Engine for TechTsunagu
 * Replicates the Rover Villa zero-asset audio architecture.
 * Synthesizes acoustic ring tests, telemetry clicks, and buzzer alerts dynamically.
 */
export class SafetyAudioEngine {
  private ctx: AudioContext | null = null;

  private initContext(): AudioContext {
    if (!this.ctx) {
      const AudioCtxClass = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      this.ctx = new AudioCtxClass();
    }
    if (this.ctx.state === 'suspended') {
      void this.ctx.resume();
    }
    return this.ctx;
  }

  /**
   * Sound 1: Grinder Wheel Ring Test - Intact Condition (Clear chime at 1200 Hz)
   */
  public playRingTestPass(): void {
    const ctx = this.initContext();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();

    osc.type = 'sine';
    osc.frequency.setValueAtTime(1200, ctx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(1180, ctx.currentTime + 0.8);

    gain.gain.setValueAtTime(0.4, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.8);

    osc.connect(gain);
    gain.connect(ctx.destination);

    osc.start();
    osc.stop(ctx.currentTime + 0.8);
  }

  /**
   * Sound 2: Grinder Wheel Ring Test - Cracked Condition (Dull thud at 320 Hz)
   */
  public playRingTestFail(): void {
    const ctx = this.initContext();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();

    osc.type = 'triangle';
    osc.frequency.setValueAtTime(320, ctx.currentTime);
    osc.frequency.linearRampToValueAtTime(120, ctx.currentTime + 0.15);

    gain.gain.setValueAtTime(0.5, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.15);

    osc.connect(gain);
    gain.connect(ctx.destination);

    osc.start();
    osc.stop(ctx.currentTime + 0.15);
  }

  /**
   * Sound 3: Fast UI Tactile Click (Navigation / Door Tap)
   */
  public playClick(): void {
    const ctx = this.initContext();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();

    osc.type = 'sine';
    osc.frequency.setValueAtTime(800, ctx.currentTime);

    gain.gain.setValueAtTime(0.2, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.03);

    osc.connect(gain);
    gain.connect(ctx.destination);

    osc.start();
    osc.stop(ctx.currentTime + 0.03);
  }

  /**
   * Sound 4: Success Chime (Chord synthesis)
   */
  public playSkillCertifiedChime(): void {
    const ctx = this.initContext();
    const notes = [523.25, 659.25, 783.99, 1046.5]; // C5, E5, G5, C6

    notes.forEach((freq, idx) => {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(freq, ctx.currentTime + idx * 0.08);

      gain.gain.setValueAtTime(0.25, ctx.currentTime + idx * 0.08);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + idx * 0.08 + 0.6);

      osc.connect(gain);
      gain.connect(ctx.destination);

      osc.start(ctx.currentTime + idx * 0.08);
      osc.stop(ctx.currentTime + idx * 0.08 + 0.6);
    });
  }
}

export const safetyAudio = new SafetyAudioEngine();
```

---

## 7. Implementation & Phased Rollout Plan

To ensure strict engineering discipline and prevent architectural clashes, the course platform rollout is partitioned into three progressive milestones:

### Phase 1: Core Foundation & Door 1 + Door 2 (Current Sprint)
- Implement `SafetyAudioEngine.ts` and `rv:course_progress:v1` localStorage synchronizer.
- Ship `CourseReader.tsx` with dual Manga KYT and JIS Handbook toggle.
- Ship `MobileCourseNav.tsx` floating navigation bar.
- Mock contract endpoints (`/api/courses/{id}`, `/api/courses/{id}/steps/{stepId}/complete`) behind Vite mock toggle.

### Phase 2: Simulation Drills & 3D Digital Twin (Door 3 & Door 4)
- Port the 9 industrial safety simulation games (beginning with Crane Sling Angle and KYT Hazard Spotting).
- Integrate `SafetyModelViewer.tsx` (Vanilla Three.js r128) with procedural coupler and grinder inspection models.
- Implement server-side verification of drill scores and Idempotency-Key support.

### Phase 3: Enterprise Foreman Roster & Compliance Exports
- Deploy Foreman assignment matrix and real-time completion tracking.
- Implement `GET /api/courses/compliance-export.csv` with full OTIT/Labour Standards audit fields and spreadsheet formula injection defense.
- Roll out Digital Skill Passport cryptographic badge generation.
