import { useEffect, useEffectEvent } from "react";
import { useLocation } from "react-router-dom";

export default function RefreshSessionOnNavigation({ auth }) {
  const { key, pathname, search, hash } = useLocation();
  const refresh = useEffectEvent(() => { void auth.refreshForNavigation(); });
  useEffect(() => { refresh(); }, [key, pathname, search, hash, auth.checking]);
  return null;
}
