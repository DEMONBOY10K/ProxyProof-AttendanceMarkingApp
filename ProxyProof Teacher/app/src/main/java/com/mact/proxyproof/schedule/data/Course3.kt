package com.mact.proxyproof.schedule.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room
 * */
@Entity(tableName = "timeTable")
data class Course3(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Int?,
    @ColumnInfo(name = "courseSubject") var courseSubject: String?,
    @ColumnInfo(name = "courseSection") var courseSection: String?,
    @ColumnInfo(name = "courseWeekday") var courseWeekday: Int?,
    @ColumnInfo(name = "courseStartTime") var courseStartTime: String?,
    @ColumnInfo(name = "courseEndTime") var courseEndTime: String?,
    @ColumnInfo(name = "courseSemester") var courseSemester: String?,
    @ColumnInfo(name = "courseDepartment") var courseDepartment: String?,
)