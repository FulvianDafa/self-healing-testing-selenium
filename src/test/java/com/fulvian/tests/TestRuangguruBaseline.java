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
 * Baseline test untuk SUT Ruangguru (https://www.ruangguru.com).
 * Jalankan: mvn clean test -Dtest=TestRuangguruBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RUANGGURU - Baseline Test")
public class TestRuangguruBaseline {

    private static final Path LOG_FILE = Paths.get("results", "ruangguru_baseline_log.csv");
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
            driver.findElements(By.xpath("//button[contains(@class,'close') or @aria-label='Close' or @aria-label='Tutup' or contains(text(),'Nanti saja')] | //div[contains(@class,'modal')]//span[text()='x']"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("RGG-BL-001 Ruangguru: input pencarian blog gagal saat id berubah")
    void rggBl001_searchInput() {
        driver.get("https://www.ruangguru.com/blog");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//input[@aria-label='Cari artikel di sini!' or @placeholder='Cari artikel di sini!']"));
        setId(orig, "rggSearchInput");
        mutate("rggSearchInput", "rggSearchInputRefactor");
        assertFails("RGG-BL-001", "By.id: rggSearchInput", "id=rggSearchInputRefactor", By.id("rggSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("RGG-BL-002 Ruangguru: tombol pencarian blog gagal saat id berubah")
    void rggBl002_searchButton() {
        driver.get("https://www.ruangguru.com/blog");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//button[@type='submit' and contains(@class,'search')]"));
        setId(orig, "rggSearchButton");
        mutate("rggSearchButton", "rggSearchButtonRefactor");
        assertFails("RGG-BL-002", "By.id: rggSearchButton", "id=rggSearchButtonRefactor", By.id("rggSearchButton"));
    }

    @Test @Order(3)
    @DisplayName("RGG-BL-003 Ruangguru: logo Ruangguru gagal saat id berubah")
    void rggBl003_logo() {
        driver.get("https://www.ruangguru.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[.//img[contains(@alt,'Ruangguru')]]"));
        setId(orig, "rggLogo");
        mutate("rggLogo", "rggLogoRefactor");
        assertFails("RGG-BL-003", "By.id: rggLogo", "id=rggLogoRefactor", By.id("rggLogo"));
    }

    @Test @Order(4)
    @DisplayName("RGG-BL-004 Ruangguru: link SD/SMP/SMA gagal saat id berubah")
    void rggBl004_sdSmpSma() {
        driver.get("https://www.ruangguru.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(normalize-space(),'SD') and contains(normalize-space(),'SMP')]"));
        setId(orig, "rggSdSmpSma");
        mutate("rggSdSmpSma", "rggSdSmpSmaRefactor");
        assertFails("RGG-BL-004", "By.id: rggSdSmpSma", "id=rggSdSmpSmaRefactor", By.id("rggSdSmpSma"));
    }

    @Test @Order(5)
    @DisplayName("RGG-BL-005 Ruangguru: tombol Masuk/Daftar gagal saat id berubah")
    void rggBl005_login() {
        driver.get("https://www.ruangguru.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Masuk / Daftar' or normalize-space()='Masuk']"));
        setId(orig, "rggLogin");
        mutate("rggLogin", "rggLoginRefactor");
        assertFails("RGG-BL-005", "By.id: rggLogin", "id=rggLoginRefactor", By.id("rggLogin"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "ruangguru", orig, mutated, status, msg});
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
