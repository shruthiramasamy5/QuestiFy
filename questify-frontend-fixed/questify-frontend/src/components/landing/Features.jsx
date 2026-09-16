import Reveal from "../common/Reveal.jsx";

const FEATURES = [
  {
    tint: "rgba(60,52,137,0.10)",
    icon: (
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <rect x="3" y="3" width="6" height="6" rx="1.5" stroke="#3C3489" strokeWidth="1.6" />
        <rect x="11" y="3" width="6" height="6" rx="1.5" stroke="#3C3489" strokeWidth="1.6" />
        <rect x="3" y="11" width="6" height="6" rx="1.5" stroke="#3C3489" strokeWidth="1.6" />
        <rect x="11" y="11" width="6" height="6" rx="1.5" stroke="#3C3489" strokeWidth="1.6" />
      </svg>
    ),
    title: "CO and K1\u2013K6 mapping",
    body: "QuestiFy suggests course outcome and Bloom level tags for every question. Faculty confirm or correct them \u2014 nothing auto-publishes.",
  },
  {
    tint: "rgba(153,60,29,0.10)",
    icon: (
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <path d="M3 15L7 6L11 12L14 5L17 15" stroke="#993C1D" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
    ),
    title: "Balanced paper generation",
    body: "An explainable constraint engine shows exactly which targets were satisfied and which were relaxed, and why.",
  },
  {
    tint: "rgba(15,110,86,0.10)",
    icon: (
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <circle cx="8.5" cy="8.5" r="5" stroke="#0F6E56" strokeWidth="1.6" />
        <path d="M15 15L12.3 12.3" stroke="#0F6E56" strokeWidth="1.6" strokeLinecap="round" />
      </svg>
    ),
    title: "Repetition checking",
    body: "Semantic similarity catches reworded repeats from past cycles \u2014 not just exact-text matches.",
  },
  {
    tint: "rgba(216,90,48,0.12)",
    icon: (
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <path d="M4 4H16V13L10 16L4 13V4Z" stroke="#D85A30" strokeWidth="1.6" strokeLinejoin="round" />
        <path d="M7 8H13M7 10.5H11" stroke="#D85A30" strokeWidth="1.4" strokeLinecap="round" />
      </svg>
    ),
    title: "Evaluation scheme drafting",
    body: "A marking scheme and answer key are auto-drafted per question, and stay fully editable before finalizing.",
  },
  {
    tint: "rgba(60,52,137,0.10)",
    icon: (
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <path d="M3 10L8 15L17 5" stroke="#3C3489" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
    ),
    title: "Multi-level approval",
    body: "Coordinators draft multiple sets, the HOD approves at least two, and a reviewer gives final sign-off \u2014 with a clear audit trail.",
  },
  {
    tint: "rgba(153,60,29,0.10)",
    icon: (
      <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
        <path d="M10 2L12.2 7.4L18 8L13.6 11.8L15 17.5L10 14.3L5 17.5L6.4 11.8L2 8L7.8 7.4L10 2Z" stroke="#993C1D" strokeWidth="1.5" strokeLinejoin="round" />
      </svg>
    ),
    title: "AI appraisal scoring",
    body: "Every question is rated on clarity, K-level alignment, difficulty fit, and uniqueness \u2014 with concrete rewrite suggestions.",
  },
];

export default function Features() {
  return (
    <section id="features">
      <div className="container">
        <Reveal className="section-head">
          <span className="eyebrow">What QuestiFy does</span>
          <h2>Every step from question bank to signed-off paper</h2>
          <p>
            Faculty stay in control at every stage &mdash; QuestiFy drafts, suggests, and checks. Nothing gets published
            without a human sign-off.
          </p>
        </Reveal>
        <Reveal className="feature-grid">
          {FEATURES.map((feature) => (
            <div className="feature-card" key={feature.title}>
              <div className="feature-icon" style={{ background: feature.tint }}>{feature.icon}</div>
              <h3>{feature.title}</h3>
              <p>{feature.body}</p>
            </div>
          ))}
        </Reveal>
      </div>
    </section>
  );
}
