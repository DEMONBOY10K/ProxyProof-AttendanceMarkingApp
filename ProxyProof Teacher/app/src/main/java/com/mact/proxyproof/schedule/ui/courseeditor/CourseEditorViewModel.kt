package com.mact.proxyproof.schedule.ui.courseeditor


import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mact.proxyproof.R
import com.mact.proxyproof.dataclass.ScheduleDay
import com.mact.proxyproof.schedule.data.Course3
import com.mact.proxyproof.schedule.data.CourseRepository
import com.mact.proxyproof.schedule.utilities.INTENT_EXTRA_COURSE_ID_DEFAULT_VALUE
import kotlinx.coroutines.launch
import java.lang.Exception

class CourseEditorViewModel(
    private val repository: CourseRepository,
    private val courseId: Int,
    private val currentViewPagerItem: Int
) : ViewModel() {

    var isEditMode = MutableLiveData<Boolean>(false)

    private var courseIdFromDatabase = MutableLiveData<Int>(null)
    var courseName = MutableLiveData<String>()
    var coursePlace = MutableLiveData<String>()
    var courseProf = MutableLiveData<String?>()
    var courseBeginTime = MutableLiveData<String>()
    var courseEndTime = MutableLiveData<String>()
    var courseWeekday = MutableLiveData<Int>(0)

    var courseNameError = MutableLiveData<Boolean>()
    var coursePlaceError = MutableLiveData<Boolean>()
    var courseBeginTimeError = MutableLiveData<Boolean>()
    var courseEndTimeError = MutableLiveData<Boolean>()
    var courseWeekdayError = MutableLiveData<Boolean>()
    var courseProfError = MutableLiveData<Boolean>()

    var openTimePicker = MutableLiveData<Boolean>(false)
    var pickBeginOrEnd = MutableLiveData<Int>()
    var courseBeginTimeForEdit = MutableLiveData<Int>()
    var courseEndTimeForEdit = MutableLiveData<Int>()

    var isSavedSuccessfully = MutableLiveData<Boolean>(false)

    private lateinit var database : DatabaseReference
    private lateinit var user : FirebaseAuth

    init {
        if (courseId != INTENT_EXTRA_COURSE_ID_DEFAULT_VALUE) {
            // Edit mode, if course id is provided.
            isEditMode.value = true
            setupValueForEditing()
        } else {
            // Add mode, no course id is provided.
            courseWeekday.value = currentViewPagerItem
//            courseDepartment.value = currentViewPagerItem
//            courseSemester.value = currentViewPagerItem
        }
    }

    /**
     * If start in edit mode, get course information from database, then set to screen.
     * */
    private fun setupValueForEditing() {
        viewModelScope.launch {
            val courseInfo = repository.getCourseById(courseId)
            courseIdFromDatabase.postValue(courseInfo.id!!)
            courseName.postValue(courseInfo.courseSubject!!)
            coursePlace.postValue(courseInfo.courseSection!!)
            courseProf.postValue(courseInfo.courseSemester)
            courseWeekday.postValue(courseInfo.courseWeekday!!)
            courseBeginTime.postValue(courseInfo.courseStartTime!!)
            courseEndTime.postValue(courseInfo.courseEndTime!!)
        }
    }

    /**
     * Open timepicker when clicked, 0 for begin time, 1 for end time.
     * */
    fun selectBeginOrEndTime(beginOrAnd: Int) {
        openTimePicker.value = !openTimePicker.value!!
        this.pickBeginOrEnd.value = beginOrAnd
    }

    /**
     * Check required entries are not empty.
     *
     * @return true, if EMPTY
     * @return false, if NOT EMPTY
     * */
    private fun isRequiredEntriesEmpty(): Boolean {
        return courseName.value.isNullOrEmpty() ||
                coursePlace.value.isNullOrBlank() ||
                courseBeginTime.value.isNullOrEmpty() ||
                courseEndTime.value.isNullOrEmpty()
    }


    /**
     * Change error value to true, use to highlight the empty entry.
     * */
    private fun markEmptyEntries() {
        courseNameError.value = courseName.value.isNullOrEmpty()
        coursePlaceError.value = coursePlace.value.isNullOrEmpty()
        courseBeginTimeError.value = courseBeginTime.value.isNullOrEmpty()
        courseEndTimeError.value = courseEndTime.value.isNullOrEmpty()
    }

    /**
     * User clicked save, start process it.
     * */
    fun saveFired(
        courseDepartment: String,
        courseSemester: String,
        courseSection: String,
        courseSubject: String,
        courseTimeSlot: String,
        url: String
    ) {
        val (startTime, endTime) = convertTimeFormat(courseTimeSlot)
        Log.i(
            TAG, "Sub = $courseSubject | Dept = $courseDepartment " +
                    " |Sec =  $courseSection | WeekDay = ${courseWeekday.value}" +
                    " | Time  = $courseTimeSlot |Semester = $courseSemester| Start = $startTime | End = $endTime"
        )

        // Check entries
//        if (isRequiredEntriesEmpty()) {
//            markEmptyEntries()
//            return
//        } else {
//            markEmptyEntries()
//        }

        // Making cake
        val course = Course3(
            id = courseIdFromDatabase.value,
            courseSubject = courseSubject,
            courseSection = courseSection,
            courseWeekday = courseWeekday.value,
            courseStartTime = startTime,
            courseEndTime = endTime,
            courseSemester = courseSemester,
            courseDepartment = courseDepartment
        )
        Log.d("ID", courseIdFromDatabase.value.toString())
        user = FirebaseAuth.getInstance()
        val userName  = emailToUserName(user.currentUser?.email!!)

        database = FirebaseDatabase.getInstance(url).getReference("teachers")
        val weekday = weekday(courseWeekday.value)
        val currentSchedule = ScheduleDay(courseDepartment,courseSemester,courseSubject,courseSection,courseTimeSlot)
        database.child(userName).child("Schedule").child(weekday).child(courseTimeSlot).setValue(currentSchedule).addOnCompleteListener{
            Log.d(TAG, "Uploaded Schedule to Firebase Successfully")
        }.addOnFailureListener{
            Log.d(TAG, "Failed to Upload Schedule to Firebase ")
        }
        // Done
        if (isEditMode.value != true) {
            // Add Mode
            viewModelScope.launch {
                try {
                    repository.insertCourse(course)
                    isSavedSuccessfully.value = true
                } catch (e: Exception){
                    Log.i(TAG, "insert failed")
                }
            }
        } else {
            // Edit Mode
            viewModelScope.launch {
                try {
                    repository.updateCourse(course)
                    isSavedSuccessfully.value = true
                } catch (e: Exception){
                    Log.i(TAG, "update failed")
                }
            }
        }
    }

    private fun emailToUserName(email : String ): String{
        var userName = email
        val regex = Regex("[^A-Za-z0-9]")
        userName = regex.replace(userName, "")
        return userName
    }
    private fun weekday(day: Int?):String {
         val dayString = when (day) {
            0 -> "Monday"
            1 -> "Tuesday"
            2 -> "Wednesday"
            3 -> "Thursday"
            4 -> "Friday"
            5 -> "Saturday"
            6 -> "Sunday"
            else -> {"Day"}
        }
        return dayString
    }
    fun convertTimeFormat(inputString: String): Pair<String, String> {
        val timeRegex = Regex("(\\d+)(am|pm|noon)-(\\d+)(am|pm|noon)")

        val matchResult = timeRegex.find(inputString)

        if (matchResult == null) {
            throw IllegalArgumentException("Invalid input string")
        }

        val startHour = when(matchResult.groupValues[2]) {
            "am" -> matchResult.groupValues[1].toInt()
            "pm" -> matchResult.groupValues[1].toInt() + 12
            "noon" -> 12
            else -> throw IllegalArgumentException("Invalid input string")
        }

        val endHour = when(matchResult.groupValues[4]) {
            "am" -> matchResult.groupValues[3].toInt()
            "pm" -> matchResult.groupValues[3].toInt() + 12
            "noon" -> 12
            else -> throw IllegalArgumentException("Invalid input string")
        }

        val startTime = "%02d00".format(startHour)
        val endTime = "%02d00".format(endHour)

        return Pair(startTime, endTime)
    }


    companion object {
        private const val TAG = "CourseEditorViewModel"
    }
}

/**
 * Factory of Course Editor ViewModel
 *
 * @param repository CourseRepository in this project.
 * @param courseId Default value is -1, passing course id to get into edit mode.
 * @param currentViewPagerItem Default value is 0, which is Monday.
 *                             Passing current viewpager item to automatic set weekday.
 * @return CourseEditorViewModel
 * */
class CourseEditorViewModelFactory(
    private val repository: CourseRepository,
    private val courseId: Int,
    private val currentViewPagerItem: Int
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CourseEditorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CourseEditorViewModel(repository, courseId, currentViewPagerItem) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}