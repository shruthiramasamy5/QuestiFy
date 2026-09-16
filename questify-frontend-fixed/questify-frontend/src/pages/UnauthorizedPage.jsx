import { Link } from "react-router-dom";

export default function UnauthorizedPage() {
  return (
    <div className="notfound">
      <div>
        <h1>403</h1>
        <p>Your role does not have access to this section of QuestiFy.</p>
        <Link to="/dashboard" className="btn btn-primary">Back to dashboard</Link>
      </div>
    </div>
  );
}
