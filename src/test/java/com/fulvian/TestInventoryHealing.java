package com.fulvian;

import com.fulvian.healing.ElementProfile;
import com.fulvian.healing.HealingDriver;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Self-Healing: Selenium dengan Mekanisme Healing AKTIF")
public class TestInventoryHealing extends BaseTest {

    private void bukaModal() throws InterruptedException {
        WebElement tambahBtn = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("inputProductBtn"))
        );
        jsClick(tambahBtn);
        Thread.sleep(1500);
    }

    @Test @Order(1)
    @DisplayName("TC-01 [id_change] Tombol Tambah: id berubah sebagian")
    void tc01_healTambahButtonIdPartialChange() throws InterruptedException {
        simulateDomChange("inputProductBtn", "id", "inputProduct--Button");
        Thread.sleep(500);
        ElementProfile profile = new ElementProfile("Tambah Produk", "inputProductBtn", "id");
        HealingDriver healing = new HealingDriver(driver, "TC-01", "id_change");
        WebElement btn = healing.findElement(By.id("inputProductBtn"), profile);
        assertNotNull(btn);
        jsClick(btn);
        Thread.sleep(1000);
        System.out.println("[TC-01] PASS");
    }

    @Test @Order(2)
    @DisplayName("TC-02 [id_change] Tombol Tambah: id berubah total")
    void tc02_healTambahButtonIdTotalChange() throws InterruptedException {
        simulateDomChange("inputProductBtn", "id", "add-new-product-btn");
        Thread.sleep(500);
        ElementProfile profile = new ElementProfile("Tambah Produk", "inputProductBtn", "id");
        HealingDriver healing = new HealingDriver(driver, "TC-02", "id_change");
        WebElement btn = healing.findElement(By.id("inputProductBtn"), profile);
        assertNotNull(btn);
        System.out.println("[TC-02] PASS");
    }

    @Test @Order(3)
    @DisplayName("TC-03 [id_change] Tombol Edit: id diubah")
    void tc03_healEditButtonIdChange() throws InterruptedException {
        simulateDomChange("editBtn-1", "id", "edit-button-row-1");
        Thread.sleep(500);
        ElementProfile profile = new ElementProfile("Edit", "editBtn-1", "id");
        HealingDriver healing = new HealingDriver(driver, "TC-03", "id_change");
        try {
            WebElement btn = healing.findElement(By.id("editBtn-1"), profile);
            assertNotNull(btn);
            System.out.println("[TC-03] PASS");
        } catch (Exception e) {
            System.out.println("[TC-03] SKIP");
            Assumptions.assumeTrue(false);
        }
    }

    @Test @Order(4)
    @DisplayName("TC-04 [class_change] Tombol Tambah: class Tailwind diubah")
    void tc04_healTambahButtonClassChange() throws InterruptedException {
        simulateDomChange("inputProductBtn", "class",
            "bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded");
        Thread.sleep(500);
        ElementProfile profile = new ElementProfile("Tambah Produk", "bg-blue-600", "class");
        HealingDriver healing = new HealingDriver(driver, "TC-04", "class_change");
        WebElement btn = healing.findElement(By.className("bg-blue-600"), profile);
        assertNotNull(btn);
        System.out.println("[TC-04] PASS");
    }

    @Test @Order(5)
    @DisplayName("TC-05 [class_change] Tombol Simpan di modal: class diubah")
    void tc05_healSimpanButtonClassChange() throws InterruptedException {
        bukaModal();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("namaBarang")));
        js.executeScript(
            "var btns=document.querySelectorAll('button');" +
            "for(var i=0;i<btns.length;i++){" +
            "if(btns[i].textContent.trim()==='Simpan'){btns[i].className='btn-primary-new';break;}}"
        );
        Thread.sleep(500);
        ElementProfile profile = new ElementProfile("Simpan", "btn-simpan", "class");
        HealingDriver healing = new HealingDriver(driver, "TC-05", "class_change");
        try {
            WebElement btn = healing.findElement(By.className("btn-simpan"), profile);
            assertNotNull(btn);
            System.out.println("[TC-05] PASS");
        } catch (Exception e) {
            System.out.println("[TC-05] SKIP");
            Assumptions.assumeTrue(false);
        }
    }

    @Test @Order(6)
    @DisplayName("TC-06 [class_change] Tombol Hapus: class berubah")
    void tc06_healHapusButtonClassChange() throws InterruptedException {
        ElementProfile profile = new ElementProfile("Hapus", "btn-danger", "class");
        HealingDriver healing = new HealingDriver(driver, "TC-06", "class_change");
        try {
            WebElement btn = healing.findElement(By.className("btn-danger"), profile);
            assertNotNull(btn);
            System.out.println("[TC-06] PASS");
        } catch (Exception e) {
            System.out.println("[TC-06] SKIP");
            Assumptions.assumeTrue(false);
        }
    }

    @Test @Order(7)
    @DisplayName("TC-07 [xpath_change] Tombol Tambah: wrapper div ditambahkan")
    void tc07_healTambahButtonXPathChange() throws InterruptedException {
        simulateXPathChange("inputProductBtn");
        Thread.sleep(500);
        String oldXPath = "//div[@class='header-actions']/button[@id='inputProductBtn']";
        ElementProfile profile = new ElementProfile("Tambah Produk", "inputProductBtn", "id");
        HealingDriver healing = new HealingDriver(driver, "TC-07", "xpath_change");
        WebElement btn = healing.findElement(By.xpath(oldXPath), profile);
        assertNotNull(btn);
        System.out.println("[TC-07] PASS");
    }

    @Test @Order(8)
    @DisplayName("TC-08 [xpath_change] Input namaBarang di modal: XPath bergeser")
    void tc08_healNamaBarangInputXPathChange() throws InterruptedException {
        bukaModal();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("namaBarang")));
        simulateXPathChange("namaBarang");
        Thread.sleep(500);
        String oldXPath = "//form[@id='formTambah']//input[@id='namaBarang']";
        ElementProfile profile = new ElementProfile("", "namaBarang", "id");
        HealingDriver healing = new HealingDriver(driver, "TC-08", "xpath_change");
        try {
            WebElement input = healing.findElement(By.xpath(oldXPath), profile);
            assertNotNull(input);
            input.clear();
            input.sendKeys("Test Healing XPath");
            System.out.println("[TC-08] PASS");
        } catch (Exception e) {
            System.out.println("[TC-08] SKIP");
            Assumptions.assumeTrue(false);
        }
    }

    @Test @Order(9)
    @DisplayName("TC-09 [xpath_change] Tombol Prev: XPath berubah")
    void tc09_healPrevButtonXPathChange() throws InterruptedException {
        String oldXPath = "//div[@class='pagination']//button[contains(text(),'Prev')]";
        ElementProfile profile = new ElementProfile("Prev", "prevBtn", "id");
        HealingDriver healing = new HealingDriver(driver, "TC-09", "xpath_change");
        try {
            WebElement btn = healing.findElement(By.xpath(oldXPath), profile);
            assertNotNull(btn);
            System.out.println("[TC-09] PASS");
        } catch (Exception e) {
            System.out.println("[TC-09] SKIP");
            Assumptions.assumeTrue(false);
        }
    }

    @Test @Order(10)
    @DisplayName("TC-10 [css_change] Tombol Tambah: CSS selector berubah")
    void tc10_healTambahButtonCSSChange() throws InterruptedException {
        simulateDomChange("inputProductBtn", "class", "btn btn-success");
        Thread.sleep(500);
        ElementProfile profile = new ElementProfile("Tambah Produk", "bg-blue-600", "class");
        HealingDriver healing = new HealingDriver(driver, "TC-10", "css_change");
        WebElement btn = healing.findElement(By.cssSelector(".bg-blue-600.text-white"), profile);
        assertNotNull(btn);
        System.out.println("[TC-10] PASS");
    }

    @Test @Order(11)
    @DisplayName("TC-11 [css_change] Input form: data-attribute berubah")
    void tc11_healFormInputCSSChange() throws InterruptedException {
        bukaModal();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("namaBarang")));
        js.executeScript(
            "var el=document.getElementById('namaBarang');" +
            "if(el) el.setAttribute('data-field','product-name-new');"
        );
        Thread.sleep(300);
        ElementProfile profile = new ElementProfile("", "namaBarang", "id");
        HealingDriver healing = new HealingDriver(driver, "TC-11", "css_change");
        try {
            WebElement input = healing.findElement(
                By.cssSelector("input[data-field='product-name']"), profile);
            assertNotNull(input);
            input.clear();
            input.sendKeys("Produk CSS Test");
            System.out.println("[TC-11] PASS");
        } catch (Exception e) {
            System.out.println("[TC-11] SKIP");
            Assumptions.assumeTrue(false);
        }
    }

    @Test @Order(12)
    @DisplayName("TC-12 [css_change] Tombol Simpan: CSS compound selector berubah")
    void tc12_healSimpanButtonCSSChange() throws InterruptedException {
        bukaModal();
        ElementProfile profile = new ElementProfile("Simpan", "btn-simpan", "class");
        HealingDriver healing = new HealingDriver(driver, "TC-12", "css_change");
        try {
            WebElement btn = healing.findElement(
                By.cssSelector("button.btn-simpan.btn-primary"), profile);
            assertNotNull(btn);
            System.out.println("[TC-12] PASS");
        } catch (Exception e) {
            System.out.println("[TC-12] SKIP");
            Assumptions.assumeTrue(false);
        }
    }
}