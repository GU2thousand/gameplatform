import { expect, test } from "@playwright/test";
import { TrainingPage } from "./pages/training-page.js";
import { expectNoSeriousA11yViolations } from "./support/accessibility.js";
import { expectNoHorizontalOverflow } from "./support/layout.js";
import { uniqueAccount } from "./support/test-data.js";

test.describe.configure({ mode: "serial" });

test("language switching, client validation, failure feedback, mobile layout, and accessibility remain usable", async ({ page }, testInfo) => {
  const account = uniqueAccount("quality");
  const app = new TrainingPage(page);

  await app.goto();
  await expectNoSeriousA11yViolations(page, testInfo, {
    artifactName: "axe-signed-out.json"
  });
  await expectNoHorizontalOverflow(page, testInfo, 390, 844, "overflow-signed-out.json");

  const initialLanguage = await page.locator("html").getAttribute("lang");
  await app.languageToggle.click();
  await expect
    .poll(() => page.locator("html").getAttribute("lang"))
    .not.toBe(initialLanguage);
  const switchedLanguage = await page.locator("html").getAttribute("lang");
  expect(switchedLanguage).toMatch(/^(en|zh)/i);
  await app.languageToggle.click();
  await expect(page.locator("html")).toHaveAttribute("lang", initialLanguage ?? "zh-CN");

  await app.register(account);
  await expectNoSeriousA11yViolations(page, testInfo, {
    artifactName: "axe-workspace.json"
  });
  await expectNoHorizontalOverflow(page, testInfo, 390, 844, "overflow-workspace.json");

  await app.generateFromPreset(app.presetApiDesign);
  await app.answer.fill("Too short");

  let submissionRequests = 0;
  const countSubmission = (request: { method(): string; url(): string }): void => {
    const path = new URL(request.url()).pathname;
    if (request.method() === "POST" && /^\/api\/submissions?\/?$/.test(path)) {
      submissionRequests += 1;
    }
  };
  page.on("request", countSubmission);
  if (await app.submit.isEnabled()) {
    await app.submit.click();
    await expect(app.submissionStatus).toContainText(/30|short|minimum|字符|至少/i);
  } else {
    await expect(app.submit).toBeDisabled();
    await expect(page.getByText(/30|minimum|字符|至少/i).first()).toBeVisible();
  }
  await page.waitForTimeout(200);
  page.off("request", countSubmission);
  expect(submissionRequests, "A short answer must not reach the submission API").toBe(0);

  await page.route("**/api/challenge/generate", async (route) => {
    await route.fulfill({
      status: 503,
      contentType: "application/json",
      body: JSON.stringify({ status: 503, message: "QA injected temporary failure" })
    });
  }, { times: 1 });
  await app.presetPm.click();
  await app.generate.click();
  await expect(page.getByRole("alert").last()).toContainText(/could not|try again|temporary|暂时|重试/i);

  await expectNoHorizontalOverflow(page, testInfo, 390, 844, "overflow-generated-quest.json");
});
