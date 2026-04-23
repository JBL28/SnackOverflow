import 'server-only'
import axios from 'axios'

const BACKEND_URL = process.env.BACKEND_URL ?? 'http://localhost:8080'

export const serverApi = axios.create({
  baseURL: BACKEND_URL,
  timeout: 10_000,
})
