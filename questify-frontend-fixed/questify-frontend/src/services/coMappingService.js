import api from "./api.js";
import { ENDPOINTS } from "../config/api.js";

async function listOutcomes(params) {
  const { data } = await api.get(ENDPOINTS.coMapping.courseOutcomes, { params });
  return data;
}

async function createOutcome(payload) {
  const { data } = await api.post(ENDPOINTS.coMapping.courseOutcomes, payload);
  return data;
}

async function updateOutcome(id, payload) {
  const { data } = await api.put(ENDPOINTS.coMapping.courseOutcomeById(id), payload);
  return data;
}

async function deleteOutcome(id) {
  await api.delete(ENDPOINTS.coMapping.courseOutcomeById(id));
}

async function getMappings(questionId) {
  const { data } = await api.get(ENDPOINTS.coMapping.forQuestion(questionId));
  return data;
}

async function map(payload) {
  const { data } = await api.post(ENDPOINTS.coMapping.map, payload);
  return data;
}

async function unmap(questionId, courseOutcomeId) {
  await api.delete(ENDPOINTS.coMapping.unmap(questionId, courseOutcomeId));
}

export default { listOutcomes, createOutcome, updateOutcome, deleteOutcome, getMappings, map, unmap };
