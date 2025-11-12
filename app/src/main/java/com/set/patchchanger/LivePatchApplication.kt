package com.set.patchchanger

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class with Hilt annotation.
 *
 * @HiltAndroidApp triggers Hilt's code generation.
 * This must be added to the application tag in AndroidManifest.
 */
@HiltAndroidApp
class LivePatchApplication : Application()