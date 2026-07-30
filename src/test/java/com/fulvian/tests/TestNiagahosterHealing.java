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
 * Self-Healing test untuk SUT Niagahoster (https://www.niagahoster.co.id).
 * Jalankan: mvn clean test -Dtest=TestNiagahosterHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("NIAGAHOSTER - Self-Healing Test")
public class TestNiagahosterHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/niagahoster_healing_log.csv";

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
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
            driver.findElements(By.xpath("//button[contains(@class,'close') or contains(@aria-label,'Close') or contains(@class,'kodee-close')]"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("NGH-SH-001 Niagahoster: input domain dipulihkan")
    void nghSh001_domainInput() {
        driver.get("https://www.niagahoster.co.id");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//input[contains(@id,'domain-input') or contains(@placeholder,'domain')]"));
        setId(orig, "nghDomainInput");
        mutateId("nghDomainInput", "nghDomainInputRefactor");

        WebElement el = heal("NGH-SH-001", By.id("nghDomainInput"),
                new ElementProfile("domain", "nghDomainInput", "id"));
        assertEquals("nghDomainInputRefactor", el.getAttribute("id"));
        el.sendKeys("tokosaya");
    }

    @Test @Order(2)
    @DisplayName("NGH-SH-002 Niagahoster: tombol cari domain dipulihkan")
    void nghSh002_domainButton() {
        driver.get("https://www.niagahoster.co.id");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//button[contains(normalize-space(),'Cari Domain') or contains(normalize-space(),'Cari')]"));
        setId(orig, "nghDomainButton");
        mutateId("nghDomainButton", "nghDomainButtonRefactor");

        WebElement el = heal("NGH-SH-002", By.id("nghDomainButton"),
                new ElementProfile("Cari Domain", "nghDomainButton", "id"));
        assertEquals("nghDomainButtonRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(3)
    @DisplayName("NGH-SH-003 Niagahoster: logo Niagahoster dipulihkan")
    void nghSh003_logo() {
        driver.get("https://www.niagahoster.co.id");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[.//img[contains(@alt,'Niagahoster')]]"));
        setId(orig, "nghLogo");
        mutateId("nghLogo", "nghLogoRefactor");

        WebElement el = heal("NGH-SH-003", By.id("nghLogo"),
                new ElementProfile("Niagahoster", "nghLogo", "id"));
        assertEquals("nghLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(4)
    @DisplayName("NGH-SH-004 Niagahoster: link Hosting dipulihkan")
    void nghSh004_hostingMenu() {
        driver.get("https://www.niagahoster.co.id");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(normalize-space(),'Hosting') and contains(@href,'hosting')]"));
        setId(orig, "nghHosting");
        mutateId("nghHosting", "nghHostingRefactor");

        WebElement el = heal("NGH-SH-004", By.id("nghHosting"),
                new ElementProfile("Hosting", "nghHosting", "id"));
        assertEquals("nghHostingRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(5)
    @DisplayName("NGH-SH-005 Niagahoster: tombol Login dipulihkan")
    void nghSh005_login() {
        driver.get("https://www.niagahoster.co.id");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Login' or contains(@href,'login')]"));
        setId(orig, "nghLogin");
        mutateId("nghLogin", "nghLoginRefactor");

        WebElement el = heal("NGH-SH-005", By.id("nghLogin"),
                new ElementProfile("Login", "nghLogin", "id"));
        assertEquals("nghLoginRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "niagahoster_id_change", THRESHOLD);
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
