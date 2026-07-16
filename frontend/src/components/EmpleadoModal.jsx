import { useState } from 'react';
export default function EmpleadoModal({ passwordGenerado, onGuardar, onCerrar }) {
  const [form, setForm] = useState({
    username: '',
    email: '',
    passwordHash: '',
    nombre: '',
    apellido: '',
    telefono: '',
    rol: '',
  });
  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };
  const handleSubmit = (e) => {
    e.preventDefault();
    onGuardar(form);
  };
  return (
    <div className="modal-overlay" onClick={onCerrar}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        {passwordGenerado ? (
          <>
            <h2>Empleado creado</h2>
            <p style={{ marginBottom: 12 }}>Contraseña temporal:</p>
            <div style={{
              background: '#fff3e0',
              padding: '16px 20px',
              borderRadius: 8,
              fontFamily: 'monospace',
              fontSize: 20,
              fontWeight: 600,
              color: '#e65100',
              textAlign: 'center',
              marginBottom: 8,
            }}>
              {passwordGenerado}
            </div>
            <p style={{ fontSize: 13, color: '#666', marginBottom: 24 }}>
              Esta contraseña solo se muestra una vez. Cópiala y entrégala al empleado.
            </p>
            <div className="modal-actions">
              <button className="btn-guardar" onClick={onCerrar}>Cerrar</button>
            </div>
          </>
        ) : (
          <>
            <h2>Crear empleado</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Username</label>
                <input name="username" value={form.username} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>Email</label>
                <input type="email" name="email" value={form.email} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>Nombre</label>
                <input name="nombre" value={form.nombre} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>Apellido</label>
                <input name="apellido" value={form.apellido} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>Teléfono</label>
                <input name="telefono" value={form.telefono} onChange={handleChange} />
              </div>
              <div className="form-group">
                <label>Rol</label>
                <select name="rol" value={form.rol} onChange={handleChange} required>
                  <option value="">Seleccionar</option>
                  <option value="RECEPCION">Recepción</option>
                  <option value="LIMPIEZA">Limpieza</option>
                  <option value="MANTENIMIENTO">Mantenimiento</option>
                </select>
              </div>
              <div className="modal-actions">
                <button type="button" className="btn-cancelar" onClick={onCerrar}>Cancelar</button>
                <button type="submit" className="btn-guardar">Crear empleado</button>
              </div>
            </form>
          </>
        )}
      </div>
    </div>
  );
}