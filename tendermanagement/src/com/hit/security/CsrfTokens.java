package com.hit.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.servlet.http.HttpSession;

/** A per-session synchronizer token for sensitive form submissions. */
public final class CsrfTokens {
    private static final String ATTRIBUTE = CsrfTokens.class.getName() + ".value";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfTokens() { }

    /**
     * Returns the existing session token, creating a cryptographically random one if absent.
     * All forms in one session share the same token, so it must not be logged or exposed in URLs.
     *
     * @param session the authenticated session, never null
     * @return a 43-character URL-safe token
     * @throws IllegalArgumentException if the session is null
     */
    public static String getOrCreate(HttpSession session) {
        if (session == null) throw new IllegalArgumentException("An authenticated session is required");
        synchronized (session) {
            Object existing = session.getAttribute(ATTRIBUTE);
            if (existing instanceof String && !((String) existing).isEmpty()) return (String) existing;
            byte[] randomBytes = new byte[32];
            RANDOM.nextBytes(randomBytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
            session.setAttribute(ATTRIBUTE, token);
            return token;
        }
    }

    /**
     * Checks a form token against the server-side session value without timing-dependent comparison.
     * This method deliberately never creates a session or new token.
     *
     * @param session the existing HTTP session, if any
     * @param submitted token supplied in the POST form body
     * @return true only when the submitted token matches the session token
     */
    public static boolean verify(HttpSession session, String submitted) {
        if (session == null || submitted == null || submitted.length() != 43) return false;
        Object expected = session.getAttribute(ATTRIBUTE);
        if (!(expected instanceof String)) return false;
        return MessageDigest.isEqual(
                ((String) expected).getBytes(StandardCharsets.US_ASCII),
                submitted.getBytes(StandardCharsets.US_ASCII));
    }
}
