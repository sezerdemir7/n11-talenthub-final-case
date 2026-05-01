import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { HiShoppingCart, HiTruck, HiShieldCheck, HiArrowLeft, HiCube } from 'react-icons/hi2';
import { productService } from '../services/productService';
import { useCart } from '../context/CartContext';
import Button from '../components/ui/Button';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import ErrorMessage from '../components/ui/ErrorMessage';

export default function ProductDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addItem } = useCart();

  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [addingToCart, setAddingToCart] = useState(false);
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    const fetchProduct = async () => {
      setLoading(true);
      setError(null);
      try {
        const { data: restResponse } = await productService.getById(id);
        setProduct(restResponse.data);
      } catch (err) {
        setError(err.message || 'Ürün bilgileri yüklenemedi');
      } finally {
        setLoading(false);
      }
    };

    fetchProduct();
  }, [id]);

  const handleAddToCart = async () => {
    setAddingToCart(true);
    await addItem(product.id, quantity, {
      productName: product.name,
      imageUrl: product.imageUrl,
      unitPrice: product.price,
    });
    setAddingToCart(false);
  };

  if (loading) return <LoadingSpinner size="lg" />;
  if (error) return <ErrorMessage message={error} onRetry={() => navigate(0)} />;
  if (!product) return <ErrorMessage message="Ürün bulunamadı" />;

  const detail = product.detail;
  const isOutOfStock = product.stock !== null && product.stock <= 0;

  return (
    <div className="max-w-7xl mx-auto px-4 py-6">
      {/* Back button */}
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-gray-500 hover:text-primary mb-6 transition-colors cursor-pointer"
      >
        <HiArrowLeft className="h-5 w-5" />
        <span className="text-sm font-medium">Geri Dön</span>
      </button>

      <div className="bg-white rounded-xl border border-gray-100 overflow-hidden">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-0">
          {/* Image */}
          <div className="relative bg-gray-50 flex items-center justify-center p-8 min-h-[400px]">
            <img
              src={product.imageUrl || 'https://via.placeholder.com/500x500?text=Ürün+Görseli'}
              alt={product.name}
              className="max-h-96 w-auto object-contain"
              onError={(e) => {
                e.target.src = 'https://via.placeholder.com/500x500?text=Ürün+Görseli';
              }}
            />
            {product.categoryName && (
              <span className="absolute top-4 left-4 bg-secondary/80 text-white text-xs font-semibold px-3 py-1 rounded">
                {product.categoryName}
              </span>
            )}
            {isOutOfStock && (
              <div className="absolute inset-0 bg-black/30 flex items-center justify-center">
                <span className="bg-white text-error font-bold text-lg px-6 py-2 rounded-lg">
                  Stokta Yok
                </span>
              </div>
            )}
          </div>

          {/* Details */}
          <div className="p-6 md:p-8 flex flex-col">
            <h1 className="text-2xl font-bold text-secondary mb-2">{product.name}</h1>

            {/* Brand & Model */}
            {(detail?.brand || detail?.model) && (
              <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-sm text-gray-500 mb-4">
                {detail.brand && (
                  <span>
                    Marka: <span className="font-medium text-primary">{detail.brand}</span>
                  </span>
                )}
                {detail.model && (
                  <span>
                    Model: <span className="font-medium text-secondary">{detail.model}</span>
                  </span>
                )}
              </div>
            )}

            {/* Price */}
            <div className="mb-6">
              <div className="text-3xl font-black text-primary">
                {product.price?.toLocaleString('tr-TR', { minimumFractionDigits: 2 })} TL
              </div>
            </div>

            {/* Short description */}
            {detail?.shortDescription && (
              <p className="text-gray-600 text-sm leading-relaxed mb-4">
                {detail.shortDescription}
              </p>
            )}

            {/* Stock info */}
            <div className="flex items-center gap-2 text-sm mb-6">
              <HiCube className="h-4 w-4 text-gray-400" />
              {isOutOfStock ? (
                <span className="text-error font-medium">Stokta yok</span>
              ) : (
                <span className="text-success font-medium">Stokta {product.stock} adet</span>
              )}
            </div>

            {/* Quantity */}
            {!isOutOfStock && (
              <div className="flex items-center gap-3 mb-6">
                <span className="text-sm font-medium text-gray-600">Adet:</span>
                <div className="flex items-center border rounded-lg">
                  <button
                    onClick={() => setQuantity(Math.max(1, quantity - 1))}
                    className="px-3 py-2 text-gray-500 hover:text-primary transition-colors cursor-pointer"
                  >
                    -
                  </button>
                  <span className="px-4 py-2 border-x font-semibold min-w-[48px] text-center">
                    {quantity}
                  </span>
                  <button
                    onClick={() => setQuantity(Math.min(product.stock || 99, quantity + 1))}
                    className="px-3 py-2 text-gray-500 hover:text-primary transition-colors cursor-pointer"
                  >
                    +
                  </button>
                </div>
              </div>
            )}

            {/* Add to Cart */}
            <Button
              onClick={handleAddToCart}
              loading={addingToCart}
              disabled={isOutOfStock}
              size="lg"
              fullWidth
              className="mb-6"
            >
              <HiShoppingCart className="h-5 w-5 mr-2" />
              {isOutOfStock ? 'Stokta Yok' : 'Sepete Ekle'}
            </Button>

            {/* Features */}
            <div className="border-t pt-6 mt-auto space-y-3">
              {detail?.warrantyPeriod && (
                <div className="flex items-center gap-3 text-sm text-gray-600">
                  <HiShieldCheck className="h-5 w-5 text-success" />
                  <span>{detail.warrantyPeriod} garanti</span>
                </div>
              )}
              <div className="flex items-center gap-3 text-sm text-gray-600">
                <HiTruck className="h-5 w-5 text-success" />
                <span>Hızlı teslimat ile kapınızda</span>
              </div>
              <div className="flex items-center gap-3 text-sm text-gray-600">
                <HiShieldCheck className="h-5 w-5 text-success" />
                <span>Güvenli alışveriş garantisi</span>
              </div>
            </div>
          </div>
        </div>

        {/* Long description & Specifications */}
        {(detail?.longDescription || detail?.specifications) && (
          <div className="border-t">
            <div className="p-6 md:p-8 space-y-6">
              {detail.longDescription && (
                <div>
                  <h2 className="text-lg font-bold text-secondary mb-3">Ürün Açıklaması</h2>
                  <p className="text-gray-600 text-sm leading-relaxed whitespace-pre-line">
                    {detail.longDescription}
                  </p>
                </div>
              )}
              {detail.specifications && (
                <div>
                  <h2 className="text-lg font-bold text-secondary mb-3">Teknik Özellikler</h2>
                  <p className="text-gray-600 text-sm leading-relaxed whitespace-pre-line">
                    {detail.specifications}
                  </p>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
