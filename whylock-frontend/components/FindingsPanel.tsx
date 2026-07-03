import type { Finding } from "@/types";
import { ShieldAlert, CheckCircle2 } from "lucide-react";

function FindingCard({ finding }: { finding: Finding }) {
  const hasCve = finding.cveMatch && finding.cveMatch !== "null";

  return (
    <div className="mb-3 rounded-lg border border-hairline p-4 transition-colors hover:border-hairline-strong">
      <div className="mb-2 flex items-center gap-2.5">
        <span
          className={`badge-${finding.severity} rounded-md border px-2 py-0.5 font-mono text-[10px] font-bold tracking-wide`}
        >
          {finding.severity}
        </span>
        <span className="text-[14px] font-medium text-foreground">{finding.title}</span>
      </div>
      <p className="mb-2.5 text-[13px] leading-relaxed text-muted">{finding.description}</p>
      {hasCve && (
        <div className="mb-2 font-mono text-[11px] text-accent">CVE: {finding.cveMatch}</div>
      )}
      {finding.fixCode && (
        <pre className="overflow-x-auto rounded-md border border-hairline bg-surface-raised p-3 font-mono text-[11px] leading-relaxed text-lock-green">
          {finding.fixCode}
        </pre>
      )}
    </div>
  );
}

export function FindingsPanel({ findings }: { findings: Finding[] }) {
  return (
    <div className="rounded-xl border border-hairline bg-surface p-5">
      <div className="mb-4 flex items-center gap-1.5 border-b border-hairline pb-3 font-mono text-[11px] uppercase tracking-widest text-muted">
        <ShieldAlert size={13} />
        Security findings ({findings.length})
      </div>
      {findings.length === 0 ? (
        <div className="flex items-center gap-2 text-sm text-lock-green">
          <CheckCircle2 size={16} />
          No issues found — site looks secure!
        </div>
      ) : (
        findings.map((f, i) => <FindingCard key={i} finding={f} />)
      )}
    </div>
  );
}
