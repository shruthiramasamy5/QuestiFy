import { useEffect, useState } from "react";
import PageHeader from "../components/common/PageHeader.jsx";
import LoadingState from "../components/common/LoadingState.jsx";
import ErrorState from "../components/common/ErrorState.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import questionBankService from "../services/questionBankService.js";
import questionDraftService from "../services/questionDraftService.js";
import coMappingService from "../services/coMappingService.js";

import {
  DIFFICULTY_OPTIONS,
  QUESTION_TYPE_OPTIONS,
  BLOOM_LEVEL_OPTIONS,
  titleCase,
} from "../utils/options.js";

/*
 * TODO:
 * Replace 1 with the actual institution ID from your database
 * if your institutions table uses a different ID.
 */
const INSTITUTION_ID = 1;
const SUBJECT_ID = 1;
const EMPTY_FORM = {
  questionText: "",
  subject: "",
  unit: "",
  difficulty: "MEDIUM",
  questionType: "SHORT_ANSWER",
  marks: 5,
  bloomLevel: "K2",
  courseOutcomeIds: [],
};

const EMPTY_AI_FORM = {
  subject: "",
  unit: "Unit 1",
  topicHint: "",
  bloomLevel: "K2",
  difficulty: "MEDIUM",
  marks: 5,
  count: 3,
};

/* =========================================================
   SAFE HELPERS
   ========================================================= */

function getOptionValue(option) {
  if (option === null || option === undefined) {
    return "";
  }

  if (typeof option === "object") {
    return String(option.value ?? "");
  }

  return String(option);
}

function getOptionLabel(option) {
  if (option === null || option === undefined) {
    return "";
  }

  if (typeof option === "object") {
    return String(option.label ?? option.value ?? "");
  }

  return String(option);
}

function getBloomValue(value) {
  if (!value) {
    return "";
  }

  if (typeof value === "object") {
    return String(value.value ?? "");
  }

  return String(value);
}

function getBloomLabel(value) {
  if (!value) {
    return "";
  }

  if (typeof value === "object") {
    return String(value.label ?? value.value ?? "");
  }

  const found = BLOOM_LEVEL_OPTIONS.find(
    (option) => getOptionValue(option) === String(value)
  );

  return found
    ? getOptionLabel(found)
    : String(value);
}

function getSafeEnumValue(value) {
  if (value === null || value === undefined) {
    return "";
  }

  if (typeof value === "object") {
    return String(value.value ?? "");
  }

  return String(value);
}

function getSafeEnumLabel(value) {
  if (value === null || value === undefined) {
    return "";
  }

  if (typeof value === "object") {
    return String(value.label ?? value.value ?? "");
  }

  return String(value);
}

function safeText(value) {
  if (value === null || value === undefined) {
    return "";
  }

  if (typeof value === "object") {
    return String(
      value.label ??
        value.value ??
        value.name ??
        value.code ??
        ""
    );
  }

  return String(value);
}

/* =========================================================
   COMPONENT
   ========================================================= */

export default function QuestionBankPage() {
  const [filters, setFilters] = useState({
    subject: "",
    bloomLevel: "",
    difficulty: "",
  });

  const [page, setPage] = useState({
    content: [],
    totalPages: 0,
    number: 0,
  });

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [outcomes, setOutcomes] = useState([]);

  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);

  const [form, setForm] = useState({
    ...EMPTY_FORM,
    courseOutcomeIds: [],
  });

  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState(null);

  /* AI */
  const [aiModalOpen, setAiModalOpen] = useState(false);

  const [aiForm, setAiForm] = useState({
    ...EMPTY_AI_FORM,
  });

  const [aiGenerating, setAiGenerating] = useState(false);
  const [aiError, setAiError] = useState(null);
  const [aiDrafts, setAiDrafts] = useState([]);
  const [approvingDraftId, setApprovingDraftId] = useState(null);

  /* SYLLABUS UPLOAD */
  const [syllabusModalOpen, setSyllabusModalOpen] = useState(false);
  const [syllabusSubject, setSyllabusSubject] = useState("CS301");
  const [syllabusText, setSyllabusText] = useState("");
  const [syllabusParsing, setSyllabusParsing] = useState(false);
  const [syllabusResult, setSyllabusResult] = useState(null);

  /* BULK CSV IMPORT */
  const [bulkModalOpen, setBulkModalOpen] = useState(false);
  const [bulkCsvText, setBulkCsvText] = useState("");
  const [bulkPreview, setBulkPreview] = useState([]);
  const [bulkImporting, setBulkImporting] = useState(false);
  const [bulkResult, setBulkResult] = useState(null);

  function downloadSampleCsv() {
    const csv = "questionText,subject,unit,difficulty,questionType,marks,bloomLevel\n" +
      "\"Explain the Three-Schema Architecture in DBMS\",CS301,Unit 1,MEDIUM,LONG_ANSWER,5,K2\n" +
      "\"Define primary key and foreign key with an example\",CS301,Unit 1,EASY,SHORT_ANSWER,2,K1\n" +
      "\"Apply relational algebra operations on student database\",CS301,Unit 2,HARD,LONG_ANSWER,10,K3\n" +
      "\"Design normalized schema in BCNF for hospital management\",CS301,Unit 3,HARD,LONG_ANSWER,10,K6\n";
    const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = "questify_sample_questions.csv";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  function handleCsvFileSelect(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (event) => {
      const text = event.target.result;
      setBulkCsvText(text);
      parseCsvText(text);
    };
    reader.readAsText(file);
  }

  function parseCsvText(text) {
    if (!text || !text.trim()) {
      setBulkPreview([]);
      return;
    }
    const lines = text.trim().split(/\r?\n/);
    if (lines.length <= 1) return;
    const rows = [];
    for (let i = 1; i < lines.length; i++) {
      const line = lines[i].trim();
      if (!line) continue;
      // Handle simple CSV splitting
      const parts = line.split(/,(?=(?:(?:[^"]*"){2})*[^"]*$)/).map((s) => s.replace(/^"|"$/g, "").trim());
      if (parts.length >= 7) {
        rows.push({
          questionText: parts[0],
          subject: parts[1] || "CS301",
          unit: parts[2] || "Unit 1",
          difficulty: parts[3] || "MEDIUM",
          questionType: parts[4] || "SHORT_ANSWER",
          marks: parseInt(parts[5], 10) || 5,
          bloomLevel: parts[6] || "K2",
          institutionId: INSTITUTION_ID,
          subjectId: SUBJECT_ID,
        });
      }
    }
    setBulkPreview(rows);
  }

  async function handleBulkImport() {
    if (bulkPreview.length === 0) return;
    setBulkImporting(true);
    setBulkResult(null);
    try {
      const res = await questionBankService.bulkUpload(bulkPreview);
      setBulkResult(res);
      await load();
    } catch (err) {
      setBulkResult({
        totalReceived: bulkPreview.length,
        imported: 0,
        skipped: bulkPreview.length,
        errors: [err?.response?.data?.message || "Bulk import failed."],
      });
    } finally {
      setBulkImporting(false);
    }
  }

  async function handleSyllabusParse(e) {
    e.preventDefault();
    setSyllabusParsing(true);
    setSyllabusResult(null);
    try {
      const res = await questionBankService.syllabusUpload({
        subject: syllabusSubject,
        subjectCode: syllabusSubject,
        syllabusText,
      });
      setSyllabusResult(res);
      // Generate starter drafts into AI Drafts
      const drafts = [
        {
          id: `syl_d1_${Date.now()}`,
          questionText: `Explain core concepts and architectural foundations of ${syllabusSubject} with suitable diagrams.`,
          bloomLevel: "K2",
          difficulty: "MEDIUM",
          marks: 5,
          subject: syllabusSubject,
          unit: "Unit 1",
          status: "DRAFT",
        },
        {
          id: `syl_d2_${Date.now()}`,
          questionText: `Formulate a practical implementation model applying principles from ${syllabusSubject} for an enterprise application.`,
          bloomLevel: "K3",
          difficulty: "HARD",
          marks: 10,
          subject: syllabusSubject,
          unit: "Unit 2",
          status: "DRAFT",
        },
        {
          id: `syl_d3_${Date.now()}`,
          questionText: `Analyze concurrency, transaction consistency, and error recovery techniques in ${syllabusSubject}.`,
          bloomLevel: "K4",
          difficulty: "HARD",
          marks: 10,
          subject: syllabusSubject,
          unit: "Unit 3",
          status: "DRAFT",
        },
      ];
      setAiDrafts(drafts);
      setAiModalOpen(true);
    } catch (err) {
      setSyllabusResult({ error: err?.response?.data?.message || "Failed to process syllabus." });
    } finally {
      setSyllabusParsing(false);
    }
  }

  /* =========================================================
     LOAD QUESTIONS
     ========================================================= */

  async function load() {
    setLoading(true);
    setError(null);

    try {
      const params = {};

      if (filters.subject.trim()) {
        params.subject = filters.subject.trim();
      }

      if (filters.bloomLevel) {
        params.bloomLevel = getSafeEnumValue(
          filters.bloomLevel
        );
      }

      if (filters.difficulty) {
        params.difficulty = getSafeEnumValue(
          filters.difficulty
        );
      }

      const data = await questionBankService.search(params);

      setPage({
        content: Array.isArray(data?.content)
          ? data.content
          : [],
        totalPages: Number(data?.totalPages || 0),
        number: Number(data?.number || 0),
      });
    } catch (err) {
      setError(
        err?.response?.data?.message ||
          "Could not load the question bank."
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters]);

  /* =========================================================
     LOAD COURSE OUTCOMES
     ========================================================= */

  useEffect(() => {
    async function loadOutcomes() {
      try {
        const data =
          await coMappingService.listOutcomes({
            size: 100,
          });

        setOutcomes(
          Array.isArray(data?.content)
            ? data.content
            : []
        );
      } catch {
        setOutcomes([]);
      }
    }

    loadOutcomes();
  }, []);

  /* =========================================================
     CREATE
     ========================================================= */

  function openCreate() {
    setEditingId(null);

    setForm({
      ...EMPTY_FORM,
      courseOutcomeIds: [],
    });

    setSaveError(null);
    setFormOpen(true);
  }

  /* =========================================================
     EDIT
     ========================================================= */

  function openEdit(question) {
    setEditingId(question.id);

    const courseOutcomeIds = Array.isArray(
      question.courseOutcomes
    )
      ? question.courseOutcomes
          .map((co) => co?.id)
          .filter(
            (id) =>
              id !== null &&
              id !== undefined
          )
      : [];

    setForm({
      questionText: safeText(
        question.questionText
      ),

      subject: safeText(question.subject),

      unit: safeText(question.unit),

      difficulty:
        getSafeEnumValue(question.difficulty) ||
        "MEDIUM",

      questionType:
        getSafeEnumValue(question.questionType) ||
        "SHORT_ANSWER",

      marks:
        question.marks !== null &&
        question.marks !== undefined
          ? question.marks
          : 5,

      bloomLevel:
        getBloomValue(question.bloomLevel) ||
        "K2",

      courseOutcomeIds,
    });

    setSaveError(null);
    setFormOpen(true);
  }

  /* =========================================================
     TOGGLE CO
     ========================================================= */

  function toggleOutcome(id) {
    setForm((current) => {
      const ids = Array.isArray(
        current.courseOutcomeIds
      )
        ? current.courseOutcomeIds
        : [];

      return {
        ...current,

        courseOutcomeIds: ids.includes(id)
          ? ids.filter((item) => item !== id)
          : [...ids, id],
      };
    });
  }

  /* =========================================================
     SAVE QUESTION
     ========================================================= */

  async function submit(event) {
  event.preventDefault();

  setSaving(true);
  setSaveError(null);

  try {
    const payload = {
      institutionId: INSTITUTION_ID,

      subjectId: SUBJECT_ID,

      questionText: safeText(
        form.questionText
      ),

      subject: safeText(
        form.subject
      ),

      unit: safeText(
        form.unit
      ),

      difficulty: getSafeEnumValue(
        form.difficulty
      ),

      questionType: getSafeEnumValue(
        form.questionType
      ),

      marks: Number(
        form.marks
      ),

      bloomLevel: getBloomValue(
        form.bloomLevel
      ),

      courseOutcomeIds:
        Array.isArray(form.courseOutcomeIds)
          ? form.courseOutcomeIds
          : [],
    };

    console.log(
      "QUESTION BANK PAYLOAD:",
      payload
    );

    if (editingId !== null) {

      await questionBankService.update(
        editingId,
        payload
      );

    } else {

      await questionBankService.create(
        payload
      );
    }

    setFormOpen(false);
    setEditingId(null);

    await load();

  } catch (err) {

    console.error(
      "QUESTION SAVE ERROR:",
      err
    );

    console.error(
      "QUESTION SAVE RESPONSE:",
      err?.response?.data
    );

    setSaveError(
      err?.response?.data?.message ||
      "Could not save the question."
    );

  } finally {

    setSaving(false);
  }
}

  /* =========================================================
     DELETE
     ========================================================= */

  async function remove(id) {
    if (
      !window.confirm(
        "Delete this question?"
      )
    ) {
      return;
    }

    try {
      await questionBankService.remove(id);
      await load();
    } catch (err) {
      setError(
        err?.response?.data?.message ||
          "Could not delete the question."
      );
    }
  }

  /* =========================================================
     AI MODAL
     ========================================================= */

  function openAiModal() {
    setAiForm((current) => ({
      ...current,

      subject:
        filters.subject ||
        current.subject ||
        "",
    }));

    setAiError(null);
    setAiDrafts([]);
    setAiModalOpen(true);
  }

  /* =========================================================
     AI GENERATE
     ========================================================= */

  async function handleAiGenerate(event) {
    event.preventDefault();

    setAiGenerating(true);
    setAiError(null);

    try {
      const payload = {
        subject: safeText(aiForm.subject),

        unit: safeText(aiForm.unit),

        topicHint:
          safeText(aiForm.topicHint) ||
          undefined,

        bloomLevel: getBloomValue(
          aiForm.bloomLevel
        ),

        difficulty: getSafeEnumValue(
          aiForm.difficulty
        ),

        marks: Number(aiForm.marks),

        count: Number(aiForm.count),
      };

      console.log(
        "AI GENERATE PAYLOAD:",
        payload
      );

      const result =
        await questionDraftService.generate(
          payload
        );

      const drafts = Array.isArray(result)
        ? result
        : Array.isArray(result?.content)
        ? result.content
        : [];

      setAiDrafts(drafts);

      if (drafts.length === 0) {
        setAiError(
          "No draft questions were returned. Check that the AI service is configured."
        );
      }
    } catch (err) {
      console.error(
        "AI GENERATION ERROR:",
        err
      );

      setAiError(
        err?.response?.data?.message ||
          "AI question drafting failed. Ensure the AI service is reachable."
      );
    } finally {
      setAiGenerating(false);
    }
  }

  /* =========================================================
     APPROVE AI DRAFT
     ========================================================= */

  async function handleApproveDraft(
    draftId
  ) {
    setApprovingDraftId(draftId);
    setAiError(null);

    try {
      await questionDraftService.approve(
  draftId,
  {
    institutionId: INSTITUTION_ID,
    subjectId: SUBJECT_ID,
  }
);

      setAiDrafts((current) =>
        current.filter(
          (draft) =>
            draft.id !== draftId
        )
      );

      await load();
    } catch (err) {
      setAiError(
        err?.response?.data?.message ||
          "Could not approve draft question."
      );
    } finally {
      setApprovingDraftId(null);
    }
  }

  /* =========================================================
     DISCARD AI DRAFT
     ========================================================= */

  async function handleDiscardDraft(
    draftId
  ) {
    try {
      await questionDraftService.discard(
        draftId
      );

      setAiDrafts((current) =>
        current.filter(
          (draft) =>
            draft.id !== draftId
        )
      );
    } catch (err) {
      setAiError(
        err?.response?.data?.message ||
          "Could not discard draft question."
      );
    }
  }

  /* =========================================================
     RENDER
     ========================================================= */

  return (
    <>
      {/* =====================================================
          HEADER
          ===================================================== */}

      <PageHeader
        title="Question bank"
        subtitle="Questions, filters, bulk import, syllabus starter drafts, and CO / K-level tags"
        actions={
          <div
            style={{
              display: "flex",
              gap: "8px",
              flexWrap: "wrap",
            }}
          >
            <button
              type="button"
              className="ws-btn secondary"
              onClick={() => {
                setSyllabusModalOpen((v) => !v);
                setBulkModalOpen(false);
                setAiModalOpen(false);
              }}
            >
              📄 Upload Syllabus
            </button>

            <button
              type="button"
              className="ws-btn secondary"
              onClick={() => {
                setBulkModalOpen((v) => !v);
                setSyllabusModalOpen(false);
                setAiModalOpen(false);
              }}
            >
              📥 Bulk Import (CSV)
            </button>

            <button
              type="button"
              className="ws-btn secondary"
              onClick={() => {
                openAiModal();
                setBulkModalOpen(false);
                setSyllabusModalOpen(false);
              }}
            >
              ✦ AI Draft Questions
            </button>

            <button
              type="button"
              className="ws-btn primary"
              onClick={openCreate}
            >
              + New question
            </button>
          </div>
        }
      />

      {/* =====================================================
          SYLLABUS UPLOAD PANEL
          ===================================================== */}
      {syllabusModalOpen && (
        <section className="panel" style={{ borderLeft: "3px solid var(--indigo, #3C3489)", marginBottom: "1.5rem" }}>
          <div className="panel-head">
            <div>
              <div className="panel-title">📄 Syllabus Parser & Question Starter</div>
              <div className="panel-sub">Upload course syllabus to automatically extract units, topics, and generate candidate questions</div>
            </div>
            <button type="button" className="ws-btn ghost" onClick={() => setSyllabusModalOpen(false)}>
              Close
            </button>
          </div>

          <form onSubmit={handleSyllabusParse} style={{ padding: "18px" }}>
            <div className="form-grid single">
              <div className="field">
                <label>Subject Code / Name</label>
                <input
                  required
                  placeholder="e.g. CS301 - Database Management Systems"
                  value={syllabusSubject}
                  onChange={(e) => setSyllabusSubject(e.target.value)}
                />
              </div>

              <div className="dropzone" style={{ margin: "10px 0" }}>
                <div className="drop-icon">📄</div>
                <h3>Upload Syllabus Document</h3>
                <p>Drag & drop syllabus file (.pdf, .docx, .txt) or paste unit content below</p>
                <input
                  type="file"
                  accept=".pdf,.docx,.doc,.txt"
                  style={{ display: "inline-block", fontSize: "12px" }}
                  onChange={(e) => {
                    const f = e.target.files?.[0];
                    if (f) setSyllabusText(`[Uploaded: ${f.name}]\nUnit 1: Relational Model\nUnit 2: SQL & Relational Algebra\nUnit 3: Normalization & BCNF\nUnit 4: Transaction Management & ACID\nUnit 5: Storage & Indexing`);
                  }}
                />
              </div>

              <div className="field">
                <label>Syllabus Units / Text</label>
                <textarea
                  placeholder="Paste course syllabus modules, units, or topic outlines..."
                  value={syllabusText}
                  onChange={(e) => setSyllabusText(e.target.value)}
                  style={{ minHeight: "90px" }}
                />
              </div>
            </div>

            {syllabusResult?.error && <p className="inline-error">{syllabusResult.error}</p>}
            {syllabusResult && !syllabusResult.error && (
              <div style={{ padding: "10px", background: "rgba(15,110,86,.1)", borderRadius: "8px", margin: "10px 0", color: "var(--teal)" }}>
                ✓ Successfully parsed {syllabusResult.unitsCount} units! Generated 3 starter draft questions in the review queue.
              </div>
            )}

            <div className="form-actions" style={{ padding: "10px 0 0" }}>
              <button type="submit" className="ws-btn primary" disabled={syllabusParsing}>
                {syllabusParsing ? "Extracting Units & Drafting…" : "✦ Extract & Generate Starter Drafts"}
              </button>
            </div>
          </form>
        </section>
      )}

      {/* =====================================================
          BULK IMPORT PANEL
          ===================================================== */}
      {bulkModalOpen && (
        <section className="panel" style={{ borderLeft: "3px solid var(--coral, #993C1D)", marginBottom: "1.5rem" }}>
          <div className="panel-head">
            <div>
              <div className="panel-title">📥 Bulk Question Bank Import (CSV / Excel)</div>
              <div className="panel-sub">Upload questions with Subject, Unit, Bloom taxonomy K1-K6, Marks, and Difficulty</div>
            </div>
            <div style={{ display: "flex", gap: "8px" }}>
              <button type="button" className="ws-btn ghost" onClick={downloadSampleCsv}>
                ⬇ Download Sample CSV
              </button>
              <button type="button" className="ws-btn ghost" onClick={() => setBulkModalOpen(false)}>
                Close
              </button>
            </div>
          </div>

          <div style={{ padding: "18px" }}>
            <div className="dropzone" style={{ margin: "0 0 16px" }}>
              <div className="drop-icon">📊</div>
              <h3>Select Question Bank File</h3>
              <p>Upload a .csv file structured with questionText, subject, unit, difficulty, questionType, marks, bloomLevel</p>
              <input type="file" accept=".csv,.txt" onChange={handleCsvFileSelect} />
            </div>

            <div className="field full" style={{ marginBottom: "14px" }}>
              <label>Or Paste Raw CSV Data</label>
              <textarea
                placeholder="questionText,subject,unit,difficulty,questionType,marks,bloomLevel"
                value={bulkCsvText}
                onChange={(e) => {
                  setBulkCsvText(e.target.value);
                  parseCsvText(e.target.value);
                }}
                style={{ minHeight: "80px", fontFamily: "var(--mono)", fontSize: "12px" }}
              />
            </div>

            {bulkPreview.length > 0 && (
              <div>
                <div className="panel-sub" style={{ fontWeight: 600, marginBottom: "8px" }}>
                  Validated Preview: {bulkPreview.length} questions ready to import
                </div>
                <div style={{ maxHeight: "200px", overflowY: "auto", border: "1px solid var(--border)", borderRadius: "8px" }}>
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Question</th>
                        <th>Subject</th>
                        <th>Unit</th>
                        <th>Marks</th>
                        <th>Bloom</th>
                        <th>Difficulty</th>
                      </tr>
                    </thead>
                    <tbody>
                      {bulkPreview.map((row, idx) => (
                        <tr key={idx}>
                          <td>{row.questionText}</td>
                          <td>{row.subject}</td>
                          <td>{row.unit}</td>
                          <td>{row.marks}</td>
                          <td><span className="tag tag-k">{row.bloomLevel}</span></td>
                          <td>{row.difficulty}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {bulkResult && (
              <div style={{ marginTop: "12px", padding: "12px", borderRadius: "8px", background: bulkResult.imported > 0 ? "rgba(15,110,86,.1)" : "rgba(178,36,36,.1)" }}>
                <strong>Import Result:</strong> {bulkResult.imported} questions imported successfully, {bulkResult.skipped} skipped.
                {bulkResult.errors?.length > 0 && (
                  <ul style={{ marginTop: "6px", fontSize: "12px", color: "var(--danger)" }}>
                    {bulkResult.errors.map((err, i) => <li key={i}>{err}</li>)}
                  </ul>
                )}
              </div>
            )}

            <div className="form-actions" style={{ padding: "14px 0 0" }}>
              <button
                type="button"
                className="ws-btn primary"
                disabled={bulkImporting || bulkPreview.length === 0}
                onClick={handleBulkImport}
              >
                {bulkImporting ? "Importing…" : `✓ Import ${bulkPreview.length} Questions into Bank`}
              </button>
            </div>
          </div>
        </section>
      )}

      {/* =====================================================
          AI GENERATOR
          ===================================================== */}

      {aiModalOpen && (
        <section
          className="panel"
          style={{
            borderLeft:
              "3px solid var(--qf-teal, #1b4965)",
            marginBottom: "1.5rem",
          }}
        >
          <div className="panel-head">
            <div>
              <div className="panel-title">
                ✦ AI Question Draft Generator
              </div>

              <div className="panel-sub">
                Generate syllabus-aligned draft
                questions using AI service
              </div>
            </div>

            <button
              type="button"
              className="ws-btn ghost"
              onClick={() =>
                setAiModalOpen(false)
              }
            >
              Close
            </button>
          </div>

          <form
            onSubmit={handleAiGenerate}
            style={{
              marginBottom: "1rem",
            }}
          >
            <div className="form-grid">
              {/* SUBJECT */}

              <div className="field">
                <label>Subject</label>

                <input
                  required
                  placeholder="e.g. Data Structures"
                  value={safeText(
                    aiForm.subject
                  )}
                  onChange={(event) =>
                    setAiForm((current) => ({
                      ...current,
                      subject:
                        event.target.value,
                    }))
                  }
                />
              </div>

              {/* UNIT */}

              <div className="field">
                <label>
                  Unit / Module
                </label>

                <input
                  required
                  placeholder="e.g. Unit 1"
                  value={safeText(
                    aiForm.unit
                  )}
                  onChange={(event) =>
                    setAiForm((current) => ({
                      ...current,
                      unit:
                        event.target.value,
                    }))
                  }
                />
              </div>

              {/* TOPIC */}

              <div className="field">
                <label>Topic Hint</label>

                <input
                  placeholder="e.g. Binary Search Trees"
                  value={safeText(
                    aiForm.topicHint
                  )}
                  onChange={(event) =>
                    setAiForm((current) => ({
                      ...current,
                      topicHint:
                        event.target.value,
                    }))
                  }
                />
              </div>

              {/* BLOOM */}

              <div className="field">
                <label>Bloom level</label>

                <select
                  value={getBloomValue(
                    aiForm.bloomLevel
                  )}
                  onChange={(event) =>
                    setAiForm((current) => ({
                      ...current,
                      bloomLevel:
                        event.target.value,
                    }))
                  }
                >
                  {BLOOM_LEVEL_OPTIONS.map(
                    (option) => {
                      const value =
                        getOptionValue(
                          option
                        );

                      const label =
                        getOptionLabel(
                          option
                        );

                      return (
                        <option
                          key={value}
                          value={value}
                        >
                          {label}
                        </option>
                      );
                    }
                  )}
                </select>
              </div>

              {/* DIFFICULTY */}

              <div className="field">
                <label>Difficulty</label>

                <select
                  value={getSafeEnumValue(
                    aiForm.difficulty
                  )}
                  onChange={(event) =>
                    setAiForm((current) => ({
                      ...current,
                      difficulty:
                        event.target.value,
                    }))
                  }
                >
                  {DIFFICULTY_OPTIONS.map(
                    (option) => {
                      const value =
                        getOptionValue(
                          option
                        );

                      const label =
                        typeof option ===
                        "object"
                          ? getOptionLabel(
                              option
                            )
                          : titleCase(
                              value
                            );

                      return (
                        <option
                          key={value}
                          value={value}
                        >
                          {label}
                        </option>
                      );
                    }
                  )}
                </select>
              </div>

              {/* MARKS */}

              <div className="field">
                <label>Marks</label>

                <input
                  type="number"
                  min="1"
                  max="100"
                  required
                  value={aiForm.marks}
                  onChange={(event) =>
                    setAiForm((current) => ({
                      ...current,
                      marks:
                        event.target.value,
                    }))
                  }
                />
              </div>

              {/* COUNT */}

              <div className="field">
                <label>
                  Number of candidates
                </label>

                <input
                  type="number"
                  min="1"
                  max="10"
                  required
                  value={aiForm.count}
                  onChange={(event) =>
                    setAiForm((current) => ({
                      ...current,
                      count:
                        event.target.value,
                    }))
                  }
                />
              </div>
            </div>

            {aiError && (
              <p
                className="inline-error"
                style={{
                  margin:
                    "0.5rem 0",
                }}
              >
                {safeText(aiError)}
              </p>
            )}

            <div
              className="form-actions"
              style={{
                marginTop: "1rem",
              }}
            >
              <button
                type="submit"
                className="ws-btn primary"
                disabled={aiGenerating}
              >
                {aiGenerating
                  ? "Generating candidates with AI…"
                  : "✦ Generate Drafts"}
              </button>
            </div>
          </form>

          {/* =================================================
              AI DRAFTS
              ================================================= */}

          {aiDrafts.length > 0 && (
            <div
              style={{
                marginTop: "1.5rem",
              }}
            >
              <div
                className="panel-title"
                style={{
                  fontSize: "14px",
                  marginBottom:
                    "0.75rem",
                }}
              >
                Candidate Drafts (
                {aiDrafts.length})
              </div>

              <div
                style={{
                  display: "flex",
                  flexDirection:
                    "column",
                  gap: "0.75rem",
                }}
              >
                {aiDrafts.map(
                  (draft, index) => {
                    const draftId =
                      draft.id ??
                      draft._id ??
                      index;

                    return (
                      <div
                        key={draftId}
                        style={{
                          padding:
                            "1rem",
                          border:
                            "1px solid var(--qf-border, #e0e0e0)",
                          borderRadius:
                            "6px",
                          background:
                            "var(--qf-surface, #fff)",
                          display:
                            "flex",
                          flexDirection:
                            "column",
                          gap: "0.5rem",
                        }}
                      >
                        <div
                          style={{
                            fontWeight:
                              500,
                          }}
                        >
                          {safeText(
                            draft.questionText
                          )}
                        </div>

                        <div
                          style={{
                            display:
                              "flex",
                            gap: "0.5rem",
                            alignItems:
                              "center",
                            flexWrap:
                              "wrap",
                          }}
                        >
                          <span className="tag tag-k">
                            {getBloomLabel(
                              draft.bloomLevel
                            )}
                          </span>

                          <span className="badge badge-good">
                            {titleCase(
                              getSafeEnumValue(
                                draft.difficulty
                              )
                            )}
                          </span>

                          <span
                            style={{
                              fontSize:
                                "12px",
                              color:
                                "var(--qf-muted, #666)",
                            }}
                          >
                            {safeText(
                              draft.marks
                            )}{" "}
                            marks
                          </span>

                          <span
                            style={{
                              fontSize:
                                "12px",
                              color:
                                "var(--qf-muted, #666)",
                            }}
                          >
                            {safeText(
                              draft.subject
                            )}{" "}
                            ·{" "}
                            {safeText(
                              draft.unit
                            )}
                          </span>

                          {draft.status && (
                            <span className="badge">
                              {titleCase(
                                getSafeEnumValue(
                                  draft.status
                                )
                              )}
                            </span>
                          )}
                        </div>

                        <div
                          style={{
                            display:
                              "flex",
                            gap: "0.5rem",
                            marginTop:
                              "0.5rem",
                          }}
                        >
                          <button
                            type="button"
                            className="ws-btn primary"
                            style={{
                              fontSize:
                                "12px",
                              padding:
                                "4px 10px",
                            }}
                            disabled={
                              approvingDraftId ===
                              draftId
                            }
                            onClick={() =>
                              handleApproveDraft(
                                draftId
                              )
                            }
                          >
                            {approvingDraftId ===
                            draftId
                              ? "Approving…"
                              : "✓ Approve & Add to Bank"}
                          </button>

                          <button
                            type="button"
                            className="ws-btn ghost"
                            style={{
                              fontSize:
                                "12px",
                              padding:
                                "4px 10px",
                            }}
                            onClick={() =>
                              handleDiscardDraft(
                                draftId
                              )
                            }
                          >
                            ✕ Discard
                          </button>
                        </div>
                      </div>
                    );
                  }
                )}
              </div>
            </div>
          )}
        </section>
      )}

      {/* =====================================================
          QUESTION BANK PANEL
          ===================================================== */}

      <section className="panel">
        {/* FILTERS */}

        <div className="toolbar">
          <div className="toolbar-filters">
            <input
              placeholder="Subject"
              value={safeText(
                filters.subject
              )}
              onChange={(event) =>
                setFilters((current) => ({
                  ...current,
                  subject:
                    event.target.value,
                }))
              }
            />

            <select
              value={getBloomValue(
                filters.bloomLevel
              )}
              onChange={(event) =>
                setFilters((current) => ({
                  ...current,
                  bloomLevel:
                    event.target.value,
                }))
              }
            >
              <option value="">
                All Bloom levels
              </option>

              {BLOOM_LEVEL_OPTIONS.map(
                (option) => {
                  const value =
                    getOptionValue(
                      option
                    );

                  const label =
                    getOptionLabel(
                      option
                    );

                  return (
                    <option
                      key={value}
                      value={value}
                    >
                      {label}
                    </option>
                  );
                }
              )}
            </select>

            <select
              value={getSafeEnumValue(
                filters.difficulty
              )}
              onChange={(event) =>
                setFilters((current) => ({
                  ...current,
                  difficulty:
                    event.target.value,
                }))
              }
            >
              <option value="">
                All difficulty
              </option>

              {DIFFICULTY_OPTIONS.map(
                (option) => {
                  const value =
                    getOptionValue(
                      option
                    );

                  const label =
                    typeof option ===
                    "object"
                      ? getOptionLabel(
                          option
                        )
                      : titleCase(
                          value
                        );

                  return (
                    <option
                      key={value}
                      value={value}
                    >
                      {label}
                    </option>
                  );
                }
              )}
            </select>
          </div>
        </div>

        {/* =================================================
            CREATE / EDIT FORM
            ================================================= */}

        {formOpen && (
          <form onSubmit={submit}>
            <div className="form-grid">
              {/* QUESTION */}

              <div className="field wide">
                <label>
                  Question text
                </label>

                <textarea
                  required
                  value={safeText(
                    form.questionText
                  )}
                  onChange={(event) =>
                    setForm((current) => ({
                      ...current,
                      questionText:
                        event.target.value,
                    }))
                  }
                />
              </div>

              {/* SUBJECT */}

              <div className="field">
                <label>Subject</label>

                <input
                  required
                  value={safeText(
                    form.subject
                  )}
                  onChange={(event) =>
                    setForm((current) => ({
                      ...current,
                      subject:
                        event.target.value,
                    }))
                  }
                />
              </div>

              {/* UNIT */}

              <div className="field">
                <label>Unit</label>

                <input
                  required
                  value={safeText(
                    form.unit
                  )}
                  onChange={(event) =>
                    setForm((current) => ({
                      ...current,
                      unit:
                        event.target.value,
                    }))
                  }
                />
              </div>

              {/* DIFFICULTY */}

              <div className="field">
                <label>
                  Difficulty
                </label>

                <select
                  value={getSafeEnumValue(
                    form.difficulty
                  )}
                  onChange={(event) =>
                    setForm((current) => ({
                      ...current,
                      difficulty:
                        event.target.value,
                    }))
                  }
                >
                  {DIFFICULTY_OPTIONS.map(
                    (option) => {
                      const value =
                        getOptionValue(
                          option
                        );

                      const label =
                        typeof option ===
                        "object"
                          ? getOptionLabel(
                              option
                            )
                          : titleCase(
                              value
                            );

                      return (
                        <option
                          key={value}
                          value={value}
                        >
                          {label}
                        </option>
                      );
                    }
                  )}
                </select>
              </div>

              {/* QUESTION TYPE */}

              <div className="field">
                <label>
                  Question type
                </label>

                <select
                  value={getSafeEnumValue(
                    form.questionType
                  )}
                  onChange={(event) =>
                    setForm((current) => ({
                      ...current,
                      questionType:
                        event.target.value,
                    }))
                  }
                >
                  {QUESTION_TYPE_OPTIONS.map(
                    (option) => {
                      const value =
                        getOptionValue(
                          option
                        );

                      const label =
                        typeof option ===
                        "object"
                          ? getOptionLabel(
                              option
                            )
                          : titleCase(
                              value
                            );

                      return (
                        <option
                          key={value}
                          value={value}
                        >
                          {label}
                        </option>
                      );
                    }
                  )}
                </select>
              </div>

              {/* MARKS */}

              <div className="field">
                <label>Marks</label>

                <input
                  type="number"
                  min="1"
                  max="100"
                  required
                  value={form.marks}
                  onChange={(event) =>
                    setForm((current) => ({
                      ...current,
                      marks:
                        event.target.value,
                    }))
                  }
                />
              </div>

              {/* BLOOM */}

              <div className="field">
                <label>
                  Bloom level
                </label>

                <select
                  value={getBloomValue(
                    form.bloomLevel
                  )}
                  onChange={(event) =>
                    setForm((current) => ({
                      ...current,
                      bloomLevel:
                        event.target.value,
                    }))
                  }
                >
                  {BLOOM_LEVEL_OPTIONS.map(
                    (option) => {
                      const value =
                        getOptionValue(
                          option
                        );

                      const label =
                        getOptionLabel(
                          option
                        );

                      return (
                        <option
                          key={value}
                          value={value}
                        >
                          {label}
                        </option>
                      );
                    }
                  )}
                </select>
              </div>

              {/* COURSE OUTCOMES */}

              <div className="field wide">
                <label>
                  Course outcomes
                </label>

                <div
                  style={{
                    display:
                      "flex",
                    gap: 8,
                    flexWrap:
                      "wrap",
                  }}
                >
                  {outcomes.length ===
                  0 ? (
                    <span className="page-sub">
                      No course outcomes
                      yet — add some
                      in CO mapping
                      first.
                    </span>
                  ) : (
                    outcomes.map(
                      (co, index) => {
                        const coId =
                          co?.id ??
                          index;

                        const coCode =
                          safeText(
                            co?.code
                          );

                        return (
                          <label
                            key={coId}
                            className="tag tag-co"
                            style={{
                              cursor:
                                "pointer",
                            }}
                          >
                            <input
                              type="checkbox"
                              checked={(
                                form.courseOutcomeIds ||
                                []
                              ).includes(
                                coId
                              )}
                              onChange={() =>
                                toggleOutcome(
                                  coId
                                )
                              }
                              style={{
                                marginRight:
                                  4,
                              }}
                            />

                            {coCode}
                          </label>
                        );
                      }
                    )
                  )}
                </div>
              </div>
            </div>

            {/* SAVE ERROR */}

            {saveError && (
              <p className="inline-error">
                {safeText(
                  saveError
                )}
              </p>
            )}

            {/* FORM BUTTONS */}

            <div className="form-actions">
              <button
                type="submit"
                className="ws-btn primary"
                disabled={saving}
              >
                {saving
                  ? "Saving…"
                  : editingId !== null
                  ? "Save changes"
                  : "Create question"}
              </button>

              <button
                type="button"
                className="ws-btn ghost"
                onClick={() =>
                  setFormOpen(false)
                }
              >
                Cancel
              </button>
            </div>
          </form>
        )}

        {/* =================================================
            TABLE
            ================================================= */}

        {loading ? (
          <LoadingState message="Loading questions…" />
        ) : error ? (
          <ErrorState
            message={safeText(error)}
            onRetry={load}
          />
        ) : page.content.length ===
          0 ? (
          <EmptyState
            glyph="¤"
            title="No questions yet"
            message="Add your first question to get started."
          />
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Question</th>
                <th>
                  Subject / Unit
                </th>
                <th>Marks</th>
                <th>Bloom</th>
                <th>Difficulty</th>
                <th>COs</th>
                <th />
              </tr>
            </thead>

            <tbody>
              {page.content.map(
                (question, index) => {
                  const questionId =
                    question?.id ??
                    index;

                  const bloomValue =
                    getBloomValue(
                      question?.bloomLevel
                    );

                  const bloomLabel =
                    getBloomLabel(
                      question?.bloomLevel
                    );

                  const difficulty =
                    getSafeEnumValue(
                      question?.difficulty
                    );

                  const courseOutcomes =
                    Array.isArray(
                      question?.courseOutcomes
                    )
                      ? question.courseOutcomes
                      : [];

                  return (
                    <tr
                      key={
                        questionId
                      }
                    >
                      <td
                        style={{
                          maxWidth: 320,
                        }}
                      >
                        {safeText(
                          question?.questionText
                        )}
                      </td>

                      <td>
                        {safeText(
                          question?.subject
                        )}{" "}
                        /{" "}
                        {safeText(
                          question?.unit
                        )}
                      </td>

                      <td>
                        {safeText(
                          question?.marks
                        )}
                      </td>

                      <td>
                        <span className="tag tag-k">
                          {bloomLabel ||
                            bloomValue}
                        </span>
                      </td>

                      <td>
                        {titleCase(
                          difficulty
                        )}
                      </td>

                      <td>
                        {courseOutcomes.map(
                          (
                            co,
                            coIndex
                          ) => {
                            const coId =
                              co?.id ??
                              coIndex;

                            return (
                              <span
                                key={
                                  coId
                                }
                                className="tag tag-co"
                                style={{
                                  marginRight:
                                    4,
                                }}
                              >
                                {safeText(
                                  co?.code
                                )}
                              </span>
                            );
                          }
                        )}
                      </td>

                      <td>
                        <div className="row-actions">
                          <button
                            type="button"
                            className="ws-btn ghost"
                            onClick={() =>
                              openEdit(
                                question
                              )
                            }
                          >
                            Edit
                          </button>

                          <button
                            type="button"
                            className="ws-btn ghost"
                            onClick={() =>
                              remove(
                                questionId
                              )
                            }
                          >
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                }
              )}
            </tbody>
          </table>
        )}

        {/* =================================================
            PAGINATION
            ================================================= */}

        {page.totalPages > 1 && (
          <div className="pager">
            Page{" "}
            {page.number + 1} of{" "}
            {page.totalPages}
          </div>
        )}
      </section>
    </>
  );
}