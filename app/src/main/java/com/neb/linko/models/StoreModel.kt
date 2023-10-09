package com.neb.linko.models

import java.io.Serializable

class StoreModel:Serializable {

    var imageUrl: String? = null
    var bgImageUrl: String? = null
    var featuredUrl: String? = null

    var subCategoryId: String? = null
    var categoryId: String? = null
    var webUrl: String? = null
    var currency: String? = null
    var instagram: String? = null

    var supportNumber: String? = null

    var name: String? = null
    var nameArabic: String? = null

    var brands: ArrayList<String>? = null
    var brandArabic: ArrayList<String>? = null
    var filters: ArrayList<String>? = null
    var filtersArabic: ArrayList<String>? = null
    var filterData: ArrayList<String>? = null

    var imagesList: ArrayList<String>? = null

    var details: String? = null
    var detailsArabic: String? = null

    var keywords: ArrayList<String>? = null

    var storeLocations: ArrayList<StoreLocation>? = null
    var email: String? = null
    var whatsapp: String? = null
    var phone: String? = null
    var phoneSec: String? = null
    var country: String? = null

    var rating: Float = 0f
    var ordersCount: Int? = null
    var trendingCount: Int? = null
    var savedSortOrder: Int? = null
    var trendingSortOrder: Int? = null
    var explorerSortOrder: Int? = null
    var shouldSave:Boolean?=null

    var sortOrder: Int? = null

    var boostDate: Double? = null
    var explorerEndDate: Double? = null
    var explorerStartDate: Double? = null

    var primaryColor: String? = null
    var primaryDarkColor: String? = null

    var status: Boolean? = null
    var isFeatured: Boolean? = null
    var showAsExplore: Boolean? = null
    var isPinned: Boolean? = null
    var isTrending: Boolean? = null

    var pinnedStartDate: Double? = null
    var pinnedEndDate: Double? = null

    var sortStartDate: Double? = null
    var sortEndDate: Double? = null


    // for banners
    var startDate: Double? = null
    var endDate: Double? = null

    var featuredList: ArrayList<StoreFeatureModel>? = null
    var key: String? = null

    var category: CategoryModel? = null
    var latestPromo: PromoModel? = null
    var subCategory: SubCategoryModel? = null
}

class StoreFeatureModel():Serializable {
    var bannerUrl: String? = null
    var linkUrl: String? = null
}

class StoreLocation():Serializable {
    var latitude: Double? = null
    var longitude: Double? = null
    var name: String? = null
}