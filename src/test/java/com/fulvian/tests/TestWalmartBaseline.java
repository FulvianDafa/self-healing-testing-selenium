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
 * Baseline test untuk SUT Walmart (https://www.walmart.com).
 * Jalankan: mvn clean test -Dtest=TestWalmartBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("WALMART - Baseline Test")
public class TestWalmartBaseline {

    private static final Path LOG_FILE = Paths.get("results", "walmart_baseline_log.csv");
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
    @DisplayName("WMT-BL-001 Walmart: input pencarian gagal saat id berubah")
    void wmtBl001_searchInput() {
        driver.get("https://www.walmart.com");
        WebElement orig = waitVisible(By.name("q"));
        setId(orig, "wmtSearchInput");
        mutate("wmtSearchInput", "wmtSearchInputRefactor");
        assertFails("WMT-BL-001", "By.id: wmtSearchInput", "id=wmtSearchInputRefactor", By.id("wmtSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("WMT-BL-002 Walmart: tombol pencarian gagal saat id berubah")
    void wmtBl002_searchButton() {
        driver.get("https://www.walmart.com");
        WebElement orig = waitVisible(By.xpath("//button[@type='submit' and @aria-label='Search']"));
        setId(orig, "wmtSearchButton");
        mutate("wmtSearchButton", "wmtSearchButtonRefactor");
        assertFails("WMT-BL-002", "By.id: wmtSearchButton", "id=wmtSearchButtonRefactor", By.id("wmtSearchButton"));
    }

    @Test @Order(3)
    @DisplayName("WMT-BL-003 Walmart: link Departments gagal saat id berubah")
    void wmtBl003_departments() {
        driver.get("https://www.walmart.com");
        WebElement orig = waitVisible(By.xpath("//button[@aria-label='Departments'] | //a[normalize-space()='Departments']"));
        setId(orig, "wmtDepartments");
        mutate("wmtDepartments", "wmtDepartmentsRefactor");
        assertFails("WMT-BL-003", "By.id: wmtDepartments", "id=wmtDepartmentsRefactor", By.id("wmtDepartments"));
    }

    @Test @Order(4)
    @DisplayName("WMT-BL-004 Walmart: link Cart gagal saat id berubah")
    void wmtBl004_cart() {
        driver.get("https://www.walmart.com");
        WebElement orig = waitVisible(By.xpath("//a[@link-identifier='Cart' or @aria-label='Cart']"));
        setId(orig, "wmtCart");
        mutate("wmtCart", "wmtCartRefactor");
        assertFails("WMT-BL-004", "By.id: wmtCart", "id=wmtCartRefactor", By.id("wmtCart"));
    }

    @Test @Order(5)
    @DisplayName("WMT-BL-005 Walmart: logo Walmart gagal saat id berubah")
    void wmtBl005_logo() {
        driver.get("https://www.walmart.com");
        WebElement orig = waitVisible(By.xpath("//a[@aria-label='Walmart homepage'] | //img[@alt='Walmart']/parent::a"));
        setId(orig, "wmtLogo");
        mutate("wmtLogo", "wmtLogoRefactor");
        assertFails("WMT-BL-005", "By.id: wmtLogo", "id=wmtLogoRefactor", By.id("wmtLogo"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "walmart", orig, mutated, status, msg});
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
