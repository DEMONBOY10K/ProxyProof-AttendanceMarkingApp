package com.mact.proxyproof.schedule.ui.courseeditor

import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.mact.proxyproof.R
import com.mact.proxyproof.databinding.FragmentCourseEditorBinding
import com.mact.proxyproof.schedule.MyApplication
import com.mact.proxyproof.schedule.utilities.INTENT_TIMETABLE_CHANGED
import com.mact.proxyproof.schedule.widget.TimetableWidgetProvider
import java.text.SimpleDateFormat
import java.util.*

private const val TAG_TIME_PICKER_BEGIN_TIME = 0
private const val TAG_TIME_PICKER_END_TIME = 1

/**
 * An editor to add or edit the class info.
 * */
class CourseEditorFragment : Fragment() {

    var courseDepartment : String? = null
    var courseSemester : String? = null
    var courseSection : String? = null
    var courseSubject : String? = null
    var courseTimeSlot : String? = null

    private lateinit var binding: FragmentCourseEditorBinding
    private val courseEditorViewModel: CourseEditorViewModel by viewModels {
        CourseEditorViewModelFactory(
            (requireActivity().application as MyApplication).courseRepository,
            args.courseId,
            args.currentViewPagerItem
        )
    }

    private val args: CourseEditorFragmentArgs by navArgs()

    private val weekdayArray by lazy { resources.getStringArray(R.array.weekdayList) }
    private val departmentArray by lazy { resources.getStringArray(R.array.Department) }
    private val semesterArray by lazy { resources.getStringArray(R.array.Sem) }
    private val subjectArray by lazy { resources.getStringArray(R.array.CseSem1) }
    private val sectionArray by lazy { resources.getStringArray(R.array.CseSection) }
    private val timeSlotArray by lazy { resources.getStringArray(R.array.TimeSlot) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            exitConfirmDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCourseEditorBinding.inflate(inflater, container, false)
        binding.viewModel = courseEditorViewModel
        binding.lifecycleOwner = this

        setupToolBar()
        setupWeekdayDropdown()

        subscribeUi()
        subscribeUiForError()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
//        val adRequest = AdRequest.Builder().build()
//        binding.adViewCourseEditorFrag.loadAd(adRequest)
    }

    private fun setupToolBar() {
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarCourseEditorFrag)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_close_24)

        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menuSave -> {
                        if(validateDepartment()  && validateSemester() && validateSection() && validateSubject() && validateTimeSlot()){
                            courseDepartment =binding.dropDownDepartment.text.toString()
                            courseSemester =binding.dropDownSemester.text.toString()
                            courseSection =binding.dropDownSection.text.toString()
                            courseSubject =binding.dropDownSubject.text.toString()
                            courseTimeSlot=binding.dropDownTimeSlot.text.toString()
                            Log.d("department", "$courseDepartment ,$courseSemester, $courseSection ,$courseSubject, $courseTimeSlot")
                            val url = getString(R.string.firebase_db_location)
                            courseEditorViewModel.saveFired(courseDepartment!!,
                                courseSemester!!, courseSection!!, courseSubject!!, courseTimeSlot!!,url
                            )
                        }
                        true
                    }
                    android.R.id.home -> {

                        exitConfirmDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        courseEditorViewModel.isEditMode.observe(viewLifecycleOwner) {
            // Set actionBar text base on mode.
            (activity as AppCompatActivity).supportActionBar!!.title = if (it) {
                getString(R.string.courseEditor_titleEdit)
            } else {
                getString(R.string.courseEditor_titleAdd)
            }
        }
    }

    /**
     * Set weekday selector text by is edit mode or not.
     * Little hack here, the text and real value is not associate.
     * */
    private fun setupWeekdayDropdown() {
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, weekdayArray)
        (binding.dropDownCourseEditorFrag as? AutoCompleteTextView)?.setAdapter(adapter)
        val department = ArrayAdapter(requireContext(), R.layout.list_item, departmentArray)
        (binding.dropDownDepartment as? AutoCompleteTextView)?.setAdapter(department)
        val semester = ArrayAdapter(requireContext(), R.layout.list_item, semesterArray)
        (binding.dropDownSemester as? AutoCompleteTextView)?.setAdapter(semester)
        val subject = ArrayAdapter(requireContext(), R.layout.list_item, subjectArray)
        (binding.dropDownSubject as? AutoCompleteTextView)?.setAdapter(subject)
        val section = ArrayAdapter(requireContext(), R.layout.list_item, sectionArray)
        (binding.dropDownSection as? AutoCompleteTextView)?.setAdapter(section)
        val timeslot = ArrayAdapter(requireContext(), R.layout.list_item, timeSlotArray)
        (binding.dropDownTimeSlot as? AutoCompleteTextView)?.setAdapter(timeslot)

    }
    private fun setSemAdapter(stringArray: Int){
        val semArray by lazy { resources.getStringArray(stringArray) }
        val semester = ArrayAdapter(requireContext(), R.layout.list_item, semArray)
        (binding.dropDownSemester as? AutoCompleteTextView)?.setAdapter(semester)
    }
    private fun setSubAdapter(stringArray: Int) {
        val subArray by lazy { resources.getStringArray(stringArray) }
        val subject = ArrayAdapter(requireContext(), R.layout.list_item, subArray)
        (binding.dropDownSubject as? AutoCompleteTextView)?.setAdapter(subject)
    }
    private fun setSecAdapter(stringArray: Int){
        val secArray by lazy { resources.getStringArray(stringArray) }
        val section = ArrayAdapter(requireContext(), R.layout.list_item, secArray)
        (binding.dropDownSection as? AutoCompleteTextView)?.setAdapter(section)
    }

    private fun subscribeUi() {
        binding.tilSection.isEnabled = false
        binding.tilSubject.isEnabled = false
        binding.tilSemester.isEnabled = false
        binding.dropDownDepartment.setOnItemClickListener { adapterView, view, i, l ->
            val department = binding.dropDownDepartment.text.toString()
            Log.d("department", department)
            setSemAdapter(R.array.Sem)
            binding.dropDownSubject.clearListSelection()
            when (department){
                "MECH" ->{
                    setSecAdapter(R.array.MechSection)
                }
                "CSE"->{
                    setSecAdapter(R.array.CseSection)
                }
                "MSME"->{
                    setSecAdapter(R.array.MsmeSection)
                }
                "ECE"->{
                    setSecAdapter(R.array.EceSection)
                }
                "EX"->{
                    setSecAdapter(R.array.ExSection)
                }
                "CHEM"->{
                    setSecAdapter(R.array.ChemSection)
                }
                "CIVIL"->{
                    setSecAdapter(R.array.CivilSection)
                }
            }
            binding.dropDownSemester.setText("")
            binding.dropDownSection.setText("")
            binding.dropDownSubject.setText("")
            binding.tilSection.isEnabled = false
            binding.tilSubject.isEnabled = false
            binding.tilSemester.isEnabled = true

//                val sem =
//                    ArrayAdapter.createFromResource(this@ScheduleActivity, R.array.Sem,R.layout.dropdown_menu)
//                sem.setDropDownViewResource(R.layout.dropdown_menu)
//                spSemester.adapter = sem
        }
        binding.dropDownSemester.setOnItemClickListener { adapterView, view, i, l ->
            val semester = binding.dropDownSemester.text.toString()
            Log.d("department", semester)
            when (semester){
                "1" ->{
                    when(binding.dropDownDepartment.text.toString()){
                        "MECH" ->{
                            setSubAdapter(R.array.MechSem1)
                        }
                        "CSE"->{
                            setSubAdapter(R.array.CseSem1)
                        }
                        "MSME"->{
                            setSubAdapter(R.array.MsmeSem1)
                        }
                        "ECE"->{
                            setSubAdapter(R.array.EceSem1)
                        }
                        "EX"->{
                            setSubAdapter(R.array.ExSem1)
                        }
                        "CHEM"->{
                            setSubAdapter(R.array.ChemSem1)
                        }
                        "CIVIL"->{
                            setSubAdapter(R.array.CivilSem1)
                        }
                    }
                }
                "2"->{
                    when(binding.dropDownDepartment.text.toString()){
                        "MECH" ->{
                            setSubAdapter(R.array.MechSem2)
                        }
                        "CSE"->{
                            setSubAdapter(R.array.CseSem2)
                        }
                        "MSME"->{
                            setSubAdapter(R.array.MsmeSem2)
                        }
                        "ECE"->{
                            setSubAdapter(R.array.EceSem2)
                        }
                        "EX"->{
                            setSubAdapter(R.array.ExSem2)
                        }
                        "CHEM"->{
                            setSubAdapter(R.array.ChemSem2)
                        }
                        "CIVIL"->{
                            setSubAdapter(R.array.CivilSem2)
                        }
                    }
                }
                "3"->{
                    when(binding.dropDownDepartment.text.toString()){
                        "MECH" ->{
                            setSubAdapter(R.array.MechSem3)
                        }
                        "CSE"->{
                            setSubAdapter(R.array.CseSem3)
                        }
                        "MSME"->{
                            setSubAdapter(R.array.MsmeSem3)
                        }
                        "ECE"->{
                            setSubAdapter(R.array.EceSem3)
                        }
                        "EX"->{
                            setSubAdapter(R.array.ExSem3)
                        }
                        "CHEM"->{
                            setSubAdapter(R.array.ChemSem3)
                        }
                        "CIVIL"->{
                            setSubAdapter(R.array.CivilSem3)
                        }
                    }
                }
                "4"->{
                    when(binding.dropDownDepartment.text.toString()){
                        "MECH" ->{
                            setSubAdapter(R.array.MechSem4)
                        }
                        "CSE"->{
                            setSubAdapter(R.array.CseSem4)
                        }
                        "MSME"->{
                            setSubAdapter(R.array.MsmeSem4)
                        }
                        "ECE"->{
                            setSubAdapter(R.array.EceSem4)
                        }
                        "EX"->{
                            setSubAdapter(R.array.ExSem4)
                        }
                        "CHEM"->{
                            setSubAdapter(R.array.ChemSem4)
                        }
                        "CIVIL"->{
                            setSubAdapter(R.array.CivilSem4)
                        }
                    }
                }
                "5"->{
                    when(binding.dropDownDepartment.text.toString()){
                        "MECH" ->{
                            setSubAdapter(R.array.MechSem5)
                        }
                        "CSE"->{
                            setSubAdapter(R.array.CseSem5)
                        }
                        "MSME"->{
                            setSubAdapter(R.array.MsmeSem5)
                        }
                        "ECE"->{
                            setSubAdapter(R.array.EceSem5)
                        }
                        "EX"->{
                            setSubAdapter(R.array.ExSem5)
                        }
                        "CHEM"->{
                            setSubAdapter(R.array.ChemSem5)
                        }
                        "CIVIL"->{
                            setSubAdapter(R.array.CivilSem5)
                        }
                    }
                }
                "6"->{
                    when(binding.dropDownDepartment.text.toString()){
                        "MECH" ->{
                            setSubAdapter(R.array.MechSem6)
                        }
                        "CSE"->{
                            setSubAdapter(R.array.CseSem6)
                        }
                        "MSME"->{
                            setSubAdapter(R.array.MsmeSem6)
                        }
                        "ECE"->{
                            setSubAdapter(R.array.EceSem6)
                        }
                        "EX"->{
                            setSubAdapter(R.array.ExSem6)
                        }
                        "CHEM"->{
                            setSubAdapter(R.array.ChemSem6)
                        }
                        "CIVIL"->{
                            setSubAdapter(R.array.CivilSem6)
                        }
                    }
                }
                "7"->{
                    when(binding.dropDownDepartment.text.toString()){
                        "MECH" ->{
                            setSubAdapter(R.array.MechSem7)
                        }
                        "CSE"->{
                            setSubAdapter(R.array.CseSem7)
                        }
                        "MSME"->{
                            setSubAdapter(R.array.MsmeSem7)
                        }
                        "ECE"->{
                            setSubAdapter(R.array.EceSem7)
                        }
                        "EX"->{
                            setSubAdapter(R.array.ExSem7)
                        }
                        "CHEM"->{
                            setSubAdapter(R.array.ChemSem7)
                        }
                        "CIVIL"->{
                            setSubAdapter(R.array.CivilSem7)
                        }
                    }
                }
                "8"->{
                    when(binding.dropDownDepartment.text.toString()){
                        "MECH" ->{
                            setSubAdapter(R.array.MechSem8)
                        }
                        "CSE"->{
                            setSubAdapter(R.array.CseSem8)
                        }
                        "MSME"->{
                            setSubAdapter(R.array.MsmeSem8)
                        }
                        "ECE"->{
                            setSubAdapter(R.array.EceSem8)
                        }
                        "EX"->{
                            setSubAdapter(R.array.ExSem8)
                        }
                        "CHEM"->{
                            setSubAdapter(R.array.ChemSem8)
                        }
                        "CIVIL"->{
                            setSubAdapter(R.array.CivilSem8)
                        }
                    }
                }
            }
            binding.dropDownSection.setText("")
            binding.dropDownSubject.setText("")
            binding.tilSection.isEnabled = true
            binding.tilSubject.isEnabled = true

        }



        // Handle selection of weekday dropdown.
        binding.dropDownCourseEditorFrag.setOnItemClickListener { adapterView, view, position, rowId ->
            courseEditorViewModel.courseWeekday.value = position
        }

        // Change weekday dropdown text. In order to set init text when is edit mode, observe it.
        courseEditorViewModel.courseWeekday.observe(viewLifecycleOwner) {
            binding.dropDownCourseEditorFrag.setText(weekdayArray[it], false)
        }

//        // Select course begin time
//        binding.textViewCourseEditorFragCourseBeginTime.setOnClickListener {
//            showMaterialTimePicker(TAG_TIME_PICKER_BEGIN_TIME)
//        }
//
//        // Select course end time
//        binding.textViewCourseEditorFragCourseEndTime.setOnClickListener {
//            showMaterialTimePicker(TAG_TIME_PICKER_END_TIME)
//        }
//
//        courseEditorViewModel.courseBeginTime.observe(viewLifecycleOwner) {
//            binding.textViewCourseEditorFragCourseBeginTime.setText(displayFormattedTime(it))
//        }
//
//        courseEditorViewModel.courseEndTime.observe(viewLifecycleOwner) {
//            binding.textViewCourseEditorFragCourseEndTime.setText(displayFormattedTime(it))
//        }

        // Close current activity
        courseEditorViewModel.isSavedSuccessfully.observe(viewLifecycleOwner) {
            // Send broadcast intent to update the widget.
            val intent = Intent(requireContext(), TimetableWidgetProvider::class.java).apply {
                action = INTENT_TIMETABLE_CHANGED
            }
            requireContext().sendBroadcast(intent)

            // Close fragment.
            if (it) findNavController().navigateUp()
        }
    }
    fun validateDepartment():Boolean{
        if(binding.dropDownDepartment.text.toString().isEmpty()){
            binding.tilDepartment.isErrorEnabled = true
            binding.tilDepartment.error = getString(R.string.courseEditor_noEntry)
            return false
        }
        else {
            binding.tilDepartment.isErrorEnabled = false
        }
        return true
    }
    fun validateSemester():Boolean{
        if(binding.dropDownSemester.text.toString().isEmpty()){
            binding.tilSemester.isErrorEnabled = true
            binding.tilSemester.error = getString(R.string.courseEditor_noEntry)
            return false
        }
        else {
            binding.tilSemester.isErrorEnabled = false
        }
        return true
    }
    fun validateSubject():Boolean{
        if(binding.dropDownSubject.text.toString().isEmpty()){
            binding.tilSubject.isErrorEnabled = true
            binding.tilSubject.error = getString(R.string.courseEditor_noEntry)
            return false
        }
        else {
            binding.tilSubject.isErrorEnabled = false
        }
        return true
    }
    fun validateSection():Boolean{
        if(binding.dropDownSection.text.toString().isEmpty()){
            binding.tilSection.isErrorEnabled = true
            binding.tilSection.error = getString(R.string.courseEditor_noEntry)
            return false
        }
        else {
            binding.tilSection.isErrorEnabled = false
        }
        return true
    }
    fun validateTimeSlot():Boolean{
        if(binding.dropDownTimeSlot.text.toString().isEmpty()){
            binding.tilTimeSlot.isErrorEnabled = true
            binding.tilTimeSlot.error = getString(R.string.courseEditor_noEntry)
            return false
        }
        else {
            binding.tilTimeSlot.isErrorEnabled = false
        }
        return true
    }

    /**
     * Subscribe Ui for error event.
     * */
    private fun subscribeUiForError() {
//        courseEditorViewModel.courseNameError.observe(viewLifecycleOwner) {
//            if (it) {
//                binding.tilCourseEditorFragCourseNameEntry.isErrorEnabled = true
//                binding.tilCourseEditorFragCourseNameEntry.error =
//                    getString(R.string.courseEditor_noEntry)
//            } else {
//                binding.tilCourseEditorFragCourseNameEntry.isErrorEnabled = false
//            }
//        }
//
//        courseEditorViewModel.coursePlaceError.observe(viewLifecycleOwner) {
//            if (it) {
//                binding.tilCourseEditorFragCoursePlaceEntry.isErrorEnabled = true
//                binding.tilCourseEditorFragCoursePlaceEntry.error =
//                    getString(R.string.courseEditor_noEntry)
//            } else {
//                binding.tilCourseEditorFragCoursePlaceEntry.isErrorEnabled = false
//            }
//        }

//        courseEditorViewModel.courseBeginTimeError.observe(viewLifecycleOwner) {
//            if (it) {
//                binding.tilCourseEditorFragCourseBeginTimeEntry.isErrorEnabled = true
//                binding.tilCourseEditorFragCourseBeginTimeEntry.error =
//                    getString(R.string.courseEditor_noEntry)
//            } else {
//                binding.tilCourseEditorFragCourseBeginTimeEntry.isErrorEnabled = false
//            }
//        }
//
//        courseEditorViewModel.courseEndTimeError.observe(viewLifecycleOwner) {
//            if (it) {
//                binding.tilCourseEditorFragCourseEndTimeEntry.isErrorEnabled = true
//                binding.tilCourseEditorFragCourseEndTimeEntry.error =
//                    getString(R.string.courseEditor_noEntry)
//            } else {
//                binding.tilCourseEditorFragCourseEndTimeEntry.isErrorEnabled = false
//            }
//        }
    }

    /**
     * Show dialog when user tries to exit course editor.
     * */
    private fun exitConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.courseEditor_exitConfirmDialogTitle)
            setMessage(R.string.courseEditor_exitConfirmDialogMsg)
            setPositiveButton(R.string.all_confirm) { _, _ ->
                findNavController().navigateUp()
            }
            setNegativeButton(R.string.all_cancel) { _, _ ->

            }
            show()
        }
    }

    private fun showMaterialTimePicker(isBeginOrEnd: Int) {
        // Determine system time format.
        val isSystem24Hour = DateFormat.is24HourFormat(requireContext())
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

        val materialTimePicker = MaterialTimePicker.Builder()
            .setTimeFormat(clockFormat)
            .build()
        materialTimePicker.show(requireActivity().supportFragmentManager, "tag")

        materialTimePicker.addOnPositiveButtonClickListener {
            val newHour = materialTimePicker.hour
            val newMinute = materialTimePicker.minute
            this.onTimeSet(newHour, newMinute, isBeginOrEnd)
        }
    }

    private fun onTimeSet(newHour: Int, newMinute: Int, isBeginOrEnd: Int) {
        val timeToDatabase = String.format("%02d%02d", newHour, newMinute)

        if (isBeginOrEnd == TAG_TIME_PICKER_BEGIN_TIME) {
            courseEditorViewModel.courseBeginTime.value = timeToDatabase
        } else {
            courseEditorViewModel.courseEndTime.value = timeToDatabase
        }
    }

    private fun displayFormattedTime(time: String): String {
        val formatter = SimpleDateFormat("a hh:mm", Locale.getDefault())
        val cal = Calendar.getInstance()

        val temp = StringBuilder().append(time)
        val h = temp.substring(0, 2)
        val m = temp.substring(2, 4)

        cal[Calendar.HOUR_OF_DAY] = h.toInt()
        cal[Calendar.MINUTE] = m.toInt()
        cal.isLenient = false

        return formatter.format(cal.time)
    }

    companion object {
        private const val TAG = "CourseEditorFragment"
    }
}