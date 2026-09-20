[English](#english) | [简体中文](#简体中文)

<a id="english"></a>

# English

# Architecture, API, and deployment

## Runtime architecture

The static frontend calls a Spring Boot REST API. Services generate briefs, evaluate submissions, calculate progress, and persist records through JPA. The default provider is `local`: deterministic challenge templates and heuristic evaluation. Custom templates use the supplied scenario, role, exercise type, goals, and custom lists. They do not infer specialist facts or provide genuine model-generated reasoning. Optional `langchain4j` mode calls an OpenAI-compatible model.

The database holds users, challenges, saved drafts, submissions, and evaluations. Challenges belong to a user; the session cookie determines the current user on every private request. Default file-backed H2 storage at `./data/gamingplatform` survives a process restart. A browser refresh restores the latest challenge, its draft, and its evaluation. History returns the latest 20 challenges owned by the current user.

## Browser session contract

`POST /api/session` with `{}` returns the current user, creating one if necessary. A new session has a random 256-bit opaque cookie with `HttpOnly` and `SameSite=Strict`. The database stores a SHA-256 hash of the token, not the raw token. No password or registration is required. Possession of the cookie grants access to that browser identity's records.

The session is anonymous and has no recovery flow. Clearing or losing the cookie, changing browsers, or expiration means a new identity; the app cannot restore old private data to it. Backing up the database alone does not recover a lost browser token. Legacy demo records with no owner remain unassigned and inaccessible.

All state-changing API requests require both headers:

```http
Content-Type: application/json
X-Requested-With: career-platform
```

Private requests must include the session cookie. A supplied `userId` is not an identity credential. Cross-user challenge access is rejected. The old arbitrary-user creation/progress API is replaced by session establishment and `/api/user/me/...` routes.

Direct HTTPS requests mark cookies `Secure` automatically. Direct local HTTP uses `APP_SESSION_COOKIE_SECURE=false` and `SERVER_FORWARD_HEADERS_STRATEGY=none` by default. An HTTPS reverse proxy requires both correct cookie handling and a correctly resolved public origin: setting the secure-cookie flag alone does not change the request scheme or port used by the same-origin check.

Enable `SERVER_FORWARD_HEADERS_STRATEGY=native` only after restricting backend network access to a trusted proxy that overwrites client-supplied forwarding headers with the public scheme, host, and port. Set `APP_SESSION_COOKIE_SECURE=true` as well. Browser write requests require those trusted forwarded values to match their public `Origin`; otherwise the app rejects them. Do not accept arbitrary client forwarding headers through a directly exposed backend.

## API reference

| Method and path | Request / behavior |
| --- | --- |
| `POST /api/session` | `{}`; create or resume the anonymous browser user |
| `POST /api/challenge/generate` | Generate and persist a challenge for the session user |
| `GET /api/challenge/current` | Restore current challenge state |
| `GET /api/challenge/{id}` | Restore an owned challenge's state |
| `PUT /api/challenge/{id}/draft` | `{"answer":"..."}`; save the answer draft, including an empty string to clear it |
| `POST /api/submission` | `{"challengeId":123,"answer":"..."}`; evaluate an owned challenge |
| `GET /api/user/me/progress` | Current user's XP, distinct completed count, scores, tier, and recommendations |
| `GET /api/user/me/history` | Latest 20 owned states as `[{challenge,draftAnswer,result}]` |
| `GET /api/debug/ai-mode` | Active provider and implementation names |
| `GET /actuator/health` | Application health |

Challenge state bundles the brief, saved answer, and evaluation. `result` is absent or null before evaluation. Use the ID returned by generation instead of assuming a fixed challenge ID.

Generation accepts:

| Field | Limits |
| --- | --- |
| `difficulty` | `BEGINNER`, `INTERMEDIATE`, or `ADVANCED`; default `INTERMEDIATE` |
| `roleTrack` | Up to 120 characters |
| `challengeType` | Up to 120 characters |
| `focusGoal` | Up to 300 characters |
| `businessContext` | Up to 1,000 characters |
| `customRequirements` | Up to 12 items, each up to 400 characters |
| `customConstraints` | Up to 12 items, each up to 400 characters |
| `customAcceptanceCriteria` | Up to 12 items, each up to 400 characters |

Custom fields are optional. An uncustomized request uses the built-in difficulty bank. Generated titles, contexts, list items, and output-format labels fit the database limits of 255, 2,000, 500, and 64 characters respectively.

### Example session

```bash
curl -c cookies.txt -b cookies.txt http://localhost:8080/api/session \
  -H 'Content-Type: application/json' \
  -H 'X-Requested-With: career-platform' -d '{}'

curl -b cookies.txt http://localhost:8080/api/challenge/generate \
  -H 'Content-Type: application/json' \
  -H 'X-Requested-With: career-platform' \
  -d '{"difficulty":"INTERMEDIATE","roleTrack":"PM","challengeType":"PRD","businessContext":"A dental clinic needs an appointment booking workflow.","focusGoal":"Reduce appointment no-shows","customConstraints":["Do not store clinical records."]}'
```

Replace `123` below with the generated ID:

```bash
curl -X PUT -b cookies.txt http://localhost:8080/api/challenge/123/draft \
  -H 'Content-Type: application/json' \
  -H 'X-Requested-With: career-platform' \
  -d '{"answer":"# Draft\nDescribe the appointment workflow and rescheduling policy."}'

curl -b cookies.txt http://localhost:8080/api/submission \
  -H 'Content-Type: application/json' \
  -H 'X-Requested-With: career-platform' \
  -d '{"challengeId":123,"answer":"# Proposal\nDescribe requirements, decisions, edge cases, and validation metrics."}'

curl -b cookies.txt http://localhost:8080/api/user/me/progress
curl -b cookies.txt http://localhost:8080/api/user/me/history
```

`cookies.txt` is a credential: do not share it or commit it.

## Scoring and repeat submissions

Five rubric dimensions contribute to the weighted final score: requirement understanding, logical clarity, technical feasibility, edge-case coverage, and communication structure. Salary titles are simulated practice tiers, not employment or salary predictions.

| Score | Tier |
| --- | --- |
| `0 <= score < 60` | Intern |
| `60 <= score < 75` | Junior Engineer |
| `75 <= score < 85` | Mid-Level |
| `85 <= score < 95` | Senior |
| `95 <= score` | Staff |

Repeated submissions of the same normalized answer are idempotent: they reuse the stored evaluation and do not earn XP again. Different answers to the same challenge can earn XP only for improvement over that challenge's best score. Completed challenges count distinct solved challenges, not submission attempts. The progress endpoint remains the authority for total XP.

## Local operation and persistence

```bash
./mvnw test
./mvnw spring-boot:run
```

Requires Java 17+. Default URL: [localhost:8080](http://localhost:8080). The H2 console is disabled unless `H2_CONSOLE_ENABLED=true`. Database files are relative to the working directory; start from a consistent directory to reopen the same database. Stop the app before copying H2 files for backup. Do not run multiple instances against the same H2 file.

For a local PostgreSQL instance, `docker compose up -d postgres` exposes port `5433`. The repository's default credentials are development values. The full stack is available through `docker compose up --build`, with persistent database data in the `postgres_data` volume.

## Public deployment

The repository includes a Dockerfile and can be hosted on a service such as Render. A repository configuration does not prove that a public deployment is live. Use `/actuator/health` as the health check path.

Use a managed PostgreSQL database for public production, HTTPS, a secure session cookie, and persistent database backups. When HTTPS terminates at a reverse proxy, first restrict the backend to the trusted proxy and make the proxy overwrite forwarding headers. Only then set:

```text
SPRING_PROFILES_ACTIVE=postgres
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/<database>
SPRING_DATASOURCE_USERNAME=<database-user>
SPRING_DATASOURCE_PASSWORD=<database-password>
APP_SESSION_COOKIE_SECURE=true
SERVER_FORWARD_HEADERS_STRATEGY=native
```

Docker Compose passes `APP_SESSION_COOKIE_SECURE` and `SERVER_FORWARD_HEADERS_STRATEGY` to the app container. Copy `.env.example` to `.env` in the repository root and set the production values there, or supply them through the deployment environment. Defaults are `false` and `none`; a `.env` file is read by Compose, not automatically by `./mvnw spring-boot:run`. Keep the database credentials in the hosting provider's secret configuration. Do not rely on a container's temporary filesystem for persistent H2 data. Test restart persistence and a full browser generate/save/submit/refresh flow at the actual public URL after deployment.

To enable the real model provider, use `SPRING_PROFILES_ACTIVE=postgres,langchain4j` and supply `OPENAI_API_KEY`. Optional settings include `OPENAI_MODEL`, `OPENAI_BASE_URL`, `OPENAI_TIMEOUT_SECONDS`, and `OPENAI_MAX_RETRIES`. A missing key is not a validated AI setup. Automated/local-template checks do not verify real model quality or provider availability; those require actual calls with working credentials.

## Capacity and time bounds

The default AI concurrency limit is 4 operations per application process. Model requests use a 60-second timeout and up to 2 retries by default (`OPENAI_TIMEOUT_SECONDS` and `OPENAI_MAX_RETRIES`). Submission transactions have a 240-second timeout. Configure provider retries and request timeouts within that transaction budget; the transaction timeout is not a guarantee that every upstream network operation is immediately cancelled.

Request rate limits and AI concurrency counters are held per process. Restarting a process resets its counters, and adding instances multiplies their independent allowances. Public multi-instance deployments need shared rate and concurrency controls at the reverse proxy or API gateway, along with appropriate provider usage limits. Forwarded client addresses must come only from the trusted proxy described above.

---

<a id="简体中文"></a>

# 简体中文

# 架构、接口与部署

## 运行架构

静态网页调用 Spring Boot REST API。服务负责出题、评分、计算进度，并通过 JPA 保存数据。默认 `local` 模式使用确定性模板和启发式评分；自定义模板依据业务背景、岗位、练习类型、目标及列表生成，不会推断专业事实，也不是模型生成的推理。可选 `langchain4j` 模式可调用兼容 OpenAI 的模型。

数据库保存用户、题目、草稿、提交和评分。每道题都有所属用户，私有请求均通过会话 Cookie 识别当前用户。默认 H2 文件数据库位于 `./data/gamingplatform`，进程重启后保留数据。刷新页面后可恢复最新题目、草稿和评分；历史接口返回当前用户最近 20 道题。

## 浏览器会话

向 `POST /api/session` 发送 `{}` 可创建或恢复当前匿名用户。新会话使用密码学随机生成的 256 位不透明 Cookie，带有 `HttpOnly` 和 `SameSite=Strict`。数据库仅保存令牌的 SHA-256 哈希，不保存原始令牌。无需密码和注册；持有 Cookie 即可访问该浏览器身份的数据。

匿名会话不提供恢复功能。清除或丢失 Cookie、更换浏览器、Cookie 过期后都会建立新身份，无法通过应用恢复旧身份的私有数据。仅备份数据库也无法恢复丢失的浏览器令牌。原有无归属的演示数据保持未分配且无法访问。

所有写入接口都需要以下请求头：

```http
Content-Type: application/json
X-Requested-With: career-platform
```

私有请求还必须携带会话 Cookie。传入 `userId` 不能用作身份凭据；不能访问其他用户的题目。旧的任意用户创建、按用户 ID 查询进度接口已由会话接口及 `/api/user/me/...` 替代。

直接 HTTPS 请求会自动使用 `Secure` Cookie。本地直接 HTTP 默认使用 `APP_SESSION_COOKIE_SECURE=false` 和 `SERVER_FORWARD_HEADERS_STRATEGY=none`。HTTPS 反向代理部署同时需要安全 Cookie 与正确解析的公网来源：仅开启 Cookie 的安全标记，不会改变同源检查使用的请求协议或端口。

仅在限制后端只能由可信代理访问，并让代理用公网协议、主机名和端口覆盖客户端传入的转发头之后，才启用 `SERVER_FORWARD_HEADERS_STRATEGY=native`，并设置 `APP_SESSION_COOKIE_SECURE=true`。浏览器写入需要解析后的来源与公网 `Origin` 一致，否则会被拒绝。不要让直接暴露的后端接受任意客户端伪造的转发头。

## 接口参考

| 方法与路径 | 请求及行为 |
| --- | --- |
| `POST /api/session` | `{}`；创建或恢复匿名浏览器用户 |
| `POST /api/challenge/generate` | 为当前用户生成并保存题目 |
| `GET /api/challenge/current` | 恢复当前题目的状态 |
| `GET /api/challenge/{id}` | 恢复属于当前用户的指定题目 |
| `PUT /api/challenge/{id}/draft` | `{"answer":"..."}`；保存草稿，可传空字符串清空 |
| `POST /api/submission` | `{"challengeId":123,"answer":"..."}`；为自己的题目提交答案并评分 |
| `GET /api/user/me/progress` | 当前用户的 XP、不同已完成题目数、分数、等级和建议 |
| `GET /api/user/me/history` | 最近 20 道题的状态，格式为 `[{challenge,draftAnswer,result}]` |
| `GET /api/debug/ai-mode` | 当前 provider 和客户端实现 |
| `GET /actuator/health` | 应用健康状态 |

题目状态包含题面、已保存答案和评分；评分前 `result` 为空或不存在。请使用生成结果返回的 ID，不要假设题目 ID 固定。

出题输入可包含：

| 字段 | 限制 |
| --- | --- |
| `difficulty` | `BEGINNER`、`INTERMEDIATE`、`ADVANCED`；默认 `INTERMEDIATE` |
| `roleTrack` | 最多 120 字符 |
| `challengeType` | 最多 120 字符 |
| `focusGoal` | 最多 300 字符 |
| `businessContext` | 最多 1,000 字符 |
| `customRequirements` | 最多 12 项，每项最多 400 字符 |
| `customConstraints` | 最多 12 项，每项最多 400 字符 |
| `customAcceptanceCriteria` | 最多 12 项，每项最多 400 字符 |

自定义字段均可选。不含自定义字段时，使用内置难度题库。生成结果的标题、背景、单条列表内容和输出格式分别遵守 255、2,000、500、64 字符的数据库限制。完整 curl 示例见本文英文部分；`cookies.txt` 相当于身份凭据，不要分享或提交到仓库。

## 评分与重复提交

最终得分综合需求理解、逻辑清晰度、技术可行性、边界情况覆盖、沟通结构五个维度。薪资称号只是练习等级，不代表就业或真实薪资预测。

| 分数 | 等级 |
| --- | --- |
| `0 <= score < 60` | Intern |
| `60 <= score < 75` | Junior Engineer |
| `75 <= score < 85` | Mid-Level |
| `85 <= score < 95` | Senior |
| `95 <= score` | Staff |

规范化后相同的答案重复提交具有幂等性：复用已有评分，不再增加 XP。同题提交不同答案时，仅对超过该题历史最高分的部分增加 XP。完成数量按不同的已完成题目统计，而不是提交次数；累计 XP 以进度接口为准。

## 本地运行与数据保存

```bash
./mvnw test
./mvnw spring-boot:run
```

需要 Java 17+。默认地址为 [localhost:8080](http://localhost:8080)。H2 控制台默认关闭，仅在 `H2_CONSOLE_ENABLED=true` 时开启。数据库路径相对于工作目录，请始终在同一目录启动以读取同一份数据。复制 H2 数据库文件备份前先停机，不要让多个实例同时使用同一 H2 文件。

`docker compose up -d postgres` 在本地 `5433` 端口启动 PostgreSQL，仓库中的默认凭据仅用于开发。`docker compose up --build` 可启动完整应用；数据库数据存放在 `postgres_data` 命名卷。

## 公网部署

仓库包含 Dockerfile，可部署到 Render 等服务。存在部署配置不代表已有可用的公网部署。健康检查路径为 `/actuator/health`。

公网生产环境应使用托管 PostgreSQL、HTTPS、安全 Cookie 和持久数据库备份。若 HTTPS 在反向代理终止，先限制后端仅允许可信代理访问，并让代理覆盖转发头，再配置：

```text
SPRING_PROFILES_ACTIVE=postgres
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/<database>
SPRING_DATASOURCE_USERNAME=<database-user>
SPRING_DATASOURCE_PASSWORD=<database-password>
APP_SESSION_COOKIE_SECURE=true
SERVER_FORWARD_HEADERS_STRATEGY=native
```

Docker Compose 会向应用容器传入 `APP_SESSION_COOKIE_SECURE` 和 `SERVER_FORWARD_HEADERS_STRATEGY`。可在仓库根目录将 `.env.example` 复制为 `.env` 后设置生产值，也可直接使用部署环境变量；默认分别为 `false` 和 `none`。`.env` 由 Compose 读取，`./mvnw spring-boot:run` 不会自动读取它。数据库凭据应保存在托管平台的密钥设置中。不要依靠容器临时文件系统保留 H2 数据。部署完成后，应在真实公网地址验证重启持久性，以及浏览器出题、保存、提交、刷新完整流程。

启用真实模型需要 `SPRING_PROFILES_ACTIVE=postgres,langchain4j` 和 `OPENAI_API_KEY`。可选配置包括 `OPENAI_MODEL`、`OPENAI_BASE_URL`、`OPENAI_TIMEOUT_SECONDS`、`OPENAI_MAX_RETRIES`。缺少 Key 不能视为已验证 AI；自动测试和本地模板检查无法确认真实模型质量与服务可用性，需使用有效凭据执行实际请求。

## 并发容量与超时

每个应用进程默认最多同时执行 4 个 AI 操作。模型请求默认超时 60 秒、最多重试 2 次，分别由 `OPENAI_TIMEOUT_SECONDS` 与 `OPENAI_MAX_RETRIES` 控制。提交事务的超时为 240 秒；调整模型超时和重试次数时应保持在事务时间预算内。事务超时并不保证上游网络请求会立刻取消。

请求限流与 AI 并发计数均保存在单个进程中。重启会重置计数，多实例会各自拥有独立额度。公网多实例部署需要在反向代理或 API 网关配置共享的请求频率、并发控制，并设置合适的模型服务用量限制。转发的客户端地址也只能来自前文所述的可信代理。
