import { Link } from "react-router-dom";
import BrandMark from "../common/BrandMark.jsx";

export default function SiteFooter() {
  return (
    <footer className="site-footer">
      <div className="container">
        <div className="footer-top">
          <div className="footer-brand">
            <Link to="/" className="brand">
              <span className="logo-mark">
                <BrandMark />
              </span>
              QuestiFy
            </Link>
            <p>Set it once, let QuestiFy build it. CO&ndash;Bloom mapped question papers for colleges and universities.</p>
          </div>
          <div className="footer-col">
            <h5>Product</h5>
            <ul>
              <li><a href="#features">Features</a></li>
              <li><a href="#how-it-works">How it works</a></li>
              <li><a href="#pricing">Pricing</a></li>
            </ul>
          </div>
          <div className="footer-col">
            <h5>Company</h5>
            <ul>
              <li><a href="#roles">About</a></li>
              <li><a href="#roles">Careers</a></li>
              <li><a href="#pricing">Contact</a></li>
            </ul>
          </div>
          <div className="footer-col">
            <h5>Legal</h5>
            <ul>
              <li><a href="#pricing">Privacy</a></li>
              <li><a href="#pricing">Terms</a></li>
              <li><a href="#features">Security</a></li>
            </ul>
          </div>
        </div>
        <div className="footer-bottom">
          <span>&copy; {new Date().getFullYear()} QuestiFy. All rights reserved.</span>
          <span>Made for exam cells everywhere.</span>
        </div>
      </div>
    </footer>
  );
}
