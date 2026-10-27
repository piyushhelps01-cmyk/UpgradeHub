export const REFERRAL_REQUIRED_DAYS = 15;
export const REFERRAL_MINUTES_PER_DAY = 60;
export const STUDY_SESSION_MIN_MINUTES = 15;
export const STUDY_SESSION_MAX_MINUTES = 180;
export const STUDY_XP_PER_SESSION = 10;
export const STUDY_XP_DAILY_CAP = 30;
export const REFERRAL_XP_REWARD = 500;

export type ReferralState =
  | "registered"
  | "in_progress"
  | "pending_review"
  | "approved"
  | "rejected";

export function utcDayKey(timestampMs: number): string {
  return new Date(timestampMs).toISOString().slice(0, 10);
}

export function utcWeekKey(timestampMs: number): string {
  const date = new Date(timestampMs);
  const day = (date.getUTCDay() + 6) % 7;
  date.setUTCDate(date.getUTCDate() - day + 3);
  const firstThursday = new Date(Date.UTC(date.getUTCFullYear(), 0, 4));
  const firstDay = (firstThursday.getUTCDay() + 6) % 7;
  firstThursday.setUTCDate(firstThursday.getUTCDate() - firstDay + 3);
  const week = 1 + Math.round((date.getTime() - firstThursday.getTime()) / 604800000);
  return `${date.getUTCFullYear()}-W${String(week).padStart(2, "0")}`;
}

export function verifiedStudyMinutes(
  elapsedMs: number,
  learnerConfirmed: boolean
): number {
  if (!learnerConfirmed) {
    throw new Error("Learner confirmation is required.");
  }
  if (!Number.isFinite(elapsedMs) || elapsedMs < STUDY_SESSION_MIN_MINUTES * 60_000) {
    throw new Error("A study session must run for at least 15 minutes.");
  }
  if (elapsedMs > STUDY_SESSION_MAX_MINUTES * 60_000) {
    throw new Error("A session over three hours must be reviewed separately.");
  }
  return Math.floor(elapsedMs / 60_000);
}

export function studyXpAward(
  verifiedMinutes: number,
  earnedToday: number
): number {
  if (verifiedMinutes < STUDY_SESSION_MIN_MINUTES) return 0;
  return Math.min(STUDY_XP_PER_SESSION, Math.max(0, STUDY_XP_DAILY_CAP - earnedToday));
}

export function referralDayQualifies(minutes: number): boolean {
  return Number.isFinite(minutes) && minutes >= REFERRAL_MINUTES_PER_DAY;
}

export function nextReferralState(
  qualifiedDays: number,
  currentState: ReferralState
): ReferralState {
  if (currentState === "approved" || currentState === "rejected") return currentState;
  if (qualifiedDays >= REFERRAL_REQUIRED_DAYS) return "pending_review";
  return qualifiedDays > 0 ? "in_progress" : "registered";
}

export function isWithinAttributionWindow(
  accountCreatedAtMs: number,
  claimedAtMs: number,
  attributionDays: number
): boolean {
  if (
    !Number.isFinite(accountCreatedAtMs) ||
    !Number.isFinite(claimedAtMs) ||
    !Number.isInteger(attributionDays) ||
    attributionDays < 1 ||
    claimedAtMs < accountCreatedAtMs
  ) {
    return false;
  }
  return claimedAtMs - accountCreatedAtMs <= attributionDays * 86_400_000;
}

export function referralCodeIsValid(value: unknown): value is string {
  return typeof value === "string" && /^[A-Z0-9]{8,12}$/.test(value);
}
