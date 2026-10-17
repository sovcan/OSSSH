package com.osssh

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.osssh.security.DesktopSecureStore
import com.osssh.ui.App
import com.osssh.ui.AppViewModel
import java.io.File

fun main() = application {
    val dataDir = File(System.getProperty("user.home"), ".osssh").apply { mkdirs() }
    val vm = AppViewModel(
        store = DesktopSecureStore(dataDir),
        knownHostsPath = File(dataDir, "known_hosts").path,
    )
    Window(onCloseRequest = { exitApplication() }, title = "OSSSH") {
        App(vm)
    }
}
