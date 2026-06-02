package com.fulvian.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotHelper {

    private ScreenshotHelper() {
        // Utility class
    }

    public static void takeScreenshot(WebDriver driver, String fileName) {
        try {
            Files.createDirectories(Path.of("results/screenshots"));

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            Path destination = Path.of(
                    "results/screenshots/" + fileName + "_" + timestamp + ".png"
            );

            Files.copy(source.toPath(), destination);

            System.out.println("[ScreenshotHelper] Screenshot disimpan: " + destination);
        } catch (Exception e) {
            System.out.println("[ScreenshotHelper] Gagal mengambil screenshot: " + e.getMessage());
        }
    }
}