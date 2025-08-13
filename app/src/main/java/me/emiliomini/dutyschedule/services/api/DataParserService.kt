package me.emiliomini.dutyschedule.services.api

import android.util.Log
import me.emiliomini.dutyschedule.data.models.AssignedEmployee
import me.emiliomini.dutyschedule.data.models.DutyDefinition
import me.emiliomini.dutyschedule.data.models.Employee
import me.emiliomini.dutyschedule.data.models.mapping.Requirement
import org.json.JSONObject
import java.time.OffsetDateTime
import kotlin.collections.iterator

object DataParserService {
    private val TAG = "DataParserService";
    private val DATA_ROOT_POSITION = "data";

    fun parseLoadPlan(root: JSONObject): List<DutyDefinition> {
        val data = root.getJSONObject(this.DATA_ROOT_POSITION);
        val keys = data.keys();

        val duties = mutableListOf<DutyDefinition>();
        val assignments = mutableListOf<AssignedEmployee>();

        for (key in keys) {
            val obj = data.getJSONObject(key);
            val requirement =
                obj.getString(Requirement.Companion.POSITION);
            val begin = obj.getString(DutyDefinition.Companion.BEGIN_POSITION);
            val end = obj.getString(DutyDefinition.Companion.END_POSITION);
            val beginTime = OffsetDateTime.parse(begin);
            val endTime = OffsetDateTime.parse(end);

            if (requirement == Requirement.SEW.value) {
                duties.add(
                    DutyDefinition(
                        key, // Might need to revisit at a later point: Figure out what the actual guid of the duty is
                        beginTime,
                        endTime
                    )
                );
            }
            if (requirement == Requirement.SEW.value || requirement == Requirement.EL.value || requirement == Requirement.TF.value || requirement == Requirement.RS.value) {
                val employeeGuid = obj.getString(Employee.Companion.GUID_POSITION);
                var employeeName = obj.getJSONObject(Employee.Companion.ADDITIONAL_INFOS_POSITION)
                    .getString(Employee.Companion.ADDITIONAL_INFOS_NAME_POSITION);
                if (employeeName.isBlank()) {
                    // Using INFO Tag as fallback
                    employeeName = obj.getString(Employee.Companion.INFO_POSITION);
                }

                if (employeeName.isBlank()) {
                    // Skip empty fields; TODO: Figure out why they are there to begin with
                    continue;
                }

                val employee = Employee(
                    employeeGuid,
                    employeeName,
                    if (requirement == Requirement.SEW.value) Employee.Companion.SEW_NAME else null
                );

                assignments.add(
                    AssignedEmployee(
                        employee,
                        Requirement.Companion.parse(requirement),
                        beginTime,
                        endTime
                    )
                );
            }
        }

        val sortedDuties = duties.sortedBy { it.begin };
        val sortedAssignments = assignments.sortedBy { it.begin };

        var dutyIndex = 0;
        var assignmentIndex = 0;
        while (dutyIndex < sortedDuties.size && assignmentIndex < sortedAssignments.size) {
            val duty = sortedDuties[dutyIndex];
            val assignment = sortedAssignments[assignmentIndex];

            if (!assignment.begin.isBefore(duty.begin) && assignment.begin.isBefore(duty.end)) {
                when (assignment.requirement) {
                    Requirement.SEW -> {
                        duty.sew.add(assignment);
                    }
                    Requirement.EL -> {
                        duty.el.add(assignment);
                    }
                    Requirement.TF -> {
                        duty.tf.add(assignment);
                    }
                    Requirement.RS -> {
                        duty.rs.add(assignment);
                    }
                    else -> {
                        Log.w(TAG, "Encountered invalid object while trying to parse duties");
                    }
                }
                assignmentIndex++;
            } else if (assignment.begin.isBefore(duty.begin)) {
                Log.w(TAG, "Assignment out of bounds while trying to parse duties");
                assignmentIndex++;
            } else {
                dutyIndex++;
            }
        }

        Log.d(TAG, "Parsed ${sortedDuties.size} duties");

        return sortedDuties;
    }

    fun parseGetStaff(root: JSONObject): List<Employee> {
        val data = root.getJSONArray(this.DATA_ROOT_POSITION);
        val employees = mutableListOf<Employee>();

        for (i in 0 until data.length()) {
            val obj = data.getJSONObject(i);

            employees.add(
                Employee(
                    obj.getString(Employee.Companion.STAFF_GUID_POSITION),
                    obj.getString(Employee.Companion.STAFF_NAME_POSITION),
                    obj.getString(Employee.Companion.STAFF_IDENTIFIER_POSITION),
                )
            );
        }

        return employees;
    }
}