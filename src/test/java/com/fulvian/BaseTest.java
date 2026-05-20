package com.fulvian;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * BaseTest — Setup dan teardown yang diwarisi semua test class.
 *
 * Semua test class (TestInventoryHealing, TestInventoryBaseline)
 * extends class ini agar tidak perlu setup WebDriver berulang.
 */
public abstract class BaseTest {

    protected static WebDriver     driver;
    protected static WebDriverWait wait;
    protected static JavascriptExecutor js;

    // URL aplikasi web yang diuji — sesuaikan dengan environment Anda
    protected static final String BASE_URL = "http://anugrah_jaya.test/app/index.html";

    // Timeout WebDriverWait dalam detik
    protected static final int WAIT_TIMEOUT = 10;

    @BeforeAll
    static void setUpDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        // Hilangkan comment di bawah jika ingin headless (tanpa tampilan browser):
        // options.addArguments("--headless=new");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-notifications");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));

        wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT));
        js   = (JavascriptExecutor) driver;

        System.out.println("[BaseTest] WebDriver siap.");
    }

    @BeforeEach
    void navigateToApp() throws InterruptedException {
        driver.get(BASE_URL);
        // Tunggu halaman stabil sebelum test dimulai
        Thread.sleep(1500);
    }

    @AfterEach
    void takeBreath() throws InterruptedException {
        // Jeda singkat antar test agar DOM stabil
        Thread.sleep(500);
    }

    @AfterAll
    static void tearDown() {
        // Cetak ringkasan metrik healing setelah semua test selesai
        com.fulvian.healing.HealingLogger.printSummary();

        if (driver != null) {
            driver.quit();
            System.out.println("[BaseTest] Browser ditutup.");
        }
    }

    // -------------------------------------------------------
    // UTILITY METHODS — tersedia untuk semua subclass
    // -------------------------------------------------------

    /** Klik elemen via JavaScript — lebih andal dari .click() langsung. */
    protected void jsClick(org.openqa.selenium.WebElement el) {
        js.executeScript("arguments[0].click();", el);
    }

    /** Scroll ke elemen agar terlihat di viewport. */
    protected void scrollTo(org.openqa.selenium.WebElement el) {
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
    }

    /**
     * Simulasikan perubahan DOM via JavaScript.
     * Dipakai untuk mengubah atribut elemen SEBELUM locator asli dicari.
     *
     * Contoh:
     *   simulateDomChange("inputProductBtn", "id", "inputProduct--Button");
     *
     * @param elementId   id elemen yang akan diubah atributnya
     * @param attribute   nama atribut yang diubah ("id", "class", dll)
     * @param newValue    nilai baru atribut tersebut
     */
    protected void simulateDomChange(String elementId,
                                     String attribute,
                                     String newValue) {
        String script = String.format(
                "var el = document.getElementById('%s'); " +
                "if(el) { el.setAttribute('%s', '%s'); " +
                "  console.log('DOM diubah: %s.%s → %s'); }",
                elementId, attribute, newValue,
                elementId, attribute, newValue
        );
        js.executeScript(script);
        System.out.printf("[BaseTest] DOM diubah: id='%s' → %s='%s'%n",
                elementId, attribute, newValue);
    }

    /**
     * Simulasikan perubahan class elemen.
     *
     * @param elementId   id elemen yang akan diubah classnya
     * @param newClass    nilai class baru (menggantikan seluruh class lama)
     */
    protected void simulateClassChange(String elementId, String newClass) {
        simulateDomChange(elementId, "class", newClass);
    }

    /**
     * Simulasikan penambahan wrapper div — menggeser XPath elemen.
     *
     * @param elementId   id elemen yang akan dibungkus wrapper baru
     */
    protected void simulateXPathChange(String elementId) {
        String script = String.format(
                "var el = document.getElementById('%s');" +
                "if(el) {" +
                "  var wrapper = document.createElement('div');" +
                "  wrapper.className = 'xpath-wrapper-injected';" +
                "  el.parentNode.insertBefore(wrapper, el);" +
                "  wrapper.appendChild(el);" +
                "  console.log('XPath diubah untuk: %s');" +
                "}",
                elementId, elementId
        );
        js.executeScript(script);
        System.out.printf("[BaseTest] XPath diubah: wrapper div ditambahkan di atas '%s'%n",
                elementId);
    }
}