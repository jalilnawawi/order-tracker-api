# Prompt BE — Fix RBAC: role CUSTOMER bocor ke endpoint admin `/orders/**` & `/batches/**`

> Tempel prompt ini ke sesi BE. Ditemukan saat M7 internal testing (smoke e2e FE↔BE),
> 11 Juni 2026, dari sisi FE via curl. **Severity: HIGH (data leak lintas-institusi).**
> Sisi FE aman (area customer cuma panggil `/me/*`) — ini murni utang BE.

---

## Masalah

Role **CUSTOMER** punya akses penuh ke seluruh subtree endpoint **admin**
`/orders/**` dan `/batches/**`. Customer bisa:

1. **Enumerasi semua order lintas-institusi** via `GET /orders` (nama customer,
   nama institusi, judul, status — semua sekolah, bukan cuma miliknya).
2. **Baca detail order/batch orang lain** via `GET /orders/{id}`, `/orders/{id}/batches`,
   `/batches/{id}`.
3. **Membongkar nama operator** via `GET /batches/{id}/progress-events` — yang
   justru **sengaja disembunyikan** di endpoint customer `/me/orders/{id}/timeline`
   (kontrak §8.3.F: timeline tanpa nama operator). Desain privasi itu jadi **percuma**
   karena bisa di-bypass lewat endpoint admin.

## Bukti (curl 11 Juni 2026, login `smpn1.kudus` / `password123` = CUSTOMER)

Customer ini cuma punya order #1 & #5. Order #3 = milik institusi lain (Ibu Yuliana).

| Request (sbg CUSTOMER) | Dapet | Seharusnya |
|---|---|---|
| `GET /me/orders` | 2 order (#1, #5 — miliknya) ✅ | benar |
| `GET /orders?size=100` | **8 order (SEMUA institusi)** ⚠️ | **403** |
| `GET /orders/3` (orang lain) | **200** ⚠️ | **403** |
| `GET /orders/3/batches` | **200** ⚠️ | **403** |
| `GET /batches/3` | **200** ⚠️ | **403** |
| `GET /batches/3/progress-events` | **200 (bocor nama operator)** ⚠️ | **403** |

Kontras — guard yang **sudah benar** (jadi masalahnya inkonsisten, bukan total):
- `GET /users` sebagai OPERATOR → **403** ✅
- `GET /me/orders/{id}` cross-tenant / tidak ada → **404** ✅ (tidak leak existence)

## Dugaan akar masalah

Spring Security config: subtree `/orders/**` dan `/batches/**` kemungkinan cuma
`.authenticated()` tanpa restriksi role, sementara `/users/**` sudah `hasRole("ADMIN")`.

## Fix yang disarankan

Batasi subtree admin ke role ADMIN (+ OPERATOR di mana relevan, mis. operator butuh
`GET /batches/{id}` & post progress-event untuk batch di divisinya). Customer **tidak boleh
sama sekali** menyentuh `/orders/**` & `/batches/**` — jalur customer hanya `/me/*`.

Pertimbangkan matriks:
- `/orders/**` (GET list/detail, POST, PATCH) → ADMIN only.
- `/batches/**` GET → ADMIN + OPERATOR; POST progress-event → OPERATOR (divisi match) + ADMIN.
- Customer → 403 untuk semua di atas.

## Verifikasi setelah fix (ulang curl di atas)

Semua baris ⚠️ harus jadi **403**. Baris ✅ tetap. Lalu re-run smoke FE: `/tmp/m7-smoke.sh`
(baris `cust->orders` harus pass exp=403).