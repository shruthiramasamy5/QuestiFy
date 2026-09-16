import api from "./api.js";
import { ENDPOINTS } from "../config/api.js";

async function list(params) {
  const { data } = await api.get(ENDPOINTS.papers.list, { params });
  return data;
}

async function getById(id) {
  const { data } = await api.get(ENDPOINTS.papers.byId(id));
  return data;
}

async function create(payload) {
  const { data } = await api.post(ENDPOINTS.papers.create, payload);
  return data;
}

async function createFromDraft(payload) {
  const { data } = await api.post(ENDPOINTS.papers.fromDraft, payload);
  return data;
}

async function updateEvaluation(id, payload) {
  const { data } = await api.put(ENDPOINTS.papers.evaluation(id), payload);
  return data;
}

export default { list, getById, create, createFromDraft, updateEvaluation };
