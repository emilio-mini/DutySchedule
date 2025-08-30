package me.emiliomini.dutyschedule.services.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import me.emiliomini.dutyschedule.datastore.alarm.AlarmItemsProto
import me.emiliomini.dutyschedule.datastore.prep.IncodeProto
import me.emiliomini.dutyschedule.datastore.prep.StatisticsProto
import me.emiliomini.dutyschedule.datastore.prep.duty.UpcomingDutyItemsProto
import me.emiliomini.dutyschedule.datastore.prep.employee.EmployeeProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgItemsProto

private val Context.store_preferences: DataStore<Preferences> by preferencesDataStore("user_data")
private val Context.store_alarm_items: DataStore<AlarmItemsProto> by dataStore(
    fileName = "alarm_proto.pb",
    serializer = ProtoSerializer<AlarmItemsProto>(AlarmItemsProto.parser(), AlarmItemsProto.getDefaultInstance())
)
private val Context.store_org_items: DataStore<OrgItemsProto> by dataStore(
    fileName = "orgs_proto.pb",
    serializer = ProtoSerializer<OrgItemsProto>(OrgItemsProto.parser(), OrgItemsProto.getDefaultInstance())
)
private val Context.store_prep_incode: DataStore<IncodeProto> by dataStore(
    fileName = "prep_incode_proto.pb",
    serializer = ProtoSerializer<IncodeProto>(IncodeProto.parser(), IncodeProto.getDefaultInstance())
)
private val Context.store_prep_self: DataStore<EmployeeProto> by dataStore(
    fileName = "prep_self_proto.pb",
    serializer = ProtoSerializer<EmployeeProto>(EmployeeProto.parser(), EmployeeProto.getDefaultInstance())
)
private val Context.store_prep_statistic: DataStore<StatisticsProto> by dataStore(
    fileName = "prep_statistics_proto.pb",
    serializer = ProtoSerializer<StatisticsProto>(StatisticsProto.parser(), StatisticsProto.getDefaultInstance())
)
private val Context.store_upcoming_duties: DataStore<UpcomingDutyItemsProto> by dataStore(
    fileName = "prep_upcoming_duties.pb",
    serializer = ProtoSerializer<UpcomingDutyItemsProto>(UpcomingDutyItemsProto.parser(), UpcomingDutyItemsProto.getDefaultInstance())
)

object DataStores {
    lateinit var PREFERENCES: DataStore<Preferences>
    lateinit var ALARM_ITEMS: DataStore<AlarmItemsProto>
    lateinit var ORG_ITEMS: DataStore<OrgItemsProto>
    lateinit var INCODE: DataStore<IncodeProto>
    lateinit var SELF: DataStore<EmployeeProto>
    lateinit var STATISTICS: DataStore<StatisticsProto>
    lateinit var UPCOMING_DUTIES: DataStore<UpcomingDutyItemsProto>

    fun initialize(context: Context) {
        if (!this::PREFERENCES.isInitialized) this.PREFERENCES = context.store_preferences
        if (!this::ALARM_ITEMS.isInitialized) this.ALARM_ITEMS = context.store_alarm_items
        if (!this::ORG_ITEMS.isInitialized) this.ORG_ITEMS = context.store_org_items
        if (!this::INCODE.isInitialized) this.INCODE = context.store_prep_incode
        if (!this::SELF.isInitialized) this.SELF = context.store_prep_self
        if (!this::STATISTICS.isInitialized) this.STATISTICS = context.store_prep_statistic
        if (!this::UPCOMING_DUTIES.isInitialized) this.UPCOMING_DUTIES = context.store_upcoming_duties
    }
}