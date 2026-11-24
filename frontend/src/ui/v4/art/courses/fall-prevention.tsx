export function fallPreventionArt({ className }: { className?: string }) {
  return (
    <svg
      viewBox='0 0 400 300'
      className={className}
      role='img'
      aria-hidden='true'
      preserveAspectRatio='xMidYMid slice'
    >
      <rect width='400' height='300' fill='#E1EBF4' />
      <rect x='24' y='264' width='352' height='10' fill='#0F2F47' />
      <rect x='290' y='250' width='30' height='14' fill='#9FB8CF' />
      <rect x='68' y='88' width='164' height='10' fill='#0F2F47' />
      <rect x='68' y='82' width='164' height='6' fill='#0F2F47' />
      <g stroke='#0F2F47' strokeWidth='6' strokeLinecap='square'>
        <line x1='80' y1='48' x2='80' y2='264' />
        <line x1='220' y1='48' x2='220' y2='264' />
        <line x1='80' y1='176' x2='220' y2='176' />
        <line x1='68' y1='48' x2='232' y2='48' />
        <line x1='68' y1='68' x2='232' y2='68' />
        <line x1='98' y1='98' x2='98' y2='264' />
        <line x1='122' y1='98' x2='122' y2='264' />
        <line x1='98' y1='140' x2='122' y2='140' />
        <line x1='98' y1='182' x2='122' y2='182' />
        <line x1='98' y1='224' x2='122' y2='224' />
      </g>
      <g stroke='#9FB8CF' strokeWidth='6' strokeLinecap='square'>
        <line x1='80' y1='176' x2='220' y2='264' />
        <line x1='220' y1='176' x2='80' y2='264' />
        <line x1='80' y1='98' x2='220' y2='176' />
        <line x1='220' y1='98' x2='80' y2='176' />
      </g>
      <rect x='240' y='32' width='80' height='28' fill='#1B4C74' />
      <line x1='280' y1='60' x2='190' y2='58' stroke='#1B4C74' strokeWidth='6' />
      <rect x='182' y='52' width='12' height='12' fill='#0F2F47' />
      <polygon points='164,44 186,44 175,32' fill='#1B4C74' />
      <line x1='160' y1='44' x2='190' y2='44' stroke='#1B4C74' strokeWidth='6' />
      <circle cx='175' cy='48' r='6' fill='#0F2F47' />
      <rect x='168' y='54' width='14' height='18' fill='#0F2F47' />
      <line x1='168' y1='54' x2='182' y2='72' stroke='#9FB8CF' strokeWidth='6' />
      <line x1='182' y1='54' x2='168' y2='72' stroke='#9FB8CF' strokeWidth='6' />
      <rect x='168' y='72' width='5' height='16' fill='#0F2F47' />
      <rect x='177' y='72' width='5' height='16' fill='#0F2F47' />
    </svg>
  )
}
