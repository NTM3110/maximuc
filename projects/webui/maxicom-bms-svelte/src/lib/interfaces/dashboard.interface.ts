export interface DashboardItem {
    id: string;
    siteName: string;
    stringName: string;
    cellVol: 'On' | 'Off';
    cellRst: 'On' | 'Off';
    stringVol: 'On' | 'Off';
    current: 'On' | 'Off';
    ambient: 'On' | 'Off';
    soC: number | null;
    soH: number | null;
    updateTime: Date;
}
