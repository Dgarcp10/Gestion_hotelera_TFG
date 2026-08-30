import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/useAuth';
import { useToast } from '../contexts/useToast';
import { LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid, ResponsiveContainer } from 'recharts';
import api from '../services/api';
import ProtectedHeader from '../components/ProtectedHeader';
import JefeNav from '../components/JefeNav';
import Footer from '../components/Footer';
import '../components/Jefe.css';
import './DashboardJefe.css';
const MESES = ['Ene','Feb','Mar','Abr','May','Jun','Jul','Ago','Sep','Oct','Nov','Dic'];
const formatearFecha = (iso) => iso ? new Date(iso).toLocaleString('es-ES', { dateStyle: 'short', timeStyle: 'short' }) : '—';
function GraficoLinea({ data, color, titulo, dataKey, unidad, alto = 220, tick }) {
  return (
    <div className="grafico-card">
      <h3>{titulo}</h3>
      <ResponsiveContainer width="100%" height={alto}>
        <LineChart data={data} margin={{ top: 5, right: 20, bottom: 0, left: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#eee" />
          <XAxis dataKey="periodo" tickFormatter={tick} tick={{ fontSize: 12, fill: '#5a5a5a' }} />
          <YAxis tick={{ fontSize: 12, fill: '#5a5a5a' }} width={55} />
          <Tooltip formatter={(v) => [`${v}${unidad}`, titulo]} />
          <Line type="monotone" dataKey={dataKey} name={titulo} stroke={color} strokeWidth={2.5}
            dot={{ r: 3, fill: color }} isAnimationActive={false} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
export default function DashboardJefe() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();
  const anioActual = new Date().getFullYear();
  const [tab, setTab] = useState('graficos');
  const [anio, setAnio] = useState(anioActual);
  const [mes, setMes] = useState(new Date().getMonth() + 1);
  const [graficos, setGraficos] = useState(null);
  const d = new Date();
  const [desde, setDesde] = useState(new Date(d.getTime() - 3 * 86400000).toISOString().slice(0, 10));
  const [hasta, setHasta] = useState(d.toISOString().slice(0, 10));
  const [tipos, setTipos] = useState([]);
  const [habitaciones, setHabitaciones] = useState([]);
  const [tipoId, setTipoId] = useState('');
  const [numero, setNumero] = useState('');
  const [seguimiento, setSeguimiento] = useState(null);
  const [cargando, setCargando] = useState(false);
  useEffect(() => {
    if (!user) navigate('/login');
    else if (user.rol !== 'JEFE') navigate('/dashboard');
  }, [user, navigate]);
  useEffect(() => {
    if (!user) return;
    api.get('/api/tipos-habitacion').then(res => setTipos(res.data)).catch(() => {});
    api.get('/api/habitaciones').then(res => setHabitaciones(res.data)).catch(() => {});
  }, [user]);
  useEffect(() => {
    if (!user || tab !== 'graficos') return;
    let activo = true;
    Promise.resolve().then(() => setCargando(true));
    api.get('/api/dashboard/graficos', { params: { anio, mes } })
      .then(res => { if (activo) setGraficos(res.data); })
      .catch(err => { if (activo) toast.error(err.response?.data?.error || 'Error al cargar los gráficos'); })
      .finally(() => { if (activo) setCargando(false); });
    return () => { activo = false; };
  }, [user, tab, anio, mes, toast]);
  const cargarSeguimiento = () => {
    if (!desde || !hasta) { toast.error('Selecciona el rango de fechas'); return; }
    if (hasta < desde) { toast.error('La fecha "hasta" debe ser posterior o igual a "desde"'); return; }
    Promise.resolve().then(() => setCargando(true));
    const params = { desde, hasta };
    if (numero !== 'SIN_NUM' && numero) params.numero = numero;
    if (numero !== 'SIN_NUM' && tipoId) params.tipoHabitacionId = tipoId;
    api.get('/api/dashboard/seguimiento', { params })
      .then(res => setSeguimiento(res.data))
      .catch(err => toast.error(err.response?.data?.error || 'Error al cargar el seguimiento'))
      .finally(() => setCargando(false));
  };
  useEffect(() => {
    if (user) cargarSeguimiento();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);
  if (!user) return null;
  const anios = [];
  for (let a = anioActual; a >= anioActual - 3; a--) anios.push(a);
  const numeroOptions = habitaciones
    .filter(h => !tipoId || h.tipoHabitacion?.id === Number(tipoId))
    .map(h => String(h.numero));
  const ocAnual = graficos?.ocupacionAnual ?? [];
  const ocMensual = graficos?.ocupacionMensual ?? [];
  const gAnual = graficos?.gananciasAnuales ?? [];
  const gMensual = graficos?.gananciasMensuales ?? [];
  const mostrarSinNum = seguimiento && (numero === 'SIN_NUM');
  return (
    <>
      <ProtectedHeader />
      <main className="dashboard-jefe-page">
        <JefeNav />
        <div className="dashboard-jefe-container">
          <div className="dashboard-jefe-header">
            <h1>Dashboard del jefe</h1>
            <div className="dashboard-tabs">
              <button className={tab === 'graficos' ? 'active' : ''} onClick={() => setTab('graficos')}>Gráficos</button>
              <button className={tab === 'seguimiento' ? 'active' : ''} onClick={() => setTab('seguimiento')}>Seguimiento</button>
            </div>
          </div>
          {tab === 'graficos' && (
            <div className="filtros-row">
              <div className="form-group">
                <label>Año</label>
                <select value={anio} onChange={e => setAnio(Number(e.target.value))}>
                  {anios.map(a => <option key={a} value={a}>{a}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>Mes</label>
                <select value={mes} onChange={e => setMes(Number(e.target.value))}>
                  {MESES.map((m, i) => <option key={i + 1} value={i + 1}>{m}</option>)}
                </select>
              </div>
            </div>
          )}
          {cargando && <p className="table-empty">Cargando...</p>}
          {tab === 'graficos' && graficos && (
            <div className="graficos-grid">
              <GraficoLinea data={ocAnual} color="#c8a15a" titulo="Ocupación anual" dataKey="porcentaje" unidad=" %"
                tick={(p) => MESES[p - 1]} />
              <GraficoLinea data={ocMensual} color="#c8a15a" titulo={`Ocupación de ${MESES[mes - 1]} ${anio}`} dataKey="porcentaje" unidad=" %"
                tick={(p) => String(p)} />
              <GraficoLinea data={gAnual} color="#2e7d32" titulo="Ganancias anuales" dataKey="importe" unidad=" €"
                tick={(p) => MESES[p - 1]} />
              <GraficoLinea data={gMensual} color="#2e7d32" titulo={`Ganancias de ${MESES[mes - 1]} ${anio}`} dataKey="importe" unidad=" €"
                tick={(p) => String(p)} />
            </div>
          )}
          {tab === 'seguimiento' && (
            <>
              <div className="filtros-row">
                <div className="form-group">
                  <label>Desde</label>
                  <input type="date" value={desde} onChange={e => setDesde(e.target.value)} />
                </div>
                <div className="form-group">
                  <label>Hasta</label>
                  <input type="date" value={hasta} onChange={e => setHasta(e.target.value)} />
                </div>
                <div className="form-group">
                  <label>Tipo</label>
                  <select value={tipoId} onChange={e => { setTipoId(e.target.value); setNumero(''); }}>
                    <option value="">Todas</option>
                    {tipos.map(t => <option key={t.id} value={t.id}>{t.nombre}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label>Número</label>
                  <select value={numero} onChange={e => setNumero(e.target.value)}>
                    <option value="">Todos</option>
                    {numeroOptions.map(n => <option key={n} value={n}>Nº {n}</option>)}
                    <option value="SIN_NUM">Sin nº</option>
                  </select>
                </div>
                <div className="filtros-accion">
                  <button className="btn-guardar" onClick={cargarSeguimiento} disabled={cargando}>
                    {cargando ? 'Cargando...' : 'Aplicar filtros'}
                  </button>
                </div>
              </div>
              {seguimiento && (
                <div className="seguimiento-container">
                  {!mostrarSinNum && (
                    <div className="seguimiento-seccion">
                      <h3>Limpieza</h3>
                      {seguimiento.tareasLimpieza.length === 0 ? (
                        <p className="table-empty">Sin resultados</p>
                      ) : (
                        <table className="habitaciones-table">
                          <thead>
                            <tr><th>Habitación</th><th>Tipo hab.</th><th>Tarea</th><th>Completado por</th><th>Completada en</th><th>Creada en</th></tr>
                          </thead>
                          <tbody>
                            {seguimiento.tareasLimpieza.map(t => (
                              <tr key={t.id}>
                                <td>Nº {t.habitacionNumero}</td>
                                <td>{t.habitacionTipo}</td>
                                <td>{t.tipo}</td>
                                <td>{t.completadoPor || '—'}</td>
                                <td>{formatearFecha(t.completadaEn)}</td>
                                <td>{formatearFecha(t.creadoEn)}</td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      )}
                    </div>
                  )}
                  {!mostrarSinNum && (
                    <div className="seguimiento-seccion">
                      <h3>Mantenimiento</h3>
                      {seguimiento.averias.length === 0 ? (
                        <p className="table-empty">Sin resultados</p>
                      ) : (
                        <table className="habitaciones-table">
                          <thead>
                            <tr><th>Habitación</th><th>Tipo hab.</th><th>Gravedad</th><th>Estado</th><th>Reportada por</th><th>Resuelta por</th><th>Resuelta en</th><th>Creada en</th></tr>
                          </thead>
                          <tbody>
                            {seguimiento.averias.map(a => (
                              <tr key={a.id}>
                                <td>Nº {a.habitacionNumero}</td>
                                <td>{a.habitacionTipo}</td>
                                <td>{a.gravedad}</td>
                                <td>{a.estado}</td>
                                <td>{a.reportadoPor}</td>
                                <td>{a.resueltaPor || '—'}</td>
                                <td>{formatearFecha(a.resueltaEn)}</td>
                                <td>{formatearFecha(a.creadoEn)}</td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      )}
                    </div>
                  )}
                  <div className="seguimiento-seccion">
                    <h3>{mostrarSinNum ? 'Alertas sin nº de habitación' : 'Alertas'}</h3>
                    {mostrarSinNum ? (
                      seguimiento.notificacionesSinHabitacion.length === 0 ? <p className="table-empty">Sin resultados</p> : (
                        <table className="habitaciones-table">
                          <thead><tr><th>Tipo</th><th>Mensaje</th><th>Creada en</th></tr></thead>
                          <tbody>
                            {seguimiento.notificacionesSinHabitacion.map(n => (
                              <tr key={n.id}>
                                <td>{n.tipo}</td>
                                <td>{n.mensaje}</td>
                                <td>{formatearFecha(n.creadoEn)}</td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      )
                    ) : (
                      <>
                        {seguimiento.notificacionesConHabitacion.length === 0 ? (
                          <p className="table-empty">Sin resultados</p>
                        ) : (
                          <table className="habitaciones-table">
                            <thead><tr><th>Habitación</th><th>Tipo</th><th>Mensaje</th><th>Creada en</th></tr></thead>
                            <tbody>
                              {seguimiento.notificacionesConHabitacion.map(n => (
                                <tr key={n.id}>
                                  <td>Nº {n.habitacionNumero}</td>
                                  <td>{n.tipo}</td>
                                  <td>{n.mensaje}</td>
                                  <td>{formatearFecha(n.creadoEn)}</td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        )}
                        {!tipoId && !numero && seguimiento.notificacionesSinHabitacion.length > 0 && (
                          <div style={{ marginTop: 16 }}>
                            <h4>Alertas sin nº de habitación</h4>
                            <table className="habitaciones-table">
                              <thead><tr><th>Tipo</th><th>Mensaje</th><th>Creada en</th></tr></thead>
                              <tbody>
                                {seguimiento.notificacionesSinHabitacion.map(n => (
                                  <tr key={n.id}>
                                    <td>{n.tipo}</td>
                                    <td>{n.mensaje}</td>
                                    <td>{formatearFecha(n.creadoEn)}</td>
                                  </tr>
                                ))}
                              </tbody>
                            </table>
                          </div>
                        )}
                      </>
                    )}
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      </main>
      <Footer />
    </>
  );
}