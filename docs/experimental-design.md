# Rancangan Eksperimen

Dokumen ini menjelaskan desain **sebagaimana digunakan pada repository**; ini bukan proposal eksperimen baru. Penelitian membandingkan skrip Selenium biasa (baseline) dan skrip dengan pemulihan locator. Perubahan atribut DOM disimulasikan pada sesi browser, bukan dengan mengubah server aplikasi.

## Alur yang diuji

1. Siapkan halaman dan identifikasi elemen yang semula bisa ditemukan.
2. Mutasi ID/class/locator target sesuai skenario tanpa mengubah fungsi elemen.
3. Jalankan locator lama. Pada baseline, perubahan diharapkan menyebabkan pencarian gagal.
4. Pada mode healing, `HealingDriver` mengumpulkan kandidat, memfilter tag/tipe/visibilitas, menghitung skor similarity, dan mengembalikan kandidat jika melewati threshold.
5. Verifikasi elemen yang dipilih lewat assertion atau ground truth yang independen. Kandidat diterima **bukan otomatis benar**.
6. Catat skor, status, waktu healing, serta hasil skenario; pisahkan kegagalan prasyarat sebelum healing.

Bobot produksi: text **0,50**, locator **0,30**, position **0,20**; default threshold **0,50**. Jika posisi target sebelumnya tidak tersedia, bobot text dan locator dinormalisasi terhadap total **0,80**.

## Cakupan dan sumber hasil

- **Data skripsi historis:** 23 SUT, 104 healing attempts, 96 diterima (HSR 92,31%). Ini bukan hasil rerun Oktober.
- **Rerun reviewer 2 Oktober 2026:** 23 SUT eksternal, 129 skenario dirancang/dieksekusi oleh JUnit, 88 mencapai proses healing, 81 diterima (HSR 92,05%). **41** skenario terhalang sebelum healing.
- Dari 74 kandidat diterima yang punya label objektif pada rerun, 8 adalah false positive: FPR berlabel = **10,81%**. Tujuh kandidat diterima tidak punya label objektif sehingga **jangan** diklaim benar.
- Waktu rerun: mean 18.390,67 ms, median 9.301 ms, SD 19.719,53 ms, rentang 494–79.555 ms. Nilai tersebut mengukur healing, bukan total execution overhead.

Sumber audit: [reviewer-analysis-report](../results/reviewer-analysis-report.md) dan [final_dataset_summary](../results/final_dataset_summary.csv). Ketersediaan SUT, consent, CAPTCHA, dan perubahan DOM situs publik dapat menyebabkan hasil berubah. Pada rerun, semua attempt tidak memiliki snapshot posisi sehingga pengaruh bobot posisi tidak bisa disimpulkan secara independen.

## Menjalankan ulang

```powershell
mvn -B -ntp clean test
mvn test "-Dtest=TestLocalDemoHealing"
# Hanya bila SUT publik/lingkungan dan izin pengujian memadai:
.\run_reviewer_analysis.ps1
python reviewer_analysis.py
```

Runner multi-SUT menghapus log healing/kandidat aktif yang sesuai sebelum rerun: **backup hasil penelitian terlebih dahulu**. Jangan menjalankan tes destruktif pada aplikasi produksi.
