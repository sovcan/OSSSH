package com.osssh

import android.app.Application
import android.content.Context
import com.osssh.security.AndroidSecureStore
import com.osssh.ui.AppViewModel

class OSSSHApplication : Application() {
    val viewModel by lazy {
        AppViewModel(
            store = AndroidSecureStore(this),
            knownHostsPath = java.io.File(filesDir, "known_hosts").path,
        )
    }

    companion object {
        fun get(ctx: Context) = ctx.applicationContext as OSSSHApplication
    }
}
