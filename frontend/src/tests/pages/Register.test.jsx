import { describe, it, expect, vi, afterEach } from 'vitest'
import { render, screen, cleanup } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import '@testing-library/jest-dom/vitest'
import { MemoryRouter } from 'react-router-dom'
import Register from '../../pages/Register'
import { AuthProvider } from '../../contexts/AuthProvider'
import api from '../../services/api'
vi.mock('../../services/api')
afterEach(() => cleanup())
const renderRegister = () => render(
  <MemoryRouter>
    <AuthProvider>
      <Register />
    </AuthProvider>
  </MemoryRouter>
)
describe('Register', () => {
  it('renderiza todos los campos del formulario', () => {
    renderRegister()
    expect(screen.getByLabelText('Nombre')).toBeInTheDocument()
    expect(screen.getByLabelText('Apellido')).toBeInTheDocument()
    expect(screen.getByLabelText('Email')).toBeInTheDocument()
    expect(screen.getByLabelText('Usuario')).toBeInTheDocument()
    expect(screen.getByLabelText('Contraseña')).toBeInTheDocument()
    expect(screen.getByLabelText('Confirmar contraseña')).toBeInTheDocument()
  })
  it('contraseñas no coinciden muestra error', async () => {
    renderRegister()
    await userEvent.type(screen.getByLabelText('Nombre'), 'Ana')
    await userEvent.type(screen.getByLabelText('Apellido'), 'Perez')
    await userEvent.type(screen.getByLabelText('Email'), 'ana@test.es')
    await userEvent.type(screen.getByLabelText('Usuario'), 'ana')
    await userEvent.type(screen.getByLabelText('Contraseña'), 'clave123')
    await userEvent.type(screen.getByLabelText('Confirmar contraseña'), 'clave456')
    await userEvent.click(screen.getByRole('button', { name: 'Crear cuenta' }))
    expect(await screen.findByText('Las contraseñas no coinciden')).toBeInTheDocument()
  })
  it('error del servidor muestra mensaje', async () => {
    api.post.mockRejectedValue({ response: { data: { error: 'El usuario ya existe' } } })
    renderRegister()
    await userEvent.type(screen.getByLabelText('Nombre'), 'Ana')
    await userEvent.type(screen.getByLabelText('Apellido'), 'Perez')
    await userEvent.type(screen.getByLabelText('Email'), 'ana@test.es')
    await userEvent.type(screen.getByLabelText('Usuario'), 'duplicado')
    await userEvent.type(screen.getByLabelText('Contraseña'), 'clave123')
    await userEvent.type(screen.getByLabelText('Confirmar contraseña'), 'clave123')
    await userEvent.click(screen.getByRole('button', { name: 'Crear cuenta' }))
    expect(await screen.findByText('El usuario ya existe')).toBeInTheDocument()
  })
  it('link a login está presente', () => {
    renderRegister()
    expect(screen.getByText('¿Ya tienes cuenta?').querySelector('a')).toHaveAttribute('href', '/login')
  })
  it('muestra texto de bienvenida', () => {
    renderRegister()
    expect(screen.getByText('Regístrate en el hotel')).toBeInTheDocument()
  })
})