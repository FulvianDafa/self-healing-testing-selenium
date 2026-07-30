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
 * Self-Healing test untuk SUT Dicoding (https://www.dicoding.com).
 * Jalankan: mvn clean test -Dtest=TestDicodingHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DICODING - Self-Healing Test")
public class TestDicodingHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/dicoding_healing_log.csv";

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
    @DisplayName("DCD-SH-001 Dicoding: input pencarian dipulihkan")
    void dcdSh001_searchInput() {
        driver.get("https://www.dicoding.com/learningpaths");
        WebElement orig = waitVisible(By.xpath("//input[@type='search' or @placeholder='Apa yang ingin Anda pelajari?' or @aria-label='Apa yang ingin Anda pelajari?']"));
        setId(orig, "dcdSearchInput");
        mutateId("dcdSearchInput", "dcdSearchInputRefactor");

        WebElement el = heal("DCD-SH-001", By.id("dcdSearchInput"),
                new ElementProfile("Apa yang ingin Anda pelajari", "dcdSearchInput", "id"));
        assertEquals("dcdSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("Android");
    }

    @Test @Order(2)
    @DisplayName("DCD-SH-002 Dicoding: logo Dicoding dipulihkan")
    void dcdSh002_logo() {
        driver.get("https://www.dicoding.com");
        WebElement orig = waitVisible(By.xpath("//a[./img[contains(@alt,'Dicoding')] or contains(@class,'logo')]"));
        setId(orig, "dcdLogo");
        mutateId("dcdLogo", "dcdLogoRefactor");

        WebElement el = heal("DCD-SH-002", By.id("dcdLogo"),
                new ElementProfile("Dicoding", "dcdLogo", "id"));
        assertEquals("dcdLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(3)
    @DisplayName("DCD-SH-003 Dicoding: link Akademi dipulihkan")
    void dcdSh003_academies() {
        driver.get("https://www.dicoding.com");
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Akademi' or contains(@href,'academies')]"));
        setId(orig, "dcdAkademi");
        mutateId("dcdAkademi", "dcdAkademiRefactor");

        WebElement el = heal("DCD-SH-003", By.id("dcdAkademi"),
                new ElementProfile("Akademi", "dcdAkademi", "id"));
        assertEquals("dcdAkademiRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(4)
    @DisplayName("DCD-SH-004 Dicoding: link Learning Paths dipulihkan")
    void dcdSh004_learningPaths() {
        driver.get("https://www.dicoding.com");
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Learning Paths' or contains(@href,'learningpaths')]"));
        setId(orig, "dcdLearningPaths");
        mutateId("dcdLearningPaths", "dcdLearningPathsRefactor");

        WebElement el = heal("DCD-SH-004", By.id("dcdLearningPaths"),
                new ElementProfile("Learning Paths", "dcdLearningPaths", "id"));
        assertEquals("dcdLearningPathsRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(5)
    @DisplayName("DCD-SH-005 Dicoding: tombol Daftar dipulihkan")
    void dcdSh005_register() {
        driver.get("https://www.dicoding.com");
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Daftar' or normalize-space()='Register']"));
        setId(orig, "dcdRegister");
        mutateId("dcdRegister", "dcdRegisterRefactor");

        WebElement el = heal("DCD-SH-005", By.id("dcdRegister"),
                new ElementProfile("Daftar", "dcdRegister", "id"));
        assertEquals("dcdRegisterRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "dicoding_id_change", THRESHOLD);
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
