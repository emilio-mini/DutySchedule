package me.emiliomini.dutyschedule.shared.mappings

import me.emiliomini.dutyschedule.shared.datastores.Skill
import me.emiliomini.dutyschedule.shared.json.JsonMapping

object SkillMapping {
    val GUID = JsonMapping.STRING("skillDataGuid")
}

enum class MappedSkills(val value: String) {
    RS("916dfa675111ff0378e15e83c4daa2ea59d043ed_2_1544534410_948"),
    AZUBI("e87b6976147e38a96a1e682ab1ef82600108a89c_2_1566370936_4014"),
    PA("b81e13ef93cd51852c704c2b715680aaa3783196_2_1728876330_2962"),
    SEF("f3f4be3bae8e974536d18f0e0e4460ee3a746036_2_1551878855_0019"),
    NFS("2e808a7931f82fd486ec2ea8de362d7545ed3a82_2_1544534699_0338"),
    FK("c30671bcde60ec8b830ed13b01c8cc6fdab22b9d_2_1574063179_2314"),
    NOTKOMPETENZ("97c906abe22bedbc345b831bbbd8780202c0ac1d_2_1574062972_0977"),
    HAEND("5d94ed22ed92e0c83adab8a19ae3c72a41b0712b_2_1544534852_643"),
    ZUGSKOMMANDANT("f57c67a6b631fcc3c89d1e9ff7c98952cee9bed0_2_1551859929_7417"),
    GRUPPENKOMMANDANT("1f1c71b6deadcf9b8eef112e874cf337859dc618_2_1551859921_966"),
    OFFIZIER("6c13602611a9373baa3ee2948be3780939658a21_2_1551859940_445"),
    INVALID("");

    fun asSkill(): Skill {
        return Skill(guid = value)
    }

    companion object Companion {
        fun parse(value: String): MappedSkills {
            return entries.find { it.value == value } ?: INVALID
        }
    }
}