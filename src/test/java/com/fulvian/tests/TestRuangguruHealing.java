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
 * Self-Healing test untuk SUT Ruangguru (https://www.ruangguru.com).
 * Jalankan: mvn clean test -Dtest=TestRuangguruHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RUANGGURU - Self-Healing Test")
public class TestRuangguruHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/ruangguru_healing_log.csv";

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
            driver.findElements(By.xpath("//button[contains(@class,'close') or @aria-label='Close' or @aria-label='Tutup' or contains(text(),'Nanti saja')] | //div[contains(@class,'modal')]//span[text()='x']"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("RGG-SH-001 Ruangguru: input pencarian blog dipulihkan")
    void rggSh001_searchInput() {
        driver.get("https://www.ruangguru.com/blog");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//input[@aria-label='Cari artikel di sini!' or @placeholder='Cari artikel di sini!']"));
        setId(orig, "rggSearchInput");
        mutateId("rggSearchInput", "rggSearchInputRefactor");

        WebElement el = heal("RGG-SH-001", By.id("rggSearchInput"),
                new ElementProfile("Cari artikel", "rggSearchInput", "id"));
        assertEquals("rggSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("Ujian Nasional");
    }

    @Test @Order(2)
    @DisplayName("RGG-SH-002 Ruangguru: tombol pencarian blog dipulihkan")
    void rggSh002_searchButton() {
        driver.get("https://www.ruangguru.com/blog");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//button[@type='submit' and contains(@class,'search')]"));
        setId(orig, "rggSearchButton");
        mutateId("rggSearchButton", "rggSearchButtonRefactor");

        WebElement el = heal("RGG-SH-002", By.id("rggSearchButton"),
                new ElementProfile("Search", "rggSearchButton", "id"));
        assertEquals("rggSearchButtonRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(3)
    @DisplayName("RGG-SH-003 Ruangguru: logo Ruangguru dipulihkan")
    void rggSh003_logo() {
        driver.get("https://www.ruangguru.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[.//img[contains(@alt,'Ruangguru')]]"));
        setId(orig, "rggLogo");
        mutateId("rggLogo", "rggLogoRefactor");

        WebElement el = heal("RGG-SH-003", By.id("rggLogo"),
                new ElementProfile("Ruangguru", "rggLogo", "id"));
        assertEquals("rggLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(4)
    @DisplayName("RGG-SH-004 Ruangguru: link SD/SMP/SMA dipulihkan")
    void rggSh004_sdSmpSma() {
        driver.get("https://www.ruangguru.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(normalize-space(),'SD') and contains(normalize-space(),'SMP')]"));
        setId(orig, "rggSdSmpSma");
        mutateId("rggSdSmpSma", "rggSdSmpSmaRefactor");

        WebElement el = heal("RGG-SH-004", By.id("rggSdSmpSma"),
                new ElementProfile("SD SMP SMA", "rggSdSmpSma", "id"));
        assertEquals("rggSdSmpSmaRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(5)
    @DisplayName("RGG-SH-005 Ruangguru: tombol Masuk/Daftar dipulihkan")
    void rggSh005_login() {
        driver.get("https://www.ruangguru.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Masuk / Daftar' or normalize-space()='Masuk']"));
        setId(orig, "rggLogin");
        mutateId("rggLogin", "rggLoginRefactor");

        WebElement el = heal("RGG-SH-005", By.id("rggLogin"),
                new ElementProfile("Masuk Daftar", "rggLogin", "id"));
        assertEquals("rggLoginRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "ruangguru_id_change", THRESHOLD);
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
