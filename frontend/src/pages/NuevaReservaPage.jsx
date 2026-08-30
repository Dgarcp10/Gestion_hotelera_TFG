import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/useAuth';
import { useToast } from '../contexts/useToast';
import api from '../services/api';
import ProtectedHeader from '../components/ProtectedHeader';
import UserNav from '../components/UserNav';
import Footer from '../components/Footer';
import '../components/Jefe.css';
import '../components/User.css';

const formatearTarjeta = (digitos) => digitos.replace(/(\d{4})(?=\d)/g, '$1 ');
const formatearCaducidad = (digitos) => {
  if (digitos.length <= 2) return digitos;
  return `${digitos.slice(0, 2)}/${digitos.slice(2)}`;
};

export default function NuevaReservaPage() {
  const { user } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [tipos, setTipos] = useState([]);
  const [tipoId, setTipoId] = useState('');
  const [fechaEntrada, setFechaEntrada] = useState('');
  const [fechaSalida, setFechaSalida] = useState('');
  const [mostrarPago, setMostrarPago] = useState(false);
  const [numeroTarjeta, setNumeroTarjeta] = useState('');
  const [caducidad, setCaducidad] = useState('');
  const [cvv, setCvv] = useState('');
  const [pagando, setPagando] = useState(false);
  useEffect(() => {
    if (!user) navigate('/login');
  }, [user, navigate]);
  useEffect(() => {
    api.get('/api/tipos-habitacion')
      .then(res => setTipos(res.data))
      .catch(() => {});
  }, []);
  const tipoSeleccionado = tipos.find(t => t.id === Number(tipoId));
  const calcularNoches = () => {
    if (!fechaEntrada || !fechaSalida) return 0;
    const e = new Date(fechaEntrada);
    const s = new Date(fechaSalida);
    return Math.max(0, Math.round((s - e) / (1000 * 60 * 60 * 24)));
  };
  const noches = calcularNoches();
  const precioTotal = tipoSeleccionado && noches > 0
    ? (tipoSeleccionado.precioBase * noches).toFixed(2)
    : null;
  const handleSubmit = (e) => {
    e.preventDefault();
    if (!tipoId || !fechaEntrada || !fechaSalida || !precioTotal) return;
    setMostrarPago(true);
  };
  const validarClientSide = () => {
    if (!/^\d{16}$/.test(numeroTarjeta)) return 'Número de tarjeta inválido';
    const m = /^(\d{2})\/(\d{2})$/.exec(formatearCaducidad(caducidad));
    if (!m) return 'Fecha de caducidad inválida';
    const mes = Number(m[1]);
    const anio = Number(`20${m[2]}`);
    if (mes < 1 || mes > 12) return 'Fecha de caducidad inválida';
    if (anio < new Date().getFullYear() ||
        (anio === new Date().getFullYear() && mes < new Date().getMonth() + 1)) {
      return 'Tarjeta caducada';
    }
    if (!/^\d{3}$/.test(cvv)) return 'CVV inválido';
    return null;
  };
  const handlePagar = async () => {
    const errorCliente = validarClientSide();
    if (errorCliente) {
      toast.error(errorCliente);
      return;
    }
    setPagando(true);
    try {
      await api.post('/api/reservas', null, {
        params: { tipoHabitacionId: tipoId, fechaEntrada, fechaSalida, numeroTarjeta, caducidad: formatearCaducidad(caducidad), cvv },
      });
      toast.success('Reserva creada y pago realizado');
      navigate('/mis-reservas');
    } catch (err) {
      toast.error(err.response?.data?.error || 'Error al crear la reserva');
    } finally {
      setPagando(false);
    }
  };

  if (!user) return null;
  return (
    <>
      <ProtectedHeader />
      <main className="mis-reservas-page">
        {user?.rol === 'USUARIO' && <UserNav />}
        <div className="mis-reservas-container">
          <div className="mis-reservas-header">
            <h1>Nueva reserva</h1>
          </div>
          <div className="habitaciones-table-wrapper" style={{ padding: '40px' }}>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Tipo de habitación</label>
                <select value={tipoId} onChange={e => setTipoId(e.target.value)} required>
                  <option value="">Seleccionar</option>
                  {tipos.map(t => (
                    <option key={t.id} value={t.id}>
                      {t.nombre} — {t.precioBase} €/noche
                    </option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>Fecha de entrada</label>
                <input type="date" value={fechaEntrada} onChange={e => setFechaEntrada(e.target.value)} required />
              </div>
              <div className="form-group">
                <label>Fecha de salida</label>
                <input type="date" value={fechaSalida} onChange={e => setFechaSalida(e.target.value)} required />
              </div>
              {precioTotal && (
                <div className="form-group">
                  <label>Precio total</label>
                  <div className="precio-display">{precioTotal} € ({noches} noche{noches !== 1 ? 's' : ''})</div>
                </div>
              )}
              <div className="modal-actions">
                <button type="button" className="btn-cancelar" onClick={() => navigate('/mis-reservas')}>
                  Cancelar
                </button>
                <button type="submit" className="btn-guardar" disabled={!precioTotal}>
                  Reservar y pagar
                </button>
              </div>
            </form>
          </div>
        </div>
        {mostrarPago && (
          <div className="modal-overlay" onClick={() => setMostrarPago(false)}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
              <h2>Pago seguro (simulado)</h2>
              <p style={{ marginBottom: 24 }}>Importe a pagar: <strong>{precioTotal} €</strong></p>
              <div className="form-group">
                <label>Número de tarjeta</label>
                <input type="text" inputMode="numeric" placeholder="1234 5678 9012 3456"
                  value={formatearTarjeta(numeroTarjeta)}
                  onChange={(e) => setNumeroTarjeta(e.target.value.replace(/\D/g, '').slice(0, 16))} />
              </div>
              <div className="form-group">
                <label>Caducidad (MM/AA)</label>
                <input type="text" inputMode="numeric" placeholder="MM/AA"
                  value={formatearCaducidad(caducidad)}
                  onChange={(e) => setCaducidad(e.target.value.replace(/\D/g, '').slice(0, 4))} />
              </div>
              <div className="form-group">
                <label>CVV</label>
                <input type="password" inputMode="numeric" maxLength={3} placeholder="123"
                  value={cvv}
                  onChange={(e) => setCvv(e.target.value.replace(/\D/g, '').slice(0, 3))} />
              </div>
              <div className="modal-actions">
                <button type="button" className="btn-cancelar" onClick={() => setMostrarPago(false)} disabled={pagando}>
                  Volver
                </button>
                <button type="button" className="btn-guardar" onClick={handlePagar} disabled={pagando}>
                  {pagando ? 'Procesando...' : `Pagar ${precioTotal} €`}
                </button>
              </div>
            </div>
          </div>
        )}
      </main>
      <Footer />
    </>
  );
}