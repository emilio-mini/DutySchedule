package me.emiliomini.dutyschedule.models.prep

import me.emiliomini.dutyschedule.R

enum class Org(val value: String) {

    WELS_STADT("11d43db06551ef61d472076f222802ca02cd1383_2_1551803241_1914"),
    WELS_LAND("60d9b5e93c24feced17c513fb3f95c18cd828abe_2_1551803338_4809"),
    GKR("278c1d32c2a17cd8c75a673dd7f73f533f83986d_2_1551861412_1988"),
    VB("0ad2ea66501e65d352d093b7ddb77739542cd15b_2_1569822329_5999"),
    TRAUN("8441f3ffd6efd473491c68bd29af172b07ca9b1c_2_1551803765_2408"),
    WZK("19a76eeb005badf4c42f239ff6bf7e8fb81096bf_2_1551861762_4779"),
    ANDORF("abbebaa7c6c54013f0105e8e1e99d50204ee4409_2_1551861276_504"),
    STEYR_STADT("cf123afe6e9b3e4b4e812a6f7659421b5c22cb16_2_1551861656_1816"),
    STEYR_LAND("32e9baebefb8e74418e62a4bbf431bfd0c2197b2_2_1551861639_0161"),
    ST_FLORIAN("7f9db4fff7a991a9e578c7c5c0f31b6554b8cd24_2_1551803724_0561"),
    ST_GEORGEN("b0545b4f246c571429696d8812c04efcb29ab3f0_2_1569822237_8073"),
    ROHRBACH("68dba753770d0bfdaba3be21392b43838ef64756_2_1551861594_1169"),
    PERG("0eb2378daae106ce287ebb178ed54b2748a7221c_2_1551802664_799"),
    MATTIGHOFEN("a17a907dd59446046c326103f51d2565c0e8883a_2_1551861531_3134"),
    LINZ_STADT("d50965e27324171d7f3408637c36de26f24eb819_2_1551807782_6699"),
    KIRCHSCHLAG("7253212e00a92df56ee72203050bd5c0cc090ee8_2_1551861489_2583"),
    KIRCHDORF("28de527b1d170cd780d9ffa9577f02368b9758d6_2_1551861438_2957"),
    GMUNDEN("a915b89c84bdd571730aad62b765a763c6f855f0_2_1551861369_7926"),
    GMUNDEN_SUED("44028dd486b56dd19e3c6e5c4c53d796f818dad7_2_1551861387_2338"),
    FREISTADT("a8a19bfefce22848c171c7033a0370e699d14b95_2_1544538123_4081"),
    BRAUNAU("e74e7dd439fcd0e3a22ddb19ac786993d81e25a5_2_1551861301_1348"),

    INVALID("");

    fun getDocscedConfig(): String? = when (this) {
        WELS_STADT      -> "welsstadt"
        WELS_LAND       -> "welsland"
        BRAUNAU         -> "braunau"
        FREISTADT       -> "freistadt"
        GMUNDEN         -> "gmundennord"     // falls nötig auf "gmsued1/2" ändern
        GMUNDEN_SUED    -> "gmsued1"         // ggf. "gmsued2"
        KIRCHDORF       -> "kirchdorf"
        LINZ_STADT      -> "linzn"
        PERG            -> "perg"
        ROHRBACH        -> "rohrbach"
        STEYR_LAND      -> "steyr"
        STEYR_STADT     -> "steyrstadt"
        TRAUN           -> "lltraun"
        ST_FLORIAN      -> "llflorian"
        VB              -> "voecklabruck"
        GKR             -> "eferdinggrieskirchen" // Eferding–Grieskirchen (HTML-Button)
        WZK   -> "grieskirchen"        // eigener Button vorhanden
        ANDORF          -> "schaerding"          // Bezirk Schärding
        // keine direkten Buttons im HTML:
        KIRCHSCHLAG, MATTIGHOFEN, ST_GEORGEN -> null
        INVALID -> null
    }


    fun getResourceString(): Int {
        return when (this) {
            WELS_STADT -> R.string.data_org_we
            WELS_LAND -> R.string.data_org_wl
            BRAUNAU -> R.string.data_org_braunau
            FREISTADT -> R.string.data_org_freistadt
            GMUNDEN -> R.string.data_org_gmunden
            GMUNDEN_SUED -> R.string.data_org_gmunden_sued
            KIRCHDORF -> R.string.data_org_kirchdorf
            LINZ_STADT -> R.string.data_org_linz_stadt
            PERG -> R.string.data_org_perg
            ROHRBACH -> R.string.data_org_rohrbach
            STEYR_LAND -> R.string.data_org_steyr_land
            STEYR_STADT -> R.string.data_org_steyr_stadt
            TRAUN -> R.string.data_org_traun
            ST_FLORIAN -> R.string.data_org_st_florian
            VB -> R.string.data_org_voecklabruck
            GKR -> R.string.data_org_gkr
            WZK -> R.string.data_org_waizenkirchen
            ANDORF -> R.string.data_org_andorf

            else -> R.string.data_requirement_none
        }
    }

    companion object {

        fun parse(value: String): Org {
            return entries.find { it.value == value } ?: INVALID
        }
    }
}