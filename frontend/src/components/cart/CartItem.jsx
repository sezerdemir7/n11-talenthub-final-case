import { Link } from 'react-router-dom';
import { HiTrash } from 'react-icons/hi2';

function formatMoney(value) {
  if (value == null) return '-';
  const n = Number(value);
  return `${n.toLocaleString('tr-TR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} TL`;
}

function unavailableText(reason) {
  if (reason === 'PRODUCT_NOT_FOUND') return 'Urun artik mevcut degil';
  if (reason === 'PRODUCT_INACTIVE') return 'Urun su anda satista degil';
  if (reason === 'INSUFFICIENT_STOCK') return 'Stok yetersiz';
  return 'Urun satin almaya uygun degil';
}

export default function CartItem({
  item,
  selected = false,
  onSelectionChange,
  onIncreaseQuantity,
  onDecreaseQuantity,
  onRemoveItem,
}) {
  const {
    productId,
    productName,
    imageUrl,
    unitPrice,
    quantity,
    totalPrice,
    available = true,
    unavailableReason,
  } = item;
  const canSelect = available;

  return (
    <div className={`flex gap-4 p-4 rounded-lg border ${available ? 'bg-white border-gray-100' : 'bg-amber-50/60 border-amber-200'}`}>
      <div className="flex items-center">
        <input
          type="checkbox"
          className="h-4 w-4 accent-primary"
          checked={selected && canSelect}
          disabled={!canSelect}
          onChange={(event) => onSelectionChange?.(productId, event.target.checked)}
          aria-label="Siparis icin sec"
        />
      </div>
      <Link
        to={`/product/${productId}`}
        className="w-24 h-24 shrink-0 bg-gray-50 rounded-lg overflow-hidden block ring-offset-2 hover:ring-2 hover:ring-primary/40 transition-shadow"
        title="Urun detayina git"
      >
        <img
          src={imageUrl || 'https://via.placeholder.com/100x100?text=Urun'}
          alt={productName}
          className="w-full h-full object-contain p-2"
          onError={(e) => {
            e.target.src = 'https://via.placeholder.com/100x100?text=Urun';
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
        <div className="flex items-center gap-2 mt-2">
          <p className="text-sm text-gray-600">
            Adet: <span className="font-semibold text-secondary">{quantity}</span>
          </p>
          {available && (
            <>
              <button
                type="button"
                onClick={() => onDecreaseQuantity?.(productId)}
                className="h-7 w-7 rounded-md border border-gray-200 text-gray-700 hover:border-primary hover:text-primary transition-colors cursor-pointer"
                aria-label="Adedi azalt"
                title="Adedi azalt"
              >
                -
              </button>
              <button
                type="button"
                onClick={() => onIncreaseQuantity?.(productId)}
                className="h-7 w-7 rounded-md border border-gray-200 text-gray-700 hover:border-primary hover:text-primary transition-colors cursor-pointer"
                aria-label="Adedi artir"
                title="Adedi artir"
              >
                +
              </button>
            </>
          )}
        </div>
        {!available && (
          <p className="text-xs font-semibold text-amber-700 mt-2">
            {unavailableText(unavailableReason)}
          </p>
        )}
      </div>

      <div className="flex flex-col items-end justify-center shrink-0">
        <span className={`text-sm font-semibold ${available ? 'text-primary' : 'text-gray-400 line-through'}`}>
          {formatMoney(totalPrice)}
        </span>
        <button
          type="button"
          onClick={() => onRemoveItem?.(productId)}
          className="mt-2 inline-flex items-center gap-1 text-xs font-semibold text-gray-500 hover:text-error transition-colors cursor-pointer"
          aria-label="Sepetten sil"
          title="Sepetten sil"
        >
          <HiTrash className="h-3.5 w-3.5" />
          Sepetten sil
        </button>
      </div>
    </div>
  );
}
