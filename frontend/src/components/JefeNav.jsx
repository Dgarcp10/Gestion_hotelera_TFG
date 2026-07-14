import { Link, useLocation } from 'react-router-dom';
import './Jefe.css';
const links = [
  { to: '/jefe/habitaciones', label: 'Habitaciones' },
];
export default function JefeNav() {
  const location = useLocation();
  return (
    <nav className="jefe-nav">
      {links.map((link) => (
        <Link
          key={link.to}
          to={link.to}
          className={`jefe-nav-link${location.pathname.startsWith(link.to) ? ' active' : ''}`}
        >
          {link.label}
        </Link>
      ))}
    </nav>
  );
}