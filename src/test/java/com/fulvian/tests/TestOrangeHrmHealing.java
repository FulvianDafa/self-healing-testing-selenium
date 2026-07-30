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
 * Self-Healing test untuk SUT OrangeHRM Demo.
 * URL : https://opensource-demo.orangehrmlive.com
 * User: Admin / admin123
 *
 * Jalankan:
 * mvn clean test -Dtest=TestOrangeHrmHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ORANGEHRM DEMO - Self-Healing Locator Test")
public class TestOrangeHrmHealing {

    private static final double THRESHOLD    = 0.50;
    private static final String SUT_LOG_FILE = "results/orangehrm_healing_log.csv";
    private static final String BASE_URL     = "https://opensource-demo.orangehrmlive.com/web/index.php/auth/login";
    private static final String USERNAME     = "Admin";
    private static final String PASSWORD     = "admin123";

    private WebDriver       driver;
    private WebDriverWait   wait;
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
        js   = (JavascriptExecutor) driver;
    }

    @AfterEach
    void tearDown() { if (driver != null) driver.quit(); }

    // ──────────────────────────── HALAMAN LOGIN ────────────────────────────

    @Test @Order(1)
    @DisplayName("OHR-SH-001 Login: input username dipulihkan saat id berubah")
    void ohrSh001_usernameInput() {
        driver.get(BASE_URL);
        WebElement orig = waitVisible(By.xpath("//input[@placeholder='Username' or @name='username']"));
        setId(orig, "ohrUsernameInput");
        mutateId("ohrUsernameInput", "ohrUsernameInputRefactor");

        WebElement el = heal("OHR-SH-001", By.id("ohrUsernameInput"),
                new ElementProfile("Username", "ohrUsernameInput", "id"));

        assertEquals("ohrUsernameInputRefactor", el.getAttribute("id"));
        el.sendKeys(USERNAME);
    }

    @Test @Order(2)
    @DisplayName("OHR-SH-002 Login: input password dipulihkan saat id berubah")
    void ohrSh002_passwordInput() {
        driver.get(BASE_URL);
        WebElement orig = waitVisible(By.xpath("//input[@placeholder='Password' or @type='password']"));
        setId(orig, "ohrPasswordInput");
        mutateId("ohrPasswordInput", "ohrPasswordInputRefactor");

        WebElement el = heal("OHR-SH-002", By.id("ohrPasswordInput"),
                new ElementProfile("Password", "ohrPasswordInput", "id"));

        assertEquals("ohrPasswordInputRefactor", el.getAttribute("id"));
        el.sendKeys(PASSWORD);
    }

    @Test @Order(3)
    @DisplayName("OHR-SH-003 Login: tombol Login dipulihkan saat id berubah")
    void ohrSh003_loginButton() {
        driver.get(BASE_URL);
        waitVisible(By.xpath("//input[@placeholder='Username' or @name='username']")).sendKeys(USERNAME);
        driver.findElement(By.xpath("//input[@placeholder='Password' or @type='password']")).sendKeys(PASSWORD);
        WebElement orig = waitVisible(By.xpath("//button[@type='submit']"));
        setId(orig, "ohrLoginButton");
        mutateId("ohrLoginButton", "ohrLoginButtonRefactor");

        WebElement el = heal("OHR-SH-003", By.id("ohrLoginButton"),
                new ElementProfile("Login", "ohrLoginButton", "id"));

        assertEquals("ohrLoginButtonRefactor", el.getAttribute("id"));
        el.click();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }

    // ──────────────────────────── HALAMAN DASHBOARD ────────────────────────────

    @Test @Order(4)
    @DisplayName("OHR-SH-004 Dashboard: judul halaman dipulihkan saat id berubah")
    void ohrSh004_dashboardTitle() {
        loginAsAdmin();
        WebElement orig = waitVisible(By.xpath(
                "//*[contains(@class,'oxd-topbar-header-title') or normalize-space(.)='Dashboard']"));
        setId(orig, "ohrDashboardTitle");
        mutateId("ohrDashboardTitle", "ohrDashboardTitleRefactor");

        WebElement el = heal("OHR-SH-004", By.id("ohrDashboardTitle"),
                new ElementProfile("Dashboard", "ohrDashboardTitle", "id"));

        assertTrue(el.isDisplayed());
        System.out.println("[OHR-SH-004] Elemen dashboard title berhasil dipulihkan: " + el.getTagName());
    }

    @Test @Order(5)
    @DisplayName("OHR-SH-005 Dashboard: menu sidebar My Info dipulihkan")
    void ohrSh005_sidebarMyInfo() {
        loginAsAdmin();
        WebElement orig = waitVisible(By.xpath(
                "//span[normalize-space()='My Info']/ancestor::a | //a[normalize-space()='My Info']"));
        setId(orig, "ohrSidebarMyInfo");
        mutateId("ohrSidebarMyInfo", "ohrSidebarMyInfoRefactor");

        WebElement el = heal("OHR-SH-005", By.id("ohrSidebarMyInfo"),
                new ElementProfile("My Info", "ohrSidebarMyInfo", "id"));

        assertEquals("ohrSidebarMyInfoRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(6)
    @DisplayName("OHR-SH-006 Dashboard: menu sidebar PIM dipulihkan")
    void ohrSh006_sidebarPim() {
        loginAsAdmin();
        WebElement orig = waitVisible(By.xpath(
                "//span[normalize-space()='PIM']/ancestor::a | //a[normalize-space()='PIM']"));
        setId(orig, "ohrSidebarPim");
        mutateId("ohrSidebarPim", "ohrSidebarPimRefactor");

        WebElement el = heal("OHR-SH-006", By.id("ohrSidebarPim"),
                new ElementProfile("PIM", "ohrSidebarPim", "id"));

        assertEquals("ohrSidebarPimRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    // ──────────────────────────── HALAMAN PIM ────────────────────────────

    @Test @Order(7)
    @DisplayName("OHR-SH-007 PIM: input Search by Name dipulihkan")
    void ohrSh007_searchNameInput() {
        loginAsAdmin();
        driver.get("https://opensource-demo.orangehrmlive.com/web/index.php/pim/viewEmployeeList");
        WebElement orig = waitVisible(By.xpath("//input[@placeholder='Type for hints...'][1]"));
        setId(orig, "ohrSearchNameInput");
        mutateId("ohrSearchNameInput", "ohrSearchNameInputRefactor");

        WebElement el = heal("OHR-SH-007", By.id("ohrSearchNameInput"),
                new ElementProfile("Employee Name", "ohrSearchNameInput", "id"));

        assertEquals("ohrSearchNameInputRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(8)
    @DisplayName("OHR-SH-008 PIM: tombol Search dipulihkan")
    void ohrSh008_searchButton() {
        loginAsAdmin();
        driver.get("https://opensource-demo.orangehrmlive.com/web/index.php/pim/viewEmployeeList");
        WebElement orig = waitVisible(By.xpath("//button[@type='submit' and normalize-space()='Search']"));
        setId(orig, "ohrSearchButton");
        mutateId("ohrSearchButton", "ohrSearchButtonRefactor");

        WebElement el = heal("OHR-SH-008", By.id("ohrSearchButton"),
                new ElementProfile("Search", "ohrSearchButton", "id"));

        assertEquals("ohrSearchButtonRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(9)
    @DisplayName("OHR-SH-009 PIM: tombol Add Employee dipulihkan")
    void ohrSh009_addEmployeeButton() {
        loginAsAdmin();
        driver.get("https://opensource-demo.orangehrmlive.com/web/index.php/pim/viewEmployeeList");
        WebElement orig = waitVisible(By.xpath("//button[normalize-space()='Add']"));
        setId(orig, "ohrAddEmployeeButton");
        mutateId("ohrAddEmployeeButton", "ohrAddEmployeeButtonRefactor");

        WebElement el = heal("OHR-SH-009", By.id("ohrAddEmployeeButton"),
                new ElementProfile("Add", "ohrAddEmployeeButton", "id"));

        assertEquals("ohrAddEmployeeButtonRefactor", el.getAttribute("id"));
        el.click();
        wait.until(ExpectedConditions.urlContains("addEmployee"));
    }

    @Test @Order(10)
    @DisplayName("OHR-SH-010 Add Employee: input First Name dipulihkan")
    void ohrSh010_firstNameInput() {
        loginAsAdmin();
        driver.get("https://opensource-demo.orangehrmlive.com/web/index.php/pim/addEmployee");
        WebElement orig = waitVisible(By.xpath("//input[@placeholder='First Name']"));
        setId(orig, "ohrFirstNameInput");
        mutateId("ohrFirstNameInput", "ohrFirstNameInputRefactor");

        WebElement el = heal("OHR-SH-010", By.id("ohrFirstNameInput"),
                new ElementProfile("First Name", "ohrFirstNameInput", "id"));

        assertEquals("ohrFirstNameInputRefactor", el.getAttribute("id"));
        el.sendKeys("Fulvian");
    }

    // ──────────────────────────── HELPERS ────────────────────────────

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        // Jeda setelah mutasi locator — validator dapat melihat kondisi "locator rusak"
        sleep(1500);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "orangehrm_id_change", THRESHOLD);
        WebElement result = healingDriver.findElement(brokenLocator, profile);
        assertNotNull(result, "[" + testCaseId + "] Self-healing gagal menemukan elemen pengganti.");
        assertTrue(result.isDisplayed(), "[" + testCaseId + "] Elemen hasil healing tidak terlihat.");
        // Jeda setelah healing berhasil — validator dapat melihat elemen yang dipulihkan
        sleep(2000);
        return result;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private void loginAsAdmin() {
        driver.get(BASE_URL);
        waitVisible(By.xpath("//input[@placeholder='Username' or @name='username']")).sendKeys(USERNAME);
        driver.findElement(By.xpath("//input[@placeholder='Password' or @type='password']")).sendKeys(PASSWORD);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
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
                "el.setAttribute('id',arguments[1]); return 1;",
                oldId, newId);
        assertEquals(1L, ((Number) r).longValue(), "Elemen tidak ditemukan saat mutasi: " + oldId);
    }

    private WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}
