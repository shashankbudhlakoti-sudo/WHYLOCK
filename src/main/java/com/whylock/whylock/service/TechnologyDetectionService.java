package com.whylock.whylock.service;

import com.whylock.whylock.model.TechStackItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

/**
 * WhyLock Technology Detection Service
 * ──────────────────────────────────────
 * 100% free — no Wappalyzer API, no third-party service.
 * Fingerprints technology stack via:
 *   1. HTTP response headers  (X-Powered-By, Server, Set-Cookie, X-Generator…)
 *   2. HTML body patterns     (meta generators, script src paths, class names)
 *   3. Cookie names           (PHPSESSID, JSESSIONID, ASP.NET_SessionId…)
 *   4. URL patterns           (/wp-content/, /drupal/, /__nuxt/…)
 *
 * Each signature has a confidence weight and a layer (Web Server / CMS /
 * Language / Framework / CDN / Analytics / JS Library / Security).
 * Multiple matching signals boost confidence up to 100%.
 *
 * CVE exposure is looked up from a bundled lightweight map (no network call).
 */
@Service
public class TechnologyDetectionService {

    private static final Logger log = LoggerFactory.getLogger(TechnologyDetectionService.class);

    // ── Signature Registry ─────────────────────────────────────────────────────
    // Format: TechSignature(name, layer, headerKey, headerPattern, htmlPattern, cookiePattern, urlPattern, baseConfidence, knownCves)

    private static final List<TechSignature> SIGNATURES = List.of(
            // Web Servers
            new TechSignature("Nginx",         "Web Server",   "Server",        "nginx",           null,                  null,          null,              85, 2),
            new TechSignature("Apache",        "Web Server",   "Server",        "Apache",          null,                  null,          null,              85, 8),
            new TechSignature("Caddy",         "Web Server",   "Server",        "Caddy",           null,                  null,          null,              90, 0),
            new TechSignature("LiteSpeed",     "Web Server",   "Server",        "LiteSpeed",       null,                  null,          null,              90, 1),
            new TechSignature("IIS",           "Web Server",   "Server",        "Microsoft-IIS",   null,                  null,          null,              90, 12),

            // Languages / Runtime
            new TechSignature("PHP",           "Language",     "X-Powered-By",  "PHP",             null,                  "PHPSESSID",   null,              80, 5),
            new TechSignature("Java",          "Language",     "X-Powered-By",  "JSP|Servlet",     null,                  "JSESSIONID",  null,              80, 3),
            new TechSignature("ASP.NET",       "Language",     "X-Powered-By",  "ASP\\.NET",       null,                  "ASP\\.NET_SessionId", null,     85, 9),
            new TechSignature("Node.js",       "Language",     "X-Powered-By",  "Express|Node",    null,                  null,          null,              75, 2),
            new TechSignature("Ruby",          "Language",     "X-Powered-By",  "Phusion Passenger|Ruby", null,           "_session_id", null,              70, 1),

            // CMS
            new TechSignature("WordPress",     "CMS",          null,            null,               "wp-content|wp-includes", "wordpress_", "/wp-content/",  90, 15),
            new TechSignature("Drupal",        "CMS",          "X-Generator",   "Drupal",          "Drupal\\.settings",   "Drupal\\.visitor", "/sites/default/", 85, 7),
            new TechSignature("Joomla",        "CMS",          null,            null,               "joomla|com_content",  null,          "/components/com_", 85, 10),
            new TechSignature("Ghost",         "CMS",          "X-Ghost-Cache", null,               "ghost-version",       null,          "/ghost/",         90, 1),
            new TechSignature("Shopify",       "CMS",          null,            null,               "Shopify\\.theme",     "_shopify_",   null,              92, 2),
            new TechSignature("Magento",       "CMS",          null,            null,               "Mage\\.Cookies",      "MAGE_",       "/magento/",       88, 11),
            new TechSignature("Wix",           "CMS",          "X-Wix-Request-Id", null,           "wix-blob",            null,          ".wixsite.com",    95, 0),
            new TechSignature("Squarespace",   "CMS",          "X-ServedBy",    null,               "squarespace-cdn",     null,          ".squarespace.com",95, 0),

            // JS Frameworks
            new TechSignature("React",         "JS Framework", null,            null,               "__REACT_DEVTOOLS|data-reactroot|_reactFiber", null, null, 80, 1),
            new TechSignature("Vue.js",        "JS Framework", null,            null,               "data-v-|__vue__",     null,          null,              80, 0),
            new TechSignature("Angular",       "JS Framework", null,            null,               "ng-version|ng-app",   null,          null,              80, 1),
            new TechSignature("Next.js",       "JS Framework", "X-Powered-By",  "Next\\.js",       "__NEXT_DATA__",       null,          "/_next/",         92, 0),
            new TechSignature("Nuxt.js",       "JS Framework", null,            null,               "__NUXT__",            null,          "/_nuxt/",         90, 0),
            new TechSignature("Svelte",        "JS Framework", null,            null,               "svelte-",             null,          null,              70, 0),
            new TechSignature("Gatsby",        "JS Framework", null,            null,               "gatsby-image|___gatsby", null,       "/gatsby/",        85, 0),

            // Backend Frameworks
            new TechSignature("Spring Boot",   "Backend",      "X-Application-Context", null,       null,                  "JSESSIONID",  null,              80, 0),
            new TechSignature("Django",        "Backend",      null,            null,               "csrfmiddlewaretoken", "csrftoken",   null,              85, 2),
            new TechSignature("Laravel",       "Backend",      null,            null,               "laravel_token",       "laravel_session", null,          85, 3),
            new TechSignature("Rails",         "Backend",      null,            null,               "_rails_app",          "_session_id", null,              80, 4),

            // CDN / WAF
            new TechSignature("Cloudflare",    "CDN",          "CF-RAY",        null,               null,                  "__cf_bm|cf_clearance", null,   98, 0),
            new TechSignature("Fastly",        "CDN",          "X-Served-By",   "cache-",           null,                  null,          null,              85, 0),
            new TechSignature("AWS CloudFront","CDN",          "X-Amz-Cf-Id",   null,               null,                  null,          ".cloudfront.net", 97, 2),
            new TechSignature("Akamai",        "CDN",          "X-Check-Cacheable", null,           null,                  "ak_bmsc",     ".edgesuite.net",  90, 1),
            new TechSignature("Imperva/Incapsula","WAF",       "X-Iinfo",       null,               null,                  "incap_ses",   null,              95, 0),
            new TechSignature("ModSecurity",   "WAF",          "Server",        "mod_security",     null,                  null,          null,              90, 0),

            // Analytics
            new TechSignature("Google Analytics","Analytics",  null,            null,               "google-analytics\\.com|gtag|UA-[0-9]|G-[A-Z0-9]", null, null, 90, 0),
            new TechSignature("Hotjar",        "Analytics",    null,            null,               "hotjar\\.com|hjid",   "_hjid",       null,              90, 0),
            new TechSignature("Mixpanel",      "Analytics",    null,            null,               "mixpanel\\.com",      "mp_",         null,              88, 0),
            new TechSignature("Segment",       "Analytics",    null,            null,               "segment\\.com|analytics\\.js", null, null,             88, 0),

            // Databases (exposed via errors/headers)
            new TechSignature("MySQL",         "Database",     null,            null,               "mysql_num_rows|MySQL Query", null,   null,              70, 5),
            new TechSignature("MongoDB",       "Database",     null,            null,               "MongoError",          null,          null,              75, 3),

            // Security Tools
            new TechSignature("reCAPTCHA",     "Security",     null,            null,               "recaptcha|grecaptcha", null,         null,              92, 0),
            new TechSignature("Cloudflare Turnstile","Security",null,           null,               "challenges.cloudflare.com", null,    null,              95, 0)
    );

    // ── Public API ─────────────────────────────────────────────────────────────

    public List<TechStackItem> detect(String targetUrl) {
        List<TechStackItem> results = new ArrayList<>();

        try {
            DetectionContext ctx = fetchPage(targetUrl);
            if (ctx == null) return results;

            for (TechSignature sig : SIGNATURES) {
                int confidence = 0;
                String detectedVersion = "Latest";

                // Header match
                if (sig.headerKey != null && sig.headerPattern != null) {
                    String headerVal = ctx.headers.getOrDefault(sig.headerKey.toLowerCase(), "");
                    if (match(headerVal, sig.headerPattern)) {
                        confidence += sig.baseConfidence;
                        detectedVersion = extractVersion(headerVal);
                    }
                }

                // HTML body match
                if (sig.htmlPattern != null && match(ctx.body, sig.htmlPattern)) {
                    confidence = Math.min(100, confidence + (confidence > 0 ? 10 : sig.baseConfidence));
                }

                // Cookie match
                if (sig.cookiePattern != null) {
                    String cookies = ctx.headers.getOrDefault("set-cookie", "");
                    if (match(cookies, sig.cookiePattern)) {
                        confidence = Math.min(100, confidence + (confidence > 0 ? 8 : sig.baseConfidence));
                    }
                }

                // URL pattern match
                if (sig.urlPattern != null && ctx.body.contains(sig.urlPattern)) {
                    confidence = Math.min(100, confidence + (confidence > 0 ? 12 : sig.baseConfidence));
                }

                if (confidence > 0) {
                    TechStackItem item = new TechStackItem();
                    item.setName(sig.name);
                    item.setLayer(sig.layer);
                    item.setVersion(detectedVersion);
                    item.setConfidence(confidence);
                    item.setCveCount(sig.knownCves);
                    item.setHasCveExposure(sig.knownCves > 0);
                    results.add(item);
                }
            }

            // Deduplicate by name, keep highest confidence
            Map<String, TechStackItem> deduped = new LinkedHashMap<>();
            for (TechStackItem item : results) {
                deduped.merge(item.getName(), item,
                        (a, b) -> a.getConfidence() >= b.getConfidence() ? a : b);
            }

            return deduped.values().stream()
                    .sorted(Comparator.comparingInt(TechStackItem::getConfidence).reversed())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Tech detection failed for {}: {}", targetUrl, e.getMessage());
            return results;
        }
    }

    // ── HTTP Fetch ─────────────────────────────────────────────────────────────

    private DetectionContext fetchPage(String targetUrl) {
        try {
            String url = targetUrl.startsWith("http") ? targetUrl : "https://" + targetUrl;
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(10_000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (compatible; WhylockBot/2.0; +https://whylock.ai/bot)");
            conn.setInstanceFollowRedirects(true);
            conn.connect();

            // Collect headers (case-insensitive, collect Set-Cookie as concat)
            Map<String, String> headers = new LinkedHashMap<>();
            conn.getHeaderFields().forEach((k, v) -> {
                if (k != null) headers.put(k.toLowerCase(), String.join("; ", v));
            });

            // Read body (first 200 KB is enough for fingerprinting)
            StringBuilder body = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                char[] buf = new char[204_800];
                int read = br.read(buf, 0, buf.length);
                if (read > 0) body.append(buf, 0, read);
            } catch (Exception ignored) {}

            conn.disconnect();
            return new DetectionContext(headers, body.toString());

        } catch (Exception e) {
            log.warn("Page fetch failed: {}", e.getMessage());
            return null;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean match(String input, String pattern) {
        if (input == null || pattern == null) return false;
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(input).find();
    }

    private String extractVersion(String headerValue) {
        // Try to extract version like "nginx/1.24.0" or "PHP/8.1.2"
        Matcher m = Pattern.compile("/(\\d+\\.\\d+\\.?\\d*)").matcher(headerValue);
        return m.find() ? m.group(1) : "Detected";
    }

    // ── Inner Types ───────────────────────────────────────────────────────────

    private record DetectionContext(Map<String, String> headers, String body) {}

    private record TechSignature(
            String name, String layer,
            String headerKey, String headerPattern,
            String htmlPattern, String cookiePattern, String urlPattern,
            int baseConfidence, int knownCves) {}
}