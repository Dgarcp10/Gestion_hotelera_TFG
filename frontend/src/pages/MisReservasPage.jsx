import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/useAuth';
import api from '../services/api';
import ProtectedHeader from '../components/ProtectedHeader';
import UserNav from '../components/UserNav';
import Footer from '../components/Footer';
import '../components/Jefe.css';
import '../components/User.css';
export default function MisReservasPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [reservas, setReservas] = useState([]);
  useEffect(() => {
    if (!user) navigate('/login');
  }, [user, navigate]);
  useEffect(() => {
    if (user) {
      api.get('/api/reservas/mis-reservas')
        .then(res => setReservas(res.data))
        .catch(err => console.error('Error al cargar reservas', err));
    }
  }, [user]);
  const estadoClass = (estado) => {
    if (estado === 'PENDIENTE') return 'tag-pendiente';
    if (estado === 'EN_CURSO') return 'tag-en_curso';
    if (estado === 'FINALIZADA') return 'tag-finalizada';
    return 'tag-cancelada';
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
                </tr>
              </thead>
              <tbody>
                {reservas.length === 0 && (
                  <tr><td colSpan="5" className="table-empty">No tienes reservas</td></tr>
                )}
                {reservas.map((r) => (
                  <tr key={r.id}>
                    <td>{r.tipoHabitacion?.nombre || '-'}</td>
                    <td>{r.fechaEntrada}</td>
                    <td>{r.fechaSalida}</td>
                    <td><span className={`estado-tag ${estadoClass(r.estado)}`}>{r.estado}</span></td>
                    <td>{r.precioTotal} €</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </main>
      <Footer />
    </>
  );
}