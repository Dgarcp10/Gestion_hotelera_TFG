import { describe, it, expect, beforeEach, vi } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { AuthProvider } from '../../contexts/AuthProvider'
import { useAuth } from '../../contexts/useAuth'
import api from '../../services/api'
vi.mock('../../services/api')
const wrapper = ({ children }) => <AuthProvider>{children}</AuthProvider>
describe('AuthProvider', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })
  it('estado inicial sin localStorage', () => {
    const { result } = renderHook(() => useAuth(), { wrapper })
    expect(result.current.user).toBeNull()
    expect(result.current.token).toBeNull()
  })
  it('restaura estado desde localStorage', () => {
    localStorage.setItem('token', 'tok123')
    localStorage.setItem('user', JSON.stringify({ username: 'ana', rol: 'JEFE' }))
    const { result } = renderHook(() => useAuth(), { wrapper })
    expect(result.current.token).toBe('tok123')
    expect(result.current.user.username).toBe('ana')
  })
  it('login guarda token y user', async () => {
    api.post.mockResolvedValue({ data: { token: 'tok456', username: 'luis', rol: 'USUARIO' } })
    const { result } = renderHook(() => useAuth(), { wrapper })
    await act(() => result.current.login('luis', 'pass123'))
    expect(result.current.token).toBe('tok456')
    expect(result.current.user.username).toBe('luis')
    expect(localStorage.getItem('token')).toBe('tok456')
  })
  it('login fallido lanza error', async () => {
    api.post.mockRejectedValue(new Error('Credenciales inválidas'))
    const { result } = renderHook(() => useAuth(), { wrapper })
    await expect(result.current.login('malo', 'mal'))
      .rejects.toThrow('Credenciales inválidas')
  })
  it('logout limpia estado y localStorage', async () => {
    localStorage.setItem('token', 'tok')
    localStorage.setItem('user', JSON.stringify({ username: 'x' }))
    const { result } = renderHook(() => useAuth(), { wrapper })
    act(() => result.current.logout())
    expect(result.current.token).toBeNull()
    expect(result.current.user).toBeNull()
    expect(localStorage.getItem('token')).toBeNull()
  })
  it('setAuth guarda datos', () => {
    const { result } = renderHook(() => useAuth(), { wrapper })
    act(() => result.current.setAuth('newtok', { username: 'maria', rol: 'JEFE' }))
    expect(result.current.token).toBe('newtok')
    expect(result.current.user.username).toBe('maria')
    expect(localStorage.getItem('token')).toBe('newtok')
  })
})