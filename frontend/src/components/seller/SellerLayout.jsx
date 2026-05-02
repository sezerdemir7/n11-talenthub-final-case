import { useState } from 'react';
import { NavLink, Outlet, Link, useLocation } from 'react-router-dom';
import {
  HiSquares2X2,
  HiShoppingBag,
  HiBuildingStorefront,
  HiArrowLeftOnRectangle,
  HiBars3,
  HiXMark,
  HiArrowTopRightOnSquare,
} from 'react-icons/hi2';
import { useAuth } from '../../context/AuthContext';

const navItems = [
  { to: '/seller', icon: HiSquares2X2, label: 'Genel Bakış', end: true },
  { to: '/seller/store', icon: HiBuildingStorefront, label: 'Mağazam' },
  { to: '/seller/products', icon: HiShoppingBag, label: 'Ürünler' },
];

const pageTitles = {
  '/seller': 'Genel Bakış',
  '/seller/store': 'Mağazam',
  '/seller/products': 'Ürün Yönetimi',
};

function Sidebar({ onClose }) {
  const { user, logout } = useAuth();
  const initials = (user?.email || 'S').charAt(0).toUpperCase();

  return (
    <div className="w-64 bg-slate-900 text-white flex flex-col h-full">
      {/* Logo */}
      <div className="px-6 pt-6 pb-5 border-b border-white/8 flex items-center justify-between">
        <div>
          <Link to="/" className="block">
            <span className="text-2xl font-black text-primary tracking-tight">n11</span>
          </Link>
          <p className="text-[11px] text-slate-400 mt-0.5 font-medium uppercase tracking-widest">
            Satıcı Paneli
          </p>
        </div>
        {onClose && (
          <button
            type="button"
            onClick={onClose}
            className="lg:hidden text-slate-400 hover:text-white transition-colors"
          >
            <HiXMark className="h-5 w-5" />
          </button>
        )}
      </div>

      {/* Nav */}
      <nav className="flex-1 py-4 px-3 space-y-0.5">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            onClick={onClose}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all ${
                isActive
                  ? 'bg-primary/20 text-primary'
                  : 'text-slate-400 hover:text-white hover:bg-white/6'
              }`
            }
          >
            {({ isActive }) => (
              <>
                <item.icon className={`h-5 w-5 shrink-0 ${isActive ? 'text-primary' : ''}`} />
                {item.label}
              </>
            )}
          </NavLink>
        ))}
      </nav>

      {/* Footer */}
      <div className="px-3 pb-5 border-t border-white/8 pt-4 space-y-3">
        <div className="flex items-center gap-3 px-3">
          <div className="h-9 w-9 rounded-full bg-primary/25 flex items-center justify-center text-primary font-bold text-sm shrink-0">
            {initials}
          </div>
          <div className="min-w-0">
            <p className="text-xs font-semibold text-white truncate">Satıcı Hesabı</p>
            <p className="text-[11px] text-slate-500 truncate">{user?.email}</p>
          </div>
        </div>
        <div className="flex items-center gap-1 px-1">
          <button
            type="button"
            onClick={logout}
            className="flex items-center gap-2 px-3 py-2 w-full rounded-lg text-xs font-medium text-slate-400 hover:text-error hover:bg-error/10 transition-colors cursor-pointer"
          >
            <HiArrowLeftOnRectangle className="h-4 w-4" />
            Çıkış Yap
          </button>
        </div>
      </div>
    </div>
  );
}

export default function SellerLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();
  const pageTitle = pageTitles[location.pathname] ?? 'Satıcı Paneli';

  return (
    <div className="min-h-screen flex bg-slate-50">
      {/* Desktop sidebar */}
      <aside className="hidden lg:flex flex-col w-64 shrink-0 sticky top-0 h-screen">
        <Sidebar />
      </aside>

      {/* Mobile sidebar overlay */}
      {sidebarOpen && (
        <div className="lg:hidden fixed inset-0 z-40 flex">
          <div
            className="absolute inset-0 bg-black/50"
            onClick={() => setSidebarOpen(false)}
          />
          <div className="relative w-64 flex flex-col">
            <Sidebar onClose={() => setSidebarOpen(false)} />
          </div>
        </div>
      )}

      {/* Main */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Top bar */}
        <header className="bg-white border-b border-gray-100 px-6 py-4 flex items-center justify-between sticky top-0 z-30">
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={() => setSidebarOpen(true)}
              className="lg:hidden text-gray-500 hover:text-secondary transition-colors"
            >
              <HiBars3 className="h-6 w-6" />
            </button>
            <div>
              <h1 className="text-base font-bold text-secondary leading-none">{pageTitle}</h1>
              <p className="text-[11px] text-gray-400 mt-0.5">Satıcı Paneli</p>
            </div>
          </div>
          <Link
            to="/"
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-gray-500 hover:text-primary transition-colors"
          >
            <HiArrowTopRightOnSquare className="h-4 w-4" />
            Siteye Dön
          </Link>
        </header>

        <main className="flex-1 p-6 md:p-8 overflow-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
