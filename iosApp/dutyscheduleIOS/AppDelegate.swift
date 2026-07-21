//
//  AppDelegate.swift
//  dutyscheduleIOS
//

import UIKit
import WidgetKit
import sharedKit

class AppDelegate: NSObject, UIApplicationDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // Must be registered before this method returns, per BGTaskScheduler's docs.
        IosPlatformBridge.shared.registerBackgroundTasks()

        // WidgetKit is Swift-only and has no Kotlin/Native interop surface, so shared code
        // reaches it through this closure instead of calling it directly.
        IosPlatformBridge.shared.widgetRefreshHook = {
            WidgetCenter.shared.reloadAllTimelines()
        }

        return true
    }
}
