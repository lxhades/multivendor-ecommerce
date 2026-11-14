import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { productPath } from "../common/catalogPaths.js";
import { toast } from "../common/toast.js";
import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { money } from "../product/productHelpers.js";
import { cartError, loadCartItem } from "./cartRequests.js";
import { MAX_QUANTITY, validQuantity } from "./cartModel.js";
import ConfirmDialog from "../common/ConfirmDialog.jsx";

export default function CartItem({ item, status, cart, onChecked }) {
  // --- STATE & REFS ---
  const [draft, setDraft] = useState(null); 
  const quantity = draft?.original === item.quantity ? draft.value : String(item.quantity);
  
  const [pending, setPending] = useState(false); 
  const [failedImage, setFailedImage] = useState(null); 
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false); // Quản lý Dialog Xóa

  const lock = useRef(false); 
  const controller = useRef(null); 

  // Hủy request đang chạy khi component bị unmount
  useEffect(() => () => controller.current?.abort(), []);

  // Ưu tiên dùng dữ liệu mới nhất từ API kiểm tra (status.snapshot)
  const display = status?.snapshot ?? item;
  const available = status?.availableQuantity; 

  // --- HÀM XỬ LÝ CẬP NHẬT SỐ LƯỢNG ---
  async function update(event) {
    event.preventDefault();
    if (lock.current) return; 

    const amount = Number(quantity);
    if (!validQuantity(amount)) {
      toast.error("Số lượng phải là số nguyên dương hợp lệ.");
      return;
    }

    controller.current = new AbortController();
    const { signal } = controller.current;

    lock.current = true;
    setPending(true);

    try {
      const checked = await loadCartItem(item.productId, item.variantId, { signal });
      if (signal.aborted) return;

      if (cart.update(checked.snapshot, amount, checked.availableQuantity)) {
        onChecked(item.variantId, checked);
      }
    } catch (failure) {
      if (!signal.aborted) toast.error(cartError(failure));
    } finally {
      lock.current = false;
      if (!signal.aborted) setPending(false);
      controller.current = null;
    }
  }

  // --- HÀM XỬ LÝ XÓA SẢN PHẨM KHỎI GIỎ ---
  const handleConfirmDelete = () => {
    cart.remove(item.variantId);
    setIsDeleteDialogOpen(false);
  };

  // --- GIAO DIỆN ---
  return (
    <>
      <article className="relative flex flex-col gap-4 rounded-2xl border border-border bg-card p-4 sm:p-5 lg:flex-row lg:items-center lg:gap-6 lg:p-4">
        
        {/* NÚT XÓA (Góc trên bên phải) */}
        <Button
          type="button"
          disabled={pending}
          onClick={() => setIsDeleteDialogOpen(true)}
          className="absolute right-3 top-3 z-10 min-h-8 rounded-lg px-2 text-xs font-semibold text-red-700 hover:bg-red-50 disabled:opacity-50"
        >
          Xóa
        </Button>

        {/* KHỐI 1: HÌNH ẢNH */}
        <Link 
          to={productPath(item)} 
          className="grid size-20 shrink-0 place-items-center overflow-hidden rounded-xl bg-background sm:size-24 lg:size-20" 
          aria-label={`Xem ${display.productName}`}
        >
          {display.imageUrl && failedImage !== display.imageUrl ? (
            <img 
              src={display.imageUrl} 
              alt="" 
              loading="lazy" 
              className="size-full object-contain" 
              onError={() => setFailedImage(display.imageUrl)} 
            />
          ) : (
            <span className="p-2 text-center text-xs text-muted-foreground">Chưa có ảnh</span>
          )}
        </Link>

        {/* KHỐI 2: THÔNG TIN & TỒN KHO */}
        <div className="flex min-w-0 flex-1 flex-col justify-center gap-1">
          <Link 
            to={productPath(item)} 
            className="w-fit rounded text-sm font-bold break-words text-foreground hover:text-primary"
          >
            {display.productName}
          </Link>
          <p className="text-sm break-words text-muted-foreground">{display.variantName}</p>

          <div className="flex flex-wrap items-center gap-2">
            <p className="text-sm font-semibold text-primary">{money.format(display.unitPrice)}</p>
            {display.unitPrice !== item.unitPrice && (
              <span className="text-xs text-amber-800">(Giá đã thay đổi)</span>
            )}
          </div>

          <div className="mt-0.5">
            {!status ? (
              <p role="status" className="text-xs text-muted-foreground">Đang kiểm tra tồn kho…</p>
            ) : status.error ? (
              <p role="alert" className="text-xs text-red-700">{status.error}</p>
            ) : (
              <p className={`text-xs ${available < item.quantity ? "text-red-700" : "text-muted-foreground"}`}>
                {available === 0 ? "Tạm hết hàng." : `Còn ${available.toLocaleString("vi-VN")} sản phẩm.`}
                {available > 0 && available < item.quantity ? " (Vượt tồn kho)" : ""}
              </p>
            )}
          </div>
        </div>

        {/* KHỐI 3: FORM (SỐ LƯỢNG VÀ THÀNH TIỀN) */}
        <form 
          onSubmit={update} 
          aria-busy={pending} 
          className="flex flex-wrap items-center justify-between gap-4 border-t border-border/60 pt-3 lg:w-auto lg:border-l lg:border-t-0 lg:pt-0 lg:pl-6"
        >
          <div className="flex items-end gap-2">
            <label className="grid gap-1 text-xs text-muted-foreground">
              Số lượng
              <Input
                type="number"
                min="1"
                max={available > 0 ? Math.min(available, MAX_QUANTITY) : MAX_QUANTITY}
                step="1"
                required
                value={quantity}
                onChange={(event) => setDraft({ original: item.quantity, value: event.target.value })}
                disabled={pending || !status?.snapshot || available === 0}
                className="min-h-10 w-16 rounded-lg border border-input px-2 text-sm disabled:bg-background disabled:opacity-60"
              />
            </label>
            <Button
              type="submit"
              disabled={pending || !status?.snapshot || available === 0}
              className="min-h-10 rounded-lg border border-input px-3 text-xs font-semibold hover:border-primary/60 disabled:opacity-50"
            >
              {pending ? "Đang lưu…" : "Cập nhật"}
            </Button>
          </div>

          <div className="flex items-center gap-4 ml-auto">
            <div className="text-right">
              <span className="block text-[10px] text-muted-foreground">Thành tiền</span>
              <strong className="text-sm text-foreground">
                {money.format(display.unitPrice * item.quantity)}
              </strong>
            </div>
          </div>
        </form>

      </article>

      {/* DIALOG XÁC NHẬN XÓA */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        title="Xóa sản phẩm?"
        message={`Bạn có chắc chắn muốn xóa "${display.productName} (${display.variantName})" khỏi giỏ hàng không? Hành động này không thể hoàn tác.`}
        confirmText="Xóa sản phẩm"
        cancelText="Hủy"
        onConfirm={handleConfirmDelete}
        onCancel={() => setIsDeleteDialogOpen(false)}
        isDanger={true}
      />
    </>
  );
}