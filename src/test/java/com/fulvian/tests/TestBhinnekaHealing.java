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
 * Self-Healing test untuk SUT Bhinneka (https://www.bhinneka.com).
 * Jalankan: mvn clean test -Dtest=TestBhinnekaHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("BHINNEKA - Self-Healing Test")
public class TestBhinnekaHealing {

    private static final double THRESHOLD = 0.50;
    private static final String SUT_LOG_FILE = "results/bhinneka_healing_log.csv";

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
    @DisplayName("BHK-SH-001 Bhinneka: input pencarian dipulihkan")
    void bhkSh001_searchInput() {
        driver.get("https://www.bhinneka.com");
        WebElement orig = waitVisible(By.xpath("//input[contains(@class,'oe_search_box') or contains(@placeholder,'Cari produk asli')]"));
        setId(orig, "bhkSearchInput");
        mutateId("bhkSearchInput", "bhkSearchInputRefactor");

        WebElement el = heal("BHK-SH-001", By.id("bhkSearchInput"),
                new ElementProfile("Cari produk", "bhkSearchInput", "id"));
        assertEquals("bhkSearchInputRefactor", el.getAttribute("id"));
        el.sendKeys("Laptop B2B");
    }

    @Test @Order(2)
    @DisplayName("BHK-SH-002 Bhinneka: logo Bhinneka dipulihkan")
    void bhkSh002_logo() {
        driver.get("https://www.bhinneka.com");
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'bhinneka.com') and .//img[contains(@alt,'Bhinneka')]]"));
        setId(orig, "bhkLogo");
        mutateId("bhkLogo", "bhkLogoRefactor");

        WebElement el = heal("BHK-SH-002", By.id("bhkLogo"),
                new ElementProfile("Bhinneka", "bhkLogo", "id"));
        assertEquals("bhkLogoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(3)
    @DisplayName("BHK-SH-003 Bhinneka: link Cart/Keranjang dipulihkan")
    void bhkSh003_cart() {
        driver.get("https://www.bhinneka.com");
        WebElement orig = waitVisible(By.xpath("//a[contains(@href,'/cart') or contains(@class,'my_cart_quantity')]"));
        setId(orig, "bhkCart");
        mutateId("bhkCart", "bhkCartRefactor");

        WebElement el = heal("BHK-SH-003", By.id("bhkCart"),
                new ElementProfile("Keranjang", "bhkCart", "id"));
        assertEquals("bhkCartRefactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(4)
    @DisplayName("BHK-SH-004 Bhinneka: link Kategori dipulihkan")
    void bhkSh004_kategori() {
        driver.get("https://www.bhinneka.com");
        WebElement orig = waitVisible(By.xpath("//span[normalize-space()='Kategori' or contains(@class,'Kategori')]"));
        setId(orig, "bhkKategori");
        mutateId("bhkKategori", "bhkKategoriRefactor");

        WebElement el = heal("BHK-SH-004", By.id("bhkKategori"),
                new ElementProfile("Kategori", "bhkKategori", "id"));
        assertEquals("bhkKategoriRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(5)
    @DisplayName("BHK-SH-005 Bhinneka: tombol Login dipulihkan")
    void bhkSh005_login() {
        driver.get("https://www.bhinneka.com");
        WebElement orig = waitVisible(By.xpath("//a[normalize-space()='Login' or contains(@href,'/web/login')]"));
        setId(orig, "bhkLogin");
        mutateId("bhkLogin", "bhkLoginRefactor");

        WebElement el = heal("BHK-SH-005", By.id("bhkLogin"),
                new ElementProfile("Login", "bhkLogin", "id"));
        assertEquals("bhkLoginRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "bhinneka_id_change", THRESHOLD);
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
