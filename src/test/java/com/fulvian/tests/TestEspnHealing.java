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
 * Self-Healing test untuk SUT ESPN (https://www.espn.com).
 * Jalankan: mvn clean test -Dtest=TestEspnHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ESPN - Self-Healing Test")
public class TestEspnHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/espn_healing_log.csv";

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
            driver.findElements(By.xpath("//button[contains(@class,'close') or contains(@aria-label,'close') or normalize-space()='Accept All Cookies']"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("ESPN-SH-001 ESPN: input pencarian dipulihkan")
    void espnSh001_searchInput() {
        driver.get("https://www.espn.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//input[@type='search' or @placeholder='Search']"));
        setId(orig, "espnSearchInput");
        mutateId("espnSearchInput", "espnSearchInputRefactor");

        WebElement el = heal("ESPN-SH-001", By.id("espnSearchInput"),
                new ElementProfile("Search", "espnSearchInput", "id"));
        assertEquals("espnSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("NBA");
    }

    @Test @Order(2)
    @DisplayName("ESPN-SH-002 ESPN: nav NFL dipulihkan")
    void espnSh002_nfl() {
        driver.get("https://www.espn.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='NFL']"));
        setId(orig, "espnNfl");
        mutateId("espnNfl", "espnNflRefactor");

        WebElement el = heal("ESPN-SH-002", By.id("espnNfl"),
                new ElementProfile("NFL", "espnNfl", "id"));
        assertEquals("espnNflRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(3)
    @DisplayName("ESPN-SH-003 ESPN: nav NBA dipulihkan")
    void espnSh003_nba() {
        driver.get("https://www.espn.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='NBA']"));
        setId(orig, "espnNba");
        mutateId("espnNba", "espnNbaRefactor");

        WebElement el = heal("ESPN-SH-003", By.id("espnNba"),
                new ElementProfile("NBA", "espnNba", "id"));
        assertEquals("espnNbaRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(4)
    @DisplayName("ESPN-SH-004 ESPN: nav Soccer dipulihkan")
    void espnSh004_soccer() {
        driver.get("https://www.espn.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Soccer']"));
        setId(orig, "espnSoccer");
        mutateId("espnSoccer", "espnSoccerRefactor");

        WebElement el = heal("ESPN-SH-004", By.id("espnSoccer"),
                new ElementProfile("Soccer", "espnSoccer", "id"));
        assertEquals("espnSoccerRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(5)
    @DisplayName("ESPN-SH-005 ESPN: logo ESPN dipulihkan")
    void espnSh005_logo() {
        driver.get("https://www.espn.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[@href='http://www.espn.com' or @aria-label='ESPN Home']"));
        setId(orig, "espnLogo");
        mutateId("espnLogo", "espnLogoRefactor");

        WebElement el = heal("ESPN-SH-005", By.id("espnLogo"),
                new ElementProfile("ESPN", "espnLogo", "id"));
        assertEquals("espnLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "espn_id_change", THRESHOLD);
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
