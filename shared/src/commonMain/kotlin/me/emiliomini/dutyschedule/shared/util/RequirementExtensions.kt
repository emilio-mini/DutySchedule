package me.emiliomini.dutyschedule.shared.util

import me.emiliomini.dutyschedule.shared.datastores.Requirement
import me.emiliomini.dutyschedule.shared.mappings.RequirementMapping

fun Requirement.getPriority(): Int {
    return RequirementMapping.parse(guid).priority
}