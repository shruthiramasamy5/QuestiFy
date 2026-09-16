import { useState } from "react";
import { useNavigate } from "react-router-dom";
import PageHeader from "../components/common/PageHeader.jsx";
import LoadingState from "../components/common/LoadingState.jsx";
import PaperBookletView from "../components/paper/PaperBookletView.jsx";
import paperGenerationService from "../services/paperGenerationService.js";
import papersService from "../services/papersService.js";
import { EXAM_TYPE_OPTIONS } from "../utils/options.js";

function parseDistribution(text) {
  const out = {};
  if (!text) return out;
  text
    .split(",")
    .map((p) => p.trim())
    .filter(Boolean)
    .forEach((pair) => {
      const [key, value] = pair.split(":").map((s) => s.trim());
      if (key && value) out[key] = Number(value);
    });
  return out;
}

const INITIAL = {
  subjectCode: "CS301",
  subjectName: "Database Management Systems",
  examType: "INTERNAL",
  questionCount: 8,
  totalMarks: 50,
  durationMinutes: 90,
  coDistribution: "CO1:2, CO2:2, CO3:2, CO4:2",
  bloomDistribution: "K1:2, K2:3, K3:2, K4:1",
  difficultyMix: "EASY:3, MEDIUM:4, HARD:1",
  repetitionWindowDays: 365,
};

export default function GeneratePaperPage() {
  const [form, setForm] = useState(INITIAL);
  const [draft, setDraft] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [creating, setCreating] = useState(false);
  const [title, setTitle] = useState("");
  const [showFullBookletModal, setShowFullBookletModal] = useState(false);
  const [createdPaperId, setCreatedPaperId] = useState(null);
  const navigate = useNavigate();

  async function submit(e) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setDraft(null);
    try {
      const payload = {
        subjectCode: form.subjectCode,
        subjectName: form.subjectName,
        examType: form.examType,
        questionCount: Number(form.questionCount),
        totalMarks: Number(form.totalMarks),
        durationMinutes: Number(form.durationMinutes),
        coDistribution: parseDistribution(form.coDistribution),
        bloomDistribution: parseDistribution(form.bloomDistribution),
        difficultyMix: parseDistribution(form.difficultyMix),
        repetitionWindowDays: Number(form.repetitionWindowDays),
      };
      const result = await paperGenerationService.generate(payload);
      setDraft(result);
      setTitle(`${form.subjectName || form.subjectCode} — ${form.examType} Assessment`);
    } catch (err) {
      setError(err?.response?.data?.message || "Could not generate a draft with these settings.");
    } finally {
      setLoading(false);
    }
  }

  async function createPaper() {
    setCreating(true);
    setError(null);
    try {
      const res = await papersService.createFromDraft({ draftId: draft.id, title });
      setCreatedPaperId(res?.id || 1);
      navigate("/papers");
    } catch (err) {
      setError(err?.response?.data?.message || "Could not create a paper from this draft.");
    } finally {
      setCreating(false);
    }
  }

  return (
    <>
      <PageHeader
        title="Generate paper"
        subtitle="Automated exam blueprint configuration, blueprint balance, and question paper generation"
      />

      <section className="panel">
        <div className="panel-head">
          <div>
            <div className="panel-title">Blueprint Configuration</div>
            <div className="panel-sub">Specify course syllabus, Bloom's Taxonomy mix, and difficulty weights</div>
          </div>
        </div>
        <form onSubmit={submit}>
          <div className="form-grid">
            <div className="field">
              <label>Subject code</label>
              <input required value={form.subjectCode} onChange={(e) => setForm((f) => ({ ...f, subjectCode: e.target.value }))} />
            </div>
            <div className="field">
              <label>Subject name</label>
              <input required value={form.subjectName} onChange={(e) => setForm((f) => ({ ...f, subjectName: e.target.value }))} />
            </div>
            <div className="field">
              <label>Exam type</label>
              <select value={form.examType} onChange={(e) => setForm((f) => ({ ...f, examType: e.target.value }))}>
                {EXAM_TYPE_OPTIONS.map((t) => (
                  <option key={t} value={t}>{t}</option>
                ))}
              </select>
            </div>
            <div className="field">
              <label>Question count</label>
              <input type="number" min="1" value={form.questionCount} onChange={(e) => setForm((f) => ({ ...f, questionCount: e.target.value }))} />
            </div>
            <div className="field">
              <label>Total marks</label>
              <input type="number" min="1" value={form.totalMarks} onChange={(e) => setForm((f) => ({ ...f, totalMarks: e.target.value }))} />
            </div>
            <div className="field">
              <label>Duration (minutes)</label>
              <input type="number" min="1" value={form.durationMinutes} onChange={(e) => setForm((f) => ({ ...f, durationMinutes: e.target.value }))} />
            </div>
            <div className="field">
              <label>Repetition window (days)</label>
              <input type="number" min="0" value={form.repetitionWindowDays} onChange={(e) => setForm((f) => ({ ...f, repetitionWindowDays: e.target.value }))} />
            </div>
            <div className="field">
              <label>CO distribution</label>
              <input placeholder="CO1:2, CO2:3" value={form.coDistribution} onChange={(e) => setForm((f) => ({ ...f, coDistribution: e.target.value }))} />
            </div>
            <div className="field">
              <label>Bloom distribution</label>
              <input placeholder="K1:2, K2:3" value={form.bloomDistribution} onChange={(e) => setForm((f) => ({ ...f, bloomDistribution: e.target.value }))} />
            </div>
            <div className="field">
              <label>Difficulty mix</label>
              <input placeholder="EASY:4, MEDIUM:4, HARD:2" value={form.difficultyMix} onChange={(e) => setForm((f) => ({ ...f, difficultyMix: e.target.value }))} />
            </div>
          </div>
          {error && <p className="inline-error">{error}</p>}
          <div className="form-actions">
            <button type="submit" className="ws-btn primary" disabled={loading}>
              {loading ? "Generating Paper Draft…" : "✦ Generate Question Paper"}
            </button>
          </div>
        </form>
      </section>

      {loading && (
        <section className="panel">
          <LoadingState message="Assembling an optimal question paper draft from the question bank…" />
        </section>
      )}

      {draft && (
        <section className="panel" style={{ borderLeft: "3px solid var(--teal, #0F6E56)" }}>
          <div className="panel-head">
            <div>
              <div className="panel-title">Generated Paper Draft — {draft.subjectName} ({draft.subjectCode})</div>
              <div className="panel-sub">{draft.questionCount} questions · {draft.totalMarks} marks · {draft.durationMinutes} min</div>
            </div>
            <div style={{ display: "flex", gap: "8px" }}>
              <button type="button" className="ws-btn secondary" onClick={() => setShowFullBookletModal(true)}>
                👁 View Full Booklet & Answer Key
              </button>
              <span className="badge badge-warn">{draft.status}</span>
            </div>
          </div>

          <div style={{ padding: "0 18px 18px" }}>
            <div className="paper-sheet" style={{ margin: "16px 0", boxShadow: "none", border: "1px solid var(--border)" }}>
              <div className="paper-head">
                <div className="paper-college">Demo Institute of Technology</div>
                <div className="paper-sub">Department of Computer Science & Engineering</div>
                <div className="paper-title">{title}</div>
                <div className="paper-meta">
                  <span>Subject: {draft.subjectCode} - {draft.subjectName}</span>
                  <span>Duration: {draft.durationMinutes} Mins</span>
                  <span>Max Marks: {draft.totalMarks}</span>
                </div>
              </div>

              {(draft.questions || []).map((q, idx) => (
                <div key={q.id || idx} className="paper-q">
                  <div className="paper-num">{idx + 1}.</div>
                  <div>
                    <div>{q.text || q.questionText}</div>
                    <div style={{ display: "flex", gap: "6px", marginTop: "4px" }}>
                      <span className="tag tag-k">{q.bloomLevel || "K2"}</span>
                      <span className="tag tag-co">{q.courseOutcome || "CO1"}</span>
                      <span className="badge badge-good">{q.difficulty || "MEDIUM"}</span>
                    </div>
                  </div>
                  <div className="paper-marks">{q.marks || 5} M</div>
                </div>
              ))}
            </div>

            <div className="form-grid single">
              <div className="field">
                <label>Paper title</label>
                <input value={title} onChange={(e) => setTitle(e.target.value)} />
              </div>
            </div>

            <div className="form-actions" style={{ padding: "12px 0 0" }}>
              <button type="button" className="ws-btn primary" onClick={createPaper} disabled={creating}>
                {creating ? "Saving Paper…" : "✓ Save Paper to Repository"}
              </button>
            </div>
          </div>
        </section>
      )}

      {showFullBookletModal && draft && (
        <PaperBookletView
          paper={{
            ...draft,
            title: title || `${draft.subjectCode} — Examination`,
            questions: draft.questions?.map((q) => ({
              ...q,
              questionText: q.text || q.questionText,
            })),
          }}
          onClose={() => setShowFullBookletModal(false)}
          onSubmitApproval={createPaper}
          submittingApproval={creating}
        />
      )}
    </>
  );
}