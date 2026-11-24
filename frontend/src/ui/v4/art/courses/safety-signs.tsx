export function safetySignsArt({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 400 300"
      className={className}
      role="img"
      aria-hidden="true"
      preserveAspectRatio="xMidYMid slice"
    >
      <rect width="400" height="300" fill="#E1EBF4" />
      <line
        x1="200"
        y1="24"
        x2="200"
        y2="276"
        stroke="#9FB8CF"
        strokeWidth="6"
      />
      <line
        x1="24"
        y1="150"
        x2="376"
        y2="150"
        stroke="#9FB8CF"
        strokeWidth="6"
      />
      <circle
        cx="110"
        cy="88"
        r="46"
        fill="none"
        stroke="#0F2F47"
        strokeWidth="12"
      />
      <line
        x1="77"
        y1="55"
        x2="143"
        y2="121"
        stroke="#0F2F47"
        strokeWidth="12"
      />
      <rect x="98" y="82" width="24" height="12" fill="#0F2F47" />
      <polygon points="290,42 238,134 342,134" fill="#0F2F47" />
      <polygon points="290,62 254,124 326,124" fill="#E1EBF4" />
      <rect x="286" y="74" width="8" height="28" fill="#0F2F47" />
      <rect x="286" y="108" width="8" height="8" fill="#0F2F47" />
      <circle cx="110" cy="212" r="48" fill="#1B4C74" />
      <rect x="84" y="204" width="52" height="18" fill="#E1EBF4" />
      <rect x="92" y="208" width="14" height="10" fill="#1B4C74" />
      <rect x="114" y="208" width="14" height="10" fill="#1B4C74" />
      <line
        x1="74"
        y1="213"
        x2="84"
        y2="213"
        stroke="#E1EBF4"
        strokeWidth="6"
      />
      <line
        x1="136"
        y1="213"
        x2="146"
        y2="213"
        stroke="#E1EBF4"
        strokeWidth="6"
      />
      <rect x="236" y="166" width="108" height="92" fill="#0F2F47" />
      <rect x="308" y="180" width="24" height="64" fill="#E1EBF4" />
      <circle cx="270" cy="192" r="8" fill="#E1EBF4" />
      <polygon points="264,204 276,201 270,224 258,222" fill="#E1EBF4" />
      <polyline
        points="270,224 286,234 290,244"
        stroke="#E1EBF4"
        strokeWidth="6"
        fill="none"
      />
      <polyline
        points="262,222 248,236 242,242"
        stroke="#E1EBF4"
        strokeWidth="6"
        fill="none"
      />
      <polygon points="296,204 296,216 304,210" fill="#E1EBF4" />
    </svg>
  )
}
