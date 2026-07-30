package com.fulvian.tests;

import com.fulvian.healing.ElementProfile;
import com.fulvian.healing.HealingDriver;
import com.fulvian.healing.HealingLogger;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Self-Healing test untuk SUT Indeed (https://www.indeed.com).
 * Jalankan: mvn clean test -Dtest=TestIndeedHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("INDEED - Self-Healing Test")
public class TestIndeedHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/indeed_healing_log.csv";

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    @BeforeAll
    static void prepareLog() throws IOException {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        Files.createDirectories(Paths.get("results"));
        Files.deleteIfExists(Paths.get(SUT_LOG_FILE));
    }

    @AfterAll
    static void printSummary() {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingLogger.printSummary();
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
    @DisplayName("IND-SH-001 Indeed: input what (job title) dipulihkan")
    void indSh001_whatInput() {
        driver.get("https://www.indeed.com");
        WebElement orig = waitVisible(By.id("text-input-what"));
        setId(orig, "indWhatInput");
        mutateId("indWhatInput", "indWhatInputRefactor");

        WebElement el = heal("IND-SH-001", By.id("indWhatInput"),
                new ElementProfile("What Job title keywords or company", "indWhatInput", "id"));
        assertEquals("indWhatInputRefactor", el.getAttribute("id"));
        el.sendKeys("Software Engineer");
    }

    @Test @Order(2)
    @DisplayName("IND-SH-002 Indeed: input where (location) dipulihkan")
    void indSh002_whereInput() {
        driver.get("https://www.indeed.com");
        WebElement orig = waitVisible(By.id("text-input-where"));
        setId(orig, "indWhereInput");
        mutateId("indWhereInput", "indWhereInputRefactor");

        WebElement el = heal("IND-SH-002", By.id("indWhereInput"),
                new ElementProfile("Where City state zip code or remote", "indWhereInput", "id"));
        assertEquals("indWhereInputRefactor", el.getAttribute("id"));
        el.sendKeys("New York");
    }

    @Test @Order(3)
    @DisplayName("IND-SH-003 Indeed: tombol Find Jobs dipulihkan")
    void indSh003_searchButton() {
        driver.get("https://www.indeed.com");
        WebElement orig = waitVisible(By.xpath("//button[@type='submit' and (contains(@class,'submit') or normalize-space()='Find jobs')]"));
        setId(orig, "indSearchButton");
        mutateId("indSearchButton", "indSearchButtonRefactor");

        WebElement el = heal("IND-SH-003", By.id("indSearchButton"),
                new ElementProfile("Find jobs", "indSearchButton", "id"));
        assertEquals("indSearchButtonRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(4)
    @DisplayName("IND-SH-004 Indeed: link Company Reviews dipulihkan")
    void indSh004_companyReviews() {
        driver.get("https://www.indeed.com");
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'companies') or normalize-space()='Company reviews']"));
        setId(orig, "indCompanyReviews");
        mutateId("indCompanyReviews", "indCompanyReviewsRefactor");

        WebElement el = heal("IND-SH-004", By.id("indCompanyReviews"),
                new ElementProfile("Company reviews", "indCompanyReviews", "id"));
        assertEquals("indCompanyReviewsRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(5)
    @DisplayName("IND-SH-005 Indeed: link Salary Guide dipulihkan")
    void indSh005_salaryGuide() {
        driver.get("https://www.indeed.com");
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'salary') or normalize-space()='Salary guide']"));
        setId(orig, "indSalaryGuide");
        mutateId("indSalaryGuide", "indSalaryGuideRefactor");

        WebElement el = heal("IND-SH-005", By.id("indSalaryGuide"),
                new ElementProfile("Salary guide", "indSalaryGuide", "id"));
        assertEquals("indSalaryGuideRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "indeed_id_change", THRESHOLD);
        WebElement result = healingDriver.findElement(brokenLocator, profile);
        assertNotNull(result, "[" + testCaseId + "] Self-healing gagal.");
        assertTrue(result.isDisplayed(), "[" + testCaseId + "] Elemen tidak terlihat.");
        return result;
    }
    private void setId(WebElement el, String id) {
        js.executeScript("arguments[0].setAttribute('id', arguments[1]);", el, id);
    }
    private void mutateId(String oldId, String newId) {
        Object r = js.executeScript(
                "const el=document.getElementById(arguments[0]);" +
                "if(!el) return 0;" +
                "el.setAttribute('data-self-healing-old-id',arguments[0]);" +
                "el.setAttribute('data-self-healing-mutated-from',arguments[0]);" +
                "el.setAttribute('id',arguments[1]); return 1;", oldId, newId);
        assertEquals(1L, ((Number) r).longValue(), "Elemen tidak ditemukan saat mutasi: " + oldId);
    }
    private WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}
