"use client";

import { useEffect, useRef, useState, type FormEvent } from "react";
import { useAuth } from "@/lib/auth-context";
import { api, ApiClientError } from "@/lib/api";
import { Sparkles, Send, Bot, User as UserIcon, Lock } from "lucide-react";
import type { ScanResult } from "@/types";

/* Same brand palette as LoginCard/RegisterCard — kept identical on purpose */
const BRAND = "#7c3aed";
const BRAND2 = "#2563eb";
const BRAND_GLOW = "rgba(124,58,237,0.55)";

interface ChatMessage {
  role: "user" | "assistant";
  content: string;
}

interface AssistantPanelProps {
  /** Current scan result, if any — used to ground the assistant's answers. */
  result?: ScanResult | null;
}

function buildContext(result?: ScanResult | null): string | undefined {
  if (!result) return undefined;
  const topFindings = (result.findings || [])
    .slice(0, 5)
    .map((f) => `- [${f.severity}] ${f.title}`)
    .join("\n");
  return [
    `Target: ${result.url}`,
    `Overall risk: ${result.overallRisk} (${result.riskScore}/100)`,
    result.summary ? `Summary: ${result.summary}` : null,
    topFindings ? `Top findings:\n${topFindings}` : null,
  ]
    .filter(Boolean)
    .join("\n");
}

export function AssistantPanel({ result }: AssistantPanelProps) {
  const { token, isAuthenticated } = useAuth();
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      role: "assistant",
      content:
        "Hi, I'm the WhyLock Assistant. Ask me about any finding above and I'll explain the risk and how to fix it.",
    },
  ]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const scrollRef = useRef<HTMLDivElement>(null);

  /* Same beam animation as LoginCard/RegisterCard */
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    canvas.width = canvas.offsetWidth;
    canvas.height = canvas.offsetHeight;

    const cx = canvas.width / 2;
    const cy = canvas.height * 0.15;
    const NUM_BEAMS = 40;

    const beams = Array.from({ length: NUM_BEAMS }, (_, i) => ({
      angle: (i / NUM_BEAMS) * Math.PI * 2,
      speed: 0.5 + Math.random() * 1.2,
      length: 0.25 + Math.random() * 0.75,
      opacity: 0.06 + Math.random() * 0.14,
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
      grd.addColorStop(0, "rgba(124,58,237,0.05)");
      grd.addColorStop(0.5, "rgba(37,99,235,0.025)");
      grd.addColorStop(1, "transparent");
      ctx.fillStyle = grd;
      ctx.fillRect(0, 0, canvas.width, canvas.height);

      beams.forEach((b) => {
        const phase = ((t * b.speed * 0.011 + b.offset) % 1);
        const len = canvas.height * 0.9 * b.length * phase;
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

      t++;
      animId = requestAnimationFrame(draw);
    }

    draw();
    return () => cancelAnimationFrame(animId);
  }, []);

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: "smooth" });
  }, [messages, loading]);

  async function handleSend(e: FormEvent) {
    e.preventDefault();
    const text = input.trim();
    if (!text || loading) return;

    if (!isAuthenticated || !token) {
      setError("Sign in to chat with the WhyLock Assistant.");
      return;
    }

    setError(null);
    setMessages((m) => [...m, { role: "user", content: text }]);
    setInput("");
    setLoading(true);

    try {
      const reply = await api.chatWithAssistant(text, buildContext(result), token);
      setMessages((m) => [...m, { role: "assistant", content: reply }]);
    } catch (err) {
      const msg =
        err instanceof ApiClientError
          ? err.message
          : "The assistant is temporarily unavailable. Please retry.";
      setMessages((m) => [...m, { role: "assistant", content: msg }]);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="relative mb-6 w-full">
      {/* Ambient glow behind card — identical treatment to LoginCard */}
      <div
        className="pointer-events-none absolute -inset-4 rounded-[32px] opacity-30 blur-3xl"
        style={{ background: `radial-gradient(ellipse at 50% 10%, ${BRAND}44, transparent 65%)` }}
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

        {/* HUD octagonal SVG border — same brand purple as LoginCard */}
        <svg
          className="pointer-events-none absolute inset-0 h-full w-full"
          viewBox="0 0 1000 460"
          fill="none"
          preserveAspectRatio="none"
        >
          <defs>
            <filter id="pglow-assistant">
              <feGaussianBlur stdDeviation="2.5" result="blur" />
              <feMerge><feMergeNode in="blur" /><feMergeNode in="SourceGraphic" /></feMerge>
            </filter>
          </defs>
          <path
            d="M28,4 L972,4 L996,28 L996,432 L972,456 L28,456 L4,432 L4,28 Z"
            stroke={BRAND}
            strokeWidth="1.5"
            strokeOpacity="0.55"
            filter="url(#pglow-assistant)"
          />
          <circle cx="28" cy="4" r="5" fill={BRAND} fillOpacity="0.85" filter="url(#pglow-assistant)" />
          <line x1="28" y1="4" x2="62" y2="4" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <line x1="28" y1="4" x2="28" y2="38" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <circle cx="972" cy="4" r="5" fill={BRAND} fillOpacity="0.85" filter="url(#pglow-assistant)" />
          <line x1="972" y1="4" x2="938" y2="4" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <line x1="972" y1="4" x2="972" y2="38" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <circle cx="28" cy="456" r="5" fill={BRAND} fillOpacity="0.85" filter="url(#pglow-assistant)" />
          <line x1="28" y1="456" x2="62" y2="456" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <line x1="28" y1="456" x2="28" y2="422" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <circle cx="972" cy="456" r="5" fill={BRAND} fillOpacity="0.85" filter="url(#pglow-assistant)" />
          <line x1="972" y1="456" x2="938" y2="456" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
          <line x1="972" y1="456" x2="972" y2="422" stroke={BRAND} strokeWidth="2" strokeOpacity="0.5" />
        </svg>

        {/* Shimmer top-edge */}
        <div
          className="pointer-events-none absolute left-[12%] top-0 h-px w-[76%] opacity-50"
          style={{ background: `linear-gradient(90deg, transparent, ${BRAND}55, transparent)` }}
        />

        <div className="relative z-10 px-6 pb-6 pt-6 md:px-8 md:pb-8 md:pt-7">
          {/* Header — same lock-ring icon treatment as LoginCard, plus a live pulse */}
          <div className="mb-4 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="relative flex h-10 w-10 items-center justify-center">
                <div
                  className="absolute inset-0 rounded-full"
                  style={{
                    background: `conic-gradient(from 0deg, ${BRAND}, ${BRAND2}, transparent 45%, transparent 55%, ${BRAND}, ${BRAND})`,
                    animation: "spin-slow 4s linear infinite",
                    mask: "radial-gradient(farthest-side, transparent calc(100% - 2px), #000 calc(100% - 2px))",
                    WebkitMask: "radial-gradient(farthest-side, transparent calc(100% - 2px), #000 calc(100% - 2px))",
                  }}
                />
                <Sparkles size={16} style={{ color: BRAND, filter: `drop-shadow(0 0 6px ${BRAND_GLOW})` }} />
              </div>
              <div>
                <h3
                  className="font-mono text-[13px] font-bold uppercase tracking-[0.1em]"
                  style={{ color: "#1d1d1f" }}
                >
                  WhyLock Assistant
                </h3>
                <p className="font-mono text-[10px] text-muted">Groq · Llama 3.3 70B</p>
              </div>
            </div>
            <div
              className="flex items-center gap-1.5 rounded-full px-2.5 py-1 font-mono text-[9.5px] uppercase tracking-wider"
              style={{ background: "#16a34a1a", color: "#16a34a" }}
            >
              <span
                className="h-1.5 w-1.5 rounded-full"
                style={{ background: "#16a34a", animation: "pulse-glow 1.6s ease-in-out infinite" }}
              />
              Live
            </div>
          </div>

          {/* Message list */}
          <div
            ref={scrollRef}
            className="mb-4 max-h-[280px] min-h-[140px] space-y-3 overflow-y-auto rounded-xl p-3"
            style={{ background: "rgba(0,0,0,0.025)", border: "1px solid rgba(0,0,0,0.06)" }}
          >
            {messages.map((m, i) => (
              <div
                key={i}
                className={`flex items-start gap-2 ${m.role === "user" ? "flex-row-reverse" : ""}`}
              >
                <div
                  className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full"
                  style={{
                    background: m.role === "user" ? "rgba(0,0,0,0.06)" : `${BRAND}14`,
                    color: m.role === "user" ? "#6e6e76" : BRAND,
                  }}
                >
                  {m.role === "user" ? <UserIcon size={12} /> : <Bot size={12} />}
                </div>
                <div
                  className={`max-w-[80%] rounded-xl px-3.5 py-2.5 text-[12.5px] leading-relaxed ${
                    m.role === "user" ? "text-white" : ""
                  }`}
                  style={
                    m.role === "user"
                      ? { background: `linear-gradient(135deg, ${BRAND}, ${BRAND2})` }
                      : { background: "#ffffff", border: "1px solid rgba(0,0,0,0.08)", color: "#1d1d1f" }
                  }
                >
                  {m.content}
                </div>
              </div>
            ))}
            {loading && (
              <div className="flex items-start gap-2">
                <div
                  className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full"
                  style={{ background: `${BRAND}14`, color: BRAND }}
                >
                  <Bot size={12} />
                </div>
                <div
                  className="flex items-center gap-1 rounded-xl px-3.5 py-2.5"
                  style={{ background: "#ffffff", border: "1px solid rgba(0,0,0,0.08)" }}
                >
                  {[0, 1, 2].map((i) => (
                    <span
                      key={i}
                      className="h-1.5 w-1.5 rounded-full"
                      style={{
                        background: BRAND,
                        opacity: 0.5,
                        animation: `pulse-glow 1s ease-in-out ${i * 0.15}s infinite`,
                      }}
                    />
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Input row */}
          <form onSubmit={handleSend} className="flex items-center gap-2">
            <div
              className="flex flex-1 items-center gap-2 rounded-xl px-4 py-2.5 transition-all focus-within:shadow-[0_0_0_2px_rgba(124,58,237,0.35)]"
              style={{ background: "rgba(0,0,0,0.04)", border: "1px solid rgba(0,0,0,0.1)" }}
            >
              <input
                type="text"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                placeholder={
                  isAuthenticated
                    ? "Ask about a finding, e.g. \"how do I fix the SSL issue?\""
                    : "Sign in to chat with the assistant"
                }
                disabled={!isAuthenticated}
                className="w-full bg-transparent text-[13px] outline-none placeholder:text-gray-400 disabled:cursor-not-allowed"
                style={{ color: "#1d1d1f" }}
              />
            </div>
            <button
              type="submit"
              disabled={!isAuthenticated || loading || !input.trim()}
              className="flex h-[42px] w-[42px] shrink-0 items-center justify-center rounded-xl text-white transition-all disabled:opacity-40"
              style={{
                background: `linear-gradient(135deg, rgba(60,20,120,0.9), rgba(20,50,130,0.85))`,
                border: `1px solid ${BRAND}55`,
                boxShadow: `0 0 20px ${BRAND}33`,
              }}
            >
              {isAuthenticated ? <Send size={15} /> : <Lock size={15} />}
            </button>
          </form>

          {error && (
            <p
              className="mt-3 rounded-xl py-2 text-center text-[11.5px] text-red-500"
              style={{ background: "rgba(239,68,68,0.06)", border: "1px solid rgba(239,68,68,0.15)" }}
            >
              {error}
            </p>
          )}
        </div>
      </div>
    </div>
  );
}