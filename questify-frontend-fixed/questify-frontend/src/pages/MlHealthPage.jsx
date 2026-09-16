import { useEffect, useState } from "react";
import PageHeader from "../components/common/PageHeader.jsx";
import LoadingState from "../components/common/LoadingState.jsx";
import ErrorState from "../components/common/ErrorState.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import mlHealthService from "../services/mlHealthService.js";
import { badgeClassFor, titleCase } from "../utils/options.js";

export default function MlHealthPage() {
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [probing, setProbing] = useState(false);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const data = await mlHealthService.current();
      setHealth(data);
    } catch (err) {
      setError(err?.response?.data?.message || "Could not load ML service health.");
    } finally {
      setLoading(false);
    }
  }

  async function runProbe() {
    setProbing(true);
    try {
      const data = await mlHealthService.probe();
      setHealth(data);
    } catch (err) {
      setError(err?.response?.data?.message || "Probe failed.");
    } finally {
      setProbing(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  const rows = Array.isArray(health) ? health : health ? [health] : [];
  const primary = rows[0] || health;

  return (
    <>
      <PageHeader
        title="ML service health"
        subtitle="Status of the paper-generation model service"
        actions={
          <button type="button" className="ws-btn primary" onClick={runProbe} disabled={probing}>
            {probing ? "Probing\u2026" : "Run probe"}
          </button>
        }
      />

      {primary && (
        <div className="stat-grid" style={{ marginBottom: "1.5rem" }}>
          <div className="stat-cell">
            <div className="num">
              <span className={badgeClassFor(primary.status)}>{titleCase(primary.status || "UNKNOWN")}</span>
            </div>
            <div className="label">Service status</div>
          </div>
          <div className="stat-cell">
            <div className="num">{primary.latencyMs != null ? `${primary.latencyMs} ms` : "\u2014"}</div>
            <div className="label">Last latency</div>
          </div>
          <div className="stat-cell">
            <div className="num">
              {primary.averageLatencyMs != null ? `${Math.round(primary.averageLatencyMs)} ms` : "\u2014"}
            </div>
            <div className="label">Avg latency</div>
          </div>
          <div className="stat-cell">
            <div className="num">{primary.errorRate != null ? `${(primary.errorRate * 100).toFixed(1)}%` : "0%"}</div>
            <div className="label">Error rate ({primary.errorCount || 0}/{primary.totalChecks || 0})</div>
          </div>
        </div>
      )}

      <section className="panel">
        {loading ? (
          <LoadingState message="Checking ML service\u2026" />
        ) : error ? (
          <ErrorState message={error} onRetry={load} />
        ) : rows.length === 0 ? (
          <EmptyState glyph={"\u2726"} title="No health data yet" />
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Endpoint</th>
                <th>Status</th>
                <th>HTTP Status</th>
                <th>Latency</th>
                <th>Error Rate</th>
                <th>Last Checked</th>
                <th>Last Error</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((r, i) => (
                <tr key={r.id || i}>
                  <td><code>{r.endpoint || "ML service"}</code></td>
                  <td><span className={badgeClassFor(r.status)}>{titleCase(r.status || "UNKNOWN")}</span></td>
                  <td>{r.lastHttpStatus ? <span className={`badge ${r.lastHttpStatus === 200 ? "badge-good" : "badge-warn"}`}>{r.lastHttpStatus}</span> : "\u2014"}</td>
                  <td>{r.latencyMs != null ? `${r.latencyMs} ms` : "\u2014"}</td>
                  <td>{r.errorRate != null ? `${(r.errorRate * 100).toFixed(1)}%` : "0%"}</td>
                  <td>{r.lastCheckedAt || r.checkedAt || r.timestamp ? new Date(r.lastCheckedAt || r.checkedAt || r.timestamp).toLocaleString() : "Never"}</td>
                  <td>{r.lastError ? <span style={{ color: "var(--qf-crimson, #9e2a2b)", fontSize: "12px" }}>{r.lastError}</span> : "\u2014"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </>
  );
}

