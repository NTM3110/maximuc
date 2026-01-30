<script lang="ts">
    import { createEventDispatcher } from "svelte";

    export let isOpen: boolean = false;
    export let title: string = "Confirm";
    export let message: string = "";
    export let type: 'danger' | 'warning' | 'info' = 'info';
    export let confirmText: string = "Confirm";
    export let cancelText: string = "Cancel";

    const dispatch = createEventDispatcher();

    function close() {
        dispatch("cancel");
    }

    function confirm() {
        dispatch("confirm");
    }
</script>

{#if isOpen}
    <div class="modal-backdrop" onclick={close} role="button" tabindex="0" onkeydown={(e) => e.key === 'Escape' && close()}>
        <div class="modal-content" onclick={(e) => e.stopPropagation()} role="document" tabindex="0" onkeydown={(e) => e.key === 'Escape' && close()}>
            <div class="modal-header" class:danger={type === 'danger'} class:warning={type === 'warning'}>
                <h2>{title}</h2>
                <button class="close-button" onclick={close}>
                    <i class="bi bi-x-lg"></i>
                </button>
            </div>
            <div class="modal-body">
                <p>{message}</p>
            </div>
            <div class="modal-footer">
                <button class="btn-secondary" onclick={close}>{cancelText}</button>
                <button class="btn-confirm" class:danger={type === 'danger'} class:warning={type === 'warning'} onclick={confirm}>
                    {confirmText}
                </button>
            </div>
        </div>
    </div>
{/if}

<style>
    .modal-backdrop {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 1000;
        animation: fadeIn 0.2s ease-out;
    }

    .modal-content {
        background-color: white;
        border-radius: 12px;
        width: 90%;
        max-width: 400px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
        animation: slideUp 0.3s ease-out;
    }

    .modal-header {
        padding: 20px 24px;
        border-bottom: 1px solid #eee;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .modal-header h2 {
        margin: 0;
        font-size: 1.25rem;
        color: var(--primary-color);
    }

    .modal-header.danger h2 {
        color: var(--warn-color);
    }

    .modal-header.warning h2 {
        color: #ff9800;
    }

    .close-button {
        background: none;
        border: none;
        font-size: 1.25rem;
        cursor: pointer;
        color: #666;
        padding: 4px;
        border-radius: 4px;
        transition: background-color 0.2s;
    }

    .close-button:hover {
        background-color: #f5f5f5;
    }

    .modal-body {
        padding: 24px;
    }

    .modal-body p {
        margin: 0;
        color: #424242;
        line-height: 1.5;
    }

    .modal-footer {
        padding: 16px 24px;
        border-top: 1px solid #eee;
        display: flex;
        justify-content: flex-end;
        gap: 12px;
    }

    button {
        padding: 10px 20px;
        border-radius: 6px;
        font-size: 0.9375rem;
        font-weight: 500;
        cursor: pointer;
        border: none;
        transition: all 0.2s;
    }

    .btn-secondary {
        background-color: #f5f5f5;
        color: #616161;
    }

    .btn-secondary:hover {
        background-color: #e0e0e0;
    }

    .btn-confirm {
        background-color: var(--primary-color);
        color: white;
    }

    .btn-confirm:hover {
        background-color: #013bb5;
    }

    .btn-confirm.danger {
        background-color: var(--warn-color);
    }

    .btn-confirm.danger:hover {
        background-color: #d32f2f;
    }

    .btn-confirm.warning {
        background-color: #ff9800;
    }

    .btn-confirm.warning:hover {
        background-color: #f57c00;
    }

    @keyframes fadeIn {
        from { opacity: 0; }
        to { opacity: 1; }
    }

    @keyframes slideUp {
        from { transform: translateY(20px); opacity: 0; }
        to { transform: translateY(0); opacity: 1; }
    }
</style>
