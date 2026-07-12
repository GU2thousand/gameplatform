import { expect, type Locator, type Page, type Response } from "@playwright/test";
import type { TestAccount } from "../support/test-data.js";

export interface QueuedSubmission {
  submissionId: number;
  status?: string;
  [key: string]: unknown;
}

export interface SubmissionObservation {
  queued: QueuedSubmission;
  postStatus: number;
  pollUrls: string[];
}

export class TrainingPage {
  readonly page: Page;
  readonly languageToggle: Locator;
  readonly authOpen: Locator;
  readonly authRegister: Locator;
  readonly authLogin: Locator;
  readonly authLogout: Locator;
  readonly profileMenu: Locator;
  readonly presetPm: Locator;
  readonly presetSystemDesign: Locator;
  readonly presetApiDesign: Locator;
  readonly generate: Locator;
  readonly challenge: Locator;
  readonly answer: Locator;
  readonly submit: Locator;
  readonly submissionStatus: Locator;
  readonly evaluation: Locator;
  readonly progress: Locator;
  readonly progressOutput: Locator;
  readonly nextWeakness: Locator;
  readonly history: Locator;
  readonly historyAttempts: Locator;
  readonly compareFirst: Locator;
  readonly compareSecond: Locator;
  readonly compare: Locator;
  readonly comparison: Locator;

  constructor(page: Page) {
    this.page = page;
    this.languageToggle = page.getByTestId("language-toggle");
    this.authOpen = page.getByTestId("auth-open");
    this.authRegister = page.getByTestId("auth-register");
    this.authLogin = page.getByTestId("auth-login");
    this.authLogout = page.getByTestId("auth-logout");
    this.profileMenu = page.locator("#profileMenuBtn");
    this.presetPm = page.getByTestId("preset-pm");
    this.presetSystemDesign = page.getByTestId("preset-system-design");
    this.presetApiDesign = page.getByTestId("preset-api-design");
    this.generate = page.getByTestId("generate");
    this.challenge = page.getByTestId("challenge");
    this.answer = page.getByTestId("answer");
    this.submit = page.getByTestId("submit");
    this.submissionStatus = page.getByTestId("submission-status");
    this.evaluation = page.getByTestId("evaluation");
    this.progress = page.getByTestId("progress");
    this.progressOutput = page.locator("#progressOutput");
    this.nextWeakness = page.getByTestId("next-weakness");
    this.history = page.getByTestId("history");
    this.historyAttempts = page.getByTestId("history-attempt");
    this.compareFirst = page.getByTestId("compare-first");
    this.compareSecond = page.getByTestId("compare-second");
    this.compare = page.getByTestId("compare");
    this.comparison = page.locator("#compareOutput");
  }

  async goto(): Promise<void> {
    await this.page.goto("/");
    await expect(this.languageToggle).toBeVisible();
  }

  async register(account: TestAccount): Promise<Response> {
    await this.authOpen.click();
    await expect(this.page.getByRole("dialog")).toBeVisible();
    await this.page.locator("#registerTab").click();
    await this.page.locator("#registerUsername").fill(account.username);
    await this.page.locator("#registerPassword").fill(account.password);
    await this.page.locator("#confirmPassword").fill(account.password);

    const responsePromise = this.page.waitForResponse(
      (response) => response.url().includes("/api/auth/register") && response.request().method() === "POST"
    );
    await this.authRegister.click();
    const response = await responsePromise;
    expect(response.status(), await response.text()).toBe(201);
    await expect(this.profileMenu).toBeVisible();
    await expect(this.presetSystemDesign).toBeVisible();
    return response;
  }

  async login(account: TestAccount): Promise<Response> {
    await this.authOpen.click();
    await expect(this.page.getByRole("dialog")).toBeVisible();
    await this.page.locator("#loginUsername").fill(account.username);
    await this.page.locator("#loginPassword").fill(account.password);

    const responsePromise = this.page.waitForResponse(
      (response) => response.url().includes("/api/auth/login") && response.request().method() === "POST"
    );
    await this.authLogin.click();
    const response = await responsePromise;
    expect(response.status(), await response.text()).toBe(200);
    await expect(this.profileMenu).toBeVisible();
    await expect(this.presetSystemDesign).toBeVisible();
    return response;
  }

  async logout(): Promise<Response> {
    await this.profileMenu.click();
    await expect(this.authLogout).toBeVisible();
    const responsePromise = this.page.waitForResponse(
      (response) => response.url().includes("/api/auth/logout") && response.request().method() === "POST"
    );
    await this.authLogout.click();
    const response = await responsePromise;
    expect(response.status(), await response.text()).toBe(204);
    await expect(this.authOpen).toBeVisible();
    return response;
  }

  async generateFromPreset(preset: Locator = this.presetSystemDesign): Promise<Record<string, unknown>> {
    await preset.click();
    const responsePromise = this.page.waitForResponse(
      (response) => response.url().includes("/api/challenge/generate") && response.request().method() === "POST"
    );
    await this.generate.click();
    const response = await responsePromise;
    expect(response.status(), await response.text()).toBe(200);
    const challenge = (await response.json()) as Record<string, unknown>;
    await expect(this.challenge).toBeVisible();
    if (typeof challenge.title === "string") {
      await expect(this.challenge).toContainText(challenge.title);
    }
    return challenge;
  }

  async submitAndWaitForEvaluation(answer: string): Promise<SubmissionObservation> {
    await this.answer.fill(answer);
    await expect(this.submit).toBeEnabled();
    const pollUrls: string[] = [];
    const observePolls = (request: { method(): string; url(): string }): void => {
      const path = new URL(request.url()).pathname;
      if (request.method() === "GET" && /^\/api\/submissions?\/\d+(?:\/status)?$/.test(path)) {
        pollUrls.push(request.url());
      }
    };
    this.page.on("request", observePolls);

    try {
      const responsePromise = this.page.waitForResponse(
        (response) => response.url().match(/\/api\/submissions?\/?$/) !== null && response.request().method() === "POST"
      );
      await this.submit.click();
      const response = await responsePromise;
      expect(response.status(), await response.text()).toBe(202);
      const queued = (await response.json()) as QueuedSubmission;
      expect(queued.submissionId).toEqual(expect.any(Number));
      const idempotencyKey = response.request().headers()["idempotency-key"];
      expect(idempotencyKey, "Every submission must carry an idempotency key").toBeTruthy();
      if (typeof queued.idempotencyKey === "string") {
        expect(queued.idempotencyKey).toBe(idempotencyKey);
      }

      await expect
        .poll(() => pollUrls.length, {
          message: "The browser should poll the queued submission status endpoint",
          timeout: 30_000
        })
        .toBeGreaterThan(0);
      await expect(this.evaluation).toBeVisible({ timeout: 120_000 });
      await expect(this.evaluation).toContainText(/\d/);
      await expect(this.progressOutput).toContainText(/XP|attempt|尝试/i);

      return { queued, postStatus: response.status(), pollUrls: [...pollUrls] };
    } finally {
      this.page.off("request", observePolls);
    }
  }

  async selectComparison(firstSubmissionId: number, secondSubmissionId: number): Promise<void> {
    await this.compareFirst.selectOption(String(firstSubmissionId));
    await this.compareSecond.selectOption(String(secondSubmissionId));
    await expect(this.compare).toBeEnabled();
    await this.compare.click();
  }
}
