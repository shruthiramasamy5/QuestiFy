import api from "./api.js";
import { ENDPOINTS } from "../config/api.js";

async function list(status) {
  const params = status ? { status } : undefined;
  const { data } = await api.get(ENDPOINTS.approvals.list, { params });
  return data;
}

async function getById(id) {
  const { data } = await api.get(ENDPOINTS.approvals.byId(id));
  return data;
}

async function create(payload) {
  const { data } = await api.post(ENDPOINTS.approvals.create, payload);
  return data;
}

async function approve(id, comment) {
  const { data } = await api.post(ENDPOINTS.approvals.approve(id), { comment, comments: comment });
  return data;
}

async function reject(id, comment) {
  const { data } = await api.post(ENDPOINTS.approvals.reject(id), { comment, comments: comment });
  return data;
}

export default { list, getById, create, approve, reject };
