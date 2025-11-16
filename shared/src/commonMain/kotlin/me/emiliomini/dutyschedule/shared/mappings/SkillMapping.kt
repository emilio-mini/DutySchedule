package me.emiliomini.dutyschedule.shared.mappings

import me.emiliomini.dutyschedule.shared.datastores.Skill
import me.emiliomini.dutyschedule.shared.json.JsonMapping

object SkillMapping {
    val GUID = JsonMapping.STRING("skillDataGuid")
}

/**
 * Values in this enum are automatically generated
 */
enum class MappedSkills(val guid: String, val abbreviation: String, val originalTitle: String) {
    NKA_V("97c906abe22bedbc345b831bbbd8780202c0ac1d_2_1574062972_0977", "NKA/V", "Allgemeine Notfallkompetenz"),
    ARZT("ec5fe3764ea8629288301b9e3b8ff77d55d76e84_2_1544534382_5269", "ARZT", "Arzt"),
    AZUBI("e87b6976147e38a96a1e682ab1ef82600108a89c_2_1566370936_4014", "AZUBI", "Auszubildender"),
    BDMA("4e1a8b845520d4f5812bf8a2a479ced75ee61efd_2_1569541115_3942", "BDMA", "Besuchsdienst Mitarbeiter"),
    BETR("cf6c7c02a9eda99f5b3e5516cf2696c83747a42e_2_1551878785_8224", "BETR", "Betreuer"),
    BTMA("7eede8031d613240b9b8ccf6ee30413e63315bed_2_1705908060_2949", "BTMA", "Betreues Reisen Mitarbeiter"),
    BRK("635947a9d4f27ebf5ad2114e10cd9cbcacc6999e_2_1574340024_2725", "BRK", "Bezirksrettungskommandant"),
    BRK_S("e811014453034c57f9e1a9232c79a4abb264e753_2_1586767580_2636", "BRK S", "Bezirksrettungskommandant Stv"),
    BSDB("f3c43668ecc6bf77b4eae1d631d35b2ea25ef601_2_1551875666_8689", "BSDB", "Blutspendebeauftragter"),
    BSDK("7d190c564cb374023e1dc8af1dd097e204138156_2_1600085565_2162", "BSDK ", "BlutspendekoordinatorIn des Bezirkes"),
    BSDC("0dfcc53e6343b9891f9ab8498f0809eb3189138c_2_1600085281_8842", "BSDC", "BSH Checkpoint"),
    BSDE("9ba7eda70889f05f19f0e4dedaba606ed0ce3510_2_1600085397_7569", "BSDE", "BSH Empfang"),
    BSDL("4a09f2aa541c606fb6ebae2b8adfccba66befb15_2_1600085357_3899", "BSDL", "BSH Labung & Betreuung"),
    BSDS("4221ef120aeefa366f8603181ff9e0118a448de8_2_1600085460_1093", "BSDS", "BSH Stammzellen"),
    BSDT("90246f239866ed340ab58cd32b6d0ebb1317fba5_2_1600085426_2736", "BSDT", "BSH Thrombo"),
    BSDR("6f76d5093f1606718f47871b215735b29eefa698_2_1709716122_8791", "BSDR", "BSH TRS Empfang"),
    CCAB("9be631589d347b2f456eec037113a34c414f69fb_2_1568107528_5542", "CCAB", "Callcenter Agent Blut"),
    CT("fb340ac75eb7b3cc886f0137d7afd140cefca542_2_1594272832_2937", "CT", "Calltaker"),
    PACT("4069809cf6bf239d30fb2548e02586d6d22dd556_2_1601893260_5531", "PACT", "CoVid Calltaker"),
    PRAB("2f0778b1cf2cfa4a02bf421843fcd2a3ea1d6ca3_2_1598446705_7788", "PRAB", "CoVid Probenabnehmer"),
    PRLO("fc215397acd2e1859774d760786064526b2d29ab_2_1598447068_2412", "PRLO", "CoVid Probenlogistiker"),
    DF("8f5484a63fb85e03584ab5109e50f33bc541baec_2_1551877357_545", "DF", "Dienstführender"),
    DFSTV("cd49468e878555e427bd316974b0d174798e0153_2_1683524492_5549", "DFStv", "Dienstführender Stv."),
    DH("b636fd6ad21225f75342d4a0911a41544006cd70_2_1551875778_4642", "DH", "Diensthabender"),
    EARMA("200945077fe556e35fe8b6d5b7e37accce83c088_2_1551876042_0522", "EARMA", "EAR Mitarbeiter"),
    ELVO("7022e2bcad21df93a1081dfdd5de300dfd866ae4_2_1569540624_8448", "ELvO", "Einsatzleiter vor Ort"),
    PALES("ed867acff05eed959c37a9c5d5a664f5219c8ce6_2_1551876630_9628", "PALES", "Emergency-Call-Nurse"),
    FBCB("92330263bd20d1ecb7804e0a57810cfa8301511a_2_1695817154_2808", "FBCB", "Fachberater CBRN"),
    FSBA("181ec68600865fd21cea5ad590e04f9ebc959b17_2_1687778781_7246", "FSBA", "Fachsozialbetreuer Altenarbeit"),
    FELDK("df44e43948df9fe629b0ae9e18fcb5e40154703c_2_1707394740_3135", "FeldK", "Feldküchenmitarbeiter"),
    FR("7d8a4ffc08ac248b51316428564a4d01889de7c6_2_1568106762_5117", "FR", "First Responder"),
    FH("eacb71a6d01aded3c9bd99b79b773477bebed104_2_1659610309_9063", "FH", "Flughelfer"),
    FK("18c945da0432ec166c9c8c8718c91efe73224503_2_1544534838_137", "FK", "Führungskraft"),
    KWHE("49ab2dc5144ee8d44f0f2a16d9f825316c077970_2_1717395648_4842", "KWHE", "Funk Helfer"),
    FUNK("725f252258c659f51656f40e01f66a1034c977bf_2_1717395702_0701", "FUNK", "Funker"),
    FELF("997508199ab587a32cb44357e6a118890a1c1afe_2_1659611710_508", "FELF", "FüU ELF"),
    GENCE("e950e19a30578afedc6d6cb89571df923fed9af6_2_1551877506_204", "GENCE", "GEN Cafe"),
    HE("7d026a182c6f5ccb224d5d2a464a2c244547f99e_2_1659614959_1655", "HE", "Helfer SH"),
    HSPTZ("194e049f25fe36ee779a6f0dc7d8f641eea7619b_2_1568107203_5529", "HSPTZ", "Hospitz Mitarbeiter"),
    HFF("14ab6fc45713b5359a74f4220bd6a17a7b4463a2_2_1659614996_2072", "HFF", "Hundeführer Fläche"),
    HPSH("cb1b258331f057a2890938d4102963bf3568e8a6_2_1659615033_1343", "HPSH", "Hundeführer PSH"),
    JGDLT("83b620b6599fc5fab63ea929d80fcb852c5cefd8_2_1551876224_8306", "JGDLT", "JRK Mitarbeiter"),
    KI("83044f1325e41d7e38789b74b01fee2371a2f9e4_2_1551876495_4368", "KI", "Krisenintervention"),
    LBAEH("38a61ea09f8777d4ac2a3a007804c0e36f4ab122_2_1551859572_372", "LBAEH", "Lehrbeauftragter"),
    LBALS("c30671bcde60ec8b830ed13b01c8cc6fdab22b9d_2_1574063179_2314", "LBALS", "Lehrsanitäter"),
    ELDR("730bb2050f302bc5e61daae71c537ec5a74ccce8_2_1659610400_0137", "ELDR", "Leiter Drohnen"),
    LSAR("a81b75f28e480b78d189f53746e21a558e60a787_2_1659614873_282", "LSAR", "Leiter Suchhunde"),
    TWAL("a2bceb32e4fde2d053e5a9029c6ca8c654eefb85_2_1658324043_5989", "TWAL", "Leiter TWA"),
    DISPO("4e9247301b1ba592750f79a7e5df904d914ae3b2_2_1544534772_3254", "DISPO", "Leitstellenmitarbeiter"),
    LEN("1e2afd7483e95976c86cb094c9024ec4fbea04db_2_1659610715_5574", "LEN", "Lenker"),
    LELF("dd9806f6bbe6791aac8f8bf0b1845f99053c65cf_2_1659611680_0506", "LELF", "Lenker ELF"),
    LC("4dbb91b8841dbfc719a105ace46c5b2037f797b8_2_1551876548_2844", "LC", "Lesecoach"),
    BNDF("bd700743ebfbb218da1887b07913b1afae95ca98_2_1686641232_8954", "BNDF", "Logistik Fahrer"),
    ABCD("12c6216b9560e9609dede58c24f1e33418698ce7_2_1659610214_4461", "ABCD", "MA BC-Deko"),
    MAKW("4f5136e1fe4633acb14456a8d7796b9138590122_2_1658319492_7316", "MAKW", "MA Kurzwelle"),
    STRA("d56062e627ec3eb59e482965769803d936e34554_2_1659610252_3392", "STRA", "MA Strahlenschutz"),
    MASAR("1c970873565b8739126ca3c296d0ed75c9439672_2_1659614909_5965", "MASAR", "MA Suchhunde"),
    TWA("328142e4ffdbb90a8dd21b5bfae7a8b9d25dc380_2_1658323962_4949", "TWA.", "MA TWA"),
    MA("e8ee4df2d7517e2092df2d470d2542e2737a71d8_2_1551876437_1729", "MA", "Mitarbeiter"),
    NFS("2e808a7931f82fd486ec2ea8de362d7545ed3a82_2_1544534699_0338", "NFS", "Notfallsanitäter"),
    OVD("74992472636bc8969088e0bf0499ef2881a8d330_2_1569540591_535", "OvD", "Offizier vom Dienst"),
    PIC("8a2a192680745b8460c6b9a8900a5c427ebaf799_2_1659610360_881", "PIC", "Pilot in Command"),
    PR("de61278b45e6868bf4c0b87aa967e524d8f42ac5_2_1589528568_7296", "PR", "Praktikant"),
    PA("cfbd5afb5dd6f8aab2c3e205d5a27b7995db8bed_2_1551859812_3923", "PA", "Praxisanleiter"),
    RD_KO("b81e13ef93cd51852c704c2b715680aaa3783196_2_1728876330_2962", "RD KO", "RD Koordinator"),
    RS("916dfa675111ff0378e15e83c4daa2ea59d043ed_2_1544534410_948", "RS", "Rettungssanitäter"),
    ROKO("5e25f49664f3f5089058bacd508b5132f2d786ec_2_1551878098_52", "ROKO", "ROKO Mitarbeiter"),
    RKMMA("47974aad8a470711e1f3639a4a84ec6bb20c5002_2_1551878003_281", "RKMMA", "Rotkreuz-Markt Mitarbeiter"),
    RUD("a4f1443340b7bc2250bb34cfaeea4fe92ad0a930_2_1551878110_9736", "RUD", "RUD Mitarbeiter"),
    RUHI("e665aa5b70a57aa227149aea9f3f37196c640035_2_1614253226_1267", "RUHI", "Rufhilfe Mitarbeiter"),
    SANH("5d94ed22ed92e0c83adab8a19ae3c72a41b0712b_2_1544534852_643", "SANHÄ", "Sanitätshelfer-HÄND"),
    SANRD("ea841b4a764ac65e8903d47cd2d306434f0bff79_2_1569540987_2835", "SANRD", "Sanitätshelfer-RD"),
    SCHUL("c7ddb55e446cd2999e66928df19d9e5291b75ac8_2_1551877011_0422", "SCHUL", "Schulung"),
    SEF("90bd3d0d6f9cea873f9dd8a07bdbd36916105b7b_2_1572342986_5013", "SEF", "SEF Praxistrainer"),
    SUHUM("e0534a63b2754c8ef0d2c76c33a0806123a28994_2_1579178592_2904", "SUHUM", "Suchhunde MA"),
    TNM("f3f4be3bae8e974536d18f0e0e4460ee3a746036_2_1551878855_0019", "ÜTNM", "Übungsteilnehmer"),
    INVALID("", "","");

    fun asSkill(): Skill {
        return Skill(guid = guid)
    }

    companion object Companion {
        fun parse(value: String): MappedSkills {
            return entries.find { it.guid == value } ?: INVALID
        }
    }
}