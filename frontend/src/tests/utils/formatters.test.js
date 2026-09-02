import { describe, it, expect } from 'vitest'
import { formatearTarjeta, formatearCaducidad } from '../../utils/formatters'
describe('formatearTarjeta', () => {
  it('cadena vacía devuelve vacío', () => {
    expect(formatearTarjeta('')).toBe('')
  })
  it('4 dígitos sin espacios', () => {
    expect(formatearTarjeta('1234')).toBe('1234')
  })
  it('8 dígitos añade un espacio', () => {
    expect(formatearTarjeta('12345678')).toBe('1234 5678')
  })
  it('16 dígitos añade tres espacios', () => {
    expect(formatearTarjeta('1234567890123456')).toBe('1234 5678 9012 3456')
  })
})
describe('formatearCaducidad', () => {
  it('cadena vacía devuelve vacío', () => {
    expect(formatearCaducidad('')).toBe('')
  })
  it('1 dígito sin barra', () => {
    expect(formatearCaducidad('1')).toBe('1')
  })
  it('2 dígitos sin barra', () => {
    expect(formatearCaducidad('12')).toBe('12')
  })
  it('4 dígitos añade barra', () => {
    expect(formatearCaducidad('1234')).toBe('12/34')
  })
})