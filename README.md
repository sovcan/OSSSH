# OSSSH

An open-source, type-safe SSH client written in Kotlin with **Compose Multiplatform**.
Runs on **Android** (APK) and **Linux desktop** (package / runnable JAR) from one shared codebase.

## Features

- One shared Kotlin codebase (`commonMain`) for Android + desktop JVM
- SSH2 via [sshj] — password and private-key (OpenSSH format) auth
- Strict host-key checking against a `known_hosts` file (TOFU warning: disable only if you accept MITM risk)
- Secrets stored encrypted: Android Keystore / EncryptedSharedPreferences on Android, AES-256-GCM with a `0600`-permission key file on desktop
- Type-safe domain model: `sealed interface SshState`, immutable `ConnectionProfile` with validated init

## Security notes

- Cleartext traffic disabled app-wide on Android (`network_security_config`), `allowBackup=false`
- Secrets never live in `ConnectionProfile`; they are referenced by ID and kept in the encrypted store
- PTY sessions run on a coroutine scope that is cancelled with the session (no leaked threads)

> The desktop secure store protects secrets at rest on a multi-user machine, but any process
> running as *your* user can read `~/.osssh/key.bin`. Prefer passphrase-protected private keys.

## Build

Requirements: JDK 17+, Gradle 8.10+ (or generate a wrapper once with `gradle wrapper`), Android SDK (for APK builds).

```bash
# Android debug APK -> composeApp/build/outputs/apk/debug/
gradle :composeApp:assembleDebug

# Signed release APK — create a keystore and configure signing first
gradle :composeApp:assembleRelease

# Linux desktop: run from sources
gradle :composeApp:run

# Linux desktop: native package (deb, requires fakeroot+dpkg) -> composeApp/build/compose/binaries/main/deb/
gradle :composeApp:packageReleaseDeb
```

CI: every push to GitHub builds the debug APK and the `.deb` and uploads them as workflow artifacts.

## Roadmap

- [ ] Full VT100/xterm terminal emulation (current UI is a basic line-oriented shell)
- [ ] `ssh-agent`-style key management UI
- [ ] SFTP file browser pane
- [ ] Jump-host (ProxyCommand) support
- [ ] Connection profiles persisted across restarts (currently in-memory)

## License

MIT — see LICENSE.

[sshj]: https://github.com/hierynomus/sshj
