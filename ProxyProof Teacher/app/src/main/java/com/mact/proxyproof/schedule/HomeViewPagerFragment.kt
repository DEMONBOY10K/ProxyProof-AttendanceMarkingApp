package com.mact.proxyproof.schedule

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mact.proxyproof.R
import com.mact.proxyproof.databinding.FragmentHomeViewPagerBinding
import com.mact.proxyproof.schedule.adapter.CourseViewerPagerAdapter
import com.mact.proxyproof.schedule.data.Course3
import com.mact.proxyproof.schedule.ui.preferences.*
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val FAB_ACTION_DEFAULT = "-1"
private const val FAB_ACTION_OPEN_MAP = "1"
private const val FAB_ACTION_OPEN_CALENDAR = "2"
private const val THREE_MINUTES_IN_MILLIS = 180000

/**
 * A simple [Fragment] subclass.
 * Use the [HomeViewPagerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeViewPagerFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentHomeViewPagerBinding

    private lateinit var sharedPref: SharedPreferences
    private lateinit var prefTableTitle: String
    private var prefWeekendCol = false
    private var prefWeekdayLengthLong = false
    val mondayMap = mutableMapOf<String, MutableList<Map<String, String>>>()
    val tuesdayMap = mutableMapOf<String, MutableList<Map<String, String>>>()
    val wednesdayMap = mutableMapOf<String, MutableList<Map<String, String>>>()
    val thursdayMap = mutableMapOf<String, MutableList<Map<String, String>>>()
    val fridayMap = mutableMapOf<String, MutableList<Map<String, String>>>()

    private lateinit var database : DatabaseReference
    private lateinit var user : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeViewPagerBinding.inflate(inflater, container, false)

        setupToolBar()

        // Listen for preference change.
        sharedPref.registerOnSharedPreferenceChangeListener(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getPrefValue()
        updateFabActionAndImage()
        updateToolbarTitle()
        setupTabLayoutAndViewPager()
        openTodayTimetable()
    }

    override fun onResume() {
        super.onResume()
        openTodayAfterIdleFor3Minutes()
    }

    override fun onPause() {
        super.onPause()
        saveLastTimeUsedTimestamp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sharedPref.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun setupToolBar() {
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarHomeFrag)

        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menuAdd -> {
                        val a =
                                HomeViewPagerFragmentDirections.actionHomeViewPagerFragmentToCourseEditorFragment(
                                        currentViewPagerItem = binding.viewPagerHomeFrag.currentItem
                                )
                        findNavController().navigate(a)
                        true
                    }
//                    R.id.menuMap -> {
//                        openMapsViewer()
//                        true
//                    }
//                    R.id.menuCalendar -> {
//                        openCalendar()
//                        true
//                    }
//                    R.id.menuSettings -> {
//                        val a =
//                                HomeViewPagerFragmentDirections.actionHomeViewPagerFragmentToPreferenceActivity()
//                        findNavController().navigate(a)
//                        true
//                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }

    /**
     * Detect preference change to update UI. Called everytime when shared preference changed.
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // Get updated preference value
        getPrefValue()
        when (key) {
            PREFERENCE_FAB_ACTION -> {
                updateFabActionAndImage()
            }
            PREFERENCE_TABLE_TITLE -> {
                updateToolbarTitle()
            }
            PREFERENCE_WEEKEND_COL, PREFERENCE_WEEKDAY_LENGTH_LONG -> {
                setupTabLayoutAndViewPager()
                openTodayTimetable()
            }
        }
    }

    /**
     * Get the latest preference value.
     * */
    private fun getPrefValue() {
        prefTableTitle = sharedPref.getString(
            PREFERENCE_TABLE_TITLE,
            getString(R.string.settings_timetableTitleDefaultValue)
        )!!
        prefWeekendCol = sharedPref.getBoolean(PREFERENCE_WEEKEND_COL, false)
        prefWeekdayLengthLong = sharedPref.getBoolean(PREFERENCE_WEEKDAY_LENGTH_LONG, false)
    }

    /**
     * Setting up fab, one thing it does very well is to close your app.
     */
    private fun updateFabActionAndImage() {
        val userFabAction = sharedPref.getString(PREFERENCE_FAB_ACTION, FAB_ACTION_DEFAULT)

        // Set fab drawable
        val drawable = when (userFabAction) {
            FAB_ACTION_DEFAULT -> R.drawable.ic_exit_24dp
            FAB_ACTION_OPEN_MAP -> R.drawable.ic_map_24dp
            FAB_ACTION_OPEN_CALENDAR -> R.drawable.ic_event_note_24dp
            else -> R.drawable.ic_exit_24dp
        }
        binding.fabHomeFrag.setImageResource(drawable)

        // Set fab onClick action
        binding.fabHomeFrag.setOnClickListener {
            when (userFabAction) {
                FAB_ACTION_DEFAULT -> requireActivity().finish()
//                FAB_ACTION_OPEN_MAP -> openMapsViewer()
//                FAB_ACTION_OPEN_CALENDAR -> openCalendar()
            }
        }
        binding.fabSaveLocal.setOnClickListener {
            Log.d("user", "Click")
            createTxt()
        }
    }
private fun createTxt(){
    user = FirebaseAuth.getInstance()
    val email = user.currentUser?.email!!
    val userName = emailToUserName(email)
    val url = getString(R.string.firebase_db_location)
    mondayMap.clear()
    tuesdayMap.clear()
    wednesdayMap.clear()
    thursdayMap.clear()
    fridayMap.clear()
    database = FirebaseDatabase.getInstance(url).getReference("teachers")


    var schedule :DataSnapshot
    context?.let { context->
        database.child(userName).get().addOnSuccessListener {
            if(it.exists()) {
                 schedule = it.child("Schedule")
                Log.d("user", schedule.toString())
                Handler().postDelayed({
                    for (childSnapshot in schedule.children) {
                        val dayMap = mutableMapOf<String, MutableList<Map<String, String>>>()
                        for (timeSlotSnapshot in childSnapshot.children) {
                            val timeSlotMap = mutableMapOf<String, String>()
                            for (slotDataSnapshot in timeSlotSnapshot.children) {
                                timeSlotMap[slotDataSnapshot.key as String] = slotDataSnapshot.value as String
                            }
                            val timeSlotId = timeSlotSnapshot.key as String
                            val timeSlotList = dayMap.getOrDefault(timeSlotId, mutableListOf())
                            timeSlotList.add(timeSlotMap)
                            dayMap[timeSlotId] = timeSlotList
                        }
                        when (childSnapshot.key) {
                            "Monday" -> mondayMap.putAll(dayMap)
                            "Tuesday" -> tuesdayMap.putAll(dayMap)
                            "Wednesday" -> wednesdayMap.putAll(dayMap)
                            "Thursday" -> thursdayMap.putAll(dayMap)
                            "Friday" -> fridayMap.putAll(dayMap)
                        }
                    }
                    Log.d("Data","Mon = $mondayMap")
                    Log.d("Data","Tues = $tuesdayMap")
                    Log.d("Data","Wed = $wednesdayMap")
                    Log.d("Data","Thur = $thursdayMap")
                    Log.d("Data","Fri = $fridayMap")
                    Handler().postDelayed( {
                        saveTxt("Monday.txt",mondayMap.toString())
                        saveTxt("Tuesday.txt",tuesdayMap.toString())
                        saveTxt("Wednesday.txt",wednesdayMap.toString())
                        saveTxt("Thursday.txt",thursdayMap.toString())
                        saveTxt("Friday.txt",fridayMap.toString())
                        Toast.makeText(context, "Synced Local Schedule Successfully", Toast.LENGTH_SHORT).show()
                    },1500)
                }, 1500)
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
        }
    }
}
 private fun saveTxt(fileName:String,fileContents:String){
     context?.let {
         try {
             // Open the private file
             val fileOutputStream = it.openFileOutput(fileName, Context.MODE_PRIVATE)

             // Write the file contents to it
             fileOutputStream.write(fileContents.toByteArray())

             // Close the output stream
             fileOutputStream.close()


         } catch (e: Exception) {
             // Handle any errors that occur
             e.printStackTrace()
             Toast.makeText(it, "Error Syncing Local Schedule: ${e.message}", Toast.LENGTH_SHORT).show()
         }
     }
    }
    private fun emailToUserName(email : String ): String{
        var userName= email
        val regex = Regex("[^A-Za-z0-9]")
        userName = regex.replace(userName, "")
        return userName
    }
    fun syncDB(){
        val course = Course3(
            id = null,
            courseSubject = "courseSubject",
            courseSection = "courseSection",
            courseWeekday = 4,
            courseStartTime = "1200",
            courseEndTime = "1300",
            courseSemester = "courseSemester",
            courseDepartment = "courseDepartment"
        )

    }

    /**
     * Update toolbar's title.
     * */
    private fun updateToolbarTitle() {
        (activity as AppCompatActivity).supportActionBar!!.title = prefTableTitle
    }

    /**
     * Set adapter for view pager and bind tab layout to it.
     * */
    private fun setupTabLayoutAndViewPager() {
        binding.viewPagerHomeFrag.adapter = CourseViewerPagerAdapter(this, prefWeekendCol)

        // Set the icon and text for each tab
        TabLayoutMediator(binding.tabLayoutHomeFrag, binding.viewPagerHomeFrag) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()
    }

    /**
     * Get weekdays string as Mon or Monday base on user's preference.
     * */
    private fun getTabTitle(position: Int): String? {
        val array = if (prefWeekdayLengthLong) R.array.weekdayList else R.array.weekdayListShort
        return resources.getStringArray(array)[position]
    }

    /**
     * Automatically open today's timetable.
     * Get day of the week, start from SUNDAY (int == 1), then open the tab belongs today.
     * */
    private fun openTodayTimetable() {
        val dayOfWeek = Calendar.getInstance()[Calendar.DAY_OF_WEEK]
        binding.viewPagerHomeFrag.setCurrentItem(if (dayOfWeek == 1) 8 else dayOfWeek - 2, false)
    }

    /**
     * Compare last time used timestamp and current timestamp, if greater than 3 minutes,
     * open today's timetable.
     */
    private fun openTodayAfterIdleFor3Minutes() {
        val currentTimeStamp = Calendar.getInstance().timeInMillis
        val lastTimUse = sharedPref.getLong(PREFERENCE_LAST_TIME_USE, 0)
        val idleTime = currentTimeStamp - lastTimUse
        if (idleTime > THREE_MINUTES_IN_MILLIS) {
            openTodayTimetable()
        }
    }

    /**
     * Called when onPause() to record the last timestamp.
     */
    private fun saveLastTimeUsedTimestamp() {
        val currentTimeStamp = Calendar.getInstance().timeInMillis
        sharedPref.edit().putLong(PREFERENCE_LAST_TIME_USE, currentTimeStamp).apply()
    }

    companion object {
        private const val TAG = "HomeViewPagerFragment"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeViewPagerFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeViewPagerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}