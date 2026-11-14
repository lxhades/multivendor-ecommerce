import { lazy } from "react";
import RoutePage from "./components/common/RoutePage.jsx";

import AdminLayout from "./components/admin/AdminLayout.jsx";

import RefreshSessionOnNavigation from "./components/auth/RefreshSessionOnNavigation.jsx";

import SellerLayout from "./components/seller/SellerLayout.jsx";

import { useAuth } from "./components/auth/useAuth.js";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import MainLayout from "./components/common/MainLayout.jsx";
import HomePage from "./pages/home/HomePage.jsx";
import ScrollToTop from "./components/checkout/ScrollToTop.jsx";

const AdminRevenuePage = lazy(() => import("./pages/admin/AdminRevenuePage.jsx"));
const AdminOrdersPage = lazy(() => import("./pages/admin/AdminOrdersPage.jsx"));
const AdminProductsPage = lazy(() => import("./pages/admin/AdminProductsPage.jsx"));
const AdminCategoriesPage = lazy(() => import("./pages/admin/AdminCategoriesPage.jsx"));
const AdminShopsPage = lazy(() => import("./pages/admin/AdminShopsPage.jsx"));
const AdminUsersPage = lazy(() => import("./pages/admin/AdminUsersPage.jsx"));
const AdminHomePage = lazy(() => import("./pages/admin/AdminHomePage.jsx"));
const ShopListPage = lazy(() => import("./pages/shop/ShopListPage.jsx"));
const SearchPage = lazy(() => import("./pages/search/SearchPage.jsx"));
const SellerReportsPage = lazy(() => import("./pages/seller/SellerReportsPage.jsx"));
const SellerOrdersPage = lazy(() => import("./pages/seller/SellerOrdersPage.jsx"));
const SellerProductsPage = lazy(() => import("./pages/seller/SellerProductsPage.jsx"));
const SellerShopPage = lazy(() => import("./pages/seller/SellerShopPage.jsx"));
const ShopPage = lazy(() => import("./pages/shop/ShopPage.jsx"));
const OrdersPage = lazy(() => import("./pages/order/OrdersPage.jsx"));
const ProfilePage = lazy(() => import("./pages/profile/ProfilePage.jsx"));
const CartPage = lazy(() => import("./pages/cart/CartPage"));
const ProductDetailPage = lazy(() => import("./pages/product/ProductDetailPage"));
const LoginPage = lazy(() => import("./pages/auth/LoginPage"));
const RegisterPage = lazy(() => import("./pages/auth/RegisterPage"));

export default function App() {
  const auth = useAuth();
  return <BrowserRouter>
   <RefreshSessionOnNavigation auth={auth} />
   <ScrollToTop />
     <Routes>
        {/* CÁC TRANG DÙNG CHUNG HEADER/FOOTER =*/}
        <Route element={<MainLayout auth={auth} />}>
          <Route path="/" element={<RoutePage><HomePage /></RoutePage>} />
          <Route path="/search" element={<RoutePage><SearchPage /></RoutePage>} />
          <Route path="/search/danh-muc/:categorySlug" element={<RoutePage><SearchPage /></RoutePage>} />
          <Route path="/products/:id" element={<RoutePage><ProductDetailPage /></RoutePage>} />
          <Route path="/products/:productSlug/:id" element={<RoutePage><ProductDetailPage /></RoutePage>} />
          <Route path="/shops" element={<RoutePage><ShopListPage /></RoutePage>} />
          <Route path="/shops/:id" element={<RoutePage><ShopPage /></RoutePage>} />
          <Route path="/cart" element={<RoutePage><CartPage /></RoutePage>} />
          <Route path="/profile" element={<RoutePage><ProfilePage /></RoutePage>} />
          <Route path="/orders" element={<RoutePage><OrdersPage /></RoutePage>} />
          <Route path="/admin" element={<AdminLayout />}>
            <Route index element={<RoutePage><AdminHomePage /></RoutePage>} />
            <Route path="users" element={<RoutePage><AdminUsersPage /></RoutePage>} />
            <Route path="shops" element={<RoutePage><AdminShopsPage /></RoutePage>} />
            <Route path="categories" element={<RoutePage><AdminCategoriesPage /></RoutePage>} />
            <Route path="products" element={<RoutePage><AdminProductsPage /></RoutePage>} />
            <Route path="orders" element={<RoutePage><AdminOrdersPage /></RoutePage>} />
            <Route path="reports" element={<RoutePage><AdminRevenuePage /></RoutePage>} />
          </Route>
          <Route path="/seller" element={<SellerLayout />}>
            <Route index element={<Navigate to="shop" replace />} />
            <Route path="shop" element={<RoutePage><SellerShopPage /></RoutePage>} />
            <Route path="products" element={<RoutePage><SellerProductsPage /></RoutePage>} />
            <Route path="orders" element={<RoutePage><SellerOrdersPage /></RoutePage>} />
            <Route path="reports" element={<RoutePage><SellerReportsPage /></RoutePage>} />
          </Route>

          
        </Route>

        {/* CÁC TRANG KHÔNG DÙNG HEADER/FOOTER  */}
        <Route path="/login" element={<RoutePage><LoginPage auth={auth} /></RoutePage>} />
        <Route path="/register" element={<RoutePage><RegisterPage auth={auth} /></RoutePage>} />
      </Routes>
  </BrowserRouter>;
}
