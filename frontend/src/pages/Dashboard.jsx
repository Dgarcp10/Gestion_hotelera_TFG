import { useAuth } from '../contexts/useAuth';
import { useNavigate } from 'react-router-dom';
import { useEffect } from 'react';
import ProtectedHeader from '../components/ProtectedHeader';
import Footer from '../components/Footer';
import './Dashboard.css';
export default function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  useEffect(() => {
    if (!user) navigate('/login');
  }, [user, navigate]);
  if (!user) return null;
  const rolMap = {
    JEFE: 'Jefe',
    RECEPCION: 'Recepción',
    LIMPIEZA: 'Limpieza',
    MANTENIMIENTO: 'Mantenimiento',
    USUARIO: 'Usuario',
  };
  return (
    <>
      <ProtectedHeader />
      <main className="dashboard-page">
        <div className="dashboard-card">
          <h1>Bienvenido, {user.username}</h1>
          <p className="dashboard-rol">{rolMap[user.rol] || user.rol}</p>
        </div>
      </main>
      <Footer />
    </>
  );
}