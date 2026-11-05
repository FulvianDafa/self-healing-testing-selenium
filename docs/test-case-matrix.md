# Matriks Test Case dan Cakupan SUT

Sumber: [final_dataset_summary.csv](../results/final_dataset_summary.csv) dari rerun reviewer tanggal **2 Oktober 2026**. Setiap nama kelas di bawah ada dalam `src/test/java/com/fulvian/tests/`. Kolom **Reached healing** bukan jumlah test JUnit yang lulus.

| SUT | Test class | Skenario dirancang | Reached healing | Blocked before healing |
| --- | --- | ---: | ---: | ---: |
| Amazon | TestAmazonHealing | 5 | 5 | 0 |
| Bhinneka | TestBhinnekaHealing | 5 | 2 | 3 |
| Bing | TestBingHealing | 10 | 10 | 0 |
| Bisnis | TestBisnisHealing | 5 | 3 | 2 |
| Cekresi | TestCekresiHealing | 5 | 4 | 1 |
| CNN Indonesia | TestCnnIndoHealing | 5 | 3 | 2 |
| Dicoding | TestDicodingHealing | 5 | 3 | 2 |
| eBay | TestEbayHealing | 5 | 2 | 3 |
| Indeed | TestIndeedHealing | 5 | 4 | 1 |
| Katadata | TestKatadataHealing | 5 | 5 | 0 |
| Kompas | TestKompasHealing | 5 | 2 | 3 |
| Niagahoster | TestNiagahosterHealing | 5 | 1 | 4 |
| OrangeHRM | TestOrangeHrmHealing | 10 | 10 | 0 |
| Rumah123 | TestRumah123Healing | 5 | 5 | 0 |
| SauceDemo | TestSauceDemoHealing | 10 | 8 | 2 |
| Target | TestTargetHealing | 5 | 4 | 1 |
| Tiket | TestTiketHealing | 5 | 3 | 2 |
| Tokopedia | TestTokopediaHealing | 5 | 1 | 4 |
| Traveloka | TestTravelokaHealing | 5 | 3 | 2 |
| Walmart | TestWalmartHealing | 5 | 2 | 3 |
| Wikipedia | TestWikipediaHealing | 4 | 2 | 2 |
| Yahoo | TestYahooHealing | 5 | 3 | 2 |
| YouTube | TestYoutubeHealing | 5 | 3 | 2 |
| **Total** | | **129** | **88** | **41** |

Detail ID scenario, jenis mutasi, locator asli, target, dan assertion harus dilihat pada kelas masing-masing. Hasil rerun **tidak menggantikan** dataset historis 104 attempts yang dipakai dalam skripsi; lihat [rancangan eksperimen](experimental-design.md).
