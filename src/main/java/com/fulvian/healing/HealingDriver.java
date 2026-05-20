package com.fulvian.healing;

import org.openqa.selenium.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================
 * HealingDriver — Self-Healing Wrapper untuk Selenium WebDriver
 * ============================================================
 *
 * Cara kerja:
 *   1. Coba temukan elemen dengan locator asli (normal Selenium)
 *   2. Kalau gagal (NoSuchElementException) → aktifkan mekanisme healing
 *   3. Kumpulkan semua elemen interaktif yang ada di DOM saat ini
 *   4. Hitung skor similarity gabungan untuk setiap kandidat
 *   5. Pilih kandidat dengan skor tertinggi yang melebihi threshold
 *   6. Catat hasilnya ke CSV via HealingLogger
 *   7. Return elemen yang dipilih (atau throw exception kalau healing gagal)
 *
 * Digunakan di test: HealingDriver hd = new HealingDriver(driver, "TC-01");
 *                    WebElement btn = hd.findElement(By.id("..."), profile);
 */
public class HealingDriver {

    private final WebDriver     driver;
    private final String        testCaseId;     // misal: "TC-01"
    private final String        scenarioType;   // misal: "id_change"
    private final double        threshold;      // ambang batas skor minimum

    // Tag HTML yang dicari saat healing — bisa diperluas sesuai kebutuhan
    private static final String[] INTERACTIVE_TAGS = {
            "button", "a", "input", "select", "textarea"
    };

    // -------------------------------------------------------
    // Constructor
    // -------------------------------------------------------

    /**
     * Constructor lengkap dengan threshold kustom.
     *
     * @param driver       WebDriver aktif
     * @param testCaseId   ID test case, misal "TC-01" (muncul di CSV)
     * @param scenarioType jenis skenario DOM, misal "id_change"
     * @param threshold    ambang batas skor minimum (0.0–1.0)
     */
    public HealingDriver(WebDriver driver,
                         String   testCaseId,
                         String   scenarioType,
                         double   threshold) {
        this.driver       = driver;
        this.testCaseId   = testCaseId;
        this.scenarioType = scenarioType;
        this.threshold    = threshold;
    }

    /** Constructor dengan threshold default (0.50). */
    public HealingDriver(WebDriver driver,
                         String   testCaseId,
                         String   scenarioType) {
        this(driver, testCaseId, scenarioType, SimilarityEngine.DEFAULT_THRESHOLD);
    }

    /** Constructor ringkas — untuk backward compatibility dengan kode lama. */
    public HealingDriver(WebDriver driver) {
        this(driver, "TC-00", "unknown", SimilarityEngine.DEFAULT_THRESHOLD);
    }

    // -------------------------------------------------------
    // METHOD UTAMA: findElement dengan healing
    // -------------------------------------------------------

    /**
     * Cari elemen dengan locator. Kalau gagal, aktifkan self-healing.
     *
     * @param locator locator Selenium (By.id, By.xpath, dll)
     * @param profile profil elemen target (teks, locator asli, posisi terakhir)
     * @return WebElement yang ditemukan (asli atau hasil healing)
     * @throws NoSuchElementException jika locator asli gagal DAN healing gagal
     */
    public WebElement findElement(By locator, ElementProfile profile) {

        // ── Tahap 1: Coba locator asli ──────────────────────────────────
        try {
            WebElement el = driver.findElement(locator);
            System.out.printf("[HealingDriver] ✓ Locator asli berhasil: %s%n", locator);
            return el;
        } catch (NoSuchElementException e) {
            System.out.printf("[HealingDriver] ✗ Locator gagal: %s%n", locator);
            System.out.println("[HealingDriver] → Mengaktifkan mekanisme self-healing...");
        }

        // ── Tahap 2: Kumpulkan kandidat elemen dari DOM ──────────────────
        long healStart = System.currentTimeMillis();
        List<WebElement> candidates = getAllInteractiveElements();

        if (candidates.isEmpty()) {
            logAndThrow(locator, profile, healStart,
                    "Tidak ada elemen interaktif ditemukan di DOM");
        }

        System.out.printf("[HealingDriver] Mengevaluasi %d kandidat elemen...%n",
                candidates.size());

        // ── Tahap 3: Hitung skor similarity per kandidat ─────────────────
        double      bestScore   = -1.0;
        WebElement  bestElement = null;
        double      bestText    = 0, bestLocator = 0, bestPosition = 0;

        for (WebElement candidate : candidates) {
            try {
                double score = SimilarityEngine.calculateCombinedScore(profile, candidate);
                String text  = candidate.getText().trim();

                System.out.printf("  ├─ %-30s → combined=%.4f%n",
                        truncate(text, 30), score);

                if (score > bestScore) {
                    bestScore    = score;
                    bestElement  = candidate;

                    // Simpan skor per komponen untuk logging
                    bestText     = SimilarityEngine.calculateLevenshteinSimilarity(
                            SimilarityEngine.normalizeText(profile.expectedText),
                            SimilarityEngine.normalizeText(text));
                    bestLocator  = SimilarityEngine.calculateLevenshteinSimilarity(
                            SimilarityEngine.normalizeText(profile.originalLocatorValue),
                            SimilarityEngine.normalizeText(
                                    safeGetAttribute(candidate, profile.locatorType)));
                    bestPosition = profile.lastKnownLocation != null
                            ? SimilarityEngine.calculatePositionSimilarity(
                                    profile.lastKnownLocation, candidate.getLocation())
                            : 0.0;
                }
            } catch (StaleElementReferenceException ignored) {
                // Elemen sudah tidak valid di DOM, lewati
            }
        }

        long healTime = System.currentTimeMillis() - healStart;

        // ── Tahap 4: Evaluasi hasil ───────────────────────────────────────
        if (bestElement != null && bestScore >= threshold) {
            // HEALING BERHASIL
            String html = safeGetAttribute(bestElement, "outerHTML");

            HealingLogger.log(new HealingResult(
                    testCaseId, scenarioType, locator.toString(),
                    "SUCCESS",
                    bestText, bestLocator, bestPosition, bestScore,
                    html, healTime, false // is_false_positive diisi manual
            ));

            System.out.printf("[HealingDriver] ✓ Healing BERHASIL | score=%.4f | time=%dms%n",
                    bestScore, healTime);
            return bestElement;

        } else {
            // HEALING GAGAL — skor tertinggi masih di bawah threshold
            HealingLogger.log(new HealingResult(
                    testCaseId, scenarioType, locator.toString(),
                    "FAIL",
                    bestText, bestLocator, bestPosition,
                    bestScore < 0 ? 0 : bestScore,
                    "-", healTime, false
            ));

            throw new NoSuchElementException(
                    String.format("[HealingDriver] Healing GAGAL. " +
                            "Skor terbaik %.4f di bawah threshold %.4f. Locator: %s",
                            bestScore, threshold, locator));
        }
    }

    // -------------------------------------------------------
    // BACKWARD COMPATIBLE: findElement dengan teks saja
    // (untuk kode TestInventory.java yang lama)
    // -------------------------------------------------------

    /**
     * Versi sederhana tanpa ElementProfile — untuk kompatibilitas kode lama.
     * Mencari elemen berdasarkan teks yang terlihat di layar.
     *
     * @deprecated Gunakan findElement(By, ElementProfile) untuk hasil lebih akurat.
     */
    @Deprecated
    public WebElement findElement(By locator, String expectedText) {
        // Ekstrak nilai locator dari string representasi By
        String locatorStr   = locator.toString();
        String locatorType  = "id";
        String locatorValue = "";

        if (locatorStr.contains("By.id:")) {
            locatorValue = locatorStr.replace("By.id: ", "").trim();
            locatorType  = "id";
        } else if (locatorStr.contains("By.className:")) {
            locatorValue = locatorStr.replace("By.className: ", "").trim();
            locatorType  = "class";
        } else if (locatorStr.contains("By.xpath:")) {
            locatorValue = locatorStr.replace("By.xpath: ", "").trim();
            locatorType  = "xpath";
        }

        ElementProfile profile = new ElementProfile(expectedText, locatorValue, locatorType);
        return findElement(locator, profile);
    }

    // -------------------------------------------------------
    // INTERNAL HELPERS
    // -------------------------------------------------------

    /**
     * Kumpulkan semua elemen interaktif yang visible dan enabled dari DOM.
     * Berbeda dari versi lama yang hanya mencari By.tagName("button").
     */
    private List<WebElement> getAllInteractiveElements() {
        List<WebElement> all = new ArrayList<>();
        for (String tag : INTERACTIVE_TAGS) {
            try {
                all.addAll(driver.findElements(By.tagName(tag)));
            } catch (Exception ignored) {}
        }

        // Filter: hanya yang visible dan enabled
        return all.stream()
                .filter(el -> {
                    try {
                        return el.isDisplayed() && el.isEnabled();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    private String safeGetAttribute(WebElement el, String attr) {
        try {
            String val = el.getAttribute(attr);
            return val == null ? "" : val;
        } catch (Exception e) {
            return "";
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    private void logAndThrow(By locator, ElementProfile profile,
                             long healStart, String reason) {
        long healTime = System.currentTimeMillis() - healStart;
        HealingLogger.log(new HealingResult(
                testCaseId, scenarioType, locator.toString(),
                "FAIL", 0, 0, 0, 0, reason, healTime, false));
        throw new NoSuchElementException(
                "[HealingDriver] " + reason + " | Locator: " + locator);
    }
}
