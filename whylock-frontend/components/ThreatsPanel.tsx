import { Radar } from "lucide-react";

export function ThreatsPanel({ threats }: { threats: string[] }) {
  return (
    <div className="rounded-xl border border-hairline bg-surface p-5">
      <div className="mb-4 flex items-center gap-1.5 border-b border-hairline pb-3 font-mono text-[11px] uppercase tracking-widest text-muted">
        <Radar size={13} />
        Global threats
      </div>
      {threats.length === 0 ? (
        <div className="text-[13px] text-muted">No global threats matched</div>
      ) : (
        threats.map((t, i) => (
          <div
            key={i}
            className="flex items-start gap-2.5 border-b border-hairline py-2.5 text-[13px] leading-relaxed text-foreground/90 last:border-none"
          >
            <span className="mt-1.5 h-1.5 w-1.5 flex-shrink-0 rounded-full bg-warn" />
            <span>{t}</span>
          </div>
        ))
      )}
    </div>
  );
}
