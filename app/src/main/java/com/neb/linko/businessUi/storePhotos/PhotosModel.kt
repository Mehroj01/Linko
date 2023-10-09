package com.neb.linko.businessUi.storePhotos

class PhotosModel {

    var id: Int? = null
    var photoUrl: String? = null

    constructor()

    constructor(id: Int?, photoUrl: String?) {
        this.id = id
        this.photoUrl = photoUrl
    }

}