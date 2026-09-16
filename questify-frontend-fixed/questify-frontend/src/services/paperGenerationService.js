import api from "./api.js";
import { ENDPOINTS } from "../config/api.js";

async function generate(payload) {
  const { data } = await api.post(ENDPOINTS.generate.create, payload);
  return data;
}

async function getById(id) {
  const { data } = await api.get(ENDPOINTS.generate.byId(id));
  return data;
}

async function list(params) {
  const { data } = await api.get(ENDPOINTS.generate.list, { params });
  return data;
}

export default { generate, getById, list };
