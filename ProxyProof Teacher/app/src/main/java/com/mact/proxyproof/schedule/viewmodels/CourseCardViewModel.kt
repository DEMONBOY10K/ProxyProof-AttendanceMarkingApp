package com.mact.proxyproof.schedule.viewmodels

import com.mact.proxyproof.schedule.data.Course3

class CourseCardViewModel(course3: Course3) {

    val course = course3

    val id = course.id

    val courseName = course.courseSubject

    val coursePlace = course.courseSection

    val courseWeekday = course.courseWeekday

    val courseStartTime
        get() = course.courseStartTime?.replace("..(?!$)".toRegex(), "$0:")

    val courseEndTime
        get() = course.courseEndTime?.replace("..(?!$)".toRegex(), "$0:")

    val profName
        get() = if (!course.courseSemester.isNullOrBlank()) ", ${course.courseSemester}" else ""
}