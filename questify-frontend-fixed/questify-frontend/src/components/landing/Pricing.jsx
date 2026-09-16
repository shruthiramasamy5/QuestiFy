import { useState } from "react";
import { Link } from "react-router-dom";
import Reveal from "../common/Reveal.jsx";
import Faq from "./Faq.jsx";

const PLANS = [
  {
    name: "Starter",
    desc: "For a single department piloting the workflow before wider rollout.",
    price: { monthly: "\u20B90", annual: "\u20B90" },
    note: { monthly: "Up to 40 papers / month \u00B7 1 department", annual: "Up to 40 papers / month \u00B7 1 department" },
    cta: { label: "Start free", className: "btn-ghost" },
    features: [
      "CO / K-level tagging & paper generation",
      "Repetition checking",
      "Single-level approval",
      "Email support",
    ],
  },
  {
    name: "Department",
    featured: true,
    badge: "Most popular",
    desc: "For an active academic department running full exam cycles.",
    price: { monthly: "\u20B9499", annual: "\u20B9399" },
    period: "/ month",
    note: { monthly: "Billed monthly \u00B7 per department", annual: "Billed annually \u00B7 per department" },
    cta: { label: "Start free trial", className: "btn-primary" },
    features: [
      "Unlimited papers & question banks",
      "Full approval workflow (HOD + reviewer)",
      "AI appraisal scoring",
      "Evaluation scheme drafting",
      "Priority support",
    ],
  },
  {
    name: "Institution",
    desc: "For multi-department or university-wide rollout.",
    price: { monthly: "Custom", annual: "Custom" },
    custom: true,
    note: { monthly: "Talk to us about your department count", annual: "Talk to us about your department count" },
    cta: { label: "Talk to sales", className: "btn-indigo" },
    features: [
      "Everything in Department",
      "SSO & admin usage dashboards",
      "Custom paper templates",
      "Dedicated onboarding",
      "SLA-backed support",
    ],
  },
];

export default function Pricing() {
  const [billing, setBilling] = useState("monthly");

  return (
    <section id="pricing" style={{ borderTop: "1px solid var(--rule)" }}>
      <div className="container">
        <Reveal className="section-head center">
          <span className="eyebrow">Simple, institution-friendly pricing</span>
          <h2>Pricing that scales with your department</h2>
          <p>
            Start with one department for free. Move to full department or campus-wide rollout when you&rsquo;re ready &mdash;
            no surprise fees.
          </p>
        </Reveal>

        <Reveal className="center">
          <div className="pricing-toggle" role="group" aria-label="Billing period">
            <button
              type="button"
              className={`toggle-opt ${billing === "monthly" ? "active" : ""}`}
              onClick={() => setBilling("monthly")}
            >
              Monthly
            </button>
            <button
              type="button"
              className={`toggle-opt ${billing === "annual" ? "active" : ""}`}
              onClick={() => setBilling("annual")}
            >
              Annual <span className="toggle-save">Save 20%</span>
            </button>
          </div>
        </Reveal>

        <Reveal className="pricing-grid">
          {PLANS.map((plan) => (
            <div className={`plan ${plan.featured ? "plan-featured" : ""}`} key={plan.name}>
              {plan.badge ? <span className="plan-badge">{plan.badge}</span> : null}
              <div className="plan-name">{plan.name}</div>
              <p className="plan-desc">{plan.desc}</p>
              <div className="plan-price">
                <span className={`amount ${plan.custom ? "custom" : ""}`}>{plan.price[billing]}</span>
                {plan.period ? <span className="period">{plan.period}</span> : null}
              </div>
              <p className="plan-price-note">{plan.note[billing]}</p>
              <Link to="/login" className={`btn ${plan.cta.className} btn-block plan-cta`}>
                {plan.cta.label}
              </Link>
              <div className="plan-features">
                {plan.features.map((feature) => (
                  <div className="plan-feature" key={feature}>
                    <span className="plan-check">&#10003;</span> {feature}
                  </div>
                ))}
              </div>
            </div>
          ))}
        </Reveal>

        <Reveal as="p" className="pricing-note">No credit card required &middot; Cancel anytime</Reveal>
        <Reveal as="p" className="pricing-illustrative">
          Department pricing shown is illustrative and may vary by institution size &mdash; confirm exact figures with sales.
        </Reveal>

        <Faq />
      </div>
    </section>
  );
}
