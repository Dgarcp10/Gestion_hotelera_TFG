import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/useAuth';
import api from '../services/api';
import PublicHeader from '../components/PublicHeader';
import Footer from '../components/Footer';
import './Auth.css';
export default function Register() {
  const navigate = useNavigate();
  const { setAuth } = useAuth();
  const [error, setError] = useState('');
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const data = new FormData(e.target);
    const password = data.get('password');
    const confirm = data.get('confirmPassword');
    if (password !== confirm) {
        setError('Las contraseñas no coinciden');
        return;
    }
    try {
      const res = await api.post('/api/auth/register', {
        username: data.get('username'),
        email: data.get('email'),
        password: data.get('password'),
        nombre: data.get('nombre'),
        apellido: data.get('apellido'),
      });
      const userData = {
        usuarioId: res.data.usuarioId,
        username: res.data.username,
        rol: res.data.rol,
      };
      setAuth(res.data.token, userData);
      navigate('/dashboard');
    } catch (err) {
        const mensaje = err.response?.data?.error || 'Error al crear la cuenta';
        setError(mensaje);
    }
  };
  return (
    <>
      <PublicHeader />
      <main className="auth-page">
        <div className="auth-card">
          <h1>Crear cuenta</h1>
          <p>Regístrate en el hotel</p>
          {error && <p className="auth-error">{error}</p>}
          <form className="auth-form" onSubmit={handleSubmit}>
            <div>
              <label htmlFor="nombre">Nombre</label>
              <input id="nombre" name="nombre" type="text" placeholder="Tu nombre" required />
            </div>
            <div>
              <label htmlFor="apellido">Apellido</label>
              <input id="apellido" name="apellido" type="text" placeholder="Tu apellido" required />
            </div>
            <div>
              <label htmlFor="email">Email</label>
              <input id="email" name="email" type="email" placeholder="tu@email.com" required />
            </div>
            <div>
              <label htmlFor="username">Usuario</label>
              <input id="username" name="username" type="text" placeholder="Elige un usuario" required />
            </div>
            <div>
              <label htmlFor="password">Contraseña</label>
              <input id="password" name="password" type="password" placeholder="Crea una contraseña" required />
            </div>
            <div>
              <label htmlFor="confirmPassword">Confirmar contraseña</label>
              <input id="confirmPassword" name="confirmPassword" type="password" placeholder="Repite la contraseña" required />
            </div>
            <button type="submit" className="auth-btn">Crear cuenta</button>
          </form>
          <p className="auth-link">
            ¿Ya tienes cuenta?<Link to="/login">Iniciar sesión</Link>
          </p>
        </div>
      </main>
      <Footer />
    </>
  );
}