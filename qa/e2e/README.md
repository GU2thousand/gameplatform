# Gaming Platform browser QA

This package is intentionally independent from the application build and root Node files.

## Run

Start the application separately, then run:

```bash
cd qa/e2e
npm ci
npm run install:browsers
npm run typecheck
npm test
```

The default target is `http://127.0.0.1:8080`. Override it without changing files:

```bash
E2E_BASE_URL=https://example.test npm test
```

If the deployment uses a non-default CSRF bootstrap endpoint or header:

```bash
E2E_CSRF_ENDPOINT=/csrf E2E_CSRF_HEADER=X-CSRF-TOKEN npm test
```

The suite uses one worker and non-parallel test groups because account, challenge, and attempt state is shared deliberately within each user journey. Failed tests keep their screenshot, video, trace, Axe results, and mobile overflow diagnostics under `test-results/`.
