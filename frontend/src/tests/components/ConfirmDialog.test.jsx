import { describe, it, expect, vi, afterEach } from 'vitest'
import { render, screen, cleanup } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import '@testing-library/jest-dom/vitest'
import ConfirmDialog from '../../components/ConfirmDialog'
describe('ConfirmDialog', () => {
  afterEach(() => cleanup())
  it('renderiza título y mensaje', () => {
    render(<ConfirmDialog title="¿Eliminar?" message="No se puede deshacer" onConfirm={() => {}} onCancel={() => {}} />)
    expect(screen.getByText('¿Eliminar?')).toBeInTheDocument()
    expect(screen.getByText('No se puede deshacer')).toBeInTheDocument()
  })
  it('muestra labels por defecto', () => {
    render(<ConfirmDialog title="T" message="M" onConfirm={() => {}} onCancel={() => {}} />)
    expect(screen.getByRole('button', { name: 'Confirmar' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Cancelar' })).toBeInTheDocument()
  })
  it('click en Confirmar llama a onConfirm', async () => {
    const onConfirm = vi.fn()
    render(<ConfirmDialog title="T" message="M" onConfirm={onConfirm} onCancel={() => {}} />)
    await userEvent.click(screen.getByRole('button', { name: 'Confirmar' }))
    expect(onConfirm).toHaveBeenCalledOnce()
  })
  it('click en Cancelar llama a onCancel', async () => {
    const onCancel = vi.fn()
    render(<ConfirmDialog title="T" message="M" onConfirm={() => {}} onCancel={onCancel} />)
    await userEvent.click(screen.getByRole('button', { name: 'Cancelar' }))
    expect(onCancel).toHaveBeenCalledOnce()
  })
})