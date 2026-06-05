# Catatan Refactor Arsip Dokumen Self-Healing

## Tujuan

Refactor ini menghapus perhitungan similarity lokal dari `TestArsipDokumenHealing.java` dan menggantinya dengan komponen utama framework self-healing:

- `HealingDriver`
- `SimilarityEngine`
- `ElementProfile`
- `HealingLogger`

Dengan perubahan ini, skenario Arsip Dokumen menggunakan mekanisme self-healing yang sama dengan SUT Anugrah Jaya. Perbedaan antar SUT hanya berada pada test script, alur halaman, dan locator target.

## File yang diubah

### `src/test/java/com/fulvian/tests/TestArsipDokumenHealing.java`

Perubahan:
- Menghapus helper similarity lokal seperti `findHealedElement`, `calculateScore`, `similarity`, dan `levenshtein`.
- Mengganti proses healing menjadi pemanggilan `HealingDriver.findElement(By, ElementProfile)`.
- Menambahkan `ElementProfile` pada tiap skenario agar similarity score dihitung oleh `SimilarityEngine` utama.
- Memisahkan log Arsip ke `results/arsip_healing_log.csv` melalui system property `healing.log.file`.
- Memperbaiki navigasi APBN agar membuka halaman tahun APBN dengan klik link/card tahun, bukan langsung `driver.get(baseUrl + "/apbn/" + year)`.

### `src/main/java/com/fulvian/healing/HealingLogger.java`

Perubahan:
- Menambahkan dukungan output log dinamis melalui system property `healing.log.file`.
- Default tetap `results/healing_log.csv` sehingga test Anugrah Jaya tetap kompatibel.
- Test Arsip dapat memakai `results/arsip_healing_log.csv` tanpa membuat logger lokal baru.

## Cara menjalankan

```bash
mvn clean test -Dtest=TestArsipDokumenHealing -DbaseUrl=http://127.0.0.1:8000
```

Log hasil healing Arsip akan tersimpan di:

```text
results/arsip_healing_log.csv
```

## Catatan lanjutan

Perbaikan status log seperti `HEALING_SUCCESS`, `TEST_PASSED`, `TEST_ERROR`, dan `FALSE_POSITIVE` belum diterapkan di refactor ini. Itu sebaiknya menjadi perbaikan berikutnya agar log dapat membedakan keberhasilan locator healing dan keberhasilan test case secara keseluruhan.
