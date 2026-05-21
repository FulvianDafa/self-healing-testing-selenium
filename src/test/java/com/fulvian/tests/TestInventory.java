package com.fulvian.tests;


import com.fulvian.healing.HealingDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;

import java.time.Duration;

public class TestInventory {

    public static void main(String[] args) {

        WebDriverManager.chromedriver().setup();

        WebDriver driver = new ChromeDriver();

        try {

            // buka browser
            driver.manage().window().maximize();

            // buka website
            driver.get("http://anugrah_jaya.test/app/index.html");

            System.out.println("Website berhasil dibuka");

            // tunggu sebentar
            Thread.sleep(2000);

            // aktifkan self-healing
            HealingDriver healingDriver =
                    new HealingDriver(driver);

            // cari tombol menggunakan healing
            WebElement tambahButton =
                    healingDriver.findElement(
                            By.id("inputProductBtn"),
                            "Tambah Produk"
                    );

            // klik tombol tambah
            JavascriptExecutor js =
        (JavascriptExecutor) driver;

js.executeScript(
        "arguments[0].click();",
        tambahButton
);

            System.out.println("Modal berhasil dibuka");
            Thread.sleep(3000);
            // wait sampai input muncul
            WebDriverWait wait =
                    new WebDriverWait(
                            driver,
                            Duration.ofSeconds(10)
                    );

            wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                        By.id("namaBarang")
                    )
            );

            // isi nama barang
            WebElement namaBarang =
                    driver.findElement(
                            By.id("namaBarang")
                    );

            namaBarang.sendKeys("Ini Produk Selenium");

            System.out.println("Input berhasil");

            // cari tombol simpan
            WebElement simpanButton =
                    driver.findElement(
                            By.xpath(
                                    "//button[contains(text(),'Simpan')]"
                            )
                    );

            // klik simpan
            simpanButton.click();

            System.out.println("Data berhasil disimpan");

            // tunggu biar keliatan hasilnya
            Thread.sleep(10000);

        } catch (Exception e) {

            System.out.println("Terjadi error:");
            e.printStackTrace();

        } finally {

            // sementara browser jangan ditutup dulu
             driver.quit();

        }
    }
}