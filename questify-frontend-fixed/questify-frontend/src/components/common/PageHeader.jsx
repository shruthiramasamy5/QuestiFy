export default function PageHeader({ title, subtitle, actions }) {
  return (
    <div className="page-head" style={{ display: "flex", justifyContent: "space-between", gap: 14, flexWrap: "wrap" }}>
      <div>
        <h1 className="page-title">{title}</h1>
        {subtitle ? <p className="page-sub">{subtitle}</p> : null}
      </div>
      {actions ? <div style={{ display: "flex", gap: 8 }}>{actions}</div> : null}
    </div>
  );
}
