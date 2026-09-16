import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ROLE_LABELS, initialsOf } from "../../utils/roles.js";
import { getNotifications, markAllAsRead, markAsRead } from "../../services/notificationService.js";

export default function Topbar({ user, onToggleSidebar, onSignOut, actingAsFaculty, onExitFacultyMode }) {
  const [menuOpen, setMenuOpen] = useState(false);
  const [notifOpen, setNotifOpen] = useState(false);
  const [theme, setTheme] = useState(() => localStorage.getItem("questify_theme") || "light");
  const [notifications, setNotifications] = useState([]);
  const wrapRef = useRef(null);
  const notifRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    document.body.setAttribute("data-theme", theme);
    localStorage.setItem("questify_theme", theme);
  }, [theme]);

  useEffect(() => {
    if (user?.role) {
      setNotifications(getNotifications(user.role));
    }
  }, [user?.role]);

  useEffect(() => {
    function onDocumentClick(event) {
      if (wrapRef.current && !wrapRef.current.contains(event.target)) setMenuOpen(false);
      if (notifRef.current && !notifRef.current.contains(event.target)) setNotifOpen(false);
    }
    document.addEventListener("click", onDocumentClick);
    return () => document.removeEventListener("click", onDocumentClick);
  }, []);

  function toggleTheme() {
    setTheme((prev) => (prev === "dark" ? "light" : "dark"));
  }

  function handleMarkAllRead() {
    if (user?.role) {
      setNotifications(markAllAsRead(user.role));
    }
  }

  function handleNotificationClick(item) {
    if (user?.role) {
      setNotifications(markAsRead(user.role, item.id));
    }
    setNotifOpen(false);
    if (item.link) {
      navigate(item.link);
    }
  }

  const unreadCount = notifications.filter((n) => n.unread).length;

  return (
    <>
      {actingAsFaculty ? (
        <div className="acting-as-banner">
          <span>Acting as Faculty</span>
          <button type="button" onClick={onExitFacultyMode}>
            Return to HOD view
          </button>
        </div>
      ) : null}
      <header className="topbar">
        <div style={{ display: "flex", gap: 10, alignItems: "center" }}>
          <button type="button" className="icon mobile-menu" onClick={onToggleSidebar} aria-label="Toggle navigation">
            &#9776;
          </button>
          <div className="scope">
            <small>
              {user?.institution || "QuestiFy"} &middot; {ROLE_LABELS[user?.role]}
            </small>
            <strong>{user?.department || user?.scope || "Workspace"}</strong>
          </div>
        </div>
        <div className="top-actions">
          {/* Theme Toggle Button */}
          <button
            type="button"
            className="icon theme-toggle-btn"
            onClick={toggleTheme}
            title={theme === "dark" ? "Switch to Light Theme" : "Switch to Dark Theme"}
            aria-label="Toggle Theme"
          >
            {theme === "dark" ? (
              <svg width="17" height="17" viewBox="0 0 18 18" fill="none">
                <circle cx="9" cy="9" r="3.2" stroke="currentColor" strokeWidth="1.3" />
                <path
                  d="M9 1.5V3.3M9 14.7V16.5M16.5 9H14.7M3.3 9H1.5M14.3 3.7L13 5M5 13L3.7 14.3M14.3 14.3L13 13M5 5L3.7 3.7"
                  stroke="currentColor"
                  strokeWidth="1.3"
                  strokeLinecap="round"
                />
              </svg>
            ) : (
              <svg width="17" height="17" viewBox="0 0 18 18" fill="none">
                <path
                  d="M15 10.5C14 12.7 11.8 14 9.3 14C6 14 3.3 11.3 3.3 8C3.3 5.5 4.6 3.3 6.8 2.3C6.3 3.1 6 4.1 6 5.1C6 8.1 8.4 10.5 11.4 10.5C12.4 10.5 13.4 10.2 15 10.5Z"
                  stroke="currentColor"
                  strokeWidth="1.3"
                  strokeLinejoin="round"
                />
              </svg>
            )}
          </button>

          {/* Notifications Button & Dropdown */}
          <div style={{ position: "relative" }} ref={notifRef}>
            <button
              type="button"
              className="icon notif-btn"
              onClick={() => setNotifOpen((v) => !v)}
              aria-label="Notifications"
              title="Notifications"
            >
              <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
                <path d="M13.73 21a2 2 0 0 1-3.46 0" />
              </svg>
              {unreadCount > 0 && <span className="notif-badge">{unreadCount}</span>}
            </button>

            {notifOpen && (
              <div className="notif-dropdown" role="region" aria-label="Notifications Panel">
                <div className="notif-header">
                  <h4>Notifications</h4>
                  {unreadCount > 0 && (
                    <button type="button" className="notif-mark-btn" onClick={handleMarkAllRead}>
                      Mark all read
                    </button>
                  )}
                </div>
                <div className="notif-list">
                  {notifications.length === 0 ? (
                    <div className="notif-empty">No notifications</div>
                  ) : (
                    notifications.map((item) => (
                      <div
                        key={item.id}
                        className={`notif-item ${item.unread ? "unread" : ""}`}
                        onClick={() => handleNotificationClick(item)}
                      >
                        <div className="notif-item-head">
                          <strong>{item.title}</strong>
                          <span className="notif-time">{item.time}</span>
                        </div>
                        <p className="notif-msg">{item.message}</p>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}
          </div>

          {/* Avatar Menu */}
          <div style={{ position: "relative" }} ref={wrapRef}>
            <button
              type="button"
              className="avatar-btn"
              onClick={() => setMenuOpen((v) => !v)}
              aria-haspopup="menu"
              aria-expanded={menuOpen}
            >
              {initialsOf(user?.name)}
            </button>
            {menuOpen ? (
              <div className="avatar-menu" role="menu">
                <button
                  type="button"
                  onClick={() => {
                    setMenuOpen(false);
                    navigate("/profile");
                  }}
                >
                  View profile
                </button>
                <button
                  type="button"
                  className="danger"
                  onClick={() => {
                    setMenuOpen(false);
                    onSignOut();
                  }}
                >
                  Sign out
                </button>
              </div>
            ) : null}
          </div>
        </div>
      </header>
    </>
  );
}
