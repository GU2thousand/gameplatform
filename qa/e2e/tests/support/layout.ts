import { expect, type Page, type TestInfo } from "@playwright/test";

interface OverflowReport {
  viewportWidth: number;
  documentWidth: number;
  offenders: Array<{
    element: string;
    left: number;
    right: number;
    width: number;
  }>;
}

export async function expectNoHorizontalOverflow(
  page: Page,
  testInfo: TestInfo,
  width = 390,
  height = 844,
  artifactName = "mobile-overflow.json"
): Promise<void> {
  await page.setViewportSize({ width, height });
  await page.waitForTimeout(100);

  const report = await page.evaluate<OverflowReport>(() => {
    const viewportWidth = document.documentElement.clientWidth;
    const documentWidth = Math.max(
      document.documentElement.scrollWidth,
      document.body?.scrollWidth ?? 0
    );

    const offenders = Array.from(document.querySelectorAll<HTMLElement>("body *"))
      .filter((element) => {
        const style = window.getComputedStyle(element);
        if (style.display === "none" || style.visibility === "hidden" || style.position === "fixed") {
          return false;
        }
        const rect = element.getBoundingClientRect();
        return rect.width > 0 && (rect.left < -1 || rect.right > viewportWidth + 1);
      })
      .slice(0, 20)
      .map((element) => {
        const rect = element.getBoundingClientRect();
        const identity = [
          element.tagName.toLowerCase(),
          element.id ? `#${element.id}` : "",
          ...Array.from(element.classList).slice(0, 3).map((name) => `.${name}`)
        ].join("");
        return {
          element: identity,
          left: Math.round(rect.left),
          right: Math.round(rect.right),
          width: Math.round(rect.width)
        };
      });

    return { viewportWidth, documentWidth, offenders };
  });

  await testInfo.attach(artifactName, {
    body: Buffer.from(JSON.stringify(report, null, 2)),
    contentType: "application/json"
  });

  expect(
    report.documentWidth,
    `The page is ${report.documentWidth}px wide in a ${report.viewportWidth}px viewport. ` +
      `Overflowing elements: ${JSON.stringify(report.offenders)}`
  ).toBeLessThanOrEqual(report.viewportWidth + 1);
}
