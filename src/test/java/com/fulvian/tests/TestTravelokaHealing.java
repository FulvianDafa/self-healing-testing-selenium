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
 * Self-Healing test untuk SUT Traveloka (https://www.traveloka.com).
 * Jalankan: mvn clean test -Dtest=TestTravelokaHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("TRAVELOKA - Self-Healing Test")
public class TestTravelokaHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/traveloka_healing_log.csv";

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
            driver.findElements(By.xpath("//div[contains(@class,'close') or @aria-label='close']"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("TVL-SH-001 Traveloka: input pencarian kota dipulihkan")
    void tvlSh001_searchInput() {
        driver.get("https://www.traveloka.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//input[@data-testid='autocomplete-field' or contains(@placeholder,'City, hotel')]"));
        setId(orig, "tvlSearchInput");
        mutateId("tvlSearchInput", "tvlSearchInputRefactor");

        WebElement el = heal("TVL-SH-001", By.id("tvlSearchInput"),
                new ElementProfile("City hotel place to go", "tvlSearchInput", "id"));
        assertEquals("tvlSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("Bali");
    }

    @Test @Order(2)
    @DisplayName("TVL-SH-002 Traveloka: tombol cari hotel dipulihkan")
    void tvlSh002_searchButton() {
        driver.get("https://www.traveloka.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//div[@data-testid='search-submit-button' or contains(normalize-space(),'Search Hotels')]/parent::div[@role='button'] | //div[text()='Search Hotels']/ancestor::div[@role='button']"));
        setId(orig, "tvlSearchButton");
        mutateId("tvlSearchButton", "tvlSearchButtonRefactor");

        WebElement el = heal("TVL-SH-002", By.id("tvlSearchButton"),
                new ElementProfile("Search Hotels", "tvlSearchButton", "id"));
        assertEquals("tvlSearchButtonRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(3)
    @DisplayName("TVL-SH-003 Traveloka: logo Traveloka dipulihkan")
    void tvlSh003_logo() {
        driver.get("https://www.traveloka.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//img[contains(@alt,'Traveloka')]/parent::a | //a[@data-testid='header-logo']"));
        setId(orig, "tvlLogo");
        mutateId("tvlLogo", "tvlLogoRefactor");

        WebElement el = heal("TVL-SH-003", By.id("tvlLogo"),
                new ElementProfile("Traveloka", "tvlLogo", "id"));
        assertEquals("tvlLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(4)
    @DisplayName("TVL-SH-004 Traveloka: nav menu Flights dipulihkan")
    void tvlSh004_flightsMenu() {
        driver.get("https://www.traveloka.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//div[normalize-space()='Flights' or normalize-space()='Tiket Pesawat']"));
        setId(orig, "tvlFlights");
        mutateId("tvlFlights", "tvlFlightsRefactor");

        WebElement el = heal("TVL-SH-004", By.id("tvlFlights"),
                new ElementProfile("Flights", "tvlFlights", "id"));
        assertEquals("tvlFlightsRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(5)
    @DisplayName("TVL-SH-005 Traveloka: nav menu Hotels dipulihkan")
    void tvlSh005_hotelsMenu() {
        driver.get("https://www.traveloka.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//div[normalize-space()='Hotels' or normalize-space()='Hotel']"));
        setId(orig, "tvlHotels");
        mutateId("tvlHotels", "tvlHotelsRefactor");

        WebElement el = heal("TVL-SH-005", By.id("tvlHotels"),
                new ElementProfile("Hotels", "tvlHotels", "id"));
        assertEquals("tvlHotelsRefactor", el.getAttribute("id"));
        el.click();
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "traveloka_id_change", THRESHOLD);
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
