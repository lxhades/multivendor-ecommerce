import { UserRound } from "lucide-react";
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
const roles = { BUYER:"Người mua",SELLER:"Người bán",ADMIN:"Quản trị viên" };
const statuses = { ACTIVE:"Đang hoạt động",INACTIVE:"Chưa kích hoạt",LOCKED:"Đã khóa",BLOCKED:"Đã khóa",DELETED:"Đã xóa" };
const profileInfoStyles = { card:"rounded-3xl shadow-none", heading:"flex items-start gap-4", icon:"grid size-12 shrink-0 place-items-center rounded-2xl bg-accent text-primary", title:"text-xl font-bold", fields:"grid gap-6 sm:grid-cols-2", label:"text-xs text-muted-foreground", value:"mt-2 break-words font-semibold", footer:"gap-2 border-t border-border pt-5" };
export default function ProfileInfo({user}) {
  const fields=[["Email",user.email],["Số điện thoại",user.phone]];
  return <Card className={profileInfoStyles.card}><CardHeader><div className={profileInfoStyles.heading}><span className={profileInfoStyles.icon}><UserRound className="size-6" aria-hidden="true" /></span><div><CardTitle className={profileInfoStyles.title}><h2>Thông tin tài khoản</h2></CardTitle><CardDescription className="mt-2">Thông tin liên hệ cho tài khoản của bạn.</CardDescription></div></div></CardHeader><CardContent><dl className={profileInfoStyles.fields}>{fields.map(([label,value]) => <div key={label}><dt className={profileInfoStyles.label}>{label}</dt><dd className={profileInfoStyles.value}>{value || "Chưa cập nhật"}</dd></div>)}</dl></CardContent><CardFooter className={profileInfoStyles.footer}><Badge variant="secondary">{roles[user.role] ?? user.role}</Badge><Badge variant="outline">{statuses[user.status] ?? user.status}</Badge></CardFooter></Card>;
}
