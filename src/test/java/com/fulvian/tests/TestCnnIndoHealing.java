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
 * Self-Healing test untuk SUT CNN Indonesia (https://www.cnnindonesia.com).
 * Jalankan: mvn clean test -Dtest=TestCnnIndoHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("CNN INDONESIA - Self-Healing Test")
public class TestCnnIndoHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/cnnindo_healing_log.csv";

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
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
            driver.findElements(By.xpath("//button[contains(@class,'close') or contains(@aria-label,'Close')]"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("CNN-SH-001 CNN Indo: icon search dipulihkan")
    void cnnSh001_searchIcon() {
        driver.get("https://www.cnnindonesia.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@class,'search-btn') or contains(@class,'btn-search')]"));
        setId(orig, "cnnSearchIcon");
        mutateId("cnnSearchIcon", "cnnSearchIconRefactor");

        WebElement el = heal("CNN-SH-001", By.id("cnnSearchIcon"),
                new ElementProfile("Search", "cnnSearchIcon", "id"));
        assertEquals("cnnSearchIconRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(2)
    @DisplayName("CNN-SH-002 CNN Indo: input pencarian dipulihkan")
    void cnnSh002_searchInput() {
        driver.get("https://www.cnnindonesia.com");
        dismissPopup();
        try { driver.findElement(By.xpath("//a[contains(@class,'search-btn')]")).click(); } catch(Exception e){}
        WebElement orig = waitVisible(By.id("searchinput"));
        setId(orig, "cnnSearchInput");
        mutateId("cnnSearchInput", "cnnSearchInputRefactor");

        WebElement el = heal("CNN-SH-002", By.id("cnnSearchInput"),
                new ElementProfile("Cari Berita", "cnnSearchInput", "id"));
        assertEquals("cnnSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("berita terkini");
    }

    @Test @Order(3)
    @DisplayName("CNN-SH-003 CNN Indo: logo utama dipulihkan")
    void cnnSh003_logo() {
        driver.get("https://www.cnnindonesia.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'cnnindonesia.com') and .//img[contains(@alt,'CNN')]]"));
        setId(orig, "cnnLogo");
        mutateId("cnnLogo", "cnnLogoRefactor");

        WebElement el = heal("CNN-SH-003", By.id("cnnLogo"),
                new ElementProfile("CNN Indonesia", "cnnLogo", "id"));
        assertEquals("cnnLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(4)
    @DisplayName("CNN-SH-004 CNN Indo: link Nasional dipulihkan")
    void cnnSh004_nasional() {
        driver.get("https://www.cnnindonesia.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Nasional' and contains(@href,'nasional')]"));
        setId(orig, "cnnNasional");
        mutateId("cnnNasional", "cnnNasionalRefactor");

        WebElement el = heal("CNN-SH-004", By.id("cnnNasional"),
                new ElementProfile("Nasional", "cnnNasional", "id"));
        assertEquals("cnnNasionalRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(5)
    @DisplayName("CNN-SH-005 CNN Indo: link Ekonomi dipulihkan")
    void cnnSh005_ekonomi() {
        driver.get("https://www.cnnindonesia.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Ekonomi' and contains(@href,'ekonomi')]"));
        setId(orig, "cnnEkonomi");
        mutateId("cnnEkonomi", "cnnEkonomiRefactor");

        WebElement el = heal("CNN-SH-005", By.id("cnnEkonomi"),
                new ElementProfile("Ekonomi", "cnnEkonomi", "id"));
        assertEquals("cnnEkonomiRefactor", el.getAttribute("id"));
        el.click();
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "cnnindo_id_change", THRESHOLD);
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
