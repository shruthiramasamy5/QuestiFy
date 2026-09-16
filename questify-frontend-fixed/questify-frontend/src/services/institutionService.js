import api from "./api.js";
import { ENDPOINTS } from "../config/api.js";

async function list(params) {
  const { data } = await api.get(ENDPOINTS.institutions.list, { params });
  return data;
}

async function me() {
  const { data } = await api.get(ENDPOINTS.institutions.me);
  return data;
}

async function getById(id) {
  const { data } = await api.get(ENDPOINTS.institutions.byId(id));
  return data;
}

async function create(payload) {
  const { data } = await api.post(ENDPOINTS.institutions.list, payload);
  return data;
}

async function update(id, payload) {
  const { data } = await api.put(ENDPOINTS.institutions.byId(id), payload);
  return data;
}

async function updateStatus(id, status) {
  const { data } = await api.patch(ENDPOINTS.institutions.status(id), { status });
  return data;
}

async function remove(id) {
  await api.delete(ENDPOINTS.institutions.byId(id));
}

export default { list, me, getById, create, update, updateStatus, remove };
