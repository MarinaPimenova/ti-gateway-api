The current flow is **React → Gateway → Okta part is basically correct**.

The important clue is this URL:

```text
http://localhost:8080/login/oauth2/code/okta?code=...&state=...
```

This means **Okta successfully authenticated the user and redirected the browser back to `ti-gateway-api`**. The problem is therefore most likely happening **after the callback**, when Spring Security processes the authorization code and creates the local session.

### Expected flow

Your architecture should work like this:

```text
React UI :3000
    |
    | GET /oauth2/authorization/okta
    v
Gateway :8080
    |
    | Authorization Code Flow
    v
Okta Hosted Login
    |
    | user authenticates
    v
Gateway :8080/login/oauth2/code/okta
    |
    | exchange code for tokens
    v
Okta
    |
    | ID token / access token
    v
Gateway
    |
    | create authenticated SecurityContext
    | create JSESSIONID/session cookie
    v
React UI
```

The fact that you reach:

```text
/login/oauth2/code/okta?code=...&state=...
```

means the failure is probably in this part:

```text
Gateway
   |
   | code -> token exchange
   v
Okta
```

or immediately afterward.

## 1. First check the redirect URI

Your configuration says:

```yaml
redirect-uri: ${app.application-url}/login/oauth2/code/okta
```

If:

```yaml
app:
  application-url: http://localhost:8080
```

then Spring sends:

```text
http://localhost:8080/login/oauth2/code/okta
```

This **must exactly match** the redirect URI configured in the Okta application.

Check Okta:

```text
Applications
  → Applications
    → your application
      → General
        → Sign-in redirect URIs
```

It should contain:

```text
http://localhost:8080/login/oauth2/code/okta
```

Be careful about:

```text
http://localhost:8080/login/oauth2/code/okta/
```

versus:

```text
http://localhost:8080/login/oauth2/code/okta
```

The trailing `/` matters.

---

## 2. Check `OKTA_DOMAIN`

You currently have:

```yaml
provider:
  okta:
    issuer-uri: https://${OKTA_DOMAIN}/
```

For example, if:

```text
OKTA_DOMAIN=dev-123456.okta.com/oauth2/default
```

the resulting issuer is:

```text
https://dev-123456.okta.com/oauth2/default/
```

That needs to correspond to the **authorization server that issued the authorization code**.

For an Okta custom authorization server, this is typically:

```text
https://{your-okta-domain}/oauth2/default
```

Do not accidentally configure:

```text
https://{your-okta-domain}
```

when your application is using:

```text
https://{your-okta-domain}/oauth2/default
```

---

## 3. Most important: verify Client ID and Client Secret

Your configuration contains:

```yaml
client-id: ${OKTA_OAUTH2_CLIENT_ID}
client-secret: ${OKTA_OAUTH2_CLIENT_SECRET}
```

The `client-id` and `client-secret` must belong to **the same Okta application** that contains:

```text
http://localhost:8080/login/oauth2/code/okta
```

A very common problem is:

```text
Okta Application A
    ↓
Client ID A
Redirect URI A

Gateway
    ↓
Client ID B
Client Secret B
```

The browser can still reach Okta, but the authorization-code exchange fails afterward.

---

## 4. Check the Okta application type

For your architecture, the Gateway is the OAuth2 client.

Therefore, the Okta application should be configured as a **Web Application**, not a SPA application.

Your architecture is:

```text
React SPA
    |
    | browser redirect
    v
ti-gateway-api
    |
    | OAuth2 Client
    v
Okta
```

The client secret belongs on the Gateway, **not in React**.

This is exactly the reason your design of:

> session between UI and Gateway

is useful.

The React application does not need to possess the OAuth client secret.

---

## 5. Your `scope` is probably too minimal

You currently have:

```yaml
scope: openid
```

I would recommend:

```yaml
scope:
  - openid
  - profile
  - email
```

So:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          okta:
            provider: okta
            authorization-grant-type: authorization_code
            scope:
              - openid
              - profile
              - email
            client-id: ${OKTA_OAUTH2_CLIENT_ID}
            client-secret: ${OKTA_OAUTH2_CLIENT_SECRET}
            redirect-uri: ${app.application-url}/login/oauth2/code/okta
```

`openid` is required for OpenID Connect.

`profile` and `email` are useful because your application apparently creates a user profile containing information such as:

```text
email
given_name
family_name
username
roles
```

---

## 6. Your session configuration is appropriate for this architecture

This part is actually aligned with your intended architecture:

```java
.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
```

You want:

```text
React
  |
  | JSESSIONID
  v
Gateway
  |
  | SecurityContext
  v
Authenticated user
```

After successful OAuth2 login, Spring Security creates the authenticated session.

You should **not** put the Okta access token into:

```text
localStorage
sessionStorage
React state
```

for this architecture.

---

## 7. I would change `sessionFixation().none()`

You currently have:

```java
.sessionFixation().none()
```

I would **not recommend disabling session fixation protection**.

Prefer Spring Security's default protection:

```java
http.sessionManagement(session -> session
        .invalidSessionStrategy(
                new SimpleRedirectInvalidSessionStrategy(LOGOUT_URL)
        )
        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
);
```

In other words, remove:

```java
.sessionFixation().none()
```

Session fixation protection is important specifically when an unauthenticated session becomes authenticated.

---

## 8. Check the session cookie

After successful login you should eventually see something like:

```text
Set-Cookie: JSESSIONID=...
```

or, depending on your Spring/session configuration:

```text
SESSION=...
```

The browser should then send:

```text
Cookie: JSESSIONID=...
```

to:

```text
http://localhost:8080
```

Your React application at:

```text
http://localhost:5000
```

does **not** need to read this cookie.

That's an important security characteristic of your architecture.

---

# 9. Your CORS configuration

You have:

```java
configuration.setAllowedOrigins(
        Arrays.asList(allowedOrigins.split(","))
);
configuration.setAllowCredentials(true);
```

This is correct for a session-cookie architecture, assuming `allowedOrigins` contains:

```text
http://localhost:5000
```

For example:

```text
allowed-origins=http://localhost:5000
```

Don't use:

```text
*
```

together with:

```java
setAllowCredentials(true)
```

---

# 10. One thing I would change in your login endpoint

Your React code:

```typescript
window.location.href =
    apiServerUrl + '/oauth2/authorization/okta' + redirectId;
```

is fine **if `redirectId` is intentionally used by your application**.

Otherwise the simplest version is:

```typescript
window.location.href =
    `${apiServerUrl}/oauth2/authorization/okta`;
```

The important point is that React should **not** implement the OAuth2 authorization-code exchange.

It should simply redirect the browser:

```text
React
 ↓
/oauth2/authorization/okta
 ↓
Okta
 ↓
/login/oauth2/code/okta
```

Spring Security handles the rest.

---

# 11. Enable Spring Security OAuth2 logging

This is probably the most useful next step because your screenshot/attachment error itself isn't visible in the text I received.

Temporarily add:

```yaml
logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.security.oauth2: DEBUG
    org.springframework.web.client: DEBUG
```

Then restart `ti-gateway-api` and perform login again.

You should see where the failure occurs:

```text
OAuth2AuthorizationRequestRedirectFilter
        ↓
Okta
        ↓
OAuth2LoginAuthenticationFilter
        ↓
Authorization Code
        ↓
Token endpoint
        ↓
UserInfo endpoint
        ↓
AuthenticationSuccessHandler
        ↓
HTTP session
```

The **first exception after `/login/oauth2/code/okta`** is what we need.

---

## 12. Most likely causes in your case

Based on the information you've provided, I would check them in this order:

| Priority | Check                       | Typical problem                                     |
| -------- | --------------------------- | --------------------------------------------------- |
| 1        | `OKTA_OAUTH2_CLIENT_ID`     | Wrong client                                        |
| 2        | `OKTA_OAUTH2_CLIENT_SECRET` | Wrong/expired secret                                |
| 3        | Okta redirect URI           | Doesn't exactly match `localhost:8080` callback     |
| 4        | `OKTA_DOMAIN`        | Wrong authorization server                          |
| 5        | Okta application type       | Should be Web Application                           |
| 6        | `scope`                     | Add `openid profile email`                          |
| 7        | Custom success handler      | Error occurs after successful authentication        |
| 8        | Session/cookie              | Authentication succeeds but session isn't preserved |

### One particularly important distinction

If the error in your attachment says something similar to:

```text
invalid_client
Client authentication failed
```

then focus on:

```text
OKTA_OAUTH2_CLIENT_ID
OKTA_OAUTH2_CLIENT_SECRET
```

If it says:

```text
invalid_grant
```

focus on:

```text
redirect-uri
issuer
authorization code
```

If it says:

```text
401 Unauthorized
```

after authentication, look at your:

```java
authorizeHttpRequests(...)
```

and custom authentication/success handler.

If it says:

```text
UserInfo endpoint
```

then look at:

```yaml
scope:
  - openid
  - profile
  - email
```

and your Okta claims/userinfo configuration.

---

### One final architectural point

Your intended architecture is sound:

```text
                    ┌─────────────┐
                    │    Okta     │
                    └──────┬──────┘
                           │
                    OAuth2 Authorization
                      Code Flow
                           │
                           ▼
┌─────────────┐      ┌──────────────┐
│   React UI  │─────▶│   Gateway    │
│   :3000     │      │    :8080     │
└─────────────┘      │ OAuth2 Client│
                     │              │
                     │ HTTP Session │
                     └──────┬───────┘
                            │
                     JWT / Bearer Token
                            │
              ┌─────────────┼─────────────┐
              ▼             ▼             ▼
        Knowledge      Orchestrator     ...
        Resource       Resource Server
        Server
```

**UI ↔ Gateway:** HTTP session/cookie.

**Gateway ↔ backend microservices:** OAuth2 access token/JWT.

That is the model I would keep.

If you paste the **exact exception/stack trace from `ti-gateway-api` after `/login/oauth2/code/okta`** (especially the `Caused by:` section), I can identify the exact configuration problem rather than just the likely causes.
