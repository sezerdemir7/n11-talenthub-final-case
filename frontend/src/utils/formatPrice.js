export function formatPrice(price) {
  if (price == null) return '0';
  return price.toLocaleString('tr-TR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

export function formatPriceWithCurrency(price) {
  return `${formatPrice(price)} TL`;
}
