import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

export default defineConfig({
	plugins: [sveltekit()],
	server: {
		proxy: {
			'/rest': {
				target: 'http://localhost:8888',
				changeOrigin: true
			}
		}
	},
	base: './',                 // VERY IMPORTANT
	build: {
		outDir: 'dist',
		emptyOutDir: true
	}
});
