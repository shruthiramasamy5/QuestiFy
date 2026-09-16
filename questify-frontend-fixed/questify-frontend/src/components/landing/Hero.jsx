import { Link } from "react-router-dom";
import Reveal from "../common/Reveal.jsx";

const SAMPLE_QUESTIONS = [
  { num: "01", text: "Explain how a B+ tree index reduces disk I/O compared to a linear scan.", co: "CO2", k: "K2" },
  { num: "02", text: "Design a normalized schema for a hostel allocation system up to 3NF.", co: "CO3", k: "K4" },
  { num: "03", text: "Evaluate the trade-offs between optimistic and pessimistic concurrency control.", co: "CO4", k: "K5" },
  { num: "04", text: "Compare ACID and BASE consistency models with one example each.", co: "CO2", k: "K2" },
];

export default function Hero() {
  return (
    <section className="hero">
      <div className="container">
        <div className="hero-grid">
          <div className="hero-copy">
            <span className="eyebrow">For colleges and universities</span>
            <h1>
              Set it once,
              <br />
              let QuestiFy build it.
            </h1>
            <p className="hero-sub">
              Define your CO and Bloom (K1&ndash;K6) targets once per course. QuestiFy generates balanced, review-ready
              question papers every exam cycle after &mdash; with repetition checks and approvals built in, not bolted on.
            </p>
            <div className="hero-ctas">
              <Link to="/login" className="btn btn-primary">Start free</Link>
              <a href="#how-it-works" className="btn btn-ghost">See how it works</a>
            </div>
            <p className="hero-cta-note">No credit card required &middot; Set up a course in under 10 minutes</p>
          </div>

          <Reveal className="hero-visual">
            <div className="paper-card margin-rule">
              <div className="paper-card-head">
                <span className="paper-card-title">generated_paper.qfy &mdash; Set A</span>
                <span className="status-chip">Balanced</span>
              </div>
              <div className="paper-card-body ruled-bg">
                <p className="paper-subject">Database Management Systems &middot; Unit III&ndash;V &middot; 60 marks</p>
                <h3 className="paper-h">Mid-Semester Examination</h3>
                {SAMPLE_QUESTIONS.map((q) => (
                  <div className="q-row" key={q.num}>
                    <span className="q-num">{q.num}</span>
                    <span className="q-text">{q.text}</span>
                    <span className="q-tags">
                      <span className="tag tag-co">{q.co}</span>
                      <span className="tag tag-k">{q.k}</span>
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </Reveal>
        </div>
      </div>
    </section>
  );
}
