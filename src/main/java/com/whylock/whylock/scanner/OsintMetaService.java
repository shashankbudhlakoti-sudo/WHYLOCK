package com.whylock.whylock.scanner;

import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OsintMetaService {

    /**
     * Collects open-source intelligence metadata about a URL:
     * - Resolved IP address
     * - Hostname
     * - Whether IP is private/local (suspicious for public URLs)
     * - Basic reachability
     *
     * Returns a map that gets bundled into the Gemini AI prompt.
     */
    public Map<String, String> collect(String url) {
        Map<String, String> meta = new LinkedHashMap<>();
        meta.put("url", url);

        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            meta.put("hostname", host != null ? host : "unknown");

            if (host != null) {
                try {
                    InetAddress address = InetAddress.getByName(host);
                    String ip = address.getHostAddress();
                    meta.put("resolvedIp", ip);
                    meta.put("isPrivateIp", String.valueOf(address.isSiteLocalAddress()));
                    meta.put("isLoopback", String.valueOf(address.isLoopbackAddress()));
                    meta.put("canonicalHostname", address.getCanonicalHostName());
                } catch (Exception e) {
                    meta.put("resolvedIp", "DNS resolution failed");
                    meta.put("dnsError", e.getMessage());
                }
            }

            // Protocol check
            String scheme = uri.getScheme();
            meta.put("protocol", scheme != null ? scheme.toUpperCase() : "UNKNOWN");
            meta.put("port", uri.getPort() != -1 ? String.valueOf(uri.getPort()) : "default");
            meta.put("path", uri.getPath() != null ? uri.getPath() : "/");

        } catch (Exception e) {
            meta.put("parseError", "Could not parse URL: " + e.getMessage());
        }

        return meta;
    }
}