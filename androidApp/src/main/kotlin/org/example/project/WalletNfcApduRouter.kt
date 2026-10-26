package org.example.project

import kotlinx.io.bytestring.ByteString
import org.multipaz.compose.prompt.PresentmentActivity
import org.multipaz.crypto.EcCurve
import org.multipaz.mdoc.nfc.MdocNdefService
import org.multipaz.mdoc.nfc.MdocNfcDataTransferService
import org.multipaz.mdoc.nfc.MdocNfcV2Service
import org.multipaz.mdoc.transport.MdocTransportOptions
import org.multipaz.nfc.Nfc
import org.multipaz.nfc.NfcApduRouter
import org.multipaz.nfc.NfcApduService

class WalletNfcApduRouter : NfcApduRouter() {
    override fun buildServices(): Map<ByteString, NfcApduService> = mapOf(
        Nfc.NDEF_APPLICATION_ID to LocalMdlNdefService(),
        Nfc.MDOC_NFC_ENGAGEMENT_V2_AID to LocalMdlNfcV2Service(),
        Nfc.ISO_MDOC_NFC_DATA_TRANSFER_APPLICATION_ID to MdocNfcDataTransferService(),
    )
}

class LocalMdlNdefService : MdocNdefService() {
    override suspend fun getSettings(): Settings {
        LocalMdlWallet.initialize()
        return Settings(
            source = LocalMdlWallet.presentmentSource,
            promptModel = PresentmentActivity.promptModel,
            presentmentModel = PresentmentActivity.presentmentModel,
            launchUi = { PresentmentActivity.startActivity() },
            sessionEncryptionCurve = EcCurve.P256,
            useNegotiatedHandover = true,
            negotiatedHandoverPreferredOrder = listOf("nfc"),
            staticHandoverBleCentralClientModeEnabled = false,
            staticHandoverBlePeripheralServerModeEnabled = false,
            staticHandoverNfcDataTransferEnabled = true,
            transportOptions = MdocTransportOptions(),
        )
    }
}

class LocalMdlNfcV2Service : MdocNfcV2Service() {
    override suspend fun getSettings(): Settings {
        LocalMdlWallet.initialize()
        return Settings(
            source = LocalMdlWallet.presentmentSource,
            promptModel = PresentmentActivity.promptModel,
            presentmentModel = PresentmentActivity.presentmentModel,
            launchUi = { PresentmentActivity.startActivity() },
            sessionEncryptionCurve = EcCurve.P256,
            onlyAllowNfc = true,
            transportOptions = MdocTransportOptions(),
        )
    }
}
