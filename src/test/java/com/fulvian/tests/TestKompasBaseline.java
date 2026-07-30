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
 * Baseline test untuk SUT Kompas.com (https://www.kompas.com).
 * Jalankan: mvn clean test -Dtest=TestKompasBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("KOMPAS - Baseline Test")
public class TestKompasBaseline {

    private static final Path LOG_FILE = Paths.get("results", "kompas_baseline_log.csv");
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
            List<WebElement> closeBtns = driver.findElements(
                By.xpath("//button[contains(@class,'close') or contains(@aria-label,'close') or contains(@aria-label,'Close') or contains(@id,'close')]"));
            closeBtns.stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("KMP-BL-001 Kompas: input pencarian gagal saat id berubah")
    void kmpBl001_searchInput() {
        driver.get("https://www.kompas.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//input[@placeholder='Cari' or @id='cSearch' or @name='q']"));
        setId(orig, "kmpSearchInput");
        mutate("kmpSearchInput", "kmpSearchInputRefactor");
        assertFails("KMP-BL-001", "By.id: kmpSearchInput", "id=kmpSearchInputRefactor", By.id("kmpSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("KMP-BL-002 Kompas: logo Kompas gagal saat id berubah")
    void kmpBl002_logo() {
        driver.get("https://www.kompas.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'kompas.com') and (./img[contains(@alt,'Kompas')] or contains(@class,'logo'))]"));
        setId(orig, "kmpLogo");
        mutate("kmpLogo", "kmpLogoRefactor");
        assertFails("KMP-BL-002", "By.id: kmpLogo", "id=kmpLogoRefactor", By.id("kmpLogo"));
    }

    @Test @Order(3)
    @DisplayName("KMP-BL-003 Kompas: link Nasional gagal saat id berubah")
    void kmpBl003_nasional() {
        driver.get("https://www.kompas.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Nasional' and contains(@href,'nasional')]"));
        setId(orig, "kmpNasional");
        mutate("kmpNasional", "kmpNasionalRefactor");
        assertFails("KMP-BL-003", "By.id: kmpNasional", "id=kmpNasionalRefactor", By.id("kmpNasional"));
    }

    @Test @Order(4)
    @DisplayName("KMP-BL-004 Kompas: link Regional gagal saat id berubah")
    void kmpBl004_regional() {
        driver.get("https://www.kompas.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Regional' and contains(@href,'regional')]"));
        setId(orig, "kmpRegional");
        mutate("kmpRegional", "kmpRegionalRefactor");
        assertFails("KMP-BL-004", "By.id: kmpRegional", "id=kmpRegionalRefactor", By.id("kmpRegional"));
    }

    @Test @Order(5)
    @DisplayName("KMP-BL-005 Kompas: link Olahraga gagal saat id berubah")
    void kmpBl005_olahraga() {
        driver.get("https://www.kompas.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Olahraga' or normalize-space()='Sports']"));
        setId(orig, "kmpOlahraga");
        mutate("kmpOlahraga", "kmpOlahragaRefactor");
        assertFails("KMP-BL-005", "By.id: kmpOlahraga", "id=kmpOlahragaRefactor", By.id("kmpOlahraga"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "kompas", orig, mutated, status, msg});
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
