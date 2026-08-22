# Mini D-Mart — Grocery Store Application

A full-stack grocery store web app: customers browse products, build a cart, check out with
either scheduled store pickup or home delivery, track orders, and request returns/exchanges.
Staff process orders and manage inventory; admins manage the catalog, users, pickup slots and
review a security audit log.

Built for the Round 2 Full Stack Developer Practical Assessment. **Stack: JSP + Servlets (no
`@WebServlet` annotations — all routing is in `web.xml`) + JDBC + MySQL, deployed on Tomcat 9.**

## 1. Architecture

Classic layered MVC on raw Servlet/JSP — no framework (no Spring, no ORM):

```
Browser
  │  HTTP
  ▼
web.xml routing ──▶ Filters (security headers, flash messages, cart badge,
  │                            authentication, role-based access control)
  ▼
Servlets (com.minidmart.servlet.*)   — one class per feature, controller layer
  │  calls
  ▼
DAOs (com.minidmart.dao.*)           — hand-written JDBC, PreparedStatements only
  │  reads/writes
  ▼
MySQL (schema in db/schema.sql)
```

JSPs under `WEB-INF/jsp/**` render the response (never accessible directly — only via a
servlet forward, so business logic can't be bypassed by hitting a JSP URL). Static assets
(`/assets/css/style.css`) are served directly by Tomcat.

Package layout:
- `com.minidmart.model` — plain POJOs (User, Product, Order, ReturnRequest, …)
- `com.minidmart.dao` — one DAO per aggregate; all SQL lives here, all transactional
  business rules (stock decrement, slot booking, return inventory handling) are here too
- `com.minidmart.servlet.{auth,catalog,cart,checkout,orders,returns,customer,staff,admin}` —
  controllers; thin, delegate to DAOs
- `com.minidmart.filter` — cross-cutting concerns (see §4)
- `com.minidmart.util` — DBUtil, PasswordUtil, CsrfUtil, ValidationUtil, AuditLogger,
  FlashUtil, SessionUtil, LoginAttemptTracker, BusinessRuleException
- `com.minidmart.tools.DataSeeder` — one-shot demo-data loader (not part of the web app)

## 2. Database design

Full DDL in [`db/schema.sql`](db/schema.sql). Key tables and relationships:

- **roles / users** — `users.role_id → roles`. One role per user (CUSTOMER / STAFF / ADMIN).
- **addresses** — `→ users`, multiple saved delivery addresses per customer, one flagged default.
- **categories / products** — `products.category_id → categories`. Products carry their own
  `stock_qty` / `reorder_level` (no separate inventory table — kept it on the product row
  since this app has no per-warehouse/location concept).
- **stock_movements** — append-only ledger of every stock change (order placed, cancelled,
  return restock, exchange in/out, manual adjustment) with a reason code and actor —
  the audit trail for inventory, independent of the general `audit_log`.
- **carts / cart_items** — one active cart per user (`carts.user_id` UNIQUE).
- **pickup_slots** — `slot_date + start_time` UNIQUE, with `capacity` / `booked_count` for
  scheduled-pickup capacity management.
- **orders / order_items** — `orders.user_id → users`, optional `pickup_slot_id` or
  `delivery_address_id` depending on `fulfillment_type`. `order_items` snapshots product name
  and price at time of purchase (so later price/name edits never rewrite history).
- **returns** — `→ order_items`, one return/exchange request per order line, with
  `type` (RETURN/EXCHANGE), `status` lifecycle, and an optional `exchange_product_id`.
- **audit_log** — append-only security/business event log (logins, role changes, order
  lifecycle, return decisions, stock adjustments) shown to admins at `/admin/audit`.

## 3. Business logic & edge cases

- **Stock validation** happens twice: a soft cap when adding to cart (`CartDao.addItem`
  clamps to current `stock_qty`), and a hard, row-locked (`SELECT … FOR UPDATE`) check inside
  the checkout transaction (`OrderDao.placeOrder`) — so two customers racing for the last unit
  can't both succeed. The whole checkout (stock decrement, slot booking, order + items insert,
  cart clear) is one JDBC transaction; any conflict rolls back everything.
- **Pickup slot capacity** is booked atomically with `UPDATE pickup_slots SET booked_count =
  booked_count + 1 WHERE slot_id = ? AND booked_count < capacity` inside the same transaction
  — the `WHERE` clause is the concurrency guard, not an application-level check-then-write.
- **Order cancellation** is only allowed while `status` is PLACED/CONFIRMED/PREPARING
  (`OrderStatus.isCancellable()`); cancelling restocks every line item and releases the pickup
  slot, atomically.
- **Order status lifecycle** is staff-driven and one-directional: PLACED → CONFIRMED →
  PREPARING → (READY_FOR_PICKUP | OUT_FOR_DELIVERY, branching on fulfillment type) →
  COMPLETED. `StaffOrdersServlet` computes the single legal next status server-side and
  rejects anything else — the UI can't be tricked into skipping a step.
- **Return/exchange eligibility**: only order items on a COMPLETED order, within a 7-day
  window of completion, with no existing return already filed for that line
  (`ReturnDao.ELIGIBILITY_DAYS`, enforced server-side in the same transaction that creates the
  request — not just hidden in the UI).
- **Return approval** restocks the original product; **exchange approval** additionally
  re-checks the replacement product's stock at approval time (not just at request time — it
  may have sold out in the meantime) and decrements it, all atomically, all logged to
  `stock_movements`.
- **RBAC self-protection**: an admin can't demote or deactivate their own account (would risk
  locking everyone out).

## 4. Authentication, sessions & RBAC

- Passwords hashed with **PBKDF2WithHmacSHA256** (120,000 iterations, random salt) —
  `PasswordUtil`, JDK-only, no external crypto dependency.
- Session-based auth: on login, `HttpSession` carries `userId`, `role`, `fullName`, `email`.
  `request.changeSessionId()` is called on every successful login (session fixation defense).
  Cookies are `HttpOnly` (`web.xml` `<cookie-config>`), 30-minute idle timeout.
- **Filter chain** (`web.xml`, in this order for every request):
  1. `SecurityHeadersFilter` — `X-Content-Type-Options`, `X-Frame-Options: DENY`,
     `Content-Security-Policy`, `Referrer-Policy` on every response.
  2. `FlashFilter` / `CartCountFilter` — request-scoped conveniences (flash messages, cart badge).
  3. `AuthenticationFilter` — requires a session on `/profile/*`, `/cart/*`, `/checkout/*`,
     `/orders/*`, `/returns/*`, `/staff/*`, `/admin/*`; redirects to `/login?redirect=...`.
  4. `StaffAccessFilter` / `AdminAccessFilter` — role gate on `/staff/*` and `/admin/*`
     respectively (403 on mismatch).
- Every servlet under a role-gated path **also** re-checks role itself (defense in depth —
  the filter and the servlet agree independently), and every order/return detail lookup
  verifies the record belongs to the requesting user before returning it (IDOR prevention).
- **CSRF**: every state-changing form carries a per-session synchronizer token
  (`CsrfUtil`), validated on every POST before any mutation.
- **Brute-force guard**: `LoginAttemptTracker` locks an email out for 5 minutes after 5 failed
  logins (in-memory — see Known Limitations).
- Public routes: `/home`, `/products`, `/products/view`, `/login`, `/register`. Everything
  else requires a session; `/staff/*` requires STAFF or ADMIN; `/admin/*` requires ADMIN.

## 5. URL map / "API" (server-rendered, not JSON)

| Method | Path | Who | Purpose |
|---|---|---|---|
| GET | `/home` | public | landing page |
| GET | `/products`, `/products/view?id=` | public | browse / product detail |
| GET/POST | `/register`, `/login` | public | account creation / sign-in |
| GET | `/logout` | any | end session |
| GET/POST | `/profile`, `/profile/address` (via `action=`) | customer | account, addresses, password |
| GET/POST | `/cart`, `/cart/add`, `/cart/update`, `/cart/remove` | customer | cart management |
| GET/POST | `/checkout` | customer | place order (pickup or delivery) |
| GET | `/orders`, `/orders/view?id=` | customer | order history / detail |
| POST | `/orders/cancel` | customer | cancel a cancellable order |
| GET/POST | `/returns`, `/returns/new?orderItemId=` | customer | file / view return-exchange requests |
| GET | `/staff/dashboard` | staff+ | staff KPIs |
| GET/POST | `/staff/orders`, `/staff/orders?id=` | staff+ | order queue + status advance |
| GET | `/staff/pickups`, `/staff/deliveries` | staff+ | fulfillment worklists |
| GET/POST | `/staff/inventory` | staff+ | stock levels + manual adjustment |
| GET/POST | `/staff/returns` | staff+ | approve/reject return-exchange requests |
| GET | `/admin/dashboard` | admin | store-wide KPIs |
| GET/POST | `/admin/users` | admin | role management, activate/deactivate |
| GET/POST | `/admin/products`, `/admin/products/new`, `/admin/products/edit` | admin | catalog CRUD |
| GET/POST | `/admin/categories` | admin | category CRUD |
| GET/POST | `/admin/slots` | admin | pickup slot capacity management |
| GET | `/admin/audit` | admin | security/business audit log |

## 6. Setup (local development)

Prerequisites: JDK 21, MySQL 8, Apache Tomcat 9, Eclipse (or any IDE with WTP-style Dynamic
Web Project support).

1. **Database**
   ```
   mysql -u root -p -e "CREATE DATABASE minidmart CHARACTER SET utf8mb4;"
   mysql -u root -p -e "CREATE USER 'minidmart_user'@'localhost' IDENTIFIED BY 'yourpassword'; GRANT ALL PRIVILEGES ON minidmart.* TO 'minidmart_user'@'localhost';"
   mysql -u minidmart_user -p minidmart < db/schema.sql
   ```
2. **Config**: `cp src/main/resources/db.properties.example src/main/resources/db.properties`
   and fill in the credentials above (this file is gitignored — never commit real secrets).
3. **Dependencies**: `mysql-connector-j` and `jstl` jars already live in
   `src/main/webapp/WEB-INF/lib/` (no Maven/Gradle build — this is a plain Dynamic Web
   Project, matching the "no framework annotations" constraint of the assignment).
4. **Seed demo data** (categories, products, pickup slots, one test account per role):
   compile the project, put the compiled classes + the two lib jars on the classpath, and run
   `com.minidmart.tools.DataSeeder`'s `main()` once. In Eclipse: right-click `DataSeeder.java`
   → Run As → Java Application.
5. **Run**: import as a Dynamic Web Project targeting Tomcat 9, add the server, Run on Server.
   Visit `/home`.

## 7. Environment variables

See [`.env.example`](.env.example). `DBUtil` checks `DB_URL` / `DB_USER` / `DB_PASSWORD` /
`DB_DRIVER` environment variables first and falls back to `db.properties` — so a free hosting
platform can inject real credentials without any properties file containing a secret.

## 8. Deployment

Package as a WAR (`WEB-INF/classes` + `WEB-INF/lib` + the JSPs/assets) and deploy to any free
Tomcat-compatible host, or a platform that runs a WAR/Java process directly, with a managed
MySQL add-on and the environment variables from §7 set as secrets. See `SECURITY.md` for the
production-hardening notes (TLS, cookie `Secure` flag, etc.) that apply once deployed behind HTTPS.

## 9. Testing

The full request/response cycle for every route (registration/login, RBAC boundaries, cart,
checkout, order lifecycle, returns/exchanges, and every staff/admin screen) was exercised
against a live local deployment (Tomcat 9 + MySQL) during development and compiles cleanly as
a whole. The concurrency-sensitive paths — stock decrement and pickup-slot booking at checkout,
manual stock adjustment, return/exchange approval — are correct **by construction**: each runs
inside a single transaction using a row lock (`SELECT … FOR UPDATE`) or a conditional `UPDATE …
WHERE` guard, not a check-then-write from application code, so a race can't produce an
inconsistent result regardless of timing. Two adjacent-window concurrent checkouts on the same
last unit of stock were not separately load-tested with concurrent clients before submission —
that's a reasonable next QA step, not a gap in the transactional design.

Before final submission, run a manual pass yourself through all three roles (customer, staff,
admin) end to end, since a fresh pair of eyes on the actual UI is worth more than a description
of what was checked during development.

## 10. AI usage

This project was built in collaboration with **Claude Code** (Anthropic), used as a coding
assistant under direction rather than as an autonomous author. Breakdown of the collaboration:

- **My decisions and direction**: chose the tech stack constraints (JSP/Servlet/JDBC, no
  `@WebServlet`, MySQL, Tomcat 9), set up and owned the MySQL instance and credentials, made
  the scope call on feature depth vs. breadth, directed priorities and course-corrected several
  times during the build (e.g. asked for a static-code-review pass instead of live testing at
  one point, asked for real product photos instead of the placeholder icons Claude proposed
  first, caught and reported that images weren't rendering when viewed in my own Eclipse-run
  instance), and did the final review of the running app myself before pushing.
- **Claude's contribution**: generated the bulk of the database schema, DAO/servlet/filter/JSP
  code, the security implementation (password hashing, CSRF, RBAC filters, audit logging),
  local build/deploy tooling, and this documentation, then iterated on it based on my feedback
  and bug reports (including catching and fixing a couple of real bugs itself during a review
  pass — an HTML parsing bug in the category admin screen, and a CSP header that would have
  silently broken inline scripts).

I reviewed the code, ran the app myself, and directed the fixes and features described above;
this README, the SECURITY.md writeup, and most of the line-level implementation were
AI-generated and reviewed rather than hand-typed from scratch.

## Known limitations

- No connection pool (plain `DriverManager` per call) — fine at demo scale, not production load.
- `LoginAttemptTracker` is in-memory, per-instance — resets on redeploy, doesn't share state
  across multiple app instances.
- Delivery fee is a flat constant rather than distance/weight-based pricing.
- No email/SMS notifications on order status changes (audit log + in-app order detail only).
- No automated test suite (JUnit) — testing was manual/functional against the running app,
  documented above.
