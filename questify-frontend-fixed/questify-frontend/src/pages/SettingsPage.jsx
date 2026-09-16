import { useEffect, useState } from "react";
import PageHeader from "../components/common/PageHeader.jsx";
import LoadingState from "../components/common/LoadingState.jsx";
import ErrorState from "../components/common/ErrorState.jsx";
import institutionService from "../services/institutionService.js";
import { PLAN_TIER_OPTIONS, titleCase } from "../utils/options.js";

export default function SettingsPage() {
  const [institution, setInstitution] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [form, setForm] = useState(null);
  const [saveMsg, setSaveMsg] = useState(null);
  const [saving, setSaving] = useState(false);
  const [currentTheme, setCurrentTheme] = useState(() => localStorage.getItem("questify_theme") || "light");

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const data = await institutionService.me();
      setInstitution(data);
      setForm({
        name: data.name, code: data.code, contactEmail: data.contactEmail, contactPhone: data.contactPhone || "",
        address: data.address || "", city: data.city || "", country: data.country || "", timezone: data.timezone || "",
        planTier: data.planTier, status: data.status, maxUsers: data.maxUsers, maxPapersPerMonth: data.maxPapersPerMonth,
        maxStorageMb: data.maxStorageMb, monthlyPrice: data.monthlyPrice,
        subscriptionStartDate: data.subscriptionStartDate || "", subscriptionEndDate: data.subscriptionEndDate || "",
      });
    } catch (err) {
      setError(err?.response?.data?.message || "Could not load your institution profile.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  function handleThemeChange(theme) {
    setCurrentTheme(theme);
    document.documentElement.setAttribute("data-theme", theme);
    document.body.setAttribute("data-theme", theme);
    localStorage.setItem("questify_theme", theme);
  }

  async function submit(e) {
    e.preventDefault();
    setSaving(true);
    setSaveMsg(null);
    try {
      const updated = await institutionService.update(institution.id, form);
      setInstitution(updated);
      setSaveMsg("Institution profile settings saved successfully.");
    } catch (err) {
      setSaveMsg(err?.response?.data?.message || "Could not save your institution settings.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <PageHeader title="Settings" subtitle="Institution configuration, contact details, and appearance preferences" />

      {/* Theme Settings */}
      <section className="panel" style={{ marginBottom: "1.5rem" }}>
        <div className="panel-head">
          <div>
            <div className="panel-title">Appearance & Workspace Theme</div>
            <div className="panel-sub">Customize the visual appearance and contrast of QuestiFy</div>
          </div>
        </div>
        <div style={{ padding: "18px", display: "flex", gap: "12px", flexWrap: "wrap" }}>
          <button
            type="button"
            className={`ws-btn ${currentTheme === "light" ? "primary" : "ghost"}`}
            onClick={() => handleThemeChange("light")}
          >
            ☀️ Warm Paper Light (Default)
          </button>
          <button
            type="button"
            className={`ws-btn ${currentTheme === "dark" ? "primary" : "ghost"}`}
            onClick={() => handleThemeChange("dark")}
          >
            🌙 Deep Warm Dark Theme
          </button>
        </div>
      </section>

      <section className="panel">
        <div className="panel-head">
          <div>
            <div className="panel-title">Institution Profile</div>
            <div className="panel-sub">Manage organization details, contact info, and quota limits</div>
          </div>
        </div>
        {loading ? (
          <LoadingState message="Loading your institution…" />
        ) : error ? (
          <ErrorState message={error} onRetry={load} />
        ) : (
          <form onSubmit={submit}>
            <div className="form-grid">
              <div className="field">
                <label>Institution name</label>
                <input required value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} />
              </div>
              <div className="field">
                <label>Code</label>
                <input required value={form.code} onChange={(e) => setForm((f) => ({ ...f, code: e.target.value }))} />
              </div>
              <div className="field">
                <label>Contact email</label>
                <input required type="email" value={form.contactEmail} onChange={(e) => setForm((f) => ({ ...f, contactEmail: e.target.value }))} />
              </div>
              <div className="field">
                <label>Contact phone</label>
                <input value={form.contactPhone} onChange={(e) => setForm((f) => ({ ...f, contactPhone: e.target.value }))} />
              </div>
              <div className="field">
                <label>City</label>
                <input value={form.city} onChange={(e) => setForm((f) => ({ ...f, city: e.target.value }))} />
              </div>
              <div className="field">
                <label>Country</label>
                <input value={form.country} onChange={(e) => setForm((f) => ({ ...f, country: e.target.value }))} />
              </div>
              <div className="field">
                <label>Timezone</label>
                <input value={form.timezone} onChange={(e) => setForm((f) => ({ ...f, timezone: e.target.value }))} />
              </div>
              <div className="field">
                <label>Plan tier</label>
                <select value={form.planTier} onChange={(e) => setForm((f) => ({ ...f, planTier: e.target.value }))}>
                  {PLAN_TIER_OPTIONS.map((t) => <option key={t} value={t}>{titleCase(t)}</option>)}
                </select>
              </div>
              <div className="field wide">
                <label>Address</label>
                <textarea value={form.address} onChange={(e) => setForm((f) => ({ ...f, address: e.target.value }))} />
              </div>
            </div>
            {saveMsg && <p className="page-sub" style={{ padding: "0 18px 10px" }}>{saveMsg}</p>}
            <div className="form-actions">
              <button type="submit" className="ws-btn primary" disabled={saving}>{saving ? "Saving…" : "Save settings"}</button>
            </div>
          </form>
        )}
      </section>
    </>
  );
}