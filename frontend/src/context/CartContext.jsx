import { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { cartService } from '../services/cartService';
import { useToast } from './ToastContext';
import { useAuth } from './AuthContext';
import {
  loadGuestCartLines,
  saveGuestCartLines,
  mergeGuestCartLine,
  guestLinesToCartState,
  mergeGuestCartIntoServer,
} from '../utils/guestCart';

const emptyCart = { cartId: null, userId: null, items: [], totalPrice: 0 };

const CartContext = createContext(null);

function normalizeCart(restData) {
  if (!restData) return emptyCart;
  return {
    cartId: restData.cartId ?? null,
    userId: restData.userId ?? null,
    items: restData.items || [],
    totalPrice: restData.totalPrice != null ? Number(restData.totalPrice) : 0,
  };
}

/** Boş sepet / bulunamadı yanıtlarında toast göstermeyiz (API bazen 404 veya "cart is empty" döner). */
function isBenignEmptyCartFetchError(error) {
  const status = error?.response?.status;
  if (status === 404 || status === 204) return true;
  const msg = String(error?.message || '').toLowerCase();
  if (!msg) return false;
  if (msg.includes('cart is empty')) return true;
  if (msg.includes('empty') && msg.includes('cart')) return true;
  if (msg.includes('no cart')) return true;
  if (msg.includes('cart not found')) return true;
  if (msg.includes('sepet') && (msg.includes('boş') || msg.includes('bos'))) return true;
  if (msg.includes('sepet') && msg.includes('bulunamad')) return true;
  return false;
}

export function CartProvider({ children }) {
  const { isAuthenticated } = useAuth();
  const [cart, setCart] = useState(emptyCart);
  /** İlk yükleme bitene kadar true — girişli kullanıcıda boş sepet flash'ını önler */
  const [loading, setLoading] = useState(true);
  const { showToast } = useToast();

  const loadGuestCartIntoState = useCallback(() => {
    const lines = loadGuestCartLines();
    setCart(guestLinesToCartState(lines));
  }, []);

  const fetchCart = useCallback(async () => {
    if (!isAuthenticated) {
      loadGuestCartIntoState();
      return;
    }
    setLoading(true);
    try {
      await mergeGuestCartIntoServer(cartService);
      const { data: rest } = await cartService.getCart();
      setCart(normalizeCart(rest.data));
    } catch (error) {
      if (isBenignEmptyCartFetchError(error)) {
        setCart(emptyCart);
      } else {
        showToast(error.message || 'Sepet yüklenemedi', 'error');
      }
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, showToast, loadGuestCartIntoState]);

  useEffect(() => {
    if (!isAuthenticated) {
      loadGuestCartIntoState();
      setLoading(false);
      return;
    }
    fetchCart();
  }, [isAuthenticated, fetchCart, loadGuestCartIntoState]);

  const addItem = async (productId, quantity = 1, snapshot = {}) => {
    const qty = Math.max(1, Number(quantity) || 1);
    const snap = {
      productName: snapshot.productName ?? snapshot.name ?? '',
      imageUrl: snapshot.imageUrl ?? '',
      unitPrice:
        snapshot.unitPrice != null
          ? Number(snapshot.unitPrice)
          : snapshot.price != null
            ? Number(snapshot.price)
            : 0,
    };

    if (isAuthenticated) {
      try {
        await cartService.addItem({ productId, quantity: qty });
        await fetchCart();
        showToast('Ürün sepete eklendi', 'success');
      } catch (error) {
        showToast(error.message || 'Ürün eklenemedi', 'error');
      }
      return;
    }

    try {
      const lines = loadGuestCartLines();
      const merged = mergeGuestCartLine(lines, {
        productId,
        quantity: qty,
        productName: snap.productName,
        imageUrl: snap.imageUrl,
        unitPrice: snap.unitPrice,
      });
      saveGuestCartLines(merged);
      setCart(guestLinesToCartState(merged));
      showToast('Ürün sepete eklendi', 'success');
    } catch (error) {
      showToast(error.message || 'Ürün eklenemedi', 'error');
    }
  };

  const clearCart = async () => {
    if (isAuthenticated) {
      try {
        await cartService.clearCart();
        setCart(emptyCart);
        showToast('Sepet temizlendi', 'success');
      } catch (error) {
        showToast(error.message || 'Sepet temizlenemedi', 'error');
      }
      return;
    }
    saveGuestCartLines([]);
    setCart(emptyCart);
    showToast('Sepet temizlendi', 'success');
  };

  const itemCount = cart.items?.reduce((sum, item) => sum + (item.quantity || 0), 0) || 0;

  const value = {
    cart,
    loading,
    itemCount,
    fetchCart,
    addItem,
    clearCart,
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useCart() {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
}
