import { useEffect, useState } from "react";
import PageHeader from "../components/common/PageHeader.jsx";
import LoadingState from "../components/common/LoadingState.jsx";
import ErrorState from "../components/common/ErrorState.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import planUsageService from "../services/planUsageService.js";
import { titleCase } from "../utils/options.js";

function UsageBar({ label, metric }) {
  if (!metric) return null;
  return (
    <div className="bar-row">
      <div className="bar-label">{label}</div>
      <div className="bar-track"><div className="bar-fill" style={{ width: `${Math.min(metric.utilizationPercent, 100)}%` }} /></div>
      <div className="bar-count">{metric.used}/{metric.limit}</div>
    </div>
  );
}

export default function PlanUsagePage() {
  const [stats, setStats] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const [s, h] = await Promise.all([planUsageService.stats(), planUsageService.history()]);
      setStats(s);
      setHistory(Array.isArray(h) ? h : h?.content || []);
    } catch (err) {
      setError(err?.response?.data?.message || "Could not load plan & usage data.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  return (
    <>
      <PageHeader title="Plan & usage" subtitle="Subscription and usage limits" />
      {loading ? (
        <section className="panel"><LoadingState message="Loading your plan\u2026" /></section>
      ) : error ? (
        <section className="panel"><ErrorState message={error} onRetry={load} /></section>
      ) : !stats ? (
        <section className="panel"><EmptyState glyph={"\u25A1"} title="No plan data available" /></section>
      ) : (
        <>
          <section className="panel">
            <div className="panel-head">
              <div>
                <div className="panel-title">{stats.institutionName || "Institution Plan"}</div>
                <div className="panel-sub">{titleCase(stats.planTier || "FREE")} plan \u00B7 {titleCase(stats.status || "ACTIVE")} {stats.currentPeriod ? `\u00B7 ${stats.currentPeriod}` : ""}</div>
              </div>
              {stats.monthlyPrice != null && <span className="badge badge-info">${stats.monthlyPrice}/mo</span>}
            </div>
            <div className="panel-body">
              <UsageBar label="Papers" metric={stats.papers} />
              <UsageBar label="Users" metric={stats.users} />
              <UsageBar label="Storage" metric={stats.storageMb} />
            </div>
            <div className="stat-grid">
              <div className="stat-cell"><div className="num">{stats.questionsCreatedThisPeriod}</div><div className="label">Questions this period</div></div>
              <div className="stat-cell"><div className="num">{stats.departmentCount}</div><div className="label">Departments</div></div>
              <div className="stat-cell"><div className="num">{stats.templateCount}</div><div className="label">Templates</div></div>
            </div>
          </section>

          <section className="panel">
            <div className="panel-head"><div className="panel-title">Usage history</div></div>
            {history.length === 0 ? (
              <EmptyState glyph={"\u25D2"} title="No history yet" />
            ) : (
              <table className="data-table">
                <thead><tr><th>Period</th><th>Papers</th><th>Questions</th><th>Active users</th><th>Storage (MB)</th></tr></thead>
                <tbody>
                  {history.map((h) => (
                    <tr key={h.period}>
                      <td>{h.period}</td>
                      <td>{h.papersGenerated}</td>
                      <td>{h.questionsCreated}</td>
                      <td>{h.activeUsers}</td>
                      <td>{h.storageUsedMb}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </section>
        </>
      )}
    </>
  );
}