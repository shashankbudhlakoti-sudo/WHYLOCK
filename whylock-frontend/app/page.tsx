"use client";

import { useState } from "react";
import { Navbar } from "@/components/Navbar";
import { Hero } from "@/components/Hero";
import { LiveShowcase } from "@/components/LiveShowcase";
import { ScanBar } from "@/components/ScanBar";
import { ScanSequenceSection } from "@/components/ScanSequenceSection";
import { ServiceGrid } from "@/components/ServiceGrid";
import { ServiceShowcase } from "@/components/ServiceShowcase";
import { Dashboard } from "@/components/Dashboard";
import { StatsGrid } from "@/components/StatsGrid";
import { SummaryBox } from "@/components/SummaryBox";
import { FindingsPanel } from "@/components/FindingsPanel";
import { AssistantPanel } from "@/components/AssistantPanel";
import { ThreatsPanel } from "@/components/ThreatsPanel";
import { OsintPanel } from "@/components/OsintPanel";
import { ComplianceGrid } from "@/components/ComplianceGrid";
import { LockLogo } from "@/components/LockLogo";
import { useAuth } from "@/lib/auth-context";
import { useScanHistory } from "@/lib/use-scan-history";
import { buildCompliance } from "@/lib/compliance";
import { downloadReport } from "@/lib/report";
import { api, ApiClientError } from "@/lib/api";
import { FileDown } from "lucide-react";
import type { ScanResult } from "@/types";

export default function Home() {
  const { token, isAuthenticated } = useAuth();
  const { history, addEntry, clear } = useScanHistory();

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<ScanResult | null>(null);

  async function handleScan(url: string) {
    if (!isAuthenticated || !token) {
      document.getElementById("login")?.scrollIntoView({ behavior: "smooth", block: "center" });
      return;
    }
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      const data = await api.scanUrl(url, token);
      setResult(data);
      addEntry(data);
    } catch (err) {
      setError(
        err instanceof ApiClientError
          ? err.message
          : "Unable to reach the scanning engine. Check that the backend is running."
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex min-h-screen flex-col">
      <Navbar />

      <Hero
        history={history}
        onWatchDemo={() =>
          document.getElementById("scan")?.scrollIntoView({ behavior: "smooth" })
        }
      />

      <ServiceGrid />

      <LiveShowcase />

      <main id="scan" className="mx-auto w-full max-w-6xl flex-1 px-6 py-16 md:px-10 md:py-20">
        <div className="mb-10 flex flex-col items-start gap-2">
          <h2 className="text-[26px] font-bold tracking-tight text-foreground md:text-[30px]">
            Run a scan
          </h2>
          <p className="max-w-lg text-[14.5px] leading-relaxed text-muted">
            Paste any URL and WHYLOCK scans it end to end — headers, certs,
            subdomains, breach exposure — then tells you exactly what to fix
            first, in plain language.
          </p>
        </div>

        <div className="mb-10">
          <ScanBar onScan={handleScan} loading={loading} />
        </div>

        {!result && !loading && !error && <ScanSequenceSection />}

        {loading && (
          <div className="flex flex-col items-center gap-4 py-20">
            <LockLogo size={48} state="scanning" />
            <p className="font-mono text-xs tracking-widest text-muted">
              AI SCANNING TARGET…
            </p>
          </div>
        )}

        {error && !loading && (
          <div className="flex flex-col items-center gap-3 rounded-xl border border-hairline bg-surface py-16 text-center">
            <LockLogo size={40} state="alert" />
            <p className="px-6 text-sm text-danger">{error}</p>
          </div>
        )}

        {result && !loading && (
          <div className="animate-fade-up">
            <div className="mb-4 flex justify-end">
              <button
                onClick={() => downloadReport(result)}
                className="flex items-center gap-2 rounded-full border border-hairline bg-surface px-4 py-2 text-[13px] font-medium text-foreground transition-colors hover:border-accent/50 hover:text-accent"
              >
                <FileDown size={15} />
                Download report
              </button>
            </div>
            <StatsGrid result={result} />
            {result.summary && <SummaryBox summary={result.summary} />}
            <AssistantPanel result={result} />
            <div className="mb-6 grid grid-cols-1 gap-5 lg:grid-cols-[1fr_340px]">
              <FindingsPanel findings={result.findings} />
              <div className="space-y-4">
                <ThreatsPanel threats={result.globalThreats} />
                <OsintPanel result={result} />
              </div>
            </div>
            <ComplianceGrid frameworks={buildCompliance(result.findings)} />
          </div>
        )}

        {!result && !loading && !error && <ServiceShowcase />}
      </main>

      {isAuthenticated && <Dashboard history={history} onClear={clear} />}

      <footer className="border-t border-hairline px-6 py-6 text-center font-mono text-[11px] tracking-wide text-muted-dim">
        WHYLOCK · the decision engine for what to fix first
      </footer>
    </div>
  );
}