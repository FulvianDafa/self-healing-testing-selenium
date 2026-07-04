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
 * Baseline test untuk SUT SauceDemo (https://www.saucedemo.com).
 *
 * Tujuan baseline:
 * - Mensimulasikan perubahan atribut id pada DOM via JavaScript.
 * - Membuktikan Selenium biasa gagal menemukan locator lama setelah id berubah.
 * - Test dianggap PASS jika NoSuchElementException terjadi sesuai ekspektasi baseline.
 *
 * Jalankan:
 * mvn clean test -Dtest=TestSauceDemoBaseline
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestSauceDemoBaseline {

    private static final String BASE_URL  = "https://www.saucedemo.com";
    private static final String USERNAME  = "standard_user";
    private static final String PASSWORD  = "secret_sauce";
    private static final Path   LOG_FILE  = Paths.get("results", "saucedemo_baseline_log.csv");

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
        wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void tearDown() { if (driver != null) driver.quit(); }

    // ──────────────────────────── HALAMAN LOGIN ────────────────────────────

    @Test @Order(1)
    @DisplayName("SCD-BL-001 Login: input username gagal setelah id berubah")
    void scdBl001_usernameInput() {
        driver.get(BASE_URL);
        waitVisible(By.id("user-name"));
        mutate("user-name", "usernameInputRefactor");
        assertFails("SCD-BL-001", "By.id: user-name", "id=usernameInputRefactor", By.id("user-name"));
    }

    @Test @Order(2)
    @DisplayName("SCD-BL-002 Login: input password gagal setelah id berubah")
    void scdBl002_passwordInput() {
        driver.get(BASE_URL);
        waitVisible(By.id("password"));
        mutate("password", "passwordInputRefactor");
        assertFails("SCD-BL-002", "By.id: password", "id=passwordInputRefactor", By.id("password"));
    }

    @Test @Order(3)
    @DisplayName("SCD-BL-003 Login: tombol Login gagal setelah id berubah")
    void scdBl003_loginButton() {
        driver.get(BASE_URL);
        waitVisible(By.id("login-button"));
        mutate("login-button", "loginButtonRefactor");
        assertFails("SCD-BL-003", "By.id: login-button", "id=loginButtonRefactor", By.id("login-button"));
    }

    // ──────────────────────────── HALAMAN PRODUK ────────────────────────────

    @Test @Order(4)
    @DisplayName("SCD-BL-004 Produk: judul halaman gagal setelah id berubah")
    void scdBl004_pageTitle() {
        loginAsStandard();
        setIdByXPath("//*[contains(@class,'title') and normalize-space(.)='Products']", "productsPageTitle");
        mutate("productsPageTitle", "productsTitleRefactor");
        assertFails("SCD-BL-004", "By.id: productsPageTitle", "id=productsTitleRefactor", By.id("productsPageTitle"));
    }

    @Test @Order(5)
    @DisplayName("SCD-BL-005 Produk: tombol Add to Cart item-1 gagal setelah id berubah")
    void scdBl005_addToCartItem1() {
        loginAsStandard();
        waitVisible(By.id("add-to-cart-sauce-labs-backpack"));
        mutate("add-to-cart-sauce-labs-backpack", "addToCartItem1Refactor");
        assertFails("SCD-BL-005", "By.id: add-to-cart-sauce-labs-backpack", "id=addToCartItem1Refactor",
                By.id("add-to-cart-sauce-labs-backpack"));
    }

    @Test @Order(6)
    @DisplayName("SCD-BL-006 Produk: tombol Add to Cart item-2 gagal setelah id berubah")
    void scdBl006_addToCartItem2() {
        loginAsStandard();
        waitVisible(By.id("add-to-cart-sauce-labs-bike-light"));
        mutate("add-to-cart-sauce-labs-bike-light", "addToCartItem2Refactor");
        assertFails("SCD-BL-006", "By.id: add-to-cart-sauce-labs-bike-light", "id=addToCartItem2Refactor",
                By.id("add-to-cart-sauce-labs-bike-light"));
    }

    @Test @Order(7)
    @DisplayName("SCD-BL-007 Produk: icon keranjang gagal setelah id berubah")
    void scdBl007_cartIcon() {
        loginAsStandard();
        waitVisible(By.id("shopping_cart_container"));
        mutate("shopping_cart_container", "cartIconRefactor");
        assertFails("SCD-BL-007", "By.id: shopping_cart_container", "id=cartIconRefactor",
                By.id("shopping_cart_container"));
    }

    @Test @Order(8)
    @DisplayName("SCD-BL-008 Produk: menu hamburger gagal setelah id berubah")
    void scdBl008_burgerMenu() {
        loginAsStandard();
        waitVisible(By.id("react-burger-menu-btn"));
        mutate("react-burger-menu-btn", "burgerMenuRefactor");
        assertFails("SCD-BL-008", "By.id: react-burger-menu-btn", "id=burgerMenuRefactor",
                By.id("react-burger-menu-btn"));
    }

    // ──────────────────────────── HALAMAN CART ────────────────────────────

    @Test @Order(9)
    @DisplayName("SCD-BL-009 Cart: tombol Checkout gagal setelah id berubah")
    void scdBl009_checkoutButton() {
        loginAsStandard();
        driver.findElement(By.id("add-to-cart-sauce-labs-backpack")).click();
        driver.findElement(By.id("shopping_cart_container")).click();
        waitVisible(By.id("checkout"));
        mutate("checkout", "checkoutButtonRefactor");
        assertFails("SCD-BL-009", "By.id: checkout", "id=checkoutButtonRefactor", By.id("checkout"));
    }

    @Test @Order(10)
    @DisplayName("SCD-BL-010 Checkout: input first name gagal setelah id berubah")
    void scdBl010_firstNameInput() {
        loginAsStandard();
        driver.findElement(By.id("add-to-cart-sauce-labs-backpack")).click();
        driver.findElement(By.id("shopping_cart_container")).click();
        waitVisible(By.id("checkout"));
        driver.findElement(By.id("checkout")).click();
        waitVisible(By.id("first-name"));
        mutate("first-name", "firstNameInputRefactor");
        assertFails("SCD-BL-010", "By.id: first-name", "id=firstNameInputRefactor", By.id("first-name"));
    }

    // ──────────────────────────── HELPERS ────────────────────────────

    private void loginAsStandard() {
        driver.get(BASE_URL);
        waitVisible(By.id("user-name")).sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.id("login-button")).click();
        wait.until(ExpectedConditions.urlContains("inventory"));
    }

    private void setIdByXPath(String xpath, String id) {
        WebElement el = waitVisible(By.xpath(xpath));
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
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "saucedemo", orig, mutated, status, msg});
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
