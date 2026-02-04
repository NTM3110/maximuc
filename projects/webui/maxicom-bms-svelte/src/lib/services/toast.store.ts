import { writable } from 'svelte/store';

export type ToastType = 'success' | 'error' | 'info';

export interface Toast {
    id: string;
    message: string;
    type: ToastType;
}

export const toasts = writable<Toast[]>([]);

export function showToast(message: string, type: ToastType = 'info') {
    const id = Math.random().toString(36).substring(2);
    const toast: Toast = { id, message, type };
    toasts.update((all) => [...all, toast]);

    setTimeout(() => {
        dismissToast(id);
    }, 3000);
}

export function dismissToast(id: string) {
    toasts.update((all) => all.filter((t) => t.id !== id));
}
