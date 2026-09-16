import axios from "axios";
import { API_BASE_URL } from "../config/api.js";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { "Content-Type": "application/json" },
});

let authTokenProvider = null;

export function setAuthTokenProvider(provider) {
  authTokenProvider = provider;
}

export function getStoredToken() {
  if (authTokenProvider) {
    const t = authTokenProvider();
    if (t) return t;
  }
  try {
    const raw = window.localStorage.getItem("questify.session");
    if (raw) {
      const parsed = JSON.parse(raw);
      return parsed?.token || null;
    }
  } catch {}
  return null;
}

api.interceptors.request.use((config) => {
  const token = getStoredToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const config = error.config;
    if (error.code === "ERR_NETWORK" && config && !config._retry) {
      config._retry = true;
      const currentBase = config.baseURL || API_BASE_URL;
      const alternateBase = currentBase.includes("localhost")
        ? currentBase.replace("localhost", "127.0.0.1")
        : currentBase.replace("127.0.0.1", "localhost");

      console.warn(
        `[QuestiFy API] Network failure on ${currentBase}. Retrying via alternate host ${alternateBase}...`
      );
      config.baseURL = alternateBase;
      if (config.url && config.url.startsWith("http")) {
        config.url = config.url.includes("localhost")
          ? config.url.replace("localhost", "127.0.0.1")
          : config.url.replace("127.0.0.1", "localhost");
      }
      return api(config);
    }
    return Promise.reject(error);
  }
);

export default api;
