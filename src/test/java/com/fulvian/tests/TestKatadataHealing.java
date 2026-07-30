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
 * Self-Healing test untuk SUT Katadata (https://www.katadata.co.id).
 * Jalankan: mvn clean test -Dtest=TestKatadataHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("KATADATA - Self-Healing Test")
public class TestKatadataHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/katadata_healing_log.csv";

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
            driver.findElements(By.xpath("//button[contains(@class,'close')] | //div[contains(@class,'close')]"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("KTD-SH-001 Katadata: input pencarian dipulihkan")
    void ktdSh001_searchInput() {
        driver.get("https://katadata.co.id/");
        dismissPopup();
        WebElement orig = waitVisible(By.id("inputSearch"));
        setId(orig, "ktdSearchInput");
        mutateId("ktdSearchInput", "ktdSearchInputRefactor");

        WebElement el = heal("KTD-SH-001", By.id("ktdSearchInput"),
                new ElementProfile("Cari di Katadata", "ktdSearchInput", "id"));
        assertEquals("ktdSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("ekonomi global");
    }

    @Test @Order(2)
    @DisplayName("KTD-SH-002 Katadata: logo Katadata dipulihkan")
    void ktdSh002_logo() {
        driver.get("https://katadata.co.id/");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[.//img[contains(@alt,'Katadata')]]"));
        setId(orig, "ktdLogo");
        mutateId("ktdLogo", "ktdLogoRefactor");

        WebElement el = heal("KTD-SH-002", By.id("ktdLogo"),
                new ElementProfile("Katadata", "ktdLogo", "id"));
        assertEquals("ktdLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(3)
    @DisplayName("KTD-SH-003 Katadata: link Finansial dipulihkan")
    void ktdSh003_finansial() {
        driver.get("https://katadata.co.id/");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Finansial' or contains(@href,'finansial')]"));
        setId(orig, "ktdFinansial");
        mutateId("ktdFinansial", "ktdFinansialRefactor");

        WebElement el = heal("KTD-SH-003", By.id("ktdFinansial"),
                new ElementProfile("Finansial", "ktdFinansial", "id"));
        assertEquals("ktdFinansialRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(4)
    @DisplayName("KTD-SH-004 Katadata: link Berita utama dipulihkan")
    void ktdSh004_berita() {
        driver.get("https://katadata.co.id/");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Berita' or contains(@href,'berita')]"));
        setId(orig, "ktdBerita");
        mutateId("ktdBerita", "ktdBeritaRefactor");

        WebElement el = heal("KTD-SH-004", By.id("ktdBerita"),
                new ElementProfile("Berita", "ktdBerita", "id"));
        assertEquals("ktdBeritaRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(5)
    @DisplayName("KTD-SH-005 Katadata: link Databoks dipulihkan")
    void ktdSh005_databoks() {
        driver.get("https://katadata.co.id/");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(normalize-space(),'Databoks') or contains(@href,'databoks')]"));
        setId(orig, "ktdDataboks");
        mutateId("ktdDataboks", "ktdDataboksRefactor");

        WebElement el = heal("KTD-SH-005", By.id("ktdDataboks"),
                new ElementProfile("Databoks", "ktdDataboks", "id"));
        assertEquals("ktdDataboksRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "katadata_id_change", THRESHOLD);
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
