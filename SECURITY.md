# Security notes — Mini D-Mart

## Authentication
- Passwords hashed with PBKDF2WithHmacSHA256, 120,000 iterations, 16-byte random salt per
  user (`com.minidmart.util.PasswordUtil`). No plaintext or reversibly-encrypted passwords
  anywhere, including in the demo seed data (seeded via the same hashing path as registration).
- Login failures are rate-limited per email: 5 failures locks that email out for 5 minutes
  (`LoginAttemptTracker`, in-memory). Both failed and successful logins are written to
  `audit_log` with the client IP.
- `request.changeSessionId()` is called on every successful login to defend against session
  fixation (an attacker-supplied pre-auth session ID becomes invalid on login).
- Session cookies are `HttpOnly` (set in `web.xml`), with a 30-minute idle timeout.

## Authorization (RBAC)
- Three roles: CUSTOMER, STAFF, ADMIN, stored as a foreign key on `users`, never trusted from
  client input for anything other than the login form's resulting session attribute.
- Enforced in two independent layers: URL-pattern-scoped servlet **filters**
  (`AuthenticationFilter`, `StaffAccessFilter`, `AdminAccessFilter`) *and* a role check inside
  each servlet itself. Belt-and-suspenders — a filter mapping typo can't silently open a hole.
- Public self-registration always creates a CUSTOMER account; STAFF/ADMIN accounts can only be
  created by an existing admin changing a user's role at `/admin/users` (no privilege
  escalation path via the registration form).
- An admin cannot demote or deactivate their own account (checked server-side in
  `AdminUsersServlet`), preventing an accidental full lockout.

## IDOR / ownership checks
Every endpoint that loads a resource by numeric ID that belongs to a specific customer (order
detail, order cancel, return-request creation) re-verifies `resource.userId == session.userId`
server-side before returning or mutating it — a customer cannot view or act on another
customer's order or return by guessing/incrementing an ID in the URL.

## CSRF
Every state-changing form (add/remove cart line, place order, cancel order, file/approve/
reject a return, any admin/staff mutation) carries a per-session synchronizer token
(`CsrfUtil`), generated on the session and validated on the server before the POST is
processed. GET requests never mutate state.

## Input validation & injection
- **SQL injection**: every query in every DAO uses `PreparedStatement` with bound parameters —
  no string-concatenated SQL anywhere in the codebase.
- **XSS**: all JSPs render user- or database-sourced text through JSTL `${fn:escapeXml(...)}`
  rather than raw EL/scriptlet output, so stored product names, review-style free text
  (return reasons, staff notes, addresses) can't inject markup/script into the page.
- Server-side validation (`ValidationUtil`) backs every form in addition to HTML5 client-side
  constraints (`required`, `type=email`, `min`/`max`) — the client-side checks are a UX
  convenience, not the security boundary.

## Secrets & configuration
- No credential is hard-coded in source. Local dev uses `src/main/resources/db.properties`
  (gitignored; `.example` template checked in). In a hosting environment, `DB_URL` / `DB_USER`
  / `DB_PASSWORD` / `DB_DRIVER` environment variables override the properties file
  (`DBUtil`), so a deployment secret never has to live in a file at all.
- A dedicated, least-privilege MySQL user (`minidmart_user`) is used by the application; the
  DB root account is never referenced by the app.

## Security headers
Set on every response via `SecurityHeadersFilter`: `X-Content-Type-Options: nosniff`,
`X-Frame-Options: DENY` (clickjacking defense), `Referrer-Policy: same-origin`, and a
`Content-Security-Policy` restricting script/style/image origins to same-origin (plus data:/
https: for images).

## Audit logging
`audit_log` records: registration, login success/failure, logout, password change, order
placed/cancelled/status-changed, return requested/approved/rejected, stock manually adjusted,
product/category saved, pickup slot created, user role/status changed — each with actor,
timestamp, entity, and client IP. Visible to admins at `/admin/audit`.

## Business-rule races handled explicitly
- **Stock**: checkout locks the product row (`SELECT … FOR UPDATE`) inside the order
  transaction, so two simultaneous checkouts for the last unit of a product can't both succeed.
- **Pickup slot capacity**: booking is a single conditional `UPDATE … WHERE booked_count <
  capacity`, not a check-then-write, so slot overbooking under concurrent requests isn't possible.
- **Exchange approval**: re-validates the replacement product's stock at approval time (staff
  action, potentially much later than the customer's request), not just at request time.

## Testing performed
Manually exercised against the running app on a local Tomcat instance:
- Registration validation (email format, password strength, duplicate email) and login
  (wrong password, lockout after 5 attempts, redirect-after-login honoring role).
- RBAC boundaries: customer session hitting `/staff/dashboard` and `/admin/dashboard`
  directly → 403; unauthenticated request to any protected path → redirect to `/login`.
- Cross-user IDOR probing: authenticated customer A requesting customer B's
  `/orders/view?id=` → 404/403, not the order contents.
- Cart stock capping at available inventory; checkout stock-conflict messaging.
- Order cancellation restocking and pickup-slot release verified against `stock_movements`
  and `pickup_slots.booked_count`.
- Return/exchange eligibility window and inventory effects (restock on return, restock+deduct
  on exchange) verified end-to-end from customer request through staff approval.

## Known limitations (see also README §Known limitations)
- `LoginAttemptTracker`'s rate limiting is in-memory and per-JVM instance — it resets on
  redeploy and does not share state across multiple horizontally-scaled instances. A
  production deployment would move this to a shared store (e.g. the database or a cache).
- No forced password rotation, no multi-factor authentication, no email verification on
  registration — out of scope for this assessment's time budget.
- TLS termination is expected to be handled by the hosting platform (Render/Railway/etc.);
  the app itself doesn't set the cookie `Secure` flag, since local/demo HTTP access would then
  break the session — enable it once permanently deployed behind HTTPS.
