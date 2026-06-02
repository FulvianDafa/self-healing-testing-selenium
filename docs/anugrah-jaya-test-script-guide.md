# Panduan Test Script ANUGRAH_JAYA

File ini menjelaskan script pengujian yang sudah ditambahkan ke project `self-healing-selenium`.

## File utama yang ditambahkan

```text
src/test/java/com/fulvian/tests/TestAnugrahJayaHealing.java
src/test/java/com/fulvian/tests/TestAnugrahJayaBaseline.java
```

File yang ikut disesuaikan:

```text
src/test/java/com/fulvian/base/BaseTest.java
pom.xml
```

## Cara menjalankan

Dari root project Selenium:

```bash
mvn test -DbaseUrl=http://anugrah_jaya.test/app/index.html
```

Kalau project web memakai host lain, ganti `baseUrl`:

```bash
mvn test -DbaseUrl=http://anugrah_jaya.test/app/index.html
```

Mode headless:

```bash
mvn test -DbaseUrl=http://anugrah_jaya.test/app/index.html -Dheadless=true
```


untuk suite self healing
```
mvn clean test -Dtest=TestAnugrahJayaHealing -DbaseUrl=http://anugrah_jaya.test/app/index.html
```

untuk suite baseline 
```
mvn clean test -Dtest=TestAnugrahJayaBaseline -DbaseUrl=http://anugrah_jaya.test/app/index.html
```


## Output eksperimen

Self-healing:

```text
results/healing_log.csv
```

Baseline tanpa self-healing:

```text
results/anugrah_baseline_log.csv
```

## Pola eksperimen

1. `TestAnugrahJayaBaseline` menjalankan Selenium biasa tanpa `HealingDriver`.
2. Locator yang sudah berubah akan gagal dan dicatat sebagai baseline failure.
3. `TestAnugrahJayaHealing` menjalankan locator lama melalui `HealingDriver`.
4. Jika locator lama gagal, sistem mencari kandidat elemen dengan similarity matching.
5. Hasil healing dicatat ke CSV.

## Skenario yang sudah ada

| ID | Halaman | Fokus |
|---|---|---|
| SH-001 | Produk | Tombol Tambah Produk berubah id |
| SH-004 | Produk | Input Nama Barang berubah id |
| SH-010 | Produk | Tombol Simpan berubah id |
| SH-011 | Produk | Search produk berubah id |
| SH-013 | Produk | Tombol Edit dinamis / XPath lama rusak |
| SH-020 | Transaksi | Search barang berubah id |
| SH-021 | Transaksi | Tombol Simpan Transaksi berubah id |
| SH-022 | Transaksi | Input Jumlah Bayar berubah id |
| SH-030 | Utang | Tombol Tambah Pengutang berubah id |
| SH-031 | Utang | Input Nama Pengutang berubah id |
| SH-040 | Dashboard | Tab Data Utang berubah id |
| SH-041 | Dashboard | Search produk dashboard berubah id |
| SH-050 | Print Daftar Barang | Search input berubah id |
| SH-051 | Print Daftar Barang | Tombol layout tabel berubah id |

## Catatan skripsi

Untuk Bab 4, bandingkan:

- failure rate dari `anugrah_baseline_log.csv`
- healing success rate dari `healing_log.csv`
- average healing time dari `healing_log.csv`
- false positive rate setelah verifikasi manual elemen yang dipilih
