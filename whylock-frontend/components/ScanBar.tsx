"use client";

import { useState, type KeyboardEvent } from "react";
import { Search, ArrowRight } from "lucide-react";

interface ScanBarProps {
  onScan: (url: string) => void;
  loading: boolean;
}

export function ScanBar({ onScan, loading }: ScanBarProps) {
  const [url, setUrl] = useState("");

  function submit() {
    const trimmed = url.trim();
    if (!trimmed) return;
    onScan(trimmed);
  }

  function handleKeyDown(e: KeyboardEvent<HTMLInputElement>) {
    if (e.key === "Enter") submit();
  }

  return (
    <div className="flex w-full max-w-xl flex-col gap-3 sm:flex-row">
      <div className="relative flex-1">
        <Search
          size={17}
          className="pointer-events-none absolute left-5 top-1/2 -translate-y-1/2 text-muted-dim"
        />
        <input
          type="text"
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="https://example.com"
          className="w-full rounded-full border border-hairline-strong bg-surface py-4 pl-12 pr-5 text-[15px] text-foreground outline-none transition-colors placeholder:text-muted-dim focus:border-accent"
        />
      </div>
      <button
        onClick={submit}
        disabled={loading}
        className="gradient-brand flex items-center justify-center gap-2 whitespace-nowrap rounded-full px-7 py-4 text-sm font-semibold text-white shadow-[0_8px_24px_-8px_rgba(139,92,246,0.6)] transition-transform hover:scale-[1.02] disabled:cursor-not-allowed disabled:opacity-50"
      >
        {loading ? "Scanning…" : "Scan now"}
        {!loading && <ArrowRight size={16} />}
      </button>
    </div>
  );
}
