import { useEffect, useState } from "react";
import PageHeader from "../components/common/PageHeader.jsx";
import LoadingState from "../components/common/LoadingState.jsx";
import ErrorState from "../components/common/ErrorState.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import departmentService from "../services/departmentService.js";

const EMPTY = { name: "", code: "", headName: "", headEmail: "", description: "", active: true };

export default function DepartmentsPage() {
  const [page, setPage] = useState({ content: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY);
  const [saveError, setSaveError] = useState(null);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const data = await departmentService.list({ size: 100 });
      setPage(Array.isArray(data) ? { content: data } : data || { content: [] });
    } catch (err) {
      setError(err?.response?.data?.message || "Could not load departments.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  function openCreate() {
    setEditingId(null);
    setForm(EMPTY);
    setSaveError(null);
    setFormOpen(true);
  }

  function openEdit(d) {
    setEditingId(d.id);
    setForm({ name: d.name, code: d.code, headName: d.headName || "", headEmail: d.headEmail || "", description: d.description || "", active: d.active });
    setSaveError(null);
    setFormOpen(true);
  }

  async function submit(e) {
    e.preventDefault();
    setSaveError(null);
    try {
      if (editingId) await departmentService.update(editingId, form);
      else await departmentService.create(form);
      setFormOpen(false);
      await load();
    } catch (err) {
      setSaveError(err?.response?.data?.message || "Could not save the department.");
    }
  }

  async function remove(id) {
    if (!window.confirm("Delete this department?")) return;
    try {
      await departmentService.remove(id);
      await load();
    } catch (err) {
      setError(err?.response?.data?.message || "Could not delete the department.");
    }
  }

  return (
    <>
      <PageHeader
        title="Departments"
        subtitle="Departments in your institution"
        actions={<button type="button" className="ws-btn primary" onClick={openCreate}>+ New department</button>}
      />

      <section className="panel">
        {formOpen && (
          <form onSubmit={submit}>
            <div className="form-grid">
              <div className="field">
                <label>Name</label>
                <input required value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} />
              </div>
              <div className="field">
                <label>Code</label>
                <input required value={form.code} onChange={(e) => setForm((f) => ({ ...f, code: e.target.value }))} />
              </div>
              <div className="field">
                <label>Head name</label>
                <input value={form.headName} onChange={(e) => setForm((f) => ({ ...f, headName: e.target.value }))} />
              </div>
              <div className="field">
                <label>Head email</label>
                <input type="email" value={form.headEmail} onChange={(e) => setForm((f) => ({ ...f, headEmail: e.target.value }))} />
              </div>
              <div className="field wide">
                <label>Description</label>
                <textarea value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} />
              </div>
              <div className="field">
                <label style={{ display: "flex", alignItems: "center", gap: 8, textTransform: "none", fontSize: 13 }}>
                  <input type="checkbox" checked={form.active} onChange={(e) => setForm((f) => ({ ...f, active: e.target.checked }))} />
                  Active
                </label>
              </div>
            </div>
            {saveError && <p className="inline-error">{saveError}</p>}
            <div className="form-actions">
              <button type="submit" className="ws-btn primary">{editingId ? "Save changes" : "Create"}</button>
              <button type="button" className="ws-btn ghost" onClick={() => setFormOpen(false)}>Cancel</button>
            </div>
          </form>
        )}

        {loading ? (
          <LoadingState message="Loading departments\u2026" />
        ) : error ? (
          <ErrorState message={error} onRetry={load} />
        ) : (page.content || []).length === 0 ? (
          <EmptyState glyph={"\u25A6"} title="No departments yet" />
        ) : (
          <table className="data-table">
            <thead><tr><th>Name</th><th>Code</th><th>Head</th><th>Status</th><th /></tr></thead>
            <tbody>
              {page.content.map((d) => (
                <tr key={d.id}>
                  <td>{d.name}</td>
                  <td>{d.code}</td>
                  <td>{d.headName || "\u2014"}</td>
                  <td><span className={`badge ${d.active ? "badge-good" : "badge-neutral"}`}>{d.active ? "Active" : "Inactive"}</span></td>
                  <td>
                    <div className="row-actions">
                      <button type="button" className="ws-btn ghost" onClick={() => openEdit(d)}>Edit</button>
                      <button type="button" className="ws-btn ghost" onClick={() => remove(d.id)}>Delete</button>
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