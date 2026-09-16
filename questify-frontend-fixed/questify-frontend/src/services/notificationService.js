// Notification service managing role-specific and workflow notifications

const STORAGE_KEY = "questify_notifications";

const DEFAULT_NOTIFICATIONS = {
  SUPER_ADMIN: [
    { id: "n1", title: "New institution onboarded", message: "Demo Institute of Technology completed setup.", time: "10 mins ago", unread: true, link: "/institutions" },
    { id: "n2", title: "ML Health Check OK", message: "Gemini / fallback models are operating with 120ms latency.", time: "1 hour ago", unread: false, link: "/ml-health" },
    { id: "n3", title: "Audit Event Recorded", message: "Admin Priya modified department CSE settings.", time: "Yesterday", unread: false, link: "/audit-log" }
  ],
  INSTITUTION_ADMIN: [
    { id: "n4", title: "New Faculty Added", message: "Dr. Rahul Faculty assigned to CSE department.", time: "15 mins ago", unread: true, link: "/faculty" },
    { id: "n5", title: "Monthly Quota Update", message: "42 of 500 papers generated this cycle.", time: "2 hours ago", unread: true, link: "/plan" },
    { id: "n6", title: "New Paper Template Created", message: "Continuous Internal Assessment (CIA) template is active.", time: "Yesterday", unread: false, link: "/templates" }
  ],
  FACULTY: [
    { id: "n7", title: "Paper Approved", message: "DBMS — CIA 1 was approved by HOD and Reviewer.", time: "25 mins ago", unread: true, link: "/papers" },
    { id: "n8", title: "Draft Questions Ready", message: "5 candidate questions from syllabus are pending your review.", time: "2 hours ago", unread: true, link: "/question-bank" },
    { id: "n9", title: "Question Bank Updated", message: "20 verified questions indexed for CS301.", time: "Yesterday", unread: false, link: "/question-bank" }
  ],
  COURSE_COORDINATOR: [
    { id: "n10", title: "Section Contributions Received", message: "Section B submitted 4 new questions for DBMS.", time: "30 mins ago", unread: true, link: "/question-bank" },
    { id: "n11", title: "Section Overlap Flagged", message: "2 questions in Unit 2 have >85% similarity.", time: "3 hours ago", unread: true, link: "/question-bank" },
    { id: "n12", title: "Course Paper Ready", message: "Set A and Set B repetition check passed.", time: "Yesterday", unread: false, link: "/generate-paper" }
  ],
  HOD: [
    { id: "n13", title: "Approval Requested", message: "Dr. Rahul Faculty submitted DBMS Mid-Sem for review.", time: "10 mins ago", unread: true, link: "/approval-queue" },
    { id: "n14", title: "Department Attainment", message: "CO3 attainment reached 88% in CSE.", time: "1 day ago", unread: false, link: "/analytics" },
    { id: "n15", title: "Faculty Activity Report", message: "All 5 faculty members submitted semester question banks.", time: "2 days ago", unread: false, link: "/faculty" }
  ],
  REVIEWER: [
    { id: "n16", title: "Final Review Required", message: "HOD approved DBMS CIA-1, awaiting final sign-off.", time: "20 mins ago", unread: true, link: "/approval-queue" },
    { id: "n17", title: "Paper Approved & Exported", message: "Operating Systems — End Sem set archived.", time: "Yesterday", unread: false, link: "/approved" },
    { id: "n18", title: "Backup Set Verified", message: "Backup Set B validated against repetition window.", time: "3 days ago", unread: false, link: "/backups" }
  ]
};

function getStored(role) {
  try {
    const raw = localStorage.getItem(`${STORAGE_KEY}_${role}`);
    if (raw) return JSON.parse(raw);
  } catch (e) {}
  const list = DEFAULT_NOTIFICATIONS[role] || DEFAULT_NOTIFICATIONS.FACULTY;
  try {
    localStorage.setItem(`${STORAGE_KEY}_${role}`, JSON.stringify(list));
  } catch (e) {}
  return list;
}

function saveStored(role, items) {
  try {
    localStorage.setItem(`${STORAGE_KEY}_${role}`, JSON.stringify(items));
  } catch (e) {}
}

export function getNotifications(role = "FACULTY") {
  const r = String(role).toUpperCase().replace(/-/g, "_");
  return getStored(r);
}

export function markAsRead(role, id) {
  const r = String(role).toUpperCase().replace(/-/g, "_");
  const list = getStored(r).map((n) => (n.id === id ? { ...n, unread: false } : n));
  saveStored(r, list);
  return list;
}

export function markAllAsRead(role) {
  const r = String(role).toUpperCase().replace(/-/g, "_");
  const list = getStored(r).map((n) => ({ ...n, unread: false }));
  saveStored(r, list);
  return list;
}

export function addNotification(role, notif) {
  const r = String(role).toUpperCase().replace(/-/g, "_");
  const list = [
    {
      id: `n_${Date.now()}`,
      time: "Just now",
      unread: true,
      ...notif,
    },
    ...getStored(r),
  ];
  saveStored(r, list);
  return list;
}

export default {
  getNotifications,
  markAsRead,
  markAllAsRead,
  addNotification,
};
