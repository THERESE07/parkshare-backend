# Deploying ParkShare (trial)

Trial setup: **Neon** (database, free) + **Brevo** (email, free) + **Render Starter** (API, $7/month ≈ 10,300 RWF).
Payments run in sandbox (no real money). Emails (verification codes, confirmations) are really sent;
SMS are only written to the server log because each SMS costs money.

## How messages work in the trial

| Message | Trial | Later (with Africa's Talking) |
|---|---|---|
| Email verification code (sent at sign-up) | **Sent by email** | Same |
| Password reset code | **By email** if the user types their email; by phone → log only | Phone too |
| Phone verification code | Not sent (log only) | By SMS |
| Booking confirmed / cancelled | In-app + **email** (+ SMS in log) | + real SMS |
| Reminder, check-out, payment failed | In-app + **email** | Same |

**Users must verify their phone or email before they can book.** In the trial they verify
by email, which costs nothing.

Expect about **30 minutes** the first time. Every step below was rehearsed with the
same settings (port, CPU, memory, connection settings) before writing this.

---

## Why Render Starter and not the free plan

Measured with this app:

| Plan | CPU | Startup | Sleeps when idle |
|---|---|---|---|
| Render Free | 0.1 | **~15 minutes** | yes, after 15 min |
| **Render Starter ($7)** | 0.5 | **~3 minutes** | no |

On the free plan every wake-up would take ~15 minutes, so testers would only see errors.
Memory use is ~280 MB, which fits the 512 MB both plans have.

---

## Step 0: Get the code onto the default branch

Render deploys the repository's default branch (`main`).

```bash
git push -u origin agnes/ft
```

Open a pull request `agnes/ft → main` on GitHub and merge it.
(Or deploy the branch directly: after step 2, Render → Settings → Branch.)

---

## Step 1: Database on Neon (free)

1. Sign up at <https://neon.com> (GitHub login is easiest).
2. **Create project**
   - Name: `parkshare`
   - Region: **AWS Europe Central 1 (Frankfurt)**, the same region as the API
   - Postgres version: default is fine
3. On the project dashboard click **Connect**.
4. **Turn OFF "Connection pooling"**. The host must **not** contain `-pooler`.
   (The pooled connection breaks database migrations.)
5. You get something like:
   ```
   postgresql://neondb_owner:AbC123xyz@ep-cool-name-123456.eu-central-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require
   ```
   Split it into three values for Render:

   | Render variable | Value from the example |
   |---|---|
   | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://ep-cool-name-123456.eu-central-1.aws.neon.tech/neondb?sslmode=require` |
   | `SPRING_DATASOURCE_USERNAME` | `neondb_owner` |
   | `SPRING_DATASOURCE_PASSWORD` | `AbC123xyz` |

   - Add `jdbc:` in front and change nothing else in the host
   - Remove the user and password from the URL
   - Keep `?sslmode=require`, drop `&channel_binding=require`

You don't need to create tables: the app does it on first start.

---

## Step 1b: Email on Brevo (free, 300 emails/day)

1. Sign up at <https://www.brevo.com> (free plan).
2. **Senders, Domains & Dedicated IPs → Senders → Add a sender.** Use an address you control,
   ideally a dedicated one such as `parkshare.rw@gmail.com`, and confirm the email Brevo sends you.
3. **SMTP & API → SMTP tab:**
   - note the **Login** (looks like `8a1b2c001@smtp-brevo.com`)
   - click **Generate a new SMTP key** and copy it (shown only once)

You'll give Render three values:

| Render variable | Value |
|---|---|
| `SPRING_MAIL_USERNAME` | the SMTP **Login** |
| `SPRING_MAIL_PASSWORD` | the **SMTP key** (not your Brevo account password) |
| `PARKSHARE_MESSAGING_EMAILFROM` | `ParkShare <the-sender-you-verified@gmail.com>` |

> Emails from a Gmail address sent through Brevo can land in spam. Fine for a trial; once you
> own a domain, authenticate it in Brevo (Senders, Domains → Domains) and send from e.g. `no-reply@parkshare.rw`.

---

## Step 2: API on Render

1. Sign up at <https://render.com> with GitHub, and allow access to the `parkshare-backend` repository.
2. Add a payment card (Billing). Starter costs $7/month, billed by the second.
   Render has also charged a workspace fee on top of services since its April 2026 pricing change;
   check the plan shown at checkout before confirming.
3. **New → Blueprint** → choose `parkshare-backend`. Render reads [`render.yaml`](../render.yaml).
4. Fill in the values it asks for:

   | Variable | Value |
   |---|---|
   | `SPRING_DATASOURCE_URL` | from step 1 |
   | `SPRING_DATASOURCE_USERNAME` | from step 1 |
   | `SPRING_DATASOURCE_PASSWORD` | from step 1 |
   | `PARKSHARE_SECURITY_ADMINEMAIL` | your email |
   | `PARKSHARE_SECURITY_ADMINPASSWORD` | strong password, **12+ characters** (otherwise no admin is created) |
   | `PARKSHARE_SECURITY_ALLOWEDORIGINS` | the frontend address, e.g. `https://parkshare-web.vercel.app`. Use `http://localhost:5173` until the frontend is deployed |
   | `SPRING_MAIL_USERNAME` | Brevo SMTP login (step 1b) |
   | `SPRING_MAIL_PASSWORD` | Brevo SMTP key (step 1b) |
   | `PARKSHARE_MESSAGING_EMAILFROM` | `ParkShare <your-verified-sender@gmail.com>` |

   Secrets (`PARKSHARE_JWT_SECRET`, `PARKSHARE_PAYMENTS_WEBHOOKSECRET`) are generated by Render automatically.
5. Click **Apply**. The first deploy takes **6–8 minutes** (build ~4 min + start ~3 min).
   Watch it under the service's **Logs** tab. It's ready when you see
   `Started ParkshareBackendApplication`.

---

## Step 3: Check it works (2 minutes)

Your address is shown at the top of the service page, e.g. `https://parkshare-api.onrender.com`.

| Open | Expect |
|---|---|
| `https://parkshare-api.onrender.com/actuator/health` | `{"status":"UP"}` |
| `https://parkshare-api.onrender.com/` | The API docs page (Swagger) |
| Swagger → `POST /api/auth/login` with your admin email/password | A token with `"roles":["ADMIN"]` |
| Swagger → `POST /api/auth/register` with **your own** email and a Rwandan phone | Within a minute an email "Your ParkShare code: 123456" (check spam) |
| Log in as that user → `POST /api/me/verify/email/confirm` `{"code":"123456"}` | `"Email verified"`; this user can now book |

---

## Step 4: Hand over to the frontend

Give the frontend developer:
- the API address (they set it as `VITE_API_URL` or similar),
- [docs/FRONTEND_INTEGRATION.md](FRONTEND_INTEGRATION.md).

When the frontend is deployed, put its address in `PARKSHARE_SECURITY_ALLOWEDORIGINS`
(Render → Environment → edit → Save; it redeploys by itself). Several addresses are comma-separated:
`https://parkshare.rw,https://www.parkshare.rw,http://localhost:5173`.

---

## Day-to-day

- **Deploys:** every push to `main` redeploys automatically (~6 min). The old version
  keeps serving until the new one is healthy.
- **Tests:** GitHub Actions runs all tests on every push (`.github/workflows/ci.yml`).
- **Emails:** Brevo → Transactional → Logs shows every email and whether it was delivered.
- **"Sent" SMS (log only):** Render → Logs, search for `SMS not sent`.
- **A tester didn't get their code:** they can press "Resend" in the app (once a minute), or check
  spam. Brevo's logs show whether it was delivered.
- **Sandbox payments:** approve automatically after 5 s. Phones ending `000` decline, `999` never answer.
- **Database:** Neon → SQL Editor to look at data. Neon sleeps after 5 idle minutes, so the
  first query after a pause takes ~1 s.

### Free-tier limits that are already handled

| Limit | How it's handled |
|---|---|
| Neon free compute hours (≈100 CU-hours/month) | Background jobs run every 15 min and idle connections close after 1 min, so Neon can sleep (verified: connections drop to 0 when idle) |
| Neon free storage (1 GB) | Enough for many thousands of bookings |
| 512 MB RAM | App uses ~280 MB; JVM tuned in the Dockerfile |
| Render port | App listens on Render's `$PORT` automatically |

---

## If something goes wrong

| Symptom (in Render Logs) | Cause and fix |
|---|---|
| `PARKSHARE_JWT_SECRET must be at least 32 bytes` | The secret was replaced by hand with a short value. Use Render's "Generate" button |
| `password authentication failed for user` | Wrong `SPRING_DATASOURCE_USERNAME`/`PASSWORD`; copy again from Neon |
| `Connection attempt timed out` / `UnknownHostException` | Wrong host in the URL, or you forgot `jdbc:` at the start |
| `prepared statement "S_1" already exists`, or migrations hanging | You used the **pooled** host (`-pooler`). Use the direct one |
| `The server requires SSL` / `no pg_hba.conf entry ... no encryption` | Add `?sslmode=require` to the URL |
| Deploy stuck, then "health check failed" | Read the first `ERROR` line in Logs; usually one of the above |
| No admin account | `PARKSHARE_SECURITY_ADMINPASSWORD` shorter than 12 characters. Fix it and redeploy |
| Browser shows a CORS error | Frontend address not in `PARKSHARE_SECURITY_ALLOWEDORIGINS` (must match exactly, including `https://`, no trailing `/`) |
| `403` on API calls from Swagger | Click **Authorize** and paste the token first |
| `403` with `"code":"VERIFICATION_REQUIRED"` when booking | Expected: the user must first enter the email (or phone) code |
| `Email to ... failed: 535 Authentication failed` | Wrong Brevo SMTP login/key. Use the SMTP **key**, not your account password |
| `Email to ... failed: ... sender ... not valid` | `PARKSHARE_MESSAGING_EMAILFROM` isn't a verified Brevo sender |
| No emails and no errors | Check Brevo → Transactional → Logs; the email may be in the recipient's spam folder |

---

## Before real money (not needed for the trial)

- [ ] MTN MoMo / Airtel Money merchant accounts and the real payment adapter
- [ ] `PARKSHARE_PAYMENTS_SANDBOXENDPOINTSENABLED=false`
- [ ] Africa's Talking live account + sender ID (RURA rules apply): set `PARKSHARE_MESSAGING_SMSPROVIDER=africastalking`,
      the API key, and `PARKSHARE_MESSAGING_VERIFYPHONEONREGISTER=true`
- [ ] Turn off the public API docs: `SPRINGDOC_API_DOCS_ENABLED=false`, `SPRINGDOC_SWAGGER_UI_ENABLED=false`
- [ ] Custom domain (`api.<your-domain>.rw`): Render → Settings → Custom Domains (free HTTPS)
- [ ] Database backups: Neon free keeps a short history; consider a paid plan or Render Postgres
- [ ] Confirm VAT handling and rates with an accountant
