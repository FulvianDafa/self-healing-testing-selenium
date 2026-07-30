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
 * Baseline test untuk SUT Indeed (https://www.indeed.com).
 * Jalankan: mvn clean test -Dtest=TestIndeedBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("INDEED - Baseline Test")
public class TestIndeedBaseline {

    private static final Path LOG_FILE = Paths.get("results", "indeed_baseline_log.csv");
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
    @DisplayName("IND-BL-001 Indeed: input what (job title) gagal saat id berubah")
    void indBl001_whatInput() {
        driver.get("https://www.indeed.com");
        WebElement orig = waitVisible(By.id("text-input-what"));
        setId(orig, "indWhatInput");
        mutate("indWhatInput", "indWhatInputRefactor");
        assertFails("IND-BL-001", "By.id: indWhatInput", "id=indWhatInputRefactor", By.id("indWhatInput"));
    }

    @Test @Order(2)
    @DisplayName("IND-BL-002 Indeed: input where (location) gagal saat id berubah")
    void indBl002_whereInput() {
        driver.get("https://www.indeed.com");
        WebElement orig = waitVisible(By.id("text-input-where"));
        setId(orig, "indWhereInput");
        mutate("indWhereInput", "indWhereInputRefactor");
        assertFails("IND-BL-002", "By.id: indWhereInput", "id=indWhereInputRefactor", By.id("indWhereInput"));
    }

    @Test @Order(3)
    @DisplayName("IND-BL-003 Indeed: tombol Find Jobs gagal saat id berubah")
    void indBl003_searchButton() {
        driver.get("https://www.indeed.com");
        WebElement orig = waitVisible(By.xpath("//button[@type='submit' and (contains(@class,'submit') or normalize-space()='Find jobs')]"));
        setId(orig, "indSearchButton");
        mutate("indSearchButton", "indSearchButtonRefactor");
        assertFails("IND-BL-003", "By.id: indSearchButton", "id=indSearchButtonRefactor", By.id("indSearchButton"));
    }

    @Test @Order(4)
    @DisplayName("IND-BL-004 Indeed: link Company Reviews gagal saat id berubah")
    void indBl004_companyReviews() {
        driver.get("https://www.indeed.com");
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'companies') or normalize-space()='Company reviews']"));
        setId(orig, "indCompanyReviews");
        mutate("indCompanyReviews", "indCompanyReviewsRefactor");
        assertFails("IND-BL-004", "By.id: indCompanyReviews", "id=indCompanyReviewsRefactor", By.id("indCompanyReviews"));
    }

    @Test @Order(5)
    @DisplayName("IND-BL-005 Indeed: link Salary Guide gagal saat id berubah")
    void indBl005_salaryGuide() {
        driver.get("https://www.indeed.com");
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'salary') or normalize-space()='Salary guide']"));
        setId(orig, "indSalaryGuide");
        mutate("indSalaryGuide", "indSalaryGuideRefactor");
        assertFails("IND-BL-005", "By.id: indSalaryGuide", "id=indSalaryGuideRefactor", By.id("indSalaryGuide"));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "indeed", orig, mutated, status, msg});
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
