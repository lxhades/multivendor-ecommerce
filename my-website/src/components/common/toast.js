// One shared notification channel. The latest action replaces the previous toast.
export const toastConfig = Object.freeze({ successDurationMs: 2000, errorDurationMs: 2000 });
let current = null;
let nextId = 0;
const listeners = new Set();

export function getToast() { return current; }
export function subscribeToast(listener) { listeners.add(listener); return () => listeners.delete(listener); }
function emit() { for (const listener of listeners) listener(); }
function show(type, message) {
  if (typeof message !== "string" || !message.trim()) return null;
  current = { id: ++nextId, type, message: message.trim() };
  emit();
  return current.id;
}
function dismiss(id) {
  if (!current || (id != null && current.id !== id)) return;
  current = null;
  emit();
}
export const toast = Object.freeze({
  success: (message) => show("success", message),
  error: (message) => show("error", message),
  dismiss,
});

// Native dialogs occupy the browser top layer. Portal the same Toaster into
// the active dialog rather than trying to out-rank it with a CSS z-index.
let hostRegistration = null;
export function getToastHost() { return hostRegistration?.node ?? null; }
export function registerToastHost(node) {
  const registration = { node };
  hostRegistration = registration;
  emit();
  return () => {
    if (hostRegistration !== registration) return;
    hostRegistration = null;
    emit();
  };
}
