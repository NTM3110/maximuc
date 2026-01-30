// src/lib/interfaces/communication.interface.ts

// === SERIAL ===
export const BAUD_RATES = [9600, 19200, 38400, 57600, 115200] as const;
export const DATA_BITS = [5, 6, 7, 8] as const;
export const STOP_BITS = [1, 1.5, 2] as const;
export const PARITIES = [
    'PARITY_NONE',
    'PARITY_EVEN',
    'PARITY_ODD',
    'PARITY_MARK',
    'PARITY_SPACE',
] as const;

export type BaudRate = (typeof BAUD_RATES)[number];
export type DataBits = (typeof DATA_BITS)[number];
export type StopBits = (typeof STOP_BITS)[number];
export type Parity = (typeof PARITIES)[number];

export interface SerialPortConfig {
    id: string; // 'serial0', 'serial1', 'serial2'
    alias: string; // 'Port 1', 'Port 2'
    port: string; // '/dev/ttyV0', '/dev/ttyV2'
    channel: string; // 'dev_serial_comm_0'
    baudRate: BaudRate;
    dataBits: DataBits;
    stopBits: StopBits;
    parity: Parity;
}

export type SerialFormData = Omit<SerialPortConfig, 'id' | 'alias' | 'port' | 'channel'>;

export interface SerialPortDefinition {
    id: string;
    alias: string;
    devicePath: string;
    channel: string;
}

// === NETWORK ===
export interface NetworkConfig {
    id: string; // 'eth1' or 'eth2'
    name: string; // 'Eth1' or 'Eth2'
    ipAddress: string | null;
    subnetMask: string | null;
    gateway: string | null;
    dns: string | null;
    mode: string | null;
}

export interface SaveNetworkResponse {
    interface: {
        ipAddress: string | null;
        subnetMask: string | null;
        gateway: string | null;
        dns: string | null;
    };
    exitCode: number;
}
