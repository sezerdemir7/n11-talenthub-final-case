import { HiExclamationTriangle } from 'react-icons/hi2';
import Button from './Button';

export default function ErrorMessage({
  message = 'Bir hata oluştu',
  onRetry,
  className = '',
}) {
  return (
    <div className={`flex flex-col items-center justify-center py-16 ${className}`}>
      <HiExclamationTriangle className="h-16 w-16 text-error mb-4" />
      <p className="text-lg text-gray-600 mb-4 text-center">{message}</p>
      {onRetry && (
        <Button variant="outline" onClick={onRetry}>
          Tekrar Dene
        </Button>
      )}
    </div>
  );
}
