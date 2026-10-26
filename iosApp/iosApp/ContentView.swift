import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        // QR/BLE presentment is provided by the shared KMP UI. NFC holder presentment
        // intentionally has no iOS wiring because Multipaz supports it only on Android.
        ComposeView()
            .ignoresSafeArea()
    }
}
