import api from "./api.js";
import { ENDPOINTS } from "../config/api.js";

async function stats() {
  const { data } = await api.get(ENDPOINTS.planUsage.stats);
  return data;
}

async function history(params) {
  const { data } = await api.get(ENDPOINTS.planUsage.history, { params });
  return data;
}

async function billingSummary() {
  const { data } = await api.get(ENDPOINTS.planUsage.billingSummary);
  return data;
}

export default { stats, history, billingSummary };
