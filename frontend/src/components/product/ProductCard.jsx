import { Link } from 'react-router-dom';
import { HiShoppingCart } from 'react-icons/hi2';
import { useCart } from '../../context/CartContext';

export default function ProductCard({ product }) {
  const { addItem } = useCart();

  const { id, name, price, imageUrl, categoryName, stock } = product;

  const isOutOfStock = stock !== null && stock !== undefined && stock <= 0;

  const handleAddToCart = (e) => {
    e.preventDefault();
    e.stopPropagation();
    addItem(id, 1, {
      productName: name,
      imageUrl,
      unitPrice: price,
    });
  };

  return (
    <Link
      to={`/product/${id}`}
      className="group bg-white rounded-lg border border-gray-100 overflow-hidden hover:shadow-lg transition-all duration-300 flex flex-col"
    >
      {/* Image */}
      <div className="relative aspect-square overflow-hidden bg-gray-50">
        <img
          src={imageUrl || 'https://via.placeholder.com/300x300?text=Ürün+Görseli'}
          alt={name}
          className="w-full h-full object-contain p-4 group-hover:scale-105 transition-transform duration-300"
          loading="lazy"
          onError={(e) => {
            e.target.src = 'https://via.placeholder.com/300x300?text=Ürün+Görseli';
          }}
        />

        {/* Category badge */}
        {categoryName && (
          <span className="absolute top-2 left-2 bg-secondary/80 text-white text-[10px] font-semibold px-2 py-0.5 rounded">
            {categoryName}
          </span>
        )}

        {isOutOfStock && (
          <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
            <span className="bg-white text-secondary font-bold text-sm px-4 py-1.5 rounded">
              Tükendi
            </span>
          </div>
        )}

        {/* Hover add to cart */}
        {!isOutOfStock && (
          <button
            onClick={handleAddToCart}
            className="absolute bottom-0 left-0 right-0 bg-primary text-white py-2.5 text-sm font-semibold
                       flex items-center justify-center gap-2 opacity-0 group-hover:opacity-100
                       translate-y-full group-hover:translate-y-0 transition-all duration-300 cursor-pointer"
          >
            <HiShoppingCart className="h-4 w-4" />
            Sepete Ekle
          </button>
        )}
      </div>

      {/* Info */}
      <div className="p-3 flex flex-col flex-1">
        <h3 className="text-sm text-secondary font-medium line-clamp-2 mb-2 min-h-[2.5rem]">
          {name}
        </h3>
        <div className="mt-auto">
          <div className="text-lg font-bold text-primary">
            {price?.toLocaleString('tr-TR', { minimumFractionDigits: 2 })} TL
          </div>
        </div>
      </div>
    </Link>
  );
}
