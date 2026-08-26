"use client";

import { useEffect, useRef, useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth-context";
import {
  Mail, KeyRound, Eye, EyeOff, ArrowRight,
  User, UserPlus2, Lock, LogIn,
} from "lucide-react";

/* Brand purple — identical palette to LoginCard, kept in lockstep on purpose */
const BRAND = "#7c3aed";
const BRAND2 = "#2563eb";
const BRAND_GLOW = "rgba(124,58,237,0.55)";

export function RegisterCard() {
  const router = useRouter();
  const { register } = useAuth();

  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const canvasRef = useRef<HTMLCanvasElement>(null);

  /* Same beam animation as LoginCard — purple/blue on white background */
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
  }, []);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await register(username.trim(), email.trim(), password);
      // Registration + auto-login succeeded — drop the user back on the
      // main app, already signed in, ready to run a scan.
      router.push("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Registration failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <GlassCard canvasRef={canvasRef}>
      {/* Lock icon + title */}
      <div className="mb-5 flex flex-col items-center">
        <div className="relative mb-4 flex h-20 w-20 items-center justify-center">
          <div
            className="absolute inset-0 rounded-full"
            style={{
              background: `conic-gradient(from 0deg, ${BRAND}, ${BRAND2}, transparent 45%, transparent 55%, ${BRAND}, ${BRAND})`,
              animation: "spin-slow 4s linear infinite",
              mask: "radial-gradient(farthest-side, transparent calc(100% - 2.5px), #000 calc(100% - 2.5px))",
              WebkitMask: "radial-gradient(farthest-side, transparent calc(100% - 2.5px), #000 calc(100% - 2.5px))",
            }}
          />
          <div
            className="absolute h-[54px] w-[54px] rounded-full"
            style={{ border: `1px solid ${BRAND}44`, boxShadow: `0 0 14px ${BRAND}33, inset 0 0 14px ${BRAND}11` }}
          />
          <UserPlus2 size={26} style={{ color: BRAND, filter: `drop-shadow(0 0 8px ${BRAND_GLOW})` }} strokeWidth={1.8} />
        </div>

        <h2
          className="text-center font-mono text-[14.5px] font-bold uppercase tracking-[0.1em]"
          style={{ color: "#1d1d1f", textShadow: `0 0 24px ${BRAND}33` }}
        >
          Register With
          <br />WhyLock
        </h2>
        <div
          className="mt-1.5 h-px w-40 opacity-50"
          style={{ background: `linear-gradient(90deg, transparent, ${BRAND}, transparent)` }}
        />
      </div>

      {/* Fields */}
      <form onSubmit={handleSubmit} className="space-y-3">
        <div
          className="flex items-center gap-3 rounded-xl px-4 py-3 transition-all focus-within:shadow-[0_0_0_2px_rgba(124,58,237,0.35)]"
          style={{ background: "rgba(0,0,0,0.04)", border: "1px solid rgba(0,0,0,0.1)" }}
        >
          <User size={15} style={{ color: BRAND }} className="shrink-0" />
          <input
            type="text"
            autoComplete="off"
            placeholder="Username"
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
          <Mail size={15} style={{ color: BRAND }} className="shrink-0" />
          <input
            type="email"
            autoComplete="off"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
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
            minLength={8}
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
              Creating account…
            </>
          ) : (
            <>
              <Lock size={14} style={{ color: "#a78bfa" }} />
              Create Account
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
        onClick={() => router.push("/")}
        className="flex w-full items-center justify-center gap-2 rounded-xl py-3 text-[12.5px] font-medium transition-all"
        style={{ background: "rgba(0,0,0,0.03)", border: "1px solid rgba(0,0,0,0.08)", color: "#6e6e76" }}
      >
        <LogIn size={13} style={{ color: BRAND }} />
        Already have an account? Sign in
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

/* ── White glass card shell with HUD SVG border — pixel-identical to LoginCard ── */
function GlassCard({
  children,
  canvasRef,
}: {
  children: React.ReactNode;
  canvasRef: React.RefObject<HTMLCanvasElement | null>;
}) {
  return (
    <div className="relative w-full max-w-[370px]">
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
        <canvas ref={canvasRef} className="absolute inset-0 h-full w-full" style={{ pointerEvents: "none" }} />

        <svg
          className="pointer-events-none absolute inset-0 h-full w-full"
          viewBox="0 0 370 640"
          fill="none"
          preserveAspectRatio="none"
        >
          <defs>
            <filter id="pglow-register">
              <feGaussianBlur stdDeviation="2.5" result="blur" />
              <feMerge><feMergeNode in="blur" /><feMergeNode in="SourceGraphic" /></feMerge>
            </filter>
          </defs>
          <path
            d="M28,4 L342,4 L366,28 L366,612 L342,636 L28,636 L4,612 L4,28 Z"
            stroke={BRAND}
            strokeWidth="1.5"
            strokeOpacity="0.55"
            filter="url(#pglow-register)"
          />
          <path
            d="M36,12 L334,12 L358,36 L358,604 L334,628 L36,628 L12,604 L12,36 Z"
            stroke={BRAND}
            strokeWidth="0.5"
            strokeOpacity="0.18"
          />
          <circle cx="28" cy="4" r="5" fill={BRAND} fillOpacity="0.85" filter="url(#pglow-register)" />
          <line x1="28" y1="4" x2="62" y2="4" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <line x1="28" y1="4" x2="28" y2="38" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <circle cx="342" cy="4" r="5" fill={BRAND} fillOpacity="0.85" filter="url(#pglow-register)" />
          <line x1="342" y1="4" x2="308" y2="4" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <line x1="342" y1="4" x2="342" y2="38" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <circle cx="28" cy="636" r="5" fill={BRAND} fillOpacity="0.85" filter="url(#pglow-register)" />
          <line x1="28" y1="636" x2="62" y2="636" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <line x1="28" y1="636" x2="28" y2="602" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <circle cx="342" cy="636" r="5" fill={BRAND} fillOpacity="0.85" filter="url(#pglow-register)" />
          <line x1="342" y1="636" x2="308" y2="636" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <line x1="342" y1="636" x2="342" y2="602" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <line x1="4" y1="315" x2="18" y2="315" stroke={BRAND} strokeWidth="1.5" strokeOpacity="0.4" />
          <line x1="352" y1="315" x2="366" y2="315" stroke={BRAND} strokeWidth="1.5" strokeOpacity="0.4" />
        </svg>

        <div
          className="pointer-events-none absolute left-[12%] top-0 h-px w-[76%] opacity-50"
          style={{ background: `linear-gradient(90deg, transparent, ${BRAND}55, transparent)` }}
        />

        <div className="relative z-10 px-7 pb-8 pt-8">{children}</div>
      </div>
    </div>
  );
}