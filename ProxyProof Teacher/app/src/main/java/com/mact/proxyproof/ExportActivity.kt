package com.mact.proxyproof

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_export.*
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.jvm.internal.Intrinsics


class ExportActivity : AppCompatActivity() {
    private var exportFile: File? = null

    private val root = Environment.getExternalStorageDirectory().absolutePath
    private val dir = "$root/Attendance/Exported"
    private var exportDir = File("$root/Attendance/Exported")
    private val importDir = File("$root/Attendance/Imported")
    private val hssfWorkbook = HSSFWorkbook()
    private val attendees: HSSFSheet = hssfWorkbook.createSheet("Attendees")
    private val absentees: HSSFSheet = hssfWorkbook.createSheet("Absentees")
    private var attendance = mutableListOf<String>()
    private var disposable: Disposable? = null
    private var department : String?= null
    private var subject : String?= null
    private var classroom : String?= null
    private var timeslot : String?= null
    private var semester : String?= null
    private var currentSubject : ArrayAdapter<CharSequence>? = null
    private var timeStamp : String? = null
    private var schedule :String? = null
    private val currentDate: Date = Calendar.getInstance().time
    private val dateFormat = SimpleDateFormat("dd-MMM-yy hh:mm", Locale.getDefault())
    private var currentDepartAdap : ArrayAdapter<CharSequence>? = null
    private var currentSubAdap : ArrayAdapter<CharSequence>? = null
    private var currentSemtAdap : ArrayAdapter<CharSequence>? = null
    private var currentSecAdap : ArrayAdapter<CharSequence>? = null
    private var currentTimeAdap : ArrayAdapter<CharSequence>? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export)

        val spSubject = findViewById<Spinner>(R.id.spSubject)
        val spSemester = findViewById<Spinner>(R.id.spSem)
        val spClass = findViewById<Spinner>(R.id.spClass)
        val spDept = findViewById<Spinner>(R.id.spDept)
        val spTimeSlot = findViewById<Spinner>(R.id.spTimeSlot)

        val dept =
            ArrayAdapter.createFromResource(this, R.array.Department,R.layout.dropdown_menu)
        currentDepartAdap = dept
        dept.setDropDownViewResource(R.layout.dropdown_menu)
        spDept.adapter = dept
        val timeSlot =
            ArrayAdapter.createFromResource(this, R.array.TimeSlot,R.layout.dropdown_menu)
        currentTimeAdap = timeSlot
        timeSlot.setDropDownViewResource(R.layout.dropdown_menu)
        spTimeSlot.adapter = timeSlot
//        textmsg = findViewById(R.id.editText1)

        val btnExport: Button? = findViewById(R.id.btnExportXLS)
//        val btnImport: Button? = findViewById(R.id.btnImportTxt)
        exportDir.mkdirs()

//        importFile = File(importDir, "1.txt")
        spDept?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("department","Nothing Selected")
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                department = spDept.selectedItem.toString()
                Log.d("department", department!!)
                val sem =
                    ArrayAdapter.createFromResource(this@ExportActivity, R.array.Sem,R.layout.dropdown_menu)
                sem.setDropDownViewResource(R.layout.dropdown_menu)
                currentSemtAdap = sem
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
                                ArrayAdapter.createFromResource(this@ExportActivity, R.array.MechSection,R.layout.dropdown_menu)
                            classroom.setDropDownViewResource(R.layout.dropdown_menu)
                            spClass.adapter = classroom
                            currentSecAdap = classroom
                        }
                        "CSE"->{
                            setCseAdapter(semester.toString().toInt())
                            val classroom =
                                ArrayAdapter.createFromResource(this@ExportActivity, R.array.CseSection,R.layout.dropdown_menu)
                            classroom.setDropDownViewResource(R.layout.dropdown_menu)
                            spClass.adapter = classroom
                            currentSecAdap = classroom
                        }
                        "MSME"->{
                            setMsmeAdapter(semester.toString().toInt())
                            val classroom =
                                ArrayAdapter.createFromResource(this@ExportActivity, R.array.MsmeSection,R.layout.dropdown_menu)
                            classroom.setDropDownViewResource(R.layout.dropdown_menu)
                            spClass.adapter = classroom
                            currentSecAdap = classroom
                        }
                        "ECE"->{
                            setEceAdapter(semester.toString().toInt())
                            val classroom =
                                ArrayAdapter.createFromResource(this@ExportActivity, R.array.EceSection,R.layout.dropdown_menu)
                            classroom.setDropDownViewResource(R.layout.dropdown_menu)
                            spClass.adapter = classroom
                            currentSecAdap = classroom
                        }
                        "EX"->{
                            setExAdapter(semester.toString().toInt())
                            val classroom =
                                ArrayAdapter.createFromResource(this@ExportActivity, R.array.ExSection,R.layout.dropdown_menu)
                            classroom.setDropDownViewResource(R.layout.dropdown_menu)
                            spClass.adapter = classroom
                            currentSecAdap = classroom
                        }
                        "CHEM"->{
                            setChemAdapter(semester.toString().toInt())
                            val classroom =
                                ArrayAdapter.createFromResource(this@ExportActivity, R.array.ChemSection,R.layout.dropdown_menu)
                            classroom.setDropDownViewResource(R.layout.dropdown_menu)
                            spClass.adapter = classroom
                            currentSecAdap = classroom
                        }
                        "CIVIL"->{
                            setCivilAdapter(semester.toString().toInt())
                            val classroom =
                                ArrayAdapter.createFromResource(this@ExportActivity, R.array.CivilSection,R.layout.dropdown_menu)
                            classroom.setDropDownViewResource(R.layout.dropdown_menu)
                            spClass.adapter = classroom
                            currentSecAdap = classroom
                        }
                }

            }

        }
        spClass?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onNothingSelected(parent: AdapterView<*>?) {
                println("Nothing Selected")
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                classroom = spClass.selectedItem.toString()
                Log.d("classroom", classroom!!)

            }
        }
        spSubject?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onNothingSelected(parent: AdapterView<*>?) {
                println("Nothing Selected")
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                subject = spSubject.selectedItem.toString()
                Log.d("subject", subject!!)
            }
        }

        spTimeSlot?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onNothingSelected(parent: AdapterView<*>?) {
                println("Nothing Selected")
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                timeslot = spTimeSlot.selectedItem.toString()
                Log.d("timeslot", timeslot!!)
            }
        }

        buttonRead.setOnClickListener{
            pbLoading.visibility = View.VISIBLE
            val day=Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            readSchedule(day)
            timeStamp = getDateTime()
            timeStamp = getTimeRange(timeStamp!!)
            Log.d("ScheduleB",schedule.toString())
            if(schedule != null){
                // Find the start index and end index of the data for the "Xam/pm - Yam/pm" time slot
                val startIndex = schedule?.indexOf("$timeStamp=[{")!!
                val endIndex = schedule?.indexOf("}]", startIndex)?.plus(2)!!
                Log.d("ScheduleM",schedule.toString())
                Log.d("ScheduleM",startIndex.toString())
                Log.d("ScheduleM",endIndex.toString())

                if(startIndex>0 && endIndex>0){
                    // Extract the data for the "Xam/pm - Yam/pm" time slot
                    val slotData = schedule?.substring(startIndex, endIndex)!!
                    Log.d("ScheduleA",schedule.toString())
                    // Extract the department, section, semester, and subject from the slot data
                    val department = slotData.substringAfter("department=").substringBefore(",")
                    val section = slotData.substringAfter("section=").substringBefore(",")
                    val semester = slotData.substringAfter("semester=").substringBefore(",")
                    val subject = slotData.substringAfter("subject=").substringBefore("}]")
                    Log.d("Department", department)
                    Log.d("Section", section)
                    Log.d("Semester",semester)
                    Log.d("Subject",subject)
                    Log.d("Schedule",schedule.toString())
                    Log.d("timeStamp",timeStamp.toString())

                    val index1 = currentDepartAdap?.getPosition(department) // get the index of item1 in adapter1
                    if (index1 != -1) { // check if the item exists in the adapter
                        spDept.setSelection(index1!!) // set the selection of spinner1 to item1
                    }
                    Handler(Looper.getMainLooper()).postDelayed({ //Do something after 100ms
                        val index2 = currentSemtAdap?.getPosition(semester)
                        if (index2 != -1) {
                            spSemester.setSelection(index2!!)
                        }
                        Handler(Looper.getMainLooper()).postDelayed({ //Do something after 100ms
                            val index3 = currentSecAdap?.getPosition(section)
                            if (index3 != -1) {
                                spClass.setSelection(index3!!)
                            }
                            val index4 = currentSubAdap?.getPosition(subject)
                            if (index4 != -1) {
                                spSubject.setSelection(index4!!)
                            }
                            val index5 = currentTimeAdap?.getPosition(timeStamp)
                            if (index5 != -1) {
                                spTimeSlot.setSelection(index5!!)
                            }
                            pbLoading.visibility = View.INVISIBLE
                        }, 1000)
                    }, 1000)

                }
                else{
                    Toast.makeText(this, "No Schedule Found", Toast.LENGTH_SHORT).show()
                    pbLoading.visibility = View.INVISIBLE
                }
            }
            else{
                pbLoading.visibility = View.INVISIBLE
            }

        }
        btnExport?.setOnClickListener {
            val string = "Department : $department\nSemester : $semester\nSection: $classroom \nSubject: $subject \nTimeSlot: $timeslot "
            showConfirmationDialog(this, string)
        }

        btnOpenFile.setOnClickListener {
            val file = File(exportDir, "$classroom $subject Attendance.xls")
            val uri = FileProvider.getUriForFile(this, "${this.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/*")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val activities: List<ResolveInfo> = this.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            if (activities.isNotEmpty()) {
                val chooserIntent = Intent.createChooser(intent, "Open File With")
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                this.startActivity(chooserIntent)
            } else {
                Toast.makeText(this, "No app found to open the file", Toast.LENGTH_SHORT).show()
            }
        }
        btnBack.setOnClickListener {
            finish()
        }

        btnAddDepart.setOnClickListener {
            showNewInputDialog(spDept,"Department")
        }
        btnAddSem.setOnClickListener {
            showNewInputDialog(spSemester,"Semester")
        }
        btnAddSec.setOnClickListener {
            showNewInputDialog(spClass,"Section")
        }
        btnAddSub.setOnClickListener {
            showNewInputDialog(spSubject,"Subject")
        }
        btnAddTime.setOnClickListener {
            showNewInputDialog(spTimeSlot,"TimeSlot")
        }
    }
    private fun beginExport(){
        btnExportXLS.isEnabled = false
        timeStamp = getDateTime()
        exportDir = File("$dir/$subject/$classroom/Semester $semester")
        exportDir.mkdirs()
        Log.d("export", exportDir.toString())
        exportFile = File(exportDir, "$classroom $subject Attendance.xls")
        listExternalStorage()
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ //Do something after 100ms
            buttonCreateExcel()
        }, 1000)
    }


    private fun showConfirmationDialog(context: Context, value: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirm Your Choices :-")
        builder.setMessage(value)
        builder.setPositiveButton("Confirm") { _, _ ->
            beginExport()
            pbLoading.visibility = View.VISIBLE
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }
    private fun showNewInputDialog(spinner: Spinner,Title: String) {
        // Inflate the dialog_input.xml layout
        val view: View = LayoutInflater.from(this).inflate(R.layout.exportdialog_input, null)

        // Get the EditText view from the layout
        val editText = view.findViewById<EditText>(R.id.edit_text_input)

        // Create the AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle(Title)
        builder.setView(view)
        builder.setCancelable(false)
        builder.setPositiveButton("Confirm", null)
        builder.setNegativeButton("Cancel", null)

        // Show the AlertDialog
        val alertDialog = builder.create()
        alertDialog.show()

        // Set a click listener on the confirm button to check if the input field is empty
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            // Check if the input field is empty
            if (TextUtils.isEmpty(editText.text)) {
                editText.error = "Input field can't be empty!"
            } else {
                // Input field is not empty, dismiss the dialog
                alertDialog.dismiss()
                // Do something with the input value
                val input = editText.text.toString()
                Log.d("Input",input)
                // ...
                val inputArrayList = arrayListOf(input)
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, inputArrayList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter =adapter
            }
        }
    }
    private fun getTimeRange(timeStamp :String): String {
        val startTimeStrings = arrayOf("09.00", "10.11", "11.11", "12.11", "02.11", "03.11", "04.11")
        val endTimeStrings = arrayOf("10.10", "11.10", "12.10", "01.10", "03.10", "04.10", "05.10")
        val timeRangeStrings = arrayOf("9am-10am", "10am-11am", "11am-12noon", "12noon-1pm", "2pm-3pm", "3pm-4pm", "4pm-5pm")
        var timeRangeString = ""
        for (i in startTimeStrings.indices) {
            val startTime = startTimeStrings[i].toFloat()
            val endTime = endTimeStrings[i].toFloat()
            val currentTime = timeStamp.toFloat()

            if (currentTime in startTime..endTime) {
                timeRangeString = timeRangeStrings[i]
                break
            }
        }

        if (timeRangeString.isEmpty()) {
            timeRangeString = "Unknown"
        }
        return timeRangeString
    }
    private fun exportFile() {

//        val file = File(exportDir, "Attendance.xls")
        val fileInputStream = FileInputStream(exportFile)
        val workbook = HSSFWorkbook(fileInputStream)

        // Get the sheet you want to modify
        val sheet = workbook.getSheetAt(0)
        val row = sheet.getRow(0)
        Log.d("match", (row.lastCellNum.toString()))
        val colIndex = row.lastCellNum.toInt()
        val date = dateFormat.format(currentDate)
        val cell2 = row.createCell(colIndex)
        cell2.setCellValue(date.toString())
        try{
            for (rowIndex in sheet.firstRowNum.. sheet.lastRowNum) {
                val row1 = sheet.getRow(rowIndex)
                var name = row1.getCell(0).stringCellValue.toString()

                if(name.length > 6){
                    name = name.substring(6)
                }
                Log.d("excel",name)
                if (name in attendance) {
                    row1.createCell(colIndex).setCellValue("Present")
                }
            }
        }catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        Toast.makeText(this, "Changes saved to file.", Toast.LENGTH_SHORT).show()
//        exportFile = File(exportDir, "Attendance.xls")
        try {
            if (!exportFile?.exists()!!) {
                exportFile?.createNewFile()
            }
            val fileOutputStream = FileOutputStream(exportFile)
            workbook.write(fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
            Toast.makeText(this, "Successfully Exported Attendance Sheet", Toast.LENGTH_SHORT)
                .show()
            btnExportXLS.isEnabled = true
            attendance.clear()
            btnOpenFile.visibility = View.VISIBLE
            btnExportXLS.visibility = View.INVISIBLE
            pbLoading.visibility = View.INVISIBLE
            val dir = File(Environment.getExternalStorageDirectory().toString() + "/Attendance/Imported")
            Log.d("dir", dir.toString())
            if (dir.isDirectory) {
                val children = dir.list()
                for (i in children!!.indices) {
                    File(dir, children[i]).delete()
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            attendance.clear()
            pbLoading.visibility = View.INVISIBLE
        }
        println("Excel sheet created successfully.")
    }

    private fun listExternalStorage(){
        val state = Environment.getExternalStorageState()
        val root = Environment.getExternalStorageDirectory().absolutePath
        val exportDir = File("$root/Attendance/Imported")

        if (Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state) {
            this.disposable = Observable.fromPublisher(FileLister(exportDir))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
//                    txtFiles.append(it + "\n")
                    Log.d("array", it)
                    Log.d("array1",it.replace("/storage/emulated/0/Attendance/Imported/","").replace(".txt","").substring(6))
                    attendance.add(it.replace("/storage/emulated/0/Attendance/Imported/","").replace(".txt","").substring(6))

                }, {
                    Log.e("array", "Error in listing files from the SD card", it)
                }, {
                    Toast.makeText(this, "Adding Files to EXCEL SHEET...", Toast.LENGTH_SHORT)
                        .show()
                    this.disposable?.dispose()
                    this.disposable = null
                })
        }
    }

    private class FileLister(val directory: File) : Publisher<String> {
        private lateinit var subscriber: Subscriber<in String>
        override fun subscribe(s: Subscriber<in String>?) {
            if (s == null) {
                return
            }
            this.subscriber = s
            this.listFiles(this.directory)
            this.subscriber.onComplete()
        }
        private fun listFiles(directory: File) {
            val files = directory.listFiles()
            if (files != null) {
                Log.d("this is my array", "arr: " + Arrays.toString(files).replace("/storage/emulated/0/Attendance/Imported/","").replace(".txt",""))
//                attendance = Arrays.toString(files).replace("/storage/emulated/0/Attendance/Imported/","").replace(".txt",""))
                for (file in files) {
                    if (file != null) {
                        if (file.isDirectory) {
                            listFiles(file)
                        } else {
                            subscriber.onNext(file.absolutePath)
                        }
                    }
                }
            }
        }
    }

    private fun fileExist(): Boolean {
        val file = File(exportDir, "$classroom $subject Attendance.xls")
        Log.d("FIle",file.toString())
        return file.exists()
    }
    private fun buttonCreateExcel() {
        //        attendance.sort()
        attendance.sortWith(compareBy {
            it.substring(it.length - 3).toInt()
        })

        if(fileExist()){
            Log.d("File","Exist")
            exportFile()
        }else{
            Log.d("File","Doesn't Exist")
            val workbook = HSSFWorkbook()
            val sheet = workbook.createSheet("Sheet1")
            val data = mutableListOf("")
            for (i in 1..99) {
                val num = if (i < 10) "0$i" else i.toString()
                data.add("xxxxxx0$num")
            }

            val row = sheet.createRow(0)
            val cell1 = row.createCell(0)
            cell1.setCellValue("Scholar No.")

            for (i in data.indices) {
                val row2=sheet.createRow(i+1)
                val cell2 = row2.createCell(0)
                cell2.setCellValue(data[i])
            }
            try {
                if (!exportFile?.exists()!!) {
                    exportFile?.createNewFile()
                }
                val fileOutputStream = FileOutputStream(exportFile)
                workbook.write(fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                pbLoading.visibility = View.INVISIBLE
            }
            exportFile()
        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun getDateTime(): String? {
        return SimpleDateFormat("hh.mm").format(
            Date()
        )
    }



    private fun readSchedule(day: Int) {
        var fileName = "Monday.txt"
        when (day) {
            Calendar.SUNDAY -> {}
            Calendar.MONDAY -> {
                fileName = "Monday.txt"
            }
            Calendar.TUESDAY -> {
                fileName = "Tuesday.txt"
            }
            Calendar.WEDNESDAY -> {
                fileName = "Wednesday.txt"
            }
            Calendar.THURSDAY -> {
                fileName = "Thursday.txt"
            }
            Calendar.FRIDAY -> {
                fileName = "Friday.txt"
            }
            Calendar.SATURDAY -> {}
        }
        try {
            val fileIn = openFileInput(fileName)
            val inputRead = InputStreamReader(fileIn as InputStream)
            val inputBuffer = CharArray(100)
            var s = ""
            while (true) {
                val var7 = inputRead.read(inputBuffer)
                if (var7 <= 0) {
                    inputRead.close()
                    break
                }
                val var8 = 0
//                val readstring: String = String(inputBuffer, var8, var7)
                val readstring = String(inputBuffer,var8, var7)
                s = Intrinsics.stringPlus(s, readstring)
            }
            schedule = s
            Log.d("Schedule Found Full", schedule!!)
        } catch (var10: java.lang.Exception) {
            var10.printStackTrace()
            Toast.makeText(this, "No Schedule File Found", Toast.LENGTH_SHORT).show()
        }


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
        spSubject.adapter = currentSubject
        currentSubAdap = currentSubject
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
        spSubject.adapter = currentSubject
        currentSubAdap = currentSubject
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
        spSubject.adapter = currentSubject
        currentSubAdap = currentSubject
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
        spSubject.adapter = currentSubject
        currentSubAdap = currentSubject
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
        spSubject.adapter = currentSubject
        currentSubAdap = currentSubject
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
        spSubject.adapter = currentSubject
        currentSubAdap = currentSubject
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
        spSubject.adapter = currentSubject
        currentSubAdap = currentSubject
        return currentSubject as ArrayAdapter<CharSequence>
    }

    //    private fun validateSubject() : Boolean{
//        val sub = etSubject.text.toString().trim()
////        val emailRegex : Regex = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$".toRegex()
//        if (sub.isEmpty()){
//            tiSubject.error = "Enter Subject"
//            return false
//        }
////        else if(!sub.matches(emailRegex)){
////            tiSubject.error = "Invalid Subject"
////            return false
////        }
//        else
//        {
//            tiSubject.isErrorEnabled = false
//            tiSubject.error=null
//            return true
//        }
//    }
//    private fun validateClass() : Boolean{
//        val classroom = etClass.text.toString().trim()
////        val emailRegex : Regex = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$".toRegex()
//        if (classroom.isEmpty()){
//            tiClass.error = "Enter Classroom"
//            return false
//        }
////        else if(!sub.matches(emailRegex)){
////            tiSubject.error = "Invalid Subject"
////            return false
////        }
//        else
//        {
//            tiClass.isErrorEnabled = false
//            tiClass.error=null
//            return true
//        }
//    }
//    private fun validateTimeSlot() : Boolean{
//        val timeslot = etTimeSlot.text.toString().trim()
////        val emailRegex : Regex = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$".toRegex()
//        if (timeslot.isEmpty()){
//            etTimeSlot.error = "Enter TimeSlot"
//            return false
//        }
////        else if(!sub.matches(emailRegex)){
////            tiSubject.error = "Invalid Subject"
////            return false
////        }
//        else
//        {
//            tiTimeSlot.isErrorEnabled = false
//            tiTimeSlot.error=null
//            return true
//        }
//    }
    //        btnDelete.setOnClickListener{
//            val dir = File(Environment.getExternalStorageDirectory().toString() + "/Attendance/Imported")
//            Log.d("dir", dir.toString())
//            if (dir.isDirectory) {
//                val children = dir.list()
//                for (i in children.indices) {
//                    File(dir, children[i]).delete()
//                }
//            }
//        }
//        btnImport?.setOnClickListener {
//            listExternalStorage()
//        }
}
