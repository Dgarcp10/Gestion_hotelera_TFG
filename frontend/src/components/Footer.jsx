import './Footer.css';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="footer-grid">
        <div className="footer-col">
          <h3 className="footer-col-title">Hotel Villa de Lerma</h3>
          <p className="footer-text">
            Tu refugio en la comarca del Arlanza
          </p>
        </div>
        <div className="footer-col">
          <h3 className="footer-col-title">Enlaces rápidos</h3>
          <ul className="footer-links">
            <li><a href="/">Inicio</a></li>
            <li><a href="/login">Iniciar sesión</a></li>
            <li><a href="/register">Registrarse</a></li>
          </ul>
        </div>
        <div className="footer-col">
          <h3 className="footer-col-title">Contacto</h3>
          <p className="footer-text">Lerma, Burgos</p>
          <p className="footer-text">info@hotelvilladelerma.com</p>
          <p className="footer-text">+34 947 000 000</p>
        </div>
        <div className="footer-col">
          <h3 className="footer-col-title">Síguenos</h3>
          <div className="footer-social">
            <a href="#" aria-label="Facebook" className="footer-social-link">
              FB
            </a>
            <a href="#" aria-label="Instagram" className="footer-social-link">
              IG
            </a>
            <a href="#" aria-label="Twitter" className="footer-social-link">
              X
            </a>
          </div>
        </div>
      </div>
      <div className="footer-bottom">
        <p>&copy; {new Date().getFullYear()} Hotel Villa de Lerma. Todos los derechos reservados.</p>
      </div>
    </footer>
  );
}
