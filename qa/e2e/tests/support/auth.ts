import { expect, type Page } from "@playwright/test";
import { csrfMutation } from "./api.js";
import type { TestAccount } from "./test-data.js";

export interface AuthenticatedUser {
  id: number;
  username: string;
  xp: number;
}

async function responseDetails(response: Awaited<ReturnType<Page["request"]["post"]>>): Promise<string> {
  const text = await response.text();
  return `${response.status()} ${response.statusText()}${text ? `: ${text.slice(0, 500)}` : ""}`;
}

export async function registerViaApi(page: Page, account: TestAccount): Promise<AuthenticatedUser> {
  const response = await csrfMutation(page, "POST", "/api/auth/register", {
    username: account.username,
    password: account.password
  });
  expect(response.ok(), `Registration failed with ${await responseDetails(response)}`).toBe(true);
  return (await response.json()) as AuthenticatedUser;
}

export async function loginViaApi(page: Page, account: TestAccount): Promise<AuthenticatedUser> {
  const response = await csrfMutation(page, "POST", "/api/auth/login", {
    username: account.username,
    password: account.password
  });
  expect(response.ok(), `Login failed with ${await responseDetails(response)}`).toBe(true);
  return (await response.json()) as AuthenticatedUser;
}

export async function logoutViaApi(page: Page): Promise<void> {
  const response = await csrfMutation(page, "POST", "/api/auth/logout");
  expect(
    response.status(),
    `Logout failed with ${await responseDetails(response)}`
  ).toBe(204);
}
