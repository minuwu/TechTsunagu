export function heatstrokeArt({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 400 300"
      className={className}
      role="img"
      aria-hidden="true"
      preserveAspectRatio="xMidYMid slice"
    >
      <rect width="400" height="300" fill="#E1EBF4" />
      <rect x="24" y="264" width="352" height="6" fill="#0F2F47" />
      <circle cx="288" cy="80" r="44" fill="#0F2F47" />
      <rect x="284" y="24" width="8" height="16" fill="#0F2F47" />
      <rect x="284" y="120" width="8" height="16" fill="#0F2F47" />
      <rect x="232" y="76" width="16" height="8" fill="#0F2F47" />
      <rect x="336" y="76" width="16" height="8" fill="#0F2F47" />
      <polyline
        points="150,190 162,170 150,150 162,130 150,110"
        stroke="#0F2F47"
        strokeWidth="6"
        strokeLinecap="square"
        strokeLinejoin="miter"
        fill="none"
      />
      <polyline
        points="180,210 192,190 180,170 192,150 180,130"
        stroke="#9FB8CF"
        strokeWidth="6"
        strokeLinecap="square"
        strokeLinejoin="miter"
        fill="none"
      />
      <rect x="66" y="54" width="28" height="150" fill="#9FB8CF" />
      <circle cx="80" cy="224" r="28" fill="#9FB8CF" />
      <rect x="74" y="90" width="12" height="116" fill="#0F2F47" />
      <circle cx="80" cy="224" r="18" fill="#0F2F47" />
      <rect x="94" y="96" width="16" height="6" fill="#0F2F47" />
      <rect x="94" y="126" width="16" height="6" fill="#0F2F47" />
      <rect x="94" y="156" width="16" height="6" fill="#0F2F47" />
      <rect x="254" y="146" width="28" height="14" fill="#0F2F47" />
      <rect x="260" y="160" width="16" height="10" fill="#0F2F47" />
      <rect x="240" y="170" width="56" height="94" fill="#1B4C74" />
      <rect x="240" y="200" width="56" height="20" fill="#E1EBF4" />
      <path
        d="M 326 182 C 326 182 342 206 342 220 A 16 16 0 1 1 310 220 C 310 206 326 182 326 182 Z"
        fill="#1B4C74"
      />
    </svg>
  )
}
