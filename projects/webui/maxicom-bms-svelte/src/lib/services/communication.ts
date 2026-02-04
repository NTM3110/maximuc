import { writable, get } from 'svelte/store';
import { browser } from '$app/environment';
import { showToast } from './toast.store';
import type {
    SerialPortConfig,
    SerialPortDefinition,
    SerialFormData,
    NetworkConfig,
    BaudRate,
    DataBits,
    Parity,
    StopBits
} from '../interfaces/communication.interface';
import { BAUD_RATES, DATA_BITS, STOP_BITS, PARITIES } from '../interfaces/communication.interface';

// Re-export constants
export { BAUD_RATES, DATA_BITS, STOP_BITS, PARITIES };

const BASE_URL = '/rest';
const LATEST_VALUE_API = '/rest/latest-value';
const NETWORK_API = '/rest/network';
const CACHE_KEY = 'maxicom-serial-ports-cache';

// Stores
export const configuredPorts = writable<SerialPortConfig[]>([]);
export const isLoadingPorts = writable<boolean>(true);

// Observable-like functions for compatibility with Angular patterns
export function getPortsLoadingStatus() {
    return {
        subscribe: (callback: (value: boolean) => void) => {
            const unsubscribe = isLoadingPorts.subscribe(callback);
            return { unsubscribe };
        }
    };
}

export function getSerialPortsStream() {
    return {
        subscribe: (onNext: (value: SerialPortConfig[]) => void, onError?: (error: any) => void) => {
            const unsubscribe = configuredPorts.subscribe(onNext);
            return { unsubscribe };
        }
    };
}

// Mock ConfigService (replace with actual config loading if needed)
// In Angular this was injected. Here we can just define it or load it.
// For now, I'll hardcode the definitions as they seem static or loaded from a file.
// Ideally this should come from a config store.
const allPortDefinitions: SerialPortDefinition[] = [
    { id: 'serial0', alias: 'Port 1', devicePath: '/dev/ttyS0', channel: 'dev_serial_comm_0' },
    { id: 'serial1', alias: 'Port 2', devicePath: '/dev/ttyS1', channel: 'dev_serial_comm_1' },
    { id: 'serial2', alias: 'Port 3', devicePath: '/dev/ttyS2', channel: 'dev_serial_comm_2' }
]; // TODO: Load this from actual config

// Initialize
if (browser) {
    loadCacheFromStorage();
    loadInitialPorts();
}

function loadCacheFromStorage() {
    try {
        const cached = localStorage.getItem(CACHE_KEY);
        if (cached) {
            const ports = JSON.parse(cached);
            configuredPorts.set(ports);
            console.log(`[Serial] Loaded ${ports.length} ports from cache`);
        }
    } catch (e) {
        console.error('[Serial] Error loading cache:', e);
    }
}

function saveCacheToStorage(ports: SerialPortConfig[]) {
    try {
        localStorage.setItem(CACHE_KEY, JSON.stringify(ports));
    } catch (e) {
        console.error('[Serial] Error saving cache:', e);
    }
}

async function loadInitialPorts() {
    console.log('[Serial] Loading initial ports...');
    isLoadingPorts.set(true);

    try {
        let ports = await loadPortsFromLatestValue();
        if (ports.length === 0) {
            console.log('[Serial] Latest-value empty, fallback to channels...');
            ports = await fetchPortsFromChannels();
        }
        handlePortsLoaded(ports);
    } catch (err) {
        console.error('[Serial] Error loading ports:', err);
        handlePortsLoaded([]);
    }
}

async function loadPortsFromLatestValue(): Promise<SerialPortConfig[]> {
    try {
        const res = await fetch(`${LATEST_VALUE_API}/dev`);
        const json = await res.json();

        if (!json?.success || !json.data) return [];

        const devValues = json.data as Record<string, string | null>;
        const configs: SerialPortConfig[] = [];

        for (const def of allPortDefinitions) {
            const settings = devValues[def.channel];
            if (typeof settings === 'string' && settings.trim() !== '') {
                const parsed = parsePortConfig(def, settings);
                if (parsed) configs.push(parsed);
            }
        }
        return configs;
    } catch (err) {
        console.warn('[Serial] Latest-value failed:', err);
        return [];
    }
}

async function fetchPortsFromChannels(): Promise<SerialPortConfig[]> {
    const promises = allPortDefinitions.map(async (def) => {
        try {
            const res = await fetch(`${BASE_URL}/channels/${def.channel}`);
            const json = await res.json();
            const settings = json?.record?.value;

            if (settings && typeof settings === 'string' && settings.trim() !== '') {
                return parsePortConfig(def, settings);
            }
            return null;
        } catch (err) {
            console.error(`[Serial] Error fetching ${def.channel}:`, err);
            return null;
        }
    });

    const results = await Promise.all(promises);
    return results.filter((p): p is SerialPortConfig => p !== null);
}

function handlePortsLoaded(loadedPorts: SerialPortConfig[]) {
    const currentCache = get(configuredPorts);
    let finalPorts = loadedPorts;

    if (loadedPorts.length < allPortDefinitions.length && currentCache.length > 0) {
        // Merge logic
        const merged: SerialPortConfig[] = [];
        allPortDefinitions.forEach(def => {
            const fromApi = loadedPorts.find(p => p.id === def.id);
            if (fromApi) {
                merged.push(fromApi);
            } else {
                const fromCache = currentCache.find(p => p.id === def.id);
                if (fromCache) merged.push(fromCache);
            }
        });
        finalPorts = merged;
    }

    configuredPorts.set(finalPorts);
    saveCacheToStorage(finalPorts);
    isLoadingPorts.set(false);
}

function parsePortConfig(def: SerialPortDefinition, settings: string): SerialPortConfig | null {
    try {
        const devicePathMatch = settings.match(/^([^:]+):/);
        const devicePath = devicePathMatch ? devicePathMatch[1] : def.devicePath;

        const regex = /:(\d+):DATABITS_(\d):([A-Z_]+):STOPBITS_([\d_]+):/;
        const matches = settings.match(regex);

        if (!matches || matches.length < 5) return null;

        const baudRate = parseInt(matches[1], 10);
        const dataBits = parseInt(matches[2], 10);
        const parity = matches[3] as Parity;
        const stopBits = parseFloat(matches[4].replace('_', '.'));

        return {
            id: def.id,
            alias: def.alias,
            port: devicePath,
            channel: def.channel,
            baudRate: baudRate as BaudRate,
            dataBits: dataBits as DataBits,
            parity: parity as Parity,
            stopBits: stopBits as StopBits,
        };
    } catch (e) {
        console.error(`[Serial] Parse error for ${def.alias}:`, e);
        return null;
    }
}

export function getAllPortDefinitions() {
    return [...allPortDefinitions];
}

export async function addSerialPort(formData: SerialFormData & { portId?: string }) {
    let selectedDef: SerialPortDefinition | undefined;

    if (formData.portId) {
        selectedDef = allPortDefinitions.find(d => d.id === formData.portId);
        if (!selectedDef) throw new Error('Selected port not found');

        // Check if configured
        const res = await fetch(`${BASE_URL}/channels/${selectedDef.channel}`);
        const json = await res.json();
        const settings = json?.record?.value;
        if (settings && typeof settings === 'string' && settings.trim() !== '') {
            throw new Error('Port already configured');
        }
    } else {
        // Find first available
        for (const def of allPortDefinitions) {
            const res = await fetch(`${BASE_URL}/channels/${def.channel}`);
            const json = await res.json();
            const settings = json?.record?.value;
            if (!settings || typeof settings !== 'string' || settings.trim() === '') {
                selectedDef = def;
                break;
            }
        }
        if (!selectedDef) throw new Error('Max ports reached');
    }

    return doAddSerialPort(selectedDef, formData);
}

async function doAddSerialPort(def: SerialPortDefinition, formData: SerialFormData) {
    const newPort: SerialPortConfig = {
        ...formData,
        id: def.id,
        alias: def.alias,
        port: def.devicePath,
        channel: def.channel
    };

    await apiPutSerialChannel(newPort.channel, newPort);

    configuredPorts.update(ports => {
        const newPorts = [...ports, newPort].sort((a, b) => a.alias.localeCompare(b.alias));
        saveCacheToStorage(newPorts);
        return newPorts;
    });

    showToast(`Successfully added ${newPort.alias}`, 'success');
    return newPort;
}

export async function updateSerialPort(portId: string, formData: SerialFormData) {
    const ports = get(configuredPorts);
    const index = ports.findIndex(p => p.id === portId);
    if (index === -1) throw new Error('Port not found');

    const updatedPort: SerialPortConfig = {
        ...ports[index],
        ...formData,
        port: ports[index].port,
        id: ports[index].id,
        alias: ports[index].alias,
        channel: ports[index].channel
    };

    await apiPutSerialChannel(updatedPort.channel, updatedPort);

    configuredPorts.update(p => {
        const newPorts = [...p];
        newPorts[index] = updatedPort;
        saveCacheToStorage(newPorts);
        return newPorts;
    });

    showToast(`Successfully updated ${updatedPort.alias}`, 'success');
    return updatedPort;
}

export async function deleteSerialPort(portId: string) {
    const ports = get(configuredPorts);
    const port = ports.find(p => p.id === portId);
    if (!port) throw new Error('Port not found');

    await apiPutSerialChannel(port.channel, null);

    configuredPorts.update(p => {
        const newPorts = p.filter(x => x.id !== portId);
        saveCacheToStorage(newPorts);
        return newPorts;
    });

    showToast(`Successfully deleted ${port.alias}`, 'success');
}

async function apiPutSerialChannel(channel: string, config: SerialPortConfig | null) {
    let value = '';
    if (config) {
        const stopBits = config.stopBits.toString().replace('.', '_');
        value = `${config.port}:RTU:SERIAL_ENCODING_RTU:${config.baudRate}:DATABITS_${config.dataBits}:${config.parity}:STOPBITS_${stopBits}:ECHO_FALSE:FLOWCONTROL_NONE:FLOWCONTROL_NONE`;
    }

    const payload = { record: { flag: 'VALID', value } };
    const res = await fetch(`${BASE_URL}/channels/${channel}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });

    if (!res.ok) throw new Error('Failed to save configuration');
}

export async function getNetworkConfigs(): Promise<NetworkConfig[]> {
    try {
        const res = await fetch(NETWORK_API);
        if (!res.ok) throw new Error('Failed to fetch network configs');
        return await res.json();
    } catch (err) {
        console.error('Error getting network configs:', err);
        return [];
    }
}

export async function saveNetworkConfig(config: NetworkConfig): Promise<NetworkConfig> {
    const payload = {
        ipAddress: config.ipAddress,
        subnetMask: config.subnetMask,
        gateway: config.gateway,
        dns: config.dns,
        mode: config.mode
    };

    const res = await fetch(`${NETWORK_API}/${config.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });

    if (!res.ok) throw new Error('Failed to save network config');

    showToast('Network saved successfully', 'success');
    return await res.json();
}
