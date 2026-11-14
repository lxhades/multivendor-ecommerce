import { Link } from "react-router-dom";
import { UserRound, Package, Store, ShieldCheck, LogOut, ChevronDown } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { DropdownMenu, DropdownMenuTrigger, DropdownMenuContent, DropdownMenuLabel, DropdownMenuGroup, DropdownMenuItem, DropdownMenuSeparator } from "@/components/ui/dropdown-menu";
const accountStyles = {
  status:"max-w-40 text-xs text-muted-foreground",
  guest:"flex items-center gap-1.5",
  register:"hidden sm:inline-flex",
  trigger:"gap-2 rounded-xl px-2 sm:px-3",
  avatar:"size-7",
  avatarFallback:"bg-accent text-xs font-bold text-accent-foreground",
  email:"hidden max-w-32 truncate text-xs sm:block",
  content:"w-64 rounded-2xl p-2",
  label:"grid gap-1 px-2 py-3",
  labelEmail:"break-all text-xs font-normal text-muted-foreground",
  item:"min-h-11 rounded-lg",
};
export default function AccountMenu({auth}) {
  if (auth.signingOut || auth.checking) return <p role="status" className={accountStyles.status}>{auth.signingOut ? "Đang đăng xuất…" : "Đang xác minh phiên…"}</p>;
  if (auth.sessionError) return <Button type="button" variant="outline" onClick={auth.retry}>Thử lại tài khoản</Button>;
  if (!auth.user) return <div className={accountStyles.guest}><Button asChild variant="ghost"><Link to="/login">Đăng nhập</Link></Button><Button asChild className={accountStyles.register}><Link to="/register">Đăng ký</Link></Button></div>;
  return <DropdownMenu><DropdownMenuTrigger asChild><Button type="button" variant="ghost" className={accountStyles.trigger} aria-label="Mở menu tài khoản"><Avatar className={accountStyles.avatar}><AvatarFallback className={accountStyles.avatarFallback}>{auth.user.email?.slice(0,1).toUpperCase() || "U"}</AvatarFallback></Avatar><span className={accountStyles.email}>{auth.user.email}</span><ChevronDown data-icon="inline-end" aria-hidden="true" /></Button></DropdownMenuTrigger><DropdownMenuContent align="end" className={accountStyles.content}><DropdownMenuLabel className={accountStyles.label}>Tài khoản của bạn<span className={accountStyles.labelEmail}>{auth.user.email}</span></DropdownMenuLabel><DropdownMenuSeparator /><DropdownMenuGroup><DropdownMenuItem asChild className={accountStyles.item}><Link to="/profile"><UserRound aria-hidden="true" />Hồ sơ cá nhân</Link></DropdownMenuItem><DropdownMenuItem asChild className={accountStyles.item}><Link to="/orders"><Package aria-hidden="true" />Đơn hàng của tôi</Link></DropdownMenuItem>{auth.user.role === "SELLER" && <DropdownMenuItem asChild className={accountStyles.item}><Link to="/seller"><Store aria-hidden="true" />Quản lý cửa hàng</Link></DropdownMenuItem>}{auth.user.role === "ADMIN" && <DropdownMenuItem asChild className={accountStyles.item}><Link to="/admin"><ShieldCheck aria-hidden="true" />Quản trị</Link></DropdownMenuItem>}</DropdownMenuGroup><DropdownMenuSeparator /><DropdownMenuGroup><DropdownMenuItem variant="destructive" disabled={auth.signingOut} onSelect={() => void auth.signOut()} className={accountStyles.item}><LogOut aria-hidden="true" />Đăng xuất</DropdownMenuItem></DropdownMenuGroup></DropdownMenuContent></DropdownMenu>;
}
