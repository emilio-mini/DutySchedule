//
//  NextDutyWidget.swift
//  DutyScheduleWidget
//

import WidgetKit
import SwiftUI
import sharedKit

struct NextDutyEntry: TimelineEntry {
    let date: Date
    let duty: MinimalDutyDefinition?
    let selfName: String?
}

struct NextDutyProvider: TimelineProvider {
    func placeholder(in context: Context) -> NextDutyEntry {
        NextDutyEntry(date: Date(), duty: nil, selfName: nil)
    }

    func getSnapshot(in context: Context, completion: @escaping (NextDutyEntry) -> Void) {
        completion(NextDutyEntry(date: Date(), duty: nil, selfName: nil))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<NextDutyEntry>) -> Void) {
        Task {
            let duty: MinimalDutyDefinition? = (try? await IosWidgetDataProvider.shared.getNextDuty()) ?? nil
            let selfName: String? = (try? await IosWidgetDataProvider.shared.getSelfName()) ?? nil
            let entry = NextDutyEntry(date: Date(), duty: duty, selfName: selfName)
            // Data changes at most a few times a day; refresh periodically and whenever
            // refreshWidgets() is called from the app (IosPlatformBridge -> WidgetCenter).
            let nextRefresh = Calendar.current.date(byAdding: .minute, value: 30, to: Date()) ?? Date().addingTimeInterval(1800)
            completion(Timeline(entries: [entry], policy: .after(nextRefresh)))
        }
    }
}

private func timeString(_ seconds: Int64) -> String {
    let date = Date(timeIntervalSince1970: TimeInterval(seconds))
    let formatter = DateFormatter()
    formatter.dateFormat = "HH:mm"
    return formatter.string(from: date)
}

private func dateBadge(_ seconds: Int64) -> (day: String, month: String) {
    let date = Date(timeIntervalSince1970: TimeInterval(seconds))
    let day = DateFormatter()
    day.dateFormat = "d"
    let month = DateFormatter()
    month.dateFormat = "MMM"
    return (day.string(from: date), month.string(from: date))
}

struct NextDutyWidgetView: View {
    var entry: NextDutyProvider.Entry

    var body: some View {
        if let duty = entry.duty {
            HStack(alignment: .top, spacing: 12) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(timeString(duty.begin.seconds))
                        .font(.headline)
                    Text(timeString(duty.end.seconds))
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }

                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text(duty.typeString.isEmpty ? "Duty" : duty.typeString)
                            .font(.subheadline).bold()
                        Spacer()
                        let badge = dateBadge(duty.begin.seconds)
                        VStack(spacing: 0) {
                            Text(badge.day).font(.caption2).bold()
                            Text(badge.month).font(.caption2)
                        }
                    }
                    if let vehicle = duty.vehicle {
                        Text(vehicle)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                    ForEach(duty.staff, id: \.self) { name in
                        Text(name)
                            .font(.caption2)
                            .fontWeight(name == entry.selfName ? .bold : .regular)
                            .foregroundStyle(name == entry.selfName ? Color.accentColor : .primary)
                    }
                }
            }
            .padding(4)
            .containerBackground(.background, for: .widget)
        } else {
            VStack {
                Text("No upcoming duties")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
            }
            .containerBackground(.background, for: .widget)
        }
    }
}

struct NextDutyWidget: Widget {
    let kind: String = "NextDutyWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: NextDutyProvider()) { entry in
            NextDutyWidgetView(entry: entry)
        }
        .configurationDisplayName("Next Duty")
        .description("Shows your next upcoming duty.")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}
