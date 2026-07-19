import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/useAuth';
import api from '../services/api';
import ProtectedHeader from '../components/ProtectedHeader';
import RecepcionNav from '../components/RecepcionNav';
import Footer from '../components/Footer';
import '../components/Jefe.css';
import '../components/Recepcion.css';
export default function CheckInPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [reservas, setReservas] = useState([]);
  const [busqueda, setBusqueda] = useState('');
  const [modal, setModal] = useState(null);
  const cargarReservas = () => {
    api.get('/api/reservas/pendientes')
      .then(res => setReservas(res.data))
      .catch(err => console.error('Error al cargar reservas', err));
  };
  useEffect(() => {
    if (!user) navigate('/login');
  }, [user, navigate]);
  useEffect(() => {
    if (user) {
      cargarReservas();
    }
  }, [user]);
  const handleCheckIn = async (id) => {
    try {
      const res = await api.post(`/api/reservas/${id}/check-in`);
      setModal({
        numeroHabitacion: res.data.habitacion?.numero,
        reservaId: id,
      });
    } catch (err) {
      alert(err.response?.data?.error || 'Error al hacer check-in');
    }
  };
  const filtrarPorUsername = (username) => {
    if (!busqueda) return true;
    return username?.toLowerCase().includes(busqueda.toLowerCase());
  };
  if (!user) return null;
  return (
    <>
      <ProtectedHeader />
      <main className="checkin-page">
        {user?.rol === 'RECEPCION' && <RecepcionNav />}
        <div className="checkin-container">
          <div className="checkin-header">
            <h1>Check-in</h1>
            <input
              className="checkin-buscador"
              placeholder="Buscar por cliente..."
              value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)}
            />
          </div>
          <div className="habitaciones-table-wrapper">
            <table className="habitaciones-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Cliente</th>
                  <th>Tipo</th>
                  <th>Entrada</th>
                  <th>Salida</th>
                  <th>Total</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {reservas.filter(r => filtrarPorUsername(r.usuario?.username)).length === 0 && (
                  <tr><td colSpan="7" className="table-empty">No hay reservas pendientes de check-in</td></tr>
                )}
                {reservas.filter(r => filtrarPorUsername(r.usuario?.username)).map((r) => (
                  <tr key={r.id}>
                    <td>{r.id}</td>
                    <td>{r.usuario?.nombre} {r.usuario?.apellido}</td>
                    <td>{r.tipoHabitacion?.nombre || '-'}</td>
                    <td>{r.fechaEntrada}</td>
                    <td>{r.fechaSalida}</td>
                    <td>{r.precioTotal} €</td>
                    <td>
                      <button className="checkin-accion" onClick={() => handleCheckIn(r.id)}>
                        Iniciar estancia
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </main>
      <Footer />
      {modal && (
        <div className="modal-overlay" onClick={() => { setModal(null); cargarReservas(); }}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Check-in realizado</h2>
            <p style={{ marginBottom: 8 }}>Habitación asignada:</p>
            <div style={{
              background: '#e8f5e9',
              padding: '16px 20px',
              borderRadius: 8,
              fontFamily: 'monospace',
              fontSize: 28,
              fontWeight: 700,
              color: '#2e7d32',
              textAlign: 'center',
              marginBottom: 24,
            }}>
              {modal.numeroHabitacion}
            </div>
            <div className="modal-actions">
              <button className="btn-guardar" onClick={() => { setModal(null); cargarReservas(); }}>
                Cerrar
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}