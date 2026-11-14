import StorefrontScroll from "./StorefrontScroll.jsx";
import { useCart } from "../cart/useCart.js";
import { Outlet } from "react-router-dom";
import Header from "./Header.jsx";
import Footer from "./Footer.jsx";

export default function MainLayout({ auth }) {
  const owner = auth.checking || auth.sessionError ? null : auth.user?.userId ?? "guest";
  return <CartLayout key={owner ?? "pending"} auth={auth} owner={owner} />;
}

function CartLayout({ auth, owner }) {
  const cart = useCart(owner);
  return (
    <div className="halloween-surface flex min-h-dvh flex-col bg-background text-foreground">
      <StorefrontScroll />
      <Header auth={auth} cart={cart} />
      <main id="main-content" tabIndex={-1} className="min-w-0 flex-1 outline-none">
        <Outlet context={{ auth, cart }} /> 
        
      </main>
      <Footer />
    </div>
  );
}