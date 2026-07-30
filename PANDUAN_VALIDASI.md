# Panduan Validasi Ahli: Self-Healing Selenium Framework

Selamat datang dan terima kasih atas kesediaan Bapak/Ibu menjadi validator ahli untuk penelitian ini! 🙏

Repositori ini berisi implementasi *framework automation testing* berbasis Selenium WebDriver yang dilengkapi dengan fitur **Self-Healing**. Fitur ini memungkinkan *test script* untuk secara otomatis menemukan elemen UI pengganti ketika elemen aslinya mengalami perubahan atribut (misal: perubahan ID, class, atau XPath) sehingga pengujian tidak gagal (error `NoSuchElementException`).

Untuk keperluan validasi ini, pengujian difokuskan pada dua Sistem Under Test (SUT):
1. **Orange HRM** — Aplikasi manajemen SDM berbasis web (demo publik)
2. **SauceDemo** — Aplikasi e-commerce contoh untuk keperluan pengujian

---

## 🛠️ Bagian 1: Instalasi Perangkat Lunak

Terdapat **3 perangkat lunak** yang perlu diinstal. Ikuti langkah-langkah berikut secara berurutan.

---

### ① Java Development Kit (JDK) 21

Java adalah bahasa pemrograman yang digunakan oleh framework ini.

1. Buka browser dan akses: **https://adoptium.net/temurin/releases/**
2. Pilih konfigurasi berikut:
   - **Version:** `21 - LTS`
   - **Operating System:** `Windows`
   - **Architecture:** `x64`
   - **Package Type:** `JDK`
3. Klik tombol `.msi` untuk mengunduh installer.
4. Jalankan file installer yang telah diunduh, klik **Next** terus hingga selesai.
   > ⚠️ Pastikan opsi **"Set JAVA_HOME variable"** dicentang saat instalasi berlangsung.
5. Verifikasi instalasi: Buka **Command Prompt** (tekan `Win + R`, ketik `cmd`, Enter), lalu ketik:
   ```
   java -version
   ```
   Hasil yang benar akan menampilkan: `openjdk version "21.x.x"`

---

### ② Apache Maven

Maven adalah alat *build* yang digunakan untuk mengelola dependensi dan menjalankan pengujian.

1. Buka browser dan akses: **https://maven.apache.org/download.cgi**
2. Unduh file **`apache-maven-3.x.x-bin.zip`** (pilih versi binary zip terbaru).
3. Ekstrak file ZIP tersebut, misalnya ke `C:\Program Files\Maven\`.
4. Tambahkan Maven ke PATH sistem:
   - Tekan `Win + S`, cari **"Environment Variables"**, klik **"Edit the system environment variables"**.
   - Klik tombol **"Environment Variables..."**.
   - Pada bagian **"System variables"**, cari variabel `Path`, klik **Edit**.
   - Klik **New**, lalu tambahkan path ke folder `bin` Maven, contoh: `C:\Program Files\Maven\apache-maven-3.9.9\bin`
   - Klik **OK** pada semua jendela.
5. **Tutup dan buka ulang** Command Prompt, lalu verifikasi:
   ```
   mvn -version
   ```
   Hasil yang benar akan menampilkan: `Apache Maven 3.x.x`

---

### ③ Google Chrome

Selenium akan mengendalikan browser Chrome secara otomatis selama pengujian. ChromeDriver akan **diunduh otomatis** oleh framework, jadi Bapak/Ibu hanya perlu memastikan Chrome terinstal.

1. Pastikan Google Chrome sudah terinstal di komputer Anda.
2. Perbarui Chrome ke versi terbaru: buka Chrome → klik ikon `⋮` (pojok kanan atas) → **Help** → **About Google Chrome** → biarkan update selesai jika ada.

---

## 📁 Bagian 2: Mengunduh Kode Program

1. Unduh kode program dari link yang diberikan oleh peneliti (file `.zip` atau via GitHub).
2. Ekstrak file ZIP ke lokasi yang mudah diakses, misalnya: `C:\self-healing-selenium\`

   **Atau**, jika menggunakan Git, buka Command Prompt dan jalankan:
   ```bash
   git clone <LINK_GITHUB_DARI_PENELITI>
   cd self-healing-selenium
   ```

---

## 🚀 Bagian 3: Menjalankan Pengujian

Setelah semua instalasi selesai, ikuti langkah berikut:

1. Buka **Command Prompt** (`Win + R` → ketik `cmd` → Enter).
2. Pindah ke folder project dengan perintah `cd`, contoh:
   ```bash
   cd C:\self-healing-selenium
   ```
3. Jalankan pengujian sesuai skenario yang ingin divalidasi:

   **Skenario A — Orange HRM (Self-Healing):**
   ```bash
   mvn clean test -Dtest=TestOrangeHrmHealing -DselfHealing.enabled=true
   ```

   **Skenario B — SauceDemo (Self-Healing):**
   ```bash
   mvn clean test -Dtest=TestSauceDemoHealing -DselfHealing.enabled=true
   ```

   **Menjalankan keduanya sekaligus:**
   ```bash
   mvn clean test -Dtest="TestOrangeHrmHealing,TestSauceDemoHealing" -DselfHealing.enabled=true
   ```

4. Saat pertama kali dijalankan, Maven akan **mengunduh dependensi** secara otomatis (membutuhkan koneksi internet, proses ini hanya terjadi sekali). Tunggu hingga selesai.

5. Browser Chrome akan terbuka secara otomatis dan menjalankan skenario pengujian. **Jangan menutup atau mengganggu jendela browser tersebut.**

---

## 📊 Bagian 4: Memeriksa Hasil Pengujian

Setelah pengujian selesai, hasil rekam jejak *self-healing* tersimpan otomatis di folder **`results/`**.

Buka file berikut menggunakan **Microsoft Excel** atau **Google Sheets**:
- `results/orangehrm_healing_log.csv`
- `results/saucedemo_healing_log.csv`

**Penjelasan kolom pada file CSV:**

| Kolom | Keterangan |
|---|---|
| `testName` | Nama skenario uji yang dijalankan |
| `status` | `HEALED` = berhasil diperbaiki otomatis, `FAILED` = gagal |
| `brokenLocator` | Locator asli yang rusak/tidak ditemukan |
| `healedLocator` | Locator pengganti yang berhasil ditemukan oleh mesin |
| `finalScore` | Skor kecocokan akhir (0.0 – 1.0), semakin tinggi semakin akurat |
| `textScore` | Skor berbasis kesamaan teks elemen |
| `locatorScore` | Skor berbasis kesamaan atribut locator |
| `positionScore` | Skor berbasis posisi elemen di layar |

Di dalam file tersebut, Bapak/Ibu dapat memvalidasi cara mesin (kombinasi *Levenshtein Distance*, *Semantic Guarding*, dan analisis posisi) dalam memilih kandidat elemen pengganti berdasarkan skor kecocokan.

---

## ❓ Troubleshooting Cepat

| Masalah | Solusi |
|---|---|
| `java` tidak dikenali di Command Prompt | Restart komputer setelah instalasi JDK, coba lagi |
| `mvn` tidak dikenali di Command Prompt | Pastikan PATH sudah ditambahkan, tutup dan buka ulang CMD |
| Browser tidak muncul / langsung error | Pastikan Google Chrome terinstal dan sudah diperbarui |
| Koneksi error saat download dependensi | Pastikan terhubung ke internet, jalankan perintah ulang |

---

*Terima kasih atas kontribusi Bapak/Ibu dalam memvalidasi fungsionalitas sistem ini! Apabila ada pertanyaan, silakan hubungi peneliti.*
