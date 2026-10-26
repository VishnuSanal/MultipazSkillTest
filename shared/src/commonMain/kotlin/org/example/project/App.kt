package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.multipaz.compose.permissions.rememberBluetoothEnabledState
import org.multipaz.compose.permissions.rememberBluetoothPermissionState
import org.multipaz.compose.presentment.MdocProximityQrPresentment
import org.multipaz.compose.presentment.MdocProximityQrSettings
import org.multipaz.compose.qrcode.generateQrCode
import org.multipaz.mdoc.connectionmethod.MdocConnectionMethodBle
import org.multipaz.mdoc.transport.MdocTransportOptions
import org.multipaz.util.Platform
import org.multipaz.util.UUID
import kotlinx.coroutines.launch

@Composable
fun App() {
    var ready by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        runCatching { LocalMdlWallet.initialize() }
            .onSuccess { ready = true }
            .onFailure { error = it.message ?: "Unable to initialize the local wallet" }
    }
    MaterialTheme {
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding().padding(20.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Local mDL holder", style = MaterialTheme.typography.headlineSmall)
            Text("This wallet contains a locally generated demo credential. It is not issuer-trusted or suitable for production identity use.")
            when {
                error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                !ready -> Text("Preparing secure local storage…")
                else -> LocalMdlPresentment()
            }
        }
    }
}

@Composable
private fun LocalMdlPresentment() {
    val bluetoothPermissions = rememberBluetoothPermissionState()
    val bluetoothEnabled = rememberBluetoothEnabledState()
    val coroutineScope = rememberCoroutineScope()
    when {
        !bluetoothPermissions.isGranted -> Button(
            onClick = { coroutineScope.launch { bluetoothPermissions.launchPermissionRequest() } },
        ) { Text("Allow Bluetooth for QR presentment") }
        !bluetoothEnabled.isEnabled -> Button(
            onClick = { coroutineScope.launch { bluetoothEnabled.enable() } },
        ) { Text("Enable Bluetooth") }
        else -> MdocProximityQrPresentment(
            source = LocalMdlWallet.presentmentSource,
            promptModel = Platform.promptModel,
            prepareSettings = { createQr ->
                Button(onClick = {
                    val serviceUuid = UUID.randomUUID()
                    createQr(MdocProximityQrSettings(
                        availableConnectionMethods = listOf(
                            MdocConnectionMethodBle(true, false, serviceUuid, null),
                            MdocConnectionMethodBle(false, true, null, serviceUuid),
                        ),
                        createTransportOptions = MdocTransportOptions(
                            bleUseL2CAP = true, bleUseL2CAPInEngagement = true,
                        ),
                    ))
                }) { Text("Present mDL via QR") }
            },
            showQrCode = { uri, cancel ->
                val qrCode = remember(uri) { generateQrCode(uri) }
                Text("Scan this fresh device-engagement QR code with a compatible mdoc verifier.")
                Image(
                    bitmap = qrCode,
                    contentDescription = "mDL device engagement QR code",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit,
                )
                Button(onClick = cancel) { Text("Cancel") }
            },
            showTransacting = { cancel ->
                Text("Connected to verifier; waiting for your consent…")
                Button(onClick = cancel) { Text("Cancel") }
            },
            showCompleted = { presentmentError, reset ->
                Text(presentmentError?.message ?: "Presentation complete")
                Button(onClick = reset) { Text("Done") }
            },
        )
    }
}
