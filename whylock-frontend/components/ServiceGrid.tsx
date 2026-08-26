"use client";

import { SERVICES } from "@/lib/services";

export function ServiceGrid() {
  return (
    <section className="border-t border-hairline bg-surface/30 px-6 py-16 md:px-10 md:py-20">
      <div className="mx-auto max-w-6xl">
        <div className="mb-12 flex flex-col items-start justify-between gap-6 lg:flex-row lg:items-end">
          <div>
            <div className="mb-3 inline-flex items-center gap-2 rounded-full border border-hairline bg-surface px-3 py-1 font-mono text-[10.5px] uppercase tracking-[0.16em] text-cyan">
              WHYLOCK Advantage
            </div>
            <h2 className="max-w-xl text-[30px] font-bold leading-tight tracking-tight text-foreground md:text-[36px]">
              Built for the future of cyber security
            </h2>
          </div>
          <p className="max-w-sm text-[14px] leading-relaxed text-muted">
            Seven coordinated services, each doing one job well — together
            they give you unmatched protection and complete visibility.
          </p>
        </div>

        <div className="grid grid-cols-1 gap-px overflow-hidden rounded-3xl border border-hairline bg-hairline sm:grid-cols-2 lg:grid-cols-4">
          {SERVICES.map(({ icon: Icon, service, title, desc }) => (
            <div
              key={service}
              className="group flex flex-col gap-4 bg-surface p-6 transition-colors hover:bg-surface-raised"
            >
              <div className="flex h-11 w-11 items-center justify-center rounded-xl border border-hairline-strong bg-surface-raised">
                <Icon size={20} className="text-accent" strokeWidth={1.8} />
              </div>
              <div>
                <p className="mb-1 font-mono text-[10px] uppercase tracking-[0.14em] text-muted-dim">
                  {service}
                </p>
                <h3 className="mb-1.5 text-[15px] font-semibold text-foreground">{title}</h3>
                <p className="text-[13px] leading-relaxed text-muted">{desc}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
