# Focused defensive security improvements (8 November 2026)

This GitHub **fork** preserves its connection to the original project. The original Java/JSP application belongs to its upstream authors; this branch adds independently written defensive code and tests, without claiming ownership of upstream work.

## Implemented in this branch

- Added an annotated servlet filter for bid acceptance, bid rejection, and notice removal: reject GET with 405; require an authenticated admin session and per-session CSRF token for POST.
- Changed the three corresponding JSP actions from GET links to POST forms with synchronizer tokens.
- Fixed nullable role/username checks and early-return behavior on the two protected views and three associated servlet handlers.
- Added tested HTML output encoding for the tender ID, bid/notice fields, and status messages. Replaced legacy inline JavaScript interpolation in the three changed action handlers with escaped status markup.
- Added four JUnit tests for CSRF, role enforcement, and HTML encoding, plus source-level regression tests and CI.

## What is **not** fixed yet

- The legacy login flow still includes hard-coded default admin credentials and plaintext password handling. **Never deploy this fork to an internet-facing service or use real accounts or financial data.**
- Other JSP pages/servlets may read passwords from sessions or omit authorization/CSRF checks.
- Other output contexts, object-level authorization, SQL/database interactions, and concurrency are not comprehensively audited.
- A passing CI build is not a live, authenticated integration test. Production use would require a full controlled security review and safe credential migration.

## Reproduce

```sh
mvn -q -f tendermanagement/pom.xml test
node --test security/*.test.mjs
```

No live third-party systems were scanned or exploited. This is an owned-fork educational security contribution, **not** an Anthropic CVP approval guarantee.
