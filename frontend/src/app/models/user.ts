export interface User {
    id_user?: number;
    username: string;
    password?: string;
    role?: string;
}

export interface LoginRequest {
    username: string;
    password: string;
}

// Enhanced Security API Response (supports both old and new format for backward compatibility)
export interface LoginResponse {
    // New enhanced format
    accessToken?: string;
    refreshToken?: string;
    tokenType?: string;
    expiresIn?: number;

    // Legacy format (backward compatibility)
    token?: string;

    // Common fields
    username: string;
    role: string;
    id_client?: number;
}

export interface RegisterRequest {
    username: string;
    password: string;
    role?: string;
}
