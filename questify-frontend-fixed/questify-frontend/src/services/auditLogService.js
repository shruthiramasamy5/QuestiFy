import api from "./api.js";
import { ENDPOINTS } from "../config/api.js";

async function search(params) {
  const { data } = await api.get(ENDPOINTS.auditEvents.list, { params });
  return data;
}

async function record(payload) {
  const { data } = await api.post(ENDPOINTS.auditEvents.create, payload);
  return data;
}

export default { search, record };
