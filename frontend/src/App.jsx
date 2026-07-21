import { Routes, Route } from 'react-router-dom'
import Landing from './pages/Landing'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import HabitacionesPage from './pages/HabitacionesPage';
import MisReservasPage from './pages/MisReservasPage';
import NuevaReservaPage from './pages/NuevaReservaPage';
import GestionEmpleadosPage from './pages/GestionEmpleadosPage';
import CheckInPage from './pages/CheckInPage';
import CheckOutPage from './pages/CheckOutPage';
import LimpiezaPage from './pages/LimpiezaPage';

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
      <Route path="/jefe/empleados" element={<GestionEmpleadosPage />} />
      <Route path="/recepcion/check-in" element={<CheckInPage />} />
      <Route path="/recepcion/check-out" element={<CheckOutPage />} />
      <Route path="/limpieza" element={<LimpiezaPage />} />
      
    </Routes>
  );
}