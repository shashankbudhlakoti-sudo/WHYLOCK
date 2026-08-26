import type { Metadata } from "next";
import { AuthProvider } from "@/lib/auth-context";
import "./globals.css";

export const metadata: Metadata = {
  title: "WHYLOCK — AI Security Decision Engine",
  description:
    "Run AI-powered security scans, surface CVE matches, and ship fixes before attackers find the gap. WHYLOCK is the decision engine for what to fix first.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="h-full antialiased">
      <body className="min-h-full flex flex-col bg-void text-foreground">
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}
