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
 * Baseline test untuk SUT ESPN (https://www.espn.com).
 * Jalankan: mvn clean test -Dtest=TestEspnBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ESPN - Baseline Test")
public class TestEspnBaseline {

    private static final Path LOG_FILE = Paths.get("results", "espn_baseline_log.csv");
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
            driver.findElements(By.xpath("//button[contains(@class,'close') or contains(@aria-label,'close') or normalize-space()='Accept All Cookies']"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("ESPN-BL-001 ESPN: input pencarian gagal saat id berubah")
    void espnBl001_searchInput() {
        driver.get("https://www.espn.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//input[@type='search' or @placeholder='Search']"));
        setId(orig, "espnSearchInput");
        mutate("espnSearchInput", "espnSearchInputRefactor");
        assertFails("ESPN-BL-001", "By.id: espnSearchInput", "id=espnSearchInputRefactor", By.id("espnSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("ESPN-BL-002 ESPN: nav NFL gagal saat id berubah")
    void espnBl002_nfl() {
        driver.get("https://www.espn.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='NFL']"));
        setId(orig, "espnNfl");
        mutate("espnNfl", "espnNflRefactor");
        assertFails("ESPN-BL-002", "By.id: espnNfl", "id=espnNflRefactor", By.id("espnNfl"));
    }

    @Test @Order(3)
    @DisplayName("ESPN-BL-003 ESPN: nav NBA gagal saat id berubah")
    void espnBl003_nba() {
        driver.get("https://www.espn.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='NBA']"));
        setId(orig, "espnNba");
        mutate("espnNba", "espnNbaRefactor");
        assertFails("ESPN-BL-003", "By.id: espnNba", "id=espnNbaRefactor", By.id("espnNba"));
    }

    @Test @Order(4)
    @DisplayName("ESPN-BL-004 ESPN: nav Soccer gagal saat id berubah")
    void espnBl004_soccer() {
        driver.get("https://www.espn.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Soccer']"));
        setId(orig, "espnSoccer");
        mutate("espnSoccer", "espnSoccerRefactor");
        assertFails("ESPN-BL-004", "By.id: espnSoccer", "id=espnSoccerRefactor", By.id("espnSoccer"));
    }

    @Test @Order(5)
    @DisplayName("ESPN-BL-005 ESPN: logo ESPN gagal saat id berubah")
    void espnBl005_logo() {
        driver.get("https://www.espn.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[@href='http://www.espn.com' or @aria-label='ESPN Home']"));
        setId(orig, "espnLogo");
        mutate("espnLogo", "espnLogoRefactor");
        assertFails("ESPN-BL-005", "By.id: espnLogo", "id=espnLogoRefactor", By.id("espnLogo"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "espn", orig, mutated, status, msg});
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
