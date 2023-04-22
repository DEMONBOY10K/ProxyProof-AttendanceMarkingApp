package com.mact.proxyproof
import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

import kotlinx.android.synthetic.main.activity_uploadschedule.*

class ScheduleActivity : AppCompatActivity(){
    private var department : String?= null
    private var subject : String?= null
    private var classroom : String?= null
    private var timeslot : String?= null
    private var semester : String?= null
    private var currentSubject : ArrayAdapter<CharSequence>? = null
    private var monday = mutableListOf<String>()
    private var tuesday = mutableListOf<String>()
    private var wednesday = mutableListOf<String>()
    private var thursday = mutableListOf<String>()
    private var friday = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_uploadschedule)
//        intializeArrays()

        val dept =
            ArrayAdapter.createFromResource(this, R.array.Department,R.layout.dropdown_menu)
        dept.setDropDownViewResource(R.layout.dropdown_menu)
        spDepartment.adapter = dept

        val timeSlot =
            ArrayAdapter.createFromResource(this, R.array.TimeSlot,R.layout.dropdown_menu)
        timeSlot.setDropDownViewResource(R.layout.dropdown_menu)
        spTimeSlots.adapter = timeSlot
        spDepartment?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("department","Nothing Selected")
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                department = spDepartment.selectedItem.toString()
                Log.d("department", department!!)
                val sem =
                    ArrayAdapter.createFromResource(this@ScheduleActivity, R.array.Sem,R.layout.dropdown_menu)
                sem.setDropDownViewResource(R.layout.dropdown_menu)
                spSemester.adapter = sem
            }

        }
        spSemester?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onNothingSelected(parent: AdapterView<*>?) {
                println("Nothing Selected")
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                semester = spSemester.selectedItem.toString()
                Log.d("semester", semester!!)
                when (department){
                    "MECH" ->{
                        setMechAdapter(semester.toString().toInt())
                        val classroom =
                            ArrayAdapter.createFromResource(this@ScheduleActivity, R.array.MechSection,R.layout.dropdown_menu)
                        classroom.setDropDownViewResource(R.layout.dropdown_menu)
                        spClasses.adapter = classroom
                    }
                    "CSE"->{
                        setCseAdapter(semester.toString().toInt())
                        val classroom =
                            ArrayAdapter.createFromResource(this@ScheduleActivity, R.array.CseSection,R.layout.dropdown_menu)
                        classroom.setDropDownViewResource(R.layout.dropdown_menu)
                        spClasses.adapter = classroom
                    }
                    "MSME"->{
                        setMsmeAdapter(semester.toString().toInt())
                        val classroom =
                            ArrayAdapter.createFromResource(this@ScheduleActivity, R.array.MsmeSection,R.layout.dropdown_menu)
                        classroom.setDropDownViewResource(R.layout.dropdown_menu)
                        spClasses.adapter = classroom
                    }
                    "ECE"->{
                        setEceAdapter(semester.toString().toInt())
                        val classroom =
                            ArrayAdapter.createFromResource(this@ScheduleActivity, R.array.EceSection,R.layout.dropdown_menu)
                        classroom.setDropDownViewResource(R.layout.dropdown_menu)
                        spClasses.adapter = classroom
                    }
                    "EX"->{
                        setExAdapter(semester.toString().toInt())
                        val classroom =
                            ArrayAdapter.createFromResource(this@ScheduleActivity, R.array.ExSection,R.layout.dropdown_menu)
                        classroom.setDropDownViewResource(R.layout.dropdown_menu)
                        spClasses.adapter = classroom
                    }
                    "CHEM"->{
                        setChemAdapter(semester.toString().toInt())
                        val classroom =
                            ArrayAdapter.createFromResource(this@ScheduleActivity, R.array.ChemSection,R.layout.dropdown_menu)
                        classroom.setDropDownViewResource(R.layout.dropdown_menu)
                        spClasses.adapter = classroom
                    }
                    "CIVIL"->{
                        setCivilAdapter(semester.toString().toInt())
                        val classroom =
                            ArrayAdapter.createFromResource(this@ScheduleActivity, R.array.CivilSection,R.layout.dropdown_menu)
                        classroom.setDropDownViewResource(R.layout.dropdown_menu)
                        spClasses.adapter = classroom
                    }
                }

            }

        }
        spClasses?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onNothingSelected(parent: AdapterView<*>?) {
                println("Nothing Selected")
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                classroom = spClasses.selectedItem.toString()
                Log.d("classroom", classroom!!)

            }
        }
        spSubjects?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onNothingSelected(parent: AdapterView<*>?) {
                println("Nothing Selected")
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                subject = spSubjects.selectedItem.toString()
                Log.d("subject", subject!!)
            }
        }

        spTimeSlots?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onNothingSelected(parent: AdapterView<*>?) {
                println("Nothing Selected")
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                timeslot = spTimeSlots.selectedItem.toString()
                Log.d("timeslot", timeslot!!)
            }
        }
        btnSaveSchedule.setOnClickListener {
//            updateArrays()
            Log.d("Arrayvalue","Monday = $monday \n Tuesday = $tuesday \n Wednesday= $wednesday \n Thursday= $thursday \n Friday = $friday")
            Log.d("",spSemester.selectedItem.toString())
//            monday.add(spDepartment.selectedItem.toString())
        }
        val onClickListener = View.OnClickListener { view ->
            // Deactivate all the buttons
            monday_button.isEnabled = false
            tuesday_button.isEnabled = false
            wednesday_button.isEnabled = false
            thursday_button.isEnabled = false
            friday_button.isEnabled = false

            // Activate the clicked button
            view.isEnabled = true
        }

        monday_button.setOnClickListener(onClickListener)
        tuesday_button.setOnClickListener(onClickListener)
        wednesday_button.setOnClickListener(onClickListener)
        thursday_button.setOnClickListener(onClickListener)
        friday_button.setOnClickListener(onClickListener)
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ //Do something after 100ms
            updateArrays()
        }, 1500)
        monday_button.performClick()
        val collapsibleLayout = findViewById<ConstraintLayout>(R.id.collapsibleLayout)
        val headerView = findViewById<FloatingActionButton>(R.id.btnSaveSchedule)


        headerView.setOnClickListener {
            if(collapsibleLayout.visibility == View.VISIBLE)
                collapsibleLayout.visibility = View.INVISIBLE
            else
                collapsibleLayout.visibility = View.VISIBLE
        }

    }
    private fun intializeArrays(){
        val t = "null"
        for(i in 0..6){
            monday.add(t)
            tuesday.add(t)
            wednesday.add(t)
            thursday.add(t)
            friday.add(t)
        }
    }
    private fun updateButtons(Day:String){

    }
    private fun updateArrays(){

        for(i in 0..6){
            Log.d("", i.toString())
            when(i){
                0 ->{

                    monday.add( "Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    tuesday.add( "Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    wednesday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    thursday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    friday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                }
                1 ->{ monday.add( "Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    tuesday.add( "Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    wednesday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    thursday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    friday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                }
                2 ->{monday.add( "Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    tuesday.add( "Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    wednesday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    thursday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    friday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                }
                3 ->{ monday.add( "Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    tuesday.add( "Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    wednesday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    thursday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    friday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                }
                4 ->{monday.add( "Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    tuesday.add( "Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    wednesday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    thursday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    friday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                }
                5 ->{ monday.add( "Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    tuesday.add( "Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    wednesday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    thursday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    friday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                }
                6 ->{ monday.add( "Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    tuesday.add( "Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    wednesday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    thursday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                    friday.add("Dept=" + department + " Sem=" + semester + "Class=" + classroom + "Sub=" + subject)
                }
            }
        }
        btnSaveSchedule.visibility = View.VISIBLE
    }


    private fun setMechAdapter(i:Int) :ArrayAdapter<CharSequence> {
        currentSubject =
            ArrayAdapter.createFromResource(this, R.array.MechSem1,R.layout.dropdown_menu)
        when (i){
            1-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.MechSem1,R.layout.dropdown_menu)
            }
            2-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.MechSem2,R.layout.dropdown_menu)
            }
            3-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.MechSem3,R.layout.dropdown_menu)
            }
            4-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.MechSem4,R.layout.dropdown_menu)
            }
            5-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.MechSem5,R.layout.dropdown_menu)
            }
            6-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.MechSem6,R.layout.dropdown_menu)
            }
            7-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.MechSem7,R.layout.dropdown_menu)
            }
            8-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.MechSem8,R.layout.dropdown_menu)
            }
        }
        currentSubject!!.setDropDownViewResource(R.layout.dropdown_menu)
        spSubjects.adapter = currentSubject
        return currentSubject as ArrayAdapter<CharSequence>
    }
    private fun setCseAdapter(i:Int) :ArrayAdapter<CharSequence> {
        currentSubject =
            ArrayAdapter.createFromResource(this, R.array.CseSem1,R.layout.dropdown_menu)
        when (i){
            1-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.CseSem1,R.layout.dropdown_menu)
            }
            2-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.CseSem2,R.layout.dropdown_menu)
            }
            3-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.CseSem3,R.layout.dropdown_menu)
            }
            4-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.CseSem4,R.layout.dropdown_menu)
            }
            5-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.CseSem5,R.layout.dropdown_menu)
            }
            6-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.CseSem6,R.layout.dropdown_menu)
            }
            7-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.CseSem7,R.layout.dropdown_menu)
            }
            8-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.CseSem8,R.layout.dropdown_menu)
            }
        }
        currentSubject!!.setDropDownViewResource(R.layout.dropdown_menu)
        spSubjects.adapter = currentSubject
        return currentSubject as ArrayAdapter<CharSequence>
    }
    private fun setMsmeAdapter(i:Int) :ArrayAdapter<CharSequence> {
        currentSubject =
            ArrayAdapter.createFromResource(this, R.array.MsmeSem1,R.layout.dropdown_menu)
        when (i){
            1-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.MsmeSem1,R.layout.dropdown_menu)
            }
            2-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.MsmeSem2,R.layout.dropdown_menu)
            }
            3-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.MsmeSem3,R.layout.dropdown_menu)
            }
            4-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.MsmeSem4,R.layout.dropdown_menu)
            }
            5-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.MsmeSem5,R.layout.dropdown_menu)
            }
            6-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.MsmeSem6,R.layout.dropdown_menu)
            }
            7-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.MsmeSem7,R.layout.dropdown_menu)
            }
            8-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.MsmeSem8,R.layout.dropdown_menu)
            }
        }
        currentSubject!!.setDropDownViewResource(R.layout.dropdown_menu)
        spSubjects.adapter = currentSubject
        return currentSubject as ArrayAdapter<CharSequence>
    }
    private fun setEceAdapter(i:Int) :ArrayAdapter<CharSequence> {
        currentSubject =
            ArrayAdapter.createFromResource(this, R.array.EceSem1,R.layout.dropdown_menu)
        when (i){
            1-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.EceSem1,R.layout.dropdown_menu)
            }
            2-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.EceSem2,R.layout.dropdown_menu)
            }
            3-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.EceSem3,R.layout.dropdown_menu)
            }
            4-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.EceSem4,R.layout.dropdown_menu)
            }
            5-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.EceSem5,R.layout.dropdown_menu)
            }
            6-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.EceSem6,R.layout.dropdown_menu)
            }
            7-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.EceSem7,R.layout.dropdown_menu)
            }
            8-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.EceSem8,R.layout.dropdown_menu)
            }
        }
        currentSubject!!.setDropDownViewResource(R.layout.dropdown_menu)
        spSubjects.adapter = currentSubject
        return currentSubject as ArrayAdapter<CharSequence>
    }

    private fun setExAdapter(i:Int) :ArrayAdapter<CharSequence> {
        currentSubject =
            ArrayAdapter.createFromResource(this, R.array.ExSem1,R.layout.dropdown_menu)
        when (i){
            1-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.ExSem1,R.layout.dropdown_menu)
            }
            2-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.ExSem2,R.layout.dropdown_menu)
            }
            3-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.ExSem3,R.layout.dropdown_menu)
            }
            4-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.ExSem4,R.layout.dropdown_menu)
            }
            5-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.ExSem5,R.layout.dropdown_menu)
            }
            6-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.ExSem6,R.layout.dropdown_menu)
            }
            7-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.ExSem7,R.layout.dropdown_menu)
            }
            8-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.ExSem8,R.layout.dropdown_menu)
            }
        }
        currentSubject!!.setDropDownViewResource(R.layout.dropdown_menu)
        spSubjects.adapter = currentSubject
        return currentSubject as ArrayAdapter<CharSequence>
    }

    private fun setChemAdapter(i:Int) :ArrayAdapter<CharSequence> {
        currentSubject=
            ArrayAdapter.createFromResource(this, R.array.ChemSem1,R.layout.dropdown_menu)
        when (i){
            1-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.ChemSem1,R.layout.dropdown_menu)
            }
            2-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.ChemSem2,R.layout.dropdown_menu)
            }
            3-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.ChemSem3,R.layout.dropdown_menu)
            }
            4-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.ChemSem4,R.layout.dropdown_menu)
            }
            5-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.ChemSem5,R.layout.dropdown_menu)
            }
            6-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.ChemSem6,R.layout.dropdown_menu)
            }
            7-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.ChemSem7,R.layout.dropdown_menu)
            }
            8-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.ChemSem8,R.layout.dropdown_menu)
            }
        }
        currentSubject!!.setDropDownViewResource(R.layout.dropdown_menu)
        spSubjects.adapter = currentSubject
        return currentSubject as ArrayAdapter<CharSequence>
    }

    private fun setCivilAdapter(i:Int) :ArrayAdapter<CharSequence> {
        currentSubject =
            ArrayAdapter.createFromResource(this, R.array.CivilSem1,R.layout.dropdown_menu)
        when (i){
            1-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.CivilSem1,R.layout.dropdown_menu)
            }
            2-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.CivilSem2,R.layout.dropdown_menu)
            }
            3-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.CivilSem3,R.layout.dropdown_menu)
            }
            4-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.CivilSem4,R.layout.dropdown_menu)
            }
            5-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.CivilSem5,R.layout.dropdown_menu)
            }
            6-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.CivilSem6,R.layout.dropdown_menu)
            }
            7-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.CivilSem7,R.layout.dropdown_menu)
            }
            8-> {
                currentSubject =
                    ArrayAdapter.createFromResource(this, R.array.CivilSem8,R.layout.dropdown_menu)
            }
        }
        currentSubject!!.setDropDownViewResource(R.layout.dropdown_menu)
        spSubjects.adapter = currentSubject
        return currentSubject as ArrayAdapter<CharSequence>
    }
}
