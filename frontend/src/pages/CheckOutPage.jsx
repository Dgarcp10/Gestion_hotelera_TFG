import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/useAuth';
import api from '../services/api';
import ProtectedHeader from '../components/ProtectedHeader';
import RecepcionNav from '../components/RecepcionNav';
import JefeNav from '../components/JefeNav';
import Footer from '../components/Footer';
import '../components/Jefe.css';
import '../components/Recepcion.css';
export default function CheckOutPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [estancias, setEstancias] = useState([]);
  const [modal, setModal] = useState(null);
  const [error, setError] = useState(null);
  const cargarEstancias = () => {
    api.get('/api/reservas/para-checkout')
      .then(res => setEstancias(res.data))
      .catch(err => console.error('Error al cargar estancias', err));
  };
  useEffect(() => {
    if (!user) navigate('/login');
  }, [user, navigate]);
  useEffect(() => {
    if (user) cargarEstancias();
  }, [user]);
  const handleCheckOut = async (id) => {
    setError(null);
    try {
      const res = await api.post(`/api/reservas/${id}/check-out`);
      setModal({
        numeroHabitacion: res.data.habitacion?.numero ?? '—',
      });
      cargarEstancias();
    } catch (err) {
      setError(err.response?.data?.error || 'Error al hacer check-out');
    }
  };
  if (!user) return null;
  return (
    <>
      <ProtectedHeader />
      <main className="checkin-page">
        {(user?.rol === 'RECEPCION' && <RecepcionNav />) ||
          (user?.rol === 'JEFE' && <JefeNav />)}
        <div className="checkin-container">
          <div className="checkin-header">
            <h1>Check-out</h1>
          </div>
          {error && (
            <div style={{
              background: '#fce4ec', color: '#c62828', padding: '12px 16px',
              borderRadius: 8, marginBottom: 20, fontSize: 14,
            }}>
              {error}
            </div>
          )}
          <div className="habitaciones-table-wrapper">
            <table className="habitaciones-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Cliente</th>
                  <th>Tipo</th>
                  <th>Habitación</th>
                  <th>Entrada</th>
                  <th>Salida</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {estancias.length === 0 && (
                  <tr><td colSpan="7" className="table-empty">No hay estancias para finalizar hoy</td></tr>
                )}
                {estancias.map((r) => (
                  <tr key={r.id}>
                    <td>{r.id}</td>
                    <td>{r.usuario?.nombre} {r.usuario?.apellido}</td>
                    <td>{r.tipoHabitacion?.nombre || '-'}</td>
                    <td>{r.habitacion?.numero ?? '-'}</td>
                    <td>{r.fechaEntrada}</td>
                    <td>{r.fechaSalida}</td>
                    <td>
                      <button className="checkin-accion" onClick={() => handleCheckOut(r.id)}>
                        Finalizar estancia
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
        <div className="modal-overlay" onClick={() => setModal(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Check-out realizado</h2>
            <p style={{ marginBottom: 8 }}>Habitación liberada:</p>
            <div style={{
              background: '#e8f5e9',
              padding: '16px 20px',
              borderRadius: 8,
              fontFamily: 'monospace',
              fontSize: 28,
              fontWeight: 700,
              color: '#2e7d32',
              textAlign: 'center',
              marginBottom: 8,
            }}>
              {modal.numeroHabitacion}
            </div>
            <p style={{ fontSize: 13, color: '#666', marginBottom: 24 }}>
              Pendiente de limpieza.
            </p>
            <div className="modal-actions">
              <button className="btn-guardar" onClick={() => setModal(null)}>Cerrar</button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}