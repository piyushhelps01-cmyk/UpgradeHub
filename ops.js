import { initializeApp } from "https://www.gstatic.com/firebasejs/11.10.0/firebase-app.js";
import {
  GoogleAuthProvider,
  getMultiFactorResolver,
  getAuth,
  PhoneAuthProvider,
  PhoneMultiFactorGenerator,
  RecaptchaVerifier,
  onAuthStateChanged,
  signInWithPopup,
  signOut,
  TotpMultiFactorGenerator,
} from "https://www.gstatic.com/firebasejs/11.10.0/firebase-auth.js";
import {
  ReCaptchaEnterpriseProvider,
  initializeAppCheck,
} from "https://www.gstatic.com/firebasejs/11.10.0/firebase-app-check.js";
import {
  getFunctions,
  httpsCallable,
} from "https://www.gstatic.com/firebasejs/11.10.0/firebase-functions.js";
import { appCheckSiteKey, firebaseConfig } from "./firebase-config.js";

const $ = (selector) => document.querySelector(selector);
const errorNode = $("#error");
const signInSection = $("#signin");
const workspace = $("#workspace");
let auth;
let functions;
let pendingMfaResolver;
let pendingPhoneVerificationId;
let pendingPhoneHint;
let recaptchaVerifier;

function showError(message = "") {
  errorNode.textContent = message;
}

function callable(name, data = {}) {
  return httpsCallable(functions, name)(data).then((result) => result.data);
}

function makeTaskCard(task) {
  const card = document.createElement("article");
  card.className = "task";
  const heading = document.createElement("h3");
  heading.textContent = task.title || "Assigned task";
  const detail = document.createElement("p");
  detail.className = "muted";
  detail.textContent = `${task.status || "open"} · Project ${task.projectId || "unassigned"}`;
  const instructions = document.createElement("p");
  instructions.textContent = task.instructions || "No additional instructions.";
  card.append(heading, detail, instructions);
  if (task.status !== "closed") {
    const form = document.createElement("form");
    const field = document.createElement("textarea");
    field.required = true;
    field.maxLength = 2000;
    field.placeholder = "Add a concise progress report or evidence summary";
    const button = document.createElement("button");
    button.type = "submit";
    button.textContent = "Submit report";
    form.append(field, button);
    form.addEventListener("submit", async (event) => {
      event.preventDefault();
      button.disabled = true;
      try {
        await callable("submitEmployeeTaskReport", { taskId: task.id, report: field.value });
        await loadDashboard();
      } catch (error) {
        showError(error.message || "The report could not be submitted.");
      } finally {
        button.disabled = false;
      }
    });
    card.append(form);
  }
  return card;
}

async function loadDashboard() {
  showError("");
  try {
    const token = await auth.currentUser.getIdTokenResult(true);
    if (token.claims.admin === true) {
      workspace.hidden = true;
      signInSection.hidden = true;
      $("#mfa-panel").hidden = true;
      $("#admin-review-section").hidden = false;
      await loadReferralReviewQueue();
      return;
    }
    const data = await callable("getEmployeeDashboard");
    signInSection.hidden = true;
    $("#mfa-panel").hidden = true;
    $("#admin-review-section").hidden = true;
    workspace.hidden = false;
    $("#staff-name").textContent = data.profile?.displayName || "Employee workspace";
    $("#staff-meta").textContent = [
      data.profile?.designation,
      data.profile?.department,
      data.profile?.workEmail,
    ].filter(Boolean).join(" · ");

    const tasks = $("#tasks-list");
    tasks.replaceChildren();
    if (!data.tasks?.length) {
      tasks.textContent = "No tasks are assigned right now.";
    } else {
      for (const task of data.tasks) tasks.append(makeTaskCard(task));
    }
    $("#task-create-section").hidden = data.canAssignTasks !== true;

    const policies = $("#policies-list");
    policies.replaceChildren();
    if (!data.policies?.length) {
      policies.textContent = "No current policy acknowledgement is required.";
    } else {
      for (const policy of data.policies) {
        const card = document.createElement("article");
        card.className = "policy";
        const title = document.createElement("h3");
        title.textContent = policy.title || "Workspace policy";
        const summary = document.createElement("p");
        summary.textContent = policy.summary || "";
        const alreadyAcknowledged = data.acknowledgedPolicyVersions?.includes(`${policy.id}:${policy.version}`);
        const button = document.createElement("button");
        button.className = alreadyAcknowledged ? "secondary" : "";
        button.disabled = alreadyAcknowledged;
        button.textContent = alreadyAcknowledged ? `Acknowledged · v${policy.version}` : `Acknowledge · v${policy.version}`;
        button.addEventListener("click", async () => {
          button.disabled = true;
          try {
            await callable("acknowledgeEmployeePolicy", { policyId: policy.id, version: policy.version });
            await loadDashboard();
          } catch (error) {
            button.disabled = false;
            showError(error.message || "The policy acknowledgement could not be saved.");
          }
        });
        card.append(title, summary, button);
        policies.append(card);
      }
    }
  } catch (error) {
    workspace.hidden = true;
    signInSection.hidden = false;
    showError(error.message || "This account has not been granted operations access.");
  }
}

async function loadReferralReviewQueue() {
  const queue = $("#pending-referrals");
  queue.replaceChildren();
  try {
    const data = await callable("listPendingReferrals");
    if (!data.referrals?.length) {
      queue.textContent = "No referrals are waiting for review.";
      return;
    }
    for (const referral of data.referrals) {
      const card = document.createElement("article");
      card.className = "task";
      const title = document.createElement("h3");
      title.textContent = `Referral · ${referral.qualifiedDays}/15 qualifying days`;
      const detail = document.createElement("p");
      detail.className = "muted";
      detail.textContent = `Learner ${referral.referredUid} · Referrer ${referral.referrerUid}`;
      const reason = document.createElement("textarea");
      reason.required = true;
      reason.minLength = 8;
      reason.maxLength = 500;
      reason.placeholder = "Record the review decision and reason";
      const approve = document.createElement("button");
      approve.type = "button";
      approve.textContent = "Approve · credit 500 XP";
      approve.addEventListener("click", async () => {
        approve.disabled = true;
        try {
          await callable("approveReferral", {
            referredUid: referral.referredUid,
            reason: reason.value,
          });
          await loadReferralReviewQueue();
        } catch (error) {
          approve.disabled = false;
          showError(error.message || "The referral was not approved.");
        }
      });
      card.append(title, detail, reason, approve);
      queue.append(card);
    }
  } catch (error) {
    queue.textContent = "The review queue is unavailable for this account.";
    showError(error.message || "MFA-backed referral review permission is required.");
  }
}

async function beginMfaChallenge(error) {
  if (error?.code !== "auth/multi-factor-auth-required") throw error;
  pendingMfaResolver = getMultiFactorResolver(auth, error);
  const hints = pendingMfaResolver.hints || [];
  const totpHint = hints.find((hint) => hint.factorId === TotpMultiFactorGenerator.FACTOR_ID);
  if (totpHint) {
    pendingPhoneHint = null;
    pendingPhoneVerificationId = null;
    $("#mfa-instructions").textContent = "Enter the current code from your authenticator app.";
    $("#mfa-panel").hidden = false;
    signInSection.hidden = true;
    return;
  }
  pendingPhoneHint = hints.find((hint) => hint.factorId === PhoneMultiFactorGenerator.FACTOR_ID);
  if (!pendingPhoneHint) {
    throw new Error("No supported MFA factor is enrolled for this account.");
  }
  $("#mfa-instructions").textContent = "A verification code will be sent to your enrolled phone.";
  $("#mfa-panel").hidden = false;
  signInSection.hidden = true;
  if (!recaptchaVerifier) {
    recaptchaVerifier = new RecaptchaVerifier(auth, "recaptcha-container", {});
  }
  const provider = new PhoneAuthProvider(auth);
  pendingPhoneVerificationId = await provider.verifyPhoneNumber({
    multiFactorHint: pendingPhoneHint,
    session: pendingMfaResolver.session,
  }, recaptchaVerifier);
  $("#mfa-code").focus();
}

$("#sign-in-button").addEventListener("click", async () => {
  showError("");
  try {
    await signInWithPopup(auth, new GoogleAuthProvider());
  } catch (error) {
    try {
      await beginMfaChallenge(error);
    } catch (mfaError) {
      showError(mfaError.message || "Sign-in could not be completed.");
    }
  }
});

$("#sign-out-button").addEventListener("click", () => signOut(auth));
$("#admin-sign-out-button").addEventListener("click", () => signOut(auth));

$("#mfa-form").addEventListener("submit", async (event) => {
  event.preventDefault();
  if (!pendingMfaResolver) return;
  const code = $("#mfa-code").value.trim();
  try {
    let assertion;
    if (pendingPhoneHint && pendingPhoneVerificationId) {
      const credential = PhoneAuthProvider.credential(pendingPhoneVerificationId, code);
      assertion = PhoneMultiFactorGenerator.assertion(credential);
    } else {
      const totpHint = pendingMfaResolver.hints.find(
        (hint) => hint.factorId === TotpMultiFactorGenerator.FACTOR_ID
      );
      if (!totpHint) throw new Error("No supported MFA factor was selected.");
      assertion = TotpMultiFactorGenerator.assertionForSignIn(totpHint.uid, code);
    }
    await pendingMfaResolver.resolveSignIn(assertion);
    pendingMfaResolver = null;
    pendingPhoneHint = null;
    pendingPhoneVerificationId = null;
    $("#mfa-panel").hidden = true;
  } catch (error) {
    showError(error.message || "The verification code could not be accepted.");
  }
});

$("#task-create-form").addEventListener("submit", async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const values = new FormData(form);
  const dueAtMs = new Date(String(values.get("dueAt"))).getTime();
  try {
    await callable("createEmployeeTask", {
      assigneeUid: String(values.get("assigneeUid")),
      projectId: String(values.get("projectId")),
      title: String(values.get("title")),
      instructions: String(values.get("instructions") || ""),
      dueAtMs,
    });
    form.reset();
    await loadDashboard();
  } catch (error) {
    showError(error.message || "The task could not be assigned.");
  }
});

try {
  if (
    !firebaseConfig?.apiKey ||
    firebaseConfig.apiKey.startsWith("REPLACE_") ||
    !appCheckSiteKey ||
    appCheckSiteKey.startsWith("REPLACE_")
  ) {
    throw new Error("Copy firebase-config.example.js to firebase-config.js and configure the Firebase Web app and App Check site key.");
  }
  const app = initializeApp(firebaseConfig);
  auth = getAuth(app);
  initializeAppCheck(app, {
    provider: new ReCaptchaEnterpriseProvider(appCheckSiteKey),
    isTokenAutoRefreshEnabled: true,
  });
  functions = getFunctions(app, "asia-south1");
  onAuthStateChanged(auth, async (user) => {
    if (!user) {
      signInSection.hidden = false;
      workspace.hidden = true;
      showError("");
      return;
    }
    if (!user.emailVerified) {
      showError("Verify your work email with your identity provider, then sign in again.");
      await signOut(auth);
      return;
    }
    await loadDashboard();
  });
} catch (error) {
  showError(error.message || "The operations portal is not configured.");
}
