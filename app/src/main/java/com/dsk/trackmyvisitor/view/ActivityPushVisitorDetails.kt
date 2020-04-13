package com.dsk.trackmyvisitor.view

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.dsk.trackmyvisitor.data.DAO.DAO
import com.dsk.trackmyvisitor.data.database.AppDatabase
import com.dsk.trackmyvisitor.data.entity.VisitorDetails
import com.dsk.trackmyvisitor.model.utility.SingleTouchView
import com.dsk.trackmyvisitor.model.utility.SingletonVisitorDetails
import com.dsk.trackmyvisitor.model.utility.UtilityService
import com.dsk.visitormanager.GetVisitorDetails
import com.dsk.visitormanager.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class ActivityPushVisitorDetails : AppCompatActivity() {

    private val PERMISSION_CODE = 1000;
    private val CAMERA_REQUEST_CODE = 12345
    private val REQUEST_GALLERY_CAMERA = 54654
    private var imageViewVisitorPhoto: ImageView? = null
    private var imageViewVisitorSignClear: ImageView? = null
    private var photoFile: File? = null
    private var currentAnimator: Animator? = null
    private var shortAnimationDuration: Int = 0
    private var visitorPhotoData: String? = "";
    private var singleTouchView: SingleTouchView? = null
    private var visitorSign: Bitmap? = null
    private var appDatabase: AppDatabase? = null
    private var visitorDetailsDAO: DAO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visitor_signature)
        initObject();
    }

    private fun initObject() {
        var submitButton = findViewById<Button>(R.id.submitButton)
        var textViewAddPhoto = findViewById<TextView>(R.id.textViewTakePhoto)
        imageViewVisitorPhoto = findViewById(R.id.imageViewVisitorPhoto)
        imageViewVisitorSignClear = findViewById(R.id.imageViewSignClear)
        var imageViewVisitorSign = findViewById<ImageView>(R.id.imageViewSign)
        var relativeLayoutVisitorSign = findViewById<RelativeLayout>(R.id.relativeLayoutVisitorSign)
        singleTouchView = findViewById(R.id.singletouchview_sign)
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

        submitButton.setOnClickListener {
            Log.d("DSK", "111")
            if (checkVisitorDetails()) {

            }
        }

        textViewAddPhoto.setOnClickListener {
            if (checkPermission()) {
                selectImage()
            } else {
                requestPermission()
            }
        }

        imageViewVisitorSignClear!!.setOnClickListener {
            imageViewVisitorSign.visibility = View.INVISIBLE
            imageViewVisitorSign.setImageBitmap(null)
            removeView(relativeLayoutVisitorSign, singleTouchView!!)
        }

    }

    private fun checkVisitorDetails(): Boolean {
        var isValidData = false
        if (visitorPhotoData != null && visitorPhotoData!!.isNotEmpty()) {
            var visitorSignature: ByteArray? = getVisitorSign()
            if (visitorSignature != null) {
                storeDatatoLocalDb()
            } else {
                Toast.makeText(this, "Put your Signature", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Please Upload Photo", Toast.LENGTH_LONG).show()
        }
        return isValidData
    }

    private fun enableImageListener() {
        var imageViewVisitorPhoto1 = findViewById<ImageView>(R.id.imageViewVisitorPhoto)

        if (visitorPhotoData != null && !visitorPhotoData!!.isEmpty() && imageViewVisitorPhoto1 != null) {
            imageViewVisitorPhoto1.setOnClickListener {
                zoomImageFromThumb(imageViewVisitorPhoto1, visitorPhotoData)
            }
        }
    }

    private fun storeDatatoLocalDb() {
        if (appDatabase == null) {
            appDatabase = AppDatabase.getAppDataBase(this)
        }
        if (appDatabase != null && visitorDetailsDAO == null) {
            visitorDetailsDAO = appDatabase?.visitorDetailsDao()
        }
        var visitorDetails: VisitorDetails? = null
        visitorDetails = getVisitorDetails()
        var visitorId: Long = 0
        GlobalScope.launch(Dispatchers.IO) {
            visitorId = visitorDetailsDAO!!.insertVisitor(visitorDetails)
            Log.d("DSK ", " visitorId $visitorId");
            if (visitorId > 0) {
                destroyVisitorSingleton()
                callHomePage()
            }
        }

    }

    private fun destroyVisitorSingleton(){
        SingletonVisitorDetails.destroy()
    }

    private fun callHomePage() {
        val intent = Intent(this, GetVisitorDetails::class.java)
        startActivity(intent)
        finish();
    }

    private fun getVisitorDetails(): VisitorDetails {
        val visitorDetails = formVisitorDetails()
        SingletonVisitorDetails.visitorDetailsSingletonData = visitorDetails
        return visitorDetails
    }

    private fun formVisitorDetails(): VisitorDetails {
        var visitorDetails: VisitorDetails? = null
        if (SingletonVisitorDetails.visitorDetailsSingletonData != null) {
            visitorDetails = SingletonVisitorDetails.visitorDetailsSingletonData
            var utilityService = UtilityService()
            if (visitorDetails != null) {
                visitorDetails.visitorSign = utilityService.getBytesPNG(visitorSign)!!
                if (visitorPhotoData != null && visitorPhotoData!!.isNotEmpty()) {
                    val visitorPhotoBitmap = BitmapFactory.decodeFile(visitorPhotoData!!)
                    if (visitorPhotoBitmap != null) {
                        visitorDetails.visitorImage =
                            utilityService.getBytesPNG(visitorPhotoBitmap)!!
                    }
                }
            }
        }
        return visitorDetails!!
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

    private fun removeView(layout: RelativeLayout, view: SingleTouchView) {
        view.signAgain()
    }

    private fun selectImage() {
        val options =
            arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Add Photo!")
        builder.setItems(options, DialogInterface.OnClickListener { dialog, item ->
            if (options[item] == "Take Photo") {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    captureImage()
                } else {
                    captureImage2()
                }
            } else if (options[item] == "Choose from Gallery") {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(intent, "Select Picture"),
                    REQUEST_GALLERY_CAMERA
                )
            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            }
        })
        builder.setCancelable(false)
        builder.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_GALLERY_CAMERA || requestCode == CAMERA_REQUEST_CODE|| requestCode == PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED
            ) {
                selectImage()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val myBitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)
                    imageViewVisitorPhoto!!.setImageBitmap(myBitmap)
                    visitorPhotoData = photoFile!!.absolutePath
                }
                REQUEST_GALLERY_CAMERA -> {
                    val selectedImageUri = data!!.data
                    if (null != selectedImageUri) {
                        handleImageOnKitkat(data)
                        imageViewVisitorPhoto!!.setImageURI(selectedImageUri)
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun handleImageOnKitkat(data: Intent?) {
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
        Log.d("DSK ", "imagePath2 $imagePath");
        visitorPhotoData = imagePath
        enableImageListener()
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

    private fun captureImage() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            var utilityService = UtilityService()
            // Create the File where the photo should go
            try {
                photoFile = utilityService.createImageFile(this)
                Log.d("DSK ", photoFile!!.absolutePath)
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    var photoURI = FileProvider.getUriForFile(
                        this,
                        "com.dsk.visitormanager.fileprovider",
                        photoFile!!
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
                }
            } catch (ex: Exception) {
                // Error occurred while creating the File
                Log.d("DSK ", "Capture Image Bug: " + ex.message.toString())
            }
        } else {
            Log.d("DSK ", "Null")
        }
    }


    /**
     *  Capture Image function for 4.4.4 and lower. Not tested for Android Version 3 and 2
     */
    private fun captureImage2() {
        try {
            var utilityService = UtilityService()
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = utilityService.createImageFile4()
            if (photoFile != null) {
                Log.d("DSK ", photoFile!!.absolutePath)
                val photoURI = Uri.fromFile(photoFile)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
            }
        } catch (e: Exception) {
            Log.d("DSK ", "Camera is not available.$e")
        }
    }


    private fun getVisitorSign(): ByteArray? {
        var visitorSignByteArray: ByteArray? = null
        if (singleTouchView != null && !singleTouchView!!.isPathNotEmpty) {
            var utilityService = UtilityService();
            visitorSign = utilityService.createBitmap(
                singleTouchView!!, false
            )
            if (visitorSign != null) {
                visitorSignByteArray = utilityService.getBytesPNG(visitorSign)!!
            } else {
                visitorSignByteArray = null
            }
        }
        return visitorSignByteArray
    }

    private fun zoomImageFromThumb(thumbView: View, imageResId: String?) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        currentAnimator?.cancel()

        // Load the high-resolution "zoomed-in" image.
//        Log.d("DSK ", "image Path $imageResId");
        val expandedImageView: ImageView = findViewById(R.id.imageViewVisitorPhoto1)
//        val bitmap = BitmapFactory.decodeFile(imageResId)
//        expandedImageView.setImageBitmap(BitmapFactory.decodeFile(imageResId));
        var myBitmap: Bitmap? = null
        var options: BitmapFactory.Options

        try {
            myBitmap = BitmapFactory.decodeFile(imageResId)
        } catch (e: OutOfMemoryError) {
            try {
                options = BitmapFactory.Options()
                options.inSampleSize = 2
                myBitmap = BitmapFactory.decodeFile(imageResId!!, options!!)
            } catch (excepetion: java.lang.Exception) {
                Log.d("DSK ", "" + excepetion)
            }
        }
        expandedImageView.setImageBitmap(myBitmap)

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        val startBoundsInt = Rect()
        val finalBoundsInt = Rect()
        val globalOffset = Point()

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBoundsInt)
        findViewById<View>(R.id.visitorDetailsSignatureScreen)
            .getGlobalVisibleRect(finalBoundsInt, globalOffset)
        startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
        finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

        val startBounds = RectF(startBoundsInt)
        val finalBounds = RectF(finalBoundsInt)

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        val startScale: Float
        if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
            // Extend start bounds horizontally
            startScale = startBounds.height() / finalBounds.height()
            val startWidth: Float = startScale * finalBounds.width()
            val deltaWidth: Float = (startWidth - startBounds.width()) / 2
            startBounds.left -= deltaWidth.toInt()
            startBounds.right += deltaWidth.toInt()
        } else {
            // Extend start bounds vertically
            startScale = startBounds.width() / finalBounds.width()
            val startHeight: Float = startScale * finalBounds.height()
            val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
            startBounds.top -= deltaHeight.toInt()
            startBounds.bottom += deltaHeight.toInt()
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.alpha = 0f
        expandedImageView.visibility = View.VISIBLE

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.pivotX = 0f
        expandedImageView.pivotY = 0f

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        currentAnimator = AnimatorSet().apply {
            play(
                ObjectAnimator.ofFloat(
                    expandedImageView,
                    View.X,
                    startBounds.left,
                    finalBounds.left
                )
            ).apply {
                with(
                    ObjectAnimator.ofFloat(
                        expandedImageView,
                        View.Y,
                        startBounds.top,
                        finalBounds.top
                    )
                )
                with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale, 1f))
                with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale, 1f))
            }
            duration = shortAnimationDuration.toLong()
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    currentAnimator = null
                }
            })
            start()
        }

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        expandedImageView.setOnClickListener {
            currentAnimator?.cancel()

            // Animate the four positioning/sizing properties in parallel,
            // back to their original values.

            currentAnimator = AnimatorSet().apply {
                play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left)).apply {
                    with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top))
                    with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale))
                    with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale))
                }
                duration = shortAnimationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        thumbView.alpha = 1f
                        expandedImageView.visibility = View.GONE
                        currentAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        thumbView.alpha = 1f
                        expandedImageView.visibility = View.GONE
                        currentAnimator = null
                    }
                })
                start()
            }
        }
    }
}