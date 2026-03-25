import { apiFetch } from "./client";
import { unwrapApiData } from "./unwrap";
import type { ApiResponse } from "../types";

export type LinkedIdentity = {
    provider: string;
    providerSub: string;
    linkedAt: string;
};

export type AccountAuthMethods = {
    hasPassword: boolean;
    identities: LinkedIdentity[];
};

export type AccountLinkResult = {
    userId: string;
    alreadyLinked: boolean;
    message: string;
};

export function isEmailCollisionErrorMessage(message: string | undefined): boolean {
    if (!message) return false;
    return message.toLowerCase().includes("account with this email already exists");
}

export async function getAccountIdentities(token: string): Promise<AccountAuthMethods> {
    const response = await apiFetch<ApiResponse<AccountAuthMethods> | AccountAuthMethods>(
        "/api/account/identities",
        { method: "GET", token }
    );
    return unwrapApiData(response);
}

export async function linkCurrentIdentityByEmail(token: string): Promise<AccountLinkResult> {
    const response = await apiFetch<ApiResponse<AccountLinkResult> | AccountLinkResult>(
        "/api/account/identities/link-by-email",
        { method: "POST", token }
    );
    return unwrapApiData(response);
}

export async function deleteMyAccount(token: string, confirmation: string): Promise<void> {
    await apiFetch<void>("/api/account", {
        method: "DELETE",
        token,
        body: JSON.stringify({ confirmation }),
    });
}
