import type { ReactNode } from 'react'

/** One equipment drawing for the Inspect door, drawn in a 100 x 100 box. */
export interface InspectArt {
  /** The main silhouette. Pulled DOWN in the exploded view. */
  body: ReactNode
  /** A separable part (cap, handle, cover, lid). Pulled UP in the exploded view. */
  cap: ReactNode
  /** Defect marks (cracks, frays, rust, missing parts); shown only on a defective sample. */
  bad: ReactNode
}
