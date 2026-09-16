import PageHeader from "../components/common/PageHeader.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import { ROLE_LABELS, WORKSPACE_LABELS, initialsOf } from "../utils/roles.js";

export default function ProfilePage() {
  const { user, logout } = useAuth();

  return (
    <>
      <PageHeader title="Profile" subtitle="Your account details" />
      <section className="panel">
        <div style={{ display: "flex", gap: 16, alignItems: "center", marginBottom: 20 }}>
          <div className="avatar" style={{ width: 56, height: 56, fontSize: 18 }}>
            {initialsOf(user?.name)}
          </div>
          <div>
            <div style={{ fontWeight: 600, fontSize: 16 }}>{user?.name || "\u2014"}</div>
            <div className="state-msg" style={{ margin: 0 }}>
              {ROLE_LABELS[user?.role] || "\u2014"} &middot; {WORKSPACE_LABELS[user?.role] || "Workspace"}
            </div>
          </div>
        </div>

        <table className="data-table">
          <tbody>
            <tr>
              <td style={{ width: 180, opacity: 0.65 }}>Email</td>
              <td>{user?.email || "\u2014"}</td>
            </tr>
            <tr>
              <td style={{ opacity: 0.65 }}>Role</td>
              <td>{ROLE_LABELS[user?.role] || "\u2014"}</td>
            </tr>
            {user?.department ? (
              <tr>
                <td style={{ opacity: 0.65 }}>Department</td>
                <td>{user.department}</td>
              </tr>
            ) : null}
            {(user?.institutionName || user?.institution) ? (
              <tr>
                <td style={{ opacity: 0.65 }}>Institution</td>
                <td>{user.institutionName || user.institution}</td>
              </tr>
            ) : null}
          </tbody>
        </table>

        <button type="button" className="ws-btn ghost" style={{ marginTop: 20 }} onClick={logout}>
          Sign out
        </button>
      </section>
    </>
  );
}
