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
 * Baseline test untuk SUT BestBuy (https://www.bestbuy.com).
 * Jalankan: mvn clean test -Dtest=TestBestBuyBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("BESTBUY - Baseline Test")
public class TestBestBuyBaseline {

    private static final Path LOG_FILE = Paths.get("results", "bestbuy_baseline_log.csv");
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
    @DisplayName("BBY-BL-001 BestBuy: input pencarian gagal saat id berubah")
    void bbyBl001_searchInput() {
        driver.get("https://www.bestbuy.com");
        WebElement orig = waitVisible(By.id("gh-search-input"));
        setId(orig, "bbySearchInput");
        mutate("bbySearchInput", "bbySearchInputRefactor");
        assertFails("BBY-BL-001", "By.id: bbySearchInput", "id=bbySearchInputRefactor", By.id("bbySearchInput"));
    }

    @Test @Order(2)
    @DisplayName("BBY-BL-002 BestBuy: tombol pencarian gagal saat id berubah")
    void bbyBl002_searchButton() {
        driver.get("https://www.bestbuy.com");
        WebElement orig = waitVisible(By.xpath("//button[@type='submit' and @class='header-search-button']"));
        setId(orig, "bbySearchButton");
        mutate("bbySearchButton", "bbySearchButtonRefactor");
        assertFails("BBY-BL-002", "By.id: bbySearchButton", "id=bbySearchButtonRefactor", By.id("bbySearchButton"));
    }

    @Test @Order(3)
    @DisplayName("BBY-BL-003 BestBuy: link Cart gagal saat id berubah")
    void bbyBl003_cart() {
        driver.get("https://www.bestbuy.com");
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'cart') or @aria-label='Cart']"));
        setId(orig, "bbyCart");
        mutate("bbyCart", "bbyCartRefactor");
        assertFails("BBY-BL-003", "By.id: bbyCart", "id=bbyCartRefactor", By.id("bbyCart"));
    }

    @Test @Order(4)
    @DisplayName("BBY-BL-004 BestBuy: link Top Deals gagal saat id berubah")
    void bbyBl004_topDeals() {
        driver.get("https://www.bestbuy.com");
        WebElement orig = waitVisible(By.xpath("//a[contains(normalize-space(),'Top Deals') or contains(normalize-space(),'Deals')]"));
        setId(orig, "bbyTopDeals");
        mutate("bbyTopDeals", "bbyTopDealsRefactor");
        assertFails("BBY-BL-004", "By.id: bbyTopDeals", "id=bbyTopDealsRefactor", By.id("bbyTopDeals"));
    }

    @Test @Order(5)
    @DisplayName("BBY-BL-005 BestBuy: logo BestBuy gagal saat id berubah")
    void bbyBl005_logo() {
        driver.get("https://www.bestbuy.com");
        WebElement orig = waitVisible(By.xpath("//a[@class='us-logo'] | //img[@alt='Best Buy']/parent::a"));
        setId(orig, "bbyLogo");
        mutate("bbyLogo", "bbyLogoRefactor");
        assertFails("BBY-BL-005", "By.id: bbyLogo", "id=bbyLogoRefactor", By.id("bbyLogo"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "bestbuy", orig, mutated, status, msg});
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
