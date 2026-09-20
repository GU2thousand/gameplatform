const state = {
  ready: false,
  busy: false,
  challenge: null,
  savedAnswer: "",
  savePromise: null,
  saveTimer: null
};

const byId = (id) => document.getElementById(id);
const output = { challenge: byId("challengeOutput"), submission: byId("submissionOutput") };
const fields = Object.fromEntries([
  "difficulty", "roleTrack", "challengeType", "focusGoal", "businessContext",
  "customRequirements", "submissionAnswer"
].map((id) => [id, byId(id)]));
const buttons = { generate: byId("generateBtn"), submit: byId("submitBtn"), save: byId("saveBtn"), refresh: byId("refreshBtn") };

function setStatus(message, error = false) {
  const status = byId("appStatus");
  status.textContent = message;
  status.classList.toggle("status--error", error);
}

function setSaveStatus(message, error = false) {
  const status = byId("saveStatus");
  status.textContent = message;
  status.classList.toggle("status--error", error);
}

function isDirty() {
  return Boolean(state.challenge) && fields.submissionAnswer.value !== state.savedAnswer;
}

function updateControls() {
  buttons.generate.disabled = !state.ready || state.busy;
  buttons.submit.disabled = !state.ready || state.busy || !state.challenge;
  buttons.save.disabled = !state.ready || state.busy || Boolean(state.savePromise) || !isDirty();
  buttons.refresh.disabled = !state.ready || state.busy;
  Object.values(fields).forEach((field) => { field.disabled = !state.ready || state.busy; });
  fields.submissionAnswer.disabled = !state.ready || state.busy || !state.challenge;
  document.querySelectorAll(".history-button").forEach((button) => {
    button.disabled = !state.ready || state.busy;
    if (button.dataset.challengeId === String(state.challenge?.id)) button.setAttribute("aria-current", "true");
    else button.removeAttribute("aria-current");
  });
  byId("answerCount").textContent = `${fields.submissionAnswer.value.length.toLocaleString()} / 10,000 characters`;
  byId("workspace").setAttribute("aria-busy", String(state.busy));
}

async function parseError(response) {
  const fallback = response.status === 429 ? "Too many requests. Please wait a minute and try again." : `Request failed (${response.status}). Please try again.`;
  try {
    if (!(response.headers.get("content-type") || "").includes("application/json")) return fallback;
    const data = await response.json();
    return typeof data.message === "string" ? data.message : typeof data.error === "string" ? data.error : fallback;
  } catch (_) { return fallback; }
}

async function requestJson(url, options = {}) {
  const method = options.method || "GET";
  const mutation = !["GET", "HEAD"].includes(method);
  const response = await fetch(url, {
    ...options,
    method,
    credentials: "same-origin",
    cache: "no-store",
    headers: {
      Accept: "application/json",
      ...(mutation ? { "Content-Type": "application/json", "X-Requested-With": "career-platform" } : {}),
      ...options.headers
    }
  });
  if (!response.ok) {
    const error = new Error(await parseError(response));
    error.status = response.status;
    throw error;
  }
  return response.status === 204 ? null : response.json();
}

function splitLines(value) {
  return value.split("\n").map((line) => line.trim()).filter(Boolean);
}

function validateGeneration(payload) {
  for (const [key, label, limit] of [
    ["roleTrack", "Track", 120],
    ["challengeType", "Challenge type", 120],
    ["focusGoal", "Focus goal", 300],
    ["businessContext", "Business context", 1000]
  ]) {
    if ((payload[key] || "").length > limit) {
      throw new Error(`${label} must be ${limit.toLocaleString()} characters or fewer. Please shorten it before generating a quest.`);
    }
  }
  for (const [key, label] of [
    ["customRequirements", "Custom requirements"],
    ["customConstraints", "Custom constraints"],
    ["customAcceptanceCriteria", "Custom acceptance criteria"]
  ]) {
    if (payload[key].length > 12) {
      throw new Error(`${label} can contain up to 12 non-empty lines. Please combine or remove some lines.`);
    }
    const longLine = payload[key].findIndex((line) => line.length > 400);
    if (longLine !== -1) {
      throw new Error(`${label}: line ${longLine + 1} is too long. Each line must be 400 characters or fewer.`);
    }
  }
}

function labelize(key) {
  return String(key).toLowerCase().split("_").map((part) => part.charAt(0).toUpperCase() + part.slice(1)).join(" ");
}

function dateLabel(value) {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "" : date.toLocaleString();
}

function textElement(tag, text, className) {
  const element = document.createElement(tag);
  element.textContent = String(text ?? "");
  if (className) element.className = className;
  return element;
}

// Deliberately small Markdown subset: paragraphs, headings, flat ordered/bullet
// lists, fenced code, inline code, and simple bold/italic emphasis. Everything
// is built with DOM nodes and textContent; raw HTML, links, and images remain
// literal text. No external parser, HTML injection, or navigable URLs are used.
function appendInline(parent, source) {
  const text = String(source ?? "");
  const tokens = /`([^`\n]+)`|\*\*([^*\n]+)\*\*|__([^_\n]+)__|\*([^*\n]+)\*|_([^_\n]+)_/g;
  let position = 0;
  for (const match of text.matchAll(tokens)) {
    parent.append(text.slice(position, match.index));
    const tag = match[1] !== undefined ? "code" : match[2] !== undefined || match[3] !== undefined ? "strong" : "em";
    parent.append(textElement(tag, match[1] ?? match[2] ?? match[3] ?? match[4] ?? match[5]));
    position = match.index + match[0].length;
  }
  parent.append(text.slice(position));
}

function appendSafeMarkdown(parent, source) {
  const lines = String(source ?? "").replace(/\r\n?/g, "\n").split("\n");
  const heading = (line) => line.match(/^ {0,3}(#{1,6})\s+(.+?)\s*#*\s*$/);
  const fence = (line) => line.match(/^ {0,3}(`{3,}|~{3,})(.*)$/);
  const listItem = (line) => line.match(/^ {0,3}(?:([-+*])|(\d+)[.)])\s+(.+)$/);
  let index = 0;
  while (index < lines.length) {
    const line = lines[index];
    if (!line.trim()) { index++; continue; }
    const fenceMatch = fence(line);
    if (fenceMatch) {
      const body = [];
      index++;
      while (index < lines.length) {
        const closing = fence(lines[index]);
        if (closing && closing[1][0] === fenceMatch[1][0] && closing[1].length >= fenceMatch[1].length && !closing[2].trim()) {
          index++;
          break;
        }
        body.push(lines[index++]);
      }
      const pre = document.createElement("pre");
      pre.append(textElement("code", body.join("\n")));
      parent.append(pre);
      continue;
    }
    const headingMatch = heading(line);
    if (headingMatch) {
      const node = document.createElement(`h${Math.min(6, headingMatch[1].length + 3)}`);
      appendInline(node, headingMatch[2]);
      parent.append(node);
      index++;
      continue;
    }
    const listMatch = listItem(line);
    if (listMatch) {
      const ordered = Boolean(listMatch[2]);
      const list = document.createElement(ordered ? "ol" : "ul");
      while (index < lines.length) {
        const itemMatch = listItem(lines[index]);
        if (!itemMatch || Boolean(itemMatch[2]) !== ordered) break;
        const item = document.createElement("li");
        appendInline(item, itemMatch[3]);
        list.append(item);
        index++;
      }
      parent.append(list);
      continue;
    }
    const paragraph = [line.trim()];
    index++;
    while (index < lines.length && lines[index].trim() && !heading(lines[index]) && !fence(lines[index]) && !listItem(lines[index])) {
      paragraph.push(lines[index++].trim());
    }
    const node = document.createElement("p");
    appendInline(node, paragraph.join(" "));
    parent.append(node);
  }
}

function renderChallenge(challenge) {
  const target = output.challenge;
  target.replaceChildren();
  if (!challenge) {
    target.append(textElement("p", "No quest yet. Generate a quest to see the brief here."));
    return;
  }
  target.append(textElement("h3", challenge.title, "output-title"));
  const metadata = document.createElement("dl");
  metadata.className = "quest-meta";
  for (const [label, value] of [["Difficulty", labelize(challenge.difficulty)], ["Expected output", challenge.expectedOutputFormat || "Written answer"]]) {
    const row = document.createElement("div");
    row.append(textElement("dt", label), textElement("dd", value));
    metadata.append(row);
  }
  target.append(metadata);
  if (challenge.context) {
    target.append(textElement("h4", "Context"));
    appendSafeMarkdown(target, challenge.context);
  }
  for (const [title, items] of [["Requirements", challenge.requirements], ["Constraints", challenge.constraints], ["Acceptance criteria", challenge.acceptanceCriteria]]) {
    if (!items?.length) continue;
    target.append(textElement("h4", title));
    const list = document.createElement("ul");
    for (const text of items) {
      const item = document.createElement("li");
      appendInline(item, text);
      list.append(item);
    }
    target.append(list);
  }
}

function renderResult(result) {
  const target = output.submission;
  target.replaceChildren();
  if (!result) {
    target.append(textElement("p", "No evaluation yet. Submit an answer to see your feedback here."));
    return;
  }
  target.append(textElement("h3", "Evaluation result", "output-title"));
  const score = textElement("p", "Final score: ", "evaluation-score");
  score.append(textElement("strong", `${result.finalScore} / 100`));
  target.append(score);
  target.append(textElement("p", `Training tier: ${result.salaryTitle} (${result.salaryTier})`, "evaluation-meta"));
  target.append(textElement("p", `Evaluated: ${dateLabel(result.submittedAt)}`, "evaluation-meta"));
  if (result.rubricScores && Object.keys(result.rubricScores).length) {
    target.append(textElement("h4", "Rubric breakdown"));
    const list = document.createElement("dl");
    list.className = "rubric-list";
    for (const [key, value] of Object.entries(result.rubricScores)) {
      const row = document.createElement("div");
      row.append(textElement("dt", labelize(key)), textElement("dd", value));
      list.append(row);
    }
    target.append(list);
  }
  for (const [title, content] of [["Feedback", result.feedback], ["Next focus", result.improvementTrack]]) {
    if (!content) continue;
    target.append(textElement("h4", title));
    appendSafeMarkdown(target, content);
  }
  target.append(textElement("p", "Identical answers reuse their saved evaluation. XP increases only when your best score for this quest improves.", "evaluation-note"));
}

const ACTIVE_CHALLENGE_KEY = "career-training-active-challenge";

async function restoreChallenge() {
  let activeId;
  try {
    // A navigation hint only. Identity and answer contents stay on the server.
    window.sessionStorage.removeItem("career-training-ui-state");
    activeId = window.sessionStorage.getItem(ACTIVE_CHALLENGE_KEY);
  } catch (_) { /* Storage may be disabled; the latest server quest still restores. */ }
  if (activeId && /^[1-9][0-9]{0,18}$/.test(activeId)) {
    try { return await requestJson(`/api/challenge/${activeId}`); }
    catch (error) { if (error.status !== 404) throw error; }
  }
  return requestJson("/api/challenge/current");
}

// Server and model text only enter the page through safe DOM rendering.
function applyChallenge(saved) {
  state.challenge = saved?.challenge || null;
  try {
    if (state.challenge) window.sessionStorage.setItem(ACTIVE_CHALLENGE_KEY, String(state.challenge.id));
    else window.sessionStorage.removeItem(ACTIVE_CHALLENGE_KEY);
  } catch (_) { /* Session storage is optional; it is never the source of saved work. */ }
  state.savedAnswer = saved?.draftAnswer || "";
  fields.submissionAnswer.value = state.savedAnswer;
  renderChallenge(state.challenge);
  renderResult(saved?.result);
  setSaveStatus(state.challenge ? "Draft saved to server." : "Generate a quest to start writing.");
  updateControls();
}

async function saveDraft() {
  clearTimeout(state.saveTimer);
  if (state.savePromise) return state.savePromise;
  if (!isDirty()) return;
  state.savePromise = Promise.resolve().then(async () => {
    try {
      // Serialize writes so an older, slower save cannot overwrite a newer answer.
      while (isDirty()) {
        const challengeId = state.challenge.id;
        const answer = fields.submissionAnswer.value;
        if (answer.length > 10000) throw new Error("Your answer must be 10,000 characters or fewer before it can be saved.");
        setSaveStatus("Saving draft…");
        await requestJson(`/api/challenge/${challengeId}/draft`, {
          method: "PUT", body: JSON.stringify({ answer }), keepalive: true
        });
        state.savedAnswer = answer;
      }
      setSaveStatus("Draft saved to server.");
    } catch (error) {
      setSaveStatus(`Draft not saved: ${error.message} Your text is still here.`, true);
      throw error;
    } finally {
      state.savePromise = null;
      updateControls();
    }
  });
  updateControls();
  return state.savePromise;
}

function renderProgress(progress) {
  byId("xpValue").textContent = Number(progress.xp).toLocaleString();
  byId("completedValue").textContent = Number(progress.completedChallenges).toLocaleString();
  byId("averageValue").textContent = Number(progress.averageScore).toFixed(1);
  byId("tierValue").textContent = progress.completedChallenges ? progress.salaryTitle : "Start your first quest";
  const list = byId("recommendations");
  list.replaceChildren();
  for (const recommendation of progress.recommendations || []) {
    const item = document.createElement("li");
    item.textContent = recommendation;
    list.append(item);
  }
}

function renderHistory(history) {
  const list = byId("historyList");
  list.replaceChildren();
  if (!history.length) {
    const empty = document.createElement("li");
    empty.textContent = "Your quests and saved answers will appear here.";
    list.append(empty);
  }
  for (const saved of history) {
    const challenge = saved.challenge;
    if (!challenge) continue;
    const item = document.createElement("li");
    const button = document.createElement("button");
    button.type = "button";
    button.className = "history-button";
    button.dataset.challengeId = String(challenge.id);
    const title = document.createElement("strong");
    title.textContent = challenge.title;
    const detail = document.createElement("span");
    detail.textContent = `${labelize(challenge.difficulty)} · ${saved.result ? `Score ${saved.result.finalScore}` : saved.draftAnswer ? "Draft saved" : "Not started"} · ${dateLabel(challenge.createdAt)}`;
    button.append(title, detail);
    button.addEventListener("click", () => runAction(button, "Opening…", async () => {
      await saveDraft();
      const latest = await requestJson(`/api/challenge/${challenge.id}`);
      applyChallenge(latest);
      setStatus("Quest and saved answer restored.");
      output.challenge.focus({ preventScroll: true });
      output.challenge.scrollIntoView({ behavior: "smooth", block: "start" });
    }));
    item.append(button);
    list.append(item);
  }
  updateControls();
}

async function refreshDashboard() {
  const results = await Promise.allSettled([
    requestJson("/api/user/me/progress"), requestJson("/api/user/me/history")
  ]);
  if (results[0].status === "fulfilled") renderProgress(results[0].value);
  if (results[1].status === "fulfilled") renderHistory(results[1].value);
  const errors = results.filter((result) => result.status === "rejected");
  if (errors.length) setStatus(`Your work is saved, but progress or history could not refresh: ${errors[0].reason.message} Use Refresh to try again.`, true);
  return errors.length === 0;
}

async function loadMode() {
  try {
    const mode = await requestJson("/api/debug/ai-mode");
    const local = String(mode.provider).toLowerCase() === "local" || /local/i.test(mode.challengeClient || "") || /local/i.test(mode.evaluationClient || "");
    byId("modeLabel").textContent = local ? "Local practice mode" : "AI practice mode";
    byId("modeDescription").textContent = local
      ? "Quests use local templates and scoring uses simple heuristics. Feedback is for practice; it does not measure job readiness or predict salary."
      : "Quests and feedback use the configured AI service. Review its feedback critically; training tiers do not predict salary.";
  } catch (_) {
    byId("modeLabel").textContent = "Training mode unavailable";
    byId("modeDescription").textContent = "Could not verify whether local or AI evaluation is active. Refresh to check again.";
  }
}

async function runAction(button, busyLabel, action) {
  if (!state.ready || state.busy) return;
  const originalContent = [...button.childNodes];
  state.busy = true;
  button.textContent = busyLabel;
  setStatus(busyLabel);
  updateControls();
  try { await action(); }
  catch (error) { setStatus(error.message, true); }
  finally {
    button.replaceChildren(...originalContent);
    state.busy = false;
    updateControls();
  }
}

buttons.generate.addEventListener("click", () => runAction(buttons.generate, "Generating…", async () => {
  const payload = {
    difficulty: fields.difficulty.value,
    roleTrack: fields.roleTrack.value || null,
    challengeType: fields.challengeType.value.trim() || null,
    focusGoal: fields.focusGoal.value.trim() || null,
    businessContext: fields.businessContext.value.trim() || null,
    customRequirements: splitLines(fields.customRequirements.value),
    customConstraints: [], customAcceptanceCriteria: []
  };
  validateGeneration(payload);
  await saveDraft();
  const challenge = await requestJson("/api/challenge/generate", {
    method: "POST", body: JSON.stringify(payload)
  });
  applyChallenge({ challenge, draftAnswer: "", result: null });
  setStatus("Quest saved. Write your answer below; drafts save automatically.");
  await refreshDashboard();
}));

buttons.submit.addEventListener("click", () => runAction(buttons.submit, "Evaluating…", async () => {
  const answer = fields.submissionAnswer.value.trim();
  if (answer.length < 30) throw new Error("Please write at least 30 characters before submitting.");
  await saveDraft();
  const result = await requestJson("/api/submission", {
    method: "POST", body: JSON.stringify({ challengeId: state.challenge.id, answer })
  });
  state.savedAnswer = answer;
  fields.submissionAnswer.value = answer;
  renderResult(result);
  setSaveStatus("Answer and evaluation saved to server.");
  setStatus("Evaluation ready. Your total XP is shown in Progress.");
  await refreshDashboard();
}));

buttons.save.addEventListener("click", () => runAction(buttons.save, "Saving…", async () => {
  await saveDraft();
  setStatus("Draft saved to server.");
}));

buttons.refresh.addEventListener("click", () => runAction(buttons.refresh, "Refreshing…", async () => {
  await saveDraft();
  const refreshed = await refreshDashboard();
  await loadMode();
  if (refreshed) setStatus("Progress and history refreshed.");
}));

fields.submissionAnswer.addEventListener("input", () => {
  clearTimeout(state.saveTimer);
  setSaveStatus(isDirty() ? "Unsaved changes. Saving shortly…" : "Draft saved to server.");
  updateControls();
  state.saveTimer = setTimeout(() => { saveDraft().catch(() => {}); }, 700);
});

window.addEventListener("beforeunload", (event) => {
  if (isDirty()) {
    saveDraft().catch(() => {});
    event.preventDefault();
    event.returnValue = "";
  }
});

document.addEventListener("visibilitychange", () => {
  if (document.visibilityState === "hidden" && isDirty()) saveDraft().catch(() => {});
});

async function initialize() {
  updateControls();
  setStatus("Restoring your saved session…");
  try {
    // The server-issued HttpOnly cookie is the only identity credential.
    await requestJson("/api/session", { method: "POST", body: "{}" });
    const current = await restoreChallenge();
    state.ready = true;
    applyChallenge(current);
    setStatus(state.challenge ? "Your quest, answer, and evaluation have been restored." : "Ready. Generate your first quest.");
    await Promise.all([refreshDashboard(), loadMode()]);
  } catch (error) {
    setStatus(`Could not restore your session: ${error.message} Reload this page to retry.`, true);
  } finally { updateControls(); }
}

initialize();
