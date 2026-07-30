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
 * Baseline test untuk SUT Tiket.com (https://www.tiket.com).
 * Jalankan: mvn clean test -Dtest=TestTiketBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("TIKET.COM - Baseline Test")
public class TestTiketBaseline {

    private static final Path LOG_FILE = Paths.get("results", "tiket_baseline_log.csv");
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
            driver.findElements(By.xpath("//div[contains(@class,'close') or @aria-label='Close' or @aria-label='Tutup']"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("TKT-BL-001 Tiket.com: input pencarian kota gagal saat id berubah")
    void tktBl001_searchInput() {
        driver.get("https://www.tiket.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//input[@aria-label='Search Text Input' or contains(@placeholder,'Mau ke mana')]"));
        setId(orig, "tktSearchInput");
        mutate("tktSearchInput", "tktSearchInputRefactor");
        assertFails("TKT-BL-001", "By.id: tktSearchInput", "id=tktSearchInputRefactor", By.id("tktSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("TKT-BL-002 Tiket.com: logo tiket.com gagal saat id berubah")
    void tktBl002_logo() {
        driver.get("https://www.tiket.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'tiket.com')]//img[contains(@alt,'tiket.com') or contains(@alt,'logo')]"));
        setId(orig, "tktLogo");
        mutate("tktLogo", "tktLogoRefactor");
        assertFails("TKT-BL-002", "By.id: tktLogo", "id=tktLogoRefactor", By.id("tktLogo"));
    }

    @Test @Order(3)
    @DisplayName("TKT-BL-003 Tiket.com: nav menu Pesawat gagal saat id berubah")
    void tktBl003_pesawatMenu() {
        driver.get("https://www.tiket.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'pesawat') or contains(normalize-space(),'Pesawat')]"));
        setId(orig, "tktPesawat");
        mutate("tktPesawat", "tktPesawatRefactor");
        assertFails("TKT-BL-003", "By.id: tktPesawat", "id=tktPesawatRefactor", By.id("tktPesawat"));
    }

    @Test @Order(4)
    @DisplayName("TKT-BL-004 Tiket.com: nav menu Hotel gagal saat id berubah")
    void tktBl004_hotelMenu() {
        driver.get("https://www.tiket.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'hotel') or contains(normalize-space(),'Hotel')]"));
        setId(orig, "tktHotel");
        mutate("tktHotel", "tktHotelRefactor");
        assertFails("TKT-BL-004", "By.id: tktHotel", "id=tktHotelRefactor", By.id("tktHotel"));
    }

    @Test @Order(5)
    @DisplayName("TKT-BL-005 Tiket.com: nav menu Event gagal saat id berubah")
    void tktBl005_eventMenu() {
        driver.get("https://www.tiket.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'to-do') or contains(normalize-space(),'Event')]"));
        setId(orig, "tktEvent");
        mutate("tktEvent", "tktEventRefactor");
        assertFails("TKT-BL-005", "By.id: tktEvent", "id=tktEventRefactor", By.id("tktEvent"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "tiket", orig, mutated, status, msg});
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
