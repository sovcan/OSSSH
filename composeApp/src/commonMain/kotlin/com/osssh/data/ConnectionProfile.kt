package com.osssh.data

/** Immutable, type-safe model for a saved connection. Secrets are never stored here. */
data class ConnectionProfile(
    val id: String,
    val name: String,
    val host: String,
    val port: Int = 22,
    val user: String,
    val auth: AuthSpec,
    val strictHostKeyChecking: Boolean = true,
) {
    init {
        require(host.isNotBlank()) { "host must not be blank" }
        require(port in 1..65535) { "invalid port" }
        require(user.isNotBlank()) { "user must not be blank" }
    }

    sealed interface AuthSpec {
        data object InteractivePassword : AuthSpec
        data object SavedSecret : AuthSpec
        data class PrivateKey(val keyPath: String, val passphraseSaved: Boolean = false) : AuthSpec
    }
}
