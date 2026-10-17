package com.osssh.ssh

import com.osssh.data.ConnectionProfile
import com.osssh.domain.SshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.io.File

/**
 * One live SSH connection with a PTY shell. All IO on Dispatchers.IO;
 * state and terminal output are exposed as flows.
 */
class SshSession(
    private val scope: CoroutineScope,
    private val knownHosts: File,
) : AutoCloseable {
    private val _state = MutableStateFlow<SshState>(SshState.Idle)
    val state: StateFlow<SshState> = _state
    private val _scrollback = MutableSharedFlow<String>(extraBufferCapacity = 1024)
    val scrollback: Flow<String> = _scrollback

    private var client: SSHClient? = null
    private var shell: Session.Shell? = null

    fun connect(profile: ConnectionProfile, secret: String? = null) {
        _state.value = SshState.Connecting(profile)
        scope.launch(Dispatchers.IO) {
            runCatching {
                SSHClient().also { c ->
                    if (profile.strictHostKeyChecking) {
                        c.loadKnownHosts(knownHosts.apply { createNewFile() })
                    } else {
                        c.addHostKeyVerifier(PromiscuousVerifier())
                    }
                    c.connect(profile.host, profile.port)
                    authenticate(profile, c, secret)
                    c.startSession().also { sess ->
                        sess.allocatePTY("xterm-256color", 120, 40, 0, 0, emptyMap())
                        shell = sess.startShell()
                        scope.launch(Dispatchers.IO) {
                            shell?.inputStream?.bufferedReader()?.forEachLine { line ->
                                if (isActive) _scrollback.tryEmit(line)
                            }
                        }
                    }
                    client = c
                }
            }.onSuccess {
                _state.value = SshState.Connected(profile, client?.transport?.serverVersion ?: "SSH server")
            }.onFailure { e ->
                _state.value = SshState.Error(e.message ?: "connection failed")
                close()
            }
        }
    }

    private fun authenticate(profile: ConnectionProfile, ssh: SSHClient, secret: String?) {
        when (val a = profile.auth) {
            is ConnectionProfile.AuthSpec.PrivateKey ->
                ssh.authPublickey(
                    profile.user,
                    ssh.loadKeys(a.keyPath, secret?.takeIf(String::isNotBlank)),
                )
            ConnectionProfile.AuthSpec.SavedSecret,
            ConnectionProfile.AuthSpec.InteractivePassword ->
                ssh.authPassword(profile.user, secret ?: error("password required"))
        }
    }

    fun write(line: String) {
        scope.launch(Dispatchers.IO) {
            shell?.outputStream?.write((line + "\n").toByteArray(Charsets.UTF_8))
            shell?.outputStream?.flush()
        }
    }

    override fun close() {
        _state.value = SshState.Disconnected("closed")
        runCatching { shell?.close() }
        runCatching { client?.disconnect() }
        scope.cancel()
    }
}
