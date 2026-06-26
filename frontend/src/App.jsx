import { Routes, Route } from 'react-router-dom'
import { useAuth } from './contexts/useAuth'
import { useState } from 'react';
import PublicHeader from './components/PublicHeader'
import ProtectedHeader from './components/ProtectedHeader'
export default function App() {
  const { user } = useAuth();
  return (
    <>
      {user ? <ProtectedHeader usuario={user} /> : <PublicHeader />}
      <Routes>
        <Route path="/" element={<h1>Bienvenido al Hotel Villa de Lerma</h1>} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<h1>Registro</h1>} />
        <Route path="/dashboard" element={<h1>Panel principal</h1>} />
      </Routes>
    </>
  );
}
function LoginPage() {
  const { login } = useAuth();
  const [error, setError] = useState('');
  const handleSubmit = async (e) => {
    e.preventDefault();
    const data = new FormData(e.target);
    try {
      await login(data.get('username'), data.get('password'));
      window.location.href = '/dashboard';
    } catch {
      setError('Credenciales inválidas');
    }
  };
  return (
    <form onSubmit={handleSubmit}>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <input name="username" placeholder="Usuario" required />
      <input name="password" type="password" placeholder="Contraseña" required />
      <button type="submit">Entrar</button>
    </form>
  );
}