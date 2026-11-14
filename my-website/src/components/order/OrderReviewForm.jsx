import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { useEffect, useId, useRef, useState } from "react";
import { uploadMedia } from "../../api/media/mediaApi.js";
import ReviewMediaFields from "./ReviewMediaFields.jsx";
import { MAX_REVIEW_FILES, MAX_REVIEW_FILE_BYTES, REVIEW_MEDIA_TYPES } from "./reviewMediaModel.js";
import { createReview } from "../../api/order/reviewApi.js";
import { authErrorMessage } from "../auth/authHelpers.js";
import { toast } from "../common/toast.js";

const labels = ["Rất không hài lòng", "Không hài lòng", "Bình thường", "Hài lòng", "Rất hài lòng"];

export default function OrderReviewForm({ orderId, product, onBusyChange, onComplete, onCancel }) {
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState("");
  const [pending, setPending] = useState(false);
  const [error, setError] = useState("");
  const [files, setFiles] = useState([]);
  const [progress, setProgress] = useState("");
  const uploaded = useRef(new Map());
  const requestRef = useRef(null);
  const lock = useRef(false);
  const commentId = useId();
  const ratingName = useId();
  useEffect(() => () => requestRef.current?.abort(), []);

  function selectFiles(event) {
    const selected = [...(event.target.files ?? [])];
    event.target.value = "";
    if (lock.current) return;
    const next = [...files];
    const errors = [];
    for (const file of selected) {
      if (!REVIEW_MEDIA_TYPES.includes(file.type)) { errors.push(`${file.name}: định dạng không hỗ trợ.`); continue; }
      if (!file.size || file.size > MAX_REVIEW_FILE_BYTES) { errors.push(`${file.name}: tệp phải có dữ liệu và tối đa 10 MB.`); continue; }
      if (next.some((entry) => entry.file.name === file.name && entry.file.size === file.size && entry.file.lastModified === file.lastModified && entry.file.type === file.type)) continue;
      if (next.length >= MAX_REVIEW_FILES) { errors.push(`Chỉ được chọn tối đa ${MAX_REVIEW_FILES} tệp.`); break; }
      next.push({ id: crypto.randomUUID(), file });
    }
    setFiles(next);
    if (errors.length) { setError(errors.join(" ")); toast.error(errors.join(" ")); }
    else setError("");
  }
  function removeFile(id) {
    if (lock.current) return;
    uploaded.current.delete(id);
    setFiles((current) => current.filter((entry) => entry.id !== id));
  }

  async function submit(event) {
    event.preventDefault();
    if (lock.current) return;
    const text = comment.trim();
    if (!Number.isInteger(rating) || rating < 1 || rating > 5 || !text || comment.length > 2000) {
      const message = "Chọn từ 1 đến 5 sao và nhập bình luận tối đa 2000 ký tự.";
      setError(message); toast.error(message); return;
    }
    lock.current = true; setPending(true); setError(""); onBusyChange(true);
    const request = new AbortController(); requestRef.current = request;
    let reviewDispatched = false;
    try {
      const mediaUrls = [];
      for (const [index, entry] of files.entries()) {
        if (request.signal.aborted) return;
        let url = uploaded.current.get(entry.id);
        if (!url) {
          setProgress(`Đang tải tệp ${index + 1}/${files.length}: ${entry.file.name}`);
          url = await uploadMedia(entry.file, { domain: "reviews", entityId: orderId, signal: request.signal });
          if (request.signal.aborted) return;
          uploaded.current.set(entry.id, url);
        }
        mediaUrls.push(url);
      }
      setProgress("Đang gửi đánh giá…");
      reviewDispatched = true;
      await createReview({ orderId, productId: product.productId, rating, comment: text, mediaUrls }, { signal: request.signal });
      if (request.signal.aborted) return;
      onComplete(product.productId, "created");
      toast.success("Gửi đánh giá thành công. Cảm ơn bạn đã chia sẻ!");
    } catch (failure) {
      if (request.signal.aborted) return;
      if (reviewDispatched && failure.response?.status === 409) {
        onComplete(product.productId, "duplicate");
        toast.error("Bạn đã đánh giá sản phẩm này trong đơn hàng này.");
      } else {
        const message = reviewDispatched && failure.response?.status === 403
          ? "Bạn chưa mua sản phẩm này hoặc đơn hàng chưa hoàn thành. Vui lòng kiểm tra lại đơn hàng."
          : authErrorMessage(failure, reviewDispatched ? "Không gửi được đánh giá. Vui lòng thử lại." : "Không tải được ảnh/video. Vui lòng kiểm tra tệp rồi thử lại.");
        setError(message); toast.error(message);
      }
    } finally {
      lock.current = false;
      if (!request.signal.aborted) { setPending(false); setProgress(""); onBusyChange(false); }
      requestRef.current = null;
    }
  }

  return <form onSubmit={submit} aria-busy={pending} className="mt-4 rounded-xl border border-primary/25 bg-accent/30 p-4">
    <h4 className="break-words font-bold">Đánh giá {product.productName || "sản phẩm"}</h4>
    <fieldset disabled={pending} className="mt-4 grid gap-4">
      <fieldset className="grid gap-2"><legend className="mb-2 text-sm font-semibold">Mức độ hài lòng</legend>
        <div className="flex flex-wrap gap-2">{labels.map((label, index) => <label key={label} className={`flex min-h-11 cursor-pointer items-center gap-1 rounded-lg border px-3 text-sm ${rating === index + 1 ? "border-primary bg-accent text-accent-foreground" : "border-input bg-card text-foreground"}`}>
          <input type="radio" name={ratingName} value={index + 1} checked={rating === index + 1} onChange={() => setRating(index + 1)} required aria-label={`${index + 1} sao — ${label}`} className="size-4 accent-primary" />
          <span>{index + 1} <span aria-hidden="true">★</span></span>
        </label>)}</div>
        <p className="text-xs text-muted-foreground" aria-live="polite">{rating ? labels[rating - 1] : "Vui lòng chọn số sao"}</p>
      </fieldset>
      <label htmlFor={commentId} className="grid gap-2 text-sm font-semibold">Bình luận
        <Textarea id={commentId} value={comment} onChange={(event) => setComment(event.target.value)} required maxLength={2000} rows={4} placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm…" className="w-full min-w-0 rounded-lg border border-input bg-card p-3 font-normal" />
      </label>
      <p className="text-right text-xs text-muted-foreground">{comment.length}/2000 ký tự</p>
      <ReviewMediaFields files={files} pending={pending} onSelect={selectFiles} onRemove={removeFile} />
      {progress && <p role="status" className="break-words text-sm text-accent-foreground">{progress}</p>}
      {error && <p role="alert" className="text-sm text-red-700">{error}</p>}
      <div className="flex flex-wrap gap-3"><Button variant="default" type="submit" className="min-h-11 rounded-lg bg-primary px-4 text-sm font-bold text-white disabled:opacity-50">{pending ? "Đang gửi đánh giá…" : "Gửi đánh giá"}</Button><Button variant="outline" type="button" onClick={onCancel} className="min-h-11 rounded-lg border border-input bg-card px-4 text-sm">Hủy</Button></div>
    </fieldset>
  </form>;
}
