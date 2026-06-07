# Prompt BE — Fix semantik `hasStartedAt` di `GET /me/queue`

> Tempel prompt ini ke sesi BE. Konteks: FE lagi bangun halaman antrian operator
> (`/app/queue`) yang butuh tahu apakah **step yang sedang aktif** sudah di-`STEP_STARTED`
> atau belum, buat nentuin label 1-tombol: **"Mulai"** vs **"Tandai selesai"**.

---

## Masalah

`GET /me/queue` sekarang mengembalikan field `hasStartedAt` per item, **tapi nilainya
salah semantik**: dia mengembalikan timestamp **start batch keseluruhan** (event
`STEP_STARTED` paling awal di batch), BUKAN start dari **step yang sedang aktif**
(current step entry).

Bukti konklusif (hasil curl 6 Juni 2026, semua operator yang punya antrian):

| operator | batch | current step | `hasStartedAt` | `enteredAt` (masuk step ini) |
|---|---|---|---|---|
| op.sablon | 4 | SABLON_BORDIR | 2026-05-18T09:00 | 2026-05-29T09:00 |
| op.design | 6 | DESIGN | 2026-05-20T10:00 | 2026-05-28T14:00 |
| op.cutting | 5 | CUTTING | 2026-05-22T09:00 | 2026-05-29T13:00 |
| op.sewing | 3 | SEWING | 2026-05-15T10:00 | 2026-05-28T08:00 |

Di **semua** baris: `hasStartedAt < enteredAt`. Ini **mustahil** kalau `hasStartedAt`
beneran "kapan step saat ini dimulai" — sebuah step gak mungkin dimulai sebelum batch
masuk ke step itu. Artinya field ini sekarang ngambil start batch, bukan start step aktif.

**Dampak ke FE:** field ini selalu keisi (non-null) untuk setiap batch `IN_PROGRESS`,
jadi tombol queue akan **selalu** "Tandai selesai" dan **tidak pernah** "Mulai".
Tidak bisa dipakai.

## Yang dibutuhkan (semantik benar)

`hasStartedAt` harus merefleksikan **current step entry**, yaitu event `STEP_STARTED`
untuk step yang sedang aktif sekarang:

- **`null`** → batch sudah masuk current step (`enteredAt` terisi) **tapi belum** ada
  `STEP_STARTED` untuk step itu. FE render tombol **"Mulai"** (`STEP_STARTED`).
- **timestamp terisi** → sudah ada `STEP_STARTED` untuk current step. FE render tombol
  **"Tandai selesai"** (`STEP_COMPLETED`).

Catatan implementasi:
- Ambil event `STEP_STARTED` **terbaru** yang `workflow_step_id = current_step_id` **dan**
  terjadi pada entry step yang sekarang (yaitu `performed_at >= enteredAt`). Kalau gak ada,
  `null`.
- Invariant yang harus selalu benar: `hasStartedAt == null OR hasStartedAt >= enteredAt`.
- (Opsional, kalau gampang) sekalian tambahkan boolean turunan `hasStarted` sesuai spec
  `QueueItem` (`hasStarted = hasStartedAt != null`). FE bisa pakai salah satu; yang
  penting semantiknya per-current-step. Kalau cuma satu, **timestamp `hasStartedAt`
  dengan semantik benar sudah cukup.**

## Acceptance criteria

1. Untuk batch yang current step-nya **sudah** di-`STEP_STARTED`: `hasStartedAt` =
   waktu `STEP_STARTED` step itu, dan `hasStartedAt >= enteredAt`.
2. Untuk batch yang **baru masuk** current step tapi **belum** di-start:
   `hasStartedAt == null`.
3. Tidak ada lagi kasus `hasStartedAt < enteredAt`.

## Cara verifikasi (curl)

```bash
# 1. Login operator
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"op.sablon","password":"password123"}' \
  | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['accessToken'])")

# 2. Lihat queue — cek hasStartedAt vs enteredAt
curl -s http://localhost:8080/api/v1/me/queue -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

# 3. Test kasus "belum start": ambil batch yang current step-nya belum di-STEP_STARTED.
#    Caranya, selesaikan current step satu batch (POST STEP_COMPLETED) supaya batch maju
#    ke step berikutnya (entered tapi belum started), lalu login operator divisi tujuan &
#    cek /me/queue → hasStartedAt HARUS null.
#    (Re-seed setelah test biar bersih.)
```

## Konteks tambahan

- Spec `QueueItem` (`sevenspeed-openapi-v0.1.yaml`) mendefinisikan `hasStarted` (boolean):
  *"TRUE kalau sudah ada STEP_STARTED untuk step entry ini"* — semantik per-step entry
  inilah yang benar. Field `hasStartedAt` cukup asal mengikuti semantik yang sama.
- Field lain di `/me/queue` sudah benar dan dipakai FE apa adanya: `isUrgent` (computed
  BE, deadline < 3 hari), `enteredAt` (FIFO sort), `batch`, `currentStep`, `orderNumber`,
  `orderTitle`, `customerName`, `deadlineDate`.
