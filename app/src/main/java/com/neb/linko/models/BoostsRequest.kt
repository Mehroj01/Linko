package com.neb.linko.models

class BoostsRequest {

    var selection: String? = null
    var date: String? = null
    var userId: String? = null
    var imageUrl: String? = null
    var dates: List<String>? = null
    var price: Int? = null

    constructor()

    constructor(selection: String?, date: String?, userId: String?, imageUrl: String?) {
        this.selection = selection
        this.date = date
        this.userId = userId
        this.imageUrl = imageUrl
    }

    constructor(
        selection: String?,
        date: String?,
        userId: String?,
        imageUrl: String?,
        dates: List<String>?,
        price: Int?
    ) {
        this.selection = selection
        this.date = date
        this.userId = userId
        this.imageUrl = imageUrl
        this.dates = dates
        this.price = price
    }

}