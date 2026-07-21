import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/useAuth';
import api from '../services/api';
import ProtectedHeader from '../components/ProtectedHeader';
import JefeNav from '../components/JefeNav';
import Footer from '../components/Footer';
import EmpleadoModal from '../components/EmpleadoModal';
import '../components/Jefe.css';
export default function GestionEmpleadosPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [usuarios, setUsuarios] = useState([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [passwordGenerado, setPasswordGenerado] = useState(null);
  useEffect(() => {
    if (!user) navigate('/login');
  }, [user, navigate]);
  useEffect(() => {
    if (user) {
      api.get('/api/usuarios')
        .then(res => setUsuarios(res.data))
        .catch(err => console.error('Error al cargar usuarios', err));
    }
  }, [user]);
  const handleCrear = async (data) => {
    try {
      const res = await api.post('/api/usuarios', data);
      setPasswordGenerado(res.data.passwordGenerado);
      api.get('/api/usuarios')
        .then(r => setUsuarios(r.data))
        .catch(err => console.error('Error al cargar usuarios', err));
    } catch (err) {
      alert(err.response?.data?.error || 'Error al crear empleado');
    }
  };
  const handleCerrarModal = () => {
    setModalOpen(false);
    setPasswordGenerado(null);
  };
  if (!user) return null;
  return (
    <>
      <ProtectedHeader />
      <main className="habitaciones-page">
        {user?.rol === 'JEFE' && <JefeNav />}
        <div className="habitaciones-container">
          <div className="habitaciones-header">
            <h1>Empleados</h1>
            <button className="btn-crear" onClick={() => setModalOpen(true)}>+ Crear empleado</button>
          </div>
          <div className="habitaciones-table-wrapper">
            <table className="habitaciones-table">
              <thead>
                <tr>
                  <th>Username</th>
                  <th>Nombre</th>
                  <th>Email</th>
                  <th>Rol</th>
                  <th>Teléfono</th>
                </tr>
              </thead>
              <tbody>
                {usuarios.filter(u => u.rol !== 'USUARIO').length === 0 && (
                  <tr><td colSpan="5" className="table-empty">No hay usuarios registrados</td></tr>
                )}
                {usuarios.filter(u => u.rol !== 'USUARIO').map((u) => (
                  <tr key={u.id}>
                    <td>{u.username}</td>
                    <td>{u.nombre} {u.apellido}</td>
                    <td>{u.email}</td>
                    <td>{u.rol}</td>
                    <td>{u.telefono || '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </main>
      <Footer />
      {modalOpen && (
        <EmpleadoModal
          key={passwordGenerado ? 'creado' : 'nuevo'}
          passwordGenerado={passwordGenerado}
          onGuardar={handleCrear}
          onCerrar={handleCerrarModal}
        />
      )}
    </>
  );
}