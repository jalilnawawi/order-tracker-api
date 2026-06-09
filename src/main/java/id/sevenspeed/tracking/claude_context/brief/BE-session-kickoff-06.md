# Prompt Kickoff — Sesi BE Sevenspeed (9 Juni 2026)

> Copas seluruh blok di bawah ke sesi backend. Self-contained (sesi BE beda repo,
> gak diandelin baca file FE). Dua bug BE dari sesi FE, di-prioritas.
> Detail lengkap per-isu juga ada di `BE-fix-me-orders-timeline-500.md` (P1) dan
> `BE-fix-queue-hasStartedAt-semantics.md` (P2 sudah RESOLVED, jangan diregresi).

---

```
Halo, ini sesi backend Sevenspeed (order tracker). Kamu backend dev. Sebelum mulai:
baca commit terakhir + dokumentasi/kontrak repo BE biar nyambung. Stack & base URL:
http://localhost:8080/api/v1. Login dibungkus `data` → data.accessToken.
Semua user password: password123.

Dari sesi FE ada 2 bug BE yang nge-block / ngeganggu. Urut prioritas:

══════════════════════════════════════════════════════════════════════
P1 — GET /me/orders/{id}/timeline → 500 INTERNAL_ERROR untuk SEMUA order
══════════════════════════════════════════════════════════════════════
Endpoint precomputed timeline customer mati total. FE kepaksa compose timeline
sendiri dari /batches/{id} + /workflows/{wfId}/steps + /batches/{id}/progress-events
(3 call, tanpa field `message` ramah, dan customer jadi ikut akses endpoint staff-ish).
Begitu endpoint ini sehat, FE bakal swap balik ke 2-call precomputed.

Bukti (login smpn1.kudus, masih 500 per 9 Juni):
  order 1 (COMPLETED)  -> 500
  order 5 (IN_PROGRESS)-> 500
  order 10 (DRAFT)     -> 500
Body: {"success":false,"error":{"code":"INTERNAL_ERROR","message":"An unexpected error occurred"}}

Sibling endpoint SEHAT (200, JWT customer): GET /me/orders, GET /me/orders/{id},
GET /batches/{id}, GET /batches/{id}/progress-events, GET /workflows/{id}/steps.
Jadi datanya ADA — kemungkinan bug murni di handler timeline ini. Tolong telusuri
stacktrace/log 500-nya dulu sebelum nebak.

Shape yang diharapkan FE (kontrak §6), array tahap urut by sequence:
  { stepName, status: DONE|CURRENT|UPCOMING|FAILED|ON_HOLD, message (ramah, BI), at? }
Tanpa nama operator (privasi customer).

Acceptance:
 1. 200 untuk order COMPLETED, IN_PROGRESS, dan DRAFT (DRAFT = semua tahap UPCOMING,
    bukan 500).
 2. Tiap tahap punya `status` precomputed + `message` ramah BI, tanpa nama operator.
 3. 404 wajar kalau order bukan milik customer / tidak ada (bukan 500).

Verifikasi:
  TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
    -H 'Content-Type: application/json' \
    -d '{"username":"smpn1.kudus","password":"password123"}' \
    | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['accessToken'])")
  for oid in 1 5 10; do echo "== order $oid =="; \
    curl -s http://localhost:8080/api/v1/me/orders/$oid/timeline \
    -H "Authorization: Bearer $TOKEN" | python3 -m json.tool; done

══════════════════════════════════════════════════════════════════════
P2 — Batch gak maju setelah STEP_COMPLETED (step advancement)
══════════════════════════════════════════════════════════════════════
Gejala dari sesi FE: setelah operator POST STEP_COMPLETED untuk current step, batch
TIDAK advance ke step berikutnya di workflow (currentStep nyangkut). Belum ditelusuri
detail dari sisi BE — perlu reproduce.

Reproduce (login operator divisi yang punya antrian, mis. op.sewing untuk batch 3):
 1. GET /me/queue → catat batch + currentStep + apakah hasStartedAt null.
 2. Kalau belum start: POST STEP_STARTED current step. Lalu POST STEP_COMPLETED.
 3. Cek ulang: batch HARUSNYA pindah ke step berikutnya (enteredAt step baru terisi,
    currentStep berubah). Sekarang dugaannya gak pindah.
Telusuri: apakah STEP_COMPLETED nulis event tapi gak update current_step pointer /
gak bikin entry step berikutnya? Cek juga kasus step terakhir → order COMPLETED.

Catatan residu seed (dari sesi FE, hati-hati saat reproduce):
 - Batch 4 ada extra STEP_COMPLETED event, stuck di SABLON_BORDIR.
 - Order 7 completedAt nyangkut; order 8/10/11 ada batch sisa probe.
 - Saran: re-seed dulu biar bersih sebelum test, re-seed lagi setelah.

══════════════════════════════════════════════════════════════════════
JANGAN diutak-atik (sudah beres):
GET /me/queue `hasStartedAt` — semantik per-current-step udah betul, verified 7 Juni.
FE /app/queue & /app/scan jalan di atasnya. Jangan regresi field ini.

Mulai dari P1. Jelasin temuan/akar masalah dulu sebelum nulis fix, lalu konfirmasi
sebelum eksekusi. Curl-verify tiap fix.
```