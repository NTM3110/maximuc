<script lang="ts">
    import { toasts, dismissToast } from "$lib/services/toast.store";
</script>

<div
    class="toast-container position-fixed top-0 end-0 p-3"
    style="z-index: 11000"
>
    {#each $toasts as toast (toast.id)}
        <div
            class="toast show align-items-center text-white bg-{toast.type ===
            'success'
                ? 'success'
                : toast.type === 'error'
                  ? 'danger'
                  : 'info'} border-0"
            role="alert"
        >
            <div class="d-flex">
                <div class="toast-body">
                    {toast.message}
                </div>
                <button
                    type="button"
                    class="btn-close btn-close-white me-2 m-auto"
                    aria-label="Close"
                    onclick={() => dismissToast(toast.id)}
                ></button>
            </div>
        </div>
    {/each}
</div>
