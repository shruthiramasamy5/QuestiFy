import { useEffect, useState } from "react";
import LoadingState from "../common/LoadingState.jsx";
import ErrorState from "../common/ErrorState.jsx";
import EmptyState from "../common/EmptyState.jsx";
import auditLogService from "../../services/auditLogService.js";

export default function AuditEventsPanel() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filters, setFilters] = useState({ actor: "", action: "" });

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const params = { page: 0, size: 50 };
      if (filters.actor) params.actor = filters.actor;
      if (filters.action) params.action = filters.action;
      const data = await auditLogService.search(params);
      setEvents(Array.isArray(data) ? data : data?.content || []);
    } catch (err) {
      setError(err?.response?.data?.message || "Could not load the audit trail.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters]);

  return (
    <section className="panel">
      <div className="toolbar">
        <div className="toolbar-filters">
          <input placeholder="Actor" value={filters.actor} onChange={(e) => setFilters((f) => ({ ...f, actor: e.target.value }))} />
          <input placeholder="Action" value={filters.action} onChange={(e) => setFilters((f) => ({ ...f, action: e.target.value }))} />
        </div>
      </div>
      {loading ? (
        <LoadingState message="Loading activity\u2026" />
      ) : error ? (
        <ErrorState message={error} onRetry={load} />
      ) : events.length === 0 ? (
        <EmptyState glyph={"\u25A1"} title="No activity yet" />
      ) : (
        <table className="data-table">
          <thead><tr><th>When</th><th>Actor</th><th>Action</th><th>Target</th></tr></thead>
          <tbody>
            {events.map((ev) => (
              <tr key={ev.id}>
                <td>{ev.timestamp ? new Date(ev.timestamp).toLocaleString() : "\u2014"}</td>
                <td>{ev.actorUserId || "\u2014"} {ev.actorRole ? `(${ev.actorRole})` : ""}</td>
                <td>{ev.action}</td>
                <td>{ev.targetEntityType ? `${ev.targetEntityType} #${ev.targetEntityId}` : "\u2014"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}