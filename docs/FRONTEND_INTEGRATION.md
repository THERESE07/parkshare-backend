# ParkShare: Frontend Integration Guide

For the web and mobile developers building on the ParkShare API. Follow the
**golden rules** and connecting to the deployed backend becomes a change of
one environment variable, not a rewrite.

---

## 1. Golden rules

1. **Never hard-code the API address.** Read it from an environment variable
   (`VITE_API_URL`, `NEXT_PUBLIC_API_URL`, `EXPO_PUBLIC_API_URL`…).
2. **Generate your types from the API** instead of writing them by hand
   (section 3). When the backend changes, you regenerate and the compiler
   shows what broke.
3. **All HTTP calls go through one small client** that adds the token and
   handles errors. No `fetch` calls scattered in components.
4. **Never calculate prices in the frontend.** Show what the API returns
   (`/api/pricing/quote`, reservation `totalAmount`). The server is the only
   source of truth for prices, VAT and fees.
5. **Never decide permissions only in the UI.** Hide buttons by role for a nicer
   experience, but always handle a `403` from the API.
6. **Send times as ISO-8601 with an offset** and display them in Kigali time.

---

## 2. Environments

| Environment | API base URL | Notes |
|---|---|---|
| Local | `http://localhost:8080` | Run the backend with Docker (see backend README) |
| Trial / production | `https://<your-api>.onrender.com` (later `https://api.<domain>.rw`) | Ask the backend owner |

```bash
# .env.local (web, Vite example)
VITE_API_URL=http://localhost:8080
```

**Interactive docs:** `<API_URL>/swagger-ui.html` (try every endpoint in the browser).
**Machine-readable spec:** `<API_URL>/v3/api-docs` (OpenAPI 3).

> CORS: the backend only accepts browser requests from origins listed in
> `PARKSHARE_SECURITY_ALLOWEDORIGINS` (default `http://localhost:3000`,
> `http://localhost:5173`). If your dev server uses another port, or when you
> deploy, give the exact origin (e.g. `https://parkshare.rw`) to the backend owner.
> Mobile apps are not affected by CORS.

---

## 3. Generate types from the API (do this first)

TypeScript, no extra runtime:

```bash
npm i -D openapi-typescript
npx openapi-typescript http://localhost:8080/v3/api-docs -o src/api/schema.d.ts
```

Add it as a script and re-run whenever the backend changes:

```json
"scripts": { "api:types": "openapi-typescript $VITE_API_URL/v3/api-docs -o src/api/schema.d.ts" }
```

Optionally use `openapi-fetch` (tiny typed client on top of these types) or
`orval` (generates React Query hooks). Any of them is fine; pick one.

---

## 4. The API client

```ts
// src/api/client.ts
const API_URL = import.meta.env.VITE_API_URL;

export class ApiError extends Error {
  constructor(public status: number, message: string) { super(message); }
}

let token: string | null = localStorage.getItem("ps_token");
export const setToken = (t: string | null) => {
  token = t;
  t ? localStorage.setItem("ps_token", t) : localStorage.removeItem("ps_token");
};

export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  const res = await fetch(`${API_URL}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...init.headers,
    },
  });
  if (res.status === 401) {          // token missing or expired (24h)
    setToken(null);
    window.location.assign("/login");
    throw new ApiError(401, "Please log in again");
  }
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new ApiError(res.status, body.message ?? "Something went wrong");
  }
  return res.status === 204 ? (undefined as T) : res.json();
}
```

Mobile: store the token with `expo-secure-store` / Keychain / Keystore, not plain storage.

---

## 5. Authentication

| Action | Request |
|---|---|
| Register driver | `POST /api/auth/register` `{firstName, lastName, email, phone, password}` |
| Register operator | same, plus `"accountType": "OPERATOR"` |
| Log in | `POST /api/auth/login` `{email, password}` → `{token, tokenType, userId, email, firstName, lastName, roles}` |
| Current user | `GET /api/me` → includes `roles`, `phoneVerified`, `emailVerified` |
| Update profile | `PATCH /api/me` `{firstName?, lastName?, phone?}` |
| Change password | `POST /api/me/password` `{currentPassword, newPassword}` |

- Send `Authorization: Bearer <token>` on every other request.
- Tokens last **24 hours** and there is no refresh token yet. On `401`, send the user to login.
- Roles: `DRIVER`, `OPERATOR`, `ATTENDANT`, `EVENT_ORGANIZER`, `ADMIN`. Route users to the right
  area after login based on `roles`.
- Password rule: at least 8 characters with a letter and a number.
- Phone: any Rwandan mobile format is accepted (`0788123456`, `+250788123456`); the API
  stores and returns `+250788123456`.

### Verification codes (OTP)

Right after registration the backend sends a 6-digit code by SMS (phone) and by email.

| Screen | Request |
|---|---|
| Enter phone code | `POST /api/me/verify/phone/confirm` `{code}` |
| Resend phone code | `POST /api/me/verify/phone` |
| Enter email code | `POST /api/me/verify/email/confirm` `{code}` |
| Resend email code | `POST /api/me/verify/email` |
| Forgot password | `POST /api/auth/password/forgot` `{identifier}` (phone or email) |
| Reset password | `POST /api/auth/password/reset` `{identifier, code, newPassword}` |

- Codes expire after **10 minutes**; 5 wrong tries invalidates the code.
- Resend is limited to once per **60 seconds**: show a countdown on the "Resend" button.
  The API answers `429` if pressed too early.
- "Forgot password" always answers `202`, even for unknown accounts. Show
  "If an account exists, we sent a code" and move to the code screen.
- In local/trial mode no SMS is really sent: the backend owner can read codes with
  `docker logs parkshare-api | grep "code is"`.

---

## 6. Errors

Every error has the same shape:

```json
{ "timestamp": "2026-11-10T10:00:00+02:00", "status": 409, "error": "Conflict",
  "message": "Parking space is already reserved for the selected time" }
```

| Status | Meaning | What the UI should do |
|---|---|---|
| 400 | Invalid input. Validation messages look like `"field: message; field2: message"` | Show `message`, or split on `; ` to mark fields |
| 401 | Not logged in / token expired | Go to login |
| 403 | Logged in but not allowed (not your booking, wrong role) | "You don't have access to this" |
| 404 | Not found | Not-found state |
| 409 | Business conflict (space taken, already paid, wrong status) | Show `message`; it is written for users |
| 429 | Too many requests (OTP resend) | Ask the user to wait |
| 500 | Server bug | Generic error + report it |

---

## 7. Money, time, phones

**Money:** all amounts are **RWF, whole francs**, as numbers (`800.0`). Display:

```ts
export const rwf = (n: number) =>
  new Intl.NumberFormat("en-RW", { maximumFractionDigits: 0 }).format(n) + " RWF";
// rwf(3000) -> "3,000 RWF"
```

**Time:** send ISO-8601 with offset; show in Kigali time (UTC+2):

```ts
const start = new Date(); // user's choice
const body = { startTime: start.toISOString(), endTime: end.toISOString() };

export const kigali = (iso: string) =>
  new Date(iso).toLocaleString("en-GB", { timeZone: "Africa/Kigali", dateStyle: "medium", timeStyle: "short" });
```

Booking rules the UI should enforce for a good experience (the API enforces them anyway):
start not in the past, minimum 30 minutes, maximum 7 days, at most 30 days ahead.

**Phones:** MTN numbers start `078`/`079`, Airtel `072`/`073`. Use this to pre-select the
payment method.

---

## 8. Pagination

These lists are paged: `/api/reservations/user/{id}`, `/api/notifications/user/{id}`,
`/api/users`, `/api/audit-logs`, `/api/reservations`.

```
GET /api/reservations/user/12?page=0&size=20
→ { "content": [...], "page": 0, "size": 20, "totalElements": 45, "totalPages": 3, "last": false }
```

`page` starts at 0; `size` max 100. Use `last` to stop infinite scroll. Other lists return a plain array.

---

## 9. Driver flows

### Find parking
- Approved lots only: `GET /api/parking-lots/status/ACTIVE`
  (`GET /api/parking-lots` also returns lots still awaiting approval, so don't use it for drivers)
- Search by name: `GET /api/parking-lots/search?name=heights`; by city: `/api/parking-lots/city?city=Kigali`
- Opening hours: `GET /api/parking-operating-hours/parking-lot/{lotId}`
- Features (EV, covered…): `GET /api/parking-features/parking-lot/{lotId}`

### Vehicles
- `GET /api/vehicles/user/{myUserId}`, `POST /api/vehicles/user/{myUserId}` `{plateNumber, vehicleType}`
- `vehicleType`: `CAR`, `MOTORCYCLE`, `TRUCK`, `BUS`, `OTHER`

### Show the price, then book
```
GET /api/pricing/quote?parkingLotId=1&startTime=...&endTime=...
→ { total: 800, vatIncluded: true, vatAmount: 122, amountExcludingVat: 678,
    lines: [{label, minutes, hourlyRate, amount}], note: "Price includes VAT at 18%. No extra fees." }
```
Show `total` big, then the `lines` and `note`. **The driver pays exactly `total`.**

```
POST /api/reservations?vehicleId=1&parkingLotId=1        (optional &parkingSpaceId=)
body { startTime, endTime, notes? }
→ 201 { id, reservationCode, status: "PENDING", totalAmount, vatAmount, paymentDueBy, parkingSpaceId, ... }
```
- Don't send `userId`, `totalAmount`, `status` or `reservationCode`: the server sets them.
- Show a countdown to `paymentDueBy` (15 minutes). After that the booking expires.

### Pay (MTN MoMo / Airtel Money)

```
POST /api/payments/reservation/{reservationId}/initiate   { "method": "MTN_MOMO", "phone": "0788123456" }
→ 202 { status: "PROCESSING", amount, transactionReference, instructions: "A MTN MoMo prompt ... Enter your PIN" }
```
- `phone` is optional (defaults to the account phone). It must match the network.
- Show `instructions` and a spinner, then **poll** until the result:

```ts
async function waitForPayment(reservationId: number, dueBy: string) {
  while (Date.now() < new Date(dueBy).getTime()) {
    const r = await api<{ status: string }>(`/api/reservations/${reservationId}`);
    if (r.status === "CONFIRMED") return "paid";
    const p = await api<{ status: string }>(`/api/payments/reservation/${reservationId}`);
    if (p.status === "FAILED") return "failed";   // let the user try again
    await new Promise(res => setTimeout(res, 3000));
  }
  return "expired";
}
```

**Sandbox (local/trial):** the "customer phone" answers by itself after about 5 seconds.
Use a phone ending in `000` to test a declined payment, `999` to test no answer.

### Receipt, QR code, cancel
- Receipt: `GET /api/payments/{paymentId}/receipt` (VAT line only when `vatAmount > 0`).
- QR code: `GET /api/reservations/{id}/qr` → `{ token, expiresAt }`. Render `token` as a QR image
  (web: `qrcode.react`; React Native: `react-native-qrcode-svg`). Keep the screen bright.
- Cancel: `PATCH /api/reservations/{id}/cancel` → `{ reservation, refundAmount }`.
  Explain the policy before confirming: full refund 60+ min before start, 50% after, none once started.

### Reservation statuses

| Status | Show |
|---|---|
| `PENDING` | "Awaiting payment" + countdown + Pay button |
| `CONFIRMED` | "Paid": QR code, directions |
| `CHECKED_IN` | "Parked": end time, overstay warning |
| `COMPLETED` | "Done": receipt, review prompt |
| `CANCELLED` / `EXPIRED` / `NO_SHOW` | Greyed out, "Book again" |

### Notifications
`GET /api/notifications/user/{myUserId}?page=0` (paged), mark read with `PATCH /api/notifications/{id}/read`.
Poll every 30–60 s while the app is open (push notifications are not wired yet).

---

## 10. Operator flows

| Screen | Requests |
|---|---|
| My lots | `GET /api/parking-lots/operator/{myUserId}` |
| Create lot | `POST /api/parking-lots/operator/{myUserId}` (`district` must be one of Rwanda's 30 districts; lat/long inside Rwanda). Starts `PENDING` until an admin approves |
| Zones and spaces | `POST /api/parking-zones/parking-lot/{lotId}`, `POST /api/parking-spaces/zone/{zoneId}` |
| Prices | `POST /api/pricing-rules/parking-lot/{lotId}` `{name, pricingType: HOURLY/DAILY/FLAT, amount, currency: "RWF", active: true, effectiveFrom}` (max 2,000 RWF/h, 20,000 RWF/day) |
| **Earnings preview** | `GET /api/pricing/operator-preview?parkingLotId=1[&hourlyRate=500][&paymentMethod=MTN_MOMO]` |
| Business / VAT verification | `POST /api/operator-verifications?userId={myUserId}` `{businessName, businessRegistrationNumber, tin, vatRegistered}` |
| Bookings at my lot | `GET /api/reservations/parking-lot/{lotId}` |

**Earnings preview:** show it next to the price form, and call it with the typed
`hourlyRate` as the operator edits (debounced) so they see the effect before saving:

```
examples: [{ stay: "2 hours", driverPays: 800, vatInPrice: 0, platformCommission: 80,
             mobileMoneyFee: 12, youReceive: 708, vatToDeclareToRra: 0, yourNetIncome: 708 }, ...]
notes: ["The driver pays only 'driverPays'...", ...]
```

## 11. Attendant (gate) app

| Action | Request |
|---|---|
| Scan QR | `POST /api/check-ins/scan` `{ "token": "<scanned text>" }` |
| No QR? Type code | `POST /api/check-ins/code` `{ "reservationCode": "PS-261110-ABC123" }` |
| Cash at the gate | `POST /api/payments/reservation/{id}/cash` |
| Exit | `POST /api/check-outs/reservation/{id}` → `{ overstayMinutes, overstayAmount, instruction }` |

Use a camera QR scanner (web: `html5-qrcode`; mobile: `expo-camera` / `react-native-vision-camera`).
Show `409` messages clearly ("Too early", "Already used", "Not paid"); they are written for attendants.
If `overstayAmount > 0`, block the "Done" button until the attendant confirms collection.

---

## 12. Admin

Approve lots: `PATCH /api/parking-lots/{id}/status?status=ACTIVE`.
Review operators: `PATCH /api/operator-verifications/{id}/review?reviewerId={adminId}`
`{verificationStatus: "VERIFIED"|"REJECTED", vatRegistered?, rejectionReason?}`.
Users: `GET /api/users?page=0`.

---

## 13. Deployment checklist

- [ ] `API_URL` set per environment; no `localhost` in the production build
- [ ] Frontend origin(s) sent to the backend owner for `PARKSHARE_SECURITY_ALLOWEDORIGINS`
- [ ] Both sides on HTTPS (browsers block HTTPS pages calling HTTP APIs)
- [ ] Types regenerated from the deployed `/v3/api-docs`
- [ ] 401 handling sends users to login (tokens expire after 24h)
- [ ] First request on the free trial can take ~1 minute (server waking up): show a
      friendly "Waking up the server…" state instead of failing
- [ ] Sandbox payment notice visible in trial builds ("Test mode: no real money")

## 14. Not available yet (don't build against these)

- Refresh tokens (re-login after 24h)
- Push notifications (poll notifications instead)
- Real MTN/Airtel payments (sandbox only)
- Image uploads (lot photos, documents)
- Map/distance search (`latitude`/`longitude` are returned; sort by distance client-side)
- `district` is accepted when creating a lot but not yet returned in lot responses

Questions or a missing field? Ask the backend owner before working around it.
Changing the API early is cheaper than adding workarounds.
