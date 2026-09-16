import { useEffect, useState } from "react";
import PageHeader from "../components/common/PageHeader.jsx";
import LoadingState from "../components/common/LoadingState.jsx";
import ErrorState from "../components/common/ErrorState.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import institutionService from "../services/institutionService.js";
import { PLAN_TIER_OPTIONS, INSTITUTION_STATUS_OPTIONS, badgeClassFor, titleCase } from "../utils/options.js";

const EMPTY = {
  name: "", code: "", contactEmail: "", contactPhone: "", address: "", city: "", country: "", timezone: "",
  planTier: "FREE", status: "PENDING", maxUsers: 50, maxPapersPerMonth: 100, maxStorageMb: 1024, monthlyPrice: 0,
  subscriptionStartDate: "", subscriptionEndDate: "",
};

export default function InstitutionsPage() {
  const [page, setPage] = useState({ content: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY);
  const [saveError, setSaveError] = useState(null);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const params = { size: 100 };
      if (search) params.search = search;
      if (statusFilter) params.status = statusFilter;
      const data = await institutionService.list(params);
      setPage(Array.isArray(data) ? { content: data } : data || { content: [] });
    } catch (err) {
      setError(err?.response?.data?.message || "Could not load institutions.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [search, statusFilter]);

  function openCreate() {
    setEditingId(null);
    setForm(EMPTY);
    setSaveError(null);
    setFormOpen(true);
  }

  function openEdit(inst) {
    setEditingId(inst.id);
    setForm({
      name: inst.name, code: inst.code, contactEmail: inst.contactEmail, contactPhone: inst.contactPhone || "",
      address: inst.address || "", city: inst.city || "", country: inst.country || "", timezone: inst.timezone || "",
      planTier: inst.planTier, status: inst.status, maxUsers: inst.maxUsers, maxPapersPerMonth: inst.maxPapersPerMonth,
      maxStorageMb: inst.maxStorageMb, monthlyPrice: inst.monthlyPrice,
      subscriptionStartDate: inst.subscriptionStartDate || "", subscriptionEndDate: inst.subscriptionEndDate || "",
    });
    setSaveError(null);
    setFormOpen(true);
  }

  async function submit(e) {
    e.preventDefault();
    setSaveError(null);
    try {
      if (editingId) await institutionService.update(editingId, form);
      else await institutionService.create(form);
      setFormOpen(false);
      await load();
    } catch (err) {
      setSaveError(err?.response?.data?.message || "Could not save the institution.");
    }
  }

  async function setStatus(id, status) {
    try {
      await institutionService.updateStatus(id, status);
      await load();
    } catch (err) {
      setError(err?.response?.data?.message || "Could not update the status.");
    }
  }

  return (
    <>
      <PageHeader
        title="Institutions"
        subtitle="Institutions on the platform"
        actions={<button type="button" className="ws-btn primary" onClick={openCreate}>+ New institution</button>}
      />

      <section className="panel">
        <div className="toolbar">
          <div className="toolbar-filters">
            <input placeholder="Search by name or code" value={search} onChange={(e) => setSearch(e.target.value)} />
            <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
              <option value="">All statuses</option>
              {INSTITUTION_STATUS_OPTIONS.map((s) => <option key={s} value={s}>{titleCase(s)}</option>)}
            </select>
          </div>
        </div>

        {formOpen && (
          <form onSubmit={submit}>
            <div className="form-grid">
              <div className="field"><label>Name</label><input required value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} /></div>
              <div className="field"><label>Code</label><input required value={form.code} onChange={(e) => setForm((f) => ({ ...f, code: e.target.value }))} /></div>
              <div className="field"><label>Contact email</label><input required type="email" value={form.contactEmail} onChange={(e) => setForm((f) => ({ ...f, contactEmail: e.target.value }))} /></div>
              <div className="field"><label>Contact phone</label><input value={form.contactPhone} onChange={(e) => setForm((f) => ({ ...f, contactPhone: e.target.value }))} /></div>
              <div className="field"><label>City</label><input value={form.city} onChange={(e) => setForm((f) => ({ ...f, city: e.target.value }))} /></div>
              <div className="field"><label>Country</label><input value={form.country} onChange={(e) => setForm((f) => ({ ...f, country: e.target.value }))} /></div>
              <div className="field">
                <label>Plan tier</label>
                <select value={form.planTier} onChange={(e) => setForm((f) => ({ ...f, planTier: e.target.value }))}>
                  {PLAN_TIER_OPTIONS.map((t) => <option key={t} value={t}>{titleCase(t)}</option>)}
                </select>
              </div>
              <div className="field">
                <label>Status</label>
                <select value={form.status} onChange={(e) => setForm((f) => ({ ...f, status: e.target.value }))}>
                  {INSTITUTION_STATUS_OPTIONS.map((s) => <option key={s} value={s}>{titleCase(s)}</option>)}
                </select>
              </div>
              <div className="field"><label>Max users</label><input type="number" value={form.maxUsers} onChange={(e) => setForm((f) => ({ ...f, maxUsers: Number(e.target.value) }))} /></div>
              <div className="field"><label>Max papers/month</label><input type="number" value={form.maxPapersPerMonth} onChange={(e) => setForm((f) => ({ ...f, maxPapersPerMonth: Number(e.target.value) }))} /></div>
              <div className="field"><label>Max storage (MB)</label><input type="number" value={form.maxStorageMb} onChange={(e) => setForm((f) => ({ ...f, maxStorageMb: Number(e.target.value) }))} /></div>
              <div className="field"><label>Monthly price</label><input type="number" step="0.01" value={form.monthlyPrice} onChange={(e) => setForm((f) => ({ ...f, monthlyPrice: Number(e.target.value) }))} /></div>
              <div className="field"><label>Subscription start</label><input type="date" value={form.subscriptionStartDate} onChange={(e) => setForm((f) => ({ ...f, subscriptionStartDate: e.target.value }))} /></div>
              <div className="field"><label>Subscription end</label><input type="date" value={form.subscriptionEndDate} onChange={(e) => setForm((f) => ({ ...f, subscriptionEndDate: e.target.value }))} /></div>
              <div className="field wide"><label>Address</label><textarea value={form.address} onChange={(e) => setForm((f) => ({ ...f, address: e.target.value }))} /></div>
            </div>
            {saveError && <p className="inline-error">{saveError}</p>}
            <div className="form-actions">
              <button type="submit" className="ws-btn primary">{editingId ? "Save changes" : "Create"}</button>
              <button type="button" className="ws-btn ghost" onClick={() => setFormOpen(false)}>Cancel</button>
            </div>
          </form>
        )}

        {loading ? (
          <LoadingState message="Loading institutions\u2026" />
        ) : error ? (
          <ErrorState message={error} onRetry={load} />
        ) : (page.content || []).length === 0 ? (
          <EmptyState glyph={"\u25A6"} title="No institutions yet" />
        ) : (
          <table className="data-table">
            <thead><tr><th>Name</th><th>Code</th><th>Plan</th><th>Status</th><th /></tr></thead>
            <tbody>
              {page.content.map((inst) => (
                <tr key={inst.id}>
                  <td>{inst.name}</td>
                  <td>{inst.code}</td>
                  <td>{titleCase(inst.planTier)}</td>
                  <td><span className={badgeClassFor(inst.status)}>{titleCase(inst.status)}</span></td>
                  <td>
                    <div className="row-actions">
                      <button type="button" className="ws-btn ghost" onClick={() => openEdit(inst)}>Edit</button>
                      {inst.status !== "ACTIVE" && <button type="button" className="ws-btn ghost" onClick={() => setStatus(inst.id, "ACTIVE")}>Activate</button>}
                      {inst.status !== "SUSPENDED" && <button type="button" className="ws-btn ghost" onClick={() => setStatus(inst.id, "SUSPENDED")}>Suspend</button>}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </>
  );
}