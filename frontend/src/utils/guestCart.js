const STORAGE_KEY = 'guest_cart_v1';

/**
 * Misafir sepet satırları — sunucu satırı ile uyumlu snapshot (ürün adı, görsel, fiyat) tutulur.
 * @typedef {{ productId: number, quantity: number, productName?: string, imageUrl?: string, unitPrice?: number }} GuestCartLine
 */

export function loadGuestCartLines() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

export function saveGuestCartLines(lines) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(lines));
}

export function clearGuestCartStorage() {
  localStorage.removeItem(STORAGE_KEY);
}

/**
 * Aynı ürün varsa adet toplanır; eksik snapshot alanları doldurulur.
 */
export function mergeGuestCartLine(lines, { productId, quantity, productName, imageUrl, unitPrice }) {
  const qty = Math.max(1, Number(quantity) || 1);
  const pid = Number(productId);
  const next = [...lines];
  const i = next.findIndex((l) => Number(l.productId) === pid);
  if (i === -1) {
    next.push({
      productId: pid,
      quantity: qty,
      productName: productName || '',
      imageUrl: imageUrl || '',
      unitPrice: unitPrice != null ? Number(unitPrice) : 0,
    });
    return next;
  }
  next[i] = {
    ...next[i],
    quantity: (Number(next[i].quantity) || 0) + qty,
    productName: next[i].productName || productName || '',
    imageUrl: next[i].imageUrl || imageUrl || '',
    unitPrice:
      next[i].unitPrice != null && Number(next[i].unitPrice) > 0
        ? Number(next[i].unitPrice)
        : unitPrice != null
          ? Number(unitPrice)
          : 0,
  };
  return next;
}

/**
 * Misafir satırlarını CartContext `cart` şekline çevirir (CartItem ile uyumlu).
 */
export function guestLinesToCartState(lines) {
  const items = (lines || []).map((line, idx) => {
    const unit = Number(line.unitPrice ?? 0);
    const qty = Math.max(0, Number(line.quantity ?? 0));
    return {
      id: `guest-${line.productId}-${idx}`,
      productId: line.productId,
      productName: line.productName || 'Ürün',
      imageUrl: line.imageUrl,
      unitPrice: unit,
      quantity: qty,
      totalPrice: unit * qty,
    };
  });
  const totalPrice = items.reduce((s, it) => s + Number(it.totalPrice || 0), 0);
  return { cartId: null, userId: null, items, totalPrice };
}

/** Giriş sonrası: misafir satırlarını API sepetine ekler; başarısız satırlar localStorage'da kalır */
export async function mergeGuestCartIntoServer(userId, cartService) {
  const lines = loadGuestCartLines();
  if (!lines.length) return;
  const failed = [];
  for (const line of lines) {
    try {
      await cartService.addItem(userId, {
        productId: line.productId,
        quantity: Math.max(1, Number(line.quantity) || 1),
      });
    } catch {
      failed.push(line);
    }
  }
  if (failed.length === 0) {
    clearGuestCartStorage();
  } else {
    saveGuestCartLines(failed);
  }
}
