package com.kanyandula.nyasa.ui


import androidx.appcompat.app.AppCompatActivity
import com.kanyandula.nyasa.session.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {

     val TAG: String = "AppDebug"

    @Inject
    lateinit var sessionManager: SessionManager
}