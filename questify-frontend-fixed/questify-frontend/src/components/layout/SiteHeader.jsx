import { Link } from "react-router-dom";
import BrandMark from "../common/BrandMark.jsx";

export default function SiteHeader() {
  return (
    <header className="site-header">
      <div className="container">
        <nav className="site-nav">
          <Link to="/" className="brand">
            <span className="logo-mark">
              <BrandMark />
            </span>
            QuestiFy
          </Link>
          <div className="nav-links">
            <a href="#features">Product</a>
            <a href="#how-it-works">How it works</a>
            <a href="#pricing">Pricing</a>
            <a href="#roles">For institutions</a>
          </div>
          <div className="nav-actions">
            <Link to="/login" className="nav-login">
              Log in
            </Link>
            <Link to="/login" className="btn btn-primary">
              Start free
            </Link>
          </div>
        </nav>
      </div>
    </header>
  );
}
