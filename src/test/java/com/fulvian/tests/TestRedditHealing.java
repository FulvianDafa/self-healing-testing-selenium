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
 * Self-Healing test untuk SUT Reddit (https://www.reddit.com).
 * Jalankan: mvn clean test -Dtest=TestRedditHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("REDDIT - Self-Healing Test")
public class TestRedditHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/reddit_healing_log.csv";

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
            driver.findElements(By.xpath(
                "//button[contains(@aria-label,'Close') or normalize-space()='Close' or normalize-space()='Accept']"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("RDT-SH-001 Reddit: input pencarian dipulihkan")
    void rdtSh001_searchInput() {
        driver.get("https://www.reddit.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//input[@name='q' or @placeholder='Search Reddit']"));
        setId(orig, "rdtSearchInput");
        mutateId("rdtSearchInput", "rdtSearchInputRefactor");

        WebElement el = heal("RDT-SH-001", By.id("rdtSearchInput"),
                new ElementProfile("Search Reddit", "rdtSearchInput", "id"));
        assertEquals("rdtSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("java");
    }

    @Test @Order(2)
    @DisplayName("RDT-SH-002 Reddit: link Popular dipulihkan")
    void rdtSh002_popular() {
        driver.get("https://www.reddit.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'/r/popular') or normalize-space()='Popular']"));
        setId(orig, "rdtPopular");
        mutateId("rdtPopular", "rdtPopularRefactor");

        WebElement el = heal("RDT-SH-002", By.id("rdtPopular"),
                new ElementProfile("Popular", "rdtPopular", "id"));
        assertEquals("rdtPopularRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(3)
    @DisplayName("RDT-SH-003 Reddit: link All dipulihkan")
    void rdtSh003_all() {
        driver.get("https://www.reddit.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'/r/all') or normalize-space()='All']"));
        setId(orig, "rdtAll");
        mutateId("rdtAll", "rdtAllRefactor");

        WebElement el = heal("RDT-SH-003", By.id("rdtAll"),
                new ElementProfile("All", "rdtAll", "id"));
        assertEquals("rdtAllRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(4)
    @DisplayName("RDT-SH-004 Reddit: logo Reddit dipulihkan")
    void rdtSh004_logo() {
        driver.get("https://www.reddit.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[@aria-label='Home' or @href='https://www.reddit.com/'][1]"));
        setId(orig, "rdtLogo");
        mutateId("rdtLogo", "rdtLogoRefactor");

        WebElement el = heal("RDT-SH-004", By.id("rdtLogo"),
                new ElementProfile("Home", "rdtLogo", "id"));
        assertEquals("rdtLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(5)
    @DisplayName("RDT-SH-005 Reddit: button Login dipulihkan")
    void rdtSh005_login() {
        driver.get("https://www.reddit.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Log In' or @href='/login/']"));
        setId(orig, "rdtLogin");
        mutateId("rdtLogin", "rdtLoginRefactor");

        WebElement el = heal("RDT-SH-005", By.id("rdtLogin"),
                new ElementProfile("Log In", "rdtLogin", "id"));
        assertEquals("rdtLoginRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "reddit_id_change", THRESHOLD);
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
