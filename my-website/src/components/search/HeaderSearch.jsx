import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useEffect, useId, useState } from "react";
import { useNavigate } from "react-router-dom";
import { searchProducts } from "../../api/product/productApi.js";
import { productPath } from "../common/catalogPaths.js";
import { money } from "../product/productHelpers.js";

export default function HeaderSearch({ initialKeyword, onSearch }) {
  const [value, setValue] = useState(initialKeyword);
  const [focused, setFocused] = useState(false);
  const [result, setResult] = useState(null);
  const [active, setActive] = useState(-1);
  const id = useId();
  const navigate = useNavigate();
  const query = value.trim();
  const eligible = query.length >= 2 && query.length <= 120;
  const current = result?.query === query ? result : null;
  const items = current?.items ?? [];
  const open = focused && eligible;
  useEffect(() => {
    if (!focused || !eligible) return;
    const request = new AbortController();
    const timer = setTimeout(() => {
      searchProducts({ q: query, page: 1, size: 5, sort: "RELEVANCE" }, request.signal).then((data) => {
        if (!request.signal.aborted) setResult({ query, items: data.items.filter((item) => item?.id && typeof item.name === "string") });
      }).catch(() => { if (!request.signal.aborted) setResult({ query, items: [], error: true }); });
    }, 300);
    return () => { clearTimeout(timer); request.abort(); };
  }, [query, focused, eligible]);
  function pick(product) { setFocused(false); navigate(productPath(product)); }
  function submit(event) { setFocused(false); onSearch(event); }
  function keyDown(event) {
    if (event.key === "Escape") { setFocused(false); setActive(-1); }
    if (open && items.length && ["ArrowDown", "ArrowUp"].includes(event.key)) { event.preventDefault(); setActive((index) => event.key === "ArrowDown" ? (index + 1) % items.length : (index - 1 + items.length) % items.length); }
    if (event.key === "Enter" && open && active >= 0 && items[active]) { event.preventDefault(); pick(items[active]); }
  }
  return <form role="search" onSubmit={submit} onBlur={(event) => { if (!event.currentTarget.contains(event.relatedTarget)) setFocused(false); }} className="relative min-w-0">
    <div className="flex min-w-0 rounded-xl border border-input bg-card/95 focus-within:border-primary focus-within:ring-2 focus-within:ring-primary/10">
      <Input name="q" type="search" role="combobox" aria-autocomplete="list" aria-expanded={open} aria-controls={id} aria-activedescendant={open && active >= 0 && items[active] ? `${id}-${active}` : undefined} aria-label="Tìm kiếm sản phẩm" value={value} onChange={(event) => { setValue(event.target.value); setActive(-1); setFocused(true); }} onFocus={() => setFocused(true)} onKeyDown={keyDown} autoComplete="off" maxLength={120} placeholder="Bạn đang tìm sản phẩm gì?" className="min-h-12 min-w-0 flex-1 bg-transparent px-4 text-sm outline-none" />
      <Button variant="default" type="submit" className="m-1 min-h-10 shrink-0 rounded-lg bg-primary px-4 text-sm font-semibold text-white">Tìm kiếm</Button>
    </div>
    {open && <div className="absolute inset-x-0 top-full z-60 mt-2 overflow-hidden rounded-xl border border-border bg-card shadow-xl"><p className="px-4 pt-3 text-xs font-semibold text-muted-foreground">Gợi ý toàn bộ sản phẩm</p>
      {!current && <p role="status" className="p-4 text-sm text-muted-foreground">Đang tìm gợi ý…</p>}
      {current?.error && <p className="p-4 text-xs text-muted-foreground">Chưa tải được gợi ý. Bạn vẫn có thể bấm Tìm kiếm.</p>}
      {current && !current.error && !items.length && <p className="p-4 text-sm text-muted-foreground">Chưa có gợi ý phù hợp.</p>}
      <ul id={id} role="listbox" aria-label="Gợi ý sản phẩm">{items.map((product, index) => <li key={product.id} role="presentation"><Button variant="outline" id={`${id}-${index}`} type="button" role="option" aria-selected={active === index} onClick={() => pick(product)} className={`grid min-h-12 w-full gap-1 px-4 py-3 text-left text-sm hover:bg-accent ${active === index ? "bg-accent" : ""}`}><span className="break-words font-semibold">{product.name}</span><span className="text-xs text-muted-foreground">{product.shopName}{typeof product.minPrice === "number" ? ` · ${money.format(product.minPrice)}` : ""}</span></Button></li>)}</ul>
      <Button variant="default" type="submit" className="min-h-11 w-full border-t border-border/60 px-4 text-left text-sm font-semibold text-primary">Xem tất cả kết quả</Button>
    </div>}
  </form>;
}
