const FALLBACK = "Something went wrong. Please try again.";

export function toUserMessage(error, fallback = FALLBACK) {
  if (!error) return fallback;
  if (error.code === "ERR_NETWORK") return "Unable to reach the QuestiFy server. Check your connection and try again.";
  const status = error.response?.status;
  const backendMessage = error.response?.data?.message;
  if (typeof backendMessage === "string" && backendMessage.length < 200) return backendMessage;
  if (status === 401) return "Invalid email or password.";
  if (status === 403) return "You do not have permission to perform this action.";
  if (status === 404) return "The requested resource was not found.";
  if (status >= 500) return "The server is currently unavailable. Please try again shortly.";
  return fallback;
}
