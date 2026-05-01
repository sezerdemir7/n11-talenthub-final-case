import { NavLink, Outlet, Link } from 'react-router-dom';
import {
  HiSquares2X2,
  HiTag,
  HiArrowLeftOnRectangle,
  HiClipboardDocumentList,
  HiUsers,
} from 'react-icons/hi2';
import { useAuth } from '../../context/AuthContext';

const navItems = [
  { to: '/admin', icon: HiSquares2X2, label: 'Dashboard', end: true },
  { to: '/admin/seller-applications', icon: HiClipboardDocumentList, label: 'Satıcı başvuruları' },
  { to: '/admin/sellers', icon: HiUsers, label: 'Satıcılar' },
  { to: '/admin/categories', icon: HiTag, label: 'Kategoriler' },
];

export default function AdminLayout() {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen flex bg-gray-100">
      {/* Sidebar */}
      <aside className="w-64 bg-secondary text-white flex flex-col shrink-0">
        <div className="p-6 border-b border-white/10">
          <Link to="/">
            <span className="text-2xl font-black text-primary">n11</span>
          </Link>
          <p className="text-xs text-gray-400 mt-1">Admin Panel</p>
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
                    ? 'bg-primary/20 text-primary border-r-3 border-primary'
                    : 'text-gray-400 hover:text-white hover:bg-white/5'
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
              {(user?.firstName || user?.email || 'A').charAt(0).toUpperCase()}
            </div>
            <div className="min-w-0">
              <p className="text-sm font-medium truncate">{user?.firstName || user?.email || 'Admin'}</p>
              <p className="text-xs text-gray-400 truncate">{user?.email}</p>
            </div>
          </div>
          <button
            onClick={logout}
            className="flex items-center gap-2 text-xs text-gray-400 hover:text-error transition-colors cursor-pointer"
          >
            <HiArrowLeftOnRectangle className="h-4 w-4" />
            Çıkış Yap
          </button>
        </div>
      </aside>

      {/* Main content */}
      <div className="flex-1 flex flex-col min-w-0">
        <header className="bg-white border-b px-8 py-4 flex items-center justify-between">
          <h1 className="text-lg font-bold text-secondary">Admin Panel</h1>
          <Link
            to="/"
            className="text-sm text-primary hover:underline font-medium"
          >
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
