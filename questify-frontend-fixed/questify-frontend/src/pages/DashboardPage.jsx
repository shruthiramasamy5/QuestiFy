import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import PageHeader from "../components/common/PageHeader.jsx";
import LoadingState from "../components/common/LoadingState.jsx";
import ErrorState from "../components/common/ErrorState.jsx";
import EmptyState from "../components/common/EmptyState.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import { ROLE_LABELS, normalizeRole } from "../utils/roles.js";
import questionBankService from "../services/questionBankService.js";
import papersService from "../services/papersService.js";
import approvalService from "../services/approvalService.js";
import analyticsService from "../services/analyticsService.js";
import departmentService from "../services/departmentService.js";
import templateService from "../services/templateService.js";
import planUsageService from "../services/planUsageService.js";
import auditLogService from "../services/auditLogService.js";
import institutionService from "../services/institutionService.js";
import mlHealthService from "../services/mlHealthService.js";
import { badgeClassFor, titleCase } from "../utils/options.js";

export default function DashboardPage() {
  const { user } = useAuth();
  const role = normalizeRole(user?.role);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [metrics, setMetrics] = useState({});
  const [recentItems, setRecentItems] = useState([]);

  async function loadDashboard() {
    setLoading(true);
    setError(null);
    try {
      if (role === "SUPER_ADMIN") {
        const [instRes, mlRes, auditRes] = await Promise.allSettled([
          institutionService.list(),
          mlHealthService.current(),
          auditLogService.search({ size: 5 }),
        ]);

        const instList = instRes.status === "fulfilled" ? (instRes.value?.content || instRes.value || []) : [];
        const mlData = mlRes.status === "fulfilled" ? mlRes.value : null;
        const auditList = auditRes.status === "fulfilled" ? (auditRes.value?.content || auditRes.value || []) : [];

        setMetrics({
          totalInstitutions: instList.length,
          mlStatus: mlData?.status || "UP",
          mlLatency: mlData?.latencyMs != null ? `${mlData.latencyMs} ms` : "\u2014",
          auditEventsCount: auditList.length,
        });

        setRecentItems(
          auditList.map((evt) => ({
            id: evt.id,
            title: `${evt.action || "Action"} &middot; ${evt.resourceType || "System"}`,
            sub: `${evt.actorEmail || evt.actor || "System"} &middot; ${new Date(evt.timestamp || Date.now()).toLocaleString()}`,
            badge: evt.status || "LOGGED",
            link: "/admin/audit-logs",
          }))
        );
      } else if (role === "INSTITUTION_ADMIN") {
        const [deptRes, tplRes, planRes, auditRes] = await Promise.allSettled([
          departmentService.active(),
          templateService.list(),
          planUsageService.stats(),
          auditLogService.search({ size: 5 }),
        ]);

        const depts = deptRes.status === "fulfilled" ? (deptRes.value || []) : [];
        const templates = tplRes.status === "fulfilled" ? (tplRes.value?.content || tplRes.value || []) : [];
        const plan = planRes.status === "fulfilled" ? planRes.value : null;
        const auditList = auditRes.status === "fulfilled" ? (auditRes.value?.content || auditRes.value || []) : [];

        setMetrics({
          activeDepartments: Array.isArray(depts) ? depts.length : 0,
          templatesCount: Array.isArray(templates) ? templates.length : 0,
          papersGenerated: plan?.papersGenerated || 0,
          papersQuota: plan?.quotaLimit || 100,
        });

        setRecentItems(
          auditList.map((evt) => ({
            id: evt.id,
            title: `${evt.action || "Action"} &middot; ${evt.resourceType || "Resource"}`,
            sub: `${evt.actorEmail || evt.actor || "User"} &middot; ${new Date(evt.timestamp || Date.now()).toLocaleString()}`,
            badge: evt.status || "SUCCESS",
            link: "/institution/activity",
          }))
        );
      } else {
        // FACULTY, COURSE_COORDINATOR, HOD, REVIEWER
        const [qRes, pRes, appRes, analyticsRes] = await Promise.allSettled([
          questionBankService.search({ size: 1 }),
          papersService.list(),
          approvalService.list(),
          analyticsService.overview().catch(() => null),
        ]);

        const qData = qRes.status === "fulfilled" ? qRes.value : { totalElements: 0 };
        const pList = pRes.status === "fulfilled" ? (Array.isArray(pRes.value) ? pRes.value : []) : [];
        const appList = appRes.status === "fulfilled" ? (Array.isArray(appRes.value) ? appRes.value : []) : [];
        const analytics = analyticsRes.status === "fulfilled" ? analyticsRes.value : null;

        const pendingApps = appList.filter((a) => a.status === "PENDING" || a.status === "STAGE_1_PENDING" || a.status === "STAGE_2_PENDING");

        setMetrics({
          totalQuestions: qData?.totalElements || 0,
          totalPapers: pList.length,
          pendingApprovals: pendingApps.length,
          avgTurnaround: analytics?.approvals?.averageTurnaroundHours != null ? `${analytics.approvals.averageTurnaroundHours}h` : "\u2014",
        });

        // Combined recent items
        const combined = [];
        pList.slice(0, 4).forEach((p) => {
          combined.push({
            id: `paper-${p.id}`,
            title: `Paper: ${p.title || p.subjectCode || "Question Paper"}`,
            sub: `${p.subjectCode || ""} &middot; ${p.totalMarks || 100} Marks &middot; Created by ${p.createdBy || "Faculty"}`,
            badge: p.status || "DRAFT",
            link: "/papers",
          });
        });
        appList.slice(0, 3).forEach((a) => {
          combined.push({
            id: `app-${a.id}`,
            title: `Approval Request: ${a.paperTitle || a.paperId || "Exam Paper"}`,
            sub: `Stage: ${a.currentStage || "1"} &middot; ${new Date(a.createdAt || Date.now()).toLocaleDateString()}`,
            badge: a.status || "PENDING",
            link: "/approval-queue",
          });
        });

        setRecentItems(combined.slice(0, 5));
      }
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to load dashboard metrics.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadDashboard();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [role]);

  return (
    <>
      <PageHeader
        title="Dashboard"
        subtitle={`${ROLE_LABELS[user?.role] || "Workspace"} overview${user?.institutionName ? ` \u00B7 ${user.institutionName}` : ""}${user?.department ? ` \u00B7 ${user.department}` : ""}`}
        actions={
          <button type="button" className="ws-btn ghost" onClick={loadDashboard}>
            ↻ Refresh
          </button>
        }
      />

      {loading ? (
        <LoadingState message="Loading dashboard metrics\u2026" />
      ) : error ? (
        <ErrorState message={error} onRetry={loadDashboard} />
      ) : (
        <>
          <div className="stat-grid" style={{ marginBottom: "1.5rem" }}>
            {role === "SUPER_ADMIN" ? (
              <>
                <div className="stat-cell">
                  <div className="num">{metrics.totalInstitutions ?? 0}</div>
                  <div className="label">Total institutions</div>
                </div>
                <div className="stat-cell">
                  <div className="num">
                    <span className={badgeClassFor(metrics.mlStatus)}>{metrics.mlStatus}</span>
                  </div>
                  <div className="label">ML model service</div>
                </div>
                <div className="stat-cell">
                  <div className="num">{metrics.mlLatency}</div>
                  <div className="label">ML response latency</div>
                </div>
                <div className="stat-cell">
                  <div className="num">{metrics.auditEventsCount ?? 0}</div>
                  <div className="label">Recent audit events</div>
                </div>
              </>
            ) : role === "INSTITUTION_ADMIN" ? (
              <>
                <div className="stat-cell">
                  <div className="num">{metrics.activeDepartments ?? 0}</div>
                  <div className="label">Active departments</div>
                </div>
                <div className="stat-cell">
                  <div className="num">{metrics.templatesCount ?? 0}</div>
                  <div className="label">Paper templates</div>
                </div>
                <div className="stat-cell">
                  <div className="num">{metrics.papersGenerated ?? 0}</div>
                  <div className="label">Papers generated</div>
                </div>
                <div className="stat-cell">
                  <div className="num">{metrics.papersQuota ?? 100}</div>
                  <div className="label">Monthly quota</div>
                </div>
              </>
            ) : (
              <>
                <div className="stat-cell">
                  <div className="num">{metrics.totalQuestions ?? 0}</div>
                  <div className="label">Question bank items</div>
                </div>
                <div className="stat-cell">
                  <div className="num">{metrics.totalPapers ?? 0}</div>
                  <div className="label">Generated / saved papers</div>
                </div>
                <div className="stat-cell">
                  <div className="num">{metrics.pendingApprovals ?? 0}</div>
                  <div className="label">Pending approvals</div>
                </div>
                <div className="stat-cell">
                  <div className="num">{metrics.avgTurnaround ?? "\u2014"}</div>
                  <div className="label">Avg approval turnaround</div>
                </div>
              </>
            )}
          </div>

          <section className="panel">
            <div className="panel-head">
              <div>
                <div className="panel-title">Recent activity</div>
                <div className="panel-sub">Latest events, papers, and cycle updates</div>
              </div>
            </div>
            {recentItems.length === 0 ? (
              <EmptyState
                glyph={"\u25A1"}
                title="No recent activity"
                message="Activity will populate as you create questions, generate papers, and review submissions."
              />
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Item</th>
                    <th>Details</th>
                    <th>Status</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {recentItems.map((item) => (
                    <tr key={item.id}>
                      <td style={{ fontWeight: 500 }} dangerouslySetInnerHTML={{ __html: item.title }} />
                      <td style={{ color: "var(--qf-muted, #666)", fontSize: "13px" }} dangerouslySetInnerHTML={{ __html: item.sub }} />
                      <td>
                        <span className={badgeClassFor(item.badge)}>{titleCase(item.badge)}</span>
                      </td>
                      <td>
                        <div className="row-actions">
                          {item.link && (
                            <Link to={item.link} className="ws-btn ghost" style={{ textDecoration: "none" }}>
                              View &rarr;
                            </Link>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </section>
        </>
      )}
    </>
  );
}

