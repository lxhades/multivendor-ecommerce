import { useEffect } from "react";
import { useLocation } from "react-router-dom";

// One controller per active route. Keep native scrolling inside every overlay/form.
export default function StorefrontScroll() {
  const { pathname, search } = useLocation();
  const scrollKey = pathname.startsWith("/search") || pathname === "/shops" ? `${pathname}${search}` : pathname;
  useEffect(() => {
    if (pathname.startsWith("/admin")) return;
    let cancelled = false, lenis = null, observer = null;
    const preference = window.matchMedia("(prefers-reduced-motion: reduce)");
    function destroy() { observer?.disconnect(); observer = null; lenis?.destroy(); lenis = null; }
    async function configure() {
      destroy();
      if (cancelled || preference.matches) return;
      try {
        const { default: Lenis } = await import("lenis");
        if (cancelled || preference.matches || lenis) return;
        lenis = new Lenis({ autoRaf: true, smoothWheel: true, syncTouch: false, anchors: true,
          prevent: (node) => Boolean(node.closest?.("dialog, textarea, [data-lenis-prevent], [data-slot=dialog-content], [data-slot=sheet-content], [data-slot=select-content], [data-slot=dropdown-menu-content]")) });
        let stopped = false;
        const syncOverlay = () => {
          const blocked = Boolean(document.querySelector('dialog[open], [data-slot="dialog-content"][data-state="open"], [data-slot="alert-dialog-content"][data-state="open"], [data-slot="sheet-content"][data-state="open"]'));
          if (blocked !== stopped) { if (blocked) lenis?.stop(); else lenis?.start(); stopped = blocked; }
        };
        observer = new MutationObserver(syncOverlay);
        observer.observe(document.body, { subtree: true, childList: true, attributes: true, attributeFilter: ["open", "data-state"] });
        syncOverlay();
      } catch { /* Native scrolling remains available if the optional chunk cannot load. */ }
    }
    preference.addEventListener("change", configure);
    void configure();
    return () => { cancelled = true; preference.removeEventListener("change", configure); destroy(); };
  }, [pathname, scrollKey]);
  return null;
}
