#!/usr/bin/env node
import { readFile } from "node:fs/promises";
import { applicationDefault, initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { FieldValue, getFirestore } from "firebase-admin/firestore";

const [uid, role, profilePath] = process.argv.slice(2);
const projectId = process.env.FIREBASE_PROJECT_ID;
const databaseId = process.env.FIRESTORE_DATABASE_ID;
const operatorEmail = process.env.PROVISIONER_EMAIL;
const allowedRoles = new Set([
  "founder",
  "superAdmin",
  "management",
  "projectLead",
  "employee",
  "viewer",
]);

if (!uid || !allowedRoles.has(role) || !profilePath || !projectId || !databaseId || !operatorEmail) {
  console.error(
    "Usage: FIREBASE_PROJECT_ID=... FIRESTORE_DATABASE_ID=... PROVISIONER_EMAIL=... " +
    "node scripts/provision-staff.mjs <firebase-uid> <role> <private-profile.json>"
  );
  process.exit(2);
}

const profileInput = JSON.parse(await readFile(profilePath, "utf8"));
const app = initializeApp({
  credential: applicationDefault(),
  projectId,
});
const auth = getAuth(app);
const firestore = getFirestore(app, databaseId);
const rolePermissions = {
  founder: ["tasks.assign", "tasks.manage", "employee.manage", "audit.read"],
  superAdmin: ["tasks.assign", "tasks.manage", "employee.manage", "audit.read"],
  management: ["tasks.assign", "tasks.manage"],
  projectLead: ["tasks.assign", "tasks.manage"],
  employee: ["tasks.report", "profile.read"],
  viewer: ["profile.read", "tasks.read"],
};

if (
  typeof profileInput.displayName !== "string" ||
  typeof profileInput.designation !== "string" ||
  typeof profileInput.department !== "string" ||
  typeof profileInput.staffId !== "string" ||
  !Array.isArray(profileInput.projectIds) ||
  profileInput.projectIds.some(
    (value) => typeof value !== "string" || !/^[A-Za-z0-9_-]{1,64}$/.test(value)
  )
) {
  throw new Error("The private profile JSON is missing required staff fields.");
}

const user = await auth.getUser(uid);
if (!user.emailVerified) {
  throw new Error("The Firebase account must have a verified email before staff access is provisioned.");
}

const profile = {
  staffId: profileInput.staffId,
  active: true,
  role,
  displayName: profileInput.displayName,
  designation: profileInput.designation,
  department: profileInput.department,
  workEmail: user.email || "",
  projectIds: [...new Set(profileInput.projectIds)],
  permissions: rolePermissions[role],
  updatedAt: FieldValue.serverTimestamp(),
};

// Claims are granted only from this operator-controlled script, never from the app.
// This deliberately does not set admin=true or grant privileged referral review.
await auth.setCustomUserClaims(uid, {
  ...(user.customClaims || {}),
  staff: true,
  staffRole: role,
  staffId: profile.staffId,
});
await firestore.collection("opsEmployees").doc(uid).set(profile, { merge: true });
for (const projectId of profile.projectIds) {
  const projectRef = firestore.collection("opsProjects").doc(projectId);
  const memberFields = {
    memberUids: FieldValue.arrayUnion(uid),
    updatedAt: FieldValue.serverTimestamp(),
  };
  if (role === "projectLead") {
    memberFields.leadUids = FieldValue.arrayUnion(uid);
  }
  await projectRef.set({
    projectId,
    name: profileInput.projectNames?.[projectId] || projectId,
    ...memberFields,
  }, { merge: true });
}
await firestore.collection("opsAuditLogs").add({
  actor: operatorEmail,
  action: "employee.provision",
  targetId: uid,
  reason: "Operator-controlled staff provisioning",
  createdAt: FieldValue.serverTimestamp(),
});

console.log("Staff access provisioned. Ask the employee to sign out and sign in again to refresh their claims.");
