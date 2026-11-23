// The Nagasaki journey's 3D Earth (three.js r134, the release already installed for Vanta). Loaded with a
// dynamic import, so three.js is only fetched when the section comes near the screen.
import {
  AdditiveBlending,
  BackSide,
  BufferGeometry,
  CircleGeometry,
  Color,
  Float32BufferAttribute,
  Group,
  Mesh,
  MeshBasicMaterial,
  PerspectiveCamera,
  Points,
  PointsMaterial,
  RingGeometry,
  Scene,
  ShaderMaterial,
  SphereGeometry,
  TextureLoader,
  Vector3,
  WebGLRenderer,
} from 'three'
import { gsap } from 'gsap'
import { distanceForScale, facing, onSphere } from './globe-math'

/** Where the camera looks (degrees) and the map scale (px per radian) to match at the end of the flight. */
export interface View {
  lon: number
  lat: number
  scale: number
}

export interface Earth {
  resize(w: number, h: number): void
  /** Spin from where the Earth is to `to`, slow down, light up home, then call `done`. */
  fly(to: View, done: () => void): void
  /** Back to the slowly turning whole Earth (before a replay). */
  reset(): void
  /** Render only while the section is on screen and the Earth is shown. */
  setActive(on: boolean): void
  dispose(): void
}

const FOV = 35
const WHOLE = 0.36 // the whole Earth's radius as a share of the stage's shorter side
const START = { lon: 18, lat: 16 } // Europe and Africa: the flight crosses Asia to reach Japan
const SPIN = 3 // idle turn, degrees per second
const STARS = 1600

const earthVertex = /* glsl */ `
  varying vec2 vUv;
  varying vec3 vNormal;
  varying vec3 vWorld;
  void main() {
    vUv = uv;
    vNormal = normalize(mat3(modelMatrix) * normal);
    vec4 world = modelMatrix * vec4(position, 1.0);
    vWorld = world.xyz;
    gl_Position = projectionMatrix * viewMatrix * world;
  }
`
// Muted, cool ocean tones: the photo is partly desaturated and tinted toward the section's navy;
// a soft terminator keeps the night side readable, and a thin rim of light marks the atmosphere.
const earthFragment = /* glsl */ `
  uniform sampler2D map;
  uniform vec3 sun;
  uniform vec3 tint;
  uniform vec3 rimColor;
  varying vec2 vUv;
  varying vec3 vNormal;
  varying vec3 vWorld;
  void main() {
    vec3 c = texture2D(map, vUv).rgb;
    float luma = dot(c, vec3(0.299, 0.587, 0.114));
    c = mix(c, vec3(luma), 0.3);
    c = mix(c, c * tint, 0.5);
    vec3 n = normalize(vNormal);
    float light = 0.16 + 0.84 * smoothstep(-0.2, 0.65, dot(n, sun));
    float rim = pow(1.0 - max(dot(normalize(cameraPosition - vWorld), n), 0.0), 3.0);
    gl_FragColor = vec4(c * light + rimColor * rim * 0.6, 1.0);
  }
`
const airVertex = /* glsl */ `
  varying vec3 vNormal;
  void main() {
    vNormal = normalize(normalMatrix * normal);
    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
  }
`
const airFragment = /* glsl */ `
  uniform vec3 glow;
  varying vec3 vNormal;
  void main() {
    float i = pow(clamp(0.72 - dot(vNormal, vec3(0.0, 0.0, 1.0)), 0.0, 1.0), 3.0);
    gl_FragColor = vec4(glow * i, i);
  }
`

const token = (el: Element, name: string, fallback: string) =>
  getComputedStyle(el).getPropertyValue(name).trim() || fallback

/** Throws if WebGL is unavailable; the caller then goes straight to the 2D map. */
export function createEarth(
  canvas: HTMLCanvasElement,
  opts: { home: [number, number]; onReady: () => void; onError: () => void },
): Earth {
  const renderer = new WebGLRenderer({ canvas, antialias: true })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.setClearColor(new Color(token(canvas, '--j-bg', '#0b1424')))

  const scene = new Scene()
  const camera = new PerspectiveCamera(FOV, 1, 0.1, 200)
  let far = 3.3 // camera distance for the whole Earth; set from the stage size in resize()
  camera.position.set(0, 0, far)

  const earth = new Group()
  scene.add(earth)

  const big = Math.max(window.innerWidth, window.innerHeight) >= 900
  const map = new TextureLoader().load(
    big ? '/media/earth-3840.webp' : '/media/earth-2048.webp',
    () => opts.onReady(),
    undefined,
    () => opts.onError(),
  )
  map.anisotropy = renderer.capabilities.getMaxAnisotropy()

  const globe = new Mesh(
    new SphereGeometry(1, 128, 96),
    new ShaderMaterial({
      uniforms: {
        map: { value: map },
        sun: { value: new Vector3(-0.55, 0.45, 1).normalize() },
        tint: { value: new Color('#9fb6dc') },
        rimColor: { value: new Color('#4f7bd1') },
      },
      vertexShader: earthVertex,
      fragmentShader: earthFragment,
    }),
  )
  earth.add(globe)

  const air = new Mesh(
    new SphereGeometry(1.14, 96, 64),
    new ShaderMaterial({
      uniforms: { glow: { value: new Color('#5d8fe8') } },
      vertexShader: airVertex,
      fragmentShader: airFragment,
      side: BackSide,
      blending: AdditiveBlending,
      transparent: true,
      depthWrite: false,
    }),
  )
  scene.add(air)

  // Stars: fixed points on a far shell, a few pixels each
  const starPos = new Float32Array(STARS * 3)
  for (let i = 0; i < STARS; i++) {
    const p = onSphere(
      Math.random() * 360 - 180,
      Math.asin(Math.random() * 2 - 1) * (180 / Math.PI),
      60 + Math.random() * 40,
    )
    starPos.set(p, i * 3)
  }
  const starGeo = new BufferGeometry()
  starGeo.setAttribute('position', new Float32BufferAttribute(starPos, 3))
  scene.add(
    new Points(
      starGeo,
      new PointsMaterial({
        color: new Color('#c9d3e6'),
        size: 1.4,
        sizeAttenuation: false,
        transparent: true,
        opacity: 0.7,
        depthWrite: false,
      }),
    ),
  )

  // Home (Nagasaki): a dot and a pulsing ring on the surface, kept the same size on screen
  const accent = new Color(token(canvas, '--j-accent', '#d9573b'))
  const dotMat = new MeshBasicMaterial({
    color: accent,
    transparent: true,
    opacity: 0,
  })
  const ringMat = new MeshBasicMaterial({
    color: accent,
    transparent: true,
    opacity: 0,
  })
  const marker = new Group()
  marker.add(new Mesh(new CircleGeometry(1, 32), dotMat))
  const ring = new Mesh(new RingGeometry(1.6, 2, 48), ringMat)
  marker.add(ring)
  marker.position.set(...onSphere(opts.home[0], opts.home[1], 1.003))
  marker.lookAt(marker.position.clone().multiplyScalar(2))
  earth.add(marker)

  const state = { lon: START.lon, lat: START.lat, dist: far, mark: 0 }
  let flying = false
  let size = { w: 1, h: 1 }
  let frame = 0
  let last = 0
  let tl: gsap.core.Timeline | null = null

  const draw = (now: number) => {
    const dt = Math.min(0.05, (now - last) / 1000 || 0)
    last = now
    if (!flying) state.lon += SPIN * dt
    const r = facing(state.lon, state.lat)
    earth.rotation.set(r.x, r.y, 0, 'XYZ')
    camera.position.z = state.dist
    // the marker's radius is 4 px whatever the distance: px per world unit at the centre of the view
    const pxPerUnit =
      size.h / 2 / Math.tan((FOV * Math.PI) / 360) / (state.dist - 1)
    marker.scale.setScalar(4 / pxPerUnit)
    const pulse = (now / 1400) % 1
    ring.scale.setScalar(1 + pulse * 2.4)
    ringMat.opacity = state.mark * (1 - pulse) * 0.9
    dotMat.opacity = state.mark
    renderer.render(scene, camera)
    frame = requestAnimationFrame(draw)
  }

  return {
    resize(w, h) {
      size = { w: Math.max(1, w), h: Math.max(1, h) }
      renderer.setSize(size.w, size.h, false)
      camera.aspect = size.w / size.h
      camera.updateProjectionMatrix()
      // a sphere at distance d looks f / sqrt(d² − 1) px in radius
      const f = size.h / 2 / Math.tan((FOV * Math.PI) / 360)
      far = Math.hypot(f / (WHOLE * Math.min(size.w, size.h)), 1)
      if (!flying) state.dist = far
    },
    fly(to, done) {
      tl?.kill()
      flying = true
      const dLon = ((to.lon - state.lon + 540) % 360) - 180 // the short way round
      const end = distanceForScale(to.scale, size.h, FOV)
      tl = gsap
        .timeline({ onComplete: done })
        .to(
          state,
          {
            lon: state.lon + dLon,
            lat: to.lat,
            duration: 4.4,
            ease: 'power2.inOut',
          },
          0,
        )
        .to(state, { dist: end, duration: 4.6, ease: 'power3.inOut' }, 0.9)
        .to(state, { mark: 1, duration: 0.8, ease: 'power1.out' }, 3.6)
        .to({}, { duration: 0.5 }) // a beat on Nagasaki before the map takes over
    },
    reset() {
      tl?.kill()
      flying = false
      Object.assign(state, {
        lon: START.lon,
        lat: START.lat,
        dist: far,
        mark: 0,
      })
    },
    setActive(on) {
      cancelAnimationFrame(frame)
      if (on) {
        last = performance.now()
        frame = requestAnimationFrame(draw)
      }
    },
    dispose() {
      cancelAnimationFrame(frame)
      tl?.kill()
      scene.traverse((o) => {
        if (o instanceof Mesh || o instanceof Points) {
          o.geometry.dispose()
          const mats = Array.isArray(o.material) ? o.material : [o.material]
          mats.forEach((m) => m.dispose())
        }
      })
      map.dispose()
      renderer.dispose()
      renderer.forceContextLoss()
    },
  }
}
