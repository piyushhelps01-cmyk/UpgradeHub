import { randomBytes } from "node:crypto";
import { initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import {
  FieldValue,
  Timestamp,
  getFirestore,
  type DocumentReference,
  type Transaction,
} from "firebase-admin/firestore";
import { setGlobalOptions } from "firebase-functions/v2";
import { HttpsError, onCall, type CallableRequest } from "firebase-functions/v2/https";
import {
  isWithinAttributionWindow,
  nextReferralState,
  referralCodeIsValid,
  referralDayQualifies,
  REFERRAL_MINUTES_PER_DAY,
  REFERRAL_REQUIRED_DAYS,
  REFERRAL_XP_REWARD,
  STUDY_SESSION_MAX_MINUTES,
  STUDY_XP_DAILY_CAP,
  studyXpAward,
  utcDayKey,
  utcWeekKey,
  verifiedStudyMinutes,
} from "./domain.js";

initializeApp();
setGlobalOptions({ region: process.env.FUNCTION_REGION || "asia-south1", maxInstances: 20 });

const databaseId = process.env.FIRESTORE_DATABASE_ID;
const db = databaseId && databaseId !== "(default)"
  ? getFirestore(databaseId)
  : getFirestore();
const attributionDays = Number.parseInt(process.env.REFERRAL_ATTRIBUTION_DAYS || "30", 10);

type AuthenticatedRequest = CallableRequest<Record<string, unknown>>;
type StaffRole =
  | "founder"
  | "superAdmin"
  | "management"
  | "projectLead"
  | "employee"
  | "viewer";

interface StaffProfile {
  active: boolean;
  staffId: string;
  role: StaffRole;
  displayName: string;
  designation: string;
  department: string;
  workEmail: string;
  projectIds: string[];
  permissions: string[];
}

function requireConfiguredDatabase(): void {
  if (!databaseId) {
    throw new HttpsError(
      "failed-precondition",
      "Set FIRESTORE_DATABASE_ID to the configured Firestore database before enabling backend actions."
    );
  }
}

function requireUser(request: AuthenticatedRequest): string {
  requireConfiguredDatabase();
  if (!request.auth?.uid) {
    throw new HttpsError("unauthenticated", "Sign in to continue.");
  }
  return request.auth.uid;
}

function requirePayloadString(
  data: Record<string, unknown>,
  name: string,
  maxLength: number
): string {
  const value = data[name];
  if (typeof value !== "string" || value.trim().length === 0 || value.length > maxLength) {
    throw new HttpsError("invalid-argument", `Invalid ${name}.`);
  }
  return value.trim();
}

function hasMfaSignIn(request: AuthenticatedRequest): boolean {
  const firebaseClaims = request.auth?.token.firebase as Record<string, unknown> | undefined;
  return typeof firebaseClaims?.sign_in_second_factor === "string" &&
    firebaseClaims.sign_in_second_factor.length > 0;
}

function isMfaAdmin(request: AuthenticatedRequest): boolean {
  return request.auth?.token.admin === true && hasMfaSignIn(request);
}

function requireMfaPermission(request: AuthenticatedRequest, permission: string): string {
  const uid = requireUser(request);
  const permissions = request.auth?.token.adminPermissions;
  if (
    !isMfaAdmin(request) ||
    !Array.isArray(permissions) ||
    !permissions.includes(permission)
  ) {
    throw new HttpsError("permission-denied", "This action is not available for this account.");
  }
  return uid;
}

function asStringArray(value: unknown): string[] {
  return Array.isArray(value)
    ? value.filter((entry): entry is string => typeof entry === "string")
    : [];
}

function toStaffProfile(uid: string, data: FirebaseFirestore.DocumentData): StaffProfile {
  const role = data.role as StaffRole;
  const validRoles: StaffRole[] = [
    "founder", "superAdmin", "management", "projectLead", "employee", "viewer",
  ];
  if (!validRoles.includes(role)) {
    throw new HttpsError("permission-denied", "This account is not authorized for operations.");
  }
  return {
    active: data.active === true,
    staffId: typeof data.staffId === "string" ? data.staffId : uid,
    role,
    displayName: typeof data.displayName === "string" ? data.displayName : "",
    designation: typeof data.designation === "string" ? data.designation : "",
    department: typeof data.department === "string" ? data.department : "",
    workEmail: typeof data.workEmail === "string" ? data.workEmail : "",
    projectIds: asStringArray(data.projectIds),
    permissions: asStringArray(data.permissions),
  };
}

async function requireStaff(
  request: AuthenticatedRequest,
  requiredPermission?: string
): Promise<{ uid: string; profile: StaffProfile }> {
  const uid = requireUser(request);
  if (request.auth?.token.email_verified !== true || request.auth.token.staff !== true) {
    throw new HttpsError("permission-denied", "This account is not authorized for operations.");
  }
  const claimedRole = request.auth.token.staffRole;
  const snapshot = await db.collection("opsEmployees").doc(uid).get();
  if (!snapshot.exists) {
    throw new HttpsError("permission-denied", "This account is not authorized for operations.");
  }
  const profile = toStaffProfile(uid, snapshot.data()!);
  if (!profile.active || claimedRole !== profile.role) {
    throw new HttpsError("permission-denied", "This account is not authorized for operations.");
  }
  if (
    ["founder", "superAdmin", "management", "projectLead"].includes(profile.role) &&
    !hasMfaSignIn(request)
  ) {
    throw new HttpsError("permission-denied", "This account is not authorized for operations.");
  }
  if (requiredPermission && !profile.permissions.includes(requiredPermission)) {
    throw new HttpsError("permission-denied", "This action is not available for this account.");
  }
  return { uid, profile };
}

async function consumeRateLimit(
  uid: string,
  action: string,
  maxCount: number,
  windowMs: number
): Promise<void> {
  const now = Date.now();
  const limitRef = db.collection("callLimits").doc(`${uid}_${action}`);
  await db.runTransaction(async (transaction) => {
    const snapshot = await transaction.get(limitRef);
    const currentStartedAt = snapshot.get("windowStartedAt");
    const withinWindow = currentStartedAt instanceof Timestamp &&
      now - currentStartedAt.toMillis() < windowMs;
    const count = withinWindow ? Number(snapshot.get("count") || 0) : 0;
    if (count >= maxCount) {
      throw new HttpsError("resource-exhausted", "Too many requests. Try again later.");
    }
    transaction.set(limitRef, {
      count: count + 1,
      windowStartedAt: withinWindow ? currentStartedAt : Timestamp.fromMillis(now),
      updatedAt: Timestamp.fromMillis(now),
    });
  });
}

function appendAudit(
  transaction: Transaction,
  actorUid: string,
  action: string,
  targetId: string,
  requestId: string,
  reason?: string
): void {
  const auditRef = db.collection("opsAuditLogs").doc();
  transaction.create(auditRef, {
    actorUid,
    action,
    targetId,
    requestId,
    reason: reason || null,
    createdAt: FieldValue.serverTimestamp(),
  });
}

function validateRequestId(request: AuthenticatedRequest): string {
  const value = request.rawRequest.headers["x-cloud-trace-context"];
  if (typeof value === "string" && value.length <= 200) return value;
  return randomBytes(12).toString("hex");
}

export const createReferralCode = onCall(
  { enforceAppCheck: true },
  async (request) => {
    const uid = requireUser(request);
    const ownerRef = db.collection("referralCodeOwners").doc(uid);
    const current = await ownerRef.get();
    if (current.exists) return { code: current.get("code") as string };
    await consumeRateLimit(uid, "create_referral_code", 3, 86_400_000);

    for (let attempt = 0; attempt < 5; attempt += 1) {
      const code = randomBytes(5).toString("hex").toUpperCase();
      const codeRef = db.collection("referralCodes").doc(code);
      const created = await db.runTransaction(async (transaction) => {
        const [ownerSnapshot, codeSnapshot] = await Promise.all([
          transaction.get(ownerRef),
          transaction.get(codeRef),
        ]);
        if (ownerSnapshot.exists) return ownerSnapshot.get("code") as string;
        if (codeSnapshot.exists) return null;
        transaction.create(codeRef, {
          ownerUid: uid,
          createdAt: FieldValue.serverTimestamp(),
          active: true,
        });
        transaction.create(ownerRef, {
          code,
          createdAt: FieldValue.serverTimestamp(),
        });
        return code;
      });
      if (created) return { code: created };
    }
    throw new HttpsError("resource-exhausted", "A referral code could not be created right now.");
  }
);

export const claimReferralCode = onCall(
  { enforceAppCheck: true },
  async (request) => {
    const uid = requireUser(request);
    await consumeRateLimit(uid, "claim_referral_code", 5, 3_600_000);
    const rawCode = requirePayloadString(request.data, "code", 12).toUpperCase();
    if (!referralCodeIsValid(rawCode)) {
      throw new HttpsError("invalid-argument", "Enter a valid referral code.");
    }
    if (!Number.isInteger(attributionDays) || attributionDays < 1 || attributionDays > 90) {
      throw new HttpsError("failed-precondition", "Referral attribution policy is not configured.");
    }
    const user = await getAuth().getUser(uid);
    const createdAt = user.metadata.creationTime ? Date.parse(user.metadata.creationTime) : NaN;
    const now = Date.now();
    if (!isWithinAttributionWindow(createdAt, now, attributionDays)) {
      throw new HttpsError("failed-precondition", "Referral codes can only be claimed by recently registered accounts.");
    }
    const codeRef = db.collection("referralCodes").doc(rawCode);
    const referralRef = db.collection("referrals").doc(uid);
    await db.runTransaction(async (transaction) => {
      const [codeSnapshot, existingReferral] = await Promise.all([
        transaction.get(codeRef),
        transaction.get(referralRef),
      ]);
      if (!codeSnapshot.exists || codeSnapshot.get("active") !== true) {
        throw new HttpsError("not-found", "That referral code is not available.");
      }
      const referrerUid = codeSnapshot.get("ownerUid");
      if (typeof referrerUid !== "string" || referrerUid === uid) {
        throw new HttpsError("failed-precondition", "This referral cannot be used for this account.");
      }
      if (existingReferral.exists) {
        throw new HttpsError("already-exists", "A referral is already linked to this account.");
      }
      transaction.create(referralRef, {
        referrerUid,
        referredUid: uid,
        state: "registered",
        qualifiedDays: 0,
        attributionCode: rawCode,
        attributedAt: Timestamp.fromMillis(now),
        accountCreatedAt: Timestamp.fromMillis(createdAt),
        attributionPolicyDays: attributionDays,
        rewardXp: REFERRAL_XP_REWARD,
      });
    });
    return { state: "registered", requiredDays: REFERRAL_REQUIRED_DAYS };
  }
);

export const startStudySession = onCall(
  { enforceAppCheck: true },
  async (request) => {
    const uid = requireUser(request);
    await consumeRateLimit(uid, "start_study_session", 12, 86_400_000);
    const now = Date.now();
    const lockRef = db.collection("studySessionLocks").doc(uid);
    const sessionRef = db.collection("studySessions").doc();
    await db.runTransaction(async (transaction) => {
      const lockSnapshot = await transaction.get(lockRef);
      const activeId = lockSnapshot.get("sessionId");
      const activeStartedAt = lockSnapshot.get("startedAt");
      let expiredSessionRef: DocumentReference | null = null;
      if (typeof activeId === "string" && activeStartedAt instanceof Timestamp) {
        if (now - activeStartedAt.toMillis() <= STUDY_SESSION_MAX_MINUTES * 60_000) {
          throw new HttpsError("failed-precondition", "Finish the active study session first.");
        }
        expiredSessionRef = db.collection("studySessions").doc(activeId);
      }
      if (expiredSessionRef) {
        const expired = await transaction.get(expiredSessionRef);
        if (expired.exists && expired.get("status") === "active") {
          transaction.update(expiredSessionRef, { status: "expired", endedAt: Timestamp.fromMillis(now) });
        }
      }
      transaction.create(sessionRef, {
        uid,
        status: "active",
        startedAt: Timestamp.fromMillis(now),
      });
      transaction.set(lockRef, {
        sessionId: sessionRef.id,
        startedAt: Timestamp.fromMillis(now),
      });
    });
    return { sessionId: sessionRef.id, startedAtMs: now };
  }
);

interface SessionCompletion {
  eligibleMinutes: number;
  studyXp: number;
  referralState: string | null;
  qualifiedDays: number | null;
}

function walletRef(uid: string): DocumentReference {
  return db.collection("xpWallets").doc(uid);
}

function ledgerRef(uid: string, id: string): DocumentReference {
  return db.collection("xpLedger").doc(uid).collection("entries").doc(id);
}

function awardWallet(
  transaction: Transaction,
  uid: string,
  ledgerId: string,
  amount: number,
  category: "study" | "referral",
  now: number,
  ledgerSnapshot: FirebaseFirestore.DocumentSnapshot,
  walletSnapshot: FirebaseFirestore.DocumentSnapshot
): void {
  if (ledgerSnapshot.exists || amount <= 0) return;
  const weekKey = utcWeekKey(now);
  const previousWeek = walletSnapshot.get("weekKey");
  const currentWeekly = previousWeek === weekKey
    ? Number(walletSnapshot.get("weeklyXp") || 0)
    : 0;
  const currentLifetime = Number(walletSnapshot.get("lifetimeXp") || 0);
  const currentStudy = Number(walletSnapshot.get("studyXp") || 0);
  const currentReferral = Number(walletSnapshot.get("referralXp") || 0);
  transaction.create(ledgerRef(uid, ledgerId), {
    amount,
    category,
    eventId: ledgerId,
    createdAt: Timestamp.fromMillis(now),
    reversible: true,
  });
  transaction.set(walletRef(uid), {
    lifetimeXp: currentLifetime + amount,
    weeklyXp: currentWeekly + amount,
    weekKey,
    studyXp: category === "study" ? currentStudy + amount : currentStudy,
    referralXp: category === "referral" ? currentReferral + amount : currentReferral,
    updatedAt: Timestamp.fromMillis(now),
  }, { merge: true });
}

export const completeStudySession = onCall(
  { enforceAppCheck: true },
  async (request) => {
    const uid = requireUser(request);
    await consumeRateLimit(uid, "complete_study_session", 30, 86_400_000);
    const sessionId = requirePayloadString(request.data, "sessionId", 128);
    if (request.data.confirmFocusedStudy !== true) {
      throw new HttpsError("invalid-argument", "Confirm that this was a focused study session.");
    }
    const now = Date.now();
    const sessionRef = db.collection("studySessions").doc(sessionId);
    const lockRef = db.collection("studySessionLocks").doc(uid);
    const referralRef = db.collection("referrals").doc(uid);
    const currentDayKey = utcDayKey(now);
    const referralDayRef = db.collection("referralDailyProgress").doc(uid)
      .collection("days").doc(currentDayKey);
    const studyCapRef = db.collection("xpDailyCaps").doc(`${uid}_${currentDayKey}`);
    const sessionLedgerRef = ledgerRef(uid, `study_${sessionId}`);
    const walletDocumentRef = walletRef(uid);

    const result = await db.runTransaction<SessionCompletion>(async (transaction) => {
      const [sessionSnapshot, lockSnapshot, referralSnapshot, daySnapshot, capSnapshot,
        ledgerSnapshot, walletSnapshot] = await Promise.all([
        transaction.get(sessionRef),
        transaction.get(lockRef),
        transaction.get(referralRef),
        transaction.get(referralDayRef),
        transaction.get(studyCapRef),
        transaction.get(sessionLedgerRef),
        transaction.get(walletDocumentRef),
      ]);
      if (!sessionSnapshot.exists || sessionSnapshot.get("uid") !== uid) {
        throw new HttpsError("not-found", "The active session was not found.");
      }
      if (sessionSnapshot.get("status") === "completed") {
        return {
          eligibleMinutes: Number(sessionSnapshot.get("eligibleMinutes") || 0),
          studyXp: Number(sessionSnapshot.get("studyXp") || 0),
          referralState: referralSnapshot.exists ? String(referralSnapshot.get("state")) : null,
          qualifiedDays: referralSnapshot.exists ? Number(referralSnapshot.get("qualifiedDays") || 0) : null,
        };
      }
      if (
        sessionSnapshot.get("status") !== "active" ||
        lockSnapshot.get("sessionId") !== sessionId
      ) {
        throw new HttpsError("failed-precondition", "This study session is no longer active.");
      }
      const startedAt = sessionSnapshot.get("startedAt");
      if (!(startedAt instanceof Timestamp)) {
        throw new HttpsError("failed-precondition", "The session start time is invalid.");
      }
      let eligibleMinutes: number;
      try {
        eligibleMinutes = verifiedStudyMinutes(now - startedAt.toMillis(), true);
      } catch (error) {
        throw new HttpsError("failed-precondition", error instanceof Error ? error.message : "Session is not eligible.");
      }
      const currentStudyXp = Number(capSnapshot.get("studyXp") || 0);
      const studyXp = studyXpAward(eligibleMinutes, currentStudyXp);
      if (studyXp > 0) {
        awardWallet(
          transaction, uid, `study_${sessionId}`, studyXp, "study", now,
          ledgerSnapshot, walletSnapshot
        );
        transaction.set(studyCapRef, { studyXp: currentStudyXp + studyXp, utcDay: currentDayKey });
      }

      let referralState: string | null = null;
      let qualifiedDays: number | null = null;
      if (referralSnapshot.exists) {
        referralState = String(referralSnapshot.get("state") || "registered");
        qualifiedDays = Number(referralSnapshot.get("qualifiedDays") || 0);
        if (!["approved", "rejected"].includes(referralState)) {
          const previousMinutes = Number(daySnapshot.get("eligibleMinutes") || 0);
          const nextMinutes = Math.min(180, previousMinutes + eligibleMinutes);
          const wasQualified = daySnapshot.get("qualified") === true;
          const nowQualified = referralDayQualifies(nextMinutes);
          if (nowQualified && !wasQualified) qualifiedDays += 1;
          referralState = nextReferralState(qualifiedDays, referralState as never);
          transaction.set(referralDayRef, {
            eligibleMinutes: nextMinutes,
            qualified: nowQualified || wasQualified,
            updatedAt: Timestamp.fromMillis(now),
          }, { merge: true });
          transaction.update(referralRef, {
            qualifiedDays,
            state: referralState,
            updatedAt: Timestamp.fromMillis(now),
          });
        }
      }

      transaction.update(sessionRef, {
        status: "completed",
        endedAt: Timestamp.fromMillis(now),
        eligibleMinutes,
        studyXp,
        utcDay: currentDayKey,
        learnerConfirmed: true,
      });
      transaction.delete(lockRef);
      return { eligibleMinutes, studyXp, referralState, qualifiedDays };
    });
    return result;
  }
);

export const getReferralDashboard = onCall(
  { enforceAppCheck: true },
  async (request) => {
    const uid = requireUser(request);
    await consumeRateLimit(uid, "read_referral_dashboard", 120, 3_600_000);
    const now = Date.now();
    const dayKey = utcDayKey(now);
    const [codeSnapshot, referralSnapshot, walletSnapshot, daySnapshot, activeSnapshot,
      recentLedger, ownedReferrals] = await Promise.all([
      db.collection("referralCodeOwners").doc(uid).get(),
      db.collection("referrals").doc(uid).get(),
      walletRef(uid).get(),
      db.collection("referralDailyProgress").doc(uid).collection("days").doc(dayKey).get(),
      db.collection("studySessionLocks").doc(uid).get(),
      db.collection("xpLedger").doc(uid).collection("entries").orderBy("createdAt", "desc").limit(10).get(),
      db.collection("referrals").where("referrerUid", "==", uid).limit(50).get(),
    ]);
    const activeSessionId = activeSnapshot.get("sessionId");
    const activeSession = typeof activeSessionId === "string"
      ? await db.collection("studySessions").doc(activeSessionId).get()
      : null;
    return {
      code: codeSnapshot.exists ? codeSnapshot.get("code") : null,
      referralState: referralSnapshot.exists ? referralSnapshot.get("state") : null,
      qualifiedDays: referralSnapshot.exists ? Number(referralSnapshot.get("qualifiedDays") || 0) : 0,
      todayEligibleMinutes: Number(daySnapshot.get("eligibleMinutes") || 0),
      lifetimeXp: Number(walletSnapshot.get("lifetimeXp") || 0),
      weeklyXp: Number(walletSnapshot.get("weeklyXp") || 0),
      studyXp: Number(walletSnapshot.get("studyXp") || 0),
      referralXp: Number(walletSnapshot.get("referralXp") || 0),
      activeStudySessionId: activeSnapshot.get("sessionId") || null,
      activeStudySessionStartedAtMs: activeSession?.get("startedAt") instanceof Timestamp
        ? activeSession.get("startedAt").toMillis()
        : null,
      requiredDays: REFERRAL_REQUIRED_DAYS,
      minutesPerDay: REFERRAL_MINUTES_PER_DAY,
      referralRewardXp: REFERRAL_XP_REWARD,
      referrals: ownedReferrals.docs.map((document) => ({
        state: document.get("state") || "registered",
        qualifiedDays: Number(document.get("qualifiedDays") || 0),
      })),
      recentLedger: recentLedger.docs.map((document) => ({
        amount: Number(document.get("amount") || 0),
        category: document.get("category") || "unknown",
        createdAtMs: document.get("createdAt") instanceof Timestamp
          ? document.get("createdAt").toMillis()
          : null,
      })),
    };
  }
);

export const approveReferral = onCall(
  { enforceAppCheck: true },
  async (request) => {
    const adminUid = requireMfaPermission(request, "referrals.review");
    await consumeRateLimit(adminUid, "approve_referral", 30, 3_600_000);
    const referredUid = requirePayloadString(request.data, "referredUid", 128);
    const reason = requirePayloadString(request.data, "reason", 500);
    if (reason.length < 8) {
      throw new HttpsError("invalid-argument", "Add a review note of at least 8 characters.");
    }
    const referralDocument = db.collection("referrals").doc(referredUid);
    const requestId = validateRequestId(request);
    const now = Date.now();
    await db.runTransaction(async (transaction) => {
      const referralSnapshot = await transaction.get(referralDocument);
      if (!referralSnapshot.exists) throw new HttpsError("not-found", "Referral record not found.");
      if (referralSnapshot.get("state") === "approved") return;
      if (
        referralSnapshot.get("state") !== "pending_review" ||
        Number(referralSnapshot.get("qualifiedDays") || 0) < REFERRAL_REQUIRED_DAYS
      ) {
        throw new HttpsError("failed-precondition", "This referral is not ready for review.");
      }
      const referrerUid = referralSnapshot.get("referrerUid");
      if (typeof referrerUid !== "string" || referrerUid === referredUid) {
        throw new HttpsError("failed-precondition", "The referral record is invalid.");
      }
      const referralLedger = ledgerRef(referrerUid, `referral_${referredUid}`);
      const walletDocument = walletRef(referrerUid);
      const [ledgerSnapshot, walletSnapshot] = await Promise.all([
        transaction.get(referralLedger),
        transaction.get(walletDocument),
      ]);
      if (ledgerSnapshot.exists) {
        transaction.update(referralDocument, {
          state: "approved",
          reviewedAt: Timestamp.fromMillis(now),
          reviewedBy: adminUid,
          reviewReason: reason,
        });
      } else {
        awardWallet(
          transaction,
          referrerUid,
          `referral_${referredUid}`,
          REFERRAL_XP_REWARD,
          "referral",
          now,
          ledgerSnapshot,
          walletSnapshot
        );
        transaction.update(referralDocument, {
          state: "approved",
          reviewedAt: Timestamp.fromMillis(now),
          reviewedBy: adminUid,
          reviewReason: reason,
        });
      }
      appendAudit(transaction, adminUid, "referral.approve", referredUid, requestId, reason);
    });
    return { approved: true, xpAwarded: REFERRAL_XP_REWARD };
  }
);

export const listPendingReferrals = onCall(
  { enforceAppCheck: true },
  async (request) => {
    requireMfaPermission(request, "referrals.review");
    await consumeRateLimit(request.auth!.uid, "list_referrals", 60, 3_600_000);
    const snapshot = await db.collection("referrals")
      .where("state", "==", "pending_review")
      .limit(100)
      .get();
    return {
      referrals: snapshot.docs.map((document) => ({
        referredUid: document.id,
        referrerUid: document.get("referrerUid") || "",
        qualifiedDays: Number(document.get("qualifiedDays") || 0),
        state: document.get("state") || "pending_review",
        attributedAtMs: document.get("attributedAt") instanceof Timestamp
          ? document.get("attributedAt").toMillis()
          : null,
      })),
    };
  }
);

async function readActiveStaffInTransaction(
  transaction: Transaction,
  uid: string,
  expectedRole: unknown
): Promise<StaffProfile> {
  const profileSnapshot = await transaction.get(db.collection("opsEmployees").doc(uid));
  if (!profileSnapshot.exists) throw new HttpsError("permission-denied", "This account is not authorized for operations.");
  const profile = toStaffProfile(uid, profileSnapshot.data()!);
  if (!profile.active || expectedRole !== profile.role) {
    throw new HttpsError("permission-denied", "This account is not authorized for operations.");
  }
  return profile;
}

export const getEmployeeDashboard = onCall(
  { enforceAppCheck: true },
  async (request) => {
    const { uid } = await requireStaff(request);
    await consumeRateLimit(uid, "read_employee_dashboard", 120, 3_600_000);
    const [profileSnapshot, tasksSnapshot, policiesSnapshot, acknowledgementsSnapshot] = await Promise.all([
      db.collection("opsEmployees").doc(uid).get(),
      db.collection("opsTasks").where("assignedUid", "==", uid).limit(50).get(),
      db.collection("opsPolicies").where("status", "==", "published").limit(20).get(),
      db.collection("opsPolicyAcks").where("uid", "==", uid).limit(50).get(),
    ]);
    const profile = toStaffProfile(uid, profileSnapshot.data()!);
    return {
      profile: {
        displayName: profile.displayName,
        designation: profile.designation,
        department: profile.department,
        workEmail: profile.workEmail,
        staffId: profile.staffId,
        role: profile.role,
      },
      canAssignTasks: profile.permissions.includes("tasks.assign"),
      tasks: tasksSnapshot.docs.map((task) => ({
        id: task.id,
        projectId: task.get("projectId") || "",
        title: task.get("title") || "",
        instructions: task.get("instructions") || "",
        status: task.get("status") || "open",
        dueAtMs: task.get("dueAt") instanceof Timestamp ? task.get("dueAt").toMillis() : null,
      })),
      policies: policiesSnapshot.docs.map((policy) => ({
        id: policy.id,
        title: policy.get("title") || "",
        summary: policy.get("summary") || "",
        version: policy.get("version") || "",
      })),
      acknowledgedPolicyVersions: acknowledgementsSnapshot.docs.map((ack) =>
        `${ack.get("policyId")}:${ack.get("version")}`
      ),
    };
  }
);

export const createEmployeeTask = onCall(
  { enforceAppCheck: true },
  async (request) => {
    const { uid, profile } = await requireStaff(request, "tasks.assign");
    await consumeRateLimit(uid, "create_employee_task", 40, 3_600_000);
    const assigneeUid = requirePayloadString(request.data, "assigneeUid", 128);
    const projectId = requirePayloadString(request.data, "projectId", 128);
    const title = requirePayloadString(request.data, "title", 120);
    const instructions = typeof request.data.instructions === "string"
      ? request.data.instructions.trim().slice(0, 2000)
      : "";
    const dueAtMs = Number(request.data.dueAtMs);
    if (!Number.isFinite(dueAtMs) || dueAtMs < Date.now() - 86_400_000) {
      throw new HttpsError("invalid-argument", "Choose a valid due date.");
    }
    if (profile.role === "projectLead" && !profile.projectIds.includes(projectId)) {
      throw new HttpsError("permission-denied", "This account is not authorized for that project.");
    }
    const taskRef = db.collection("opsTasks").doc();
    const assigneeRef = db.collection("opsEmployees").doc(assigneeUid);
    const projectRef = db.collection("opsProjects").doc(projectId);
    await db.runTransaction(async (transaction) => {
      const [actorProfile, assigneeSnapshot, projectSnapshot] = await Promise.all([
        transaction.get(db.collection("opsEmployees").doc(uid)),
        transaction.get(assigneeRef),
        transaction.get(projectRef),
      ]);
      const currentActor = toStaffProfile(uid, actorProfile.data() || {});
      if (!currentActor.active || !currentActor.permissions.includes("tasks.assign")) {
        throw new HttpsError("permission-denied", "This action is not available for this account.");
      }
      if (!assigneeSnapshot.exists || assigneeSnapshot.get("active") !== true) {
        throw new HttpsError("not-found", "The assigned employee is not active.");
      }
      const projectMembers = asStringArray(projectSnapshot.get("memberUids"));
      const projectLeads = asStringArray(projectSnapshot.get("leadUids"));
      if (!projectSnapshot.exists ||
        (!projectMembers.includes(assigneeUid) && !projectLeads.includes(assigneeUid))) {
        throw new HttpsError("failed-precondition", "The employee must be assigned to this project first.");
      }
      if (profile.role === "projectLead" && !projectLeads.includes(uid)) {
        throw new HttpsError("permission-denied", "This account is not authorized for that project.");
      }
      transaction.create(taskRef, {
        projectId,
        assignedUid: assigneeUid,
        title,
        instructions,
        status: "open",
        createdBy: uid,
        createdAt: FieldValue.serverTimestamp(),
        dueAt: Timestamp.fromMillis(dueAtMs),
      });
      appendAudit(transaction, uid, "employee.task.create", taskRef.id, validateRequestId(request));
    });
    return { taskId: taskRef.id };
  }
);

export const submitEmployeeTaskReport = onCall(
  { enforceAppCheck: true },
  async (request) => {
    const { uid, profile } = await requireStaff(request);
    await consumeRateLimit(uid, "submit_employee_report", 20, 3_600_000);
    const taskId = requirePayloadString(request.data, "taskId", 128);
    const report = requirePayloadString(request.data, "report", 2000);
    const taskRef = db.collection("opsTasks").doc(taskId);
    const reportRef = db.collection("opsTaskReports").doc();
    await db.runTransaction(async (transaction) => {
      const [actorSnapshot, taskSnapshot] = await Promise.all([
        transaction.get(db.collection("opsEmployees").doc(uid)),
        transaction.get(taskRef),
      ]);
      const actor = toStaffProfile(uid, actorSnapshot.data() || {});
      if (!actor.active || actor.role !== profile.role || !taskSnapshot.exists) {
        throw new HttpsError("permission-denied", "This task is not available for this account.");
      }
      const canReport = taskSnapshot.get("assignedUid") === uid ||
        (actor.permissions.includes("tasks.manage") &&
          actor.projectIds.includes(String(taskSnapshot.get("projectId") || "")));
      if (!canReport) throw new HttpsError("permission-denied", "This task is not available for this account.");
      if (taskSnapshot.get("status") === "closed") {
        throw new HttpsError("failed-precondition", "Closed tasks cannot receive new reports.");
      }
      transaction.create(reportRef, {
        taskId,
        projectId: taskSnapshot.get("projectId") || "",
        authorUid: uid,
        report,
        createdAt: FieldValue.serverTimestamp(),
      });
      transaction.update(taskRef, { status: "submitted", updatedAt: FieldValue.serverTimestamp() });
      appendAudit(transaction, uid, "employee.task.report", taskId, validateRequestId(request));
    });
    return { submitted: true };
  }
);

export const acknowledgeEmployeePolicy = onCall(
  { enforceAppCheck: true },
  async (request) => {
    const { uid, profile } = await requireStaff(request);
    await consumeRateLimit(uid, "acknowledge_policy", 20, 3_600_000);
    const policyId = requirePayloadString(request.data, "policyId", 128);
    const version = requirePayloadString(request.data, "version", 40);
    const policyRef = db.collection("opsPolicies").doc(policyId);
    const ackRef = db.collection("opsPolicyAcks").doc(`${uid}_${policyId}_${version}`);
    await db.runTransaction(async (transaction) => {
      const [actorSnapshot, policySnapshot, existingAck] = await Promise.all([
        transaction.get(db.collection("opsEmployees").doc(uid)),
        transaction.get(policyRef),
        transaction.get(ackRef),
      ]);
      const actor = toStaffProfile(uid, actorSnapshot.data() || {});
      if (!actor.active || actor.role !== profile.role) {
        throw new HttpsError("permission-denied", "This action is not available for this account.");
      }
      if (!policySnapshot.exists || policySnapshot.get("status") !== "published" ||
        policySnapshot.get("version") !== version) {
        throw new HttpsError("failed-precondition", "This policy version is no longer current.");
      }
      if (existingAck.exists) return;
      transaction.create(ackRef, {
        uid,
        policyId,
        version,
        acknowledgedAt: FieldValue.serverTimestamp(),
      });
      appendAudit(transaction, uid, "employee.policy.acknowledge", policyId, validateRequestId(request));
    });
    return { acknowledged: true };
  }
);
