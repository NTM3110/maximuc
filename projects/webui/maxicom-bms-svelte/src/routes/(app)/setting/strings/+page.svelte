<script lang="ts">
    import {
        stringsState,
        addString,
        updateString,
        deleteString as deleteStringService,
        loadStringsFromApi,
    } from "$lib/services/battery-string";
    import { configuredPorts } from "$lib/services/communication";
    import { onMount } from "svelte";
    import { goto } from "$app/navigation";
    import StringForm from "$lib/components/StringForm.svelte";
    import type {
        BatteryString,
        StringFormData,
    } from "$lib/interfaces/string.interface";
    import { showToast } from "$lib/services/toast.store";

    let isLoading = $state(true);
    let siteName = $state("Site");
    let isFormOpen = $state(false);
    let selectedString: BatteryString | null = $state(null);

    onMount(async () => {
        isLoading = true;
        await loadStringsFromApi();
        isLoading = false;
    });

    function getPortAlias(portId: string): string {
        const port = $configuredPorts.find((p) => p.id === portId);
        return port?.alias || portId || "N/A";
    }

    function viewStringDetail(str: BatteryString) {
        goto(`/setting/string/str${str.stringIndex}`);
    }

    function openStringForm(str?: BatteryString) {
        selectedString = str || null;
        isFormOpen = true;
    }

    function closeStringForm() {
        isFormOpen = false;
        selectedString = null;
    }

    async function handleSave(event: CustomEvent<StringFormData>) {
        const formData = event.detail;
        const portConfig = $configuredPorts.find(
            (p) => p.id === formData.serialPortId,
        );

        if (!portConfig) {
            showToast("Invalid Serial Port selected", "error");
            return;
        }

        try {
            isLoading = true;
            if (selectedString) {
                await updateString(selectedString.id, formData, portConfig);
                showToast("String updated successfully", "success");
            } else {
                await addString(formData, portConfig);
                showToast("String added successfully", "success");
            }
            closeStringForm();
        } catch (error) {
            console.error("Failed to save string", error);
            showToast(
                error instanceof Error
                    ? error.message
                    : "Failed to save string",
                "error",
            );
        } finally {
            isLoading = false;
        }
    }

    async function deleteString(str: BatteryString) {
        if (!confirm(`Are you sure you want to delete ${str.stringName}?`))
            return;

        try {
            isLoading = true;
            await deleteStringService(str.id);
            showToast("String deleted successfully", "success");
        } catch (error) {
            console.error("Failed to delete string", error);
            showToast("Failed to delete string", "error");
        } finally {
            isLoading = false;
        }
    }
</script>

<div class="string-list-container">
    <div class="page-header">
        <div class="header-content">
            <h1 class="page-title">
                <i class="bi bi-battery-charging-full title-icon"></i>
                String Configuration
            </h1>
            <p class="page-subtitle">
                Manage Strings at site: {siteName}
            </p>
        </div>
        <div class="header-actions">
            <button class="btn-primary" onclick={() => openStringForm()}>
                <i class="bi bi-plus-lg"></i>
                Add String
            </button>
        </div>
    </div>

    {#if isLoading}
        <div class="loading-spinner">
            <div class="spinner"></div>
            <p>Loading data...</p>
        </div>
    {:else}
        <div class="table-card">
            <div class="table-container">
                <table class="mat-table">
                    <thead>
                        <tr>
                            <th>Index</th>
                            <th>Name</th>
                            <th>Cell Qty</th>
                            <th>Serial Port</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {#if $stringsState.length === 0}
                            <tr class="no-data-row">
                                <td colspan="5">
                                    No Strings have been configured yet.
                                </td>
                            </tr>
                        {:else}
                            {#each $stringsState as str, index (str.id)}
                                <tr>
                                    <td>{str.stringIndex || index + 1}</td>
                                    <td>{str.stringName}</td>
                                    <td>{str.cellQty}</td>
                                    <td>{getPortAlias(str.serialPortId)}</td>
                                    <td class="actions-cell">
                                        <button
                                            class="icon-button primary"
                                            onclick={() =>
                                                viewStringDetail(str)}
                                            title="View"
                                        >
                                            <i class="bi bi-eye"></i>
                                        </button>
                                        <button
                                            class="icon-button"
                                            onclick={() => openStringForm(str)}
                                            title="Edit"
                                        >
                                            <i class="bi bi-pencil"></i>
                                        </button>
                                        <button
                                            class="icon-button warn"
                                            onclick={() => deleteString(str)}
                                            title="Delete"
                                        >
                                            <i class="bi bi-trash"></i>
                                        </button>
                                    </td>
                                </tr>
                            {/each}
                        {/if}
                    </tbody>
                </table>
            </div>

            <div class="table-footer">
                <div class="paginator">
                    <span class="paginator-label">Items per page: 5</span>
                    <span class="paginator-range"
                        >1 - {$stringsState.length} of {$stringsState.length}</span
                    >
                </div>
            </div>
        </div>
    {/if}

    <StringForm
        isOpen={isFormOpen}
        stringConfig={selectedString}
        on:cancel={closeStringForm}
        on:save={handleSave}
    />
</div>

<style>
    .string-list-container {
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

    .btn-primary:hover {
        background-color: #013bb5;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
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
        to {
            transform: rotate(360deg);
        }
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
