// Shared enum-like option lists and small display helpers used by the
// workspace pages (dropdown options + status/label formatting).

export function titleCase(value) {
  if (!value) return "";
  return String(value)
    .toLowerCase()
    .split(/[_\s]+/)
    .filter(Boolean)
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
}

const GOOD_STATUSES = new Set(["APPROVED", "ACTIVE", "COMPLETED", "HEALTHY", "PUBLISHED"]);
const DANGER_STATUSES = new Set(["REJECTED", "FAILED", "INACTIVE", "SUSPENDED", "CANCELLED", "EXPIRED"]);
const WARN_STATUSES = new Set(["PENDING", "IN_REVIEW", "DRAFT", "PROCESSING", "GENERATING"]);

export function badgeClassFor(status) {
  const normalized = String(status || "").toUpperCase();
  if (GOOD_STATUSES.has(normalized)) return "badge badge-good";
  if (DANGER_STATUSES.has(normalized)) return "badge badge-danger";
  if (WARN_STATUSES.has(normalized)) return "badge badge-warn";
  return "badge badge-info";
}

export const DIFFICULTY_OPTIONS = ["EASY", "MEDIUM", "HARD"];

export const QUESTION_TYPE_OPTIONS = [
  "MCQ",
  "SHORT_ANSWER",
  "LONG_ANSWER",
  "TRUE_FALSE",
  "FILL_IN_THE_BLANK",
];

export const BLOOM_LEVEL_OPTIONS = [
  { value: "K1", label: "REMEMBER" },
  { value: "K2", label: "UNDERSTAND" },
  { value: "K3", label: "APPLY" },
  { value: "K4", label: "ANALYZE" },
  { value: "K5", label: "EVALUATE" },
  { value: "K6", label: "CREATE" },
];

export const TEMPLATE_TYPE_OPTIONS = ["INTERNAL", "EXTERNAL", "PRACTICAL", "MODEL"];

export const PLAN_TIER_OPTIONS = ["FREE", "STANDARD", "PREMIUM", "ENTERPRISE"];

export const EXAM_TYPE_OPTIONS = ["INTERNAL", "EXTERNAL", "MODEL", "SUPPLEMENTARY"];

export const INSTITUTION_STATUS_OPTIONS = ["ACTIVE", "SUSPENDED", "PENDING", "INACTIVE"];
