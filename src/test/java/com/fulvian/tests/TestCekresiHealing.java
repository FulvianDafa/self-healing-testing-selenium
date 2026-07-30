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
 * Self-Healing test untuk SUT Cekresi.com (https://www.cekresi.com).
 * Jalankan: mvn clean test -Dtest=TestCekresiHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("CEKRESI - Self-Healing Test")
public class TestCekresiHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/cekresi_healing_log.csv";

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

    @Test @Order(1)
    @DisplayName("CKR-SH-001 Cekresi: input nomor resi dipulihkan")
    void ckrSh001_resiInput() {
        driver.get("https://www.cekresi.com");
        WebElement orig = waitVisible(By.id("noresi"));
        setId(orig, "ckrResiInput");
        mutateId("ckrResiInput", "ckrResiInputRefactor");

        WebElement el = heal("CKR-SH-001", By.id("ckrResiInput"),
                new ElementProfile("Masukkan nomor resi", "ckrResiInput", "id"));
        assertEquals("ckrResiInputRefactor", el.getAttribute("id"));
        el.sendKeys("JT123456789");
    }

    @Test @Order(2)
    @DisplayName("CKR-SH-002 Cekresi: tombol Cek Resi dipulihkan")
    void ckrSh002_checkButton() {
        driver.get("https://www.cekresi.com");
        WebElement orig = waitVisible(By.id("cekresi"));
        setId(orig, "ckrCheckButton");
        mutateId("ckrCheckButton", "ckrCheckButtonRefactor");

        WebElement el = heal("CKR-SH-002", By.id("ckrCheckButton"),
                new ElementProfile("Cek Resi", "ckrCheckButton", "id"));
        assertEquals("ckrCheckButtonRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(3)
    @DisplayName("CKR-SH-003 Cekresi: logo Cekresi dipulihkan")
    void ckrSh003_logo() {
        driver.get("https://www.cekresi.com");
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'cekresi.com') and (./img or contains(@class,'logo') or contains(@class,'brand'))]"));
        setId(orig, "ckrLogo");
        mutateId("ckrLogo", "ckrLogoRefactor");

        WebElement el = heal("CKR-SH-003", By.id("ckrLogo"),
                new ElementProfile("Cek Resi", "ckrLogo", "id"));
        assertEquals("ckrLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(4)
    @DisplayName("CKR-SH-004 Cekresi: dropdown ekspedisi dipulihkan")
    void ckrSh004_expedition() {
        driver.get("https://www.cekresi.com");
        WebElement orig = waitVisible(By.xpath("//select[@name='kurir' or @id='kurir'] | //select[contains(@class,'kurir')]"));
        setId(orig, "ckrExpedition");
        mutateId("ckrExpedition", "ckrExpeditionRefactor");

        WebElement el = heal("CKR-SH-004", By.id("ckrExpedition"),
                new ElementProfile("Pilih ekspedisi", "ckrExpedition", "id"));
        assertEquals("ckrExpeditionRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(5)
    @DisplayName("CKR-SH-005 Cekresi: heading utama dipulihkan")
    void ckrSh005_heading() {
        driver.get("https://www.cekresi.com");
        WebElement orig = waitVisible(By.xpath("//h1 | //h2[contains(normalize-space(),'Cek Resi') or contains(normalize-space(),'Tracking')]"));
        setId(orig, "ckrHeading");
        mutateId("ckrHeading", "ckrHeadingRefactor");

        WebElement el = heal("CKR-SH-005", By.id("ckrHeading"),
                new ElementProfile("Cek Resi Tracking", "ckrHeading", "id"));
        assertEquals("ckrHeadingRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "cekresi_id_change", THRESHOLD);
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
