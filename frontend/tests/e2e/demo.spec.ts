import { expect, test } from '@playwright/test'
import AxeBuilder from '@axe-core/playwright'

// Core loop from the plan: expired Worker is locked, passes the Check, Panel turns READY
test('passing the Check unlocks the Machine Panel', async ({
  page,
  context,
}) => {
  await page.goto('/')
  await page.getByRole('button', { name: 'Reset demo' }).click()
  // Kits that confirm a destructive action (F-30) open a dialog; confirm it there
  const confirm = page
    .getByRole('dialog')
    .getByRole('button', { name: 'Reset demo' })
  await expect(confirm.or(page.getByText('Demo data reset.'))).toBeVisible()
  if (await confirm.isVisible()) await confirm.click()
  await expect(page.getByText('Demo data reset.')).toBeVisible()

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
  ] as const) {
    await page.goto(path)
    await page.getByLabel('4-digit PIN').fill(pin)
    await page.getByRole('button', { name: 'Open' }).click()
    await expect(
      page.getByRole('button', { name: 'Lock this screen' }),
    ).toBeVisible()
  }
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

// Pitch opener: flyer photo becomes a Bengali notice with confirmed dates; the Worker sees it and can Ask
test('flyer notice reaches the Worker and Ask cites it', async ({ page }) => {
  await page.goto('/')
  await page.getByRole('button', { name: 'Reset demo' }).click()
  await page.goto('/community')
  await page.getByLabel('4-digit PIN').fill('5678')
  await page.getByRole('button', { name: 'Open' }).click()
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
  await expect(page.getByText('Source:')).toBeVisible()
})

// Pitch beat two: a new Note Version expires every Sign-off on that Machine
test('publishing a new version locks the Machine again', async ({ page }) => {
  await page.goto('/')
  await page.getByRole('button', { name: 'Reset demo' }).click()
  await page.goto('/foreman/record/m-plasma')
  await page.getByLabel('4-digit PIN').fill('1234')
  await page.getByRole('button', { name: 'Open' }).click()
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
