package com.runtime.permission.`in`.kotlin.ui.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.documentfile.provider.DocumentFile
import com.runtime.permission.`in`.kotlin.R
import com.runtime.permission.`in`.kotlin.permissionutils.ManagePermission
import com.runtime.permission.`in`.kotlin.permissionutils.PermissionDialog
import com.runtime.permission.`in`.kotlin.permissionutils.PermissionName
import com.runtime.permission.`in`.kotlin.saf.SAFRequestCode
import com.runtime.permission.`in`.kotlin.saf.SAFUtils
import java.text.SimpleDateFormat
import java.util.*

class AskSinglePermissionInActivity : AppCompatActivity() {

    private val TAG = AskSinglePermissionInActivity::class.java.simpleName

    private lateinit var askSinglePermissionButton: Button

    private val SINGLE_PERMISSION_REQUEST_CODE = 1001
    private val SINGLE_PERMISSIONS_FROM_SETTING_REQUEST_CODE = 2001
    private val CAPTURE_IMAGE_REQUEST_CODE = 3001

    private lateinit var managePermission: ManagePermission

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.out.println("==============================onCreate(Bundle savedInstanceState)==============================");

        title = "Single Permission In Activity"

        setContentView(R.layout.activity_ask_single_permission_in)

        initializeView()
        initializeObject()
        setOnClickListener()
    }

    protected fun initializeView() {
        askSinglePermissionButton = findViewById(R.id.askSinglePermissionButton)
    }

    protected fun initializeObject() {
        managePermission = ManagePermission(this@AskSinglePermissionInActivity)
    }

    protected fun setOnClickListener() {
        askSinglePermissionButton.setOnClickListener {
            if (managePermission.hasPermission(PermissionName.CAMERA))
            {
                Log.e(TAG, "permission already granted");

                captureImageAndSave();
            }
            else
            {
                Log.e(TAG, "permission is not granted, request for permission");

                ActivityCompat.requestPermissions(this, arrayOf(PermissionName.CAMERA), SINGLE_PERMISSION_REQUEST_CODE);
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            SINGLE_PERMISSION_REQUEST_CODE -> if (grantResults.size > 0) {
                val permission = permissions[0]
                if (permission.equals(PermissionName.CAMERA, ignoreCase = true)) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "camera permission granted")
                        captureImageAndSave()
                    } else if (managePermission.shouldShowRequestPermissionRationale(permission)) {
                        Log.e(TAG, "camera permission denied")
                        ActivityCompat.requestPermissions(
                            this@AskSinglePermissionInActivity,
                            arrayOf(PermissionName.CAMERA),
                            SINGLE_PERMISSION_REQUEST_CODE
                        )
                    } else {
                        Log.e(TAG, "camera permission denied and don't ask for it again")
                        PermissionDialog.permissionDeniedWithNeverAskAgain(
                            this@AskSinglePermissionInActivity,
                            R.drawable.permission_ic_camera,
                            "Camera Permission",
                            "Kindly allow Camera Permission from Settings, without this permission the app is unable to provide photo capture feature. Please turn on permissions at [Setting] -> [Permissions]>",
                            permission,
                            SINGLE_PERMISSIONS_FROM_SETTING_REQUEST_CODE
                        )
                    }
                }
            }
            else -> throw RuntimeException("unhandled permissions request code: $requestCode")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SINGLE_PERMISSIONS_FROM_SETTING_REQUEST_CODE) {
            if (managePermission.hasPermission(PermissionName.CAMERA)) {
                Log.e(TAG, "permission granted from settings")
                captureImageAndSave()
            } else {
                Log.e(TAG, "permission is not granted, request for permission, from settings")
                ActivityCompat.requestPermissions(
                    this@AskSinglePermissionInActivity,
                    arrayOf(PermissionName.CAMERA),
                    SINGLE_PERMISSION_REQUEST_CODE
                )
            }
        }
        if (resultCode == RESULT_OK) {
            if (data != null) {
                val uri: Uri? = data.data
                if (uri != null) {
                    if (SAFRequestCode.SELECT_FOLDER_REQUEST_CODE == requestCode) {
                        /* Save the obtained directory permissions */
                        val takeFlags =
                            data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        contentResolver.takePersistableUriPermission(uri, takeFlags)

                        /* Save uri */
                        val sharedPreferences = getSharedPreferences(
                            SAFUtils.SAF_SHARED_PREFERENCES_FILE_NAME,
                            Context.MODE_PRIVATE
                        )
                        val sharedPreferencesEditor = sharedPreferences.edit()
                        sharedPreferencesEditor.putString(SAFUtils.ALLOW_DIRECTORY, uri.toString())
                        sharedPreferencesEditor.apply()
                    }
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            Log.e(TAG, "Activity canceled")
        } else {
            Log.e(TAG, "Something want wrong")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun captureImageAndSave() {
        Log.e(TAG, "capture image and save image require, Camera and Storage Permission")
        var file: DocumentFile? = null
        val rootDirectory = SAFUtils.takeRootDirectoryWithPermission(
            applicationContext,
            this@AskSinglePermissionInActivity
        )
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileNameWithExtension = "IMG_$timeStamp.png"
        if (rootDirectory != null) {
            file = createFileOwnDirectory(rootDirectory, "captureImages", fileNameWithExtension)
        }
        if (file != null) {
            val fileUri: Uri = SAFUtils.getUri(file)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, CAPTURE_IMAGE_REQUEST_CODE)
            }
        }
    }

    private fun createFileOwnDirectory(
        rootDirectory: DocumentFile,
        childDirectoryName: String,
        fileNameWithExtension: String
    ): DocumentFile? {
        val childDirectory: DocumentFile?
        val file: DocumentFile
        childDirectory = if (rootDirectory.findFile(childDirectoryName) == null) {
            SAFUtils.createDirectory(rootDirectory, childDirectoryName)
        } else {
            rootDirectory.findFile(childDirectoryName)
        }
        assert(childDirectory != null)
        file = SAFUtils.createFile(childDirectory, "image/png", fileNameWithExtension)
        return file
    }

    /* ========================================================================================== */
    /* Life cycle method                                                                          */
    /* ========================================================================================== */
    override fun onStart() {
        super.onStart()
        println("==============================onStart()==============================")
    }

    override fun onRestart() { /* Only called after onStop() */
        super.onRestart()
        println("==============================onRestart()==============================")
    }

    override fun onResume() {
        super.onResume()
        println("==============================onResume()==============================")
    }

    override fun onPause() {
        super.onPause()
        println("==============================onPause()==============================")
    }

    override fun onStop() {
        super.onStop()
        println("==============================onStop()==============================")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("==============================onDestroy()==============================")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        println("==============================onBackPressed()==============================")
    }
}