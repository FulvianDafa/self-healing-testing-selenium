package com.fulvian.tests;

import com.fulvian.healing.ElementProfile;
import com.fulvian.healing.HealingDriver;
import com.fulvian.healing.HealingLogger;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Self-healing test untuk SUT Arsip Dokumen (Laravel sistembagluri).
 *
 * Refactor penting:
 * - Tidak lagi memakai helper similarity lokal di dalam file test.
 * - Semua proses healing sekarang memakai engine utama penelitian:
 *   HealingDriver, SimilarityEngine, ElementProfile, dan HealingLogger.
 * - Log healing Arsip dipisahkan melalui system property:
 *   results/arsip_healing_log.csv
 *
 * Jalankan:
 * mvn clean test -Dtest=TestArsipDokumenHealing -DbaseUrl=http://127.0.0.1:8000
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("SISTEMBAGLURI - Self-Healing Locator Test")
public class TestArsipDokumenHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/arsip_healing_log.csv";

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private String baseUrl;

    @BeforeAll
    static void prepareHealingLog() throws IOException {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        Files.createDirectories(Paths.get("results"));
        Files.deleteIfExists(Paths.get(SUT_LOG_FILE));
    }

    @BeforeEach
    void setUp() {
        baseUrl = System.getProperty("baseUrl", "http://127.0.0.1:8000");

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        if (Boolean.parseBoolean(System.getProperty("headless", "false"))) {
            options.addArguments("--headless=new");
        }

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        js = (JavascriptExecutor) driver;
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @AfterAll
    static void printHealingSummary() {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingLogger.printSummary();
    }

    @Test
    @Order(1)
    @DisplayName("ARS-SH-001 Login: input email dipulihkan saat id berubah")
    void arsSh001_loginEmailChangedId() {
        openLoginPage();
        waitVisible(By.id("loginEmailInput"));

        mutateId("loginEmailInput", "loginEmailInputRefactor");

        WebElement email = heal(
                "ARS-SH-001",
                By.id("loginEmailInput"),
                new ElementProfile("Email", "loginEmailInput", "id")
        );

        assertEquals("loginEmailInputRefactor", email.getAttribute("id"));
        assertEquals("email", email.getAttribute("name"));

        email.clear();
        email.sendKeys("admin@example.com");
        waitVisible(By.id("loginPasswordInput")).sendKeys("password");
        waitClickable(By.id("loginSubmitButton")).click();

        waitUntilDashboardLoaded();
    }

    @Test
    @Order(2)
    @DisplayName("ARS-SH-002 Login: tombol login dipulihkan saat id berubah")
    void arsSh002_loginButtonChangedId() {
        openLoginPage();
        waitVisible(By.id("loginSubmitButton"));

        waitVisible(By.id("loginEmailInput")).sendKeys("admin@example.com");
        waitVisible(By.id("loginPasswordInput")).sendKeys("password");

        mutateId("loginSubmitButton", "loginSubmitButtonRefactor");

        WebElement button = heal(
                "ARS-SH-002",
                By.id("loginSubmitButton"),
                new ElementProfile("Log In", "loginSubmitButton", "id")
        );

        assertEquals("loginSubmitButtonRefactor", button.getAttribute("id"));
        assertEquals("button", button.getTagName().toLowerCase());

        button.click();
        waitUntilDashboardLoaded();
    }

    @Test
    @Order(3)
    @DisplayName("ARS-SH-003 Master Data: card APBN dipulihkan saat id berubah")
    void arsSh003_masterApbnCardChangedId() {
        loginAsAdmin();
        driver.get(baseUrl + "/masterdata");
        waitVisible(By.id("masterApbnCard"));

        mutateId("masterApbnCard", "masterApbnCardRefactor");

        WebElement card = heal(
                "ARS-SH-003",
                By.id("masterApbnCard"),
                new ElementProfile("Dokumen APBN", "masterApbnCard", "id")
        );

        assertEquals("masterApbnCardRefactor", card.getAttribute("id"));
        assertTrue(card.getText().toLowerCase().contains("apbn"));

        jsClick(card);
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

        WebElement yearInput = heal(
                "ARS-SH-004",
                By.id("apbnYearInput"),
                new ElementProfile("Tahun APBN", "apbnYearInput", "id")
        );

        assertEquals("apbnYearInputRefactor", yearInput.getAttribute("id"));
        assertEquals("year", yearInput.getAttribute("name"));

        yearInput.clear();
        yearInput.sendKeys(year);
        waitClickable(By.id("addApbnYearButton")).click();
        wait.until(d -> d.getPageSource().contains(year));
    }

    @Test
    @Order(5)
    @DisplayName("ARS-SH-005 APBN: input nama paket dipulihkan saat id berubah")
    void arsSh005_apbnPackageNameInputChangedId() {
        loginAsAdmin();
        String year = createApbnYear();
        openApbnYearPage(year);
        waitVisible(By.id("apbnPackageNameInput"));

        String packageName = "Paket Healing APBN " + System.currentTimeMillis();
        mutateId("apbnPackageNameInput", "apbnPackageNameInputRefactor");

        WebElement input = heal(
                "ARS-SH-005",
                By.id("apbnPackageNameInput"),
                new ElementProfile("Nama Paket", "apbnPackageNameInput", "id")
        );

        assertEquals("apbnPackageNameInputRefactor", input.getAttribute("id"));
        assertEquals("nama_paket", input.getAttribute("name"));

        input.clear();
        input.sendKeys(packageName);
        clickTambahPaketButton();
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

        WebElement uploadButton = heal(
                "ARS-SH-006",
                By.id("uploadApbnDocumentButton"),
                new ElementProfile("Upload", "uploadApbnDocumentButton", "id")
        );

        assertEquals("uploadApbnDocumentButtonRefactor", uploadButton.getAttribute("id"));
        assertEquals("button", uploadButton.getTagName().toLowerCase());

        uploadButton.click();
        wait.until(d -> d.getPageSource().toLowerCase().contains("berhasil")
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

        WebElement input = heal(
                "ARS-SH-007",
                By.id("plnPackageNameInput"),
                new ElementProfile("Nama Paket", "plnPackageNameInput", "id")
        );

        assertEquals("plnPackageNameInputRefactor", input.getAttribute("id"));
        assertEquals("nama_paket", input.getAttribute("name"));

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

        WebElement uploadButton = heal(
                "ARS-SH-008",
                By.id("uploadPlnDocumentButton"),
                new ElementProfile("Upload Dokumen", "uploadPlnDocumentButton", "id")
        );

        assertEquals("uploadPlnDocumentButtonRefactor", uploadButton.getAttribute("id"));
        assertEquals("button", uploadButton.getTagName().toLowerCase());

        uploadButton.click();
        wait.until(d -> d.getCurrentUrl().contains("/pln")
                && (d.getPageSource().toLowerCase().contains("berhasil")
                || d.getPageSource().contains(nomorSurat)));
    }

    // =========================================================
    // SELF-HEALING ADAPTER
    // =========================================================

    private WebElement heal(String testCaseId, By originalLocator, ElementProfile profile) {
        HealingDriver healing = new HealingDriver(driver, testCaseId, "sistembagluri_id_change", THRESHOLD);
        return healing.findElement(originalLocator, profile);
    }

    // =========================================================
    // PAGE HELPERS
    // =========================================================

    private void openLoginPage() {
        driver.get(baseUrl + "/login");
        waitVisible(By.id("loginEmailInput"));
    }

    private void loginAsAdmin() {
        openLoginPage();
        waitVisible(By.id("loginEmailInput")).sendKeys("admin@example.com");
        waitVisible(By.id("loginPasswordInput")).sendKeys("password");
        waitClickable(By.id("loginSubmitButton")).click();
        waitUntilDashboardLoaded();
    }

    private void waitUntilDashboardLoaded() {
        wait.until(d -> d.getCurrentUrl().contains("/dashboard")
                || d.getPageSource().contains("Dashboard")
                || d.getPageSource().contains("Master Data"));
    }

    private String createApbnYear() {
        String year = uniqueYear();
        driver.get(baseUrl + "/apbn");

        WebElement input = waitVisible(By.id("apbnYearInput"));
        input.clear();
        input.sendKeys(year);
        waitClickable(By.id("addApbnYearButton")).click();

        wait.until(d -> d.getPageSource().contains(year)
                || d.getCurrentUrl().contains("/apbn"));
        return year;
    }

    /**
     * Membuka halaman detail tahun APBN secara defensif.
     *
     * Pada project Laravel ini route detail tahun biasanya memakai id database,
     * bukan nilai tahun. Jadi test tidak boleh langsung driver.get("/apbn/" + year).
     * Lebih aman: setelah tahun dibuat, klik link/card yang berisi teks tahun tersebut.
     */
    private void openApbnYearPage(String year) {
        if (isPresent(By.id("apbnPackageNameInput"))) {
            return;
        }

        driver.get(baseUrl + "/apbn");
        waitVisible(By.id("apbnYearInput"));
        wait.until(d -> d.getPageSource().contains(year));

        By yearEntry = By.xpath(
                "//*[self::a or self::button][contains(normalize-space(.), '" + year + "')]"
        );

        List<WebElement> entries = driver.findElements(yearEntry);
        if (!entries.isEmpty()) {
            jsClick(entries.get(0));
        }

        waitVisible(By.id("apbnPackageNameInput"));
    }

    private String createApbnPackage(String year) {
        String packageName = "Paket Upload APBN " + System.currentTimeMillis();
        openApbnYearPage(year);

        WebElement input = waitVisible(By.id("apbnPackageNameInput"));
        input.clear();
        input.sendKeys(packageName);
        clickTambahPaketButton();

        wait.until(d -> d.getPageSource().contains(packageName));
        return packageName;
    }

    private void clickTambahPaketButton() {
        By button = By.xpath("//button[contains(normalize-space(.), 'Tambah Paket')]");
        waitClickable(button).click();
    }

    private void openApbnPackage(String packageName) {
        By packageEntry = By.xpath(
                "//*[self::a or self::button][contains(normalize-space(.), '" + packageName + "')]"
        );
        waitClickable(packageEntry).click();
        waitVisible(By.id("apbnNomorSuratInput"));
    }

    private void fillApbnUploadForm(String nomorSurat) throws IOException {
        setValue(By.name("tanggal_diterima"), "2026-06-05");
        setValue(By.name("surat_dari"), "Bagian Pengadaan");
        setValue(By.id("apbnNomorSuratInput"), nomorSurat);
        setValue(By.name("tanggal_surat"), "2026-06-05");
        setValue(By.id("apbnPerihalInput"), "Dokumen APBN untuk pengujian self-healing Selenium");
        waitVisible(By.id("apbnFileDokumenInput")).sendKeys(createTempPdf().toString());
    }

    private void fillPlnUploadForm(String nomorSurat) throws IOException {
        Select yearSelect = new Select(waitVisible(By.name("tahun_anggaran")));
        yearSelect.selectByValue("2026");

        setValue(By.id("plnPackageNameInput"), "Paket Upload PLN " + System.currentTimeMillis());
        setValue(By.name("tanggal_diterima"), "2026-06-05");
        setValue(By.name("surat_dari"), "Unit PLN Testing");
        setValue(By.id("plnNomorSuratInput"), nomorSurat);
        setValue(By.name("tanggal_surat"), "2026-06-05");
        setValue(By.id("plnPerihalInput"), "Dokumen PLN untuk pengujian self-healing Selenium");
        waitVisible(By.id("plnFileDokumenInput")).sendKeys(createTempPdf().toString());
    }

    private void setValue(By locator, String value) {
        WebElement element = waitVisible(locator);
        element.clear();
        element.sendKeys(value);
    }

    private Path createTempPdf() throws IOException {
        Path dir = Paths.get("target", "test-files");
        Files.createDirectories(dir);
        Path pdf = dir.resolve("sample-dokumen.pdf").toAbsolutePath();

        String content = "%PDF-1.4\n" +
                "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n" +
                "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n" +
                "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 200 200] >> endobj\n" +
                "trailer << /Root 1 0 R >>\n" +
                "%%EOF\n";

        Files.writeString(pdf, content, StandardCharsets.ISO_8859_1);
        return pdf;
    }

    // =========================================================
    // DOM / WAIT HELPERS
    // =========================================================

    private WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private boolean isPresent(By locator) {
        try {
            return !driver.findElements(locator).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private void mutateId(String oldId, String newId) {
        Object result = js.executeScript(
                "const el = document.getElementById(arguments[0]);" +
                        "if (!el) return 0;" +
                        "el.setAttribute('id', arguments[1]);" +
                        "return 1;",
                oldId,
                newId
        );
        assertEquals(1L, ((Number) result).longValue(),
                "Elemen yang akan dimutasi tidak ditemukan: " + oldId);
    }

    private void jsClick(WebElement element) {
        js.executeScript("arguments[0].click();", element);
    }

    private String uniqueYear() {
        long offset = Math.abs(System.nanoTime() % 120L);
        return String.valueOf(2030L + offset);
    }
}
