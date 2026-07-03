"use client";

import { Navbar } from "@/components/Navbar";
import { RegisterCard } from "@/components/RegisterCard";

export default function RegisterPage() {
  return (
    <div className="flex min-h-screen flex-col">
      <Navbar />

      <section className="relative flex flex-1 items-center justify-center overflow-hidden px-6 py-16 md:px-10 md:py-20">
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

        <div className="relative">
          <RegisterCard />
        </div>
      </section>

      <footer className="border-t border-hairline px-6 py-6 text-center font-mono text-[11px] tracking-wide text-muted-dim">
        WHYLOCK · the decision engine for what to fix first
      </footer>
    </div>
  );
}