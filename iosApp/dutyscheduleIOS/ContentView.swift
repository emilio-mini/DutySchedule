//
//  ContentView.swift
//  dutyscheduleIOS
//
//  Created by Emilio Miniberger on 12.10.25.
//

import SwiftUI
import sharedKit

struct ContentView: View {
    var body: some View {
        MainComposeHost(
            onLogout: {
            },
            onRestart: {
            }
        )
    }
}

#Preview {
    ContentView()
}

struct MainComposeHost: UIViewControllerRepresentable {

    var onLogout: () -> Void
    var onRestart: () -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.MainViewController(onLogout: onLogout, onRestart: onRestart)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
