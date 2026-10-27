import assert from "node:assert/strict";
import test from "node:test";
import {
  isWithinAttributionWindow,
  nextReferralState,
  referralCodeIsValid,
  referralDayQualifies,
  studyXpAward,
  utcDayKey,
  utcWeekKey,
  verifiedStudyMinutes,
} from "../src/domain.js";

test("study time is measured from server elapsed time and requires confirmation", () => {
  assert.equal(verifiedStudyMinutes(45 * 60_000 + 59_000, true), 45);
  assert.throws(() => verifiedStudyMinutes(45 * 60_000, false), /confirmation/);
  assert.throws(() => verifiedStudyMinutes(14 * 60_000, true), /15 minutes/);
  assert.throws(() => verifiedStudyMinutes(181 * 60_000, true), /three hours/);
});

test("study XP uses a fixed award and stops at the daily cap", () => {
  assert.equal(studyXpAward(15, 0), 10);
  assert.equal(studyXpAward(15, 20), 10);
  assert.equal(studyXpAward(15, 30), 0);
  assert.equal(studyXpAward(5, 0), 0);
});

test("referral day and review states follow the 15 x 60-minute rule", () => {
  assert.equal(referralDayQualifies(59), false);
  assert.equal(referralDayQualifies(60), true);
  assert.equal(nextReferralState(0, "registered"), "registered");
  assert.equal(nextReferralState(7, "in_progress"), "in_progress");
  assert.equal(nextReferralState(15, "in_progress"), "pending_review");
  assert.equal(nextReferralState(15, "approved"), "approved");
});

test("referral attribution window and code validation reject malformed input", () => {
  const created = Date.UTC(2026, 0, 1);
  assert.equal(isWithinAttributionWindow(created, created + 30 * 86_400_000, 30), true);
  assert.equal(isWithinAttributionWindow(created, created + 31 * 86_400_000, 30), false);
  assert.equal(referralCodeIsValid("ABCD1234"), true);
  assert.equal(referralCodeIsValid("ab-cd"), false);
});

test("UTC day and week keys are deterministic at boundaries", () => {
  assert.equal(utcDayKey(Date.UTC(2026, 0, 1, 23, 59)), "2026-01-01");
  assert.equal(utcWeekKey(Date.UTC(2026, 0, 1)), "2026-W01");
  assert.equal(utcWeekKey(Date.UTC(2026, 0, 5)), "2026-W02");
});
