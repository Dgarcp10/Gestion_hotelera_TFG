import { describe, it, expect, afterEach } from 'vitest'
import { render, screen, cleanup } from '@testing-library/react'
import '@testing-library/jest-dom/vitest'
import { MemoryRouter } from 'react-router-dom'
import Dashboard from '../../pages/Dashboard'
import { AuthProvider } from '../../contexts/AuthProvider'
afterEach(() => cleanup())
const renderDashboard = (user) => {
  if (user) {
    localStorage.setItem('token', 'tok')
    localStorage.setItem('user', JSON.stringify(user))
  }
  return render(
    <MemoryRouter>
      <AuthProvider>
        <Dashboard />
      </AuthProvider>
    </MemoryRouter>
  )
}
describe('Dashboard', () => {
  it('sin usuario muestra nada', () => {
    localStorage.clear()
    const { container } = renderDashboard(null)
    expect(container.innerHTML).toBe('')
  })
  it('rol JEFE muestra JefeNav', () => {
    renderDashboard({ username: 'jefe', rol: 'JEFE' })
    expect(screen.getByText('Dashboard')).toBeInTheDocument()
  })
  it('rol USUARIO muestra UserNav', () => {
    renderDashboard({ username: 'cli', rol: 'USUARIO' })
    expect(screen.getByText('Mis reservas')).toBeInTheDocument()
  })
  it('muestra nombre de usuario', () => {
    renderDashboard({ username: 'maria', rol: 'RECEPCION' })
    expect(screen.getByText('Bienvenido, maria')).toBeInTheDocument()
  })
})