package com.neb.linko.models

import java.io.Serializable

class CategoriesModel : Serializable {

    var brands: ArrayList<String>? = null
    var brandArabic: ArrayList<String>? = null

    var filters: ArrayList<String>? = null
    var filtersArabic: ArrayList<String>? = null

    var sortOrder: Int? = null
    var storesCount: Int? = null
    var limit: Int? = null
    var key: String? = "trending"
    var name: String? = null
    var status: Boolean? = null
    var nameArabic: String? = null
    var icon: String? = null
    var subCategories: ArrayList<SubCategoryModel>? = null
    var country: String? = null
}