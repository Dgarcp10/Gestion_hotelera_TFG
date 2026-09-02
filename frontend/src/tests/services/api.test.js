import { describe, it, expect, beforeEach } from 'vitest'
import api from '../../services/api'
describe('api service', () => {
  beforeEach(() => {
    localStorage.clear()
  })
  it('tiene baseURL por defecto localhost:8080', () => {
    expect(api.defaults.baseURL).toBe('http://localhost:8080')
  })
  it('interceptor añade Authorization con token', async () => {
    localStorage.setItem('token', 'tok123')
    const config = { headers: {} }
    const interceptor = api.interceptors.request.handlers[0]
    const result = interceptor.fulfilled(config)
    expect(result.headers.Authorization).toBe('Bearer tok123')
  })
  it('interceptor no añade Authorization sin token', async () => {
    const config = { headers: {} }
    const interceptor = api.interceptors.request.handlers[0]
    const result = interceptor.fulfilled(config)
    expect(result.headers.Authorization).toBeUndefined()
  })
})