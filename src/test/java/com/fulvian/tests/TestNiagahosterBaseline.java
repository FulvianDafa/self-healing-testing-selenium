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
 * Baseline test untuk SUT Niagahoster (https://www.niagahoster.co.id).
 * Jalankan: mvn clean test -Dtest=TestNiagahosterBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("NIAGAHOSTER - Baseline Test")
public class TestNiagahosterBaseline {

    private static final Path LOG_FILE = Paths.get("results", "niagahoster_baseline_log.csv");
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
            driver.findElements(By.xpath("//button[contains(@class,'close') or contains(@aria-label,'Close') or contains(@class,'kodee-close')]"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("NGH-BL-001 Niagahoster: input domain gagal saat id berubah")
    void nghBl001_domainInput() {
        driver.get("https://www.niagahoster.co.id");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//input[contains(@id,'domain-input') or contains(@placeholder,'domain')]"));
        setId(orig, "nghDomainInput");
        mutate("nghDomainInput", "nghDomainInputRefactor");
        assertFails("NGH-BL-001", "By.id: nghDomainInput", "id=nghDomainInputRefactor", By.id("nghDomainInput"));
    }

    @Test @Order(2)
    @DisplayName("NGH-BL-002 Niagahoster: tombol cari domain gagal saat id berubah")
    void nghBl002_domainButton() {
        driver.get("https://www.niagahoster.co.id");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//button[contains(normalize-space(),'Cari Domain') or contains(normalize-space(),'Cari')]"));
        setId(orig, "nghDomainButton");
        mutate("nghDomainButton", "nghDomainButtonRefactor");
        assertFails("NGH-BL-002", "By.id: nghDomainButton", "id=nghDomainButtonRefactor", By.id("nghDomainButton"));
    }

    @Test @Order(3)
    @DisplayName("NGH-BL-003 Niagahoster: logo Niagahoster gagal saat id berubah")
    void nghBl003_logo() {
        driver.get("https://www.niagahoster.co.id");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[.//img[contains(@alt,'Niagahoster')]]"));
        setId(orig, "nghLogo");
        mutate("nghLogo", "nghLogoRefactor");
        assertFails("NGH-BL-003", "By.id: nghLogo", "id=nghLogoRefactor", By.id("nghLogo"));
    }

    @Test @Order(4)
    @DisplayName("NGH-BL-004 Niagahoster: link Hosting gagal saat id berubah")
    void nghBl004_hostingMenu() {
        driver.get("https://www.niagahoster.co.id");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(normalize-space(),'Hosting') and contains(@href,'hosting')]"));
        setId(orig, "nghHosting");
        mutate("nghHosting", "nghHostingRefactor");
        assertFails("NGH-BL-004", "By.id: nghHosting", "id=nghHostingRefactor", By.id("nghHosting"));
    }

    @Test @Order(5)
    @DisplayName("NGH-BL-005 Niagahoster: tombol Login gagal saat id berubah")
    void nghBl005_login() {
        driver.get("https://www.niagahoster.co.id");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Login' or contains(@href,'login')]"));
        setId(orig, "nghLogin");
        mutate("nghLogin", "nghLoginRefactor");
        assertFails("NGH-BL-005", "By.id: nghLogin", "id=nghLoginRefactor", By.id("nghLogin"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "niagahoster", orig, mutated, status, msg});
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
