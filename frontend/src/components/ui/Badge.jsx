const variants = {
  discount: 'bg-accent-yellow text-secondary',
  free_shipping: 'bg-success text-white',
  coupon: 'bg-primary text-white',
  new: 'bg-secondary text-white',
};

export default function Badge({ children, variant = 'discount', className = '' }) {
  return (
    <span
      className={`
        inline-flex items-center px-2 py-0.5 text-xs font-bold rounded
        ${variants[variant]}
        ${className}
      `}
    >
      {children}
    </span>
  );
}
