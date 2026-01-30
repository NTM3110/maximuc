<script lang="ts">
    import { isLoggedIn } from "$lib/services/auth.store";
    import { goto } from "$app/navigation";
    import { onMount } from "svelte";
    import Header from "$lib/components/Header.svelte";
    import Sidebar from "$lib/components/Sidebar.svelte";

    let { children } = $props();

    let sidenavOpen = $state(true);
    let isMobile = $state(false);

    onMount(() => {
        if (!$isLoggedIn) {
            goto("/login");
        }

        // Check if mobile
        const checkMobile = () => {
            isMobile = window.innerWidth < 768;
            if (isMobile) {
                sidenavOpen = false;
            }
        };
        checkMobile();
        window.addEventListener('resize', checkMobile);

        return () => {
            window.removeEventListener('resize', checkMobile);
        };
    });

    function toggleSidenav() {
        sidenavOpen = !sidenavOpen;
    }
</script>

{#if $isLoggedIn}
    <div class="app-container">
        <!-- Sidebar -->
        {#if sidenavOpen || !isMobile}
            <aside class="app-sidenav" class:mobile={isMobile}>
                <Sidebar />
            </aside>
        {/if}

        <!-- Overlay for mobile -->
        {#if isMobile && sidenavOpen}
            <div class="sidenav-overlay" onclick={toggleSidenav}></div>
        {/if}

        <!-- Main content -->
        <div class="app-sidenav-content">
            <Header onMenuToggle={toggleSidenav} />
            
            <main class="main-content">
                {@render children()}
            </main>
        </div>
    </div>
{/if}

<style>
    .app-container {
        display: flex;
        height: 100vh;
        overflow: hidden;
        background-color: var(--background-color);
    }

    .app-sidenav {
        width: var(--sidebar-width);
        height: 100%;
        background-color: #f5f5f5;
        box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
        z-index: 100;
        transition: transform 0.3s ease-in-out;
    }

    .app-sidenav.mobile {
        position: fixed;
        top: 0;
        left: 0;
        height: 100vh;
    }

    .sidenav-overlay {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(0, 0, 0, 0.5);
        z-index: 99;
    }

    .app-sidenav-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow: hidden;
    }

    .main-content {
        flex: 1;
        overflow-y: auto;
        padding: 24px;
        background-color: var(--background-color);
    }

    @media (max-width: 768px) {
        .main-content {
            padding: 16px;
        }
    }
</style>
