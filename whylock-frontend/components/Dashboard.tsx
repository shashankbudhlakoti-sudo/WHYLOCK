"use client";

import { useMemo } from "react";
import { useAuth } from "@/lib/auth-context";
import { SERVICES } from "@/lib/services";
import { downloadReport } from "@/lib/report";
import type { HistoryEntry } from "@/types";
import { FileDown, LayoutDashboard, TrendingUp, History as HistoryIcon, Trash2 } from "lucide-react";

interface DashboardProps {
  history: HistoryEntry[];
  onClear: () => void;
}

/**
 * The authenticated home base: account summary, full scan history with
 * per-scan report export, and a quick-link panel to every backend service.
 * Mounted only once the user is signed in.
 */
export function Dashboard({ history, onClear }: DashboardProps) {
  const { username, role } = useAuth();

  const stats = useMemo(() => {
    if (history.length === 0) return { count: 0, avg: 0, last: null as HistoryEntry | null };
    const avg = Math.round(history.reduce((sum, h) => sum + h.score, 0) / history.length);
    return { count: history.length, avg, last: history[0] };
  }, [history]);

  return (
    <section id="dashboard" className="border-t border-hairline bg-surface/40 px-6 py-16 md:px-10 md:py-20">
      <div className="mx-auto max-w-6xl">
        <div className="mb-10 flex flex-col items-start justify-between gap-4 sm:flex-row sm:items-center">
          <div>
            <div className="mb-2 flex items-center gap-2 font-mono text-[11px] uppercase tracking-[0.16em] text-cyan">
              <LayoutDashboard size={13} />
              Dashboard
            </div>
            <h2 className="text-[26px] font-bold tracking-tight text-foreground md:text-[30px]">
              Welcome back, <span className="gradient-brand-text">{username}</span>
            </h2>
            {role && (
              <p className="mt-1 font-mono text-[12px] uppercase tracking-wide text-muted">
                {role} access
              </p>
            )}
          </div>
        </div>

        {/* Stats */}
        <div className="mb-10 grid grid-cols-1 gap-4 sm:grid-cols-3">
          <StatCard label="Total scans" value={String(stats.count)} icon={HistoryIcon} />
          <StatCard label="Average risk score" value={stats.count ? `${stats.avg}/100` : "—"} icon={TrendingUp} />
          <StatCard
            label="Last scan"
            value={stats.last ? stats.last.url : "No scans yet"}
            icon={LayoutDashboard}
            truncate
          />
        </div>

        {/* History with report export */}
        <div className="mb-12 rounded-2xl border border-hairline bg-surface-raised">
          <div className="flex items-center justify-between border-b border-hairline px-5 py-4">
            <p className="font-mono text-[11px] uppercase tracking-widest text-muted">
              Scan history
            </p>
            {history.length > 0 && (
              <button
                onClick={onClear}
                className="flex items-center gap-1.5 rounded-md border border-hairline px-2.5 py-1 font-mono text-[11px] text-muted transition-colors hover:border-danger/40 hover:text-danger"
              >
                <Trash2 size={11} />
                clear
              </button>
            )}
          </div>

          {history.length === 0 ? (
            <p className="px-5 py-10 text-center text-[13.5px] text-muted">
              No scans yet — run one above and it&apos;ll show up here, with a
              downloadable report.
            </p>
          ) : (
            <div className="divide-y divide-hairline">
              {history.map((h, i) => (
                <div key={i} className="flex flex-wrap items-center gap-4 px-5 py-4">
                  <div className="min-w-0 flex-1">
                    <p className="truncate font-mono text-[13px] text-foreground">{h.url}</p>
                    {h.time && (
                      <p className="font-mono text-[11px] text-muted-dim">
                        {new Date(h.time).toLocaleString()}
                      </p>
                    )}
                  </div>
                  <span className={`font-mono text-[12px] font-bold risk-${h.risk}`}>{h.risk}</span>
                  <span className="font-mono text-[12px] text-muted">{h.score}/100</span>
                  <button
                    onClick={() => h.result && downloadReport(h.result)}
                    disabled={!h.result}
                    title={h.result ? "Download PDF report" : "Re-run this scan to enable export"}
                    className="flex items-center gap-1.5 rounded-full border border-hairline px-3 py-1.5 font-mono text-[11px] text-muted transition-colors hover:border-accent/50 hover:text-accent disabled:cursor-not-allowed disabled:opacity-40"
                  >
                    <FileDown size={12} />
                    Report
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Quick links to all services */}
        <div>
          <p className="mb-4 font-mono text-[11px] uppercase tracking-widest text-muted">
            All services
          </p>
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-7">
            {SERVICES.map(({ icon: Icon, service, title }) => (
              <div
                key={service}
                title={title}
                className="flex flex-col items-center gap-2 rounded-xl border border-hairline bg-surface-raised px-3 py-4 text-center transition-colors hover:border-accent/40"
              >
                <Icon size={18} className="text-accent" strokeWidth={1.8} />
                <span className="font-mono text-[9.5px] leading-tight text-muted">{service}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}

function StatCard({
  label,
  value,
  icon: Icon,
  truncate,
}: {
  label: string;
  value: string;
  icon: typeof TrendingUp;
  truncate?: boolean;
}) {
  return (
    <div className="rounded-2xl border border-hairline bg-surface-raised p-5">
      <div className="mb-3 flex items-center gap-2 text-muted">
        <Icon size={15} />
        <span className="font-mono text-[10.5px] uppercase tracking-[0.12em]">{label}</span>
      </div>
      <p className={`text-[18px] font-semibold text-foreground ${truncate ? "truncate" : ""}`}>
        {value}
      </p>
    </div>
  );
}
