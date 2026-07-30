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
 * Baseline test untuk SUT Bhinneka (https://www.bhinneka.com).
 * Jalankan: mvn clean test -Dtest=TestBhinnekaBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("BHINNEKA - Baseline Test")
public class TestBhinnekaBaseline {

    private static final Path LOG_FILE = Paths.get("results", "bhinneka_baseline_log.csv");
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

    @Test @Order(1)
    @DisplayName("BHK-BL-001 Bhinneka: input pencarian gagal saat id berubah")
    void bhkBl001_searchInput() {
        driver.get("https://www.bhinneka.com");
        WebElement orig = waitVisible(By.xpath("//input[contains(@class,'oe_search_box') or contains(@placeholder,'Cari produk asli')]"));
        setId(orig, "bhkSearchInput");
        mutate("bhkSearchInput", "bhkSearchInputRefactor");
        assertFails("BHK-BL-001", "By.id: bhkSearchInput", "id=bhkSearchInputRefactor", By.id("bhkSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("BHK-BL-002 Bhinneka: logo Bhinneka gagal saat id berubah")
    void bhkBl002_logo() {
        driver.get("https://www.bhinneka.com");
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'bhinneka.com') and .//img[contains(@alt,'Bhinneka')]]"));
        setId(orig, "bhkLogo");
        mutate("bhkLogo", "bhkLogoRefactor");
        assertFails("BHK-BL-002", "By.id: bhkLogo", "id=bhkLogoRefactor", By.id("bhkLogo"));
    }

    @Test @Order(3)
    @DisplayName("BHK-BL-003 Bhinneka: link Cart/Keranjang gagal saat id berubah")
    void bhkBl003_cart() {
        driver.get("https://www.bhinneka.com");
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'/cart') or contains(@class,'my_cart_quantity')]"));
        setId(orig, "bhkCart");
        mutate("bhkCart", "bhkCartRefactor");
        assertFails("BHK-BL-003", "By.id: bhkCart", "id=bhkCartRefactor", By.id("bhkCart"));
    }

    @Test @Order(4)
    @DisplayName("BHK-BL-004 Bhinneka: link Kategori gagal saat id berubah")
    void bhkBl004_kategori() {
        driver.get("https://www.bhinneka.com");
        WebElement orig = waitVisible(By.xpath("//span[normalize-space()='Kategori' or contains(@class,'Kategori')]"));
        setId(orig, "bhkKategori");
        mutate("bhkKategori", "bhkKategoriRefactor");
        assertFails("BHK-BL-004", "By.id: bhkKategori", "id=bhkKategoriRefactor", By.id("bhkKategori"));
    }

    @Test @Order(5)
    @DisplayName("BHK-BL-005 Bhinneka: tombol Login gagal saat id berubah")
    void bhkBl005_login() {
        driver.get("https://www.bhinneka.com");
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Login' or contains(@href,'/web/login')]"));
        setId(orig, "bhkLogin");
        mutate("bhkLogin", "bhkLoginRefactor");
        assertFails("BHK-BL-005", "By.id: bhkLogin", "id=bhkLoginRefactor", By.id("bhkLogin"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "bhinneka", orig, mutated, status, msg});
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
