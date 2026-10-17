-keep class com.osssh.** { *; }
-dontwarn org.bouncycastle.**
# sshj uses Bouncy Castle; keep provider classes
-keep class org.bouncycastle.** { *; }
