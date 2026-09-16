import PageHeader from "../components/common/PageHeader.jsx";
import EmptyState from "../components/common/EmptyState.jsx";

// Sidebar destinations that are scheduled for a later implementation phase.
// They render the real workspace shell so navigation never breaks.
export default function SectionPlaceholderPage({ title, subtitle, message }) {
  return (
    <>
      <PageHeader title={title} subtitle={subtitle} />
      <section className="panel">
        <EmptyState glyph={"\u2726"} title={`${title} is not implemented yet`} message={message} />
      </section>
    </>
  );
}
