import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/useAuth';
import api from '../services/api';
import ProtectedHeader from '../components/ProtectedHeader';
import JefeNav from '../components/JefeNav';
import Footer from '../components/Footer';
import HabitacionModal from '../components/HabitacionModal';
import '../components/Jefe.css';

export default function HabitacionesPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [habitaciones, setHabitaciones] = useState([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editando, setEditando] = useState(null);
  useEffect(() => {
    if (!user) navigate('/login');
  }, [user, navigate]);
  useEffect(() => {
    if (user) {
      api.get('/api/habitaciones')
        .then(res => setHabitaciones(res.data))
        .catch(err => console.error('Error al cargar habitaciones', err));
    }
  }, [user]);
  const abrirCrear = () => {
    setEditando(null);
    setModalOpen(true);
  };
  const abrirEditar = (habitacion) => {
    setEditando(habitacion);
    setModalOpen(true);
  };
  const handleEliminar = async (id) => {
    if (!confirm('¿Eliminar esta habitación?')) return;
    try {
      await api.delete(`/api/habitaciones/${id}`);
      api.get('/api/habitaciones')
        .then(res => setHabitaciones(res.data))
        .catch(err => console.error('Error al cargar habitaciones', err));
    } catch (err) {
      alert(err.response?.data?.error || 'Error al eliminar');
    }
  };
  const handleGuardar = async (data) => {
    try {
      if (editando) {
        await api.put(`/api/habitaciones/${editando.id}`, data);
      } else {
        await api.post('/api/habitaciones', data);
      }
      setModalOpen(false);
      api.get('/api/habitaciones')
        .then(res => setHabitaciones(res.data))
        .catch(err => console.error('Error al cargar habitaciones', err));
    } catch (err) {
      alert(err.response?.data?.error || 'Error al guardar');
    }
  };
  const estadoClass = (estado) => {
    if (estado === 'LIBRE') return 'tag-libre';
    if (estado === 'OCUPADA') return 'tag-ocupada';
    return 'tag-bloqueada';
  };
  if (!user) return null;
  return (
    <>
      <ProtectedHeader />
      <main className="habitaciones-page">
        {user?.rol === 'JEFE' && <JefeNav />}
        <div className="habitaciones-container">
          <div className="habitaciones-header">
            <h1>Habitaciones</h1>
            <button className="btn-crear" onClick={abrirCrear}>+ Crear habitación</button>
          </div>
          <div className="habitaciones-table-wrapper">
            <table className="habitaciones-table">
              <thead>
                <tr>
                  <th>Nº</th>
                  <th>Tipo</th>
                  <th>Estado</th>
                  <th>Pendiente limpieza</th>
                  <th>Averiada</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {habitaciones.length === 0 && (
                  <tr><td colSpan="6" className="table-empty">No hay habitaciones registradas</td></tr>
                )}
                {habitaciones.map((h) => (
                  <tr key={h.id}>
                    <td>{h.numero}</td>
                    <td>{h.tipoHabitacion?.nombre || '-'}</td>
                    <td><span className={`estado-tag ${estadoClass(h.estado)}`}>{h.estado}</span></td>
                    <td>{h.pendienteLimpieza ? 'Sí' : 'No'}</td>
                    <td>{h.averiada ? 'Sí' : 'No'}</td>
                    <td className="acciones-cell">
                      <button className="btn-accion btn-editar" onClick={() => abrirEditar(h)}>Editar</button>
                      <button className="btn-accion btn-eliminar" onClick={() => handleEliminar(h.id)}>Eliminar</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </main>
      <Footer />
      {modalOpen && (
        <HabitacionModal
          key={editando ? editando.id : 'nuevo'}
          habitacion={editando}
          onGuardar={handleGuardar}
          onCerrar={() => setModalOpen(false)}
        />
      )}
    </>
  );
}