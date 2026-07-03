import type { ScanResult, Finding } from "@/types";

/**
 * Generates a premium, print-ready security report for a scan result and
 * opens the browser's native "Save as PDF" dialog. No backend round-trip
 * required — this works the moment a scan finishes, using data already
 * in memory.
 *
 * Design language: Apple-inspired white surface, glassmorphism cards,
 * an animated watermark on screen that becomes a static watermark on
 * print, and lightweight hand-rolled SVG charts built purely from the
 * scan's own findings/riskScore data (no external chart libs, no network
 * calls, no extra bundle weight).
 */

/* ------------------------------------------------------------------ */
/*  Severity model                                                     */
/* ------------------------------------------------------------------ */

type Severity = "CRITICAL" | "HIGH" | "MEDIUM" | "LOW";

const SEVERITY_ORDER: Severity[] = ["CRITICAL", "HIGH", "MEDIUM", "LOW"];

const SEVERITY_COLORS: Record<Severity, string> = {
  CRITICAL: "#e11d48",
  HIGH: "#ea580c",
  MEDIUM: "#d97706",
  LOW: "#16a34a",
};

const SEVERITY_SOFT: Record<Severity, string> = {
  CRITICAL: "#fde8ec",
  HIGH: "#fdecdf",
  MEDIUM: "#fbedd6",
  LOW: "#e5f6ea",
};

function normalizeSeverity(raw: string): Severity {
  const s = (raw || "").toUpperCase();
  if (s === "CRITICAL" || s === "HIGH" || s === "MEDIUM" || s === "LOW") return s;
  return "LOW";
}

function riskTone(overallRisk: string): { color: string; soft: string } {
  const s = (overallRisk || "").toUpperCase();
  if (s.includes("CRIT")) return { color: SEVERITY_COLORS.CRITICAL, soft: SEVERITY_SOFT.CRITICAL };
  if (s.includes("HIGH")) return { color: SEVERITY_COLORS.HIGH, soft: SEVERITY_SOFT.HIGH };
  if (s.includes("MED")) return { color: SEVERITY_COLORS.MEDIUM, soft: SEVERITY_SOFT.MEDIUM };
  return { color: SEVERITY_COLORS.LOW, soft: SEVERITY_SOFT.LOW };
}

/* ------------------------------------------------------------------ */
/*  SVG chart generation — built only from result data                 */
/* ------------------------------------------------------------------ */

/** Donut chart of findings grouped by severity. */
function buildSeverityDonut(findings: Finding[]): string {
  const counts: Record<Severity, number> = { CRITICAL: 0, HIGH: 0, MEDIUM: 0, LOW: 0 };
  findings.forEach((f) => { counts[normalizeSeverity(f.severity)]++; });
  const total = findings.length;

  const size = 220;
  const cx = size / 2;
  const cy = size / 2;
  const r = 78;
  const stroke = 26;
  const circumference = 2 * Math.PI * r;

  if (total === 0) {
    return `
    <svg class="chart-svg" viewBox="0 0 ${size} ${size}" xmlns="http://www.w3.org/2000/svg">
      <circle cx="${cx}" cy="${cy}" r="${r}" fill="none" stroke="#e9e9ee" stroke-width="${stroke}" />
      <text x="${cx}" y="${cy - 6}" text-anchor="middle" class="donut-center-num">0</text>
      <text x="${cx}" y="${cy + 16}" text-anchor="middle" class="donut-center-label">FINDINGS</text>
    </svg>`;
  }

  let offset = 0;
  const segments = SEVERITY_ORDER.filter((sev) => counts[sev] > 0)
    .map((sev) => {
      const frac = counts[sev] / total;
      const dash = frac * circumference;
      const gap = circumference - dash;
      const seg = `<circle cx="${cx}" cy="${cy}" r="${r}" fill="none" stroke="${SEVERITY_COLORS[sev]}"
        stroke-width="${stroke}" stroke-dasharray="${dash.toFixed(2)} ${gap.toFixed(2)}"
        stroke-dashoffset="${(-offset).toFixed(2)}" stroke-linecap="butt" transform="rotate(-90 ${cx} ${cy})"
        class="donut-seg" style="--seg-dash:${dash.toFixed(2)};" />`;
      offset += dash;
      return seg;
    })
    .join("\n      ");

  return `
    <svg class="chart-svg" viewBox="0 0 ${size} ${size}" xmlns="http://www.w3.org/2000/svg">
      <circle cx="${cx}" cy="${cy}" r="${r}" fill="none" stroke="#eef0f3" stroke-width="${stroke}" />
      ${segments}
      <text x="${cx}" y="${cy - 6}" text-anchor="middle" class="donut-center-num">${total}</text>
      <text x="${cx}" y="${cy + 16}" text-anchor="middle" class="donut-center-label">FINDINGS</text>
    </svg>`;
}

/** Legend rows matched to the donut, with counts + percentages. */
function buildSeverityLegend(findings: Finding[]): string {
  const counts: Record<Severity, number> = { CRITICAL: 0, HIGH: 0, MEDIUM: 0, LOW: 0 };
  findings.forEach((f) => { counts[normalizeSeverity(f.severity)]++; });
  const total = findings.length || 1;

  return SEVERITY_ORDER.map((sev) => {
    const count = counts[sev];
    const pct = Math.round((count / total) * 100);
    return `
    <div class="legend-row">
      <span class="legend-dot" style="background:${SEVERITY_COLORS[sev]}"></span>
      <span class="legend-label">${sev.charAt(0) + sev.slice(1).toLowerCase()}</span>
      <span class="legend-bar-track"><span class="legend-bar-fill" style="width:${findings.length ? pct : 0}%;background:${SEVERITY_COLORS[sev]}"></span></span>
      <span class="legend-count">${count}</span>
    </div>`;
  }).join("");
}

/** Semicircular gauge showing overall riskScore out of 100. */
function buildRiskGauge(riskScore: number, overallRisk: string): string {
  const score = Math.max(0, Math.min(100, Number.isFinite(riskScore) ? riskScore : 0));
  const tone = riskTone(overallRisk);

  const size = 220;
  const cx = size / 2;
  const cy = 128;
  const r = 84;
  const stroke = 20;
  const startAngle = 180;
  const endAngle = 0;
  const totalArc = Math.PI * r; // half circumference

  const scoreArc = (score / 100) * totalArc;
  const trackDash = `${totalArc.toFixed(2)} ${totalArc.toFixed(2)}`;
  const scoreDash = `${scoreArc.toFixed(2)} ${totalArc.toFixed(2)}`;

  const angle = 180 - (score / 100) * 180;
  const rad = (angle * Math.PI) / 180;
  const needleLen = r - stroke / 2 - 4;
  const nx = cx + needleLen * Math.cos(rad);
  const ny = cy - needleLen * Math.sin(rad);

  return `
    <svg class="chart-svg gauge-svg" viewBox="0 0 ${size} 150" xmlns="http://www.w3.org/2000/svg">
      <path d="M ${cx - r} ${cy} A ${r} ${r} 0 0 1 ${cx + r} ${cy}"
        fill="none" stroke="#eef0f3" stroke-width="${stroke}" stroke-linecap="round" />
      <path d="M ${cx - r} ${cy} A ${r} ${r} 0 0 1 ${cx + r} ${cy}"
        fill="none" stroke="${tone.color}" stroke-width="${stroke}" stroke-linecap="round"
        stroke-dasharray="${scoreDash}" class="gauge-fill" />
      <line x1="${cx}" y1="${cy}" x2="${nx.toFixed(2)}" y2="${ny.toFixed(2)}"
        stroke="#1d1d1f" stroke-width="3" stroke-linecap="round" class="gauge-needle" />
      <circle cx="${cx}" cy="${cy}" r="6" fill="#1d1d1f" />
      <text x="${cx}" y="${cy - 22}" text-anchor="middle" class="gauge-score">${Math.round(score)}</text>
      <text x="${cx}" y="${cy - 4}" text-anchor="middle" class="gauge-score-max">/100</text>
    </svg>`;
}

/* ------------------------------------------------------------------ */
/*  Findings table                                                     */
/* ------------------------------------------------------------------ */

function buildFindingsRows(findings: Finding[]): string {
  return findings
    .map((f, i) => {
      const sev = normalizeSeverity(f.severity);
      const breakBefore = i > 0 && i % 8 === 0 ? ` page-break-before` : "";
      return `
      <tr class="finding-row${breakBefore}">
        <td class="col-sev">
          <span class="sev-chip sev-${sev}">${sev}</span>
        </td>
        <td class="col-detail">
          <div class="ftitle">${escapeHtml(f.title)}</div>
          <div class="fdesc">${escapeHtml(f.description)}</div>
          ${f.cveMatch ? `<div class="cve"><span class="cve-tag">CVE</span>${escapeHtml(f.cveMatch)}</div>` : ""}
        </td>
      </tr>`;
    })
    .join("");
}

/* ------------------------------------------------------------------ */
/*  Main entry point                                                    */
/* ------------------------------------------------------------------ */

export function downloadReport(result: ScanResult) {
  const win = window.open("", "_blank", "width=980,height=1200");
  if (!win) return;

  const date = result.scannedAt ? new Date(result.scannedAt) : new Date();
  const tone = riskTone(result.overallRisk);
  const findingsRows = buildFindingsRows(result.findings);
  const donut = buildSeverityDonut(result.findings);
  const legend = buildSeverityLegend(result.findings);
  const gauge = buildRiskGauge(result.riskScore, result.overallRisk);
  const generatedStamp = date.toLocaleString();

  win.document.write(`<!doctype html>
<html>
<head>
<meta charset="utf-8" />
<title>WHYLOCK Report — ${escapeHtml(result.url)}</title>
<style>
  @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap');

  :root {
    --ink: #1d1d1f;
    --ink-soft: #48484c;
    --muted: #6e6e76;
    --hairline: rgba(0,0,0,0.08);
    --hairline-soft: rgba(0,0,0,0.06);
    --surface: rgba(255,255,255,0.72);
    --surface-solid: #ffffff;
    --bg-0: #f5f5f7;
    --bg-1: #ffffff;
    --accent-a: #7c3aed;
    --accent-b: #2563eb;
    --radius-lg: 22px;
    --radius-md: 16px;
    --radius-sm: 10px;
    --shadow-card: 0 1px 2px rgba(0,0,0,0.04), 0 12px 32px -12px rgba(0,0,0,0.12);
  }

  * { box-sizing: border-box; }

  html, body {
    margin: 0;
    padding: 0;
    background:
      radial-gradient(1200px 600px at 12% -10%, rgba(124,58,237,0.10), transparent 60%),
      radial-gradient(1000px 500px at 100% 0%, rgba(37,99,235,0.08), transparent 55%),
      var(--bg-0);
  }

  body {
    font-family: 'Inter', -apple-system, BlinkMacSystemFont, "SF Pro Display", "Segoe UI", Helvetica, Arial, sans-serif;
    color: var(--ink);
    -webkit-font-smoothing: antialiased;
    position: relative;
    min-height: 100vh;
  }

  /* ---------------- Watermark: animated on screen, static on print --------------- */
  .watermark {
    position: fixed;
    inset: -20%;
    z-index: 0;
    pointer-events: none;
    overflow: hidden;
    opacity: 0.05;
  }
  .watermark-track {
    width: 300%;
    height: 300%;
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    transform: rotate(-28deg);
    animation: watermark-drift 34s linear infinite;
  }
  .watermark-track span {
    font-weight: 800;
    font-size: 46px;
    letter-spacing: 0.12em;
    color: var(--ink);
    white-space: nowrap;
    padding: 36px 0;
    text-align: center;
  }
  @keyframes watermark-drift {
    0%   { transform: rotate(-28deg) translate3d(0,0,0); }
    100% { transform: rotate(-28deg) translate3d(-6%,-6%,0); }
  }

  .page {
    position: relative;
    z-index: 1;
    max-width: 900px;
    margin: 0 auto;
    padding: 56px 48px 80px;
  }

  /* ---------------- Header / brand ---------------- */
  .brand-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 40px;
  }
  .brand {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .brand-mark {
    width: 34px;
    height: 34px;
    border-radius: 10px;
    background: linear-gradient(135deg, var(--accent-a), var(--accent-b));
    box-shadow: 0 4px 14px -4px rgba(124,58,237,0.55);
  }
  .brand-name {
    font-weight: 800;
    font-size: 16px;
    letter-spacing: 0.14em;
  }
  .brand-tag {
    font-size: 11px;
    color: var(--muted);
    letter-spacing: 0.04em;
    margin-top: 1px;
  }
  .stamp {
    text-align: right;
    font-size: 11.5px;
    color: var(--muted);
    line-height: 1.5;
  }
  .stamp strong { color: var(--ink-soft); font-weight: 600; }

  .hero {
    margin-bottom: 32px;
  }
  .hero-eyebrow {
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0.14em;
    color: var(--accent-a);
    text-transform: uppercase;
    margin-bottom: 10px;
  }
  h1 {
    font-size: 32px;
    font-weight: 800;
    letter-spacing: -0.02em;
    margin: 0 0 8px;
    line-height: 1.15;
    word-break: break-word;
  }
  .meta {
    color: var(--muted);
    font-size: 13.5px;
  }

  /* ---------------- Glass cards ---------------- */
  .glass {
    background: var(--surface);
    backdrop-filter: blur(20px) saturate(180%);
    -webkit-backdrop-filter: blur(20px) saturate(180%);
    border: 1px solid rgba(255,255,255,0.6);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-card);
  }

  .score-row {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    margin-bottom: 28px;
  }
  .score-card {
    padding: 20px 22px;
  }
  .score-label {
    font-size: 10.5px;
    text-transform: uppercase;
    letter-spacing: 0.1em;
    color: var(--muted);
    margin-bottom: 8px;
    font-weight: 600;
  }
  .score-value {
    font-size: 25px;
    font-weight: 800;
    letter-spacing: -0.01em;
  }
  .score-value.risk-tone { color: ${tone.color}; }
  .score-pill {
    display: inline-block;
    margin-top: 8px;
    padding: 3px 10px;
    border-radius: 999px;
    font-size: 10.5px;
    font-weight: 700;
    letter-spacing: 0.04em;
    background: ${tone.soft};
    color: ${tone.color};
  }

  /* ---------------- Charts section ---------------- */
  .charts-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
    margin-bottom: 28px;
  }
  .chart-card {
    padding: 24px 26px 20px;
  }
  .chart-card-title {
    font-size: 12.5px;
    font-weight: 700;
    letter-spacing: 0.02em;
    color: var(--ink-soft);
    margin-bottom: 4px;
  }
  .chart-card-sub {
    font-size: 11.5px;
    color: var(--muted);
    margin-bottom: 14px;
  }
  .chart-svg { width: 100%; height: auto; display: block; margin: 0 auto; }
  .donut-wrap { display: flex; align-items: center; gap: 20px; }
  .donut-wrap .chart-svg { max-width: 150px; flex: 0 0 auto; }
  .donut-center-num { font-size: 34px; font-weight: 800; fill: var(--ink); font-family: 'Inter', sans-serif; }
  .donut-center-label { font-size: 9px; font-weight: 700; letter-spacing: 0.1em; fill: var(--muted); }
  .donut-seg {
    transform-origin: center;
    stroke-dashoffset: var(--seg-dash);
    animation: donut-in 1s cubic-bezier(0.22,1,0.36,1) forwards;
  }
  @keyframes donut-in {
    from { stroke-dashoffset: calc(var(--seg-dash) + 251); }
  }

  .legend { flex: 1; display: flex; flex-direction: column; gap: 10px; }
  .legend-row { display: grid; grid-template-columns: 10px 60px 1fr 22px; align-items: center; gap: 10px; }
  .legend-dot { width: 8px; height: 8px; border-radius: 50%; }
  .legend-label { font-size: 12px; color: var(--ink-soft); font-weight: 500; }
  .legend-bar-track { height: 6px; border-radius: 4px; background: #eef0f3; overflow: hidden; }
  .legend-bar-fill { display: block; height: 100%; border-radius: 4px; transition: width 0.4s ease; }
  .legend-count { font-size: 12px; font-weight: 700; text-align: right; color: var(--ink); }

  .gauge-svg { max-width: 200px; }
  .gauge-score { font-size: 30px; font-weight: 800; fill: var(--ink); }
  .gauge-score-max { font-size: 11px; fill: var(--muted); font-weight: 600; }
  .gauge-fill { transition: stroke-dasharray 0.9s ease; }

  /* ---------------- Summary ---------------- */
  .summary {
    padding: 22px 24px;
    margin: 0 0 28px;
    line-height: 1.65;
    font-size: 13.5px;
    color: var(--ink-soft);
  }
  .summary-title {
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0.1em;
    text-transform: uppercase;
    color: var(--accent-a);
    margin-bottom: 10px;
  }

  /* ---------------- Findings table ---------------- */
  .findings-card {
    padding: 4px 0;
    overflow: hidden;
  }
  .findings-head {
    padding: 20px 26px 14px;
    display: flex;
    align-items: baseline;
    justify-content: space-between;
  }
  .findings-title { font-size: 15px; font-weight: 700; }
  .findings-count { font-size: 11.5px; color: var(--muted); }

  table { width: 100%; border-collapse: collapse; }
  td { border-top: 1px solid var(--hairline-soft); padding: 16px 26px; vertical-align: top; font-size: 13px; }
  tr:first-child td { border-top: 1px solid var(--hairline); }
  .col-sev { width: 120px; white-space: nowrap; }
  .sev-chip {
    display: inline-block;
    font-weight: 700;
    font-size: 10.5px;
    letter-spacing: 0.04em;
    padding: 4px 10px;
    border-radius: 999px;
  }
  .sev-CRITICAL { color: ${SEVERITY_COLORS.CRITICAL}; background: ${SEVERITY_SOFT.CRITICAL}; }
  .sev-HIGH { color: ${SEVERITY_COLORS.HIGH}; background: ${SEVERITY_SOFT.HIGH}; }
  .sev-MEDIUM { color: ${SEVERITY_COLORS.MEDIUM}; background: ${SEVERITY_SOFT.MEDIUM}; }
  .sev-LOW { color: ${SEVERITY_COLORS.LOW}; background: ${SEVERITY_SOFT.LOW}; }

  .ftitle { font-weight: 600; margin-bottom: 4px; color: var(--ink); }
  .fdesc { color: var(--ink-soft); line-height: 1.55; }
  .cve {
    margin-top: 8px;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-family: 'JetBrains Mono', monospace;
    font-size: 10.5px;
    color: var(--accent-a);
  }
  .cve-tag {
    background: rgba(124,58,237,0.1);
    color: var(--accent-a);
    font-family: 'Inter', sans-serif;
    font-weight: 700;
    font-size: 9px;
    letter-spacing: 0.06em;
    padding: 2px 6px;
    border-radius: 4px;
  }

  .footer {
    margin-top: 44px;
    padding-top: 20px;
    border-top: 1px solid var(--hairline-soft);
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 11px;
    color: #a1a1a8;
  }
  .footer-brand { font-weight: 700; letter-spacing: 0.08em; color: var(--muted); }

  /* ---------------- Responsive ---------------- */
  @media (max-width: 760px) {
    .page { padding: 32px 20px 60px; }
    .score-row { grid-template-columns: 1fr; }
    .charts-row { grid-template-columns: 1fr; }
    .donut-wrap { flex-direction: column; }
    h1 { font-size: 26px; }
    td { padding: 14px 16px; }
    .col-sev { width: 90px; }
  }

  /* ---------------- Print ---------------- */
  @media print {
    @page {
      size: A4;
      margin: 16mm 14mm;
    }
    html, body {
      background: #ffffff !important;
    }
    .watermark {
      opacity: 0.045;
      position: fixed;
    }
    .watermark-track {
      animation: none !important;
      transform: rotate(-28deg) translate3d(0,0,0) !important;
    }
    .glass {
      background: #ffffff !important;
      backdrop-filter: none !important;
      -webkit-backdrop-filter: none !important;
      box-shadow: none !important;
      border: 1px solid #e3e3e8 !important;
    }
    .page { max-width: 100%; padding: 0; }
    .donut-seg, .gauge-fill { animation: none !important; }
    .brand-row { margin-bottom: 24px; }
    .score-row, .charts-row { break-inside: avoid; }
    .findings-card { break-before: page; }
    .finding-row { break-inside: avoid; }
    .page-break-before { break-before: page; }
    a { text-decoration: none; color: inherit; }
  }
</style>
</head>
<body>

  <div class="watermark" aria-hidden="true">
    <div class="watermark-track">
      ${Array.from({ length: 42 }).map(() => "<span>WHYLOCK</span>").join("")}
    </div>
  </div>

  <div class="page">

    <div class="brand-row">
      <div class="brand">
        <div class="brand-mark"></div>
        <div>
          <div class="brand-name">WHYLOCK</div>
          <div class="brand-tag">Decision engine for what to fix first</div>
        </div>
      </div>
      <div class="stamp">
        Generated <strong>${escapeHtml(generatedStamp)}</strong><br />
        ${result.aiModel ? `Model: <strong>${escapeHtml(result.aiModel)}</strong>` : ""}
      </div>
    </div>

    <div class="hero">
      <div class="hero-eyebrow">Security Scan Report</div>
      <h1>${escapeHtml(result.url)}</h1>
      <div class="meta">Scanned ${escapeHtml(date.toLocaleString())}</div>
    </div>

    <div class="score-row">
      <div class="score-card glass">
        <div class="score-label">Overall risk</div>
        <div class="score-value risk-tone">${escapeHtml(result.overallRisk)}</div>
        <span class="score-pill">${result.findings.length} total finding${result.findings.length === 1 ? "" : "s"}</span>
      </div>
      <div class="score-card glass">
        <div class="score-label">Risk score</div>
        <div class="score-value">${result.riskScore}<span style="font-size:14px;color:var(--muted);font-weight:600;">/100</span></div>
      </div>
      <div class="score-card glass">
        <div class="score-label">Findings</div>
        <div class="score-value">${result.findings.length}</div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card glass">
        <div class="chart-card-title">Findings by severity</div>
        <div class="chart-card-sub">Distribution across all detected issues</div>
        <div class="donut-wrap">
          ${donut}
          <div class="legend">${legend}</div>
        </div>
      </div>
      <div class="chart-card glass">
        <div class="chart-card-title">Risk score</div>
        <div class="chart-card-sub">Composite score derived from this scan</div>
        ${gauge}
      </div>
    </div>

    ${result.summary ? `
    <div class="summary glass">
      <div class="summary-title">Executive summary</div>
      ${escapeHtml(result.summary).split("\n").filter(Boolean).map((p) => `<p style="margin:0 0 10px;">${p}</p>`).join("")}
    </div>` : ""}

    <div class="findings-card glass">
      <div class="findings-head">
        <div class="findings-title">Detailed findings</div>
        <div class="findings-count">${result.findings.length} item${result.findings.length === 1 ? "" : "s"}</div>
      </div>
      <table>
        <tbody>${findingsRows || `<tr><td colspan="2" style="text-align:center;color:var(--muted);padding:32px 26px;">No findings recorded.</td></tr>`}</tbody>
      </table>
    </div>

    <div class="footer">
      <span class="footer-brand">WHYLOCK</span>
      <span>Generated automatically · the decision engine for what to fix first</span>
    </div>

  </div>

  <script>
    window.onload = () => setTimeout(() => window.print(), 300);
  </script>
</body>
</html>`);
  win.document.close();
}

/* ------------------------------------------------------------------ */
/*  Utilities                                                          */
/* ------------------------------------------------------------------ */

function escapeHtml(s: string): string {
  return (s ?? "")
    .toString()
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}