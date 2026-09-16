import { useEffect, useState } from "react";
import PageHeader from "../components/common/PageHeader.jsx";
import LoadingState from "../components/common/LoadingState.jsx";
import ErrorState from "../components/common/ErrorState.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import PaperBookletView from "../components/paper/PaperBookletView.jsx";
import approvalService from "../services/approvalService.js";
import papersService from "../services/papersService.js";
import { titleCase } from "../utils/options.js";

export default function ApprovedPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedPaper, setSelectedPaper] = useState(null);
  const [inspectLoading, setInspectLoading] = useState(false);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const data = await approvalService.list("APPROVED");
      setItems(Array.isArray(data) ? data : data?.content || []);
    } catch (err) {
      setError(err?.response?.data?.message || "Could not load approved papers.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function viewPaper(paperId, defaultTitle) {
    setInspectLoading(true);
    try {
      const paper = await papersService.getById(paperId);
      setSelectedPaper(paper || { id: paperId, title: defaultTitle, status: "APPROVED", questions: [] });
    } catch {
      setSelectedPaper({ id: paperId, title: defaultTitle, status: "APPROVED", questions: [] });
    } finally {
      setInspectLoading(false);
    }
  }

  return (
    <>
      <PageHeader title="Approved papers" subtitle="Signed-off question papers ready for download, print, and examination archive" />
      <section className="panel">
        {loading ? (
          <LoadingState message="Loading approved papers…" />
        ) : error ? (
          <ErrorState message={error} onRetry={load} />
        ) : items.length === 0 ? (
          <EmptyState glyph={"✓"} title="Nothing approved yet" message="Approved question papers will appear here for download and archiving." />
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Paper</th>
                <th>Stage</th>
                <th>Submitted by</th>
                <th>Approved Date</th>
                <th style={{ textAlign: "right" }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {items.map((req) => (
                <tr key={req.id}>
                  <td><strong>{req.paperTitle || `Paper #${req.paperId}`}</strong></td>
                  <td><span className="badge badge-good">{titleCase(req.currentStage)}</span></td>
                  <td>{req.createdBy}</td>
                  <td>{req.updatedAt ? new Date(req.updatedAt).toLocaleString() : "—"}</td>
                  <td style={{ textAlign: "right" }}>
                    <button
                      type="button"
                      className="ws-btn primary"
                      style={{ fontSize: "12px", padding: "4px 10px" }}
                      onClick={() => viewPaper(req.paperId, req.paperTitle)}
                    >
                      👁 View Paper & Export
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      {selectedPaper && (
        <PaperBookletView
          paper={selectedPaper}
          onClose={() => setSelectedPaper(null)}
        />
      )}
    </>
  );
}