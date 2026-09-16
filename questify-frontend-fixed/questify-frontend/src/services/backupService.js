import api from "./api.js";
import { ENDPOINTS } from "../config/api.js";

async function list(params) {
  const { data } = await api.get(ENDPOINTS.backups.list, { params });
  return data;
}

async function getById(id) {
  const { data } = await api.get(ENDPOINTS.backups.byId(id));
  return data;
}

async function create(payload) {
  const { data } = await api.post(ENDPOINTS.backups.create, payload);
  return data;
}

export default { list, getById, create };
