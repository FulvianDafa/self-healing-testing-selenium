package com.fulvian;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * ============================================================
 * TestInventoryBaseline — Selenium TANPA mekanisme self-healing
 * ============================================================
 *
 * Ini adalah kondisi PEMBANDING (baseline) untuk data Bab 4.
 * Skenario DOM yang sama dengan TestInventoryHealing, tapi
 * menggunakan Selenium biasa — TIDAK ada HealingDriver.
 *
 * Output: results/baseline_log.csv
 * Isi: berapa test case yang GAGAL akibat perubahan DOM
 *
 * Cara hitung metrik baseline:
 *   Failure Rate Baseline = (jumlah FAIL / total TC) × 100%
 *   Bandingkan dengan Healing Success Rate dari TestInventoryHealing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Baseline: Selenium TANPA Mekanisme Self-Healing")
public class TestInventoryBaseline extends BaseTest {

    private static final String BASELINE_FILE = "results/baseline_log.csv";
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static int totalTC   = 0;
    private static int failCount = 0;

    @BeforeAll
    static void initBaselineLog() {
        try {
            Files.createDirectories(Paths.get("results"));
            String header = "timestamp,test_case_id,scenario_type,locator,status,error_message\n";
            Files.write(Paths.get(BASELINE_FILE),
                    header.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("[Baseline] Log diinisialisasi: " + BASELINE_FILE);
        } catch (IOException e) {
            System.err.println("[Baseline] Gagal inisialisasi log: " + e.getMessage());
        }
    }

    // =========================================================
    // SKENARIO A: Perubahan ID — Baseline
    // =========================================================

    @Test @Order(1)
    @DisplayName("TC-B01 [id_change] Tombol Tambah: id berubah sebagian — tanpa healing")
    void tcB01_tambahButtonIdChange_noHealing() {
        simulateDomChange("inputProductBtn", "id", "inputProduct--Button");

        totalTC++;
        long start = System.currentTimeMillis();
        try {
            WebElement btn = driver.findElement(By.id("inputProductBtn"));
            jsClick(btn);
            logBaseline("TC-B01", "id_change", "By.id('inputProductBtn')",
                    "SUCCESS", "-", System.currentTimeMillis() - start);
        } catch (NoSuchElementException e) {
            failCount++;
            logBaseline("TC-B01", "id_change", "By.id('inputProductBtn')",
                    "FAIL", "NoSuchElementException: " + truncate(e.getMessage(), 80),
                    System.currentTimeMillis() - start);
            System.out.println("[TC-B01] FAIL — terbukti locator gagal tanpa healing ✓");
            // Tidak fail test — kita MEMANG mengharapkan ini gagal
        }
    }

    @Test @Order(2)
    @DisplayName("TC-B02 [id_change] Tombol Tambah: id berubah total — tanpa healing")
    void tcB02_tambahButtonIdTotalChange_noHealing() {
        simulateDomChange("inputProductBtn", "id", "add-new-product-btn");

        totalTC++;
        long start = System.currentTimeMillis();
        try {
            WebElement btn = driver.findElement(By.id("inputProductBtn"));
            jsClick(btn);
            logBaseline("TC-B02", "id_change", "By.id('inputProductBtn')",
                    "SUCCESS", "-", System.currentTimeMillis() - start);
        } catch (NoSuchElementException e) {
            failCount++;
            logBaseline("TC-B02", "id_change", "By.id('inputProductBtn')",
                    "FAIL", "NoSuchElementException", System.currentTimeMillis() - start);
            System.out.println("[TC-B02] FAIL — seperti yang diharapkan ✓");
        }
    }

    @Test @Order(3)
    @DisplayName("TC-B03 [id_change] Tombol Edit: id diubah — tanpa healing")
    void tcB03_editButtonIdChange_noHealing() {
        totalTC++;
        try {
            simulateDomChange("editBtn-1", "id", "edit-button-row-1");
            long start = System.currentTimeMillis();
            driver.findElement(By.id("editBtn-1"));
            logBaseline("TC-B03", "id_change", "By.id('editBtn-1')",
                    "SUCCESS", "-", System.currentTimeMillis() - start);
        } catch (NoSuchElementException e) {
            failCount++;
            logBaseline("TC-B03", "id_change", "By.id('editBtn-1')",
                    "FAIL", "NoSuchElementException", 0);
            System.out.println("[TC-B03] FAIL — seperti yang diharapkan ✓");
        }
    }

    // =========================================================
    // SKENARIO B: Perubahan Class — Baseline
    // =========================================================

    @Test @Order(4)
    @DisplayName("TC-B04 [class_change] Tombol Tambah: class diubah — tanpa healing")
    void tcB04_tambahButtonClassChange_noHealing() {
        simulateDomChange("inputProductBtn", "class",
                "bg-green-600 text-white font-bold py-2 px-4 rounded");
        totalTC++;
        long start = System.currentTimeMillis();
        try {
            driver.findElement(By.className("bg-blue-600"));
            logBaseline("TC-B04", "class_change", "By.className('bg-blue-600')",
                    "SUCCESS", "-", System.currentTimeMillis() - start);
        } catch (NoSuchElementException e) {
            failCount++;
            logBaseline("TC-B04", "class_change", "By.className('bg-blue-600')",
                    "FAIL", "NoSuchElementException", System.currentTimeMillis() - start);
            System.out.println("[TC-B04] FAIL — seperti yang diharapkan ✓");
        }
    }

    @Test @Order(5)
    @DisplayName("TC-B05 [class_change] Tombol Simpan: class diubah total — tanpa healing")
    void tcB05_simpanButtonClassChange_noHealing() throws InterruptedException {
        WebElement tambahBtn = driver.findElement(By.id("inputProductBtn"));
        jsClick(tambahBtn);
        Thread.sleep(1500);

        js.executeScript(
                "var btns = document.querySelectorAll('button');" +
                "for(var i=0; i<btns.length; i++){" +
                "  if(btns[i].textContent.trim()==='Simpan'){" +
                "    btns[i].className='btn-primary-new'; break;" +
                "  }" +
                "}"
        );

        totalTC++;
        try {
            driver.findElement(By.className("btn-simpan"));
            logBaseline("TC-B05", "class_change", "By.className('btn-simpan')",
                    "SUCCESS", "-", 0);
        } catch (NoSuchElementException e) {
            failCount++;
            logBaseline("TC-B05", "class_change", "By.className('btn-simpan')",
                    "FAIL", "NoSuchElementException", 0);
            System.out.println("[TC-B05] FAIL — seperti yang diharapkan ✓");
        }
    }

    @Test @Order(6)
    @DisplayName("TC-B06 [class_change] Tombol Hapus: class prefix berubah — tanpa healing")
    void tcB06_hapusButtonClassChange_noHealing() {
        totalTC++;
        try {
            driver.findElement(By.className("btn-danger"));
            logBaseline("TC-B06", "class_change", "By.className('btn-danger')",
                    "SUCCESS", "-", 0);
        } catch (NoSuchElementException e) {
            failCount++;
            logBaseline("TC-B06", "class_change", "By.className('btn-danger')",
                    "FAIL", "NoSuchElementException", 0);
            System.out.println("[TC-B06] FAIL — seperti yang diharapkan ✓");
        }
    }

    // =========================================================
    // SKENARIO C: Perubahan XPath — Baseline
    // =========================================================

    @Test @Order(7)
    @DisplayName("TC-B07 [xpath_change] Tombol Tambah: wrapper ditambah — tanpa healing")
    void tcB07_tambahButtonXPathChange_noHealing() throws InterruptedException {
        simulateXPathChange("inputProductBtn");
        Thread.sleep(300);

        String oldXPath = "//div[@class='header-actions']/button[@id='inputProductBtn']";
        totalTC++;
        long start = System.currentTimeMillis();
        try {
            driver.findElement(By.xpath(oldXPath));
            logBaseline("TC-B07", "xpath_change", "By.xpath(oldXPath)",
                    "SUCCESS", "-", System.currentTimeMillis() - start);
        } catch (NoSuchElementException e) {
            failCount++;
            logBaseline("TC-B07", "xpath_change", "By.xpath(oldXPath)",
                    "FAIL", "NoSuchElementException", System.currentTimeMillis() - start);
            System.out.println("[TC-B07] FAIL — seperti yang diharapkan ✓");
        }
    }

    @Test @Order(8)
    @DisplayName("TC-B08 [xpath_change] Input namaBarang: XPath bergeser — tanpa healing")
    void tcB08_namaBarangXPathChange_noHealing() throws InterruptedException {
        WebElement tambahBtn = driver.findElement(By.id("inputProductBtn"));
        jsClick(tambahBtn);
        Thread.sleep(1500);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("namaBarang")));

        simulateXPathChange("namaBarang");
        Thread.sleep(300);

        String oldXPath = "//form[@id='formTambah']//input[@id='namaBarang']";
        totalTC++;
        try {
            driver.findElement(By.xpath(oldXPath));
            logBaseline("TC-B08", "xpath_change", "By.xpath(oldXPath)",
                    "SUCCESS", "-", 0);
        } catch (NoSuchElementException e) {
            failCount++;
            logBaseline("TC-B08", "xpath_change", "By.xpath(oldXPath)",
                    "FAIL", "NoSuchElementException", 0);
            System.out.println("[TC-B08] FAIL — seperti yang diharapkan ✓");
        }
    }

    @Test @Order(9)
    @DisplayName("TC-B09 [xpath_change] Tombol Prev: XPath berubah — tanpa healing")
    void tcB09_prevButtonXPathChange_noHealing() {
        String oldXPath = "//div[@class='pagination']//button[contains(text(),'Prev')]";
        totalTC++;
        try {
            driver.findElement(By.xpath(oldXPath));
            logBaseline("TC-B09", "xpath_change", "By.xpath(oldXPath)",
                    "SUCCESS", "-", 0);
        } catch (NoSuchElementException e) {
            failCount++;
            logBaseline("TC-B09", "xpath_change", "By.xpath(oldXPath)",
                    "FAIL", "NoSuchElementException", 0);
            System.out.println("[TC-B09] FAIL — seperti yang diharapkan ✓");
        }
    }

    // =========================================================
    // SKENARIO D: Perubahan CSS Selector — Baseline
    // =========================================================

    @Test @Order(10)
    @DisplayName("TC-B10 [css_change] Tombol Tambah: CSS selector berubah — tanpa healing")
    void tcB10_tambahButtonCSSChange_noHealing() throws InterruptedException {
        simulateDomChange("inputProductBtn", "class", "btn btn-success");
        Thread.sleep(300);

        totalTC++;
        try {
            driver.findElement(By.cssSelector(".bg-blue-600.text-white"));
            logBaseline("TC-B10", "css_change", ".bg-blue-600.text-white",
                    "SUCCESS", "-", 0);
        } catch (NoSuchElementException e) {
            failCount++;
            logBaseline("TC-B10", "css_change", ".bg-blue-600.text-white",
                    "FAIL", "NoSuchElementException", 0);
            System.out.println("[TC-B10] FAIL — seperti yang diharapkan ✓");
        }
    }

    @Test @Order(11)
    @DisplayName("TC-B11 [css_change] Input form: data-attribute berubah — tanpa healing")
    void tcB11_inputCSSChange_noHealing() throws InterruptedException {
        WebElement tambahBtn = driver.findElement(By.id("inputProductBtn"));
        jsClick(tambahBtn);
        Thread.sleep(1500);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("namaBarang")));

        js.executeScript(
                "var el = document.getElementById('namaBarang');" +
                "if(el) el.setAttribute('data-field','product-name-new');"
        );

        totalTC++;
        try {
            driver.findElement(By.cssSelector("input[data-field='product-name']"));
            logBaseline("TC-B11", "css_change", "input[data-field='product-name']",
                    "SUCCESS", "-", 0);
        } catch (NoSuchElementException e) {
            failCount++;
            logBaseline("TC-B11", "css_change", "input[data-field='product-name']",
                    "FAIL", "NoSuchElementException", 0);
            System.out.println("[TC-B11] FAIL — seperti yang diharapkan ✓");
        }
    }

    @Test @Order(12)
    @DisplayName("TC-B12 [css_change] Tombol Simpan: compound CSS selector berubah — tanpa healing")
    void tcB12_simpanButtonCSSChange_noHealing() throws InterruptedException {
        WebElement tambahBtn = driver.findElement(By.id("inputProductBtn"));
        jsClick(tambahBtn);
        Thread.sleep(1500);

        totalTC++;
        try {
            driver.findElement(By.cssSelector("button.btn-simpan.btn-primary"));
            logBaseline("TC-B12", "css_change", "button.btn-simpan.btn-primary",
                    "SUCCESS", "-", 0);
        } catch (NoSuchElementException e) {
            failCount++;
            logBaseline("TC-B12", "css_change", "button.btn-simpan.btn-primary",
                    "FAIL", "NoSuchElementException", 0);
            System.out.println("[TC-B12] FAIL — seperti yang diharapkan ✓");
        }
    }

    // =========================================================
    // RINGKASAN BASELINE — ditampilkan setelah semua test selesai
    // =========================================================

    @AfterAll
    static void printBaselineSummary() {
        String sep = "─".repeat(70);
        System.out.println("\n" + sep);
        System.out.println("  RINGKASAN BASELINE (Tanpa Self-Healing)");
        System.out.println(sep);
        System.out.printf("  Total test case    : %d%n", totalTC);
        System.out.printf("  Gagal (FAIL)       : %d%n", failCount);
        System.out.printf("  Berhasil (SUCCESS) : %d%n", totalTC - failCount);
        System.out.printf("  Failure Rate       : %.2f%%%n",
                totalTC > 0 ? failCount * 100.0 / totalTC : 0);
        System.out.println(sep);
        System.out.println("  Data: " + Paths.get(BASELINE_FILE).toAbsolutePath());
        System.out.println("  Bandingkan Failure Rate ini dengan Healing Success Rate");
        System.out.println("  dari TestInventoryHealing di Bab 4 skripsi.");
        System.out.println(sep + "\n");
    }

    // -------------------------------------------------------
    // HELPER: tulis satu baris ke baseline CSV
    // -------------------------------------------------------
    private static synchronized void logBaseline(String tcId, String scenario,
                                                  String locator, String status,
                                                  String errorMsg, long timeMs) {
        String row = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                LocalDateTime.now().format(FMT),
                tcId, scenario, locator, status,
                errorMsg.replace("\"", "'"));
        try {
            Files.write(Paths.get(BASELINE_FILE), row.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            System.err.println("[Baseline] Gagal menulis log: " + e.getMessage());
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
