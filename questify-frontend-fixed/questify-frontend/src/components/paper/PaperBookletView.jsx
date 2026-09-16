import { useState } from "react";
import { badgeClassFor, titleCase } from "../../utils/options.js";

export default function PaperBookletView({
  paper,
  onClose,
  onSubmitApproval,
  submittingApproval,
  approvalStatus,
}) {
  const [activeTab, setActiveTab] = useState("paper"); // "paper" | "answerKey"

  if (!paper) return null;

  const questions = paper.questions || [];
  const partA = questions.filter((q) => !q.section || q.section === "Part A" || (q.marks && q.marks <= 2));
  const partB = questions.filter((q) => q.section === "Part B" || (q.marks && q.marks > 2));

  function handlePrint() {
    window.print();
  }

  function handleDownloadText() {
    let content = `========================================================================\n`;
    content += `                    DEMO INSTITUTE OF TECHNOLOGY\n`;
    content += `         Department of Computer Science & Engineering\n`;
    content += `             Continuous Internal Assessment (CIA)\n`;
    content += `========================================================================\n`;
    content += `Course: ${paper.subjectCode || "CS301"} - ${paper.subjectName || paper.title || "Database Management Systems"}\n`;
    content += `Duration: ${paper.durationMinutes || 90} Minutes          Max Marks: ${paper.totalMarks || 50}\n`;
    content += `------------------------------------------------------------------------\n`;
    content += `INSTRUCTIONS:\n`;
    content += `1. Answer all questions in Part A. Answer any two questions in Part B.\n`;
    content += `2. Illustrate your answers with neat diagrams and examples wherever necessary.\n\n`;

    content += `PART A (Answer all questions - Short Answer)\n`;
    content += `------------------------------------------------------------------------\n`;
    (partA.length > 0 ? partA : questions.slice(0, 5)).forEach((q, i) => {
      content += `Q${i + 1}. ${q.questionText || q.text}   [${q.marks || 2} Marks] [Bloom: ${q.bloomLevel || "K1"}] [CO: ${q.courseOutcome || "CO1"}]\n\n`;
    });

    content += `PART B (Analytical & Design Questions)\n`;
    content += `------------------------------------------------------------------------\n`;
    (partB.length > 0 ? partB : questions.slice(5)).forEach((q, i) => {
      content += `Q${(partA.length || 5) + i + 1}. ${q.questionText || q.text}   [${q.marks || 10} Marks] [Bloom: ${q.bloomLevel || "K3"}] [CO: ${q.courseOutcome || "CO2"}]\n\n`;
    });

    content += `========================================================================\n`;
    content += `                             END OF PAPER\n`;
    content += `========================================================================\n`;

    const blob = new Blob([content], { type: "text/plain;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `${paper.subjectCode || "Paper"}_${paper.examType || "Exam"}.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

  function handleDownloadAnswerKey() {
    let content = `========================================================================\n`;
    content += `                 MODEL ANSWER KEY & SCORING RUBRICS\n`;
    content += `Course: ${paper.subjectCode || "CS301"} - ${paper.subjectName || paper.title}\n`;
    content += `Total Marks: ${paper.totalMarks || 50}\n`;
    content += `========================================================================\n\n`;

    questions.forEach((q, i) => {
      content += `Q${i + 1} (${q.marks || 5} Marks) [Bloom: ${q.bloomLevel || "K2"}] [CO: ${q.courseOutcome || "CO1"}]\n`;
      content += `Question: ${q.questionText || q.text}\n`;
      content += `Model Solution Outline:\n`;
      content += `- Definition / Concept Statement: 40% marks\n`;
      content += `- Technical illustration / formula / syntax: 40% marks\n`;
      content += `- Example / Edge Case justification: 20% marks\n`;
      content += `------------------------------------------------------------------------\n\n`;
    });

    const blob = new Blob([content], { type: "text/plain;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `${paper.subjectCode || "Paper"}_Answer_Key.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" style={{ maxWidth: "860px" }} onClick={(e) => e.stopPropagation()}>
        <div className="modal-head">
          <div style={{ display: "flex", gap: "10px", alignItems: "center" }}>
            <button
              type="button"
              className={`ws-btn ${activeTab === "paper" ? "primary" : "ghost"}`}
              onClick={() => setActiveTab("paper")}
            >
              📄 Question Paper Booklet
            </button>
            <button
              type="button"
              className={`ws-btn ${activeTab === "answerKey" ? "primary" : "ghost"}`}
              onClick={() => setActiveTab("answerKey")}
            >
              🔑 Model Answer Key & Rubrics
            </button>
          </div>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <div className="modal-body" style={{ padding: "10px 20px" }}>
          {/* Action Bar */}
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "12px", flexWrap: "wrap", gap: "8px" }}>
            <div style={{ display: "flex", gap: "6px" }}>
              <button type="button" className="ws-btn ghost" onClick={handlePrint}>
                🖨 Print / PDF
              </button>
              <button type="button" className="ws-btn ghost" onClick={handleDownloadText}>
                ⬇ Export Question Paper
              </button>
              <button type="button" className="ws-btn ghost" onClick={handleDownloadAnswerKey}>
                🔑 Export Answer Key
              </button>
            </div>
            {onSubmitApproval && (
              <button
                type="button"
                className="ws-btn primary"
                onClick={onSubmitApproval}
                disabled={submittingApproval || paper.status === "APPROVED"}
              >
                {submittingApproval ? "Submitting…" : paper.status === "APPROVED" ? "✓ Approved" : "Submit to Approval Queue &rarr;"}
              </button>
            )}
          </div>

          {approvalStatus && (
            <div style={{ padding: "8px 12px", borderRadius: "6px", background: "rgba(60,52,137,.1)", marginBottom: "12px", fontSize: "12.5px" }}>
              {approvalStatus}
            </div>
          )}

          {activeTab === "paper" ? (
            /* Booklet View */
            <div className="paper-sheet">
              <div className="paper-head">
                <div className="paper-college">Demo Institute of Technology</div>
                <div className="paper-sub">Department of Computer Science & Engineering</div>
                <div className="paper-title">{paper.title || `${paper.subjectCode} — Examination`}</div>
                <div className="paper-meta">
                  <span>Subject: {paper.subjectCode} - {paper.subjectName || paper.title}</span>
                  <span>Duration: {paper.durationMinutes || 90} Mins</span>
                  <span>Max Marks: {paper.totalMarks || 50}</span>
                </div>
              </div>

              <div className="paper-instructions">
                <strong>General Instructions:</strong>
                <ol style={{ marginLeft: "18px", marginTop: "4px" }}>
                  <li>Answer all questions. Show detailed working and neat diagrams wherever applicable.</li>
                  <li>Bloom's Taxonomy levels (K1-K6) and mapped Course Outcomes (CO) are indicated against each question.</li>
                </ol>
              </div>

              {questions.length === 0 ? (
                <div style={{ textAlign: "center", padding: "30px", color: "var(--muted)" }}>
                  No questions attached to this paper.
                </div>
              ) : (
                <div>
                  <div style={{ fontWeight: 700, fontSize: "13px", letterSpacing: "0.04em", textTransform: "uppercase", margin: "14px 0 8px", color: "var(--ink)" }}>
                    Questions
                  </div>
                  {questions.map((q, idx) => (
                    <div key={q.id || idx} className="paper-q">
                      <div className="paper-num">{idx + 1}.</div>
                      <div>
                        <div>{q.questionText || q.text}</div>
                        <div style={{ display: "flex", gap: "6px", marginTop: "4px" }}>
                          <span className="tag tag-k">{q.bloomLevel || "K2"}</span>
                          <span className="tag tag-co">{q.courseOutcome || "CO1"}</span>
                          <span style={{ fontSize: "11px", color: "var(--muted)" }}>{q.unit || "Unit 1"}</span>
                        </div>
                      </div>
                      <div className="paper-marks">{q.marks || 5} M</div>
                    </div>
                  ))}
                </div>
              )}

              <div style={{ textAlign: "center", marginTop: "30px", fontSize: "11px", fontFamily: "var(--mono)", color: "var(--muted)" }}>
                *** END OF QUESTION PAPER ***
              </div>
            </div>
          ) : (
            /* Answer Key View */
            <div className="paper-sheet">
              <div className="paper-head">
                <div className="paper-college">Model Answer Key & Rubric Scheme</div>
                <div className="paper-sub">{paper.subjectCode} — {paper.subjectName || paper.title}</div>
              </div>

              {questions.map((q, idx) => (
                <div key={q.id || idx} style={{ padding: "14px 0", borderBottom: "1px dashed var(--border)" }}>
                  <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "6px" }}>
                    <strong>Q{idx + 1}. {q.questionText || q.text}</strong>
                    <span style={{ fontFamily: "var(--mono)", fontWeight: 600 }}>{q.marks || 5} Marks</span>
                  </div>
                  <div style={{ fontSize: "12.5px", color: "var(--muted)", background: "rgba(36,28,22,0.03)", padding: "10px", borderRadius: "6px" }}>
                    <div><strong>Key Points / Solution Outline:</strong></div>
                    <ul style={{ marginLeft: "18px", marginTop: "4px" }}>
                      <li>Core definition, governing axioms, or architectural overview ({Math.max(1, Math.round((q.marks || 5) * 0.4))} M)</li>
                      <li>Detailed technical derivation, relational schema, or syntax implementation ({Math.max(1, Math.round((q.marks || 5) * 0.4))} M)</li>
                      <li>Example application or edge-case handling ({Math.max(1, Math.round((q.marks || 5) * 0.2))} M)</li>
                    </ul>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
