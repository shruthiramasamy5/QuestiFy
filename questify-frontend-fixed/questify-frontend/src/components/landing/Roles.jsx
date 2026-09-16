import Reveal from "../common/Reveal.jsx";

const ROLE_CARDS = [
  { role: "Faculty", body: "Spend less time manually drafting papers from scratch each cycle \u2014 start from a generated set and edit from there." },
  { role: "Course coordinator", body: "See consolidated question sets across every section of a course in one place, instead of chasing individual faculty." },
  { role: "HOD", body: "Get department-wide oversight without having to personally review every single paper before it goes out." },
  { role: "Reviewer", body: "Work through a clean final-approval queue that shows exactly what changed since the last review pass." },
  { role: "Institution admin", body: "Configure templates once, and get usage visibility across every department without emailing for updates." },
];

export default function Roles() {
  return (
    <section id="roles">
      <div className="container">
        <Reveal className="section-head">
          <span className="eyebrow">Built around your department</span>
          <h2>Everyone gets less busywork, not another dashboard to babysit</h2>
        </Reveal>
        <Reveal className="role-grid">
          {ROLE_CARDS.map((card) => (
            <div className="role-card" key={card.role}>
              <span className="eyebrow">{card.role}</span>
              <p>{card.body}</p>
            </div>
          ))}
        </Reveal>
      </div>
    </section>
  );
}
