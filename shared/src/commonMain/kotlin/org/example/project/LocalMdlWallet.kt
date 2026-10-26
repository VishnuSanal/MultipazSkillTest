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
import org.multipaz.documenttype.knowntypes.addKnownTypes
import org.multipaz.mdoc.util.MdocUtil
import org.multipaz.presentment.PresentmentSource
import org.multipaz.presentment.SimplePresentmentSource
import org.multipaz.securearea.CreateKeySettings
import org.multipaz.securearea.SecureAreaRepository
import org.multipaz.util.Platform
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

/** A deliberately local-only, non-production mDL holder. */
object LocalMdlWallet {
    private const val MDOC_DOMAIN = "local-mdl"
    private val initializationLock = Mutex()

    lateinit var documentStore: DocumentStore
        private set
    lateinit var documentTypeRepository: DocumentTypeRepository
        private set
    lateinit var presentmentSource: PresentmentSource
        private set

    private var initialized = false

    suspend fun initialize() {
        initializationLock.withLock {
            if (initialized) return
            documentTypeRepository = DocumentTypeRepository().apply { addKnownTypes() }
            val secureArea = Platform.getSecureArea(Platform.nonBackedUpStorage)
            val secureAreas = SecureAreaRepository.Builder().add(secureArea).build()
            documentStore = buildDocumentStore(Platform.storage, secureAreas) {}
            presentmentSource = SimplePresentmentSource(
                documentStore = documentStore,
                documentTypeRepository = documentTypeRepository,
                domainsMdocSignature = listOf(MDOC_DOMAIN),
            )
            if (documentStore.listDocuments().isEmpty()) seedLocalDrivingLicense(secureArea)
            initialized = true
        }
    }

    private suspend fun seedLocalDrivingLicense(secureArea: org.multipaz.securearea.SecureArea) {
        val now = Clock.System.now()
        val iacaPrivateKey = Crypto.createEcPrivateKey(EcCurve.P521)
        val iacaCertificate = MdocUtil.generateIacaCertificate(
            iacaKey = AsymmetricKey.anonymous(iacaPrivateKey),
            subject = X500Name.fromName("C=US,CN=Local mDL Demo IACA"),
            serial = ASN1Integer.fromRandom(numBits = 128),
            validFrom = now - 1.days,
            validUntil = now + 365.days,
            issuerAltNameUrl = "https://example.invalid/local-mdl",
            crlUrl = "https://example.invalid/local-mdl/crl",
        )
        val iacaKey = AsymmetricKey.X509CertifiedExplicit(
            X509CertChain(listOf(iacaCertificate)), iacaPrivateKey,
        )
        val documentSignerPrivateKey = Crypto.createEcPrivateKey(EcCurve.P384)
        val documentSignerCertificate = MdocUtil.generateDsCertificate(
            iacaKey = iacaKey,
            dsKey = documentSignerPrivateKey.publicKey,
            subject = X500Name.fromName("C=US,CN=Local mDL Demo Document Signer"),
            serial = ASN1Integer.fromRandom(numBits = 128),
            validFrom = now - 1.days,
            validUntil = now + 365.days,
        )
        val documentSigner = AsymmetricKey.X509CertifiedExplicit(
            X509CertChain(listOf(documentSignerCertificate, iacaCertificate)), documentSignerPrivateKey,
        )
        val document = documentStore.createDocument(
            displayName = "Local sample driving license", typeDisplayName = "mDL (demo)",
        )
        DrivingLicense.getDocumentType().createMdocCredentialWithSampleData(
            document = document,
            secureArea = secureArea,
            createKeySettings = CreateKeySettings(),
            dsKey = documentSigner,
            signedAt = now,
            validFrom = now - 1.days,
            validUntil = now + 30.days,
            domain = MDOC_DOMAIN,
        )
    }
}
