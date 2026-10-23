# Kuota mobile API contract draft

This is a local implementation contract for the Android demo, not a production backend contract. The current Ktor service implements signed supplier QR verification, demo eligibility, quote and purchase creation, a demo supplier confirmation and a simulated payment lifecycle. Identity providers, real payment providers, payment-method identifiers, consent versions and commercial calculations must be confirmed with backend and local product/legal review before enabling real operations.

## Draft mobile operations

| Operation | Client route | Notes |
|---|---|---|
| Start identity verification | `POST /v1/identity/sessions` | Backend creates a provider session. The Android app should not collect provider passwords, OTPs or raw identity documents unless the approved provider flow requires it. |
| Verify supplier QR | `POST /v1/supplier/qr/verify` | Public. Body `{"qr_payload":"<base64>"}`. The backend verifies the HMAC-SHA256 signature, required fields, positive amount, payload size and expiry. Returns supplier and signed amount, or `422 QR_INVALID_OR_EXPIRED` / `QR_PAYLOAD_TOO_LARGE`. The app must not verify locally or ship the signing secret. |
| Generate a demo supplier QR | `POST /v1/supplier/generate-qr` | Demo only; `application/x-www-form-urlencoded` with `amountPen`. Returns a signed payload valid for 15 minutes. Production returns `403 SUPPLIER_DEMO_ONLY`. |
| Confirm demo supplier sale | `POST /v1/supplier/confirm-sale` | Demo only. Body `{"qr_payload":"<base64>"}`. Re-verifies the QR and derives the 60/40 split and 4% supplier fee from the signed amount. Returns `DEMO_SUPPLIER_CONFIRMED`; it is not a settlement, delivery or real sale. |
| Check buyer eligibility | `POST /v1/eligibility/checks` | Backend returns a decision and the exact approved initial payment/limit. Do not calculate a real score on-device. |
| Request a purchase quote | `POST /v1/quotes` | Backend validates the signed supplier QR, buyer eligibility, current rates and quote expiry. It rejects an amount different from the signed amount with `QR_AMOUNT_MISMATCH`. Return a complete immutable breakdown in integer céntimos. |
| Confirm purchase | `POST /v1/purchases` | Require an idempotency key and the exact quote/terms/notice versions that the customer reviewed. The demo app registers the purchase only after the supplier confirmation and always sends `DEMO_ONLY_NOT_ACCEPTED`; the result is `DEMO_CREATED`, not a sale, debt or disbursement. |
| List demo purchase history | `GET /v1/purchases` | Requires a demo Bearer token and filters by the authenticated user. Returns synthetic `DEMO_CREATED` purchases and quote totals; it is not a production history API. |
| Read purchase and installments | `GET /v1/purchases/{id}` and `/installments` | Backend state is authoritative; client should refresh after uncertain network outcomes. |
| Create installment payment intent | `POST /v1/installments/{id}/payment-intents` | Require an idempotency key. Provider redirects or SDK actions must be validated against the selected provider before use. |
| Read payment status | `GET /v1/payments/{id}` | A client timeout does not mean failure. Refresh status before offering another payment attempt. |
| Apply a simulated payment event | `POST /v1/payments/{id}/demo-events` | Demo-only allowlist: `PROCESSING`, `SUCCEED`, `FAIL`, `SUCCEED_AMOUNT_MISMATCH`, `SUCCEED_REFERENCE_MISMATCH`, `REVERSE`, `REFUND`, `RECONCILE_MATCH`. Require an idempotency key. |
| Read the demo reconciliation queue | `GET /v1/demo/reconciliation` | Returns only mismatched or under-review synthetic settlements. No bank statement or provider is connected. |

## State and error handling

- Treat `PENDING`, `ACTION_REQUIRED`, `PROCESSING`, `SUCCEEDED`, `FAILED`, `REVERSED`, `REFUNDED` and unknown future payment states distinctly. Mark an installment paid only after a backend-confirmed final state.
- Keep provider webhook delivery, reversals, refunds and reconciliation on the backend. The mobile app must not claim settlement based only on a provider redirect or local callback.
- Every response carries `X-Request-Id`, `X-Content-Type-Options: nosniff` and `Referrer-Policy: no-referrer`. The server reuses an inbound `X-Request-Id` only when it is 1–64 characters of `[A-Za-z0-9._:-]`; otherwise it generates a UUID.
- Error bodies are `{"code":"…","message":"…","request_id":"…","retryable":false}`. `retryable` is always present, including `false`. `500 INTERNAL_ERROR` is retryable and never exposes a stack trace. The app displays `request_id` as a support reference.
- Use stable error `code`, `request_id` and `retryable` values. Show a safe translated message; do not render arbitrary provider or server text as a trusted instruction.
- Create purchase and payment actions with an idempotency key. If the request times out, query status or safely repeat with the same key.
- Demo payment intents accept only `payment_method_id=DEMO_MANUAL`; they begin at `PENDING`. A same-key/same-body retry replays the original intent, while a different body returns `409 IDEMPOTENCY_KEY_REUSED`. A new intent is blocked while an attempt is active or after a confirmed payment until its state is resolved.
- Demo events append to a payment event history. `SUCCEED` creates a synthetic provider reference and marks the installment `DEMO_PAID` only when the simulated amount and reference match. Mismatch scenarios remain for review; `RECONCILE_MATCH` is an explicit demo operator action that records `MANUALLY_RESOLVED` without overwriting the synthetic provider evidence.
- `REVERSE` and `REFUND` are distinct demo transitions. A failed attempt returns the installment to its programmed state and permits a new idempotent attempt with a new key.
- A repeated purchase request with the same idempotency key and identical body returns the same demo purchase. Reusing the key with a different body returns `409 IDEMPOTENCY_KEY_REUSED`; trying to consume one quote with a new key returns `409 QUOTE_ALREADY_USED`.
- Demo purchase creation accepts only `DEMO_ONLY_NOT_ACCEPTED` markers for the terms and privacy fields. These values explicitly do not record legal acceptance.
- Use integer cents for all amounts. Server quote values are authoritative; client calculations are preview only.
- Treat `429 RATE_LIMITED` with its `Retry-After` header as a transient state. Limits are process-local fixed windows, reset on restart and are not a substitute for an API gateway or distributed abuse protection. The current demo applies them to QR verification/generation, demo supplier confirmation, OTP request/verification, eligibility, quotes, purchases, payment intents and demo payment events.
- The demo OTP is the exact code `123456`; any other value returns `401 OTP_INVALID`. Production returns `503 OTP_PROVIDER_NOT_CONFIGURED` for both OTP routes. This is not a real identity flow.
- A verified QR proves only that the demo backend signed the payload. It does not prove a real supplier, price, stock, delivery or authorization from the merchant.

## Security and privacy integration gates

- Configure only a real HTTPS base URL using the `KuotaApiFactory`; do not commit URLs containing credentials or tokens.
- Obtain short-lived identity session credentials from the backend. Keep them in memory only and never log them.
- Do not send raw Yape/Plin credentials, PINs or OTPs. Decide the permitted data sources and user consent before implementing credit scoring.
- Keep payment-provider secret keys, `KUOTA_QR_SIGNING_SECRET` and webhook credentials on the backend. The Android app must not contain them.
- Do not enable real identity, credit, purchase or payment calls until the API contract, provider sandbox, consent language and Peru-specific legal review are approved.

## Code locations

- Draft Retrofit routes and DTOs: `app/src/main/java/com/kouta/app/data/remote/`
- Demo purchase registration and quote flow: `app/src/main/java/com/kouta/app/feature/financing/FinancingScreen.kt`
- Demo purchase history in Actividad: `app/src/main/java/com/kouta/app/feature/sales/SalesScreen.kt`
- Payment-state presentation mapping: `app/src/main/java/com/kouta/app/domain/model/PaymentStatus.kt`
- HTTPS-only Retrofit factory: `app/src/main/java/com/kouta/app/data/remote/KuotaApiFactory.kt`

The financing screen calls the local demo backend to verify or generate a signed QR, check eligibility, request a quote and simulate supplier confirmation. It shows no supplier amount or installment plan before the server verifies a QR. Add a production host through a local Gradle property named `KUOTA_API_BASE_URL`; release builds accept HTTPS only. Do not store secrets in that property. The local demo backend is file-backed and loopback-only; it is not suitable for deployment or real customer data.
