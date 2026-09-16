import Reveal from "../common/Reveal.jsx";

const STEPS = [
  { title: "Question bank", body: "Faculty upload or write questions once, per unit and course." },
  { title: "CO / K-level tagging", body: "QuestiFy suggests tags; faculty confirm or adjust each one." },
  { title: "Generate paper", body: "The constraint engine builds a paper that hits your targets." },
  { title: "Repetition check", body: "Semantic matching flags anything too close to past cycles." },
  { title: "Evaluation scheme", body: "A draft marking scheme and answer key are generated for review." },
  { title: "Approval & export", body: "HOD and reviewer sign off, then export the final paper set." },
];

export default function HowItWorks() {
  return (
    <section id="how-it-works" className="ruled-bg" style={{ borderTop: "1px solid var(--rule)", borderBottom: "1px solid var(--rule)" }}>
      <div className="container">
        <Reveal className="section-head">
          <span className="eyebrow">How it works</span>
          <h2>From question bank to a signed-off paper, in six steps</h2>
        </Reveal>
        <Reveal className="flow">
          {STEPS.map((step, index) => (
            <div className="flow-step" key={step.title}>
              <div className="flow-num">{index + 1}</div>
              <h4>{step.title}</h4>
              <p>{step.body}</p>
            </div>
          ))}
        </Reveal>
      </div>
    </section>
  );
}
