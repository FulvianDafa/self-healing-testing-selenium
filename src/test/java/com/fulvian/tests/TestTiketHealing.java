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
 * Self-Healing test untuk SUT Tiket.com (https://www.tiket.com).
 * Jalankan: mvn clean test -Dtest=TestTiketHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("TIKET.COM - Self-Healing Test")
public class TestTiketHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/tiket_healing_log.csv";

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
            driver.findElements(By.xpath("//div[contains(@class,'close') or @aria-label='Close' or @aria-label='Tutup']"))
                .stream().filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("TKT-SH-001 Tiket.com: input pencarian kota dipulihkan")
    void tktSh001_searchInput() {
        driver.get("https://www.tiket.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//input[@aria-label='Search Text Input' or contains(@placeholder,'Mau ke mana')]"));
        setId(orig, "tktSearchInput");
        mutateId("tktSearchInput", "tktSearchInputRefactor");

        WebElement el = heal("TKT-SH-001", By.id("tktSearchInput"),
                new ElementProfile("Mau ke mana", "tktSearchInput", "id"));
        assertEquals("tktSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("Jakarta");
    }

    @Test @Order(2)
    @DisplayName("TKT-SH-002 Tiket.com: logo tiket.com dipulihkan")
    void tktSh002_logo() {
        driver.get("https://www.tiket.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'tiket.com')]//img[contains(@alt,'tiket.com') or contains(@alt,'logo')]"));
        setId(orig, "tktLogo");
        mutateId("tktLogo", "tktLogoRefactor");

        WebElement el = heal("TKT-SH-002", By.id("tktLogo"),
                new ElementProfile("tiket.com", "tktLogo", "id"));
        assertEquals("tktLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(3)
    @DisplayName("TKT-SH-003 Tiket.com: nav menu Pesawat dipulihkan")
    void tktSh003_pesawatMenu() {
        driver.get("https://www.tiket.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'pesawat') or contains(normalize-space(),'Pesawat')]"));
        setId(orig, "tktPesawat");
        mutateId("tktPesawat", "tktPesawatRefactor");

        WebElement el = heal("TKT-SH-003", By.id("tktPesawat"),
                new ElementProfile("Pesawat", "tktPesawat", "id"));
        assertEquals("tktPesawatRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(4)
    @DisplayName("TKT-SH-004 Tiket.com: nav menu Hotel dipulihkan")
    void tktSh004_hotelMenu() {
        driver.get("https://www.tiket.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'hotel') or contains(normalize-space(),'Hotel')]"));
        setId(orig, "tktHotel");
        mutateId("tktHotel", "tktHotelRefactor");

        WebElement el = heal("TKT-SH-004", By.id("tktHotel"),
                new ElementProfile("Hotel", "tktHotel", "id"));
        assertEquals("tktHotelRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(5)
    @DisplayName("TKT-SH-005 Tiket.com: nav menu Event dipulihkan")
    void tktSh005_eventMenu() {
        driver.get("https://www.tiket.com");
        dismissPopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'to-do') or contains(normalize-space(),'Event')]"));
        setId(orig, "tktEvent");
        mutateId("tktEvent", "tktEventRefactor");

        WebElement el = heal("TKT-SH-005", By.id("tktEvent"),
                new ElementProfile("Event", "tktEvent", "id"));
        assertEquals("tktEventRefactor", el.getAttribute("id"));
        el.click();
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "tiket_id_change", THRESHOLD);
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
