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
 * Self-Healing test untuk SUT eBay (https://www.ebay.com).
 * Jalankan: mvn clean test -Dtest=TestEbayHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("EBAY - Self-Healing Test")
public class TestEbayHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/ebay_healing_log.csv";

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
        driver = new ChromeDriver(opts);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        js = (JavascriptExecutor) driver;
    }

    @AfterEach
    void tearDown() { if (driver != null) driver.quit(); }

    private void dismissPopup() {
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
            driver.findElements(By.xpath("//button[normalize-space()='Accept all cookies' or normalize-space()='Accept']"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("EBY-SH-001 eBay: input pencarian dipulihkan")
    void ebySh001_searchInput() {
        driver.get("https://www.ebay.com");
        dismissPopup();
        WebElement orig = waitVisible(By.id("gh-ac"));
        setId(orig, "ebySearchInput");
        mutateId("ebySearchInput", "ebySearchInputRefactor");

        WebElement el = heal("EBY-SH-001", By.id("ebySearchInput"),
                new ElementProfile("Search for anything", "ebySearchInput", "id"));
        assertEquals("ebySearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("laptop");
    }

    @Test @Order(2)
    @DisplayName("EBY-SH-002 eBay: tombol pencarian dipulihkan")
    void ebySh002_searchButton() {
        driver.get("https://www.ebay.com");
        dismissPopup();
        WebElement orig = waitVisible(By.id("gh-btn"));
        setId(orig, "ebySearchButton");
        mutateId("ebySearchButton", "ebySearchButtonRefactor");

        WebElement el = heal("EBY-SH-002", By.id("ebySearchButton"),
                new ElementProfile("Search", "ebySearchButton", "id"));
        assertEquals("ebySearchButtonRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(3)
    @DisplayName("EBY-SH-003 eBay: link My eBay dipulihkan")
    void ebySh003_myEbay() {
        driver.get("https://www.ebay.com");
        dismissPopup();
        WebElement orig = waitVisible(By.id("gh-eb-My"));
        setId(orig, "ebyMyEbay");
        mutateId("ebyMyEbay", "ebyMyEbayRefactor");

        WebElement el = heal("EBY-SH-003", By.id("ebyMyEbay"),
                new ElementProfile("My eBay", "ebyMyEbay", "id"));
        assertEquals("ebyMyEbayRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(4)
    @DisplayName("EBY-SH-004 eBay: link Cart dipulihkan")
    void ebySh004_cart() {
        driver.get("https://www.ebay.com");
        dismissPopup();
        WebElement orig = waitVisible(By.id("gh-cart-n"));
        setId(orig, "ebyCart");
        mutateId("ebyCart", "ebyCartRefactor");

        WebElement el = heal("EBY-SH-004", By.id("ebyCart"),
                new ElementProfile("Cart", "ebyCart", "id"));
        assertEquals("ebyCartRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(5)
    @DisplayName("EBY-SH-005 eBay: logo eBay dipulihkan")
    void ebySh005_logo() {
        driver.get("https://www.ebay.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[@class='gh-logo' or @aria-label='eBay Home']"));
        setId(orig, "ebyLogo");
        mutateId("ebyLogo", "ebyLogoRefactor");

        WebElement el = heal("EBY-SH-005", By.id("ebyLogo"),
                new ElementProfile("eBay Home", "ebyLogo", "id"));
        assertEquals("ebyLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "ebay_id_change", THRESHOLD);
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
