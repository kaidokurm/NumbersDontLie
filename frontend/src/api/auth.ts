/**
 * Authentication API calls for email/password auth
 */
import { api } from "../shared/api/client";
import { unwrapApiData } from "../shared/api/unwrap";
import type { ApiResponse } from "../shared/types";

export interface AuthLoginRequest {
    email: string;
    password: string;
    twoFactorCode?: string;
}

export interface AuthRegisterRequest {
    email: string;
    password: string;
}

type RegisterResponseJson = {
    id: string;
    email: string;
    email_verified: boolean;
    message?: string;
};

// Login response as returned by backend (snake_case JSON)
interface LoginResponseJson {
    access_token: string;
    refresh_token: string;
    token_type?: string;
    expires_in?: number;
}

interface VerifyEmailResponseJson {
    message: string;
    email_verified: boolean;
}

interface ResendCodeResponseJson {
    message: string;
    cooldown_seconds: number;
}

interface PasswordResetResponseJson {
    message: string;
}

interface RefreshAccessTokenResponseJson {
    access_token: string;
    token_type?: string;
    expires_in?: number;
}

// Normalized login response (camelCase)
export interface LoginResponse {
    accessToken: string;
    refreshToken: string;
}

export interface RegisterResponse {
    id: string;
    email: string;
    emailVerified: boolean;
    message?: string;
}

export interface VerifyEmailResponse {
    message: string;
    emailVerified: boolean;
}

export interface ResendCodeResponse {
    message: string;
    cooldownSeconds: number;
}

export interface PasswordResetResponse {
    message: string;
}

export interface RefreshAccessTokenResponse {
    accessToken: string;
    tokenType?: string;
    expiresIn?: number;
}

/**
 * Register a new user with email and password
 */
export async function registerUser(email: string, password: string): Promise<RegisterResponse> {
    const raw = await api.post<RegisterResponseJson | ApiResponse<RegisterResponseJson>>(
        "/api/auth/register",
        { email, password } as AuthRegisterRequest
    );
    const data = unwrapApiData(raw);
    return {
        id: data.id,
        email: data.email,
        emailVerified: data.email_verified,
        message: data.message,
    };
}

/**
 * Login with email and password
 * Returns JWT access token and refresh token
 */
export async function loginUser(email: string, password: string, twoFactorCode?: string): Promise<LoginResponse> {
    const raw = await api.post<LoginResponseJson | ApiResponse<LoginResponseJson>>(
        "/api/auth/login",
        { email, password, twoFactorCode } as AuthLoginRequest
    );
    const data = unwrapApiData(raw as LoginResponseJson | ApiResponse<LoginResponseJson>);
    return {
        accessToken: data.access_token,
        refreshToken: data.refresh_token,
    };
}

/**
 * Verify email with one-time verification code.
 */
export async function verifyEmail(email: string, code: string): Promise<VerifyEmailResponse> {
    const raw = await api.post<VerifyEmailResponseJson | ApiResponse<VerifyEmailResponseJson>>(
        "/api/email-verification/verify",
        { email, code }
    );
    const data = unwrapApiData(raw);
    return { message: data.message, emailVerified: data.email_verified };
}

export async function resendVerificationCode(email: string): Promise<ResendCodeResponse> {
    const raw = await api.post<ResendCodeResponseJson | ApiResponse<ResendCodeResponseJson>>(
        "/api/email-verification/resend-code",
        { email }
    );
    const data = unwrapApiData(raw);
    return { message: data.message, cooldownSeconds: data.cooldown_seconds };
}

export async function requestPasswordReset(email: string): Promise<PasswordResetResponse> {
    const raw = await api.post<PasswordResetResponseJson | ApiResponse<PasswordResetResponseJson>>(
        "/api/password-reset/request",
        { email }
    );
    const data = unwrapApiData(raw);
    return { message: data.message };
}

export async function completePasswordReset(
    email: string,
    token: string,
    newPassword: string
): Promise<PasswordResetResponse> {
    const raw = await api.post<PasswordResetResponseJson | ApiResponse<PasswordResetResponseJson>>(
        "/api/password-reset/complete",
        { email, token, newPassword }
    );
    const data = unwrapApiData(raw);
    return { message: data.message };
}

export async function refreshAccessToken(refreshToken: string): Promise<RefreshAccessTokenResponse> {
    const raw = await api.post<RefreshAccessTokenResponseJson | ApiResponse<RefreshAccessTokenResponseJson>>(
        "/api/auth/refresh",
        { refresh_token: refreshToken }
    );
    const data = unwrapApiData(raw);
    return {
        accessToken: data.access_token,
        tokenType: data.token_type,
        expiresIn: data.expires_in,
    };
}
