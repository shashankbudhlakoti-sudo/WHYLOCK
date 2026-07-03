"use client";

interface LockLogoProps {
  size?: number;
  state?: "idle" | "scanning" | "locked" | "alert";
  className?: string;
}

/**
 * The WHYLOCK signature mark: a padlock rendered as two independent
 * SVG groups (body + shackle) so the shackle can animate open while
 * scanning and click shut when a scan resolves to SAFE.
 */
export function LockLogo({ size = 28, state = "idle", className = "" }: LockLogoProps) {
  const shackleStateClass =
    state === "scanning"
      ? "origin-[28px_24px] animate-[shackle-open_0.5s_ease-out_forwards]"
      : state === "alert"
      ? "origin-[28px_24px]"
      : "origin-[28px_24px] animate-[shackle-close_0.4s_ease-out_forwards]";

  const bodyStroke =
    state === "alert" ? "var(--danger)" : state === "locked" ? "var(--lock-green)" : "var(--accent)";

  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 56 56"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
      aria-hidden="true"
    >
      <g className={shackleStateClass}>
        <path
          d="M16 24V18C16 11.373 21.373 6 28 6C34.627 6 40 11.373 40 18V24"
          stroke={bodyStroke}
          strokeWidth="4"
          strokeLinecap="round"
          fill="none"
          style={{ transition: "stroke 0.3s ease" }}
        />
      </g>
      <rect
        x="10"
        y="24"
        width="36"
        height="26"
        rx="6"
        fill="var(--surface-raised)"
        stroke={bodyStroke}
        strokeWidth="2"
        style={{ transition: "stroke 0.3s ease" }}
      />
      <circle cx="28" cy="35" r="3.2" fill={bodyStroke} style={{ transition: "fill 0.3s ease" }} />
      <rect x="26.6" y="37" width="2.8" height="7" rx="1.4" fill={bodyStroke} style={{ transition: "fill 0.3s ease" }} />
    </svg>
  );
}
