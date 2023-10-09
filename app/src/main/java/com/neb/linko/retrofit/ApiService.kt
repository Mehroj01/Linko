package com.neb.linko.retrofit

import com.neb.linko.models.BoostsRequest
import com.neb.linko.models.BoostsResponse
import com.neb.linko.models.DetectModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("v1/payForBoost")
    suspend fun sendBoostsRequest(@Body boostsRequest: BoostsRequest): Response<BoostsResponse>

    @GET("v1/finalizeBoostRegistration")
    suspend fun detect(
        @Query("paymentId") paymentId: Long,
        @Query("Id") Id: Long,
    ): Response<DetectModel>

}