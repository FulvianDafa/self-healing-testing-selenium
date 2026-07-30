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
 * Baseline test untuk SUT Rumah123 (https://www.rumah123.com).
 * Jalankan: mvn clean test -Dtest=TestRumah123Baseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RUMAH123 - Baseline Test")
public class TestRumah123Baseline {

    private static final Path LOG_FILE = Paths.get("results", "rumah123_baseline_log.csv");
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
    @DisplayName("RMH-BL-001 Rumah123: input lokasi gagal saat id berubah")
    void rmhBl001_searchInput() {
        driver.get("https://www.rumah123.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//input[contains(@placeholder,'Lokasi') or contains(@placeholder,'area')]"));
        setId(orig, "rmhSearchInput");
        mutate("rmhSearchInput", "rmhSearchInputRefactor");
        assertFails("RMH-BL-001", "By.id: rmhSearchInput", "id=rmhSearchInputRefactor", By.id("rmhSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("RMH-BL-002 Rumah123: tombol Cari gagal saat id berubah")
    void rmhBl002_searchButton() {
        driver.get("https://www.rumah123.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//button[normalize-space()='Cari']"));
        setId(orig, "rmhSearchButton");
        mutate("rmhSearchButton", "rmhSearchButtonRefactor");
        assertFails("RMH-BL-002", "By.id: rmhSearchButton", "id=rmhSearchButtonRefactor", By.id("rmhSearchButton"));
    }

    @Test @Order(3)
    @DisplayName("RMH-BL-003 Rumah123: logo utama gagal saat id berubah")
    void rmhBl003_logo() {
        driver.get("https://www.rumah123.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[.//img[contains(@alt,'Rumah123')]]"));
        setId(orig, "rmhLogo");
        mutate("rmhLogo", "rmhLogoRefactor");
        assertFails("RMH-BL-003", "By.id: rmhLogo", "id=rmhLogoRefactor", By.id("rmhLogo"));
    }

    @Test @Order(4)
    @DisplayName("RMH-BL-004 Rumah123: tombol Dijual gagal saat id berubah")
    void rmhBl004_dijualMenu() {
        driver.get("https://www.rumah123.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//button[normalize-space()='Dijual'] | //a[normalize-space()='Dijual']"));
        setId(orig, "rmhDijual");
        mutate("rmhDijual", "rmhDijualRefactor");
        assertFails("RMH-BL-004", "By.id: rmhDijual", "id=rmhDijualRefactor", By.id("rmhDijual"));
    }

    @Test @Order(5)
    @DisplayName("RMH-BL-005 Rumah123: tombol Disewa gagal saat id berubah")
    void rmhBl005_disewaMenu() {
        driver.get("https://www.rumah123.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//button[normalize-space()='Disewa'] | //a[normalize-space()='Disewa']"));
        setId(orig, "rmhDisewa");
        mutate("rmhDisewa", "rmhDisewaRefactor");
        assertFails("RMH-BL-005", "By.id: rmhDisewa", "id=rmhDisewaRefactor", By.id("rmhDisewa"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "rumah123", orig, mutated, status, msg});
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
