import api from "./api.js";
import { ENDPOINTS } from "../config/api.js";

async function list(params) {
  const { data } = await api.get(ENDPOINTS.templates.list, { params });
  return data;
}

async function getById(id) {
  const { data } = await api.get(ENDPOINTS.templates.byId(id));
  return data;
}

async function create(payload) {
  const { data } = await api.post(ENDPOINTS.templates.list, payload);
  return data;
}

async function update(id, payload) {
  const { data } = await api.put(ENDPOINTS.templates.byId(id), payload);
  return data;
}

async function remove(id) {
  await api.delete(ENDPOINTS.templates.byId(id));
}

export default { list, getById, create, update, remove };
