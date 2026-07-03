"use client";

import { useEffect, useRef, useState } from "react";
import {
  Activity,
  BrainCircuit,
  ShieldCheck,
  Workflow,
  FileText,
  Wrench,
  Fingerprint,
  type LucideIcon,
} from "lucide-react";

interface ServicePanel {
  icon: LucideIcon;
  eyebrow: string;
  title: string;
  description: string;
}

const SERVICES: ServicePanel[] = [
  {
    icon: Activity,
    eyebrow: "MonitoringService",
    title: "Watch every endpoint, continuously",
    description:
      "WHYLOCK keeps a live pulse on your attack surface — uptime, certificate health, and config drift, tracked the moment something changes.",
  },
  {
    icon: BrainCircuit,
    eyebrow: "AiService",
    title: "Reasoning, not just regex",
    description:
      "An LLM reads each finding in context and explains why it matters, in plain language your team can act on without a security degree.",
  },
  {
    icon: Fingerprint,
    eyebrow: "SslAnalysisService",
    title: "Every cipher, every cert, verified",
    description:
      "TLS handshake, chain of trust, expiry windows — interrogated down to the cipher suite so weak crypto never hides in plain sight.",
  },
  {
    icon: Workflow,
    eyebrow: "ScanOrchestrationService",
    title: "One scan, every layer",
    description:
      "Headers, subdomains, ports, and breach exposure run in coordinated passes — orchestrated, not bolted together.",
  },
  {
    icon: FileText,
    eyebrow: "PdfReportService",
    title: "Evidence your auditors accept",
    description:
      "Every scan compiles into a clean, exportable report — built for compliance reviews, not just your own dashboard.",
  },
  {
    icon: Wrench,
    eyebrow: "AiFixAssistantService",
    title: "The fix, already written",
    description:
      "Findings ship with working remediation code, generated for your exact stack — so closing the gap takes minutes, not tickets.",
  },
  {
    icon: ShieldCheck,
    eyebrow: "TechnologyDetectionService",
    title: "Know what you're actually running",
    description:
      "Full fingerprint of frameworks, libraries, and CMS versions in play — so every CVE check starts from the truth.",
  },
];

function ServicePanelRow({ service, index }: { service: ServicePanel; index: number }) {
  const ref = useRef<HTMLDivElement>(null);
  const [visible, setVisible] = useState(false);
  const Icon = service.icon;
  const fromLeft = index % 2 === 0;

  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) setVisible(true);
      },
      { threshold: 0.3 }
    );
    observer.observe(el);
    return () => observer.disconnect();
  }, []);

  return (
    <div
      ref={ref}
      className={`flex items-center gap-6 border-b border-hairline py-10 transition-all duration-700 ease-out md:gap-10 md:py-14 ${
        visible
          ? "translate-x-0 opacity-100"
          : `opacity-0 ${fromLeft ? "-translate-x-10" : "translate-x-10"}`
      }`}
    >
      <div className="flex h-14 w-14 flex-shrink-0 items-center justify-center rounded-2xl border border-hairline-strong bg-surface-raised md:h-16 md:w-16">
        <Icon size={26} className="text-accent" strokeWidth={1.6} />
      </div>
      <div>
        <div className="mb-1.5 font-mono text-[11px] uppercase tracking-[0.18em] text-cyan">
          {service.eyebrow}
        </div>
        <h3 className="mb-1.5 text-xl font-semibold text-foreground md:text-2xl">
          {service.title}
        </h3>
        <p className="max-w-xl text-[14px] leading-relaxed text-muted">
          {service.description}
        </p>
      </div>
    </div>
  );
}

export function ServiceShowcase() {
  return (
    <section className="border-y border-hairline bg-surface/40 px-6 py-16 md:px-10 md:py-24">
      <div className="mx-auto max-w-4xl">
        <div className="mb-12 md:mb-16">
          <div className="mb-3 font-mono text-[11px] uppercase tracking-[0.2em] text-accent">
            What&apos;s running under the hood
          </div>
          <h2 className="max-w-2xl text-3xl font-bold leading-tight text-foreground md:text-4xl">
            Seven engines, one decision: fix this first.
          </h2>
        </div>
        {SERVICES.map((service, i) => (
          <ServicePanelRow key={service.eyebrow} service={service} index={i} />
        ))}
      </div>
    </section>
  );
}
