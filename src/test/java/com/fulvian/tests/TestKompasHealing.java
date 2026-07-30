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
 * Self-Healing test untuk SUT Kompas.com (https://www.kompas.com).
 * Jalankan: mvn clean test -Dtest=TestKompasHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("KOMPAS - Self-Healing Test")
public class TestKompasHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/kompas_healing_log.csv";

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
            List<WebElement> closeBtns = driver.findElements(
                By.xpath("//button[contains(@class,'close') or contains(@aria-label,'close') or contains(@aria-label,'Close') or contains(@id,'close')]"));
            closeBtns.stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("KMP-SH-001 Kompas: input pencarian dipulihkan")
    void kmpSh001_searchInput() {
        driver.get("https://www.kompas.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//input[@placeholder='Cari' or @id='cSearch' or @name='q']"));
        setId(orig, "kmpSearchInput");
        mutateId("kmpSearchInput", "kmpSearchInputRefactor");

        WebElement el = heal("KMP-SH-001", By.id("kmpSearchInput"),
                new ElementProfile("Cari", "kmpSearchInput", "id"));
        assertEquals("kmpSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("berita terbaru");
    }

    @Test @Order(2)
    @DisplayName("KMP-SH-002 Kompas: logo Kompas dipulihkan")
    void kmpSh002_logo() {
        driver.get("https://www.kompas.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'kompas.com') and (./img[contains(@alt,'Kompas')] or contains(@class,'logo'))]"));
        setId(orig, "kmpLogo");
        mutateId("kmpLogo", "kmpLogoRefactor");

        WebElement el = heal("KMP-SH-002", By.id("kmpLogo"),
                new ElementProfile("Kompas", "kmpLogo", "id"));
        assertEquals("kmpLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(3)
    @DisplayName("KMP-SH-003 Kompas: link Nasional dipulihkan")
    void kmpSh003_nasional() {
        driver.get("https://www.kompas.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Nasional' and contains(@href,'nasional')]"));
        setId(orig, "kmpNasional");
        mutateId("kmpNasional", "kmpNasionalRefactor");

        WebElement el = heal("KMP-SH-003", By.id("kmpNasional"),
                new ElementProfile("Nasional", "kmpNasional", "id"));
        assertEquals("kmpNasionalRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(4)
    @DisplayName("KMP-SH-004 Kompas: link Regional dipulihkan")
    void kmpSh004_regional() {
        driver.get("https://www.kompas.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Regional' and contains(@href,'regional')]"));
        setId(orig, "kmpRegional");
        mutateId("kmpRegional", "kmpRegionalRefactor");

        WebElement el = heal("KMP-SH-004", By.id("kmpRegional"),
                new ElementProfile("Regional", "kmpRegional", "id"));
        assertEquals("kmpRegionalRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(5)
    @DisplayName("KMP-SH-005 Kompas: link Olahraga dipulihkan")
    void kmpSh005_olahraga() {
        driver.get("https://www.kompas.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Olahraga' or normalize-space()='Sports']"));
        setId(orig, "kmpOlahraga");
        mutateId("kmpOlahraga", "kmpOlahragaRefactor");

        WebElement el = heal("KMP-SH-005", By.id("kmpOlahraga"),
                new ElementProfile("Olahraga", "kmpOlahraga", "id"));
        assertEquals("kmpOlahragaRefactor", el.getAttribute("id"));
        el.click();
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "kompas_id_change", THRESHOLD);
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
