package com.fulvian.tests;

import com.fulvian.healing.ElementProfile;
import com.fulvian.healing.HealingDriver;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.openqa.selenium.NoSuchElementException;

class TestLocalDemoHealing {

    @Test
    void recoversChangedLocatorWithoutExternalWebsite() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--disable-gpu");
        WebDriver driver = new ChromeDriver(options);
        try {
            String fixtureUrl = Path.of("src", "test", "resources", "demo", "self-healing.html")
                    .toAbsolutePath().toUri().toString();
            driver.get(fixtureUrl);

            WebElement original = driver.findElement(By.id("saveBtn"));
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].id = 'saveBtnRefactor';", original);

            assertThrows(NoSuchElementException.class,
                    () -> driver.findElement(By.id("saveBtn")));

            HealingDriver healing = new HealingDriver(driver, "DEMO-001", "id_change", 0.50);
            ElementProfile profile = new ElementProfile("Save", "saveBtn", "id");
            WebElement recovered = healing.findElement(By.id("saveBtn"), profile);

            assertEquals("saveBtnRefactor", recovered.getAttribute("id"));
            recovered.click();
            assertEquals("Saved", driver.findElement(By.id("status")).getText());
        } finally {
            driver.quit();
        }
    }
}
