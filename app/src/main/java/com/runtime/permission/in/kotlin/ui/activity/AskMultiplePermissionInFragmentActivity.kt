package com.runtime.permission.`in`.kotlin.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.runtime.permission.`in`.kotlin.R
import com.runtime.permission.`in`.kotlin.fragmentutils.ManageFragment
import com.runtime.permission.`in`.kotlin.ui.fragment.AskMultiplePermissionFragment

class AskMultiplePermissionInFragmentActivity : AppCompatActivity() {

    private lateinit var fragmentManager: FragmentManager
    private lateinit var manageFragment: ManageFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "Multiple Permission In Fragment Activity"

        setContentView(R.layout.activity_ask_multiple_permission_in_fragment)

        initializeObject()
    }

    protected fun initializeObject() {
        fragmentManager = supportFragmentManager
        manageFragment = ManageFragment(fragmentManager, R.id.fragmentContainer)
        val profileBundle = Bundle()
        profileBundle.putString("bundle_key", "Ask Multiple Permission Fragment")
        val askMultiplePermissionFragment = AskMultiplePermissionFragment.newInstance(profileBundle)
        manageFragment.addFragment(askMultiplePermissionFragment, null, true)
    }
}