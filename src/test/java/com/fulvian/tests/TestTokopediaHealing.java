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
 * Self-Healing test untuk SUT Tokopedia (https://www.tokopedia.com).
 * Jalankan: mvn clean test -Dtest=TestTokopediaHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("TOKOPEDIA - Self-Healing Test")
public class TestTokopediaHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/tokopedia_healing_log.csv";

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    @BeforeAll
    static void prepareLog() throws IOException {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        Files.createDirectories(Paths.get("results"));
        Files.deleteIfExists(Paths.get(SUT_LOG_FILE));
    }

    @AfterAll
    static void printSummary() {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingLogger.printSummary();
    }

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--start-maximized");
        opts.addArguments("--disable-blink-features=AutomationControlled");
        opts.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        opts.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        driver = new ChromeDriver(opts);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        wait = new WebDriverWait(driver, Duration.ofSeconds(25));
        js = (JavascriptExecutor) driver;
    }

    @AfterEach
    void tearDown() { if (driver != null) driver.quit(); }

    private void dismissPopup() {
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
            driver.findElements(By.xpath("//button[contains(@aria-label,'Close') or contains(@aria-label,'Tutup') or @data-testid='btnCloseModal']"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("TKP-SH-001 Tokopedia: input pencarian dipulihkan")
    void tkpSh001_searchInput() {
        driver.get("https://www.tokopedia.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//input[@data-testid='desktop-header-search-input' or @aria-label='Cari di Tokopedia' or @placeholder='Cari di Tokopedia']"));
        setId(orig, "tkpSearchInput");
        mutateId("tkpSearchInput", "tkpSearchInputRefactor");

        WebElement el = heal("TKP-SH-001", By.id("tkpSearchInput"),
                new ElementProfile("Cari di Tokopedia", "tkpSearchInput", "id"));
        assertEquals("tkpSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("laptop");
    }

    @Test @Order(2)
    @DisplayName("TKP-SH-002 Tokopedia: tombol pencarian dipulihkan")
    void tkpSh002_searchButton() {
        driver.get("https://www.tokopedia.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//button[@data-testid='desktop-header-search-btn' or (@type='submit' and contains(@class,'search'))]"));
        setId(orig, "tkpSearchButton");
        mutateId("tkpSearchButton", "tkpSearchButtonRefactor");

        WebElement el = heal("TKP-SH-002", By.id("tkpSearchButton"),
                new ElementProfile("Cari", "tkpSearchButton", "id"));
        assertEquals("tkpSearchButtonRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(3)
    @DisplayName("TKP-SH-003 Tokopedia: link keranjang dipulihkan")
    void tkpSh003_cart() {
        driver.get("https://www.tokopedia.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[@data-testid='icon-cart' or @href='/cart' or contains(@aria-label,'cart') or contains(@aria-label,'keranjang')]"));
        setId(orig, "tkpCart");
        mutateId("tkpCart", "tkpCartRefactor");

        WebElement el = heal("TKP-SH-003", By.id("tkpCart"),
                new ElementProfile("keranjang", "tkpCart", "id"));
        assertEquals("tkpCartRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(4)
    @DisplayName("TKP-SH-004 Tokopedia: logo Tokopedia dipulihkan")
    void tkpSh004_logo() {
        driver.get("https://www.tokopedia.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[@data-testid='header-main-logo' or (./img[contains(@alt,'Tokopedia')])]"));
        setId(orig, "tkpLogo");
        mutateId("tkpLogo", "tkpLogoRefactor");

        WebElement el = heal("TKP-SH-004", By.id("tkpLogo"),
                new ElementProfile("Tokopedia", "tkpLogo", "id"));
        assertEquals("tkpLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(5)
    @DisplayName("TKP-SH-005 Tokopedia: link Flash Sale dipulihkan")
    void tkpSh005_flashSale() {
        driver.get("https://www.tokopedia.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'flash-sale') or contains(normalize-space(),'Flash Sale')]"));
        setId(orig, "tkpFlashSale");
        mutateId("tkpFlashSale", "tkpFlashSaleRefactor");

        WebElement el = heal("TKP-SH-005", By.id("tkpFlashSale"),
                new ElementProfile("Flash Sale", "tkpFlashSale", "id"));
        assertEquals("tkpFlashSaleRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "tokopedia_id_change", THRESHOLD);
        WebElement result = healingDriver.findElement(brokenLocator, profile);
        assertNotNull(result, "[" + testCaseId + "] Self-healing gagal.");
        assertTrue(result.isDisplayed(), "[" + testCaseId + "] Elemen tidak terlihat.");
        return result;
    }
    private void setId(WebElement el, String id) {
        js.executeScript("arguments[0].setAttribute('id', arguments[1]);", el, id);
    }
    private void mutateId(String oldId, String newId) {
        Object r = js.executeScript(
                "const el=document.getElementById(arguments[0]);" +
                "if(!el) return 0;" +
                "el.setAttribute('data-self-healing-old-id',arguments[0]);" +
                "el.setAttribute('data-self-healing-mutated-from',arguments[0]);" +
                "el.setAttribute('id',arguments[1]); return 1;", oldId, newId);
        assertEquals(1L, ((Number) r).longValue(), "Elemen tidak ditemukan saat mutasi: " + oldId);
    }
    private WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}
