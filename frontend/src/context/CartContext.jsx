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
    items: (restData.items || []).map((item) => ({
      ...item,
      available: item.available !== false,
      unavailableReason: item.unavailableReason ?? null,
    })),
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

  const increaseItemQuantity = async (productId) => {
    const pid = Number(productId);
    if (!pid) return;

    if (isAuthenticated) {
      try {
        await cartService.addItem({ productId: pid, quantity: 1 });
        await fetchCart();
      } catch (error) {
        showToast(error.message || 'Urun adedi artirilamadi', 'error');
      }
      return;
    }

    try {
      const lines = loadGuestCartLines();
      const existing = lines.find((line) => Number(line.productId) === pid);
      if (!existing) return;

      const merged = mergeGuestCartLine(lines, {
        productId: pid,
        quantity: 1,
        productName: existing.productName,
        imageUrl: existing.imageUrl,
        unitPrice: existing.unitPrice,
      });
      saveGuestCartLines(merged);
      setCart(guestLinesToCartState(merged));
    } catch (error) {
      showToast(error.message || 'Urun adedi artirilamadi', 'error');
    }
  };

  const decreaseItemQuantity = async (productId) => {
    const pid = Number(productId);
    if (!pid) return;

    if (isAuthenticated) {
      try {
        const currentItems = Array.isArray(cart.items) ? cart.items : [];
        const updatedItems = currentItems
          .map((item) => {
            if (Number(item.productId) !== pid) return item;
            return { ...item, quantity: Math.max(0, Number(item.quantity || 0) - 1) };
          })
          .filter((item) => Number(item.quantity || 0) > 0);

        await cartService.clearCart();
        for (const item of updatedItems) {
          await cartService.addItem({
            productId: Number(item.productId),
            quantity: Number(item.quantity || 0),
          });
        }
        await fetchCart();
      } catch (error) {
        showToast(error.message || 'Urun adedi azaltilamadi', 'error');
      }
      return;
    }

    try {
      const lines = loadGuestCartLines();
      const updated = lines
        .map((line) => {
          if (Number(line.productId) !== pid) return line;
          return { ...line, quantity: Math.max(0, Number(line.quantity || 0) - 1) };
        })
        .filter((line) => Number(line.quantity || 0) > 0);
      saveGuestCartLines(updated);
      setCart(guestLinesToCartState(updated));
    } catch (error) {
      showToast(error.message || 'Urun adedi azaltilamadi', 'error');
    }
  };

  const removeItemFromCart = async (productId) => {
    const pid = Number(productId);
    if (!pid) return;

    if (isAuthenticated) {
      try {
        const currentItems = Array.isArray(cart.items) ? cart.items : [];
        const updatedItems = currentItems.filter((item) => Number(item.productId) !== pid);

        await cartService.clearCart();
        for (const item of updatedItems) {
          await cartService.addItem({
            productId: Number(item.productId),
            quantity: Number(item.quantity || 0),
          });
        }
        await fetchCart();
        showToast('Urun sepetten kaldirildi', 'success');
      } catch (error) {
        showToast(error.message || 'Urun sepetten kaldirilamadi', 'error');
      }
      return;
    }

    try {
      const lines = loadGuestCartLines();
      const updated = lines.filter((line) => Number(line.productId) !== pid);
      saveGuestCartLines(updated);
      setCart(guestLinesToCartState(updated));
      showToast('Urun sepetten kaldirildi', 'success');
    } catch (error) {
      showToast(error.message || 'Urun sepetten kaldirilamadi', 'error');
    }
  };

  const itemCount = cart.items?.reduce((sum, item) => sum + (item.quantity || 0), 0) || 0;

  const value = {
    cart,
    loading,
    itemCount,
    fetchCart,
    addItem,
    increaseItemQuantity,
    decreaseItemQuantity,
    removeItemFromCart,
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
