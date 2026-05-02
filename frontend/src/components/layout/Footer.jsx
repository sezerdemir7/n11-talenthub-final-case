import { Link } from 'react-router-dom';
import { HiShieldCheck, HiTruck, HiPhone } from 'react-icons/hi2';

const COLUMNS = [
  {
    heading: 'Kurumsal',
    links: [
      { label: 'Hakkımızda', href: '#' },
      { label: 'Kariyer', href: '#' },
      { label: 'Basın', href: '#' },
      { label: 'İletişim', href: '#' },
    ],
  },
  {
    heading: 'Yardım',
    links: [
      { label: 'Nasıl Sipariş Verilir?', href: '#' },
      { label: 'İade & Değişim', href: '#' },
      { label: 'Kargo Takibi', href: '#' },
      { label: 'SSS', href: '#' },
    ],
  },
  {
    heading: 'Kategoriler',
    links: [
      { label: 'Elektronik', to: '/?categoryId=1' },
      { label: 'Moda', to: '/?categoryId=3' },
      { label: 'Ev & Yaşam', to: '/?categoryId=4' },
      { label: 'Tüm Ürünler', to: '/' },
    ],
  },
];

const TRUST_BADGES = [
  { icon: HiShieldCheck, label: 'Güvenli Ödeme' },
  { icon: HiTruck, label: 'Hızlı Teslimat' },
  { icon: HiPhone, label: '7/24 Destek' },
];

export default function Footer() {
  return (
    <footer className="bg-secondary text-white mt-auto">
      {/* Trust bar */}
      <div className="border-b border-white/10">
        <div className="max-w-7xl mx-auto px-4 py-5">
          <div className="flex flex-wrap justify-center gap-8">
            {TRUST_BADGES.map(({ icon: Icon, label }) => (
              <div key={label} className="flex items-center gap-2 text-sm text-gray-400">
                <Icon className="h-4 w-4 text-primary" />
                {label}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Main content */}
      <div className="max-w-7xl mx-auto px-4 py-10">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Brand */}
          <div>
            <Link to="/">
              <span className="text-2xl font-black text-primary">n11</span>
            </Link>
            <p className="mt-3 text-sm text-gray-400 leading-relaxed">
              Türkiye'nin güvenilir alışveriş platformu. Milyonlarca ürün, uygun fiyat garantisiyle.
            </p>
          </div>

          {/* Link columns */}
          {COLUMNS.map((col) => (
            <div key={col.heading}>
              <h4 className="text-sm font-semibold text-white mb-4">{col.heading}</h4>
              <ul className="space-y-2.5">
                {col.links.map((link) => (
                  <li key={link.label}>
                    {link.to ? (
                      <Link
                        to={link.to}
                        className="text-sm text-gray-400 hover:text-white transition-colors"
                      >
                        {link.label}
                      </Link>
                    ) : (
                      <a
                        href={link.href}
                        className="text-sm text-gray-400 hover:text-white transition-colors"
                      >
                        {link.label}
                      </a>
                    )}
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="border-t border-white/10 mt-10 pt-6 flex flex-col sm:flex-row items-center justify-between gap-2">
          <p className="text-xs text-gray-500">
            © {new Date().getFullYear()} n11 Marketplace. Tüm hakları saklıdır.
          </p>
          <div className="flex items-center gap-4">
            <a href="#" className="text-xs text-gray-500 hover:text-gray-300 transition-colors">
              Gizlilik Politikası
            </a>
            <a href="#" className="text-xs text-gray-500 hover:text-gray-300 transition-colors">
              Kullanım Koşulları
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
}
