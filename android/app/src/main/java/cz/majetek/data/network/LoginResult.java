package cz.majetek.data.network;

public class LoginResult {
    public final String token;
    public final long expiresAtEpochMillis;

    public LoginResult(String token, long expiresAtEpochMillis) {
        this.token = token;
        this.expiresAtEpochMillis = expiresAtEpochMillis;
    }
}
