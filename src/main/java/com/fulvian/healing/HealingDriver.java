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
 *   1. Coba temukan elemen dengan locator asli.
 *   2. Kalau gagal, aktifkan mekanisme self-healing.
 *   3. Kumpulkan elemen interaktif dari DOM.
 *   4. Filter kandidat berdasarkan kompatibilitas tag dan tipe elemen.
 *   5. Hitung similarity hanya pada kandidat yang kompatibel.
 *   6. Pilih kandidat dengan skor tertinggi di atas threshold.
 *   7. Catat hasil ke CSV via HealingLogger.
 *   8. Return elemen hasil healing, atau throw exception jika gagal.
 *
 * Catatan penting:
 *   - Versi ini menambahkan candidate filtering agar:
 *       input text tidak nyasar ke input file,
 *       button tidak nyasar ke input,
 *       select tidak nyasar ke textarea,
 *       input pencarian tidak dipilih untuk target input pembayaran/nama barang.
 */
public class HealingDriver {

    private final WebDriver driver;
    private final String testCaseId;
    private final String scenarioType;
    private final double threshold;

    private static final String[] INTERACTIVE_TAGS = {
            "button", "a", "input", "select", "textarea"
    };

    // -------------------------------------------------------
    // Constructor
    // -------------------------------------------------------

    public HealingDriver(WebDriver driver,
                         String testCaseId,
                         String scenarioType,
                         double threshold) {
        this.driver = driver;
        this.testCaseId = testCaseId;
        this.scenarioType = scenarioType;
        this.threshold = threshold;
    }

    public HealingDriver(WebDriver driver,
                         String testCaseId,
                         String scenarioType) {
        this(driver, testCaseId, scenarioType, SimilarityEngine.DEFAULT_THRESHOLD);
    }

    public HealingDriver(WebDriver driver) {
        this(driver, "TC-00", "unknown", SimilarityEngine.DEFAULT_THRESHOLD);
    }

    // -------------------------------------------------------
    // METHOD UTAMA: findElement dengan healing
    // -------------------------------------------------------

    public WebElement findElement(By locator, ElementProfile profile) {

        // Tahap 1: coba locator asli dulu
        try {
            WebElement el = driver.findElement(locator);
            System.out.printf("[HealingDriver] ✓ Locator asli berhasil: %s%n", locator);
            return el;
        } catch (NoSuchElementException e) {
            System.out.printf("[HealingDriver] ✗ Locator gagal: %s%n", locator);
            System.out.println("[HealingDriver] → Mengaktifkan mekanisme self-healing...");
        }

        long healStart = System.currentTimeMillis();

        // Tahap 2: ambil kandidat elemen dari DOM
        List<WebElement> candidates = getAllInteractiveElements();

        if (candidates.isEmpty()) {
            logAndThrow(locator, healStart,
                    "Tidak ada elemen interaktif ditemukan di DOM");
        }

        System.out.printf("[HealingDriver] Mengevaluasi %d kandidat elemen...%n",
                candidates.size());

        // Tahap 3: cari kandidat terbaik
        double bestScore = -1.0;
        WebElement bestElement = null;

        double bestText = 0.0;
        double bestLocator = 0.0;
        double bestPosition = 0.0;

        int skippedIncompatible = 0;
        int evaluatedCompatible = 0;

        for (WebElement candidate : candidates) {
            try {
                // FIX UTAMA:
                // Jangan langsung hitung similarity.
                // Filter dulu apakah kandidat kompatibel dengan target lama.
                if (!isCompatibleElement(profile, candidate)) {
                    skippedIncompatible++;

                    System.out.printf("  ├─ SKIP %-70s | alasan: incompatible%n",
                            truncate(describeCandidate(candidate), 70));
                    continue;
                }

                evaluatedCompatible++;

                double score = SimilarityEngine.calculateCombinedScore(profile, candidate);

                System.out.printf("  ├─ CAND %-70s → combined=%.4f%n",
                        truncate(describeCandidate(candidate), 70), score);

                if (score > bestScore) {
                    bestScore = score;
                    bestElement = candidate;

                    bestText = calculateTextScore(profile, candidate);
                    bestLocator = calculateLocatorScore(profile, candidate);
                    bestPosition = calculatePositionScore(profile, candidate);
                }

            } catch (StaleElementReferenceException ignored) {
                // Elemen sudah stale, lewati saja
            } catch (Exception ex) {
                // Supaya satu kandidat error tidak menggagalkan seluruh proses healing
                System.out.printf("  ├─ SKIP candidate error: %s%n", ex.getMessage());
            }
        }

        long healTime = System.currentTimeMillis() - healStart;

        System.out.printf("[HealingDriver] Kandidat kompatibel: %d | Kandidat dilewati: %d%n",
                evaluatedCompatible, skippedIncompatible);

        // Tahap 4: evaluasi hasil healing
        if (bestElement != null && bestScore >= threshold) {
            String html = safeGetAttribute(bestElement, "outerHTML");

            HealingLogger.log(new HealingResult(
                    testCaseId,
                    scenarioType,
                    locator.toString(),
                    "SUCCESS",
                    bestText,
                    bestLocator,
                    bestPosition,
                    bestScore,
                    html,
                    healTime,
                    false
            ));

            System.out.printf("[HealingDriver] ✓ Healing BERHASIL | score=%.4f | time=%dms%n",
                    bestScore, healTime);
            System.out.printf("[HealingDriver] Elemen dipilih: %s%n",
                    truncate(describeCandidate(bestElement), 120));

            return bestElement;
        }

        // Tahap 5: healing gagal
        HealingLogger.log(new HealingResult(
                testCaseId,
                scenarioType,
                locator.toString(),
                "FAIL",
                bestText,
                bestLocator,
                bestPosition,
                bestScore < 0 ? 0.0 : bestScore,
                "-",
                healTime,
                false
        ));

        throw new NoSuchElementException(
                String.format("[HealingDriver] Healing GAGAL. " +
                                "Skor terbaik %.4f di bawah threshold %.4f. Locator: %s",
                        bestScore, threshold, locator)
        );
    }

    // -------------------------------------------------------
    // BACKWARD COMPATIBLE: findElement dengan expectedText saja
    // -------------------------------------------------------

    @Deprecated
    public WebElement findElement(By locator, String expectedText) {
        String locatorStr = locator.toString();
        String locatorType = "id";
        String locatorValue = "";

        if (locatorStr.contains("By.id:")) {
            locatorValue = locatorStr.replace("By.id: ", "").trim();
            locatorType = "id";
        } else if (locatorStr.contains("By.name:")) {
            locatorValue = locatorStr.replace("By.name: ", "").trim();
            locatorType = "name";
        } else if (locatorStr.contains("By.className:")) {
            locatorValue = locatorStr.replace("By.className: ", "").trim();
            locatorType = "class";
        } else if (locatorStr.contains("By.xpath:")) {
            locatorValue = locatorStr.replace("By.xpath: ", "").trim();
            locatorType = "xpath";
        } else if (locatorStr.contains("By.cssSelector:")) {
            locatorValue = locatorStr.replace("By.cssSelector: ", "").trim();
            locatorType = "css";
        }

        ElementProfile profile = new ElementProfile(expectedText, locatorValue, locatorType);
        return findElement(locator, profile);
    }

    // -------------------------------------------------------
    // KUMPULKAN KANDIDAT ELEMEN
    // -------------------------------------------------------

    private List<WebElement> getAllInteractiveElements() {
        List<WebElement> all = new ArrayList<>();

        for (String tag : INTERACTIVE_TAGS) {
            try {
                all.addAll(driver.findElements(By.tagName(tag)));
            } catch (Exception ignored) {
                // abaikan jika tag tidak ditemukan
            }
        }

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

    // -------------------------------------------------------
    // FILTER KOMPATIBILITAS KANDIDAT
    // -------------------------------------------------------

    /**
     * Method ini adalah inti perbaikan.
     *
     * Tujuannya:
     *   - input text tidak boleh heal ke input file
     *   - button tidak boleh heal ke input text
     *   - select harus heal ke select
     *   - textarea harus heal ke textarea
     *   - input pencarian tidak boleh dipilih untuk target non-search
     */
    private boolean isCompatibleElement(ElementProfile profile, WebElement candidate) {
        String expectedTag = inferExpectedTag(profile);
        String candidateTag = safeGetTagName(candidate);

        // Kalau target tidak bisa ditebak, jangan terlalu agresif.
        // Tapi tetap tolak input yang jelas tidak aman.
        if (isBlank(expectedTag)) {
            return !isForbiddenInputCandidate(candidate);
        }

        // Target berupa tombol/action.
        // Tombol di HTML kadang bisa berupa <button>, <a>, atau <input type="submit">.
        if ("button".equals(expectedTag)) {
            return isActionElement(candidate);
        }

        // Selain button, tag harus sama.
        if (!expectedTag.equals(candidateTag)) {
            return false;
        }

        // Khusus input, type dan konteksnya harus cocok.
        if ("input".equals(expectedTag)) {
            String expectedType = inferExpectedInputType(profile);
            String candidateType = normalizeInputType(safeGetAttribute(candidate, "type"));

            if (!isCompatibleInputType(expectedType, candidateType)) {
                return false;
            }

            if (!passesInputSemanticGuard(profile, candidate)) {
                return false;
            }
        }

        return true;
    }

    private boolean isActionElement(WebElement candidate) {
        String tag = safeGetTagName(candidate);

        if ("button".equals(tag)) {
            return true;
        }

        if ("a".equals(tag)) {
            return true;
        }

        if ("input".equals(tag)) {
            String type = normalizeInputType(safeGetAttribute(candidate, "type"));
            return type.equals("button")
                    || type.equals("submit")
                    || type.equals("reset");
        }

        return false;
    }

    private boolean isForbiddenInputCandidate(WebElement candidate) {
        String tag = safeGetTagName(candidate);

        if (!"input".equals(tag)) {
            return false;
        }

        String type = normalizeInputType(safeGetAttribute(candidate, "type"));

        return type.equals("hidden")
                || type.equals("file")
                || type.equals("checkbox")
                || type.equals("radio")
                || type.equals("password");
    }

    private boolean isCompatibleInputType(String expectedType, String candidateType) {
        expectedType = normalizeInputType(expectedType);
        candidateType = normalizeInputType(candidateType);

        // File harus benar-benar file.
        if ("file".equals(expectedType)) {
            return "file".equals(candidateType);
        }

        // Numeric input kadang di HTML ditulis text, misalnya untuk format rupiah.
        // Jadi number-like boleh number/text/tel, tapi nanti tetap dijaga semantic guard.
        if ("number".equals(expectedType)) {
            return candidateType.equals("number")
                    || candidateType.equals("text")
                    || candidateType.equals("tel");
        }

        if ("password".equals(expectedType)) {
            return "password".equals(candidateType);
        }

        if ("email".equals(expectedType)) {
            return candidateType.equals("email")
                    || candidateType.equals("text");
        }

        if ("date".equals(expectedType)) {
            return candidateType.equals("date")
                    || candidateType.equals("datetime-local")
                    || candidateType.equals("month")
                    || candidateType.equals("week")
                    || candidateType.equals("time");
        }

        // Default target text:
        // Tolak file, hidden, checkbox, radio, submit, button.
        return candidateType.equals("text")
                || candidateType.equals("search")
                || candidateType.equals("email")
                || candidateType.equals("tel")
                || candidateType.equals("url")
                || candidateType.equals("number");
    }

    /**
     * Guard tambahan untuk input.
     *
     * Ini penting buat mencegah kasus:
     *   - paidInput malah heal ke searchInput
     *   - namaBarang malah heal ke input pencarian
     */
    private boolean passesInputSemanticGuard(ElementProfile profile, WebElement candidate) {
        String targetSignature = buildTargetSignature(profile);
        String candidateSignature = buildCandidateSignature(candidate);

        // Kalau kandidat adalah search input, hanya boleh dipilih jika target-nya juga search.
        if (isSearchLike(candidateSignature) && !isSearchLike(targetSignature)) {
            return false;
        }

        List<String> expectedKeywords = inferExpectedKeywords(profile);

        if (expectedKeywords.isEmpty()) {
            return true;
        }

        for (String keyword : expectedKeywords) {
            if (candidateSignature.contains(keyword)) {
                return true;
            }
        }

        // Fallback ringan:
        // Kalau keyword tidak ketemu, masih boleh lolos jika signature kandidat
        // cukup mirip dengan locator lama.
        double locatorSimilarity = SimilarityEngine.calculateLevenshteinSimilarity(
                SimilarityEngine.normalizeText(getSafe(profile.originalLocatorValue)),
                SimilarityEngine.normalizeText(candidateSignature)
        );

        return locatorSimilarity >= 0.35;
    }

    // -------------------------------------------------------
    // INFERENSI TARGET DARI ElementProfile
    // -------------------------------------------------------

    private String inferExpectedTag(ElementProfile profile) {
        String signature = buildTargetSignature(profile);

        // Button/action
        if (containsAny(signature,
                "btn", "button", "simpan", "save", "tambah", "add",
                "hapus", "delete", "edit", "update", "submit", "bayar",
                "checkout", "login", "logout")) {
            return "button";
        }

        // Select/dropdown
        if (containsAny(signature,
                "select", "dropdown", "kategori", "category", "supplier",
                "status", "jenis", "role")) {
            return "select";
        }

        // Textarea
        if (containsAny(signature,
                "textarea", "alamat", "address", "deskripsi", "description",
                "keterangan", "catatan", "note", "notes")) {
            return "textarea";
        }

        // Input
        if (containsAny(signature,
                "input", "nama", "name", "barang", "produk", "product",
                "harga", "price", "stok", "stock", "qty", "quantity",
                "jumlah", "paid", "bayar", "payment", "total", "nominal",
                "gambar", "image", "foto", "file", "search", "cari",
                "email", "password", "tanggal", "date")) {
            return "input";
        }

        return "";
    }

    private String inferExpectedInputType(ElementProfile profile) {
        String signature = buildTargetSignature(profile);

        if (containsAny(signature, "gambar", "image", "foto", "photo", "file", "upload")) {
            return "file";
        }

        if (containsAny(signature, "password", "pass")) {
            return "password";
        }

        if (containsAny(signature, "email", "mail")) {
            return "email";
        }

        if (containsAny(signature, "tanggal", "date", "waktu", "time")) {
            return "date";
        }

        if (containsAny(signature,
                "harga", "price", "stok", "stock", "qty", "quantity",
                "jumlah", "paid", "bayar", "payment", "total",
                "nominal", "amount", "cash", "uang")) {
            return "number";
        }

        return "text";
    }

    private List<String> inferExpectedKeywords(ElementProfile profile) {
        String signature = buildTargetSignature(profile);
        List<String> keywords = new ArrayList<>();

        if (containsAny(signature, "nama", "name", "barang", "produk", "product", "item")) {
            keywords.add("nama");
            keywords.add("name");
            keywords.add("barang");
            keywords.add("produk");
            keywords.add("product");
            keywords.add("item");
        }

        if (containsAny(signature, "harga", "price")) {
            keywords.add("harga");
            keywords.add("price");
        }

        if (containsAny(signature, "stok", "stock")) {
            keywords.add("stok");
            keywords.add("stock");
        }

        if (containsAny(signature, "qty", "quantity", "jumlah")) {
            keywords.add("qty");
            keywords.add("quantity");
            keywords.add("jumlah");
        }

        if (containsAny(signature, "paid", "bayar", "payment", "uang", "cash", "nominal", "amount")) {
            keywords.add("paid");
            keywords.add("bayar");
            keywords.add("payment");
            keywords.add("uang");
            keywords.add("cash");
            keywords.add("nominal");
            keywords.add("amount");
        }

        if (containsAny(signature, "gambar", "image", "foto", "file", "upload")) {
            keywords.add("gambar");
            keywords.add("image");
            keywords.add("foto");
            keywords.add("file");
            keywords.add("upload");
        }

        if (containsAny(signature, "search", "cari", "filter")) {
            keywords.add("search");
            keywords.add("cari");
            keywords.add("filter");
        }

        return keywords;
    }

    // -------------------------------------------------------
    // SKOR KOMPONEN UNTUK LOGGING
    // -------------------------------------------------------

    private double calculateTextScore(ElementProfile profile, WebElement candidate) {
        String expected = SimilarityEngine.normalizeText(getSafe(profile.expectedText));
        String actual = SimilarityEngine.normalizeText(getCandidateReadableText(candidate));

        return SimilarityEngine.calculateLevenshteinSimilarity(expected, actual);
    }

    private double calculateLocatorScore(ElementProfile profile, WebElement candidate) {
        String expected = SimilarityEngine.normalizeText(getSafe(profile.originalLocatorValue));

        String attrName = resolveLocatorAttributeName(profile.locatorType);
        String actual = SimilarityEngine.normalizeText(safeGetAttribute(candidate, attrName));

        return SimilarityEngine.calculateLevenshteinSimilarity(expected, actual);
    }

    private double calculatePositionScore(ElementProfile profile, WebElement candidate) {
        if (profile.lastKnownLocation == null) {
            return 0.0;
        }

        return SimilarityEngine.calculatePositionSimilarity(
                profile.lastKnownLocation,
                candidate.getLocation()
        );
    }

    // -------------------------------------------------------
    // SIGNATURE HELPER
    // -------------------------------------------------------

    private String buildTargetSignature(ElementProfile profile) {
        return normalizeForMatching(
                getSafe(profile.expectedText) + " " +
                        getSafe(profile.originalLocatorValue) + " " +
                        getSafe(profile.locatorType)
        );
    }

    private String buildCandidateSignature(WebElement candidate) {
        return normalizeForMatching(
                safeGetTagName(candidate) + " " +
                        safeGetAttribute(candidate, "type") + " " +
                        safeGetAttribute(candidate, "id") + " " +
                        safeGetAttribute(candidate, "name") + " " +
                        safeGetAttribute(candidate, "class") + " " +
                        safeGetAttribute(candidate, "placeholder") + " " +
                        safeGetAttribute(candidate, "aria-label") + " " +
                        safeGetAttribute(candidate, "value") + " " +
                        getCandidateReadableText(candidate)
        );
    }

    private String getCandidateReadableText(WebElement candidate) {
        String text = "";

        try {
            text = candidate.getText();
        } catch (Exception ignored) {
            text = "";
        }

        if (!isBlank(text)) {
            return text.trim();
        }

        String value = safeGetAttribute(candidate, "value");
        if (!isBlank(value)) {
            return value.trim();
        }

        String placeholder = safeGetAttribute(candidate, "placeholder");
        if (!isBlank(placeholder)) {
            return placeholder.trim();
        }

        String aria = safeGetAttribute(candidate, "aria-label");
        if (!isBlank(aria)) {
            return aria.trim();
        }

        String name = safeGetAttribute(candidate, "name");
        if (!isBlank(name)) {
            return name.trim();
        }

        String id = safeGetAttribute(candidate, "id");
        if (!isBlank(id)) {
            return id.trim();
        }

        return "";
    }

    private String describeCandidate(WebElement candidate) {
        String tag = safeGetTagName(candidate);
        String type = safeGetAttribute(candidate, "type");
        String id = safeGetAttribute(candidate, "id");
        String name = safeGetAttribute(candidate, "name");
        String placeholder = safeGetAttribute(candidate, "placeholder");
        String text = getCandidateReadableText(candidate);

        return String.format("<%s type='%s' id='%s' name='%s' placeholder='%s' text='%s'>",
                tag,
                type,
                id,
                name,
                placeholder,
                truncate(text, 40)
        );
    }

    // -------------------------------------------------------
    // GENERAL HELPER
    // -------------------------------------------------------

    private String safeGetTagName(WebElement el) {
        try {
            String tag = el.getTagName();
            return tag == null ? "" : tag.trim().toLowerCase();
        } catch (Exception e) {
            return "";
        }
    }

    private String safeGetAttribute(WebElement el, String attr) {
        try {
            if (isBlank(attr)) {
                return "";
            }

            String val = el.getAttribute(attr);
            return val == null ? "" : val;
        } catch (Exception e) {
            return "";
        }
    }

    private String resolveLocatorAttributeName(String locatorType) {
        if (locatorType == null) {
            return "";
        }

        String type = locatorType.trim().toLowerCase();

        if (type.equals("id")) {
            return "id";
        }

        if (type.equals("name")) {
            return "name";
        }

        if (type.equals("class") || type.equals("classname")) {
            return "class";
        }

        // XPath dan CSS selector tidak bisa langsung diambil sebagai atribut HTML.
        // Jadi untuk logging similarity locator, fallback ke id/name/class.
        if (type.equals("xpath") || type.equals("css") || type.equals("cssselector")) {
            return "id";
        }

        return type;
    }

    private String normalizeInputType(String type) {
        if (isBlank(type)) {
            return "text";
        }

        return type.trim().toLowerCase();
    }

    private String normalizeForMatching(String text) {
        if (text == null) {
            return "";
        }

        return text
                .toLowerCase()
                .replace("_", " ")
                .replace("-", " ")
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null) {
            return false;
        }

        String normalized = normalizeForMatching(text);

        for (String keyword : keywords) {
            if (normalized.contains(normalizeForMatching(keyword))) {
                return true;
            }
        }

        return false;
    }

    private boolean isSearchLike(String text) {
        return containsAny(text, "search", "cari", "filter");
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    private String getSafe(String text) {
        return text == null ? "" : text;
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return "";
        }

        if (s.length() <= max) {
            return s;
        }

        return s.substring(0, Math.max(0, max - 3)) + "...";
    }

    private void logAndThrow(By locator, long healStart, String reason) {
        long healTime = System.currentTimeMillis() - healStart;

        HealingLogger.log(new HealingResult(
                testCaseId,
                scenarioType,
                locator.toString(),
                "FAIL",
                0,
                0,
                0,
                0,
                reason,
                healTime,
                false
        ));

        throw new NoSuchElementException(
                "[HealingDriver] " + reason + " | Locator: " + locator
        );
    }
}