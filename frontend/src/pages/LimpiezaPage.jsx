import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/useAuth';
import { useToast } from '../contexts/useToast';
import api from '../services/api';
import ProtectedHeader from '../components/ProtectedHeader';
import LimpiezaNav from '../components/LimpiezaNav';
import JefeNav from '../components/JefeNav';
import Footer from '../components/Footer';
import '../components/Jefe.css';
import '../components/Limpieza.css';
import { Icon } from '@mdi/react';
import { mdiBroom } from '@mdi/js';

export default function LimpiezaPage() {
  const { user } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [habitaciones, setHabitaciones] = useState([]);
  const [habitacionesLimpieza, setHabitacionesLimpieza] = useState([]);
  const [modalProgramar, setModalProgramar] = useState(false);
  const [modalConfirmar, setModalConfirmar] = useState(null);
  const [numeroInput, setNumeroInput] = useState('');
  const [errorNumero, setErrorNumero] = useState('');
  const cargarDatos = () => {
    api.get('/api/tareas-limpieza/pendientes').then(res => setHabitaciones(res.data)).catch(() => {});
    api.get('/api/habitaciones').then(res =>
      setHabitacionesLimpieza(res.data.filter(h => h.pendienteLimpieza === false))
    ).catch(() => {});
  };
  useEffect(() => {
    if (!user) navigate('/login');
  }, [user, navigate]);
  useEffect(() => {
    if (user) cargarDatos();
  }, [user]);
  const handleProgramar = async () => {
    const numero = Number(numeroInput);
    if (!habitacionesLimpieza.some(h => h.numero === numero)) {
      setErrorNumero('La habitación no existe o ya tiene una limpieza pendiente');
      return;
    }
    try {
      await api.post('/api/tareas-limpieza/programar', null, { params: { numero } });
      setModalProgramar(false);
      setNumeroInput('');
      setErrorNumero('');
      cargarDatos();
    } catch (err) {
      toast.error(err.response?.data?.error || 'Error al programar limpieza');
    }
  };
  const handleLimpiar = async (numero) => {
    try {
      await api.post('/api/tareas-limpieza/limpiar', null, { params: { numero } });
      setModalConfirmar(null);
      cargarDatos();
    } catch (err) {
      toast.error(err.response?.data?.error || 'Error al completar limpieza');
    }
  };
  const estadoLabel = (estado) => {
    if (estado === 'LIBRE') return 'Libre';
    if (estado === 'OCUPADA') return 'Ocupada';
    return 'Bloqueada';
  };
  if (!user) return null;
  return (
    <>
      <ProtectedHeader />
      <main className="limpieza-page">
        {(user?.rol === 'LIMPIEZA' && <LimpiezaNav />) ||
        (user?.rol === 'JEFE' && <JefeNav />)}
        <div className="limpieza-container">
          <div className="limpieza-header">
            <h1>Limpieza</h1>
            <button className="btn-crear" onClick={() => setModalProgramar(true)}>Añadir limpieza</button>
          </div>
          <div className="limpieza-section-title">Pendientes ({habitaciones.length})</div>
          <div className="habitaciones-table-wrapper">
            <table className="habitaciones-table">
              <thead>
                <tr>
                  <th>Nº</th>
                  <th>Estado</th>
                  <th>Tipo</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {habitaciones.length === 0 && (
                  <tr><td colSpan="4" className="table-empty">No hay habitaciones pendientes de limpieza</td></tr>
                )}
                {habitaciones.map(h => (
                  <tr key={h.id}>
                    <td>{h.numero}</td>
                    <td>{estadoLabel(h.estado)}</td>
                    <td>{h.tipoHabitacion?.nombre || '-'}</td>
                    <td>
                      <button
                        className="limpiar-btn"
                        aria-label={`Limpiar habitación ${h.numero}`}
                        title="Limpiar"
                        onClick={() => setModalConfirmar(h)}
                      >
                        <Icon path={mdiBroom} size={1} />
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
                onChange={e => { setNumeroInput(e.target.value); setErrorNumero(''); }}
                required
              />
            </div>
            {errorNumero && <p className="form-error">{errorNumero}</p>}
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
              ¿Marcar habitación <strong>{modalConfirmar.numero}</strong> como limpia?
            </p>
            <div className="modal-actions">
              <button className="btn-cancelar" onClick={() => setModalConfirmar(null)}>Cancelar</button>
              <button className="btn-guardar" onClick={() => handleLimpiar(modalConfirmar.numero)}>
                Sí, marcar como limpia
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}