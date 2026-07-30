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
 * Baseline test untuk SUT Blibli (https://www.blibli.com).
 * Jalankan: mvn clean test -Dtest=TestBlibliBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("BLIBLI - Baseline Test")
public class TestBlibliBaseline {

    private static final Path LOG_FILE = Paths.get("results", "blibli_baseline_log.csv");
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
            driver.findElements(By.xpath("//button[contains(@class,'close') or @aria-label='Tutup' or @aria-label='Close' or contains(text(),'Nanti saja')]"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("BLB-BL-001 Blibli: input pencarian gagal saat id berubah")
    void blbBl001_searchInput() {
        driver.get("https://www.blibli.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//input[contains(@placeholder,'Cari brand')]"));
        setId(orig, "blbSearchInput");
        mutate("blbSearchInput", "blbSearchInputRefactor");
        assertFails("BLB-BL-001", "By.id: blbSearchInput", "id=blbSearchInputRefactor", By.id("blbSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("BLB-BL-002 Blibli: tombol pencarian gagal saat id berubah")
    void blbBl002_searchButton() {
        driver.get("https://www.blibli.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//button[@title='search' or contains(@class,'search')]"));
        setId(orig, "blbSearchButton");
        mutate("blbSearchButton", "blbSearchButtonRefactor");
        assertFails("BLB-BL-002", "By.id: blbSearchButton", "id=blbSearchButtonRefactor", By.id("blbSearchButton"));
    }

    @Test @Order(3)
    @DisplayName("BLB-BL-003 Blibli: logo Blibli gagal saat id berubah")
    void blbBl003_logo() {
        driver.get("https://www.blibli.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[.//img[contains(@alt,'Blibli')]]"));
        setId(orig, "blbLogo");
        mutate("blbLogo", "blbLogoRefactor");
        assertFails("BLB-BL-003", "By.id: blbLogo", "id=blbLogoRefactor", By.id("blbLogo"));
    }

    @Test @Order(4)
    @DisplayName("BLB-BL-004 Blibli: ikon Keranjang gagal saat id berubah")
    void blbBl004_cart() {
        driver.get("https://www.blibli.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'/cart')]"));
        setId(orig, "blbCart");
        mutate("blbCart", "blbCartRefactor");
        assertFails("BLB-BL-004", "By.id: blbCart", "id=blbCartRefactor", By.id("blbCart"));
    }

    @Test @Order(5)
    @DisplayName("BLB-BL-005 Blibli: tombol Masuk gagal saat id berubah")
    void blbBl005_login() {
        driver.get("https://www.blibli.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//button[normalize-space()='Masuk' or contains(@class,'login')]"));
        setId(orig, "blbLogin");
        mutate("blbLogin", "blbLoginRefactor");
        assertFails("BLB-BL-005", "By.id: blbLogin", "id=blbLoginRefactor", By.id("blbLogin"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "blibli", orig, mutated, status, msg});
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
