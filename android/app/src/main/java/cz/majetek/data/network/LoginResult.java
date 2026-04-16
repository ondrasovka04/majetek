package cz.majetek.data.network;

/**
 * Login result – JWT token and expiration epoch millis.
 */
public record LoginResult(String token, long expiresAtEpochMillis) {
}
