import { NavLink, Outlet, Link } from 'react-router-dom';
import {
  HiSquares2X2,
  HiShoppingBag,
  HiBuildingStorefront,
  HiArrowLeftOnRectangle,
} from 'react-icons/hi2';
import { useAuth } from '../../context/AuthContext';

const navItems = [
  { to: '/seller', icon: HiSquares2X2, label: 'Özet', end: true },
  { to: '/seller/store', icon: HiBuildingStorefront, label: 'Mağazam' },
  { to: '/seller/products', icon: HiShoppingBag, label: 'Ürünler' },
];

export default function SellerLayout() {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen flex bg-slate-100">
      <aside className="w-64 bg-slate-900 text-white flex flex-col shrink-0">
        <div className="p-6 border-b border-white/10">
          <Link to="/">
            <span className="text-2xl font-black text-primary">n11</span>
          </Link>
          <p className="text-xs text-slate-400 mt-1">Satıcı Paneli</p>
        </div>

        <nav className="flex-1 py-4">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                `flex items-center gap-3 px-6 py-3 text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-primary/25 text-primary border-r-4 border-primary'
                    : 'text-slate-400 hover:text-white hover:bg-white/5'
                }`
              }
            >
              <item.icon className="h-5 w-5" />
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="p-4 border-t border-white/10">
          <div className="flex items-center gap-3 mb-3">
            <div className="h-8 w-8 rounded-full bg-primary/20 flex items-center justify-center text-primary font-bold text-sm">
              {(user?.email || 'S').charAt(0).toUpperCase()}
            </div>
            <div className="min-w-0">
              <p className="text-sm font-medium truncate">Satıcı</p>
              <p className="text-xs text-slate-400 truncate">{user?.email}</p>
            </div>
          </div>
          <button
            type="button"
            onClick={logout}
            className="flex items-center gap-2 text-xs text-slate-400 hover:text-error transition-colors cursor-pointer"
          >
            <HiArrowLeftOnRectangle className="h-4 w-4" />
            Çıkış Yap
          </button>
        </div>
      </aside>

      <div className="flex-1 flex flex-col min-w-0">
        <header className="bg-white border-b px-8 py-4 flex items-center justify-between">
          <h1 className="text-lg font-bold text-secondary">Satıcı Paneli</h1>
          <Link to="/" className="text-sm text-primary hover:underline font-medium">
            Siteye Dön
          </Link>
        </header>
        <main className="flex-1 p-8 overflow-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
