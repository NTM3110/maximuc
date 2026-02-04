<script lang="ts">
	import "../app.scss";
	import Toast from "$lib/components/Toast.svelte";
	import { loadConfig } from "$lib/services/config";
	import { onMount } from "svelte";
	import favicon from "$lib/assets/favicon.svg";

	let { children } = $props();

	onMount(async () => {
		await loadConfig();
	});
</script>

<svelte:head>
	<link rel="icon" href={favicon} />
</svelte:head>

<Toast />
{@render children()}
