package com.neb.linko.models

class Registrations {

    var email: String? = null
    var instagram: String? = null
    var isIsNew: Boolean? = null
    var isIsPaid: Boolean? = null
    var link: String? = null
    var optionType: String? = null
    var payments: Payments? = null
    var phone: String? = null
    var priceType: String? = null
    var userId: String? = null
    var boostKey:String?=null
    var boost:BoostModel?=null
    var date:String?=null

    constructor()

    constructor(
        email: String?,
        instagram: String?,
        isNew: Boolean?,
        link: String?,
        phone: String?,
        priceType: String?,
        userId: String?
    ) {
        this.email = email
        this.instagram = instagram
        this.isIsNew = isNew
        this.link = link
        this.phone = phone
        this.priceType = priceType
        this.userId = userId
    }


}