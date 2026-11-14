import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useEffect, useRef, useState } from "react";

import { MAX_REVIEW_FILES, REVIEW_MEDIA_TYPES } from "./reviewMediaModel.js";

export default function ReviewMediaFields({ files, pending, onSelect, onRemove }) {
  return <section aria-label="Ảnh và video đánh giá" className="grid gap-3">
    <label className="grid gap-2 text-sm font-semibold">Ảnh/video (không bắt buộc)
      <Input type="file" multiple accept={REVIEW_MEDIA_TYPES.join(",")} disabled={pending || files.length >= MAX_REVIEW_FILES} onChange={onSelect} className="min-h-11 w-full min-w-0 rounded-lg border border-input bg-card p-2 text-sm font-normal file:mr-3 file:rounded file:border-0 file:bg-accent file:px-3 file:py-2 file:text-accent-foreground disabled:opacity-50" />
    </label>
    <p className="text-xs leading-6 text-muted-foreground">Tối đa {MAX_REVIEW_FILES} tệp, mỗi tệp tối đa 10 MB. Ảnh JPG, PNG, WebP hoặc video MP4. Chỉ tải lên khi bạn gửi đánh giá.</p>
    {files.length > 0 && <ul className="grid grid-cols-2 gap-3 sm:grid-cols-3">{files.map((entry) => <li key={entry.id} className="min-w-0 overflow-hidden rounded-lg border border-border bg-card">
      <MediaPreview file={entry.file} />
      <div className="grid gap-1 p-2"><p className="truncate text-xs text-foreground" title={entry.file.name}>{entry.file.name}</p><p className="text-xs text-muted-foreground">{(entry.file.size / (1024 * 1024)).toFixed(2)} MB</p>
        <Button variant="outline" type="button" disabled={pending} onClick={() => onRemove(entry.id)} aria-label={`Bỏ tệp ${entry.file.name}`} className="min-h-11 text-sm font-semibold text-red-700 disabled:opacity-50">Bỏ tệp</Button>
      </div>
    </li>)}</ul>}
  </section>;
}

function MediaPreview({ file }) {
  const media = useRef(null);
  const [failed, setFailed] = useState(false);
  useEffect(() => {
    const url = URL.createObjectURL(file);
    const node = media.current;
    node.src = url;
    return () => {
      node.removeAttribute("src");
      if (file.type.startsWith("video/")) node.load();
      URL.revokeObjectURL(url);
    };
  }, [file]);
  const className = `aspect-square w-full bg-muted object-contain ${failed ? "hidden" : ""}`;
  return <>
    {file.type.startsWith("video/") ? <video ref={media} controls preload="metadata" playsInline className={className} onError={() => setFailed(true)} aria-label={`Xem trước ${file.name}`} />
      : <img ref={media} alt={`Xem trước ${file.name}`} className={className} onError={() => setFailed(true)} />}
    {failed && <p className="grid aspect-square place-items-center bg-muted p-3 text-center text-xs text-muted-foreground">Không xem trước được tệp này. Bạn có thể bỏ tệp và chọn lại.</p>}
  </>;
}
