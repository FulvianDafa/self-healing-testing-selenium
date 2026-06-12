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
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Self-healing test untuk SUT Sapi Berkah Amanah - Client Side Vue.
 *
 * Versi ini sudah mengikuti pola repo self-healing utama:
 * - memakai HealingDriver utama,
 * - memakai ElementProfile utama,
 * - memakai HealingLogger utama,
 * - log dipisahkan ke results/sapi_client_healing_log.csv.
 *
 * Jalankan:
 * mvn clean test -Dtest=TestSapiClientHealing -DbaseUrl=http://localhost:5173
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("SAPI BERKAH AMANAH CLIENT - Self-Healing Locator Test")
public class TestSapiClientHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/sapi_client_healing_log.csv";

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
    static void printHealingSummary() {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingLogger.printSummary();
    }

    @Test
    @Order(1)
    @DisplayName("SBA-CL-SH-001 Landing: tombol BELI SEKARANG dipulihkan saat id berubah")
    void sbaClSh001_heroBuyButtonChangedId() {
        openClientPage("/");
        setTemporaryIdByXPath("//a[normalize-space()='BELI SEKARANG']", "clientHeroBuyButton");
        mutateId("clientHeroBuyButton", "clientHeroBuyButtonRefactor");

        WebElement buyButton = heal(
                "SBA-CL-SH-001",
                By.id("clientHeroBuyButton"),
                new ElementProfile("BELI SEKARANG", "clientHeroBuyButton", "id")
        );

        assertEquals("clientHeroBuyButtonRefactor", buyButton.getAttribute("id"));
        assertEquals("a", buyButton.getTagName().toLowerCase());
        assertTrue(buyButton.getText().contains("BELI SEKARANG"));

        jsClick(buyButton);
        wait.until(d -> d.getCurrentUrl().contains("/produk"));
    }

    @Test
    @Order(2)
    @DisplayName("SBA-CL-SH-002 Navbar: link FAQ dipulihkan saat id berubah")
    void sbaClSh002_navFaqLinkChangedId() {
        openClientPage("/");
        setTemporaryIdByXPath("//nav//a[normalize-space()='FAQ']", "clientFaqNavLink");
        mutateId("clientFaqNavLink", "clientFaqNavLinkRefactor");

        WebElement faqLink = heal(
                "SBA-CL-SH-002",
                By.id("clientFaqNavLink"),
                new ElementProfile("FAQ", "clientFaqNavLink", "id")
        );

        assertEquals("clientFaqNavLinkRefactor", faqLink.getAttribute("id"));
        assertEquals("a", faqLink.getTagName().toLowerCase());
        assertEquals("FAQ", faqLink.getText().trim());

        jsClick(faqLink);
        wait.until(d -> d.getCurrentUrl().contains("/faq"));
    }

    @Test
    @Order(3)
    @DisplayName("SBA-CL-SH-003 Landing: link DAFTAR SEKARANG dipulihkan saat id berubah")
    void sbaClSh003_resellerCtaLinkChangedId() {
        openClientPage("/");
        setTemporaryIdByXPath("//a[normalize-space()='DAFTAR SEKARANG']", "clientResellerCtaLink");
        mutateId("clientResellerCtaLink", "clientResellerCtaLinkRefactor");

        WebElement resellerLink = heal(
                "SBA-CL-SH-003",
                By.id("clientResellerCtaLink"),
                new ElementProfile("DAFTAR SEKARANG", "clientResellerCtaLink", "id")
        );

        assertEquals("clientResellerCtaLinkRefactor", resellerLink.getAttribute("id"));
        assertEquals("a", resellerLink.getTagName().toLowerCase());
        assertTrue(resellerLink.getText().contains("DAFTAR SEKARANG"));

        jsClick(resellerLink);
        wait.until(d -> d.getCurrentUrl().contains("/daftarreseller"));
    }

    @Test
    @Order(4)
    @DisplayName("SBA-CL-SH-004 Reseller: input nama lengkap dipulihkan saat id berubah")
    void sbaClSh004_resellerNameInputChangedId() {
        openClientPage("/daftarreseller");
        setTemporaryIdByCss("input[placeholder='Masukkan nama lengkap Anda']", "clientResellerNameInput");
        mutateId("clientResellerNameInput", "clientResellerNameInputRefactor");

        WebElement nameInput = heal(
                "SBA-CL-SH-004",
                By.id("clientResellerNameInput"),
                new ElementProfile("Nama Lengkap", "clientResellerNameInput", "id")
        );

        assertEquals("clientResellerNameInputRefactor", nameInput.getAttribute("id"));
        assertEquals("input", nameInput.getTagName().toLowerCase());
        assertEquals("text", nameInput.getAttribute("type"));

        nameInput.clear();
        nameInput.sendKeys("Fulvian Test Reseller");
        assertEquals("Fulvian Test Reseller", nameInput.getAttribute("value"));
    }

    @Test
    @Order(5)
    @DisplayName("SBA-CL-SH-005 Reseller: tombol Cancel dipulihkan saat id berubah")
    void sbaClSh005_resellerCancelButtonChangedId() {
        openClientPage("/daftarreseller");
        setTemporaryIdByXPath("//button[normalize-space()='Cancel']", "clientResellerCancelButton");
        mutateId("clientResellerCancelButton", "clientResellerCancelButtonRefactor");

        WebElement cancelButton = heal(
                "SBA-CL-SH-005",
                By.id("clientResellerCancelButton"),
                new ElementProfile("Cancel", "clientResellerCancelButton", "id")
        );

        assertEquals("clientResellerCancelButtonRefactor", cancelButton.getAttribute("id"));
        assertEquals("button", cancelButton.getTagName().toLowerCase());

        jsClick(cancelButton);
        wait.until(d -> d.getPageSource().contains("Yakin ingin membatalkan pendaftaran?"));
    }

    private WebElement heal(String testCaseId, By originalLocator, ElementProfile profile) {
        HealingDriver healing = new HealingDriver(driver, testCaseId, "sapi_client_id_change", THRESHOLD);
        return healing.findElement(originalLocator, profile);
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

    private void jsClick(WebElement element) {
        js.executeScript("arguments[0].click();", element);
    }

    private void scrollTo(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView({block:'center', inline:'nearest'});", element);
    }
}
