<script lang="ts">
    import { createEventDispatcher } from "svelte";
    import type { SerialPortConfig, SerialFormData } from "$lib/interfaces/communication.interface";

    export let isOpen: boolean = false;
    export let portToEdit: SerialPortConfig | null = null;
    export let baudRates: number[] = [];
    export let dataBits: number[] = [];
    export let stopBits: number[] = [];
    export let parityOptions: string[] = [];
    export let availablePorts: any[] = [];
    export let configuredPortIds: string[] = [];

    const dispatch = createEventDispatcher();

    let formData: SerialFormData & { portId?: string } = {
        baudRate: 9600,
        dataBits: 8,
        parity: 'PARITY_NONE',
        stopBits: 1,
        portId: ''
    };

    let isEditMode = false;
    let selectedPort: any = null;

    $: if (isOpen) {
        if (portToEdit) {
            isEditMode = true;
            formData = {
                baudRate: portToEdit.baudRate,
                dataBits: portToEdit.dataBits,
                parity: portToEdit.parity,
                stopBits: portToEdit.stopBits,
                portId: portToEdit.id
            };
            selectedPort = availablePorts.find(p => p.id === portToEdit.id);
        } else {
            isEditMode = false;
            resetForm();
        }
    }

    function resetForm() {
        const availablePort = availablePorts.find(p => !configuredPortIds.includes(p.id));
        formData = {
            baudRate: 9600,
            dataBits: 8,
            parity: 'PARITY_NONE',
            stopBits: 1,
            portId: availablePort?.id || ''
        };
        selectedPort = availablePort || null;
    }

    function close() {
        dispatch("cancel");
    }

    function save() {
        if (!formData.portId && !isEditMode) {
            alert('Please select a port');
            return;
        }
        dispatch("save", { ...formData });
    }

    function handlePortChange(e: Event) {
        const target = e.target as HTMLSelectElement;
        formData.portId = target.value;
        selectedPort = availablePorts.find(p => p.id === target.value);
    }
</script>

{#if isOpen}
    <div class="modal-backdrop" onclick={close} role="button" tabindex="0" onkeydown={(e) => e.key === 'Escape' && close()}>
        <div class="modal-content" onclick={(e) => e.stopPropagation()} role="document" tabindex="0" onkeydown={(e) => e.key === 'Escape' && close()}>
            <div class="modal-header">
                <h2>{isEditMode ? "Edit Serial Port" : "Add Serial Port"}</h2>
                <button class="close-button" onclick={close}>
                    <i class="bi bi-x-lg"></i>
                </button>
            </div>
            <div class="modal-body">
                {#if !isEditMode}
                    <div class="form-group">
                        <label for="portSelect">Serial Port</label>
                        <select 
                            id="portSelect" 
                            bind:value={formData.portId}
                            onchange={handlePortChange}
                        >
                            <option value="">-- Select a port --</option>
                            {#each availablePorts as port}
                                {#if !configuredPortIds.includes(port.id) || port.id === formData.portId}
                                    <option value={port.id}>
                                        {port.alias} ({port.devicePath})
                                    </option>
                                {/if}
                            {/each}
                        </select>
                        {#if selectedPort}
                            <div class="port-info">
                                <small>Device Path: {selectedPort.devicePath}</small>
                            </div>
                        {/if}
                    </div>
                {/if}

                <div class="form-row">
                    <div class="form-group">
                        <label for="baudRate">Baud Rate</label>
                        <select id="baudRate" bind:value={formData.baudRate}>
                            {#each baudRates as rate}
                                <option value={rate}>{rate}</option>
                            {/each}
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="dataBits">Data Bits</label>
                        <select id="dataBits" bind:value={formData.dataBits}>
                            {#each dataBits as bits}
                                <option value={bits}>{bits}</option>
                            {/each}
                        </select>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="parity">Parity</label>
                        <select id="parity" bind:value={formData.parity}>
                            {#each parityOptions as parity}
                                <option value={parity} label={parity.replace('PARITY_', '')}>
                                    {parity.replace('PARITY_', '')}
                                </option>
                            {/each}
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="stopBits">Stop Bits</label>
                        <select id="stopBits" bind:value={formData.stopBits}>
                            {#each stopBits as bits}
                                <option value={bits}>{bits}</option>
                            {/each}
                        </select>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn-secondary" onclick={close}>Cancel</button>
                <button class="btn-primary" onclick={save}>Save</button>
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
        max-width: 500px;
        max-height: 90vh;
        overflow-y: auto;
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
        display: flex;
        flex-direction: column;
        gap: 16px;
    }

    .form-group {
        display: flex;
        flex-direction: column;
        gap: 8px;
        flex: 1;
    }

    .form-row {
        display: flex;
        gap: 16px;
    }

    label {
        font-size: 0.875rem;
        font-weight: 500;
        color: #424242;
    }

    input, select {
        padding: 10px 12px;
        border: 1px solid #e0e0e0;
        border-radius: 6px;
        font-size: 0.9375rem;
        transition: border-color 0.2s;
    }

    input:focus, select:focus {
        outline: none;
        border-color: var(--primary-color);
    }

    .port-info {
        padding: 8px 12px;
        background-color: #f5f5f5;
        border-radius: 4px;
        color: #666;
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

    .btn-primary {
        background-color: var(--primary-color);
        color: white;
    }

    .btn-primary:hover {
        background-color: #013bb5;
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
