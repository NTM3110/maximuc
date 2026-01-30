<script lang="ts">
    let isLoading = $state(false);

    function handleImportConfig() {
        console.log("Import config clicked");
        // TODO: Implement import functionality
    }

    function handleExportConfig() {
        console.log("Export config clicked");
        // TODO: Implement export functionality
    }

    function handleResetConfig() {
        console.log("Reset config clicked");
        // TODO: Implement reset functionality
    }

    function handleReboot() {
        console.log("Reboot clicked");
        // TODO: Implement reboot functionality
    }
</script>

<div class="maintenance-page">
    <div class="title-row">
        <i class="bi bi-tools title-icon"></i>
        <h1>Maintenance</h1>
    </div>
    <p class="subtitle">System maintenance actions. These actions are UI-only placeholders for now.</p>

    <div class="cards">
        <div class="action-card">
            <div class="card-header">
                <i class="bi bi-arrow-repeat"></i>
                <div class="title">Import / Export Configuration</div>
            </div>
            <div class="card-content">
                <p>Import an existing configuration file or export the current configuration for backup.</p>
            </div>
            <div class="card-actions">
                <button class="btn-primary" disabled onclick={handleImportConfig}>
                    <i class="bi bi-upload"></i>
                    Import Config
                </button>
                <button class="btn-secondary" disabled onclick={handleExportConfig}>
                    <i class="bi bi-download"></i>
                    Export Config
                </button>
            </div>
        </div>

        <div class="action-card">
            <div class="card-header">
                <i class="bi bi-arrow-counterclockwise"></i>
                <div class="title">Reset Configuration</div>
            </div>
            <div class="card-content">
                <p>Reset all settings to factory defaults. This will not perform any action yet.</p>
            </div>
            <div class="card-actions">
                <button class="btn-warn" disabled onclick={handleResetConfig}>
                    <i class="bi bi-arrow-clockwise"></i>
                    Reset to Default
                </button>
            </div>
        </div>

        <div class="action-card">
            <div class="card-header">
                <i class="bi bi-arrow-repeat"></i>
                <div class="title">Reboot Device</div>
            </div>
            <div class="card-content">
                <p>Reboot the device. This is only a placeholder button for now.</p>
            </div>
            <div class="card-actions">
                <button class="btn-accent" disabled onclick={handleReboot}>
                    <i class="bi bi-power"></i>
                    Reboot
                </button>
            </div>
        </div>
    </div>
</div>

<style>
    .maintenance-page {
        padding: 24px;
    }

    .title-row {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 8px;
    }

    .title-row h1 {
        margin: 0;
        color: var(--primary-color);
        font-size: 1.8rem;
        font-weight: 500;
    }

    .title-icon {
        font-size: 2rem;
        color: var(--accent-color);
    }

    .subtitle {
        color: rgba(0, 0, 0, 0.6);
        margin: 8px 0 24px 0;
        font-size: 1rem;
    }

    .cards {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
        gap: 16px;
        align-items: stretch;
    }

    .action-card {
        background: white;
        border-radius: 12px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        display: flex;
        flex-direction: column;
        overflow: hidden;
        transition: box-shadow 0.2s;
    }

    .action-card:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .card-header {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 16px;
        border-bottom: 1px solid #eee;
        background-color: #fafafa;
    }

    .card-header i {
        font-size: 1.5rem;
        color: var(--primary-color);
    }

    .card-header .title {
        font-size: 1.1rem;
        font-weight: 600;
        color: #333;
    }

    .card-content {
        padding: 16px;
        flex: 1;
    }

    .card-content p {
        margin: 0;
        color: #666;
        font-size: 0.9rem;
        line-height: 1.5;
    }

    .card-actions {
        display: flex;
        gap: 8px;
        padding: 12px 16px;
        border-top: 1px solid #eee;
        background-color: #fafafa;
    }

    button {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 10px 16px;
        border: none;
        border-radius: 6px;
        font-size: 0.875rem;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s;
    }

    .btn-primary {
        background-color: var(--primary-color);
        color: white;
    }

    .btn-primary:hover:not(:disabled) {
        background-color: #013bb5;
    }

    .btn-primary:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }

    .btn-secondary {
        background-color: white;
        color: var(--primary-color);
        border: 1px solid var(--primary-color);
    }

    .btn-secondary:hover:not(:disabled) {
        background-color: #f5f5f5;
    }

    .btn-secondary:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }

    .btn-warn {
        background-color: var(--warn-color);
        color: white;
    }

    .btn-warn:hover:not(:disabled) {
        background-color: #d32f2f;
    }

    .btn-warn:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }

    .btn-accent {
        background-color: var(--accent-color);
        color: white;
    }

    .btn-accent:hover:not(:disabled) {
        background-color: #ff6f00;
    }

    .btn-accent:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }

    @media (max-width: 900px) {
        .cards {
            grid-template-columns: 1fr;
        }
    }
</style>
