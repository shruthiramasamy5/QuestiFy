import { Navigate, Route, Routes } from "react-router-dom";

import PublicLayout from "../layouts/PublicLayout.jsx";
import AuthLayout from "../layouts/AuthLayout.jsx";
import MainLayout from "../layouts/MainLayout.jsx";
import ProtectedRoute from "./ProtectedRoute.jsx";

import LandingPage from "../pages/LandingPage.jsx";
import LoginPage from "../pages/LoginPage.jsx";
import DashboardPage from "../pages/DashboardPage.jsx";
import UnauthorizedPage from "../pages/UnauthorizedPage.jsx";
import NotFoundPage from "../pages/NotFoundPage.jsx";

import QuestionBankPage from "../pages/QuestionBankPage.jsx";
import CoMappingPage from "../pages/CoMappingPage.jsx";
import GeneratePaperPage from "../pages/GeneratePaperPage.jsx";
import PapersPage from "../pages/PapersPage.jsx";
import AnalyticsPage from "../pages/AnalyticsPage.jsx";

import ApprovalQueuePage from "../pages/ApprovalQueuePage.jsx";
import ApprovedPage from "../pages/ApprovedPage.jsx";
import BackupsPage from "../pages/BackupsPage.jsx";

import FacultyPage from "../pages/FacultyPage.jsx";
import DepartmentsPage from "../pages/DepartmentsPage.jsx";
import TemplatesPage from "../pages/TemplatesPage.jsx";
import SettingsPage from "../pages/SettingsPage.jsx";
import PlanUsagePage from "../pages/PlanUsagePage.jsx";
import ActivityPage from "../pages/ActivityPage.jsx";

import InstitutionsPage from "../pages/InstitutionsPage.jsx";
import BillingPage from "../pages/BillingPage.jsx";
import MlHealthPage from "../pages/MlHealthPage.jsx";
import AuditLogPage from "../pages/AuditLogPage.jsx";
import ProfilePage from "../pages/ProfilePage.jsx";

import { useAuth } from "../context/AuthContext.jsx";
import { ROLES } from "../utils/roles.js";

const ROUTES = [
  {
    path: "/question-bank",
    element: <QuestionBankPage />,
    roles: [ROLES.FACULTY, ROLES.COURSE_COORDINATOR, ROLES.HOD, ROLES.INSTITUTION_ADMIN],
  },
  {
    path: "/co-mapping",
    element: <CoMappingPage />,
    roles: [ROLES.FACULTY, ROLES.COURSE_COORDINATOR, ROLES.HOD, ROLES.INSTITUTION_ADMIN],
  },
  {
    path: "/generate-paper",
    element: <GeneratePaperPage />,
    roles: [ROLES.FACULTY, ROLES.COURSE_COORDINATOR, ROLES.HOD],
  },
  {
    path: "/papers",
    element: <PapersPage />,
    roles: [ROLES.FACULTY, ROLES.COURSE_COORDINATOR, ROLES.HOD, ROLES.REVIEWER, ROLES.INSTITUTION_ADMIN],
  },
  {
    path: "/analytics",
    element: <AnalyticsPage />,
    roles: [ROLES.FACULTY, ROLES.COURSE_COORDINATOR, ROLES.HOD, ROLES.INSTITUTION_ADMIN, ROLES.SUPER_ADMIN, ROLES.REVIEWER],
  },

  {
    path: "/approval-queue",
    element: <ApprovalQueuePage />,
    roles: [ROLES.HOD, ROLES.REVIEWER, ROLES.COURSE_COORDINATOR],
  },
  {
    path: "/approved",
    element: <ApprovedPage />,
    roles: [ROLES.REVIEWER, ROLES.HOD, ROLES.FACULTY, ROLES.COURSE_COORDINATOR],
  },
  {
    path: "/backups",
    element: <BackupsPage />,
    roles: [ROLES.REVIEWER, ROLES.INSTITUTION_ADMIN, ROLES.SUPER_ADMIN],
  },

  {
    path: "/faculty",
    element: <FacultyPage />,
    roles: [ROLES.HOD, ROLES.INSTITUTION_ADMIN, ROLES.SUPER_ADMIN],
  },
  {
    path: "/departments",
    element: <DepartmentsPage />,
    roles: [ROLES.INSTITUTION_ADMIN, ROLES.SUPER_ADMIN],
  },
  {
    path: "/templates",
    element: <TemplatesPage />,
    roles: [ROLES.INSTITUTION_ADMIN, ROLES.SUPER_ADMIN],
  },
  {
    path: "/settings",
    element: <SettingsPage />,
    roles: null,
  },
  {
    path: "/plan",
    element: <PlanUsagePage />,
    roles: [ROLES.INSTITUTION_ADMIN, ROLES.SUPER_ADMIN],
  },
  {
    path: "/activity",
    element: <ActivityPage />,
    roles: [ROLES.INSTITUTION_ADMIN, ROLES.SUPER_ADMIN],
  },

  {
    path: "/institutions",
    element: <InstitutionsPage />,
    roles: [ROLES.SUPER_ADMIN],
  },
  {
    path: "/billing",
    element: <BillingPage />,
    roles: [ROLES.SUPER_ADMIN],
  },
  {
    path: "/ml-health",
    element: <MlHealthPage />,
    roles: [ROLES.SUPER_ADMIN],
  },
  {
    path: "/audit-log",
    element: <AuditLogPage />,
    roles: [ROLES.SUPER_ADMIN],
  },

  {
    path: "/profile",
    element: <ProfilePage />,
    roles: null,
  },
];

function LoginRoute() {
  const { isAuthenticated } = useAuth();

  return isAuthenticated ? (
    <Navigate to="/dashboard" replace />
  ) : (
    <LoginPage />
  );
}

export default function AppRoutes() {
  return (
    <Routes>
      {/* Public pages */}
      <Route element={<PublicLayout />}>
        <Route path="/" element={<LandingPage />} />
      </Route>

      {/* Authentication */}
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<LoginRoute />} />
      </Route>

      {/* Protected application */}
      <Route element={<ProtectedRoute />}>
        <Route element={<MainLayout />}>

          {/* Workspace dashboard for all roles */}
          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<DashboardPage />} />
          </Route>

          {/* Role-based pages */}
          {ROUTES.map((route) => (
            <Route
              key={route.path}
              element={<ProtectedRoute allowedRoles={route.roles} />}
            >
              <Route
                path={route.path}
                element={route.element}
              />
            </Route>
          ))}

        </Route>
      </Route>

      {/* Error pages */}
      <Route
        path="/unauthorized"
        element={<UnauthorizedPage />}
      />

      <Route
        path="*"
        element={<NotFoundPage />}
      />
    </Routes>
  );
}