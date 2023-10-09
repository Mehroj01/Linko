package com.neb.linko.businessUi.boosts

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.applandeo.materialcalendarview.EventDay
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.neb.linko.App
import com.neb.linko.R
import com.neb.linko.businessUi.boosts.adapters.BoostsAdapters
import com.neb.linko.databinding.ActivityBoostsBinding
import com.neb.linko.databinding.BoostsBottomDialogBinding
import com.neb.linko.models.BoostModel
import com.neb.linko.utils.Status
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class BoostsActivity : AppCompatActivity() {

    @Inject
    lateinit var boostsViewModel: BoostsViewModel

    lateinit var boostsBinding: ActivityBoostsBinding

    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    lateinit var boosts: ArrayList<BoostModel>

    lateinit var boostsAdapters: BoostsAdapters

    var view: BoostsBottomDialogBinding? = null

    var busyTimesList = ArrayList<String>()

    var oldUrl = ""
    var events: MutableList<EventDay> = ArrayList()

    var firstClick: Calendar? = null
    var secondClick: Calendar? = null

    var boostKey = ""

    var creationDate: String = ""

    var dates: ArrayList<String> = ArrayList()

    var top: BoostModel? = null
    var pin: BoostModel? = null

    private val TAG = "BoostsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        boostsBinding = ActivityBoostsBinding.inflate(layoutInflater)
        setContentView(boostsBinding.root)
        App.appComponent.boostsActivity(this)

        sharedPreferences = getSharedPreferences("Base", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        boostsBinding.backBtn.setOnClickListener {
            finish()
        }

        boosts = ArrayList()

        boostsAdapters = BoostsAdapters(boosts, object : BoostsAdapters.BoostsClick {
            override fun click(boostModel: BoostModel) {
                firstClick = null
                secondClick = null
                events.clear()
                showBottomSheetDialog(boostModel)
            }
        }, resources)
        boostsBinding.rv.adapter = boostsAdapters

        getBoosts()

    }

    private fun showBottomSheetDialog(boostModel: BoostModel) {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetTheme)
        view = BoostsBottomDialogBinding.inflate(LayoutInflater.from(this), null, false)
        view?.progress?.setOnClickListener {
            Toast.makeText(this, "Please wait!", Toast.LENGTH_SHORT).show()
        }
        boostKey = boostModel.key ?: ""
        when (boostModel.type) {
            2 -> {
                view?.k10?.visibility = View.VISIBLE
                view?.k50?.visibility = View.GONE
                view?.k70?.visibility = View.GONE
                view?.calendar?.visibility = View.GONE
                view?.success?.visibility = View.GONE
                view?.failed?.visibility = View.GONE
                view?.tv10k?.text = boostModel.title
            }
            1 -> {
                view?.k10?.visibility = View.GONE
                view?.k50?.visibility = View.VISIBLE
                view?.k70?.visibility = View.GONE
                view?.calendar?.visibility = View.GONE
                view?.success?.visibility = View.GONE
                view?.failed?.visibility = View.GONE
                view?.tv50k?.text = boostModel.title
            }
            0 -> {
                view?.k10?.visibility = View.GONE
                view?.k50?.visibility = View.GONE
                view?.k70?.visibility = View.VISIBLE
                view?.calendar?.visibility = View.GONE
                view?.success?.visibility = View.GONE
                view?.failed?.visibility = View.GONE
                view?.tv70k?.text = boostModel.title
                bannerBgClear()
            }
        }

        view?.datePickerBtn10k?.setOnClickListener {
            disableCalendarDays()
            createCal()
        }

        view?.datePickerBtn50k?.setOnClickListener {
            disableCalendarDays()
            createCal()
        }

        view?.datePickerBtn70k?.setOnClickListener {
            disableCalendarDays()
            createCal()
        }

        view?.finalize?.setOnClickListener {
            if (events.isNotEmpty()) {
                var format = SimpleDateFormat("dd/MM/yyyy")
                events.sortBy { t -> t.calendar.timeInMillis }
                dates.clear()
                for (e in events) {
                    dates.add("${format.format(e.calendar.timeInMillis)}")
                }
                when (boostModel.type) {
                    0 -> {
                        if (events.size == 1) {
                            view?.k70DateInfo?.text =
                                "${format.format(events.first().calendar.timeInMillis)}"
                            view?.priceInfo70k?.text = "70 KWD"
                        } else {
                            view?.k70DateInfo?.text =
                                "${format.format(events.first().calendar.timeInMillis)} - ${
                                    format.format(
                                        events.last().calendar.timeInMillis
                                    )
                                }"
                            view?.priceInfo70k?.text =
                                "${(((events.last().calendar.timeInMillis - events.first().calendar.timeInMillis) / 1000 / 60 / 60 / 24) + 1) * 70} KWD"
                        }
                        view?.k10?.visibility = View.GONE
                        view?.k50?.visibility = View.GONE
                        view?.k70?.visibility = View.VISIBLE
                        view?.calendar?.visibility = View.GONE
                        view?.success?.visibility = View.GONE
                        view?.failed?.visibility = View.GONE
                    }
                    1 -> {
                        if (events.size == 1) {
                            view?.k50DateInfo?.text =
                                "${format.format(events.first().calendar.timeInMillis)}"
                            view?.priceInfo50k?.text = "50 KWD"
                        } else {
                            view?.k50DateInfo?.text =
                                "${format.format(events.first().calendar.timeInMillis)} - ${
                                    format.format(
                                        events.last().calendar.timeInMillis
                                    )
                                }"
                            view?.priceInfo50k?.text =
                                "${(((events.last().calendar.timeInMillis - events.first().calendar.timeInMillis) / 1000 / 60 / 60 / 24) + 1) * 50} KWD"
                        }
                        view?.k10?.visibility = View.GONE
                        view?.k50?.visibility = View.VISIBLE
                        view?.k70?.visibility = View.GONE
                        view?.calendar?.visibility = View.GONE
                        view?.success?.visibility = View.GONE
                        view?.failed?.visibility = View.GONE
                    }
                    2 -> {
                        if (events.size == 1) {
                            view?.k10DateInfo?.text =
                                "${format.format(events.first().calendar.timeInMillis)}"
                            view?.priceInfo10k?.text = "10 KWD"
                        } else {
                            view?.k10DateInfo?.text =
                                "${format.format(events.first().calendar.timeInMillis)} - ${
                                    format.format(
                                        events.last().calendar.timeInMillis
                                    )
                                }"
                            view?.priceInfo10k?.text =
                                "${(((events.last().calendar.timeInMillis - events.first().calendar.timeInMillis) / 1000 / 60 / 60 / 24) + 1) * 10} KWD"
                        }
                        view?.k10?.visibility = View.VISIBLE
                        view?.k50?.visibility = View.GONE
                        view?.k70?.visibility = View.GONE
                        view?.calendar?.visibility = View.GONE
                        view?.success?.visibility = View.GONE
                        view?.failed?.visibility = View.GONE
                    }
                }
            }
        }

        view?.saveBtn10k?.setOnClickListener {
            val d = view?.k10DateInfo?.text.toString()
            Log.d(TAG, "showBottomSheetDialog: ${dates}")
            if (d != "dd/mm/yyy") {
                sendRequest(
                    boostModel.key ?: "",
                    d,
                    "",
                    dates,
                    (dates.size * 10)
                )
            }
        }

        view?.saveBtn50k?.setOnClickListener {
            val d = view?.k50DateInfo?.text.toString()
            if (d != "dd/mm/yyy") {
                sendRequest(
                    boostModel.key ?: "",
                    d,
                    "",
                    dates,
                    (dates.size * 50)
                )
            }
        }

        view?.saveBtn70k?.setOnClickListener {
            val d = view?.k70DateInfo?.text.toString()
            var bannerBg = sharedPreferences.getString("BannerUri", "")
            if (bannerBg != "") {
                if (d != "dd/mm/yyy") {
                    sendRequest(
                        boostModel.key ?: "",
                        d,
                        bannerBg ?: "",
                        dates,
                        (dates.size * 70)
                    )
                }
            } else {
                var intent = Intent(this, BannerImgActivity::class.java)
                startActivity(intent)
            }
        }

        view?.closeBtn10k?.setOnClickListener {
            dialog.onBackPressed()
        }
        view?.closeBtn50k?.setOnClickListener {
            dialog.onBackPressed()
        }
        view?.closeBtn70k?.setOnClickListener {
            dialog.onBackPressed()
            bannerBgClear()
        }

        view?.closeBtn?.setOnClickListener {
            boostKey = ""
            creationDate = ""
            dialog.dismiss()
        }
        view?.closeBtnFailed?.setOnClickListener {
            boostKey = ""
            creationDate = ""
            dialog.dismiss()
        }

        view?.closeBtnCalendarK?.setOnClickListener {
            when (boostModel.type) {
                2 -> {
                    view?.k10?.visibility = View.VISIBLE
                    view?.k50?.visibility = View.GONE
                    view?.k70?.visibility = View.GONE
                    view?.calendar?.visibility = View.GONE
                    view?.success?.visibility = View.GONE
                    view?.failed?.visibility = View.GONE
                    view?.k10DateInfo?.text = "dd/mm/yyy"
                }
                1 -> {
                    view?.k10?.visibility = View.GONE
                    view?.k50?.visibility = View.VISIBLE
                    view?.k70?.visibility = View.GONE
                    view?.calendar?.visibility = View.GONE
                    view?.success?.visibility = View.GONE
                    view?.failed?.visibility = View.GONE
                    view?.k50DateInfo?.text = "dd/mm/yyy"
                }
                0 -> {
                    view?.k10?.visibility = View.GONE
                    view?.k50?.visibility = View.GONE
                    view?.k70?.visibility = View.VISIBLE
                    view?.calendar?.visibility = View.GONE
                    view?.success?.visibility = View.GONE
                    view?.failed?.visibility = View.GONE
                    view?.k70DateInfo?.text = "dd/mm/yyy"
                }
            }
        }

        dialog.setContentView(view?.root!!)
        dialog.show()

        detectDate(boostModel)
    }

    private fun disableCalendarDays() {
        val calendars: ArrayList<Calendar> = ArrayList()
        for (i in 0 until busyTimesList.size) {
            val list = busyTimesList[i].split('/')
            val calendar1 = Calendar.getInstance()
            try {
                calendar1.set(list[2].toInt(), list[1].toInt() - 1, list[0].toInt())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            calendars.add(calendar1)
        }
        view?.calendarView?.setDisabledDays(calendars)
    }

    private fun sendRequest(
        key: String,
        date: String,
        bannerBg: String,
        dates: ArrayList<String>,
        price: Int
    ) {
        view?.progress?.visibility = View.VISIBLE
        boostsViewModel.sentBoostsRequest(this, key, date, bannerBg, dates, price)
            .observe(this) {
                when (it.status) {
                    Status.ERROR -> {
                        view?.progress?.visibility = View.INVISIBLE
                        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                    }

                    Status.LOADING -> {
                    }

                    Status.SUCCESS -> {
                        if (it.data != null) {
                            if (oldUrl != it.data.data) {
                                view?.progress?.visibility = View.INVISIBLE
                                oldUrl = it.data.data
                                openWebView(oldUrl)
                                creationDate = date
                                bannerBgClear()
                            }
                        } else {
                            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                            view?.progress?.visibility = View.INVISIBLE
                        }
                    }
                }
            }

    }

    private fun bannerBgClear() {
        editor.putString("BannerUri", "")
        editor.commit()
    }

    private fun openWebView(oldUrl: String) {
        val intent = Intent(this, PaymentActivity::class.java)
        intent.putExtra("payUrl", oldUrl)
        startActivity(intent)
    }

    private fun getBoosts() {
        boostsBinding.progress.visibility = View.VISIBLE
        boostsViewModel.getBoosts().observe(this, Observer {
            if (it != null) {
                boosts.clear()
                boosts.addAll(it)
                boosts.sortBy { l -> l.type }
                boostsAdapters.notifyDataSetChanged()
                for (b in boosts) if (b.type == 2) top = b else if (b.type == 1) pin = b
            }
            boostsBinding.progress.visibility = View.INVISIBLE
        })
    }

    private fun detectDate(boostModel: BoostModel) {
        busyTimesList.clear()
        view?.progress?.visibility = View.VISIBLE
        boostsViewModel.getBoostTimes(boostModel) {
            if (it != null) {
                busyTimesList.addAll(it)
            } else {
                Toast.makeText(this, "busy day data did not arrive", Toast.LENGTH_SHORT).show()
            }
            view?.progress?.visibility = View.INVISIBLE
        }
    }

    fun createCal() {
        view?.k10?.visibility = View.GONE
        view?.k50?.visibility = View.GONE
        view?.k70?.visibility = View.GONE
        view?.calendar?.visibility = View.VISIBLE
        view?.success?.visibility = View.GONE
        view?.failed?.visibility = View.GONE

        firstClick = null
        secondClick = null

        view?.calendarView?.setMinimumDate(Calendar.getInstance())

        view?.calendarView?.setOnDayClickListener { eventDay ->
            cal(eventDay)
        }
    }

    fun cal(eventDay: EventDay) {
        val clickedDayCalendar = eventDay.calendar
        if (firstClick != null) {
            secondClick = clickedDayCalendar
            events.clear()

            var day1 = firstClick?.get(Calendar.DAY_OF_MONTH)
            var day2 = secondClick?.get(Calendar.DAY_OF_MONTH)
            var month1 = firstClick?.get(Calendar.MONTH)
            var month2 = secondClick?.get(Calendar.MONTH)
            var year1 = firstClick?.get(Calendar.YEAR)
            var year2 = secondClick?.get(Calendar.YEAR)

            firstClick = null
            secondClick = null

            if (month1 == month2 && year1 == year2) {
                if (day2!! - day1!! > 6) {
                    day2 = day1 + 6
                }

                for (i in day1..day2) {
                    val calendar = Calendar.getInstance()
                    calendar.set(year1!!, month1!!, i)
                    val testDay = if (i < 10) "0$i" else "$i"
                    val testMonth = if (month1 + 1 < 10) "0${month1 + 1}" else "${month1 + 1}"
                    if (busyTimesList.contains("$testDay/${testMonth}/$year1")) {
                        firstClick = null
                        secondClick = null
                        break
                    }
                    events.add(EventDay(calendar, R.drawable.ic_point))
                }
            }
            view?.calendarView?.setEvents(events)
        } else {
            val d = clickedDayCalendar?.get(Calendar.DAY_OF_MONTH)
            val m = clickedDayCalendar?.get(Calendar.MONTH)
            var y = clickedDayCalendar?.get(Calendar.YEAR)
            val testDay = if (d!! < 10) "0$d" else "$d"
            val testMonth = if (m!! + 1 < 10) "0${m + 1}" else "${m + 1}"
            if (busyTimesList.contains("$testDay/${testMonth}/$y")) {
                firstClick = null
                secondClick = null
            } else {
                if (clickedDayCalendar.timeInMillis > Date().time) {
                    firstClick = clickedDayCalendar
                    events.clear()
                    events.add(EventDay(eventDay.calendar, R.drawable.ic_point))
                    view?.calendarView?.setEvents(events)
                }
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        view?.progress?.visibility = View.GONE
        if (boostsViewModel.situation != null) {
            if (boostsViewModel.situation!!) {
                view?.k10?.visibility = View.GONE
                view?.k50?.visibility = View.GONE
                view?.k70?.visibility = View.GONE
                view?.calendar?.visibility = View.GONE
                view?.success?.visibility = View.GONE
                view?.failed?.visibility = View.VISIBLE
            } else {
                view?.k10?.visibility = View.GONE
                view?.k50?.visibility = View.GONE
                view?.k70?.visibility = View.GONE
                view?.calendar?.visibility = View.GONE
                view?.success?.visibility = View.VISIBLE
                view?.failed?.visibility = View.GONE
            }
            boostsViewModel.situation = null
        }
    }
}