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
 * Baseline test untuk SUT CNN Indonesia (https://www.cnnindonesia.com).
 * Jalankan: mvn clean test -Dtest=TestCnnIndoBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("CNN INDONESIA - Baseline Test")
public class TestCnnIndoBaseline {

    private static final Path LOG_FILE = Paths.get("results", "cnnindo_baseline_log.csv");
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
            driver.findElements(By.xpath("//button[contains(@class,'close') or contains(@aria-label,'Close')]"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("CNN-BL-001 CNN Indo: icon search gagal saat id berubah")
    void cnnBl001_searchIcon() {
        driver.get("https://www.cnnindonesia.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@class,'search-btn') or contains(@class,'btn-search')]"));
        setId(orig, "cnnSearchIcon");
        mutate("cnnSearchIcon", "cnnSearchIconRefactor");
        assertFails("CNN-BL-001", "By.id: cnnSearchIcon", "id=cnnSearchIconRefactor", By.id("cnnSearchIcon"));
    }

    @Test @Order(2)
    @DisplayName("CNN-BL-002 CNN Indo: input pencarian gagal saat id berubah")
    void cnnBl002_searchInput() {
        driver.get("https://www.cnnindonesia.com");
        dismissPopup();
        try { driver.findElement(By.xpath("//a[contains(@class,'search-btn')]")).click(); } catch(Exception e){}
        WebElement orig = waitVisible(By.id("searchinput"));
        setId(orig, "cnnSearchInput");
        mutate("cnnSearchInput", "cnnSearchInputRefactor");
        assertFails("CNN-BL-002", "By.id: cnnSearchInput", "id=cnnSearchInputRefactor", By.id("cnnSearchInput"));
    }

    @Test @Order(3)
    @DisplayName("CNN-BL-003 CNN Indo: logo utama gagal saat id berubah")
    void cnnBl003_logo() {
        driver.get("https://www.cnnindonesia.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'cnnindonesia.com') and .//img[contains(@alt,'CNN')]]"));
        setId(orig, "cnnLogo");
        mutate("cnnLogo", "cnnLogoRefactor");
        assertFails("CNN-BL-003", "By.id: cnnLogo", "id=cnnLogoRefactor", By.id("cnnLogo"));
    }

    @Test @Order(4)
    @DisplayName("CNN-BL-004 CNN Indo: link Nasional gagal saat id berubah")
    void cnnBl004_nasional() {
        driver.get("https://www.cnnindonesia.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Nasional' and contains(@href,'nasional')]"));
        setId(orig, "cnnNasional");
        mutate("cnnNasional", "cnnNasionalRefactor");
        assertFails("CNN-BL-004", "By.id: cnnNasional", "id=cnnNasionalRefactor", By.id("cnnNasional"));
    }

    @Test @Order(5)
    @DisplayName("CNN-BL-005 CNN Indo: link Ekonomi gagal saat id berubah")
    void cnnBl005_ekonomi() {
        driver.get("https://www.cnnindonesia.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Ekonomi' and contains(@href,'ekonomi')]"));
        setId(orig, "cnnEkonomi");
        mutate("cnnEkonomi", "cnnEkonomiRefactor");
        assertFails("CNN-BL-005", "By.id: cnnEkonomi", "id=cnnEkonomiRefactor", By.id("cnnEkonomi"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "cnnindo", orig, mutated, status, msg});
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
