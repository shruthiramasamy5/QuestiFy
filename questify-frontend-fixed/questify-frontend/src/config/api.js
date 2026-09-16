export const API_BASE_URL =
  (import.meta.env.VITE_API_BASE_URL !== undefined && import.meta.env.VITE_API_BASE_URL !== "")
    ? import.meta.env.VITE_API_BASE_URL
    : (typeof window !== "undefined"
        ? (window.location.port === "5173" || window.location.port === "3000"
            ? `${window.location.protocol}//${window.location.hostname}:8000`
            : "")
        : "http://localhost:8000");

// Endpoint paths are declared in one place so they can be aligned with the
// Spring Boot API contract without touching components. Every path here is
// routed through api-gateway (see api-gateway/src/main/resources/application.yml).
export const ENDPOINTS = {
  auth: {
    login: "/api/auth/login",
    logout: "/api/auth/logout",
    currentUser: "/api/auth/me",
    users: "/api/auth/users",
    userById: (id) => `/api/auth/users/${id}`,
  },

  // institution-service
  institutions: {
    list: "/api/institutions",
    me: "/api/institutions/me",
    byId: (id) => `/api/institutions/${id}`,
    status: (id) => `/api/institutions/${id}/status`,
  },
  departments: {
    list: "/api/departments",
    active: "/api/departments/active",
    byId: (id) => `/api/departments/${id}`,
  },
  templates: {
    list: "/api/templates",
    byId: (id) => `/api/templates/${id}`,
  },
  planUsage: {
    stats: "/api/plan-usage/stats",
    history: "/api/plan-usage/history",
    billingSummary: "/api/plan-usage/billing-summary",
  },

  // question-bank-service
  questions: {
    list: "/api/questions",
    byId: (id) => `/api/questions/${id}`,
    bulkUpload: "/api/questions/bulk-upload",
    syllabusUpload: "/api/questions/syllabus-upload",
  },
  questionDrafts: {
    generate: "/api/question-drafts/generate",
    list: "/api/question-drafts",
    byId: (id) => `/api/question-drafts/${id}`,
    approve: (id) => `/api/question-drafts/${id}/approve`,
    discard: (id) => `/api/question-drafts/${id}/discard`,
  },
  coMapping: {
    courseOutcomes: "/api/co-mapping/course-outcomes",
    courseOutcomeById: (id) => `/api/co-mapping/course-outcomes/${id}`,
    forQuestion: (questionId) => `/api/co-mapping/questions/${questionId}`,
    map: "/api/co-mapping",
    unmap: (questionId, courseOutcomeId) =>
      `/api/co-mapping/questions/${questionId}/course-outcomes/${courseOutcomeId}`,
  },

  // paper-generation-service
  generate: {
    create: "/api/generate",
    byId: (id) => `/api/generate/${id}`,
    list: "/api/generate",
  },

  // papers-service
  papers: {
    list: "/api/papers",
    byId: (id) => `/api/papers/${id}`,
    create: "/api/papers",
    fromDraft: "/api/papers/from-draft",
    evaluation: (id) => `/api/papers/${id}/evaluation`,
  },

  // approval-service
  approvals: {
    list: "/api/approvals",
    byId: (id) => `/api/approvals/${id}`,
    create: "/api/approvals",
    approve: (id) => `/api/approvals/${id}/approve`,
    reject: (id) => `/api/approvals/${id}/reject`,
  },

  // analytics-service
  analytics: {
    overview: "/api/analytics",
    questions: "/api/analytics/questions",
    papers: "/api/analytics/papers",
    approvals: "/api/analytics/approvals",
  },

  // backup-service
  backups: {
    list: "/api/backups",
    byId: (id) => `/api/backups/${id}`,
    create: "/api/backups",
  },

  // audit-log-service
  auditEvents: {
    list: "/api/audit-events",
    create: "/api/audit-events",
  },

  // ml-health-service
  mlHealth: {
    current: "/api/ml-health",
    probe: "/api/ml-health/probe",
  },
};