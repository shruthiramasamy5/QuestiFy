import { useEffect, useState } from "react";
import PageHeader from "../components/common/PageHeader.jsx";
import LoadingState from "../components/common/LoadingState.jsx";
import ErrorState from "../components/common/ErrorState.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import PaperBookletView from "../components/paper/PaperBookletView.jsx";
import papersService from "../services/papersService.js";
import approvalService from "../services/approvalService.js";
import { badgeClassFor, titleCase } from "../utils/options.js";

export default function PapersPage() {
  const [papers, setPapers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [subjectFilter, setSubjectFilter] = useState("");
  const [selected, setSelected] = useState(null);
  const [showBookletModal, setShowBookletModal] = useState(false);
  const [evalForm, setEvalForm] = useState(null);
  const [saveMsg, setSaveMsg] = useState(null);
  const [submitMsg, setSubmitMsg] = useState(null);
  const [submittingApproval, setSubmittingApproval] = useState(false);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const params = {};
      if (subjectFilter) params.subjectCode = subjectFilter;
      const data = await papersService.list(params);
      setPapers(Array.isArray(data) ? data : data?.content || []);
    } catch (err) {
      setError(err?.response?.data?.message || "Could not load papers.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [subjectFilter]);

  async function openPaper(p) {
    try {
      const full = await papersService.getById(p.id);
      setSelected(full || p);
    } catch {
      setSelected(p);
    }
    setEvalForm({ totalMarks: p.totalMarks || 50, passingMarks: 20, negativeMarking: false, partialMarking: true });
    setSaveMsg(null);
    setSubmitMsg(null);
    setShowBookletModal(true);
  }

  async function saveEvaluation(e) {
    e.preventDefault();
    setSaveMsg(null);
    try {
      await papersService.updateEvaluation(selected.id, { ...evalForm, rubrics: [] });
      setSaveMsg("Evaluation scheme saved successfully.");
    } catch (err) {
      setSaveMsg(err?.response?.data?.message || "Could not save the evaluation scheme.");
    }
  }

  async function sendForApproval() {
    if (!selected) return;
    setSubmittingApproval(true);
    setSubmitMsg(null);
    try {
      await approvalService.create({
        paperId: selected.id,
        paperTitle: selected.title,
        institutionId: selected.institutionId || 1,
      });
      setSubmitMsg("Paper successfully submitted to the approval queue.");
      await load();
    } catch (err) {
      setSubmitMsg(err?.response?.data?.message || "Could not open an approval workflow for this paper.");
    } finally {
      setSubmittingApproval(false);
    }
  }

  return (
    <>
      <PageHeader title="Papers" subtitle="Generated question papers, booklet inspector, answer keys and exports" />

      <section className="panel">
        <div className="toolbar">
          <div className="toolbar-filters">
            <input
              placeholder="Filter by subject code"
              value={subjectFilter}
              onChange={(e) => setSubjectFilter(e.target.value)}
            />
          </div>
        </div>
        {loading ? (
          <LoadingState message="Loading papers…" />
        ) : error ? (
          <ErrorState message={error} onRetry={load} />
        ) : papers.length === 0 ? (
          <EmptyState glyph={"□"} title="No papers yet" message="Generate a draft and turn it into a paper first." />
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Subject</th>
                <th>Marks</th>
                <th>Status</th>
                <th style={{ textAlign: "right" }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {papers.map((p) => (
                <tr key={p.id}>
                  <td><strong>{p.title}</strong></td>
                  <td>{p.subjectCode} - {p.subjectName}</td>
                  <td>{p.totalMarks} Marks</td>
                  <td><span className={badgeClassFor(p.status)}>{titleCase(p.status)}</span></td>
                  <td style={{ textAlign: "right" }}>
                    <button type="button" className="ws-btn primary" style={{ fontSize: "12px", padding: "4px 10px" }} onClick={() => openPaper(p)}>
                      👁 View Paper & Answer Key
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      {showBookletModal && selected && (
        <PaperBookletView
          paper={selected}
          onClose={() => setShowBookletModal(false)}
          onSubmitApproval={selected.status !== "APPROVED" ? sendForApproval : null}
          submittingApproval={submittingApproval}
          approvalStatus={submitMsg}
        />
      )}
    </>
  );
}