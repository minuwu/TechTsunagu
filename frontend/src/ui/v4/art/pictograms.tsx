import type { ReactNode } from 'react'

export const PICTOGRAMS = {
  'no-entry': (
    <>
      <circle cx="32" cy="32" r="29" fill="#FFFFFF" />
      <circle cx="32" cy="17" r="4.5" fill="#141414" />
      <path d="M 26 23 L 38 23 L 37 37 L 27 37 Z" fill="#141414" />
      <path d="M 25 24 L 21 34 L 24 35 L 27 25 Z" fill="#141414" />
      <path d="M 39 24 L 43 34 L 40 35 L 37 25 Z" fill="#141414" />
      <path d="M 27 37 L 23 49 L 27 49 L 30 37 Z" fill="#141414" />
      <path d="M 34 37 L 37 49 L 41 49 L 37 37 Z" fill="#141414" />
      <line
        x1="13.5"
        y1="13.5"
        x2="50.5"
        y2="50.5"
        stroke="#C8102E"
        strokeWidth="6"
      />
      <circle
        cx="32"
        cy="32"
        r="26"
        fill="none"
        stroke="#C8102E"
        strokeWidth="6"
      />
    </>
  ),
  'no-open-flame': (
    <>
      <circle cx="32" cy="32" r="29" fill="#FFFFFF" />
      <polygon
        points="32,15 37,23 35,26 41,34 38,43 26,43 22,35 27,29 25,23"
        fill="#141414"
      />
      <polygon points="30,42 33,45 22,51 19,48" fill="#141414" />
      <line
        x1="13.5"
        y1="13.5"
        x2="50.5"
        y2="50.5"
        stroke="#C8102E"
        strokeWidth="6"
      />
      <circle
        cx="32"
        cy="32"
        r="26"
        fill="none"
        stroke="#C8102E"
        strokeWidth="6"
      />
    </>
  ),
  'no-smoking': (
    <>
      <circle cx="32" cy="32" r="29" fill="#FFFFFF" />
      <rect x="15" y="29" width="6" height="6" fill="#141414" />
      <rect x="23" y="29" width="18" height="6" fill="#141414" />
      <rect x="43" y="29" width="3" height="6" fill="#141414" />
      <path
        d="M 45 26 L 45 22 L 42 19 L 42 15 L 45 12"
        fill="none"
        stroke="#141414"
        strokeWidth="2.5"
      />
      <line
        x1="13.5"
        y1="13.5"
        x2="50.5"
        y2="50.5"
        stroke="#C8102E"
        strokeWidth="6"
      />
      <circle
        cx="32"
        cy="32"
        r="26"
        fill="none"
        stroke="#C8102E"
        strokeWidth="6"
      />
    </>
  ),
  'no-gloves': (
    <>
      <circle cx="32" cy="32" r="29" fill="#FFFFFF" />
      <rect x="25" y="33" width="14" height="12" fill="#141414" />
      <rect x="23" y="45" width="18" height="5" fill="#141414" />
      <polygon points="25,35 18,31 16,34 25,41" fill="#141414" />
      <rect x="25" y="21" width="3" height="13" fill="#141414" />
      <rect x="29" y="19" width="3" height="15" fill="#141414" />
      <rect x="33" y="21" width="3" height="13" fill="#141414" />
      <rect x="37" y="24" width="3" height="10" fill="#141414" />
      <line
        x1="13.5"
        y1="13.5"
        x2="50.5"
        y2="50.5"
        stroke="#C8102E"
        strokeWidth="6"
      />
      <circle
        cx="32"
        cy="32"
        r="26"
        fill="none"
        stroke="#C8102E"
        strokeWidth="6"
      />
    </>
  ),
  'no-under-load': (
    <>
      <circle cx="32" cy="32" r="29" fill="#FFFFFF" />
      <line x1="32" y1="13" x2="32" y2="20" stroke="#141414" strokeWidth="3" />
      <polygon points="21,22 43,22 45,30 19,30" fill="#141414" />
      <circle cx="32" cy="37" r="3.5" fill="#141414" />
      <path d="M 27 42 L 37 42 L 35 50 L 29 50 Z" fill="#141414" />
      <line x1="30" y1="50" x2="30" y2="56" stroke="#141414" strokeWidth="3" />
      <line x1="34" y1="50" x2="34" y2="56" stroke="#141414" strokeWidth="3" />
      <line
        x1="13.5"
        y1="13.5"
        x2="50.5"
        y2="50.5"
        stroke="#C8102E"
        strokeWidth="6"
      />
      <circle
        cx="32"
        cy="32"
        r="26"
        fill="none"
        stroke="#C8102E"
        strokeWidth="6"
      />
    </>
  ),
  'no-touch': (
    <>
      <circle cx="32" cy="32" r="29" fill="#FFFFFF" />
      <rect x="26" y="26" width="13" height="14" fill="#141414" />
      <rect x="28" y="40" width="9" height="10" fill="#141414" />
      <polygon points="26,34 19,29 18,33 26,38" fill="#141414" />
      <rect x="26" y="16" width="2.5" height="11" fill="#141414" />
      <rect x="29.5" y="14" width="2.5" height="13" fill="#141414" />
      <rect x="33" y="16" width="2.5" height="11" fill="#141414" />
      <rect x="36.5" y="19" width="2.5" height="8" fill="#141414" />
      <line
        x1="13.5"
        y1="13.5"
        x2="50.5"
        y2="50.5"
        stroke="#C8102E"
        strokeWidth="6"
      />
      <circle
        cx="32"
        cy="32"
        r="26"
        fill="none"
        stroke="#C8102E"
        strokeWidth="6"
      />
    </>
  ),
  'warn-general': (
    <>
      <polygon
        points="32,5 60,57 4,57"
        fill="#F9A800"
        stroke="#141414"
        strokeWidth="4"
        strokeLinejoin="miter"
      />
      <polygon points="29.5,21 34.5,21 33.5,39 30.5,39" fill="#141414" />
      <circle cx="32" cy="46" r="3" fill="#141414" />
    </>
  ),
  'warn-electric': (
    <>
      <polygon
        points="32,5 60,57 4,57"
        fill="#F9A800"
        stroke="#141414"
        strokeWidth="4"
        strokeLinejoin="miter"
      />
      <polygon
        points="34,18 24,32 31,32 26,49 41,31 33,31 38,18"
        fill="#141414"
      />
    </>
  ),
  'warn-hot': (
    <>
      <polygon
        points="32,5 60,57 4,57"
        fill="#F9A800"
        stroke="#141414"
        strokeWidth="4"
        strokeLinejoin="miter"
      />
      <rect x="20" y="46" width="24" height="4" fill="#141414" />
      <path
        d="M 25 43 C 23 39 27 35 25 31 C 23 27 27 23 25 21"
        fill="none"
        stroke="#141414"
        strokeWidth="2.5"
      />
      <path
        d="M 32 43 C 30 38 34 33 32 28 C 30 23 34 18 32 15"
        fill="none"
        stroke="#141414"
        strokeWidth="2.5"
      />
      <path
        d="M 39 43 C 37 39 41 35 39 31 C 37 27 41 23 39 21"
        fill="none"
        stroke="#141414"
        strokeWidth="2.5"
      />
    </>
  ),
  'warn-fall': (
    <>
      <polygon
        points="32,5 60,57 4,57"
        fill="#F9A800"
        stroke="#141414"
        strokeWidth="4"
        strokeLinejoin="miter"
      />
      <polygon points="14,40 25,40 25,52 14,52" fill="#141414" />
      <circle cx="41" cy="27" r="3.5" fill="#141414" />
      <polygon points="36,29 29,39 25,36 32,26" fill="#141414" />
      <line x1="27" y1="38" x2="20" y2="36" stroke="#141414" strokeWidth="3" />
      <line x1="28" y1="39" x2="29" y2="47" stroke="#141414" strokeWidth="3" />
      <line
        x1="34"
        y1="28"
        x2="42"
        y2="21"
        stroke="#141414"
        strokeWidth="2.5"
      />
    </>
  ),
  'warn-overhead-load': (
    <>
      <polygon
        points="32,5 60,57 4,57"
        fill="#F9A800"
        stroke="#141414"
        strokeWidth="4"
        strokeLinejoin="miter"
      />
      <line x1="32" y1="17" x2="32" y2="24" stroke="#141414" strokeWidth="3" />
      <line
        x1="32"
        y1="24"
        x2="20"
        y2="33"
        stroke="#141414"
        strokeWidth="2.5"
      />
      <line
        x1="32"
        y1="24"
        x2="44"
        y2="33"
        stroke="#141414"
        strokeWidth="2.5"
      />
      <rect x="20" y="33" width="24" height="13" fill="#141414" />
      <circle cx="32" cy="50" r="2.5" fill="#141414" />
      <path
        d="M 27 54 C 29 52 35 52 37 54"
        stroke="#141414"
        strokeWidth="2"
        fill="none"
      />
    </>
  ),
  'warn-forklift': (
    <>
      <polygon
        points="32,5 60,57 4,57"
        fill="#F9A800"
        stroke="#141414"
        strokeWidth="4"
        strokeLinejoin="miter"
      />
      <polygon points="20,44 34,44 34,34 27,34 22,38 20,38" fill="#141414" />
      <path
        d="M 21 38 L 22 27 L 31 27 L 32 34"
        fill="none"
        stroke="#141414"
        strokeWidth="2.5"
      />
      <circle cx="27" cy="31" r="2.5" fill="#141414" />
      <rect x="34" y="24" width="3" height="20" fill="#141414" />
      <polygon points="34,44 48,44 48,42 37,42" fill="#141414" />
      <rect x="38" y="32" width="8" height="10" fill="#141414" />
      <circle cx="23" cy="46" r="3.5" fill="#141414" />
      <circle cx="33" cy="46" r="3.5" fill="#141414" />
    </>
  ),
  'warn-slip': (
    <>
      <polygon
        points="32,5 60,57 4,57"
        fill="#F9A800"
        stroke="#141414"
        strokeWidth="4"
        strokeLinejoin="miter"
      />
      <circle cx="38" cy="22" r="3.5" fill="#141414" />
      <path d="M 33 25 L 36 23 L 26 38 L 23 36 Z" fill="#141414" />
      <line
        x1="33"
        y1="26"
        x2="43"
        y2="18"
        stroke="#141414"
        strokeWidth="2.5"
      />
      <line
        x1="29"
        y1="31"
        x2="39"
        y2="28"
        stroke="#141414"
        strokeWidth="2.5"
      />
      <line
        x1="25"
        y1="37"
        x2="37"
        y2="40"
        stroke="#141414"
        strokeWidth="3.5"
      />
      <line
        x1="25"
        y1="37"
        x2="19"
        y2="48"
        stroke="#141414"
        strokeWidth="3.5"
      />
      <line
        x1="16"
        y1="52"
        x2="48"
        y2="52"
        stroke="#141414"
        strokeWidth="2.5"
      />
      <path
        d="M 22 49 C 26 47 30 51 34 49"
        fill="none"
        stroke="#141414"
        strokeWidth="2"
      />
    </>
  ),
  'warn-crush-hand': (
    <>
      <polygon
        points="32,5 60,57 4,57"
        fill="#F9A800"
        stroke="#141414"
        strokeWidth="4"
        strokeLinejoin="miter"
      />
      <circle
        cx="23"
        cy="36"
        r="7"
        fill="none"
        stroke="#141414"
        strokeWidth="3"
      />
      <circle
        cx="41"
        cy="36"
        r="7"
        fill="none"
        stroke="#141414"
        strokeWidth="3"
      />
      <polygon points="30,18 34,18 34,28 30,28" fill="#141414" />
      <polygon points="29,28 35,28 33,40 31,40" fill="#141414" />
      <polygon points="30,28 26,26 27,29 30,31" fill="#141414" />
    </>
  ),
  'warn-flammable': (
    <>
      <polygon
        points="32,5 60,57 4,57"
        fill="#F9A800"
        stroke="#141414"
        strokeWidth="4"
        strokeLinejoin="miter"
      />
      <path
        d="M 32 18 C 36 24 42 30 42 37 C 42 44 37 46 32 46 C 27 46 22 44 22 37 C 22 32 25 28 28 28 C 29 32 32 34 33 34 C 33 30 31 24 32 18 Z"
        fill="#141414"
      />
      <rect x="20" y="48" width="24" height="3" fill="#141414" />
    </>
  ),
  'warn-trip': (
    <>
      <polygon
        points="32,5 60,57 4,57"
        fill="#F9A800"
        stroke="#141414"
        strokeWidth="4"
        strokeLinejoin="miter"
      />
      <polygon points="14,50 32,50 32,46 50,46 50,53 14,53" fill="#141414" />
      <circle cx="28" cy="22" r="3.5" fill="#141414" />
      <polygon points="26,26 29,24 37,36 34,38" fill="#141414" />
      <line
        x1="33"
        y1="31"
        x2="43"
        y2="28"
        stroke="#141414"
        strokeWidth="2.5"
      />
      <line
        x1="30"
        y1="34"
        x2="40"
        y2="36"
        stroke="#141414"
        strokeWidth="2.5"
      />
      <line x1="34" y1="38" x2="31" y2="46" stroke="#141414" strokeWidth="3" />
      <line x1="34" y1="38" x2="22" y2="38" stroke="#141414" strokeWidth="3" />
    </>
  ),
  'must-helmet': (
    <>
      <circle cx="32" cy="32" r="29" fill="#005387" />
      <path
        d="M 19 26 C 19 17 25 14 34 14 C 43 14 47 18 47 26 Z"
        fill="#FFFFFF"
      />
      <polygon points="17,26 51,26 47,23 18,23" fill="#FFFFFF" />
      <path
        d="M 23 28 C 23 28 23 40 30 42 C 34 43 38 43 41 38 C 42 36 43 33 43 28 Z"
        fill="#FFFFFF"
      />
      <polygon points="26,41 34,41 35,49 27,49" fill="#FFFFFF" />
      <line x1="18" y1="27" x2="48" y2="27" stroke="#005387" strokeWidth="2" />
      <path
        d="M 25 28 L 32 44 L 37 41"
        fill="none"
        stroke="#FFFFFF"
        strokeWidth="2"
      />
    </>
  ),
  'must-goggles': (
    <>
      <circle cx="32" cy="32" r="29" fill="#005387" />
      <rect
        x="14"
        y="24"
        width="14"
        height="14"
        fill="none"
        stroke="#FFFFFF"
        strokeWidth="3.5"
      />
      <rect
        x="36"
        y="24"
        width="14"
        height="14"
        fill="none"
        stroke="#FFFFFF"
        strokeWidth="3.5"
      />
      <line
        x1="28"
        y1="30"
        x2="36"
        y2="30"
        stroke="#FFFFFF"
        strokeWidth="3.5"
      />
      <path
        d="M 14 30 C 8 32 8 40 18 46 L 46 46 C 56 40 56 32 50 30"
        fill="none"
        stroke="#FFFFFF"
        strokeWidth="2.5"
      />
      <line x1="17" y1="27" x2="23" y2="35" stroke="#FFFFFF" strokeWidth="2" />
      <line x1="39" y1="27" x2="45" y2="35" stroke="#FFFFFF" strokeWidth="2" />
    </>
  ),
  'must-gloves': (
    <>
      <circle cx="32" cy="32" r="29" fill="#005387" />
      <rect x="18" y="29" width="11" height="12" fill="#FFFFFF" />
      <rect x="17" y="41" width="13" height="6" fill="#FFFFFF" />
      <polygon points="18,34 13,31 12,34 18,38" fill="#FFFFFF" />
      <rect x="18" y="19" width="2.2" height="10" fill="#FFFFFF" />
      <rect x="21" y="17" width="2.2" height="12" fill="#FFFFFF" />
      <rect x="24" y="19" width="2.2" height="10" fill="#FFFFFF" />
      <rect x="27" y="22" width="2.2" height="7" fill="#FFFFFF" />
      <rect x="35" y="29" width="11" height="12" fill="#FFFFFF" />
      <rect x="34" y="41" width="13" height="6" fill="#FFFFFF" />
      <polygon points="46,34 51,31 52,34 46,38" fill="#FFFFFF" />
      <rect x="35" y="22" width="2.2" height="7" fill="#FFFFFF" />
      <rect x="38" y="19" width="2.2" height="10" fill="#FFFFFF" />
      <rect x="41" y="17" width="2.2" height="12" fill="#FFFFFF" />
      <rect x="44" y="19" width="2.2" height="10" fill="#FFFFFF" />
    </>
  ),
  'must-safety-shoes': (
    <>
      <circle cx="32" cy="32" r="29" fill="#005387" />
      <rect x="17" y="18" width="14" height="3" fill="#FFFFFF" />
      <polygon points="18,20 30,20 30,34 18,34" fill="#FFFFFF" />
      <polygon points="14,46 18,34 30,34 38,38 48,40 50,46" fill="#FFFFFF" />
      <path
        d="M 14 46 L 50 46 L 50 49 L 34 49 L 34 47 L 22 47 L 22 49 L 14 49 Z"
        fill="#FFFFFF"
      />
      <path
        d="M 44 40 C 47 40 49 42 49 46"
        stroke="#005387"
        strokeWidth="2"
        fill="none"
      />
      <line x1="28" y1="24" x2="31" y2="24" stroke="#005387" strokeWidth="2" />
      <line x1="28" y1="28" x2="32" y2="28" stroke="#005387" strokeWidth="2" />
      <line x1="29" y1="32" x2="34" y2="32" stroke="#005387" strokeWidth="2" />
    </>
  ),
  'must-mask': (
    <>
      <circle cx="32" cy="32" r="29" fill="#005387" />
      <polygon points="32,18 44,28 39,43 25,43 20,28" fill="#FFFFFF" />
      <circle cx="32" cy="33" r="4.5" fill="#005387" />
      <line
        x1="32"
        y1="30"
        x2="32"
        y2="36"
        stroke="#FFFFFF"
        strokeWidth="1.5"
      />
      <line
        x1="29"
        y1="33"
        x2="35"
        y2="33"
        stroke="#FFFFFF"
        strokeWidth="1.5"
      />
      <line
        x1="26"
        y1="22"
        x2="12"
        y2="18"
        stroke="#FFFFFF"
        strokeWidth="2.5"
      />
      <line
        x1="38"
        y1="22"
        x2="52"
        y2="18"
        stroke="#FFFFFF"
        strokeWidth="2.5"
      />
      <line
        x1="22"
        y1="38"
        x2="12"
        y2="44"
        stroke="#FFFFFF"
        strokeWidth="2.5"
      />
      <line
        x1="42"
        y1="38"
        x2="52"
        y2="44"
        stroke="#FFFFFF"
        strokeWidth="2.5"
      />
    </>
  ),
  'must-earplugs': (
    <>
      <circle cx="32" cy="32" r="29" fill="#005387" />
      <circle cx="32" cy="34" r="11" fill="#FFFFFF" />
      <rect x="28" y="43" width="8" height="8" fill="#FFFFFF" />
      <path
        d="M 19 33 C 19 19 45 19 45 33"
        fill="none"
        stroke="#FFFFFF"
        strokeWidth="3.5"
      />
      <rect x="16" y="28" width="6" height="12" fill="#FFFFFF" />
      <rect x="42" y="28" width="6" height="12" fill="#FFFFFF" />
      <line
        x1="22"
        y1="28"
        x2="22"
        y2="40"
        stroke="#005387"
        strokeWidth="1.5"
      />
      <line
        x1="42"
        y1="28"
        x2="42"
        y2="40"
        stroke="#005387"
        strokeWidth="1.5"
      />
    </>
  ),
  'must-harness': (
    <>
      <circle cx="32" cy="32" r="29" fill="#005387" />
      <circle cx="32" cy="16" r="3.5" fill="#FFFFFF" />
      <polygon
        points="25,22 39,22 37,38 27,38"
        fill="none"
        stroke="#FFFFFF"
        strokeWidth="2"
      />
      <line x1="27" y1="22" x2="29" y2="38" stroke="#FFFFFF" strokeWidth="3" />
      <line x1="37" y1="22" x2="35" y2="38" stroke="#FFFFFF" strokeWidth="3" />
      <line x1="28" y1="28" x2="36" y2="28" stroke="#FFFFFF" strokeWidth="3" />
      <line x1="26" y1="38" x2="38" y2="38" stroke="#FFFFFF" strokeWidth="3" />
      <ellipse
        cx="28"
        cy="44"
        rx="4"
        ry="5"
        fill="none"
        stroke="#FFFFFF"
        strokeWidth="3"
      />
      <ellipse
        cx="36"
        cy="44"
        rx="4"
        ry="5"
        fill="none"
        stroke="#FFFFFF"
        strokeWidth="3"
      />
      <line
        x1="28"
        y1="38"
        x2="28"
        y2="52"
        stroke="#FFFFFF"
        strokeWidth="2.5"
      />
      <line
        x1="36"
        y1="38"
        x2="36"
        y2="52"
        stroke="#FFFFFF"
        strokeWidth="2.5"
      />
      <path
        d="M 37 24 L 43 20 L 46 22"
        fill="none"
        stroke="#FFFFFF"
        strokeWidth="2"
      />
    </>
  ),
  'must-face-shield': (
    <>
      <circle cx="32" cy="32" r="29" fill="#005387" />
      <path
        d="M 20 25 C 20 16 26 14 34 14 C 42 14 47 17 47 25 L 47 42 C 47 46 42 48 37 48 L 26 48 C 22 46 20 42 20 38 Z"
        fill="#FFFFFF"
      />
      <rect x="33" y="24" width="12" height="7" fill="#005387" />
      <rect
        x="32"
        y="23"
        width="14"
        height="9"
        fill="none"
        stroke="#FFFFFF"
        strokeWidth="1.5"
      />
      <circle cx="21" cy="27" r="2.5" fill="#005387" />
      <circle cx="21" cy="27" r="1.5" fill="#FFFFFF" />
      <path
        d="M 18 51 C 24 48 38 48 46 51"
        stroke="#FFFFFF"
        strokeWidth="3"
        fill="none"
      />
    </>
  ),
  'safe-exit': (
    <>
      <rect x="3" y="3" width="58" height="58" fill="#237F52" />
      <rect x="38" y="15" width="14" height="34" fill="#FFFFFF" />
      <rect x="41" y="18" width="11" height="31" fill="#237F52" />
      <circle cx="27" cy="22" r="3.5" fill="#FFFFFF" />
      <polygon points="25,26 29,25 32,36 28,37" fill="#FFFFFF" />
      <polygon points="30,28 39,26 38,29 29,31" fill="#FFFFFF" />
      <polygon points="26,29 18,34 19,37 27,32" fill="#FFFFFF" />
      <polygon points="30,36 37,42 37,47 34,47 28,39" fill="#FFFFFF" />
      <polygon points="28,37 20,38 14,46 12,44 19,35" fill="#FFFFFF" />
      <polygon
        points="12,18 19,18 19,15 24,20 19,25 19,22 12,22"
        fill="#FFFFFF"
      />
    </>
  ),
  'safe-first-aid': (
    <>
      <rect x="3" y="3" width="58" height="58" fill="#237F52" />
      <rect x="14" y="24" width="36" height="16" fill="#FFFFFF" />
      <rect x="24" y="14" width="16" height="36" fill="#FFFFFF" />
    </>
  ),
  'safe-aed': (
    <>
      <rect x="3" y="3" width="58" height="58" fill="#237F52" />
      <path
        d="M 32 49 L 16 33 C 10 27 10 17 18 14 C 24 11 29 15 32 19 C 35 15 40 11 46 14 C 54 17 54 27 48 33 Z"
        fill="#FFFFFF"
      />
      <polygon points="34,18 26,30 32,30 28,42 38,28 32,28" fill="#237F52" />
    </>
  ),
  'safe-assembly': (
    <>
      <rect x="3" y="3" width="58" height="58" fill="#237F52" />
      <circle cx="32" cy="27" r="2.5" fill="#FFFFFF" />
      <polygon points="29,31 35,31 34,42 30,42" fill="#FFFFFF" />
      <circle cx="26" cy="30" r="2" fill="#FFFFFF" />
      <polygon points="24,33 28,33 28,42 24,42" fill="#FFFFFF" />
      <circle cx="38" cy="30" r="2" fill="#FFFFFF" />
      <polygon points="36,33 40,33 40,42 36,42" fill="#FFFFFF" />
      <line
        x1="12"
        y1="12"
        x2="22"
        y2="22"
        stroke="#FFFFFF"
        strokeWidth="2.5"
      />
      <polygon points="15,22 22,22 22,15" fill="#FFFFFF" />
      <line
        x1="52"
        y1="12"
        x2="42"
        y2="22"
        stroke="#FFFFFF"
        strokeWidth="2.5"
      />
      <polygon points="49,22 42,22 42,15" fill="#FFFFFF" />
      <line
        x1="12"
        y1="52"
        x2="22"
        y2="42"
        stroke="#FFFFFF"
        strokeWidth="2.5"
      />
      <polygon points="15,42 22,42 22,49" fill="#FFFFFF" />
      <line
        x1="52"
        y1="52"
        x2="42"
        y2="42"
        stroke="#FFFFFF"
        strokeWidth="2.5"
      />
      <polygon points="49,42 42,42 42,49" fill="#FFFFFF" />
    </>
  ),
  'safe-eyewash': (
    <>
      <rect x="3" y="3" width="58" height="58" fill="#237F52" />
      <path
        d="M 16 34 C 22 24 42 24 48 34 C 42 44 22 44 16 34 Z"
        fill="none"
        stroke="#FFFFFF"
        strokeWidth="3"
      />
      <circle cx="32" cy="34" r="5" fill="#FFFFFF" />
      <circle cx="32" cy="34" r="2.5" fill="#237F52" />
      <rect x="18" y="14" width="6" height="5" fill="#FFFFFF" />
      <rect x="40" y="14" width="6" height="5" fill="#FFFFFF" />
      <line x1="14" y1="16" x2="18" y2="16" stroke="#FFFFFF" strokeWidth="2" />
      <line x1="46" y1="16" x2="50" y2="16" stroke="#FFFFFF" strokeWidth="2" />
      <path
        d="M 21 20 L 26 28"
        stroke="#FFFFFF"
        strokeWidth="2"
        strokeDasharray="2,2"
      />
      <path
        d="M 43 20 L 38 28"
        stroke="#FFFFFF"
        strokeWidth="2"
        strokeDasharray="2,2"
      />
      <circle cx="28" cy="24" r="1.5" fill="#FFFFFF" />
      <circle cx="36" cy="24" r="1.5" fill="#FFFFFF" />
      <circle cx="32" cy="21" r="1.5" fill="#FFFFFF" />
    </>
  ),
  'fire-extinguisher': (
    <>
      <rect x="3" y="3" width="58" height="58" fill="#C8102E" />
      <path
        d="M 26 25 C 26 21 30 19 34 19 C 38 19 42 21 42 25 Z"
        fill="#FFFFFF"
      />
      <rect x="26" y="24" width="16" height="26" fill="#FFFFFF" />
      <rect x="25" y="49" width="18" height="3" fill="#FFFFFF" />
      <rect x="33" y="15" width="2" height="5" fill="#FFFFFF" />
      <polygon points="31,14 39,12 39,15 31,16" fill="#FFFFFF" />
      <line x1="28" y1="18" x2="33" y2="18" stroke="#FFFFFF" strokeWidth="2" />
      <path
        d="M 35 18 C 45 18 46 28 46 36 L 46 44"
        fill="none"
        stroke="#FFFFFF"
        strokeWidth="2.5"
      />
      <polygon points="44,43 48,43 49,49 43,49" fill="#FFFFFF" />
      <polygon
        points="17,28 21,34 18,37 23,44 19,50 14,50 11,43 15,38 12,33"
        fill="#FFFFFF"
      />
    </>
  ),
  'fire-alarm': (
    <>
      <rect x="3" y="3" width="58" height="58" fill="#C8102E" />
      <path
        d="M 18 36 C 18 20 24 16 32 16 C 40 16 46 20 46 36 Z"
        fill="#FFFFFF"
      />
      <rect x="15" y="36" width="34" height="4" fill="#FFFFFF" />
      <rect x="30" y="12" width="4" height="5" fill="#FFFFFF" />
      <circle cx="32" cy="44" r="3.5" fill="#FFFFFF" />
      <path
        d="M 12 28 C 9 32 9 38 12 42"
        fill="none"
        stroke="#FFFFFF"
        strokeWidth="2.5"
      />
      <path
        d="M 8 24 C 4 30 4 40 8 46"
        fill="none"
        stroke="#FFFFFF"
        strokeWidth="2"
      />
      <path
        d="M 52 28 C 55 32 55 38 52 42"
        fill="none"
        stroke="#FFFFFF"
        strokeWidth="2.5"
      />
      <path
        d="M 56 24 C 60 30 60 40 56 46"
        fill="none"
        stroke="#FFFFFF"
        strokeWidth="2"
      />
    </>
  ),
  'fire-hose': (
    <>
      <rect x="3" y="3" width="58" height="58" fill="#C8102E" />
      <circle
        cx="32"
        cy="32"
        r="18"
        fill="none"
        stroke="#FFFFFF"
        strokeWidth="4"
      />
      <circle
        cx="32"
        cy="32"
        r="11"
        fill="none"
        stroke="#FFFFFF"
        strokeWidth="3"
      />
      <circle cx="32" cy="32" r="5" fill="#FFFFFF" />
      <path d="M 46 22 L 52 14" stroke="#FFFFFF" strokeWidth="3.5" />
      <polygon points="50,14 55,9 57,11 52,16" fill="#FFFFFF" />
      <polygon
        points="12,18 15,24 13,27 17,33 14,40 9,40 7,33 10,28 8,23"
        fill="#FFFFFF"
      />
      <line x1="56" y1="8" x2="60" y2="4" stroke="#FFFFFF" strokeWidth="1.5" />
      <line x1="58" y1="10" x2="62" y2="6" stroke="#FFFFFF" strokeWidth="1.5" />
    </>
  ),
} satisfies Record<string, ReactNode>

export type PictogramId = keyof typeof PICTOGRAMS

export function Pictogram({
  id,
  className,
  label,
}: {
  id: PictogramId
  className?: string
  label?: string
}) {
  return (
    <svg
      viewBox="0 0 64 64"
      className={className}
      role={label ? 'img' : undefined}
      aria-label={label}
      aria-hidden={label ? undefined : true}
    >
      {PICTOGRAMS[id]}
    </svg>
  )
}
