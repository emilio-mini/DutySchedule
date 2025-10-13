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
        MainComposeHost()
    }
}

#Preview {
    ContentView()
}

struct MainComposeHost: UIViewControllerRepresentable {

    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
