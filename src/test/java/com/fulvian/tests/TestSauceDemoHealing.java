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
 * Self-Healing test untuk SUT SauceDemo (https://www.saucedemo.com).
 *
 * Jalankan:
 * mvn clean test -Dtest=TestSauceDemoHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("SAUCEDEMO - Self-Healing Locator Test")
public class TestSauceDemoHealing {

    private static final double THRESHOLD    = 0.50;
    private static final String SUT_LOG_FILE = "results/saucedemo_healing_log.csv";
    private static final String BASE_URL     = "https://www.saucedemo.com";
    private static final String USERNAME     = "standard_user";
    private static final String PASSWORD     = "secret_sauce";

    /** Delay antar step (ms). Atur via: -Dstep.delay.ms=2000 (default: 1500) */
    private static final long STEP_DELAY_MS =
            Long.parseLong(System.getProperty("step.delay.ms", "1500"));

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
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        js   = (JavascriptExecutor) driver;
    }

    @AfterEach
    void tearDown() { if (driver != null) driver.quit(); }

    // ──────────────────────────── HALAMAN LOGIN ────────────────────────────

    @Test @Order(1)
    @DisplayName("SCD-SH-001 Login: input username dipulihkan saat id berubah")
    void scdSh001_usernameInput() {
        step("[1/3] Membuka halaman SauceDemo...");
        driver.get(BASE_URL);
        waitVisible(By.id("user-name"));

        step("[2/3] Mensimulasikan perubahan ID elemen username...");
        mutateId("user-name", "usernameInputRefactor");

        step("[3/3] Menjalankan Self-Healing untuk memulihkan elemen...");
        WebElement el = heal("SCD-SH-001", By.id("user-name"),
                new ElementProfile("Username", "user-name", "id"));

        assertEquals("usernameInputRefactor", el.getAttribute("id"));
        el.sendKeys(USERNAME);
    }

    @Test @Order(2)
    @DisplayName("SCD-SH-002 Login: input password dipulihkan saat id berubah")
    void scdSh002_passwordInput() {
        step("[1/3] Membuka halaman SauceDemo...");
        driver.get(BASE_URL);
        waitVisible(By.id("password"));

        step("[2/3] Mensimulasikan perubahan ID elemen password...");
        mutateId("password", "passwordInputRefactor");

        step("[3/3] Menjalankan Self-Healing untuk memulihkan elemen...");
        WebElement el = heal("SCD-SH-002", By.id("password"),
                new ElementProfile("Password", "password", "id"));

        assertEquals("passwordInputRefactor", el.getAttribute("id"));
        el.sendKeys(PASSWORD);
    }

    @Test @Order(3)
    @DisplayName("SCD-SH-003 Login: tombol Login dipulihkan saat id berubah")
    void scdSh003_loginButton() {
        step("[1/3] Membuka halaman login dan mengisi kredensial...");
        driver.get(BASE_URL);
        waitVisible(By.id("user-name")).sendKeys(USERNAME);
        waitVisible(By.id("password")).sendKeys(PASSWORD);

        step("[2/3] Mensimulasikan perubahan ID tombol Login...");
        mutateId("login-button", "loginButtonRefactor");

        step("[3/3] Menjalankan Self-Healing untuk memulihkan tombol Login...");
        WebElement el = heal("SCD-SH-003", By.id("login-button"),
                new ElementProfile("Login", "login-button", "id"));

        assertEquals("loginButtonRefactor", el.getAttribute("id"));
        el.click();
        wait.until(ExpectedConditions.urlContains("inventory"));
    }

    // ──────────────────────────── HALAMAN PRODUK ────────────────────────────

    @Test @Order(4)
    @DisplayName("SCD-SH-004 Produk: judul halaman dipulihkan saat id berubah")
    void scdSh004_pageTitle() {
        step("[1/3] Login dan membuka halaman Products...");
        loginAsStandard();
        setIdByXPath("//*[contains(@class,'title') and normalize-space(.)='Products']", "productsPageTitle");

        step("[2/3] Mensimulasikan perubahan ID judul halaman Products...");
        mutateId("productsPageTitle", "productsTitleRefactor");

        step("[3/3] Menjalankan Self-Healing untuk memulihkan judul halaman...");
        WebElement el = heal("SCD-SH-004", By.id("productsPageTitle"),
                new ElementProfile("Products", "productsPageTitle", "id"));

        assertEquals("productsTitleRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(5)
    @DisplayName("SCD-SH-005 Produk: tombol Add to Cart item-1 dipulihkan")
    void scdSh005_addToCartItem1() {
        step("[1/3] Login dan membuka halaman Products...");
        loginAsStandard();

        step("[2/3] Mensimulasikan perubahan ID tombol Add to Cart item Backpack...");
        mutateId("add-to-cart-sauce-labs-backpack", "addToCartItem1Refactor");

        step("[3/3] Menjalankan Self-Healing untuk memulihkan tombol Add to Cart...");
        WebElement el = heal("SCD-SH-005", By.id("add-to-cart-sauce-labs-backpack"),
                new ElementProfile("Add to cart", "add-to-cart-sauce-labs-backpack", "id"));

        assertEquals("addToCartItem1Refactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(6)
    @DisplayName("SCD-SH-006 Produk: tombol Add to Cart item-2 dipulihkan")
    void scdSh006_addToCartItem2() {
        step("[1/3] Login dan membuka halaman Products...");
        loginAsStandard();

        step("[2/3] Mensimulasikan perubahan ID tombol Add to Cart item Bike Light...");
        mutateId("add-to-cart-sauce-labs-bike-light", "addToCartItem2Refactor");

        step("[3/3] Menjalankan Self-Healing untuk memulihkan tombol Add to Cart...");
        WebElement el = heal("SCD-SH-006", By.id("add-to-cart-sauce-labs-bike-light"),
                new ElementProfile("Add to cart", "add-to-cart-sauce-labs-bike-light", "id"));

        assertEquals("addToCartItem2Refactor", el.getAttribute("id"));
        el.click();
    }

    @Test @Order(7)
    @DisplayName("SCD-SH-007 Produk: icon keranjang dipulihkan saat id berubah")
    void scdSh007_cartIcon() {
        step("[1/3] Login dan membuka halaman Products...");
        loginAsStandard();

        step("[2/3] Mensimulasikan perubahan ID icon keranjang belanja...");
        mutateId("shopping_cart_container", "cartIconRefactor");

        step("[3/3] Menjalankan Self-Healing untuk memulihkan icon keranjang...");
        WebElement el = heal("SCD-SH-007", By.id("shopping_cart_container"),
                new ElementProfile("cart", "shopping_cart_container", "id"));

        assertEquals("cartIconRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    @Test @Order(8)
    @DisplayName("SCD-SH-008 Produk: menu hamburger dipulihkan saat id berubah")
    void scdSh008_burgerMenu() {
        step("[1/3] Login dan membuka halaman Products...");
        loginAsStandard();

        step("[2/3] Mensimulasikan perubahan ID tombol menu hamburger...");
        mutateId("react-burger-menu-btn", "burgerMenuRefactor");

        step("[3/3] Menjalankan Self-Healing untuk memulihkan tombol menu...");
        WebElement el = heal("SCD-SH-008", By.id("react-burger-menu-btn"),
                new ElementProfile("menu", "react-burger-menu-btn", "id"));

        assertEquals("burgerMenuRefactor", el.getAttribute("id"));
        assertTrue(el.isDisplayed());
    }

    // ──────────────────────────── HALAMAN CART ────────────────────────────

    @Test @Order(9)
    @DisplayName("SCD-SH-009 Cart: tombol Checkout dipulihkan saat id berubah")
    void scdSh009_checkoutButton() {
        step("[1/4] Login dan menambahkan item ke keranjang...");
        loginAsStandard();
        driver.findElement(By.id("add-to-cart-sauce-labs-backpack")).click();

        step("[2/4] Membuka halaman keranjang belanja...");
        driver.findElement(By.id("shopping_cart_container")).click();
        waitVisible(By.id("checkout"));

        step("[3/4] Mensimulasikan perubahan ID tombol Checkout...");
        mutateId("checkout", "checkoutButtonRefactor");

        step("[4/4] Menjalankan Self-Healing untuk memulihkan tombol Checkout...");
        WebElement el = heal("SCD-SH-009", By.id("checkout"),
                new ElementProfile("Checkout", "checkout", "id"));

        assertEquals("checkoutButtonRefactor", el.getAttribute("id"));
        el.click();
        wait.until(ExpectedConditions.urlContains("checkout-step-one"));
    }

    @Test @Order(10)
    @DisplayName("SCD-SH-010 Checkout: input first name dipulihkan saat id berubah")
    void scdSh010_firstNameInput() {
        step("[1/4] Login dan menambahkan item ke keranjang...");
        loginAsStandard();
        driver.findElement(By.id("add-to-cart-sauce-labs-backpack")).click();

        step("[2/4] Navigasi ke halaman Checkout Step 1...");
        driver.findElement(By.id("shopping_cart_container")).click();
        waitVisible(By.id("checkout"));
        driver.findElement(By.id("checkout")).click();
        waitVisible(By.id("first-name"));

        step("[3/4] Mensimulasikan perubahan ID input First Name...");
        mutateId("first-name", "firstNameInputRefactor");

        step("[4/4] Menjalankan Self-Healing untuk memulihkan input First Name...");
        WebElement el = heal("SCD-SH-010", By.id("first-name"),
                new ElementProfile("First Name", "first-name", "id"));

        assertEquals("firstNameInputRefactor", el.getAttribute("id"));
        el.sendKeys("coba");
    }

    // ──────────────────────────── HELPERS ────────────────────────────

    private WebElement heal(String testCaseId, By brokenLocator, ElementProfile profile) {
        System.setProperty("healing.log.file", SUT_LOG_FILE);
        // Jeda setelah mutasi locator — validator dapat melihat kondisi "locator rusak"
        sleep(1500);
        HealingDriver healingDriver = new HealingDriver(driver, testCaseId, "saucedemo_id_change", THRESHOLD);
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

    private void loginAsStandard() {
        driver.get(BASE_URL);
        waitVisible(By.id("user-name")).sendKeys(USERNAME);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.id("login-button")).click();
        wait.until(ExpectedConditions.urlContains("inventory"));
    }

    private void setIdByXPath(String xpath, String id) {
        WebElement el = waitVisible(By.xpath(xpath));
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

    /**
     * Pause antar step agar validator dapat mengamati perubahan di browser.
     * Delay dikonfigurasi via: -Dstep.delay.ms=2000 (default 1500ms)
     */
    private void step(String description) {
        System.out.println("\n▶ STEP: " + description);
        try {
            Thread.sleep(STEP_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
