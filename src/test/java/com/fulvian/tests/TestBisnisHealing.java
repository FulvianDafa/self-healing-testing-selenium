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
 * Self-Healing test untuk SUT Bisnis (https://www.bisnis.com).
 * Jalankan: mvn clean test -Dtest=TestBisnisHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("BISNIS.COM - Self-Healing Test")
public class TestBisnisHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/bisnis_healing_log.csv";

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
            driver.findElements(By.xpath("//button[contains(@class,'close') or @aria-label='Close']"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("BSN-SH-001 Bisnis: icon search dipulihkan")
    void bsnSh001_searchIcon() {
        driver.get("https://www.bisnis.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'search.bisnis') or @aria-label='search' or contains(@class,'search')]"));
        setId(orig, "bsnSearchIcon");
        mutateId("bsnSearchIcon", "bsnSearchIconRefactor");

        WebElement el = heal("BSN-SH-001", By.id("bsnSearchIcon"),
                new ElementProfile("Search", "bsnSearchIcon", "id"));
        assertEquals("bsnSearchIconRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(2)
    @DisplayName("BSN-SH-002 Bisnis: logo utama dipulihkan")
    void bsnSh002_logo() {
        driver.get("https://www.bisnis.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[.//img[contains(@alt,'Bisnis')]]"));
        setId(orig, "bsnLogo");
        mutateId("bsnLogo", "bsnLogoRefactor");

        WebElement el = heal("BSN-SH-002", By.id("bsnLogo"),
                new ElementProfile("Bisnis", "bsnLogo", "id"));
        assertEquals("bsnLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(3)
    @DisplayName("BSN-SH-003 Bisnis: menu Market dipulihkan")
    void bsnSh003_market() {
        driver.get("https://www.bisnis.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Market' or contains(@href,'market.bisnis')]"));
        setId(orig, "bsnMarket");
        mutateId("bsnMarket", "bsnMarketRefactor");

        WebElement el = heal("BSN-SH-003", By.id("bsnMarket"),
                new ElementProfile("Market", "bsnMarket", "id"));
        assertEquals("bsnMarketRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(4)
    @DisplayName("BSN-SH-004 Bisnis: menu Finansial dipulihkan")
    void bsnSh004_finansial() {
        driver.get("https://www.bisnis.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Finansial' or contains(@href,'finansial.bisnis')]"));
        setId(orig, "bsnFinansial");
        mutateId("bsnFinansial", "bsnFinansialRefactor");

        WebElement el = heal("BSN-SH-004", By.id("bsnFinansial"),
                new ElementProfile("Finansial", "bsnFinansial", "id"));
        assertEquals("bsnFinansialRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(5)
    @DisplayName("BSN-SH-005 Bisnis: link Terpopuler dipulihkan")
    void bsnSh005_terpopuler() {
        driver.get("https://www.bisnis.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Terpopuler' or contains(normalize-space(),'Populer')]"));
        setId(orig, "bsnTerpopuler");
        mutateId("bsnTerpopuler", "bsnTerpopulerRefactor");

        WebElement el = heal("BSN-SH-005", By.id("bsnTerpopuler"),
                new ElementProfile("Terpopuler", "bsnTerpopuler", "id"));
        assertEquals("bsnTerpopulerRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "bisnis_id_change", THRESHOLD);
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
