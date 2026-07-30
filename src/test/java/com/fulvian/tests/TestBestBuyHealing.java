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
 * Self-Healing test untuk SUT BestBuy (https://www.bestbuy.com).
 * Jalankan: mvn clean test -Dtest=TestBestBuyHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("BESTBUY - Self-Healing Test")
public class TestBestBuyHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/bestbuy_healing_log.csv";

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

    @Test @Order(1)
    @DisplayName("BBY-SH-001 BestBuy: input pencarian dipulihkan")
    void bbySh001_searchInput() {
        driver.get("https://www.bestbuy.com");
        WebElement orig = waitVisible(By.id("gh-search-input"));
        setId(orig, "bbySearchInput");
        mutateId("bbySearchInput", "bbySearchInputRefactor");

        WebElement el = heal("BBY-SH-001", By.id("bbySearchInput"),
                new ElementProfile("Search Best Buy", "bbySearchInput", "id"));
        assertEquals("bbySearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("gaming chair");
    }

    @Test @Order(2)
    @DisplayName("BBY-SH-002 BestBuy: tombol pencarian dipulihkan")
    void bbySh002_searchButton() {
        driver.get("https://www.bestbuy.com");
        WebElement orig = waitVisible(By.xpath("//button[@type='submit' and @class='header-search-button']"));
        setId(orig, "bbySearchButton");
        mutateId("bbySearchButton", "bbySearchButtonRefactor");

        WebElement el = heal("BBY-SH-002", By.id("bbySearchButton"),
                new ElementProfile("Submit", "bbySearchButton", "id"));
        assertEquals("bbySearchButtonRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(3)
    @DisplayName("BBY-SH-003 BestBuy: link Cart dipulihkan")
    void bbySh003_cart() {
        driver.get("https://www.bestbuy.com");
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'cart') or @aria-label='Cart']"));
        setId(orig, "bbyCart");
        mutateId("bbyCart", "bbyCartRefactor");

        WebElement el = heal("BBY-SH-003", By.id("bbyCart"),
                new ElementProfile("Cart", "bbyCart", "id"));
        assertEquals("bbyCartRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(4)
    @DisplayName("BBY-SH-004 BestBuy: link Top Deals dipulihkan")
    void bbySh004_topDeals() {
        driver.get("https://www.bestbuy.com");
        WebElement orig = waitVisible(By.xpath("//a[contains(normalize-space(),'Top Deals') or contains(normalize-space(),'Deals')]"));
        setId(orig, "bbyTopDeals");
        mutateId("bbyTopDeals", "bbyTopDealsRefactor");

        WebElement el = heal("BBY-SH-004", By.id("bbyTopDeals"),
                new ElementProfile("Top Deals", "bbyTopDeals", "id"));
        assertEquals("bbyTopDealsRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(5)
    @DisplayName("BBY-SH-005 BestBuy: logo BestBuy dipulihkan")
    void bbySh005_logo() {
        driver.get("https://www.bestbuy.com");
        WebElement orig = waitVisible(By.xpath("//a[@class='us-logo'] | //img[@alt='Best Buy']/parent::a"));
        setId(orig, "bbyLogo");
        mutateId("bbyLogo", "bbyLogoRefactor");

        WebElement el = heal("BBY-SH-005", By.id("bbyLogo"),
                new ElementProfile("Best Buy", "bbyLogo", "id"));
        assertEquals("bbyLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "bestbuy_id_change", THRESHOLD);
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
