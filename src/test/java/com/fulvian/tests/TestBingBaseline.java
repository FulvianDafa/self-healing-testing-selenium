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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("BING - Baseline Test")
public class TestBingBaseline {

    private static final Path LOG_FILE = Paths.get("results", "bing_baseline_log.csv");
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
    }

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--start-maximized");
        driver = new ChromeDriver(opts);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        js = (JavascriptExecutor) driver;
    }

    @AfterEach
    void tearDown() { if (driver != null) driver.quit(); }

    private void handleCookiePopup() {
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
            List<WebElement> acceptButtons = driver.findElements(By.xpath(
                "//button[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept') or @id='bnp_btn_accept']"
            ));
            for (WebElement btn : acceptButtons) {
                if (btn.isDisplayed()) {
                    btn.click();
                    break;
                }
            }
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("BING-BL-001 Bing: input pencarian gagal saat id berubah")
    void bingBl001_searchInput() {
        driver.get("https://www.bing.com");
        handleCookiePopup();
        WebElement orig = waitVisible(By.name("q"));
        setId(orig, "bingSearchInput");
        mutate("bingSearchInput", "bingSearchInputRefactor");
        assertFails("BING-BL-001", "By.id: bingSearchInput", "id=bingSearchInputRefactor", By.id("bingSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("BING-BL-002 Bing: tombol pencarian gagal saat id berubah")
    void bingBl002_searchButton() {
        driver.get("https://www.bing.com");
        handleCookiePopup();
        WebElement orig = waitVisible(By.xpath("//label[@id='search_icon' or contains(@class, 'search')]"));
        setId(orig, "bingSearchButton");
        mutate("bingSearchButton", "bingSearchButtonRefactor");
        assertFails("BING-BL-002", "By.id: bingSearchButton", "id=bingSearchButtonRefactor", By.id("bingSearchButton"));
    }

    @Test @Order(3)
    @DisplayName("BING-BL-003 Bing: link Images gagal saat id berubah")
    void bingBl003_imagesLink() {
        driver.get("https://www.bing.com");
        handleCookiePopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(text(), 'Images') or contains(text(), 'Gambar')]"));
        setId(orig, "bingImagesLink");
        mutate("bingImagesLink", "bingImagesLinkRefactor");
        assertFails("BING-BL-003", "By.id: bingImagesLink", "id=bingImagesLinkRefactor", By.id("bingImagesLink"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "bing", orig, mutated, status, msg});
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
