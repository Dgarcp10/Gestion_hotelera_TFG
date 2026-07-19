import { Link, useLocation } from 'react-router-dom';
import './Recepcion.css';
const links = [
  { to: '/recepcion/check-in', label: 'Check-in' },
];
export default function RecepcionNav() {
  const location = useLocation();
  return (
    <nav className="recepcion-nav">
      {links.map((link) => (
        <Link
          key={link.to}
          to={link.to}
          className={`recepcion-nav-link${location.pathname.startsWith(link.to) ? ' active' : ''}`}
        >
          {link.label}
        </Link>
      ))}
    </nav>
  );
}