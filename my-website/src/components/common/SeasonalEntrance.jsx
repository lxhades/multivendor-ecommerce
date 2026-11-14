import { useRef } from "react";
import gsap from "gsap";
import { useGSAP } from "@gsap/react";

gsap.registerPlugin(useGSAP);
export default function SeasonalEntrance({ children }) {
  const scope = useRef(null);
  useGSAP(() => {
    const media = gsap.matchMedia();
    media.add("(prefers-reduced-motion: no-preference)", () => {
      gsap.from("[data-season-copy] > *", { y: 18, opacity: 0, duration: 0.55, stagger: 0.07, ease: "power2.out", clearProps: "all" });
      gsap.from("[data-season-art]", { x: 24, duration: 0.8, ease: "power3.out", clearProps: "all" });
    });
    return () => media.revert();
  }, { scope });
  return <div ref={scope}>{children}</div>;
}
