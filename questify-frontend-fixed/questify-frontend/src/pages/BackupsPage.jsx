import { useEffect, useState } from "react";
import PageHeader from "../components/common/PageHeader.jsx";
import LoadingState from "../components/common/LoadingState.jsx";
import ErrorState from "../components/common/ErrorState.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import backupService from "../services/backupService.js";

const EMPTY = { label: "", paperIds: "", storageReference: "" };

export default function BackupsPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [formOpen, setFormOpen] = useState(false);
  const [form, setForm] = useState(EMPTY);
  const [saveError, setSaveError] = useState(null);
  const [saving, setSaving] = useState(false);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const data = await backupService.list();
      setItems(Array.isArray(data) ? data : data?.content || []);
    } catch (err) {
      setError(err?.response?.data?.message || "Could not load backup sets.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function submit(e) {
    e.preventDefault();
    setSaving(true);
    setSaveError(null);
    try {
      const paperIds = form.paperIds
        .split(",")
        .map((s) => s.trim())
        .filter(Boolean)
        .map(Number);
      await backupService.create({ label: form.label, paperIds, storageReference: form.storageReference || null });
      setFormOpen(false);
      setForm(EMPTY);
      await load();
    } catch (err) {
      setSaveError(err?.response?.data?.message || "Could not create the backup set.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <PageHeader
        title="Backup sets"
        subtitle="Reserve paper sets"
        actions={
          <button type="button" className="ws-btn primary" onClick={() => setFormOpen((v) => !v)}>
            + New backup set
          </button>
        }
      />

      <section className="panel">
        {formOpen && (
          <form onSubmit={submit}>
            <div className="form-grid">
              <div className="field">
                <label>Label</label>
                <input value={form.label} onChange={(e) => setForm((f) => ({ ...f, label: e.target.value }))} />
              </div>
              <div className="field">
                <label>Paper IDs (comma separated)</label>
                <input required value={form.paperIds} onChange={(e) => setForm((f) => ({ ...f, paperIds: e.target.value }))} placeholder="12, 13, 14" />
              </div>
              <div className="field wide">
                <label>Storage reference</label>
                <input value={form.storageReference} onChange={(e) => setForm((f) => ({ ...f, storageReference: e.target.value }))} />
              </div>
            </div>
            {saveError && <p className="inline-error">{saveError}</p>}
            <div className="form-actions">
              <button type="submit" className="ws-btn primary" disabled={saving}>{saving ? "Creating\u2026" : "Create backup set"}</button>
              <button type="button" className="ws-btn ghost" onClick={() => setFormOpen(false)}>Cancel</button>
            </div>
          </form>
        )}

        {loading ? (
          <LoadingState message="Loading backup sets\u2026" />
        ) : error ? (
          <ErrorState message={error} onRetry={load} />
        ) : items.length === 0 ? (
          <EmptyState glyph={"\u25A1"} title="No backup sets yet" />
        ) : (
          <table className="data-table">
            <thead><tr><th>Label</th><th>Papers</th><th>Created by</th><th>Created</th></tr></thead>
            <tbody>
              {items.map((b) => (
                <tr key={b.id}>
                  <td>{b.label || `Backup #${b.id}`}</td>
                  <td>{b.paperCount}</td>
                  <td>{b.createdBy}</td>
                  <td>{b.createdAt ? new Date(b.createdAt).toLocaleString() : "\u2014"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </>
  );
}