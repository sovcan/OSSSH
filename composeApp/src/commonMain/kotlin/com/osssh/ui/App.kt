package com.osssh.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.osssh.domain.SshState
import com.osssh.ui.theme.OSSSHTheme

@Composable
fun App(vm: AppViewModel) {
    OSSSHTheme {
        Surface(Modifier.fillMaxSize()) {
            val ui by vm.ui.collectAsState()
            Box(Modifier.fillMaxSize().safeContentPadding()) {
                when (val s = ui.sshState) {
                    SshState.Idle -> ConnectionListScreen(
                        profiles = ui.profiles,
                        onAdd = vm::addProfile,
                        onConnect = vm::connect,
                    )
                    is SshState.Connecting -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    is SshState.Connected -> TerminalScreen(
                        lines = ui.terminal,
                        banner = s.serverBanner,
                        onSend = vm::send,
                        onDisconnect = vm::disconnect,
                    )
                    is SshState.Disconnected ->
                        ConnectionListScreen(ui.profiles, vm::addProfile, vm::connect, notice = s.reason)
                    is SshState.Error ->
                        ConnectionListScreen(ui.profiles, vm::addProfile, vm::connect, notice = s.message)
                }
            }
        }
    }
}
