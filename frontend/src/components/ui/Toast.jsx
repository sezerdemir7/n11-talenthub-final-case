import { HiCheckCircle, HiExclamationCircle, HiInformationCircle, HiXMark } from 'react-icons/hi2';
import { useToast } from '../../context/ToastContext';

const icons = {
  success: HiCheckCircle,
  error: HiExclamationCircle,
  info: HiInformationCircle,
};

const styles = {
  success: 'bg-success text-white',
  error: 'bg-error text-white',
  info: 'bg-secondary text-white',
};

export default function ToastContainer() {
  const { toasts, removeToast } = useToast();

  if (toasts.length === 0) return null;

  return (
    <div className="fixed top-4 right-4 z-[9999] flex flex-col gap-2 max-w-sm">
      {toasts.map((toast) => {
        const Icon = icons[toast.type] || icons.info;
        return (
          <div
            key={toast.id}
            className={`flex items-center gap-3 px-4 py-3 rounded-lg shadow-lg animate-slide-in ${styles[toast.type] || styles.info}`}
          >
            <Icon className="h-5 w-5 shrink-0" />
            <p className="text-sm font-medium flex-1">{toast.message}</p>
            <button
              onClick={() => removeToast(toast.id)}
              className="shrink-0 hover:opacity-70 transition-opacity cursor-pointer"
            >
              <HiXMark className="h-4 w-4" />
            </button>
          </div>
        );
      })}
    </div>
  );
}
