# Halloween Market Lane — direction UI Buyer/Seller

Palette: paper #FFFFFF, fog #F3F4F4, ink #171717, pumpkin #FF6A1A, purple #57406B, steel #747474. White/gray is the reading surface; orange identifies actions; black frames the marketplace. Halloween is expressed through a pumpkin illustration, small ghost details and a seasonal hero, never fake discounts or made-up shop/order statistics.

Typography: Be Vietnam Pro self-hosted (weights 400–800, fallbacks Segoe UI/Arial), bold close-set display headings and relaxed readable body. Layout: wide seasonal storefront, clean product grid; Seller uses a dark vertical workspace navigation with white content. Real product images, names, prices and stock remain sourced from APIs.

```text
Buyer:  dark seasonal ribbon / white search header
        white hero copy + orange pumpkin illustration
        category strip / product grid / dark footer
Seller: shared header
        dark sidebar | workspace title / filters / table / detail dialog
```

One hero entrance animation with GSAP; smooth wheel scroll via one Lenis instance except Admin/reduced motion, stopped while overlays are open. No autoplaying decoration, no scroll hijacking inside forms/dialogs. Each page keeps an independent module-scope Tailwind style const; shared components have their own style map. All business handlers/auth/API contracts retained. No build/test/lint or browser checks in this implementation per standing user request.
