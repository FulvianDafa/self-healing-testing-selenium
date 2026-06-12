# Test Script Sapi Berkah Amanah untuk Repo `self-healing-selenium`

Paket ini berisi test script baseline dan self-healing yang sudah disesuaikan dengan struktur repo utama `self-healing-selenium` milik Fulvian.

## File yang ditambahkan

Copy ke folder:

```text
src/test/java/com/fulvian/tests/
```

Daftar file:

```text
TestSapiClientBaseline.java
TestSapiClientHealing.java
TestSapiAdminBaseline.java
TestSapiAdminHealing.java
```

## Status integrasi

Script ini sudah memakai komponen utama repo self-healing:

```text
com.fulvian.healing.ElementProfile
com.fulvian.healing.HealingDriver
com.fulvian.healing.HealingLogger
```

Jadi tidak lagi memakai helper `SapiSelfHealingSupport.java` dari draft sebelumnya.

## Catatan penting untuk Client Side Vue

Pada project Vue Sapi Berkah Amanah, beberapa elemen interaktif belum memiliki `id` stabil. Supaya tetap bisa menguji skenario perubahan `id`, script memberi temporary id melalui JavaScript pada DOM browser Selenium, lalu mengubahnya menjadi id baru.

Contoh:

```text
clientHeroBuyButton -> clientHeroBuyButtonRefactor
```

Perubahan ini hanya terjadi di browser Selenium, bukan pada source code Vue dan bukan pada file hosting/local project.

## Command menjalankan Client Side

Pastikan frontend Vue sudah jalan:

```bash
cd frontend
npm install
npm run dev
```

URL default Vite biasanya:

```text
http://localhost:5173
```

Jalankan baseline:

```bash
mvn clean test -Dtest=TestSapiClientBaseline -DbaseUrl=http://localhost:5173
```

Jalankan self-healing:

```bash
mvn clean test -Dtest=TestSapiClientHealing -DbaseUrl=http://localhost:5173
```

## Command menjalankan Admin Side

Pastikan backend Laravel sudah jalan:

```bash
cd backend
composer install
cp .env.example .env
php artisan key:generate
php artisan migrate --seed
php artisan serve
```

Akun default dari seeder project:

```text
admin@example.com / admin123
```

Jalankan baseline:

```bash
mvn clean test "-Dtest=TestSapiAdminBaseline" "-DadminUrl=http://localhost:8000" "-DadminEmail=admin@example.com" "-DadminPassword=admin123"
```

Jalankan self-healing:

```bash
mvn clean test "-Dtest=TestSapiAdminHealing" "-DadminUrl=http://localhost:8000" "-DadminEmail=admin@example.com" "-DadminPassword=admin123"
```

## Output log

Baseline:

```text
results/sapi_client_baseline_log.csv
results/sapi_admin_baseline_log.csv
```

Self-healing:

```text
results/sapi_client_healing_log.csv
results/sapi_admin_healing_log.csv
```

## Skenario Client Side

```text
SBA-CL-001 Landing: tombol BELI SEKARANG
SBA-CL-002 Navbar: link FAQ
SBA-CL-003 Landing: link DAFTAR SEKARANG
SBA-CL-004 Reseller: input Nama Lengkap
SBA-CL-005 Reseller: tombol Cancel
```

## Skenario Admin Side

```text
SBA-AD-001 Login: input email
SBA-AD-002 Login: tombol Sign in
SBA-AD-003 Sidebar: link Hewan Kurban
SBA-AD-004 Hewan Kurban: filter jenis hewan
SBA-AD-005 Hewan Kurban: tombol Tambah Hewan
SBA-AD-006 Form Hewan: input nama
SBA-AD-007 Form Hewan: input harga display
```

## Catatan validasi

Saya tidak menjalankan Maven test di environment ChatGPT karena command `mvn` tidak tersedia di sandbox. Namun file sudah disusun mengikuti pola `TestArsipDokumenBaseline.java` dan `TestArsipDokumenHealing.java` pada repo utama.
