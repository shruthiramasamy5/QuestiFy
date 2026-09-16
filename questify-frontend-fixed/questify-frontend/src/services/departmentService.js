import api from "./api.js";
import { ENDPOINTS } from "../config/api.js";

async function list(params) {
  const { data } = await api.get(ENDPOINTS.departments.list, { params });
  return data;
}

async function active() {
  const { data } = await api.get(ENDPOINTS.departments.active);
  return data;
}

async function getById(id) {
  const { data } = await api.get(ENDPOINTS.departments.byId(id));
  return data;
}

async function create(payload) {
  const { data } = await api.post(ENDPOINTS.departments.list, payload);
  return data;
}

async function update(id, payload) {
  const { data } = await api.put(ENDPOINTS.departments.byId(id), payload);
  return data;
}

async function remove(id) {
  await api.delete(ENDPOINTS.departments.byId(id));
}

export default { list, active, getById, create, update, remove };
