// SVG -> PNG / WebP via sharp (bundled librsvg). No fonts: outline all text before rendering.
// Usage: node render.cjs <in.svg> <out.png|out.webp> <width> [background-hex|none]
// Needs NODE_PATH to point at the brand-tools node_modules (see docs/brand/tools/README.md).
const fs = require('fs')
const path = require('path')
const sharp = require('sharp')

async function main() {
  const [, , input, output, widthArg, bg] = process.argv
  if (!input || !output || !widthArg) {
    console.error('usage: node render.cjs <in.svg> <out.png|webp> <width> [bg-hex|none]')
    process.exit(2)
  }
  const width = Number(widthArg)
  const svg = fs.readFileSync(input)
  let img = sharp(svg, { density: 384 }).resize({ width })
  if (bg && bg !== 'none') img = img.flatten({ background: bg })
  const ext = path.extname(output).toLowerCase()
  if (ext === '.webp') img = img.webp({ quality: 90 })
  else if (ext === '.png') img = img.png({ compressionLevel: 9 })
  else throw new Error('output must be .png or .webp')
  fs.mkdirSync(path.dirname(path.resolve(output)), { recursive: true })
  await img.toFile(output)
  const m = await sharp(output).metadata()
  console.log(`${output} ${m.width}x${m.height} ${fs.statSync(output).size}B`)
}
main().catch((e) => {
  console.error(e.message)
  process.exit(1)
})
