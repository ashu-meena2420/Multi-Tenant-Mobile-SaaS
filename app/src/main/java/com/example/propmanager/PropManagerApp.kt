package com.example.propmanager

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class required by Hilt for dependency injection.
 * Registered in AndroidManifest.xml via android:name=".PropManagerApp"
 */
@HiltAndroidApp
class PropManagerApp : Application()
