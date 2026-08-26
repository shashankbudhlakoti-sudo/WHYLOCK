"use client";

import { Sparkles, ArrowRight, PlayCircle } from "lucide-react";
import { HeroCubes } from "@/components/HeroCubes";
import { LoginCard } from "@/components/LoginCard";
import { ImageTicker } from "@/components/ImageTicker";
import type { HistoryEntry } from "@/types";

interface HeroProps {
  history: HistoryEntry[];
  onWatchDemo: () => void;
}

export function Hero({ history, onWatchDemo }: HeroProps) {
  const latestScore = history[0]?.score ?? null;

  return (
    <section id="login" className="relative overflow-hidden">
      <div
        aria-hidden
        className="pointer-events-none absolute -top-40 left-1/4 h-[560px] w-[560px] rounded-full opacity-[0.15] blur-3xl"
        style={{ background: "radial-gradient(circle, var(--accent), transparent 70%)" }}
      />
      <div
        aria-hidden
        className="pointer-events-none absolute -top-20 right-0 h-[420px] w-[420px] rounded-full opacity-[0.12] blur-3xl"
        style={{ background: "radial-gradient(circle, var(--accent-2), transparent 70%)" }}
      />

      <div className="relative mx-auto grid max-w-6xl grid-cols-1 items-center gap-16 px-6 py-16 md:px-10 md:py-20 lg:grid-cols-[1fr_1.05fr_auto] lg:gap-10">
        {/* Left: pitch */}
        <div>
          <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-hairline bg-surface px-3.5 py-1.5 font-mono text-[11px] uppercase tracking-[0.16em] text-muted">
            <Sparkles size={13} className="text-cyan" />
            AI Powered · Real Time · Explainable
          </div>

          <h1 className="text-[40px] font-bold leading-[1.06] tracking-tight text-foreground sm:text-[48px] xl:text-[56px]">
            Know why
            <br />a system locks
            <br />
            <span className="gradient-brand-text">before</span> it locks.
          </h1>

          <p className="mt-6 max-w-md text-[15.5px] leading-relaxed text-muted">
            WHYLOCK is an AI-powered security engine that analyzes, predicts,
            and prevents threats before they impact your system — and tells
            you exactly why, in language your whole team can act on.
          </p>

          <div className="mt-9 flex flex-wrap items-center gap-5">
            <a
              href="#scan"
              className="gradient-brand group inline-flex items-center gap-2 rounded-full px-6 py-3.5 text-[14.5px] font-semibold text-white shadow-[0_8px_30px_-8px_rgba(139,92,246,0.6)] transition-transform hover:scale-[1.03]"
            >
              Get Started Free
              <ArrowRight size={16} className="transition-transform group-hover:translate-x-0.5" />
            </a>
            <button
              onClick={onWatchDemo}
              className="inline-flex items-center gap-2 text-[14.5px] font-medium text-muted transition-colors hover:text-foreground"
            >
              <PlayCircle size={18} />
              Watch demo
            </button>
          </div>
        </div>

        {/* Center: orbiting capability cluster */}
        <HeroCubes riskScore={latestScore} />

        {/* Right: inline login */}
        <div className="lg:justify-self-end">
          <LoginCard />
        </div>
      </div>

      {/* Left→right flowing image ticker */}
      <ImageTicker />
    </section>
  );
}
