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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("BING - Baseline Test")
public class TestBingBaseline {

    private static final Path LOG_FILE = Paths.get("results", "bing_baseline_log.csv");
    private static final List<String[]> LOGS = new ArrayList<>();
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

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
    }

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--start-maximized");
        driver = new ChromeDriver(opts);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        js = (JavascriptExecutor) driver;
    }

    @AfterEach
    void tearDown() { if (driver != null) driver.quit(); }

    private void handleCookiePopup() {
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
            List<WebElement> acceptButtons = driver.findElements(By.xpath(
                "//button[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept') or @id='bnp_btn_accept']"
            ));
            for (WebElement btn : acceptButtons) {
                if (btn.isDisplayed()) {
                    btn.click();
                    break;
                }
            }
        } catch (Exception ignored) {
        } finally {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Test @Order(1)
    @DisplayName("BING-BL-001 Bing: input pencarian gagal saat id berubah")
    void bingBl001_searchInput() {
        driver.get("https://www.bing.com");
        handleCookiePopup();
        WebElement orig = waitVisible(By.name("q"));
        setId(orig, "bingSearchInput");
        mutate("bingSearchInput", "bingSearchInputRefactor");
        assertFails("BING-BL-001", "By.id: bingSearchInput", "id=bingSearchInputRefactor", By.id("bingSearchInput"));
    }

    @Test @Order(2)
    @DisplayName("BING-BL-002 Bing: tombol pencarian gagal saat id berubah")
    void bingBl002_searchButton() {
        driver.get("https://www.bing.com");
        handleCookiePopup();
        WebElement orig = waitVisible(By.xpath("//label[@id='search_icon' or contains(@class, 'search')]"));
        setId(orig, "bingSearchButton");
        mutate("bingSearchButton", "bingSearchButtonRefactor");
        assertFails("BING-BL-002", "By.id: bingSearchButton", "id=bingSearchButtonRefactor", By.id("bingSearchButton"));
    }

    @Test @Order(3)
    @DisplayName("BING-BL-003 Bing: link Images gagal saat id berubah")
    void bingBl003_imagesLink() {
        driver.get("https://www.bing.com");
        handleCookiePopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(text(), 'Images') or contains(text(), 'Gambar')]"));
        setId(orig, "bingImagesLink");
        mutate("bingImagesLink", "bingImagesLinkRefactor");
        assertFails("BING-BL-003", "By.id: bingImagesLink", "id=bingImagesLinkRefactor", By.id("bingImagesLink"));
    }

    @Test @Order(4)
    @DisplayName("BING-BL-004 Bing: link Videos gagal saat id berubah")
    void bingBl004_videosLink() {
        driver.get("https://www.bing.com");
        handleCookiePopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(text(), 'Videos') or contains(text(), 'Video')]"));
        setId(orig, "bingVideosLink");
        mutate("bingVideosLink", "bingVideosLinkRefactor");
        assertFails("BING-BL-004", "By.id: bingVideosLink", "id=bingVideosLinkRefactor", By.id("bingVideosLink"));
    }

    @Test @Order(5)
    @DisplayName("BING-BL-005 Bing: link Maps gagal saat id berubah")
    void bingBl005_mapsLink() {
        driver.get("https://www.bing.com");
        handleCookiePopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(text(), 'Maps') or contains(text(), 'Peta')]"));
        setId(orig, "bingMapsLink");
        mutate("bingMapsLink", "bingMapsLinkRefactor");
        assertFails("BING-BL-005", "By.id: bingMapsLink", "id=bingMapsLinkRefactor", By.id("bingMapsLink"));
    }

    @Test @Order(6)
    @DisplayName("BING-BL-006 Bing: link News gagal saat id berubah")
    void bingBl006_newsLink() {
        driver.get("https://www.bing.com");
        handleCookiePopup();
        WebElement orig = waitVisible(By.xpath("//a[contains(text(), 'News') or contains(text(), 'Berita')]"));
        setId(orig, "bingNewsLink");
        mutate("bingNewsLink", "bingNewsLinkRefactor");
        assertFails("BING-BL-006", "By.id: bingNewsLink", "id=bingNewsLinkRefactor", By.id("bingNewsLink"));
    }

    @Test @Order(7)
    @DisplayName("BING-BL-007 Bing: icon Mic gagal saat id berubah")
    void bingBl007_micIcon() {
        driver.get("https://www.bing.com");
        handleCookiePopup();
        WebElement orig = waitVisible(By.xpath("//*[@id='sb_form_mic' or @aria-label='Search using voice' or contains(@class,'mic')]"));
        setId(orig, "bingMicIcon");
        mutate("bingMicIcon", "bingMicIconRefactor");
        assertFails("BING-BL-007", "By.id: bingMicIcon", "id=bingMicIconRefactor", By.id("bingMicIcon"));
    }

    @Test @Order(8)
    @DisplayName("BING-BL-008 Bing: icon Camera gagal saat id berubah")
    void bingBl008_cameraIcon() {
        driver.get("https://www.bing.com");
        handleCookiePopup();
        WebElement orig = waitVisible(By.xpath("//*[@id='sb_form_cb' or @aria-label='Search using an image' or contains(@class,'camera')]"));
        setId(orig, "bingCameraIcon");
        mutate("bingCameraIcon", "bingCameraIconRefactor");
        assertFails("BING-BL-008", "By.id: bingCameraIcon", "id=bingCameraIconRefactor", By.id("bingCameraIcon"));
    }

    @Test @Order(9)
    @DisplayName("BING-BL-009 Bing: tombol Settings gagal saat id berubah")
    void bingBl009_settingsMenu() {
        driver.get("https://www.bing.com");
        handleCookiePopup();
        WebElement orig = waitVisible(By.xpath("//*[@id='id_sc' or @aria-label='Settings']"));
        setId(orig, "bingSettingsMenu");
        mutate("bingSettingsMenu", "bingSettingsMenuRefactor");
        assertFails("BING-BL-009", "By.id: bingSettingsMenu", "id=bingSettingsMenuRefactor", By.id("bingSettingsMenu"));
    }

    @Test @Order(10)
    @DisplayName("BING-BL-010 Bing: icon Logo gagal saat id berubah")
    void bingBl010_logoIcon() {
        driver.get("https://www.bing.com");
        handleCookiePopup();
        WebElement orig = waitVisible(By.xpath("//*[@id='b_logo' or contains(@class, 'logo')]"));
        setId(orig, "bingLogoIcon");
        mutate("bingLogoIcon", "bingLogoIconRefactor");
        assertFails("BING-BL-010", "By.id: bingLogoIcon", "id=bingLogoIconRefactor", By.id("bingLogoIcon"));
    }

    private void setId(WebElement el, String id) {
        js.executeScript("arguments[0].setAttribute('id', arguments[1]);", el, id);
    }
    private void mutate(String oldId, String newId) {
        Object r = js.executeScript(
                "const el=document.getElementById(arguments[0]);" +
                "if(!el) return 0;" +
                "el.setAttribute('data-self-healing-old-id',arguments[0]);" +
                "el.setAttribute('id',arguments[1]); return 1;", oldId, newId);
        assertEquals(1L, ((Number) r).longValue(), "Elemen tidak ditemukan saat mutasi: " + oldId);
    }
    private void assertFails(String id, String orig, String mutated, By locator) {
        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> driver.findElement(locator));
        log(id, orig, mutated, "LOCATOR_FAILED_EXPECTED", ex.getClass().getSimpleName());
    }
    private void log(String id, String orig, String mutated, String status, String msg) {
        LOGS.add(new String[]{LocalDateTime.now().toString(), id, "bing", orig, mutated, status, msg});
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
