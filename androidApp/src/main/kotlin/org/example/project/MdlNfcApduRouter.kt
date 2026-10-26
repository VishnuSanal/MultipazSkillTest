package org.example.project

import kotlinx.io.bytestring.ByteString
import org.multipaz.mdoc.nfc.MdocNfcDataTransferService
import org.multipaz.nfc.Nfc
import org.multipaz.nfc.NfcApduRouter
import org.multipaz.nfc.NfcApduService

/** Android HCE entry point; iOS does not support Multipaz holder NFC presentation. */
class MdlNfcApduRouter : NfcApduRouter() {
    override fun buildServices(): Map<ByteString, NfcApduService> = mapOf(
        Nfc.NDEF_APPLICATION_ID to LocalMdlNdefService(),
        Nfc.MDOC_NFC_ENGAGEMENT_V2_AID to LocalMdlNfcV2Service(),
        Nfc.ISO_MDOC_NFC_DATA_TRANSFER_APPLICATION_ID to MdocNfcDataTransferService(),
    )
}
