package com.fulvian.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Baseline test untuk SUT Arsip Dokumen (Laravel sistembagluri).
 *
 * Tujuan baseline:
 * - Mensimulasikan perubahan atribut id pada DOM.
 * - Membuktikan bahwa Selenium biasa gagal menemukan locator lama setelah id berubah.
 * - Test dianggap PASS jika NoSuchElementException terjadi sesuai ekspektasi baseline.
 *
 * Jalankan:
 * mvn clean test -Dtest=TestArsipDokumenBaseline -DbaseUrl=http://127.0.0.1:8000
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestArsipDokumenBaseline {

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    private static final Path RESULT_FILE = Paths.get("results", "arsip_baseline_log.csv");
    private static final List<String[]> BASELINE_LOGS = new ArrayList<>();

    @BeforeAll
    static void prepareResultFile() throws IOException {
        Files.createDirectories(RESULT_FILE.getParent());
        BASELINE_LOGS.clear();
        BASELINE_LOGS.add(new String[]{
                "timestamp",
                "test_case_id",
                "sut",
                "original_locator",
                "mutated_locator",
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
        for (String[] row : BASELINE_LOGS) {
            lines.add(toCsv(row));
        }
        Files.write(RESULT_FILE, lines, StandardCharsets.UTF_8);
        System.out.println("\n[BASELINE] Log tersimpan di: " + RESULT_FILE.toAbsolutePath());
    }

    @Test
    @Order(1)
    @DisplayName("ARS-BL-001 Login: input email gagal ditemukan saat id berubah")
    void arsBl001_loginEmailChangedId() {
        driver.get(baseUrl + "/login");
        waitVisible(By.id("loginEmailInput"));

        mutateId("loginEmailInput", "loginEmailInputRefactor");

        assertLocatorFails(
                "ARS-BL-001",
                "By.id: loginEmailInput",
                "id=loginEmailInputRefactor",
                By.id("loginEmailInput")
        );
    }

    @Test
    @Order(2)
    @DisplayName("ARS-BL-002 Login: tombol login gagal ditemukan saat id berubah")
    void arsBl002_loginButtonChangedId() {
        driver.get(baseUrl + "/login");
        waitVisible(By.id("loginSubmitButton"));

        mutateId("loginSubmitButton", "loginSubmitButtonRefactor");

        assertLocatorFails(
                "ARS-BL-002",
                "By.id: loginSubmitButton",
                "id=loginSubmitButtonRefactor",
                By.id("loginSubmitButton")
        );
    }

    @Test
    @Order(3)
    @DisplayName("ARS-BL-003 Master Data: card APBN gagal ditemukan saat id berubah")
    void arsBl003_masterApbnCardChangedId() {
        loginAsAdmin();
        driver.get(baseUrl + "/masterdata");
        waitVisible(By.id("masterApbnCard"));

        mutateId("masterApbnCard", "masterApbnCardRefactor");

        assertLocatorFails(
                "ARS-BL-003",
                "By.id: masterApbnCard",
                "id=masterApbnCardRefactor",
                By.id("masterApbnCard")
        );
    }

    @Test
    @Order(4)
    @DisplayName("ARS-BL-004 APBN: input tahun gagal ditemukan saat id berubah")
    void arsBl004_apbnYearInputChangedId() {
        loginAsAdmin();
        driver.get(baseUrl + "/apbn");
        waitVisible(By.id("apbnYearInput"));

        mutateId("apbnYearInput", "apbnYearInputRefactor");

        assertLocatorFails(
                "ARS-BL-004",
                "By.id: apbnYearInput",
                "id=apbnYearInputRefactor",
                By.id("apbnYearInput")
        );
    }

    @Test
    @Order(5)
    @DisplayName("ARS-BL-005 APBN: input nama paket gagal ditemukan saat id berubah")
    void arsBl005_apbnPackageNameInputChangedId() {
        loginAsAdmin();
        String year = createApbnYear();
        driver.get(baseUrl + "/apbn/" + year);
        waitVisible(By.id("apbnPackageNameInput"));

        mutateId("apbnPackageNameInput", "apbnPackageNameInputRefactor");

        assertLocatorFails(
                "ARS-BL-005",
                "By.id: apbnPackageNameInput",
                "id=apbnPackageNameInputRefactor",
                By.id("apbnPackageNameInput")
        );
    }

    @Test
    @Order(6)
    @DisplayName("ARS-BL-006 APBN: tombol upload gagal ditemukan saat id berubah")
    void arsBl006_apbnUploadButtonChangedId() {
        loginAsAdmin();
        String year = createApbnYear();
        String packageName = createApbnPackage(year);
        openApbnPackage(packageName);
        waitVisible(By.id("uploadApbnDocumentButton"));

        mutateId("uploadApbnDocumentButton", "uploadApbnDocumentButtonRefactor");

        assertLocatorFails(
                "ARS-BL-006",
                "By.id: uploadApbnDocumentButton",
                "id=uploadApbnDocumentButtonRefactor",
                By.id("uploadApbnDocumentButton")
        );
    }

    @Test
    @Order(7)
    @DisplayName("ARS-BL-007 PLN: input nama paket gagal ditemukan saat id berubah")
    void arsBl007_plnPackageNameInputChangedId() {
        loginAsAdmin();
        driver.get(baseUrl + "/pln/create");
        waitVisible(By.id("plnPackageNameInput"));

        mutateId("plnPackageNameInput", "plnPackageNameInputRefactor");

        assertLocatorFails(
                "ARS-BL-007",
                "By.id: plnPackageNameInput",
                "id=plnPackageNameInputRefactor",
                By.id("plnPackageNameInput")
        );
    }

    @Test
    @Order(8)
    @DisplayName("ARS-BL-008 PLN: tombol upload gagal ditemukan saat id berubah")
    void arsBl008_plnUploadButtonChangedId() {
        loginAsAdmin();
        driver.get(baseUrl + "/pln/create");
        waitVisible(By.id("uploadPlnDocumentButton"));

        mutateId("uploadPlnDocumentButton", "uploadPlnDocumentButtonRefactor");

        assertLocatorFails(
                "ARS-BL-008",
                "By.id: uploadPlnDocumentButton",
                "id=uploadPlnDocumentButtonRefactor",
                By.id("uploadPlnDocumentButton")
        );
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
        String packageName = "Paket Selenium " + System.currentTimeMillis();
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

    private void assertLocatorFails(String testCaseId, String originalLocator, String mutatedLocator, By oldLocator) {
        NoSuchElementException ex = assertThrows(
                NoSuchElementException.class,
                () -> driver.findElement(oldLocator),
                "Baseline harus gagal menemukan locator lama setelah id berubah"
        );

        log(testCaseId, originalLocator, mutatedLocator, "LOCATOR_FAILED_EXPECTED", ex.getClass().getSimpleName());
    }

    private void log(String testCaseId, String originalLocator, String mutatedLocator, String status, String message) {
        BASELINE_LOGS.add(new String[]{
                LocalDateTime.now().toString(),
                testCaseId,
                "sistembagluri",
                originalLocator,
                mutatedLocator,
                status,
                message
        });
    }

    private String uniqueYear() {
        long value = Math.abs(System.nanoTime() % 7000L);
        return String.valueOf(2000L + value); // tetap 4 digit dan aman untuk input APBN
    }

    private static String toCsv(String[] row) {
        List<String> escaped = new ArrayList<>();
        for (String value : row) {
            String safe = value == null ? "" : value.replace("\"", "\"\"");
            escaped.add("\"" + safe + "\"");
        }
        return String.join(",", escaped);
    }
}
