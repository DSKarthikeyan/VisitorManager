package com.dsk.trackmyvisitor.ui.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dsk.trackmyvisitor.data.local.DAO.DAO
import com.dsk.trackmyvisitor.data.local.database.AppDataBase
import com.dsk.trackmyvisitor.model.EmployeeDetails
import com.dsk.trackmyvisitor.model.VisitorDetails
import com.dsk.trackmyvisitor.util.CSVWriterReader
import com.dsk.visitormanager.R


class ActivityAppSettings : AppCompatActivity() {

    private val PERMISSION_CODE = 1000;
    private val REQUEST_GALLERY_FILE = 12345

    private var appDatabase: AppDataBase? = null
    private var visitorDetailsDAO: DAO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val cardViewExportDB = findViewById<CardView>(R.id.cardViewExportData)
        val cardViewImportDB = findViewById<CardView>(R.id.cardViewImportData)

        cardViewExportDB.setOnClickListener {
            if (checkPermission()) {
                exportDatabaseFile()
            } else {
                requestPermission()
            }
        }

        cardViewImportDB.setOnClickListener {
            if (checkPermission()) {
                importDatabaseFile()
            } else {
                requestPermission()
            }
        }

    }

    private fun checkPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ),
            PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_GALLERY_FILE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED
            ) {
                importDatabaseFile()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportDatabaseFile() {
        val csvHeader = "id,name,emailId,companyName"
        val visitorDetails = ArrayList<VisitorDetails>()
        visitorDetails.add(
            VisitorDetails(
                1, "DSK", "Karthik", "8526603722",
                "dsk@mail.com", "Inferno", "Interview", "Oneplus", null,
                null
            )
        )
        visitorDetails.add(
            VisitorDetails(
                2, "Karthik", "DSK", "7904018967",
                "dsk@mail.com", "Inferno", "Interview", "Oneplus", null,
                null
            )
        )
        visitorDetails.add(
            VisitorDetails(
                3, "D S Karthikeyan", "Karthik", "8526603722",
                "dsk@mail.com", "Inferno", "Meeting", "Oneplus", null,
                null
            )
        )
        CSVWriterReader().writeCsv(this, csvHeader, visitorDetails, "visitor-details", ".csv")
    }

    private fun importDatabaseFile() {
        if (checkPermission()) {
            selectImage()
        } else {
            requestPermission()
        }
    }

    private fun selectImage() {
        val options =
            arrayOf<CharSequence>("Select File", "Cancel")
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Employee Data")
        builder.setItems(options, DialogInterface.OnClickListener { dialog, item ->
            if (options[item] == "Select File") {
                val intent = Intent()
                intent.type = "text/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(intent, "Choose File"),
                    REQUEST_GALLERY_FILE
                )
            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            }
        })
        builder.setCancelable(false)
        builder.show()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_GALLERY_FILE -> {
                    val clipData = data!!.data
                    if (clipData != null) {
                        Log.d("DSK ", "in $clipData");
                        var arrayListEmployeeDetails = ArrayList<EmployeeDetails>()
                        arrayListEmployeeDetails =  CSVWriterReader().csvReader(clipData, this)
                        if (arrayListEmployeeDetails != null) {
                            Log.d("DSK ", "in ${arrayListEmployeeDetails.size}");
                            insertCSVDatatoDb(arrayListEmployeeDetails)
                        } else {
                            Toast.makeText(
                                this,
                                "Data not found in csv, please upload valid .csv file",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        val msg = "Null filename data received!"
                        val toast = Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG)
                        toast.show()
                    }
                }
            }
        }
    }

    private fun insertCSVDatatoDb(visitorData: ArrayList<EmployeeDetails>) {
//        if (appDatabase == null) {
            appDatabase = AppDataBase.getDatabase(this)
//        }
//        if (appDatabase != null) {
            visitorDetailsDAO = appDatabase!!.visitorDetailsDAO()
//        }

        Log.d("DSK", " visitorDetails " + visitorData!!.size)
//        GlobalScope.launch(Dispatchers.IO) {
//            visitorDetailsDAO!!.insertVisitorList(visitorData!!)
//        }
    }

    private fun formEmployeeDatatoDB(employeeData: ArrayList<EmployeeDetails>?): ArrayList<EmployeeDetails>? {
        var visitorDetailsArrayList: ArrayList<EmployeeDetails>? = null
        if (employeeData != null && employeeData.size > 0) {
            val employeeDetails =
                EmployeeDetails()
            for (employeeDetailsData in employeeData) {
                if (employeeDetailsData != null) {
//                    println(employeeDetailsData[0] + " | " + employeeDetailsData[1] + " | " + employeeDetailsData[2] + " | " + employeeDetailsData[3])

//                    if (employeeDetailsData[0] != null && employeeDetailsData[0].isEmpty()) {
//                        employeeDetails.employeeId = employeeDetailsData[0].toLong()
//                    }
//                    if (employeeDetailsData[1] != null && employeeDetailsData[1].isEmpty()) {
//                        employeeDetails.employeeName = employeeDetailsData[1]
//                    }
//                    if (employeeDetailsData[2] != null && employeeDetailsData[2].isEmpty()) {
//                        employeeDetails.employeeEmailId = employeeDetailsData[2]
//                    }
//                    if (employeeDetailsData[3] != null && employeeDetailsData[3].isEmpty()) {
//                        employeeDetails.companyName = employeeDetailsData[3]
//                    }
//                    if (employeeDetailsData[4] != null && employeeDetailsData[4].isEmpty()) {
//                        employeeDetails.phoneNumber = employeeDetailsData[4]
//                    }
//                    if (employeeDetailsData[5] != null && employeeDetailsData[5].isEmpty()) {
//                        employeeDetails.isEmployeeActive =
//                            employeeDetailsData[5].toLowerCase() == "true"
//                    }
                    visitorDetailsArrayList!!.add(employeeDetails)
                }
            }
        }
        return visitorDetailsArrayList
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun handleImageOnKitkat(data: Intent?): String {
        var imagePath: String? = null
        val uri = data!!.data
        //DocumentsContract defines the contract between a documents provider and the platform.
        if (DocumentsContract.isDocumentUri(this, uri)) {
            Log.d("DSK ", "Authority " + uri!!.authority);
            val docId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri!!.authority) {
                val id = docId.split(":")[1]
                val selection = MediaStore.Images.Media._ID + "=" + id
                imagePath = getImagePath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    selection
                )
            } else if ("com.android.providers.downloads.documents" == uri.authority) {
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse(
                        "content://downloads/public_downloads"
                    ), java.lang.Long.valueOf(docId)
                )
                imagePath = getImagePath(contentUri, null)
            }
        } else if ("content".equals(uri!!.scheme, ignoreCase = true)) {
            imagePath = getImagePath(uri, null)
        } else if ("file".equals(uri!!.scheme, ignoreCase = true)) {
            imagePath = uri!!.path
        }
        Log.d("DSK ", "imagePath2 $imagePath")
        return imagePath!!
    }

    private fun getImagePath(uri: Uri, selection: String?): String {
        var path: String? = null
        val cursor = contentResolver.query(uri, null, selection, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path!!
    }

}