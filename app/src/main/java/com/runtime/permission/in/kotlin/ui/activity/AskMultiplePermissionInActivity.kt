package com.runtime.permission.`in`.kotlin.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.runtime.permission.`in`.kotlin.R
import com.runtime.permission.`in`.kotlin.permissionutils.ManagePermission
import com.runtime.permission.`in`.kotlin.permissionutils.PermissionDialog
import com.runtime.permission.`in`.kotlin.permissionutils.PermissionName


class AskMultiplePermissionInActivity : AppCompatActivity() {

    private val TAG = AskMultiplePermissionInActivity::class.java.simpleName

    private lateinit var askMultiplePermissionButton: Button

    private val MULTIPLE_PERMISSION_REQUEST_CODE = 1001
    private val MULTIPLE_PERMISSIONS_FROM_SETTING_REQUEST_CODE = 2001

    val MULTIPLE_PERMISSIONS = arrayOf(
        PermissionName.WRITE_CONTACTS,
        PermissionName.CALL_PHONE,
        PermissionName.MANAGE_EXTERNAL_STORAGE
    )

    private lateinit var managePermission: ManagePermission

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "Multiple Permission In Activity"

        setContentView(R.layout.activity_ask_multiple_permission_in)

        initializeView()
        initializeObject()
        setOnClickListener()
    }

    protected fun initializeView() {
        askMultiplePermissionButton = findViewById(R.id.askMultiplePermissionButton)
    }

    protected fun initializeObject() {
        managePermission = ManagePermission(this@AskMultiplePermissionInActivity)
    }

    protected fun setOnClickListener() {
        askMultiplePermissionButton.setOnClickListener{
            if (managePermission.hasPermission(MULTIPLE_PERMISSIONS))
            {
                Log.e(TAG, "permission already granted");

                createContactAndCall();
            }
            else
            {
                Log.e(TAG, "permission is not granted, request for permission");

                ActivityCompat.requestPermissions(
                    this,
                    MULTIPLE_PERMISSIONS,
                    MULTIPLE_PERMISSION_REQUEST_CODE);
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
            MULTIPLE_PERMISSION_REQUEST_CODE -> if (grantResults.size > 0) {
                var i = 0
                while (i < grantResults.size) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        val permission = permissions[i]
                        if (permission.equals(PermissionName.WRITE_CONTACTS, ignoreCase = true)) {
                            val showRationale =
                                managePermission.shouldShowRequestPermissionRationale(permission)
                            if (showRationale) {
                                Log.e(TAG, "write contact permission denied")
                                ActivityCompat.requestPermissions(
                                    this@AskMultiplePermissionInActivity,
                                    MULTIPLE_PERMISSIONS,
                                    MULTIPLE_PERMISSION_REQUEST_CODE
                                )
                                return
                            } else {
                                Log.e(
                                    TAG,
                                    "write contact permission denied and don't ask for it again"
                                )
                                PermissionDialog.permissionDeniedWithNeverAskAgain(
                                    this@AskMultiplePermissionInActivity,
                                    R.drawable.permission_ic_contacts,
                                    "Contacts Permission",
                                    "Kindly allow Contact Permission from Settings, without this permission the app is unable to provide create contact feature. Please turn on permissions at [Setting] -> [Permissions]>",
                                    permission,
                                    MULTIPLE_PERMISSIONS_FROM_SETTING_REQUEST_CODE
                                )
                                return
                            }
                        }
                        if (permission.equals(PermissionName.CALL_PHONE, ignoreCase = true)) {
                            val showRationale =
                                managePermission.shouldShowRequestPermissionRationale(permission)
                            if (showRationale) {
                                Log.e(TAG, "call phone permission denied")
                                ActivityCompat.requestPermissions(
                                    this@AskMultiplePermissionInActivity,
                                    MULTIPLE_PERMISSIONS,
                                    MULTIPLE_PERMISSION_REQUEST_CODE
                                )
                                return
                            } else {
                                Log.e(
                                    TAG,
                                    "call phone permission denied and don't ask for it again"
                                )
                                PermissionDialog.permissionDeniedWithNeverAskAgain(
                                    this@AskMultiplePermissionInActivity,
                                    R.drawable.permission_ic_phone,
                                    "Phone Permission",
                                    "Kindly allow Phone Permission from Settings, without this permission the app is unable to provide calling feature. Please turn on permissions at [Setting] -> [Permissions]>",
                                    permission,
                                    MULTIPLE_PERMISSIONS_FROM_SETTING_REQUEST_CODE
                                )
                                return
                            }
                        }
                        if (permission.equals(
                                PermissionName.MANAGE_EXTERNAL_STORAGE,
                                ignoreCase = true
                            )
                        ) {
                            val showRationale =
                                managePermission.shouldShowRequestPermissionRationale(permission)
                            if (showRationale) {
                                Log.e(TAG, "manage external storage permission denied")
                                ActivityCompat.requestPermissions(
                                    this@AskMultiplePermissionInActivity,
                                    MULTIPLE_PERMISSIONS,
                                    MULTIPLE_PERMISSION_REQUEST_CODE
                                )
                                return
                            } else {
                                Log.e(
                                    TAG,
                                    "manage external storage permission denied and don't ask for it again"
                                )
                                PermissionDialog.permissionDeniedWithNeverAskAgain(
                                    this@AskMultiplePermissionInActivity,
                                    R.drawable.permission_ic_storage,
                                    "Manage Storage Permission",
                                    "Kindly allow Manage Storage Permission from Settings, without this permission the app is unable to provide file read write feature. Please turn on permissions at [Setting] -> [Permissions]>",
                                    permission,
                                    MULTIPLE_PERMISSIONS_FROM_SETTING_REQUEST_CODE
                                )
                                return
                            }
                        }
                    }
                    i++
                }
                Log.e(TAG, "all permission granted, do the task")
                createContactAndCall()
            } else {
                Log.e(TAG, "Unknown Error")
            }
            else -> throw RuntimeException("unhandled permissions request code: $requestCode")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MULTIPLE_PERMISSIONS_FROM_SETTING_REQUEST_CODE) {
            if (managePermission.hasPermission(MULTIPLE_PERMISSIONS)) {
                Log.e(TAG, "permission granted from settings")
                createContactAndCall()
            } else {
                Log.e(TAG, "permission is not granted, request for permission, from settings")
                ActivityCompat.requestPermissions(
                    this@AskMultiplePermissionInActivity,
                    MULTIPLE_PERMISSIONS,
                    MULTIPLE_PERMISSION_REQUEST_CODE
                )
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun createContactAndCall() {
        Log.e(TAG, "create contact and call require, Contact and Phone Permission")
        createContact()
        call()
    }

    private fun createContact() {}

    private fun call() {
        try {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:9753100453")
            startActivity(intent)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}