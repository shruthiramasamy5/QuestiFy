import PageHeader from "../components/common/PageHeader.jsx";
import AuditEventsPanel from "../components/admin/AuditEventsPanel.jsx";

export default function AuditLogPage() {
  return (
    <>
      <PageHeader title="Platform audit log" subtitle="Every recorded action across all institutions" />
      <AuditEventsPanel />
    </>
  );
}
