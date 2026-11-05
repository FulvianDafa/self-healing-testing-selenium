# Security and Safe Testing

## Credentials

- GUI tidak lagi meneruskan email/password lewat argumen Maven `-DadminEmail` atau `-DadminPassword`: argumen proses dapat terbaca oleh pengguna/sistem lain dan sebelumnya sempat ditampilkan di output GUI.
- Kredensial GUI diteruskan ke proses Maven sebagai environment `ADMIN_EMAIL` dan `ADMIN_PASSWORD`. Pada custom test class, baca dengan `System.getenv("ADMIN_EMAIL")` dan `System.getenv("ADMIN_PASSWORD")`.
- Environment **bukan** vault: proses anak tetap dapat mengaksesnya. Hindari dump environment, console yang mencetak kredensial, dan laporan test yang memuat password/token.
- Jangan commit `.env`, credentials, cookies, session token, data produksi, maupun screenshot pribadi. Periksa file log **dan riwayat Git** sebelum membagikan link repository.
- Jika pernah terpublikasi credential asli, lakukan rotasi credential; menghapus dari commit terbaru saja tidak menghapus riwayat.

## Public SUTs

- Jalankan tes hanya pada website dan akun yang memang boleh diuji. Hindari aksi pembelian, submit data asli, unggah berkas sensitif, atau mengganggu layanan publik.
- Simulasi DOM melalui JavaScript browser bersifat sementara, tetapi aksi klik/form yang dilakukan Selenium dapat menimbulkan efek di server.
- CAPTCHA, anti-bot, blocked requests, timeout, atau perubahan DOM bukan selalu kegagalan engine; catat sebagai precondition/blocked secara terpisah.
- Tes lokal `TestLocalDemoHealing` menggunakan fixture yang disertakan sehingga tidak mengubah data SUT produksi.

## Reporting

Jika menemukan masalah keamanan, jangan taruh token/kredensial maupun exploit detail pada issue publik. Hubungi pemilik repository melalui kanal GitHub yang sesuai.
