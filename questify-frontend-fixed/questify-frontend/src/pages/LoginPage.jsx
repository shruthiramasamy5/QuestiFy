import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import BrandMark from "../components/common/BrandMark.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import { validateLogin } from "../utils/validation.js";
import { toUserMessage } from "../utils/errors.js";
import { landingRouteFor } from "../config/navigation.js";

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [values, setValues] = useState({ email: "", password: "" });
  const [errors, setErrors] = useState({});
  const [formError, setFormError] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  function handleChange(event) {
    const { name, value } = event.target;
    setValues((prev) => ({ ...prev, [name]: value }));
    setErrors((prev) => ({ ...prev, [name]: undefined }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setFormError("");
    const validationErrors = validateLogin(values);
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length > 0) return;

    setSubmitting(true);
    try {
      const user = await login({ email: values.email.trim().toLowerCase(), password: values.password });
      const redirectTo = location.state?.from || landingRouteFor(user?.role);
      navigate(redirectTo, { replace: true });
    } catch (error) {
      console.error("[QuestiFy] Sign in failed:", error);
      setFormError(toUserMessage(error, "Invalid email or password."));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="form-wrap">
      <Link to="/" className="mobile-brand">
        <span className="auth-logo-mark">
          <BrandMark />
        </span>
        <span className="brand-word">QuestiFy</span>
      </Link>

      <h1 className="form-heading">Sign in to QuestiFy</h1>
      <p className="form-subtext">Enter your institution email and password.</p>

      {formError ? (
        <div className="auth-error" role="alert">
          {formError}
        </div>
      ) : null}

      <form onSubmit={handleSubmit} noValidate>
        <div className="auth-field">
          <label htmlFor="email">Email</label>
          <div className="input-wrap">
            <input
              id="email"
              name="email"
              type="email"
              autoComplete="email"
              placeholder="you@institution.edu"
              value={values.email}
              onChange={handleChange}
              className={errors.email ? "invalid" : ""}
              aria-invalid={Boolean(errors.email)}
            />
          </div>
          {errors.email ? <p className="field-error">{errors.email}</p> : null}
        </div>

        <div className="auth-field">
          <label htmlFor="password">Password</label>
          <div className="input-wrap">
            <input
              id="password"
              name="password"
              type={showPassword ? "text" : "password"}
              autoComplete="current-password"
              placeholder="&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;"
              className={`pw-input ${errors.password ? "invalid" : ""}`}
              value={values.password}
              onChange={handleChange}
              aria-invalid={Boolean(errors.password)}
            />
            <button
              type="button"
              className="eye-toggle"
              aria-label={showPassword ? "Hide password" : "Show password"}
              aria-pressed={showPassword}
              onClick={() => setShowPassword((v) => !v)}
            >
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                <path
                  d="M1.5 9C1.5 9 4.2 3.75 9 3.75C13.8 3.75 16.5 9 16.5 9C16.5 9 13.8 14.25 9 14.25C4.2 14.25 1.5 9 1.5 9Z"
                  stroke="currentColor"
                  strokeWidth="1.4"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
                <circle cx="9" cy="9" r="2.25" stroke="currentColor" strokeWidth="1.4" />
                {showPassword ? <path d="M2.5 15.5L15.5 2.5" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" /> : null}
              </svg>
            </button>
          </div>
          {errors.password ? <p className="field-error">{errors.password}</p> : null}
        </div>

        <div className="forgot-row">
          <Link to="/login" className="forgot-link">Forgot password?</Link>
        </div>

        <button type="submit" className="btn-signin" disabled={submitting}>
          {submitting ? "Signing in\u2026" : "Sign in"}
        </button>
      </form>

      <hr className="auth-divider" />

      <div style={{ marginBottom: "1rem" }}>
        <p style={{ fontSize: "12px", color: "var(--qf-muted, #666)", marginBottom: "0.5rem" }}>
          Demo quick-fill accounts:
        </p>
        <div style={{ display: "flex", gap: "6px", flexWrap: "wrap" }}>
          {[
            { label: "Super Admin", email: "superadmin@questify.dev", pass: "Questify@123" },
            { label: "Inst Admin", email: "admin@questify.dev", pass: "Questify@123" },
            { label: "Faculty", email: "faculty@questify.dev", pass: "Questify@123" },
            { label: "HOD", email: "hod@questify.dev", pass: "Questify@123" },
            { label: "Reviewer", email: "reviewer@questify.dev", pass: "Questify@123" },
            { label: "Coordinator", email: "coordinator@questify.dev", pass: "Questify@123" },
          ].map((d) => (
            <button
              key={d.email}
              type="button"
              className="ws-btn ghost"
              style={{ fontSize: "11px", padding: "3px 8px", background: "var(--qf-bg, #f4f1ea)" }}
              onClick={() => {
                setValues({ email: d.email, password: d.pass });
                setErrors({});
                setFormError("");
              }}
            >
              {d.label}
            </button>
          ))}
        </div>
      </div>

      <p className="invite-note">
        Don&rsquo;t have an account? Ask your <strong>institution admin</strong> to send you an invite &mdash; QuestiFy
        accounts aren&rsquo;t self-signed-up.
      </p>
    </div>
  );
}
