# Firebase backend setup

This source bundle adds server-side referral/XP functions, an employee operations portal, and a separate staff-claim provisioning tool. Nothing is deployed automatically.

## 1. Configure the project

1. Use a non-production Firebase project for first-time testing. Select the intended project with the Firebase CLI.
2. Enable Firebase Authentication providers used by your team and students. Add the operations portal's Hosting domain to the authorized domains.
3. Configure Firebase App Check for the Android and Web apps. The callable functions enforce App Check.
4. Copy `functions/.env.example` to `functions/.env`. Set `FIRESTORE_DATABASE_ID` to the exact database ID in `firebase.json`. Confirm the Firestore database location and set `FUNCTION_REGION` to a compatible region.
5. Confirm the 30-day referral-attribution window in `REFERRAL_ATTRIBUTION_DAYS` before launch.
6. Copy `public/ops/firebase-config.example.js` to `public/ops/firebase-config.js`, then add the Web app's Firebase client config and App Check site key. This file is ignored by Git; it is public client configuration, not a place for service-account credentials.

Do not put service-account JSON, Admin SDK credentials, tokens, or API secrets in the Android app, web bundle, `.env.example`, or this archive.

## 2. Provision staff

The employee portal has no sign-up or self-promotion path. Create a private JSON profile file outside the public directory and invoke the operator tool from a trusted machine with Application Default Credentials:

```sh
FIREBASE_PROJECT_ID=your-project \
FIRESTORE_DATABASE_ID=your-database \
PROVISIONER_EMAIL=operator@example.com \
node scripts/provision-staff.mjs FIREBASE_UID employee /secure/path/profile.json
```

The private profile JSON contains `staffId`, `displayName`, `designation`, `department`, and `projectIds` (optionally `projectNames`). Do not commit it. The script checks verified email, uses the email returned by Firebase Auth, assigns only the role's predefined permissions, writes the server-side active profile, assigns the employee to the listed projects, and logs the provisioning action. It deliberately does not grant the `admin` claim or referral-review permission. Remove access by setting the server-side `opsEmployees/{uid}.active` field to `false` and removing the staff custom claims through a trusted Firebase Admin SDK operator.

To approve referral rewards, an operator must separately provision `admin: true` and `adminPermissions: ["referrals.review"]` through an audited, protected process. The account must have Firebase MFA enrolled. Never set either claim from the client or this staff-provisioning script.

## 3. Deploy and verify

From the project root, install the Functions dependencies in the `functions/` directory and run:

```sh
cd functions
npm install
npm run check
cd ..
firebase deploy --only firestore:rules,functions,hosting
```

The Functions test suite covers deterministic eligibility, XP caps, referral states, and attribution windows. Before production, also test Firestore rules and callable authorization with the Firebase Emulator Suite, including student A/B data isolation, missing/expired staff access, non-MFA admin denial, duplicate referral claims, repeat session completions, duplicate reward approvals, and App Check enforcement.

The Android `Referral & XP` page calls Functions in `asia-south1`. Keep the deployed Functions region aligned with the client and Firestore location. Android build/installation still requires the Android SDK and JDK listed in the main README.

## Functions and records

Student callable functions: `createReferralCode`, `claimReferralCode`, `getReferralDashboard`, `startStudySession`, and `completeStudySession`.

MFA-gated review functions: `listPendingReferrals`, `approveReferral`.

Employee callable functions: `getEmployeeDashboard`, `createEmployeeTask`, `submitEmployeeTaskReport`, and `acknowledgeEmployeePolicy`.

Server-owned collections include `referralCodes`, `referralCodeOwners`, `referrals`, `referralDailyProgress`, `studySessions`, `studySessionLocks`, `xpWallets`, `xpLedger`, `xpDailyCaps`, `opsEmployees`, `opsProjects`, `opsTasks`, `opsTaskReports`, `opsPolicies`, `opsPolicyAcks`, and `opsAuditLogs`. Direct client reads and writes are denied by `firestore.rules`; client screens receive only the current user's summary through callable responses.

The operations portal is a separate Firebase Hosting surface at `/ops/`. It supports employee task reports, policy acknowledgements, task assignment for authorized managers, and MFA-backed referral review. It is not linked in the student app navigation.
