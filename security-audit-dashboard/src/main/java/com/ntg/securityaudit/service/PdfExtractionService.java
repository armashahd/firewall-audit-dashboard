package com.ntg.securityaudit.service;

import com.ntg.securityaudit.dto.ParsedAuditReport;
import com.ntg.securityaudit.dto.ParsedFinding;
import com.ntg.securityaudit.enums.Severity;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfExtractionService {

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)
    );

    public ParsedAuditReport extract(File file) throws IOException {
        String text = extractText(file);
        ParsedAuditReport report = new ParsedAuditReport();
        populateMetadata(text, report);
        report.setFindings(parseFindings(text));
        return report;
    }

    private String extractText(File file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private void populateMetadata(String text, ParsedAuditReport report) {
        String location = firstValue(text, "Location");
        if (location != null) {
            String[] parts = location.split("\\s+", 2);
            report.setCountry(parts[0]);
            if (parts.length > 1) {
                report.setSiteName(parts[1]);
            }
        }

        report.setSiteName(firstNonBlank(report.getSiteName(), firstValue(text, "Site Name", "Site")));
        report.setCountry(firstNonBlank(report.getCountry(), firstValue(text, "Country")));
        report.setReportVersion(firstNonBlank(firstValue(text, "Version"), latestSummaryVersion(text)));
        report.setAuditor(firstNonBlank(firstValue(text, "Auditor", "Assessor", "Prepared By"), parseAuthor(text)));
        report.setAuditDate(firstNonNull(parseDate(firstValue(text, "Audit Date", "Report Date", "Scanned Date")), parseDate(firstValue(text, "Issue Date"))));
        report.setAssessmentDate(firstNonNull(parseDate(firstValue(text, "Assessment Date", "Assessment", "Scanned Date")), report.getAuditDate()));
        populateScope(text, report);
        populateCounts(text, report);
    }

    private List<ParsedFinding> parseFindings(String text) {
        List<ParsedFinding> reportFindings = new ArrayList<>();
        reportFindings.addAll(parseNumberedFindings(section(text, "2.10 Host By Vulnerabilities", "2.11 Host by Compliances"), "Vulnerability"));
        reportFindings.addAll(parseNumberedFindings(section(text, "2.11 Host by Compliances", "3 Document Acceptance Certificate"), "Compliance"));
        if (!reportFindings.isEmpty()) {
            return reportFindings;
        }

        List<ParsedFinding> findings = new ArrayList<>();
        Matcher matcher = Pattern.compile("(?is)(Vulnerability|Compliance)\\s*(?:Section|Finding|Check|Item)?\\s*[:#-]?\\s*(.*?)(?=\\n\\s*(?:Vulnerability|Compliance)\\s*(?:Section|Finding|Check|Item)?\\s*[:#-]?|\\z)").matcher(text);
        while (matcher.find()) {
            String category = clean(matcher.group(1));
            String block = matcher.group(2);
            ParsedFinding finding = new ParsedFinding();
            finding.setCategory("Vulnerability".equalsIgnoreCase(category) ? "Vulnerability" : "Compliance");
            finding.setTitle(firstNonBlank(firstValue(block, "Title", "Finding", "Control", "Check"), firstMeaningfulLine(block)));
            finding.setSeverity(parseSeverity(firstValue(block, "Severity", "Risk", "Priority")));
            finding.setDescription(firstNonBlank(firstValue(block, "Description", "Observation", "Details"), truncate(block, 2000)));
            finding.setImpact(firstValue(block, "Impact", "Risk Impact", "Business Impact"));
            finding.setRecommendation(firstNonBlank(firstValue(block, "Recommendation", "Remediation", "Fix", "Action"), "Review and remediate according to firewall security standards."));
            if (finding.getSeverity() == null) {
                finding.setSeverity("Compliance".equals(finding.getCategory()) ? Severity.MEDIUM : Severity.LOW);
            }
            if (finding.getTitle() != null && !finding.getTitle().isBlank()) {
                findings.add(finding);
            }
        }
        return findings;
    }

    private void populateScope(String text, ParsedAuditReport report) {
        String scope = section(text, "2.5 Scope", "2.6 Report Summary");
        if (scope == null) {
            return;
        }

        report.setIpAddress(firstNonBlank(report.getIpAddress(), firstMatch(scope, "(\\d{1,3}(?:\\.\\d{1,3}){3})")));
        String[] lines = nonEmptyLines(scope);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.matches("\\d{1,3}(?:\\.\\d{1,3}){3}") && i > 0) {
                String hostname = lines[i - 1];
                if (hostname.length() <= 6 && i > 1 && lines[i - 2].endsWith("-")) {
                    hostname = lines[i - 2] + hostname;
                }
                report.setHostname(hostname);
            }
            if (line.toLowerCase(Locale.ENGLISH).contains("fortigate")) {
                Matcher matcher = Pattern.compile("(?i)(FortiGate)\\s+(FortiGate\\s+\\S+)").matcher(line);
                if (matcher.find()) {
                    report.setDeviceType(matcher.group(1));
                    report.setDeviceModel(matcher.group(2));
                    report.setVendor("Fortinet");
                }
            }
        }
    }

    private void populateCounts(String text, ParsedAuditReport report) {
        String summary = section(text, "2.6 Report Summary", "2.7 Vulnerability Risk Calculation");
        Matcher severityMatcher = summary != null
                ? Pattern.compile("(\\d{1,3}(?:\\.\\d{1,3}){3})\\s+(-|\\d+)\\s+(-|\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)").matcher(summary)
                : Pattern.compile("$^").matcher("");
        if (severityMatcher.find()) {
            report.setCriticalCount(dashToZero(severityMatcher.group(2)));
            report.setHighCount(dashToZero(severityMatcher.group(3)));
            report.setMediumCount(dashToZero(severityMatcher.group(4)));
            report.setLowCount(dashToZero(severityMatcher.group(5)));
            report.setInfoCount(dashToZero(severityMatcher.group(6)));
        }

        report.setPassedComplianceCount(firstNonNull(parseInteger(firstMatch(text, "(?is)Passed\\s*(\\d+)")), parseInteger(firstValue(text, "Passed Compliance Count", "Passed Compliance"))));
        report.setFailedComplianceCount(firstNonNull(parseInteger(firstMatch(text, "(?is)Failed\\s*(\\d+)")), parseInteger(firstValue(text, "Failed Compliance Count", "Failed Compliance"))));
        Double riskScore = parseDecimal(firstMatch(text, "(?is)Risk Score for the Assets:.*?(\\d+(?:\\.\\d+)?)\\s*%"));
        if (riskScore != null) {
            report.setRiskScore((int) Math.round(riskScore));
        }
    }

    private List<ParsedFinding> parseNumberedFindings(String section, String category) {
        List<ParsedFinding> findings = new ArrayList<>();
        if (section == null || section.isBlank()) {
            return findings;
        }

        Matcher matcher = Pattern.compile("(?ms)^\\s*(\\d{2})\\s+(.+?)(?=^\\s*\\d{2}\\s+|\\z)").matcher(section);
        while (matcher.find()) {
            ParsedFinding finding = parseNumberedBlock(matcher.group(2), category);
            if (finding.getTitle() != null && !finding.getTitle().isBlank()) {
                findings.add(finding);
            }
        }
        return findings;
    }

    private ParsedFinding parseNumberedBlock(String block, String category) {
        ParsedFinding finding = new ParsedFinding();
        finding.setCategory(category);
        finding.setSeverity("Compliance".equals(category) ? Severity.MEDIUM : parseSeverity(block));
        finding.setTitle(parseTitle(block, category, finding.getSeverity()));
        finding.setDescription(firstNonBlank(between(block, "Description", "Impact"), truncate(block, 2000)));
        finding.setImpact(between(block, "Impact", firstPresent(block, "PoC", "Remediation")));
        finding.setRecommendation(firstNonBlank(after(block, "Remediation"), "Review and remediate according to firewall security standards."));
        if (finding.getSeverity() == null) {
            finding.setSeverity(Severity.LOW);
        }
        return finding;
    }

    private String parseTitle(String block, String category, Severity severity) {
        int end = indexOfIgnoreCase(block, "Description");
        String header = end >= 0 ? block.substring(0, end) : block;
        if ("Compliance".equals(category)) {
            header = header.replaceFirst("(?is)\\b(Failed|Passed)\\b.*", "");
        } else if (severity != null) {
            header = header.replaceFirst("(?is)\\b" + severity.name() + "\\b.*", "");
        }
        return clean(header);
    }

    private String firstValue(String text, String... labels) {
        for (String label : labels) {
            Matcher matcher = Pattern.compile("(?im)^\\s*" + Pattern.quote(label) + "\\s*[:=-]?\\s+(.+?)\\s*$").matcher(text);
            if (matcher.find()) {
                return clean(matcher.group(1));
            }
        }
        return null;
    }

    private String latestSummaryVersion(String text) {
        Matcher matcher = Pattern.compile("(?m)^\\s*\\d{2}/\\d{2}/\\d{4}\\s+(\\d+(?:\\.\\d+)?)\\s+").matcher(text);
        String version = null;
        while (matcher.find()) {
            version = matcher.group(1);
        }
        return version;
    }

    private String parseAuthor(String text) {
        Matcher matcher = Pattern.compile("(?is)N-Able\\s*\\(Pvt\\)\\s*Ltd\\s+([A-Za-z]+)\\s+([A-Za-z]+)\\s+Engineer").matcher(text);
        return matcher.find() ? matcher.group(1) + " " + matcher.group(2) : null;
    }

    private String section(String text, String startMarker, String endMarker) {
        int start = text.lastIndexOf(startMarker);
        if (start < 0) {
            return null;
        }
        int end = endMarker != null ? text.indexOf(endMarker, start + startMarker.length()) : -1;
        return text.substring(start, end > start ? end : text.length());
    }

    private String[] nonEmptyLines(String text) {
        return text.lines()
                .map(this::clean)
                .filter(line -> line != null && !line.isBlank())
                .toArray(String[]::new);
    }

    private String firstMatch(String text, String regex) {
        if (text == null) {
            return null;
        }
        Matcher matcher = Pattern.compile(regex).matcher(text);
        return matcher.find() ? clean(matcher.group(1)) : null;
    }

    private Integer dashToZero(String value) {
        return "-".equals(value) ? 0 : parseInteger(value);
    }

    private Double parseDecimal(String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(value);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : null;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String cleaned = value.replaceAll("(?i)(st|nd|rd|th)", "").trim();
        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                return LocalDate.parse(cleaned, formatter);
            } catch (DateTimeParseException ignored) {
                // try the next supported format
            }
        }
        return null;
    }

    private Integer parseInteger(String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = Pattern.compile("(\\d+)").matcher(value);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    private Severity parseSeverity(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.toUpperCase(Locale.ENGLISH);
        if (normalized.contains("CRITICAL")) {
            return Severity.CRITICAL;
        }
        if (normalized.contains("HIGH")) {
            return Severity.HIGH;
        }
        if (normalized.contains("MEDIUM")) {
            return Severity.MEDIUM;
        }
        if (normalized.contains("LOW") || normalized.contains("INFO")) {
            return Severity.LOW;
        }
        return null;
    }

    private String between(String block, String startLabel, String endLabel) {
        int start = indexOfIgnoreCase(block, startLabel);
        if (start < 0) {
            return null;
        }
        start += startLabel.length();
        int end = endLabel != null ? indexOfIgnoreCase(block, endLabel, start) : -1;
        return clean(block.substring(start, end > start ? end : block.length()));
    }

    private String after(String block, String label) {
        int start = indexOfIgnoreCase(block, label);
        return start >= 0 ? clean(block.substring(start + label.length())) : null;
    }

    private String firstPresent(String block, String... labels) {
        for (String label : labels) {
            if (indexOfIgnoreCase(block, label) >= 0) {
                return label;
            }
        }
        return null;
    }

    private int indexOfIgnoreCase(String value, String search) {
        return indexOfIgnoreCase(value, search, 0);
    }

    private int indexOfIgnoreCase(String value, String search, int fromIndex) {
        return value.toLowerCase(Locale.ENGLISH).indexOf(search.toLowerCase(Locale.ENGLISH), fromIndex);
    }

    private String firstMeaningfulLine(String block) {
        for (String line : block.split("\\R")) {
            String cleaned = clean(line);
            if (cleaned != null && cleaned.length() > 3 && !cleaned.contains(":")) {
                return cleaned;
            }
        }
        return null;
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private <T> T firstNonNull(T first, T second) {
        return first != null ? first : second;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String cleaned = clean(value);
        return cleaned != null && cleaned.length() > maxLength ? cleaned.substring(0, maxLength) : cleaned;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        return value.replace('\u00a0', ' ').replaceAll("[ \\t]+", " ").trim();
    }
}
