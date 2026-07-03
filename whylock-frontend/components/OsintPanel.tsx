import type { ScanResult } from "@/types";
import { Globe2 } from "lucide-react";

export function OsintPanel({ result }: { result: ScanResult }) {
  const rows = [
    { label: "URL", value: result.url },
    { label: "Scanned", value: result.scannedAt || "—" },
    { label: "Score", value: `${result.riskScore}/100` },
  ];

  return (
    <div className="rounded-xl border border-hairline bg-surface p-5">
      <div className="mb-4 flex items-center gap-1.5 border-b border-hairline pb-3 font-mono text-[11px] uppercase tracking-widest text-muted">
        <Globe2 size={13} />
        OSINT metadata
      </div>
      {rows.map((r) => (
        <div
          key={r.label}
          className="flex items-start gap-2.5 border-b border-hairline py-2.5 text-[13px] last:border-none"
        >
          <span className="mt-1.5 h-1.5 w-1.5 flex-shrink-0 rounded-full bg-accent" />
          <span className="break-all text-foreground/90">
            <span className="text-muted">{r.label}: </span>
            {r.value}
          </span>
        </div>
      ))}
    </div>
  );
}
