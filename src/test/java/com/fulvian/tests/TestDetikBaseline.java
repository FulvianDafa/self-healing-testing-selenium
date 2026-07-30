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
 * Baseline test untuk SUT Detik.com (https://www.detik.com).
 * Jalankan: mvn clean test -Dtest=TestDetikBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DETIK - Baseline Test")
public class TestDetikBaseline {

    private static final Path LOG_FILE = Paths.get("results", "detik_baseline_log.csv");
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
    @DisplayName("DTK-BL-001 Detik: input pencarian gagal saat id berubah")
    void dtkBl001_searchInput() {
        driver.get("https://www.detik.com");
        WebElement orig = waitVisible(By.id("search-text"));
        setId(orig, "dtkSearchInput");
        mutate("dtkSearchInput", "dtkSearchInputRefactor");
        assertFails("DTK-BL-001", "By.id: dtkSearchInput", "id=dtkSearchInputRefactor", By.id("dtkSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("DTK-BL-002 Detik: tombol pencarian gagal saat id berubah")
    void dtkBl002_searchButton() {
        driver.get("https://www.detik.com");
        WebElement orig = waitVisible(By.xpath("//button[@type='submit' and (contains(@class,'search') or contains(@class,'btn-search'))]"));
        setId(orig, "dtkSearchButton");
        mutate("dtkSearchButton", "dtkSearchButtonRefactor");
        assertFails("DTK-BL-002", "By.id: dtkSearchButton", "id=dtkSearchButtonRefactor", By.id("dtkSearchButton"));
    }

    @Test @Order(3)
    @DisplayName("DTK-BL-003 Detik: link News gagal saat id berubah")
    void dtkBl003_news() {
        driver.get("https://www.detik.com");
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'news.detik.com') and normalize-space()='News']"));
        setId(orig, "dtkNews");
        mutate("dtkNews", "dtkNewsRefactor");
        assertFails("DTK-BL-003", "By.id: dtkNews", "id=dtkNewsRefactor", By.id("dtkNews"));
    }

    @Test @Order(4)
    @DisplayName("DTK-BL-004 Detik: link Sport gagal saat id berubah")
    void dtkBl004_sport() {
        driver.get("https://www.detik.com");
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'sport.detik.com') or normalize-space()='Sport']"));
        setId(orig, "dtkSport");
        mutate("dtkSport", "dtkSportRefactor");
        assertFails("DTK-BL-004", "By.id: dtkSport", "id=dtkSportRefactor", By.id("dtkSport"));
    }

    @Test @Order(5)
    @DisplayName("DTK-BL-005 Detik: link Finance gagal saat id berubah")
    void dtkBl005_finance() {
        driver.get("https://www.detik.com");
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'finance.detik.com') or normalize-space()='Finance']"));
        setId(orig, "dtkFinance");
        mutate("dtkFinance", "dtkFinanceRefactor");
        assertFails("DTK-BL-005", "By.id: dtkFinance", "id=dtkFinanceRefactor", By.id("dtkFinance"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "detik", orig, mutated, status, msg});
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
