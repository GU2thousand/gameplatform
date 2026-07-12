import { expect, type APIResponse, type Page } from "@playwright/test";

export interface CsrfOptions {
  endpoint?: string;
  cookieNames?: string[];
  headerName?: string;
}

export interface PollOptions<T> {
  timeout?: number;
  intervals?: number[];
  headers?: Record<string, string>;
  isComplete: (payload: T, response: APIResponse) => boolean;
}

const DEFAULT_CSRF_COOKIES = ["XSRF-TOKEN", "CSRF-TOKEN", "csrf-token"];

function tokenFromPayload(payload: unknown): string | undefined {
  if (!payload || typeof payload !== "object") {
    return undefined;
  }

  const record = payload as Record<string, unknown>;
  for (const key of ["token", "csrfToken", "_csrf"]) {
    if (typeof record[key] === "string" && record[key]) {
      return record[key];
    }
  }
  return undefined;
}

/**
 * Initializes CSRF state when the application exposes a bootstrap endpoint and
 * returns the matching mutation header. A 404/405 is treated as "CSRF bootstrap
 * not required" so the same helper works with cookie-only and disabled setups.
 */
export async function csrfHeaders(
  page: Page,
  options: CsrfOptions = {}
): Promise<Record<string, string>> {
  const endpoint = options.endpoint ?? process.env.E2E_CSRF_ENDPOINT ?? "/api/auth/csrf";
  const cookieNames = options.cookieNames ?? DEFAULT_CSRF_COOKIES;
  const headerName = options.headerName ?? process.env.E2E_CSRF_HEADER ?? "X-XSRF-TOKEN";
  let payloadToken: string | undefined;

  const response = await page.request.get(endpoint, { failOnStatusCode: false });
  if (response.ok()) {
    const contentType = response.headers()["content-type"] ?? "";
    if (contentType.includes("application/json")) {
      payloadToken = tokenFromPayload(await response.json());
    }
  } else if (![404, 405].includes(response.status())) {
    throw new Error(`CSRF bootstrap failed: GET ${endpoint} returned ${response.status()}.`);
  }

  const cookies = await page.context().cookies();
  const cookie = cookies.find((candidate) => cookieNames.includes(candidate.name));
  const token = cookie ? decodeURIComponent(cookie.value) : payloadToken;
  return token ? { [headerName]: token } : {};
}

export async function csrfMutation(
  page: Page,
  method: "POST" | "PUT" | "PATCH" | "DELETE",
  url: string,
  data?: Record<string, unknown> | string,
  options: CsrfOptions = {}
): Promise<APIResponse> {
  const headers = await csrfHeaders(page, options);
  return page.request.fetch(url, {
    method,
    data,
    headers,
    failOnStatusCode: false
  });
}

/** Polls a JSON endpoint and returns the first payload accepted by isComplete. */
export async function pollJson<T>(
  page: Page,
  url: string | (() => string),
  options: PollOptions<T>
): Promise<T> {
  let latest: T | undefined;
  let latestStatus = 0;
  let latestBody = "";

  try {
    await expect
      .poll(
        async () => {
          const endpoint = typeof url === "function" ? url() : url;
          const response = await page.request.get(endpoint, {
            headers: options.headers,
            failOnStatusCode: false
          });
          latestStatus = response.status();

          if (!response.ok()) {
            latestBody = (await response.text()).slice(0, 500);
            return false;
          }

          latest = (await response.json()) as T;
          return options.isComplete(latest, response);
        },
        {
          message: `Expected ${typeof url === "string" ? url : "the API endpoint"} to complete`,
          timeout: options.timeout ?? 120_000,
          intervals: options.intervals ?? [250, 500, 1_000, 2_000, 3_000]
        }
      )
      .toBe(true);
  } catch (error) {
    throw new Error(
      `API polling timed out; last HTTP status was ${latestStatus}` +
        `${latestBody ? ` (${latestBody})` : ""}.`,
      { cause: error }
    );
  }

  if (latest === undefined) {
    throw new Error(
      `Polling completed without a JSON payload; last HTTP status was ${latestStatus}` +
        `${latestBody ? ` (${latestBody})` : ""}.`
    );
  }
  return latest;
}
