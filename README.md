## Gestión Hotelera - TFG
Aplicación web para la gestión integral de un hotel, desarrollada como Trabajo de Fin de Grado. Permite gestionar reservas, habitaciones, personal, limpieza, averías y estadísticas del hotel con un sistema de cinco roles diferenciados.

## Requisitos
- Java 17 o superior
- Node.js 22 o superior
- npm
- Cuenta en Supabase con base de datos PostgreSQL

## Instalación
1. Configurar el archivo `backend/.env` siguiendo el formato del archivo `.env.example` que se encuentra en la misma carpeta.
2. Iniciar el backend:
   cd backend
   ./mvnw.cmd spring-boot:run
3. Iniciar el frontend (en otra terminal):
   cd frontend
   npm install
   npm run dev
4. Abrir http://localhost:5173 en el navegador.

## Ejecutar tests
# Backend
cd backend
.\mvnw.cmd test
# Frontend
cd frontend
npm test

## Estructura del proyecto
├── backend/          # Spring Boot (Java 17)
├── frontend/         # React + Vite
├── Otros/            # Documentación y esquemas SQL
└── .github/workflows/# CI/CD (GitHub Actions)

## Licencia
Proyecto académico - Trabajo de Fin de Grado
---