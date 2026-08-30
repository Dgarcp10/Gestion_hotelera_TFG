import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/useAuth';
import { useToast } from '../contexts/useToast';
import api from '../services/api';
import ProtectedHeader from '../components/ProtectedHeader';
import LimpiezaNav from '../components/LimpiezaNav';
import JefeNav from '../components/JefeNav';
import RecepcionNav from '../components/RecepcionNav';
import MantenimientoNav from '../components/MantenimientoNav';
import Footer from '../components/Footer';
import '../components/Jefe.css';
import '../components/Limpieza.css';
import '../components/Recepcion.css';
import { Icon } from '@mdi/react';
import { mdiCheck, mdiPencil } from '@mdi/js';
export default function AveriasPage() {
  const { user } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [averias, setAverias] = useState([]);
  const [habitaciones, setHabitaciones] = useState([]);
  const [filtro, setFiltro] = useState('ABIERTA');
  const [modalCrear, setModalCrear] = useState(false);
  const [modalResolver, setModalResolver] = useState(null);
  const [modalEditar, setModalEditar] = useState(null);
  const [numeroInput, setNumeroInput] = useState('');
  const [gravedadInput, setGravedadInput] = useState('LEVE');
  const [notasInput, setNotasInput] = useState('');
  const [editGravedad, setEditGravedad] = useState('LEVE');
  const [editNotas, setEditNotas] = useState('');
  const [errorNumero, setErrorNumero] = useState('');
  const [fechaInicioInput, setFechaInicioInput] = useState('');
  const [fechaFinInput, setFechaFinInput] = useState('');
  const [errorFechas, setErrorFechas] = useState('');
  const [confirmandoSinReubicacion, setConfirmandoSinReubicacion] = useState(null);
  const [editFechaInicioInput, setEditFechaInicioInput] = useState('');
  const [editFechaFinInput, setEditFechaFinInput] = useState('');
  const [editErrorFechas, setEditErrorFechas] = useState('');
  const [resultado, setResultado] = useState(null);
  const cargarDatos = useCallback(() => {
    api.get('/api/averias', { params: { estado: filtro } })
      .then(res => setAverias(res.data)).catch(() => {});
    api.get('/api/habitaciones').then(res => setHabitaciones(res.data)).catch(() => {});
  }, [filtro]);
  useEffect(() => {
    if (!user) navigate('/login');
  }, [user, navigate]);
  useEffect(() => {
    if (user) cargarDatos();
  }, [user, cargarDatos]);
  const obtenerPreview = async (habitacionId, fechaInicio, fechaFin) => {
    const res = await api.get('/api/averias/preview', { params: { habitacionId, fechaInicio, fechaFin } });
    return res.data;
  };
  const ejecutar = async ({ tipo, id, params, preview, numeroHabitacion }) => {
    try {
      if (tipo === 'crear') {
        await api.post('/api/averias', null, { params });
      } else {
        await api.put(`/api/averias/${id}`, null, { params });
      }
      if (!preview) {
        toast.success(tipo === 'crear' ? 'Avería registrada.' : 'Avería actualizada.');
      } else if (!preview.hayHuesped) {
        toast.success(tipo === 'crear' ? 'Avería grave registrada. Habitación bloqueada.' : 'Avería actualizada.');
      } else if (preview.hayHueco) {
        setResultado({
          mensaje: `Bloqueo de la habitación ${numeroHabitacion}. Huésped ${preview.huesped} reubicado de la ${numeroHabitacion} a la ${preview.habitacionDestino}${preview.categoriaSuperior ? ' (categoría superior).' : '.'}`,
        });
      } else {
        setResultado({
          mensaje: `Bloqueo de la habitación ${numeroHabitacion}. El huésped ${preview.huesped} no ha podido ser reubicado: no hay habitación disponible. Es necesario realizar una reubicación manual.`,
        });
      }
      if (tipo === 'crear') cerrarModalCrear();
      else setModalEditar(null);
      cargarDatos();
    } catch (err) {
      toast.error(err.response?.data?.error || (tipo === 'crear' ? 'Error al registrar la avería' : 'Error al actualizar la avería'));
    }
  };
  const iniciarPrevio = async ({ tipo, id, habitacionId, params, numeroHabitacion }) => {
    if (params.gravedad !== 'GRAVE') {
      ejecutar({ tipo, id, params, preview: null, numeroHabitacion });
      return;
    }
    try {
      const preview = await obtenerPreview(habitacionId, params.fechaInicio, params.fechaFin);
      if (preview.yaBloqueada) {
        toast.error('La habitación ya tiene un bloqueo activo en ese periodo.');
        return;
      }
      if (preview.hayHuesped && !preview.hayHueco) {
        setConfirmandoSinReubicacion({ tipo, id, params, numeroHabitacion, huesped: preview.huesped });
        return;
      }
      ejecutar({ tipo, id, params, preview, numeroHabitacion });
    } catch (err) {
      toast.error(err.response?.data?.error || 'Error al preparar el bloqueo');
    }
  };
  const confirmarBloqueoSinReubicacion = async () => {
    if (!confirmandoSinReubicacion) return;
    const { tipo, id, params, numeroHabitacion, huesped } = confirmandoSinReubicacion;
    const preview = { hayHuesped: true, huesped, hayHueco: false, categoriaSuperior: false, habitacionDestino: null, yaBloqueada: false };
    await ejecutar({ tipo, id, params: { ...params, confirmarSinReubicacion: true }, preview, numeroHabitacion });
    setConfirmandoSinReubicacion(null);
  };
  const cerrarModalCrear = () => {
    setModalCrear(false);
    setNumeroInput('');
    setGravedadInput('LEVE');
    setNotasInput('');
    setFechaInicioInput('');
    setFechaFinInput('');
    setErrorNumero('');
    setErrorFechas('');
  };
  const handleCrear = async () => {
    const numero = Number(numeroInput);
    const hab = habitaciones.find(h => h.numero === numero);
    if (!hab) {
      setErrorNumero('No existe una habitación con ese número');
      return;
    }
    if (gravedadInput === 'GRAVE') {
      const inicio = fechaInicioInput || new Date().toISOString().slice(0, 10);
      if (!fechaFinInput) {
        setErrorFechas('Indica una fecha fin estimada');
        return;
      }
      if (fechaFinInput < inicio) {
        setErrorFechas('La fecha fin no puede ser anterior a la de inicio');
        return;
      }
    }
    const params = { habitacionId: hab.id, gravedad: gravedadInput };
    if (notasInput.trim()) params.notas = notasInput.trim();
    if (gravedadInput === 'GRAVE') {
      params.fechaInicio = fechaInicioInput || new Date().toISOString().slice(0, 10);
      params.fechaFin = fechaFinInput;
    }
    iniciarPrevio({ tipo: 'crear', habitacionId: hab.id, params, numeroHabitacion: numero });
  };
  const handleResolver = async (id) => {
    try {
      await api.post(`/api/averias/${id}/resolver`);
      setModalResolver(null);
      cargarDatos();
    } catch (err) {
      toast.error(err.response?.data?.error || 'Error al resolver la avería');
    }
  };
  const handleGuardarEdicion = async () => {
    const esGrave = editGravedad === 'GRAVE';
    const id = modalEditar.id;
    const params = { gravedad: editGravedad };
    if (editNotas.trim()) params.notas = editNotas.trim();
    if (esGrave) {
      const inicio = editFechaInicioInput || new Date().toISOString().slice(0, 10);
      if (!editFechaFinInput) {
        setEditErrorFechas('Indica una fecha fin estimada');
        return;
      }
      if (editFechaFinInput < inicio) {
        setEditErrorFechas('La fecha fin no puede ser anterior a la de inicio');
        return;
      }
      params.fechaInicio = inicio;
      params.fechaFin = editFechaFinInput;
    }
    if (esGrave && !modalEditar.bloqueo) {
      iniciarPrevio({ tipo: 'editar', id, habitacionId: modalEditar.habitacion?.id, params, numeroHabitacion: modalEditar.habitacion?.numero });
    } else {
      ejecutar({ tipo: 'editar', id, params, preview: null, numeroHabitacion: modalEditar.habitacion?.numero });
    }
  };
  const abrirEditar = (a) => {
    setEditGravedad(a.gravedad);
    setEditNotas(a.notas || '');
    setModalEditar(a);
    setEditFechaInicioInput(a.bloqueo?.fechaInicio || '');
    setEditFechaFinInput(a.bloqueo?.fechaFin || '');
    setEditErrorFechas('');
  };
  const gravedadLabel = (g) => g === 'GRAVE' ? 'Grave' : 'Leve';
  if (!user) return null;
  return (
    <>
      <ProtectedHeader />
      <main className="limpieza-page">
        {(user?.rol === 'LIMPIEZA' && <LimpiezaNav />) ||
        (user?.rol === 'MANTENIMIENTO' && <MantenimientoNav />) ||
        (user?.rol === 'RECEPCION' && <RecepcionNav />) ||
        (user?.rol === 'JEFE' && <JefeNav />)}
        <div className="limpieza-container">
          <div className="limpieza-header">
            <h1>Averías</h1>
            <div>
              <button
                className={filtro === 'ABIERTA' ? 'btn-guardar' : 'btn-cancelar'}
                onClick={() => setFiltro(filtro === 'ABIERTA' ? 'RESUELTA' : 'ABIERTA')}
              >
                {filtro === 'ABIERTA' ? 'Ver resueltas' : 'Ver abiertas'}
              </button>{' '}
              {filtro === 'ABIERTA' && (
                <button className="btn-crear" onClick={() => { setFechaInicioInput(new Date().toISOString().slice(0, 10)); setModalCrear(true); }}>Registrar avería</button>
              )}
            </div>
          </div>
          <div className="limpieza-section-title">
            {filtro === 'ABIERTA' ? `Abiertas (${averias.length})` : `Resueltas (${averias.length})`}
          </div>
          <div className="habitaciones-table-wrapper">
            <table className="habitaciones-table">
              <thead>
                <tr>
                  <th>Nº</th>
                  <th>Gravedad</th>
                  <th>Notas</th>
                  <th>Reportada por</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {averias.length === 0 && (
                  <tr><td colSpan="5" className="table-empty">No hay averías {filtro === 'ABIERTA' ? 'abiertas' : 'resueltas'}</td></tr>
                )}
                {averias.map(a => (
                  <tr key={a.id}>
                    <td>{a.habitacion?.numero}</td>
                    <td>{gravedadLabel(a.gravedad)}</td>
                    <td>{a.notas || '-'}</td>
                    <td>{a.reportadoPor?.username || '-'}</td>
                    <td>
                      {filtro === 'ABIERTA' && (
                        <>
                          <button
                            className="limpiar-btn"
                            aria-label={`Editar avería de la habitación ${a.habitacion?.numero}`}
                            title="Editar"
                            onClick={() => abrirEditar(a)}
                          >
                            <Icon path={mdiPencil} size={1} />
                          </button>{' '}
                          <button
                            className="limpiar-btn"
                            aria-label={`Resolver avería de la habitación ${a.habitacion?.numero}`}
                            title="Resolver"
                            onClick={() => setModalResolver(a)}
                          >
                            <Icon path={mdiCheck} size={1} />
                          </button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </main>
      <Footer />
      {modalCrear && (
        <div className="modal-overlay" onClick={() => setModalCrear(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <h2>Registrar avería</h2>
            <div className="form-group">
              <label>Nº de habitación</label>
              <input
                type="number"
                value={numeroInput}
                onChange={e => { setNumeroInput(e.target.value); setErrorNumero(''); }}
                required
              />
            </div>
            {errorNumero && <p className="form-error">{errorNumero}</p>}
            <div className="form-group">
              <label>Gravedad</label>
              <select value={gravedadInput} onChange={e => { const g = e.target.value; setGravedadInput(g); if (g === 'GRAVE' && !fechaInicioInput) setFechaInicioInput(new Date().toISOString().slice(0, 10)); }}>                <option value="LEVE">Leve</option>
                <option value="GRAVE">Grave</option>
              </select>
            </div>
            {gravedadInput === 'GRAVE' && (
              <>
                <div className="form-group">
                  <label>Fecha inicio</label>
                  <input
                    type="date"
                    value={fechaInicioInput}
                    onChange={e => { setFechaInicioInput(e.target.value); setErrorFechas(''); }}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Fecha fin estimada</label>
                  <input
                    type="date"
                    value={fechaFinInput}
                    onChange={e => { setFechaFinInput(e.target.value); setErrorFechas(''); }}
                    required
                  />
                </div>
                {errorFechas && <p className="form-error">{errorFechas}</p>}
              </>
            )}
            <div className="form-group">
              <label>Notas</label>
              <textarea
                value={notasInput}
                onChange={e => setNotasInput(e.target.value)}
                placeholder="Describe la avería"
              />
            </div>
            <div className="modal-actions">
              <button className="btn-cancelar" onClick={cerrarModalCrear}>Cancelar</button>
              <button className="btn-guardar" onClick={handleCrear}>Registrar</button>
            </div>
          </div>
        </div>
      )}
      {modalResolver && (
        <div className="modal-overlay" onClick={() => setModalResolver(null)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <h2>Confirmar resolución</h2>
            <p style={{ marginBottom: 24 }}>
              ¿Marcar como resuelta la avería de la habitación <strong>{modalResolver.habitacion?.numero}</strong>?
            </p>
            <div className="modal-actions">
              <button className="btn-cancelar" onClick={() => setModalResolver(null)}>Cancelar</button>
              <button className="btn-guardar" onClick={() => handleResolver(modalResolver.id)}>
                Sí, marcar como resuelta
              </button>
            </div>
          </div>
        </div>
      )}
      {confirmandoSinReubicacion && (
        <div className="modal-overlay" onClick={() => setConfirmandoSinReubicacion(null)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <h2>Bloqueo sin reubicación</h2>
            <p style={{ marginBottom: 24 }}>
              No hay habitación disponible para reubicar a{' '}
              <strong>{confirmandoSinReubicacion.huesped}</strong> de la habitación{' '}
              <strong>{confirmandoSinReubicacion.numeroHabitacion}</strong>. ¿Aplicar el bloqueo igualmente? Es necesario realizar una reubicación manual.
            </p>
            <div className="modal-actions">
              <button className="btn-cancelar" onClick={() => setConfirmandoSinReubicacion(null)}>Cancelar</button>
              <button className="btn-guardar" onClick={confirmarBloqueoSinReubicacion}>
                Sí, bloquear sin reubicar
              </button>
            </div>
          </div>
        </div>
      )}
      {modalEditar && (
        <div className="modal-overlay" onClick={() => setModalEditar(null)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <h2>Editar avería (hab. {modalEditar.habitacion?.numero})</h2>
            <div className="form-group">
              <label>Gravedad</label>
              <select value={editGravedad} onChange={e => setEditGravedad(e.target.value)}>
                <option value="LEVE">Leve</option>
                <option value="GRAVE">Grave</option>
              </select>
            </div>
            {editGravedad === 'GRAVE' && (
              <>
                <div className="form-group">
                  <label>Fecha inicio</label>
                  <input
                    type="date"
                    value={editFechaInicioInput}
                    onChange={e => { setEditFechaInicioInput(e.target.value); setEditErrorFechas(''); }}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Fecha fin estimada</label>
                  <input
                    type="date"
                    value={editFechaFinInput}
                    onChange={e => { setEditFechaFinInput(e.target.value); setEditErrorFechas(''); }}
                    required
                  />
                </div>
                {editErrorFechas && <p className="form-error">{editErrorFechas}</p>}
              </>
            )}
            <div className="form-group">
              <label>Notas</label>
              <textarea
                value={editNotas}
                onChange={e => setEditNotas(e.target.value)}
              />
            </div>
            <div className="modal-actions">
              <button className="btn-cancelar" onClick={() => setModalEditar(null)}>Cancelar</button>
              <button className="btn-guardar" onClick={handleGuardarEdicion}>Guardar</button>
            </div>
          </div>
        </div>
      )}
      {resultado && (
        <div className="modal-overlay" onClick={() => setResultado(null)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <h2>Bloqueo aplicado</h2>
            <p style={{ marginBottom: 24 }}>{resultado.mensaje}</p>
            <div className="modal-actions">
              <button className="btn-guardar" onClick={() => setResultado(null)}>Aceptar</button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}