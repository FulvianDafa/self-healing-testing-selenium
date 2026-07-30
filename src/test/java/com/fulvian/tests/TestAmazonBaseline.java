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
 * Baseline test untuk SUT Amazon (https://www.amazon.com).
 * Locator dirusak via JavaScript, membuktikan Selenium biasa gagal.
 * Jalankan: mvn clean test -Dtest=TestAmazonBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("AMAZON - Baseline Test")
public class TestAmazonBaseline {

    private static final Path LOG_FILE = Paths.get("results", "amazon_baseline_log.csv");
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
        opts.addArguments("--start-maximized", "--disable-blink-features=AutomationControlled");
        opts.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
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
            driver.findElements(By.xpath("//*[@id='nav-flyout-ya-signin' or contains(@class,'a-popover-closebutton')]"))
                    .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("AMZ-BL-001 Amazon: input pencarian gagal saat id berubah")
    void amzBl001_searchInput() {
        driver.get("https://www.amazon.com");
        dismissPopup();
        WebElement orig = waitVisible(By.id("twotabsearchtextbox"));
        setId(orig, "amzSearchInput");
        mutate("amzSearchInput", "amzSearchInputRefactor");
        assertFails("AMZ-BL-001", "By.id: amzSearchInput", "id=amzSearchInputRefactor", By.id("amzSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("AMZ-BL-002 Amazon: tombol pencarian gagal saat id berubah")
    void amzBl002_searchButton() {
        driver.get("https://www.amazon.com");
        dismissPopup();
        WebElement orig = waitVisible(By.id("nav-search-submit-button"));
        setId(orig, "amzSearchButton");
        mutate("amzSearchButton", "amzSearchButtonRefactor");
        assertFails("AMZ-BL-002", "By.id: amzSearchButton", "id=amzSearchButtonRefactor", By.id("amzSearchButton"));
    }

    @Test @Order(3)
    @DisplayName("AMZ-BL-003 Amazon: link nav All gagal saat id berubah")
    void amzBl003_navAll() {
        driver.get("https://www.amazon.com");
        dismissPopup();
        WebElement orig = waitVisible(By.id("nav-hamburger-menu"));
        setId(orig, "amzNavAll");
        mutate("amzNavAll", "amzNavAllRefactor");
        assertFails("AMZ-BL-003", "By.id: amzNavAll", "id=amzNavAllRefactor", By.id("amzNavAll"));
    }

    @Test @Order(4)
    @DisplayName("AMZ-BL-004 Amazon: link Returns & Orders gagal saat id berubah")
    void amzBl004_ordersLink() {
        driver.get("https://www.amazon.com");
        dismissPopup();
        WebElement orig = waitVisible(By.id("nav-orders"));
        setId(orig, "amzOrdersLink");
        mutate("amzOrdersLink", "amzOrdersLinkRefactor");
        assertFails("AMZ-BL-004", "By.id: amzOrdersLink", "id=amzOrdersLinkRefactor", By.id("amzOrdersLink"));
    }

    @Test @Order(5)
    @DisplayName("AMZ-BL-005 Amazon: link Cart gagal saat id berubah")
    void amzBl005_cartLink() {
        driver.get("https://www.amazon.com");
        dismissPopup();
        WebElement orig = waitVisible(By.id("nav-cart"));
        setId(orig, "amzCartLink");
        mutate("amzCartLink", "amzCartLinkRefactor");
        assertFails("AMZ-BL-005", "By.id: amzCartLink", "id=amzCartLinkRefactor", By.id("amzCartLink"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "amazon", orig, mutated, status, msg});
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
