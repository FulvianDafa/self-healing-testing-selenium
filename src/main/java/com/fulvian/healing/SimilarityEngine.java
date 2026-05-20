package com.fulvian.healing;

import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

/**
 * ============================================================
 * SimilarityEngine — Inti algoritma penelitian skripsi ini
 * ============================================================
 *
 * Menghitung skor kemiripan GABUNGAN antara elemen target
 * dan kandidat elemen di DOM, menggunakan tiga atribut:
 *
 *   1. Kemiripan TEKS     → Levenshtein Distance   (bobot 50%)
 *   2. Kemiripan ID/CLASS → Levenshtein Distance   (bobot 30%)
 *   3. Kemiripan POSISI   → Euclidean Distance      (bobot 20%)
 *
 * Diadaptasi dari pendekatan Similo (Nass et al., 2023) yang
 * menggunakan kombinasi atribut dengan bobot tertimbang.
 *
 * Referensi:
 *   Nass et al. (2023). Similarity-based Web Element Localization
 *   for Robust Test Automation. ACM TOSEM, 32(3).
 */
public class SimilarityEngine {

    // -------------------------------------------------------
    // BOBOT PER ATRIBUT — total harus = 1.0
    // Nilai ini hasil kalibrasi eksperimen (lihat Bab 4 skripsi).
    // Bobot teks paling besar karena paling stabil saat DOM berubah.
    // -------------------------------------------------------
    public static final double WEIGHT_TEXT     = 0.50;
    public static final double WEIGHT_LOCATOR  = 0.30;
    public static final double WEIGHT_POSITION = 0.20;

    // Threshold default — elemen diterima jika skor >= nilai ini
    public static final double DEFAULT_THRESHOLD = 0.50;

    // Faktor normalisasi Euclidean Distance (piksel)
    // Makin besar = toleransi posisi makin longgar
    private static final double POSITION_NORMALIZATION = 150.0;

    // -------------------------------------------------------
    // METHOD UTAMA: hitung skor gabungan
    // -------------------------------------------------------

    /**
     * Hitung skor similarity gabungan antara profil target dan kandidat elemen.
     *
     * @param profile   profil elemen yang dicari (dari sebelum locator gagal)
     * @param candidate elemen kandidat dari DOM terkini
     * @return skor antara 0.0 (tidak mirip) sampai 1.0 (identik)
     */
    public static double calculateCombinedScore(ElementProfile profile,
                                                WebElement candidate) {
        // --- Komponen 1: kemiripan teks ---
        String candidateText = safeGetText(candidate);
        double textScore = calculateLevenshteinSimilarity(
                normalizeText(profile.expectedText),
                normalizeText(candidateText)
        );

        // --- Komponen 2: kemiripan locator (id atau class) ---
        String candidateLocatorValue = getLocatorValue(candidate, profile.locatorType);
        double locatorScore = calculateLevenshteinSimilarity(
                normalizeText(profile.originalLocatorValue),
                normalizeText(candidateLocatorValue)
        );

        // --- Komponen 3: kemiripan posisi (Euclidean Distance) ---
        double positionScore = 0.0;
        if (profile.lastKnownLocation != null) {
            positionScore = calculatePositionSimilarity(
                    profile.lastKnownLocation,
                    safeGetLocation(candidate)
            );
        } else {
            // Kalau tidak ada posisi referensi, netralkan bobot teks dan locator
            // agar total tetap 1.0 — redistribusi bobot secara proporsional
            double totalWeight = WEIGHT_TEXT + WEIGHT_LOCATOR;
            return (textScore * (WEIGHT_TEXT / totalWeight))
                 + (locatorScore * (WEIGHT_LOCATOR / totalWeight));
        }

        // --- Gabungkan dengan bobot tertimbang ---
        return (WEIGHT_TEXT     * textScore)
             + (WEIGHT_LOCATOR  * locatorScore)
             + (WEIGHT_POSITION * positionScore);
    }

    // -------------------------------------------------------
    // LEVENSHTEIN DISTANCE — untuk teks dan locator
    // -------------------------------------------------------

    /**
     * Hitung similarity berbasis Levenshtein Distance.
     * Hasil: 1.0 = identik, 0.0 = sama sekali tidak mirip.
     *
     * Formula normalisasi: 1 - (distance / maxLength)
     * Sama dengan yang digunakan Nass et al. (2023).
     */
    public static double calculateLevenshteinSimilarity(String s1, String s2) {
        if (s1 == null) s1 = "";
        if (s2 == null) s2 = "";

        // Edge case: keduanya kosong = identik
        if (s1.isEmpty() && s2.isEmpty()) return 1.0;
        // Edge case: salah satu kosong
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;
        // Edge case: identik
        if (s1.equals(s2)) return 1.0;

        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());

        return 1.0 - ((double) distance / maxLength);
    }

    /**
     * Algoritma Levenshtein Distance dengan dynamic programming.
     * Menghitung jumlah minimum operasi (insert, delete, replace)
     * untuk mengubah s1 menjadi s2.
     */
    private static int levenshteinDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];

        // Inisialisasi: transformasi string kosong ke s1/s2
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;

        // Isi tabel DP
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1,    // delete
                                 dp[i][j - 1] + 1),   // insert
                        dp[i - 1][j - 1] + cost        // replace
                );
            }
        }

        return dp[m][n];
    }

    // -------------------------------------------------------
    // EUCLIDEAN DISTANCE — untuk posisi elemen di layar
    // -------------------------------------------------------

    /**
     * Hitung similarity posisi menggunakan Euclidean Distance.
     * Normalisasi: fungsi eksponensial terbalik sehingga
     *   - jarak 0 piksel   → skor 1.0 (sama persis)
     *   - jarak 150 piksel → skor ~0.5
     *   - jarak 500+ piksel → skor mendekati 0.0
     */
    public static double calculatePositionSimilarity(Point target, Point candidate) {
        if (target == null || candidate == null) return 0.0;

        double dx = target.getX() - candidate.getX();
        double dy = target.getY() - candidate.getY();
        double euclideanDist = Math.sqrt(dx * dx + dy * dy);

        // Normalisasi ke [0, 1] menggunakan fungsi eksponensial
        return Math.exp(-euclideanDist / POSITION_NORMALIZATION);
    }

    // -------------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------------

    /**
     * Normalisasi teks: lowercase, trim, hapus simbol non-alfanumerik.
     * Penting agar "+ Tambah Produk" dan "Tambah Produk" mendapat skor tinggi.
     */
    public static String normalizeText(String text) {
        if (text == null) return "";
        // Hapus simbol di depan/belakang, lowercase, trim spasi
        return text.toLowerCase()
                   .replaceAll("[^a-z0-9\\s]", "") // hapus simbol
                   .replaceAll("\\s+", " ")         // normalisasi spasi
                   .trim();
    }

    /** Ambil teks elemen dengan aman (tidak throw exception). */
    private static String safeGetText(WebElement el) {
        try {
            return el.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /** Ambil posisi elemen dengan aman. */
    private static Point safeGetLocation(WebElement el) {
        try {
            return el.getLocation();
        } catch (Exception e) {
            return new Point(0, 0);
        }
    }

    /** Ambil nilai atribut sesuai tipe locator (id, class, name, dll). */
    private static String getLocatorValue(WebElement el, String locatorType) {
        try {
            switch (locatorType.toLowerCase()) {
                case "id":    return el.getAttribute("id");
                case "class": return el.getAttribute("class");
                case "name":  return el.getAttribute("name");
                default:      return el.getAttribute(locatorType);
            }
        } catch (Exception e) {
            return "";
        }
    }

    // -------------------------------------------------------
    // METHOD UNTUK BACKWARD COMPATIBILITY
    // (agar kode lama yang pakai calculateSimilarity() tetap bisa jalan)
    // -------------------------------------------------------

    /**
     * @deprecated Gunakan calculateCombinedScore() untuk hasil yang lebih akurat.
     */
    @Deprecated
    public static double calculateSimilarity(String oldValue, String newValue) {
        return calculateLevenshteinSimilarity(oldValue, newValue);
    }
}
