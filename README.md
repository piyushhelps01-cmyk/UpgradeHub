# UpgradeHub Android

UpgradeHub is a native Android app written in Kotlin and Jetpack Compose. The project includes study planning, curriculum tracking, practice and mock-test screens, revision tools, analytics, campus/community screens, and local Room storage, with optional Firebase sign-in and Firestore sync.

## Open and build

1. Install Android Studio with a compatible JDK and Android SDK. The app requests Android SDK 36 (minor API level 1).
2. Open this directory as an Android Studio project and allow Gradle sync to complete.
3. Configure the Firebase project you intend to use. This archive replaces its Firebase API key with a placeholder; restore your own `app/google-services.json` before building. The file is gitignored.
4. For local AI-provider configuration, start from `.env.example`; keep real credentials in the IDE/CI secret store and out of source control.
5. Run the `app` configuration on an emulator or Android device.

The project includes a Gradle wrapper configured for Gradle 9.3.1. The wrapper distribution can be downloaded automatically on first build.

## Secure rewards and operations features

The student app now has a Referral & XP screen backed by Firebase callable functions. Study sessions are timed by the server, require learner confirmation, are capped at 30 study XP per UTC day, and feed referral progress only after a code is linked. A referral becomes eligible for review after 15 distinct UTC days with at least 60 eligible minutes on each day. Approval credits 500 referral XP once to an append-only ledger; XP is not cash.

The old leaderboard and event data are now explicitly preview-only. Client-side taps, focus timers, practice results, and demo leaderboard controls cannot mint server XP. No cash or UPI reward campaign is active, and the app does not collect payout details.

The separate employee workspace is hosted at `/ops/`. It has no public registration and supports verified Google sign-in, MFA challenge for administrators, assigned task reports, policy acknowledgements, and manager task assignment. Every employee action checks active operator-provisioned staff status on the server. The Android app and public client cannot write XP, staff roles, referral status, or audit records directly.

See [BACKEND_SETUP.md](BACKEND_SETUP.md) for Firebase configuration, staff provisioning, policy decisions, and deployment steps. Firebase deployment is not performed by generating this archive.

## Admin access and deployment status

The Settings → About logo reveals the admin access gate after nine deliberate taps within five seconds. This gesture only reveals a sign-in screen; it does not assign an admin role. See [SECURITY.md](SECURITY.md) before configuring Firebase or testing the admin path.

This archive is source code, not a verified production release. The employee and referral endpoints are included but have not been deployed to a Firebase project. The web operations portal supports MFA challenge flows; the Android admin gate still fails closed unless the existing Firebase session already carries an MFA sign-in claim. Android admin content actions still use local preview state; trusted content publishing, notification dispatch, and other blueprint phases need separate server-side implementation and release testing.
