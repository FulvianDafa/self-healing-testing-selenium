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
 * Baseline test untuk SUT Etsy (https://www.etsy.com).
 * Jalankan: mvn clean test -Dtest=TestEtsyBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ETSY - Baseline Test")
public class TestEtsyBaseline {

    private static final Path LOG_FILE = Paths.get("results", "etsy_baseline_log.csv");
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
            driver.findElements(By.xpath("//button[contains(@data-gdpr-single-choice-accept,'') or contains(normalize-space(),'Accept')]"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("ETSY-BL-001 Etsy: input pencarian gagal saat id berubah")
    void etsyBl001_searchInput() {
        driver.get("https://www.etsy.com");
        dismissPopup();
        WebElement orig = waitVisible(By.name("search_query"));
        setId(orig, "etsySearchInput");
        mutate("etsySearchInput", "etsySearchInputRefactor");
        assertFails("ETSY-BL-001", "By.id: etsySearchInput", "id=etsySearchInputRefactor", By.id("etsySearchInput"));
    }

    @Test @Order(2)
    @DisplayName("ETSY-BL-002 Etsy: tombol pencarian gagal saat id berubah")
    void etsyBl002_searchButton() {
        driver.get("https://www.etsy.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//button[@type='submit' and contains(@aria-label,'Search')]"));
        setId(orig, "etsySearchButton");
        mutate("etsySearchButton", "etsySearchButtonRefactor");
        assertFails("ETSY-BL-002", "By.id: etsySearchButton", "id=etsySearchButtonRefactor", By.id("etsySearchButton"));
    }

    @Test @Order(3)
    @DisplayName("ETSY-BL-003 Etsy: link Sign In gagal saat id berubah")
    void etsyBl003_signIn() {
        driver.get("https://www.etsy.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'/signin') or normalize-space()='Sign in']"));
        setId(orig, "etsySignIn");
        mutate("etsySignIn", "etsySignInRefactor");
        assertFails("ETSY-BL-003", "By.id: etsySignIn", "id=etsySignInRefactor", By.id("etsySignIn"));
    }

    @Test @Order(4)
    @DisplayName("ETSY-BL-004 Etsy: link Cart gagal saat id berubah")
    void etsyBl004_cart() {
        driver.get("https://www.etsy.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'/cart') or @aria-label='Cart']"));
        setId(orig, "etsyCart");
        mutate("etsyCart", "etsyCartRefactor");
        assertFails("ETSY-BL-004", "By.id: etsyCart", "id=etsyCartRefactor", By.id("etsyCart"));
    }

    @Test @Order(5)
    @DisplayName("ETSY-BL-005 Etsy: link logo Etsy gagal saat id berubah")
    void etsyBl005_logo() {
        driver.get("https://www.etsy.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[@aria-label='Etsy' or contains(@href,'etsy.com') and .//*[name()='svg']][1]"));
        setId(orig, "etsyLogo");
        mutate("etsyLogo", "etsyLogoRefactor");
        assertFails("ETSY-BL-005", "By.id: etsyLogo", "id=etsyLogoRefactor", By.id("etsyLogo"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "etsy", orig, mutated, status, msg});
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
