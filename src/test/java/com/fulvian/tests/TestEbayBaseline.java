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
 * Baseline test untuk SUT eBay (https://www.ebay.com).
 * Jalankan: mvn clean test -Dtest=TestEbayBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("EBAY - Baseline Test")
public class TestEbayBaseline {

    private static final Path LOG_FILE = Paths.get("results", "ebay_baseline_log.csv");
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
            driver.findElements(By.xpath("//button[normalize-space()='Accept all cookies' or normalize-space()='Accept']"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("EBY-BL-001 eBay: input pencarian gagal saat id berubah")
    void ebyBl001_searchInput() {
        driver.get("https://www.ebay.com");
        dismissPopup();
        WebElement orig = waitVisible(By.id("gh-ac"));
        setId(orig, "ebySearchInput");
        mutate("ebySearchInput", "ebySearchInputRefactor");
        assertFails("EBY-BL-001", "By.id: ebySearchInput", "id=ebySearchInputRefactor", By.id("ebySearchInput"));
    }

    @Test @Order(2)
    @DisplayName("EBY-BL-002 eBay: tombol pencarian gagal saat id berubah")
    void ebyBl002_searchButton() {
        driver.get("https://www.ebay.com");
        dismissPopup();
        WebElement orig = waitVisible(By.id("gh-btn"));
        setId(orig, "ebySearchButton");
        mutate("ebySearchButton", "ebySearchButtonRefactor");
        assertFails("EBY-BL-002", "By.id: ebySearchButton", "id=ebySearchButtonRefactor", By.id("ebySearchButton"));
    }

    @Test @Order(3)
    @DisplayName("EBY-BL-003 eBay: link My eBay gagal saat id berubah")
    void ebyBl003_myEbay() {
        driver.get("https://www.ebay.com");
        dismissPopup();
        WebElement orig = waitVisible(By.id("gh-eb-My"));
        setId(orig, "ebyMyEbay");
        mutate("ebyMyEbay", "ebyMyEbayRefactor");
        assertFails("EBY-BL-003", "By.id: ebyMyEbay", "id=ebyMyEbayRefactor", By.id("ebyMyEbay"));
    }

    @Test @Order(4)
    @DisplayName("EBY-BL-004 eBay: link Cart gagal saat id berubah")
    void ebyBl004_cart() {
        driver.get("https://www.ebay.com");
        dismissPopup();
        WebElement orig = waitVisible(By.id("gh-cart-n"));
        setId(orig, "ebyCart");
        mutate("ebyCart", "ebyCartRefactor");
        assertFails("EBY-BL-004", "By.id: ebyCart", "id=ebyCartRefactor", By.id("ebyCart"));
    }

    @Test @Order(5)
    @DisplayName("EBY-BL-005 eBay: logo eBay gagal saat id berubah")
    void ebyBl005_logo() {
        driver.get("https://www.ebay.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[@class='gh-logo' or @aria-label='eBay Home']"));
        setId(orig, "ebyLogo");
        mutate("ebyLogo", "ebyLogoRefactor");
        assertFails("EBY-BL-005", "By.id: ebyLogo", "id=ebyLogoRefactor", By.id("ebyLogo"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "ebay", orig, mutated, status, msg});
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
