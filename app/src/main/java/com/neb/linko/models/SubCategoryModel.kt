package com.neb.linko.models

import java.io.Serializable

class SubCategoryModel : Serializable {
    var key: String? = null
    var categoryId: String? = null
    var name: String? = null
    var nameArabic: String? = null
    var imageUrl: String? = null
    var sortOrder: Int? = null
}