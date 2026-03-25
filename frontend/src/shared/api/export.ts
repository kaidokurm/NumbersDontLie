import { API_BASE_URL } from "../config";

function extractFilename(contentDisposition: string | null): string {
    if (!contentDisposition) return `numbers-dont-lie-export-${Date.now()}.json`;

    const utf8 = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
    if (utf8?.[1]) return decodeURIComponent(utf8[1]);

    const basic = contentDisposition.match(/filename="?([^";]+)"?/i);
    if (basic?.[1]) return basic[1];

    return `numbers-dont-lie-export-${Date.now()}.json`;
}

export async function downloadDataExport(token: string): Promise<void> {
    const res = await fetch(`${API_BASE_URL}/api/export`, {
        method: "GET",
        headers: {
            Authorization: `Bearer ${token}`,
        },
    });

    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(text || `Export failed (HTTP ${res.status})`);
    }

    const blob = await res.blob();
    const filename = extractFilename(res.headers.get("content-disposition"));

    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
}
