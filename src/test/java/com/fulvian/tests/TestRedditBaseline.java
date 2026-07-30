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
 * Baseline test untuk SUT Reddit (https://www.reddit.com).
 * Jalankan: mvn clean test -Dtest=TestRedditBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("REDDIT - Baseline Test")
public class TestRedditBaseline {

    private static final Path LOG_FILE = Paths.get("results", "reddit_baseline_log.csv");
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
            driver.findElements(By.xpath(
                "//button[contains(@aria-label,'Close') or normalize-space()='Close' or normalize-space()='Accept']"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("RDT-BL-001 Reddit: input pencarian gagal saat id berubah")
    void rdtBl001_searchInput() {
        driver.get("https://www.reddit.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//input[@name='q' or @placeholder='Search Reddit']"));
        setId(orig, "rdtSearchInput");
        mutate("rdtSearchInput", "rdtSearchInputRefactor");
        assertFails("RDT-BL-001", "By.id: rdtSearchInput", "id=rdtSearchInputRefactor", By.id("rdtSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("RDT-BL-002 Reddit: link Popular gagal saat id berubah")
    void rdtBl002_popular() {
        driver.get("https://www.reddit.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'/r/popular') or normalize-space()='Popular']"));
        setId(orig, "rdtPopular");
        mutate("rdtPopular", "rdtPopularRefactor");
        assertFails("RDT-BL-002", "By.id: rdtPopular", "id=rdtPopularRefactor", By.id("rdtPopular"));
    }

    @Test @Order(3)
    @DisplayName("RDT-BL-003 Reddit: link All gagal saat id berubah")
    void rdtBl003_all() {
        driver.get("https://www.reddit.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'/r/all') or normalize-space()='All']"));
        setId(orig, "rdtAll");
        mutate("rdtAll", "rdtAllRefactor");
        assertFails("RDT-BL-003", "By.id: rdtAll", "id=rdtAllRefactor", By.id("rdtAll"));
    }

    @Test @Order(4)
    @DisplayName("RDT-BL-004 Reddit: logo Reddit gagal saat id berubah")
    void rdtBl004_logo() {
        driver.get("https://www.reddit.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[@aria-label='Home' or @href='https://www.reddit.com/'][1]"));
        setId(orig, "rdtLogo");
        mutate("rdtLogo", "rdtLogoRefactor");
        assertFails("RDT-BL-004", "By.id: rdtLogo", "id=rdtLogoRefactor", By.id("rdtLogo"));
    }

    @Test @Order(5)
    @DisplayName("RDT-BL-005 Reddit: button Login gagal saat id berubah")
    void rdtBl005_login() {
        driver.get("https://www.reddit.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Log In' or @href='/login/']"));
        setId(orig, "rdtLogin");
        mutate("rdtLogin", "rdtLoginRefactor");
        assertFails("RDT-BL-005", "By.id: rdtLogin", "id=rdtLoginRefactor", By.id("rdtLogin"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "reddit", orig, mutated, status, msg});
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
