package com.runtime.permission.`in`.kotlin.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.runtime.permission.`in`.kotlin.R

class AfterPermissionGrantedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "After Permission Granted"
        
        setContentView(R.layout.activity_after_permission_granted)
    }
}