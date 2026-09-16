import api from "./api.js";
import { ENDPOINTS } from "../config/api.js";

async function login(credentials) {
  const { data } = await api.post(ENDPOINTS.auth.login, credentials);
  return data;
}

async function logout() {
  await api.post(ENDPOINTS.auth.logout);
}

async function getCurrentUser() {
  const { data } = await api.get(ENDPOINTS.auth.currentUser);
  return data;
}

export default { login, logout, getCurrentUser };
