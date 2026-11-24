export function lowBackArt({ className }: { className?: string }) {
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
      <circle cx="154" cy="92" r="22" fill="#0F2F47" />
      <rect x="154" y="108" width="28" height="6" fill="#0F2F47" />
      <polygon points="144,116 174,130 152,192 126,184" fill="#9FB8CF" />
      <line
        x1="152"
        y1="116"
        x2="132"
        y2="186"
        stroke="#1B4C74"
        strokeWidth="8"
        strokeLinecap="square"
      />
      <polygon
        points="126,182 138,190 190,222 182,236 122,198"
        fill="#0F2F47"
      />
      <polygon points="182,222 194,230 162,264 150,258" fill="#0F2F47" />
      <rect x="144" y="256" width="46" height="8" fill="#0F2F47" />
      <line
        x1="166"
        y1="126"
        x2="194"
        y2="174"
        stroke="#0F2F47"
        strokeWidth="8"
        strokeLinecap="square"
      />
      <line
        x1="194"
        y1="174"
        x2="222"
        y2="204"
        stroke="#0F2F47"
        strokeWidth="8"
        strokeLinecap="square"
      />
      <rect x="218" y="148" width="86" height="86" fill="#0F2F47" />
      <rect x="218" y="182" width="86" height="18" fill="#9FB8CF" />
      <rect x="244" y="172" width="6" height="30" fill="#E1EBF4" />
      <polygon points="247,160 238,174 256,174" fill="#E1EBF4" />
      <rect x="272" y="172" width="6" height="30" fill="#E1EBF4" />
      <polygon points="275,160 266,174 284,174" fill="#E1EBF4" />
      <rect x="330" y="162" width="10" height="58" fill="#1B4C74" />
      <polygon points="335,142 316,168 354,168" fill="#1B4C74" />
    </svg>
  )
}
