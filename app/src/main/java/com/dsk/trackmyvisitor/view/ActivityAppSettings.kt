package com.dsk.trackmyvisitor.view

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
import com.dsk.trackmyvisitor.data.DAO.DAO
import com.dsk.trackmyvisitor.data.database.AppDatabase
import com.dsk.trackmyvisitor.data.entity.EmployeeDetails
import com.dsk.trackmyvisitor.model.utility.CSVWriterReader
import com.dsk.visitormanager.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ActivityAppSettings : AppCompatActivity() {

    private val PERMISSION_CODE = 1000;
    private val REQUEST_GALLERY_FILE = 12345

    private var appDatabase: AppDatabase? = null
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
//        CSVWriterReader().writeCsv(this, csvHeader, visitorDetails, "visitor-details", ".csv")
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
        Log.d("DSK", " in11 $resultCode");
        if (resultCode == Activity.RESULT_OK) {
            Log.d("DSK", "in11")
            when (requestCode) {
                REQUEST_GALLERY_FILE -> {
                    val clipData = data!!.data
                    if (clipData != null) {
                        var records = CSVWriterReader().csvReader(clipData, this)
                        insertCSVDatatoDb(records);
                    } else {
                        val msg = "Null filename data received!"
                        val toast = Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG)
                        toast.show()
                    }
                }
            }
        }
    }

    private fun insertCSVDatatoDb(visitorData: MutableList<Array<String>>?) {
        if (visitorData != null) {
            if (appDatabase == null) {
                appDatabase = AppDatabase.getAppDataBase(this)
            }
            if (appDatabase != null && visitorDetailsDAO == null) {
                visitorDetailsDAO = appDatabase?.visitorDetailsDao()
            }
            var visitorDetails = formEmployeeDatatoDB(visitorData)
            GlobalScope.launch(Dispatchers.IO)  {
                visitorDetailsDAO!!.insertVisitor(visitorDetails)
            }
        }
    }

    private fun formEmployeeDatatoDB(employeeData: MutableList<Array<String>>?): ArrayList<EmployeeDetails> {
        var visitorDetailsArrayList = ArrayList<EmployeeDetails>()
        if (employeeData != null) {
            var employeeDetails = EmployeeDetails()
            for (visitorDetailsData in employeeData) {
                employeeDetails.employeeId = visitorDetailsData[0].toLong()
                employeeDetails.employeeName = visitorDetailsData[1]
                employeeDetails.employeeEmailId = visitorDetailsData[2]
                employeeDetails.companyName = visitorDetailsData[3]
                employeeDetails.phoneNumber = visitorDetailsData[4]
                employeeDetails.isEmployeeActive = visitorDetailsData[5].toBoolean()

                visitorDetailsArrayList.add(employeeDetails)
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