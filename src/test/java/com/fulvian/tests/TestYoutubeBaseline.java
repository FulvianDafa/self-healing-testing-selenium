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
 * Baseline test untuk SUT YouTube (https://www.youtube.com).
 * Jalankan: mvn clean test -Dtest=TestYoutubeBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("YOUTUBE - Baseline Test")
public class TestYoutubeBaseline {

    private static final Path LOG_FILE = Paths.get("results", "youtube_baseline_log.csv");
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

    private void dismissCookiePopup() {
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
            List<WebElement> btns = driver.findElements(By.xpath(
                "//button[contains(.,'Accept') or contains(.,'Agree') or contains(.,'I agree')]"));
            for (WebElement btn : btns) {
                if (btn.isDisplayed()) { btn.click(); break; }
            }
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("YT-BL-001 YouTube: input pencarian gagal saat id berubah")
    void ytBl001_searchInput() {
        driver.get("https://www.youtube.com");
        dismissCookiePopup();
        WebElement orig = waitVisible(By.name("search_query"));
        setId(orig, "ytSearchInput");
        mutate("ytSearchInput", "ytSearchInputRefactor");
        assertFails("YT-BL-001", "By.id: ytSearchInput", "id=ytSearchInputRefactor", By.id("ytSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("YT-BL-002 YouTube: tombol pencarian gagal saat id berubah")
    void ytBl002_searchButton() {
        driver.get("https://www.youtube.com");
        dismissCookiePopup();
        WebElement orig = waitVisible(By.id("search-icon-legacy"));
        setId(orig, "ytSearchButton");
        mutate("ytSearchButton", "ytSearchButtonRefactor");
        assertFails("YT-BL-002", "By.id: ytSearchButton", "id=ytSearchButtonRefactor", By.id("ytSearchButton"));
    }

    @Test @Order(3)
    @DisplayName("YT-BL-003 YouTube: logo YouTube gagal saat id berubah")
    void ytBl003_logo() {
        driver.get("https://www.youtube.com");
        dismissCookiePopup();
        WebElement orig = waitVisible(By.id("logo-icon"));
        setId(orig, "ytLogo");
        mutate("ytLogo", "ytLogoRefactor");
        assertFails("YT-BL-003", "By.id: ytLogo", "id=ytLogoRefactor", By.id("ytLogo"));
    }

    @Test @Order(4)
    @DisplayName("YT-BL-004 YouTube: link Trending gagal saat id berubah")
    void ytBl004_trending() {
        driver.get("https://www.youtube.com");
        dismissCookiePopup();
        WebElement orig = waitVisible(By.xpath("//a[@title='Trending' or contains(@href,'/feed/trending')]"));
        setId(orig, "ytTrending");
        mutate("ytTrending", "ytTrendingRefactor");
        assertFails("YT-BL-004", "By.id: ytTrending", "id=ytTrendingRefactor", By.id("ytTrending"));
    }

    @Test @Order(5)
    @DisplayName("YT-BL-005 YouTube: link Home gagal saat id berubah")
    void ytBl005_home() {
        driver.get("https://www.youtube.com");
        dismissCookiePopup();
        WebElement orig = waitVisible(By.xpath("//a[@title='Home' or @href='/']"));
        setId(orig, "ytHome");
        mutate("ytHome", "ytHomeRefactor");
        assertFails("YT-BL-005", "By.id: ytHome", "id=ytHomeRefactor", By.id("ytHome"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "youtube", orig, mutated, status, msg});
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
