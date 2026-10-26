package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.multipaz.compose.presentment.MdocProximityQrPresentment
import org.multipaz.compose.presentment.MdocProximityQrSettings
import org.multipaz.compose.qrcode.generateQrCode
import org.multipaz.mdoc.connectionmethod.MdocConnectionMethodBle
import org.multipaz.mdoc.transport.MdocTransportOptions
import org.multipaz.util.Platform
import org.multipaz.util.UUID

@Composable
fun LocalMdlPresentment() {
    MdocProximityQrPresentment(
        source = LocalMdlWallet.presentmentSource,
        promptModel = Platform.promptModel,
        prepareSettings = { generate ->
            Button(onClick = {
                val uuid = UUID.randomUUID()
                generate(MdocProximityQrSettings(
                    availableConnectionMethods = listOf(
                        MdocConnectionMethodBle(false, true, null, uuid),
                        MdocConnectionMethodBle(true, false, uuid, null),
                    ),
                    createTransportOptions = MdocTransportOptions(
                        bleUseL2CAP = true,
                        bleUseL2CAPInEngagement = true,
                    ),
                ))
            }) { Text("Present specimen mDL via QR") }
        },
        showQrCode = { uri, reset ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    bitmap = remember(uri) { generateQrCode(uri) },
                    contentDescription = "mDL device engagement QR code",
                    modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.FillWidth,
                )
                Button(onClick = reset) { Text("Cancel") }
            }
        },
        showTransacting = { reset ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Connected to reader; awaiting approval")
                Button(onClick = reset) { Text("Cancel") }
            }
        },
        showCompleted = { error, reset ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(error?.message ?: "Presentation completed")
                Button(onClick = reset) { Text("Done") }
            }
        },
    )
}
