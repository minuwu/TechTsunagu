export function shisaKankoArt({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 400 300"
      className={className}
      role="img"
      aria-hidden="true"
      preserveAspectRatio="xMidYMid slice"
    >
      <rect width="400" height="300" fill="#E3E2DE" />
      <rect x="24" y="146" width="86" height="72" fill="#C7C7C7" />
      <rect x="94" y="146" width="16" height="72" fill="#141414" />
      <rect x="110" y="154" width="58" height="56" fill="#141414" />
      <rect x="124" y="140" width="36" height="16" fill="#141414" />
      <rect x="168" y="154" width="72" height="18" fill="#141414" />
      <circle cx="306" cy="106" r="54" fill="#141414" />
      <circle cx="306" cy="106" r="42" fill="#E3E2DE" />
      <rect x="303" y="68" width="6" height="12" fill="#141414" />
      <rect x="268" y="103" width="12" height="6" fill="#141414" />
      <rect x="332" y="103" width="12" height="6" fill="#141414" />
      <polygon points="304,74 308,74 309,108 303,108" fill="#141414" />
      <circle cx="306" cy="106" r="8" fill="#141414" />
      <line
        x1="244"
        y1="163"
        x2="276"
        y2="138"
        stroke="#141414"
        strokeWidth="6"
        strokeDasharray="8 8"
      />
      <path
        d="M196 112 C216 94 244 90 262 90"
        stroke="#141414"
        strokeWidth="6"
        fill="none"
      />
      <path
        d="M176 86 C208 58 250 54 276 56"
        stroke="#141414"
        strokeWidth="6"
        fill="none"
      />
      <line
        x1="210"
        y1="132"
        x2="240"
        y2="114"
        stroke="#141414"
        strokeWidth="6"
      />
      <rect x="260" y="196" width="88" height="76" fill="#1351AA" />
      <polyline
        points="276,236 294,254 332,214"
        stroke="#E3E2DE"
        strokeWidth="12"
        strokeLinecap="square"
        strokeLinejoin="miter"
        fill="none"
      />
    </svg>
  )
}
