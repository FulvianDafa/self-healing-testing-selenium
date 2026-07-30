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
 * Baseline test untuk SUT Target (https://www.target.com).
 * Jalankan: mvn clean test -Dtest=TestTargetBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("TARGET - Baseline Test")
public class TestTargetBaseline {

    private static final Path LOG_FILE = Paths.get("results", "target_baseline_log.csv");
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
    @DisplayName("TGT-BL-001 Target: input pencarian gagal saat id berubah")
    void tgtBl001_searchInput() {
        driver.get("https://www.target.com");
        WebElement orig = waitVisible(By.id("search"));
        setId(orig, "tgtSearchInput");
        mutate("tgtSearchInput", "tgtSearchInputRefactor");
        assertFails("TGT-BL-001", "By.id: tgtSearchInput", "id=tgtSearchInputRefactor", By.id("tgtSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("TGT-BL-002 Target: tombol pencarian gagal saat id berubah")
    void tgtBl002_searchButton() {
        driver.get("https://www.target.com");
        WebElement orig = waitVisible(By.xpath("//button[@aria-label='search' or @aria-label='Search']"));
        setId(orig, "tgtSearchButton");
        mutate("tgtSearchButton", "tgtSearchButtonRefactor");
        assertFails("TGT-BL-002", "By.id: tgtSearchButton", "id=tgtSearchButtonRefactor", By.id("tgtSearchButton"));
    }

    @Test @Order(3)
    @DisplayName("TGT-BL-003 Target: link Cart gagal saat id berubah")
    void tgtBl003_cart() {
        driver.get("https://www.target.com");
        WebElement orig = waitVisible(By.xpath("//a[@aria-label='cart' or @aria-label='Cart']"));
        setId(orig, "tgtCart");
        mutate("tgtCart", "tgtCartRefactor");
        assertFails("TGT-BL-003", "By.id: tgtCart", "id=tgtCartRefactor", By.id("tgtCart"));
    }

    @Test @Order(4)
    @DisplayName("TGT-BL-004 Target: logo Target gagal saat id berubah")
    void tgtBl004_logo() {
        driver.get("https://www.target.com");
        WebElement orig = waitVisible(By.xpath("//a[@aria-label='Target home'] | //img[@alt='Target']/parent::a"));
        setId(orig, "tgtLogo");
        mutate("tgtLogo", "tgtLogoRefactor");
        assertFails("TGT-BL-004", "By.id: tgtLogo", "id=tgtLogoRefactor", By.id("tgtLogo"));
    }

    @Test @Order(5)
    @DisplayName("TGT-BL-005 Target: link Categories gagal saat id berubah")
    void tgtBl005_categories() {
        driver.get("https://www.target.com");
        WebElement orig = waitVisible(By.xpath("//button[normalize-space()='Categories'] | //a[normalize-space()='Categories']"));
        setId(orig, "tgtCategories");
        mutate("tgtCategories", "tgtCategoriesRefactor");
        assertFails("TGT-BL-005", "By.id: tgtCategories", "id=tgtCategoriesRefactor", By.id("tgtCategories"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "target", orig, mutated, status, msg});
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
