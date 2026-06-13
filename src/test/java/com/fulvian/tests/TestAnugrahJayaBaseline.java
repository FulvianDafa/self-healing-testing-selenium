package com.fulvian.tests;

import com.fulvian.base.BaseTest;
import com.fulvian.healing.AutoHealingWebDriver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
import java.util.List;

/**
 * Baseline pembanding untuk Bab 4.
 *
 * Test ini sengaja TIDAK memakai HealingDriver.
 * Tujuannya menunjukkan bahwa locator lama gagal ketika DOM/id berubah.
 *
 * Baseline ini dibuat apple-to-apple dengan skenario self-healing aktif:
 * BL-001  ↔ SH-001
 * BL-004  ↔ SH-004
 * BL-011  ↔ SH-011
 * BL-013  ↔ SH-013
 * BL-020  ↔ SH-020
 * BL-021  ↔ SH-021
 * BL-022  ↔ SH-022
 * BL-030  ↔ SH-030
 * BL-031  ↔ SH-031
 * BL-040  ↔ SH-040
 * BL-041  ↔ SH-041
 * BL-050  ↔ SH-050
 * BL-051  ↔ SH-051
 *
 * Catatan:
 * BL-010 tidak dimasukkan karena SH-010 sudah dinonaktifkan,
 * sehingga bukan bagian dari perbandingan utama.
 *
 * Hasil baseline ditulis ke:
 * results/anugrah_baseline_log.csv
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
        totalTC = 0;
        failCount = 0;
        successCount = 0;

        try {
            Files.createDirectories(Paths.get("results"));

            String header = "timestamp,test_case_id,page,scenario_type,locator,status,error_message\n";

            Files.write(
                    Paths.get(BASELINE_FILE),
                    header.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            System.out.println("[Baseline] Log diinisialisasi: " + BASELINE_FILE);
        } catch (IOException e) {
            System.err.println("[Baseline] Gagal inisialisasi log: " + e.getMessage());
        }
    }

    // =========================================================
    // PRODUK / INDEX PAGE
    // =========================================================

    @Test
    @Order(1)
    @DisplayName("BL-001 Produk: locator lama tombol Tambah Produk gagal tanpa healing")
    void bl001_addProductOldLocatorFails() {
        /*
         * Pasangan: SH-001
         * Locator lama: inputProductBtn
         * DOM aktual: inputProduct--Button
         */
        expectLocatorFailure(
                "BL-001",
                "index.html",
                "id_change",
                By.id("inputProductBtn")
        );
    }

    @Test
    @Order(2)
    @DisplayName("BL-004 Produk: input Nama Barang gagal setelah id berubah tanpa healing")
    void bl004_namaBarangChangedIdFails() {
        /*
         * Pasangan: SH-004
         * Locator lama: namaBarang
         * DOM dimutasi menjadi: inputNamaBarangRefactor
         *
         * Catatan self-healing mode:
         * Modal harus tetap terbuka agar inputNamaBarangRefactor
         * terbaca sebagai displayed/enabled oleh HealingDriver.
         */
        openProductModalUsingCurrentLocator();

        waitForElementPresent(
                By.id("namaBarang"),
                "Precondition gagal: namaBarang tidak ditemukan sebelum mutasi"
        );

        simulateIdChange("namaBarang", "inputNamaBarangRefactor");

        // Pastikan modal terbuka DAN input visible (penting untuk self-healing)
        forceOpenProductModalIfNeeded();
        forceElementVisible("inputNamaBarangRefactor");

        expectLocatorFailure(
                "BL-004",
                "index.html",
                "id_change",
                By.id("namaBarang")
        );
    }

    @Test
    @Order(3)
    @DisplayName("BL-011 Produk: search input gagal setelah id berubah tanpa healing")
    void bl011_productSearchChangedIdFails() {
        /*
         * Pasangan: SH-011
         * Locator lama: searchInput
         * DOM dimutasi menjadi: productSearchInputRefactor
         */
        waitForPageReady();

        waitForElementPresent(
                By.id("searchInput"),
                "Precondition gagal: searchInput produk tidak ditemukan sebelum mutasi"
        );

        simulateIdChange("searchInput", "productSearchInputRefactor");

        expectLocatorFailure(
                "BL-011",
                "index.html",
                "id_change",
                By.id("searchInput")
        );
    }

    @Test
    @Order(4)
    @DisplayName("BL-013 Produk: tombol Edit gagal saat XPath lama rusak tanpa healing")
    void bl013_editButtonBrokenXPathFails() {
        /*
         * Pasangan: SH-013
         * XPath lama sengaja rusak karena mencari button id='editBtn',
         * sedangkan tombol Edit aktual tidak memakai id tersebut.
         */
        waitForPageReady();
        waitForEditButtonPresent();

        By brokenXPath = By.xpath("//tbody[@id='productTableBody']/tr[1]/td[10]/button[@id='editBtn']");

        expectLocatorFailure(
                "BL-013",
                "index.html",
                "xpath_change",
                brokenXPath
        );
    }

    // =========================================================
    // TRANSAKSI PENJUALAN
    // =========================================================

    @Test
    @Order(5)
    @DisplayName("BL-020 Transaksi: search barang gagal setelah id berubah tanpa healing")
    void bl020_transactionSearchChangedIdFails() {
        /*
         * Pasangan: SH-020
         */
        openPage("transaction_page.html");
        waitForPageReady();

        waitForElementPresent(
                By.id("searchInput"),
                "Precondition gagal: searchInput transaksi tidak ditemukan sebelum mutasi"
        );

        simulateIdChange("searchInput", "transactionSearchInputRefactor");

        expectLocatorFailure(
                "BL-020",
                "transaction_page.html",
                "id_change",
                By.id("searchInput")
        );
    }

    @Test
    @Order(6)
    @DisplayName("BL-021 Transaksi: tombol Simpan Transaksi gagal setelah id berubah tanpa healing")
    void bl021_transactionSaveChangedIdFails() {
        /*
         * Pasangan: SH-021
         *
         * State yang diperlukan:
         * - Halaman transaksi terbuka
         * - Invoice baru dibuat
         * - Minimal 1 produk masuk ke keranjang
         *   (simpanTransactionBtn disabled saat keranjang kosong)
         */
        openTransactionPageAndEnsureTarget("simpanTransactionBtn");

        // Tambahkan produk ke keranjang agar simpanTransactionBtn enabled
        addProductToCartIfNeeded();

        simulateIdChange("simpanTransactionBtn", "saveTransactionButtonRefactor");

        expectLocatorFailure(
                "BL-021",
                "transaction_page.html",
                "id_change",
                By.id("simpanTransactionBtn")
        );
    }

    @Test
    @Order(7)
    @DisplayName("BL-022 Transaksi: input Jumlah Bayar gagal setelah id berubah tanpa healing")
    void bl022_paidInputChangedIdFails() {
        /*
         * Pasangan: SH-022
         *
         * State yang diperlukan:
         * - Halaman transaksi terbuka
         * - Invoice baru dibuat
         * - Minimal 1 produk masuk ke keranjang
         * - Metode pembayaran "Utang" dipilih (radioUtang)
         *   agar paidInput enabled
         */
        openTransactionPageAndEnsureTarget("paidInput");

        // Tambahkan produk ke keranjang
        addProductToCartIfNeeded();

        // Pilih metode pembayaran Utang agar paidInput menjadi enabled
        selectRadioUtangIfNeeded();

        simulateIdChange("paidInput", "paidInputRefactor");

        expectLocatorFailure(
                "BL-022",
                "transaction_page.html",
                "id_change",
                By.id("paidInput")
        );
    }

    // =========================================================
    // UTANG
    // =========================================================

    @Test
    @Order(8)
    @DisplayName("BL-030 Utang: tombol Tambah Pengutang gagal setelah id berubah tanpa healing")
    void bl030_addDebtorChangedIdFails() {
        /*
         * Pasangan: SH-030
         */
        openPage("debt_page.html");
        waitForPageReady();

        waitForElementPresent(
                By.id("addDebtorBtn"),
                "Precondition gagal: addDebtorBtn tidak ditemukan sebelum mutasi"
        );

        simulateIdChange("addDebtorBtn", "addDebtorButtonRefactor");

        expectLocatorFailure(
                "BL-030",
                "debt_page.html",
                "id_change",
                By.id("addDebtorBtn")
        );
    }

    @Test
    @Order(9)
    @DisplayName("BL-031 Utang: input Nama Pengutang gagal setelah id berubah tanpa healing")
    void bl031_debtorNameChangedIdFails() {
        /*
         * Pasangan: SH-031
         */
        openPage("debt_page.html");
        openDebtModalUsingCurrentLocator();

        waitForElementPresent(
                By.id("inputDebtorName"),
                "Precondition gagal: inputDebtorName tidak ditemukan sebelum mutasi"
        );

        simulateIdChange("inputDebtorName", "inputDebtorNameRefactor");

        expectLocatorFailure(
                "BL-031",
                "debt_page.html",
                "id_change",
                By.id("inputDebtorName")
        );
    }

    // =========================================================
    // DASHBOARD TRACKING
    // =========================================================

    @Test
    @Order(10)
    @DisplayName("BL-040 Dashboard: tab Data Utang gagal setelah id berubah tanpa healing")
    void bl040_dashboardDebtTabChangedIdFails() {
        /*
         * Pasangan: SH-040
         */
        openPage("dashboard.html");
        waitForPageReady();

        waitForElementPresent(
                By.id("tab-debt"),
                "Precondition gagal: tab-debt tidak ditemukan sebelum mutasi"
        );

        simulateIdChange("tab-debt", "tabDebtRefactor");

        expectLocatorFailure(
                "BL-040",
                "dashboard.html",
                "id_change",
                By.id("tab-debt")
        );
    }

    @Test
    @Order(11)
    @DisplayName("BL-041 Dashboard: search produk gagal setelah id berubah tanpa healing")
    void bl041_dashboardProductSearchChangedIdFails() {
        /*
         * Pasangan: SH-041
         */
        openPage("dashboard.html");
        waitForPageReady();

        waitForElementPresent(
                By.id("product-search-input"),
                "Precondition gagal: product-search-input tidak ditemukan sebelum mutasi"
        );

        simulateIdChange("product-search-input", "productSearchDashboardRefactor");

        expectLocatorFailure(
                "BL-041",
                "dashboard.html",
                "id_change",
                By.id("product-search-input")
        );
    }

    // =========================================================
    // PRINT DAFTAR BARANG
    // =========================================================

    @Test
    @Order(12)
    @DisplayName("BL-050 Print Daftar Barang: search input gagal setelah id berubah tanpa healing")
    void bl050_printSearchChangedIdFails() {
        /*
         * Pasangan: SH-050
         */
        openPage("print_daftar_barang.html");
        waitForPageReady();

        waitForElementPresent(
                By.id("searchInput"),
                "Precondition gagal: searchInput print tidak ditemukan sebelum mutasi"
        );

        simulateIdChange("searchInput", "printSearchInputRefactor");

        expectLocatorFailure(
                "BL-050",
                "print_daftar_barang.html",
                "id_change",
                By.id("searchInput")
        );
    }

    @Test
    @Order(13)
    @DisplayName("BL-051 Print Daftar Barang: tombol layout tabel gagal setelah id berubah tanpa healing")
    void bl051_printLayoutTableChangedIdFails() {
        /*
         * Pasangan: SH-051
         */
        openPage("print_daftar_barang.html");
        waitForPageReady();

        waitForElementPresent(
                By.id("btnLayoutTable"),
                "Precondition gagal: btnLayoutTable tidak ditemukan sebelum mutasi"
        );

        simulateIdChange("btnLayoutTable", "layoutTableButtonRefactor");

        expectLocatorFailure(
                "BL-051",
                "print_daftar_barang.html",
                "id_change",
                By.id("btnLayoutTable")
        );
    }

    // =========================================================
    // SUMMARY
    // =========================================================

    @AfterAll
    static void printBaselineSummary() {
        String sep = "-".repeat(70);
        boolean healingMode = Boolean.parseBoolean(
                System.getProperty("selfHealing.enabled", "false")
        );

        System.out.println("\n" + sep);

        if (healingMode) {
            int healedCount = successCount; // successCount berisi HEALED_BY_SELF_HEALING + UNEXPECTED_SUCCESS
            System.out.println("  RINGKASAN BASELINE SCRIPT DENGAN AUTO SELF-HEALING");
            System.out.println(sep);
            System.out.printf("  Total test case    : %d%n", totalTC);
            System.out.printf("  Berhasil di-heal   : %d%n", healedCount);
            System.out.printf("  Gagal locator      : %d%n", failCount);
            System.out.printf("  Healing Success Rate : %.2f%%%n",
                    totalTC > 0 ? healedCount * 100.0 / totalTC : 0);
        } else {
            System.out.println("  RINGKASAN BASELINE ANUGRAH_JAYA (Tanpa Self-Healing)");
            System.out.println(sep);
            System.out.printf("  Total test case    : %d%n", totalTC);
            System.out.printf("  Gagal locator      : %d%n", failCount);
            System.out.printf("  Masih ditemukan    : %d%n", successCount);
            System.out.printf("  Failure Rate       : %.2f%%%n",
                    totalTC > 0 ? failCount * 100.0 / totalTC : 0);
        }

        System.out.println("  Data               : " + Paths.get(BASELINE_FILE).toAbsolutePath());
        System.out.println(sep + "\n");
    }

    // =========================================================
    // LOCAL HELPERS
    // =========================================================

    private void expectLocatorFailure(String tcId, String page, String scenario, By locator) {
        totalTC++;

        boolean selfHealingEnabled = Boolean.parseBoolean(
                System.getProperty("selfHealing.enabled", "false")
        );

        try {
            driver.findElement(locator);

            // Cek apakah elemen ditemukan melalui healing
            if (selfHealingEnabled && AutoHealingWebDriver.wasLastFindHealedAndReset()) {
                successCount++;

                logBaseline(
                        tcId,
                        page,
                        scenario,
                        locator.toString(),
                        "HEALED_BY_SELF_HEALING",
                        "Locator asli gagal tetapi elemen berhasil ditemukan melalui self-healing"
                );

                System.out.printf("[%s] HEALED_BY_SELF_HEALING — elemen di-heal otomatis: %s%n", tcId, locator);
            } else {
                successCount++;

                logBaseline(
                        tcId,
                        page,
                        scenario,
                        locator.toString(),
                        "UNEXPECTED_SUCCESS",
                        "Locator masih ditemukan"
                );

                System.out.printf("[%s] UNEXPECTED_SUCCESS — locator masih ditemukan: %s%n", tcId, locator);
            }

        } catch (NoSuchElementException e) {
            failCount++;

            logBaseline(
                    tcId,
                    page,
                    scenario,
                    locator.toString(),
                    "EXPECTED_FAIL",
                    "NoSuchElementException"
            );

            System.out.printf("[%s] EXPECTED_FAIL — locator gagal tanpa healing: %s%n", tcId, locator);
        }
    }

    private static synchronized void logBaseline(
            String tcId,
            String page,
            String scenario,
            String locator,
            String status,
            String errorMessage
    ) {
        String row = String.format(
                "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                LocalDateTime.now().format(FMT),
                tcId,
                page,
                scenario,
                locator.replace("\"", "'"),
                status,
                errorMessage.replace("\"", "'")
        );

        try {
            Files.write(
                    Path.of(BASELINE_FILE),
                    row.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE
            );
        } catch (IOException e) {
            System.err.println("[Baseline] Gagal menulis log: " + e.getMessage());
        }
    }

    private void openProductModalUsingCurrentLocator() {
        waitForPageReady();

        WebElement tambahButton = ensureElementClickable(By.id("inputProduct--Button"));

        jsClick(tambahButton);

        forceOpenProductModalIfNeeded();

        waitForElementPresent(
                By.id("productModal"),
                "Precondition gagal: productModal tidak ditemukan"
        );
    }

    private void forceOpenProductModalIfNeeded() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "const modal = document.getElementById('productModal');" +
                    "if (modal) {" +
                    "  modal.classList.remove('hidden');" +
                    "  modal.classList.remove('opacity-0');" +
                    "  modal.classList.remove('pointer-events-none');" +
                    "  modal.classList.add('flex');" +
                    "  modal.style.display = 'flex';" +
                    "  modal.style.opacity = '1';" +
                    "  modal.style.visibility = 'visible';" +
                    "  modal.style.pointerEvents = 'auto';" +
                    "}"
            );

            System.out.println("[DEBUG] productModal dipaksa terbuka untuk stabilisasi baseline.");
        } catch (Exception e) {
            System.out.println("[DEBUG] Gagal force open productModal: " + e.getMessage());
        }
    }

    private void openDebtModalUsingCurrentLocator() {
        waitForPageReady();

        WebElement tambahButton = ensureElementClickable(By.id("addDebtorBtn"));

        jsClick(tambahButton);

        forceOpenDebtModalIfNeeded();

        waitForElementPresent(
                By.id("debtModal"),
                "Precondition gagal: debtModal tidak ditemukan"
        );
    }

    private void forceOpenDebtModalIfNeeded() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "const modal = document.getElementById('debtModal');" +
                    "if (modal) {" +
                    "  modal.classList.remove('hidden');" +
                    "  modal.classList.remove('opacity-0');" +
                    "  modal.classList.remove('pointer-events-none');" +
                    "  modal.style.display = 'flex';" +
                    "  modal.style.opacity = '1';" +
                    "  modal.style.visibility = 'visible';" +
                    "  modal.style.pointerEvents = 'auto';" +
                    "}"
            );

            System.out.println("[DEBUG] debtModal dipaksa terbuka untuk stabilisasi baseline.");
        } catch (Exception e) {
            System.out.println("[DEBUG] Gagal force open debtModal: " + e.getMessage());
        }
    }

    /**
     * Paksa elemen dengan id tertentu menjadi visible dan enabled via JavaScript.
     * Juga memaksa semua ancestor di rantai DOM agar tidak hidden.
     */
    private void forceElementVisible(String elementId) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "const el = document.getElementById(arguments[0]);" +
                    "if (el) {" +
                    "  el.style.display = '';" +
                    "  el.style.visibility = 'visible';" +
                    "  el.style.opacity = '1';" +
                    "  el.style.pointerEvents = 'auto';" +
                    "  el.removeAttribute('disabled');" +
                    "  el.removeAttribute('readonly');" +
                    "  let p = el.parentElement;" +
                    "  while (p && p !== document.body) {" +
                    "    const cs = getComputedStyle(p);" +
                    "    if (cs.display === 'none' || cs.visibility === 'hidden') {" +
                    "      p.style.display = 'block';" +
                    "      p.style.visibility = 'visible';" +
                    "      p.style.opacity = '1';" +
                    "    }" +
                    "    p = p.parentElement;" +
                    "  }" +
                    "}",
                    elementId
            );
            System.out.println("[Baseline] forceElementVisible: " + elementId);
        } catch (Exception e) {
            System.out.println("[Baseline] Gagal forceElementVisible " + elementId + ": " + e.getMessage());
        }
    }

    private void openTransactionPageAndEnsureTarget(String targetElementId) {
        openPage("transaction_page.html");

        waitForPageReady();

        clickIfVisible(By.id("generateNewInvoiceBtn"));

        waitForElementPresent(
                By.id(targetElementId),
                "Precondition gagal: " + targetElementId + " tidak ditemukan sebelum mutasi"
        );
    }

    private void waitForPageReady() {
        wait.until(d -> {
            Object readyState = ((JavascriptExecutor) d)
                    .executeScript("return document.readyState");

            return "complete".equals(readyState);
        });
    }

    private WebElement waitForElementPresent(By locator, String errorMessage) {
        return wait.until(d -> {
            try {
                return d.findElement(locator);
            } catch (NoSuchElementException e) {
                return null;
            }
        });
    }

    private WebElement waitForEditButtonPresent() {
        return wait.until(d -> {
            List<WebElement> buttons = d.findElements(By.tagName("button"));

            for (WebElement button : buttons) {
                try {
                    String text = button.getText();

                    if (button.isDisplayed()
                            && text != null
                            && text.trim().equalsIgnoreCase("Edit")) {
                        return button;
                    }
                } catch (Exception ignored) {
                }
            }

            return null;
        });
    }

    private boolean clickIfVisible(By locator) {
        try {
            List<WebElement> elements = driver.findElements(locator);

            for (WebElement element : elements) {
                if (element.isDisplayed() && element.isEnabled()) {
                    jsClick(element);
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Tambahkan produk pertama ke keranjang transaksi.
     * Diperlukan agar simpanTransactionBtn menjadi enabled.
     */
    private void addProductToCartIfNeeded() {
        try {
            // Tunggu daftar produk termuat
            WebElement firstProductRow = wait.until(d -> {
                List<WebElement> rows = d.findElements(By.cssSelector("#productListBody tr"));

                for (WebElement row : rows) {
                    String text = row.getText();
                    if (text == null) continue;
                    String normalized = text.toLowerCase().trim();

                    if (row.isDisplayed()
                            && !normalized.isEmpty()
                            && !normalized.contains("tidak ada produk")
                            && !normalized.contains("gagal memuat")) {
                        return row;
                    }
                }
                return null;
            });

            if (firstProductRow != null) {
                jsClick(firstProductRow);

                // Tunggu item masuk ke keranjang
                wait.until(d -> {
                    List<WebElement> cartRows = d.findElements(
                            By.cssSelector("#cartTableBody tr[data-kode-barang]"));
                    return cartRows.size() > 0;
                });

                System.out.println("[Baseline] Produk berhasil ditambahkan ke keranjang.");
            }
        } catch (Exception e) {
            System.out.println("[Baseline] Gagal menambahkan produk ke keranjang: " + e.getMessage());
        }
    }

    /**
     * Pilih metode pembayaran Utang (radioUtang) agar paidInput menjadi enabled.
     */
    private void selectRadioUtangIfNeeded() {
        try {
            WebElement radioUtang = wait.until(d -> {
                WebElement radio = d.findElement(By.id("radioUtang"));
                return radio.isDisplayed() && radio.isEnabled() ? radio : null;
            });

            if (radioUtang != null) {
                jsClick(radioUtang);
                System.out.println("[Baseline] Metode pembayaran Utang dipilih, paidInput seharusnya enabled.");

                // Tunggu paidInput menjadi enabled
                wait.until(d -> {
                    try {
                        WebElement paidInput = d.findElement(By.id("paidInput"));
                        return paidInput.isDisplayed() && paidInput.isEnabled();
                    } catch (Exception ex) {
                        return false;
                    }
                });
            }
        } catch (Exception e) {
            System.out.println("[Baseline] Gagal memilih radioUtang: " + e.getMessage());
        }
    }
}