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
 * Self-Healing test untuk SUT Yahoo (https://www.yahoo.com).
 * Jalankan: mvn clean test -Dtest=TestYahooHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("YAHOO - Self-Healing Test")
public class TestYahooHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/yahoo_healing_log.csv";

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
            driver.findElements(By.xpath("//button[normalize-space()='Accept all' or normalize-space()='Reject all']"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("YHOO-SH-001 Yahoo: input pencarian dipulihkan")
    void yhooSh001_searchInput() {
        driver.get("https://www.yahoo.com");
        dismissPopup();
        WebElement orig = waitVisible(By.name("p"));
        setId(orig, "yhooSearchInput");
        mutateId("yhooSearchInput", "yhooSearchInputRefactor");

        WebElement el = heal("YHOO-SH-001", By.id("yhooSearchInput"),
                new ElementProfile("Search query", "yhooSearchInput", "id"));
        assertEquals("yhooSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("Java programming");
    }

    @Test @Order(2)
    @DisplayName("YHOO-SH-002 Yahoo: tombol pencarian dipulihkan")
    void yhooSh002_searchButton() {
        driver.get("https://www.yahoo.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//button[@type='submit' and @id='header-search-btn']"));
        setId(orig, "yhooSearchButton");
        mutateId("yhooSearchButton", "yhooSearchButtonRefactor");

        WebElement el = heal("YHOO-SH-002", By.id("yhooSearchButton"),
                new ElementProfile("Search web", "yhooSearchButton", "id"));
        assertEquals("yhooSearchButtonRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(3)
    @DisplayName("YHOO-SH-003 Yahoo: nav Finance dipulihkan")
    void yhooSh003_finance() {
        driver.get("https://www.yahoo.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'finance') and contains(normalize-space(),'Finance')]"));
        setId(orig, "yhooFinance");
        mutateId("yhooFinance", "yhooFinanceRefactor");

        WebElement el = heal("YHOO-SH-003", By.id("yhooFinance"),
                new ElementProfile("Finance", "yhooFinance", "id"));
        assertEquals("yhooFinanceRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(4)
    @DisplayName("YHOO-SH-004 Yahoo: nav News dipulihkan")
    void yhooSh004_news() {
        driver.get("https://www.yahoo.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'news') and contains(normalize-space(),'News')]"));
        setId(orig, "yhooNews");
        mutateId("yhooNews", "yhooNewsRefactor");

        WebElement el = heal("YHOO-SH-004", By.id("yhooNews"),
                new ElementProfile("News", "yhooNews", "id"));
        assertEquals("yhooNewsRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(5)
    @DisplayName("YHOO-SH-005 Yahoo: nav Sports dipulihkan")
    void yhooSh005_sports() {
        driver.get("https://www.yahoo.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'sports') and contains(normalize-space(),'Sports')]"));
        setId(orig, "yhooSports");
        mutateId("yhooSports", "yhooSportsRefactor");

        WebElement el = heal("YHOO-SH-005", By.id("yhooSports"),
                new ElementProfile("Sports", "yhooSports", "id"));
        assertEquals("yhooSportsRefactor", el.getAttribute("id"));
        el.click();
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "yahoo_id_change", THRESHOLD);
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
