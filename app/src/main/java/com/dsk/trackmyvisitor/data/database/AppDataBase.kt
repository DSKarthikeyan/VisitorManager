package com.dsk.trackmyvisitor.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dsk.trackmyvisitor.data.DAO.DAO
import com.dsk.trackmyvisitor.data.entity.VisitorDetails

@Database(entities = [VisitorDetails::class], version = 1,exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun visitorDetailsDao(): DAO

    companion object {
        var Instance: AppDatabase? = null

        fun getAppDataBase(context: Context): AppDatabase? {
            if (Instance == null){
                synchronized(AppDatabase::class){
                    Instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "visitor-details.db").build()
                }
            }
            return Instance
        }

        fun destroyDataBase(){
            Instance = null
        }
    }
}