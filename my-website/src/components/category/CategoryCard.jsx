import { useState } from "react";
import { Shapes } from "lucide-react";
import { cn } from "@/lib/utils";
const categoryStyles = {
  card: "group flex min-h-32 w-full min-w-0 flex-col items-center justify-center gap-3 rounded-2xl border px-3 py-5 text-center transition-colors",
  selected: "border-primary bg-accent text-accent-foreground",
  idle: "border-border bg-card text-foreground hover:border-primary/40 hover:bg-accent/40",
  imageWrap: "grid size-14 place-items-center overflow-hidden rounded-xl bg-muted/60 sm:size-16",
  image: "size-full object-contain p-1.5",
  title: "line-clamp-2 text-xs font-semibold leading-5 sm:text-sm",
};
export default function CategoryCard({category,selected=false,onSelect}) {
  const [failedUrl,setFailedUrl] = useState(null);
  const image = typeof category.imageUrl === "string" && /^https?:\/\//i.test(category.imageUrl) ? category.imageUrl : null;
  return <button type="button" aria-pressed={selected} onClick={() => onSelect(category.id)} className={cn(categoryStyles.card,selected ? categoryStyles.selected : categoryStyles.idle)}><span className={categoryStyles.imageWrap}>{image && failedUrl !== image ? <img src={image} alt="" loading="lazy" decoding="async" onError={() => setFailedUrl(image)} className={categoryStyles.image} /> : <Shapes className="size-6 text-primary" aria-hidden="true" />}</span><span className={categoryStyles.title}>{category.name}</span></button>;
}
