import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useEffect, useRef, useState } from "react";
import { isVideo } from "../product/productHelpers.js";
import { toast } from "../common/toast.js";

const types = ["image/jpeg", "image/png", "image/webp", "video/mp4"];
const maxFiles = 10;

export default function SellerProductMediaField({ entries, pending, onChange }) {
  function choose(event) {
    const files = [...(event.target.files ?? [])];
    event.target.value = "";
    if (pending) return;
    const next = [...entries];
    const errors = [];
    for (const file of files) {
      if (!types.includes(file.type) || !file.size || file.size > 10 * 1024 * 1024) {
        errors.push(`${file.name}: chỉ nhận JPG, PNG, WebP, MP4 có dữ liệu, tối đa 10 MB.`); continue;
      }
      if (next.some((entry) => entry.file && entry.file.name === file.name && entry.file.type === file.type && entry.file.size === file.size && entry.file.lastModified === file.lastModified)) continue;
      if (next.length >= maxFiles) { errors.push(`Tối đa ${maxFiles} ảnh/video trong danh sách.`); break; }
      next.push({ id: crypto.randomUUID(), file });
    }
    if (next.length !== entries.length) onChange(next);
    if (errors.length) toast.error(errors.join(" "));
  }
  function move(index, offset) {
    const next = [...entries];
    const other = index + offset;
    if (pending || other < 0 || other >= next.length) return;
    [next[index], next[other]] = [next[other], next[index]];
    onChange(next);
  }
  return <section aria-label="Ảnh và video sản phẩm" className="grid min-w-0 gap-3 border-t border-border pt-4">
    <h4 className="text-sm font-bold">Ảnh và video sản phẩm</h4>
    <label className="grid gap-2 text-sm font-semibold">Thêm ảnh/video
      <Input type="file" multiple accept={types.join(",")} disabled={pending || entries.length >= maxFiles} onChange={choose} className="min-h-11 w-full min-w-0 rounded-lg border border-input bg-card p-2 text-sm font-normal file:mr-2 file:rounded file:border-0 file:bg-accent file:px-3 file:py-2 file:text-accent-foreground disabled:opacity-50" />
    </label>
    <p className="text-xs leading-6 text-muted-foreground">Tối đa {maxFiles} ảnh/video. JPG, PNG, WebP hoặc MP4, mỗi tệp tối đa 10 MB. Tệp mới chỉ được tải lên sau khi xác nhận lưu. Có thể đổi thứ tự hoặc bỏ media khỏi sản phẩm.</p>
    {!entries.length ? <p className="rounded-lg bg-card p-4 text-sm text-muted-foreground">Chưa có ảnh/video.</p> : <ul className="grid grid-cols-2 gap-3 sm:grid-cols-3">
      {entries.map((entry, index) => <li key={entry.id} className="min-w-0 overflow-hidden rounded-lg border border-border bg-card">
        <MediaPreview entry={entry} />
        <div className="grid gap-2 p-2"><p className="truncate text-xs text-muted-foreground" title={entry.file?.name}>{entry.file ? entry.file.name : `Media hiện tại ${index + 1}`}</p><p className="text-xs text-muted-foreground">{entry.file ? "Tệp mới, chưa lưu vào sản phẩm" : "Đã lưu"}</p>
          <div className="flex flex-wrap gap-1"><Button variant="outline" type="button" disabled={pending || index === 0} onClick={() => move(index, -1)} aria-label={`Di chuyển media ${index + 1} về trước`} className="min-h-11 rounded border px-2 text-xs disabled:opacity-40">←</Button><Button variant="outline" type="button" disabled={pending || index === entries.length - 1} onClick={() => move(index, 1)} aria-label={`Di chuyển media ${index + 1} về sau`} className="min-h-11 rounded border px-2 text-xs disabled:opacity-40">→</Button><Button variant="outline" type="button" disabled={pending} onClick={() => onChange(entries.filter((item) => item.id !== entry.id))} className="min-h-11 px-2 text-xs font-semibold text-red-700 disabled:opacity-50">Bỏ media</Button></div>
        </div>
      </li>)}
    </ul>}
  </section>;
}

function MediaPreview({ entry }) {
  const nodeRef = useRef(null);
  const [failed, setFailed] = useState(false);
  const video = entry.file ? entry.file.type === "video/mp4" : isVideo(entry.url);
  useEffect(() => {
    const blob = entry.file ? URL.createObjectURL(entry.file) : null;
    const node = nodeRef.current;
    node.src = blob ?? entry.url;
    return () => {
      node.removeAttribute("src");
      if (video) node.load();
      if (blob) URL.revokeObjectURL(blob);
    };
  }, [entry, video]);
  const className = `aspect-square w-full bg-muted object-contain ${failed ? "hidden" : ""}`;
  return <>
    {video ? <video ref={nodeRef} controls preload="metadata" playsInline aria-label="Xem trước video sản phẩm" onError={() => setFailed(true)} className={className} /> : <img ref={nodeRef} alt="Xem trước ảnh sản phẩm" onError={() => setFailed(true)} className={className} />}
    {failed && <p className="grid aspect-square place-items-center bg-muted p-3 text-center text-xs text-muted-foreground">Không tải được media. Bạn có thể bỏ và chọn lại.</p>}
  </>;
}
