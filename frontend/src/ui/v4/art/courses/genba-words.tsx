export function genbaWordsArt({ className }: { className?: string }) {
  return (
    <svg
      viewBox='0 0 400 300'
      className={className}
      role='img'
      aria-hidden='true'
      preserveAspectRatio='xMidYMid slice'
    >
      <rect width='400' height='300' fill='#E4ECF2' />
      <rect x='36' y='36' width='154' height='88' fill='#214C69' />
      <polygon points='70,124 92,124 66,150' fill='#214C69' />
      <g fill='none' stroke='#E4ECF2' strokeWidth='6' strokeLinecap='square'>
        <path d='M 58 64 L 92 64 L 92 90' />
        <line x1='112' y1='60' x2='112' y2='94' />
        <line x1='124' y1='64' x2='142' y2='94' />
        <line x1='60' y1='104' x2='90' y2='104' />
        <line x1='110' y1='104' x2='156' y2='104' />
      </g>
      <rect x='210' y='46' width='154' height='82' fill='#0A1C26' />
      <polygon points='296,128 318,128 328,152' fill='#0A1C26' />
      <g stroke='#E4ECF2' strokeWidth='6' strokeLinecap='square'>
        <line x1='246' y1='66' x2='246' y2='104' />
        <line x1='246' y1='78' x2='272' y2='94' />
        <line x1='294' y1='74' x2='306' y2='104' />
        <line x1='334' y1='74' x2='322' y2='104' />
      </g>
      <path d='M 70 180 A 18 18 0 0 1 106 180 Z' fill='#0A1C26' />
      <line x1='66' y1='180' x2='112' y2='180' stroke='#0A1C26' strokeWidth='6' />
      <circle cx='88' cy='186' r='12' fill='#0A1C26' />
      <polygon points='50,270 50,216 88,198 126,216 126,270' fill='#0A1C26' />
      <rect x='80' y='216' width='16' height='54' fill='#A9BCCB' />
      <circle cx='312' cy='188' r='12' fill='#0A1C26' />
      <path d='M 294 182 A 18 18 0 0 1 330 182 Z' fill='#0A1C26' />
      <line x1='290' y1='182' x2='336' y2='182' stroke='#0A1C26' strokeWidth='6' />
      <polygon points='274,270 274,218 312,200 350,218 350,270' fill='#0A1C26' />
      <path
        d='M 172 238 A 28 28 0 0 1 228 238 Z'
        fill='#E4ECF2'
        stroke='#0A1C26'
        strokeWidth='6'
      />
      <rect x='164' y='238' width='72' height='8' fill='#0A1C26' />
      <rect x='196' y='214' width='8' height='18' fill='#214C69' />
      <rect x='191' y='219' width='18' height='8' fill='#214C69' />
      <rect x='24' y='270' width='352' height='6' fill='#0A1C26' />
    </svg>
  )
}
