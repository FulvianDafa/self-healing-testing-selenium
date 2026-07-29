package com.fulvian.healing;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data class yang merekam data tiap kandidat yang dievaluasi saat self-healing.
 * Diisi oleh HealingDriver, dikirim ke HealingLogger untuk disimpan ke CSV terpisah.
 */
public class CandidateResult {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public final String timestamp;
    public final String testCaseId;
    public final String originalLocator;
    public final String candidateDescription;
    public final double textScore;
    public final double locatorScore;
    public final double positionScore;
    public final double combinedScore;
    public final String status; // misal: "EVALUATED", "SKIPPED_INCOMPATIBLE"

    public CandidateResult(String testCaseId,
                           String originalLocator,
                           String candidateDescription,
                           double textScore,
                           double locatorScore,
                           double positionScore,
                           double combinedScore,
                           String status) {
        this.timestamp = LocalDateTime.now().format(FMT);
        this.testCaseId = testCaseId;
        this.originalLocator = originalLocator;
        this.candidateDescription = candidateDescription == null ? "-" : 
                candidateDescription.replaceAll("[\\r\\n]+", " ");
        this.textScore = textScore;
        this.locatorScore = locatorScore;
        this.positionScore = positionScore;
        this.combinedScore = combinedScore;
        this.status = status;
    }

    public static String csvHeader() {
        return "timestamp,test_case_id,original_locator,candidate_description," +
               "status,text_score,locator_score,position_score,combined_score";
    }

    public String toCsvRow() {
        return String.join(",",
                wrap(timestamp),
                wrap(testCaseId),
                wrap(originalLocator),
                wrap(candidateDescription),
                wrap(status),
                String.format("%.4f", textScore),
                String.format("%.4f", locatorScore),
                String.format("%.4f", positionScore),
                String.format("%.4f", combinedScore)
        );
    }

    private static String wrap(String val) {
        if (val == null) return "\"\"";
        return "\"" + val.replace("\"", "'") + "\"";
    }
}
