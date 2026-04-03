# Ping API

Spring Boot sample with a **debug headers** endpoint for inspecting HTTP exchanges (request and response headers) as JSON—useful for proxies, gateways, and client debugging.

## Debug headers endpoint

### What it does

`GET /api/debug/headers` returns JSON describing:

- Request method, URI, and query string
- Request headers and response headers (multi-map style: name → list of values)
- HTTP response status

So you can confirm which headers the server actually saw on the way in, and which it sent on the way out (similar spirit to `curl -v`, but structured).

A common use case is **verifying what a load balancer or ingress forwards**: send `X-Forwarded-For`, `X-Forwarded-Proto`, `X-Forwarded-Host`, or custom tracing headers to the LB, then call this endpoint **through the same path** and inspect `requestHeaders`. If a header disappears between your client and this JSON, something **in front of the JVM** (LB, proxy, WAF) removed or replaced it.

This endpoint does **not** drop request header names. It lists what the servlet container exposes on `HttpServletRequest`, and only **replaces values** with `[REDACTED]` for sensitive names (built-ins such as `Authorization`, plus `app.security.obfuscated-header-names` and the debug token header when configured). Anything else—including `X-Forwarded-For` by default—is shown as received.

### Why a servlet filter rewrites the JSON

The controller builds a first draft of the body with placeholder `responseStatus` and empty `responseHeaders`, because at controller time the response is not finished yet. **`HeadersDebugResponseFinalizeFilter`** wraps the response, lets the full Spring MVC chain run, then parses the cached JSON and **patches** `responseStatus` and `responseHeaders` to match the **final** response (after filters, error handling, etc.). That keeps the payload aligned with what the client receives.

## Access control (one shared secret)

Everything under **`/api/debug/**`** is protected by Spring Security using a **single** configured secret (`app.security.debug-headers-access-token`), not application user accounts.

The client must send that **same** value in either:

1. **Header** (default name: `X-Debug-Token`), or  
2. **Query parameter** (default name: `debug-token`) if the header is missing or blank.

There is only one secret; the query param is an alternate transport, not a second password.

Comparison uses a **constant-time** check (`MessageDigest.isEqual`) to reduce timing leaks.

### If the secret is unset

If `debug-headers-access-token` is empty, **no one** can access `/api/debug/**` (authorization denies all). This is the safe default for environments where you do not intend to expose the debug API.

### Prefer the header over the query string

Query strings appear in browser history, referrers, and access logs more often than custom headers. Use **`X-Debug-Token`** (or your configured header name) when you can.

## Configuration

| Mechanism | Purpose |
| --------- | ------- |
| **`DEBUG_HEADERS_ACCESS_TOKEN`** (environment) | Recommended: set the secret here; `application.yml` maps it into `app.security.debug-headers-access-token`. |
| **`app.security.debug-headers-*`** | Override header name, query param name, or bind the token from another source. |

Local workflow:

1. Copy `.env.example` to `.env` and set `DEBUG_HEADERS_ACCESS_TOKEN`.
2. `source scripts/source-env.sh` before `mvn spring-boot:run`, or run **`./scripts/dev-server.sh`**, which loads env for you.

Optional: **`app.security.obfuscated-header-names`** adds extra header names (case-insensitive) whose values are redacted in logs and in the JSON dump, in addition to the built-in list (`Authorization`, `Cookie`, `Set-Cookie`, `X-Api-Key`, etc.).

When the debug token is configured, the **debug token header name** is also treated as sensitive: its value is shown as `[REDACTED]` in logs and in the `/api/debug/headers` JSON so a captured response does not leak the secret.

The **`debug-token` query value** is redacted in **logs** and in the **`queryString` field** of the JSON body when the access token is configured.

## OpenAPI / Swagger UI

SpringDoc documents the debug token as security schemes (header and query). **`springdoc.swagger-ui.persistAuthorization`** is enabled so the “Authorize” values survive a page reload while you iterate.

## Other behavior (not debug-auth)

**Echo headers:** If the request includes the configured trigger header (default **`X-Echo-Request`**), the response gets the configured response header (default **`X-Echo-Response`**) set to the same value. This is independent of `/api/debug/**` and is **not** protected by the debug token.

**Request/response logging:** At debug log level, **`LogFilter`** logs each exchange in a curl `-v`-style form, with the same header obfuscation and query redaction rules as above.

## Quick try

With the app on `http://localhost:8080` and `DEBUG_HEADERS_ACCESS_TOKEN` set:

```bash
./scripts/request.sh
```

Or manually:

```bash
curl -sS -H "Accept: application/json" -H "X-Debug-Token: $DEBUG_HEADERS_ACCESS_TOKEN" \
  http://localhost:8080/api/debug/headers
```

## Build and CI

- **JDK 21** is required; `maven-enforcer-plugin` fails the build on older JDKs during `validate`.
- **`mvn -B verify`** or **`make verify`**: Spotless (format/imports), tests, and packaging—the same command [GitHub Actions](.github/workflows/ci.yml) runs.
- **`make lint`** / **`make format`**: Spotless check or apply only.

[Dependabot](.github/dependabot.yml) proposes weekly updates for Maven dependencies and GitHub Actions.

## Production note

Treat **`DEBUG_HEADERS_ACCESS_TOKEN`** like any other secret: strong random value, stored in a secret manager, and **omit or leave empty** in environments where `/api/debug/**` should not be reachable.
