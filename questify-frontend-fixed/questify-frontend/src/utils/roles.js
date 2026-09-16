export const ROLES = {
  SUPER_ADMIN: "super-admin",
  INSTITUTION_ADMIN: "institution-admin",
  FACULTY: "faculty",
  HOD: "hod",
  REVIEWER: "reviewer",
  COURSE_COORDINATOR: "course-coordinator",
};

export const ROLE_LABELS = {
  [ROLES.SUPER_ADMIN]: "Super admin",
  [ROLES.INSTITUTION_ADMIN]: "Institution admin",
  [ROLES.FACULTY]: "Faculty",
  [ROLES.HOD]: "HOD",
  [ROLES.REVIEWER]: "Reviewer",
  [ROLES.COURSE_COORDINATOR]: "Course coordinator",
};

export const WORKSPACE_LABELS = {
  [ROLES.SUPER_ADMIN]: "Platform console",
  [ROLES.INSTITUTION_ADMIN]: "Admin console",
  [ROLES.FACULTY]: "Faculty workspace",
  [ROLES.HOD]: "HOD workspace",
  [ROLES.REVIEWER]: "Reviewer console",
  [ROLES.COURSE_COORDINATOR]: "Coordinator workspace",
};

export function normalizeRole(role) {
  if (!role) return "";
  const s = String(role).trim().toUpperCase().replace(/-/g, "_").replace(/ /g, "_");
  return s.startsWith("ROLE_") ? s.substring(5) : s;
}

export function isKnownRole(role) {
  const norm = normalizeRole(role);
  return Object.keys(ROLES).some((k) => k === norm);
}

export function hasRole(user, allowedRoles) {
  if (!user) return false;
  if (!allowedRoles || allowedRoles.length === 0) return true;
  const userNorm = normalizeRole(user.role);
  return allowedRoles.some((r) => normalizeRole(r) === userNorm || r === user.role);
}

export function initialsOf(name = "") {
  return name
    .split(" ")
    .filter((part) => /[A-Za-z]/.test(part))
    .slice(0, 2)
    .map((part) => part[0].toUpperCase())
    .join("");
}
