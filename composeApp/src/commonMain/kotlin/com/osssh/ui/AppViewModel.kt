package com.osssh.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osssh.data.ConnectionProfile
import com.osssh.domain.SshState
import com.osssh.security.SecureStore
import com.osssh.ssh.SshSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class UiState(
    val profiles: List<ConnectionProfile> = emptyList(),
    val sshState: SshState = SshState.Idle,
    val terminal: List<String> = emptyList(),
)

class AppViewModel(
    private val store: SecureStore,
    private val knownHostsPath: String,
) : ViewModel() {
    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private var session: SshSession? = null

    fun addProfile(p: ConnectionProfile, secretToSave: String? = null) {
        _ui.update { it.copy(profiles = it.profiles + p) }
        if (secretToSave != null && p.auth == ConnectionProfile.AuthSpec.SavedSecret) {
            viewModelScope.launch { store.put("secret:${p.id}", secretToSave) }
        }
    }

    fun connect(p: ConnectionProfile, interactiveSecret: String? = null) {
        session?.close()
        session = SshSession(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            knownHosts = java.io.File(knownHostsPath),
        ).also { s ->
            viewModelScope.launch { s.state.collect { st -> _ui.update { it.copy(sshState = st) } } }
            viewModelScope.launch {
                s.scrollback.collect { line ->
                    _ui.update { it.copy(terminal = (it.terminal + line).takeLast(2_000)) }
                }
            }
        }
        viewModelScope.launch {
            val secret = interactiveSecret
                ?: (if (p.auth == ConnectionProfile.AuthSpec.SavedSecret) store.get("secret:${p.id}") else null)
            session?.connect(p, secret)
        }
    }

    fun send(cmd: String) = session?.write(cmd) ?: Unit

    fun disconnect() {
        session?.close()
        session = null
    }

    override fun onCleared() = disconnect()
}
