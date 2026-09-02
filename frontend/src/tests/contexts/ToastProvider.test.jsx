import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { renderHook, act, cleanup } from '@testing-library/react'
import { ToastProvider } from '../../contexts/ToastProvider'
import { useToast } from '../../contexts/useToast'
const wrapper = ({ children }) => <ToastProvider>{children}</ToastProvider>
describe('ToastProvider', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })
  afterEach(() => {
    cleanup()
    vi.useRealTimers()
  })
  it('success muestra toast', () => {
    const { result } = renderHook(() => useToast(), { wrapper })
    act(() => result.current.success('Hecho'))
    expect(document.querySelector('.toast-success').textContent).toBe('Hecho')
  })
  it('error muestra toast', () => {
    const { result } = renderHook(() => useToast(), { wrapper })
    act(() => result.current.error('Fallo'))
    expect(document.querySelector('.toast-error').textContent).toBe('Fallo')
  })
  it('click descarta el toast', () => {
    const { result } = renderHook(() => useToast(), { wrapper })
    act(() => result.current.success('Borrar'))
    const toast = document.querySelector('.toast-success')
    act(() => { toast.click() })
    expect(document.querySelector('.toast-success')).toBeNull()
  })
  it('auto-dismiss después de 4 segundos', () => {
    const { result } = renderHook(() => useToast(), { wrapper })
    act(() => result.current.success('Auto'))
    expect(document.querySelector('.toast-success')).not.toBeNull()
    act(() => vi.advanceTimersByTime(4000))
    expect(document.querySelector('.toast-success')).toBeNull()
  })
  it('múltiples toasts se apilan', () => {
    const { result } = renderHook(() => useToast(), { wrapper })
    act(() => {
      result.current.success('Primero')
      result.current.error('Segundo')
    })
    expect(document.querySelectorAll('.toast').length).toBe(2)
  })
})