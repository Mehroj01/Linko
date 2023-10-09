package com.neb.linko.ui.home.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.neb.linko.models.BannerModel
import com.neb.linko.models.StoreModel
import com.neb.linko.ui.home.CarouselPageFragment

class CarouselAdapter(var currentsCodeList: ArrayList<StoreModel>, fragmentManager: FragmentManager) :
    FragmentPagerAdapter(
        fragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
    override fun getCount(): Int = currentsCodeList.size

    override fun getItem(position: Int): Fragment {
        return CarouselPageFragment.newInstance(currentsCodeList[position])
    }

    override fun getPageTitle(position: Int): CharSequence {
        return ""
    }
}