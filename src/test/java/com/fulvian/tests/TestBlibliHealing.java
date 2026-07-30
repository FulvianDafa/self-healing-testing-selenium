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
 * Self-Healing test untuk SUT Blibli (https://www.blibli.com).
 * Jalankan: mvn clean test -Dtest=TestBlibliHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("BLIBLI - Self-Healing Test")
public class TestBlibliHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/blibli_healing_log.csv";

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
            driver.findElements(By.xpath("//button[contains(@class,'close') or @aria-label='Tutup' or @aria-label='Close' or contains(text(),'Nanti saja')]"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("BLB-SH-001 Blibli: input pencarian dipulihkan")
    void blbSh001_searchInput() {
        driver.get("https://www.blibli.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//input[contains(@placeholder,'Cari brand')]"));
        setId(orig, "blbSearchInput");
        mutateId("blbSearchInput", "blbSearchInputRefactor");

        WebElement el = heal("BLB-SH-001", By.id("blbSearchInput"),
                new ElementProfile("Cari brand", "blbSearchInput", "id"));
        assertEquals("blbSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("Samsung");
    }

    @Test @Order(2)
    @DisplayName("BLB-SH-002 Blibli: tombol pencarian dipulihkan")
    void blbSh002_searchButton() {
        driver.get("https://www.blibli.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//button[@title='search' or contains(@class,'search')]"));
        setId(orig, "blbSearchButton");
        mutateId("blbSearchButton", "blbSearchButtonRefactor");

        WebElement el = heal("BLB-SH-002", By.id("blbSearchButton"),
                new ElementProfile("Search", "blbSearchButton", "id"));
        assertEquals("blbSearchButtonRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(3)
    @DisplayName("BLB-SH-003 Blibli: logo Blibli dipulihkan")
    void blbSh003_logo() {
        driver.get("https://www.blibli.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[.//img[contains(@alt,'Blibli')]]"));
        setId(orig, "blbLogo");
        mutateId("blbLogo", "blbLogoRefactor");

        WebElement el = heal("BLB-SH-003", By.id("blbLogo"),
                new ElementProfile("Blibli", "blbLogo", "id"));
        assertEquals("blbLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(4)
    @DisplayName("BLB-SH-004 Blibli: ikon Keranjang dipulihkan")
    void blbSh004_cart() {
        driver.get("https://www.blibli.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'/cart')]"));
        setId(orig, "blbCart");
        mutateId("blbCart", "blbCartRefactor");

        WebElement el = heal("BLB-SH-004", By.id("blbCart"),
                new ElementProfile("Cart", "blbCart", "id"));
        assertEquals("blbCartRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(5)
    @DisplayName("BLB-SH-005 Blibli: tombol Masuk dipulihkan")
    void blbSh005_login() {
        driver.get("https://www.blibli.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//button[normalize-space()='Masuk' or contains(@class,'login')]"));
        setId(orig, "blbLogin");
        mutateId("blbLogin", "blbLoginRefactor");

        WebElement el = heal("BLB-SH-005", By.id("blbLogin"),
                new ElementProfile("Masuk", "blbLogin", "id"));
        assertEquals("blbLoginRefactor", el.getAttribute("id"));
        el.click();
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "blibli_id_change", THRESHOLD);
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
