# Deployment guide — Render + Aiven (both free, no credit card)

Two accounts needed (I can't create these for you — sign-up is a manual step).
Everything after that, I can drive for you if you share the connection details,
or you can follow these steps yourself.

## 1. Free MySQL on Aiven

1. Sign up at https://aiven.io (email or GitHub — no card required).
2. Create a new service → **MySQL** → Free plan → any region close to you.
3. Wait for it to go "Running" (~2 min), then open the service's **Overview** tab
   and copy: **Host**, **Port**, **User** (usually `avnadmin`), **Password**,
   and note the default database name (usually `defaultdb`).
4. Open the **Query editor** (or connect with any MySQL client using those
   details) and run the contents of [`db/schema.sql`](db/schema.sql) against
   that database, then optionally seed demo data — see step 4 below.

Build the JDBC URL as:
```
jdbc:mysql://<HOST>:<PORT>/<DATABASE>?useSSL=true&requireSSL=false&verifyServerCertificate=false&serverTimezone=UTC&characterEncoding=UTF-8
```
Aiven enforces SSL; `verifyServerCertificate=false` lets the driver connect
without bundling Aiven's CA certificate into the image (fine for a demo
deployment, not for production).

## 2. Deploy the app on Render

1. Sign up at https://render.com (GitHub login is fastest — no card required).
2. **New** → **Blueprint** → connect the `shreyadike04/MiniDmart` GitHub repo.
   Render will detect [`render.yaml`](render.yaml), which points at the
   [`Dockerfile`](Dockerfile) (multi-stage build: compiles the servlet classes,
   then deploys them into an official Tomcat 9 image as the ROOT webapp — no
   Maven/Gradle needed, matching the project's plain-WTP-project setup).
3. When prompted for environment variables, fill in:
   - `DB_URL` — the JDBC URL from step 1
   - `DB_USER` — from Aiven (e.g. `avnadmin`)
   - `DB_PASSWORD` — from Aiven
   - `DB_DRIVER` is already set to `com.mysql.cj.jdbc.Driver` in `render.yaml`
4. Deploy. First build takes a few minutes (compiling + pulling the Tomcat
   base image). Render gives you a public URL like `https://minidmart.onrender.com`.

Note: Render's free web services sleep after 15 minutes idle and take
30-60 seconds to wake on the next request — expected on the free tier, not a
bug. Mention this if a grader hits a slow first load.

## 3. Seed demo data (test accounts + products)

Once the schema is loaded, seed data can be added either by running the SQL
in `db/schema.sql`'s companion seed step yourself via Aiven's query editor,
or by running `DataSeeder` locally against the Aiven database:

```
java -DDB_URL="jdbc:mysql://<HOST>:<PORT>/<DATABASE>?useSSL=true&requireSSL=false&verifyServerCertificate=false" \
     -DDB_USER=<user> -DDB_PASSWORD=<password> -DDB_DRIVER=com.mysql.cj.jdbc.Driver \
     -cp "build/classes;src/main/webapp/WEB-INF/lib/mysql-connector-j-8.4.0.jar" \
     com.minidmart.tools.DataSeeder
```

(`DataSeeder` reads `DB_URL`/`DB_USER`/`DB_PASSWORD`/`DB_DRIVER` from
environment variables via `DBUtil`, the same mechanism the deployed app uses
— System properties set with `-D` are read as environment variables are not
directly interchangeable in Java, so on Windows PowerShell use
`$env:DB_URL="..."` etc. instead of `-D` before running the `java` command.)

This creates the three test accounts (`admin@minidmart.com` / `Admin@123`,
`staff@minidmart.com` / `Staff@123`, `customer@minidmart.com` / `Customer@123`),
24 products with real photos, and pickup slots for the next 5 days.
