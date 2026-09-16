import { ROLES } from "../utils/roles.js";

// Sidebar navigation per role, mirroring the QuestiFy demo workspaces.
const TEACHING_NAV = [
  { to: "/dashboard", glyph: "\u25A6", label: "Dashboard" },
  { to: "/question-bank", glyph: "\u25A4", label: "Question bank" },
  { to: "/co-mapping", glyph: "\u25CE", label: "CO mapping" },
  { to: "/generate-paper", glyph: "\u2726", label: "Generate paper" },
  { to: "/papers", glyph: "\u25A1", label: "Papers" },
  { to: "/analytics", glyph: "\u25D2", label: "Analytics" },
  { to: "/profile", glyph: "\u25CB", label: "Profile" },
];

// HOD's normal workspace nav, plus a trailing action item that toggles
// into the Faculty workspace (reuses TEACHING_NAV, no separate pages).
const HOD_NAV = [
  { to: "/dashboard", glyph: "\u25A6", label: "Dashboard" },
  { to: "/approval-queue", glyph: "\u25A4", label: "Approval queue" },
  { to: "/faculty", glyph: "\u25CE", label: "Faculty" },
  { to: "/analytics", glyph: "\u25D2", label: "Analytics" },
  { to: "/profile", glyph: "\u25CB", label: "Profile" },
  { action: "toggle-faculty-mode", glyph: "\u21C4", label: "Acts as Faculty", divider: true },
];

// While an HOD is "acting as faculty", show the same teaching nav faculty
// gets, with a trailing item to switch back to the normal HOD nav.
const HOD_ACTING_AS_FACULTY_NAV = [
  ...TEACHING_NAV,
  { action: "toggle-faculty-mode", glyph: "\u21C4", label: "Return to HOD view", divider: true },
];

export const NAVIGATION = {
  [ROLES.FACULTY]: TEACHING_NAV,
  [ROLES.COURSE_COORDINATOR]: TEACHING_NAV,
  [ROLES.HOD]: HOD_NAV,
  [ROLES.REVIEWER]: [
    { to: "/dashboard", glyph: "\u25A6", label: "Dashboard" },
    { to: "/approval-queue", glyph: "\u25A4", label: "Review queue" },
    { to: "/approved", glyph: "\u2713", label: "Approved papers" },
    { to: "/backups", glyph: "\u25A1", label: "Backup sets" },
    { to: "/profile", glyph: "\u25CB", label: "Profile" },
  ],
  [ROLES.INSTITUTION_ADMIN]: [
    { to: "/dashboard", glyph: "\u25A6", label: "Dashboard" },
    { to: "/departments", glyph: "\u25A4", label: "Departments" },
    { to: "/faculty", glyph: "\u25CE", label: "Faculty & Users" },
    { to: "/question-bank", glyph: "\u25A4", label: "Question Bank" },
    { to: "/templates", glyph: "\u2726", label: "Templates" },
    { to: "/settings", glyph: "\u2699", label: "Settings" },
    { to: "/plan", glyph: "\u25D2", label: "Plan & usage" },
    { to: "/activity", glyph: "\u25A1", label: "Activity" },
    { to: "/profile", glyph: "\u25CB", label: "Profile" },
  ],
  [ROLES.SUPER_ADMIN]: [
    { to: "/dashboard", glyph: "\u25A6", label: "Dashboard" },
    { to: "/institutions", glyph: "\u25A4", label: "Institutions" },
    { to: "/billing", glyph: "\u25D2", label: "Billing & Plans" },
    { to: "/ml-health", glyph: "\u2726", label: "ML health" },
    { to: "/audit-log", glyph: "\u25A4", label: "Audit log" },
    { to: "/profile", glyph: "\u25CB", label: "Profile" },
  ],
};

export function navigationFor(role, { actingAsFaculty = false } = {}) {
  if (role === ROLES.HOD && actingAsFaculty) return HOD_ACTING_AS_FACULTY_NAV;
  return NAVIGATION[role] || [];
}

export function landingRouteFor(role) {
  const items = navigationFor(role);
  return items.length ? items[0].to : "/dashboard";
}
