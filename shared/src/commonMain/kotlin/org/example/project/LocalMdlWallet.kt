package org.example.project

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.multipaz.asn1.ASN1Integer
import org.multipaz.crypto.AsymmetricKey
import org.multipaz.crypto.Crypto
import org.multipaz.crypto.EcCurve
import org.multipaz.crypto.X500Name
import org.multipaz.crypto.X509CertChain
import org.multipaz.document.DocumentStore
import org.multipaz.document.buildDocumentStore
import org.multipaz.documenttype.DocumentTypeRepository
import org.multipaz.documenttype.knowntypes.DrivingLicense
import org.multipaz.mdoc.util.MdocUtil
import org.multipaz.presentment.PresentmentSource
import org.multipaz.presentment.SimplePresentmentSource
import org.multipaz.securearea.CreateKeySettings
import org.multipaz.securearea.SecureAreaRepository
import org.multipaz.util.Platform
import org.multipaz.util.truncateToWholeSeconds
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

/** Persistent, local-only specimen mDL. Its issuer chain is generated on-device and is untrusted. */
object LocalMdlWallet {
    private val initializationLock = Mutex()
    private var initialized = false

    lateinit var documentStore: DocumentStore
        private set
    lateinit var documentTypeRepository: DocumentTypeRepository
        private set
    lateinit var presentmentSource: PresentmentSource
        private set

    suspend fun initialize() {
        initializationLock.withLock {
        if (initialized) return
        val storage = Platform.nonBackedUpStorage
        val secureArea = Platform.getSecureArea(storage)
        documentTypeRepository = DocumentTypeRepository().apply {
            addDocumentType(DrivingLicense.getDocumentType())
        }
        documentStore = buildDocumentStore(
            storage = storage,
            secureAreaRepository = SecureAreaRepository.Builder().add(secureArea).build(),
        ) {}
        if (documentStore.listDocuments().isEmpty()) {
            val now = Clock.System.now().truncateToWholeSeconds()
            val issuerAuthorityKey = Crypto.createEcPrivateKey(EcCurve.P384)
            val issuerAuthorityCert = MdocUtil.generateIacaCertificate(
                iacaKey = AsymmetricKey.anonymous(issuerAuthorityKey),
                subject = X500Name.fromName("C=XX,CN=Local development mDL IACA"),
                serial = ASN1Integer.fromRandom(numBits = 128),
                validFrom = now - 1.days,
                validUntil = now + 365.days,
                issuerAltNameUrl = "https://example.invalid/local-mdl",
                crlUrl = "https://example.invalid/local-mdl/crl",
            )
            val documentSignerPrivateKey = Crypto.createEcPrivateKey(EcCurve.P256)
            val documentSignerCert = MdocUtil.generateDsCertificate(
                iacaKey = AsymmetricKey.X509CertifiedExplicit(
                    X509CertChain(listOf(issuerAuthorityCert)), issuerAuthorityKey,
                ),
                dsKey = documentSignerPrivateKey.publicKey,
                subject = X500Name.fromName("C=XX,CN=Local development mDL DS"),
                serial = ASN1Integer.fromRandom(numBits = 128),
                validFrom = now - 1.days,
                validUntil = now + 365.days,
            )
            val document = documentStore.createDocument(
                displayName = "Local specimen mDL",
                typeDisplayName = DrivingLicense.getDocumentType().displayName,
            )
            DrivingLicense.getDocumentType().createMdocCredentialWithSampleData(
                document = document,
                secureArea = secureArea,
                createKeySettings = CreateKeySettings(),
                dsKey = AsymmetricKey.X509CertifiedExplicit(
                    X509CertChain(listOf(documentSignerCert, issuerAuthorityCert)), documentSignerPrivateKey,
                ),
                signedAt = now,
                validFrom = now - 1.days,
                validUntil = now + 365.days,
                domain = "mdoc",
            )
        }
        presentmentSource = SimplePresentmentSource(
            documentStore = documentStore,
            documentTypeRepository = documentTypeRepository,
            domainsMdocSignature = listOf("mdoc"),
        )
            initialized = true
        }
    }
}
