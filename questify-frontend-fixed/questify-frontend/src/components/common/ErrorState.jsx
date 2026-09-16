export default function ErrorState({ title = "Something went wrong", message, onRetry }) {
  return (
    <div className="state-block" role="alert">
      <div className="big">!</div>
      <p className="state-title">{title}</p>
      {message ? <p className="state-msg">{message}</p> : null}
      {onRetry ? (
        <button type="button" className="ws-btn ghost" onClick={onRetry} style={{ marginTop: 12 }}>
          Try again
        </button>
      ) : null}
    </div>
  );
}
