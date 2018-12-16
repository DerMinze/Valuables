package com.anders.valuables

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.widget.EditText
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_add_valuable.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.content.DialogInterface
import android.graphics.Matrix
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.support.design.widget.FloatingActionButton
import android.widget.Toast
import java.lang.Exception


class AddValuableActivity : AppCompatActivity() {
    private val REQUEST_TAKE_PHOTO = 3
    private val REQUEST_PICK_PHOTO = 4
    var mCurrentPhotoPath : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_valuable)

        val fab: FloatingActionButton = findViewById(R.id.saveButton)
        fab.setOnClickListener { returnData() }

        val imageView: ImageView = findViewById(R.id.addItemImageView)
        imageView.setOnClickListener { showPictureDialog() }
    }

    private fun showPictureDialog() {
        val builder = AlertDialog.Builder(this)
        val listener = DialogInterface.OnClickListener {_, which ->
            when (which) {
                0 -> dispatchSelectPictureRequest()
                1 -> dispatchTakePictureRequest()
            }
        }
        builder.setTitle(R.string.picture_select_title)
            .setItems(R.array.picture_select_options, listener)
        builder.create()
        builder.show()
    }

    private fun dispatchSelectPictureRequest() {
        Intent(Intent.ACTION_GET_CONTENT).also { getPictureIntent ->
            getPictureIntent.type = "image/*"
            getPictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(getPictureIntent, REQUEST_PICK_PHOTO)
            }
        }
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
    }

    private fun dispatchTakePictureRequest() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error ocurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.anders.valuables.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir : File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            mCurrentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_PHOTO) {
                try {
                    val bitmap          = MediaStore.Images.Media.getBitmap(contentResolver, data.data)
                    val scaledBitmap    = Bitmap.createScaledBitmap(bitmap, 1024, 1024, false)
                    addItemImageView.setImageBitmap(scaledBitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            else if (requestCode == REQUEST_TAKE_PHOTO) {
                try {
                    val uri: Uri = Uri.parse("file:" + mCurrentPhotoPath)
                    val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

                    val manager: CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
                    var orientation = 0
                    try {
                        val cameraId: String = manager.cameraIdList[0]
                        val characteristic: CameraCharacteristics = manager.getCameraCharacteristics(cameraId)
                        orientation = characteristic.get(CameraCharacteristics.SENSOR_ORIENTATION)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    val rotatedBitmap   : Bitmap = rotateImage(bitmap, orientation.toFloat())
                    val scaledBitmap    : Bitmap = Bitmap.createScaledBitmap(rotatedBitmap, 1024, 1024, false)
                    addItemImageView.setImageBitmap(scaledBitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun returnData() {
        val itemName         : String = findViewById<EditText>(R.id.addItemName).text.toString()
        val itemDescription  : String = findViewById<EditText>(R.id.addItemDescription).text.toString()
        val itemPrice        : String = findViewById<EditText>(R.id.addItemPrice).text.toString()

        if (itemName.trim().isBlank() || itemName.trim().isEmpty()) {
            Toast.makeText(this, R.string.item_name_missing_message, Toast.LENGTH_LONG).show()
            return
        }
        if (itemDescription.trim().isBlank() || itemDescription.trim().isEmpty()) {
            Toast.makeText(this, R.string.item_desription_missing_message, Toast.LENGTH_LONG).show()
            return
        }
        if (itemPrice.trim().isBlank() || itemPrice.trim().isEmpty()) {
            Toast.makeText(this, R.string.item_price_missing_message, Toast.LENGTH_LONG).show()
            return
        }

        val bitmap = (findViewById<ImageView>(R.id.addItemImageView).drawable as BitmapDrawable).bitmap
        val bs = ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bs)

        val extras = Bundle()
        extras.putString("name", itemName)
        extras.putString("description", itemDescription)
        extras.putString("price", itemPrice)
        extras.putByteArray("image", bs.toByteArray())

        val returnIntent = Intent()
        returnIntent.putExtras(extras)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }
}
