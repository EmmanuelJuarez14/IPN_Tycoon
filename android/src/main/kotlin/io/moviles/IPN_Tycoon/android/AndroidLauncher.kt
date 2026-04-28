package io.moviles.IPN_Tycoon.android

import android.os.Bundle

import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import io.moviles.IPN_Tycoon.Main
import io.moviles.IPN_Tycoon.android.database.RoomTester

/** Launches the Android application. */
class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // PRUEBA DE ARQUITECTURA (Día 3 y 4)
        RoomTester.testDatabase(this)

        initialize(Main(), AndroidApplicationConfiguration().apply {
            useImmersiveMode = true
        })
    }
}
