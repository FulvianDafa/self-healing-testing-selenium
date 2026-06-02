package com.fulvian.tests;

import com.fulvian.base.BaseTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Baseline pembanding untuk Bab 4.
 *
 * Test ini sengaja TIDAK memakai HealingDriver.
 * Tujuannya menunjukkan bahwa locator lama gagal ketika DOM/id berubah.
 * Hasil baseline ditulis ke results/anugrah_baseline_log.csv.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ANUGRAH_JAYA - Baseline Tanpa Self-Healing")
public class TestAnugrahJayaBaseline extends BaseTest {

    private static final String BASELINE_FILE = "results/anugrah_baseline_log.csv";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static int totalTC = 0;
    private static int failCount = 0;
    private static int successCount = 0;

    @BeforeAll
    static void initBaselineLog() {
        try {
            Files.createDirectories(Paths.get("results"));
            String header = "timestamp,test_case_id,page,scenario_type,locator,status,error_message\n";
            Files.write(Paths.get(BASELINE_FILE), header.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("[Baseline] Log diinisialisasi: " + BASELINE_FILE);
        } catch (IOException e) {
            System.err.println("[Baseline] Gagal inisialisasi log: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    @DisplayName("BL-001 Produk: locator lama tombol Tambah Produk gagal tanpa healing")
    void bl001_addProductOldLocatorFails() {
        expectLocatorFailure("BL-001", "index.html", "id_change", By.id("inputProductBtn"));
    }

    @Test
    @Order(2)
    @DisplayName("BL-004 Produk: input Nama Barang gagal setelah id berubah tanpa healing")
    void bl004_namaBarangChangedIdFails() {
        openProductModalUsingCurrentLocator();
        simulateIdChange("namaBarang", "inputNamaBarangRefactor");
        expectLocatorFailure("BL-004", "index.html", "id_change", By.id("namaBarang"));
    }

    @Test
    @Order(3)
    @DisplayName("BL-010 Produk: tombol Simpan gagal setelah id berubah tanpa healing")
    void bl010_saveButtonChangedIdFails() {
        openProductModalUsingCurrentLocator();
        simulateIdChange("saveBtn", "saveProductButtonRefactor");
        expectLocatorFailure("BL-010", "index.html", "id_change", By.id("saveBtn"));
    }

    @Test
    @Order(4)
    @DisplayName("BL-020 Transaksi: search input gagal setelah id berubah tanpa healing")
    void bl020_transactionSearchChangedIdFails() {
        openPage("transaction_page.html");
        simulateIdChange("searchInput", "transactionSearchInputRefactor");
        expectLocatorFailure("BL-020", "transaction_page.html", "id_change", By.id("searchInput"));
    }

    @Test
    @Order(5)
    @DisplayName("BL-021 Transaksi: tombol Simpan gagal setelah id berubah tanpa healing")
    void bl021_transactionSaveChangedIdFails() {
        openPage("transaction_page.html");
        simulateIdChange("simpanTransactionBtn", "saveTransactionButtonRefactor");
        expectLocatorFailure("BL-021", "transaction_page.html", "id_change", By.id("simpanTransactionBtn"));
    }

    @Test
    @Order(6)
    @DisplayName("BL-030 Utang: tombol Tambah Pengutang gagal setelah id berubah tanpa healing")
    void bl030_addDebtorChangedIdFails() {
        openPage("debt_page.html");
        simulateIdChange("addDebtorBtn", "addDebtorButtonRefactor");
        expectLocatorFailure("BL-030", "debt_page.html", "id_change", By.id("addDebtorBtn"));
    }

    @Test
    @Order(7)
    @DisplayName("BL-040 Dashboard: tab Data Utang gagal setelah id berubah tanpa healing")
    void bl040_dashboardDebtTabChangedIdFails() {
        openPage("dashboard.html");
        simulateIdChange("tab-debt", "tabDebtRefactor");
        expectLocatorFailure("BL-040", "dashboard.html", "id_change", By.id("tab-debt"));
    }

    @Test
    @Order(8)
    @DisplayName("BL-050 Print Daftar Barang: search input gagal setelah id berubah tanpa healing")
    void bl050_printSearchChangedIdFails() {
        openPage("print_daftar_barang.html");
        simulateIdChange("searchInput", "printSearchInputRefactor");
        expectLocatorFailure("BL-050", "print_daftar_barang.html", "id_change", By.id("searchInput"));
    }

    @AfterAll
    static void printBaselineSummary() {
        String sep = "-".repeat(70);
        System.out.println("\n" + sep);
        System.out.println("  RINGKASAN BASELINE ANUGRAH_JAYA (Tanpa Self-Healing)");
        System.out.println(sep);
        System.out.printf("  Total test case    : %d%n", totalTC);
        System.out.printf("  Gagal locator      : %d%n", failCount);
        System.out.printf("  Masih ditemukan    : %d%n", successCount);
        System.out.printf("  Failure Rate       : %.2f%%%n", totalTC > 0 ? failCount * 100.0 / totalTC : 0);
        System.out.println("  Data               : " + Paths.get(BASELINE_FILE).toAbsolutePath());
        System.out.println(sep + "\n");
    }

    private void expectLocatorFailure(String tcId, String page, String scenario, By locator) {
        totalTC++;
        try {
            WebElement ignored = driver.findElement(locator);
            successCount++;
            logBaseline(tcId, page, scenario, locator.toString(), "UNEXPECTED_SUCCESS", "Locator masih ditemukan");
            System.out.printf("[%s] UNEXPECTED_SUCCESS — locator masih ditemukan: %s%n", tcId, locator);
        } catch (NoSuchElementException e) {
            failCount++;
            logBaseline(tcId, page, scenario, locator.toString(), "EXPECTED_FAIL", "NoSuchElementException");
            System.out.printf("[%s] EXPECTED_FAIL — locator gagal tanpa healing: %s%n", tcId, locator);
        }
    }

    private static synchronized void logBaseline(String tcId, String page, String scenario,
                                                 String locator, String status, String errorMessage) {
        String row = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                LocalDateTime.now().format(FMT),
                tcId,
                page,
                scenario,
                locator.replace("\"", "'"),
                status,
                errorMessage.replace("\"", "'"));
        try {
            Files.write(Path.of(BASELINE_FILE), row.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            System.err.println("[Baseline] Gagal menulis log: " + e.getMessage());
        }
    }

    private void openProductModalUsingCurrentLocator() {
        WebElement tambahButton = ensureElementClickable(By.id("inputProduct--Button"));
        jsClick(tambahButton);
        wait.until(d -> !hasClass(d.findElement(By.id("productModal")), "hidden"));
    }
}
