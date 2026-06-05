package com.fulvian.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Self-healing test untuk SUT Arsip Dokumen (Laravel sistembagluri).
 *
 * Catatan integrasi:
 * - File ini membawa helper healing lokal berbasis similarity agar bisa langsung dipahami dan dijalankan.
 * - Jika project kamu sudah punya HealingDriver/SimilarityEngine sendiri, bagian findHealedElement(...)
 *   bisa diganti menjadi pemanggilan HealingDriver kamu agar metriknya masuk ke logger utama penelitian.
 *
 * Jalankan:
 * mvn clean test -Dtest=TestArsipDokumenHealing -DbaseUrl=http://127.0.0.1:8000
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestArsipDokumenHealing {

    private static final double THRESHOLD = 0.50;
    private static final Path RESULT_FILE = Paths.get("results", "arsip_healing_log.csv");
    private static final List<String[]> HEALING_LOGS = new ArrayList<>();

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @BeforeAll
    static void prepareResultFile() throws IOException {
        Files.createDirectories(RESULT_FILE.getParent());
        HEALING_LOGS.clear();
        HEALING_LOGS.add(new String[]{
                "timestamp",
                "test_case_id",
                "sut",
                "original_locator",
                "mutated_locator",
                "selected_element",
                "similarity_score",
                "healing_time_ms",
                "status",
                "message"
        });
    }

    @BeforeEach
    void setUp() {
        baseUrl = System.getProperty("baseUrl", "http://127.0.0.1:8000");

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @AfterAll
    static void writeResultFile() throws IOException {
        List<String> lines = new ArrayList<>();
        for (String[] row : HEALING_LOGS) {
            lines.add(toCsv(row));
        }
        Files.write(RESULT_FILE, lines, StandardCharsets.UTF_8);
        System.out.println("\n[HEALING] Log tersimpan di: " + RESULT_FILE.toAbsolutePath());
    }

    @Test
    @Order(1)
    @DisplayName("ARS-SH-001 Login: input email dipulihkan saat id berubah")
    void arsSh001_loginEmailChangedId() {
        driver.get(baseUrl + "/login");
        waitVisible(By.id("loginEmailInput"));

        mutateId("loginEmailInput", "loginEmailInputRefactor");

        WebElement email = findHealedElement(
                "ARS-SH-001",
                By.id("loginEmailInput"),
                "loginEmailInput",
                "loginEmailInputRefactor",
                ExpectedElement.input("loginEmailInput", "email", "email", "Email address")
        );

        email.sendKeys("admin@example.com");
        driver.findElement(By.id("loginPasswordInput")).sendKeys("password");
        driver.findElement(By.id("loginSubmitButton")).click();

        wait.until(d -> d.getCurrentUrl().contains("/dashboard")
                || d.getPageSource().contains("Dashboard"));
    }

    @Test
    @Order(2)
    @DisplayName("ARS-SH-002 Login: tombol login dipulihkan saat id berubah")
    void arsSh002_loginButtonChangedId() {
        driver.get(baseUrl + "/login");
        waitVisible(By.id("loginSubmitButton"));

        driver.findElement(By.id("loginEmailInput")).sendKeys("admin@example.com");
        driver.findElement(By.id("loginPasswordInput")).sendKeys("password");

        mutateId("loginSubmitButton", "loginSubmitButtonRefactor");

        WebElement button = findHealedElement(
                "ARS-SH-002",
                By.id("loginSubmitButton"),
                "loginSubmitButton",
                "loginSubmitButtonRefactor",
                ExpectedElement.button("loginSubmitButton", "Log In")
        );

        button.click();
        wait.until(d -> d.getCurrentUrl().contains("/dashboard")
                || d.getPageSource().contains("Dashboard"));
    }

    @Test
    @Order(3)
    @DisplayName("ARS-SH-003 Master Data: card APBN dipulihkan saat id berubah")
    void arsSh003_masterApbnCardChangedId() {
        loginAsAdmin();
        driver.get(baseUrl + "/masterdata");
        waitVisible(By.id("masterApbnCard"));

        mutateId("masterApbnCard", "masterApbnCardRefactor");

        WebElement card = findHealedElement(
                "ARS-SH-003",
                By.id("masterApbnCard"),
                "masterApbnCard",
                "masterApbnCardRefactor",
                ExpectedElement.link("masterApbnCard", "Dokumen APBN")
        );

        card.click();
        wait.until(d -> d.getCurrentUrl().contains("/apbn"));
    }

    @Test
    @Order(4)
    @DisplayName("ARS-SH-004 APBN: input tahun dipulihkan saat id berubah")
    void arsSh004_apbnYearInputChangedId() {
        loginAsAdmin();
        driver.get(baseUrl + "/apbn");
        waitVisible(By.id("apbnYearInput"));

        String year = uniqueYear();
        mutateId("apbnYearInput", "apbnYearInputRefactor");

        WebElement yearInput = findHealedElement(
                "ARS-SH-004",
                By.id("apbnYearInput"),
                "apbnYearInput",
                "apbnYearInputRefactor",
                ExpectedElement.input("apbnYearInput", "year", "text", "Masukkan tahun")
        );

        yearInput.clear();
        yearInput.sendKeys(year);
        driver.findElement(By.id("addApbnYearButton")).click();
        wait.until(d -> d.getPageSource().contains(year));
    }

    @Test
    @Order(5)
    @DisplayName("ARS-SH-005 APBN: input nama paket dipulihkan saat id berubah")
    void arsSh005_apbnPackageNameInputChangedId() {
        loginAsAdmin();
        String year = createApbnYear();
        driver.get(baseUrl + "/apbn/" + year);
        waitVisible(By.id("apbnPackageNameInput"));

        String packageName = "Paket Healing APBN " + System.currentTimeMillis();
        mutateId("apbnPackageNameInput", "apbnPackageNameInputRefactor");

        WebElement input = findHealedElement(
                "ARS-SH-005",
                By.id("apbnPackageNameInput"),
                "apbnPackageNameInput",
                "apbnPackageNameInputRefactor",
                ExpectedElement.input("apbnPackageNameInput", "nama_paket", "text", "Nama Paket")
        );

        input.clear();
        input.sendKeys(packageName);
        driver.findElement(By.xpath("//button[normalize-space()='Tambah Paket']")).click();
        wait.until(d -> d.getPageSource().contains(packageName));
    }

    @Test
    @Order(6)
    @DisplayName("ARS-SH-006 APBN: tombol upload dokumen dipulihkan saat id berubah")
    void arsSh006_apbnUploadButtonChangedId() throws IOException {
        loginAsAdmin();
        String year = createApbnYear();
        String packageName = createApbnPackage(year);
        openApbnPackage(packageName);

        String nomorSurat = "APBN/SEL/" + System.currentTimeMillis();
        fillApbnUploadForm(nomorSurat);
        mutateId("uploadApbnDocumentButton", "uploadApbnDocumentButtonRefactor");

        WebElement uploadButton = findHealedElement(
                "ARS-SH-006",
                By.id("uploadApbnDocumentButton"),
                "uploadApbnDocumentButton",
                "uploadApbnDocumentButtonRefactor",
                ExpectedElement.button("uploadApbnDocumentButton", "Upload")
        );

        uploadButton.click();
        wait.until(d -> d.getPageSource().contains("berhasil")
                || d.getPageSource().contains(nomorSurat));
    }

    @Test
    @Order(7)
    @DisplayName("ARS-SH-007 PLN: input nama paket dipulihkan saat id berubah")
    void arsSh007_plnPackageNameInputChangedId() {
        loginAsAdmin();
        driver.get(baseUrl + "/pln/create");
        waitVisible(By.id("plnPackageNameInput"));

        mutateId("plnPackageNameInput", "plnPackageNameInputRefactor");

        WebElement input = findHealedElement(
                "ARS-SH-007",
                By.id("plnPackageNameInput"),
                "plnPackageNameInput",
                "plnPackageNameInputRefactor",
                ExpectedElement.input("plnPackageNameInput", "nama_paket", "text", "Nama Paket Pengadaan")
        );

        input.clear();
        input.sendKeys("Paket Healing PLN " + System.currentTimeMillis());
        assertTrue(input.getAttribute("value").contains("Paket Healing PLN"));
    }

    @Test
    @Order(8)
    @DisplayName("ARS-SH-008 PLN: tombol upload dokumen dipulihkan saat id berubah")
    void arsSh008_plnUploadButtonChangedId() throws IOException {
        loginAsAdmin();
        driver.get(baseUrl + "/pln/create");
        waitVisible(By.id("uploadPlnDocumentButton"));

        String nomorSurat = "PLN/SEL/" + System.currentTimeMillis();
        fillPlnUploadForm(nomorSurat);
        mutateId("uploadPlnDocumentButton", "uploadPlnDocumentButtonRefactor");

        WebElement uploadButton = findHealedElement(
                "ARS-SH-008",
                By.id("uploadPlnDocumentButton"),
                "uploadPlnDocumentButton",
                "uploadPlnDocumentButtonRefactor",
                ExpectedElement.button("uploadPlnDocumentButton", "Upload Dokumen")
        );

        uploadButton.click();
        wait.until(d -> d.getCurrentUrl().contains("/pln")
                && (d.getPageSource().contains("berhasil") || d.getPageSource().contains(nomorSurat)));
    }

    private void loginAsAdmin() {
        driver.get(baseUrl + "/login");
        waitVisible(By.id("loginEmailInput")).sendKeys("admin@example.com");
        driver.findElement(By.id("loginPasswordInput")).sendKeys("password");
        driver.findElement(By.id("loginSubmitButton")).click();

        wait.until(d -> d.getCurrentUrl().contains("/dashboard")
                || d.getPageSource().contains("Dashboard"));
    }

    private String createApbnYear() {
        String year = uniqueYear();
        driver.get(baseUrl + "/apbn");
        WebElement input = waitVisible(By.id("apbnYearInput"));
        input.clear();
        input.sendKeys(year);
        driver.findElement(By.id("addApbnYearButton")).click();
        wait.until(d -> d.getPageSource().contains(year));
        return year;
    }

    private String createApbnPackage(String year) {
        String packageName = "Paket Upload APBN " + System.currentTimeMillis();
        driver.get(baseUrl + "/apbn/" + year);
        WebElement input = waitVisible(By.id("apbnPackageNameInput"));
        input.clear();
        input.sendKeys(packageName);
        driver.findElement(By.xpath("//button[normalize-space()='Tambah Paket']")).click();
        wait.until(d -> d.getPageSource().contains(packageName));
        return packageName;
    }

    private void openApbnPackage(String packageName) {
        waitVisible(By.linkText(packageName)).click();
        waitVisible(By.id("apbnNomorSuratInput"));
    }

    private void fillApbnUploadForm(String nomorSurat) throws IOException {
        driver.findElement(By.name("tanggal_diterima")).sendKeys("2026-06-05");
        driver.findElement(By.name("surat_dari")).sendKeys("Bagian Pengadaan");
        driver.findElement(By.id("apbnNomorSuratInput")).sendKeys(nomorSurat);
        driver.findElement(By.name("tanggal_surat")).sendKeys("2026-06-05");
        driver.findElement(By.id("apbnPerihalInput")).sendKeys("Dokumen APBN untuk pengujian self-healing Selenium");
        driver.findElement(By.id("apbnFileDokumenInput")).sendKeys(createTempPdf().toString());
    }

    private void fillPlnUploadForm(String nomorSurat) throws IOException {
        new Select(driver.findElement(By.name("tahun_anggaran"))).selectByValue("2026");
        driver.findElement(By.id("plnPackageNameInput")).sendKeys("Paket Upload PLN " + System.currentTimeMillis());
        driver.findElement(By.name("tanggal_diterima")).sendKeys("2026-06-05");
        driver.findElement(By.name("surat_dari")).sendKeys("Unit PLN Testing");
        driver.findElement(By.id("plnNomorSuratInput")).sendKeys(nomorSurat);
        driver.findElement(By.name("tanggal_surat")).sendKeys("2026-06-05");
        driver.findElement(By.id("plnPerihalInput")).sendKeys("Dokumen PLN untuk pengujian self-healing Selenium");
        driver.findElement(By.id("plnFileDokumenInput")).sendKeys(createTempPdf().toString());
    }

    private Path createTempPdf() throws IOException {
        Path dir = Paths.get("target", "test-files");
        Files.createDirectories(dir);
        Path pdf = dir.resolve("sample-dokumen.pdf").toAbsolutePath();

        // PDF minimal yang cukup untuk kebutuhan upload file test.
        String content = "%PDF-1.4\n" +
                "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n" +
                "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n" +
                "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 200 200] >> endobj\n" +
                "trailer << /Root 1 0 R >>\n" +
                "%%EOF\n";

        Files.writeString(pdf, content, StandardCharsets.ISO_8859_1);
        return pdf;
    }

    private WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private void mutateId(String oldId, String newId) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object result = js.executeScript(
                "const el = document.getElementById(arguments[0]);" +
                        "if (!el) return 0;" +
                        "el.setAttribute('id', arguments[1]);" +
                        "return 1;",
                oldId,
                newId
        );
        assertEquals(1L, ((Number) result).longValue(), "Elemen yang akan dimutasi tidak ditemukan: " + oldId);
    }

    private WebElement findHealedElement(String testCaseId,
                                         By originalLocator,
                                         String originalId,
                                         String mutatedLocatorDescription,
                                         ExpectedElement expected) {
        long start = System.nanoTime();

        try {
            WebElement original = driver.findElement(originalLocator);
            long elapsed = elapsedMs(start);
            log(testCaseId, originalLocator.toString(), mutatedLocatorDescription, describe(original), 1.0, elapsed,
                    "NOT_HEALED", "Locator lama masih ditemukan, healing tidak aktif");
            return original;
        } catch (NoSuchElementException ignored) {
            // lanjut ke proses healing
        }

        List<WebElement> candidates = driver.findElements(By.cssSelector("input, textarea, button, a, select"));

        CandidateScore best = candidates.stream()
                .filter(WebElement::isDisplayed)
                .map(candidate -> new CandidateScore(candidate, calculateScore(originalId, expected, candidate)))
                .max(Comparator.comparingDouble(c -> c.score))
                .orElseThrow(() -> new NoSuchElementException("Tidak ada kandidat elemen untuk healing"));

        long elapsed = elapsedMs(start);

        if (best.score < THRESHOLD) {
            log(testCaseId, originalLocator.toString(), mutatedLocatorDescription, describe(best.element), best.score, elapsed,
                    "FAILED", "Skor terbaik di bawah threshold " + THRESHOLD);
            throw new NoSuchElementException("Healing gagal. Skor terbaik " + best.score + " di bawah threshold " + THRESHOLD);
        }

        log(testCaseId, originalLocator.toString(), mutatedLocatorDescription, describe(best.element), best.score, elapsed,
                "SUCCESS", "Elemen berhasil dipulihkan");
        return best.element;
    }

    private double calculateScore(String originalId, ExpectedElement expected, WebElement candidate) {
        String candidateId = attr(candidate, "id");
        String candidateName = attr(candidate, "name");
        String candidateType = normalizedType(candidate);
        String candidateTag = candidate.getTagName().toLowerCase(Locale.ROOT);
        String candidateText = visibleText(candidate);
        String candidatePlaceholder = attr(candidate, "placeholder");

        double locatorScore = similarity(originalId, candidateId);
        double nameScore = similarity(expected.name, candidateName);
        double textScore = Math.max(similarity(expected.textHint, candidateText), similarity(expected.textHint, candidatePlaceholder));
        double tagScore = expected.tag.equals(candidateTag) ? 1.0 : 0.0;
        double typeScore = expected.type.isBlank() || expected.type.equals(candidateType) ? 1.0 : 0.0;

        // Bobot sengaja dibuat mirip pendekatan penelitian: locator/text dominan, lalu kompatibilitas tag/type.
        return (locatorScore * 0.40)
                + (nameScore * 0.20)
                + (textScore * 0.20)
                + (tagScore * 0.10)
                + (typeScore * 0.10);
    }

    private String normalizedType(WebElement element) {
        if (!"input".equalsIgnoreCase(element.getTagName())) {
            return "";
        }
        String type = attr(element, "type");
        return type.isBlank() ? "text" : type.toLowerCase(Locale.ROOT);
    }

    private String visibleText(WebElement element) {
        String text = element.getText();
        return text == null ? "" : text.trim();
    }

    private String attr(WebElement element, String name) {
        String value = element.getAttribute(name);
        return value == null ? "" : value.trim();
    }

    private double similarity(String a, String b) {
        a = normalize(a);
        b = normalize(b);

        if (a.isBlank() && b.isBlank()) return 1.0;
        if (a.isBlank() || b.isBlank()) return 0.0;
        if (a.equals(b)) return 1.0;

        int max = Math.max(a.length(), b.length());
        int distance = levenshtein(a, b);
        return Math.max(0.0, 1.0 - ((double) distance / max));
    }

    private String normalize(String value) {
        if (value == null) return "";
        return value.toLowerCase(Locale.ROOT)
                .replace("refactor", "")
                .replaceAll("[^a-z0-9]", "")
                .trim();
    }

    private int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[a.length()][b.length()];
    }

    private long elapsedMs(long startNano) {
        return Duration.ofNanos(System.nanoTime() - startNano).toMillis();
    }

    private void log(String testCaseId,
                     String originalLocator,
                     String mutatedLocator,
                     String selectedElement,
                     double score,
                     long healingTimeMs,
                     String status,
                     String message) {
        HEALING_LOGS.add(new String[]{
                LocalDateTime.now().toString(),
                testCaseId,
                "sistembagluri",
                originalLocator,
                mutatedLocator,
                selectedElement,
                String.format(Locale.US, "%.4f", score),
                String.valueOf(healingTimeMs),
                status,
                message
        });
    }

    private String describe(WebElement element) {
        String tag = element.getTagName();
        String id = attr(element, "id");
        String name = attr(element, "name");
        String type = attr(element, "type");
        String text = visibleText(element);
        return "<" + tag
                + (id.isBlank() ? "" : " id='" + id + "'")
                + (name.isBlank() ? "" : " name='" + name + "'")
                + (type.isBlank() ? "" : " type='" + type + "'")
                + (text.isBlank() ? "" : " text='" + text + "'")
                + ">";
    }

    private String uniqueYear() {
        long value = Math.abs(System.nanoTime() % 7000L);
        return String.valueOf(2000L + value);
    }

    private static String toCsv(String[] row) {
        List<String> escaped = new ArrayList<>();
        for (String value : row) {
            String safe = value == null ? "" : value.replace("\"", "\"\"");
            escaped.add("\"" + safe + "\"");
        }
        return String.join(",", escaped);
    }

    private static class ExpectedElement {
        final String id;
        final String name;
        final String tag;
        final String type;
        final String textHint;

        private ExpectedElement(String id, String name, String tag, String type, String textHint) {
            this.id = id == null ? "" : id;
            this.name = name == null ? "" : name;
            this.tag = tag == null ? "" : tag.toLowerCase(Locale.ROOT);
            this.type = type == null ? "" : type.toLowerCase(Locale.ROOT);
            this.textHint = textHint == null ? "" : textHint;
        }

        static ExpectedElement input(String id, String name, String type, String placeholderOrLabel) {
            return new ExpectedElement(id, name, "input", type, placeholderOrLabel);
        }

        static ExpectedElement button(String id, String textHint) {
            return new ExpectedElement(id, "", "button", "", textHint);
        }

        static ExpectedElement link(String id, String textHint) {
            return new ExpectedElement(id, "", "a", "", textHint);
        }
    }

    private static class CandidateScore {
        final WebElement element;
        final double score;

        CandidateScore(WebElement element, double score) {
            this.element = element;
            this.score = score;
        }
    }
}
