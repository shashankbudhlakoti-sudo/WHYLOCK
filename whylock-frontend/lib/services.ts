import {
  Activity,
  BrainCircuit,
  Fingerprint,
  Workflow,
  FileText,
  Wrench,
  ShieldCheck,
  type LucideIcon,
} from "lucide-react";

export interface ServiceInfo {
  icon: LucideIcon;
  service: string;
  title: string;
  desc: string;
}

/** Every card here maps 1:1 to a real backend service class. */
export const SERVICES: ServiceInfo[] = [
  {
    icon: BrainCircuit,
    service: "AiService",
    title: "AI Powered Analysis",
    desc: "An LLM reads every finding in context and explains why it matters.",
  },
  {
    icon: Activity,
    service: "MonitoringService",
    title: "Real-Time Monitoring",
    desc: "24/7 continuous watch on uptime, certs, and config drift.",
  },
  {
    icon: Fingerprint,
    service: "SslAnalysisService",
    title: "SSL & Cert Verified",
    desc: "TLS handshake and chain of trust checked down to the cipher suite.",
  },
  {
    icon: Workflow,
    service: "ScanOrchestrationService",
    title: "Coordinated Scanning",
    desc: "Headers, subdomains, ports, and breach exposure run in one pass.",
  },
  {
    icon: FileText,
    service: "PdfReportService",
    title: "Detailed Reports",
    desc: "Clean, exportable reports built for compliance review.",
  },
  {
    icon: Wrench,
    service: "AiFixAssistantService",
    title: "AI Fix Assistant",
    desc: "Findings ship with working remediation code for your exact stack.",
  },
  {
    icon: ShieldCheck,
    service: "TechnologyDetectionService",
    title: "Technology Detection",
    desc: "Full fingerprint of frameworks, libraries, and CMS versions in play.",
  },
];
