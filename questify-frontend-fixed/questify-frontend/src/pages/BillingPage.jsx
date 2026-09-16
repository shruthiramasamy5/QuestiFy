import { useEffect, useState } from "react";
import PageHeader from "../components/common/PageHeader.jsx";
import LoadingState from "../components/common/LoadingState.jsx";
import ErrorState from "../components/common/ErrorState.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import institutionService from "../services/institutionService.js";
import { titleCase } from "../utils/options.js";

export default function BillingPage() {
  const [institutions, setInstitutions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const data = await institutionService.list({ size: 100 });
      setInstitutions(Array.isArray(data) ? data : data?.content || []);
    } catch (err) {
      setError(err?.response?.data?.message || "Could not load billing and plans.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  return (
    <>
      <PageHeader title="Billing and plans" subtitle="Plan tier and status across institutions" />
      <section className="panel">
        {loading ? (
          <LoadingState message="Loading billing data\u2026" />
        ) : error ? (
          <ErrorState message={error} onRetry={load} />
        ) : institutions.length === 0 ? (
          <EmptyState glyph={"\u25D2"} title="No institutions yet" />
        ) : (
          <table className="data-table">
            <thead><tr><th>Institution</th><th>Plan</th><th>Status</th></tr></thead>
            <tbody>
              {institutions.map((inst) => (
                <tr key={inst.id}>
                  <td>{inst.name}</td>
                  <td>{titleCase(inst.planTier)}</td>
                  <td>{titleCase(inst.status)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </>
  );
}
