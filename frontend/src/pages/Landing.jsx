import { Link } from 'react-router-dom';
import PublicHeader from '../components/PublicHeader';
import Footer from '../components/Footer';
import './Landing.css';

const BANNER_URL = import.meta.env.VITE_BANNER_URL || '/banner.jpg';

export default function Landing() {
  return (
    <>
      <PublicHeader />

      <section className="hero-section">
        <div className="hero-bg" style={{ backgroundImage: `url(${BANNER_URL})` }} />
        <div className="hero-overlay" />
        <div className="hero-content">
          <p className="hero-sup">BIENVENIDO A</p>
          <h1 className="hero-title">Hotel Villa de Lerma</h1>
          <p className="hero-desc">
            Tu próxima experiencia inolvidable comienza aquí. Situado en el corazón de la comarca del Arlanza, 
            en Burgos, nuestro hotel es el punto de partida perfecto para descubrir paisajes únicos, un rico 
            patrimonio histórico y la tranquilidad de un entorno incomparable. Descubre los rincones más 
            especiales de la zona mientras disfrutas del confort y la hospitalidad que nos caracterizan.
          </p>
          <div className="hero-buttons">
            <Link to="/login" className="hero-btn">
              Iniciar sesión
            </Link>
            <Link to="/register" className="hero-btn hero-btn-gold">
              Registrarse
            </Link>
          </div>
        </div>
      </section>

      <section className="info-cards">
        <div className="info-cards-inner">
          <div className="info-card">
            <h2>Sobre nosotros</h2>
            <p>Próximamente</p>
          </div>
          <div className="info-card">
            <h2>Descubre el hotel</h2>
            <p>Próximamente</p>
          </div>
        </div>
      </section>

      <Footer />
    </>
  );
}
