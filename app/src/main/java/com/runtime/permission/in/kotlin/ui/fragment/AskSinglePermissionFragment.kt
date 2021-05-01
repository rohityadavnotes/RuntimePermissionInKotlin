package com.runtime.permission.`in`.kotlin.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import com.runtime.permission.`in`.kotlin.R
import com.runtime.permission.`in`.kotlin.fragmentutils.IFragment
import com.runtime.permission.`in`.kotlin.permissionutils.ManagePermission
import com.runtime.permission.`in`.kotlin.permissionutils.PermissionDialog
import com.runtime.permission.`in`.kotlin.permissionutils.PermissionName
import com.runtime.permission.`in`.kotlin.saf.SAFRequestCode
import com.runtime.permission.`in`.kotlin.saf.SAFUtils
import java.text.SimpleDateFormat
import java.util.*

class AskSinglePermissionFragment : Fragment(), IFragment {

    private val TAG = AskSinglePermissionFragment::class.java.simpleName

    private var bundleData: String? = null

    private lateinit var rootView: View
    private lateinit var askSinglePermissionButton: Button

    private val SINGLE_PERMISSION_REQUEST_CODE = 1001
    private val SINGLE_PERMISSIONS_FROM_SETTING_REQUEST_CODE = 2001
    private val CAPTURE_IMAGE_REQUEST_CODE = 3001

    private lateinit var managePermission: ManagePermission

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle?) =
            AskSinglePermissionFragment().apply {
                arguments = bundle
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            bundleData = arguments!!.getString("bundle_key")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_ask_single_permission, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        initializeObject()
        setOnClickListener()
    }

    protected fun initializeView() {
        askSinglePermissionButton = rootView.findViewById(R.id.askSinglePermissionButton)
    }

    protected fun initializeObject() {
        managePermission = ManagePermission(activity)
    }

    protected fun setOnClickListener() {
        askSinglePermissionButton.setOnClickListener {
            if (managePermission.hasPermission(PermissionName.CAMERA)) {
                Log.e(TAG, "permission already granted")

                captureImageAndSave()
            } else {
                Log.e(TAG, "permission is not granted, request for permission")

                requestPermissions(
                    arrayOf(PermissionName.CAMERA),
                    SINGLE_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun getFragmentTag(): String? {
        return TAG
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
                        requestPermissions(
                            arrayOf(PermissionName.CAMERA),
                            SINGLE_PERMISSION_REQUEST_CODE
                        )
                    } else {
                        Log.e(TAG, "camera permission denied and don't ask for it again")
                        PermissionDialog.permissionDeniedWithNeverAskAgain(
                            activity,
                            this,
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
                requestPermissions(
                    arrayOf(PermissionName.CAMERA),
                    SINGLE_PERMISSION_REQUEST_CODE
                )
            }
        }
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val uri: Uri? = data.data
                if (uri != null) {
                    if (SAFRequestCode.SELECT_FOLDER_REQUEST_CODE == requestCode) {
                        /* Save the obtained directory permissions */
                        val takeFlags =
                            data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        activity!!.contentResolver.takePersistableUriPermission(uri, takeFlags)

                        /* Save uri */
                        val sharedPreferences = activity!!.getSharedPreferences(
                            SAFUtils.SAF_SHARED_PREFERENCES_FILE_NAME,
                            Context.MODE_PRIVATE
                        )
                        val sharedPreferencesEditor = sharedPreferences.edit()
                        sharedPreferencesEditor.putString(SAFUtils.ALLOW_DIRECTORY, uri.toString())
                        sharedPreferencesEditor.apply()
                    }
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e(TAG, "Activity canceled")
        } else {
            Log.e(TAG, "Something want wrong")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun captureImageAndSave() {
        Log.e(TAG, "capture image and save image require, Camera and Storage Permission")
        var file: DocumentFile? = null
        val rootDirectory = SAFUtils.takeRootDirectoryWithPermission(context, activity)
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
            if (intent.resolveActivity(activity!!.packageManager) != null) {
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
}