import { Routes, Route } from 'react-router-dom'
import Landing from './pages/Landing'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import HabitacionesPage from './pages/HabitacionesPage';
import MisReservasPage from './pages/MisReservasPage';
import NuevaReservaPage from './pages/NuevaReservaPage';

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/dashboard" element={<Dashboard />} />
      <Route path="/jefe/habitaciones" element={<HabitacionesPage />} />
      <Route path="/mis-reservas" element={<MisReservasPage />} />
      <Route path="/nueva-reserva" element={<NuevaReservaPage />} />
      
    </Routes>
  );
}