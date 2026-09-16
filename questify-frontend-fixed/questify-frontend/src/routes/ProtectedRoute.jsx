import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import LoadingState from "../components/common/LoadingState.jsx";
import { hasRole } from "../utils/roles.js";

export default function ProtectedRoute({ allowedRoles }) {
  const { user, isAuthenticated, initializing } = useAuth();
  const location = useLocation();

  if (initializing) return <LoadingState message={"Checking your session\u2026"} />;
  if (!isAuthenticated) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  if (!hasRole(user, allowedRoles)) return <Navigate to="/unauthorized" replace />;

  return <Outlet />;
}
