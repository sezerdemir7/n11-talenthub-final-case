import { Link } from 'react-router-dom';
import { HiShoppingCart, HiOutlineCube } from 'react-icons/hi2';
import { useCart } from '../../context/CartContext';

export default function ProductCard({ product }) {
  const { addItem } = useCart();
  const { id, name, price, imageUrl, categoryName, stock } = product;
  const isOutOfStock = stock !== null && stock !== undefined && stock <= 0;

  const handleAddToCart = (e) => {
    e.preventDefault();
    e.stopPropagation();
    addItem(id, 1, { productName: name, imageUrl, unitPrice: price });
  };

  return (
    <Link
      to={`/product/${id}`}
      className="group bg-white rounded-xl border border-gray-100 overflow-hidden hover:border-gray-200 hover:shadow-md transition-all duration-200 flex flex-col"
    >
      {/* Image area */}
      <div className="relative aspect-square bg-gray-50 overflow-hidden">
        <img
          src={imageUrl || 'https://via.placeholder.com/300x300?text=Ürün'}
          alt={name}
          className="w-full h-full object-contain p-4 group-hover:scale-[1.03] transition-transform duration-300"
          loading="lazy"
          onError={(e) => { e.target.src = 'https://via.placeholder.com/300x300?text=Ürün'; }}
        />

        {categoryName && (
          <span className="absolute top-2 left-2 bg-secondary/75 text-white text-[10px] font-medium px-2 py-0.5 rounded-md">
            {categoryName}
          </span>
        )}

        {isOutOfStock && (
          <div className="absolute inset-0 bg-white/60 flex items-center justify-center">
            <div className="flex items-center gap-1.5 bg-white border border-gray-200 px-3 py-1.5 rounded-lg shadow-sm">
              <HiOutlineCube className="h-4 w-4 text-gray-400" />
              <span className="text-xs font-semibold text-gray-600">Stok Yok</span>
            </div>
          </div>
        )}

        {!isOutOfStock && (
          <button
            onClick={handleAddToCart}
            className="absolute bottom-0 inset-x-0 bg-primary text-white py-2.5 text-xs font-semibold
                       flex items-center justify-center gap-1.5
                       translate-y-full group-hover:translate-y-0 opacity-0 group-hover:opacity-100
                       transition-all duration-200 cursor-pointer"
          >
            <HiShoppingCart className="h-4 w-4" />
            Sepete Ekle
          </button>
        )}
      </div>

      {/* Info */}
      <div className="p-3 flex flex-col flex-1 gap-2">
        <h3 className="text-sm text-secondary font-medium line-clamp-2 leading-snug flex-1">
          {name}
        </h3>
        <p className="text-base font-bold text-primary">
          {price?.toLocaleString('tr-TR', { minimumFractionDigits: 2 })} TL
        </p>
      </div>
    </Link>
  );
}
