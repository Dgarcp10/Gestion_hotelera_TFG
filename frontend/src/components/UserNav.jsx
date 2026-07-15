import { Link, useLocation } from 'react-router-dom';
import './User.css';
const links = [
  { to: '/mis-reservas', label: 'Mis reservas' },
];
export default function UserNav() {
  const location = useLocation();
  return (
    <nav className="user-nav">
      {links.map((link) => (
        <Link
          key={link.to}
          to={link.to}
          className={`user-nav-link${location.pathname.startsWith(link.to) ? ' active' : ''}`}
        >
          {link.label}
        </Link>
      ))}
    </nav>
  );
}