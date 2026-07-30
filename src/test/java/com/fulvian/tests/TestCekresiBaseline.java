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
 * Baseline test untuk SUT Cekresi.com (https://www.cekresi.com).
 * Jalankan: mvn clean test -Dtest=TestCekresiBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("CEKRESI - Baseline Test")
public class TestCekresiBaseline {

    private static final Path LOG_FILE = Paths.get("results", "cekresi_baseline_log.csv");
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
    @DisplayName("CKR-BL-001 Cekresi: input nomor resi gagal saat id berubah")
    void ckrBl001_resiInput() {
        driver.get("https://www.cekresi.com");
        WebElement orig = waitVisible(By.id("noresi"));
        setId(orig, "ckrResiInput");
        mutate("ckrResiInput", "ckrResiInputRefactor");
        assertFails("CKR-BL-001", "By.id: ckrResiInput", "id=ckrResiInputRefactor", By.id("ckrResiInput"));
    }

    @Test @Order(2)
    @DisplayName("CKR-BL-002 Cekresi: tombol Cek Resi gagal saat id berubah")
    void ckrBl002_checkButton() {
        driver.get("https://www.cekresi.com");
        WebElement orig = waitVisible(By.id("cekresi"));
        setId(orig, "ckrCheckButton");
        mutate("ckrCheckButton", "ckrCheckButtonRefactor");
        assertFails("CKR-BL-002", "By.id: ckrCheckButton", "id=ckrCheckButtonRefactor", By.id("ckrCheckButton"));
    }

    @Test @Order(3)
    @DisplayName("CKR-BL-003 Cekresi: logo Cekresi gagal saat id berubah")
    void ckrBl003_logo() {
        driver.get("https://www.cekresi.com");
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'cekresi.com') and (./img or contains(@class,'logo') or contains(@class,'brand'))]"));
        setId(orig, "ckrLogo");
        mutate("ckrLogo", "ckrLogoRefactor");
        assertFails("CKR-BL-003", "By.id: ckrLogo", "id=ckrLogoRefactor", By.id("ckrLogo"));
    }

    @Test @Order(4)
    @DisplayName("CKR-BL-004 Cekresi: dropdown ekspedisi gagal saat id berubah")
    void ckrBl004_expedition() {
        driver.get("https://www.cekresi.com");
        WebElement orig = waitVisible(By.xpath("//select[@name='kurir' or @id='kurir'] | //select[contains(@class,'kurir')]"));
        setId(orig, "ckrExpedition");
        mutate("ckrExpedition", "ckrExpeditionRefactor");
        assertFails("CKR-BL-004", "By.id: ckrExpedition", "id=ckrExpeditionRefactor", By.id("ckrExpedition"));
    }

    @Test @Order(5)
    @DisplayName("CKR-BL-005 Cekresi: heading utama gagal saat id berubah")
    void ckrBl005_heading() {
        driver.get("https://www.cekresi.com");
        WebElement orig = waitVisible(By.xpath("//h1 | //h2[contains(normalize-space(),'Cek Resi') or contains(normalize-space(),'Tracking')]"));
        setId(orig, "ckrHeading");
        mutate("ckrHeading", "ckrHeadingRefactor");
        assertFails("CKR-BL-005", "By.id: ckrHeading", "id=ckrHeadingRefactor", By.id("ckrHeading"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "cekresi", orig, mutated, status, msg});
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
