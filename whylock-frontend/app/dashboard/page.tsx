"use client";

import { Navbar } from "@/components/Navbar";
import { Dashboard } from "@/components/Dashboard";
import { useScanHistory } from "@/lib/use-scan-history";

export default function DashboardPage() {
  const { history, addEntry, clear } = useScanHistory();
  return (
    <div className="flex min-h-screen flex-col">
      <Navbar />
      <main className="mx-auto w-full max-w-6xl flex-1 px-6 py-16 md:px-10 md:py-20">
        <Dashboard history={history} onClear={clear} />
      </main>
      <footer className="border-t border-hairline px-6 py-6 text-center font-mono text-[11px] tracking-wide text-muted-dim">
        WHYLOCK · the decision engine for what to fix first
      </footer>
    </div>
  );
}
