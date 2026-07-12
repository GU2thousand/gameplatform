"use strict";

const LOCALE_KEY = "career-quest-locale-v1";
const WORKSPACE_PREFIX = "career-quest-workspace-v3:";
const MIN_ANSWER_LENGTH = 30;
const MAX_ANSWER_LENGTH = 10000;
const POLL_TIMEOUT_MS = 120000;

const copy = {
  zh: {
    skip: "跳到训练工作区",
    offline: "当前离线。你的草稿已保留，恢复网络后可继续。",
    online: "在线",
    loginRegister: "登录 / 注册",
    eyebrow: "AI 驱动的实战训练",
    heroTitle: "把每一次作答，变成看得见的进步",
    heroCopy: "完成接近真实工作的任务，获得透明、可行动的反馈，再针对薄弱项继续训练。",
    heroNoteTitle: "训练，不是猜薪资",
    heroNoteBody: "结果展示技能等级、证据与下一步。",
    stepCreate: "创建任务",
    stepAnswer: "作答",
    stepReview: "查看反馈",
    stepImprove: "继续提升",
    stepOne: "第 1 步",
    stepTwo: "第 2 步",
    stepThree: "第 3 步",
    stepFour: "第 4 步",
    generatorTitle: "选择训练场景",
    generatorCopy: "从一个快捷模板开始，或用三个核心选项定制任务。",
    presetPmTitle: "产品经理面试",
    presetPmCopy: "PRD · 优先级 · 指标",
    presetSystemTitle: "系统设计面试",
    presetSystemCopy: "架构 · 扩展性 · 权衡",
    presetApiTitle: "API 设计训练",
    presetApiCopy: "契约 · 错误 · 安全",
    difficulty: "难度",
    beginner: "入门",
    intermediate: "中级",
    advanced: "高级",
    track: "岗位方向",
    general: "综合",
    productManagement: "产品管理",
    softwareEngineering: "软件工程",
    pmSde: "产品 + 工程",
    challengeType: "任务类型",
    challengePlaceholder: "例如：系统设计",
    advancedOptions: "高级定制",
    advancedHint: "可选：背景、目标、要求与约束",
    focusGoal: "训练重点",
    focusPlaceholder: "例如：延迟、权衡、边界情况",
    businessContext: "业务背景",
    contextPlaceholder: "描述产品、用户或业务问题。",
    contextHelp: "最多 1,200 字。",
    customRequirements: "自定义要求",
    customConstraints: "自定义约束",
    acceptanceCriteria: "验收标准",
    onePerLine: "每行一项",
    criteriaPlaceholder: "每行一条可衡量的成功标准",
    listHelp: "每行一项，最多 10 项。",
    shortcutGenerate: "最多 10 项 · Ctrl/⌘ + Enter 生成。",
    generate: "生成训练任务",
    nextWeakness: "根据薄弱项出题",
    challengeBrief: "任务简报",
    copyChallenge: "复制任务",
    clearTraining: "清空当前训练",
    noChallenge: "还没有任务",
    noChallengeCopy: "选择模板或设置选项，生成你的第一道训练题。",
    writeAnswer: "提交你的方案",
    answerGate: "生成任务后即可作答；失败时草稿仍会保留。",
    loadExample: "载入示例结构",
    answer: "你的答案",
    answerPlaceholder: "写下你的 PRD、系统设计、API 设计或权衡分析。",
    answerHelp: "至少 30 字符 · Ctrl/⌘ + Enter 提交。",
    submit: "提交并评分",
    noEvaluation: "还没有反馈",
    noEvaluationCopy: "提交答案后，这里会优先展示亮点、改进重点和示例结构。",
    downloadFeedback: "下载反馈",
    retryChallenge: "再次作答",
    currentTraining: "当前训练",
    waitingChallenge: "等待生成任务",
    attempts: "尝试次数",
    bestScore: "最佳成绩",
    jumpAnswer: "前往作答 →",
    scoringTrust: "评分说明",
    scoringTrustCopy: "不同任务使用不同 Rubric 权重。每次结果都会标明 AI 来源或本地降级。",
    progressTitle: "进步仪表盘",
    progressCopy: "区分完成任务数与总尝试次数，关注趋势而不是单次分数。",
    refresh: "刷新",
    signInProgress: "登录后查看长期进步",
    signInProgressCopy: "连续训练、7 日计划、分数趋势和弱项都会集中展示。",
    historyLabel: "训练记录",
    historyTitle: "任务与作答历史",
    historyCopy: "展开同一道题的多次尝试，选择任意两次查看分数和维度变化。",
    noHistory: "暂无训练记录",
    noHistoryCopy: "完成第一次评分后，你的作答会出现在这里。",
    compareLabel: "进步对比",
    compareTitle: "选择两次已完成的作答",
    earlierAttempt: "较早一次",
    laterAttempt: "较晚一次",
    compare: "对比",
    privacyTitle: "隐私与恢复：",
    privacyCopy: "未提交草稿仅保存在当前浏览器会话。账号数据由服务保存，你可以随时导出或删除。",
    account: "账号",
    welcome: "继续你的训练",
    close: "关闭",
    login: "登录",
    register: "注册",
    username: "用户名",
    password: "密码",
    confirmPassword: "确认密码",
    createAccount: "创建账号",
    usernameHelp: "3–50 个字符，不可包含控制字符。",
    passwordHelp: "至少 10 个字符。",
    authPrivacy: "密码不会保存在浏览器中；登录状态由安全会话管理。",
    exportData: "导出我的数据",
    deleteAccount: "删除账号",
    logout: "退出登录",
    cancel: "取消",
    confirm: "确认",
    working: "处理中…",
    signedIn: "已登录，可以开始训练。",
    signedOut: "请先登录或注册，再开始训练。",
    authFailed: "登录失败，请检查用户名和密码。",
    passwordsMismatch: "两次输入的密码不一致。",
    generated: "训练任务已生成。",
    generating: "正在生成训练任务…",
    copied: "任务已复制。",
    copyFailed: "无法访问剪贴板，请手动复制。",
    answerTooShort: "答案至少需要 30 个字符。",
    submitting: "答案已进入评分队列…",
    pending: "等待评分",
    processing: "正在评分",
    completed: "已完成",
    failed: "失败",
    evaluationReady: "评分已完成，反馈和进步数据已更新。",
    evaluationFailed: "评分失败，草稿仍然保留，请稍后重试。",
    pollTimeout: "评分等待超时。任务仍保存在服务器，可刷新历史记录查看。",
    serviceUnavailable: "服务暂时不可用，请稍后重试。",
    networkError: "网络连接失败。草稿已保留，请恢复连接后重试。",
    invalidResponse: "服务返回了无法识别的数据。",
    listTooLong: "每个列表最多 10 项，且每项不超过 400 字符。",
    loadProgress: "正在更新进步数据…",
    loadHistory: "正在更新训练历史…",
    noData: "暂无数据",
    xp: "XP",
    completedTasks: "完成任务",
    totalAttempts: "总尝试",
    averageScore: "平均分",
    currentStreak: "当前连续",
    longestStreak: "最长连续",
    days: "天",
    scoreTrend: "近 7 日趋势",
    sevenDayPlan: "7 日训练计划",
    weakFocus: "当前薄弱项",
    recommendations: "建议",
    rubricDetails: "Rubric 分数与权重",
    feedback: "综合反馈",
    strengths: "做得好的地方",
    improvements: "优先改进",
    exampleOutline: "参考结构",
    improvementTrack: "下一步训练",
    provider: "评分来源",
    local: "本地评估",
    langchain4j: "AI 评估",
    fallback: "AI 降级为本地评估",
    legacy: "旧版评分",
    context: "背景",
    requirements: "要求",
    constraints: "约束",
    expectedOutput: "预期输出",
    viewAttempt: "查看",
    best: "最佳",
    attempt: "第 {value} 次",
    scoreDelta: "总分变化",
    improved: "提升维度",
    declined: "下降维度",
    compareReady: "作答对比已生成。",
    chooseSameChallenge: "请选择同一道任务的两次已完成作答。",
    confirmClearTitle: "清空当前训练？",
    confirmClearBody: "这会清除本标签页中的任务、草稿和当前反馈，不会删除服务器历史。",
    confirmDeleteTitle: "永久删除账号？",
    confirmDeleteBody: "账号、任务、作答和评分数据会被删除，且无法撤销。",
    cleared: "当前训练已清空。",
    accountDeleted: "账号和训练数据已删除。",
    exportReady: "数据导出已开始。",
    feedbackDownloaded: "反馈文件已生成。",
    nextGenerated: "已根据当前薄弱项生成下一道任务。",
    offlineAction: "当前离线，恢复网络后再试。",
    loggedOut: "已退出登录。",
    unknown: "未指定",
    noAttempts: "还没有作答。",
    planDone: "已完成",
    planTodo: "待训练",
    dayLabel: "第 {value} 天",
    score: "分数",
    weight: "权重"
  },
  en: {
    skip: "Skip to training workspace",
    offline: "You are offline. Your draft is safe and will be ready when the connection returns.",
    online: "Online",
    loginRegister: "Log in / Register",
    eyebrow: "AI-powered deliberate practice",
    heroTitle: "Turn every attempt into visible progress",
    heroCopy: "Practice realistic work scenarios, get transparent feedback, and train the skills that need attention next.",
    heroNoteTitle: "Skills, not salary guesses",
    heroNoteBody: "Results show evidence, a skill level, and the next action.",
    stepCreate: "Create",
    stepAnswer: "Answer",
    stepReview: "Review",
    stepImprove: "Improve",
    stepOne: "Step 1",
    stepTwo: "Step 2",
    stepThree: "Step 3",
    stepFour: "Step 4",
    generatorTitle: "Choose a training scenario",
    generatorCopy: "Start with a preset or tune the three essentials.",
    presetPmTitle: "Product interview",
    presetPmCopy: "PRD · prioritization · metrics",
    presetSystemTitle: "System design",
    presetSystemCopy: "architecture · scale · tradeoffs",
    presetApiTitle: "API design",
    presetApiCopy: "contracts · errors · security",
    difficulty: "Difficulty",
    beginner: "Beginner",
    intermediate: "Intermediate",
    advanced: "Advanced",
    track: "Role track",
    general: "General",
    productManagement: "Product management",
    softwareEngineering: "Software engineering",
    pmSde: "Product + engineering",
    challengeType: "Challenge type",
    challengePlaceholder: "e.g. System design",
    advancedOptions: "Advanced options",
    advancedHint: "Optional context, goals, requirements, and constraints",
    focusGoal: "Focus goal",
    focusPlaceholder: "e.g. latency, tradeoffs, edge cases",
    businessContext: "Business context",
    contextPlaceholder: "Describe the product, user, or business problem.",
    contextHelp: "Up to 1,200 characters.",
    customRequirements: "Custom requirements",
    customConstraints: "Custom constraints",
    acceptanceCriteria: "Acceptance criteria",
    onePerLine: "One item per line",
    criteriaPlaceholder: "One measurable success criterion per line",
    listHelp: "One per line, up to 10 items.",
    shortcutGenerate: "Up to 10 items · Ctrl/⌘ + Enter to generate.",
    generate: "Generate challenge",
    nextWeakness: "Train my weakest skill",
    challengeBrief: "Challenge brief",
    copyChallenge: "Copy challenge",
    clearTraining: "Clear current training",
    noChallenge: "No challenge yet",
    noChallengeCopy: "Choose a preset or tune the options to generate your first challenge.",
    writeAnswer: "Submit your solution",
    answerGate: "Generate a challenge to begin. Your draft remains safe if a request fails.",
    loadExample: "Load example structure",
    answer: "Your answer",
    answerPlaceholder: "Write your PRD, system design, API design, or tradeoff analysis.",
    answerHelp: "At least 30 characters · Ctrl/⌘ + Enter to submit.",
    submit: "Submit for review",
    noEvaluation: "No feedback yet",
    noEvaluationCopy: "After submission, strengths, improvements, and an example outline appear here.",
    downloadFeedback: "Download feedback",
    retryChallenge: "Try again",
    currentTraining: "Current training",
    waitingChallenge: "Waiting for a challenge",
    attempts: "Attempts",
    bestScore: "Best score",
    jumpAnswer: "Jump to answer →",
    scoringTrust: "How scoring works",
    scoringTrustCopy: "Each challenge type has its own rubric weights. Every result identifies the AI source or local fallback.",
    progressTitle: "Progress dashboard",
    progressCopy: "Separate completed challenges from total attempts and focus on the trend.",
    refresh: "Refresh",
    signInProgress: "Log in to see long-term progress",
    signInProgressCopy: "Streaks, a seven-day plan, score trends, and weak skills live here.",
    historyLabel: "Training log",
    historyTitle: "Challenges and attempts",
    historyCopy: "Open repeated attempts and compare any two answers to the same challenge.",
    noHistory: "No training history",
    noHistoryCopy: "Your attempts will appear here after the first evaluation.",
    compareLabel: "Progress comparison",
    compareTitle: "Choose two completed attempts",
    earlierAttempt: "Earlier attempt",
    laterAttempt: "Later attempt",
    compare: "Compare",
    privacyTitle: "Privacy and recovery:",
    privacyCopy: "Unsubmitted drafts stay in this browser session. You can export or delete server-side account data at any time.",
    account: "Account",
    welcome: "Continue your training",
    close: "Close",
    login: "Log in",
    register: "Register",
    username: "Username",
    password: "Password",
    confirmPassword: "Confirm password",
    createAccount: "Create account",
    usernameHelp: "3–50 characters with no control characters.",
    passwordHelp: "At least 10 characters.",
    authPrivacy: "Passwords are never saved in the browser. A secure server session keeps you signed in.",
    exportData: "Export my data",
    deleteAccount: "Delete account",
    logout: "Log out",
    cancel: "Cancel",
    confirm: "Confirm",
    working: "Working…",
    signedIn: "You are signed in and ready to train.",
    signedOut: "Log in or register before starting a challenge.",
    authFailed: "Login failed. Check the username and password.",
    passwordsMismatch: "The passwords do not match.",
    generated: "Your training challenge is ready.",
    generating: "Generating a training challenge…",
    copied: "Challenge copied.",
    copyFailed: "Clipboard access failed. Please copy manually.",
    answerTooShort: "Your answer must contain at least 30 characters.",
    submitting: "Your answer is in the evaluation queue…",
    pending: "Waiting for evaluation",
    processing: "Evaluation in progress",
    completed: "Completed",
    failed: "Failed",
    evaluationReady: "Evaluation complete. Feedback and progress are up to date.",
    evaluationFailed: "Evaluation failed. Your draft is still safe; please try again.",
    pollTimeout: "Evaluation is taking longer than expected. Refresh history to check the saved job.",
    serviceUnavailable: "The service is temporarily unavailable. Please try again.",
    networkError: "The network request failed. Your draft is safe; reconnect and retry.",
    invalidResponse: "The service returned data that could not be read.",
    listTooLong: "Each list supports up to 10 items and 400 characters per item.",
    loadProgress: "Updating progress…",
    loadHistory: "Updating training history…",
    noData: "No data yet",
    xp: "XP",
    completedTasks: "Completed",
    totalAttempts: "Total attempts",
    averageScore: "Average score",
    currentStreak: "Current streak",
    longestStreak: "Longest streak",
    days: "days",
    scoreTrend: "Seven-day trend",
    sevenDayPlan: "Seven-day training plan",
    weakFocus: "Current weak skill",
    recommendations: "Recommendations",
    rubricDetails: "Rubric scores and weights",
    feedback: "Overall feedback",
    strengths: "What worked",
    improvements: "Priority improvements",
    exampleOutline: "Example outline",
    improvementTrack: "Next training step",
    provider: "Evaluation source",
    local: "Local evaluator",
    langchain4j: "AI evaluator",
    fallback: "AI fallback to local evaluator",
    legacy: "Legacy evaluation",
    context: "Context",
    requirements: "Requirements",
    constraints: "Constraints",
    expectedOutput: "Expected output",
    viewAttempt: "View",
    best: "Best",
    attempt: "Attempt {value}",
    scoreDelta: "Score change",
    improved: "Improved dimensions",
    declined: "Declined dimensions",
    compareReady: "Attempt comparison is ready.",
    chooseSameChallenge: "Choose two completed attempts from the same challenge.",
    confirmClearTitle: "Clear current training?",
    confirmClearBody: "This removes the current challenge, draft, and feedback from this tab. Server history is preserved.",
    confirmDeleteTitle: "Permanently delete this account?",
    confirmDeleteBody: "The account, challenges, attempts, and evaluations will be deleted and cannot be restored.",
    cleared: "The current training workspace has been cleared.",
    accountDeleted: "The account and its training data were deleted.",
    exportReady: "Your data export has started.",
    feedbackDownloaded: "The feedback file is ready.",
    nextGenerated: "A new challenge was generated for your weakest skill.",
    offlineAction: "You are offline. Reconnect before trying this action.",
    loggedOut: "You are logged out.",
    unknown: "Unspecified",
    noAttempts: "No attempts yet.",
    planDone: "Done",
    planTodo: "To do",
    dayLabel: "Day {value}",
    score: "Score",
    weight: "Weight"
  }
};

const rubricLabels = {
  requirement_understanding: { zh: "需求理解", en: "Requirement understanding" },
  logical_clarity: { zh: "逻辑清晰度", en: "Logical clarity" },
  technical_feasibility: { zh: "技术可行性", en: "Technical feasibility" },
  edge_case_coverage: { zh: "边界情况覆盖", en: "Edge-case coverage" },
  communication_structure: { zh: "表达结构", en: "Communication structure" }
};

const state = {
  locale: safeLocalGet(LOCALE_KEY) === "en" ? "en" : "zh",
  user: null,
  challenge: null,
  submission: null,
  evaluation: null,
  progress: null,
  history: [],
  busy: new Set(),
  pendingKey: null,
  pendingFingerprint: null,
  selectedPreset: null
};

const dom = {
  offlineBanner: byId("offlineBanner"),
  connectionState: byId("connectionState"),
  localeToggle: byId("localeToggle"),
  globalStatus: byId("globalStatus"),
  signedOutActions: byId("signedOutActions"),
  signedInActions: byId("signedInActions"),
  openAuthBtn: byId("openAuthBtn"),
  profileMenuBtn: byId("profileMenuBtn"),
  profileDropdown: byId("profileDropdown"),
  currentUsername: byId("currentUsername"),
  menuUsername: byId("menuUsername"),
  menuXp: byId("menuXp"),
  exportDataBtn: byId("exportDataBtn"),
  deleteAccountBtn: byId("deleteAccountBtn"),
  logoutBtn: byId("logoutBtn"),
  generateForm: byId("generateForm"),
  difficulty: byId("difficulty"),
  roleTrack: byId("roleTrack"),
  challengeType: byId("challengeType"),
  focusGoal: byId("focusGoal"),
  businessContext: byId("businessContext"),
  customRequirements: byId("customRequirements"),
  customConstraints: byId("customConstraints"),
  customAcceptanceCriteria: byId("customAcceptanceCriteria"),
  generateBtn: byId("generateBtn"),
  nextWeaknessBtn: byId("nextWeaknessBtn"),
  challengeStatus: byId("challengeStatus"),
  challengeOutput: byId("challengeOutput"),
  copyChallengeBtn: byId("copyChallengeBtn"),
  clearTrainingBtn: byId("clearTrainingBtn"),
  submissionForm: byId("submissionForm"),
  submissionAnswer: byId("submissionAnswer"),
  answerCount: byId("answerCount"),
  submissionGate: byId("submissionGate"),
  exampleAnswerBtn: byId("exampleAnswerBtn"),
  submitBtn: byId("submitBtn"),
  submissionStatus: byId("submissionStatus"),
  submissionOutput: byId("submissionOutput"),
  downloadFeedbackBtn: byId("downloadFeedbackBtn"),
  retryChallengeBtn: byId("retryChallengeBtn"),
  summaryTitle: byId("summaryTitle"),
  summaryTrack: byId("summaryTrack"),
  summaryDifficulty: byId("summaryDifficulty"),
  summaryAttempts: byId("summaryAttempts"),
  summaryBest: byId("summaryBest"),
  refreshProgressBtn: byId("refreshProgressBtn"),
  progressStatus: byId("progressStatus"),
  progressOutput: byId("progressOutput"),
  refreshHistoryBtn: byId("refreshHistoryBtn"),
  historyStatus: byId("historyStatus"),
  historyOutput: byId("historyOutput"),
  compareFirst: byId("compareFirst"),
  compareSecond: byId("compareSecond"),
  compareBtn: byId("compareBtn"),
  compareStatus: byId("compareStatus"),
  compareOutput: byId("compareOutput"),
  authDialog: byId("authDialog"),
  closeAuthBtn: byId("closeAuthBtn"),
  loginTab: byId("loginTab"),
  registerTab: byId("registerTab"),
  loginForm: byId("loginForm"),
  registerForm: byId("registerForm"),
  loginUsername: byId("loginUsername"),
  loginPassword: byId("loginPassword"),
  registerUsername: byId("registerUsername"),
  registerPassword: byId("registerPassword"),
  confirmPassword: byId("confirmPassword"),
  authStatus: byId("authStatus"),
  confirmDialog: byId("confirmDialog"),
  confirmTitle: byId("confirmTitle"),
  confirmMessage: byId("confirmMessage"),
  confirmCancelBtn: byId("confirmCancelBtn"),
  confirmActionBtn: byId("confirmActionBtn")
};

let persistTimer = null;
let confirmResolver = null;

class ApiError extends Error {
  constructor(message, status, code) {
    super(message);
    this.name = "ApiError";
    this.status = status || 0;
    this.code = code || "REQUEST_FAILED";
  }
}

function byId(id) {
  return document.getElementById(id);
}

function safeLocalGet(key) {
  try {
    return window.localStorage.getItem(key);
  } catch (error) {
    return null;
  }
}

function safeLocalSet(key, value) {
  try {
    window.localStorage.setItem(key, value);
  } catch (error) {
    return;
  }
}

function safeSessionGet(key) {
  try {
    return window.sessionStorage.getItem(key);
  } catch (error) {
    return null;
  }
}

function safeSessionSet(key, value) {
  try {
    window.sessionStorage.setItem(key, value);
  } catch (error) {
    return;
  }
}

function safeSessionRemove(key) {
  try {
    window.sessionStorage.removeItem(key);
  } catch (error) {
    return;
  }
}

function tr(key, variables) {
  let value = copy[state.locale][key] || copy.en[key] || key;
  if (variables) {
    Object.keys(variables).forEach(function (name) {
      value = value.replace("{" + name + "}", String(variables[name]));
    });
  }
  return value;
}

function cleanText(value, fallback) {
  if (typeof value !== "string") {
    return fallback || "";
  }
  const text = value.trim();
  return text || fallback || "";
}

function finite(value, fallback) {
  return typeof value === "number" && Number.isFinite(value) ? value : (fallback || 0);
}

function list(value) {
  return Array.isArray(value) ? value.filter(function (item) {
    return typeof item === "string" && item.trim();
  }).map(function (item) {
    return item.trim();
  }) : [];
}

function element(tagName, className, text) {
  const node = document.createElement(tagName);
  if (className) {
    node.className = className;
  }
  if (text !== undefined && text !== null) {
    node.textContent = String(text);
  }
  return node;
}

function clear(node) {
  while (node.firstChild) {
    node.removeChild(node.firstChild);
  }
}

function setStatus(node, message, kind) {
  node.textContent = message || "";
  node.className = "status" + (kind ? " status--" + kind : "");
  node.setAttribute("role", kind === "error" ? "alert" : "status");
}

function errorMessage(error) {
  if (error instanceof ApiError) {
    if (error.status === 502 || error.status === 503 || error.status === 504) {
      return error.message ? tr("serviceUnavailable") + " " + error.message : tr("serviceUnavailable");
    }
    if (error.status === 429) {
      return error.message || tr("serviceUnavailable");
    }
    return error.message || tr("serviceUnavailable");
  }
  if (!navigator.onLine || error instanceof TypeError) {
    return tr("networkError");
  }
  return tr("serviceUnavailable");
}

function setBusy(name, button, busy) {
  if (busy) {
    state.busy.add(name);
  } else {
    state.busy.delete(name);
  }
  if (button) {
    button.dataset.busy = busy ? "true" : "false";
    button.disabled = busy;
    if (busy) {
      button.textContent = tr("working");
    } else {
      const key = button.getAttribute("data-i18n");
      if (key) {
        button.textContent = tr(key);
      }
    }
  }
  updateControls();
}

function isMutation(method) {
  return !["GET", "HEAD", "OPTIONS"].includes(method);
}

async function getCsrfToken() {
  const response = await fetch("/api/auth/csrf", {
    method: "GET",
    credentials: "same-origin",
    headers: { Accept: "application/json" }
  });
  if (!response.ok) {
    throw new ApiError(tr("serviceUnavailable"), response.status);
  }
  const payload = await response.json();
  if (!payload || typeof payload.token !== "string") {
    throw new ApiError(tr("invalidResponse"), response.status, "INVALID_RESPONSE");
  }
  return {
    token: payload.token,
    headerName: cleanText(payload.headerName, "X-XSRF-TOKEN")
  };
}

async function api(url, options) {
  const settings = options || {};
  const method = (settings.method || "GET").toUpperCase();
  const timeoutMs = settings.timeoutMs || 30000;
  const headers = Object.assign({ Accept: "application/json" }, settings.headers || {});
  if (isMutation(method)) {
    if (!navigator.onLine) {
      throw new ApiError(tr("offlineAction"), 0, "OFFLINE");
    }
    const csrf = await getCsrfToken();
    headers[csrf.headerName] = csrf.token;
  }
  let body;
  if (settings.body !== undefined) {
    headers["Content-Type"] = "application/json";
    body = JSON.stringify(settings.body);
  }
  const controller = new AbortController();
  const timeout = window.setTimeout(function () {
    controller.abort();
  }, timeoutMs);
  try {
    const response = await fetch(url, {
      method: method,
      credentials: "same-origin",
      headers: headers,
      body: body,
      signal: controller.signal
    });
    const raw = await response.text();
    let payload = null;
    if (raw.trim()) {
      try {
        payload = JSON.parse(raw);
      } catch (error) {
        if (response.ok) {
          throw new ApiError(tr("invalidResponse"), response.status, "INVALID_RESPONSE");
        }
      }
    }
    if (!response.ok) {
      const message = payload && typeof payload.message === "string"
        ? payload.message
        : "HTTP " + response.status;
      throw new ApiError(message, response.status);
    }
    return payload;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    if (error && error.name === "AbortError") {
      throw new ApiError(tr("pollTimeout"), 0, "TIMEOUT");
    }
    throw new ApiError(tr("networkError"), 0, "NETWORK");
  } finally {
    window.clearTimeout(timeout);
  }
}

function applyLocale() {
  document.documentElement.lang = state.locale === "zh" ? "zh-CN" : "en";
  document.title = state.locale === "zh" ? "Career Quest · AI 职业训练" : "Career Quest · AI Career Training";
  document.querySelectorAll("[data-i18n]").forEach(function (node) {
    const key = node.getAttribute("data-i18n");
    if (copy[state.locale][key]) {
      node.textContent = tr(key);
    }
  });
  document.querySelectorAll("[data-i18n-placeholder]").forEach(function (node) {
    node.setAttribute("placeholder", tr(node.getAttribute("data-i18n-placeholder")));
  });
  document.querySelectorAll("[data-i18n-aria]").forEach(function (node) {
    node.setAttribute("aria-label", tr(node.getAttribute("data-i18n-aria")));
  });
  dom.localeToggle.textContent = state.locale === "zh" ? "EN" : "中文";
  dom.localeToggle.setAttribute("aria-label", state.locale === "zh" ? "Switch to English" : "切换到中文");
  if (state.challenge) {
    renderChallenge(state.challenge, false);
  } else {
    renderEmptyChallenge();
  }
  if (state.evaluation) {
    renderEvaluation(state.evaluation);
  } else {
    renderEmptyEvaluation();
  }
  if (state.progress) {
    renderProgress(state.progress);
  } else {
    renderEmptyProgress();
  }
  renderHistory();
  updateAuthUi();
  updateAnswerCount();
}

function formatDate(value) {
  if (!value) {
    return "—";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "—";
  }
  return new Intl.DateTimeFormat(state.locale === "zh" ? "zh-CN" : "en-US", {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  }).format(date);
}

function formatScore(value) {
  return new Intl.NumberFormat(state.locale === "zh" ? "zh-CN" : "en-US", {
    maximumFractionDigits: 2
  }).format(finite(value, 0));
}

function difficultyName(value) {
  const key = cleanText(value).toUpperCase();
  if (key === "BEGINNER") {
    return tr("beginner");
  }
  if (key === "INTERMEDIATE") {
    return tr("intermediate");
  }
  if (key === "ADVANCED") {
    return tr("advanced");
  }
  return cleanText(value, tr("unknown"));
}

function providerName(value) {
  const provider = cleanText(value, "local").toLowerCase();
  if (provider === "fallback") {
    return tr("fallback");
  }
  if (provider === "langchain4j") {
    return tr("langchain4j");
  }
  if (provider === "legacy") {
    return tr("legacy");
  }
  return tr("local");
}

function rubricName(key) {
  return rubricLabels[key] ? rubricLabels[key][state.locale] : key.replaceAll("_", " ");
}

function updateOnlineState() {
  const online = navigator.onLine;
  dom.offlineBanner.hidden = online;
  dom.connectionState.dataset.offline = online ? "false" : "true";
  const label = dom.connectionState.querySelector("[data-i18n]");
  if (label) {
    label.textContent = online ? tr("online") : (state.locale === "zh" ? "离线" : "Offline");
  }
}

function workspaceKey(userId) {
  return WORKSPACE_PREFIX + (userId || "guest");
}

function formSnapshot() {
  return {
    difficulty: dom.difficulty.value,
    roleTrack: dom.roleTrack.value,
    challengeType: dom.challengeType.value,
    focusGoal: dom.focusGoal.value,
    businessContext: dom.businessContext.value,
    customRequirements: dom.customRequirements.value,
    customConstraints: dom.customConstraints.value,
    customAcceptanceCriteria: dom.customAcceptanceCriteria.value,
    answer: dom.submissionAnswer.value
  };
}

function persistWorkspace() {
  const payload = {
    form: formSnapshot(),
    challenge: state.challenge,
    submission: state.submission,
    evaluation: state.evaluation,
    pendingKey: state.pendingKey,
    pendingFingerprint: state.pendingFingerprint
  };
  safeSessionSet(workspaceKey(state.user && state.user.id), JSON.stringify(payload));
}

function queuePersist() {
  window.clearTimeout(persistTimer);
  persistTimer = window.setTimeout(persistWorkspace, 150);
}

function readWorkspace(key) {
  const raw = safeSessionGet(key);
  if (!raw) {
    return null;
  }
  try {
    const parsed = JSON.parse(raw);
    return parsed && typeof parsed === "object" ? parsed : null;
  } catch (error) {
    safeSessionRemove(key);
    return null;
  }
}

function restoreWorkspace() {
  const userKey = workspaceKey(state.user && state.user.id);
  let saved = readWorkspace(userKey);
  if (!saved && state.user) {
    saved = readWorkspace(workspaceKey(null));
  }
  if (!saved) {
    return;
  }
  const form = saved.form || {};
  [
    "difficulty",
    "roleTrack",
    "challengeType",
    "focusGoal",
    "businessContext",
    "customRequirements",
    "customConstraints",
    "customAcceptanceCriteria"
  ].forEach(function (key) {
    if (typeof form[key] === "string" && dom[key]) {
      dom[key].value = form[key];
    }
  });
  if (typeof form.answer === "string") {
    dom.submissionAnswer.value = form.answer.slice(0, MAX_ANSWER_LENGTH);
  }
  if (saved.challenge && typeof saved.challenge.id === "number") {
    state.challenge = saved.challenge;
    renderChallenge(state.challenge, false);
  }
  if (saved.submission && typeof saved.submission.submissionId === "number") {
    state.submission = saved.submission;
  }
  if (saved.evaluation && typeof saved.evaluation.finalScore === "number") {
    state.evaluation = saved.evaluation;
    renderEvaluation(state.evaluation);
  }
  state.pendingKey = typeof saved.pendingKey === "string" ? saved.pendingKey : null;
  state.pendingFingerprint = typeof saved.pendingFingerprint === "string" ? saved.pendingFingerprint : null;
  updateAnswerCount();
  updateControls();
}

function clearWorkspace(removeStored) {
  if (removeStored) {
    safeSessionRemove(workspaceKey(state.user && state.user.id));
  }
  state.challenge = null;
  state.submission = null;
  state.evaluation = null;
  state.pendingKey = null;
  state.pendingFingerprint = null;
  dom.submissionAnswer.value = "";
  renderEmptyChallenge();
  renderEmptyEvaluation();
  updateAnswerCount();
  updateSteps(1);
  updateControls();
}

function updateAuthUi() {
  const signedIn = Boolean(state.user);
  dom.signedOutActions.hidden = signedIn;
  dom.signedInActions.hidden = !signedIn;
  if (signedIn) {
    const username = cleanText(state.user.username, tr("account"));
    dom.currentUsername.textContent = username;
    dom.menuUsername.textContent = username;
    dom.menuXp.textContent = formatScore(state.user.xp) + " XP";
    const avatar = dom.profileMenuBtn.querySelector(".avatar");
    if (avatar) {
      avatar.textContent = username.slice(0, 1).toUpperCase();
    }
  }
  updateControls();
}

function updateControls() {
  const signedIn = Boolean(state.user);
  const hasChallenge = Boolean(state.challenge);
  const hasEvaluation = Boolean(state.evaluation);
  const answerValid = dom.submissionAnswer.value.trim().length >= MIN_ANSWER_LENGTH;
  dom.generateBtn.disabled = state.busy.has("generate");
  dom.nextWeaknessBtn.disabled = !signedIn || state.busy.has("generate");
  dom.copyChallengeBtn.disabled = !hasChallenge;
  dom.clearTrainingBtn.disabled = !hasChallenge && !dom.submissionAnswer.value;
  dom.exampleAnswerBtn.disabled = !hasChallenge;
  dom.submitBtn.disabled = !signedIn || !hasChallenge || !answerValid || state.busy.has("submit");
  dom.downloadFeedbackBtn.disabled = !hasEvaluation;
  dom.retryChallengeBtn.disabled = !hasChallenge || !hasEvaluation;
  dom.refreshProgressBtn.disabled = !signedIn || state.busy.has("progress");
  dom.refreshHistoryBtn.disabled = !signedIn || state.busy.has("history");
}

function updateAnswerCount() {
  const count = dom.submissionAnswer.value.length;
  dom.answerCount.textContent = count.toLocaleString() + " / " + MAX_ANSWER_LENGTH.toLocaleString();
  dom.answerCount.dataset.limit = count >= MAX_ANSWER_LENGTH
    ? "reached"
    : (count >= MAX_ANSWER_LENGTH * 0.9 ? "near" : "");
  dom.submissionAnswer.setAttribute("aria-invalid",
    count > 0 && count < MIN_ANSWER_LENGTH ? "true" : "false");
  updateControls();
}

function updateSteps(active) {
  document.querySelectorAll(".stepper li").forEach(function (item) {
    const step = Number(item.dataset.step);
    if (step === active) {
      item.setAttribute("aria-current", "step");
    } else {
      item.removeAttribute("aria-current");
    }
    item.dataset.complete = step < active ? "true" : "false";
  });
}

function emptyState(title, body) {
  const wrapper = element("div", "empty-state");
  wrapper.appendChild(element("h3", "", title));
  wrapper.appendChild(element("p", "", body));
  return wrapper;
}

function renderEmptyChallenge() {
  clear(dom.challengeOutput);
  dom.challengeOutput.appendChild(emptyState(tr("noChallenge"), tr("noChallengeCopy")));
  dom.summaryTitle.textContent = tr("waitingChallenge");
  dom.summaryTrack.textContent = "—";
  dom.summaryDifficulty.textContent = "—";
  dom.summaryAttempts.textContent = "0";
  dom.summaryBest.textContent = "—";
}

function renderEmptyEvaluation() {
  clear(dom.submissionOutput);
  dom.submissionOutput.appendChild(emptyState(tr("noEvaluation"), tr("noEvaluationCopy")));
}

function renderEmptyProgress() {
  clear(dom.progressOutput);
  if (state.user) {
    dom.progressOutput.appendChild(emptyState(tr("noData"), tr("signInProgressCopy")));
  } else {
    dom.progressOutput.appendChild(emptyState(tr("signInProgress"), tr("signInProgressCopy")));
  }
}

function addMeta(listNode, text, extraClass, fallback) {
  if (!text && !fallback) {
    return;
  }
  const item = element("li", "meta-pill" + (extraClass ? " " + extraClass : ""), text || fallback);
  listNode.appendChild(item);
  return item;
}

function addTextSection(parent, title, content) {
  if (!content) {
    return;
  }
  const section = element("section", "result-section");
  section.appendChild(element("h4", "", title));
  section.appendChild(element("p", "", content));
  parent.appendChild(section);
}

function addListSection(parent, title, values) {
  if (!values.length) {
    return;
  }
  const section = element("section", "result-section");
  section.appendChild(element("h4", "", title));
  const ul = element("ul");
  values.forEach(function (value) {
    ul.appendChild(element("li", "", value));
  });
  section.appendChild(ul);
  parent.appendChild(section);
}

function renderChallenge(challenge, focus) {
  if (!challenge) {
    renderEmptyChallenge();
    return;
  }
  state.challenge = challenge;
  clear(dom.challengeOutput);
  const header = element("header", "result-header");
  const heading = element("div");
  heading.appendChild(element("h3", "result-title", cleanText(challenge.title, tr("challengeBrief"))));
  const meta = element("ul", "meta-list");
  addMeta(meta, difficultyName(challenge.difficulty));
  addMeta(meta, cleanText(challenge.roleTrack));
  addMeta(meta, cleanText(challenge.challengeType));
  const provider = addMeta(meta, providerName(challenge.generationProvider), "provider-pill");
  if (provider) {
    provider.dataset.fallback = cleanText(challenge.generationProvider).toLowerCase() === "fallback"
      ? "true"
      : "false";
  }
  heading.appendChild(meta);
  header.appendChild(heading);
  dom.challengeOutput.appendChild(header);
  addTextSection(dom.challengeOutput, tr("context"), cleanText(challenge.context));
  addListSection(dom.challengeOutput, tr("requirements"), list(challenge.requirements));
  addListSection(dom.challengeOutput, tr("constraints"), list(challenge.constraints));
  addListSection(dom.challengeOutput, tr("acceptanceCriteria"), list(challenge.acceptanceCriteria));
  addTextSection(dom.challengeOutput, tr("expectedOutput"), cleanText(challenge.expectedOutputFormat));
  dom.summaryTitle.textContent = cleanText(challenge.title, tr("challengeBrief"));
  dom.summaryTrack.textContent = cleanText(challenge.roleTrack, tr("general"));
  dom.summaryDifficulty.textContent = difficultyName(challenge.difficulty);
  dom.submissionGate.textContent = tr("answerGate");
  updateSteps(state.evaluation ? 4 : 2);
  updateControls();
  queuePersist();
  if (focus) {
    dom.challengeOutput.focus({ preventScroll: true });
    dom.challengeOutput.scrollIntoView({ behavior: "smooth", block: "start" });
  }
}

function challengeAsText() {
  if (!state.challenge) {
    return "";
  }
  const challenge = state.challenge;
  const lines = [
    cleanText(challenge.title),
    "",
    tr("difficulty") + ": " + difficultyName(challenge.difficulty),
    tr("track") + ": " + cleanText(challenge.roleTrack, tr("general")),
    "",
    tr("context") + ":",
    cleanText(challenge.context),
    "",
    tr("requirements") + ":"
  ];
  list(challenge.requirements).forEach(function (item) {
    lines.push("- " + item);
  });
  lines.push("", tr("constraints") + ":");
  list(challenge.constraints).forEach(function (item) {
    lines.push("- " + item);
  });
  lines.push("", tr("acceptanceCriteria") + ":");
  list(challenge.acceptanceCriteria).forEach(function (item) {
    lines.push("- " + item);
  });
  return lines.join("\n");
}

function renderEvaluation(evaluation) {
  if (!evaluation) {
    renderEmptyEvaluation();
    return;
  }
  state.evaluation = evaluation;
  clear(dom.submissionOutput);
  const header = element("header", "result-header");
  const titleBox = element("div");
  titleBox.appendChild(element("h3", "result-title", cleanText(evaluation.skillTitle, tr("completed"))));
  const meta = element("ul", "meta-list");
  addMeta(meta, providerName(evaluation.provider), "provider-pill");
  const provider = meta.lastElementChild;
  if (provider) {
    provider.dataset.fallback = cleanText(evaluation.provider).toLowerCase() === "fallback" ? "true" : "false";
  }
  addMeta(meta, cleanText(evaluation.skillTier));
  titleBox.appendChild(meta);
  const score = element("div", "score-callout");
  score.appendChild(element("span", "score-value", formatScore(evaluation.finalScore)));
  score.appendChild(element("span", "score-unit", "/ 100"));
  header.appendChild(titleBox);
  header.appendChild(score);
  dom.submissionOutput.appendChild(header);

  const priorities = element("div", "feedback-priority-grid");
  priorities.appendChild(feedbackCard("feedback-card feedback-card--strength", tr("strengths"),
    list(evaluation.strengths)));
  priorities.appendChild(feedbackCard("feedback-card feedback-card--improve", tr("improvements"),
    list(evaluation.improvements)));
  dom.submissionOutput.appendChild(priorities);

  addTextSection(dom.submissionOutput, tr("feedback"), cleanText(evaluation.feedback));
  addTextSection(dom.submissionOutput, tr("improvementTrack"), cleanText(evaluation.improvementTrack));

  const details = element("details", "collapsible-section");
  details.open = true;
  details.appendChild(element("summary", "", tr("rubricDetails")));
  const rubricGrid = element("div", "rubric-grid");
  const scores = evaluation.rubricScores && typeof evaluation.rubricScores === "object"
    ? evaluation.rubricScores
    : {};
  const weights = evaluation.rubricWeights && typeof evaluation.rubricWeights === "object"
    ? evaluation.rubricWeights
    : {};
  Object.keys(scores).forEach(function (key) {
    const row = element("div", "rubric-row");
    const label = element("div", "rubric-label");
    label.appendChild(element("span", "", rubricName(key)));
    const weight = finite(weights[key], 0);
    label.appendChild(element("small", "rubric-weight",
      tr("weight") + " " + formatScore(weight * 100) + "%"));
    const progress = element("progress");
    progress.max = 100;
    progress.value = finite(scores[key], 0);
    progress.setAttribute("aria-label", rubricName(key));
    const numeric = element("span", "rubric-score", formatScore(scores[key]) + " / 100");
    row.appendChild(label);
    row.appendChild(progress);
    row.appendChild(numeric);
    rubricGrid.appendChild(row);
  });
  details.appendChild(rubricGrid);
  dom.submissionOutput.appendChild(details);
  addTextSection(dom.submissionOutput, tr("exampleOutline"), cleanText(evaluation.exampleOutline));

  dom.summaryBest.textContent = formatScore(evaluation.finalScore);
  updateSteps(4);
  updateControls();
  queuePersist();
}

function feedbackCard(className, title, values) {
  const card = element("section", className);
  card.appendChild(element("h4", "", title));
  if (values.length) {
    const ul = element("ul");
    values.forEach(function (value) {
      ul.appendChild(element("li", "", value));
    });
    card.appendChild(ul);
  } else {
    card.appendChild(element("p", "", tr("noData")));
  }
  return card;
}

function renderProgress(progress) {
  if (!progress) {
    renderEmptyProgress();
    return;
  }
  state.progress = progress;
  if (state.user) {
    state.user.xp = finite(progress.xp, state.user.xp);
    updateAuthUi();
  }
  clear(dom.progressOutput);
  const stats = element("div", "progress-stats");
  [
    [tr("xp"), formatScore(progress.xp) + " XP"],
    [tr("completedTasks"), String(finite(progress.completedChallenges))],
    [tr("totalAttempts"), String(finite(progress.totalAttempts))],
    [tr("averageScore"), formatScore(progress.averageScore)],
    [tr("currentStreak"), String(finite(progress.currentStreak)) + " " + tr("days")]
  ].forEach(function (entry) {
    const card = element("div", "stat-card");
    card.appendChild(element("span", "stat-label", entry[0]));
    card.appendChild(element("strong", "stat-value", entry[1]));
    stats.appendChild(card);
  });
  dom.progressOutput.appendChild(stats);

  const layout = element("div", "progress-layout");
  const trendCard = element("section", "trend-card");
  trendCard.appendChild(element("h4", "", tr("scoreTrend")));
  const chart = element("div", "trend-chart");
  const days = Array.isArray(progress.last7Days) ? progress.last7Days : [];
  days.forEach(function (day) {
    const column = element("div", "trend-column");
    const bar = element("div", "trend-bar");
    const score = finite(day.averageScore);
    const activityHeight = score > 0 ? score : Math.min(20, finite(day.attempts) * 8);
    bar.style.height = Math.max(3, activityHeight) + "%";
    bar.title = formatScore(score) + " / 100 · " + finite(day.attempts) + " " + tr("attempts");
    column.appendChild(bar);
    column.appendChild(element("span", "", cleanText(day.date).slice(5)));
    chart.appendChild(column);
  });
  if (!days.length) {
    chart.appendChild(element("p", "", tr("noData")));
  }
  trendCard.appendChild(chart);

  const planCard = element("section", "plan-card");
  planCard.appendChild(element("h4", "", tr("sevenDayPlan")));
  const planList = element("ol", "training-plan");
  const plan = Array.isArray(progress.trainingPlan) ? progress.trainingPlan : [];
  plan.forEach(function (item) {
    const row = element("li");
    row.dataset.complete = item.completed ? "true" : "false";
    row.appendChild(element("span", "plan-day", String(item.day)));
    const details = element("div");
    details.appendChild(element("strong", "", cleanText(item.focus, tr("noData"))));
    details.appendChild(element("small", "", item.completed ? tr("planDone") : tr("planTodo")));
    row.appendChild(details);
    planList.appendChild(row);
  });
  planCard.appendChild(planList);
  layout.appendChild(trendCard);
  layout.appendChild(planCard);
  dom.progressOutput.appendChild(layout);

  const priorityGrid = element("div", "feedback-priority-grid");
  priorityGrid.appendChild(feedbackCard("feedback-card feedback-card--improve", tr("weakFocus"),
    [cleanText(progress.weakestDimension, tr("noData"))]));
  priorityGrid.appendChild(feedbackCard("feedback-card feedback-card--strength", tr("recommendations"),
    list(progress.recommendations)));
  dom.progressOutput.appendChild(priorityGrid);
  updateControls();
}

function renderHistory() {
  clear(dom.historyOutput);
  if (!state.user) {
    dom.historyOutput.appendChild(emptyState(tr("noHistory"), tr("signInProgressCopy")));
    populateCompareOptions([]);
    return;
  }
  if (!state.history.length) {
    dom.historyOutput.appendChild(emptyState(tr("noHistory"), tr("noHistoryCopy")));
    populateCompareOptions([]);
    return;
  }
  const completedAttempts = [];
  state.history.forEach(function (entry) {
    const challenge = entry.challenge;
    const attempts = entry.attempts || [];
    const details = element("details", "history-item");
    details.open = Boolean(state.challenge && state.challenge.id === challenge.id);
    const summary = element("summary");
    const title = element("div");
    title.appendChild(element("span", "history-item-title", cleanText(challenge.title, tr("challengeBrief"))));
    title.appendChild(element("span", "history-item-meta",
      difficultyName(challenge.difficulty) + " · " + attempts.length + " " + tr("attempts") +
      " · " + formatDate(challenge.createdAt)));
    summary.appendChild(title);
    const completed = attempts.filter(function (attempt) {
      return attempt.status === "COMPLETED" && attempt.evaluation;
    });
    const best = completed.reduce(function (value, attempt) {
      return Math.max(value, finite(attempt.evaluation.finalScore));
    }, 0);
    summary.appendChild(element("span", "history-best",
      completed.length ? tr("best") + " " + formatScore(best) : tr("noAttempts")));
    details.appendChild(summary);
    const attemptList = element("ul", "attempt-list");
    if (!attempts.length) {
      attemptList.appendChild(element("li", "", tr("noAttempts")));
    }
    attempts.forEach(function (attempt, index) {
      const row = element("li", "attempt-row");
      row.setAttribute("data-testid", "history-attempt");
      const when = element("div");
      when.appendChild(element("strong", "", tr("attempt", { value: attempts.length - index })));
      when.appendChild(element("small", "history-item-meta", formatDate(attempt.submittedAt)));
      row.appendChild(when);
      row.appendChild(element("span", "attempt-status",
        attempt.evaluation ? formatScore(attempt.evaluation.finalScore) : statusName(attempt.status)));
      const view = element("button", "button button--small", tr("viewAttempt"));
      view.type = "button";
      view.addEventListener("click", function () {
        state.challenge = challenge;
        state.submission = attempt;
        state.evaluation = attempt.evaluation || null;
        renderChallenge(challenge, true);
        if (attempt.evaluation) {
          renderEvaluation(attempt.evaluation);
          dom.submissionOutput.scrollIntoView({ behavior: "smooth", block: "start" });
        }
        queuePersist();
      });
      row.appendChild(view);
      attemptList.appendChild(row);
      if (attempt.status === "COMPLETED" && attempt.evaluation) {
        completedAttempts.push({
          challengeId: challenge.id,
          challengeTitle: challenge.title,
          attempt: attempt
        });
      }
    });
    details.appendChild(attemptList);
    dom.historyOutput.appendChild(details);
  });
  populateCompareOptions(completedAttempts);
  updateCurrentSummary();
}

function statusName(status) {
  const value = cleanText(status).toUpperCase();
  if (value === "PENDING") {
    return tr("pending");
  }
  if (value === "PROCESSING") {
    return tr("processing");
  }
  if (value === "COMPLETED") {
    return tr("completed");
  }
  if (value === "FAILED") {
    return tr("failed");
  }
  return tr("unknown");
}

function populateCompareOptions(items) {
  clear(dom.compareFirst);
  clear(dom.compareSecond);
  items.forEach(function (item) {
    const attempt = item.attempt;
    const label = cleanText(item.challengeTitle, tr("challengeBrief")) + " · " +
      formatDate(attempt.submittedAt) + " · " + formatScore(attempt.evaluation.finalScore);
    [dom.compareFirst, dom.compareSecond].forEach(function (select) {
      const option = element("option", "", label);
      option.value = String(attempt.submissionId);
      option.dataset.challengeId = String(item.challengeId);
      select.appendChild(option);
    });
  });
  const group = findComparablePair(items);
  if (group) {
    dom.compareFirst.value = String(group[0].attempt.submissionId);
    dom.compareSecond.value = String(group[group.length - 1].attempt.submissionId);
  }
  const enabled = Boolean(group);
  dom.compareFirst.disabled = !enabled;
  dom.compareSecond.disabled = !enabled;
  dom.compareBtn.disabled = !enabled;
}

function findComparablePair(items) {
  const groups = new Map();
  items.forEach(function (item) {
    if (!groups.has(item.challengeId)) {
      groups.set(item.challengeId, []);
    }
    groups.get(item.challengeId).push(item);
  });
  for (const group of groups.values()) {
    if (group.length >= 2) {
      return group.slice().sort(function (a, b) {
        return new Date(a.attempt.submittedAt) - new Date(b.attempt.submittedAt);
      });
    }
  }
  return null;
}

function updateCurrentSummary() {
  if (!state.challenge) {
    return;
  }
  const entry = state.history.find(function (item) {
    return item.challenge.id === state.challenge.id;
  });
  const attempts = entry ? entry.attempts : [];
  dom.summaryAttempts.textContent = String(attempts.length);
  const completed = attempts.filter(function (attempt) {
    return attempt.evaluation;
  });
  const best = completed.reduce(function (value, attempt) {
    return Math.max(value, finite(attempt.evaluation.finalScore));
  }, 0);
  dom.summaryBest.textContent = completed.length ? formatScore(best) : "—";
}

async function refreshSession() {
  try {
    state.user = await api("/api/auth/me");
    updateAuthUi();
    restoreWorkspace();
    await loadDashboard();
  } catch (error) {
    if (!(error instanceof ApiError) || error.status !== 401) {
      setStatus(dom.globalStatus, errorMessage(error), "warning");
    }
    state.user = null;
    updateAuthUi();
  }
}

function openAuth(mode) {
  activateAuthTab(mode || "login", false);
  setStatus(dom.authStatus, "");
  if (typeof dom.authDialog.showModal === "function") {
    if (!dom.authDialog.open) {
      dom.authDialog.showModal();
    }
  } else {
    dom.authDialog.setAttribute("open", "");
  }
  window.setTimeout(function () {
    (mode === "register" ? dom.registerUsername : dom.loginUsername).focus();
  }, 0);
}

function closeAuth() {
  if (dom.authDialog.open && typeof dom.authDialog.close === "function") {
    dom.authDialog.close();
  } else {
    dom.authDialog.removeAttribute("open");
  }
}

function activateAuthTab(mode, moveFocus) {
  const register = mode === "register";
  dom.loginTab.setAttribute("aria-selected", register ? "false" : "true");
  dom.registerTab.setAttribute("aria-selected", register ? "true" : "false");
  dom.loginTab.tabIndex = register ? -1 : 0;
  dom.registerTab.tabIndex = register ? 0 : -1;
  dom.loginForm.hidden = register;
  dom.registerForm.hidden = !register;
  if (moveFocus) {
    (register ? dom.registerUsername : dom.loginUsername).focus();
  }
}

async function completeAuthentication(user) {
  state.user = user;
  updateAuthUi();
  closeAuth();
  dom.loginPassword.value = "";
  dom.registerPassword.value = "";
  dom.confirmPassword.value = "";
  restoreWorkspace();
  setStatus(dom.globalStatus, tr("signedIn"), "success");
  await loadDashboard();
}

async function handleLogin(event) {
  event.preventDefault();
  if (!dom.loginForm.reportValidity()) {
    return;
  }
  const button = dom.loginForm.querySelector("[type=submit]");
  setBusy("auth", button, true);
  setStatus(dom.authStatus, tr("working"));
  try {
    const user = await api("/api/auth/login", {
      method: "POST",
      body: {
        username: dom.loginUsername.value.trim(),
        password: dom.loginPassword.value
      }
    });
    await completeAuthentication(user);
  } catch (error) {
    setStatus(dom.authStatus, error.status === 401 ? tr("authFailed") : errorMessage(error), "error");
  } finally {
    setBusy("auth", button, false);
  }
}

async function handleRegister(event) {
  event.preventDefault();
  if (!dom.registerForm.reportValidity()) {
    return;
  }
  if (dom.registerPassword.value !== dom.confirmPassword.value) {
    dom.confirmPassword.setAttribute("aria-invalid", "true");
    setStatus(dom.authStatus, tr("passwordsMismatch"), "error");
    return;
  }
  dom.confirmPassword.removeAttribute("aria-invalid");
  const button = dom.registerForm.querySelector("[type=submit]");
  setBusy("auth", button, true);
  setStatus(dom.authStatus, tr("working"));
  try {
    const user = await api("/api/auth/register", {
      method: "POST",
      body: {
        username: dom.registerUsername.value.trim(),
        password: dom.registerPassword.value
      }
    });
    await completeAuthentication(user);
  } catch (error) {
    setStatus(dom.authStatus, errorMessage(error), "error");
  } finally {
    setBusy("auth", button, false);
  }
}

async function handleLogout() {
  const userId = state.user && state.user.id;
  try {
    await api("/api/auth/logout", { method: "POST" });
  } catch (error) {
    if (!(error instanceof ApiError) || error.status !== 401) {
      setStatus(dom.globalStatus, errorMessage(error), "error");
      return;
    }
  }
  if (userId) {
    safeSessionRemove(workspaceKey(userId));
  }
  state.user = null;
  state.progress = null;
  state.history = [];
  clearWorkspace(false);
  renderEmptyProgress();
  renderHistory();
  dom.profileDropdown.hidden = true;
  updateAuthUi();
  setStatus(dom.globalStatus, tr("loggedOut"), "success");
}

function parseLines(value) {
  const values = value.split(/\r?\n/).map(function (item) {
    return item.trim();
  }).filter(Boolean);
  if (values.length > 10 || values.some(function (item) {
    return item.length > 400;
  })) {
    throw new ApiError(tr("listTooLong"), 400, "VALIDATION");
  }
  return values;
}

function generationPayload() {
  return {
    difficulty: dom.difficulty.value,
    roleTrack: dom.roleTrack.value || null,
    challengeType: dom.challengeType.value.trim() || null,
    focusGoal: dom.focusGoal.value.trim() || null,
    businessContext: dom.businessContext.value.trim() || null,
    customRequirements: parseLines(dom.customRequirements.value),
    customConstraints: parseLines(dom.customConstraints.value),
    customAcceptanceCriteria: parseLines(dom.customAcceptanceCriteria.value)
  };
}

async function handleGenerate(event) {
  if (event) {
    event.preventDefault();
  }
  if (!state.user) {
    setStatus(dom.globalStatus, tr("signedOut"), "warning");
    openAuth("login");
    return;
  }
  if (!dom.generateForm.reportValidity()) {
    return;
  }
  setBusy("generate", dom.generateBtn, true);
  setStatus(dom.challengeStatus, tr("generating"));
  dom.challengeOutput.setAttribute("aria-busy", "true");
  try {
    const challenge = await api("/api/challenge/generate", {
      method: "POST",
      body: generationPayload(),
      timeoutMs: 90000
    });
    state.challenge = challenge;
    state.submission = null;
    state.evaluation = null;
    state.pendingKey = null;
    state.pendingFingerprint = null;
    dom.submissionAnswer.value = "";
    renderChallenge(challenge, true);
    renderEmptyEvaluation();
    updateAnswerCount();
    setStatus(dom.challengeStatus, tr("generated"), "success");
    loadHistory().catch(function () {
      return;
    });
  } catch (error) {
    setStatus(dom.challengeStatus, errorMessage(error), "error");
  } finally {
    dom.challengeOutput.removeAttribute("aria-busy");
    setBusy("generate", dom.generateBtn, false);
  }
}

async function handleNextWeakness() {
  if (!state.user) {
    openAuth("login");
    return;
  }
  setBusy("generate", dom.nextWeaknessBtn, true);
  setStatus(dom.challengeStatus, tr("generating"));
  try {
    const challenge = await api("/api/training/next", {
      method: "POST",
      timeoutMs: 90000
    });
    state.challenge = challenge;
    state.submission = null;
    state.evaluation = null;
    dom.submissionAnswer.value = "";
    renderChallenge(challenge, true);
    renderEmptyEvaluation();
    updateAnswerCount();
    setStatus(dom.challengeStatus, tr("nextGenerated"), "success");
    loadHistory().catch(function () {
      return;
    });
  } catch (error) {
    setStatus(dom.challengeStatus, errorMessage(error), "error");
  } finally {
    setBusy("generate", dom.nextWeaknessBtn, false);
  }
}

function applyPreset(name) {
  state.selectedPreset = name;
  document.querySelectorAll("[data-preset]").forEach(function (button) {
    button.setAttribute("aria-pressed", button.dataset.preset === name ? "true" : "false");
  });
  if (name === "pm") {
    dom.difficulty.value = "INTERMEDIATE";
    dom.roleTrack.value = "PM";
    dom.challengeType.value = "PRD";
    dom.focusGoal.value = state.locale === "zh" ? "优先级、指标与发布计划" : "Prioritization, metrics, and rollout";
  } else if (name === "api") {
    dom.difficulty.value = "INTERMEDIATE";
    dom.roleTrack.value = "PM + SDE";
    dom.challengeType.value = "API Design";
    dom.focusGoal.value = state.locale === "zh" ? "契约、幂等、安全与错误处理" : "Contracts, idempotency, security, and errors";
  } else {
    dom.difficulty.value = "ADVANCED";
    dom.roleTrack.value = "SDE";
    dom.challengeType.value = "System Design";
    dom.focusGoal.value = state.locale === "zh" ? "扩展性、可靠性与架构权衡" : "Scalability, reliability, and tradeoffs";
  }
  queuePersist();
}

function exampleAnswer() {
  const type = cleanText(state.challenge && state.challenge.challengeType).toLowerCase();
  if (state.locale === "zh") {
    if (type.includes("prd") || type.includes("product")) {
      return "# 问题与目标用户\n明确要解决的问题、目标用户和证据。\n\n# 目标与非目标\n列出成功指标、范围边界和关键假设。\n\n# 方案\n说明用户流程、核心需求、优先级和验收标准。\n\n# 风险与发布\n覆盖依赖、边界情况、灰度计划、监控和回滚。";
    }
    if (type.includes("api")) {
      return "# 用例与资源\n定义调用方、资源模型和权限边界。\n\n# 接口契约\n列出端点、请求响应、分页和版本策略。\n\n# 可靠性与安全\n说明幂等、错误码、限流、认证、重试和可观测性。\n\n# 权衡\n比较同步与异步设计，并给出发布和回滚方案。";
    }
    return "# 需求与规模\n确认用户、流量、延迟、可用性和数据一致性目标。\n\n# 高层架构\n说明核心服务、存储、队列、缓存和关键调用链。\n\n# 可靠性与安全\n覆盖超时、重试、幂等、降级、权限和数据保护。\n\n# 权衡与上线\n解释替代方案、容量验证、监控、灰度和回滚。";
  }
  if (type.includes("prd") || type.includes("product")) {
    return "# Problem and target user\nDefine the user, evidence, and the problem worth solving.\n\n# Goals and non-goals\nState measurable outcomes, scope boundaries, and assumptions.\n\n# Solution\nDescribe the journey, prioritized requirements, and acceptance criteria.\n\n# Risk and rollout\nCover dependencies, edge cases, staged rollout, monitoring, and rollback.";
  }
  if (type.includes("api")) {
    return "# Use cases and resources\nDefine callers, resource models, and authorization boundaries.\n\n# Contract\nList endpoints, requests, responses, pagination, and versioning.\n\n# Reliability and security\nCover idempotency, errors, rate limits, authentication, retries, and observability.\n\n# Tradeoffs\nCompare synchronous and asynchronous options, rollout, and rollback.";
  }
  return "# Requirements and scale\nConfirm users, traffic, latency, availability, and consistency needs.\n\n# High-level architecture\nDescribe services, storage, queues, caches, and critical flows.\n\n# Reliability and security\nCover timeouts, retries, idempotency, degradation, authorization, and data protection.\n\n# Tradeoffs and delivery\nExplain alternatives, capacity tests, monitoring, canary rollout, and rollback.";
}

function loadExampleAnswer() {
  dom.submissionAnswer.value = exampleAnswer().slice(0, MAX_ANSWER_LENGTH);
  updateAnswerCount();
  queuePersist();
  dom.submissionAnswer.focus();
}

function fingerprintFor(answer) {
  return String(state.challenge.id) + ":" + answer;
}

function newIdempotencyKey() {
  if (window.crypto && typeof window.crypto.randomUUID === "function") {
    return window.crypto.randomUUID();
  }
  return "web-" + Date.now().toString(36) + "-" + Math.random().toString(36).slice(2);
}

async function handleSubmission(event) {
  event.preventDefault();
  if (!state.user) {
    openAuth("login");
    return;
  }
  if (!state.challenge) {
    setStatus(dom.submissionStatus, tr("noChallengeCopy"), "error");
    return;
  }
  const answer = dom.submissionAnswer.value.trim();
  if (answer.length < MIN_ANSWER_LENGTH) {
    dom.submissionAnswer.setAttribute("aria-invalid", "true");
    setStatus(dom.submissionStatus, tr("answerTooShort"), "error");
    return;
  }
  const fingerprint = fingerprintFor(answer);
  if (!state.pendingKey || state.pendingFingerprint !== fingerprint) {
    state.pendingKey = newIdempotencyKey();
    state.pendingFingerprint = fingerprint;
  }
  setBusy("submit", dom.submitBtn, true);
  setStatus(dom.submissionStatus, tr("submitting"));
  dom.submissionOutput.setAttribute("aria-busy", "true");
  queuePersist();
  try {
    const accepted = await api("/api/submissions", {
      method: "POST",
      headers: { "Idempotency-Key": state.pendingKey },
      body: {
        challengeId: state.challenge.id,
        answer: answer,
        idempotencyKey: state.pendingKey
      }
    });
    state.submission = accepted;
    await pollSubmission(accepted.submissionId);
  } catch (error) {
    setStatus(dom.submissionStatus, errorMessage(error), "error");
  } finally {
    dom.submissionOutput.removeAttribute("aria-busy");
    setBusy("submit", dom.submitBtn, false);
    queuePersist();
  }
}

async function pollSubmission(submissionId) {
  const started = Date.now();
  let delay = 250;
  while (Date.now() - started < POLL_TIMEOUT_MS) {
    const current = await api("/api/submissions/" + encodeURIComponent(submissionId), {
      timeoutMs: 20000
    });
    state.submission = current;
    setStatus(dom.submissionStatus, statusName(current.status));
    if (current.status === "COMPLETED" && current.evaluation) {
      state.evaluation = current.evaluation;
      state.pendingKey = null;
      state.pendingFingerprint = null;
      renderEvaluation(current.evaluation);
      setStatus(dom.submissionStatus, tr("evaluationReady"), "success");
      await Promise.allSettled([loadProgress(), loadHistory()]);
      return current;
    }
    if (current.status === "FAILED") {
      state.pendingKey = null;
      state.pendingFingerprint = null;
      throw new ApiError(cleanText(current.errorMessage, tr("evaluationFailed")), 502);
    }
    await new Promise(function (resolve) {
      window.setTimeout(resolve, delay);
    });
    delay = Math.min(2000, Math.round(delay * 1.5));
  }
  throw new ApiError(tr("pollTimeout"), 0, "POLL_TIMEOUT");
}

async function loadProgress() {
  if (!state.user || state.busy.has("progress")) {
    return;
  }
  setBusy("progress", dom.refreshProgressBtn, true);
  setStatus(dom.progressStatus, tr("loadProgress"));
  try {
    const progress = await api("/api/user/me/progress");
    renderProgress(progress);
    setStatus(dom.progressStatus, "");
  } catch (error) {
    setStatus(dom.progressStatus, errorMessage(error), "error");
  } finally {
    setBusy("progress", dom.refreshProgressBtn, false);
  }
}

async function loadHistory() {
  if (!state.user || state.busy.has("history")) {
    return;
  }
  setBusy("history", dom.refreshHistoryBtn, true);
  setStatus(dom.historyStatus, tr("loadHistory"));
  try {
    const challenges = await api("/api/challenges");
    const entries = await Promise.all((Array.isArray(challenges) ? challenges : []).slice(0, 50)
      .map(async function (challenge) {
        try {
          const attempts = await api("/api/challenges/" + encodeURIComponent(challenge.id) + "/attempts");
          return {
            challenge: challenge,
            attempts: Array.isArray(attempts) ? attempts : []
          };
        } catch (error) {
          return { challenge: challenge, attempts: [] };
        }
      }));
    state.history = entries;
    renderHistory();
    setStatus(dom.historyStatus, "");
  } catch (error) {
    setStatus(dom.historyStatus, errorMessage(error), "error");
  } finally {
    setBusy("history", dom.refreshHistoryBtn, false);
  }
}

async function loadDashboard() {
  await Promise.allSettled([loadProgress(), loadHistory()]);
}

async function handleCompare() {
  const firstId = Number(dom.compareFirst.value);
  const secondId = Number(dom.compareSecond.value);
  const firstOption = dom.compareFirst.selectedOptions[0];
  const secondOption = dom.compareSecond.selectedOptions[0];
  if (!firstId || !secondId || firstId === secondId ||
      !firstOption || !secondOption ||
      firstOption.dataset.challengeId !== secondOption.dataset.challengeId) {
    setStatus(dom.compareStatus, tr("chooseSameChallenge"), "error");
    return;
  }
  setBusy("compare", dom.compareBtn, true);
  setStatus(dom.compareStatus, tr("working"));
  try {
    const result = await api("/api/attempts/compare?firstId=" +
      encodeURIComponent(firstId) + "&secondId=" + encodeURIComponent(secondId));
    renderComparison(result);
    setStatus(dom.compareStatus, tr("compareReady"), "success");
  } catch (error) {
    setStatus(dom.compareStatus, errorMessage(error), "error");
  } finally {
    setBusy("compare", dom.compareBtn, false);
  }
}

function renderComparison(result) {
  clear(dom.compareOutput);
  const scores = element("div", "compare-score");
  const first = element("div");
  first.appendChild(element("small", "", tr("earlierAttempt")));
  first.appendChild(element("strong", "", formatScore(result.first.evaluation.finalScore)));
  const delta = element("div", finite(result.scoreDelta) >= 0 ? "delta-positive" : "delta-negative");
  delta.appendChild(element("small", "", tr("scoreDelta")));
  const deltaValue = finite(result.scoreDelta);
  delta.appendChild(element("strong", "", (deltaValue > 0 ? "+" : "") + formatScore(deltaValue)));
  const second = element("div");
  second.appendChild(element("small", "", tr("laterAttempt")));
  second.appendChild(element("strong", "", formatScore(result.second.evaluation.finalScore)));
  scores.appendChild(first);
  scores.appendChild(delta);
  scores.appendChild(second);
  dom.compareOutput.appendChild(scores);
  const details = element("section", "result-section");
  details.appendChild(element("h4", "", tr("rubricDetails")));
  const values = result.rubricDeltas && typeof result.rubricDeltas === "object"
    ? result.rubricDeltas
    : {};
  const ul = element("ul");
  Object.keys(values).forEach(function (key) {
    const value = finite(values[key]);
    ul.appendChild(element("li", value >= 0 ? "delta-positive" : "delta-negative",
      rubricName(key) + ": " + (value > 0 ? "+" : "") + formatScore(value)));
  });
  details.appendChild(ul);
  dom.compareOutput.appendChild(details);
}

async function copyChallenge() {
  const text = challengeAsText();
  if (!text) {
    return;
  }
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text);
    } else {
      const area = element("textarea");
      area.value = text;
      area.style.position = "fixed";
      area.style.left = "-9999px";
      document.body.appendChild(area);
      area.select();
      document.execCommand("copy");
      area.remove();
    }
    setStatus(dom.challengeStatus, tr("copied"), "success");
  } catch (error) {
    setStatus(dom.challengeStatus, tr("copyFailed"), "error");
  }
}

function download(name, content, type) {
  const blob = new Blob([content], { type: type || "text/plain;charset=utf-8" });
  const url = URL.createObjectURL(blob);
  const link = element("a");
  link.href = url;
  link.download = name;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.setTimeout(function () {
    URL.revokeObjectURL(url);
  }, 0);
}

function downloadFeedback() {
  if (!state.evaluation || !state.challenge) {
    return;
  }
  const lines = [
    "# " + cleanText(state.challenge.title, tr("challengeBrief")),
    "",
    tr("score") + ": " + formatScore(state.evaluation.finalScore) + " / 100",
    tr("provider") + ": " + providerName(state.evaluation.provider),
    "",
    "## " + tr("strengths")
  ];
  list(state.evaluation.strengths).forEach(function (item) {
    lines.push("- " + item);
  });
  lines.push("", "## " + tr("improvements"));
  list(state.evaluation.improvements).forEach(function (item) {
    lines.push("- " + item);
  });
  lines.push("", "## " + tr("feedback"), cleanText(state.evaluation.feedback));
  lines.push("", "## " + tr("exampleOutline"), cleanText(state.evaluation.exampleOutline));
  download("career-quest-feedback.md", lines.join("\n"), "text/markdown;charset=utf-8");
  setStatus(dom.submissionStatus, tr("feedbackDownloaded"), "success");
}

async function exportAccountData() {
  try {
    const data = await api("/api/account/export");
    const date = new Date().toISOString().slice(0, 10);
    download("career-quest-data-" + date + ".json", JSON.stringify(data, null, 2),
      "application/json;charset=utf-8");
    dom.profileDropdown.hidden = true;
    setStatus(dom.globalStatus, tr("exportReady"), "success");
  } catch (error) {
    setStatus(dom.globalStatus, errorMessage(error), "error");
  }
}

function askConfirmation(title, message) {
  dom.confirmTitle.textContent = title;
  dom.confirmMessage.textContent = message;
  if (typeof dom.confirmDialog.showModal === "function") {
    dom.confirmDialog.showModal();
  } else {
    dom.confirmDialog.setAttribute("open", "");
  }
  return new Promise(function (resolve) {
    confirmResolver = resolve;
  });
}

function resolveConfirmation(value) {
  if (dom.confirmDialog.open && typeof dom.confirmDialog.close === "function") {
    dom.confirmDialog.close();
  } else {
    dom.confirmDialog.removeAttribute("open");
  }
  if (confirmResolver) {
    const resolve = confirmResolver;
    confirmResolver = null;
    resolve(value);
  }
}

async function clearCurrentTraining() {
  const confirmed = await askConfirmation(tr("confirmClearTitle"), tr("confirmClearBody"));
  if (!confirmed) {
    return;
  }
  clearWorkspace(true);
  setStatus(dom.globalStatus, tr("cleared"), "success");
}

async function deleteAccount() {
  const confirmed = await askConfirmation(tr("confirmDeleteTitle"), tr("confirmDeleteBody"));
  if (!confirmed) {
    return;
  }
  const userId = state.user && state.user.id;
  try {
    await api("/api/account", { method: "DELETE" });
    if (userId) {
      safeSessionRemove(workspaceKey(userId));
    }
    state.user = null;
    state.progress = null;
    state.history = [];
    clearWorkspace(false);
    renderEmptyProgress();
    renderHistory();
    dom.profileDropdown.hidden = true;
    updateAuthUi();
    setStatus(dom.globalStatus, tr("accountDeleted"), "success");
  } catch (error) {
    setStatus(dom.globalStatus, errorMessage(error), "error");
  }
}

function retryChallenge() {
  state.evaluation = null;
  state.submission = null;
  state.pendingKey = null;
  state.pendingFingerprint = null;
  renderEmptyEvaluation();
  setStatus(dom.submissionStatus, "");
  updateSteps(3);
  dom.submissionAnswer.focus();
  dom.submissionAnswer.scrollIntoView({ behavior: "smooth", block: "center" });
  queuePersist();
}

function bindEvents() {
  dom.localeToggle.addEventListener("click", function () {
    state.locale = state.locale === "zh" ? "en" : "zh";
    safeLocalSet(LOCALE_KEY, state.locale);
    applyLocale();
  });
  dom.openAuthBtn.addEventListener("click", function () {
    openAuth("login");
  });
  dom.closeAuthBtn.addEventListener("click", closeAuth);
  dom.loginTab.addEventListener("click", function () {
    activateAuthTab("login", true);
  });
  dom.registerTab.addEventListener("click", function () {
    activateAuthTab("register", true);
  });
  [dom.loginTab, dom.registerTab].forEach(function (tab) {
    tab.addEventListener("keydown", function (event) {
      if (event.key === "ArrowRight" || event.key === "ArrowLeft") {
        event.preventDefault();
        activateAuthTab(tab === dom.loginTab ? "register" : "login", true);
      }
    });
  });
  dom.loginForm.addEventListener("submit", handleLogin);
  dom.registerForm.addEventListener("submit", handleRegister);
  dom.profileMenuBtn.addEventListener("click", function () {
    dom.profileDropdown.hidden = !dom.profileDropdown.hidden;
    dom.profileMenuBtn.setAttribute("aria-expanded", dom.profileDropdown.hidden ? "false" : "true");
  });
  document.addEventListener("click", function (event) {
    if (!dom.signedInActions.contains(event.target)) {
      dom.profileDropdown.hidden = true;
      dom.profileMenuBtn.setAttribute("aria-expanded", "false");
    }
  });
  dom.logoutBtn.addEventListener("click", handleLogout);
  dom.exportDataBtn.addEventListener("click", exportAccountData);
  dom.deleteAccountBtn.addEventListener("click", deleteAccount);
  dom.generateForm.addEventListener("submit", handleGenerate);
  dom.nextWeaknessBtn.addEventListener("click", handleNextWeakness);
  document.querySelectorAll("[data-preset]").forEach(function (button) {
    button.addEventListener("click", function () {
      applyPreset(button.dataset.preset);
    });
  });
  dom.copyChallengeBtn.addEventListener("click", copyChallenge);
  dom.clearTrainingBtn.addEventListener("click", clearCurrentTraining);
  dom.exampleAnswerBtn.addEventListener("click", loadExampleAnswer);
  dom.submissionForm.addEventListener("submit", handleSubmission);
  dom.submissionAnswer.addEventListener("input", function () {
    updateAnswerCount();
    queuePersist();
  });
  [
    dom.difficulty,
    dom.roleTrack,
    dom.challengeType,
    dom.focusGoal,
    dom.businessContext,
    dom.customRequirements,
    dom.customConstraints,
    dom.customAcceptanceCriteria
  ].forEach(function (field) {
    field.addEventListener("input", queuePersist);
    field.addEventListener("change", queuePersist);
  });
  dom.downloadFeedbackBtn.addEventListener("click", downloadFeedback);
  dom.retryChallengeBtn.addEventListener("click", retryChallenge);
  dom.refreshProgressBtn.addEventListener("click", loadProgress);
  dom.refreshHistoryBtn.addEventListener("click", loadHistory);
  dom.compareBtn.addEventListener("click", handleCompare);
  dom.confirmCancelBtn.addEventListener("click", function () {
    resolveConfirmation(false);
  });
  dom.confirmActionBtn.addEventListener("click", function () {
    resolveConfirmation(true);
  });
  dom.confirmDialog.addEventListener("cancel", function (event) {
    event.preventDefault();
    resolveConfirmation(false);
  });
  window.addEventListener("online", updateOnlineState);
  window.addEventListener("offline", updateOnlineState);
  window.addEventListener("pagehide", persistWorkspace);
  document.addEventListener("keydown", function (event) {
    if (!(event.ctrlKey || event.metaKey) || event.key !== "Enter") {
      return;
    }
    if (document.activeElement === dom.submissionAnswer) {
      event.preventDefault();
      dom.submissionForm.requestSubmit();
    } else if (dom.generateForm.contains(document.activeElement)) {
      event.preventDefault();
      dom.generateForm.requestSubmit();
    }
  });
}

async function initialize() {
  bindEvents();
  updateOnlineState();
  restoreWorkspace();
  applyLocale();
  updateControls();
  await refreshSession();
}

initialize().catch(function (error) {
  setStatus(dom.globalStatus, errorMessage(error), "error");
});
