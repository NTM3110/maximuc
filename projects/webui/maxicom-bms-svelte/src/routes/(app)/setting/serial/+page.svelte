<script lang="ts">
    import { configuredPorts, getPortsLoadingStatus, getSerialPortsStream, BAUD_RATES, DATA_BITS, STOP_BITS, PARITIES, getAllPortDefinitions, updateSerialPort, addSerialPort, deleteSerialPort } from "$lib/services/communication";
    import { onMount } from "svelte";
    import SerialForm from "$lib/components/SerialForm.svelte";
    import ConfirmationDialog from "$lib/components/ConfirmationDialog.svelte";

    let isLoading = $state(true);
    let isFormOpen = $state(false);
    let isConfirmOpen = $state(false);
    let selectedPort: any = $state(null);
    let confirmMessage = $state("");
    let portToDelete: any = $state(null);
    
    const maxPorts = 3;

    onMount(() => {
        // 1. Listen for loading status
        const loadingUnsubscribe = getPortsLoadingStatus().subscribe((loading: boolean) => {
            isLoading = loading;
        });

        // 2. Use 'getSerialPortsStream' to receive continuous updates
        const portsUnsubscribe = getSerialPortsStream().subscribe(
            (ports: any[]) => {
                // Data is automatically updated in the store
            },
            (err: any) => {
                console.error('Error loading Serial Ports:', err);
                showMessage('Could not load Serial configuration', 'error');
            }
        );

        // Cleanup subscriptions on component unmount
        return () => {
            loadingUnsubscribe?.unsubscribe?.();
            portsUnsubscribe?.unsubscribe?.();
        };
    });

    function formatParity(parity: string): string {
        if (!parity) {
            return '';
        }
        return parity.replace('PARITY_', '');
    }

    function showMessage(message: string, type: 'success' | 'error' | 'info') {
        // TODO: Implement snackbar/toast notification
        console.log(`[${type}] ${message}`);
    }

    function openPortForm(portToEdit?: any) {
        selectedPort = portToEdit || null;
        isFormOpen = true;
    }

    function closePortForm() {
        isFormOpen = false;
        selectedPort = null;
    }

    async function handleSavePort(event: CustomEvent) {
        const formData = event.detail;
        
        try {
            if (selectedPort) {
                // Edit mode
                const { portId, ...editData } = formData;
                await updateSerialPort(selectedPort.id, editData);
                showMessage(`Updated ${selectedPort.alias}`, 'success');
            } else {
                // Add mode
                const newPort = await addSerialPort(formData);
                showMessage(`Added ${newPort.alias}`, 'success');
            }
            closePortForm();
        } catch (err: any) {
            showMessage(err.message || 'Error while saving port', 'error');
        }
    }

    function deletePort(port: any) {
        portToDelete = port;
        confirmMessage = `Are you sure you want to delete "${port.alias}"? This action will reset the port configuration on the device.`;
        isConfirmOpen = true;
    }

    function closeConfirmDialog() {
        isConfirmOpen = false;
        portToDelete = null;
    }

    async function handleConfirmDelete() {
        if (!portToDelete) return;
        
        try {
            await deleteSerialPort(portToDelete.id);
            showMessage(`Deleted ${portToDelete.alias}`, 'info');
            closeConfirmDialog();
        } catch (err: any) {
            showMessage(err.message || 'Error when deleting port', 'error');
        }
    }
</script>

<div class="serial-container">
    <div class="page-header">
        <div class="header-content">
            <h1 class="page-title">
                <i class="bi bi-usb-symbol title-icon"></i>
                Serial Configuration
            </h1>
            <p class="page-subtitle">
                Manage and configure Serial ports (RS-485)
            </p>
        </div>
        <div class="header-actions">
            <button 
                class="btn-primary" 
                onclick={() => openPortForm()}
                disabled={$configuredPorts.length >= maxPorts}
            >
                <i class="bi bi-plus-lg"></i>
                Add Port
            </button>
        </div>
    </div>

    {#if isLoading}
        <div class="loading-spinner">
            <div class="spinner"></div>
            <p>Loading Serial port configuration...</p>
        </div>
    {:else}
        <div class="table-card">
            <div class="table-container">
                <table class="mat-table">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Baud Rate</th>
                            <th>Data Bits</th>
                            <th>Parity</th>
                            <th>Stop Bits</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {#if $configuredPorts.length === 0}
                            <tr class="no-data-row">
                                <td colspan="6">
                                    No ports added yet. Click "Add Port" to get started.
                                </td>
                            </tr>
                        {:else}
                            {#each $configuredPorts as port (port.id)}
                                <tr>
                                    <td>{port.alias}</td>
                                    <td>{port.baudRate}</td>
                                    <td>{port.dataBits}</td>
                                    <td>{formatParity(port.parity)}</td>
                                    <td>{port.stopBits}</td>
                                    <td class="actions-cell">
                                        <button class="icon-button primary" onclick={() => openPortForm(port)} title="Edit">
                                            <i class="bi bi-pencil"></i>
                                        </button>
                                        <!-- <button class="icon-button warn" onclick={() => deletePort(port)} title="Delete">
                                            <i class="bi bi-trash"></i>
                                        </button> -->
                                    </td>
                                </tr>
                            {/each}
                        {/if}
                    </tbody>
                </table>
            </div>

            <div class="table-footer">
                <div class="paginator">
                    <span class="paginator-label">Items per page: 3</span>
                    <span class="paginator-range">1 - {$configuredPorts.length} of {$configuredPorts.length}</span>
                </div>
            </div>
        </div>
    {/if}
</div>

<SerialForm
    isOpen={isFormOpen}
    portToEdit={selectedPort}
    baudRates={BAUD_RATES}
    dataBits={DATA_BITS}
    stopBits={STOP_BITS}
    parityOptions={PARITIES}
    availablePorts={getAllPortDefinitions()}
    configuredPortIds={$configuredPorts.map((p: any) => p.id)}
    on:save={handleSavePort}
    on:cancel={closePortForm}
/>

<ConfirmationDialog
    isOpen={isConfirmOpen}
    title="Delete Port"
    message={confirmMessage}
    type="danger"
    confirmText="Delete"
    cancelText="Cancel"
    on:confirm={handleConfirmDelete}
    on:cancel={closeConfirmDialog}
/>

<style>
    .serial-container {
        min-height: calc(100vh - var(--header-height) - 48px);
        animation: fadeIn 0.5s ease-in;
    }

    .page-header {
        margin-bottom: 24px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        gap: 16px;
    }

    .header-content .page-title {
        margin: 0 0 8px 0;
        display: flex;
        align-items: center;
        gap: 12px;
        color: var(--primary-color);
        font-size: 1.8rem;
        font-weight: 500;
    }

    .title-icon {
        font-size: 2rem;
        color: var(--accent-color);
    }

    .page-subtitle {
        margin: 0;
        color: #666;
        font-size: 1rem;
        line-height: 1.5;
    }

    .btn-primary {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 10px 20px;
        background-color: var(--primary-color);
        color: white;
        border: none;
        border-radius: 8px;
        font-size: 0.9375rem;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    }

    .btn-primary:hover:not(:disabled) {
        background-color: #013bb5;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
    }

    .btn-primary:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }

    .loading-spinner {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 40px;
        gap: 16px;
        color: rgba(0, 0, 0, 0.6);
    }

    .spinner {
        width: 40px;
        height: 40px;
        border: 4px solid #e0e0e0;
        border-top-color: var(--primary-color);
        border-radius: 50%;
        animation: spin 0.8s linear infinite;
    }

    @keyframes spin {
        to { transform: rotate(360deg); }
    }

    .table-card {
        background: white;
        border-radius: 16px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
        overflow: hidden;
    }

    .table-container {
        overflow-x: auto;
    }

    .mat-table {
        width: 100%;
        border-collapse: collapse;
    }

    .mat-table thead {
        background-color: #fafafa;
    }

    .mat-table th {
        padding: 16px;
        text-align: left;
        font-weight: 500;
        font-size: 0.875rem;
        color: #616161;
        border-bottom: 1px solid #e0e0e0;
    }

    .mat-table td {
        padding: 16px;
        border-bottom: 1px solid #e0e0e0;
        color: #424242;
    }

    .mat-table tbody tr {
        transition: background-color 0.2s;
    }

    .mat-table tbody tr:hover {
        background-color: #f5f5f5;
    }

    .no-data-row td {
        text-align: center;
        padding: 40px;
        color: #757575;
    }

    .actions-cell {
        display: flex;
        gap: 4px;
    }

    .icon-button {
        background: none;
        border: none;
        color: #616161;
        cursor: pointer;
        padding: 8px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.2s;
    }

    .icon-button:hover {
        background-color: #f5f5f5;
    }

    .icon-button.primary {
        color: var(--primary-color);
    }

    .icon-button.primary:hover {
        background-color: #e3f2fd;
    }

    .icon-button.warn {
        color: var(--warn-color);
    }

    .icon-button.warn:hover {
        background-color: #ffebee;
    }

    .icon-button i {
        font-size: 1.125rem;
    }

    .table-footer {
        padding: 16px;
        border-top: 1px solid #e0e0e0;
        background-color: #fafafa;
    }

    .paginator {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 0.75rem;
        color: #757575;
    }

    @keyframes fadeIn {
        from {
            opacity: 0;
            transform: translateY(20px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }
</style>
