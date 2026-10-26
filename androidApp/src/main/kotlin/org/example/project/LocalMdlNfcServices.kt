package org.example.project

import org.multipaz.compose.prompt.PresentmentActivity
import org.multipaz.crypto.EcCurve
import org.multipaz.mdoc.nfc.MdocNdefService
import org.multipaz.mdoc.nfc.MdocNfcV2Service
import org.multipaz.mdoc.transport.MdocTransportOptions

private fun source() = LocalMdlWallet.presentmentSource

class LocalMdlNdefService : MdocNdefService() {
    override suspend fun getSettings(): Settings {
        LocalMdlWallet.initialize()
        if (!PresentmentActivity.presentmentModel.isActive) {
            PresentmentActivity.presentmentModel.reset(source(), emptyList())
        }
        return Settings(source(), PresentmentActivity.promptModel, PresentmentActivity.presentmentModel,
            { PresentmentActivity.startActivity() }, EcCurve.P256, false, emptyList(), true, true, true,
            MdocTransportOptions(bleUseL2CAP = true, bleUseL2CAPInEngagement = true))
    }
}

class LocalMdlNfcV2Service : MdocNfcV2Service() {
    override suspend fun getSettings(): Settings {
        LocalMdlWallet.initialize()
        if (!PresentmentActivity.presentmentModel.isActive) {
            PresentmentActivity.presentmentModel.reset(source(), emptyList())
        }
        return Settings(source(), PresentmentActivity.promptModel, PresentmentActivity.presentmentModel,
            { PresentmentActivity.startActivity() }, EcCurve.P256, false, emptyList(),
            MdocTransportOptions(bleUseL2CAP = true, bleUseL2CAPInEngagement = true))
    }
}
