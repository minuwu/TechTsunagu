import { expect, test } from '@playwright/test'
import AxeBuilder from '@axe-core/playwright'

// The two-phone prototype (public/prototype) opens from the nav inside a 16:9 stage
test('the nav opens the live demo with both phones in a 16:9 stage', async ({
  page,
}) => {
  await page.setViewportSize({ width: 1440, height: 900 })
  await page.goto('/')
  await page
    .getByRole('navigation', { name: 'Main menu' })
    .getByRole('link', { name: 'Live demo' })
    .click()
  await expect(page).toHaveURL(/\/demo$/)
  await expect(
    page.getByRole('heading', {
      level: 1,
      name: 'One instruction, two phones',
    }),
  ).toBeVisible()

  const stage = page.getByTitle('Wakatta? demo: Foreman and Worker phones')
  await expect(stage).toBeVisible()
  const box = await stage.boundingBox()
  expect(box ? box.width / box.height : 0).toBeCloseTo(16 / 9, 1)
  // Both phones load: the prototype falls back to one phone below 900px, so this proves the stage is wide enough
  const proto = page.frameLocator('iframe[title^="Wakatta? demo"]')
  await expect(proto.locator('iframe#foreman')).toBeVisible()
  await expect(proto.locator('iframe#worker')).toBeVisible()

  // The page around the stage only: the prototype is its own document, and walking its nested frames
  // outlasts the test timeout
  const { violations } = await new AxeBuilder({ page })
    .options({ iframes: false })
    .exclude('iframe')
    .analyze()
  expect(violations).toEqual([])
})

test.describe('phone', () => {
  test.use({ viewport: { width: 375, height: 667 } })

  test('asks for a wider screen instead of loading the phones', async ({
    page,
  }) => {
    await page.goto('/demo')
    await expect(page.getByText('Open this page on a computer')).toBeVisible()
    await expect(page.locator('iframe')).toHaveCount(0)
    const overflow = await page.evaluate(
      () => document.documentElement.scrollWidth - window.innerWidth,
    )
    expect(overflow).toBeLessThanOrEqual(0)
  })
})
