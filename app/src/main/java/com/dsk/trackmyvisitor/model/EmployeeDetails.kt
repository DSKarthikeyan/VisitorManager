package com.dsk.trackmyvisitor.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Employee_Details")
class EmployeeDetails {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "employee_id")
    var employeeId: Long = 0

    @ColumnInfo(name = "employee_name")
    var employeeName: String? = ""

    @ColumnInfo(name = "phone_number")
    var phoneNumber: String? = ""

    @ColumnInfo(name = "employee_email")
    var employeeEmailId: String? = ""

    @ColumnInfo(name = "company_name")
    var companyName: String? = ""

    @ColumnInfo(name = "employee_image")
    var employeeImage: ByteArray? = null

    @ColumnInfo(name = "isEmployeeActive")
    var isEmployeeActive: Boolean? = false

    constructor() {}
    constructor(
        employeeId: Long,
        employeeName: String?,
        phoneNumber: String?,
        employeeEmailId: String?,
        companyName: String?,
        employeeImage: ByteArray?,
        isEmployeeActive: Boolean
    ) {
        this.employeeId = employeeId
        this.employeeName = employeeName
        this.phoneNumber = phoneNumber
        this.employeeEmailId = employeeEmailId
        this.companyName = companyName
        this.employeeImage = employeeImage
        this.isEmployeeActive = isEmployeeActive
    }

}