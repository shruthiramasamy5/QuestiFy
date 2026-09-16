import { useEffect, useState } from "react";
import PageHeader from "../components/common/PageHeader.jsx";
import LoadingState from "../components/common/LoadingState.jsx";
import ErrorState from "../components/common/ErrorState.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import templateService from "../services/templateService.js";
import { TEMPLATE_TYPE_OPTIONS, titleCase } from "../utils/options.js";

const EMPTY = { name: "", type: "QUESTION_PAPER", description: "", headerHtml: "", footerHtml: "", instructions: "", defaultTemplate: false, active: true };

export default function TemplatesPage() {
  const [page, setPage] = useState({ content: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [typeFilter, setTypeFilter] = useState("");
  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY);
  const [saveError, setSaveError] = useState(null);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const params = { size: 100 };
      if (typeFilter) params.type = typeFilter;
      const data = await templateService.list(params);
      setPage(Array.isArray(data) ? { content: data } : data || { content: [] });
    } catch (err) {
      setError(err?.response?.data?.message || "Could not load templates.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [typeFilter]);

  function openCreate() {
    setEditingId(null);
    setForm(EMPTY);
    setSaveError(null);
    setFormOpen(true);
  }

  function openEdit(t) {
    setEditingId(t.id);
    setForm({
      name: t.name, type: t.type, description: t.description || "",
      headerHtml: t.headerHtml || "", footerHtml: t.footerHtml || "", instructions: t.instructions || "",
      defaultTemplate: t.defaultTemplate, active: t.active,
    });
    setSaveError(null);
    setFormOpen(true);
  }

  async function submit(e) {
    e.preventDefault();
    setSaveError(null);
    try {
      if (editingId) await templateService.update(editingId, form);
      else await templateService.create(form);
      setFormOpen(false);
      await load();
    } catch (err) {
      setSaveError(err?.response?.data?.message || "Could not save the template.");
    }
  }

  async function remove(id) {
    if (!window.confirm("Delete this template?")) return;
    try {
      await templateService.remove(id);
      await load();
    } catch (err) {
      setError(err?.response?.data?.message || "Could not delete the template.");
    }
  }

  return (
    <>
      <PageHeader
        title="Templates"
        subtitle="Paper and evaluation templates"
        actions={<button type="button" className="ws-btn primary" onClick={openCreate}>+ New template</button>}
      />

      <section className="panel">
        <div className="toolbar">
          <div className="toolbar-filters">
            <select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)}>
              <option value="">All types</option>
              {TEMPLATE_TYPE_OPTIONS.map((t) => <option key={t} value={t}>{titleCase(t)}</option>)}
            </select>
          </div>
        </div>

        {formOpen && (
          <form onSubmit={submit}>
            <div className="form-grid">
              <div className="field">
                <label>Name</label>
                <input required value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} />
              </div>
              <div className="field">
                <label>Type</label>
                <select value={form.type} onChange={(e) => setForm((f) => ({ ...f, type: e.target.value }))}>
                  {TEMPLATE_TYPE_OPTIONS.map((t) => <option key={t} value={t}>{titleCase(t)}</option>)}
                </select>
              </div>
              <div className="field wide">
                <label>Description</label>
                <textarea value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} />
              </div>
              <div className="field wide">
                <label>Header HTML</label>
                <textarea value={form.headerHtml} onChange={(e) => setForm((f) => ({ ...f, headerHtml: e.target.value }))} />
              </div>
              <div className="field wide">
                <label>Footer HTML</label>
                <textarea value={form.footerHtml} onChange={(e) => setForm((f) => ({ ...f, footerHtml: e.target.value }))} />
              </div>
              <div className="field wide">
                <label>Instructions</label>
                <textarea value={form.instructions} onChange={(e) => setForm((f) => ({ ...f, instructions: e.target.value }))} />
              </div>
              <div className="field">
                <label style={{ display: "flex", alignItems: "center", gap: 8, textTransform: "none", fontSize: 13 }}>
                  <input type="checkbox" checked={form.defaultTemplate} onChange={(e) => setForm((f) => ({ ...f, defaultTemplate: e.target.checked }))} />
                  Default for its type
                </label>
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
          <LoadingState message="Loading templates\u2026" />
        ) : error ? (
          <ErrorState message={error} onRetry={load} />
        ) : (page.content || []).length === 0 ? (
          <EmptyState glyph={"\u25A4"} title="No templates yet" />
        ) : (
          <table className="data-table">
            <thead><tr><th>Name</th><th>Type</th><th>Default</th><th>Status</th><th /></tr></thead>
            <tbody>
              {page.content.map((t) => (
                <tr key={t.id}>
                  <td>{t.name}</td>
                  <td>{titleCase(t.type)}</td>
                  <td>{t.defaultTemplate ? "\u2713" : ""}</td>
                  <td><span className={`badge ${t.active ? "badge-good" : "badge-neutral"}`}>{t.active ? "Active" : "Inactive"}</span></td>
                  <td>
                    <div className="row-actions">
                      <button type="button" className="ws-btn ghost" onClick={() => openEdit(t)}>Edit</button>
                      <button type="button" className="ws-btn ghost" onClick={() => remove(t.id)}>Delete</button>
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