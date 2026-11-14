import { Button } from "@/components/ui/button";
import { useState } from "react";
import { isVideo } from "./productHelpers.js";

export default function ProductGallery({ media, activeUrl, onSelect, name }) {
  const [failed, setFailed] = useState([]);
  const hasMedia = activeUrl && !failed.includes(activeUrl);
  return <section aria-label="Ảnh và video sản phẩm" className="grid min-w-0 content-start gap-3">
    <div className="grid aspect-square place-items-center overflow-hidden rounded-3xl border border-border bg-card">
      {!hasMedia ? <p className="p-6 text-center text-sm text-muted-foreground">{activeUrl ? "Không tải được ảnh hoặc video này." : "Sản phẩm chưa có ảnh."}</p>
        : isVideo(activeUrl) ? <video key={activeUrl} src={activeUrl} controls preload="metadata" aria-label={`Video ${name}`} className="size-full object-contain"
          onError={() => setFailed((current) => [...current, activeUrl])}>Trình duyệt không hỗ trợ phát video.</video>
          : <img src={activeUrl} alt={name} className="size-full object-contain" onError={() => setFailed((current) => [...current, activeUrl])} />}
    </div>
    {media.length > 1 && <ul className="flex min-w-0 gap-2 overflow-x-auto py-1">
      {media.map((url, index) => <li key={url} className="shrink-0">
        <Button variant="outline" type="button" onClick={() => onSelect(url)} aria-label={`Xem ${isVideo(url) ? "video" : "ảnh"} ${index + 1}`} aria-pressed={url === activeUrl}
          className="grid size-16 p-0 place-items-center overflow-hidden rounded-lg border border-border bg-card aria-pressed:border-primary aria-pressed:ring-1 aria-pressed:ring-ring">
          {isVideo(url) ? <span className="text-xs font-semibold text-primary"><span aria-hidden="true">▶ </span>Video</span>
            : <Thumbnail url={url} />}
        </Button>
      </li>)}
    </ul>}
  </section>;
}

function Thumbnail({ url }) {
  const [failed, setFailed] = useState(false);
  return failed ? <span className="text-xs text-muted-foreground">Ảnh lỗi</span>
    : <img src={url} alt="" loading="lazy" decoding="async" className="size-full object-contain" onError={() => setFailed(true)} />;
}
