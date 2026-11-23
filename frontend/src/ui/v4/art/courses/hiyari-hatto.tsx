export function hiyariHattoArt({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 400 300"
      className={className}
      role="img"
      aria-hidden="true"
      preserveAspectRatio="xMidYMid slice"
    >
      <rect width="400" height="300" fill="#E3E2DE" />
      <rect x="24" y="264" width="352" height="6" fill="#141414" />
      <rect x="40" y="210" width="120" height="48" fill="#C7C7C7" />
      <rect x="62" y="156" width="76" height="48" fill="#141414" />
      <rect x="85" y="102" width="30" height="48" fill="#1351AA" />
      <polygon points="340,36 302,106 378,106" fill="#141414" />
      <rect x="337" y="52" width="6" height="28" fill="#E3E2DE" />
      <rect x="337" y="88" width="6" height="8" fill="#E3E2DE" />
      <rect x="194" y="234" width="124" height="14" fill="#141414" />
      <path d="M 206 234 C 206 170 306 170 306 234 Z" fill="#141414" />
      <rect x="251" y="176" width="10" height="48" fill="#C7C7C7" />
      <circle cx="256" cy="40" r="16" fill="#141414" />
      <circle cx="256" cy="40" r="8" fill="#E3E2DE" />
      <rect x="250" y="44" width="12" height="52" fill="#141414" />
      <path
        d="M 242 96 L 270 96 L 270 148 L 262 148 L 262 128 L 250 128 L 250 148 L 242 148 Z"
        fill="#141414"
      />
      <line
        x1="228"
        y1="162"
        x2="284"
        y2="162"
        stroke="#1351AA"
        strokeWidth="6"
      />
      <line
        x1="238"
        y1="152"
        x2="274"
        y2="152"
        stroke="#1351AA"
        strokeWidth="6"
      />
    </svg>
  )
}
