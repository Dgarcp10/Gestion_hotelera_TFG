import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/useAuth';
import api from '../services/api';
import ProtectedHeader from '../components/ProtectedHeader';
import LimpiezaNav from '../components/LimpiezaNav';
import Footer from '../components/Footer';
import '../components/Jefe.css';
import '../components/Limpieza.css';
export default function LimpiezaPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [tareas, setTareas] = useState([]);
  const [previstas, setPrevistas] = useState([]);
  const [modalProgramar, setModalProgramar] = useState(false);
  const [modalConfirmar, setModalConfirmar] = useState(null);
  const [modalExito, setModalExito] = useState(null);
  const [numeroInput, setNumeroInput] = useState('');
  const cargarDatos = () => {
    api.get('/api/tareas-limpieza/pendientes').then(res => setTareas(res.data)).catch(() => {});
    api.get('/api/tareas-limpieza/previstas').then(res => setPrevistas(res.data)).catch(() => {});
  };
  useEffect(() => {
    if (!user) navigate('/login');
  }, [user, navigate]);
  useEffect(() => {
    if (user) cargarDatos();
  }, [user]);
  const handleProgramar = async () => {
    try {
      await api.post('/api/tareas-limpieza/programar', null, { params: { numero: numeroInput } });
      setModalProgramar(false);
      setNumeroInput('');
      cargarDatos();
    } catch (err) {
      alert(err.response?.data?.error || 'Error al programar limpieza');
    }
  };
  const handleCompletar = async (tareaId) => {
    try {
      await api.post(`/api/tareas-limpieza/${tareaId}/completar`);
      setModalConfirmar(null);
      cargarDatos();
    } catch (err) {
      alert(err.response?.data?.error || 'Error al completar limpieza');
    }
  };
  const handleLimpiarAhora = async (numero) => {
    try {
      await api.post('/api/tareas-limpieza/programar', null, { params: { numero } });
      cargarDatos();
    } catch (err) {
      alert(err.response?.data?.error || 'Error al programar limpieza');
    }
  };
  const estadoLabel = (estado) => {
    if (estado === 'LIBRE') return 'Libre';
    if (estado === 'OCUPADA') return 'Ocupada';
    return 'Bloqueada';
  };
  const motivoLabel = (tipo) => {
    if (tipo === 'CHECKOUT') return 'Post-checkout';
    if (tipo === 'REPASO_ESTANCIA') return 'Repaso';
    return 'Repaso';
  };
  if (!user) return null;
  return (
    <>
      <ProtectedHeader />
      <main className="limpieza-page">
        {user?.rol === 'LIMPIEZA' && <LimpiezaNav />}
        <div className="limpieza-container">
          <div className="limpieza-header">
            <h1>Limpieza</h1>
            <button className="btn-crear" onClick={() => setModalProgramar(true)}>Añadir limpieza</button>
          </div>
          <div className="limpieza-section-title">Pendientes ({tareas.length})</div>
          <div className="habitaciones-table-wrapper">
            <table className="habitaciones-table">
              <thead>
                <tr>
                  <th>Nº</th>
                  <th>Tipo</th>
                  <th>Estado</th>
                  <th>Motivo</th>
                  <th>Info</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {tareas.length === 0 && (
                  <tr><td colSpan="6" className="table-empty">No hay habitaciones pendientes de limpieza</td></tr>
                )}
                {tareas.map(t => (
                  <tr key={t.id}>
                    <td>{t.habitacion?.numero}</td>
                    <td>{t.habitacion?.tipoHabitacion?.nombre || '-'}</td>
                    <td>{estadoLabel(t.habitacion?.estado)}</td>
                    <td>{motivoLabel(t.tipo)}</td>
                    <td className="limpieza-info">
                      {t.habitacion?.estado === 'OCUPADA'
                        ? 'Ocupada'
                        : 'Libre'}
                    </td>
                    <td>
                      <button
                        className="checkin-accion"
                        onClick={() => setModalConfirmar(t)}
                      >
                        Limpia
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="limpieza-section-title">Previstas mañana ({previstas.length})</div>
          <div className="habitaciones-table-wrapper">
            <table className="habitaciones-table">
              <thead>
                <tr>
                  <th>Nº</th>
                  <th>Tipo</th>
                  <th>Estado</th>
                  <th>Próxima limpieza</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {previstas.length === 0 && (
                  <tr><td colSpan="5" className="table-empty">No hay limpiezas previstas para mañana</td></tr>
                )}
                {previstas.map(h => (
                  <tr key={h.id}>
                    <td>{h.numero}</td>
                    <td>{h.tipoHabitacion?.nombre || '-'}</td>
                    <td>{estadoLabel(h.estado)}</td>
                    <td className="limpieza-info">{h.proximaLimpieza}</td>
                    <td>
                      <button
                        className="checkin-accion"
                        onClick={() => handleLimpiarAhora(h.numero)}
                      >
                        Limpiar ahora
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
      {modalProgramar && (
        <div className="modal-overlay" onClick={() => setModalProgramar(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <h2>Añadir limpieza</h2>
            <div className="form-group">
              <label>Nº de habitación</label>
              <input
                type="number"
                value={numeroInput}
                onChange={e => setNumeroInput(e.target.value)}
                required
              />
            </div>
            <div className="modal-actions">
              <button className="btn-cancelar" onClick={() => setModalProgramar(false)}>Cancelar</button>
              <button className="btn-guardar" onClick={handleProgramar}>Añadir</button>
            </div>
          </div>
        </div>
      )}
      {modalConfirmar && (
        <div className="modal-overlay" onClick={() => setModalConfirmar(null)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <h2>Confirmar limpieza</h2>
            <p style={{ marginBottom: 24 }}>
              ¿Marcar habitación <strong>{modalConfirmar.habitacion?.numero}</strong> como limpia?
            </p>
            <div className="modal-actions">
              <button className="btn-cancelar" onClick={() => setModalConfirmar(null)}>Cancelar</button>
              <button className="btn-guardar" onClick={() => handleCompletar(modalConfirmar.id)}>
                Sí, marcar como limpia
              </button>
            </div>
          </div>
        </div>
      )}
      {modalExito && (
        <div className="modal-overlay" onClick={() => setModalExito(null)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <h2>Limpieza completada</h2>
            <p>Habitación <strong>{modalExito}</strong> marcada como limpia.</p>
            <div className="modal-actions">
              <button className="btn-guardar" onClick={() => setModalExito(null)}>Cerrar</button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}