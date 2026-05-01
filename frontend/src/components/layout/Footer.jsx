import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="bg-secondary text-white mt-auto">
      <div className="max-w-7xl mx-auto px-4 py-10">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div>
            <span className="text-3xl font-black text-primary">n11</span>
            <p className="mt-3 text-gray-400 text-sm">
              Türkiye&apos;nin en büyük alışveriş platformu.
              Milyonlarca ürün, uygun fiyat garantisiyle.
            </p>
          </div>

          <div>
            <h4 className="font-semibold mb-3">Kurumsal</h4>
            <ul className="space-y-2 text-sm text-gray-400">
              <li><a href="#" className="hover:text-white transition-colors">Hakkımızda</a></li>
              <li><a href="#" className="hover:text-white transition-colors">Kariyer</a></li>
              <li><a href="#" className="hover:text-white transition-colors">İletişim</a></li>
            </ul>
          </div>

          <div>
            <h4 className="font-semibold mb-3">Yardım</h4>
            <ul className="space-y-2 text-sm text-gray-400">
              <li><a href="#" className="hover:text-white transition-colors">Nasıl Sipariş Verilir?</a></li>
              <li><a href="#" className="hover:text-white transition-colors">İade & Değişim</a></li>
              <li><a href="#" className="hover:text-white transition-colors">SSS</a></li>
            </ul>
          </div>

          <div>
            <h4 className="font-semibold mb-3">Kategoriler</h4>
            <ul className="space-y-2 text-sm text-gray-400">
              <li><Link to="/?categoryId=1" className="hover:text-white transition-colors">Elektronik</Link></li>
              <li><Link to="/?categoryId=3" className="hover:text-white transition-colors">Moda</Link></li>
              <li><Link to="/?categoryId=4" className="hover:text-white transition-colors">Ev & Yaşam</Link></li>
            </ul>
          </div>
        </div>

        <div className="border-t border-gray-700 mt-8 pt-6 text-center text-sm text-gray-500">
          © {new Date().getFullYear()} n11 Marketplace. Tüm hakları saklıdır.
        </div>
      </div>
    </footer>
  );
}
