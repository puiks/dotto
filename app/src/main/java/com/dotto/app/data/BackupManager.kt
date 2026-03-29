package com.dotto.app.data

import com.dotto.app.data.local.dao.CheckInDao
import com.dotto.app.data.local.dao.HabitDao
import com.dotto.app.data.local.entity.CheckInEntity
import com.dotto.app.data.local.entity.HabitEntity
import org.json.JSONArray
import org.json.JSONObject

class BackupManager(
    private val habitDao: HabitDao,
    private val checkInDao: CheckInDao
) {

    suspend fun export(): String {
        val habits = habitDao.getAll()
        val root = JSONObject()
        root.put("version", 1)

        val habitsArray = JSONArray()
        for (habit in habits) {
            val obj = JSONObject().apply {
                put("name", habit.name)
                put("color", habit.color)
                put("createdAt", habit.createdAt)
                put("sortOrder", habit.sortOrder)
                if (habit.reminderHour != null) put("reminderHour", habit.reminderHour)
                if (habit.reminderMinute != null) put("reminderMinute", habit.reminderMinute)
            }

            val checkIns = checkInDao.getAllByHabit(habit.id)
            val checkInsArray = JSONArray()
            for (ci in checkIns) {
                val ciObj = JSONObject().apply {
                    put("date", ci.date)
                    if (ci.comment != null) put("comment", ci.comment)
                }
                checkInsArray.put(ciObj)
            }
            obj.put("checkIns", checkInsArray)
            habitsArray.put(obj)
        }

        root.put("habits", habitsArray)
        return root.toString(2)
    }

    suspend fun import(json: String) {
        val root = JSONObject(json)
        val habitsArray = root.getJSONArray("habits")

        for (i in 0 until habitsArray.length()) {
            val obj = habitsArray.getJSONObject(i)

            val habitId = habitDao.insert(
                HabitEntity(
                    name = obj.getString("name"),
                    color = obj.getInt("color"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    sortOrder = obj.optInt("sortOrder", 0),
                    reminderHour = if (obj.has("reminderHour")) obj.getInt("reminderHour") else null,
                    reminderMinute = if (obj.has("reminderMinute")) obj.getInt("reminderMinute") else null
                )
            )

            val checkInsArray = obj.optJSONArray("checkIns") ?: continue
            for (j in 0 until checkInsArray.length()) {
                val ciObj = checkInsArray.getJSONObject(j)
                checkInDao.insert(
                    CheckInEntity(
                        habitId = habitId,
                        date = ciObj.getString("date"),
                        comment = if (ciObj.has("comment")) ciObj.getString("comment") else null
                    )
                )
            }
        }
    }
}
