package com.runtime.permission.`in`.kotlin.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.runtime.permission.`in`.kotlin.R
import com.runtime.permission.`in`.kotlin.fragmentutils.ManageFragment
import com.runtime.permission.`in`.kotlin.ui.fragment.AskSinglePermissionFragment.Companion.newInstance

class AskSinglePermissionInFragmentActivity : AppCompatActivity() {

    private lateinit var fragmentManager: FragmentManager
    private lateinit var manageFragment: ManageFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "Single Permission In Fragment Activity"

        setContentView(R.layout.activity_ask_single_permission_in_fragment)

        initializeObject()
    }

    protected fun initializeObject() {
        fragmentManager = supportFragmentManager
        manageFragment = ManageFragment(fragmentManager, R.id.fragmentContainer)
        val profileBundle = Bundle()
        profileBundle.putString("bundle_key", "Ask Single Permission Fragment")
        val askSinglePermissionFragment = newInstance(profileBundle)
        manageFragment.addFragment(askSinglePermissionFragment, null, true)
    }
}