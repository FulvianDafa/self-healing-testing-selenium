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
 * Baseline test untuk SUT Yahoo (https://www.yahoo.com).
 * Jalankan: mvn clean test -Dtest=TestYahooBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("YAHOO - Baseline Test")
public class TestYahooBaseline {

    private static final Path LOG_FILE = Paths.get("results", "yahoo_baseline_log.csv");
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

    private void dismissPopup() {
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
            driver.findElements(By.xpath("//button[normalize-space()='Accept all' or normalize-space()='Reject all']"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("YHOO-BL-001 Yahoo: input pencarian gagal saat id berubah")
    void yhooBl001_searchInput() {
        driver.get("https://www.yahoo.com");
        dismissPopup();
        WebElement orig = waitVisible(By.name("p"));
        setId(orig, "yhooSearchInput");
        mutate("yhooSearchInput", "yhooSearchInputRefactor");
        assertFails("YHOO-BL-001", "By.id: yhooSearchInput", "id=yhooSearchInputRefactor", By.id("yhooSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("YHOO-BL-002 Yahoo: tombol pencarian gagal saat id berubah")
    void yhooBl002_searchButton() {
        driver.get("https://www.yahoo.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//button[@type='submit' and @id='header-search-btn']"));
        setId(orig, "yhooSearchButton");
        mutate("yhooSearchButton", "yhooSearchButtonRefactor");
        assertFails("YHOO-BL-002", "By.id: yhooSearchButton", "id=yhooSearchButtonRefactor", By.id("yhooSearchButton"));
    }

    @Test @Order(3)
    @DisplayName("YHOO-BL-003 Yahoo: nav Finance gagal saat id berubah")
    void yhooBl003_finance() {
        driver.get("https://www.yahoo.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'finance') and contains(normalize-space(),'Finance')]"));
        setId(orig, "yhooFinance");
        mutate("yhooFinance", "yhooFinanceRefactor");
        assertFails("YHOO-BL-003", "By.id: yhooFinance", "id=yhooFinanceRefactor", By.id("yhooFinance"));
    }

    @Test @Order(4)
    @DisplayName("YHOO-BL-004 Yahoo: nav News gagal saat id berubah")
    void yhooBl004_news() {
        driver.get("https://www.yahoo.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'news') and contains(normalize-space(),'News')]"));
        setId(orig, "yhooNews");
        mutate("yhooNews", "yhooNewsRefactor");
        assertFails("YHOO-BL-004", "By.id: yhooNews", "id=yhooNewsRefactor", By.id("yhooNews"));
    }

    @Test @Order(5)
    @DisplayName("YHOO-BL-005 Yahoo: nav Sports gagal saat id berubah")
    void yhooBl005_sports() {
        driver.get("https://www.yahoo.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'sports') and contains(normalize-space(),'Sports')]"));
        setId(orig, "yhooSports");
        mutate("yhooSports", "yhooSportsRefactor");
        assertFails("YHOO-BL-005", "By.id: yhooSports", "id=yhooSportsRefactor", By.id("yhooSports"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "yahoo", orig, mutated, status, msg});
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
