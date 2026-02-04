import type { SerialPortDefinition } from '../interfaces/communication.interface';

interface AppConfig {
    siteName: string;
    serialPorts: SerialPortDefinition[];
}

let config: AppConfig = {
    siteName: 'Site',
    serialPorts: []
};

export async function loadConfig() {
    try {
        const response = await fetch('/assets/config/app-config.json');
        const data = await response.json();
        config = data;
    } catch (e) {
        console.error('Failed to load app config:', e);
    }
}

export function getSiteName(): string {
    return config.siteName || 'Site';
}

export function getSerialPorts(): SerialPortDefinition[] {
    return config.serialPorts || [];
}
