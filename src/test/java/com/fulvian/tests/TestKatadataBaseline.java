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
 * Baseline test untuk SUT Katadata (https://www.katadata.co.id).
 * Jalankan: mvn clean test -Dtest=TestKatadataBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("KATADATA - Baseline Test")
public class TestKatadataBaseline {

    private static final Path LOG_FILE = Paths.get("results", "katadata_baseline_log.csv");
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
            driver.findElements(By.xpath("//button[contains(@class,'close')] | //div[contains(@class,'close')]"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("KTD-BL-001 Katadata: input pencarian gagal saat id berubah")
    void ktdBl001_searchInput() {
        driver.get("https://katadata.co.id/");
        dismissPopup();
        WebElement orig = waitVisible(By.id("inputSearch"));
        setId(orig, "ktdSearchInput");
        mutate("ktdSearchInput", "ktdSearchInputRefactor");
        assertFails("KTD-BL-001", "By.id: ktdSearchInput", "id=ktdSearchInputRefactor", By.id("ktdSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("KTD-BL-002 Katadata: logo Katadata gagal saat id berubah")
    void ktdBl002_logo() {
        driver.get("https://katadata.co.id/");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[.//img[contains(@alt,'Katadata')]]"));
        setId(orig, "ktdLogo");
        mutate("ktdLogo", "ktdLogoRefactor");
        assertFails("KTD-BL-002", "By.id: ktdLogo", "id=ktdLogoRefactor", By.id("ktdLogo"));
    }

    @Test @Order(3)
    @DisplayName("KTD-BL-003 Katadata: link Finansial gagal saat id berubah")
    void ktdBl003_finansial() {
        driver.get("https://katadata.co.id/");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Finansial' or contains(@href,'finansial')]"));
        setId(orig, "ktdFinansial");
        mutate("ktdFinansial", "ktdFinansialRefactor");
        assertFails("KTD-BL-003", "By.id: ktdFinansial", "id=ktdFinansialRefactor", By.id("ktdFinansial"));
    }

    @Test @Order(4)
    @DisplayName("KTD-BL-004 Katadata: link Berita utama gagal saat id berubah")
    void ktdBl004_berita() {
        driver.get("https://katadata.co.id/");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Berita' or contains(@href,'berita')]"));
        setId(orig, "ktdBerita");
        mutate("ktdBerita", "ktdBeritaRefactor");
        assertFails("KTD-BL-004", "By.id: ktdBerita", "id=ktdBeritaRefactor", By.id("ktdBerita"));
    }

    @Test @Order(5)
    @DisplayName("KTD-BL-005 Katadata: link Databoks gagal saat id berubah")
    void ktdBl005_databoks() {
        driver.get("https://katadata.co.id/");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(normalize-space(),'Databoks') or contains(@href,'databoks')]"));
        setId(orig, "ktdDataboks");
        mutate("ktdDataboks", "ktdDataboksRefactor");
        assertFails("KTD-BL-005", "By.id: ktdDataboks", "id=ktdDataboksRefactor", By.id("ktdDataboks"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "katadata", orig, mutated, status, msg});
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
