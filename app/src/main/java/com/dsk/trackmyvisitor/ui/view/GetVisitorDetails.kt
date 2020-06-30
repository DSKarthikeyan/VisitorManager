package com.dsk.visitormanager

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.dsk.trackmyvisitor.model.VisitorDetails
import com.dsk.trackmyvisitor.data.prefs.SingletonVisitorDetails
import com.dsk.trackmyvisitor.ui.view.ActivityAppSettings
import com.dsk.trackmyvisitor.ui.view.ActivityPushVisitorDetails


class GetVisitorDetails : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visitor_details)
        initObject();
    }

    /**
     * fun: Initialize Objects
     */
    fun initObject() {
        var submitNextButtonClick = findViewById<Button>(R.id.nextButton)
        submitNextButtonClick.setOnClickListener {
            if (checkInputIsValid()) {
                openSignatureActivity();
            }
        }
    }

    fun checkEditTextInputEmpty(editText: EditText): Boolean {
        var isValidData = false;
        if (editText != null) {
            val inputString: String = editText.text.toString()
            if (inputString.trim().length > 0) {
                isValidData = true
            }
        }
        return isValidData;
    }

    fun setErrorMessageFocus(editText: EditText, message: String) {
        if (editText != null && message != null && !message.isEmpty()) {
            editText.requestFocus()
            editText.error = message
        }
    }

    /**
     * fun: open Signature Activity
     */
    fun openSignatureActivity() {
        val intent = Intent(this, ActivityPushVisitorDetails::class.java)
        startActivity(intent)
    }

    /**
     * fun: Get the selected radio button text using radio button on click listener
     */
    fun radio_button_click(view: View) {
        var editTextViewHaveDevices = findViewById<EditText>(R.id.editTextHaveAnyDevices)
        val radio: RadioButton = findViewById(radio_group.checkedRadioButtonId)
        if (radio != null && radio.text != null && !radio.text.isEmpty()) {
            if (radio.text.equals("No")) {
                editTextViewHaveDevices.visibility = View.INVISIBLE
            } else if (radio.text.equals("Yes")) {
                editTextViewHaveDevices.visibility = View.VISIBLE
            }
        }
    }

    fun String.isValidEmail(): Boolean =
        this.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    fun String.isValidPhone(): Boolean = this.isNotEmpty() && Patterns.PHONE.matcher(this).matches()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.app_settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_settings -> {
                openAppSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openAppSettings() {
        val intent = Intent(this, ActivityAppSettings::class.java)
        startActivity(intent)
    }

    fun checkInputIsValid(): Boolean {
        var isValidForm = false
        if (checkEditTextInputEmpty(editTextName)) {
            val visitorName = editTextName.text.toString().trim()
            if (checkEditTextInputEmpty(editTextEmployeeName)) {
                val employeeName = editTextEmployeeName.text.toString().trim()
                if (checkEditTextInputEmpty(editTextCompanyName)) {
                    val companyName = editTextCompanyName.text.toString().trim()
                    if (checkEditTextInputEmpty(editTextEmailId)) {
                        val emailId = editTextEmailId.text.toString()
                        if (emailId.isValidEmail()) {
                            val visitorEmailId = emailId
                            if (checkEditTextInputEmpty(editTextPhoneNumber)) {
                                if (checkEditTextInputEmpty(editTextPurposeofVisit)) {
                                    val phNumber = editTextPhoneNumber.text.toString()
                                    if (phNumber.isValidPhone()) {
                                        val phoneNumber = phNumber
                                        if (checkEditTextInputEmpty(editTextPurposeofVisit)) {
                                            val visitPurpose =
                                                editTextPurposeofVisit.text.toString()
                                            var deviceDetails = ""
                                            var id: Int = radio_group.checkedRadioButtonId
                                            if (id != null && id != -1) {
                                                val radio: RadioButton = findViewById(id)
                                                if (radio != null && radio.text != null && !radio.text.isEmpty()) {
                                                    if (radio.text == "Yes") {
                                                        if (checkEditTextInputEmpty(
                                                                editTextHaveAnyDevices
                                                            )
                                                        ) {
                                                            deviceDetails =
                                                                editTextHaveAnyDevices.text.toString()
                                                                    .trim()
                                                            isValidForm = true
                                                        } else {
                                                            setErrorMessageFocus(
                                                                editTextHaveAnyDevices,
                                                                "Device details not be empty"
                                                            )
                                                        }
                                                    } else if (radio.text == "No") {
                                                        isValidForm = true
                                                    }

                                                    val visitorDetails =
                                                        VisitorDetails(
                                                            0,
                                                            visitorName,
                                                            employeeName,
                                                            phoneNumber,
                                                            visitorEmailId,
                                                            companyName,
                                                            visitPurpose,
                                                            deviceDetails,
                                                            null,
                                                            null
                                                        )
                                                    SingletonVisitorDetails.visitorDetailsSingletonData =
                                                        visitorDetails
                                                }
                                            } else {
                                                Toast.makeText(
                                                    this,
                                                    "Choose Have any Devices",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        } else {
                                            setErrorMessageFocus(
                                                editTextPurposeofVisit,
                                                "Purpose of visit not be empty"
                                            )
                                        }
                                    } else {
                                        setErrorMessageFocus(
                                            editTextPhoneNumber,
                                            "Not Valid Phone Number"
                                        )
                                    }
                                } else {
                                    setErrorMessageFocus(
                                        editTextPhoneNumber,
                                        "Phone Number is not be empty"
                                    )
                                }
                            } else {
                                setErrorMessageFocus(
                                    editTextPhoneNumber,
                                    "Phone Number is not be empty"
                                )
                            }
                        } else {
                            setErrorMessageFocus(editTextEmailId, "EmailId is not valid")
                        }
                    } else {
                        setErrorMessageFocus(editTextEmailId, "EmailId Not be empty")
                    }
                } else {
                    setErrorMessageFocus(editTextCompanyName, "Company Name Not be empty")
                }
            } else {
                setErrorMessageFocus(editTextCompanyName, "Employee Name Not be empty")
            }
        } else {
            setErrorMessageFocus(editTextName, "Name Not be empty")
        }
        return isValidForm
    }
}