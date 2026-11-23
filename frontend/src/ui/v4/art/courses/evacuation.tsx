export function evacuationArt({ className }: { className?: string }) {
  return (
    <svg
      viewBox='0 0 400 300'
      className={className}
      role='img'
      aria-hidden='true'
      preserveAspectRatio='xMidYMid slice'
    >
      <rect width='400' height='300' fill='#E3E2DE' />
      <polygon
        points='24,264 24,64 74,64 74,96 124,96 124,136 168,136 168,264'
        fill='#1351AA'
      />
      <polygon
        points='24,150 74,150 74,180 134,180 134,264 24,264'
        fill='#141414'
      />
      <rect x='64' y='54' width='16' height='10' fill='#C7C7C7' />
      <rect x='114' y='86' width='16' height='10' fill='#C7C7C7' />
      <rect x='158' y='126' width='16' height='10' fill='#C7C7C7' />
      <polygon points='140,264 260,190 376,140 376,264' fill='#141414' />
      <line x1='344' y1='64' x2='344' y2='140' stroke='#141414' strokeWidth='6' />
      <polygon points='344,68 344,106 292,87' fill='#1351AA' />
      <line x1='210' y1='226' x2='260' y2='194' stroke='#C7C7C7' strokeWidth='8' />
      <polygon points='262,180 278,196 254,206' fill='#C7C7C7' />
      <circle cx='208' cy='152' r='11' fill='#E3E2DE' />
      <polygon points='200,166 220,162 214,196 196,192' fill='#E3E2DE' />
      <g fill='none' stroke='#E3E2DE' strokeWidth='6' strokeLinecap='square'>
        <line x1='212' y1='170' x2='232' y2='168' />
        <line x1='202' y1='174' x2='184' y2='184' />
        <path d='M 212 195 L 232 214 L 246 208' />
        <line x1='198' y1='193' x2='176' y2='208' />
      </g>
      <rect x='24' y='264' width='352' height='8' fill='#141414' />
    </svg>
  )
}
