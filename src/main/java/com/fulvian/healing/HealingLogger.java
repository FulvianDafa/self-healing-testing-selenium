package com.fulvian.healing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * ============================================================
 * HealingLogger — Pencatat data eksperimen ke file CSV
 * ============================================================
 *
 * Setiap kali healing terjadi, HealingDriver memanggil
 * HealingLogger.log(result) → data tersimpan ke CSV.
 *
 * File output: results/healing_log.csv
 * File ini yang akan dibuka di Excel/Google Sheets
 * untuk menghitung metrik di Bab 4 skripsi.
 *
 * Format CSV:
 *   timestamp, test_case_id, scenario_type, original_locator,
 *   status, text_score, locator_score, position_score, combined_score,
 *   selected_element, healing_time_ms, is_false_positive
 */
public class HealingLogger {

    // Lokasi file output — relatif dari root project
    private static final String DEFAULT_LOG_FILE = "results/healing_log.csv";
    private static final String STDOUT_SEP  = "─".repeat(70);

    // Flag: apakah header CSV sudah ditulis di sesi ini?
    private static boolean headerWritten = false;
    private static String activeLogFile = null;

    private static String logFile() {
        return System.getProperty("healing.log.file", DEFAULT_LOG_FILE);
    }

    // -------------------------------------------------------
    // METHOD UTAMA: catat satu event healing
    // -------------------------------------------------------

    /**
     * Simpan satu hasil healing ke CSV dan cetak ringkasannya ke konsol.
     *
     * @param result objek HealingResult yang diisi oleh HealingDriver
     */
    public static synchronized void log(HealingResult result) {
        // 1. Pastikan direktori results/ ada
        ensureDirectoryExists();

        // 2. Tulis header kalau file baru / pertama kali di sesi ini
        String currentLogFile = logFile();
        if (!currentLogFile.equals(activeLogFile)) {
            headerWritten = false;
            activeLogFile = currentLogFile;
        }

        if (!headerWritten || !Files.exists(Paths.get(currentLogFile))) {
            writeToFile(HealingResult.csvHeader() + "\n", false);
            headerWritten = true;
        }

        // 3. Append baris data
        writeToFile(result.toCsvRow() + "\n", true);

        // 4. Cetak ke konsol (untuk debugging saat test berjalan)
        printToConsole(result);
    }

    // -------------------------------------------------------
    // METHOD UNTUK MENGHITUNG METRIK LANGSUNG
    // (opsional — bisa juga dihitung manual di Excel)
    // -------------------------------------------------------

    /**
     * Hitung Healing Success Rate dari semua data yang sudah dicatat.
     * Rumus: (jumlah SUCCESS / total baris) × 100%
     */
    public static HealingMetrics calculateMetrics() {
        Path path = Paths.get(logFile());
        if (!Files.exists(path)) return new HealingMetrics(0, 0, 0, 0, 0);

        int total = 0, success = 0, falsePositive = 0;
        long totalHealingTime = 0;

        try {
            java.util.List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) { // skip header (baris 0)
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;

                String[] cols = parseCSVLine(line);
                if (cols.length < 12) continue;

                total++;
                if ("\"SUCCESS\"".equals(cols[4])) success++;
                if ("true".equals(cols[11].trim()))  falsePositive++;

                try {
                    totalHealingTime += Long.parseLong(cols[10].trim());
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException e) {
            System.err.println("[HealingLogger] Gagal membaca CSV: " + e.getMessage());
        }

        double successRate    = total > 0 ? (success * 100.0 / total) : 0;
        double fpRate         = success > 0 ? (falsePositive * 100.0 / success) : 0;
        double avgHealingTime = total > 0 ? (totalHealingTime / (double) total) : 0;

        return new HealingMetrics(total, success, successRate, fpRate, avgHealingTime);
    }

    /** Cetak ringkasan metrik ke konsol — panggil setelah semua test selesai. */
    public static void printSummary() {
        HealingMetrics m = calculateMetrics();
        System.out.println("\n" + STDOUT_SEP);
        System.out.println("  RINGKASAN METRIK SELF-HEALING");
        System.out.println(STDOUT_SEP);
        System.out.printf("  Total skenario yang di-heal : %d%n", m.totalAttempts);
        System.out.printf("  Berhasil dipulihkan         : %d%n", m.successCount);
        System.out.printf("  Healing Success Rate        : %.2f%%%n", m.healingSuccessRate);
        System.out.printf("  False Positive Rate         : %.2f%%%n", m.falsePositiveRate);
        System.out.printf("  Rata-rata waktu healing     : %.2f ms%n", m.avgHealingTimeMs);
        System.out.println(STDOUT_SEP);
        System.out.println("  Data lengkap: " + Paths.get(logFile()).toAbsolutePath());
        System.out.println(STDOUT_SEP + "\n");
    }

    // -------------------------------------------------------
    // INTERNAL HELPERS
    // -------------------------------------------------------

    private static void ensureDirectoryExists() {
        try {
            Files.createDirectories(Paths.get("results"));
        } catch (IOException e) {
            System.err.println("[HealingLogger] Tidak bisa buat direktori results/: " + e.getMessage());
        }
    }

    private static void writeToFile(String content, boolean append) {
        try {
            StandardOpenOption mode = append
                    ? StandardOpenOption.APPEND
                    : StandardOpenOption.TRUNCATE_EXISTING;
            Files.write(Paths.get(logFile()), content.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, mode);
        } catch (IOException e) {
            System.err.println("[HealingLogger] Gagal menulis ke CSV: " + e.getMessage());
        }
    }

    private static void printToConsole(HealingResult r) {
        System.out.println("\n" + STDOUT_SEP);
        System.out.printf("  [HEALING] TC=%-8s | Skenario: %s%n", r.testCaseId, r.scenarioType);
        System.out.printf("  Status       : %s%n", r.status);
        System.out.printf("  Skor teks    : %.4f  (bobot %.0f%%)%n",
                r.textScore,    SimilarityEngine.WEIGHT_TEXT * 100);
        System.out.printf("  Skor locator : %.4f  (bobot %.0f%%)%n",
                r.locatorScore, SimilarityEngine.WEIGHT_LOCATOR * 100);
        System.out.printf("  Skor posisi  : %.4f  (bobot %.0f%%)%n",
                r.positionScore, SimilarityEngine.WEIGHT_POSITION * 100);
        System.out.printf("  Skor gabungan: %.4f%n", r.combinedScore);
        System.out.printf("  Waktu healing: %d ms%n", r.healingTimeMs);
        System.out.printf("  Elemen dipilih: %s%n",
                r.selectedElement.substring(0, Math.min(r.selectedElement.length(), 80)));
        System.out.println(STDOUT_SEP);
    }

    /** Parse satu baris CSV yang nilainya mungkin terbungkus tanda kutip. */
    private static String[] parseCSVLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    // -------------------------------------------------------
    // INNER CLASS: wadah untuk 5 metrik utama
    // -------------------------------------------------------
    public static class HealingMetrics {
        public final int    totalAttempts;
        public final int    successCount;
        public final double healingSuccessRate; // persen
        public final double falsePositiveRate;  // persen
        public final double avgHealingTimeMs;

        public HealingMetrics(int total, int success,
                              double successRate, double fpRate, double avgTime) {
            this.totalAttempts      = total;
            this.successCount       = success;
            this.healingSuccessRate = successRate;
            this.falsePositiveRate  = fpRate;
            this.avgHealingTimeMs   = avgTime;
        }
    }
}
