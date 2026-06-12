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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Self-healing test untuk SUT Sapi Berkah Amanah - Admin Side Laravel.
 *
 * Versi ini sudah mengikuti pola repo self-healing utama:
 * - memakai HealingDriver utama,
 * - memakai ElementProfile utama,
 * - memakai HealingLogger utama,
 * - log dipisahkan ke results/sapi_admin_healing_log.csv.
 *
 * Jalankan:
 * mvn clean test -Dtest=TestSapiAdminHealing -DadminUrl=http://localhost:8000 -DadminEmail=admin@example.com -DadminPassword=admin123
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("SAPI BERKAH AMANAH ADMIN - Self-Healing Locator Test")
public class TestSapiAdminHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/sapi_admin_healing_log.csv";

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private String adminUrl;
    private String adminEmail;
    private String adminPassword;

    @BeforeAll
    static void prepareHealingLog() throws IOException {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        Files.createDirectories(Paths.get("results"));
        Files.deleteIfExists(Paths.get(SUT_LOG_FILE));
    }

    @BeforeEach
    void setUp() {
        adminUrl = System.getProperty("adminUrl", System.getProperty("baseUrl", "https://sso.upi.edu/cas/login?service=https%3A%2F%2Fspot.upi.edu%2Fberanda"));
        adminEmail = System.getProperty("adminEmail", "2205469");
        adminPassword = System.getProperty("adminPassword", "2c6756c");

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
    static void printHealingSummary() {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingLogger.printSummary();
    }

    @Test
    @Order(1)
    @DisplayName("SBA-AD-SH-001 Login: input email dipulihkan saat id berubah")
    void sbaAdSh001_loginEmailChangedId() {
        openLoginPage();
        waitVisible(By.id("email"));

        mutateId("email", "adminEmailInputRefactor");

        WebElement emailInput = heal(
                "SBA-AD-SH-001",
                By.id("email"),
                new ElementProfile("Email Address", "email", "id")
        );

        assertEquals("adminEmailInputRefactor", emailInput.getAttribute("id"));
        assertEquals("email", emailInput.getAttribute("name"));
        assertEquals("email", emailInput.getAttribute("type"));

        emailInput.clear();
        emailInput.sendKeys(adminEmail);
        waitVisible(By.id("password")).sendKeys(adminPassword);
        waitVisible(By.xpath("//button[normalize-space()='Sign in']")).click();
        waitUntilAdminLoaded();
    }

    @Test
    @Order(2)
    @DisplayName("SBA-AD-SH-002 Login: tombol Sign in dipulihkan saat id berubah")
    void sbaAdSh002_loginButtonChangedId() {
        openLoginPage();
        waitVisible(By.id("email")).sendKeys(adminEmail);
        waitVisible(By.id("password")).sendKeys(adminPassword);

        setTemporaryIdByXPath("//button[normalize-space()='Sign in']", "adminLoginButton");
        mutateId("adminLoginButton", "adminLoginButtonRefactor");

        WebElement loginButton = heal(
                "SBA-AD-SH-002",
                By.id("adminLoginButton"),
                new ElementProfile("Sign in", "adminLoginButton", "id")
        );

        assertEquals("adminLoginButtonRefactor", loginButton.getAttribute("id"));
        assertEquals("button", loginButton.getTagName().toLowerCase());
        assertTrue(loginButton.getText().contains("Sign in"));

        jsClick(loginButton);
        waitUntilAdminLoaded();
    }

    @Test
    @Order(3)
    @DisplayName("SBA-AD-SH-003 Sidebar: link Hewan Kurban dipulihkan saat id berubah")
    void sbaAdSh003_hewanKurbanSidebarLinkChangedId() {
        loginAsAdmin();
        setTemporaryIdByXPath("//aside//a[contains(normalize-space(.), 'Hewan Kurban')]", "adminHewanKurbanNavLink");
        mutateId("adminHewanKurbanNavLink", "adminHewanKurbanNavLinkRefactor");

        WebElement navLink = heal(
                "SBA-AD-SH-003",
                By.id("adminHewanKurbanNavLink"),
                new ElementProfile("Hewan Kurban", "adminHewanKurbanNavLink", "id")
        );

        assertEquals("adminHewanKurbanNavLinkRefactor", navLink.getAttribute("id"));
        assertEquals("a", navLink.getTagName().toLowerCase());
        assertTrue(navLink.getText().contains("Hewan Kurban"));

        jsClick(navLink);
        wait.until(d -> d.getCurrentUrl().contains("/admin/hewan-kurban")
                || d.getPageSource().contains("Daftar Hewan Kurban"));
    }

    @Test
    @Order(4)
    @DisplayName("SBA-AD-SH-004 Hewan Kurban: filter jenis hewan dipulihkan saat id berubah")
    void sbaAdSh004_filterJenisHewanChangedId() {
        loginAsAdmin();
        openAdminPage("/admin/hewan-kurban");
        waitVisible(By.id("filter_jenis_hewan"));

        mutateId("filter_jenis_hewan", "filterJenisHewanRefactor");

        WebElement selectElement = heal(
                "SBA-AD-SH-004",
                By.id("filter_jenis_hewan"),
                new ElementProfile("Jenis Hewan", "filter_jenis_hewan", "id")
        );

        assertEquals("filterJenisHewanRefactor", selectElement.getAttribute("id"));
        assertEquals("select", selectElement.getTagName().toLowerCase());

        Select select = new Select(selectElement);
        select.selectByValue("sapi");
        assertEquals("sapi", select.getFirstSelectedOption().getAttribute("value"));
    }

    @Test
    @Order(5)
    @DisplayName("SBA-AD-SH-005 Hewan Kurban: tombol Tambah Hewan dipulihkan saat id berubah")
    void sbaAdSh005_addHewanButtonChangedId() {
        loginAsAdmin();
        openAdminPage("/admin/hewan-kurban");
        setTemporaryIdByXPath("//a[contains(normalize-space(.), 'Tambah Hewan')]", "addHewanButton");
        mutateId("addHewanButton", "addHewanButtonRefactor");

        WebElement addButton = heal(
                "SBA-AD-SH-005",
                By.id("addHewanButton"),
                new ElementProfile("Tambah Hewan", "addHewanButton", "id")
        );

        assertEquals("addHewanButtonRefactor", addButton.getAttribute("id"));
        assertEquals("a", addButton.getTagName().toLowerCase());
        assertTrue(addButton.getText().contains("Tambah Hewan"));

        jsClick(addButton);
        wait.until(d -> d.getCurrentUrl().contains("/admin/hewan-kurban/create")
                || d.getPageSource().contains("Tambah Hewan"));
    }

    @Test
    @Order(6)
    @DisplayName("SBA-AD-SH-006 Form Hewan: input nama dipulihkan saat id berubah")
    void sbaAdSh006_namaHewanInputChangedId() {
        loginAsAdmin();
        openAdminPage("/admin/hewan-kurban/create");
        waitVisible(By.id("nama"));

        mutateId("nama", "namaHewanInputRefactor");

        WebElement namaInput = heal(
                "SBA-AD-SH-006",
                By.id("nama"),
                new ElementProfile("Nama / Julukan", "nama", "id")
        );

        assertEquals("namaHewanInputRefactor", namaInput.getAttribute("id"));
        assertEquals("nama", namaInput.getAttribute("name"));
        assertEquals("input", namaInput.getTagName().toLowerCase());

        namaInput.clear();
        namaInput.sendKeys("Sapi Healing Selenium");
        assertEquals("Sapi Healing Selenium", namaInput.getAttribute("value"));
    }

    @Test
    @Order(7)
    @DisplayName("SBA-AD-SH-007 Form Hewan: input harga display dipulihkan saat id berubah")
    void sbaAdSh007_hargaDisplayInputChangedId() {
        loginAsAdmin();
        openAdminPage("/admin/hewan-kurban/create");
        waitVisible(By.id("harga_display"));

        mutateId("harga_display", "hargaDisplayInputRefactor");

        WebElement hargaInput = heal(
                "SBA-AD-SH-007",
                By.id("harga_display"),
                new ElementProfile("Harga", "harga_display", "id")
        );

        assertEquals("hargaDisplayInputRefactor", hargaInput.getAttribute("id"));
        assertEquals("input", hargaInput.getTagName().toLowerCase());
        assertEquals("text", hargaInput.getAttribute("type"));

        hargaInput.clear();
        hargaInput.sendKeys("25000000");
        assertFalse(hargaInput.getAttribute("value").isBlank());
    }

    private WebElement heal(String testCaseId, By originalLocator, ElementProfile profile) {
        HealingDriver healing = new HealingDriver(driver, testCaseId, "sapi_admin_id_change", THRESHOLD);
        return healing.findElement(originalLocator, profile);
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

    private void jsClick(WebElement element) {
        js.executeScript("arguments[0].click();", element);
    }

    private void scrollTo(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView({block:'center', inline:'nearest'});", element);
    }
}
