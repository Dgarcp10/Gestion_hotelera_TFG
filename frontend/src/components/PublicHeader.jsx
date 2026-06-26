import { Link, useLocation } from 'react-router-dom';
const HOTEL_NAME = import.meta.env.VITE_HOTEL_NAME || 'Hotel';
const LOGO_URL = import.meta.env.VITE_LOGO_URL || null;
export default function PublicHeader() {
  const { pathname } = useLocation();
  return (
    <header className="public-header">
      <div className="header-left">
        {LOGO_URL && <img src={LOGO_URL} alt={HOTEL_NAME} className="header-logo" />}
        <span className="header-hotel-name">{HOTEL_NAME}</span>
      </div>
      <nav className="header-right">
        {pathname !== '/login' && <Link to="/login">Iniciar sesión</Link>}
        {pathname !== '/register' && <Link to="/register">Registrarse</Link>}
      </nav>
    </header>
  );
}