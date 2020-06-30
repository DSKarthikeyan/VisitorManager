package com.dsk.trackmyvisitor.data.prefs

import com.dsk.trackmyvisitor.model.VisitorDetails

class SingletonVisitorDetails private constructor() {

    companion object {
        @Volatile private var INSTANCE: SingletonVisitorDetails? = null
        fun getInstance(): SingletonVisitorDetails {
            return INSTANCE
                ?: synchronized(this){
                SingletonVisitorDetails().also {
                    INSTANCE = it
                }
            }
        }

        var visitorDetailsSingletonData: VisitorDetails? = null

        fun destroy() {
            INSTANCE = null
        }

    }
}