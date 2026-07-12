import AxeBuilder from "@axe-core/playwright";
import { expect, type Page, type TestInfo } from "@playwright/test";

type AxeImpact = "minor" | "moderate" | "serious" | "critical" | null;

export interface AccessibilityOptions {
  include?: string | string[];
  exclude?: string | string[];
  impacts?: AxeImpact[];
  artifactName?: string;
}

export async function expectNoSeriousA11yViolations(
  page: Page,
  testInfo: TestInfo,
  options: AccessibilityOptions = {}
): Promise<void> {
  let builder = new AxeBuilder({ page });

  const includes = typeof options.include === "string" ? [options.include] : options.include ?? [];
  const excludes = typeof options.exclude === "string" ? [options.exclude] : options.exclude ?? [];
  for (const selector of includes) {
    builder = builder.include(selector);
  }
  for (const selector of excludes) {
    builder = builder.exclude(selector);
  }

  const results = await builder.analyze();
  const relevantImpacts = options.impacts ?? ["serious", "critical"];
  const violations = results.violations.filter((violation) =>
    relevantImpacts.includes(violation.impact as AxeImpact)
  );

  await testInfo.attach(options.artifactName ?? "axe-results.json", {
    body: Buffer.from(JSON.stringify(results, null, 2)),
    contentType: "application/json"
  });

  const summary = violations
    .map((violation) => {
      const nodes = violation.nodes
        .map((node) => `  - ${node.target.join(" ")}: ${node.failureSummary ?? node.html}`)
        .join("\n");
      return `[${violation.impact}] ${violation.id}: ${violation.help}\n${nodes}`;
    })
    .join("\n\n");

  expect(violations, summary || "No serious or critical accessibility violations expected.").toEqual([]);
}
