package com.neb.linko.models

class UserModel() {
    var imageUrl   : String? = null
    var country : String? = null
    var businessName : String? = null
    var name         : String? = null
    var phone : String? = null
    var details : String? = null

    var ordersCount : Int? = 0
    var rating : Float? = 0f

    var status : Boolean? = false
    var key: String? = null
    var storeKey: String? = null

    var deviceType : String? = null
    var deviceToken : String? = null
    var deviceModel : String? = null

    var age : String? = null
    var city : String? = null
    var language : String? = null

    var interestsList : ArrayList<String>? = ArrayList()
    var bookmarks : ArrayList<String>? = ArrayList()
    var ratedFor : Map<String, Int>? = HashMap()
}