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
 * Baseline test untuk SUT OrangeHRM Demo (https://opensource-demo.orangehrmlive.com).
 *
 * Tujuan baseline:
 * - Mensimulasikan perubahan atribut id pada DOM via JavaScript.
 * - Membuktikan Selenium biasa gagal menemukan locator lama setelah id berubah.
 *
 * Jalankan:
 * mvn clean test -Dtest=TestOrangeHrmBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestOrangeHrmBaseline {

    private static final String BASE_URL  = "https://opensource-demo.orangehrmlive.com/web/index.php/auth/login";
    private static final String USERNAME  = "Admin";
    private static final String PASSWORD  = "admin123";
    private static final Path   LOG_FILE  = Paths.get("results", "orangehrm_baseline_log.csv");

    private static final List<String[]> LOGS = new ArrayList<>();

    private WebDriver driver;
    private WebDriverWait wait;

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
        wait   = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterEach
    void tearDown() { if (driver != null) driver.quit(); }

    // ──────────────────────────── HALAMAN LOGIN ────────────────────────────

    @Test @Order(1)
    @DisplayName("OHR-BL-001 Login: input username gagal setelah id berubah")
    void ohrBl001_usernameInput() {
        driver.get(BASE_URL);
        WebElement el = waitVisible(By.xpath("//input[@placeholder='Username' or @name='username']"));
        setId(el, "ohrUsernameInput");
        mutate("ohrUsernameInput", "ohrUsernameInputRefactor");
        assertFails("OHR-BL-001", "By.id: ohrUsernameInput", "id=ohrUsernameInputRefactor",
                By.id("ohrUsernameInput"));
    }

    @Test @Order(2)
    @DisplayName("OHR-BL-002 Login: input password gagal setelah id berubah")
    void ohrBl002_passwordInput() {
        driver.get(BASE_URL);
        WebElement el = waitVisible(By.xpath("//input[@placeholder='Password' or @type='password']"));
        setId(el, "ohrPasswordInput");
        mutate("ohrPasswordInput", "ohrPasswordInputRefactor");
        assertFails("OHR-BL-002", "By.id: ohrPasswordInput", "id=ohrPasswordInputRefactor",
                By.id("ohrPasswordInput"));
    }

    @Test @Order(3)
    @DisplayName("OHR-BL-003 Login: tombol Login gagal setelah id berubah")
    void ohrBl003_loginButton() {
        driver.get(BASE_URL);
        WebElement el = waitVisible(By.xpath("//button[@type='submit']"));
        setId(el, "ohrLoginButton");
        mutate("ohrLoginButton", "ohrLoginButtonRefactor");
        assertFails("OHR-BL-003", "By.id: ohrLoginButton", "id=ohrLoginButtonRefactor",
                By.id("ohrLoginButton"));
    }

    // ──────────────────────────── HALAMAN DASHBOARD ────────────────────────────

    @Test @Order(4)
    @DisplayName("OHR-BL-004 Dashboard: judul halaman gagal setelah id berubah")
    void ohrBl004_dashboardTitle() {
        loginAsAdmin();
        WebElement el = waitVisible(By.xpath(
                "//*[contains(@class,'oxd-topbar-header-title') or normalize-space(.)='Dashboard']"));
        setId(el, "ohrDashboardTitle");
        mutate("ohrDashboardTitle", "ohrDashboardTitleRefactor");
        assertFails("OHR-BL-004", "By.id: ohrDashboardTitle", "id=ohrDashboardTitleRefactor",
                By.id("ohrDashboardTitle"));
    }

    @Test @Order(5)
    @DisplayName("OHR-BL-005 Dashboard: menu sidebar My Info gagal setelah id berubah")
    void ohrBl005_sidebarMyInfo() {
        loginAsAdmin();
        WebElement el = waitVisible(By.xpath(
                "//span[normalize-space()='My Info']/ancestor::a | //a[normalize-space()='My Info']"));
        setId(el, "ohrSidebarMyInfo");
        mutate("ohrSidebarMyInfo", "ohrSidebarMyInfoRefactor");
        assertFails("OHR-BL-005", "By.id: ohrSidebarMyInfo", "id=ohrSidebarMyInfoRefactor",
                By.id("ohrSidebarMyInfo"));
    }

    @Test @Order(6)
    @DisplayName("OHR-BL-006 Dashboard: menu sidebar PIM gagal setelah id berubah")
    void ohrBl006_sidebarPim() {
        loginAsAdmin();
        WebElement el = waitVisible(By.xpath(
                "//span[normalize-space()='PIM']/ancestor::a | //a[normalize-space()='PIM']"));
        setId(el, "ohrSidebarPim");
        mutate("ohrSidebarPim", "ohrSidebarPimRefactor");
        assertFails("OHR-BL-006", "By.id: ohrSidebarPim", "id=ohrSidebarPimRefactor",
                By.id("ohrSidebarPim"));
    }

    // ──────────────────────────── HALAMAN PIM ────────────────────────────

    @Test @Order(7)
    @DisplayName("OHR-BL-007 PIM: input Search by Name gagal setelah id berubah")
    void ohrBl007_searchNameInput() {
        loginAsAdmin();
        driver.get("https://opensource-demo.orangehrmlive.com/web/index.php/pim/viewEmployeeList");
        WebElement el = waitVisible(By.xpath("//input[@placeholder='Type for hints...'][1]"));
        setId(el, "ohrSearchNameInput");
        mutate("ohrSearchNameInput", "ohrSearchNameInputRefactor");
        assertFails("OHR-BL-007", "By.id: ohrSearchNameInput", "id=ohrSearchNameInputRefactor",
                By.id("ohrSearchNameInput"));
    }

    @Test @Order(8)
    @DisplayName("OHR-BL-008 PIM: tombol Search gagal setelah id berubah")
    void ohrBl008_searchButton() {
        loginAsAdmin();
        driver.get("https://opensource-demo.orangehrmlive.com/web/index.php/pim/viewEmployeeList");
        WebElement el = waitVisible(By.xpath("//button[@type='submit' and normalize-space()='Search']"));
        setId(el, "ohrSearchButton");
        mutate("ohrSearchButton", "ohrSearchButtonRefactor");
        assertFails("OHR-BL-008", "By.id: ohrSearchButton", "id=ohrSearchButtonRefactor",
                By.id("ohrSearchButton"));
    }

    @Test @Order(9)
    @DisplayName("OHR-BL-009 PIM: tombol Add Employee gagal setelah id berubah")
    void ohrBl009_addEmployeeButton() {
        loginAsAdmin();
        driver.get("https://opensource-demo.orangehrmlive.com/web/index.php/pim/viewEmployeeList");
        WebElement el = waitVisible(By.xpath("//button[normalize-space()='Add']"));
        setId(el, "ohrAddEmployeeButton");
        mutate("ohrAddEmployeeButton", "ohrAddEmployeeButtonRefactor");
        assertFails("OHR-BL-009", "By.id: ohrAddEmployeeButton", "id=ohrAddEmployeeButtonRefactor",
                By.id("ohrAddEmployeeButton"));
    }

    @Test @Order(10)
    @DisplayName("OHR-BL-010 Add Employee: input First Name gagal setelah id berubah")
    void ohrBl010_firstNameInput() {
        loginAsAdmin();
        driver.get("https://opensource-demo.orangehrmlive.com/web/index.php/pim/addEmployee");
        WebElement el = waitVisible(By.xpath("//input[@placeholder='First Name']"));
        setId(el, "ohrFirstNameInput");
        mutate("ohrFirstNameInput", "ohrFirstNameInputRefactor");
        assertFails("OHR-BL-010", "By.id: ohrFirstNameInput", "id=ohrFirstNameInputRefactor",
                By.id("ohrFirstNameInput"));
    }

    // ──────────────────────────── HELPERS ────────────────────────────

    private void loginAsAdmin() {
        driver.get(BASE_URL);
        WebElement user = waitVisible(By.xpath("//input[@placeholder='Username' or @name='username']"));
        user.sendKeys(USERNAME);
        driver.findElement(By.xpath("//input[@placeholder='Password' or @type='password']")).sendKeys(PASSWORD);
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }

    private void setId(WebElement el, String id) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].setAttribute('id', arguments[1]);", el, id);
    }

    private void mutate(String oldId, String newId) {
        Object r = ((JavascriptExecutor) driver).executeScript(
                "const el=document.getElementById(arguments[0]);" +
                "if(!el) return 0;" +
                "el.setAttribute('data-self-healing-old-id',arguments[0]);" +
                "el.setAttribute('id',arguments[1]); return 1;",
                oldId, newId);
        assertEquals(1L, ((Number) r).longValue(), "Elemen tidak ditemukan saat mutasi: " + oldId);
    }

    private void assertFails(String id, String orig, String mutated, By locator) {
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> driver.findElement(locator),
                "Baseline harus gagal setelah id berubah");
        log(id, orig, mutated, "LOCATOR_FAILED_EXPECTED", ex.getClass().getSimpleName());
    }

    private void log(String id, String orig, String mutated, String status, String msg) {
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "orangehrm", orig, mutated, status, msg});
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
