export function fiveSArt({ className }: { className?: string }) {
  return (
    <svg
      viewBox='0 0 400 300'
      className={className}
      role='img'
      aria-hidden='true'
      preserveAspectRatio='xMidYMid slice'
    >
      <rect width='400' height='300' fill='#E3E2DE' />
      <rect x='36' y='36' width='64' height='20' fill='#1351AA' />
      <rect x='36' y='60' width='64' height='20' fill='#141414' />
      <rect x='36' y='84' width='64' height='20' fill='#C7C7C7' />
      <rect x='36' y='108' width='64' height='20' fill='#141414' />
      <rect x='36' y='132' width='64' height='20' fill='#C7C7C7' />
      <rect
        x='120'
        y='36'
        width='236'
        height='136'
        fill='#E3E2DE'
        stroke='#141414'
        strokeWidth='6'
      />
      <g stroke='#C7C7C7' strokeWidth='6' strokeDasharray='4 24'>
        <line x1='120' y1='80' x2='356' y2='80' />
        <line x1='120' y1='126' x2='356' y2='126' />
      </g>
      <rect x='142' y='50' width='22' height='10' fill='#141414' />
      <rect x='148' y='60' width='10' height='40' fill='#141414' />
      <rect x='198' y='50' width='38' height='16' fill='#141414' />
      <rect x='212' y='66' width='10' height='58' fill='#141414' />
      <polygon points='264,52 304,52 304,62 274,62 274,118 264,118' fill='#141414' />
      <polygon points='322,54 340,54 344,98 336,98 331,76 326,98 318,98' fill='#141414' />
      <line x1='336' y1='150' x2='352' y2='240' stroke='#141414' strokeWidth='6' />
      <rect x='336' y='240' width='34' height='10' fill='#1351AA' />
      <polygon points='334,250 372,250 376,268 330,268' fill='#141414' />
      <rect x='24' y='196' width='352' height='78' fill='#C7C7C7' />
      <line x1='24' y1='196' x2='376' y2='196' stroke='#141414' strokeWidth='6' />
      <rect x='24' y='224' width='352' height='18' fill='#E3E2DE' />
      <polygon points='50,224 66,224 48,242 32,242' fill='#141414' />
      <polygon points='110,224 126,224 108,242 92,242' fill='#141414' />
      <polygon points='170,224 186,224 168,242 152,242' fill='#141414' />
      <polygon points='230,224 246,224 228,242 212,242' fill='#141414' />
    </svg>
  )
}
