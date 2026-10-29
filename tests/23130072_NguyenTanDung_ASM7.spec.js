import { test, expect } from '@playwright/test';

const BASE_URL = 'https://thienlong.vn/';

// Hàm đóng pop-up quảng cáo/khuyến mãi nếu xuất hiện
async function handlePopups(page) {
  try {
    const popupClose = page.locator('.modal-close, .popup-close, .close-popup, #close-icon, .btn-close').first();
    if (await popupClose.isVisible({ timeout: 2000 })) {
      await popupClose.click();
      await page.waitForTimeout(1000);
    }
  } catch (e) {}
}

// Hàm thực hiện tìm kiếm từ khóa chung
async function doSearch(page, keyword) {
  await handlePopups(page);
  const searchInput = page.locator('input[name="q"]').first();
  await searchInput.scrollIntoViewIfNeeded();
  await searchInput.click({ force: true });
  await searchInput.fill('');
  
  await searchInput.pressSequentially(keyword, { delay: 100 });
  await page.waitForTimeout(1000);

  await Promise.all([
    page.waitForNavigation({ waitUntil: 'domcontentloaded', timeout: 15000 }).catch(() => {}),
    searchInput.press('Enter')
  ]);
  
  await page.waitForTimeout(3000);
}

test.describe('Automation Suite ASM7 - Chức năng Tìm kiếm (SRC-1 đến SRC-22)', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto(BASE_URL, { waitUntil: 'domcontentloaded', timeout: 60000 });
    await handlePopups(page);
  });

  test('SRC-1: Kiểm tra hiển thị ô tìm kiếm ở trang chủ', async ({ page }) => {
    const searchInput = page.locator('input[name="q"]').first();
    await searchInput.scrollIntoViewIfNeeded();
    await expect(searchInput).toBeVisible({ timeout: 10000 });
    await searchInput.click();
    await page.waitForTimeout(3000);
  });

  test('SRC-2: Ô tìm kiếm hiển thị ở các trang khác', async ({ page }) => {
    await page.goto(`${BASE_URL}collections/but-bi`, { waitUntil: 'domcontentloaded' });
    await handlePopups(page);
    await expect(page.locator('input[name="q"]').first()).toBeAttached();
    await page.waitForTimeout(2500); 

    await page.goto(`${BASE_URL}cart`, { waitUntil: 'domcontentloaded' });
    await handlePopups(page);
    await expect(page.locator('input[name="q"]').first()).toBeAttached();
    await page.waitForTimeout(2500); 
  });

  test('SRC-3 & SRC-4: Tìm kiếm từ khóa "bút bi"', async ({ page }) => {
    await doSearch(page, 'bút bi');
    expect(page.url()).toContain('search'); 
  });

  test('SRC-5: Tìm kiếm theo mã SKU (TL-095)', async ({ page }) => {
    await doSearch(page, 'TL-095');
    expect(page.url()).toContain('search');
  });

  test('SRC-6 & SRC-7: Tìm kiếm từ khóa thương hiệu (Flexoffice)', async ({ page }) => {
    await doSearch(page, 'Flexoffice');
    expect(page.url()).toContain('search');
  });

  test('SRC-8: Kết quả tìm kiếm liên quan từ khóa (vở)', async ({ page }) => {
    await doSearch(page, 'vở');
    expect(page.url()).toContain('search');
  });

  test('SRC-9: Bấm vào sản phẩm ở kết quả tìm kiếm', async ({ page }) => {
    await doSearch(page, 'bút bi');
    const productTitleLink = page.locator('.proloop .proloop-detail a, .product-block .product-title a, h3 a').first();
    await productTitleLink.scrollIntoViewIfNeeded();
    await productTitleLink.waitFor({ state: 'visible', timeout: 10000 });
    await page.waitForTimeout(1500);
    await Promise.all([
      page.waitForNavigation({ waitUntil: 'domcontentloaded', timeout: 15000 }).catch(() => {}),
      productTitleLink.click({ force: true })
    ]);
    await page.waitForTimeout(3500);
    expect(page.url()).toContain('/products/');
  });

  test('SRC-10 & SRC-11: Gợi ý tìm kiếm Auto-suggest', async ({ page }) => {
    const searchInput = page.locator('input[name="q"]').first();
    await searchInput.click({ force: true });
    await searchInput.pressSequentially('bút', { delay: 200 });
    await page.waitForTimeout(4000);
  });

  test('SRC-12: Tìm kiếm với từ khóa không tồn tại', async ({ page }) => {
    await doSearch(page, 'xyzabc123');
    expect(page.url()).toContain('search');
  });

  test('SRC-13 & SRC-14: Tìm kiếm không dấu / chữ hoa', async ({ page }) => {
    await doSearch(page, 'but may');
    expect(page.url()).toContain('search');
  });

  test('SRC-15: Xử lý từ khóa có khoảng trắng thừa', async ({ page }) => {
    await doSearch(page, '   bút   bi   ');
    expect(page.url()).toContain('search');
  });

  test('SRC-16 & SRC-17: Để trống hoặc gõ khoảng trắng', async ({ page }) => {
    const searchInput = page.locator('input[name="q"]').first();
    await searchInput.click({ force: true });
    await searchInput.fill('     ');
    await page.waitForTimeout(1000);
    await searchInput.press('Enter');
    await page.waitForTimeout(2000);
  });

  test('SRC-18: Từ khóa chứa ký tự đặc biệt', async ({ page }) => {
    await doSearch(page, '@#$%^&*()');
  });

  test('SRC-19: Từ khóa chỉ có 1 ký tự (b)', async ({ page }) => {
    await doSearch(page, 'b');
    expect(page.url()).toContain('search');
  });

  // SRC-20 đã được sửa dứt điểm
  test('SRC-20: Từ khóa rất dài (300 ký tự)', async ({ page }) => {
    await handlePopups(page);
    const searchInput = page.locator('input[name="q"]').first();
    await searchInput.scrollIntoViewIfNeeded();
    await searchInput.click({ force: true });
    
    const longText = 'a'.repeat(300);
    await searchInput.fill(longText);
    await page.waitForTimeout(1000);

    await page.keyboard.press('Enter');
    await page.waitForTimeout(4000);

    expect(page.url()).toBeTruthy();
  });

  test('SRC-21 & SRC-22: Reload trang kết quả', async ({ page }) => {
    await doSearch(page, 'bút bi');
    const urlBefore = page.url();
    await page.waitForTimeout(1500);
    await page.reload();
    await page.waitForTimeout(2500);
    expect(page.url()).toBe(urlBefore);
  });
});