import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/useAuth';
import api from '../services/api';
import ProtectedHeader from '../components/ProtectedHeader';
import UserNav from '../components/UserNav';
import Footer from '../components/Footer';
import '../components/Jefe.css';
import '../components/User.css';
export default function NuevaReservaPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [tipos, setTipos] = useState([]);
  const [tipoId, setTipoId] = useState('');
  const [fechaEntrada, setFechaEntrada] = useState('');
  const [fechaSalida, setFechaSalida] = useState('');
  const [enviando, setEnviando] = useState(false);
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
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!tipoId || !fechaEntrada || !fechaSalida) return;
    setEnviando(true);
    try {
      await api.post('/api/reservas', null, {
        params: {
          tipoHabitacionId: tipoId,
          fechaEntrada,
          fechaSalida,
        },
      });
      navigate('/mis-reservas');
    } catch (err) {
      alert(err.response?.data?.error || 'Error al crear la reserva');
    } finally {
      setEnviando(false);
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
                <button type="submit" className="btn-guardar" disabled={enviando}>
                  {enviando ? 'Reservando...' : 'Reservar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      </main>
      <Footer />
    </>
  );
}