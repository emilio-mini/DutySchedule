package me.emiliomini.dutyschedule.demo

import me.emiliomini.dutyschedule.services.storage.DataStores

object DemoService {
    var DEMO_MODE_ACTIVE = false

    suspend fun initialize() {
        DataStores.STATISTICS.updateData {
            DemoData.statistics
        }

        DataStores.ORG_ITEMS.updateData {
            DemoData.orgItems
        }

        DataStores.UPCOMING_DUTIES.updateData {
            DemoData.upcomingItems
        }

        DataStores.EMPLOYEES.updateData {
            DemoData.employeeItems
        }
    }
}