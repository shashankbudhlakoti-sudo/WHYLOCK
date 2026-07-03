"use client";
import { useRouter } from "next/navigation";
import { useEffect, useRef, useState, type FormEvent } from "react";
import { useAuth } from "@/lib/auth-context";
import {
  Mail, KeyRound, Eye, EyeOff, ArrowRight,
  User, ShieldAlert, ShieldCheck, Lock, UserPlus,
} from "lucide-react";

/* Brand purple — same as the "Sign in" button in the navbar */
const BRAND = "#7c3aed";
const BRAND2 = "#2563eb";
const BRAND_GLOW = "rgba(124,58,237,0.55)";

export function LoginCard() {
      const router = useRouter();
      const { login, isAuthenticated, username: authedUser, role, logout } = useAuth();

  const [mode, setMode] = useState<"user" | "admin">("user");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [mounted, setMounted] = useState(false);
  const canvasRef = useRef<HTMLCanvasElement>(null);

  // eslint-disable-next-line react-hooks/set-state-in-effect
  useEffect(() => setMounted(true), []);

  /* Beam animation — purple/blue on white background */
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    canvas.width = canvas.offsetWidth;
    canvas.height = canvas.offsetHeight;

    const cx = canvas.width / 2;
    const cy = canvas.height * 0.30;
    const NUM_BEAMS = 56;

    const beams = Array.from({ length: NUM_BEAMS }, (_, i) => ({
      angle: (i / NUM_BEAMS) * Math.PI * 2,
      speed: 0.5 + Math.random() * 1.2,
      length: 0.25 + Math.random() * 0.75,
      opacity: 0.08 + Math.random() * 0.18,
      offset: Math.random(),
      width: 0.3 + Math.random() * 0.9,
      hue: Math.random() > 0.5 ? BRAND : BRAND2,
    }));

    let animId: number;
    let t = 0;

    function draw() {
      if (!ctx || !canvas) return;
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      /* Subtle center glow */
      const grd = ctx.createRadialGradient(cx, cy, 0, cx, cy, 130);
      grd.addColorStop(0, "rgba(124,58,237,0.06)");
      grd.addColorStop(0.5, "rgba(37,99,235,0.03)");
      grd.addColorStop(1, "transparent");
      ctx.fillStyle = grd;
      ctx.fillRect(0, 0, canvas.width, canvas.height);

      beams.forEach((b) => {
        const phase = ((t * b.speed * 0.011 + b.offset) % 1);
        const len = canvas.height * 0.7 * b.length * phase;
        const x2 = cx + Math.cos(b.angle) * len;
        const y2 = cy + Math.sin(b.angle) * len;

        const g = ctx.createLinearGradient(cx, cy, x2, y2);
        g.addColorStop(0, `${b.hue}${Math.round(b.opacity * 255 * phase).toString(16).padStart(2,"0")}`);
        g.addColorStop(0.7, `${b.hue}${Math.round(b.opacity * 80 * phase).toString(16).padStart(2,"0")}`);
        g.addColorStop(1, "transparent");

        ctx.beginPath();
        ctx.moveTo(cx, cy);
        ctx.lineTo(x2, y2);
        ctx.strokeStyle = g;
        ctx.lineWidth = b.width;
        ctx.stroke();
      });

      /* Circuit grid traces */
      ctx.save();
      ctx.globalAlpha = 0.04;
      ctx.strokeStyle = BRAND;
      ctx.lineWidth = 0.5;
      [0.22, 0.44, 0.66, 0.78].forEach((f) => {
        ctx.beginPath();
        ctx.moveTo(0, canvas.height * f);
        ctx.lineTo(canvas.width, canvas.height * f);
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(canvas.width * f, 0);
        ctx.lineTo(canvas.width * f, canvas.height);
        ctx.stroke();
      });
      ctx.restore();

      t++;
      animId = requestAnimationFrame(draw);
    }

    draw();
    return () => cancelAnimationFrame(animId);
  }, [mounted]);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login(username.trim(), password);
      setUsername("");
      setPassword("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Authentication failed");
    } finally {
      setLoading(false);
    }
  }

  /* ── Signed-in state ─────────────────────── */
  if (mounted && isAuthenticated) {
    return (
      <GlassCard canvasRef={canvasRef}>
        <div className="text-center py-4">
          <div
            className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-2xl"
            style={{ background: `linear-gradient(135deg,${BRAND},${BRAND2})`, boxShadow: `0 0 28px ${BRAND_GLOW}` }}
          >
            <ShieldCheck size={28} className="text-white" />
          </div>
          <p className="font-mono text-[9px] uppercase tracking-[0.28em]" style={{ color: BRAND }}>
            Access Granted
          </p>
          <h3 className="mt-1 text-lg font-bold" style={{ color: "#1d1d1f" }}>{authedUser}</h3>
          {role && (
            <span
              className="mt-1 inline-block rounded-full px-3 py-0.5 font-mono text-[10px] uppercase tracking-wider"
              style={{ border: `1px solid ${BRAND}33`, background: `${BRAND}0d`, color: BRAND }}
            >
              {role}
            </span>
          )}
          <p className="mt-4 text-[12px]" style={{ color: "#6e6e76" }}>
            Scroll down to run a scan or view your dashboard.
          </p>
          <button
            onClick={logout}
            className="mt-6 w-full rounded-xl py-2.5 text-[13px] font-medium transition-colors"
            style={{ border: "1px solid rgba(0,0,0,0.1)", color: "#6e6e76" }}
          >
            Sign out
          </button>
        </div>
      </GlassCard>
    );
  }

  /* ── Sign-in form ────────────────────────── */
  return (
    <GlassCard canvasRef={canvasRef}>
      {/* Lock icon + title */}
      <div className="mb-5 flex flex-col items-center">
        <div className="relative mb-4 flex h-20 w-20 items-center justify-center">
          {/* Rotating conic ring — brand purple */}
          <div
            className="absolute inset-0 rounded-full"
            style={{
              background: `conic-gradient(from 0deg, ${BRAND}, ${BRAND2}, transparent 45%, transparent 55%, ${BRAND}, ${BRAND})`,
              animation: "spin-slow 4s linear infinite",
              mask: "radial-gradient(farthest-side, transparent calc(100% - 2.5px), #000 calc(100% - 2.5px))",
              WebkitMask: "radial-gradient(farthest-side, transparent calc(100% - 2.5px), #000 calc(100% - 2.5px))",
            }}
          />
          {/* Inner ring */}
          <div
            className="absolute h-[54px] w-[54px] rounded-full"
            style={{ border: `1px solid ${BRAND}44`, boxShadow: `0 0 14px ${BRAND}33, inset 0 0 14px ${BRAND}11` }}
          />
          <Lock size={26} style={{ color: BRAND, filter: `drop-shadow(0 0 8px ${BRAND_GLOW})` }} strokeWidth={1.8} />
        </div>

        <h2
          className="text-center font-mono text-[14.5px] font-bold uppercase tracking-[0.1em]"
          style={{ color: "#1d1d1f", textShadow: `0 0 24px ${BRAND}33` }}
        >
          WhyLock Password
          <br />Protection System
        </h2>
        <div
          className="mt-1.5 h-px w-40 opacity-50"
          style={{ background: `linear-gradient(90deg, transparent, ${BRAND}, transparent)` }}
        />
      </div>

      {/* User / Admin toggle */}
      <div
        className="mb-4 flex gap-1 rounded-xl p-1"
        style={{ background: `${BRAND}0a`, border: `1px solid ${BRAND}22` }}
      >
        {(["user", "admin"] as const).map((m) => {
          const Icon = m === "user" ? User : ShieldAlert;
          const active = mode === m;
          return (
            <button
              key={m}
              type="button"
              onClick={() => setMode(m)}
              className="flex flex-1 items-center justify-center gap-1.5 rounded-lg py-2.5 text-[12px] font-semibold capitalize transition-all duration-200"
              style={
                active
                  ? {
                      background: `linear-gradient(135deg, ${BRAND}, ${BRAND2})`,
                      color: "#fff",
                      boxShadow: `0 4px 14px ${BRAND_GLOW}`,
                      border: `1px solid ${BRAND}66`,
                    }
                  : { color: "#6e6e76" }
              }
            >
              <Icon size={13} />
              {m === "user" ? "User Login" : "Admin Login"}
            </button>
          );
        })}
      </div>

      {/* Fields */}
      <form onSubmit={handleSubmit} className="space-y-3">
        <div
          className="flex items-center gap-3 rounded-xl px-4 py-3 transition-all focus-within:shadow-[0_0_0_2px_rgba(124,58,237,0.35)]"
          style={{ background: "rgba(0,0,0,0.04)", border: "1px solid rgba(0,0,0,0.1)" }}
        >
          <Mail size={15} style={{ color: BRAND }} className="shrink-0" />
          <input
            type="text"
            autoComplete="off"
            placeholder={mode === "admin" ? "Admin username" : "Username"}
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="w-full bg-transparent text-[13px] outline-none placeholder:text-gray-400"
            style={{ color: "#1d1d1f" }}
            required
          />
        </div>

        <div
          className="flex items-center gap-3 rounded-xl px-4 py-3 transition-all focus-within:shadow-[0_0_0_2px_rgba(124,58,237,0.35)]"
          style={{ background: "rgba(0,0,0,0.04)", border: "1px solid rgba(0,0,0,0.1)" }}
        >
          <KeyRound size={15} style={{ color: BRAND }} className="shrink-0" />
          <input
            type={showPw ? "text" : "password"}
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full bg-transparent text-[13px] outline-none placeholder:text-gray-400"
            style={{ color: "#1d1d1f" }}
            required
          />
          <button
            type="button"
            onClick={() => setShowPw((s) => !s)}
            style={{ color: BRAND }}
            className="shrink-0 opacity-60 hover:opacity-100 transition-opacity"
          >
            {showPw ? <EyeOff size={14} /> : <Eye size={14} />}
          </button>
        </div>

        <div className="flex items-center justify-between text-[11px]">
          <label className="flex cursor-pointer items-center gap-2" style={{ color: "#6e6e76" }}>
            <input type="checkbox" className="h-3 w-3 cursor-pointer rounded" style={{ accentColor: BRAND }} />
            Remember Me
          </label>
          <a href="#" className="font-medium" style={{ color: BRAND }}>
            Forgot Password?
          </a>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="mt-1 flex w-full items-center justify-center gap-2 rounded-xl py-3.5 text-[13.5px] font-bold uppercase tracking-[0.1em] text-white transition-all disabled:opacity-50"
          style={{
            background: `linear-gradient(135deg, rgba(60,20,120,0.9), rgba(20,50,130,0.85))`,
            border: `1px solid ${BRAND}55`,
            boxShadow: `0 0 24px ${BRAND}33, inset 0 1px 0 rgba(255,255,255,0.1)`,
          }}
        >
          {loading ? (
            <>
              <span
                className="h-3.5 w-3.5 rounded-full border-2 border-white/20 border-t-white"
                style={{ animation: "spin-slow 0.8s linear infinite" }}
              />
              Authenticating…
            </>
          ) : (
            <>
              <Lock size={14} style={{ color: "#a78bfa" }} />
              Login
              <ArrowRight size={14} className="ml-auto" />
            </>
          )}
        </button>

        {error && (
          <p
            className="rounded-xl py-2 text-center text-[11.5px] text-red-500"
            style={{ background: "rgba(239,68,68,0.06)", border: "1px solid rgba(239,68,68,0.15)" }}
          >
            {error}
          </p>
        )}
      </form>

      <div className="my-4 flex items-center gap-2">
        <div className="h-px flex-1" style={{ background: "rgba(0,0,0,0.08)" }} />
        <span className="text-[10px] uppercase tracking-wider" style={{ color: "#a1a1a8" }}>OR</span>
        <div className="h-px flex-1" style={{ background: "rgba(0,0,0,0.08)" }} />
      </div>

     <button
       type="button"
       onClick={() => router.push("/register")}
       className="flex w-full items-center justify-center gap-2 rounded-xl py-3 text-[12.5px] font-medium transition-all"
       style={{ background: "rgba(0,0,0,0.03)", border: "1px solid rgba(0,0,0,0.08)", color: "#6e6e76" }}
     >
       <UserPlus size={13} style={{ color: BRAND }} />
       Register with WhyLock
     </button>

      <div className="mt-5 flex items-center justify-center gap-5">
        {["AES-256", "TLS 1.3", "Zero-Trust"].map((tag) => (
          <div key={tag} className="flex items-center gap-1 font-mono text-[9px]" style={{ color: "#a1a1a8" }}>
            <div className="h-1 w-1 rounded-full" style={{ background: BRAND, opacity: 0.5 }} />
            {tag}
          </div>
        ))}
      </div>
    </GlassCard>
  );
}

/* ── White glass card shell with HUD SVG border ─── */
function GlassCard({
  children,
  canvasRef,
}: {
  children: React.ReactNode;
  canvasRef: React.RefObject<HTMLCanvasElement | null>;
}) {
  return (
    <div className="relative w-full max-w-[370px]">
      {/* Ambient glow behind card */}
      <div
        className="pointer-events-none absolute -inset-6 rounded-[40px] opacity-30 blur-3xl"
        style={{ background: `radial-gradient(ellipse at 50% 30%, ${BRAND}44, transparent 65%)` }}
      />

      <div
        className="relative overflow-hidden rounded-2xl"
        style={{
          background: "#ffffff",
          boxShadow: "0 24px 60px -16px rgba(0,0,0,0.12), 0 4px 16px -4px rgba(0,0,0,0.06)",
        }}
      >
        {/* Beam canvas */}
        <canvas ref={canvasRef} className="absolute inset-0 h-full w-full" style={{ pointerEvents: "none" }} />

        {/* HUD octagonal SVG border — same as before, brand purple */}
        <svg
          className="pointer-events-none absolute inset-0 h-full w-full"
          viewBox="0 0 370 600"
          fill="none"
          preserveAspectRatio="none"
        >
          <defs>
            <filter id="pglow">
              <feGaussianBlur stdDeviation="2.5" result="blur" />
              <feMerge><feMergeNode in="blur" /><feMergeNode in="SourceGraphic" /></feMerge>
            </filter>
          </defs>
          <path
            d="M28,4 L342,4 L366,28 L366,572 L342,596 L28,596 L4,572 L4,28 Z"
            stroke={BRAND}
            strokeWidth="1.5"
            strokeOpacity="0.55"
            filter="url(#pglow)"
          />
          <path
            d="M36,12 L334,12 L358,36 L358,564 L334,588 L36,588 L12,564 L12,36 Z"
            stroke={BRAND}
            strokeWidth="0.5"
            strokeOpacity="0.18"
          />
          {/* Corner connectors */}
          <circle cx="28" cy="4" r="5" fill={BRAND} fillOpacity="0.85" filter="url(#pglow)" />
          <line x1="28" y1="4" x2="62" y2="4" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <line x1="28" y1="4" x2="28" y2="38" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <circle cx="342" cy="4" r="5" fill={BRAND} fillOpacity="0.85" filter="url(#pglow)" />
          <line x1="342" y1="4" x2="308" y2="4" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <line x1="342" y1="4" x2="342" y2="38" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <circle cx="28" cy="596" r="5" fill={BRAND} fillOpacity="0.85" filter="url(#pglow)" />
          <line x1="28" y1="596" x2="62" y2="596" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <line x1="28" y1="596" x2="28" y2="562" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <circle cx="342" cy="596" r="5" fill={BRAND} fillOpacity="0.85" filter="url(#pglow)" />
          <line x1="342" y1="596" x2="308" y2="596" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <line x1="342" y1="596" x2="342" y2="562" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          {/* Mid-side ticks */}
          <line x1="4" y1="295" x2="18" y2="295" stroke={BRAND} strokeWidth="1.5" strokeOpacity="0.4" />
          <line x1="352" y1="295" x2="366" y2="295" stroke={BRAND} strokeWidth="1.5" strokeOpacity="0.4" />
        </svg>

        {/* Shimmer top-edge */}
        <div
          className="pointer-events-none absolute left-[12%] top-0 h-px w-[76%] opacity-50"
          style={{ background: `linear-gradient(90deg, transparent, ${BRAND}55, transparent)` }}
        />

        <div className="relative z-10 px-7 pb-8 pt-8">{children}</div>
      </div>
    </div>
  );
}
