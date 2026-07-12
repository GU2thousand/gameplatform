import { expect, test } from "@playwright/test";
import { TrainingPage } from "./pages/training-page.js";
import { expectNoSeriousA11yViolations } from "./support/accessibility.js";
import {
  IMPROVED_SECOND_ANSWER,
  STRONG_FIRST_ANSWER,
  uniqueAccount
} from "./support/test-data.js";

test.describe.configure({ mode: "serial" });

test("a preset quest survives refresh and two asynchronous attempts can be compared", async ({ page }, testInfo) => {
  test.setTimeout(300_000);
  const account = uniqueAccount("journey");
  const app = new TrainingPage(page);

  await app.goto();
  await app.register(account);
  const challenge = await app.generateFromPreset(app.presetSystemDesign);
  expect(challenge.title).toEqual(expect.any(String));
  const challengeTitle = String(challenge.title ?? "");
  expect(challengeTitle.length).toBeGreaterThan(0);

  await app.answer.fill(STRONG_FIRST_ANSWER);
  await page.reload();
  await expect(app.profileMenu).toBeVisible();
  await expect(app.challenge).toContainText(challengeTitle);
  await expect(app.answer).toHaveValue(STRONG_FIRST_ANSWER);

  const first = await app.submitAndWaitForEvaluation(STRONG_FIRST_ANSWER);
  await expect(app.historyAttempts).toHaveCount(1);
  await expect(app.nextWeakness).toBeVisible();

  const second = await app.submitAndWaitForEvaluation(IMPROVED_SECOND_ANSWER);
  await expect(app.historyAttempts).toHaveCount(2);
  expect(second.queued.submissionId).not.toBe(first.queued.submissionId);

  const comparisonResponse = page.waitForResponse(
    (response) => response.request().method() === "GET" && response.url().includes("/compare")
  );
  await app.selectComparison(first.queued.submissionId, second.queued.submissionId);
  const comparison = await comparisonResponse;
  expect(comparison.status(), await comparison.text()).toBe(200);
  await expect(app.comparison).toContainText(/[+-]?\d/);

  await expect(app.progressOutput).toContainText(/2/);
  await page.reload();
  await expect(app.historyAttempts).toHaveCount(2);
  await expect(app.evaluation).toBeVisible();

  await expectNoSeriousA11yViolations(page, testInfo, {
    artifactName: "axe-evaluated-workspace.json"
  });
});
