package com.neb.linko.models

import java.io.Serializable

class BannerModel:Serializable {

    var boostDate: Long? = null
    var clicks: Long? = null
    var country: String? = null

    //
//    var creationDate: String? = null
    //

    var endDate: Long? = null
    var imageUrl: String? = null
    var isPaid: Boolean? = null
    var shows: Long? = null
    var startDate: Long? = null
    var status: Boolean? = null
    var title: String? = null
    var webUrl: String? = null

    constructor()

    constructor(
        boostDate: Long?,
        clicks: Long?,
        country: String?,
//        creationDate: String?,
        endDate: Long?,
        imageUrl: String?,
        isPaid: Boolean?,
        shows: Long?,
        startDate: Long?,
        status: Boolean?,
        title: String?,
        webUrl: String?
    ) {
        this.boostDate = boostDate
        this.clicks = clicks
        this.country = country
//        this.creationDate = creationDate
        this.endDate = endDate
        this.imageUrl = imageUrl
        this.isPaid = isPaid
        this.shows = shows
        this.startDate = startDate
        this.status = status
        this.title = title
        this.webUrl = webUrl
    }

}