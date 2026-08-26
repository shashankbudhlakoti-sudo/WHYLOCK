export type Severity = "CRITICAL" | "HIGH" | "MEDIUM" | "LOW";

export type RiskLevel =
  | "CRITICAL"
  | "HIGH"
  | "MEDIUM"
  | "LOW"
  | "SAFE"
  | "ERROR"
  | "UNKNOWN";

export interface Finding {
  title: string;
  description: string;
  severity: Severity;
  cveMatch?: string | null;
  fixCode?: string | null;
}

export interface ScanResult {
  url: string;
  overallRisk: RiskLevel;
  riskScore: number;
  findings: Finding[];
  globalThreats: string[];
  summary?: string;
  aiModel?: string;
  scannedAt?: string;
}

export interface HistoryEntry {
  url: string;
  risk: RiskLevel;
  score: number;
  time?: string;
  result?: ScanResult;
}

export interface ComplianceItem {
  label: string;
  ok: boolean;
}

export interface ComplianceFramework {
  name: string;
  items: ComplianceItem[];
  pass: number;
  fail: number;
}

export interface AuthResponse {
  token: string;
}

export interface ApiError {
  message: string;
  status?: number;
}
