import { useState } from "react";
import { isMediaUrl, isVideo } from "./productHelpers.js";

const reviewDate = new Intl.DateTimeFormat("vi-VN", { dateStyle: "medium", timeZone: "Asia/Ho_Chi_Minh" });

export default function ReviewItem({ review }) {
  const name = review.buyerName?.trim() || "Người mua";
  const createdAt = Number.isFinite(Date.parse(review.createdAt)) ? review.createdAt : null;
  const media = Array.isArray(review.mediaUrls) ? [...new Set(review.mediaUrls.filter(isMediaUrl))] : [];
  return <article className="flex min-w-0 items-start gap-3 py-5 sm:gap-4">
    <span aria-hidden="true" className="grid size-10 shrink-0 place-items-center rounded-full bg-accent text-sm font-bold text-primary">{Array.from(name)[0].toUpperCase()}</span>
    <div className="grid min-w-0 flex-1 gap-3">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <div className="grid min-w-0 gap-1"><h3 className="text-sm font-semibold break-words text-foreground">{name}</h3>
          <span aria-label={`${review.rating} trên 5 sao`} className="w-fit text-base tracking-wider text-primary">
            <span aria-hidden="true">{"★".repeat(review.rating)}<span className="text-slate-200">{"★".repeat(5 - review.rating)}</span></span>
          </span>
        </div>
        {createdAt && <time dateTime={createdAt} className="text-xs text-muted-foreground">{reviewDate.format(new Date(createdAt))}</time>}
      </div>
      <p className="text-sm leading-7 whitespace-pre-wrap break-words text-muted-foreground">{review.comment}</p>
      {media.length > 0 && <ul aria-label="Ảnh và video kèm đánh giá" className="grid max-w-xl grid-cols-2 gap-2 sm:grid-cols-3">
        {media.map((url, index) => <li key={url} className="min-w-0"><ReviewMedia url={url} index={index} /></li>)}
      </ul>}
    </div>
  </article>;
}

function ReviewMedia({ url, index }) {
  const [failed, setFailed] = useState(false);
  if (failed) return <div className="grid aspect-square place-items-center rounded-lg border border-border bg-background p-2 text-center text-xs text-muted-foreground">Không tải được ảnh/video</div>;
  if (isVideo(url)) return <video src={url} controls preload="metadata" aria-label={`Video đánh giá ${index + 1}`} className="aspect-square w-full rounded-lg border border-border bg-background object-contain" onError={() => setFailed(true)}>Trình duyệt không hỗ trợ phát video.</video>;
  return <a href={url} target="_blank" rel="noopener noreferrer" className="block overflow-hidden rounded-lg border border-border bg-background" aria-label={`Mở ảnh đánh giá ${index + 1} trong tab mới`}>
    <img src={url} alt={`Ảnh đánh giá ${index + 1}`} loading="lazy" className="aspect-square w-full object-contain" onError={() => setFailed(true)} />
  </a>;
}
