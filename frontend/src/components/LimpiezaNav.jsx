import { Link, useLocation } from 'react-router-dom';
import './Limpieza.css';
const links = [
  { to: '/limpieza', label: 'Limpieza' },
];
export default function LimpiezaNav() {
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
    </nav>
  );
}