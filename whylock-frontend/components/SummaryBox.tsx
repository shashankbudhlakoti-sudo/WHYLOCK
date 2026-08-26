import { Sparkles } from "lucide-react";

export function SummaryBox({ summary }: { summary: string }) {
  return (
    <div className="mb-6 rounded-xl border border-hairline border-l-[3px] border-l-accent bg-surface p-5">
      <div className="mb-2 flex items-center gap-1.5 font-mono text-[10px] uppercase tracking-widest text-accent">
        <Sparkles size={12} />
        AI analysis
      </div>
      <p className="text-[15px] leading-relaxed text-foreground/90">{summary}</p>
    </div>
  );
}
