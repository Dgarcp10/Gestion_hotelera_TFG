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

export default function MisReservasPage() {
  const { user } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [reservas, setReservas] = useState([]);
  const [tipos, setTipos] = useState([]);
  const [modalModificar, setModalModificar] = useState(null);
  const [tipoIdMod, setTipoIdMod] = useState('');
  const [entradaMod, setEntradaMod] = useState('');
  const [salidaMod, setSalidaMod] = useState('');
  const [numTarjeta, setNumTarjeta] = useState('');
  const [caducidadMod, setCaducidadMod] = useState('');
  const [cvvMod, setCvvMod] = useState('');
  const [guardandoMod, setGuardandoMod] = useState(false);
  const [reservaModal, setReservaModal] = useState(null);
  const [infoCancel, setInfoCancel] = useState(null);
  const [cancelando, setCancelando] = useState(false);
  const cargarReservas = () => {
    api.get('/api/reservas/mis-reservas')
      .then(res => setReservas(res.data))
      .catch(err => console.error('Error al cargar reservas', err));
  };
  useEffect(() => {
    api.get('/api/tipos-habitacion')
      .then(res => setTipos(res.data))
      .catch(() => {});
  }, []);
  useEffect(() => {
    if (!user) navigate('/login');
  }, [user, navigate]);
  useEffect(() => {
    if (user) cargarReservas();
  }, [user]);
  const estadoClass = (estado) => {
    if (estado === 'PENDIENTE') return 'tag-pendiente';
    if (estado === 'EN_CURSO') return 'tag-en_curso';
    if (estado === 'FINALIZADA') return 'tag-finalizada';
    return 'tag-cancelada';
  };
  const hoy = new Date().toISOString().slice(0, 10);
  const esCancelable = (r) => r.estado === 'PENDIENTE' && r.fechaEntrada > hoy;
  const handleVerCancelacion = (r) => {
    api.get(`/api/reservas/${r.id}/info-cancelacion`)
      .then(res => { setInfoCancel(res.data); setReservaModal(r); })
      .catch(err => toast.error(err.response?.data?.error || 'Error al obtener la cancelación'));
  };
  const handleConfirmarCancelacion = async () => {
    setCancelando(true);
    try {
      await api.post(`/api/reservas/${reservaModal.id}/cancelar`);
      toast.success('Reserva cancelada');
      setReservaModal(null);
      setInfoCancel(null);
      cargarReservas();
    } catch (err) {
      toast.error(err.response?.data?.error || 'Error al cancelar la reserva');
    } finally {
      setCancelando(false);
    }
  };
  const handleAbrirModificar = (r) => {
    setModalModificar(r);
    setTipoIdMod(String(r.tipoHabitacion?.id ?? ''));
    setEntradaMod(r.fechaEntrada);
    setSalidaMod(r.fechaSalida);
    setNumTarjeta('');
    setCaducidadMod('');
    setCvvMod('');
  };
  const tipoMod = tipos.find(t => t.id === Number(tipoIdMod));
  const nochesMod = (() => {
    if (!entradaMod || !salidaMod) return 0;
    return Math.max(0, Math.round((new Date(salidaMod) - new Date(entradaMod)) / (1000 * 60 * 60 * 24)));
  })();
  const nuevoTotal = tipoMod && nochesMod > 0
    ? (Number(tipoMod.precioBase) * nochesMod).toFixed(2)   // Number() evita "NaN €"
    : null;
  const totalAnterior = modalModificar ? Number(modalModificar.precioTotal) : 0;
  const diferencia = nuevoTotal !== null ? Number(nuevoTotal) - totalAnterior : 0;
  const sube = diferencia > 0;
  const validarTarjetaCliente = () => {
    if (!/^\d{16}$/.test(numTarjeta)) return 'Número de tarjeta inválido';
    const m = /^(\d{2})\/(\d{2})$/.exec(formatearCaducidad(caducidadMod));
    if (!m) return 'Fecha de caducidad inválida';
    const mes = Number(m[1]);
    const anio = Number(`20${m[2]}`);
    if (mes < 1 || mes > 12) return 'Fecha de caducidad inválida';
    if (anio < new Date().getFullYear() ||
        (anio === new Date().getFullYear() && mes < new Date().getMonth() + 1)) {
      return 'Tarjeta caducada';
    }
    if (!/^\d{3}$/.test(cvvMod)) return 'CVV inválido';
    return null;
  };
  const handleGuardarModificacion = async () => {
    if (!tipoIdMod || !entradaMod || !salidaMod || nuevoTotal === null) return;
    if (sube) {
      const errorCliente = validarTarjetaCliente();
      if (errorCliente) { toast.error(errorCliente); return; }
    }
    setGuardandoMod(true);
    try {
      await api.put(`/api/reservas/${modalModificar.id}`, null, {
        params: {
          tipoHabitacionId: tipoIdMod,
          fechaEntrada: entradaMod,
          fechaSalida: salidaMod,
          numeroTarjeta: sube ? numTarjeta : '',
          caducidad: sube ? formatearCaducidad(caducidadMod) : '',
          cvv: sube ? cvvMod : '',
        },
      });
      toast.success('Reserva modificada');
      setModalModificar(null);
      cargarReservas();
    } catch (err) {
      toast.error(err.response?.data?.error || 'Error al modificar la reserva');
    } finally {
      setGuardandoMod(false);
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
            <h1>Mis reservas</h1>
            <button className="btn-crear" onClick={() => navigate('/nueva-reserva')}>+ Nueva reserva</button>
          </div>
          <div className="habitaciones-table-wrapper">
            <table className="habitaciones-table">
              <thead>
                <tr>
                  <th>Tipo</th>
                  <th>Entrada</th>
                  <th>Salida</th>
                  <th>Estado</th>
                  <th>Total</th>
                  <th>Ref. pago</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {reservas.length === 0 && (
                  <tr><td colSpan="7" className="table-empty">No tienes reservas</td></tr>
                )}
                {reservas.map((r) => (
                  <tr key={r.id}>
                    <td>{r.tipoHabitacion?.nombre || '-'}</td>
                    <td>{r.fechaEntrada}</td>
                    <td>{r.fechaSalida}</td>
                    <td><span className={`estado-tag ${estadoClass(r.estado)}`}>{r.estado}</span></td>
                    <td>{r.estado === 'CANCELADA' ? `${r.importeCobrado} €` : `${r.precioTotal} €`}</td>
                    <td>{r.pago?.referencia || '-'}</td>
                    <td>
                      {esCancelable(r) && (
                        <span className="acciones-cell">
                          <button className="btn-cancelar" onClick={() => handleVerCancelacion(r)}>Cancelar</button>
                          <button className="btn-guardar" onClick={() => handleAbrirModificar(r)}>Modificar</button>
                        </span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
        {reservaModal && infoCancel && (
          <div className="modal-overlay" onClick={() => setReservaModal(null)}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
              <h2>Cancelar reserva</h2>
              <p>Reserva de <strong>{reservaModal.tipoHabitacion?.nombre}</strong> (del <strong>{reservaModal.fechaEntrada}</strong> al <strong>{reservaModal.fechaSalida}</strong>).</p>
              <p style={{ marginTop: 16 }}>Total reserva vigente: <strong>{reservaModal.precioTotal} €</strong></p>
              <p>Coste de cancelación (penalización del <strong>{infoCancel.penalizacionPorcentaje}%</strong>): <strong>{infoCancel.importeCobrado} €</strong></p>
              <p>Se te reembolsará: <strong>{infoCancel.importeReembolsar} €</strong></p>
              <div className="modal-actions">
                <button type="button" className="btn-cancelar" onClick={() => setReservaModal(null)} disabled={cancelando}>
                  No, mantener reserva
                </button>
                <button type="button" className="btn-guardar" onClick={handleConfirmarCancelacion} disabled={cancelando}>
                  {cancelando ? 'Cancelando...' : 'Cancelar reserva y cobrar penalización'}
                </button>
              </div>
            </div>
          </div>
        )}
        {modalModificar && (
          <div className="modal-overlay" onClick={() => setModalModificar(null)}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
              <h2>Modificar reserva</h2>
              <div className="form-group">
                <label>Tipo de habitación</label>
                <select value={tipoIdMod} onChange={e => setTipoIdMod(e.target.value)}>
                  {tipos.map(t => (
                    <option key={t.id} value={t.id}>{t.nombre} — {t.precioBase} €/noche</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>Fecha de entrada</label>
                <input type="date" value={entradaMod} onChange={e => setEntradaMod(e.target.value)} />
              </div>
              <div className="form-group">
                <label>Fecha de salida</label>
                <input type="date" value={salidaMod} onChange={e => setSalidaMod(e.target.value)} />
              </div>
              {nuevoTotal !== null && (
                <div className="form-group">
                  <label>Nuevo precio</label>
                  <div className="precio-display">{nuevoTotal} € ({nochesMod} noche{nochesMod !== 1 ? 's' : ''})</div>
                </div>
              )}
              {sube && (
                <>
                  <p style={{ marginBottom: 12 }}>Diferencia a pagar: <strong>{diferencia.toFixed(2)} €</strong></p>
                  <div className="form-group">
                    <label>Número de tarjeta</label>
                    <input type="text" inputMode="numeric" placeholder="1234 5678 9012 3456"
                      value={formatearTarjeta(numTarjeta)}
                      onChange={(e) => setNumTarjeta(e.target.value.replace(/\D/g, '').slice(0, 16))} />
                  </div>
                  <div className="form-group">
                    <label>Caducidad (MM/AA)</label>
                    <input type="text" inputMode="numeric" placeholder="MM/AA"
                      value={formatearCaducidad(caducidadMod)}
                      onChange={(e) => setCaducidadMod(e.target.value.replace(/\D/g, '').slice(0, 4))} />
                  </div>
                  <div className="form-group">
                    <label>CVV</label>
                    <input type="password" inputMode="numeric" maxLength={3} placeholder="123"
                      value={cvvMod}
                      onChange={(e) => setCvvMod(e.target.value.replace(/\D/g, '').slice(0, 3))} />
                  </div>
                </>
              )}
              {!sube && nuevoTotal !== null && diferencia < 0 && (
                <p style={{ marginTop: 8 }}>Se te reembolsarán <strong>{Math.abs(diferencia).toFixed(2)} €</strong> automáticamente.</p>
              )}
              <div className="modal-actions">
                <button type="button" className="btn-cancelar" onClick={() => setModalModificar(null)} disabled={guardandoMod}>
                  Cerrar
                </button>
                <button type="button" className="btn-guardar" onClick={handleGuardarModificacion}
                  disabled={guardandoMod || !tipoIdMod || !entradaMod || !salidaMod || nuevoTotal === null}>
                  {guardandoMod ? 'Guardando...' : 'Guardar cambios'}
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