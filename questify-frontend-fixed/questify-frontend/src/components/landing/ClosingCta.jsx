import { Link } from "react-router-dom";
import Reveal from "../common/Reveal.jsx";

export default function ClosingCta() {
  return (
    <section>
      <div className="container">
        <Reveal className="closing">
          <div className="closing-inner">
            <span className="eyebrow">Ready when you are</span>
            <h2>Set it once, let QuestiFy build it.</h2>
            <div className="closing-ctas">
              <Link to="/login" className="btn btn-primary">Start free</Link>
            </div>
            <p className="closing-note">No credit card required</p>
          </div>
        </Reveal>
      </div>
    </section>
  );
}
