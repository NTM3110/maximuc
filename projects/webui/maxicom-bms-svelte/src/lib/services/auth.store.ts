import { writable } from 'svelte/store';
import { showToast } from './toast.store';
import { browser } from '$app/environment';

// Types
interface ApiRecord {
    record: {
        timestamp: number;
        flag: 'VALID';
        value: string | number | boolean;
    };
}

interface LatestValueAccountResponse {
    code: string;
    success: boolean;
    description: string | null;
    data: {
        username: string;
        password: string;
    };
}

interface OpenMucRecord {
    id: string;
    valueType: string;
    record: {
        timestamp: number;
        flag: 'VALID';
        value: string | number | boolean;
    };
}

const BASE_URL = '/rest';
const LATEST_VALUE_API = '/rest/latest-value';

// Store
const initialLoggedIn = browser ? sessionStorage.getItem('session-active') === 'true' : false;
export const isLoggedIn = writable<boolean>(initialLoggedIn);

// Actions
export async function login(username: string, password: string): Promise<boolean> {
    try {
        const params = new URLSearchParams({ accountID: '1' });
        const response = await fetch(`${LATEST_VALUE_API}/account?${params}`);

        if (!response.ok) throw new Error('Network response was not ok');

        const data: LatestValueAccountResponse = await response.json();

        if (data?.success && data.data) {
            const storedUser = data.data.username;
            const storedPass = data.data.password;

            if (username === storedUser && password === storedPass) {
                startSession();
                return true;
            }

            showToast('Incorrect username or password.', 'error');
            return false;
        }

        showToast('Invalid response from authentication server.', 'error');
        return false;
    } catch (err) {
        console.error('Error during login (latest-value API):', err);
        return loginFallback(username, password);
    }
}

async function loginFallback(username: string, password: string): Promise<boolean> {
    try {
        const [userRes, passRes] = await Promise.all([
            fetch(`${BASE_URL}/channels/account_1_username`).then(r => r.json() as Promise<ApiRecord>),
            fetch(`${BASE_URL}/channels/account_1_password`).then(r => r.json() as Promise<ApiRecord>)
        ]);

        const storedUser = userRes.record.value as string;
        const storedPass = passRes.record.value as string;

        if (username === storedUser && password === storedPass) {
            startSession();
            return true;
        }

        showToast('Incorrect username or password.', 'error');
        return false;
    } catch (err) {
        console.error('Error during login (fallback):', err);
        showToast('Could not connect to authentication server.', 'error');
        return false;
    }
}

export function logout() {
    if (browser) {
        sessionStorage.removeItem('session-active');
    }
    isLoggedIn.set(false);
    // Navigation should be handled by the component calling logout or a reactive statement
}

export async function changePassword(oldPass: string, newPass: string): Promise<boolean> {
    try {
        const params = new URLSearchParams({ accountID: '1' });
        const response = await fetch(`${LATEST_VALUE_API}/account?${params}`);
        const data: LatestValueAccountResponse = await response.json();

        if (!data?.success || !data.data) {
            showToast('Could not verify old password.', 'error');
            return false;
        }

        const storedPass = data.data.password;
        if (oldPass !== storedPass) {
            showToast('Incorrect old password.', 'error');
            return false;
        }

        const updatePayload: OpenMucRecord = {
            id: 'account_1_password',
            valueType: 'STRING',
            record: {
                timestamp: Date.now(),
                flag: 'VALID',
                value: newPass
            }
        };

        const putResponse = await fetch(`${BASE_URL}/channels/account_1_password/latestRecord`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(updatePayload)
        });

        if (putResponse.ok) {
            showToast('Password changed successfully.', 'success');
            return true;
        } else {
            throw new Error('Put failed');
        }

    } catch (err) {
        console.error('Error changing password', err);
        return changePasswordFallback(oldPass, newPass);
    }
}

async function changePasswordFallback(oldPass: string, newPass: string): Promise<boolean> {
    try {
        const passRes = await fetch(`${BASE_URL}/channels/account_1_password`).then(r => r.json() as Promise<ApiRecord>);
        const storedPass = passRes.record.value as string;

        if (oldPass !== storedPass) {
            showToast('Incorrect old password.', 'error');
            return false;
        }

        const payload = {
            record: {
                flag: 'VALID',
                value: newPass,
            },
        };

        const putResponse = await fetch(`${BASE_URL}/channels/account_1_password`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (putResponse.ok) {
            showToast('Password changed successfully.', 'success');
            return true;
        }

        throw new Error('Fallback put failed');
    } catch (err) {
        console.error('Error changing password fallback', err);
        showToast('Error saving new password.', 'error');
        return false;
    }
}

function startSession() {
    if (browser) {
        sessionStorage.setItem('session-active', 'true');
    }
    isLoggedIn.set(true);
}
