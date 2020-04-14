package com.dsk.trackmyvisitor.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dsk.trackmyvisitor.data.DAO.DAO
import com.dsk.trackmyvisitor.data.entity.VisitorDetails

@Database(entities = [VisitorDetails::class], version = 1, exportSchema = false)
abstract class AppDataBase : RoomDatabase() {

    abstract fun visitorDetailsDAO(): DAO

    companion object {

        @Volatile
        private var INSTANCE: AppDataBase? = null

        fun getDatabase(context: Context): AppDataBase? {
            if (INSTANCE == null) {
                synchronized(AppDataBase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            AppDataBase::class.java, "visitor-details.db"
                        )
                            .build()
                    }
                }
            }
            return INSTANCE
        }
    }
}