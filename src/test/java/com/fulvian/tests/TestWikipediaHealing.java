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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("WIKIPEDIA - Self-Healing Test")
public class TestWikipediaHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/wikipedia_healing_log.csv";

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
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        js = (JavascriptExecutor) driver;
    }

    @AfterEach
    void tearDown() { if (driver != null) driver.quit(); }

    @Test @Order(1)
    @DisplayName("WIKI-SH-001 Wikipedia: input pencarian dipulihkan")
    void wikiSh001_searchInput() {
        driver.get("https://en.wikipedia.org");
        WebElement orig = waitVisible(By.name("search"));
        setId(orig, "wikiSearchInput");
        mutateId("wikiSearchInput", "wikiSearchInputRefactor");

        WebElement el = heal("WIKI-SH-001", By.id("wikiSearchInput"),
                new ElementProfile("Search Wikipedia", "wikiSearchInput", "id"));
        
        assertEquals("wikiSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("Selenium WebDriver");
    }

    @Test @Order(2)
    @DisplayName("WIKI-SH-002 Wikipedia: tombol pencarian dipulihkan")
    void wikiSh002_searchButton() {
        driver.get("https://en.wikipedia.org");
        WebElement orig = waitVisible(By.xpath("//button[normalize-space()='Search']"));
        setId(orig, "wikiSearchButton");
        mutateId("wikiSearchButton", "wikiSearchButtonRefactor");

        WebElement el = heal("WIKI-SH-002", By.id("wikiSearchButton"),
                new ElementProfile("Search", "wikiSearchButton", "id"));
        
        assertEquals("wikiSearchButtonRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(3)
    @DisplayName("WIKI-SH-003 Wikipedia: link Random Article dipulihkan")
    void wikiSh003_randomArticle() {
        driver.get("https://en.wikipedia.org");
        WebElement orig = waitVisible(By.xpath("//span[normalize-space()='Random article']/parent::a | //a[normalize-space()='Random article']"));
        setId(orig, "wikiRandomArticle");
        mutateId("wikiRandomArticle", "wikiRandomArticleRefactor");

        WebElement el = heal("WIKI-SH-003", By.id("wikiRandomArticle"),
                new ElementProfile("Random article", "wikiRandomArticle", "id"));
        
        assertEquals("wikiRandomArticleRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(4)
    @DisplayName("WIKI-SH-004 Wikipedia: link About Wikipedia dipulihkan")
    void wikiSh004_about() {
        driver.get("https://en.wikipedia.org");
        WebElement orig = waitVisible(By.xpath("//span[normalize-space()='About Wikipedia']/parent::a | //a[normalize-space()='About Wikipedia']"));
        setId(orig, "wikiAbout");
        mutateId("wikiAbout", "wikiAboutRefactor");

        WebElement el = heal("WIKI-SH-004", By.id("wikiAbout"),
                new ElementProfile("About Wikipedia", "wikiAbout", "id"));
        
        assertEquals("wikiAboutRefactor", el.getAttribute("id"));
        el.click();
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "wikipedia_id_change", THRESHOLD);
        WebElement result = healingDriver.findElement(brokenLocator, profile);
        assertNotNull(result, "[" + testCaseId + "] Self-healing gagal menemukan elemen pengganti.");
        assertTrue(result.isDisplayed(), "[" + testCaseId + "] Elemen hasil healing tidak terlihat.");
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
                "el.setAttribute('id',arguments[1]); return 1;",
                oldId, newId);
        assertEquals(1L, ((Number) r).longValue(), "Elemen tidak ditemukan saat mutasi: " + oldId);
    }

    private WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}
