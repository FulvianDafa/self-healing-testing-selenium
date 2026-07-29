# Panduan Validasi Ahli: Self-Healing Selenium Framework

Selamat datang dan terima kasih atas kesediaan Anda menjadi validator ahli untuk penelitian ini! 

Repositori ini berisi implementasi *framework automation testing* berbasis Selenium WebDriver yang dilengkapi dengan fitur **Self-Healing**. Fitur ini memungkinkan *test script* untuk secara otomatis menemukan elemen UI pengganti ketika elemen aslinya mengalami perubahan atribut (misal: perubahan ID, class, atau XPath) sehingga pengujian tidak gagal (*error* `NoSuchElementException`).

Untuk keperluan validasi ini, pengujian difokuskan pada dua Sistem Under Test (SUT):
1. **Orange HRM** 
2. **SauceDemo**

## 🛠️ Prasyarat Sistem
Sebelum menjalankan pengujian, pastikan mesin Anda telah terinstal:
- **Java (JDK) 21** atau yang lebih baru.
- **Apache Maven**.
- **Google Chrome** versi terbaru.

## 🚀 Cara Menjalankan Pengujian

Anda hanya perlu menjalankan *test suite* yang sudah disiapkan melalui terminal/command prompt.

1. Buka Terminal atau Command Prompt.
2. *Clone* repositori ini ke komputer Anda:
   ```bash
   git clone <LINK_GITHUB_DISINI>
   cd self-healing-selenium
   ```
3. Jalankan pengujian *healing* menggunakan Maven:

   **Menjalankan Skenario Orange HRM:**
   ```bash
   mvn clean test -Dtest=TestOrangeHrmHealing
   ```

   **Menjalankan Skenario SauceDemo:**
   ```bash
   mvn clean test -Dtest=TestSauceDemoHealing
   ```

   *(Opsional)* Anda juga dapat menjalankan keduanya sekaligus dengan perintah:
   ```bash
   mvn clean test -Dtest=TestOrangeHrmHealing,TestSauceDemoHealing
   ```

## 📊 Memeriksa Hasil Pengujian
Setelah *test* selesai dieksekusi, sistem *Self-Healing* secara otomatis mencatat rekam jejak penyembuhan *locator* yang rusak ke dalam *folder* `results/`. 

Anda dapat membuka file CSV berikut di Excel/Spreadsheet:
- `orangehrm_healing_log.csv`
- `saucedemo_healing_log.csv`

Di dalam file tersebut, Anda dapat memvalidasi algoritma mesin (kombinasi *Levenshtein Distance*, *Semantic Guarding*, dan Posisi) dalam memilih kandidat elemen pengganti berdasarkan skor kecocokan.

Terima kasih atas bantuan Anda dalam memvalidasi fungsionalitas sistem ini!
