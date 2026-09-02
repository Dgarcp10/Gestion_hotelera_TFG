import { describe, it, expect, afterEach } from 'vitest'
import { render, screen, cleanup } from '@testing-library/react'
import '@testing-library/jest-dom/vitest'
import { MemoryRouter } from 'react-router-dom'
import Landing from '../../pages/Landing'
afterEach(() => cleanup())
const renderLanding = () => render(
  <MemoryRouter><Landing /></MemoryRouter>
)
describe('Landing', () => {
  it('muestra nombre del hotel en hero', () => {
    renderLanding()
    expect(screen.getByRole('heading', { name: 'Hotel Villa de Lerma', level: 1 })).toBeInTheDocument()
  })
  it('muestra botones en hero', () => {
    renderLanding()
    const heroButtons = document.querySelector('.hero-buttons')
    expect(heroButtons).not.toBeNull()
    expect(heroButtons.querySelector('a[href="/login"]')).not.toBeNull()
    expect(heroButtons.querySelector('a[href="/register"]')).not.toBeNull()
  })
  it('muestra sección info cards', () => {
    renderLanding()
    expect(screen.getByText('Sobre nosotros')).toBeInTheDocument()
    expect(screen.getByText('Descubre el hotel')).toBeInTheDocument()
  })
})