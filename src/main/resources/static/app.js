const state = {
  userId: null,
  challengeId: null
};

const output = {
  challenge: document.getElementById("challengeOutput"),
  submission: document.getElementById("submissionOutput")
};

const fields = {
  difficulty: document.getElementById("difficulty"),
  roleTrack: document.getElementById("roleTrack"),
  challengeType: document.getElementById("challengeType"),
  focusGoal: document.getElementById("focusGoal"),
  businessContext: document.getElementById("businessContext"),
  customRequirements: document.getElementById("customRequirements"),
  submissionAnswer: document.getElementById("submissionAnswer")
};

const buttons = {
  generate: document.getElementById("generateBtn"),
  submit: document.getElementById("submitBtn")
};

const STORAGE_KEY = "career-training-ui-state";

function splitLines(value) {
  return value
    .split("\n")
    .map((line) => line.trim())
    .filter(Boolean);
}

function loadState() {
  try {
    const saved = window.sessionStorage.getItem(STORAGE_KEY);
    if (!saved) {
      return;
    }

    const parsed = JSON.parse(saved);
    state.userId = parsed.userId || null;
    state.challengeId = parsed.challengeId || null;
  } catch (error) {
    state.userId = null;
    state.challengeId = null;
  }
}

function persistState() {
  window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify({
    userId: state.userId,
    challengeId: state.challengeId
  }));
}

function setBusy(button, busy, text) {
  if (!button) {
    return;
  }
  button.disabled = busy;
  if (busy) {
    button.dataset.originalLabel = button.textContent;
    button.textContent = text;
  } else if (button.dataset.originalLabel) {
    button.textContent = button.dataset.originalLabel;
  }
}

async function parseError(response) {
  let message = `Request failed with ${response.status}`;

  try {
    const contentType = response.headers.get("content-type") || "";
    if (contentType.includes("application/json")) {
      const payload = await response.json();
      message = payload.message || payload.error || message;
    } else {
      const text = await response.text();
      if (text.trim()) {
        message = text.trim();
      }
    }
  } catch (error) {
    message = `${message}: ${error.message}`;
  }

  return message;
}

async function requestJson(url, options = {}) {
  const response = await fetch(url, options);
  if (!response.ok) {
    throw new Error(await parseError(response));
  }
  return response.json();
}

function formatSection(title, items) {
  if (!items || items.length === 0) {
    return "";
  }

  return `## ${title}\n${items.map((item) => `- ${item}`).join("\n")}\n`;
}

function formatChallengeMarkdown(challenge) {
  const lines = [
    `# ${challenge.title}`,
    "",
    `**Difficulty:** ${challenge.difficulty}`,
    `**Expected Output:** ${challenge.expectedOutputFormat}`,
    ""
  ];

  if (challenge.context) {
    lines.push("## Context", challenge.context, "");
  }

  const requirements = formatSection("Requirements", challenge.requirements);
  const constraints = formatSection("Constraints", challenge.constraints);
  const acceptanceCriteria = formatSection("Acceptance Criteria", challenge.acceptanceCriteria);

  if (requirements) {
    lines.push(requirements.trimEnd(), "");
  }
  if (constraints) {
    lines.push(constraints.trimEnd(), "");
  }
  if (acceptanceCriteria) {
    lines.push(acceptanceCriteria.trimEnd(), "");
  }

  return lines.join("\n").trim();
}

function labelizeScoreKey(key) {
  return key
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function formatSubmissionMarkdown(result) {
  const lines = [
    "# Evaluation Result",
    "",
    `**Final Score:** ${result.finalScore}`,
    `**Salary Tier:** ${result.salaryTitle} (${result.salaryTier})`,
    `**Submitted At:** ${result.submittedAt}`,
    ""
  ];

  if (result.rubricScores && Object.keys(result.rubricScores).length > 0) {
    lines.push("## Rubric Breakdown");
    Object.entries(result.rubricScores).forEach(([key, value]) => {
      lines.push(`- ${labelizeScoreKey(key)}: ${value}`);
    });
    lines.push("");
  }

  if (result.feedback) {
    lines.push("## Feedback", result.feedback, "");
  }

  if (result.improvementTrack) {
    lines.push("## Next Focus", result.improvementTrack, "");
  }

  return lines.join("\n").trim();
}

async function ensureUserId() {
  if (state.userId) {
    return state.userId;
  }

  const username = `web_user_${Date.now()}`;
  const data = await requestJson("/api/user", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username })
  });

  state.userId = data.id;
  persistState();
  return state.userId;
}

buttons.generate?.addEventListener("click", async () => {
  const payload = {
    difficulty: fields.difficulty.value,
    roleTrack: fields.roleTrack.value || null,
    challengeType: fields.challengeType.value.trim() || null,
    focusGoal: fields.focusGoal.value.trim() || null,
    businessContext: fields.businessContext.value.trim() || null,
    customRequirements: splitLines(fields.customRequirements.value),
    customConstraints: [],
    customAcceptanceCriteria: []
  };

  setBusy(buttons.generate, true, "Generating...");
  output.challenge.textContent = "Generating quest...";

  try {
    const data = await requestJson("/api/challenge/generate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    state.challengeId = data.id;
    persistState();
    output.challenge.textContent = formatChallengeMarkdown(data);
  } catch (error) {
    output.challenge.textContent = `Failed: ${error.message}`;
  } finally {
    setBusy(buttons.generate, false);
  }
});

buttons.submit?.addEventListener("click", async () => {
  const answer = fields.submissionAnswer.value.trim();

  if (!state.challengeId) {
    output.submission.textContent = "Generate a quest first.";
    return;
  }

  if (answer.length < 30) {
    output.submission.textContent = "Answer must be at least 30 characters.";
    return;
  }

  setBusy(buttons.submit, true, "Submitting...");
  output.submission.textContent = "Submitting answer...";

  try {
    const userId = await ensureUserId();
    const data = await requestJson("/api/submission", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId, challengeId: state.challengeId, answer })
    });

    output.submission.textContent = formatSubmissionMarkdown(data);
  } catch (error) {
    output.submission.textContent = `Failed: ${error.message}`;
  } finally {
    setBusy(buttons.submit, false);
  }
});

loadState();
