package com.neb.linko.businessUi.analytics

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.neb.linko.App
import com.neb.linko.databinding.ActivityAnalyticsBinding
import com.neb.linko.models.StoreModel
import com.neb.linko.models.User
import com.neb.linko.ui.home.adapters.CarouselAdapter
import javax.inject.Inject

class AnalyticsActivity : AppCompatActivity() {

    @Inject
    lateinit var analyticsViewModel: AnalyticsViewModel

    lateinit var analyticsBinding: ActivityAnalyticsBinding
    var filter = "Today"
    var user: User? = null
    lateinit var carouselAdapter: CarouselAdapter
    lateinit var carouselBannerList: ArrayList<StoreModel>
    var bannerCarouselSituation = false
    lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsBinding = ActivityAnalyticsBinding.inflate(layoutInflater)
        setContentView(analyticsBinding.root)
        App.appComponent.analyticsActivity(this)
        carouselBannerList = ArrayList()
        loadDate()

        analyticsBinding.backBtn.setOnClickListener {
            finish()
        }

        carouselAdapter = CarouselAdapter(carouselBannerList, supportFragmentManager)
        analyticsBinding.imageSlider.adapter = carouselAdapter
        analyticsBinding.tabs.setupWithViewPager(analyticsBinding.imageSlider)

        analyticsViewModel.getBanners().observe(this, Observer {
            if (it != null && it.size > 0) {
                carouselBannerList.clear()
                carouselBannerList.addAll(it)
                carouselAdapter.notifyDataSetChanged()
                if (!bannerCarouselSituation) {
                    handler = Handler()
                    handler.postDelayed(run, 5000)
                    bannerCarouselSituation = true
                }
            } else {
                carouselBannerList.clear()
                analyticsBinding.pagerContainer.removeView(analyticsBinding.imageSlider)
                analyticsBinding.pagerContainer.removeView(analyticsBinding.tabs)
//                val params = homeBinding.mainToolBar.layoutParams
//                params.height = dpToPx(120, requireContext())
//                homeBinding.mainToolBar.layoutParams = params
            }
        })

        analyticsBinding.filtersBtn.setOnClickListener {

            val baseItems =
                arrayOf<CharSequence>("Today", "Yesterday", "This Month", "This Year", "Overall")
            val items =
                arrayOf<CharSequence>("Today", "Yesterday", "This Month", "This Year", "Overall")

            AlertDialog.Builder(this)
                .setTitle("Select an option")
                .setItems(items) { dialog, which ->
                    analyticsBinding.filtersBtn.text = items[which] as String
                    filter = baseItems[which] as String
                    if (user != null) {
                        analyticsBinding.progress.visibility = View.VISIBLE
                        getStats(filter)
                    } else {
                        loadDate()
                    }
                }
                .show()
        }

    }

    private fun getStats(filter: String) {
        analyticsViewModel.getState(filter,this).observe(this, Observer {
            it?.let { stats ->
                analyticsBinding.whatsappTv.text = "${stats.whatsappClicks}"
                analyticsBinding.locationTv.text = "${stats.locationClicks}"
                analyticsBinding.callTv.text = "${stats.phoneClicks}"
                analyticsBinding.openTv.text = "${stats.websiteClicks}"
                analyticsBinding.saveTv.text = "${stats.bookmarkClicks}"
                analyticsBinding.viewTv.text = "${stats.views}"
            }
            analyticsBinding.progress.visibility = View.INVISIBLE
        })
    }

    private fun loadDate() {
        getStats(filter)
    }

    var run = object : Runnable {
        override fun run() {
            if (carouselBannerList.size - 1 != analyticsBinding.imageSlider.currentItem) {
                analyticsBinding.imageSlider.currentItem =
                    analyticsBinding.imageSlider.currentItem + 1
            } else {
                analyticsBinding.imageSlider.currentItem = 0
            }
            handler.postDelayed(this, 5000)
        }
    }
}