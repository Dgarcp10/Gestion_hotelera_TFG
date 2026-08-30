import { Link, useLocation } from 'react-router-dom';
import NotificacionesCampana from './NotificacionesCampana';
import './Limpieza.css';
const links = [
  { to: '/trabajo/averias', label: 'Averías' },
];
export default function MantenimientoNav() {
  const location = useLocation();
  return (
    <nav className="limpieza-nav">
      {links.map((link) => (
        <Link
          key={link.to}
          to={link.to}
          className={`limpieza-nav-link${location.pathname.startsWith(link.to) ? ' active' : ''}`}
        >
          {link.label}
        </Link>
      ))}
      <NotificacionesCampana />
    </nav>
  );
}