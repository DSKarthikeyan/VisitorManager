package com.dsk.trackmyvisitor.util

import android.content.Context
import android.net.Uri
import com.dsk.trackmyvisitor.model.EmployeeDetails
import com.dsk.trackmyvisitor.model.VisitorDetails
import com.opencsv.CSVReader
import java.io.*
import kotlin.collections.ArrayList

class CSVWriterReader {

    fun writeCsv(
        context: Context,
        header: String,
        data: ArrayList<VisitorDetails>,
        fileName: String,
        fileExtension: String
    ) {
        var fileWriter: FileWriter? = null

        try {
            var createdFileName = UtilityService().createFile(context, fileName, fileExtension)
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
            println("Write CSV successfully! $createdFileName")
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

    fun csvReader(filePath: Uri, context: Context): ArrayList<EmployeeDetails> {
        var fileReader: BufferedReader? = null
        var csvReader: CSVReader? = null
        var employeeDetailsArrayList= ArrayList<EmployeeDetails>()
        try {
            fileReader = context.contentResolver.openInputStream(filePath)?.bufferedReader()
            csvReader = CSVReader(fileReader)

            var record: Array<String>
            val employeeDetails =
                EmployeeDetails()
            csvReader.readNext() // skip Header

            record = csvReader.readNext()
            var size = record.size;
            var count = 0;
            while (count <= size-1) {
                println(record[0] + " | " + record[1] + " | " + record[2] + " | " + record[3] + " | " + record[4] + " | " + record[5] )

                if (record[0] != null && !record[0].isEmpty()) {
                    employeeDetails.employeeId = record[0].toLong()
                }
                if (record[1] != null && !record[1].isEmpty()) {
                    employeeDetails.employeeName = record[1]
                }
                if (record[2] != null && !record[2].isEmpty()) {
                    employeeDetails.employeeEmailId = record[2]
                }
                if (record[3] != null && !record[3].isEmpty()) {
                    employeeDetails.companyName = record[3]
                }
                if (record[4] != null && !record[4].isEmpty()) {
                    employeeDetails.phoneNumber = record[4]
                }
                if (record[5] != null && !record[5].isEmpty()) {
                    employeeDetails.isEmployeeActive =
                        record[5].toLowerCase() == "true"
                }
                employeeDetailsArrayList.add(employeeDetails)
                if(count <= size-1) {
                    record = csvReader.readNext()
                }
                count++
            }
//            println("record size "+records!!.size)
            // -------------------------------------------
//            println("\n--- Read all at once '---")

//            fileReader = context.contentResolver.openInputStream(filePath)!!.bufferedReader()
//            csvReader = CSVReaderBuilder(fileReader).withSkipLines(1).build()
//
//            records = csvReader.readAll()
            csvReader.close()
        } catch (e: Exception) {
            println("Reading CSV Error!")
            e.printStackTrace()
        } finally {
            try {
                fileReader!!.close()
                csvReader!!.close()
            } catch (e: IOException) {
                println("Closing fileReader/csvParser Error!")
                e.printStackTrace()
            }
        }
        return employeeDetailsArrayList
    }
}


