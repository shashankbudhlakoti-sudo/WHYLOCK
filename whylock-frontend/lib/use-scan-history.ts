"use client";

import { useCallback, useEffect, useState } from "react";
import type { HistoryEntry, ScanResult } from "@/types";
import { useAuth } from "@/lib/auth-context";

const HISTORY_PREFIX = "wl_history";
const MAX_ENTRIES = 10;

/* Each signed-in user gets their own history bucket, keyed off username,
   so a fresh registration never inherits another account's scanned sites,
   and each returning user still sees their own past scans. */
function historyKey(username: string | null): string {
  return username ? `${HISTORY_PREFIX}:${username}` : HISTORY_PREFIX;
}

function loadHistory(username: string | null): HistoryEntry[] {
  if (typeof window === "undefined") return [];
  try {
    const stored = localStorage.getItem(historyKey(username));
    return stored ? JSON.parse(stored) : [];
  } catch {
    return [];
  }
}

export function useScanHistory() {
  const { username } = useAuth();
  const [history, setHistory] = useState<HistoryEntry[]>([]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- intentional: reload from localStorage whenever the signed-in user changes (login, logout, or a brand-new registration)
    setHistory(loadHistory(username));
  }, [username]);

  const addEntry = useCallback(
    (result: ScanResult) => {
      setHistory((prev) => {
        const next = [
          {
            url: result.url,
            risk: result.overallRisk,
            score: result.riskScore,
            time: result.scannedAt,
            result,
          },
          ...prev,
        ].slice(0, MAX_ENTRIES);
        localStorage.setItem(historyKey(username), JSON.stringify(next));
        return next;
      });
    },
    [username]
  );

  const clear = useCallback(() => {
    localStorage.removeItem(historyKey(username));
    setHistory([]);
  }, [username]);

  return { history, addEntry, clear };
}