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
 * Self-Healing test untuk SUT Amazon (https://www.amazon.com).
 * Jalankan: mvn clean test -Dtest=TestAmazonHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("AMAZON - Self-Healing Test")
public class TestAmazonHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/amazon_healing_log.csv";

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
        opts.addArguments("--start-maximized", "--disable-blink-features=AutomationControlled");
        opts.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
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
            driver.findElements(By.xpath("//*[@id='nav-flyout-ya-signin' or contains(@class,'a-popover-closebutton')]"))
                    .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("AMZ-SH-001 Amazon: input pencarian dipulihkan")
    void amzSh001_searchInput() {
        driver.get("https://www.amazon.com");
        dismissPopup();
        WebElement orig = waitVisible(By.id("twotabsearchtextbox"));
        setId(orig, "amzSearchInput");
        mutateId("amzSearchInput", "amzSearchInputRefactor");

        WebElement el = heal("AMZ-SH-001", By.id("amzSearchInput"),
                new ElementProfile("Search Amazon", "amzSearchInput", "id"));
        assertEquals("amzSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("Selenium");
    }

    @Test @Order(2)
    @DisplayName("AMZ-SH-002 Amazon: tombol pencarian dipulihkan")
    void amzSh002_searchButton() {
        driver.get("https://www.amazon.com");
        dismissPopup();
        WebElement orig = waitVisible(By.id("nav-search-submit-button"));
        setId(orig, "amzSearchButton");
        mutateId("amzSearchButton", "amzSearchButtonRefactor");

        WebElement el = heal("AMZ-SH-002", By.id("amzSearchButton"),
                new ElementProfile("Go", "amzSearchButton", "id"));
        assertEquals("amzSearchButtonRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(3)
    @DisplayName("AMZ-SH-003 Amazon: link nav hamburger dipulihkan")
    void amzSh003_navAll() {
        driver.get("https://www.amazon.com");
        dismissPopup();
        WebElement orig = waitVisible(By.id("nav-hamburger-menu"));
        setId(orig, "amzNavAll");
        mutateId("amzNavAll", "amzNavAllRefactor");

        WebElement el = heal("AMZ-SH-003", By.id("amzNavAll"),
                new ElementProfile("All", "amzNavAll", "id"));
        assertEquals("amzNavAllRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(4)
    @DisplayName("AMZ-SH-004 Amazon: link Returns & Orders dipulihkan")
    void amzSh004_ordersLink() {
        driver.get("https://www.amazon.com");
        dismissPopup();
        WebElement orig = waitVisible(By.id("nav-orders"));
        setId(orig, "amzOrdersLink");
        mutateId("amzOrdersLink", "amzOrdersLinkRefactor");

        WebElement el = heal("AMZ-SH-004", By.id("amzOrdersLink"),
                new ElementProfile("Returns Orders", "amzOrdersLink", "id"));
        assertEquals("amzOrdersLinkRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(5)
    @DisplayName("AMZ-SH-005 Amazon: link Cart dipulihkan")
    void amzSh005_cartLink() {
        driver.get("https://www.amazon.com");
        dismissPopup();
        WebElement orig = waitVisible(By.id("nav-cart"));
        setId(orig, "amzCartLink");
        mutateId("amzCartLink", "amzCartLinkRefactor");

        WebElement el = heal("AMZ-SH-005", By.id("amzCartLink"),
                new ElementProfile("Cart", "amzCartLink", "id"));
        assertEquals("amzCartLinkRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "amazon_id_change", THRESHOLD);
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
