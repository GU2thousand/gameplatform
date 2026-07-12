import { expect, test } from "@playwright/test";
import { TrainingPage } from "./pages/training-page.js";
import { uniqueAccount } from "./support/test-data.js";

test.describe.configure({ mode: "serial" });

test("a new user can register, log out, log back in, and keep the session after refresh", async ({ page }) => {
  const account = uniqueAccount("auth");
  const app = new TrainingPage(page);

  await app.goto();
  await app.register(account);

  const meAfterRegistration = await page.request.get("/api/auth/me", { failOnStatusCode: false });
  expect(meAfterRegistration.status()).toBe(200);
  await expect(meAfterRegistration.json()).resolves.toMatchObject({ username: account.username });

  await app.logout();
  const meAfterLogout = await page.request.get("/api/auth/me", { failOnStatusCode: false });
  expect(meAfterLogout.status()).toBe(401);

  await app.login(account);
  await page.reload();
  await expect(app.profileMenu).toBeVisible();
  await expect(app.presetSystemDesign).toBeVisible();
});
