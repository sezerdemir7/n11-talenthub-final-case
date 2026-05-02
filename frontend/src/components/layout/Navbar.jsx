import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  HiShoppingCart,
  HiUser,
  HiMagnifyingGlass,
  HiBars3,
  HiXMark,
  HiCog6Tooth,
  HiBuildingStorefront,
  HiArrowRightOnRectangle,
} from 'react-icons/hi2';
import { useAuth } from '../../context/AuthContext';
import { useCart } from '../../context/CartContext';
import { hasRole } from '../../utils/jwt';

export default function Navbar() {
  const { isAuthenticated, user, logout } = useAuth();
  const { itemCount } = useCart();
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const isAdmin = hasRole(user, 'ADMIN');
  const isSeller = hasRole(user, 'SELLER');

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/?keyword=${encodeURIComponent(searchQuery.trim())}`);
      setSearchQuery('');
      setMobileMenuOpen(false);
    }
  };

  const userInitial = user?.firstName?.charAt(0)?.toUpperCase() || user?.email?.charAt(0)?.toUpperCase() || 'U';

  return (
    <header className="bg-white border-b border-gray-200 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4">
        <div className="flex items-center gap-4 lg:gap-6 h-16">

          {/* Logo */}
          <Link to="/" className="shrink-0 flex items-center">
            <span className="text-2xl font-black text-primary tracking-tight leading-none">n11</span>
          </Link>

          {/* Desktop Search */}
          <form onSubmit={handleSearch} className="hidden md:flex flex-1 max-w-xl">
            <div className="flex w-full rounded-lg border-2 border-gray-200 focus-within:border-primary transition-colors overflow-hidden">
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Ürün, kategori veya marka ara..."
                className="flex-1 px-4 py-2 text-sm text-secondary placeholder:text-gray-400 focus:outline-none bg-white"
              />
              <button
                type="submit"
                className="px-4 bg-primary text-white hover:bg-primary/90 transition-colors cursor-pointer flex items-center"
              >
                <HiMagnifyingGlass className="h-4 w-4" />
              </button>
            </div>
          </form>

          {/* Desktop Actions */}
          <div className="hidden md:flex items-center gap-1 ml-auto">
            {isAuthenticated ? (
              <>
                {isAdmin && (
                  <Link
                    to="/admin"
                    className="flex items-center gap-1.5 px-3 py-2 rounded-lg text-sm font-medium text-gray-600 hover:text-primary hover:bg-gray-50 transition-colors"
                  >
                    <HiCog6Tooth className="h-4 w-4" />
                    Admin
                  </Link>
                )}
                {isSeller && (
                  <Link
                    to="/seller"
                    className="flex items-center gap-1.5 px-3 py-2 rounded-lg text-sm font-medium text-gray-600 hover:text-primary hover:bg-gray-50 transition-colors"
                  >
                    <HiBuildingStorefront className="h-4 w-4" />
                    Satıcı Paneli
                  </Link>
                )}
                <Link
                  to="/account"
                  className="flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium text-gray-600 hover:text-primary hover:bg-gray-50 transition-colors"
                >
                  <div className="h-7 w-7 rounded-full bg-primary/15 flex items-center justify-center">
                    <span className="text-xs font-bold text-primary">{userInitial}</span>
                  </div>
                  <span>Hesabım</span>
                </Link>
                <button
                  onClick={logout}
                  className="flex items-center gap-1.5 px-3 py-2 rounded-lg text-sm font-medium text-gray-500 hover:text-error hover:bg-gray-50 transition-colors cursor-pointer"
                >
                  <HiArrowRightOnRectangle className="h-4 w-4" />
                  Çıkış
                </button>
              </>
            ) : (
              <div className="flex items-center gap-2">
                <Link
                  to="/login"
                  className="flex items-center gap-1.5 px-3 py-2 rounded-lg text-sm font-medium text-gray-600 hover:text-primary hover:bg-gray-50 transition-colors"
                >
                  <HiUser className="h-4 w-4" />
                  Giriş Yap
                </Link>
                <Link
                  to="/register/seller"
                  className="px-3 py-2 rounded-lg text-sm font-medium text-white bg-secondary hover:bg-secondary/90 transition-colors"
                >
                  Satıcı Ol
                </Link>
                <Link
                  to="/register"
                  className="px-3 py-2 rounded-lg text-sm font-semibold text-white bg-primary hover:bg-primary/90 transition-colors"
                >
                  Üye Ol
                </Link>
              </div>
            )}
          </div>

          {/* Cart */}
          <Link
            to="/cart"
            className="relative flex items-center gap-1.5 px-3 py-2 rounded-lg text-sm font-medium text-gray-600 hover:text-primary hover:bg-gray-50 transition-colors"
          >
            <div className="relative">
              <HiShoppingCart className="h-5 w-5" />
              {itemCount > 0 && (
                <span className="absolute -top-2 -right-2 bg-primary text-white text-[10px] font-bold rounded-full h-4 w-4 flex items-center justify-center leading-none">
                  {itemCount > 99 ? '99+' : itemCount}
                </span>
              )}
            </div>
            <span className="hidden lg:inline">Sepetim</span>
          </Link>

          {/* Mobile menu toggle */}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="md:hidden p-2 rounded-lg text-gray-600 hover:bg-gray-50 transition-colors cursor-pointer"
          >
            {mobileMenuOpen ? <HiXMark className="h-5 w-5" /> : <HiBars3 className="h-5 w-5" />}
          </button>
        </div>

        {/* Mobile Search */}
        <form onSubmit={handleSearch} className="md:hidden pb-3">
          <div className="flex rounded-lg border-2 border-gray-200 focus-within:border-primary transition-colors overflow-hidden">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Ürün ara..."
              className="flex-1 px-4 py-2.5 text-sm text-secondary placeholder:text-gray-400 focus:outline-none bg-white"
            />
            <button
              type="submit"
              className="px-4 bg-primary text-white hover:bg-primary/90 transition-colors cursor-pointer flex items-center"
            >
              <HiMagnifyingGlass className="h-4 w-4" />
            </button>
          </div>
        </form>
      </div>

      {/* Mobile Menu */}
      {mobileMenuOpen && (
        <div className="md:hidden border-t border-gray-100 bg-white px-4 py-3 space-y-1">
          {isAuthenticated ? (
            <>
              <div className="flex items-center gap-3 px-2 py-3 border-b border-gray-100 mb-2">
                <div className="h-9 w-9 rounded-full bg-primary/15 flex items-center justify-center">
                  <span className="text-sm font-bold text-primary">{userInitial}</span>
                </div>
                <div>
                  <p className="text-sm font-semibold text-secondary">{user?.firstName || 'Hesabım'}</p>
                  <p className="text-xs text-gray-500">{user?.email}</p>
                </div>
              </div>
              <Link
                to="/account"
                onClick={() => setMobileMenuOpen(false)}
                className="flex items-center gap-2 px-2 py-2.5 rounded-lg text-sm font-medium text-gray-700 hover:text-primary hover:bg-gray-50"
              >
                <HiUser className="h-4 w-4" />
                Hesabım
              </Link>
              {isAdmin && (
                <Link
                  to="/admin"
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex items-center gap-2 px-2 py-2.5 rounded-lg text-sm font-medium text-gray-700 hover:text-primary hover:bg-gray-50"
                >
                  <HiCog6Tooth className="h-4 w-4" />
                  Admin Panel
                </Link>
              )}
              {isSeller && (
                <Link
                  to="/seller"
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex items-center gap-2 px-2 py-2.5 rounded-lg text-sm font-medium text-gray-700 hover:text-primary hover:bg-gray-50"
                >
                  <HiBuildingStorefront className="h-4 w-4" />
                  Satıcı Paneli
                </Link>
              )}
              <button
                onClick={() => { logout(); setMobileMenuOpen(false); }}
                className="flex items-center gap-2 px-2 py-2.5 rounded-lg text-sm font-medium text-gray-500 hover:text-error hover:bg-gray-50 w-full cursor-pointer"
              >
                <HiArrowRightOnRectangle className="h-4 w-4" />
                Çıkış Yap
              </button>
            </>
          ) : (
            <div className="flex flex-col gap-2 py-2">
              <Link
                to="/login"
                onClick={() => setMobileMenuOpen(false)}
                className="flex items-center justify-center gap-2 py-2.5 rounded-lg border-2 border-primary text-sm font-semibold text-primary hover:bg-primary hover:text-white transition-colors"
              >
                Giriş Yap
              </Link>
              <Link
                to="/register"
                onClick={() => setMobileMenuOpen(false)}
                className="flex items-center justify-center py-2.5 rounded-lg text-sm font-semibold text-white bg-primary hover:bg-primary/90 transition-colors"
              >
                Üye Ol
              </Link>
              <Link
                to="/register/seller"
                onClick={() => setMobileMenuOpen(false)}
                className="flex items-center justify-center py-2.5 rounded-lg text-sm font-semibold text-white bg-secondary hover:bg-secondary/90 transition-colors"
              >
                Satıcı Ol
              </Link>
            </div>
          )}
        </div>
      )}
    </header>
  );
}
