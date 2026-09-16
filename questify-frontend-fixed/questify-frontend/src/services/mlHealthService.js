import api from "./api.js";
import { ENDPOINTS } from "../config/api.js";

async function current() {
  const { data } = await api.get(ENDPOINTS.mlHealth.current);
  return data;
}

async function probe() {
  const { data } = await api.post(ENDPOINTS.mlHealth.probe);
  return data;
}

export default { current, probe };
