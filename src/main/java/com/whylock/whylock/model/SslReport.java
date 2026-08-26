package com.whylock.whylock.model;

import java.util.List;

public class SslReport {
    private String certificateAuthority;
    private boolean caValid;
    private String certExpiry;
    private int daysUntilExpiry;
    private boolean tls13Supported;
    private boolean tls12Supported;
    private boolean legacyTlsEnabled;
    private String hstsValue;
    private boolean hstsEnabled;
    private boolean certPinned;
    private int ctLogCount;
    private String cipherSuite;
    private boolean cipherStrong;
    private String targetUrl;
    private String host;
    private String sslGrade;
    private boolean scanSuccess;
    private String errorMessage;
    private String fullIssuerDn;
    private int chainDepth;
    private String certSubject;
    private boolean certExpired;
    private boolean certExpiringSoon;
    private String certSerialNumber;
    private String signatureAlgorithm;
    private List<String> subjectAltNames;
    private boolean wildcardCert;
    private boolean selfSigned;
    private boolean tls10Supported;
    private boolean tls11Supported;
    private boolean hstsIncludesSubdomains;
    private boolean hstsPreload;
    private long hstsMaxAge;
    private boolean ctCompliant;
    private boolean contentSecurityPolicy;
    private boolean xFrameOptions;
    private boolean xContentTypeOptions;

    public SslReport() {}

    public SslReport(String certificateAuthority, boolean caValid, String certExpiry, 
                     int daysUntilExpiry, boolean tls13Supported, boolean tls12Supported,
                     boolean legacyTlsEnabled, String hstsValue, boolean hstsEnabled,
                     boolean certPinned, int ctLogCount, String cipherSuite, boolean cipherStrong) {
        this.certificateAuthority = certificateAuthority;
        this.caValid = caValid;
        this.certExpiry = certExpiry;
        this.daysUntilExpiry = daysUntilExpiry;
        this.tls13Supported = tls13Supported;
        this.tls12Supported = tls12Supported;
        this.legacyTlsEnabled = legacyTlsEnabled;
        this.hstsValue = hstsValue;
        this.hstsEnabled = hstsEnabled;
        this.certPinned = certPinned;
        this.ctLogCount = ctLogCount;
        this.cipherSuite = cipherSuite;
        this.cipherStrong = cipherStrong;
    }

    public String getCertificateAuthority() { return certificateAuthority; }
    public void setCertificateAuthority(String certificateAuthority) { this.certificateAuthority = certificateAuthority; }

    public boolean isCaValid() { return caValid; }
    public void setCaValid(boolean caValid) { this.caValid = caValid; }

    public String getCertExpiry() { return certExpiry; }
    public void setCertExpiry(String certExpiry) { this.certExpiry = certExpiry; }

    public int getDaysUntilExpiry() { return daysUntilExpiry; }
    public void setDaysUntilExpiry(int daysUntilExpiry) { this.daysUntilExpiry = daysUntilExpiry; }

    public boolean isTls13Supported() { return tls13Supported; }
    public void setTls13Supported(boolean tls13Supported) { this.tls13Supported = tls13Supported; }

    public boolean isTls12Supported() { return tls12Supported; }
    public void setTls12Supported(boolean tls12Supported) { this.tls12Supported = tls12Supported; }

    public boolean isLegacyTlsEnabled() { return legacyTlsEnabled; }
    public void setLegacyTlsEnabled(boolean legacyTlsEnabled) { this.legacyTlsEnabled = legacyTlsEnabled; }

    public String getHstsValue() { return hstsValue; }
    public void setHstsValue(String hstsValue) { this.hstsValue = hstsValue; }

    public boolean isHstsEnabled() { return hstsEnabled; }
    public void setHstsEnabled(boolean hstsEnabled) { this.hstsEnabled = hstsEnabled; }

    public boolean isCertPinned() { return certPinned; }
    public void setCertPinned(boolean certPinned) { this.certPinned = certPinned; }

    public int getCtLogCount() { return ctLogCount; }
    public void setCtLogCount(int ctLogCount) { this.ctLogCount = ctLogCount; }

    public String getCipherSuite() { return cipherSuite; }
    public void setCipherSuite(String cipherSuite) { this.cipherSuite = cipherSuite; }

    public boolean isCipherStrong() { return cipherStrong; }
    public void setCipherStrong(boolean cipherStrong) { this.cipherStrong = cipherStrong; }

    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public String getSslGrade() { return sslGrade; }
    public void setSslGrade(String sslGrade) { this.sslGrade = sslGrade; }

    public boolean isScanSuccess() { return scanSuccess; }
    public void setScanSuccess(boolean scanSuccess) { this.scanSuccess = scanSuccess; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getFullIssuerDn() { return fullIssuerDn; }
    public void setFullIssuerDn(String fullIssuerDn) { this.fullIssuerDn = fullIssuerDn; }

    public int getChainDepth() { return chainDepth; }
    public void setChainDepth(int chainDepth) { this.chainDepth = chainDepth; }

    public String getCertSubject() { return certSubject; }
    public void setCertSubject(String certSubject) { this.certSubject = certSubject; }

    public boolean isCertExpired() { return certExpired; }
    public void setCertExpired(boolean certExpired) { this.certExpired = certExpired; }

    public boolean isCertExpiringSoon() { return certExpiringSoon; }
    public void setCertExpiringSoon(boolean certExpiringSoon) { this.certExpiringSoon = certExpiringSoon; }

    public String getCertSerialNumber() { return certSerialNumber; }
    public void setCertSerialNumber(String certSerialNumber) { this.certSerialNumber = certSerialNumber; }

    public String getSignatureAlgorithm() { return signatureAlgorithm; }
    public void setSignatureAlgorithm(String signatureAlgorithm) { this.signatureAlgorithm = signatureAlgorithm; }

    public List<String> getSubjectAltNames() { return subjectAltNames; }
    public void setSubjectAltNames(List<String> subjectAltNames) { this.subjectAltNames = subjectAltNames; }

    public boolean isWildcardCert() { return wildcardCert; }
    public void setWildcardCert(boolean wildcardCert) { this.wildcardCert = wildcardCert; }

    public boolean isSelfSigned() { return selfSigned; }
    public void setSelfSigned(boolean selfSigned) { this.selfSigned = selfSigned; }

    public boolean isTls10Supported() { return tls10Supported; }
    public void setTls10Supported(boolean tls10Supported) { this.tls10Supported = tls10Supported; }

    public boolean isTls11Supported() { return tls11Supported; }
    public void setTls11Supported(boolean tls11Supported) { this.tls11Supported = tls11Supported; }

    public boolean isHstsIncludesSubdomains() { return hstsIncludesSubdomains; }
    public void setHstsIncludesSubdomains(boolean hstsIncludesSubdomains) { this.hstsIncludesSubdomains = hstsIncludesSubdomains; }

    public boolean isHstsPreload() { return hstsPreload; }
    public void setHstsPreload(boolean hstsPreload) { this.hstsPreload = hstsPreload; }

    public long getHstsMaxAge() { return hstsMaxAge; }
    public void setHstsMaxAge(long hstsMaxAge) { this.hstsMaxAge = hstsMaxAge; }

    public boolean isCtCompliant() { return ctCompliant; }
    public void setCtCompliant(boolean ctCompliant) { this.ctCompliant = ctCompliant; }

    public boolean isContentSecurityPolicy() { return contentSecurityPolicy; }
    public void setContentSecurityPolicy(boolean contentSecurityPolicy) { this.contentSecurityPolicy = contentSecurityPolicy; }

    public boolean isXFrameOptions() { return xFrameOptions; }
    public void setXFrameOptions(boolean xFrameOptions) { this.xFrameOptions = xFrameOptions; }

    public boolean isXContentTypeOptions() { return xContentTypeOptions; }
    public void setXContentTypeOptions(boolean xContentTypeOptions) { this.xContentTypeOptions = xContentTypeOptions; }
}
