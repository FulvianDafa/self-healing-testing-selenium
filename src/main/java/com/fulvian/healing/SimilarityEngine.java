package com.fulvian.healing;

import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

/**
 * ============================================================
 * SimilarityEngine — Inti algoritma penelitian skripsi ini
 * ============================================================
 *
 * Menghitung skor kemiripan gabungan antara elemen target
 * dan kandidat elemen di DOM menggunakan tiga atribut utama:
 *
 * 1. Kemiripan teks/atribut terbaca
 * 2. Kemiripan locator
 * 3. Kemiripan posisi
 *
 * Pendekatan similarity menggunakan kombinasi:
 * - Levenshtein Similarity
 * - Substring Matching
 * - Token Coverage Similarity
 *
 * Tujuannya agar engine tetap kuat terhadap perubahan locator seperti:
 * searchInput -> productSearchInputRefactor
 * searchInput -> transactionSearchInputRefactor
 * inputDebtorName -> inputDebtorNameRefactor
 * product-search-input -> productSearchDashboardRefactor
 */
public class SimilarityEngine {

    // -------------------------------------------------------
    // BOBOT PER ATRIBUT
    // -------------------------------------------------------
    public static final double WEIGHT_TEXT = 0.50;
    public static final double WEIGHT_LOCATOR = 0.30;
    public static final double WEIGHT_POSITION = 0.20;

    public static final double DEFAULT_THRESHOLD = 0.50;

    private static final double POSITION_NORMALIZATION = 150.0;

    // -------------------------------------------------------
    // METHOD UTAMA: HITUNG SKOR GABUNGAN
    // -------------------------------------------------------

    public static double calculateCombinedScore(ElementProfile profile,
                                                WebElement candidate) {
        if (profile == null || candidate == null) {
            return 0.0;
        }

        /*
         * Untuk elemen input, expectedText kadang kosong atau tidak sama
         * dengan visible text, karena input biasanya tidak punya getText().
         *
         * Maka expectedText fallback ke originalLocatorValue.
         */
        String expectedText = firstNonBlank(
                profile.expectedText,
                profile.originalLocatorValue
        );

        // 1. Skor kemiripan teks/atribut terbaca
        double textScore = calculateBestReadableTextSimilarity(
                expectedText,
                candidate
        );

        // 2. Skor kemiripan locator
        String candidateLocatorValue = getLocatorValue(candidate, profile.locatorType);

        double locatorScore = calculateSmartSimilarity(
                profile.originalLocatorValue,
                candidateLocatorValue
        );

        // 3. Skor kemiripan posisi
        if (profile.lastKnownLocation == null) {
            /*
             * Kalau tidak ada posisi referensi, bobot position tidak dipakai.
             * Bobot text dan locator dinormalisasi ulang supaya total tetap 1.0.
             */
            double totalWeight = WEIGHT_TEXT + WEIGHT_LOCATOR;

            return (textScore * (WEIGHT_TEXT / totalWeight))
                    + (locatorScore * (WEIGHT_LOCATOR / totalWeight));
        }

        double positionScore = calculatePositionSimilarity(
                profile.lastKnownLocation,
                safeGetLocation(candidate)
        );

        return (WEIGHT_TEXT * textScore)
                + (WEIGHT_LOCATOR * locatorScore)
                + (WEIGHT_POSITION * positionScore);
    }

    // -------------------------------------------------------
    // SMART SIMILARITY
    // -------------------------------------------------------

    /**
     * Menghitung similarity terbaik dari atribut-atribut yang bisa dibaca.
     *
     * Ini penting untuk input karena input biasanya tidak punya visible text.
     * Maka kandidat dibaca dari:
     * - text
     * - placeholder
     * - aria-label
     * - name
     * - id
     * - value
     */
    private static double calculateBestReadableTextSimilarity(String expected,
                                                              WebElement candidate) {
        String[] candidateValues = {
                safeGetText(candidate),
                safeGetAttribute(candidate, "placeholder"),
                safeGetAttribute(candidate, "aria-label"),
                safeGetAttribute(candidate, "name"),
                safeGetAttribute(candidate, "id"),
                safeGetAttribute(candidate, "value")
        };

        double bestScore = 0.0;

        for (String value : candidateValues) {
            if (isBlank(value)) {
                continue;
            }

            double score = calculateSmartSimilarity(expected, value);

            if (score > bestScore) {
                bestScore = score;
            }
        }

        return bestScore;
    }

    /**
     * Similarity gabungan:
     * 1. Levenshtein similarity
     * 2. Substring similarity
     * 3. Token coverage similarity
     *
     * Ini membuat kasus locator panjang tetap terbaca mirip.
     */
    private static double calculateSmartSimilarity(String expected, String actual) {
        String normalizedExpected = normalizeText(expected);
        String normalizedActual = normalizeText(actual);

        if (isBlank(normalizedExpected) && isBlank(normalizedActual)) {
            return 1.0;
        }

        if (isBlank(normalizedExpected) || isBlank(normalizedActual)) {
            return 0.0;
        }

        double levenshteinScore = calculateLevenshteinSimilarity(
                normalizedExpected,
                normalizedActual
        );

        double tokenScore = calculateTokenCoverageSimilarity(
                normalizedExpected,
                normalizedActual
        );

        double substringScore = 0.0;

        /*
         * Contoh:
         * search input ada di product search input refactor
         * input debtor name ada di input debtor name refactor
         */
        if (normalizedActual.contains(normalizedExpected)
                || normalizedExpected.contains(normalizedActual)) {
            substringScore = 0.85;
        }

        return Math.max(
                levenshteinScore,
                Math.max(tokenScore, substringScore)
        );
    }

    /**
     * Menghitung berapa banyak token expected yang muncul di actual.
     *
     * Contoh:
     * expected: product search input
     * actual  : product search dashboard refactor
     *
     * token cocok: product, search
     * score: 2 / 3 = 0.6667
     */
    private static double calculateTokenCoverageSimilarity(String expected,
                                                           String actual) {
        String[] expectedTokens = expected.split("\\s+");
        String[] actualTokens = actual.split("\\s+");

        if (expectedTokens.length == 0 || actualTokens.length == 0) {
            return 0.0;
        }

        int validExpectedTokens = 0;
        int matchedTokens = 0;

        for (String expectedToken : expectedTokens) {
            if (isBlank(expectedToken)) {
                continue;
            }

            validExpectedTokens++;

            for (String actualToken : actualTokens) {
                if (expectedToken.equals(actualToken)) {
                    matchedTokens++;
                    break;
                }
            }
        }

        if (validExpectedTokens == 0) {
            return 0.0;
        }

        return (double) matchedTokens / validExpectedTokens;
    }

    // -------------------------------------------------------
    // LEVENSHTEIN DISTANCE
    // -------------------------------------------------------

    public static double calculateLevenshteinSimilarity(String s1, String s2) {
        if (s1 == null) {
            s1 = "";
        }

        if (s2 == null) {
            s2 = "";
        }

        if (s1.isEmpty() && s2.isEmpty()) {
            return 1.0;
        }

        if (s1.isEmpty() || s2.isEmpty()) {
            return 0.0;
        }

        if (s1.equals(s2)) {
            return 1.0;
        }

        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());

        return 1.0 - ((double) distance / maxLength);
    }

    private static int levenshteinDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;

                dp[i][j] = Math.min(
                        Math.min(
                                dp[i - 1][j] + 1,
                                dp[i][j - 1] + 1
                        ),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[m][n];
    }

    // -------------------------------------------------------
    // POSITION SIMILARITY
    // -------------------------------------------------------

    public static double calculatePositionSimilarity(Point target, Point candidate) {
        if (target == null || candidate == null) {
            return 0.0;
        }

        double dx = target.getX() - candidate.getX();
        double dy = target.getY() - candidate.getY();

        double euclideanDistance = Math.sqrt((dx * dx) + (dy * dy));

        return Math.exp(-euclideanDistance / POSITION_NORMALIZATION);
    }

    // -------------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------------

    /**
     * Normalisasi teks.
     *
     * Penting:
     * CamelCase dipecah sebelum lowercase.
     *
     * Contoh:
     * productSearchInputRefactor
     * menjadi:
     * product search input refactor
     */
    public static String normalizeText(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("[^a-zA-Z0-9\\s]", " ")
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String safeGetText(WebElement el) {
        try {
            String text = el.getText();
            return text == null ? "" : text.trim();
        } catch (Exception e) {
            return "";
        }
    }

    private static String safeGetAttribute(WebElement el, String attributeName) {
        try {
            String value = el.getAttribute(attributeName);
            return value == null ? "" : value.trim();
        } catch (Exception e) {
            return "";
        }
    }

    private static Point safeGetLocation(WebElement el) {
        try {
            return el.getLocation();
        } catch (Exception e) {
            return new Point(0, 0);
        }
    }

    private static String getLocatorValue(WebElement el, String locatorType) {
        if (isBlank(locatorType)) {
            return "";
        }

        String normalizedLocatorType = locatorType.toLowerCase().trim();

        switch (normalizedLocatorType) {
            case "id":
                return safeGetAttribute(el, "id");

            case "class":
            case "classname":
            case "class_name":
                return safeGetAttribute(el, "class");

            case "name":
                return safeGetAttribute(el, "name");

            case "placeholder":
                return safeGetAttribute(el, "placeholder");

            case "aria-label":
                return safeGetAttribute(el, "aria-label");

            default:
                return safeGetAttribute(el, locatorType);
        }
    }

    private static String firstNonBlank(String first, String second) {
        if (!isBlank(first)) {
            return first;
        }

        if (!isBlank(second)) {
            return second;
        }

        return "";
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // -------------------------------------------------------
    // BACKWARD COMPATIBILITY
    // -------------------------------------------------------

    /**
     * @deprecated Gunakan calculateCombinedScore() untuk perhitungan utama.
     */
    @Deprecated
    public static double calculateSimilarity(String oldValue, String newValue) {
        return calculateSmartSimilarity(oldValue, newValue);
    }
}