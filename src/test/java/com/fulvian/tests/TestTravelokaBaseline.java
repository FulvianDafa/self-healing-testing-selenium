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
 * Baseline test untuk SUT Traveloka (https://www.traveloka.com).
 * Jalankan: mvn clean test -Dtest=TestTravelokaBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("TRAVELOKA - Baseline Test")
public class TestTravelokaBaseline {

    private static final Path LOG_FILE = Paths.get("results", "traveloka_baseline_log.csv");
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
            driver.findElements(By.xpath("//div[contains(@class,'close') or @aria-label='close']"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("TVL-BL-001 Traveloka: input pencarian kota gagal saat id berubah")
    void tvlBl001_searchInput() {
        driver.get("https://www.traveloka.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//input[@data-testid='autocomplete-field' or contains(@placeholder,'City, hotel')]"));
        setId(orig, "tvlSearchInput");
        mutate("tvlSearchInput", "tvlSearchInputRefactor");
        assertFails("TVL-BL-001", "By.id: tvlSearchInput", "id=tvlSearchInputRefactor", By.id("tvlSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("TVL-BL-002 Traveloka: tombol cari hotel gagal saat id berubah")
    void tvlBl002_searchButton() {
        driver.get("https://www.traveloka.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//div[@data-testid='search-submit-button' or contains(normalize-space(),'Search Hotels')]/parent::div[@role='button'] | //div[text()='Search Hotels']/ancestor::div[@role='button']"));
        setId(orig, "tvlSearchButton");
        mutate("tvlSearchButton", "tvlSearchButtonRefactor");
        assertFails("TVL-BL-002", "By.id: tvlSearchButton", "id=tvlSearchButtonRefactor", By.id("tvlSearchButton"));
    }

    @Test @Order(3)
    @DisplayName("TVL-BL-003 Traveloka: logo Traveloka gagal saat id berubah")
    void tvlBl003_logo() {
        driver.get("https://www.traveloka.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//img[contains(@alt,'Traveloka')]/parent::a | //a[@data-testid='header-logo']"));
        setId(orig, "tvlLogo");
        mutate("tvlLogo", "tvlLogoRefactor");
        assertFails("TVL-BL-003", "By.id: tvlLogo", "id=tvlLogoRefactor", By.id("tvlLogo"));
    }

    @Test @Order(4)
    @DisplayName("TVL-BL-004 Traveloka: nav menu Flights gagal saat id berubah")
    void tvlBl004_flightsMenu() {
        driver.get("https://www.traveloka.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//div[normalize-space()='Flights' or normalize-space()='Tiket Pesawat']"));
        setId(orig, "tvlFlights");
        mutate("tvlFlights", "tvlFlightsRefactor");
        assertFails("TVL-BL-004", "By.id: tvlFlights", "id=tvlFlightsRefactor", By.id("tvlFlights"));
    }

    @Test @Order(5)
    @DisplayName("TVL-BL-005 Traveloka: nav menu Hotels gagal saat id berubah")
    void tvlBl005_hotelsMenu() {
        driver.get("https://www.traveloka.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//div[normalize-space()='Hotels' or normalize-space()='Hotel']"));
        setId(orig, "tvlHotels");
        mutate("tvlHotels", "tvlHotelsRefactor");
        assertFails("TVL-BL-005", "By.id: tvlHotels", "id=tvlHotelsRefactor", By.id("tvlHotels"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "traveloka", orig, mutated, status, msg});
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
