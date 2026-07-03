package com.whylock.whylock.scanner;

import com.whylock.whylock.model.PortScanResult;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

@Service
public class PortScannerService {

    // Most common ports + their services + risk level
    private static final Map<Integer, String[]> PORT_INFO = new LinkedHashMap<>();

    static {
        // [service name, risk level, description]
        PORT_INFO.put(21,   new String[]{"FTP",         "HIGH",     "File Transfer — often exploited"});
        PORT_INFO.put(22,   new String[]{"SSH",         "MEDIUM",   "Secure Shell — brute force target"});
        PORT_INFO.put(23,   new String[]{"Telnet",      "CRITICAL", "Unencrypted remote access"});
        PORT_INFO.put(25,   new String[]{"SMTP",        "MEDIUM",   "Mail server — spam relay risk"});
        PORT_INFO.put(53,   new String[]{"DNS",         "MEDIUM",   "Domain Name System"});
        PORT_INFO.put(80,   new String[]{"HTTP",        "LOW",      "Web server — unencrypted"});
        PORT_INFO.put(110,  new String[]{"POP3",        "HIGH",     "Mail retrieval — unencrypted"});
        PORT_INFO.put(143,  new String[]{"IMAP",        "HIGH",     "Mail access — unencrypted"});
        PORT_INFO.put(443,  new String[]{"HTTPS",       "SAFE",     "Secure web server"});
        PORT_INFO.put(445,  new String[]{"SMB",         "CRITICAL", "Windows sharing — WannaCry target"});
        PORT_INFO.put(1433, new String[]{"MSSQL",       "CRITICAL", "SQL Server — exposed DB"});
        PORT_INFO.put(1521, new String[]{"Oracle DB",   "CRITICAL", "Oracle database exposed"});
        PORT_INFO.put(2375, new String[]{"Docker",      "CRITICAL", "Docker API — full server access"});
        PORT_INFO.put(3000, new String[]{"Dev Server",  "HIGH",     "Development server exposed"});
        PORT_INFO.put(3306, new String[]{"MySQL",       "CRITICAL", "Database exposed to internet"});
        PORT_INFO.put(3389, new String[]{"RDP",         "CRITICAL", "Remote Desktop — ransomware target"});
        PORT_INFO.put(4443, new String[]{"HTTPS-Alt",   "LOW",      "Alternative HTTPS port"});
        PORT_INFO.put(5432, new String[]{"PostgreSQL",  "CRITICAL", "Database exposed to internet"});
        PORT_INFO.put(5900, new String[]{"VNC",         "CRITICAL", "Remote desktop — often unencrypted"});
        PORT_INFO.put(6379, new String[]{"Redis",       "CRITICAL", "Cache DB — no auth by default"});
        PORT_INFO.put(7070, new String[]{"AJP",         "HIGH",     "Tomcat connector — CVE-2020-1938"});
        PORT_INFO.put(8000, new String[]{"Dev HTTP",    "HIGH",     "Development server exposed"});
        PORT_INFO.put(8080, new String[]{"HTTP-Alt",    "MEDIUM",   "Alternative HTTP port"});
        PORT_INFO.put(8443, new String[]{"HTTPS-Alt",   "LOW",      "Alternative HTTPS port"});
        PORT_INFO.put(8888, new String[]{"Jupyter",     "CRITICAL", "Jupyter notebook — code execution"});
        PORT_INFO.put(9200, new String[]{"Elasticsearch","CRITICAL","Search DB — no auth by default"});
        PORT_INFO.put(9300, new String[]{"ES Cluster",  "CRITICAL", "Elasticsearch cluster port"});
        PORT_INFO.put(27017,new String[]{"MongoDB",     "CRITICAL", "Database exposed to internet"});
        PORT_INFO.put(6443, new String[]{"Kubernetes",  "CRITICAL", "K8s API server exposed"});
        PORT_INFO.put(2181, new String[]{"ZooKeeper",   "HIGH",     "Coordination service exposed"});
    }

    /**
     * Scans all common ports for a given host.
     * Runs in parallel using thread pool.
     */
    public List<PortScanResult> scanPorts(String target) {
        String host = extractHost(target);
        List<PortScanResult> results = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<Future<PortScanResult>> futures = new ArrayList<>();

        for (Map.Entry<Integer, String[]> entry : PORT_INFO.entrySet()) {
            int port = entry.getKey();
            String[] info = entry.getValue();
            futures.add(executor.submit(() -> checkPort(host, port, info)));
        }

        for (Future<PortScanResult> future : futures) {
            try {
                PortScanResult result = future.get(5, TimeUnit.SECONDS);
                if (result != null) results.add(result);
            } catch (Exception e) {
                // timeout — port likely filtered
            }
        }

        executor.shutdown();

        // Sort — open ports first, then by risk level
        results.sort((a, b) -> {
            if (a.isOpen() && !b.isOpen()) return -1;
            if (!a.isOpen() && b.isOpen()) return 1;
            return riskOrder(b.getRiskLevel()) - riskOrder(a.getRiskLevel());
        });

        return results;
    }

    // ─── Check single port ────────────────────────────────────────────────────

    private PortScanResult checkPort(String host, int port, String[] info) {
        PortScanResult result = new PortScanResult();
        result.setPort(port);
        result.setService(info[0]);
        result.setRiskLevel(info[1]);
        result.setDescription(info[2]);

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 2000);
            result.setOpen(true);
            result.setStatus("open");
        } catch (Exception e) {
            result.setOpen(false);
            result.setStatus("closed");
        }

        return result;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String extractHost(String input) {
        try {
            if (!input.startsWith("http")) input = "https://" + input;
            URI uri = URI.create(input);
            String host = uri.getHost();
            return host != null ? host : input;
        } catch (Exception e) {
            return input.replace("https://", "")
                    .replace("http://", "");
        }
    }

    private int riskOrder(String risk) {
        return switch (risk) {
            case "CRITICAL" -> 4;
            case "HIGH"     -> 3;
            case "MEDIUM"   -> 2;
            case "LOW"      -> 1;
            default         -> 0;
        };
    }
}