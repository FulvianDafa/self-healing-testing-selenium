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
 * Self-Healing test untuk SUT Rumah123 (https://www.rumah123.com).
 * Jalankan: mvn clean test -Dtest=TestRumah123Healing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RUMAH123 - Self-Healing Test")
public class TestRumah123Healing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/rumah123_healing_log.csv";

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
    @DisplayName("RMH-SH-001 Rumah123: input lokasi dipulihkan")
    void rmhSh001_searchInput() {
        driver.get("https://www.rumah123.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//input[contains(@placeholder,'Lokasi') or contains(@placeholder,'area')]"));
        setId(orig, "rmhSearchInput");
        mutateId("rmhSearchInput", "rmhSearchInputRefactor");

        WebElement el = heal("RMH-SH-001", By.id("rmhSearchInput"),
                new ElementProfile("Lokasi", "rmhSearchInput", "id"));
        assertEquals("rmhSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("Jakarta");
    }

    @Test @Order(2)
    @DisplayName("RMH-SH-002 Rumah123: tombol Cari dipulihkan")
    void rmhSh002_searchButton() {
        driver.get("https://www.rumah123.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//button[normalize-space()='Cari']"));
        setId(orig, "rmhSearchButton");
        mutateId("rmhSearchButton", "rmhSearchButtonRefactor");

        WebElement el = heal("RMH-SH-002", By.id("rmhSearchButton"),
                new ElementProfile("Cari", "rmhSearchButton", "id"));
        assertEquals("rmhSearchButtonRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(3)
    @DisplayName("RMH-SH-003 Rumah123: logo utama dipulihkan")
    void rmhSh003_logo() {
        driver.get("https://www.rumah123.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[.//img[contains(@alt,'Rumah123')]]"));
        setId(orig, "rmhLogo");
        mutateId("rmhLogo", "rmhLogoRefactor");

        WebElement el = heal("RMH-SH-003", By.id("rmhLogo"),
                new ElementProfile("Rumah123", "rmhLogo", "id"));
        assertEquals("rmhLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(4)
    @DisplayName("RMH-SH-004 Rumah123: tombol Dijual dipulihkan")
    void rmhSh004_dijualMenu() {
        driver.get("https://www.rumah123.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//button[normalize-space()='Dijual'] | //a[normalize-space()='Dijual']"));
        setId(orig, "rmhDijual");
        mutateId("rmhDijual", "rmhDijualRefactor");

        WebElement el = heal("RMH-SH-004", By.id("rmhDijual"),
                new ElementProfile("Dijual", "rmhDijual", "id"));
        assertEquals("rmhDijualRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(5)
    @DisplayName("RMH-SH-005 Rumah123: tombol Disewa dipulihkan")
    void rmhSh005_disewaMenu() {
        driver.get("https://www.rumah123.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//button[normalize-space()='Disewa'] | //a[normalize-space()='Disewa']"));
        setId(orig, "rmhDisewa");
        mutateId("rmhDisewa", "rmhDisewaRefactor");

        WebElement el = heal("RMH-SH-005", By.id("rmhDisewa"),
                new ElementProfile("Disewa", "rmhDisewa", "id"));
        assertEquals("rmhDisewaRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "rumah123_id_change", THRESHOLD);
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
