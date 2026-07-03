import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/useAuth';
import './Headers.css';
const LOGO_URL = import.meta.env.VITE_LOGO_URL || null;
export default function ProtectedHeader() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const handleLogout = () => {
    logout();
    navigate('/');
  };
  const inicial = user?.username?.charAt(0)?.toUpperCase() || '?';
  return (
    <header className="protected-header">
      <Link to="/dashboard" className="ph-left-link">
        <div className="ph-left">
          {LOGO_URL && <img src={LOGO_URL} alt="Hotel Villa de Lerma" className="ph-logo" />}
          <div className="ph-brand">
            <span className="ph-brand-line ph-brand-line--top">Hotel</span>
            <span className="ph-brand-line">Villa de</span>
            <span className="ph-brand-line">Lerma</span>
          </div>
        </div>
      </Link>
      <div className="ph-right">
        <div className="ph-user">
          <div className="ph-avatar">{inicial}</div>
          <span className="ph-username">{user?.username}</span>
        </div>
        <button onClick={handleLogout} className="ph-btn ph-btn-logout">
          Cerrar sesión
        </button>
      </div>
    </header>
  );
}