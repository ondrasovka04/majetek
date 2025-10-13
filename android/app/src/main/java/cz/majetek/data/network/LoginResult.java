package cz.majetek.data.network;

public record LoginResult(String token, long expiresAtEpochMillis) {
}
