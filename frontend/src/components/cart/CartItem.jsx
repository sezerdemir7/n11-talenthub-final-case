import { Link } from 'react-router-dom';

function formatMoney(value) {
  if (value == null) return '—';
  const n = Number(value);
  return `${n.toLocaleString('tr-TR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} TL`;
}

export default function CartItem({ item }) {
  const {
    productId,
    productName,
    imageUrl,
    unitPrice,
    quantity,
    totalPrice,
  } = item;

  return (
    <div className="flex gap-4 bg-white p-4 rounded-lg border border-gray-100">
      <Link
        to={`/product/${productId}`}
        className="w-24 h-24 shrink-0 bg-gray-50 rounded-lg overflow-hidden block ring-offset-2 hover:ring-2 hover:ring-primary/40 transition-shadow"
        title="Ürün detayına git"
      >
        <img
          src={imageUrl || 'https://via.placeholder.com/100x100?text=Ürün'}
          alt={productName}
          className="w-full h-full object-contain p-2"
          onError={(e) => {
            e.target.src = 'https://via.placeholder.com/100x100?text=Ürün';
          }}
        />
      </Link>

      <div className="flex-1 min-w-0">
        <Link
          to={`/product/${productId}`}
          className="text-sm font-medium text-secondary truncate hover:text-primary block"
        >
          {productName}
        </Link>
        <p className="text-xs text-gray-500 mt-1">Birim: {formatMoney(unitPrice)}</p>
        <p className="text-sm text-gray-600 mt-2">
          Adet: <span className="font-semibold text-secondary">{quantity}</span>
        </p>
      </div>

      <div className="flex flex-col items-end justify-center shrink-0">
        <span className="text-sm font-semibold text-primary">{formatMoney(totalPrice)}</span>
      </div>
    </div>
  );
}
