// @ts-check
import js from '@eslint/js'
import tseslint from 'typescript-eslint'
import reactHooks from 'eslint-plugin-react-hooks'
import jsxA11y from 'eslint-plugin-jsx-a11y'
import boundaries from 'eslint-plugin-boundaries'
import i18next from 'eslint-plugin-i18next'

const sameVersion = { version: '{{from.element.captured.version}}' }
const core = ['contract', 'feature', 'domain', 'lib', 'i18n', 'theme']

export default tseslint.config(
  {
    ignores: [
      '.output/**',
      '.nitro/**',
      '.tanstack/**',
      'src/i18n/paraglide/**',
      'src/services/generated/**',
      'src/routeTree.gen.ts',
      'playwright-report/**',
      'test-results/**',
    ],
  },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  reactHooks.configs.flat['recommended-latest'],
  jsxA11y.flatConfigs.recommended,

  // G-09 / S-xx bans. Each message names the fix (G-14).
  {
    files: ['src/**/*.{ts,tsx}'],
    rules: {
      'no-restricted-syntax': [
        'error',
        {
          selector: "JSXAttribute[name.name='dangerouslySetInnerHTML']",
          message:
            'dangerouslySetInnerHTML is banned (G-09). Render text or a sanitised component.',
        },
        {
          selector: "NewExpression[callee.name='QueryClient']",
          message:
            'Create the QueryClient only in src/router.tsx getRouter(), once per request (S-17).',
        },
        {
          selector:
            "ImportDeclaration[source.value='zustand'] ImportSpecifier[imported.name='create']",
          message:
            "Module-level Zustand stores leak between SSR requests. Use createStore from 'zustand/vanilla' + context (S-20).",
        },
      ],
      'no-restricted-globals': [
        'error',
        {
          name: 'fetch',
          message:
            'Only src/services/ talks to the network (G-12). Add a function to src/services/api.ts.',
        },
      ],
      'no-restricted-properties': [
        'error',
        {
          object: 'window',
          property: 'location',
          message:
            'Read the URL from the router (useLocation, validateSearch) so SSR works (S-28).',
        },
        {
          object: 'window',
          property: 'confirm',
          message: 'Use an in-page modal, never window.confirm (F-30).',
        },
      ],
    },
  },
  {
    files: ['src/router.tsx', 'src/**/*.test.{ts,tsx}'], // tests build their own client per render
    rules: { 'no-restricted-syntax': 'off' },
  },
  {
    files: ['src/services/**/*.ts'],
    rules: { 'no-restricted-globals': 'off' },
  },

  // F-01: no hard-coded UI text in kits
  {
    files: ['src/ui/**/*.tsx'],
    plugins: { i18next },
    rules: {
      'i18next/no-literal-string': ['error', { mode: 'jsx-text-only' }],
    },
  },

  // A-09 import directions (docs/FRONTEND_ARCHITECTURE.md §3), plus routes/, router.tsx and server.ts as shell (S-08)
  {
    files: ['src/**/*.{ts,tsx}'],
    plugins: { boundaries },
    settings: {
      'import/resolver': { typescript: { alwaysTryTypes: true } },
      'boundaries/include': ['src/**/*'],
      'boundaries/elements': [
        {
          type: 'screen',
          pattern: 'src/ui/*/screens/*',
          capture: ['version', 'screen'],
        },
        { type: 'ui', pattern: 'src/ui/*', capture: ['version'] },
        { type: 'feature', pattern: 'src/features/*', capture: ['feature'] },
        { type: 'shell', pattern: 'src/shell' },
        { type: 'shell', pattern: 'src/routes' },
        { type: 'contract', pattern: 'src/contract' },
        { type: 'services', pattern: 'src/services' },
        { type: 'domain', pattern: 'src/domain' },
        { type: 'lib', pattern: 'src/lib' },
        { type: 'i18n', pattern: 'src/i18n' },
        { type: 'theme', pattern: 'src/theme' },
      ],
      // Root files Start requires in src/ belong to shell too (S-08)
      'boundaries/files': [
        {
          pattern: 'src/{router.tsx,server.ts,routeTree.gen.ts}',
          category: 'shell',
        },
      ],
    },
    rules: {
      ...boundaries.configs.recommended.rules,
      'boundaries/dependencies': [
        2,
        {
          default: 'disallow',
          policies: [
            { allow: { dependency: { relationship: { to: 'internal' } } } },
            {
              from: { element: { type: 'shell' } },
              allow: {
                to: {
                  element: { types: ['shell', 'ui', ...core, 'services'] },
                },
              },
            },
            {
              from: { file: { categories: 'shell' } },
              allow: {
                to: {
                  element: { types: ['shell', 'ui', ...core, 'services'] },
                },
              },
            },
            {
              from: { file: { categories: 'shell' } },
              allow: { to: { file: { categories: 'shell' } } },
            },
            {
              from: { element: { types: ['ui', 'screen'] } },
              allow: { to: { element: { types: core } } },
            },
            {
              from: { element: { types: ['ui', 'screen'] } },
              allow: { to: { element: { type: 'ui', captured: sameVersion } } },
            },
            {
              from: { element: { type: 'ui' } },
              allow: {
                to: { element: { type: 'screen', captured: sameVersion } },
              },
            },
            {
              from: { element: { type: 'feature' } },
              allow: {
                to: {
                  element: { types: ['services', 'domain', 'lib', 'i18n'] },
                },
              },
            },
            {
              from: { element: { type: 'services' } },
              allow: { to: { element: { type: 'domain' } } },
            },
          ],
        },
      ],
    },
  },
)
