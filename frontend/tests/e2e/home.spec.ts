import { expect, test, type Page } from '@playwright/test'
import AxeBuilder from '@axe-core/playwright'

// Text that must never reach a judge's screen
async function expectNoBrokenText(page: Page) {
  await expect(page.locator('body')).not.toContainText(
    /\bundefined\b|\bNaN\b|\[object Object\]|[Ll]orem ipsum/,
  )
}

test('home renders in English by default and passes axe', async ({ page }) => {
  await page.goto('/')
  await expect(page.locator('html')).toHaveAttribute('lang', 'en')
  await expect(
    page.getByRole('heading', {
      level: 1,
      name: 'Understood at work. Understood at home.',
    }),
  ).toBeVisible()
  await expectNoBrokenText(page)
  const { violations } = await new AxeBuilder({ page }).analyze()
  expect(violations).toEqual([])
})

test.describe('Japanese browser', () => {
  test.use({ locale: 'ja-JP' })

  test('gets Japanese with no click and passes axe', async ({ page }) => {
    await page.goto('/')
    await expect(page.locator('html')).toHaveAttribute('lang', 'ja')
    await expect(
      page.getByRole('heading', {
        level: 1,
        name: '職場でも、くらしでも、わかった。',
      }),
    ).toBeVisible()
    await expectNoBrokenText(page)
    const { violations } = await new AxeBuilder({ page }).analyze()
    expect(violations).toEqual([])
  })
})

test('the language toggle switches to Japanese', async ({ page }) => {
  await page.goto('/')
  await page.getByRole('button', { name: '日本語' }).click()
  await expect(page.locator('html')).toHaveAttribute('lang', 'ja')
  await expect(
    page.getByRole('heading', {
      level: 1,
      name: '職場でも、くらしでも、わかった。',
    }),
  ).toBeVisible()
})

test.describe('phone', () => {
  test.use({ viewport: { width: 375, height: 667 } })

  test('fits a 375px screen without sideways scroll', async ({ page }) => {
    await page.goto('/')
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
    const overflow = await page.evaluate(
      () => document.documentElement.scrollWidth - window.innerWidth,
    )
    expect(overflow).toBeLessThanOrEqual(0)
  })
})

test.describe('unknown URL in a Japanese browser', () => {
  test.use({ locale: 'ja-JP' })

  test('shows the kit 404 screen in Japanese', async ({ page }) => {
    await page.goto('/no-such-page')
    await expect(
      page.getByRole('heading', { level: 1, name: 'ページが見つかりません' }),
    ).toBeVisible()
    await expect(page.getByRole('link', { name: 'ホームに戻る' })).toBeVisible()
  })
})

// Landing motion (GSAP reveals, Lenis wheel scroll): with motion on, nothing may stay hidden after scrolling
test.describe('landing with motion on', () => {
  test.use({ reducedMotion: 'no-preference' })

  test('every revealed block ends fully visible', async ({ page }) => {
    await page.goto('/')
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
    for (let i = 0; i < 14; i++) {
      await page.mouse.wheel(0, 700)
      await page.waitForTimeout(120)
    }
    await expect
      .poll(
        () =>
          page.$$eval(
            '[data-reveal], [data-hero]',
            (els) =>
              els.filter((el) => getComputedStyle(el).opacity !== '1').length,
          ),
        { timeout: 8000 },
      )
      .toBe(0)
  })
})
