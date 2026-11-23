// Pure geometry shared by the 3D Earth and the Japan map, so the hand-off between them lines up.
const RAD = Math.PI / 180

/** A lon/lat point on a radius-r sphere, in three.js SphereGeometry's layout (lon 0 on +x, 90°E on −z). */
export function onSphere(
  lon: number,
  lat: number,
  r = 1,
): [number, number, number] {
  const φ = lat * RAD
  const λ = lon * RAD
  return [
    r * Math.cos(φ) * Math.cos(λ),
    r * Math.sin(φ),
    -r * Math.cos(φ) * Math.sin(λ),
  ]
}

/** Euler angles (order 'XYZ') that turn the Earth so (lon, lat) faces a camera on the +z axis. */
export function facing(lon: number, lat: number): { x: number; y: number } {
  return { x: lat * RAD, y: -Math.PI / 2 - lon * RAD }
}

/** Camera distance from a radius-1 globe at which one radian at the centre spans `scale` px, the same
 *  scale as d3's orthographic projection, so the 3D view at the end of the flight matches the 2D map. */
export function distanceForScale(
  scale: number,
  viewportH: number,
  fovDeg: number,
): number {
  const f = viewportH / 2 / Math.tan((fovDeg * RAD) / 2)
  return 1 + f / scale
}
