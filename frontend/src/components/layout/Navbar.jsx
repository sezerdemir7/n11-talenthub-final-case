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
    }
  };

  return (
    <header className="bg-white shadow-sm sticky top-0 z-50">
      {/* Main navbar */}
      <div className="max-w-7xl mx-auto px-4 py-3">
        <div className="flex items-center gap-4 lg:gap-8">
          {/* Logo */}
          <Link to="/" className="shrink-0">
            <span className="text-3xl font-black text-primary tracking-tight">n11</span>
          </Link>

          {/* Search */}
          <form onSubmit={handleSearch} className="hidden md:flex flex-1 max-w-2xl">
            <div className="flex w-full">
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Ürün, kategori veya marka ara..."
                className="flex-1 px-4 py-2.5 border-2 border-r-0 border-gray-200 rounded-l-lg focus:outline-none focus:border-primary transition-colors"
              />
              <button
                type="submit"
                className="px-5 bg-primary text-white rounded-r-lg hover:bg-primary/90 transition-colors cursor-pointer"
              >
                <HiMagnifyingGlass className="h-5 w-5" />
              </button>
            </div>
          </form>

          {/* Actions */}
          <div className="flex items-center gap-2 ml-auto">
            {isAuthenticated ? (
              <div className="hidden md:flex items-center gap-3">
                {isAdmin && (
                  <Link
                    to="/admin"
                    className="flex items-center gap-1.5 text-secondary hover:text-primary transition-colors text-sm font-medium"
                  >
                    <HiCog6Tooth className="h-5 w-5" />
                    <span>Admin</span>
                  </Link>
                )}
                {isSeller && (
                  <Link
                    to="/seller"
                    className="flex items-center gap-1.5 text-secondary hover:text-primary transition-colors text-sm font-medium"
                  >
                    <HiBuildingStorefront className="h-5 w-5" />
                    <span>Satıcı paneli</span>
                  </Link>
                )}
                <Link
                  to="/account"
                  className="flex items-center gap-1.5 text-secondary hover:text-primary transition-colors text-sm font-medium"
                >
                  <HiUser className="h-5 w-5" />
                  <span>Hesabım</span>
                </Link>
                <button
                  onClick={logout}
                  className="text-sm text-gray-500 hover:text-primary transition-colors cursor-pointer"
                >
                  Çıkış
                </button>
              </div>
            ) : (
              <div className="hidden md:flex items-center gap-2">
                <Link
                  to="/login"
                  className="px-4 py-2 text-sm font-medium text-primary border border-primary rounded-lg hover:bg-primary hover:text-white transition-all"
                >
                  Giriş Yap
                </Link>
                <Link
                  to="/register/seller"
                  className="px-4 py-2 text-sm font-medium text-white bg-secondary rounded-lg hover:bg-secondary/90 transition-all"
                >
                  Satıcı olun
                </Link>
                <Link
                  to="/register"
                  className="px-4 py-2 text-sm font-medium text-white bg-primary rounded-lg hover:bg-primary/90 transition-all"
                >
                  Üye Ol
                </Link>
              </div>
            )}

            {/* Cart */}
            <Link
              to="/cart"
              className="relative flex items-center gap-1.5 px-3 py-2 text-secondary hover:text-primary transition-colors"
            >
              <HiShoppingCart className="h-6 w-6" />
              {itemCount > 0 && (
                <span className="absolute -top-1 -right-1 bg-primary text-white text-xs font-bold rounded-full h-5 w-5 flex items-center justify-center">
                  {itemCount > 99 ? '99+' : itemCount}
                </span>
              )}
              <span className="hidden lg:inline text-sm font-medium">Sepetim</span>
            </Link>

            {/* Mobile menu toggle */}
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="md:hidden p-2 text-secondary cursor-pointer"
            >
              {mobileMenuOpen ? <HiXMark className="h-6 w-6" /> : <HiBars3 className="h-6 w-6" />}
            </button>
          </div>
        </div>

        {/* Mobile search */}
        <form onSubmit={handleSearch} className="md:hidden mt-3">
          <div className="flex w-full">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Ürün, kategori veya marka ara..."
              className="flex-1 px-4 py-2.5 border-2 border-r-0 border-gray-200 rounded-l-lg focus:outline-none focus:border-primary"
            />
            <button
              type="submit"
              className="px-5 bg-primary text-white rounded-r-lg hover:bg-primary/90 cursor-pointer"
            >
              <HiMagnifyingGlass className="h-5 w-5" />
            </button>
          </div>
        </form>
      </div>

      {/* Mobile menu */}
      {mobileMenuOpen && (
        <div className="md:hidden border-t bg-white px-4 py-3 space-y-2">
          {isAuthenticated ? (
            <>
              <Link
                to="/account"
                onClick={() => setMobileMenuOpen(false)}
                className="flex items-center gap-2 py-2 text-secondary hover:text-primary"
              >
                <HiUser className="h-5 w-5" />
                <span className="font-medium">Hesabım</span>
              </Link>
              {isAdmin && (
                <Link
                  to="/admin"
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex items-center gap-2 py-2 text-secondary hover:text-primary"
                >
                  <HiCog6Tooth className="h-5 w-5" />
                  <span className="font-medium">Admin Panel</span>
                </Link>
              )}
              {isSeller && (
                <Link
                  to="/seller"
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex items-center gap-2 py-2 text-secondary hover:text-primary"
                >
                  <HiBuildingStorefront className="h-5 w-5" />
                  <span className="font-medium">Satıcı paneli</span>
                </Link>
              )}
              <button
                onClick={() => { logout(); setMobileMenuOpen(false); }}
                className="w-full text-left py-2 text-gray-500 hover:text-primary cursor-pointer"
              >
                Çıkış Yap
              </button>
            </>
          ) : (
            <div className="flex flex-col gap-2">
              <Link
                to="/login"
                onClick={() => setMobileMenuOpen(false)}
                className="py-2 text-center text-primary border border-primary rounded-lg font-medium"
              >
                Giriş Yap
              </Link>
              <Link
                to="/register/seller"
                onClick={() => setMobileMenuOpen(false)}
                className="py-2 text-center text-white bg-secondary rounded-lg font-medium"
              >
                Satıcı olun
              </Link>
              <Link
                to="/register"
                onClick={() => setMobileMenuOpen(false)}
                className="py-2 text-center text-white bg-primary rounded-lg font-medium"
              >
                Üye Ol
              </Link>
            </div>
          )}
        </div>
      )}
    </header>
  );
}
