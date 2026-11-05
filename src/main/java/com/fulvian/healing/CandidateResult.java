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
    public final String attemptId;
    public final String testCaseId;
    public final String sut;
    public final String originalLocator;
    public final String candidateIdentifier;
    public final String candidateDescription;
    public final Double textScore;
    public final Double locatorScore;
    public final Double positionScore;
    public final boolean positionAvailable;
    public final Double combinedScore;
    public final String status; // misal: "EVALUATED", "SKIPPED_INCOMPATIBLE"
    public final boolean selected;
    public final boolean groundTruthAvailable;
    public final Boolean isTarget;

    public CandidateResult(String attemptId,
                           String testCaseId,
                           String sut,
                           String originalLocator,
                           String candidateIdentifier,
                           String candidateDescription,
                           Double textScore,
                           Double locatorScore,
                           Double positionScore,
                           boolean positionAvailable,
                           Double combinedScore,
                           String status,
                           boolean selected,
                           boolean groundTruthAvailable,
                           Boolean isTarget) {
        this.timestamp = LocalDateTime.now().format(FMT);
        this.attemptId = attemptId;
        this.testCaseId = testCaseId;
        this.sut = sut;
        this.originalLocator = originalLocator;
        this.candidateIdentifier = candidateIdentifier;
        this.candidateDescription = candidateDescription == null ? "-" : 
                candidateDescription.replaceAll("[\\r\\n]+", " ");
        this.textScore = textScore;
        this.locatorScore = locatorScore;
        this.positionScore = positionScore;
        this.positionAvailable = positionAvailable;
        this.combinedScore = combinedScore;
        this.status = status;
        this.selected = selected;
        this.groundTruthAvailable = groundTruthAvailable;
        this.isTarget = groundTruthAvailable ? Boolean.TRUE.equals(isTarget) : null;
    }

    public static String csvHeader() {
        return "timestamp,attempt_id,test_case_id,sut,original_locator," +
               "candidate_identifier,candidate_description,status," +
               "text_score,locator_score,position_score,position_available," +
               "combined_score,selected,ground_truth_available,is_target";
    }

    public String toCsvRow() {
        return String.join(",",
                wrap(timestamp),
                wrap(attemptId),
                wrap(testCaseId),
                wrap(sut),
                wrap(originalLocator),
                wrap(candidateIdentifier),
                wrap(candidateDescription),
                wrap(status),
                formatScore(textScore),
                formatScore(locatorScore),
                formatScore(positionScore),
                String.valueOf(positionAvailable),
                formatScore(combinedScore),
                String.valueOf(selected),
                String.valueOf(groundTruthAvailable),
                isTarget == null ? "" : String.valueOf(isTarget)
        );
    }

    private static String formatScore(Double value) {
        return value == null ? "" : String.format(java.util.Locale.ROOT, "%.4f", value);
    }

    private static String wrap(String val) {
        if (val == null) return "\"\"";
        return "\"" + val.replace("\"", "'") + "\"";
    }
}
