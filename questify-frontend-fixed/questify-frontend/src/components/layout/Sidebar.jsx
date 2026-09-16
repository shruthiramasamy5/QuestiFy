import { NavLink } from "react-router-dom";
import { navigationFor } from "../../config/navigation.js";
import { ROLE_LABELS, WORKSPACE_LABELS, initialsOf } from "../../utils/roles.js";

export default function Sidebar({ user, open, onNavigate, actingAsFaculty, onToggleFacultyMode }) {
  const items = navigationFor(user?.role, { actingAsFaculty });
  const workspaceLabel = actingAsFaculty ? "Faculty workspace" : WORKSPACE_LABELS[user?.role] || "Workspace";
  const roleChipLabel = actingAsFaculty ? "ACTING AS FACULTY" : (ROLE_LABELS[user?.role] || "").toUpperCase();

  return (
    <aside className={`sidebar ${open ? "open" : ""}`}>
      <div className="brand">
        <div className="logo">?</div>
        <div>
          <div className="brand-name">QuestiFy</div>
          <div className="brand-sub">{workspaceLabel}</div>
        </div>
      </div>
      <div className="role-chip">{roleChipLabel}</div>
      <nav className="nav">
        {items.map((item) =>
          item.action === "toggle-faculty-mode" ? (
            <div key={item.action} className={item.divider ? "nav-item-group" : undefined}>
              {item.divider ? <div className="nav-divider" /> : null}
              <button
                type="button"
                className="acts-as-btn"
                onClick={() => {
                  onToggleFacultyMode?.();
                  onNavigate?.();
                }}
              >
                <span className="nav-glyph" aria-hidden="true">{item.glyph}</span>
                <span>{item.label}</span>
              </button>
            </div>
          ) : (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => (isActive ? "active" : "")}
              onClick={onNavigate}
            >
              <span className="nav-glyph" aria-hidden="true">{item.glyph}</span>
              <span>{item.label}</span>
            </NavLink>
          )
        )}
      </nav>
      <NavLink to="/profile" className="profile-mini" onClick={onNavigate}>
        <div className="avatar">{initialsOf(user?.name)}</div>
        <div>
          <div className="mini-name">{user?.name}</div>
          <div className="mini-role">
            {ROLE_LABELS[user?.role]}
            {user?.department ? ` \u00B7 ${user.department}` : ""}
          </div>
        </div>
      </NavLink>
    </aside>
  );
}
