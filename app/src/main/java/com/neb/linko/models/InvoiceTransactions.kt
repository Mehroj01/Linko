package com.neb.linko.models

class InvoiceTransactions {

    var authorizationId:String?=null
    var cardNumber:Int?=null
    var currency:String?=null
    var customerServiceCharge:String?=null
    var dueValue:String?=null
    var error:String?=null
    var errorCode:String?=null
    var paidCurrency:String?=null
    var paidCurrencyValue:String?=null
    var paymentGateway:String?=null
    var paymentId:String?=null
    var referenceId:String?=null
    var trackId:String?=null
    var transactionDate:String?=null
    var transactionId:String?=null
    var transactionStatus:String?=null
    var transationValue:String?=null

    constructor()

    constructor(
        authorizationId: String?,
        cardNumber: Int?,
        currency: String?,
        customerServiceCharge: String?,
        dueValue: String?,
        error: String?,
        errorCode: String?,
        paidCurrency: String?,
        paidCurrencyValue: String?,
        paymentGateway: String?,
        paymentId: String?,
        referenceId: String?,
        trackId: String?,
        transactionDate: String?,
        transactionId: String?,
        transactionStatus: String?,
        transationValue: String?
    ) {
        this.authorizationId = authorizationId
        this.cardNumber = cardNumber
        this.currency = currency
        this.customerServiceCharge = customerServiceCharge
        this.dueValue = dueValue
        this.error = error
        this.errorCode = errorCode
        this.paidCurrency = paidCurrency
        this.paidCurrencyValue = paidCurrencyValue
        this.paymentGateway = paymentGateway
        this.paymentId = paymentId
        this.referenceId = referenceId
        this.trackId = trackId
        this.transactionDate = transactionDate
        this.transactionId = transactionId
        this.transactionStatus = transactionStatus
        this.transationValue = transationValue
    }

}