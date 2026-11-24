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

// Demo states (mock data): an empty shop says so instead of loading forever; a failed read shows the
// caution band with a retry, and that state still passes axe
test('demo states show the empty message and the connection band', async ({
  page,
}) => {
  await page.goto('/?demo=empty')
  const demo = page.getByRole('complementary', { name: 'Demo state' })
  await expect(demo.getByRole('button', { name: 'Empty' })).toHaveAttribute(
    'aria-pressed',
    'true',
  )
  await expect(page.getByText('Nothing here yet.')).toHaveCount(2)

  await demo.getByRole('button', { name: 'Error' }).click()
  const band = page.getByRole('alert').filter({ hasText: "Can't reach" })
  await expect(band).toBeVisible()
  await expect(band.getByRole('button', { name: 'Try again' })).toBeVisible()
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

// The interactive Japan map (v3 kit, so asked for with ?ui=v3 whatever the default kit is): its buttons move
// it to Nagasaki (with the public-domain photo) and back
test('the Japan map goes to Nagasaki and back to all of Japan', async ({
  page,
}) => {
  await page.goto('/?ui=v3')
  await expect(page.getByRole('img', { name: /Map of Japan/ })).toBeVisible()

  await page.getByRole('button', { name: 'Nagasaki', exact: true }).click()
  await expect(page.getByText('pilot site: Sasebo')).toBeVisible()
  await expect(page.getByRole('img', { name: /Megane Bridge/ })).toBeVisible()

  const all = page.getByRole('button', { name: 'All Japan' })
  await all.click()
  await expect(all).toHaveAttribute('aria-pressed', 'true')
  await expect(page.getByRole('img', { name: /Megane Bridge/ })).toBeHidden()
})

// "Journey to Nagasaki" on the v4 landing. Reduced motion (the default in this config) goes straight to the
// map with Nagasaki chosen; the panel follows the choice from the list and the next button.
test('the journey map opens on Nagasaki and the panel follows the chosen prefecture', async ({
  page,
}) => {
  await page.goto('/?ui=v4')
  const journey = page.getByRole('region', {
    name: 'From anywhere on Earth to Nagasaki.',
  })
  await journey.scrollIntoViewIfNeeded()
  await expect(
    journey.getByRole('img', { name: /Map of Japan's 47 prefectures/ }),
  ).toBeVisible()
  await expect(journey.getByRole('heading', { name: '長崎県' })).toBeVisible()
  await expect(
    journey.getByRole('heading', { name: 'Megane Bridge' }),
  ).toBeVisible()

  await journey.getByLabel('Choose a prefecture').selectOption('JP-26')
  await expect(journey.getByRole('heading', { name: '京都府' })).toBeVisible()
  await expect(
    journey.getByRole('img', { name: 'Fushimi Inari-taisha' }),
  ).toBeVisible()
  await expect(journey.getByText(/Each torii is a gift/)).toBeVisible()

  await journey.getByRole('button', { name: 'Next: Ōsaka' }).click()
  await expect(journey.getByRole('heading', { name: '大阪府' })).toBeVisible()
})

test.describe('journey with motion on', () => {
  test.use({ reducedMotion: 'no-preference' })

  test('the Earth shows first and Skip goes to the map', async ({ page }) => {
    await page.goto('/?ui=v4')
    const journey = page.getByRole('region', {
      name: 'From anywhere on Earth to Nagasaki.',
    })
    await journey.scrollIntoViewIfNeeded()
    await expect(
      journey.getByRole('img', { name: /The Earth turning toward Japan/ }),
    ).toBeVisible()
    await journey.getByRole('button', { name: 'Skip to the map' }).click()
    await expect(journey.locator('.journey-stage')).toHaveAttribute(
      'data-phase',
      'map',
    )
    await expect(journey.getByRole('heading', { name: '長崎県' })).toBeVisible()
  })
})
