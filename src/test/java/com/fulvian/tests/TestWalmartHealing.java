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
 * Self-Healing test untuk SUT Walmart (https://www.walmart.com).
 * Jalankan: mvn clean test -Dtest=TestWalmartHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("WALMART - Self-Healing Test")
public class TestWalmartHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/walmart_healing_log.csv";

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
    @DisplayName("WMT-SH-001 Walmart: input pencarian dipulihkan")
    void wmtSh001_searchInput() {
        driver.get("https://www.walmart.com");
        WebElement orig = waitVisible(By.name("q"));
        setId(orig, "wmtSearchInput");
        mutateId("wmtSearchInput", "wmtSearchInputRefactor");

        WebElement el = heal("WMT-SH-001", By.id("wmtSearchInput"),
                new ElementProfile("Search everything at Walmart online", "wmtSearchInput", "id"));
        assertEquals("wmtSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("television");
    }

    @Test @Order(2)
    @DisplayName("WMT-SH-002 Walmart: tombol pencarian dipulihkan")
    void wmtSh002_searchButton() {
        driver.get("https://www.walmart.com");
        WebElement orig = waitVisible(By.xpath("//button[@type='submit' and @aria-label='Search']"));
        setId(orig, "wmtSearchButton");
        mutateId("wmtSearchButton", "wmtSearchButtonRefactor");

        WebElement el = heal("WMT-SH-002", By.id("wmtSearchButton"),
                new ElementProfile("Search", "wmtSearchButton", "id"));
        assertEquals("wmtSearchButtonRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(3)
    @DisplayName("WMT-SH-003 Walmart: link Departments dipulihkan")
    void wmtSh003_departments() {
        driver.get("https://www.walmart.com");
        WebElement orig = waitVisible(By.xpath("//button[@aria-label='Departments'] | //a[normalize-space()='Departments']"));
        setId(orig, "wmtDepartments");
        mutateId("wmtDepartments", "wmtDepartmentsRefactor");

        WebElement el = heal("WMT-SH-003", By.id("wmtDepartments"),
                new ElementProfile("Departments", "wmtDepartments", "id"));
        assertEquals("wmtDepartmentsRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(4)
    @DisplayName("WMT-SH-004 Walmart: link Cart dipulihkan")
    void wmtSh004_cart() {
        driver.get("https://www.walmart.com");
        WebElement orig = waitVisible(By.xpath("//a[@link-identifier='Cart' or @aria-label='Cart']"));
        setId(orig, "wmtCart");
        mutateId("wmtCart", "wmtCartRefactor");

        WebElement el = heal("WMT-SH-004", By.id("wmtCart"),
                new ElementProfile("Cart", "wmtCart", "id"));
        assertEquals("wmtCartRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(5)
    @DisplayName("WMT-SH-005 Walmart: logo Walmart dipulihkan")
    void wmtSh005_logo() {
        driver.get("https://www.walmart.com");
        WebElement orig = waitVisible(By.xpath("//a[@aria-label='Walmart homepage'] | //img[@alt='Walmart']/parent::a"));
        setId(orig, "wmtLogo");
        mutateId("wmtLogo", "wmtLogoRefactor");

        WebElement el = heal("WMT-SH-005", By.id("wmtLogo"),
                new ElementProfile("Walmart homepage", "wmtLogo", "id"));
        assertEquals("wmtLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "walmart_id_change", THRESHOLD);
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
