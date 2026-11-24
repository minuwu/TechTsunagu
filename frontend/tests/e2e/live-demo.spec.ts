import { expect, test } from '@playwright/test'
import AxeBuilder from '@axe-core/playwright'

// The story prototype (public/prototype) opens from the nav and fills the window under the header
test('the nav opens the story demo filling the window under the header', async ({
  page,
}) => {
  await page.setViewportSize({ width: 1440, height: 900 })
  await page.goto('/')
  await page
    .getByRole('navigation', { name: 'Main menu' })
    .getByRole('link', { name: 'Play the Story' })
    .click()
  await expect(page).toHaveURL(/\/demo$/)
  await expect(
    page.getByRole('heading', {
      level: 1,
      name: 'One instruction, two phones',
    }),
  ).toBeAttached()

  const stage = page.getByTitle('Wakatta? demo: Foreman and Worker phones')
  await expect(stage).toBeVisible()
  // 80px: the header
  expect(await stage.boundingBox()).toEqual({
    x: 0,
    y: 80,
    width: 1440,
    height: 820,
  })
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

  // The prototype's ?app= switch once went straight into location.replace: a javascript: URL ran on our origin
  test('the prototype ignores a script URL in ?app=', async ({ page }) => {
    let ran = false
    page.on('dialog', async (d) => {
      ran = true
      await d.dismiss()
    })
    await page.goto('/prototype/index.html?app=javascript:alert(1)//')
    // The phones still load the bundled app.html
    await expect(page.locator('iframe#foreman')).toHaveAttribute(
      'src',
      /^app\.html\?role=foreman/,
    )
    expect(ran).toBe(false)
  })

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
