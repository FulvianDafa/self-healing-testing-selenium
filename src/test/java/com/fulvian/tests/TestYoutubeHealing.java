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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Self-Healing test untuk SUT YouTube (https://www.youtube.com).
 * Jalankan: mvn clean test -Dtest=TestYoutubeHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("YOUTUBE - Self-Healing Test")
public class TestYoutubeHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/youtube_healing_log.csv";

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

    private void dismissCookiePopup() {
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
            List<WebElement> btns = driver.findElements(By.xpath(
                "//button[contains(.,'Accept') or contains(.,'Agree') or contains(.,'I agree')]"));
            for (WebElement btn : btns) {
                if (btn.isDisplayed()) { btn.click(); break; }
            }
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("YT-SH-001 YouTube: input pencarian dipulihkan")
    void ytSh001_searchInput() {
        driver.get("https://www.youtube.com");
        dismissCookiePopup();
        WebElement orig = waitVisible(By.name("search_query"));
        setId(orig, "ytSearchInput");
        mutateId("ytSearchInput", "ytSearchInputRefactor");

        WebElement el = heal("YT-SH-001", By.id("ytSearchInput"),
                new ElementProfile("Search", "ytSearchInput", "id"));
        assertEquals("ytSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("Selenium tutorial");
    }

    @Test @Order(2)
    @DisplayName("YT-SH-002 YouTube: tombol pencarian dipulihkan")
    void ytSh002_searchButton() {
        driver.get("https://www.youtube.com");
        dismissCookiePopup();
        WebElement orig = waitVisible(By.id("search-icon-legacy"));
        setId(orig, "ytSearchButton");
        mutateId("ytSearchButton", "ytSearchButtonRefactor");

        WebElement el = heal("YT-SH-002", By.id("ytSearchButton"),
                new ElementProfile("Search", "ytSearchButton", "id"));
        assertEquals("ytSearchButtonRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(3)
    @DisplayName("YT-SH-003 YouTube: logo YouTube dipulihkan")
    void ytSh003_logo() {
        driver.get("https://www.youtube.com");
        dismissCookiePopup();
        WebElement orig = waitVisible(By.id("logo-icon"));
        setId(orig, "ytLogo");
        mutateId("ytLogo", "ytLogoRefactor");

        WebElement el = heal("YT-SH-003", By.id("ytLogo"),
                new ElementProfile("YouTube", "ytLogo", "id"));
        assertEquals("ytLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(4)
    @DisplayName("YT-SH-004 YouTube: link Trending dipulihkan")
    void ytSh004_trending() {
        driver.get("https://www.youtube.com");
        dismissCookiePopup();
        WebElement orig = waitVisible(By.xpath("//a[@title='Trending' or contains(@href,'/feed/trending')]"));
        setId(orig, "ytTrending");
        mutateId("ytTrending", "ytTrendingRefactor");

        WebElement el = heal("YT-SH-004", By.id("ytTrending"),
                new ElementProfile("Trending", "ytTrending", "id"));
        assertEquals("ytTrendingRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(5)
    @DisplayName("YT-SH-005 YouTube: link Home dipulihkan")
    void ytSh005_home() {
        driver.get("https://www.youtube.com");
        dismissCookiePopup();
        WebElement orig = waitVisible(By.xpath("//a[@title='Home' or @href='/']"));
        setId(orig, "ytHome");
        mutateId("ytHome", "ytHomeRefactor");

        WebElement el = heal("YT-SH-005", By.id("ytHome"),
                new ElementProfile("Home", "ytHome", "id"));
        assertEquals("ytHomeRefactor", el.getAttribute("id"));
        el.click();
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "youtube_id_change", THRESHOLD);
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
