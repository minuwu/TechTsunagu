import { describe, expect, it } from 'vitest'
import { distanceForScale, facing, onSphere } from './globe-math'

// Rotate v by Euler XYZ (x then y applied as R = Rx·Ry, three.js order 'XYZ')
function rotate([x, y, z]: number[], ex: number, ey: number) {
  const [x1, z1] = [
    x! * Math.cos(ey) + z! * Math.sin(ey),
    -x! * Math.sin(ey) + z! * Math.cos(ey),
  ]
  return [
    x1,
    y! * Math.cos(ex) - z1 * Math.sin(ex),
    y! * Math.sin(ex) + z1 * Math.cos(ex),
  ]
}

describe('onSphere', () => {
  it('puts lon 0 on +x, lon 90°E on −z and the north pole on +y (three.js sphere UVs)', () => {
    expect(onSphere(0, 0)[0]).toBeCloseTo(1)
    expect(onSphere(90, 0)[2]).toBeCloseTo(-1)
    expect(onSphere(0, 90)[1]).toBeCloseTo(1)
  })
})

describe('facing', () => {
  it.each([
    [129.87, 32.75], // Nagasaki
    [-74, 40.7],
    [0, 0],
  ])('turns (%f, %f) to face the camera on +z', (lon, lat) => {
    const { x, y } = facing(lon, lat)
    const [px, py, pz] = rotate(onSphere(lon, lat), x, y)
    expect(px).toBeCloseTo(0)
    expect(py).toBeCloseTo(0)
    expect(pz).toBeCloseTo(1)
  })
})

describe('distanceForScale', () => {
  it('places the camera so one radian at the centre spans `scale` pixels', () => {
    const h = 800
    const fov = 35
    const d = distanceForScale(1200, h, fov)
    const f = h / 2 / Math.tan((fov * Math.PI) / 360)
    // a small arc ε at the centre projects to f·sin ε / (d − cos ε) ≈ f·ε / (d − 1)
    expect(f / (d - 1)).toBeCloseTo(1200)
  })
  it('gets closer as the scale grows', () => {
    expect(distanceForScale(4000, 800, 35)).toBeLessThan(
      distanceForScale(1000, 800, 35),
    )
  })
})
