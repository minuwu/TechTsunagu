import { expect, test, type Page } from '@playwright/test'
import AxeBuilder from '@axe-core/playwright'

// Unlock a PIN screen. Typing can land before hydration, when React resets the field to empty,
// so retry until the PIN sticks and Open is enabled.
async function unlock(page: Page, path: string, pin: string) {
  await page.goto(path)
  await expect(async () => {
    await page.getByLabel('4-digit PIN').fill(pin)
    await expect(page.getByRole('button', { name: 'Open' })).toBeEnabled({
      timeout: 1000,
    })
  }).toPass()
  await page.getByRole('button', { name: 'Open' }).click()
  await expect(
    page.getByRole('button', { name: 'Lock this screen' }),
  ).toBeVisible()
}

// Reset the demo store: the mock database lives in localStorage (services/mocks/store.ts), so drop it
// and reload, and the next read seeds it again
async function resetDemo(page: Page) {
  await page.goto('/')
  await page.evaluate(() => localStorage.removeItem('wakatta.demo.v1'))
  await page.reload()
}

// Core loop from the plan: expired Worker is locked, passes the Check, Panel turns READY
test('passing the Check unlocks the Machine Panel', async ({
  page,
  context,
}) => {
  await resetDemo(page)

  await page.goto('/m/m-press')
  await page.getByRole('button', { name: 'Tanvir' }).click()
  await page.getByRole('button', { name: 'English' }).click()
  await expect(page.getByText('Expired')).toBeVisible()

  const panel = await context.newPage()
  await panel.goto('/panel/m-press')
  await expect(panel.getByText('LOCKED')).toBeVisible()

  await page
    .getByRole('button', { name: 'I understand. Take the Check' })
    .click()
  await page.getByLabel('Press the emergency stop').check()
  await page.getByLabel('After both hands are away').check()
  await page.getByRole('button', { name: 'Check my answer' }).click()
  await expect(page.getByText('Passed. The Machine is unlocked.')).toBeVisible()

  await expect(panel.getByText('READY')).toBeVisible({ timeout: 5000 })
})

const ROUTES = [
  '/me/w-rahim',
  '/me/w-rahim/life',
  '/panel/m-plasma',
  '/foreman',
  '/foreman/record/m-plasma',
  '/foreman/assign',
  '/foreman/skills',
  '/foreman/heatmap',
  '/foreman/glossary',
  '/foreman/briefing',
  '/foreman/near-misses',
  '/foreman/library/m-plasma',
  '/foreman/qr',
  '/foreman/senpai',
  '/community',
  '/senpai/demo-tanaka-7f3a9c',
]

// Every screen renders a heading, no broken text, no axe violations
test('every demo screen renders cleanly', async ({ page }) => {
  // 16 full-page axe scans in one test: well past the 30 s default
  test.setTimeout(120_000)
  for (const [path, pin] of [
    ['/foreman', '1234'],
    ['/community', '5678'],
  ] as const)
    await unlock(page, path, pin)
  for (const path of ROUTES) {
    await page.goto(path)
    await expect(page.getByRole('heading', { level: 1 }), path).toBeVisible()
    await expect(page.locator('body')).not.toContainText(
      /\bundefined\b|\bNaN\b|\[object Object\]/,
    )
    const { violations } = await new AxeBuilder({ page }).analyze()
    expect(violations, path).toEqual([])
  }
})

// Pitch opener: flyer photo becomes an English notice with confirmed dates; the Worker sees it and can Ask
test('flyer notice reaches the Worker and Ask cites it', async ({ page }) => {
  await resetDemo(page)
  await unlock(page, '/community', '5678')
  await page
    .getByRole('button', { name: 'Use the sample garbage flyer' })
    .click()
  const publish = page.getByRole('button', { name: 'Publish' })
  await expect(publish).toBeDisabled()
  for (const box of await page.getByRole('checkbox').all()) await box.check()
  await expect(publish).toBeEnabled()
  await publish.click()
  await expect(page.getByText('Published. Workers see it now')).toBeVisible()

  await page.goto('/me/w-rahim/life')
  await page.getByLabel('Or type').fill('fan garbage')
  await page.getByRole('button', { name: 'Ask', exact: true }).click()
  // The cited Notice is named under the answer (the answer text itself may mention its source too)
  await expect(
    page.getByText('Source: How to put out garbage', { exact: true }),
  ).toBeVisible()
})

// Pitch beat two: a new Note Version expires every Sign-off on that Machine
test('publishing a new version locks the Machine again', async ({ page }) => {
  await resetDemo(page)
  await unlock(page, '/foreman/record/m-plasma', '1234')
  await page.getByRole('button', { name: 'Use the sample take' }).click()
  await page.getByRole('button', { name: 'Next: confirm' }).click()
  await page.getByRole('button', { name: 'Approve and publish' }).click()
  await expect(page.getByRole('heading', { name: 'Published' })).toBeVisible()

  await page.goto('/m/m-plasma')
  await page.getByRole('button', { name: 'Sadia' }).click()
  await expect(page.getByText('Expired')).toBeVisible()
  await page.goto('/panel/m-plasma')
  await expect(page.getByText('LOCKED')).toBeVisible()
})

// Desktop layout of the Foreman console: no sideways scroll at the 1024px tablet-landscape
// width, and the Foreman nav stays on screen (any kit; checked by role, not by class)
test.describe('tablet landscape', () => {
  test.use({ viewport: { width: 1024, height: 768 } })

  test('Foreman screens fit 1024px', async ({ page }) => {
    // 12 server-rendered pages: past the 30 s default on a slow runner
    test.setTimeout(120_000)
    await unlock(page, '/foreman', '1234')
    for (const path of ROUTES.filter((p) => p.startsWith('/foreman'))) {
      await page.goto(path)
      await expect(page.getByRole('heading', { level: 1 }), path).toBeVisible()
      await expect(
        page.getByRole('navigation', { name: 'Foreman', exact: true }),
        path,
      ).toBeInViewport()
      const overflow = await page.evaluate(
        () => document.documentElement.scrollWidth - window.innerWidth,
      )
      expect(overflow, path).toBeLessThanOrEqual(0)
    }
  })
})
