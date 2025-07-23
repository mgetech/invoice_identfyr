
import { test, expect } from '@playwright/test';

test('should categorize an invoice item successfully', async ({ page }) => {
  await page.goto('http://localhost:3000/');

  await page.getByLabel('Invoice Item Description').fill('Allgemeine Untersuchung mit Beratung, Hund');

  await page.getByRole('button', { name: 'Categorize' }).click();

  const result = page.locator('div[role="alert"]:has-text("Category:")');
  await expect(result).toBeVisible();
  await expect(result).toContainText(/Category:/i);
});
