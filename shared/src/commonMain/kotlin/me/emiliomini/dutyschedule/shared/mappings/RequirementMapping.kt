package me.emiliomini.dutyschedule.shared.mappings

enum class RequirementMapping(val value: String, val priority: Int) {
    // Util
    TIMESLOT("fd4abb09f1cbf2687319798b396cc38255ffb817_2_1544535064_9602", 20),
    INVALID("", 20),
    TRAINING("451da18e7b16209a6a04071a274777cb3d362d43_2_1551892405_1111", 20),

    // Vehicle
    KFZ("8401f287277707d77fc1a6bf17df4ca0e470b115_2_1589909211_2348", 100),
    KFZ_2("b689ac89571d82c40ae113e7ebd9fe69ceecf9ea_2_1589909202_6474", 100),
    VEHICLE("bfce1996338b66a25fd523acf544acc5d295bb28_2_1551892388_1698", 100),
    SEW("d590f25129127adfdb7e994be630de152f5f1682_2_1544535093_4117", 100),
    RTW("390b263970bc93d4612d9a9544d50b1b6bc1d9a7_2_1551891770_4987", 100),
    ITF("297d898e34f3ce811302fbfd98c238d6adfc5617_2_1668611309_2843", 100),
    HAEND("efafdf85ad4b64da658b23b359a3a456fd340b35_2_1551887183_8843", 100),

    // Driver
    EL("43e3811f89fea7883aa664c53b10f287fdf63020_2_1544535120_1506", 80),
    HAEND_EL("1afcee15599a9d17e679b98bf46ad775b6408e8e_2_1570008349_4147", 80),
    ITF_LKW("72e19bdf669c42d2b8fbfe5be410d082690d2f0a_2_1668611327_4692", 80),

    // Passenger
    HAEND_DR("HAEND_DR", 60),
    TF("6509a03415cb338da9ffa9b2b849cd617bd756ca_2_1544535149_1171", 60),
    ITF_NFS("946e81e89c32695625a890358c9ccb79fec693b6_2_1668611344_1827", 60),
    RTW_NFS("30b51644b352bb3df6b7c2ce86db5c0b2762d710_2_1551891794_834", 40),

    // Teammember
    RTW_RS("5e5dd6c0ea76308df39e5986aa814b6d3856f24c_2_1707217926_8039", 60),
    RS("e022f1d19a68909adac66c55ce7adafb520a75ae_2_1544535615_1745", 40);

    companion object {
        val VEHICLES = listOf(KFZ, KFZ_2, VEHICLE, SEW, RTW, ITF, HAEND).map { it.value }
        val DRIVERS = listOf(EL, HAEND_EL, ITF_LKW).map { it.value }
        val PASSENGERS = listOf(HAEND_DR, TF, ITF_NFS, RTW_NFS).map { it.value }

        val NFS_SLOTS = listOf(ITF_NFS, ITF_LKW, RTW_NFS).map { it.value }

        fun parse(value: String): RequirementMapping {
            return entries.find { it.value == value } ?: INVALID
        }
    }
}