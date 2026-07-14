import { useState, useEffect } from 'react';
import api from '../services/api';
export default function HabitacionModal({ habitacion, onGuardar, onCerrar }) {
  const [tipos, setTipos] = useState([]);
  const esEdicion = !!habitacion;
  const [form, setForm] = useState({
    numero: habitacion?.numero || '',
    tipoHabitacionId: habitacion?.tipoHabitacion?.id || '',
    pendienteLimpieza: habitacion?.pendienteLimpieza || false,
    averiada: habitacion?.averiada || false,
  });
  useEffect(() => {
    api.get('/api/tipos-habitacion').then((res) => setTipos(res.data)).catch(() => {});
  }, []);
  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm((prev) => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
  };
  const handleSubmit = (e) => {
    e.preventDefault();
    const payload = {
      numero: parseInt(form.numero, 10),
      tipoHabitacion: { id: parseInt(form.tipoHabitacionId, 10) },
      pendienteLimpieza: form.pendienteLimpieza,
      averiada: form.averiada,
    };
    onGuardar(payload);
  };
  return (
    <div className="modal-overlay" onClick={onCerrar}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <h2>{esEdicion ? 'Editar habitación' : 'Crear habitación'}</h2>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Número</label>
            <input type="number" name="numero" value={form.numero} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Tipo</label>
            <select name="tipoHabitacionId" value={form.tipoHabitacionId} onChange={handleChange} required>
              <option value="">Seleccionar tipo</option>
              {tipos.map((t) => (
                <option key={t.id} value={t.id}>{t.nombre}</option>
              ))}
            </select>
          </div>
          <div className="form-group checkbox-group">
            <label>
              <input type="checkbox" name="pendienteLimpieza" checked={form.pendienteLimpieza} onChange={handleChange} />
              Pendiente de limpieza
            </label>
          </div>
          <div className="form-group checkbox-group">
            <label>
              <input type="checkbox" name="averiada" checked={form.averiada} onChange={handleChange} />
              Averiada
            </label>
          </div>
          <div className="modal-actions">
            <button type="button" className="btn-cancelar" onClick={onCerrar}>Cancelar</button>
            <button type="submit" className="btn-guardar">{esEdicion ? 'Guardar cambios' : 'Crear'}</button>
          </div>
        </form>
      </div>
    </div>
  );
}