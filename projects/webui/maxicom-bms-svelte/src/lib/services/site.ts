// src/lib/services/site.ts

// Matches your Angular types
export interface LatestValueResponse<T> {
    success: boolean;
    data: T;
    message?: string;
}

interface ApiRecord {
    record: {
        timestamp?: number;
        flag: "VALID";
        value: string;
    };
}

const BASE_URL = "/rest";
const LATEST_VALUE_API = "/rest/latest-value";
const SITE_NAME_CHANNEL = "site_name_1";

const DEFAULT_SITE_NAME = "My Monitoring Site";

async function parseJsonSafe(res: Response) {
    const text = await res.text();
    try {
        return text ? JSON.parse(text) : null;
    } catch {
        return null;
    }
}

/**
 * Get site name from API
 * - Calls: GET /rest/latest-value/site-name
 * - Uses fallback "My Monitoring Site" on errors/invalid response
 */
export async function getSiteName(): Promise<string> {
    try {
        const res = await fetch(`${LATEST_VALUE_API}/site-name`, {
            method: "GET",
            headers: { Accept: "application/json" }
        });

        const json = (await parseJsonSafe(res)) as LatestValueResponse<string> | null;

        console.log("[Site] Latest-value API response:", json);

        if (!res.ok) {
            console.warn("[Site] HTTP error when loading site name:", res.status, json);
            return DEFAULT_SITE_NAME;
        }

        if (json?.success && typeof json.data === "string" && json.data.trim().length > 0) {
            return json.data;
        }

        console.warn("[Site] Unexpected latest-value response, falling back to default:", json);
        return DEFAULT_SITE_NAME;
    } catch (err) {
        console.error("[Site] Could not load site name from latest-value, using default.", err);
        return DEFAULT_SITE_NAME;
    }
}

/**
 * Update site name via API (PUT)
 * - Calls: PUT /rest/channels/site_name_1
 * - Payload matches Angular:
 *   { record: { flag: "VALID", value: name } }
 * - Throws error if request fails (same as Angular: throw err)
 */
export async function setSiteName(name: string): Promise<any> {
    const payload: ApiRecord = {
        record: {
            flag: "VALID",
            value: name
        }
    };

    console.log("[Site] Saving site name:", name);

    const res = await fetch(`${BASE_URL}/channels/${SITE_NAME_CHANNEL}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            Accept: "application/json"
        },
        body: JSON.stringify(payload)
    });

    const json = await parseJsonSafe(res);

    if (!res.ok) {
        console.error("[Site] Error saving site name:", res.status, json);
        // Throw so caller can show error message like Angular
        throw new Error(
            (json && (json.message || json.error)) ||
                `Error saving site name (HTTP ${res.status})`
        );
    }

    console.log("[Site] Site name saved successfully:", json);
    return json;
}
