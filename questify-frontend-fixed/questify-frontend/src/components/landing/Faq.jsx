import { useState } from "react";
import Reveal from "../common/Reveal.jsx";

const ITEMS = [
  {
    q: "Can we pilot with one department first?",
    a: "Yes. Starter is scoped to a single department so you can try the full generation and review workflow before rolling out further.",
  },
  {
    q: "Is there a limit on question bank size?",
    a: "No. Question banks are unlimited on every plan \u2014 limits only apply to generated papers per month on Starter.",
  },
  {
    q: "Do you offer academic or nonprofit discounts?",
    a: "Yes, government and state-university pricing is available on the Institution plan \u2014 talk to sales for details.",
  },
];

export default function Faq() {
  const [openIndex, setOpenIndex] = useState(null);

  return (
    <Reveal className="faq">
      {ITEMS.map((item, index) => {
        const open = openIndex === index;
        return (
          <div className={`faq-item ${open ? "open" : ""}`} key={item.q}>
            <button
              type="button"
              className="faq-q"
              aria-expanded={open}
              onClick={() => setOpenIndex(open ? null : index)}
            >
              {item.q}
              <span className="faq-plus" aria-hidden="true">+</span>
            </button>
            <div className="faq-a">{item.a}</div>
          </div>
        );
      })}
    </Reveal>
  );
}
