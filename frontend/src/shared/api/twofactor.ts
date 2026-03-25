import { api } from "./client";
import type { ApiResponse } from "../types";
import { unwrapApiData } from "./unwrap";

export type TwoFactorStatusResponse = {
    enabled: boolean;
};

export type TwoFactorSetupResponse = {
    secret: string;
    otpauthUri: string;
};

export function getTwoFactorStatus(token: string): Promise<TwoFactorStatusResponse> {
    return api
        .get<TwoFactorStatusResponse | ApiResponse<TwoFactorStatusResponse>>("/api/auth/2fa/status", token)
        .then((response) => unwrapApiData(response));
}

export function setupTwoFactor(token: string): Promise<TwoFactorSetupResponse> {
    return api
        .post<TwoFactorSetupResponse | ApiResponse<TwoFactorSetupResponse>>("/api/auth/2fa/setup", {}, token)
        .then((response) => unwrapApiData(response));
}

export function enableTwoFactor(code: string, token: string): Promise<TwoFactorStatusResponse> {
    return api
        .post<TwoFactorStatusResponse | ApiResponse<TwoFactorStatusResponse>>("/api/auth/2fa/enable", { code }, token)
        .then((response) => unwrapApiData(response));
}

export function disableTwoFactor(code: string, token: string): Promise<TwoFactorStatusResponse> {
    return api
        .post<TwoFactorStatusResponse | ApiResponse<TwoFactorStatusResponse>>("/api/auth/2fa/disable", { code }, token)
        .then((response) => unwrapApiData(response));
}
