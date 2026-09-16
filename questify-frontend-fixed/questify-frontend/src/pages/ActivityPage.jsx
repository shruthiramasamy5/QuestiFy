import PageHeader from "../components/common/PageHeader.jsx";
import AuditEventsPanel from "../components/admin/AuditEventsPanel.jsx";

export default function ActivityPage() {
  return (
    <>
      <PageHeader
        title="Activity"
        subtitle="Recent activity in your institution"
      />

      <AuditEventsPanel />
    </>
  );
}