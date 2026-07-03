import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/useAuth';
import PublicHeader from '../components/PublicHeader';
import Footer from '../components/Footer';
import './Auth.css';
export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const data = new FormData(e.target);
    try {
      await login(data.get('username'), data.get('password'));
      navigate('/dashboard');
    } catch {
      setError('Credenciales inválidas');
    }
  };
  return (
    <>
      <PublicHeader />
      <main className="auth-page">
        <div className="auth-card">
          <h1>Iniciar sesión</h1>
          <p>Accede a tu cuenta</p>
          {error && <p className="auth-error">{error}</p>}
          <form className="auth-form" onSubmit={handleSubmit}>
            <div>
              <label htmlFor="username">Usuario</label>
              <input id="username" name="username" type="text" placeholder="Tu usuario" required />
            </div>
            <div>
              <label htmlFor="password">Contraseña</label>
              <input id="password" name="password" type="password" placeholder="Tu contraseña" required />
            </div>
            <button type="submit" className="auth-btn">Iniciar sesión</button>
          </form>
          <p className="auth-link">
            ¿No tienes cuenta?<Link to="/register">Registrarse</Link>
          </p>
        </div>
      </main>
      <Footer />
    </>
  );
}