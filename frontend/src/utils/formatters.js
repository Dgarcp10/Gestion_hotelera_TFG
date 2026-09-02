export const formatearTarjeta = (digitos) => digitos.replace(/(\d{4})(?=\d)/g, '$1 ');
export const formatearCaducidad = (digitos) => {
  if (digitos.length <= 2) return digitos;
  return `${digitos.slice(0, 2)}/${digitos.slice(2)}`;
};