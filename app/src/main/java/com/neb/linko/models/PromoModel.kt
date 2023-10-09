package com.neb.linko.models

import java.io.Serializable

class PromoModel : Serializable {

    var storeKey: String? = null
    var promoCode: String? = null
    var claims: ArrayList<String>? = null
    var claimsCount: Int? = null
    var claimsRequired: Int? = null
    var promoDetails: String? = null

    var percentage: Int? = null
    var expiry: Int? = null

    var status: Boolean? = null
    var key: String? = null
    var expirationTime: Long? = null
    var creationDate: Long? = null

}