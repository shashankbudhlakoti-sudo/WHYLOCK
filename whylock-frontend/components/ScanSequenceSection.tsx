"use client";

import { useInView } from "@/lib/use-in-view";
import { Target, ScanLine, ListChecks, ShieldCheck } from "lucide-react";
import type { LucideIcon } from "lucide-react";

interface Step {
  icon: LucideIcon;
  eyebrow: string;
  title: string;
  body: string;
  accent: string;
}

const STEPS: Step[] = [
  {
    icon: Target,
    eyebrow: "01 · TARGET",
    title: "Point WHYLOCK at anything with a URL",
    body: "Drop in a domain, an internal app, or a client's site. No agents to install, no config files — the engine reads the live surface exactly as an attacker would see it.",
    accent: "var(--accent)",
  },
  {
    icon: ScanLine,
    eyebrow: "02 · SCAN",
    title: "The shackle lifts while the engine works",
    body: "TLS posture, headers, exposed endpoints, and known CVEs get cross-referenced against your stack in seconds — orchestrated across our scanning and OSINT services in parallel.",
    accent: "var(--cyan)",
  },
  {
    icon: ListChecks,
    eyebrow: "03 · FINDINGS",
    title: "Every issue, ranked by what actually matters",
    body: "No noisy dumps. Findings are scored, mapped to CVEs where they exist, and paired with copy-paste fix code — so triage takes minutes, not a meeting.",
    accent: "var(--warn)",
  },
  {
    icon: ShieldCheck,
    eyebrow: "04 · DECISION",
    title: "The shackle clicks shut on SAFE",
    body: "One score. One verdict. CRITICAL means block the release; SAFE means ship it. WHYLOCK is built to end the debate, not add another dashboard to check.",
    accent: "var(--lock-green)",
  },
];

function StepRow({ step, index }: { step: Step; index: number }) {
  const { ref, inView } = useInView<HTMLDivElement>(0.25);
  const fromLeft = index % 2 === 0;
  const Icon = step.icon;

  return (
    <div
      ref={ref}
      className={`flex flex-col items-start gap-5 border-b border-hairline py-10 transition-all duration-700 ease-out last:border-none sm:flex-row sm:items-center sm:gap-8 ${
        inView
          ? "translate-x-0 opacity-100"
          : fromLeft
          ? "-translate-x-10 opacity-0"
          : "translate-x-10 opacity-0"
      }`}
    >
      <div
        className="flex h-14 w-14 flex-shrink-0 items-center justify-center rounded-xl border"
        style={{
          borderColor: `${step.accent}44`,
          background: `${step.accent}14`,
          color: step.accent,
        }}
      >
        <Icon size={22} />
      </div>
      <div className="flex-1">
        <div
          className="mb-2 font-mono text-[11px] tracking-[0.2em]"
          style={{ color: step.accent }}
        >
          {step.eyebrow}
        </div>
        <h3 className="mb-2 text-xl font-semibold text-foreground sm:text-2xl">
          {step.title}
        </h3>
        <p className="max-w-2xl text-[15px] leading-relaxed text-muted">
          {step.body}
        </p>
      </div>
    </div>
  );
}

export function ScanSequenceSection() {
  return (
    <section className="mb-10 rounded-2xl border border-hairline bg-surface/60 px-6 py-2 sm:px-10">
      <div className="border-b border-hairline py-6">
        <div className="font-mono text-[11px] uppercase tracking-[0.2em] text-muted">
          How a scan unfolds
        </div>
      </div>
      {STEPS.map((step, i) => (
        <StepRow key={step.title} step={step} index={i} />
      ))}
    </section>
  );
}
