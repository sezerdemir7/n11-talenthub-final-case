import { forwardRef } from 'react';

const Input = forwardRef(function Input(
  { label, error, type = 'text', className = '', ...props },
  ref
) {
  return (
    <div className="w-full">
      {label && (
        <label className="block text-sm font-medium text-secondary mb-1.5">
          {label}
        </label>
      )}
      <input
        ref={ref}
        type={type}
        className={`
          w-full px-4 py-2.5 border rounded-lg text-secondary
          placeholder:text-gray-400
          focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary
          transition-all duration-200
          ${error ? 'border-error ring-1 ring-error/30' : 'border-gray-300'}
          ${className}
        `}
        {...props}
      />
      {error && <p className="mt-1 text-sm text-error">{error}</p>}
    </div>
  );
});

export default Input;
