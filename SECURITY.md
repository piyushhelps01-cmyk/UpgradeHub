# Security setup and current limits

## Admin authorization

The Android client accepts admin access only when a freshly refreshed Firebase ID token contains the trusted custom claim `admin: true` and the token shows an MFA-backed sign-in (`firebase.sign_in_second_factor`). Student-profile reads require the separate least-privilege claim `can_view_student_data: true`. A local Room role, profile document, email address, hidden gesture, or client-side preference is not an authorization source.

Release builds must have the Firebase App Check reCAPTCHA Enterprise site key configured. If it is missing, the Android client now leaves App Check unavailable rather than falling back to a debug attestation provider; protected callable endpoints will fail closed.

The first admin must be provisioned out of band using trusted Firebase Admin SDK tooling in a protected operator environment. Do not add a bootstrap password, admin passkey, service-account credential, or claim-setting endpoint to the Android app. Grant `can_view_student_data` only when the admin's duties require it.

The included `firestore.rules` is source only; this archive has not deployed it or validated it with the Firebase emulator. Deploy and test it against the intended Firebase project before using the gate (the Firebase CLI configuration points to this rules file). The rules restrict user documents to their owner unless the separate student-data claim is present; user-owned subcollections remain private to the owner. Material and announcement writes require an MFA-authenticated admin claim.

## MFA integration and current limits

The private `/ops/` web portal handles Firebase phone-SMS and TOTP sign-in challenges. The Android admin gate still does not resolve Firebase multi-factor sign-in exceptions; it fails closed unless the existing Firebase session already carries an MFA sign-in claim. Do not onboard administrators through the Android gate until a native challenge flow is implemented and tested.

Cloud Functions independently verify the refreshed Firebase token's `admin: true`, MFA second-factor marker and explicit `adminPermissions` before reviewing referrals. Employee APIs require a verified email, signed `staff` / `staffRole` claims, and a matching active server-side employee record. Founder, super-admin, management, and project-lead roles also require an MFA-backed session. Custom claims must be provisioned through a trusted operator process only.

## Admin portal limitation

The Android admin screen remains a local prototype: its upload, announcement, audit, and permission panels are not wired to trusted publishing/dispatch endpoints. The ZIP includes server functions for referral approval and employee task workflows, but not the complete content-management backend. Android local preview changes and local audit rows are not published content or immutable audit evidence.

## Referral, XP and employee controls

`functions/src/index.ts` is the trusted source for referral attribution, session duration, qualifying-day progress, XP ledger credits, review approval, and employee task/policy actions. Firestore rules deny direct client access to all of these records. Referral approval is idempotent; reversals must be new compensating ledger entries, never deletion or editing of ledger history.

Study-session timing is server-measured but relies on the learner's confirmation; it is not independent evidence that learning occurred. Sessions are limited to one active session per account and three hours per session, and study XP is capped at 30 per UTC day. Referral days are UTC calendar days. The configurable attribution window defaults to 30 days because the blueprint does not specify its exact duration; confirm this before launch. Duplicate-device detection is not implemented, so suspicious cases remain a human-review responsibility. Cash rewards and XP-to-cash conversion are not implemented.

## Deployment and verification status

The functions and web portal are source only until configured and deployed by the project owner. Set the exact Firestore database ID and App Check configuration; configure Firebase Authentication providers; create the required Firestore indexes if Firebase requests them; provision active staff profiles through the trusted operator tool; and grant referral-review permissions only to MFA-protected admins. Run the Functions tests, Firestore rules tests against the Firebase emulator, and an Android build on an environment with the required JDK and Android SDK before release.

The uploaded app already has substantial study-planning, practice, revision, analytics, flashcard, community, campus, and event UI. The server-backed AI workflow, tutor marketplace, content publishing, production notification dispatch, remote configuration, export/deletion service, and moderation/appeals backend remain separate phases. No deployment, production readiness, or third-party integration is implied by the source archive.
