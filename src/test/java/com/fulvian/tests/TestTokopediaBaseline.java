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
 * Baseline test untuk SUT Tokopedia (https://www.tokopedia.com).
 * Jalankan: mvn clean test -Dtest=TestTokopediaBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("TOKOPEDIA - Baseline Test")
public class TestTokopediaBaseline {

    private static final Path LOG_FILE = Paths.get("results", "tokopedia_baseline_log.csv");
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
        opts.addArguments("--disable-blink-features=AutomationControlled");
        opts.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        opts.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        driver = new ChromeDriver(opts);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        wait = new WebDriverWait(driver, Duration.ofSeconds(25));
        js = (JavascriptExecutor) driver;
    }

    @AfterEach
    void tearDown() { if (driver != null) driver.quit(); }

    private void dismissPopup() {
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
            driver.findElements(By.xpath("//button[contains(@aria-label,'Close') or contains(@aria-label,'Tutup') or @data-testid='btnCloseModal']"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("TKP-BL-001 Tokopedia: input pencarian gagal saat id berubah")
    void tkpBl001_searchInput() {
        driver.get("https://www.tokopedia.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//input[@data-testid='desktop-header-search-input' or @aria-label='Cari di Tokopedia' or @placeholder='Cari di Tokopedia']"));
        setId(orig, "tkpSearchInput");
        mutate("tkpSearchInput", "tkpSearchInputRefactor");
        assertFails("TKP-BL-001", "By.id: tkpSearchInput", "id=tkpSearchInputRefactor", By.id("tkpSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("TKP-BL-002 Tokopedia: tombol pencarian gagal saat id berubah")
    void tkpBl002_searchButton() {
        driver.get("https://www.tokopedia.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//button[@data-testid='desktop-header-search-btn' or (@type='submit' and contains(@class,'search'))]"));
        setId(orig, "tkpSearchButton");
        mutate("tkpSearchButton", "tkpSearchButtonRefactor");
        assertFails("TKP-BL-002", "By.id: tkpSearchButton", "id=tkpSearchButtonRefactor", By.id("tkpSearchButton"));
    }

    @Test @Order(3)
    @DisplayName("TKP-BL-003 Tokopedia: link keranjang gagal saat id berubah")
    void tkpBl003_cart() {
        driver.get("https://www.tokopedia.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[@data-testid='icon-cart' or @href='/cart' or contains(@aria-label,'cart') or contains(@aria-label,'keranjang')]"));
        setId(orig, "tkpCart");
        mutate("tkpCart", "tkpCartRefactor");
        assertFails("TKP-BL-003", "By.id: tkpCart", "id=tkpCartRefactor", By.id("tkpCart"));
    }

    @Test @Order(4)
    @DisplayName("TKP-BL-004 Tokopedia: logo Tokopedia gagal saat id berubah")
    void tkpBl004_logo() {
        driver.get("https://www.tokopedia.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[@data-testid='header-main-logo' or (./img[contains(@alt,'Tokopedia')])]"));
        setId(orig, "tkpLogo");
        mutate("tkpLogo", "tkpLogoRefactor");
        assertFails("TKP-BL-004", "By.id: tkpLogo", "id=tkpLogoRefactor", By.id("tkpLogo"));
    }

    @Test @Order(5)
    @DisplayName("TKP-BL-005 Tokopedia: link Flash Sale gagal saat id berubah")
    void tkpBl005_flashSale() {
        driver.get("https://www.tokopedia.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'flash-sale') or contains(normalize-space(),'Flash Sale')]"));
        setId(orig, "tkpFlashSale");
        mutate("tkpFlashSale", "tkpFlashSaleRefactor");
        assertFails("TKP-BL-005", "By.id: tkpFlashSale", "id=tkpFlashSaleRefactor", By.id("tkpFlashSale"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "tokopedia", orig, mutated, status, msg});
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
