import { useCallback, useRef, useState } from 'react';
import { ToastContext } from './toastContext';
let nextId = 1;
export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  const timers = useRef({});
  const dismiss = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
    clearTimeout(timers.current[id]);
    delete timers.current[id];
  }, []);
  const notify = useCallback((type, message) => {
    const id = nextId++;
    setToasts((prev) => [...prev, { id, type, message }]);
    timers.current[id] = setTimeout(() => dismiss(id), 4000);
  }, [dismiss]);
  const success = useCallback((m) => notify('success', m), [notify]);
  const error = useCallback((m) => notify('error', m), [notify]);
  return (
    <ToastContext.Provider value={{ success, error }}>
      {children}
      <div className="toast-container" aria-live="polite">
        {toasts.map((t) => (
          <div key={t.id} className={`toast toast-${t.type}`} onClick={() => dismiss(t.id)}>
            {t.message}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}