## Security Benefits of Session-Based Authentication

The platform uses **server-side HTTP sessions** between the browser and the `ti-gateway-api` instead of exposing OAuth access tokens to the React application.

| Security Threat | JWT Stored in Browser (SPA) | Session Between Browser and Gateway | Mitigation |
|-----------------|-----------------------------|-------------------------------------|------------|
| Access token theft via XSS | High | Eliminated | Access tokens remain on the Gateway and are never exposed to JavaScript. |
| Refresh token theft | High | Eliminated | Refresh tokens are stored only by the Gateway (OAuth2 Client). |
| Token leakage through browser storage | High | Eliminated | No tokens are stored in `localStorage` or `sessionStorage`. |
| Accidental token exposure in browser debugging tools | Possible | Eliminated | The browser only contains an opaque session cookie. |
| Token reuse by malicious browser extensions | Possible | Significantly Reduced | Extensions cannot directly read access or refresh tokens because they are never available to the SPA. |
| Client-side token lifecycle management errors | Possible | Eliminated | Token refresh and expiration are handled by Spring Security on the Gateway. |
| Token replay after browser storage compromise | Possible | Reduced | Attackers cannot steal OAuth tokens from browser storage because none are stored there. |
| Exposure of OAuth implementation details to frontend developers | Possible | Reduced | OAuth/OpenID Connect logic is centralized in the Gateway. |
| Inconsistent authorization implementation | Possible | Reduced | Authentication is enforced in one place before requests reach backend services. |
| Credential exposure during frontend logging | Possible | Eliminated | The frontend never logs OAuth tokens because it never receives them. |

### Additional Advantages

- Centralized authentication using **Spring Security OAuth2 Client**
- Simpler React application (no OAuth SDK required)
- Tokens remain within the trusted backend
- Easier token rotation and renewal
- Centralized logout handling
- Better alignment with enterprise Backend-for-Frontend (BFF) architecture
- Reduced attack surface for browser-based applications

### Security Flow

```text
Browser
    │
    │ HTTPS + Secure HttpOnly Session Cookie
    ▼
ti-gateway-api (OAuth2 Client)
    │
    │ Bearer JWT Access Token
    ▼
Backend Microservices (OAuth2 Resource Servers)
```

**Note:** The session cookie should be configured with the following security attributes:

- `HttpOnly`
- `Secure`
- `SameSite=Lax` (or `Strict` where appropriate)
- Session timeout
- Session invalidation on logout

# One important clarification

A few threats are reduced, not completely eliminated. For example:

- XSS itself is not eliminated—an attacker may still manipulate the page—but OAuth access token theft via XSS is eliminated because the tokens are never exposed to browser JavaScript.
- CSRF is not automatically eliminated by using sessions. 
In fact, session-based authentication reintroduces the need for CSRF protection (which Spring Security provides). For a production system, you should explicitly mention that:
- CSRF protection is enabled.
- Session cookies are HttpOnly, Secure, and SameSite.
- All communication uses HTTPS.

This distinction is important for an enterprise-grade security document.



