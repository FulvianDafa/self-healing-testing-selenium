package com.fulvian.healing;

import org.openqa.selenium.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ============================================================
 * AutoHealingWebDriver — Transparent Self-Healing WebDriver Wrapper
 * ============================================================
 *
 * Class ini membungkus WebDriver asli dan secara otomatis mengaktifkan
 * mekanisme self-healing ketika locator gagal.
 *
 * Cara kerja:
 *   1. Semua panggilan findElement(By) dicegat oleh wrapper ini.
 *   2. Jika locator asli berhasil, elemen dikembalikan langsung.
 *   3. Jika locator gagal (NoSuchElementException):
 *      - Jika selfHealingEnabled=false, throw exception seperti biasa.
 *      - Jika selfHealingEnabled=true, buat ElementProfile otomatis
 *        dari locator, lalu panggil HealingDriver untuk mencari
 *        elemen pengganti berdasarkan similarity score.
 *
 * Keuntungan:
 *   - Tester cukup menjalankan baseline script yang sudah ada
 *     dengan flag -DselfHealing.enabled=true
 *   - Tidak perlu mengubah locator manual di script
 *   - Semua method WebDriver lain tetap didelegasikan ke driver asli
 */
public class AutoHealingWebDriver implements WebDriver, JavascriptExecutor, TakesScreenshot {

    private final WebDriver realDriver;
    private final boolean selfHealingEnabled;
    private final double threshold;

    /**
     * ThreadLocal untuk melacak apakah findElement terakhir
     * berhasil melalui healing atau locator asli.
     */
    private static final ThreadLocal<Boolean> lastFindWasHealed = ThreadLocal.withInitial(() -> false);

    // Counter untuk auto-generate test case ID
    private int healingCounter = 0;

    // Kata-kata yang dihapus saat inferExpectedText
    private static final Set<String> NOISE_WORDS = Set.of(
            "btn", "button", "input", "refactor", "old", "new",
            "wrapper", "container", "element", "field", "form",
            "ctrl", "control"
    );

    // -------------------------------------------------------
    // Constructor
    // -------------------------------------------------------

    public AutoHealingWebDriver(WebDriver realDriver, boolean selfHealingEnabled, double threshold) {
        this.realDriver = realDriver;
        this.selfHealingEnabled = selfHealingEnabled;
        this.threshold = threshold;

        System.out.printf("[AUTO-HEALING] Wrapper diinisialisasi | enabled=%s | threshold=%.2f%n",
                selfHealingEnabled, threshold);
    }

    // -------------------------------------------------------
    // STATIC HELPER: cek apakah findElement terakhir di-heal
    // -------------------------------------------------------

    /**
     * Mengembalikan true jika findElement terakhir berhasil melalui healing,
     * lalu reset flag ke false.
     */
    public static boolean wasLastFindHealedAndReset() {
        boolean wasHealed = lastFindWasHealed.get();
        lastFindWasHealed.set(false);
        return wasHealed;
    }

    // -------------------------------------------------------
    // OVERRIDE findElement — inti dari auto-healing
    // -------------------------------------------------------

    @Override
    public WebElement findElement(By by) {
        // Tahap 1: coba locator asli
        try {
            WebElement element = realDriver.findElement(by);
            lastFindWasHealed.set(false);
            return element;
        } catch (NoSuchElementException e) {
            // Tahap 2: jika healing tidak aktif, lempar exception seperti biasa
            if (!selfHealingEnabled) {
                lastFindWasHealed.set(false);
                throw e;
            }

            // Tahap 3: aktifkan self-healing
            System.out.printf("[AUTO-HEALING] Locator gagal: %s — mengaktifkan self-healing...%n", by);

            healingCounter++;
            String testCaseId = "AUTO-" + String.format("%03d", healingCounter);

            // Buat ElementProfile otomatis dari informasi locator By
            String locatorType = inferLocatorType(by);
            String locatorValue = inferLocatorValue(by);
            String expectedText = inferExpectedText(by);

            ElementProfile profile = new ElementProfile(expectedText, locatorValue, locatorType);

            System.out.printf("[AUTO-HEALING] Profile: type=%s, value=%s, expectedText=%s%n",
                    locatorType, locatorValue, expectedText);

            // Buat HealingDriver dan cari elemen pengganti
            HealingDriver healer = new HealingDriver(
                    realDriver,
                    testCaseId,
                    "auto_healing",
                    threshold
            );

            WebElement healedElement = healer.findElement(by, profile);
            lastFindWasHealed.set(true);

            System.out.printf("[AUTO-HEALING] ✓ Elemen berhasil di-heal untuk locator: %s%n", by);

            return healedElement;
        }
    }

    @Override
    public List<WebElement> findElements(By by) {
        return realDriver.findElements(by);
    }

    // -------------------------------------------------------
    // LOCATOR INFERENCE HELPERS
    // -------------------------------------------------------

    /**
     * Menentukan tipe locator dari objek By.
     * Contoh: By.id → "id", By.xpath → "xpath"
     */
    static String inferLocatorType(By by) {
        String byString = by.toString();

        if (byString.startsWith("By.id:")) return "id";
        if (byString.startsWith("By.name:")) return "name";
        if (byString.startsWith("By.className:")) return "class";
        if (byString.startsWith("By.cssSelector:")) return "css";
        if (byString.startsWith("By.xpath:")) return "xpath";
        if (byString.startsWith("By.tagName:")) return "tag";
        if (byString.startsWith("By.linkText:")) return "linkText";
        if (byString.startsWith("By.partialLinkText:")) return "partialLinkText";

        return "unknown";
    }

    /**
     * Mengekstrak nilai locator dari objek By.
     * Contoh: By.id: inputProductBtn → "inputProductBtn"
     */
    static String inferLocatorValue(By by) {
        String byString = by.toString();

        // Format umum: "By.xxx: value"
        int colonIndex = byString.indexOf(':');
        if (colonIndex >= 0 && colonIndex < byString.length() - 1) {
            return byString.substring(colonIndex + 1).trim();
        }

        return byString;
    }

    /**
     * Menghasilkan teks yang diharapkan berdasarkan locator value.
     *
     * Pendekatan:
     * 1. Pecah camelCase, underscore, dash
     * 2. Bersihkan kata noise (btn, button, input, refactor, old)
     * 3. Kalau XPath/CSS terlalu kompleks, gunakan raw value
     *
     * Contoh:
     *   inputProductBtn → "product"
     *   namaBarang → "nama barang"
     *   simpanTransactionBtn → "simpan transaction"
     *   searchInput → "search"
     */
    static String inferExpectedText(By by) {
        String locatorType = inferLocatorType(by);
        String locatorValue = inferLocatorValue(by);

        // Untuk XPath dan CSS yang kompleks, gunakan raw value
        if ("xpath".equals(locatorType) || "css".equals(locatorType)) {
            // Coba ekstrak nilai id/class dari XPath/CSS jika sederhana
            String extracted = extractSimpleValueFromComplex(locatorValue);
            if (extracted != null && !extracted.isBlank()) {
                String cleaned = cleanAndSplitLocatorValue(extracted);

                // Untuk XPath: deteksi tag dari path dan tambahkan sebagai hint
                // Contoh: //tbody/tr/button[@id='editBtn'] → tag="button", cleaned="edit"
                // Hasil: "button edit" — agar HealingDriver tahu target adalah button
                if ("xpath".equals(locatorType)) {
                    String xpathTag = inferTagFromXPath(locatorValue);
                    if (xpathTag != null && !cleaned.contains(xpathTag)) {
                        cleaned = xpathTag + " " + cleaned;
                    }
                }

                return cleaned;
            }
            // Fallback: gunakan raw value
            return locatorValue;
        }

        return cleanAndSplitLocatorValue(locatorValue);
    }

    /**
     * Pecah string locator menjadi kata-kata bermakna.
     */
    private static String cleanAndSplitLocatorValue(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        // 1. Pecah camelCase: inputProductBtn → input Product Btn
        String split = value.replaceAll("([a-z])([A-Z])", "$1 $2");

        // 2. Pecah underscore dan dash
        split = split.replace("_", " ").replace("-", " ");

        // 3. Lowercase dan split
        String[] words = split.toLowerCase().split("\\s+");

        // 4. Buang noise words
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isBlank() && !NOISE_WORDS.contains(word)) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(word);
            }
        }

        String cleaned = result.toString().trim();

        // Jika semua kata terbuang, kembalikan split original (tanpa noise filtering)
        if (cleaned.isEmpty()) {
            return String.join(" ", words).trim();
        }

        return cleaned;
    }

    /**
     * Coba ekstrak nilai sederhana dari XPath/CSS.
     *
     * Untuk XPath dengan multiple @id, ambil yang TERAKHIR (elemen target).
     * Contoh:
     *   //tbody[@id='productTableBody']/tr[1]/td[10]/button[@id='editBtn'] → editBtn
     *   //button[@id='editBtn'] → editBtn
     *   #searchInput → searchInput
     */
    private static String extractSimpleValueFromComplex(String value) {
        if (value == null) return null;

        // XPath: coba ambil @id='xxx' atau @name='xxx' — ambil yang TERAKHIR
        Pattern xpathAttrPattern = Pattern.compile("@(?:id|name|class)=['\"]([^'\"]+)['\"]");
        Matcher matcher = xpathAttrPattern.matcher(value);
        String lastMatch = null;
        while (matcher.find()) {
            lastMatch = matcher.group(1);
        }
        if (lastMatch != null) {
            return lastMatch;
        }

        // CSS: #idValue
        Pattern cssIdPattern = Pattern.compile("#([a-zA-Z][a-zA-Z0-9_-]*)");
        matcher = cssIdPattern.matcher(value);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Deteksi tag HTML dari XPath.
     * Contoh:
     *   //tbody[@id='x']/tr/button[@id='editBtn'] → "button"
     *   //a[contains(@class,'link')] → "a"
     */
    static String inferTagFromXPath(String xpath) {
        if (xpath == null || xpath.isBlank()) return null;

        // Ambil segment terakhir sebelum [ atau akhir string
        // Contoh: .../button[@id='editBtn'] → "button"
        Pattern tagPattern = Pattern.compile("/([a-z]+)(?:\\[|$)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = tagPattern.matcher(xpath);
        String lastTag = null;
        while (matcher.find()) {
            lastTag = matcher.group(1).toLowerCase();
        }

        // Hanya return jika tag adalah elemen interaktif yang relevan
        if (lastTag != null) {
            switch (lastTag) {
                case "button":
                case "a":
                case "input":
                case "select":
                case "textarea":
                    return lastTag;
            }
        }

        return null;
    }

    // -------------------------------------------------------
    // GETTER untuk akses internal
    // -------------------------------------------------------

    /**
     * Mengembalikan driver asli (real driver) yang dibungkus.
     * Berguna ketika perlu akses langsung ke ChromeDriver.
     */
    public WebDriver getRealDriver() {
        return realDriver;
    }

    public boolean isSelfHealingEnabled() {
        return selfHealingEnabled;
    }

    // -------------------------------------------------------
    // DELEGASI WebDriver METHODS
    // -------------------------------------------------------

    @Override
    public void get(String url) {
        realDriver.get(url);
    }

    @Override
    public String getCurrentUrl() {
        return realDriver.getCurrentUrl();
    }

    @Override
    public String getTitle() {
        return realDriver.getTitle();
    }

    @Override
    public String getPageSource() {
        return realDriver.getPageSource();
    }

    @Override
    public void close() {
        realDriver.close();
    }

    @Override
    public void quit() {
        realDriver.quit();
    }

    @Override
    public Set<String> getWindowHandles() {
        return realDriver.getWindowHandles();
    }

    @Override
    public String getWindowHandle() {
        return realDriver.getWindowHandle();
    }

    @Override
    public TargetLocator switchTo() {
        return realDriver.switchTo();
    }

    @Override
    public Navigation navigate() {
        return realDriver.navigate();
    }

    @Override
    public Options manage() {
        return realDriver.manage();
    }

    // -------------------------------------------------------
    // DELEGASI JavascriptExecutor
    // -------------------------------------------------------

    @Override
    public Object executeScript(String script, Object... args) {
        if (realDriver instanceof JavascriptExecutor) {
            return ((JavascriptExecutor) realDriver).executeScript(script, args);
        }
        throw new UnsupportedOperationException("Real driver does not support JavascriptExecutor");
    }

    @Override
    public Object executeAsyncScript(String script, Object... args) {
        if (realDriver instanceof JavascriptExecutor) {
            return ((JavascriptExecutor) realDriver).executeAsyncScript(script, args);
        }
        throw new UnsupportedOperationException("Real driver does not support JavascriptExecutor");
    }

    // -------------------------------------------------------
    // DELEGASI TakesScreenshot
    // -------------------------------------------------------

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
        if (realDriver instanceof TakesScreenshot) {
            return ((TakesScreenshot) realDriver).getScreenshotAs(target);
        }
        throw new UnsupportedOperationException("Real driver does not support TakesScreenshot");
    }
}
