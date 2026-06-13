package com.fulvian.base;

import com.fulvian.healing.AutoHealingWebDriver;
import com.fulvian.healing.HealingLogger;
import com.fulvian.utils.DomMutationHelper;
import com.fulvian.utils.WaitHelper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * BaseTest untuk semua test Selenium.
 *
 * Catatan penting:
 * - Jangan menunggu id tertentu di @BeforeEach, karena penelitian self-healing
 *   memang sengaja menguji kondisi locator berubah/rusak.
 * - baseUrl bisa diganti dari command line:
 *   mvn test -DbaseUrl=http://anugrah_jaya.test/app/index.html
 *
 * Mode Self-Healing:
 * - Jika -DselfHealing.enabled=true, driver dibungkus AutoHealingWebDriver.
 * - Semua panggilan driver.findElement(By...) otomatis masuk mekanisme healing
 *   tanpa perlu mengubah code di baseline test.
 */
public abstract class BaseTest {

    protected static WebDriver driver;
    protected static WebDriverWait wait;
    protected static JavascriptExecutor js;

    protected DomMutationHelper domMutation;
    protected WaitHelper waitHelper;

    protected static final String BASE_URL = System.getProperty(
            "baseUrl",
            "http://anugrah_jaya.test/app/index.html"
    );

    protected static final int WAIT_TIMEOUT = 10;

    @BeforeAll
    static void setUpDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(System.getProperty("headless", "false"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");

        // Buat raw driver dulu
        WebDriver rawDriver = new ChromeDriver(options);
        rawDriver.manage().window().maximize();

        // Implicit wait dimatikan agar waktu healing tidak bias.
        rawDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));

        // Cek flag self-healing
        boolean selfHealingEnabled = Boolean.parseBoolean(
                System.getProperty("selfHealing.enabled", "false")
        );
        double threshold = Double.parseDouble(
                System.getProperty("healing.threshold", "0.50")
        );

        if (selfHealingEnabled) {
            driver = new AutoHealingWebDriver(rawDriver, true, threshold);
            System.out.println("[BaseTest] WebDriver siap dalam mode AUTO SELF-HEALING");
        } else {
            driver = rawDriver;
            System.out.println("[BaseTest] WebDriver siap dalam mode BASELINE/NORMAL");
        }

        wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT));
        js = (JavascriptExecutor) driver;
    }

    @BeforeEach
    void navigateToApp() {
        driver.get(BASE_URL);

        domMutation = new DomMutationHelper(driver);
        waitHelper = new WaitHelper(wait);

        waitHelper.waitUntilPresent(By.tagName("body"));
        System.out.println("[BaseTest] Halaman SUT dimuat: " + BASE_URL);
    }

    @AfterEach
    void afterEachTest() {
        System.out.println("[BaseTest] Test selesai, halaman akan di-reset pada test berikutnya.");
    }

    @AfterAll
    static void tearDown() {
        // Summary self-healing hanya dicetak saat mode self-healing aktif
        boolean selfHealingEnabled = Boolean.parseBoolean(
                System.getProperty("selfHealing.enabled", "false")
        );
        if (selfHealingEnabled) {
            HealingLogger.printSummary();
        }

        if (driver != null) {
            driver.quit();
            System.out.println("[BaseTest] Browser ditutup.");
        }
    }

    // -------------------------------------------------------
    // PAGE HELPERS
    // -------------------------------------------------------

    protected void openIndexPage() {
        driver.get(BASE_URL);
        waitHelper.waitUntilPresent(By.tagName("body"));
    }

    protected void openPage(String pageFileName) {
        driver.get(resolvePageUrl(pageFileName));
        waitHelper.waitUntilPresent(By.tagName("body"));
        System.out.println("[BaseTest] Pindah halaman: " + pageFileName);
    }

    protected String resolvePageUrl(String pageFileName) {
        int lastSlash = BASE_URL.lastIndexOf('/');
        if (lastSlash < 0) {
            return pageFileName;
        }
        return BASE_URL.substring(0, lastSlash + 1) + pageFileName;
    }

    protected boolean isDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean hasClass(WebElement element, String className) {
        String classes = element.getAttribute("class");
        return classes != null && classes.contains(className);
    }

    // -------------------------------------------------------
    // COMMON SELENIUM HELPERS
    // -------------------------------------------------------

    protected void jsClick(WebElement element) {
        js.executeScript("arguments[0].click();", element);
    }

    protected void scrollTo(WebElement element) {
        js.executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'nearest'});",
                element
        );
    }

    protected void reloadApp() {
        driver.get(driver.getCurrentUrl());
        waitHelper.waitUntilPresent(By.tagName("body"));
        System.out.println("[BaseTest] Halaman SUT di-reload manual.");
    }

    protected WebElement ensureElementPresent(By locator) {
        return waitHelper.waitUntilPresent(locator);
    }

    protected WebElement ensureElementClickable(By locator) {
        return waitHelper.waitUntilClickable(locator);
    }

    // -------------------------------------------------------
    // DOM MUTATION HELPERS
    // -------------------------------------------------------

    protected void simulateDomChange(String elementId, String attribute, String newValue) {
        domMutation.changeAttributeById(elementId, attribute, newValue);
    }

    protected void simulateIdChange(String oldId, String newId) {
        domMutation.changeId(oldId, newId);
    }

    protected void simulateClassChange(String elementId, String newClass) {
        domMutation.changeClassById(elementId, newClass);
    }

    protected void simulateXPathChange(String elementId) {
        domMutation.wrapElementById(elementId, "xpath-wrapper-injected");
    }

    protected void simulateRemoveAttribute(String elementId, String attributeName) {
        domMutation.removeAttributeById(elementId, attributeName);
    }
}
