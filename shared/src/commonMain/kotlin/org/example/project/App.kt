package org.example.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.multipaz.compose.permissions.rememberBluetoothEnabledState
import org.multipaz.compose.permissions.rememberBluetoothPermissionState
import org.multipaz.compose.prompt.PromptDialogs
import org.multipaz.util.Platform
import kotlinx.coroutines.launch

@Composable
fun App() {
    var ready by remember { mutableStateOf(false) }
    var failure by remember { mutableStateOf<Throwable?>(null) }
    LaunchedEffect(Unit) {
        runCatching { LocalMdlWallet.initialize() }
            .onSuccess { ready = true }
            .onFailure { failure = it }
    }
    MaterialTheme {
        PromptDialogs(promptModel = Platform.promptModel)
        Column(
            modifier = Modifier.fillMaxSize().safeContentPadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Local specimen mDL wallet")
            Text("Development credential only; its issuer is not trusted.")
            when {
                failure != null -> Text("Wallet setup failed: ${failure!!.message}")
                !ready -> Text("Preparing secure local storage…")
                else -> BluetoothGatedPresentment()
            }
        }
    }
}

@Composable
private fun BluetoothGatedPresentment() {
    val permissions = rememberBluetoothPermissionState()
    val enabled = rememberBluetoothEnabledState()
    val scope = rememberCoroutineScope()
    when {
        !permissions.isGranted -> Button(onClick = {
            scope.launch { permissions.launchPermissionRequest() }
        }) { Text("Grant Bluetooth permissions") }
        !enabled.isEnabled -> Button(onClick = {
            scope.launch { enabled.enable() }
        }) { Text("Enable Bluetooth") }
        else -> LocalMdlPresentment()
    }
}
