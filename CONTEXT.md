# CONTEXT.MD - SINGLE SOURCE OF TRUTH PROYEK

Dokumen ini adalah **Single Source of Truth** untuk proyek penelitian dan pengembangan *Self-Healing Selenium WebDriver*. Dokumen ini sangat esensial digunakan untuk memahami keseluruhan proyek secara mendalam, riwayat pengembangan, arsitektur, hingga status penulisan skripsi.

---

## 1. IDENTITAS PROYEK

* **Nama proyek:** Self-Healing Selenium WebDriver
* **Jenis proyek:** Penelitian Akademik (Skripsi S1) / Software Testing & Automation
* **Tujuan proyek:** Membangun *wrapper* untuk Selenium WebDriver yang secara otomatis memperbaiki kegagalan pencarian elemen (*NoSuchElementException*) akibat perubahan struktur UI atau atribut *locator* secara mandiri (*self-healing*) saat *runtime*.
* **Latar belakang proyek:** Script pengujian otomatis berbasis web (UI automation) sangat rapuh (*fragile*). Perubahan kecil pada antarmuka pengguna (seperti ganti nama *class* atau ID oleh developer frontend) dapat merusak *test script* secara keseluruhan, menyebabkan biaya *maintenance* script yang sangat tinggi.
* **Permasalahan utama yang diselesaikan:** *Fragile Test Problem* dan tingginya biaya *maintenance locator* pada Selenium WebDriver.
* **Target pengguna:** Automation Tester, QA Engineer, Developer.
* **Ruang lingkup proyek:** Pembuatan *engine* komparasi string dan posisi (Similarity Engine), pembuatan proxy/wrapper untuk WebDriver, dan pembuatan GUI kontrol untuk eksperimen.
* **Batasan proyek:** Hanya mendukung Selenium WebDriver untuk platform *Web Browser*. Tidak mendukung *Mobile Testing* (Appium). Algoritma menggunakan pendekatan kalkulasi *string/heuristic* (Levenshtein, Semantic Guard), bukan *Deep Learning/Machine Learning* real-time yang membebani komputasi lokal.
* **Hasil yang diharapkan:** Script testing yang sebelumnya gagal (error) dapat dilanjutkan secara otomatis, menghasilkan log/laporan kemiripan, metrik akurasi tinggi (*Healing Success Rate*), dan tingkat *False Positive* yang sangat minim.

---

## 2. EXECUTIVE SUMMARY

Proyek "Self-Healing Selenium" ini adalah sebuah penelitian yang mengusulkan pengembangan pustaka/wrapper cerdas di atas Selenium WebDriver standar. Tujuannya adalah untuk mengatasi masalah skenario pengujian otomatis yang tiba-tiba gagal karena ada *refactoring* pada kode HTML target (contoh: ID tombol "btnSubmit" diubah menjadi "button-submit-new").

Alih-alih berhenti dan melemparkan *error*, sistem ini akan mencegat pesan *error* tersebut, menghentikan sementara proses, membongkar DOM (*Document Object Model*) halaman saat itu, dan secara cerdas mencari elemen pengganti yang paling mirip dengan elemen asli. Setelah kandidat terbaik ditemukan (melalui proses komparasi teks, *locator*, dan posisi koordinat yang diawasi oleh filter *Semantic Guarding*), sistem akan mengklik/berinteraksi dengan elemen baru tersebut dan mencatat kejadian ini ke dalam log CSV.

Saat ini, proyek **sudah selesai di tahap pengembangan sistem**. Kode inti (`SimilarityEngine`, `HealingDriver`), GUI untuk eksekusi, serta log telemetri CSV sudah berjalan 100%. Fokus utama saat ini bergeser pada **Penyusunan Skripsi Bab 4 (Implementasi & Evaluasi) dan Bab 5 (Kesimpulan)**, termasuk merapikan *evidence* pengujian terhadap berbagai *System Under Test* (SUT) seperti sistem Anugrah Jaya, Arsip Dokumen, dan sistem Sapi.

---

## 3. LATAR BELAKANG DAN MOTIVASI

* **Masalah awal:** Saat frontend di-*update*, *locator* (ID, XPath, Class) sering berubah. Hal ini membuat ratusan script QA yang sudah dibuat menjadi *obsolete* (usang/gagal).
* **Konteks penelitian:** Menemukan algoritma pencocokan kemiripan yang ringan namun akurat untuk memulihkan *test script*.
* **Konteks bisnis:** QA menghabiskan terlalu banyak waktu (hingga 30-40% waktu kerja) hanya untuk *maintain script* lama, bukan membuat *script* baru.
* **Alasan proyek dibuat:** Selenium tidak memiliki kemampuan *auto-heal* secara *native*.
* **Gap yang ingin diselesaikan:** Membedakan antara metode *healing* yang sering salah (*False Positive* - misal: input password malah tertukar dengan input email karena skor Levenshteinnya kebetulan mirip) dengan metode *healing* yang punya kesadaran semantik (*Semantic Guarding*).
* **Urgensi penelitian:** Mendesak, karena pendekatan *agile* modern menuntut frontend rilis dengan cepat, yang otomatis membuat *test script* sering rusak.

---

## 4. TUJUAN DAN KONTRIBUSI

### Tujuan Utama
Menciptakan algoritma dan implementasi *self-healing* yang *plug-and-play* untuk *script* Selenium.

### Tujuan Khusus
Mengintegrasikan metode *Semantic Guarding* (Filter berbasis tag dan tipe input) untuk memastikan proses *healing* memiliki akurasi yang tinggi dan menolak kandidat elemen secara logis sebelum masuk tahap kalkulasi matematis.

### Kontribusi Teoretis
Membuktikan bahwa penggabungan Levenshtein Distance dengan heuristik spesifik-DOM (Semantic Guarding + Posisi Euclidean) jauh lebih efektif dibanding murni menggunakan algoritma perbandingan string saja dalam konteks Web Automation.

### Kontribusi Praktis
Memberikan sebuah alat (*tool*) lengkap dengan GUI dan sistem *logging* otomatis (ke CSV) yang siap pakai oleh kalangan industri.

### Manfaat Penelitian
Meringankan beban *maintenance* QA, serta memberikan kontribusi berupa metrik *Healing Success Rate* pada eksperimen 4 *System Under Test* (SUT) berbeda.

---

## 5. STATUS PROYEK TERKINI

* **Yang Sudah Selesai:** 
  - Seluruh algoritma *core* (`SimilarityEngine`).
  - Pembuatan Wrapper (`HealingDriver`, `AutoHealingWebDriver`).
  - *Logging Engine* (`HealingLogger`, `HealingResult`).
  - GUI Aplikasi (`SelfHealingGui`).
  - Pengujian Baseline vs Mutated pada berbagai SUT.
* **Yang Sedang Dikerjakan:** 
  - Penulisan dokumen Bab 4 (Implementasi dan Evaluasi Metrik).
  - Mengompilasi bukti (*evidence*) seperti CSV, *screenshot* tabel GUI, dan diagram.
* **Yang Belum Dikerjakan:** 
  - Penyelesaian Bab 5.
  - Persiapan draft *slide* presentasi Sidang.
* **Milestone yang Sudah Tercapai:** Aplikasi berhasil menyembuhkan *script* yang *error* secara otomatis dan mencatat tingkat *False Positive* yang dapat dikendalikan.
* **Milestone Berikutnya:** *Approval* Skripsi Bab 4 dan 5 oleh Dosen Pembimbing.
* **Persentase Progress (estimasi):** 85% - 90% secara total menuju sidang.

---

## 6. TIMELINE PERJALANAN PROYEK

* **Periode Awal:**
  - **Peristiwa:** Merumuskan masalah *fragile test* dan mencoba implementasi Levenshtein sederhana.
  - **Keputusan:** Menggunakan Levenshtein Distance sebagai baseline komparasi string.
  - **Dampak:** Sering terjadi *False Positive* (tombol terdeteksi sebagai input, dll).
* **Periode Pertengahan (Refactor Besar):**
  - **Peristiwa:** Identifikasi bahwa elemen web memiliki struktur semantik.
  - **Keputusan:** Penambahan fitur **Semantic Guarding** di `HealingDriver`.
  - **Alasan:** Algoritma harus mencegah input *text* *heal* ke input *file*, atau *button* ke *input search*.
  - **Dampak:** Tingkat akurasi meningkat drastis. Penurunan tajam angka *False Positive*.
* **Periode Lanjut:**
  - **Peristiwa:** Pengumpulan data sangat sulit kalau harus *running* lewat terminal terus.
  - **Keputusan:** Dibuatlah `SelfHealingGui.java`.
  - **Dampak:** Eksekusi Maven dengan argumen dinamis menjadi sangat mudah dan representatif. Sangat berguna untuk *screenshot* Bab 4 Skripsi.

---

## 7. RIWAYAT BIMBINGAN DAN REVISI

*(Bagian ini dapat diisi secara spesifik dengan tanggal bimbingan Anda sebenarnya)*
* **Tanggal/Periode:** [Isi sesuai log bimbingan]
* **Arahan:** Harus ada bukti empiris pengujian di lebih dari 1 SUT.
* **Tindakan:** Memasukkan 4 sistem berbeda: Anugrah Jaya, Arsip Dokumen, Sapi Admin, Sapi Client.
* **Status:** Selesai. (GUI dan log sudah mendukung multi-SUT).

---

## 8. PENJELASAN DOMAIN PROYEK

* **Self-Healing:** Mekanisme pemulihan otomatis kode program ketika menemui jalan buntu/error.
* **Locator / Selector:** Alamat unik suatu elemen di Web (ID, XPath, CSS Selector, Name, Class).
* **DOM (Document Object Model):** Struktur hierarki HTML halaman web.
* **False Positive:** Kegagalan sistemik saat *healing*; algoritma merasa *berhasil* menemukan elemen baru, tapi elemen yang di-klik ternyata salah/tidak sesuai peruntukannya.
* **System Under Test (SUT):** Aplikasi/Sistem target yang sedang diuji (*automated test*).
* **Semantic Guarding:** Konsep penyaringan kandidat berdasarkan struktur dan tipe HTML (misal `<input type="text">` hanya boleh ditukar dengan sesama *text input*).

---

## 9. ARSITEKTUR SISTEM

Sistem bekerja dengan mencegat komunikasi antara Test Script dan browser.
```text
[Baseline Test Script (Misal: JUnit/TestNG)]
       ↓ (memanggil findElement)
[AutoHealingWebDriver (Proxy)]
       ↓ (jika sukses) -----> [Browser / Web Driver Asli]
       ↓ (jika NoSuchElementException)
[HealingDriver]
       ↓ (mengumpulkan kandidat elemen interaktif dari DOM)
       ↓ (Filtering dengan Semantic Guarding)
[SimilarityEngine]
       ↓ (Kalkulasi skor: Teks 50%, Locator 30%, Posisi 20%)
[Memilih Elemen Skor Tertinggi (>= Threshold)]
       ↓ 
[HealingLogger] ----> Tulis ke file CSV
       ↓
[Kembalikan Elemen Baru ke Test Script] ---> [Script Berlanjut]
```

---

## 10. PENJELASAN ENGINE / CORE SYSTEM

### Tujuan Engine
Menyediakan modul yang transparan (*invisible*) bagi *tester*, agar kode lama tidak perlu dirombak, namun dapat pulih dari *error locator* yang usang.

### Masalah yang Diselesaikan
*Script* yang langsung melempar error `NoSuchElementException` akan menghentikan seluruh antrian pengetesan (pipeline). Engine ini menahan error tersebut.

### Cara Kerja Umum & Alur Eksekusi
1. *Intercept* perintah pencarian elemen.
2. Jika *error*, buat `ElementProfile` (snapshot dari locator lama).
3. `HealingDriver` membongkar DOM untuk mencari tag interaktif (`<button>`, `<input>`, `<a>`, dll).
4. Saring elemen dengan `isCompatibleElement()` (Semantic Guarding).
5. Elemen yang lolos dikirim ke `SimilarityEngine`.
6. Skor kombinasi dihitung. Jika tertinggi melebihi *threshold* (misal 0.50), healing Sukses. Jika tidak, healing Gagal.

### Komponen Internal Utama

**Nama:** `SimilarityEngine`
* **Tujuan:** Kalkulasi matematis kemiripan.
* **Input:** `ElementProfile` (eksperimen lama) & `WebElement` kandidat.
* **Output:** Angka `double` (0.0 hingga 1.0).
* **Algoritma:** Levenshtein, Substring Matching, Token Coverage.

**Nama:** `HealingDriver`
* **Tujuan:** Orkestrator proses healing dan Semantic Guarding.
* **Input:** *Locator* lama.
* **Output:** *WebElement* baru atau pelemparan *Exception* jika di bawah *threshold*.

**Nama:** `AutoHealingWebDriver`
* **Tujuan:** Membungkus WebDriver asli agar transparan.

**Nama:** `HealingLogger` & `HealingResult`
* **Tujuan:** Mencatat ke `healing_log.csv`. Berisi metrik kesuksesan.

### Limitasi Engine
* Posisi elemen tidak bisa jadi acuan 100% karena jika UI dirombak total, posisinya bergeser.
* Akan melambat (beberapa milidetik) saat menghitung ratusan kandidat elemen pada halaman web yang sangat padat.

---

## 11. ALGORITMA DAN METODOLOGI

* **Algoritma yang digunakan:**
  1. *Levenshtein Distance*: Menghitung berapa banyak perubahan karakter yang dibutuhkan untuk mengubah String A menjadi String B.
  2. *Token Coverage & Substring*: Untuk menangani *refactoring* variabel seperti dari `searchInput` menjadi `productSearchInputRefactor`.
  3. *Semantic Guarding*: Heuristik berbasis logika tipe HTML.
* **Parameter & Threshold:**
  * Bobot skor: Teks 50%, Locator 30%, Posisi 20%.
  * Threshold keberhasilan minimum: `0.50` (Dapat diubah via argumen JVM/GUI).
* **Alasan Pemilihan:** Pendekatan string-based + semantic ini sangat ringan dieksekusi secara lokal dibanding Model AI.

---

## 12. STRUKTUR SOURCE CODE

* `src/main/java/com/fulvian/healing/` -> Engine inti.
* `src/main/java/com/fulvian/gui/` -> GUI Kontrol panel penelitian.
* `src/test/java/com/fulvian/tests/` -> Script *Baseline Test* (skenario pengujian per SUT).
* `results/` -> Lokasi *output file* CSV dari log eksperimen.
* `pom.xml` -> Konfigurasi proyek (Maven).

---

## 13. FILE PALING PENTING

1. **Nama:** `HealingDriver.java`
   - **Tujuan:** Menerapkan *Semantic Guarding* dan alur eksekusi pencarian alternatif. Jantung utama logika seleksi kandidat.
2. **Nama:** `SimilarityEngine.java`
   - **Tujuan:** Berisi parameter bobot skor (Weight) dan algoritma Levenshtein + perhitungan Euclidean Distance.
3. **Nama:** `AutoHealingWebDriver.java`
   - **Tujuan:** *Wrapper* transparan.
4. **Nama:** `SelfHealingGui.java`
   - **Tujuan:** GUI utama untuk memudahkan pengujian.
5. **Nama:** `HealingLogger.java`
   - **Tujuan:** Penyimpan metrik CSV. Sangat krusial untuk data skripsi.

---

## 14. DATA DAN EVIDENCE

* **Evidence Utama:** Log CSV dalam direktori `/results/` (contoh: `healing_log.csv`, `arsip_healing_log.csv`).
* **Fungsi:** Digunakan pada BAB 4 untuk menampilkan perhitungan manual tingkat *Healing Success Rate* dan evaluasi performa waktu.
* **Tabel GUI:** *Screenshot* dari aplikasi `SelfHealingGui` juga merupakan *evidence* implementasi untuk BAB 4.

---

## 15. HASIL YANG SUDAH DIPEROLEH

Berdasarkan *log history* yang ada:
* **Sistem berhasil mengurangi *False Positive***: Dibanding versi awal, penerapan *Semantic Guarding* membuat algoritma tidak lagi keliru memilih elemen interaktif yang berbeda tipe meskipun nilai teksnya mirip.
* **Metrik Kesuksesan (*Healing Success Rate*)**: Menunjukkan angka keberhasilan di atas threshold yang ditargetkan di berbagai SUT.

---

## 16. BUG DAN MASALAH PENTING

* **Bug Historis Terpenting:** Input tipe harga/nama barang sering salah terdeteksi (*heal*) ke *Search Input* global di *navbar*, karena struktur *string*-nya mirip.
* **Solusi/Status Final:** Ditambahkan heuristik di `HealingDriver` (Method `passesInputSemanticGuard` dan pembentukan *target signature*). Ini adalah keputusan arsitektur yang sudah *Selesai* dan berfungsi.

---

## 17. KEPUTUSAN PENTING

* **Keputusan:** Membangun *Wrapper* (Proxy Pattern).
  - **Alasan:** Agar dapat diintegrasikan dengan *Test Script* Selenium konvensional tanpa harus menulis ulang kode aslinya.
* **Keputusan:** Memakai *Semantic Guarding*.
  - **Alasan:** Algoritma Levenshtein buta akan konteks elemen UI. Guarding mencegah kesalahan fatal (*False Positive*).
* **Keputusan:** Menyediakan GUI.
  - **Alasan:** Mempermudah presentasi sidang, demontrasi sistem, dan ekstraksi data bagi dosen penguji, alih-alih menggunakan command prompt `mvn clean test`.

---

## 18. HAL YANG TIDAK BOLEH DISARANKAN LAGI

* **Mencabut Semantic Guarding:** Pendekatan *pure mathematics* komparasi string terbukti tidak relevan di dunia Web DOM.
* **Mengganti Java dengan Python:** Skripsi sudah masuk tahap akhir, ekosistem menggunakan Selenium Java dan Maven.
* **Menggunakan Real-Time AI / OpenAI API saat runtime:** Terlalu lambat untuk skenario *UI automation testing* masif, *flaky network*, dan tidak memenuhi syarat performansi lokal dari penelitian ini.

---

## 19. STRUKTUR DOKUMEN PENELITIAN (SKRIPSI)

* **BAB 1 (Pendahuluan):** Asumsi Selesai. Latar belakang *fragile test* dan batasan masalah.
* **BAB 2 (Tinjauan Pustaka):** Asumsi Selesai. Teori *Software Testing*, DOM, Levenshtein, dll.
* **BAB 3 (Metodologi):** Asumsi Selesai. Rancangan sistem *wrapper* dan alur flowchart.
* **BAB 4 (Implementasi & Evaluasi):** **SEDANG DIKERJAKAN**. Menampilkan *evidence* implementasi `HealingDriver`, `SimilarityEngine`, `GUI`, serta analisis *table log* CSV dan grafik *Success Rate*.
* **BAB 5 (Kesimpulan):** **BELUM SELESAI**. Merangkum bahwa algoritma campuran (String + Semantic + Posisi) terbukti dapat diandalkan.

---

## 20. METRIK DAN EVALUASI

* **Healing Success Rate (HSR):** `(Total Healing Sukses / Total Usaha Healing) x 100%`
* **False Positive Rate (FPR):** `(Total Salah Pilih Elemen / Total Healing Sukses) x 100%` (Dicek secara manual dari hasil screenshot/visual sistem).
* **Average Healing Time:** Waktu (dalam milidetik) yang dihabiskan algoritma mengevaluasi DOM. Digunakan untuk membuktikan bahwa komputasi sistem ini relatif ringan (*overhead* kecil).

---

## 21. RISIKO DAN KETERBATASAN

* **Overhead Eksekusi:** Ada *delay* beberapa ms hingga 1-2 detik ketika *healing* sedang berjalan.
* **Batas Toleransi Threshold:** Jika perubahan UI terlalu ekstrem/radikal (berubah desain 100%), *score* akan berada di bawah 0.50 dan sistem memilih untuk gagal (*Fail Fast*) demi keamanan integritas test.

---

## 22. NEXT ACTION

1. **Pekerjaan:** Kompilasi Data Hasil Log per SUT (Anugrah Jaya, dsb).
   - **Alasan:** Diperlukan untuk isi tabel analisis Bab 4.
   - **Prioritas:** SANGAT TINGGI.
2. **Pekerjaan:** Pengambilan Screenshot GUI saat memproses script rusak.
   - **Alasan:** Bukti validasi sistem berjalan.
   - **Prioritas:** TINGGI.
3. **Pekerjaan:** Merangkum narasi hasil akhir untuk Bab 5.
   - **Alasan:** Menyelesaikan draf skripsi.
   - **Prioritas:** SEDANG.

---

## 23. PANDUAN UNTUK AI BARU

Jika AI baru diinisialisasi dalam percakapan:
1. **Baca file ini (CONTEXT.md) terlebih dahulu.**
2. Pahami bahwa *engine* utama berpusat pada `SimilarityEngine.java` (Logika Matematis) dan `HealingDriver.java` (Logika *Semantic Guard*).
3. **Fokus saat ini:** Fokus membantu merapikan kalimat akademis (skripsi), *formatting* laporan Bab 4 dan 5, dan tidak menyarankan perombakan arsitektur besar-besaran karena proyek sudah *final stage*.
4. Jangan menyarankan penggunaan algoritma ML (Machine Learning), karena skripsi ini khusus menganalisis metode heuristik & *string distance*.

---

## 24. PANDUAN PRESENTASI DAN SIDANG

### Penjelasan 1 Menit (Elevator Pitch)
"Penelitian ini menghasilkan sebuah *tools/wrapper* cerdas untuk Selenium WebDriver. Jika sebuah *test script* gagal karena elemen Web berubah nama/ID, alat ini tidak langsung melempar error, melainkan membongkar HTML, mencari kandidat terdekat secara semantik dan teks, dan secara otomatis memulihkan (*healing*) jalannya *test* tanpa campur tangan manusia."

### Penjelasan 3 Menit
(Sama dengan di atas ditambah): "Poin krusial penelitian ini adalah penambahan fitur *Semantic Guarding*. Tanpa *guarding*, algoritma pencocokan teks biasa sering salah mengklik elemen yang teksnya mirip tapi fungsinya berbeda (contoh: *text input* ditukar dengan *file upload*). Sistem ini mengkombinasikan metode matematis Levenshtein Distance dan logika heuristik DOM untuk menghasilkan *healing* yang cepat, akurat, dan sangat meminimalisir kesalahan (*False Positive*)."

### FAQ yang Mungkin Ditanyakan Dosen
* **Tanya:** Mengapa pakai Levenshtein, tidak AI/Machine Learning saja?
  * **Jawab:** AI/ML menambah waktu latensi (lambat karena butuh request jaringan/GPU lokal) yang tidak cocok untuk CI/CD pipeline yang mengutamakan kecepatan. Algoritma matematis dan heuristik ini terbukti ringan dan cukup efektif mengatasi refactoring wajar di industri.
* **Tanya:** Bagaimana memastikan elemen yang diklik benar-benar elemen yang dimaksud, bukan elemen kebetulan mirip?
  * **Jawab:** Itulah tujuan utama *Semantic Guarding* pada class `HealingDriver`. Kami secara ketat melarang elemen *button* tertukar dengan *input*, memastikan ekspektasi tipe data terjaga. Kami juga mengecek tingkat *False Positive* pada log CSV.
* **Tanya:** Apa output terukur (Metrik) dari skripsi ini?
  * **Jawab:** Ada 3: *Healing Success Rate* (%), *False Positive Rate* (%), dan *Average Healing Time* (ms). Seluruhnya dicatat transparan di tabel `healing_log.csv`.
