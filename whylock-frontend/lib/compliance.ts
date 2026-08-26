import type { ComplianceFramework, Finding } from "@/types";

export function buildCompliance(findings: Finding[]): ComplianceFramework[] {
  const titles = findings.map((f) => f.title.toLowerCase());
  const has = (kw: string) => titles.some((t) => t.includes(kw));

  const frameworks: Omit<ComplianceFramework, "pass" | "fail">[] = [
    {
      name: "GDPR",
      items: [
        { label: "HTTPS enforced", ok: !has("ssl") && !has("http") },
        { label: "HSTS present", ok: !has("hsts") && !has("strict-transport") },
        { label: "No data leakage", ok: !has("referrer") },
      ],
    },
    {
      name: "PCI-DSS",
      items: [
        { label: "Valid SSL cert", ok: !has("ssl") && !has("certificate") },
        { label: "CSP header", ok: !has("content-security") },
        { label: "X-Frame-Options", ok: !has("x-frame") },
      ],
    },
    {
      name: "ISO 27001",
      items: [
        {
          label: "Encryption in transit",
          ok: !has("http only") && !has("plain http"),
        },
        {
          label: "Security headers",
          ok:
            findings.filter((f) => f.title.toLowerCase().includes("header"))
              .length < 2,
        },
        { label: "No open redirects", ok: !has("redirect") },
      ],
    },
  ];

  return frameworks.map((f) => ({
    ...f,
    pass: f.items.filter((i) => i.ok).length,
    fail: f.items.filter((i) => !i.ok).length,
  }));
}

export function riskAction(risk: string): string {
  const actions: Record<string, string> = {
    CRITICAL: "BLOCK IMMEDIATELY",
    HIGH: "ACTION REQUIRED",
    MEDIUM: "REVIEW NEEDED",
    LOW: "MONITOR",
    SAFE: "ALL CLEAR",
  };
  return actions[risk] || "UNKNOWN";
}
