import { useEffect, useState } from "react";
import PageHeader from "../components/common/PageHeader.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import LoadingState from "../components/common/LoadingState.jsx";
import ErrorState from "../components/common/ErrorState.jsx";
import departmentService from "../services/departmentService.js";
import userService from "../services/userService.js";

const EMPTY = {
  name: "",
  email: "",
  password: "Questify@123",
  department: "Computer Science",
  role: "FACULTY",
  active: true,
};

export default function FacultyPage() {
  const [faculty, setFaculty] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY);
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState(null);

  async function loadData() {
    setLoading(true);
    setError(null);
    try {
      const [usersData, deptsData] = await Promise.allSettled([
        userService.list(),
        departmentService.active(),
      ]);

      const usersList = usersData.status === "fulfilled" && Array.isArray(usersData.value)
        ? usersData.value
        : [];
      const deptsList = deptsData.status === "fulfilled" && Array.isArray(deptsData.value)
        ? deptsData.value
        : [];

      setDepartments(deptsList);
      setFaculty(usersList);
    } catch (err) {
      setError(err?.response?.data?.message || "Could not load faculty members.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, []);

  function openCreate() {
    setEditingId(null);
    setForm(EMPTY);
    setSaveError(null);
    setFormOpen(true);
  }

  function openEdit(member) {
    setEditingId(member.id);
    setForm({
      name: member.name,
      email: member.email,
      password: "",
      department: member.institutionName || "Computer Science",
      role: member.role || "FACULTY",
      active: member.active !== false,
    });
    setSaveError(null);
    setFormOpen(true);
  }

  async function submit(e) {
    e.preventDefault();
    setSaving(true);
    setSaveError(null);

    try {
      if (editingId) {
        await userService.update(editingId, {
          name: form.name,
          email: form.email,
          role: form.role,
          password: form.password || undefined,
        });
      } else {
        await userService.create({
          name: form.name,
          email: form.email,
          password: form.password || "Questify@123",
          role: form.role,
        });
      }

      setForm(EMPTY);
      setEditingId(null);
      setFormOpen(false);
      await loadData();
    } catch (err) {
      setSaveError(err?.response?.data?.message || "Could not save faculty member.");
    } finally {
      setSaving(false);
    }
  }

  async function remove(id) {
    if (!window.confirm("Are you sure you want to deactivate/delete this user?")) return;
    try {
      await userService.remove(id);
      await loadData();
    } catch (err) {
      setError(err?.response?.data?.message || "Could not remove user.");
    }
  }

  return (
    <>
      <PageHeader
        title="Faculty"
        subtitle="Manage faculty members in your institution"
        actions={
          <button
            type="button"
            className="ws-btn primary"
            onClick={openCreate}
          >
            + New faculty
          </button>
        }
      />

      <section className="panel">
        {formOpen && (
          <form onSubmit={submit}>
            <div className="form-grid">
              <div className="field">
                <label>Name</label>
                <input
                  required
                  value={form.name}
                  onChange={(e) =>
                    setForm((f) => ({
                      ...f,
                      name: e.target.value,
                    }))
                  }
                  placeholder="Faculty name"
                />
              </div>

              <div className="field">
                <label>Email</label>
                <input
                  required
                  type="email"
                  value={form.email}
                  onChange={(e) =>
                    setForm((f) => ({
                      ...f,
                      email: e.target.value,
                    }))
                  }
                  placeholder="faculty@college.edu"
                />
              </div>

              <div className="field">
                <label>Department</label>
                {departments.length > 0 ? (
                  <select
                    required
                    value={form.department}
                    onChange={(e) =>
                      setForm((f) => ({
                        ...f,
                        department: e.target.value,
                      }))
                    }
                  >
                    <option value="">Select Department</option>
                    {departments.map((d) => (
                      <option key={d.id} value={d.name}>
                        {d.name} ({d.code})
                      </option>
                    ))}
                  </select>
                ) : (
                  <input
                    required
                    value={form.department}
                    onChange={(e) =>
                      setForm((f) => ({
                        ...f,
                        department: e.target.value,
                      }))
                    }
                    placeholder="Computer Science"
                  />
                )}
              </div>

              <div className="field">
                <label>Role</label>
                <select
                  value={form.role}
                  onChange={(e) =>
                    setForm((f) => ({
                      ...f,
                      role: e.target.value,
                    }))
                  }
                >
                  <option value="FACULTY">Faculty</option>
                  <option value="COURSE_COORDINATOR">
                    Course Coordinator
                  </option>
                  <option value="HOD">HOD</option>
                </select>
              </div>

              <div className="field">
                <label
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: 8,
                    textTransform: "none",
                    fontSize: 13,
                  }}
                >
                  <input
                    type="checkbox"
                    checked={form.active}
                    onChange={(e) =>
                      setForm((f) => ({
                        ...f,
                        active: e.target.checked,
                      }))
                    }
                  />
                  Active
                </label>
              </div>
            </div>

            {saveError && <p className="inline-error">{saveError}</p>}

            <div className="form-actions">
              <button
                type="submit"
                className="ws-btn primary"
                disabled={saving}
              >
                {saving ? "Saving…" : editingId ? "Save changes" : "Create faculty"}
              </button>

              <button
                type="button"
                className="ws-btn ghost"
                onClick={() => {
                  setFormOpen(false);
                  setEditingId(null);
                  setForm(EMPTY);
                }}
              >
                Cancel
              </button>
            </div>
          </form>
        )}

        {loading ? (
          <LoadingState message="Loading faculty and departments\u2026" />
        ) : error ? (
          <ErrorState message={error} onRetry={loadData} />
        ) : faculty.length === 0 ? (
          <EmptyState
            glyph="◉"
            title="No faculty members yet"
            message="Add your first faculty member to get started."
          />
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Department</th>
                <th>Role</th>
                <th>Status</th>
                <th />
              </tr>
            </thead>

            <tbody>
              {faculty.map((member) => (
                <tr key={member.id}>
                  <td>{member.name}</td>

                  <td>{member.email}</td>

                  <td>{member.department}</td>

                  <td>{member.role.replaceAll("_", " ")}</td>

                  <td>
                    <span
                      className={`badge ${
                        member.active
                          ? "badge-good"
                          : "badge-neutral"
                      }`}
                    >
                      {member.active ? "Active" : "Inactive"}
                    </span>
                  </td>

                  <td>
                    <div className="row-actions">
                      <button
                        type="button"
                        className="ws-btn ghost"
                        onClick={() => openEdit(member)}
                      >
                        Edit
                      </button>

                      <button
                        type="button"
                        className="ws-btn ghost"
                        onClick={() => remove(member.id)}
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </>
  );
}