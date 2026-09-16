import api from "./api.js";
import { ENDPOINTS } from "../config/api.js";

async function search(params) {
  const { data } = await api.get(ENDPOINTS.questions.list, { params });
  return data;
}

async function getById(id) {
  const { data } = await api.get(ENDPOINTS.questions.byId(id));
  return data;
}

async function create(payload) {
  const { data } = await api.post(ENDPOINTS.questions.list, payload);
  return data;
}

async function update(id, payload) {
  const { data } = await api.put(ENDPOINTS.questions.byId(id), payload);
  return data;
}

async function remove(id) {
  await api.delete(ENDPOINTS.questions.byId(id));
}

async function bulkUpload(requests) {
  const { data } = await api.post(ENDPOINTS.questions.bulkUpload, requests);
  return data;
}

async function syllabusUpload(payload) {
  const { data } = await api.post(ENDPOINTS.questions.syllabusUpload, payload);
  return data;
}

export default { search, getById, create, update, remove, bulkUpload, syllabusUpload };
