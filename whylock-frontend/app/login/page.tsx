"use client";

import { useState } from "react";
import { useAuth } from "@/lib/auth-context";
import { useRouter } from "next/navigation";

export default function LoginPage() {
  const { login } = useAuth();
  const router = useRouter();

  const [username, setUsername] = useState("");
  const [email, setEmail] = useState(""); // NEW (UI only)
  const [password, setPassword] = useState("");

  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();

    setError(null);
    setLoading(true);

    try {
      // Login remains Username + Password
      await login(username, password);
      router.push("/dashboard");
    } catch (err: any) {
      setError(err?.message || "Login failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-xl px-6 py-20">
      <h1 className="text-2xl font-bold mb-4">Sign in</h1>

      <form onSubmit={submit} className="space-y-4">

        {/* Username */}
        <input
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          placeholder="Username"
          type="text"
          required
          className="w-full rounded-md border border-hairline px-3 py-2"
        />

        {/* Email (same design, untouched) */}
        <input
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="Email"
          type="email"
          required
          className="w-full rounded-md border border-hairline px-3 py-2"
        />

        {/* Password */}
        <input
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Password"
          type="password"
          required
          className="w-full rounded-md border border-hairline px-3 py-2"
        />

        {error && (
          <div className="text-sm text-danger">
            {error}
          </div>
        )}

        <div className="flex items-center justify-between">
          <button
            type="submit"
            disabled={loading}
            className="rounded-md bg-accent px-4 py-2 text-white disabled:opacity-60"
          >
            {loading ? "Signing in…" : "Sign in"}
          </button>

          <a
            href="/register"
            className="font-mono text-sm text-muted underline"
          >
            Create account
          </a>
        </div>
      </form>
    </div>
  );
}