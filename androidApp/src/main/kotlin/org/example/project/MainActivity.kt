package org.example.project

import android.content.ComponentName
import android.nfc.NfcAdapter
import android.nfc.cardemulation.CardEmulation
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

import org.multipaz.context.initializeApplication

class MainActivity : androidx.fragment.app.FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeApplication(applicationContext)
        enableEdgeToEdge()

        setContent {
            App()
        }
    }

    override fun onResume() {
        super.onResume()
        NfcAdapter.getDefaultAdapter(this)?.let { adapter ->
            CardEmulation.getInstance(adapter).setPreferredService(
                this,
                ComponentName(this, WalletNfcApduRouter::class.java),
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
