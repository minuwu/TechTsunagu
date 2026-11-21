import { defineConfig } from '@hey-api/openapi-ts'

// Types only: src/services/api.ts owns fetching and the USE_MOCK toggle (S-12, S-13)
export default defineConfig({
  input: '../docs/openapi.yaml',
  output: 'src/services/generated',
  plugins: ['@hey-api/typescript'],
})
