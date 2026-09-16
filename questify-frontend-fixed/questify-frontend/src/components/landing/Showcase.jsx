import Reveal from "../common/Reveal.jsx";

const SIDE_ITEMS = ["Overview", "Question bank", "Generated papers", "Approvals", "Evaluation schemes"];
const STATS = [
  { label: "CO coverage", value: "98%", color: "var(--teal)" },
  { label: "Repetition flags", value: "2", color: "var(--coral)" },
  { label: "Sets pending review", value: "1", color: "var(--indigo)" },
];
const SETS = [
  { name: "Set A \u2014 Mid-Semester", meta: "Drafted by R. Menon \u00B7 32 questions", status: "HOD approved", pill: "pill-approved" },
  { name: "Set B \u2014 Mid-Semester", meta: "Drafted by R. Menon \u00B7 32 questions", status: "HOD approved", pill: "pill-approved" },
  { name: "Set C \u2014 Mid-Semester", meta: "Drafted by A. Iyer \u00B7 32 questions", status: "Awaiting reviewer", pill: "pill-pending" },
];

export default function Showcase() {
  return (
    <section style={{ borderTop: "1px solid var(--rule)" }}>
      <div className="container">
        <Reveal className="section-head">
          <span className="eyebrow">Inside QuestiFy</span>
          <h2>What faculty and coordinators actually see</h2>
        </Reveal>
        <Reveal className="showcase">
          <div>
            <div className="paper-card">
              <div className="paper-card-head">
                <span className="paper-card-title">Coordinator dashboard &mdash; DBMS, Sem V</span>
                <span className="status-chip">3 sets ready</span>
              </div>
              <div className="dash-mock">
                <div className="dash-side">
                  {SIDE_ITEMS.map((item, index) => (
                    <div className={`dash-side-item ${index === 0 ? "active" : ""}`} key={item}>{item}</div>
                  ))}
                </div>
                <div className="dash-main">
                  <div className="dash-stat-row">
                    {STATS.map((stat) => (
                      <div className="dash-stat" key={stat.label}>
                        <div className="dash-stat-label">{stat.label}</div>
                        <div className="dash-stat-val" style={{ color: stat.color }}>{stat.value}</div>
                      </div>
                    ))}
                  </div>
                  {SETS.map((set) => (
                    <div className="approval-row" key={set.name}>
                      <div>
                        <div className="approval-set">{set.name}</div>
                        <div className="approval-meta">{set.meta}</div>
                      </div>
                      <span className={`pill ${set.pill}`}>{set.status}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
            <div className="showcase-caption">
              <h4>Coordinator dashboard</h4>
              <span>Consolidated sets, CO coverage, approval status</span>
            </div>
          </div>
        </Reveal>
      </div>
    </section>
  );
}
