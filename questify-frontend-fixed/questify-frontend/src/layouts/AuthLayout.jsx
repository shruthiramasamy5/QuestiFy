import { Outlet } from "react-router-dom";
import BrandMark from "../components/common/BrandMark.jsx";

export default function AuthLayout() {
  return (
    <div className="auth-screen">
      <aside className="brand-panel">
        <div className="brand-panel-inner">
          <div className="brand-lockup">
            <span className="auth-logo-mark">
              <BrandMark size={17} stroke="#F5E4DC" />
            </span>
            <span className="brand-word">QuestiFy</span>
          </div>
          <p className="slogan">&ldquo;Set it once, let QuestiFy build it.&rdquo;</p>
          <div className="auth-rule" />
          <p className="support-line">
            CO and K-level mapping, balanced paper generation, and multi-level approval &mdash; all in one place for your
            department.
          </p>
        </div>
      </aside>
      <main className="form-panel">
        <Outlet />
      </main>
    </div>
  );
}
