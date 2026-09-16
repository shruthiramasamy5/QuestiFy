export default function EmptyState({ glyph = "\u25A1", title, message, action }) {
  return (
    <div className="state-block">
      <div className="big">{glyph}</div>
      {title ? <p className="state-title">{title}</p> : null}
      {message ? <p className="state-msg">{message}</p> : null}
      {action}
    </div>
  );
}
