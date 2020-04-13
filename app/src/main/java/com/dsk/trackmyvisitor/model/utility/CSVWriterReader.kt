package com.dsk.trackmyvisitor.model.utility

import android.content.Context
import android.net.Uri
import com.dsk.trackmyvisitor.data.entity.VisitorDetails
import com.opencsv.CSVReader
import com.opencsv.CSVReaderBuilder
import com.opencsv.CSVWriter
import com.opencsv.bean.ColumnPositionMappingStrategy
import com.opencsv.bean.StatefulBeanToCsv
import com.opencsv.bean.StatefulBeanToCsvBuilder
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

class CSVWriterReader {

   fun writeCsv(context: Context,header: String,data: ArrayList<VisitorDetails>,fileName: String,fileExtension: String) {
        var fileWriter: FileWriter? = null

        try {
            var createdFileName = UtilityService().createFile(context,fileName,fileExtension)
            fileWriter = FileWriter(createdFileName)

            fileWriter.append(header)
            fileWriter.append('\n')
            for (customer in data) {
                fileWriter.append(customer.visitorId.toString())
                fileWriter.append(',')
                fileWriter.append(customer.visitorName)
                fileWriter.append(',')
                fileWriter.append(customer.visitorEmailId)
                fileWriter.append(',')
                fileWriter.append(customer.companyName)
                fileWriter.append('\n')
            }
//            println("Write CSV successfully! $createdFileName")
        } catch (e: Exception) {
//            println("Writing CSV error!")
            e.printStackTrace()
        } finally {
            try {
                fileWriter!!.flush()
                fileWriter.close()
            } catch (e: IOException) {
//                println("Flushing/closing error!")
                e.printStackTrace()
            }
        }
    }

    fun csvReader(filePath: Uri, context: Context): MutableList<Array<String>>? {
        var fileReader: BufferedReader? = null
        var csvReader: CSVReader? = null
        var records: MutableList<Array<String>>? = null

        try {
            fileReader = context.contentResolver.openInputStream(filePath)?.bufferedReader()
            csvReader = CSVReader(fileReader)

//            var record: Array<String>?
            csvReader.readNext() // skip Header

//            record = csvReader.readNext()
//            while (record != null) {
//                println(record[0] + " | " + record[1] + " | " + record[2] + " | " + record[3])
//                record = csvReader.readNext()
//            }

            // -------------------------------------------
//            println("\n--- Read all at once '---")
//
//            fileReader = context.contentResolver.openInputStream(filePath)?.bufferedReader()
//            csvReader = CSVReaderBuilder(fileReader).withSkipLines(1).build()

            records = csvReader.readAll()
            csvReader.close()
        } catch (e: Exception) {
//            println("Reading CSV Error!")
            e.printStackTrace()
        } finally {
            try {
                fileReader!!.close()
                csvReader!!.close()
            } catch (e: IOException) {
//                println("Closing fileReader/csvParser Error!")
                e.printStackTrace()
            }
        }
        return records
    }
}


