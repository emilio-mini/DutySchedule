//
//  DutyCalendarWidget.swift
//  DutyScheduleWidget
//

import WidgetKit
import SwiftUI
import sharedKit

struct DutyCalendarEntry: TimelineEntry {
    let date: Date
    let month: CalendarMonth?
}

struct DutyCalendarProvider: TimelineProvider {
    func placeholder(in context: Context) -> DutyCalendarEntry {
        DutyCalendarEntry(date: Date(), month: nil)
    }

    func getSnapshot(in context: Context, completion: @escaping (DutyCalendarEntry) -> Void) {
        completion(DutyCalendarEntry(date: Date(), month: nil))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<DutyCalendarEntry>) -> Void) {
        Task {
            let month: CalendarMonth? = (try? await IosWidgetDataProvider.shared.getCalendarMonth()) ?? nil
            let entry = DutyCalendarEntry(date: Date(), month: month)
            let nextRefresh = Calendar.current.date(byAdding: .hour, value: 6, to: Date()) ?? Date().addingTimeInterval(6 * 3600)
            completion(Timeline(entries: [entry], policy: .after(nextRefresh)))
        }
    }
}

struct DutyCalendarWidgetView: View {
    var entry: DutyCalendarProvider.Entry

    var body: some View {
        if let month = entry.month {
            VStack(spacing: 4) {
                Text(month.monthLabel)
                    .font(.caption).bold()
                HStack(spacing: 0) {
                    ForEach(month.weekdayLabels, id: \.self) { label in
                        Text(label.uppercased())
                            .font(.system(size: 8))
                            .foregroundStyle(.secondary)
                            .frame(maxWidth: .infinity)
                    }
                }
                ForEach(Array(month.weeks.enumerated()), id: \.offset) { _, week in
                    HStack(spacing: 0) {
                        ForEach(week, id: \.timestamp.seconds) { day in
                            DayCell(day: day)
                                .frame(maxWidth: .infinity)
                        }
                    }
                }
            }
            .padding(6)
            .containerBackground(.background, for: .widget)
        } else {
            Text("No calendar data")
                .font(.caption)
                .foregroundStyle(.secondary)
                .containerBackground(.background, for: .widget)
        }
    }
}

private struct DayCell: View {
    let day: CalendarDay

    var body: some View {
        VStack(spacing: 1) {
            Text("\(day.dayOfMonth)")
                .font(.system(size: 11))
                .fontWeight(day.isToday ? .bold : .regular)
                .foregroundStyle(
                    !day.isCurrentMonth ? Color.secondary.opacity(0.4) :
                    (day.isSelfDuty ? Color.accentColor : Color.primary)
                )
                .frame(width: 18, height: 18)
                .background(day.isToday ? Color.accentColor.opacity(0.2) : Color.clear)
                .clipShape(Circle())

            HStack(spacing: 1) {
                if day.hasDayShift {
                    Image(systemName: "sun.max.fill").font(.system(size: 6)).foregroundStyle(.orange)
                }
                if day.hasNightShift {
                    Image(systemName: "moon.fill").font(.system(size: 6)).foregroundStyle(.indigo)
                }
            }
            .frame(height: 8)
        }
    }
}

struct DutyCalendarWidget: Widget {
    let kind: String = "DutyCalendarWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: DutyCalendarProvider()) { entry in
            DutyCalendarWidgetView(entry: entry)
        }
        .configurationDisplayName("Duty Calendar")
        .description("Shows a monthly overview of your duties.")
        .supportedFamilies([.systemMedium, .systemLarge])
    }
}
