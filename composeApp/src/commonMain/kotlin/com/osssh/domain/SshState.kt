package com.osssh.domain

import com.osssh.data.ConnectionProfile

sealed interface SshState {
    data object Idle : SshState
    data class Connecting(val profile: ConnectionProfile) : SshState
    data class Connected(val profile: ConnectionProfile, val serverBanner: String) : SshState
    data class Disconnected(val reason: String) : SshState
    data class Error(val message: String) : SshState
}
