import { useEffect, useState } from "react";
import PageHeader from "../components/common/PageHeader.jsx";
import LoadingState from "../components/common/LoadingState.jsx";
import ErrorState from "../components/common/ErrorState.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import coMappingService from "../services/coMappingService.js";
import { BLOOM_LEVEL_OPTIONS } from "../utils/options.js";

const EMPTY_OUTCOME = { code: "", description: "", subject: "", bloomLevel: "K2" };

export default function CoMappingPage() {
  const [outcomes, setOutcomes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_OUTCOME);
  const [saveError, setSaveError] = useState(null);

  const [questionId, setQuestionId] = useState("");
  const [mapping, setMapping] = useState(null);
  const [mapError, setMapError] = useState(null);
  const [mapLoading, setMapLoading] = useState(false);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const data = await coMappingService.listOutcomes({ size: 100 });
      setOutcomes(Array.isArray(data) ? data : data?.content || []);
    } catch (err) {
      setError(err?.response?.data?.message || "Could not load course outcomes.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  function openCreate() {
    setEditingId(null);
    setForm(EMPTY_OUTCOME);
    setSaveError(null);
    setFormOpen(true);
  }

  function openEdit(co) {
    setEditingId(co.id);
    setForm({ code: co.code, description: co.description, subject: co.subject, bloomLevel: co.bloomLevel });
    setSaveError(null);
    setFormOpen(true);
  }

  async function submit(e) {
    e.preventDefault();
    setSaveError(null);
    try {
      if (editingId) await coMappingService.updateOutcome(editingId, form);
      else await coMappingService.createOutcome(form);
      setFormOpen(false);
      await load();
    } catch (err) {
      setSaveError(err?.response?.data?.message || "Could not save the course outcome.");
    }
  }

  async function removeOutcome(id) {
    if (!window.confirm("Delete this course outcome?")) return;
    try {
      await coMappingService.deleteOutcome(id);
      await load();
    } catch (err) {
      setError(err?.response?.data?.message || "Could not delete the course outcome.");
    }
  }

  async function loadMapping() {
    if (!questionId) return;
    setMapLoading(true);
    setMapError(null);
    try {
      const [mappingData] = await Promise.all([coMappingService.getMappings(questionId)]);
      setMapping(mappingData);
    } catch (err) {
      setMapping(null);
      setMapError(err?.response?.data?.message || "Question not found.");
    } finally {
      setMapLoading(false);
    }
  }

  async function toggleMap(coId, mapped) {
    try {
      if (mapped) await coMappingService.unmap(questionId, coId);
      else await coMappingService.map({ questionId: Number(questionId), courseOutcomeIds: [coId] });
      await loadMapping();
    } catch (err) {
      setMapError(err?.response?.data?.message || "Could not update the mapping.");
    }
  }

  const mappedIds = new Set((mapping?.courseOutcomes || []).map((co) => co.id));

  return (
    <>
      <PageHeader
        title="CO mapping"
        subtitle="Course outcomes and Bloom targets"
        actions={
          <button type="button" className="ws-btn primary" onClick={openCreate}>
            + New course outcome
          </button>
        }
      />

      <section className="panel">
        <div className="panel-head">
          <div>
            <div className="panel-title">Course outcomes</div>
            <div className="panel-sub">Codes used when tagging questions</div>
          </div>
        </div>

        {formOpen && (
          <form onSubmit={submit}>
            <div className="form-grid">
              <div className="field">
                <label>Code (e.g. CO1)</label>
                <input required value={form.code} onChange={(e) => setForm((f) => ({ ...f, code: e.target.value }))} />
              </div>
              <div className="field">
                <label>Subject</label>
                <input required value={form.subject} onChange={(e) => setForm((f) => ({ ...f, subject: e.target.value }))} />
              </div>
              <div className="field">
                <label>Bloom level</label>
                <select value={form.bloomLevel} onChange={(e) => setForm((f) => ({ ...f, bloomLevel: e.target.value }))}>
                  {BLOOM_LEVEL_OPTIONS.map((b) => (
  <option key={b.value} value={b.value}>
    {b.label}
  </option>
))}
                </select>
              </div>
              <div className="field wide">
                <label>Description</label>
                <textarea
                  required
                  value={form.description}
                  onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
                />
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
          <LoadingState message="Loading course outcomes\u2026" />
        ) : error ? (
          <ErrorState message={error} onRetry={load} />
        ) : outcomes.length === 0 ? (
          <EmptyState glyph={"\u25CE"} title="No course outcomes yet" message="Add one to start mapping questions." />
        ) : (
          <table className="data-table">
            <thead>
              <tr><th>Code</th><th>Subject</th><th>Bloom</th><th>Description</th><th /></tr>
            </thead>
            <tbody>
              {outcomes.map((co) => (
                <tr key={co.id}>
                  <td><span className="tag tag-co">{co.code}</span></td>
                  <td>{co.subject}</td>
                  <td><span className="tag tag-k">{co.bloomLevel}</span></td>
                  <td style={{ maxWidth: 360 }}>{co.description}</td>
                  <td>
                    <div className="row-actions">
                      <button type="button" className="ws-btn ghost" onClick={() => openEdit(co)}>Edit</button>
                      <button type="button" className="ws-btn ghost" onClick={() => removeOutcome(co.id)}>Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      <section className="panel">
        <div className="panel-head">
          <div>
            <div className="panel-title">Map a question</div>
            <div className="panel-sub">Look up a question by ID and toggle which COs it covers</div>
          </div>
        </div>
        <div className="form-grid single">
          <div className="field" style={{ display: "flex", gap: 8, alignItems: "flex-end" }}>
            <div style={{ flex: 1 }}>
              <label>Question ID</label>
              <input value={questionId} onChange={(e) => setQuestionId(e.target.value)} placeholder="e.g. 12" />
            </div>
            <button type="button" className="ws-btn primary" onClick={loadMapping} disabled={mapLoading}>
              {mapLoading ? "Loading\u2026" : "Look up"}
            </button>
          </div>
        </div>
        {mapError && <p className="inline-error">{mapError}</p>}
        {mapping && (
          <div className="panel-body">
            <p className="page-sub" style={{ marginBottom: 10 }}>{mapping.questionText}</p>
            <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
              {outcomes.map((co) => {
                const mapped = mappedIds.has(co.id);
                return (
                  <button
                    key={co.id}
                    type="button"
                    className={mapped ? "ws-btn primary" : "ws-btn ghost"}
                    onClick={() => toggleMap(co.id, mapped)}
                  >
                    {co.code}{mapped ? " \u2713" : ""}
                  </button>
                );
              })}
            </div>
          </div>
        )}
      </section>
    </>
  );
}