[English](#english) | [简体中文](#简体中文)

<a id="english"></a>

# English

# AI Gamified Career Training Platform

A PM and software engineering interview practice app: generate a brief, save a draft, submit an answer, and review rubric feedback and progress. The default `local` mode uses deterministic templates and heuristic scoring. It does **not** call an AI model. Optional LangChain4j/OpenAI integration requires an API key.

<img width="1209" height="926" alt="Career practice dashboard" src="https://github.com/user-attachments/assets/1242cc46-d777-45ef-aaad-84ccfa45ec7a" />

## Current behavior

- Custom briefs are assembled from your role, exercise type, goal, context, and requirements without adding unrelated game scenarios.
- A private browser session owns its challenges, saved drafts, evaluations, and progress.
- Refreshing restores the current challenge and saved answer; recent history shows up to 20 challenges.
- Repeating the same normalized answer returns its existing evaluation. Improved answers earn only the increase over that challenge's best score; the solved count counts distinct challenges.
- The default H2 database is stored at `./data/gamingplatform` and survives application restarts.

## Run locally

Java 17+ is required. From the repository directory:

```bash
./mvnw test
./mvnw spring-boot:run
```

Open [the app](http://localhost:8080/). [Health](http://localhost:8080/actuator/health) and [active AI mode](http://localhost:8080/api/debug/ai-mode) are available separately.

The H2 console is disabled by default. Set `H2_CONSOLE_ENABLED=true` only when you intentionally need it for local development. Keep the `data` directory to retain local records; stop the app before copying H2 database files for a backup.

## Session identity and privacy

There is no registration or password login. `POST /api/session` creates or resumes an anonymous browser identity using a cryptographically random 256-bit opaque cookie. The cookie is `HttpOnly` and `SameSite=Strict`; only its SHA-256 hash is stored in the database. Private endpoints derive the user from this cookie rather than trusting a supplied user ID.

Keep the browser cookie: clearing it, switching browsers, or letting it expire creates a new identity, and old private records cannot be recovered through the app. Existing demo records without an owner remain unassigned and inaccessible. This session model does not provide cross-device accounts or account recovery.

Direct HTTPS requests mark cookies `Secure` automatically. Defaults are `APP_SESSION_COOKIE_SECURE=false` and `SERVER_FORWARD_HEADERS_STRATEGY=none` for direct local HTTP. For a public HTTPS reverse proxy, first restrict backend access to that trusted proxy and configure it to overwrite forwarding headers with the public scheme, host, and port. Then set `APP_SESSION_COOKIE_SECURE=true` and `SERVER_FORWARD_HEADERS_STRATEGY=native`. Secure cookies alone do not change the scheme or port used by the same-origin check; browser writes need correctly trusted forwarded headers. Docker Compose passes both variables from its environment or a repository-root `.env` copied from `.env.example`. Use PostgreSQL for public production.

## API quick start

State-changing requests require `Content-Type: application/json` and `X-Requested-With: career-platform`. Retain and resend the session cookie. The browser frontend handles these automatically.

```bash
curl -c cookies.txt -b cookies.txt http://localhost:8080/api/session \
  -H 'Content-Type: application/json' \
  -H 'X-Requested-With: career-platform' -d '{}'

curl -c cookies.txt -b cookies.txt http://localhost:8080/api/challenge/generate \
  -H 'Content-Type: application/json' \
  -H 'X-Requested-With: career-platform' \
  -d '{"difficulty":"INTERMEDIATE","roleTrack":"PM + SDE","challengeType":"API Design","focusGoal":"latency reduction","businessContext":"A fintech app has slow balance lookups.","customRequirements":["Include rollout metrics."],"customConstraints":["Stay under 150ms p95."],"customAcceptanceCriteria":["Explain how success is measured."]}'

curl -b cookies.txt http://localhost:8080/api/challenge/current
curl -b cookies.txt http://localhost:8080/api/user/me/progress
curl -b cookies.txt http://localhost:8080/api/user/me/history
```

Use the generated challenge ID in draft and submission requests; see the [API reference](AI_Gamified_Career_Training_Platform_README.md). Treat `cookies.txt` as a credential and keep it out of source control.

Input limits: role and challenge type 120 characters each, focus goal 300, business context 1,000; each custom list allows up to 12 items of up to 400 characters each.

## PostgreSQL and optional AI

Start local PostgreSQL (Docker required):

```bash
docker compose up -d postgres
SPRING_PROFILES_ACTIVE=postgres \
SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5433/gamingplatform' \
SPRING_DATASOURCE_USERNAME='gamingplatform' \
SPRING_DATASOURCE_PASSWORD='gamingplatform' \
./mvnw spring-boot:run
```

The full Docker stack runs with `docker compose up --build`. PostgreSQL data is held in the named `postgres_data` volume; deleting that volume deletes its data.

To use an actual model, add `langchain4j` to the active profiles and configure `OPENAI_API_KEY` in your environment. Optional settings include `OPENAI_MODEL` and `OPENAI_BASE_URL`. Never commit API keys. A configured provider name is not proof that a real model request succeeded; no live AI validation is claimed without a working key and an actual generation/evaluation request.

Each application process allows up to 4 concurrent AI operations by default. Model requests default to a 60-second timeout and 2 retries; submission transactions have a 240-second timeout. Rate limits are also per process, so public deployments with multiple instances need shared rate and concurrency controls at the reverse proxy or gateway.

Built with Java 17, Spring Boot 3, JPA, H2/PostgreSQL, and optional LangChain4j/OpenAI. See [architecture, API, and deployment details](AI_Gamified_Career_Training_Platform_README.md), [.env.example](.env.example), and [docker-compose.yml](docker-compose.yml).

---

<a id="简体中文"></a>

# 简体中文

# AI Gamified Career Training Platform

面向产品经理和软件工程师的面试练习工具：生成题目、保存草稿、提交答案，并查看分项反馈和进度。默认 `local` 模式使用确定性模板出题与启发式评分，**不会调用 AI 模型**。可选的 LangChain4j/OpenAI 模式需要 API Key。

## 当前功能

- 自定义题目根据岗位、练习类型、目标、业务背景和要求生成，不再混入无关的游戏业务场景。
- 每个浏览器会话拥有独立的题目、草稿、评分和进度。
- 刷新后恢复当前题目和已保存的答案；历史记录最多显示最近 20 道题。
- 规范化后相同的答案复用已有评分；同题改进答案只奖励超过该题历史最高分的部分；完成数量按不同题目计算。
- 默认 H2 数据库保存在 `./data/gamingplatform`，应用重启后保留数据。

## 本地启动

需要 Java 17+。在仓库目录运行：

```bash
./mvnw test
./mvnw spring-boot:run
```

打开 [应用首页](http://localhost:8080/)。另有 [健康检查](http://localhost:8080/actuator/health) 和 [当前 AI 模式](http://localhost:8080/api/debug/ai-mode)。

H2 控制台默认关闭，仅在确实需要本地调试时设置 `H2_CONSOLE_ENABLED=true`。保留 `data` 目录才能保留本地数据；备份 H2 数据库文件前先停止应用。

## 会话身份与隐私

目前没有注册和密码登录。`POST /api/session` 通过密码学随机生成的 256 位不透明 Cookie 创建或恢复匿名浏览器身份。Cookie 使用 `HttpOnly` 和 `SameSite=Strict`，数据库仅保存其 SHA-256 哈希。私有接口根据 Cookie 识别用户，不信任请求传入的用户 ID。

请保留浏览器 Cookie：清除 Cookie、更换浏览器或 Cookie 过期后会创建新身份，应用不支持找回旧身份下的私有记录。已有无归属的演示数据保持未分配且无法访问。目前不提供跨设备账号和账号恢复。

直接 HTTPS 请求会自动使用 `Secure` Cookie。本地直接 HTTP 默认配置为 `APP_SESSION_COOKIE_SECURE=false` 和 `SERVER_FORWARD_HEADERS_STRATEGY=none`。公网 HTTPS 反向代理部署时，先限制后端仅允许可信代理访问，并让代理覆盖转发请求头，写入公网协议、主机名和端口，再设置 `APP_SESSION_COOKIE_SECURE=true` 和 `SERVER_FORWARD_HEADERS_STRATEGY=native`。仅启用安全 Cookie 不会改变同源检查使用的协议与端口；浏览器写入还需要正确解析可信的转发头。Docker Compose 会传入这两个环境变量，也可在仓库根目录从 `.env.example` 复制的 `.env` 中设置。公网生产环境请使用 PostgreSQL。

## 接口使用

所有写入请求都需要 `Content-Type: application/json` 与 `X-Requested-With: career-platform`，并保存、回传会话 Cookie。网页会自动处理这些设置。

```bash
curl -c cookies.txt -b cookies.txt http://localhost:8080/api/session \
  -H 'Content-Type: application/json' \
  -H 'X-Requested-With: career-platform' -d '{}'

curl -c cookies.txt -b cookies.txt http://localhost:8080/api/challenge/generate \
  -H 'Content-Type: application/json' \
  -H 'X-Requested-With: career-platform' \
  -d '{"difficulty":"INTERMEDIATE","roleTrack":"PM + SDE","challengeType":"API Design","focusGoal":"降低延迟","businessContext":"金融应用的余额查询缓慢。"}'

curl -b cookies.txt http://localhost:8080/api/challenge/current
curl -b cookies.txt http://localhost:8080/api/user/me/progress
curl -b cookies.txt http://localhost:8080/api/user/me/history
```

保存草稿和提交答案时使用生成结果中的题目 ID，详见 [接口文档](AI_Gamified_Career_Training_Platform_README.md)。`cookies.txt` 相当于身份凭据，请勿提交到源码仓库。

输入限制：岗位、练习类型各 120 字符；目标 300 字符；业务背景 1,000 字符；每组自定义列表最多 12 项，每项最多 400 字符。

## PostgreSQL 与可选 AI

本地 PostgreSQL 需要 Docker：

```bash
docker compose up -d postgres
SPRING_PROFILES_ACTIVE=postgres \
SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5433/gamingplatform' \
SPRING_DATASOURCE_USERNAME='gamingplatform' \
SPRING_DATASOURCE_PASSWORD='gamingplatform' \
./mvnw spring-boot:run
```

也可以通过 `docker compose up --build` 启动完整应用。数据库使用 `postgres_data` 命名卷；删除此卷会删除数据。

如需真实模型，在启用的 profile 中加入 `langchain4j`，并在环境变量中配置 `OPENAI_API_KEY`。可选设置包括 `OPENAI_MODEL` 和 `OPENAI_BASE_URL`。不要提交 API Key。仅显示 provider 名称不能证明模型调用成功；没有可用 Key 和真实出题、评分请求时，不声称已验证真实 AI。

每个应用进程默认最多同时执行 4 个 AI 操作。模型请求默认超时 60 秒、最多重试 2 次；提交事务超时为 240 秒。限流也按进程计算，因此多实例公网部署需要在反向代理或网关配置共享的请求频率和并发控制。

技术栈包括 Java 17、Spring Boot 3、JPA、H2/PostgreSQL，以及可选 LangChain4j/OpenAI。更多信息见 [架构、接口与部署说明](AI_Gamified_Career_Training_Platform_README.md)、[.env.example](.env.example) 和 [docker-compose.yml](docker-compose.yml)。
