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
 * Baseline test untuk SUT Sapi Berkah Amanah - Admin Side Laravel.
 *
 * Tujuan baseline:
 * - Mensimulasikan perubahan atribut id pada halaman admin.
 * - Membuktikan Selenium biasa gagal menemukan locator lama setelah id berubah.
 * - Test dianggap PASS jika NoSuchElementException terjadi sesuai ekspektasi baseline.
 *
 * Jalankan:
 * mvn clean test -Dtest=TestSapiAdminBaseline -DadminUrl=http://localhost:8000 -DadminEmail=admin@example.com -DadminPassword=admin123
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("SAPI BERKAH AMANAH ADMIN - Baseline Locator Test")
public class TestSapiAdminBaseline {

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private String adminUrl;
    private String adminEmail;
    private String adminPassword;

    private static final Path RESULT_FILE = Paths.get("results", "sapi_admin_baseline_log.csv");
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
        adminUrl = System.getProperty("adminUrl", System.getProperty("baseUrl", "http://localhost:8000"));
        adminEmail = System.getProperty("adminEmail", "admin@example.com");
        adminPassword = System.getProperty("adminPassword", "admin123");

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
        System.out.println("\n[SAPI ADMIN BASELINE] Log tersimpan di: " + RESULT_FILE.toAbsolutePath());
    }

    @Test
    @Order(1)
    @DisplayName("SBA-AD-BL-001 Login: input email gagal ditemukan saat id berubah")
    void sbaAdBl001_loginEmailChangedId() {
        openLoginPage();
        waitVisible(By.id("email"));

        mutateId("email", "adminEmailInputRefactor");

        assertLocatorFails(
                "SBA-AD-BL-001",
                "By.id: email",
                "id=adminEmailInputRefactor",
                By.id("email")
        );
    }

    @Test
    @Order(2)
    @DisplayName("SBA-AD-BL-002 Login: tombol Sign in gagal ditemukan saat id berubah")
    void sbaAdBl002_loginButtonChangedId() {
        openLoginPage();
        setTemporaryIdByXPath("//button[normalize-space()='Sign in']", "adminLoginButton");

        mutateId("adminLoginButton", "adminLoginButtonRefactor");

        assertLocatorFails(
                "SBA-AD-BL-002",
                "By.id: adminLoginButton",
                "id=adminLoginButtonRefactor",
                By.id("adminLoginButton")
        );
    }

    @Test
    @Order(3)
    @DisplayName("SBA-AD-BL-003 Sidebar: link Hewan Kurban gagal ditemukan saat id berubah")
    void sbaAdBl003_hewanKurbanSidebarLinkChangedId() {
        loginAsAdmin();
        setTemporaryIdByXPath("//aside//a[contains(normalize-space(.), 'Hewan Kurban')]", "adminHewanKurbanNavLink");

        mutateId("adminHewanKurbanNavLink", "adminHewanKurbanNavLinkRefactor");

        assertLocatorFails(
                "SBA-AD-BL-003",
                "By.id: adminHewanKurbanNavLink",
                "id=adminHewanKurbanNavLinkRefactor",
                By.id("adminHewanKurbanNavLink")
        );
    }

    @Test
    @Order(4)
    @DisplayName("SBA-AD-BL-004 Hewan Kurban: filter jenis hewan gagal ditemukan saat id berubah")
    void sbaAdBl004_filterJenisHewanChangedId() {
        loginAsAdmin();
        openAdminPage("/admin/hewan-kurban");
        waitVisible(By.id("filter_jenis_hewan"));

        mutateId("filter_jenis_hewan", "filterJenisHewanRefactor");

        assertLocatorFails(
                "SBA-AD-BL-004",
                "By.id: filter_jenis_hewan",
                "id=filterJenisHewanRefactor",
                By.id("filter_jenis_hewan")
        );
    }

    @Test
    @Order(5)
    @DisplayName("SBA-AD-BL-005 Hewan Kurban: tombol Tambah Hewan gagal ditemukan saat id berubah")
    void sbaAdBl005_addHewanButtonChangedId() {
        loginAsAdmin();
        openAdminPage("/admin/hewan-kurban");
        setTemporaryIdByXPath("//a[contains(normalize-space(.), 'Tambah Hewan')]", "addHewanButton");

        mutateId("addHewanButton", "addHewanButtonRefactor");

        assertLocatorFails(
                "SBA-AD-BL-005",
                "By.id: addHewanButton",
                "id=addHewanButtonRefactor",
                By.id("addHewanButton")
        );
    }

    @Test
    @Order(6)
    @DisplayName("SBA-AD-BL-006 Form Hewan: input nama gagal ditemukan saat id berubah")
    void sbaAdBl006_namaHewanInputChangedId() {
        loginAsAdmin();
        openAdminPage("/admin/hewan-kurban/create");
        waitVisible(By.id("nama"));

        mutateId("nama", "namaHewanInputRefactor");

        assertLocatorFails(
                "SBA-AD-BL-006",
                "By.id: nama",
                "id=namaHewanInputRefactor",
                By.id("nama")
        );
    }

    @Test
    @Order(7)
    @DisplayName("SBA-AD-BL-007 Form Hewan: input harga display gagal ditemukan saat id berubah")
    void sbaAdBl007_hargaDisplayInputChangedId() {
        loginAsAdmin();
        openAdminPage("/admin/hewan-kurban/create");
        waitVisible(By.id("harga_display"));

        mutateId("harga_display", "hargaDisplayInputRefactor");

        assertLocatorFails(
                "SBA-AD-BL-007",
                "By.id: harga_display",
                "id=hargaDisplayInputRefactor",
                By.id("harga_display")
        );
    }

    private void openLoginPage() {
        driver.get(resolveAdminUrl("/login"));
        waitVisible(By.tagName("body"));
    }

    private void openAdminPage(String path) {
        driver.get(resolveAdminUrl(path));
        waitVisible(By.tagName("body"));
    }

    private void loginAsAdmin() {
        openLoginPage();
        waitVisible(By.id("email")).sendKeys(adminEmail);
        waitVisible(By.id("password")).sendKeys(adminPassword);
        waitVisible(By.xpath("//button[normalize-space()='Sign in']")).click();
        waitUntilAdminLoaded();
    }

    private void waitUntilAdminLoaded() {
        wait.until(d -> d.getCurrentUrl().contains("/admin/dashboard")
                || d.getPageSource().contains("ADMIN SBA")
                || d.getPageSource().contains("Dashboard"));
    }

    private String resolveAdminUrl(String path) {
        String cleanBase = adminUrl.endsWith("/") ? adminUrl.substring(0, adminUrl.length() - 1) : adminUrl;
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
                "sapi_admin_laravel",
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
