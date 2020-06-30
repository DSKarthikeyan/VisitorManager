package com.dsk.trackmyvisitor.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Visitor_Details")
class VisitorDetails{
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "visitor_id")
    var visitorId: Long = 0

    @ColumnInfo(name = "visitor_name")
    var visitorName: String? = ""

    @ColumnInfo(name = "employee_name")
    var employeeName: String? = ""

    @ColumnInfo(name = "phone_number")
    var phoneNumber: String? = ""

    @ColumnInfo(name = "visitor_email")
    var visitorEmailId: String? = ""

    @ColumnInfo(name = "company_name")
    var companyName: String? = ""

    @ColumnInfo(name = "visit_purpose")
    var visitPurpose: String? = ""

    @ColumnInfo(name = "device_details")
    var deviceDetails: String? = ""

    @ColumnInfo(name = "visitor_image")
    var visitorImage: ByteArray? = null

    @ColumnInfo(name = "visitor_sign")
    var visitorSign: ByteArray? = null

    constructor(){}
    constructor(id: Long, visitorName: String?,employeeName: String?,phoneNumber: String?,
                visitorEmailId: String?,companyName: String?,visitPurpose: String?,deviceDetails: String?,visitorImage: ByteArray? ,
                visitorSign: ByteArray?) {
        this.visitorId = id
        this.visitorName = visitorName
        this.employeeName = employeeName
        this.phoneNumber = phoneNumber
        this.visitorEmailId = visitorEmailId
        this.companyName = companyName
        this.visitPurpose = visitPurpose
        this.deviceDetails = deviceDetails
        this.deviceDetails = deviceDetails
        this.visitorImage = visitorImage
        this.visitorSign = visitorSign
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VisitorDetails

        if (visitorId != other.visitorId) return false
        if (visitorName != other.visitorName) return false
        if (employeeName != other.employeeName) return false
        if (phoneNumber != other.phoneNumber) return false
        if (visitorEmailId != other.visitorEmailId) return false
        if (companyName != other.companyName) return false
        if (visitPurpose != other.visitPurpose) return false
        if (deviceDetails != other.deviceDetails) return false
        if (visitorImage != null) {
            if (other.visitorImage == null) return false
            if (!visitorImage!!.contentEquals(other.visitorImage!!)) return false
        } else if (other.visitorImage != null) return false
        if (visitorSign != null) {
            if (other.visitorSign == null) return false
            if (!visitorSign!!.contentEquals(other.visitorSign!!)) return false
        } else if (other.visitorSign != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = visitorId.hashCode()
        result = 31 * result + (visitorName?.hashCode() ?: 0)
        result = 31 * result + (employeeName?.hashCode() ?: 0)
        result = 31 * result + (phoneNumber?.hashCode() ?: 0)
        result = 31 * result + (visitorEmailId?.hashCode() ?: 0)
        result = 31 * result + (companyName?.hashCode() ?: 0)
        result = 31 * result + (visitPurpose?.hashCode() ?: 0)
        result = 31 * result + (deviceDetails?.hashCode() ?: 0)
        result = 31 * result + (visitorImage?.contentHashCode() ?: 0)
        result = 31 * result + (visitorSign?.contentHashCode() ?: 0)
        return result
    }
}