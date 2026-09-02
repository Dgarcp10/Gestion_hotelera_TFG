import { describe, it, expect, afterEach } from 'vitest'
import { render, screen, cleanup } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import '@testing-library/jest-dom/vitest'
import { MemoryRouter } from 'react-router-dom'
import PublicHeader from '../../components/PublicHeader'
import ProtectedHeader from '../../components/ProtectedHeader'
import { AuthProvider } from '../../contexts/AuthProvider'
afterEach(() => cleanup())
describe('PublicHeader', () => {
  it('muestra nombre del hotel', () => {
    render(<MemoryRouter><PublicHeader /></MemoryRouter>)
    expect(screen.getByAltText('Hotel Villa de Lerma')).toBeInTheDocument()
  })
  it('muestra links de login y register', () => {
    render(<MemoryRouter><PublicHeader /></MemoryRouter>)
    expect(screen.getByText('Iniciar sesión')).toBeInTheDocument()
    expect(screen.getByText('Registrarse')).toBeInTheDocument()
  })
})
describe('ProtectedHeader', () => {
  it('muestra username y botón logout', () => {
    localStorage.setItem('token', 'tok')
    localStorage.setItem('user', JSON.stringify({ username: 'ana', rol: 'JEFE' }))
    render(
      <MemoryRouter>
        <AuthProvider>
          <ProtectedHeader />
        </AuthProvider>
      </MemoryRouter>
    )
    expect(screen.getByText('ana')).toBeInTheDocument()
    expect(screen.getByText('Cerrar sesión')).toBeInTheDocument()
  })
  it('click en logout limpia sesión', async () => {
    localStorage.setItem('token', 'tok')
    localStorage.setItem('user', JSON.stringify({ username: 'luis', rol: 'USUARIO' }))
    render(
      <MemoryRouter>
        <AuthProvider>
          <ProtectedHeader />
        </AuthProvider>
      </MemoryRouter>
    )
    await userEvent.click(screen.getByText('Cerrar sesión'))
    expect(localStorage.getItem('token')).toBeNull()
  })
})