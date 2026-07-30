package com.fulvian.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Baseline test untuk SUT Dicoding (https://www.dicoding.com).
 * Jalankan: mvn clean test -Dtest=TestDicodingBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DICODING - Baseline Test")
public class TestDicodingBaseline {

    private static final Path LOG_FILE = Paths.get("results", "dicoding_baseline_log.csv");
    private static final List<String[]> LOGS = new ArrayList<>();
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    @BeforeAll
    static void prepareLog() throws IOException {
        Files.createDirectories(LOG_FILE.getParent());
        LOGS.clear();
        LOGS.add(new String[]{"timestamp","test_case_id","sut","original_locator","mutated_locator","status","message"});
    }

    @AfterAll
    static void writeLog() throws IOException {
        List<String> lines = new ArrayList<>();
        for (String[] row : LOGS) lines.add(toCsv(row));
        Files.write(LOG_FILE, lines, StandardCharsets.UTF_8);
        System.out.println("[BASELINE] Log tersimpan: " + LOG_FILE.toAbsolutePath());
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
    @DisplayName("DCD-BL-001 Dicoding: input pencarian gagal saat id berubah")
    void dcdBl001_searchInput() {
        driver.get("https://www.dicoding.com/learningpaths");
        WebElement orig = waitVisible(By.xpath("//input[@type='search' or @placeholder='Apa yang ingin Anda pelajari?' or @aria-label='Apa yang ingin Anda pelajari?']"));
        setId(orig, "dcdSearchInput");
        mutate("dcdSearchInput", "dcdSearchInputRefactor");
        assertFails("DCD-BL-001", "By.id: dcdSearchInput", "id=dcdSearchInputRefactor", By.id("dcdSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("DCD-BL-002 Dicoding: logo Dicoding gagal saat id berubah")
    void dcdBl002_logo() {
        driver.get("https://www.dicoding.com");
        WebElement orig = waitVisible(By.xpath("//a[./img[contains(@alt,'Dicoding')] or contains(@class,'logo')]"));
        setId(orig, "dcdLogo");
        mutate("dcdLogo", "dcdLogoRefactor");
        assertFails("DCD-BL-002", "By.id: dcdLogo", "id=dcdLogoRefactor", By.id("dcdLogo"));
    }

    @Test @Order(3)
    @DisplayName("DCD-BL-003 Dicoding: link Akademi gagal saat id berubah")
    void dcdBl003_academies() {
        driver.get("https://www.dicoding.com");
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Akademi' or contains(@href,'academies')]"));
        setId(orig, "dcdAkademi");
        mutate("dcdAkademi", "dcdAkademiRefactor");
        assertFails("DCD-BL-003", "By.id: dcdAkademi", "id=dcdAkademiRefactor", By.id("dcdAkademi"));
    }

    @Test @Order(4)
    @DisplayName("DCD-BL-004 Dicoding: link Learning Paths gagal saat id berubah")
    void dcdBl004_learningPaths() {
        driver.get("https://www.dicoding.com");
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Learning Paths' or contains(@href,'learningpaths')]"));
        setId(orig, "dcdLearningPaths");
        mutate("dcdLearningPaths", "dcdLearningPathsRefactor");
        assertFails("DCD-BL-004", "By.id: dcdLearningPaths", "id=dcdLearningPathsRefactor", By.id("dcdLearningPaths"));
    }

    @Test @Order(5)
    @DisplayName("DCD-BL-005 Dicoding: tombol Daftar gagal saat id berubah")
    void dcdBl005_register() {
        driver.get("https://www.dicoding.com");
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Daftar' or normalize-space()='Register']"));
        setId(orig, "dcdRegister");
        mutate("dcdRegister", "dcdRegisterRefactor");
        assertFails("DCD-BL-005", "By.id: dcdRegister", "id=dcdRegisterRefactor", By.id("dcdRegister"));
    }

    private void setId(WebElement el, String id) {
        js.executeScript("arguments[0].setAttribute('id', arguments[1]);", el, id);
    }
    private void mutate(String oldId, String newId) {
        Object r = js.executeScript(
                "const el=document.getElementById(arguments[0]);" +
                "if(!el) return 0;" +
                "el.setAttribute('data-self-healing-old-id',arguments[0]);" +
                "el.setAttribute('id',arguments[1]); return 1;", oldId, newId);
        assertEquals(1L, ((Number) r).longValue(), "Elemen tidak ditemukan saat mutasi: " + oldId);
    }
    private void assertFails(String id, String orig, String mutated, By locator) {
        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> driver.findElement(locator));
        log(id, orig, mutated, "LOCATOR_FAILED_EXPECTED", ex.getClass().getSimpleName());
    }
    private void log(String id, String orig, String mutated, String status, String msg) {
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "dicoding", orig, mutated, status, msg});
    }
    private WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    private static String toCsv(String[] row) {
        List<String> e = new ArrayList<>();
        for (String v : row) e.add("\"" + (v == null ? "" : v.replace("\"", "\"\"")) + "\"");
        return String.join(",", e);
    }
}
