"use client";

import { useEffect, useRef, useState } from "react";
import { Radar, ListChecks, FileDown, Gauge } from "lucide-react";
import type { LucideIcon } from "lucide-react";

interface Slide {
  icon: LucideIcon;
  eyebrow: string;
  title: string;
  body: string;
  accent: string;
  visual: "radar" | "findings" | "report" | "gauge";
}

const SLIDES: Slide[] = [
  {
    icon: Radar,
    eyebrow: "ScanOrchestrationService",
    title: "A live sweep of the entire surface",
    body: "Headers, TLS, subdomains, and exposed endpoints are checked in parallel, the moment you submit a URL — not on a nightly cron job.",
    accent: "#7c3aed",
    visual: "radar",
  },
  {
    icon: ListChecks,
    eyebrow: "AiService",
    title: "Findings ranked the second they land",
    body: "Every result is scored and ordered by what actually matters first, with plain-language reasoning behind each ranking.",
    accent: "#2563eb",
    visual: "findings",
  },
  {
    icon: FileDown,
    eyebrow: "PdfReportService",
    title: "One click, a client-ready report",
    body: "Every scan can be exported as a clean PDF in seconds — built for the compliance review, not just the engineer.",
    accent: "#0891b2",
    visual: "report",
  },
  {
    icon: Gauge,
    eyebrow: "MonitoringService",
    title: "A risk score that's never stale",
    body: "Your score recalculates continuously as conditions change, so the number on screen is always the current truth.",
    accent: "#16a34a",
    visual: "gauge",
  },
];

const DURATION = 5000;

export function LiveShowcase() {
  const [active, setActive] = useState(0);
  const [progress, setProgress] = useState(0);
  const rafRef = useRef<number | null>(null);
  const startRef = useRef<number>(0);

  useEffect(() => {
    function tick(now: number) {
      if (!startRef.current) startRef.current = now;
      const elapsed = now - startRef.current;
      const pct = Math.min(100, (elapsed / DURATION) * 100);
      setProgress(pct);
      if (pct >= 100) {
        startRef.current = now;
        setActive((a) => (a + 1) % SLIDES.length);
      }
      rafRef.current = requestAnimationFrame(tick);
    }
    rafRef.current = requestAnimationFrame(tick);
    return () => {
      if (rafRef.current) cancelAnimationFrame(rafRef.current);
    };
  }, [active]);

  function select(i: number) {
    startRef.current = 0;
    setProgress(0);
    setActive(i);
  }

  const slide = SLIDES[active];

  return (
    <section className="bg-[#0a0a0c] px-6 py-20 md:px-10 md:py-28">
      <div className="mx-auto max-w-6xl">
        <div className="mb-12 max-w-xl">
          <p className="mb-3 font-mono text-[11px] uppercase tracking-[0.2em] text-[#7dd3fc]">
            Live, not static
          </p>
          <h2 className="text-[30px] font-bold leading-tight tracking-tight text-white md:text-[36px]">
            See what&apos;s actually running, in real time
          </h2>
        </div>

        <div className="grid grid-cols-1 gap-12 lg:grid-cols-[0.9fr_1.1fr] lg:items-center">
          {/* visual stage */}
          <div className="order-2 lg:order-1">
            <Visual kind={slide.visual} accent={slide.accent} />
          </div>

          {/* tabs */}
          <div className="order-1 flex flex-col gap-2 lg:order-2">
            {SLIDES.map((s, i) => {
              const Icon = s.icon;
              const isActive = i === active;
              return (
                <button
                  key={s.title}
                  onClick={() => select(i)}
                  className={`group relative overflow-hidden rounded-2xl border px-5 py-5 text-left transition-colors ${
                    isActive
                      ? "border-white/15 bg-white/[0.04]"
                      : "border-white/[0.06] hover:border-white/12"
                  }`}
                >
                  {isActive && (
                    <div className="absolute left-0 top-0 h-[2px] bg-white/15" style={{ width: "100%" }}>
                      <div
                        className="h-full"
                        style={{ width: `${progress}%`, background: s.accent, transition: "width 80ms linear" }}
                      />
                    </div>
                  )}
                  <div className="flex items-start gap-4">
                    <div
                      className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl"
                      style={{ background: `${s.accent}22`, color: s.accent }}
                    >
                      <Icon size={18} />
                    </div>
                    <div>
                      <p
                        className="mb-1 font-mono text-[10px] uppercase tracking-[0.14em]"
                        style={{ color: isActive ? s.accent : "#8b8fa8" }}
                      >
                        {s.eyebrow}
                      </p>
                      <h3 className="mb-1 text-[16px] font-semibold text-white">{s.title}</h3>
                      {isActive && (
                        <p className="text-[13px] leading-relaxed text-white/55">{s.body}</p>
                      )}
                    </div>
                  </div>
                </button>
              );
            })}
          </div>
        </div>
      </div>
    </section>
  );
}

function Visual({ kind, accent }: { kind: Slide["visual"]; accent: string }) {
  return (
    <div className="relative flex h-[320px] w-full items-center justify-center overflow-hidden rounded-3xl border border-white/[0.08] bg-white/[0.02]">
      <div
        className="pointer-events-none absolute h-[260px] w-[260px] rounded-full opacity-30 blur-3xl"
        style={{ background: `radial-gradient(circle, ${accent}, transparent 70%)` }}
      />
      {kind === "radar" && <RadarVisual accent={accent} />}
      {kind === "findings" && <FindingsVisual accent={accent} />}
      {kind === "report" && <ReportVisual accent={accent} />}
      {kind === "gauge" && <GaugeVisual accent={accent} />}
    </div>
  );
}

function RadarVisual({ accent }: { accent: string }) {
  return (
    <div className="relative h-44 w-44">
      {[1, 0.7, 0.4].map((s, i) => (
        <div
          key={i}
          className="absolute inset-0 rounded-full border"
          style={{ borderColor: `${accent}33`, transform: `scale(${s})` }}
        />
      ))}
      <div
        className="absolute inset-0 origin-center rounded-full"
        style={{
          background: `conic-gradient(from 0deg, ${accent}aa, transparent 35%)`,
          animation: "spin-slow 3s linear infinite",
        }}
      />
      <div
        className="absolute left-1/2 top-1/2 h-3 w-3 -translate-x-1/2 -translate-y-1/2 rounded-full"
        style={{ background: accent, boxShadow: `0 0 16px ${accent}` }}
      />
    </div>
  );
}

function FindingsVisual({ accent }: { accent: string }) {
  const widths = ["88%", "64%", "76%", "42%"];
  return (
    <div className="flex w-56 flex-col gap-3">
      {widths.map((w, i) => (
        <div key={i} className="flex items-center gap-3">
          <div className="h-2.5 w-2.5 shrink-0 rounded-full" style={{ background: accent, opacity: 1 - i * 0.18 }} />
          <div
            className="h-2.5 rounded-full bg-white/10"
            style={{
              width: w,
              animation: `findings-grow 1.4s ease-out ${i * 0.18}s both`,
            }}
          />
        </div>
      ))}
      <style>{`@keyframes findings-grow { from { transform: scaleX(0); transform-origin: left; } to { transform: scaleX(1); transform-origin: left; } }`}</style>
    </div>
  );
}

function ReportVisual({ accent }: { accent: string }) {
  return (
    <div className="flex h-40 w-32 flex-col gap-2 rounded-lg border border-white/15 bg-white/[0.03] p-4">
      <div className="h-2 w-3/4 rounded-full" style={{ background: accent }} />
      {[1, 2, 3, 4, 5].map((i) => (
        <div
          key={i}
          className="h-1.5 rounded-full bg-white/15"
          style={{ animation: `findings-grow 1s ease-out ${i * 0.12}s both`, transformOrigin: "left" }}
        />
      ))}
      <div
        className="mt-auto flex items-center justify-center rounded-md py-1.5 text-[9px] font-semibold text-white"
        style={{ background: accent }}
      >
        PDF
      </div>
    </div>
  );
}

function GaugeVisual({ accent }: { accent: string }) {
  return (
    <div className="relative flex h-32 w-32 items-center justify-center">
      <svg viewBox="0 0 100 100" className="h-32 w-32 -rotate-90">
        <circle cx="50" cy="50" r="44" fill="none" stroke="#ffffff1a" strokeWidth="8" />
        <circle
          cx="50"
          cy="50"
          r="44"
          fill="none"
          stroke={accent}
          strokeWidth="8"
          strokeLinecap="round"
          strokeDasharray={2 * Math.PI * 44}
          strokeDashoffset={2 * Math.PI * 44 * 0.13}
          style={{ animation: "gauge-sweep 2.5s ease-in-out infinite alternate" }}
        />
      </svg>
      <span className="absolute font-mono text-2xl font-bold text-white">87</span>
      <style>{`@keyframes gauge-sweep { from { stroke-dashoffset: ${2 * Math.PI * 44 * 0.05}; } to { stroke-dashoffset: ${2 * Math.PI * 44 * 0.22}; } }`}</style>
    </div>
  );
}
