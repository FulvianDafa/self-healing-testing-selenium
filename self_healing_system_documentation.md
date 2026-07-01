# Dokumentasi Sistem Self-Healing Selenium

Dokumen ini berisi penjelasan mendalam mengenai arsitektur, sistem, dan engine yang digunakan dalam proyek penelitian "Self-Healing Selenium". Dokumen ini dirancang untuk memberikan konteks yang komprehensif bagi LLM untuk riset lebih lanjut.

## 1. Package `com.fulvian.healing` (Inti Engine)

### `SimilarityEngine.java`
**Peran Sistem:** Merupakan "otak" atau mesin algoritma utama dari proses self-healing.
**Cara Kerja:**
- Menghitung skor kemiripan (*similarity score*) antara target elemen asli dengan elemen kandidat yang ditemukan di DOM.
- Menggunakan 3 atribut utama dengan bobot berbeda:
  1. **Kemiripan Teks (Bobot 50%):** Membaca teks yang terlihat, `placeholder`, `aria-label`, `name`, `id`, atau `value`.
  2. **Kemiripan Locator (Bobot 30%):** Membandingkan *string* locator asli dengan struktur locator kandidat.
  3. **Kemiripan Posisi (Bobot 20%):** Menggunakan *Euclidean distance* untuk menghitung kedekatan posisi elemen secara koordinat visual.
- Pendekatan perhitungan (*Smart Similarity*): Menggabungkan algoritma *Levenshtein Distance*, *Substring Matching*, dan *Token Coverage* agar tahan terhadap perubahan drastis seperti *refactoring* variabel (contoh: dari `searchInput` menjadi `productSearchDashboardRefactor`).

### `HealingDriver.java`
**Peran Sistem:** Sebagai eksekutor atau pengeksekusi algoritma pencarian elemen pengganti.
**Cara Kerja:**
- Awalnya mencoba mencari elemen dengan locator asli. Jika gagal (`NoSuchElementException`), mode *self-healing* akan diaktifkan.
- **Pengumpulan & Filtering (*Semantic Guarding*):** Mengumpulkan seluruh kandidat elemen interaktif di DOM (seperti `<button>`, `<input>`, `<select>`, dll). Sebelum skor dihitung, sistem memfilter elemen secara semantik agar sesuai dengan target. Misalnya, *input type text* tidak akan dialihkan ke *input type file*, atau tombol *submit* tidak akan di-healing ke kolom pencarian teks.
- **Eksekusi Healing:** Mengirim elemen yang lolos *guarding* ke `SimilarityEngine`. Elemen dengan skor tertinggi dan berada di atas *threshold* yang ditetapkan akan dipilih sebagai elemen *healed*, dicatat, lalu dikembalikan ke sistem agar testing tetap berlanjut tanpa error.

### `AutoHealingWebDriver.java`
**Peran Sistem:** Proxy transparan (*wrapper*) di atas *instance* `WebDriver` asli.
**Cara Kerja:**
- Meng-*intercept* atau mencegat seluruh instruksi pencarian elemen (`driver.findElement()`).
- Jika elemen gagal ditemukan dan mode *healing* aktif, kelas ini secara cerdas akan membangun objek `ElementProfile`.
- Sistem mendeteksi ekspektasi tag, teks, dan atribut hanya bermodalkan *string locator* lama. Dengan cara ini, tester tidak perlu mengubah satupun baris kode *test script baseline* yang lama; *self-healing* bekerja di belakang layar (*under-the-hood*).

### `ElementProfile.java`
**Peran Sistem:** Objek penyimpan status (*Snapshot*) spesifikasi elemen.
**Cara Kerja:** Menyimpan data atribut elemen yang dicari (seperti tipe locator, nilai locator asli, dan estimasi teks) sebagai *baseline/blueprint* yang dikirimkan ke *engine similarity* saat proses identifikasi kandidat berlangsung.

### `HealingLogger.java` & `HealingResult.java`
**Peran Sistem:** Modul pencatatan (*Logging* & Telemetri).
**Cara Kerja:**
- Segala sesuatu yang terjadi saat sistem melakukan *healing* disimpan ke dalam objek memori `HealingResult` (termasuk *timestamp*, *score*, waktu yang dihabiskan untuk *healing*, dan *locator* baru).
- `HealingLogger` bertugas secara rekursif menulis data tersebut ke *file log* berbasis format CSV (misal: `results/healing_log.csv`). File CSV ini merupakan artefak bukti utama yang nantinya akan dievaluasi untuk mendapatkan metrik penelitian (seperti *Healing Success Rate*, waktu rata-rata, dan *False Positive Rate*).

---

## 2. Package `com.fulvian.gui` (Antarmuka Pengguna)

### `SelfHealingGui.java`
**Peran Sistem:** Antarmuka grafis (GUI) pendukung kontrol eksperimen.
**Cara Kerja:**
- Dibangun dengan Java Swing, dirancang agar peneliti atau *tester* dapat menjalankan *script test* dengan parameter yang berbeda tanpa harus melalui konfigurasi baris perintah (*CLI*).
- Memungkinkan pengguna untuk memilih *project root*, letak file *script baseline*, target sistem yang diuji (SUT), serta mengubah batas toleransi *threshold*.
- Ketika tombol eksekusi ditekan, *GUI* merangkai perintah eksekusi otomatis Maven (menginjeksikan konfigurasi Java properties `-D`), menjalankan *thread* eksekutor di latar belakang, lalu membaca *output console* serta meload data hasil log CSV ke dalam bentuk tabel *grid* secara *real-time*.

---
**Catatan Penting Untuk Riset Lanjutan:**
Poin kontribusi utama (*novelty*) dari implementasi proyek ini terletak pada kolaborasi antara **Smart Text Similarity** di `SimilarityEngine` dengan **Semantic Guarding / Filter Kompatibilitas** di `HealingDriver`. Implementasi *Semantic Guarding* memecahkan masalah kelemahan *Levenshtein Distance* konvensional dengan memblokir kandidat elemen interaktif ber-tag beda (mencegah fenomena *False Positive* yang tinggi) secara kontekstual. Ini yang membedakan *engine* ini dari sekadar pembanding *string locator* biasa.
