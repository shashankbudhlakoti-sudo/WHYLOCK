"use client";

/**
 * ImageTicker — cybersecurity images that continuously flow from LEFT to RIGHT.
 * Cards enter from the left edge and exit at the right edge in an infinite loop.
 * Uses pure CSS animation — no JS timer needed, no layout shift.
 *
 * The images use CSS-generated cybersecurity cards so the component works
 * immediately. Drop in real licensed photos by replacing the `CARDS` array's
 * `bg` and `img` values with your own URLs.
 */

const CARDS = [
  {
    bg: "linear-gradient(135deg,#0a1628 0%,#0d2244 50%,#061630 100%)",
    accent: "#00c8ff",
    label: "Threat Analysis",
    sub: "AI-powered detection",
    icon: "🛡",
  },
  {
    bg: "linear-gradient(135deg,#12042a 0%,#1e0840 50%,#0c0220 100%)",
    accent: "#a78bfa",
    label: "Network Security",
    sub: "Real-time monitoring",
    icon: "🔐",
  },
  {
    bg: "linear-gradient(135deg,#001820 0%,#002a38 50%,#001018 100%)",
    accent: "#2bffa0",
    label: "SSL Verified",
    sub: "Certificate validation",
    icon: "✓",
  },
  {
    bg: "linear-gradient(135deg,#1a0800 0%,#2e1000 50%,#120400 100%)",
    accent: "#ff8c4f",
    label: "Vulnerability Scan",
    sub: "CVE cross-reference",
    icon: "⚡",
  },
  {
    bg: "linear-gradient(135deg,#001a2e 0%,#003050 50%,#000e1e 100%)",
    accent: "#7dd3fc",
    label: "Data Protection",
    sub: "Encryption layer active",
    icon: "🔒",
  },
  {
    bg: "linear-gradient(135deg,#0d1a00 0%,#1a2c00 50%,#080e00 100%)",
    accent: "#86efac",
    label: "Compliance Ready",
    sub: "ISO 27001 · SOC 2",
    icon: "📋",
  },
  {
    bg: "linear-gradient(135deg,#1a0014 0%,#2e0022 50%,#100008 100%)",
    accent: "#f0abfc",
    label: "AI Fix Assistant",
    sub: "Remediation in seconds",
    icon: "🤖",
  },
];

/* Duplicate the array so the seamless loop never shows a gap */
const DOUBLED = [...CARDS, ...CARDS];

export function ImageTicker() {
  return (
    <div className="relative w-full overflow-hidden border-y border-hairline py-6" style={{ background: "rgba(0,0,0,0.02)" }}>
      {/* Left fade */}
      <div
        className="pointer-events-none absolute left-0 top-0 z-10 h-full w-24"
        style={{ background: "linear-gradient(to right, var(--void), transparent)" }}
      />
      {/* Right fade */}
      <div
        className="pointer-events-none absolute right-0 top-0 z-10 h-full w-24"
        style={{ background: "linear-gradient(to left, var(--void), transparent)" }}
      />

      {/* The ticker track — flows LEFT to RIGHT (translateX goes from -50% → 0%) */}
      <div
        className="flex gap-5"
        style={{
          width: "max-content",
          animation: "ticker-ltr 32s linear infinite",
        }}
      >
        {DOUBLED.map((card, i) => (
          <TickerCard key={i} card={card} />
        ))}
      </div>

      <style>{`
        @keyframes ticker-ltr {
          from { transform: translateX(-50%); }
          to   { transform: translateX(0%); }
        }
      `}</style>
    </div>
  );
}

function TickerCard({ card }: { card: (typeof CARDS)[0] }) {
  return (
    <div
      className="relative flex h-[130px] w-[220px] shrink-0 flex-col justify-between overflow-hidden rounded-2xl p-4"
      style={{
        background: card.bg,
        border: `1px solid ${card.accent}30`,
        boxShadow: `0 0 20px ${card.accent}12`,
      }}
    >
      {/* Corner bracket */}
      <div
        className="absolute right-3 top-3 h-4 w-4"
        style={{
          borderTop: `1.5px solid ${card.accent}`,
          borderRight: `1.5px solid ${card.accent}`,
          opacity: 0.6,
        }}
      />
      <div
        className="absolute bottom-3 left-3 h-4 w-4"
        style={{
          borderBottom: `1.5px solid ${card.accent}`,
          borderLeft: `1.5px solid ${card.accent}`,
          opacity: 0.6,
        }}
      />

      {/* Icon */}
      <div className="flex items-center gap-2">
        <span className="text-2xl leading-none">{card.icon}</span>
        <div
          className="h-px flex-1 opacity-20"
          style={{ background: card.accent }}
        />
      </div>

      {/* Text */}
      <div>
        <p
          className="font-mono text-[9.5px] uppercase tracking-[0.18em] opacity-60"
          style={{ color: card.accent }}
        >
          WhyLock
        </p>
        <p className="text-[14px] font-bold leading-tight text-white">
          {card.label}
        </p>
        <p className="mt-0.5 text-[10.5px]" style={{ color: `${card.accent}90` }}>
          {card.sub}
        </p>
      </div>

      {/* Glow dot */}
      <div
        className="absolute right-4 bottom-4 h-1.5 w-1.5 rounded-full"
        style={{
          background: card.accent,
          boxShadow: `0 0 6px ${card.accent}`,
          animation: "pulse-glow 2s ease-in-out infinite",
        }}
      />
    </div>
  );
}
