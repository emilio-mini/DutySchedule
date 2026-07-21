//
//  DutyScheduleWidgetBundle.swift
//  DutyScheduleWidget
//
//  This target does not exist in the Xcode project yet - see the setup instructions
//  for how to create it and add these files. Mirrors the Android widgets
//  (NextDutyWidget / DutyCalendarWidget) built with Glance.
//

import WidgetKit
import SwiftUI

@main
struct DutyScheduleWidgetBundle: WidgetBundle {
    var body: some Widget {
        NextDutyWidget()
        DutyCalendarWidget()
    }
}
