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
 * Baseline test untuk SUT Bisnis (https://www.bisnis.com).
 * Jalankan: mvn clean test -Dtest=TestBisnisBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("BISNIS.COM - Baseline Test")
public class TestBisnisBaseline {

    private static final Path LOG_FILE = Paths.get("results", "bisnis_baseline_log.csv");
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
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
            driver.findElements(By.xpath("//button[contains(@class,'close') or @aria-label='Close']"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("BSN-BL-001 Bisnis: icon search gagal saat id berubah")
    void bsnBl001_searchIcon() {
        driver.get("https://www.bisnis.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'search.bisnis') or @aria-label='search' or contains(@class,'search')]"));
        setId(orig, "bsnSearchIcon");
        mutate("bsnSearchIcon", "bsnSearchIconRefactor");
        assertFails("BSN-BL-001", "By.id: bsnSearchIcon", "id=bsnSearchIconRefactor", By.id("bsnSearchIcon"));
    }

    @Test @Order(2)
    @DisplayName("BSN-BL-002 Bisnis: logo utama gagal saat id berubah")
    void bsnBl002_logo() {
        driver.get("https://www.bisnis.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[.//img[contains(@alt,'Bisnis')]]"));
        setId(orig, "bsnLogo");
        mutate("bsnLogo", "bsnLogoRefactor");
        assertFails("BSN-BL-002", "By.id: bsnLogo", "id=bsnLogoRefactor", By.id("bsnLogo"));
    }

    @Test @Order(3)
    @DisplayName("BSN-BL-003 Bisnis: menu Market gagal saat id berubah")
    void bsnBl003_market() {
        driver.get("https://www.bisnis.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Market' or contains(@href,'market.bisnis')]"));
        setId(orig, "bsnMarket");
        mutate("bsnMarket", "bsnMarketRefactor");
        assertFails("BSN-BL-003", "By.id: bsnMarket", "id=bsnMarketRefactor", By.id("bsnMarket"));
    }

    @Test @Order(4)
    @DisplayName("BSN-BL-004 Bisnis: menu Finansial gagal saat id berubah")
    void bsnBl004_finansial() {
        driver.get("https://www.bisnis.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Finansial' or contains(@href,'finansial.bisnis')]"));
        setId(orig, "bsnFinansial");
        mutate("bsnFinansial", "bsnFinansialRefactor");
        assertFails("BSN-BL-004", "By.id: bsnFinansial", "id=bsnFinansialRefactor", By.id("bsnFinansial"));
    }

    @Test @Order(5)
    @DisplayName("BSN-BL-005 Bisnis: link Terpopuler gagal saat id berubah")
    void bsnBl005_terpopuler() {
        driver.get("https://www.bisnis.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Terpopuler' or contains(normalize-space(),'Populer')]"));
        setId(orig, "bsnTerpopuler");
        mutate("bsnTerpopuler", "bsnTerpopulerRefactor");
        assertFails("BSN-BL-005", "By.id: bsnTerpopuler", "id=bsnTerpopulerRefactor", By.id("bsnTerpopuler"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "bisnis", orig, mutated, status, msg});
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
