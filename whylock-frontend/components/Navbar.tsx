"use client";

import { useEffect, useState } from "react";
import { LockLogo } from "@/components/LockLogo";
import { useAuth } from "@/lib/auth-context";
import { ShieldCheck, LogOut, Search, Globe, LayoutDashboard } from "lucide-react";

export function Navbar() {
  const { isAuthenticated, username, role, logout } = useAuth();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- intentional hydration-safe mount flag
    setMounted(true);
  }, []);

  const showAuthedState = mounted && isAuthenticated;

  function scrollToLogin() {
    document.getElementById("login")?.scrollIntoView({ behavior: "smooth", block: "center" });
  }

  return (
    <nav className="sticky top-0 z-40 flex items-center justify-between border-b border-hairline bg-void/80 px-6 py-4 backdrop-blur-xl md:px-10">
      <div className="flex items-center gap-3">
        <LockLogo size={28} />
        <div className="leading-tight">
          <span className="font-mono text-[15px] font-semibold tracking-[0.14em] text-foreground">
            WHY<span className="gradient-brand-text">LOCK</span>
          </span>
          <div className="hidden font-mono text-[9.5px] uppercase tracking-[0.2em] text-muted-dim sm:block">
            the decision engine
          </div>
        </div>
      </div>

      <div className="flex items-center gap-3">
        <div className="hidden items-center gap-1.5 md:flex">
          <button
            aria-label="Search"
            className="flex h-9 w-9 items-center justify-center rounded-full border border-hairline text-muted transition-colors hover:border-hairline-strong hover:text-foreground"
          >
            <Search size={15} />
          </button>
          <button
            aria-label="Language"
            className="flex h-9 w-9 items-center justify-center rounded-full border border-hairline text-muted transition-colors hover:border-hairline-strong hover:text-foreground"
          >
            <Globe size={15} />
          </button>
        </div>

        {showAuthedState ? (
          <>
            <button
              onClick={() =>
                document.getElementById("dashboard")?.scrollIntoView({ behavior: "smooth" })
              }
              className="hidden items-center gap-1.5 rounded-full border border-hairline px-4 py-2 text-sm text-muted transition-colors hover:border-hairline-strong hover:text-foreground sm:flex"
            >
              <LayoutDashboard size={14} />
              Dashboard
            </button>
            <div className="hidden items-center gap-2 rounded-full border border-hairline bg-surface px-3.5 py-1.5 sm:flex">
              <ShieldCheck size={13} className="text-lock-green" />
              <span className="font-mono text-xs text-foreground">{username}</span>
              {role && (
                <span className="font-mono text-[10px] uppercase tracking-wide text-muted">
                  · {role}
                </span>
              )}
            </div>
            <button
              onClick={logout}
              className="flex items-center gap-1.5 rounded-full border border-hairline px-4 py-2 text-sm text-muted transition-colors hover:border-danger/40 hover:text-danger"
            >
              <LogOut size={14} />
              <span className="hidden sm:inline">Sign out</span>
            </button>
          </>
        ) : (
          <button
            onClick={scrollToLogin}
            className="gradient-brand rounded-full px-5 py-2 text-sm font-semibold text-white shadow-[0_8px_24px_-8px_rgba(139,92,246,0.6)] transition-transform hover:scale-[1.03]"
          >
            Sign in
          </button>
        )}
      </div>
    </nav>
  );
}
