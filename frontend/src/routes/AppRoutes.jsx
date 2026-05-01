import { Routes, Route, Outlet } from 'react-router-dom';
import Layout from '../components/layout/Layout';
import AdminLayout from '../components/admin/AdminLayout';
import ProtectedRoute from '../components/auth/ProtectedRoute';
import HomePage from '../pages/HomePage';
import ProductDetailPage from '../pages/ProductDetailPage';
import CartPage from '../pages/CartPage';
import CheckoutPage from '../pages/CheckoutPage';
import PaymentPage from '../pages/PaymentPage';
import PaymentSuccessPage from '../pages/PaymentSuccessPage';
import AccountPage from '../pages/AccountPage';
import AccountAddressesPage from '../pages/AccountAddressesPage';
import OrderDetailPage from '../pages/OrderDetailPage';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import RegisterSellerPage from '../pages/RegisterSellerPage';
import SellerLayout from '../components/seller/SellerLayout';
import AdminDashboard from '../pages/admin/AdminDashboard';
import AdminCategories from '../pages/admin/AdminCategories';
import AdminProducts from '../pages/admin/AdminProducts';
import AdminSellerApplications from '../pages/admin/AdminSellerApplications';
import AdminSellersPage from '../pages/admin/AdminSellersPage';
import SellerDashboard from '../pages/seller/SellerDashboard';
import SellerStorePage from '../pages/seller/SellerStorePage';

export default function AppRoutes() {
  return (
    <Routes>
      {/* Public + Customer Routes */}
      <Route element={<Layout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/product/:id" element={<ProductDetailPage />} />
        <Route path="/cart" element={<CartPage />} />
        <Route
          path="/checkout"
          element={
            <ProtectedRoute>
              <CheckoutPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/checkout/pay/:orderId"
          element={
            <ProtectedRoute>
              <PaymentPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/checkout/success"
          element={
            <ProtectedRoute>
              <PaymentSuccessPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/account"
          element={
            <ProtectedRoute>
              <Outlet />
            </ProtectedRoute>
          }
        >
          <Route index element={<AccountPage />} />
          <Route path="addresses" element={<AccountAddressesPage />} />
          <Route path="orders/:orderId" element={<OrderDetailPage />} />
        </Route>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/register/seller" element={<RegisterSellerPage />} />
      </Route>

      {/* Seller panel */}
      <Route
        path="/seller"
        element={
          <ProtectedRoute requiredRole="SELLER">
            <SellerLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<SellerDashboard />} />
        <Route path="store" element={<SellerStorePage />} />
        <Route path="products" element={<AdminProducts />} />
      </Route>

      {/* Admin Routes */}
      <Route
        path="/admin"
        element={
          <ProtectedRoute requiredRole="ADMIN">
            <AdminLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<AdminDashboard />} />
        <Route path="seller-applications" element={<AdminSellerApplications />} />
        <Route path="sellers" element={<AdminSellersPage />} />
        <Route path="categories" element={<AdminCategories />} />
      </Route>
    </Routes>
  );
}
