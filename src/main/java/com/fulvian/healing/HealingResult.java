package com.fulvian.healing;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data class yang merekam satu event healing.
 * Diisi oleh HealingDriver, dikirim ke HealingLogger untuk disimpan ke CSV.
 *
 * Setiap baris di healing_log.csv = satu objek HealingResult.
 */
public class HealingResult {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // -------------------------------------------------------
    // Field — urutan ini sama dengan kolom di CSV
    // -------------------------------------------------------
    public final String  timestamp;
    public final String  testCaseId;        // misal: "TC-01"
    public final String  scenarioType;      // "id_change", "class_change", "xpath_change", "css_change"
    public final String  originalLocator;   // misal: "By.id('inputProductBtn')"
    public final String  status;            // "SUCCESS" atau "FAIL"
    public final double  textScore;         // skor komponen teks saja
    public final double  locatorScore;      // skor komponen locator saja
    public final double  positionScore;     // skor komponen posisi saja
    public final double  combinedScore;     // skor gabungan tertimbang
    public final String  selectedElement;   // outerHTML elemen yang dipilih (dipotong 100 char)
    public final long    healingTimeMs;     // durasi proses healing dalam milidetik
    public final boolean isFalsePositive;   // diisi manual setelah verifikasi visual

    // -------------------------------------------------------
    // Constructor
    // -------------------------------------------------------
    public HealingResult(String  testCaseId,
                         String  scenarioType,
                         String  originalLocator,
                         String  status,
                         double  textScore,
                         double  locatorScore,
                         double  positionScore,
                         double  combinedScore,
                         String  selectedElement,
                         long    healingTimeMs,
                         boolean isFalsePositive) {

        this.timestamp       = LocalDateTime.now().format(FMT);
        this.testCaseId      = testCaseId;
        this.scenarioType    = scenarioType;
        this.originalLocator = originalLocator;
        this.status          = status;
        this.textScore       = textScore;
        this.locatorScore    = locatorScore;
        this.positionScore   = positionScore;
        this.combinedScore   = combinedScore;
        // Potong outerHTML agar tidak terlalu panjang di CSV
        this.selectedElement = selectedElement == null ? "-"
                : selectedElement.replaceAll("[\\r\\n]+", " ")
                                  .substring(0, Math.min(selectedElement.length(), 150));
        this.healingTimeMs   = healingTimeMs;
        this.isFalsePositive = isFalsePositive;
    }

    /** Header kolom CSV — urutan sama dengan toCsvRow(). */
    public static String csvHeader() {
        return "timestamp,test_case_id,scenario_type,original_locator," +
               "status,text_score,locator_score,position_score,combined_score," +
               "selected_element,healing_time_ms,is_false_positive";
    }

    /** Konversi ke satu baris CSV. */
    public String toCsvRow() {
        return String.join(",",
                wrap(timestamp),
                wrap(testCaseId),
                wrap(scenarioType),
                wrap(originalLocator),
                wrap(status),
                String.format("%.4f", textScore),
                String.format("%.4f", locatorScore),
                String.format("%.4f", positionScore),
                String.format("%.4f", combinedScore),
                wrap(selectedElement),
                String.valueOf(healingTimeMs),
                String.valueOf(isFalsePositive)
        );
    }

    /** Bungkus nilai dengan tanda kutip agar aman di CSV (handle koma di dalam nilai). */
    private static String wrap(String val) {
        if (val == null) return "\"\"";
        return "\"" + val.replace("\"", "'") + "\"";
    }

    @Override
    public String toString() {
        return String.format("[%s] TC=%s | Status=%s | Score=%.4f | Time=%dms | FP=%s",
                timestamp, testCaseId, status, combinedScore, healingTimeMs, isFalsePositive);
    }
}
