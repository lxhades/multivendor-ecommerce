const sellerSectionPageStyles = {
  section: "min-h-64 rounded-2xl border border-border bg-card p-5 sm:p-7",
  title: "text-xl font-bold text-foreground",
  description: "mt-3 text-sm leading-7 text-muted-foreground",
};

export default function SellerSectionPage({ title }) {
  return <section className={sellerSectionPageStyles.section}>
    <h2 className={sellerSectionPageStyles.title}>{title}</h2>
    <p className={sellerSectionPageStyles.description}>Chức năng đang chờ phát triển.</p>
  </section>;
}
