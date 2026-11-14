const adminSectionPageStyles = {
  section: "min-h-64 rounded-2xl border border-border bg-card p-5 sm:p-7",
  title: "text-2xl font-extrabold tracking-tight",
  description: "mt-3 text-sm leading-7 text-muted-foreground",
};

export default function AdminSectionPage({ title }) {
  return <section className={adminSectionPageStyles.section}><h2 className={adminSectionPageStyles.title}>{title}</h2><p className={adminSectionPageStyles.description}>Chức năng đang chờ phát triển.</p></section>;
}
