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
 * Self-Healing test untuk SUT Detik.com (https://www.detik.com).
 * Jalankan: mvn clean test -Dtest=TestDetikHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DETIK - Self-Healing Test")
public class TestDetikHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/detik_healing_log.csv";

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
    @DisplayName("DTK-SH-001 Detik: input pencarian dipulihkan")
    void dtkSh001_searchInput() {
        driver.get("https://www.detik.com");
        WebElement orig = waitVisible(By.id("search-text"));
        setId(orig, "dtkSearchInput");
        mutateId("dtkSearchInput", "dtkSearchInputRefactor");

        WebElement el = heal("DTK-SH-001", By.id("dtkSearchInput"),
                new ElementProfile("Cari berita, topik, atau kata kunci", "dtkSearchInput", "id"));
        assertEquals("dtkSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("teknologi");
    }

    @Test @Order(2)
    @DisplayName("DTK-SH-002 Detik: tombol pencarian dipulihkan")
    void dtkSh002_searchButton() {
        driver.get("https://www.detik.com");
        WebElement orig = waitVisible(By.xpath("//button[@type='submit' and (contains(@class,'search') or contains(@class,'btn-search'))]"));
        setId(orig, "dtkSearchButton");
        mutateId("dtkSearchButton", "dtkSearchButtonRefactor");

        WebElement el = heal("DTK-SH-002", By.id("dtkSearchButton"),
                new ElementProfile("Search", "dtkSearchButton", "id"));
        assertEquals("dtkSearchButtonRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(3)
    @DisplayName("DTK-SH-003 Detik: link News dipulihkan")
    void dtkSh003_news() {
        driver.get("https://www.detik.com");
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'news.detik.com') and normalize-space()='News']"));
        setId(orig, "dtkNews");
        mutateId("dtkNews", "dtkNewsRefactor");

        WebElement el = heal("DTK-SH-003", By.id("dtkNews"),
                new ElementProfile("News", "dtkNews", "id"));
        assertEquals("dtkNewsRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(4)
    @DisplayName("DTK-SH-004 Detik: link Sport dipulihkan")
    void dtkSh004_sport() {
        driver.get("https://www.detik.com");
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'sport.detik.com') or normalize-space()='Sport']"));
        setId(orig, "dtkSport");
        mutateId("dtkSport", "dtkSportRefactor");

        WebElement el = heal("DTK-SH-004", By.id("dtkSport"),
                new ElementProfile("Sport", "dtkSport", "id"));
        assertEquals("dtkSportRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(5)
    @DisplayName("DTK-SH-005 Detik: link Finance dipulihkan")
    void dtkSh005_finance() {
        driver.get("https://www.detik.com");
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'finance.detik.com') or normalize-space()='Finance']"));
        setId(orig, "dtkFinance");
        mutateId("dtkFinance", "dtkFinanceRefactor");

        WebElement el = heal("DTK-SH-005", By.id("dtkFinance"),
                new ElementProfile("Finance", "dtkFinance", "id"));
        assertEquals("dtkFinanceRefactor", el.getAttribute("id"));
        el.click();
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "detik_id_change", THRESHOLD);
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
