import { Link } from "react-router-dom";
import { Ghost, ArrowLeft, ShoppingBag } from "lucide-react";
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";

const authLayoutStyles = {
  page: "halloween-surface min-h-dvh bg-background px-4 py-6 sm:px-6 lg:py-10",
  navigation: "mx-auto mb-8 flex max-w-6xl items-center justify-between gap-3",
  brand: "flex items-center gap-2 text-lg font-extrabold tracking-tight",
  columns: "mx-auto grid max-w-6xl items-center gap-8 lg:grid-cols-[1fr_1fr] lg:gap-16",
  story: "hidden min-w-0 overflow-hidden rounded-[2rem] bg-night text-white lg:block",
  storyCopy: "grid gap-5 p-9",
  heading: "market-display text-5xl",
  description: "max-w-sm text-sm leading-7 text-white/60",
  illustration: "aspect-[7/4] w-full object-cover object-[center_45%]",
  card: "w-full max-w-lg justify-self-center gap-6 rounded-3xl py-8 shadow-none",
  cardHeader: "px-7 sm:px-9",
  title: "text-3xl font-extrabold tracking-tight",
  content: "px-7 sm:px-9",
  footer: "justify-center px-7 text-xs text-muted-foreground",
};

export default function AuthLayout({ title, description, children }) {
  return (
    <div className={authLayoutStyles.page}>
      {/* Navigation */}
      <nav aria-label="Điều hướng đăng nhập" className={authLayoutStyles.navigation}>
        <div className="flex items-center gap-2 border-1 border-slate-800/20 rounded-2xl bg-orange-500/10 px-3 py-1.5 text-sm text-orange-500">
          <Link to="/" className={authLayoutStyles.brand} >
          <ShoppingBag className="size-5 text-primary" aria-hidden="true" />
          Market Lane.
        </Link>
        </div>
        <Button asChild variant="ghost">
          <Link to="/">
            <ArrowLeft data-icon="inline-start" aria-hidden="true" />
            Về khu chợ
          </Link>
        </Button>
      </nav>

      {/* Main Content Columns */}
      <div className={authLayoutStyles.columns}>
        {/* Left Side: Story & Illustration (Desktop Only) */}
        <aside className={authLayoutStyles.story}>
          <div className={authLayoutStyles.storyCopy}>
            <Badge variant="outline" className="w-fit border-white/20 text-white">
              <Ghost aria-hidden="true" />
              Halloween edition
            </Badge>
            <h2 className={authLayoutStyles.heading}>
              Một góc chợ.
              <br />
              Nhiều điều hay.
            </h2>
            <p className={authLayoutStyles.description}>
              Ghé các cửa hàng, lưu những món yêu thích vào giỏ và theo dõi đơn
              hàng trong tài khoản của bạn.
            </p>
          </div>
          <img
            src="/halloween-market.svg"
            alt="Bí ngô và chú ma Halloween"
            width="700"
            height="620"
            className={authLayoutStyles.illustration}
          />
        </aside>

        {/* Right Side: Auth Form Card */}
        <Card className={authLayoutStyles.card}>
          <CardHeader className={authLayoutStyles.cardHeader}>
            <CardTitle className={authLayoutStyles.title}>
              <h1>{title}</h1>
            </CardTitle>
            <CardDescription>{description}</CardDescription>
          </CardHeader>
          <CardContent className={authLayoutStyles.content}>
            {children}
          </CardContent>
          <CardFooter className={authLayoutStyles.footer}>
            Market Lane · Mua sắm theo cách của bạn
          </CardFooter>
        </Card>
      </div>
    </div>
  );
}