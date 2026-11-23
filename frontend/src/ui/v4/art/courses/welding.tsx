export function weldingArt({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 400 300"
      className={className}
      role="img"
      aria-hidden="true"
      preserveAspectRatio="xMidYMid slice"
    >
      <rect width="400" height="300" fill="#E3E2DE" />
      <polygon
        points="64,52 156,52 168,98 168,202 144,254 84,254 58,202 58,98"
        fill="#141414"
      />
      <rect
        x="80"
        y="86"
        width="68"
        height="44"
        fill="none"
        stroke="#E3E2DE"
        strokeWidth="6"
      />
      <rect x="84" y="90" width="60" height="36" fill="#C7C7C7" />
      <rect x="92" y="98" width="44" height="20" fill="#141414" />
      <circle cx="58" cy="118" r="10" fill="#C7C7C7" />
      <line
        x1="364"
        y1="264"
        x2="304"
        y2="204"
        stroke="#141414"
        strokeWidth="16"
        strokeLinecap="square"
      />
      <line
        x1="338"
        y1="250"
        x2="348"
        y2="240"
        stroke="#C7C7C7"
        strokeWidth="6"
      />
      <line
        x1="304"
        y1="204"
        x2="254"
        y2="174"
        stroke="#141414"
        strokeWidth="12"
        strokeLinecap="square"
      />
      <line
        x1="254"
        y1="174"
        x2="232"
        y2="162"
        stroke="#C7C7C7"
        strokeWidth="6"
        strokeLinecap="square"
      />
      <polygon
        points="216,132 232,156 256,166 232,176 216,200 200,176 176,166 200,156"
        fill="#1351AA"
      />
      <polygon
        points="216,146 226,160 238,166 226,172 216,186 206,172 194,166 206,160"
        fill="#E3E2DE"
      />
      <polyline
        points="236,148 254,134 270,136"
        stroke="#141414"
        strokeWidth="6"
        fill="none"
      />
      <polyline
        points="242,182 264,196 280,192"
        stroke="#141414"
        strokeWidth="6"
        fill="none"
      />
      <polyline
        points="192,148 174,134 158,138"
        stroke="#141414"
        strokeWidth="6"
        fill="none"
      />
      <polyline
        points="188,184 168,204 152,200"
        stroke="#141414"
        strokeWidth="6"
        fill="none"
      />
      <polyline
        points="216,118 220,98 230,92"
        stroke="#141414"
        strokeWidth="6"
        fill="none"
      />
      <rect x="152" y="216" width="164" height="18" fill="#141414" />
      <rect x="152" y="210" width="74" height="6" fill="#C7C7C7" />
    </svg>
  )
}
