export default function ConfirmDialog({ title, message, onConfirm, onCancel, confirmLabel = 'Confirmar', cancelLabel = 'Cancelar' }) {
  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <h2>{title}</h2>
        <p style={{ marginBottom: 24 }}>{message}</p>
        <div className="modal-actions">
          <button className="btn-cancelar" onClick={onCancel}>{cancelLabel}</button>
          <button className="btn-guardar" onClick={onConfirm}>{confirmLabel}</button>
        </div>
      </div>
    </div>
  );
}