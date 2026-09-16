import { Outlet } from "react-router-dom";
import SiteHeader from "../components/layout/SiteHeader.jsx";
import SiteFooter from "../components/layout/SiteFooter.jsx";

export default function PublicLayout() {
  return (
    <div className="landing">
      <SiteHeader />
      <main id="top">
        <Outlet />
      </main>
      <SiteFooter />
    </div>
  );
}
