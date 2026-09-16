import { useEffect, useState } from "react";
import PageHeader from "../components/common/PageHeader.jsx";
import LoadingState from "../components/common/LoadingState.jsx";
import ErrorState from "../components/common/ErrorState.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import analyticsService from "../services/analyticsService.js";
import { useAuth } from "../context/AuthContext.jsx";
import { ROLES, normalizeRole } from "../utils/roles.js";

function CoverageBars({ slices }) {
  if (!slices || slices.length === 0) return <EmptyState glyph={"\u25D2"} title="No coverage data yet" />;
  const max = Math.max(...slices.map((s) => s.count), 1);
  return (
    <div className="panel-body">
      {slices.map((s) => (
        <div className="bar-row" key={s.label}>
          <div className="bar-label">{s.label}</div>
          <div className="bar-track"><div className="bar-fill" style={{ width: `${(s.count / max) * 100}%` }} /></div>
          <div className="bar-count">{s.count}</div>
        </div>
      ))}
    </div>
  );
}

export default function AnalyticsPage() {
  const { user } = useAuth();
  const role = normalizeRole(user?.role);
  const [overview, setOverview] = useState(null);
  const [approvalStats, setApprovalStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const tasks = [analyticsService.overview()];
      if (role === "HOD") tasks.push(analyticsService.approvals());
      const [ov, ap] = await Promise.all(tasks);
      setOverview(ov);
      if (ap) setApprovalStats(ap);
    } catch (err) {
      setError(err?.response?.data?.message || "Could not load analytics.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.role]);

  return (
    <>
      <PageHeader title="Analytics" subtitle="CO attainment and question quality" />

      {loading ? (
        <section className="panel"><LoadingState message="Crunching the numbers\u2026" /></section>
      ) : error ? (
        <section className="panel"><ErrorState message={error} onRetry={load} /></section>
      ) : (
        <>
          {overview?.degradedSources?.length > 0 && (
            <section className="panel">
              <div className="panel-body">
                <span className="badge badge-warn">Partial data</span>{" "}
                <span className="page-sub">
                  Some figures could not be reached: {overview.degradedSources.join(", ")}
                </span>
              </div>
            </section>
          )}

          <section className="panel">
            <div className="panel-head"><div className="panel-title">At a glance</div></div>
            <div className="stat-grid">
              <div className="stat-cell">
                <div className="num">{overview?.questions?.totalQuestions ?? 0}</div>
                <div className="label">Questions in bank</div>
              </div>
              <div className="stat-cell">
                <div className="num">{overview?.papers?.totalPapers ?? 0}</div>
                <div className="label">Papers total</div>
              </div>
              <div className="stat-cell">
                <div className="num">{overview?.papers?.generatedPapers ?? 0}</div>
                <div className="label">Generated this period</div>
              </div>
              <div className="stat-cell">
                <div className="num">{overview?.approvals?.pending ?? 0}</div>
                <div className="label">Approvals pending</div>
              </div>
            </div>
          </section>

          <section className="panel">
            <div className="panel-head"><div className="panel-title">CO coverage</div><div className="panel-sub">Questions tagged per course outcome</div></div>
            <CoverageBars slices={overview?.questions?.coCoverage} />
          </section>

          <section className="panel">
            <div className="panel-head"><div className="panel-title">Bloom coverage</div><div className="panel-sub">Questions tagged per K-level</div></div>
            <CoverageBars slices={overview?.questions?.bloomCoverage} />
          </section>

          <section className="panel">
            <div className="panel-head"><div className="panel-title">Papers per month</div></div>
            <CoverageBars
              slices={(overview?.papers?.papersPerMonth || []).map((m) => ({ label: m.month, count: m.count }))}
            />
          </section>

          {approvalStats && (
            <section className="panel">
              <div className="panel-head"><div className="panel-title">Approval turnaround</div></div>
              <div className="stat-grid">
                <div className="stat-cell"><div className="num">{approvalStats.pending}</div><div className="label">Pending</div></div>
                <div className="stat-cell"><div className="num">{approvalStats.approved}</div><div className="label">Approved</div></div>
                <div className="stat-cell"><div className="num">{approvalStats.rejected}</div><div className="label">Rejected</div></div>
                <div className="stat-cell">
                  <div className="num">{approvalStats.averageTurnaroundHours != null ? approvalStats.averageTurnaroundHours.toFixed(1) : "\u2014"}</div>
                  <div className="label">Avg hours to decide</div>
                </div>
              </div>
            </section>
          )}
        </>
      )}
    </>
  );
}