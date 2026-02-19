# ── Stack traces ─────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Annotations & Signature (Room, Gson, etc.) ──────────────
-keepattributes *Annotation*,Signature

# ── App model classes ────────────────────────────────────────
# Room entities — field names must match DB column names.
-keep class cz.majetek.data.entity.** { *; }
# Room DAOs and database.
-keep class cz.majetek.data.db.** { *; }
# LoginResult Java record — prevent accessor renaming.
-keep class cz.majetek.data.network.LoginResult { *; }

# ── OkHttp — suppress warnings for optional platform classes ─
-dontwarn okhttp3.internal.platform.**
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**