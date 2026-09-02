import { describe, it, expect, afterEach } from 'vitest'
import { render, screen, cleanup } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import '@testing-library/jest-dom/vitest'
import { MemoryRouter } from 'react-router-dom'
import Login from '../../pages/Login'
import { AuthProvider } from '../../contexts/AuthProvider'
afterEach(() => cleanup())
const renderLogin = () => render(
  <MemoryRouter>
    <AuthProvider>
      <Login />
    </AuthProvider>
  </MemoryRouter>
)
describe('Login', () => {
  it('renderiza formulario con campos usuario y password', () => {
    renderLogin()
    expect(screen.getByLabelText('Usuario')).toBeInTheDocument()
    expect(screen.getByLabelText('Contraseña')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Iniciar sesión' })).toBeInTheDocument()
  })
  it('credenciales inválidas muestra error', async () => {
    renderLogin()
    await userEvent.type(screen.getByLabelText('Usuario'), 'malo')
    await userEvent.type(screen.getByLabelText('Contraseña'), 'mal')
    await userEvent.click(screen.getByRole('button', { name: 'Iniciar sesión' }))
    expect(await screen.findByText('Credenciales inválidas')).toBeInTheDocument()
  })
  it('link a registro está presente', () => {
    renderLogin()
    expect(screen.getByText('¿No tienes cuenta?').querySelector('a')).toHaveAttribute('href', '/register')  })
  it('campos son required', () => {
    renderLogin()
    expect(screen.getByLabelText('Usuario')).toBeRequired()
    expect(screen.getByLabelText('Contraseña')).toBeRequired()
  })
  it('muestra formulario de login', () => {
    renderLogin()
    expect(screen.getByText('Accede a tu cuenta')).toBeInTheDocument()
  })
})