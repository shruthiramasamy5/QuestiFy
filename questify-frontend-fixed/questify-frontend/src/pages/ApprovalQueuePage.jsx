import { useEffect, useState } from "react";
import PageHeader from "../components/common/PageHeader.jsx";
import LoadingState from "../components/common/LoadingState.jsx";
import ErrorState from "../components/common/ErrorState.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import PaperBookletView from "../components/paper/PaperBookletView.jsx";
import approvalService from "../services/approvalService.js";
import papersService from "../services/papersService.js";
import { titleCase } from "../utils/options.js";

export default function ApprovalQueuePage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [comments, setComments] = useState({});
  const [busyId, setBusyId] = useState(null);
  const [inspectPaper, setInspectPaper] = useState(null);
  const [inspectLoading, setInspectLoading] = useState(false);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const data = await approvalService.list("PENDING");
      setItems(Array.isArray(data) ? data : data?.content || []);
    } catch (err) {
      setError(err?.response?.data?.message || "Could not load the approval queue.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function viewPaperDetails(paperId, defaultTitle) {
    setInspectLoading(true);
    try {
      const paper = await papersService.getById(paperId);
      setInspectPaper(paper || { id: paperId, title: defaultTitle, questions: [] });
    } catch {
      setInspectPaper({ id: paperId, title: defaultTitle, questions: [] });
    } finally {
      setInspectLoading(false);
    }
  }

  async function decide(id, action) {
    setBusyId(id);
    try {
      if (action === "approve") await approvalService.approve(id, comments[id]);
      else await approvalService.reject(id, comments[id]);
      await load();
    } catch (err) {
      setError(err?.response?.data?.message || "Could not record the decision.");
    } finally {
      setBusyId(null);
    }
  }

  return (
    <>
      <PageHeader title="Approval queue" subtitle="Paper sets awaiting your review, inspection, and sign-off" />
      <section className="panel">
        {loading ? (
          <LoadingState message="Loading the queue…" />
        ) : error ? (
          <ErrorState message={error} onRetry={load} />
        ) : items.length === 0 ? (
          <EmptyState glyph={"✓"} title="Queue is clear" message="Nothing is waiting on your review right now." />
        ) : (
          items.map((req) => (
            <div key={req.id} className="approval-row" style={{ padding: 18, borderBottom: "1px solid var(--border)" }}>
              <div className="approval-set" style={{ display: "flex", justifyContent: "space-between", gap: 12, flexWrap: "wrap" }}>
                <div>
                  <strong>{req.paperTitle || `Paper #${req.paperId}`}</strong>
                  <div className="approval-meta page-sub">
                    Stage: {titleCase(req.currentStage)} · Submitted by {req.createdBy}
                  </div>
                </div>
                <div style={{ display: "flex", gap: "8px", alignItems: "center" }}>
                  <button
                    type="button"
                    className="ws-btn secondary"
                    style={{ fontSize: "12px", padding: "4px 10px" }}
                    onClick={() => viewPaperDetails(req.paperId, req.paperTitle)}
                    disabled={inspectLoading}
                  >
                    👁 Inspect Paper & Answer Key
                  </button>
                  <span className="badge badge-warn">{titleCase(req.status)}</span>
                </div>
              </div>
              <div className="field wide" style={{ marginTop: 10 }}>
                <textarea
                  placeholder="Feedback / review comments"
                  value={comments[req.id] || ""}
                  onChange={(e) => setComments((c) => ({ ...c, [req.id]: e.target.value }))}
                />
              </div>
              <div className="row-actions" style={{ marginTop: 10, display: "flex", gap: "8px" }}>
                <button type="button" className="ws-btn primary" disabled={busyId === req.id} onClick={() => decide(req.id, "approve")}>
                  ✓ Approve Paper
                </button>
                <button type="button" className="ws-btn ghost" disabled={busyId === req.id} onClick={() => decide(req.id, "reject")}>
                  ✕ Reject with Feedback
                </button>
              </div>
            </div>
          ))
        )}
      </section>

      {inspectPaper && (
        <PaperBookletView
          paper={inspectPaper}
          onClose={() => setInspectPaper(null)}
        />
      )}
    </>
  );
}