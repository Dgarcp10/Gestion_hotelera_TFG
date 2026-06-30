import { Link, useLocation } from 'react-router-dom';
import './PublicHeader.css';

const LOGO_URL = import.meta.env.VITE_LOGO_URL || null;

export default function PublicHeader() {
  const { pathname } = useLocation();

  return (
    <header className="public-header">
      <div className="ph-left">
        {LOGO_URL && (
          <img src={LOGO_URL} alt="Hotel Villa de Lerma" className="ph-logo" />
        )}
        <div className="ph-brand">
          <span className="ph-brand-line ph-brand-line--top">Hotel</span>
          <span className="ph-brand-line">Villa de</span>
          <span className="ph-brand-line">Lerma</span>
        </div>
      </div>
      <nav className="ph-right">
        {pathname !== '/login' && (
          <Link to="/login" className="ph-btn ph-btn-outline">
            Iniciar sesión
          </Link>
        )}
        {pathname !== '/register' && (
          <Link to="/register" className="ph-btn ph-btn-gold">
            Registrarse
          </Link>
        )}
      </nav>
    </header>
  );
}
