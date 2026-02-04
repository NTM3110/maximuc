<script lang="ts">
  interface LogoProps {
    size?: 'small' | 'medium' | 'large';
  }

  let { size = 'medium' }: LogoProps = $props();
</script>

<div class="logo-container" class:small={size === 'small'} class:medium={size === 'medium'} class:large={size === 'large'}>
  <img src="/MAXiCom_Logo.png" alt="MAXiCom Logo" class="logo-image" />
</div>

<style>
  .logo-container {
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }

  .logo-image {
    object-fit: contain;
  }

  .logo-container.small .logo-image {
    height: 30px;
  }

  .logo-container.medium .logo-image {
    height: 45px;
  }

  .logo-container.large .logo-image {
    height: 55px;
  }
</style>
