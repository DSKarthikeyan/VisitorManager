package com.dsk.trackmyvisitor.data.DAO

import androidx.room.*
import com.dsk.trackmyvisitor.data.entity.EmployeeDetails
import com.dsk.trackmyvisitor.data.entity.VisitorDetails

@Dao
interface DAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVisitor(visitorDetails: VisitorDetails): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVisitorList(visitorDetails: List<EmployeeDetails>)

    @Query("SELECT * FROM Visitor_Details WHERE visitor_id == :id")
    fun getVisitorById(id: Long): List<VisitorDetails>

    @Query("SELECT * FROM Visitor_Details")
    fun getVisitor(): List<VisitorDetails>

}