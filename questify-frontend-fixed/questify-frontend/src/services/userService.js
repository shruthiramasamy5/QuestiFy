import api from "./api.js";
import { ENDPOINTS } from "../config/api.js";

async function list() {
  const { data } = await api.get(ENDPOINTS.auth.users);
  return data;
}

async function create(payload) {
  const { data } = await api.post(ENDPOINTS.auth.users, payload);
  return data;
}

async function update(id, payload) {
  const { data } = await api.put(ENDPOINTS.auth.userById(id), payload);
  return data;
}

async function remove(id) {
  await api.delete(ENDPOINTS.auth.userById(id));
}

export default { list, create, update, remove };
