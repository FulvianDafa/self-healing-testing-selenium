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
 * Self-Healing test untuk SUT Target (https://www.target.com).
 * Jalankan: mvn clean test -Dtest=TestTargetHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("TARGET - Self-Healing Test")
public class TestTargetHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/target_healing_log.csv";

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
    @DisplayName("TGT-SH-001 Target: input pencarian dipulihkan")
    void tgtSh001_searchInput() {
        driver.get("https://www.target.com");
        WebElement orig = waitVisible(By.id("search"));
        setId(orig, "tgtSearchInput");
        mutateId("tgtSearchInput", "tgtSearchInputRefactor");

        WebElement el = heal("TGT-SH-001", By.id("tgtSearchInput"),
                new ElementProfile("Search target.com", "tgtSearchInput", "id"));
        assertEquals("tgtSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("headphones");
    }

    @Test @Order(2)
    @DisplayName("TGT-SH-002 Target: tombol pencarian dipulihkan")
    void tgtSh002_searchButton() {
        driver.get("https://www.target.com");
        WebElement orig = waitVisible(By.xpath("//button[@aria-label='search' or @aria-label='Search']"));
        setId(orig, "tgtSearchButton");
        mutateId("tgtSearchButton", "tgtSearchButtonRefactor");

        WebElement el = heal("TGT-SH-002", By.id("tgtSearchButton"),
                new ElementProfile("search", "tgtSearchButton", "id"));
        assertEquals("tgtSearchButtonRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(3)
    @DisplayName("TGT-SH-003 Target: link Cart dipulihkan")
    void tgtSh003_cart() {
        driver.get("https://www.target.com");
        WebElement orig = waitVisible(By.xpath("//a[@aria-label='cart' or @aria-label='Cart']"));
        setId(orig, "tgtCart");
        mutateId("tgtCart", "tgtCartRefactor");

        WebElement el = heal("TGT-SH-003", By.id("tgtCart"),
                new ElementProfile("cart", "tgtCart", "id"));
        assertEquals("tgtCartRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(4)
    @DisplayName("TGT-SH-004 Target: logo Target dipulihkan")
    void tgtSh004_logo() {
        driver.get("https://www.target.com");
        WebElement orig = waitVisible(By.xpath("//a[@aria-label='Target home'] | //img[@alt='Target']/parent::a"));
        setId(orig, "tgtLogo");
        mutateId("tgtLogo", "tgtLogoRefactor");

        WebElement el = heal("TGT-SH-004", By.id("tgtLogo"),
                new ElementProfile("Target home", "tgtLogo", "id"));
        assertEquals("tgtLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(5)
    @DisplayName("TGT-SH-005 Target: link Categories dipulihkan")
    void tgtSh005_categories() {
        driver.get("https://www.target.com");
        WebElement orig = waitVisible(By.xpath("//button[normalize-space()='Categories'] | //a[normalize-space()='Categories']"));
        setId(orig, "tgtCategories");
        mutateId("tgtCategories", "tgtCategoriesRefactor");

        WebElement el = heal("TGT-SH-005", By.id("tgtCategories"),
                new ElementProfile("Categories", "tgtCategories", "id"));
        assertEquals("tgtCategoriesRefactor", el.getAttribute("id"));
        el.click();
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "target_id_change", THRESHOLD);
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
