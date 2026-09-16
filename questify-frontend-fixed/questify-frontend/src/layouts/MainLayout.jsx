import { useState } from "react";
import { Outlet, useNavigate } from "react-router-dom";
import Sidebar from "../components/layout/Sidebar.jsx";
import Topbar from "../components/layout/Topbar.jsx";
import { useAuth } from "../context/AuthContext.jsx";

export default function MainLayout() {
  const { user, logout, actingAsFaculty, toggleActingAsFaculty } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const navigate = useNavigate();

  async function handleSignOut() {
    await logout();
    navigate("/login", { replace: true });
  }

  return (
    <div className="shell">
      <Sidebar
        user={user}
        open={sidebarOpen}
        onNavigate={() => setSidebarOpen(false)}
        actingAsFaculty={actingAsFaculty}
        onToggleFacultyMode={toggleActingAsFaculty}
      />
      <div className="main">
        <Topbar
          user={user}
          onToggleSidebar={() => setSidebarOpen((v) => !v)}
          onSignOut={handleSignOut}
          actingAsFaculty={actingAsFaculty}
          onExitFacultyMode={toggleActingAsFaculty}
        />
        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
