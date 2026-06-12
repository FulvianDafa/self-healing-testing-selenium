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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Baseline test untuk SUT Sapi Berkah Amanah - Client Side Vue.
 *
 * Tujuan baseline:
 * - Mensimulasikan perubahan atribut id pada elemen client side.
 * - Membuktikan Selenium biasa gagal menemukan locator lama setelah id berubah.
 * - Test dianggap PASS jika NoSuchElementException terjadi sesuai ekspektasi baseline.
 *
 * Catatan:
 * Beberapa elemen Vue pada project asli belum memiliki id stabil. Oleh karena itu,
 * test baseline ini memberi id awal secara sementara melalui JavaScript, lalu
 * mengubahnya menjadi id baru untuk mensimulasikan DOM locator change.
 * Perubahan ini hanya terjadi pada DOM browser Selenium, bukan source code Vue.
 *
 * Jalankan:
 * mvn clean test -Dtest=TestSapiClientBaseline -DbaseUrl=http://localhost:5173
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("SAPI BERKAH AMANAH CLIENT - Baseline Locator Test")
public class TestSapiClientBaseline {

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private String baseUrl;

    private static final Path RESULT_FILE = Paths.get("results", "sapi_client_baseline_log.csv");
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
        baseUrl = System.getProperty("baseUrl", "http://localhost:5173");

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        if (Boolean.parseBoolean(System.getProperty("headless", "false"))) {
            options.addArguments("--headless=new");
        }

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        js = (JavascriptExecutor) driver;
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
        System.out.println("\n[SAPI CLIENT BASELINE] Log tersimpan di: " + RESULT_FILE.toAbsolutePath());
    }

    @Test
    @Order(1)
    @DisplayName("SBA-CL-BL-001 Landing: tombol BELI SEKARANG gagal ditemukan saat id berubah")
    void sbaClBl001_heroBuyButtonChangedId() {
        openClientPage("/");
        setTemporaryIdByXPath("//a[normalize-space()='BELI SEKARANG']", "clientHeroBuyButton");

        mutateId("clientHeroBuyButton", "clientHeroBuyButtonRefactor");

        assertLocatorFails(
                "SBA-CL-BL-001",
                "By.id: clientHeroBuyButton",
                "id=clientHeroBuyButtonRefactor",
                By.id("clientHeroBuyButton")
        );
    }

    @Test
    @Order(2)
    @DisplayName("SBA-CL-BL-002 Navbar: link FAQ gagal ditemukan saat id berubah")
    void sbaClBl002_navFaqLinkChangedId() {
        openClientPage("/");
        setTemporaryIdByXPath("//nav//a[normalize-space()='FAQ']", "clientFaqNavLink");

        mutateId("clientFaqNavLink", "clientFaqNavLinkRefactor");

        assertLocatorFails(
                "SBA-CL-BL-002",
                "By.id: clientFaqNavLink",
                "id=clientFaqNavLinkRefactor",
                By.id("clientFaqNavLink")
        );
    }

    @Test
    @Order(3)
    @DisplayName("SBA-CL-BL-003 Landing: link DAFTAR SEKARANG gagal ditemukan saat id berubah")
    void sbaClBl003_resellerCtaLinkChangedId() {
        openClientPage("/");
        setTemporaryIdByXPath("//a[normalize-space()='DAFTAR SEKARANG']", "clientResellerCtaLink");

        mutateId("clientResellerCtaLink", "clientResellerCtaLinkRefactor");

        assertLocatorFails(
                "SBA-CL-BL-003",
                "By.id: clientResellerCtaLink",
                "id=clientResellerCtaLinkRefactor",
                By.id("clientResellerCtaLink")
        );
    }

    @Test
    @Order(4)
    @DisplayName("SBA-CL-BL-004 Reseller: input nama lengkap gagal ditemukan saat id berubah")
    void sbaClBl004_resellerNameInputChangedId() {
        openClientPage("/daftarreseller");
        setTemporaryIdByCss("input[placeholder='Masukkan nama lengkap Anda']", "clientResellerNameInput");

        mutateId("clientResellerNameInput", "clientResellerNameInputRefactor");

        assertLocatorFails(
                "SBA-CL-BL-004",
                "By.id: clientResellerNameInput",
                "id=clientResellerNameInputRefactor",
                By.id("clientResellerNameInput")
        );
    }

    @Test
    @Order(5)
    @DisplayName("SBA-CL-BL-005 Reseller: tombol Cancel gagal ditemukan saat id berubah")
    void sbaClBl005_resellerCancelButtonChangedId() {
        openClientPage("/daftarreseller");
        setTemporaryIdByXPath("//button[normalize-space()='Cancel']", "clientResellerCancelButton");

        mutateId("clientResellerCancelButton", "clientResellerCancelButtonRefactor");

        assertLocatorFails(
                "SBA-CL-BL-005",
                "By.id: clientResellerCancelButton",
                "id=clientResellerCancelButtonRefactor",
                By.id("clientResellerCancelButton")
        );
    }

    private void openClientPage(String path) {
        driver.get(resolveUrl(path));
        waitVisible(By.tagName("body"));
    }

    private String resolveUrl(String path) {
        String cleanBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        if (path == null || path.isBlank() || "/".equals(path)) {
            return cleanBase + "/";
        }
        return cleanBase + (path.startsWith("/") ? path : "/" + path);
    }

    private WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private void setTemporaryIdByXPath(String xpath, String id) {
        WebElement element = waitVisible(By.xpath(xpath));
        scrollTo(element);
        setId(element, id);
    }

    private void setTemporaryIdByCss(String cssSelector, String id) {
        WebElement element = waitVisible(By.cssSelector(cssSelector));
        scrollTo(element);
        setId(element, id);
    }

    private void setId(WebElement element, String id) {
        Object result = js.executeScript(
                "arguments[0].setAttribute('id', arguments[1]); return 1;",
                element,
                id
        );
        assertEquals(1L, ((Number) result).longValue(), "Gagal memberi temporary id: " + id);
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

    private void scrollTo(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView({block:'center', inline:'nearest'});", element);
    }

    private void log(String testCaseId, String originalLocator, String mutatedLocator, String status, String message) {
        BASELINE_LOGS.add(new String[]{
                LocalDateTime.now().toString(),
                testCaseId,
                "sapi_client_vue",
                originalLocator,
                mutatedLocator,
                status,
                message
        });
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
