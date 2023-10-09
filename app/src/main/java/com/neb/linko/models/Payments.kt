package com.neb.linko.models

class Payments {

    var comments: String? = null
    var createdDate: String? = null
    var customerEmail: String? = null
    var customerMobile: String? = null
    var customerName: String? = null
    var customerReference: String? = null
    var expiryDate: String? = null
    var invoiceDisplayValue: String? = null
    var invoiceId: Int? = null

    //    var invoiceItems:ArrayList<>
    var invoiceReference: String? = null
    var invoiceStatus: String? = null
    var invoiceTransactions: ArrayList<InvoiceTransactions>? = null
    var invoiceValue: Int? = null

    //    var suppliers:ArrayList<>
    var userDefinedField: String? = null

    constructor()

    constructor(
        comments: String?,
        createdDate: String?,
        customerEmail: String?,
        customerMobile: String?,
        customerName: String?,
        customerReference: String?,
        expiryDate: String?,
        invoiceDisplayValue: String?,
        invoiceId: Int?,
        invoiceReference: String?,
        invoiceStatus: String?,
        invoiceTransactions: ArrayList<InvoiceTransactions>?,
        invoiceValue: Int?,
        userDefinedField: String?
    ) {
        this.comments = comments
        this.createdDate = createdDate
        this.customerEmail = customerEmail
        this.customerMobile = customerMobile
        this.customerName = customerName
        this.customerReference = customerReference
        this.expiryDate = expiryDate
        this.invoiceDisplayValue = invoiceDisplayValue
        this.invoiceId = invoiceId
        this.invoiceReference = invoiceReference
        this.invoiceStatus = invoiceStatus
        this.invoiceTransactions = invoiceTransactions
        this.invoiceValue = invoiceValue
        this.userDefinedField = userDefinedField
    }
}