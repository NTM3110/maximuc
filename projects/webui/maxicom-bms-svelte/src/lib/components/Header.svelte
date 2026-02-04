<script lang="ts">
  import { logout } from '$lib/services/auth.store';
  import { goto } from '$app/navigation';
  import { onMount, onDestroy } from 'svelte';

  interface HeaderProps {
    onMenuToggle?: () => void;
  }

  let { onMenuToggle }: HeaderProps = $props();

  let currentTime = $state(new Date());
  let interval: number;

  onMount(() => {
    interval = setInterval(() => {
      currentTime = new Date();
    }, 1000);
  });

  onDestroy(() => {
    if (interval) clearInterval(interval);
  });

  function handleLogout() {
    logout();
    goto('/login');
  }

  function formatTime(date: Date): string {
    return date.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
      second: '2-digit',
      hour12: true
    });
  }
</script>

<header class="main-header">
  <div class="header-left">
    <button class="menu-button" onclick={onMenuToggle} type="button">
      <i class="bi bi-list"></i>
    </button>
    <span class="app-title">Battery Monitoring System</span>
  </div>

  <div class="header-right">
    <span class="current-time">
      {formatTime(currentTime)}
    </span>
    <button class="logout-button" onclick={handleLogout} type="button" title="Logout">
      <i class="bi bi-box-arrow-right"></i>
    </button>
  </div>
</header>

<style>
  .main-header {
    height: var(--header-height);
    background-color: var(--primary-color);
    color: white;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 16px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    position: relative;
    z-index: 10;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .menu-button {
    background: none;
    border: none;
    color: white;
    cursor: pointer;
    padding: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    transition: background-color 0.2s;
  }

  .menu-button:hover {
    background-color: rgba(255, 255, 255, 0.1);
  }

  .menu-button i {
    font-size: 1.5rem;
  }

  .app-title {
    font-size: 1.25rem;
    font-weight: 400;
    letter-spacing: 0.5px;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .current-time {
    font-size: 0.875rem;
    opacity: 0.9;
  }

  .logout-button {
    background: none;
    border: none;
    color: white;
    cursor: pointer;
    padding: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    transition: background-color 0.2s;
  }

  .logout-button:hover {
    background-color: rgba(255, 255, 255, 0.1);
  }

  .logout-button i {
    font-size: 1.25rem;
  }

  @media (max-width: 768px) {
    .app-title {
      font-size: 1rem;
    }

    .current-time {
      display: none;
    }
  }
</style>
