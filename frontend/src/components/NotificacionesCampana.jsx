import { useState, useEffect, useRef } from 'react';
import { Icon } from '@mdi/react';
import { mdiBell, mdiBellOutline } from '@mdi/js';
import api from '../services/api';
import { useAuth } from '../contexts/useAuth';
import './Notificaciones.css';
export default function NotificacionesCampana() {
  const [count, setCount] = useState(0);
  const [notificaciones, setNotificaciones] = useState([]);
  const [abierta, setAbierta] = useState(false);
  const ref = useRef(null);
  const { user } = useAuth();
  const puedeMarcarLeida = user?.rol === 'JEFE' || user?.rol === 'RECEPCION';
  useEffect(() => {
    const cargar = () => {
      api.get('/api/notificaciones/no-leidas').then(res => setCount(res.data)).catch(() => {});
    };
    cargar();
    const t = setInterval(cargar, 30000);
    return () => clearInterval(t);
  }, []);
  useEffect(() => {
    const cerrar = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setAbierta(false);
    };
    document.addEventListener('mousedown', cerrar);
    return () => document.removeEventListener('mousedown', cerrar);
  }, []);
  const toggle = () => {
    if (!abierta) {
      api.get('/api/notificaciones').then(res => setNotificaciones(res.data)).catch(() => {});
    }
    setAbierta(!abierta);
  };
  const marcarLeida = (id) => {
    api.post(`/api/notificaciones/${id}/leida`).then(() => {
      setNotificaciones(prev => prev.map(n => n.id === id ? { ...n, leida: true } : n));
      setCount(c => Math.max(0, c - 1));
    }).catch(() => {});
  };
  return (
    <div className="campana-wrap" ref={ref}>
      <button className="campana-btn" aria-label="Notificaciones" onClick={toggle}>
        <Icon path={abierta ? mdiBell : mdiBellOutline} size={1} />
        {count > 0 && <span className="campana-badge">{count > 99 ? '99+' : count}</span>}
      </button>
      {abierta && (
        <div className="campana-dropdown">
          {notificaciones.length === 0 && <p className="notif-vacio">Sin notificaciones</p>}
          {notificaciones.map(n => (
            <div
              key={n.id}
              className={`notif-item${n.leida ? '' : ' notif-no-leida'}`}
              onClick={() => { if (puedeMarcarLeida && !n.leida) marcarLeida(n.id); }}
            >
              <span className="notif-mensaje">{n.mensaje}</span>
              {!n.leida && puedeMarcarLeida && <span className="notif-marcar">Marcar leída</span>}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}