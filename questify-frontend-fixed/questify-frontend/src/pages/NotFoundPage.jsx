import { Link } from "react-router-dom";

export default function NotFoundPage() {
  return (
    <div className="notfound">
      <div>
        <h1>404</h1>
        <p>The page you&rsquo;re looking for doesn&rsquo;t exist or has been moved.</p>
        <Link to="/" className="btn btn-primary">Go home</Link>
      </div>
    </div>
  );
}
