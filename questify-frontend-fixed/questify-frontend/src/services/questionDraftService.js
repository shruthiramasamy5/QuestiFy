import api from "./api.js";
import { ENDPOINTS } from "../config/api.js";

async function generate(payload) {
  const { data } = await api.post(ENDPOINTS.questionDrafts.generate, payload);
  return data;
}

async function list(status) {
  const params = status ? { status } : undefined;
  const { data } = await api.get(ENDPOINTS.questionDrafts.list, { params });
  return data;
}

async function approve(id, overrides = {}) {
  const { data } = await api.post(ENDPOINTS.questionDrafts.approve(id), overrides);
  return data;
}

async function discard(id) {
  await api.post(ENDPOINTS.questionDrafts.discard(id));
}

export default { generate, list, approve, discard };
