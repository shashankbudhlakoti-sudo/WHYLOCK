import type { ScanResult } from "@/types";
import { riskAction } from "@/lib/compliance";
import { RiskDial } from "@/components/RiskDial";
import { AlertTriangle, Cpu, Clock } from "lucide-react";

interface StatsGridProps {
  result: ScanResult;
}

export function StatsGrid({ result }: StatsGridProps) {
  const criticalHigh = result.findings.filter(
    (f) => f.severity === "CRITICAL" || f.severity === "HIGH"
  ).length;

  return (
    <div className="mb-6 grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
      <div className="flex items-center gap-4 rounded-xl border border-hairline bg-surface p-5">
        <RiskDial score={result.riskScore} risk={result.overallRisk} size={84} />
        <div>
          <div className="font-mono text-[10px] uppercase tracking-widest text-muted">
            Risk score
          </div>
          <div className={`mt-1.5 font-mono text-sm font-bold risk-${result.overallRisk}`}>
            {result.overallRisk}
          </div>
          <div className="mt-0.5 text-xs text-muted">{riskAction(result.overallRisk)}</div>
        </div>
      </div>

      <div className="rounded-xl border border-hairline bg-surface p-5">
        <div className="mb-3 flex items-center gap-1.5 font-mono text-[10px] uppercase tracking-widest text-muted">
          <AlertTriangle size={12} />
          Findings
        </div>
        <div className="font-mono text-3xl font-bold text-foreground">
          {result.findings.length}
        </div>
        <div className="mt-1 text-xs text-muted">{criticalHigh} critical / high</div>
      </div>

      <div className="rounded-xl border border-hairline bg-surface p-5">
        <div className="mb-3 flex items-center gap-1.5 font-mono text-[10px] uppercase tracking-widest text-muted">
          <Cpu size={12} />
          AI model
        </div>
        <div className="break-words font-mono text-sm font-semibold text-cyan">
          {result.aiModel || "groq · llama-3.3-70b"}
        </div>
      </div>

      <div className="rounded-xl border border-hairline bg-surface p-5">
        <div className="mb-3 flex items-center gap-1.5 font-mono text-[10px] uppercase tracking-widest text-muted">
          <Clock size={12} />
          Scanned at
        </div>
        <div className="font-mono text-sm text-foreground">{result.scannedAt || "—"}</div>
      </div>
    </div>
  );
}
