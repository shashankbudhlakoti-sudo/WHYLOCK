"use client";

import type { RiskLevel } from "@/types";

interface RiskDialProps {
  score: number;
  risk: RiskLevel;
  size?: number;
}

const RISK_COLOR_VAR: Record<string, string> = {
  CRITICAL: "var(--danger)",
  HIGH: "var(--orange)",
  MEDIUM: "var(--warn)",
  LOW: "var(--lock-green)",
  SAFE: "var(--cyan)",
  ERROR: "var(--muted)",
  UNKNOWN: "var(--muted)",
};

export function RiskDial({ score, risk, size = 120 }: RiskDialProps) {
  const radius = (size - 16) / 2;
  const circumference = 2 * Math.PI * radius;
  const clamped = Math.max(0, Math.min(100, score));
  const offset = circumference - (clamped / 100) * circumference;
  const color = RISK_COLOR_VAR[risk] || "var(--muted)";

  return (
    <div className="relative" style={{ width: size, height: size }}>
      <svg width={size} height={size} className="-rotate-90">
        {/* tick marks, like a dial */}
        {Array.from({ length: 40 }).map((_, i) => {
          const angle = (i / 40) * 360;
          const isMajor = i % 5 === 0;
          return (
            <line
              key={i}
              x1={size / 2}
              y1={4}
              x2={size / 2}
              y2={isMajor ? 9 : 7}
              stroke="var(--hairline-strong)"
              strokeWidth={isMajor ? 1.5 : 1}
              transform={`rotate(${angle} ${size / 2} ${size / 2})`}
            />
          );
        })}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke="var(--hairline)"
          strokeWidth={5}
        />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke={color}
          strokeWidth={5}
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          style={{ transition: "stroke-dashoffset 0.8s cubic-bezier(0.16,1,0.3,1)" }}
        />
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center">
        <span
          className="font-mono text-2xl font-bold leading-none"
          style={{ color }}
        >
          {Math.round(clamped)}
        </span>
        <span className="mt-1 font-mono text-[9px] uppercase tracking-widest text-muted-dim">
          / 100
        </span>
      </div>
    </div>
  );
}
