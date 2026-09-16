export default function LoadingState({ message = "Loading\u2026" }) {
  return (
    <div className="state-block" role="status" aria-live="polite">
      <div className="spinner" />
      <p className="state-msg">{message}</p>
    </div>
  );
}
