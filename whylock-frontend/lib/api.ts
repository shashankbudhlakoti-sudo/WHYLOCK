import type { AuthResponse, ScanResult } from "@/types";

const API_BASE =
  process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "") || "http://localhost:8085";

class ApiClientError extends Error {
  status?: number;
  constructor(message: string, status?: number) {
    super(message);
    this.status = status;
  }
}

async function request<T>(
  path: string,
  options: RequestInit = {},
  token?: string | null
): Promise<T> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(options.headers as Record<string, string> | undefined),
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });

  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const body = await res.json();
      message = body?.message || body?.error || message;
    } catch {
      // response wasn't JSON — keep default message
    }
    throw new ApiClientError(message, res.status);
  }

  // Some endpoints (e.g. /auth/register) return a plain string, not JSON.
  const text = await res.text();
  try {
    return JSON.parse(text) as T;
  } catch {
    return text as unknown as T;
  }
}

export const api = {
  login(username: string, password: string) {
    return request<AuthResponse>("/auth/login", {
      method: "POST",
      body: JSON.stringify({ username, password }),
    });
  },

  register(
    username: string,
    email: string,
    password: string,
    role?: string
  ) {
    return request<string>("/auth/register", {
      method: "POST",
      body: JSON.stringify({
        username,
        email,
        password,
        role,
      }),
    });
  },

  scanUrl(targetUrl: string, token: string) {
    return request<ScanResult>(
      "/api/scan/ai-analyze",
      {
        method: "POST",
        body: JSON.stringify({ targetUrl }),
      },
      token
    );
  },

  scanHistory(token: string) {
    return request<ScanResult[]>("/api/scan/history", { method: "GET" }, token);
  },

  scanStats(token: string) {
    return request<Record<string, unknown>>(
      "/api/scan/stats",
      { method: "GET" },
      token
    );
  },

  scanQuota(token: string) {
    return request<Record<string, unknown>>(
      "/api/scan/quota",
      { method: "GET" },
      token
    );
  },

  chatWithAssistant(message: string, context: string | undefined, token: string) {
    return request<string>(
      "/api/fix/chat",
      {
        method: "POST",
        body: JSON.stringify({ message, context }),
      },
      token
    );
  },
};

export { ApiClientError };
export { API_BASE };