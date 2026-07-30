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
 * Self-Healing test untuk SUT Etsy (https://www.etsy.com).
 * Jalankan: mvn clean test -Dtest=TestEtsyHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ETSY - Self-Healing Test")
public class TestEtsyHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/etsy_healing_log.csv";

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
    @DisplayName("ETSY-SH-001 Etsy: input pencarian dipulihkan")
    void etsySh001_searchInput() {
        driver.get("https://www.etsy.com");
        dismissPopup();
        WebElement orig = waitVisible(By.name("search_query"));
        setId(orig, "etsySearchInput");
        mutateId("etsySearchInput", "etsySearchInputRefactor");

        WebElement el = heal("ETSY-SH-001", By.id("etsySearchInput"),
                new ElementProfile("Search for anything", "etsySearchInput", "id"));
        assertEquals("etsySearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("handmade jewelry");
    }

    @Test @Order(2)
    @DisplayName("ETSY-SH-002 Etsy: tombol pencarian dipulihkan")
    void etsySh002_searchButton() {
        driver.get("https://www.etsy.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//button[@type='submit' and contains(@aria-label,'Search')]"));
        setId(orig, "etsySearchButton");
        mutateId("etsySearchButton", "etsySearchButtonRefactor");

        WebElement el = heal("ETSY-SH-002", By.id("etsySearchButton"),
                new ElementProfile("Search", "etsySearchButton", "id"));
        assertEquals("etsySearchButtonRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(3)
    @DisplayName("ETSY-SH-003 Etsy: link Sign In dipulihkan")
    void etsySh003_signIn() {
        driver.get("https://www.etsy.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'/signin') or normalize-space()='Sign in']"));
        setId(orig, "etsySignIn");
        mutateId("etsySignIn", "etsySignInRefactor");

        WebElement el = heal("ETSY-SH-003", By.id("etsySignIn"),
                new ElementProfile("Sign in", "etsySignIn", "id"));
        assertEquals("etsySignInRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(4)
    @DisplayName("ETSY-SH-004 Etsy: link Cart dipulihkan")
    void etsySh004_cart() {
        driver.get("https://www.etsy.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'/cart') or @aria-label='Cart']"));
        setId(orig, "etsyCart");
        mutateId("etsyCart", "etsyCartRefactor");

        WebElement el = heal("ETSY-SH-004", By.id("etsyCart"),
                new ElementProfile("Cart", "etsyCart", "id"));
        assertEquals("etsyCartRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(5)
    @DisplayName("ETSY-SH-005 Etsy: logo Etsy dipulihkan")
    void etsySh005_logo() {
        driver.get("https://www.etsy.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[@aria-label='Etsy' or contains(@href,'etsy.com') and .//*[name()='svg']][1]"));
        setId(orig, "etsyLogo");
        mutateId("etsyLogo", "etsyLogoRefactor");

        WebElement el = heal("ETSY-SH-005", By.id("etsyLogo"),
                new ElementProfile("Etsy", "etsyLogo", "id"));
        assertEquals("etsyLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "etsy_id_change", THRESHOLD);
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
