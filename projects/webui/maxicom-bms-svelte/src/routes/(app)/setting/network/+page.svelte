<script lang="ts">
    import { onMount } from "svelte";
    import { getNetworkConfigs, saveNetworkConfig } from "$lib/services/communication";
    import type { NetworkConfig } from "$lib/interfaces/communication.interface";
    import { showToast } from "$lib/services/toast.store";

    let configs: NetworkConfig[] = [];
    let isLoading = true;
    let savingId: string | null = null;

    onMount(async () => {
        try {
            configs = await getNetworkConfigs();
        } catch (err) {
            console.error("Failed to load network configs:", err);
        } finally {
            isLoading = false;
        }
    });

    function isDhcp(config: NetworkConfig) {
        return config.mode === "dhcp";
    }

    async function save(config: NetworkConfig) {
        savingId = config.id;
        try {
            await saveNetworkConfig(config);
            try {
                configs = await getNetworkConfigs();
            } catch (err) {
                console.error("Failed to load network configs:", err);
            } finally {
                isLoading = false;
            }
        } catch (err) {
            console.error(err);
            showToast("Failed to save network configuration", "error");
        } finally {
            savingId = null;
        }
    }
</script>

<div class="container-fluid">
    <h1 class="mb-4">Network Configuration</h1>

    {#if isLoading}
        <div class="text-center py-5">
            <div class="spinner-border text-primary" role="status" />
        </div>
    {:else if configs.length === 0}
        <div class="alert alert-info">
            No network interfaces found.
        </div>
    {:else}
        <div class="row">
            {#each configs as config (config.id)}
                <div class="col-md-6 mb-4">
                    <div class="card h-100">
                        <div class="card-header bg-primary text-white">
                            <h5 class="mb-0">{config.name}</h5>
                        </div>

                        <div class="card-body">
                            <!-- DHCP toggle -->
                            <div class="form-check form-switch mb-3">
                                <input
                                    class="form-check-input"
                                    type="checkbox"
                                    checked={isDhcp(config)}
                                    on:change={(e) =>
                                        config.mode = e.currentTarget.checked ? "DHCP" : "STATIC"
                                    }
                                />
                                <label class="form-check-label">
                                    Auto (DHCP)
                                </label>
                            </div>

                            <!-- IP -->
                            <div class="mb-3">
                                <label class="form-label">IP Address</label>
                                <input
                                    class="form-control"
                                    bind:value={config.ipAddress}
                                    disabled={isDhcp(config)}
                                    placeholder="192.168.1.10"
                                />
                            </div>

                            <!-- Subnet -->
                            <div class="mb-3">
                                <label class="form-label">Subnet Mask</label>
                                <input
                                    class="form-control"
                                    bind:value={config.subnetMask}
                                    disabled={isDhcp(config)}
                                    placeholder="255.255.255.0"
                                />
                            </div>

                            <!-- Gateway -->
                            <div class="mb-3">
                                <label class="form-label">Gateway</label>
                                <input
                                    class="form-control"
                                    bind:value={config.gateway}
                                    disabled={isDhcp(config)}
                                    placeholder="192.168.1.1"
                                />
                            </div>

                            <!-- DNS -->
                            <div class="mb-3">
                                <label class="form-label">DNS</label>
                                <input
                                    class="form-control"
                                    bind:value={config.dns}
                                    disabled={isDhcp(config)}
                                    placeholder="8.8.8.8,1.1.1.1"
                                />
                                <div class="form-text">
                                    Separate multiple DNS servers with commas
                                </div>
                            </div>
                        </div>

                        <div class="card-footer text-end">
                            <button
                                class="btn btn-primary"
                                disabled={savingId === config.id}
                                on:click={() => save(config)}
                            >
                                {#if savingId === config.id}
                                    <span class="spinner-border spinner-border-sm me-2" />
                                    Saving…
                                {:else}
                                    Save
                                {/if}
                            </button>
                        </div>
                    </div>
                </div>
            {/each}
        </div>
    {/if}
</div>
