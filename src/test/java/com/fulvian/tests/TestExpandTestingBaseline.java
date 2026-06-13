package com.fulvian.tests;

import com.fulvian.base.BaseTest;
import com.fulvian.healing.AutoHealingWebDriver;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Baseline script untuk website online demo ExpandTesting.
 *
 * Fokus:
 * - Hanya menguji pemulihan locator pada halaman login.
 * - Tidak melakukan login sungguhan.
 * - Tidak memakai banyak fallback locator supaya log healing tidak meledak.
 *
 * Skenario:
 * EXP-001: username input
 * EXP-002: password input
 * EXP-003: login button
 */
public class TestExpandTestingBaseline extends BaseTest {

    private static final String DEFAULT_URL = "https://practice.expandtesting.com/login";

    private final String expandUrl = System.getProperty(
            "expandUrl",
            System.getProperty("baseUrl", DEFAULT_URL)
    );

    @Test
    void EXP_001_healUsernameInput() {
        openCleanLoginPage();

        By oldLocator = By.id("username");

        WebElement target = wait.until(
                ExpectedConditions.visibilityOfElementLocated(oldLocator)
        );

        mutateId(target, "username", "usernameInputRefactor");

        expectLocatorFailureOrAutoHealing("EXP-001", oldLocator);
    }

    @Test
    void EXP_002_healPasswordInput() {
        openCleanLoginPage();

        By oldLocator = By.id("password");

        WebElement target = wait.until(
                ExpectedConditions.visibilityOfElementLocated(oldLocator)
        );

        mutateId(target, "password", "passwordInputRefactor");

        expectLocatorFailureOrAutoHealing("EXP-002", oldLocator);
    }

    @Test
    void EXP_003_healLoginButton() {
        openCleanLoginPage();

        By oldLocator = By.id("submit-login");

        WebElement target = wait.until(
                ExpectedConditions.elementToBeClickable(oldLocator)
        );

        mutateId(target, "submit-login", "submitLoginButtonRefactor");

        expectLocatorFailureOrAutoHealing("EXP-003", oldLocator);
    }

    private void openCleanLoginPage() {
        try {
            driver.get("https://practice.expandtesting.com/logout");
            driver.manage().deleteAllCookies();
            clearBrowserStorage();
        } catch (Exception ignored) {
            // Abaikan kalau logout page tidak tersedia / belum login.
        }

        driver.get(expandUrl);

        waitUntilLoginPageReady();

        System.out.println("[EXPAND] Login page siap diuji: " + expandUrl);
    }

    private void waitUntilLoginPageReady() {
        wait.until(driverInstance -> {
            JavascriptExecutor js = (JavascriptExecutor) driverInstance;

            Object result = js.executeScript(
                    "return !!document.querySelector('#username')" +
                            " && !!document.querySelector('#password')" +
                            " && !!document.querySelector('#submit-login');"
            );

            return Boolean.TRUE.equals(result);
        });
    }

    private void clearBrowserStorage() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "window.localStorage.clear();" +
                            "window.sessionStorage.clear();"
            );
        } catch (Exception ignored) {
            // Abaikan kalau storage belum bisa diakses.
        }
    }

    private void mutateId(WebElement element, String oldId, String newId) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].setAttribute('data-self-healing-old-id', arguments[1]);" +
                        "arguments[0].setAttribute('data-self-healing-mutated-from', arguments[1]);" +
                        "arguments[0].setAttribute('id', arguments[2]);",
                element,
                oldId,
                newId
        );

        System.out.println("[EXPAND] DOM sementara dimutasi | oldId=" + oldId + " | newId=" + newId);
    }

    private void expectLocatorFailureOrAutoHealing(String testCaseId, By oldLocator) {
        boolean healingMode = Boolean.parseBoolean(
                System.getProperty("selfHealing.enabled", "false")
        );

        try {
            WebElement result = driver.findElement(oldLocator);
            boolean wasHealed = AutoHealingWebDriver.wasLastFindHealedAndReset();

            if (healingMode) {
                assertTrue(wasHealed, "[" + testCaseId + "] Elemen harus ditemukan melalui auto-healing.");
                assertTrue(result.isDisplayed(), "[" + testCaseId + "] Elemen hasil healing harus visible.");

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
}