// Validate a Lottie JSON in lottie-web (what the site will most likely play with) and render frames.
// Usage: node lottie-check.cjs <anim.json> <out-dir> [frames=6] [bg-hex=#E3E2DE]
// Writes <out-dir>/<name>-f0..fN.png and prints a one-line report. Exit 1 on any error.
// Needs NODE_PATH to point at the brand-tools node_modules (see docs/brand/tools/README.md).
const fs = require('fs')
const path = require('path')
const { chromium } = require('playwright-core')

const CHROME = process.env.CHROME_PATH || 'C:/Program Files/Google/Chrome/Application/chrome.exe'
const BAD_TYPES = { 5: 'text layer', 9: 'audio layer', 6: 'audio layer' }

async function main() {
  const [, , file, outDir, framesArg, bgArg] = process.argv
  if (!file || !outDir) {
    console.error('usage: node lottie-check.cjs <anim.json> <out-dir> [frames] [bg]')
    process.exit(2)
  }
  const frames = Number(framesArg || 6)
  const bg = bgArg || '#E3E2DE'
  const data = JSON.parse(fs.readFileSync(file, 'utf8').replace(/^﻿/, ''))
  const problems = []
  for (const l of data.layers || []) {
    if (BAD_TYPES[l.ty]) problems.push(`layer "${l.nm}" is a ${BAD_TYPES[l.ty]} (not allowed)`)
    if (l.ef && l.ef.length) problems.push(`layer "${l.nm}" has effects (not allowed)`)
  }
  if (/"x"\s*:\s*"/.test(JSON.stringify(data))) problems.push('expressions found (not allowed)')
  if (data.assets && data.assets.some((a) => a.p && !a.layers)) problems.push('image assets found (shape layers only)')

  const lottiePath = require.resolve('lottie-web/build/player/lottie.min.js')
  const browser = await chromium.launch({ executablePath: CHROME, headless: true })
  const page = await browser.newPage({ viewport: { width: Math.min(data.w, 800), height: Math.round((Math.min(data.w, 800) * data.h) / data.w) } })
  const errors = []
  page.on('pageerror', (e) => errors.push(e.message))
  page.on('console', (m) => m.type() === 'error' && errors.push(m.text()))
  await page.setContent(`<body style="margin:0;background:${bg}"><div id="a" style="width:100vw;height:100vh"></div></body>`)
  await page.addScriptTag({ path: lottiePath })
  const info = await page.evaluate((d) => {
    window.anim = lottie.loadAnimation({ container: document.getElementById('a'), renderer: 'svg', loop: false, autoplay: false, animationData: d })
    return { total: window.anim.totalFrames, fr: window.anim.frameRate }
  }, data)
  fs.mkdirSync(outDir, { recursive: true })
  const name = path.basename(file, '.json')
  for (let i = 0; i < frames; i++) {
    const f = Math.min(info.total - 1, Math.round((i / (frames - 1)) * (info.total - 1)))
    await page.evaluate((fr) => window.anim.goToAndStop(fr, true), f)
    await page.screenshot({ path: path.join(outDir, `${name}-f${i}.png`) })
  }
  await browser.close()
  const kb = (fs.statSync(file).size / 1024).toFixed(1)
  const dur = (info.total / info.fr).toFixed(2)
  const all = [...problems, ...errors]
  console.log(`${name}: ${data.w}x${data.h} ${info.fr}fps ${dur}s ${kb}KB ${all.length ? 'FAIL: ' + all.join('; ') : 'OK'}`)
  process.exit(all.length ? 1 : 0)
}
main().catch((e) => {
  console.error(e.message)
  process.exit(1)
})
