package com.fulvian.tests;

import com.fulvian.base.BaseTest;
import com.fulvian.healing.AutoHealingWebDriver;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Baseline script untuk website online SPOT UPI.
 *
 * Tujuan:
 * - Membuktikan framework self-healing bisa diterapkan pada website online,
 *   selama tersedia script baseline.
 * - Pengujian dibuat read-only: tidak submit data, tidak edit, tidak hapus.
 * - Perubahan locator dilakukan sementara melalui JavaScript di browser Selenium,
 *   bukan mengubah source code/server SPOT.
 *
 * Mode baseline:
 * -DselfHealing.enabled=false
 * Locator lama harus gagal setelah id dimutasi.
 *
 * Mode auto-healing:
 * -DselfHealing.enabled=true
 * Locator lama gagal, lalu AutoHealingWebDriver mengaktifkan HealingDriver.
 */
public class TestSpotUpiBaseline extends BaseTest {

    private static final String DEFAULT_SPOT_LOGIN_URL =
            "https://sso.upi.edu/cas/login?service=https%3A%2F%2Fspot.upi.edu%2Fberanda";

    private final String spotUrl = System.getProperty(
        "spotUrl",
        System.getProperty("baseUrl", DEFAULT_SPOT_LOGIN_URL)
);

private final String spotUsername = System.getProperty(
        "spotUsername",
        System.getProperty("adminEmail", "")
);

private final String spotPassword = System.getProperty(
        "spotPassword",
        System.getProperty("adminPassword", "")
);
    private final boolean manualLogin = Boolean.parseBoolean(System.getProperty("spotManualLogin", "false"));

    @Test
    void SPOT_001_healMainHeaderTitle() {
        openSpotAndLoginIfNeeded();

        WebElement target = waitVisibleAny(
                By.xpath("//*[contains(normalize-space(.),'Sistem Pembelajaran Online Terpadu')]")
        );

        prepareMutationThenFind(
                "SPOT-001",
                target,
                "spotMainHeaderTitle",
                "spotMainHeaderTitleRefactor"
        );
    }

    @Test
    void SPOT_002_healSidebarDaftarMataKuliah() {
        openSpotAndLoginIfNeeded();

        WebElement target = waitVisibleAny(
                By.xpath("//*[contains(normalize-space(.),'daftar Mata Kuliah') or contains(normalize-space(.),'Daftar Mata Kuliah')]")
        );

        prepareMutationThenFind(
                "SPOT-002",
                target,
                "spotCourseMenuLink",
                "spotCourseMenuLinkRefactor"
        );
    }

    @Test
    void SPOT_003_healCourseTableTitle() {
        openSpotAndLoginIfNeeded();

        WebElement target = waitVisibleAny(
                By.xpath("//h1[contains(normalize-space(.),'Daftar Mata Kuliah')]"),
                By.xpath("//h2[contains(normalize-space(.),'Daftar Mata Kuliah')]"),
                By.xpath("//h3[contains(normalize-space(.),'Daftar Mata Kuliah')]"),
                By.xpath("//h4[contains(normalize-space(.),'Daftar Mata Kuliah')]"),
                By.xpath("//*[contains(normalize-space(.),'Daftar Mata Kuliah')]")
        );

        prepareMutationThenFind(
                "SPOT-003",
                target,
                "spotCourseTableTitle",
                "spotCourseTableTitleRefactor"
        );
    }

    @Test
    void SPOT_004_healFirstCourseCodeCell() {
        openSpotAndLoginIfNeeded();

        WebElement target = waitVisibleAny(
                By.xpath("//table//tbody/tr[1]/td[1]"),
                By.xpath("//*[contains(normalize-space(.),'RL598')]")
        );

        prepareMutationThenFind(
                "SPOT-004",
                target,
                "spotCourseCodeCell",
                "spotCourseCodeCellRefactor"
        );
    }

    @Test
    void SPOT_005_healLogoutButton() {
        openSpotAndLoginIfNeeded();

        WebElement target = waitVisibleAny(
                By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout')]"),
                By.xpath("//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'log out')]")
        );

        prepareMutationThenFind(
                "SPOT-005",
                target,
                "spotLogoutButton",
                "spotLogoutButtonRefactor"
        );
    }

    private void openSpotAndLoginIfNeeded() {
        driver.get(spotUrl);

        sleep(1500);

        if (isSpotDashboardLoaded()) {
            System.out.println("[SPOT] User sudah berada di halaman SPOT mahasiswa.");
            return;
        }

        if (isCasLoginPage()) {
            System.out.println("[SPOT] Halaman SSO/CAS terdeteksi.");

            if (manualLogin || spotUsername.isBlank() || spotPassword.isBlank()) {
                waitForManualLogin();
            } else {
                loginWithCredentialFromSystemProperties();
            }
        }

        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(normalize-space(.),'Daftar Mata Kuliah')]")),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(normalize-space(.),'Sistem Pembelajaran Online Terpadu')]")),
                ExpectedConditions.urlContains("spot.upi.edu")
        ));

        System.out.println("[SPOT] Halaman SPOT siap diuji.");
    }

    private boolean isCasLoginPage() {
        String currentUrl = driver.getCurrentUrl().toLowerCase();
        return currentUrl.contains("sso.upi.edu") || currentUrl.contains("/cas/login");
    }

    private boolean isSpotDashboardLoaded() {
        try {
            List<WebElement> elements = driver.findElements(
                    By.xpath("//*[contains(normalize-space(.),'Daftar Mata Kuliah') or contains(normalize-space(.),'Sistem Pembelajaran Online Terpadu')]")
            );
            return !elements.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private void loginWithCredentialFromSystemProperties() {
        WebElement usernameInput = waitVisibleAny(
                By.name("username"),
                By.id("username"),
                By.cssSelector("input[accesskey='u']"),
                By.cssSelector("input[type='text']")
        );

        WebElement passwordInput = waitVisibleAny(
                By.name("password"),
                By.id("password"),
                By.cssSelector("input[accesskey='p']"),
                By.cssSelector("input[type='password']")
        );

        usernameInput.clear();
        usernameInput.sendKeys(spotUsername);

        passwordInput.clear();
        passwordInput.sendKeys(spotPassword);

        WebElement loginButton = waitClickableAny(
                By.xpath("//button[contains(translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'LOGIN')]"),
                By.xpath("//input[@type='submit' and contains(translate(@value,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'LOGIN')]"),
                By.cssSelector("button[type='submit']"),
                By.cssSelector("input[type='submit']")
        );

        loginButton.click();

        System.out.println("[SPOT] Login dikirim menggunakan credential dari system properties.");
    }

    private void waitForManualLogin() {
        System.out.println("[SPOT] Login manual aktif.");
        System.out.println("[SPOT] Silakan login di browser Selenium.");
        System.out.println("[SPOT] Test menunggu maksimal 90 detik sampai halaman SPOT terbuka.");

        wait.withTimeout(Duration.ofSeconds(90)).until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(normalize-space(.),'Daftar Mata Kuliah')]")),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(normalize-space(.),'Sistem Pembelajaran Online Terpadu')]")),
                ExpectedConditions.urlContains("spot.upi.edu")
        ));

        wait.withTimeout(Duration.ofSeconds(WAIT_TIMEOUT));
    }

    private void prepareMutationThenFind(String testCaseId, WebElement target, String oldId, String newId) {
        setTemporaryId(target, oldId);

        By oldLocator = By.id(oldId);

        wait.until(ExpectedConditions.presenceOfElementLocated(oldLocator));

        mutateId(oldId, newId);

        expectLocatorFailureOrAutoHealing(testCaseId, oldLocator);
    }

    private void setTemporaryId(WebElement element, String oldId) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].setAttribute('data-self-healing-old-id', arguments[1]);" +
                        "arguments[0].setAttribute('id', arguments[1]);",
                element,
                oldId
        );

        System.out.println("[SPOT] Temporary old id diset: " + oldId);
    }

    private void mutateId(String oldId, String newId) {
        WebElement element = driver.findElement(By.id(oldId));

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].setAttribute('data-self-healing-mutated-from', arguments[1]);" +
                        "arguments[0].setAttribute('id', arguments[2]);",
                element,
                oldId,
                newId
        );

        System.out.println("[SPOT] DOM sementara dimutasi | oldId=" + oldId + " | newId=" + newId);
    }

    private void expectLocatorFailureOrAutoHealing(String testCaseId, By oldLocator) {
        boolean healingMode = Boolean.parseBoolean(System.getProperty("selfHealing.enabled", "false"));

        try {
            WebElement result = driver.findElement(oldLocator);
            boolean wasHealed = AutoHealingWebDriver.wasLastFindHealedAndReset();

            if (healingMode) {
                assertTrue(wasHealed, "[" + testCaseId + "] Elemen harus ditemukan melalui auto-healing.");
                assertTrue(result.isDisplayed(), "[" + testCaseId + "] Elemen hasil healing harus terlihat.");

                System.out.println("[" + testCaseId + "] HEALED_BY_SELF_HEALING -> " + oldLocator);
            } else {
                fail("[" + testCaseId + "] Baseline normal seharusnya gagal setelah locator dimutasi: " + oldLocator);
            }

        } catch (NoSuchElementException e) {
            if (healingMode) {
                fail("[" + testCaseId + "] Auto-healing gagal menemukan elemen pengganti untuk locator: " + oldLocator);
            } else {
                System.out.println("[" + testCaseId + "] EXPECTED_FAIL -> locator gagal tanpa healing: " + oldLocator);
            }
        }
    }

    private WebElement waitVisibleAny(By... locators) {
        RuntimeException lastError = null;

        for (By locator : locators) {
            try {
                return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            } catch (RuntimeException e) {
                lastError = e;
            }
        }

        throw new NoSuchElementException("Tidak ada locator visible yang cocok. Last error: " +
                (lastError != null ? lastError.getMessage() : "-"));
    }

    private WebElement waitClickableAny(By... locators) {
        RuntimeException lastError = null;

        for (By locator : locators) {
            try {
                return wait.until(ExpectedConditions.elementToBeClickable(locator));
            } catch (RuntimeException e) {
                lastError = e;
            }
        }

        throw new NoSuchElementException("Tidak ada locator clickable yang cocok. Last error: " +
                (lastError != null ? lastError.getMessage() : "-"));
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}