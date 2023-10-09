package com.neb.linko.repository

import com.neb.linko.models.BoostsRequest
import com.neb.linko.retrofit.ApiService

class BoostsRepository(var apiService: ApiService) {

    suspend fun sendBoostsRequest(boostsRequest: BoostsRequest) = apiService.sendBoostsRequest(boostsRequest)
    suspend fun detect(paymentId:Long,id:Long) = apiService.detect(paymentId,id)

}