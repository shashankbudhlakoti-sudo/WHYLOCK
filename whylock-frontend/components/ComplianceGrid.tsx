import type { ComplianceFramework } from "@/types";
import { Check, X, ClipboardCheck } from "lucide-react";

export function ComplianceGrid({ frameworks }: { frameworks: ComplianceFramework[] }) {
  return (
    <div className="mb-6 grid grid-cols-1 gap-3 sm:grid-cols-3">
      {frameworks.map((c) => (
        <div key={c.name} className="rounded-xl border border-hairline bg-surface p-5">
          <div className="mb-1 flex items-center gap-1.5 font-mono text-[13px] text-cyan">
            <ClipboardCheck size={13} />
            {c.name}
          </div>
          <div className="mb-3 text-xs text-muted">
            {c.pass} passed · {c.fail} failed
          </div>
          <div className="space-y-1.5">
            {c.items.map((item) => (
              <div key={item.label} className="flex items-center gap-2 text-[12.5px]">
                {item.ok ? (
                  <Check size={13} className="flex-shrink-0 text-lock-green" />
                ) : (
                  <X size={13} className="flex-shrink-0 text-danger" />
                )}
                <span className="text-muted">{item.label}</span>
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
