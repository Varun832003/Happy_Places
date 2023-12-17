package com.example.happyplaces.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import com.example.happyplaces.R
import com.example.happyplaces.database.DataBaseHandler
import com.example.happyplaces.models.HappyPlaceModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddHappyPlacesActivity : AppCompatActivity()  /*View.OnClickListener*/{

    private var et_date:AppCompatEditText? = null
    private var tv_add_image: TextView? = null
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var iv_place_image: AppCompatImageView? = null
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var btn_save: Button? = null
    private var et_title: AppCompatEditText? = null
    private var et_description: AppCompatEditText? = null
    private var et_location: AppCompatEditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_places)

         et_date = findViewById(R.id.et_date)
        tv_add_image = findViewById((R.id.tv_add_image))
        iv_place_image = findViewById(R.id.iv_place_image)
        et_title = findViewById(R.id.et_title)
        et_description = findViewById(R.id.et_description)
        et_location = findViewById(R.id.et_location)
        btn_save = findViewById(R.id.btn_save)
        val toolBar_add_place: Toolbar = findViewById(R.id.toolbar_add_place)
        setSupportActionBar(toolBar_add_place)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolBar_add_place.setNavigationOnClickListener {
            onBackPressed()
        }

        dateSetListener = DatePickerDialog.OnDateSetListener{
            view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDateInView()
        }
        updateDateInView()

        et_date?.setOnClickListener {
            DatePickerDialog(
            this@AddHappyPlacesActivity,
            dateSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show() }

        tv_add_image?.setOnClickListener {
            val pictureDialog = AlertDialog.Builder(this)
            pictureDialog.setTitle("SelectAction")
            val pictureDialogItems = arrayOf("Select photo from Gallery",
                "Capture photo from Camera")
            pictureDialog.setItems(pictureDialogItems){
                dialog, which ->
                when(which){
                    0 -> choosePhotofromGallery()
                    1 -> takePhotoFromCamera()
                }

            }
            pictureDialog.show()
        }
        btn_save?.setOnClickListener {
            when{
                et_title?.text.isNullOrEmpty() ->{
                    Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
                }
                et_description?.text.isNullOrEmpty() ->{
                    Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT).show()
                }
                et_location?.text.isNullOrEmpty() ->{
                    Toast.makeText(this, " Please enter location", Toast.LENGTH_SHORT).show()
                }
                saveImageToInternalStorage == null ->{
                    Toast.makeText(this," Please select an image", Toast.LENGTH_SHORT).show()
                }else ->{
                    val happyPlacesModel = HappyPlaceModel(
                        0,
                        et_title?.text.toString(),
                        saveImageToInternalStorage.toString(),
                        et_description?.text.toString(),
                        et_date?.text.toString(),
                        et_location?.text.toString(),
                        mLatitude,
                        mLongitude
                    )
                
                val dbHandler = DataBaseHandler(this)
                val addHappyPlace = dbHandler.addHappyPlace(happyPlacesModel)
                if(addHappyPlace >0){
                   setResult(Activity.RESULT_OK)
                    finish()
                }
                }
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == GALLERY){
                if(data != null){
                    val contentURI = data.data
                    try{
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,contentURI)

                        saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)

                        Log.e("Saved Image","Path::${saveImageToInternalStorage}")
                        iv_place_image?.setImageBitmap(selectedImageBitmap)
                    }catch (e: IOException){
                        print(e.printStackTrace())
                        Toast.makeText(this,"Failed to load the image from Gallery!",Toast.LENGTH_SHORT).show()
                    }
                }
            }else if(requestCode == CAMERA){
                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap

                 saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)

                Log.e("Saved Image","Path::${saveImageToInternalStorage}")
                iv_place_image?.setImageBitmap(thumbnail)
            }
        }
    }
    private fun takePhotoFromCamera(){
        Dexter.withContext(this).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
        ).withListener(
            object :MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()){
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(cameraIntent, CAMERA)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }

            }).onSameThread().check()
    }
    private fun choosePhotofromGallery() {
            Dexter.withContext(this).withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(
               object :MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                            if (report.areAllPermissionsGranted()){
                               val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                               startActivityForResult(galleryIntent, GALLERY)
                            }
                    }

                   override fun onPermissionRationaleShouldBeShown(
                       permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                       token: PermissionToken?
                   ) {
                       showRationalDialogForPermissions()
                   }

                }).onSameThread().check()
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this).setMessage("It looks like you have turned off permissions required for this feature. It can be enabled under the Application Settings")
            .setPositiveButton("Go to Settings")
            { _, _ ->
                try {
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel"){dialog,_ ->
                dialog.dismiss()
            }.show()
    }


//    override fun onClick(v: View?) {
//        when(v!!.id){
//            R.id.et_date ->{
//                DatePickerDialog(
//                    this@AddHappyPlacesActivity,
//                    dateSetListener,
//                    cal.get(Calendar.YEAR),
//                    cal.get(Calendar.MONTH),
//                    cal.get(Calendar.DAY_OF_MONTH)
//                ).show()
//            }
//        }
//    }

    private fun updateDateInView(){
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        et_date?.setText(sdf.format(cal.time).toString())
    }
    private fun saveImageToInternalStorage(bitmap: Bitmap):Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }
    companion object{
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }
}