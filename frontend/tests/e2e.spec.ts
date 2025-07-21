
import { test, expect } from '@playwright/test';

test('should categorize an invoice item successfully', async ({ page }) => {
  // Start from the index page
  await page.goto('http://localhost:3000/');

  // Find the text field and fill it
  await page.getByLabel('Invoice Item Description').fill('A new office desk');

  // Click the categorize button
  await page.getByRole('button', { name: 'Categorize' }).click();

  // Wait for the result and assert
  const result = page.locator('div[role="alert"]:has-text("Category:")');
  await expect(result).toBeVisible();
  await expect(result).toContainText(/Category:/i);
});
