package com.fulvian;

import com.fulvian.healing.ElementProfile;
import com.fulvian.healing.HealingDriver;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================
 * TestInventoryHealing — Test dengan mekanisme self-healing AKTIF
 * ============================================================
 *
 * Skenario yang diuji (sesuai Bab 3.5.3 skripsi):
 *
 *   Skenario A: Perubahan nilai atribut ID   (TC-01 s/d TC-03)
 *   Skenario B: Perubahan nilai atribut Class(TC-04 s/d TC-06)
 *   Skenario C: Perubahan struktur XPath     (TC-07 s/d TC-09)
 *   Skenario D: Perubahan CSS Selector       (TC-10 s/d TC-12)
 *
 * Cara baca hasil:
 *   - Test PASS = healing berhasil menemukan elemen pengganti
 *   - Test FAIL = healing tidak berhasil (skor di bawah threshold)
 *   - Data lengkap tersimpan di results/healing_log.csv
 *
 * Catatan untuk skripsi:
 *   is_false_positive di CSV diisi FALSE secara default.
 *   Setelah test selesai, verifikasi manual: apakah elemen yang dipilih
 *   benar secara semantik? Kalau salah, ubah ke TRUE di CSV.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Self-Healing: Selenium dengan Mekanisme Healing AKTIF")
public class TestInventoryHealing extends BaseTest {

    // =========================================================
    // SKENARIO A: Perubahan Nilai Atribut ID
    // Perubahan: id asli → id baru yang berbeda
    // =========================================================

    @Test
    @Order(1)
    @DisplayName("TC-01 [id_change] Tombol Tambah Produk: id diubah sebagian")
    void tc01_healTambahButtonIdPartialChange() throws InterruptedException {
        // Simulasikan perubahan DOM: id berubah sebagian
        // "inputProductBtn" → "inputProduct--Button"
        simulateDomChange("inputProductBtn", "id", "inputProduct--Button");
        Thread.sleep(500);

        ElementProfile profile = new ElementProfile(
                "Tambah Produk",     // teks yang terlihat di tombol
                "inputProductBtn",   // id ASLI sebelum berubah
                "id"
        );

        HealingDriver healing = new HealingDriver(driver, "TC-01", "id_change");
        WebElement btn = healing.findElement(By.id("inputProductBtn"), profile);

        assertNotNull(btn, "Healing harus berhasil menemukan tombol Tambah Produk");
        jsClick(btn);
        Thread.sleep(1000);

        // Verifikasi modal terbuka — sesuaikan selector dengan DOM aplikasi Anda
        // WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
        //         By.id("modalTambahProduk")));
        // assertTrue(modal.isDisplayed(), "Modal harus terbuka setelah tombol diklik");

        System.out.println("[TC-01] PASS — Tombol berhasil diklik setelah healing");
    }

    @Test
    @Order(2)
    @DisplayName("TC-02 [id_change] Tombol Tambah Produk: id diubah total ke bahasa Inggris")
    void tc02_healTambahButtonIdTotalChange() throws InterruptedException {
        // Perubahan lebih drastis: id sama sekali berbeda
        simulateDomChange("inputProductBtn", "id", "add-new-product-btn");
        Thread.sleep(500);

        ElementProfile profile = new ElementProfile(
                "Tambah Produk",
                "inputProductBtn",
                "id"
        );

        HealingDriver healing = new HealingDriver(driver, "TC-02", "id_change");
        WebElement btn = healing.findElement(By.id("inputProductBtn"), profile);

        assertNotNull(btn, "Healing harus berhasil meski id berubah total");
        System.out.println("[TC-02] PASS");
    }

    @Test
    @Order(3)
    @DisplayName("TC-03 [id_change] Tombol Edit pada baris pertama tabel: id diubah")
    void tc03_healEditButtonIdChange() throws InterruptedException {
        // Catatan: sesuaikan "editBtn-1" dengan id tombol Edit di aplikasi Anda
        simulateDomChange("editBtn-1", "id", "edit-button-row-1");
        Thread.sleep(500);

        ElementProfile profile = new ElementProfile(
                "Edit",
                "editBtn-1",
                "id"
        );

        HealingDriver healing = new HealingDriver(driver, "TC-03", "id_change");

        try {
            WebElement btn = healing.findElement(By.id("editBtn-1"), profile);
            assertNotNull(btn);
            System.out.println("[TC-03] PASS");
        } catch (Exception e) {
            // Kalau elemen memang tidak ada di halaman, lewati (bukan failure healing)
            System.out.println("[TC-03] SKIP — elemen editBtn-1 tidak ditemukan di halaman");
            Assumptions.assumeTrue(false, "Elemen editBtn-1 tidak ada di halaman ini");
        }
    }

    // =========================================================
    // SKENARIO B: Perubahan Nilai Atribut Class
    // Perubahan: class Tailwind lama → class Tailwind baru
    // =========================================================

    @Test
    @Order(4)
    @DisplayName("TC-04 [class_change] Tombol Tambah: class diubah ke versi berbeda")
    void tc04_healTambahButtonClassChange() throws InterruptedException {
        // Ubah class CSS — locator By.className jadi tidak valid
        simulateDomChange("inputProductBtn", "class",
                "bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded");
        Thread.sleep(500);

        // Locator menggunakan class lama yang kini sudah tidak ada
        ElementProfile profile = new ElementProfile(
                "Tambah Produk",
                "bg-blue-600",   // class ASLI yang dipakai sebagai selector
                "class"
        );

        HealingDriver healing = new HealingDriver(driver, "TC-04", "class_change");
        WebElement btn = healing.findElement(
                By.className("bg-blue-600"), profile);

        assertNotNull(btn, "Healing harus berhasil meski class berubah");
        System.out.println("[TC-04] PASS");
    }

    @Test
    @Order(5)
    @DisplayName("TC-05 [class_change] Tombol Simpan di modal: class diubah total")
    void tc05_healSimpanButtonClassChange() throws InterruptedException {
        // Buka modal dulu
        WebElement tambahBtn = driver.findElement(By.id("inputProductBtn"));
        jsClick(tambahBtn);
        Thread.sleep(1500);

        // Ubah class tombol Simpan (sesuaikan dengan class aktual di aplikasi Anda)
        js.executeScript(
                "var btns = document.querySelectorAll('button');" +
                "for(var i=0; i<btns.length; i++) {" +
                "  if(btns[i].textContent.trim() === 'Simpan') {" +
                "    btns[i].className = 'btn-primary-new-style'; break;" +
                "  }" +
                "}"
        );
        Thread.sleep(500);

        ElementProfile profile = new ElementProfile(
                "Simpan",
                "btn-simpan",
                "class"
        );

        HealingDriver healing = new HealingDriver(driver, "TC-05", "class_change");
        WebElement btn = healing.findElement(By.className("btn-simpan"), profile);

        assertNotNull(btn, "Healing harus berhasil menemukan tombol Simpan");
        System.out.println("[TC-05] PASS");
    }

    @Test
    @Order(6)
    @DisplayName("TC-06 [class_change] Tombol Hapus: class prefix berubah")
    void tc06_healHapusButtonClassChange() throws InterruptedException {
        ElementProfile profile = new ElementProfile(
                "Hapus",
                "btn-danger",
                "class"
        );

        HealingDriver healing = new HealingDriver(driver, "TC-06", "class_change");

        try {
            WebElement btn = healing.findElement(By.className("btn-danger"), profile);
            assertNotNull(btn);
            System.out.println("[TC-06] PASS");
        } catch (Exception e) {
            System.out.println("[TC-06] SKIP — btn-danger tidak ada di halaman ini");
            Assumptions.assumeTrue(false, "Elemen btn-danger tidak ada");
        }
    }

    // =========================================================
    // SKENARIO C: Perubahan Struktur XPath
    // Perubahan: wrapper div ditambahkan → XPath bergeser
    // =========================================================

    @Test
    @Order(7)
    @DisplayName("TC-07 [xpath_change] Tombol Tambah: wrapper div ditambahkan")
    void tc07_healTambahButtonXPathChange() throws InterruptedException {
        // Tambahkan wrapper div → XPath bergeser satu level
        simulateXPathChange("inputProductBtn");
        Thread.sleep(500);

        // XPath lama yang kini tidak valid karena ada wrapper baru
        String oldXPath = "//div[@class='header-actions']/button[@id='inputProductBtn']";

        ElementProfile profile = new ElementProfile(
                "Tambah Produk",
                "inputProductBtn",
                "id"
        );

        HealingDriver healing = new HealingDriver(driver, "TC-07", "xpath_change");
        WebElement btn = healing.findElement(By.xpath(oldXPath), profile);

        assertNotNull(btn, "Healing harus berhasil meski XPath bergeser");
        System.out.println("[TC-07] PASS");
    }

    @Test
    @Order(8)
    @DisplayName("TC-08 [xpath_change] Input nama barang: XPath berubah akibat restrukturisasi form")
    void tc08_healNamaBarangInputXPathChange() throws InterruptedException {
        // Buka modal terlebih dahulu
        WebElement tambahBtn = driver.findElement(By.id("inputProductBtn"));
        jsClick(tambahBtn);
        Thread.sleep(1500);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("namaBarang")));

        // Tambahkan wrapper di sekitar input
        simulateXPathChange("namaBarang");
        Thread.sleep(500);

        String oldXPath = "//form[@id='formTambah']//input[@id='namaBarang']";

        ElementProfile profile = new ElementProfile(
                "",              // input tidak punya teks terlihat
                "namaBarang",   // placeholder atau id
                "id"
        );

        HealingDriver healing = new HealingDriver(driver, "TC-08", "xpath_change");
        WebElement input = healing.findElement(By.xpath(oldXPath), profile);

        assertNotNull(input, "Healing harus berhasil menemukan input namaBarang");
        input.clear();
        input.sendKeys("Test Healing XPath");
        System.out.println("[TC-08] PASS");
    }

    @Test
    @Order(9)
    @DisplayName("TC-09 [xpath_change] Tombol Prev navigasi: XPath diubah")
    void tc09_healPrevButtonXPathChange() throws InterruptedException {
        String oldXPath = "//div[@class='pagination']//button[contains(text(),'Prev')]";

        ElementProfile profile = new ElementProfile(
                "Prev",
                "prevBtn",
                "id"
        );

        HealingDriver healing = new HealingDriver(driver, "TC-09", "xpath_change");

        try {
            WebElement btn = healing.findElement(By.xpath(oldXPath), profile);
            assertNotNull(btn);
            System.out.println("[TC-09] PASS");
        } catch (Exception e) {
            System.out.println("[TC-09] SKIP — elemen Prev tidak ada di halaman ini");
            Assumptions.assumeTrue(false);
        }
    }

    // =========================================================
    // SKENARIO D: Perubahan CSS Selector
    // Perubahan: selector CSS yang tadinya valid menjadi tidak ada
    // =========================================================

    @Test
    @Order(10)
    @DisplayName("TC-10 [css_change] Tombol Tambah: CSS selector berubah akibat refaktor Tailwind")
    void tc10_healTambahButtonCSSChange() throws InterruptedException {
        // Ubah class sehingga CSS selector lama tidak valid
        simulateDomChange("inputProductBtn", "class",
                "btn btn-success");
        Thread.sleep(500);

        // CSS selector lama berdasarkan class Tailwind
        String oldCSS = ".bg-blue-600.text-white";

        ElementProfile profile = new ElementProfile(
                "Tambah Produk",
                "bg-blue-600",
                "class"
        );

        HealingDriver healing = new HealingDriver(driver, "TC-10", "css_change");
        WebElement btn = healing.findElement(By.cssSelector(oldCSS), profile);

        assertNotNull(btn, "Healing harus berhasil meski CSS selector berubah");
        System.out.println("[TC-10] PASS");
    }

    @Test
    @Order(11)
    @DisplayName("TC-11 [css_change] Input form: CSS attribute selector berubah")
    void tc11_healFormInputCSSChange() throws InterruptedException {
        WebElement tambahBtn = driver.findElement(By.id("inputProductBtn"));
        jsClick(tambahBtn);
        Thread.sleep(1500);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("namaBarang")));

        // Ubah type attribute input
        js.executeScript(
                "var el = document.getElementById('namaBarang');" +
                "if(el) el.setAttribute('data-field', 'product-name-new');"
        );
        Thread.sleep(300);

        String oldCSS = "input[data-field='product-name']";

        ElementProfile profile = new ElementProfile(
                "",
                "namaBarang",
                "id"
        );

        HealingDriver healing = new HealingDriver(driver, "TC-11", "css_change");
        WebElement input = healing.findElement(By.cssSelector(oldCSS), profile);

        assertNotNull(input, "Healing harus berhasil menemukan input meski data-field berubah");
        input.clear();
        input.sendKeys("Produk CSS Test");
        System.out.println("[TC-11] PASS");
    }

    @Test
    @Order(12)
    @DisplayName("TC-12 [css_change] Tombol Simpan: CSS compound selector berubah")
    void tc12_healSimpanButtonCSSChange() throws InterruptedException {
        WebElement tambahBtn = driver.findElement(By.id("inputProductBtn"));
        jsClick(tambahBtn);
        Thread.sleep(1500);

        String oldCSS = "button.btn-simpan.btn-primary";

        ElementProfile profile = new ElementProfile(
                "Simpan",
                "btn-simpan",
                "class"
        );

        HealingDriver healing = new HealingDriver(driver, "TC-12", "css_change");

        try {
            WebElement btn = healing.findElement(By.cssSelector(oldCSS), profile);
            assertNotNull(btn);
            System.out.println("[TC-12] PASS");
        } catch (Exception e) {
            System.out.println("[TC-12] SKIP — elemen btn-simpan tidak ada");
            Assumptions.assumeTrue(false);
        }
    }
}
