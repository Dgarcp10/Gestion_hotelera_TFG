const HOTEL_NAME = import.meta.env.VITE_HOTEL_NAME || 'Hotel';
const LOGO_URL = import.meta.env.VITE_LOGO_URL || null;
export default function ProtectedHeader({ usuario }) {
  const inicial = usuario?.username?.charAt(0)?.toUpperCase() || '?';
  const avatarUrl = usuario?.avatarPath || null;
  return (
    <header className="protected-header">
      <div className="header-left">
        {LOGO_URL && <img src={LOGO_URL} alt={HOTEL_NAME} className="header-logo" />}
        <span className="header-hotel-name">{HOTEL_NAME}</span>
      </div>
      <div className="header-right">
        {avatarUrl ? (
          <img src={avatarUrl} alt="Avatar" className="header-avatar" />
        ) : (
          <div className="header-avatar-placeholder">{inicial}</div>
        )}
        <span className="header-username">{usuario?.username}</span>
      </div>
    </header>
  );
}