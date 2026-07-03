"use client";

import {
  BrainCircuit,
  Globe2,
  Lock,
  Radar,
  ShieldCheck,
  Activity,
} from "lucide-react";

interface OrbitCardProps {
  icon: typeof Lock;
  label: string;
  className: string;
  delay: string;
  accent?: string;
}

function OrbitCard({ icon: Icon, label, className, delay, accent = "var(--accent)" }: OrbitCardProps) {
  return (
    <div
      className={`absolute hidden flex-col items-center gap-2 rounded-2xl border border-hairline-strong bg-surface/90 px-4 py-3.5 shadow-[0_20px_50px_-15px_rgba(0,0,0,0.7)] backdrop-blur-sm lg:flex ${className}`}
      style={{ animation: `orbit-float 7s ease-in-out infinite`, animationDelay: delay }}
    >
      <Icon size={20} style={{ color: accent }} strokeWidth={1.8} />
      <span className="whitespace-nowrap font-mono text-[10px] uppercase tracking-[0.14em] text-muted">
        {label}
      </span>
    </div>
  );
}

/**
 * The hero's signature element: a central glowing lock with six
 * orbiting capability cards, gently floating at staggered intervals —
 * each one mapped to a real backend service, not decoration.
 */
export function HeroCubes({ riskScore }: { riskScore: number | null }) {
  return (
    <div className="relative mx-auto h-[420px] w-full max-w-[560px] lg:h-[480px]">
      {/* connecting rings */}
      <div className="absolute left-1/2 top-1/2 h-[260px] w-[260px] -translate-x-1/2 -translate-y-1/2 rounded-full border border-hairline" />
      <div className="absolute left-1/2 top-1/2 h-[380px] w-[380px] -translate-x-1/2 -translate-y-1/2 rounded-full border border-hairline opacity-60" />

      {/* central lock */}
      <div className="absolute left-1/2 top-1/2 flex -translate-x-1/2 -translate-y-1/2 items-center justify-center">
        <div
          className="absolute h-[160px] w-[160px] rounded-full opacity-70 blur-2xl"
          style={{ background: "radial-gradient(circle, var(--accent), transparent 70%)" }}
        />
        <div
          className="absolute h-[136px] w-[136px] rounded-full"
          style={{
            background:
              "conic-gradient(from 0deg, var(--accent), var(--accent-2), var(--cyan), var(--accent))",
            animation: "spin-slow 14s linear infinite",
            mask: "radial-gradient(farthest-side, transparent calc(100% - 2px), #000 calc(100% - 2px))",
            WebkitMask:
              "radial-gradient(farthest-side, transparent calc(100% - 2px), #000 calc(100% - 2px))",
          }}
        />
        <div className="relative flex h-[112px] w-[112px] items-center justify-center rounded-full border border-hairline-strong bg-surface">
          <Lock size={42} className="text-foreground" strokeWidth={1.6} />
        </div>
      </div>

      <OrbitCard
        icon={BrainCircuit}
        label="AI Analysis"
        className="left-[6%] top-[8%]"
        delay="0s"
      />
      <OrbitCard
        icon={Globe2}
        label="Threat Intel"
        className="right-[6%] top-[8%]"
        delay="1.2s"
        accent="var(--cyan)"
      />
      <OrbitCard
        icon={ShieldCheck}
        label="SSL Security"
        className="left-[2%] top-[58%]"
        delay="2.4s"
        accent="var(--lock-green)"
      />
      <OrbitCard
        icon={Radar}
        label="Vulnerability Scan"
        className="right-[2%] top-[58%]"
        delay="0.6s"
        accent="var(--warn)"
      />
      <OrbitCard
        icon={Activity}
        label="Real-Time Protection"
        className="bottom-[2%] right-[22%]"
        delay="1.8s"
        accent="var(--accent-2)"
      />

      {/* live risk score chip — the one data-backed element in the cluster */}
      <div
        className="absolute bottom-[2%] left-[20%] hidden flex-col items-start gap-0.5 rounded-2xl border border-hairline-strong bg-surface/90 px-4 py-3.5 shadow-[0_20px_50px_-15px_rgba(0,0,0,0.7)] backdrop-blur-sm lg:flex"
        style={{ animation: "orbit-float 7s ease-in-out infinite", animationDelay: "3s" }}
      >
        <span className="font-mono text-[10px] uppercase tracking-[0.14em] text-muted">
          Risk score
        </span>
        <span className="font-mono text-lg font-bold text-cyan">
          {riskScore !== null ? riskScore : "—"}
          <span className="text-xs font-normal text-muted-dim">/100</span>
        </span>
      </div>

      {/* mobile fallback: simple icon row under the lock */}
      <div className="absolute -bottom-2 left-1/2 flex -translate-x-1/2 gap-4 lg:hidden">
        {[BrainCircuit, Globe2, ShieldCheck, Radar].map((Icon, i) => (
          <div
            key={i}
            className="flex h-10 w-10 items-center justify-center rounded-xl border border-hairline-strong bg-surface"
          >
            <Icon size={16} className="text-accent" strokeWidth={1.8} />
          </div>
        ))}
      </div>
    </div>
  );
}
