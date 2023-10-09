package com.neb.linko.di.component

import com.neb.linko.SplashActivity
import com.neb.linko.businessUi.analytics.AnalyticsActivity
import com.neb.linko.businessUi.boosts.BoostsActivity
import com.neb.linko.businessUi.boosts.PaymentActivity
import com.neb.linko.businessUi.businessEditProfile.BusinessEditProfileActivity
import com.neb.linko.businessUi.businessProfile.BusinessProfileFragment
import com.neb.linko.businessUi.storeDetails.AddLocationActivity
import com.neb.linko.businessUi.storeDetails.StoreDetailsActivity
import com.neb.linko.businessUi.storeItems.StoreItemsActivity
import com.neb.linko.businessUi.storePhotos.StorePhotosActivity
import com.neb.linko.businessUi.storePromoCodes.PromoCodesActivity
import com.neb.linko.di.module.NetworkModule
import com.neb.linko.stats.StatsViewModel
import com.neb.linko.ui.category.CategoryFragment
import com.neb.linko.ui.filter.FilterViewModel
import com.neb.linko.ui.filter.ShowFilterStoresActivity
import com.neb.linko.ui.home.HomeFragment
import com.neb.linko.ui.profile.ProfileFragment
import com.neb.linko.ui.ratings.RatingActivity
import com.neb.linko.ui.registerscreens.experience.EperienceFragment
import com.neb.linko.ui.save.SaveFragment
import com.neb.linko.ui.save.SaveViewModel
import com.neb.linko.ui.search.SearchActivity
import com.neb.linko.ui.search.SearchViewModel
import com.neb.linko.ui.store.StoreActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class])
interface AppComponent {

    fun inject(splashActivity: SplashActivity)
    fun home(homeFragment: HomeFragment)
    fun save(saveFragment: SaveFragment)
    fun experience(eperienceFragment: EperienceFragment)
    fun profile(profileFragment: ProfileFragment)
    fun saveViewModel(saveViewModel: SaveViewModel)
    fun storeActivity(storeActivity: StoreActivity)
    fun ratingActivity(ratingActivity: RatingActivity)
    fun showFilterViewModel(filterViewModel: FilterViewModel)
    fun showFilterActivity(showFilterStoresActivity: ShowFilterStoresActivity)
    fun searchActivity(searchActivity: SearchActivity)
    fun searchViewModel(searchViewModel: SearchViewModel)
    fun statsViewModel(statsViewModel: StatsViewModel)
    fun analyticsActivity(analyticsActivity: AnalyticsActivity)
    fun businessProfile(businessProfileFragment: BusinessProfileFragment)
    fun addStoreLocationActivity(addLocationActivity: AddLocationActivity)
    fun storeDetailsActivity(storeDetailsActivity: StoreDetailsActivity)
    fun businessEditProfile(businessEditProfileActivity: BusinessEditProfileActivity)
    fun storePhotosActivity(storePhotosActivity: StorePhotosActivity)
    fun storeItemsActivity(storeItemsActivity: StoreItemsActivity)
    fun promoCodesActivity(promoCodesActivity: PromoCodesActivity)
    fun boostsActivity(boostsActivity: BoostsActivity)
    fun categoryFragment(categoryFragment: CategoryFragment)
    fun paymentActivity(paymentActivity: PaymentActivity)
}