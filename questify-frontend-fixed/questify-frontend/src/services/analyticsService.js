import api from "./api.js";
import { ENDPOINTS } from "../config/api.js";

async function overview(params) {
  const { data } = await api.get(ENDPOINTS.analytics.overview, { params });
  return data;
}

async function questions(params) {
  const { data } = await api.get(ENDPOINTS.analytics.questions, { params });
  return data;
}

async function papers(params) {
  const { data } = await api.get(ENDPOINTS.analytics.papers, { params });
  return data;
}

async function approvals(params) {
  const { data } = await api.get(ENDPOINTS.analytics.approvals, { params });
  return data;
}

export default { overview, questions, papers, approvals };
