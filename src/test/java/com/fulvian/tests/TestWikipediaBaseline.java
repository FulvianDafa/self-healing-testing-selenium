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
@DisplayName("WIKIPEDIA - Baseline Test")
public class TestWikipediaBaseline {

    private static final Path LOG_FILE = Paths.get("results", "wikipedia_baseline_log.csv");
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

    @Test @Order(1)
    @DisplayName("WIKI-BL-001 Wikipedia: input pencarian gagal saat id berubah")
    void wikiBl001_searchInput() {
        driver.get("https://en.wikipedia.org");
        WebElement orig = waitVisible(By.name("search"));
        setId(orig, "wikiSearchInput");
        mutate("wikiSearchInput", "wikiSearchInputRefactor");
        assertFails("WIKI-BL-001", "By.id: wikiSearchInput", "id=wikiSearchInputRefactor", By.id("wikiSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("WIKI-BL-002 Wikipedia: tombol pencarian gagal saat id berubah")
    void wikiBl002_searchButton() {
        driver.get("https://en.wikipedia.org");
        WebElement orig = waitVisible(By.xpath("//button[normalize-space()='Search']"));
        setId(orig, "wikiSearchButton");
        mutate("wikiSearchButton", "wikiSearchButtonRefactor");
        assertFails("WIKI-BL-002", "By.id: wikiSearchButton", "id=wikiSearchButtonRefactor", By.id("wikiSearchButton"));
    }

    @Test @Order(3)
    @DisplayName("WIKI-BL-003 Wikipedia: link Random Article gagal saat id berubah")
    void wikiBl003_randomArticle() {
        driver.get("https://en.wikipedia.org");
        WebElement orig = waitVisible(By.xpath("//span[normalize-space()='Random article']/parent::a | //a[normalize-space()='Random article']"));
        setId(orig, "wikiRandomArticle");
        mutate("wikiRandomArticle", "wikiRandomArticleRefactor");
        assertFails("WIKI-BL-003", "By.id: wikiRandomArticle", "id=wikiRandomArticleRefactor", By.id("wikiRandomArticle"));
    }

    @Test @Order(4)
    @DisplayName("WIKI-BL-004 Wikipedia: link About Wikipedia gagal saat id berubah")
    void wikiBl004_about() {
        driver.get("https://en.wikipedia.org");
        WebElement orig = waitVisible(By.xpath("//span[normalize-space()='About Wikipedia']/parent::a | //a[normalize-space()='About Wikipedia']"));
        setId(orig, "wikiAbout");
        mutate("wikiAbout", "wikiAboutRefactor");
        assertFails("WIKI-BL-004", "By.id: wikiAbout", "id=wikiAboutRefactor", By.id("wikiAbout"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "wikipedia", orig, mutated, status, msg});
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
