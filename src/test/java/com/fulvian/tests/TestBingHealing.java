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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("BING - Self-Healing Test")
public class TestBingHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/bing_healing_log.csv";

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

    private void handleCookiePopup() {
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
            List<WebElement> acceptButtons = driver.findElements(By.xpath(
                "//button[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept') or @id='bnp_btn_accept']"
            ));
            for (WebElement btn : acceptButtons) {
                if (btn.isDisplayed()) {
                    btn.click();
                    break;
                }
            }
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("BING-SH-001 Bing: input pencarian dipulihkan")
    void bingSh001_searchInput() {
        driver.get("https://www.bing.com");
        handleCookiePopup();
        WebElement orig = waitVisible(By.name("q"));
        setId(orig, "bingSearchInput");
        mutateId("bingSearchInput", "bingSearchInputRefactor");

        WebElement el = heal("BING-SH-001", By.id("bingSearchInput"),
                new ElementProfile("Search", "bingSearchInput", "id"));
        
        assertEquals("bingSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("OpenAI");
    }

    @Test @Order(2)
    @DisplayName("BING-SH-002 Bing: tombol pencarian dipulihkan")
    void bingSh002_searchButton() {
        driver.get("https://www.bing.com");
        handleCookiePopup();
        WebElement orig = waitVisible(By.xpath("//label[@id='search_icon' or contains(@class, 'search')]"));
        setId(orig, "bingSearchButton");
        mutateId("bingSearchButton", "bingSearchButtonRefactor");

        WebElement el = heal("BING-SH-002", By.id("bingSearchButton"),
                new ElementProfile("Search", "bingSearchButton", "id"));
        
        assertEquals("bingSearchButtonRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(3)
    @DisplayName("BING-SH-003 Bing: link Images dipulihkan")
    void bingSh003_imagesLink() {
        driver.get("https://www.bing.com");
        handleCookiePopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(text(), 'Images') or contains(text(), 'Gambar')]"));
        setId(orig, "bingImagesLink");
        mutateId("bingImagesLink", "bingImagesLinkRefactor");

        WebElement el = heal("BING-SH-003", By.id("bingImagesLink"),
                new ElementProfile("Images", "bingImagesLink", "id"));
        
        assertEquals("bingImagesLinkRefactor", el.getAttribute("id"));
        el.click();
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "bing_id_change", THRESHOLD);
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
