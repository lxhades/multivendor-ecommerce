import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useEffect, useRef, useState } from "react";
import { isMediaUrl } from "../product/productHelpers.js";
import { toast } from "../common/toast.js";

export default function SellerShopImageField({ label, currentUrl, selection, onSelect, pending, round = false }) {
  function choose(event) {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file || pending) return;
    if (!["image/jpeg", "image/png", "image/webp"].includes(file.type) || !file.size || file.size > 10 * 1024 * 1024) {
      toast.error("Chọn ảnh JPG, PNG hoặc WebP có dữ liệu, tối đa 10 MB."); return;
    }
    onSelect({ id: crypto.randomUUID(), file });
  }
  return <div className="grid content-start gap-3">
    <p className="text-sm font-semibold text-foreground">{label}</p>
    <ImagePreview key={selection?.id ?? currentUrl ?? "empty"} file={selection?.file} url={currentUrl} label={label} round={round} />
    <label className="grid gap-2 text-sm"><span className="sr-only">Chọn {label.toLowerCase()}</span><Input type="file" accept="image/jpeg,image/png,image/webp" disabled={pending} onChange={choose} className="min-h-11 w-full min-w-0 rounded-lg border border-input bg-card p-2 file:mr-2 file:rounded file:border-0 file:bg-accent file:px-3 file:py-2 file:text-accent-foreground" /></label>
    {selection && <div className="flex flex-wrap items-center gap-2"><p className="min-w-0 break-words text-xs text-muted-foreground">{selection.file.name}</p><Button variant="outline" type="button" disabled={pending} onClick={() => onSelect(null)} className="min-h-11 text-xs font-semibold text-red-700">Bỏ ảnh mới</Button></div>}
  </div>;
}

function ImagePreview({ file, url, label, round }) {
  const image = useRef(null);
  const [failed, setFailed] = useState(false);
  const hasImage = Boolean(file || isMediaUrl(url));
  useEffect(() => {
    if (!hasImage) return;
    const blob = file ? URL.createObjectURL(file) : null;
    const node = image.current;
    node.src = blob ?? url;
    return () => { node.removeAttribute("src"); if (blob) URL.revokeObjectURL(blob); };
  }, [file, url, hasImage]);
  return <div className={`grid place-items-center overflow-hidden border border-border bg-background ${round ? "size-28 rounded-full" : "aspect-[3/1] w-full rounded-xl"}`}>
    {hasImage && <img ref={image} alt={label} onError={() => setFailed(true)} className={`size-full object-cover ${failed ? "hidden" : ""}`} />}
    {(!hasImage || failed) && <span className="p-3 text-center text-xs text-muted-foreground">Chưa có ảnh hiển thị</span>}
  </div>;
}
